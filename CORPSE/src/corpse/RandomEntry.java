package corpse;

import java.io.File;
import java.util.Random;

public final class RandomEntry
{
   private static Random random = new Random();

   private RandomEntry() { }
   
   public static void randomize()
   {
      random = new Random();
   }
   
   public static void setSeed (final long seed)
   {
      random.setSeed (seed);
   }
   
   public static int get (final int max)
   {
      return (int) (random.nextDouble() * max);

   }
   
   public static int getExp (final int mean, final int max)
   {
      double d;
      do {
         d = -mean * Math.log (random.nextDouble());
      } while (d >= max);
      return (int) Math.ceil (d);
   }
   
   public static String get (final String tableName, final String subName, String colName)
   {
      String entry = null;
      
      Table table = Table.getTable (tableName);
      if (table != null)
      {
         int index = -1;
         if (subName != null)
         {
            Subset subset = table.getSubset (subName);
            if (subset != null)
            {
               index = subset.random() - 1;
               if (subset.getColumnName() != null && colName == null)
                  colName = subset.getColumnName();
            }
         }
         if (index < 0)
            index = RandomEntry.get (table.size());

         try
         {
            entry = table.getColumn (index, colName);
            if (entry != null)
            {
               entry = table.resolve (entry);
               // trim leading, trailing, and redundant embedded spaces
               entry = entry.trim().replaceAll ("  +", " ");
            }
         }
         catch (IndexOutOfBoundsException x)
         {
            System.err.println (x);
            System.err.println ("Table: " + tableName + "; Subset: " + subName +
                                "; Column:" + colName + "; Entry: " + index);
         }
      }

      return entry;
   }
   
   public static void main (final String[] args)
   {
      RandomEntry.setSeed (0);
      for (int i = 0; i < 10; i++)
      {
         for (int j = 0; j < 10; j++)
            System.out.print (RandomEntry.get (9) + 1 + " ");
         System.out.println();
      }
      System.out.println();
      
      Table.populate (new File ("data/Tables"));

      // String tableName = "TREASURE";
      // String tableName = "REAGENT";
      String tableName = "INN-NAME";
      for (int i = 0; i < 10; i++)
      {
         String entry = RandomEntry.get (tableName, null, null);
         System.out.println (tableName + " " + i + ": " + entry);
      }
      System.out.println();
      
      String entry = RandomEntry.get ("MATERIAL", "Wand", null); 
      System.out.println ("Wand: " + entry);
      System.out.println();

      // test RandomEntry.getExp()
      RandomEntry.randomize();
      int runs = 1000, range = 15;
      double max = 0, total = 0;
      int[] count = new int[range];
      int mean = 4;
      for (int i = 0; i < runs; i++)
      {
         int r = Math.abs (RandomEntry.getExp (mean, range));
         max = Math.max (max, r);
         total += r;
         count[r - 1]++;
      }
      System.out.println ("avg: " + (total / runs));
      for (int i = 0; i < range; i++)
         System.out.println ((i + 1) + " = " + count[i]);
   }
}
