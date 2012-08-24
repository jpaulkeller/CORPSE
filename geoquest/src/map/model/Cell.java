package map.model;

public class Cell
{
   private int row;
   private int col;
   
   public Cell (final int row, final int col)
   {
      this.row = row;
      this.col = col;
   }
   
   public Cell (final Cell cell)
   {
      this (cell.row, cell.col);
   }
   
   public int getRow()
   {
      return row;
   }

   public int getCol()
   {
      return col;
   }

   public void setRow (final int row)
   {
      this.row = row;
   }

   public void setCol (final int col)
   {
      this.col = col;
   }

   public void clear()
   {
      row = -1;
      col = -1;
   }
   
   public void set (final Cell cell)
   {
      row = cell.row;
      col = cell.col;
   }
   
   public boolean isValid()
   {
      return row >= 0 && col >= 0;
   }
   
   @Override
   public boolean equals (final Object cell)
   {
      if (cell instanceof Cell)
         return row == ((Cell) cell).row && col == ((Cell) cell).col;
      return false;
   }
   
   @Override
   public int hashCode()
   {
      return row * 10 + col;
   }
   
   @Override
   public String toString()
   {
      return row + "," + col;
   }   
}

