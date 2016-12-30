package geoquest;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import file.FileUtils;
import str.StringUtils;
import utils.ImageTools;

public class Event extends Component implements Comparable<Event>
{
   enum Type
   {
      STD, NOW, ANY
   };

   private static final Pattern SPECIAL_EQUIPMENT = Pattern.compile("\\[(.+)\\]");

   static final Map<String, Event> EVENTS = new TreeMap<>();
   static
   {
      populateFR();
      populate();
      setStats();
   }

   private static void setStats()
   {
      stats.w = 825;
      stats.h = 1125;
      stats.centerX = stats.w / 2;
      stats.centerY = stats.h / 2;
      
      stats.safeMarginW = Math.round(stats.w / 100f * 9.5f);
      stats.safeMarginH = Math.round(stats.h / 100f * 6.7f);
      stats.safeW = stats.w - (stats.safeMarginW * 2);
      stats.safeH = stats.h - (stats.safeMarginH * 2);

      stats.cutMarginH = Math.round(stats.h / 100f * 3.5f);
      stats.cutMarginW = Math.round(stats.w / 100f * 4.8f);

      // size of the art on the final card
      stats.artW = 650;
      stats.artH = 400;
      
      if (Factory.LANGUAGE == Language.FRENCH)
         stats.titleFontName = "EB Garamond 12"; // TODO
      else
         stats.titleFontName = "Bree Serif";
      
      stats.titleFont = new Font(stats.titleFontName, Font.PLAIN, 60);
      stats.titleFont2 = new Font(stats.titleFontName, Font.PLAIN, 50);
      stats.titleFont3 = new Font(stats.titleFontName, Font.PLAIN, 42);
      stats.titleBg = new Color(255, 215, 0); // gold
         
      stats.textFont = new Font("Cabin", Font.PLAIN, 60);
      stats.textFont2 = new Font("Cabin", Font.PLAIN, 54);
      
      stats.playFont = new Font(stats.titleFontName, Font.BOLD, 60);
      stats.playAnyColor = new Color(152, 251, 152); // light green
      stats.playNowColor = new Color(255, 160, 122); // light salmon
   }
   
   public static ImageStats getImageStats()
   {
      return stats;
   }
   
   private StringBuilder sb;
   private String image;
   private String icon;
   private Type type;

   // ignore event if you have any of these
   private List<String> equipment = new ArrayList<>();
   private List<String> specialEquipment = new ArrayList<>(); // these require special text

   public Event(final String name, final Type type, final int points, final String effect, 
                final String image, final String icon, final String... protection)
   {
      this.name = name;
      this.type = type;
      this.image = CardUtils.findImage("Art/Events", image != null ? image : name);
      if (this.image == null)
         this.image = CardUtils.findImage("Icons", "Missing");

      if (icon != null)
      {
         this.icon = CardUtils.findImage("Icons", icon);
         if (this.icon == null)
            this.icon = CardUtils.findImage("Icons", "Missing");
      }

      for (String eqName : protection)
      {
         Matcher m = SPECIAL_EQUIPMENT.matcher(eqName);
         boolean special = m.matches();
         if (special)
            specialEquipment.add(m.group(1));
         else
            equipment.add(eqName);
      }

      // build the text
      text = sb = new StringBuilder();
      if (effect.length() > 0)
         sb.append(effect);

      if (effect.endsWith(" unless"))
      {
         appendIgnore();
         appendPoints(points);
      }
      else
      {
         appendPoints(points);
         appendIgnore();
      }
   }

   private void appendPoints(final int points)
   {
      if (points != 0)
      {
         appendPointsEN(points);
         appendPointsFR(points);
      }
   }
   
   private void appendPointsEN(final int points)
   {
      if (sb.length() > 0)
         sb.append("  ");
      if (points > 0)
         sb.append("Gain " + points + " point");
      else if (points < 0)
         sb.append("Lose " + (-points) + " point");
      if (points > 1 || points < -1)
         sb.append("s");
      sb.append(".");
   }
   
   private void appendPointsFR(final int points)
   {
      CharSequence textFR = TEXT_FR.get(name);
      if (textFR != null)
      {
         StringBuilder fr = new StringBuilder(textFR);
         
         if (fr.length() > 0)
            fr.append("  ");
         if (points > 0)
            fr.append("Gain " + points + " point"); // TODO
         else if (points < 0)
            fr.append("Perdre " + (-points) + " point");
         if (points > 1 || points < -1)
            fr.append("s");
         fr.append(".");
         
         TEXT_FR.put(name, fr);
      }
   }
   
   private void appendIgnore()
   {
      if (!equipment.isEmpty())
      {
         appendIgnoreEN();
         appendIgnoreFR();
      }
   }
   
   private void appendIgnoreEN()
   {
      if (sb.toString().endsWith(" unless"))
         sb.append(" you have ");
      else
      {
         if (sb.length() > 0)
            sb.append("  ");
         sb.append("Ignore this event if you have ");
      }
      
      int count = equipment.size();
      int i = 0;
      for (String equip : equipment)
      {
         if (i == 0) // && !equip.equals ("Ol' Blue"))
            sb.append("the ");
         sb.append("<em class=equipment>" + equip + "</em>");
         if (count > 2 && i < count - 1)
            sb.append(", "); // Oxford comma
         if (i == count - 2)
            sb.append(" or ");
         i++;
      }
      sb.append(".");
   }

