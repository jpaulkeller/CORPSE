package lotro.gui;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

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
import lotro.models.CharacterWithDeeds;
import lotro.models.Klass;
import lotro.models.Race;

public class CharacterDialog extends Observable implements ActionListener
{
   private JDialog dialog;
   private JTextField playerField;
   private JTextField charField;
   private JComboBox raceField;
   private JComboBox classField;
   private JSpinner levelField;
   
   public CharacterDialog (final JFrame appFrame)
   {
      makeGUI (appFrame);
   }
   
   private void makeGUI (final JFrame appFrame)
   {
      playerField = new JTextField (12);
      charField = new JTextField (12);
      int level = Character.MAX_LEVEL;
      levelField = new JSpinner (new SpinnerNumberModel (level / 2, 1, level, 1));
      raceField = new JComboBox (Race.FREEPS.toArray());
      classField = new JComboBox (Klass.FREEPS.toArray());
      
      JButton add = new JButton ("Add");
      add.setToolTipText ("Add the entered character");
      add.addActionListener (this);
      
      JPanel p = new JPanel();
      p.add (new JLabel ("Player"));
      p.add (playerField);
      p.add (new JLabel ("Name"));
      p.add (charField);
      p.add (new JLabel ("Race"));
      p.add (raceField);
      p.add (new JLabel ("Class"));
      p.add (classField);
      p.add (new JLabel ("Level"));
      p.add (levelField);
      
      JPanel buttons = new JPanel();
      buttons.add (add);
      
      dialog = new JDialog (appFrame, "Character Creator");
      dialog.add (p, BorderLayout.CENTER);
      dialog.add (buttons, BorderLayout.SOUTH);
      dialog.pack();
      ComponentTools.centerComponent (dialog);
   }
   
   public void setVisible (final boolean visible)
   {
      dialog.setVisible (visible);
   }
   
   public void actionPerformed (final ActionEvent e)
   {
      String name = charField.getText();
      if (name != null && !name.trim().equals (""))
      {
         String player = playerField.getText();
         Race race = (Race) raceField.getSelectedItem();
         Klass klass = (Klass) classField.getSelectedItem();
         int level = (Integer) levelField.getValue();
         CharacterWithDeeds ch = new CharacterWithDeeds (player, name, race, klass, level);
         
         setChanged();
         notifyObservers (ch); // calls update() then addCharacter()
         clearChanged();
         
         charField.setText ("");
      }
   }
   
   public static void main (final String[] args)
   {
      CharacterDialog dialog = new CharacterDialog (new JFrame());
      dialog.setVisible (true);
   }
}
