package corpse;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Subset
{
   static final Pattern PATTERN = 
      Pattern.compile ("\\" + Macros.SUBSET_CHAR + ".*");
   private static final Pattern SUBSET_PATTERN =
      Pattern.compile ("\\" + Macros.SUBSET_CHAR + 
                       " *([A-Za-z_]+) +(\\{[^}]+\\})(?: +" + Column.NAME + ")?");

   private String name;
   private String roll;
   private String columnName;
   
   public Subset (final String entry)
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
      sb.append (name + " " + roll);
      if (columnName != null)
         sb.append (" " + columnName);
      return sb.toString();
   }
   
   public static void main (final String[] args)
   {
      Table.populate (new File ("data/Tables"));
      
      for (String name : Table.tables.keySet())
      {
         Table table = Table.getTable (name);
         if (!table.subsets.isEmpty())
            System.out.println (table);
      }
   }
}