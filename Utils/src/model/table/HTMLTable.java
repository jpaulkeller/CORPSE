package model.table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import db.Model;
import file.FileUtils;
import gui.ComponentTools;
import gui.db.TableView;
import gui.form.FileItem;

public class HTMLTable extends Model
{
   private static final Pattern TABLE =
      Pattern.compile ("<table[^>]*>(.*?)</table>",
                       Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
   private static final Pattern ROW =
      Pattern.compile ("<tr[^>]*>(.*?)</tr>",
                       Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
   private static final Pattern HEADER =
      Pattern.compile ("<th[^>]*>(.*?)</th>",
                       Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
   private static final Pattern DATA =
      Pattern.compile ("<td[^>]*>(.*?)</td>",
                       Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
   private static final Pattern TAG =
      Pattern.compile ("<([a-z]+)[^>]*>(.*?)</\\1>",
                       Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
   
   private static final Pattern COLSPAN =
      Pattern.compile ("colspan=[\"']?([0-9]+)\\b", Pattern.CASE_INSENSITIVE);
      
   private static final Pattern NUMERIC = Pattern.compile ("-?[0-9,]+");
   
   private String address;
   
   public HTMLTable (final String name, final String address)
   {
      super (name);
      if (name == null)
         setName (FileUtils.getNameWithoutSuffix (new File (address)));
      this.address = address;
   }
   
   public void scrape()
   {
      String htmlTable = extractTable();
      if (htmlTable != null)
         createModel (htmlTable);
   }

   private String extractTable()
   {
      String htmlTable = null;
      
      BufferedReader buf = null;
      try
      {
         InputStream is;
         if (address.startsWith ("http://"))
            is = new URL (address).openStream();
         else
            is = new FileInputStream (address);
         InputStreamReader isr = new InputStreamReader (is, "UTF8");
         buf = new BufferedReader (isr);

         StringBuilder text = new StringBuilder();
         String line;
         int lineNo = 1;
         int tableOpen = 0, tableClose = 0;
         
         while ((line = buf.readLine()) != null && htmlTable == null)
         {
            if (tableOpen == 0 && line.toLowerCase().contains ("<table"))
               tableOpen = lineNo;
            if (tableOpen > 0)
            {
               text.append (line + "\n");
               if (tableClose == 0 && line.toLowerCase().contains ("</table>"))
                  tableClose = lineNo;
               if (tableClose > 0)
               {
                  Matcher matcher = TABLE.matcher (text);
                  if (matcher.find())
                  {
                     System.out.println ("Found TABLE on lines: " + tableOpen + 
                                         " to " + lineNo);
                     htmlTable = matcher.group (1);
                  }
               }
            }
            lineNo++;
         }
      }
      catch (MalformedURLException x)
      {
         System.err.println (x);
      }
      catch (IOException x)
      {
         System.err.println (x);
      }
      finally
      {
         FileUtils.close (buf);
      }
      
      return htmlTable;
   }
   
   private void createModel (final String htmlTable)
   {
      Matcher matcher = ROW.matcher (htmlTable);
      if (matcher.find())
      {
         String htmlRow = matcher.group (1);
         matcher = HEADER.matcher (htmlRow);
         while (matcher.find())
            addColumn (matcher.group (1));
         
         if (getColumnCount() == 0) // try again using the first data row
         {
            matcher = DATA.matcher (htmlRow);
            while (matcher.find())
            {
               Matcher spanM = COLSPAN.matcher (matcher.group (0));
               int count = spanM.find() ? Integer.parseInt (spanM.group (1)) : 1;
               for (int col = 0; col < count; col++)
                  addColumn ("Field " + (getColumnCount() + 1));
            }
         }
      }
      
      if (getColumnCount() > 0)
         populateModel (htmlTable);
      else
         System.out.println ("No table header found!");
   }
   
   private void populateModel (final String htmlTable)
   {
      List<Object> rowData = new ArrayList<Object>();
      
      Matcher rowMatcher = ROW.matcher (htmlTable);
      while (rowMatcher.find())
      {
         String htmlRow = rowMatcher.group (1);
         Matcher dataMatcher = DATA.matcher (htmlRow);
         while (dataMatcher.find())
         {
            String value = extractValue (dataMatcher.group (1));
            rowData.add (formatValue (value));
         }
         if (rowData.size() == getColumnCount())
            addRow (rowData.toArray());
         rowData.clear();
      }
   }

   // recursively strip HTML tags
   
   private String extractValue (final String html)
   {
      String value = html;
      Matcher tagMatcher = TAG.matcher (html);
      while (tagMatcher.find())
         value = extractValue (tagMatcher.group (2));
      return value;
   }
   
   private Object formatValue (final String value)
   {
      Object data = value.trim();
      if (data.equals ("&nbsp;"))
         data = "";
      
      Matcher m = NUMERIC.matcher (data.toString());
      if (m.matches())
         data = Integer.parseInt (data.toString().replace (",", ""));
      
      return data;
   }
      
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      String title = "Select File or enter URL";
      FileItem fi = new FileItem (title, null, 50);

      int result = JOptionPane.showConfirmDialog 
         (null, fi.getTitledPanel(), "HTMLTable", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION)
      {
         HTMLTable model = new HTMLTable (null, fi.getPath());
         model.scrape();
         TableView.show (model);
      }
   }
}
