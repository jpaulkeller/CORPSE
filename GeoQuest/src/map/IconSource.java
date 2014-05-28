package map;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class IconSource
{
   private static final Pattern IMAGE_SUFFIX =
      Pattern.compile (".+[.]GIF$|.+[.]JPG$|.+[.]ICO$|.+[.]PNG$", Pattern.CASE_INSENSITIVE);
   private static final int MAX_BYTES = 4096;

   private DynamicPalette palette;
   private String searchTerm;
   
   public IconSource (final DynamicPalette palette, final String searchTerm)
   {
      this.palette = palette;
      this.searchTerm = searchTerm;
   }
   
   private PaletteTile addIcon (final String relativePath)
   {
      PaletteTile pi = new PaletteTile (palette, relativePath);
      palette.queueTile (pi);
      return pi;
   }
   
   int searchFiles (final String root)
   {
      List<File> files = new ArrayList<File>();
      findFiles (new File (root), files);
      
      Iterator<File> iter = files.iterator();
      while (iter.hasNext())
      {
         if (palette.getURLs().size() == DynamicPalette.MAX_ICONS)
            break;
         File file = iter.next();
         
         if (palette.getURLs().contains (file.getPath()))
         {
            iter.remove(); // don't count this one twice
            continue; // don't add duplicates
         }

         String relativePath = file.getPath().substring (root.length()); 
         PaletteTile pi = addIcon (relativePath);
         if (pi != null)
         {
            pi.put ("SOURCE", root);
            palette.getURLs().add (file.getPath());
         }
      }

      return files.size();
   }
   
   // Recursively search the given directory for files that satisfy the
   // criteria (that is, match the search pattern, have an image suffix,
   // and are smaller than MAX_BYTES).  
   
   private void findFiles (final File dir, final List<File> matches)
   {
      File[] dirEntries = dir.listFiles();
      if (dirEntries != null)
      {
         for (File file : dirEntries)
         {
            if (file.isDirectory())
               findFiles (file, matches);
            else if (file.isFile())
            {
               String path = file.getPath().toUpperCase();
               if (searchTerm != null && !path.contains (searchTerm))
                  continue;
               if (file.length() > MAX_BYTES)
                  continue;
               if (!IMAGE_SUFFIX.matcher (path).matches())
                  continue;
               matches.add (file);
            }
         }
      }
   }
}
