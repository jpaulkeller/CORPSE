package geoquest;

import java.util.Map;
import java.util.TreeMap;

import str.StringUtils;

public class Cacher extends Card implements Comparable<Cacher>
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
      add("Athletic Amanda", "Amanda gets +1 on all Move rolls, as long as she has at least 2 empty Equipment slots.");
      add("Birder Brandon", "Brandon has an eye for details; he gets +1 when searching for Multi-caches.");
      add("Collector Colin", "Colin can carry any number of Travel Bugs; and he starts the game with a (random) Travel Bug.");
      add("Determined Dan", "Once per turn, Dan may re-roll a <em class=dnf>DNF</em> when searching.");
      add("Eager Earl", "When Earl gets equipment from a cache, he may draw an extra card to choose from.");
      add("Fast Freddie", "Freddie always moves first.  Whenever a new cache is placed on the board, Freddie immediately takes an extra turn.");
      add("Grampa Gary", "Gary takes his time; he gets -1 to all Move rolls, and +1 to all Search rolls.");
      add("Hunter Henry", "Henry gets +1 when searching in Forest tiles.  Other players can't play events on Henry if he's on a Forest tile.");
      add("Independent Isabel", "Other players may only play events on Isabel if they are in the same map quadrant.");
      add("Jolly Jamie", "Other players can't play events on Jamie unless she has the most points.");
      add("Kindly Kate", "Kate can discard Event cards to gain 1 point each.");
      add("Lucky Lisa", "Lisa gets +1 whenever she rolls doubles.");
      add("Marathon Mike", "Tiles next to a path count as a path for Mike.");
      add("Nosey Norman", "Whenever another player rolls <em class=roll>5</em> or higher, Norman may immediately move 1 tile.");
      add("Observant Oscar", "If another player is within 2 tiles of him, Oscar gets +2 on his roll.");
      add("Puzzler Paul", "Paul gets +1 when searching for puzzle caches.");
      add("Quirky Quigly", "Quigly may hold up to 3 Event cards; and he starts the game with a (random) Event card.");
      add("Ranger Rachel", "Rachel gets +2 to all Move rolls in Forest tiles.");
      add("Scout Scotty", "Scotty may discard Equipment cards to gain 1 point each.");
      add("Tireless Ted", "Ted may treat any <i>end your turn</i> effect as -2 instead.");
      add("Unique Ursula", "Whenever Ursula rolls a <em class=roll>1</em> on either die, she gets +1 on her roll.");
      add("Volunteer Veda", "Veda gains 1 extra point for Event cards that award her points. She must attend all <em class=event>Meet and Greet</em> events.");
      add("Wandering Warren", "Warren finds his own path.  He doesn't get the +1 path bonus, but if he rolls a <em class=find>FIND</em> while moving, he may take an extra turn.");
      // add("Xander the Explorer", "Xander gets 2 points for every <em class=tile>Scenic View</em> he visits (once per).  He must end his turn on that tile.");
      add("Yuppie Yuri", "When Yuri finds a cache, he may keep both Equipment cards by discarding one of his current Equipment cards.");
      // add("Zealous Zach", 
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
      
      HtmlGenerator htmlGen = new HtmlGenerator(9, 3, 120, 0, 0, 80);
      htmlGen.printCachers(CACHERS);

      ImageGenerator imgGen = new ImageGenerator(ImageStats.getCacherStats(), false);
      for (Cacher cacher : CACHERS.values())
         imgGen.publish(cacher);
      System.out.println();
   }
}
