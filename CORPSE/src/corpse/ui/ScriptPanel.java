package corpse.ui;

import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import corpse.CORPSE;
import corpse.Script;

public class ScriptPanel extends TreePanel 
{
   private static final long serialVersionUID = 1L;

   public ScriptPanel (final CORPSE app, final String dir, final String suffix)
   {
      super (app, dir, suffix);
   }
   
   @Override
   protected File getFile(final String name)
   {
      return Script.getScript(name).getFile();
   }

   @Override
   protected int getResolvedPosition()
   {
      return 0; // TODO
   }

   @Override
   protected void loadResolved(final String name, final int caret)
   {
      Script script = Script.getScript(name);
      if (script != null)
      {
         JPanel resolved = findResolved(name);
         if (resolved != null)
            resolved.removeAll();
         else
            resolved = makeResolved(name);
         
         JTextArea textArea = new JTextArea();
         textArea.setText (script.resolve());
         
         resolved.add (new JScrollPane (textArea));
         resolved.validate();
         
         tabs.setSelectedIndex(tabs.indexOfTab(name));
      }
   }

   @Override
   public void roll()
   {
      // resolve(); TODO
   }
}
