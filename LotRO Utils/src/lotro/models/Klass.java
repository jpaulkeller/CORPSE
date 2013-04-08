package lotro.models;

import java.util.SortedSet;
import java.util.TreeSet;

public enum Klass
{
   None ("", "FFFFFF", "black"),
   Burglar ("B", "000000", "white"),
   Captain ("Cpt", "77FFFF", "black"), // teal
   Champion ("Ch", "FBA017", "black"), // orange
   Guardian ("G", "CCCCCC", "black"), // light grey
   Hunter ("H", "FF0000", "white"), // red
   LoreMaster ("LM", "0000FF", "white"), // blue
   Minstrel ("M", "77FF77", "black"), // light green
   RuneKeeper ("RK", "FF00FF", "black"), // magenta
   Warden ("W", "AF7817", "white"), // tan
   Unknown ("?", "FFFFFF", "black"),
   
   Blackarrow ("BA", "000000", "white"),
   Defiler ("D", "DDDD44", "black"), // yellowish
   Reaver ("R", "FF1493", "white"), // deep pink
   Stalker ("S", "996600", "white"), // brown
   Warleader ("WL", "9B00AA", "white"), // dark magenta
   Weaver ("Wv", "7FFF00", "black"); // chartreuse
   
   public static final SortedSet<Klass> FREEPS = new TreeSet<Klass>();
   public static final SortedSet<Klass> CREEPS = new TreeSet<Klass>();
   
   static
   {
      FREEPS.add (Burglar);
      FREEPS.add (Captain);
      FREEPS.add (Champion);
      FREEPS.add (Guardian);
      FREEPS.add (Hunter);
      FREEPS.add (LoreMaster);
      FREEPS.add (Minstrel);
      FREEPS.add (RuneKeeper);
      FREEPS.add (Warden);
      
      CREEPS.add (Blackarrow);
      CREEPS.add (Defiler);
      CREEPS.add (Reaver);
      CREEPS.add (Stalker);
      CREEPS.add (Warleader);
      CREEPS.add (Weaver);
   }
   
   private String abbrev;
   private String bgColor;
   private String fgColor;
   
   Klass (final String abbrev, final String bgColor, final String fgColor)
   {
      this.abbrev = abbrev;
      this.bgColor = bgColor;
      this.fgColor = fgColor;
   }
   
   public String abbrev()
   {
      return abbrev;
   }
   
   public String getColorBG (final String prefix)
   {
      return prefix + bgColor;
   }
   
   public String getColorFG()
   {
      return fgColor;
   }
   
   @Override
   public String toString()
   {
      return this.equals(Klass.None) ? "" : super.toString();
   }
   
   public static Klass parse (final String name)
   {
      Klass klass = Klass.None;
      if (name != null)
      {
         for (Klass c : Klass.values())
            if (name.equalsIgnoreCase (c.toString()))
               return c;
         
         if (name.toLowerCase().contains ("burg"))
            klass = Klass.Burglar;
         else if (name.toLowerCase().contains ("capt"))
            klass = Klass.Captain;
         else if (name.toLowerCase().contains ("champ"))
            klass = Klass.Champion;
         else if (name.toLowerCase().contains ("guard"))
            klass = Klass.Guardian;
         else if (name.toLowerCase().contains ("hunter"))
            klass = Klass.Hunter;
         else if (name.toLowerCase().contains ("lore") && 
                  name.toLowerCase().contains ("master"))
            klass = Klass.LoreMaster;
         else if (name.toLowerCase().contains ("minst"))
            klass = Klass.Minstrel;
         else if (name.toLowerCase().contains ("rune") &&
                  name.toLowerCase().contains ("keeper"))
            klass = Klass.RuneKeeper;
         else if (name.toLowerCase().contains ("warden"))
            klass = Klass.Warden;
         else if (name.toLowerCase().contains ("unknown"))
            klass = Klass.Unknown;
         
         else if (!name.toLowerCase().contains ("&nbsp;"))
            System.err.println ("Invalid Class: " + name);
      }
      return klass;
   }
}
