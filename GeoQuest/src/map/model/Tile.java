package map.model;

import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import map.MapMaker;
import utils.ImageTools;

public class Tile
{
   private String file;
   private Map<Integer, Image> images = new HashMap<Integer, Image>();
   
   public Tile (final String relativePath)
   {
      String fullPath = MapMaker.IMAGE_ROOT + File.separator + relativePath;
      if (!new File (fullPath).exists())
         System.err.println ("Missing tile image: " + fullPath);
      
      file = relativePath;
   }
   
   public String getFile()
   {
      return file;
   }

   public Image getImage(final int size)
   {
      Image image = images.get(size);
      if (image == null)
      {
         String fullPath = MapMaker.IMAGE_ROOT + File.separator + file;
         image = new ImageIcon (fullPath).getImage();
         if (image != null)
            image = ImageTools.scaleImage (image, size, size, Image.SCALE_SMOOTH, null);
         images.put(size,  image); // cache it
      }
      return image;
   }
   
   @Override
   public String toString()
   {
      return getFile();
   }

   @Override
   public int hashCode()
   {
      return file.hashCode();
   }

   @Override
   public boolean equals (final Object obj)
   {
      if (this == obj)
         return true;
      if (!(obj instanceof Tile))
         return false;
      return file.equals (((Tile) obj).file);
   }
}
