package lotro.quest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

public final class ScrapeQuests
{
   private static final String LOREBOOK = "http://lorebook.lotro.com/";
   private static final String ENCODING = "UTF8";
   
   // <ul><li><a href="/wiki/Quest:A_Curious_Number_of_Bears"
   // title="Quest:A Curious Number of Bears">Quest:A Curious Number of
   // Bears</a></li>
   private static final Pattern QUEST_PATTERN = 
      Pattern.compile ("wiki/Quest:([^\"]+)");

   // <div class="lorebooktitle">Quest: The Tomb 0f Elendil</div>
   private static final Pattern TITLE_PATTERN = 
      Pattern.compile (".*<div class=\"lorebooktitle\">Quest: ([^<]+)</div>.*");

   // <div class="questfield"><b>Description:</b>&nbsp;Gwindeth has...</div>
   private static final Pattern DESC_PATTERN = 
      Pattern.compile (".*<b>Description:</b>&nbsp;([^<]+)</div>.*");

   // <div class="questfield"><b>Category:</b>&nbsp;Evendim</div>
   private static final Pattern CAT_PATTERN = 
      Pattern.compile (".*<b>Category:</b>&nbsp;([^<]+)</div>.*");

   // <div class="questfield"><b>Quest Level:</b>&nbsp;40</div>
   private static final Pattern LEVEL_PATTERN = 
      Pattern.compile (".*<b>Min Level:</b>&nbsp;([0-9]+)</div>.*");

   // <div class="bestowerlink">...<a href="/index.php/NPC:Eilig">Eilig</a>
   private static final Pattern SOURCE_PATTERN = 
      Pattern.compile (".*<div class=\"bestowerlink\">.*?<a href=[^>]*?>([^<]+)</a>.*");
   
   // <div class="bestowertext">'My brothers and I...<br /><br />...'</div></div>
   private static final Pattern TEXT_PATTERN = 
      Pattern.compile (".*<div class=\"bestowertext\">(.*?)</div>.*");

   private static final Pattern PREREQ_PATTERN = 
      Pattern.compile ("headings/hdg_prerequisite_quests[.]gif");
   private static final Pattern FOLLOW_PATTERN = 
      Pattern.compile ("headings/hdg_next_quest[.]gif");
   // <tr><td><a href="/index.php/Quest:A_Striking_Absence_of_Boar">A Striking
   // Absence of Boar</a></td></tr>
   private static final Pattern REQUISITE_PATTERN = 
      Pattern.compile ("index.php/Quest:([^\"]+)");
   private static final Pattern OR_PATTERN = // for OR'd prerequisites 
      Pattern.compile ("<table class=\"questprereq\">.*&nbsp;OR&nbsp;.*</table>");
   
   // <a href="/index.php/Item:Superior_Stew_of_Kings">Superior Stew of Kings</a>
   private static final Pattern REWARD_PATTERN =
      Pattern.compile ("<a href=\"/index.php/([^:]+):([^\"]+)\">([^<]+)</a>");
   private static final Pattern ITEM_PATTERN =
      Pattern.compile ("Armour|Item|Weapon|Title|Tool|Trait");
   private static final Pattern NOT_ITEM_PATTERN =
      Pattern.compile ("Class|Monster|NPC|Quest|Race");
   private static final Pattern OPTION_PATTERN = Pattern.compile ("Selectable Items");

   private static final SortedMap<String, Quest> QUEST_MAP = new TreeMap<String, Quest>();
   private static final Queue<Quest> QUEST_QUEUE = new LinkedList<Quest>();
   private static final Set<Quest> SCRAPED = new HashSet<Quest>();
   private static final Set<Quest> SHOWN = new HashSet<Quest>();
   private static String category;
   
   private static enum Order { UNKNOWN, PREQUEL, SEQUEL };
   private static Order order = Order.UNKNOWN; 

   private ScrapeQuests() { /* prevent instantiation */ }
   
