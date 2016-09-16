package geoquest;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class Combo extends Card implements Comparable<Combo>
{
   public static final String COLOR = "#DDCCFF";

   static final Map<String, Combo> COMBOS = new TreeMap<>();
   static
   {
      populate();
   }

   private String name;
   private String trigger;
   private String text;
   String eq1, eq2, eq3;

   public Combo(final String name, final String eq1, final String eq2, final String eq3, final String trigger, final String text)
   {
      this.name = name;
      this.trigger = trigger;
      this.text = text.length() > 0 ? text : CardUtils.BLANK;
      this.eq1 = eq1;
      this.eq2 = eq2;
      this.eq3 = eq3;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public String getText()
   {
      return trigger + text;
   }

   @Override
   public int compareTo(final Combo c)
   {
      return name.compareTo(c.name);
   }

   @Override
   public String toString()
   {
      return name + " (" + eq1 + " + " + eq2 + " + " + eq3 + ")";
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof Combo)
         return name.equals(((Combo) other).name);
      return false;
   }

   private static void populate()
   {
      add("Eagle Scout", "Batteries", "Compass", "Pocket Knife", "When an event would affect you, ",
         "you may discard any Equipment card to ignore it.");
      add("Engineer", "Antenna", "Cell Phone", "Laptop", "Whenever another player rolls a <em class=find>FIND</em>, ",
         "you get +1 on your next roll.  (This is not limited to once per turn.)");
      add("Event Coordinator", "Geocoin", "Hiking Staff", "Letterbox Stamp",
         "Whenever someone tries to play an Event card on you, ",
         "you both roll the dice.  If you roll higher, the event does not affect you (it is discarded).");
      add("Lifeguard", "Bandana", "Hat", "Whistle",
         "If a player next to a stream or lake rolls a <em class=roll>1</em> on either die, ",
         "you may immediately jump to their location.");
      add("Hitchhiker", "Lucky Charm", "Survival Strap", "Swag Bag", "Whenever you roll <em class=roll>4+2</em>,",
         "you may jump to any tile as your move.");
      add("MacGyver", "Belt Pack", "Duct Tape", "Utility Tool", "", "You may skip your turn to draw an Equipment card.");
      add("Naturalist", "CITO Bag", "Field Guide", "Walking Stick", "",
         "All wildlife Event cards (drawn, in play, or in a player's hand) are discarded, and you gain 2 points for each one.");
      add("Night Cacher", "Flashlight", "Head Lamp", "Insect Repellent", "",
         "All roll and point penalties against you are treated as -1.");
      add("Paramedic", "First-aid Kit", "Jeep", "Repair Kit", "If an Event card would award you points, ",
         "instead roll the dice to determine how many points you earn.");
      add("Photographer", "Camera", "Mirror", "Safari Vest", "",
         "You get 2 points if you end your turn on a <em class=tile>Scenic View</em> or on the same tile as another player.  (You may only get points once per <em class=tile>View</em> or player.)");
      add("Road Warrior", "Gorp", "Camel Pack", "Mountain Bike", "If you roll 5 or higher while on a Path, ",
         "you may jump to any tile on that Path.</em>");
      add("Search and Rescue", "Binoculars", "FRS Radio", "Rope",
         "Whenever anyone rolls a <em class=dnf>DNF</em>, ", "you may immediately jump to the roller's location.");
      add("Through Hiker", "Backpack", "Hiking Boots", "Trail Guide", "Each time you roll doubles while on a Path, ",
         "you may take an extra turn.  (This is not limited to once per turn.)");
      add("Tracker", "Ol' Blue", "Map", "Waders",
         "If a player in your quadrant rolls <em class=find>FIND</em> or <em class=dnf>DNF</em>, ",
         "you may immediately jump to their location.");
      add("Veteran", "Gaiters", "Long Pants", "Water Bottle",
         "If you roll a <em class=find>FIND</em> or <em class=dnf>DNF</em> while moving, ",
         "you may ignore it, and add 2 to your roll.");
      add("Weatherman", "Emergency Radio", "Gloves", "Rain Jacket", "",
         "All weather Event cards (drawn, in play, or in a player's hand) are discarded, and you gain 2 points for each one.");
   }

   private static void add(final String cardName, final String eq1, final String eq2, final String eq3, final String trigger,
                           final String text)
   {
      Combo combo = new Combo(cardName, eq1, eq2, eq3, trigger, text);
      COMBOS.put(combo.getName(), combo);
   }

   static void print()
   {
      String target = "docs/Combos.html";

      try
      {
         PrintWriter out = null;

         out = new PrintWriter(target);
         out.println("<html>");
         out.println("<body>\n");
         CardUtils.printStyle(out);

         out.println("<dl>");
         for (Combo combo : COMBOS.values())
         {
            out.println("  <dt><b><em class=combo>" + combo.name + "</em>:</b>");
            out.println("      <em class=equipment>" + combo.eq1 + "</em> + ");
            out.println("      <em class=equipment>" + combo.eq2 + "</em> + ");
            out.println("      <em class=equipment>" + combo.eq3 + "</em></dt>");
            out.println("  <dd>" + combo.getText() + "</dd>");
            dump(combo);
         }
         out.println("</dl>\n");

         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(COMBOS.size() + " combos written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   private static void dump(final Combo combo)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(combo.name);
      sb.append(": ");
      sb.append(combo.eq1);
      sb.append(", ");
      sb.append(combo.eq2);
      sb.append(", ");
      sb.append(combo.eq3);
      sb.append(", \"");
      sb.append(combo.trigger);
      sb.append(" ");
      sb.append(combo.text);
      sb.append("\"");

      String s = sb.toString();
      s = s.replaceAll("<[^>]+>", "");
      System.out.println(s);
   }

   public static void main(final String[] args)
   {
      Combo.print();
      System.out.println();

      System.out.println("Validating Equipment References:");
      System.out.flush();
      for (Combo c : COMBOS.values())
      {
         Equipment eq = Equipment.EQUIPMENT.get(c.eq1);
         if (eq == null)
            System.err.println("  Missing equipment for " + c.name + ": " + c.eq1);
         else if (!c.name.equalsIgnoreCase(eq.getCombo()))
            System.err.println("  Invalid combo for " + c.name + " " + c.eq1 + ": " + eq.getCombo());
         
         eq = Equipment.EQUIPMENT.get(c.eq2);
         if (eq == null)
            System.err.println("  Missing equipment for " + c.name + ": " + c.eq2);
         else if (!c.name.equalsIgnoreCase(eq.getCombo()))
            System.err.println("  Invalid combo for " + c.name + " " + c.eq2 + ": " + eq.getCombo());

         eq = Equipment.EQUIPMENT.get(c.eq3);
         if (eq == null)
            System.err.println("  Missing equipment for " + c.name + ": " + c.eq3);
         else if (!c.name.equalsIgnoreCase(eq.getCombo()))
            System.err.println("  Invalid combo for " + c.name + " " + c.eq3 + ": " + eq.getCombo());
         System.err.flush();
      }
      System.out.println();
      System.out.flush();
   }
}
