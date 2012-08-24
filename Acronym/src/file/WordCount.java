package file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.filechooser.FileFilter;

/**
 * Command line program to count lines, words and characters in files or from
 * standard input, similar to the unix wc utility.
 * 
 * Usage: java WordCount FILE1 FILE2 ... or java WordCount < FILENAME
 */
public final class WordCount
{
   private static long totalChars;
   private static long totalWords;
   private static long totalLines;
   
   private WordCount() { /* utility class */ }
   
   private static int count (final File fileOrDir, final FileFilter filter)
   {
      int total = 0; // for this directory
      if (fileOrDir.isDirectory())
      {
         for (File entry : fileOrDir.listFiles())
            total += count (entry, filter);
         if (total > 0)
            System.out.println ("TOTAL " + fileOrDir + ": " + total + "\n");
      }
      else if (fileOrDir.isFile() && filter.accept (fileOrDir))
         total = countFile (fileOrDir);
      return total;
   }

   private static int countFile (final File file)
   {
      int numLines = 0;
      BufferedReader in = null;
      try
      {
         FileReader fileReader = new FileReader (file);
         in = new BufferedReader (fileReader);
         numLines = count (file.getPath(), in);
      }
      catch (IOException x)
      {
         x.printStackTrace();
      }
      finally
      {
         FileUtils.close (in);
      }
      return numLines;
   }

   private static int count (final String streamName, final InputStream input)
   {
      int numLines = 0;
      try
      {
         InputStreamReader inputStreamReader = new InputStreamReader (input);
         BufferedReader in = new BufferedReader (inputStreamReader);
         numLines = count (streamName, in);
         in.close();
      }
      catch (IOException x)
      {
         x.printStackTrace();
      }
      return numLines;
   }

   private static int count (final String name, final BufferedReader in)
   throws IOException
   {
      int numLines = 0;
      long numWords = 0;
      long numChars = 0;
      String line;
      do
      {
         line = in.readLine();
         if (line != null)
         {
            numLines++;
            numChars += line.length();
            numWords += countWords (line);
         }
      }
      while (line != null);
      
      totalChars += numChars;
      totalWords += numWords;
      totalLines += numLines;

      System.out.println (name + "\t" + numLines + "\t" + numWords + "\t" + numChars);
      
      return numLines;
   }

   private static long countWords (final String line)
   {
      long numWords = 0;
      int index = 0;
      boolean prevWhitespace = true;
      while (index < line.length())
      {
         char c = line.charAt (index++);
         boolean currWhitespace = Character.isWhitespace (c);
         if (prevWhitespace && !currWhitespace)
            numWords++;
         prevWhitespace = currWhitespace;
      }
      return numWords;
   }

   public static void main (final String[] args)
   {
      if (args.length == 0)
         count ("stdin", System.in);
      else
      {
         FileFilter filter = new RegexFileFilter (".*[.]java", null); // TBD
         for (String arg : args)
            count (new File (arg), filter);
         System.out.println ("TOTAL: " + totalLines + " lines\t" + 
                             totalWords + " words\t" + totalChars + " chars");
      }
   }
}