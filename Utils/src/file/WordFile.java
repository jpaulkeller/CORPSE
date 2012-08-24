package file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

import gui.ComponentTools;
import gui.comp.FileChooser;

public class WordFile
{
   private File file;
   private HWPFDocument doc;
   
   public WordFile (final File file) throws IOException
   {
      this.file = file;
      FileInputStream is = null;
      try
      {
         if (file.exists())
         {
            is = new FileInputStream (file);
            doc = new HWPFDocument (is);
         }
      }
      finally
      {
         FileUtils.close (is);
      }
   }

   public File getFile()
   {
      return file;
   }
   
   public HWPFDocument getDocument()
   {
      return doc;
   }
   
   public String getText() throws IOException
   {
      WordExtractor wex = new WordExtractor (doc);
      return WordExtractor.stripFields (wex.getText());
   }
   
   public static String getText (final File file)
   {
      if (file != null && file.exists())
      {
         try
         {
            WordFile wf = new WordFile (file);
            return wf.getText();
         }
         catch (IOException x)
         {
            x.printStackTrace (System.err);
         }
      }
      return null;
   }
      
   public static void main (final String[] args) throws IOException
   {
      ComponentTools.setDefaults();
      
      String user = System.getProperty ("user.name");
      String dir = "C:/Documents and Settings/" + user + "/Desktop/";
      FileChooser fc = new FileChooser ("Select File", dir);
      fc.setRegexFilter (".+[.]doc", "Microsoft Word (*.doc) files");
      
      if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      {
         File file = fc.getSelectedFile();
         if (file != null)
         {
            WordFile wf = new WordFile (file);
            System.out.println (wf.getText());
         }
      }
   }
}
