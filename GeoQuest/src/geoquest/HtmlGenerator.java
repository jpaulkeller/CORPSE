package geoquest;

import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import geoquest.Event.Type;

public class HtmlGenerator
{
   public static final String CACHER_COLOR = "#F8E484";
   public static final String EVENT_COLOR = "#FFC4D2";
   public static final String ENSEMBLE_COLOR = "#DDCCFF";
   public static final String EQUIP_COLOR = "#93F3FF";
   public static final String TB_COLOR = "#F2F2F2";

   int cardsPerPage;
   int cardsPerRow;
   int width;
   int artH;
   int imageH;
   int textH;

   public HtmlGenerator(final int cardsPerPage, final int cardsPerRow, final int width,
                        final int pictureH, final int imageH, final int textH)
   {
      this.cardsPerPage = cardsPerPage;
      this.cardsPerRow = cardsPerRow;
      this.width = width;
      this.artH = pictureH;
      this.imageH = imageH;
      this.textH = textH;
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

   private void printToken(final PrintWriter out, final TravelBug bug, final int i)
   {
      if (i % cardsPerPage == 0)
         out.println("<table cellspacing=10>\n");
      if (i % cardsPerRow == 0)
         out.println("<tr>");

      out.print("<td style=\"" + " background-image: url(images/rules/TravelBug.png); " + " font-family: arial;" +
                " font-size: small;" + " color: black;" + "\" align=center height=70 width=113>");
      out.print("<b>" + bug.getName().replace(" ",  "&nbsp;") + "</b><br/>" + bug.getText().replace("\n", "<br/>"));
      out.println("</td>");

      if (i % cardsPerRow == cardsPerRow - 1)
         out.println("</tr>\n");
      if (i % cardsPerPage == cardsPerPage - 1)
         out.println("</table></td>\n<p><hr><p>\n");
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
         CardUtils.printStyle(out);

         int i = 0;
         for (Equipment eq : equipment.values())
            printCard(out, eq, i++);

         // pad with blanks to fill out the sheet
         Equipment blankCard = new Equipment(CardUtils.BLANK, "", "Blank", null, null);
         if (equipment.size() % cardsPerPage > 0)
            for (i = 0; i < cardsPerPage - (equipment.size() % cardsPerPage); i++)
               printCard(out, blankCard, i + equipment.size());

         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(equipment.size() + " events written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   private void printCard(final PrintWriter out, final Equipment eq, final int i)
   {
      if (i % cardsPerPage == 0)
         out.println("<table cellpadding=10>\n");
      if (i % cardsPerRow == 0)
         out.println("<tr>");

      out.println("<td valign=top><table border=1>");
      out.println(
         "  <tr><td align=center bgcolor=" + EQUIP_COLOR + "><b>" + eq.getName().replace(" ", "&nbsp;") + "</b></td></tr>");

      if (eq.getImage() != null)
      {
         out.println("  <tr><td align=center height=" + artH + " width=" + width + ">");
         out.print("      <img align=center src=\"../" + eq.getImage() + "\"");
         if (!eq.getImage().startsWith("Equipment"))
            out.print(" style=\"border:3px solid red\"");
         out.print(" height=" + imageH + ">");
      }

      out.println("</td></tr>");

      out.println("  <tr><td align=center height=" + textH + " width=" + width + ">");
      out.println(getText(eq.getText()));
      out.println("</td></tr>");
      
      String ensemble = eq.getEnsemble() != null ? eq.getEnsemble().replace(" ", "&nbsp;") : CardUtils.BLANK;
      out.println("  <tr><td align=center bgcolor=" + ENSEMBLE_COLOR + "><b>" + ensemble + "</b></td></tr>");
      out.println("</table></td>\n");

      if (i % cardsPerRow == cardsPerRow - 1)
         out.println("</tr>\n");
      if (i % cardsPerPage == cardsPerPage - 1)
         out.println("</table></td>\n<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>\n");
   }

   public void printEnsebles(final Map<String, Ensemble> ENSEMBLES)
   {
      String target = "docs/HTML/Ensembles.html";

      try
      {
         PrintWriter out = null;

         out = new PrintWriter(target);
         out.println("<html>");
         out.println("<body>\n");
         CardUtils.printStyle(out);

         out.println("<dl>");
         for (Ensemble ensemble : ENSEMBLES.values())
         {
            out.println("  <dt><b><em class=ensemble>" + ensemble.getName() + "</em>:</b>");
            out.println("      <em class=equipment>" + ensemble.eq1 + "</em> + ");
            out.println("      <em class=equipment>" + ensemble.eq2 + "</em> + ");
            out.println("      <em class=equipment>" + ensemble.eq3 + "</em></dt>");
            out.println("  <dd>" + ensemble.getText() + "</dd>");
         }
         out.println("</dl>\n");

         out.println("</body>");
         out.println("</html>");
         out.close();

         System.out.println(ENSEMBLES.size() + " ensembles written to: " + target);
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
            printCard(out, event, i++);

         // pad with blanks to fill out the sheet
         Event blankCard = new Event(CardUtils.BLANK, Type.STD, 0, CardUtils.BLANK, "Blank", null);
         if (events.size() % cardsPerPage > 0)
            for (i = 0; i < cardsPerPage - (events.size() % cardsPerPage); i++)
               printCard(out, blankCard, i + events.size());

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

   private void printCard(final PrintWriter out, final Event event, final int i)
   {
      if (i % cardsPerPage == 0)
         out.println("<table>\n");
      if (i % cardsPerRow == 0)
         out.println("<tr>");

      out.println("<td valign=top>");
      out.println("  <table class=\"event\">");
      out.println(
         "    <tr><td align=center bgcolor=" + EVENT_COLOR + "><b>" + event.getName().replace(" ", "&nbsp;") + "</b></td></tr>");

      out.print("    <tr><td align=center height=" + artH + " width=" + width + ">");
      if (event.getImage() != null)
      {
         out.print("<img align=center src=\"../" + event.getImage() + "\"");
         if (!event.getImage().startsWith("Events"))
            out.print(" style=\"border:2px solid red\"");
         out.print(" height=" + imageH + ">");
      }
      out.println("</td></tr>");

      int h = event.getType() == Type.STD ? textH : textH - 25;
      out.print("    <tr><td align=center height=" + h + " width=" + width + ">");
      out.print(getText(event.getText()));
      out.println("</td></tr>");

      if (event.getType() == Type.NOW)
         out.println("    <tr><td align=center bgcolor=salmon><b>Play Now</b></td></tr>");
      else if (event.getType() == Type.ANY)
         out.println("    <tr><td align=center bgcolor=lightgreen><b>Play Any Time</b></td></tr>");

      out.println("  </table></td>\n");

      if (i % cardsPerRow == cardsPerRow - 1)
         out.println("</tr>\n");
      if (i % cardsPerPage == cardsPerPage - 1)
         out.println("</table></td>\n<p><p>\n");
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
         CardUtils.printStyle(out);

         int i = 0;
         for (Cacher cacher : cachers.values())
            printCard(out, cacher, i++);

         // pad with blanks to fill out the sheet
         Cacher blankCard = new Cacher(CardUtils.BLANK, CardUtils.BLANK);
         for (i = 0; i < cardsPerPage - (cachers.size() % cardsPerPage); i++)
            printCard(out, blankCard, i + cachers.size());

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

   private void printCard(final PrintWriter out, final Cacher cacher, final int i)
   {
      if (i % cardsPerPage == 0)
         out.println("<table cellpadding=12>\n");
      if (i % cardsPerRow == 0)
         out.println("<tr>");

      out.println("<td><table border=1 valign=top>");
      out.print("  <tr><th align=center bgcolor=" + CACHER_COLOR + " width=" + width + ">");
      out.println("<b>" + cacher.getName().replace(" ", "&nbsp;") + "</b></th></tr>");
      out.print("  <tr><td height=" + textH + ">");
      out.print("<table cellpadding=10><tr><td align=center>" + getText(cacher.getText()) + "</td></tr></table>");
      out.println("</td></tr>");
      out.println("</table></td>\n");

      if (i % cardsPerRow == cardsPerRow - 1)
         out.println("</tr>\n");
      if (i % cardsPerPage == cardsPerPage - 1)
         out.println("</table></td><br/><br/><br/><br/><br/><br/><hr><p>");
   }

   private static final Pattern REF = Pattern.compile(">[^<]+ [^<]+?</em>");
   
   private String getText(final String text)
   {
      String html = text;
      Matcher m;
      while ((m = REF.matcher(html)).find())
         html = m.replaceFirst(m.group(0).replace(" ", "&nbsp;"));
      return html;
   }
}
