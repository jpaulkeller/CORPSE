package model.table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfWriter2;

import file.FileUtils;

// iText-2.1.5.jar

public final class TableToRTF
{
   private TableToRTF() { }
   
   public static void export (final JTable view, final File file)
   {
      PrintStream out = null;
      try
      {
         FileUtils.makeDir (file.getParent());
         
         Document document = new Document();
         RtfWriter2.getInstance (document, new FileOutputStream (file));
         document.open();

         try
         {
            int colCount = view.getColumnCount();
            Table table = new Table (colCount);
         
            for (int row = 0, rows = view.getRowCount(); row < rows; row++)
               for (int col = 0; col < colCount; col++)
               {
                  Object value = view.getValueAt (row, col);
                  String s = value != null ? value.toString() : "";
                  table.addCell (new Cell (new Paragraph (s)));
               }
            document.add (table);
         }
         catch (DocumentException x)
         {
            x.printStackTrace();
         }
         document.close();
         
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
