package lotro.web;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotro.models.Klass;
import model.CollectionMap;
import model.NumericMap;
import web.GoogleChart;
import file.FileUtils;
import gui.Gradient;
import gui.TranslucentColor;

public final class DevTracker
{
   private static final SimpleDateFormat MDY = new SimpleDateFormat ("MM-dd-yyyy");
   private static final SimpleDateFormat YYYYMM = new SimpleDateFormat ("yyyy-MM");
   private static final SimpleDateFormat MONTH = new SimpleDateFormat ("MMM");
   
   private static final int MIN_POSTS = 10;

   /*
    * <tr> <td nowrap class="alt2"> <a
    * href="showthread.php?goto=newpost&t=265193"><img
    * src="http://images.lorforum.turbine.com/images/buttons/firstnew.gif"
    * border="0" alt="Go to first new post" align="absmiddle"></a> <a
    * href="showthread.php?goto=lastpost&t=265193"><img
    * src="http://images.lorforum.turbine.com/images/buttons/lastpost.gif"
    * border="0" alt="Go to last post" align="absmiddle"></a>
    * 
    * </td> <td class="alt1"> <a
    * href="showthread.php?&postid=3657458#post3657458">Welcome Back Week
    * Ends!</a> </td> <td class="alt2"> <table cellpadding="0" cellspacing="0"
    * border="0" id="ltlink"> <tr align="right"> <td>
    * 
    * <div class="smallfont" style="text-align:right; white-space:nowrap">
    * 05-01-2009 <span class="time">11:29 AM</span><br> by Patience </div> </td>
    * <td nowrap>&nbsp;</td> </tr> </table>
    * 
    * </td> <td class="alt1"> <a href="forumdisplay.php?f=3">Announcements</a>
    * </td> </tr>
    */

   class Post implements Comparable<Post>
   {
      private String dev; // author
      private Date date;
      private String forum;
      private String topic;
      private String id;

      public Post (final String dev, final Date date,
                   final String forum, final String topic, final String id)
      {
         this.dev = dev;
         this.date = date;
         this.forum = forum;
         this.topic = topic;
         this.id = id;
      }
      
      @Override
      public String toString()
      {
         return YYYYMM.format (date) + " by " + dev + " in " + forum + ": " + topic;
      }

      public int compareTo (final Post o)
      {
         return toString().compareTo (o.toString());
      }
   }

   private static final Pattern POST_PATTERN = 
      Pattern.compile ("<tr>.*?" +
                       "<a href=\"showthread.php[?]&postid=(\\d+)[^>]*\">([^<]*)</a>.*?" +
                       "<div.*?([0-9]{2}-[0-9]{2}-[0-9]{4}) *<span class=\"time\">" +
                       ".*?</span><br>.*?by ([A-Za-z]+).*?</div>.*?" +
                       "<a href=\"forumdisplay.php[^>]*>([^<]+)</a>.*?</tr>",
                       Pattern.DOTALL | Pattern.MULTILINE);

   private List<Post> posts = new ArrayList<Post>();
   private NumericMap<String, Integer> byDev = new NumericMap<String, Integer> (Integer.class);
   private NumericMap<String, Integer> byDevActive = new NumericMap<String, Integer> (Integer.class);
   private NumericMap<String, Integer> byForum = new NumericMap<String, Integer> (Integer.class);
   private NumericMap<String, Integer> byMonth = new NumericMap<String, Integer> (Integer.class);
   private CollectionMap<String, Post> suggestionsByTopic = new CollectionMap<String, Post>();

   private DevTracker()
   {
      /* prevent instantiation */
   }

