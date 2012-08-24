package model.table;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFileChooser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import db.Model;
import file.FileUtils;
import file.Spreadsheet;
import gui.ComponentTools;
import gui.comp.FileChooser;
import gui.db.TableView;

public class ExcelTable extends Model
{
   private File file;
   private Workbook wb;
   private int firstRow;
   
   // maps column index to a Date-flag
   private Map<Integer, Boolean> isDate = new HashMap<Integer, Boolean>();
   
   public ExcelTable (final File file)
   {
      super (FileUtils.getNameWithoutSuffix (file));
      this.file = file;
   }
   
   public void setFirstRow (final int firstRow)
   {
      this.firstRow = firstRow;
   }

   /**
    * Attempts to load the spreadsheet file contents into the model.
    * This method will throw an IOException (RecordFormatException) if the file
    * is encrypted (password-protected). 
    */
   public void load() throws IOException
   {
      if (file != null && file.exists())
      {
         wb = Spreadsheet.getWorkbook (file.getPath());
         if (wb != null && wb.getNumberOfSheets() > 0)
         {
            Sheet sheet = wb.getSheetAt (0);
            addColumns (sheet);
            loadSheet (sheet);
         }
      }
   }

   private void addColumns (final Sheet sheet)
   {
      Row ssRow = sheet.getRow (firstRow);
      for (int c = 0; c < ssRow.getLastCellNum(); c++)
      {
         Cell cell = ssRow.getCell (c);
         if (cell != null)
            
         {
            String header = Spreadsheet.getText (cell);
            addColumn (header);
            isDate.put (c, header.toUpperCase().contains ("DATE"));
         }
      }
   }
   
   private void loadSheet (final Sheet sheet)
   {
      int rows = sheet.getLastRowNum();
      for (int r = firstRow + 1; r < rows; r++)
      {
         Row ssRow = sheet.getRow (r);
         if (ssRow != null)
         {
            Vector<Object> dbRow = new Vector<Object>();
            for (int c = 0; c < ssRow.getLastCellNum(); c++)
               dbRow.add (getValue (ssRow.getCell (c))); // TBD: check for null?
            addRow (dbRow);
         }
      }
   }

   private Object getValue (final Cell cell)
   {
      if (cell == null)
         return "";
      
      switch (cell.getCellType())
      {
      case Cell.CELL_TYPE_BOOLEAN:
         return cell.getBooleanCellValue();
      case Cell.CELL_TYPE_FORMULA:
      case Cell.CELL_TYPE_NUMERIC:
         if (isDate.get (cell.getColumnIndex()))
            return cell.getDateCellValue();
         Double value = cell.getNumericCellValue();
         if (value.doubleValue() == (int) value.doubleValue())
            return Integer.valueOf (value.intValue());
         return value;
      default:
         return cell.toString();
      }
   }

   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      String dir = System.getProperty ("user.home") + "/Desktop";
      FileChooser fc = new FileChooser ("Select Spreadsheet", dir);
      fc.setRegexFilter (".+[.]xlsx?", "Spreadsheets (*.xls, *.xlsx) files");
      
      if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      {
         File file = fc.getSelectedFile();
         if (file != null)
         {      
            ExcelTable model = new ExcelTable (file);
            model.setFirstRow (0);
            try
            {
               model.load();
               TableView.show (model);
            }
            catch (IOException x)
            {
               x.printStackTrace();
            }
         }
      }
   }
}
