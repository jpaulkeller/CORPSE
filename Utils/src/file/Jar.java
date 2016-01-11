package file;
 
import gui.ClipboardHelper;
import gui.ComponentTools;
import gui.VFlowLayout;
import gui.comp.ProgressBar;
import gui.form.FileItem;
import gui.form.TextAreaItem;
import gui.form.TextItem;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
 
/**
 * Static convenience methods for searching JAR files.
 * Also see: http://www.findjar.com */
 
public final class Jar
{
   private static final Pattern JAR_ENTRY = Pattern.compile ("(?:(?:jar:)?file:)?(.+)!/.+");
 
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
   public static SortedSet<File> findJarsContainingPattern (final String regexp, final File dir1, final File dir2)
   {
      System.out.println("Searching for [" + regexp + "] in: " + dir1);
      SortedSet<File> jars = new TreeSet<File>();
      findJarsContainingPattern (jars, dir1, regexp);
      if (dir2 != null && dir2.isDirectory())
         findJarsContainingPattern (jars, dir2, regexp);
      return jars;
   }
  
   public static void findJarsContainingPattern (final SortedSet<File> jars, final File dir, final String regexp)
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
   private static FileItem dirItem1;
   private static FileItem dirItem2;
   private static TextAreaItem outItem;
   private static ProgressBar progress;
  
   public static void findJar()
   {
      ComponentTools.setDefaults();
     
      regexItem = new TextItem("Search Pattern (regex)", 50, ClipboardHelper.pasteString());
     
      dirItem1 = new FileItem("Root Directory Containing Libraries", null, 50, true);
      dirItem1.setInitialValue(new File("C:/pkgs/workspace/PPB/Applications/MDMApplication"));
      dirItem1.setMode(JFileChooser.DIRECTORIES_ONLY);
      dirItem2 = new FileItem("Root Directory Containing Libraries (optional)", null, 50, true);
      dirItem2.setInitialValue(new File("C:/Oracle/Middleware"));
      dirItem2.setMode(JFileChooser.DIRECTORIES_ONLY);
     
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
      input.add (dirItem1.getTitledPanel());
      input.add (dirItem2.getTitledPanel());
      input.add (buttons);
     
      progress = new ProgressBar();
     
      JPanel form = new JPanel(new BorderLayout());
      form.add(input, BorderLayout.NORTH);
      form.add(outItem.getTitledPanel(), BorderLayout.CENTER);
      form.add(progress, BorderLayout.SOUTH);
      form.setPreferredSize(new Dimension(1100, 400));
     
      ComponentTools.open(form, "Jar Finder");
   }
  
   static class SearchJarButtonListener implements ActionListener
   {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
         new Thread()
         {
            @Override
            public void run()
            {
               progress.setIndeterminate(true);
              
               SortedSet<File> jars = new TreeSet<File>();
              
               String regex = ((String) regexItem.getValue()).trim();
               File dir1 = dirItem1.getFile();
               File dir2 = dirItem2.getFile();
               if (regex != null && !regex.isEmpty() && dir1 != null && dir1.exists())
                  jars.addAll(findJarsContainingPattern (regex, dir1, dir2));
              
               String jdk = System.getenv("JAVA_HOME");
               String jre = System.getenv("JRE_HOME");
               if (jdk != null)
                  jars.addAll(findJarsContainingPattern (regex, new File (jdk), null));
               else if (jre != null)
                  jars.addAll(findJarsContainingPattern (regex, new File (jre), null));
              
               StringBuilder sb = new StringBuilder();
               for (File jar : jars)
                  sb.append (jar + "\n");
               outItem.setValue(sb.toString());
              
               progress.setIndeterminate(false);
            }
         }.start();
      }
   }
  
   static class SearchSrcButtonListener implements ActionListener
   {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
         new Thread()
         {
            @Override
            public void run()
            {
               progress.setIndeterminate(true);
              
               SortedSet<File> files = new TreeSet<File>();
              
               String regex = ((String) regexItem.getValue()).trim();
               File dir = dirItem1.getFile();
               if (regex != null && !regex.isEmpty() && dir != null && dir.exists())
                  files.addAll(findFilesMatchingPattern (dir, regex));
              
               StringBuilder sb = new StringBuilder();
               for (File file : files)
                  sb.append (file + "\n");
               outItem.setValue(sb.toString());
              
               progress.setIndeterminate(false);
            }
         }.start();
      }
   }
  
   public static void main (final String[] args)
   {
      if (args.length == 2)
         findJarsContainingPattern (args[1], new File (args[0]), null);
      else
         findJar();
   }
}
