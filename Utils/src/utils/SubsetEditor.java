package utils;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.MutableComboBoxModel;
import javax.swing.table.TableCellEditor;

/** Allows selection of a value from the current columns list of values. */

public class SubsetEditor extends AbstractCellEditor implements TableCellEditor
{
   private static final long serialVersionUID = 1L;
   
   private Editor editor;

   public SubsetEditor (final boolean editable)
   {
      editor = new Editor();
      editor.setEditable (editable);
   }
   
   // This method is called when a cell value is edited by the user.
   public Component getTableCellEditorComponent (final JTable table,
                                                 final Object value,
                                                 final boolean isSelected,
                                                 final int row, final int vCol)
   {
      editor.reloadValues (table, vCol);
      editor.setSelectedItem (value);
      return editor;
   }

   // This method is called when editing is completed.
   // It must return the new value to be stored in the cell.
   public Object getCellEditorValue()
   {
      return editor.getSelectedItem();
   }
   
   private class Editor extends JComboBox
   {
      private static final long serialVersionUID = 1L;
      
      void reloadValues (final JTable table, final int col)
      {
         MutableComboBoxModel model = (MutableComboBoxModel) getModel();
         while (model.getSize() > 0)
            model.removeElementAt (0);
         for (Object val : getColumnValues (table, col))
            model.addElement (val);
      }

      SortedSet<Object> getColumnValues (final JTable table, final int col)
      {
         SortedSet<Object> set = new TreeSet<Object>();
         for (int row = 0; row < table.getRowCount(); row++)
            set.add (table.getValueAt (row, col));
         return set;
      }

      @Override
      protected void fireItemStateChanged (final ItemEvent e)
      {
         super.fireItemStateChanged (e);
         fireEditingStopped(); 
      }
   }
}
