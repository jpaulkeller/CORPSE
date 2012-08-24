package lotro.my.xml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import web.Firewall;

import file.FileUtils;

public final class Scraper
{
   private static final String ENCODING = "UTF8";
   private static final Pattern RESPONSE = 
      Pattern.compile ("<apiresponse>(.+)</apiresponse>", Pattern.MULTILINE | Pattern.DOTALL); 
   private static final Pattern ERROR = 
      Pattern.compile ("<error>(.+)</error>", Pattern.MULTILINE | Pattern.DOTALL); 
   
   private Scraper() { }
   
   public static String scrapeURL (final String request, final String... args) 
   throws IOException
   {
      StringBuilder url = new StringBuilder ("http://data.lotro.com/");
      // TODO
      // url.append (System.getenv ("MYLOTRO_DEV") + "/");
      // url.append (System.getenv ("MYLOTRO_KEY") + "/");
      url.append (API.DEVELOPER + "/");
      url.append (API.KEY + "/");
      
      url.append (request + "/");
      try
      {
         for (int arg = 0; arg < args.length; arg += 2)
         {
            url.append (args[arg] + "/");
            url.append (URLEncoder.encode (args[arg + 1], ENCODING) + "/");
         }
      }
      catch (UnsupportedEncodingException x)
      {
         x.printStackTrace();
      }
      
      System.out.println ("URL: " + url); // TBD
      return scrape (url.toString());
   }

   public static String scrape (final String path) throws IOException
   {
      BufferedReader buf = null;
      try
      {
         Firewall.defineProxy();
         
         InputStream is;
         if (path.startsWith ("http://"))
            is = new URL (path).openStream();
         else
            is = new FileInputStream (path);
         InputStreamReader isr = new InputStreamReader (is, ENCODING);
         buf = new BufferedReader (isr);

         StringBuilder response = new StringBuilder();
         String line;
         while ((line = buf.readLine()) != null)
         {
            response.append (line);
            Matcher m = RESPONSE.matcher (response);
            if (m.find())
            {
               Matcher error = ERROR.matcher (m.group (1));
               if (error.find())
                  System.out.println ("ERROR: " + error.group (1));
               return m.group (1);
            }
         }
      }
      catch (MalformedURLException x)
      {
         x.printStackTrace (System.err);
      }
      finally
      {
         FileUtils.close (buf);
      }
      
      return null;
   }
   
   /**
    * Returns a map of key/value pairs (one for each attribute).  Adds a property
    * called "contents" for the unprocessed text (nested tags), if any. */
   
   public static Map<String, String> parseWrapper (final String xml, final String tag)
   {
      Map<String, String> attributes = null;
      
      // <tag key="value" ...>
      //   ...
      // </tag>
      String regex = "<" + tag + "([^>]*)>" + "(.*)" + "</" + tag + ">";
      Pattern p = Pattern.compile (regex, Pattern.MULTILINE | Pattern.DOTALL);
      Matcher m = p.matcher (xml);
      if (m.find())
      {
         attributes = parseAttributes (m.group (1));
         attributes.put ("contents", m.group (2));
      }
      else // <tag key="value" .../>
      {
         regex = "<" + tag + "([^>]*)/>";
         p = Pattern.compile (regex, Pattern.MULTILINE | Pattern.DOTALL);
         m = p.matcher (xml);
         if (m.find())
            attributes = parseAttributes (m.group (1));
         else
            System.out.println ("No match: " + regex + "\n" + xml); // TBD
      }
      
      return attributes;
   }

   /** Returns a list of maps of key/value pairs. */
   
   public static List<Map<String, String>> parseTags (final String xml, final String tag)
   {
      List<Map<String, String>> tags = new ArrayList<Map<String, String>>();
      
      // <tag key="value" .../>
      Pattern p = Pattern.compile ("<" + tag + "([^>]*)" + "/>");
      Matcher m = p.matcher (xml);
      while (m.find())
         tags.add (parseAttributes (m.group (1)));
      
      return tags;
   }

   // <profession name="Weaponsmith" proficiency="5" mastery="5" />
   private static final Pattern ATTR = Pattern.compile ("([a-zA-Z]+)=\"([^\"]*)\"");
   
   private static Map<String, String> parseAttributes (final String xml)
   {
      Map<String, String> attributes = new HashMap<String, String>();
      Matcher m = ATTR.matcher (xml);
      while (m.find())
         attributes.put (m.group (1), m.group (2));
      return attributes;
   }
}
