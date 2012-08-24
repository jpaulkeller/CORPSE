package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;

/** Supports a button group that allows multiple selected buttons. */

public class ButtonGroup implements ActionListener
{
   private List<AbstractButton> buttonList = new ArrayList<AbstractButton>();
   private Set<AbstractButton> buttons = new HashSet<AbstractButton>();
   
   private int maxSelectable; // 0 means no max
   private int selected;
   
   public void setMaxSelectable (final int max)
   {
      maxSelectable = max;
   }

   public void add (final AbstractButton button)
   {
      button.addActionListener (this);
      buttonList.add (button);
      buttons.add (button);
   }
   
   public List<AbstractButton> getButtons()
   {
      return buttonList;
   }
   
   public void clear()
   {
      for (AbstractButton button : buttons)
         button.removeActionListener (this);
      buttons.clear();
   }

   public void setEnabled (final boolean enabled)
   {
      for (AbstractButton button : buttons)
         button.setEnabled (enabled);
   }
   
   public void setSelected (final boolean selected)
   {
      for (AbstractButton button : buttons)
         button.setSelected (selected);
   }
   
   public int getSelectedCount()
   {
      int count = 0;
      for (AbstractButton button : buttons)
         if (button.isSelected())
            count++;
      return count;
   }
   
   /** Returns the first selected button. */
   
   public AbstractButton getSelected()
   {
      for (AbstractButton button : buttons)
         if (button.isSelected())
            return button;
      return null;
   }
   
   public void actionPerformed (final ActionEvent e)
   {
      AbstractButton button = (AbstractButton) e.getSource();
      
      if (maxSelectable > 0)
      {
         if (button.isSelected())
         {
            selected++;

            // too many selected; clear all and start over
            if (selected > maxSelectable)
            {
               setSelected (false);
               button.setSelected (true);
               selected = 1;
            }
               
            if (maxSelectable > 1 && selected == maxSelectable) // just at max
               for (AbstractButton b : buttons)
                  if (!b.isSelected())
                     b.setEnabled (false);
         }
         else
         {
            selected--;
            if (maxSelectable > 1 && selected == maxSelectable - 1) // just under max
               for (AbstractButton b : buttons)
                  if (!b.isEnabled())
                     b.setEnabled (true);
         }
      }
   }
}
