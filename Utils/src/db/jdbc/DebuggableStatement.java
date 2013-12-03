package db.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.StringTokenizer;

/**
 * PreparedStatements have no way to retrieve the statement that was
 * executed on the database. This is due to the nature of prepared statements, which
 * are database driver specific. This class proxies for a PreparedStatement and
 * creates the SQL string that is created from the sets done on the
 * PreparedStatement.
 * <p>
   Some of the objects such as blob, clob, and Ref are only represented as
   Strings and are not the actual objects populating the database.
   Array is represented by the object type within the array.

   Example code:
        int payPeriod = 1;
        String name = "Troy Thompson";
        ArrayList employeePay = new ArrayList();
        ResultSet rs = null;
        PreparedStatement ps = null;
        Connection con = null;
        try{
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            String url = "jdbc:odbc:Employee";
            con = DriverManager.getConnection(url);
            String sql = "SELECT e.name,e.employee_number,e.pay_rate,e.type,"+
                        " e.hire_date,h.pay_period,h.hours,h.commissions"+
                        " FROM Employee_tbl e,hours_tbl h "+
                        " WHERE h.pay_period = ?"+
                        " AND e.name = ?"+
                        " AND h.employee_number = e.employee_number";
            ps = StatementFactory.getStatement(con,sql); // <-- insert this to debug
            //ps = con.prepareStatement(sql);
            ps.setInt(1,payPeriod);
            ps.setString(2,name);
            System.out.println();
            System.out.println(" debuggable statement= " + ps.toString());
            rs = ps.executeQuery();
        }catch(SQLException e){
            e.printStackTrace();
        }catch(ClassNotFoundException ce){
            ce.printStackTrace();
        }
        finally{
            try{
                if(rs != null){rs.close();}
                if(ps != null){ps.close();}
                if(!con.isClosed()) con.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
   </p>
 * *****notes*****
 *  One of the main differences between databases is how they handle dates/times.
 *  Since we use Oracle, the debug string for Dates, Times, Timestamps are using
 *  an Oracle specific SqlFormatter called OracleSqlFormatter.
 *
 *  The following is in our debug class:
 *  static{
 *      StatementFactory.setDefaultDebug(DebugLevel.ON);
 *      StatementFactory.setDefaultFormatter(new OracleSqlFormatter());
 *  }
 *
 */
public class DebuggableStatement implements PreparedStatement
{
   
   /** The ps. */
   private PreparedStatement ps; // preparedStatement being proxied for.
   
   /** The sql. */
   private String sql; // original statement going to database.
   
   /** The filtered sql. */
   private String filteredSql; // statement filtered for rogue '?' that are not
                               // bind variables.
   /** The variables. */
   private DebugObject[] variables; // array of bind variables
   
   /** The formatter. */
   private SqlFormatter formatter; // format for dates
   
   /** The start time. */
   private long startTime; // time that statement began execution
   
   /** The execute time. */
   private long executeTime; // time elapsed while executing statement
   
   /** The debug level. */
   private DebugLevel debugLevel; // level of debug

   /**
    * Construct new DebugableStatement.
    * Uses the SqlFormatter to format date, time, timestamp outputs
    *
    * @param con Connection to be used to construct PreparedStatement
    * @param sqlStatement sql statement to be sent to database.
    * @param formatter the formatter
    * @param debugLevel DebugLevel can be ON, OFF, VERBOSE.
    * @throws SQLException the sQL exception
    */
   protected DebuggableStatement(final Connection con, final String sqlStatement, 
                                 final SqlFormatter formatter, final DebugLevel debugLevel)
   throws SQLException
   {
      // set values for member variables
      if (con == null)
         throw new SQLException("Connection object is null");
      this.ps = con.prepareStatement(sqlStatement);
      this.sql = sqlStatement;
      this.debugLevel = debugLevel;
      this.formatter = formatter;

      // see if there are any '?' in the statement that are not bind variables
      // and filter them out.
      boolean isString = false;
      char[] sqlString = sqlStatement.toCharArray();
      for (int i = 0; i < sqlString.length; i++)
      {
         if (sqlString[i] == '\'')
            isString = !isString;
         // substitute the ? with an unprintable character if the ? is in a
         // string.
         if (sqlString[i] == '?' && isString)
            sqlString[i] = '\u0007';
      }
      filteredSql = new String(sqlString);

      // find out how many variables are present in statement.
      int count = 0;
      int index = -1;
      while ((index = filteredSql.indexOf("?", index + 1)) != -1)
      {
         count++;
      }

      // show how many bind variables found
      if (debugLevel == DebugLevel.VERBOSE)
         System.out.println("count= " + count);

      // create array for bind variables
      variables = new DebugObject[count];
   }

