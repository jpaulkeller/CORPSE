package str;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A convenience class for using regular expressions.  This class
 * provides public constants for regular expression patterns.  When
 * used, the resulting Pattern objects are cached.
 *
 * By default, the comparison is case-sensitive.  However, if the
 * given pattern contains no uppercase characters, it will be
 * case-insensitive.
 *
 * Each public pattern will be a valid regular expression (within
 * non-saved grouping operators).
 *
 * @see comet.gui.form.RegexValidator
 */
public final class RegExp
{
   /** A number from 0 to 255 (without leading zeros). */
   public static final String OCTET = "(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])";
   /** A 4-octet Internet Protocol (IP) address. */
   public static final String IP4 = "(?:" + OCTET + "[.]){3}" + OCTET;
   /** An Internet Protocol (IP) address. */
   public static final String IP = "(?:" + IP4 + ")";
   /** A machine name. */
   public static final String HOST = "(?:[A-Za-z]+[-_A-Za-z0-9.]*)";
   /** An IP address or host name. */
   public static final String IP_OR_HOST = "(?:" + IP + "|" + HOST + ")";

   /** Text containing XML tags. */
   public static final String XML = "(?:<[^ ].*>.*</[^ ].*>)";

   private static Map<String, Pattern> cache = new HashMap<String, Pattern>();

   private RegExp() { }
   
   /**
    * Return the RE object for the given regular expression pattern.
    * This will return null (but will not throw an exception) if the
    * pattern in invalid. */

   public static Pattern get (final String regex)
   {
      Pattern pattern = cache.get (regex);
      
      if (pattern == null)
      {
         try
         {
            // this is a hack to support case-insensitive comparisons
            if (regex.equals (regex.toLowerCase()))
               pattern = Pattern.compile (regex, Pattern.CASE_INSENSITIVE);
            else
               pattern = Pattern.compile (regex);
            cache.put (regex, pattern);
         }
         catch (PatternSyntaxException x)
         {
            System.err.println ("Invalid regular expression: " + regex);
            System.err.println (x);
         }
      }

      return (pattern);
   }

   /**
    * Returns true if the given regex pattern is a valid regular expression.
    */
   public static boolean isValid (final String regex)
   {
      return (get (regex) != null);
   }
   
   /**
    * Returns the Matcher if the given target matches the given
    * regular expression pattern, otherwise null.
    */
   public static Matcher getMatch (final String regex, final String target)
   {
      if (target == null)
         return (null);
      Pattern pattern = get (regex);
      Matcher matcher = null;
      if (pattern != null)
      {
         matcher = pattern.matcher (target);
         if (!matcher.find())
            matcher = null;
      }
      return matcher;
   }
   
   /**
    * Returns true if the given target matches the given regular
    * expression pattern. */
   
   public static boolean contains (final String regex, final String target)
   {
      return (getMatch (regex, target) != null);
   }
   
   /**
    * Returns true if the given target fully matches the given regular
    * expression pattern. The regex will be wrapped with "^" and "$".
    */
   public static boolean matches (final String regex, final String target)
   {
      return (contains ("^" + regex + "$", target));
   }
   
   /**
    * Returns the requested token from the target string which matches
    * the <i>token</i>th sub-pattern within the given pattern.
    */
   public static String getToken (final String regex, final String target, 
                                  final int token)
   {
      Matcher matcher = getMatch (regex, target);
      if (matcher != null && token <= matcher.groupCount())
         return (matcher.group (token));
      return (null);
   }

   /**
    * Replaces all occurrences of the pattern in the input with the
    * replacement. This should typically be used for complex
    * replacements, where the replacement string contains references
    * to capture groups in the pattern (for example, $1 would be
    * replaced with the first group, etc).
    *
    * If the pattern is not found in the input, the input is returned
    * unchanged.
    */
   public static CharSequence replaceAll (final CharSequence input,
                                          final Pattern pattern,
                                          final String replacement)
   {
      CharSequence output = input;

      Matcher m = pattern.matcher (input);
      if (m.find())
      {
         StringBuffer buf = new StringBuffer();
         m.reset();
         while (m.find())
            m.appendReplacement (buf, replacement);
         m.appendTail (buf);
         output = buf;
      }
      return output;
   }

   /**
    * A destructive version of replaceAll that will modify the given
    * StringBuffer.
    */
   public static void overwrite (final StringBuffer target,
                                 final Pattern pattern,
                                 final String replacement)
   {
      CharSequence output = replaceAll (target, pattern, replacement);
      if (output != target)
      {
         target.setLength (0);
         target.append (output);
      }
   }
   
   /**
    * A destructive version of replaceAll that will modify the given
    * StringBuilder.
    */
   public static void overwrite (final StringBuilder target,
                                 final Pattern pattern,
                                 final String replacement)
   {
      CharSequence output = replaceAll (target, pattern, replacement);
      if (output != target)
      {
         target.setLength (0);
         target.append (output);
      }
   }
   
   private static void test (final String name, final String regex, final String target)
   {
      System.out.print ("RegExp.match (" + name + ", \"" +
                        target + "\") => ");
      System.out.println (RegExp.matches (regex, target));
   }

   public static void main (final String[] args)
   {
      test ("RegExp.IP", RegExp.IP, "123.123.123.123");
      test ("RegExp.IP", RegExp.IP, "123.123.123,123"); // should be false
      test ("RegExp.IP", RegExp.IP, "123.123.123.256"); // should be false
      test ("RegExp.HOST", RegExp.HOST, "host");
      test ("RegExp.HOST", RegExp.HOST, "123"); // should be false
      test ("RegExp.IP_OR_HOST", RegExp.IP_OR_HOST, "1.2.3.4");
      test ("RegExp.IP_OR_HOST", RegExp.IP_OR_HOST, "host");
      
      test ("RegExp.XML", RegExp.XML, "normal");
      test ("RegExp.XML", RegExp.XML, "<b>BOLD</b>");
      
      String s = RegExp.getToken
      ("select .* from +(.+) *", "select FIELD from TABLE", 1);
      System.out.println ("RegExp.getToken => " + s + "\n");
      
      // test replaceAll (output should be: night nighttime monday)
      Pattern p = Pattern.compile ("\\bday(.*?)\\b");
      s = "day daytime monday";
      System.out.print ("replaceAll (" + s + ", night$1) => ");
      s = replaceAll (s, p, "night$1").toString();
      System.out.println (s);
      
      // test overwrite (output should be: day daytime monday)
      p = Pattern.compile ("\\bnight(.*?)\\b");
      StringBuilder buf = new StringBuilder (s);
      System.out.print ("overwrite (" + buf + ", day$1) => ");
      overwrite (buf, p, "day$1");
      System.out.println (buf);
   }
}
