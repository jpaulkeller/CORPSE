package lotro.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

public class TopicPie
{
   private static final Pattern PROPERTY = Pattern.compile ("(.+)=(.+)");
   
   public static Map<Integer, String> readMap (final String path)
   {
      Map<Integer, String> map = new TreeMap<Integer, String>();
      
      InputStream is = null;
      try
      {
         is = new FileInputStream (path);
         InputStreamReader isr = new InputStreamReader (is);
         BufferedReader br = new BufferedReader (isr);
         String line = null;
         while ((line = br.readLine()) != null)
         {
            Matcher m = PROPERTY.matcher (line);
            if (m.matches())
               map.put (Integer.parseInt (m.group(2)), m.group(1));
         }
      }
      catch (Exception x)
      {
         x.printStackTrace (System.err);
      }
      finally
      {
         FileUtils.close (is);
      }
      
      return map;
   }
   
   public static void main (String[] args)
   {
      String path = FileUtils.MY_DESK + File.separator + "WordCount.txt";
      Map<Integer, String> map = readMap(path);
      for (Entry<Integer, String> entry : map.entrySet())
         System.out.println (entry.getValue() + "\t" + entry.getKey());
   }
}
