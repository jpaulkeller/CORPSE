package file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Static convenience methods for creating and accessing zip files. */

// See java.sun.com/developer/technicalArticles/Programming/compression/
// and www.dogma.net/markn/articles/JavaZip/JavaZip.html

public final class Zip
{
   private static final Pattern ARGS = Pattern.compile ("-?[cxt]v?f?n?");
   
   private static final int BUF_SIZE = 1024;

   private File file;
   private ZipFile zf;                  // only used when reading
   private ZipOutputStream zos;         // only used when writing
   
   private Zip (final File file)
   {
      this.file = file;
   }

   /** Return a list of entries in the given zip file. */
   
   public static List<ZipEntry> entries (final File file)
      throws IOException
   {
      List<ZipEntry> entries = new ArrayList<ZipEntry>();
      
      if (file.isFile())
      {
         Zip zip = new Zip (file);
         zip.zf = new ZipFile (file);
         entries = zip.unzip ((String) null, "t");
         zip.zf.close();
      }
      
      return entries;
   }

   /**
    * Extracts all of the files in the given zip file into the given
    * directory (which is optional).  Returns the number of files
    * unzipped, or a negative number to indicate an error. */

   public static int unzip (final File file, final String dir) throws IOException
   {
      return unzip (file, dir, "xv");
   }

   public static int unzip (final File file, final String dir, final String operation)
      throws IOException
   {
      if (!file.isFile())
         return -1;
      
      Zip zip = new Zip (file);
      zip.zf = new ZipFile (file);
      int count = zip.zf.size();
      zip.unzip (dir, operation);
      zip.zf.close();
      return count;
   }

   private List<ZipEntry> unzip (final String dir, final String operation) 
      throws IOException
   {
      List<ZipEntry> list = new ArrayList<ZipEntry>();
      
      Enumeration<? extends ZipEntry> entries = zf.entries();
      while (entries.hasMoreElements())
      {
         ZipEntry entry = entries.nextElement();
         if (!entry.isDirectory())
         {
            list.add (entry);
            if (operation.contains ("v") || operation.contains ("x"))
               unzipEntry (entry, dir, operation);
         }
         // TBD: else mkdir?
      }
      
      return list;
   }
   
   /**
    * Extract the zip-file entry into the given directory with the given
    * operation string.<br>
    * "x" - extract contents (default off)<br>
    * "v" - verbose output (default off)<br>
    * "n" - never overwrite files (default off)
    */
   private void unzipEntry (final ZipEntry entry, 
                            final String dir,
                            final String operation)
      throws IOException
   {
      File f = new File (entry.getName());
      if (operation.contains ("v"))
         System.out.println (f.getPath());
         
      if (operation.contains ("x"))
      {
         String parent = f.getParent();
         if (!parent.toUpperCase().startsWith("META-INF")) // Ignore JAR META-INF
         {
            // create any necessary directories
            if (dir != null)
               parent = dir + File.separator + parent;
            FileUtils.makeDir (parent);
            
            String path = parent + File.separator + f.getName();
            if (!operation.contains("n") || !new File(path).exists())
               extractFile (zf.getInputStream (entry), path);
         }
         else
            System.out.println ("Ignoring extraction of META-INF path");
      }
   }

   private void extractFile (final InputStream is, final String path)
   throws IOException
   {
      FileOutputStream fos = new FileOutputStream (path);
      BufferedOutputStream bos = new BufferedOutputStream (fos);
      // TBD: encoding is probably required for database files
         
      BufferedInputStream bis = new BufferedInputStream (is, BUF_SIZE);
         
      int count;
      byte[] data = new byte [BUF_SIZE];
      while ((count = bis.read (data, 0, BUF_SIZE)) != -1)
         bos.write (data, 0, count);

      bis.close();
      bos.close();
   }

   /**
    * Compresses and archives the given list of files into a ZIP file,
    * optionally deleting the input files.
    */
   public static boolean zip (final Collection<String> inNames,
                              final String outName,
                              final boolean delete)
   {
      return zip (inNames, outName, "cv", delete);
   }
   
   public static boolean zip (final Collection<String> inNames, 
                              final String outName,
                              final String operation, 
                              final boolean delete)
   {
      Zip zip = new Zip (new File (outName));
      return zip.zip (inNames, delete, operation);
   }

   private boolean zip (final Collection<String> inNames,
                        final boolean delete, final String operation)
   {
      try
      {
         FileOutputStream fos = new FileOutputStream (file);
         BufferedOutputStream bos = new BufferedOutputStream (fos);
         this.zos = new ZipOutputStream (bos);

         for (String fileName : inNames)
            zipFile (new File (fileName), delete, operation);

         zos.flush();
         zos.close();
         return true;
      }
      catch (IOException x)
      {
         System.err.println ("Could not zip: " + x.getMessage());
         return false;
      }
   }

   private void zipFile (final File f, final boolean delete, final String operation)
   throws IOException
   {
      if (f.getCanonicalFile().equals (file.getCanonicalFile()))
         return;                // don't try to zip yourself!

      if (f.isDirectory())
         for (File dirEntry : f.listFiles())
            zipFile (dirEntry, delete, operation); // recurse
      else
      {
         if (operation.contains ("v"))
            System.out.println (f.getPath());
         
         if (operation.contains ("c"))
         {
            insertFile (f);
            if (delete)
               if (!f.delete())
                  System.err.println ("Zip.zipFile() failed to delete: " + f);
         }
      }
   }

   private void insertFile (final File f) throws IOException
   {
      FileInputStream fis = new FileInputStream (f);
      BufferedInputStream bis = new BufferedInputStream (fis, BUF_SIZE);

      ZipEntry entry = new ZipEntry (f.getPath());
      zos.putNextEntry (entry); // add entry to ZIP file

      int count;
      byte[] data = new byte [BUF_SIZE];
      while ((count = bis.read (data, 0, BUF_SIZE)) != -1)
         zos.write (data, 0, count);

      bis.close();
   }
   
   public static void main (final String[] args)
   {
      if (args.length > 1)
      {
         Matcher m = ARGS.matcher (args[0]);
         if (m.matches())
         {
            try
            {
               String operation = args[0];
               String zipFile = args[1];
               boolean delete = false; // TBD

               if (operation.contains ("c"))
               {
                  Collection<String> files = new ArrayList<String>();
                  for (int i = 2, n = args.length; i < n; i++)
                     files.add (args[i]);
                  if (Zip.zip (files, zipFile, operation, delete))
                     System.out.println ("\n" + zipFile + " created.");
               }
               else
               {
                  String dir = args.length > 2 ? args[2] : null;
                  int count = Zip.unzip (new File (zipFile), dir, operation);
                  System.out.println ("\nEntries in " + zipFile + ": " + count);
               }
            }
            catch (IOException x)
            {
               System.err.println (x);
            }
            System.exit (0);
         }
      }

      // TBD:
      // Usage: jar {ctxu}[vfm0Mi] [jar-file] [manifest-file] [-C dir] files ...
      // - support deleting inserted files
      // - support extracting specified files

      System.out.println ("\nUsage: java " + Zip.class.getName() +
                          " {ctx}[vfn] zipfile [files-to-zip... or target-dir]");
   }
}
