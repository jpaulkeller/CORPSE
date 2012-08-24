package map;

import gui.CursorFactory;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;

public enum Mode
{
   Draw (true, false, false, null, Color.GREEN,
         "icons/cursors/Draw.gif", new Point (1, 18)),
   Fill (true, false, false, null, Color.MAGENTA,
         "icons/cursors/Fill.gif", new Point (10, 17)),
   Box (true, true, false, null, Color.CYAN,
        "icons/cursors/Box.gif", new Point (4, 8)),
   Path (false, false, true, "P", Color.YELLOW,
         "icons/cursors/Path.gif", new Point (3, 13)),
   Stream (false, false, true, "S", Color.BLUE, null, null),
   Erase (false, true, false, null, Color.RED, 
          "icons/cursors/Erase.gif", new Point (10, 10));
   
   private boolean usesTile;
   private boolean usesBox;
   private boolean usesLine;
   private String lineType;
   private Color color;
   private Cursor cursor;
   
   private Mode (final boolean usesTile,
                 final boolean usesBox, 
                 final boolean usesLine,
                 final String lineType,
                 final Color color,
                 final String cursorPath,
                 final Point hotSpot)
   {
      this.usesTile = usesTile;
      this.usesBox = usesBox;
      this.usesLine = usesLine;
      this.lineType = lineType;
      this.color = color;
      
      if (cursorPath != null)
         this.cursor = CursorFactory.getCursor (cursorPath, hotSpot);
      if (cursor == null)
         this.cursor = Cursor.getDefaultCursor(); 
   }
   
   public boolean usesTile()
   {
      return usesTile;
   }
   
   public boolean usesBox()
   {
      return usesBox;
   }
   
   public boolean usesLine()
   {
      return usesLine;
   }
   
   public String getLineType()
   {
      return lineType;
   }
   
   public Color getColor()
   {
      return color;
   }
   
   public Cursor getCursor()
   {
      return cursor;
   }
}
