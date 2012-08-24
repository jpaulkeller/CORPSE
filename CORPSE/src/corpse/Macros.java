package corpse;

import java.io.File;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import str.Token;

public final class Macros
{
   static final boolean DEBUG = false;
   
   private static final Stack<String> TOKEN_STACK = new Stack<String>();
   
   static final Pattern TOKEN = Pattern.compile ("\\{([^{}]+)\\}");
   
   static final Pattern ROLL = // {#}
      Pattern.compile ("\\{(\\d+)\\}");
   static final Pattern RANGE = // {#-#} 
      Pattern.compile ("\\{(\\d+) *- *(\\d+)\\}");
   
   // {#d#+#} A space may be used instead of the "d".
   // The extra +# is optional.  The operator can be + or -.
   static final Pattern DICE = 
      Pattern.compile ("\\{(\\d+) *[dD] *(\\d+)(?:([-+])(\\d+))?\\}");

   // {m,M} Generate a random number using normal distribution with a mean 
   // of "m" and a max of "M". See TitleMilitary.tbl for example.
   static final Pattern NORM = Pattern.compile ("\\{(\\d+), *(\\d+)\\}");
   
   // {#/max} Useful for magic-item charges (e.g., #/100 would roll 1-100)
   static final Pattern CHARGES = Pattern.compile ("\\{#/(\\d+)\\}");
   
   // TBD: best 3 of 4?
   // 3d6, 1d10+3, 1d%

   // Open-ended Rolls
   // Many games use the concept of an open roll. This is a roll that has no upper limit. 
   // They work by rolling a die, and if the maximum value for the die comes up, you add 
   // that value to another roll of the same die. If the second roll comes up at the maximum, 
   // you add and roll again, and so on.
   // RollPlay handles these kinds of rolls as well. Such rolls are indicated with a 't' in 
   // the form instead of a 'd'. For example, to indicate an open six-sided roll, the form 
   // would be 't6'. The rest of the syntax works just the same, so '3t6' means to roll three
   // open-ended six-sided dice and add them together. You can mix tests with normal rolls as well.
   
   // [50/50] CONDITION (all-or-nothing format): {{2}=2?ALL}
   // [50/50] CONDITION (either/or format)     : {{2}=2?YES:NO}
   // [70/30] CONDITION (using > operator)     : {{10}>7?RARE:COMMON}
   // Note: the first element of the inner expression (e.g., {2}) will be 
   // resolved prior to the evaluation the outer expression, and so the 
   // first group of the pattern includes just the number.  It is necessary
   // to use the braces though, to support more complex patterns such as:
   // BELL-CURVE CONDITION (embedded roll)     : {{3d6}<13?Normal:Good} 
   static final Pattern CONDITION =
      Pattern.compile ("\\{(\\d+)([=<>])(\\d+)[?]([^:]+)(?::([^:{}]+))?\\}");

   // {one|two|three|four} -- chooses one option, with equal chance for each
   // TODO: weighted options: {#:opt1|#:opt2|...}
   static final Pattern ONE_OF =
      Pattern.compile ("\\{([^|{]+([|][^|{]*)+)\\}");
   
   static final String COMMENT_CHAR   = "*";
   static final String FOOTNOTE_CHAR  = "]";
   static final String HEADER_CHAR    = ".";
   static final String SEPR_CHAR      = "-";
   static final String SOURCE_CHAR    = "?";
   static final String TITLE_CHAR     = "!";
   static final String INCLUDE_CHAR   = "+";
   static final String COLUMN_CHAR    = "@";
   static final String SUBSET_CHAR    = ":";
   static final String ONE_OF_CHAR    = "|";
   
   static final Pattern COMMENT_LINE =
      Pattern.compile ("^[" + 
                       Pattern.quote (COMMENT_CHAR + HEADER_CHAR + FOOTNOTE_CHAR +
                                      SEPR_CHAR + SOURCE_CHAR + TITLE_CHAR) +
                       "].*", Pattern.MULTILINE);
   
   static final String NAME = "([A-Za-z][-_A-Za-z0-9]*)";
   
   private static final String QTY = "(?:(\\d+) +)?";
   private static final String TABLE = "=?" + NAME; // TBD {=Quality }
   private static final String SUBSET = "(?:\\" + SUBSET_CHAR + NAME + "?)?";
   private static final String COLUMN = "(?:\\" + COLUMN_CHAR + NAME + "?)?";
   
