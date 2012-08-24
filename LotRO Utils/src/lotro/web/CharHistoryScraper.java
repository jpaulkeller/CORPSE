package lotro.web;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotro.models.Character;
import lotro.my.xml.CharacterXML;
import utils.Utils;
import web.ReadURL;
import file.FileUtils;

public class CharHistoryScraper
{
   protected static final String LOREBOOK_URL =
      "http://my.lotro.com/home/character/3751/146929937842963348/activitylog?cl[pp]=";
   
   private List<JournalEntry> entries = new ArrayList<JournalEntry>();
   
   private static final Pattern ENTRY = 
      Pattern.compile ("<td class=\"date\">([0-9]{4}/[0-9]{2}/[0-9]{2})</td>.*?" +
                       "icon_([^.]+).png\" />\\s*([^<]+?)\\s*<",
                       Pattern.MULTILINE | Pattern.DOTALL);
   
   protected static final SimpleDateFormat ENTRY_DATE = new SimpleDateFormat ("yyyy/MM/dd");
   
   private void scrape (final String charID, final int page)
   {
      System.out.println(LOREBOOK_URL + page);
      StringBuilder sb = ReadURL.capture(LOREBOOK_URL + page);
      parse(sb);
   }

   protected void parse (final CharSequence sb)
   {
      Matcher m = ENTRY.matcher (sb);
      while (m.find())
         entries.add (new JournalEntry (m));
   }
   
   private static final class JournalEntry
   {
      private String category;
      private Date date;
      private String topic;
      
      private JournalEntry (final Matcher m)
      {
         this.category = m.group (2);
         this.topic = m.group (3);
         try
         {
            date = ENTRY_DATE.parse (m.group (1));
         }
         catch (ParseException x) { }
      }
      
      @Override
      public String toString()
      {
         return ENTRY_DATE.format (date) + " - " + category + ": " + topic;
      }
   }
   
   public static void main (final String[] args) throws Exception
   {
      String name = "Mosby"; // 146929937842963348
      // String name = "Amryn"; // 146929937842963373
      
      Character ch = CharacterXML.getCharacter ("Landroval", "Mosby");
      String id = ch.getProp ("ID");
      
      CharHistoryScraper scraper = new CharHistoryScraper();
      for (int page = 1; page <= 224; page++)
      {
         scraper.scrape (id, page);
         Utils.sleep (Math.round (Math.random() * 2000)); // be nice
      }
      
      StringBuilder sb = new StringBuilder();
      for (JournalEntry entry : scraper.entries)
         sb.append(entry + "\n");
      
      String dir = System.getProperty ("user.home") + "/Desktop";
      File file = new File(dir + File.separator + "Mosby.txt");
      FileUtils.writeFile (file, sb, false);
      System.out.println ("Output: " + file);
   }
}
