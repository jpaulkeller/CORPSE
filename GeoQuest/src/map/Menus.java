package map;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import gui.comp.FileChooser;
import map.model.MapModel;
import map.model.Scale;
import utils.ImageTools;

public class Menus
{
   private static final String NEW = "New";
   private static final String OPEN = "Open";
   private static final String SAVE = "Save";
   private static final String SAVE_AS = "Save As...";
   private static final String EXPORT = "Export";
   private static final String PRINT = "Print";
   private static final String PROPS = "Properties";

   // modes
   private static final String DRAW = "Draw";
   private static final String FILL = "Fill";
   private static final String BOX = "Box";
   private static final String PATH = "Path";
   private static final String STREAM = "Stream";
   private static final String ERASE = "Erase";

   private static final String SHIFT_UP = "Up";
   private static final String SHIFT_DOWN = "Down";
   private static final String SHIFT_LEFT = "Left";
   private static final String SHIFT_RIGHT = "Right";
   private static final String FLIP_H = "Flip (Top/Bottom)";
   private static final String FLIP_V = "Flip (Left/Right)";
   private static final String ROTATE = "Rotate (Clockwise)";
   private static final String EXPAND = "Expand"; // resize? shrink?

   private static final String UNDO = "Undo";
   private static final String REVERT = "Revert All";

   private static final String CLR_STRM = "Clear Streams";
   private static final String CLR_PATH = "Clear Paths";

   private static final String LAYER_ADD = "Add Layer";

   private static final String GRID_NOT = "No Grid";
   private static final String GRID_SQR = "Square Grid";
   private static final String GRID_HEX = "Hex Grid";
   private static final String GRID_SLIDER = "Adjust Grid Size";

   private static final String SCALE_1 = "Full";
   private static final String SCALE_2 = "Half";
   private static final String SCALE_3 = "Third";
   private static final String SCALE_4 = "Quarter";

   private ButtonGroup viewScales;
   private ButtonGroup toolScales;
   private AbstractButton viewScale1, viewScale2, viewScale3, viewScale4;
   private AbstractButton toolScale1, toolScale2, toolScale4;

   private static final String EXIT = "Exit";

   private ActionListener buttonListener = new ButtonListener();
   private Map<String, JComponent> invokers = new HashMap<>();

   private MapMaker app;
   private JMenuBar menus;
   private JToolBar tools;
   private JLabel drawIcon;
   private FileChooser chooser;

   public Menus(final MapMaker app)
   {
      this.app = app;

      menus = makeMenus();
      tools = makeTools();
      chooser = new FileChooser("Maps", "data/maps");
      chooser.setRegexFilter(".*[.]map", "Map files");
   }

   public JMenuBar getMenus()
   {
      enable();
      return menus;
   }

   public JToolBar getTools()
   {
      enable();
      return tools;
   }

   public File getFile(final String approveButtonText)
   {
      File file = null;
      chooser.setApproveButtonText(approveButtonText);
      if (chooser.showOpenDialog(app.getFrame()) == JFileChooser.APPROVE_OPTION)
         file = chooser.getSelectedFile();
      return file;
   }

   public void setDrawIcon(final Icon icon)
   {
      drawIcon.setIcon(icon);
   }

   public void enableIcon(final boolean state)
   {
      drawIcon.setEnabled(state);
   }

   private JMenuBar makeMenus()
   {
      JMenuBar menubar = new JMenuBar();
      menubar.add(makeMapMenu());
      menubar.add(makeEditMenu());
      menubar.add(makeLayerMenu());
      menubar.add(makeViewMenu());
      return menubar;
   }

   private JMenu makeMapMenu()
   {
      JMenu menu = new JMenu("Map");
      menu.setMnemonic('M');

      menu.add(makeMenuItem(NEW, KeyEvent.VK_N, "MapNew.gif", "Create a new map"));
      menu.add(makeMenuItem(OPEN, KeyEvent.VK_O, "MapLoad.gif", "Open a previously saved map"));
      menu.add(makeMenuItem(SAVE, KeyEvent.VK_S, "MapSave.gif", "Save the current map"));
      menu.add(makeMenuItem(SAVE_AS, KeyEvent.VK_A, "MapSaveAs.gif", "Save the current map to a new file"));
      menu.addSeparator();

      menu.add(makeMenuItem(EXPORT, KeyEvent.VK_N, "Camera.gif", "Export the current map as an image"));
      menu.add(makeMenuItem(PRINT, KeyEvent.VK_P, "Printer.gif", "Print the map"));
      menu.addSeparator();

      menu.add(makeMenuItem(PROPS, '-', "Form.gif", "Edit map properties (including size)"));
      menu.addSeparator();

      menu.add(makeMenuItem(EXIT, KeyEvent.VK_E, "Exit.gif", "Exit this application"));
      return menu;
   }

