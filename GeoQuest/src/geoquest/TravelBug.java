package geoquest;

import java.util.Map;
import java.util.TreeMap;

public class TravelBug extends Component implements Comparable<TravelBug>
{
   static final Map<String, TravelBug> BUGS = new TreeMap<>();
   static
   {
      populate();
   }

   private String name;
   private String goal;

   public TravelBug(final String name, final String goal)
   {
      this.name = name;
      this.goal = goal.length() > 0 ? goal : CardUtils.BLANK;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public String getText()
   {
      return goal;
   }

   @Override
   public int compareTo(final TravelBug other)
   {
      return name.compareTo(other.name);
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof TravelBug)
         return name.equals(((TravelBug) other).name);
      return false;
   }

   private static void populate()
   {
      add("After You", "Lat or Lon\nV, W, or Y");
      add("Animal Lover", "Within\n3 tiles of\nthe Zoo"); // TODO
      add("Apropos", "Lat or\nLon with\nCacher's\nletter");
      add("Beast of Burden", "With 5+\nequipment\ncards");
      add("Bridge Too Far", "Within\n3 tiles of\na bridge");
      add("Bushwhacker", "At least\n4 tiles from\nany path");
      add("Camper", "Within 3\ntiles of the\ncampground");
      add("Can't Fool Me", "Search\nroll must\nbe 5+");
      add("Cornered", "In a\ncorner\nof any\nquadrant");
      add("Day Tripper", "Both\nsearch dice\nmust roll\nwhite"); // TODO: dice color
      add("Easy Puzzle", "Level 1\nor 2 Puzzle\ncache");
      add("Footprints", "Already\nfound by\n2+ other\nplayers");
      add("Frosty", "On or\nnext to a\npath that\nforks");
      add("Get Lucky", "Search\ndice must\nroll\ndoubles");
      add("Grover", "In an\nisolated\nForest tile");
      add("Hook Me Up", "With\nanother\nTravel Bug\nin it");
      add("Kid Friendly", "Level 1\nor 2 in a\nClear tile");
      add("Like I Care", "With an\nevent card\nplayed\non you");
      add("Long Hike", "On the\noutside\nedge of\nthe map");
      add("Looks Like Rain", "While a\nweather\nevent is\nin effect");
      add("Me First", "You must\nbe FTF");
      add("Night Owl", "Both\nsearch dice\nmust roll\nblack"); // TODO: dice color
      add("One More Time", "Multi-\ncache with\n4+ stages");
      add("Only the Best", "Level 5\ncache");
      add("Out of Town", "Farthest\nfrom an\nUrban tile");
      add("Park and Grab", "Level 1 in\nan Urban\ntile");
      add("Picture This", "At or\nnext to a\nScenic\nView");
      add("Radical", "Level\n4+ in an\nUrban tile");
      add("Rainbow", "Within\n3 tiles of a\nwaterfall");
      add("Rocky Road", "On a\nRocky tile\nwith a path");
      add("Safe Zone", "Between\nthe hospital\nand police\nstation");
      add("Sloth", "Level\n4+ Puzzle\ncache");
      add("Spelunker", "Within\n3 tiles\nof a cave");
      add("Spooky", "In or\nnext to a\ngraveyard");
      add("Swimmer", "Next to\na stream");
      add("Travel Light", "With 2\nor fewer\nequipment\ncards");
      add("Tree Hugger", "Level 3+\nin a Forest\ntile");
      add("Trvl Bg", "Lat or Lon\nA, E, I, O,\nor U");
      add("Will o' Wisp", "On a\nSwamp\ntile");
      
      // add("Art Critic", "Within 2 tiles of\nthe Museum");
      // add("Bookworm", "Within 2 tiles of\nthe Library");
      // add("Haystack", "Within 2 tiles of\na farm");
   }

   private static void add(final String name, final String goal)
   {
      BUGS.put(name, new TravelBug(name, goal));
   }

   public static void main(final String[] args)
   {
      HtmlGenerator htmlGen = new HtmlGenerator(80, 8);
      htmlGen.printTravelBugs(BUGS);
      
      ImageGenerator imgGen = new ImageGenerator(ImageStats.getShardStats(32), false);
      int i = 1;
      for (TravelBug tb : BUGS.values())
         imgGen.publish(tb, i++);
      System.out.println();
   }
}