   /**
    * Facade for PreparedStatement.
    *
    * @throws SQLException the sQL exception
    */
   @Override
   public void addBatch() throws SQLException
   {
      ps.addBatch();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @param ignored the sql
    * @throws SQLException the sQL exception
    */
   @Override
   public void addBatch(final String ignored) throws SQLException
   {
      throw new UnsupportedOperationException("Use addBatch() instead: " + ignored);
   }

   /**
    * Facade for PreparedStatement.
    *
    * @throws SQLException the sQL exception
    */
   @Override
   public void cancel() throws SQLException
   {
      ps.cancel();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @throws SQLException the sQL exception
    */
   @Override
   public void clearBatch() throws SQLException
   {
      ps.clearBatch();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @throws SQLException the sQL exception
    */
   @Override
   public void clearParameters() throws SQLException
   {
      ps.clearParameters();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @throws SQLException the sQL exception
    */
   @Override
   public void clearWarnings() throws SQLException
   {
      ps.clearWarnings();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @throws SQLException the sQL exception
    */
   @Override
   public void close() throws SQLException
   {
      ps.close();
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#isClosed()
    */
   @Override
   public boolean isClosed() throws SQLException
   {
      return ps.isClosed();
   }

   /**
    * Executes query and Calculates query execution time if DebugLevel = VERBOSE.
    *
    * @return results of query
    * @throws SQLException the sQL exception
    */
   @Override
   public boolean execute() throws SQLException
   {
      // execute query
      Boolean results = null;
      try
      {
         results = (Boolean) executeVerboseQuery("execute", null);
      } catch (Exception e)
      {
         throw new SQLException(e.getMessage() + ": " + sql, e);
      }
      return results.booleanValue();
   }

   /**
    * This method is only here for convenience. If a different sql string is executed
    * than was passed into Debuggable, unknown results will occur.
    * Executes query and Calculates query execution time if DebugLevel = VERBOSE
    *
    * @param ignored should be same string that was passed into Debuggable
    * @return results of query
    * @throws SQLException the sQL exception
    */
   @Override
   public boolean execute(final String ignored) throws SQLException
   {
      throw new UnsupportedOperationException("Use execute() instead: " + ignored);
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#execute(java.lang.String, int)
    */
   @Override
   public boolean execute(final String ignored, final int autoGeneratedKeys) throws SQLException
   {
      return execute(ignored);
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#execute(java.lang.String, int[])
    */
   @Override
   public boolean execute(final String ignored, final int[] columnIndexes) throws SQLException
   {
      return execute(ignored);
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
    */
   @Override
   public boolean execute(final String ignored, final String[] columnNames) throws SQLException
   {
      return execute(ignored);
   }

   /**
    * Executes query and Calculates query execution time if DebugLevel = VERBOSE.
    *
    * @return results of query
    * @throws SQLException the sQL exception
    */
   @Override
   public int[] executeBatch() throws SQLException
   {
      // execute query
      int[] results = null;
      try
      {
         results = (int[]) executeVerboseQuery("executeBatch", null);
      } catch (Exception e)
      {
         throw new SQLException(e.getMessage() + ": " + sql, e);
      }
      return results;
   }

   /**
    * Executes query and Calculates query execution time if DebugLevel = VERBOSE.
    *
    * @return results of query
    * @throws SQLException the sQL exception
    */
   @Override
   public ResultSet executeQuery() throws SQLException
   {
      // execute query
      ResultSet results = null;
      try
      {
         results = (ResultSet) executeVerboseQuery("executeQuery", null);
      } catch (Exception e)
      {
         throw new SQLException(e.getMessage() + ": " + sql, e);
      }
      return results;
   }

   /**
    * This method is only here for convenience. If a different sql string is executed
    * than was passed into Debuggable, unknown results will occur.
    * Executes query and Calculates query execution time if DebugLevel = VERBOSE
    *
    * @param ignored should be same string that was passed into Debuggable
    * @return results of query
    * @throws SQLException the sQL exception
    */
   @Override
   public ResultSet executeQuery(final String ignored) throws SQLException
   {
      // execute query
      ResultSet results = null;
      try
      {
         results = (ResultSet) executeVerboseQuery("executeQuery", 
                                                   new Class[] {ignored.getClass()});
      } catch (Exception e)
      {
         throw new SQLException(e.getMessage() + ": " + ignored, e);
      }
      return results;
   }

   /**
    * Executes query and Calculates query execution time if DebugLevel = VERBOSE.
    *
    * @return results of query
    * @throws SQLException the sQL exception
    */
   @Override
   public int executeUpdate() throws SQLException
   {
      // execute query
      Integer results = null;
      try
      {
         results = (Integer) executeVerboseQuery("executeUpdate", null);
      } catch (Exception e)
      {
         throw new SQLException(e.getMessage() + ": " + sql, e);
      }
      return results.intValue();
   }

   /**
    * This method is only here for convenience. If a different sql string is executed
    * than was passed into Debuggable, unknown results will occur.
    * Executes query and Calculates query execution time if DebugLevel = VERBOSE
    *
    * @param ignored should be same string that was passed into Debuggable
    * @return results of query
    * @throws SQLException the sQL exception
    */
   @Override
   public int executeUpdate(final String ignored) throws SQLException
   {
      throw new UnsupportedOperationException("Use executeUpdate() instead: " + ignored);
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#executeUpdate(java.lang.String, int)
    */
   @Override
   public int executeUpdate(final String ignored, final int autoGeneratedKeys) throws SQLException
   {
      return executeUpdate(ignored);
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
    */
   @Override
   public int executeUpdate(final String ignored, final int[] columnIndexes) throws SQLException
   {
      return executeUpdate(ignored);
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
    */
   @Override
   public int executeUpdate(final String ignored, final String[] columnNames) throws SQLException
   {
      return executeUpdate(ignored);
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the connection
    * @throws SQLException the sQL exception
    */
   @Override
   public Connection getConnection() throws SQLException
   {
      return ps.getConnection();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the fetch direction
    * @throws SQLException the sQL exception
    */
   @Override
   public int getFetchDirection() throws SQLException
   {
      return ps.getFetchDirection();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the fetch size
    * @throws SQLException the sQL exception
    */
   @Override
   public int getFetchSize() throws SQLException
   {
      return ps.getFetchSize();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the max field size
    * @throws SQLException the sQL exception
    */
   @Override
   public int getMaxFieldSize() throws SQLException
   {
      return ps.getMaxFieldSize();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the max rows
    * @throws SQLException the sQL exception
    */
   @Override
   public int getMaxRows() throws SQLException
   {
      return ps.getMaxRows();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the meta data
    * @throws SQLException the sQL exception
    */
   @Override
   public ResultSetMetaData getMetaData() throws SQLException
   {
      return ps.getMetaData();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the more results
    * @throws SQLException the sQL exception
    */
   @Override
   public boolean getMoreResults() throws SQLException
   {
      return ps.getMoreResults();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the query timeout
    * @throws SQLException the sQL exception
    */
   @Override
   public int getQueryTimeout() throws SQLException
   {
      return ps.getQueryTimeout();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the result set
    * @throws SQLException the sQL exception
    */
   @Override
   public ResultSet getResultSet() throws SQLException
   {
      return ps.getResultSet();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the result set concurrency
    * @throws SQLException the sQL exception
    */
   @Override
   public int getResultSetConcurrency() throws SQLException
   {
      return ps.getResultSetConcurrency();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the result set type
    * @throws SQLException the sQL exception
    */
   @Override
   public int getResultSetType() throws SQLException
   {
      return ps.getResultSetType();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the statement
    */
   public String getStatement()
   {
      return sql;
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the update count
    * @throws SQLException the sQL exception
    */
   @Override
   public int getUpdateCount() throws SQLException
   {
      return ps.getUpdateCount();
   }

   /**
    * Facade for PreparedStatement.
    *
    * @return the warnings
    * @throws SQLException the sQL exception
    */
   @Override
   public SQLWarning getWarnings() throws SQLException
   {
      return ps.getWarnings();
   }

   /**
    * Tests Object o for parameterIndex (which parameter is being set) and places
    * object in array of variables.
    *
    * @param parameterIndex which PreparedStatement parameter is being set.
    * Sequence begins at 1.
    * @param o Object being stored as parameter
    * @throws ParameterIndexOutOfBoundsException the parameter index out of bounds exception
    */
   private void saveObject(final int parameterIndex, final Object o) 
   throws ParameterIndexOutOfBoundsException
   {
      if (parameterIndex > variables.length)
         throw new ParameterIndexOutOfBoundsException
         ("Parameter index " + parameterIndex +
          " exceeds actual parameter count of " + variables.length + ": " + sql);

      variables[parameterIndex - 1] = new DebugObject(o);
   }

   /**
    * Adds name of the Array's internal class type(by using x.getBaseTypeName())
    * to the debug String. If x is null, NULL is added to debug String.
    *
    * @param i index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setArray(final int i, final java.sql.Array x) throws SQLException
   {
      saveObject(i, x);
      ps.setArray(i, x);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
    */
   @Override
   public void setAsciiStream(final int parameterIndex, final InputStream x) throws SQLException
   {
      saveObject(parameterIndex, (x == null ? "NULL" : "<stream>"));
      ps.setAsciiStream(parameterIndex, x);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
    */
   @Override
   public void setAsciiStream(final int parameterIndex, final InputStream x, 
                              final int length) throws SQLException
   {
      saveObject(parameterIndex, (x == null ? "NULL" : "<stream length= " + length + ">"));
      ps.setAsciiStream(parameterIndex, x, length);
   }

   /**
    * Debug string prints NULL if InputStream is null, or adds "stream length = " + length.
    *
    * @param parameterIndex the parameter index
    * @param x the x
    * @param length the length
    * @throws SQLException the sQL exception
    */
   @Override
   public void setAsciiStream(final int parameterIndex, final InputStream x, 
                              final long length) throws SQLException
   {
      saveObject(parameterIndex, (x == null ? "NULL" : "<stream length= " + length + ">"));
      ps.setAsciiStream(parameterIndex, x, length);
   }

   /**
    * Adds BigDecimal to debug string in parameterIndex position.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException
   {
      saveObject(parameterIndex, x);
      ps.setBigDecimal(parameterIndex, x);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
    */
   @Override
   public void setBinaryStream(final int parameterIndex, final InputStream x) throws SQLException
   {
      saveObject(parameterIndex, (x == null ? "NULL" : "<stream>"));
      ps.setBinaryStream(parameterIndex, x);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
    */
   @Override
   public void setBinaryStream(final int parameterIndex, final InputStream x, 
                               final int length) throws SQLException
   {
      saveObject(parameterIndex, (x == null ? "NULL" : "<stream length= " + length + ">"));
      ps.setBinaryStream(parameterIndex, x, length);

   }

   /**
    * Debug string prints NULL if InputStream is null, or adds "stream length= " + length.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @param length length of InputStream
    * @throws SQLException the sQL exception
    */
   @Override
   public void setBinaryStream(final int parameterIndex, final InputStream x, 
                               final long length) throws SQLException
   {
      saveObject(parameterIndex, (x == null ? "NULL" : "<stream length= " + length + ">"));
      ps.setBinaryStream(parameterIndex, x, length);
   }

   /**
    * Adds name of the object's class type(Blob) to the debug String. If
    * object is null, NULL is added to debug String.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setBlob(final int parameterIndex, final Blob x) throws SQLException
   {
      saveObject(parameterIndex, x);
      ps.setBlob(parameterIndex, x);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
    */
   @Override
   public void setBlob(final int parameterIndex, final InputStream x) throws SQLException
   {
      saveObject(parameterIndex, x == null ? "NULL" : "<stream>");
      ps.setBlob(parameterIndex, x);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
    */
   @Override
   public void setBlob(final int parameterIndex, final InputStream x,
                       final long length) throws SQLException
   {
      saveObject(parameterIndex, x == null ? "NULL" : "<stream length= " + length + ">");
      ps.setBlob(parameterIndex, x, length);
   }

   /**
    * Adds boolean to debug string in parameterIndex position.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setBoolean(final int parameterIndex, final boolean x) throws SQLException
   {
      saveObject(parameterIndex, new Boolean(x));
      ps.setBoolean(parameterIndex, x);
   }

   /**
    * Adds byte to debug string in parameterIndex position.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setByte(final int parameterIndex, final byte x) throws SQLException
   {
      saveObject(parameterIndex, new Byte(x));
      ps.setByte(parameterIndex, x);
   }

   /**
    * Adds byte[] to debug string in parameterIndex position.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setBytes(final int parameterIndex, final byte[] x) throws SQLException
   {
      saveObject(parameterIndex, (x == null ? "NULL" : "byte[] length=" + x.length));
      ps.setBytes(parameterIndex, x);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
    */
   @Override
   public void setCharacterStream(final int parameterIndex, final Reader reader) 
   throws SQLException
   {
      saveObject(parameterIndex, (reader == null ? "NULL" : "<reader>"));
      ps.setCharacterStream(parameterIndex, reader);
   }

   /**
    * Debug string prints NULL if reader is null, or adds "stream length= " + length.
    *
    * @param parameterIndex index of parameter
    * @param reader the reader
    * @param length length of InputStream
    * @throws SQLException the sQL exception
    */
   @Override
   public void setCharacterStream(final int parameterIndex, final Reader reader, 
                                  final int length) throws SQLException
   {
      saveObject(parameterIndex, (reader == null ? "NULL" : "<stream length= " + length + ">"));
      ps.setCharacterStream(parameterIndex, reader, length);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, long)
    */
   @Override
   public void setCharacterStream(final int parameterIndex, final Reader reader, 
                                  final long length) throws SQLException
   {
      saveObject(parameterIndex, (reader == null ? "NULL" : "<stream length= " + length + ">"));
      ps.setCharacterStream(parameterIndex, reader, length);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
    */
   @Override
   public void setNCharacterStream(final int parameterIndex, final Reader reader) 
   throws SQLException
   {
      saveObject(parameterIndex, (reader == null ? "NULL" : "<reader>"));
      ps.setNCharacterStream(parameterIndex, reader);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader, long)
    */
   @Override
   public void setNCharacterStream(final int parameterIndex, final Reader reader, 
                                   final long length) throws SQLException
   {
      saveObject(parameterIndex, (reader == null ? "NULL" : "<reader length= " + length + ">"));
      ps.setNCharacterStream(parameterIndex, reader, length);
   }

   /**
    * Adds name of the object's class type(Clob) to the debug String. If
    * object is null, NULL is added to debug String.
    *
    * @param i the i
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setClob(final int i, final Clob x) throws SQLException
   {
      saveObject(i, x);
      ps.setClob(i, x);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
    */
   @Override
   public void setClob(final int parameterIndex, final Reader reader) throws SQLException
   {
      saveObject(parameterIndex, (reader == null ? "NULL" : "<reader>"));
      ps.setClob(parameterIndex, reader);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
    */
   @Override
   public void setClob(final int parameterIndex, final Reader reader, 
                       final long length) throws SQLException
   {
      saveObject(parameterIndex, (reader == null ? "NULL" : "<reader length= " + length + ">"));
      ps.setClob(parameterIndex, reader, length);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
    */
   @Override
   public void setNClob(final int parameterIndex, final NClob value) throws SQLException
   {
      saveObject(parameterIndex, value);
      ps.setNClob(parameterIndex, value);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
    */
   @Override
   public void setNClob(final int parameterIndex, final Reader reader) throws SQLException
   {
      saveObject(parameterIndex, (reader == null ? "NULL" : "<reader>"));
      ps.setNClob(parameterIndex, reader);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
    */
   @Override
   public void setNClob(final int parameterIndex, final Reader reader, 
                        final long length) throws SQLException
   {
      saveObject(parameterIndex, (reader == null ? "NULL" : "<reader length= " + length + ">"));
      ps.setNClob(parameterIndex, reader, length);
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#setCursorName(java.lang.String)
    */
   @Override
   public void setCursorName(final String name) throws SQLException
   {
      ps.setCursorName(name);
   }

   /**
    * Debug string displays date in YYYY-MM-DD HH24:MI:SS.# format.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setDate(final int parameterIndex, final java.sql.Date x) throws SQLException
   {
      saveObject(parameterIndex, x);
      ps.setDate(parameterIndex, x);
   }

   /**
    * this implementation assumes that the Date has the date, and the
    * calendar has the local info. For the debug string, the cal date
    * is set to the date of x. Debug string displays date in YYYY-MM-DD HH24:MI:SS.# format.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @param cal uses x to set time
    * @throws SQLException the sQL exception
    */
   @Override
   public void setDate(final int parameterIndex, final java.sql.Date x, 
                       final Calendar cal) throws SQLException
   {
      cal.setTime(new java.util.Date(x.getTime()));
      saveObject(parameterIndex, cal);
      ps.setDate(parameterIndex, x, cal);
   }

   /**
    * Adds double to debug string in parameterIndex position.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setDouble(final int parameterIndex, final double x) throws SQLException
   {
      saveObject(parameterIndex, new Double(x));
      ps.setDouble(parameterIndex, x);
   }

   /**
    * Facade for PreparedStatement.
    *
    * @param enable the new escape processing
    * @throws SQLException the sQL exception
    */
   @Override
   public void setEscapeProcessing(final boolean enable) throws SQLException
   {
      ps.setEscapeProcessing(enable);
   }

   /**
    * Facade for PreparedStatement.
    *
    * @param formatter the new formatter
    */
   public void setFormatter(final SqlFormatter formatter)
   {
      this.formatter = formatter;
   }

   /**
    * Facade for PreparedStatement.
    *
    * @param direction the new fetch direction
    * @throws SQLException the sQL exception
    */
   @Override
   public void setFetchDirection(final int direction) throws SQLException
   {
      ps.setFetchDirection(direction);
   }

   /**
    * Facade for PreparedStatement.
    *
    * @param rows the new fetch size
    * @throws SQLException the sQL exception
    */
   @Override
   public void setFetchSize(final int rows) throws SQLException
   {
      ps.setFetchSize(rows);
   }

   /**
    * Adds float to debug string in parameterIndex position.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setFloat(final int parameterIndex, final float x) throws SQLException
   {
      saveObject(parameterIndex, new Float(x));
      ps.setFloat(parameterIndex, x);
   }

   /**
    * Adds int to debug string in parameterIndex position.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setInt(final int parameterIndex, final int x) throws SQLException
   {
      saveObject(parameterIndex, new Integer(x));
      ps.setInt(parameterIndex, x);
   }

   /**
    * Adds long to debug string in parameterIndex position.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setLong(final int parameterIndex, final long x) throws SQLException
   {
      saveObject(parameterIndex, new Long(x));
      ps.setLong(parameterIndex, x);
   }

   /**
    * Facade for PreparedStatement.
    *
    * @param max the new max field size
    * @throws SQLException the sQL exception
    */
   @Override
   public void setMaxFieldSize(final int max) throws SQLException
   {
      ps.setMaxFieldSize(max);
   }

   /**
    * Facade for PreparedStatement.
    *
    * @param max the new max rows
    * @throws SQLException the sQL exception
    */
   @Override
   public void setMaxRows(final int max) throws SQLException
   {
      ps.setMaxRows(max);
   }

   /**
    * Adds a NULL to the debug String.
    *
    * @param parameterIndex index of parameter
    * @param sqlType the sql type
    * @throws SQLException the sQL exception
    */
   @Override
   public void setNull(final int parameterIndex, final int sqlType) throws SQLException
   {
      saveObject(parameterIndex, "NULL");
      ps.setNull(parameterIndex, sqlType);
   }

   /**
    * Adds a NULL to the debug String.
    *
    * @param parameterIndex index of parameter
    * @param sqlType the sql type
    * @param typeName type of Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setNull(final int parameterIndex, final int sqlType, 
                       final String typeName) throws SQLException
   {
      saveObject(parameterIndex, "NULL");
      ps.setNull(parameterIndex, sqlType, typeName);
   }

   /**
    * Adds name of the object's class type to the debug String. If
    * object is null, NULL is added to debug String.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setObject(final int parameterIndex, final Object x) throws SQLException
   {
      saveObject(parameterIndex, (x == null ? "NULL" : x.getClass().getName()));
      ps.setObject(parameterIndex, x);
   }

   /**
    * Adds name of the object's class type to the debug String. If
    * object is null, NULL is added to debug String.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @param targetSqlType database type
    * @throws SQLException the sQL exception
    */
   @Override
   public void setObject(final int parameterIndex, final Object x, 
                         final int targetSqlType) throws SQLException
   {
      saveObject(parameterIndex, (x == null ? "NULL" : x.getClass().getName()));
      ps.setObject(parameterIndex, x, targetSqlType);
   }

   /**
    * Adds name of the object's class type to the debug String. If
    * object is null, NULL is added to debug String.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @param targetSqlType database type
    * @param scale see PreparedStatement
    * @throws SQLException the sQL exception
    */
   @Override
   public void setObject(final int parameterIndex, final Object x, 
                         final int targetSqlType, final int scale) throws SQLException
   {
      saveObject(parameterIndex, (x == null ? "NULL" : x.getClass().getName()));
      ps.setObject(parameterIndex, x, targetSqlType, scale);
   }

   /**
    * Facade for PreparedStatement.
    *
    * @param seconds the new query timeout
    * @throws SQLException the sQL exception
    */
   @Override
   public void setQueryTimeout(final int seconds) throws SQLException
   {
      ps.setQueryTimeout(seconds);
   }

   /**
    * From the javadocs:
    * A reference to an SQL structured type value in the database.
    * A Ref can be saved to persistent storage.
    * The output from this method call in DebuggableStatement is a string representation
    * of the Ref object by calling the Ref object's getBaseTypeName() method.
    * Again, this will only be a String representation of the actual object
    * being stored in the database.
    *
    * @param i index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */

   @Override
   public void setRef(final int i, final Ref x) throws SQLException
   {
      saveObject(i, x);
      ps.setRef(i, x);
   }

   /**
    * Adds short to debug string in parameterIndex position.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setShort(final int parameterIndex, final short x) throws SQLException
   {
      saveObject(parameterIndex, new Short(x));
      ps.setShort(parameterIndex, x);
   }

   /**
    * Adds String to debug string in parameterIndex position.
    * If String is null "NULL" is inserted in debug string.
    * ***note****
    * In situations where a single ' is in the string being
    * inserted in the database. The debug string will need to be modified to
    * reflect this when running the debug statement in the database.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setString(final int parameterIndex, final String x) throws SQLException
   {
      saveObject(parameterIndex, x);
      ps.setString(parameterIndex, x);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
    */
   @Override
   public void setNString(final int parameterIndex, final String value) throws SQLException
   {
      saveObject(parameterIndex, value);
      ps.setNString(parameterIndex, value);
   }

   /**
    * Debug string displays Time in HH24:MI:SS.# format.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setTime(final int parameterIndex, final Time x) throws SQLException
   {
      saveObject(parameterIndex, x);
      ps.setTime(parameterIndex, x);
   }

   /**
    * This implementation assumes that the Time object has the time and
    * Calendar has the locale info. For the debug string, the cal time
    * is set to the value of x. Debug string displays time in HH24:MI:SS.# format.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @param cal sets time based on x
    * @throws SQLException the sQL exception
    */
   @Override
   public void setTime(final int parameterIndex, final Time x, 
                       final Calendar cal) throws SQLException
   {
      cal.setTime(new java.util.Date(x.getTime()));
      saveObject(parameterIndex, cal);
      ps.setTime(parameterIndex, x, cal);
   }

   /**
    * Debug string displays timestamp in YYYY-MM-DD HH24:MI:SS.# format.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @throws SQLException the sQL exception
    */
   @Override
   public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException
   {
      saveObject(parameterIndex, x);
      ps.setTimestamp(parameterIndex, x);
   }

   /**
    * This implementation assumes that the Timestamp has the date/time and
    * Calendar has the locale info. For the debug string, the cal date/time
    * is set to the default value of Timestamp which is YYYY-MM-DD HH24:MI:SS.#.
    * Debug string displays timestamp in DateFormat.LONG format.
    *
    * @param parameterIndex index of parameter
    * @param x parameter Object
    * @param cal sets time based on x
    * @throws SQLException the sQL exception
    */
   @Override
   public void setTimestamp(final int parameterIndex, final Timestamp x, 
                            final Calendar cal) throws SQLException
   {
      cal.setTime(new java.util.Date(x.getTime()));
      saveObject(parameterIndex, cal);
      ps.setTimestamp(parameterIndex, x, cal);
   }

   /**
    * Method has been deprecated in PreparedStatement interface.
    * This method is present only to satisfy interface and does
    * not do anything.
    * Do not use...
    *
    * @param parameterIndex the parameter index
    * @param x the x
    * @param length the length
    * @throws SQLException the sQL exception
    * @deprecated
    */
   @Deprecated
   @Override
   public void setUnicodeStream(final int parameterIndex, final InputStream x, 
                                final int length) throws SQLException
   {
      throw new UnsupportedOperationException("Unsupported: " + sql);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
    */
   @Override
   public void setRowId(final int parameterIndex, final RowId x) throws SQLException
   {
      saveObject(parameterIndex, x);
      ps.setRowId(parameterIndex, x);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
    */
   @Override
   public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException
   {
      saveObject(parameterIndex, xmlObject);
      ps.setSQLXML(parameterIndex, xmlObject);
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
    */
   @Override
   public void setURL(final int parameterIndex, final URL x) throws SQLException
   {
      saveObject(parameterIndex, x);
      ps.setURL(parameterIndex, x);
   }

   /**
       this toString is overidden to return a String representation of
       the sql statement being sent to the database. If a bind variable
       is missing then the String contains a ? + (missing variable #)
       @return the above string representation
   */
   @Override
   public String toString()
   {
      StringTokenizer st = new StringTokenizer(filteredSql, "?");
      int count = 1;
      StringBuffer statement = new StringBuffer();
      while (st.hasMoreTokens())
      {
         statement.append(st.nextToken());
         if (count <= variables.length)
         {
            if (variables[count - 1] != null && variables[count - 1].isValueAssigned())
            {
               try
               {
                  statement.append(formatter.format(variables[count - 1].getDebugObject()));
               } catch (SQLException e)
               {
                  statement.append("SQLException");
               }
            } else
            {
               statement.append("? " + "(missing variable # " + count + " ) ");
            }
         }
         count++;
      }
      // unfilter the string in case there were rogue '?' in query string.
      char[] unfilterSql = statement.toString().toCharArray();
      for (int i = 0; i < unfilterSql.length; i++)
         if (unfilterSql[i] == '\u0007')
            unfilterSql[i] = '?';

      // return execute time
      if (debugLevel == DebugLevel.ON)
         return new String(unfilterSql);
      else
         return new String(unfilterSql) + "\nquery executed in " + executeTime + " milliseconds\n";
   }

   /**
    * Execute verbose query.
    *
    * @param methodName the method name
    * @param parameters the parameters
    * @return the object
    * @throws SQLException the sQL exception
    * @throws NoSuchMethodException the no such method exception
    * @throws InvocationTargetException the invocation target exception
    * @throws IllegalAccessException the illegal access exception
    */
   private Object executeVerboseQuery(final String methodName, 
                                      final Class<?>[] parameters)
   throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
   {
      // determine which method we have
      Method m = ps.getClass().getDeclaredMethod(methodName, parameters);

      // debug is set to on, so no times are calculated
      if (debugLevel == DebugLevel.ON)
         return m.invoke(ps, parameters);

      // calculate execution time for verbose debugging
      start();
      Object returnObject = m.invoke(ps, parameters);
      end();

      // return the executions return type
      return returnObject;
   }

   /**
    * Start.
    */
   private void start()
   {
      startTime = System.currentTimeMillis();
   }

   /**
    * End.
    */
   private void end()
   {
      executeTime = System.currentTimeMillis() - startTime;
   }

   /**
    * The Class DebugObject.
    */
   private class DebugObject
   {
      
      /** The debug object. */
      private Object debugObject;
      
      /** The value assigned. */
      private boolean valueAssigned;

      /**
       * Instantiates a new debug object.
       *
       * @param debugObject the debug object
       */
      public DebugObject(final Object debugObject)
      {
         this.debugObject = debugObject;
         valueAssigned = true;
      }

      /**
       * Gets the debug object.
       *
       * @return the debug object
       */
      public Object getDebugObject()
      {
         return debugObject;
      }

      /**
       * Checks if is value assigned.
       *
       * @return true, if is value assigned
       */
      public boolean isValueAssigned()
      {
         return valueAssigned;
      }
   }

   /* (non-Javadoc)
    * @see java.sql.PreparedStatement#getParameterMetaData()
    */
   @Override
   public ParameterMetaData getParameterMetaData() throws SQLException
   {
      return ps.getParameterMetaData();
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#getGeneratedKeys()
    */
   @Override
   public ResultSet getGeneratedKeys() throws SQLException
   {
      return ps.getGeneratedKeys();
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#getMoreResults(int)
    */
   @Override
   public boolean getMoreResults(final int current) throws SQLException
   {
      return ps.getMoreResults();
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#getResultSetHoldability()
    */
   @Override
   public int getResultSetHoldability() throws SQLException
   {
      return ps.getResultSetHoldability();
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#isPoolable()
    */
   @Override
   public boolean isPoolable() throws SQLException
   {
      return ps.isPoolable();
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#setPoolable(boolean)
    */
   @Override
   public void setPoolable(final boolean poolable) throws SQLException
   {
      ps.setPoolable(poolable);
   }

   /* (non-Javadoc)
    * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
    */
   @Override
   public boolean isWrapperFor(final Class<?> iface) throws SQLException
   {
      return ps.isWrapperFor(iface);
   }

   /* (non-Javadoc)
    * @see java.sql.Wrapper#unwrap(java.lang.Class)
    */
   @Override
   public <T> T unwrap(final Class<T> iface) throws SQLException
   {
      return ps.unwrap(iface);
   }

   @Override
   public void closeOnCompletion() throws SQLException 
   {
	   // TODO Auto-generated method stub
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException 
   {
	   // TODO Auto-generated method stub
	   return false;
   }
}
