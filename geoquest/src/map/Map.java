package map;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import map.model.MapModel;
import utils.ImageTools;
import file.FileUtils;

public class Map extends MapModel
{
   private MapPanel mapPanel;
   private MapMouseListener listener;
   private JScrollPane scroll;
   
   public Map()
   {
      super (39, 39);
      mapPanel = new MapPanel (this);
      addObserver (mapPanel);
      
      scroll = new JScrollPane (mapPanel);
      scroll.setPreferredSize (new Dimension (1000, 600));
      scroll.getVerticalScrollBar().setUnitIncrement (30);
      
      listener = new MapMouseListener (this, mapPanel); 
      mapPanel.addMouseListener (listener);
      mapPanel.addMouseMotionListener (listener);
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
   public void load (final File file)
   {
      super.load (file);
      mapPanel.repaint();
   }
   
   @Override
   public boolean restore (final String fileName)
   {
      boolean restored = super.restore (fileName);
      if (restored)
         mapPanel.repaint();
      return restored;
   }
   
   public void saveAsImage()
   {
      String path = "data/images/" +
         FileUtils.getNameWithoutSuffix (getFile()) + ".jpg";
      int w = mapPanel.getPreferredSize ().width;
      int h = mapPanel.getPreferredSize ().height;
      Image image = ImageTools.getImage (mapPanel, w, h);
      ImageTools.saveImageAsJpeg (path, image);
      fireChange ("Map exported as: " + path);
   }
   
   public void setMode (final Mode mode)
   {
      mapPanel.setMode (mode);
      mapPanel.setCursor (mode.getCursor());
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
   
   public void showGrid (final Grid grid)
   {
      mapPanel.setGrid (grid);
   }
}
