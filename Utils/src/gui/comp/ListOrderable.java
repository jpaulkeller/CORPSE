package gui.comp;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.ListModel;
import utils.ImageTools;

public class ListOrderable<E> extends JPanel
{
   private static final long serialVersionUID = 0;
   
   private JList list;
   private ListModel<E> model;
   
   private TitledBorder border;
   private JButton top, up, down, end;
   private transient ActionListener listener;

   public ListOrderable (final JList list, final String title)
   {
      this.list = list;
      this.model = (ListModel<E>) list.getModel();
      
      setLayout (new BorderLayout());

      this.listener = new ButtonHandler();

      up   = makeButton ("up",   "flow/VCRUp.gif",     "up one position");
      down = makeButton ("down", "flow/VCRDown.gif",   "down one position");
      top  = makeButton ("top",  "flow/VCRTop.gif",    "to the top of the list");
      end  = makeButton ("end",  "flow/VCRBottom.gif", "to the end of the list");

      JPanel control = new JPanel (new GridLayout (4, 1));
      control.add (top);
      control.add (up);
      control.add (down);
      control.add (end);

      border = BorderFactory.createTitledBorder (title);
      setBorder (border);

      add (new JScrollPane (list), BorderLayout.CENTER);
      add (control, BorderLayout.EAST);

      enableButtons();
      list.addListSelectionListener (new SelectionListener());
   }
   
   private JButton makeButton (final String command, final String icon, final String tip)
   {
      JButton button = new JButton();
      button.setActionCommand (command);
      button.setIcon (ImageTools.getIcon ("icons/20/" + icon));
      button.addActionListener (listener);
      button.setToolTipText ("Move selected entry " + tip);
      return button;
   }

   public JList getList()
   {
      return list;
   }
   
   public void setTitle (final String title)
   {
      border.setTitle (title);
      repaint();
   }

   private void enableButtons()
   {
      boolean canUp = false; 
      boolean canDown = false; 
      
      if (model.size() > 1)
      {
         int[] selected = list.getSelectedIndices();
         if (selected.length == 1)
         {
            if (selected[0] > 0) // first
               canUp = true;
            if (selected[0] < model.size() - 1) // last
               canDown = true;
         }
      }
      
      top.setEnabled (canUp);
      up.setEnabled (canUp);
      down.setEnabled (canDown);
      end.setEnabled (canDown);
   }
   
   private class ButtonHandler implements ActionListener
   {
      @Override
      public void actionPerformed (final ActionEvent e)
      {
         int index = list.getSelectedIndex();
         if (index >= 0)
         {
            E element = (E) list.getSelectedValue();
            model.remove (index);
            
            String cmd = e.getActionCommand();
            if (cmd.equals ("top"))
               index = 0;
            else if (cmd.equals ("up"))
               index -= 1;
            else if (cmd.equals ("down"))
               index += 1;
            else // end
               index = model.size();
            
            model.add (index, element);
            list.setSelectedIndex (index);
         }
      }
   }

   private class SelectionListener implements ListSelectionListener
   {
      @Override
      public void valueChanged (final ListSelectionEvent arg0)
      {
         enableButtons();
      }
   }

   public static void main (final String[] args) // for testing
   {
      String[] objects = new String[]
      { "Bell Peppers", "Green Beans", "Snow Peas", "Corn", "Eggplant",
               "Onion", "Tomato", "Cucumber", "Raspberry" };
      ListModel<String> model = new ListModel<String> (new ArrayList<String>());
      for (String s : objects)
         model.add (s);
      
      ListOrderable<String> list =
         new ListOrderable<String> (new JList (model), "Vegetables");
      ComponentTools.open (list, "ListOrderable");
   }
}
