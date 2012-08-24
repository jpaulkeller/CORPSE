package lotro.deed;

import java.util.ArrayList;
import java.util.Set;

public class Partition<E> extends ArrayList<Set<E>>
{
   private static final long serialVersionUID = 1L;
   
   public int sizeOfSmallest()
   {
      int size = Integer.MAX_VALUE;
      for (Set<E> set : this)
         if (set.size() < size)
            size = set.size();
      return size;
   }
   
   public int sizeOfLargest()
   {
      int size = Integer.MIN_VALUE;
      for (Set<E> set : this)
         if (set.size() > size)
            size = set.size();
      return size;
   }
}