   private void appendIgnoreFR()
   {
      CharSequence textFR = TEXT_FR.get(name);
      if (textFR != null)
      {
         StringBuilder fr = new StringBuilder(textFR);
         
         if (sb.toString().endsWith(" unless"))
            fr.append(" vous avez ");
         else
         {
            if (fr.length() > 0)
               fr.append("  ");
            fr.append("Ignorer cet événement si vous avez ");
         }
         
         int count = equipment.size();
         int i = 0;
         for (String equip : equipment)
         {
            if (i == 0)
               fr.append("the ");
            fr.append("<em class=equipment>" + getEquipmentName(equip, Language.FRENCH) + "</em>");
            if (count > 2 && i < count - 1)
               fr.append(", "); // Oxford comma
            if (i == count - 2)
               fr.append(" or ");
            i++;
         }
         fr.append(".");
         
         TEXT_FR.put(name, fr);
      }
   }
   
   public String getEquipmentName(final String eqName, final Language language)
   {
      Equipment eq = Equipment.EQUIPMENT.get(eqName);
      Language prevLang = Factory.LANGUAGE;
      Factory.setLanguage(language);
      String name = eq.getName();
      Factory.setLanguage(prevLang);
      return name;
   }

   public List<String> getEquipment()
   {
      List<String> allEquipment = new ArrayList<>();
      allEquipment.addAll(equipment);
      allEquipment.addAll(specialEquipment);
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
   public int compareTo(final Event e)
   {
      return name.compareTo(e.name);
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof Event)
         return name.equals(((Event) other).name);
      return false;
   }

