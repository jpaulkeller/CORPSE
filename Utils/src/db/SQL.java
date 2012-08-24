package db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.RandomGUID;

public class SQL implements DAO
{
   private static final String[] TABLES = {"TABLE"};
   private static final String[] VIEWS  = {"VIEW"};
   // others: "ALIAS", "SYNONYM"
   
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

   /**
    * See java.sql.Connection
    * 
    * @param autoCommit
    *           - true or false. Note that setting autoCommit to true will
    *           commit any pending actions as a side effect.
    * @throws SQLException
    */
   public void setAutoCommit (boolean autoCommit) throws SQLException
   {
      if (autoCommit)
         commit(); // commit any pending changes
      if (conn != null)
         conn.setAutoCommit (autoCommit);
   }

   public void commit() throws SQLException
   {
      if (conn != null && !conn.getAutoCommit())
         conn.commit();
   }

   public void rollback() throws SQLException
   {
      if (conn != null && !conn.getAutoCommit())
         conn.rollback();
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
    * Creates a sorted list of entries matching the given patterns.
    *
    * @return a list of sorted entry names, or null
    */
   public List<String> getSchemas()
   {
      List<String> schemas = null;
      ResultSet rs = null;

      try
      {
         DatabaseMetaData dbmd = conn.getMetaData();
         rs = dbmd.getSchemas();
         if (rs != null)
         {
            schemas = new ArrayList<String>();
            while (rs.next())
            {
               String tableName = rs.getString ("TABLE_SCHEM");
               // unique the table list to avoid duplicates
               if (!schemas.contains (tableName))
                  schemas.add (tableName);
            }
            Collections.sort (schemas);
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

      return schemas;
   }

   /**
    * Creates a sorted list of entries matching the given patterns.
    *
    * @return a list of sorted entry names, or null
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

   private static final Pattern GUID = Pattern.compile (Pattern.quote ("${GUID}"));
   private static final Pattern TIME = Pattern.compile (Pattern.quote ("${TIME}"));

   /**
    * This STATIC method is used to expand the GUID and TIME place-holders in sql
    * statements. The purpose is to provide a completely expanded sql statement
    * which can be used in native calls, such as when using prepared statements.
    * 
    * @param sql
    *           - the sql to be expanded. Any instance of ${GUID} or ${TIME}
    *           will be expanded. if none exist, the provided sql is unchanged.
    * @return the (possibly) expanded sql.
    */
   public static String expandSQL (final CharSequence sql)
   {
      String s = sql.toString();

      Matcher m = GUID.matcher (s);
      while (m.find())
         s = m.replaceAll ("'" + RandomGUID.generateGUID() + "'");

      m = TIME.matcher (s);
      if (m.find())
      {
         // SimpleDateFormat df = new SimpleDateFormat ("yyyy/MM/dd:hh:mm:ssaa");
         SimpleDateFormat df = new SimpleDateFormat ("yyyy/MM/dd:kk:mm:ss.SSS");
         df.setTimeZone (TimeZone.getTimeZone ("Etc/GMT0")); // Zulu
         String time = df.format (new Date());
         // String toDate = "TO_DATE('" + time + "', 'yyyy/mm/dd:hh:mi:ssam')";
         String toDate = "TO_TIMESTAMP('" + time + "', 'yyyy/mm/dd:hh24:mi:ss.ff3')";
         s = m.replaceAll (Matcher.quoteReplacement (toDate));
      }
      return s;
   }

   public int execute (final CharSequence update) throws SQLException
   {
      int rowsChanged = 0;
      Statement stmt = null;
      String s = expandSQL (update);
      try
      {
         stmt = getStatement();
         rowsChanged = stmt.executeUpdate (s);
         // note: valid create/drop statements return -1
      }
      finally
      {
         close (stmt, null);
      }

      return rowsChanged;
   }
   
   /**
    * Creates and executes an SQL statement to insert the given values into the
    * given table. A GUID is generated and used as the first value. This serves
    * as the Primary Key. The GUID is returned if the insert succeeded;
    * otherwise null is returned.
    */
   public String insertWithGuid (final String table, final Object... values)
            throws SQLException
   {
      String guid = RandomGUID.generateGUID();
      String insert = SQLU.getInsertWithGuid (table, guid, values);
      int count = execute (insert);
      return count == 1 ? guid : null;
   }

   /**
    * The given SQL insert statement should contain an embedded token (%1$d)
    * representing a place-holder for a GUID. This method will generate a GUID,
    * replace the token with the GUID, and execute it. The GUID is returned if
    * the insert succeeded; otherwise null is returned. For example: INSERT INTO
    * MyTable (Key, Col2, Col3) VALUES ('%1$s', 2, 'Three')
    */
   public String insertWithGuid (final CharSequence insertFormat) throws SQLException
   {
      String guid = RandomGUID.generateGUID();
      String insert = insertFormat.toString();
      if (insert.contains ("%1$s"))
         insert = String.format (insertFormat.toString(), guid);
      else
      {
         Matcher m = GUID.matcher (insert);
         if (m.find())
            insert = m.replaceAll ("'" + guid + "'");
      }
      int count = execute (insert);
      return count == 1 ? guid : null;
   }

   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a String.
    *
    * @return the requested field's value from the first record
    */
   public String getString (final CharSequence select) throws SQLException
   {
      String s = null;

      Statement stmt = null;
      ResultSet rs = null;
      
      try
      {
         stmt = getStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            if (rs != null && rs.next())
            {
               Object value = rs.getObject (1); 
               if (value instanceof oracle.sql.CLOB)
                  s = OracleAPI.getAsString ((oracle.sql.CLOB) value);
               else
                  s = rs.getString(1);
            }
         }
      }
      finally
      {
         close (stmt, rs);
      }

      return s;
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
         stmt = getStatement();
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
         stmt = getStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            if (rs != null && rs.next())
            {
               value = rs.getDate (1);
               Calendar cal = Calendar.getInstance();
               cal.setTime (value);
               cal.set (Calendar.ZONE_OFFSET, 0); // zulu
               value = cal.getTime();
            }
         }
      }
      finally
      {
         close (stmt, rs);
      }

      return value;
   }

