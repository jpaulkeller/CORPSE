package lotro.models;

import java.util.HashMap;
import java.util.Map;

/** 
 * Each skill is ranked for each class, from 0 to 9.
 *
 * 0 - none/negligible
 * 2 - support 
 * 4 - secondary / support
 * 9 - primary
 */

public enum Skill
{
   AggroMgmt ("Agr"),
   AreaEffects ("AoE"),
   BuffsAndDebuffs ("B/D"),
   CrowdControl ("CC"),
   // CorruptionRemoval ("Cor"),
   DPS ("DPS"),
   Healing ("Heal"),
   Interrupts ("Int"),
   Power ("Pwr"),
   Range ("Rng"),
   Resurrect ("Rez"),
   Tanking ("Tnk");

   private String abbrev;
   
   private Skill (final String abbrev)
   {
      this.abbrev = abbrev;
   }
   
   public String getAbbrev()
   {
      return abbrev;
   }

   public static float getPercent (final Klass klass, final Skill skill)
   {
      return BC.get (klass) [skill.ordinal()] / 10f;
   }
   
   private static final Map<Klass, int[]> BC = new HashMap<Klass, int[]>(); // By Class
   static
   {
      //                                    A  A  B  C  D  H  I  P  R  R  T
      //                                    G  O  /  C  P  L  N  W  N  E  N
      //                                    R  E  D     S  G  T  R  G  Z  K
      BC.put (Klass.Burglar,    new int[] { 2, 0, 7, 5, 2, 2, 4, 2, 0, 0, 0 });
      BC.put (Klass.Captain,    new int[] { 4, 1, 7, 0, 2, 4, 1, 1, 1, 5, 5 });
      BC.put (Klass.Champion,   new int[] { 6, 9, 1, 0, 9, 0, 8, 0, 2, 0, 5 });
      BC.put (Klass.Guardian,   new int[] { 9, 2, 1, 0, 2, 0, 2, 0, 1, 0, 9 });
      BC.put (Klass.Hunter,     new int[] { 0, 3, 0, 5, 9, 0, 0, 0, 9, 0, 0 });
      BC.put (Klass.LoreMaster, new int[] { 1, 4, 7, 9, 4, 1, 3, 8, 5, 2, 1 });
      BC.put (Klass.Minstrel,   new int[] { 0, 2, 1, 2, 2, 9, 1, 1, 2, 9, 0 });
      BC.put (Klass.RuneKeeper, new int[] { 0, 3, 0, 1, 8, 6, 1, 0, 5, 2, 0 });
      BC.put (Klass.Warden,     new int[] { 4, 4, 0, 0, 5, 0, 2, 0, 4, 0, 6 });
   }
   
   public static Skill parse (final String name)
   {
      if (name != null)
         for (Skill skill : Skill.values())
            if (name.equalsIgnoreCase (skill.toString()))
               return skill;

      throw new IllegalArgumentException ("Invalid Skill: " + name); 
   }
   
   public static void main (final String[] args)
   {
      System.out.println ("Burglar Debuffs: " + Skill.getPercent (Klass.Burglar, Skill.Interrupts));
   }
}
