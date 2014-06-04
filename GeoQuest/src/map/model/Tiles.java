package map.model;

import java.io.File;
import java.util.HashMap;

import map.MapMaker;

public class Tiles extends HashMap<String, Tile>
{
   private static final long serialVersionUID = 1L;

   public static Tiles getInstance()
   {
      return new Tiles();
   }
   
   private Tiles()
   {
   }
   
   @Override
   public Tile get(final Object tileName)
   {
      Tile tile = null;
      if (tileName != null)
      {
         tile = super.get(tileName);
         if (tile == null)
         {
            String fullPath = MapMaker.IMAGE_ROOT + File.separator + tileName;
            if (!new File (fullPath).exists())
               System.err.println ("Missing tile image: " + fullPath); // TODO use default "missing" image?
            tile = new Tile (tileName.toString());
            put(tileName.toString(),  tile); // cache it
         }
      }
      return tile;
   }
}
