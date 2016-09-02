package map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import javax.swing.JPanel;

import map.model.Cell;
import map.model.Layer;
import map.model.MapModel;
import map.model.Scale;
import map.model.Tile;

public class MapPanel extends JPanel implements Observer
{
   private static final long serialVersionUID = 1L;

   private static Stroke pathPoint = getStroke(3);
   private static Component observer = null;

   private MapModel model;
   private Scale scale;
   private int outerWidth; // for paths and streams

   private Mode mode = Mode.Draw;
   private Grid grid = Grid.Square;

   private Point anchorPt;
   private Point cursorPt = new Point(-1, -1);

   public MapPanel(final MapModel model)
   {
      this.model = model;
      this.scale = Scale.Full;
      configure();
   }

   private void configure()
   {
      int w = model.getColumnCount() * scale.getCellSize();
      int h = model.getRowCount() * scale.getCellSize();
      setSize(w, h);
      setPreferredSize(new Dimension(w, h));

      outerWidth = Math.round(scale.getCellSize() * 0.4f);
      int innerWidth = Math.round(outerWidth * 0.6f);

      model.getStreams().setOuterStroke(getStroke(outerWidth));
      model.getStreams().setInnerStroke(getStroke(innerWidth));
      model.getStreams().setOuterColor(new Color(0x00, 0x3F, 0x87)); // dark blue
      model.getStreams().setInnerColor(new Color(0x00, 0x7F, 0xFF)); // blue

      model.getPaths().setOuterStroke(getStroke(outerWidth));
      model.getPaths().setInnerStroke(getStroke(innerWidth));
      model.getPaths().setOuterColor(new Color(0x8B, 0x45, 0x13)); // dark brown
      model.getPaths().setInnerColor(new Color(0xD9, 0x87, 0x19)); // brown
   }

