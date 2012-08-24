package gui.tree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import str.TokenizedString;

/**
 * The AutoTree class extends the DefaultTreeModel class to provide
 * some convenience methods which automatically convert flat data into
 * hierarchical data.
 *
 * For example, all nodes with the same prefix are assumed to be part
 * of the same branch.  New branches are created and deleted as
 * needed, as nodes are added and removed.
 */
public class AutoTree extends DefaultTreeModel
{
   private static final long serialVersionUID = 0;
   // private static boolean DEBUG = false;

   private Map<Object, TreeNode> pathToNode = new HashMap<Object, TreeNode>();

   private String separator = "/";      // string to use as breakpoint

   private boolean forceLeaves = true;  // use false to allow non-leaf input nodes
   private boolean simpleNames = true;  // true to strip duplicate prefix from leaf nodes

   public AutoTree()
   {
      super (new AutoTreeNode (null, null, null, null));
   }

   public void setSeparator (final String separator)
   {
      this.separator = separator;
   }

   public String getSeparator()
   {
      return this.separator;
   }

   /** Set false to allow duplicate prefix on leaf nodes. */
   
   public void setSimpleNames (final boolean simpleNames)
   {
      this.simpleNames = simpleNames;
   }
   
   public void setForceLeaves (final boolean forceLeaves)
   {
      this.forceLeaves = forceLeaves;
   }

   public boolean getForceLeaves()
   {
      return this.forceLeaves;
   }

   public void clear()
   {
      AutoTreeNode root = (AutoTreeNode) getRoot();
      while (root.getChildCount() > 0)
         removeNodeFromParent ((MutableTreeNode) root.getChildAt (0));
   }
   
   // Given two node names, find the common prefix.  For example,
   // given A/B/C1, and "A/B/C2", return A/B (where "/" is the separator).

   private String getCommonPrefix (final AutoTreeNode one, final AutoTreeNode two)
   {
      String name1 = one.getName();
      String name2 = two.getName();

      if (name1.startsWith (name2 + separator))
         return name2;
      if (name2.startsWith (name1 + separator))
         return name1;

      String prefix = null;
      int len = Math.min (name1.length(), name2.length());
      int pos = 0;
      while (pos < len && name1.charAt (pos) == name2.charAt (pos))
         pos++;
      if (pos > 0)
      {
         prefix = name1.substring (0, pos);
         pos = prefix.lastIndexOf (separator);
         if (pos > 0)
            prefix = prefix.substring (0, pos);
      }

      // if (DEBUG) 
      //    System.out.println ("getCommonPrefix (" + name1 + ", " + name2 + ") = " + prefix);

      return prefix;
   }

   protected Object getKey (final AutoTreeNode rootNode, final String name, 
                            final boolean branch)
   {
      String key = name;

      if (rootNode != null && rootNode.getKey() != null)
         key = rootNode.getKey() + ":" + key;

      if (forceLeaves && branch)
         key += separator;

      return key;
   }

   /** Insert the given child into the given parent, in lexical order. */

   protected AutoTreeNode insert (final AutoTreeNode parent,
                                  final AutoTreeNode child,
                                  final boolean leaf)
   {
      if (!leaf || simpleNames)
      {
         // simplify the branch name (don't duplicate parent text)
         String parentLabel = parent.getName() + separator;
         String childLabel = child.getName();

         if (childLabel.startsWith (parentLabel))
         {
            String label = childLabel.substring (parentLabel.length());
            while (label.endsWith (separator))
               label = label.substring (0, label.length() - separator.length());
            child.setLabel (label);
         }
      }

      /*
      if (DEBUG) System.out.println ("  add " + child.getKey() +
                                     " into " + parent.getKey() +
                                     " as " + child.toString());
       */

      // TBD sorted
      int position;
      for (position = 0; position < parent.getChildCount(); position++)
      {
         String label = ((AutoTreeNode) parent.getChildAt (position)).getLabel();
         if (label.compareTo (child.getLabel()) > 0)
            break;
      }
      insertNodeInto (child, parent, position);

      // cache the nodes in a hash table for easy access
      pathToNode.put (child.getKey(), child);

      // if (DEBUG) System.out.println (" # = " + parent.getChildCount());

      return child;
   }

