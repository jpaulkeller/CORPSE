package model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * CollectionMap is a mapping of Collections; a 1-to-many data structure.
 * The key can be any object of type K. The value is a List of objects of type V.
 * The addElement and removeElement methods have been provided to support 
 * adding/removing elements from the list.
 */

public class CollectionMap<K, V> extends TreeMap<K, List<V>>
{
   private static final long serialVersionUID = 0;

   /**
    * Adds the non-null <code>value</code> into the list associated
    * with the given <code>key</code>, creating the list if necessary.
    * Neither the key nor the value can be <code>null</code>.
    *
    * <p> The list can be retrieved by calling the <code>get</code>
    * method with a key that is equal to the original key.
    *
    * @param      key     the key into the map
    * @param      value   the value to add into the key's list.
    *
    * @return this method returns the length of the list before the add
    * @exception  NullPointerException  if the key is <code>null</code>.
    */

   public synchronized int putElement (final K key, final V value)
   {
      List<V> c = get (key);
      if (c != null)      // add the value into the collection
         c.add (value);
      else                // create a new collection with 1 value
      {
         c = new ArrayList<V>();
         c.add (value);
         super.put (key, c);
      }
      return (c.size() - 1);
   }

   /**
    * Removes the <code>value</code> from the list associated with the
    * given <code>key</code>.  This method does nothing if the key is
    * not in the map.
    *
    * @param   key the key mapped to the list containing the value.
    * @param   value the value that needs to be removed.
    * @return  true if the value was removed from the list.
    */

   public synchronized boolean removeElement (final K key, final V value)
   {
      boolean removed = false;
      List<V> c = get (key);
      if (c != null)      // remove the value from the collection
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

      for (Entry<K, List<V>> entry : entrySet())
      {
         K key = entry.getKey();
         sb.append (key + "\n");
         if (showElements)
         {
            List<V> elements = entry.getValue();
            if (elements != null)
               for (V element : elements)
                  sb.append ("  " + element + "\n");
         }
      }

      return sb.toString();
   }

   public static void main (final String[] args) // for testing
   {
      CollectionMap<String, String> hv = new CollectionMap<String, String>();
      System.out.println ("\nAdding elements...");
      hv.putElement ("dog", "collie");
      hv.putElement ("dog", "doberman");
      hv.putElement ("dog", "spaniel");
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