   private static Stroke getStroke(final int width)
   {
      return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, null, 0);
   }

   public void setScale(final Scale scale)
   {
      this.scale = scale;
      model.setScale(scale);
      configure();
      repaint();
   }

   public Scale getScale()
   {
      return scale;
   }

   public int getCellSize()
   {
      return scale.getCellSize();
   }

   public int getMapWidth()
   {
      return model.getColumnCount() * scale.getCellSize();
   }

   public int getMapHeight()
   {
      return model.getRowCount() * scale.getCellSize();
   }

   public int getLineWidth()
   {
      return outerWidth;
   }

   public void setMode(final Mode mode)
   {
      this.mode = mode;
      anchorPt = null;
   }

   public Mode getMode()
   {
      return mode;
   }

   public void setCursorPoint(final Point p)
   {
      cursorPt = p;
   }

   public Point getCursorPoint()
   {
      return cursorPt;
   }

   public void setAnchor(final Point p)
   {
      anchorPt = p;
   }

   public Point getAnchor()
   {
      return anchorPt;
   }

   public Cell getCell(final Point p)
   {
      int col = Math.min(p.x / getCellSize(), model.getColumnCount() - 1);
      int row = Math.min(p.y / getCellSize(), model.getRowCount() - 1);
      return new Cell(row, col);
   }

   public void setGrid(final Grid grid)
   {
      this.grid = grid;
      repaint();
   }

   @Override
   public void update(final Observable o, final Object action)
   {
      configure();
      repaint();
   }

   @Override
   public void paint(final Graphics g)
   {
      g.clearRect(0, 0, getWidth(), getHeight());
      int maxX = getPreferredSize().width;
      int maxY = getPreferredSize().height;
      ((Graphics2D) g).clip(new Rectangle(maxX, maxY));

      if (!model.getLayers().isEmpty())
         paintLayer((Graphics2D) g, model.getLayers().get(0), 0); // terrain layer

      paintLines((Graphics2D) g);

      if (model.getLayers().size() > 1)
         for (int i = 1; i < model.getLayers().size(); i++)
            paintLayer((Graphics2D) g, model.getLayers().get(i), i);

      paintGrid(g);

      if (cursorPt != null)
         paintCursor(g);
   }

   private void paintCursor(final Graphics g)
   {
      if (mode.usesTile()) // draw, fill, box
         paintCell((Graphics2D) g, model.getDrawTile(), getCell(cursorPt), -1);
      else if (mode.usesLine())
         paintPoint((Graphics2D) g);

      if (mode.usesTile() || mode.usesBox())
      {
         Point from = cursorPt;
         if (mode.usesBox() && anchorPt != null)
            from = anchorPt;
         paintRubberBox((Graphics2D) g, getCell(from), mode.getColor());
      }
   }

   private void paintPoint(final Graphics2D g)
   {
      int x = cursorPt.x - (outerWidth / 2);
      int y = cursorPt.y - (outerWidth / 2);

      Stroke oldStroke = g.getStroke();
      g.setStroke(pathPoint);
      g.setColor(mode.getColor());
      g.drawOval(x, y, outerWidth, outerWidth);
      g.setStroke(oldStroke);
   }

   private void paintRubberBox(final Graphics2D g, final Cell from, final Color color)
   {
      Cell cursorCell = getCell(cursorPt);
      int x = Math.min(from.getCol(), cursorCell.getCol()) * scale.getCellSize();
      int y = Math.min(from.getRow(), cursorCell.getRow()) * scale.getCellSize();
      int w = (Math.abs(from.getCol() - cursorCell.getCol()) + 1) * scale.getCellSize();
      int h = (Math.abs(from.getRow() - cursorCell.getRow()) + 1) * scale.getCellSize();

      Stroke oldStroke = g.getStroke();
      g.setStroke(pathPoint);
      g.setColor(color);
      g.drawRect(x, y, w, h);
      g.setStroke(oldStroke);
   }

   private void paintLayer(final Graphics2D g, final Layer layer, final int depth)
   {
      for (int row = 0; row < model.getRowCount(); row++)
         for (int col = 0; col < model.getColumnCount(); col++)
         {
            Tile tile = layer.getValue(row, col);
            paintTile(g, tile, row, col, depth);
         }
   }

   private void paintCell(final Graphics2D g, final Tile tile, final Cell cell, final int depth)
   {
      paintTile(g, tile, cell.getRow(), cell.getCol(), depth);
   }

   private final Random random = new Random();

   private void paintTile(final Graphics2D g, final Tile tile, final int row, final int col, final int depth)
   {
      int size = scale.getCellSize();
      int dx1 = col * size;
      int dy1 = row * size;

      if (depth == 0)
         g.clearRect(dx1, dy1, size, size);

      if (tile != null)
      {
         Image image = tile.getImage(size);
         if (image != null)
         {
            int sx2 = image.getWidth(observer);
            int sy2 = image.getHeight(observer);
            BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            ((Graphics2D) bi.getGraphics()).drawImage(image, 0, 0, sx2, sy2, observer);
            if (depth == 2) // TODO
               bi = blend(bi, size);
            else if (depth > 0) // offset the icon position randomly (a little bit, for variety)
            {
               int maxOffset = size / 2;
               int half = maxOffset / 2;
               random.setSeed((row + 1) * (col + 1)); // use a seed which is constant for the icon
               random.nextFloat(); // skip the first one, it's not very random
               dx1 += Math.round(random.nextFloat() * maxOffset - half);
               dy1 += Math.round(random.nextFloat() * maxOffset - half);
            }
            int dx2 = dx1 + size;
            int dy2 = dy1 + size;
            g.drawImage(bi, dx1, dy1, dx2, dy2, 0, 0, size, size, observer);
         }
      }
   }

   // blend 2 terrain tiles so there's a smooth transition
   // https://ryzomcore.atlassian.net/wiki/display/RC/Landscape+Texture+Constraints

   public static BufferedImage blend(final BufferedImage srcBuf, final int size)
   {
      int[] srcRGB = srcBuf.getRGB(0, 0, size, size, null, 0, size);
      int[] srcA = srcBuf.getAlphaRaster().getPixels(0, 0, size, size, new int[size * size]);
      int[] dstA = new int[srcRGB.length];

      for (int i = 0; i < srcRGB.length; i++)
         if (srcA[i] > 128)
            dstA[i] = 128;

      BufferedImage dstBuf = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
      dstBuf.setRGB(0, 0, size, size, srcRGB, 0, size);
      dstBuf.getAlphaRaster().setPixels(0, 0, size, size, dstA);

      return dstBuf;
   }

   private void paintLines(final Graphics2D g)
   {
      Stroke oldStroke = g.getStroke();
      Composite oldComp = g.getComposite();
      // Graphics2D g2d = (Graphics2D) g.create(); // temporary copy
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f)); // translucent
      model.getStreams().paintLines(g, scale, anchorPt, cursorPt, mode == Mode.Stream);
      model.getPaths().paintLines(g, scale, anchorPt, cursorPt, mode == Mode.Path);
      g.setComposite(oldComp);
      g.setStroke(oldStroke);
      // g2d.dispose();
   }

   private void paintGrid(final Graphics g)
   {
      int maxX = getPreferredSize().width;
      int maxY = getPreferredSize().height;

      int gridSize = model.getGridSize() / scale.getFactor();
      if (grid == Grid.Square)
         Grid.paintGrid(g, gridSize, maxX, maxY);
      else if (grid == Grid.Hex)
         Grid.paintGridHex(g, gridSize, maxX, maxY);
   }
}
