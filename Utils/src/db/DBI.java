package db;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The <b>DBI</b> class provides static utility methods for accessing a DBMS.
 */
public class DBI
{
   private static OracleAPI api = new OracleAPI();
   
   private DBI()
   {
      // utility class; prevent instantiation
   }
   
   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a String.
    *
    * @return the requested field's value from the first record
    */
   public static String getString (final CharSequence select)
   {
      String result = null;
      try
      {
         result = api.getString (select);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + select);
         x.printStackTrace (System.err);
      }
      return result;
   }
   
   /**
    * Executes the SQL statement and returns the value from the first column
    * of the first row as a number.  The query should result in a single value;
    * such as "select count(*) from ..."
    */
   public static double getDouble (final CharSequence select)
   {
      double result = 0;
      try
      {
         result = api.getDouble (select);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + select);
         x.printStackTrace (System.err);
      }
      return result;
   }
   
   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a Date.
    *
    * @return the requested field's value from the first record
    */
   public static Date getDate (final CharSequence select)
   {
      Date result = null;
      try
      {
         result = api.getDate (select);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + select);
         x.printStackTrace (System.err);
      }
      return result;
   }
   
   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a Timestamp.
    *
    * @return the requested field's value from the first record
    */
   public static Timestamp getTimestamp (final CharSequence select)
   {
      Timestamp result = null;
      try
      {
         result = api.getTimestamp (select);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + select);
         x.printStackTrace (System.err);
      }
      return result;
   }
 
   /**
    * Executes the SQL statement and returns the values from the first
    * column as a List of Strings.
    *
    * @return a (possibly empty) list of the selected field's values
    * for all matching records
    */
   
   public static List<String> getList (final CharSequence select)
   {
      List<String> result = null;
      try
      {
         result = api.getList (select);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + select);
         x.printStackTrace (System.err);
      }
      return result;
   }
   
   /**
    * Executes the SQL statement, select Field1, Field2 from Table where etc,
    * and returns a Map using the values from the first column as the
    * key, and the values from the second column as the value.
    *
    * @return a mapping of the selected field values for all matching records
    */
   public static Map<String, String> getMapping (final CharSequence select)
   {
      Map<String, String> result = null;
      try
      {
         result = api.getMapping (select);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + select);
         x.printStackTrace (System.err);
      }
      return result;
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
   public static Map<String, String> getFieldMap (final CharSequence select) 
   {
      Map<String, String> result = null;
      try
      {
         result = api.getFieldMap (select);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + select);
         x.printStackTrace (System.err);
      }
      return result;
   }
   
   /**
    * Queries the given table, and returns a TableModel containing all records.
    */
   public static Model getTableAll (final CharSequence tableName)
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
   public static Model getTable (final CharSequence select)
   {
      Model result = null;
      try
      {
         result = api.getTable (select);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + select);
         x.printStackTrace (System.err);
      }
      return result;
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
   public static Model getTable (final CharSequence select, int start, int max)
   {
      Model result = null;
      try
      {
         result = api.getTable (select, start, max);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + select);
         x.printStackTrace (System.err);
      }
      return result;
   }
   
   /** 
    * Executes the given SQL statement, and returns the number of records affected. 
    */
   public static int execute (final CharSequence update)
   {
      int result = 0;
      try
      {
         result = api.execute (update);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + update);
         x.printStackTrace (System.err);
      }
      return result;
   }
   
   /** 
    * The given SQL insert statement should contain an embedded token (%1$d) 
    * representing a place-holder for a GUID.  This method will generate a GUID,
    * replace the token with the GUID, and execute it.  The GUID is returned if 
    * the insert succeeded; otherwise null is returned.  For example: 
    * INSERT INTO MyTable (Key, Col2, Col3) VALUES ('%1$s', 2, 'Three')
    */
   public static String insertWithGuid (final CharSequence insertFormat)
   {
      String result = null;
      try
      {
         result = api.insertWithGuid (insertFormat);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + insertFormat);
         x.printStackTrace (System.err);
      }
      return result;
   }
   
   /** 
    * Creates and executes an SQL statement to insert the given values into
    * the given table.  A GUID is generated and used as the first value.  This
    * serves as the Primary Key.  The GUID is returned if the insert succeeded;
    * otherwise null is returned. 
    */
   public static String insertWithGuid (final String table, final Object... values) 
   {
      String result = null;
      try
      {
         result = api.insertWithGuid (table, values);
      }
      catch (SQLException x)
      {
         System.err.println (x.getMessage () + ": " + table);
         x.printStackTrace (System.err);
      }
      return result;
   }
   
   public static void main (final String[] args) // for testing
   {
   }
}