   private static void populate()
   {
      add("All About the Numbers", Type.STD, 0, "Jump to the closest level <em class=diff1>.1.</em> or <em class=diff1>.2.</em> cache you have not yet found.  End your turn unless", "Numbers", null, "Jeep", "Mountain Bike");
      add("Angry Bees", Type.STD, 0, "You accidentally disturb a hive of bees; end your turn. The player on your right moves your token 2 tiles in any direction.", null, "Move Other 2");
      add("Archive Cache", Type.NOW, -2, "Remove any unoccupied cache from the board.", null, "Point -2", "Repair Kit");
      add("Bad Coordinates", Type.STD, 0, "End your turn.", null, null, "Cell Phone");
      add("Bad Weather", Type.STD, 0, "End your turn.", null, null, "Emergency Radio", "Rain Jacket");
      add("Barbed Wire", Type.STD, 0, "-2 to your roll.", null, "Roll -2", "Utility Tool");
      add("Bear Encounter", Type.STD, 0, "End your turn. The player on your right moves your token 1 tile in any direction.", null, "Move Other 1", "Whistle", "Ol' Blue");
      add("Blisters", Type.STD, 0, "You may move at most 2 tiles (per turn) until you skip a turn.", null, "Move 2 Cap", "Hiking Boots", "First-aid Kit");
      add("Bomb Squad", Type.STD, 0, "Remove the cache closest to your token from the board.  Ignore caches with player tokens in the same tile.", null, null);
      add("Bragging Rights", Type.STD, 0, "Gain one point for every <em class=ftf>.F.</em> coin you have.  Lose 2 points if you have none, unless", null, "Points +1", "Geocoin");
      add("Broken Wrist", Type.STD, 0, "Jump your token to the Hospital, and end your turn.  Lose 4 points if you play this card on another player.", null, "Move Hospital", "[First-aid Kit]");
      add("Bug Hog", Type.STD, 0, "Lose one point for each <em class=bug>Travel Bug</em> you have.", null, "Points -1", "Swag Bag");
      add("Bug Swap", Type.STD, 0, "Trade <em class=bug>Travel Bugs</em> with any other player.  You both gain 1 point.  If you have none, you may draw one first.", null, "Point +1");
      add("Bushwhacked", Type.STD, 0, "If you are not on a path or Urban tile, lose 1 point and get -2 on your roll.", null, "Point -1", "Trail Guide", "Ol' Blue");
      add("Call from the Boss", Type.STD, 0, "End your turn and skip the next one.  Then draw one Equipment card.", null, "Equip 1", "Laptop");
      add("Campsite Confusion", Type.NOW, 0, "All players give one Equipment card to the player on their left.", null, null);
      add("Careless Hide", Type.NOW, -2, "From now on, all Search rolls for the last cache you found get +1.", null, "Point -2", "Mirror");
      add("Caught in the Rain", Type.STD, 0, "Discard a random Equipment card.", null, "Equip -1", "Rain Jacket", "Emergency Radio");
      add("Complete Collection", Type.STD, 0, "You may discard 3 points to select any Equipment card from the deck.", "Ensemble", null);
      add("Creek Crossing", Type.STD, 0, "Oops! You slip crossing a small creek. End your turn.", null, null, "Walking Stick", "Hiking Staff");
      add("Dead Batteries", Type.STD, 0, "End your turn.  You may discard an Equipment card instead.", null, null, "Batteries");
      add("Dollar Store", Type.STD, 0, "If you're within 3 tiles of an Urban tile, end your turn.", null, null, "Swag Bag");
      add("Drought", Type.NOW, 0, "For the next 3 rounds, streams only cost 1 movement point to cross.", null, "Move Stream 1");
      add("Earth Cache", Type.STD, 3, "You learn some interesting facts while logging an Earth Cache.", null, "Point +3");
      add("Equipment Rental", Type.STD, 0, "Select any Equipment card from the deck.  You may keep this equipment until you roll a <em class=dnf>.D.</em>.", null, "Equip 1");
      add("Eureka!", Type.ANY, 0, "You may discard this card to solve any Puzzle Cache, or guarantee a successful Search roll.", null, "Search");
      add("Feed The Trolls", Type.STD, 0, "You stop to read the forums; end your turn.", null, null, "Laptop");
      add("Fire Ants", Type.STD, 0, "End your turn.  The player on your right moves your token 1 tile in any direction.", null, "Move Other 1", "Gorp", "Long Pants");
      add("Flash Flood", Type.STD, 0, "Remove the closest cache that is next to a stream from the board.  End your turn.", null, null, "Waders");
      add("Fox Hunt", Type.STD, 1, "You spot a red fox.  If you have <em class=equipment>Ol' Blue</em>, end your turn.", null, "Point +1");
      add("Fresh Snow", Type.NOW, 0, "-1 to all rolls this round.", "Snow", "Roll -1");
      add("Ground Zero", Type.ANY, 0, "You may jump your token to the closest cache.", null, "Move Cache");
      add("Happy Birthday!", Type.STD, 0, "Draw 1 Equipment card.", null, "Equip 1");
      add("Heat Wave", Type.STD, 0, "-2 to your roll.", null, "Roll -2", "Bandana", "Camel Pack", "Hat", "Water Bottle");
      add("Heavy Rain", Type.NOW, 0, "For the next 3 rounds, streams cost 4 movement points to cross.", null, "Move Stream 4");
      add("Helpful Hint", Type.ANY, 0, "You decrypt the clue.  Discard this card to get +2 on your roll.", null, "Roll +2");
      add("Hidden Path", Type.ANY, 0, "You find a hidden path.  Double your Move roll this turn.", null, "Move x2");
      add("In a Hurry", Type.STD, 0, "You may move 2 extra tiles this turn (even after searching).  Lose 1 point unless", null, "Move 2", "Letterbox Stamp");
      add("Is That Venomous?", Type.STD, 0, "End your turn.  Roll the dice.  If you roll a <em class=dnf>.D.</em>, jump your token to the Hospital.", "Black Widow", "Move Hospital", "Field Guide", "Gloves");
      add("Landslide", Type.STD, 0, "End your turn.  Discard a random Equipment card unless", null, "Equip -1", "Survival Strap");
      add("Leave No Trace", Type.STD, 2, "You carefully avoided the fragile flora.", null, "Point +2");
      add("Letterbox", Type.STD, 0, "You find a Letterbox Hybrid cache.  Gain 1 point (3 if you have the <em class=equipment>Letterbox Stamp</em>).", null, "Point +1", "[Letterbox Stamp]");
      add("Litterbug", Type.STD, -3, "You left some garbage in the woods.", null, "Point -4", "CITO Bag");
      add("Lost and Found", Type.STD, 2, "Draw 1 Equipment card and give it to any other player.", null, "Point +2");
      add("Lost Travel Bug", Type.STD, 0, "If you have a <em class=bug>Travel Bug</em>, discard it and lose 2 points.", null, "Point -3", "Belt Pack");
      add("Lost!", Type.STD, 0, "The player on your right moves your token in any direction the number tiles equal to your roll.", "Lost", "Move Other Roll", "Compass", "Map", "Whistle");
      add("May I Borrow That?", Type.STD, -1, "Take any Equipment card from any player that has more Equipment cards than you.", "Borrow", "Point -1");
      add("Meet and Greet", Type.NOW, 0, "Select a random location; any player may jump to that tile.  All players who do receive 2 points.  Shuffle this card back into the deck.", null, "Point +2");
      add("Meet the Muggles", Type.STD, 2, "You stop to explain geocaching.  End your turn unless", null, "Point +2", "CITO Bag", "Safari Vest");
      add("Mega Event", Type.NOW, 0, "Select a random location; all players jump to that tile.  Each player rolls the dice and gains that many points.", null, "Point +Roll");
      add("Mosquitoes", Type.STD, 0, "-1 to your roll; -2 if you are in a Forest tile; end your turn if you are in a Swamp tile.", null, "Roll -1", "Bandana", "Insect Repellent");
      add("Muggled!", Type.STD, 0, "The closest level 1 or 2 cache has been emptied.  Remove any <em class=bug>Travel Bugs</em> it has.  It may still be found, but finders don't get to draw Equipment cards.", null, null);
      add("Night Caching", Type.STD, 0, "All players not on an Urban tile skip their next turn.", null, null, "Flashlight", "Head Lamp");
      add("Not About the Numbers", Type.STD, 0, "Gain one point for each level 5 cache, Multi-cache, or Puzzle cache you have found.  If none, lose 2 points unless", null, null, "Binoculars", "Rope");
      add("Out of Ink", Type.STD, 0, "End your turn while you improvise a way to sign the logbook.", null, null, "Letterbox Stamp", "Pocket Knife");
      add("Park Closed", Type.STD, 0, "End your turn.  Does not affect <em class=cacher>Ranger Rachel</em>.", null, null);
      add("Parking Ticket", Type.STD, -1, "Discard an Equipment card.", null, "Equip -1", "Lucky Charm");
      add("Pawn Shop", Type.STD, 0, "Draw five Equipment cards.  You may trade any of your Equipment cards for any of those five.", null, null);
      add("Poison Ivy", Type.STD, 0, "Jump your token to your starting tile, and end your turn.", null, "Move Start", "Gloves", "Field Guide");
      add("Private Property", Type.STD, -2, "Explain what you're up to; end your turn.", null, "Point -2", "CITO Bag", "Trail Guide");
      add("Quench Your Thirst", Type.STD, 0, "End your turn.", null, null, "Camel Pack", "Water Bottle");
      add("Rare Bird", Type.STD, 0, "You spot an ivory-billed woodpecker.  Gain 2 points (4 if you have the <em class=equipment>Camera</em>).", null, "Point +2", "[Camera]");
      add("Recycle", Type.STD, 0, "Discard this card to draw any Event card from the discard pile.", null, null);
      add("Repair a Cache", Type.STD, 3, "You stop to repair a cache.  End your turn unless", null, "Point +3", "Duct Tape", "Repair Kit", "Utility Tool");
      add("Save a Turtle", Type.STD, 3, "You stop to help a turtle across the road.  End your turn unless", null, "Point +3", "Field Guide");
      add("Scenic View", Type.STD, 0, "End your turn.  Gain 2 points (3 if you have the <em class=equipment>Binoculars</em> or <em class=equipment>Camera</em>).", null, "Point +2", "[Binoculars]", "[Camera]");
      add("Shortcut", Type.ANY, 0, "Discard to move up to 5 more tiles this turn (even after searching).", null, "Move 5");
      add("Signal Bounce", Type.STD, 0, "The player on your right moves your token 1 tile in any direction.", null, "Move Other 1", "Compass", "Antenna");
      add("Soggy Log", Type.STD, -1, "You let the last logbook you signed get wet.", null, "Point -1", "Rain Jacket");
      add("Solar Flares", Type.NOW, 0, "-2 on all Search rolls this round.", null, "Search -2", "Antenna");
      add("Souvenir Day", Type.STD, 0, "Anyone finding a cache this round gains 1 extra point.", null, "Point +1");
      add("Spider Webs", Type.STD, 0, "-1 to your roll.", null, "Roll -1", "Walking Stick");
      add("Steep Slope", Type.STD, 0, "If moving, end your turn.  If searching, -1 to your roll.", null, "Search -1", "Hiking Staff", "Rope");
      add("Stick Race", Type.STD, 1, "Jump your token to the closest bridge, and end your turn.", null, "Move Bridge");
      add("Stop For Directions", Type.STD, 0, "End your turn, but get +3 to your next Move.", null, "Move 3", "Cell Phone", "Map");
      add("Suspicious Activity", Type.STD, 0, "End your turn.  Roll the dice.  If you roll a <em class=dnf>.D.</em>, jump to the Police tile.", null, "Move Police", "Backpack", "CITO Bag");
      add("There's the Path!", Type.STD, 0, "You stumble upon the path you should have taken. You get -1 on this roll, but +2 on your next roll.", "Path", "Roll -1", "Trail Guide");
      add("Thorns", Type.STD, 0, "-2 to your roll.", null, "Roll -2", "Gloves", "Long Pants");
      add("Ticks", Type.STD, 0, "End your next turn.", null, null, "Gaiters", "Insect Repellent", "Long Pants");
      add("Trade Up", Type.STD, 2, "You helped restock an empty cache.  Discard an Equipment card unless", null, "Point +2", "Swag Bag");
      add("Trash Out", Type.STD, 3, "You stop to pick up some trash.  End your turn unless", null, "Point +3", "CITO Bag");
      add("Twist and Shout", Type.NOW, 0, "All event cards (in play or held) are moved to the next player (clockwise).", null, null);
      add("Twisted Ankle", Type.NOW, 0, "End your turn.  If you are not on a path, skip your next turn too.", null, null, "FRS Radio", "[First-aid Kit]");
      add("Upgrade", Type.STD, 1, "Draw an Equipment card, then give any one of your Equipment cards to another player of your choice.", null, "Point +1");
      add("Waterfall", Type.NOW, 0, "The next player to end their turn at a waterfall gets 3 points (4 if they have the <em class=equipment>Camera</em>).", null, "Point +3", "[Camera]");
      add("Waypoint My Car?", Type.STD, 0, "The player on your right moves your token 2 tiles in any direction.", "Waypoint", "Move Other 2", "Map", "Trail Guide");
      add("Well-stocked Cache", Type.ANY, 0, "You may discard this card when you find a cache, to choose from 3 extra Equipment cards.", null, null);
      add("Where's George?", Type.STD, -1, "You ran out of trinkets to trade.", "Wheres George", "Point -1", "Swag Bag");
      add("Winter is Coming", Type.STD, 0, "Sometimes it's just too cold for caching.  Jump your token back to your starting location, and end your turn.", null, "Move Start", "Emergency Radio");
      add("Yellow Jackets", Type.STD, 0, "+2 if moving, or -2 if searching.", null, "Search -2");

      // New Species (moth) - 3 points
      // Detour
      // Timber!
      // Lightning Strike
      // Mega-event
      // Webcam Cache
      // Virtual Cache
      // etc

      // add ("13-Year Cicadas", Type.NOW, 0, "All players get -1 on all Search rolls in Forest tiles for the next 2 rounds.", null, null);
      // add ("Ancient Ruins", Type.STD, 1, "You discover an artifact. Draw 1 Equipment card.", null, null);
      // add ("Cache Maintenance", Type.STD, 0, "The cache closest to you needs maintenance. The player on your right moves the cache 1 tile in any direction.", null, null);
      // add ("Cache Pirates", Type.NOW, 0, "Remove the cache farthest from your token from the board.", null, null);
      // add ("Early Frost", Type.STD, 0, "End your turn.", null, null, "Gloves");
      // add ("Hide a Cache", EventType.STD, 2, "Add a new cache onto the board (in a random location). Shuffle this card back into the deck.", null, null);
      // add ("It's a Dry Heat", EventType.STD, 0, "-2 to your roll.", null, null, "Camel Pack", "Canteen");
      // add ("Missed Anniversary", Type.STD, -1, "End your turn. Discard any Equipment card.", null, "Equip -1");
      // add ("Sneaky Search", Type.ANY, -1, "Keep this card. When you find a cache, you may discard it. Other cachers on that tile do not get credit for the find.", null, null);
      // add ("Weather Forecast", EventType.ANY, 0, "Keep this card. You may discard it to avoid the effects of any weather event played on you.", null, null);
      // add ("You're Fired!", Type.STD, 0, "Discard any Equipment card. Take an extra turn after this one.", null, "Equip -1");
   }

