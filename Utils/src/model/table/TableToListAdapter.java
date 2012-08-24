package model.table;

import javax.swing.ComboBoxModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * This class is an adapter for TableModel objects, so that they can be used
 * as a ListModel or ComboBoxModel.
 */

public class TableToListAdapter
implements javax.swing.ListModel, ComboBoxModel
{
   private TableModel model;
   private EventListenerList listenerList = new EventListenerList();
   private Object selected;
   
   public TableToListAdapter (final TableModel model)
   {
      this.model = model;
      model.addTableModelListener (new ModelListener());
   }

   // implement ListModel (source from AbstractListModel)
   
   public Object getElementAt (final int row)
   {
      return getSize() > row ? model.getValueAt (row, 0) : null; 
   }
   
   public int getSize()
   {
      return model.getRowCount();
   }
   
   public void addListDataListener (final ListDataListener l)
   {
      listenerList.add (ListDataListener.class, l);
   }

   public void removeListDataListener (final ListDataListener l)
   {
      listenerList.remove (ListDataListener.class, l);
   }

   public ListDataListener[] getListDataListeners()
   {
      return listenerList.getListeners (ListDataListener.class);
   }

   protected void fireContentsChanged (final Object source, 
                                       final int index0, final int index1)
   {
      Object[] listeners = listenerList.getListenerList();
      ListDataEvent e = null;

      for (int i = listeners.length - 2; i >= 0; i -= 2)
      {
         if (listeners[i] == ListDataListener.class)
         {
            if (e == null)
               e = new ListDataEvent (source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
            ((ListDataListener) listeners[i + 1]).contentsChanged (e);
         }
      }
   }

   protected void fireIntervalAdded (final Object source,
                                     final int index0, final int index1)
   {
      Object[] listeners = listenerList.getListenerList();
      ListDataEvent e = null;

      for (int i = listeners.length - 2; i >= 0; i -= 2)
      {
         if (listeners[i] == ListDataListener.class)
         {
            if (e == null)
               e = new ListDataEvent (source, ListDataEvent.INTERVAL_ADDED, index0, index1);
            ((ListDataListener) listeners[i + 1]).intervalAdded (e);
         }
      }
   }

   protected void fireIntervalRemoved (final Object source, 
                                       final int index0, final int index1)
   {
      Object[] listeners = listenerList.getListenerList();
      ListDataEvent e = null;

      for (int i = listeners.length - 2; i >= 0; i -= 2)
      {
         if (listeners[i] == ListDataListener.class)
         {
            if (e == null)
               e = new ListDataEvent (source, ListDataEvent.INTERVAL_REMOVED, index0, index1);
            ((ListDataListener) listeners[i + 1]).intervalRemoved (e);
         }
      }
   }

   // implement ComboBoxModel
   
   public Object getSelectedItem()
   {
      return selected;
   }

   public void setSelectedItem (final Object element)
   {
      this.selected = element;
   }
   
   class ModelListener implements TableModelListener
   {
      public void tableChanged (final TableModelEvent e)
      {
         int type = e.getType();
         switch (type)
         {
         case TableModelEvent.INSERT: 
            fireIntervalAdded (model, e.getFirstRow(), e.getLastRow());
            break;
         case TableModelEvent.UPDATE: 
            fireContentsChanged (model, e.getFirstRow(), e.getLastRow());
            break;
         case TableModelEvent.DELETE: 
            fireIntervalRemoved (model, e.getFirstRow(), e.getLastRow());
            break;
         default:
            System.out.println ("Unsupported event: " + e);
         }
      }
   }
}
