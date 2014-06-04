package map.model;

public enum Scale
{
   Full (1),
   Half (2),
   Quarter (4);
   
   public static final int CELL_SIZE = 64;
   public static final int GRID_COUNT = 13;
   public static final int CELLS_PER_GRID = 2;

   private int scaleFactor;
   
   private Scale (final int scaleFactor)
   {
      this.scaleFactor = scaleFactor;
   }
   
   public int getFactor()
   {
      return scaleFactor;
   }
   
   public int getCellSize()
   {
      return CELL_SIZE / scaleFactor;
   }
}
