package model;

import gui.ComponentTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JList;

/**
 * A collection-based implementation of the ListModel interface, which delegates
 * to a wrapped Collection.  Primarily designed for use with JList.
 */
public class ListModel<E> extends CollectionModel<E> implements List<E>
{
   private static final long serialVersionUID = 0;
   
   private List<E> delegate;
   
   public ListModel (final List<E> list)
   {
      super (list);
      this.delegate = list;
   }

   public List<E> subList (final int fromIndex, final int toIndex)
   {
      return delegate.subList (fromIndex, toIndex);
   }
   
   public E get (final int index)
   {
      return delegate.get (index);
   }

   @Override
   public int indexOf (final Object o)
   {
      return delegate.indexOf (o);
   }

   public int lastIndexOf (final Object o)
   {
      return delegate.lastIndexOf (o);
   }

   public ListIterator<E> listIterator()
   {
      return delegate.listIterator();
   }

   public ListIterator<E> listIterator (final int index)
   {
      return delegate.listIterator (index);
   }

   // Implement all destructive operations to fire events
   
   public void add (final int index, final E element)
   {
      delegate.add (index, element);
      fireIntervalAdded (this, index, index);
   }

   public boolean addAll (final int index, final Collection<? extends E> c)
   {
      boolean status = delegate.addAll (index, c);
      if (status)
         fireIntervalAdded (this, index, index + c.size() - 1);
      return status;
   }

   public E set (final int index, final E element)
   {
      return delegate.set (index, element);
      // TBD fire?
   }

   public E remove (final int index)
   {
      E removed = delegate.remove (index);
      fireIntervalRemoved (this, index, index);
      return removed;
   }

   public static void main (final String[] args)
   {
      ListModel<String> model = new ListModel<String> (new ArrayList<String>());
      model.add ("one");
      model.add ("two");
      model.add ("three");
      ComponentTools.open (new JList (model), "ListModel");
   }
}
