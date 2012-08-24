package gui.form;

import gui.comp.SliderWheel;
import gui.form.valid.StatusEvent;
import gui.form.valid.Validator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A FormItem for entering durations.  Currently limited to seconds,
 * minutes, and hours. */

public class DurationItem extends FormItemAdapter
{
   private static final long serialVersionUID = 17;

   public static final List<String> SMH = new ArrayList<String>();
   static
   {
      SMH.add ("seconds");
      SMH.add ("minutes");
      SMH.add ("hours");
   }

   public static final List<String> DWM = new ArrayList<String>();
   static
   {
      DWM.add ("days");
      DWM.add ("weeks");
      DWM.add ("months");
   }

   private Pattern valuePattern; // "# units" (e.g., "5 minutes")
   private JPanel panel;
   private JSlider slider;
   private JLabel sliderLabel;
   private boolean adjusting;
   private JSpinner numSpinner;
   private JSpinner unitSpinner;

   public DurationItem (final String label)
   {
      this (label, false, false);
   }

   /**
    * Constructs a DurationItem.  If userSlider is true, a JSlider
    * will be used instead of the default JSpinner.  If adjusting is
    * also true, this will fire change events as the slider is
    * adjusted (instead of when it's done adjusting). */

   public DurationItem (final String label, 
                        final boolean useSlider,
                        final boolean adjusting)
   {
      this (label, SMH, useSlider, adjusting);
   }

   public DurationItem (final String label, final List<String> units,
                        final boolean useSlider, final boolean adjusting)
   {
      setLabel (label);

      ToolListener toolListener = new ToolListener();

      if (useSlider)
         getSlider (adjusting, toolListener);
      else // use spinner
         getSpinner (toolListener);

      // build the regular expression to match possible values
      StringBuilder unitPattern = new StringBuilder();
      Iterator<String> iter = units.iterator();
      while (iter.hasNext())
      {
         unitPattern.append (iter.next());
         if (iter.hasNext())
            unitPattern.append ("|");
      }
      String pattern = "\\s*([0-9]+)\\s*(" + unitPattern + ")\\s*";
      valuePattern = Pattern.compile (pattern);

      unitSpinner = new JSpinner();
      SpinnerListModel listModel = new SpinnerListModel (units);
      listModel.addChangeListener (toolListener);
      unitSpinner.setModel (listModel);
      unitSpinner.setPreferredSize (new Dimension (75, 25));
      DefaultEditor editor = (DefaultEditor) unitSpinner.getEditor();
      editor.getTextField().setEditable (false);

      panel = new JPanel();
      if (useSlider)
      {
         JPanel left = new JPanel (new BorderLayout());
         left.add (slider, BorderLayout.CENTER);
         left.add (sliderLabel, BorderLayout.EAST);
         panel.setLayout (new BorderLayout());
         panel.add (left, BorderLayout.CENTER);
         panel.add (unitSpinner, BorderLayout.EAST);
      }
      else
      {
         panel.setLayout (new BorderLayout());
         panel.add (numSpinner, BorderLayout.CENTER);
         panel.add (unitSpinner, BorderLayout.EAST);
      }
   }

   private void getSlider (final boolean isAdjusting, final ToolListener toolListener)
   {
      this.adjusting = isAdjusting;

      slider = new SliderWheel (0, 60);
      slider.setMinorTickSpacing (5);
      slider.setMajorTickSpacing (15);
      slider.setPaintTicks (true);
      slider.setPaintLabels (true);
      slider.setValue (60);
      slider.setPreferredSize (new Dimension (180, 45));
      slider.addChangeListener (toolListener);

      sliderLabel = new JLabel (" 60 ");
      Font f = sliderLabel.getFont();
      Font bigger = new Font (f.getName(), f.getStyle(), f.getSize() + 4);
      sliderLabel.setFont (bigger);
      sliderLabel.setHorizontalAlignment (SwingConstants.CENTER);
      sliderLabel.setPreferredSize (new Dimension (25, 20));
   }

   private void getSpinner (final ToolListener toolListener)
   {
      numSpinner = new JSpinner();
      SpinnerNumberModel numModel = new SpinnerNumberModel();
      numSpinner.setModel (numModel);
      numSpinner.setPreferredSize (new Dimension (40, 25));

      numModel.setMinimum (Integer.valueOf (0));
      numModel.addChangeListener (toolListener);

      DefaultEditor editor = (DefaultEditor) numSpinner.getEditor();
      JFormattedTextField textField = editor.getTextField();
      textField.getDocument().addDocumentListener (this);
   }

