package db;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.table.DefaultTableModel;

// TBD: merge with db.Table?

public class Model extends DefaultTableModel
{
   private static final long serialVersionUID = 1L;

   private Map<Integer, Class<?>> types = new HashMap<Integer, Class<?>>();
   private String name;
   private Set<Integer> readOnlyColumns = new HashSet<Integer>();
   private int queryResultTotal = 0;
   
   public Model (final String name)
   {
      setName (name);
   }
   
   public void clear()
   {
      while (getRowCount() > 0)
         removeRow (0);
   }
   
   public void setName (final String name)
   {
      this.name = name;
   }
   
   public String getName()
   {
      return name;
   }
   
   public int getQueryResultTotal()
   {
      return queryResultTotal;
   }

   public void setQueryResultTotal (final int total)
   {
      queryResultTotal = total;
   }
   
   public Object getValueAt (final int row, final String columnName)
   {
      int col = findColumn (columnName);
      if (col >= 0)
         return super.getValueAt (row, col);
      return null;
   }
   
   public String getStringAt (final int row, final String columnName)
   {
      int col = findColumn (columnName);
      if (col >= 0)
         return getStringAt (row, col);
      return null;
   }
   
   public String getStringAt (final int row, final int col)
   {
      String s = null;
      Object value = getValueAt (row, col);
      if (value != null)
         s = value.toString();
      return s;
   }

   /**
    * Returns a textual representation of the value.  Unlike getStringAt,
    * this method will convert CLOB and Date/Time values.
    */
   public String getTextAt (final int row, final String columnName)
   {
      int col = findColumn (columnName);
      if (col >= 0)
         return getTextAt (row, col);
      return null;
   }
   
   public String getTextAt (final int row, final int col)
   {
      String s = null;
      Object value = getValueAt (row, col);
      try
      {
         if (value instanceof oracle.sql.CLOB) // ojdbc14.jar
            s = OracleAPI.getAsString ((oracle.sql.CLOB) value);         
         else if (value instanceof Date ||
                  value instanceof oracle.sql.DATE ||
                  value instanceof oracle.sql.TIMESTAMP ||
                  value instanceof oracle.sql.TIMESTAMPTZ ||
                  value instanceof oracle.sql.TIMESTAMPLTZ)
            s = getDTGAt (row, col);
         else if (value != null)
            s = value.toString();
      }
      catch (SQLException x)
      {
      }
      return s;
   }
   
   public double getDoubleAt (final int row, final int col)
   {
      double d = 0;
      Object value = getValueAt (row, col);
      if (value instanceof Number)
         d = ((Number) value).doubleValue();
      return d;
   }
   
   public double getDoubleAt (final int row, final String columnName, 
                              final double defaultValue)
   {
      int col = findColumn (columnName);
      if (col >= 0)
         return getDoubleAt (row, col);
      return defaultValue;
   }
   
   public Date getDateAt (final int row, final String columnName)
   throws SQLException
   {
      int col = findColumn (columnName);
      if (col >= 0)
         return getDateAt (row, col);
      return null;
   }
   
   public Date getDateAt (final int row, final int col)
   throws SQLException
   {
      Date d = null;
      Object value = getValueAt (row, col);
      if (value instanceof Date)
      {
         // hack to force zulu time
         Calendar cal = Calendar.getInstance();
         cal.setTime ((Date) value);
         cal.set (Calendar.ZONE_OFFSET, 0); // zulu
         d = cal.getTime();
      }
      else if (value instanceof oracle.sql.Datum)
      {
         // hack to force zulu time
         Calendar cal = Calendar.getInstance();
         cal.setTime (((oracle.sql.Datum) value).dateValue());
         cal.set (Calendar.ZONE_OFFSET, 0); // zulu
         d = cal.getTime();
      }
      return d;
   }
   
   public String getDTGAt (final int row, final String columnName)
   throws SQLException
   {
      String dtg = "";
      Date date = getDateAt (row, columnName);
      if (date != null)
         dtg = dateToDTG (date);
      return dtg;
   }

   public String getDTGAt (final int row, final int col)
   throws SQLException
   {
      String dtg = "";
      Date date = getDateAt (row, col);
      if (date != null)
         dtg = dateToDTG (date);
      return dtg;
   }
   
