package model.table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import file.FileUtils;

public final class TableToHTML
{
   private TableToHTML() { }
   
   public static void export (final JTable view, final File file)
   {
      PrintStream out = null;
      try
      {
         FileUtils.makeDir (file.getParent());
         FileOutputStream fos = new FileOutputStream (file); 
         out = new PrintStream (fos, true, FileUtils.UTF8); // support Unicode
         
         out.println ("<html>");
         out.println ("<meta http-equiv=\"Content-Type\" content=\"text/html; " +
         "charset=utf-8\" />\n");
         out.println ("<body>");
         
         out.println ("<table border=1 cellspacing=1 cellpadding=1>");
         
         out.println ("  <tr bgcolor=yellow>");
         int columnCount = view.getColumnCount();
         for (int col = 0; col < columnCount; col++)
            out.println ("    <th>" + view.getColumnName (col) + "</th>");
         out.println ("  </tr>");
         
         for (int row = 0, rows = view.getRowCount(); row < rows; row++)
         {
            out.println ("  <tr>");
            for (int col = 0; col < columnCount; col++)
            {
               Object value = view.getValueAt (row, col);
               if (value instanceof Number)
                  out.println ("    <td align=right>" + value + "</td>");
               else if (value instanceof Date)
                  out.println ("    <td align=center>" + value + "</td>");
               else if (value == null)
                  out.println ("    <td>&nbsp;</td>");
               else
                  out.println ("    <td align=center>" + value + "</td>");
            }
            out.println ("  </tr>");
         }
         
         out.println ("</table>");
         
         out.println ("</body>");
         out.println ("</html>");
         
         out.flush();
         
         JOptionPane.showMessageDialog (null, "Saved as: " + file, "Export Complete", 
                                        JOptionPane.INFORMATION_MESSAGE, null);
      }
      catch (IOException x)
      {
         System.err.println ("Unable to write: " + file);
         x.printStackTrace (System.err);
         JOptionPane.showMessageDialog (null, x.getMessage(), "Export Failed", 
                                        JOptionPane.ERROR_MESSAGE, null);
      }
      finally
      {
         FileUtils.close (out);
      }
   }
}
