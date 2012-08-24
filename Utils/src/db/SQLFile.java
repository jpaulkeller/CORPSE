package db;

import gui.ComponentTools;
import gui.comp.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

public class SQLFile
{
   private static final String quotedValues = "[^';]+(?:'[^']*'[^';]*)*[^';]*";
   public static final String SQL_PATTERN =
      "\\s*(select|insert|update|delete|create|drop)" + quotedValues;
   private static Pattern commentRegex; // used to ignore comment lines
   private static Pattern sqlRegex;  // to extract SQL statements from stream
   private static Pattern createRegex;
   // used for auto-commit
   private static Pattern dropRegex;
   private static Pattern deleteRegex;

   static
   {
      int flags = Pattern.DOTALL | Pattern.CASE_INSENSITIVE;
      // regular expression to extract SQL statements from input stream
      sqlRegex = Pattern.compile ("^" + SQL_PATTERN + "(;\\s*)$", flags);

      String pattern = "^(#|--).*";
      commentRegex = Pattern.compile (pattern, Pattern.CASE_INSENSITIVE);

      // Note: the following patterns should not contain the ";",
      // since that will be stripped off before the comparison.

      // regular expressions used to convert between TEXT and MEMO fields
      pattern = "^\\s*create\\s+(?:table|view)\\s+([^\\(]+).*$";
      createRegex = Pattern.compile (pattern, flags);
      pattern = "^\\s*drop\\s+.*$";
      dropRegex = Pattern.compile (pattern, flags);
      pattern = "^\\s*delete\\s+.*$";
      deleteRegex = Pattern.compile (pattern, flags);
   }

   private OracleAPI dbAPI;

   public SQLFile()
   {
      dbAPI = new OracleAPI();
   }

   public void setAutoCommit (boolean autoCommit) throws SQLException
   {
      if (autoCommit)
         commit(); // commit any pending changes
      Connection conn = dbAPI.getConnection(); 
      if (conn != null)
         conn.setAutoCommit (autoCommit);
   }

   public void commit() throws SQLException
   {
      Connection conn = dbAPI.getConnection(); 
      if (conn != null && !conn.getAutoCommit())
         conn.commit();
   }

   // Return the number of statements successfully executed (1 if
   // executed, -1 if error, 0 if ignored -- like a failed drop).

   private int executeBatchedStatement (String sql,
                                        String mode,
                                        boolean autoCommit,
                                        PrintStream out)
   throws SQLException
   {
      int executed = 0;

      String command = sql.substring (0, 1).toLowerCase();

      // Hack to force auto-commit for drop/create/delete statements.
      // You must commit the "drop" before calling "create", or commit
      // the "create" or "delete" before calling "insert".  This must
      // be done before executing the statement -- calling commit()
      // after executing does not work.
      Matcher cm = null;

      // commit required after drop, create, or delete
      boolean commitRequired = false;
      boolean drop = false;
      boolean delete = false;
      boolean create = false;
      if ((drop = dropRegex.matcher (sql).matches()) ||
          (delete = deleteRegex.matcher (sql).matches()) ||
          (create = (cm = createRegex.matcher (sql)).matches()))
         commitRequired = true;

      if (mode == null || mode.equals (command))
      {
         if (out != null) out.print (command);
         if (!autoCommit && commitRequired && cm != null)
         {
            setAutoCommit (true);
            executed = executeOne (out, sql, create ? cm.group (1) : null,
                                   create, drop || delete);
            setAutoCommit (false);
         }
         else
            executed = executeOne (out, sql, null, create, drop || delete);
      }
      else if (out != null) out.print (".");

      return executed;
   }

   private static Pattern TOO_BIG = 
      Pattern.compile ("'([^']{4001,})'", Pattern.MULTILINE | Pattern.DOTALL);
   
