package model.table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import file.FileUtils;

public final class TableToFixed
{
   private TableToFixed() { }
   
   public static void export (final JTable view, final File file)
   {
      // find the max width for each column
      int columnCount = view.getColumnCount();
      int[] columnMax = new int[columnCount];
      for (int row = 0, rows = view.getRowCount(); row < rows; row++)
         for (int col = 0; col < columnCount; col++)
         {
            String value = view.getValueAt (row, col) + "";
            int width = value.length();
            if (width > columnMax[col])
               columnMax[col] =  width;
         }
            
      for (int col = 0; col < columnCount; col++)
         columnMax[col] += 2;
         
      PrintStream out = null;
      try
      {
         FileUtils.makeDir (file.getParent());
         FileOutputStream fos = new FileOutputStream (file); 
         out = new PrintStream (fos, true, FileUtils.UTF8); // support Unicode
         
         for (int col = 0; col < columnCount; col++)
            out.printf("%-" + columnMax[col] + "s", view.getColumnName (col));
         out.println();
         
         for (int row = 0, rows = view.getRowCount(); row < rows; row++)
         {
            for (int col = 0; col < columnCount; col++)
            {
               Object value = view.getValueAt (row, col);
               out.printf ("%-" + columnMax[col] + "s", value);
            }
            out.println();
         }
         
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
