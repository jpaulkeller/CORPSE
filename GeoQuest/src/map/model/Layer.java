package map.model;

import map.MapPanel;
import model.ObjectMatrix;

public class Layer extends ObjectMatrix<Tile>
{
   private int depth;
   
   public Layer (final int rows, final int cols, final int depth)
   {
      super (rows, cols);
      this.depth = depth;
   }
   
   public int getDepth()
   {
      return depth;
   }
   
   Tile getTile (final Cell cell)
   {
      return getValue (cell.getRow(), cell.getCol());
   }
   
   @Override
   public String toString()
   {
      return "Layer: " + depth + " " + getRowCount() + " rows, " +
             getColumnCount() + " columns"; 
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
      int topRow  = cell.getRow() - (cell.getRow() % MapPanel.CELLS_PER_GRID);
      int leftCol = cell.getCol() - (cell.getCol() % MapPanel.CELLS_PER_GRID);
      for (int row = topRow; row < topRow + MapPanel.CELLS_PER_GRID; row++)
         for (int col = leftCol; col < leftCol + MapPanel.CELLS_PER_GRID; col++)
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