   private static final Pattern TABLE_XREF = // {# Table:Subset@Column} 
      Pattern.compile ("\\{" + QTY + TABLE + SUBSET + COLUMN + " *\\}");

   private static final Pattern SCRIPT_XREF = // {# Script.cmd}
      Pattern.compile ("\\{" + QTY + NAME + "[.]cmd\\}",
                       Pattern.CASE_INSENSITIVE);

   private static final Pattern NAME_MACRO = 
      Pattern.compile ("\\{!N(?:AME)?\\}", Pattern.CASE_INSENSITIVE);
   
   private Macros() { }

   public static String resolve (final String entry)
   {
      String resolvedEntry = entry;
      
      Matcher m;
      while ((m = TOKEN.matcher (resolvedEntry)).find()) // loop for multiple tokens
      {
         String token = m.group();
         String resolvedToken;
         resolvedToken = resolveTables (token);
         if (resolvedToken.equals(token))
            resolvedToken = resolveExpressions (token);
         if (resolvedToken.equals(token))
            resolvedToken = resolveMacros (token);
         if (resolvedToken.equals(token))
            resolvedToken = resolveScripts (token);
         if (resolvedToken.equals(token))
            resolvedToken = m.replaceFirst ("<$1>"); // avoid infinite loop
         
         try
         {
            resolvedEntry = m.replaceFirst(resolvedToken);
         }
         catch (Exception x)
         {
            System.out.println("Entry: " + entry);
            System.out.println("Groups: " + m.groupCount());
            x.printStackTrace(System.err);
         }
      }
      
      return resolvedEntry;
   }

   public static int resolveNumber (final String entry)
   {
      Matcher m;
      if ((m = ROLL.matcher (entry)).matches())
         return resolveRoll (m);
      else if ((m = RANGE.matcher (entry)).matches())
         return resolveRange (m);
      else if ((m = DICE.matcher (entry)).matches())
         return resolveDice (m);
      else if ((m = NORM.matcher (entry)).matches())
          return resolveNorm (m);
      return 0;
   }
   
   public static int getMax (final String token)
   {
      int max = 0;
      
      Matcher m;
      if ((m = ROLL.matcher (token)).matches())
         max = Integer.parseInt (m.group (1));
      
      else if ((m = RANGE.matcher (token)).matches())
         max = Integer.parseInt (m.group (2));
      
      else if ((m = DICE.matcher (token)).matches())
      {
         int count = Integer.parseInt (m.group (1));
         int sides = Integer.parseInt (m.group (2));
         max = count * sides;
         
         String operator = m.group (3);
         if (operator != null)
         {
            int bonus = Integer.parseInt (m.group (4));
            max += operator.equals ("+") ? bonus : -bonus; 
         }
      }
      else if ((m = NORM.matcher (token)).matches())
         max = Integer.parseInt (m.group (2));
      
      return max;
   }

