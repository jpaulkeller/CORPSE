package corpse.ui;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class InvisibleTreeModel extends DefaultTreeModel
{
   private static final long serialVersionUID = 1L;
   
   protected boolean filterIsActive;

   public InvisibleTreeModel(final TreeNode root)
   {
      this(root, false);
   }

   public InvisibleTreeModel(final TreeNode root, final boolean asksAllowsChildren)
   {
      this(root, false, false);
   }

   public InvisibleTreeModel(final TreeNode root, final boolean asksAllowsChildren, final boolean filterIsActive)
   {
      super(root, asksAllowsChildren);
      this.filterIsActive = filterIsActive;
   }

   public void activateFilter(final boolean newValue)
   {
      filterIsActive = newValue;
   }

   public boolean isActivatedFilter()
   {
      return filterIsActive;
   }

   @Override
   public Object getChild(final Object parent, final int index)
   {
      if (filterIsActive && parent instanceof InvisibleNode)
         return ((InvisibleNode) parent).getChildAt(index, filterIsActive);

      return ((TreeNode) parent).getChildAt(index);
   }

   @Override
   public int getChildCount(final Object parent)
   {
      if (filterIsActive && parent instanceof InvisibleNode)
         return ((InvisibleNode) parent).getChildCount(filterIsActive);

      return ((TreeNode) parent).getChildCount();
   }
}
