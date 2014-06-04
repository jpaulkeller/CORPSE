package map;

import file.FileUtils;
import gui.ComponentTools;
import gui.comp.SliderWheel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import map.model.MapModel;
import map.model.Scale;
import utils.PrintUtil;

// TODO
// translucent grid lines (80%)
// multiple layers with toggle button to hide
// plot point icons (so they can overlap) instead of at a tile
// right-click to erase
// better Undo
// pop-up (for eye-dropper?)
// better tool icons
// custom lines: styles/fills, width, randomness, grid-alignment
// text!

// tile sources:
/*
http://www.squareforge.com/pdf-forests.html
http://www.plaintextures.com/grasstextures.php?urlcode=m7irGaPIQWG4Overr3IagiK2zuZVPuEuHiFX4rMascUwg2DVx30XhA8Y125%2BG9h4WAkBqz3%2FD8Hn0Lmc%2FNuoJg%3D%3D
http://cgtextures.com/
https://app.box.com/shared/ljb15vae1b (Dundjinni)
*/

// map ideas
// http://lotro-wiki.com/images/thumb/5/5e/Entwood_map.jpg/400px-Entwood_map.jpg

/*
 How to print:
 - export the map (this will create a JPG file)
 - go to http://www.blockposters.com/
 - upload the image
 - slice the image: select 1-page-wide, Letter, Landscape
 - download the slices (PDF file)
*/

public class MapMaker implements Observer
{
   public static final String IMAGE_ROOT = "D:/pkgs/workspace/personal/geoquest/icons";
   
   private Map map;
   private JFrame frame;
   private JSplitPane palettes;
   private DynamicPalette terrainPalette; // ground
   private DynamicPalette featurePalette; // trees and such
   private DynamicPalette recentPalette; // tiles currently/recently used on the map
   private LayerPanel layerPanel;
   private JPanel mainPanel;
   private JSplitPane split;
   private JProgressBar progress;
   private Menus menus;
   private SliderWheel gridSlider;

   private void buildGUI()
   {
      map = new Map();
      map.setGridSize (Scale.CELLS_PER_GRID * map.getMapPanel().getCellSize());
      map.addObserver (this);

      menus = new Menus (this);

      int minWidth = ((DynamicPalette.getIconSize() + 2) * DynamicPalette.getMinIconsPerRow()) + 40; // icons plus scroller
      
      IconButtonListener iconListener = new IconButtonListener();
      recentPalette = new DynamicPalette(map, "Recent Tiles", IMAGE_ROOT, null, iconListener);
      recentPalette.getPanel().setPreferredSize (new Dimension (minWidth, 100));
            
      featurePalette = new DynamicPalette(null, "Features (top layers)", IMAGE_ROOT, "features", iconListener);
      featurePalette.getPanel().setMinimumSize (new Dimension (minWidth, 120));
      featurePalette.getPanel().setPreferredSize (new Dimension (minWidth, 250));
                  
      terrainPalette = new DynamicPalette(null, "Terrain (bottom layer)", IMAGE_ROOT, "terrain", iconListener);
      terrainPalette.getPanel().setMinimumSize (new Dimension (minWidth, 120));
      terrainPalette.getPanel().setPreferredSize (new Dimension (minWidth, 250));
      
      palettes = new JSplitPane (JSplitPane.VERTICAL_SPLIT, true);
      palettes.add (featurePalette.getPanel(), JSplitPane.TOP);
      palettes.add (terrainPalette.getPanel(), JSplitPane.BOTTOM);

      JPanel iconPanel = new JPanel(new BorderLayout());
      iconPanel.add(recentPalette.getPanel(), BorderLayout.NORTH);
      iconPanel.add(palettes, BorderLayout.CENTER);
      
      layerPanel = new LayerPanel(map);
      
      JPanel left = new JPanel (new BorderLayout());
      left.add (iconPanel, BorderLayout.CENTER);
      left.add (layerPanel, BorderLayout.EAST);

      split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, true);
      split.add (left, JSplitPane.LEFT);
      split.add (map.getView(), JSplitPane.RIGHT);
      
      progress = new JProgressBar (0, 100);
      progress.setFont (new Font ("Arial", Font.BOLD, 14));
      progress.setStringPainted (true);

      JPanel controls = new JPanel (new BorderLayout());
      controls.add (menus.getMenus(), BorderLayout.NORTH);
      controls.add (menus.getTools(), BorderLayout.SOUTH);
      
