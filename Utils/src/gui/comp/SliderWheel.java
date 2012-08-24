package gui.comp;

import gui.ComponentTools;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** Extends JSlider to provide support for the mouse wheel. */

public class SliderWheel extends JSlider implements MouseWheelListener
{
   private static final long serialVersionUID = 5;

   private int increment = 1;

   /**
    * Creates a horizontal slider with the range 0 to 100 and an
    * initial value of 50.
    */
   public SliderWheel()
   {
      super();
      addMouseWheelListener (this);
   }

   /**
    * Creates a slider using the specified orientation with the range
    * 0 to 100 and an initial value of 50.
    */
   public SliderWheel (final int orientation)
   {
      super (orientation);
      addMouseWheelListener (this);
   }

   /**
    * Creates a horizontal slider using the specified min and max with
    * an initial value equal to the average of the min plus max.
    */
   public SliderWheel (final int min, final int max)
   {
      super (min, max);
      addMouseWheelListener (this);
   }

   /**
    * Creates a horizontal slider using the specified min, max and value.
    */
   public SliderWheel (final int min, final int max, final int value)
   {
      super (min, max, value);
      addMouseWheelListener (this);
   }

   /**
    * Creates a slider with the specified orientation and the
    * specified minimum, maximum, and initial values.
    *
    * @exception IllegalArgumentException if orientation is not one of
    * VERTICAL, HORIZONTAL
    *
    * @see javax.swing.JSlider#setOrientation
    * @see javax.swing.JSlider#setMinimum
    * @see javax.swing.JSlider#setMaximum
    * @see javax.swing.JSlider#setValue
    */
   public SliderWheel (final int orientation, 
                       final int min, final int max,
                       final int value)
   {
      super (orientation, min, max, value);
      addMouseWheelListener (this);
   }

   /** Creates a horizontal slider using the specified BoundedRangeModel. */

   public SliderWheel (final BoundedRangeModel brm)
   {
      super (brm);
      addMouseWheelListener (this);
   }

   public void setIncrement (final int increment)
   {
      this.increment = increment;
   }

   public int getIncrement()
   {
      return increment;
   }

   public void mouseWheelMoved (final MouseWheelEvent e)
   {
      int currentValue = getValue();
      int newValue = currentValue + (increment * e.getWheelRotation());
      setValue (newValue);
   }

   public static void main (final String[] args)
   {
      SliderWheel slider = new SliderWheel();
      slider.setMinimum (0);
      slider.setMaximum (100);
      slider.setValue (50);
      // slider.setOrientation (JSlider.VERTICAL);
      slider.setMinorTickSpacing (5);
      slider.setMajorTickSpacing (25);
      slider.setPaintTicks (true);
      slider.setPaintLabels (true);
      slider.setPaintTrack (true);
      // slider.setPreferredSize (new Dimension (180, 45));

      slider.addChangeListener (new ChangeListener()
         {
            public void stateChanged (final ChangeEvent e)
            {
               JSlider jSlider = (JSlider) e.getSource();
               if (!jSlider.getValueIsAdjusting())
                  System.out.println (jSlider.getValue() + "");
            }
         });

      ComponentTools.open (slider, "SliderWheel");
   }
}
