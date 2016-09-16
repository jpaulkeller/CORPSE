package geoquest;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import str.StringUtils;

public class Cacher extends Card implements Comparable<Cacher>
{
   public static final String COLOR = "#F8E484";

   static final Map<String, Cacher> CACHERS = new TreeMap<>();
   static
   {
      populate();
   }

   private String name;
   private String equipment;
   private String text;

   public Cacher(final String name, final String startingEquipment, final String text)
   {
      this.name = name.replaceAll(" ", "&nbsp;");
      this.text = text.length() > 0 ? text : CardUtils.BLANK;
      if (startingEquipment != null)
         this.equipment = startingEquipment.replaceAll(" ", "&nbsp;");
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

   public String getEquipment()
   {
      return equipment;
   }

   @Override
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
      add("Athletic Amanda", "Hiking Boots", "Amanda gets +1 to all rolls on Rocky tiles.");
      add("Birder Brandon", "Binoculars", "Brandon has an eye for details; he gets +1 when searching for Multi-caches.");
      add("Collector Colin", "Geocoin",
         "Colin can carry any number of Travel Bugs; and he starts the game with a (random) Travel Bug.");
      add("Determined Dan", "Duct Tape", "Once per turn, Dan may re-roll a <em class=dnf>DNF</em> when searching.");
      add("Eager Earl", "Emergency Radio", "When Earl gets equipment from a cache, he may draw an extra card to choose from.");
      add("Fast Freddie", "Flashlight",
         "Freddie always moves first.  Whenever a new cache is placed on the board, Freddie immediately takes an extra turn.");
      add("Grampa Gary", "Gaiters", "Gary takes his time; he gets -1 to all Move rolls, and +1 to all Search rolls.");
      add("Hunter Henry", "Hat",
         "Henry gets +1 when searching in Forest tiles.  Other players can't play events on Henry if he's on a Forest tile.");

      add("Independent Isabel", "Insect Repellent",
         "Other players may only play events on Isabel if they are in the same map quadrant.");
      add("Jolly Jamie", "Gorp", "Other players can't play events on Jamie (unless she has the most points).");
      add("Kindly Kate", "First-aid Kit", "During her turn, Kate can discard an Event card to gain 1 point.");
      add("Lucky Lisa", "Long Pants", "Lisa gets +1 whenever she rolls doubles.");
      add("Marathon Mike", "Mountain Bike", "Tiles next to a path count as a path for Mike.");
      add("Nosey Norman", "Mirror",
         "Whenever another player rolls <em class=roll>5</em> or higher, Norman may immediately move 1 tile.");
      add("Observant Oscar", "Ol' Blue", "If another player is within 2 tiles of him, Oscar gets +2 on his roll.");
      add("Puzzler Paul", "PDA", "Paul gets +1 when searching for puzzle caches.");
      add("Quirky Quigly", "Map",
         "Quigly may hold any number of Event cards; and he starts the game with a (random) Event card.");

      add("Ranger Rachel", "Rope", "Rachel gets +2 to all Move rolls in Forest tiles.");
      add("Scout Scotty", "Swag Bag", "During his turn, Scotty may discard any Equipment cards to gain 1 point each.");
      add("Tireless Ted", "Trail Guide", "Ted may treat any <i>end your turn</i> effect as -2 instead.");
      add("Unique Ursula", "Utility Tool",
         "Whenever Ursula rolls a <em class=roll>1</em> on either die, she gets +1 on her roll.");
      // TODO
      add("Volunteer Veda", "CITO Bag",
         "Veda gains 1 extra point for all Event cards that award her points. She must attend all Meet and Greet events.");
      add("Wandering Warren", "Waders",
         "Warren finds his own path.  He doesn't get the +1 path bonus, but if he rolls a <em class=find>FIND</em> while moving, he may take an extra turn.");
      // TODO: change Scenic View to Point of Interest?
      add("Xander the Explorer", "Antenna",
         "Xander gets 2 points for every <em class=tile>Scenic View</em> he visits (once per).  He must end his turn on that tile.");
      add("Yuppie Yuri", "Jeep",
         "When Yuri finds a cache, he may keep both Equipment cards by disarding one of his current Equipment cards.");
      // TODO
      add("Zealous Zach", "1 (random) Equipment Card",
         "Zach gets +1 on all Move rolls, as long as he has at least 2 empty Equipment slots.");
   }