   public static void scrapeCategory (final String address)
   {
      BufferedReader buf = null;
      try
      {
         InputStream is;
         if (address.startsWith ("http://"))
            is = new URL (address).openStream();
         else
            is = new FileInputStream (address);
         InputStreamReader isr = new InputStreamReader (is, ENCODING);
         buf = new BufferedReader (isr);

         String line;
         while ((line = buf.readLine()) != null)
         {
            Matcher matcher = QUEST_PATTERN.matcher (line);
            while (matcher.find())
               QUEST_QUEUE.add (getQuest (matcher.group (1)));
         }
      }
      catch (MalformedURLException x)
      {
         System.err.println (x);
      }
      catch (IOException x)
      {
         System.err.println (x);
      }
      finally
      {
         FileUtils.close (buf);
      }

      System.out.println (QUEST_QUEUE.size() + " quests in queue.");
      while (!QUEST_QUEUE.isEmpty())
      {
         Quest quest = QUEST_QUEUE.remove();
         if (!SCRAPED.contains (quest))
            scrapeQuest (quest, LOREBOOK + "wiki/Quest:" + quest.getName());
      }
   }

   private static Quest scrapeQuest (final Quest quest, final String address)
   {
      System.out.println ("Scrape: " + quest);
      SCRAPED.add (quest);
      
      BufferedReader buf = null;
      try
      {
         InputStream is;
         if (address.startsWith ("http://"))
            is = new URL (address).openStream();
         else
            is = new FileInputStream (address);
         
         InputStreamReader isr = new InputStreamReader (is, ENCODING);
         buf = new BufferedReader (isr);

         order = Order.UNKNOWN;
         String line;
         while ((line = buf.readLine()) != null)
            parseQuest (quest, line);
      }
      catch (MalformedURLException x)
      {
         System.err.println (x);
      }
      catch (IOException x)
      {
         System.err.println (x);
      }
      finally
      {
         if (buf != null)
            try { buf.close(); } catch (IOException x) { }
      }
      
      try { Thread.sleep (500); } catch (InterruptedException x) { } // be nice

      return quest;
   }

   private static void parseQuest (final Quest quest, final String line)
   {
      Matcher m = TITLE_PATTERN.matcher (line);
      if (m.matches())
         quest.setName (m.group (1));

      m = DESC_PATTERN.matcher (line);
      if (m.matches())
         quest.setDesc (m.group (1));

      m = CAT_PATTERN.matcher (line);
      if (m.matches())
      {
         quest.setCategory (m.group (1));
         if (category == null)
            category = quest.getCategory(); // capture the first category
         else if (!category.equals (quest.getCategory()))
            System.out.println (">>> Skipping: " + quest);
      }

      m = LEVEL_PATTERN.matcher (line);
      if (m.matches())
         quest.setLevel (Integer.parseInt (m.group (1)));

      if (category != null && category.equals (quest.getCategory()))
      {
         m = SOURCE_PATTERN.matcher (line);
         if (m.matches())
            quest.setBestower (m.group (1));
         
         m = TEXT_PATTERN.matcher (line);
         if (m.matches())
            quest.setText (m.group (1));

         if (PREREQ_PATTERN.matcher (line).find())
            order = Order.PREQUEL;
         else if (FOLLOW_PATTERN.matcher (line).find())
            order = Order.SEQUEL;
         parseRelated (quest, line);
      
         parseRewards (quest, line);
      }
   }

   private static void parseRelated (final Quest quest, final String line)
   {
      Matcher m = REQUISITE_PATTERN.matcher (line);
      if (m.find())
      {
         boolean or = OR_PATTERN.matcher (line).find();
         do
         {
            Quest related = getQuest (m.group (1));
            
            if (!quest.isEpic() && related.isEpic())
               continue; // these are handled elsewhere
            
            if (order == Order.PREQUEL) 
               quest.addPrereq (related, or);
            else if (order == Order.SEQUEL) 
               quest.addSequel (related);
            else
               System.err.println ("Indeterminate requisite for " +
                        quest.getName() + ": " + related.getName());

            if (!QUEST_QUEUE.contains (related))
               QUEST_QUEUE.add (related);
         }
         while (m.find());
      }
   }
   
   private static void parseRewards (final Quest quest, final String line)
   {
      Matcher m = REWARD_PATTERN.matcher (line);
      if (m.find())
      {
         int select = Integer.MAX_VALUE;
         Matcher m2 = OPTION_PATTERN.matcher (line);
         if (m2.find())
            select = m2.start();

         do
         {
            String type = m.group (1);
            if (ITEM_PATTERN.matcher (type).matches())
            {
               boolean option = m.start() > select;
               String link = LOREBOOK + "index.php/" + type + ":" + m.group (2);
               String name = m.group (3);
               quest.addReward (new Reward (type, name, link, option));
            }
            else if (!NOT_ITEM_PATTERN.matcher (type).matches())
               System.out.println ("Unknown tag: " + type);
         } while (m.find());
      }
   }

