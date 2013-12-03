package gui.comp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Implements a Swing-based Range slider, which allows the user to enter a range
 * (minimum and maximum) value.
 */

public class RangeSlider extends JComponent
{
   public static final int VERTICAL = 0;
   public static final int HORIZONTAL = 1;
   public static final int LEFTRIGHT_TOPBOTTOM = 0;
   public static final int RIGHTLEFT_BOTTOMTOP = 1;

   private static final int ARROW_SZ = 16;
   private static final int ARROW_WIDTH = 8;
   private static final int ARROW_HEIGHT = 4;
   
   private static final Font FONT = new Font ("Arial", Font.PLAIN, 9);
   private Color thumbColor = new Color (150, 180, 220);
   private boolean useLabels;
   private int prefWidth = 16;
   private int prefLength = 300;

   private BoundedRangeModel model;
   private int orientation;
   private int direction;
   private boolean empty;
   private int minExtent = 0; // min extent, in pixels

   private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
   private ChangeListener listener;
   private ChangeEvent changeEvent;

   // ------------------------------------------------------------------------

   /**
    * Create a new range slider.
    * 
    * @param minimum - the minimum value of the range.
    * @param maximum - the maximum value of the range.
    * @param lowValue - the current low value shown by the range slider's bar.
    * @param highValue - the current high value shown by the range slider's bar.
    * @param orientation - construct a horizontal or vertical slider?
    */
   public RangeSlider (final int minimum, final int maximum, 
                       final int lowValue, final int highValue,
                       final int orientation)
   {
      this (new DefaultBoundedRangeModel (lowValue, highValue - lowValue, minimum,
                                          maximum), orientation, LEFTRIGHT_TOPBOTTOM);
   }

   /**
    * Create a new range slider.
    * 
    * @param minimum - the minimum value of the range.
    * @param maximum - the maximum value of the range.
    * @param lowValue - the current low value shown by the range slider's bar.
    * @param highValue - the current high value shown by the range slider's bar.
    * @param orientation - construct a horizontal or vertical slider?
    * @param direction - left-to-right/top-to-bottom or right-to-left/bottom-to-top
    */
   public RangeSlider (final int minimum, final int maximum, 
                       final int lowValue, final int highValue,
                       final int orientation, final int direction)
   {
      this (new DefaultBoundedRangeModel (lowValue, highValue - lowValue, minimum,
                                          maximum), orientation, direction);
   }

   /**
    * Create a new range slider.
    * 
    * @param model - a BoundedRangeModel specifying the slider's range
    * @param orientation - construct a horizontal or vertical slider?
    * @param direction - left-to-right/top-to-bottom or right-to-left/bottom-to-top
    */
   public RangeSlider (final BoundedRangeModel model, final int orientation, 
                       final int direction)
   {
      super.setFocusable (true);
      this.model = model;
      this.orientation = orientation;
      this.direction = direction;

      this.listener = createListener();
      model.addChangeListener (listener);

      MyMouseListener mouseListener = new MyMouseListener();
      addMouseListener (mouseListener);
      addMouseMotionListener (mouseListener);
      addKeyListener (new MyKeyListener());
   }

   public void setUseLabels (final boolean useLabels)
   {
      this.useLabels = useLabels;
   }
   
   public void setPreferredSize (final int wid, final int len)
   {
      this.prefWidth = wid;
      this.prefLength = len;
   }
   
   /**
    * Create a listener to relay change events from the bounded range model.
    * 
    * @return a ChangeListener to relay events from the range model
    */
   protected ChangeListener createListener()
   {
      return new RangeSliderChangeListener();
   }

   /**
    * Listener that fires a change event when it receives change event from the
    * slider list model.
    */
   protected class RangeSliderChangeListener implements ChangeListener
   {
      @Override
      public void stateChanged (final ChangeEvent e)
      {
         fireChangeEvent();
      }
   }

   /**
    * Returns the current "low" value shown by the range slider's bar. The low
    * value meets the constraint minimum <= lowValue <= highValue <= maximum.
    */
   public int getLowValue()
   {
      return model.getValue();
   }

