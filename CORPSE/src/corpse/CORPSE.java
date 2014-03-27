package corpse;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import utils.ImageTools;
import corpse.ui.Menus;
import corpse.ui.ScriptPanel;
import corpse.ui.TabPane;
import corpse.ui.TreePanel;

/** Computer-Oriented Role-Playing System & Environment */

// TODO
// generate HTML from scripts!  Tavern.cmd
// structured variables: table.column (e.g. gender.pronoun, gender.possessive, etc)
// columns that affect other columns (e.g. Cost modified by Quality)?
// SoundX filter
// if {table} defaults to {table@table}, how do you get the full entry?
// show footnotes in resolved view? (Reagent.tbl)
// filter to remove trailing parenthesized text (Equipment.tbl) 
// filter to reformat "General, Specific" to "Specific General" (Equipment.tbl) part of Column?
// why doesn't TSR Giant range prefix work? (in resolved view, and as a random text)
// how to resolve male/female titles
// "snowflake" charts (for emotion, weather, etc)
// figure out a way to cap-init a sentence w/o cap-initing each word
// support UnitOfMeasure fields! which use dynamic currency conversion settings

// export text
// include/weight/etc for scripts? eg. Custom.cmd
// support composite columns (like Job xxx = Title + Profession)
// change CMD suffix?
// support include lines with prefixes like ">> {{4d2-3} reagent}" in potion.cmd 
// consolidate comment characters

// use fixed-width font for raw view of tables

// support .html files (table, definition)
// support .wiki files

// determine numeric fields in resolved view renderer

// named filters? see Professions.tbl

// test embedded subsets in weighted lines TSR SCROLL
// consider allowing unique (or local) column names to work as shortcuts (e.g. @Royalty)
// matrix (e.g. name generators in KoDT #200)

// : TitleGuild {7,15} -- consider {~7} format

// splash and progress bar
// consider extending ArrayList to support WeightedList (store weight; don't duplicate element)
// player vs. DM views of data
// Figure out how to resolve CONDITIONS before resolving the inner values

// Interactive color-coded display (click to re-generate, or manually override, etc)
// A FIRST-like annotated-HTML GUI. Click on the random entries to re-roll or
// select a new value (for scripts)

// TODO data
// convert all costs to a standard generic number ~1sp
// merge job/profession tables

// prompt should have re-roll button
// deal with null return from prompt better

public final class CORPSE
{
   private static JFrame frame;

   private JPanel mainPanel;
   private TabPane tabs;
   private TreePanel tables;
   private TreePanel scripts;
   private JProgressBar progress;
   private JTextField quickSlot;
   private JButton entryButton;

   public static void init(final boolean debug)
   {
      Macros.DEBUG = debug;
      Table.populate(new File("data/Tables"));
      Script.populate(new File("data/Scripts"));
   }

   public CORPSE()
   {
      init(false);
   }

   public JPanel getMainPanel()
   {
      return mainPanel;
   }

