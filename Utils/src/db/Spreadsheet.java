package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import str.StringUtils;
import file.FileUtils;

// poi-3.5-beta5-20090219.jar
// poi-ooxml-3.5-beta5-20090219.jar

public final class Spreadsheet
{
   private static final Pattern NUMBER =
      Pattern.compile (" *(-?[0-9]+(?:[,0-9]+)(?:[.][0-9]+)?) *");
   private static final Pattern UPDATE_PATTERN = 
      Pattern.compile ("(.*) \\(updated [0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{6}\\)");
   private static final SimpleDateFormat YMDHMS =
      new SimpleDateFormat ("yyyy-MM-dd hhmmss");
    
   private static final Pattern ID_PATTERN = Pattern.compile ("[0-9]{6}");
   private static final DecimalFormat ID_FORMAT = new DecimalFormat ("000000");
   
   private Spreadsheet() { }
   
   public static Workbook getWorkbook (final String path) throws IOException
   {
      Workbook wb = null;
      
      FileInputStream ssIn = null;
      try
      {
         ssIn = new FileInputStream (new File (path));
         wb = WorkbookFactory.create (ssIn);
      }
      catch (IOException x)
      {
         throw x;
      }
      catch (Exception x)
      {
         throw new IOException (x);
      }
      finally
      {
         FileUtils.close (ssIn);
      }
      
      return wb;
   }
   
   /** This is a temporary hack to work-around a bug in Apache POI (HSSF). */
   
   public static void evaluateFormulas (final JProgressBar progress,
                                        final Workbook wb)
   {
      progress.setString ("Evaluating Formulas");
      progress.setIndeterminate (false);

      CreationHelper creator = wb.getCreationHelper();
      FormulaEvaluator evaluator = creator.createFormulaEvaluator();

      Sheet sheet = wb.getSheetAt (0);
      int rows = sheet.getLastRowNum();
      for (int r = 0; r < rows; r++)
      {
         int percent = r * 100 / rows;
         progress.setValue (percent);
         
         Row row = sheet.getRow (r);
         if (row != null)
         {
            for (Iterator<Cell> cit = row.cellIterator(); cit.hasNext();)
            {
               Cell cell = cit.next();
               if (cell != null && cell.getCellType() == Cell.CELL_TYPE_FORMULA)
               {
                  String formula = cell.getCellFormula();
                  if (formula != null)
                  {
                     try
                     {
                        evaluator.evaluateFormulaCell (cell);
                        cell.setCellFormula (formula);
                     }
                     catch (Exception x)
                     {
                        String message = "Row: " + (r + 1) + 
                        " Col: " + (cell.getColumnIndex() + 1) +
                        "\n" + x.getMessage();
                        JOptionPane.showMessageDialog 
                           (null, message, "Formula Error", JOptionPane.WARNING_MESSAGE);
                     }
                  }
               }
            }
         }
      }
   }
 
   public static String getText (final Cell cell)
   {
      if (cell == null)
         return "";
      switch (cell.getCellType())
      {
      case Cell.CELL_TYPE_BOOLEAN:
         return cell.getBooleanCellValue() + "";
      case Cell.CELL_TYPE_FORMULA:
      case Cell.CELL_TYPE_NUMERIC:
         double d = cell.getNumericCellValue();
         return d == (int) d ? (int) d + "" : d + ""; // format as int
      default:
         return cell.toString();
      }
   }
   
   public static double getDouble (final Cell cell)
   {
      if (cell == null)
         return 0;
      
      Matcher m;
      switch (cell.getCellType())
      {
      case Cell.CELL_TYPE_BOOLEAN:
         return cell.getBooleanCellValue() ? 1 : 0;
      case Cell.CELL_TYPE_FORMULA:
      case Cell.CELL_TYPE_NUMERIC:
         return cell.getNumericCellValue();
      default:
         if ((m = NUMBER.matcher (cell.toString())).matches())
            return Double.parseDouble (m.group (1));
      }
      return 0;
   }
   
   public static int findColumn (final Sheet sheet, final int headerRow,
                                 final String header)
   {
      Row row = sheet.getRow (headerRow);
      for (Iterator<Cell> cit = row.cellIterator(); cit.hasNext();)
      {
         Cell cell = cit.next();
         if (header.equals (getText (cell).trim()))
            return cell.getColumnIndex();
      }
      
      System.out.println ("Spreadsheet.findColumn() did not find: " + header);
      return -1;
   }
   
   public static String getHeader (final Sheet sheet, final int headerRow,
                                   final int column)
   {
      Row row = sheet.getRow (headerRow);
      Cell cell = row.getCell (column);
      return getText (cell).trim();
   }
   
   public static String checkHeader (final Sheet sheet, final int headerRow,
                                     final int column, final String expectedName)
   {
      String warning = null;
      String header = getHeader (sheet, headerRow, column);
      if (!expectedName.equalsIgnoreCase (header))
         warning = "A column in the selected spreadsheet " +
         "does not contain the expected value:\n" +
         "Column #" + (column + 1) + " is '" + header +
         "' but is supposed to be: " + expectedName;
      return warning;
   }
   
   public static String getID (final Cell cell)
   {
      String obID = null;
      if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
      {
         int id = (int) cell.getNumericCellValue();
         obID = ID_FORMAT.format (id);
      }
      else
      {
         obID = getText (cell).trim();
         if (obID != null)
            obID = StringUtils.pad (obID, -6, '0'); // pad with leading zeros
      }

      if (obID == null || !ID_PATTERN.matcher (obID).matches())
         return null;

      return obID;
   }
   
   public static File getUpdateFile (final File file)
   {
      String name = FileUtils.getNameWithoutSuffix (file);
      Matcher m = UPDATE_PATTERN.matcher (name);
      if (m.matches())
         name = m.group (1); // strip off the date/time from previous run
      
      String dateTime = YMDHMS.format (new Date());
      String update = " (updated " + dateTime + ")";
      String path = file.getParent() + File.separator + name + update + ".xls";
      return new File (path);
   }
}
