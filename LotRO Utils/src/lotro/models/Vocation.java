package lotro.models;

import java.util.ArrayList;
import java.util.List;

public enum Vocation
{
   /*
    <vocation name="Armsman">
      <Professionessions>
        <Professionession name="Weaponsmith" proficiency="5" mastery="5" />
        <Professionession name="Woodworker" proficiency="4" mastery="3" />
        <Professionession name="Prospector" proficiency="6" mastery="5" />
      </Professionessions>
    </vocation> 
    */

   Unknown,
   Armourer (Profession.Prospector, Profession.Metalsmith, Profession.Tailor),
   Armsman (Profession.Prospector, Profession.Weaponsmith, Profession.Woodworker),
   Explorer (Profession.Forester, Profession.Prospector, Profession.Tailor),
   Historian (Profession.Farmer, Profession.Scholar, Profession.Weaponsmith),
   Tinker (Profession.Prospector, Profession.Cook, Profession.Jeweller),
   Woodsman (Profession.Farmer, Profession.Forester, Profession.Woodworker),
   Yeoman (Profession.Farmer, Profession.Cook, Profession.Tailor);
   
   private List<Profession> professions = new ArrayList<Profession>();
   
   private Vocation (final Profession... professions)
   {
      for (Profession p : professions)
         this.professions.add (p); 
   }

   public List<Profession> getProfessions()
   {
      return professions;
   }
   
   public static Vocation parse (final String s)
   {
      if (s != null)
         for (Vocation name : Vocation.values())
            if (s.equalsIgnoreCase (name.toString()))
               return name;

      System.err.println ("Unrecognized Vocation: " + s);
      return Vocation.Unknown;
   }
}
