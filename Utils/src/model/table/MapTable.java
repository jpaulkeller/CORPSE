package model.table;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * The MapTable class uses a Map as a data model, and implements TableModel by
 * extending AbstractTableModel.
 * 
 * The model (a property mapping) is provided by the caller. Note that if the
 * value is not a "simple type" (currently String or Boolean), it is ignored by
 * this class. This allows "hidden" properties to be stored in the model, but
 * not accessible to the user. This behavior can be controlled by calling the
 * hideNonSimple() method.
 */

public class MapTable<K, V> extends AbstractTableModel
{
   private static final long serialVersionUID = 13;

   private String keyName;
   private String valName;

   private Map<K, V> map; // the model
   
   private Comparator<K> comparator;
   private boolean editable;
   private boolean hideNonSimpleKeys = true;
   private boolean hideNonSimpleValues = true;

   private int keyCol = 0;
   private int valCol = 1;

   private Vector<Object> row1 = new Vector<Object>();
   private Vector<Object> row2 = new Vector<Object>();

   public MapTable()
   {
      this (null);
   }

   public MapTable (final Map<K, V> map)
   {
      this (map, "Property", "Value");
   }

   public MapTable (final Map<K, V> map, final String keyName, final String valName)
   {
      this.map = (map != null) ? map : new HashMap<K, V>();
      this.keyName = keyName;
      this.valName = valName;
   }

   public void setComparator (final Comparator<K> comparator)
   {
      this.comparator = comparator;
   }

   public void swapColumns()
   {
      int tmp = keyCol;
      keyCol = valCol;
      valCol = tmp;
   }

   public V put (final K key, final V value)
   {
      V currentValue = map.get (key);
      if (value.equals (currentValue))
         return value; // no change required

      synchronized (map)
      {
         map.put (key, value);
      }
      /*
       * TBD: determine the actual row if (currentValue == null)
       * fireTableRowsInserted (0, 0); else fireTableRowsUpdated (0, 0);
       */
      fireTableDataChanged();

      return currentValue;
   }

   public V get (final K property)
   {
      return map.get (property);
   }

   public V remove (final K property)
   {
      V value;
      synchronized (map)
      {
         value = map.remove (property);
      }

      if (value != null)
         // fireTableRowsDeleted (0, 0); // TBD: determine the actual row
         fireTableDataChanged();

      return value;
   }

   public void setMap (final Map<K, V> newMap)
   {
      // TBD maybe should replace map?
      if (map != newMap)
      {
         synchronized (map)
         {
            map.clear();
            map.putAll (newMap);
         }
      }
      fireTableDataChanged();
   }

   public Map<K, V> getMap()
   {
      return map;
   }

   public void setEditable (final boolean editable)
   {
      this.editable = editable;
   }

   public void hideNonSimple (final boolean keys, final boolean values)
   {
      this.hideNonSimpleKeys = keys;
      this.hideNonSimpleValues = values;
   }

   protected boolean isHidden (final K key, final V val)
   {
      if (hideNonSimpleKeys && hideNonSimpleValues)
         return !isSimple (key) || !isSimple (val);
      else if (hideNonSimpleKeys)
         return !isSimple (key);
      else if (hideNonSimpleValues)
         return !isSimple (val);
      return false;
   }

   protected boolean isSimple (final Object o)
   {
      return o instanceof String || o instanceof Boolean;
   }

   // implement TableModel

   public int getRowCount()
   {
      if (!hideNonSimpleKeys && !hideNonSimpleValues)
         return map.size();

      int count = 0;

      // traverse the map, ignoring any hidden values
      synchronized (map)
      {
         Set<Map.Entry<K, V>> entries = map.entrySet();
         for (Map.Entry<K, V> entry : entries)
            if (!isHidden (entry.getKey(), entry.getValue()))
               count++;
      }

      return count;
   }

   public int getColumnCount()
   {
      return 2;
   }

   public Iterator<K> getMapIterator()
   {
      TreeSet<K> sorted;
      if (comparator == null)
         sorted = new TreeSet<K>();
      else
         sorted = new TreeSet<K> (comparator);
      sorted.addAll (map.keySet());
      return sorted.iterator();
   }

   public void sort (final Comparator<Vector<Object>> rowSorter)
   {
      setComparator (new MapComparator (rowSorter));
      fireTableDataChanged();
   }

