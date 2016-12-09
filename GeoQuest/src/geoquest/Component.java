package geoquest;

import java.util.HashMap;
import java.util.Map;

public abstract class Component
{
   protected static final Map<String, String> NAMES_FR = new HashMap<>();
   protected static final Map<String, CharSequence> TEXT_FR = new HashMap<>();
   protected static final ImageStats stats = new ImageStats();
   
   protected String name;
   protected CharSequence text;

   public String getNameEnglish()
   {
      return name;
   }
   
   public String getName()
   {
      if (Factory.LANGUAGE == Language.FRENCH)
         return NAMES_FR.get(name);
      return name;
   }

   public CharSequence getText()
   {
      if (Factory.LANGUAGE == Language.FRENCH)
         return TEXT_FR.get(name);
      return text;
   }
   
   public String getTextForImage()
   {
      String s = getText().toString();
      s = s.replace("<em class=", "<");
      return s;
   }
   
   public String getIcon()
   {
      return null;
   }

   public static void addFR(final String cardNameEN, final String cardNameFR, final String cardTextFR)
   {
      NAMES_FR.put(cardNameEN, cardNameFR);
      if (cardTextFR != null)
         TEXT_FR.put(cardNameEN, cardTextFR);
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