      mainPanel = new JPanel (new BorderLayout());
      mainPanel.add (controls, BorderLayout.NORTH);
      mainPanel.add (split, BorderLayout.CENTER);
      mainPanel.add (progress, BorderLayout.SOUTH);
   }

   void setText (final String text)
   {
      progress.setString (text);
   }
   
   public void open()
   {
      frame = ComponentTools.open ("Map Maker", null, mainPanel, null);
      palettes.setDividerLocation (0.5); // must be done after the GUI is visible
   }

   public Map getMap()
   {
      return map;
   }
   
   public JFrame getFrame()
   {
      return frame;
   }
   
   public void editProperties()
   {
      int result = map.getProps().open (frame);
      if (result == JOptionPane.OK_OPTION)
      {
         for (String key : map.getProps().keySet())
            System.out.println (key + " = " + map.getProps().get (key)); // TODO
         int newRows = Integer.parseInt (map.getProps().get ("Rows"));
         int newCols = Integer.parseInt (map.getProps().get ("Columns"));
         if (newRows != map.getRowCount() || newCols != map.getColumnCount())
            map.resize (newRows, newCols);
      }
   }
   
   public void newMap()
   {
      if (map.needsSave())
         saveAs(); // offer to save
      int cellsPerMap = Scale.GRID_COUNT * Scale.CELLS_PER_GRID;
      map.create (cellsPerMap, cellsPerMap);
   }
   
   public void load()
   {
      File file = menus.getFile ("Open");
      if (file != null && file.exists())
      {
         map.load (file);
         map.backup (MapModel.BACKUP_0);
         frame.setTitle (FileUtils.getNameWithoutSuffix (file));
         progress.setString ("Map loaded from: " + file);
      }
   }
   
   public void save()
   {
      if (map.getFile() != null)
      {
         map.save();
         progress.setString ("Map saved to: " + map.getFile());
      }
      else
         saveAs();
   }
   
   public void saveAs()
   {
      File file = menus.getFile ("Save");
      if (file != null)
      {
         map.save (file);
         frame.setTitle (FileUtils.getNameWithoutSuffix (file));
         progress.setString ("Map saved as: " + file);
      }
   }
   
   public void adjustGrid()
   {
      if (gridSlider == null)
         makeGridSlider();
      
      int gridSize = map.getGridSize();
      int result = JOptionPane.showOptionDialog
         (frame, gridSlider, "Adjust Grid Size", JOptionPane.OK_CANCEL_OPTION, 
          JOptionPane.PLAIN_MESSAGE, null, null, null);
      if (result != JOptionPane.OK_OPTION)
      {
         map.setGridSize (gridSize);
         map.getMapPanel().repaint();
      }
   }

   private void makeGridSlider()
   {
      gridSlider = new SliderWheel();
      gridSlider.setMinimum (10);
      gridSlider.setMaximum (100);
      gridSlider.setValue (map.getGridSize());
      // gridSlider.setMinorTickSpacing (5);
      gridSlider.setMajorTickSpacing (10);
      gridSlider.setPaintTicks (true);
      gridSlider.setPaintLabels (true);
      gridSlider.setPaintTrack (true);

      gridSlider.addChangeListener (new ChangeListener()
      {
         @Override
         public void stateChanged (final ChangeEvent e)
         {
            // JSlider gridSlider = (JSlider) e.getSource();
            //if (!gridSlider.getValueIsAdjusting())
            map.setGridSize (gridSlider.getValue());
            map.getMapPanel().repaint();
         }
      });
   }
      
   public void print()
   {
      PrintUtil.print (frame, map.getMapPanel());
      progress.setString ("Map sent to the printer");
   }
   
   @Override
   public void update (final Observable o, final Object obj)
   {
      if (progress != null)
      {
         String action = obj.toString();
         // ignore actions that don't start with an uppercase letter
         if (action != null && action.substring(0, 1).equals(action.substring(0, 1).toUpperCase()))
            progress.setString (action);
      }
   }

   class IconButtonListener implements ActionListener
   {
      @Override
      public void actionPerformed (final ActionEvent e)
      {
         PaletteTile tile = (PaletteTile) e.getSource();
         // TODO: consider using top dir as the default layer (by name)
         int defaultLayer = tile.getFile().contains("terrain") ? 0 : 1;
         if (defaultLayer == 0 && map.getDrawLayerIndex() != 0)
            map.setDrawLayerIndex(defaultLayer);
         else if (defaultLayer == 1 && map.getDrawLayerIndex() == 0) // only change if it's the terrain layer
            map.setDrawLayerIndex(defaultLayer);
         
         map.setDrawTile (tile.getTile());
         menus.setDrawIcon (tile.getIcon());
         menus.enableIcon (true);
         
         if (!map.getMode().usesTile())
            menus.setMode (Mode.Draw);
      }
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();

      MapMaker app = new MapMaker();
      app.buildGUI();
      app.open();
   }
}
