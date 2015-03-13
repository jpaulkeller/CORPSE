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
 * 4) Composite subsets such as ": Metal = Alloy + Normal + Precious" (where all names are non-composite subsets in that table).
 * 
 * 5) Filter subsets, such as ": Military = .*military.*".
 * 
 * To reference a subset, you would use {Table:Subset} -- or just {:Subset} within the table, as a shortcut. 
 */

package corpse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Roll
{
   private static final String RC = Constants.ROLL_CHAR;
   private static final String RN = Constants.NAME;

   // : Name {2d6}
   public static final String QTY_TOKEN = "(\\{[-0-9dt% +*x/^.,{}]+\\})";
   // TODO use Quantity.REGEX
   public static final String ROLL_REGEX = "^\\" + RC + "\\s*" + RN + "\\s*" + QTY_TOKEN + Constants.COMMENT;
   private static final Pattern ROLL = Pattern.compile(ROLL_REGEX, Pattern.CASE_INSENSITIVE);

   private String name;
   private Quantity quantity;

   public static void parse(final Table table, final String entry)
   {
      Matcher m;
      if ((m = ROLL.matcher(entry)).find())
      {
         Roll roll = new Roll();
         roll.name = m.group(1);
         roll.quantity = Quantity.getQuantity(m.group(2));
         System.out.println("ROLL: " + roll); //TODO
         table.addRoll(roll);
      }
      else
         System.err.println("Invalid roll in " + table.getFile() + ": " + entry);
   }

   public String getName()
   {
      return name;
   }

   public int random()
   {
      return quantity.get();
      // return Macros.resolveNumber(rollToken); TODO
   }

   @Override
   public String toString()
   {
      return name + " " + quantity;
   }

   public static void main(final String[] args)
   {
      CORPSE.init(true);

      String test = "Condition";
      Table table = Table.getTable(test); 
      System.out.println (table);
      for (Roll roll : table.getRolls().values())
      {
         String token = "{" + table.getName() + Constants.ROLL_CHAR + roll.getName() + "}";
         for (int i = 0; i < 10; i++)
         {
            int index = roll.random();
            System.out.println("  > " + token + " = (" + index + ") " + table.get(index));
            // System.out.println("  > " + token + " = " + Macros.resolve(table.getName(), token, null));
         }
      }
      
      /*
      for (Table table : Table.getTables())
      {
         table.importTable();
         if (!table.getRolls().isEmpty())
         {
            System.out.println(table);
            for (Roll roll : table.getRolls().values())
            {
               String token = "{" + table.getName().toLowerCase() + Constants.ROLL_CHAR + roll.getName() + "}";
               int index = roll.random();
               System.out.println("  > " + token +  " (" + index + ") = " + table.get(index));
            }
         }
      }
      */
   }
}
