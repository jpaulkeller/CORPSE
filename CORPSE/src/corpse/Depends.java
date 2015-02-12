package corpse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

public final class Depends
{
   private static final Pattern COLLISION = // multiple references to the same table 
      Pattern.compile("(" + Constants.TABLE_XREF + ").+\\1", Pattern.CASE_INSENSITIVE);
         
   private static final Map<String, String> TOKENS = new TreeMap<>(); // token to file
   private static final Set<String> MISSING_TABLES = new HashSet<>();
   private static final Set<String> MISSING_SUBSETS = new HashSet<>();
   private static final Set<String> MISSING_COLUMNS = new HashSet<>();
   
   private static File dependsOut;
   private static File tablesOut;
   private static PrintWriter dpw;
   private static PrintWriter tpw;

   private Depends()
   {
   }

   private static void processDir(final File dir)
   {
      for (Table table : Table.getTables())
         tpw.println(table.getName() + ";" + table.getFile());

      for (File f : dir.listFiles())
      {
         if (f.getPath().toUpperCase().contains("SAMPLES"))
            continue;
         else if (f.isDirectory() && !f.getName().startsWith("."))
            processDir(f);
         else if (f.isFile())
         {
            String suffix = FileUtils.getSuffix(f);
            if (suffix != null && suffix.toLowerCase().equals("tbl"))
               extractVariables(f);
            // TODO: support .cmd files
         }
      }
   }

   private static void extractVariables(final File file)
   {
      System.out.println("  > " + file);
      System.out.flush();

      String tableName = FileUtils.getNameWithoutSuffix(file);
      Table table = Table.getTable(tableName);
      if (table != null)
         for (Subset subset : table.getSubsets().values())
         {
            String subsetXref = tableName + Constants.SUBSET_CHAR + subset.getName();
            dpw.println(file + "; [" + subsetXref + "] ; {" + subsetXref + "}");
         }

      BufferedReader br = null;
      String line = null;
      try
      {
         FileInputStream fis = new FileInputStream(file);
         InputStreamReader isr = new InputStreamReader(fis);
         br = new BufferedReader(isr);
         int lineNum = 0; 

         while ((line = br.readLine()) != null && !line.startsWith(Constants.EOF))
         {
            lineNum++;
            if (line.startsWith(Constants.SUBSET_CHAR))
               continue;
            if (line.startsWith(Constants.COMMENT_CHAR))
               continue;
            
            if (line.contains("{"))
            {
               Matcher m = Constants.TOKEN.matcher(line);
               while (m.find())
               {
                  String token = m.group();
                  if (token.startsWith("{" + Constants.SUBSET_CHAR) || token.startsWith("{" + Constants.COLUMN_CHAR))
                     token = "{" + FileUtils.getNameWithoutSuffix(file) + token.substring(1); // resolve local refs
                  if (!TOKENS.containsKey(token))
                     checkToken(token);
                  TOKENS.put(token, file.toString());
               }
               System.err.flush();
            }
            
            // check for mismatched token references
            int openCount = countIn(line, "{");
            int closeCount = countIn(line, "}");
            if (openCount != closeCount)
            {
               System.err.println("Warning - possible token mismatch on " + lineNum + ": " + line);
               System.err.flush();
            }
            
            // check for possible collision
            Matcher m = COLLISION.matcher(line);
            if (m.find() && !ignoreCollision(file, m.group(1)))
            {
               System.err.println("Warning - possible collision on " + lineNum + ": " + line);
               System.err.flush();
            }
         }
      }
      catch (IOException x)
      {
         System.err.println("File: " + file);
         System.err.println("Line: " + line);
         x.printStackTrace(System.err);
      }
      finally
      {
         FileUtils.close(br);
      }
   }
   
   private static void checkToken(final String token)
   {
      Matcher m = Constants.TABLE_XREF.matcher(token);
      if (m.matches()) // {Table:Subset.Column#Filter#}
      {
         String tbl = m.group(1).toUpperCase();
         if (!MISSING_TABLES.contains(tbl) && !Table.TABLES.containsKey(tbl))
         {
            if (tbl.length() > 1) // ignore 1-character names (which are probably variables) 
               System.err.println("    Missing table: " + tbl);
            MISSING_TABLES.add(tbl);
         }
         else if (!MISSING_TABLES.contains(tbl))
         {
            Table table = Table.getTable(tbl);
            String sub = m.group(2) != null ? m.group(2).toUpperCase() : null;
            if (sub != null && !MISSING_SUBSETS.contains(tbl + ":" + sub) && table.getSubset(sub) == null)
               MISSING_SUBSETS.add(tbl + ":" + sub);
            
            String col = m.group(3) != null ? m.group(3).toUpperCase() : null;
            if (col != null && !MISSING_COLUMNS.contains(tbl + ":" + col) && table.getColumn(col) == null)
               MISSING_COLUMNS.add(tbl + ":" + col);
         }
      }
   }

   private static int countIn(final String text, final String s)
   {
      int count = 0;
      int at = -1;
      while (at < text.length() && (at = text.indexOf(s, at + 1)) >= 0)
         count++;
      return count;
   }
   
   private static final Pattern DEREF = 
      Pattern.compile("\\{[A-Z]+[.]" + Constants.COLUMN_NAME + "\\}", Pattern.CASE_INSENSITIVE);
   
   private static boolean ignoreCollision(final File file, final String token)
   {
      if (file.getName().equals("Generated Name.tbl"))
         return true;
      if (token.toLowerCase().contains("name"))
         return true;
      if (token.toLowerCase().startsWith("{degree"))
         return true;
      if (token.toLowerCase().startsWith("{duration"))
         return true;
      if (token.length() <= 3) // ignore 1-character names (which are probably variables) 
         return true;
      if (DEREF.matcher(token).matches()) // ignore assignment references
         return true;
      return false;
   }
   
   public static void checkDependencies()
   {
      try
      {
         tablesOut = new File("data/Tables/Samples/Tables.tbl");
         tpw = new PrintWriter(tablesOut);
         tpw.println("\n" + Constants.COMMENT_CHAR + " Generated by Depends.java at " + new Date() + "\n");
         tpw.println(Constants.COLUMN_CHAR + " Table");
         tpw.println(Constants.COLUMN_CHAR + " Path");

         dependsOut = new File("data/Tables/Samples/Depends.tbl");
         dpw = new PrintWriter(dependsOut);
         dpw.println("\n" + Constants.COMMENT_CHAR + " Generated by Depends.java at " + new Date() + "\n");
         dpw.println(Constants.COLUMN_CHAR + " Table");
         dpw.println(Constants.COLUMN_CHAR + " Variable");
         dpw.println(Constants.COLUMN_CHAR + " Value\n");

         processDir(new File("data/Tables"));

         for (Entry<String, String> entry : TOKENS.entrySet())
         {
            String token = entry.getKey();
            String stripped = token.replace('{', '[').replace('}', ']');
            dpw.println(entry.getValue() + ";" + stripped + ";" + token);
         }
         
         System.out.println("\nTables summarized in: " + tablesOut);
         System.out.println("Variables extracted to: " + dependsOut);
      }
      catch (IOException x)
      {
         x.printStackTrace(System.err);
      }
      finally
      {
         FileUtils.close(tpw);
         FileUtils.close(dpw);
      }
   }

   public static void main(final String[] args)
   {
      CORPSE.init(false);
      Depends.checkDependencies();
   }
}
