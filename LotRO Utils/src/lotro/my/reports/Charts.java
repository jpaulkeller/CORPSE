package lotro.my.reports;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lotro.models.Alliance;
import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Klass;
import lotro.models.Rank;
import lotro.my.reports.FilterFactory.LevelFilter;
import lotro.my.xml.KinshipXML;
import lotro.web.Dropbox;
import file.FileUtils;

public final class Charts
{
   private static final int FILTER_THRESHOLD = 49;
   
   private Kinship kinship;
   
   public void chartKinship(final Kinship kinship) 
   {
      this.kinship = kinship;
      int level = 0;

      LevelFilter levelFilter = (LevelFilter) FilterFactory.getLevelFilter (level);
      kinship.setFilter (levelFilter);
      kinship.addFilter (FilterFactory.getKinRankFilter (Rank.Member));

      int size = kinship.size(true, false); // just count Members+ only
      
      level = 40;
      if (size > FILTER_THRESHOLD)
         levelFilter.setLevels(level, Character.MAX_LEVEL);
      String suffix = level > 0 ? " " + level + "+" : "";
      Report app = new ReportStats (kinship, "Stats" + suffix);
      app.saveFile();
      
      if (size > FILTER_THRESHOLD)
      {
         level = 70;
         levelFilter.setLevels(level, Character.MAX_LEVEL);
         suffix = level > 0 ? " " + level + "+" : "";
         app.setName ("Stats" + suffix);
         app.saveFile();
      }
      
      kinship.setFilter (new ReportCraft.CraftFilter (6));
      app = new ReportCraft (kinship, "Master Crafters");
      app.saveFile();
      
      if (size > FILTER_THRESHOLD)
      {
         level = 66;
         levelFilter.setLevels(level, Character.MAX_LEVEL);
      }
      suffix = level > 0 ? " " + level + "+" : "";
      app = new ReportGear (kinship, "Gear" + suffix);
      app.saveFile();
      
      kinship.setFilter (null);
      Graphs graph = new Graphs (kinship);
      String url = graph.getBarChartByClass();
      System.out.println ("\n" + kinship.getName() + " Bar Chart:\n" + url);
      System.out.println ("<img border=1 src=\"" + url + "\" alt=\"Bar Chart\" />");
   }
   
   private void extraCharts()
   {
      List<String> lines = new ArrayList<String>();
      String prefix = "/charts/" + kinship.getName() + " ";
      for (Klass cls : Klass.FREEPS)
      {
         if (cls == Klass.Unknown)
            continue;
         
         kinship.setFilter (FilterFactory.getLevelFilter (50));
         kinship.addFilter (FilterFactory.getClassFilter (cls));
         lines.clear();
         
         Report app = new ReportStats (kinship, cls + " Stats");
         app.addHead (lines);
         lines.add ("<body>\n");
         lines.add ("<center>\n");
         
         app.exportAsHTML (lines);
         lines.add ("<hr>");
         Report app2 = new ReportGear (kinship, cls + " Gear");
         app2.exportAsHTML (lines);
         
         lines.add ("</center>\n");
         lines.add ("</body>");
         lines.add ("</html>");
         
         String path = Dropbox.get().getPath (prefix + cls + "s.html");
         FileUtils.writeList (lines, path, false);
         System.out.println ("Output: " + path);
      }
      
      // Kinship kinship = alliance.getKinship ("The Palantiri");
      if (kinship.getName().equals ("The Palantiri"))
      {
         String date = new SimpleDateFormat ("yyyyMM").format (new Date());
         String path = Dropbox.get().getPath ("/Palantiri/" + kinship.getName() + ".chr." + date);
         kinship.setFilter (null);
         Character.write (kinship.getCharacters().values(), path);
      }
   }      

   public static void main (final String[] args) throws Exception
   {
      Charts charts = new Charts();
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (true);

      /*
      Kinship palantiri = xml.scrapeURL ("Landroval", "The Palantiri");
      charts.chartKinship (palantiri);
      charts.extraCharts();
      System.exit(0);
      */
      
      Alliance alliance = new Alliance ("Alliance"); // TODO
      /*
      alliance.addKinship (xml.scrapeURL ("Landroval", "Order of the Tower Guard"));
      alliance.addKinship (xml.scrapeURL ("Landroval", "Broken Shadows"));
      alliance.addKinship (xml.scrapeURL ("Landroval", "Caran Gwaith"));
      alliance.addKinship (xml.scrapeURL ("Landroval", "Irony and Spite"));
      alliance.addKinship (xml.scrapeURL ("Landroval", "Knights of the White Lady"));
      alliance.addKinship (xml.scrapeURL ("Landroval", "Section VIII"));
      alliance.addKinship (xml.scrapeURL ("Landroval", "Shadow Company"));
      alliance.addKinship (xml.scrapeURL ("Landroval", "Smaug's Legacy"));
      alliance.addKinship (xml.scrapeURL ("Landroval", "The Dark Blade"));
      */
      
      xml.setLookupPlayer (true);
      alliance.addKinship (xml.scrapeURL ("Landroval", "The Palantiri"));
      // alliance.addKinship (xml.scrapeURL ("Elendilmir", "Black Isle"));

      for (Kinship kinship : alliance.getKinships())
         charts.chartKinship (kinship);
      
      if (alliance.size() > 1)
         charts.chartKinship (alliance);
      
      charts.extraCharts();
   }
}