   private int executeOne (PrintStream out, String sql, String tableName, 
                           boolean create, boolean delete)
   {
      int executed = 0;

      try
      {
         int rowsAffected = 0;

         // if any field > 4000, we have to use a PreparedStatement
         Matcher m = TOO_BIG.matcher (sql);
         if (m.find())
         {
            List<String> values = new ArrayList<String>();
            do
            {
               values.add (m.group (1));
               sql = m.replaceFirst ("?");
            } while (m.find());

            PreparedStatement ps = dbAPI.getConnection().prepareStatement (sql);
            int index = 1;
            for (String value : values)
               ps.setString (index++, value);
            rowsAffected = ps.executeUpdate();
         }
         else
            rowsAffected = dbAPI.execute (sql);
         
         if (rowsAffected == 1)
         {
            executed = 1;
            if (out != null && tableName != null)
               out.println ("\n" + tableName);
         }
         else if (create || delete)
            executed = 1;
         else
         {
            executed = -1;
            if (out != null) out.println ("\nFAILED: " + sql);
         }
      }
      catch (SQLException x)
      {
         System.err.println ("\n" + x);
         System.err.println ("SQL: [" + sql + "]");
         x.printStackTrace (System.err);
      }

      return executed;
   }

   /**
    * Read the given filename of SQL statements, and execute those statements.
    *
    * @param mode currently, either "d" for drop/delete, or anything else
    *        SQL statements, or "-" to read from stdin
    * @param fileName the name of a file containing semi-colon-terminated SQL
    * @param out an optional PrintStream for feedback messages
    * @param  autoCommit true to commit after every statement
    *
    * @return an array of two ints: (1) the number of statements
    * successfully executed and (2) the number of statements that
    * failed to execute (or a negative error code)
    */
   public int[] executeFile (String mode, String fileName, 
                             PrintStream out, boolean autoCommit)
   {
      int[] result = null;

      InputStream in = null;
      try
      {
         if (fileName.equals ("-"))
            in = System.in;
         else
         {
            File file = new File (fileName);
            if (file.isFile())
            {
               in = new FileInputStream (file);
               result = executeStream (mode, in, out, autoCommit);
            }
            else if (file.isDirectory()) // execute all .sql files in the dir
            {
               String[] fileNames = file.list();
               for (String f : fileNames)
               {
                  if (f != null && f.toUpperCase().endsWith (".SQL"))
                  {
                     String path = fileName + File.separator + f;
                     result = executeFile (mode, path, out, autoCommit);
                  }
               }
            }
         }
      }
      catch (IOException x)
      {
         out.println (x.toString());
      }
      finally
      {
         if (!fileName.equals ("-") && (in != null))
            try { in.close(); } catch (IOException x) { /* ignore */ }
      }

      return result;
   }

   /**
    * Read the given stream of SQL statements, and execute those statements.
    *
    * @param mode currently, either "d" for drop/delete, or anything else
    *        SQL statements, or "-" to read from stdin
    * @param in an input stream of SQL statements
    * @param out an optional PrintStream (or StatusBar) for feedback messages
    * @param  autoCommit true to commit after every statement
    *
    * @return an array of two ints: (1) the number of statements
    * successfully executed and (2) the number of statements that
    * failed to execute (or a negative error code)
    */
   public int[] executeStream (String mode, InputStream in,
                               PrintStream out, boolean autoCommit)
   {
      int[] result;
      try
      {
         Reader reader = new InputStreamReader (in, "UTF8");
         result = executeStream (mode, reader, out, autoCommit);
      }
      catch (IOException x)
      {
         out.println (x.toString());
         result = new int[] { 0, -2 };
      }
      return result;
   }

   /**
    * Using the given reader, read and execute a stream of SQL statements.
    *
    * @param mode currently, either "d" for drop/delete, or anything else
    *        SQL statements, or "-" to read from stdin
    * @param reader an input reader of SQL statements
    * @param out an optional PrintStream (or StatusBar) for feedback messages
    * @param  autoCommit true to commit after every statement
    *
    * @return an array of two ints: (1) the number of statements
    * successfully executed and (2) the number of statements that
    * failed to execute (or a negative error code)
    */
   public int[] executeStream (String mode,
                               Reader reader,
                               PrintStream out,
                               boolean autoCommit)
   {
      int[] result;
      
      try
      {
         if (!autoCommit)
            dbAPI.getConnection().setAutoCommit (false);
         
         result = readStatements (mode, reader, out);
         
         if (!autoCommit)
            dbAPI.getConnection().commit();
      }
      catch (SQLException x)
      {
         out.println (x.toString());
         result = new int[] { 0, -1 };
      }
      catch (IOException x)
      {
         out.println (x.toString());
         result = new int[] { 0, -2 };
      }

      return result;
   }

