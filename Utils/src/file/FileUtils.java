package file;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FileUtils
{
   public static final String UTF8 = "UTF8";

   public static final String MY_DOCS = System.getProperty ("user.home") + "/My Documents";
   public static final String MY_DESK = System.getProperty ("user.home") + "/Desktop";
   
   // TBD: deal with quoted values
   // private static final Pattern FIRST_TOKEN = Pattern.compile ("([^,]*).*");
   // private static final Pattern NEXT_TOKEN = Pattern.compile (",([^,]*)");
   
   private FileUtils()
   {
      // utility class; prevent instantiation
   }
   
   public static String getSuffix (final File file)
   {
      String suffix = null;
      final int pos = file.getName().lastIndexOf (".");
      if (pos >= 0)
         suffix = file.getName().substring (pos + 1);
      return suffix;
   }
   
   /** Returns the file name without path or suffix. */
   
   public static String getNameWithoutSuffix (final File file)
   {
      String name = file.getName();
      final int pos = name.lastIndexOf (".");
      if (pos > 0)
         name = name.substring (0, pos);
      return name;
   }
   
   public static boolean renameSafe (final File from, final File to)
   {
      if (to.exists())
         return false;
      if (from.getPath().equalsIgnoreCase (to.getPath()))
      {
         String tmpPath = to.getParent() + File.separator + UUID.randomUUID();
         File tmp = new File (tmpPath);
         if (from.renameTo (tmp))
            return tmp.renameTo (to);
      }      
      return from.renameTo (to);
   }
   
   /**
    * static convenience method called to create directories
    *
    * NOTE: This class does not solve the file separator problem.
    *       So if you pass in "C:\Program Files" on a Solaris OS, it will fail
    *
    * @param  dirPath Absolute path (directory) to create
    * @return boolean - false, if directory could not be made
    *                   true, if directory made or already exists
    */
   public static boolean makeDir (final String dirPath)
   {
      if (dirPath != null && dirPath.length() > 0)
      {
         final File dir = new File (dirPath);
         return dir.exists() || dir.mkdirs();
      }
      return false;
   }

   public static void copyFile (final File in, final File out) throws IOException
   {
      FileInputStream fis = null;
      FileOutputStream fos = null;
      FileChannel inChannel = null;
      FileChannel outChannel = null;
      try
      {
         fis = new FileInputStream (in);
         fos = new FileOutputStream (out);
         inChannel = fis.getChannel();
         outChannel = fos.getChannel();
         inChannel.transferTo (0, inChannel.size(), outChannel);
      }
      finally
      {
         close (inChannel);
         close (outChannel);
         close (fis);
         close (fos);
      }
   }
   
   public static boolean writeFile (final File file, final CharSequence data, 
                                    final boolean append)
   {
      return writeFile (file, data, append, UTF8);
   }
   
   public static boolean writeFile (final File file, final CharSequence data, 
                                    final boolean append, final String encoding)
   {
      boolean written = false;
      OutputStreamWriter osw = null;
      try
      {
         OutputStream os = null;
         if (append)
            os = new FileOutputStream (file, append);
         else
            os = new FileOutputStream (file);
         osw = new OutputStreamWriter (os, encoding);
         osw.write (data.toString());
         written = true;
      }
      catch (IOException x)
      {
         System.err.println ("FileUtils.writeFile() " + x);
      }
      finally
      {
         close (osw);
      }
      
      return written;
   }
   
   public static String getText (final File file)
   {
      return getText (file.getPath(), UTF8);
   }

   public static String getText (final String path)
   {
      return getText (path, UTF8);
   }

   public static String getText (final String path, final String encoding)
   {
      String contents = null;
      
      InputStream is = null;
      try
      {
         if (path.startsWith ("http"))
            is = new URL (path).openStream();
         else if (new File (path).exists())
            is = new FileInputStream (path);
         if (is != null)
            contents = getText (is, encoding);
      }
      catch (IOException x)
      {
         System.out.println ("FileUtil.getContents(): " + path);
         x.printStackTrace (System.err);
      }
      finally
      {
         close (is);
      }
      
      return contents;
   }
   
   public static String getText (final InputStream is, final String encoding)
   throws IOException
   {
      StringBuilder buf = new StringBuilder();
      InputStreamReader isr = new InputStreamReader (is, encoding);
      BufferedReader br = new BufferedReader (isr);
      String line = null;
      while ((line = br.readLine()) != null)
         buf.append (line + "\n");
      return buf.toString();
   }
   
   public static List<String> getList (final String path,
                                       final String encoding,
                                       final boolean skipBlanks)
   {
      List<String> lines = new ArrayList<String>();
      
      InputStream is = null;
      try
      {
         if (path.startsWith ("http"))
            is = new URL (path).openStream();
         else if (new File (path).exists())
            is = new FileInputStream (path);
         if (is != null)
            lines = getList (is, encoding, skipBlanks);
      }
      catch (Exception x)
      {
         System.out.println ("FileUtil.getList(): " + path);
         x.printStackTrace (System.err);
      }
      finally
      {
         close (is);
      }
      
      return lines;
   }
   
   public static List<String> getList (final InputStream is, 
                                       final String encoding,
                                       final boolean skipBlanks)
   throws IOException
   {
      List<String> lines = new ArrayList<String>();
      
      InputStreamReader isr;
      if (encoding != null)
         isr = new InputStreamReader (is, encoding);
      else
         isr = new InputStreamReader (is);
      BufferedReader br = new BufferedReader (isr);
      String line = null;
      while ((line = br.readLine()) != null)
         if (!skipBlanks || !line.trim().equals (""))
            lines.add (line);
            
      return lines;
   }
   
   public static void writeList (final Collection<String> lines, final String path,
                                 final boolean append)
   {
      PrintStream out = null;
      try
      {
         FileUtils.makeDir (new File (path).getParent());
         FileOutputStream fos = new FileOutputStream (path, append); 
         out = new PrintStream (fos, true, UTF8); // support Unicode
         for (String line : lines)
            out.println (line);
         out.flush();
      }
      catch (IOException x)
      {
         x.printStackTrace (System.err);
      }
      finally
      {
         close (out);
      }
   }
   
   public static List<String> grep (final File file, final String regex)
   {
      List<String> matches = new ArrayList<String>();
      
      BufferedReader br = null;
      try
      {
         Pattern pattern =
            Pattern.compile (".*?" + regex + ".*?", Pattern.UNICODE_CASE);

         FileInputStream fis = new FileInputStream (file);
         InputStreamReader isr = new InputStreamReader (fis, UTF8);
         br = new BufferedReader (isr);

         String line = null;
         while ((line = br.readLine()) != null)
         {
            Matcher m = pattern.matcher (line.trim());
            if (m.matches())
               if (m.groupCount() == 1)
                  matches.add (m.group (1));
               else
                  matches.add (line);
         }
      }
      catch (IOException x)
      {
         System.err.println ("FileUtils.grep() " + x);
         x.printStackTrace (System.err);
      }
      finally
      {
         close (br);
      }

      return matches;
   }
   
   public static void close (final Closeable c)
   {
      try
      {
         if (c != null)
            c.close();
      }
      catch (IOException x)
      {
         x.printStackTrace (System.err);
      }
   }
   
   public static void main (final String[] args)
   {
      String fileName = "C:/Documents and Settings/jkeller/Desktop/utf-16.txt";
      FileUtils.writeFile (new File (fileName), 
                           "ABCDEFGHIJKLMNOPQRSTUVWXYZ", false, "UTF-16");
   }
}
