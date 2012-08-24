package db.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A factory for creating Statement objects.
 */
public final class StatementFactory
{
   /* Default debug level */
   /** The default debug. */
   private static DebugLevel defaultDebug = DebugLevel.OFF;

   /* Default sql formatter */
   /** The default formatter. */
   private static SqlFormatter defaultFormatter = new DefaultSqlFormatter();

   /**
    * StatementFactory returns either a regular PreparedStatement or a DebuggableStatement
    * class depending on the DebugLevel. If DebugLevel is OFF then a PreparedStatement is
    * returned. If DebugLevel is ON or VERBOSE then a DebuggableStatement is returned. This
    * minimizes overhead when debugging is not needed without effecting the code.
    */
   public StatementFactory()
   {
   }

   /**
    * Use this method if you want a class to override the global nature of a
    * property file approach. This gives a class an option of a formatter and
    * the debug value other than the global setting.
    *
    * @param con Connection to jdbc data source.
    * @param stmt sql statement that will be executed.
    * @param formatter SqlFormatter that matches the database type (i.e. OracleFormatter)
    * @param debug sets the debug level for this statement. DebugLevel can be OFF, ON, VERBOSE
    * @return PreparedStatement returns a DebuggableStatement if debug = ON or VERBOSE. 
    * Returns a standard PreparedStatement if debug = OFF.
    * @throws SQLException thrown if problem with connection.
    */
   public static PreparedStatement getStatement(final Connection con, final String stmt, 
                                                final SqlFormatter formatter, 
                                                final DebugLevel debug)
            throws SQLException
   {
      if (con == null)
         throw new SQLException("Connection passed to StatementFactory is null");
      if (debug != DebugLevel.OFF)
      {
         return new DebuggableStatement(con, stmt, formatter, debug);
      } else
      {
         return con.prepareStatement(stmt);
      }
   }

   /**
    * Use this if you want a class to override the global nature of a property
    * file approach. This gives a class an option of a formatter other than the global setting.
    *
    * @param con Connection to jdbc data source.
    * @param stmt sql statement that will be executed.
    * @param formatter SqlFormatter that matches the database type (i.e. OracleFormatter)
    * @return PreparedStatement returns a DebuggableStatement if debug = ON or VERBOSE. 
    * Returns a standard PreparedStatement if debug = OFF.
    * @throws SQLException thrown if problem with connection.
    */
   public static PreparedStatement getStatement(final Connection con, 
                                                final String stmt, 
                                                final SqlFormatter formatter)
            throws SQLException
   {

      return StatementFactory.getStatement(con, stmt, formatter, defaultDebug);

   }

   /**
    * Use this if you want a class to override the global nature of a property
    * file approach. This gives a class the option of turning debug code
    * on or off no matter what the global value. This will not effect the
    * global setting.
    *
    * @param con Connection to jdbc data source.
    * @param stmt sql statement that will be executed.
    * @param debug sets the debug level for this statement. DebugLevel can be OFF, ON, VERBOSE
    * @return PreparedStatement returns a DebuggableStatement if debug = ON or VERBOSE. 
    * Returns a standard PreparedStatement if debug = OFF.
    * @throws SQLException thrown if problem with connection.
    */
   public static PreparedStatement getStatement(final Connection con, 
                                                final String stmt, 
                                                final DebugLevel debug) throws SQLException
   {

      return StatementFactory.getStatement(con, stmt, defaultFormatter, debug);

   }

   /**
    * this is the typical way to retrieve a statement. This method uses the static
    * formatter and debug level.
    *
    * @param con Connection to jdbc data source.
    * @param stmt sql statement that will be executed.
    * @return PreparedStatement returns a DebuggableStatement if debug = ON or VERBOSE. 
    * Returns a standard PreparedStatement if debug = OFF.
    * @throws SQLException thrown if problem with connection.
    */
   public static PreparedStatement getStatement(final Connection con, 
                                                final String stmt) throws SQLException
   {

      return StatementFactory.getStatement(con, stmt, defaultFormatter, defaultDebug);
   }

   /**
    * typically set from property file so change is made in one place.
    * default is to false which immulates a preparedstatement.
    * This will change debug value in all places.
    * @param debug sets the debug level for this statement. DebugLevel can be OFF, ON, VERBOSE
    */
   public static void setDefaultDebug(final DebugLevel debug)
   {
      defaultDebug = debug;
   }

   /**
    * typically set from property file so change is made in one place.
    * This will change formatter in all places.
    * @param formatter sets the SqlFormatter to the database type used in this
    * application.
    */
   public static void setDefaultFormatter(final SqlFormatter formatter)
   {
      defaultFormatter = formatter;
   }
}
