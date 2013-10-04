package corpse;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import utils.ImageTools;
import corpse.ui.ScriptPanel;
import corpse.ui.TabPane;
import corpse.ui.TreePanel;

/** Computer-Oriented Role-Playing System & Environment */

// TODO
// maybe built-in global filters for common ones (e.g. no-spaces)?
// add a !token which resolves to the last resolved (so we can avoid adding 's' to plural entries)
// export text
// player vs. DM views of data
// include/weight/etc for scripts? eg. Custom.cmd
// allow multi-columns (like Job Title+Profession)
// change CMD suffix?
// Figure out how to resolve CONDITIONS before resolving the inner values
// Interactive color-coded display (click to re-generate, or manually override, etc)
// use default macro for dice button (e.g, TitleMilitary:)
// matrix (e.g. name generators in KoDT #200)
// change subset from ": Mine {5-40}" to ": Mine 5 40"
// This ad hoc subset syntax is not yet supported, but might be useful: Table STRUCTURE value: {AAA:3 - 4}
// splash and progress bar

// TODO simplify range format
// : SubsetName {Quantity} ColumnName
// : SubsetName {to} ColumnName
// : SubsetName {from-to} ColumnName

public class CORPSE
{
   private TabPane tabs;
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

      tabs = new TabPane();
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
      mainPanel.setPreferredSize(new Dimension(900, 600));
      
      mainPanel.add (menus.getMenus(), BorderLayout.NORTH);
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
   
   public void search()
   {
      Search.search(mainPanel, tabs);
   }
   
   public void setText (final String text)
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

   // TODO: is this used?
   private class MyKeyListener extends KeyAdapter
   {
      @Override
      public void keyPressed (final KeyEvent e)
      {
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
         // TODO: when focus is in the dice roll slot, just refresh it
      }
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      Table.populate (new File ("data/Tables"));
      Script.populate (new File ("data/Scripts"));

      CORPSE app = new CORPSE();
      app.buildGUI();
      app.open();
   }
}
