package model;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * CollectionMap is a mapping of Sets; a 1-to-many data structure.
 * The key can be any object of type K. The value is a Set of objects of type V.
 * The addElement and removeElement methods have been provided to support 
 * adding/removing elements from the set.
 */

public class SetMap<K, V> extends TreeMap<K, Set<V>>
{
   private static final long serialVersionUID = 0;

   /**
    * Adds the non-null <code>value</code> into the set associated
    * with the given <code>key</code>, creating the set if necessary.
    * Neither the key nor the value can be <code>null</code>.
    *
    * <p> The set can be retrieved by calling the <code>get</code>
    * method with a key that is equal to the original key.
    *
    * @param      key     the key into the map
    * @param      value   the value to add into the key's set.
    *
    * @return this method returns the length of the set before the add
    * @exception  NullPointerException  if the key is <code>null</code>.
    */

   public synchronized int putElement (final K key, final V value)
   {
      Set<V> set = get (key);
      if (set != null)      // add the value into the set
         set.add (value);
      else                  // create a new set with 1 value
      {
         set = new TreeSet<V>();
         set.add (value);
         super.put (key, set);
      }
      return (set.size() - 1);
   }

   /**
    * Removes the <code>value</code> from the set associated with the
    * given <code>key</code>.  This method does nothing if the key is
    * not in the map.
    *
    * @param   key the key mapped to the set containing the value.
    * @param   value the value that needs to be removed.
    * @return  true if the value was removed from the set.
    */

   public synchronized boolean removeElement (final K key, final V value)
   {
      boolean removed = false;
      Set<V> c = get (key);
      if (c != null)      // remove the value from the set
      {
         removed = c.remove (value);
         if (c.isEmpty())  // last element, set value to null
            super.remove (key);
      }

      return removed;
   }

   /** A diagnostic routine convert the HashVector into an outline. */

   public String toString (final boolean showElements)
   {
      StringBuilder sb = new StringBuilder();

      for (Entry<K, Set<V>> entry : entrySet())
      {
         K key = entry.getKey();
         sb.append (key + "\n");
         if (showElements)
         {
            Set<V> elements = entry.getValue();
            if (elements != null)
               for (V element : elements)
                  sb.append ("  " + element + "\n");
         }
      }

      return sb.toString();
   }

   public static void main (final String[] args) // for testing
   {
      SetMap<String, String> hv = new SetMap<String, String>();
      System.out.println ("\nAdding elements...");
      hv.putElement ("dog", "collie");
      hv.putElement ("dog", "spaniel");
      hv.putElement ("dog", "spaniel");
      hv.putElement ("dog", "spaniel");
      hv.putElement ("dog", "doberman");
      hv.putElement ("cat", "persian");
      hv.putElement ("cat", "cheshire");
      System.out.println (hv.toString (true));

      System.out.println ("\nRemoving elements...");
      hv.removeElement ("dog", "spaniel");
      hv.removeElement ("cat", "persian");
      hv.removeElement ("cat", "cheshire");
      System.out.println (hv.toString (true));
   }
}

