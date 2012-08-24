package lotro.raid;

import gui.ComponentTools;
import gui.comp.DragDropList;
import gui.comp.ListOrderable;
import gui.db.TableView;
import gui.form.NumericSpinner;
import gui.form.Range;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lotro.models.Character;
import lotro.models.CharacterTable;
import lotro.models.Kinship;
import lotro.my.xml.KinshipXML;

public final class GroupChecker
{
   private JPanel mainPanel;
   private JProgressBar progress;

   private Kinship kinship;
   private Roster roster;
   private CharacterHTML html;
   private CharacterTable model;
   
   private CharacterSelector charChooser;
   private NumericSpinner raidLevel;
   private NumericSpinner raidSize;
   private JTabbedPane tabs;
   private StatPanel statPanel;
   private JList candidateView;
   private ListOrderable<Signup> candidatePanel;
   
   private GroupChecker (final String world, final String kinName, 
                         final boolean includePlayers)
   {
      charChooser = new CharacterSelector (world, kinName, includePlayers); 
      charChooser.addListSelectionListener (new CharListener());
      roster = new Roster (progress);
      
      statPanel = new StatPanel();
      html = new CharacterHTML (statPanel);
      model = new CharacterTable();
      
      tabs = new JTabbedPane();
      tabs.setPreferredSize (new Dimension (750, 500));
      tabs.addTab ("Table", new TableView (model).getPanel (false, 600, 200));
      tabs.addTab ("HTML Table", new JScrollPane (html.getHtmlPane()));
      tabs.addTab ("Raw HTML", new JScrollPane (html.getRawPane()));
      tabs.addTab ("Stats", statPanel);
      tabs.addChangeListener (new TabListener());
      
      JPanel grid = new JPanel (new BorderLayout());
      grid.add (makeSignupPanel(), BorderLayout.NORTH);
      grid.add (tabs, BorderLayout.CENTER);
      
      progress = new JProgressBar (0, 100);
      progress.setStringPainted (true);

      mainPanel = new JPanel (new BorderLayout());
      mainPanel.add (grid, BorderLayout.CENTER);
      mainPanel.add (progress, BorderLayout.SOUTH);
   }
   
   private JPanel makeSignupPanel()
   {
      JPanel panel = new JPanel (new BorderLayout());
      
      raidLevel = new NumericSpinner ("Raid Level", 60);
      raidLevel.setRange (new Range (1, Character.MAX_LEVEL));
      
      raidSize = new NumericSpinner ("Raid Size", 12);
      raidSize.setRange (new Range (3, 24));

      DataListener dataListener = new DataListener();
      roster.getCandidates().addListDataListener (dataListener);
      roster.getCandidates().addListener (dataListener);
      
      candidateView = new DragDropList (roster.getCandidates(), DnDConstants.ACTION_COPY_OR_MOVE);
      candidatePanel = new ListOrderable<Signup> (candidateView, "Group Candidates");
      candidateView.addMouseListener (new ClickListener());
      
      JPanel rosterCombo = roster.makeRosterCombo();
      
      JPanel grid = new JPanel (new GridLayout (1, 0));
      grid.add (raidLevel.getTitledPanel());
      grid.add (raidSize.getTitledPanel());
      
      JPanel top = new JPanel (new GridLayout (1, 0));
      top.add (grid);
      top.add (rosterCombo);
      
      JPanel center = new JPanel (new BorderLayout());
      center.add (top, BorderLayout.NORTH);
      center.add (candidatePanel, BorderLayout.CENTER);

      panel.add (charChooser.getComponent(), BorderLayout.WEST);
      panel.add (center, BorderLayout.CENTER);
      
      return panel;
   }

   public void open (final String version)
   {
      ComponentTools.open ("The Palantiri Group Checker (version " + version + ")",
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
            KinshipXML xml = new KinshipXML();
            xml.setIncludeDetails (false);
            xml.setLookupPlayer (true);
            kinship = xml.scrapeURL ("Landroval", "The Palantiri");
            roster.setKinship (kinship);
            
            for (Character ch : kinship.getCharacters().values())
               charChooser.add (ch);
            charChooser.setLevelRange (50, Character.MAX_LEVEL);
            
            SwingUtilities.invokeLater (new Runnable()
            {
               public void run()
               {
                  progress.setString (charChooser.size() + " characters loaded");
                  progress.setIndeterminate (false);
               }
            });
         }
      });
      thread.start();
   }
   
   private void updateReport()
   {
      html.updateReport (roster.getCandidates().getCharacters(),
                         roster.getName(),
                         raidLevel.getValueAsInt(),
                         raidSize.getValueAsInt());
   }
   
   class CharListener implements ListSelectionListener
   {
      public void valueChanged (final ListSelectionEvent e)
      {
         if (!e.getValueIsAdjusting())
         {
            Character ch = charChooser.getSelected();
            if (ch != null)
               roster.getCandidates().add (ch);
         }
      }
   }
   
   class DataListener implements ListDataListener, SignupListener
   {
      public void intervalAdded   (final ListDataEvent e) { updateView (e); }
      public void intervalRemoved (final ListDataEvent e) { updateView (e); }
      public void contentsChanged (final ListDataEvent e) { updateView (e); }
      
      private void updateView (final ListDataEvent e)
      {
         candidatePanel.setTitle ("Group Candidates (" + roster.getCandidates().size() + ")");
         // updateReport();
      }

      public void characterAdded (final Character ch)
      {
         model.addCharacter (ch);
         updateReport();
      }

      public void characterRemoved (final Character ch)
      {
         model.removeCharacter (ch);
         updateReport();
      }

      public void characterUpdated (final Character ch)
      {
         model.updateCharacter (ch);
         updateReport();
      }
   }
   
   class ClickListener extends MouseAdapter
   {
      @Override
      public void mouseClicked (final MouseEvent e)
      {
         if (e.getClickCount() > 1) // support double-click
         {
            Signup signup = (Signup) candidateView.getSelectedValue();
            roster.getCandidates().remove (signup);
         }
      }
   }
   
   class TabListener implements ChangeListener
   {
      public void stateChanged (final ChangeEvent e)
      {
         if (tabs.getTitleAt (tabs.getSelectedIndex()).equals ("HTML Table"))
            updateReport();
      }
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      
      GroupChecker app = new GroupChecker ("Landroval", "The Palantiri", true);
      app.open ("May 6 2009");
      app.loadData();
   }
}
