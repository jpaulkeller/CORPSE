package geoquest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import str.StringUtils;

public class Event extends Card implements Comparable<Event>
{
   enum Type { STD, NOW, ANY };
   
   private static final Pattern SPECIAL_EQUIPMENT = Pattern.compile ("\\[(.+)\\]");

   static final Map<String, Event> EVENTS = new TreeMap<>();
   static { populate(); }

   private String name;
   private String image;
   private StringBuilder text;
   private Type type;
   
   // ignore event if you have any of these   
   private List<String> equipment = new ArrayList<>();
   private List<String> specialEquipment = new ArrayList<>(); // these require special text

   public Event (final String name, final Type type,
                 final int points, final String effect,
                 final String image, final String... protection)
   {
      this.name = name.replaceAll (" ", "&nbsp;");
      this.image = CardUtils.findImage ("Events", image != null ? image : name);
      this.type = type;
      
      for (String eqName : protection)
      {
         Matcher m = SPECIAL_EQUIPMENT.matcher (eqName);
         boolean special = m.matches(); 
         if (special)
            specialEquipment.add (m.group (1).replaceAll (" ", "&nbsp;"));
         else
            equipment.add (eqName.replaceAll (" ", "&nbsp;"));
      }

      // build the text
      text = new StringBuilder();
      if (effect.length () > 0)
         text.append (effect);
      
      if (text.toString().endsWith (" unless"))
      {
         appendIgnore();
         appendPoints (points);
      }
      else
      {
         appendPoints (points);
         appendIgnore();
      }
   }

   private void appendPoints (final int points)
   {
      if (points != 0)
      {
         if (text.length() > 0)
            text.append ("  ");
         if (points > 0)
            text.append ("Gain " + points + " point");
         else if (points < 0)
            text.append ("Lose " + (-points) + " point");
         if (points > 1 || points < -1)
            text.append ("s");
         text.append (".");
      }
   }

   private void appendIgnore()
   {
      if (!equipment.isEmpty())
      {
         if (text.toString().endsWith (" unless"))
            text.append (" you have ");
         else
         {
            if (text.length () > 0)
               text.append ("  ");
            text.append ("Ignore this card if you have ");
         }
         
         int count = equipment.size();
         int i = 0;
         for (String equip : equipment)
         {
            if (i == 0) // && !equip.equals ("Ol' Blue"))
               text.append ("the ");
            text.append ("<em class=equipment>" + equip + "</em>");
            if (count > 2 && i < count - 1)
               text.append (", "); // Oxford comma
            if (i == count - 2)
               text.append (" or ");
            i++;
         }
         text.append (".");
      }
   }

   @Override
   public String getName()
   {
      return name;
   }
   
   @Override
   public String getText() // for HTML
   {
      return text.toString();
   }
   
   public List<String> getEquipment()
   {
      List<String> allEquipment = new ArrayList<>(); 
      allEquipment.addAll (equipment);
      allEquipment.addAll (specialEquipment);
      return allEquipment;
   }
   
   public Type getType()
   {
      return type;
   }
   
   public String getImage()
   {
      return image;
   }
   
   @Override
   public int compareTo (final Event e)
   {
      return name.compareTo (e.name);
   }
   
   @Override
   public boolean equals (final Object other)
   {
      if (other instanceof Event)
         return name.equals (((Event) other).name);
      return false;
   }
   
