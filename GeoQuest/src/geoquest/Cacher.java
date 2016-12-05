package geoquest;

import java.util.Map;
import java.util.TreeMap;

import str.StringUtils;

public class Cacher extends Component implements Comparable<Cacher>
{
   static final Map<String, Cacher> CACHERS = new TreeMap<>();
   static
   {
      populate();
   }

   private String name;
   private String text;

   public Cacher(final String name, final String text)
   {
      this.name = name;
      this.text = text.length() > 0 ? text : CardUtils.BLANK;
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

   public int compareTo(final Cacher e)
   {
      return name.compareTo(e.name);
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof Cacher)
         return name.equals(((Cacher) other).name);
      return false;
   }

   @Override
   public int hashCode()
   {
      return name.hashCode();
   }

   private static void populate()
   {
      add("Athletic Alex", "Alex gets +1 to Move for each empty Equipment slot (not counting extra slots).");
      add("Birder Brandon", "Brandon gets +1 when searching for Multi-caches.");
      add("Collector Colin", "Colin starts the game with a random Travel Bug ®.");
      add("Determined Dan", "Once per turn, Dan may re-roll a <em class=dnf>.D.</em> when searching.");
      add("Eager Earl", "Whenever a new cache is placed on the board, Earl may immediately move 4 tiles.");
      add("Fast Freddie", "Freddie gets +1 to all Move rolls in Clear and Urban tiles.");
      add("Grampa Gary", "Gary can't move more than 5 per turn, but he gets +1 to all Search rolls.");
      add("Hunter Henry", "Henry gets +1 when searching in Forest tiles.  Other players can't play events on Henry if he's on a Forest tile.");
      add("Independent Isabel", "Other players may only play events on Isabel if they are in the same map quadrant.");
      add("Jolly Jamie", "Other players can't play events on Jamie unless she has more points than they do.");
      add("Kindly Kris", "Kris can discard Event cards to gain 1 point each.");
      add("Lucky Lisa", "Lisa gets +1 whenever she rolls doubles.");
      add("Marathon Mike", "Mike may treat any \"end your turn\" effect as -2 instead.");
      add("Nosey Norman", "Whenever another player rolls 5 or higher, Norman may immediately move 1 tile.");
      add("Observant Oscar", "Oscar gets +1 when searching for any cache that has already been found.");
      add("Puzzler Paul", "Paul gets +1 when searching for puzzle caches.");
      add("Quirky Quigly", "Quigly may hold up to 3 Event cards; and he starts the game with a random one.");
      add("Ranger Rachel", "Rachel gets +2 to all Move rolls in Forest tiles.");
      add("Scout Scotty", "Scotty may discard Equipment cards to gain 1 point each.");
      add("Trader Ted", "When Ted finds a cache, he may draw two Equipment cards (but only keep one).");
      add("Unique Ursula", "Whenever Ursula rolls a 1 on either die, she gets +1 on her roll.");
      add("Volunteer Veda", "Veda gains 1 extra point for Event cards that award her points. She must attend all <em class=event>Meet and Greet</em> events.");
      add("Wandering Warren", "If Warren rolls a <em class=find>.F.</em> while moving, he may take an extra turn.");
      add("Yuppie Yuri", "Yuri starts the game with an extra Equipment card.");
      // "Xander the Explorer"
      // "Zealous Zach"
   }

   private static void add(final String cardName, final String cardText)
   {
      Cacher cacher = new Cacher(cardName, cardText);
      CACHERS.put(cacher.getName(), cacher);
   }

   private static void show()
   {
      for (Cacher cacher : CACHERS.values())
         if (cacher.name != CardUtils.BLANK)
         {
            System.out.print(StringUtils.pad(cacher.toString(), 20));
            for (Event event : Event.EVENTS.values())
               if (event.getText().contains(cacher.name))
                  System.out.print(StringUtils.pad(event.getName(), 20) + ", ");
            System.out.println();
            System.out.println("  > " + cacher.text);
         }
      System.out.println();
   }

   public static void main(final String[] args)
   {
      show();
      
      HtmlGenerator htmlGen = new HtmlGenerator(12, 3);
      htmlGen.printCachers(CACHERS);

      ImageGenerator imgGen = new ImageGenerator(ImageStats.getCacherStats(), false);
      for (Cacher cacher : CACHERS.values())
         imgGen.publish(cacher);
      System.out.println();
   }
}
