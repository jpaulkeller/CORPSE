package gui.form.valid;


/** 
 * This interface provides an API for listeners who want to know when
 * the value of a form item changes validity status.
 *
 * @see StatusEvent
 * @see Validator */

public interface StatusListener
{
   void stateChanged (StatusEvent event);
}
