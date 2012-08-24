package map.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;

import map.Scale;

public class Lines extends ArrayList<Line>
{
   private static final long serialVersionUID = 1L;

   private Stroke outerStroke;
   private Stroke innerStroke;
   private Color outerColor;
   private Color innerColor;

   public void setOuterStroke (final Stroke outerStroke)
   {
      this.outerStroke = outerStroke;
   }

   public void setInnerStroke (final Stroke innerStroke)
   {
      this.innerStroke = innerStroke;
   }

   public void setOuterColor (final Color outerColor)
   {
      this.outerColor = outerColor;
   }

   public void setInnerColor (final Color innerColor)
   {
      this.innerColor = innerColor;
   }

   public void paintLines (final Graphics2D g, final Scale scale,
                           final Point from, final Point to,
                           final boolean rubberBand)
   {
      paintLayer (g, scale, from, to, outerStroke, outerColor, rubberBand);
      paintLayer (g, scale, from, to, innerStroke, innerColor, rubberBand);
   }

   private void paintLayer (final Graphics2D g, final Scale scale, 
                            final Point from, final Point to,
                            final Stroke stroke, final Color color,
                            final boolean rubberBand)
   {
      g.setStroke (stroke);
      g.setColor (color);
      for (Line line : this)
         paintSegment (g, scale, line.getFrom(), line.getTo(), line.getX(), line.getY());
      if (rubberBand && from != null && to != null)
         paintSegment (g, scale, from, to, 0, 0);
   }

   private void paintSegment (final Graphics2D g, final Scale scale, 
                              final Point from, final Point to,
                              final int xOff, final int yOff)
   {
      int sf = scale.getFactor();
      if (xOff != 0 || yOff != 0)
      {
         int xMid = (from.x + to.x) / 2 + xOff;
         int yMid = (from.y + to.y) / 2 + yOff;
         
         g.drawLine (from.x / sf, from.y / sf, xMid / sf, yMid / sf);
         g.drawLine (xMid / sf, yMid / sf, to.x / sf, to.y / sf);
      }
      else
         g.drawLine (from.x / sf, from.y / sf, to.x / sf, to.y / sf);
   }
}
