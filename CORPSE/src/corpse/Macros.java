package corpse;

import java.io.File;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import str.Token;

public final class Macros
{
   public static boolean DEBUG = true;
   
   private static final Stack<String> TOKEN_STACK = new Stack<String>();
   
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
   static final String FILTER_CHAR    = "#";
   
   static final Pattern COMMENT_LINE =
      Pattern.compile ("^[" + 
                       Pattern.quote (COMMENT_CHAR + HEADER_CHAR + FOOTNOTE_CHAR +
                                      SEPR_CHAR + SOURCE_CHAR + TITLE_CHAR) +
                       "].*", Pattern.MULTILINE);
   
   static final Pattern TOKEN = Pattern.compile ("\\{([^{}]+)\\}");
   
   // [50/50] CONDITION (all-or-nothing format): {{2}=2?ALL}
   // [50/50] CONDITION (either/or format)     : {{2}=2?YES:NO}
   // [70/30] CONDITION (using > operator)     : {{10}>7?RARE:COMMON}
   
   // Note: the first element of the inner expression (e.g., {2}) will be 
   // resolved prior to the evaluation the outer expression, and so the 
   // first group of the pattern includes just the number.  The first element
   // may be any numeric expression supported by Quantity.java.
   // BELL-CURVE CONDITION (embedded roll)     : {{3d6}<13?Normal:Good} 
   static final Pattern CONDITION =
      Pattern.compile ("\\{(\\d+)([=<>])(\\d+)[?]([^:]+)(?::([^:{}]+))?\\}");

   // {one|two|three|four} -- chooses one option, with equal chance for each
   static final Pattern ONE_OF = Pattern.compile ("\\{([^|{]+([|][^|{]*)+)\\}");
   // TODO: weighted options: {#:opt1|#:opt2|...}
   
   static final String NAME = "([A-Za-z][-_A-Za-z0-9]*)";
   
   private static final String QTY = "(?:(\\d+) +)?";
   private static final String TABLE = "=?" + NAME; // TODO {=Quality }
   private static final String SUBSET = "(?:\\" + SUBSET_CHAR + NAME + "?)?";
   private static final String COLUMN = "(?:\\" + COLUMN_CHAR + NAME + "?)?";
   private static final String FILTER = "(?:\\" + FILTER_CHAR + "([^}]+)?)?";
   
   private static final Pattern TABLE_XREF = // {# Table:Subset@Column} 
      Pattern.compile ("\\{" + QTY + TABLE + SUBSET + COLUMN + FILTER + " *\\}");

   private static final Pattern SCRIPT_XREF = // {# Script.cmd}
      Pattern.compile ("\\{" + QTY + NAME + "[.]cmd\\}", Pattern.CASE_INSENSITIVE);

   private static final Pattern NAME_MACRO = 
      Pattern.compile ("\\{!N(?:AME)?\\}", Pattern.CASE_INSENSITIVE);
   
   private Macros() { }

   public static String resolve (final String entry, final String filter)
   {
      String resolvedEntry = entry;
      
      Matcher m;
      while ((m = TOKEN.matcher (resolvedEntry)).find()) // loop for multiple tokens
      {
         String token = m.group();
         String resolvedToken;
         resolvedToken = resolveTables (token, filter);
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
      int qty = 0;
      if (Quantity.is(entry))
         qty = new Quantity(entry).get();
      return qty;
   }
   
   public static int getMax (final String token)
   {
      return new Quantity(token).getMax();
   }

   private static String resolveExpressions (final String token)
   {
      String resolvedToken;
      resolvedToken = resolveQuantity (token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveConditions (token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveOneOfs(token);
      
      if (DEBUG && !token.equals (resolvedToken))
          System.out.println ("resolveExpressions: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static String resolveQuantity (final String token)
   {
      String resolvedToken = token;
      if (Quantity.is(token))
         resolvedToken = "" + new Quantity(token).resolve();
      if (DEBUG && !token.equals (resolvedToken))
         System.out.println ("resolveQuantity: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
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

   private static String resolveTables (final String entry, final String filter)
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
            String xrefFil = m.group (5);
            if (xrefFil == null && filter != null) // TODO
               xrefFil = filter;
            
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
                  String loop = getInvalidTableToken (xrefTbl, xrefSub, xrefCol, xrefFil);
                  resolved = m.replaceFirst (Matcher.quoteReplacement (loop));
               }
            }
            TOKEN_STACK.push (token);
            
            if (xrefSub == null && token.contains (SUBSET_CHAR)) // e.g., Metal:
               xrefSub = xrefTbl;
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < count; i++)
            {
               String xref = RandomEntry.get (xrefTbl, xrefSub, xrefCol, xrefFil);
               if (xref == null)
               {
                  System.err.println ("Invalid reference: " + entry);
                  xref = getInvalidTableToken (xrefTbl, xrefSub, xrefCol, xrefFil);
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
   
   private static String getInvalidTableToken (final String table, final String subset, final String column,
         final String filter)
   {
      StringBuilder sb = new StringBuilder();
      sb.append (TokenRenderer.INVALID_OPEN);
      sb.append (table);
      if (subset != null)
         sb.append (SUBSET_CHAR + subset);
      if (column != null)
         sb.append (COLUMN_CHAR + column);
      if (filter != null)
         sb.append (FILTER_CHAR + filter);
      sb.append (TokenRenderer.INVALID_CLOSE);

      return sb.toString();
   }
   
   public static void main (final String[] args)
   {
      Macros.DEBUG = true;
      
      Table.populate (new File ("data/Tables"));
      String entry = "{Metal" + SUBSET_CHAR + "}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));

      entry = "Smell: {{3}=3?{SmellAdjective} }{Smell}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));
      
      entry ="Description: {Color}{{5}=5?, with bits of {Reagent} floating in it}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));
   }
}
