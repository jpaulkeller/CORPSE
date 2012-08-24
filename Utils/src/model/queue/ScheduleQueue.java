package model.queue;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import utils.Utils;

/**
 * ScheduleQueue manages an ordered list of runnable tasks.  Tasks
 * will be executed (in a separate thread) in order (by priority,
 * lowest first). If the task requiresSwing() method returns true, the
 * task will be executed (using SwingUtilities.invokeLater) in the
 * event dispatch thread.
 *
 * Note that to avoid redundant tasks, the ScheduleQueue will prevent
 * the queuing of tasks with the same ID (as returned by getID),
 * unless that ID is null. */

public class ScheduleQueue implements Serializable
{
   private static final long serialVersionUID = 11;

   private SortedSet<Task> tasks; // ordered list of Runnable Task objects
   private boolean running;
   private transient TaskThread taskThread = new TaskThread();

   private int minDelay;      // milliseconds
   private int maxDelay;      // milliseconds
   private int delay;         // milliseconds

   private boolean stopWhenEmpty;

   /**
    * If there are no tasks to perform, the thread will sleep for at
    * least minDelay but not more than maxDelay milliseconds.  The
    * delay will start at minDelay, and double each time until
    * maxDelay is reached.
    */
   public ScheduleQueue (final int minDelay, final int maxDelay)
   {
      this.minDelay = minDelay;
      this.maxDelay = maxDelay;
      this.delay = minDelay;

      Comparator<Task> byPriority = new PriorityComparator();
      tasks = Collections.synchronizedSortedSet (new TreeSet<Task> (byPriority));
   }

   public boolean add (final Task task)
   {
      synchronized (tasks)
      {
         return isQueued (task) ? false : tasks.add (task);
      }
   }

   public boolean remove (final Task task)
   {
      synchronized (tasks)
      {
         return tasks.remove (task);
      }
   }

   public void clear()
   {
      synchronized (tasks)
      {
         tasks.clear();
      }
   }

   public boolean isQueued (final Task task)
   {
      // if there is no ID, don't worry about duplicate tasks
      String id = task.getID();
      if (id == null)
         return false;

      synchronized (tasks)
      {
         if (tasks.contains (task))
         {
            tasks.contains (task);
            return true;
         }

         // check for duplicate ID (to avoid redundant tasks)
         for (Task scheduledTask : tasks)
            if (id.equals (scheduledTask.getID()))
               return true;
      }

      return false;
   }

   public boolean isEmpty()
   {
      synchronized (tasks)
      {
         return tasks.isEmpty();
      }
   }

   public int size()
   {
      synchronized (tasks)
      {
         return tasks.size();
      }
   }

   public void start()
   {
      if (!running)
      {
         this.running = true;
         taskThread.start();
      }
   }

   public void stop()
   {
      if (running)
      {
         this.running = false;
         taskThread.interrupt();
      }
   }

   public boolean isRunning()
   {
      return running;
   }

   public void setStopWhenEmpty (final boolean stopWhenEmpty)
   {
      synchronized (tasks)
      {
         this.stopWhenEmpty = stopWhenEmpty;
      }
   }

   class TaskThread extends Thread
   {
      private boolean interrupted;

      public TaskThread()
      {
         setDaemon (false); // don't exit JVM if this is the only thread running
      }
      
      Task next()
      {
         synchronized (tasks)
         {
            if (!isEmpty())
            {
               Task task = tasks.first();
               remove (task);
               return task;
            }
            return null;
         }
      }

      @Override
      public void run()
      {
         while (!interrupted)
         {
            Task task = next();
            if (task != null)
            {
               delay = minDelay;
               try
               {
                  if (task.requiresSwing())
                     // do the following on the GUI event-dispatching thread
                     SwingUtilities.invokeLater (task);
                  else
                     task.run();

                  // TBD: consider starting thread for each task?
                  // Thread thread = new Thread (task);
                  // thread.start();
               }
               catch (Exception x)
               {
                  System.err.println (x);
               }
            }
            else if (stopWhenEmpty)
            {
               interrupt();
            }
            else
            {
               Utils.sleep (delay);
               if (delay < maxDelay)
                  delay = Math.min (maxDelay, 2 * delay); // adaptive timer
            }
         }
      }

      @Override
      public void interrupt()
      {
         interrupted = true;
      }
   }

   static class PriorityComparator implements Comparator<Task>, Serializable
   {
      public int compare (final Task t1, final Task t2)
      {
         if (t1 == t2)
            return 0;
         int delta = t1.getPriority() - t2.getPriority();
         if (delta == 0) // only return 0 if the tasks are identical
            delta = t1.hashCode() - t2.hashCode();
         return delta;
      }
   }
   
   public static void main (final String[] args)
   {
      ScheduleQueue q = new ScheduleQueue (500, 1000);

      q.add (new ScheduleQueueTest (2, "B"));
      q.add (new ScheduleQueueTest (4, "D"));
      q.add (new ScheduleQueueTest (1, "A"));
      q.add (new ScheduleQueueTest (1, "A")); // duplicate, skip it
      q.add (new ScheduleQueueTest (1, "a")); // not duplicate, run it

      q.start();

      q.add (new ScheduleQueueTest (5, "E"));
      q.add (new ScheduleQueueTest (3, "C"));

      Utils.sleep (3000);
      q.add (new ScheduleQueueTest (0, "Z"));

      // wait until the queue is empty, then add one
      while (!q.isEmpty())
         Utils.sleep (1000);
      q.add (new ScheduleQueueTest (9, "I"));

      q.setStopWhenEmpty (true);
   }
}

class ScheduleQueueTest extends Task
{
   private static final long serialVersionUID = 11;

   private String name;

   ScheduleQueueTest (final int priority, final String name)
   {
      super (priority);
      this.name = name;
   }

   @Override
   public void run()
   {
      System.out.println ("Running: " + this);
      Utils.sleep (1000);
   }

   @Override
   public String getID() { return name; }
   
   @Override
   public String toString()
   {
      return name + " (priority: " + getPriority() + ")";
   }
}
