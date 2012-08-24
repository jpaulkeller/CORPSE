package lotro.my.reports;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

import model.NumericMap;
import web.Firewall;
import web.GoogleChart;
import web.GoogleChart.ChartType;

public final class PvMP
{
   private static final String ENCODING = "UTF8";
   
   private GoogleChart chart;
   
   private NumericMap<String, Integer> classMap =
      new  NumericMap<String, Integer>(Integer.class);
   
   public PvMP()
   {
      String url;
      // for (int page = 1; page <= 5; page++)
      {
         // url = "http://my.lotro.com/home/leaderboard/pvmp?lp[sf]=renown&lp[sd]=DESC&lp[f][wd]=5&lp[pp]=" + page;
         url = "http://my.lotro.com/home/leaderboard/pvmp?lp[f][wd]=5&lp[f][kt]=The+Palantiri&lp[sf]=rating&lp[sd]=DESC";
         try
         {
            CharSequence text = scrape (url);
            parsePage (text);
         }
         catch (IOException x)
         {
            x.printStackTrace();
         }
      }
      
      chartMap ("Top 100 Renown By Class", classMap);
   }
   
   public static CharSequence scrape (final String path) throws IOException
   {
      StringBuilder response = new StringBuilder();
      
      BufferedReader buf = null;
      try
      {
         // Firewall.defineProxy();
         
         InputStream is;
         if (path.startsWith ("http://"))
            is = new URL (path).openStream();
         else
            is = new FileInputStream (path);
         InputStreamReader isr = new InputStreamReader (is, ENCODING);
         buf = new BufferedReader (isr);

         String line;
         while ((line = buf.readLine()) != null)
            response.append (line);
      }
      catch (MalformedURLException x)
      {
         x.printStackTrace (System.err);
      }
      finally
      {
         FileUtils.close (buf);
      }
      
      return response;
   }

   /*
   <a class="class" href="http://lorebook.lotro.com/wiki/Class:Hunter">
   <img src="http://content.turbine.com/sites/playerportal/modules/lotro-base/images/icons/class/hunter.png" />
   </a>
   <a href="/home/character/5/146929937843041923">
   Budhorn
   </a></td>
     <td class="rank"><img src="http://content.turbine.com/sites/playerportal/modules/lotro-base/images/icons/rank/freep_rank_14.png" title="Rank 14" /></td>
     <td class="kinship">The Gank Squad</td>
     <td class="renown">2562419</td>
     <td class="rating">1306</td>
   </tr>
   */
   
   private final static Pattern CLASS =
      Pattern.compile ("<a class=\"class\" href=\"http://lorebook.lotro.com/wiki/Class:([-A-Za-z]+)\">");
   private final static Pattern RENOWN =
      Pattern.compile ("<td class=\"renown\">([0-9]+)</td>");

   void parsePage(final CharSequence page)
   {
      int i = 0;
      Matcher m = CLASS.matcher (page);
      while (m.find())
      {
         String klass = m.group (1);
         System.out.println ((i++) + ") Class: " + klass);
         classMap.plus (klass, 1);
      }
   }
   
   void chartMap(final String title, final NumericMap<String, Integer> map)
   {
      /*
      chart = new GoogleChart (title, ChartType.BarVerticalGrouped);
      for (Entry<String, Integer> entry : map.entrySet ())
         chart.addValue(entry.getKey(), entry.getValue() / 20);
      chart.setColorsUsingGradient();
      chart.show();
      */
      
      chart = new GoogleChart (title, ChartType.Pie);
      for (Entry<String, Integer> entry : map.entrySet ())
      {
         System.out.println (entry.getKey() + " - " + entry.getValue());
         chart.addValue(entry.getKey(), entry.getValue());
      }
      chart.setColorsUsingGradient();
      chart.show();
   }
   
   public static void main (final String[] args) throws Exception
   {
      PvMP pvmp = new PvMP();
   }
}
