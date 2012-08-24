package gui.editors;

import gui.form.FileItem;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import utils.ImageTools;

/** Allows selection of a file using a JFileChooser. */

public class FileEditor extends AbstractCellEditor implements TableCellEditor
{
   private static final long serialVersionUID = 1L;
   
   private FileItem editor;

   public FileEditor (final int mode, final String... suffixes)
   {
      editor = FileItem.make (null, 12, null, mode, null, suffixes);
      Icon icon = ImageTools.getIcon ("icons/16/documents/Folder.gif");
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
      return editor.getFile();
   }
}
