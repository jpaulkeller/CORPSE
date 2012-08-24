package lotro.deed;

// TBD

// show exploration locations (ongoing)

// hot-keys?

// Use mob images for icons (slugs, etc)

// Expand options to allow more user control (avg threshold)

// Change sysout to output.setText

import gui.ComponentTools;
import gui.editors.EnumEditor;
import gui.editors.RangeEditor;
import gui.editors.SubsetEditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lotro.gui.AssignmentEditor;
import lotro.gui.AssignmentRenderer;
import lotro.gui.RelationshipEditor;
import lotro.gui.RelationshipRenderer;
import lotro.models.Assignment;
import lotro.models.AssignmentModel;
import lotro.models.Klass;
import lotro.models.Race;
import lotro.models.Relationship;
import model.table.CSVTable;

import org.jdesktop.swingx.JXTable;

import utils.Utils;

public class DeedOrganizer
{
   private static final long serialVersionUID = 1L;

   static final String AUTOSAVE_FILE = "saves/autosave.grp";
   
   private AssignmentModel model;
   private JXTable table;
   
   private JTabbedPane tabs;
   private JPanel mainPanel;
   private JTextField output;
   private JTextArea results;
   
   JXTable getTable()
   {
      return table;
   }
   
   JTabbedPane getTabs()
   {
      return tabs;
   }
   
   JPanel getMainPanel()
   {
      return mainPanel;
   }

   JTextField getOutput()
   {
      return output;
   }
   
   JTextArea getResults()
   {
      return results;
   }
   
   private void buildGUI()
   {
      table = new JXTable (model.getTable());
      table.getTableHeader().setReorderingAllowed (false);
      table.setColumnControlVisible (true);
      table.setPreferredScrollableViewportSize (new Dimension (800, 500));
      table.setDefaultRenderer (Assignment.class, new AssignmentRenderer());
      table.setDefaultEditor (Assignment.class, new AssignmentEditor());
      table.packAll();
      
      JScrollPane scroll = new JScrollPane (table);
      scroll.setBorder (Utils.BORDER);

      output = new JTextField (80);
      output.setEditable (false);
      
      Menus menus = new Menus (this, model);
      
      final JPanel bottom = new JPanel (new BorderLayout());
      bottom.add (menus.makeProgressPanel(), BorderLayout.NORTH);
      bottom.add (new JScrollPane (output), BorderLayout.SOUTH);

      results = new JTextArea (20, 80);
      results.setEditable (false);
      JPanel groupPanel = new JPanel (new BorderLayout());
      groupPanel.add (new JScrollPane (results), BorderLayout.CENTER);
      
      tabs = new JTabbedPane();
      // TBD: convert to use icons.jar and ImageTools.getIcon
      tabs.addTab ("Assignments", new ImageIcon ("icons/Assignments.gif"), scroll);
      tabs.addTab ("Groups", new ImageIcon ("icons/Group.gif"), groupPanel);
      tabs.addTab ("Deeds", new ImageIcon ("icons/Deed.gif"), buildDeedGUI());
      tabs.addTab ("Characters", new ImageIcon ("icons/Character.gif"), buildCharGUI());
      tabs.addTab ("Relationships", new ImageIcon ("icons/Relationship.gif"), 
                   buildRelationshipGUI());
      tabs.addTab ("Traits", new ImageIcon ("icons/Trait.gif"), buildTraitGUI());
      
      mainPanel = new JPanel (new BorderLayout());
      mainPanel.add (menus.getMenus(), BorderLayout.NORTH);
      mainPanel.add (tabs, BorderLayout.CENTER);
      mainPanel.add (bottom, BorderLayout.SOUTH);
   }

   private JComponent buildDeedGUI()
   {
      JXTable tbl = new JXTable (model.getDeedTable());
      tbl.getTableHeader().setReorderingAllowed (false);
      tbl.setDefaultEditor (String.class, new SubsetEditor (true));
      tbl.setDefaultEditor (Integer.class, new RangeEditor (1, 50, 1));
      return new JScrollPane (tbl);
   }
   
   private JComponent buildCharGUI()
   {
      JXTable tbl = new JXTable (model.getCharacterTable());
      tbl.getTableHeader().setReorderingAllowed (false);
      tbl.setDefaultEditor (Race.class, new EnumEditor<Race> (Race.FREEPS));
      tbl.setDefaultEditor (Klass.class, new EnumEditor<Klass> (Klass.FREEPS));
      tbl.setDefaultEditor (Integer.class, new RangeEditor (1, 50, 1));
      return new JScrollPane (tbl);
   }
   
   private JComponent buildRelationshipGUI()
   {
      JTable tbl = new JTable (model.getRelationshipTable());
      tbl.getTableHeader().setReorderingAllowed (false);
      tbl.setDefaultRenderer (Relationship.class, new RelationshipRenderer());
      tbl.setDefaultEditor (Relationship.class, new RelationshipEditor());
      return new JScrollPane (tbl);
   }

   private JPanel buildTraitGUI()
   {
      CSVTable csv = new CSVTable (new File ("data/traits.csv"));
      try
      {
         csv.load();
      }
      catch (IOException x)
      {
         x.printStackTrace();
      }
      
      JXTable tbl = new JXTable (csv);
      tbl.getTableHeader().setReorderingAllowed (false);
      tbl.setColumnControlVisible (true);
      tbl.setPreferredScrollableViewportSize (new Dimension (800, 500));
      tbl.packAll();
      
      JPanel panel = new JPanel (new BorderLayout());
      panel.setBorder (Utils.BORDER);
      panel.add (new JScrollPane (tbl), BorderLayout.CENTER);
      return panel;
   }
   
   public void init()
   {
      ComponentTools.setDefaults();
      model = new AssignmentModel (AUTOSAVE_FILE);
      model.populate();
      buildGUI();
   }

   public void open()
   {
      ComponentTools.open ("The Palantiri Deed Organizer (version 1.0)",
                           null, mainPanel, null);
   }

   public static void main (final String[] args)
   {
      DeedOrganizer app = new DeedOrganizer();
      app.init();
      app.open();
   }
}
