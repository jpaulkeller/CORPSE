package map.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Random;

public class Lines extends ArrayList<Line>
{
   private static final long serialVersionUID = 1L;
   private static final Random RANDOM = new Random();

   private Stroke outerStroke;
   private Stroke innerStroke;
   private Color outerColor;
   private Color innerColor;

   public void setOuterStroke(final Stroke outerStroke)
   {
      this.outerStroke = outerStroke;
   }

   public void setInnerStroke(final Stroke innerStroke)
   {
      this.innerStroke = innerStroke;
   }

   public void setOuterColor(final Color outerColor)
   {
      this.outerColor = outerColor;
   }

   public void setInnerColor(final Color innerColor)
   {
      this.innerColor = innerColor;
   }

   public void paintLines(final Graphics2D g, final Scale scale, final Point from, final Point to, final boolean rubberBand)
   {
      paintLayer(g, scale, from, to, outerStroke, outerColor, rubberBand);
      paintLayer(g, scale, from, to, innerStroke, innerColor, rubberBand);
   }

   private void paintLayer(final Graphics2D g, final Scale scale, final Point from, final Point to, final Stroke stroke,
                           final Color color, final boolean rubberBand)
   {
      g.setStroke(stroke);
      g.setColor(color);
      for (Line line : this)
         paintSegment(g, scale, line.getFrom(), line.getTo(), true);
      if (rubberBand && from != null && to != null)
         paintSegment(g, scale, from, to, false);
   }

   private void paintSegment(final Graphics2D g, final Scale scale, final Point from, final Point to, final boolean distort)
   {
      int sf = scale.getFactor();
      if (distort)
      {
         // "wiggle" the segment, by adding a mid-point (randomly offset in any direction)
         RANDOM.setSeed(from.x * from.y + to.x * to.y);
         float randX = RANDOM.nextFloat();
         float randY = RANDOM.nextFloat();
         int deltaX = Math.abs(from.x - to.x);
         int deltaY = Math.abs(from.y - to.y);
         int size = Math.max(scale.getCellSize() / 3, Math.max(deltaX, deltaY) / 2);
         int xMid = (from.x + to.x) / 2 + Math.round(size * randX) - size / 2;
         int yMid = (from.y + to.y) / 2 + Math.round(size * randY) - size / 2;
         g.drawLine(from.x / sf, from.y / sf, xMid / sf, yMid / sf);
         g.drawLine(xMid / sf, yMid / sf, to.x / sf, to.y / sf);
      }
      else
         g.drawLine(from.x / sf, from.y / sf, to.x / sf, to.y / sf);
   }
}
