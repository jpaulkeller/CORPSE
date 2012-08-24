package lotro.models;

import java.util.ArrayList;
import java.util.List;

public enum Profession
{
   Unknown (false),
   Farmer (false),
   Forester (false),
   Prospector  (false),
   Cook (true),
   Jeweller (true),
   Metalsmith (true),
   Scholar (true),
   Tailor  (true),
   Weaponsmith (true),
   Woodworker (true);
   
   private boolean crafter; // true for professions which craft items
   
   private Profession (final boolean crafter)
   {
      this.crafter = crafter;
   }
   
   public boolean isCrafter()
   {
      return crafter;
   }
   
   public static List<Profession> getCrafters()
   {
      List<Profession> crafters = new ArrayList<Profession>();
      for (Profession p : Profession.values())
         if (p.isCrafter())
            crafters.add (p);
      return crafters;
   }

   public static Profession parse (final String s)
   {
      if (s != null)
         for (Profession p : Profession.values())
            if (s.equalsIgnoreCase (p.toString()))
               return p;

      System.err.println ("Invalid Profession: " + s);
      return Profession.Unknown;
   }
}
