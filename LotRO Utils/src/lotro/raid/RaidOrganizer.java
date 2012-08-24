package lotro.raid;

import gui.ComponentTools;
import gui.editors.EnumEditor;
import gui.editors.RangeEditor;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lotro.models.Character;
import lotro.models.CharacterListModel;
import lotro.models.CharacterModel;
import lotro.models.Kinship;
import lotro.models.Klass;
import lotro.models.Race;
import lotro.my.reports.FilterFactory;
import lotro.my.xml.KinshipXML;

import org.jdesktop.swingx.JXTable;

import utils.ImageTools;
import utils.Utils;

// TBD:
// create interactive GUI for moving characters around, show the score as it changes

public final class RaidOrganizer
{
   private static final long serialVersionUID = 1L;
   
   private JTabbedPane tabs;
   private JPanel mainPanel;
   private JProgressBar progress;

   private Kinship kinship;
   private CharacterModel model;
   private PlayerListModel players;
   private CharacterListModel characters;
   private Roster roster;
   
   private RaidOrganizer (final Kinship kinship)
   {
      this.kinship = kinship;
   }

   public JTabbedPane getTabs()
   {
      return tabs;
   }

   public JProgressBar getProgress()
   {
      return progress;
   }

   public SignupModel getCandidates()
   {
      return roster.getCandidates();
   }

   public SignupModel getBackups()
   {
      return roster.getBackups();
   }

   public void init()
   {
      progress = new JProgressBar (0, 100);
      progress.setStringPainted (true);
      
      tabs = new JTabbedPane();
      ImageIcon icon = ImageTools.getIcon ("icons/Character.gif");
      tabs.addTab ("Characters", icon, makeCharacterPanel());
      icon = ImageTools.getIcon ("icons/BookEdit.gif");
      tabs.addTab ("Sign-ups", icon, makeSignupPanel());
      icon = ImageTools.getIcon ("icons/Offense.gif");
      tabs.addTab ("Raid Group", icon, new RaidPanel (this, roster));
      tabs.setEnabledAt (1, false);
      tabs.addChangeListener (new TabListener());
      tabs.setSelectedIndex (2);
      
      mainPanel = new JPanel (new BorderLayout());
      mainPanel.add (tabs, BorderLayout.CENTER);
      mainPanel.add (progress, BorderLayout.SOUTH);
   }

   private JPanel makeCharacterPanel()
   {
      JXTable tbl = buildCharacterModel();
      JScrollPane scroll = new JScrollPane (tbl);
      scroll.setBorder (Utils.BORDER);
      
      JPanel panel = new JPanel (new BorderLayout()); 
      panel.add (scroll, BorderLayout.CENTER);
      return panel;
   }
   
   private JXTable buildCharacterModel()
   {
      model = new CharacterModel();
      
      JXTable tbl = new JXTable (model.getTable());
      tbl.getTableHeader().setReorderingAllowed (false);
      tbl.setColumnControlVisible (true);
      tbl.setPreferredScrollableViewportSize (new Dimension (800, 600));
      tbl.setDefaultEditor (Race.class, new EnumEditor<Race> (Race.FREEPS));
      tbl.setDefaultEditor (Klass.class, new EnumEditor<Klass> (Klass.FREEPS));
      tbl.setDefaultEditor (Integer.class, new RangeEditor (1, Character.MAX_LEVEL, 1));
      tbl.packAll();
      return tbl;
   }
   
   private JPanel makeSignupPanel()
   {
      players = new PlayerListModel();
      characters = new CharacterListModel (kinship, false);
      roster = new Roster (getProgress());
      roster.setKinship (kinship);
      
      return new SignupPanel (this, players, characters, roster);
   }
   
   public void open (final String version)
   {
      ComponentTools.open ("The Palantiri Raid Organizer (version " + version + ")",
                           null, mainPanel, null);
   }

   private void loadData()
   {
      progress.setString ("Loading characters...");
      progress.setIndeterminate (true);
      
      Thread thread = new Thread (new Runnable()
      {
         public void run()
         {
            for (Character ch : kinship.getCharacters().values())
            {
               characters.add (ch);
               model.addCharacter (ch);
               if (!players.contains (ch.getPlayer()))
                  players.add (ch.getPlayer());
            }
            
            SwingUtilities.invokeLater (new Runnable()
            {
               public void run()
               {
                  // populate in the Swing thread to force update
                  model.populate();

                  tabs.setEnabledAt (1, !characters.isEmpty());
                  progress.setString (characters.size() + " characters loaded");
                  progress.setIndeterminate (false);
               }
            });
         }
      });
      thread.start();
   }

   /* TBD
   private void saveCharacters()
   {
      if (!Character.PATH.startsWith ("http"))
      {         
         model.updateFromTable();
         model.write (Character.PATH);
         // TBD: update charList
         progress.setString ("Saved as: " + Character.PATH);
      }
      else
         progress.setString ("Save failed; unable to access to shared DropBox");
   }
   */
   
   class TabListener implements ChangeListener
   {
      public void stateChanged (final ChangeEvent e)
      {
         int tab = tabs.getSelectedIndex();
         if (tab == 0) // character panel
            progress.setString ("");
         else if (tab == 1) // sign-up panel
            progress.setString ("Drag-and-drop entries, or double-click to " +
            "cycle them through Characters, Candidates, and Backup lists");
         else if (tab == 2) // raid group panel
            progress.setString ("Select Raid to generate optimal group composition");
      }
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (false);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      kinship.setFilter (FilterFactory.getLevelFilter (48));
      
      RaidOrganizer app = new RaidOrganizer (kinship);
      app.init();
      app.open ("0.9.3 beta");
      app.loadData();
   }
}
