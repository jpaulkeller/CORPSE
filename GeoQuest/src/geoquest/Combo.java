package geoquest;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class Combo implements Card, Comparable<Combo>
{
   public static final String COLOR = "#DDCCFF";
   
   static final Map<String, Combo> COMBOS = new TreeMap<String, Combo>();
   static { populate(); }
      
   private String name;
   private String trigger;
   private String text;
   String eq1, eq2;

   public Combo (final String name, final String eq1, final String eq2,
                 final String trigger, final String text)
   {
      this.name = name.replaceAll (" ", "&nbsp;");
      this.trigger = trigger;
      this.text = text.length() > 0 ? text : CardUtils.BLANK;
      this.eq1 = eq1.replaceAll (" ", "&nbsp;");
      this.eq2 = eq2.replaceAll (" ", "&nbsp;");
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
   public int compareTo (final Combo c)
   {
      return name.compareTo (c.name);
   }
      
   @Override
   public String toString()
   {
      return name + " (" + eq1 + " + " + eq2 + ")";
   }
      
   @Override
   public boolean equals (final Object other)
   {
      if (other instanceof Combo)
         return name.equals (((Combo) other).name);
      return false;
   }
   
   @Override
   public int hashCode()
   {
      return name.hashCode();
   }

   private static void populate()
   {
      add ("Bird Watcher", "Binoculars", "Field Guide", 
           "Whenever another player rolls a <em class=find>FIND</em>, ",
            "you get +1 on your next roll.");
      add ("Canyon Cacher", "External Antenna", "Rope", 
           "", "You get +2 for all rolls on Rocky terrain.");
      add ("Coin Collector", "Geocoin (1)", "Geocoin (2)", 
           "", "Double any bonus that applies to your rolls.");
      add ("Eagle Scout", "Pocket Knife", "Survival Strap", 
            "When an event would affect you, ",
            "you may discard any Equipment card to ignore it.");
      add ("Engineer", "Cell Phone", "Laptop", 
           "If you roll a <em class=dnf>DNF</em>, ",
           "you may ignore it, and add 2 to your roll.");
      add ("Environmentalist", "CITO Bag", "Waders", 
           "If an Event card awards you points, ",
           "you get double those points instead.");
      add ("Event Coordinator", "Hat", "Hiking Staff", 
           "If you roll doubles, ", 
           "you may skip your turn (don't draw an Event card) to host a <em class=event>Meet&nbsp;and&nbsp;Greet</em>.  You must attend the event.  Roll the dice to determine how many points you earn.");
      add ("Hitchhiker", "Belt Pack", "PDA", 
           "Whenever you roll <em class=roll>4+2</em>, ",
           "you may jump to any tile as your move.");
      add ("Insulation", "Coat", "Gloves", 
           "", "All roll and point penalties against you are treated as -1.");
      add ("Karma", "Lucky Charm", "Mirror", 
           "Whenever someone tries to play an Event card on you, ",
           "you both roll the dice.  If you roll higher, the event does not affect you (it is discarded).");
      add ("Life Guard", "Bandana", "Whistle",
           "If a player next to a stream or lake rolls a <em class=roll>1</em> on either die, ",
           "you may immediately jump to their location.");
      add ("MacGyver", "Duct Tape", "Utility Tool", 
           "", "You may skip your turn to draw an Equipment card.");
      add ("Marsh Madness", "Gaiters", "Insect Repellent", 
           "", "You get +2 for all rolls on Swamp terrain, and all rolls to cross streams.");
      add ("Naturalist", "Swag Bag", "Walking Stick", 
           "Whenever a wildlife Event card is played, ",
           "you gain 2 points.");
      add ("Night Cacher", "Flashlight", "Head Lamp", 
           "If you roll 1 or less, ",
           "you may take an extra turn.  (This is not limited to once per turn.)");
      add ("Paramedic", "First-aid Kit", "Repair Kit", 
           "", "All injury Event cards (drawn or in a player's hand) are discarded, and you gain 4 points for each one.");
      add ("Photographer", "Camera", "Safari Vest", 
           "", "You get 2 points if you end your turn on a <em class=tile>Scenic View</em> or on the same tile as another player.  (You may only get points once per <em class=tile>View</em> or player.)");
      add ("Road Warrior", "Hydration Pack", "Mountain Bike", 
           "If you roll 5 or higher while on a Path, ",
           "you may jump to any tile on that Path.</em>");
      add ("Search and Rescue", "FRS Radio", "Yellow Jeep", 
           "Whenever anyone rolls <em class=roll>1</em> or less, ",
           "you may immediately jump to the roller's location.");
      add ("Through Hiker", "Backpack", "Hiking Boots", 
           "Each time you roll doubles while on a Path, ",
           "you may take an extra turn.  (This is not limited to once per turn.)");
      add ("Tracker", "Ol' Blue", "Map", 
           "If a player in your quadrant rolls <em class=find>FIND</em> or <em class=dnf>DNF</em>, ",
           "you may immediately jump to their location.");
      add ("Trail Mix", "Gorp", "Trail Guide", 
           "", "You get +2 on every Move roll.");
      add ("Veteran", "Water Bottle", "Long Pants", 
           "If you roll a <em class=find>FIND</em> or <em class=dnf>DNF</em> while moving, ", 
           "you may re-roll that die.");
      add ("Weatherman", "Emergency Radio", "Waterproof Jacket", 
           "", "All weather Event cards (drawn or in a player's hand) are discarded, and you gain 2 points for each one.");
   }
    
   private static void add (final String cardName, final String eq1, final String eq2,
                            final String trigger, final String text)
   {
      Combo combo = new Combo (cardName, eq1, eq2, trigger, text);
      COMBOS.put (combo.getName(), combo);
   }
   
   static void print()
   {
      String target = "docs/Combos.html";
      
      try
      {
         PrintWriter out = null;
         
         out = new PrintWriter (target);
         out.println ("<html>");
         out.println ("<body>\n");
         CardUtils.printStyle (out);

         out.println ("<dl>");
         for (Combo combo : COMBOS.values())
         {
            // out.println ("  <dt><b>" + combo.name + ":</b>"); 
            // out.println ("      " + combo.eq1 + " + " + combo.eq2 + "</dt>"); 
            out.println ("  <dt><b><em class=combo>" + combo.name + "</em>:</b>"); 
            out.println ("      <em class=equipment>" + combo.eq1 + "</em> + "); 
            out.println ("      <em class=equipment>" + combo.eq2 + "</em></dt>"); 
            out.println ("  <dd>" + combo.getText() + "</dd>");
            dump (combo);
         }
         out.println ("</dl>\n");
      
         out.println ("</body>");
         out.println ("</html>");
         out.close();
         
         System.out.println (COMBOS.size() + " combos written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }
   
   private static void dump (final Combo combo)
   {
      StringBuilder sb = new StringBuilder();
      sb.append (combo.name);
      sb.append (",");
      sb.append (combo.eq1);
      sb.append (",");
      sb.append (combo.eq2);
      sb.append (",\"");
      sb.append (combo.text);
      sb.append ("\",\"");
      sb.append (combo.trigger);
      sb.append ("\"");

      String s = sb.toString();
      s = s.replaceAll ("&nbsp;", " ");
      s = s.replaceAll ("<[^>]+>", "");
      System.out.println (s);
   }
   
   public static void main (final String[] args)
   {
      Combo.print();
      System.out.println();
      
      System.out.println ("Validating Equipment References:");
      System.out.flush();
      for (Combo c : COMBOS.values())
      {
         Equipment eq = Equipment.EQUIPMENT.get (c.eq1);
         if (eq == null)
            System.err.println ("  Missing equipment for " + c.name + ": " + c.eq1);
         else if (!c.name.equalsIgnoreCase (eq.getCombo()))
            System.err.println ("  Invalid combo for " + c.name + " " + c.eq1 + ": " + eq.getCombo());
         eq = Equipment.EQUIPMENT.get (c.eq2);
         if (eq == null)
            System.err.println ("  Missing equipment for " + c.name + ": " + c.eq2);
         else if (!c.name.equalsIgnoreCase (eq.getCombo()))
            System.err.println ("  Invalid combo for " + c.name + " " + c.eq2 + ": " + eq.getCombo());
         System.err.flush();
      }
      System.out.println();
      System.out.flush();
   }
}
