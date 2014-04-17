package corpse;

import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import str.Token;
import corpse.ui.TokenRenderer;

public final class Macros
{
   public static boolean DEBUG = false;
   private static boolean TEST = false;

   private static final Stack<String> TOKEN_STACK = new Stack<String>();

   private static Vector<String> lastResolved = new Vector<String>(); // for back-references

   private Macros()
   {
   }

   public static String resolve(final String tableOrScriptName, final String entry) // for debugging
   {
      String resolved = resolve(tableOrScriptName, entry, null);
      if (DEBUG || TEST)
         System.out.println(entry + " = [" + resolved + "]");
      return resolved;
   }

   public static String resolve(final String tableOrScriptName, final String entry, final String filter)
   {
      String resolvedEntry = entry;
      lastResolved.clear();

      Matcher m;
      while ((m = Constants.TOKEN.matcher(resolvedEntry)).find()) // loop for multiple tokens
      {
         String token = m.group();
         String resolvedToken = token;

         resolvedToken = resolveExpressions(resolvedToken);
         resolvedToken = resolveTables(tableOrScriptName, resolvedToken, filter);
         resolvedToken = resolveScripts(resolvedToken);
         if (resolvedToken.equals(token))
            resolvedToken = "<" + m.group(1) + ">"; // avoid infinite loop

         String caseSample = token;
         if (caseSample.equals(Constants.LAST_RESOLVED_TOKEN) && !lastResolved.isEmpty())
            caseSample = lastResolved.lastElement();
         resolvedToken = matchCase(caseSample, resolvedToken);

         if (!resolvedToken.contains("{")) // don't capture other tokens (e.g. variables)?
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

   public static int resolveNumber(final String entry)
   {
      int resolved = 0;
      Quantity qty = Quantity.getQuantity(entry);
      if (qty != null)
         resolved = qty.get();
      return resolved;
   }

   public static int getMin(final String token)
   {
      return new Quantity(token).getMin();
   }

   public static int getMax(final String token)
   {
      return new Quantity(token).getMax();
   }

   public static String resolveExpressions(final String token)
   {
      String resolvedToken;
      resolvedToken = resolveQuantity(token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveVariables(token);
      if (resolvedToken.equals(token))
         resolvedToken = resolvePlurals(token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveFormatter(token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveFilters(token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveConditions(token);
      if (resolvedToken.equals(token))
         resolvedToken = resolveOneOfs(token);

      if (DEBUG && !token.equals(resolvedToken))
         System.out.println("  resolveExpressions: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static String resolveVariables(final String token)
   {
      String resolvedToken = token;

      Matcher m = Constants.VARIABLE_TOKEN.matcher(resolvedToken);
      if (m.find())
      {
         String replacement = Constants.VARIABLES.get(m.group(1).toUpperCase());
         if (m.group(0).equals(Constants.LAST_RESOLVED_TOKEN) && !lastResolved.isEmpty())
            resolvedToken = m.replaceFirst(Matcher.quoteReplacement(lastResolved.lastElement()));
         else if (replacement != null) // found a matching variable
            resolvedToken = m.replaceFirst(Matcher.quoteReplacement(replacement));
         else
            resolvedToken = m.replaceFirst("<" + m.group(1) + ">");
      }

      if (DEBUG && !token.equals(resolvedToken))
         System.out.println("  resolveVariables: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static String resolveFilters(final String token)
   {
      String resolvedToken = token;

      Matcher m = Constants.FILTER_TOKEN.matcher(resolvedToken);
      if (m.matches())
      {
         String text = m.group(1);
         Pattern p = CORPSE.safeCompile("Invalid filter pattern in " + token, m.group(2));
         Matcher filterMatcher = p.matcher(text);
         if (filterMatcher.find())
         {
            String replacement = "";
            if (filterMatcher.groupCount() > 0)
               replacement = filterMatcher.group(1);
            else
               replacement = filterMatcher.group();
            resolvedToken = m.replaceFirst(replacement);
         }
         else
            resolvedToken = m.replaceFirst(text);
      }

      if (DEBUG && !token.equals(resolvedToken))
         System.out.println("  resolveFilters: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static final Pattern FORMAT_PART = Pattern.compile("\\s*([^,]+)");
   private static final Pattern FORMAT_COMMA = Pattern.compile("([^,]+)(?:,\\s*([^,]+)){1,4}"); // chain, iron, heavy 
   private static final Pattern FORMAT_PAREN = Pattern.compile("([^(]+?)\\s*\\(([^)]+)\\)"); // tent (large)
   
   private static String resolveFormatter(final String token)
   {
      String resolvedToken = token;

      Matcher m = Constants.FORMAT_TOKEN.matcher(resolvedToken);
      if (m.matches())
      {
         resolvedToken = m.group(1); 
         m = FORMAT_COMMA.matcher(resolvedToken);
         if (m.matches())
         {
            m = FORMAT_PART.matcher(resolvedToken);
            resolvedToken = null;
            while (m.find())
               resolvedToken = m.group(1) + (resolvedToken != null ? " " + resolvedToken : "");
         }
         
         m = FORMAT_PAREN.matcher(resolvedToken);
         if (m.matches())
            resolvedToken = m.group(2) + " " + m.group(1);
      }

      if (DEBUG && !token.equals(resolvedToken))
         System.out.println("  resolveFormatter: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static String resolvePlurals(final String token)
   {
      String resolvedToken = token;

      Matcher m = Constants.PLURAL_TOKEN.matcher(resolvedToken);
      if (m.matches())
      {
         String text = m.group(1);
         String upper = text.toUpperCase();
         String replacement = Constants.PLURALS.get(upper);

         if (replacement != null) // found the appropriate plural form
            resolvedToken = m.replaceFirst(replacement);
         else if (upper.endsWith("S") || upper.endsWith("X"))
            resolvedToken = text + "es";
         else if (upper.endsWith("Y"))
            resolvedToken = text.substring(0, text.length() - 1) + "ies"; // strip Y, add IES
         else
            resolvedToken = text + "s";
      }

      if (DEBUG && !token.equals(resolvedToken))
         System.out.println("  resolvePlurals: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static String resolveQuantity(final String token)
   {
      String resolvedToken = token;
      Quantity qty = Quantity.getQuantity(token);
      if (qty != null)
         resolvedToken = "" + qty.resolve();
      if (DEBUG && !token.equals(resolvedToken))
         System.out.println("  resolveQuantity: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static String resolveConditions(final String token)
   {
      String resolved = token;
      
      Matcher m = Constants.PERCENT_CONDITION.matcher(resolved);
      if (m.matches())
      {
         int percent = Integer.parseInt(m.group(1));
         String ifVal = m.group(2);
         String elseVal = m.group(3);
         if (elseVal == null)
            elseVal = "";
         
         int roll = RandomEntry.get(100);
         boolean satisfied = roll <= percent;
         resolved = m.replaceFirst(satisfied ? ifVal : elseVal);
         if (DEBUG && !token.equals(resolved))
            System.out.println("  resolvePercent: [" + token + "] = [" + resolved + "]");
      }
      
      m = Constants.CONDITION.matcher(resolved);
      if (m.matches())
      {
         int roll = Integer.parseInt(m.group(1));
         String oper = m.group(2);
         int target = Integer.parseInt(m.group(3));
         String ifVal = m.group(4);
         String elseVal = m.group(5);
         if (elseVal == null)
            elseVal = "";

         boolean satisfied = false;
         if (oper.equals("="))
            satisfied = roll == target;
         else if (oper.equals(">"))
            satisfied = roll > target;
         else if (oper.equals("<"))
            satisfied = roll < target;

         resolved = m.replaceFirst(satisfied ? ifVal : elseVal);
         if (DEBUG && !token.equals(resolved))
            System.out.println("  resolveConditions: [" + token + "] = [" + resolved + "]");
      }
      
      return resolved;
   }

   private static String resolveOneOfs(final String token)
   {
      String resolved = token;
      Matcher m = Constants.ONE_OF.matcher(resolved);
      if (m.matches())
      {
         // add a terminator, in case the last token is empty
         String[] tokens;
         if (m.group(1).contains(Constants.ONE_OF_CHAR_1))
            tokens = Token.tokenizeAllowEmpty(m.group(1) + Constants.ONE_OF_CHAR_1, Constants.ONE_OF_CHAR_1);
         else
            tokens = Token.tokenizeAllowEmpty(m.group(1) + Constants.ONE_OF_CHAR_2, Constants.ONE_OF_CHAR_2);
         int roll = RandomEntry.get(tokens.length);
         resolved = m.replaceFirst(tokens[roll]);
         if (DEBUG && !token.equals(resolved))
            System.out.println("  resolveOneOfs: [" + token + "] = [" + resolved + "]");
      }
      return resolved;
   }

   private static String resolveScripts(final String token)
   {
      String resolved = token;
      Matcher m = Constants.SCRIPT_XREF.matcher(resolved);
      if (m.matches())
      {
         int count = 1;
         if (m.group(1) != null)
            count = Integer.parseInt(m.group(1));
         if (count > 0)
         {
            String name = m.group(2);

            // avoid infinite loop references
            if (TOKEN_STACK.contains(name))
            {
               int depth = 0;
               for (String s : TOKEN_STACK)
                  if (s.equals(name))
                     depth++;
               if (depth > 1) // allow limited recursion (e.g., for Potion of Delusion)
               {
                  System.err.println("Recursive script error: " + name);
                  String loop = TokenRenderer.INVALID_OPEN + name + TokenRenderer.INVALID_CLOSE;
                  resolved = m.replaceFirst(Matcher.quoteReplacement(loop));
               }
            }
            TOKEN_STACK.push(name);

            Script script = Script.getScript(name);
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < count; i++)
               buf.append(script.resolve());
            resolved = m.replaceFirst(Matcher.quoteReplacement(buf.toString()));

            TOKEN_STACK.pop();
         }

         if (DEBUG && !token.equals(resolved))
            System.out.println("  resolveScripts: [" + token + "] = [" + resolved + "]");
      }
      return resolved;
   }

   private static String resolveTables(final String tableOrScriptName, final String entry, final String filter)
   {
      String resolved = entry;

      int count = 0;
      String token = null;
      String xrefTbl = null;
      String xrefSub = null;
      String xrefCol = null;
      String xrefFil = null;

      Matcher m = Constants.SUBSET_REF.matcher(resolved);
      if (m.matches() && tableOrScriptName != null)
      {
         count = 1;
         token = m.group(0);
         xrefSub = m.group(1);
         xrefCol = m.group(2);
         xrefFil = m.group(3);
         xrefTbl = matchCase(xrefSub + xrefCol, tableOrScriptName.toLowerCase()); // ignore table/script case
      }
      else if ((m = Constants.TABLE_XREF.matcher(resolved)).matches())
      {
         token = m.group(0);
         
         count = 1;
         if (m.group(1) != null)
            count = Integer.parseInt(m.group(1));

         xrefTbl = m.group(2);
         
         if (Constants.INCLUDE_CHAR.equals(m.group(6))) // {Table+}
            xrefFil = m.group(7);
         else // {Table:Subset.Column#Filter}
         {
            xrefSub = m.group(3);
            xrefCol = m.group(4);
            xrefFil = m.group(5);
            
            // support default subsets and columns
            if (xrefCol == null)
               xrefCol = xrefTbl;
            if (xrefSub == null)
               xrefSub = xrefTbl;
         }
      }

      if (count > 0)
      {
         // TODO: must handle Table:.# (see also SubTable.java)
         if (xrefFil == null && filter != null)
            xrefFil = filter;

         // System.out.println("Macros [" + token + "] T[" + xrefTbl + "] S[" + xrefSub + "] C[" + xrefCol + "] F[" + xrefFil + "]");

         // avoid infinite loop references
         if (TOKEN_STACK.contains(token))
         {
            int depth = 0;
            for (String s : TOKEN_STACK)
               if (s.equals(token))
                  depth++;
            if (depth > 1) // allow limited recursion (e.g., for Potion of Delusion)
            {
               System.err.println("Recursive token error: " + token);
               String loop = getInvalidTableToken(xrefTbl, xrefSub, xrefCol, xrefFil);
               resolved = m.replaceFirst(Matcher.quoteReplacement(loop));
            }
         }

         if (entry.equals(resolved))
         {
            TOKEN_STACK.push(token);

            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < count; i++)
            {
               String xref = RandomEntry.get(xrefTbl, xrefSub, xrefCol, xrefFil);
               if (xref == null)
               {
                  System.err.println("Invalid reference: " + entry);
                  xref = getInvalidTableToken(xrefTbl, xrefSub, xrefCol, xrefFil);
               }
               buf.append(xref);
               if (count > 1)
                  buf.append("\n");
            }

            resolved = m.replaceFirst(Matcher.quoteReplacement(buf.toString()));

            TOKEN_STACK.pop();
         }

         if (DEBUG && !entry.equals(resolved))
            System.out.println("  resolveTables: [" + entry + "] = [" + resolved + "]");
      }

      return resolved;
   }

   // Make the case of the resolved token match the case of the token.
   
   private static final Pattern TO_CAPITALIZE = Pattern.compile("\\b([a-z])");

   private static String matchCase(final String token, final String resolved)
   {
      String caseMatched = resolved;
      if (resolved.isEmpty())
         ; // nothing to do
      else if (resolved.toUpperCase().equals(resolved))
         ; // do nothing; leave the value all uppercase (for acronyms)
      else if (token.toLowerCase().equals(token)) // all lower
         caseMatched = resolved.toLowerCase();
      else if (token.toUpperCase().equals(token)) // all upper
         ; // do nothing; leave the value as-is (good for sentences)
      else // cap-init all words
      {
         Matcher m;
         while ((m = TO_CAPITALIZE.matcher(caseMatched)).find())
            caseMatched = m.replaceFirst(m.group(1).toUpperCase());
      }
      return caseMatched;
   }

   private static String getInvalidTableToken(final String table, final String subset, final String column, final String filter)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(TokenRenderer.INVALID_OPEN);
      sb.append(table);
      if (subset != null)
         sb.append(Constants.SUBSET_CHAR + subset);
      if (column != null)
         sb.append(Constants.COLUMN_CHAR + column);
      if (filter != null)
         sb.append(Constants.FILTER_CHAR + filter);
      sb.append(TokenRenderer.INVALID_CLOSE);

      return sb.toString();
   }

   public static void main(final String[] args)
   {
      Macros.TEST = true;
      CORPSE.init(true); // toggle debug
      RandomEntry.randomize();

      /*
      Macros.resolve(null, "{50%?Yes:No}");
      Macros.resolve(null, "{Island Event}");
      Macros.resolve(null, "{Metal" + Constants.SUBSET_CHAR + "}");
      Macros.resolve(null, "Description: {Color}{20%?, with bits of {Reagent} floating in it}");
      Macros.resolve(null, "Filter: {Noise#S.+}");
      Macros.resolve(null, "Filter Variable: {Color:Basic{!OneWord}}");
      Macros.resolve(null, "Filter: {Color:Basic#S.+}");
      Macros.resolve(null, "{Color} " + Constants.LAST_RESOLVED_TOKEN);
      Macros.resolve(null, "{#text:.}"); // filtered token
      Macros.resolve(null, "{Color} {Fauna#{#{!}:.}.*}"); // back-reference with a filter
      Macros.resolve(null, "{+thing} {+moss} {+fly} {+mouse} {+fox}"); // test plurals
      Macros.resolve(null, "{~last, first} / {~chain, gold, fine} / {~boat (large)}"); // formatter
      Macros.resolve("Barsoom Plot", "{:Villain}"); // subset short-cut
      Macros.resolve(null, "{equipment+}"); // full line
      Macros.resolve(null, "{equipment}"); // default column
      */
      Macros.resolve(null, "Filter: {Name#S.+}");
   }
}
