package file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfWriter2;

import gui.ComponentTools;
import gui.comp.FileChooser;

public class RTFFile
{
   private File file;
   private com.lowagie.text.Document document;
   private Table table;

   public RTFFile (final String filename) throws IOException
   {
      document = new com.lowagie.text.Document();
      RtfWriter2.getInstance (document, new FileOutputStream (filename));
      document.open();
   }
   
   public RTFFile (final File file)
   {
      this.file = file;
   }

   public File getFile()
   {
      return file;
   }
   
   public String getText()
   {
      String text = null;
      
      if (file.exists())
      {
         try
         {
            text = RtfToText.getPlainText (file);
         }
         catch (IOException x)
         {
            x.printStackTrace (System.err);
         }
      }
      
      return text;
   }

   public static String getText (final File file)
   {
      return new RTFFile (file).getText();
   }   

   // methods to create an RTF file
   
   public void createTable (final int numColumns)
   {
      try
      {
         table = new Table (numColumns);
      }
      catch (BadElementException x)
      {
         x.printStackTrace();
      }
   }

   public void addRow (final String[] contents)
   {
      for (String s : contents)
      {
         Paragraph p = new Paragraph (s);
         Cell c = null;
         try
         {
            c = new Cell (p);
         }
         catch (BadElementException x)
         {
            x.printStackTrace();
         }
         table.addCell (c);
      }
   }

   public void close()
   {
      try
      {
         document.add (table);
      }
      catch (DocumentException x)
      {
         x.printStackTrace();
      }
      document.close();
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      
      String user = System.getProperty ("user.name");
      String dir = "C:/Documents and Settings/" + user + "/Desktop/";
      FileChooser fc = new FileChooser ("Select File", dir);
      fc.setRegexFilter (".+[.]rtf", "Rich Text Format (*.rtf) files");
      
      if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      {
         File file = fc.getSelectedFile();
         if (file != null)
         {
            RTFFile rtf = new RTFFile (file);
            System.out.println (rtf.getText());
         }
      }
   }
}
