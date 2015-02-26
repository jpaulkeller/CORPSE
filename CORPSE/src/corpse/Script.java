package corpse;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

   // simple repeat: !5x ...
   private static final Pattern REPEAT = 
      Pattern.compile("^" + SCRIPT_COMMAND + "([0-9]+)x +(.+)$", Pattern.CASE_INSENSITIVE);
   
   // !loop #, !loop end
   private static final Pattern LOOP =
      Pattern.compile("^" + SCRIPT_COMMAND + "\\s*loop\\s*(.+)$", Pattern.CASE_INSENSITIVE);
   
   // !switch VALUE, !switch CASE, !switch end
   private static final Pattern SWITCH = 
      Pattern.compile("^" + SCRIPT_COMMAND + "\\s*switch\\s+(.+)$", Pattern.CASE_INSENSITIVE);
         
   private static boolean promptsEnabled = true; // must match Menus value

   private String name;
   private File file;
   private List<String> lines = new ArrayList<>();
   private int loopDepth;
   private boolean inSwitch, inSwitchCase = false;
   private String switchValue;
   private StringBuilder buf = new StringBuilder();

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
   
   public String resolve() // TODO: thread
   {
      buf.setLength(0);
      lines = loadScript(false);
      for (int i = 0, count = lines.size(); i < count; i++)
         i = processLine(i);
      return buf.toString();
   }
   
   private List<String> loadScript(final boolean nested)
   {
      List<String> lines = new ArrayList<String>();
      
      InputStream is = null;
      try
      {
         is = new FileInputStream (file.getPath());
         
         InputStreamReader isr = new InputStreamReader (is, "UTF8");
         BufferedReader br = new BufferedReader (isr);
         Matcher m;
         String line = null;
         while ((line = br.readLine()) != null)
         {
            if (nested && IGNORE_HTML.matcher(line).find())
               ; // ignore
            else if ((m = Constants.INCLUDE_LINE.matcher(line)).matches())
            {
               Script nestedScript = Script.getScript(m.group(1));
               if (nestedScript != null)
                  lines.addAll(nestedScript.loadScript(true));
            }
            else
               lines.add (line);
         }
      }
      catch (Exception x)
      {
         x.printStackTrace (System.err);
      }
      finally
      {
         FileUtils.close (is);
      }
      
      return lines;
   }

   private int processLine(final int index)
   {
      String line = lines.get(index);
      Matcher m;
      
      if ((m = SWITCH.matcher(line)).matches())
         processSwitch(m.group(1));
      else if (inSwitch && !inSwitchCase)
         ; // ignore other switch cases
      else if (line.startsWith(SCRIPT_COMMAND))
         return processScriptCommand(index);
      else
         resolveLine(line, buf);
      return index;
   }

   private int processScriptCommand(final int index)
   {
      String line = lines.get(index);
      Matcher m = REPEAT.matcher(line);
      if (m.matches())
         processRepeat(Macros.resolveNumber(resolve(m.group(1))), m.group(2));
      else if ((m = LOOP.matcher(line)).matches())
         return processLoop(index, m.group(1));
      else
         System.err.println("Unrecognized script command: " + line);
      return index;
   }

   private void processSwitch(final String token)
   {
      if (!inSwitch)
      {
         inSwitch = true;
         switchValue = resolve(token);
      }
      else if (token.equalsIgnoreCase("end"))
      {
         inSwitch = false;
         inSwitchCase = false;
      }
      else if (token.equalsIgnoreCase(switchValue))
         inSwitchCase = true;
      else
         inSwitchCase = false;
   }

   private void processRepeat(final int count, final String line)
   {
      for (int i = 0; i < count; i++)
         buf.append(resolve(line));
   }
   
   private int processLoop(final int loopLine, final String token)
   {
      if (token.equalsIgnoreCase("end"))
      {
         loopDepth--;
         return loopLine;
      }

      int loopCount = Macros.resolveNumber(token);
      int startingDepth = loopDepth;
      int index = loopLine + 1;
      for (int loop = 0; loop < loopCount; loop++)
      {
         loopDepth++;
         index = loopLine + 1;
         while (loopDepth > startingDepth)
            index = processLine(index) + 1;
      }

      return index - 1;
   }

   private void resolveLine(final String line, final StringBuilder buf)
   {
      if (line.equals("")) // ignore blank lines
         return;
      if (Constants.COMMENT_LINE.matcher(line).find())
         return;

      String resolved = resolve(line);
      if (resolved == null) // user cancelled
         return;
      // System.out.println("R: " + resolved);

      Matcher m;
      if ((m = Constants.RANDOMIZE_LINE.matcher(resolved)).matches())
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
         String resolvedToken = token;
         
         if (resolvedToken.equals(token))
         {
            resolvedToken = resolveQueries(token);
            if (resolvedToken == null) // user cancelled
               return null;
         }

         if (resolvedToken.equals(token))
            resolvedToken = Macros.resolve(getName(), token, null);

         if (resolvedToken.equals(token))
            line = m.replaceFirst("<$1>"); // avoid infinite loop
         else
            line = m.replaceFirst(Matcher.quoteReplacement(resolvedToken));
      }

      return line;
   }

   private static final String F = Constants.FILTER_CHAR; 
   private static final String NOT_F = "[^" + F + "]+";
   private static final Pattern FILTER = Pattern.compile("(" + F + NOT_F + ")\\?(" + NOT_F + F + ")");
   
   private String resolveQueries(final String entry)
   {
      String resolved = entry;

      Component owner = null;
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
         String title = "Script: " + getName();
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