   /**
    * Returns the number of statements successfully executed.
    */
   private int[] readStatements (String mode, Reader reader, PrintStream out)
   throws IOException, SQLException
   {
      int executed = 0;
      int failed = 0;

      BufferedReader br = new BufferedReader (reader);
      StringBuilder buf = new StringBuilder();

      Connection conn = dbAPI.getConnection(); 
      boolean autoCommit = conn.getAutoCommit();

      int lineNum = 0;          // line in file
      int stmtNum = 0;

      String line;
      while ((line = br.readLine()) != null)
      {
         lineNum++;

         // only check for comments between statements (or in a create)
         if (buf.length() == 0 || buf.indexOf ("create") == 0)
         {
            if (line.trim().length() == 0) // line is empty
               continue; // ignore blank lines
            if (commentRegex.matcher (line).matches()) // comment line
               continue; // ignore comment lines
         }

         buf.append (line);

         try
         {
            Matcher match = sqlRegex.matcher (buf);
            if (match.matches())   // end of statement found
            {
               // remove the ";" (Oracle doesn't like it)
               buf.replace (match.start (2), match.end (2), "");

               stmtNum++;
               int count = executeBatchedStatement
                  (buf.toString().trim(), mode, autoCommit, out);
               if (count > 0)
                  executed++;
               else if (count < 0)
                  failed++;
               // count == 0 is ignored (e.g., failed drop statement)

               buf.setLength (0); // SQL statement handled; reset buffer
            }
            else
               buf.append ("\n");  // add separator
         }
         catch (StackOverflowError x)
         {
            out.println (x.getMessage());
            out.println ("\nLine number: " + lineNum);
            out.println ("Statement number: " + stmtNum);
            out.println ("\nInvalid SQL: " + buf);
            buf.setLength (0);
         }
      }

      // check if there is any remaining un-executed text
      if (buf.toString().trim().length() > 0)
      {
         failed++;
         if (out != null)
         {
            buf.append ("\n");
            out.println ("\nFAILED: (line " + lineNum + ") " +
                         buf.substring (0, buf.indexOf ("\n")));
            if (lineNum != stmtNum)
               out.println ("Statement number: " + stmtNum);
         }
      }

      if (out != null)
         out.println();

      return new int[] { executed, failed };
   }

   private void executeOneFile (String mode, String file,
                                PrintStream out, boolean autoCommit)
   {     
      int[] qty = executeFile (mode, file, out, autoCommit);
      if (qty == null)
         System.out.println ("SQL execution failed");
      else if (qty[1] < 0)
         System.out.println ("SQL execution failed: " + qty[1]);
      else
      {
         System.out.println (qty[0] + " statement(s) executed.");
         if (qty[1] > 0)
            System.out.println (qty[1] + " statement(s) failed to execute.");
      }
   }

   public static void main (final String[] args) // for testing
   {
      SQLFile sqlFile = new SQLFile();
      
      ComponentTools.setDefaults();
      String dir = ".";
      FileChooser fc = new FileChooser ("Select SQL File", dir);
      fc.setRegexFilter (".+[.]sql", "SQL files");
      
      File file = null;
      try
      {
         do // repeatedly prompt for and execute file
         {
            file = null;
            if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
            {
               file = fc.getSelectedFile();
               if (file != null)
               {
                  System.out.println ("\nLoading: " + file);
                  sqlFile.executeOneFile (null, file.getPath(), System.out, true);
               }
            }
         } while (file != null);
      }
      finally
      {
         sqlFile.dbAPI.close();
      }
   }
}
