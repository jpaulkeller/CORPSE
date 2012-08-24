package gui;

import java.awt.Color;

public class ColorField
{
   private Object value;
   private Color color;
   
   public ColorField (final Object value)
   {
      this.value = value;
   }

   public void setValue (final Object value)
   {
      this.value = value;
   }
   
   public Object getValue()
   {
      return value;
   }
   
   public double getDouble()
   {
      if (value == null)
         return 0;
      if (value instanceof Number)
         return ((Number) value).doubleValue();
      try
      {
         return Double.parseDouble (value.toString());
      }
      catch (NumberFormatException x)
      {
         System.err.println ("ValidatedField: " + x);
      }
      return -1;
   }
   
   public void setColor (final Color color)
   {
      this.color = color;
   }
   
   public Color getColor()
   {
      return color;
   }
   
   @Override
   public String toString()
   {
      return value != null ? value.toString() : "";  
   }
}
