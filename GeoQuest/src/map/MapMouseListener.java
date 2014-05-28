package map;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import map.model.Cell;
import map.model.Layer;
import map.model.Line;
import map.model.MapModel;
import map.model.Tile;

public class MapMouseListener extends MouseAdapter
{
   private MapModel model;
   private MapPanel view;

   public MapMouseListener (final MapModel model, final MapPanel mapPanel)
   {
      this.model = model;
      this.view = mapPanel;
   }
   
   @Override
   public void mouseMoved (final MouseEvent e)
   {
      determineCell (e);

      if (view.getMode().usesTile() && model.getDrawTile() != null) // draw, fill, box
         view.repaint();
      else if (view.getMode().usesLine())
         view.repaint();
      else if (view.getMode() == Mode.Erase)
         view.repaint();
   }

   @Override
   public void mouseDragged (final MouseEvent e)
   {
      determineCell (e);
      if (view.getMode() == Mode.Draw && model.getDrawTile() != null)
         model.drawTile (view.getCell (view.getCursorPoint()));
      view.repaint();
   }

   @Override
   public void mouseEntered (final MouseEvent e)
   {
      model.backup (MapModel.BACKUP_1);
   }
   
   @Override
   public void mouseExited (final MouseEvent e)
   {
      if (!isLeftClick (e))
      {
         view.setCursorPoint (null);
         view.repaint();
      }
   }

   @Override
   public void mousePressed (final MouseEvent e)
   {
      determineCell (e); 
      if (view.getMode().usesBox())
      {
         view.setAnchor (e.getPoint());
         view.repaint();
      }
   }

   @Override
   public void mouseReleased (final MouseEvent e)
   {
      if (e.isPopupTrigger())
      {
         System.out.println ("MapPanel popup release"); // TBD
         // sample (eye-dropper) either layer to change drawIcon
      }
      else
      {
         determineCell (e);
         if (view.getMode().usesBox() && view.getAnchor() != null)
         {
            if (view.getMode() == Mode.Box && model.getDrawTile() != null)
               fillBox (model.getDrawLayer(), model.getDrawTile());
            else if (view.getMode() == Mode.Erase &&
                     !view.getAnchor().equals (view.getCursorPoint()))
               fillBox (model.getLayers().get (1), null);
            view.setAnchor (null);
         }
      }
   }

   private void fillBox (final Layer layer, final Tile tile)
   {
      Cell anchor = view.getCell (view.getAnchor());
      Cell cursor = view.getCell (view.getCursorPoint());
      int fromRow = anchor.getRow();
      int toRow   = cursor.getRow();
      int fromCol = anchor.getCol();
      int toCol   = cursor.getCol();
      
      int deltaX = fromCol <= toCol ? 1 : -1;
      int deltaY = fromRow <= toRow ? 1 : -1;
      for (int row = fromRow; row != toRow + deltaY; row += deltaY)
         for (int col = fromCol; col != toCol + deltaX; col += deltaX)
            model.setTile (layer, row, col, tile);
      
      view.repaint();
   }
   
   @Override
   public void mouseClicked (final MouseEvent e)
   {
      if (isRightClick (e))
      {
         view.setAnchor (null);
         view.repaint();
      }
      else
      {
         determineCell (e);
         Point cursor = view.getCursorPoint();
         Mode mode = view.getMode();
         if (mode == Mode.Draw)
            model.drawTile (view.getCell (cursor));
         else if (mode == Mode.Fill)
            model.fillTiles (view.getCell (cursor));
         else if (mode == Mode.Erase)
            erase (view.getCell (cursor));
         else if (mode.usesLine())
         {
            if (view.getAnchor() != null && !view.getAnchor().equals (cursor))
               model.addLine (new Line (mode.getLineType(), view.getAnchor(), cursor));
            view.setAnchor (cursor);
         }
         view.repaint();
      }
   }

   private void erase (final Cell cell)
   {
      Layer layer = model.getLayers().get (1);
      if (layer.getValue (cell.getRow(), cell.getCol()) != null)
         model.setTile (layer, cell, null); // erase feature
      else
         model.setTile (model.getLayers().get (0), cell, null); // erase terrain
   }
   
   private boolean isLeftClick (final MouseEvent e)
   {
      return (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0;
   }
   
   private boolean isRightClick (final MouseEvent e)
   {
      return (e.getModifiers() & InputEvent.BUTTON3_MASK) != 0;
   }
   
   private void determineCell (final MouseEvent e)
   {
      view.setCursorPoint (e.getPoint());
   }
}