   private JMenu makeEditMenu()
   {
      JMenu menu = new JMenu("Edit");
      menu.setMnemonic('E');

      menu.add(makeMenuItem(FLIP_H, KeyEvent.VK_H, "FlipHorz.gif", "Flip around the horizontal axis"));
      menu.add(makeMenuItem(FLIP_V, KeyEvent.VK_V, "FlipVert.gif", "Flip around the vertical axis"));
      menu.add(makeMenuItem(ROTATE, KeyEvent.VK_R, "RotateCW.gif", "Rotate clockwise"));
      menu.add(makeMenuItem(EXPAND, KeyEvent.VK_E, "Expand.gif", "Expand"));
      menu.addSeparator();

      menu.add(makeMenuItem(SHIFT_UP, KeyEvent.VK_UP, "ArrowUp.gif", "Shift all tiles up"));
      menu.add(makeMenuItem(SHIFT_DOWN, KeyEvent.VK_DOWN, "ArrowDown.gif", "Shift all tiles down"));
      menu.add(makeMenuItem(SHIFT_LEFT, KeyEvent.VK_LEFT, "ArrowLeft.gif", "Shift all tiles left"));
      menu.add(makeMenuItem(SHIFT_RIGHT, KeyEvent.VK_RIGHT, "ArrowRight.gif", "Shift all tiles right"));
      menu.addSeparator();

      menu.add(makeMenuItem(UNDO, KeyEvent.VK_U, "Undo.gif", "Undo recent changes"));
      menu.add(makeMenuItem(REVERT, KeyEvent.VK_A, "UndoAll.gif", "Undo all changes made this session"));
      menu.addSeparator();

      menu.add(makeMenuItem(CLR_PATH, KeyEvent.VK_P, "Path.gif", "Remove all paths from the map"));
      menu.add(makeMenuItem(CLR_STRM, KeyEvent.VK_M, "Stream.gif", "Remove all streams from the map"));

      return menu;
   }

   private JMenu makeLayerMenu()
   {
      JMenu menu = new JMenu("Layer");
      menu.setMnemonic('L');

      menu.add(makeMenuItem(LAYER_ADD, KeyEvent.VK_A, "LayerAdd.gif", "Add a new layer (on top)"));

      return menu;
   }

   private JMenu makeViewMenu()
   {
      JMenu menu = new JMenu("View");
      menu.setMnemonic('V');

      ButtonGroup grids = new ButtonGroup();
      menu.add(makeCheckItem(GRID_NOT, KeyEvent.VK_N, "GridNone.gif", false, grids, "Clear the grid display"));
      menu.add(makeCheckItem(GRID_SQR, KeyEvent.VK_G, "Grid.gif", true, grids, "Show the square grid lines"));
      menu.add(makeCheckItem(GRID_HEX, KeyEvent.VK_H, "GridHex.gif", false, grids, "Show the hexagonal grid lines"));
      menu.add(makeMenuItem(GRID_SLIDER, KeyEvent.VK_S, "GridSlider.gif", "Adjust a slider to change the grid size"));
      menu.addSeparator();

      viewScales = new ButtonGroup();
      viewScale1 = menu.add(makeCheckItem(SCALE_1, KeyEvent.VK_1, "ScaleFull.gif", true, viewScales, "Show map at full scale"));
      viewScale2 = menu.add(makeCheckItem(SCALE_2, KeyEvent.VK_2, "ScaleHalf.gif", false, viewScales, "Show map at 1/2 scale"));
      viewScale3 = menu.add(makeCheckItem(SCALE_3, KeyEvent.VK_3, null, false, viewScales, "Show map at 1/3 scale"));
      viewScale4 = menu.add(makeCheckItem(SCALE_4, KeyEvent.VK_4, "ScaleQuarter.gif", false, viewScales, "Show map at 1/4 scale"));

      return menu;
   }
   
   public void reset()
   {
      // reset to full scale
      AbstractButton button = viewScales.getElements().nextElement();
      viewScales.setSelected(button.getModel(), true);
      button = toolScales.getElements().nextElement();
      toolScales.setSelected(button.getModel(), true);
   }
   
