package corpse;

import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import str.Token;
import corpse.ui.TokenRenderer;

public final class Macros
{
   private static final Pattern VOWEL = Pattern.compile("^[aeiou]", Pattern.CASE_INSENSITIVE);
   
   public static boolean DEBUG = false;
   private static boolean TEST = false;

   private static final Stack<String> TOKEN_STACK = new Stack<String>();

   private static Vector<String> lastResolved = new Vector<String>(); // for back-references

   private Macros()
   {
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
         if (resolvedToken.equals(token)) // avoid infinite loop
            resolvedToken = TokenRenderer.INVALID_OPEN + m.group(1) + TokenRenderer.INVALID_CLOSE;

         String caseSample = token;
         if (caseSample.equals(Constants.LAST_RESOLVED_TOKEN) && !lastResolved.isEmpty())
            caseSample = lastResolved.lastElement();
         resolvedToken = matchCase(caseSample, resolvedToken);

         if (!resolvedToken.contains("{")) // don't capture other tokens (e.g. variables)?
            lastResolved.add(resolvedToken); // TODO: only capture user-specified ones?

         try
         {
            resolvedEntry = m.replaceFirst(Matcher.quoteReplacement(resolvedToken));
            // fix "a" vs "an"
            if (VOWEL.matcher(resolvedToken).find()) // starts with a vowel
            {
               Pattern article = Pattern.compile("\\b(a) " + resolvedToken, Pattern.CASE_INSENSITIVE);
               Matcher m2 = article.matcher(resolvedEntry) ;
               if (m2.find()) // preceded by "a" but should be "an"
                  resolvedEntry = m2.replaceFirst("$1n " + Matcher.quoteReplacement(resolvedToken));
            }
                  
         }
         catch (Exception x)
         {
            System.out.println("Entry: " + entry);
            System.out.println("Groups: " + m.groupCount());
            x.printStackTrace(System.err);
         }
      }

      if (DEBUG || TEST)
         System.out.println(entry + " = [" + resolvedEntry + "]");
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
            String replacement = filterMatcher.groupCount() > 0 ? filterMatcher.group(1) : filterMatcher.group();
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
         
         resolvedToken = resolvedToken.replaceAll(" *\\[[0-9]+\\]", ""); // strip any footnotes
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
         else if (upper.endsWith("S") || upper.endsWith("X") || upper.endsWith("SH") || upper.endsWith("CH"))
            resolvedToken = text + "es";
         else if (upper.endsWith("Y") && !upper.endsWith("EY"))
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

   private static final String F = Constants.FILTER_CHAR; 
   private static final String NOT_F = "[^" + F + "]+";
   private static final Pattern FILTER = Pattern.compile("(" + F + NOT_F + ")\\|(" + NOT_F + F + ")");
   
   private static String resolveOneOfs(final String token)
   {
      String resolved = token;
      Matcher m = Constants.ONE_OF.matcher(resolved);
      if (m.matches())
      {
         // hack to allow alteration in regex filters ({Color#.*a|e.*#|Color#.*i|o.*#})
         Matcher fm = FILTER.matcher(resolved);
         while (fm.find())
            resolved = fm.replaceAll("$1!!$2"); // replace "|" with "!!" 
         
         m = Constants.ONE_OF.matcher(resolved);
         if (m.matches()) // make sure there's still alteration to split
         {
            // add a terminator, in case the last token is empty
            String[] tokens;
            if (m.group(1).contains(Constants.ONE_OF_CHAR_1))
               tokens = Token.tokenizeAllowEmpty(m.group(1) + Constants.ONE_OF_CHAR_1, Constants.ONE_OF_CHAR_1);
            else
               tokens = Token.tokenizeAllowEmpty(m.group(1) + Constants.ONE_OF_CHAR_2, Constants.ONE_OF_CHAR_2);
            int roll = RandomEntry.get(tokens.length);
            resolved = m.replaceFirst(tokens[roll]);
         }
         
         resolved = resolved.replace("!!", "|"); // restore any regex filter alteration
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

      String token = null;
      String xrefTbl = null;
      String xrefSub = null;
      String xrefCol = null;
      String xrefFil = null;

      Matcher m = Constants.INTERNAL_REF.matcher(resolved);
      if (m.matches() && tableOrScriptName != null)
      {
         token = m.group(0);
         xrefSub = m.group(1);
         xrefCol = m.group(2);
         xrefFil = m.group(3);
         xrefTbl = matchCase(xrefSub + xrefCol, tableOrScriptName.toLowerCase()); // ignore table/script case
      }
      else if ((m = Constants.TABLE_XREF.matcher(resolved)).matches())
      {
         token = m.group(0);
         
         xrefTbl = m.group(1);
         
         if (Constants.ALL_CHAR.equals(m.group(5))) // {Table!#Filter#}
         {
            // TODO
            // xrefTbl += Contants.ALL_CHAR; // we want the entire line for this table
            // new Table(xrefTbl, xrefFil); // load the full table
            xrefFil = m.group(6);
         }
         else // {Table:Subset.Column#Filter#}
         {
            xrefSub = m.group(2);
            xrefCol = m.group(3);
            xrefFil = m.group(4);
            
            // support default subsets and columns
            if (xrefCol == null)
               xrefCol = xrefTbl;
            if (xrefSub == null)
               xrefSub = xrefTbl;
         }
      }

      if (xrefTbl != null)
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

            String xref = RandomEntry.get(xrefTbl, xrefSub, xrefCol, xrefFil);
            if (xref == null)
            {
               System.err.println("Invalid reference: " + entry);
               xref = getInvalidTableToken(xrefTbl, xrefSub, xrefCol, xrefFil);
            }

            resolved = m.replaceFirst(Matcher.quoteReplacement(xref));

            TOKEN_STACK.pop();
         }

         if (DEBUG && !entry.equals(resolved))
            System.out.println("  resolveTables: [" + entry + "] = [" + resolved + "]");
      }