   protected void insertBranch (final AutoTreeNode rootNode,
                                final AutoTreeNode match,
                                final AutoTreeNode node,
                                final String branchName)
   {
      // if (DEBUG) System.out.println ("> insertBranch: " + branchName);

      AutoTreeNode branch;

      // TBD: should we compare branch key instead of name here?
      if (forceLeaves || !branchName.equals (match.getKey()))
      {
         // if (DEBUG) System.out.println ("  remove: " + match);
         AutoTreeNode parent = (AutoTreeNode) match.getParent();
         removeNodeFromParent (match);

         Object key = getKey (rootNode, branchName, true);
         branch = new AutoTreeNode (null, branchName, branchName, key);

         insert (parent, branch, false);
         insert (branch, match, match.isLeaf());
      }
      else
         branch = match;

      insert (branch, node, true);
   }

   /**
    * API methods to add a node into the model.  Branches are added as
    * needed. */

   public AutoTreeNode add (final Object obj)
   {
      return add (obj, obj.toString());
   }

   public AutoTreeNode add (final Object obj, final String label)
   {
      return addInto ((AutoTreeNode) getRoot(), obj, label);
   }

   public AutoTreeNode addInto (final AutoTreeNode rootNode, final Object obj)
   {
      return addInto (rootNode, obj, obj.toString());
   }

   public AutoTreeNode addInto (final AutoTreeNode rootNode, final Object obj, 
                                final String label)
   {
      String name = obj != null ? obj.toString() : label;
      return addInto (rootNode, obj, name, label);
   }

   public AutoTreeNode addInto (final AutoTreeNode rootNode,
                                final Object obj,
                                final String name,
                                final String label)
   {
      Object key = getKey (rootNode, name, false);
      return addInto (rootNode, obj, name, label, key);
   }

   public AutoTreeNode addInto (final AutoTreeNode rootNode,
                                final Object obj,
                                final String name,
                                final String label,
                                final Object key)
   {
      AutoTreeNode node;

      // if (DEBUG) System.out.println ("\naddInto " + rootNode + ": " + key + " as " + label);

      // if the tree is empty, don't bother searching, just add it
      if (rootNode.getChildCount() == 0)
      {
         node = new AutoTreeNode (obj, name, label, key);
         return insert (rootNode, node, true);
      }

      // check if the given object is already in the tree (if so, skip it)
      node = findNode (key);
      if (node != null)
      {
         // if (DEBUG) System.out.println ("ignoring duplicate node: " + key);
         return node;
      }

      // if a branch of the same name is found, add the node to the branch
      AutoTreeNode branch = findNode (key + separator);
      if (branch != null)
      {
         node = new AutoTreeNode (obj, name, label, key);
         return insert (branch, node, true);
      }

      node = new AutoTreeNode (obj, name, label, key);

      TokenizedString ts = new TokenizedString (node.getName(), separator);
      AutoTreeNode match = findTarget (rootNode, node, ts);

      if (okToInsert (rootNode, match, ts))
         insert (match, node, true); // add new node to the parent
      else
         insertBranch (rootNode, match, node, getCommonPrefix (node, match));

      return node;
   }

   private boolean okToInsert (final AutoTreeNode rootNode,
                               final AutoTreeNode match,
                               final TokenizedString ts)
   {
      boolean ok = false;
      String parentName = ts.getAllButLast();

      if (match.isLeaf())
         ok = false;
      else if (match == rootNode)
         ok = true;
      else if (parentName == null)
         ok = true;
      else if (match.getName().equals (parentName))
         ok = true;
      else if (parentName.startsWith (match.getName() + separator))
         ok = true;

      // for multiple-embedded token separators
      else if (match.getName().endsWith (separator))
      {
         TokenizedString m = new TokenizedString (match.getName(), separator);
         if (m.toString().equals (parentName))
            ok = true;
      }

      /*
      if (DEBUG)
      {
         System.out.println ("okToInsert: " + ok);
         System.out.println ("  is Branch? = " + !match.isLeaf());
         System.out.println ("  root       = " + rootNode);
         System.out.println ("  match      = " + match.getName());
         System.out.println ("  parentName = " + parentName);
      }
      */

      return ok;
   }

