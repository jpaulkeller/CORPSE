package applet;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Counts down to a particular time/date

public class CountdownClock extends Applet implements Runnable
{
   private static final SimpleDateFormat YMD =
      new SimpleDateFormat ("yyyy-MM-dd hh:mm");
   private static final DecimalFormat D2 = new DecimalFormat ("00");
   
   // PARAMETER SUPPORT:
   private static final String PARAM_DATE = "date";
   private String paramDate = "2008-11-18 00:00";

   private Thread cdThread = null;
   private boolean stopped;
   private Date targetDate;
   private StringBuilder sb = new StringBuilder();
   private String label; // displayed to user
   
   public CountdownClock()
   {
      super();
   }

   @Override
   public String getAppletInfo()
   {
      return "Name: CountdownClock";
   }

   // Returns an array of strings for the parameters understood by this applet.
   @Override
   public String[][] getParameterInfo()
   {
      String[][] info =
      { 
         { PARAM_DATE, "String", "Date to which applet will countdown" },
      };
      return info;
   }

   @Override
   public void init()
   {
      // The following code retrieves the value of each parameter specified with
      // the tag and stores it in a member variable.
      String param = getParameter (PARAM_DATE);
      try
      {
         if (param != null)
            paramDate = param;
         targetDate = YMD.parse (paramDate);
      }
      catch (ParseException x)
      {
         System.err.println (x);
      }
      
      setBackground (new Color (200, 255, 200));
      setForeground (Color.BLACK);
      setFont (new Font ("Arial", Font.BOLD, 15));
      resize (200, 100);
      
      start(); // Start new thread to calculate date
   }

   @Override
   public void paint (final Graphics g)
   {
      FontMetrics fm = g.getFontMetrics();
      int strWidth = fm.stringWidth (label);
      int appletWidth = getSize().width;
      g.drawString (label, (appletWidth - strWidth) / 2, 20);
   }

   @Override
   public void start()
   {
      if (cdThread == null)
      {
         cdThread = new Thread (this);
         cdThread.start();
      }
   }

   @Override
   public void stop()
   {
      if (cdThread != null)
      {
         stopped = true;
         cdThread = null;
      }
   }

   // Run method to recalculate the countdown string
   @Override
   public void run()
   {
      while (!stopped)
      {
         try
         {
            getLabel();
            repaint();
            Thread.sleep (1000);
         }
         catch (Exception x)
         {
            stop();
         }
      }
   }

   private String getLabel()
   {
      Date currentDate = new Date();
      if (targetDate.after (currentDate))
      {
         long diff = targetDate.getTime() - currentDate.getTime();
         diff = diff / 1000; // seconds
         // int seconds = (int) diff % 1000;
         diff = diff / 60; // minutes
         int minutes = (int) diff % 60;
         diff = diff / 60; // hours
         int hours = (int) diff % 24;
         diff = diff / 24; // days
         int days = (int) diff % 365;
         
         sb.setLength (0);
         if (days > 1)
            sb.append (days + " days ");
         else
            hours += days * 24;
         sb.append (D2.format (hours) + " hrs ");
         sb.append (D2.format (minutes) + " min");
         label = sb.toString();
      }
      else
         label = "Countdown reached!";
      return label;
   }
   
   public static void main (final String[] args)
   {
      try
      {
         CountdownClock app = new CountdownClock();
         app.targetDate = YMD.parse (app.paramDate);
         System.out.println (app.getLabel());
      }
      catch (Exception x)
      {
         x.printStackTrace (System.err);
      }
   }
}
