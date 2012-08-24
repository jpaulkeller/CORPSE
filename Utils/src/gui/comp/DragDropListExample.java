package gui.comp;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

public class DragDropListExample extends JList
{
   private static final long serialVersionUID = 1L;

   public static final DataFlavor DRAG_DROP_LIST_FLAVOR =
      new DataFlavor (DragDropListData.class, "DragDropListData");
   private static DataFlavor[] supportedFlavors = { DRAG_DROP_LIST_FLAVOR };

   public DragDropListExample()
   {
      setTransferHandler (new ReorderHandler());
      setDragEnabled (true);
      setSelectionMode (ListSelectionModel.SINGLE_INTERVAL_SELECTION);
   }

   public DragDropListExample (final DefaultListModel m)
   {
      setModel (m);
   }

   public void dropComplete()
   {
   }

   private class ReorderHandler extends TransferHandler
   {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean importData (final TransferSupport support)
      {
         // index of the element onto which the dragged element is dropped
         final int dropIndex = locationToIndex (getDropLocation().getDropPoint());

         try
         {
            Transferable xfer = support.getTransferable();
            DragDropListData dlData = 
               (DragDropListData) xfer.getTransferData (DRAG_DROP_LIST_FLAVOR);
            Object[] draggedData = dlData.data;
            final DragDropListExample dragList = dlData.parent;
            DefaultListModel dragModel = (DefaultListModel) dragList.getModel();
            DefaultListModel dropModel = (DefaultListModel) DragDropListExample.this.getModel();

            final Object leadItem = dropIndex >= 0 ? dropModel.elementAt (dropIndex) : null;
            final int dataLength = draggedData.length;

            // make sure that the lead item is not in the dragged data
            if (leadItem != null)
               for (Object item : draggedData)
                  if (item.equals (leadItem))
                     return false;

            int dragLeadIndex = -1;
            final boolean localDrop = dropModel.contains (draggedData[0]);

            if (localDrop)
               dragLeadIndex = dropModel.indexOf (draggedData[0]);

            // TBD: really?
            for (int i = 0; i < dataLength; i++)
               dragModel.removeElement (draggedData[i]);

            if (localDrop)
            {
               final int adjustedLeadIndex = dropModel.indexOf (leadItem);
               final int insertionAdjustment = dragLeadIndex <= adjustedLeadIndex ? 1 : 0;

               final int[] indices = new int[dataLength];
               for (int i = 0; i < dataLength; i++)
               {
                  dropModel.insertElementAt 
                     (draggedData[i], adjustedLeadIndex + insertionAdjustment + i);
                  indices[i] = adjustedLeadIndex + insertionAdjustment + i;
               }

               SwingUtilities.invokeLater (new Runnable()
               {
                  public void run()
                  {
                     DragDropListExample.this.clearSelection();
                     DragDropListExample.this.setSelectedIndices (indices);
                     dropComplete();
                  }
               });
            }
            else
            {
               final int[] indices = new int[dataLength];
               for (int i = 0; i < dataLength; i++)
               {
                  dropModel.insertElementAt (draggedData[i], dropIndex + 1);
                  indices[i] = dropIndex + 1 + i;
               }

               SwingUtilities.invokeLater (new Runnable()
               {
                  public void run()
                  {
                     DragDropListExample.this.clearSelection();
                     DragDropListExample.this.setSelectedIndices (indices);
                     dragList.clearSelection();
                     dropComplete();
                  }
               });
            }
         }
         catch (Exception x)
         {
            x.printStackTrace();
         }
         return false;
      }

      @Override
      public int getSourceActions (final JComponent c)
      {
         return TransferHandler.MOVE;
      }

      @Override
      protected Transferable createTransferable (final JComponent c)
      {
         return new DragDropListData (DragDropListExample.this,
                                      DragDropListExample.this.getSelectedValues());
      }

      @Override
      public boolean canImport (final TransferSupport support)
      {
         return support.isDrop() && support.isDataFlavorSupported (DRAG_DROP_LIST_FLAVOR);
      }

      @Override
      public Icon getVisualRepresentation (final Transferable t)
      {
         return super.getVisualRepresentation (t);
      }
   }

   private class DragDropListData implements Transferable
   {
      private DragDropListExample parent;
      private Object[] data;

      protected DragDropListData (final DragDropListExample p, final Object[] d)
      {
         parent = p;
         data = d;
      }

      public Object getTransferData (final DataFlavor flavor)
      throws UnsupportedFlavorException, IOException
      {
         if (flavor.equals (DRAG_DROP_LIST_FLAVOR))
            return DragDropListData.this;
         return null;
      }

      public DataFlavor[] getTransferDataFlavors()
      {
         return supportedFlavors;
      }

      public boolean isDataFlavorSupported (final DataFlavor flavor)
      {
         return true;
      }
   }
   
   public static void main (final String[] args)
   {
      DefaultListModel model1 = new DefaultListModel();
      model1.addElement ("one");
      model1.addElement ("two");
      model1.addElement ("three");
      DragDropListExample list1 = new DragDropListExample (model1);
      JScrollPane scroll1 = new JScrollPane (list1);
      scroll1.setPreferredSize (new Dimension (200, 200));
      
      DefaultListModel model2 = new DefaultListModel();
      model2.addElement ("four");
      model2.addElement ("five");
      model2.addElement ("six");
      DragDropListExample list2 = new DragDropListExample (model2);
      JScrollPane scroll2 = new JScrollPane (list2);
      scroll2.setPreferredSize (new Dimension (200, 200));
      
      JPanel panel = new JPanel (new BorderLayout());
      panel.add (scroll1, BorderLayout.WEST);
      panel.add (scroll2, BorderLayout.EAST);
      ComponentTools.open (panel, "DragDropList");
   }
}