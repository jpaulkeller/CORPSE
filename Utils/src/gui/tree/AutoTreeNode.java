package gui.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * The AutoTree class extends the DefaultMutableTreeNode class to
 * provide some convenience methods. */

public class AutoTreeNode extends DefaultMutableTreeNode
{
   private static final long serialVersionUID = 1;

   private String label;
   private String name;
   private Object key;
   private int count; // number of descendants
   private String toolTip;
   
   /**
    * For simple String objects, it's quite possible for all four values
    * to be the same.
    *
    * @param obj the wrapped object
    * @param name the path (controls placement within the hierarchy)
    * @param label the value to use as the label on the tree GUI
    * @param key a unique value (within the tree)
    */
   
   public AutoTreeNode (final Object obj, final String name, 
                        final String label, final Object key)
   {
      super (obj);
      setName (name);
      setLabel (label);
      setKey (key);
   }
   
   @Override
   public String toString()
   {
      return label;
   }

   public void setLabel (final String label)
   {
      this.label = label;
   }
   
   public String getLabel()
   {
      return label;
   }

   public void setName (final String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public void setKey (final Object key)
   {
      this.key = key;
   }

   public Object getKey()
   {
      return key;
   }

   public void setToolTipText (final String toolTipText)
   {
      this.toolTip = toolTipText;
   }
   
   public String getToolTipText()
   {
      return toolTip;
   }
   
   public int getDescendantCount()
   {
      return count;
   }
   
   public void updateCount()
   {
      count = getLeaves().size();
      TreeNode parent = getParent();
      if (parent instanceof AutoTreeNode)
         ((AutoTreeNode) parent).updateCount();
   }
   
   @Override
   public void insert (final MutableTreeNode newChild, final int childIndex)
   {
      super.insert (newChild, childIndex);
      updateCount();
   }

   @Override
   public void remove (final int childIndex)
   {
      super.remove (childIndex);
      updateCount();
   }

   @Override
   public void removeAllChildren()
   {
      super.removeAllChildren();
      count = 0;
   }

   public List<AutoTreeNode> getLeaves()
   {
      List<AutoTreeNode> leaves = new ArrayList<AutoTreeNode>();
      Enumeration<?> en = breadthFirstEnumeration();
      while (en.hasMoreElements())
      {
         AutoTreeNode node = (AutoTreeNode) en.nextElement();
         if (node.isLeaf())
            leaves.add (node);
      }
      return leaves;
   }

   public static void main (final String[] args)
   {
      AutoTreeNode node = new AutoTreeNode ("obj", "name", "label", "key");
      System.out.println ("getUserObject() = " + node.getUserObject());
      System.out.println ("getName()       = " + node.getName());
      System.out.println ("toString()      = " + node.toString());
      System.out.println ("getKey()        = " + node.getKey());
   }
}