   private void buildGUI()
   {
      Menus menus = new Menus(this);

      tables = new TreePanel(this, "data/Tables", "tbl");
      scripts = new ScriptPanel(this, "data/Scripts", "cmd");

      tabs = new TabPane();
      tabs.addTab("Tables", ImageTools.getIcon("icons/20/gui/Table.gif"), tables);
      tabs.addTab("Scripts", ImageTools.getIcon("icons/20/objects/GearGreen.gif"), scripts);

      entryButton = menus.makeButton(Menus.ROLL, null, "Click to roll an entry from the selected table");
      entryButton.setEnabled(false);

      quickSlot = new JTextField(30);
      quickSlot.addKeyListener(new MyKeyListener());
      quickSlot.setToolTipText("For a quick random value, enter any table name or dice expression (e.g., INN NAME or 3d6)");
      quickSlot.getDocument().addDocumentListener(new QuickSlotListener());

      progress = new JProgressBar(0, 100);
      progress.setFont(new Font("Arial", Font.BOLD, 14));
      progress.setStringPainted(true);

      final JPanel left = new JPanel(new BorderLayout());
      left.add(entryButton, BorderLayout.WEST);
      left.add(quickSlot, BorderLayout.CENTER);
      final JPanel bottom = new JPanel(new BorderLayout());
      bottom.add(left, BorderLayout.WEST);
      bottom.add(progress, BorderLayout.CENTER);

      mainPanel = new JPanel(new BorderLayout());
      mainPanel.setPreferredSize(new Dimension(900, 600));

      mainPanel.add(menus.getMenus(), BorderLayout.NORTH);
      mainPanel.add(tabs, BorderLayout.CENTER);
      mainPanel.add(bottom, BorderLayout.SOUTH);

      KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
      mainPanel.getActionMap().put("Refresh", new RefreshAction());
      mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, "Refresh");
   }

   public void roll()
   {
      String macro = quickSlot.getText();
      if (macro != null && !macro.equals(""))
         progress.setString(Macros.resolve(null, "{" + macro + "}")); // TODO currently selected?
      else
         tables.roll();
   }

   public void search()
   {
      Search.search(mainPanel, tabs);
   }

   public void setText(final String text)
   {
      progress.setString(text);
   }

   public void open()
   {
      frame = ComponentTools.open("CORPSE (v0.1)", null, mainPanel, null);

      // must be done after the GUI is visible
      tables.setDividerLocation(0.25);
      scripts.setDividerLocation(0.25);
   }

   // when the user pressed "ENTER", this fill generate a random entry
   private class MyKeyListener extends KeyAdapter
   {
      @Override
      public void keyPressed(final KeyEvent e)
      {
         if (entryButton.isEnabled())
         {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_F5)
               entryButton.doClick();
         }
      }
   }

   class RefreshAction extends AbstractAction
   {
      private static final long serialVersionUID = 1L;

      public RefreshAction()
      {
         super("Refresh");
      }

      @Override
      public void actionPerformed(final ActionEvent e)
      {
         refresh();
      }
   }

   public void refresh()
   {
      Table.TABLES.clear();
      Script.SCRIPTS.clear();
      
      Table.populate(new File("data/Tables"));
      Script.populate(new File("data/Scripts"));
      
      if (tabs.getSelectedIndex() == 0)
         tables.refresh();
      else
         scripts.refresh();
   }

   public void export()
   {
      System.out.println("CORPSE.export()"); // TODO
   }

   public void setQuickSlot(final String text)
   {
      quickSlot.setText(text);
   }

   public static Pattern safeCompile(final String message, final String regex)
   {
      Pattern p = null;
      try
      {
         p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
      }
      catch (PatternSyntaxException x)
      {
         showError("Invalid Pattern: " + regex, message + "\n\n" + x.getMessage());
      }
      return p;
   }

   public static void showError(final String title, final String message)
   {
      if (frame != null)
         JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
      else
         System.err.println(title + ": " + message);
   }

   class QuickSlotListener implements DocumentListener
   {
      // implement DocumentListener
      @Override
      public void removeUpdate(final DocumentEvent e)
      {
         valueChanged();
      }

      @Override
      public void changedUpdate(final DocumentEvent e)
      {
         valueChanged();
      }

      @Override
      public void insertUpdate(final DocumentEvent e)
      {
         valueChanged();
      }

      private void valueChanged()
      {
         String resolve = null;
         if (!quickSlot.getText().isEmpty())
            resolve = Macros.resolve(null, "{" + quickSlot.getText() + "}"); // TODO currently selected?
         entryButton.setEnabled(resolve != null && !resolve.matches("<.+>"));
      }
   }

   public static void main(final String[] args)
   {
      ComponentTools.setLookAndFeelNimbus();

      CORPSE app = new CORPSE();
      app.buildGUI();
      app.open();
   }
}
