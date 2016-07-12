package corpse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Quantity
{
   interface Numeric
   {
      String getToken();
      void setToken(final String token);
      boolean matches();
      String getRegex();
      Matcher getMatcher();
      int get();
      int getMin();
      int getMax();
      String resolve();
   }

   static abstract class NumericAdapter implements Numeric
   {
      private String token;
      private String regex;
      private Pattern pattern;
      private Matcher matcher;

      public NumericAdapter(final String regex)
      {
         this.regex = regex;
         this.pattern = CORPSE.safeCompile("Invalid numeric pattern", "^" + regex + "$");
      }

      @Override
      public void setToken(final String token)
      {
         this.token = token;
         this.matcher = pattern.matcher(token);
      }
      
      @Override
      public String getToken()
      {
         return this.token;
      }

      @Override
      public boolean matches()
      {
         return getMatcher().matches();
      }

      @Override
      public String getRegex()
      {
         return regex;
      }

      @Override
      public Matcher getMatcher()
      {
         return matcher;
      }

      @Override
      public String resolve()
      {
         return "" + get(); // default implementation just returns the value
      }
   }

   static class Constant extends NumericAdapter // #
   {
      public Constant()
      {
         super("(\\d+)");
      }

      @Override
      public int get()
      {
         return Integer.parseInt(getMatcher().group(1));
      }

      @Override
      public int getMin()
      {
         return get();
      }

      @Override
      public int getMax()
      {
         return get();
      }
   }

   static class ConstantRange extends NumericAdapter // #-#
   {
      public ConstantRange()
      {
         super("(\\d+)[-](\\d+)");
      }

      @Override
      public int get()
      {
         int from = getMin();
         int to = getMax();
         return to - from + 1;
      }

      @Override
      public int getMin()
      {
         return Integer.parseInt(getMatcher().group(1));
      }

      @Override
      public int getMax()
      {
         return Integer.parseInt(getMatcher().group(2));
      }
   }

   static class Roll extends NumericAdapter // {#} same as d# or 1d#
   {
      public Roll()
      {
         super("\\{(\\d+)\\}");
      }

      @Override
      public int get()
      {
         return RandomEntry.get(getMax()) + 1;
      }

      @Override
      public int getMin()
      {
         return 1;
      }

      @Override
      public int getMax()
      {
         return Integer.parseInt(getMatcher().group(1));
      }
   }

   static class Percent extends NumericAdapter // {%} (same as d100)
   {
      public Percent()
      {
         super("\\{%\\}");
      }

      @Override
      public int get()
      {
         return RandomEntry.get(getMax()) + 1;
      }

      @Override
      public int getMin()
      {
         return 1;
      }

      @Override
      public int getMax()
      {
         return 100;
      }
   }

   static class Range extends NumericAdapter // {#-#}
   {
      public Range()
      {
         super("\\{(\\d+) *- *(\\d+)\\}");
      }

      @Override
      public int get()
      {
         int from = getMin();
         int to = getMax();
         int range = to - from + 1;
         return RandomEntry.get(range) + from;
      }

      @Override
      public int getMin()
      {
         return Integer.parseInt(getMatcher().group(1));
      }

      @Override
      public int getMax()
      {
         return Integer.parseInt(getMatcher().group(2));
      }
   }

   private static final String OPERATOR = "([-+*/^])";
   private static final String FLOAT = "(\\d+(?:[.]\\d+)?)";
   
   static class Formula extends NumericAdapter // {#oper#}
   {
      
      public Formula()
      {
         super("\\{\\s*" + FLOAT + "\\s*" + OPERATOR + "\\s*" + FLOAT + "\\s*\\}");
      }

      @Override
      public int get()
      {
         return getMax();
      }

      @Override
      public int getMin()
      {
         return 1;
      }

      @Override
      public int getMax()
      {
         float i = Float.parseFloat(getMatcher().group(1));
         float j = Float.parseFloat(getMatcher().group(3));
         String operator = getMatcher().group(2);
         
         if (operator.equals("+"))
            return Math.round(i + j);
         else if (operator.equals("-"))
            return Math.round(i - j);
         else if (operator.equals("*") || operator.equals("x"))
            return Math.round(i * j);
         else if (operator.equals("/"))
            return Math.round(i / j);
         else if (operator.equals("^"))
            return Math.round((float) Math.pow(i, j));
         return 1;
      }
   }

   static class Dice extends NumericAdapter // {#d#+#} The extra +# is optional. Operators: +, -, *, x, /
   {
      public Dice()
      {
         super("\\{(\\d+)? *[dD] *(\\d+)(?:([-+*x/])(\\d+))?\\}");
      }

      @Override
      public int get()
      {
         Matcher m = getMatcher();
         int roll = 0;
         int count = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
         int sides = Integer.parseInt(m.group(2));
         for (int i = 0; i < count; i++)
            roll += RandomEntry.get(sides) + 1;
         return calc(roll, m);
      }

      @Override
      public int getMin()
      {
         Matcher m = getMatcher();
         int count = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
         int min = count;
         return calc(min, m);
      }

      @Override
      public int getMax()
      {
         Matcher m = getMatcher();
         int count = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
         int sides = Integer.parseInt(m.group(2));
         int max = count * sides;
         return calc(max, m); 
      }
      
      private int calc(final int roll, final Matcher m)
      {
         final String operator= m.group(3);         
         if (operator != null && m.groupCount() == 4)
         {
            int bonus = Integer.parseInt(m.group(4));
            if (operator.equals("+"))
               return roll + bonus;
            else if (operator.equals("-"))
               return roll - bonus;
            else if (operator.equals("*") || operator.equals("x"))
               return roll * bonus;
            else if (operator.equals("/"))
               return roll / bonus;
         }
         return roll;
      }
   }

   // Many games use the concept of an exploding or open roll. This is a roll that has no upper limit.
   // They work by rolling a die, and if the maximum value for the die comes up, you add
   // that value to another roll of the same die. If the second roll comes up at the maximum,
   // you add and roll again, and so on.

   static class Exploding extends NumericAdapter // {#x#}
   {
      public Exploding()
      {
         this("\\{(\\d+)? *[xX] *(\\d+)}");
      }

      public Exploding(final String regex)
      {
         super(regex);
      }

      @Override
      public int get()
      {
         Matcher m = getMatcher();
         int roll = 0;
         int count = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
         int sides = Integer.parseInt(m.group(2));
         while (count > 0)
         {
            int die = RandomEntry.get(sides) + 1;
            roll += die;
            if (die != sides) // if not max, decrement roll count, else "explode" (roll again)
               count--;
         }
         return roll;
      }

      @Override
      public int getMin()
      {
         Matcher m = getMatcher();
         int count = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
         return count;
      }

      @Override
      public int getMax()
      {
         Matcher m = getMatcher();
         System.err.println("Warning: there is no maximum for open rolls like: " + m.group());
         int count = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
         int sides = Integer.parseInt(m.group(2));
         return count * sides; // return the max if this was a normal (not open) roll
      }
   }

   // Hackmaster uses "penetrating" dice.  Just like exploding dice, but the extra dice get -1.

   static class Penetrating extends Exploding // {#p#}
   {
      public Penetrating()
      {
         super("\\{(\\d+)? *[pP] *(\\d+)}");
      }

      @Override
      public int get()
      {
         Matcher m = getMatcher();
         int roll = 0;
         int count = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
         int sides = Integer.parseInt(m.group(2));
         int extra = 0;
         for (int i = 0; i < count; i++)
         {
            int die = RandomEntry.get(sides) + 1;
            roll += die;
            if (die == sides) // penetrate (increment extra to roll again)
               extra++;
         }
         while (extra > 0)
         {
            int die = RandomEntry.get(sides) + 1;
            roll += die - 1;
            if (die != sides)
               extra--;
         }
         return roll;
      }
   }

   // Keep "best of" rolls. For example, 3/4d6 would roll 4 dice, and total the best 3 rolls.

   static class Keep extends NumericAdapter // {#/#d#}
   {
      public Keep()
      {
         super("\\{(\\d+)/(\\d+) *[dD] *(\\d+)}");
      }

      @Override // to differentiate with Drop
      public boolean matches()
      {
         Matcher m = getMatcher();
         if (m.matches())
         {
            int keep = Integer.parseInt(m.group(1));
            int roll = Integer.parseInt(m.group(2));
            return keep <= roll;
         }
         return false;
      }

      @Override
      public int get()
      {
         Matcher m = getMatcher();
         int keep = Integer.parseInt(m.group(1));
         int roll = Integer.parseInt(m.group(2));
         int sides = Integer.parseInt(m.group(3));

         List<Integer> sorted = new ArrayList<Integer>();
         for (int i = 0; i < roll; i++)
            sorted.add(RandomEntry.get(sides) + 1);
         Collections.sort(sorted);

         int total = 0;
         for (int i = roll - keep; i < roll; i++)
            total += sorted.get(i);
         return total;
      }

      @Override
      public int getMin()
      {
         Matcher m = getMatcher();
         return Integer.parseInt(m.group(1)); // keep
      }

      @Override
      public int getMax()
      {
         Matcher m = getMatcher();
         int count = Integer.parseInt(m.group(1));
         int sides = Integer.parseInt(m.group(3));
         return count * sides;
      }
   }

   // Drop worst rolls. For example, 4/1d6 would roll 4 dice, drop the worst one, and total the best 3 rolls.

   static class Drop extends NumericAdapter // {#/#d#}
   {
      public Drop()
      {
         super("\\{(\\d+)/(\\d+) *[dD] *(\\d+)}");
      }

      @Override // to differentiate with Keep
      public boolean matches()
      {
         Matcher m = getMatcher();
         if (m.matches())
         {
            int roll = Integer.parseInt(m.group(1));
            int drop = Integer.parseInt(m.group(2));
            return roll > drop;
         }
         return false;
      }

      @Override
      public int get()
      {
         Matcher m = getMatcher();
         int roll = Integer.parseInt(m.group(1));
         int drop = Integer.parseInt(m.group(2));
         int sides = Integer.parseInt(m.group(3));

         List<Integer> sorted = new ArrayList<Integer>();
         for (int i = 0; i < roll; i++)
            sorted.add(RandomEntry.get(sides) + 1);
         Collections.sort(sorted);

         int total = 0;
         for (int i = drop; i < roll; i++)
            total += sorted.get(i);
         return total;
      }

      @Override
      public int getMin()
      {
         Matcher m = getMatcher();
         int roll = Integer.parseInt(m.group(1));
         int drop = Integer.parseInt(m.group(2));
         return roll - drop;// keep
      }

      @Override
      public int getMax()
      {
         Matcher m = getMatcher();
         int roll = Integer.parseInt(m.group(1));
         int drop = Integer.parseInt(m.group(2));
         int sides = Integer.parseInt(m.group(3));
         return (roll - drop) * sides;
      }
   }

   // Generate a random number using normal distribution with a mean
   // of "m" and a max of "M". See TitleMilitary.tbl for example.

   static class Norm extends NumericAdapter // {m,M}
   {
      public Norm()
      {
         super("\\{(\\d+), *(\\d+)\\}");
      }

      @Override
      public int get()
      {
         Matcher m = getMatcher();
         int mean = Integer.parseInt(m.group(1));
         int max = Integer.parseInt(m.group(2));
         double stdDev = max / 3.0;
         return RandomEntry.getGaussian(mean, stdDev, max);
      }

      @Override
      public int getMin()
      {
         return 1;
      }

      @Override
      public int getMax()
      {
         return Integer.parseInt(getMatcher().group(2));
      }
   }

   // {#/max} for magic-item charges where max is random (e.g., #/{10-20})

   static class Charges extends NumericAdapter
   {
      public Charges()
      {
         super("\\{#/(\\d+)\\}");
      }

      @Override
      public int get()
      {
         return RandomEntry.get(getMax() + 1);
      }

      @Override
      public int getMin()
      {
         return 0;
      }

      @Override
      public int getMax()
      {
         return Integer.parseInt(getMatcher().group(1));
      }

      @Override
      public String resolve()
      {
         return get() + "/" + getMax();
      }
   }

   private static final Numeric CONSTANT = new Constant();
   private static final Numeric CONSTANT_RANGE = new ConstantRange();
   private static final Numeric ROLL = new Roll();
   private static final Numeric PERCENT = new Percent();
   private static final Numeric RANGE = new Range();
   private static final Numeric FORMULA = new Formula();
   private static final Numeric DICE = new Dice();
   private static final Numeric EXPLODING = new Exploding();
   private static final Numeric PENETRATING = new Penetrating();
   private static final Numeric KEEP = new Keep();
   private static final Numeric DROP = new Drop();
   private static final Numeric NORM = new Norm();
   private static final Numeric CHARGES = new Charges();

   private static List<Numeric> numerics = new ArrayList<>();
   static
   {
      numerics.add(CONSTANT);
      numerics.add(CONSTANT_RANGE);
      numerics.add(ROLL);
      numerics.add(PERCENT);
      numerics.add(RANGE);
      numerics.add(FORMULA);
      numerics.add(DICE);
      numerics.add(EXPLODING);
      numerics.add(PENETRATING);
      numerics.add(KEEP);
      numerics.add(DROP);
      numerics.add(NORM);
      numerics.add(CHARGES);
   }

   public static final String REGEX;
   static
   {
      StringBuilder s = new StringBuilder();
      s.append("(?:");
      Iterator<Numeric> iter = numerics.iterator();
      s.append(iter.next().getRegex());
      while (iter.hasNext())
         s.append("|" + iter.next().getRegex());
      s.append(")");
      REGEX = s.toString();
   }

   public static Quantity getQuantity(final String token)
   {
      for (Numeric n : numerics)
      {
         n.setToken(token);
         if (n.matches())
            return new Quantity(n);
      }
      return null;
   }

   private Numeric numeric;

   private Quantity(final Numeric n)
   {
      try
      {
         numeric = n.getClass().newInstance();
         numeric.setToken(n.getToken());
         numeric.matches();
      }
      catch (InstantiationException | IllegalAccessException x)
      {
         throw new IllegalArgumentException("Invalid QUANTITY token: " + n.getToken());
      }
      return;
   }

   /** Resolve the expression, and return it as an int. */

   public int get()
   {
      return numeric.get();
   }

   /** Resolve the expression, and return it as a string. */

   public String resolve()
   {
      return numeric.resolve();
   }

   public int getMin()
   {
      return numeric.getMin();
   }

   public int getMax()
   {
      return numeric.getMax();
   }

   public static boolean isNumeric(final String token)
   {
      for (Numeric n : numerics)
      {
         n.setToken(token);
         if (n.getMatcher().matches())
            return true;
      }
      return false;
   }

   public static Numeric startsWith(final String line)
   {
      int brk = line.indexOf(Constants.WEIGHT);
      if (brk > 0)
      {
         String token = line.substring(0, brk); // first word
         for (Numeric n : numerics)
         {
            n.setToken(token);
            if (n.getMatcher().find())
               return n;
         }
      }
      return null;
   }

   @Override
   public String toString()
   {
      String type = numeric.getClass().getName().substring("corpse.Quantity$".length());
      
      int total = 0;
      for (int i = 0; i < 1000; i++)
         total += get();
      int average = Math.round(total / 1000f);
      
      return numeric.getToken() + " " + type + " (min=" + getMin() + " avg=" + average + ", max=" + getMax() + ")";
   }

   public static void main(final String[] args)
   {
      List<String> tokens = new ArrayList<>();
      tokens.add("invalid");
      tokens.add("5"); // CONSTANT (not a token)
      tokens.add("6-10"); // CONSTANT range (not a token)
      
      tokens.add("{5}"); // ROLL
      tokens.add("{%}"); // PERCENT
      tokens.add("{10-20}"); // RANGE
      
      tokens.add("{5+10}"); // FORMULA
      tokens.add("{10-3}"); // FORMULA
      tokens.add("{3*2}"); // FORMULA
      tokens.add("{12/2}"); // FORMULA
      tokens.add("{5^2}"); // FORMULA (square)
      tokens.add("{100^0.5}"); // FORMULA (square root)

      tokens.add("{3d6}"); // DICE
      tokens.add("{d12}"); // DICE
      tokens.add("{d4*5}"); // DICE
      tokens.add("{2d4+3}"); // DICE
      tokens.add("{3x6}"); // EXPLODING/OPEN
      tokens.add("{3p6}"); // PENTRATING
      tokens.add("{3/4d6}"); // KEEP BEST
      tokens.add("{4/2d6}"); // DROP WORST
      tokens.add("{5,10}"); // NORM
      tokens.add("{5,10+10}"); // NORM OFFSET
      tokens.add("{#/100}"); // CHARGES

      for (String token : tokens)
      {
         Quantity qty = Quantity.getQuantity(token);
         if (qty != null)
            System.out.println(qty);
         else
            System.out.println("Invalid: " + token);
      }
   }
}
