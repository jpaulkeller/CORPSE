package words;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.TextExtractor;

public class Words extends TreeSet<String>
{
   private static final long serialVersionUID = 1L;
   
   private static final Pattern WORD = 
      Pattern.compile ("\\b(\\w+)\\b", Pattern.MULTILINE | Pattern.DOTALL);
   
   public void load (File file)
   {
      try
      {
         FileInputStream fis = new FileInputStream (file);
         InputStreamReader isr = new InputStreamReader (fis, "UTF8");
         BufferedReader br = new BufferedReader (isr);
         String line = null;
         while ((line = br.readLine ()) != null)
            add (line);
         fis.close ();
      }
      catch (IOException x)
      {
         System.err.println (x);
      }
      System.out.println (file + ": " + size());
   }
   
   public void load (TextExtractor ext)
   {
      if (ext.isValid())
      {
         String text = ext.getText();
         Matcher m = WORD.matcher (text);
         while (m.find())
            add (m.group (1));
      }
      
      if (ext.getFile() != null)
         System.out.println (ext.getFile() + ": " + size());
      else
         System.out.println ("clipboard: " + size());
   }

   void findReverse()
   {
      List<String> matches = new ArrayList<String>();
      StringBuilder sb = new StringBuilder();

      for (String word : this)
      {
         sb.setLength (0);
         sb.append (word);
         sb.reverse();
         String reverse = sb.toString();
         if (!matches.contains (word) && !word.equals (reverse) && contains (reverse))
         {
            System.out.println (word + " <==> " + reverse);
            matches.add (word);
            matches.add (reverse);
         }
      }
      System.out.println ("\nReverse Complete");
   }
   
   void findContains (String letters)
   {
      System.out.println ("Find words containing: " + letters);
      for (String word : this)
      {
         int matched = 0;
         int len = letters.length();
         for (int i = 0; i < len; i++)
            if (word.indexOf (letters.charAt (i)) >= 0)
               matched++;
            else
               break;
         if (matched == len)
            System.out.println (" > " + word);
      }
      System.out.println ("Complete");
   }

   public static void main (String[] args)
   {
      Words words = new Words();
      words.load (new File ("C:/pkgs/workspace/Words/english.words")); 
      
      if (args[0].equals ("reverse"))
         words.findReverse();
      else if (args[0].equals ("contains"))
         words.findContains (args[1]);
   }
}