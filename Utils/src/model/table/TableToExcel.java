package model.table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import file.FileUtils;

public final class TableToExcel
{
   private TableToExcel() { }
   
   public static void export (final JTable view, final File file)
   {
      FileOutputStream ss = null;
      try
      {
         Workbook wb = new HSSFWorkbook(); // XSSFWorkbook();
         Sheet sheet = wb.createSheet();
         
         addHeader (view, wb, sheet);
         addRows (view, wb, sheet);
         
         ss = new FileOutputStream (file);
         wb.write (ss);
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
         FileUtils.close (ss);
      }
   }
      
   private static void addHeader (final JTable view, 
                                  final Workbook wb, 
                                  final Sheet sheet)
   {
      // create the style for header cells
      org.apache.poi.ss.usermodel.Font font = wb.createFont();
      font.setBoldweight (org.apache.poi.ss.usermodel.Font.BOLDWEIGHT_BOLD);
      CellStyle style = wb.createCellStyle();
      style.setAlignment (CellStyle.ALIGN_CENTER);
      style.setFillPattern (CellStyle.SOLID_FOREGROUND);
      style.setFillForegroundColor (new HSSFColor.YELLOW().getIndex());
      style.setFont (font);
      
      Row ssRow = sheet.createRow (0);
      
      CreationHelper creator = wb.getCreationHelper();
      int columnCount = view.getColumnCount();
      for (int col = 0; col < columnCount; col++)
      {
         Cell cell = ssRow.createCell (col, Cell.CELL_TYPE_STRING);
         cell.setCellValue (creator.createRichTextString (view.getColumnName (col)));
         cell.setCellStyle (style);
      }
      
      sheet.createFreezePane (0, 1); // freeze the header so it doesn't scroll
   }

   private static void addRows (final JTable view, 
                                final Workbook wb, 
                                final Sheet sheet)
   {
      CreationHelper creator = wb.getCreationHelper();
      int columnCount = view.getColumnCount();
      
      // create the style for date cells
      CellStyle dateStyle = wb.createCellStyle();
      dateStyle.setDataFormat ((short) 14); // m/d/y TBD: constant
      
      for (int row = 0, rows = view.getRowCount(); row < rows; row++)
      {
         Row ssRow = sheet.createRow (row + 1);
         for (int col = 0; col < columnCount; col++)
         {
            Object value = view.getValueAt (row, col);

            Cell cell = ssRow.createCell (col);
            if (value instanceof Number)
            {
               cell.setCellType (Cell.CELL_TYPE_NUMERIC);
               cell.setCellValue (((Number) value).doubleValue());
            }
            else if (value instanceof Date)
            {
               cell.setCellValue ((Date) value);
               cell.setCellStyle (dateStyle);
            }
            else
            {
               cell.setCellType (Cell.CELL_TYPE_STRING);
               if (value == null)
                  value = "";
               cell.setCellValue (creator.createRichTextString (value.toString()));
            }
         }
      }
   }
}
