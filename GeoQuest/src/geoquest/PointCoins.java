package geoquest;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;

import utils.ImageTools;

public final class PointCoins
{
   private static final int DPI = 300;
   private static final int WIDTH = 11 * DPI;
   private static final int HEIGHT = Math.round (8.5f * DPI);
   private static final int COINS_PER_ROW = 10;
   private static final int MARGIN = 50;
   private static final int CELL = (WIDTH - MARGIN) / COINS_PER_ROW;
   private static final int DIAMETER = Math.round (CELL * 0.75f);
   private static final int PAD = CELL - DIAMETER;
   
   private Image image;
   private String[] labels;
   
   private PointCoins()
   {
      image = ImageTools.createImage (WIDTH, HEIGHT);
      labels = new String[] { "1", "2", "2", "3", "4", "5", "FTF" };
   }

   public void exportImage (final String path)
   {
      Graphics2D g = (Graphics2D) image.getGraphics();

      g.setColor (Color.WHITE);
      g.fillRect (0, 0, WIDTH, HEIGHT);
      
      Stroke stroke = new BasicStroke
         (3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, null, 0);
      g.setStroke (stroke);
      
      int x = MARGIN;
      int y = MARGIN;
      int label = 0;
      
      while (y + DIAMETER < HEIGHT)
      {
         while (x + DIAMETER < WIDTH)
         {
            paintCoin (g, x, y, labels [label]);
            x += DIAMETER + PAD;
         }
         x = MARGIN;
         y += DIAMETER + PAD;
         label++;
      }
      
      ImageTools.saveImageAsJpeg (path, image);
   }

   private void paintCoin (final Graphics2D g, final int x, final int y,
                           final String label)
   {
      g.setColor (Color.YELLOW);
      g.fillOval (x, y, DIAMETER, DIAMETER);
      g.setColor (Color.BLACK);
      g.drawOval (x, y, DIAMETER, DIAMETER);

      Font font = fitFont (g, label);
      g.setColor (Color.BLUE);
      g.setFont (font);
      
      FontMetrics fm = g.getFontMetrics (font);
      int w = fm.stringWidth (label);
      int h = fm.getHeight();
      int xOff = x + ((DIAMETER - w) / 2);
      int yOff = y + ((DIAMETER - h) / 2);
      // g.drawRect (xOff, yOff, w, h); // show bounding box around label
      yOff += fm.getAscent();
      g.drawString (label, xOff, yOff);
   }
   
   private Font fitFont (final Graphics2D g, final String label)
   {
      Font font = null;
      
      int targetWidth  = Math.round (DIAMETER * 0.8f);
      int targetHeight = Math.round (DIAMETER * 0.9f);
      int size = 50;
      int width = 0, height = 0;
      while (width < targetWidth && height < targetHeight)
      {
         if (label.equals ("FTF"))
            font = new Font ("Arial Narrow", Font.BOLD, size);
         else
            font = new Font ("Arial Black", Font.BOLD, size);
         FontMetrics fm = g.getFontMetrics (font);
         width = fm.stringWidth (label);
         height = fm.getHeight();
         size += 10;
      }
      
      return font;
   }
   
   public static void main (final String[] args)
   {
      String desk = System.getProperty ("user.home") + "/Desktop";
      String path = desk + "/PointCoins.jpg";
      PointCoins app = new PointCoins();
      app.exportImage (path);
      System.out.println ("Created: " + path);
   }
}
