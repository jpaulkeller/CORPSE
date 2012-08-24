package gui.tree;

import gui.ClipboardHelper;
import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import utils.PrintUtil;

public class WarningTree
{
   private Component parent;
   private AutoTree model;
   private JTree view;
   private JPanel panel;
   
   public WarningTree (final Component parent, final AutoTree warnings)
   {
      this.parent = parent;
      this.model = warnings;
      
      view = new JTree (model);
      view.putClientProperty ("JTree.lineStyle", "Angled");
      view.setShowsRootHandles (true);
      view.setRootVisible (false);
      view.setEditable (false);
      // view.setCellRenderer (new TreeRenderer()); TBD

      supportCopy();
      
      // model.outline (System.out); // trace
   }

   public AutoTree getModel()
   {
      return model;
   }
   
   public JTree getView()
   {
      return view;
   }
   
   public void show (final String title)
   {
      if (panel == null)
      {
         ActionListener listener = new ButtonListener();
         JButton copyButton = ComponentTools.makeButtonNarrow
            ("Copy", "icons/20/gui/ClipboardPaste.gif", true, listener,
             "Copy the selected warnings to the clipboard");
         JButton printButton = ComponentTools.makeButtonNarrow 
            ("Print", "icons/20/objects/Printer.gif", true, listener,
             "Print the tree (as it appears now)");
      
         JPanel buttons = new JPanel();
         buttons.add (copyButton);
         buttons.add (printButton);
         
         JScrollPane scroll = new JScrollPane (view); 
         scroll.setPreferredSize (new Dimension (500, 400));
         
         panel = new JPanel (new BorderLayout());
         panel.add (scroll, BorderLayout.CENTER);
         panel.add (buttons, BorderLayout.SOUTH);
      }
      JOptionPane.showMessageDialog (parent, panel, title, 
                                     JOptionPane.PLAIN_MESSAGE, null);      
   }
   
   @SuppressWarnings("unchecked")
   private void copy()
   {
      StringBuilder sb = new StringBuilder();

      // copy just the selected ones (if there are any)
      if (view.getSelectionCount() > 0)
         for (TreePath tp : view.getSelectionPaths())
         {
            AutoTreeNode node = (AutoTreeNode) tp.getLastPathComponent();
            if (node.isLeaf() && node.getUserObject() != null)
               sb.append (node.getUserObject() + "\n");
            else // select all children (TBD: recurse)
               for (AutoTreeNode leaf : node.getLeaves())
                  sb.append (leaf.getUserObject() + "\n");
         }
      else
      {
         DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
         Enumeration<DefaultMutableTreeNode> en = root.preorderEnumeration();
         while (en.hasMoreElements())
         {
            DefaultMutableTreeNode node = en.nextElement();
            if (node.toString() != null)
            {
               for (int i = 0, level = node.getLevel(); i < level; i++)
                  sb.append ("  ");
               sb.append (node.toString() + "\n");
            }
         }
      }

      ClipboardHelper.copyString (sb);
   }

   private void supportCopy()
   {
      KeyStroke controlC = KeyStroke.getKeyStroke (KeyEvent.VK_C, Event.CTRL_MASK);
      view.getInputMap().put (controlC, "none"); // stop JTree from consuming

      Action action = new CopyAction();
      Object name = action.getValue (Action.NAME);
      view.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW).put (controlC, name);
      view.getActionMap().put (name, action);
   }
   
   class CopyAction extends AbstractAction
   {
      public CopyAction()
      {
         super ("CopyAction");
      }
      
      public void actionPerformed (final ActionEvent e)
      {
         copy();
      }
   }
   
   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         String cmd = e.getActionCommand();
         if (cmd.equals ("Copy"))
            copy();
         else if (cmd.equals ("Print"))
            PrintUtil.print (parent, getView());   
         else
            System.err.println ("Unsupported command: " + cmd);
      }
   }
}
