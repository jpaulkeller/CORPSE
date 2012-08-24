package db;

import java.io.Reader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.TimeZone;

import oracle.sql.CLOB;

import utils.Config;

public final class OracleAPI implements DatabaseAPI
{
   // default connection settings
   private static final String DEFAULT_HOST = "localhost";
   private static final int    DEFAULT_PORT = 1521;
   private static final String DEFAULT_DB   = "XE";
   private static final String DEFAULT_USER = "jacob";
   private static final String DEFAULT_PSWD = "jacob";

   private DatabaseDriver dd;
   private String host;
   private String url;
   private SQL sql;
   
   public OracleAPI()
   {
      this (null);
   }

   public OracleAPI (final String[] args)
   {
      Config config = new Config("server.properties");
      if (args != null)
         config.setArguments (args);

      dd = new OracleDriver();
      url = config.get ("DB_URL");
      System.out.println ("DB_URL: " + url);
      if (url == null)
      {
        host = config.get ("DB_HOST", DEFAULT_HOST);
        int port = config.getInt ("DB_PORT", DEFAULT_PORT);
        String db = config.get ("DB_NAME", DEFAULT_DB);
        url = getURL (host, port, db);
      }
      String user = config.get ("DB_USER", DEFAULT_USER);
      String pswd = config.get ("DB_PSWD", DEFAULT_PSWD);
      if (!connect (user, pswd))
         throw new ServiceConfigurationError ("Unable to connect using: " + url);
   }

   public OracleAPI (final String host, final int port, final String db,
                     final String user, final String pswd)
   {
     dd = new OracleDriver();
     this.host = host;
     url = getURL (host, port, db);
     if (!connect (user, pswd))
         throw new ServiceConfigurationError ("Unable to connect using: " + url);
   }
   
   public SQL getSQL()
   {
      return sql;
   }

   private String getURL (final String host, final int port, final String db)
   {
      return dd.getUrlString() + host + ":" + port + ":" + db; 
   }
   
   private boolean connect (final String user, final String pswd)
   {
      String className = dd.getDriverString();
      try
      {
         System.out.print ("Connecting to: " + url + "... ");
         
         // load the class (without requiring Oracle jar at compile time)
         Class<Driver> c = (Class<Driver>) Class.forName (className);
         System.out.println ("Loaded: " + className);

         Driver driver = c.newInstance();
         DriverManager.registerDriver (driver);
         System.out.println ("Registered Oracle driver: " + url);
         
         Connection conn = DriverManager.getConnection (url, user, pswd);
         System.out.println ("Established connection: " + conn);

         sql = new SQL (host, conn);
         
         System.out.println ("connected.");
      }
      catch (ClassNotFoundException x)
      {
         String message = "Oracle Driver not found: " + className;
         System.err.println (message);
         throw new ServiceConfigurationError (message, x);
      }
      catch (Exception x)
      {
         System.err.println (x);
         throw new ServiceConfigurationError (x.getMessage(), x);
      }
      
      return sql != null;
   }
   
   /** See java.sql.Connection */
   public void setAutoCommit (boolean autoCommit) throws SQLException
   {
      sql.setAutoCommit (autoCommit);
   }

   /** See java.sql.Connection */
   public void commit() throws SQLException
   {
      sql.commit();
   }

   /** See java.sql.Connection */
   public void rollback() throws SQLException
   {
      sql.rollback();
   }
   
   public void close()
   {
      if (sql != null)
         sql.close();
   }
      
   /**
    * Returns the connection.  This is useful for prepared statements.
    * ex: PreparedStatement ps = dbApi.getConnection().prepareStatement(sql);
    *
    * @return the connection
    */
   public Connection getConnection()
   {
     return getSQL().getConnection();
   }

   public List<String> getTables (final String schema)
   {
      return sql.getTables (null, schema, null);
   }

   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a String.
    *
    * @return the requested field's value from the first record
    */
   public String getString (final CharSequence select) throws SQLException
   {
      return sql.getString (select);
   }
   