   private String dateToDTG (Date date)
   {
      SimpleDateFormat df = new SimpleDateFormat ("ddHHmmss'Z' MMM yyyy");
      df.setTimeZone (TimeZone.getTimeZone ("Etc/GMT0")); // Zulu
      return df.format (date).toUpperCase();
   }
   
   /**
    * Creates a (possibly empty) list containing the values (as Strings) of each
    * row in the table from the given <i>columnName</i>.
    *
    * @param   columnName  the column whose values are to be looked up
    * @return a list of values from the specified column
    */
   public List<String> getList (final String columnName)
   {
      int col = findColumn (columnName);
      return col >= 0 ? getList (col) : new ArrayList<String>();   
   }
   
   public List<String> getList (int col)
   {
      List<String> list = new ArrayList<String>();
      int qty = getRowCount();
      for (int row = 0; row < qty; row++)
         list.add (getValueAt (row, col).toString());
      return list;
   }   
   
   public void addColumn (final String columnName, final Class<?> type)
   {
      addColumn (columnName);
      setColumnClass (columnName, type);
   }
   
   public void setColumnClass (final String columnName, final Class<?> type)
   {
      int col = findColumn (columnName);
      if (col >= 0)
         types.put (col, type);
   }
   
   // return the index of the update column, or -1.
   
   public int renameColumn (final String oldName, final String newName)
   {
      int columnChanged = -1;
      int count = getColumnCount();
      Object[] columns = new Object [count];
      for (int col = 0; col < count; col++)
      {
         columns[col] = getColumnName (col);
         if (columns[col].equals (oldName))
         {
            columns[col] = newName; // rename the given column
            columnChanged = col;
         }
      }
      setColumnIdentifiers (columns);
      return columnChanged;
   }

   public List<String> getColumns()
   {
      List<String> columns = new ArrayList<String>(); 
      for (int col = 0; col < this.getColumnCount(); col++)
         columns.add (getColumnName (col));
      return columns;
   }
   
   @Override
   public int findColumn (final String columnName)
   {
      for (int col = 0; col < this.getColumnCount(); col++)
         if (this.getColumnName (col).equalsIgnoreCase (columnName))
            return col;
      return -1;
   }

   @Override
   public Class<?> getColumnClass (final int c)
   {
      Class<?> type = types.get (c);
      if (type == null && getRowCount() > 0)
      {
         Object firstValue = getValueAt (0, c);
         if (firstValue != null)
            type = firstValue.getClass(); // default to first value
      }
      if (type == null)
         type = super.getColumnClass (c);
      return type;
   }

   public void addRowValues (final Object... values)
   {
      super.addRow (values);
   }

   public void setColumnEditable (final int col, final boolean editable)
   {
      if (editable)
         readOnlyColumns.remove (col);
      else
         readOnlyColumns.add (col);
   }
   
   @Override
   public boolean isCellEditable (final int row, final int col)
   {
      return readOnlyColumns.contains (col) ? false : super.isCellEditable (row, col);
   }
   
   /**
    * Traverses the table and returns the index of the first row
    * where the value in the given column matches the given value.
    *
    * @return a row index, or -1
    */
   public int search (final String columnName, final String targetValue)
   {
      if (columnName != null && targetValue != null)
      {
         int col = findColumn (columnName);
         if (col >= 0)
            for (int row = 0, rows = getRowCount(); row < rows; row++)
               if (targetValue.equals (getValueAt (row, col).toString()))
                  return row;
      }
      return -1;
   }
   
   /**
    * Traverses the table and returns the index of the first row where
    * the value in the given column matches the given regular
    * expression pattern.
    *
    * @return a row index, or -1 */

   public int search (final String columnName, final String regex, final int flags)
   throws PatternSyntaxException
   {
      int columnIndex = findColumn (columnName);
      if (columnIndex >= 0)
      {
         Pattern p = Pattern.compile (regex, flags);
         for (int rowIndex = 0, n = getRowCount(); rowIndex < n; rowIndex++)
            if (p.matcher (getValueAt (rowIndex, columnIndex).toString()).matches())
               return rowIndex;
      }
      return -1;
   }
}

