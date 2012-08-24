package gui.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This class can be used in place of the default (platform-specific)
 * DragGestureRecognizer, which does not work very well as of JDK
 * 1.4. 
 
     protected DragSource dragSource = DragSource.getDefaultDragSource();
  
     // support for drag-and-drop
     // This is a hack to avoid a problem in JDK 1.4 (the drag
     // gesture was not being recognized when you move too slowly).
     new DragRecognizer
        (dragSource, this, DnDConstants.ACTION_COPY_OR_MOVE,
         new SymbolDragListener()); 
 */

public class DragRecognizer extends DragGestureRecognizer
implements MouseListener, MouseMotionListener
{
   private static final long serialVersionUID = 4;

   private Point pressed;
   
   public DragRecognizer (final DragSource ds,
                          final Component c,
                          final int dndConstants,
                          final DragGestureListener dgl)
   {
      super (ds, c, dndConstants, dgl);
   }

   @Override
   protected void registerListeners()
   {
      getComponent().addMouseListener (this);
      getComponent().addMouseMotionListener (this);
   }

   @Override
   protected void unregisterListeners()
   {
      getComponent().removeMouseListener (this);
      getComponent().removeMouseMotionListener (this);
   }

   // implement MouseListeners

   public void mousePressed (final MouseEvent me)
   {
      pressed = me.getPoint();
      events.add (me);
   }

   public void mouseReleased (final MouseEvent me)
   {
      pressed = null;
      events.clear();
   }

   public void mouseDragged (final MouseEvent me)
   {
      if (pressed != null)
      {
         events.add (me);
         // TBD: determine actions based on OS and mouse button / meta-keys
         fireDragGestureRecognized (DnDConstants.ACTION_MOVE, pressed);
         pressed = null;
      }
   }

   public void mouseClicked (final MouseEvent me) { }
   public void mouseEntered (final MouseEvent me) { }
   public void mouseExited  (final MouseEvent me) { }
   public void mouseMoved   (final MouseEvent me) { }
}
