package lotro.my.reports;

import gui.form.Range;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lotro.models.Alliance;
import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Klass;
import lotro.my.xml.KinshipXML;
import model.NumericMap;

public final class Graphs
{
   private static final String CHART_URL = "http://chart.apis.google.com/chart";
   
   private Alliance alliance;
   private List<Klass> classes;
   
   public Graphs (final Kinship kinship)
   {
      this (new Alliance (kinship.getName()));
      alliance.addKinship (kinship);
   }
   
   public Graphs (final Alliance alliance)
   {
      this.alliance = alliance;
      classes = new ArrayList<Klass> (Klass.FREEPS);
      Collections.reverse (classes);
   }
   
   public String getBarChartByClass()
   {
      LevelRanges ranges = new LevelRanges();
      ranges.add ("C6D9FD", 01, 30); // very light blue
      ranges.add ("89B1FB", 31, 45); // light blue
      ranges.add ("4D89F9", 46, 65); // blue
      ranges.add ("AAFFAA", 66, 74); // light green
      ranges.add ("77EE77", 75, Integer.MAX_VALUE); // green 
      return getBarChartByClass2 (ranges);
   }
   
   private String getBarChartByClass2 (final LevelRanges ranges)
   {
      StringBuilder url = new StringBuilder (CHART_URL);
      
      String date = new SimpleDateFormat ("MMMMM yyyy").format (new Date());
      String title = alliance.getName ().replace (" ", "+") + "+By+Class (" + date + ")";
      
      int w = 1000, h = 300; // max w*h is 300,000
      url.append ("?cht=bhs"); // horizontal bar
      url.append ("&chs=" + w + "x" + h); // size
      url.append ("&chtt=" + title);
      url.append ("&chts=0000FF,24"); // title color,size
      url.append ("&chxt=y"); // label the Y-axis
      
      appendList (url, "&chco=", ranges.colors, ",");
      
      StringBuilder labels = new StringBuilder ("&chxl=0:|");
      int max = 0;
      for (Klass cls : classes)
      {
         int totalForClass = ranges.totalByClass.getInt (cls); 
         if (totalForClass > max)
            max = totalForClass;
         labels.append (cls + " (" + totalForClass + ")|");
      }
      url.append (labels);
      
      appendList (url, "&chd=t:", ranges.dataSets, "|"); // data
      appendList (url, "&chm=", ranges.pointSpecs, "|"); // labels on top of each bar
      url.append ("&chds=0," + Math.round (max * 1.05)); // scaling limits
      appendList (url, "&chdl=", ranges.labels, "|"); // legend labels
      url.append ("&chdlp=b"); // legend position (below)

      return url.toString();
   }
   
   private void appendList (final StringBuilder url, final String prefix, 
                            final List<String> list, final String sepr)
   {
      url.append (prefix);
      url.append (list.get (0));
      for (String token : list.subList (1, list.size()))
         url.append (sepr + token);
   }
   
   private final class LevelRanges
   {
      private List<String> colors = new ArrayList<String>();
      private List<Range> levels = new ArrayList<Range>();
      private List<String> labels = new ArrayList<String>();
      private List<String> dataSets = new ArrayList<String>();
      private List<String> pointSpecs = new ArrayList<String>();
      
      private NumericMap<Klass, Integer> totalByClass = 
         new NumericMap<Klass, Integer> (Integer.class);
      
      private LevelRanges() { }
      
      private void add (final String color, final int min, final int max)
      {
         colors.add (color);
         Range range = new Range (min, max);
         levels.add (range);
         labels.add ("Level " + range.getLabel ("-"));
         
         StringBuilder data = new StringBuilder();
         List<Klass> reversed = new ArrayList<Klass> (classes);
         Collections.reverse (reversed);
         for (Klass cls : reversed)
         {
            int qty = getQuantity (cls, range);
            if (data.length() > 0)
               data.append (",");
            data.append (qty);
            totalByClass.plus (cls, qty);
         }
         // color, set#, points, size
         pointSpecs.add ("N,000000," + dataSets.size() + ",-1,10");
         dataSets.add (data.toString());
      }
      
      private int getQuantity (final Klass klass, final Range range)
      {
         int count = 0;
         for (Character ch : alliance.getCharacters().values())
            if (ch.getKlass() == klass && range.includes (ch.getLevel()))
               count++;
         return count;
      }
   }
   
   public static void main (final String[] args) throws Exception
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (false);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      // Kinship kinship = xml.scrapeURL ("Landroval", "Ranya Palantiri");
      Graphs app = new Graphs (kinship);
      
      String url = app.getBarChartByClass();
      System.out.println (url);
      System.out.println ("<img border=1 src=\"" + url + "\" alt=\"Bar Chart\" />");
   }
}
