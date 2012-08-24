package file;

import java.io.File;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

public class RegexFileFilter extends FileFilter
{
   private Pattern pattern;
   private String description;

   public RegexFileFilter (final String regex, final String description)
   {
      pattern = Pattern.compile (regex, Pattern.CASE_INSENSITIVE);
      this.description = description;
   }

   @Override
   public boolean accept (final File f)
   {
      if (f != null)
      {
         if (f.isDirectory())
            return true;
         return (pattern.matcher (f.getPath()).matches());
      }
      return false;
   }

   @Override
   public String getDescription()
   {
      return description;
   }
}
