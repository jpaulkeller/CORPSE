package lotro.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import lotro.models.Assignment;


/** This will highlight assigned deeds. */

public class AssignmentRenderer extends JCheckBox implements TableCellRenderer
{
   private static final long serialVersionUID = 1;
   
   public AssignmentRenderer()
   {
      setOpaque (true); // must do this for background to show up
      setHorizontalAlignment (SwingConstants.CENTER);
   }
   
   public Component getTableCellRendererComponent (final JTable table,
                                                   final Object value,
                                                   final boolean isSelected,
                                                   final boolean hasFocus,
                                                   final int row,
                                                   final int viewColumn)
   {
      Assignment assignment = (Assignment) value;
      setSelected (assignment.isNeeded());
      decorate (assignment);
      return this;
      
   }
   
   public void decorate (final Assignment assignment)
   {
      Color color = null;
      if (assignment.isOrganized())
      {
         if (assignment.isNeeded() && assignment.isAssigned())
            color = Color.GREEN; // needs and assigned
         else if (assignment.isNeeded())
            color = Color.YELLOW; // needs but not assigned
         else if (assignment.isAssigned())
            color = Color.CYAN; // assigned but not needed
      }
      else if (assignment.isNeeded())
         color = Color.LIGHT_GRAY; // not yet organized

      // TBD: assign color based on character level vs deed level?
         
      setBackground (color);
   }
}
