package corpse;

import gui.ComponentTools;
import gui.comp.ProgressBar;

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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import utils.ImageTools;
import corpse.ui.Menus;
import corpse.ui.ScriptPanel;
import corpse.ui.TabPane;
import corpse.ui.TreePanel;

/** CORPSE - Computer-Oriented Role-Playing System & Environment */

// TODO high priority
// lower-case subsets are internal
// capitalize based on assignment token (allow lower-case variables?)
// internal-only subsets
// script recursion for cave generation
// add Categories for each table (like Wiki) and provide a way to view by category
// text which can control where to include it (prepend, append, etc) - Dressed.tbl
// consider {Table.1} to get the first field, separated by at least 2 spaces - Herb
// or some way to specify a regex, and extract the group -- e.g. Table.([^ ]+)
// fix capitalization for scripts (Library.cmd - Extra)
// within a line, automatically apply !Different

// TODO data changes
// convert scripts to HTML (Tavern.cmd)

// TODO minor enhancements
// change CMD suffix?
// show footnotes in resolved view? (Reagent.tbl)
// figure out a way to cap-init a sentence w/o cap-initing each word?
// deal with null return from prompt better
// consider allowing ":" as field separator (specified in Column declaration?)
// Better CSV handling (specify separator) - Merchandise.tbl
// CSV quote format (val, "a, b", val3)

// TODO moderate enhancements
// global variables - such as "how common are elves?"
// SoundX filter? or for Searches?
// export text
// support .html files (table, definition)
// support .wiki files
// splash
// consider extending ArrayList to support WeightedList (store weight; don't duplicate element)
// Figure out how to resolve CONDITIONS before resolving the inner values
//
// TODO major enhancements
// consider applying filter based on its position in the token (e.g Profession#filter#:column)
// change Table to not populate, just use SubTable?
// matrix (e.g. name generators in KoDT #200)
// columns that affect other columns (e.g. Cost modified by Quality)?
// a FIRST-like annotated-HTML GUI. Click on the random entries to re-roll or select a new value (for scripts). Interactive color-coded display (click to re-generate, or manually override, etc)
// prompt should have re-roll button
// "snowflake" charts (for emotion, weather, etc)
// support UnitOfMeasure fields! which use dynamic currency conversion settings (or convert all costs to a standard generic number ~1sp)
// player vs. DM views of data
// UI - Add option on top with pull-downs to: Resolve [#] [Table]:[Subset].[Column]#[Filter]#

// TODO misc
// include/weight/etc for scripts? eg. Custom.cmd
// test embedded subsets in weighted lines TSR SCROLL
// consider: if there is a default subset, don't show other subsets in the resolved tabular view (e.g. Book)
// consider: support hidden columns (maybe :: prefix? private, invisible outside of file)
// ignore composite columns with 1 simple field (for the resolved view)
// why aren't composite columns (e.g Profession.Job) case-matched?
// support filtering of local xrefs (see Menu.tbl)
// remember which tables/scripts were open, and maybe the selected one, search text, etc
// while-loop for Kingdom.cmd
// if-condition for Poison.cmd (antidote)
// table-loop for each entry?

public final class CORPSE
{
   private static JFrame frame;

   private JPanel mainPanel;
   private TabPane tabs;
   private TreePanel tables;
   private TreePanel scripts;
   private ProgressBar progress;
   private JTextField quickSlot;
   private JButton entryButton;

   public static void init(final boolean debug)
   {
      Macros.DEBUG = debug;
      Table.populate(new File(Constants.DATA_PATH));
      Script.populate(new File(Constants.DATA_PATH));
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

      tables = new TreePanel(this, Constants.DATA_PATH, "tbl");
      scripts = new ScriptPanel(this, Constants.DATA_PATH, Constants.SCRIPT_SUFFIX);

      tabs = new TabPane();
      tabs.addTab("Tables", ImageTools.getIcon("icons/20/gui/Table.gif"), tables);
      tabs.addTab("Scripts", ImageTools.getIcon("icons/20/objects/GearGreen.gif"), scripts);

      entryButton = menus.makeButton(Menus.ROLL, null, "Click to roll an entry from the selected table");
      entryButton.setEnabled(false);

      quickSlot = new JTextField(30);
      quickSlot.addKeyListener(new MyKeyListener());
      quickSlot.setToolTipText("For a quick random value, enter any table name or dice expression (e.g., INN NAME or 3d6)");
      quickSlot.getDocument().addDocumentListener(new QuickSlotListener());

      progress = new ProgressBar();
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
         progress.setString(Macros.resolve(null, "{" + macro + "}", null)); // TODO currently selected?
      else
         tables.roll();
   }

   public void search()
   {
      Search.search(mainPanel, tabs, progress);
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
      
      Table.populate(new File(Constants.DATA_PATH));
      Script.populate(new File(Constants.DATA_PATH));
      
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
            resolve = Macros.resolve(null, "{" + quickSlot.getText() + "}", null); // TODO currently selected?
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
