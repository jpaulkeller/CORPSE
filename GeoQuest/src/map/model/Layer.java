package map.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.ObjectMatrix;

public class Layer extends ObjectMatrix<Tile>
{
   private String name;
   private int depth;
   private boolean hidden;
   private boolean translucent;
   private boolean offset; // randomly offset the position to provide some variety
   private boolean rotate; // randomly rotate the icon to provide some variety
   
   private static final String BOOL = "(true|false)";
   public static final Pattern LAYER = Pattern.compile ("Layer: ([^ ]+) " +
                                                       " Depth: ([0-9]+) " +
                                                       " Rows: ([0-9]+) " +
                                                       " Columns: ([0-9]+) " +
                                                       " Translucent: " + BOOL +
                                                       " Hidden: " + BOOL +
                                                       " Offset: " + BOOL +
                                                       " Rotate: " + BOOL);

   @Override
   public String toString()
   {
      return "Layer: " + getName() + 
            " Depth: " + depth + 
            " Rows: " + getRowCount() + 
            " Columns: " + getColumnCount() +
            " Translucent: " + translucent +
            " Hidden: " + hidden +
            " Offset: " + offset +
            " Rotate: " + rotate;
   }
   
   public Layer (final String layerLine)
   {
      Matcher m = LAYER.matcher(layerLine);
      if (m.matches())
      {
         int g = 1;
         setName(m.group(g++)); // use the depth, for now
         int depth = Integer.parseInt(m.group(g++));
         int rows = Integer.parseInt(m.group(g++));
         int cols = Integer.parseInt(m.group(g++));
         translucent = Boolean.parseBoolean(m.group(g++));
         hidden = Boolean.parseBoolean(m.group(g++));
         offset = Boolean.parseBoolean(m.group(g++));
         rotate = Boolean.parseBoolean(m.group(g++));
         
         setData(new Tile[rows][cols]);
      }
   }
   
   public Layer (final int rows, final int cols, final int depth)
   {
      super (rows, cols);
      setName(depth + "");
      this.depth = depth;
   }
   
   public void setName(final String name)
   {
      this.name = name;
   }
   
   public String getName()
   {
      return name;
   }
   
   public int getDepth()
   {
      return depth;
   }
   
   Tile getTile (final Cell cell)
   {
      return getValue (cell.getRow(), cell.getCol());
   }
   
   void fillTiles (final Cell cell, final Tile tile)
   {
      Tile from = getTile (cell); // tile to be replaced
      if (from == null && depth > 0)
         fillBlock (cell, tile);
      else if (tile != null && !tile.equals (from))
         fillRecursive (cell, from, tile);
   }
   
   private void fillBlock (final Cell cell, final Tile tile)
   {
      int topRow  = cell.getRow() - (cell.getRow() % Scale.CELLS_PER_GRID);
      int leftCol = cell.getCol() - (cell.getCol() % Scale.CELLS_PER_GRID);
      for (int row = topRow; row < topRow + Scale.CELLS_PER_GRID; row++)
         for (int col = leftCol; col < leftCol + Scale.CELLS_PER_GRID; col++)
            setValue (row, col, tile);
   }
   
   private void fillRecursive (final Cell cell, final Tile from, final Tile to) 
   {
      setValue (cell.getRow(), cell.getCol(), to);
      // recurse to fill adjacent tiles
      fill (cell.getRow() - 1, cell.getCol(), from, to); // up
      fill (cell.getRow() + 1, cell.getCol(), from, to); // down
      fill (cell.getRow(), cell.getCol() - 1, from, to); // left
      fill (cell.getRow(), cell.getCol() + 1, from, to); // right
   }
   
   private void fill (final int row, final int col, final Tile from, final Tile to)
   {
      if (row < 0 || row >= getRowCount() || col < 0 || col >= getColumnCount())
         return;

      Tile tile = getValue (row, col);
      if (tile == null && from == null && depth == 0)
         fillRecursive (new Cell (row, col), null, to);
      if (tile != null && tile.equals (from))
         fillRecursive (new Cell (row, col), from, to);
   }

   public void shift (final int rowDelta, final int colDelta)
   {
      int fromRow, toRow, fromCol, toCol;
      int rowIncr = 1, colIncr = 1;
      
      if (rowDelta <= 0) // up
      {
         fromRow = -rowDelta;
         toRow = getRowCount();
      }
      else // down
      {
         fromRow = getRowCount() - rowDelta - 1;
         toRow = 0;
         rowIncr = -1;
      }
      if (colDelta <= 0) // left
      {
         fromCol = -colDelta;
         toCol = getColumnCount();
      }
      else // right
      {
         fromCol = getColumnCount() - colDelta - 1;
         toCol = 0;
         colIncr = -1;
      }

      for (int row = fromRow; row != toRow; row += rowIncr)
         for (int col = fromCol; col != toCol; col += colIncr)
            setValue (row + rowDelta, col + colDelta, getValue (row, col));
   }

   public void flipLeftRight()
   {
      for (int c1 = 0, c2 = getColumnCount() - 1; c1 < c2; c1++, c2--)
         swapColumns (c1, c2);
   }
   
   public void flipTopBottom()
   {
      for (int r1 = 0, r2 = getRowCount() - 1; r1 < r2; r1++, r2--)
         swapRows (r1, r2);
   }
   
   public void rotateCW()
   {
      Tile[][] newData = new Tile[getColumnCount()][getRowCount()]; // may not be square
      for (int row = 0; row < getRowCount(); row++)
         for (int col = 0; col < getColumnCount(); col++)
            newData [col][getRowCount() - row - 1] = getValue (row, col);
         
      setData (newData);
   }
   
   private void swapColumns (final int c1, final int c2)
   {
      for (int row = 0; row < getRowCount(); row++)
      {
         Tile v1 = getValue (row, c1);
         setValue (row, c1, getValue (row, c2));
         setValue (row, c2, v1);
      }
   }
   
   private void swapRows (final int r1, final int r2)
   {
      for (int col = 0; col < getColumnCount(); col++)
      {
         Tile v1 = getValue (r1, col);
         setValue (r1, col, getValue (r2, col));
         setValue (r2, col, v1);
      }
   }
}
