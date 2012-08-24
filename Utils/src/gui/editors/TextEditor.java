package gui.editors;

import gui.form.TextCellItem;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import utils.ImageTools;

/** Allows simple text editing using a TextAreaItem. */

public class TextEditor extends AbstractCellEditor implements TableCellEditor
{
   private static final long serialVersionUID = 1L;
   
   private TextCellItem editor;

   public TextEditor (final String label, final int rows, final int columns)
   {
      editor = new TextCellItem (null, label, null, rows, columns);
      Icon icon = ImageTools.getIcon ("icons/16/documents/Page Edit.gif");
      editor.setIcon (icon);
   }
   
   // This method is called when a cell value is edited by the user.
   public Component getTableCellEditorComponent (final JTable table,
                                                 final Object value,
                                                 final boolean isSelected,
                                                 final int row, final int vCol)
   {
      editor.setValue (value);
      return editor.getComponent();
   }

   // This method is called when editing is completed.
   // It must return the new value to be stored in the cell.
   public Object getCellEditorValue()
   {
      return editor.getValue();
   }
}
