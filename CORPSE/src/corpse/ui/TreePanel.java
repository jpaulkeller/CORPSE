package corpse.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;import javax.swing.JSplitPane;
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

public class TreePanel extends JSplitPane implements TabListener
{
   private static final long serialVersionUID = 1L;
   
   private static final Font FIXED_WIDTH = new Font(Font.DIALOG_INPUT, Font.PLAIN, 12);

   private Map<String, JPanel> cardsByName = new HashMap<String, JPanel>();

   private CORPSE app;
   private String dir;
   private FileFilter fileFilter;

   private InvisibleTreeModel treeModel;
   private InvisibleNode root;
   private JXTree tree;
   private JTextField treeFilter;
   protected TabPane tabs;

   public TreePanel(final CORPSE app, final String dir, final String suffix)
   {
      this.app = app;
      this.dir = dir;

      fileFilter = new SuffixFilter(suffix);

      root = getFileTree(dir);
      treeModel = new InvisibleTreeModel(root);
      treeModel.activateFilter(true);

      tree = new JXTree(treeModel);
      tree.putClientProperty("JTree.lineStyle", "Angled");
      tree.setShowsRootHandles(true);
      tree.setRootVisible(false);
      tree.setEditable(false);
      tree.addTreeSelectionListener(new TreeListener());
      tree.addMouseListener(new TreeNodeClickListener()); // to handle double-clicks

      treeFilter = new JTextField();
      treeFilter.getDocument().addDocumentListener(new FilterListener());

      JPanel filterPanel = new JPanel(new BorderLayout());
      filterPanel.add(new JLabel(" Filter "), BorderLayout.WEST);
      filterPanel.add(treeFilter, BorderLayout.CENTER);

      JScrollPane scroll = new JScrollPane(tree);
      scroll.getVerticalScrollBar().setUnitIncrement(16);

      JPanel left = new JPanel(new BorderLayout());
      left.add(filterPanel, BorderLayout.NORTH);
      left.add(scroll, BorderLayout.CENTER);

      tabs = new TabPane();
      tabs.addTabListener(this);

      setLeftComponent(left);
      setRightComponent(tabs);
   }

   protected InvisibleNode getFileTree(final String path)
   {
      InvisibleNode node = new InvisibleNode();
      loadDirIntoTree(node, new File(path));
      return node;
   }

   private void loadDirIntoTree(final DefaultMutableTreeNode branch, final File d)
   {
      // process the folders first
      for (File f : d.listFiles(fileFilter))
      {
         if (f.isDirectory())
         {
            FileNode node = new FileNode(f);
            branch.add(node);
            loadDirIntoTree(node, f);
            if (node.getChildCount() == 0)
               branch.remove(node); // prune empty directories
         }
      }
      
      // then add the files
      for (File f : d.listFiles(fileFilter))
         if (!f.isDirectory())
            branch.add(new FileNode(f));
   }

   class FileNode extends InvisibleNode
   {
      private static final long serialVersionUID = 1L;

      public FileNode(final File f)
      {
         super(f);
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

      public SuffixFilter(final String suffix)
      {
         this.suffix = "." + suffix.toLowerCase();
      }

      @Override
      public boolean accept(final File f)
      {
         return f.isDirectory() || f.getName().toLowerCase().endsWith(suffix);
      }
   }

   class TreeListener implements TreeSelectionListener
   {
      @Override
      public void valueChanged(final TreeSelectionEvent e)
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
            loadRaw(name, 0);
      }
   }

   private void loadRaw(final String name, final int caret)
   {
      File file = getFile(name);
      if (file != null)
      {
         JTextArea raw = findRaw(name);
         if (raw == null)
            raw = makeRaw(name);

         raw.setText(FileUtils.getText(file));
         try
         {
            raw.setCaretPosition(caret);
         }
         catch (IllegalArgumentException x)
         {
         }
         tabs.setSelectedIndex(tabs.indexOfTab(name));
         tabs.validate();
      }
   }

   private JTextArea findRaw(final String name)
   {
      int index = tabs.indexOfTab(name);
      JTextArea raw = findRaw(index);
      return raw;
   }

   private JTextArea findRaw(final int index)
   {
      JTextArea raw = null;
      if (index >= 0)
      {
         Component c = tabs.getComponentAt(index);
         if (c != null)
         {
            JPanel cards = (JPanel) c;
            JScrollPane scroll = (JScrollPane) cards.getComponent(0);
            JViewport viewport = (JViewport) scroll.getComponent(0);
            raw = (JTextArea) viewport.getComponent(0);
         }
      }
      return raw;
   }