   static void populateFR()
   {
      addFR("All About the Numbers", "Tout Sur les Chiffres", "Passez au niveau le plus proche <em class=diff1>.1.</em> ou <em class=diff1>.2.</em> cache que vous n'avez pas encore trouvé. Terminez votre tour à moins");
      addFR("Angry Bees", "Abeilles en Colère", "Vous dérangez accidentellement une ruche d'abeilles; Terminez votre tour Le joueur sur votre droite déplace votre jeton de 2 tuiles dans n'importe quelle direction.");
      addFR("Archive Cache", "Archiver un Cache", "Supprimer tout cache inoccupé de la carte.");
      addFR("Bad Coordinates", "Mauvaises Coordonnées", "Terminez votre tour.");
      addFR("Bad Weather", "Mauvais Temps", "Terminez votre tour.");
      addFR("Barbed Wire", "Fil Barbelé", "-2 à votre rouleau.");
      addFR("Bear Encounter", "Rencontre D'ours", "Terminez votre tour. Le joueur sur votre droite déplace votre jeton 1 tuile dans n'importe quelle direction.");
      addFR("Blisters", "Cloque", "Vous pouvez déplacer au maximum 2 tuiles (par tour) jusqu'à ce que vous sautez un tour.");
      addFR("Bomb Squad", "Équipe D'explosifs", "Retirez le cache le plus proche de votre jeton de la carte. Ignorer les caches avec des jetons de joueur dans la même tuile.");
      addFR("Bragging Rights", "Droits de Bravoure", "Gagnez un point pour chaque pièce <em class=ftf>.F.</em> que vous avez. Perdez 2 points si vous n'en avez pas, à moins que");
      addFR("Broken Wrist", "Poignet Cassé", "Sautez votre jeton à l'hôpital, et terminez votre tour. Perdez 4 points si vous jouez cette carte sur un autre joueur.");
      addFR("Bug Hog", "Punaise Porc", "Perdez un point pour chaque <em class=bug>Travel Bug</em> que vous avez.");
      addFR("Bug Swap", "Punaise Échanger", "Commerce <em class=bug>Travel Bugs</em> avec n'importe quel autre joueur. Vous gagnez tous deux 1 point. Si vous n'en avez pas, vous pouvez en faire une première.");
      addFR("Bushwhacked", "Broussaille", "Si vous n'êtes pas sur un chemin ou tuile Urban, perdre 1 point et obtenir -2 sur votre rouleau.");
      addFR("Call from the Boss", "Appel du Patron", "Terminez votre tour et sautez le suivant. Dessinez ensuite une carte d'équipement.");
      addFR("Campsite Confusion", "Confusion de Camp", "Tous les joueurs donnent une carte d'équipement au joueur sur leur gauche.");
      addFR("Careless Hide", "Masquer Négligent", "A partir de maintenant, tous les rouleaux de recherche pour le dernier cache que vous avez trouvé obtiennent +1.");
      addFR("Caught in the Rain", "Pris dans la Pluie", "Jeter une carte d'équipement aléatoire.");
      addFR("Complete Collection", "Collection Complète", "Vous pouvez défausser 3 points pour sélectionner n'importe quelle carte d'équipement du pont.");
      addFR("Creek Crossing", "Passage du Ruisseau", "Oops! Vous glissez en traversant un petit ruisseau. Terminez votre tour.");
      addFR("Dead Batteries", "Batteries Mortes", "Terminez votre tour. Vous pouvez jeter une carte d'équipement à la place.");
      addFR("Dollar Store", "Boutique Dollar", "Si vous êtes dans les 3 carreaux d'une tuile Urban, terminez votre tour.");
      addFR("Drought", "Sécheresse", "Pour les 3 tours suivants, les flux ne coûtent que 1 point de mouvement à franchir.");
      addFR("Earth Cache", "Cache de la Terre", "Vous apprendrez des faits intéressants lors de l'enregistrement d'un Cache de la Terre.");
      addFR("Equipment Rental", "Location d'Équipement", "Sélectionnez n'importe quelle carte d'équipement de la plate-forme. Vous pouvez conserver cet équipement jusqu'à ce que vous lancez <em class=dnf>.D.</em>.");
      addFR("Eureka!", "Eureka!", "Vous pouvez jeter cette carte pour résoudre n'importe quel cache de casse-tête, ou garantir un rouleau de recherche réussi.");
      addFR("Feed The Trolls", "Nourrir les Trolls", "Vous arrêtez de lire les forums; Terminez votre tour.");
      addFR("Fire Ants", "Fourmis Rouges", "Terminez votre tour. Le joueur sur votre droite déplace votre jeton 1 tuile dans n'importe quelle direction.");
      addFR("Flash Flood", "Crue Subite", "Retirez le cache le plus proche qui se trouve à côté d'un flux de la carte. Terminez votre tour.");
      addFR("Fox Hunt", "Chasse au Renard", "Tu vois un renard roux. Si vous avez <em class=equipment>Chien Pisteur</em>, terminez votre tour.");
      addFR("Fresh Snow", "Neige Fraîche", "-1 à tous les rouleaux de ce tour.");
      addFR("Ground Zero", "?Ground Zero?", "Vous pouvez sauter votre jeton vers le cache le plus proche.");
      addFR("Happy Birthday!", "Bon Anniversaire!", "Dessinez 1 carte d'équipement.");
      addFR("Heat Wave", "Canicule", "-2 à votre rouleau.");
      addFR("Heavy Rain", "Faible Pluie", "Heavy RainFor les 3 tours suivants, les ruisseaux coûtent 4 points de mouvement à traverser.");
      addFR("Helpful Hint", "Conseil Utile", "Vous décrypter l'indice. Jeter cette carte pour obtenir +2 sur votre rouleau.");
      addFR("Hidden Path", "Chemin Caché", "Vous trouvez un chemin caché. Doublez votre rouleau de mouvement ce tour.");
      addFR("In a Hurry", "Pressé", "Vous pouvez déplacer 2 tuiles supplémentaires ce tour-ci (même après la recherche). Perdre 1 point à moins");
      addFR("Is That Venomous?", "Est-ce Venimeux?", "Terminez votre tour. Lancer les dés. Si vous lancez une <em class=dnf>.D.</em>, sautez votre jeton vers l'Hôpital.");
      addFR("Landslide", "Glissement de Terrain", "Terminez votre tour. Jeter une carte d'équipement aléatoire sauf");
      addFR("Leave No Trace", "Ne laisse aucune Trace", "Vous avez soigneusement évité la flore fragile.");
      addFR("Letterbox", "Boîte aux Lettres", "Vous trouvez un cache hybride Letterbox. Gain 1 point (3 si vous avez <em class=equipment>Étampe</em>).");
      addFR("Litterbug", "Litterbug", "Vous avez laissé des ordures dans les bois.");
      addFR("Lost and Found", "Perdu et Trouvé", "Dessinez une carte d'équipement et donnez-la à tout autre joueur.");
      addFR("Lost Travel Bug", "Erreur Travel Bug", "Si vous avez un <em class=bug>Travel Bug</em>, le jeter et perdre 2 points.");
      addFR("Lost!", "Perdu!", "Le joueur sur votre droite déplace votre jeton dans n'importe quelle direction le nombre tuiles égal à votre rouleau.");
      addFR("May I Borrow That?", "Puis-je Emprunter Cela?", "Prenez n'importe quelle carte d'équipement de n'importe quel joueur qui a plus de cartes d'équipement que vous.");
      addFR("Meet and Greet", "Rencontrez-nous", "Sélectionner un emplacement aléatoire; N'importe quel joueur peut sauter à cette tuile. Tous les joueurs qui reçoivent 2 points. Mélangez cette carte de nouveau dans le pont.");
      addFR("Meet the Muggles", "Rencontrez les Moldus", "Vous arrêtez d'expliquer geocaching. Terminez votre tour à moins");
      addFR("Mega Event", "Mega Événement", "Sélectionner un emplacement aléatoire; Tous les joueurs sauter à cette tuile. Chaque joueur lance les dés et gagne autant de points.");
      addFR("Mosquitoes", "Moustiques", "-1 à votre rouleau; -2 si vous êtes dans une tuile Forest; Terminer votre tour si vous êtes dans une tuile Swamp.");
      addFR("Muggled!", "Muggled!", "Le cache de niveau 1 ou 2 le plus proche a été vidé. Supprimez tout <em class=bug>Travel Bugs</em> qu'il a. Il peut toujours être trouvé, mais les chercheurs ne parviennent pas à dessiner des cartes d'équipement.");
      addFR("Night Caching", "Mise en cache de nuit", "Tous les joueurs qui ne sont pas sur une tuile Urban échappent à leur tour suivant.");
      addFR("Not About the Numbers", "Pas sur les chiffres", "Gagnez un point pour chaque cache de niveau 5, Multi-cache ou cache de puzzle que vous avez trouvé. Si aucun, perd 2 points à moins que");
      addFR("Out of Ink", "Sans Encre", "Terminez votre tour pendant que vous improvisez un moyen de signer le journal de bord.");
      addFR("Park Closed", "Parc Fermé", "End your turn.  Does not affect <em class=cacher>François</em>.");
      addFR("Parking Ticket", "Ticket de Parking", "Jeter une carte d'équipement.");
      addFR("Pawn Shop", "Prêteur sur Gage", "Dessinez cinq cartes d'équipement. Vous pouvez échanger vos cartes Équipement pour l'une de ces cinq.");
      addFR("Poison Ivy", "Sumac Vénéneux", "Sautez votre jeton à votre tuile de départ et terminez votre tour.");
      addFR("Private Property", "Propriété Privée", "Expliquez ce que vous faites; Finis ton tour.");
      addFR("Quench Your Thirst", "Étancher Votre Soif", "Terminez votre tour.");
      addFR("Rare Bird", "Oiseau Rare", "Vous apercevez un pic à facon ivoire. Gagnez 2 points (4 si vous avez <em class=equipment>Appareil Photo?</em>).");
      addFR("Recycle", "Recycler", "Jeter cette carte pour dessiner toute carte Event de la pile de défausse.");
      addFR("Repair a Cache", "Réparer un cache", "Vous arrêtez de réparer un cache. Terminez votre tour à moins");
      addFR("Save a Turtle", "Enregistrer une Tortue", "Vous vous arrêtez pour aider une tortue à traverser la route. Terminez votre tour à moins");
      addFR("Scenic View", "Vue Panoramique", "Terminez votre tour. Gagnez 2 points (3 si vous avez <em class=equipment>Jumelles?</em> ou <em class=equipment>Appareil photo?</em>).");
      addFR("Shortcut", "Raccourci", "Jeter pour déplacer jusqu'à 5 tuiles supplémentaires ce tour (même après la recherche).");
      addFR("Signal Bounce", "Signal Bounce", "Le joueur sur votre droite déplace votre jeton 1 tuile dans n'importe quelle direction.");
      addFR("Soggy Log", "Soggy Log", "Vous laissez le dernier journal de bord que vous avez signé mouiller.");
      addFR("Solar Flares", "Éclaboussures Solaires", "-2 sur tous les rôles de recherche cette ronde.");
      addFR("Souvenir Day", "Souvenir Day", "Quiconque trouve un cache ce tour gagne 1 point supplémentaire.");
      addFR("Spider Webs", "Toiles d'Araignée", "-1 à votre rouleau.");
      addFR("Steep Slope", "Pente Raide", "If moving, end your turn.  If searching, -1 to your roll.");
      addFR("Stick Race", "Suiv", "Sautez votre jeton au pont le plus proche et terminez votre tour.");
      addFR("Stop For Directions", "Arrêter pour des Directions", "Terminez votre tour, mais obtenez +3 à votre prochain Déplacer.");
      addFR("Suspicious Activity", "Activité Suspecte", "Terminez votre tour. Lancer les dés. Si vous lancez une <em class=dnf>.D.</em>, allez au carreau Police.");
      addFR("There's the Path!", "Il y a le Chemin!", "Vous trébuchez sur le chemin que vous auriez dû prendre. Vous obtenez -1 sur ce rouleau, mais +2 sur votre prochain rouleau.");
      addFR("Thorns", "Les Épines", "-2 à votre rouleau.");
      addFR("Ticks", "Ticks?", "Terminez votre prochain tour.");
      addFR("Trade Up", "Commerce?", "Vous avez aidé à reconstituer un cache vide. Jeter une carte d'équipement à moins");
      addFR("Trash Out", "Poubelle Sortie", "Vous arrêtez de ramasser des ordures. Terminez votre tour à moins");
      addFR("Twist and Shout", "Torsion et Crier", "Toutes les cartes d'événement (en jeu ou en attente) sont déplacées vers le prochain joueur (dans le sens des aiguilles d'une montre).");
      addFR("Twisted Ankle", "Cheville Foulée", "Terminez votre tour. Si vous n'êtes pas sur un chemin, sautez votre prochain tour aussi.");
      addFR("Upgrade", "Surclassement", "Dessinez une carte d'équipement, puis donnez l'une de vos cartes d'équipement à un autre joueur de votre choix.");
      addFR("Waterfall", "Cascade", "Le joueur suivant à terminer son tour à une chute d'eau obtient 3 points (4 s'ils ont le <em class=equipment>Camera</em>).");
      addFR("Waypoint My Car?", "Waypoint My Car?", "Le joueur sur votre droite déplace votre jeton de 2 tuiles dans n'importe quelle direction.");
      addFR("Well-stocked Cache", "Cache bien Approvisionné", "Vous pouvez jeter cette carte lorsque vous trouvez un cache, pour choisir parmi 3 cartes d'équipement supplémentaires.");
      addFR("Where's George?", "Oů est George?", "Vous avez manqué de bibelots pour le commerce.");
      addFR("Winter is Coming", "L'hiver Arrive", "Parfois, c'est juste trop froid pour la mise en cache. Retournez votre jeton à votre emplacement de départ et terminez votre tour.");
      addFR("Yellow Jackets", "Guêpes", "+2 en déplacement ou -2 en recherche.");
   }

