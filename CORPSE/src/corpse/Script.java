package corpse;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import file.FileUtils;

public final class Script
{
   // the master list of scripts (names must be unique)
   public static final SortedMap<String, Script> SCRIPTS = new TreeMap<String, Script>();

   private final static Pattern IGNORE_HTML = Pattern.compile("html>|body>", Pattern.CASE_INSENSITIVE);
   
   private static final String SCRIPT_COMMAND = "!";
   private static final Pattern LOOP_BEGIN =
      Pattern.compile("^" + SCRIPT_COMMAND + "\\s*loop\\s*(.+)", Pattern.CASE_INSENSITIVE);
   private static final Pattern LOOP_END = 
      Pattern.compile("^" + SCRIPT_COMMAND + "\\s*end", Pattern.CASE_INSENSITIVE);
         
   private static boolean promptsEnabled = true; // must match Menus value

   private String name;
   private File file;
   private Map<String, String> variables = new HashMap<String, String>();

   public static void populate(final File dir)
   {
      for (File f : dir.listFiles())
      {
         if (f.isDirectory() && !f.getName().startsWith("."))
            populate(f);
         else if (f.isFile() && f.getName().toLowerCase().endsWith("." + Constants.SCRIPT_SUFFIX))
            new Script(f.getPath());
      }
   }

   public static Script getScript(final String name)
   {
      Script script = SCRIPTS.get(name.toUpperCase());
      if (script == null)
         System.err.println("Script not yet loaded: " + name);
      return script;
   }