   private JTextArea makeRaw(final String name)
   {
      JTextArea raw = new JTextArea(20, 80);
      raw.setFont(FIXED_WIDTH);
      raw.setEditable(false); // TODO
      JPanel cards = new JPanel(new CardLayout());
      cardsByName.put(name, cards); // TODO prefix name with Table/Script to ensure unique?
      cards.add(new JScrollPane(raw), "raw");

      JLabel label = tabs.addToggleTab(name, null, cards, "Double-click to swap between raw and resolved views");
      label.addMouseListener(new ToggleListener(cards));
      return raw;
   }

   protected void loadResolved(final String name, final int row)
   {
      Table table = Table.getTable(name);
      if (table != null)
      {
         JPanel resolved = findResolved(name);
         if (resolved != null)
            resolved.removeAll();
         else
            resolved = makeResolved(name);

         JXTable view = populateResolved(table, name);
         resolved.add(new JScrollPane(view)); // the scroller here also gives us the column headers
         resolved.validate();
         if (row < view.getRowCount())
         {
            view.setRowSelectionInterval(row, row);
            view.scrollRowToVisible(row);
         }

         tabs.setSelectedIndex(tabs.indexOfTab(name));
      }
   }

   private JXTable populateResolved(final Table table, final String name)
   {
      DefaultTableModel model = table.getModel();
      
      JXTable view = new TableView(model, name, new TokenRenderer()).getView();
      view.setEditable(true);
      view.getTableHeader().setReorderingAllowed(false);
      view.packAll();
      return view;
   }

   protected JPanel findResolved(final String name)
   {
      int index = tabs.indexOfTab(name);
      JPanel resolved = findResolved(index);
      return resolved;
   }

   private JPanel findResolved(final int index)
   {
      JPanel resolved = null;
      if (index >= 0)
      {
         Component c = tabs.getComponentAt(index);
         if (c != null)
         {
            JPanel cards = (JPanel) c;
            if (cards.getComponentCount() > 1)
               resolved = (JPanel) cards.getComponent(1);
         }
      }
      return resolved;
   }

   protected JPanel makeResolved(final String name)
   {
      JPanel resolved = new JPanel(new BorderLayout());
      JPanel cards = cardsByName.get(name);
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
         String entry = RandomEntry.get(table, table, table, null);
         app.setText(entry);
      }
   }

   private String getSelectedTable()
   {
      String table = null;

      File file = getSelectedFile();
      if (file != null)
      {
         table = file.getName();
         table = table.substring(0, table.lastIndexOf('.'));
      }

      return table;
   }

   public void refresh()
   {
      refreshTree();
      refreshRaw(getRawCaretPosition());
      refreshResolved(getResolvedPosition());
   }

   protected int getRawCaretPosition()
   {
      int caret = 0;
      int index = tabs.getSelectedIndex();
      JTextArea raw = findRaw(index);
      if (raw != null)
         caret = raw.getCaretPosition();
      return caret;
   }

   protected int getResolvedPosition()
   {
      int row = 0;
      int index = tabs.getSelectedIndex();
      JPanel resolved = findResolved(index);
      if (resolved != null)
      {
         JScrollPane scroll = (JScrollPane) resolved.getComponent(0);
         JViewport viewport = (JViewport) scroll.getComponent(0);
         JXTable view = (JXTable) viewport.getComponent(0);
         row = view.getSelectedRow(); // TODO - visible row, not selected
      }
      return row;
   }

   private void refreshTree()
   {
      root.removeAllChildren();
      loadDirIntoTree(root, new File(dir));
      tree.expandAll(); // TODO?
   }

   private void refreshRaw(final int caret)
   {
      int tabIndex = tabs.getSelectedIndex();
      if (tabIndex >= 0)
      {
         String title = tabs.getTitleAt(tabIndex);
         loadRaw(title, caret);
      }
   }

   private void refreshResolved(final int row)
   {
      int tabIndex = tabs.getSelectedIndex();
      if (tabIndex >= 0)
      {
         String title = tabs.getTitleAt(tabIndex);
         loadResolved(title, row);
      }
   }

   // implement TabListener
   @Override
   public void tabClosed(final String label)
   {
      cardsByName.remove(label);
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

   class TreeNodeClickListener extends MouseAdapter
   {
      @Override
      public void mouseClicked(final MouseEvent e)
      {
         if (e.getClickCount() > 1) // support double-click selection
         {
            String table = getSelectedTable();
            if (table != null)
               app.setQuickSlot(table);
            roll();
         }
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
               loadResolved(name, 0);
            }
            layout.next(cards);
         }
      }
   }
}