   /**
    * Sets the low value shown by this range slider. This causes the range
    * slider to be repainted and a ChangeEvent to be fired.
    * 
    * @param lowValue - the low value to use
    */
   public void setLowValue (final int lowValue)
   {
      int e = (model.getValue() - lowValue) + model.getExtent();
      model.setRangeProperties (lowValue, e, model.getMinimum(), model.getMaximum(),
                                false);
      model.setValue (lowValue);
   }

   /**
    * Returns the current "high" value shown by the range slider's bar. The high
    * value meets the constraint minimum <= lowValue <= highValue <= maximum.
    */
   public int getHighValue()
   {
      return model.getValue() + model.getExtent();
   }

   /**
    * Sets the high value shown by this range slider. This causes the range
    * slider to be repainted and a ChangeEvent to be fired.
    * 
    * @param highValue - the high value to use
    */
   public void setHighValue (final int highValue)
   {
      model.setExtent (highValue - model.getValue());
   }

   /**
    * Set the slider range span.
    * 
    * @param lowValue the low value of the slider range
    * @param highValue the high value of the slider range
    */
   public void setRange (final int lowValue, final int highValue)
   {
      model.setRangeProperties (lowValue, highValue - lowValue, model.getMinimum(),
                                model.getMaximum(), false);
   }

   /**
    * Gets the minimum possible value for either the low value or the high
    * value.
    * 
    * @return the minimum possible range value
    */
   public int getMinimum()
   {
      return model.getMinimum();
   }

   /**
    * Sets the minimum possible value for either the low value or the high
    * value.
    * 
    * @param minimum the minimum possible range value
    */
   public void setMinimum (final int minimum)
   {
      model.setMinimum (minimum);
   }

   /**
    * Gets the maximum possible value for either the low value or the high
    * value.
    * 
    * @return the maximum possible range value
    */
   public int getMaximum()
   {
      return model.getMaximum();
   }

   /**
    * Sets the maximum possible value for either the low value or the high
    * value.
    * 
    * @param maximum the maximum possible range value
    */
   public void setMaximum (final int maximum)
   {
      model.setMaximum (maximum);
   }

   /**
    * Sets the minimum extent (difference between low and high values). This
    * method <strong>does not</strong> change the current state of the model,
    * but can affect all subsequent interaction.
    * 
    * @param minExtent the minimum extent allowed in subsequent interaction
    */
   public void setMinExtent (final int minExtent)
   {
      this.minExtent = minExtent;
   }

   /**
    * Sets whether this slider is empty.
    * 
    * @param empty true if set to empty, false otherwise
    */
   public void setEmpty (final boolean empty)
   {
      this.empty = empty;
      repaint();
   }

   /**
    * Get the slider thumb color. This is the part of the slider between the
    * range resize buttons.
    * 
    * @return the slider thumb color
    */
   public Color getThumbColor()
   {
      return thumbColor;
   }

   /**
    * Set the slider thumb color. This is the part of the slider between the
    * range resize buttons.
    * 
    * @param thumbColor the slider thumb color
    */
   public void setThumbColor (final Color thumbColor)
   {
      this.thumbColor = thumbColor;
   }

   /**
    * Get the BoundedRangeModel backing this slider.
    * 
    * @return the slider's range model
    */
   public BoundedRangeModel getModel()
   {
      return model;
   }

   /**
    * Set the BoundedRangeModel backing this slider.
    * 
    * @param brm the slider range model to use
    */
   public void setModel (final BoundedRangeModel brm)
   {
      model.removeChangeListener (listener);
      model = brm;
      model.addChangeListener (listener);
      repaint();
   }

   /**
    * Registers a listener for ChangeEvents.
    * 
    * @param cl the ChangeListener to add
    */
   public void addChangeListener (final ChangeListener cl)
   {
      if (!listeners.contains (cl))
         listeners.add (cl);
   }

   /**
    * Removes a listener for ChangeEvents.
    * 
    * @param cl the ChangeListener to remove
    */
   public void removeChangeListener (final ChangeListener cl)
   {
      listeners.remove (cl);
   }

