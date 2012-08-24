package lotro.my.reports;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

public class LevelGraph
{
   private static final Date DAY_1 = new Date (1175227200000L); // 03/30/2007 
   private static final Date DAY_MOM = new Date (1226984400000L); // 11/18/2008
   private static final long MSECS_PER_DAY = 1000 * 60 * 60 * 24;
   
   private static final String MDY = "MM/dd/yyyy";
   private static final Pattern LEVEL_DATE = 
      Pattern.compile ("Reached Level ([0-9]+).([0-9]{2}/[0-9]{2}/[0-9]{4})", 
                       Pattern.DOTALL);

   public Map<Integer, Date> getLevelDates()
   {
      Map<Integer, Date> dates = new HashMap<Integer, Date>();

      SimpleDateFormat df = new SimpleDateFormat (MDY);
      String path = "C:/pkgs/workspace/LOTRO/data/level.txt";
      
      String text = FileUtils.getText (path, FileUtils.UTF8);
      Matcher m = LEVEL_DATE.matcher (text);
      while (m.find())
      {
         try
         {
            int level = Integer.parseInt (m.group (1));
            Date when = df.parse (m.group (2));
            dates.put (level, when);
            // System.out.println (level + " on " + when); // TBD
         }
         catch (ParseException x)
         {
            System.err.println ("Invalid date: " + m.group (0));
            x.printStackTrace (System.err);
         }
      }

      return dates;
   }
   
   public static void main (final String[] args) throws Exception
   {
      LevelGraph app = new LevelGraph();
      
      StringBuilder url = new StringBuilder ("http://chart.apis.google.com/chart?cht=lxy");
      int w = 400, h = 400;
      url.append ("&chs=" + w + "x" + h);
      url.append ("&chtt=Mosby's Levels");
      url.append ("&chxt=x,y&chxl=0:|0|20|40|60|1:|0|50|100|150");
      url.append ("&chds=0,60,0,150"); // data scaling (level 0-60, days 0-150)
         
      StringBuilder days = new StringBuilder();
      StringBuilder levels = new StringBuilder();
      Map<Integer, Date> dates = app.getLevelDates();
      
      // TBD what if 50 > MOM?
      
      int daysTo50 = (int) ((dates.get (50).getTime() - DAY_1.getTime()) / MSECS_PER_DAY);
      for (int level = 2; level <= dates.size(); level++)
      {
         long msecs = dates.get (level).getTime();
         int daysToLevel;
         if (level <= 50)
            daysToLevel = (int) ((msecs - DAY_1.getTime()) / MSECS_PER_DAY);
         else
            daysToLevel = (int) ((msecs - DAY_MOM.getTime()) / MSECS_PER_DAY) + daysTo50;
         days.append (daysToLevel);
         levels.append (level);
         if (level < dates.size())
         {
            days.append (",");
            levels.append (",");
         }
      }  
      url.append ("&chd=t:");
      url.append (levels); // x-axis
      url.append ("|");
      url.append (days); // y-axis
      
      System.out.println (url);
      
      // System.out.println ("Moria: " + new SimpleDateFormat ("MM/dd/yyyy").parse ("11/18/2008").getTime());
   }
}
