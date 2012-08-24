package lotro.my.reports;

import gui.ComponentTools;
import gui.comp.DragDropList;
import gui.db.TableView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JApplet;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import lotro.models.Character;
import lotro.models.CharacterListModel;
import lotro.models.CharacterTable;
import lotro.my.xml.CharacterXML;
import lotro.raid.CharacterSelector;
import lotro.raid.SignupListener;
import lotro.raid.StatPanel;
import lotro.views.ClassPie;
import lotro.views.Roster;

public final class Dashboard extends JApplet
{
   private static final long serialVersionUID = 1L;
   
   private JPanel mainPanel;
   private JProgressBar progress;

   private CharacterListModel characters;
   private CharacterTable model;
   
   // private CharacterHTML html;
   private Roster roster;
   private ClassPie pie;
   
   private CharacterSelector charChooser;
   private JTabbedPane tabs;
   private StatPanel statPanel;
   private JList candidateView;
   private JPanel candidatePanel;
   
   public Dashboard()
   {
      charChooser = new CharacterSelector (null, null, false);
      charChooser.setLevelRange (50, Character.MAX_LEVEL);
      CharListener charListener = new CharListener();
      charChooser.addMouseListener (charListener);
      charChooser.addObserver (charListener);
      Component comp = charChooser.getComponent(); 
      comp.setPreferredSize (comp.getMinimumSize());
      
      model = new CharacterTable();
      characters = new CharacterListModel (null, true);
      roster = new Roster (characters);
      pie = new ClassPie (characters);
      statPanel = new StatPanel();
      // html = new CharacterHTML (statPanel);

      ReportSelector reportSelector = new ReportSelector (new ReportListener());

      tabs = new JTabbedPane();
      tabs.setPreferredSize (new Dimension (750, 500));
      tabs.addTab ("Table", new TableView (model).getPanel (false, 600, 200));
      tabs.addTab ("Stats", statPanel);
      tabs.addChangeListener (new TabListener());
      
      JPanel reports = new JPanel (new BorderLayout());
      reports.add (reportSelector.getComponent(), BorderLayout.NORTH);
      reports.add (tabs, BorderLayout.CENTER);
                                   
      progress = new JProgressBar (0, 100);
      progress.setStringPainted (true);

      mainPanel = new JPanel (new BorderLayout());
      mainPanel.add (makeSignupPanel(), BorderLayout.WEST);
      mainPanel.add (reports, BorderLayout.CENTER);
      mainPanel.add (progress, BorderLayout.SOUTH);
   }
   
   @Override
   public String getAppletInfo()
   {
      return "Name: Test"; // TBD
   }

   // Returns an array of strings for the parameters understood by this applet.
   @Override
   public String[][] getParameterInfo()
   {
      String[][] info =
      { 
         { "date", "String", "Date/time to which applet will countdown" }, // TBD
      };
      return info;
   }

   @Override
   public void init()
   {
      try
      {
         SwingUtilities.invokeAndWait (new Runnable()
         {
            public void run() 
            {
               createGUI();
            }
         });
      } 
      catch (Exception x) 
      {
         x.printStackTrace();
      }
   }
   
   private void createGUI() 
   {
      getContentPane().add (mainPanel, BorderLayout.CENTER);
   }
   
   @Override
   public void start()
   {
      System.out.println ("Dashboard.start()"); // TBD
   }

   @Override
   public void stop()
   {
      System.out.println ("Dashboard.stop()"); // TBD
   }
   
   private JPanel makeSignupPanel()
   {
      JPanel panel = new JPanel (new GridLayout (1, 0));
      
      DataListener dataListener = new DataListener();
      characters.addListDataListener (dataListener);
      characters.addListener (dataListener);
      
      candidateView = new DragDropList (characters, DnDConstants.ACTION_COPY_OR_MOVE);
      candidateView.addMouseListener (new ClickListener());
      
      candidatePanel = new JPanel (new BorderLayout());
      candidatePanel.add (new JScrollPane (candidateView), BorderLayout.CENTER);
      candidatePanel.setBorder (new TitledBorder ("Characters"));
      candidatePanel.setPreferredSize (candidatePanel.getMinimumSize());
      
      panel.add (charChooser.getComponent());
      panel.add (candidatePanel);
      
      return panel;
   }

