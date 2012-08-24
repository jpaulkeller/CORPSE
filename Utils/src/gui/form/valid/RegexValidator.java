package gui.form.valid;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import str.Token;

/**
 * Validator for textual data.  This is a powerful validator which
 * supports comparing multiple regular expressions against a value.
 * It can ensure that a value matches (or does not match) one or more
 * patterns.
 *
 * This class does not support full boolean logic, but should be
 * sufficient for most needs (and easy to use).  There are three lists
 * of expressions (mustMatchAny, mustMatchAll, and mustMatchNone),
 * which can be used in various combinations.  If more than one list
 * is used, then each list must be fully satisfied.
 */
public class RegexValidator extends ValidationAdapter implements Serializable
{
   private static final long serialVersionUID = 4;

   // If set, the value must match AT LEAST ONE of these regular expressions 
   private List<Pattern> mustMatchAny = new ArrayList<Pattern>();
   // If set, the value must match ALL of these regular expressions
   private List<Pattern> mustMatchAll = new ArrayList<Pattern>();
   // If set, the value must match NONE of these regular expressions
   private List<Pattern> mustMatchNone = new ArrayList<Pattern>();
   
   /**
    * Caller must use one of the set methods below to initialize one or more
    * of the pattern lists.
    */
   public RegexValidator() { }
   
   /**
    * Constructor for simple regular expressions. The value will be
    * considered valid if it matches any of the given patterns.
    */
   public RegexValidator (final String... patterns)
   {
      setMustMatchAny (patterns);
   }
   
   /**
    * Interpret the given arguments String as three quoted lists (one
    * for ALL, one for ANY, and one for NONE).  Each list should be a
    * comma-separated list of regular expressions.
    *
    * Alternatively, you may send a single comma-separated list which
    * will be used as a list of "must match any" patterns.
    *
    * For example: "^(Red|Green|Blue)$"
    */
   @Override
   public boolean initialize (final String arguments)
   {
      if (arguments != null)
      {
         setNullValidity (true); // easy to override with expressions
         
         String[] allAnyNone = Token.tokenizeQuoted (arguments, "\"");
         String[] all  = null;
         String[] any  = null;
         String[] none = null;

         if (allAnyNone.length == 1)
         {
            any = Token.tokenize (allAnyNone[0]);
         }
         else if (allAnyNone.length == 3)
         {
            if (!allAnyNone[0].equals (""))
               all  = Token.tokenize (allAnyNone[0]);
            if (!allAnyNone[1].equals (""))
               any  = Token.tokenize (allAnyNone[1]);
            if (!allAnyNone[2].equals (""))
               none = Token.tokenize (allAnyNone[2]);
         }
         else
         {
            System.out.println ("RegexValidator invalid arguments: " + arguments);
            return false;
         }
            
         try
         {
            setMustMatchAll (all);
            setMustMatchAny (any);
            setMustMatchNone (none);
         }
         catch (IllegalArgumentException x)
         {
            System.out.println ("Invalid regular expression: " + x.getMessage());
            return false;
         }
      }

      return true;
   }
   
   public void setMustMatchAny (final String... patterns)
   {
      mustMatchAny.clear();
      if (patterns != null)
      {
         try
         {
            for (String pattern : patterns)
               mustMatchAny.add (Pattern.compile (pattern));
         }
         catch (PatternSyntaxException e)
         {
            throw new IllegalArgumentException (e.getMessage());
         }
      }
   }
   
   public void setMustMatchAll (final String... patterns)
   {
      mustMatchAll.clear();
      if (patterns != null)
      {
         try
         {
            for (String pattern : patterns)
               mustMatchAll.add (Pattern.compile (pattern));
         }
         catch (PatternSyntaxException e)
         {
            throw new IllegalArgumentException (e.getMessage());
         }
      }
   }
   
