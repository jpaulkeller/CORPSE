package oberon;

import gui.comp.TipComboBox;

import java.awt.Component;
import java.awt.event.ItemEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

public class ComboEditor extends AbstractCellEditor implements TableCellEditor
{
   private static final long serialVersionUID = 1L;

   private Editor editor;
   private Acronym acronym;
   private TableModel model;
   private int modelRow;

   public ComboEditor()
   {
      editor = new Editor();
      editor.setEditable (true);
      editor.setRenderer (new Renderer()); // to bold the acronyms and show the source
   }

   /**
    * This method is called when editing is completed. It must return the new
    * value to be stored in the cell.
    */

   public Object getCellEditorValue()
   {
      return acronym.getDefinitions();
   }

   // This method is called when a cell value is edited by the user.

   public Component getTableCellEditorComponent (final JTable table, 
                                                 final Object value,
                                                 final boolean isSelected, 
                                                 final int row,
                                                 final int vCol)
   {
      model = table.getModel();
      modelRow = table.convertRowIndexToModel (row);
      acronym = (Acronym) table.getValueAt (row, 1);
      editor.reloadValues();
      return editor;
   }

   private class Editor extends TipComboBox
   {
      private static final long serialVersionUID = 1L;

      void reloadValues()
      {
         DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
         for (Definition def : acronym.getDefinitions().getDefinitions())
            comboModel.addElement (def);
         setModel (comboModel);
         if (acronym.getSelected() != null) 
            comboModel.setSelectedItem (acronym.getSelected());
      }

      @Override
      protected void fireItemStateChanged (final ItemEvent e)
      {
         if (e.getStateChange() == ItemEvent.SELECTED)
         {
            Definition oldDef = acronym.getSelected();
            ComboBoxEditor textEditor = getEditor();
            JTextField comp = (JTextField) textEditor.getEditorComponent();
            String text = comp.getText(); // typed or selected            
            
            Definitions defs = acronym.getDefinitions();
            if (text != null)
            {
               if (!text.trim().equals ("") && (oldDef == null || !text.equals (oldDef.getText())))
               {
                  if (!defs.contains (text)) // user added new definition
                     acronym.addValue(text.trim(), Source.User);
                  defs.setSelected (text.trim());
               }
               else if (text.trim().equals ("") && !acronym.getDefinitions().isEmpty())
                  defs.setSelected (Definition.EMPTY);
            }
            
            // hack to avoid JTable issue (when the rows are sorted)
            SwingUtilities.invokeLater (new Runnable()
            {
               public void run()
               {
                  // if the user entered or picked a new definition, auto-approve it
                  model.setValueAt (Boolean.TRUE, modelRow, 0);
               }
            });
         }
         
         super.fireItemStateChanged (e);
         fireEditingStopped();
      } 
   }

   private static class Renderer extends JLabel implements ListCellRenderer
   {
      public Component getListCellRendererComponent (final JList list, 
                                                     final Object value, 
                                                     final int index,
                                                     final boolean isSelected,
                                                     final boolean cellHasFocus)
      {
         Definition def = (Definition) value;
         setText ("<html><b>" + def.getText() + "</b> " +
                  "<i>(" + def.getSource() + ")</i></html>");
         return this;
      }
   }
}
