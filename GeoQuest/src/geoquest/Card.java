package geoquest;

public abstract class Card
{
   public abstract String getName();
   
   public abstract String getText();
   
   public String getTextForImage()
   {
      String s = getText();
      s = s.replace("&nbsp;", " ");
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
