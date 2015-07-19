package gui.db;

import gui.ColorField;

import java.awt.Color;
import java.awt.Component;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Used to render all tables for this application.  Supports alternating row
 * colors, check-boxes for boolean data, right-aligned numeric data, and 
 * "colorized" cell borders to highlight special values.
 */
public class ColoredTableRenderer extends DefaultTableCellRenderer
{
   private static final long serialVersionUID = 0L;
   
   private static final Color ODD_COLOR  = null;
   private static final Color EVEN_COLOR = new Color (245, 255, 245); // pale green
   
   protected static final Border PLAIN = BorderFactory.createEmptyBorder (1, 2, 1, 2);

   // custom field-specific formats
   private Map<Integer, Format> formatsByColumn = new HashMap<Integer, Format>();
   // custom (and default) type-specific formats
   private Map<Class<?>, Format> formatsByClass = new HashMap<Class<?>, Format>();
   
   // plug-in decorator for cell-specific values (border colors, tool=tips)
   private Map<Class<?>, CellDecorator> decorators = new HashMap<Class<?>, CellDecorator>();
   
   private JLabel label;
   private JCheckBox check;
   
   public ColoredTableRenderer()
   {
      // set default formats
      SimpleDateFormat ymd = new SimpleDateFormat ("yyyy-MM-dd");
      setFormat (Date.class, ymd);
      setFormat (Timestamp.class, ymd);
      
      label = new JLabel();
      label.setBorder (PLAIN);
      label.setOpaque (true); // must do this for background to show up
      
      check = new JCheckBox();
      check.setBorder (PLAIN);
      check.setOpaque (true); // must do this for background to show up
      check.setHorizontalAlignment (JCheckBox.CENTER);
   }
   
   public void setFormat (final int modelCol, final Format format)
   {
      formatsByColumn.put (modelCol, format);
   }
   
   public void setFormat (final Class<?> type, final Format format)
   {
      formatsByClass.put (type, format);
   }
   
   public void setDecorator (final Class<?> type, final CellDecorator decorator)
   {
      decorators.put (type, decorator);
   }
   
   @Override
   public Component getTableCellRendererComponent (final JTable table,
                                                   final Object value,
                                                   final boolean isSelected,
                                                   final boolean hasFocus,
                                                   final int row,
                                                   final int viewColumn)
   {
      Color borderColor = null;
      
      Object actualValue = value;
      if (value instanceof ColorField)
      {
         ColorField validated = (ColorField) value;
         borderColor = validated.getColor();
         actualValue = validated.getValue();
      }

      JComponent cell = actualValue instanceof Boolean ? check : label;
      formatValue (table, viewColumn, actualValue);
      setBackground (table, isSelected, hasFocus, row, viewColumn, actualValue, cell);
      setBorder (borderColor, cell); 
      decorateCell (table, isSelected, row, viewColumn, actualValue, cell);
      align (actualValue);
      
      return cell;
   }

   private void formatValue (final JTable table, final int viewColumn, final Object value)
   {
      int modelCol = table.convertColumnIndexToModel (viewColumn);

      Format format = formatsByColumn.get (modelCol); // column-specific format
      
      if (value != null) // type-specific format (check up the hierarchy)
      {
         Class<?> cls = value.getClass();
         while (format == null && cls != null)
         {
            format = formatsByClass.get (cls);
            cls = cls.getSuperclass();
         }
      }
      
      if (value instanceof Boolean)
         check.setSelected ((Boolean) value);
      else if (value instanceof Date && format != null)
         label.setText (format.format (value));
      else if (value instanceof Number)
      {
         /* if (((Number) value).doubleValue() == 0)
            label.setText ("");
         else */ if (format != null)
            label.setText (format.format (((Number) value).doubleValue()));
         else
            label.setText (value.toString());
      }
      else if (format != null)
         label.setText (format.format (value));
      else if (value != null)
         label.setText (value.toString());
      else
         label.setText ("");
   }

   private void decorateCell (final JTable table, 
                              final boolean isSelected,
                              final int viewRow, final int viewColumn,
                              final Object value,
                              final JComponent cell)
   {
      CellDecorator decorator = null;
      
      if (value != null) // class-specific decorator (check up the hierarchy)
      {
         Class<?> cls = value.getClass();
         while (decorator == null && cls != null)
         {
            decorator = decorators.get (cls);
            cls = cls.getSuperclass();
         }
      }

      if (decorator != null)
      {
         int row = table.convertRowIndexToModel (viewRow);
         int col = table.convertColumnIndexToModel (viewColumn);
         cell.setToolTipText (decorator.getToolTipText (table, row, col, value));
         
         if (!isSelected)
         {
            Color bgColor = decorator.getBackgroundColor (table, row, col, value);
            if (bgColor != null)
               cell.setBackground (bgColor);
         }
         
         Color borderColor = decorator.getBorderColor (table, row, col, value);
         setBorder (borderColor, cell); 
      }
   }

   private void setBackground (final JTable table, 
                               final boolean isSelected, final boolean hasFocus, 
                               final int row, final int viewColumn, 
                               final Object value, final JComponent cell)
   {
      if (isSelected)
      {
         Component comp = super.getTableCellRendererComponent
            (table, value, isSelected, hasFocus, row, viewColumn);
         cell.setBackground (comp.getBackground()); // [r=49,g=106,b=197]
         cell.setForeground (comp.getForeground());
      }
      else // alternate row colors
      {
         cell.setBackground ((row % 2 == 0) ? EVEN_COLOR : ODD_COLOR);
         cell.setForeground (Color.BLACK);
      }
   }

   private void setBorder (final Color borderColor, final JComponent cell)
   {
      if (borderColor != null)
         cell.setBorder (BorderFactory.createLineBorder (borderColor, 2));
      else
         cell.setBorder (PLAIN);
   }

   private void align (final Object value)
   {
      if (value instanceof Number)
         label.setHorizontalAlignment (JLabel.RIGHT);
      else if (value instanceof Date)
         label.setHorizontalAlignment (JLabel.CENTER);
      else
         label.setHorizontalAlignment (JLabel.LEFT);
   }
}
