package lotro.raid;

import gui.ComponentTools;
import gui.comp.DragDropList;
import gui.comp.ListOrderable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import lotro.models.Character;
import lotro.models.CharacterListModel;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.my.reports.FilterFactory;
import lotro.my.xml.KinshipXML;

public class SignupPanel extends JPanel
{
   private static final long serialVersionUID = 1L;

   private RaidOrganizer app;
   private CharacterListModel characters;
   private Roster roster;
   private ListOrderable<Signup> candidatePanel;
   private ListOrderable<Signup> backupPanel;
   private JList playerView;
   private JList charView;
   private JList candidateView;
   private JList backupView;
   
   public SignupPanel (final RaidOrganizer app,
                       final PlayerListModel players,
                       final CharacterListModel characters,
                       final Roster roster)
   {
      super (new BorderLayout());
      
      this.app = app;
      this.characters = characters;
      this.roster = roster;
      
      DataListener dataListener = new DataListener();
      roster.getCandidates().addListDataListener (dataListener);
      roster.getBackups().addListDataListener (dataListener);

      playerView = new DragDropList (players, DnDConstants.ACTION_COPY);
      charView = new DragDropList (characters, DnDConstants.ACTION_COPY_OR_MOVE);
      candidateView = new DragDropList (roster.getCandidates(), DnDConstants.ACTION_COPY_OR_MOVE);
      backupView = new DragDropList (roster.getBackups(), DnDConstants.ACTION_COPY_OR_MOVE);
      
      JScrollPane playerScroll = new JScrollPane (playerView);
      JPanel playerPanel = ComponentTools.getTitledPanel (playerScroll, "Players");
      
      JScrollPane charScroll = new JScrollPane (charView);
      JPanel charPanel = ComponentTools.getTitledPanel (charScroll, "Characters");
      charPanel.setPreferredSize (new Dimension (200, 300));
      
      candidatePanel = new ListOrderable<Signup> (candidateView, "Group Candidates");
      backupPanel = new ListOrderable<Signup> (backupView, "Backups");
      
      MouseListener mouseListener = new ClickListener();
      playerView.addMouseListener (mouseListener);
      charView.addMouseListener (mouseListener);
      candidateView.addMouseListener (mouseListener);
      backupView.addMouseListener (mouseListener);

      JPanel left = new JPanel (new BorderLayout());
      left.add (playerPanel, BorderLayout.NORTH);
      left.add (charPanel, BorderLayout.CENTER);
      
      JPanel center = new JPanel (new BorderLayout());
      center.add (candidatePanel, BorderLayout.CENTER);
      center.add (backupPanel, BorderLayout.SOUTH);
      
      JPanel rosterCombo = roster.makeRosterCombo();
      center.add (rosterCombo, BorderLayout.NORTH);

      add (left, BorderLayout.WEST);
      add (center, BorderLayout.CENTER);      
   }
   
   class DataListener implements ListDataListener
   {
      public void contentsChanged (final ListDataEvent e) { updateTitle (e); }
      public void intervalAdded   (final ListDataEvent e) { updateTitle (e); }
      public void intervalRemoved (final ListDataEvent e) { updateTitle (e); }
      
      public void updateTitle (final ListDataEvent e)
      {
         if (e.getSource() == roster.getCandidates())
            candidatePanel.setTitle ("Group Candidates (" + roster.getCandidates().size() + ")");
         else if (e.getSource() == roster.getBackups())
            backupPanel.setTitle ("Backups (" + roster.getBackups().size() + ")");
         
         // TBD: clear the raid panel, and disable the save button!
         if (app != null)
         {
            app.getTabs().setEnabledAt (2, !roster.getCandidates().isEmpty());
            app.getProgress().setString ("");
         }
      }
   }
   
   class ClickListener extends MouseAdapter
   {
      @Override
      public void mouseClicked (final MouseEvent e)
      {
         if (e.getClickCount() > 1) // support double-click
         {
            if (e.getSource() == playerView)
               roster.getCandidates().add ((Player) playerView.getSelectedValue());
            else if (e.getSource() == charView)
            {
               Character ch = (Character) charView.getSelectedValue();
               characters.remove (ch);
               roster.getCandidates().add (ch);
            }
            else if (e.getSource() == candidateView)
            {
               Signup signup = (Signup) candidateView.getSelectedValue();
               roster.getCandidates().remove (signup);
               roster.getBackups().add (signup);
            }
            else if (e.getSource() == backupView)
            {
               Signup signup = (Signup) backupView.getSelectedValue();
               roster.getBackups().remove (signup);
               for (Character ch : signup.getCharacters())
                  characters.add (ch);
            }
         }
      }
   }
   
   public static void main (final String[] args)
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (true);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      kinship.setFilter (FilterFactory.getLevelFilter (48));
      
      PlayerListModel players = new PlayerListModel(); 
      CharacterListModel characters = new CharacterListModel (kinship, true);
      
      for (Character ch : kinship.getCharacters().values())
      {
         players.add (ch.getPlayer());
         characters.add (ch);
      }
      
      Roster roster = new Roster (null);
      roster.setKinship (kinship);
      
      SignupPanel panel = new SignupPanel (null, players, characters, roster);      
      ComponentTools.open (panel, "SignupPanel");
   }   
}
