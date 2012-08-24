package db;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

public class SQL
{
   private static final String[] TABLES = {"TABLE"};
   private static final String[] VIEWS  = {"VIEW"};
   // others: "ALIAS", "SYNONYM"
   
   private static final String QUOTED_VALUES = "[^';]+(?:'[^']*'[^';]*)*[^';]*";
   public static final String SQL_REGEX =
      "(?i)\\s*(select|insert|update|delete|create|drop)" + QUOTED_VALUES;
   public static final Pattern SQL_PATTERN = 
      Pattern.compile ("^" + SQL_REGEX + "(;\\s*)$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
   
   private String database;
   private Connection conn;
   
   public SQL (final String database, final Connection conn)
   {
      this.database = database;
      this.conn = conn;
   }
   
   public String getDatabase()
   {
      return database;
   }
   
   public Connection getConnection()
   {
      return conn;
   }
   
   public void close()
   {
      try { conn.close(); } catch (SQLException x) { System.err.println (x); }
      conn = null;
   }
   
   public List<String> getTables (final String tablePattern)
   {
      return getEntries (TABLES, null, null, tablePattern);
   }
   
   public List<String> getTables (final String catalog,
                                  final String schemaPattern,
                                  final String tablePattern)
   {
      return getEntries (TABLES, catalog, schemaPattern, tablePattern);
   }
   
   public List<String> getViews (final String tablePattern)
   {
      return getEntries (VIEWS, null, null, tablePattern);
   }
   
   /**
    * Static interface which determines if the given <i>tableName</i>
    * is a valid table.  Note that the comparison is
    * case-insensitive to comply with SQL92 standards.
    */
   public boolean exists (final String tableName)
   {
      boolean found = false;
      List<String> tables = getTables (tableName);
      if (tables != null)
      {
         for (String table : tables)
            if (table.equalsIgnoreCase (tableName))
            {
               found = true;
               break;
            }
      }
      return found;
   }

   /**
    * Creates a sorted list of table names matching the given pattern.
    *
    * @param  pattern String - to compare against the meta-data TABLE column
    * @return a list of sorted table names, or null
    */
   public List<String> getEntries (final String[] types,
                                   final String catalog,
                                   final String schemaPattern,
                                   final String tablePattern)
   {
      List<String> tables = null;
      ResultSet rs = null;

      try
      {
         DatabaseMetaData dbmd = conn.getMetaData();
         rs = dbmd.getTables (catalog, schemaPattern, tablePattern, types);
         if (rs != null)
         {
            tables = new ArrayList<String>();
            while (rs.next())
            {
               String tableName = rs.getString ("TABLE_NAME");
               // unique the table list to avoid duplicates
               if (!tables.contains (tableName))
                 tables.add (tableName);
            }
            Collections.sort (tables);
         }
      }
      catch (SQLException x)
      {
         x.printStackTrace();
      }
      finally
      {
         close (null, rs);
      }

      return tables;
   }
   
   public int execute (final CharSequence update) throws SQLException
   {
      int rowsChanged = 0;
      Statement stmt = null;
      try
      {
         stmt = conn.createStatement();
         rowsChanged = stmt.executeUpdate (update.toString());
         // note: valid create/drop statements return -1
      }
      finally
      {
         close (stmt, null);
      }

      return rowsChanged;
   }
   
   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a String.
    *
    * @return the requested field's value from the first record
    */
   public String getString (final CharSequence select) throws SQLException
   {
      String value = null;

      Statement stmt = null;
      ResultSet rs = null;
      
      try
      {
         stmt = conn.createStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            if (rs != null && rs.next())
               value = rs.getString (1);
         }
      }
      finally
      {
         close (stmt, rs);
      }

      return value;
   }

   /**
    * Executes the SQL statement and returns the value from the first column
    * of the first row as a number.  The query should result in a single value;
    * such as "select count(*) from ..."
    */
   public double getDouble (final CharSequence select) throws SQLException
   {
      double value = 0.0;

      Statement stmt = null;
      ResultSet rs = null;
      
      try
      {
         stmt = conn.createStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            if (rs != null && rs.next())
               value = rs.getDouble (1);
         }
      }
      finally
      {
         close (stmt, rs);
      }
      
