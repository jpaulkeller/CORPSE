package geoquest;

public abstract class Component
{
   public abstract String getName();
   
   public String getName(final Language language)
   {
      return getName();
   }
   
   public abstract String getText();

   public String getText(final Language language)
   {
      return getText();
   }
   
   public String getTextForImage(final Language language)
   {
      String s = getText(language);
      s = s.replace("<em class=", "<");
      return s;
   }
   
   public String getIcon()
   {
      return null;
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
