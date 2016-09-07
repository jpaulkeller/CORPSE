package geoquest;

import java.io.PrintWriter;
import java.util.Map;

import geoquest.Event.Type;

public class HtmlGenerator
{
   public static final String EVENT_COLOR = "#FFC4D2"; 

   public static final int CardsPerPage = 9;
   private static final int CardsPerRow  = 3;
   private static final int Width    = 200;
   private static final int PictureH =  95;
   private static final int ImageH   =  90;
   private static final int TextH    = 142;

   public static void print(final Map<String, Event> events)
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
         Event blankCard = new Event (CardUtils.BLANK, Type.Std, 0, CardUtils.BLANK, "Blank");
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

   private static void printCard (final PrintWriter out, final Event event, final int i)
   {
      if (i % CardsPerPage == 0) out.println ("<table>\n");
      if (i % CardsPerRow == 0) out.println ("<tr>");

      out.println ("<td valign=top>");
      out.println ("  <table class=\"event\">");
      out.println ("    <tr><td align=center bgcolor=" + EVENT_COLOR + "><b>" + event.getName() + "</b></td></tr>");
      
      out.print    ("    <tr><td align=center height=" + PictureH + " width=" + Width + ">");
      if (event.getImageName() != null)
      {
         out.print    ("<img align=center src=\"" + event.getImageName() + "\"");
         if (event.getImageName().startsWith ("Good"))
            out.print (" style=\"border:2px solid yellow\"");
         else if (!event.getImageName().startsWith ("Events"))
            out.print (" style=\"border:2px solid red\"");
         out.print    (" height=" + ImageH + ">");
      }
      out.println  ("</td></tr>");

      int h = event.getType() == Type.Std ? TextH : TextH - 25;
      out.print ("    <tr><td align=center height=" + h + " width=" + Width + ">");
      out.print (event.getText());
      out.println ("</td></tr>");

      if (event.getType() == Type.Now)
         out.println ("    <tr><td align=center bgcolor=salmon><b>Resolve Now</b></td></tr>");
      else if (event.getType() == Type.Discard)
         out.println ("    <tr><td align=center bgcolor=lightgreen><b>Discard To Resolve</b></td></tr>");
         
      out.println ("  </table></td>\n");

      if (i % CardsPerRow == CardsPerRow - 1) out.println ("</tr>\n");
      if (i % CardsPerPage == CardsPerPage - 1)
         out.println ("</table></td>\n<p><p>\n");
   }
}
