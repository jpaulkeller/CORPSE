package model;

import gui.ComponentTools;

import java.util.Collection;
import java.util.EventListener;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.JList;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * A collection-based implementation of the ListModel interface, which delegates
 * to a wrapped Collection.  Primarily designed for use with JList.
 */
public class CollectionModel<E> 
implements Collection<E>, javax.swing.ListModel, ComboBoxModel
{
   private static final long serialVersionUID = 0;
   
   private Collection<E> delegate;
   private E selected;
   
   public CollectionModel (final Collection<E> collection)
   {
      this.delegate = collection;
   }

   public int size()
   {
      return delegate.size();  
   }

   public boolean isEmpty()
   {
      return delegate.isEmpty();
   }

   public boolean contains (final Object o)
   {
      return delegate.contains (o);
   }

   public Iterator<E> iterator()
   {
      return delegate.iterator();
   }

   public Object[] toArray()
   {
      return delegate.toArray();
   }

   public <T> T[] toArray (final T[] a)
   {
      return delegate.toArray (a);
   }

   public boolean containsAll (final Collection<?> c)
   {
      return delegate.containsAll (c);
   }

   @Override
   public boolean equals (final Object o)
   {
      return delegate.equals (o);
   }

   @Override
   public int hashCode()
   {
      return delegate.hashCode();
   }
   
   protected int indexOf (final Object element)
   {
      int i = 0;
      for (E e : this)
      {
         if (e == element)
            return i;
         i++;
      }
      return -1;
   }
   
   // Implement all destructive operations to fire events
   
   public boolean add (final E element)
   {
      boolean status = delegate.add (element);
      if (status)
      {
         int index = size() - 1;
         fireIntervalAdded (this, index, index);
      }
      return status;
   }
   
   public boolean remove (final Object o)
   {
      int index = indexOf (o);
      boolean status = delegate.remove (o);
      if (status)
         fireIntervalRemoved (this, index, index);
      return status;
   }
   
   public boolean addAll (final Collection<? extends E> c)
   {
      int last = size() - 1;
      boolean status = delegate.addAll (c);
      if (status)
         fireIntervalAdded (this, last, size() - 1);
      return status;
   }

   public boolean removeAll (final Collection<?> c)
   {
      boolean status = false;
      Iterator<?> iter = c.iterator();
      while (iter.hasNext())
      {
         Object o = iter.next();
         if (contains (o))
            status &= remove (o);
      }
      return status;
   }

   public boolean retainAll (final Collection<?> c)
   {
      boolean status = false;
      Iterator<E> iter = iterator();
      while (iter.hasNext())
      {
         E e = iter.next();
         if (!c.contains (e))
         {
            iter.remove();
            status &= !contains (e);
         }
      }
      return status;
   }

   public void clear()
   {
      if (!isEmpty())
      {
         int last = size() - 1;
         delegate.clear();
         fireIntervalRemoved (this, 0, last);
      }
   }
   
   // implement ListModel

   public E getElementAt (final int index)
   {
      int i = 0;
      for (E e : this)
         if (index == i++)
            return e;
      return null;
   }

   public int getSize()
   {
      return size();
   }
   
   // Source from AbstractListModel
   
   private EventListenerList listenerList = new EventListenerList();

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

   public <T extends EventListener> T[] getListeners (final Class<T> listenerType)
   {
      return listenerList.getListeners (listenerType);
   }
   
   public Object getSelectedItem()
   {
      return selected;
   }

   @SuppressWarnings("unchecked")
   public void setSelectedItem (final Object element)
   {
      this.selected = (E) element;
   }
   
   public static void main (final String[] args)
   {
      CollectionModel<String> model = 
         new CollectionModel<String> (new TreeSet<String>());
      model.add ("one");
      model.add ("two");
      model.add ("three");
      ComponentTools.open (new JList (model), "CollectionModel (TreeSet)");
   }
}
