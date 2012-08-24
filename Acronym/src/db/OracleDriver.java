package db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Types;

public class OracleDriver extends DatabaseDriver
{
   private static final long serialVersionUID = 1;

   @Override
   public String getDriverString() 
   {
      return "oracle.jdbc.driver.OracleDriver";
   }

   @Override
   public String getUrlString() 
   {
      return "jdbc:oracle:thin:@";
   }

   @Override
   public boolean translateJdbcType (final Column col) 
   {
      boolean translated = true;
      int type = col.getType();
      int size = col.getWidth();

      switch (type) 
      {
      case Types.CHAR:
         if (size <= getMaxCharLen()) 
         {
            col.setTypeName ("CHAR");
            col.setSizeRequired (true);
         }
         else if (size > getMaxCharLen()) // Don't allow strings > 255
         {
            col.setType (Types.LONGVARCHAR);
            translateJdbcType (col);
         }
         break;

      case Types.VARCHAR:
         if (size <= getMaxVarcharLen()) 
         {
            col.setTypeName ("VARCHAR");
            col.setSizeRequired (true);
         }
         else if (size > getMaxVarcharLen()) // Don't allow strings > 255
         {
            col.setType (Types.LONGVARCHAR);
            translateJdbcType (col);
         }
         break;

      case Types.LONGVARCHAR:
         col.setTypeName ("LONG VARCHAR");
         col.setSizeRequired (false);
         break;

      case Types.VARBINARY:
         if (size == 0)
            size = getMaxVarbinaryLen();
         if (size <= getMaxVarbinaryLen()) 
         {
            col.setTypeName ("RAW");
            col.setSizeRequired (true);
         }
         else if (size > getMaxVarbinaryLen()) // Don't allow strings > 255
         {
            col.setTypeName ("LONG RAW");
            col.setSizeRequired (false);
         }
         col.setWidth (size);
         break;

      case Types.OTHER:
         if (col.getTypeName().equals ("FLOAT")) 
         {
            if (size == 63) 
            {
               col.setTypeName ("REAL");
               col.setType (Types.REAL);
            }
            else if (size == 126) 
            {
               col.setTypeName ("FLOAT");
               col.setType (Types.FLOAT);
            }
         }
         else
            translated = false;
         break;

      default:
         translated = false;
      }

      return (translated);
   }

   @Override
   public int getMaxVarcharLen() 
   {
      return 2000;
   }

   @Override
   public String getTextType()
   {
      return ("LONG");
   }

   /** This format is quite different than the normal ODBC format.. */
   // Steve: This format worked '23 JAN 1955'

   @Override
   public String getTimestampFormat()
   {
      return ("ddHHmm MMMyy");
   }
   
   @SuppressWarnings("unchecked")
   public static SQL connect (final String url, final String user, final String pswd)
   {
      SQL sql = null;
      DatabaseDriver dd = new OracleDriver();
      String className = dd.getDriverString();
      
      try
      {
         // load the class (without requiring Oracle jar at compile time)
         System.out.print ("Loading " + className + "... ");
         Class<Driver> c = (Class<Driver>) Class.forName (className);
         System.out.println ("loaded.");

         System.out.print ("Instantiating Oracle driver... ");
         Driver driver = c.newInstance();
         System.out.println (driver);
         
         System.out.println ("Registering Oracle driver...");
         DriverManager.registerDriver (driver);

         System.out.println ("URL = " + url);
         
         System.out.print ("Establishing connection... ");
         Connection conn = DriverManager.getConnection (url, user, pswd);
         System.out.println (conn);
         
         String dbName = "JACOB"; // TODO
         sql = new SQL (dbName, conn);
      }
      catch (ClassNotFoundException x)
      {
         System.err.println ("Oracle Driver not found: " + className);
         System.err.println (x.getMessage());
      }
      catch (Exception x)
      {
         System.err.println (x.getMessage());
         x.printStackTrace (System.err);
      }
      
      return sql;
   }

   public static void main (final String[] args)
   {
      String url = System.getenv ("DB_URL"); // jdbc:oracle:thin:@12.69.43.74:1521:ORCL 
      String user = System.getenv ("DB_USER");
      String pswd = System.getenv ("DB_PSWD");
      
      url = "jdbc:oracle:thin:@grid.ngis-cville.com:1521:dev";
      user = "jacob_data";
      pswd = "jacob";
      
      if (url != null)
      {
         System.out.println ("Connecting to: " + url);
         OracleDriver.connect (url, user, pswd);
      }
         
      /*
      String host = argMap.get ("-host");
      String port = argMap.get ("-port"); // 1433
      String db   = argMap.get ("-database"); // SID
      
         url = dd.getUrlString() + db;
         url = dd.getUrlString() +
         "(description=(address=(host=" + host + ")(protocol=tcp)" +
         "(port=" + port + "))(connect_data=(sid=" + db + ")))";
      */
   }
}
