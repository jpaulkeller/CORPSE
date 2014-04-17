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
      String getRegex();
      Matcher getMatcher();
      void setToken(final String token);
      int get();
      int getMin();
      int getMax();
      String resolve();
   }

   abstract static class NumericAdapter implements Numeric
   {
      private String regex;
      private Pattern pattern;
      private Matcher matcher;

      public NumericAdapter(final String regex)
      {
         this.regex = "^" + regex + "$";
         this.pattern = CORPSE.safeCompile("Invalid numeric pattern", this.regex);
      }

      @Override
      public String getRegex()
      {
         return regex;
      }

      @Override
      public void setToken(final String token)
      {
         this.matcher = pattern.matcher(token);
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
         int from = Integer.parseInt(getMatcher().group(1));
         int to = Integer.parseInt(getMatcher().group(2));
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

   static class Formula extends NumericAdapter // {=#+#}
   {
      public Formula()
      {
         super("\\{= *(\\d+) *([-+*/^]) *(\\d+(?:[.]\\d+)?)\\}");
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
         int i = Integer.parseInt(getMatcher().group(1));
         float j = Float.parseFloat(getMatcher().group(3));
         String operator = getMatcher().group(2); 
         if (operator.equals("+"))
            return i + (int) j;
         else if (operator.equals("-"))
            return i - (int) j;
         else if (operator.equals("*"))
            return i * (int) j;
         else if (operator.equals("/"))
            return Math.round(i / j);
         else if (operator.equals("^"))
            return (int) Math.round(Math.pow(i, j));
         return 1;
      }
   }

   static class Dice extends NumericAdapter // {#d#+#} The extra +# is optional. Operators: +, -, *, /
   {
      public Dice()
      {
         super("\\{(\\d+)? *[dD] *(\\d+)(?:([-+*/])(\\d+))?\\}");
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

         String operator = m.group(3);
         if (operator != null)
         {
            int bonus = Integer.parseInt(m.group(4));
            if (operator.equals("+"))
               roll += bonus;
            else if (operator.equals("-"))
               roll -= bonus;
            else if (operator.equals("*"))
               roll *= bonus;
            else if (operator.equals("/"))
               roll /= bonus;
         }
         return roll;
      }

      @Override
      public int getMin()
      {
         Matcher m = getMatcher();
         int count = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
         int min = count;

         String operator = m.group(3);
         if (operator != null)
         {
            int bonus = Integer.parseInt(m.group(4));
            min += operator.equals("+") ? bonus : -bonus;
         }

         return min;
      }

      @Override
      public int getMax()
      {
         Matcher m = getMatcher();
         int count = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
         int sides = Integer.parseInt(m.group(2));
         int max = count * sides;

         String operator = m.group(3);
         if (operator != null)
         {
            int bonus = Integer.parseInt(m.group(4));
            max += operator.equals("+") ? bonus : -bonus;
         }

         return max;
      }
   }

   // Many games use the concept of an open roll. This is a roll that has no upper limit.
   // They work by rolling a die, and if the maximum value for the die comes up, you add
   // that value to another roll of the same die. If the second roll comes up at the maximum,
   // you add and roll again, and so on.

   static class Open extends NumericAdapter // {#t#}
   {
      public Open()
      {
         super("\\{(\\d+)? *[tT] *(\\d+)}");
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
            if (die != sides)
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

   // "Best of" rolls. For example, 3/4d6 would roll 4 dice, and total the best 3 rolls.

   static class Best extends NumericAdapter // {#/#d#}
   {
      public Best()
      {
         super("\\{(\\d+)/(\\d+) *[dD] *(\\d+)}");
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
   private static final Numeric ROLL = new Roll();
   private static final Numeric PERCENT = new Percent();
   private static final Numeric RANGE = new Range();
   private static final Numeric FORMULA = new Formula();
   private static final Numeric DICE = new Dice();
   private static final Numeric OPEN = new Open();
   private static final Numeric BEST = new Best();
   private static final Numeric NORM = new Norm();
   private static final Numeric CHARGES = new Charges();

   private static List<Numeric> numerics = new ArrayList<Numeric>();
   static
   {
      numerics.add(CONSTANT);
      numerics.add(ROLL);
      numerics.add(PERCENT);
      numerics.add(RANGE);
      numerics.add(FORMULA);
      numerics.add(DICE);
      numerics.add(OPEN);
      numerics.add(BEST);
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
         if (n.getMatcher().matches())
            return new Quantity(token);
      }
      return null;
   }

   private Numeric numeric;

   public Quantity(final String token)
   {
      for (Numeric n : numerics)
      {
         n.setToken(token);
         if (n.getMatcher().matches())
         {
            numeric = n; // found a match
            return;
         }
      }
      throw new IllegalArgumentException("Invalid QUANTITY token: " + token);
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

   public static Numeric startsWith(final String line)
   {
      int brk = line.indexOf(' ');
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

   public static void main(final String[] args)
   {
      List<String> tokens = new ArrayList<String>();
      tokens.add("invalid");
      tokens.add("5"); // CONSTANT (not a token)
      
      tokens.add("{5}"); // ROLL
      tokens.add("{%}"); // PERCENT
      tokens.add("{10-20}"); // RANGE
      
      tokens.add("{=5+10}"); // FORMULA
      tokens.add("{=10-3}"); // FORMULA
      tokens.add("{=3*2}"); // FORMULA
      tokens.add("{=12/2}"); // FORMULA
      tokens.add("{=5^2}"); // FORMULA (square)
      tokens.add("{=100^0.5}"); // FORMULA (square root)

      tokens.add("{3d6}"); // DICE
      tokens.add("{d12}"); // DICE
      tokens.add("{2d4+3}"); // DICE

      tokens.add("{3t6}"); // OPEN
      tokens.add("{3/4d6}"); // BEST
      tokens.add("{5,10}"); // NORM
      tokens.add("{#/100}"); // CHARGES

      for (String token : tokens)
      {
         Quantity qty = Quantity.getQuantity(token);
         if (qty != null)
         {
            String type = qty.numeric.getClass().getName().substring("corpse.Quantity$".length());
            
            int total = 0;
            for (int i = 0; i < 1000; i++)
               total += qty.get();
            int average = Math.round(total / 1000f);
               
            System.out.println(token + " " + type + " = " + qty.resolve() + 
                               " (avg = " + average + ", max = " + qty.getMax() + ")");
         }
         else
            System.out.println("Invalid: " + token);
      }
   }
}
