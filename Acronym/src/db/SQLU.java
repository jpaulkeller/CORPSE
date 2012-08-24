package db;

import java.util.StringTokenizer;

/**
 * The <b>SQLU</b> class provides static utility methods for working
 * with Structured Query Language (SQL).
 */
public final class SQLU
{
   public static final String CONNECTION_TABLE_DELIMITER = ":";
   
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
      if (value == null)
         return "''";
      return "'" + value.replaceAll ("'", escapeQuote) + "'";
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
   }
}
