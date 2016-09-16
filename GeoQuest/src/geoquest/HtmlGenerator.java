package geoquest;

import java.io.PrintWriter;
import java.util.Map;

import geoquest.Event.Type;

public class HtmlGenerator
{
   public static final String EVENT_COLOR = "#FFC4D2"; 
   public static final String EQUIP_COLOR = "#93F3FF";


   int CardsPerPage;
   int CardsPerRow;
   int Width;
   int PictureH;
   int ImageH;
   int TextH;

   public HtmlGenerator(final int CardsPerPage, final int CardsPerRow,
                        final int Width, final int PictureH,
                        final int ImageH, final int TextH)
   {
      this.CardsPerPage = CardsPerPage;
      this.CardsPerRow = CardsPerRow;
      this.Width = Width;
      this.PictureH = PictureH;
      this.ImageH = ImageH;
      this.TextH = TextH;
   }
      
   public void printEquipment(final Map<String, Equipment> equipment)
   {
      String target = "docs/Equipment.html";
      
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
         if (equipment.size() % CardsPerPage > 0)
            for (i = 0; i < CardsPerPage - (equipment.size() % CardsPerPage); i++)
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
      if (i % CardsPerPage == 0)
         out.println("<table cellpadding=10>\n");
      if (i % CardsPerRow == 0)
         out.println("<tr>");

      out.println("<td valign=top><table border=1>");
      out.println("  <tr><td align=center bgcolor=" + EQUIP_COLOR + "><b>" + eq.getName() + "</b></td></tr>");

      if (eq.getImage() != null)
      {
         out.println("  <tr><td align=center height=" + PictureH + " width=" + Width + ">");
         out.print("      <img align=center src=\"" + eq.getImage() + "\"");
         if (eq.getImage().startsWith("Good"))
            out.print(" style=\"border:3px solid yellow\"");
         else if (!eq.getImage().startsWith("Equipment"))
            out.print(" style=\"border:3px solid red\"");
         out.print(" height=" + ImageH + ">");
      }

      out.println("</td></tr>");

      out.println("  <tr><td align=center height=" + TextH + " width=" + Width + ">" + eq.getText() + "</td></tr>");
      String combo = eq.getCombo() != null ? eq.getCombo() : CardUtils.BLANK;
      out.println("  <tr><td align=center bgcolor=" + Combo.COLOR + "><b>" + combo + "</b></td></tr>");
      out.println("</table></td>\n");

      if (i % CardsPerRow == CardsPerRow - 1)
         out.println("</tr>\n");
      if (i % CardsPerPage == CardsPerPage - 1)
         out.println("</table></td>\n<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>\n");

      // dump (eq);
   }

   public void printEvents(final Map<String, Event> events)
   {
      String target = "docs/Events.html";

      try
      {
         PrintWriter out = new PrintWriter (target);

         out.println ("<html>");
         out.println ("<body>\n");
         CardUtils.printStyle (out);

         int i = 0;
         for (Event event : events.values())
            printCard (out, event, i++);

         // pad with blanks to fill out the sheet
         Event blankCard = new Event (CardUtils.BLANK, Type.STD, 0, CardUtils.BLANK, "Blank");
         if (events.size() % CardsPerPage > 0)
            for (i = 0; i < CardsPerPage - (events.size() % CardsPerPage); i++)
               printCard (out, blankCard, i + events.size());
         
         out.println ("</body>");
         out.println ("</html>");

         out.close();
         
         System.out.println (events.size() + " events written to: " + target);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   private void printCard (final PrintWriter out, final Event event, final int i)
   {
      if (i % CardsPerPage == 0) out.println ("<table>\n");
      if (i % CardsPerRow == 0) out.println ("<tr>");

      out.println ("<td valign=top>");
      out.println ("  <table class=\"event\">");
      out.println ("    <tr><td align=center bgcolor=" + EVENT_COLOR + "><b>" + event.getName() + "</b></td></tr>");
      
      out.print    ("    <tr><td align=center height=" + PictureH + " width=" + Width + ">");
      if (event.getImage() != null)
      {
         out.print    ("<img align=center src=\"" + event.getImage() + "\"");
         if (event.getImage().startsWith ("Good"))
            out.print (" style=\"border:2px solid yellow\"");
         else if (!event.getImage().startsWith ("Events"))
            out.print (" style=\"border:2px solid red\"");
         out.print    (" height=" + ImageH + ">");
      }
      out.println  ("</td></tr>");

      int h = event.getType() == Type.STD ? TextH : TextH - 25;
      out.print ("    <tr><td align=center height=" + h + " width=" + Width + ">");
      out.print (event.getText());
      out.println ("</td></tr>");

      if (event.getType() == Type.NOW)
         out.println ("    <tr><td align=center bgcolor=salmon><b>Play Now</b></td></tr>");
      else if (event.getType() == Type.ANY)
         out.println ("    <tr><td align=center bgcolor=lightgreen><b>Discard To Play</b></td></tr>");
         
      out.println ("  </table></td>\n");

      if (i % CardsPerRow == CardsPerRow - 1) out.println ("</tr>\n");
      if (i % CardsPerPage == CardsPerPage - 1)
         out.println ("</table></td>\n<p><p>\n");
   }
}
