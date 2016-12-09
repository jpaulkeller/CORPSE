package geoquest;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HtmlGenerator
{
   int cardsPerPage;
   int cardsPerRow;

   public HtmlGenerator(final int cardsPerPage, final int cardsPerRow)
   {
      this.cardsPerPage = cardsPerPage;
      this.cardsPerRow = cardsPerRow;
   }
   
   public void printTravelBugs(final Map<String, TravelBug> bugs)
   {
      String target = "docs/HTML/TravelBugs.html";

      try
      {
         PrintWriter out = null;

         out = new PrintWriter(target);
         out.println("<html>");
         out.println("<body>\n");
         CardUtils.printStyle(out);

         int i = 0;
         for (TravelBug bug : bugs.values())
            printToken(out, bug, i++);

         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(bugs.size() + " bugs written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   private void printToken(final PrintWriter out, final Component token, final int i)
   {
      if (i % cardsPerPage == 0)
         out.println("<table cellspacing=10>\n");
      if (i % cardsPerRow == 0)
         out.println("<tr>");

      out.print("<td style=\"" + " background-image: url(images/rules/TravelBug.png); " + " font-family: arial;" +
                " font-size: small;" + " color: black;" + "\" align=center height=70 width=113>");
      out.print("<b>" + token.getName().replace(" ",  "&nbsp;") + "</b><br/>" + 
                token.getText().toString().replace("\n", "<br/>"));
      out.println("</td>");

      if (i % cardsPerRow == cardsPerRow - 1)
         out.println("</tr>\n");
      if (i % cardsPerPage == cardsPerPage - 1)
         out.println("</table></td>\n<p><hr><p>\n");
   }
   
   // Page settings in Chrome -- Margins: Custom, Top = 0.5, Left = 0.1, Right = 0.2
   
   public void printTokens()
   {
      String target = "docs/HTML/Tokens.html";

      try
      {
         List<String> tokens = new ArrayList<>();
         int width = 70;
         
         addDice(tokens, width);
         addCaches(tokens, width);

         PrintWriter out = new PrintWriter(target);
         out.println("<html>");
         out.println("<body>\n");

         int i = 0;
         for (String token : tokens)
         {
            if (i % cardsPerPage == 0)
               out.println("<table cellpadding=1 cellspacing=10>\n");
            if (i % cardsPerRow == 0)
               out.println("<tr>");
            
            out.print("  <td align=center><img align=center " + token + "></td>");
            
            if (i % cardsPerRow == cardsPerRow - 1)
               out.println("</tr>\n");
            if (i % cardsPerPage == cardsPerPage - 1)
               out.println("</table>\n");
            
            i++;
         }
         
         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(tokens.size() + " tokens written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }
   
   private void addDice(final List<String> tokens, final int width)
   {
      // green die
      tokens.add("src=\"../Tokens/Dice/GD-FIND.png\" width=" + width + "\"");
      tokens.add("src=\"../Tokens/Dice/GD-Black-1.png\" width=" + width + "\"");
      tokens.add("src=\"../Tokens/Dice/GD-Black-2.png\" width=" + width + "\"");
      tokens.add("src=\"../Tokens/Dice/GD-White-0.png\" width=" + width + "\"");
      tokens.add("src=\"../Tokens/Dice/GD-White-2.png\" width=" + width + "\"");
      tokens.add("src=\"../Tokens/Dice/GD-White-3.png\" width=" + width + "\"");

      // red die
      tokens.add("src=\"../Tokens/Dice/RD-DNF.png\" width=" + width + "\"");
      tokens.add("src=\"../Tokens/Dice/RD-Black-1.png\" width=" + width + "\"");
      tokens.add("src=\"../Tokens/Dice/RD-Black-2.png\" width=" + width + "\"");
      tokens.add("src=\"../Tokens/Dice/RD-Black-4.png\" width=" + width + "\"");
      tokens.add("src=\"../Tokens/Dice/RD-White-1.png\" width=" + width + "\"");
      tokens.add("src=\"../Tokens/Dice/RD-White-2.png\" width=" + width + "\"");
   }

   private void addCaches(final List<String> tokens, final int width)
   {
      int level = 1;
      addNormal(tokens, "Cache", 11, level++, width);
      addNormal(tokens, "Cache", 11, level++, width); // 14
      addNormal(tokens, "Cache", 11, level++, width); // 16
      addNormal(tokens, "Cache", 11, level++, width); // 14
      addNormal(tokens, "Cache", 11, level++, width);

      level = 1;
      addNormal(tokens, "Puzzle cache", 8, level++, width);
      addNormal(tokens, "Puzzle cache", 8, level++, width); // 10
      addNormal(tokens, "Puzzle cache", 8, level++, width); // 12
      addNormal(tokens, "Puzzle cache", 8, level++, width); // 10
      addNormal(tokens, "Puzzle cache", 8, level++, width);
      
      addMultis(tokens, 2, 2, 5, width);
      addMultis(tokens, 3, 1, 5, width);
      addMultis(tokens, 4, 2, 4, width);
   }
   
   private void addNormal(final List<String> tokens, final String prefix, final int qty, final int level, final int width)
   {
      for (int count = 0; count < qty; count++)
         tokens.add("src=\"../Tokens/Caches/" + prefix + " " + level + ".png\" " + 
                  "width=" + width + " height=" + (width - 1) + "\"");
   }

   private void addMultis(final List<String> tokens, final int stages, final int minLevel, final int maxLevel, final int width)
   {
      for (int level = minLevel; level <= maxLevel; level++)
         tokens.add("src=\"../Tokens/Caches/Multi-cache " + stages + "x " + level + ".png\" " + 
                  "width=" + width + " height=" + (width - 1) + "\"");
   }

   public void printCachers(final Map<String, Cacher> cachers)
   {
      String target = "docs/HTML/Cachers.html";

      try
      {
         PrintWriter out = null;

         out = new PrintWriter(target);
         out.println("<html>");
         out.println("<body>\n");

         int i = 0;
         for (Cacher cacher : cachers.values())
            printCard(out, "../Cards/Cachers/", "height=210", cacher, i++);

         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(cachers.size() + " cachers written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   public void printEnsembles(final Map<String, Ensemble> ensembles)
   {
      String target = "docs/HTML/Ensembles.html";

      try
      {
         PrintWriter out = null;

         out = new PrintWriter(target);
         out.println("<html>");
         out.println("<body>\n");

         int i = 0;
         for (Ensemble en : ensembles.values())
            printCard(out, "../Cards/Ensembles/", "height=210", en, i++);

         // pad with blanks to fill out the sheet
         Equipment blankCard = new Equipment(CardUtils.BLANK, "", "Blank", null, null);
         if (ensembles.size() % cardsPerPage > 0)
            for (i = 0; i < cardsPerPage - (ensembles.size() % cardsPerPage); i++)
               printCard(out, "../Cards/Ensembles/", "height=210", blankCard, i + ensembles.size());

         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(ensembles.size() + " ensembles written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   public void printEquipment(final Map<String, Equipment> equipment)
   {
      String target = "docs/HTML/Equipment.html";

      try
      {
         PrintWriter out = null;

         out = new PrintWriter(target);
         out.println("<html>");
         out.println("<body>\n");
         // CardUtils.printStyle(out); // old

         int i = 0;
         for (Equipment eq : equipment.values())
            printCard(out, "../Cards/Equipment/", "width=210", eq, i++);

         // pad with blanks to fill out the sheet
         Equipment blankCard = new Equipment(CardUtils.BLANK, "", "Blank", null, null);
         if (equipment.size() % cardsPerPage > 0)
            for (i = 0; i < cardsPerPage - (equipment.size() % cardsPerPage); i++)
               printCard(out, "../Cards/Equipment/", "width=210", blankCard, i + equipment.size());

         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(equipment.size() + " equipment written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   public void printEvents(final Map<String, Event> events)
   {
      String target = "docs/HTML/Events.html";

      try
      {
         PrintWriter out = new PrintWriter(target);

         out.println("<html>");
         out.println("<body>\n");
         CardUtils.printStyle(out);

         int i = 0;
         for (Event event : events.values())
            printCard(out, "../Cards/Events/", "width=200", event, i++);

         out.println("</body>");
         out.println("</html>");

         out.close();

         System.out.println(events.size() + " events written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }
   
   private void printCard(final PrintWriter out, final String path, final String size, final Component card, final int i)
   {
      String name = path + card.getName().replaceAll("[^-A-Za-z0-9' ]", "") + ".png";

      if (i % cardsPerPage == 0)
         out.println("<table cellpadding=10>\n");
      if (i % cardsPerRow == 0)
         out.println("<tr>");

      out.print("  <td align=center>");
      out.print("<img align=center src=\"" + name + "\" " + size + ">");
      out.println("</td>");

      if (i % cardsPerRow == cardsPerRow - 1)
         out.println("</tr>\n");
      if (i % cardsPerPage == cardsPerPage - 1)
         out.println("</table></td>\n<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>\n");
   }

   public static void main(final String[] args)
   {
      HtmlGenerator htmlGen = new HtmlGenerator(30, 10);
      htmlGen.printTokens();
   }
}
