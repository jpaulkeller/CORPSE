package gui.comp;

import gui.ComponentTools;
import gui.Gradient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.JLabel;

import utils.ImageTools;

/**
 * A JPanel that displays a 2-dimensional array of data
 * using a selected color gradient scheme.
 *
 * For specifying data, the first index into the double[][] array is the x-
 * coordinate, and the second index is the y-coordinate.
 * 
 * From Matthew Beckler (matthew.beckler@gmail.com)
 */

public class HeatMapPanel extends ImagePanel
{
   private static final Color CLEAR = new Color (0, 0, 0, 0);
   
   private double[][] data;
   private boolean positiveOnly;
   private Color[] colors;
   private Color[][] dataColors;

   private BufferedImage bufferedImage;
   private Graphics2D bufferedGraphics;

   /**
    * @param data The data to display, must be a complete array (non-ragged)
    */
   public HeatMapPanel (final double[][] data, final Color[] colors,
                        final boolean positiveOnly) // if true, data <= 0 will be clear
   {
      this.positiveOnly = positiveOnly;
      setDoubleBuffered (true);
      setGradient (colors);
      setData (data);
   }

   public double[][] getData()
   {
      return data;
   }
   
   /**
    * Updates the data display, calls drawData() to do the expensive re-drawing
    * of the data plot, and then calls repaint().
    * 
    * @param data The data to display, must be a complete array (non-ragged)
    */
   public void setData (final double[][] data)
   {
      this.data = data;
      if (data != null)
      {
         setPreferredSize (new Dimension (data.length, data[0].length));
         update();
      }
   }

   public void clear()
   {
      for (int i = 0; i < data.length; i++)
         Arrays.fill (data[i], 0);
   }

   /**
    * Updates the gradient used to display the data. Calls drawData() and
    * repaint() when finished.
    */
   public void setGradient (final Color[] gradientColors)
   {
      this.colors = gradientColors;
      update();
   }

   private void update()
   {
      if (data != null && colors != null)
      {
         updateDataColors();
         drawData();
         repaint();
      }
   }

   /**
    * This uses the current array of colors that make up the gradient, and
    * assigns a color to each data point, stored in the dataColors array, which
    * is used by the drawData() method to plot the points.
    */
   private void updateDataColors()
   {
      // We need to find the range of the data values,
      // in order to assign proper colors.
      double largest = Double.MIN_VALUE;
      double smallest = Double.MAX_VALUE;
      for (int x = 0; x < data.length; x++)
         for (int y = 0; y < data[0].length; y++)
         {
            largest = Math.max (data[x][y], largest);
            smallest = Math.min (data[x][y], smallest);
         }
      if (positiveOnly && smallest < 0)
         smallest = 0;
      double range = largest - smallest;

      // dataColors is the same size as the data array
      dataColors = new Color[data.length][data[0].length];

      // assign a Color to each data point
      for (int x = 0; x < data.length; x++)
         for (int y = 0; y < data[0].length; y++)
         {
            if (positiveOnly && data[x][y] <= 0)
               dataColors[x][y] = CLEAR;
            else
            {
               double norm = (data[x][y] - smallest) / range; // 0 < norm < 1
               int color = (int) Math.floor (norm * (colors.length - 1));
               dataColors[x][y] = colors[color];
            }
         }
   }

   /**
    * Creates a BufferedImage of the actual data plot.
    * 
    * Since the scaling of the data plot will be handled by the drawImage in
    * paintComponent, we take the easy way out and draw our bufferedImage with 1
    * pixel per data point.
    * 
    * This function should be called whenever the data or the gradient changes.
    */
   private void drawData()
   {
      bufferedImage = new BufferedImage (data.length, data[0].length,
                                         BufferedImage.TYPE_INT_ARGB);
      bufferedGraphics = bufferedImage.createGraphics();

      for (int x = 0; x < data.length; x++)
         for (int y = 0; y < data[0].length; y++)
         {
            bufferedGraphics.setColor (dataColors[x][y]);
            bufferedGraphics.fillRect (x, y, 1, 1);
         }
      setOverlay (bufferedImage);
   }

   /**
    * This function generates an appropriate data array for display. It uses the
    * function: z = sin(x)*cos(y). The parameter specifies the number of data
    * points in each direction, producing a square matrix.
    * 
    * @param dimension Size of each side of the returned array
    * @return double[][] calculated values of z = sin(x)*cos(y)
    */
   private static double[][] generateSinCosData (final int dimension)
   {
      double[][] data = new double[dimension][dimension];
      double sX, sY; // s for 'Scaled'

      for (int x = 0; x < dimension; x++)
         for (int y = 0; y < dimension; y++)
         {
            sX = 2 * Math.PI * (x / (double) dimension); // 0 < sX < 2 * Pi
            sY = 2 * Math.PI * (y / (double) dimension); // 0 < sY < 2 * Pi
            data[x][y] = Math.sin (sX) * Math.cos (sY);
         }

      return data;
   }
   
   public static void main (final String[] args)
   {
      String docs = "C:/Documents and Settings/jkeller/My Documents/";
      String bg = docs + "My Pictures/BAT/BAT Kit.png";
      
      int alpha = 128;
      Color[] gradient = Gradient.createMultiGradient 
      (new Color[] {
               new Color (0, 0, 255, alpha), // blue
               new Color (0, 255, 0, alpha), // green
               new Color (255, 255, 0, alpha), // yellow
               new Color (255, 200, 0, alpha), // orange
               new Color (255, 0, 0, alpha) // red
      }, 500);      

      HeatMapPanel panel = new HeatMapPanel (null, gradient, true);
      panel.add (new JLabel (ImageTools.getIcon (bg)));
      double[][] data = generateSinCosData (201);
      panel.setData (data);
      
      ComponentTools.open (panel, HeatMapPanel.class.getName());
   }
   
   /* sample use from SessionViewer:

   private void updateHeatMap (final GazeTrack track)
   {
      if (track != null && !track.isEmpty())
      {
         GazeTrack scaled = getTrackScaled (track);
         int w = heatPanel.getWidth();
         int h = heatPanel.getHeight();
         int dist = 35;
         double[][] data = heatPanel.getData();
         if (data == null || data.length != w || data[0].length != h)
            data = new double[w][h];
         else
            heatPanel.clear();

         for (GazePoint point : scaled)
         {
            int x = point.getX();
            int y = point.getY();
            if (x < w && y < h)
               for (int i = Math.max (0, x - dist); i <= Math.min (w - 1, x + dist); i++)
                  for (int j = Math.max (0, y - dist); j <= Math.min (h - 1, y + dist); j++)
                     data[i][j] += Math.max (0, dist - point.getPoint().distance (new Point (i, j)));
         }
         
         heatPanel.setData (data);
      }
   }
   */   
}
