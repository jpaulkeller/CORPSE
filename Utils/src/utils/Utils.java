package utils;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public final class Utils
{
   public static final Dimension 
   SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

   public static final Border BORDER;
   static
   {
      Border up    = BorderFactory.createRaisedBevelBorder();
      Border down  = BorderFactory.createLoweredBevelBorder();
      BORDER = BorderFactory.createCompoundBorder (up, down);
   }

   private Utils()
   {
      // utility class; prevent instantiation
   }
   
   public static final String OS = System.getProperty ("os.name").toUpperCase();

   public static boolean equals (final Object o1, final Object o2)
   {
      if (o1 == o2)
         return true;
      if (o1 == null)
         return false;
      return o1.equals (o2);
   }
   
   public static boolean isWindowsPlatform()
   {
      return OS != null && OS.startsWith ("WINDOWS");
   }
   
   public static Image scaleImage (final Image image, final int w, final int h)
   {
      return image.getScaledInstance (w, h, Image.SCALE_SMOOTH);
   }

   /** A convenience method for invoking Thread.sleep(). */

   public static boolean sleep (final long millis)
   {
      try
      {
         Thread.sleep (millis);
         return true;
      }
      catch (InterruptedException x)
      {
         System.err.println (x);
         x.printStackTrace (System.err);
      }

      return false;
   }
   
   public static String getExceptionText (final Throwable x)
   {
      StringBuilder sb = new StringBuilder();
      if (x.getMessage() != null)
         sb.append (x.getMessage() + "\n");
      else
         sb.append (x + "\n");
      for (StackTraceElement line : x.getStackTrace())
         sb.append (line + "\n");
      return sb.toString();
   }
   
   /**
    * Gets a one-line representation of the call-stack.
    *
    * @return a one-line representation of the call-stack
    */
   public static String getStack(final String regex)
   {
      return getStack(Thread.currentThread().getStackTrace(), regex);
   }
 
   /**
    * Gets a one-line representation of the given call-stack.
    *
    * @param stack the call stack (e.g., exception.getStackTrace)
    * @param classNameFilter an optional class name filter
    * @return a one-line representation of the call-stack
    */
   public static String getStack(final StackTraceElement[] stack, final String regex)
   {
      StringBuilder sb = new StringBuilder();
      
      for (int i = stack.length - 1; i >= 0; i--)
      {
         String className = stack[i].getClassName();
         if ((regex == null || Pattern.matches(regex, className)) && !"getStack".equals(stack[i].getMethodName())) // ignore this method
         {
            if (sb.length() > 0)
               sb.append(", ");
            className = stack[i].getClassName();
            sb.append(className + "." + stack[i].getMethodName() + ":" + stack[i].getLineNumber());
         }
      }
      
      return sb.toString();
   }
}
