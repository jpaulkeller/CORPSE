package lotro.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import lotro.models.Relationship;

public class RelationshipRenderer extends JLabel implements TableCellRenderer
{
   private static final long serialVersionUID = 1;
   
   public RelationshipRenderer()
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
      Relationship relationship = (Relationship) value;
      decorate (relationship);
      return this;
   }
   
   public void decorate (final Relationship relationship)
   {
      setText (getText (relationship));
      setBackground (getColor (relationship));
   }
   
   public static String getText (final Relationship relationship)
   {
      switch (relationship)
      {
      case JOIN:   return "Join";
      case AVOID:  return "Avoid";
      default:     return "";
      }
   }
   
   public static Color getColor (final Relationship relationship)
   {
      switch (relationship)
      {
      case NORMAL: return Color.WHITE;
      case JOIN:   return Color.GREEN;
      case AVOID:  return Color.PINK;
      default:     return Color.LIGHT_GRAY;
      }
   }
}
