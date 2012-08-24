package db;

import gui.db.TableFormat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import str.Token;

/**
 * The <b>ColumnMetaData</b> class implements a table model containing
 * meta-data about the columns for a given database table.
 */
public class ColumnMetaData extends Model
{
   /**
    * Constructs a table model of columns using JDBC's getColumns
    * method against the DBMS associated with the given <i>connName</i>.
    *
    * @param tableName to identify a subset of tables (usually one)
    */
   public ColumnMetaData (final Connection connection, final String schema, 
                          final String tableName)
   throws SQLException
   {
      this (connection, schema, tableName, null);
   }

   /**
    * Constructs a table model for a given columnName using JDBC's
    * getColumn  method against the DBMS associated with the given
    * <i>connName</i>.
    *
    * @param tableName to identify a table
    * @param columnName to identify a subset of columns (usually one)
    */
   public ColumnMetaData (Connection connection, String schema, String tableName, 
                          String columnName)
   throws SQLException
   {
      super (tableName);
      getColumns (connection, schema, tableName, columnName);
      if (getRowCount() == 0) // try again for MS Access
      {
         String[] schemaName = Token.tokenize (tableName, ".");
         if (schemaName.length == 2) // SCHEMA.TABLE
            getColumns (connection, schemaName[0], schemaName[1], null);
      }
      if (getRowCount() == 0)
         throw new SQLException ("Missing table/column: " + tableName + "/" + columnName);
   }

   /**
    * MetaData Cross Reference  COLUMN NAME (COLUMN INDEX).
    *
    * <pre>
    * Postgres              Access 97        Access 2000     Oracle (7.3.2)     Sybase 5.5
    * =================  ===============  =================  ==============     ===============
    *                                     TABLE_CAT                             TABLE_CAT
    *                    TABLE_QUALIFIER                     TABLE_SCHEM        TABLE_SCHEM
    * TABLE_NAME         TABLE_NAME       TABLE_NAME         TABLE_NAME         TABLE_NAME
    * COLUMN_NAME        COLUMN_NAME      COLUMN_NAME        COLUMN_NAME        COLUMN_NAME
    * DATA_TYPE          DATA_TYPE        DATA_TYPE          DATA_TYPE          DATA_TYPE
    * TYPE_NAME          TYPE_NAME        TYPE_NAME (sql92)  TYPE_NAME          TYPE_NAME
    * COLUMN_SIZE        PRECISION        COLUMN_SIZE        COLUMN_SIZE        COLUMN_SIZE
    *                    LENGTH           BUFFER_LENGTH      BUFFER_LENGTH      BUFFER_LENGTH
    * DECIMAL_DIGITS     SCALE            DECIMAL_DIGITS     DECIMAL_DIGITS     DECIMAL_DIGITS
    * NUM_PREC_RADIX     RADIX            NUM_PREC_RADIX     NUM_PREC_RADIX     NUM_PREC_RADIX
    * NULLABLE           NULLABLE         NULLABLE           NULLABLE           NULLABLE
    * REMARKS                                                                   REMAKRS
    *                                                                           COLUMN_DEF
    *                                     SQL_DATA_TYPE      SQL_DATA_TYPE      SQL_DATA_TYPE
    *                                                        SQL_DATETIME_SUB   SQL_DATETIME_SUB
    * CHAR_OCTET_LENGTH                   CHAR_OCTET_LENGTH  CHAR_OCTET_LENGTH  CHAR_OCTET_LENGTH
    * ORDINAL_POSITION                    ORDINAL_POSITION   ORDINAL_POSITION   ORDINAL_POSITION
    * IS_NULLABLE                         IS_NULLABLE        IS_NULLABLE        IS_NULLABLE
    *                    ORDINAL          ORDINAL
    * </pre>
    *
    * WARNING: Indices do not match across DMBSs.  Values should always be 
    * obtained via the string name, NOT the index.
    */
   public Column getColumn()
   {
      String colName = (String) getValueAt (0, "COLUMN_NAME");
      int type = (int) getDoubleAt (0, "DATA_TYPE", 0);
      String typeName = (String) getValueAt (0, "TYPE_NAME");
      int size = (int) getDoubleAt (0, "COLUMN_SIZE", -1);
      if (size == -1)
         size = (int) getDoubleAt (0, "PRECISION", -1);  // Access 97
      /*
      int digits = getInt ("DECIMAL_DIGITS", -1);
      if (digits == -1)
         digits = getInt ("SCALE", -1); // Access 97
      */
      return new Column (colName, type, typeName, size);
   }

   private void getColumns (Connection connection,
                            String schema,
                            String tableName,
                            String columnName)
   throws SQLException
   {
      DatabaseMetaData md = connection.getMetaData();
      ResultSet rs = md.getColumns (null, schema, tableName, columnName);
      if (rs != null)
      {
         try
         {
            convertResultSet (rs);
         }
         finally
         {
            rs.close();
         }
      }
      else
         System.out.println ("No columns found for: " + tableName);
   }

   /**
    * Returns a list of the columns of a table.  Note that this list
    * is not sorted, since some clients may want the columns ordered
    * as returned by JDBC.
    *
    * @return a (unsorted) list of column names
    */
   public List<String> getTableColumns()
   {
      return getList ("COLUMN_NAME");
   }

