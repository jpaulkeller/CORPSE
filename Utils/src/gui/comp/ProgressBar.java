package gui.comp;

import java.awt.Font;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/** A thread-safe extension of JProgressBar. */

public class ProgressBar extends JProgressBar
{
   public ProgressBar()
   {
      super (0, 100);
      setFont (new Font ("Arial", Font.BOLD, 12));
      setStringPainted (true);
   }
   
   @Override
   public void setString (final String text)
   {
      SwingUtilities.invokeLater (new Runnable()
      {
         @Override
         public void run()
         {
            ProgressBar.super.setString (text);
            if (text != null)
               System.out.println (text);
         }
      });
   }

   @Override
   public void setIndeterminate (final boolean newValue)
   {
      SwingUtilities.invokeLater (new Runnable()
      {
         @Override
         public void run()
         {
            ProgressBar.super.setIndeterminate (newValue);
         }
      });
   }

   @Override
   public void setValue (final int n)
   {
      SwingUtilities.invokeLater (new Runnable()
      {
         @Override
         public void run()
         {
            ProgressBar.super.setValue (n);
         }
      });
   }
   
   public void reset (final String text)
   {
      SwingUtilities.invokeLater (new Runnable()
      {
         @Override
         public void run()
         {
            ProgressBar.super.setIndeterminate (false);
            ProgressBar.super.setString (text != null ? text : "");
            ProgressBar.super.setValue (0);
         }
      });
   }
}