   /**
    * The main API method to remove a leaf node from the model.
    * Branches are collapsed as needed. */

   public void remove (final AutoTreeNode rootNode, final String name)
   {
      remove (getKey (rootNode, name, false));
   }

   public void remove (final Object key)
   {
      remove (findNode (key));
      pathToNode.remove (key);
   }

   public void remove (final MutableTreeNode node)
   {
      if (node != null && node.isLeaf())
      {
         MutableTreeNode parent = (MutableTreeNode) node.getParent();
         removeNodeFromParent (node);
         if (parent.getChildCount() == 1)
            promoteNode ((AutoTreeNode) (parent.getChildAt (0)));
      }
   }

   /** Find the node associated with the given object. */

   public AutoTreeNode findNode (final Object key)
   {
      /*
      if (DEBUG)
      {
         System.out.println ("\npathToNode: " + pathToNode.size());
         for (Object k : pathToNode.keySet())
            System.out.println ("  " + k);
      }
      */

      AutoTreeNode node = (AutoTreeNode) pathToNode.get (key);
      // if (DEBUG) System.out.println ("findNode " + key + " -> " + node);
      return node;
   }

   /**
    * Find the node in the tree at which to insert the given object.
    * Note that this may return a branch where the object belongs, or
    * a node (leaf or branch) where a new branch needs to be added
    * first.
    *
    * For example (using the test data in the main), if object is
    * "Animal/Canine/Wolf", this method will find
    * "Animal/Canine/Dog/Labrador" (even though that is a leaf, and
    * "Animal/Canine" does not yet exist), and not "Animal" (an
    * already existing branch).  */

   AutoTreeNode findTarget (final AutoTreeNode rootNode,
                            final AutoTreeNode node,
                            final TokenizedString ts)
   {
      // if (DEBUG) System.out.println ("findTarget " + ts + " in " + rootNode);
      AutoTreeNode branch = findBranch (rootNode, ts);
      AutoTreeNode relative = findRelative (branch, ts);

      String branchName = branch.getName();
      String relativeName = relative.getName();
      String targetName = ts.getAllButLast();

      int branchDepth   = getDepth (branchName, targetName);
      int relativeDepth = getDepth (relativeName, targetName);

      if (branchDepth >= relativeDepth)
      {
         // if (DEBUG) System.out.println ("> target branch found: " + branch.getKey());
         return branch;
      }
      // if (DEBUG) System.out.println ("> target relative found: " + relative.getKey());
      return relative;
   }

   // only consider the relevant part of the name

   int getDepth (final String name, final String targetName)
   {
      // if (DEBUG) System.out.println ("getDepth (\"" + name +
      //                                "\", \"" + targetName + "\")"); // TBD
      if (name == null)
         return -1;
      if (targetName == null)
         return name.length();

      int min = Math.min (name.length(), targetName.length());
      int d = 0;
      while (d < min && name.charAt (d) == targetName.charAt (d))
         d++;
      String relevant = name.substring (0, d);
      // if (DEBUG) System.out.println ("  relevant: " + relevant);
      if (d < min)
      {
         while (relevant.length() > 0 && !relevant.endsWith (separator))
            relevant = name.substring (0, --d);
         // if (DEBUG) System.out.println ("  relevant: " + relevant);
      }
      if (relevant.endsWith (separator))
         d -= separator.length(); // ignore trailing separator
      // if (DEBUG) System.out.println ("  depth: " + d);

      return d;
   }

   /**
    * Find the deepest branch which should contain the given node.
    * For example, check "A/B/C", then "A/B", etc.  Cannot return
    * null.  In the "worst" case, this method will return root. */

