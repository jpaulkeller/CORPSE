package gui.form;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class provides a simple implementation of the Filter interface
 * for textual data.  Clients can specify a string of characters or a
 * regular expression to indicate which are allowed (or disallowed) in
 * the data.  The regular expression (if provided) takes precedence.
 *
 * Filters provide syntactic validation.  Validators provide semantic
 * validation.  A Filter is applied to each character in the data
 * (unlike a Validator, which is applied to the whole value). Filters
 * are used to prevent the entry of invalid characters.
 *
 * For example, if you wanted postive numbers only, you could filter
 * out the negative sign.  But if you wanted any number, you would
 * allow the negative sign, but validate the value (to ensure the user
 * didn't enter more than one negative sign).
 */
public class TextFilter implements Filter
{
   private static final long serialVersionUID = 4;

   private static Toolkit tk = Toolkit.getDefaultToolkit();

   // maps legal character strings to filters
   private static Map<String, Filter> filters;

   private Pattern regex;       // a pattern matching characaters allowed (or denied)
   private String characters;   // a list of characaters allowed (or denied)

   private boolean allowed = false;     // defaults to allow all
   private boolean audio = true;

   public TextFilter()
   {
   }

   public TextFilter (final String characters, final boolean allowed)
   {
      setAllowed (allowed);
      setCharacters (characters);
   }

   public TextFilter (final String regexPattern)
   {
      setRegex (regexPattern);
      setAllowed (true);
   }

   public TextFilter (final String regexPattern, final int flags)
   {
      setRegex (regexPattern, flags);
   }

   public void setAllowed (final boolean allowed)
   {
      this.allowed = allowed;
   }

   public void setRegex (final String regexPattern)
   {
      this.regex = Pattern.compile (regexPattern);
   }

   public void setRegex (final String regexPattern, final int flags)
   {
      this.regex = Pattern.compile (regexPattern, flags);
   }

   public void setCharacters (final String characters)
   {
      this.characters = characters;
   }

   public String getCharacters()
   {
      return characters;
   }

   /**
    * Filters the given input character by returning a null character
    * if it does not satisfy the filter criteria.
    */
   public char process (final char input)
   {
      boolean found = false;

      if (regex != null)
         found = regex.matcher (input + "").matches();
      else if (characters != null)
         found = characters.indexOf (input) >= 0;

      boolean trap = ((found != allowed) && (input != ''));
      if (audio && trap &&
          (input != KeyEvent.VK_BACK_SPACE) &&
          (input != KeyEvent.VK_DELETE))
         tk.beep();
      return trap ? 0 : input;
   }

   /**
    * Filters the given input string by removing any characters which
    * do not satisfy the filter criteria.
    */
   public String process (final String input)
   {
      boolean saveAudio = audio;
      enableAudio (false);      // disable during processing loop

      StringBuffer buf = new StringBuffer();
      int len = input.length();
      char c;
      for (int i = 0; i < len; i++)
      {
         c = process (input.charAt (i));
         if (c > 0)
            buf.append (c);
      }

      String output = buf.toString();
      enableAudio (saveAudio);
      if (audio && !input.equals (output))
         tk.beep();
      return output;
   }

   /**
    * Enable or disable audio feedback (a single beep) when filtering data.
    */
   public void enableAudio (final boolean status)
   {
      this.audio = status;
   }

   /**
    * These static method accesses a local cache to allow similar
    * filters to be shared.  Caution: if you use a shared filter, you
    * should not change it.
    */
   public static TextFilter getFilter (final String charactersAllowed)
   {
      if (filters == null)
         filters = new HashMap<String, Filter>();

      TextFilter filter = (TextFilter) filters.get (charactersAllowed);
      if (filter == null)
      {
         filter = new TextFilter (charactersAllowed, true);
         filters.put (charactersAllowed, filter);
      }

      return filter;
   }

   public static TextFilter getRegexFilter (final String regexAllowed)
   {
      if (filters == null)
         filters = new HashMap<String, Filter>();

      TextFilter filter = (TextFilter) filters.get (regexAllowed);
      if (filter == null)
      {
         filter = new TextFilter (regexAllowed);
         filters.put (regexAllowed, filter);
      }

      return filter;
   }

   public static void main (final String[] args)
   {
      String data;
      if (args.length > 0)
         data = args[0];
      else
         data = "-ABC123 $.456";

      TextFilter tf = TextFilter.getFilter ("01234567890");
      System.out.print ("tf.process (" + data + ") => ");
      System.out.println (tf.process (data));

      tf = TextFilter.getRegexFilter ("[-0-9.]");
      System.out.print ("tf.process (" + data + ") => ");
      System.out.println (tf.process (data));

      System.exit (0);
   }
}
