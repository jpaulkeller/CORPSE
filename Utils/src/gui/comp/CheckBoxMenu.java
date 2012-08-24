package gui.comp;

import gui.ButtonGroup;
import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

public class CheckBoxMenu<T>
{
   private static final long serialVersionUID = 2;

   private static final int VSCROLL_INC = 8;

   private Map<String, T> map;
   private String heading;
   private int maxSelectable; // 0 means no max

   private List<T> selectedItems;
   private Map<T, JCheckBox> checkBoxes;
   private int cellWidth = 85;
   private JDialog checkBoxDialog;
   private JPanel checkBoxForm;

   public CheckBoxMenu (final Collection<T> items, final String heading)
   {
      setItems (items);
      setHeading (heading);
   }

   public void setItems (final Collection<T> items)
   {
      map = new HashMap<String, T>();
      checkBoxes = new LinkedHashMap<T, JCheckBox>();
      for (T obj : items)
      {
         String name = obj.toString();
         map.put (name, obj);
         JCheckBox box = new JCheckBox (name, false);
         checkBoxes.put (obj, box);
      }
      makeForm();
   }

   public void setHeading (final String h)
   {
      heading = h;
   }

   public void setMaxSelectable (final int max)
   {
      maxSelectable = max;
   }

   public void setSelected (final Collection<T> selected)
   {
      for (T obj : selected)
         checkBoxes.get (obj).setSelected (true);
   }
   
   public Collection<T> getSelected()
   {
      Collection<T> selected = new ArrayList<T>();
      for (JCheckBox box : checkBoxes.values())
         if (box.isSelected())
            selected.add (map.get (box.getText()));
      return selected;
   }
   
   private void makeForm()
   {
      checkBoxForm = new JPanel (new GridLayout (0, 1));
      for (JCheckBox checkBox : checkBoxes.values())
         checkBoxForm.add (checkBox);
   }

   public List<T> select (final Frame owner, final String title)
   {
      selectedItems = new ArrayList<T>();

      JPanel panel = getPanel (new Dimension (400, 200));
      JPanel btnPanel = createBtnPanel (new ActionListener()
      {
         public void actionPerformed (final ActionEvent event)
         {
            String action = event.getActionCommand();
            if (action.equals ("OK"))
               selectItems();
            if (action.equals ("ALL"))
               selectAllItems();
            if (action.equals ("CANCEL"))
               cancelSelection();
         }
      });
      panel.add (btnPanel, BorderLayout.SOUTH);
   
      checkBoxDialog = new JDialog (owner, title, true);
      checkBoxDialog.add (panel);
      checkBoxDialog.pack();
      ComponentTools.centerComponent (checkBoxDialog);

      for (JCheckBox box : checkBoxes.values())
         if (box.getPreferredSize().width > cellWidth)
            cellWidth = box.getPreferredSize().width; // determine the widest cell

      checkBoxDialog.setVisible (true);

      return selectedItems;
   }

   public JPanel getPanel (final Dimension prefSize)
   {
      if (maxSelectable > 0)
      {
         ButtonGroup group = new ButtonGroup();
         group.setMaxSelectable (maxSelectable);
         for (JCheckBox checkBox : checkBoxes.values())
            group.add (checkBox);
      }

      JPanel panel = new JPanel (new BorderLayout());
      
      Border up = BorderFactory.createRaisedBevelBorder();
      Border down = BorderFactory.createLoweredBevelBorder();
      Border border = BorderFactory.createCompoundBorder (up, down);
      panel.setBorder (border);
      
      if (heading != null)
      {
         JTextArea header = new JTextArea (heading);
         header.setBackground (null);
         header.setEditable (false);
         panel.add (header, BorderLayout.NORTH);
      }

      JScrollPane scroll = new JScrollPane (checkBoxForm);
      scroll.getVerticalScrollBar().setUnitIncrement (VSCROLL_INC);
      scroll.addComponentListener (new ResizeListener());

      scroll.setPreferredSize (prefSize);
      panel.add (scroll, BorderLayout.CENTER);

      return panel;
   }

   private void selectItems()
   {
      for (JCheckBox checkBox : checkBoxes.values())
         if (checkBox.isSelected())
            selectedItems.add (map.get (checkBox.getText()));

      if (checkBoxDialog != null)
      {
         checkBoxDialog.setVisible (false);
         checkBoxDialog.dispose();
      }
   }

   public void selectAllItems()
   {
      for (JCheckBox checkBox : checkBoxes.values())
         checkBox.setSelected (true);
   }

   private void cancelSelection()
   {
      if (checkBoxDialog != null)
      {
         checkBoxDialog.setVisible (false);
         checkBoxDialog.dispose();
      }
   }

   private JPanel createBtnPanel (final ActionListener listener)
   {
      JPanel panel = new JPanel();

      if (maxSelectable == 0 || checkBoxes.size() <= maxSelectable)
      {
         JButton all = new JButton ("Select All");
         all.setActionCommand ("ALL");
         all.addActionListener (listener);
         panel.add (all);
      }

      JButton ok = new JButton ("OK");
      ok.setActionCommand ("OK");
      ok.addActionListener (listener);
      panel.add (ok);

      JButton cancel = new JButton ("Cancel");
      cancel.setActionCommand ("CANCEL");
      cancel.addActionListener (listener);
      panel.add (cancel);

      return panel;
   }

   // set the number of columns based on current size of the scroll panel
   class ResizeListener extends ComponentAdapter
   {
      @Override
      public void componentResized (final ComponentEvent e)
      {
         JScrollPane scroll = (JScrollPane) e.getSource();
         Dimension dim = scroll.getSize();
         int width = dim.width - scroll.getVerticalScrollBar().getWidth();
         int columns = Math.max (1, (width / cellWidth) - 1);
         checkBoxForm.setLayout (new GridLayout (0, columns));
      }
   }

   public static void main (final String[] args)
   {
      List<Integer> values = new ArrayList<Integer>();
      for (int i = 1; i <= 100; i++)
         values.add (i);
      CheckBoxMenu<Integer> menu =
         new CheckBoxMenu<Integer> (values, "Select values");
      List<Integer> selected = menu.select (null, "CheckBoxMenu Test");
      for (int i : selected)
         System.out.println (i);
   }
}
