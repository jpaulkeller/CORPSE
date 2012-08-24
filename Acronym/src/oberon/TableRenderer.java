package oberon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import utils.ImageTools;

/**
 * Used to render all tables for this application. Supports row colors based on
 * the acronym definition source.
 */
public class TableRenderer extends DefaultTableCellRenderer 
{
   private static final long serialVersionUID = 0L;

   private JCheckBox check;
   private JLabel label;
   
   private JPanel combo;
   private JLabel comboText;
   private JLabel comboIcon;

   public TableRenderer() 
   {
      label = new JLabel();
      label.setOpaque (true); // must do this for background to show up
      // label.setHorizontalAlignment n(JLabel.LEFT);
      
      check = new JCheckBox();
      check.setOpaque (true); // must do this for background to show up
      check.setHorizontalAlignment (JCheckBox.CENTER);
      
      combo = new JPanel (new BorderLayout());
      comboText = new JLabel();
      comboIcon = new JLabel (ImageTools.getIcon ("icons/ArrowDownSmall.gif"));
      combo.add (comboText, BorderLayout.WEST);
      combo.add (comboIcon, BorderLayout.EAST);
   }

   @Override
   public Component getTableCellRendererComponent (final JTable table,
                                                   final Object value,
                                                   final boolean isSelected,
                                                   final boolean hasFocus,
                                                   final int row, 
                                                   final int viewColumn)
   {
      JComponent cell = null;

      if (value instanceof Boolean) 
      {
         check.setSelected ((Boolean) value);
         cell = check;
      }
      else if (value instanceof Definitions)
      {
         Definitions defs = (Definitions) value;
         if ((Boolean) table.getValueAt (row, 0)) // if approved
            comboText.setText ("<html><b>" + defs + "</b></html>");
         else
            comboText.setText (defs.toString());
         
         comboIcon.setVisible (defs.getSelected() != null && 
                               (defs.size() > 2 || 
                                defs.getSelected().getSource() == Source.Empty));
         cell = combo;
      }
      else // Acronym
      {
         label.setText (value.toString());
         cell = label;
      } 

      int modelCol = table.convertColumnIndexToModel (viewColumn);
      Acronym acronym = (Acronym) table.getValueAt (row, 1);

      Color color = acronym.getBackgroundColor();
      if (isSelected)
         cell.setBackground (color);
      else if (modelCol == 2) // Definition
         cell.setBackground (color);
      else
         cell.setBackground (null);

      if (value instanceof Acronym)
         cell.setToolTipText (acronym.getToolTipText());
      else
         cell.setToolTipText (null);

      return cell;
   }
}