   /**
    * Fire a change event to all listeners.
    */
   protected void fireChangeEvent()
   {
      repaint();
      if (changeEvent == null)
         changeEvent = new ChangeEvent (this);
      Iterator<ChangeListener> iter = listeners.iterator();
      while (iter.hasNext())
         iter.next().stateChanged (changeEvent);
   }

   @Override
   public Dimension getPreferredSize()
   {
      if (orientation == VERTICAL)
         return new Dimension (prefWidth, prefLength);
      return new Dimension (prefLength, prefWidth);
   }

   // ------------------------------------------------------------------------
   // Rendering

   /**
    * Override this method to perform custom painting of the slider trough.
    * 
    * @param g a Graphics2D context for rendering
    * @param width the width of the slider trough
    * @param height the height of the slider trough
    */
   protected void customPaint (final Graphics2D g, final int width, final int height)
   {
      // does nothing in this class
      // subclasses can override to perform custom painting
   }

   @Override
   public void paintComponent (final Graphics g)
   {
      Rectangle bounds = getBounds();
      int width = (int) bounds.getWidth() - 1;
      int height = (int) bounds.getHeight() - 1;

      int min = toScreen (getLowValue());
      int max = toScreen (getHighValue());

      // Paint the full slider if the slider is marked as empty
      if (empty)
      {
         if (direction == LEFTRIGHT_TOPBOTTOM)
         {
            min = ARROW_SZ;
            max = (orientation == VERTICAL) ? height - ARROW_SZ : width - ARROW_SZ;
         }
         else
         {
            min = (orientation == VERTICAL) ? height - ARROW_SZ : width - ARROW_SZ;
            max = ARROW_SZ;
         }
      }

      Graphics2D g2 = (Graphics2D) g;
      g2.setColor (getBackground());
      g2.fillRect (0, 0, width, height);
      g2.setColor (getForeground());
      g2.drawRect (0, 0, width, height);

      customPaint (g2, width, height);

      // Draw arrow and thumb backgrounds
      g2.setStroke (new BasicStroke (1));
      if (orientation == VERTICAL)
      {
         if (direction == LEFTRIGHT_TOPBOTTOM)
         {
            g2.setColor (getForeground());
            g2.fillRect (0, min - ARROW_SZ, width, ARROW_SZ - 1);
            paint3DRectLighting (g2, 0, min - ARROW_SZ, width, ARROW_SZ - 1);

            paintThumb (g2, 0, min, width, max - min - 1);

            g2.setColor (getForeground());
            g2.fillRect (0, max, width, ARROW_SZ - 1);
            paint3DRectLighting (g2, 0, max, width, ARROW_SZ - 1);

            // Draw arrows
            g2.setColor (Color.black);
            paintHandle (g2, (width - ARROW_WIDTH) / 2.0, 
                         min - ARROW_SZ + (ARROW_SZ - ARROW_HEIGHT) / 2.0, 
                         ARROW_WIDTH, ARROW_HEIGHT, true);
            paintHandle (g2, (width - ARROW_WIDTH) / 2.0, 
                         max + (ARROW_SZ - ARROW_HEIGHT) / 2.0, 
                         ARROW_WIDTH, ARROW_HEIGHT, false);
         }
         else
         {
            g2.setColor (getForeground());
            g2.fillRect (0, min, width, ARROW_SZ - 1);
            paint3DRectLighting (g2, 0, min, width, ARROW_SZ - 1);

            paintThumb (g2, 0, max, width, min - max - 1);

            g2.setColor (getForeground());
            g2.fillRect (0, max - ARROW_SZ, width, ARROW_SZ - 1);
            paint3DRectLighting (g2, 0, max - ARROW_SZ, width, ARROW_SZ - 1);

            // Draw arrows
            g2.setColor (Color.black);
            paintHandle (g2, (width - ARROW_WIDTH) / 2.0, 
                         min + (ARROW_SZ - ARROW_HEIGHT) / 2.0, 
                         ARROW_WIDTH, ARROW_HEIGHT, false);
            paintHandle (g2, (width - ARROW_WIDTH) / 2.0, 
                         max - ARROW_SZ + (ARROW_SZ - ARROW_HEIGHT) / 2.0, 
                         ARROW_WIDTH, ARROW_HEIGHT, true);
         }
      }
      else
      {
         if (direction == LEFTRIGHT_TOPBOTTOM)
         {
            g2.setColor (getForeground());
            g2.fillRect (min - ARROW_SZ, 0, ARROW_SZ - 1, height);
            paint3DRectLighting (g2, min - ARROW_SZ, 0, ARROW_SZ - 1, height);

            paintThumb (g2, min, 0, max - min - 1, height); 

            g2.setColor (getForeground());
            g2.fillRect (max, 0, ARROW_SZ - 1, height);
            paint3DRectLighting (g2, max, 0, ARROW_SZ - 1, height);

            // Draw arrows
            g2.setColor (Color.black);
            paintHandle (g2, min - ARROW_SZ + (ARROW_SZ - ARROW_HEIGHT) / 2.0,
                         (height - ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, true);
            paintHandle (g2, max + (ARROW_SZ - ARROW_HEIGHT) / 2.0,
                         (height - ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, false);
         }
         else
         {
            g2.setColor (getForeground());
            g2.fillRect (min, 0, ARROW_SZ - 1, height);
            paint3DRectLighting (g2, min, 0, ARROW_SZ - 1, height);

            paintThumb (g2, max, 0, min - max - 1, height);

            g2.setColor (getForeground());
            g2.fillRect (max - ARROW_SZ, 0, ARROW_SZ - 1, height);
            paint3DRectLighting (g2, max - ARROW_SZ, 0, ARROW_SZ - 1, height);

            // Draw arrows
            g2.setColor (Color.black);
            paintHandle (g2, min + (ARROW_SZ - ARROW_HEIGHT) / 2.0,
                         (height - ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, true);
            paintHandle (g2, max - ARROW_SZ + (ARROW_SZ - ARROW_HEIGHT) / 2.0,
                         (height - ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, false);
         }
      }
   }

   private void paintThumb (final Graphics2D g2, final int x, final int y,
                            final int w, final int h)
   {
      if (thumbColor != null)
      {
         g2.setColor (thumbColor);
         g2.fillRect (x, y, w, h);
         paint3DRectLighting (g2, x, y, w, h);
      }
   }
   
   protected void paintHandle (final Graphics2D g2, 
                               final double x, final double y, 
                               final int w, final int h,
                               final boolean topDown)
   {
      if (useLabels)
      {
         g2.setFont (FONT);
         String label = topDown ? getLowValue() + "" : getHighValue() + "";
         g2.drawChars (label.toCharArray(), 0, label.length(), (int) x, (int) y + 5);
      }
      else
         paintArrow (g2, x, y, w, h, topDown);
   }
   
   /**
    * This draws an arrow as a series of lines within the specified box. The
    * last boolean specifies whether the point should be at the right/bottom or
    * left/top.
    */
   protected void paintArrow (final Graphics2D g2, 
                              final double x, final double y, 
                              int w, int h,
                              final boolean topDown)
   {
      int intX = (int) (x + 0.5);
      int intY = (int) (y + 0.5);

      if (orientation == VERTICAL)
      {
         if (w % 2 == 0)
            w = w - 1;

         if (topDown)
            for (int i = 0; i < (w / 2 + 1); i++)
               g2.drawLine (intX + i, intY + i, intX + w - i - 1, intY + i);
         else
            for (int i = 0; i < (w / 2 + 1); i++)
               g2.drawLine (intX + w / 2 - i, intY + i, intX + w - w / 2 + i - 1, intY + i);
      }
      else
      {
         if (h % 2 == 0)
            h = h - 1;

         if (topDown)
            for (int i = 0; i < (h / 2 + 1); i++)
               g2.drawLine (intX + i, intY + i, intX + i, intY + h - i - 1);
         else
            for (int i = 0; i < (h / 2 + 1); i++)
               g2.drawLine (intX + i, intY + h / 2 - i, intX + i, intY + h - h / 2 + i - 1);
      }
   }

   /**
    * Adds Windows2K type 3D lighting effects.
    */
   protected void paint3DRectLighting (final Graphics2D g2, final int x, final int y,
                                       final int width, final int height)
   {
      g2.setColor (Color.white);
      g2.drawLine (x + 1, y + 1, x + 1, y + height - 1);
      g2.drawLine (x + 1, y + 1, x + width - 1, y + 1);
      g2.setColor (Color.gray);
      g2.drawLine (x + 1, y + height - 1, x + width - 1, y + height - 1);
      g2.drawLine (x + width - 1, y + 1, x + width - 1, y + height - 1);
      g2.setColor (Color.darkGray);
      g2.drawLine (x, y + height, x + width, y + height);
      g2.drawLine (x + width, y, x + width, y + height);
   }

   /**
    * Converts from screen coordinates to a range value.
    */
   protected int toLocal (final int xOrY)
   {
      Dimension sz = getSize();
      int min = getMinimum();
      double scale;
      if (orientation == VERTICAL)
         scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
      else
         scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum() - min);

      if (direction == LEFTRIGHT_TOPBOTTOM)
         return (int) (((xOrY - ARROW_SZ) / scale) + min + 0.5);

      if (orientation == VERTICAL)
         return (int) ((sz.height - xOrY - ARROW_SZ) / scale + min + 0.5);

      return (int) ((sz.width - xOrY - ARROW_SZ) / scale + min + 0.5);
   }

   /**
    * Converts from a range value to screen coordinates.
    */
   protected int toScreen (final int xOrY)
   {
      Dimension sz = getSize();
      int min = getMinimum();
      double scale;
      if (orientation == VERTICAL)
         scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
      else
         scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum() - min);

      // If the direction is left/right_top/bottom then we subtract the min and
      // multiply times scale
      // Otherwise, we have to invert the number by subtracting the value from
      // the height
      if (direction == LEFTRIGHT_TOPBOTTOM)
         return (int) (ARROW_SZ + ((xOrY - min) * scale) + 0.5);

      if (orientation == VERTICAL)
         return (int) (sz.height - (xOrY - min) * scale - ARROW_SZ + 0.5);

      return (int) (sz.width - (xOrY - min) * scale - ARROW_SZ + 0.5);
   }

   /**
    * Converts from a range value to screen coordinates.
    */
   protected double toScreenDouble (final int xOrY)
   {
      Dimension sz = getSize();
      int min = getMinimum();
      double scale;
      if (orientation == VERTICAL)
         scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum() + 1 - min);
      else
         scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum() + 1 - min);

      // If the direction is left/right_top/bottom then we subtract the min and
      // multiply times scale
      // Otherwise, we have to invert the number by subtracting the value from
      // the height
      if (direction == LEFTRIGHT_TOPBOTTOM)
         return ARROW_SZ + ((xOrY - min) * scale);

      if (orientation == VERTICAL)
         return sz.height - (xOrY - min) * scale - ARROW_SZ;

      return sz.width - (xOrY - min) * scale - ARROW_SZ;
   }

