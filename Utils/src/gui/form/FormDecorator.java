package gui.form;

import gui.form.valid.StatusEvent;
import gui.form.valid.StatusListener;
import gui.form.valid.Validator;

import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Adapter class for FormItem decorators. This class should override
 * every method in the FormItem interface, and delegate to the
 * decorated item. */

public abstract class FormDecorator implements FormItem
{
   private static final long serialVersionUID = 1L;
   
   private FormItem item;
   
   /**
    * If this constructor is used, setItem() must be called before
    * using any other method.  Sub-classes may set the item attribute
    * directly. */
   
   public FormDecorator()
   {
   }
   
   public FormDecorator (final FormItem itemToDecorate)
   {
      this.item = itemToDecorate;
   }

   public void setItem (final FormItem itemToDecorate)
   {
      this.item = itemToDecorate;
   }
   
   /** Return the decorated item. */
   
   public FormItem getItem()
   {
      return item;
   }
      
   // The following methods implement FormItem by delegating to the
   // decorated item.

   public JComponent getComponent()
   {
      return item.getComponent();
   }

   public JPanel getTitledPanel()
   {
      return item.getTitledPanel();
   }

   public void setInitialValue (final Object value)
   {
      item.setInitialValue (value);
   }

   public Object getInitialValue()
   {
      return item.getInitialValue();
   }

   public void setValue (final Object value)
   {
      item.setValue (value);
   }

   public Object getValue()
   {
      return item.getValue();
   }

   public void apply()
   {
      item.apply();
   }

   public void restore()
   {
      item.restore();
   }

   public void setLabel (final String label)
   {
      item.setLabel (label);
   }

   public String getLabel()
   {
      return item.getLabel();
   }

   public void setEnabled (final boolean enable)
   {
      item.setEnabled (enable);
   }

   public boolean isEnabled()
   {
      return item.isEnabled();
   }

   public void setEditable (final boolean editable)
   {
      item.setEditable (editable);
   }
   
   public boolean isEditable()
   {
      return item.isEditable();
   }
   
   public boolean isVisible()
   {
      return item.isVisible();
   }
   
   public void setToolTipText (final String tip)
   {
      item.setToolTipText (tip);
   }

   public boolean isValid()
   {
      return item.isValid();
   }

   public boolean isValid (final Object value)
   {
      return item.isValid (value);
   }

   public final Validator getValidator()
   {
      return item.getValidator();
   }

   public void setValidator (final Validator validator)
   {
      item.setValidator (validator);
   }

   public void removeValidator (final Validator validator)
   {
      item.removeValidator (validator);
   }

   public void setNullValidity (final boolean status)
   {
      item.setNullValidity (status);
   }
   
   public void addValueChangeListener (final ValueChangeListener listener)
   {
      item.addValueChangeListener (listener);
   }

   public void removeValueChangeListener (final ValueChangeListener listener)
   {
      item.removeValueChangeListener (listener);
   }

   public void fireValueChanged()
   {
      item.fireValueChanged();
   }

   public boolean hasChanged()
   {
      return item.hasChanged();
   }

   public void addStatusListener (final StatusListener listener)
   {
      item.addStatusListener (listener);
   }

   public void removeStatusListener (final StatusListener listener)
   {
      item.removeStatusListener (listener);
   }

   public void addFocusListener (final FocusListener listener)
   {
      item.addFocusListener (listener);
   }

   public void removeFocusListener (final FocusListener listener)
   {
      item.removeFocusListener (listener);
   }

   public boolean hasFocus()
   {
      return item.hasFocus();
   }

   public String convertToHTML()
   {
      return item.convertToHTML();
   }

   public void stateChanged (final StatusEvent event)
   {
      item.stateChanged (event);
   }
}
