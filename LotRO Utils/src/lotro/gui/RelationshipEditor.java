package lotro.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import lotro.models.Relationship;


public class RelationshipEditor extends AbstractCellEditor
implements TableCellEditor, ActionListener
{
   private static final long serialVersionUID = 1L;
   
   private JButton button;
   private Relationship relationship;

   // This method is called when a cell value is edited by the user.
   public Component getTableCellEditorComponent (final JTable table,
                                                 final Object value,
                                                 final boolean isSelected,
                                                 final int row, final int vCol)
   {
      relationship = (Relationship) value;
      button = new JButton();
      button.setBackground (RelationshipRenderer.getColor (relationship));
      button.addActionListener (this);
      return button;
   }

   // This method is called when editing is completed.
   // It must return the new value to be stored in the cell.
   public Object getCellEditorValue()
   {
      return relationship;
   }
   
   public void actionPerformed (final ActionEvent e)
   {
      if (relationship != Relationship.NONE)
      {
         if (relationship == Relationship.NORMAL)
            relationship = Relationship.JOIN;
         else if (relationship == Relationship.JOIN)
            relationship = Relationship.AVOID;
         else // if (relationship == Relationship.Separate)
            relationship = Relationship.NORMAL;
      }
      button.setText (RelationshipRenderer.getText (relationship));
      button.setBackground (RelationshipRenderer.getColor (relationship));
      fireEditingStopped(); 
   }
}
