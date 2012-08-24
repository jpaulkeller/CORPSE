package state;

import java.util.Set;

/**
 * StateChangeListener is the interface a view must implement to receive state
 * change events fired by StateModel when the state changes. */

interface StateChangeListener
{
   void stateChanged (Set<String> states);
}
