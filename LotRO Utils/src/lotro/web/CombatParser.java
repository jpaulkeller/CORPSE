package lotro.web;

import gui.ComponentTools;
import gui.form.FileItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import utils.Utils;

public class CombatParser
{
   private static final long serialVersionUID = 1L;

   private static final Pattern START = 
      Pattern.compile ("### Chat Log: Combat (.+) ###"); // 10/20 12:44 PM
   private static final Pattern WOUND_YOU = 
      Pattern.compile ("The (.+) wounds you with (.+) for (\\d+) points? of (.+).");
   private static final Pattern YOU_WOUND = 
      Pattern.compile ("You wound the (.+?)(?: with (.+))? for (\\d+) points? of (.+) damage.");
   
   private static Map<String, Integer> offTotalByType =
      new TreeMap<String, Integer>();
   private static Map<String, Integer> offCountByType =
      new HashMap<String, Integer>();
   private static Map<String, Integer> defTotalByType =
      new TreeMap<String, Integer>();
   private static Map<String, Integer> defCountByType =
      new HashMap<String, Integer>();
   
   private JTabbedPane tabs;
   private JPanel mainPanel;
   private FileItem fileItem;
   private JButton loadButton;
   private JTextArea inputData;
   
   private StatsModel offModel; // damage you deal (offense)
   private StatsModel defModel; // damage you take (defense)
   private JXTable offTable;
   private JXTable defTable;
   
   private void buildGUI()
   {
      offModel = new StatsModel();
      offModel.addColumn ("Type/Target");
      offModel.addColumn ("Total");
      offModel.addColumn ("Count");
      offModel.addColumn ("Average");
      
      defModel = new StatsModel();
      defModel.addColumn ("Type/Attacker");
      defModel.addColumn ("Total");
      defModel.addColumn ("Count");
      defModel.addColumn ("Average");
      
      offTable = new JXTable (offModel);
      offTable.getTableHeader().setReorderingAllowed (false);
      offTable.setColumnControlVisible (true);
      offTable.setPreferredScrollableViewportSize (new Dimension (800, 500));
      offTable.packAll();
      
      defTable = new JXTable (defModel);
      defTable.getTableHeader().setReorderingAllowed (false);
      defTable.setColumnControlVisible (true);
      defTable.setPreferredScrollableViewportSize (new Dimension (800, 500));
      defTable.packAll();
      
      loadButton = new JButton ("Load");
      loadButton.setEnabled (false);
      loadButton.addActionListener (new ButtonListener());

      fileItem = new FileItem (null, null, 80);
      fileItem.addValueChangeListener (new FileListener());
      fileItem.setInitialValue (new File ("C:/pkgs/workspace/LOTRO/data/Combat_20080106_1.txt"));
      
      JPanel filePanel = new JPanel (new BorderLayout());
      filePanel.add (fileItem.getComponent(), BorderLayout.CENTER);
      filePanel.add (loadButton, BorderLayout.EAST);
      
      // Menus menus = new Menus (this, model);
      
      inputData = new JTextArea (20, 80);
      inputData.setEditable (false);
      JPanel logPanel = new JPanel (new BorderLayout());
      logPanel.add (new JScrollPane (inputData), BorderLayout.CENTER);

      JScrollPane offScroll = new JScrollPane (offTable);
      offScroll.setBorder (Utils.BORDER);
      JScrollPane defScroll = new JScrollPane (defTable);
      defScroll.setBorder (Utils.BORDER);

      JPanel offPanel = new JPanel (new BorderLayout());
      offPanel.add (new JScrollPane (offScroll), BorderLayout.CENTER);
      JPanel defPanel = new JPanel (new BorderLayout());
      defPanel.add (new JScrollPane (defScroll), BorderLayout.CENTER);

      tabs = new JTabbedPane();
      // TBD: convert to use ImageTools.getIcon
      tabs.addTab ("Log", new ImageIcon ("icons/Assignments.gif"), logPanel);
      tabs.addTab ("Offense", new ImageIcon ("icons/Offense.gif"), offPanel);
      tabs.addTab ("Defense", new ImageIcon ("icons/Defense.gif"), defPanel);
      
      JProgressBar progress;
      progress = new JProgressBar (0, 100);
      
      mainPanel = new JPanel (new BorderLayout());
      // mainPanel.add (menus.getMenus(), BorderLayout.NORTH);
      mainPanel.add (filePanel, BorderLayout.NORTH);
      mainPanel.add (tabs, BorderLayout.CENTER);
      mainPanel.add (progress, BorderLayout.SOUTH);
   }