   // ------------------------------------------------------------------------
   // Event Handling

   private void offset (final int dxOrDy)
   {
      model.setValue (model.getValue() + dxOrDy);
   }
   
   enum Pick { None, LeftOrTop, Thumb, RightOrBottom };
   
   class MyMouseListener extends MouseAdapter
   {
      private Pick pick;
      private int pickOffsetLow;
      private int pickOffsetHigh;

      @Override
      public void mousePressed (final MouseEvent e)
      {
         if (orientation == VERTICAL)
         {
            pick = pickHandle (e.getY());
            pickOffsetLow = e.getY() - toScreen (getLowValue());
            pickOffsetHigh = e.getY() - toScreen (getHighValue());
         }
         else
         {
            pick = pickHandle (e.getX());
            pickOffsetLow = e.getX() - toScreen (getLowValue());
            pickOffsetHigh = e.getX() - toScreen (getHighValue());
         }
         repaint();
      }
      
      @Override
      public void mouseDragged (final MouseEvent e)
      {
         requestFocus();
         int value = (orientation == VERTICAL) ? e.getY() : e.getX();
         
         int minimum = getMinimum();
         int maximum = getMaximum();
         int lowValue = getLowValue();
         int highValue = getHighValue();
         
         switch (pick)
         {
         case LeftOrTop:
            int low = toLocal (value - pickOffsetLow);
            
            if (low < minimum)
               low = minimum;
            if (low > maximum - minExtent)
               low = maximum - minExtent;
            if (low > highValue - minExtent)
               setRange (low, low + minExtent);
            else
               setLowValue (low);
            break;
            
         case RightOrBottom:
            int high = toLocal (value - pickOffsetHigh);
            
            if (high < minimum + minExtent)
               high = minimum + minExtent;
            if (high > maximum)
               high = maximum;
            if (high < lowValue + minExtent)
               setRange (high - minExtent, high);
            else
               setHighValue (high);
            break;
            
         case Thumb:
            int dxOrDy = toLocal (value - pickOffsetLow) - lowValue;
            if ((dxOrDy < 0) && ((lowValue + dxOrDy) < minimum))
               dxOrDy = minimum - lowValue;
            if ((dxOrDy > 0) && ((highValue + dxOrDy) > maximum))
               dxOrDy = maximum - highValue;
            if (dxOrDy != 0)
               offset (dxOrDy);
            break;
            
         default:
         }
      }
      
