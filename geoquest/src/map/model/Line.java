package map.model;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import map.Scale;

public class Line
{
   private static final Pattern LINE_PATTERN = 
      Pattern.compile ("(?:([PS]): )?([0-9]+),([0-9]+) ([0-9]+),([0-9]+)");
   
   private Point from, to;
   private int xOff, yOff;
   private String type;
   
   public Line (final String type, final Point from, final Point to)
   {
      this.type = type;
      this.from = new Point (from);
      this.to = new Point (to);
   }
   
   public static Line getLine (final String encoded)
   {
      Line line = null;
      Matcher m = LINE_PATTERN.matcher (encoded);
      if (m.matches())
      {
         String type = m.group (1);
         int x1 = Integer.parseInt (m.group (2));
         int y1 = Integer.parseInt (m.group (3));
         int x2 = Integer.parseInt (m.group (4));
         int y2 = Integer.parseInt (m.group (5));
         line = new Line (type, new Point (x1, y1), new Point (x2, y2));
      }
      return line;
   }

   public String getType()
   {
      return type;
   }
   
   public Point getFrom()
   {
      return from;
   }

   public Point getTo()
   {
      return to;
   }
   
   public void offset (final int pixels)
   {
      xOff = (int) Math.round (Math.random() * pixels) - (pixels / 2);
      yOff = (int) Math.round (Math.random() * pixels) - (pixels / 2);
   }
   
   public int getX()
   {
      return xOff;
   }
   
   public int getY()
   {
      return yOff;
   }
   
   // Clips the line (if necessary) to fit into the resized matrix.  Returns 
   // true if the new line intersects the matrix.
   
   public boolean clip (final int width, final int height)
   {
      Rectangle2D box = new Rectangle2D.Float (0, 0, width, height);
      if (box.contains (from.x, from.y) && box.contains (to.x, to.y))
         return true; // no clipping needed
      
      Line2D line = new Line2D.Float (from.x, from.y, to.x, to.y);
      if (!line.intersects (box))
         return false; // clip the whole line

      return true;
   }

   public void shift (final int rowDelta, final int colDelta, final Scale scale)
   {
      from.x += colDelta * scale.getCellSize();
      from.y += rowDelta *  scale.getCellSize();
      to.x += colDelta *  scale.getCellSize();
      to.y += rowDelta *  scale.getCellSize();
   }

   public void flipLeftRight (final int width)
   {
      from.x = width - from.x;
      to.x = width - to.x;
   }

   public void flipTopBottom (final int height)
   {
      from.y = height - from.y;
      to.y = height - to.y;
   }

   public void rotateCW (final int size)
   {
      int tmp = from.y;
      from.y = from.x;
      from.x = size - tmp;
      
      tmp = to.y;
      to.y = to.x;
      to.x = size - tmp;
      
      flipOffsets();
   }

   private void flipOffsets()
   {
      int tmp = xOff;
      xOff = yOff;
      yOff = tmp;
   }
   
   @Override
   public String toString()
   {
      return type + ": " + from.x + "," + from.y + " " + to.x + "," + to.y;
   }
}
