package gui.form;

import java.io.Serializable;

public class Range implements Serializable
{
   private static final long serialVersionUID = 4;

   private double min = Double.NEGATIVE_INFINITY;
   private double max = Double.POSITIVE_INFINITY;

   public Range() { }

   public Range (final double min, final double max)
   {
      setMin (min);
      setMax (max);
   }

   public void setMin (final double min)
   {
      if (min > max)
         throw new ArithmeticException ("Invalid range: " + min + " > " + max);
      this.min = min;
   }

   public void setMax (final double max)
   {
      if (max < min)
         throw new ArithmeticException ("Invalid range: " + max + " < " + min);
      this.max = max;
   }

   public double getMin()
   {
      return min;
   }

   public double getMax()
   {
      return max;
   }

   public boolean includes (final double value)
   {
      return value >= min && value <= max;
   }

   public String getLabel (final String separator)
   {
      if (min == max)
         return Math.round (min) + "";
      else if (max == Integer.MAX_VALUE)
         return Math.round (min) + "%2B"; // %2B is escape code for URL "+"
      return Math.round (min) + separator + Math.round (max);
   }
   
   @Override
   public String toString()
   {
      String s = super.toString() + ": Range is ";
      if (min > Double.NEGATIVE_INFINITY)
         s += "Minimum=" + min + "  ";
      if (max < Double.POSITIVE_INFINITY)
         s += "Maximum=" + max;
      return s;
   }

   /* Returns true if the given value is between low and high (exclusive). */

   public static boolean contains (final double low, final double high, 
                                   final double value)
   {
      return low < value && value < high;
   }

   public static void main (final String[] args)
   {
      Range r = new Range();
      r.setMax (100);
      System.out.println ("range = " + r);
      System.out.println ("  includes (-1)  => " + r.includes (-1d));
      System.out.println ("  includes (0)   => " + r.includes (0d));
      System.out.println ("  includes (1)   => " + r.includes (1d));
      System.out.println ("  includes (99)  => " + r.includes (99d));
      System.out.println ("  includes (100) => " + r.includes (100d));
      System.out.println ("  includes (101) => " + r.includes (101d));
   }
}
