package corpse;

import java.util.Random;

public final class RandomEntry
{
   private static Random random = new Random();

   private RandomEntry()
   {
   }

   public static void randomize()
   {
      random = new Random();
   }

   public static void setSeed(final long seed)
   {
      random.setSeed(seed);
   }

   public static int get(final int max)
   {
      return (int) (random.nextDouble() * max);
   }

   // For best results, the standard deviation should be about 1/3 of the maximum value.
   
   public static int getGaussian(double mean, double stdDev, final int max)
   {
      double x = 0;
      
      while (x < 1 || x > max)
      {
         double v, w;
         do
         {
            v = 2 * random.nextDouble() - 1;
            double v2 = 2 * random.nextDouble() - 1;
            w = v * v + v2 * v2;
         } while (w > 1);
         
         double y = v * Math.sqrt(-2 * Math.log(w) / w);
         x = Math.round(mean + y * stdDev);
      }
      
      return (int) x;
   }

   public static String get(final String tableName, final String subName, String colName, final String filter)
   {
      // System.out.println("RandomEntry.get: T[" + tableName + "] S[" + subName + "] C[" + colName + "] F[" + filter + "]");
      String entry = null;

      Table table = Table.getTable(tableName);
      if (table != null)
      {
         int index = -1;

         if (subName != null)
         {
            Subset subset = table.getSubset(subName);
            if (subset != null)
               index = subset.random() - 1;
         }

         if (filter != null)
         {
            index = -1; // subset is no longer valid since we're going to filter the data

            // Token format: {# Table:Subset.Column#Filter#}
            String token = "{" + tableName;
            if (subName == null && colName == null)
               token += Constants.ALL_CHAR;
            if (subName != null)
               token += Constants.SUBSET_CHAR + subName;
            if (colName != null)
            {
               token += Constants.COLUMN_CHAR + colName;
               colName = null; // don't want to apply it twice
            }
            token += Constants.FILTER_CHAR + filter + Constants.FILTER_CHAR + "}";

            Table filteredTable = Table.TABLES.get(token);
            if (filteredTable == null)
               filteredTable = new SubTable(token); // resolve the table before rolling a value

            if (filteredTable.size() > 0) // if no filtered entries match, just use the normal table?
               table = filteredTable;
         }

         try
         {
            entry = get(table, index, colName, filter);
         }
         catch (IndexOutOfBoundsException x)
         {
            System.err.println(x);
            System.err.println("Table: " + table.getName() + "; Subset: " + subName + "; Column: " + colName + "; Filter: "
                     + filter + "; Entry: " + index);
            x.printStackTrace();
         }
      }

      return entry;
   }

   public static String get(final Table table, int index, final String colName, final String filter)
   {
      String entry = null;

      if (!table.isEmpty())
      {
         if (index < 0)
            index = RandomEntry.get(table.size());

         entry = table.getColumn(index, colName, filter);
         if (entry != null)
         {
            entry = table.resolve(entry, filter);
            // trim leading, trailing, and redundant embedded spaces
            entry = entry.trim(); // TODO .replaceAll("  +", " ");
         }
      }

      return entry;
   }

   public static void main(final String[] args)
   {
      RandomEntry.setSeed(0);
      System.out.println("Random numbers (1-9):");
      for (int i = 0; i < 10; i++)
      {
         for (int j = 0; j < 10; j++)
            System.out.print(RandomEntry.get(9) + 1 + " ");
         System.out.println();
      }
      System.out.println();

      // test RandomEntry.getGaussian()
      RandomEntry.randomize();
      int runs = 1000, range = 10;
      double max = 0, total = 0;
      int[] count = new int[range];
      int mean = 4;
      double stdDev = range / 3;
      
      for (int i = 0; i < runs; i++)
      {
         int r = Math.abs(RandomEntry.getGaussian(mean, stdDev, range));
         max = Math.max(max, r);
         total += r;
         count[r - 1]++;
      }
      
      for (int i = 0; i < range; i++)
         System.out.println((i + 1) + " = " + count[i]);
      System.out.println("Avg (~= " + mean + "): " + (total / runs));
      System.out.println("Max (<= " + range + "): " + max);
      System.out.println();

      CORPSE.init(true);

      // String tableName = "TREASURE";
      // String tableName = "REAGENT";
      String tableName = "INN NAME";
      for (int i = 1; i <= 10; i++)
      {
         String entry = RandomEntry.get(tableName, null, null, null);
         System.out.println(tableName + " " + i + ": " + entry);
      }
      System.out.println();

      String entry = RandomEntry.get("TSR Material", "Wand", null, null);
      System.out.println("Wand: " + entry);
      System.out.println();

      System.out.println("Professions starting with I: " + RandomEntry.get("Profession", null, "Profession", "I.*"));
   }
}
