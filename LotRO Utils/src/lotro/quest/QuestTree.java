package lotro.quest;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class QuestTree extends DefaultTreeModel
{
   private static final long serialVersionUID = 1L;
   
   private final DefaultMutableTreeNode root;
   private final Map<Quest, DefaultMutableTreeNode> nodes;

   public QuestTree()
   {
      super (new DefaultMutableTreeNode());
      root = (DefaultMutableTreeNode) getRoot();
      nodes = new HashMap<Quest, DefaultMutableTreeNode>();
   }
   
   public void add (final Quest quest, final Quest parent)
   {
      DefaultMutableTreeNode parentNode = null;
      if (parent != null)
         parentNode = nodes.get (parent);
      if (parentNode == null)
         parentNode = root;
      
      DefaultMutableTreeNode node = nodes.get (quest);
      if (node == null)
      {
         node = new DefaultMutableTreeNode (quest);
         insertNodeInto (node, parentNode, 0); // TBD sort
         nodes.put (quest, node);
      }
      else
      {
         DefaultMutableTreeNode currentParent =
            (DefaultMutableTreeNode) node.getParent();
         if (currentParent == null)
            System.err.println ("Null parent: " + quest);
         else if (currentParent.equals (parentNode))
            return;
         else if (currentParent.equals (root))
         {
            removeNodeFromParent (node);
            insertNodeInto (node, parentNode, 0); // TBD sort
         }
         else
            System.err.println ("Two parents for: " + quest);
      }
   }
}