   private void parseLog (final File logFile)
   {
      inputData.setText ("Loading...");
      
      try
      {
         FileInputStream fis = new FileInputStream (logFile);
         InputStreamReader isr = new InputStreamReader (fis, "UTF8");
         BufferedReader br = new BufferedReader (isr);
         StringBuilder buf = new StringBuilder();
         String line = null;
         while ((line = br.readLine()) != null)
            parseLine (buf, line);
         fis.close();

         inputData.setText (buf.toString());
         loadModel (offModel, offCountByType, offTotalByType);
         loadModel (defModel, defCountByType, defTotalByType);
      }
      catch (IOException x)
      {
         System.err.println ("CombatParser.parseLog() " + x);
      }
   }

   private void parseLine (final StringBuilder buf, final String line)
   {
      buf.append (line + "\n");
      
      Matcher m;
      if ((m = START.matcher (line)).matches())
      {
         Date start = parseDate ("2007 " + m.group (1));
         System.out.println ("Started: " + start); // TBD
      }
      else if ((m = WOUND_YOU.matcher (line)).matches())
      {
         String mob        = m.group (1);
         String kind       = m.group (2);
         int damage        = Integer.parseInt (m.group (3));
         String damageType = m.group (4);
         addToTotalDef ("Mob: " + mob, damage);
         addToTotalDef ("Skill: " + kind, damage);
         addToTotalDef ("Type: " + damageType, damage);
      }
      // "You wound the (.+)(?: with (.+))? for (\\d+) points? of (.+) damage.");
      else if ((m = YOU_WOUND.matcher (line)).matches())
      {
         String mob        = m.group (1);
         String kind       = m.group (2);
         int damage        = Integer.parseInt (m.group (3));
         String damageType = m.group (4);
         if (kind == null)
            kind = "Auto-attack";
         addToTotalOff ("Mob: " + mob, damage);
         addToTotalOff ("Skill: " + kind, damage);
         addToTotalOff ("Type: " + damageType, damage);
      }
   }

   private void addToTotalOff (final String kind, final int damage)
   {
      Integer currentCount = offCountByType.get (kind);
      Integer currentTotal = offTotalByType.get (kind);
      int count = currentCount != null ? currentCount : 0;
      int total = currentTotal != null ? currentTotal : 0;
      offCountByType.put (kind, count + 1);
      offTotalByType.put (kind, total + damage);
   }

   private void addToTotalDef (final String kind, final int damage)
   {
      Integer currentCount = defCountByType.get (kind);
      Integer currentTotal = defTotalByType.get (kind);
      int count = currentCount != null ? currentCount : 0;
      int total = currentTotal != null ? currentTotal : 0;
      defCountByType.put (kind, count + 1);
      defTotalByType.put (kind, total + damage);
   }

   private void loadModel (final DefaultTableModel model,
                           final Map<String, Integer> countByType,
                           final Map<String, Integer> totalByType)
   {
      while (model.getRowCount() > 0)
         model.removeRow (0);
      
      for (Map.Entry<String, Integer> entry : totalByType.entrySet())
      {
         String type = entry.getKey();
         int total = entry.getValue();
         int count = countByType.get (type);
         Vector<Object> row = new Vector<Object>();
         row.add (type);
         row.add (total);
         row.add (count);
         row.add (total / count);
         model.addRow (row);
      }
   }

   private Date parseDate (final String s)
   {
      Date date = null;
      DateFormat df = new SimpleDateFormat ("yyyy MM/dd hh:mm a");
      try
      {
         date = df.parse (s);
         System.out.println ("start: " + date);
      }
      catch (ParseException x)
      {
         System.err.println (x);
      }
      return date;
   }

   public void open()
   {
      ComponentTools.open ("The Palantiri Combat Log Parser (version 0.1)",
                           null, mainPanel, null);
   }

   public void init()
   {
      try
      {
         UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception x) { System.err.println (x); }
      
      buildGUI();
   }

   class FileListener implements ValueChangeListener
   {
      public void valueChanged (final ValueChangeEvent e)
      {
         File file = fileItem.getFile();
         loadButton.setEnabled (file != null && file.exists());
      }
   }
   
   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         parseLog (fileItem.getFile());
      }
   }
   
   class StatsModel extends DefaultTableModel
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getColumnClass (final int c)
      {
         return getValueAt (0, c).getClass();
      }      
   }
   
   public static void main (final String[] args)
   {
      CombatParser app = new CombatParser();
      app.init();
      app.open();
   }
}
