package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * This class manages the transactions between an instance that is
 * being Observed and all the Observers of that instance. The observed
 * class delegates change-events to this class. */

public class ObservableDelegate extends Observable implements Serializable
{
   private static final long serialVersionUID = 3;

   /** Invoked by the observed class when it changes. */

   public synchronized void registerChange()
   {
      setChanged();
      notifyObservers();
      clearChanged();
   }

   public synchronized void registerChange (final Object arg)
   {
      setChanged();
      notifyObservers (arg);
      clearChanged();
   }

   /**
    * This method supports more complex events.  The first argument
    * should be the source object.  It assumes the remaining arguments
    * are pairs of keys (which much be Strings) and values (which can
    * be any Object).  They are added into a Map, and the Map is sent
    * to the observers. */

   public synchronized void registerChange (final ObservableDelegator source,
                                            final Object... keysAndValues)
   {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put ("source", source);

      for (int i = 0; i < keysAndValues.length; i += 2)
         if (keysAndValues[i] instanceof String)
            map.put ((String) keysAndValues[i], keysAndValues [i + 1]);
      
      setChanged();
      notifyObservers (map);
      clearChanged();
   }
}
