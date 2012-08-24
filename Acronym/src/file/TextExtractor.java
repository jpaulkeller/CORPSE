package file;

import gui.form.FileItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;
import gui.form.valid.FileValidator;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import state.StateModel;
import utils.Options;
import web.ReadURL;

public class TextExtractor
{
   private FileItem item;
   private Options options;
   private StateModel stateModel;
   private String state;
   
   public TextExtractor()
   {
      item = new FileItem ("Document", null, 50, true);
      item.setToolTipText ("Select or enter a document or URL");

      FileValidator validator = new FileValidator (true); // must exist
      validator.setAllowHTTP (true);
      validator.setMode (JFileChooser.FILES_ONLY);
      item.setValidator (validator);

      item.setMode (JFileChooser.FILES_ONLY);
      
      item.addFilter ("Text files", "txt");
      item.addFilter ("Rich Text Format files", "rtf");
      // item.addFilter ("Portable Document Format files", "pdf");
      item.addFilter ("Powerpoint Presentations", "ppt");
      item.addFilter ("MS-Word documents", "doc");

      item.addValueChangeListener (new FileListener());
   }

   public void setOptions (final Options options)
   {
      this.options = options;
      if (options != null)
      {
         File f = options.getFile ("Document");
         if (f != null)
         {
            item.setDefaultDir (f.isDirectory() ? f.getPath() : f.getParent());
            item.setValue (f);
         }
      }
   }
   
   public void setState (final StateModel model, final String docValidState)
   {
      this.stateModel = model;
      this.state = docValidState;
   }
   
   public File getFile()
   {
      return item.getFile();
   }
   
   public boolean isValid()
   {
      return item.isValid();
   }
   
   public JComponent getComponent()
   {
      return item.getComponent();
   }
   
   public JPanel getPanel()
   {
      return item.getTitledPanel();
   }
   
   private class FileListener implements ValueChangeListener
   {
      public void valueChanged (final ValueChangeEvent e)
      {
         if (item.isValid())
         {
            File f = item.getFile();
            item.setDefaultDir (f.isDirectory() ? f.getPath() : f.getParent());
            if (options != null)
            {
               options.put ("Document", f.getPath());
               options.write();
            }
         }
         if (stateModel != null)
            stateModel.updateState (state, item.isValid());
      }
   }
   
   public String getText()
   {
      String text = null;

      String path = getFile().getPath();
      if (path.toLowerCase().startsWith ("http:"))
      {
         // hack to convert File representation to simple text URL
         path = path.replaceAll ("\\\\", "/");
         path = path.replaceFirst ("/", "//");
         text = ReadURL.capture (path).toString();
         text = text.replaceAll ("<[^>]+>", " "); // strip HTML tags
         text = text.replaceAll ("&nbsp;", " ");
         text = text.replaceAll ("\\s*\\n\\s*", "\n"); // trim redundant new lines
         text = text.replaceAll ("  +", " "); // trim redundant white space
      }
      
      else if (getFile().exists())
      {
         String suffix = FileUtils.getSuffix (getFile());
         if ("doc".equalsIgnoreCase (suffix))
            text = WordFile.getText (getFile());
         /*
         else if ("pdf".equalsIgnoreCase (suffix))
            text = PDFFile.getText (getFile());
         */
         else if ("ppt".equalsIgnoreCase (suffix))
            text = PPTFile.getText (getFile());
         else if ("rtf".equalsIgnoreCase (suffix))
            text = RTFFile.getText (getFile());
         else
            text = FileUtils.getText (getFile());
      }
      
      return text;
   }
}