   private static void populate()
   {
      add ("All About the Numbers", Type.STD, 0, "Jump to the nearest level 1 cache you have not yet found.  End your turn unless", "Numbers", "Jeep", "Mountain Bike");
      add ("Angry Bees", Type.STD, 0, "You accidentally disturb a hive of bees; end your turn. The player on your right moves your token 2 tiles in any direction.", null);
      add ("Archive Cache", Type.NOW, -2, "Remove any unoccupied cache from the board.", null, "Repair Kit");
      add ("Bad Coordinates", Type.STD, 0, "End your turn.", null, "Cell Phone");
      add ("Bad Weather", Type.STD, 0, "End your turn.", null, "Emergency Radio", "Rain Jacket");
      add ("Barbed Wire", Type.STD, 0, "-2 to your roll.", null, "Utility Tool");
      add ("Bear Encounter", Type.STD, 0, "End your turn. The player on your right moves your token 1 tile in any direction.", null, "Whistle", "Ol' Blue");
      add ("Blisters", Type.STD, 0, "You may move at most 2 tiles (per turn) until you skip a turn.", null, "Hiking Boots", "First-aid Kit");
      add ("Bomb Squad", Type.STD, 0, "Remove the cache closest to your token from the board.  Ignore caches with player tokens in the same tile.", null);
      add ("Bragging Rights", Type.STD, 0, "Gain 1 point for every <em class=ftf>FTF coin</em> you have.  Lose 2 points if you have none, unless" , null, "Geocoin");
      add ("Broken Wrist", Type.STD, 0, " Jump your token to the Hospital.  End your turn, and skip your next turn.  Lose 4 points if you play this card on another player.", null, "[First-aid Kit]");
      add ("Bug Hog", Type.STD, 0, "Lose 1 point for each <em class=bug>Travel Bug</em> you have.", null, "Swag Bag");
      add ("Bug Swap", Type.STD, 0, "Trade <em class=bug>Travel Bugs</em> with any other player.  You both gain 1 point.  If you have none, you may draw one first.", null);
      add ("Bushwhacked", Type.STD, 0, "If you are not on a path or Urban tile, lose 1 point and get -2 on your roll.", null, "Trail Guide", "Ol' Blue");
      add ("Cache Pirates", Type.NOW, 0, "Remove the cache farthest from your token from the board.", null);
      add ("Call from the Boss", Type.STD, 0, "End your turn and skip the next one.  Then draw one Equipment card.", null, "Laptop");
      add ("Campsite Confusion", Type.NOW, 0, "All players give one Equipment card to the player on their left.", null);
      add ("Careless Cacher", Type.STD, -1, "You didn't re-hide your last cache well.  All Search rolls for it get +1.", null, "Mirror");
      add ("Caught in the Rain", Type.STD, 0, "Discard any one piece of equipment.", null, "Rain Jacket", "Emergency Radio");
      add ("Creek Crossing", Type.STD, 0, "Oops! You slip crossing a small creek. End your turn.", null, "Walking Stick", "Hiking Staff");
      add ("Dead Batteries", Type.STD, 0, "End your turn.  You may discard any Equipment card instead.", null, "Batteries");
      add ("Dollar Store", Type.STD, 0, "If you're within 3 tiles of an Urban tile, end your turn.", null, "Swag Bag");
      add ("Drought", Type.NOW, 0, "Streams only cost 1 movement point to cross (for the next 3 rounds).", null);
      add ("Equipment Rental", Type.STD, 0, "Select any equipment card from the deck.  You may keep this equipment until you roll a <em class=dnf>DNF</em>.", null);
      add ("Eureka!", Type.ANY, 0, "You may discard this card to solve any Puzzle Cache, or guarantee a successful Search roll.", null);
      add ("Feed The Trolls", Type.STD, 0, "You stop to read the forums; end your turn.", null, "Laptop");
      add ("Fire Ants", Type.STD, 0, "End your turn.  The player on your right moves your token 1 tile in any direction.", null, "Gorp", "Long Pants");
      add ("Flash Flood", Type.STD, 0, "Remove the nearest cache that is next to a stream from the board.  End your turn.", null, "Waders");
      add ("Fox Hunt", Type.STD, 1, "You spot a red fox.  If you have <em class=equipment>Ol'&nbsp;Blue</em>, end your turn.", null);
      add ("Fresh Snow", Type.NOW, 0, "-1 to all rolls this round.", "Snow");
      add ("Ground Zero", Type.ANY, 0, "You may jump your token to the nearest cache location.", null);
      add ("Happy Birthday!", Type.STD, 0, "Draw 1 Equipment card.", null);
      add ("Heat Wave", Type.STD, 0, "-2 to your roll.", null, "Bandana", "Camel Pack", "Hat", "Water Bottle");
      add ("Heavy Rain", Type.NOW, 0, "Streams cost 4 movement points to cross (for the next 3 rounds).", null);
      add ("Helpful Hint", Type.ANY, 0, "You may discard this card to get +2 on your roll.", null);
      add ("Hidden Path", Type.STD, 0, "You find a hidden path.  If you were moving, you get +3 on your Move roll.  If you were searching, you may take another turn.", null);
      add ("In a Hurry", Type.STD, 0, "You may move an additional 2 tiles this turn.  Lose 1 point unless", null, "Letterbox Stamp");
      add ("Is That Venomous?", Type.STD, 0, "End your turn.  Roll the dice.  If you roll a <em class=dnf>DNF</em>, jump your token to the Hospital.", "Black Widow", "Field Guide", "Gloves");
      add ("Ivory-billed Woodpecker", Type.STD, 0, "You spot a very rare bird.  Gain 2 points (4 if you have the <em class=equipment>Camera</em>).", null);
      add ("Leave No Trace", Type.STD, 2, "You carefully avoided the fragile flora.", null);
      add ("Litterbug", Type.STD, -4, "You left some garbage in the woods.", null, "CITO Bag");
      add ("Lost and Found", Type.STD, 2, "Draw 1 Equipment card and give it to any other player.", null);
      add ("Lost Travel Bug", Type.STD, 0, "Discard a <em class=bug>Travel Bug</em>.  Lose 3 points.  Ignore this card if you don't have a <em class=bug>Travel Bug</em>.", null, "Belt Pack");
      add ("Lost!", Type.STD, 0, "The player on your right moves your token in any direction the number tiles equal to your roll.", "Lost", "Compass", "Map", "Whistle");
      add ("May I Borrow That?", Type.STD, -1, "Take any Equipment card from any player that has more points than you.", "Borrow");
      add ("Meet and Greet", Type.NOW, 0, "Select a random location.  Any player may jump to that tile.  All players who do receive 2 points.  Shuffle this card back into the deck.", null);
      add ("Meet the Muggles", Type.STD, 2, "You stop to explain geocaching.  End your turn unless", null, "CITO Bag", "Safari Vest");
      add ("Missed Anniversary", Type.STD, -1, "Skip your next turn.  Discard any Equipment card.", null);
      add ("Mosquitoes", Type.STD, 0, "-1 to your roll; -2 if you are in a Forest tile; end your turn if you are in a Swamp tile.", null, "Bandana", "Insect Repellent");
      add ("Mud Slide", Type.STD, 0, "End your turn.  Discard one of your Equipment cards unless", null, "Survival Strap");
      add ("Muggled!", Type.STD, 0, "The nearest level 1 or 2 Traditional cache has been emptied.  Remove any <em class=bug>Travel Bugs</em> it has.  It may still be found, but finders don't get to draw Equipment cards.", null);
      add ("Night Caching", Type.STD, 0, "All players not on an Urban tile skip their next turn.", null, "Flashlight", "Head Lamp");
      add ("Not About the Numbers", Type.STD, 0, "Gain 1 point for each level <em class=diff5>5</em> cache you have found.  If none, lose 2 points unless", null, "Binoculars", "Rope");
      add ("Out of Ink", Type.STD, 0, "End your turn while you try to improvise something to write with.", null, "Letterbox Stamp", "Pocket Knife");
      add ("Park Closed", Type.STD, 0, "End your turn.  Does not affect <em class=cacher>Ranger&nbsp;Rachel</em>.", null);
      add ("Parking Ticket", Type.STD, -1, "Discard an Equipment card.", null, "Lucky Charm");
      add ("Pawn Shop", Type.STD, 0, "Draw five Equipment cards.  You may trade any of your Equipment cards for any of those five.", null);
      // TODO rename? maybe Well Equipped?
      add ("Perfect Pair", Type.STD, 0, "You may discard one Equipment card to select any other one you want from the deck.", null);
      add ("Point of Interest", Type.STD, 0, "End your turn.  Gain 2 points (3 if you have the <em class=equipment>Binoculars</em> or <em class=equipment>Camera</em>).", "Scenic View", "[Binoculars]", "[Camera]");
      add ("Poison Ivy", Type.STD, 0, "After finding your next cache, jump your token to your starting tile, and skip a turn.", null, "Gloves", "Field Guide");
      add ("Private Property", Type.STD, -2, "Explain what you're up to; end your turn.", null, "CITO Bag", "Trail Guide");
      add ("Quench Your Thirst", Type.STD, 0, "End your turn.", null, "Camel Pack", "Water Bottle");
      add ("Recycle", Type.STD, 0, "Discard this card to draw any Event card from the discard pile.", null);
      add ("Repair a Cache", Type.STD, 3, "You stop to repair a cache.  End your turn unless", null, "Duct Tape", "Repair Kit", "Utility Tool");
      add ("Save a Turtle", Type.STD, 3, "You stop to help a turtle across the road.  End your turn unless", null, "Field Guide");
      add ("Shortcut", Type.STD, 0, "Instead of rolling, you may jump up to 6 tiles this turn (or on your next Move if you were searching).", null);
      add ("Signal Bounce", Type.STD, 0, "The player on your right moves your token 1 tile in any direction.", null, "Compass", "Antenna");
      add ("Soggy Log", Type.STD, -1, "You let the last logbook you signed get wet.", null, "Rain Jacket");
      add ("Solar Flares", Type.NOW, 0, "-2 on all Search rolls this round.", null, "Antenna");
      add ("Souvenir Day", Type.STD, 0, "Anyone finding a cache this round gains 1 extra point.", null);
      add ("Spider Webs", Type.STD, 0, "-1 to your roll.", null, "Walking Stick");
      add ("Steep Slope", Type.STD, 0, "If moving, end your turn.  If searching, -1 to your roll.", null, "Hiking Staff", "Rope");
      add ("Stick Race", Type.STD, 1, "Jump your token to the nearest bridge, and end your turn.", "Pooh Sticks");
      add ("Stop For Directions", Type.STD, 0, "End your turn, but get +3 to your next Move roll.", null, "Cell Phone", "Map");
      add ("Suspicious Activity", Type.STD, 0, "End your turn.  Roll the dice: if you roll a <em class=dnf>DNF</em>, jump to the Police tile.", "Police", "Backpack", "CITO Bag");
      add ("There's the Path!", Type.STD, 0, "You stumble upon the path you should have taken. You get -1 on this roll, but +2 on your next roll.", "Path", "Trail Guide");
      add ("Thorns", Type.STD, 0, "-2 to your roll.", null, "Gloves", "Long Pants");
      add ("Ticks", Type.STD, 0, "Skip your next turn (you may finish this turn normally).", null, "Gaiters", "Insect Repellent", "Long Pants");
      add ("Trade Up", Type.STD, 2, "You helped restock an empty cache.  Discard an Equipment card unless", null, "Swag Bag");
      add ("Trash Out", Type.STD, 3, "You stop to pick up some trash.  End your turn unless", null, "CITO Bag");
      add ("Twist and Shout", Type.NOW, 0, "All event cards (in play or held) are moved to the next player (clockwise).", null);
      add ("Twisted Ankle", Type.NOW, 0, "End your turn.  If you are not on a path, skip your next turn too.", null, "FRS Radio", "[First-aid Kit]");
      add ("Upgrade", Type.STD, 1, "Draw an Equipment card, then give any one Equipment card you have to the player with the fewest equipment cards (other than yourself).", null);
      add ("Waterfall", Type.NOW, 0, "The next player to end their turn at a waterfall gets 3 points (4 if they have the <em class=equipment>Camera</em>).", null, "[Camera]");
      add ("Waypoint My Car?", Type.STD, 0, "The player on your right moves your token 2 tiles in any direction.", "Waypoint", "Map", "Trail Guide");
      add ("Well-stocked Cache", Type.ANY, 0, "You may discard this card when you find a cache, to choose from 3 extra Equipment cards.", null);
      add ("Where's George?", Type.STD, -1, "You ran out of trinkets to trade.", "Wheres George", "Swag Bag");
      add ("Winter is Coming", Type.STD, 0, "Sometimes it's just too cold for caching.  End your turn, and jump your token back to your starting location.", null, "Emergency Radio");
      add ("Yellow Jackets", Type.STD, 0, "+2 if moving, or -2 if searching.", null);
      add ("You're Fired!", Type.STD, 0, "Discard any Equipment card.  Take an extra turn after this one.", null);
      
      // New Species - 1 free equip?
      
      // add ("13-Year Cicadas", Type.NOW, 0, "All players get -1 on all Search rolls in Forest tiles for the next 2 rounds.", null);
      // add ("Ancient Ruins", Type.STD, 1, "You discover an artifact.  Draw 1 Equipment card.", null);
      // add ("Cache Maintenance", Type.STD, 0, "The cache nearest to you needs maintenance.  The player on your right moves the cache 1 tile in any direction.", null);
      // add ("Early Frost", Type.STD, 0, "End your turn.", null, "Gloves");
      // add ("Hide a Cache", EventType.STD, 2, "Add a new cache onto the board (in a random location).  Shuffle this card back into the deck.", null);
      // add ("It's a Dry Heat", EventType.STD, 0, "-2 to your roll.", null, "Camel Pack", "Canteen");
      // add ("No Trespassing", EventType.STD, -1, "The player on your right moves your token 1 tile in any direction.", null, "Trail Guide");
      // add ("Sneaky Search", Type.ANY, -1, "Keep this card.  When you find a cache, you may discard it.  Other cachers on that tile do not get credit for the find.", null);
      // add ("Weather Forecast", EventType.ANY, 0, "Keep this card.  You may discard it to avoid the effects of any weather event played on you.", null);
   }
   
