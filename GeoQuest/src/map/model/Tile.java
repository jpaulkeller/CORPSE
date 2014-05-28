package map.model;

import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;

import map.MapMaker;
import utils.ImageTools;

public class Tile
{
   private String file;
   private Image image;
   
   public Tile (final String relativePath)
   {
      String fullPath = MapMaker.IMAGE_ROOT + File.separator + relativePath;
      if (!new File (fullPath).exists())
         System.err.println ("Missing tile image: " + fullPath);
      
      file = relativePath;
      image = new ImageIcon (fullPath).getImage();
      if (image != null)
         image = ImageTools.scaleImage (image, 32, 32, Image.SCALE_SMOOTH, null);
   }
   
   public String getFile()
   {
      return file;
   }

   public Image getImage()
   {
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
