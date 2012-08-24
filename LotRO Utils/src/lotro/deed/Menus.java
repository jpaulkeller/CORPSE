package lotro.deed;

import file.FileUtils;
import gui.ComponentTools;
import gui.comp.CheckBoxMenu;
import gui.comp.FileChooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import lotro.gui.CharacterDialog;
import lotro.gui.DeedDialog;
import lotro.models.AssignmentModel;
import lotro.models.Character;
import lotro.models.CharacterWithDeeds;
import lotro.models.Deed;
import lotro.models.Kinship;
import lotro.my.xml.KinshipXML;
import utils.BrowseURL;
import utils.Utils;

public class Menus implements Observer
{
   private static final int MAX_CHARS = 24;
   
   private static final String ABOUT     = "About";
   private static final String ADD_C     = "Add Character";
   private static final String ADD_D     = "Add Deed";
   private static final String CLEAR_A   = "Clear Assignments";
   private static final String CLEAR_R   = "Clear Relationships";
   private static final String EXIT      = "Exit";
   private static final String FORUM     = "Forum";
   private static final String HELP      = "Help";
   private static final String LEGEND    = "Legend";
   private static final String LOAD_A    = "Load Assignments";
   private static final String LOAD_C    = "Load Characters";
   private static final String OPTIONS   = "Options";
   private static final String ORGANIZE  = "Organize into Groups";
   private static final String QUICK_SV  = "Quick Save";
   private static final String RANDOM    = "Random Assignments";
   private static final String RELOAD_D  = "Reload Deeds";
   private static final String REMOVE_C  = "Remove Selected Characters";
   private static final String RMVALL_C  = "Remove All Characters";
   private static final String SAVE_A    = "Save Assignments";
   private static final String SAVE_C    = "Save Characters";
   private static final String SAVE_D    = "Save Deeds";
   private static final String SCRAPE    = "Scrape Roster";
   private static final String SLAYER    = "Slayer Only";
   private static final String SPOTS     = "Hunting Locations";
   private static final String START     = "Start Processing";
   private static final String STOP      = "Stop Processing";
   
   private ActionListener buttonListener = new ButtonListener();
   private About about;
   private CharacterDialog charDialog;
   private DeedDialog deedDialog;
   private FileChooser charFileChooser;
   private FileChooser groupFileChooser;
   private Map<String, JComponent> invokers = new HashMap<String, JComponent>();
   private Options options = new Options();
   private Score score;
   
   private DeedOrganizer app;
   private AssignmentModel model;
   private JMenuBar menus;
   
   private Thread thread;
   private JProgressBar progress;
   
   public Menus (final DeedOrganizer app, final AssignmentModel model)
   {
      this.app = app;
      this.model = model;
      score = new Score (app);
      menus = makeMenus();
   }

   JMenuBar getMenus()
   {
      enable();
      return menus;
   }
   
   JPanel makeProgressPanel()
   {
      JButton startButton = makeButton (START, "icons/Start.gif",
         "Suggest fellowships of characters with overlapping deeds");
      JButton stopButton = makeButton
         (STOP, "icons/Stop.gif", "Interrupt the processing");
      stopButton.setEnabled (false);

      JPanel buttons = new JPanel (new GridLayout (1, 0));
      buttons.add (startButton);
      buttons.add (stopButton);
      
      progress = new JProgressBar (0, 100);
      JPanel progressPanel = new JPanel (new BorderLayout());
      progressPanel.add (buttons, BorderLayout.WEST);
      progressPanel.add (progress, BorderLayout.CENTER);
      return progressPanel;
   }
   
   private JButton makeButton (final String command, final String iconName, 
                               final String tip)
   {
      // TBD: convert to use ImageTools.getIcon
      JButton button = new JButton (new ImageIcon (iconName));
      button.setMargin (new Insets (0, 0, 0, 0));
      button.setActionCommand (command);
      button.setToolTipText (tip);
      button.addActionListener (buttonListener);
      invokers.put (command, button);
      return button;
   }
   
   private JMenuBar makeMenus()
   {
      JMenuBar menubar = new JMenuBar();
      menubar.add (makeAssignmentsMenu());
      menubar.add (makeDeedMenu());
      menubar.add (makeCharacterMenu());
      menubar.add (makeHelpMenu());
      return menubar;
   }

