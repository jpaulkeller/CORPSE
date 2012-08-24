package utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SysProperties are system specific properties that require a higher
 * scope than application properties.  These properties may specify
 * the root or home directory of the System's main application, or
 * System wide database location and type.  In general, anything that
 * is common to all apps running on this system.
 *
 * When System properties are loaded, the user may specify, through a
 * boolean flag, whether or not to combine these properties with the
 * JVM default System properties.  This allows the user to be certain
 * that a property or group of properties is available using the
 * System.getProperty() method.
 */
public final class SysProperties
{
   private static final Pattern WINDOWS_ENV_VAR = Pattern.compile ("%([^%]+)%");
   private static final Pattern KEY_VALUE = Pattern.compile ("([^=]+)=(.+)");
   
   private static Map<String, String> properties;

   static { loadProperties(); }

   private SysProperties()
   {
      // utility class; prevent instantiation
   }

   private static void loadProperties()
   {
      properties = new TreeMap<String, String>();
      
      // add properties from the environment
      Map<String, String> props = System.getenv();
      if (props != null)
         properties.putAll (props);
      
      // add Java system properties
      Map<Object, Object> javaProps = System.getProperties();
      for (Map.Entry<Object, Object> entry : javaProps.entrySet())
         if (entry.getValue() instanceof String)
            properties.put ((String) entry.getKey(), (String) entry.getValue()); 
   }

   /**
    * Retrieves the value associated with the given property.  This method
    * first looks in the Java system properties, and then in the environment
    * variables, and finally in a property cache.
    *
    * @param key the property key-value to look up.
    * @return     the value of key or null if the property isn't defined.
    */
   public static String getProperty (final String key)
   {
      String val = System.getProperty (key); // first try the Java system
      if (val == null)
         val = System.getenv (key); // then try the environment variables
      if (val == null)
         val = properties.get (key); // check the cache
      return val;
   }

   public static Map<String, String> getProperties()
   {
      return properties;
   }

   /**
    * Replace all resolvable environment variables in the given string with
    * their associated values.
    */
   public static String expandEnvVars (final String s)
   {
      String result = s;
      if (result != null)
      {
         Matcher m = WINDOWS_ENV_VAR.matcher (result);
         while (m.find())
         {
            String val = System.getenv (m.group (1));
            if (val != null)
            {
               result = m.replaceFirst (Matcher.quoteReplacement (val));
               m = WINDOWS_ENV_VAR.matcher (result); // string changed; reset m
            }
         }
      }
      return result;
   }
   
   // TBD: move to StringUtil
   /**
    * Remove any elements from the given list which do not start with the
    * given prefix and end with the given suffix.  Case is ignored.
    */
   public static void filter (final Collection<String> list,
                              final String prefix, final String suffix)
   {
      Iterator<String> iter = list.iterator();
      while (iter.hasNext())
      {
         String entry = iter.next().toLowerCase();
         if (prefix != null && !entry.startsWith (prefix.toLowerCase()))
            iter.remove();
         else if (suffix != null && !entry.endsWith (suffix.toLowerCase()))
            iter.remove();
      }
   }

   /**
    * This method provides a convenient way to time operations.
    *
    * To use this method, call it once with reset==true, and then
    * later with reset==false.  It will output a message (for each
    * reset==false call) showing the given key and time elapsed (in
    * microseconds) between the calls.
    *
    * SysProperties.showTime (System.out, "method", true);
    * ... // do some processing
    * SysProperties.showTime (System.out, "method", false);
    */

   public static void showClassPath()
   {
      String separator = System.getProperty ("path.separator");
      String cp = System.getProperty ("java.class.path");
      int from = 0;
      int brk = cp.indexOf (separator);
      while (brk > 0)
      {
         System.out.println (cp.substring (from, brk));
         from = brk + 1;
         brk = cp.indexOf (separator, from);
      }
   }
   
   /**
    * @return A Map of key/value pairs for the passed in arguments.
    */
   public static Map<String, String> parseArguments (final String[] args)
   {
      return parseArguments (args, null);
   }
   
   /**
    * Assumes all argument pairs are in the form "-key" "value", and that the
    * keys are unique. Argument pairs will be stored in the (ordered) props map
    * (for easy access), and returned.
    *
    * If your application needs to support multiple pairs with the same key, 
    * you will need to handle that directly (see DatabaseSAO for an example).
    *
    * Any pairs where the key is "-sys" are assumed to have values in the form
    * "Property=Value", and will be stored as System properties. This 
    * facilitates testing by simulating the JNLP "property" name/value tag.
    * 
    * @param args
    * @param usage An optional usage string to display if the argument syntax is
    *           invalid.
    * @return A Map of key/value pairs for the passed in arguments
    */
   public static Map<String, String> parseArguments (final String[] args,
                                                     final String usage)
   {
      // create a map for the argument key/value pairs
      Map<String, String> props = new LinkedHashMap<String, String>();
   
      Matcher m;
   
      for (int i = 0, n = args.length; i < n; i++)
         if (args[i].startsWith ("-") && i + 1 < n)
         {
            if (args[i].equalsIgnoreCase ("-sys") &&
                (m = KEY_VALUE.matcher (args[i + 1])).matches())
            {
               String key = m.group (1);
               String val = m.group (2);
               System.setProperty (key, val);
               properties.put (key, val); // update the cache
               i++;
            }
            else
               props.put (args[i], args[++i]);
         }
         else
         {
            if (usage != null)
               System.err.println (usage);
            props.clear();      // to indicate an error has occurred
            break;              // abort
         }
   
      return props;
   }

   public static void main (final String[] args)
   {
      SysProperties.parseArguments (args);
      
      System.out.println ("Java Class Path:");
      showClassPath();
      System.out.println();
      
      System.out.println ("All Properties (system, file, and environment):");
      MapUtils.trace (properties, System.out);
      System.out.println();
      
      for (String s : new String[] { "os.name", "COMET", "Internal.DbName", "Missing" }) 
         System.out.println ("getProperty (" + s + "): " + getProperty (s));
      System.out.println();
      
      String s = "COMET: %COMET%; COMET_HOME: %COMET_HOME%; BAD: %BAD%";
      System.out.println ("Input: " + s);
      System.out.println ("expandEnvVars: " + expandEnvVars (s));
      System.out.println();
   }
}
