package model.queue;

import java.io.Serializable;

/**
 * A runnable with optional attributes such as priority.  Note that
 * you can override getID() to prevent the queuing of redundant
 * tasks. */

public class Task implements Runnable, Serializable
{
   private static final long serialVersionUID = 4;

   private Runnable runnable;
   private int priority;
   private boolean requiresSwing;

   public Task (final Runnable runnable)
   {
      this (runnable, 0);
   }

   public Task (final Runnable runnable, final int priority)
   {
      this.runnable = runnable;
      setPriority (priority);
   }

   // for sub-classes that override run()

   public Task()
   {
      this (0);
   }

   public Task (final int priority)
   {
      setPriority (priority);
   }

   public void run()
   {
      runnable.run();
   }

   /** Override this to prevent redundant tasks. */

   public String getID()
   {
      return null;
   }

   public void setPriority (final int priority)
   {
      this.priority = priority;
   }

   public int getPriority()
   {
      return priority;
   }

   public void setRequiresSwing (final boolean requiresSwing)
   {
      this.requiresSwing = requiresSwing;
   }

   public boolean requiresSwing()
   {
      return requiresSwing;
   }

   @Override
   public String toString()
   {
      return runnable != null ? runnable.toString() : super.toString();
   }
}
