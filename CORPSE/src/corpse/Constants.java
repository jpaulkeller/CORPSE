package corpse;

import java.util.regex.Pattern;

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
   
   static final Pattern COMMENT_LINE =
      Pattern.compile ("^[" + 
                       Pattern.quote (COMMENT_CHAR + HEADER_CHAR + FOOTNOTE_CHAR + SEPR_CHAR + SOURCE_CHAR + TITLE_CHAR) +
                       "].*", Pattern.MULTILINE);
   
   static final String NAME = "([A-Z](?: ?[-_A-Z0-9]+){0,})";
   static final String COLUMN_NAME = "([-A-Z0-9_/()]+)";
   
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
   // TODO: empty option:  {opt1|opt2|}
   // TODO: weighted options: {#:opt1|#:opt2|...}
   
   static final String TABLE_REGEX = NAME;
   static final Pattern SIMPLE_TABLE = Pattern.compile(TABLE_REGEX, Pattern.CASE_INSENSITIVE);
   
   private static final String QTY = "(?:(\\d+) +)?";
   private static final String SUBSET = "(?:\\" + SUBSET_CHAR + NAME + "?)?";
   private static final String COLUMN = "(?:\\" + COLUMN_CHAR + COLUMN_NAME + "?)?";
   private static final String FILTER = "(?:\\" + FILTER_CHAR + "([^}]+)?)?";
   
   static final Pattern TABLE_XREF = // {# Table:Subset@Column#Filter} 
      Pattern.compile ("\\{" + QTY + TABLE_REGEX + SUBSET + COLUMN + FILTER + " *\\}", Pattern.CASE_INSENSITIVE);

   static final Pattern SCRIPT_XREF = // {# Script.cmd}
      Pattern.compile ("\\{" + QTY + NAME + "[.]cmd\\}", Pattern.CASE_INSENSITIVE);

   static Pattern BACK_REF = Pattern.compile(REF_CHAR + "([^" + REF_CHAR + "]+)" + REF_CHAR);
   
   static final Pattern COLUMN_SEMI = Pattern.compile ("([^;]+)(?: *; *)?"); // TODO what is this?
   
   static final Pattern COLUMN_FIXED = 
      Pattern.compile ("^" + COLUMN_CHAR + " *" + NAME + " +(\\d+) +(\\d+) *", Pattern.CASE_INSENSITIVE);
   static final Pattern COLUMN_CSV = 
      Pattern.compile ("^" + COLUMN_CHAR + " *" + NAME + " *", Pattern.CASE_INSENSITIVE);
   
   // includes a script, but ignores any embedded randomize commands? // TODO
   static final Pattern INCLUDE_LINE = Pattern.compile ("[+] *(.*) *");
   
   static final Pattern RANDOMIZE_LINE = Pattern.compile ("# *(.*)");
   
   static final Pattern ASSIGNMENT = Pattern.compile ("\\{([^{}]+)=([^{}?]+)\\}");
   
   // {prompt?default} where the default value is optional, and the prompt must
   // start with a non-numeric (to avoid confusion with the CONDITIONAL token).
   static final Pattern QUERY = Pattern.compile ("\\{([^{}?0-9][^{}?]+?)[?]([^{}]+)?\\}");
   
   private static final String RANGE_TOKEN = "(\\{[^}]+\\})";
   private static final String SUBSET_REGEX = 
         "^\\" + SUBSET_CHAR + " *" + NAME + " +" + RANGE_TOKEN + "(?: +" + COLUMN_NAME + ")?"; 
   static final Pattern SUBSET_PATTERN = Pattern.compile (SUBSET_REGEX, Pattern.CASE_INSENSITIVE);

   static final Pattern SUBSET_REF = // {Table:Subset}
         Pattern.compile (NAME + SUBSET_CHAR + NAME + "?", Pattern.CASE_INSENSITIVE);
   
   // # {Table}
   static final Pattern WEIGHTED_LINE = Pattern.compile ("^(\\d+) +(.+)");
   // #-# {Table}
   static final Pattern RANGE_LINE = Pattern.compile ("^(\\d+)-(\\d+) +(.+)");
   
   // + {Table} (or "text{Table}text", but at most one {Table}) // TODO: support subset columns
   static final Pattern INCLUDED_TBL = Pattern.compile ("^[" + INCLUDE_CHAR + "] *([^{]*)\\{([^}]+)\\}([^{]*)");
}
