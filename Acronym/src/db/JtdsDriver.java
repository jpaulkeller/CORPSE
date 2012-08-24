package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import utils.SysProperties;

public class JtdsDriver extends SQLServerDriver // TBD
{
   private static final long serialVersionUID = 0;

   @Override
   public String getDriverString() 
   {
      return "net.sourceforge.jtds.jdbc.Driver";
   }

   /*
    * jdbc:jtds:<server_type>://<server>[:<port>][/<database>][;<property>=<value>[;...]]
    * 
    * <server_type> is one of either 'sqlserver' or 'sybase',
    * <port> is the port the database server is listening to 
    *        (default is 1433 for SQL Server and 7100 for Sybase)
    * <database> is the database name (JDBC term: catalog); if not specified,
    *            the user's default database is used). 
    *            
    * Properties are documented on the JTDS SourceForge FAQ page.
    */
   @Override
   public String getUrlString() 
   {
      return "jdbc:jtds:sqlserver://"; // <server>[:<port>][/<database>]
   }

   public Connection connect (final String host, final String port, final String db)
   {
      return connect (host, port, db, null, null);
   }
   
   public static void main (final String[] args)
   {
      // -host ob03 -port 1433 -database TimeExpTracker -user jkeller -password 8899Won
      // -host ob06w -port 1433 -database oberon -user oberonweb -password ohoberon12
      
      // If user and password are not provided, this driver will attempt to 
      // connect via Widows Authentication Single-Sign-On (SSO).  In order for
      // SSO to work, jTDS must be able to load the native SPPI library.  
      // Place this DLL (ntlmauth.dll) anywhere in the system path (defined by
      // the PATH system variable).
      
      // create a map for the non -sys arguments, and set any defaults
      final Map<String, String> argMap = SysProperties.parseArguments (args, null);
      
      String host = argMap.get ("-host");
      String port = argMap.get ("-port"); // 1433
      String user = argMap.get ("-user");
      String pass = argMap.get ("-password");
      String db   = argMap.get ("-database");
         
      JtdsDriver dd = new JtdsDriver();
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