      return resolved;
   }

   // Make the case of the resolved token match the case of the token.
   
   private static final Pattern TO_CAPITALIZE = Pattern.compile("\\b([a-z])");
   private static final Pattern NO_CAP = 
      Pattern.compile(" A | An | And | At | By | For | From | In | Of | On | Or | The | To | With ");

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
         caseMatched = caseMatched.replace("'S", "'s"); // hack to fix possessive suffix
         while ((m = NO_CAP.matcher(caseMatched)).find())
            caseMatched = m.replaceFirst(m.group(0).toLowerCase());
         // don't cap some words
         /*
         for (String ignore : new String[] { "A", "An", "And", "At", "By", "For", "From", "In", "Of", "On", "Or", "The", "To", "With", "Without" })
            caseMatched = caseMatched.replace(" " + ignore + " ", " " + ignore.toLowerCase() + " ");
            */
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
      Macros.resolve(null, "{50%?Yes:No}", null);
      Macros.resolve(null, "{Island Event}", null);
      Macros.resolve(null, "{Metal" + Constants.SUBSET_CHAR + "}", null);
      Macros.resolve(null, "Description: {Color}{20%?, with bits of {Reagent} floating in it}", null);
      Macros.resolve(null, "Filter: {Noise#S.+#}", null);
      Macros.resolve(null, "Filter Variable: {Color:Basic{!OneWord}}", null);
      Macros.resolve(null, "Filter: {Color:Basic#S.+#}", null);
      Macros.resolve(null, "{Color} " + Constants.LAST_RESOLVED_TOKEN, null);
      Macros.resolve(null, "{#text:.}", null); // filtered token
      Macros.resolve(null, "{Color} {Fauna#{#{!}:.}.*#}", null); // back-reference with a filter
      Macros.resolve(null, "{~last, first} / {~chain, gold, fine} / {~boat (large)}", null); // formatter
      Macros.resolve("Barsoom Plot", "{:Villain}", null); // subset short-cut
      Macros.resolve(null, "{equipment+}", null); // full line
      Macros.resolve(null, "{equipment}", null); // default column
      Macros.resolve(null, "2 * 5 = {=2*5}", null);
      Macros.resolve(null, "Alteration = {Alteration}", null);
      Macros.resolve(null, "Filter Alteration = {Spell#.*(walk|fall).*#}", null);
      Macros.resolve(null, "Filter and One-Of Alteration = {{Color#.*a|e.*#}|{Color#.*i|o.*#}}", null);
      Macros.resolve(null, "Filter: {Name#S.+#}", null);
      Macros.resolve(null, "{Profession:Craftsman}", null); // filter subset
      Macros.resolve(null, "{Profession.all#^([^ ]+) .*craftsman.*#}", null); // filter vs all with group
      Macros.resolve(null, "{#{Profession+#.*craftsman.*#}:^(.*?)  }", null); // filter vs all with group
      */
      Macros.resolve(null, "{thing+} {moss+} {fly+} {mouse+} {fox+}", null); // test plurals
      
      System.out.println("Aa: " + Macros.matchCase("Aa", "cap each word's first letter in the phrase")); 
      System.out.println("AA: " + Macros.matchCase("AA", "Leave ALL words in the phrase alone"));
      System.out.println("aa: " + Macros.matchCase("aa", "Lower Case ALL words in the phrase"));
   }
}
