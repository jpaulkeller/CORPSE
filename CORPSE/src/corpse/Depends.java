package corpse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;

import file.FileUtils;

public final class Depends
{
   private static File dependsOut;
   private static File tablesOut;
   private static PrintWriter dpw;
   private static PrintWriter tpw; 

   private Depends() { }
   
   private static void processDir (final File dir)
   {
      for (Table table : Table.getTables())
         tpw.println(table.getName() + ";" + table.getFile());
      
      for (File f : dir.listFiles())
      {
         if (f.getPath().toUpperCase().contains("SAMPLES"))
            continue;
         else if (f.isDirectory() && !f.getName().startsWith ("."))
            processDir (f);
         else if (f.isFile())
            extractVariables (f);
      }
   }

   private static void extractVariables (final File file)
   {
      System.out.println ("  > " + file);
      
      String tableName = FileUtils.getNameWithoutSuffix(file);
      Table table = Table.getTable(tableName);
      for (Subset subset : table.getSubsets().values())
      {
         String subsetXref = tableName + Constants.SUBSET_CHAR + subset.getName();
         dpw.println (file + "; [" + subsetXref + "] ; {" + subsetXref + "}");
      }
      
      String line = null;
      try
      {
         FileInputStream fis = new FileInputStream (file);
         InputStreamReader isr = new InputStreamReader (fis);
         BufferedReader br = new BufferedReader (isr);
         
         while ((line = br.readLine()) != null)
         {
            if (line.startsWith(Constants.SUBSET_CHAR))
               ;
            else if (line.contains ("{"))
            {
               String stripped = line.replace ('{', '[').replace ('}', ']');
               dpw.println (file + ";" + stripped + ";" + line);
            }
         }
         
         fis.close();
      }
      catch (IOException x)
      {
         System.err.println ("File: " + file);
         System.err.println ("Line: " + line);
         x.printStackTrace (System.err);
      }
   }

   public static void checkDependencies()
   {
      try
      {
         CORPSE.init(true);
         
         tablesOut = new File ("data/Tables/Samples/Tables.tbl");
         tpw = new PrintWriter (tablesOut);
         tpw.println ("\n? Generated by Depends.java at " + new Date() + "\n");
         tpw.println (Constants.COLUMN_CHAR + " Table");
         tpw.println (Constants.COLUMN_CHAR + " Path");
         
         dependsOut = new File ("data/Tables/Samples/Depends.tbl");
         dpw = new PrintWriter (dependsOut);
         dpw.println ("\n? Generated by Depends.java at " + new Date() + "\n");
         dpw.println (Constants.COLUMN_CHAR + " Table");
         dpw.println (Constants.COLUMN_CHAR + " Variable");
         dpw.println (Constants.COLUMN_CHAR + " Value\n");
         
         processDir (new File ("data/Tables"));
         
         tpw.close();
         dpw.close();
         
         System.out.println ("Tables summarized in: " + tablesOut);
         System.out.println ("Variables extracted to: " + dependsOut);
      }
      catch (IOException x)
      {
         x.printStackTrace (System.err);
      }
   }

   public static void main (final String[] args)
   {
      Depends.checkDependencies();
   }
}
