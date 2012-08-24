package corpse;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

// TBD: use a FIRST-like annotated-HTML GUI.  Click on the random entries
// to re-roll or select a new value.

public class Script
{
   private static final long serialVersionUID = 1L;
   
   // includes a script, but ignores any embedded randomize commands? // TBD
   static final Pattern INCLUDE_LINE   = Pattern.compile ("[+] *(.*) *");
   
   static final Pattern RANDOMIZE_LINE = Pattern.compile ("# *(.*)");
   
   static final Pattern ASSIGNMENT = Pattern.compile ("\\{([^{}]+)=([^{}]+)\\}");
   
   // {prompt?default} where the default value is optional, and the prompt must
   // start with a non-numeric (to avoid confusion with the CONDITION token).
   static final Pattern QUERY =
      Pattern.compile ("\\{([^{}?0-9][^{}?]+?)[?]([^{}]+)?\\}");
   
   private File file;
   private Map<String, String> variables = new HashMap<String, String>();

   public Script (final String path)
   {
      file = new File (path);
   }
   
   public String resolve() // TBD: thread
   {
      StringBuilder buf = new StringBuilder();
      
      try
      {
         FileInputStream fis = new FileInputStream (file);
         InputStreamReader isr = new InputStreamReader (fis);
         BufferedReader br = new BufferedReader (isr);
         Matcher m;
         
         String line = null;
         while ((line = br.readLine()) != null)
         {
            if (Macros.COMMENT_LINE.matcher (line).matches())
               continue;
            if (line.equals ("")) // ignore blank lines
               continue;
            
            int prevStart = -1;
            while ((m = Macros.TOKEN.matcher (line)).find()) // loop for nested tokens
            {
               if (m.start() != prevStart)
               {
                  line = resolve (line);
                  prevStart = m.start();
               }
               else
                  line = m.replaceFirst ("<$1>"); // avoid infinite loop
            }
            
            if ((m = INCLUDE_LINE.matcher (line)).matches())
               buf.append (include (m.group (1)));
            else if ((m = RANDOMIZE_LINE.matcher (line)).matches())
               randomize (m.group (1));
            else if (!line.equals (""))
               buf.append (line + "\n");
         }
         
         fis.close();
      }
      catch (IOException x)
      {
         buf.append (x.getMessage());
         x.printStackTrace (System.err);
      }
      
      return buf.toString();
   }

   public String resolve (final String entry)
   {
      String resolvedEntry = entry;
      resolvedEntry = resolveVariables (resolvedEntry);
      resolvedEntry = resolveAssignments (resolvedEntry);
      resolvedEntry = resolveQueries (resolvedEntry);
      resolvedEntry = Macros.resolve (resolvedEntry);
      return resolvedEntry;
   }
   
   private String resolveVariables (final String entry)
   {
      String resolvedEntry = entry;
      for (String variable : variables.keySet())
      {
         String pattern = Pattern.quote ("{" + variable + "}");
         String value = Matcher.quoteReplacement (variables.get (variable));
         resolvedEntry = resolvedEntry.replaceAll (pattern, value); 
      }
      if (Macros.DEBUG && !entry.equals (resolvedEntry))
         System.out.println ("VAR [" + entry + "] = [" + resolvedEntry + "]");
      return resolvedEntry;
   }

   private String resolveAssignments (final String entry)
   {
      String resolvedEntry = entry;
      Matcher m;
      while ((m = ASSIGNMENT.matcher (resolvedEntry)).find())
      {
         variables.put (m.group (1), m.group (2));
         resolvedEntry = m.replaceFirst (Matcher.quoteReplacement (m.group (2)));
      }
      if (Macros.DEBUG && !entry.equals (resolvedEntry))
         System.out.println ("ASN [" + entry + "] = [" + resolvedEntry + "]");
      return resolvedEntry;
   }
   
   private String resolveQueries (final String entry)
   {
      String resolvedEntry = entry;
      Matcher m;
      while ((m = QUERY.matcher (resolvedEntry)).find())
      {
         Component owner = null; // TBD
         String message = m.group (1);
         String defaultValue = m.group (2);
         // handle quick-query syntax: Token?? => Token?{Token}                  
         if (defaultValue != null && defaultValue.equals ("?"))
            defaultValue = Macros.resolve ("{" + message + "}");
         String answer = JOptionPane.showInputDialog (owner, message, defaultValue);
         if (answer != null)
            resolvedEntry = m.replaceFirst (Matcher.quoteReplacement (answer));
      }
      if (Macros.DEBUG && !entry.equals (resolvedEntry))
         System.out.println ("QRY [" + entry + "] = [" + resolvedEntry + "]");
      return resolvedEntry;
   }
      
   private String include (final String scriptName)
   {
      Script script = new Script ("data/Scripts/" + scriptName);
      return script.resolve();
   }
   
   private void randomize (final String seed)
   {
      if (seed != null)
      {
         System.out.println ("Randomizing: " + seed);
         RandomEntry.setSeed (seed.hashCode());
      }
      else
         RandomEntry.randomize();
   }
   
   public static void main (final String[] args)
   {
      Table.populate (new File ("data/Tables"));
      // Script script = new Script ("data/Scripts/TREASURE.CMD");
      Script script = new Script ("data/Scripts/NPC.CMD");
      System.out.println (script.resolve());
   }
}
