package gui.tree;

import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import gui.ToolTips;

/** Extends JTree to support tool tips. */

public class TipTree extends JTree
{
   private static final long serialVersionUID = 0;

   public TipTree (final TreeModel model)
   {
      super (model);
      setToolTipText (""); // hack to enable tooltips
   }

   @Override
   public String getToolTipText (final MouseEvent e)
   {
      TreePath tp = getPathForLocation (e.getX(), e.getY());
      if (tp != null)
      {
         DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) tp.getLastPathComponent();
         if (node.isLeaf())
         {
            Object obj = node.getUserObject();
            if (obj instanceof JComponent)
               return ((JComponent) obj).getToolTipText();
            if (obj instanceof ToolTips)
               return ((ToolTips) obj).getToolTipText();
         }
         if (node instanceof ToolTips)
            return ((ToolTips) node).getToolTipText();
         if (node.getUserObject() instanceof ToolTips)
            return ((ToolTips) node.getUserObject()).getToolTipText();
      }
      return null;
   }
}