   private static void add(final String name, final Type type, final int points, final String effect, final String image,
                           final String icon, final String... protection)
   {
      Event event = new Event(name, type, points, effect, image, icon, protection);
      EVENTS.put(event.getName(), event);
   }

   private static void showEvents()
   {
      for (Event event : EVENTS.values())
      {
         if (event.name == CardUtils.BLANK)
            continue;
         System.out.print(StringUtils.pad(event.toString(), 30));
         for (String eq : event.equipment)
            System.out.print(eq + ", ");
         for (String eq : event.specialEquipment)
            System.out.print(eq + ", ");
         System.out.println();
      }
      System.out.println();
   }

   private static void validate()
   {
      System.out.println("Validating Equipment References:");
      System.out.flush();
      for (Event event : EVENTS.values())
         for (String eq : event.getEquipment())
            if (Equipment.EQUIPMENT.get(eq) == null)
               System.err.println("Missing equipment for " + event + ": " + eq);
      System.err.flush();
      System.out.println();
      System.out.flush();

      System.out.println("Negative Events (without mitigating equipment):");
      Pattern pattern = Pattern.compile("\\b[Ll]ose\\b|\\b[Ee]nd\\b|-[1-9]");
      for (Event event : EVENTS.values())
         if (pattern.matcher(event.text).find() && // bad event
             !event.text.toString().contains("gain") && // no points gained
             event.equipment.isEmpty() && event.specialEquipment.isEmpty()) // no counter
            System.out.println("  " + event.name);
      System.out.println();
      System.out.flush();
   }

