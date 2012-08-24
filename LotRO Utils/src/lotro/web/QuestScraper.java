package lotro.web;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotro.models.Character;
import lotro.my.xml.CharacterXML;
import lotro.my.xml.LogScraper;
import file.FileUtils;

public class QuestScraper extends LogScraper
{
   private static final Pattern QUEST = 
      Pattern.compile ("<div class=\"clog_event\">" +
                       "<a href=\"" + LOREBOOK_URL + "[?]id=([0-9]+)\">([^<]+)</a></div>." +
                       "<div class=\"clog_event_date\">([^<]+)</div>",
                       Pattern.MULTILINE | Pattern.DOTALL);
   
   private List<Quest> quests = new ArrayList<Quest>();

   @Override
   protected void parse (final String html)
   {
      Matcher m = QUEST.matcher (html);
      while (m.find())
         quests.add (new Quest (m));
   }
   
   private static final class Quest
   {
      private String id;
      private String name;
      private Date date;
      
      private Quest (final Matcher m)
      {
         this.id   = m.group (1); 
         this.name = m.group (2);
         try
         {
            date = HTML_DATE.parse (m.group (3));
         }
         catch (ParseException x) { }
      }
      
      @Override
      public String toString()
      {
         return name + " (" + USER_DATE.format (date) + ")";
      }
      
      public String toListItem()
      {
         return "<li><a href=\"" + LOREBOOK_URL + "?id=" + id + "\">" + name + 
                "</a> (" + USER_DATE.format (date) + ")</li>";
      }
   }
   
   public static void main (final String[] args) throws Exception
   {
      String name = "Mosby"; // 146929937842963348
      // String name = "Amryn"; // 146929937842963373
      
      Character ch = CharacterXML.getCharacter ("Landroval", "Mosby");
      String id = ch.getProp ("ID");
      
      QuestScraper scraper = new QuestScraper();
      scraper.scrape ("quest", id);
      
      List<String> lines = new ArrayList<String>();
      lines.add ("<html>");
      lines.add ("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");

      lines.add ("<body>");
      lines.add ("<h2>Quests Completed by " +
                 "<a href=\"http://my.lotro.com/character/" + 
                 ch.getWorld().toLowerCase() + "/" +
                 name.toLowerCase() + "/\" target=_blank>" + name + "</a></h2>");
      lines.add ("<ol>");
      Collections.reverse (scraper.quests); // sort by date
      for (Quest quest : scraper.quests)
         lines.add ("  " + quest.toListItem());
      lines.add ("</ol>");
      lines.add ("</body>");
      lines.add ("</html>");
      
      String path = Dropbox.get().getPath ("/quests/" + name + ".html");
      FileUtils.writeList (lines, path, false);
      System.out.println ("Output: " + path);
   }
}
