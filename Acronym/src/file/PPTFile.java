package file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.apache.poi.hslf.extractor.PowerPointExtractor;

import gui.ComponentTools;
import gui.comp.FileChooser;

public class PPTFile
{
   private File file;
   
   public PPTFile (final File file)
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

      FileInputStream is = null;
      try
      {
         is = new FileInputStream (file);
         PowerPointExtractor extractor = new PowerPointExtractor (is);
         text = extractor.getText (true, true);
      }
      catch (IOException x)
      {
         x.printStackTrace (System.err);
      }
      finally
      {
         FileUtils.close (is);
      }
      
      return text;
   }
   
   public static String getText (final File file)
   {
      PPTFile ppt = new PPTFile (file);
      return ppt.getText();
   }   
      
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      
      String user = System.getProperty ("user.name");
      String dir = "C:/Documents and Settings/" + user + "/Desktop/";
      FileChooser fc = new FileChooser ("Select File", dir);
      fc.setRegexFilter (".+[.]ppt", "Powerpoint Presentation (*.ppt) files");
      
      if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      {
         File file = fc.getSelectedFile();
         if (file != null)
         {
            PPTFile ppt = new PPTFile (file);
            System.out.println (ppt.getText());
         }
      }
   }

}
