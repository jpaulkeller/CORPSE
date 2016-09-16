package geoquest;

import java.util.Map;
import java.util.TreeMap;

public class Equipment extends Card implements Comparable<Equipment>
{
   static final Map<String, Equipment> EQUIPMENT = new TreeMap<>();
   static
   {
      populate();
   }

   private String name;
   private String text;
   private String image;
   private String icon;
   private String combo;
   // private boolean usedByCacher; // true if used by Cache Character
   private boolean usedByEvent; // true if used by Event

   public Equipment(final String name, final String text, final String imageName, final String icon, final String combo)
   {
      this.name = name.replaceAll(" ", "&nbsp;");
      this.text = text.length() > 0 ? text : CardUtils.BLANK;
      this.combo = combo != null ? combo.replaceAll(" ", "&nbsp;") : null;
      this.image = CardUtils.findImage("Equipment", imageName != null ? imageName : name);
      if (icon != null)
      {
         this.icon = CardUtils.findImage("TGC", icon);
         if (this.icon == null)
            this.icon = CardUtils.findImage("TGC/Icons", "Missing");
      }
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

   public String getImage()
   {
      return image;
   }

   public String getIcon()
   {
      return icon;
   }

   public String getCombo()
   {
      return combo;
   }

   @Override
   public int compareTo(final Equipment e)
   {
      return name.compareTo(e.name);
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof Equipment)
         return name.equals(((Equipment) other).name);
      return false;
   }

