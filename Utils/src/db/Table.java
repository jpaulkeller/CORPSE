package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

// TBD: merge with db.Model?

public class Table extends DefaultTableModel
{
   private static final long serialVersionUID = 1L;

   private Connection conn;
   
   public Table (final Connection conn)
   {
      this.conn = conn;
   }

   public void load (final CharSequence sql) throws SQLException
   {
      boolean schemaChanged = getColumnCount() == 0;
      
      Statement stmt = null;
      ResultSet rs = null;

      try
      {
         stmt = conn.createStatement();
         if (stmt != null)
         {
            rs = stmt.executeQuery (sql.toString());
            if (rs != null)
               convertResultSet (rs);
         }
      }
      finally // ensure ResultSet and Statement are closed
      {
         SQL.close (stmt, rs);
      }

      if (schemaChanged)
         fireTableStructureChanged();
      else
         fireTableDataChanged();
   }

   protected void convertResultSet (final ResultSet rs) 
   throws SQLException
   {
      ResultSetMetaData rsmd = rs.getMetaData();
      int colQty = rsmd != null ? rsmd.getColumnCount() : 0;
      if (colQty > 0)
      {
         if (getColumnCount() == 0) // first load, add the columns
            for (int col = 0; col < colQty; col++)
               appendColumn (rsmd, col);
         
         convertRows (rs, colQty);
      }
   }
   
   protected void appendColumn (final ResultSetMetaData rsmd, final int i)
   throws SQLException
   {
      int col = i + 1;          // JDBC columns start at 1, not 0
      addColumn (rsmd.getColumnName (col));
      /*
      String typeName = rsmd.getColumnTypeName (col);
      int type        = rsmd.getColumnType (col);
      int dispSize    = rsmd.getColumnDisplaySize (col);
      System.out.println (colName + " " + typeName + ":" + dispSize);
      */
   }
   
   protected void convertRows (final ResultSet rs, final int colQty)
   throws SQLException
   {
      while (rs.next())
      {
         Vector<Object> row = new Vector<Object> (colQty);
         Object element = null;
         for (int col = 0; col < colQty; col++)
         {
            try
            {
               element = rs.getObject (col + 1);
            }
            catch (Exception x)
            {
               x.printStackTrace();
               element = "-1";
            }
            row.add (element);
         }
         
         addRow (row);
      }
   }
}
