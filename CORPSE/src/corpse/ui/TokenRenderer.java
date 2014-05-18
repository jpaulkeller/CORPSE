package corpse.ui;

import gui.db.ColoredTableRenderer;

import java.awt.Color;
import java.awt.Component;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;

/** This will highlight unresolved tokens. */

public class TokenRenderer extends ColoredTableRenderer
{
   private static final long serialVersionUID = 1;

   public static final String INVALID_OPEN = "<";
   public static final String INVALID_CLOSE = ">";

   // match unresolved tokens (but ignore the <!> last-match pattern)
   private static final Pattern ERROR = Pattern.compile(INVALID_OPEN + "([^!]+)" + INVALID_CLOSE);

   @Override
   public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                  final boolean hasFocus, final int row, final int viewColumn)
   {
      JComponent cell = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, viewColumn);

      if (ERROR.matcher(value.toString()).find())
         cell.setBorder(BorderFactory.createLineBorder(Color.RED, 2));

      return cell;
   }
}
