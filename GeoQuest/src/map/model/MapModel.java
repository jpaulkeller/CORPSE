
package map.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import map.MapProps;
import map.Scale;
import model.CrossMap;
import model.MatrixPair;
import file.FileUtils;

public class MapModel extends Observable
{
   public static final String BACKUP_0 = "data/backups/original.map";
   public static final String BACKUP_1 = "data/backups/recent.map";
   
   private int rowCount;
   private int colCount;
   private int gridSize = Scale.CELL_SIZE; // default
   
   private MapProps props = new MapProps();
   private List<Layer> layers = new ArrayList<Layer>();
   private Lines paths = new Lines();
   private Lines streams = new Lines();
   
   private File mapFile;
   private boolean needsSave;
   
   private Tile drawTile; // the currently active tile to draw?
   private int drawLayer;
   
   // maps tile files to a unique index
   private CrossMap<String, Integer> tiles = new CrossMap<String, Integer>();
   
   public MapModel (final int rows, final int cols)
   {
      create (rows, cols);
   }
   
   public void create (final int rows, final int cols)
   {
      clear();
      setSize (rows, cols);
      
      while (layers.size() < 2)
         layers.add (new Layer (rows, cols, layers.size()));
      fireChange ("Map cleared");
   }
   
   private void setSize (final int rows, final int cols)
   {
      this.rowCount = rows;
      this.colCount = cols;
      
      props.put ("Rows", rows + "");
      props.put ("Columns", cols + "");
   }

   private void clear()
   {
      props.clear();
      layers.clear();
      paths.clear();
      streams.clear();
      tiles.clear();
      
      mapFile = null;
      needsSave = false;
   }
   
   protected void fireChange (final String action)
   {
      setChanged();
      notifyObservers (action);
      clearChanged();
   }
   
   public File getFile()
   {
      return mapFile;
   }
   
   public MapProps getProps()
   {
      return props;
   }
   
   public CrossMap<String, Integer> getTiles()
   {
      return tiles;
   }
   
   public int getRowCount()
   {
      return rowCount;
   }
   
   public int getColumnCount()
   {
      return colCount;
   }
   
   public int getGridSize()
   {
      return gridSize;
   }
   
   public void setGridSize (final int pixels)
   {
      if (gridSize != pixels)
      {
         this.gridSize = pixels;
         // props.put ("GridType", ); TBD
         props.put ("GridSize", gridSize + "");
         fireChange ("Grid size changed to: " + pixels);
      }
   }
   
   public boolean isSquare()
   {
      return rowCount == colCount;
   }
   
   public void resize (final int newRows, final int newCols, final Scale scale)
   {
      for (Layer layer : layers)
         layer.resize (newRows, newCols);
      
      setSize (newRows, newCols);

      int pix = scale.getCellSize();
      Iterator<Line> iter = paths.iterator();
      while (iter.hasNext())
         if (!iter.next().clip (colCount * pix, rowCount * pix))
            iter.remove();
      
      iter = streams.iterator();
      while (iter.hasNext())
         if (!iter.next().clip (colCount * pix, rowCount * pix))
            iter.remove();
      
      needsSave = true;
      fireChange ("Map resized: " + newRows + " x " + newCols);
   }
   
   public void setDrawTile (final Tile tile, final int layer)
   {
      this.drawTile = tile;
      this.drawLayer = layer;
   }
   
   public Tile getDrawTile()
   {
      return drawTile;
   }
   
   public Layer getDrawLayer()
   {
      return layers.get (drawLayer);
   }
   
   public List<Layer> getLayers()
   {
      return layers;
   }
   
   public Lines getPaths()
   {
      return paths;
   }
   
   public Lines getStreams()
   {
      return streams;
   }

   public void drawTile (final Cell cell)
   {
      drawTile (cell.getRow(), cell.getCol());
   }
   
   public void drawTile (final int row, final int col)
   {
      setTile (layers.get (drawLayer), row, col, drawTile);
   }
   
   public void fillTiles (final Cell cell)
   {
      if (drawTile != null && tiles.get (drawTile.getFile()) == null)
         tiles.put (drawTile.getFile(), tiles.size() + 1);
      layers.get (drawLayer).fillTiles (cell, drawTile);
   }
   
   public void setTile (final Layer layer, final Cell cell, final Tile tile)
   {
      setTile (layer, cell.getRow(), cell.getCol(), tile);
   }
   
   public void setTile (final Layer layer, final int row, final int col, final Tile tile)
   {
      if (tile != null && tiles.get (tile.getFile()) == null) // first time for this tile
      {
         tiles.put (tile.getFile(), tiles.size() + 1);
         if (layer.getDepth() == 1) // feature
            fireChange("tile: " + tile.getFile());
      }
      layer.setValue (row, col, tile);
      needsSave = true;
   }
   
   public void roll (final int rowDelta, final int colDelta, 
                     final String direction, final Scale scale)
   {
      for (Layer layer : layers)
         layer.shift (rowDelta, colDelta);
      for (Line line : paths)
         line.shift (rowDelta, colDelta, scale);
      for (Line line : streams)
         line.shift (rowDelta, colDelta, scale);
      
      needsSave = true;
      fireChange ("Map shifted " + direction);
   }
   
   public void flipLeftRight (final int width)
   {
      for (Layer layer : layers)
         layer.flipLeftRight();
      for (Line line : paths)
         line.flipLeftRight (width);
      for (Line line : streams)
         line.flipLeftRight (width);
      
      needsSave = true;
      fireChange ("Map flipped (left to right)");
   }
   
