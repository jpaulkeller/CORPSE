package lotro.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import lotro.models.Assignment;


public class AssignmentEditor extends AbstractCellEditor implements TableCellEditor
{
   private static final long serialVersionUID = 1L;
   
   private EditableCheckBox checkBox;
   private Assignment assignment;

   // This method is called when a cell value is edited by the user.
   public Component getTableCellEditorComponent (final JTable table,
                                                 final Object value,
                                                 final boolean isSelected,
                                                 final int row, final int vCol)
   {
      assignment = (Assignment) value;
      checkBox = new EditableCheckBox();
      checkBox.setSelected (assignment.isNeeded());
      checkBox.decorate (assignment);
      return checkBox;
   }

   // This method is called when editing is completed.
   // It must return the new value to be stored in the cell.
   public Object getCellEditorValue()
   {
      return assignment;
   }
   
   class EditableCheckBox extends AssignmentRenderer
   {
      private static final long serialVersionUID = 1L;

      @Override
      protected void fireItemStateChanged (final ItemEvent e)
      {
         super.fireItemStateChanged (e);
         assignment.setNeeded (isSelected());
         decorate (assignment);
         fireEditingStopped(); 
      }
   }
}
