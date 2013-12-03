package gui.comp;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.InputEvent;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import model.CollectionModel;

public class DragDropList extends JList 
implements DragSourceListener, DragGestureListener
{
   private static final long serialVersionUID = 1L;
   
   public static final DataFlavor DRAG_DROP_LIST_FLAVOR =
      new DataFlavor (DragDropListData.class, "DragDropListData");
   private static DataFlavor[] supportedFlavors = { DRAG_DROP_LIST_FLAVOR };
   
   private DragSource dragSource;
   private int dndAction;

   public DragDropList (final int action)
   {
      enableDragAndDrop (action);
   }
   
   public DragDropList (final ListModel model, final int action)
   {
      super (model);
      enableDragAndDrop (action);
   }
   
   private void enableDragAndDrop (final int action)
   {
      this.dndAction = action;
      dragSource = new DragSource();
      dragSource.createDefaultDragGestureRecognizer
         (this, DnDConstants.ACTION_COPY_OR_MOVE, this);
      new DropTarget (this, new DropListener());
   }

   private boolean dragGestureAllowed (final DragGestureEvent e)
   {
      // ignore right-button drag to avoid conflict with pop-up menu
      InputEvent ie = e.getTriggerEvent();
      int modifiers = ie.getModifiers();
      if ((modifiers & InputEvent.BUTTON3_MASK) != 0)
         return false;
      return true;
   }

   /**
    * Called when a DragGestureRecognizer has detected a platform-dependent
    * drag-initiating gesture and is notifying this listener in order for it
    * to initiate the action for the user. */

   @Override
   public void dragGestureRecognized (final DragGestureEvent e)
   {
      if (dragGestureAllowed (e))
      {
         Object[] selected = getSelectedValues();
         Transferable xfer = new DragDropListData (this, selected);
         Cursor cursor = selectCursor (e.getDragAction());
         dragSource.startDrag (e, cursor, xfer, this);
      }
   }

   @Override
   public void dragDropEnd (final DragSourceDropEvent event)
   {
      ListModel model = getModel();
      if (event.getDropSuccess() &&
          event.getDropAction() == DnDConstants.ACTION_MOVE &&
          (dndAction & DnDConstants.ACTION_MOVE) != 0)
      {
         DragSourceContext drag = event.getDragSourceContext();
         DragDropListData data = getData (drag.getTransferable());
         if (data != null)
         {
            if (model instanceof DragSite)
               for (Object item : data.data)
                  ((DragSite) model).drag (item);
            else if (model instanceof DefaultListModel)
               for (Object item : data.data)
                  ((DefaultListModel) model).removeElement (item);
            else if (model instanceof CollectionModel)
               for (Object item : data.data)
                  ((CollectionModel) model).remove (item);
            else
               System.err.println ("DragDropList unable to modify: " + model.getClass());
         }
      }
   }

   @Override
   public void dragEnter (final DragSourceDragEvent e)
   {
      DragSourceContext context = e.getDragSourceContext();
      Cursor cursor = selectCursor (e.getDropAction());
      context.setCursor (null); // to avoid flicker
      context.setCursor (cursor); 
   }

   @Override
   public void dragExit (final DragSourceEvent event)
   {
   }

   @Override
   public void dragOver (final DragSourceDragEvent event)
   {
   }

   @Override
   public void dropActionChanged (final DragSourceDragEvent event)
   {
   }
   
   private Cursor selectCursor (final int action)
   {
      switch (action)
      {
      case DnDConstants.ACTION_COPY:
         return DragSource.DefaultCopyDrop;
      case DnDConstants.ACTION_LINK:
         return DragSource.DefaultLinkDrop;
      case DnDConstants.ACTION_MOVE:
         return DragSource.DefaultMoveDrop;
      default:
         return DragSource.DefaultCopyNoDrop;
      }
   }
   
   private DragDropListData getData (final Transferable xfer)
   {
      DragDropListData data = null; 
      try
      {
         if (xfer.isDataFlavorSupported (DRAG_DROP_LIST_FLAVOR))
            data = (DragDropListData) xfer.getTransferData (DRAG_DROP_LIST_FLAVOR); 
      }
      catch (IOException x)
      {
         System.err.println (x.getMessage());
         x.printStackTrace();
      }
      catch (UnsupportedFlavorException x)
      {
         System.err.println (x.getMessage());
         x.printStackTrace();
      }
      return data;
   }
   
   private static class DragDropListData implements Transferable
   {
      // private DragList list;
      private Object[] data;

      protected DragDropListData (final DragDropList list, final Object[] data)
      {
         // this.list = list;
         this.data = data;
      }

      @Override
      public Object getTransferData (final DataFlavor flavor) throws UnsupportedFlavorException, IOException
      {
         return flavor.equals (DRAG_DROP_LIST_FLAVOR) ? this : null;
      }

      @Override
      public DataFlavor[] getTransferDataFlavors()
      {
         return supportedFlavors;
      }

      @Override
      public boolean isDataFlavorSupported (final DataFlavor flavor)
      {
         return flavor == DRAG_DROP_LIST_FLAVOR;
      }
   }
   
   class DropListener extends DropTargetAdapter
   {
      // Return the first compatible DataFlavor, or null. This method
      // also ensures the action type is compatible.

      private DataFlavor getDataFlavor (final DropTargetDragEvent e)
      {
         if ((e.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) != 0)
            if (e.isDataFlavorSupported (DRAG_DROP_LIST_FLAVOR))
               return DRAG_DROP_LIST_FLAVOR;
         return null;
      }
      
      /** Return true if we find a compatible DataFlavor and Action. */

      public boolean isDragOk (final DropTargetDragEvent e)
      {
         return getDataFlavor (e) != null;
      }

      /** Called when a drag operation has encountered the DropTarget. */

      @Override
      public void dragEnter (final DropTargetDragEvent e)
      {
         dragOver (e);
      }

      @Override
      public void dropActionChanged (final DropTargetDragEvent e)
      {
         dragOver (e);
      }
      
      /** Called when a drag operation is ongoing on the DropTarget. */

      @Override
      public void dragOver (final DropTargetDragEvent e)
      {
         if (isDragOk (e))
            e.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
         else
            e.rejectDrag();
      }

      @Override
      public void drop (final DropTargetDropEvent e)
      {
         DragDropListData data = getData (e.getTransferable());
         if (data != null)
            drop (e, data);
         else
         {
            e.getDropTargetContext().dropComplete (true);
            e.rejectDrop();
         }
      }
      
      private void drop (final DropTargetDropEvent e, final DragDropListData data)
      {
         e.acceptDrop (DnDConstants.ACTION_COPY_OR_MOVE);
         ListModel model = getModel();
         if (model instanceof DropSite)
            for (Object item : data.data)
               ((DropSite) model).drop (item);
         else if (model instanceof DefaultListModel)
            for (Object item : data.data)
               ((DefaultListModel) model).addElement (item);
         else if (model instanceof CollectionModel)
            for (Object item : data.data)
               ((CollectionModel) model).add (item);
         else
            System.err.println ("DragDropList unable to modify: " + model.getClass());
         e.getDropTargetContext().dropComplete (true);
      }
   }
   
   public static void main (final String[] args)
   {
      DefaultListModel model1 = new DefaultListModel();
      model1.addElement ("one");
      model1.addElement ("two");
      model1.addElement ("three");
      JList list1 = new DragDropList (model1, DnDConstants.ACTION_COPY_OR_MOVE);
      JScrollPane scroll1 = new JScrollPane (list1);
      scroll1.setPreferredSize (new Dimension (200, 200));
      
      DefaultListModel model2 = new DefaultListModel();
      model2.addElement ("four");
      model2.addElement ("five");
      model2.addElement ("six");
      JList list2 = new DragDropList (model2, DnDConstants.ACTION_COPY);
      JScrollPane scroll2 = new JScrollPane (list2);
      scroll2.setPreferredSize (new Dimension (200, 200));
      
      JPanel panel = new JPanel (new BorderLayout());
      panel.add (scroll1, BorderLayout.WEST);
      panel.add (scroll2, BorderLayout.EAST);
      ComponentTools.open (panel, "DragDropList");
   }
}
