package db.jdbc;

/**
 * The Class DebugLevel.
 */
public final class DebugLevel
{
   /**
    * private constructor keeps all instances within class.
    */
   private DebugLevel()
   {
   }

   /** Turn debugging off. */
   public static final DebugLevel OFF = new DebugLevel();

   /** Turn debugging on. */
   public static final DebugLevel ON = new DebugLevel();

   /** Set debugging to verbose. */
   public static final DebugLevel VERBOSE = new DebugLevel();
}