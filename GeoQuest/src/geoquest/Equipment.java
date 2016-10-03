package geoquest;

import java.util.Map;
import java.util.TreeMap;

public class Equipment extends Component implements Comparable<Equipment>
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
   private String ensemble;
   private boolean usedByEvent; // true if used by Event

   public Equipment(final String name, final String text, final String imageName, final String icon, final String ensemble)
   {
      this.name = name;
      this.text = text.length() > 0 ? text : CardUtils.BLANK;
      this.ensemble = ensemble;
      this.image = CardUtils.findImage("Art/Equipment", imageName != null ? imageName : name);
      
      if (icon != null)
      {
         this.icon = CardUtils.findImage("Icons", icon);
         if (this.icon == null)
            this.icon = CardUtils.findImage("Icons", "Missing");
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

   public String getEnsemble()
   {
      return ensemble;
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
      add("Antenna", "+1 when Searching on Forest and Urban tiles", "Antenna", "Search +1", "Engineer");
      add("Backpack", "Provides 4 extra equipment slots, but you can't move more than 5 per turn", null, "Slot 4", "Through Hiker");
      add("Bandana", "You may trade this for another player's equipment card (if they agree)", null, null, "Lifeguard");
      add("Batteries", "You may reroll any Move roll of 1 or less", null, "Reroll", "Eagle Scout");
      add("Belt Pack", "Provides 2 extra equipment slots", null, "Slot 2", "MacGyver");
      add("Binoculars", "+1 to Move when you roll 4 or higher", null, "Move 1", "Search and Rescue");
      add("Camel Pack", "Provides 2 extra equipment slots", null, "Slot 2", "Road Warrior");
      add("Camera", "Gain 1 point for each other <em class=event>Meet and Greet</em> attendee (if you attend)", null, "Point +1", "Photographer");
      add("Cell Phone", "You may add 1 to your Search roll (if you do, gain 1 less point)", null, "Search +1", "Engineer");
      add("CITO Bag", "You may discard this card to gain 3 points", "CITO", "Point +3", "Naturalist");
      add("Compass", "+1 when Searching for all Multi-caches", null, "Search +1", "Eagle Scout");
      add("Duct Tape", "You may discard this Equipment to ignore any Event played on you", null, null, "MacGyver");
      add("Emergency Radio", "+1 when Moving if any weather Event is in effect", null, "Move 1", "Weatherman");
      add("Field Guide", "You may discard this to solve any puzzle cache", null, "Cache Puzzle", "Naturalist");
      add("First-aid Kit", "Discard to prevent any injury Event; gain 4 points if used on another player", null, "Point +4", "Paramedic");
      add("Flashlight", "+1 when Searching for caches not yet found by anyone", null, "Search +1", "Night Cacher");
      // TODO too good?
      add("FRS Radio", "If you roll <em class=find>.F.</em>, you may jump to any tile occupied by another player", null, "Move Run", "Search and Rescue");
      add("Gaiters", "+1 when Moving on Swamp tiles, and when crossing a stream", null, "Move 1", "Veteran");
      add("Geocoin", "This card counts as 3 points, or discard it for 1 point", null, "Point +3", "Event Coordinator");
      add("Gloves", "+1 when Searching for any level 3 or higher cache on a Forest tile", null, "Search +1", "Weatherman");
      add("Gorp", "Discard this card to take another turn", null, null, "Road Warrior");
      add("Hat", "You earn an extra point for every <em class=event>Meet and Greet</em> you attend", null, "Point +1", "Lifeguard");
      add("Head Lamp", "+2 when Moving if both dice are black", null, "Move 2", "Night Cacher");
      add("Hiking Boots", "+1 when Moving on Forest tiles", null, "Move 1", "Through Hiker");
      add("Hiking Staff", "+1 when Moving on Rocky tiles", null, "Move 1", "Event Coordinator");
      add("Insect Repellent", "+1 when Searching on Swamp tiles", null, "Search +1", "Night Cacher");
      add("Jeep", "+2 when Moving on Urban tiles; provides 1 extra equipment slot", null, "Slot 1", "Paramedic");
      add("Laptop", "You may solve any Puzzle cache using only 1 of the letters", null, "Cache Puzzle", "Engineer");
      add("Letterbox Stamp", "+1 point whenever you find a level 3 cache", null, "Point +1", "Event Coordinator");
      add("Long Pants", "Provides 1 extra equipment slot", null, "Slot 1", "Veteran");
      add("Lucky Charm", "You get +1 whenever you roll doubles", null, "Roll +1", "Hitchhiker");
      add("Map", "+1 when Moving if either die is white", null, "Move 1", "Tracker");
      add("Mirror", "Ignore any <em class=dnf>.D.</em> when searching on Urban tiles", null, "Search", "Photographer");
      add("Mountain Bike", "+1 when Moving if either die is 1", null, "Move 1", "Road Warrior");
      add("Ol' Blue", "You may search for caches in adjacent tiles as if you were on them", "Dog", "Search", "Tracker");
      add("Pocket Knife", "+1 when Searching for any level 5 cache", null, "Search +1", "Eagle Scout");
      add("Rain Jacket", "Provides 1 extra equipment slot", null, "Slot 1", "Weatherman");
      add("Repair Kit", "You may discard to gain 2 points if you are on a cache", null, "Point +2", "Paramedic");
      add("Rope", "No other player may play Events on you if you are on a Rocky tile", null, null, "Search and Rescue");
      add("Safari Vest", "Provides 3 extra equipment slots", null, "Slot 3", "Photographer");
      add("Survival Strap", "You may reroll if both dice are 1", null, "Reroll", "Hitchhiker");
      add("Swag Bag", "Provides 1 extra equipment slot", null, "Slot 1", "Hitchhiker");
      add("Trail Guide", "You may move 1 extra tile along a path each turn", null, "Move 1", "Through Hiker");
      add("Utility Tool", "+1 when Searching on Urban tiles", null, "Search +1", "MacGyver");
      add("Waders", "+2 when crossing a stream", null, "Move 2", "Tracker");
      add("Walking Stick", "+1 when Moving on Clear tiles", null, "Move 1", "Naturalist");
      add("Water Bottle", "+1 when Moving if either die is black", null, "Move 1", "Veteran");
      add("Whistle", "Moving onto a tile occupied by another player costs you no movement points", null, "Move Join", "Lifeguard");

      // add ("Coat", "Protects against some cold-related effects", null, "Insulation");
      // add ("Emergency Blanket", "Discard to ignore any weather-related effect", null, "Just In Case");
      // add ("Metal Detector", "Provides a 50/50 chance to prevent effects that would cause you to lose an Equipment card", null, "");
      // add ("Sunscreen", "Protects against some sun-related effects", null, "");
      // add ("PDA", "You may roll again if your first Search roll fails", null, "Engineer");
      // add ("Umbrella", "Protects against some rain-related effects", null, "");
   }

   private static void add(final String cardName, final String cardText, final String image, 
                           final String icon, final String ensemble)
   {
      Equipment equip = new Equipment(cardName, cardText, image, icon, ensemble);
      EQUIPMENT.put(equip.getName(), equip);
   }

   private static void show()
   {
      for (Equipment eq : EQUIPMENT.values())
         System.out.println(eq.getName() + ": " + eq.getText());
      System.out.println();

      System.out.println("\nASSOCIATED CACHERS/EVENTS/ENSEMBLES");
      for (Equipment eq : EQUIPMENT.values())
      {
         System.out.println(eq.getName() + ":");

         System.out.print("  Ensemble: ");
         for (Ensemble ensemble : Ensemble.ENSEMBLES.values())
            if (eq.getName().equals(ensemble.eq1) || eq.getName().equals(ensemble.eq2) || eq.getName().equals(ensemble.eq3))
               System.out.print(ensemble.getName());
         System.out.println();

         System.out.print("  Events: ");
         for (Event ev : Event.EVENTS.values())
            if (ev.getEquipment().contains(eq.name) || ev.getText().contains(eq.name))
            {
               eq.usedByEvent = true;
               System.out.print(ev.getName() + ", ");
            }
         System.out.println();
      }
      System.out.println();
      System.out.flush();

      System.out.println("Equipment not referenced by any event:");
      for (Equipment eq : EQUIPMENT.values())
         if (!eq.usedByEvent)
            System.out.println("  " + eq.name);
      System.out.println();
      System.out.flush();
      System.out.println();
   }

   private static void validate()
   {
      for (Equipment eq : EQUIPMENT.values())
      {
         if (eq.text == null || eq.text.equals("") || eq.text.equals(CardUtils.BLANK))
            System.err.println("No text for: " + eq);

         Ensemble ensemble = Ensemble.ENSEMBLES.get(eq.ensemble);
         if (ensemble == null)
            System.err.println("Missing ensemble for " + eq.name + ": " + eq.ensemble);
         else if (!ensemble.eq1.equals(eq.name) && !ensemble.eq2.equals(eq.name) && !ensemble.eq3.equals(eq.name))
            System.err.println("Invalid ensemble for " + eq.name + ": " + eq.ensemble);
      }
      System.err.flush();
   }

   public static void main(final String[] args)
   {
      HtmlGenerator htmlGen = new HtmlGenerator(12, 4);
      htmlGen.printEquipment(EQUIPMENT);

      ImageGenerator imgGen = new ImageGenerator(ImageStats.getEquipmentStats(), false);
      for (Equipment eq : EQUIPMENT.values())
         imgGen.publish(eq);
      System.out.println();

      show();
      validate();
   }
}
