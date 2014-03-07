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
   private static final SimpleDateFormat YYYY = new SimpleDateFormat ("yyyy");
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

      @Override
      public int compareTo (final Post o)
      {
         return toString().compareTo (o.toString());
      }
   }

   // pre 2013
   /*
   private static final Pattern POST_PATTERN = 
      Pattern.compile ("<tr>.*?" +
                       "<a href=\"showthread.php[?]&postid=(\\d+)[^>]*\">([^<]*)</a>.*?" +
                       "<div.*?([0-9]{2}-[0-9]{2}-[0-9]{4}) *<span class=\"time\">" +
                       ".*?</span><br>.*?by ([A-Za-z]+).*?</div>.*?" +
                       "<a href=\"forumdisplay.php[^>]*>([^<]+)</a>.*?</tr>",
                       Pattern.DOTALL | Pattern.MULTILINE);
                       */
   
   // groups: PostID, Topic, MDY, Dev, Forum
   private static final Pattern POST_PATTERN = 
      Pattern.compile ("<div class=\"trackerbit\">.*?" +
                       "<a href=\"showthread.php[?]&postid=(\\d+)[^>]*\">([^<]*)</a>.*?" + // Post and Topic
                       "([0-9]{2}-[0-9]{2}-[0-9]{4}) *<span class=\"time\">.*?" + // MDY
                       "by ([A-Za-z]+).*?</div>.*?" + // Dev
                       "<a href=\"forumdisplay.php[^>]*>([^<]+)</a>", // Forum
                       Pattern.DOTALL | Pattern.MULTILINE);

   private List<Post> posts = new ArrayList<Post>();
   private NumericMap<String, Integer> byDev = new NumericMap<String, Integer> (Integer.class);
   private NumericMap<String, Integer> byDevActive = new NumericMap<String, Integer> (Integer.class);
   private NumericMap<String, Integer> byForum = new NumericMap<String, Integer> (Integer.class);
   private NumericMap<String, Integer> byTopic = new NumericMap<String, Integer> (Integer.class);
   private NumericMap<String, Integer> byMonth = new NumericMap<String, Integer> (Integer.class);
   private NumericMap<String, Integer> byYear = new NumericMap<String, Integer> (Integer.class);
   private CollectionMap<String, Post> suggestionsByTopic = new CollectionMap<String, Post>();

   private DevTracker()
   {
      // prevent instantiation
   }

   public boolean scrape (final String address,
                          final Date firstDate,
                          final Date lastDate)
   {
      System.out.println (address);
      
      InputStream is = null;
      try
      {
         if (address.startsWith ("http"))
         {
            // if (Login.login (owner, address))
            // is = new URL (address).openStream();
            is = BypassSSL.openStream(address);
         }
         else
            is = new FileInputStream (address);

         String page = FileUtils.getText (is, "UTF8");
         // System.out.println(page); //TODO
         
         Date prevDate = null;
         
         Matcher matcher = POST_PATTERN.matcher (page);
         while (matcher.find())
         {
            String id    = matcher.group (1); // post ID 
            String topic = matcher.group (2);
            String mdy   = matcher.group (3);
            String dev   = matcher.group (4);
            String forum = matcher.group (5);
            Date date = null;
            
            /*
            System.out.println("ID    = [" + id + "]"); 
            System.out.println("Topic = [" + topic + "]");
            System.out.println("Date  = [" + mdy + "]");
            System.out.println("Dev   = [" + dev + "]");
            System.out.println("Forum = [" + forum + "]");
            */

            try
            {
               date = MDY.parse (mdy);
               if (!date.equals (prevDate))
               {
                  System.out.println (" > " + date);
                  prevDate = date;
               }
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
         System.err.println (x + ": " + address);
      }
      catch (IOException x)
      {
         System.err.println (x + ": " + address);
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
         byYear.plus (YYYY.format (post.date), 1);
         if (post.forum.equals ("Suggestions"))
            suggestionsByTopic.putElement (post.topic, post);
      }
      
      for (String dev : byDev.keySet())
      {
         int count = byDev.getInt (dev);
         if (count >= MIN_POSTS)
            byDevActive.put (dev, count);
      }
      
      if (!wordCount.isEmpty())
      {
         String path = FileUtils.MY_DESK + File.separator + "WordCount.txt";
         writeMap (wordCount, path, false);
      }
      
      organizeByTopic();
   }

   private void organizeByTopic()
   {
      for (String forum : byForum.keySet())
      {
         if (forum.contains("Announcements") ||
             forum.contains("Official") ||
             forum.contains("Store"))
            byTopic.plus("Official", byForum.getInt(forum));
         
         else if (forum.contains("General"))
            byTopic.plus("General", byForum.getInt(forum));
         
         else if (forum.contains("Suggestions"))
            byTopic.plus("Suggestions", byForum.getInt(forum));
         
         else if (forum.contains("Community") ||
               forum.contains("Feedback") ||
               forum.contains("Fansite") ||
               forum.contains("Postcards") ||
               forum.contains("Hall of Fame") ||
               forum.contains("Events") ||
               forum.contains("Tolkien") ||
               forum.contains("Off-Topic"))
            byTopic.plus("Community", byForum.getInt(forum));
         
         else if (forum.contains("Quests") ||
               forum.contains("Instances") ||
               forum.contains("Legendary Items") ||
               forum.contains("War-steeds") ||
               forum.contains("Mounted Combat") ||
               
               forum.contains("Crafting") ||
               forum.equals("Cook") ||
               forum.equals("Farmer") ||
               forum.equals("Forester") ||
               forum.equals("Jeweller") ||
               forum.equals("Metalsmith") ||
               forum.equals("Propector") ||
               forum.equals("Scholar") ||
               forum.equals("Tailor") ||
               forum.equals("Weaponsmith") ||
               forum.equals("Woodworker") ||
               
               forum.contains("Races") ||
               forum.equals("Burglars") ||
               forum.equals("Captains") ||
               forum.equals("Champions") ||
               forum.equals("Guardians") ||
               forum.equals("Hunters") ||
               forum.equals("Lore-masters") ||
               forum.equals("Minstrels") ||
               forum.equals("Rune-keepers") ||
               forum.equals("Wardens") ||
               
               forum.contains("Housing") ||
               forum.contains("Kinships") ||
               forum.contains("Cosmetics") ||
               forum.contains("Roleplaying") ||
               forum.contains("Music") ||
               
               forum.contains("Scripting") ||
               forum.contains("API") ||
               forum.contains("Plugins") ||
               
               forum.contains("User Interface") ||
               
               forum.contains("Monster play") ||
               forum.equals("Creeps") ||
               forum.equals("Freeps"))
            
            byTopic.plus("Game Play and Systems", byForum.getInt(forum));
         
         else if (forum.contains("Deutsch") ||
               forum.contains("Français") ||
               forum.contains("[DE") ||
               forum.contains("[FR"))
            byTopic.plus("Non-English", byForum.getInt(forum));
         
         else if (forum.contains("Arkenstone") ||
               forum.contains("Brandywine") ||
               forum.contains("Crickhollow") ||
               forum.contains("Dwarrowdelf") ||
               forum.contains("Eldar") ||
               forum.contains("Elendilmir") ||
               forum.contains("Evernight") ||
               forum.contains("Firefoot") ||
               forum.contains("Gilrain") ||
               forum.contains("Gladden") ||
               forum.contains("Imladris") ||
               forum.contains("Landroval") ||
               forum.contains("Laurelin") ||
               forum.contains("Meneldor") ||
               forum.contains("Nimrodel") ||
               forum.contains("Riddermark") ||
               forum.contains("Silverlode") ||
               forum.contains("Snowbourn") ||
               forum.contains("Vilya") ||
               forum.contains("Windfola") ||
               forum.contains("Withywindle") ||
               forum.contains("Bullroarer"))
            byTopic.plus("Servers", byForum.getInt(forum));
         
         else if (forum.contains("Support") ||
               forum.contains("Technical") ||
               forum.contains("Knowledge") ||
               forum.contains("Assistance"))
            byTopic.plus("Customer Support", byForum.getInt(forum));
         
         else
         {
            System.out.println("Forum: " + forum);
            byTopic.plus("Other", byForum.getInt(forum));
         }
      }
      
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
   
   /*
   private static void setupCookies()
   {
      HttpClient client = new HttpClient();
      client.getHostConfiguration().setHost (HOST, 80, "http");
      client.getParams().setCookiePolicy (CookiePolicy.RFC_2109);
      // client.getParams().setCookiePolicy (CookiePolicy.NETSCAPE);
      // client.getParams().setCookiePolicy (CookiePolicy.BROWSER_COMPATIBILITY);

      PostMethod post = new PostMethod ("/login/login2.asp");
      NameValuePair action   = new NameValuePair ("submit", "Login");
      NameValuePair userid   = new NameValuePair ("email", "jpaulkeller@comcast.net");
      NameValuePair password = new NameValuePair ("password", "nagshead");
      post.setRequestBody (new NameValuePair[] { action, userid, password });

      execute (client, post);
      showCookies (client);
   }
   
   private static void showCookies (final HttpClient client)
   {
      Cookie[] cookies = client.getState().getCookies();
      System.out.println("Cookies: ");
      for (int i = 0; i < cookies.length; i++)
         System.out.println(" - " + cookies[i].toExternalForm());
   }

   private static String execute (final HttpClient client, final HttpMethod method)
   {
      String response = null;

      try
      {
         method.setDoAuthentication (true);
         // method.setFollowRedirects (true);

         int statusCode = client.executeMethod (method);
         if (statusCode != HttpStatus.SC_OK)
            System.err.println ("Method failed: " + method.getStatusLine());

         // Read the response body.
         // Use caution: ensure correct character encoding and is not binary data
         response = method.getResponseBodyAsString();
       }
       catch (HttpException e)
       {
          System.err.println ("Fatal protocol violation: " + e.getMessage());
          e.printStackTrace();
       }
       catch (IOException e)
       {
          System.err.println ("Fatal transport error: " + e.getMessage());
          e.printStackTrace();
       }
       finally
       {
          method.releaseConnection();
       }

       return response;
   }
   */
   
   public static void main (final String[] args) throws Exception
   {
      String firstMDY = "01-01-2013"; // TODO
      String lastMDY = "12-31-2013";
      Date firstDate = MDY.parse (firstMDY);
      Date lastDate = MDY.parse (lastMDY);

      DevTracker tracker = new DevTracker();
      
      // String url = "http://forums.lotro.com/turbine_tracker.php?tracker=devT&pp=25&page=";
      String url = "https://www.lotro.com/forums/post_tracker.php?tracker=devtracker&searchid=&pp=25&page=";
      int page = 1;
      while (tracker.scrape (url + page, firstDate, lastDate))
      {
         page++;
         // if (page >= 3) break;
      }
      tracker.organize();
      
      String dates = "(" + firstMDY + " to " + lastMDY + ")"; 

      // --------------------------------------------------------------------
      
      GoogleChart chart = new GoogleChart ("Dev Posts In Class Forums " + dates, GoogleChart.ChartType.Pie);
      chart.put (GoogleChart.ChartProp.Width, "600");
      chart.put (GoogleChart.ChartProp.Height, "400");

      int pvp = 0;
      for (String forum : tracker.byForum.keySet())
      {
         Klass klass = Klass.parse (forum);
         if (klass != Klass.Unknown && klass != Klass.None)
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
      
      chart = new GoogleChart ("Dev Posts By Topic " + dates, GoogleChart.ChartType.BarHorizontalGrouped);
      chart.put (GoogleChart.ChartProp.Width, "600");
      chart.put (GoogleChart.ChartProp.Height, "400");

      Color[] gradient = Gradient.createMultiGradient (new Color[] {
            new Color (181, 32, 255), Color.blue, Color.green, Color.yellow },
            tracker.byTopic.size());

      int index = 0;
      for (String topic : tracker.byTopic.keySet())
      {
         String color = TranslucentColor.toHex (gradient [index++], "");
         chart.addValue (topic, tracker.byTopic.getInt (topic), color);
      }
      
      if (chart.size() > 0)
         chart.show();
      System.out.println();
      
      // --------------------------------------------------------------------
      
      chart = new GoogleChart ("Dev Posts " + dates, GoogleChart.ChartType.Pie);
      chart.put (GoogleChart.ChartProp.Width, "600");
      chart.put (GoogleChart.ChartProp.Height, "400");
      
      gradient = Gradient.createMultiGradient (new Color[] {
            new Color (181, 32, 255), Color.blue, Color.green, Color.yellow },
            tracker.byDev.size());

      index = 0;
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
      
      chart = new GoogleChart (MIN_POSTS + "%2B Dev Posts " + dates, GoogleChart.ChartType.Pie);
      chart.put (GoogleChart.ChartProp.Width, "600");
      chart.put (GoogleChart.ChartProp.Height, "400");
      
      gradient = Gradient.createMultiGradient (new Color[] {
               new Color (181, 32, 255), Color.blue, Color.green, Color.yellow },
               tracker.byDevActive.size());

      index = 0;
      for (String dev : tracker.byDevActive.keySet())
      {
         String color = TranslucentColor.toHex (gradient [index++], "");
         // if (dev.equals ("Zombie")) color = "FF3333";
         chart.addValue (dev, tracker.byDevActive.getInt (dev), color);
      }
      if (chart.size() > 0)
         chart.show();
      System.out.println();
      
      // --------------------------------------------------------------------
      
      chart = new GoogleChart ("Dev Posts By Month " + dates, GoogleChart.ChartType.BarHorizontalGrouped);
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
      
      chart = new GoogleChart ("Dev Posts By Year " + dates, GoogleChart.ChartType.BarHorizontalGrouped);
      chart.put (GoogleChart.ChartProp.Width, "600");
      chart.put (GoogleChart.ChartProp.Height, "400");
      
      gradient = new Color[] { new Color (181, 32, 255), Color.blue, Color.green, Color.yellow };
      if (tracker.byYear.size() > 1)
        gradient = Gradient.createMultiGradient (gradient, tracker.byYear.size());

      index = 0;
      for (String year : tracker.byYear.keySet())
      {
         Date date = YYYY.parse (year);
         String label = YYYY.format (date);
         String color = TranslucentColor.toHex (gradient [index++], "");
         chart.addValue (label, tracker.byYear.getInt (year), color);
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
