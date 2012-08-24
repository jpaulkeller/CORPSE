package db;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableModel;

/**
 * Static convenience methods for accessing a web server's database
 * from client applets.
 */
public interface DatabaseAPI
{
   /** 
    * Executes the given SQL statement, and returns the number of records affected. 
    */
   int execute (final CharSequence sql) throws SQLException;

   /** 
    * The given SQL insert statement should contain an embedded token (%1$d) 
    * representing a place-holder for a GUID.  This method will generate a GUID,
    * replace the token with the GUID, and execute it.  The GUID is returned if 
    * the insert succeeded; otherwise null is returned.  For example: 
    * INSERT INTO MyTable (Key, Col2, Col3) VALUES ('%1$s', 2, 'Three')
    */
   String insertWithGuid (final CharSequence insertFormat) throws SQLException;
   
   /** 
    * Creates and executes an SQL statement to insert the given values into
    * the given table.  A GUID is generated and used as the first value.  This
    * serves as the Primary Key.  The GUID is returned. 
    */
   String insertWithGuid (final String table, final Object... values) throws SQLException;

   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a String.
    *
    * @return the requested field's value from the first record
    */
   String getString (final CharSequence sql) throws SQLException;
   
   /**
    * Executes the SQL statement and returns the value from the first column
    * of the first row as a number.  The query should result in a single value;
    * such as "select count(*) from ..."
    */
   double getDouble (final CharSequence select) throws SQLException;
   
   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a Date.
    *
    * @return the requested field's value from the first record
    */
   Date getDate (final CharSequence select) throws SQLException;   

   /**
    * Executes the SQL statement "select Field from Table where ..."
    * and returns the first value from the first column as a Timestamp.
    *
    * @return the requested field's value from the first record
    */
   Timestamp getTimestamp (final CharSequence select) throws SQLException;
   
   /**
    * Executes the SQL statement and returns the values from the first
    * column as a List of Strings.
    *
    * @return a (possibly empty) list of the selected field's values
    * for all matching records
    */
   List<String> getList (final CharSequence sql) throws SQLException;

   /**
    * Returns map of two field values (Field1's value as the key
    * mapped to Field2's value) for multiple records. */
   Map<String, String> getMapping (final CharSequence sql) throws SQLException;
   
   /**
    * Returns map of (uppercased) field name to values (for a single
    * record). */
   Map<String, String> getFieldMap (final CharSequence sql) throws SQLException;
   
   /**
    * Executes the given SQL select statement (such as "select * from Table where ..."),
    * and returns a TableModel containing the matching records.
    * 
    * Warning: Functions such as SUM(col) return blank names.  To avoid this
    * problem, use 'select SUM(Field) as FieldSum ...' 
    */
   TableModel getTable (final CharSequence select) throws SQLException;
}