   /**
    * Executes the SQL statement "select Field from Table where ..." and returns
    * the first value from the first column as a Timestamp.
    * 
    * @return the requested field's value from the first record
    */
   public Timestamp getTimestamp (final CharSequence select) throws SQLException
   {
      Timestamp value = null;
      Statement stmt = null;
      ResultSet rs = null;

      try
      {
         stmt = getStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            if (rs != null && rs.next())
            {
               value = rs.getTimestamp (1);
               Calendar cal = Calendar.getInstance();
               if ( value != null )
               {
                 cal.setTime (value);
               }
               else
               {
                 long now = System.currentTimeMillis();
                 value = new Timestamp( now );
                 cal.setTimeInMillis( now );
               }
               cal.set( Calendar.ZONE_OFFSET, 0 ); // zulu
               value.setTime( cal.getTimeInMillis() );
            }
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
         stmt = getStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (select.toString());
            while (rs != null && rs.next())
            {
               Object obj = rs.getObject (1);
               if (obj != null)
                  list.add (obj.toString());
            }
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
         stmt = getStatement();
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
         stmt = getStatement();
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

   /**
    * Executes the given SQL select statement, "select * from Table where ...",
    * and returns a TableModel.
    *
    * Warning: Functions such as SUM(col) return blank names. To avoid this
    * problem, use 'select SUM(Field) as FieldSum ...'
    */
   public Model getModel (final CharSequence select, int start, int max) throws SQLException
   {
      Model model = new Model (SQLU.getTableName (select));
      populateModel (model, select, start, max);
      return model;
   }

   public void populateModel (final Model model, final CharSequence select) 
   throws SQLException
   {
      Statement stmt = null;
      ResultSet rs = null;
      
      try
      {
         stmt = getStatement();
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
                  model.setQueryResultTotal (model.getRowCount());
               }
            }
         }
      }
      finally
      {
         close (stmt, rs);
      }
   }

   /**
    * This method populates a Model with rows starting at the row numbered 'start' and
    * only returning a maximum of 'max' rows.
    *
    * @param start int containing the start row
    * @param max int containing the maximum number of rows to be returned
    */
   public void populateModel (final Model model, final CharSequence select, 
                              int start, int max)
   throws SQLException
   {
      Statement stmt = null;
      ResultSet rs = null;

      try
      {
         stmt = getStatement();
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
                  if (start < 0)
                     start = 0;
                  if (max <= 0)
                     max = Integer.MAX_VALUE;

                  boolean haveRows = rs.last();
                  if (haveRows)
                     model.setQueryResultTotal (rs.getRow());
                  if (start == 0)
                     rs.beforeFirst();
                  else
                     haveRows = rs.absolute (start);
                  int fetchCount = 0;
                  while (haveRows && rs.next() && fetchCount < max)
                  {
                    fetchCount++;
                    Vector<Object> row = new Vector<Object>();
                    for (int col = 1; col <= colQty; col++)
                      // JDBC starts at 1
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
   
   private Statement getStatement() throws SQLException
   {
      return conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE,
                                   ResultSet.CONCUR_READ_ONLY);
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