   AutoTreeNode findBranch (final AutoTreeNode rootNode, final TokenizedString ts)
   {
      // if (DEBUG) System.out.println ("findBranch " + ts + " in " + rootNode);

      // find a branch into (or under) which the given node should be put
      Object key = getKey (rootNode, ts.toString(), true);
      // if (DEBUG) System.out.println ("  getKey = " + key);

      AutoTreeNode branch = findNode (key);
      if (branch != null)
      {
         // if (DEBUG) System.out.println ("  branch = " + branch.getKey());
         return branch;
      }

      TokenizedString parent = ts.withoutLast();
      if (parent != null)
         return findBranch (rootNode, parent);

      // if (DEBUG) System.out.println ("  branch -> " + rootNode.getKey());
      return rootNode;
   }

   /**
    * Find the nearest relative which should contain the given node.
    * For example, check "A/B/C", then "A/B", etc.  Cannot return
    * null.  In the "worst" case, this method will return root. */

   @SuppressWarnings("unchecked")
   AutoTreeNode findRelative (final AutoTreeNode rootNode, final TokenizedString ts)
   {
      String pattern = ts.toString();
      // if (DEBUG) System.out.println ("findRel " + pattern + " in " + rootNode);

      Enumeration<DefaultMutableTreeNode> children = rootNode.children();
      while (children.hasMoreElements())
      {
         AutoTreeNode node = (AutoTreeNode) children.nextElement();
         if (node.getName().equals (pattern) ||
             node.getName().startsWith (pattern + separator))
         {
            // if (DEBUG) System.out.println (" -> " + node);
            return node;      // relative found
         }
      }

      // recursively strip the last token and try again
      TokenizedString parent = ts.withoutLast();
      if (parent != null && !parent.toString().equals (rootNode.toString()))
         return findRelative (rootNode, parent);

      // if (DEBUG) System.out.println (" -> " + rootNode);
      return rootNode;
   }

   @SuppressWarnings("unchecked")
   void promoteNode (final AutoTreeNode node)
   {
      // if (DEBUG) System.out.println ("promoting " + node);

      AutoTreeNode parent = (AutoTreeNode) node.getParent();

      if (node.isLeaf())
         parent.setUserObject (node.getUserObject());
      else
      {
         String pack = parent.getUserObject() + separator +
            node.getUserObject();
         parent.setUserObject (pack);
         // promote the only-child nodes
         Enumeration<DefaultMutableTreeNode> children = node.children();
         List<AutoTreeNode> childrenToPromote = new ArrayList<AutoTreeNode>();
         while (children.hasMoreElements())
            childrenToPromote.add ((AutoTreeNode) children.nextElement());
         for (AutoTreeNode child : childrenToPromote)
         {
            // if (DEBUG) System.out.println ("  " + child);
            parent.add (child);
         }
      }

      pathToNode.remove (node.getKey());
      remove (node);
   }

   public void outline (final PrintStream out) // for debugging
   {
      TreeUtils.outline (out, (DefaultMutableTreeNode) getRoot());
   }

   public List<AutoTreeNode> findMatches (final String searchString)
   {
      List<AutoTreeNode> foundList = new ArrayList<AutoTreeNode>();
      findMatches ((AutoTreeNode) getRoot(), searchString, foundList);
      return foundList;
   }

   @SuppressWarnings("unchecked")
   public List<AutoTreeNode> findMatches (final AutoTreeNode node,
                                          final String searchString,
                                          final List<AutoTreeNode> foundList)
   {
      String label = node.getLabel();
      if (label != null && label.indexOf (searchString) != -1)
         foundList.add (node);

      Enumeration<DefaultMutableTreeNode> items = node.children();
      while (items.hasMoreElements())
         findMatches ((AutoTreeNode) items.nextElement(), searchString, foundList);

      return foundList;
   }

