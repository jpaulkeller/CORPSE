package geoquest;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class TravelBug extends Card implements Comparable<TravelBug>
{
   public static final String COLOR = "#F2F2F2";

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
      add("After You", "Lat or Lon<br />V, W, or Y");
      add("Trvl Bg", "Lat or Lon<br />A, E, I, O, or U");
      add("Animal Lover", "Within 3 tiles of<br />the Zoo");
      add("Bridge Too Far", "Within 3 tiles of<br />a bridge");
      add("Bushwhacker", "At least 5 tiles<br />from any path");
      add("Cornered", "In a corner of any quadrant");
      add("Day Tripper", "Both search dice must roll white"); // TODO: dice color
      add("Easy Puzzle", "Level 1 or 2<br />Puzzle cache");
      add("Footprints", "Already found by<br />2+ other players");
      add("Frosty", "On or next to<br />a path that forks");
      add("Get Lucky", "Search dice must roll doubles");
      add("Grover", "In an isolated Forest tile");
      add("Hook Me Up", "With another<br />Travel Bug in it");
      add("Kid Friendly", "Level 1 or 2 in a Clear tile");
      add("Long Hike", "On the outside edge of the map");
      add("Me First", "Lat or Lon with your Cacher's<br />first letter ");
      add("Night Owl", "Both search dice must roll black"); // TODO: dice color
      add("One More Time", "Multi-cache with 4+ stages");
      add("Only the Best", "Level 5 cache");
      add("Out of Town", "Farthest from<br />an Urban tile");
      add("Park and Grab", "Level 1 in<br />an Urban tile");
      add("Picture This", "At or next<br />to a Scenic View");
      add("Radical", "Level 4+ in an Urban tile");
      add("Rainbow", "Within 3 tiles of<br />a waterfall");
      add("Rocky Road", "On a Rocky tile<br />with a path");
      add("Sloth", "Level 4+ Puzzle cache");
      add("Spelunker", "Within 2 tiles<br />of a cave");
      add("Spooky", "In or next to<br />a graveyard"); // TODO
      add("Swimmer", "Next to<br />a stream");
      add("Tree Hugger", "Level 3+ in a Forest tile");
      add("Will o' Wisp", "On a Swamp tile");
      // add("Art Critic", "Within 2 tiles of<br />the Museum");
      // add("Bookworm", "Within 2 tiles of<br />the Library");
      // add("Appropos", "Cache name contains both Lat and Lon letters");
      // add("Flakey", "A hybrid cache");
      // add("Haystack", "Within 2 tiles of<br />a farm");
   }

   private static void add(final String name, final String goal)
   {
      BUGS.put(name, new TravelBug(name, goal));
   }

   static void print()
   {
      String target = "docs/TravelBugs.html";

      try
      {
         PrintWriter out = null;

         out = new PrintWriter(target);
         out.println("<html>");
         out.println("<body>\n");
         CardUtils.printStyle(out);

         int i = 0;
         for (TravelBug bug : BUGS.values())
            printCard(out, bug, i++);

         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(BUGS.size() + " bugs written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   private static final int PER_ROW = 8;
   private static final int PER_PAGE = 80;

   private static void printCard(final PrintWriter out, final TravelBug bug, final int i)
   {
      if (i % PER_PAGE == 0)
         out.println("<table cellspacing=10>\n");
      if (i % PER_ROW == 0)
         out.println("<tr>");

      // out.print (" <td bgcolor=" + COLOR + " align=center height=90 width=90>");
      out.print("<td style=\"" + " background-image: url(images/rules/TravelBug.png); " + " font-family: arial;" +
                " font-size: small;" + " color: black;" + "\" align=center height=70 width=113>");
      // out.print ("<span style=\"max-width: 90;\">");
      out.print("<b>" + bug.name.replace(" ",  "&nbsp;") + "</b><br />" + bug.goal);
      out.println("</td>");

      if (i % PER_ROW == PER_ROW - 1)
         out.println("</tr>\n");
      if (i % PER_PAGE == PER_PAGE - 1)
         out.println("</table></td>\n<p><hr><p>\n");
   }

   public static void main(final String[] args)
   {
      TravelBug.print();
      System.out.println();
      
      ImageGenerator imgGen = new ImageGenerator(ImageStats.getHexStats(), true);
      for (TravelBug tb : BUGS.values())
         imgGen.publish(tb);
      System.out.println();
   }
}
