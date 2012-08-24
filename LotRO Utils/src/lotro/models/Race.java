package lotro.models;

import java.util.SortedSet;
import java.util.TreeSet;

public enum Race
{
   Unknown ("", "", ""),
   Elf ("goblin", "orc", "drake"),
   Dwarf ("dourhand", "orc", "troll"),
   Hobbit ("wolf", "spider", "goblin"),
   Man ("wight", "warg", "hillmen"),

   Orc(),
   Spider(),
   Uruk(),
   Warg();
   
   public static final SortedSet<Race> FREEPS = new TreeSet<Race>();
   public static final SortedSet<Race> CREEPS = new TreeSet<Race>();
   
   static
   {
      FREEPS.add (Dwarf);
      FREEPS.add (Elf);
      FREEPS.add (Hobbit);
      FREEPS.add (Man);
      
      CREEPS.add (Orc);
      CREEPS.add (Spider);
      CREEPS.add (Uruk);
      CREEPS.add (Warg);
   }
   
   private final String mob1, mob2, mob3;
   
   private Race()
   {
      this (null, null, null);
   }
   
   private Race (final String mob1, final String mob2, final String mob3)
   {
      this.mob1 = mob1;
      this.mob2 = mob2;
      this.mob3 = mob3;
   }

   public String mob1() { return mob1; }
   public String mob2() { return mob2; }
   public String mob3() { return mob3; }
   
   public boolean needs (final Deed deed)
   {
      String name = deed.getName().toLowerCase();
      return name.startsWith (mob1) || 
             name.startsWith (mob2) ||
             name.startsWith (mob3);
   }
   
   public static Race parse (final String name)
   {
      Race race = Race.Unknown;
      if (name != null)
      {
         for (Race r : Race.values())
            if (name.equalsIgnoreCase (r.toString()))
               return r;
         
         if (name.equalsIgnoreCase ("dwarf"))
            race = Race.Dwarf;
         else if (name.equalsIgnoreCase ("elf"))
            race = Race.Elf;
         else if (name.equalsIgnoreCase ("hobbit"))
            race = Race.Hobbit;
         else if (name.toLowerCase().contains ("man")) // race of man
            race = Race.Man;
         else if (!name.equalsIgnoreCase ("unknown") &&
                  !name.equalsIgnoreCase ("&nbsp;"))
            System.err.println ("Invalid race: " + name);
      }
      return race;
   }
}
