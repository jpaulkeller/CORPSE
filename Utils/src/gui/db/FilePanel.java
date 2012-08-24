package gui.db;

import gui.comp.FileChooser;
import gui.wizard.Wizard;
import gui.wizard.WizardPanel;

import java.awt.BorderLayout;
import java.io.File;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;

public class FilePanel extends WizardPanel
{
   private Wizard wiz;
   private Object callback;
   private Method onNextMethod;
   
   private FileChooser cf;
   private File file;

   public FilePanel (final Wizard wiz, 
                     final Object callback,
                     final Method onNextMethod)
   {
      this.wiz = wiz;
      this.callback = callback;
      this.onNextMethod = onNextMethod;
      
      cf = new FileChooser ("TBD", "."); //  ("FormattedDataImporter", null, null, "f"); TBD
      // cf.addSuffix ("Comma-separated Values", "csv");
      cf.setControlButtonsAreShown (false); // no OK or CANCEL buttons

      setBorder (BorderFactory.createTitledBorder ("Select File To Import"));
      add (cf, BorderLayout.CENTER);
   }
   
   @Override
   public void onEntry()
   {
      wiz.enablePrev (true);
      wiz.enableNext (true);         
   }

   @Override
   public void onNext()
   {
      setFile();
      if (onNextMethod != null)
         try
         {
            onNextMethod.invoke (callback);
         }
         catch (Exception x)
         {
            x.printStackTrace();
         }
   }

   private void setFile()
   {
      File prev = file;
      File curr = cf.getEnteredFile();

      if (curr != null && curr.exists() && curr.isFile())
      {
         // cf.rememberPath (curr.getPath()); // persist
         if (!curr.equals (prev))
            file = curr;
      }
   }
   
   public File getFile()
   {
      return file;
   }
}