      @Override
      public void mouseReleased (final MouseEvent e)
      {
         pick = Pick.None;
         repaint();
      }
      
      @Override
      public void mouseMoved (final MouseEvent e)
      {
         if (orientation == VERTICAL)
         {
            switch (pickHandle (e.getY()))
            {
            case LeftOrTop:
               setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
               break;
            case RightOrBottom:
               setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
               break;
            case Thumb:
               setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
               break;
            case None:
               setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
               break;
            default:
            }
         }
         else
         {
            switch (pickHandle (e.getX()))
            {
            case LeftOrTop:
               setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
               break;
            case RightOrBottom:
               setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
               break;
            case Thumb:
               setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
               break;
            case None:
               setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));
               break;
            default:
            }
         }
      }
      
      private Pick pickHandle (final int xOrY)
      {
         int min = toScreen (getLowValue());
         int max = toScreen (getHighValue());

         if (direction == LEFTRIGHT_TOPBOTTOM)
         {
            if ((xOrY > (min - ARROW_SZ)) && (xOrY < min))
               return Pick.LeftOrTop;
            else if ((xOrY >= min) && (xOrY <= max))
               return Pick.Thumb;
            else if ((xOrY > max) && (xOrY < (max + ARROW_SZ)))
               return Pick.RightOrBottom;
         }
         else if ((xOrY > min) && (xOrY < (min + ARROW_SZ)))
            return Pick.LeftOrTop;
         else if ((xOrY <= min) && (xOrY >= max))
            return Pick.Thumb;
         else if ((xOrY > (max - ARROW_SZ) && (xOrY < max)))
            return Pick.RightOrBottom;

         return Pick.None;
      }
   }
      
   class MyKeyListener extends KeyAdapter
   {
      @Override
      public void keyPressed (final KeyEvent e)
      {
         int kc = e.getKeyCode();
         boolean v = (orientation == VERTICAL);
         boolean d = (kc == KeyEvent.VK_DOWN);
         boolean u = (kc == KeyEvent.VK_UP);
         boolean l = (kc == KeyEvent.VK_LEFT);
         boolean r = (kc == KeyEvent.VK_RIGHT);
         
         int minimum = getMinimum();
         int maximum = getMaximum();
         int lowValue = getLowValue();
         int highValue = getHighValue();
         int increment = 1;
         
         if (v && r || !v && u)
         {
            if (lowValue - increment >= minimum && highValue + increment <= maximum)
               grow (increment);
         }
         else if (v && l || !v && d)
         {
            if (highValue - lowValue >= 2 * increment)
               grow (-1 * increment);
         }
         else if (v && d || !v && l)
         {
            if (lowValue - increment >= minimum)
               offset (-increment);
         }
         else if (v && u || !v && r)
         {
            if (highValue + increment <= maximum)
               offset (increment);
         }
      }
      
      private void grow (final int increment)
      {
         model.setRangeProperties (model.getValue() - increment, 
                                   model.getExtent() + 2 * increment, 
                                   model.getMinimum(), model.getMaximum(), false);
      }
   }
}
