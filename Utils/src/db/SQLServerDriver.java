package db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import utils.SysProperties;

public class SQLServerDriver extends DatabaseDriver
{
   private static final long serialVersionUID = 0;

   @Override
   public String getDriverString() 
   {
      return "com.microsoft.jdbc.sqlserver.SQLServerDriver";
   }

   @Override
   public String getUrlString() 
   {
      return "jdbc:microsoft:sqlserver://";
   }

   @Override
   public boolean translateJdbcType (final Column col) 
   {
      boolean translated = true;
      int type = col.getType();
      int size = col.getWidth();

      switch (type) 
      {
      case Types.TIMESTAMP:
         col.setTypeName ("DATETIME");
         break;

      case Types.INTEGER:
         col.setTypeName ("INT");
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
         col.setTypeName ("TEXT");
         break;

      default:
         translated = false;
      }

      return translated;
   }

   @Override
   public int getMaxVarcharLen() 
   {
      return 2000;
   }

   public Connection connect (final String host, final String port, final String db,
                              final String user, final String pass)
   {
      Connection conn = null;
      
      String className = getDriverString();
      try
      {
         // load the class (without requiring SQLServer jar at compile time)
         // System.out.print ("Loading " + className + "... ");
         Class<?> c = Class.forName (className);
         // System.out.println ("loaded.");
         
         // System.out.print ("Instantiating SQLServer driver... ");
         Driver driver = (Driver) c.newInstance();
         // System.out.println (driver);
         
         // System.out.println ("Registering SQLServer driver...");
         DriverManager.registerDriver (driver);
         
         String url = getUrlString() + host + ":" + port;
         if (db != null)
            url = url + ";DatabaseName=" + db;
         
         // System.out.println ("Establishing Connection: " + url + " ...");
         conn = DriverManager.getConnection (url, user, pass);
      }
      catch (ClassNotFoundException x)
      {
         System.err.println ("SQLServer Driver not found: " + className);
         System.err.println (x.getMessage());
      }
      catch (Exception x)
      {
         System.err.println (x.getMessage());
         x.printStackTrace (System.err);
      }
      return conn;
   }
   
   public static void main (final String[] args)
   {
      // create a map for the non -sys arguments, and set any defaults
      final Map<String, String> argMap = SysProperties.parseArguments (args, null);
      
      String host = argMap.get ("-host");
      String port = argMap.get ("-port"); // 1433
      String user = argMap.get ("-user");
      String pass = argMap.get ("-password");
      String db   = argMap.get ("-database");
         
      SQLServerDriver dd = new SQLServerDriver();
      Connection conn = dd.connect (host, port, db, user, pass);

      if (conn != null)
      {
         System.out.print ("Connected");
         // test create
         // java.sql.Statement stmt = conn.createStatement();
         // stmt.execute("create table ALL_TEST (testy int);");
         
         try { conn.close(); } catch (SQLException x) { }
      }
   }
}