   public void publishTGC() // in TheGameCrafter format
   {
      ImageGenerator imgGen = Factory.getImageGenerator();
      
      OutputStream os = null;
      try
      {
         String name = getName();
         System.out.println(" > " + name + ": " + getText());
         
         BufferedImage cardImage = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();
         
         File face = new File(Factory.ROOT + "Cards/Events/Event Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }

         imgGen.paintGrid(g);
         int playHeight = paintPlayRule(g);
         int titleHeight = imgGen.paintTitleLeft(g, this);
         int titleBottom =  stats.safeMarginH + 2 + titleHeight;
         int top = titleBottom + (getType() == Type.STD ? 75 : 40);
         if (getImage() != null)
            imgGen.paintArt(g, getImage(), top);
         if (getIcon() != null)
            imgGen.paintIcon(g, getIcon(), 110);
         top = (stats.h / 2) + playHeight + 1;
         int bottom = stats.h - stats.safeMarginH;
         imgGen.paintText(g, this, top, bottom, 5);
         hackIcons(g);

         // safe box
         g.setColor(Color.BLUE);
         g.setStroke(ImageGenerator.DASHED);
         // g.drawRect(stats.safeMarginW, stats.safeMarginH, stats.safeW, stats.safeH);
         
         g.dispose();
         cardImage.flush();
         
         String path = Factory.getRoot() + "Cards/Events/";
         File file = new File(path + name.replaceAll("[?!]", "") + ".png");
         os = new FileOutputStream(file);
         ImageTools.saveAs(cardImage, "png", os, 0f);

         // if (name.contains("Archive")) gui.ComponentTools.open(new javax.swing.JLabel(new ImageIcon(cardImage)), name); // UI trace
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      finally
      {
         imgGen.close(os);
      }
   }

   private int paintPlayRule(final Graphics2D g)
   {
      int textHeight = 0;
      
      if (getType() != Type.STD)
      {
         String text;
         if (Factory.LANGUAGE == Language.FRENCH)
            text = getType() == Type.NOW ? "JOUE MAINTENANT" : "JOUER À TOUT MOMENT";
         else
            text = getType() == Type.NOW ? "PLAY NOW" : "PLAY ANY TIME";
         g.setFont(stats.playFont);
         FontMetrics fm = g.getFontMetrics(stats.playFont);
         textHeight = fm.getHeight();
         int textWidth = fm.stringWidth(text);
         // background
         g.setColor(getType() == Type.NOW ? stats.playNowColor : stats.playAnyColor);
         int left = (stats.w - textWidth) / 2;
         int top = (stats.h / 2) + 50;
         RoundRectangle2D bg = new RoundRectangle2D.Float(left - 10, top, textWidth + 20, textHeight, 20, 20);
         g.fill(bg);
         g.setColor(Color.BLACK);
         Stroke origStroke = g.getStroke();
         g.setStroke(ImageGenerator.STROKE3);
         g.draw(bg);
         g.setStroke(origStroke);
         // text
         int bottom = top + textHeight - 10; // 15;
         g.drawString(text, left, bottom); // lower-left
      }
      
      return textHeight;
   }

   private void hackIcons(final Graphics2D g)
   {
      ImageGenerator imgGen = Factory.getImageGenerator();
      String name = getNameEnglish();
      
      if (name.equals("All About the Numbers"))
      {
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Move Run.png", 120, 120, stats.safeW - 50, stats.safeMarginH + 45);
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Cache 1.png", 70, 70, stats.safeMarginW + 20, stats.centerY + 163);
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Cache 2.png", 70, 70, stats.safeMarginW + 160, stats.centerY + 163);
      }
      else if (name.equals("Bragging Rights"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Point FTF.png", 90, 90, 84, 712);
      else if (name.equals("Bushwhacked"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll -2.png", 145, 145, 605, 225);
      else if (name.equals("Equipment Rental"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll DNF.png", 70, 70, 555, 878);
      else if (name.equals("In a Hurry"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Point -1.png", 150, 150, 585, 235);
      else if (name.equals("Is That Venomous?"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll DNF.png", 65, 65, 383, 726);
      else if (name.equals("Meet and Greet"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Move Join.png", 150, 150, 595, 225);
      else if (name.equals("Not About the Numbers"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Points +1.png", 150, 150, stats.safeW - 60, stats.safeMarginH + 65);
      else if (name.equals("Parking Ticket"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Point -1.png", 145, 145, 610, 220);
      else if (name.equals("Stick Race"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Point +1.png", 150, 150, 445, 85);
      else if (name.equals("Suspicious Activity"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll DNF.png", 65, 65, 514, 720);
      else if (name.equals("Trade Up"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Equip -1.png", 150, 150, 475, 85);
      else if (name.equals("Yellow Jackets"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Move 2.png", 150, 150, 515, 80);
   }
   
   private static void publishTTS() // for Tabletop Simulator
   {
      int columns = 7;
      int rows = 7;
      
      List<String> lines = new ArrayList<>();
      List<String> lines2 = new ArrayList<>();
      lines.add("# Save File");
      lines.add("cardsx=" + columns);
      lines.add("cardsy=" + rows);
      lines.add("card-width=825");
      lines.add("card-height=1125");
      lines.add("zoom=0.15");
      lines.add("background-color=-16777216");
      lines2.addAll(lines);

      String path = Factory.TGC.replace(":", "\\:").replace("/", "\\\\");
      int col = 0, row = 0;
      
      for (Event event : EVENTS.values())
      {
         if ((col + 1) * (row + 1) <= (columns * rows))
            lines.add(col + "_" + row + "=" + path + "Cards\\\\Events\\\\" + event.getName() + ".png");
         else
            lines2.add(col + "_" + (row - rows) + "=" + path + "Cards\\\\Events\\\\" + event.getName() + ".png");
         
         col++;
         if (col == columns)
         {
            col = 0;
            row++;
         }
      }
      
      // TODO: add "col_row=null" up to 49
      
      path = Factory.TTS + "Cards/Events.tsdb";
      FileUtils.writeList(lines, path, false);
      System.out.println("Tabletop Simulator: " + path);
      
      path = Factory.TTS + "Cards/Events2.tsdb";
      FileUtils.writeList(lines2, path, false);
      System.out.println("Tabletop Simulator: " + path);
      
      System.out.println();
   }
   
   public static void publish()
   {
      Factory.setImageStats(Event.getImageStats());
      
      HtmlGenerator htmlGen = new HtmlGenerator(9, 3);
      htmlGen.printEvents(EVENTS);

      // for (Event event : EVENTS.values())
      //    event.publishTGC();
      System.out.println();
      
      publishTTS();

      /*
      Factory.setLanguage(Language.FRENCH);
      for (Event event : EVENTS.values())
         event.publishTGC();
      System.out.println();
      */
   }

   public static void main(final String[] args)
   {
      // Event.showEvents();
      Event.validate();
      Event.publish();
   }
}
