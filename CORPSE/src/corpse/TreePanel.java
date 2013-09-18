package corpse;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTable;

import utils.ImageTools;
import file.FileUtils;
import gui.db.TableView;

public class TreePanel extends JSplitPane
{
   private static final long serialVersionUID = 1L;
   
   private CORPSE app;
   private FileFilter filter;
   private JTree tree;
   
   private JTabbedPane tabs;
   private JTextArea raw;
   protected JPanel resolved;

   public TreePanel (final CORPSE app, final String dir, final String suffix)
   {
      this.app = app;
      
      filter = new SuffixFilter (suffix);
      
      TreeNode root = getFileTree (dir);
      tree = new JTree (root);
      tree.putClientProperty ("JTree.lineStyle", "Angled");
      tree.setShowsRootHandles (true);
      tree.setRootVisible (false);
      tree.setEditable (false);
      tree.addTreeSelectionListener (new TreeListener());
      tree.addMouseListener (new ClickListener()); // to handle double-clicks

      raw = new JTextArea (20, 80);
      raw.setEditable (false); // TBD
      
      resolved = new JPanel (new BorderLayout());
      
      tabs = new JTabbedPane();
      tabs.addChangeListener (new TabListener());
      tabs.addTab ("Raw Text", ImageTools.getIcon ("icons/20/markers/CheckAll.gif"), new JScrollPane (raw));
      tabs.addTab ("Processed", ImageTools.getIcon ("icons/20/markers/CheckAll.gif"), resolved);
      
      // scroll.setBorder (Utils.BORDER);
      setLeftComponent (new JScrollPane (tree));
      setRightComponent (tabs);
   }
   
   private TreeNode getFileTree (final String path)
   {
      DefaultMutableTreeNode root = new DefaultMutableTreeNode();
      loadDirIntoTree (root, new File (path));
      return root;
   }
   
   private void loadDirIntoTree (final DefaultMutableTreeNode branch, final File dir)
   {
      for (File f : dir.listFiles (filter))
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

   class FileNode extends DefaultMutableTreeNode
   {
      private static final long serialVersionUID = 1L;

      public FileNode (final File f)
      {
         super (f);
      }
      
      @Override
      public String toString()
      {
         String name = ((File) getUserObject()).getName();
         int brk = name.lastIndexOf ('.'); // TBD use suffix?
         if (brk > 0)
            name = name.substring (0, brk); 
         return name;
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
   
   class TabListener implements ChangeListener
   {
      public void stateChanged (final ChangeEvent e)
      {
         if (tabs.getSelectedIndex() == 1)
            resolve();
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
         raw.setText (FileUtils.getText (file));
         raw.setCaretPosition (0);
         if (tabs.getSelectedIndex() == 1)
            resolve();
      }
   }
   
   protected void resolve()
   {
      String tableName = getSelectedTable();
      if (tableName != null)
      {
         Table table = Table.getTable (tableName);
         if (table != null)
         {
            DefaultTableModel model = table.getModel();
            
            JXTable view = new TableView (model, tableName, new TokenRenderer()).getView();
            view.setEditable (true);
            view.getTableHeader().setReorderingAllowed (false);
            view.packAll();
            
            resolved.removeAll();
            resolved.add (new JScrollPane (view));
            resolved.validate();
         }
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

   protected void roll()
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
   
   void refresh()
   {
      if (tabs.getSelectedIndex() == 0)
         Table.populate (new File ("data/Tables"));
      applySelection();
   }
}