   public void open (final String version)
   {
      ComponentTools.open ("Kin Charts (version " + version + ")",
                           null, mainPanel, null);
   }

   private void updateReport()
   {
	  // System.out.println ("Dashboard.updateReport()"); // TBD
      roster.update(); // TBD
      pie.update();
      /*
      int raidLevel = 60;
      int raidSize = 12;
      html.updateReport (characters, "TITLE TBD", raidLevel, raidSize);
      */
   }

   class CharListener extends MouseAdapter implements Observer
   {
      @Override
      public void mouseClicked (final MouseEvent e)
      {
         if (e.getClickCount() > 1) // support double-click
         {
            Character ch = charChooser.getSelected();
            if (ch != null)
            {
               CharacterXML.loadCharacter (ch);
               characters.add (ch);
            }
         }
      }

      public void update (final Observable o, final Object arg)
      {
         progress.setString ((String) arg);
         progress.setIndeterminate (arg.toString().contains ("Loading"));
      }
   }
   
   class DataListener implements ListDataListener, SignupListener
   {
      public void intervalAdded   (final ListDataEvent e) { updateView (e); }
      public void intervalRemoved (final ListDataEvent e) { updateView (e); }
      public void contentsChanged (final ListDataEvent e) { updateView (e); }
      
      private void updateView (final ListDataEvent e)
      {
         String title = "Characters (" + characters.size() + ")";
         ((TitledBorder) candidatePanel.getBorder()).setTitle (title);
         candidatePanel.repaint();
         updateReport();
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
            Character ch = (Character) candidateView.getSelectedValue();
            characters.remove (ch);
         }
      }
   }
   
   class ReportListener implements ActionListener
   {
      Map<String, Component> reports = new HashMap<String, Component>();
      
      public void actionPerformed (final ActionEvent e)
      {
         String cmd = e.getActionCommand();
         
         Component c = reports.get (cmd);
         if (c == null)
         {
            if (cmd.equals ("Classes"))
               c = addClasses (cmd);
            else if (cmd.equals ("Class/Level"))
               System.out.println ("ReportListener.actionPerformed(): " + cmd);
            else if (cmd.equals ("Roster"))
               c = addRoster (cmd);
            else if (cmd.equals ("Stats"))
               System.out.println ("ReportListener.actionPerformed(): " + cmd);
            else if (cmd.equals ("Crafters"))
               System.out.println ("ReportListener.actionPerformed(): " + cmd);
            else if (cmd.equals ("Equipment"))
               System.out.println ("ReportListener.actionPerformed(): " + cmd);
            reports.put (cmd, c);
         }
         if (c != null)
            tabs.setSelectedComponent (c);
      }

      private Component addClasses (final String title)
      {
         Component c = new JScrollPane (pie.getHtmlPane());
         tabs.addTab (title, c);
         tabs.addTab (title + " HTML", new JScrollPane (pie.getRawPane()));
         return c;
      }
      
      private Component addRoster (final String title)
      {
         Component c = new JScrollPane (roster.getHtmlPane());
         tabs.addTab (title, c);
         tabs.addTab (title + " HTML", new JScrollPane (roster.getRawPane()));
         return c;
      }
   }

   class TabListener implements ChangeListener
   {
      public void stateChanged (final ChangeEvent e)
      {
         System.out.println ("TabListener updating");
         if (tabs.getTitleAt (tabs.getSelectedIndex()).equals ("Classes"))
            pie.update();
         else if (tabs.getTitleAt (tabs.getSelectedIndex()).equals ("Roster"))
            roster.update();
      }
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      
      Dashboard app = new Dashboard();
      app.open ("June 7 2009");
   }
}