   private static void add (final String name, final Type type,
                            final int points, final String effect,
                            final String image, final String... protection)
   {
      Event event = new Event (name, type, points, effect, image, protection);
      EVENTS.put (event.getName(), event);
   }
   
   /*
   private static void dump (final Event event)
   {
      StringBuilder sb = new StringBuilder();
      sb.append (event.name);
      sb.append (", \"");
      sb.append (event.text);
      sb.append ("\"");

      String s = sb.toString();
      s = s.replace ("&nbsp;", " ");
      s = s.replaceAll ("<[^>]+>", "");
      System.out.println (s);
   }
   */
   
   private static void showEvents()
   {
      /*
      for (Event event : EVENTS.values())
         System.out.println(event.name.replaceAll("&nbsp;", " "));
      System.out.println();
      */

      for (Event event : EVENTS.values())
      {
         if (event.name == CardUtils.BLANK)
            continue;
         System.out.print (StringUtils.pad (event.toString().replaceAll("&nbsp;", " "), 30));
         for (String eq : event.equipment)
            System.out.print (eq.replaceAll("&nbsp;", " ") + ", ");
         for (String eq : event.specialEquipment)
            System.out.print (eq.replaceAll("&nbsp;", " ") + ", ");
         System.out.println();
      }
      System.out.println();
   }
   
