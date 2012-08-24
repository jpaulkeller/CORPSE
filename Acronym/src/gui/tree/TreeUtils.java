package gui.tree;

import java.io.PrintStream;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class TreeUtils
{
   private TreeUtils() { } 

   @SuppressWarnings("unchecked")
   public static void outline (final PrintStream out, final DefaultMutableTreeNode root)
   {
      Enumeration<DefaultMutableTreeNode> en = root.preorderEnumeration();
      while (en.hasMoreElements())
      {
         DefaultMutableTreeNode node = en.nextElement();
         int level = node.getLevel();
         for (int i = 0; i < level; i++)
            out.print ("  ");
         out.println (node.toString());
      }
   }

   public static void expandAll (final JTree tree, final boolean expand)
   {
      if (tree != null)
      {
         TreeNode root = (TreeNode) tree.getModel().getRoot();
         expandAll (tree, new TreePath (root), expand);
      }
   }

   @SuppressWarnings("unchecked")
   private static void expandAll (final JTree tree, final TreePath path,
                                  final boolean expand)
   {
      TreeNode parent = (TreeNode) path.getLastPathComponent();
      Enumeration<DefaultMutableTreeNode> e = parent.children();      
      while (e.hasMoreElements())
      {
         TreeNode child = e.nextElement();
         if (!child.isLeaf())
            expandAll (tree, path.pathByAddingChild (child), expand);
      }

      // expansion or collapse must be done bottom-up
      if (expand)
         tree.expandPath (path);
      else
         tree.collapsePath (path);
   }
}
