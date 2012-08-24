package lotro.web;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.NumericMap;
import web.GoogleChart;
import file.FileUtils;
import gui.Gradient;
import gui.TranslucentColor;

public final class DeedChartDiablo
{
   private NumericMap<String, Integer> byType = new NumericMap<String, Integer> (Integer.class); // type to count
   
   public DeedChartDiablo()
   {
   }

   // name, requirement
   private static final Pattern DEED_PATTERN = Pattern.compile("\t([^\t]+)\t(.+)");

   private static final Pattern RQMT_PATTERN = Pattern.compile(" *([^ ]+).*");
         
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

         NumericMap<String, Integer> byWord = new NumericMap<String, Integer> (Integer.class); // first-word to count
         
         Matcher matcher = DEED_PATTERN.matcher(page);
         while (matcher.find()) 
         {
            String name = matcher.group(1);
            String rqmt = matcher.group(2);
                  
            Matcher m2 = RQMT_PATTERN.matcher(rqmt);
            if (m2.matches())
            {
               String word = m2.group(1); 
               System.out.println(name + " -- " + word);
               byWord.plus(word, 1);
            }
            else
               System.err.println("Bad requirement: " + rqmt);
         }
         
         // pack the data
         for (String word : byWord.keySet())
         {
            int count = byWord.get(word);
            if (count >= 3)
               byType.put(word, count);
            else
               byType.plus("Other", 1);
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
      DeedChartDiablo app = new DeedChartDiablo();

      // from "http://d3db.com/achievement";
      String url = "C:/Users/J/Desktop/diablo.htm";

      String title = "Diablo 3 Deeds by Type";
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
