package gui.form;

import java.util.EventObject;

public class ValueChangeEvent extends EventObject
{
   private static final long serialVersionUID = 1;

   private Object value;
   
   public ValueChangeEvent (final Object source, final Object value)
   {
      super (source);
      this.value = value;
   }

   public Object getValue()
   {
      return value;
   }
}
