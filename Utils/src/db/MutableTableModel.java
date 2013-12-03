
package db;

import java.util.Comparator;
import java.util.List;
import javax.swing.table.TableModel;

// TBD: extend Collection (for non-optional operations)

public interface MutableTableModel extends TableModel
{
   public String getName();
   public void setName (String name);

   public String getStringAt (int row, String columnName);

   public boolean isEditable();
   public void setEditable (boolean editable);

   public Object getProperty (String property);
   public Object setProperty (String property, Object value);
   public Object removeProperty (String property);

   public boolean addRow (Row row);
   public Row getRow (int row);
   public List<Row> getRows();
   public void remRow (int row);
   public void removeAllRows();
   public MutableTableModel subset (int[] rowsToCopy);

   public TableIterator iterator();
   public void sort (Comparator<Row> comp);

   public List<String> getColumnNames();
   public Column getColumn (int col);
   public int getColumnIndex (String columnName);
   @Override
   public String getColumnName (int col);
   public int getColumnType (int col);
   public String getColumnTypeName (int col);
   
   public boolean isNumeric (int col);
   public boolean isNumeric (String columnName);
   public boolean isDate (int col);
   public boolean isDate (String columnName);
}
