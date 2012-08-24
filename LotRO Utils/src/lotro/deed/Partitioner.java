package lotro.deed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 A class definition which will calculate "set partition" information.
 A set partition is a collection of disjoint subsets of a set of distinct
 objects which together include all of the members of the set exactly once.

 Calls to partitionCount will return the "Bell number", the total number of set
 partitions for the current set size as a 64 bit integer (accurate for set
 sizes up to approximately 25 which is 10^20).

 Internally, each partition is returned as a matrix, a square array of ints,
 with each row representing a subset and column as elements of the subsets.
 Unused elements of the array in the right and bottom portions of the array
 are set to  -1.

 So, for example, if the set is {1,2,3,4} and the particular set partition
 returned is {1},{2,3},{4} then the partition array after a call to
 getNextMatrix would contain:

   1,-1,-1,-1
   2, 3,-1,-1
   4,-1,-1,-1
  -1,-1,-1,-1

  If sets contain other than in-order integers, the returned partition array
  subsets may be used to index the actual set members.
  
  See also: http://search.cpan.org/~pkent/
  Set-Partition-SimilarValues-1.003/lib/Set/Partition/SimilarValues.pm
 */

public class Partitioner<E>
{
   private List<E> list;
   private long partitionCount;
   private int setSize;
   private int[] restrictedGrowth; // restricted growth
   private int[] subsetCount;
   private int[][] matrix; 
   
   public Partitioner (final Set<E> set)
   {
      list = new ArrayList<E> (set);
      setSize = set.size();
      subsetCount = new int [setSize];
      matrix = new int[setSize][setSize];      
      restrictedGrowth = new int [setSize];
      restrictedGrowth [restrictedGrowth.length - 1] = -1;
      partitionCount = getBellNumber();
   }
   
   public Partition<E> getNextPartition()
   {
      Partition<E> partition = null;
      
      if (getNextMatrix())
      {
         partition = new Partition<E>();
         for (int[] row : matrix)
            if (row[0] >= 0)
            {
               Set<E> subset = new HashSet<E>();
               for (int index : row)
                  if (index >= 0)
                     subset.add (list.get (index));
               partition.add (subset);
            }
      }
      
      return partition;
   }
   
   private boolean getNextMatrix()
   {
      if (getNextRG()) // get the next restrictred growth array}
      {
         // initialize the partition record         
         for (int i = 0; i < setSize; i++)
         {
            for (int j = 0; j < setSize; j++)
               matrix[i][j] = -1;               
            subsetCount[i] = -1;
         }
         
         // elements of RG tell us the subset numbers to which each element belongs
         for (int i = 0; i < setSize; i++)
         {
            int rg = restrictedGrowth[i];
            subsetCount[rg]++;
            matrix[rg][subsetCount[rg]] = i;
         }
         
         return true;
      }
      
      return false;
   }
   
   /*
    * Get next "restricted growth" array, an array of subset indices for 
    * partitioning a set.
    *
    * For example:  if the set is {1,2,3,4} and RG is [0,0,1,2] then
    * the partitioning is {1,2}, {3}, {4}.
    */
   
   private boolean getNextRG()
   {
      boolean result = false;
      int incpos = restrictedGrowth.length - 1;
      
      if (incpos == 0 && restrictedGrowth[0] < 0)
      {
         restrictedGrowth[0] = 0;
         result = true;
      }
      
      while (incpos > 0 && !result)
      {
         int x = restrictedGrowth[incpos] + 1; // get potential next value for RG[incpos]
         int mx = 0;
         // get the max RG value to the left of incpos
         for (int i = 0; i < incpos; i++)
            if (restrictedGrowth[i] > mx)
               mx = restrictedGrowth[i];
         
         // if it's less than max+1 of anything to the left, then use it and
         // set everything to the right to 0            
         if (x <= mx + 1)
         {
            restrictedGrowth[incpos] = x;
            for (int i = incpos + 1, size = restrictedGrowth.length; i < size; i++)
               restrictedGrowth[i] = 0;               
            result = true;
         }
         else
            incpos--; // otherwise back up one position
      }
      
      return result;
   }
   
   public long partitionCount()
   {
      return partitionCount;
   }
   
   /*
    * Based on the Bell triangle description found at
    * http://www.pballew.net/Bellno.html. The numbers can be constructed by
    * using the Bell Triangle, a name suggested to Martin Gardner by Jeffrey
    * Shallit. Start with a row with the number one. Afterward each row
    * begins with the last number of the previous row and continues to the
    * right adding each number to the number above it to get the next number
    * in the row.
    */
   private long getBellNumber()
   {
      long[][] bell = new long[setSize][setSize];
      
      bell[0][0] = 1;
      for (int r = 1; r < setSize; r++)
      {
         bell[0][r] = bell[r - 1][r - 1];
         for (int c = 1; c <= r; c++)
            bell[c][r] = bell[c - 1][r] + bell[c - 1][r - 1];
      }
      
      return bell [setSize - 1][setSize - 1];
   }

   public static void main (final String[] args)
   {
      Set<String> set = new HashSet<String>();
      set.add ("A");
      set.add ("B");
      set.add ("C");
      set.add ("D");
      System.out.println ("Set Size: " + set.size());
      
      Partitioner<String> p = new Partitioner<String> (set);
      System.out.println ("Partitions: " + p.partitionCount());
      
      int i = 1;
      Partition<String> partition;
      while ((partition = p.getNextPartition()) != null)
      {
         System.out.print ((i++) + ") ");
         for (Set<String> subset : partition)
         {
            System.out.print ("{");
            for (String element : subset)
               System.out.print (element + " ");
            System.out.print ("} ");
         }
         System.out.println();
      }
   }
}
