package gui.form.valid;

import gui.form.ValueChangeEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides an adapter for data validators.  This was
 * designed for use with FormItem objects, but any source can be used.
 *
 * A StatusEvent is sent (via statusChanged) the first time the data
 * is validated, and then each time the validity status changes.
 *
 * One source can have multiple "daisy-chained" validators.  These
 * validators are tested in order, so that more complex (slower)
 * validation is only done if easier (faster) validation succeeds.
 *
 * Also, one validator object can be used to validate multiple
 * sources, even if there are multiple StatusListeners registered.
 * That is, the StatusChange events are per-source.
 *
 * Sub-classes must override the isValid(Object) method.  Also, in
 * order to facilitate using this class as a plug-in, the initialize()
 * method may be overridden.  This method takes a single String, and
 * should interpret that String as needed in order to meet any
 * specific requirements.  No other methods should need to be
 * overridden.
 */

public abstract class ValidationAdapter implements Validator
{
   private static final long serialVersionUID = 1L;

   /** Data structure which contains the source-specific information. */
   static class DataSource implements Serializable
   {
      private static final long serialVersionUID = 4;

      private List<StatusListener> listeners;
      private boolean initialized;      // true if source was initialized
      private boolean wasValid;         // true if source was valid

      protected DataSource (final boolean initialStatus)
      {
         listeners = new ArrayList<StatusListener>();
         initialized = false;
         wasValid = initialStatus;
      }
   }
   
   // This mapping of DataSource objects tracks info for each source.
   private Map<Object, DataSource> sources = new HashMap<Object, DataSource>();

   // The initial status for all data sources
   private boolean initialStatus = false;

   // Returned if the value is null
   private boolean isNullValid = false;
   
   public ValidationAdapter() { }

   public ValidationAdapter (final boolean initialStatus)
   {
      this.initialStatus = initialStatus;
   }

   /**
    * Interpret the given String as needed in order to initialize the
    * Validator as a plug-in. Should return true only if the
    * initialization succeeds. */

   public boolean initialize (final String arguments)
   {
      return true;
   }
   
   public void setNullValidity (final boolean status)
   {
      isNullValid = status;
   }
   
   public boolean isNullValid()
   {
      return isNullValid;
   }

   public abstract boolean isValid (Object value);
   
   public boolean validate (final Object source, final Object value)
   {
      boolean isValidNow = isValid (value);

      DataSource dataSource = sources.get (source);
      if (dataSource == null)
         dataSource = addDataSource (source);
      
      if (!dataSource.initialized) // first time for this source
      {
         dataSource.initialized = true;
         dataSource.wasValid = !isValidNow; // force event the 1st time
      }
      if (isValidNow != dataSource.wasValid) // state changed
      {
         fireChangedEvent (source, value, isValidNow);
         dataSource.wasValid = isValidNow;
      }
      
      return isValidNow;
   }
   
   public void valueChanged (final ValueChangeEvent e)
   {
      validate (e.getSource(), e.getValue());
   }
   
   public void addStatusListener (final Object source, final StatusListener listener)
   {
      DataSource dataSource = sources.get (source);
      if (dataSource == null)
         dataSource = addDataSource (source);
      dataSource.listeners.add (listener);
   }
   
   public void removeStatusListener (final Object source, final StatusListener listener)
   {
      DataSource dataSource = sources.get (source);
      if (dataSource != null)
         dataSource.listeners.remove (listener);
   }
   
   public void fireChangedEvent (final Object source, final Object value,
                                 final boolean status)
   {
      DataSource dataSource = sources.get (source);
      if (dataSource != null && !dataSource.listeners.isEmpty())
      {
         StatusEvent event = new StatusEvent (source, value, status);
         for (StatusListener listener : dataSource.listeners)
            listener.stateChanged (event);
      }
   }

   DataSource addDataSource (final Object source)
   {
      DataSource dataSource = new DataSource (initialStatus);
      sources.put (source, dataSource);
      return dataSource;
   }
}