   static void populate()
   {
      add("Antenna", "+1 to Search rolls on Forest and Urban tiles", "Antenna", "Icons/Search 1", "Engineer");
      add("Backpack", "Provides 4 extra equipment slots, but you can't move more than 5 per turn", null, "Icons/Slot 4", "Through Hiker");
      add("Bandana", "You may trade this for another player's equipment card (if they agree)", null, null, "Lifeguard");
      add("Batteries", "You may reroll any Move roll of 1 or less", null, "Icons/Reroll", "Eagle Scout");
      add("Belt Pack", "Provides 2 extra equipment slots", null, "Icons/Slot 2", "MacGyver");
      add("Binoculars", "+1 to Move rolls of 4 or higher", null, "Icons/Move 1", "Search and Rescue");
      add("Camel Pack", "Provides 2 extra equipment slots", null, "Icons/Slot 2", "Road Warrior");
      add("Camera", "Gain 1 point for each other <em class=event>Meet and Greet</em> attendee (if you attend)", null, "Icons/Point Coins +1", "Photographer");
      add("Cell Phone", "You may add 1 to your Search roll (if you do, gain 1 less point)", null, "Icons/Search 1", "Engineer");
      add("CITO Bag", "You may discard this card to gain 3 points", "CITO", "Icons/Point Coins +3", "Naturalist");
      add("Compass", "+1 to Search rolls for all Multi-caches", null, "Icons/Search 1", "Eagle Scout");
      add("Duct Tape", "You may discard this Equipment to prevent any Event from affecting you", null, null, "MacGyver");
      add("Emergency Radio", "+1 to Move rolls if any weather Event is in effect", null, "Icons/Move 1", "Weatherman");
      add("Field Guide", "You may discard this to solve any puzzle cache", null, "Icons/Cache Puzzle", "Naturalist");
      add("First-aid Kit", "Discard to prevent any injury Event; gain 4 points if used on another player", null, "Icons/Point Coins +4", "Paramedic");
      add("Flashlight", "+1 to Search rolls for caches not yet found by anyone", null, "Icons/Search 1", "Night Cacher");
      add("FRS Radio", "If you roll <em class=find>.F.</em>, you may jump to any tile occupied by another player", null, "Icons/Move Run", "Search and Rescue");
      add("Gaiters", "+1 to Move rolls on Swamp tiles, and when crossing a stream", null, "Icons/Move 1", "Veteran");
      add("Geocoin", "This card counts as 3 points, or discard it for 1 point", null, "Icons/Point Coins +3", "Event Coordinator");
      add("Gloves", "+1 to Search rolls for any level <em class=diff3>.3.</em> or higher cache on a Forest tile", null, "Icons/Search 1", "Weatherman");
      add("Gorp", "Discard this card to take another turn", null, null, "Road Warrior");
      add("Hat", "You earn an extra point for every <em class=event>Meet and Greet</em> you attend", null, "Icons/Point Coins +1", "Lifeguard");
      add("Head Lamp", "+2 on Move rolls if both dice are black", null, "Icons/Move 2", "Night Cacher");
      add("Hiking Boots", "+1 to Move rolls on Forest tiles", null, "Icons/Move 1", "Through Hiker");
      add("Hiking Staff", "+1 to Move rolls on Rocky tiles", null, "Icons/Move 1", "Event Coordinator");
      // TODO art!
      add("Insect Repellent", "Can prevent several bug-related events", "BugSpray", null, "Night Cacher");
      add("Jeep", "+2 to Move rolls on Urban tiles; provides 1 extra equipment slot", null, "Icons/Slot 1", "Paramedic");
      add("Laptop", "You may solve any Puzzle cache using only 1 of the letters", null, "Icons/Cache Puzzle", "Engineer");
      add("Letterbox Stamp", "+1 point whenever you find a level <em class=diff3>.3.</em> cache", null, "Icons/Point Coins +1", "Event Coordinator");
      add("Long Pants", "Provides 1 extra equipment slot", null, "Icons/Slot 1", "Veteran");
      add("Lucky Charm", "You get +1 whenever you roll doubles", null, "Icons/Roll 1", "Hitchhiker");
      add("Map", "+1 to Move rolls if either die is white", null, "Icons/Move 1", "Tracker");
      add("Mirror", "Ignore any <em class=dnf>.D.</em> when searching on Urban tiles", null, "Icons/Search", "Photographer");
      add("Mountain Bike", "+1 to Move rolls if either die is 1", null, "Icons/Move 1", "Road Warrior");
      add("Ol' Blue", "You may search for caches in adjacent tiles as if you were on them", "Dog", null, "Tracker");
      add("Pocket Knife", "+1 to Search rolls for any level <em class=diff5>.5.</em> cache", null, "Icons/Search 1", "Eagle Scout");
      add("Rain Jacket", "Provides 1 extra equipment slot", null, "Icons/Slot 1", "Weatherman");
      add("Repair Kit", "You may discard to gain 2 points if you are on a cache", null, "Icons/Point Coins +2", "Paramedic");
      // TODO - no-event icon?
      add("Rope", "No other player may play Events on you if you are on a Rocky tile", null, null, "Search and Rescue");
      add("Safari Vest", "Provides 3 extra equipment slots", null, "Icons/Slot 3", "Photographer");
      add("Survival Strap", "You may reroll if both dice are 1", null, "Icons/Reroll", "Hitchhiker");
      add("Swag Bag", "Provides 1 extra equipment slot", null, "Icons/Slot 1", "Hitchhiker");
      add("Trail Guide", "You may move 1 extra tile along a path each turn", null, "Icons/Move 1", "Through Hiker");
      add("Utility Tool", "+1 to Search rolls on Urban tiles", null, "Icons/Search 1", "MacGyver");
      add("Waders", "+2 to Move rolls when crossing a stream", null, "Icons/Move 2", "Tracker");
      add("Walking Stick", "+1 to Move rolls on Clear tiles", null, "Icons/Move 1", "Naturalist");
      add("Water Bottle", "+1 to Move rolls if either die is zero", null, "Icons/Move 1", "Veteran");
      add("Whistle", "Moving onto an occupied tile costs you no movement points", null, "Icons/Move Join", "Lifeguard");

      // OBSOLETE 
      // add ("Coat", "Protects against some cold-related effects", null, "Insulation");
      // add ("Emergency Blanket", "Discard to ignore any weather-related effect", null, "Just In Case");
      // add ("Metal Detector", "Provides a 50/50 chance to prevent effects that would cause you to lose an Equipment card", null, "");
      // add ("Sunscreen", "Protects against some sun-related effects", null, "");
      // add ("PDA", "You may roll again if your first Search roll fails", null, "Engineer");
      // add ("Umbrella", "Protects against some rain-related effects", null, "");
   }

