package geoquest;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class Equipment implements Card, Comparable<Equipment>
{
   public static final String COLOR = "#93F3FF";
   
   static final Map<String, Equipment> EQUIPMENT = new TreeMap<String, Equipment>();
   static { populate(); }
   
   private String name;
   private String text;
   private String image;
   private String combo;
   // private boolean usedByCacher; // true if used by Cache Character
   private boolean usedByEvent; // true if used by Event

   public Equipment (final String name, final String text, 
                     final String imageName, final String combo)
   {
      this.name = name.replaceAll (" ", "&nbsp;");
      this.text = text.length() > 0 ? text : CardUtils.BLANK;
      this.combo = combo != null ? combo.replaceAll (" ", "&nbsp;") : null;
      this.image = CardUtils.findImage ("Equipment", imageName != null ? imageName : name);
   }
   
   @Override
   public String getName()
   {
      return name;
   }
   
   @Override
   public String getText()
   {
      return text;
   }
   
   public String getCombo()
   {
      return combo;
   }
   
   @Override
   public int compareTo (final Equipment e)
   {
      return name.compareTo (e.name);
   }
   
   @Override
   public boolean equals (final Object other)
   {
      if (other instanceof Equipment)
         return name.equals (((Equipment) other).name);
      return false;
   }
   
   @Override
   public int hashCode()
   {
      return name.hashCode();
   }
   
   @Override
   public String toString()
   {
      return name;
   }

   static void populate()
   {
      add ("Backpack", "Provides 4 extra equipment slots, but you can't move more than 5 each turn.", null, "Through Hiker");
      add ("Belt Pack", "Provides 2 extra equipment slots.", null, "Hitchhiker");
      add ("Binoculars", "+1 on any move roll 4 or higher.", null, "Bird Watcher");
      add ("Camera", "Gain 1 point per Event Cache attendee (only if you attend).", null, "Photographer");
      add ("Cell Phone", "You may add 1 to your Search roll (if you do, gain 1 less point).", "CellPhone", "Engineer");
      add ("CITO Bag", "You may discard this card to gain 2 points.", "CITO", "Environmentalist");
      add ("Coat", "Protects against some cold-related effects", null, "Insulation");
      add ("Duct Tape", "You may discard this Equipment to prevent any Event from affecting you.", null, "MacGyver");
      add ("Emergency Radio", "Receives weather forecasts.", null, "Weatherman");
      add ("External Antenna", "Add 1 to your Search roll in Forest and Urban tiles.", "Antenna", "Canyon Cacher");
      add ("Field Guide", "You may discard to solve any puzzle cache.", null, "Bird Watcher");
      add ("First-aid Kit", "Discard to prevent any injury Event.  Gain 4 points if used on another player.", null, "Paramedic");
      add ("Flashlight", "+1 on Search rolls for caches not yet found by anyone.", null, "Night Cacher");
      add ("FRS Radio", "If you roll a <em class=find>FIND</em>, you may jump to any tile in your quadrant occupied by a player.", null, "Search and Rescue");
      add ("Gaiters", "+1 on Move rolls in Swamp terrain and when crossing a stream.", null, "Marsh Madness");
      add ("Geocoin (1)", "This card counts as 3 points.", "Geocoin", "Coin Collector");
      add ("Geocoin (2)", "This card counts as 3 points.", "Geocoin", "Coin Collector"); // TBD back side
      add ("Gloves", "+1 on Search rolls for any level 3 or higher cache in a Forest tile.", null, "Insulation");
      add ("Gorp", "Discard this card to take another turn.", null, "Trail Mix");
      add ("Hat", "You earn an extra point for every Event cache you attend.", null, "Event Coordinator");
      add ("Head Lamp", "+2 on Move rolls if both dice are blue.", null, "Night Cacher");
      add ("Hiking Boots", "+1 to Move rolls in Forest terrain.", "HikingBoots", "Through Hiker");
      add ("Hiking Staff", "+1 to Move rolls in Rocky terrain.", null, "Event Coordinator");
      add ("Hydration Pack", "Provides 2 extra equipment slots.", "HydrationPack", "Road Warrior");
      add ("Insect Repellent", "Can prevent several bug-related events.", "BugSpray", "Marsh Madness");
      add ("Laptop", "You may solve any Puzzle cache using only 1 of the letters.", null, "Engineer");
      add ("Long Pants", "Protects against some Events such as <em class=event>Thorns</em> and <em class=event>Ticks</em>.", "LongPants", "Veteran");
      add ("Lucky Charm", "You get +1 whenever you roll doubles.", null, "Karma");
      add ("Map", "Can prevent the effects of getting lost.", null, "Tracker");
      add ("Mirror", "+1 on Search rolls for caches in Urban areas.", null, "Karma");
      add ("Mountain Bike", "+1 to your Move roll if either die is 1.", null, "Road Warrior");
      add ("Ol' Blue", "You search for caches in adjacent tiles as if you were on them.", "Dog", "Tracker");
      add ("PDA", "You may roll again if your first Search roll fails.", null, "Hitchhiker");
      add ("Rope", "Discard to ignore any event if you are in Rocky terrain.", null, "Canyon Cacher");
      add ("Safari Vest", "Provides 3 extra equipment slots.", null, "Photographer");
      add ("Swag Bag", "Provides 2 extra equipment slots.", null, "Naturalist");
      add ("Trail Guide", "You may move 1 extra tile along a path each turn.", null, "Trail Mix");
      add ("Utility Tool", "+1 on Search rolls for all Multi-caches.", null, "MacGyver");
      add ("Waders", "Crossing a stream only costs you 1 movement point.", null, "Environmentalist");
      add ("Walking Stick", "+1 on Move rolls if either die is yellow", null, "Naturalist");
      add ("Water Bottle", "+1 on Move rolls if either die is blue.", null, "Veteran");
      add ("Waterproof Jacket", "Provides 1 extra equipment slot.", "GoreTex", "Weatherman");
      add ("Yellow Jeep", "+1 on Move rolls on Urban and Clear tiles.", null, "Search and Rescue");

      // TODO new to print
      add ("Pocket Knife", "+1 on Search rolls for any level 5 cache.", null, "Eagle Scout");
      add ("Survival Strap", "You may reroll if both dice are 1.", null, "Paramedic");
      add ("Whistle", "Good for clearing the pool, too.", null, "Eagle Scout"); // TODO
      // add ("Bandana", "Protects against some heat-related effects", null, "Fashion ?");
      
      /*
      // OBSOLETE
      add ("Compass", "Can prevent the effects of getting lost", null, "");
      add ("Emergency Blanket", "Discard to ignore any weather-related effect.", null, "Just In Case");
      add ("Metal Detector", "Provides a 50/50 chance to prevent effects that would cause you to lose an Equipment card.", null, "");
      add ("Rechargable Batteries", "", null, "Ever-Ready");
      add ("Sunscreen", "Protects against some sun-related effects", null, "");
      add ("Umbrella", "Protects against some rain-related effects.", null, "");
      */
   }
   
   private static void add (final String cardName, final String cardText,
                            final String image, final String combo)
   {
      Equipment equip = new Equipment (cardName, cardText, image, combo);
      EQUIPMENT.put (equip.getName(), equip); 
   }
   
   static void print()
   {
      String target = "docs/Equipment.html";
      
      try
      {
         PrintWriter out = null;
         
         out = new PrintWriter (target);
         out.println ("<html>");
         out.println ("<body>\n");
         CardUtils.printStyle (out);
         
         int i = 0;
         for (Equipment eq : EQUIPMENT.values())         
            printCard (out, eq, i++);
         
         // pad with blanks to fill out the sheet
         Equipment blankCard = new Equipment (CardUtils.BLANK, "", "Blank", null);
         if (EQUIPMENT.size() % CardsPerPage > 0)
            for (i = 0; i < CardsPerPage - (EQUIPMENT.size() % CardsPerPage); i++)
               printCard (out, blankCard, i + EQUIPMENT.size());
         
         out.println ("</body>");
         out.println ("</html>");
         out.close();
         
         System.out.println (EQUIPMENT.size() + " events written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   private static final int CardsPerPage = 12;
   private static final int CardsPerRow  = 4;
   // page width ~ 600
   private static final int Width    = 140; // 180;
   private static final int PictureH =  90; // 120;
   // private static final int ImageW   =  95;
   private static final int ImageH   =  75; // 100;
   private static final int TextH    =  80; // 120;
   
   private static void printCard (final PrintWriter out, final Equipment eq, final int i)
   {
      if (i % CardsPerPage == 0) out.println ("<table cellpadding=12>\n");
      if (i % CardsPerRow == 0) out.println ("<tr>");
      
      out.println ("<td valign=top><table border=1>");
      out.println ("  <tr><td colspan=2 align=center bgcolor=" + COLOR + "><b>" + eq.name + "</b></td></tr>");
      
      if (eq.image != null)
      {
         out.println  ("  <tr><td colspan=2 align=center height=" + PictureH + " width=" + Width + ">");
         out.print    ("      <img align=center src=\"" + eq.image + "\"");
         if (eq.image.startsWith ("Good"))
            out.print (" style=\"border:5px solid yellow\"");
         else if (!eq.image.startsWith ("Equipment"))
            out.print (" style=\"border:5px solid red\"");
         out.print    (" height=" + ImageH + ">");
      }

      out.println ("</td></tr>");
      
      out.println ("  <tr><td colspan=2 align=center height=" + TextH + " width=" + Width + ">" + eq.text + "</td></tr>");
      String combo = eq.combo != null ? eq.combo : CardUtils.BLANK;
      out.println ("  <tr><td align=center bgcolor=" + Combo.COLOR + "><b>" + combo + "</b></td></tr>");
      out.println ("</table></td>\n");
      
      if (i % CardsPerRow == CardsPerRow - 1) out.println ("</tr>\n");
      if (i % CardsPerPage == CardsPerPage - 1)
         out.println ("</table></td>\n<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>\n");
      
      // dump (eq);
   }
   
   private static void dump (final Equipment eq)
   {
      StringBuilder sb = new StringBuilder();
      sb.append (eq.name);
      sb.append (",");
      sb.append (eq.combo);
      sb.append (",\"");
      sb.append (eq.text);
      sb.append ("\"");

      String s = sb.toString();
      s = s.replaceAll ("&nbsp;", " ");
      s = s.replaceAll ("<[^>]+>", "");
      System.out.println (s);
   }
   
   private static void showEquipment()
   {
      for (Equipment eq : EQUIPMENT.values())
         System.out.println(eq.name.replaceAll("&nbsp;", " "));
      System.out.println();

      System.out.println ("\nASSOCIATED CACHERS/EVENTS/COMBOS");
      for (Equipment eq : EQUIPMENT.values())         
      {
         if (eq.name == null || eq.name.equals ("") || eq.name == CardUtils.BLANK)
            continue;
         System.out.println (eq.name + ":");
         
         System.out.print ("  Combos: ");
         for (Combo combo : Combo.COMBOS.values())
            if (eq.name.equals (combo.eq1) || eq.name.equals (combo.eq2))
               System.out.print (combo + ", ");
         System.out.println();
         
         /*
         System.out.print ("  Cachers: ");
         for (Cacher cacher : Cacher.CACHERS.values())
            if (cacher.getEquipment().contains (eq.name))
            {
               eq.usedByCacher = true;
               System.out.print (cacher + ", ");
            }
         System.out.println();
         */
         
         System.out.print ("  Events: ");
         for (Event ev : Event.EVENTS.values())
            if (ev.getEquipment().contains (eq.name) || ev.getText().contains (eq.name))
            {
               eq.usedByEvent = true;
               System.out.print (ev + ", ");
            }
         System.out.println();
      }
      System.out.println();
      System.out.flush();
      
      /*
      System.out.println ("Equipment not referenced by any cacher:");
      for (Equipment eq : EQUIPMENT.values())
         if (!eq.usedByCacher)
            System.out.println ("  " + eq.name.replace ("&nbsp;", " "));
      System.out.println();
      System.out.flush();
      */
      
      System.out.println ("Equipment not referenced by any event:");
      for (Equipment eq : EQUIPMENT.values())
         if (!eq.usedByEvent)
            System.out.println ("  " + eq.name.replace ("&nbsp;", " "));
      System.out.println();
      System.out.flush();
   }
   
   private static void validateEquipment()
   {
      for (Equipment eq : EQUIPMENT.values())         
      {
         if (eq.text == null || eq.text.equals ("") || eq.text.equals (CardUtils.BLANK))
            System.err.println ("No text for: " + eq);
         
         Combo combo = Combo.COMBOS.get (eq.combo);
         if (combo == null)
            System.err.println ("Missing combo for " + eq.name + ": " + eq.combo);
         else if (!combo.eq1.equals (eq.name) && !combo.eq2.equals (eq.name))
            System.err.println ("Invalid combo for " + eq.name + ": " + eq.combo);
      }
      System.err.flush();
   }
   
   public static void main (final String[] args)
   {
      showEquipment();
      Equipment.print();
      System.out.println();
      
      validateEquipment();
   }
}