   private static void add(final String cardName, final String equip, final String cardText)
   {
      Cacher cacher = new Cacher(cardName, equip, cardText);
      CACHERS.put(cacher.getName(), cacher);
   }

   static void print()
   {
      String target = "docs/Cachers.html";

      try
      {
         PrintWriter out = null;

         out = new PrintWriter(target);
         out.println("<html>");
         out.println("<body>\n");
         CardUtils.printStyle(out);

         int i = 0;
         for (Cacher cacher : CACHERS.values())
            printCard(out, cacher, i++);

         // pad with blanks to fill out the sheet
         Cacher blankCard = new Cacher(CardUtils.BLANK, "", CardUtils.BLANK);
         for (i = 0; i < CardsPerPage - (CACHERS.size() % CardsPerPage); i++)
            printCard(out, blankCard, i + CACHERS.size());

         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(CACHERS.size() + " cachers written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   private static final int CardsPerPage = 9;
   private static final int CardsPerRow = 3;
   private static final int Height = 120; // 180;
   private static final int Width = 225; // 250;

   private static void printCard(final PrintWriter out, final Cacher cacher, final int i)
   {
      if (i % CardsPerPage == 0)
         out.println("<table cellpadding=12>\n");
      if (i % CardsPerRow == 0)
         out.println("<tr>");

      out.println("<td><table border=1 valign=top>");
      out.print("  <tr><th align=center bgcolor=" + COLOR + " width=" + Width + ">");
      out.println("<b>" + cacher.name + "</b></th></tr>");
      out.print("  <tr><td height=" + Height + ">");
      out.print("<table cellpadding=10><tr><td align=center>" + cacher.text + "</td></tr></table>");
      out.println("</td></tr>");
      /*
       * out.println ("  <tr><td height=130 width=250 align=center>" + cacher.text + "</td></tr>"); out.println (
       * "  <tr><td height=50 bgcolor=" + Equipment.COLOR + " align=center valign=top>" +
       * "<i>Starting Equipment:</i><br><b>" + cacher.equipment + "</b></td></tr>");
       */
      out.println("</table></td>\n");

      dump(cacher);

      if (i % CardsPerRow == CardsPerRow - 1)
         out.println("</tr>\n");
      if (i % CardsPerPage == CardsPerPage - 1)
         out.println("</table></td><br/><br/><br/><br/><br/><br/><hr><p>");
   }

   private static void dump(final Cacher cacher)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(cacher.name);
      sb.append(",\"");
      sb.append(cacher.text);
      sb.append("\",");
      sb.append(cacher.equipment);
      String s = sb.toString();
      s = s.replaceAll("&nbsp;", " ");
      s = s.replaceAll("<[^>]+>", "");
      System.out.println(s);
   }

   private static void showCachers()
   {
      for (Cacher cacher : CACHERS.values())
         if (cacher.name != CardUtils.BLANK)
         {
            System.out.print(StringUtils.pad(cacher.toString(), 20));
            System.out.print(StringUtils.pad(cacher.equipment, 30));
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
      // showCachers();
      Cacher.print();
      System.out.println();

      System.out.println("Validating Equipment References:");
      for (Cacher cacher : CACHERS.values())
      {
         Equipment eq = Equipment.EQUIPMENT.get(cacher.equipment);
         if (eq == null && !cacher.equipment.equals("") && !cacher.equipment.contains("(random)"))
            System.err.println("Invalid equipment for " + cacher.getName() + ": " + cacher.equipment);
      }
      System.out.println();
      System.out.flush();
      System.err.flush();
   }
}