   public static void main (final String[] args)
   {
      showEvents();
      
      HtmlGenerator htmlGen = new HtmlGenerator(12, 4, 150, 85, 75, 80);
      // HtmlGenerator htmlGen = new HtmlGenerator(12, 4, 180, 120, 100, 120);
      htmlGen.printEvents(EVENTS);
      
      ImageGenerator imgGen = new ImageGenerator(ImageStats.getEventStats(), false);
      for (Event event : EVENTS.values())
         imgGen.publish(event);
      System.out.println();
      
      System.out.println ("Validating Equipment References:");
      System.out.flush();
      for (Event event : EVENTS.values())
         for (String eq : event.getEquipment())
            if (Equipment.EQUIPMENT.get (eq) == null)
               System.err.println ("Missing equipment for " + event + ": " + eq);
      System.err.flush();
      System.out.println();
      System.out.flush();
      
      System.out.println ("Negative Events (without mitigating equipment):");
      Pattern pattern = Pattern.compile ("\\b[Ll]ose\\b|\\b[Ee]nd\\b|-[1-9]");
      for (Event event : EVENTS.values())
         if (pattern.matcher (event.text).find() && // bad event
             !event.text.toString().contains ("gain") && // no points gained
             event.equipment.isEmpty() && event.specialEquipment.isEmpty()) // no counter
            System.out.println ("  " + event.name.replace ("&nbsp;", " "));
      System.out.println();
      System.out.flush();
   }
}
