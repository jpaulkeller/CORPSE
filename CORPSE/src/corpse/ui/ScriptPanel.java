package corpse.ui;

import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import corpse.CORPSE;
import corpse.Script;

public class ScriptPanel extends TreePanel
{
   private static final long serialVersionUID = 1L;
   private String text;
   
   public ScriptPanel(final CORPSE app, final String dir, final String suffix)
   {
      super(app, dir, suffix);
   }

   public String getText()
   {
      return text;
   }

   @Override
   protected File getFile(final String name)
   {
      return Script.getScript(name).getFile();
   }

   @Override
   protected int getResolvedPosition()
   {
      return getRawCaretPosition();
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

         text = script.resolve();
         if (text.contains("<html>"))
         {
            JTextPane htmlPane = new JTextPane();
            htmlPane.setBackground(null);
            htmlPane.setEditable(false);
            htmlPane.setEditorKit(new HTMLEditorKit());
            htmlPane.setText(text);
            htmlPane.setCaretPosition(0);
            resolved.add(new JScrollPane(htmlPane));
         }
         else
         {
            JTextArea textArea = new JTextArea();
            textArea.setText(text);
            resolved.add(new JScrollPane(textArea));
         }
         
         resolved.validate();

         tabs.setSelectedIndex(tabs.indexOfTab(name));
      }
   }

   @Override
   public void roll()
   {
      // scripts don't resolve to a single value
   }
}
