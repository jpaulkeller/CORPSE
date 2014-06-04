package map;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import model.CrossMap;

public class IconInUse implements Observer
{
   private DynamicPalette palette;
   private Map map;
   private String root;
   
   public IconInUse (final DynamicPalette palette, final Map map, final String root)
   {
      this.palette = palette;
      this.map = map;
      this.root = root;
      if (map != null) // for testing DynamicPalette
         map.addObserver(this);
   }
   
   private PaletteTile addIcon (final String relativePath)
   {
      PaletteTile pi = new PaletteTile (palette, relativePath);
      palette.queueTile (pi);
      return pi;
   }
   
   @Override
   public void update(Observable o, Object action)
   {
      if (action.equals(Map.MAP_LOADED))
         loadIcons();
      else if (action.toString().startsWith(Map.TILE_FIRST_USE))
         if (palette.getURLs().size() <= DynamicPalette.MAX_ICONS)
            addToPalette(action.toString().substring(Map.TILE_FIRST_USE.length()));
   }

   private int loadIcons()
   {
      CrossMap<String, Integer> tiles = map.getTiles();
      for (String path : tiles.keySet())
      {
         File file = new File(root + File.separator + path);
         if (file.exists())
            addToPalette(path);
         else
            System.err.println("Missing icon: " + file.getPath());
         
         if (palette.getURLs().size() == DynamicPalette.MAX_ICONS)
            break;
      }

      return tiles.size();
   }

   private void addToPalette(final String path)
   {
      PaletteTile pi = addIcon (path);
      if (pi != null)
      {
         pi.put ("SOURCE", root);
         palette.getURLs().add (path);
      }
   }
}
