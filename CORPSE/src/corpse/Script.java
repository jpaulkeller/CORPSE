package corpse;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import file.FileUtils;

// TODO: use a FIRST-like annotated-HTML GUI.  Click on the random entries
// to re-roll or select a new value.

public class Script
{
   // the master list of scripts (names must be unique)
   public static SortedMap<String, Script> scripts = new TreeMap<String, Script>();
   
   // includes a script, but ignores any embedded randomize commands? // TODO
   static final Pattern INCLUDE_LINE = Pattern.compile ("[+] *(.*) *");
   
   static final Pattern RANDOMIZE_LINE = Pattern.compile ("# *(.*)");
   
   static final Pattern ASSIGNMENT = Pattern.compile ("\\{([^{}]+)=([^{}?]+)\\}");
   
   // {prompt?default} where the default value is optional, and the prompt must
   // start with a non-numeric (to avoid confusion with the CONDITIONAL token).
   static final Pattern QUERY = Pattern.compile ("\\{([^{}?0-9][^{}?]+?)[?]([^{}]+)?\\}");
   
   private String name;
   private File file;
   private Map<String, String> variables = new HashMap<String, String>();

   public static void populate (final File dir)
   {
      for (File f : dir.listFiles())
      {
         if (f.isDirectory() && !f.getName().startsWith ("."))
            populate (f);
         else if (f.isFile())
            new Script (f.getPath());
      }
   }
   
   public static Script getScript (final String name)
   {
      Script script = scripts.get (name.toUpperCase());
      if (script == null)
         System.err.println ("Script not yet loaded: " + name);
      return script;
   }
   
   private Script (final String path)
   {
      file = new File (path);
      name = FileUtils.getNameWithoutSuffix (file).toUpperCase();
      Script script = scripts.get(name);
      if (script == null)
         scripts.put (name, this);
      else
         System.err.println("Ignoring duplicate script name: " + path + ", " + script.getFile());
   }
   
   public String getName()
   {
      return name;
   }
   
   public File getFile()
   {
      return file;
   }
   
   public String resolve() // TODO: thread
   {
      StringBuilder buf = new StringBuilder();
      
      BufferedReader br = null;
      try
      {
         FileInputStream fis = new FileInputStream (file);
         InputStreamReader isr = new InputStreamReader (fis);
         br = new BufferedReader (isr);
         Matcher m;
         
         String line = null;
         while ((line = br.readLine()) != null)
         {
            if (Macros.COMMENT_LINE.matcher (line).matches())
               continue;
            if (line.trim().equals ("")) // ignore blank lines
               continue;

            line = resolve(line);
            if (line == null) // user cancelled
               return null;
            
            if ((m = INCLUDE_LINE.matcher (line)).matches())
               buf.append (include (m.group (1)));
            else if ((m = RANDOMIZE_LINE.matcher (line)).matches())
               randomize (m.group (1));
            else if (!line.equals (""))
               buf.append (line + "\n");
         }
      }
      catch (IOException x)
      {
         buf.append (x.getMessage());
         x.printStackTrace (System.err);
      }
      finally
      {
         if (br != null)
            try { br.close(); } catch (IOException x) { }
      }
      
      return buf.toString();
   }

   public String resolve (final String entry)
   {
      String line = entry;
      
      Matcher m;
      while ((m = Macros.TOKEN.matcher (line)).find()) // loop for multiple tokens
      {
         String token = m.group();
         String resolvedToken;
         resolvedToken = resolveVariables (token);
         if (resolvedToken.equals(token))
         {
            resolvedToken = resolveAssignments (token);
            // System.out.println("ASN Token: " + token + " R: " + resolvedToken);
         }
         
         if (resolvedToken.equals(token))
         {
            resolvedToken = resolveQueries (token);
            if (resolvedToken == null) // user cancelled
               return null;
         }
         
         if (resolvedToken.equals(token))
         {
            resolvedToken = Macros.resolve (token, null);
            System.out.println("MAC Token: " + token + " R: " + resolvedToken); // TODO
         }
            
         if (resolvedToken.equals(token))
            line = m.replaceFirst ("<$1>"); // avoid infinite loop
         else
            line = m.replaceFirst(resolvedToken);
         // System.out.println("Line = " + line);
      }
      
      return line;
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
         System.out.println ("resolveVariables: [" + entry + "] = [" + resolvedEntry + "]");
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
         System.out.println ("resolveAssignments: [" + entry + "] = [" + resolvedEntry + "]");
      return resolvedEntry;
   }
   
   private String resolveQueries (final String entry)
   {
      String resolvedEntry = entry;
      
      Component owner = null; // TODO
      String title = "";
      Icon icon = null;
      Object[] options = null; // TODO support multiple choice pattern

      Matcher m;
      while ((m = QUERY.matcher (resolvedEntry)).find())
      {
         String message = m.group (1);
         String defaultValue = m.group (2);
         // handle quick-query syntax: Token?? => Token?{Token}                  
         if (defaultValue != null && defaultValue.equals ("?"))
            defaultValue = Macros.resolve ("{" + message + "}", null);
         String answer = (String) JOptionPane.showInputDialog (owner, message, title, JOptionPane.QUESTION_MESSAGE, 
               icon, options, defaultValue);
         if (answer != null)
            resolvedEntry = m.replaceFirst (Matcher.quoteReplacement (answer));
         else // user cancelled
            return null;
      }
      
      if (Macros.DEBUG && !entry.equals (resolvedEntry))
         System.out.println ("resolveQueries: [" + entry + "] = [" + resolvedEntry + "]");
      return resolvedEntry;
   }
      
   private String include (final String scriptName)
   {
      Script script = new Script ("data/Scripts/" + scriptName); // TODO
      return script.resolve();
   }
   
   private void randomize (final String seed)
   {
      if (seed != null && !seed.isEmpty())
         RandomEntry.setSeed (seed.hashCode());
      else
         RandomEntry.randomize();
   }
   
   public static void main (final String[] args)
   {
      Table.populate (new File ("data/Tables"));
      Script.populate (new File ("data/Scripts"));
      
      // Script script = Script.getScript ("Treasure");
      // Script script = Script.getScript ("NPC");
      Script script = Script.getScript ("Potion");
      String resolved = script.resolve();
      if (resolved != null)
         System.out.println (resolved);
      
      //String line = "Smell: {{3}=3?{SmellAdjective} }{Smell}";
      //System.out.println (script.resolve(line));
   }
}
