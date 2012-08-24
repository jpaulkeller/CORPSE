package samples;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.JPanel;

/**
 * <p>
 * <strong>Title:</strong> HeatMap
 * </p>
 * 
 * <p>
 * Description: HeatMap is a JPanel that displays a 2-dimensional array of data
 * using a selected color gradient scheme.
 * </p>
 * <p>
 * For specifying data, the first index into the double[][] array is the x-
 * coordinate, and the second index is the y-coordinate.
 * </p>
 * <p>
 * There are a number of defined gradient types (look at the static fields), but
 * you can create any gradient you like by using either of the following
 * functions:
 * <ul>
 * <li>public static Color[] createMultiGradient(Color[] colors, int numSteps)</li>
 * <li>public static Color[] createGradient(Color one, Color two, int numSteps)</li>
 * </ul>
 * You can then assign an arbitrary Color[] object to the HeatMap as follows:
 * 
 * <pre>
 * heatMap.updateGradient (heatMap.createMultiGradient (new Color[]
 * { Color.red, Color.white, Color.blue }, 256));
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * By default, the graph title, axis titles, and axis tick marks are not
 * displayed. Be sure to set the appropriate title before enabling them.
 * </p>
 * 
 * <hr />
 * <p>
 * <strong>Copyright:</strong> Copyright (c) 2007, 2008
 * </p>
 * 
 * <p>
 * HeatMap is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * </p>
 * 
 * <p>
 * HeatMap is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </p>
 * 
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * HeatMap; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 * </p>
 * 
 * @author Matthew Beckler (matthew@mbeckler.org)
 * @author Josh Hayes-Sheen (grey@grevian.org), Converted to use BufferedImage
 * @version 1.5
 */

public class HeatMap extends JPanel
{
   private double[][] data;
   private Color[][] dataColors;

   // these four variables are used to print the axis labels
   private double xMin;
   private double xMax;
   private double yMin;
   private double yMax;

   private String title;
   private String xAxis;
   private String yAxis;

   private boolean drawTitle = false;
   private boolean drawXTitle = false;
   private boolean drawYTitle = false;
   private boolean drawLegend = false;
   private boolean drawXTicks = false;
   private boolean drawYTicks = false;

   private Color[] colors;
   private Color bg = Color.white;
   private Color fg = Color.black;

   private BufferedImage bufferedImage;
   private Graphics2D bufferedGraphics;

   /**
    * @param data
    *           The data to display, must be a complete array (non-ragged)
    * @param colors
    *           A variable of the type Color[]. See also
    *           {@link #createMultiGradient} and {@link #createGradient}.
    */
   public HeatMap (final double[][] data, final Color[] colors)
   {
      super();

      this.data = data;
      updateGradient (colors);
      updateData (data);

      this.setPreferredSize (new Dimension (60 + data.length, 60 + data[0].length));
      this.setDoubleBuffered (true);

      this.bg = Color.white;
      this.fg = Color.black;

      // this is the expensive function that draws the data plot into a
      // BufferedImage. The data plot is then cheaply drawn to the screen when
      // needed, saving us a lot of time in the end.
      drawData();
   }

   /**
    * Specify the coordinate bounds for the map. Only used for the axis labels,
    * which must be enabled separately. Calls repaint() when finished.
    * 
    * @param xMin The lower bound of x-values, used for axis labels
    * @param xMax The upper bound of x-values, used for axis labels
    */
   public void setCoordinateBounds (final double minX, final double maxX, 
                                    final double minY, final double maxY)
   {
      this.xMin = minX;
      this.xMax = maxX;
      this.yMin = minY;
      this.yMax = maxY;

      repaint();
   }

   /**
    * Specify the coordinate bounds for the X-range. Only used for the axis
    * labels, which must be enabled separately. Calls repaint() when finished.
    * 
    * @param xMin The lower bound of x-values, used for axis labels
    * @param xMax The upper bound of x-values, used for axis labels
    */
   public void setXCoordinateBounds (final double minX, final double maxX)
   {
      this.xMin = minX;
      this.xMax = maxX;
      repaint();
   }

