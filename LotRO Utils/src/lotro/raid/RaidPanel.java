package lotro.raid;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;

import lotro.models.Player;
import lotro.web.Dropbox;
import utils.ImageTools;
import file.FileUtils;

public class RaidPanel extends JPanel
{
   private static final long serialVersionUID = 1L;

   private RaidOrganizer app;
   private Roster roster;
   private ButtonGroup buttons;
   private JTextPane htmlPane;
   private Group group;
   private JButton optimize;
   private Composition composition;
   
   public RaidPanel (final RaidOrganizer app, final Roster roster)
   {
      super (new BorderLayout());
      
      this.app = app;
      this.roster = roster;
      this.group = new Group (app.getProgress());
      
      ActionListener listener = new RaidListener();
      buttons = new ButtonGroup();
      
      ImageIcon icon = ImageTools.getIcon ("icons/Optimize.gif");
      optimize = new JButton ("Optimize", icon);
      // optimize.setEnabled (composition != null && !app.candidates.isEmpty()); TBD
      optimize.addActionListener (listener);
      optimize.setToolTipText ("Find the optimal group using these characters");
      
      String raid1 = Dropbox.get().getPath ("/raids/Rift Day 1.raid");
      String raid2 = Dropbox.get().getPath ("/raids/Rift Day 2.raid");
      String raid3 = Dropbox.get().getPath ("/raids/Rift Day 3 Balrog.raid");
      String raid4 = Dropbox.get().getPath ("/raids/Rift.raid");
      
      JPanel raids = new JPanel (new GridLayout (0, 1));
      raids.add (addButton (listener, raid1));
      raids.add (addButton (listener, raid2));
      raids.add (addButton (listener, raid3));
      raids.add (addButton (listener, raid4));
      raids.setBorder (BorderFactory.createTitledBorder ("Raids"));
      
      JPanel rosterPanel = roster.makeRosterPanel();

      JPanel options = new JPanel (new BorderLayout());
      options.add (raids, BorderLayout.NORTH);
      options.add (rosterPanel, BorderLayout.CENTER);
      
      JPanel left = new JPanel (new BorderLayout());
      left.add (options, BorderLayout.CENTER);
      left.add (optimize, BorderLayout.SOUTH);
      
      htmlPane = new JTextPane();
      htmlPane.setBackground (null);
      htmlPane.setEditable (false);
      htmlPane.setEditorKit (new HTMLEditorKit());
      JScrollPane scroll = new JScrollPane (htmlPane);
      
      add (left, BorderLayout.WEST);
      add (scroll, BorderLayout.CENTER);
   }
   
   private JRadioButton addButton (final ActionListener listener,
                                   final String url)
   {
      String label = FileUtils.getNameWithoutSuffix (new File (url));
      label = label.replace ("%20", " ");
      JRadioButton button = new JRadioButton (label);
      button.setActionCommand (url);
      button.addActionListener (listener);
      buttons.add (button);
      return button;
   }
                             
   private void findBestGroup (final String raidURL)
   {
      Map<Player, Signup> signups = new LinkedHashMap<Player, Signup>();
      for (Signup signup : app.getCandidates())
         signups.put (signup.getPlayer(), signup);
      for (Signup signup : app.getBackups())
         signups.put (signup.getPlayer(), signup);
      group.setSignups (signups);
      
      // optimize.setEnabled (false);
      
      Thread thread = new Thread (new Runnable()
      {
         public void run()
         {
            group.setComposition (composition);
            group.optimize();
            
            SwingUtilities.invokeLater (new Runnable()
            {
               public void run()
               {
                  app.getProgress().setValue (0);
                  saveRaidGroup (true);
               }
            });
         }
      });
      thread.start();
   }
   
   private void saveRaidGroup (final boolean showScores)
   {
      if (group.isOptimized())
      {
         String desk = System.getProperty ("user.home") + "/Desktop";
         String raid = group.getComposition().getName();
         String team = roster.getName();
         String path = desk + "/" + raid + "-" + team + ".html";
         group.saveAsHTML (path, showScores);
         htmlPane.setText (FileUtils.getText (path));
         app.getProgress().setString ("Optimal group saved as: " + path);
      }
   }
   
   class RaidListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         ButtonModel button = buttons.getSelection();
         if (button != null)
         {
            String url = button.getActionCommand();
            String cmd = e.getActionCommand();
            if (cmd.equals ("Optimize"))
               findBestGroup (url);
            else
            {
               composition = Composition.loadFromFile (url);
               app.getProgress().setString ("Raid composition loaded: " + composition.getName());
            }
         }
      }
   }

}