   /**
    * Executes the SQL statement and returns the value from the first column
    * of the first row as a number.  The query should result in a single value;
    * such as "select count(*) from ..."
    */
   public double getDouble (final CharSequence select) throws SQLException
   {
      return sql.getDouble (select);
   }
   
   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a Date.
    *
    * @return the requested field's value from the first record
    */
   public Date getDate (final CharSequence select) throws SQLException
   {
      return sql.getDate (select);
   }
   
   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a Timestamp.
    *
    * @return the requested field's value from the first record
    */
   public Timestamp getTimestamp (final CharSequence select) throws SQLException
   {
      return sql.getTimestamp (select);
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
      return sql.getList (select);
   }
   
   /**
    * Executes the SQL statement, select Field1, Field2 from Table where etc,
    * and returns a Map using the values from the first column as the
    * key, and the values from the second column as the value.
    *
    * @return a mapping of the selected field values for all matching records
    */
   public Map<String, String> getMapping (final CharSequence select)
   throws SQLException
   {
      return sql.getMapping (select);
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
      return sql.getFieldMap (select);
   }
   
   /**
    * Queries the given table, and returns a TableModel containing all records.
    */
   public Model getTableAll (final CharSequence tableName)
   throws SQLException
   {
      return getTable ("select * from " + tableName);
   }
   
   /**
    * Executes the given SQL select statement (such as "select * from Table where ..."),
    * and returns a TableModel containing the matching records.
    * 
    * Warning: Functions such as SUM(col) return blank names.  To avoid this
    * problem, use 'select SUM(Field) as FieldSum ...' 
    */
   public Model getTable (final CharSequence select) throws SQLException
   {
      return sql.getModel (select);
   }

   /**
    * Executes the given SQL select statement (such as "select * from Table where ..."),
    * and returns a TableModel containing the matching records.
    *
    * Warning: Functions such as SUM(col) return blank names.  To avoid this
    * problem, use 'select SUM(Field) as FieldSum ...'
    *
    * @param select CharSequence containing the select statement
    * @param start int containing the first row number (zero based) to be returned
    * @param max int containing the max number of rows to be returned
    */
   public Model getTable (final CharSequence select, int start, int max) throws SQLException
   {
      return sql.getModel (select, start, max);
   }
   
   /** 
    * Executes the given SQL statement, and returns the number of records affected. 
    */
   public int execute (final CharSequence update) throws SQLException
   {
      return sql.execute (update);
   }
   
   /** 
    * The given SQL insert statement should contain an embedded token (%1$d) 
    * representing a place-holder for a GUID.  This method will generate a GUID,
    * replace the token with the GUID, and execute it.  The GUID is returned if 
    * the insert succeeded; otherwise null is returned.  For example: 
    * INSERT INTO MyTable (Key, Col2, Col3) VALUES ('%1$s', 2, 'Three')
    */
   public String insertWithGuid (final CharSequence insertFormat)
   throws SQLException
   {
      return sql.insertWithGuid (insertFormat);
   }
   
   /** 
    * Creates and executes an SQL statement to insert the given values into
    * the given table.  A GUID is generated and used as the first value.  This
    * serves as the Primary Key.  The GUID is returned if the insert succeeded;
    * otherwise null is returned. 
    */
   public String insertWithGuid (final String table, final Object... values) 
   throws SQLException
   {
      return sql.insertWithGuid (table, values);
   }
   
   public static String getAsString (final CLOB value)
   {
      String s = null;
      
      char[] bytes = new char[512];
      try
      {
         StringBuilder sb = new StringBuilder();
         Reader reader = ((CLOB) value).getCharacterStream();
         int len;
         while ((len = reader.read (bytes)) >= 0)
            for (int i = 0; i < len; i++)
               sb.append (bytes[i]);
         s = sb.toString();
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }

      return s;
   }
   
   public static String getDateTimeSQL (final Date date)
   {
      SimpleDateFormat df = new SimpleDateFormat ("yyyy/MM/dd:kk:mm:ss.SSS");
      df.setTimeZone (TimeZone.getTimeZone ("Etc/GMT0")); // Zulu
      String time = df.format (date != null ? date: new Date());
      // String toDate = "TO_DATE('" + time + "', 'yyyy/mm/dd:hh:mi:ssam')";
      return "TO_TIMESTAMP('" + time + "', 'yyyy/mm/dd:hh24:mi:ss.ff3')";      
   }
   
   public static void main (final String[] args)
   {
      System.out.println ("getDateTimeSQL (null): " + OracleAPI.getDateTimeSQL (null));
      System.out.println();
      
      OracleAPI api = new OracleAPI();
      api.close();
   }
}