   @Override public JComponent getComponent()
   {
      return panel;
   }

   @Override public void setEnabled (final boolean enabled)
   {
      if (slider != null)
         slider.setEnabled (enabled);
      if (numSpinner != null)
         numSpinner.setEnabled (enabled);
      unitSpinner.setEnabled (enabled);
   }

   @Override public void setToolTipText (final String tip)
   {
      setToolTipText (tip, tip);
   }

   public void setToolTipText (final String numTip, final String unitTip)
   {
      DefaultEditor editor = (DefaultEditor) unitSpinner.getEditor();
      editor.getTextField().setToolTipText (unitTip);

      if (numSpinner != null)
      {
         editor = (DefaultEditor) numSpinner.getEditor();
         editor.getTextField().setToolTipText (numTip);
      }
      else
         slider.setToolTipText (numTip);
   }

   /** Assumes the value is a string (e.g., "5 minutes"). */

   @Override public void setValue (final Object value)
   {
      if (value != null)
      {
         Matcher matcher = valuePattern.matcher (value.toString());
         if (matcher.matches())
         {
            setNumber (Integer.parseInt (matcher.group (1)));
            setUnit (matcher.group (2));
            showChangeStatus();
         }
      }
   }

   protected int getNumber()
   {
      int num;

      if (numSpinner != null)
      {
         num = ((Integer) numSpinner.getModel().getValue()).intValue();

         // check the text (which may not match the model)
         DefaultEditor editor = (DefaultEditor) numSpinner.getEditor();
         String text = editor.getTextField().getText();
         if (!text.equals (""))
         {
            try { num = Integer.parseInt (text); }
            catch (NumberFormatException x) { }
         }
      }
      else
         num = slider.getValue();

      return num;
   }

   /** Retuns the value as a string (e.g., "5 minutes"). */
   @Override public Object getValue()
   {
      return getNumber() + " " + unitSpinner.getModel().getValue();
   }

   public void setNumber (final int number)
   {
      if (numSpinner != null)
         numSpinner.setValue (Integer.valueOf (number));
      else
      {
         slider.setValue (number);
         sliderLabel.setText (number + "");
      }
   }

   public void setUnit (final String unit)
   {
      unitSpinner.setValue (unit);
   }

   /** Get the value in seconds. Override this to support other units. */

   public long getSeconds()
   {
      long num = getNumber();
      String unit = (String) unitSpinner.getModel().getValue();

      if (unit.equals ("seconds"))
         return num;
      else if (unit.equals ("minutes"))
         return num * SEC_PER_MIN;
      else if (unit.equals ("hours"))
         return num * SEC_PER_HOUR;
      else if (unit.equals ("days"))
         return num * SEC_PER_DAY;
      else if (unit.equals ("weeks"))
         return num * SEC_PER_WEEK;
      else if (unit.equals ("months"))
         return num * SEC_PER_MONTH;
      else if (unit.equals ("years"))
         return num * SEC_PER_YEAR;
      else
      {
         System.out.println ("DurationItem invalid unit: " + unit);
         return -1;
      }
   }

   private static final long SEC_PER_MIN   =  60;
   private static final long SEC_PER_HOUR  =  60 * SEC_PER_MIN;
   private static final long SEC_PER_DAY   =  24 * SEC_PER_HOUR;
   private static final long SEC_PER_WEEK  =   7 * SEC_PER_DAY;
   private static final long SEC_PER_MONTH =  30 * SEC_PER_DAY;
   private static final long SEC_PER_YEAR  = 365 * SEC_PER_DAY;
   
   /** Get the a user-friendly textual value for the given seconds. */

   public static String getText (final long seconds)
   {
      String friendly;

      if ((seconds % SEC_PER_YEAR) == 0) 
         friendly = (seconds / SEC_PER_YEAR) + " years";
      else if ((seconds % SEC_PER_MONTH) == 0)
         friendly = (seconds / SEC_PER_MONTH) + " months";
      else if ((seconds % SEC_PER_WEEK) == 0)
         friendly = (seconds / SEC_PER_WEEK) + " weeks";
      else if ((seconds % SEC_PER_DAY) == 0)
         friendly = (seconds / SEC_PER_DAY) + " days";
      else if ((seconds % SEC_PER_HOUR) == 0)
         friendly = (seconds / SEC_PER_HOUR) + " hours";
      else if ((seconds % SEC_PER_MIN) == 0)
         friendly = (seconds / SEC_PER_MIN) + " minutes";
      else
         friendly = seconds + " seconds";

      return friendly;
   }

