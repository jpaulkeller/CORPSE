package gui.editors;

import gui.comp.TipComboBox;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.Collection;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

public class EnumEditor<E> extends AbstractCellEditor implements TableCellEditor
{
   private static final long serialVersionUID = 1L;

   private Collection<E> model;
   private Editor editor;

   public EnumEditor (final Set<E> values)
   {
      this (values, false);
   }
   
   /** Note: Only set editable to true if E is String. */
   
   public EnumEditor (final Set<E> values, final boolean editable)
   {
      this.model = values;
      editor = new Editor();
      editor.setEditable (editable);
   }
   
   public void addValue (final E value)
   {
      model.add (value);
      // editor.setModel (new DefaultComboBoxModel (model.toArray()));
   }
   
   // This method is called when a cell value is edited by the user.
   @SuppressWarnings("unchecked")
   public Component getTableCellEditorComponent (final JTable table,
                                                 final Object value,
                                                 final boolean isSelected,
                                                 final int row, final int vCol)
   {
      editor.reloadValues ((E) value);
      return editor;
   }

   // This method is called when editing is completed.
   // It must return the new value to be stored in the cell.
   public Object getCellEditorValue()
   {
      return editor.getSelectedItem();
   }
   
   private class Editor extends TipComboBox
   {
      private static final long serialVersionUID = 1L;

      void reloadValues (final E selected)
      {
         DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
         for (E element : model)
            comboModel.addElement (element);
         setModel (comboModel);
         if (selected != null)
            comboModel.setSelectedItem (selected);
      }

      @Override
      @SuppressWarnings("unchecked")
      protected void fireItemStateChanged (final ItemEvent e)
      {
         if (isEditable() &&  e.getStateChange() == ItemEvent.SELECTED)
         {
            ComboBoxEditor textEditor = getEditor();
            JTextField comp = (JTextField) textEditor.getEditorComponent();
            String text = comp.getText(); // typed or selected
            if (text != null && !text.trim().equals (""))
               addValue ((E) text);
         }
         
         super.fireItemStateChanged (e);
         fireEditingStopped();
      }       
   }
}
