package corpse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import str.Token;
import corpse.ui.TokenRenderer;
import file.FileUtils;

import corpse.Constants;

public final class Macros
{
   public static boolean DEBUG = false;
   
   private static final Stack<String> TOKEN_STACK = new Stack<String>();
   
   private static List<String> lastResolved = new ArrayList<String>(); // for back-references
   
   private Macros() { }

   public static String resolve (final String entry, final String filter)
   {
      String resolvedEntry = entry;
      lastResolved.clear();
      
      Matcher m;
      while ((m = Constants.TOKEN.matcher (resolvedEntry)).find()) // loop for multiple tokens
      {
         String token = m.group();
         String resolvedToken = token;
         
         resolvedToken = resolveVariables (resolvedToken);
         resolvedToken = resolveExpressions (resolvedToken);
         resolvedToken = resolveReferences (resolvedToken);
         resolvedToken = resolveTables (resolvedToken, filter);
         resolvedToken = resolveScripts (resolvedToken);
         if (resolvedToken.equals(token))
            resolvedToken = m.replaceFirst ("<$1>"); // avoid infinite loop
         resolvedToken = matchCase(token, resolvedToken);
         
         lastResolved.add(resolvedToken); // TODO: only capture user-specified ones?
         
         try
         {
            resolvedEntry = m.replaceFirst(Matcher.quoteReplacement(resolvedToken));
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

   private static final String VARIABLE_REGEX = "(\\{![^}]+\\})";
   private static final Pattern VARIABLE_TOKEN = Pattern.compile(VARIABLE_REGEX);
   // {!1} = #[^-_ ]+ // one word
   private static final Pattern VARIABLE = Pattern.compile(VARIABLE_REGEX + "=(.+) //.*");

   private static final Map<String, String> VARIABLES = new HashMap<String, String>();
   static
   {
      List<String> lines = FileUtils.getList("data/Variables.txt", FileUtils.UTF8, true);
      for (String line : lines)
      {
         Matcher m = VARIABLE.matcher(line);
         if (m.matches())
            VARIABLES.put(m.group(1), m.group(2));
      }
   }
   
   public static String resolveVariables (final String entry)
   {
      String resolvedToken = entry;
      
      Matcher m = VARIABLE_TOKEN.matcher (resolvedToken);
      while (m.find())
      {
         String replacement = VARIABLES.get(m.group(1));
         if (replacement != null)
            resolvedToken = m.replaceFirst(Matcher.quoteReplacement(replacement));
         else
            resolvedToken = m.replaceFirst("<" + m.group(0) + ">");
      }
      
      if (DEBUG && !entry.equals (resolvedToken))
         System.out.println ("resolveVariables: [" + entry + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }
   
   public static int resolveNumber (final String entry)
   {
      int qty = 0;
      if (Quantity.is(entry))
         qty = new Quantity(entry).get();
      return qty;
   }
   
   public static int getMin (final String token)
   {
      return new Quantity(token).getMin();
   }

   public static int getMax (final String token)
   {
      return new Quantity(token).getMax();
   }

   private static String resolveReferences (final String token)
   {
      String resolvedToken = token;
      
      Matcher m = Constants.BACK_REF.matcher (token);
      while (m.find())
      {
         String replacement = "";
         if (!lastResolved.isEmpty())
         {
            String regex = m.group(1);
            try
            {
               Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
               Matcher backRefMatcher = p.matcher(lastResolved.get(lastResolved.size() - 1));
               if (backRefMatcher.find())
               {
                  if (backRefMatcher.groupCount() > 0)
                     replacement = backRefMatcher.group(1);
                  else
                     replacement = backRefMatcher.group();
               }
            }
            catch (PatternSyntaxException x)
            {
               System.out.println("Invalid reference in " + token + ": [" + regex + "]");
            }
         }
         else
            System.out.println("Warning: Back reference without previous token!");
         resolvedToken = m.replaceFirst(replacement);
      }
      
      if (DEBUG && !token.equals (resolvedToken))
         System.out.println ("resolveReferences: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
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
      Matcher m = Constants.CONDITION.matcher (resolved);
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
      Matcher m = Constants.ONE_OF.matcher (resolved);
      if (m.matches())
      {
         String[] tokens = Token.tokenizeAllowEmpty (m.group (1), Constants.ONE_OF_CHAR);
         int roll = RandomEntry.get (tokens.length);
         resolved = m.replaceFirst (tokens[roll]); 
         if (DEBUG && !token.equals (resolved))
             System.out.println ("resolveOneOfs: [" + token + "] = [" + resolved + "]");
      }
      return resolved;
   }

   private static String resolveScripts (final String token)
   {
      String resolved = token;
      Matcher m = Constants.SCRIPT_XREF.matcher (resolved);
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

            Script script = Script.getScript(name);
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
      Matcher m = Constants.TABLE_XREF.matcher (resolved);
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
            
            if (xrefFil == null && filter != null)
               xrefFil = filter;
            if (xrefSub == null && token.contains (Constants.SUBSET_CHAR)) // e.g., Metal:
               xrefSub = xrefTbl;
            
            // System.out.println("token [" + token + "] sub [" + xrefSub + "] col [" + xrefCol + "] fil [" + xrefFil + "]");
            
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
            
            if (entry.equals(resolved))
            {
               TOKEN_STACK.push (token);
               
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
            }
            
            if (DEBUG && !entry.equals (resolved))
               System.out.println ("resolveTables: [" + entry + "] = [" + resolved + "]");
         }
      }
      
      return resolved;
   }

   // Make the case of the resolved token match the case of the token.
   
   private static String matchCase(final String token, String resolved)
   {
      if (token.toLowerCase().equals(token))
         resolved = resolved.toLowerCase();
      else if (token.toUpperCase().equals(token) || resolved.length() <= 1) 
         resolved = resolved.toUpperCase();
      else
         resolved = resolved.substring(0, 1).toUpperCase() + resolved.substring(1).toLowerCase();
      return resolved;
   }
   
   private static String getInvalidTableToken (final String table, final String subset, final String column, final String filter)
   {
      StringBuilder sb = new StringBuilder();
      sb.append (TokenRenderer.INVALID_OPEN);
      sb.append (table);
      if (subset != null)
         sb.append (Constants.SUBSET_CHAR + subset);
      if (column != null)
         sb.append (Constants.COLUMN_CHAR + column);
      if (filter != null)
         sb.append (Constants.FILTER_CHAR + filter);
      sb.append (TokenRenderer.INVALID_CLOSE);

      return sb.toString();
   }
   
   public static void main (final String[] args)
   {
      Macros.DEBUG = true;
      
      Table.populate (new File ("data/Tables"));
      String entry;
      
      entry = "{Island Event}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));
      
      entry = "{Metal" + Constants.SUBSET_CHAR + "}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));

      entry ="Description: {Color}{{5}=5?, with bits of {Reagent} floating in it}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));
      
      entry ="Filter: {Noise#S.+}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));
      
      entry ="Filter Variable: {Color:Basic{!1}}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));
      
      entry ="Filter: {Color:Basic#S.+}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));
      
      // test a back-reference to produce alliteration
      entry ="Filter: {Noise} {Fauna#!.!.*}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));
      
      // test a back-reference with a column
      entry ="{Trait} {Class@Class#!.!.*}";
      System.out.println (entry + " = " + Macros.resolve (entry, null));
   }
}
