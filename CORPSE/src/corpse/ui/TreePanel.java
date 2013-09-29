package corpse.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTree;

import corpse.CORPSE;
import corpse.RandomEntry;
import corpse.Table;
import file.FileUtils;
import gui.db.TableView;

public class TreePanel extends JSplitPane
{
   private static final long serialVersionUID = 1L;
   
   private CORPSE app;
   private String dir;
   private FileFilter fileFilter;
   
   private InvisibleTreeModel treeModel;
   private InvisibleNode root;
   private JXTree tree;
   private JTextField treeFilter;
   protected TabPane tabs;

   public TreePanel (final CORPSE app, final String dir, final String suffix)
   {
      this.app = app;
      this.dir = dir;
      
      fileFilter = new SuffixFilter (suffix);
      
      root = getFileTree (dir);
      treeModel = new InvisibleTreeModel(root);
      treeModel.activateFilter(true);
      
      tree = new JXTree (treeModel);
      tree.putClientProperty ("JTree.lineStyle", "Angled");
      tree.setShowsRootHandles (true);
      tree.setRootVisible (false);
      tree.setEditable (false);
      tree.addTreeSelectionListener (new TreeListener());
      tree.addMouseListener (new ClickListener()); // to handle double-clicks

      treeFilter = new JTextField();
      treeFilter.getDocument().addDocumentListener (new FilterListener());
      
      JPanel filterPanel = new JPanel(new BorderLayout());
      filterPanel.add(new JLabel(" Filter "), BorderLayout.WEST);
      filterPanel.add(treeFilter, BorderLayout.CENTER);
      
      JScrollPane scroll = new JScrollPane (tree);
      scroll.getVerticalScrollBar().setUnitIncrement(16);
      
      JPanel left = new JPanel(new BorderLayout());
      left.add(filterPanel, BorderLayout.NORTH);
      left.add(scroll, BorderLayout.CENTER);
      
      tabs = new TabPane();

      setLeftComponent (left);
      setRightComponent (tabs);
   }
   
   protected InvisibleNode getFileTree (final String path)
   {
      InvisibleNode root = new InvisibleNode();
      loadDirIntoTree (root, new File (path));
      return root;
   }

   private void loadDirIntoTree (final DefaultMutableTreeNode branch, final File dir)
   {
      for (File f : dir.listFiles (fileFilter))
      {
         FileNode node = new FileNode (f);
         branch.add (node);
         if (f.isDirectory())
         {
            loadDirIntoTree (node, f);
            if (node.getChildCount() == 0)
               branch.remove (node); // prune empty directories
         }
      }
   }

   class FileNode extends InvisibleNode
   {
      private static final long serialVersionUID = 1L;

      public FileNode (final File f)
      {
         super (f);
      }
      
      @Override
      public String toString()
      {
         return FileUtils.getNameWithoutSuffix((File) getUserObject());
      }
   }
   
   static class SuffixFilter implements FileFilter
   {
      private String suffix;
      
      public SuffixFilter (final String suffix)
      {
         this.suffix = "." + suffix.toLowerCase();
      }
      
      public boolean accept (final File f)
      {
         return f.isDirectory() || f.getName().toLowerCase().endsWith (suffix);
      }
   }
   
   class TreeListener implements TreeSelectionListener
   {
      public void valueChanged (final TreeSelectionEvent e)
      {
         applySelection();
      }
   }
   
   public void applySelection()
   {
      File file = getSelectedFile();
      if (file != null)
      {
         String name = FileUtils.getNameWithoutSuffix(file);
         int index = tabs.indexOfTab(name);
         if (index >= 0)
            tabs.setSelectedIndex(index);
         else
            loadRaw(name);
      }
   }

   private void loadRaw(final String name)
   {
      File file = getFile(name);
      if (file != null)
      {
         JTextArea raw = findRaw(name);
         System.out.println("TreePanel.loadRaw(): " + raw); // TODO
         if (raw == null)
            raw = makeRaw(name);
         
         raw.setText (FileUtils.getText (file));
         raw.setCaretPosition (0);
         tabs.setSelectedIndex(tabs.indexOfTab(name));
      }
   }

   private JTextArea findRaw(final String name)
   {
      JTextArea raw = null;
      int index = tabs.indexOfTab(name);
      if (index >= 0)
      {
         Component c = tabs.getComponentAt(index);
         if (c != null)
         {
            JScrollPane scroll = (JScrollPane) c;
            JViewport viewport = (JViewport) scroll.getComponent(0);
            JPanel panel = (JPanel) viewport.getComponent(0);
            raw = (JTextArea) panel.getComponent(0);
         }
      }
      return raw;
   }

   private JTextArea makeRaw(final String name)
   {
      System.out.println("TreePanel.makeRaw(): " + name); // TODO
      JTextArea raw = new JTextArea (20, 80);
      raw.setEditable (false); // TODO
      JPanel cards = new JPanel(new CardLayout());
      cards.add(raw, "raw");
      JLabel label = tabs.addToggleTab (name, new JScrollPane (cards));
      label.addMouseListener(new ToggleListener(cards));
      return raw;
   }
   
