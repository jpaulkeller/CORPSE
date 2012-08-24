package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * VFillLayout is similar to FlowLayout except it lays out components
 * vertically. */

public class VFillLayout extends FlowLayout
{
   private static final long serialVersionUID = 3;

   int gap;

   public VFillLayout()
   {
      this (FlowLayout.LEADING);
   }

   /**
    * Construct a new VFillLayout.
    * @param gap the gap in pixels between each component
    */

   public VFillLayout (int gap)
   {
      setGap (gap);
   }

   /**
    * @param gap the gap in pixels between each component */

   public void setGap (int gap)
   {
      this.gap = gap;
   }
   
   /**
    * Returns the preferred dimensions given the components
    * in the target container.
    * @param target the component to lay out
    */

   @Override
   public Dimension preferredLayoutSize (Container target)
   {
      Dimension dim = new Dimension(0, 0);
      int count = target.getComponentCount();

      for (int i = 0; i < count; i++)
      {
         Component m = target.getComponent(i);
         if (m.isVisible())
         {
            Dimension d = m.getPreferredSize();
            dim.width = Math.max (dim.width, d.width);
            dim.height += d.height;
            if (i > 0)
               dim.height += gap;
         }
      }

      Insets insets = target.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;

      return (dim);
   }

   /**
    * Returns the minimum size needed to layout the target container
    * @param target the component to lay out 
    */
   @Override
   public Dimension minimumLayoutSize (Container target)
   {
      Dimension dim = new Dimension(0, 0);
      int count = target.getComponentCount();

      for (int i = 0; i < count; i++)
      {
         Component m = target.getComponent(i);
         if (m.isVisible())
         {
            Dimension d = m.getMinimumSize();
            dim.width = Math.max (dim.width, d.width);
            dim.height += d.height;
            if (i > 0)
               dim.height += gap;
         }
      }

      Insets insets = target.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;

      return (dim);
   }

   /**
    * Lays out the container. 
    * @param target the container to lay out.
    */
   @Override
   public void layoutContainer (Container target)
   {
      Dimension available = target.getSize();
      Dimension required = preferredLayoutSize (target);
      Insets insets = target.getInsets();
      final int x = insets.left;
      int y = insets.top;
      final int w = Math.max (available.width, required.width);
      final int excessHeight = available.height - required.height;

      int count = target.getComponentCount();
      for (int i = 0; i < count; i++)
      {
         Component comp = target.getComponent (i);
         if (comp.isVisible())
         {
            int h = comp.getPreferredSize().height;
            if (excessHeight > 0)
               h += (h * excessHeight / required.height); // stretch to fill
            comp.setBounds (x, y, w, h);
            y += (h + gap);
         }
      }
   }

   public static void main (String[] args)
   {
      JPanel panel = new JPanel();
      VFillLayout layout = new VFillLayout();
      panel.setLayout (layout);
		
      JButton label1 = new JButton ("normal");
      JButton label2 = new JButton ("double");
      JButton label3 = new JButton ("normal");
      JButton label4 = new JButton ("normal");

      Dimension d = label2.getPreferredSize();
      d.height *= 2;
      label2.setPreferredSize (d);

      panel.add (label1);
      panel.add (label2);
      panel.add (label3);
      panel.add (label4);
      
      final JFrame frame = new JFrame ("VFillLayout");
      frame.getContentPane().add (panel, BorderLayout.CENTER);
      
      // do the following on the GUI event-dispatching thread
      SwingUtilities.invokeLater (new Runnable() {
         public void run()
         {
            frame.pack();
            ComponentTools.centerComponent (frame);
            frame.setVisible (true);
         }
      });
   }
}
