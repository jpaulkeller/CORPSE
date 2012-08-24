package db.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The Class tester.
 */
public class Tester
{
   /**
    * The main method.
    *
    * @param args the arguments
    * @throws IllegalAccessException 
    * @throws InstantiationException 
    */
   public static void main(final String[] args) throws InstantiationException, IllegalAccessException
   {
      SqlFormatter formatter = new OracleSqlFormatter();
      Object o = new java.sql.Date(new java.util.Date().getTime());
      try
      {
         System.out.println("date=" + formatter.format(o));
         o = new Long(198);
         System.out.println("Long=" + formatter.format(o));
         o = new Boolean(true);
         System.out.println("Boolean=" + formatter.format(o));
      } catch (SQLException e)
      {
         e.printStackTrace();
      }

      // this goes in static debug class once, not individual classes
      StatementFactory.setDefaultDebug(DebugLevel.VERBOSE);
      StatementFactory.setDefaultFormatter(formatter);

      // test debuggable
      java.sql.Connection con = null;
      java.sql.PreparedStatement ps = null;
      java.sql.ResultSet rs = null;

      String databaseURL = "jdbc:oracle:thin:@grid.ngis-cville.com:1521:dev"; 
      System.out.println(databaseURL);
      String user = "jacob_data";
      String password = "jacob";

      try
      {
         Class<Driver> c = (Class<Driver>) Class.forName("oracle.jdbc.driver.OracleDriver");
         Driver driver = c.newInstance(); // instantiate Oracle driver
         DriverManager.registerDriver(driver); // register Oracle driver
         con = DriverManager.getConnection(databaseURL, user, password);
         System.out.println("Connection established.");

         String sql = "select * from APPLICATION_LOGS where LOG_TYPE = ?";
         ps = StatementFactory.getStatement(con, sql);
         ps.setString(1, "WARNING");
         System.out.println(ps.toString());
         rs = ps.executeQuery();
         while (rs.next())
            System.out.println(" > DATE = " + rs.getDate("LOG_DATE"));

      } catch (java.lang.ClassNotFoundException e)
      {
         e.printStackTrace();
      } catch (java.sql.SQLException se)
      {
         se.printStackTrace();
      } finally
      {
         try
         {
            if (rs != null)
               rs.close();
            if (ps != null)
               ps.close();
            if (con != null && !con.isClosed())
               con.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
      }
   }
}