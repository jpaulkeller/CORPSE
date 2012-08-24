package db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

public final class ConnectAPI
{
   private ConnectAPI()
   {
      // utility class; prevent instantiation
   }
   
   public static SQL connect (final String dbms)
   {
      // TBD: change to properties
      if (dbms.equalsIgnoreCase ("Navision"))
         return connect ("ob03", "1433", "navision");
      else if (dbms.equalsIgnoreCase ("Navision etass"))
         return connect ("ob03", "1433", "navision", "etass", "8foxes6cats");
      else if (dbms.equalsIgnoreCase ("Navision jkeller"))
         return connect ("ob03", "1433", "navision", "jkeller", "8899Won");
      else if (dbms.equalsIgnoreCase ("TimeExpTracker"))
         return connect ("ob03", "1433", "TimeExpTracker");
      else if (dbms.equalsIgnoreCase ("TimeExpTracker_SM"))
         return connect ("ob03", "1433", "TimeExpTracker_SM");
      else if (dbms.equalsIgnoreCase ("TimeExpTracker etass"))
         return connect ("ob03", "1433", "TimeExpTracker", "etass", "8foxes6cats");
      else if (dbms.equalsIgnoreCase ("Web"))
         return connect ("ob06w", "1433", "oberon");
      else if (dbms.equalsIgnoreCase ("Web!"))
         return connect ("ob06w", "1433", "oberon", "oberonweb", "ohoberon12");
      else if (dbms.equalsIgnoreCase ("BAT"))
         return connect ("localhost", "1433", "BiometricsDatabaseSQL");
      else
         System.err.println (ConnectAPI.class.getName() + " unsupported DBMS: " + dbms);
      return null;
   }
   
   private static SQL connect (final String host,
                               final String port,
                               final String database)
   {
      return connect (host, port, database, null, null);
   }
   
   private static SQL connect (final String host, final String port,
                               final String database, 
                               final String user, final String password)
   {
      SQL sql = null;
      // SQLServerDriver dd = new SQLServerDriver();
      JtdsDriver dd = new JtdsDriver();
      Connection conn = dd.connect (host, port, database, user, password);
      if (conn != null)
         sql = new SQL (database, conn);
      return sql;
   }
   
   public static SQL connect (final DatabaseDriver dd,
                              final String host, final String port,
                              final String database, 
                              final String user, final String password)
   {
      SQL sql = null;
   
      Connection conn = null;
      String driverClassName = dd.getDriverString();
      try
      {
         // load the class (without requiring the driver jar at compile time)
         System.out.print ("Loading " + driverClassName + "... ");
         Class<?> c = Class.forName (driverClassName);
         System.out.println ("loaded.");
         
         System.out.print ("Instantiating " + c.getSimpleName() + "... ");
         Driver driver = (Driver) c.newInstance();
         System.out.println (driver);
         
         System.out.println ("Registering driver...");
         DriverManager.registerDriver (driver);

         // TBD: make this generic; this only works for SQL Server
         String url = dd.getUrlString() + host + ":" + port;
         if (database != null)
            url = url + ";DatabaseName=" + database;
         
         System.out.println ("Establishing Connection: " + url + " ...");
         conn = DriverManager.getConnection (url, user, password);
      }
      catch (ClassNotFoundException x)
      {
         System.err.println ("Driver not found: " + driverClassName);
         System.err.println (x.getMessage());
      }
      catch (Exception x)
      {
         System.err.println (x.getMessage());
         x.printStackTrace (System.err);
      }
      
      if (conn != null)
         sql = new SQL (database, conn);
      return sql;
   }
   
   public static void main (final String[] args)
   {
      String[] dbs = new String[] { "Navision", "TimeExpTracker", "Web", "BAT" };
      
      for (String db : dbs)
      {
         SQL sql = connect (db);
         System.out.println (db + ": " + (sql != null));
         if (sql != null)
            sql.close();
      }
   }
}
