package state;

import java.awt.Cursor;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/** 
 * The model that reflects the current state of a system.  Typically, this will
 * be the state of an application.  If the model changes, the StateChangeListener
 * is notified. */

public class StateModel implements Serializable
{
   private static final long serialVersionUID = 0;

   static final String DEFAULT_STATE = "StateModel.DefaultState";
   public static final Cursor DEFAULT_CURSOR =
      Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR);

   private Set<String> states = new HashSet<String>(); // current states
   private String mode; // a modal state (optional)
   private Cursor cursor = DEFAULT_CURSOR;

   private transient StateChangeListener stateChangeListener;
   
   public StateModel()
   {
      addState (DEFAULT_STATE);
   }

   public void setStateChangeListener (final StateChangeListener listener)
   {
      stateChangeListener = listener;
   }

   public StateChangeListener getStateChangeListener()
   {
      return stateChangeListener;
   }

   /** Returns the current States. */

   public Set<String> getStates()
   {
      return states;
   }

   /** Sets the current state. */

   public void setState (final String newState)
   {
      states.clear();
      states.add (newState);
      fireStateChanged();
   }

   /** Adds the given state to the active state list. */

   public void addState (final String state)
   {
      if (!states.contains (state))
      {
         states.add (state);
         fireStateChanged();
      }
   }

   /** Removes the given state from the active state list. */

   public void removeState (final String state)
   {
      if (states.contains (state))
      {
         states.remove (state);
         fireStateChanged();
      }
   }

   /** Adds or removes the given state, depending on the given active flag. */
   
   public void updateState (final String state, final boolean active)
   {
      if (active)
         addState (state);
      else
         removeState (state);
   }

   /** Returns the current mode, or null. */
   
   public String getMode()
   {
      return mode;
   }

   /** Removes the given mode from the state list. */
   public void removeMode (final String modeToRemove)
   {
      if (modeToRemove == this.mode)
         setMode (null);
   }

   public void setMode (final String mode)
   {
      setMode (mode, null);
   }

   /** Adds the given mode to the state list, removing the previous
       mode if any. */
   public void setMode (final String newMode, final Cursor modeCursor)
   {
      if (newMode != this.mode)
      {
         if (this.mode != null && states.contains (this.mode))
            states.remove (this.mode);
         if (newMode != null && !states.contains (newMode))
            states.add (newMode);
         this.mode = newMode;
         fireStateChanged();
      }
      setCursor (modeCursor);
   }

   public Cursor getCursor()
   {
      return cursor != null ? cursor : DEFAULT_CURSOR;
   }

   public void setCursor (final Cursor cursor)
   {
      this.cursor = cursor != null ? cursor : DEFAULT_CURSOR;
   }

   public void fireStateChanged()
   {
      if (stateChangeListener != null)
         stateChangeListener.stateChanged (states);
   }
}
