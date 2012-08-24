package lotro.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

public final class TopicCounter
{
   class Topic implements Comparable<Topic>
   {
      private String id;
      private String title;
      private int replies;
      private int views;
      
      Topic (final String id, final String title,
             final int replies, final int views)
      {
         this.id = id;
         this.title = title.replaceAll ("&quot;", "\"");
         this.replies = replies;
         this.views = views;
      }
      
      @Override
      public String toString()
      {
         String url = "http://forums.lotro.com/showthread.php?t=" + id;
         return "[URL=\"" + url + "\"]" + title + "[/URL] " +
                "([COLOR=Red]" + replies + "[/COLOR]/" + views + ")";
      }

      public int compareTo (final Topic o)
      {
         // return o.replies - replies;
         return title.compareTo (o.title);
      }
   }
   
/*   
   <a href="showthread.php?s=617f6075deffc77187915c622e530caa&amp;t=304696" id="thread_title_304696" style="font-weight:bold">Making ingots..</a>
   ...
   <td class="alt2" title="Replies: 11, Views: 166">
*/

   private static final Pattern TOPIC_PATTERN = 
      Pattern.compile ("<a href=\"showthread[.]php[?](?:s=[a-f0-9]*&amp;)?t=(\\d+)\"[^>]*\">([^<]*)</a>.*?" +
                       "<td class=\"alt2\" title=\"Replies: ([0-9,]+), Views: ([0-9,]+)\">",
                       Pattern.DOTALL | Pattern.MULTILINE);

   private List<Topic> topics = new ArrayList<Topic>();

   private TopicCounter()
   {
      /* prevent instantiation */
   }

   public boolean scrape (final String address)
   {
      // System.out.println (address);
      
      InputStream is = null;
      try
      {
         if (address.startsWith ("http://"))
            is = new URL (address).openStream();
         else
            is = new FileInputStream (address);

         String page = FileUtils.getText (is, "UTF8");

         Matcher matcher = TOPIC_PATTERN.matcher (page);
         while (matcher.find())
         {
            String id    = matcher.group (1);
            String title = matcher.group (2);
            int replies = Integer.parseInt (matcher.group (3).replaceAll (",", ""));
            int views   = Integer.parseInt (matcher.group (4).replaceAll (",", ""));
            topics.add (new Topic (id, title, replies, views));
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
         FileUtils.close (is);
      }
      
      return true;
   }
   
   public static void main (final String[] args) throws Exception
   {
      TopicCounter counter = new TopicCounter();
      
      String url = "http://forums.lotro.com/forumdisplay.php?f=122&order=desc&page=";
      for (int page = 1; page <= 110; page++)
      {
         System.out.print (page + ".");
         counter.scrape (url + page);
      }
      System.out.println();
      // String url = "C:/Documents and Settings/jkeller/Desktop/test.html";
      // counter.scrape (url);

      Collections.sort (counter.topics);
      for (Topic topic : counter.topics)
         if (topic.replies >= 20)
            System.out.println (topic);
      System.out.println();
   }
}