   private static void add(final String cardName, final String cardText, final String image, 
                           final String icon, final String combo)
   {
      Equipment equip = new Equipment(cardName, cardText, image, icon, combo);
      EQUIPMENT.put(equip.getName(), equip);
   }

   /*
   private static void dump(final Equipment eq)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(eq.name);
      sb.append(",");
      sb.append(eq.combo);
      sb.append(",\"");
      sb.append(eq.text);
      sb.append("\"");

      String s = sb.toString();
      s = s.replaceAll("&nbsp;", " ");
      s = s.replaceAll("<[^>]+>", "");
      System.out.println(s);
   }
   */

   private static void showEquipment()
   {
      for (Equipment eq : EQUIPMENT.values())
         System.out.println(eq.getName().replace("&nbsp;", " "));
      System.out.println();

      System.out.println("\nASSOCIATED CACHERS/EVENTS/COMBOS");
      for (Equipment eq : EQUIPMENT.values())
      {
         System.out.println(eq.getName().replace("&nbsp;", " ") + ":");

         System.out.print("  Combo: ");
         for (Combo combo : Combo.COMBOS.values())
            if (eq.getName().equals(combo.eq1) || eq.getName().equals(combo.eq2) || eq.getName().equals(combo.eq3))
               System.out.print(combo.getName().replace("&nbsp;", " "));
         System.out.println();

         /*
          * System.out.print ("  Cachers: "); for (Cacher cacher : Cacher.CACHERS.values()) if
          * (cacher.getEquipment().contains (eq.name)) { eq.usedByCacher = true; System.out.print (cacher + ", "); }
          * System.out.println();
          */

         System.out.print("  Events: ");
         for (Event ev : Event.EVENTS.values())
            if (ev.getEquipment().contains(eq.name) || ev.getText().contains(eq.name))
            {
               eq.usedByEvent = true;
               System.out.print(ev.getName().replace("&nbsp;", " ") + ", ");
            }
         System.out.println();
      }
      System.out.println();
      System.out.flush();

      /*
       * System.out.println ("Equipment not referenced by any cacher:"); for (Equipment eq : EQUIPMENT.values()) if
       * (!eq.usedByCacher) System.out.println ("  " + eq.name.replace ("&nbsp;", " ")); System.out.println();
       * System.out.flush();
       */

      System.out.println("Equipment not referenced by any event:");
      for (Equipment eq : EQUIPMENT.values())
         if (!eq.usedByEvent)
            System.out.println("  " + eq.name.replace("&nbsp;", " "));
      System.out.println();
      System.out.flush();
   }

   private static void validateEquipment()
   {
      for (Equipment eq : EQUIPMENT.values())
      {
         if (eq.text == null || eq.text.equals("") || eq.text.equals(CardUtils.BLANK))
            System.err.println("No text for: " + eq);

         Combo combo = Combo.COMBOS.get(eq.combo);
         if (combo == null)
            System.err.println("Missing combo for " + eq.name + ": " + eq.combo);
         else if (!combo.eq1.equals(eq.name) && !combo.eq2.equals(eq.name) && !combo.eq3.equals(eq.name))
            System.err.println("Invalid combo for " + eq.name + ": " + eq.combo);
      }
      System.err.flush();
   }

   public static void main(final String[] args)
   {
      HtmlGenerator htmlGen = new HtmlGenerator(9, 3, 200, 95, 90, 142);
      htmlGen.printEquipment(EQUIPMENT);

      ImageGenerator imgGen = new ImageGenerator(ImageStats.getEquipmentStats(), false);
      for (Equipment event : EQUIPMENT.values())
         imgGen.publish(event);
      System.out.println();

      showEquipment();
      System.out.println();

      validateEquipment();
   }
}