   public void setMustMatchNone (final String... patterns)
   {
      mustMatchNone.clear();
      if (patterns != null)
      {
         try
         {
            for (String pattern : patterns)
               mustMatchNone.add (Pattern.compile (pattern));
         }
         catch (PatternSyntaxException e)
         {
            throw new IllegalArgumentException (e.getMessage());
         }
      }
   }
   
   /**
    * Returns true if the value is valid.
    *
    * @param value The input text (a String, char[], StringBuffer or
    * InputStream).
    */
   @Override
   public boolean isValid (final Object value)
   {
      if (value == null || value.toString().equals (""))
         return isNullValid(); // default for null value

      for (Pattern mustMatch : mustMatchAll)
         if (!mustMatch.matcher (value.toString()).matches())
            return false; // value is not valid

      for (Pattern mustNotMatch : mustMatchNone)
         if (mustNotMatch.matcher (value.toString()).matches())
            return false; // value is not valid

      if (!mustMatchAny.isEmpty())
      {
         for (Pattern mustMatch : mustMatchAny)
            if (mustMatch.matcher (value.toString()).matches())
               return true; // value is valid
         return false;
      }

      return true;
   }

   private void test (final String description, final String value)
   {
      System.out.print (description + ": \"" + value + "\" ");
      if (isValid (value))
         System.out.println ("is valid");
      else
         System.out.println ("is not valid");
   }
   
   public static void main (final String[] args) // for testing
   {
      RegexValidator rv1 = new RegexValidator (".+");
      rv1.test ("abc", "abc");
      rv1.test ("(blank)", "");
      
      RegexValidator rv = new RegexValidator();
      String value = "test value";

      try
      {
         // test mustMatchAny
         System.out.println();
         rv.setMustMatchAny ("^t.*");
         rv.test ("Test Any with 1 matching pattern", value);
         rv.setMustMatchAny ("^n.*");
         rv.test ("Test Any with no matching pattern", value);
         rv.setMustMatchAny ("^n.*", "^t.*");
         rv.test ("Test Any with 1 matching, 1 not", value);
         rv.setMustMatchAny ((String[]) null); // clear it

         // test mustMatchAll
         System.out.println();
         rv.setMustMatchAll ("^t.*");
         rv.test ("Test All with 1 matching pattern", value);
         rv.setMustMatchAll ("^t.*", ".*e$");
         rv.test ("Test All with 2 matching patterns", value);
         rv.setMustMatchAll ("^n.*");
         rv.test ("Test All with no matching pattern", value);
         rv.setMustMatchAll ("^n.*", "^t.*");
         rv.test ("Test All with 1 matching, 1 not", value);
         rv.setMustMatchAll ((String[]) null); // clear it

         // test mustMatchNone
         System.out.println();
         rv.setMustMatchNone ("^t.*");
         rv.test ("Test None with 1 matching pattern", value);
         rv.setMustMatchNone ("^n.*");
         rv.test ("Test None with no matching pattern", value);
         rv.setMustMatchNone ("^n.*", "^t.*");
         rv.test ("Test None with 1 matching, 1 not", value);
         rv.setMustMatchNone ((String[]) null); // clear it

         // test composites
         System.out.println();
         rv.setMustMatchAny ("^t.*");
         rv.setMustMatchAll ("^t.*");
         rv.setMustMatchNone ("^n.*");
         rv.test ("Test valid composite", value);
         rv.setMustMatchAny ("^n.*");
         rv.setMustMatchAll ("^t.*");
         rv.setMustMatchNone ("^n.*");
         rv.test ("Test invalid composite", value);
         rv.setMustMatchAny ("^t.*");
         rv.setMustMatchAll ("^n.*");
         rv.setMustMatchNone ("^n.*");
         rv.test ("Test invalid composite", value);
         rv.setMustMatchAny ("^t.*");
         rv.setMustMatchAll ("^t.*");
         rv.setMustMatchNone ("^t.*");
         rv.test ("Test invalid composite", value);
         
         System.out.println();
      }
      catch (Exception x)
      {
         System.err.println (x + ": " + x.getMessage());
         x.printStackTrace (System.err);
      }
   }
}