   /**
    * Specify the coordinate bounds for the X Min. Only used for the axis
    * labels, which must be enabled separately. Calls repaint() when finished.
    * 
    * @param xMin The lower bound of x-values, used for axis labels
    */
   public void setXMinCoordinateBounds (final double minX)
   {
      this.xMin = minX;
      repaint();
   }

   /**
    * Specify the coordinate bounds for the X Max. Only used for the axis
    * labels, which must be enabled separately. Calls repaint() when finished.
    * 
    * @param xMax The upper bound of x-values, used for axis labels
    */
   public void setXMaxCoordinateBounds (final double maxX)
   {
      this.xMax = maxX;
      repaint();
   }

   /**
    * Specify the coordinate bounds for the Y-range. Only used for the axis
    * labels, which must be enabled separately. Calls repaint() when finished.
    * 
    * @param yMin The lower bound of y-values, used for axis labels
    * @param yMax The upper bound of y-values, used for axis labels
    */
   public void setYCoordinateBounds (final double minY, final double maxY)
   {
      this.yMin = minY;
      this.yMax = maxY;
      repaint();
   }

   /**
    * Specify the coordinate bounds for the Y Min. Only used for the axis
    * labels, which must be enabled separately. Calls repaint() when finished.
    * 
    * @param yMin The lower bound of Y-values, used for axis labels
    */
   public void setYMinCoordinateBounds (final double minY)
   {
      this.yMin = minY;
      repaint();
   }

   /**
    * Specify the coordinate bounds for the Y Max. Only used for the axis
    * labels, which must be enabled separately. Calls repaint() when finished.
    * 
    * @param yMax The upper bound of y-values, used for axis labels
    */
   public void setYMaxCoordinateBounds (final double maxY)
   {
      this.yMax = maxY;
      repaint();
   }

   /**
    * Updates the title. Calls repaint() when finished.
    * 
    * @param title The new title
    */
   public void setTitle (final String title)
   {
      this.title = title;
      repaint();
   }

   /**
    * Updates the state of the title. Calls repaint() when finished.
    * 
    * @param drawTitle Specifies if the title should be drawn
    */
   public void setDrawTitle (final boolean drawTitle)
   {
      this.drawTitle = drawTitle;
      repaint();
   }

   /**
    * Updates the X-Axis title. Calls repaint() when finished.
    * 
    * @param xAxisTitle The new X-Axis title
    */
   public void setXAxisTitle (final String xAxisTitle)
   {
      this.xAxis = xAxisTitle;
      repaint();
   }

   /**
    * Updates the state of the X-Axis Title. Calls repaint() when finished.
    * 
    * @param drawXAxisTitle Specifies if the X-Axis title should be drawn
    */
   public void setDrawXAxisTitle (final boolean drawXAxisTitle)
   {
      this.drawXTitle = drawXAxisTitle;
      repaint();
   }

   /**
    * Updates the Y-Axis title. Calls repaint() when finished.
    * 
    * @param yAxisTitle The new Y-Axis title
    */
   public void setYAxisTitle (final String yAxisTitle)
   {
      this.yAxis = yAxisTitle;
      repaint();
   }

   /**
    * Updates the state of the Y-Axis Title. Calls repaint() when finished.
    * 
    * @param drawYAxisTitle Specifies if the Y-Axis title should be drawn
    */
   public void setDrawYAxisTitle (final boolean drawYAxisTitle)
   {
      this.drawYTitle = drawYAxisTitle;
      repaint();
   }

   /**
    * Updates the state of the legend. Calls repaint() when finished.
    * 
    * @param drawLegend Specifies if the legend should be drawn
    */
   public void setDrawLegend (final boolean drawLegend)
   {
      this.drawLegend = drawLegend;
      repaint();
   }

   /**
    * Updates the state of the X-Axis ticks. Calls repaint() when finished.
    * 
    * @param drawXTicks Specifies if the X-Axis ticks should be drawn
    */
   public void setDrawXTicks (final boolean drawXTicks)
   {
      this.drawXTicks = drawXTicks;
      repaint();
   }

   /**
    * Updates the state of the Y-Axis ticks. Calls repaint() when finished.
    * 
    * @param drawYTicks Specifies if the Y-Axis ticks should be drawn
    */
   public void setDrawYTicks (final boolean drawYTicks)
   {
      this.drawYTicks = drawYTicks;
      repaint();
   }