   protected K getKeyAt (final int row)
   {
      if (row >= getRowCount())
         return null;

      // traverse the map (sorted by key)
      synchronized (map)
      {
         Iterator<K> iterator = getMapIterator();
         int rowsToSkip = row;
         while (iterator.hasNext())
         {
            K key = iterator.next();
            V val = map.get (key);
            if (!isHidden (key, val) && (rowsToSkip-- == 0))
               return key;
         }
      }

      return null;
   }

   protected V getValAt (final int row)
   {
      if (row >= getRowCount())
         return null;

      // traverse the map (sorted by key)
      synchronized (map)
      {
         Iterator<K> iterator = getMapIterator();
         int rowsToSkip = row;
         while (iterator.hasNext())
         {
            K key = iterator.next();
            V val = map.get (key);
            if (!isHidden (key, val) && (rowsToSkip-- == 0))
               return val;
         }
      }

      return null;
   }

   public Object getValueAt (final int row, final int column)
   {
      if (column == keyCol)
         return getKeyAt (row);
      else if (column == valCol)
         return getValAt (row);
      return null;
   }

   // override AbstractTableModel methods

   @Override
   public String getColumnName (final int column)
   {
      if (column == keyCol)
         return keyName;
      else if (column == valCol)
         return valName;
      return null;
   }

   @Override
   public boolean isCellEditable (final int rowIndex, final int columnIndex)
   {
      return editable;
   }

   @Override
   public void setValueAt (final Object value, final int row, final int column)
   {
      if (!editable || (row >= getRowCount()) || (column >= getColumnCount()))
         return;

      int skip = row;

      // traverse the map (sorted by the comparator)
      synchronized (map)
      {
         Iterator<K> iterator = getMapIterator();
         while (iterator.hasNext())
         {
            K key = iterator.next();
            V currentValue = map.get (key);
            if (!isHidden (key, currentValue) && (skip-- == 0)) // at right
                                                                  // row
            {
               setValue (key, currentValue, value, row, column);
               break;
            }
         }
      }
   }

   @SuppressWarnings ("unchecked")
   protected void setValue (final K key, final V currentValue, 
                            final Object value, final int row, final int column)
   {
      if ((value == null) || value.equals (""))
      {
         remove (key);
         return;
      }

      if ((column == keyCol) && value.equals (key))
         return; // property name didn't change
      else if ((column == valCol) && value.equals (currentValue))
         return; // property value didn't change

      // make the change to the model
      synchronized (map)
      {
         // this fails to compile, but I'm not sure why
         /*
          * if (column == keyCol && value instanceof K) map.put ((K) value,
          * map.remove (key)); // new key else if (column == valCol && value
          * instanceof V) map.put (key, (V) value); // new value
          */
         // so use this version instead
         if (column == keyCol)
            map.put ((K) value, map.remove (key)); // new key
         else if (column == valCol)
            map.put (key, (V) value); // new value
      }

      fireTableCellUpdated (row, column);
   }

   class MapComparator implements Comparator<K>
   {
      private Comparator<Vector<Object>> rowSorter;

      public MapComparator (final Comparator<Vector<Object>> rowSorter)
      {
         this.rowSorter = rowSorter;
      }

      public int compare (final K key1, final K key2)
      {
         row1.set (keyCol, key1);
         row1.set (valCol, map.get (key1));
         row2.set (keyCol, key2);
         row2.set (valCol, map.get (key2));
         return rowSorter.compare (row1, row2);
      }
   }

   public static void main (final String[] args)
   {
      Map<String, Object> map = new HashMap<String, Object>();
      MapTable<String, Object> table = new MapTable<String, Object> (map);
      table.put ("One", "1");
      table.put ("Two", 2);
      table.put ("Three", "3");
      table.put ("Four", 4);
      table.put ("Five", Boolean.TRUE);

      System.out.println ("getRowCount() => " + table.getRowCount());

      // show the table
      for (int c = 0; c < table.getColumnCount(); c++)
         System.out.print (table.getColumnName (c) + "\t");
      System.out.println();
      for (int r = 0; r < table.getRowCount(); r++)
      {
         for (int c = 0; c < table.getColumnCount(); c++)
            System.out.print (table.getValueAt (r, c) + "\t");
         System.out.println();
      }
      System.out.println();

      // stop hiding the non-String values
      table.hideNonSimple (true, false);
      System.out.println ("getRowCount() => " + table.getRowCount());

      // show the map
      Collection<String> keys = map.keySet();
      for (String key : keys)
         System.out.println ("    " + key + " = " + map.get (key));
      System.out.println();
   }
}