   private JToolBar makeTools()
   {
      tools = new JToolBar();

      tools.add(makeButton(OPEN, "MapLoad.gif", "Open a previously saved map"));
      tools.add(makeButton(SAVE, "MapSave.gif", "Save the current map"));
      tools.addSeparator();

      tools.add(makeButton(UNDO, "Undo.gif", "Undo recent changes"));
      tools.addSeparator();

      toolScales = new ButtonGroup();
      toolScale1 = (AbstractButton) tools.add(makeToggle(SCALE_1, "ScaleFull.gif", true, toolScales, "Show map at full scale"));
      toolScale2 = (AbstractButton) tools.add(makeToggle(SCALE_2, "ScaleHalf.gif", false, toolScales, "Show map at 1/2 scale"));
      toolScale4 = (AbstractButton) tools.add(makeToggle(SCALE_4, "ScaleQuarter.gif", false, toolScales, "Show map at 1/4 scale"));
      tools.addSeparator();

      drawIcon = new JLabel();
      drawIcon.setHorizontalAlignment(JLabel.CENTER);
      drawIcon.setVerticalAlignment(JLabel.CENTER);
      tools.add(drawIcon);

      ButtonGroup modes = new ButtonGroup();
      tools.add(makeToggle(DRAW, "Pencil.gif", true, modes, "Click or drag to file tiles"));
      tools.add(makeToggle(FILL, "Fill.gif", true, modes, "Click to file an area with the current tile"));
      tools.add(makeToggle(BOX, "BoxSelect.gif", false, modes, "Fill rectangular area"));
      tools.add(makeToggle(PATH, "Path.gif", false, modes, "Click or drag to create a path along selected points"));
      tools.add(makeToggle(STREAM, "Stream.gif", false, modes, "Click or drag to create a stream along selected points"));
      tools.add(makeToggle(ERASE, "Eraser.gif", true, modes, "Click or drag to erase features (one layer at a time)"));

      return tools;
   }

   private JMenuItem makeMenuItem(final String label, final int mnemonic, final String iconName, final String tip)
   {
      JMenuItem mi = new JMenuItem(label);
      mi.setMnemonic(mnemonic);
      mi.setToolTipText(tip);
      if (iconName != null)
      {
         ImageIcon icon = ImageTools.getIcon("icons/buttons/" + iconName);
         if (icon != null)
            mi.setIcon(icon);
      }
      mi.addActionListener(buttonListener);
      invokers.put(label, mi);
      return mi;
   }

   private JCheckBoxMenuItem makeCheckItem(final String label, final int mnemonic, final String iconFile, final boolean state,
                                           final ButtonGroup group, final String tipText)
   {
      JCheckBoxMenuItem mi;
      ImageIcon icon = ImageTools.getIcon("icons/buttons/" + iconFile);
      if (icon != null)
         mi = new JCheckBoxMenuItem(label, icon, state);
      else
         mi = new JCheckBoxMenuItem(label, state);
      mi.setHorizontalTextPosition(SwingConstants.RIGHT);
      mi.setMnemonic(mnemonic);
      mi.setActionCommand(label);
      mi.setToolTipText(tipText);
      mi.addActionListener(buttonListener);
      group.add(mi);
      invokers.put(label, mi);
      return mi;
   }

   private JButton makeButton(final String command, final String iconName, final String tip)
   {
      ImageIcon icon = ImageTools.getIcon("icons/buttons/" + iconName);
      JButton button = new JButton(command, icon);
      button.setHorizontalTextPosition(JButton.CENTER);
      button.setVerticalTextPosition(JButton.BOTTOM);
      button.setMargin(new Insets(2, 8, 1, 8));
      button.setFocusable(false);
      button.setToolTipText(tip);
      button.setActionCommand(command);
      button.addActionListener(buttonListener);
      invokers.put(command, button);
      return button;
   }

   private JToggleButton makeToggle(final String command, final String iconName, final boolean on, final ButtonGroup group,
                                    final String tip)
   {
      ImageIcon icon = ImageTools.getIcon("icons/buttons/" + iconName);
      JToggleButton button = new JToggleButton(command, icon, on);
      button.setHorizontalTextPosition(JButton.CENTER);
      button.setVerticalTextPosition(JButton.BOTTOM);
      button.setMargin(new Insets(2, 8, 1, 8));
      button.setFocusable(false);
      button.setToolTipText(tip);
      button.setActionCommand(command);
      button.addActionListener(buttonListener);
      if (group != null)
         group.add(button);
      invokers.put(command, button);
      return button;
   }

   public void setMode(final Mode mode)
   {
      ((AbstractButton) invokers.get(mode.toString())).doClick();
   }

   class ButtonListener implements ActionListener
   {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
         String cmd = e.getActionCommand();
         if (handleMapCommand(cmd))
            return;
         if (handleEditCommand(cmd))
            return;
         if (handleLayerCommand(cmd))
            return;

         if (handleMoveCommand(cmd))
            return;
         if (handleGridCommand(cmd))
            return;
         if (handleScaleCommand(cmd))
            return;
         if (handleModeCommand(cmd))
            return;
         if (cmd.equals(EXIT))
            System.exit(0);

         Toolkit.getDefaultToolkit().beep();
         app.setText("Unsupported command: " + cmd);
      }