   /**
    * Determines if the given columnName refers to a numeric column.
    *
    * @param columnName a name from the table (not the meta table)
    * @return true if the given columnName refers to a numeric column
    */
   public boolean isFieldNumeric (String columnName)
   {
      boolean numeric = false;
      int row = search ("COLUMN_NAME", columnName);

      if (row < 0) // try again using case-insensitive regular expression
      {
         try
         {
            row = search ("COLUMN_NAME", "^" + columnName + "$", Pattern.CASE_INSENSITIVE);
         }
         catch (PatternSyntaxException x)
         {
            System.out.println (x + ": " + columnName);
         }
      }

      if (row >= 0)
      {
         String s = (String) getValueAt (row, "DATA_TYPE");
         if (s != null)
         {
            try
            {
               int dataType = Integer.parseInt (s);
               switch (dataType)
               {
               case Types.BIGINT:
               case Types.BIT:
               case Types.DECIMAL:
               case Types.DOUBLE:
               case Types.FLOAT:
               case Types.INTEGER:
               case Types.NUMERIC:
               case Types.REAL:
               case Types.SMALLINT:
               case Types.TINYINT:
                  numeric = true;
               }
            }
            catch (NumberFormatException e)
            {
               System.err.println (e + ": " + e.getMessage());
            }
         }
         else
            System.err.println
               ("Missing DATA_TYPE for " + getName() + " " + columnName);
      }
      else
         System.err.println ("Invalid " + getName() + " column: " + columnName);

      return numeric;
   }

   /**
    * A static convenience method that returns the columns of a table
    * in a list.  Note that this list is not sorted, since some
    * clients may want the columns ordered as returned by JDBC.
    *
    * @param connName which uniquely identifies a DBMS connection
    * @param tableName any table name
    * @return a (unsorted) list of column names, or null
    */
   public static List<String> getColumns (Connection connection, String schema, String tableName)
   throws SQLException
   {
      ColumnMetaData tbl = new ColumnMetaData (connection, schema, tableName);
      return tbl.getTableColumns();
   }

   private void convertResultSet (final ResultSet rs) throws SQLException
   {
      ResultSetMetaData rsmd = rs.getMetaData();
      if (rsmd != null)
      {
         int colQty = rsmd.getColumnCount();
         if (colQty > 0)
         {
            for (int col = 0; col < colQty; col++)
               appendColumn (rsmd, col);

            while (rs.next())
            {
               Vector<Object> row = new Vector<Object>();
               for (int col = 0; col < colQty; col++)
                  row.add (rs.getString (col + 1));
               addRow (row);
            }
         }
         else
            System.out.println ("Empty metadata for: " + getName());
      }
      else
         System.out.println ("No metadata for: " + getName());
   }
   
   private void appendColumn (final ResultSetMetaData rsmd, final int i)
   throws SQLException
   {
      int col = i + 1; // JDBC columns start at 1, not 0
      String colName  = rsmd.getColumnName (col);
      // int type        = rsmd.getColumnType (col);
      // String typeName = rsmd.getColumnTypeName (col);
      // int dispSize    = rsmd.getColumnDisplaySize (col);
      // columns.add (new Column (colName, type, typeName, dispSize)); // TBD
      addColumn (colName);
   }

   public static void main (String[] args)
   {
      try
      {
         String url = System.getenv ("DB_URL"); // jdbc:oracle:thin:@12.69.43.74:1521:ORCL 
         String user = "jacob_data"; // System.getenv ("DB_USER");
         String pswd = "jacob"; // System.getenv ("DB_PSWD");
         String schema = "JACOB_DATA";
         
         System.out.println ("Connecting to: " + url);
         SQL sql = OracleDriver.connect (url, user, pswd);

         List<String> tables = new ArrayList<String>();
         tables.add ("???"); // TODO
         
         for (String table : tables)
         {
            ColumnMetaData tbl = new ColumnMetaData (sql.getConnection(), schema, table);
            System.out.println (new TableFormat (tbl).getFormattedBuffer());
            /*
            System.out.println ("drop table " + table + ";\n");
            System.out.println ("create table " + table + " (");
            for (int row = 0, rows = tbl.getRowCount(); row < rows; row++)
            {
               String colName = (String) tbl.getValueAt (row, "COLUMN_NAME");
               String type = (String) tbl.getValueAt (row, "TYPE_NAME");
               String size = (String) tbl.getValueAt (row, "COLUMN_SIZE");
               System.out.print ("   " + StringUtils.pad (colName, 40));
               if (type.startsWith ("TIMESTAMP"))
                  System.out.print ("TIMESTAMP");
               else if (type.equals ("CLOB"))
                  System.out.print (type);
               else
               {
                  System.out.print (type);
                  System.out.print ("(");
                  System.out.print (size);
                  if (type.equals ("NUMBER"))
                     System.out.print ("," + tbl.getValueAt (row, "DECIMAL_DIGITS"));
                  System.out.print (")");
               }
               if ("NO".equals (tbl.getValueAt (row, "IS_NULLABLE")))
                  System.out.print (" NOT NULL");
               if (row < rows - 1)
                  System.out.println (",");
            }
            System.out.println (");\n");
            */
         }
      }
      catch (Exception x)
      {
         System.err.println (x.getMessage());
         x.printStackTrace (System.err);
      }
   }
}
