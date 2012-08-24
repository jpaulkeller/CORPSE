package gui.comp;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import file.RegexFileFilter;

public class FileChooser extends JFileChooser
{
   private JTextField textField;

   public FileChooser (final String title, final String path)
   {
      super (new File (path != null ? path : "."));
      
      setDialogTitle (title);
      setFileSelectionMode (JFileChooser.FILES_ONLY);
      setDialogType (JFileChooser.OPEN_DIALOG);
   }
   
   public void setRegexFilter (final String regex,
                               final String description)
   {
      if (regex != null)
         addChoosableFileFilter (getRegexFilter (regex, description));
   }
   
   // setMultiSelectionEnabled (true);
   
   public void preselectMatchingFiles() // assumes MultiSelectionEnabled
   {
      FileFilter filter = getFileFilter();
      if (filter != null)
      {
         Collection<File> files = new ArrayList<File>();
         for (File f : getCurrentDirectory().listFiles())
            if (filter.accept (f))
               files.add (f);
         if (!files.isEmpty())
            setSelectedFiles (files.toArray (new File [files.size()]));
      }
   }
   
   /** Reads the entered text, instead of the selected file. */

   public File getEnteredFile()
   {
      String fileName = getEnteredText (this);
      if (fileName != null)
         return new File
            (getCurrentDirectory().getPath() + File.separator + fileName);
      return null;
   }

   public String getEnteredText()
   {
      return getEnteredText (this);
   }

   private String getEnteredText (final Component c)
   {
      JTextField field = getTextField (c);
      return field != null ? field.getText() : null;
   }

   // recursively search the component tree to find the JTextField

   private JTextField getTextField (final Component c)
   {
      if (textField != null)
         return textField;

      if (c instanceof JTextField)
      {
         textField = (JTextField) c; // cache it
         return textField;
      }

      if (c instanceof Container)
      {
         for (Component child : ((Container) c).getComponents())
         {
            JTextField field = getTextField (child);
            if (field != null)
               return field;
         }
      }

      return null;
   }

   private FileFilter getRegexFilter (final String regex, 
                                      final String description)
   {
      return new RegexFileFilter (regex, description);
   }
}
