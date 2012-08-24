package file;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import gui.ComponentTools;
import gui.comp.FileChooser;

import org.faceless.pdf2.PDF;
import org.faceless.pdf2.PDFParser;
import org.faceless.pdf2.PDFReader;
import org.faceless.pdf2.PageExtractor;

// bfopdf-2.11.3.jar

public class PDFFile
{
   private File file;
   private PDF pdf;
   
   public PDFFile (final File file) throws IOException
   {
      this.file = file;
      if (file.exists())
         pdf = new PDF (new PDFReader (file));
   }

   public File getFile()
   {
      return file;
   }
   
   public String getText()
   {
      String text = null;
      
      if (pdf != null)
      {
         StringBuilder sb = new StringBuilder();
         PDFParser parser = new PDFParser (pdf);
         int pageCount = pdf.getNumberOfPages();
         for (int p = 0; p < pageCount; p++)
         {
            PageExtractor extractor = parser.getPageExtractor (p);
            sb.append (extractor.getTextAsStringBuffer());
         }
         text = sb.toString();
      }
      
      return text;
   }
   
   public static String getText (final File file)
   {
      try
      {
         PDFFile pdf = new PDFFile (file);
         return pdf.getText();
      }
      catch (IOException x)
      {
         x.printStackTrace (System.err);
      }
      return null;
   }   
      
   public static void main (final String[] args) throws IOException
   {
      ComponentTools.setDefaults();
      
      String user = System.getProperty ("user.name");
      String dir = "C:/Documents and Settings/" + user + "/Desktop/";
      FileChooser fc = new FileChooser ("Select File", dir);
      fc.setRegexFilter (".+[.]pdf", "Portable Document Format (*.pdf) files");
      
      if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      {
         File file = fc.getSelectedFile();
         if (file != null)
         {
            PDFFile pdf = new PDFFile (file);
            System.out.println (pdf.getText());
         }
      }
   }
}