   private Script(final String path)
   {
      file = new File(path);
      name = FileUtils.getNameWithoutSuffix(file).toUpperCase();
      Script script = SCRIPTS.get(name);
      if (script == null)
         SCRIPTS.put(name, this);
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

   public static void togglePrompts()
   {
      promptsEnabled = !promptsEnabled;
   }
   
   public String resolve()
   {
      return resolve(false);
   }
   
   public String resolve(final boolean nested) // TODO: thread
   {
      StringBuilder buf = new StringBuilder();

      BufferedReader br = null;
      try
      {
         FileInputStream fis = new FileInputStream(file);
         InputStreamReader isr = new InputStreamReader(fis);
         br = new BufferedReader(isr);

         String line = null;
         while ((line = br.readLine()) != null && !line.startsWith(Constants.EOF))
         {
            if (nested && IGNORE_HTML.matcher(line).find())
               continue;
            if (line.startsWith(SCRIPT_COMMAND))
               parseScriptCommand(br, resolve(line), buf);
            else
               processLine(line, buf);
         }
      }
      catch (IOException x)
      {
         buf.append(x.getMessage());
         x.printStackTrace(System.err);
      }
      finally
      {
         FileUtils.close(br);
      }

      return buf.toString();
   }

   private void parseScriptCommand(final BufferedReader br, final String command, final StringBuilder buf)
      throws IOException
   {
      Matcher m = LOOP_BEGIN.matcher(command);
      if (m.matches())
      {
         String line;
         int count = Macros.resolveNumber(m.group(1));
         if (count > 0)
         {
            List<String> lines = new ArrayList<String>();
            while ((line = br.readLine()) != null && !LOOP_END.matcher(line).find())
               lines.add(line);
            
            for (int i = 0; i < count; i++)
               for (String loopLine : lines)
                  processLine(loopLine, buf);
         }
         else
            while ((line = br.readLine()) != null && !LOOP_END.matcher(line).find())
               ; // skip to the end of the loop
      }
   }
   
   private void processLine(final String line, final StringBuilder buf)
   {
      if (line.equals("")) // ignore blank lines
         return;
      if (Constants.COMMENT_LINE.matcher(line).find())
         return;

      String resolved = resolve(line);
      if (resolved == null) // user cancelled
         return;

      Matcher m;
      if ((m = Constants.INCLUDE_LINE.matcher(resolved)).matches())
         buf.append(include(m.group(1)));
      else if ((m = Constants.RANDOMIZE_LINE.matcher(resolved)).matches())
         randomize(m.group(1));
      else if (!resolved.trim().isEmpty())
         buf.append(resolved + "\n");
   }
   
   public String resolve(final String entry)
   {
      String line = entry;

      Matcher m;
      while ((m = Constants.TOKEN.matcher(line)).find()) // loop for multiple tokens
      {
         String token = m.group();
         String resolvedToken;
         resolvedToken = resolveVariables(token);
         if (resolvedToken.equals(token))
         {
            resolvedToken = resolveAssignments(token);
            // System.out.println("Script assignment Token: " + token + " R: " + resolvedToken);
         }

         if (resolvedToken.equals(token))
         {
            resolvedToken = resolveQueries(token);
            if (resolvedToken == null) // user cancelled
               return null;
         }

         if (resolvedToken.equals(token))
         {
            resolvedToken = Macros.resolve(getName(), token, null);
            // System.out.println("Script macro Token: " + token + " R: " + resolvedToken);
         }

         if (resolvedToken.equals(token))
            line = m.replaceFirst("<$1>"); // avoid infinite loop
         else
            line = m.replaceFirst(Matcher.quoteReplacement(resolvedToken));
         // System.out.println("Line = " + line);
      }

      return line;
   }

   private String resolveVariables(final String entry)
   {
      String resolvedEntry = entry;
      for (String variable : variables.keySet())
      {
         String pattern = Pattern.quote("{" + variable + "}");
         String value = Matcher.quoteReplacement(variables.get(variable));
         resolvedEntry = resolvedEntry.replaceAll(pattern, value);
      }
      if (Macros.DEBUG && !entry.equals(resolvedEntry))
         System.out.println("resolveVariables: [" + entry + "] = [" + resolvedEntry + "]");
      return resolvedEntry;
   }

   private String resolveAssignments(final String entry)
   {
      String resolvedEntry = entry;
      Matcher m;
      while ((m = Constants.ASSIGNMENT.matcher(resolvedEntry)).find())
      {
         variables.put(m.group(1), m.group(2));
         resolvedEntry = m.replaceFirst(Matcher.quoteReplacement(m.group(2)));
      }
      if (Macros.DEBUG && !entry.equals(resolvedEntry))
         System.out.println("resolveAssignments: [" + entry + "] = [" + resolvedEntry + "]");
      return resolvedEntry;
   }

   private static final String F = Constants.FILTER_CHAR; 
   private static final String NOT_F = "[^" + F + "]+";
   private static final Pattern FILTER = Pattern.compile("(" + F + NOT_F + ")\\?(" + NOT_F + F + ")");
   
   private String resolveQueries(final String entry)
   {
      String resolved = entry;

      Component owner = null; // TODO
      Icon icon = null;
      int type = JOptionPane.QUESTION_MESSAGE;
      Object[] options = null;

      // hack to allow question-mark in regex filters
      Matcher fm = FILTER.matcher(resolved);
      while (fm.find())
         resolved = fm.replaceAll("$1!!$2"); // replace "?" with "!!"
            
      Matcher m;
      while ((m = Constants.QUERY.matcher(resolved)).find())
      {
         String title = m.group(1);
         Object message = m.group(1);
         String defaultValue = m.group(2);
         
         // quick-query syntax: Token?? => Token?{Token}
         if (defaultValue != null && defaultValue.equals("?"))
            defaultValue = Macros.resolve(getName(), "{" + message + "}", null);
         else if (defaultValue != null && defaultValue.equals("*")) // offer multiple choices TODO
         {
            String[] randomOptions = new String[7];
            for (int i = 0; i < randomOptions.length; i++)
               randomOptions[i] = Macros.resolve(getName(), "{" + message + "}", null);
            // import gui.comp.TipComboBox;
            // TipComboBox box = new TipComboBox(randomOptions);
            // box.setEditable(true);
            options =  randomOptions;
            defaultValue = randomOptions[0];
         }
         
         String answer = defaultValue;
         if (promptsEnabled)
            answer = (String) JOptionPane.showInputDialog(owner, message, title, type, icon, options, defaultValue);
         if (answer != null)
            resolved = m.replaceFirst(Matcher.quoteReplacement(answer));
         else // user cancelled
            return null;
      }

      resolved = resolved.replace("!!", "?"); // restore any regex question-marks
      if (Macros.DEBUG && !entry.equals(resolved))
         System.out.println("resolveQueries: [" + entry + "] = [" + resolved + "]");
      return resolved;
   }

   private String include(final String scriptName)
   {
      // Script script = new Script(Constants.DATA_PATH + File.separator + scriptName); // TODO
      Script script = Script.getScript(scriptName); // TODO
      return script.resolve(true);
   }

   private void randomize(final String seed)
   {
      if (seed != null && !seed.isEmpty())
         RandomEntry.setSeed(seed.hashCode());
      else
         RandomEntry.randomize();
   }

   public List<String> search(final String pattern)
   {
      List<String> matches = new ArrayList<String>();

      String line = null;
      BufferedReader br = null;
      try
      {
         FileInputStream fis = new FileInputStream(file);
         InputStreamReader isr = new InputStreamReader(fis, Table.ENCODING);
         br = new BufferedReader(isr);

         while ((line = br.readLine()) != null)
            if (line.toUpperCase().contains(pattern))
               matches.add(line);
      }
      catch (Exception x)
      {
         System.err.println("File: " + file);
         System.err.println("Line: " + line);
         x.printStackTrace(System.err);
      }
      finally
      {
         FileUtils.close(br);
      }

      return matches;
   }

   public static void main(final String[] args)
   {
      CORPSE.init(true);

      // Script script = Script.getScript ("Treasure");
      // Script script = Script.getScript ("NPC");
      Script script = Script.getScript("Potion");
      String resolved = script.resolve();
      System.out.println("-------------------------------------------------------------");
      if (resolved != null)
         System.out.println(resolved);
   }
}
