package corpse.ui;

import gui.db.ColoredTableRenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.jdesktop.swingx.JXTable;

import corpse.CORPSE;

/** This will highlight unresolved tokens. */

public class TokenRenderer extends ColoredTableRenderer implements MouseListener, MouseMotionListener
{
   private static final long serialVersionUID = 1;

   public static final String INVALID_OPEN = "<<";
   public static final String INVALID_CLOSE = ">>";

   // match unresolved tokens (but ignore the <!> last-match pattern)
   private static final String ERROR_REGEX = 
      Pattern.quote(INVALID_OPEN) + "(.+)" + Pattern.quote(INVALID_CLOSE);
   private static final Pattern ERROR = Pattern.compile(ERROR_REGEX);

   private static final Pattern LINK = Pattern.compile("(.*)<(.+)=(.+)>(.*)");

   private CORPSE app;
   private int mouseRow, mouseCol;
   private String link;
   private String firstResponse;
   
   public TokenRenderer (final CORPSE app) 
   {
      this.app = app;
   }

   @Override
   public Component getTableCellRendererComponent(final JTable table, final Object value, 
                                                  final boolean isSelected, final boolean hasFocus, 
                                                  final int row, final int viewColumn)
   {
      JComponent cell = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, viewColumn);

      Matcher m = LINK.matcher(value.toString()); 
      if (m.find())
      {
         JLabel label = (JLabel) cell;
         String linkColor = isSelected ? "yellow" : "blue";
         String html;
         
         firstResponse = m.group(3); // the text to be displayed to the user

         if (mouseRow == row && mouseCol == viewColumn)
         {
            link = m.group(2); // the script name
            html = "<font color=\"" + linkColor + "\"><u>" + firstResponse + "</u></font>";
         }
         else
         {
            link = null;
            html = "<font color=\"" + linkColor + "\">" + firstResponse + "</font>";
         }
         
         html = "<html>" + m.group(1) + html + m.group(4) + "</html>";
         label.setText(html);
      }

      if (ERROR.matcher(value.toString()).find())
         cell.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
      
      return cell;
   }
   
   @Override
   public void mouseClicked(MouseEvent e)
   {
      if (link != null)
         app.openScript(link, firstResponse);
   }

   @Override
   public void mouseMoved(MouseEvent e)
   {
      JXTable table = (JXTable) e.getSource();
      Point p = e.getPoint();
      mouseRow = table.rowAtPoint(p);
      mouseCol = table.columnAtPoint(p);
      table.repaint();
   }
   
   @Override
   public void mousePressed(MouseEvent e)
   {
   }

   @Override
   public void mouseReleased(MouseEvent e)
   {
   }

   @Override
   public void mouseEntered(MouseEvent e)
   {
   }

   @Override
   public void mouseExited(MouseEvent e)
   {
   }

   @Override
   public void mouseDragged(MouseEvent e)
   {
   }
}
