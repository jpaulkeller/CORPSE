package lotro.web;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.NumericMap;
import web.GoogleChart;
import file.FileUtils;
import gui.Gradient;
import gui.TranslucentColor;

public final class ServerActivity
{
   private static final SimpleDateFormat MDY = new SimpleDateFormat("MM-dd-yyyy");
   private static final SimpleDateFormat MON_D_Y = new SimpleDateFormat("MMM dd yyyy");

   private Map<String, String> servers = new TreeMap<String, String>(); // name to link
   private Map<String, Integer> pages = new TreeMap<String, Integer>(); // name to max pages
   private NumericMap<String, Integer> byServer = new NumericMap<String, Integer>(Integer.class); // name to count

   private ServerActivity()
   {
      /* prevent instantiation */
   }

   // <h2 class="forumtitle"><a href="forumdisplay.php?81-Landroval-EN-RE">Landroval [EN-RE]</a></h2>
   private static final Pattern SERVER_PATTERN = 
      Pattern.compile("<h2 class=\"forumtitle\"><a href=\"([^\"]+)\">([A-Za-z]+)[^<]*</a></h2>", 
                      Pattern.DOTALL | Pattern.MULTILINE);

   private static final Pattern PVMP_PATTERN = 
      Pattern.compile("<h2 class=\"forumtitle\"><a href=\"([^\"]+?)\">(.*?)(?: - )?(?:PvMP|Monsterspiel|Joueur monstre)[^<]*</a></h2>");

   public void getServers(final String address, final Pattern pattern)
   {
      System.out.println("getServers: " + address);

      InputStream is = null;
      try
      {
         if (address.startsWith("http"))
            // is = new URL(address).openStream();
            is = BypassSSL.openStream(address);
         else
            is = new FileInputStream(address);

         String page = FileUtils.getText(is, "UTF8");
         // FileUtils.writeFile(new File("C:/Users/J/Desktop/lotro.html"), page, false);

         Matcher matcher = pattern.matcher(page);
         while (matcher.find())
         {
            String serverName = matcher.group(2);
            // System.out.println(" > " + serverName + " -> " + matcher.group(1));
            if (servers.get(serverName) == null)
            {
               String serverLink = "https://www.lotro.com/en/forums/" + matcher.group(1);
               servers.put(serverName, serverLink);
            }
         }
      }
      catch (MalformedURLException x)
      {
         System.err.println(x + ": " + address);
      }
      catch (IOException x)
      {
         System.err.println(x + ": " + address);
      }
      finally
      {
         FileUtils.close(is);
      }
   }

   private static final Pattern PAGE_PATTERN = Pattern.compile(">Page [0-9]+ of ([0-9]+)<");

   public void getPages(final String serverName)
   {
      String url = servers.get(serverName);
      InputStream is = null;
      try
      {
         // is = new URL(url).openStream();
         is = BypassSSL.openStream(url);
         String page = FileUtils.getText(is, "UTF8");

         Matcher matcher = PAGE_PATTERN.matcher(page);
         if (matcher.find())
         {
            String maxPage = matcher.group(1);
            pages.put(serverName, Integer.parseInt(maxPage));
         }
         else
         {
            System.out.println("Page pattern not found: " + url);
            pages.put(serverName, 1);
            FileUtils.writeFile(new File("C:/Users/J/Desktop/lotro.html"), page, false);
            System.exit(0); // TODO
         }

         System.out.println(" > getPages: " + serverName + " " + url + ": " + pages.get(serverName));

      }
      catch (MalformedURLException x)
      {
         System.err.println(x + ": " + url);
      }
      catch (IOException x)
      {
         System.err.println(x + ": " + url);
      }
      finally
      {
         FileUtils.close(is);
      }
   }

   public void scrapeServer(final String serverName, final Date firstDate)
   {
      Integer maxPage = pages.get(serverName);
      System.out.println("scrapeServer: " + serverName + "  Pages = " + maxPage);
      if (maxPage != null)
         for (int page = 1; page <= maxPage; page++)
            if (scrapePage(serverName, page, firstDate))
               break;
      System.out.println("scrapeServer: " + serverName + " = " + byServer.get(serverName));
   }

