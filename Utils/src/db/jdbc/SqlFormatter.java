package db.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.SQLException;

/**
 * Base class for all database Formatters such as OracleFormatter.
 */
public abstract class SqlFormatter
{

   /**
    * Formats a blob to the following String "'<Blob length = " + blob.length()+">'"
    * This method's output will not translate directly into the database. It is informational only.
    *
    * @param blob The blob to be translated
    * @return The String representation of the blob
    * @throws SQLException the sQL exception
    */
   protected String format(final java.sql.Blob blob) throws SQLException
   {
      return "'<Blob length = " + blob.length() + ">'";
   }

   /**
    * Formats a clob to the following String "'<Clob length = " + clob.length()+">'"
    * This method's output will not translate directly into the database. It is informational only.
    *
    * @param clob The clob to be translated
    * @return The String representation of the clob
    * @throws SQLException the sQL exception
    */
   protected String format(final java.sql.Clob clob) throws SQLException
   {
      return "'<Clob length = " + clob.length() + ">'";
   }

   /**
    * Formats an Array to the following String "array.getBaseTypeName()"
    * This method's output will not translate directly into the database. It is informational only.
    *
    * @param array The array to be translated
    * @return The base name of the array
    * @throws SQLException the sQL exception
    */
   protected String format(final java.sql.Array array) throws SQLException
   {
      return array.getBaseTypeName();
   }

   /**
    * Formats a Ref to the following String "ref.getBaseTypeName()"
    * This method's output will not translate directly into the database. It is informational only.
    *
    * @param ref The ref to be translated
    * @return The base name of the ref
    * @throws SQLException the sQL exception
    */
   protected String format(final java.sql.Ref ref) throws SQLException
   {
      return ref.getBaseTypeName();
   }

   /**
    * Checks the String for null and returns "'" + string + "'".
    *
    * @param string String to be formatted
    * @return formatted String (null returns "NULL")
    * @throws SQLException the sQL exception
    */
   protected String format(final java.lang.String string) throws SQLException
   {
      if (string.equals("NULL"))
         return string;
      else
         return "'" + string + "'";
   }

   /**
    * If object is null, Blob, Clob, Array, Ref, or String this returns the value from 
    * the protected methods in this class that take those Classes.
    *
    * @param o Object to be formatted
    * @return formatted String
    * @throws SQLException the sQL exception
    */
   public String format(final Object o) throws SQLException
   {
      if (o == null)
         return "NULL";
      if (o instanceof Blob)
         return format((Blob) o);
      if (o instanceof Clob)
         return format((Clob) o);
      if (o instanceof Array)
         return format((Array) o);
      if (o instanceof Ref)
         return format((Ref) o);
      if (o instanceof String)
         return format((String) o);
      return o.toString();
   }
}