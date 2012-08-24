package web;

import gui.Gradient;
import gui.TranslucentColor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import model.NumericMapLinked;

public final class GoogleChart
{
   public enum ChartProp
   {
      AppendValuesToLabels,
      TitleColor,
      TitleSize,
      Width,
      Height;
   }
   
   public enum ChartType 
   {
      BarHorizontalGrouped ("bhg"),
      BarVerticalGrouped ("bvg"),
      Pie ("p"),
      Pie3D ("p3");

      private String code;
      
      private ChartType (final String code)
      {
         this.code = code;
      }
      
      public String getCode()
      {
         return code;
      }
   }
   
   private static final String CHART_URL = "http://chart.apis.google.com/chart";

   private String title;
   private String chartType;
   
   private NumericMapLinked<String, Integer> values;
   private List<String> colors;
   
   private Map<ChartProp, String> props = new TreeMap<ChartProp, String>(); 
   
   public GoogleChart (final String title, final ChartType type)
   {
      this.title = title;
      this.chartType = type.getCode();
      
      // set default properties
      put (ChartProp.TitleColor, "0000FF");
      put (ChartProp.TitleSize, "24");
      put (ChartProp.Width, "1000");
      put (ChartProp.Height, "300");
      put (ChartProp.AppendValuesToLabels, "true");
   }
   
   public String getTitle()
   {
      return title;
   }
   
   public void addValue (final String label, final Integer value, final String color)
   {
      if (value > 0)
      {
         if (colors == null)
            colors = new ArrayList<String>();
         colors.add (color);
         addValue (label, value);
      }
   }
   
   public void addValue (final String label, final Integer value)
   {
      if (values == null)
         values = new NumericMapLinked<String, Integer> (Integer.class);
      
      if ("true".equalsIgnoreCase (props.get (ChartProp.AppendValuesToLabels)))
         values.plus (label + " (" + value + ")", value);
      else
         values.plus (label, value);
   }
   
   public int size()
   {
      return values == null ? 0 : values.size();
   }
   
   public void put (final ChartProp key, final String value)
   {
      props.put (key, value);
   }
   
   public void setColorsUsingGradient()
   {
      Color[] gradient = Gradient.createMultiGradient (new Color[] {
               new Color (181, 32, 255), Color.blue, Color.green, Color.yellow,
               Color.orange, Color.red }, values.size());
      colors = new ArrayList<String>();
      for (Color color : gradient)
         colors.add (TranslucentColor.toHex (color, ""));
   }
   
   public String buildURL()
   {
      StringBuilder url = new StringBuilder (CHART_URL);
      String dataSeparator = ",";
      
      url.append ("?cht=" + chartType);
      
      if (chartType.contains ("b")) // bar
      {
         List<String> labels = new ArrayList<String>();
         int set = 0;
         for (String key : values.keySet())
         {
            String label = key; // + " (" + values.getInt (key) + ")";
            // type, color, set#, points, size
            labels.add ("t " + label + ",000000," + (set++) + ",-1,10");
         }
         appendList (url, "&chm=", labels, "|"); // labels at the end of each bar
      }
      else if (chartType.contains ("p")) // pie
         appendList (url, "&chl=", values.keySet(), "|"); // slice labels
      
      if (chartType.contains ("g")) // grouped
         dataSeparator = "|";
      else if (chartType.contains ("h")) // horizontal
         url.append ("&chxt=y"); // label the Y-axis
      else if (chartType.contains ("v")) // vertical
         url.append ("&chxt=x"); // label the X-axis
      
      String w = props.get (ChartProp.Width);
      String h = props.get (ChartProp.Height);
      url.append ("&chs=" + w + "x" + h); // size, max w*h is 300,000
      url.append ("&chtt=" + title.replaceAll (" ", "+"));
      url.append ("&chts=" + props.get (ChartProp.TitleColor) + "," +
                  props.get (ChartProp.TitleSize));
      
      if (colors != null)
         appendList (url, "&chco=", colors, ","); // colors (for each data set)
      
      double max = 0;
      for (Number value : values.values())
         if (value.doubleValue() > max)
            max = value.doubleValue();
      
      appendList (url, "&chd=t:", values.values(), dataSeparator); // data
      url.append ("&chds=0," + Math.round (max * 1.1)); // scaling limits

      // add Legend
      // appendList (url, "&chdl=", ranges.labels, "|"); // legend labels
      // url.append ("&chdlp=b"); // legend position (below)
      
      return url.toString();
   }
   