      return value;
   }
   
   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a Date.
    *
    * @return the requested field's value from the first record
    */
   public Date getDate (final CharSequence select) throws SQLException
   {
      Date value = null;

      Statement stmt = null;
      ResultSet rs = null;
      
      try
      {
         stmt = conn.createStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            if (rs != null && rs.next())
               value = rs.getDate (1);
         }
      }
      finally
      {
         close (stmt, rs);
      }

      return value;
   }

   /**
    * Executes the SQL statement and returns the values from the first
    * column as a List of Strings.
    *
    * @return a (possibly empty) list of the selected field's values
    * for all matching records
    */
   public List<String> getList (final CharSequence select) throws SQLException
   {
      List<String> list = new ArrayList<String>();

      Statement stmt = null;
      ResultSet rs = null;
      
      try
      {
         stmt = conn.createStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            while (rs != null && rs.next())
               list.add (rs.getObject (1).toString());
         }
      }
      finally
      {
         close (stmt, rs);
      }
      
      return list;
   }
   
   /**
    * Executes the SQL statement, select Field1, Field2 from Table where etc,
    * and returns a Map using the values from the first column as the
    * key, and the values from the second column as the value.
    *
    * @return a mapping of the selected field values for all matching
    * records
    */
   public Map<String, String> getMapping (final CharSequence select)
   throws SQLException
   {
      Map<String, String> map = new LinkedHashMap<String, String>();

      Statement stmt = null;
      ResultSet rs = null;
      
      try
      {
         stmt = conn.createStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            while (rs != null && rs.next())
            {
               Object key = rs.getObject (1);
               if (key != null)
               {
                  Object val = rs.getObject (2);
                  if (val != null)
                     map.put (key.toString(), val.toString());
               }
            }
         }
      }
      finally
      {
         close (stmt, rs);
      }

      return map;
   }
   
   /**
    * Executes the give SQL select statement (which should result in a single
    * row), "select * from Table where ...", and returns a Map using
    * the field names as the keys mapped to the values.
    * 
    * Warning: Functions such as SUM(col) return blank names.  To avoid this
    * problem, use 'select SUM(Field) as FieldSum ...' 
    *
    * @return a mapping of the fields to values for a single row
    */
   public Map<String, String> getFieldMap (final CharSequence select) 
   throws SQLException
   {
      Map<String, String> map = new LinkedHashMap<String, String>();

      Statement stmt = null;
      ResultSet rs = null;
      
      try
      {
         stmt = conn.createStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            if (rs != null && rs.next()) // only use the first row
            {
               ResultSetMetaData rsmd = rs.getMetaData();
               if (rsmd != null)
               {
                  int colQty = rsmd.getColumnCount();
                  for (int col = 1; col <= colQty; col++) // JDBC starts at 1
                  {
                     String name = rsmd.getColumnLabel (col);
                     String val = rs.getString (col);
                     if (val != null)
                        map.put (name, val);
                  }
               }
            }
         }
      }
      finally
      {
         close (stmt, rs);
      }
      
      return map;
   }
   
   /**
    * Executes the given SQL select statement, "select * from Table where ...",
    * and returns a TableModel.
    * 
    * Warning: Functions such as SUM(col) return blank names.  To avoid this
    * problem, use 'select SUM(Field) as FieldSum ...' 
    */
   public Model getModel (final CharSequence select) throws SQLException
   {
      Model model = new Model (SQLU.getTableName (select));
      populateModel (model, select);
      return model;
   }
   
   public void populateModel (final Model model, final CharSequence select) 
   throws SQLException
   {
      Statement stmt = null;
      ResultSet rs = null;
      
      try
      {
         stmt = conn.createStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            if (rs != null)
            {
               ResultSetMetaData rsmd = rs.getMetaData();
               if (rsmd != null)
               {
                  int colQty = rsmd.getColumnCount();
                  for (int col = 1; col <= colQty; col++) // JDBC starts at 1
                     model.addColumn (rsmd.getColumnLabel (col));

                  while (rs.next())
                  {
                     Vector<Object> row = new Vector<Object>();
                     for (int col = 1; col <= colQty; col++) // JDBC starts at 1
                        row.add (rs.getObject (col));
                     model.addRow (row);
                  }
               }
            }
         }
      }
      finally
      {
         close (stmt, rs);
      }
   }
   
   private static final Pattern SQL_STMT = 
      Pattern.compile ("([a-z].+?); *$", 
                       Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
   
   public void loadFile (final File file)
   {
      String statements = FileUtils.getText (file);
      Matcher m = SQL_STMT.matcher (statements);
      while (m.find())
      {
         try
         {
            execute (m.group (1));
         }
         catch (SQLException x)
         {
            x.printStackTrace();
         }
      }
   }
   
   public static void close (final Statement stmt, final ResultSet rs)
   {
      if (rs != null)
      {
         try
         { 
            rs.close();
         }
         catch (SQLException x)
         {
            System.err.println (x);
         }
      }
      
      if (stmt != null)
      {
         try
         {
            stmt.close();
         }
         catch (SQLException x)
         {
            System.err.println (x); 
         }
      }
   }
}
