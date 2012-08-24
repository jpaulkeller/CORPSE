package db;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The <b>SQLTypes</b> class essentially extends java.sql.Types
 * (although actually extending it is not possible since the
 * constructor is private). */
 
public final class SQLTypes
{
   private static Map<String, Integer> nameToNum;
   private static Map<Integer, String> numToName;

   static
   {
      nameToNum = new HashMap<String, Integer>();
      nameToNum.put ("ARRAY",         Types.ARRAY);
      nameToNum.put ("BIGINT",        Types.BIGINT);
      nameToNum.put ("BINARY",        Types.BINARY);
      nameToNum.put ("BIT",           Types.BIT);
      nameToNum.put ("BLOB",          Types.BLOB);
      nameToNum.put ("CHAR",          Types.CHAR);
      nameToNum.put ("CLOB",          Types.CLOB);
      nameToNum.put ("DATE",          Types.DATE);
      nameToNum.put ("DECIMAL",       Types.DECIMAL);
      nameToNum.put ("DISTINCT",      Types.DISTINCT);
      nameToNum.put ("DOUBLE",        Types.DOUBLE);
      nameToNum.put ("FLOAT",         Types.FLOAT);
      nameToNum.put ("INTEGER",       Types.INTEGER);
      nameToNum.put ("JAVA_OBJECT",   Types.JAVA_OBJECT);
      nameToNum.put ("LONGVARBINARY", Types.LONGVARBINARY);
      nameToNum.put ("LONGVARCHAR",   Types.LONGVARCHAR);
      nameToNum.put ("NULL",          Types.NULL);
      nameToNum.put ("NUMERIC",       Types.NUMERIC);
      nameToNum.put ("OTHER",         Types.OTHER);
      nameToNum.put ("REAL",          Types.REAL);
      nameToNum.put ("REF",           Types.REF);
      nameToNum.put ("SMALLINT",      Types.SMALLINT);
      nameToNum.put ("STRUCT",        Types.STRUCT);
      nameToNum.put ("TIME",          Types.TIME);
      nameToNum.put ("TIMESTAMP",     Types.TIMESTAMP);
      nameToNum.put ("TINYINT",       Types.TINYINT);
      nameToNum.put ("VARBINARY",     Types.VARBINARY);
      nameToNum.put ("VARCHAR",       Types.VARCHAR);

      // populate the inverse mapping
      numToName = new HashMap<Integer, String>();
      for (String name : nameToNum.keySet())
         numToName.put (nameToNum.get (name), name);
   }

   private SQLTypes()
   {
      // utility class; prevent instantiation
   }
   
   /**
    * Return a common SQL Type for a java.sql.Type returned by the
    * Connection.getMetaData call.  This method is used to map from
    * JDBC unique types to common SQL types. */

   public static String getCommonType (final int type)
   {
      String str = null;
      switch (type)
      {
      case Types.BIT:
      case Types.TINYINT:
      case Types.SMALLINT:
         str = "SMALLINT";
         break;
      case Types.LONGVARBINARY:
         str = "LONGVARBINARY";
         break;
      case Types.BINARY:
         str = "BINARY";
         break;
      case Types.LONGVARCHAR:
         str = "LONGVARCHAR";
         break;
      case Types.NULL:
         str = "NULL";
         break;
      case Types.CHAR:
         str = "CHAR";
         break;
      case Types.NUMERIC:
         str = "NUMERIC";
         break;
      case Types.DECIMAL:
      case Types.INTEGER:
      case Types.BIGINT:
         str = "INTEGER";
         break;
      case Types.FLOAT:
      case Types.DOUBLE:
         str = "FLOAT";
         break;
      case Types.REAL:
         str = "REAL";
         break;
      case Types.VARCHAR:
         str = "VARCHAR";
         break;
      case Types.VARBINARY:
         str = "VARBINARY";
         break;
      case Types.DATE:
         str = "DATE";
         break;
      case Types.TIME:
         str = "TIME";
         break;
      case Types.TIMESTAMP:
         str = "TIMESTAMP";
         break;
      case Types.OTHER:
         str = "OTHER";
         break;

      default:
         System.out.println ("getCommonType() not supported: " + type);
      }

      return str;
   }

   /** A method to allow you to see the mapped types. */

   public static void dumpTypes()
   {
      System.out.println ("Name To Number:");
      for (String name : nameToNum.keySet())
         System.out.println ("   " + name + "\t= " + nameToNum.get (name));
      System.out.println();

      System.out.println ("Number To Name:");
      for (Integer num : numToName.keySet())
         System.out.println ("   " + num + "\t= " + numToName.get (num));
      System.out.println();
   }

   public static Collection<String> getTypeNames()
   {
      return Collections.unmodifiableCollection (numToName.values());
   }

   /**
    * Maps String SQL Types to int constants specified in
    * java.sql.Types.
    *
    * @param  typeName the String type that you want to map.
    * @return int the mapped type as defined in java.sql.Types */

   public static int toInt (final String typeName)
   {
      int type = 0;
      Integer i = nameToNum.get (typeName);
      if (i != null)
         type =  i.intValue();
      return type;
   }

   /**
    * Returns a String to represent the int constant passed as a
    * parameter.
    *
    * @param  type int constant from java.sql.Types to turn to a String
    * @return String representation of constant  */

   public static String toString (final int type)
   {
      return numToName.get (type);
   }

   public static void main (final String[] args)
   {
      SQLTypes.dumpTypes();

      System.out.println ("Test toString()");
      System.out.println ("  " + Types.DATE + " = " +
                          SQLTypes.toString (Types.DATE));
      System.out.println ("  " + Types.DECIMAL + " = " +
                          SQLTypes.toString (Types.DECIMAL));
      System.out.println ("  " + Types.BINARY + " = " +
                          SQLTypes.toString (Types.BINARY));
      System.out.println ("  " + Types.OTHER + " = " +
                          SQLTypes.toString (Types.OTHER));
      System.out.println();

      System.out.println ("Test toInt()");
      System.out.println ("  DATE = "    + SQLTypes.toInt ("DATE"));
      System.out.println ("  DECIMAL = " + SQLTypes.toInt ("DECIMAL"));
      System.out.println ("  BINARY = "  + SQLTypes.toInt ("BINARY"));
      System.out.println ("  OTHER = "   + SQLTypes.toInt ("OTHER"));
      System.out.println();
   }
}