   public String getImageURL()
   {
      return "<img border=1 src=\"" + buildURL() + "\" alt=\"" + title + "\" />";
   }
   
   private void appendList (final StringBuilder url, final String prefix, 
                            final Collection<? extends Object> data, 
                            final String sepr)
   {
      Iterator<? extends Object> iter = data.iterator();
      if (iter.hasNext())
      {
         url.append (prefix);
         StringBuilder arg = new StringBuilder (iter.next().toString());
         while (iter.hasNext())
            arg.append (sepr + iter.next());
         url.append (HTMLUtils.encode (arg.toString()));
         /*
         url.append (iter.next());
         while (iter.hasNext())
            url.append (sepr + iter.next());
         */
      }
   }
   
   public void show()
   {
      String url = buildURL();
      System.out.println (title);
      System.out.println (url);
      System.out.println ("<img border=1 src=\"" + url + "\" alt=\"" + title + "\" />");
      System.out.println();
   }
   
   public static void main (final String[] args) throws Exception
   {
      GoogleChart chart;
      
      chart = new GoogleChart ("Dev Posts in Server Forums", ChartType.BarVerticalGrouped);
      
      chart.addValue ("Arkenstone", 22);
      chart.addValue ("Brandywine", 41);
      chart.addValue ("Elendilmir", 20);
      chart.addValue ("Firefoot", 4);
      chart.addValue ("Gladden", 2);
      chart.addValue ("Landroval", 37);
      chart.addValue ("Meneldor", 13);
      chart.addValue ("Nimrodel", 2);
      chart.addValue ("Silverlode", 5);
      chart.addValue ("Vilya", 6);
      chart.addValue ("Windfola", 1);
      
      chart.setColorsUsingGradient();
      chart.show();
      
      chart = new GoogleChart ("Dev Posts In Class Forums", ChartType.Pie);
      
      chart.addValue ("Burglars", 64, "000000"); // black
      chart.addValue ("Captains", 12, "00FFFF"); // cyan
      chart.addValue ("Champions", 23, "F88017"); // orange
      chart.addValue ("Guardians", 23, "CCCCCC"); // light grey
      chart.addValue ("Hunters", 10, "FF0000"); // red
      chart.addValue ("Lore-masters", 32, "0000FF"); // blue
      chart.addValue ("Minstrels", 39, "AAFFAA"); // light green
      chart.addValue ("Rune-keepers", 85, "FF00FF"); // magenta
      chart.addValue ("Wardens", 64, "AF7817"); // tan

      chart.show();
   }
   
   /*
   http://chart.apis.google.com/chart?
   cht=bhs&
   chs=1000x300&
   chtt=Palantiri+By+Class%20%28April%202009%29&
   chts=0000FF,24&
   chxt=y&
   chco=C6D9FD,89B1FB,4D89F9,AAFFAA&
   chxl=0:%7CWarden%20%2824%29%7CRuneKeeper%20%2824%29%7CMinstrel%20%2830%29%7CLoreMaster%20%2835%29%7CHunter%20%2849%29%7CGuardian%20%2836%29%7CChampion%20%2835%29%7CCaptain%20%2832%29%7CBurglar%20%2837%29%7C&
   chd=t:12,9,12,8,12,8,5,16,13%7C10,8,5,8,8,10,9,4,6%7C10,9,12,13,16,7,10,1,4%7C5,6,6,7,13,10,6,3,1&
   chm=N,000000,0,-1,10%7CN,000000,1,-1,10%7CN,000000,2,-1,10%7CN,000000,3,-1,10&
   chds=0,51&
   chdl=Level%201-30%7CLevel%2031-45%7CLevel%2046-59%7CLevel%2060%2B&
   chdlp=b
   */
}
