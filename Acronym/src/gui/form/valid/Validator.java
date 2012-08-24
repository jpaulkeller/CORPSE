package gui.form.valid;

import gui.form.ValueChangeListener;

import java.awt.Color;
import java.io.Serializable;

/** 
 * This interface provides an API for form item validators.
 *
 * @see ValidationAdapter */

public interface Validator extends ValueChangeListener, Serializable
{
   Color INVALID_COLOR = new Color (255, 220, 220);

   boolean initialize (String arguments);
   void setNullValidity (boolean status);

   boolean isValid (Object value);
   boolean validate (Object source, Object value);

   void addStatusListener (Object source, StatusListener listener);
   void removeStatusListener (Object source, StatusListener listener);
}
