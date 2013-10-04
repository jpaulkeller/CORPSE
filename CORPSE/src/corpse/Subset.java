package corpse;

import java.io.File;
import java.util.regex.Matcher;

public class Subset
{
   private String name;
   private String roll;
   private String columnName;
   
   public Subset (final Table table, final String entry)
   {
      Matcher m = Constants.SUBSET_PATTERN.matcher (entry);
      if (m.find())
      {
         name       = m.group (1);
         roll       = m.group (2);
         columnName = m.group (3);
      }
      else
         System.err.println ("Invalid subset: " + entry);
   }

   public int random()
   {
      return Macros.resolveNumber (roll);
   }

   public int getMin()
   {
      return Macros.getMin (roll);
   }
   
   public int getMax()
   {
      return Macros.getMax (roll);
   }
   
   public String getName()
   {
      return name;
   }

   public String getColumnName()
   {
      return columnName;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append (name + " " + getMin() + "-" + getMax());
      if (columnName != null)
         sb.append (" " + columnName);
      return sb.toString();
   }
   
   public static void main (final String[] args)
   {
      Table.populate (new File ("data/Tables"));
      
      for (Table table : Table.getTables())
      {
         table.importTable();
         if (!table.subsets.isEmpty())
         {
            System.out.println (table);
            for (Subset subset : table.subsets.values())
               System.out.println("  > " + subset + " = " + 
                     Macros.resolve("{" + table.getName() + Constants.SUBSET_CHAR + subset.getName() + "}", null));
         }
      }
   }
}
