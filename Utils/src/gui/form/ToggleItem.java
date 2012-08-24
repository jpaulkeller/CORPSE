package gui.form;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import utils.ImageTools;

/**
 * ToggleItem objects are simple on/off check box form items for
 * setting boolean data.
 */
public class ToggleItem extends FormItemAdapter implements ItemListener
{
   private static final long serialVersionUID = 7;

   private JCheckBox checkBox; // main component

   private String textOn;
   private String textOff;
   
   public ToggleItem (final String text, final boolean state)
   {
      checkBox = new JCheckBox (text, state);
      initialize();
      setInitialValue (Boolean.valueOf (state));
   }
   
   public ToggleItem (final String label)
   {
      this (label, false);
      setLabel (label);
   }
   
   public ToggleItem (final String label, final String text)
   {
      this (label, text, false);
   }
   
   public ToggleItem (final String label, final String text, final boolean state)
   {
      this (text, state);
      setLabel (label);
   }
   
   void initialize()
   {
      setIcon ("icons/20/markers/XRed.gif");
      setSelectedIcon ("icons/20/markers/Check.gif");
      checkBox.addItemListener (this);
   }
   
   public void setText (final String txtOn, final String txtOff)
   {
      this.textOn  = txtOn;
      this.textOff = txtOff;
      checkBox.setText (isSelected() ? textOn : textOff);
   }
   
   @Override
   public void setValue (final Object value)
   {
      if (value == null)
         checkBox.setSelected (false);
      else if (value instanceof Boolean)
         checkBox.setSelected ((Boolean) value);
      else
         checkBox.setSelected (!value.equals(""));
   }

   @Override
   public Object getValue() 
   {
      return Boolean.valueOf (checkBox.isSelected());
   }

   public boolean isSelected() 
   {
      return (checkBox.isSelected());
   }

   public void setSelected (final boolean selected) 
   {
      checkBox.setSelected (selected);
   }

   public void setSelectedIcon (final Icon icon) 
   {
      checkBox.setSelectedIcon (icon);
   }

   public void setSelectedIcon (final String name) 
   {
      ImageIcon icon = ImageTools.getIcon (name); 
      if (icon != null)
         checkBox.setSelectedIcon (icon);
   }

   public void setIcon (final Icon icon) 
   {
      checkBox.setIcon (icon);
   }

   public void setIcon (final String name)
   {
      ImageIcon icon = ImageTools.getIcon (name); 
      if (icon != null)
         checkBox.setIcon (icon);
   }

   @Override
   public JComponent getComponent()
   {
      return checkBox;
   }
   
   public void addActionListener (final ActionListener listener)
   {
      checkBox.addActionListener (listener);
   }
   
   public void removeActionListener (final ActionListener listener)
   {
      checkBox.removeActionListener (listener);
   }
   
   // implement ItemListener   
   public void itemStateChanged (final ItemEvent e)
   {
      if (textOff != null)
         checkBox.setText (isSelected() ? textOn : textOff);
      fireValueChanged();
   }
   
   public static void main (final String[] args) // for testing
   {
      ToggleItem item = new ToggleItem ("Toggle Item", "Click to Toggle", true);
      item.setText ("ON", "OFF");
      item.test();
   }
}
