package lotro.gui;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Observable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import lotro.models.Character;
import lotro.models.Deed;

public class DeedDialog extends Observable implements ActionListener
{
   private JDialog dialog;
   private JTextField nameField;
   private JComboBox regionField;
   private JComboBox typeField;
   private JComboBox traitField;
   private JSpinner levelField;
   
   private SortedSet<String> regions = new TreeSet<String>();
   private SortedSet<String> types = new TreeSet<String>();
   private SortedSet<String> traits = new TreeSet<String>();
   
   public DeedDialog (final JFrame appFrame)
   {
      Collection<Deed> deeds = Deed.read (Deed.FILE).values();
      for (Deed deed : deeds)
      {
         regions.add (deed.getRegion());
         types.add (deed.getType());
         traits.add (deed.getTrait());
      }
         
      makeGUI (appFrame);
   }
   
   private void makeGUI (final JFrame appFrame)
   {
      nameField = new JTextField (12);
      regionField = makeCombo (regions);
      typeField   = makeCombo (types);
      traitField  = makeCombo (traits);
      levelField = new JSpinner (new SpinnerNumberModel (0, 0, Character.MAX_LEVEL, 1));

      JButton add = new JButton ("Add");
      add.setToolTipText ("Add the entered deed");
      add.addActionListener (this);
      
      JPanel p = new JPanel();
      p.add (new JLabel ("Name"));
      p.add (nameField);
      p.add (new JLabel ("Region"));
      p.add (regionField);
      p.add (new JLabel ("Type"));
      p.add (typeField);
      p.add (new JLabel ("Trait"));
      p.add (traitField);
      p.add (new JLabel ("Level"));
      p.add (levelField);
      
      JPanel buttons = new JPanel();
      buttons.add (add);
      
      dialog = new JDialog (appFrame, "Deed Creator");
      dialog.add (p, BorderLayout.CENTER);
      dialog.add (buttons, BorderLayout.SOUTH);
      dialog.pack();
      ComponentTools.centerComponent (dialog);
   }
   
   private JComboBox makeCombo (final Set<String> values)
   {
      JComboBox combo = new JComboBox (values.toArray());
      combo.setEditable (true);
      return combo;
   }
   
   public void setVisible (final boolean visible)
   {
      dialog.setVisible (visible);
   }
   
   public void actionPerformed (final ActionEvent e)
   {
      String name = nameField.getText();
      if (name != null && !name.trim().equals (""))
      {
         String region = (String) regionField.getSelectedItem();
         String type = (String) typeField.getSelectedItem();
         String trait = (String) traitField.getSelectedItem();
         int level = (Integer) levelField.getValue();
         Deed deed = new Deed (region, name, type, trait, level);
         
         setChanged();
         notifyObservers (deed); // calls update then addDeed()
         clearChanged();
         
         nameField.setText ("");
      }
   }
   
   public static void main (final String[] args)
   {
      DeedDialog dialog = new DeedDialog (new JFrame());
      dialog.setVisible (true);
   }
}
