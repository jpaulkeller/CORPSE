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
import utils.PrintUtil;

// TODO
// multiple layers with toggle button to hide
// right-click to erase
// better Undo
// pop-up (for eye-dropper?)
// better tool icons
// custom lines: styles/fills, width, randomness, grid-alignment
// text!

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
   private DynamicPalette terrainPalette;
   private DynamicPalette featurePalette; // trees and such
   private DynamicPalette usedPalette; // all icons currently used on the map
   private JPanel mainPanel;
   private JSplitPane split;
   private JProgressBar progress;
   private Menus menus;
   private SliderWheel gridSlider;

   private void buildGUI()
   {
      map = new Map();
      map.setGridSize (MapPanel.CELLS_PER_GRID * map.getMapPanel().getCellSize());
      map.addObserver (this);

      menus = new Menus (this);
    
      terrainPalette = new DynamicPalette
         (null, "Terrain (bottom layer)", IMAGE_ROOT, "terrain", new IconButtonListener (0));
      terrainPalette.getPanel().setPreferredSize (new Dimension (200, 200));
      
      usedPalette = new DynamicPalette
         (map, "Features In Use", IMAGE_ROOT, null, new IconButtonListener (1));
      usedPalette.getPanel().setPreferredSize (new Dimension (200, 100));
            
      featurePalette = new DynamicPalette
         (null, "Features (top layer)", IMAGE_ROOT, "features", new IconButtonListener (1));
      featurePalette.getPanel().setPreferredSize (new Dimension (200, 200));
                  
      JPanel featurePanel = new JPanel(new BorderLayout());
      featurePanel.add(usedPalette.getPanel(), BorderLayout.NORTH);
      featurePanel.add(featurePalette.getPanel(), BorderLayout.CENTER);
      
      palettes = new JSplitPane (JSplitPane.VERTICAL_SPLIT, true);
      palettes.add (terrainPalette.getPanel(), JSplitPane.TOP);
      palettes.add (featurePanel, JSplitPane.BOTTOM);
      
      JPanel left = new JPanel (new BorderLayout());
      left.add (palettes, BorderLayout.CENTER);

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
      // must be done after the GUI is visible
      palettes.setDividerLocation (0.35);
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
            map.resize (newRows, newCols, map.getMapPanel().getScale());
      }
   }
   
   public void newMap()
   {
      if (map.needsSave())
         saveAs(); // offer to save
      map.create (39, 39);
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
   public void update (final Observable o, final Object action)
   {
      if (progress != null)
         progress.setString (action.toString());
   }

   class IconButtonListener implements ActionListener
   {
      private int layer;
      
      public IconButtonListener (final int layer)
      {
         this.layer = layer;
      }
      
      @Override
      public void actionPerformed (final ActionEvent e)
      {
         PaletteTile tile = (PaletteTile) e.getSource();
         map.setDrawTile (tile.getTile(), layer);
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
