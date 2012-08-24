package str;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Collection;

/** This class adds some convenience methods to StringTokenizers. */

public final class Token
{
   private Token()
   {
      // Prevent instantiation from outside this class.
   }

   /**
    * Copy StringTokenizer to String[].
    * @param tokenizer - the StringTokenizer to turn into array
    * @return The String array of tokens or null if the tokenizer param
    *         is null or has no tokens.
    */
   public static String[] tokenize (final StringTokenizer tokenizer)
   {
      if (tokenizer == null || tokenizer.countTokens() == 0)
         return null;

      int i = 0;
      String[] tokens = new String[tokenizer.countTokens()];

      while (tokenizer.hasMoreTokens())
      {
         tokens[i] = tokenizer.nextToken();
         i++;
      }

      return tokens;
   }

   /**
    * Creates an array of tokens for the input string using the
    * default delimiter.
    * @param input - The string to be tokenized.
    * @return - The String array created from the input string and default
    *           delimiter.
    */
   public static String[] tokenize (final String input)
   {
      if (input == null || input.length() < 1)
         return null;
      return tokenize (new StringTokenizer (input));
   }

   /**
    * Creates an array of tokens for the input string using the
    * passed in delimiter.
    *
    * @param input - The string to be tokenized.
    * @return - The String array created from the input string and passed in
    *           delimiter.
    */
   public static String[] tokenize (final String input, final String delim)
   {
      return tokenize (new StringTokenizer (input, delim));
   }

   /**
    * Creates an array of tokens for the input string using the
    * passed in delimiter.  Allows empty tokens.
    *
    * Normal uses of tokening will ignore (skip) empty token values
    * that exist between delimiters.
    *
    * For example, (note "//") in string "too/many//secrets".
    *
    * String[] tokens = tokenize ("too/many//secrets", "/");
    * results in the following:
    *
    *   tokens[0]=too
    *   tokens[1]=many
    *   tokens[2]=secrets
    *
    * String[] tokens = tokenizeAllowEmpty ("too/many//secrets", "/");
    * results in the following:
    *
    *   tokens[0]=too
    *   tokens[1]=many
    *   tokens[2]=
    *   tokens[3]=secrets
    *
    * @param input - The string to be tokenized.
    * @param delim - The delimiter to tokenize.
    * @return - The String array created from the input string and passed in
    *           delimiter.
    */
   public static String[] tokenizeAllowEmpty (final String input, final String delim)
   {
      Collection<String> tokens = new ArrayList<String>();

      String s = input;
      int pos = s.indexOf (delim);
      while (pos >= 0)
      {
         tokens.add (s.substring (0, pos));
         if (pos + 1 <= s.length())
         {
            s = s.substring (pos + 1, s.length());
            pos = s.indexOf (delim);
         }
         else
            pos = -1;
      }
      if (s.length() > 0)
         tokens.add (s);

      return tokens.toArray (new String[tokens.size()]);
   }

   /**
    * Tokenizes the <i>input</i>, treating quoted phrases as single tokens.
    */
   public static String[] tokenizeQuoted (final String input,
                                          final String quote,
                                          final String delim)
   {
      StringTokenizer quoted = new StringTokenizer (input, quote, true);
      Collection<String> c = new ArrayList<String>();
      boolean inQuotes = false;
      boolean wroteQuoted = false;

      while (quoted.hasMoreTokens())
      {
         String token = quoted.nextToken();
         if (token.equals (quote))
         {
            inQuotes = !inQuotes;
            if (!inQuotes && !wroteQuoted) // empty quoted string
               c.add ("");
            else
               wroteQuoted = false;
         }
         else if (inQuotes)
         {
            c.add (token);
            wroteQuoted = true;
         }
         else                   // tokenize the non-quoted phrase
         {
            StringTokenizer words;
            if (delim == null)  // delimit by white-space
               words = new StringTokenizer (token);
            else
               words = new StringTokenizer (token, delim);
            while (words.hasMoreTokens())
               c.add (words.nextToken());
         }
      }

      return c.toArray (new String[c.size()]);
   }

   /**
    * Tokenizes a quoted string to ensures if a delimiter character
    * exists in a quoted string it it ignored.  Also, does not close
    * quoted values on escaped quotes (i.e. '' or "").  Also, unlike
    * the other tokenizeQuoted methods, this does not consider
    * adjacent delimiters to be one delimiter.
    *
    *  @param input The string to tokenize.
    *  @param quote The character value of a quote.  This is what will
    *               override the delimiter. If a  delimiter resides within
    *               a set of characters represented here it will be treated
    *               as a regular character and not a delimiter.
    *  @param delim The character value of the delimiter.  Currently only
    *               characters are supported
    *  @return Returns an array of strings containing the tokenized values.  */
   public static String[] tokenizeQuoted (final String input,
                                          final char quote,
                                          final char delim)
   {
      return tokenizeQuoted (input, quote, delim, false);
   }

   /**
    * Tokenizes a quoted string to ensures if a delimiter character
    * exists in a quoted string it it ignored.  Also, does not close
    * quoted values on escaped quotes (i.e. '' or "").  Also, unlike
    * the other tokenizeQuoted methods, this does not consider
    * adjacent delimiters to be one delimiter.
    *
    * @param input The string to tokenize.
    * @param quote The character value of a quote.  This is what will
    *              override the delimiter. If a  delimiter resides within
    *              a set of characters represented here it will be treated
    *              as a regular character and not a delimiter.
    * @param delim The character value of the delimiter.  Currently only
    *              characters are supported
    * @param trim  If this value is set to true, the quotes surrounding a
    *              token value will be removed when it is parsed. (i.e.
    *              /'quoted/value'/ -> quoted/value.)
    * @return Returns an array of strings containing the tokenized values. */

