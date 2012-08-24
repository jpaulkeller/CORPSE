package model;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

import utils.MapUtils;

/**
 * An extension of TreeMap that provides inverted access (value to
 * key).  This class assumes there is a 1-to-1 mapping in both
 * directions; no attempt is made to ensure this. */

public class CrossMap<K extends Comparable<K>, V extends Comparable<V>>
extends TreeMap<K, V>
{
   private static final long serialVersionUID = 0;

   private Map<V, K> inverted = new TreeMap<V, K>();

   public CrossMap()
   {
   }
   
   public CrossMap (final Map<K, V> map)
   {
      putAll (map);
   }
   
   public Map<V, K> invert()
   {
      return inverted;
   }

   /** Assumes 1-to-1 mapping.  See getKeys(). */

   public K getKey (final V value)
   {
      return inverted.get (value);
   }

   /**
    * A convenience method for getting the key(s) associated with the
    * given value.  This method supports 1-to-many mappings.  Null
    * values are not supported. */

   public List<K> getKeys (final V value)
   {
      List<K> keys = new ArrayList<K>();
      if (value != null)
         for (Map.Entry<K, V> entry : entrySet())
            if (value.equals (entry.getValue()))
               keys.add (entry.getKey());
      return keys;
   }
      
   // override the following methods to cross-link the data

   @Override
   public V put (final K key, final V value)
   {
      /* TBD
      // check for possible non 1-to-1 mapping:
      K k = inverted.get (value);
      if (k != null)
         System.out.println ("CrossMap collision " + value + ": " +
                             key + " and " + k);
      */
      inverted.put (value, key);
      return super.put (key, value); 
   }

   @Override
   public V remove (final Object key)
   {
      V value = get (key);
      if (value != null)
         inverted.remove (value);
      return super.remove (key);
   }

   public static void main (final String[] args)
   {
      CrossMap<String, String> map = new CrossMap<String, String>();

      for (int i = 0; i < 3; i++)
         map.put ("k" + i,   "v" + i);

      MapUtils.trace (map, System.out);
      MapUtils.trace (map.invert(), System.out);
   }
}
