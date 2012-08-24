package gui.form;

import gui.comp.FileChooser;
import gui.form.valid.FileValidator;
import gui.form.valid.StatusListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import utils.ImageTools;

/**
 * FileItem objects provide a friendly interface for selecting (or
 * entering) a file, for use in a form.
 */
public class FileItem extends TextItem implements ActionListener
{
   private static final long serialVersionUID = 2;

   // borders used to indicate file existence
   private static final Border BEVEL = BorderFactory.createLoweredBevelBorder();
   // private static final Border GREEN = BorderFactory.createLineBorder (Color.GREEN, 2);
   // private static final Border FOUND = BorderFactory.createCompoundBorder (Bevel, Green); 
   private static final Border FOUND = BEVEL;
   private static final Border YELLOW = BorderFactory.createLineBorder (Color.YELLOW, 2);
   private static final Border MISSING = BorderFactory.createCompoundBorder (BEVEL, YELLOW); 
   
   private JPanel panel;                // main component
   private FileChooser fc;
   private JButton button;
   private String regex;
   private String description;
   private String defaultDir;
   
   public static FileItem make (final String label, final File f,
                                final Object listener, final String... suffixes)
   {
      return make (label, 50, f, JFileChooser.FILES_ONLY, listener, suffixes);
   }
   
   public static FileItem make (final String label, final int columns, final File f, 
                                final int mode, final Object listener,
                                final String... suffixes)
   {
      FileItem item = new FileItem (label, null, columns, true);

      if (f != null)
      {
         item.setDefaultDir (f.isDirectory() ? f.getPath() : f.getParent());
         item.setValue (f);
      }

      FileValidator validator = new FileValidator (true); // must exist
      validator.setMode (mode);
      if (suffixes != null)
         validator.setSuffixes (suffixes);
      item.setValidator (validator);

      item.setMode (mode);
      if (suffixes != null && suffixes.length > 0)
      {
         StringBuilder sb = new StringBuilder (label + "s (" + suffixes[0]);
         for (int i = 1; i < suffixes.length; i++)
            sb.append (", " + suffixes[i]);
         item.addFilter (sb + ")", suffixes);
      }

      if (listener instanceof ValueChangeListener)
         item.addValueChangeListener ((ValueChangeListener) listener);
      if (listener instanceof StatusListener)
         item.addStatusListener ((StatusListener) listener);

      return item;
   }
   
   public FileItem (final String label, final File file, final int columns)
   {
      this (label, file, columns, false);
   }

   public FileItem (final String label, final File file, 
                    final int columns, final boolean iconTrailing)
   {
      super (columns);

      Icon icon = ImageTools.getIcon ("icons/FolderDocument.gif"); 
      button = new JButton (icon);
      button.setFocusable (false);
      button.setPreferredSize (new Dimension (24, 24));
      button.addActionListener (new MenuButtonListener());

      panel = new JPanel (new BorderLayout());
      panel.add (button, iconTrailing ? BorderLayout.EAST : BorderLayout.WEST);
      panel.add (super.getComponent(), BorderLayout.CENTER);

      if (file == null || file.isAbsolute())
         setInitialValue (file);
      else
         setInitialValue (new File ("." + File.separator + file.getName()));

      setLabel (label);
   }

   /** Returns an object of type File, or null. */
   @Override
   public Object getValue()
   {
      String txt = (String) super.getValue();
      return txt != null && !txt.equals ("") ? new File (txt) : null;
   }

   @Override
   public void setValue (final Object value)
   {
      if (value == null || value.equals (""))
         super.setValue (null);
      else if (value instanceof File)
         super.setValue (value);
      else
         super.setValue (new File (value.toString()));
   }

   @Override
   public void setInitialValue (final Object value)
   {
      if ((value == null) || (value.equals ("")))
         super.setInitialValue (null);
      else if (value instanceof File)
         super.setInitialValue (value);
      else
         super.setInitialValue (new File (value.toString()));
   }

   public void setMode (final int mode)
   {
      if (fc == null)
         makeChooser (null);
      fc.setFileSelectionMode (mode);
      if (getValidator() instanceof FileValidator)
         ((FileValidator) getValidator()).setMode (mode);
   }

   @Override
   public void setEnabled (final boolean enable)
   {
      button.setEnabled (enable);
      super.setEnabled (enable);
   }
   
   @Override
   public void setEditable (final boolean state)
   {
      button.setEnabled (state);
      super.setEditable (state);
   }

   public String getText()
   {
      return (String) super.getValue();
   }
   
   public File getFile()
   {
      return (File) getValue();
   }

   public String getParent()
   {
      File file = getFile();
      return file != null ? file.getParent() : null;
   }

   public String getPath()
   {
      File file = getFile();
      return file != null ? file.getPath() : null;
   }

   public String getName()
   {
      File file = getFile();
      return file != null ? file.getName() : null;
   }

   @Override
   public JComponent getComponent()
   {
      return panel;
   }

   @Override
   public void setToolTipText (final String tip)
   {
      super.setToolTipText (tip);
      button.setToolTipText (tip);
   }

   public void setIcon (final String name)
   {
      setIcon (ImageTools.getIcon (name));
   }

   public void setIcon (final Icon icon)
   {
      button.setIcon (icon);
   }
   
   public void setDefaultDir (final String defaultDir)
   {
      this.defaultDir = defaultDir;
      if (fc != null && defaultDir != null)
         fc.setCurrentDirectory (new File (defaultDir));
   }
   
   public String getDefaultDir()
   {
      return defaultDir;
   }
   
   public void setFilter (final String filterRegex,
                          final String filterDescription)
   {
      this.regex = filterRegex;
      this.description = filterDescription;
   }

   public void addFilter (final String filterDescription, final String... suffixes) 
   {
      if (fc == null)
         makeChooser (null);
      FileFilter filter = new FileNameExtensionFilter (filterDescription, suffixes);
      fc.addChoosableFileFilter (filter);
   }
   
   private void makeChooser (final File file)
   {
      String defaultName = null;
      if (file != null)
      {
         defaultName = file.getName();
         if (defaultDir == null)
            defaultDir = file.isDirectory() ? file.getPath() : file.getParent();
      }

      fc = new FileChooser ("Select File", defaultDir);
      fc.setRegexFilter (regex, description);
      if (defaultName != null)
         fc.setSelectedFile (new File (defaultName));
   }
   
   class MenuButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         chooseFile();
      }

      public void chooseFile()
      {
         File file = getFile();
         if (fc == null)
            makeChooser (file);

         if (fc.showOpenDialog (getComponent()) == JFileChooser.APPROVE_OPTION)
         {
            File choice = fc.getSelectedFile();
            if (choice != null)
            {
               setValue (choice);
               valueChanged();
            }
         }
      }
   }

   public boolean exists()
   {
      File file = getFile();
      if (file == null)
         return false;
      return file.exists();
   }

   /**
    * Override TextItem's implementation in order to show a third
    * state.  Light yellow is used to indicate a file which is valid,
    * but does not exist.
    */
   @Override
   protected void showChangeStatus()
   {
      super.showChangeStatus();
      
      File file = getFile();
      if (file == null)
         super.getComponent().setBorder (BEVEL);
      else if (!file.exists())
         super.getComponent().setBorder (MISSING);
      else
         super.getComponent().setBorder (FOUND);
   }

   public static void main (final String[] args)
   {
      FileItem item = new FileItem ("FileItem Test", new File ("."), 25);
      item.setValidator (new FileValidator (true));
      item.test();
   }
}
