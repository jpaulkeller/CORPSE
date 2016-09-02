package map;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import file.FileUtils;
import map.model.MapModel;
import utils.ImageTools;

public class Map extends MapModel
{
   private MapPanel mapPanel;
   private MapMouseListener listener;
   private JScrollPane scroll;

   public Map()
   {
      // super(Scale.GRID_COUNT * Scale.CELLS_PER_GRID, Scale.GRID_COUNT * Scale.CELLS_PER_GRID);
      super(45, 39); // add a few rows to avoid the blockposter.com watermark
      mapPanel = new MapPanel(this);
      addObserver(mapPanel);

      scroll = new JScrollPane(mapPanel);
      scroll.setPreferredSize(new Dimension(1000, 600));
      scroll.getVerticalScrollBar().setUnitIncrement(30);

      listener = new MapMouseListener(this, mapPanel);
      mapPanel.addMouseListener(listener);
      mapPanel.addMouseMotionListener(listener);
   }

   public JComponent getView()
   {
      return scroll;
   }

   public MapPanel getMapPanel()
   {
      return mapPanel;
   }

   @Override
   public void load(final File file)
   {
      super.load(file);
      mapPanel.repaint();
   }

   @Override
   public boolean restore(final String fileName)
   {
      boolean restored = super.restore(fileName);
      if (restored)
         mapPanel.repaint();
      return restored;
   }

   public void saveAsImage()
   {
      // export to JPEG (other formats can be exported, but blockposters.com only supports JPEG)
      String path = "data/images/" + FileUtils.getNameWithoutSuffix(getFile()) + ".jpg";
      Dimension dim = mapPanel.getPreferredSize();
      BufferedImage image = ImageTools.getImage(mapPanel, dim.width, dim.height);
      ImageTools.saveImage(path, image);
      fireChange("Map exported as: " + path);
   }

   public void setMode(final Mode mode)
   {
      mapPanel.setMode(mode);
      mapPanel.setCursor(mode.getCursor());
   }

   public Mode getMode()
   {
      return mapPanel.getMode();
   }

   @Override
   public void clearStreams()
   {
      super.clearStreams();
      mapPanel.repaint();
   }

   @Override
   public void clearPaths()
   {
      super.clearPaths();
      mapPanel.repaint();
   }

   public void showGrid(final Grid grid)
   {
      mapPanel.setGrid(grid);
   }
}
