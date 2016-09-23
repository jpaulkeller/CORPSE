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
   private String icon;
   private StringBuilder text;
   private Type type;
   
   // ignore event if you have any of these   
   private List<String> equipment = new ArrayList<>();
   private List<String> specialEquipment = new ArrayList<>(); // these require special text

   public Event (final String name, final Type type,
                 final int points, final String effect,
                 final String image, final String icon, final String... protection)
   {
      this.name = name;
      this.type = type;
      this.image = CardUtils.findImage ("Events", image != null ? image : name);
      if (this.image == null)
         this.image = CardUtils.findImage("TGC/Icons", "Missing");
      
      if (icon != null)
      {
         this.icon = CardUtils.findImage("TGC/Icons", icon);
         if (this.icon == null)
            this.icon = CardUtils.findImage("TGC/Icons", "Missing");
      }
      
      for (String eqName : protection)
      {
         Matcher m = SPECIAL_EQUIPMENT.matcher (eqName);
         boolean special = m.matches(); 
         if (special)
            specialEquipment.add (m.group(1));
         else
            equipment.add (eqName);
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
            text.append ("Ignore this event if you have ");
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
   
   public String getIcon()
   {
      return icon;
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
      add ("All About the Numbers", Type.STD, 0, "Jump to the closest level <em class=diff1>.1.</em> or <em class=diff1>.2.</em> cache you have not yet found.  End your turn unless", "Numbers", null, "Jeep", "Mountain Bike");
      add ("Angry Bees", Type.STD, 0, "You accidentally disturb a hive of bees; end your turn. The player on your right moves your token 2 tiles in any direction.", null, "Move Other 2");
      add ("Archive Cache", Type.NOW, -2, "Remove any unoccupied cache from the board.", null, "Point -2", "Repair Kit");
      add ("Bad Coordinates", Type.STD, 0, "End your turn.", null, null, "Cell Phone");
      add ("Bad Weather", Type.STD, 0, "End your turn.", null, null, "Emergency Radio", "Rain Jacket");
      add ("Barbed Wire", Type.STD, 0, "-2 to your roll.", null, "Roll -2", "Utility Tool");
      add ("Bear Encounter", Type.STD, 0, "End your turn. The player on your right moves your token 1 tile in any direction.", null, "Move Other 1", "Whistle", "Ol' Blue");
      add ("Blisters", Type.STD, 0, "You may move at most 2 tiles (per turn) until you skip a turn.", null, "Move 2 Cap", "Hiking Boots", "First-aid Kit");
      add ("Bomb Squad", Type.STD, 0, "Remove the cache closest to your token from the board.  Ignore caches with player tokens in the same tile.", null, null);
      add ("Bragging Rights", Type.STD, 0, "Gain one point for every <em class=ftf>.F.</em> coin you have.  Lose 2 points if you have none, unless" , null, "Points +1", "Geocoin");
      add ("Broken Wrist", Type.STD, 0, "Jump your token to the Hospital, and end your turn.  Lose 4 points if you play this card on another player.", null, "Move Hospital", "[First-aid Kit]");
      add ("Bug Hog", Type.STD, 0, "Lose one point for each <em class=bug>Travel Bug</em> you have.", null, "Points -1", "Swag Bag");
      add ("Bug Swap", Type.STD, 0, "Trade <em class=bug>Travel Bugs</em> with any other player.  You both gain 1 point.  If you have none, you may draw one first.", null, "Point +1");
      add ("Bushwhacked", Type.STD, 0, "If you are not on a path or Urban tile, lose 1 point and get -2 on your roll.", null, "Point -1", "Trail Guide", "Ol' Blue");
      add ("Call from the Boss", Type.STD, 0, "End your turn and skip the next one.  Then draw one Equipment card.", null, "Equip 1", "Laptop");
      add ("Campsite Confusion", Type.NOW, 0, "All players give one Equipment card to the player on their left.", null, "Equip Cycle");
      add ("Careless Hide", Type.NOW, -2, "From now on, all Search rolls for the last cache you found get +1.", null, "Point -2", "Mirror");
      add ("Caught in the Rain", Type.STD, 0, "Discard any one piece of equipment.", null, "Equip -1", "Rain Jacket", "Emergency Radio");
      add ("Creek Crossing", Type.STD, 0, "Oops! You slip crossing a small creek. End your turn.", null, null, "Walking Stick", "Hiking Staff");
      add ("Dead Batteries", Type.STD, 0, "End your turn.  You may discard any Equipment card instead.", null, null, "Batteries");
      add ("Dollar Store", Type.STD, 0, "If you're within 3 tiles of an Urban tile, end your turn.", null, null, "Swag Bag");
      add ("Drought", Type.NOW, 0, "For the next 3 rounds, streams only cost 1 movement point to cross.", null, "Move Stream 1");
      add ("Earth Cache", Type.STD, 3, "You learn some interesting facts while logging an Earth Cache.", null, "Point +3");
      add ("Ensemble", Type.STD, 0, "You may discard one Equipment card to select any other one you want from the deck.", null, null);
      add ("Equipment Rental", Type.STD, 0, "Select any equipment card from the deck.  You may keep this equipment until you roll a <em class=dnf>.D.</em>.", null, "Equip 1");
      add ("Eureka!", Type.ANY, 0, "You may discard this card to solve any Puzzle Cache, or guarantee a successful Search roll.", null, "Search");
      add ("Feed The Trolls", Type.STD, 0, "You stop to read the forums; end your turn.", null, null, "Laptop");
      add ("Fire Ants", Type.STD, 0, "End your turn.  The player on your right moves your token 1 tile in any direction.", null, "Move Other 1", "Gorp", "Long Pants");
      add ("Flash Flood", Type.STD, 0, "Remove the closest cache that is next to a stream from the board.  End your turn.", null, null, "Waders");
      add ("Fox Hunt", Type.STD, 1, "You spot a red fox.  If you have <em class=equipment>Ol' Blue</em>, end your turn.", null, "Point +1");
      add ("Fresh Snow", Type.NOW, 0, "-1 to all rolls this round.", "Snow", "Roll -1");
      add ("Ground Zero", Type.ANY, 0, "You may jump your token to the closest cache.", null, "Move Cache");
      add ("Happy Birthday!", Type.STD, 0, "Draw 1 Equipment card.", null, "Equip 1");
      add ("Heat Wave", Type.STD, 0, "-2 to your roll.", null, "Roll -2", "Bandana", "Camel Pack", "Hat", "Water Bottle");
      add ("Heavy Rain", Type.NOW, 0, "For the next 3 rounds, streams cost 4 movement points to cross.", null, "Move Stream 4");
      add ("Helpful Hint", Type.ANY, 0, "You decrypt the clue.  Discard this card to get +2 on your roll.", null, "Roll +2");
      add ("Hidden Path", Type.ANY, 0, "You find a hidden path.  Double your Move roll this turn.", null, "Move x2");
      add ("In a Hurry", Type.STD, 0, "You may move 2 extra tiles this turn (even after searching).  Lose 1 point unless", null, "Move 2", "Letterbox Stamp");
      add ("Is That Venomous?", Type.STD, 0, "End your turn.  Roll the dice.  If you roll a <em class=dnf>.D.</em>, jump your token to the Hospital.", "Black Widow", "Move Hospital", "Field Guide", "Gloves");
      add ("Landslide", Type.STD, 0, "End your turn.  Discard one of your Equipment cards unless", null, "Equip -1", "Survival Strap");
      add ("Leave No Trace", Type.STD, 2, "You carefully avoided the fragile flora.", null, "Point +2");
      add ("Letterbox", Type.STD, 0, "You find a Letterbox Hybrid cache.  Gain 1 point (3 if you have the <em class=equipment>Letterbox Stamp</em>).", null, "Point +1", "[Letterbox Stamp]");
      add ("Litterbug", Type.STD, -4, "You left some garbage in the woods.", null, "Point -4", "CITO Bag");
      add ("Lost and Found", Type.STD, 2, "Draw 1 Equipment card and give it to any other player.", null, "Point +2");
      add ("Lost Travel Bug", Type.STD, 0, "If you have a <em class=bug>Travel Bug</em>, discard it and lose 3 points.", null, "Point -3", "Belt Pack");
      add ("Lost!", Type.STD, 0, "The player on your right moves your token in any direction the number tiles equal to your roll.", "Lost", "Move Other Roll", "Compass", "Map", "Whistle");
      add ("May I Borrow That?", Type.STD, -1, "Take any Equipment card from any player that has more equipment cards than you.", "Borrow", "Point -1");
      add ("Meet and Greet", Type.NOW, 0, "Select a random location; any player may jump to that tile.  All players who do receive 2 points.  Shuffle this card back into the deck.", null, "Point +2");
      add ("Meet the Muggles", Type.STD, 2, "You stop to explain geocaching.  End your turn unless", null, "Point +2", "CITO Bag", "Safari Vest");
      add ("Mega Event", Type.NOW, 0, "Select a random location; all players jump to that tile.  Each player rolls the dice and gains that many points.", null, "Point +Roll");
      add ("Mosquitoes", Type.STD, 0, "-1 to your roll; -2 if you are in a Forest tile; end your turn if you are in a Swamp tile.", null, "Roll -1", "Bandana", "Insect Repellent");
      add ("Muggled!", Type.STD, 0, "The closest level 1 or 2 cache has been emptied.  Remove any <em class=bug>Travel Bugs</em> it has.  It may still be found, but finders don't get to draw Equipment cards.", null, null);
      add ("Night Caching", Type.STD, 0, "All players not on an Urban tile skip their next turn.", null, null, "Flashlight", "Head Lamp");
      add ("Not About the Numbers", Type.STD, 0, "Gain one point for each level 5 cache, Multi-cache, or Puzzle cache you have found.  If none, lose 2 points unless", null, null, "Binoculars", "Rope");
      add ("Out of Ink", Type.STD, 0, "End your turn while you improvise a way to sign the logbook.", null, null, "Letterbox Stamp", "Pocket Knife");
      add ("Park Closed", Type.STD, 0, "End your turn.  Does not affect <em class=cacher>Ranger Rachel</em>.", null, null);
      add ("Parking Ticket", Type.STD, -1, "Discard an Equipment card.", null, "Equip -1", "Lucky Charm");
      add ("Pawn Shop", Type.STD, 0, "Draw five Equipment cards.  You may trade any of your Equipment cards for any of those five.", null, null);
      add ("Poison Ivy", Type.STD, 0, "Jump your token to your starting tile, and end your turn.", null, "Move Start", "Gloves", "Field Guide");
      add ("Private Property", Type.STD, -2, "Explain what you're up to; end your turn.", null, "Point -2", "CITO Bag", "Trail Guide");
      add ("Quench Your Thirst", Type.STD, 0, "End your turn.", null, null, "Camel Pack", "Water Bottle");
      add ("Rare Bird", Type.STD, 0, "You spot an ivory-billed woodpecker.  Gain 2 points (4 if you have the <em class=equipment>Camera</em>).", null, "Point +2", "[Camera]");
      add ("Recycle", Type.STD, 0, "Discard this card to draw any Event card from the discard pile.", null, null);
      add ("Repair a Cache", Type.STD, 3, "You stop to repair a cache.  End your turn unless", null, "Point +3", "Duct Tape", "Repair Kit", "Utility Tool");
      add ("Save a Turtle", Type.STD, 3, "You stop to help a turtle across the road.  End your turn unless", null, "Point +3", "Field Guide");
      add ("Scenic View", Type.STD, 0, "End your turn.  Gain 2 points (3 if you have the <em class=equipment>Binoculars</em> or <em class=equipment>Camera</em>).", null, "Point +2", "[Binoculars]", "[Camera]");
      add ("Shortcut", Type.ANY, 0, "Discard to move up to 5 more tiles this turn (even after searching).", null, "Move 5");
      add ("Signal Bounce", Type.STD, 0, "The player on your right moves your token 1 tile in any direction.", null, "Move Other 1", "Compass", "Antenna");
      add ("Soggy Log", Type.STD, -1, "You let the last logbook you signed get wet.", null, "Point -1", "Rain Jacket");
      add ("Solar Flares", Type.NOW, 0, "-2 on all Search rolls this round.", null, "Search -2", "Antenna");
      add ("Souvenir Day", Type.STD, 0, "Anyone finding a cache this round gains 1 extra point.", null, "Point +1");
      add ("Spider Webs", Type.STD, 0, "-1 to your roll.", null, "Roll -1", "Walking Stick");
      add ("Steep Slope", Type.STD, 0, "If moving, end your turn.  If searching, -1 to your roll.", null, "Search -1", "Hiking Staff", "Rope");
      add ("Stick Race", Type.STD, 1, "Jump your token to the closest bridge, and end your turn.", null, "Move Bridge");
      add ("Stop For Directions", Type.STD, 0, "End your turn, but get +3 to your next Move.", null, "Move 3", "Cell Phone", "Map");
      add ("Suspicious Activity", Type.STD, 0, "End your turn.  Roll the dice.  If you roll a <em class=dnf>.D.</em>, jump to the Police tile.", null, "Move Police", "Backpack", "CITO Bag");
      add ("There's the Path!", Type.STD, 0, "You stumble upon the path you should have taken. You get -1 on this roll, but +2 on your next roll.", "Path", "Roll -1", "Trail Guide");
      add ("Thorns", Type.STD, 0, "-2 to your roll.", null, "Roll -2", "Gloves", "Long Pants");
      add ("Ticks", Type.STD, 0, "End your next turn.", null, null, "Gaiters", "Insect Repellent", "Long Pants");
      add ("Trade Up", Type.STD, 2, "You helped restock an empty cache.  Discard an Equipment card unless", null, "Point +2", "Swag Bag");
      add ("Trash Out", Type.STD, 3, "You stop to pick up some trash.  End your turn unless", null, "Point +3", "CITO Bag");
      add ("Twist and Shout", Type.NOW, 0, "All event cards (in play or held) are moved to the next player (clockwise).", null, null);
      add ("Twisted Ankle", Type.NOW, 0, "End your turn.  If you are not on a path, skip your next turn too.", null, null, "FRS Radio", "[First-aid Kit]");
      add ("Upgrade", Type.STD, 1, "Draw an Equipment card, then give any one of your Equipment cards to another player of your choice.", null, "Point +1");
      add ("Waterfall", Type.NOW, 0, "The next player to end their turn at a waterfall gets 3 points (4 if they have the <em class=equipment>Camera</em>).", null, "Point +3", "[Camera]");
      add ("Waypoint My Car?", Type.STD, 0, "The player on your right moves your token 2 tiles in any direction.", "Waypoint", "Move Other 2", "Map", "Trail Guide");
      add ("Well-stocked Cache", Type.ANY, 0, "You may discard this card when you find a cache, to choose from 3 extra Equipment cards.", null, null);
      add ("Where's George?", Type.STD, -1, "You ran out of trinkets to trade.", "Wheres George", "Point -1", "Swag Bag");
      add ("Winter is Coming", Type.STD, 0, "Sometimes it's just too cold for caching.  Jump your token back to your starting location, and end your turn.", null, "Move Start", "Emergency Radio");
      add ("Yellow Jackets", Type.STD, 0, "+2 if moving, or -2 if searching.", null, "Search -2");
      
      // New Species (moth) - 3 points
      // Detour
      // Timber!
      // Lightning Strike
      // Mega-event
      // Webcam Cache
      // Virtual Cache
      // etc
      
      // Famous Quotes series:
      // Not all those who wander are lost.
      // Pascal - We seek the very seeking...
      // I took the path less travelled...
      
      // add ("13-Year Cicadas", Type.NOW, 0, "All players get -1 on all Search rolls in Forest tiles for the next 2 rounds.", null, null);
      // add ("Ancient Ruins", Type.STD, 1, "You discover an artifact.  Draw 1 Equipment card.", null, null);
      // add ("Cache Maintenance", Type.STD, 0, "The cache closest to you needs maintenance.  The player on your right moves the cache 1 tile in any direction.", null, null);
      // add ("Cache Pirates", Type.NOW, 0, "Remove the cache farthest from your token from the board.", null, null);
      // add ("Early Frost", Type.STD, 0, "End your turn.", null, null, "Gloves");
      // add ("Hide a Cache", EventType.STD, 2, "Add a new cache onto the board (in a random location).  Shuffle this card back into the deck.", null, null);
      // add ("It's a Dry Heat", EventType.STD, 0, "-2 to your roll.", null, null, "Camel Pack", "Canteen");
      // add ("Missed Anniversary", Type.STD, -1, "End your turn.  Discard any Equipment card.", null, "Equip -1");
      // add ("Sneaky Search", Type.ANY, -1, "Keep this card.  When you find a cache, you may discard it.  Other cachers on that tile do not get credit for the find.", null, null);
      // add ("Weather Forecast", EventType.ANY, 0, "Keep this card.  You may discard it to avoid the effects of any weather event played on you.", null, null);
      // add ("You're Fired!", Type.STD, 0, "Discard any Equipment card.  Take an extra turn after this one.", null, "Equip -1");
   }
   
   private static void add (final String name, final Type type,
                            final int points, final String effect,
                            final String image, final String icon, final String... protection)
   {
      Event event = new Event (name, type, points, effect, image, icon, protection);
      EVENTS.put (event.getName(), event);
   }
   
   private static void showEvents()
   {
      for (Event event : EVENTS.values())
      {
         if (event.name == CardUtils.BLANK)
            continue;
         System.out.print (StringUtils.pad (event.toString(), 30));
         for (String eq : event.equipment)
            System.out.print (eq + ", ");
         for (String eq : event.specialEquipment)
            System.out.print (eq + ", ");
         System.out.println();
      }
      System.out.println();
   }
   
   private static void validate()
   {
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
            System.out.println ("  " + event.name);
      System.out.println();
      System.out.flush();
   }
   
   public static void main (final String[] args)
   {
      showEvents();
      
      HtmlGenerator htmlGen = new HtmlGenerator(9, 3, 200, 95, 90, 142);
      htmlGen.printEvents(EVENTS);
      
      ImageGenerator imgGen = new ImageGenerator(ImageStats.getEventStats(), false);
      for (Event event : EVENTS.values())
         imgGen.publish(event);
      System.out.println();
      
      validate();
   }
}