   private static String resolveExpressions (final String token)
   {
      String resolvedToken;
      resolvedToken = resolveRolls (token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveRanges (token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveDice (token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveNorm (token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveCharges (token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveConditions (token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveOneOfs(token);
      
      if (DEBUG && !token.equals (resolvedToken))
          System.out.println ("resolveExpressions: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static String resolveRolls (final String token)
   {
      String resolvedToken = token;
      Matcher m = ROLL.matcher (resolvedToken);
      if (m.matches())
      {
         resolvedToken = m.replaceFirst (resolveRoll  (m) + "");
         if (DEBUG && !token.equals (resolvedToken))
            System.out.println ("resolveRolls: [" + token + "] = [" + resolvedToken + "]");
      }
      return resolvedToken;
   }
   
   private static int resolveRoll (final Matcher m)
   {
      int range = Integer.parseInt (m.group (1));
      return RandomEntry.get (range) + 1;
   }

   private static String resolveRanges (final String token)
   {
      String resolvedToken = token;
      Matcher m = RANGE.matcher (resolvedToken);
      if (m.matches())
      {
         resolvedToken = m.replaceFirst (resolveRange (m) + "");
         if (DEBUG && !token.equals (resolvedToken))
            System.out.println ("resolveRanges: [" + token + "] = [" + resolvedToken + "]");
      }
      return resolvedToken;
   }

   private static int resolveRange (final Matcher m)
   {
      int from = Integer.parseInt (m.group (1));
      int to   = Integer.parseInt (m.group (2));
      int range = to - from + 1;
      return RandomEntry.get (range) + from;
   }

   private static String resolveCharges (final String token)
   {
      String resolved = token;
      Matcher m = CHARGES.matcher (resolved);
      if (m.matches())
      {
         int max = Integer.parseInt (m.group (1));
         int roll = RandomEntry.get (max) + 1;
         resolved = m.replaceFirst (roll + "/" + max);
         if (DEBUG && !token.equals (resolved))
             System.out.println ("resolveCharges: [" + token + "] = [" + resolved + "]");
      }
      return resolved;
   }

   private static String resolveDice (final String token)
   {
      String resolved = token;
      Matcher m = DICE.matcher (resolved);
      if (m.matches())
      {
         resolved = m.replaceFirst (resolveDice (m) + "");
         if (DEBUG && !token.equals (resolved))
             System.out.println ("resolveDice: [" + token + "] = [" + resolved + "]");
      }
      return resolved;
   }

   private static int resolveDice (final Matcher m)
   {
      int roll = 0;
      int count = Integer.parseInt (m.group (1));
      int sides = Integer.parseInt (m.group (2));
      for (int i = 0; i < count; i++)
         roll += RandomEntry.get (sides) + 1;
      
      String operator = m.group (3);
      if (operator != null)
      {
         int bonus = Integer.parseInt (m.group (4));
         if (operator.equals ("+"))
            roll += bonus;
         else if (operator.equals ("-"))
            roll -= bonus;
      }
      return roll;
   }

   private static String resolveNorm (final String token)
   {
      String resolved = token;
      Matcher m = NORM.matcher (resolved);
      if (m.matches())
      {
         resolved = m.replaceFirst (resolveNorm (m) + "");
         if (DEBUG && !token.equals (resolved))
             System.out.println ("resolveNorm: [" + token + "] = [" + resolved + "]");
      }
      return resolved;
   }

   private static int resolveNorm (final Matcher m)
   {
      int mean = Integer.parseInt (m.group (1));
      int max  = Integer.parseInt (m.group (2));
      return RandomEntry.getExp (mean, max);
   }

   private static String resolveConditions (final String token)
   {
      String resolved = token;
      Matcher m = CONDITION.matcher (resolved);
      if (m.matches())
      {
         int roll       = Integer.parseInt (m.group (1));
         String oper    = m.group (2);
         int target     = Integer.parseInt (m.group (3));
         String ifVal   = m.group (4); 
         String elseVal = m.group (5);
         if (elseVal == null)
            elseVal = "";
         
         boolean satisfied = false;
         if (oper.equals ("="))
            satisfied = roll == target;
         else if (oper.equals (">"))
            satisfied = roll > target;
         else if (oper.equals ("<"))
            satisfied = roll < target;
         
         resolved = m.replaceFirst (satisfied ? ifVal : elseVal); 
         if (DEBUG && !token.equals (resolved))
             System.out.println ("resolveConditions: [" + token + "] = [" + resolved + "]");
      }
      return resolved;
   }

   private static String resolveOneOfs (final String token)
   {
      String resolved = token;
      Matcher m = ONE_OF.matcher (resolved);
      if (m.matches())
      {
         String[] tokens = Token.tokenizeAllowEmpty (m.group (1), ONE_OF_CHAR);
         int roll = RandomEntry.get (tokens.length);
         resolved = m.replaceFirst (tokens[roll]); 
         if (DEBUG && !token.equals (resolved))
             System.out.println ("resolveOneOfs: [" + token + "] = [" + resolved + "]");
      }
      return resolved;
   }

   private static String resolveMacros (final String token)
   {
      String resolved = token;
      Matcher m = NAME_MACRO.matcher (resolved);
      if (m.matches())
      {
         resolved = m.replaceFirst (Name.getRandomName());
         if (DEBUG && !token.equals (resolved))
            System.out.println ("resolveMacros: [" + token + "] = [" + resolved + "]");
      }
      return resolved;
   }

   private static String resolveScripts (final String token)
   {
      String resolved = token;
      Matcher m = SCRIPT_XREF.matcher (resolved);
      if (m.matches())
      {
         int count = 1;
         if (m.group (1) != null)
            count = Integer.parseInt (m.group (1));
         if (count > 0)
         {
            String name = m.group (2);
            
            // avoid infinite loop references
            if (TOKEN_STACK.contains (name))
            {
               int depth = 0;
               for (String s : TOKEN_STACK)
                  if (s.equals (name))
                     depth++;
               if (depth > 1) // allow limited recursion (e.g., for Potion of Delusion)
               {
                  System.err.println ("Recursive script error: " + name);
                  String loop = TokenRenderer.INVALID_OPEN + name + TokenRenderer.INVALID_CLOSE;
                  resolved = m.replaceFirst (Matcher.quoteReplacement (loop));
               }
            }
            TOKEN_STACK.push (name);

            Script script = new Script ("data/Scripts/" + name + ".CMD");
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < count; i++)
               buf.append (script.resolve());
            resolved = m.replaceFirst (Matcher.quoteReplacement (buf.toString()));
            
            TOKEN_STACK.pop();
         }
         
         if (DEBUG && !token.equals (resolved))
            System.out.println ("resolveScripts: [" + token + "] = [" + resolved + "]");
      }
      return resolved;
   }

   private static String resolveTables (final String entry)
   {
      String resolved = entry;
      Matcher m = TABLE_XREF.matcher (resolved);
      if (m.matches())
      {
         int count = 1;
         if (m.group (1) != null)
            count = Integer.parseInt (m.group (1));
         if (count > 0)
         {
            String token   = m.group (0);
            String xrefTbl = m.group (2);
            String xrefSub = m.group (3);
            String xrefCol = m.group (4);
            
            // avoid infinite loop references
            if (TOKEN_STACK.contains (token))
            {
               int depth = 0;
               for (String s : TOKEN_STACK)
                  if (s.equals (token))
                     depth++;
               if (depth > 1) // allow limited recursion (e.g., for Potion of Delusion)
               {
                  System.err.println ("Recursive token error: " + token);
                  String loop = getInvalidTableToken (xrefTbl, xrefSub, xrefCol);
                  resolved = m.replaceFirst (Matcher.quoteReplacement (loop));
               }
            }
            TOKEN_STACK.push (token);
            
            if (xrefSub == null && token.contains (SUBSET_CHAR)) // e.g., Metal:
               xrefSub = xrefTbl;
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < count; i++)
            {
               String xref = RandomEntry.get (xrefTbl, xrefSub, xrefCol);
               if (xref == null)
               {
                  System.err.println ("Invalid reference: " + entry);
                  xref = getInvalidTableToken (xrefTbl, xrefSub, xrefCol);
               }
               buf.append (xref);
               if (count > 1)
                  buf.append ("\n");
            }

            resolved = m.replaceFirst (Matcher.quoteReplacement (buf.toString()));
            
            TOKEN_STACK.pop();
            
            if (DEBUG && !entry.equals (resolved))
               System.out.println ("resolveTables: [" + entry + "] = [" + resolved + "]");
         }
      }
      
      return resolved;
   }
   
   private static String getInvalidTableToken (final String table, final String subset, final String column)
   {
      StringBuilder sb = new StringBuilder();
      sb.append (TokenRenderer.INVALID_OPEN);
      sb.append (table);
      if (subset != null)
         sb.append (SUBSET_CHAR + subset);
      if (column != null)
         sb.append (COLUMN_CHAR + column);
      sb.append (TokenRenderer.INVALID_CLOSE);

      return sb.toString();
   }
   
   public static void main (final String[] args)
   {
      Table.populate (new File ("data/Tables"));
      String entry = "{Metal" + SUBSET_CHAR + "}";
      System.out.println (entry + " = " + Macros.resolve (entry));

      entry = "Smell: {{3}=3?{SmellAdjective} }{Smell}";
      System.out.println (entry + " = " + Macros.resolve (entry));
      
      entry ="Description: {Color}{{5}=5?, with bits of {Reagent} floating in it}";
      System.out.println (entry + " = " + Macros.resolve (entry));
   }
}
