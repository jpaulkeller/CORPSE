package geoquest;

public abstract class Component
{
   public abstract String getName();
   
   public abstract String getText();
   
   public String getTextForImage()
   {
      String s = getText();
      s = s.replace("<em class=", "<");
      return s;
   }
   
   public int hashCode()
   {
      return getName().hashCode();
   }

   public String toString()
   {
      return getName();
   }
}
