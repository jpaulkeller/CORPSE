package corpse;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import utils.ImageTools;

/** Computer-Oriented Role-Playing System & Environment */

// TODO
// Tab for each opened table
// change CMD suffix
// Figure out how to resolve CONDITIONS before resolving the inner values
// Interactive color-coded display (click to re-generate, or manually override, etc)
// use default macro for dice button (e.g, TitleMilitary:)

public class CORPSE
{
   private JTabbedPane tabs;
   private JPanel mainPanel;
   private TreePanel tables;
   private TreePanel scripts;
   private JProgressBar progress;
   private JTextField quickSlot; 
   private JButton entryButton;
   
   public JPanel getMainPanel()
   {
      return mainPanel;
   }
   
   private void buildGUI()
   {
      Menus menus = new Menus (this);
      
      tables  = new TreePanel (this, "data/Tables", "tbl");
      scripts = new ScriptPanel (this, "data/Scripts", "cmd");

      tabs = new JTabbedPane();
      tabs.addTab ("Tables", ImageTools.getIcon ("icons/20/gui/Table.gif"), tables);
      tabs.addTab ("Scripts", ImageTools.getIcon ("icons/20/objects/GearGreen.gif"), scripts);
      
      quickSlot = new JTextField (15);
      quickSlot.addKeyListener (new MyKeyListener());
      quickSlot.setToolTipText
         ("For a quick random value, enter any table name or dice expression (e.g., INN-NAME or 3d6)");
      
      progress = new JProgressBar (0, 100);
      progress.setFont (new Font ("Arial", Font.BOLD, 14));
      progress.setStringPainted (true);
      
      entryButton = menus.makeButton (Menus.ROLL, "icons/20/objects/Dice.gif",
      "Click to roll an entry from the selected table");

      final JPanel left =  new JPanel (new BorderLayout());
      left.add (entryButton, BorderLayout.WEST);
      left.add (quickSlot, BorderLayout.CENTER);
      final JPanel bottom = new JPanel (new BorderLayout());
      bottom.add (left, BorderLayout.WEST);
      bottom.add (progress, BorderLayout.CENTER);
      
      mainPanel = new JPanel (new BorderLayout());
      // mainPanel.add (menus.getMenus(), BorderLayout.NORTH); TODO
      mainPanel.add (tabs, BorderLayout.CENTER);
      mainPanel.add (bottom, BorderLayout.SOUTH);

      KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
      mainPanel.getActionMap().put("Refresh", new RefreshAction());
      mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, "Refresh");
   }

   void roll()
   {
      String macro = quickSlot.getText();
      if (macro != null && !macro.equals (""))
         progress.setString (Macros.resolve ("{" + macro + "}", null));
      else
         tables.roll();
   }
   
   void setText (final String text)
   {
      progress.setString (text);
   }
   
   public void open()
   {
      ComponentTools.open ("CORPSE (v0.1)", null, mainPanel, null);
      
      // must be done after the GUI is visible
      tables.setDividerLocation (0.25);
      scripts.setDividerLocation (0.25);
   }

   private class MyKeyListener extends KeyAdapter
   {
      @Override
      public void keyPressed (final KeyEvent e)
      {
         // TODO Incremental search of the Table/Script tree, highlight when found
         System.out.println("CORPSE.MyKeyListener.keyReleased()");
         int keyCode = e.getKeyCode();
         if (keyCode == KeyEvent.VK_ENTER && entryButton.isEnabled())
            entryButton.doClick();
      }
   }

   class RefreshAction extends AbstractAction
   {
      private static final long serialVersionUID = 1L;

      public RefreshAction()
      {
         super("Refresh");
      }
      
      public void actionPerformed(final ActionEvent e)
      {
         if (tabs.getSelectedIndex() == 0)
            tables.refresh();
         else
            scripts.refresh();
      }
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      Table.populate (new File ("data/Tables"));

      CORPSE app = new CORPSE();
      app.buildGUI();
      app.open();
   }
}
