package lotro.web;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotro.models.Deed;
import model.NumericMap;
import web.GoogleChart;
import file.FileUtils;
import gui.Gradient;
import gui.TranslucentColor;

public final class DeedChart
{
   // private List<Deed> deeds = new ArrayList<Deed>();

   private NumericMap<String, Integer> byType = new NumericMap<String, Integer> (Integer.class); // type to count
   
   public DeedChart()
   {
   }

   /*
   <th>Required</th></tr></thead>
   <tr><td class="dr"><a href="/db/deeds.html?lotrdeed=708">Ally of the Council of the North</a></td>
   <td class="dr nobr"><a href="/db/geography.html?lotrzone=10">Angmar</a></td>
   <td class="dr nobr">Reputation</td>
   
   <tr><td class="lr"><a href="/db/deeds.html?lotrdeed=694">Ally to the Eldgang</a></td>
   <td class="lr nobr"><a href="/db/geography.html?lotrzone=10">Angmar</a></td>
   <td class="lr nobr">Reputation<>
   
    <td class="lr"><b>Deed:</b> <a href="/db/skills.html?lotrdeed=46">Wight-slayer</a> </td></tr><tr><td class="dr"><a href="/db/deeds.html?lotrdeed=416">A Keen Blade</a></td>
    <td class="dr nobr">Class</td>
    <td class="dr nobr">Skill</td>
   */

   private static final Pattern DEED_PATTERN = Pattern.compile(
         "<tr>.*?<a href=\"/db/deeds.html[?]lotrdeed=[0-9]+\">([^<]+)</a>.*?"
               + "<td class=\"[dl]r nobr\">(.+?)</td>.*?" // category, mostly region
               + "<td class=\"[dl]r nobr\">([^<]+)</td>", // type
         Pattern.DOTALL | Pattern.MULTILINE);

   private static final Pattern ZONE_PATTERN = Pattern.compile("<a href=\"/db/geography.html[?]lotrzone=[0-9]+\">([^<]+)</a>.*?");

   public void loadDeeds(final String address) 
   {
      System.out.println("loadDeeds: " + address);

      InputStream is = null;
      try 
      {
         if (address.startsWith("http"))
            is = new URL(address).openStream();
         else
            is = new FileInputStream(address);

         String page = FileUtils.getText(is, "UTF8");

         Matcher matcher = DEED_PATTERN.matcher(page);
         while (matcher.find()) 
         {
            String name = matcher.group(1);
            String category = matcher.group(2);
            String type = matcher.group(3);
            String trait = "";
            int level = 0;

            String region = category;
            Matcher m2 = ZONE_PATTERN.matcher(category);
            if (m2.matches())
               region = m2.group(1);
            
            else if (category.equals("Dunland")
                  || category.equals("Dunlending")
                  || category.equals("Endewaith") // sic
                  || category.equals("Eregion")
                  || category.equals("Forochel")
                  || category.equals("Gap of Rohan")
                  || category.equals("Isengard")
                  || category.equals("Lothlorien")
                  || category.equals("Mirkwood")
                  || category.equals("Moria")
                  || category.equals("Moria Central Halls")
                  || category.equals("Moria Lower Deeps")
                  || category.equals("Moria Upper Levels")
                  || category.equals("Nan Curunir")
                  || category.equals("The Great River"))
               region = category;
            
            else if (category.equals("Ettenmoors"))
               type = "PvMP";
            else if (category.equals("Hobbies"))
            {
               region = "n/a";
               type = "Hobbies";
            }
            else if (category.equals("Class"))
               region = "n/a";
            else if (category.equals("Epic"))
            {
               region = "n/a";
               type = "Meta-deed";
            }
            else if (category.equals("Race &amp; Social"))
               region = "n/a";
            else if (category.equals("Worldwide"))
               region = "n/a";
            else
               System.err.println("Unhandled Deed: " + name + "; " + category + "; " + type);
               
            // if (category.equals("Class")) + Required column TODO
            
            Deed deed = new Deed(region, name, type, trait, level);
            byType.plus(deed.getType(), 1);
            System.out.println(deed.getName() + " (in " + deed.getRegion() + ") - " + deed.getType());
         }
      } 
      catch (Exception x) 
      {
         System.err.println(x + ": " + address);
         x.printStackTrace();
      }
      finally 
      {
         FileUtils.close(is);
      }
   }

   public static void main(final String[] args) throws Exception
   {
      DeedChart app = new DeedChart();

      // String url = "http://lotro.allakhazam.com/db/deeds.html?mode=listall";
      String url = "C:/Users/J/Desktop/deeds.html";

      String title = "Deeds by Type";
      app.loadDeeds(url);
      
      // --------------------------------------------------------------------

      if (app.byType.size() > 1)
      {
         GoogleChart chart = new GoogleChart(title, GoogleChart.ChartType.Pie);
         chart.put(GoogleChart.ChartProp.Width, "600");
         chart.put(GoogleChart.ChartProp.Height, "400");
         
         Color[] gradient = Gradient.createMultiGradient(new Color[] {
               new Color(181, 32, 255), Color.blue, Color.green, Color.yellow, Color.red }, app.byType.size());
         
         int index = 0;
         for (String type : app.byType.keySet()) 
         {
            String color = TranslucentColor.toHex(gradient[index++], "");
            chart.addValue(type, app.byType.get(type), color);
         }
         
         if (chart.size() > 0)
            chart.show();
      }
   }
}
