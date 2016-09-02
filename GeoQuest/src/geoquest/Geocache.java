package geoquest;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class Geocache implements Card, Comparable<Geocache>
{
   public static final String COLOR = "#254C00"; // dark olive green

   static final Map<String, Geocache> CACHES = new TreeMap<>();
   static
   {
      populate();
   }

   private String name;
   private String type;
   private int diff;
   private String text;
   private String color = "white";

   public Geocache(final String name, final String type, final int diff, final String text)
   {
      this.name = name;
      this.type = type;
      this.diff = diff;
      this.text = text;
      if (type != null)
         setColor();
   }

   private void setColor()
   {
      // hybrids
      if (type.contains("Micro") && type.contains("Multi"))
         color = "lightgreen";
      else if (type.contains("Micro") && type.contains("Puzzle"))
         color = "magenta";
      else if (type.contains("Multi") && type.contains("Puzzle"))
         color = "orange";

      else if (type.contains("Micro"))
         color = "cyan";
      else if (type.contains("Multi"))
         color = "yellow";
      else if (type.contains("Puzzle"))
         color = "pink";
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

   @Override
   public int compareTo(final Geocache other)
   {
      return name.compareTo(other.name);
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof Geocache)
         return name.equals(((Geocache) other).name);
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
      // traditional (20)
      add("Traditional 1a", null, 1, null);
      add("Planet APE", null, 2, "Draw 4 Equipment cards");
      add("North, To Alaska!", null, 3, "Move after finding");
      add("Traditional 4a", null, 4, null);
      add("Traditional 5a", null, 5, null);

      add("Bug Hotel", null, 1, "Starts with 3 TBs");
      add("Traditional 2b", null, 2, null);
      add("Leprechaun Ritual", null, 3, "Find requires one blue die");
      add("Traditional 4b", null, 4, null);
      add("Traditional 5b", null, 5, null);

      add("Traditional 1c", null, 1, null);
      add("Traditional 2c", null, 2, null);
      add("Traditional 3c", null, 3, null);
      add("Traditional 4c", null, 4, null);
      add("Traditional 5c", null, 5, null);

      add("Traditional 2d", null, 2, null);
      add("Traditional 3d", null, 3, null);
      add("Traditional 4d", null, 4, null);
      add("Traditional 2e", null, 2, null);
      add("Traditional 3e", null, 3, null);
      add("Traditional 4e", null, 4, null);
      add("Traditional 2f", null, 2, null);
      add("Traditional 3f", null, 3, null);
      add("Traditional 4f", null, 4, null);
      add("Traditional 3g", null, 3, null);

      // micro (10)
      add("Micro 1a", "Micro", 1, null);
      add("Micro 2a", "Micro", 2, null);
      add("Power of Projection", "Micro", 3, null);
      add("Micro 4a", "Micro", 4, null);
      add("Psycho Urban", "Micro", 5, null);

      add("Micro 1b", "Micro", 1, null);
      add("Micro 2b", "Micro", 2, null);
      add("Micro 3b", "Micro", 3, null);
      add("Micro 4b", "Micro", 4, null);
      add("Micro 5b", "Micro", 5, null);

      // multi (10)
      add("Multi 2a", "Multi (2)", 2, null);
      add("Multi 3a", "Multi (2)", 3, null);
      add("Multi 4a", "Multi (2)", 4, null);
      add("Multi 5a", "Multi (2)", 5, null);

      add("Multi 2b", "Multi (2)", 2, null);
      add("Multi 3b", "Multi (3)", 3, null);
      add("Multi 4b", "Multi (3)", 4, null);
      add("Multi 5b", "Multi (3)", 5, null);

      add("Multi 3c", "Multi (3)", 3, null);
      add("Multi 4c", "Multi (4)", 4, null);

      // puzzle (10)
      add("It's Comcastic!", "Puzzle (TV Shows)", 3, null);
      add("Solitary Confinement", "Puzzle (Games)", 3, null);
      add("Diagon Alley", "Puzzle (Harry Potter)", 3, null);
      add("Primal Instinct", "Puzzle (Animals)", 3, null);
      add("Pentagony", "Puzzle (5-Letter Words)", 4, null);

      add("Puzzle 1", "Puzzle (Food)", 1, null); // TBD
      add("Puzzle 2", "Tri Tri Again (Books)", 2, null);
      add("Puzzle 3", "Horses and Knights (Famous People)", 2, null);
      add("Puzzle 4", "Hour of Reflection (Places)", 4, null);
      add("Puzzle 5", "Photo Finish (Movies)", 5, null); // TBD 4

      // hybrid (10)
      add("Blood and Guts", "Multi (5) / Puzzle (Civil War)", 5, null);
      add("Tube Torcher", "Multi (3) / Puzzle (Sports)", 5, null);
      add("Eagle's Tour", "Multi (4) / Puzzle (Birds)", 4, null);
      add("Multi / Micro 3/3", "Multi (3) / Micro", 3, null);
      add("Multi / Micro 5/1", "Multi (5) / Micro", 2, null);
      add("Puzzle / Micro 2", "Puzzle (TBD) / Micro", 2, null);
      add("Puzzle / Micro 4", "Puzzle (TBD) / Micro", 4, null);
   }

   /*
    * --- CATEGORIES Songs Undersea Leaders
    * 
    * --- CACHES
    * 
    * // TBD unknown add ("Garden State Dark Way", null, 3, "Find requires one blue die"); add ("Tycho Anomaly", null,
    * 4, null);
    * 
    * Wildman Creek For the Birds Forbidden Tower Bewitched and Bothered The Treasure of Crawford County Ward’s Rusty
    * Box Geocaching In Hawaii Where’s In A Name? Monocacy Madness? Summiting For Cache Sake Melvin's Multiple Madness
    * BookCrossing Meets Geocaching The Adirondack Traveler Fairy Stones Museum of Modern Artifacts Cold Mountain
    * Heartbreak Illuminati Santa's Workshop For Whom the Bell Tolls Claustrophobia Met Arachnophobia Triple Dog Dare
    * You
    */

   private static void add(final String name, final String type, final int diff, final String text)
   {
      CACHES.put(name, new Geocache(name, type, diff, text));
   }

   static void print()
   {
      String target = "docs/Geocaches.html";

      try
      {
         PrintWriter out = null;

         out = new PrintWriter(target);
         out.println("<html>");
         out.println("<body>\n");
         CardUtils.printStyle(out);

         int i = 0;
         for (Geocache cache : CACHES.values())
            printCard(out, cache, i++);

         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(CACHES.size() + " geocaches written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   private static final int PER_ROW = 6;
   private static final int PER_PAGE = PER_ROW * 6;

   private static void printCard(final PrintWriter out, final Geocache cache, final int i)
   {
      if (i % PER_PAGE == 0)
         out.println("<table cellspacing=12>\n");
      if (i % PER_ROW == 0)
         out.println("<tr>");

      out.print("<td style=\"" + "background-color: " + cache.color + "; " + "background-image: url(images/rules/AmmoBox-" +
                cache.diff + ".png); " + "\" align=center height=110 width=160>");

      out.print("<table>");
      out.print("<tr style=\"");
      out.print(" color: white;");
      out.print(" font-family: arial;");
      out.print(" font-size: small;");
      out.print("\">");

      out.print("<td width=140 align=center>");
      out.print("<b>" + cache.name + "</b><br />");
      if (cache.type != null)
         out.print(getTypeText(cache.type) + "<br />");
      if (cache.text != null)
         out.print("<i>" + cache.text + "</i>");
      out.print("</td>");

      out.print("</tr>");
      out.print("</table>");

      out.println("</td>\n");

      if (i % PER_ROW == PER_ROW - 1)
         out.println("</tr>\n");
      if (i % PER_PAGE == PER_PAGE - 1)
         out.println("</table></td>\n<p><hr><p>\n");

      dump(cache);
   }

   private static String getTypeText(final String type)
   {
      if (type.contains("/"))
      {
         int split = type.indexOf("/");
         return getTypeText(type.substring(0, split)) + "<br />" + getTypeText(type.substring(split + 1));
      }
      else if (type.contains("Multi"))
         return "<span style=\"color: yellow;\">" + type + "</span>";
      else if (type.contains("Micro"))
         return "<span style=\"color: cyan;\">" + type + "</span>";
      else if (type.contains("Puzzle"))
         return "<span style=\"color: pink;\">" + type + "</span>";
      else // won't get here
         return type;
   }

   private static void dump(final Geocache cache)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(cache.name);
      sb.append(",");
      if (cache.type != null)
         sb.append(cache.type);
      else
         sb.append("Traditional");
      sb.append(",\"");
      if (cache.text != null)
         sb.append(cache.text);
      sb.append("\"");

      String s = sb.toString();
      s = s.replaceAll("&nbsp;", " ");
      s = s.replaceAll("<[^>]+>", "");
      System.out.println(s);
   }

   public static void main(final String[] args)
   {
      Geocache.print();
      System.out.println();
   }
}
