package corpse.ui;

import gui.comp.TipComboBox;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class Prompt // for Script
{
   private TipComboBox box;
   private JDialog dialog;
   
   public Prompt(final JFrame owner, final String title, final String[] options, final String defaultValue, final boolean editable)
   {
      box = new TipComboBox(options);
      box.setEditable(editable);
      
      dialog = new JDialog(owner, title, true);
      dialog.add(box);
      // TODO buttons
      dialog.pack();
      dialog.setVisible(true);
   }
}