   private JMenu makeAssignmentsMenu()
   {
      JMenu menu = new JMenu ("Assignments");
      menu.setMnemonic ('A');
      menu.add (makeMenuItem (ORGANIZE, 'G', "Group.gif", 
                              "Suggest fellowships of characters with overlapping deeds"));
      menu.addSeparator();
      menu.add (makeMenuItem (CLEAR_A, 'C', "AssignmentClear.gif", "Unassign all deeds"));
      menu.add (makeMenuItem (RANDOM, 'R', "AssignmentRandom.gif", 
                              "Randomly assign needed deeds (for testing)"));
      menu.addSeparator();
      menu.add (makeMenuItem (QUICK_SV, 'Q', "QuickSave.gif", 
                              "Save your current assignments to quicksave.grp"));
      menu.add (makeMenuItem (SAVE_A, 'S', "AssignmentSave.gif", 
                              "Save assignments to selected file"));
      menu.add (makeMenuItem (LOAD_A, 'L', "AssignmentLoad.gif", 
                              "Load assignments from selected file"));
      menu.addSeparator();
      menu.add (makeMenuItem (OPTIONS, 'O', "Options.gif", "Edit configuration options"));
      menu.addSeparator();
      menu.add (makeMenuItem (EXIT, 'E', "Exit.gif", "Exit this application"));
      return menu;
   }
   
   private JMenu makeDeedMenu()
   {
      JMenu menu = new JMenu ("Deeds");
      menu.setMnemonic ('D');
      menu.add (makeMenuItem (ADD_D, 'A', "DeedAdd.gif", "Open a dialog to add deeds"));
      menu.add (makeMenuItem (SAVE_D, 'S', "DeedSave.gif", "Save deeds (to deeds.txt)"));
      menu.add (makeMenuItem (RELOAD_D, 'R', "DeedReload.gif", "Remove and reload the deeds"));
      menu.addSeparator();
      menu.add (makeMenuItem (SLAYER, (char) 0, "DeedFilter.gif", 
                              "Only show the 'slayer' deeds"));
      // menu.add (makeMenuItem ("Scrape Deeds", 0, "Scrape deeds from mylotro.com"));
      return menu;
   }

   private JMenu makeCharacterMenu()
   {
      JMenu menu = new JMenu ("Characters");
      menu.setMnemonic ('C');
      menu.add (makeMenuItem (ADD_C, 'A', "CharacterAdd.gif", "Open a dialog to add characters"));
      menu.add (makeMenuItem (REMOVE_C, 'R', "CharacterRemove.gif", "Remove selected characters"));
      menu.add (makeMenuItem (RMVALL_C, 'v', "CharactersRemoveAll.gif", "Remove all characters"));
      menu.addSeparator();
      menu.add (makeMenuItem (SAVE_C, 'S', "CharacterSave.gif", 
                              "Save characters to a selected file"));
      menu.add (makeMenuItem (LOAD_C, 'L', "CharacterLoad.gif", 
                              "Load characters from a selected file"));
      menu.add (makeMenuItem (SCRAPE, 'P', "GuildPortal.gif", // TBD 
                              "Scrape characters from a mylotro.com"));
      menu.addSeparator();
      menu.add (makeMenuItem (CLEAR_R, 'C', "RelationshipClear.gif", "Clear all relationships"));
      return menu;
   }

   private JMenu makeHelpMenu()
   {
      JMenu menu = new JMenu (HELP);
      menu.setMnemonic ('H');
      menu.add (makeMenuItem (ABOUT, 'A', "About.gif", "Show infomation about this application"));
      menu.add (makeMenuItem (HELP, 'H', "Help.gif", "How do I use this?"));
      menu.add (makeMenuItem (LEGEND, 'L', "Legend.gif", "What do the colors mean?"));
      menu.addSeparator();
      menu.add (makeMenuItem (SPOTS, (char) 0, "Location.gif", 
                              "Good locations for racial enmity deeds"));
      menu.addSeparator();
      menu.add (makeMenuItem (FORUM, 'F', "Forum.gif", "Visit the Palantiri Tools forum"));
      return menu;
   }

