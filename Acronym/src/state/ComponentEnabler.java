package state;

import java.awt.Component;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import model.CollectionMap;

/**
 * The class listens for state change events fired by a StateModel when
 * the model changes. Based on which states are active, this class will
 * enable the objects that are supposed to be active and disable all others.
 */

public class ComponentEnabler implements StateChangeListener, Serializable
{
   private static final long serialVersionUID = 0;

   // all of the components registered with this class
   private Set<Component> allComponents;

   // maps states to components that should be enabled during those states 
   private CollectionMap<String, Component> enabledWhen;

   // maps states to components that should be disabled during those states 
   private CollectionMap<String, Component> disabledWhen;
   
   public ComponentEnabler (final StateModel model)
   {
      model.setStateChangeListener (this);
      
      allComponents = new HashSet<Component>();
      enabledWhen = new CollectionMap<String, Component>();
      disabledWhen = new CollectionMap<String, Component>();
   }
   
   /**
    * Sets the list of states for which the given component should be enabled.
    *
    * @param comp Component to be registered
    * @param enabledStates states in which comp should be enabled
    */
   public void enableWhen (final Component comp, final String... enabledStates)
   {
      allComponents.add (comp);
      for (String state : enabledStates)
         enabledWhen.putElement (state, comp);
   }

   /**
    * Sets the states for which a the given component should be disabled.
    * Note that inactive states take precedence.  So, if the list of current
    * states includes one or more states in which a component should be enabled,
    * and one or more in which the same component should be disabled, it will be
    * disabled.
    *
    * @param comp Component to be registered
    * @param disabledStates states in which comp should be disabled */

   public void disableWhen (final Component comp, final String... disabledStates)
   {
      if (!allComponents.contains (comp))
         enableWhen (comp, StateModel.States.DefaultState.toString());
      for (String state : disabledStates)
         this.disabledWhen.putElement (state, comp);
   }

   /**
    * Called by the StateModel every time it experiences a
    * change. The current state list is passed to it.
    * @param states the list of currently active states */

   public void stateChanged (final Set<String> states)
   {
      Set<Component> componentsToEnable = new HashSet<Component>();
      Set<Component> componentsToDisable = new HashSet<Component>();

      for (String state : states)
      {
         Collection<Component> list = enabledWhen.get (state);
         if (list != null)
            componentsToEnable.addAll (list);
         list = disabledWhen.get (state);
         if (list != null)
            componentsToDisable.addAll (list);
      }
      // remove any components which should be disabled
      componentsToEnable.removeAll (componentsToDisable);

      // enable or disable each object
      for (Component comp : allComponents)
         comp.setEnabled (componentsToEnable.contains (comp));
   }
}
