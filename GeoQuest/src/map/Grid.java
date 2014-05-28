package map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public enum Grid
{
   None,
   Square,
   Hex;
   
   public static void paintGrid (final Graphics g, final int gridSize,
                                 final int maxX, final int maxY)
   {
      g.setColor (Color.BLACK);

      // draw horizontal lines
      int y = gridSize;
      while (y <= maxY)
      {
         g.drawLine (0, y, maxX, y);
         y += gridSize;
      }
      
      // draw vertical lines
      int x = 0;
      while (x <= maxX)
      {
         g.drawLine (x, 0, x, maxY);
         x += gridSize;
      }
   }
   
   public static void paintGridHex (final Graphics g, final int gridSize,
                                    final int maxX, final int maxY)
   {
      g.setColor (Color.BLACK);

      int h = calculateH (gridSize);
      int r = calculateR (gridSize);
      Point p = new Point (0, 0);

      while (p.y < maxY)
      {
         while (p.x < maxX)
            drawHexLine (g, p, gridSize, h, r); // top line /-\_
         p.x = 0;
         
         while (p.x < maxX)
            drawHexLine (g, p, gridSize, h, -r); // bottom line \_/-
         p.x = 0;
         p.y += 2 * r;
      }
   }

   private static void drawHexLine (final Graphics g, final Point p,
                                    final float gridSize, final int h, final int r)
   {
      int x2 = p.x + h;
      int y2 = p.y - r;
      g.drawLine (p.x, p.y, x2, y2);
      p.x = x2;
      p.y = y2;
      
      x2 = Math.round (p.x + gridSize);
      g.drawLine (p.x, p.y, x2, y2);
      p.x = x2;
      
      x2 = p.x + h;
      y2 = p.y + r;
      g.drawLine (p.x, p.y, x2, y2);
      p.x = x2;
      p.y = y2;
      
      x2 = Math.round (p.x + gridSize);
      g.drawLine (p.x, p.y, x2, y2);
      p.x = x2;
   }

   private static final double RADIANS = 30 * Math.PI / 180;
   
   private static int calculateH (final float gridSize)
   {
      return (int) Math.round (Math.sin (RADIANS) * gridSize); 
   }
   
   private static int calculateR (final float gridSize)
   {
      return (int) Math.round (Math.cos (RADIANS) * gridSize); 
   }
}