   // <dl class="threadlastpost td">...<dd>May 13 2012 <span class=\"time\">04:11 PM</span>"
   // <dl class="threadlastpost td">...<dd>Today, <span class="time">01:04 PM</span>

   private static final Pattern LAST_POST_PATTERN = 
      Pattern.compile("<dl class=\"threadlastpost td\">.*?<dd>([^<,]+), <span", Pattern.DOTALL | Pattern.MULTILINE);

   private boolean scrapePage(final String serverName, final int page, final Date firstDate)
   {
      boolean done = false;

      String url = servers.get(serverName);
      if (page > 1)
         url += "&order=desc&page=" + page;
      System.out.println("   scrapePage: " + url);

      InputStream is = null;
      try
      {
         // is = new URL(url).openStream();
         is = BypassSSL.openStream(url);
         String html = FileUtils.getText(is, "UTF8");

         int count = 0;
         Matcher matcher = LAST_POST_PATTERN.matcher(html);

         /*
         if (!matcher.find())
         {
            System.out.println("Last Post not found: " + url);
            FileUtils.writeFile(new File("C:/Users/J/Desktop/lotro.html"), html, false);
            System.exit(0);
         }
         */
         
         while (matcher.find())
         {
            String mdy = matcher.group(1);
            Date date = null;

            try
            {
               if (mdy.equals("Today"))
                  date = new Date();
               else if (mdy.equals("Yesterday"))
                  date = new Date(); // should -1, but close enough for now
               else
                  date = MON_D_Y.parse(mdy);
               if (date.before(firstDate) && page > 1) // don't abort on first page to avoid old sticky threads
               {
                  done = true;
                  break;
               }
            }
            catch (ParseException x)
            {
               System.err.println("Invalid date: " + mdy);
            }

            if (date != null)
               count++;
         }

         if (count > 0)
            byServer.plus(serverName, count);
      }
      catch (MalformedURLException x)
      {
         System.err.println(x + ": " + url);
      }
      catch (IOException x)
      {
         System.err.println(x + ": " + url);
      }
      finally
      {
         FileUtils.close(is);
      }

      return done;
   }

   public static void main(final String[] args) throws Exception
   {
      String firstMDY = "07-01-2013";
      Date firstDate = MDY.parse(firstMDY);

      ServerActivity app = new ServerActivity();

      String url = "https://www.lotro.com/en/forums/forumdisplay.php?77-Servers";

      String title = "Threads Updated Since " + firstMDY + " (by server)";
      app.getServers(url, SERVER_PATTERN); // server activity
      // String title = "PvMP Threads Updated Since " + firstMDY + " (by server)";
      // app.getServers(url, PVMP_PATTERN); // optional for PvMP (updates the server links)
      System.out.println("Servers found: " + app.servers.size());

      for (String serverName : app.servers.keySet())
         app.getPages(serverName);
      for (String serverName : app.servers.keySet())
         app.scrapeServer(serverName, firstDate);

      /*
       * test 1 String serverName = "Landroval-EN-RE"; app.servers.put(serverName,
       * "http://forums.lotro.com/forumdisplay.php?81-Landroval-EN-RE"); app.pages.put(serverName, 6);
       * app.scrapeServer(serverName, firstDate);
       */

      // --------------------------------------------------------------------

      System.out.println("Servers processed: " + app.byServer.size());
      if (app.byServer.size() > 1)
      {
         GoogleChart chart = new GoogleChart(title, GoogleChart.ChartType.Pie);
         chart.put(GoogleChart.ChartProp.Width, "600");
         chart.put(GoogleChart.ChartProp.Height, "400");

         Color[] gradient = Gradient.createMultiGradient(new Color[] { new Color(181, 32, 255), Color.blue, Color.green,
                  Color.yellow, Color.red }, app.byServer.size());

         int index = 0;
         for (String server : app.byServer.keySet())
         {
            String color = TranslucentColor.toHex(gradient[index++], "");
            chart.addValue(server, app.byServer.get(server), color);
         }

         if (chart.size() > 0)
            chart.show();
      }
   }
}
