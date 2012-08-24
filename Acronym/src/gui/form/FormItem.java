package gui.form;

import gui.form.valid.StatusListener;
import gui.form.valid.Validator;

import java.awt.event.FocusListener;
import java.io.Serializable;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * This interface provides an API for form component items.  Each form
 * item is a labelled data entry object.
 *
 * FormItems fire ValueChangeEvents whenever their value changes.
 *
 * FormItems know when they've changed, and generally visibly depict
 * this status.  For example, TextItems change their foreground color
 * to blue.
 *
 * FormItems can also be hooked to multiple Validators, and generally
 * visibly depict their validity status.  For example, items
 * containing invalid data usually have pink backgrounds.
 *
 * FormItems provide access to their main component via the
 * getComponent method.
 *
 * @see FormItemAdapter
 * @see FormPanel
 * @see Validator */

public interface FormItem extends StatusListener, Serializable
{
   JComponent getComponent();
   JPanel getTitledPanel();

   void setInitialValue (Object value);
   Object getInitialValue();
   void setValue (Object value);
   Object getValue();
   void apply();
   void restore();

   void setLabel (String label);
   String getLabel();

   void setEnabled (boolean enable);
   boolean isEnabled();

   void setEditable (boolean editable);
   boolean isEditable();
   
   boolean isVisible();

   void setToolTipText (String tip);

   boolean isValid();
   boolean isValid (Object value);
   Validator getValidator();
   void setValidator (Validator validator);
   void removeValidator (Validator validator);
   void setNullValidity (boolean status);

   void addValueChangeListener (ValueChangeListener listener);
   void removeValueChangeListener (ValueChangeListener listener);
   void fireValueChanged();
   boolean hasChanged();

   void addStatusListener (StatusListener listener);
   void removeStatusListener (StatusListener listener);

   void addFocusListener (FocusListener listener);
   void removeFocusListener (FocusListener listener);
   boolean hasFocus();

   /**
    * This method should return one or more table record <tr> entries.
    * Each entry should have two table data fields <td>. */
   String convertToHTML();
}
