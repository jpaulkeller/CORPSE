package gui.form.valid;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JFileChooser;

public class FileValidator extends RegexValidator
{
   private static final long serialVersionUID = 1;

   private boolean mustExist;
   private boolean allowHTTP;
   private int mode = -1;
   private String[] suffixes;

   public FileValidator (final boolean mustExist)
   {
      setMustExist (mustExist);
      // default to valid for non-null files only (caller can change)
      setNullValidity (false);
   }
   
   public void setMustExist (final boolean mustExist)
   {
      this.mustExist = mustExist;
   }
   
   public void setAllowHTTP (final boolean allowHTTP)
   {
      this.allowHTTP = allowHTTP;
   }

   public void setMode (final int mode)
   {
      this.mode = mode;
   }
   
   public void setRegex (final String regex)
   {
      if (regex != null)
         setMustMatchAny (regex);
      else
         setMustMatchAny ((String[]) null);
   }
   
   public void setSuffixes (final String... suffixes)
   {
      this.suffixes = suffixes;
   }

   @Override
   public boolean isValid (final Object value)
   {
      if (value == null)
         return isNullValid();
      
      File file = (File) value;
      
      if (allowHTTP && file.getPath().toLowerCase().startsWith ("http:"))
         return true; 
         
      if (mustExist && !file.exists())
         return false;

      if (mode == JFileChooser.FILES_ONLY && !file.isFile())
         return false;
      else if (mode == JFileChooser.DIRECTORIES_ONLY && !file.isDirectory())
         return false; 
            
      if (!hasValidSuffix (file.getPath()))
         return false;
      
      return super.isValid (value); // check regex
   }

   // ensure the file has the right suffix
   
   private boolean hasValidSuffix (final String path)
   {
      if (suffixes == null || suffixes.length == 0)
         return true;
      
      String pathLower = path.toLowerCase(); 
      for (String suffix : suffixes)
         if (pathLower.endsWith ("." + suffix.toLowerCase()))
            return true;
      return false;
   }

   public static void main (final String[] args) // for testing
   {
      String[] fileArray = new String[] { null, System.getProperty ("user.home"), "a.txt" };
      Collection<String> files = Arrays.asList (fileArray);
      for (String fileName : files)
      {
         FileValidator v = new FileValidator (false); // false == need not exist
         
         File file = fileName != null ? new File (fileName) : null;
         v.setMode (JFileChooser.FILES_ONLY);
         System.out.println (file + (v.isValid (file) ? " is a file" : " is not a file"));
         v.setMode (JFileChooser.DIRECTORIES_ONLY);
         System.out.println
            (file + (v.isValid (file) ? " is a directory" : " is not a directory"));
         v.setMode (-1);
         
         String regex = ".*[.]txt";
         v.setRegex (regex);
         System.out.println
            (file + (v.isValid (file) ? " matches" : " does not match") + ": " + regex);
         v.setRegex (null);
         
         v.setMustExist (true);
         System.out.println (file + (v.isValid (file) ? " exists" : " does not exist"));
      }
   }
}
