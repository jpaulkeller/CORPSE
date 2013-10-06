package corpse.ui;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class InvisibleTreeModel extends DefaultTreeModel
{
   private static final long serialVersionUID = 1L;
   
   protected boolean filterIsActive;

   public InvisibleTreeModel(TreeNode root)
   {
      this(root, false);
   }

   public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren)
   {
      this(root, false, false);
   }

   public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren, boolean filterIsActive)
   {
      super(root, asksAllowsChildren);
      this.filterIsActive = filterIsActive;
   }

   public void activateFilter(boolean newValue)
   {
      filterIsActive = newValue;
   }

   public boolean isActivatedFilter()
   {
      return filterIsActive;
   }

   public Object getChild(Object parent, int index)
   {
      if (filterIsActive && parent instanceof InvisibleNode)
         return ((InvisibleNode) parent).getChildAt(index, filterIsActive);

      return ((TreeNode) parent).getChildAt(index);
   }

   public int getChildCount(Object parent)
   {
      if (filterIsActive && parent instanceof InvisibleNode)
         return ((InvisibleNode) parent).getChildCount(filterIsActive);

      return ((TreeNode) parent).getChildCount();
   }
}