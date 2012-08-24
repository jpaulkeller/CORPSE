package model;

import java.util.Observable;
import java.util.Observer;

/**
 * This interface facilitates the use of the Model-View pattern, using
 * java's Observer and Observable classes.  It is required since
 * Observable is not an interface, and some implementations cannot
 * extend it. */

public interface ObservableDelegator
{
   /**
    * Returns a reference to the Observable instance that
    * is managing the transactions to Observers. */
   Observable getObservable();

   void addObserver (final Observer observer);
   void deleteObserver (final Observer observer);
}
