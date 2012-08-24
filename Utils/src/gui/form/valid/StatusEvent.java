package gui.form.valid;


import javax.swing.event.ChangeEvent;

/**
 * This event is fired by a Validator, as the validity status of
 * a value is initialized or changes.
 *
 * @see StatusListener
 * @see Validator */

public class StatusEvent extends ChangeEvent
{
   private static final long serialVersionUID = 1;

   private Object value;
   private boolean status;
   
   /**
    * Constructs a StatusEvent with a source.  This is for validators
    * which are used by multiple sources.
    *
    * @param source -- generally a FormItem
    * @param value -- the value which has been validated
    * @param status -- true if the value is valid */

   public StatusEvent (final Object source, final Object value, 
                       final boolean status)
   {
      super (source);
      this.value = value;
      this.status = status;
   }

   public Object getValue()
   {
      return value;
   }
   
   public boolean getStatus()
   {
      return status;
   }
}
