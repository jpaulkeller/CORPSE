package gui.form;

import gui.form.valid.StatusEvent;
import gui.form.valid.StatusListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The FormItems class represents a collection of FormItem objects.  It 
 * provides convenience methods for dealing with multiple items.
 *
 * For example, it is possible to have one ValueChangeListener
 * for the entire collection.  This listener is called whenever any form item
 * changes.  You may add multiple ValueChangeListeners, but if want to
 * do so, you must call addValueChangeListener after adding all of the
 * form items.
 *
 * This class also supports one StatusListener, which will be called
 * only when the status of the collection changes.  You can call
 * isValid(), which returns true if and only if all items are valid;
 * or hasChanged(), which returns true if any item has changed.
 *
 * @see FormItem
 */
public class FormItems extends ArrayList<FormItem> implements StatusListener
{
   private static final long serialVersionUID = 1;

   private List<StatusListener> statusListeners = new ArrayList<StatusListener>();
   private ValueChangeListener vcListener;
   private int validItemCount;

   public FormItems()
   {
   }

   public FormItems (final ValueChangeListener vcListener)
   {
      addValueChangeListener (vcListener);
   }

   @Override
   public boolean add (final FormItem item)
   {
      boolean added = super.add (item);

      if (vcListener != null)
         item.addValueChangeListener (vcListener);

      if (item.getValidator() != null && !statusListeners.isEmpty())
      {
         item.addStatusListener (this);
         if (item.isValid())
            validItemCount++;
      }
      else
         validItemCount++;

      return added;
   }

   public FormItem add (final FormItem item, final ValueChangeListener listener)
   {
      item.addValueChangeListener (listener);
      add (item);
      return item;
   }

   public void addAll (final FormItem[] newItems)
   {
      if (newItems != null)
         for (FormItem item : newItems)
            add (item);
   }

   public void addAll (final FormItem[] newItems, final ValueChangeListener listener)
   {
      if (newItems != null)
         for (FormItem item : newItems)
            add (item, listener);
   }

   public void stateChanged (final StatusEvent event)
   {
      boolean itemStatus = event.getStatus();
      if (itemStatus)
         validItemCount++;
      else
         validItemCount--;

      // Send an event only if the whole form becomes valid or
      // invalid.  The form is valid if and only if all items are
      // valid.  The form is invalid if any item is invalid.
      if (validItemCount == size())
         fireChangedEvent (true); // all items valid now
      else if (!itemStatus && (validItemCount == size() - 1))
         fireChangedEvent (false); // one item became invalid
   }

   public void fireChangedEvent (final boolean status)
   {
      if (!statusListeners.isEmpty())
      {
         StatusEvent event = new StatusEvent (this, null, status);
         for (StatusListener listener : statusListeners)
            listener.stateChanged (event);
      }
   }

   /**
    * Add the given listener to each item already in the form, and to
    * any items subsequently added to the form. Although you can add
    * multiple listeners, you must do so after adding all of the form
    * items.  Only the last listener added will be added to any new
    * form items. */

   public void addValueChangeListener (final ValueChangeListener listener)
   {
      this.vcListener = listener;
      for (FormItem item : this)
         item.addValueChangeListener (listener);
   }

   public void addStatusListener (final StatusListener listener)
   {
      if (statusListeners.isEmpty())
      {
         validItemCount = 0;
         for (FormItem item : this)
         {
            item.addStatusListener (this);
            if (item.isValid())
               validItemCount++;
         }
      }
      statusListeners.add (listener);
   }

   public void removeStatusListener (final StatusListener listener)
   {
      statusListeners.remove (listener);
      if (statusListeners.isEmpty())
         for (FormItem item : this)
            item.removeStatusListener (this);
   }

   public void apply()
   {
      for (FormItem item : this)
         item.apply();
   }
   
   public boolean isValid()
   {
      for (FormItem item : this)
         if (!item.isValid())
            return false;
      return true;
   }

   public boolean hasChanged()
   {
      for (FormItem item : this)
         if (item.hasChanged())
            return true;
      return false;
   }
   
   public void setEnabled (final boolean enable)
   {
      for (FormItem item : this)
         item.setEnabled (enable);
   }
}