   public static String[] tokenizeQuoted (final String input,
                                          final char quote,
                                          final char delim,
                                          final boolean trim)
   {
      StringBuilder sb         = new StringBuilder (input); // Buffer for input
      StringBuilder token      = new StringBuilder(); // buffer for each token
      Collection<String> tokens = new ArrayList<String>();
      boolean      inQuotes   = false; // boolean to know if we are in quotes
      int          index;       // index point within the sb

      for (index = 0; index < sb.length(); index++)
      {
         char ch = sb.charAt (index);

         // Check for the delimiter and not inQuotes
         if (ch == delim && !inQuotes)
         {
            // add the current token to the list and move on to the next token
            if (token.toString() != null)
               tokens.add (token.toString());
            token = new StringBuilder();
         }
         else if (ch == quote) // Check to see if the char is a quote
         {
            if (sb.length() > index + 1 && sb.charAt (index + 1) == quote)
            {
               // Add the quote to the token (it's an escape char)
               token.append (ch);
               // Add the following quote also to the token
               token.append (sb.charAt (++index));
            }
            else
            {
               // Here we are either entering or leaving the quote
               // Add the quote to the token if not trimmed
               if (!trim)
                  token.append (ch);
               // Reverse the quote boolean
               inQuotes = !inQuotes;
            }
         }
         else // It's a regular character just add it to the Token
            token.append (ch);
      }

      // Write last token out
      if (token.toString() != null)
         tokens.add (token.toString());

      // Return the string array
      return tokens.toArray (new String [tokens.size()]);
   }

   /**
    *  Tokenizes the <i>input</i>, treating quoted phrases as single tokens.
    */
   public static String[] tokenizeQuoted (final String input, final String quote)
   {
      return tokenizeQuoted (input, quote, null);
   }


   /**
    * Tokenizes the given input string, and returns the Nth token
    * (where N = which).  If which is negative, then offset is from
    * the number of tokens.  For example, -1 will return the last
    * token.
    */
   public static String getToken (final String input,
                                  final String quote,
                                  final String delim,
                                  final int which)
   {
      String token = null;
      String[] tokens = Token.tokenizeQuoted (input, quote, delim);

      if ((which >= 0) && (which < tokens.length))
         token = tokens [which];
      else if ((which < 0) && ((tokens.length + which) >= 0))
         token = tokens [tokens.length + which];

      return token;
   }

   /**
    * Tokenizes the given input string, and returns the Nth token
    * (where N = which).  If which is negative, then offset is from
    * the number of tokens.  For example, -1 will return the last
    * token.
    */
   public static String getToken (final String input,
                                  final String quote,
                                  final int which)
   {
      return getToken (input, quote, null, which);
   }

   /** For testing .... */
   public static void main (final String[] args) // for testing
   {
      // test tokenize using default delimiter (" ")
      String s = "this,is,a,,test";
      String[] tokens = Token.tokenize (s, ",");
      System.out.println ("\nToken.tokenize (" + s + "):");
      for (int i = 0; i < tokens.length; i++)
         System.out.println ("  " + tokens[i]);

      // test tokenizeQuoted using default delimiter (" ")
      s = "this is \"a test\" of \"quoted phrases\", some \"\" empty.";
      tokens = Token.tokenizeQuoted (s, "\"");
      System.out.println ("\nToken.tokenizeQuoted (" + s + "):");
      for (int i = 0; i < tokens.length; i++)
         System.out.println ("  " + tokens[i]);

      // test tokenizeQuoted using delimiter
      s = "///first/second/thirdA thirdB";
      tokens = Token.tokenizeQuoted (s, "\"", "/");
      System.out.println ("\nToken.tokenizeQuoted (" + s + ", /):");
      for (int i = 0; i < tokens.length; i++)
         System.out.println ("  " + tokens[i]);

      // test tokenizeQuoted using char (not String) quote and delimiter val
      tokens = Token.tokenizeQuoted (s, '"', '/');
      System.out.println ("\nToken.tokenizeQuoted (" + s + ", /):");
      for (int i = 0; i < tokens.length; i++)
         System.out.println ("  " + tokens[i]);

      System.out.println();
      System.out.print ("Token.getToken (s, \"\\\"\", 1) => ");
      System.out.println (Token.getToken (s, "\"", 1));
      System.out.print ("Token.getToken (s, \"\\\"\", -1) => ");
      System.out.println (Token.getToken (s, "\"", -1));

      // test tokenizeQuoted no trim
      s = "///first/'secondA/secondB' secondC 'secondD ''/'' secondE'/thirdB";
      tokens = Token.tokenizeQuoted (s, '\'', '/');
      System.out.println ("\nToken.TokenizeQuoted(no trim) (" + s + ", /):");
      for (int i = 0; i < tokens.length; i++)
         System.out.println ("  " + tokens[i]);

      // test tokenizeQuoted with trim
      s = "///first/'secondA/secondB' secondC 'secondD ''/'' secondE'/thirdB";
      tokens = Token.tokenizeQuoted (s, '\'', '/', true);
      System.out.println ("\nToken.TokenizeQuoted(with trim) (" + s + ", /):");
      for (int i = 0; i < tokens.length; i++)
         System.out.println ("  " + tokens[i]);

      // test tokenizeAllowEmpty
      s = "Talken/'bout/my//Gen-er-ation";
      tokens = Token.tokenizeAllowEmpty (s, "/");
      System.out.println ("\nToken.TokenizeAllowEmpty (\"" + s + "\", \"/\");");
      for (int i = 0; i < tokens.length; i++)
         System.out.println ("  " + tokens[i]);

      System.out.println();
   }
}
