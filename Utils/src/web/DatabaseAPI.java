package web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static convenience methods for accessing a web server's database
 * from client applets.
 */
public final class DatabaseAPI
{
   private static String databaseURL;
   private static String user;

   private static final Pattern MAP_ENTRY_PATTERN = Pattern.compile ("(.+)==(.+)");
   
   private DatabaseAPI() { }

   public static void setURL (final String url)
   {
      DatabaseAPI.databaseURL = url;
   }

   public static String getURL()
   {
      return databaseURL;
   }

   public static void setUser (final String user)
   {
      DatabaseAPI.user = user;
   }

   public static String getUser()
   {
      return user;
   }

   public static void execute (final String connName, final String sql)
   {
      try
      {
         BufferedReader reader = pushSQL (connName, sql, null);
         if (reader != null)
         {
            /* String reply = */ reader.readLine(); // currently ignored
            reader.close();
         }
      }
      catch (IOException x)
      {
         System.err.println (x);
      }
   }

   public static String getString (final String connName, final String sql)
   {
      String value = null;
      
      try
      {
         BufferedReader reader =
            pushSQL (connName, sql, DatabaseServlet.STRING_FORMAT);
         if (reader != null)
         {
            value = reader.readLine();
            reader.close();
         }
      }
      catch (IOException x)
      {
         System.err.println (x);
      }

      return value;
   }

   public static List<String> getList (final String connName, final String sql)
   {
      List<String> list = new ArrayList<String>();
      
      try
      {
         BufferedReader reader = pushSQL (connName, sql, DatabaseServlet.LIST_FORMAT);
         if (reader != null)
         {
            String value;
            while ((value = reader.readLine()) != null)
               list.add (value);
            reader.close();
         }
      }
      catch (IOException x)
      {
         System.err.println (x);
      }

      return list;
   }

   /**
    * Returns map of two field values (Field1's value as the key
    * mapped to Field2's value) for multiple records. */

   public static Map<String, String> getMapping (final String connName, final String sql)
   {
      return getMap (connName, sql, DatabaseServlet.MAP_FORMAT);
   }

   /**
    * Returns map of (uppercased) field name to values (for a single
    * record). */

   public static Map<String, String> getFieldMap (final String connName, final String sql)
   {
      return getMap (connName, sql, DatabaseServlet.FIELD_MAP_FORMAT);
   }

   private static Map<String, String> getMap (final String connName, final String sql,
                                              final String format)
   {
      Map<String, String> map = new LinkedHashMap<String, String>();
      
      try
      {
         BufferedReader reader = pushSQL (connName, sql, format);
         if (reader != null)
         {
            String entry;       // key==value
            while ((entry = reader.readLine()) != null)
            {
               Matcher m = MAP_ENTRY_PATTERN.matcher (entry);
               if (m.matches())
                  map.put (m.group (1), m.group (2));
            }
            reader.close();
         }
      }
      catch (IOException x)
      {
         System.err.println (x);
      }

      return map;
   }

   private static BufferedReader pushSQL (final String connName, final String sql,
                                          final String format)
   {
      StringBuilder address = new StringBuilder (databaseURL);

      HTMLUtils.appendArgument (address, Servlet.USERNAME, user);
      // HTMLUtils.appendArgument (address, DatabaseServlet.CONNECTION_FIELD, connName);

      if (format != null)
         HTMLUtils.appendArgument (address, DatabaseServlet.FORMAT_FIELD, format);

      try
      {
         URL url = new URL (address.toString());

         URLConnection conn = url.openConnection();
         conn.setDoInput (true);
         conn.setDoOutput (true);
         conn.setUseCaches (false);
         conn.setRequestProperty ("Content-type", "text/plain");

         // push the SQL statement
         PrintStream out = new PrintStream (conn.getOutputStream());
         out.println (sql);
         out.flush();
         out.close();

         InputStreamReader isr = new InputStreamReader (conn.getInputStream());
         return new BufferedReader (isr);
      }
      catch (MalformedURLException x)
      {
         System.err.println ("Error creating URL: " + address + "\n" + x);
      }
      catch (IOException x)
      {
         System.err.println ("Error opening URL: " + address + "\n" + x);
      }
      catch (Exception x)
      {
         System.err.println ("Error processing URL: " + address + "\n" + x);
      }

      return null;
   }
}