      private boolean handleMapCommand(final String cmd)
      {
         if (cmd.equals(NEW))
            app.newMap();
         else if (cmd.equals(OPEN))
            app.load();
         else if (cmd.equals(SAVE))
            app.save();
         else if (cmd.equals(SAVE_AS))
            app.saveAs();
         else if (cmd.equals(EXPORT))
            app.getMap().saveAsImage();
         else if (cmd.equals(PRINT))
            app.print();
         else if (cmd.equals(PROPS))
            app.editProperties();
         else
            return false;
         return true;
      }

      private boolean handleEditCommand(final String cmd)
      {
         if (cmd.equals(UNDO))
            app.getMap().restore(MapModel.BACKUP_1);
         else if (cmd.equals(REVERT))
            app.getMap().restore(MapModel.BACKUP_0);
         else if (cmd.equals(CLR_PATH))
            app.getMap().clearPaths();
         else if (cmd.equals(CLR_STRM))
            app.getMap().clearStreams();
         else
            return false;
         return true;
      }

      private boolean handleLayerCommand(final String cmd)
      {
         if (cmd.equals(LAYER_ADD))
            app.getMap().addLayer();
         else
            return false;
         return true;
      }

      private boolean handleMoveCommand(final String cmd)
      {
         int width = app.getMap().getMapPanel().getMapWidth();
         int height = app.getMap().getMapPanel().getMapHeight();

         if (cmd.equals(SHIFT_UP))
            app.getMap().roll(-1, 0, cmd);
         else if (cmd.equals(SHIFT_DOWN))
            app.getMap().roll(1, 0, cmd);
         else if (cmd.equals(SHIFT_LEFT))
            app.getMap().roll(0, -1, cmd);
         else if (cmd.equals(SHIFT_RIGHT))
            app.getMap().roll(0, 1, cmd);
         else if (cmd.equals(FLIP_H))
            app.getMap().flipTopBottom(height);
         else if (cmd.equals(FLIP_V))
            app.getMap().flipLeftRight(width);
         else if (cmd.equals(ROTATE))
            app.getMap().rotateCW(width);
         else if (cmd.equals(EXPAND))
            app.getMap().resize(app.getMap().getRowCount() + 1, app.getMap().getColumnCount() + 1);
         else
            return false;
         return true;
      }

      private boolean handleGridCommand(final String cmd)
      {
         if (cmd.equals(GRID_NOT))
            app.getMap().showGrid(Grid.None);
         else if (cmd.equals(GRID_SQR))
            app.getMap().showGrid(Grid.Square);
         else if (cmd.equals(GRID_HEX))
            app.getMap().showGrid(Grid.Hex);
         else if (cmd.equals(GRID_SLIDER))
            app.adjustGrid();
         else
            return false;
         return true;
      }

      private boolean handleScaleCommand(final String cmd)
      {
         toolScales.clearSelection();
         viewScales.clearSelection();
         
         if (cmd.equals(SCALE_1))
         {
            app.getMap().getMapPanel().setScale(Scale.Full);
            viewScales.setSelected(viewScale1.getModel(), true);
            toolScales.setSelected(toolScale1.getModel(), true);
         }
         else if (cmd.equals(SCALE_2))
         {
            app.getMap().getMapPanel().setScale(Scale.Half);
            viewScales.setSelected(viewScale2.getModel(), true);
            toolScales.setSelected(toolScale2.getModel(), true);
         }
         else if (cmd.equals(SCALE_3))
         {
            app.getMap().getMapPanel().setScale(Scale.Third);
            viewScales.setSelected(viewScale3.getModel(), true);
            // toolScales.setSelected(toolScale3.getModel(), true);
         }
         else if (cmd.equals(SCALE_4))
         {
            app.getMap().getMapPanel().setScale(Scale.Quarter);
            viewScales.setSelected(viewScale4.getModel(), true);
            toolScales.setSelected(toolScale4.getModel(), true);
         }
         else
            return false;
         
         return true;
      }

      private boolean handleModeCommand(final String cmd)
      {
         Mode mode = null;
         if (cmd.equals(DRAW))
            mode = Mode.Draw;
         else if (cmd.equals(FILL))
            mode = Mode.Fill;
         else if (cmd.equals(BOX))
            mode = Mode.Box;
         else if (cmd.equals(PATH))
            mode = Mode.Path;
         else if (cmd.equals(STREAM))
            mode = Mode.Stream;
         else if (cmd.equals(ERASE))
            mode = Mode.Erase;
         else
            return false;

         app.getMap().setMode(mode);
         enableIcon(mode.usesTile());
         if (mode.usesLine())
            app.setText("Left-click to add a segment; right-click to end the line");
         else if (mode.usesBox())
            app.setText("Drag the mouse to select a rectangular region");
         return true;
      }
   }

   public void enable()
   {
      invokers.get(ROTATE).setEnabled(app.getMap().isSquare());

      // invokers.get (SAVE).setEnabled(); needsSave
      // invokers.get (UNDO).setEnabled(); backup exists
   }
}
