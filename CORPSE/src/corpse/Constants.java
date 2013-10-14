package corpse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

public class Constants
{
   static final String COLUMN_CHAR    = "@";
   static final String COMMENT_CHAR   = "*";
   static final String FILTER_CHAR    = "#";
   static final String FOOTNOTE_CHAR  = "]";
   static final String HEADER_CHAR    = ".";
   static final String INCLUDE_CHAR   = "+";
   static final String ONE_OF_CHAR    = "|";
   static final String REF_CHAR       = "!";
   static final String SEPR_CHAR      = "-";
   static final String SOURCE_CHAR    = "?";
   static final String SUBSET_CHAR    = ":";
   static final String TITLE_CHAR     = "!";

   private static final String IGNORE = COMMENT_CHAR + HEADER_CHAR + FOOTNOTE_CHAR + SEPR_CHAR + SOURCE_CHAR + TITLE_CHAR; 
   static final Pattern COMMENT_LINE = Pattern.compile ("^[" + Pattern.quote (IGNORE) + "]", Pattern.MULTILINE);
   
   static final String COMMENT = "(?:\\s*//.*$)?";
   
   static final String NAME = "([A-Z](?: ?[-_A-Z0-9]+){0,10})"; // use {0,10} to avoid infinite loop
   static final String COLUMN_NAME = "([A-Z0-9](?: ?[-_A-Z0-9/()]+){0,10})";
   private static final String TABLE_NAME = NAME;
   
   static final Pattern NAME_PATTERN = Pattern.compile(NAME, Pattern.CASE_INSENSITIVE);
   static final Pattern SIMPLE_TABLE = Pattern.compile(TABLE_NAME, Pattern.CASE_INSENSITIVE);
   static final Pattern TOKEN = Pattern.compile ("\\{([^{}]+)\\}");
   
   // [50/50] CONDITION (all-or-nothing format): {{2}=2?ALL}
   // [50/50] CONDITION (either/or format)     : {{2}=2?YES:NO}
   // [70/30] CONDITION (using > operator)     : {{10}>7?RARE:COMMON}
   
   // Note: the first element of the inner expression (e.g., {2}) will be 
   // resolved prior to the evaluation the outer expression, and so the 
   // first group of the pattern includes just the number.  The first element
   // may be any numeric expression supported by Quantity.java.
   // BELL-CURVE CONDITION (embedded roll)     : {{3d6}<13?Normal:Good} 
   static final Pattern CONDITION = Pattern.compile ("\\{(\\d+)([=<>])(\\d+)[?]([^:]+)(?::([^:{}]+))?\\}");

   // {one|two|three|four} -- chooses one option, with equal chance for each 
   static final Pattern ONE_OF = Pattern.compile ("\\{([^|{]+([|][^|{]*)+)\\}");
   // TODO: empty option:  {opt1|opt2|}
   // TODO: weighted options: {#:opt1|#:opt2|...}

   // {prompt?default} where the default value is optional, and the prompt must
   // start with a non-numeric (to avoid confusion with the CONDITIONAL token).
   static final Pattern QUERY = Pattern.compile ("\\{([^{}?0-9][^{}?]+?)[?]([^{}]+)?\\}");

   private static final String QTY = "(?:(\\d+)\\s+)?";
   private static final String SUBSET = "(?:\\" + SUBSET_CHAR + NAME + "?)?";
   private static final String COLUMN = "(?:\\" + COLUMN_CHAR + COLUMN_NAME + "?)?";
   private static final String FILTER = "(?:\\" + FILTER_CHAR + "([^}]+)?)?";
   
   // {1 Table:Subset@Column#Filter} 
   private static final String TABLE_REF_REGEX = TABLE_NAME + SUBSET + COLUMN + FILTER; 
   static final Pattern TABLE_XREF = Pattern.compile ("\\{" + QTY + TABLE_REF_REGEX + "\\s*\\}", Pattern.CASE_INSENSITIVE);
   // {Subset@Column#Filter} -- short-cut for a subset reference (Within the table) 
   static final Pattern SUBSET_REF = Pattern.compile ("\\{" + SUBSET + COLUMN + FILTER + "\\}", Pattern.CASE_INSENSITIVE);

   static final Pattern SCRIPT_XREF = // {1 Script.cmd}
      Pattern.compile ("\\{" + QTY + NAME + "[.]cmd\\}", Pattern.CASE_INSENSITIVE);

   // {#text:regex} (e.g. {#text:.} would resolve to the first letter of the text) 
   static Pattern FILTER_TOKEN = Pattern.compile("\\{" + FILTER_CHAR + "(.+):([^}]+)\\}");
   
   static final Pattern RANDOMIZE_LINE = Pattern.compile ("#\\s*(.*)");
   
   static final Pattern ASSIGNMENT = Pattern.compile ("\\{([^{}]+)=([^{}?]+)\\}");
   
   // includes a script, but ignores any embedded randomize commands? // TODO
   static final Pattern INCLUDE_LINE = Pattern.compile ("[+]\\s*(.*)\\s*");
   
   // + prefix{Table XRef}suffix
   static final Pattern INCLUDED_TBL = 
         Pattern.compile ("^[" + INCLUDE_CHAR + "]\\s*([^{]*)\\{(" + TABLE_REF_REGEX + ")\\}([^{]*)", Pattern.CASE_INSENSITIVE);
   
   static final String LAST_RESOLVED_TOKEN = "{!}";
   
   private static final String VARIABLE_REGEX = "\\{(![^}]*)\\}";
   static final Pattern VARIABLE_TOKEN = Pattern.compile(VARIABLE_REGEX);
   private static final Pattern VARIABLE = Pattern.compile(VARIABLE_REGEX + "=(.+) //.*"); // {!OneWord}=#[^-_ ]+ // one word
   static final Map<String, String> VARIABLES = new HashMap<String, String>();
   static { loadVariables(); }

   private static final Pattern PROPERTY = Pattern.compile("([^=]+)=(.+)"); // noun=nouns
   static final Pattern PLURAL_TOKEN = Pattern.compile("\\{\\+(.+)\\}"); // {+thing} => things
   static final SortedMap<String, String> PLURALS = new TreeMap<String, String>();
   static { loadPlurals(); }

   private static void loadVariables()
   {
      List<String> lines = FileUtils.getList("data/Variables.txt", FileUtils.UTF8, true);
      for (String line : lines)
      {
         Matcher m = VARIABLE.matcher(line);
         if (m.matches())
            VARIABLES.put(m.group(1).toUpperCase(), m.group(2));
      }
      System.out.println(VARIABLES.size() + " variables loaded.");
   }
   
   private static void loadPlurals()
   {
      List<String> lines = FileUtils.getList("data/Plurals.txt", FileUtils.UTF8, true);
      for (String line : lines)
      {
         Matcher m = PROPERTY.matcher(line);
         if (m.matches())
            PLURALS.put(m.group(1).toUpperCase(), m.group(2));
      }
      System.out.println(PLURALS.size() + " plurals loaded.");
   }
   
   public static void main(String[] args)
   {
   }
}