   private static Quest getQuest (String name)
   {
      if (name.endsWith ("_"))
         name = name.substring (0, name.length() - 1); // strip trailing "_"
      String cleanName = Quest.decode (name);
      Quest quest = QUEST_MAP.get (cleanName);
      if (quest == null)
      {
         quest = new Quest (name);
         QUEST_MAP.put (cleanName, quest);
      }
      return quest;
   }

   private static void exportAsHTML (final String outFile)
   throws FileNotFoundException, UnsupportedEncodingException
   {
      FileOutputStream fos = new FileOutputStream (outFile);
      PrintStream out = new PrintStream (fos, true, ENCODING);

      out.println ("<html>");
      out.println ("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");

      showChains (out);

      out.println ("<hr>\n");
      
      for (Quest quest : QUEST_MAP.values())
         out.println (quest.export());
      
      out.println ("</html>");
      out.flush();
      out.close();
      
      System.out.println ("Output written to: " + outFile);
   }

   // for each "final" quest with more than 1 pre-requisite, show the chain
   
   private static void showChains (final PrintStream out)
   {
      for (Quest quest : QUEST_MAP.values())
      {
         Stack<Quest> chain = quest.getChain();
         Iterator<Quest> iter = chain.iterator();
         while (iter.hasNext())
            if (SHOWN.contains (iter.next()))
               iter.remove ();
         if (chain.size() >= 3)
         {
            out.println ("<p><ol>");
            while (!chain.isEmpty())
            {
               Quest q = chain.pop();
               out.println ("  <li>" + q.getLinkLocal());
               SHOWN.add (q);
            }
            out.println ("</ol>\n");
         }
      }
   }
   
   public static void main (final String[] args) throws Exception
   {
      String type =
         // "Angmar";
         // "Bree-land";
         // "Carn_D%C3%BBm";
         // "Ered_Luin";
         // "Ettenmoors";
         // "Evendim";
         // "Fornost";
         // "Garth_Agarwen";
         // "Great_Barrows";
         // "Lone-lands";
         // "Misty_Mountains";
         // "North_Downs";
         // "Shire";
         // "Trollshaws";
         // "Urugarth";

         // PvMP
         // "Grimwood";
         // "Grothum"; // TBD
         // "Hoarhallow";
         // "Isendeep_Mine";
         // "Lugazag";
         // "Monster_Play";
         // "Tirith_Rhaw";
         // "Tol_Ascarnen";
         
         // CLASS
         // "Burglar";
         // "Captain";
         // "Champion";
         // "Guardian";
         // "Hunter";
         // "Lore-master";
         // "Minstrel";

         // "Introduction";
         // "Epic_-_Prologue";
         // "Epic_-_Book_I:_Stirrings_in_the_Darkness";
         // "Epic_-_Book_II:_The_Red_Maid";
         // "Epic_-_Book_III:_The_Council_of_the_North";
         // "Epic_-_Book_IV:_Chasing_Shadows";
         // "Epic_-_Book_V:_The_Last_Refuge";
         // "Epic_-_Book_VI:_Fires_in_the_North";
         // "Epic_-_Book_VII:_The_Hidden_Hope";
         // "Epic_-_Book_VIII:_The_Scourge_of_the_North";
         // "Epic_-_Book_IX:_The_Shores_of_Evendim";
         // "Epic_-_Book_X:_The_City_of_the_Kings";
         "Epic_-_Book_XI:_Prisoner_of_the_Free_Peoples";
            
         // "Crafting";

      String url = LOREBOOK + "wiki/Category:" + type + "_Quests";
      ScrapeQuests.scrapeCategory (url);
      String outFile = type.replace (':', '_');
      String home = System.getProperty ("user.home");
      exportAsHTML (home + "/Desktop/" + outFile + ".html");
      
      /*
      String name = "test";
      String addr = home + "/Desktop/test.html";
      Quest quest = scrapeQuest (new Quest (name), addr);
      questMap.put (name, quest);
      exportAsHTML (home + "/Desktop/output.html");
      */
   }
}
