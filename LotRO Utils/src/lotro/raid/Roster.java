package lotro.raid;

import file.FileUtils;
import gui.ComponentTools;
import gui.form.ComboBoxItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;

import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.web.Dropbox;

public class Roster
{
   private Kinship kinship;
   private SignupModel candidates;
   private SignupModel backups;
   
   private JProgressBar progress;
   private ComboBoxItem combo;
   private JButton saveButton;
   private JPanel radioPanel;
   private String fileName;
   
   public Roster (final JProgressBar progress)
   {
      this.progress = progress;
      candidates = new SignupModel();
      backups = new SignupModel();
      
      saveButton = ComponentTools.makeButton
         ("Save", "icons/FloppyDisk.gif", false, new ButtonListener(), null);
   }
   
   public void setKinship (final Kinship kinship) // TBD
   {
      this.kinship = kinship;
      candidates.setKinship (kinship);
      backups.setKinship (kinship);
   }

   public String getName()
   {
      String name = "";
      if (fileName != null)
         name = FileUtils.getNameWithoutSuffix (new File (fileName));
      return name;
   }
   
   public SignupModel getCandidates()
   {
      return candidates;
   }

   public boolean isEmpty()
   {
      return candidates.isEmpty();
   }
   
   public int size()
   {
      return candidates.size();
   }

   public SignupModel getBackups()
   {
      return backups;
   }
   
   public JPanel makeRosterCombo()
   {
      String path = Dropbox.get().getPath ("/raids/Rosters.txt");
      List<String> rosterNames = FileUtils.getList (path, FileUtils.UTF8, true);
      rosterNames.add (0, "");
      combo = new ComboBoxItem ("Rosters", rosterNames);
      combo.setEditable (true);
      RosterListener listener = new RosterListener(); 
      combo.addActionListener (listener);
      combo.addValueChangeListener (listener);
      
      JPanel panel = new JPanel (new BorderLayout());
      panel.add (combo.getComponent(), BorderLayout.CENTER);
      panel.add (saveButton, BorderLayout.EAST);
      panel.setBorder (BorderFactory.createTitledBorder ("Rosters"));
      
      return panel;
   }
   
   public JPanel makeRosterPanel()
   {
      if (radioPanel == null)
      {
         radioPanel = new JPanel (new GridLayout (0, 1));
         radioPanel.setBorder (BorderFactory.createTitledBorder ("Rosters"));
      }
      else
         radioPanel.removeAll();
      
      String path = Dropbox.get().getPath ("/raids/Rosters.txt");
      List<String> rosterNames = FileUtils.getList (path, FileUtils.UTF8, true);
      if (!rosterNames.isEmpty())
      {
         ActionListener listener = new RosterListener();
         ButtonGroup buttons = new ButtonGroup();
         for (String roster : rosterNames)
            radioPanel.add (addButton (buttons, listener, roster));
      }
      else
         System.out.println ("Empty roster file: " + path);
      
      return radioPanel;
   }
   
   private JRadioButton addButton (final ButtonGroup buttons, 
                                   final ActionListener listener,
                                   final String url)
   {
      String label = FileUtils.getNameWithoutSuffix (new File (url));
      label = label.replace ("%20", " ");
      JRadioButton button = new JRadioButton (label);
      button.setFocusable (false);
      button.setToolTipText ("Replace your current Raid Group with this roster");
      button.setActionCommand (url);
      button.addActionListener (listener);
      buttons.add (button);
      return button;
   }

   private void loadRoster()
   {
      System.out.println ("Roster.loadRoster(): " + fileName); // TBD
      String path = Dropbox.get().getPath ("/raids/" + fileName);
      Map<Player, Signup> signups = Signup.loadFromFile (kinship, path);

      if (!signups.isEmpty())
      {
         candidates.clear();
         backups.clear();
         for (Signup signup : signups.values())
         {
            if (signup.isBackup())
               for (Character ch : signup.getCharacters())
                  backups.add (ch);
            else
               for (Character ch : signup.getCharacters())
                  candidates.add (ch);
         }
         // optimize.setEnabled (app.composition != null && !app.candidates.isEmpty());
         if (progress != null)
            progress.setString (candidates.size() + " characters selected");
      }
   }

   class RosterListener implements ActionListener, ValueChangeListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         if (e.getSource() instanceof JRadioButton)
            fileName = e.getActionCommand();
         else // combo box
            fileName = (String) combo.getValue();
         
         if (!fileName.equals (""))
            loadRoster();

         enableButton();
      }

      public void valueChanged (final ValueChangeEvent e)
      {
         enableButton();
      }
      
      private void enableButton()
      {
         saveButton.setEnabled (combo != null && combo.getValue() != null &&
                                !combo.getValue().equals ("") && 
                                !Dropbox.get().isRemote()); 
      }
   }
   
   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         String userFile = (String) combo.getValue();
         String path = Dropbox.get().getPath ("/raids/" + userFile);
         
         Collection<String> lines = new ArrayList<String>();
         // TBD: one line per signup?
         for (Signup signup : candidates)
            for (Character ch : signup.getCharacters())
               lines.add (ch.getName() + " " + ch.getScore());
         for (Signup signup : backups)
            for (Character ch : signup.getCharacters())
               lines.add (ch.getName() + " " + ch.getScore());
         
         FileUtils.writeList (lines, path, false);
         if (progress != null)
            progress.setString ("Saved as: " + path);

         // TBD: support delete roster
         
         // update the list of rosters
         path = Dropbox.get().getPath ("/raids/Rosters.txt");
         List<String> rosterNames = FileUtils.getList (path, FileUtils.UTF8, true);
         if (!rosterNames.contains (userFile))
         {
            rosterNames.add (userFile);
            Collections.sort (rosterNames);
            FileUtils.writeList (rosterNames, path, false);

            combo.getModel().addElement (userFile);
            makeRosterPanel();
         }
      }
   }
}
