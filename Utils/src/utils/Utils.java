package utils;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;

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
}
