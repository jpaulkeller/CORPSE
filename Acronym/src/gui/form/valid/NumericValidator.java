package gui.form.valid;

import gui.form.Range;
import str.Token;

/**
 * Validator for numeric data.  This is a simple validator which
 * ensures the data is numeric.
 *
 * Note: If you only want to allow integral or positive data, you can
 * use the TextFilter getFilter method to prevent entry of decimal
 * points or negative signs.
 *
 * @see TextFilter#getFilter(String)
 */

public class NumericValidator extends ValidationAdapter
{
   private static final long serialVersionUID = 6;

   private Range range; // optional range constraint

   /**
    * No-arg constructor.  Caller must use one of the set methods
    * below to initialize. */

   public NumericValidator() { }

   public NumericValidator (final double min, final double max)
   {
      setRange (new Range (min, max));
   }

   public void setRange (final Range range)
   {
      this.range = range;
   }

   public Range getRange()
   {
      return range;
   }

   /**
    * Interpret the given arguments String as a value range, separated
    * by a colon: "{min}:{max}".  Only one value is required (but the
    * colon is always required).  Any non-empty non-numeric string may
    * be used to indicate no limit (in place of the value).
    *
    * For example: "-:100", "0:-", "0:21" */

   @Override
   public boolean initialize (final String arguments)
   {
      boolean ok = true;

      if (arguments != null)
      {
         String[] limits = Token.tokenize (arguments, ":");
         if (limits.length == 2)
            ok = setRange (limits[0], limits[1]);
      }

      return ok;
   }

   public boolean setRange (final String minimum, final String maximum)
   {
      int problems = 0;

      if (range == null)
         range = new Range();

      try
      {
         if (minimum != null)
         {
            try
            {
               range.setMin (Double.parseDouble (minimum));
            }
            catch (NumberFormatException e)
            {
               problems++;
               range.setMin (Double.NEGATIVE_INFINITY);
            }
         }
         if (maximum != null)
         {
            try
            {
               range.setMax (Double.parseDouble (maximum));
            }
            catch (NumberFormatException e)
            {
               problems++;
               range.setMax (Double.POSITIVE_INFINITY);
            }
         }
      }
      catch (ArithmeticException e) // min > max
      {
         System.err.println (e.getMessage());
         range = null;
         problems += 2;
      }

      return problems <= 1;   // one end of range is optional
   }

   /**
    * Returns true if the value is valid.
    *
    * @param value The input text */

   @Override
   public boolean isValid (final Object value)
   {
      boolean ok;

      if (value instanceof Number)
         ok = range == null || range.includes (((Number) value).doubleValue());
      else if (value != null && !value.toString().trim().equals (""))
      {
         try
         {
            Double dbl = new Double (value.toString());
            ok = range == null || range.includes (dbl.doubleValue());
         }
         catch (NumberFormatException e)
         {
            ok = false;
         }
      }
      else
         ok = isNullValid();      // default for null value

      return ok;
   }

   private void test (final String value)
   {
      System.out.print ("  " + value + " ");
      if (isValid (value))
         System.out.println ("is valid");
      else
         System.out.println ("is not valid");
   }

   static void main (final String[] args) // for testing
   {
      NumericValidator v = new NumericValidator();
      if (args.length > 0)
         v.test (args[0]);
      else                      // default test cases
      {
         v.test ("abc");
         v.test ("123");
         v.test ("-1");
         v.test ("1-");
         v.test ("1.");
         v.test (".1");
         v.test ("a1");
         v.test ("1a");
         v.test ("-1.2");

         System.out.println();
         v.initialize ("-:52");
         System.out.println (v.getRange());
         v.initialize ("21:-");
         System.out.println (v.getRange());
         v.initialize ("1990:2010");
         System.out.println (v.getRange());
         v.initialize ("100:1"); // invalid
         System.out.println (v.getRange());

         System.out.println ("\nTest Range constraint (-5, 5):");
         v.setRange (new Range (-5, 5));
         v.test ("-5.1");
         v.test ("-5.0");
         v.test ("0");
         v.test ("5.0");
         v.test ("5.1");
      }
   }
}
