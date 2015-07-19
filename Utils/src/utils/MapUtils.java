package utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static convenience methods for accessing String-to-String Map structures.
 */

public final class MapUtils
{
   private MapUtils()
   {
      // utility class; prevent instantiation
   }
   
   /**
    * A convenience method for getting the key associated with the
    * given value. Null values are not supported. */

   public static String getKey (final Map<String, String> map, final String value)
   {
      if (value != null)
         for (Map.Entry<String, String> entry : map.entrySet())
            if (value.equals (entry.getValue()))
               return entry.getKey();
      return null;
   }
      
   /**
    * Retrieve the property keys associated with all properties
    * matching the given regular expression, as a collection.  A key
    * is only added to the collection if the property key matches the
    * given regex.
    *
    * The keys will be returned in alphabetical order.
    *
    * @param regexPattern An optional regular expression that the
    * property key must match (if null, it will be ignored).
    *
    * @return An ordered list of property keys. */
   
   public static List<String> getKeys (final Map<String, String> map,
                                       final String regexPattern)
   {
      List<String> matches = new ArrayList<String>();

      if (map != null)
      {
         Pattern pattern = null;
         if (regexPattern != null)
            pattern = Pattern.compile (regexPattern);
         
         for (String key : map.keySet())
            if (pattern == null || pattern.matcher (key).matches())
               matches.add (key);
         if (!matches.isEmpty())
            Collections.sort (matches); // sort by property key
      }

      return matches;
   }

   /**
    * Retrieve a specifiable portion of the the property keys
    * associated with all properties matching the given regular
    * expression, as a collection.  An entry is only added to the
    * collection if the property key matches the given regex.
    *
    * The key parts will be returned in alphabetical order.
    *
    * @param regexPattern A regular expression that the property key
    *                     must match.  It must contain 1 capture group.
    *
    * @return An ordered collection of property key parts.
    */

   public static Collection<String> getKeyParts (final Map<String, String> map,
                                                 final String regexPattern)
   {
      Collection<String> matches = new TreeSet<String>();

      if (map != null && regexPattern != null)
      {
         Pattern pattern = Pattern.compile (regexPattern);
         if (pattern != null)
         {
            for (String key : map.keySet())
            {
               Matcher matcher = pattern.matcher (key);
               if (matcher.matches())
                  matches.add (matcher.group (1));
            }
         }
      }

      return matches;
   }

   /** Return a list of keys which end with the given suffix. */

   public static List<String> getKeysForSuffix (final Map<String, String> map,
                                                final String suffix)
   {
      return getKeys (map, ".+" + suffix);
   }

   /** Return a list of keys which start with the given prefix. */

   public static List<String> getKeysForPrefix (final Map<String, String> map,
                                                final String prefix)
   {
      return getKeys (map, prefix + ".+");
   }

   /**
    * This method returns a list of keys for a given value.
    *
    * @return Unsorted list of property keys given a value
    */
   public static List<String> getKeysForValue (final Map<String, String> map,
                                               final String value)
   {
      List<String> keys = new ArrayList<String>();

      if (map != null)
         for (Entry<String, String> entry : map.entrySet())
            if (entry.getValue().equals (value))
               keys.add (entry.getKey());

      return keys;
   }

   /**
    * Returns an array of key prefixes that exist for a given key suffix.
    */
   public static List<String> getKeyPrefixesForSuffix (final Map<String, String> map,
                                                       final String suffix)
   {
      List<String> prefixes = new ArrayList<String>();

      if (map != null)
      {
         for (String key : map.keySet())
         {
            int idx = key.indexOf (suffix);
            if (idx >= 0)
               prefixes.add (key.substring (0, idx));
         }
      }

      return prefixes;
   }

   /**
    * Return a list of all property values where the keys match the
    * given regexPattern. */

   public static List<String> getList (final Map<String, String> map,
                                       final String regexPattern)
   {
      List<String> values = new ArrayList<String>();

      if (map != null)
      {
         List<String> keys = getKeys (map, regexPattern);
         for (String key : keys)
            values.add (map.get (key));
      }
      
      return values;
   }

   /**
    * Return a list of all property values matching the given
    * regexPattern. */

   public static List<String> getKeysMatchingValue (final Map<String, String> map,
                                                    final String regexPattern)
   {
      List<String> keys = new ArrayList<String>();

      if (map != null)
      {
         Pattern pattern = Pattern.compile (regexPattern);
         for (Map.Entry<String, String> entry : map.entrySet())
            if (pattern.matcher (entry.getValue()).matches())
               keys.add (entry.getKey());
      }
      
      return keys;
   }

   /**
    * Return a list of all property values matching the given
    * regexPattern. */

   public static List<String> getValuesMatching (final Map<String, String> map,
                                                 final String regexPattern)
   {
      List<String> values = new ArrayList<String>();

      if (map != null)
      {
         Pattern pattern = Pattern.compile (regexPattern);
         for (String value : map.values())
            if (pattern.matcher (value).matches())
               values.add (value);
      }
      
      return values;
   }

   /** Return a list of values for keys which start with the given prefix. */

   public static List<String> getValuesForPrefix (final Map<String, String> map,
                                                  final String prefix)
   {
      return getList (map, prefix + ".*");
   }

   /**
    * Return an array of values for keys which start with the given
    * prefix (ordered by property key).
    */
   public static List<String> getValuesForSuffix (final Map<String, String> map,
                                                  final String suffix)
   {
      return getList (map, ".*" + suffix);
   }

   public static void trace (final Map<String, ? extends Object> map,
                             final PrintStream out)
   {
      trace (map, out, true);
   }
   
   public static void trace (final Map<String, ? extends Object> map,
                             final PrintStream out, final boolean sortByKey)
   {
      if (map != null)
      {
         Collection<String> keys = map.keySet();
         if (sortByKey)
         {
            List<String> sortable = new ArrayList<String> (keys);
            Collections.sort (sortable);
            keys = sortable;
         }
         for (String key : keys)
         {
            Object val = map.get (key);
            if (val instanceof String || val == null)
               out.println ("   " + key + " = " + val);
            else
               out.println ("   " + key + " = " + val +
                            " (" + val.getClass().getName() + ")");
         }
         out.println();
      }
   }

   /**
    * Retrieve the property value (as a boolean) associated with the given
    * property key. */

   public static boolean getBoolean (final Map<String, String> map,
                                     final String key, 
                                     final boolean defaultValue)
   {
      boolean b = defaultValue;

      if (map != null)
      {
         String value = map.get (key);
         if (value != null)
            b = Boolean.valueOf (value).booleanValue();
      }

      return b;
   }

   /**
    * Retrieve the property value (as a double) associated with the
    * given property key.
    */
   public static double getDouble (final Map<String, String> map,
                                   final String key,
                                   final double defaultValue)
   {
      double dbl = defaultValue;

      if (map != null)
      {
         String value = map.get (key);
         try
         {
            if (value != null)
               dbl = Double.parseDouble (value);
         }
         catch (NumberFormatException e)
         {
            dbl = defaultValue;
         }
      }
      
      return dbl;
   }

   public static void main (final String[] args)
   {
      Map<String, String> map = new java.util.HashMap<String, String>();

      map.put ("A", "1");
      map.put ("B", "2");
      map.put ("C", "3");

      MapUtils.trace (map, System.out);

      System.out.println ("getKey (2): " + MapUtils.getKey (map, "2"));
   }
}
