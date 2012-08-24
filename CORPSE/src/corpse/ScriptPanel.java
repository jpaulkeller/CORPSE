package corpse;

import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ScriptPanel extends TreePanel 
{
   private static final long serialVersionUID = 1L;

   private JTextArea textArea = new JTextArea();
   
   public ScriptPanel (final CORPSE app, final String dir, final String suffix)
   {
      super (app, dir, suffix);
      resolved.add (new JScrollPane (textArea));
   }
   
   @Override
   protected void resolve()
   {
      File file = getSelectedFile();
      if (file != null)
      {
         Script script = new Script (file.getPath());
         String text = script.resolve();
         textArea.setText (text);
         resolved.validate();
      }
   }
   
   @Override
   protected void roll()
   {
      resolve();
   }
}
