package gui.db;

import gui.form.valid.ValidationAdapter;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexValidator extends ValidationAdapter
{
   private Pattern pattern;
   private int flags;
   private boolean autoWild;
   
   public RegexValidator (final boolean autoAddWildChars, final int flags)
   {
      this.autoWild = autoAddWildChars;
      this.flags = flags;
   }

   @Override
   public boolean isValid (final Object value)
   {
      pattern = null;
      
      String s = (String) value;
      if (s.equals (""))
         return true;

      try
      {
         if (autoWild && !s.startsWith ("^"))
            s = ".*" + s;
         if (autoWild && !s.endsWith ("$"))
            s = s + ".*";
         
         pattern = Pattern.compile (s, flags); // validate regex
         return true;
      }
      catch (PatternSyntaxException x)
      {
         return false;
      }
   }
   
   public Pattern getPattern()
   {
      return pattern;
   }
}
