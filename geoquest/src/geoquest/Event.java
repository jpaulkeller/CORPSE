package geoquest;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import str.StringUtils;

public class Event implements Card, Comparable<Event>
{
   public static final String COLOR = "#FFC4D2"; 

   enum Type { Std, Now, Discard };
   
   private static final Pattern SPECIAL_EQUIPMENT = Pattern.compile ("\\[(.+)\\]");

   static final Map<String, Event> EVENTS = new TreeMap<String, Event>();
   static { populate(); }

   private String name;
   private String image;
   private StringBuilder text;
   private Type type;
   
   // ignore event if you have any of these   
   private List<String> equipment = new ArrayList<String>();
   private List<String> specialEquipment =
      new ArrayList<String>(); // these require special text

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
            text.append ("You gain " + points + " point");
         else if (points < 0)
            text.append ("You lose " + (-points) + " point");
         if (points > 1 || points < -1)
            text.append ("s");
         text.append (".");
      }
   }

   private void appendIgnore ()
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
            if (i == 0 && !equip.equals ("Ol' Blue"))
               text.append ("the ");
            text.append ("<em class=equipment>" + equip + "</em>");
            if (i < count - 2)
               text.append (", ");
            else if (i < count - 1)
               text.append (" or ");
            i++;
         }
         text.append (".");
      }
   }

   public String getName()
   {
      return name;
   }
   
   public String getText()
   {
      return text.toString();
   }
   
   public List<String> getEquipment()
   {
      List<String> allEquipment = new ArrayList<String>(); 
      allEquipment.addAll (equipment);
      allEquipment.addAll (specialEquipment);
      return allEquipment;
   }
   
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

   private static void populate()
   {
      add ("13-Year Cicadas", Type.Now, 0, "All players get -1 on all Search rolls in Forest tiles for the next 2 rounds.", null);
      add ("Ancient Ruins", Type.Std, 1, "You discover an artifact.  Draw 1 Equipment card.", null);
      add ("Archive Cache", Type.Now, -2, "Choose any cache (not on the same tile as a player's token), and remove it from the board.", null);
      add ("Bad Coordinates", Type.Std, 0, "End your turn.", null, "Cell Phone", "PDA");
      add ("Bad Weather", Type.Std, 0, "End your turn.", null, "Emergency Radio", "Waterproof Jacket");
      add ("Barbed Wire", Type.Std, 0, "-2 to your roll.", null, "Gloves", "Utility Tool");
      add ("Bear Encounter", Type.Std, 0, "End your turn. The player on your right moves your token 1 tile in any direction.", null,  "Ol' Blue", "Whistle");
      add ("Blisters", Type.Std, 0, "You may move at most 2 tiles (per turn) until you skip a turn.", null, "Hiking Boots", "First-aid Kit");
      add ("Bomb Squad", Type.Std, 0, "Remove the cache closest to your token from the board.  Ignore caches with player tokens in the same tile.", null);
      add ("Bragging Rights", Type.Std, 0, "Gain 1 point for every FTF coin you have.  Lose 1 point if you have none." , null);
      add ("Bug Hog", Type.Std, 0, "Lose 1 point for each Travel Bug you have.", null, "Swag Bag");
      add ("Bug Swap", Type.Std, 0, "Trade travel bugs with any other player.  You both gain 1 point.  If you have none, you may draw one first.", null);
      add ("Broken Wrist", Type.Std, 0, " Jump your token to the Hospital.  End your turn, and skip your next turn.  Lose 4 points if you play this card on another player.", null, "[First-aid Kit]");
      add ("Bushwhacked", Type.Std, 0, "If you are not on a path or Urban tile, lose 1 point and get -2 on your roll.", null, "Ol' Blue", "Trail Guide");
      add ("Cache Maintenance", Type.Std, 0, "The cache nearest to you needed maintenance.  The player on your right moves the cache 1 tile in any direction.", null);
      add ("Cache Pirates", Type.Now, 0, "Remove the cache farthest from your token from the board.", null);
      add ("Call from the Boss", Type.Std, 0, "End your turn and skip the next one.  Then draw one Equipment card.", null, "Laptop");
      add ("Careless Cacher", Type.Std, -2, "You failed to properly re-hide the last cache you found.  All Search rolls for it get +1 (until it's found).", null, "Mirror");
      add ("Caught in the Rain", Type.Std, 0, "Discard any one piece of equipment.", null, "Waterproof Jacket", "Emergency Radio");
      add ("Cold Snap", Type.Std, 0, "Sometimes it's just too cold for caching.  End your turn, and jump your token back to your starting location.", null, "Coat", "Emergency Radio");
      add ("Creek Crossing", Type.Std, 0, "Oops! You slip crossing a small creek. End your turn.", null, "Walking Stick", "Hiking Staff");
      add ("Dead Batteries", Type.Std, 0, "End your turn.  You may discard any Equipment card instead.", "Batteries", "Safari Vest");
      add ("Dollar Store", Type.Std, 0, "If you're within 3 tiles of an Urban tile, end your turn.", null, "Swag Bag");
      add ("Drought", Type.Now, 0, "Streams only cost 1 movement point to cross (for the next 2 rounds).", null);
      add ("Early Frost", Type.Std, 0, "End your turn.", null, "Coat", "Gloves");
      add ("Equipment Rental", Type.Std, 0, "Select any equipment card from the deck.  You may keeps this equipment until you roll a <em class=dnf>DNF</em>.", null);
      add ("Eureka!", Type.Discard, 0, "Keep this card.  You may discard it to solve any Puzzle Cache, or make a successful Search roll.", null);
      add ("Feed The Trolls", Type.Std, 0, "You stop to read the forums; end your turn.", null, "Laptop");
      add ("Fire Ants", Type.Std, 0, "End your turn.  The player on your right moves your token 1 tile in any direction.", null, "Gorp", "Long Pants");
      add ("Flash Flood", Type.Std, 0, "Remove the nearest cache that is next to a stream from the board.  End your turn.", null, "Waders");
      add ("Fox Hunt", Type.Std, 1, "You spot a red fox.  If you have <em class=equipment>Ol'&nbsp;Blue</em>, end your turn.", null);
      add ("Fresh Snow", Type.Now, 0, "-1 to all rolls this round.", "Snow");
      add ("Ground Zero", Type.Std, 0, "You may jump your token to the nearest cache location.  If you do, this counts as your turn.", null);
      add ("Happy Birthday!", Type.Std, 0, "Draw 1 Equipment card.", null);
      add ("Heat Stroke", Type.Std, 0, "-2 to your roll.", null, "Hydration Pack", "Water Bottle", "Hat");
      add ("Helpful Hint", Type.Discard, 0, "Keep this card.  You may discard it to get +2 on your roll.", null);
      add ("Hidden Path", Type.Std, 0, "You find a hidden path.  If you were moving, you get +3 on your Move roll.  If you were searching, you may take another turn.", null);
      add ("It's All About the Numbers!", Type.Std, 0, "Jump to the nearest level 1 cache you have not yet found.  End your turn unless", "Numbers", "Mountain Bike", "Yellow Jeep");
      add ("It's Not About the Numbers!", Type.Std, 0, "You gain 1 point for each Multi-cache or Difficulty 5 cache you have found.  You lose 2 points if you have found none, unless", "Trophy", "Binoculars", "Rope");
      add ("Ivory-billed Woodpecker", Type.Std, 0, "You spot a very rare bird.  Gain 2 points (4 if you have the <em class=equipment>Camera</em>).", null);
      add ("Killer Bees", Type.Std, 0, "You accidentally disturb a hive of angry bees; end your turn. The player on your right moves your token 2 tiles in any direction.", null);
      add ("Leave No Trace", Type.Std, 2, "You carefully avoided the fragile flora.", null);
      add ("Litterbug", Type.Std, -4, "You left some garbage in the woods.", null, "CITO Bag");
      add ("Lost!", Type.Std, 0, "The player on your right moves your token in any direction the number tiles equal to your roll.", "Lost", "Map", "Whistle");
      add ("Lost Travel Bug", Type.Std, 0, "Discard a Travel Bug.  You lose 3 points.  Ignore this card if you don't have a Travel Bug.", null, "Belt Pack");
      add ("Lost and Found", Type.Std, 2, "Draw 1 Equipment card and give it to any other player.", null);
      add ("May I Borrow That?", Type.Std, -1, "Take any Equipment card from any player that has more points than you.", "Borrow");
      add ("Meet and Greet", Type.Now, 0, "Roll a random location.  Any player may jump to that tile.  All players who do receive 2 points.  Shuffle this card back into the deck.", "Event Cache");
      add ("Meet the Muggles", Type.Std, 2, "You stop to explain geocaching.  End your turn unless", null, "CITO Bag", "Geocoin");
      add ("Missed Anniversary", Type.Std, -1, "Skip your next turn.  Discard any Equipment card.", null);
      add ("Mosquitoes", Type.Std, 0, "-1 to your roll; -2 if you are in a Forest tile; end your turn if you are in a Swamp tile.", null, "Insect Repellent");
      add ("Mud Slide", Type.Std, 0, "End your turn.  Discard one of your Equipment cards unless", null, "Survival Strap");
      add ("Muggled!", Type.Std, 0, "The nearest level 1 or 2 Traditional cache has been emptied.  Remove any Travel Bugs it has.  It may still be found, but finders don't get to draw Equipment cards.", null);
      add ("Night Caching", Type.Std, 0, "All players not on an Urban tile skip their next turn.", null, "Flashlight", "Head Lamp");
      add ("Out of Ink", Type.Std, 0, "End your turn while you try to improvise something to write with.", null, "Backpack", "Pocket Knife");
      add ("Park Closed", Type.Std, 0, "End your turn.  Does not affect <em class=cacher>Ranger&nbsp;Rachel</em>.", null);
      add ("Parking Ticket", Type.Std, -1, "Discard an Equipment card.", null, "Lucky Charm");
      add ("Pawn Shop", Type.Std, 0, "Draw 5 Equipment cards.  You may trade in any 1 of your Equipment cards for 1 of those 5.", null);
      add ("Pooh Sticks", Type.Std, 1, "Jump your token to the nearest bridge, and end your turn.", null);
      add ("Poison Ivy", Type.Std, 0, "After finding your next cache, jump your token to your starting tile, and skip a turn.", null, "Gloves", "Field Guide");
      add ("Private Property", Type.Std, -2, "Explain what you're up to; end your turn.", null, "CITO Bag", "Trail Guide");
      add ("Quench Your Thirst", Type.Std, 0, "End your turn.", null, "Hydration Pack", "Water Bottle");
      add ("Repair a Cache", Type.Std, 3, "You stop to repair a cache.  End your turn unless", null, "Duct Tape", "Utility Tool");
      add ("Save a Turtle", Type.Std, 3, "You stop to help a turtle across the road.  End your turn unless", null, "Field Guide");
      add ("Scenic View", Type.Std, 2, "End your turn.  If you have the <em class=equipment>Camera</em>, skip your next turn too.", null, "[Binoculars]", "[Camera]");
      add ("Shortcut", Type.Std, 0, "Instead of rolling, you may jump up to 6 tiles this turn (or on your next move if you were searching).", null);
      add ("Signal Bounce", Type.Std, 0, "The player on your right moves your token 1 tile in any direction.", null, "External Antenna");
      add ("Sneaky Search", Type.Discard, -1, "Keep this card.  When you find a cache, you may discard it.  Other cachers on that tile do not get credit for the find.", null);
      add ("Soggy Log", Type.Std, -1, "Skip a turn after finding your next cache (does not apply to multi-cache stages).", null, "Waterproof Jacket");
      add ("Solar Flares", Type.Now, 0, "No one is able to get a good signal; -2 on all Search rolls this round.", null, "External Antenna");
      add ("Spider Webs", Type.Std, 0, "-1 to your roll.", null, "Walking Stick");
      add ("Steep Slope", Type.Std, 0, "If moving, end your turn.  If searching, -1 to your roll.", null, "Hiking Staff", "Rope");
      add ("Stop For Directions", Type.Std, 0, "End your turn, but get +3 to your next Move roll.", null, "Cell Phone", "Map");
      add ("Suspicious Activity", Type.Std, 0, "End your turn.  Roll the dice: if you roll a <em class=dnf>DNF</em>, jump to the Police tile.", "Police", "CITO Bag");
      add ("Thorns", Type.Std, 0, "-2 to your roll.", null, "Gloves", "Long Pants");
      add ("Ticks", Type.Std, 0, "Skip your next turn (you may finish this turn normally).", null, "Gaiters", "Insect Repellent", "Long Pants");
      add ("Trade Up", Type.Std, 3, "You helped restock an empty cache.  Discard an Equipment card unless", null, "Swag Bag");
      add ("Trash Out", Type.Std, 3, "You stop to pick up some trash.  End your turn unless", null, "CITO Bag");
      add ("Twisted Ankle", Type.Std, 0, "End your turn.  If you are not on a path, skip your next turn too.  Lose 2 points if you play this card on another player.", null, "FRS Radio", "[First-aid Kit]");
      add ("Upgrade", Type.Std, 1, "Draw an Equipment card, then give any one Equipment card you have to the player with the fewest equipment cards (other than yourself).", null);
      add ("Waterfall", Type.Now, 0, "The next player to end their turn at a waterfall gets 3 points (4 if they have the <em class=equipment>Camera</em>).", null, "[Camera]");
      add ("Waypoint My Car?", Type.Std, 0, "The player on your right moves your token 2 tiles in any direction.", "Waypoint", "Map", "Trail Guide");
      add ("Well-stocked Cache", Type.Discard, 0, "Keep this card.  You may discard it when you find a cache, to choose from 3 extra Equipment cards.", null);
      add ("What Kind of Snake Is That?", Type.Std, 0, "End your turn.  Roll the dice.  If you roll a <em class=dnf>DNF</em>, jump your token to the Hospital.", "Snake", "Field Guide", "Hiking Staff");
      add ("Where's George?", Type.Std, -1, "You ran out of trinkets to trade.", "Wheres George", "Swag Bag");
      add ("You're Fired!", Type.Std, 0, "Discard any Equipment card.  Take an extra turn after this one.", null);

      // TODO: new cards, not yet printed
      add ("Heavy Rain", Type.Now, 0, "Streams cost 4 movement points to cross (for the next 2 rounds).", null);
      add ("Twist and Shout", Type.Now, 0, "All event cards (in play or held) are moved to the next player (clockwise).", null);
      add ("Mine!", Type.Std, 0, "You may take one Equipment card from the deck to complete a Combo.", null);
      add ("Campsite Confusion", Type.Now, 0, "All players give one Equipment card to the player on their left.", null);
      
      // add ("It's a Dry Heat", EventType.Normal, 0, "-2 to your roll.", null, "Hydration Pack", "Canteen");
      // add ("Weather Forecast", EventType.Discard, 0, "Keep this card.  You may discard it to avoid the effects of any weather event played on you.", null);
      // add ("Hide a Cache", EventType.Normal, 2, "Add a new cache onto the board (in a random location).  Shuffle this card back into the deck.", null);
      // add ("No Trespassing", EventType.Normal, -1, "The player on your right moves your token 1 tile in any direction.", null, "Trail Guide");
      // There's the Path! (after finding a cache by bushwhacking in, you discover the path you should have taken. +2 to your next Move roll.
   }
   
   private static void add (final String name, final Type type,
                            final int points, final String effect,
                            final String image, final String... protection)
   {
      Event event = new Event (name, type, points, effect, image, protection);
      EVENTS.put (event.getName(), event);
   }
   
   static void print()
   {
      String target = "docs/Events.html";

      try
      {
         PrintWriter out = new PrintWriter (target);

         out.println ("<html>");
         out.println ("<body>\n");
         CardUtils.printStyle (out);

         int i = 0;
         for (Event event : EVENTS.values())
            printCard (out, event, i++);

         // pad with blanks to fill out the sheet
         Event blankCard = new Event (CardUtils.BLANK, Type.Std, 0, CardUtils.BLANK, "Blank");
         if (EVENTS.size() % CardsPerPage > 0)
            for (i = 0; i < CardsPerPage - (EVENTS.size() % CardsPerPage); i++)
               printCard (out, blankCard, i + EVENTS.size());
         
         out.println ("</body>");
         out.println ("</html>");

         out.close();
         
         System.out.println (EVENTS.size() + " events written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   private static final int CardsPerPage = 9;
   private static final int CardsPerRow  = 3;
   private static final int Width    = 200;
   private static final int PictureH = 100;
   private static final int TextH    = 150;

   private static void printCard (final PrintWriter out, final Event event, final int i)
   {
      if (i % CardsPerPage == 0) out.println ("<table cellpadding=15>\n");
      if (i % CardsPerRow == 0) out.println ("<tr>");

      out.println ("<td valign=top>");
      out.println ("  <table border=1>");
      out.println ("    <tr><td align=center bgcolor=" + COLOR + "><b>" + event.name + "</b></td></tr>");
      
      out.print    ("    <tr><td colspan=2 align=center height=" + PictureH + " width=" + Width + ">");
      if (event.image != null)
      {
         out.print    ("<img align=center src=\"" + event.image + "\"");
         if (event.image.startsWith ("Good"))
            out.print (" style=\"border:5px solid yellow\"");
         else if (!event.image.startsWith ("Events"))
            out.print (" style=\"border:5px solid red\"");
         out.print    (" height=" + PictureH + ">");
      }
      out.println  ("</td></tr>");

      int h = event.type != Type.Std ? TextH - 25 : TextH;
      out.println ("    <tr><td height=" + h + " width=" + Width + ">");
      out.println ("        <table cellpadding=5>");
      out.println ("          <tr><td align=center>" + event.text + "</td></tr>");
      out.println ("        </table></td></tr>");

      if (event.type == Type.Now)
         out.println ("    <tr><td align=center bgcolor=salmon><b>Resolve Now</b></td></tr>");
      else if (event.type == Type.Discard)
         out.println ("    <tr><td align=center bgcolor=lightgreen><b>Discard To Resolve</b></td></tr>");
         
      out.println ("  </table></td>\n");

      if (i % CardsPerRow == CardsPerRow - 1) out.println ("</tr>\n");
      if (i % CardsPerPage == CardsPerPage - 1)
         out.println ("</table></td>\n<p><hr><p>\n");
      
      dump (event);
   }

   private static void dump (final Event event)
   {
      StringBuilder sb = new StringBuilder();
      sb.append (event.name);
      sb.append (",\"");
      sb.append (event.text);
      sb.append ("\"");

      String s = sb.toString();
      s = s.replaceAll ("&nbsp;", " ");
      s = s.replaceAll ("<[^>]+>", "");
      System.out.println (s);
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
   
   public static void main (final String[] args)
   {
      // showEvents();
      Event.print();
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