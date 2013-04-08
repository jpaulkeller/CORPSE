package lotro.models;

/*
<stat name="morale" value="7054"/>
<stat name="power" value="3005"/>
<stat name="armour" value="4420"/>
<stat name="might" value="202"/>
<stat name="agility" value="1622"/>
<stat name="vitality" value="576"/>
<stat name="will" value="163"/>
<stat name="fate" value="356"/>
<stat name="radiance" value=""/>
<stat name="meleeCrit" value="6232"/>
<stat name="rangedCrit" value="5632"/>
<stat name="tacticalCrit" value="5632"/>
<stat name="fearResistance" value="7515"/>
<stat name="woundResistance" value="7515"/>
<stat name="diseaseResistance" value="7515"/>
<stat name="poisonResistance" value="7515"/>
<stat name="commonDef" value="7639"/>
<stat name="fireDef" value="4845"/>
<stat name="frostDef" value="4845"/>
<stat name="shadowDef" value="4845"/>
<stat name="lightningDef" value="4845"/>
<stat name="acidDef" value="4845"/>
<stat name="block" value="N/A"/>
<stat name="evade" value="7104"/>
<stat name="parry" value="3948"/>
<stat name="criticalPoints" value="5632"/>
<stat name="criticalDefense" value="780"/>
<stat name="physicalMitigation" value="4103"/>
<stat name="tacticalMitigation" value="4845"/>
<stat name="theOneResistance" value="7515"/>
<stat name="finessePoints" value="2964"/>

OBSOLETE:
   // Radiance ("radiance", "#82CAFF", "center", true),

   // MeleeCrit    ("meleeCrit", "#FAAFBA", false),
   // RangedCrit   ("rangedCrit", "#FAAFBA", false),

   // TacticalCrit ("tacticalCrit", "#FAAFBA", false),
   // FearResist    ("fearResistance", "#AE8BFF", true),
   // WoundResist   ("woundResistance", "#FF6666", true),
   // DiseaseResist ("diseaseResistance", "#EEFF55", true),
   // PoisonResist  ("poisonResistance", "#8AFB17", true),

   // Fire      ("fireDef", "#FAF8CC", false),
   // Frost     ("frostDef", "#FAF8CC", false),
   // Shadow    ("shadowDef", "#FAF8CC", true),
   // Lightning ("lightningDef", "#FAF8CC", false),
   // Acid      ("acidDef", "#FAF8CC", true),
*/

public enum Stats
{
   Morale ("morale", "lightgreen", false),
   Power  ("power", "lightblue", false),
   Armour ("armour", "tan", false),

   Might    ("might", "#DDDDDD", true),
   Agility  ("agility", "#DDDDDD", true),
   Vitality ("vitality", "#DDDDDD", true),
   Will     ("will", "#DDDDDD", true),
   Fate     ("fate", "#DDDDDD", true),
   
   Critical ("criticalPoints", "#FAAFBA", true), // TODO freep-only?
   Finesse ("finessePoints", "#FAAFBA", true), // TODO freep-only?
   
   Block ("block", "tan", false),
   Evade ("evade", "tan", false),
   Parry ("parry", "tan", false),
   CriticalDefense ("criticalDefense", "tan", true), // TODO freep-only?
   
   Resist  ("theOneResistance", "#EEFF55", true),
   Common    ("commonDef", "#EEFF55", false),
   TacticalDefense ("acidDef", "#EEFF55", true),
   PhysicalMitigation ("physicalMitigation", "#EEFF55", true), // TODO freep-only?
   TacticalMitigation ("tacticalMitigation", "#EEFF55", true); // TODO freep-only?
   
   private String tag;
   private String color;
   private String align;
   private boolean freepOnly;
   
   Stats (final String tag, final String color, final boolean freepOnly)
   {
      this (tag, color, "right", freepOnly);
   }
   
   Stats (final String tag, final String color, final String align,
          final boolean freepOnly)
   {
      this.tag = tag;
      this.color = color;
      this.align = align;
      this.freepOnly = freepOnly;
   }
   
   public String getTag()
   {
      return tag;
   }
   
   public String getColor()
   {
      return color;
   }
   
   public String getAlign()
   {
      return align;
   }
   
   public boolean isFreepOnly()
   {
      return freepOnly;
   }
   
   @Override
   public String toString()
   {
      // insert spaces to separate words
      String s = super.toString().replaceAll ("\\B([A-Z][a-z])", " $1");
      // abbreviate some words
      s = s.replace("Critical", "Crit");
      s = s.replace("Defense", "Def");
      s = s.replace("Physical", "Phys");
      s = s.replace("Tactical", "Tact");
      s = s.replace("Mitigation", "Mit");
      return s;
   }
   
   public static Stats parse (final String s)
   {
      for (Stats stat : Stats.values())
         if (s.equalsIgnoreCase (stat.toString()))
            return stat;
      return null;
   }
   
   public static Stats parseTag (final String tag)
   {
      for (Stats stat : Stats.values())
         if (stat.getTag ().equalsIgnoreCase (tag))
            return stat;
      return null;
   }
}