   /**
    * Returns the Calendar object representing the date/time that is
    * offset from the system's current date/time by the DurationItem's
    * current value.  In other words, if the current value of this
    * form item is "2 weeks", then this method would return a date
    * that was 2 weeks before (or after) now (depending on the given
    * boolean, which determines the direction of the offset).
    */
   public Calendar getRelative (final boolean before)
   {
      Calendar relative = new GregorianCalendar();
      relative.setTime (new Date()); // current date/time
      int seconds = (int) getSeconds();
      if (before)
         seconds = -seconds;    // go back in time
      relative.add (Calendar.SECOND, seconds);
      return relative;
   }

   class ToolListener implements ChangeListener
   {
      public void stateChanged (final ChangeEvent e)
      {
         boolean fireEvent = true;
         if (e.getSource() instanceof JSlider)
         {
            JSlider slide = (JSlider) e.getSource();
            if (slide.getValueIsAdjusting())
            {
               sliderLabel.setText (slide.getValue() + "");
               fireEvent = adjusting;
            }
         }
         if (fireEvent)
            valueChanged();
      }
   }

   // set the foreground color based on whether or not the value changed
   @Override protected void showChangeStatus()
   {
      if (hasChanged())
      {
         DefaultEditor editor = (DefaultEditor) unitSpinner.getEditor();
         editor.getTextField().setForeground (Color.blue);
         if (numSpinner != null)
         {
            editor = (DefaultEditor) numSpinner.getEditor();
            editor.getTextField().setForeground (Color.blue);
         }
         else
            sliderLabel.setForeground (Color.blue);
      }
      else
      {
         DefaultEditor editor = (DefaultEditor) unitSpinner.getEditor();
         editor.getTextField().setForeground (Color.black);
         if (numSpinner != null)
         {
            editor = (DefaultEditor) numSpinner.getEditor();
            editor.getTextField().setForeground (Color.black);
         }
         else
            sliderLabel.setForeground (Color.black);
      }
   }

   public void setMinimum (final int min)
   {
      if (numSpinner != null)
      {
         SpinnerNumberModel model = (SpinnerNumberModel) numSpinner.getModel();
         model.setMinimum (Integer.valueOf (min));
      }
      else
         slider.setMinimum (min);
   }

   public void setMaximum (final int max)
   {
      if (numSpinner != null)
      {
         SpinnerNumberModel model = (SpinnerNumberModel) numSpinner.getModel();
         model.setMaximum (Integer.valueOf (max));
      }
      else
         slider.setMaximum (max);
   }

   // set the background color based on whether or not the value is valid
   @Override public void stateChanged (final StatusEvent e)
   {
      if (numSpinner != null)
      {
         DefaultEditor editor = (DefaultEditor) numSpinner.getEditor();
         if (e.getStatus())   // if valid
            editor.getTextField().setBackground (Color.white);
         else
            editor.getTextField().setBackground (Validator.INVALID_COLOR);
      }
   }

   public static long getSeconds (final String value)
   {
      // TBD: implement this without creating a DurationItem
      DurationItem item = new DurationItem ("");
      item.setInitialValue (value);
      return item.getSeconds();
   }

   public static void main (final String[] args)
   {
      System.out.println ("getSeconds (\"5 minutes\") => " +
                          DurationItem.getSeconds ("5 minutes"));
      System.out.println();

      DurationItem item = new DurationItem ("Enter Duration");
      item.setInitialValue ("5 minutes");
      item.setToolTipText ("Enter duration", "Select unit of time");
      item.test();

      item = new DurationItem ("Enter Duration", true, true);
      item.setInitialValue ("5 seconds");
      item.setToolTipText ("Select duration", "Select unit of time");
      item.test();

      item = new DurationItem ("Enter Duration", DWM, true, true);
      item.setInitialValue ("10 days");
      item.setToolTipText ("Select duration", "Select unit of time");
      item.test();
   }
}
