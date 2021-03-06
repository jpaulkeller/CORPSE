package corpse;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import str.Token;
import utils.Utils;
import corpse.ui.TokenRenderer;

public final class Macros
{
   private static final Pattern VOWEL = Pattern.compile("^[aeiou]", Pattern.CASE_INSENSITIVE);
   
   public static boolean DEBUG = false;
   private static boolean TEST = false;

   private static final Stack<String> TOKEN_STACK = new Stack<>();

   private static final String VAR = "([A-Za-z][A-Za-z0-9]*)";
   private static final Pattern PRIMITIVE_ASSIGNMENT = Pattern.compile("\\{" + VAR + "=([^}]+)\\}");
   private static final Pattern TABLE_ASSIGNMENT = Pattern.compile("\\{" + VAR + ":=([^}]+)\\}");
   private static final Map<String, String> primitiveAssignments = new HashMap<>();
   private static final Map<String, Assignment> tableAssignments = new HashMap<>();
            
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

         if (!resolvedToken.contains("{") && !resolvedToken.contains("!")) // don't capture other tokens or variables
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
            x.printStackTrace(System.out);
         }
      }

      if (DEBUG || TEST)
         System.out.println(entry + " = [" + resolvedEntry + "]");
      return resolvedEntry;
   }

   public static int resolveNumber(final String entry)
   {
      int resolved = 0;
      
      String resolvedEntry = resolveAssignments(entry);
      Quantity qty = Quantity.getQuantity(resolvedEntry);
      if (qty != null)
         resolved = qty.get();
      return resolved;
   }

   public static int getMin(final String token)
   {
      return Quantity.getQuantity(token).getMin();
   }

   public static int getMax(final String token)
   {
      return Quantity.getQuantity(token).getMax();
   }

   public static String resolveExpressions(final String token)
   {
      String resolvedToken = token;
      try
      {
         resolvedToken = resolveQuantity(token);
         if (resolvedToken.equals(token))
            resolvedToken = resolveAssignments(token);
         
         // not sure of the best order here, both can contain |
         if (resolvedToken.equals(token))
            resolvedToken = resolveOneOfs(token);
         if (resolvedToken.equals(token))
            resolvedToken = resolveVariables(token);
         
         if (resolvedToken.equals(token))
            resolvedToken = resolveExtensions(token);
         if (resolvedToken.equals(token))
            resolvedToken = resolveFormatter(token);
         if (resolvedToken.equals(token))
            resolvedToken = resolveFilters(token);
         if (resolvedToken.equals(token))
            resolvedToken = resolveConditions(token);
      }
      catch (Exception x)
      {
         System.out.println(x);
         System.out.println("  resolveExpressions: [" + token + "] = [" + resolvedToken + "]");
      }

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
            resolvedToken = m.replaceFirst("<" + Matcher.quoteReplacement(m.group(1)) + ">");
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
         // System.out.println("RT: " + resolvedToken + "; TX: " + text); // TODO
         Pattern p = CORPSE.safeCompile("Invalid filter pattern in " + token, m.group(2));
         Matcher filterMatcher = p.matcher(text);
         if (filterMatcher.find())
         {
            String replacement = filterMatcher.groupCount() > 0 ? filterMatcher.group(1) : filterMatcher.group();
            resolvedToken = m.replaceFirst(Matcher.quoteReplacement(replacement));
            // System.out.println(">>>> Replacement: " + replacement + "; RT: " + resolvedToken); // TODO
         }
         else
            resolvedToken = m.replaceFirst(Matcher.quoteReplacement(text));
      }

      if (DEBUG && !token.equals(resolvedToken))
         System.out.println("  resolveFilters: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static final Pattern FORMAT_PART = Pattern.compile("\\s*([^,]+)");
   private static final Pattern FORMAT_COMMA = Pattern.compile("([^,]+)(?:,\\s*([^,]+)){1,4}"); // chain, iron, heavy 
   private static final Pattern FORMAT_PAREN = Pattern.compile("([^(]+?)\\s*\\(([^)]+)\\)"); // tent (large)
   private static final Pattern FORMAT_QTY = Pattern.compile("(.*) *(x[0-9]+) *(.+)"); // quarrel/bolt, hand x20
   
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
         
         m = FORMAT_QTY.matcher(resolvedToken);
         if (m.matches())
            resolvedToken = m.group(1) + m.group(3) + " " + m.group(2);
         
         resolvedToken = resolvedToken.replaceAll(" *\\[[0-9]+\\]", ""); // strip any footnotes
      }

      if (DEBUG && !token.equals(resolvedToken))
         System.out.println("  resolveFormatter: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static String resolveExtensions(final String token)
   {
      String resolvedToken = token;

      Matcher m = Constants.EXTEND_TOKEN.matcher(resolvedToken);
      if (m.matches())
      {
         String extension = m.group(2);
         if (extension == null || extension.equals("s"))
            resolvedToken = convertToPlural(m);
         else if (extension.equals("er"))
            resolvedToken = convertToActor(m);
         else if (extension.equals("ing"))
            resolvedToken = convertToGerund(m);
      }

      if (DEBUG && !token.equals(resolvedToken))
         System.out.println("  resolveExtensions: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }

   private static String convertToPlural(final Matcher m)
   {
      String resolvedToken;
      String text = m.group(1);
      String upper = text.toUpperCase();
      String replacement = Constants.PLURALS.get(upper);

      if (replacement != null) // found the appropriate plural form
         resolvedToken = m.replaceFirst(Matcher.quoteReplacement(replacement));
      else if (upper.endsWith("S") || upper.endsWith("X") || upper.endsWith("Z") || upper.endsWith("SH") || upper.endsWith("CH"))
         resolvedToken = text + "es";
      else if (upper.endsWith("Y") && !upper.matches(".+[AEO]Y"))
         resolvedToken = text.substring(0, text.length() - 1) + "ies"; // strip Y, add IES
      else if (upper.endsWith("MAN"))
         resolvedToken = text.substring(0, text.length() - 3) + "men"; // strip MAN, add MEN
      else
         resolvedToken = text + "s";
      
      return resolvedToken;
   }

   private static String convertToActor(final Matcher m)
   {
      String resolvedToken;
      String text = m.group(1);
      String upper = text.toUpperCase();

      if (upper.endsWith("E"))
         resolvedToken = text + "r";
      else
         resolvedToken = text + "er";
      
      return resolvedToken;
   }

   private static String convertToGerund(final Matcher m)
   {
      String resolvedToken;
      String text = m.group(1);
      String upper = text.toUpperCase();

      if (upper.endsWith("E"))
         resolvedToken = text.substring(0, text.length() - 1) + "ing"; // strip E, add ING
      else
         resolvedToken = text + "ing";
      
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
         resolved = m.replaceFirst(Matcher.quoteReplacement(satisfied ? ifVal : elseVal));
         if (DEBUG && !token.equals(resolved))
            System.out.println("  resolvePercentCond: [" + token + "] = [" + resolved + "]");
      }
      
      m = Constants.NUMERIC_CONDITION.matcher(resolved);
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

         resolved = m.replaceFirst(Matcher.quoteReplacement(satisfied ? ifVal : elseVal));
         if (DEBUG && !token.equals(resolved))
            System.out.println("  resolveNumericCondition: [" + token + "] = [" + resolved + "]");
      }
      
      m = Constants.PERCENT_CHANCE.matcher(resolved);
      if (m.matches())
      {
         int percent = Integer.parseInt(m.group(1));
         String val = m.group(2);
         int roll = RandomEntry.get(100);
         boolean satisfied = roll <= percent;
         resolved = m.replaceFirst(Matcher.quoteReplacement(satisfied ? val : ""));
         if (DEBUG && !token.equals(resolved))
            System.out.println("  resolvePercent: [" + token + "] = [" + resolved + "]");
      }
      
      m = Constants.CONDITION.matcher(resolved);
      if (m.matches())
      {
         String left = m.group(1);
         String right = m.group(2);
         String ifVal = m.group(3);
         String elseVal = m.group(4);
         if (elseVal == null)
            elseVal = "";

         boolean satisfied = left.equalsIgnoreCase(right);
         resolved = m.replaceFirst(Matcher.quoteReplacement(satisfied ? ifVal : elseVal));
         if (DEBUG && !token.equals(resolved))
            System.out.println("  resolveCondition: [" + token + "] = [" + resolved + "]");
      }
      
      return resolved;
   }

   private static final String F = Constants.FILTER_CHAR; 
   private static final String NOT_F = "[^" + F + "]+";
   private static final Pattern FILTER = Pattern.compile("(" + F + NOT_F + ")\\|(" + NOT_F + F + ")");
   
   private static String resolveOneOfs(final String token)
   {
      String resolved = token;
      Matcher m = Constants.ONE_OF_PATTERN.matcher(resolved);
      if (m.matches())
      {
         // hack to allow alteration in regex filters ({Color#.*a|e.*#|Color#.*i|o.*#})
         Matcher fm = FILTER.matcher(resolved);
         while (fm.find())
            resolved = fm.replaceAll("$1!!$2"); // replace "|" with "!!"
         
         m = Constants.ONE_OF_PATTERN.matcher(resolved);
         if (m.matches()) // make sure there's still alteration to split
         {
            // add a terminator, in case the last token is empty
            String[] tokens = Token.tokenizeAllowEmpty(m.group(1) + Constants.ONE_OF_CHAR, Constants.ONE_OF_CHAR);
            int roll = RandomEntry.get(tokens.length);
            resolved = m.replaceFirst(Matcher.quoteReplacement(tokens[roll]));
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
         xrefTbl = tableOrScriptName.toLowerCase(); // ignore table/script case
         
         if (xrefCol == null) // support default columns
            xrefCol = xrefTbl;
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

         if (DEBUG || TEST)
            System.out.println("Macros [" + token + "] T[" + xrefTbl + "] S[" + xrefSub + "] C[" + xrefCol + "] F[" + xrefFil + "]");

         // avoid infinite loop references
         if (TOKEN_STACK.contains(token))
         {
            int depth = 0;
            for (String s : TOKEN_STACK)
               if (s.equals(token))
                  depth++;
            if (depth > 1) // allow limited recursion (e.g., for Potion of Delusion)
            {
               System.out.flush();
               System.err.println("Recursive token error: " + token);
               System.err.println("Macros [" + token + "] T[" + xrefTbl + "] S[" + xrefSub + "] C[" + xrefCol + "] F[" + xrefFil + "]");
               for (String s : TOKEN_STACK)
                  System.err.println(" > " + s);
               System.err.flush();
               String loop = getInvalidTableToken(xrefTbl, xrefSub, xrefCol, xrefFil);
               resolved = m.replaceFirst(Matcher.quoteReplacement(loop));
            }
         }

         if (entry.equals(resolved))
         {
            TOKEN_STACK.push(token);

            for (String t : TOKEN_STACK) System.out.print(t + "> "); // TODO
            String xref = RandomEntry.get(xrefTbl, xrefSub, xrefCol, xrefFil);
            if (xref == null)
            {
               if (!Utils.getStack(null).contains("EventDispatchThread"))
                  System.err.println("Invalid reference: " + entry);
               xref = getInvalidTableToken(xrefTbl, xrefSub, xrefCol, xrefFil);
            }

            resolved = m.replaceFirst(Matcher.quoteReplacement(xref));
            
            String caseSample = token;
            if (caseSample.equals(Constants.LAST_RESOLVED_TOKEN) && !lastResolved.isEmpty())
               caseSample = lastResolved.lastElement();
            resolved = matchCase(caseSample, resolved);

            TOKEN_STACK.pop();
         }

         if (DEBUG && !entry.equals(resolved))
            System.out.println("  resolveTables: [" + entry + "] = [" + resolved + "]");
      }

      return resolved;
   }

   static class Assignment
   {
      private String line;
      private Table table;
      
      public Assignment(final String tableName)
      {
         this.table = Table.getTable(tableName);
         this.line = RandomEntry.get(tableName, null, null, null);
      }
      
      public String get(final String columnName)
      {
         return table.getColumnValue(line, columnName);
      }
      
      @Override
      public String toString()
      {
         return table.getName() + "(" + line + ")";
      }
   }
   
   private static String resolveAssignments(final String token)
   {
      String resolvedToken = token;

      Matcher m = TABLE_ASSIGNMENT.matcher(resolvedToken);
      if (m.find()) // store the assigned value
      {
         tableAssignments.put(m.group(1), new Assignment(m.group(2)));
         resolvedToken = m.replaceFirst("");
         if (DEBUG)
            System.out.println("  assignment: " + m.group(1) + " := [" + m.group(2) + "]");
      }
      else if ((m = PRIMITIVE_ASSIGNMENT.matcher(resolvedToken)).find()) // store the assigned value
      {
         primitiveAssignments.put(m.group(1).toUpperCase(), m.group(2));
         resolvedToken = m.replaceFirst("");
         if (DEBUG)
            System.out.println("  assignment: " + m.group(1) + " = [" + m.group(2) + "]");
      }
      else // extract the desired value (or column) from the stored assignment
      {
         for (Entry<String, Assignment> entry : tableAssignments.entrySet())
         {
            String regex = "\\{" + Pattern.quote(entry.getKey()) + "(?:[.]" + Constants.COLUMN_NAME + ")?\\}";
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            m = p.matcher(resolvedToken);
            if (m.find())
            {
               Assignment a = entry.getValue();
               String value = m.groupCount() == 1 ? a.get(m.group(1)) : a.line;
               resolvedToken = m.replaceFirst(Matcher.quoteReplacement(value));
               resolvedToken = matchCase(token, resolvedToken);
            }
         }
         
         for (Entry<String, String> entry : primitiveAssignments.entrySet())
         {
            String regex = "\\{" + Pattern.quote(entry.getKey()) + "\\}";
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            m = p.matcher(resolvedToken);
            if (m.find())
            {
               resolvedToken = m.replaceFirst(Matcher.quoteReplacement(entry.getValue()));
               resolvedToken = matchCase(token, resolvedToken);
            }
         }
      }
      
      if (DEBUG && !token.equals(resolvedToken))
         System.out.println("  resolveAssignments: [" + token + "] = [" + resolvedToken + "]");
      return resolvedToken;
   }


   // Make the case of the resolved token match the case of the token.
   
   private static final Pattern TO_CAPITALIZE = Pattern.compile("\\b([a-z])");
   private static final Pattern CONTRACTIONS = Pattern.compile("'D|'Ll|'Nt|'Re|'S|'T|'Ve");
   private static final Pattern NO_CAP = 
      Pattern.compile(" A | An | And | At | By | For | From | In | Of | On | Or | The | To | With ");

   private static String matchCase(final String token, final String resolved)
   {
      String caseMatched = resolved;
      if (resolved.isEmpty())
         ; // nothing to do
      else if (resolved.length() > 1 && resolved.toUpperCase().equals(resolved))
         ; // do nothing; leave the value all uppercase (for acronyms)
      else if (token.toLowerCase().equals(token)) // all lower
         caseMatched = resolved.toLowerCase();
      else if (token.toUpperCase().equals(token)) // all upper
         ; // do nothing; leave the value as-is (good for sentences)
      else // cap-init all words
      {
         Matcher m;
         while ((m = TO_CAPITALIZE.matcher(caseMatched)).find())
            caseMatched = m.replaceFirst(Matcher.quoteReplacement(m.group(1).toUpperCase()));
         // hacks to fix possessive suffix and contractions TODO not working
         while ((m = CONTRACTIONS.matcher(caseMatched)).find())
            caseMatched = m.replaceFirst(Matcher.quoteReplacement(m.group(0).toLowerCase()));
         // don't cap some words
         while ((m = NO_CAP.matcher(caseMatched)).find())
            caseMatched = m.replaceFirst(Matcher.quoteReplacement(m.group(0).toLowerCase()));
      }
      // System.out.println("Macros.matchCase() TOKEN [" + token + "]  RESOLVED [" + resolved + "]  MATCHED: [" + caseMatched + "]");
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
      Macros.resolve("Barsoom Plot", "{:Villain}", null); // subset short-cut
      Macros.resolve(null, "2 * 5 = {=2*5}", null);
      Macros.resolve(null, "Alteration = {Alteration}", null);
      Macros.resolve(null, "Description: {Color}{20%?, with bits of {Reagent} floating in it}", null);
      Macros.resolve(null, "Simple Alteration: {one|two}", null);
      Macros.resolve(null, "Filter Alteration = {Spell#.*(?:walk|fall).*#}", null);
      Macros.resolve(null, "Filter and One-Of Alteration = {{Color#.*a|e.*#}|{Color#.*i|o.*#}}", null);
      Macros.resolve(null, "Filter Variable: {Color:Simple{!OneWord}}", null);
      Macros.resolve(null, "Filter: {Color:Simple#S.+#}", null);
      Macros.resolve(null, "Filter: {Name#S.+#}", null);
      Macros.resolve(null, "Filter: {Noise#S.+#}", null);
      Macros.resolve(null, "{#text:.}", null); // filtered token
      Macros.resolve(null, "{#{Profession+#.*craftsman.*#}:^(.*?)  }", null); // filter vs all with group
      Macros.resolve(null, "{50%?Yes:No}", null);
      Macros.resolve(null, "{Color} " + Constants.LAST_RESOLVED_TOKEN, null);
      Macros.resolve(null, "{Color} {Fauna#{#{!}:.}.*#}", null); // back-reference with a filter
      Macros.resolve(null, "{equipment+}", null); // full line
      Macros.resolve(null, "{equipment}", null); // default column
      Macros.resolve(null, "{H=Herb} Herb: {H.Herb} Cost: {H.Cost}", null); // assignment
      Macros.resolve(null, "{Island Event}", null);
      Macros.resolve(null, "{Metal" + Constants.SUBSET_CHAR + "}", null);
      Macros.resolve(null, "{Profession.all#^([^ ]+) .*craftsman.*#}", null); // filter vs all with group
      Macros.resolve(null, "{Profession.Job}", null); // composite column
      Macros.resolve(null, "{Profession:Craftsman}", null); // filter subset
      Macros.resolve(null, "{Profession:Criminal}", null); // filter subset
      Macros.resolve(null, "{thing+} {moss+} {fly+} {mouse+} {fox+}", null); // test plurals
      Macros.resolve(null, "{~last, first} / {~chain, gold, fine} / {~boat (large)}", null); // formatter
      
      Macros.resolve("Meat", "{Meat:Game#.*r.*#}", null);
      Macros.resolve(null, "{Appearance} {Herb{!MaybeSameFirst}}", null);
      Macros.resolve(null, "{Appearance} {Herb{!OneWithSame}}", null);
      Macros.resolve(null, "{Appearance} {Herb{!SameFirst}}", null);
      Macros.resolve(null, "{Appearance} {Herb{{!OneWithSame|!OneWord}}}", null);
      Macros.resolve(null, "{Appearance} {Humanoid{!OneMaybeSame}}", null);
      Macros.resolve(null, "{Herb{!OneWord}}", null);
      Macros.resolve(null, "{Humanoid#T[A-Z]+$#}", null);
      Macros.resolve(null, "{Name}", null);
      Macros.resolve(null, "{Spell#.*(?:walk|fall).*#}", null);
      Macros.resolve(null, "{Weapon{!OneWord}}", null);
      Macros.resolve(null, "{Weapon}", null);
      Macros.resolve(null, "{contents}", null);
      
      System.out.println("Aa: " + Macros.matchCase("Aa", "cap each word's first letter in the phrase"));
      System.out.println("AA: " + Macros.matchCase("AA", "Leave ALL words in the phrase alone"));
      System.out.println("aa: " + Macros.matchCase("aa", "Lower Case ALL words in the phrase"));
      */
      
      // Macros.resolve(null, "{one|two}", null);
      // Macros.resolve(null, "{Spell#.*walk.*#}", null);
      // Macros.resolve(null, "{Spell#.*(?:walk|fall).*#}", null);
      // Macros.resolve(null, "{C={Generated Name:Consonant}}{v={generated name:vl}}{C}{v}", null);
      
      // Macros.resolve(null, "{VAR={Inn Name}}VAR = {VAR}; Var = {Var}; var = {var}", null);
      // Macros.resolve(null, "{VAR:=Herb}VAR = {VAR.HERB}; Var = {Var.Herb}; var = {var.herb}", null);
      Macros.resolve(null, "{DiffTest}/{DiffTest}/{DiffTest{!Different}}", null);
   }
}
