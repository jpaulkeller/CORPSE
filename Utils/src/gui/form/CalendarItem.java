package gui.form;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import utils.ImageTools;
import wkw.beans.ui.JCalendarPanel;

// JCalendarPanel.jar

/**
 * CalendarItem objects provide a friendly interface for selecting (or
 * entering) a date and/or time, for use in a form.
 */
public class CalendarItem extends FormItemAdapter
   implements ChangeListener, PropertyChangeListener
{
   private static final long serialVersionUID = 16;

   public static final String DATE_FORMAT = "yyyy-MM-dd";
   public static final String TIME_FORMAT = "HH:mm:ss";
   public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
   
   private JButton calButton;
   private JPanel panel;                // main component
   private JCalendarPanel calendarPopup;
   private JDialog calendar;
   private SimpleDateFormat dateFormat;
   private SimpleDateFormat alternateFormat;

   private SpinnerDateModel model;
   private JSpinner spinner;

   public CalendarItem ()
   {
      this ("Select Date/Time", DATE_TIME_FORMAT);
   }

   public CalendarItem (final String label, final String format)
   {
      this (label, format, null, Calendar.MINUTE);
   }
   
   /**
    * @param label the FormItem label
    * @param format a format for use with javax.swing.JSpinner.DateEditor
    * @param curDate initial date displayed (defaults to "now" if null)
    * @param intervalUnit spinner increment/decrement (e.g., Calendar.SECOND)
    *
    * @see javax.swing.SpinnerDateModel
    */
   public CalendarItem (final String label, final String format,
                        final Date curDate, final int intervalUnit)
   {
      setLabel (label);
      setFormat (format);

      model = new SpinnerDateModel();
      model.setCalendarField (intervalUnit);

      if (curDate == null)
         setValue (getCurrentEpoch (format));
      else
         setInitialValue (curDate);

      spinner = new JSpinner (model);
      int w = (format.length() * 9) + 18; // 9 per char + the up/down arrows
      spinner.setPreferredSize (new Dimension (w, 25));
      spinner.setEditor (new JSpinner.DateEditor (spinner, format));
      model.addChangeListener (this);

      panel = new JPanel (new BorderLayout());
      panel.add (spinner, BorderLayout.CENTER);

      getEditorComponent().getDocument().addDocumentListener (this);
      showChangeStatus();
   }

   // Hack to reset undisplayed fields to the Epoch time.  This is
   // necessary since undisplayed fields will be returned as Epoch
   // (not as 0).  For example, the year (if not displayed) will be
   // returned as 1970.  Undisplayed fields should not be used by the
   // caller; however we still need this hack because without it the
   // hasChanged method will not work properly.
   
   private Date getCurrentEpoch (final String format)
   {
      Calendar now = new GregorianCalendar();
      if (format != null)
      {
         // now.setTimeInMillis (0); -- should work, but doesn't

         if (format.indexOf ("y") < 0)
            now.set (Calendar.YEAR, 1970);
         if (format.indexOf ("M") < 0)
            now.set (Calendar.MONTH, 0); // January
         if (format.indexOf ("d") < 0)
            now.set (Calendar.DATE, 0);
         if (format.indexOf ("H") < 0 && format.indexOf ("h") < 0 &&
             format.indexOf ("K") < 0 && format.indexOf ("k") < 0)
            now.set (Calendar.HOUR, 12);
         if (format.indexOf ("m") < 0)
            now.set (Calendar.MINUTE, 0);
         if (format.indexOf ("s") < 0)
            now.set (Calendar.SECOND, 0);
         if (format.indexOf ("S") < 0)
            now.set (Calendar.MILLISECOND, 0);
      }
      return now.getTime();
   }

   public void enablePopup (final boolean enabled, final boolean modalPopup)
   {
      if (enabled)
      {
         Icon icon = ImageTools.getIcon ("icons/20/objects/Calendar.gif");
         if (icon != null)
            calButton = new JButton (icon);
         else
            calButton = new JButton ("CAL");
         calButton.setFocusable (false);
         calButton.setPreferredSize (new Dimension (25, 25));
         calButton.addActionListener (new MenuButtonListener());
         
         calendarPopup = new JCalendarPanel();
         calendarPopup.addPropertyChangeListener (this);
         
         createCalendar (modalPopup);

         panel.add (calButton, BorderLayout.WEST);
      }
   }

   public void setMinDate (final Date min)
   {
      if (min != null)
         model.setStart (min);
   }

   /** Not working yet. */
   
   public void setMaxDate (final Date max)
   {
      if (max != null)
         model.setStart (max);
   }

   @Override
   public Object getValue()
   {
      return model.getValue();
   }

   public Date getDate()
   {
      return (Date) getValue();
   }

   public Calendar getCalendar()
   {
      Calendar cal = new GregorianCalendar();
      cal.setTime ((Date) getValue());
      return cal;
   }

   public int getYear()
   {
      return getCalendar().get (Calendar.YEAR);
   }

   public int getMonth()
   {
      return getCalendar().get (Calendar.MONTH);
   }

   public int getDay()
   {
      return getCalendar().get (Calendar.DATE);
   }

   public int getHour()
   {
      return getCalendar().get (Calendar.HOUR);
   }

   public int getMinute()
   {
      return getCalendar().get (Calendar.MINUTE);
   }

   public Date getValueAsDate()
   {
      return (Date) getValue();
   }

   public String getValueAsString()
   {
      return dateFormat.format (getDate());
   }

   @Override
   public void setInitialValue (final Object value)
   {
      if (value instanceof Date)
         super.setInitialValue (value);
      else if (value != null && !value.toString().trim().equals (""))
      {
         Date date = parseDate (value.toString());
         if (date != null)
            super.setInitialValue (date);
      }
      else
         super.setInitialValue (null);
   }
   
   @Override
   public void setValue (final Object value)
   {
      if (value instanceof Date)
         model.setValue (value);
   }

   public void setValue (final String value)
   {
      Date date = parseDate (value);
      if (date != null)
         setValue (date);
   }

   // This is a hack to avoid what appears to be a bug in the
   // Microsoft Jet driver.  The date format returned by the ResultSet
   // getString method contains dash separators, but the expected
   // value uses slash separators.  There may be a better way to fix
   // this.
   private Date parseDate (final String value)
   {
      Date date = null;

      try
      {
         date = dateFormat.parse (value);
      }
      catch (ParseException x) { }

      if (date == null && alternateFormat != null)
      {
         try
         {
            date = alternateFormat.parse (value);
         }
         catch (ParseException x) { }
      }

      if (date == null)
      {
         System.out.println ("CalendarItem unparsable date: " + value);
         Thread.dumpStack();
      }

      return date;
   }

   public void setFormat (final String format)
   {
      dateFormat = new SimpleDateFormat (format);
      if (format.indexOf ("/") > 0)
         alternateFormat = new SimpleDateFormat (format.replaceAll ("/", "-"));
      else if (format.indexOf ("-") > 0)
         alternateFormat = new SimpleDateFormat (format.replaceAll ("-", "/"));
   }

   @Override
   public JComponent getComponent()
   {
      return (panel);
   }

   private JFormattedTextField getEditorComponent()
   {
      JComponent c = spinner.getEditor();
      return ((JSpinner.DefaultEditor) c).getTextField();
   }
   
   @Override
   public void setEnabled (final boolean enabled)
   {
      spinner.setEnabled (enabled);
      if (calButton != null)
         calButton.setEnabled (enabled);
   }
   
   @Override
   public void setToolTipText (final String tip)
   {
      getEditorComponent().setToolTipText (tip);
      if (calButton != null)
         calButton.setToolTipText (tip);
   }

   @Override
   public boolean hasChanged()
   {
      if (getInitialValue() == null)
         return true;
      return !getInitialValue().equals (model.getDate());
   }

   public void stateChanged (final ChangeEvent e)
   {
      fireValueChanged();
   }

   @Override
   protected void showChangeStatus()
   {
      // set the foreground color based on whether or not the value changed
      if (hasChanged())
         getEditorComponent().setForeground (Color.blue);
      else
         getEditorComponent().setForeground (Color.black);
   }
   
   public void setBackground (final Color c)
   {
      getEditorComponent().setBackground (c);
   }

   class MenuButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         calendar.setVisible (true);
      }
   }

   protected void createCalendar (final boolean modalPopup)
   {
      calendar = new JDialog ((Frame) null, getLabel(), modalPopup);
      calendar.addWindowListener (new WindowAdapter() {
         @Override
         public void windowClosing (final WindowEvent evt)
         {
            Window win = evt.getWindow();
            win.setVisible (false);
            win.dispose();
         }
      });
      calendar.getContentPane().add (calendarPopup);
      calendar.pack();
      ComponentTools.centerComponent (calendar);
   }

   // implement PropertyChangeListener for the popup calendar

   public void propertyChange (final PropertyChangeEvent event)
   {
      Date selectedDate = calendarPopup.getSelectedDate();
      setValue (selectedDate); // calls fireValueChanged();
   }

   public static void main (final String[] args)
   {
      // prompt for a date
      CalendarItem item = new CalendarItem();
      // enable the pop-up calendar (since the format includes the date)
      item.enablePopup (true, false);
      item.test();

      // prompt for a time
      item = new CalendarItem ("Enter Time", "hh:mm a");
      item.test();

      // prompt for a duration
      item = new CalendarItem ("Enter Duration", "mm'm' ss's'");
      item.setToolTipText ("Enter duration");
      item.test();
   }
}
