package corpse;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Subset
{
   // TODO simplify range format
   // : SubsetName {Quantity} ColumnName
   // : SubsetName {to} ColumnName
   // : SubsetName {from-to} ColumnName
   private static final String RANGE_TOKEN = "(\\{[^}]+\\})";
   private static final String SUBSET_REGEX = "^\\" + Macros.SUBSET_CHAR + " *" + Macros.NAME + " +" 
         + RANGE_TOKEN + "(?: +" + Column.NAME + ")?"; 
   static final Pattern SUBSET_PATTERN = Pattern.compile (SUBSET_REGEX, Pattern.CASE_INSENSITIVE);

   static final Pattern SUBSET_REF = // {Table:Subset}
         Pattern.compile (Macros.NAME + Macros.SUBSET_CHAR + Macros.NAME + "?", Pattern.CASE_INSENSITIVE);
   
   private String name;
   private String roll;
   private String columnName;
   
   public Subset (final Table table, final String entry)
   {
      Matcher m = SUBSET_PATTERN.matcher (entry);
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
      
      for (String name : new ArrayList<String>(Table.tables.keySet()))
      {
         Table table = Table.getTable (name);
         if (!table.subsets.isEmpty())
         {
            System.out.println (table);
            for (Subset subset : table.subsets.values())
               System.out.println("  > " + subset + " = " + Macros.resolve("{" + name + Macros.SUBSET_CHAR + subset.getName() + "}", null));
         }
      }
   }
}
