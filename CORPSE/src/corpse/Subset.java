/*
 * The Subset class represents a subset of tables rows.  There are several ways to define a subset, but only one
 * format should be used within any file.
 * 
 * 1) Simply use a header such as ": Common".  The subset will included the following rows until the next subset (or EOF).
 * 
 * 2) Define exactly which rows you want, such as ": Rare {98-100}".  This is necessary in some cases, but should be avoided
 *    since it's hard to maintain.
 *  
 * 3) Define a roll, such as ": Title {2d10}".  This allows bell-curves when selecting a random row from the subset.
 * 
 * 4) Combine subsets such as ": Metal = Alloy + Normal + Precious" (where all names are non-combined subsets in that table).
 */

package corpse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Subset
{
   private static final String SC = Constants.SUBSET_CHAR;
   private static final String SN = Constants.NAME;
   
   private static final String RANGE_TOKEN = "(\\{[^}]+\\})"; // should be a valid roll
   
   // : Name
   // : Name {1-10)
   private static final String SUBSET_REGEX = "^\\" + SC + " *" + SN + "(?: +" + RANGE_TOKEN + ")?";
   private static final Pattern SUBSET = Pattern.compile (SUBSET_REGEX, Pattern.CASE_INSENSITIVE);
   
   // : Name = name + name + name (up to 20)
   private static final String META_SUBSET_REGEX = "^\\" + SC + " *" + SN + " *= *(" + SN + "(?: *\\+ *" + SN + "){0,20})";
   private static final Pattern COMPOSITE_SUBSET = Pattern.compile (META_SUBSET_REGEX, Pattern.CASE_INSENSITIVE);

   // {Table:Subset}
   // private static final Pattern SUBSET_REF = Pattern.compile (Constants.NAME + SC + SN + "?", Pattern.CASE_INSENSITIVE);
   
   private String name;
   private String roll;
   private int min, max;
   
   private List<String> composites = new ArrayList<String>();
   
   public static void parse(final Table table, final String entry)
   {
      Subset subset = null;
      
      Matcher m;
      if ((m = COMPOSITE_SUBSET.matcher (entry)).find())
      {
         subset = new Subset();
         subset.name = m.group (1);
         subset.min = Integer.MAX_VALUE; // set in finish()
         subset.max = Integer.MIN_VALUE; // set in finish()

         Matcher nameMatcher = Constants.NAME_PATTERN.matcher(m.group(2));
         while (nameMatcher.find())
            subset.composites.add(nameMatcher.group(1));
         table.addSubset(subset);
      }
      else if ((m = SUBSET.matcher (entry)).find())
      {
         subset = new Subset();
         subset.name = m.group (1);
         if (m.group(2) != null)
            subset.setRoll(m.group (2));
         else // support embedded subsets
         {
            Subset.closeSubset(table);
            subset.min = table.size() + 1;
            // max will be set when the next subset is read (or EOF)
         }
         table.addSubset(subset);
      }
      else
         System.err.println ("Invalid subset in " + table.getFile() + ": " + entry);
   }
   
   void setRoll(final String roll)
   {
      this.roll = roll;
      min = Macros.getMin (roll);
      max = Macros.getMax (roll);
   }

   static void finish(final Table table)
   {
      closeSubset(table);
      
      for (Subset s : table.getSubsets().values())
         if (!s.composites.isEmpty())
         {
            for (String composite : s.composites)
            {
               Subset child = table.getSubset(composite);
               if (child != null)
               {
                  s.min = Math.min(s.min, child.min);
                  s.max = Math.max(s.max, child.max);
               }
            }
            s.setRoll("{" + s.min + "-" + s.max + "}");
         }
   }
   
   private static void closeSubset(final Table table) // close any "open" subset
   {
      for (Subset s : table.getSubsets().values())
         if (s.getMax() == 0) // should be 1 at most
            s.setRoll("{" + s.getMin() + "-" + table.size() + "}");
   }
   
   public int random()
   {
      return Macros.resolveNumber (roll);
   }

   public int getMin()
   {
      return min;
   }
   
   public int getMax()
   {
      return max;
   }
   
   public String getName()
   {
      return name;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append (name + " " + roll + " ");
      for (String composite : composites)
         sb.append(composite + " + ");
      return sb.toString();
   }
   
   public static void main (final String[] args)
   {
      Table.populate (new File ("data/Tables"));

      for (Table table : Table.getTables())
      {
         table.importTable();
         if (!table.getSubsets().isEmpty())
         {
            System.out.println (table);
            for (Subset subset : table.getSubsets().values())
            {
               String token = "{" + table.getName() + Constants.SUBSET_CHAR + subset.getName() + "}";
               System.out.println("  > " + token + " = " + Macros.resolve(token));
            }
         }
      }
   }
}