   protected void loadResolved(final String name)
   {
      Table table = Table.getTable (name);
      if (table != null)
      {
         JPanel resolved = findResolved(name);
         if (resolved != null)
            resolved.removeAll();
         else
            resolved = makeResolved(name);
         
         Component view = populateResolved(table, name);
         resolved.add (new JScrollPane (view));
         
         tabs.setSelectedIndex(tabs.indexOfTab(name));
      }
   }

   private Component populateResolved(final Table table, final String name)
   {
      DefaultTableModel model = table.getModel();
      JXTable view = new TableView (model, name, new TokenRenderer()).getView();
      view.setEditable (true);
      view.getTableHeader().setReorderingAllowed (false);
      view.packAll();
      return view;
   }

   protected JPanel findResolved(final String name)
   {
      JPanel resolved = null;
      int index = tabs.indexOfTab(name);
      if (index >= 0)
      {
         Component c = tabs.getComponentAt(index);
         if (c != null)
         {
            JScrollPane scroll = (JScrollPane) c;
            JViewport viewport = (JViewport) scroll.getComponent(0);
            JPanel panel = (JPanel) viewport.getComponent(0);
            if (panel.getComponentCount() > 1)
               resolved = (JPanel) panel.getComponent(1);
         }
      }
      return resolved;
   }

   protected JPanel makeResolved(final String name)
   {
      JPanel resolved = new JPanel (new BorderLayout());
      Component c = tabs.getSelectedComponent();
      JScrollPane scroll = (JScrollPane) c;
      JViewport viewport = (JViewport) scroll.getComponent(0);
      JPanel cards = (JPanel) viewport.getComponent(0);
      cards.add(resolved, "resolved");
      return resolved;
   }

   protected File getSelectedFile()
   {
      File file = null;
      TreePath tp = tree.getSelectionPath();
      if (tp != null)
      {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
         if (node.isLeaf())
            file = (File) node.getUserObject();
      }
      return file;
   }
   
   protected File getFile(final String name)
   {
      return Table.getTable(name).getFile();
   }

   public void roll()
   {
      String table = getSelectedTable();
      if (table != null)
      {
         String entry = RandomEntry.get (table, null, null, null);
         app.setText (entry);
      }
   }
   
   private String getSelectedTable()
   {
      String table = null;

      File file = getSelectedFile();
      if (file != null)
      {
         table = file.getName();
         table = table.substring (0, table.lastIndexOf ('.'));
      }
      
      return table;
   }
   
   public void refresh()
   {
      refreshTree();
      refreshRaw();
      refreshResolved();
   }

   private void refreshTree()
   {
      root.removeAllChildren();
      loadDirIntoTree (root, new File (dir));
      tree.expandAll(); // TODO?
   }
   
   private void refreshRaw()
   {
      int tabIndex = tabs.getSelectedIndex();
      if (tabIndex >= 0)
      {
         String title = tabs.getTitleAt(tabIndex);
         loadRaw(title);
      }
   }

   private void refreshResolved()
   {
      int tabIndex = tabs.getSelectedIndex();
      if (tabIndex >= 0)
      {
         String title = tabs.getTitleAt(tabIndex);
         loadResolved(title);
      }
   }

   class FilterListener implements DocumentListener
   {
      @Override
      public void insertUpdate(final DocumentEvent e)
      {
         changedUpdate(e);
      }

      @Override
      public void removeUpdate(final DocumentEvent e)
      {
         changedUpdate(e);
      }

      @Override
      public void changedUpdate(final DocumentEvent e)
      {
         filter(treeFilter.getText());
      }
      
      private void filter(final String pattern)
      {
         String upper = pattern.toUpperCase();
         @SuppressWarnings("unchecked")
         Enumeration<InvisibleNode> nodes = root.depthFirstEnumeration();
         while (nodes.hasMoreElements())
         {
            InvisibleNode node = nodes.nextElement();
            node.setVisible(node.toString().toUpperCase().contains(upper));
         }
         treeModel.nodeStructureChanged(root);
         tree.expandAll();
      }
   }
   
   class ClickListener extends MouseAdapter
   {
      @Override
      public void mouseClicked (final MouseEvent e)
      {
         if (e.getClickCount() > 1) // support double-click selection
            roll();
      }
   }
   
   class ToggleListener extends MouseAdapter
   {
      private JPanel cards;
      
      public ToggleListener(final JPanel cards)
      {
         this.cards = cards;
      }
      
      @Override
      public void mousePressed(final MouseEvent e)
      {
         JLabel label = (JLabel) e.getSource();
         int index = tabs.indexOfTab(label.getText());
         tabs.setSelectedIndex(index);
      }

      @Override
      public void mouseClicked(final MouseEvent e)
      {
         if (e.getClickCount() > 1) // support double-click selection
         {
            CardLayout layout = (CardLayout) cards.getLayout();
            if (cards.getComponentCount() == 1)
            {
               JLabel label = (JLabel) e.getSource();
               String name = label.getText();
               loadResolved(name);
            }
            layout.next(cards);
         }
      }
   }
}
