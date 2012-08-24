package utils;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.Collection;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class EnumEditor extends AbstractCellEditor implements TableCellEditor
{
   private static final long serialVersionUID = 1L;
   
   private Editor editor;

   public EnumEditor (final Object[] values)
   {
      editor = new Editor (values);
   }
   
   public EnumEditor (final Collection<? extends Object> values)
   {
      editor = new Editor (values.toArray());
   }
   
   // This method is called when a cell value is edited by the user.
   public Component getTableCellEditorComponent (final JTable table,
                                                 final Object value,
                                                 final boolean isSelected,
                                                 final int row, final int vCol)
   {
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

      Editor (final Object[] values)
      {
         super (values);
      }

      @Override
      protected void fireItemStateChanged (final ItemEvent e)
      {
         super.fireItemStateChanged (e);
         fireEditingStopped(); 
      }
   }
}