   public static void show (final JFrame frame, final AutoTree model,
                            final String title, final String header,
                            final int optionType)
   {
      JTree tree = new JTree (model);
      tree.putClientProperty ("JTree.lineStyle", "Angled");
      tree.setShowsRootHandles (true);
      tree.setRootVisible (false);
      tree.setEditable (false);
      JPanel panel = new JPanel (new BorderLayout());
      if (header != null)
      {
         JLabel label = new JLabel (header);
         panel.add (label, BorderLayout.NORTH);
      }
      JScrollPane scroll = new JScrollPane (tree); 
      scroll.setPreferredSize (new Dimension (500, 200));
      panel.add (scroll, BorderLayout.CENTER);
      JOptionPane.showMessageDialog (frame, panel, title, optionType, null);
   }   

   public static void main (final String[] args)
   {
      AutoTree tree = new AutoTree();
      // AutoTree.DEBUG = true;

      AutoTreeNode root = (AutoTreeNode) tree.getRoot();
      root.setName ("My Root");
      root.setLabel ("My Root");

      tree.addInto (root, "Animal/Cat/Cheshire");
      tree.addInto (root, "Animal/Cat/Persian");
      tree.addInto (root, "Animal/Cattle/Auroch");
      tree.addInto (root, "Animal/Canine/Dog/Retriever");
      tree.addInto (root, "Animal/Canine/Dog/Labrador");
      tree.addInto (root, "Animal/Canine/Wolf");
      tree.addInto (root, "Animal/Canine"); // test leaf matching branch
      tree.addInto (root, "Animal");
      tree.addInto (root, "Animal");      // should fail, already in tree

      tree.addInto (root, "/LEADING");
      tree.addInto (root, "TRAILING/");
      tree.addInto (root, "DOUBLE//EMBEDDED");
      tree.addInto (root, "DOUBLE//TWO");
      tree.addInto (root, "DOUBLE//THREE");
      tree.addInto (root, "DEEP//DOUBLE//EMBEDDED");
      tree.addInto (root, "DEEP//DOUBLE//TWO");
      tree.addInto (root, "DEEP//DOUBLE//THREE");

      tree.addInto (root, "2/3/a/A");
      tree.addInto (root, "2/3/a/B");
      tree.addInto (root, "2/3/b/A");
      tree.addInto (root, "2/3/b/B");

      tree.addInto (root, "2/4/a/A");
      tree.addInto (root, "2/4/a/B");
      tree.addInto (root, "2/4/b/A");
      tree.addInto (root, "2/4/b/B");

      tree.addInto (root, "Project/9004/204/TEST");
      tree.addInto (root, "Project/2067/TEST");
      tree.addInto (root, "Project/9004/201/TEST");
      tree.addInto (root, "Project/9004/203/TEST");

      System.out.println ("\n" + tree.getClass().getName() + ":\n");
      tree.outline (System.out);
      
      System.out.println ("\nRemoving Wolf and Canine...\n");
      tree.remove (root, "Animal/Canine/Wolf");
      tree.remove (root, "Animal/Canine"); // remove leaf
      // TBD: should change "Canine" branch to "Canine/Dog"
      tree.outline (System.out);

      // test with forceLeaves = false

      tree = new AutoTree();
      tree.setSimpleNames (false);
      tree.setForceLeaves (false);
      tree.setSeparator (".");

      root = (AutoTreeNode) tree.getRoot();
      root.setName ("My Root");
      root.setLabel ("My Root");

      tree.addInto (root, "1");
      tree.addInto (root, "1.a");
      tree.addInto (root, "1.a.1");
      tree.addInto (root, "1.a.2");
      tree.addInto (root, "1.a.3");
      tree.addInto (root, "1.b");
      tree.addInto (root, "1.c");
      tree.addInto (root, "2");
      // tree.addInto (root, "2.a");
      tree.addInto (root, "2.a.1");
      tree.addInto (root, "2.a.2");
      tree.addInto (root, "2.a.3");
      tree.addInto (root, "2.b");
      tree.addInto (root, "2.c");

      System.out.println ("\nForceLeaves = false\n");
      tree.outline (System.out);
   }
}
