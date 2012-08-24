package gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * VFlowLayout is similar to FlowLayout except it lays out components
 * vertically. Extends FlowLayout because it mimics much of the behavior of the
 * FlowLayout class, except vertically. An additional feature is that you can
 * specify a fill to edge flag, which causes the VFlowLayout manager to resize
 * all components to expand to the column width Warning: This causes problems
 * when the main panel has less space that it needs and it seems to prohibit
 * multi-column output
 */
public class VFlowLayout extends FlowLayout
{
   private static final long serialVersionUID = 1;
   
   private static final int GAP = 5;

   public static final int TOP = 0;
   public static final int MIDDLE = 1;
   public static final int BOTTOM = 2;

   private int align;
   private int hgap;
   private int vgap;
   private boolean fill;

   /**
    * Construct a new VFlowLayout with a middle alignment, and the fill to edge
    * flag set.
    */
   public VFlowLayout ()
   {
      this (MIDDLE, GAP, GAP, true);
   }

   /**
    * Construct a new VFlowLayout with a middle alignment.
    * 
    * @param fill
    *           the fill to edge flag
    */
   public VFlowLayout (final boolean fill)
   {
      this (MIDDLE, GAP, GAP, fill);
   }

   /**
    * Construct a new VFlowLayout with a middle alignment.
    * 
    * @param align
    *           the alignment value
    */
   public VFlowLayout (final int align)
   {
      this (align, GAP, GAP, true);
   }

   /**
    * Construct a new VFlowLayout.
    * 
    * @param align
    *           the alignment value
    * @param fill
    *           the fill to edge flag
    */
   public VFlowLayout (final int align, final boolean fill)
   {
      this (align, GAP, GAP, fill);
   }

   /**
    * Construct a new VFlowLayout.
    * 
    * @param align
    *           the alignment value
    * @param hgap
    *           the horizontal gap variable
    * @param vgap
    *           the vertical gap variable
    * @param fill
    *           the fill to edge flag
    */
   public VFlowLayout (final int align, final int hgap, final int vgap,
                       final boolean fill)
   {
      this.align = align;
      this.hgap = hgap;
      this.vgap = vgap;
      this.fill = fill;
   }

   /**
    * Returns the preferred dimensions given the components in the target
    * container.
    * 
    * @param target
    *           the component to lay out
    */
   @Override
   public Dimension preferredLayoutSize (final Container target)
   {
      Dimension tarsiz = new Dimension (0, 0);

      for (int i = 0; i < target.getComponentCount (); i++)
      {
         Component m = target.getComponent (i);
         if (m.isVisible ())
         {
            Dimension d = m.getPreferredSize ();
            tarsiz.width = Math.max (tarsiz.width, d.width);
            if (i > 0)
               tarsiz.height += hgap;
            tarsiz.height += d.height;
         }
      }
      Insets insets = target.getInsets ();
      tarsiz.width += insets.left + insets.right + hgap * 2;
      tarsiz.height += insets.top + insets.bottom + vgap * 2;
      return tarsiz;
   }

   /**
    * Returns the minimum size needed to layout the target container.
    * 
    * @param target
    *           the component to lay out
    */
   @Override
   public Dimension minimumLayoutSize (final Container target)
   {
      Dimension tarsiz = new Dimension (0, 0);

      for (int i = 0; i < target.getComponentCount (); i++)
      {
         Component m = target.getComponent (i);
         if (m.isVisible ())
         {
            Dimension d = m.getMinimumSize ();
            tarsiz.width = Math.max (tarsiz.width, d.width);
            if (i > 0)
               tarsiz.height += vgap;
            tarsiz.height += d.height;
         }
      }
      Insets insets = target.getInsets ();
      tarsiz.width += insets.left + insets.right + hgap * 2;
      tarsiz.height += insets.top + insets.bottom + vgap * 2;
      return tarsiz;
   }

   /**
    * Places the components defined by first to last within the target container
    * using the bounds box defined.
    * 
    * @param target
    *           the container
    * @param x
    *           the x coordinate of the area
    * @param y
    *           the y coordinate of the area
    * @param width
    *           the width of the area
    * @param height
    *           the height of the area
    * @param first
    *           the first component of the container to place
    * @param last
    *           the last component of the container to place
    */
   private void placethem (final Container target, 
                           final int x, final int y,
                           final int width, final int height,
                           final int first, final int last)
   {
      int yPos = y;
      if (align == VFlowLayout.MIDDLE)
         yPos += height / 2;
      else if (align == VFlowLayout.BOTTOM)
         yPos += height;

      for (int i = first; i < last; i++)
      {
         Component m = target.getComponent (i);
         Dimension md = m.getSize ();
         if (m.isVisible ())
         {
            m.setLocation (x + (width - md.width) / 2, yPos);
            yPos += vgap + md.height;
         }
      }
   }

   /**
    * Lays out the container.
    * 
    * @param target
    *           the container to lay out.
    */
   @Override
   public void layoutContainer (final Container target)
   {
      Insets insets = target.getInsets ();
      int maxheight = target.getSize ().height -
      (insets.top + insets.bottom + vgap * 2);
      int maxwidth = target.getSize ().width -
      (insets.left + insets.right + hgap * 2);
      int numcomp = target.getComponentCount ();
      int x = insets.left + hgap, y = 0;
      int colw = 0, start = 0;

      for (int i = 0; i < numcomp; i++)
      {
         Component m = target.getComponent (i);
         if (m.isVisible ())
         {
            Dimension d = m.getPreferredSize ();
            if (this.fill)
            {
               m.setSize (maxwidth, d.height);
               d.width = maxwidth;
            }
            else
            {
               m.setSize (d.width, d.height);
            }

            if (y > maxheight)
            {
               placethem (target, x, insets.top + vgap, colw, maxheight - y,
                        start, i);
               y = d.height;
               x += hgap + colw;
               colw = d.width;
               start = i;
            }
            else
            {
               if (y > 0)
                  y += vgap;
               y += d.height;
               colw = Math.max (colw, d.width);
            }
         }
      }
      placethem (target, x, insets.top + vgap, colw, maxheight - y, start,
               numcomp);
   }
}