   public boolean scrape (final String address,
                          final Date firstDate,
                          final Date lastDate)
   {
      System.out.println (address);
      
      InputStream is = null;
      try
      {
         if (address.startsWith ("http://"))
            is = new URL (address).openStream();
         else
            is = new FileInputStream (address);

         String page = FileUtils.getText (is, "UTF8");

         Matcher matcher = POST_PATTERN.matcher (page);
         while (matcher.find())
         {
            String id    = matcher.group (1); 
            String topic = matcher.group (2);
            String mdy   = matcher.group (3);
            String dev   = matcher.group (4);
            String forum = matcher.group (5);
            Date date = null;

            try
            {
               date = MDY.parse (mdy);
               if (date.before (firstDate))
                  return false;
            }
            catch (ParseException x) { }
            if (date != null && date.before (lastDate))
            {
               Post post = new Post (dev, date, forum, topic, id);
               // scrapePost(post);
               posts.add (post);
            }
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
   
   
   public boolean scrapePost (final Post post)
   {
      String address = "http://forums.lotro.com/showthread.php?&postid=" + 
                       post.id + "#post" + post.id;
      
      InputStream is = null;
      try
      {
         is = new URL (address).openStream();

         String page = FileUtils.getText (is, "UTF8");

         Pattern pattern = Pattern.compile
         ("<div id=\"post_message_" + post.id + "\">\\s*<blockquote class=\"postcontent restore\">\\s*" +
          "(.*?)\\s*</blockquote>\\s*</div>", Pattern.DOTALL | Pattern.MULTILINE);
         Matcher matcher = pattern.matcher (page);
         if (matcher.find())
            processContent (post, matcher.group (1));
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

   private static final Pattern WORD = Pattern.compile("([a-z]+)");
   private static final NumericMap<String, Integer> wordCount = 
      new NumericMap<String, Integer>(Integer.class);
   
   private void processContent (final Post post, String content)
   {
      // strip out quotes
      content = content.replace ("\n", " ");
      content = content.replaceAll ("<!-- BEGIN TEMPLATE: bbcode_quote -->(.*?)" +
                                    "<!-- END TEMPLATE: bbcode_quote -->", "");
      Matcher m = WORD.matcher (content);
      while (m.find())
         wordCount.plus(m.group(1).toLowerCase(), 1);
   }
   
   private void organize()
   {
      for (Post post : posts)
      {
         System.out.println (post);
         byDev.plus (post.dev, 1);
         byForum.plus (post.forum, 1);
         byMonth.plus (YYYYMM.format (post.date), 1);
         if (post.forum.equals ("Suggestions"))
            suggestionsByTopic.putElement (post.topic, post);
      }
      
      for (String dev : byDev.keySet())
      {
         int count = byDev.getInt (dev);
         if (count >= MIN_POSTS)
            byDevActive.put (dev, count);
      }
      
      String path = FileUtils.MY_DESK + File.separator + "WordCount.txt";
      writeMap (wordCount, path, false);
   }

   public static void writeMap (final Map<String, Integer> map, final String path,
                                 final boolean append)
   {
      PrintStream out = null;
      try
      {
         FileUtils.makeDir (new File (path).getParent());
         FileOutputStream fos = new FileOutputStream (path, append); 
         out = new PrintStream (fos, true, FileUtils.UTF8); // support Unicode
         for (Entry<String, Integer> entry : map.entrySet())
            if (entry.getValue() > 20)
               out.println (entry.getKey() + "=" + entry.getValue());
         out.flush();
      }
      catch (IOException x)
      {
         x.printStackTrace (System.err);
      }
      finally
      {
         FileUtils.close (out);
      }
   }
   
   public static void main (final String[] args) throws Exception
   {
      String firstMDY = "01-01-2011";
      String lastMDY = "12-31-2011";
      Date firstDate = MDY.parse (firstMDY);
      Date lastDate = MDY.parse (lastMDY);

      DevTracker tracker = new DevTracker();
      
      String url = "http://forums.lotro.com/turbine_tracker.php?tracker=dev&pp=25&page=";
      int page = 1;  // start ~137 for 2008
      while (tracker.scrape (url + page, firstDate, lastDate))
         page++;
      tracker.organize();
      
      String dates = "(" + firstMDY + " to " + lastMDY + ")"; 

      // --------------------------------------------------------------------
      
      GoogleChart chart = new GoogleChart ("Dev Posts In Class Forums " + dates,
                                           GoogleChart.ChartType.Pie);
      chart.put (GoogleChart.ChartProp.Width, "600");
      chart.put (GoogleChart.ChartProp.Height, "400");

      int pvp = 0;
      for (String forum : tracker.byForum.keySet())
      {
         Klass klass = Klass.parse (forum);
         if (klass != Klass.Unknown)
            chart.addValue (forum, tracker.byForum.getInt (forum), klass.getColorBG (""));
         else if (forum.equalsIgnoreCase ("Monster play") ||         
                  forum.equalsIgnoreCase ("Freeps") ||         
                  forum.equalsIgnoreCase ("Creeps"))
            pvp += tracker.byForum.getInt (forum);
      }
      if (pvp > 0)
         chart.addValue ("PvMP", pvp, "FFFF00"); // yellow
      if (chart.size() > 0)
         chart.show();
      System.out.println();
      
      // --------------------------------------------------------------------
      
      chart = new GoogleChart ("Dev Posts " + dates,
                               GoogleChart.ChartType.Pie);
      chart.put (GoogleChart.ChartProp.Width, "600");
      chart.put (GoogleChart.ChartProp.Height, "400");
      
      Color[] gradient = Gradient.createMultiGradient (new Color[] {
               new Color (181, 32, 255), Color.blue, Color.green, Color.yellow },
               tracker.byDev.size());

      int index = 0;
      for (String dev : tracker.byDev.keySet())
      {
         String color = TranslucentColor.toHex (gradient [index++], "");
         chart.addValue (dev, tracker.byDev.getInt (dev), color);
      }
      if (chart.size() > 0)
         chart.show();
      System.out.println();
      
      // --------------------------------------------------------------------
      // more than MIN_POSTS only
      
      chart = new GoogleChart (MIN_POSTS + "%2B Dev Posts " + dates,
                               GoogleChart.ChartType.Pie);
      chart.put (GoogleChart.ChartProp.Width, "600");
      chart.put (GoogleChart.ChartProp.Height, "400");
      
      gradient = Gradient.createMultiGradient (new Color[] {
               new Color (181, 32, 255), Color.blue, Color.green, Color.yellow },
               tracker.byDevActive.size());

      index = 0;
      for (String dev : tracker.byDevActive.keySet())
      {
         String color = TranslucentColor.toHex (gradient [index++], "");
         if (dev.equals ("Zombie"))
            color = "FF3333";
         chart.addValue (dev, tracker.byDevActive.getInt (dev), color);
      }
      if (chart.size() > 0)
         chart.show();
      System.out.println();
      
      // --------------------------------------------------------------------
      
      chart = new GoogleChart ("Dev Posts By Month " + dates,
                               GoogleChart.ChartType.BarHorizontalGrouped);
      chart.put (GoogleChart.ChartProp.Width, "600");
      chart.put (GoogleChart.ChartProp.Height, "400");
      
      gradient = new Color[] { new Color (181, 32, 255), Color.blue, Color.green, Color.yellow };
      if (tracker.byMonth.size() > 1)
    	  gradient = Gradient.createMultiGradient (gradient, tracker.byMonth.size());

      index = 0;
      for (String month : tracker.byMonth.keySet())
      {
         Date date = YYYYMM.parse (month);
         String label = MONTH.format (date);
         String color = TranslucentColor.toHex (gradient [index++], "");
         chart.addValue (label, tracker.byMonth.getInt (month), color);
      }
      if (chart.size() > 0)
         chart.show();
      System.out.println();
      
      // --------------------------------------------------------------------
      /*
      String site = "http://forums.lotro.com/showthread.php?";
      for (String topic : tracker.suggestionsByTopic.keySet())
      {
         System.out.println (topic);
         System.out.println ("[LIST]");
         List<Post> inTopicByDate = tracker.suggestionsByTopic.get (topic); 
         Collections.sort (inTopicByDate);
         for (Post post : inTopicByDate)
            System.out.println ("[*][URL=" + site + "postid=" + post.id + 
                                "#post" + post.id + "]" + YYYYMM.format (post.date) + 
                                ": " + post.dev + "[/URL]");
         System.out.println ("[/LIST]");
      }
      */
   }
}