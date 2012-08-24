package db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import utils.RandomGUID;

/**
 * The <b>SQLU</b> class provides static utility methods for working
 * with Structured Query Language (SQL).
 */
public final class SQLU
{
   public static final String CONNECTION_TABLE_DELIMITER = ":";
   public static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss.SSS";
   
   private static String escapeQuote = "''"; // SQL92 standard

   private SQLU()
   {
      // utility class; prevent instantiation
   }
      /**
    * Static interface to extract and return the (first) table name
    * from a given SQL statement.  Does not require a valid connection.
    *
    * @param sql any SQL statement
    * @return the name of the first table in the given <i>sql</i>.
    */
   public static String getTableName (final CharSequence sql)
   {
      // TBD handle Schema.Table and [Table]
      String tableName = null;

      if (sql != null)
      {
         StringTokenizer st = new StringTokenizer (sql.toString(), " ,(\t\n\r");
         while (st.hasMoreTokens())
         {
            String tok = st.nextToken();
            if (tok.equalsIgnoreCase ("from")   || // select * from TABLE
                tok.equalsIgnoreCase ("update") || // update TABLE
                tok.equalsIgnoreCase ("into")   || // insert into TABLE
                tok.equalsIgnoreCase ("table"))    // create/drop table TABLE
            {
               if (st.hasMoreTokens())
                  tableName = st.nextToken();
               break;
            }
         }
      }

      return tableName;
   }

   /**
    * A static method that single-quotes a given
    * value for use in an SQL statement.  More importantly, it escapes
    * any embedded single-quotes first.
    *
    * This method should be called for any value which could contain
    * single-quotes.  Double quotes are not a problem.
    *
    * Note that data returned by the DBMS will not have the escape characters.
    *
    * Example: quote ("my string") returns "'my string'"
    *
    * @param  value   String - any string value
    * @return         String - single-quoted string value
    */
   public static String quote (final String value)
   {
     return quote(value, "''");
   }

   public static String quote (final String value, String nullValue)
   {
      if (value == null)
         return nullValue;
      return "'" + value.replaceAll ("'", escapeQuote) + "'";
   }

   public static String getInsertWithGuid (final String table,
                                           final String guid,
                                           final Object... values)
   {
      StringBuilder insert = new StringBuilder();
      insert.append ("insert into ");
      insert.append (table);
      insert.append (" values (");
      insert.append ("'" + guid + "'"); // include the Primary Key

      for (Object value : values)
      {
         insert.append (", ");
         if (value == null)
            insert.append ("null");
         else if (value instanceof Number)
            insert.append (value);
         else if (value instanceof Date)
         {
            SimpleDateFormat df = new SimpleDateFormat (DATE_FORMAT);      
            insert.append ("'" + df.format ((Date) value) + "'");
         }            
         else
            insert.append (SQLU.quote (value.toString()));
      }

      insert.append (")");
      
      return insert.toString();
   }
   
   private static void testQuote (final String text)
   {
      System.out.print ("quote (\"" + text + "\")    => [");
      System.out.println (quote (text) + "]");
   }

   public static void main (final String[] args) // for testing
   {
      String s = "select FieldName from Schema.Table_Name";
      String val = SQLU.getTableName (s);
      System.out.println ("getTableName() => " + val);
      System.out.println();

      testQuote ("1234");
      testQuote ("123456");
      testQuote ("ab's");
      testQuote ("abcd's");
      System.out.println();
      
      String guid = RandomGUID.generateGUID();
      String insert = getInsertWithGuid ("TABLE", guid, "Value1", 2, new Date());
      System.out.println (insert);
      System.out.println();
   }
}
