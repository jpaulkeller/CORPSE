package gui.editors;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;

public class RangeEditor extends AbstractCellEditor implements TableCellEditor
{
   private static final long serialVersionUID = 1L;
   
   private Editor editor;

   public RangeEditor (final int min, final int max, final int step)
   {
      editor = new Editor (min, max, step);
   }
   
   // This method is called when a cell value is edited by the user.
   public Component getTableCellEditorComponent (final JTable table,
                                                 final Object value,
                                                 final boolean isSelected,
                                                 final int row, final int vCol)
   {
      editor.setValue (value);
      return editor;
   }

   // This method is called when editing is completed.
   // It must return the new value to be stored in the cell.
   public Object getCellEditorValue()
   {
      return editor.getValue();
   }
   
   private class Editor extends JSpinner implements FocusListener
   {
      private static final long serialVersionUID = 1L;
      
      Editor (final int min, final int max, final int step)
      {
         super (new SpinnerNumberModel (min, min, max, step));
         addFocusListener (this);
      }

      // This approach doesn't quite work; focus is not lost when a menu item
      // is selected.
      public void focusLost (final FocusEvent e)
      {
         fireEditingStopped(); 
      }
      
      public void focusGained (final FocusEvent e)
      {
      }
   }
}