   /**
    * Updates the foreground color. Calls repaint() when finished.
    * 
    * @param fgColor Specifies the desired foreground color
    */
   public void setColorForeground (final Color fgColor)
   {
      this.fg = fgColor;
      repaint();
   }

   /**
    * Updates the background color. Calls repaint() when finished.
    * 
    * @param bgColor Specifies the desired background color
    */
   public void setColorBackground (final Color bgColor)
   {
      this.bg = bgColor;
      repaint();
   }

   /**
    * Updates the gradient used to display the data. Calls drawData() and
    * repaint() when finished.
    * 
    * @param gradientColors A variable of type Color[]
    */
   public void updateGradient (final Color[] gradientColors)
   {
      this.colors = gradientColors.clone();
      updateDataColors();
      drawData();
      repaint();
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
      double range = largest - smallest;

      // dataColors is the same size as the data array
      dataColors = new Color[data.length][data[0].length];

      // assign a Color to each data point
      for (int x = 0; x < data.length; x++)
         for (int y = 0; y < data[0].length; y++)
         {
            double norm = (data[x][y] - smallest) / range; // 0 < norm < 1
            int color = (int) Math.floor (norm * (colors.length - 1));
            dataColors[x][y] = colors[color];
         }
   }

   /**
    * This function generates an appropriate data array for display. It uses the
    * function: z = sin(x)*cos(y). The parameter specifies the number of data
    * points in each direction, producing a square matrix.
    * 
    * @param dimension size of each side of the returned array (% 2 != 0)
    * @return double[][] calculated values of z = sin(x)*cos(y)
    */
   public static double[][] generateSinCosData (final int dimension)
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

   /**
    * This function generates an appropriate data array for display. It uses the
    * function: z = Math.cos(Math.abs(sX) + Math.abs(sY)). The parameter
    * specifies the number of data points in each direction, producing a square
    * matrix.
    * 
    * @param dimension
    *           Size of each side of the returned array (% 2 != 0)
    * @return double[][] calculated values of z = Math.cos(Math.abs(sX) +
    *         Math.abs(sY));
    */
   public static double[][] generatePyramidData (final int dimension)
   {
      double[][] data = new double[dimension][dimension];
      double sX, sY; // s for 'Scaled'

      for (int x = 0; x < dimension; x++)
         for (int y = 0; y < dimension; y++)
         {
            sX = 6 * (x / (double) dimension); // 0 < sX < 6
            sY = 6 * (y / (double) dimension); // 0 < sY < 6
            sX = sX - 3; // -3 < sX < 3
            sY = sY - 3; // -3 < sY < 3
            data[x][y] = Math.cos (Math.abs (sX) + Math.abs (sY));
         }

      return data;
   }

   /**
    * Updates the data display, calls drawData() to do the expensive re-drawing
    * of the data plot, and then calls repaint().
    * 
    * @param data
    *           The data to display, must be a complete array (non-ragged)
    */
   public void updateData (final double[][] newData)
   {
      this.data = newData;
      updateDataColors();
      drawData();
      repaint();
   }

   /**
    * Creates a BufferedImage of the actual data plot.
    * 
    * After doing some profiling, it was discovered that 90% of the drawing time
    * was spend drawing the actual data (not on the axes or tick marks). Since
    * the Graphics2D has a drawImage method that can do scaling, we are using
    * that instead of scaling it ourselves. We only need to draw the data into
    * the bufferedImage on startup, or if the data or gradient changes. This
    * saves us an enormous amount of time. Thanks to Josh Hayes-Sheen
    * (grey@grevian.org) for the suggestion and initial code to use the
    * BufferedImage technique.
    * 
    * Since the scaling of the data plot will be handled by the drawImage in
    * paintComponent, we take the easy way out and draw our bufferedImage with 1
    * pixel per data point. Too bad there isn't a setPixel method in the
    * Graphics2D class, it seems a bit silly to fill a rectangle just to set a
    * single pixel...
    * 
    * This function should be called whenever the data or the gradient changes.
    */
   private void drawData()
   {
      bufferedImage = new BufferedImage (data.length, data[0].length,
                                         BufferedImage.TYPE_INT_RGB);
      bufferedGraphics = bufferedImage.createGraphics();

      for (int x = 0; x < data.length; x++)
         for (int y = data[0].length - 1; y >= 0; y--)
         {
            bufferedGraphics.setColor (dataColors[x][y]);
            bufferedGraphics.fillRect (x, data[0].length - y - 1, 1, 1);
         }
   }

