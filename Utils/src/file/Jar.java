package file;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import web.HTMLUtils;
import gui.ClipboardHelper;
import gui.ComponentTools;
import gui.VFlowLayout;
import gui.form.FileItem;
import gui.form.TextAreaItem;
import gui.form.TextItem;

/**
 * Static convenience methods for searching JAR files. 
 * Also see: http://www.findjar.com */

public final class Jar
{
   private static final Pattern
      JAR_ENTRY = Pattern.compile ("(?:(?:jar:)?file:)?(.+)!/.+");

   private Jar()
   {
      // utility class; prevent instantiation
   }
   
   /**
    * Returns the path to the directory that contains the jar file
    * from which the given class was loaded.
    */
   public static String getJarPath (final Class<?> c)
   {
      String jarPath = null;
      
      URL jarURL = c.getResource (c.getSimpleName() + ".class");
      Matcher m = JAR_ENTRY.matcher (jarURL.getFile());
      if (m.matches())
      {
         jarPath = m.group (1); // strip prefix and suffix
         jarPath = HTMLUtils.decode (jarPath); // fix %20, etc
         jarPath = new File (jarPath).getParent(); // strip jar file
      }

      return jarPath;
   }

   /**
    * Return a list of all jar files in the given directory that contain
    * an entry matching the given regular expression.
    */
   public static SortedSet<File> findJarsContainingPattern (final File dir, final String regexp)
   {
      System.out.println("Searching for [" + regexp + "] in: " + dir);
      SortedSet<File> jars = new TreeSet<File>();
      findJarsContainingPattern (jars, dir, regexp);
      return jars;
   }
   
   public static void findJarsContainingPattern (final SortedSet<File> jars, 
                                                 final File dir, final String regexp)
   {
      Pattern pattern = Pattern.compile (regexp);
      try
      {
         File[] files = dir.listFiles();
         for (File jar : files)
         {
            if (jar.isFile() && jar.getName().toLowerCase().endsWith (".jar"))
            {
               // System.out.println (jar);
               List<ZipEntry> entries = Zip.entries (jar);
               for (ZipEntry entry : entries)
               {
                  if (pattern.matcher (entry.getName()).find())
                  {
                     jars.add (jar);
                     System.out.println (" > " + jar + ": " + entry.getName());
                  }
               }
            }
            else if (jar.isDirectory())
               findJarsContainingPattern (jars, jar, regexp); // recurse
         }
      }
      catch (IOException x)
      {
         System.err.println (x);
      }
   }
   
   /**
    * Return a list of all files (in the given directory) whose names match
    * the given regular expression.
    */
   public static SortedSet<File> findFilesMatchingPattern (final File dir, final String regexp)
   {
      System.out.println("Searching for [" + regexp + "] in: " + dir);
      SortedSet<File> matches = new TreeSet<File>();
      findFilesMatchingPattern (matches, dir, regexp);
      return matches;
   }
   
   public static void findFilesMatchingPattern (final SortedSet<File> matches, 
                                                final File dir, final String regexp)
   {
      Pattern pattern = Pattern.compile (regexp);
      File[] files = dir.listFiles();
      for (File file : files)
      {
         if (file.isFile())
         {
            if (pattern.matcher (file.getPath()).find())
            {
               matches.add (file);
               System.out.println (" > " + file);
            }
         }
         else if (file.isDirectory())
            findFilesMatchingPattern (matches, file, regexp); // recurse
      }
   }
   
   private static TextItem regexItem;
   private static FileItem dirItem;
   private static TextAreaItem outItem;
   
   public static void findJar()
   {
      ComponentTools.setDefaults();
      
      regexItem = new TextItem("Search Pattern (regex)", 50, ClipboardHelper.pasteString());
      dirItem = new FileItem("Root Directory Containing Libraries", null, 50, true);
      dirItem.setMode(JFileChooser.DIRECTORIES_ONLY);
      outItem = new TextAreaItem("Jars containing the entered pattern:", null, 8, 50);
      
      JButton searchJarButton = new JButton("Search For Jar");
      searchJarButton.addActionListener(new SearchJarButtonListener());
      JButton searchSrcButton = new JButton("Search For Source");
      searchSrcButton.addActionListener(new SearchSrcButtonListener());
      
      JPanel buttons = new JPanel();
      buttons.add (searchJarButton);
      buttons.add (searchSrcButton);
      
      JPanel input = new JPanel(new VFlowLayout());
      input.add (regexItem.getTitledPanel());
      input.add (dirItem.getTitledPanel());
      input.add (buttons);
      
      JPanel form = new JPanel(new BorderLayout());
      form.add(input, BorderLayout.NORTH);
      form.add(outItem.getTitledPanel(), BorderLayout.CENTER);
      
      ComponentTools.open(form, "Jar Finder");
   }
   
   static class SearchJarButtonListener implements ActionListener
   {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
         SortedSet<File> jars = new TreeSet<File>();
         
         String regex = ((String) regexItem.getValue()).trim();
         File dir = dirItem.getFile();
         if (regex != null && !regex.isEmpty() && dir != null && dir.exists())
            jars.addAll(findJarsContainingPattern (dir, regex));
         
         String jdk = System.getenv("JAVA_HOME");
         String jre = System.getenv("JRE_HOME");
         if (jdk != null)
            jars.addAll(findJarsContainingPattern (new File (jdk), regex));
         else if (jre != null)
            jars.addAll(findJarsContainingPattern (new File (jre), regex));
         
         StringBuilder sb = new StringBuilder();
         for (File jar : jars)
            sb.append (jar + "\n");
         outItem.setValue(sb.toString());
      }
   }
   
   static class SearchSrcButtonListener implements ActionListener
   {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
         SortedSet<File> files = new TreeSet<File>();
         
         String regex = ((String) regexItem.getValue()).trim();
         File dir = dirItem.getFile();
         if (regex != null && !regex.isEmpty() && dir != null && dir.exists())
            files.addAll(findFilesMatchingPattern (dir, regex));
         
         StringBuilder sb = new StringBuilder();
         for (File file : files)
            sb.append (file + "\n");
         outItem.setValue(sb.toString());
      }
   }
   
   public static void main (final String[] args)
   {
      if (args.length == 2)
         findJarsContainingPattern (new File (args[0]), args[1]);
      else
         findJar();
   }
}
