package utils;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import db.JtdsDriver;
import db.SQL;
import file.FileUtils;
import gui.ComponentTools;
import gui.comp.FileChooser;

/** 
 * Provides a means to store and retrieve persistent default configuration 
 * settings using a simple property (key = value) file.
 */
public class Options extends TreeMap<String, String>
{
   public static final String DBMS_HOST = "DBMSHost"; 
   public static final String DBMS_PORT = "DBMSPort"; 
   public static final String DATABASE  = "Database"; 
   
   private static final long serialVersionUID = 1L;
   
   private static final Pattern OPT_PATTERN = Pattern.compile ("([^=]+)=(.+)");

   private String optionsFile;
   
   public Options (final String dir)
   {
      String home = System.getProperty ("user.home");
      String path = home + "/Local Settings/Application Data/" + dir; 
      optionsFile = path + File.separator + "options.txt";
      
      // set default values
      put (DBMS_HOST, "ob03");
      put (DBMS_PORT, "1433");
   }
   
   public String getOptionsFile()
   {
      return optionsFile;
   }

   public void setOptionsFile (final String optionsFile)
   {
      this.optionsFile = optionsFile;
   }

   public void read()
   {
      BufferedReader br = null;
      try
      {
         FileInputStream fis = new FileInputStream (optionsFile);
         InputStreamReader isr = new InputStreamReader (fis);
         br = new BufferedReader (isr);

         String line = null;
         while ((line = br.readLine()) != null)
            parseOption (line);
      }
      catch (IOException x)
      {
         System.err.println (x);
      }
      finally
      {
         FileUtils.close (br);
      }
   }

   public String get (final String key, final String defaultValue)
   {
      String value = get (key);
      if (value == null)
         value = defaultValue;
      return value;
   }

   public int getInt (final String key, final int defaultValue)
   {
      String value = get (key);
      return value != null ? Integer.parseInt (value) : defaultValue; 
   }
   
   public File getFile (final String key)
   {
      String value = get (key);
      return value != null ? new File (value) : null; 
   }
   
   private void parseOption (final String line)
   {
      Matcher m = OPT_PATTERN.matcher (line);
      if (m.matches())
         put (m.group (1), m.group (2));
   }
   
   public void write()
   {
      PrintStream out = null;
      try
      {
         FileUtils.makeDir (new File (optionsFile).getParent());
         out = new PrintStream (optionsFile);
         write (out);
      }
      catch (IOException x)
      {
         System.err.println (x);
      }
      finally
      {
         FileUtils.close (out);
      }
   }
   
   public void write (final PrintStream out)
   {
      for (Map.Entry<String, String> entry : this.entrySet())
         out.println (entry.getKey() + "=" + entry.getValue());
      out.flush();
   }

   public void warn (final Component owner, final String title,
                     final String message, final Exception x)
   {
      System.err.println (message);
      if (x != null)
         System.err.println (x);
      if (title != null)
         JOptionPane.showMessageDialog (owner, message + "\n" + x, title,
                                        JOptionPane.ERROR_MESSAGE, null);
   }
   
   public SQL connect()
   {
      SQL sql = null;
      
      final String host = get (DBMS_HOST);
      final int port = getInt (DBMS_PORT, 1433);
      final String db = get (DATABASE);

      JtdsDriver dd = new JtdsDriver(); // supports Windows Authentication SSO
      final Connection conn = dd.connect (host, port + "", db);
      if (conn != null)
         sql = new SQL (db, conn);
      
      return sql;
   }
   
   public String getDatabase() // used in warnings
   {
      return get (DBMS_HOST) + ":" + get (DBMS_PORT) + " " + get (DATABASE);
   }
   
   public File selectFile (final Component owner, final String title, 
                           final String regex, final String description,
                           final String pathOption, final int mode)
   {
      File choice = null;
      
      FileChooser fc = new FileChooser (title, get (pathOption));
      fc.setFileSelectionMode (mode);
      fc.setRegexFilter (regex, description);
      // cf.setSelectedFile (new File (defaultName));
      
      if (fc.showOpenDialog (owner) == JFileChooser.APPROVE_OPTION)
      {
         choice = fc.getSelectedFile();
         put (pathOption, choice.isDirectory() ? choice.getPath() : choice.getParent());
         write();
      }
      return choice;
   }

   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      Options options = new Options ("Oberon/HR");
      options.read();
      System.out.println (options.optionsFile);
      options.write (System.out);
   }
}