   private JMenuItem makeMenuItem (final String label, final char mnemonic, 
                                   final String icon, final String tip)
   {
      JMenuItem mi = new JMenuItem (label);
      mi.setMnemonic (mnemonic);
      mi.setToolTipText (tip);
      if (icon != null)
         // TBD: convert to use ImageTools.getIcon
         mi.setIcon (new ImageIcon ("icons/" + icon));
      mi.addActionListener (buttonListener);
      invokers.put (label, mi);
      return mi;
   }
   
   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         String cmd = e.getActionCommand();         
         if      (cmd.equals (ABOUT))     about();
         else if (cmd.equals (ADD_C))     addCharacterDialog();
         else if (cmd.equals (ADD_D))     addDeedDialog();
         else if (cmd.equals (CLEAR_A))   clearAssignments();
         else if (cmd.equals (CLEAR_R))   clearRelationships();
         else if (cmd.equals (EXIT))      System.exit (0);
         else if (cmd.equals (FORUM))     openForum();
         else if (cmd.equals (HELP))      showFile ("data/readme.txt", 
                                                    "Deed Organizer Quick Help"); 
         else if (cmd.equals (LEGEND))    showLegend();
         else if (cmd.equals (LOAD_A))    loadModel();
         else if (cmd.equals (LOAD_C))    loadCharacters();
         else if (cmd.equals (OPTIONS))   setOptions();
         else if (cmd.equals (ORGANIZE))  assignGroups();
         else if (cmd.equals (QUICK_SV))  saveModel ("saves/quicksave.grp");
         else if (cmd.equals (RANDOM))    randomize();
         else if (cmd.equals (RELOAD_D))  reloadDeeds (Deed.FILE);
         else if (cmd.equals (REMOVE_C))  removeCharacters();
         else if (cmd.equals (RMVALL_C))  removeCharacters (model.getCharacters());
         else if (cmd.equals (SAVE_A))    saveModel();
         else if (cmd.equals (SAVE_C))    saveCharacters();
         else if (cmd.equals (SAVE_D))    saveDeeds (Deed.FILE);
         else if (cmd.equals (SCRAPE))    scrapeRoster();
         else if (cmd.equals (SLAYER))    filterDeeds ("slay");
         else if (cmd.equals (SPOTS))     showFile ("data/locations.txt", 
                                                    "Suggested Hunting Locations");
         else if (cmd.equals (START))     assignGroups();
         else if (cmd.equals (STOP))      stopProcessing();
         else
         {
            Toolkit.getDefaultToolkit().beep();
            app.getOutput().setText ("Unsupported command: " + cmd);
         }
      }
   }
   
   private void assignGroups()
   {
      app.getTabs().setSelectedIndex (0); // show the Assignment tab

      model.updateFromTable();
      int validChars = model.getCharactersWithDeeds().size();
      if (validChars > options.getMinGroupSize())
      {
         app.getOutput().setText ("Scoring...");
         invokers.get (ORGANIZE).setEnabled (false);
         invokers.get (START).setEnabled (false);
         invokers.get (STOP).setEnabled (true);
         progress.setIndeterminate (true);
         
         thread = new Thread (new Organizer());
         thread.setPriority (Thread.MIN_PRIORITY);
         thread.start();
      }
      else
         app.getOutput().setText
            ("Not enough characters with needed deeds yet: " + validChars);
   }

   class Organizer implements Runnable
   {
      public void run()
      {
         model.write (DeedOrganizer.AUTOSAVE_FILE);
         
         Set<CharacterWithDeeds> charSet =
            new HashSet<CharacterWithDeeds> (model.getCharactersWithDeeds());
         Partitioner<CharacterWithDeeds> p = new Partitioner<CharacterWithDeeds> (charSet);
         score.clear();
         scorePartitions (p);
         
         SwingUtilities.invokeLater (new Runnable() {
            public void run()
            {
               if (score.getBestScore() < 0)
                  app.getOutput().setText ("No good fellowships found");

               progress.setValue (0);
               progress.setIndeterminate (false);
               app.getOutput().setText ("");
               app.getTable().repaint();
               app.getTabs().setSelectedIndex (1); // change to Groups tab
               invokers.get (STOP).setEnabled (false);
               enable();
            }
         });
      }
   }
   
   private void stopProcessing()
   {
      thread.interrupt();
      if (score.getBestScore() < 0)
         app.getOutput().setText ("No good fellowships found yet");
   }
   
   // Score all possible partitions of the set of characters.  Partitions
   // with fewer than 2 or more than 6 Characters are not considered.
   
   private void scorePartitions (final Partitioner<CharacterWithDeeds> partitioner)
   {
      score.setPossible (partitioner.partitionCount());
      
      int minGroup = Math.max (options.getMinGroupSize(), model.getCharacterCount() / 4);
      int maxGroup = Math.min (options.getMaxGroupSize(), model.getCharacterCount());

      Partition<CharacterWithDeeds> partition;
      while ((partition = partitioner.getNextPartition()) != null)
      {
         score.incTrials();
         updateProgress();
         score.scorePartition (partition, model, minGroup, maxGroup);
         if (thread.isInterrupted())
            break;
      }
      System.out.println (score);
   }

   private void updateProgress()
   {
      int newPercent = (int) Math.round (score.getTrials() * 100d / score.getPossible());
      if (newPercent > score.getPercent()) // don't bother updating for small changes
      {
         score.setPercent (newPercent);
         if (score.getPercent() >= 10)
            SwingUtilities.invokeLater (new Runnable() {
               public void run()
               {
                  progress.setIndeterminate (false);
                  progress.setValue (score.getPercent());
               }
            });
      }
   }

   public void update (final Observable o, final Object arg)
   {
      if (arg instanceof CharacterWithDeeds)
         addCharacter ((CharacterWithDeeds) arg);
      else if (arg instanceof Deed)
         addDeed ((Deed) arg);
   }
   
   private void addCharacter (final CharacterWithDeeds ch)
   {
      model.updateFromTable();      
      model.addCharacter (ch);
      modelChanged();
   }
   
   private void addDeed (final Deed deed)
   {
      model.updateFromTable();      
      model.addDeed (deed);
      modelChanged();
   }
   
   private void modelChanged()
   {
      model.populate();
      app.getTable().packAll();
      enable();
   }
   
   private void enable()
   {
      int charCount = model.getCharacterCount();
      
      boolean chars = charCount > 0;
      invokers.get (CLEAR_A).setEnabled (chars);
      invokers.get (QUICK_SV).setEnabled (chars);
      invokers.get (RANDOM).setEnabled (chars);
      invokers.get (REMOVE_C).setEnabled (chars);
      invokers.get (RMVALL_C).setEnabled (chars);
      invokers.get (SAVE_A).setEnabled (chars);
      invokers.get (SAVE_C).setEnabled (chars);
      
      boolean ready = charCount > options.getMinGroupSize();
      invokers.get (START).setEnabled (ready);
      invokers.get (ORGANIZE).setEnabled (ready);
      
      boolean full = charCount >= MAX_CHARS;
      invokers.get (ADD_C).setEnabled (!full);
      invokers.get (LOAD_C).setEnabled (!full);
      boolean roster = true; // TBD options.getRosterURL() != null
      invokers.get (SCRAPE).setEnabled (!full && roster); 

      invokers.get (CLEAR_R).setEnabled (model.getRelationshipCount() > 0);
   }
   
   private void about()
   {
      if (about == null)
         about = new About ("data", "Splash.jpg");
      
      String title = "About the Deed Organizer";
      JFrame frame = (JFrame) app.getMainPanel().getTopLevelAncestor();
      JDialog window = new JDialog (frame, title, true);
      window.add (about);
      window.pack();
      ComponentTools.centerComponent (window);
      window.setVisible (true);
   }
   
   private void clearAssignments()
   {
      app.getOutput().setText ("");
      model.clear();
      modelChanged();
   }
   
   private void clearRelationships()
   {
      app.getOutput().setText ("");
      model.clearRelationships();
      model.populateRelationships();
   }
   
   private void saveModel()
   {
      if (groupFileChooser == null)
         makeGroupFileChooser();
      groupFileChooser.setApproveButtonText ("Save");
      
      int result = groupFileChooser.showOpenDialog (app.getMainPanel());
      if (result == JFileChooser.APPROVE_OPTION)
      {
         String file = groupFileChooser.getSelectedFile().getPath();
         if (file != null)
            saveModel (file);
      }
   }

   private void saveModel (final String file)
   {
      model.updateFromTable();      
      model.write (file);
      app.getOutput().setText ("Assignments saved to: " + file);
   }
   
   private void loadModel()
   {
      if (groupFileChooser == null)
         makeGroupFileChooser();
      groupFileChooser.setApproveButtonText ("Load");
      
      int result = groupFileChooser.showOpenDialog (app.getMainPanel());
      if (result == JFileChooser.APPROVE_OPTION)
      {
         String file = groupFileChooser.getSelectedFile().getPath();
         if (file != null)
         {
            model.read (file);
            modelChanged();
            app.getOutput().setText ("Loaded assignments from: " + file);
         }
      }
   }
   
   private void addDeedDialog()
   {
      if (deedDialog == null)
      {
         deedDialog = new DeedDialog ((JFrame) app.getMainPanel().getTopLevelAncestor());
         deedDialog.addObserver (this); // calls update()
      }
      deedDialog.setVisible (true);
   }
   
   private void reloadDeeds (final String file)
   {
      if (file != null)
      {
         model.updateFromTable();      
         model.clearDeeds();
         SortedMap<String, Deed> deeds = Deed.read (file);
         for (Deed deed : deeds.values())
            model.addDeed (deed);
         modelChanged();
         app.getOutput().setText ("Re-loaded deeds from: " + file);
      }
   }
   
   private void saveDeeds (final String file)
   {
      if (file != null)
      {
         model.updateFromTable();
         Deed.write (model.getDeeds(), file);
         app.getOutput().setText ("Deeds saved to: " + file);
      }
   }
   
   private void filterDeeds (final String pattern)
   {
      model.updateFromTable();
      // TBD: should removed deed be removed from assignments?
      for (Deed deed : model.getDeeds())
         if (!deed.getType().equalsIgnoreCase (pattern))
            model.removeDeed (deed);
      modelChanged();
   }
   
   private void addCharacterDialog()
   {
      if (charDialog == null)
      {
         charDialog = new CharacterDialog ((JFrame) app.getMainPanel().getTopLevelAncestor());
         charDialog.addObserver (this); // calls update()
      }
      charDialog.setVisible (true);
   }
   
   private void removeCharacters()
   {
      List<CharacterWithDeeds> charList = 
         new ArrayList<CharacterWithDeeds> (model.getCharacters());
      CheckBoxMenu<CharacterWithDeeds> menu = 
         new CheckBoxMenu<CharacterWithDeeds> (charList, null);
      JFrame frame = (JFrame) app.getMainPanel().getTopLevelAncestor();
      removeCharacters (menu.select (frame, "Select Characters to Remove"));
   }
   
   private void removeCharacters (final Collection<CharacterWithDeeds> charsToRemove)
   {
      if (!charsToRemove.isEmpty())
      {
         for (CharacterWithDeeds ch : charsToRemove)
            model.removeCharacter (ch.getName());
         modelChanged();
      }
   }
   
   private void loadCharacters()
   {
      if (charFileChooser == null)
         makeCharFileChooser();
      charFileChooser.setApproveButtonText ("Load");
      
      int result = charFileChooser.showOpenDialog (app.getMainPanel());
      if (result == JFileChooser.APPROVE_OPTION)
      {
         String file = charFileChooser.getSelectedFile().getPath();
         if (file != null)
         {
            model.updateFromTable();
            loadCharacters (file);
            modelChanged();
            app.getOutput().setText ("Loaded characters from: " + file);
         }
      }
   }
   
   private void loadCharacters (final String file)
   {
      for (CharacterWithDeeds ch : CharacterWithDeeds.read2 (file)) 
         model.addCharacter (ch);
   }
   
   private void scrapeRoster()
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (false);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      // TBD options.getRosterURL()
      
      List<CharacterWithDeeds> chars = new ArrayList<CharacterWithDeeds>();
      for (Character ch : kinship.getCharacters().values())
         chars.add (new CharacterWithDeeds (ch));
      
      CheckBoxMenu<CharacterWithDeeds> menu = 
         new CheckBoxMenu<CharacterWithDeeds> (chars, null);
      menu.setMaxSelectable (MAX_CHARS - model.getCharacterCount());
      JFrame frame = (JFrame) app.getMainPanel().getTopLevelAncestor();
      chars = menu.select (frame, "Select Characters to Add");

      if (!chars.isEmpty())
      {
         model.updateFromTable();
         for (CharacterWithDeeds ch : chars)
            model.addCharacter (ch);
         modelChanged();
         app.getOutput().setText ("Scraped characters from: " + options.getRosterURL());
      }
   }
   
   private void saveCharacters()
   {
      if (charFileChooser == null)
         makeCharFileChooser();
      charFileChooser.setApproveButtonText ("Save");
      
      int result = charFileChooser.showOpenDialog (app.getMainPanel());
      if (result == JFileChooser.APPROVE_OPTION)
      {
         String file = charFileChooser.getSelectedFile().getPath();
         if (file != null)
         {
            model.updateFromTable();
            CharacterWithDeeds.write (model.getCharacters(), file);
            app.getOutput().setText ("Characters saved to: " + file);
         }
      }
   }
   
   // randomly assign deeds (for testing)
   private void randomize()
   {
      if (model.getCharacters().isEmpty()) // load some default characters
         loadCharacters ("saves/characters.chr");
      
      for (CharacterWithDeeds ch : model.getCharacters())
         for (Deed deed : model.getDeeds())
            if (Math.random() < 0.15) // 15% chance to need this deed
               ch.assign (deed, true);
      modelChanged();
   }
   
   private void setOptions()
   {
      options.configure ((JFrame) app.getMainPanel().getTopLevelAncestor());
      enable();
   }
   
   private void openForum()
   {
      BrowseURL.displayURL (Options.FORUM_URL);
   }
   
   private void showFile (final String file, final String title)
   {
      String s = FileUtils.getText (file);
      JTextArea text = new JTextArea (s);
      text.setEditable (false);
      
      JPanel panel = new JPanel();
      panel.setBorder (Utils.BORDER);
      panel.add (text);
      
      JOptionPane.showMessageDialog
         (app.getMainPanel(), panel, title, JOptionPane.INFORMATION_MESSAGE);
   }
   
   private void showLegend()
   {
      GridLayout grid = new GridLayout (0, 1);
      grid.setVgap (5);

      JPanel p1 = new JPanel (grid);
      p1.setBackground (Color.WHITE);
      p1.setBorder (BorderFactory.createTitledBorder ("Assigned Deeds"));
      p1.add (makeLabel (Color.LIGHT_GRAY, "Grey: Not yet organized into groups "));
      p1.add (makeLabel (Color.GREEN, "Green: Needed and assigned"));
      p1.add (makeLabel (Color.YELLOW, "Yellow: Needed, but not assigned"));
      p1.add (makeLabel (Color.CYAN, "Blue: Assigned but not needed"));
      
      JPanel p2 = new JPanel (grid);
      p2.setBackground (Color.WHITE);
      p2.setBorder (BorderFactory.createTitledBorder ("Relationships"));
      p2.add (makeLabel (Color.GREEN, "Green: Characters must group together"));
      p2.add (makeLabel (Color.PINK, "Pink: Characters won't group together"));
      
      JPanel legend = new JPanel (new BorderLayout());
      legend.setBorder (Utils.BORDER);
      legend.add (p1, BorderLayout.NORTH);
      legend.add (p2, BorderLayout.SOUTH);
      
      String title = "Deed Organizer Legend";
      JOptionPane.showMessageDialog
         (app.getMainPanel(), legend, title, JOptionPane.INFORMATION_MESSAGE);
   }
   
   private JLabel makeLabel (final Color color, final String text)
   {
      JLabel label = new JLabel (text);
      label.setOpaque (true);
      label.setBackground (color);
      return label;
   }
   
   private void makeCharFileChooser()
   {
      charFileChooser = new FileChooser ("Character Files", "saves");
      charFileChooser.setRegexFilter (".*[.](?i:chr)", "Character Files");
      charFileChooser.setSelectedFile (new File ("sample.chr"));
   }
   
   private void makeGroupFileChooser()
   {
      groupFileChooser = new FileChooser ("Fellowship Files", "saves");
      groupFileChooser.setRegexFilter (".*[.](?i:grp)", "Fellowship Files");
      groupFileChooser.setSelectedFile (new File ("sample.grp"));
   }
}