   public void flipTopBottom (final int height)
   {
      for (Layer layer : layers)
         layer.flipTopBottom();
      for (Line line : paths)
         line.flipTopBottom (height);
      for (Line line : streams)
         line.flipTopBottom (height);
      
      needsSave = true;
      fireChange ("Map flipped (top to bottom)");
   }
   
   public void rotateCW (final int size)
   {
      for (Layer layer : layers)
         layer.rotateCW();
      for (Line line : paths)
         line.rotateCW (size);
      for (Line line : streams)
         line.rotateCW (size);
      
      needsSave = true;
      fireChange ("Map rotated clockwise");
   }
   
   public void addLine (final Line line)
   {
      if (line.getType().equals ("S"))
         addStream (line);
      else if (line.getType().equals ("P"))
         addPath (line);
      needsSave = true;
   }
   
   private void addStream (final Line stream)
   {
      stream.offset (20); // based on tile size of 32
      streams.add (stream);
   }
   
   private void addPath (final Line path)
   {
      path.offset (10); // based on tile size of 32
      paths.add (path);
   }
   
   public void clearStreams()
   {
      if (!streams.isEmpty())
      {
         streams.clear();
         needsSave = true;
      }
   }
   
   public void clearPaths()
   {
      if (!paths.isEmpty())
      {
         paths.clear();
         needsSave = true;
      }
   }
   
   public boolean needsSave()
   {
      return needsSave;
   }

   private static final Pattern PROPERTY = Pattern.compile ("([A-Za-z ]+)=(.*)");
   private static final Pattern TILE_PROP = Pattern.compile ("([0-9]+)=(.*)");
   private static final Pattern TILE_RUN = Pattern.compile ("([0-9]+)x([0-9]+)");
   private static final Pattern SEGMENT =
      Pattern.compile ("[SP]: ([0-9]+),([0-9]+) ([0-9]+),([0-9]+)");
   
   public void load (final File file)
   {
      clear();
      
      this.mapFile = file;
      
      List<String> lines = FileUtils.getList (file.getPath(), FileUtils.UTF8, false);
      loadProperties (lines);
      loadTiles (lines);
      loadLayers (lines);
      loadLines (lines);
      fireChange ("load");
   }

   private void loadProperties (final List<String> lines)
   {
      for (String line : lines)
      {
         Matcher m = PROPERTY.matcher (line);
         if (m.matches())
            props.put (m.group (1), m.group (2));
      }
      rowCount = Integer.parseInt (props.get ("Rows"));
      colCount = Integer.parseInt (props.get ("Columns"));
      if (props.get ("GridSize") != null)
         gridSize = Integer.parseInt (props.get ("GridSize"));
   }

   private void loadTiles (final List<String> lines)
   {
      for (String line : lines)
      {
         Matcher m = TILE_PROP.matcher (line);
         if (m.matches())
            tiles.put (m.group (2), Integer.parseInt (m.group (1)));
      }
   }

   private void loadLayers (final List<String> lines)
   {
      List<MatrixPair<Tile>> pairs = new ArrayList<MatrixPair<Tile>>();
      int total = 0;
      int size = rowCount * colCount;
      
      for (String line : lines)
      {
         Matcher m = TILE_RUN.matcher (line);
         if (m.matches())
         {
            int count = Integer.parseInt (m.group (1));
            int index = Integer.parseInt (m.group (2));
            String tileName = tiles.getKey (index);
            Tile tile = tileName != null ? new Tile (tileName) : null;
            pairs.add (new MatrixPair<Tile> (count, tile));
            total += count;
            if (total == size)
            {
               Layer layer = new Layer (rowCount, colCount, layers.size());
               layers.add (layer);
               layer.setDataCompressed (pairs);
               pairs.clear();
               total = 0;
            }
         }
      }

      while (layers.size() < 2)
         layers.add (new Layer (rowCount, colCount, layers.size()));
   }

   private void loadLines (final List<String> lines)
   {
      for (String line : lines)
      {
         Matcher m = SEGMENT.matcher (line);
         if (m.matches())
         {
            Line segment = Line.getLine (line);
            if ("S".equals (segment.getType()))
               addStream (segment);
            else if (line != null)
               addPath (segment);
         }
      }
   }

   public void save()
   {
      save (mapFile);
   }

   public void save (final File file)
   {
      this.mapFile = file;

      Collection<String> lines = new ArrayList<String>();

      // properties
      props.put ("Version", "20080929");
      for (Map.Entry<String, String> entry : props.entrySet())
         lines.add (entry.getKey() + "=" + entry.getValue());

      // tiles
      for (java.util.Map.Entry<String, Integer> entry : tiles.entrySet())
         lines.add (entry.getValue() + "=" + entry.getKey());

      for (Layer layer : layers)
         saveLayer (lines, layer); 
      
      // linear features
      for (Line stream : streams)
         lines.add (stream.toString());
      for (Line path : paths)
         lines.add (path.toString());
         
      FileUtils.writeList (lines, file.getPath(), false);
      needsSave = false;
   }

   private void saveLayer (final Collection<String> lines, final Layer layer)
   {
      // save the given (run-length encoded) layer
      for (MatrixPair<Tile> pair : layer.getDataCompressed()) 
      {
         Tile tile = pair.getValue();
         if (tile != null)
            lines.add (pair.getCount() + "x" + tiles.get (tile.getFile()));
         else
            lines.add (pair.getCount() + "x0"); // null tile
      }
   }

   public void backup (final String fileName)
   {
      File f = mapFile;
      save (new File (fileName));
      mapFile = f; // restore the real file
   }

   public boolean restore (final String fileName) // undo
   {
      boolean restored = false;
      File backup = new File (fileName);
      if (backup.exists())
      {
         File f = mapFile;
         load (backup);
         mapFile = f; // restore the real file
         restored = true;
      }
      return restored;
   }
}