   /**
    * The overridden painting method, now optimized to simply draw the data plot
    * to the screen, letting the drawImage method do the resizing. This saves an
    * extreme amount of time.
    */
   @Override
   public void paintComponent (final Graphics g)
   {
      super.paintComponent (g);
      Graphics2D g2d = (Graphics2D) g;

      int width = this.getWidth();
      int height = this.getHeight();

      this.setOpaque (true);

      // clear the panel
      g2d.setColor (bg);
      g2d.fillRect (0, 0, width, height);

      // draw the heat map
      if (bufferedImage == null)
      {
         // Ideally, we only call drawData in the constructor, or if we
         // change the data or gradients. We include this just to be safe.
         drawData();
      }

      // The data plot itself is drawn with 1 pixel per data point, and the
      // drawImage method scales that up to fit our current window size. This
      // is very fast, and is much faster than the previous version, which
      // redrew the data plot each time we had to repaint the screen.
      g2d.drawImage (bufferedImage, 31, 31, width - 30, height - 30, 0, 0, 
                     bufferedImage.getWidth(), bufferedImage.getHeight(), null);

      // border
      g2d.setColor (fg);
      g2d.drawRect (30, 30, width - 60, height - 60);

      // title
      if (drawTitle && title != null)
         g2d.drawString (title, (width / 2) - 4 * title.length(), 20);

      // axis ticks - ticks start even with the bottom left coner, end very
      // close to end of line (might not be right on)
      int numXTicks = (width - 60) / 50;
      int numYTicks = (height - 60) / 50;

      String label = "";
      DecimalFormat df = new DecimalFormat ("##.##");

      // Y-Axis ticks
      if (drawYTicks)
      {
         int yDist = (int) ((height - 60) / (double) numYTicks); // distance between ticks
         for (int y = 0; y <= numYTicks; y++)
         {
            g2d.drawLine (26, height - 30 - y * yDist, 30, height - 30 - y * yDist);
            label = df.format (((y / (double) numYTicks) * (yMax - yMin)) + yMin);
            int labelY = height - 30 - y * yDist - 4 * label.length();
            // to get the text to fit nicely, we need to rotate the graphics
            g2d.rotate (Math.PI / 2);
            g2d.drawString (label, labelY, -14);
            g2d.rotate (-Math.PI / 2);
         }
      }

      // Y-Axis title
      if (drawYTitle && yAxis != null)
      {
         // to get the text to fit nicely, we need to rotate the graphics
         g2d.rotate (Math.PI / 2);
         g2d.drawString (yAxis, (height / 2) - 4 * yAxis.length(), -3);
         g2d.rotate (-Math.PI / 2);
      }

      // X-Axis ticks
      if (drawXTicks)
      {
         int xDist = (int) ((width - 60) / (double) numXTicks); // distance between ticks
         for (int x = 0; x <= numXTicks; x++)
         {
            g2d.drawLine (30 + x * xDist, height - 30, 30 + x * xDist, height - 26);
            label = df.format (((x / (double) numXTicks) * (xMax - xMin)) + xMin);
            int labelX = (31 + x * xDist) - 4 * label.length();
            g2d.drawString (label, labelX, height - 14);
         }
      }

      // X-Axis title
      if (drawXTitle && xAxis != null)
         g2d.drawString (xAxis, (width / 2) - 4 * xAxis.length(), height - 3);

      // Legend
      if (drawLegend)
      {
         g2d.drawRect (width - 20, 30, 10, height - 60);
         for (int y = 0; y < height - 61; y++)
         {
            int yStart = height - 31 - 
               (int) Math.ceil (y * ((height - 60) / (colors.length * 1.0)));
            yStart = height - 31 - y;
            g2d.setColor (colors[(int) ((y / (double) (height - 60)) * (colors.length * 1.0))]);
            g2d.fillRect (width - 19, yStart, 9, 1);
         }
      }
   }
}
