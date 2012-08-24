package gui.form;

import gui.comp.TipComboBox;
import gui.form.valid.NumericValidator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * MeasurementItem objects are numeric items which provide a JComboBox
 * of different units of measurement.  Changing the JComboBox will
 * convert the numeric value to the new unit type.
 *
 * Caution: The getValue() method will return the contents of item (in
 * whatever the current unit of measurement is). Similarly, the
 * setValue() expects the value to be in the current units.  You can
 * use getStandardValue() and setStandardValue() to force standard
 * units.
 *
 * The standard can be changed by calling setStandard(), and accessed
 * by calling getStandard(). This is not related to the UnitOfMeasure
 * object's base value; they may or may not be the same. */

public class MeasurementItem extends NumericItem
{
   private static final long serialVersionUID = 15;

   private JPanel miPanel;
   private TipComboBox unitComboBox;
   private UnitOfMeasure unitOfMeasure;
   private String standard;             // standard unit type
   private String selectedUnits;        // currently selected unit type
   private boolean autoConvert = true;  // auto-convert value when units change
   private double round;                // e.g., 100 rounds to hundredths
   private boolean useAbbrev;           // true to use unit keys, not names

   public MeasurementItem()
   {
      super();
   }

   public MeasurementItem (final String label)
   {
      super (label);
   }

   public MeasurementItem (final String label, final String value)
   {
      super (label, value);
   }

   public MeasurementItem (final String label, final double value)
   {
      this (label, value + "");
   }

   public MeasurementItem (final String label, final double value, 
                           final UnitOfMeasure uom)
   {
      this (label, value);
      setUnitOfMeasure (uom);
   }

   @Override
   public JComponent getComponent()
   {
      return miPanel;
   }

   @Override
   public void setToolTipText (final String tip)
   {
      super.setToolTipText (tip);
      unitComboBox.setToolTipText (tip);
   }

   @Override
   public void setEditable (final boolean state)
   {
      super.setEditable (state);
      super.getComponent().setEnabled (state);
   }

   public String getUnits()
   {
      String abbrev = unitComboBox.getSelectedItem().toString();
      return useAbbrev ? abbrev : unitOfMeasure.getUnit (abbrev);
   }

   public String getStandard()
   {
      return standard;
   }

   public String getStandardName()
   {
      return unitOfMeasure.getName (standard);
   }

   public void setStandard (final String standard)
   {
      this.standard = standard;
      this.selectedUnits = standard;
      setSelected (selectedUnits);
   }

   public void setInitialUnit (final String unit)
   {
      setSelected (unit);
   }

   protected void setSelected (final String unit)
   {
      unitComboBox.setSelectedItem (useAbbrev ? unit : unitOfMeasure.getName (unit));
   }

   /** Convert the value into the standard unit type before returning it. */

   public Object getStandardValue()
   {
      Object obj = getValue();
      if (obj != null && !obj.equals (""))
         obj = "" + getStandardValueAsDouble();
      return obj;
   }

   /** Convert the value into the standard unit type before returning it. */

   public double getStandardValueAsDouble()
   {
      return unitOfMeasure.convert (getValueAsDouble(), selectedUnits, standard);
   }

   /** Convert the value into the current unit type before applying it. */

   public void setInitialStandardValue (final Object standardValue)
   {
      if (standardValue != null && !standardValue.equals (""))
         setInitialStandardValue (Double.parseDouble (standardValue.toString()));
      else
         setInitialValue (null);
   }

   public void setInitialStandardValue (final double standardValue)
   {
      double dbl = unitOfMeasure.convert (standardValue, standard, selectedUnits);
      setInitialValue (dbl);
   }

   public void setStandardValue (final Object standardValue)
   {
      if (standardValue == null || standardValue.equals (""))
      {
         updateModel (null);
         super.setValue (null);
      }
      else if (standardValue instanceof Double)
         setStandardValue (((Double) standardValue).doubleValue());
      else
      {
         try
         {
            double v = new Double (standardValue.toString()).doubleValue();
            setStandardValue (v);
         }
         catch (NumberFormatException x)
         {
            updateModel (null);
         }
      }
   }

   public void setStandardValue (final double standardValue)
   {
      double dbl = unitOfMeasure.convert (standardValue, standard, selectedUnits);
      setValue (dbl);
   }

   @Override
   public void setInitialValue (final double value)
   {
      double v = value;
      if (round != 0)
         v = Math.round (value * round) / round;
      super.setInitialValue (v);
   }

   @Override
   public void setValue (final double value)
   {
      double v = value;
      if (round != 0)
         v = Math.round (value * round) / round;
      super.setValue (v);
   }

   /**
    * Override validation methods to use the standard value. This is
    * necessary so that if the range is 0 to 50 (km), 500 (meters)
    * will be valid. */

   @Override
   public void setRange (final Range range)
   {
      if (!(getValidator() instanceof MeasurementValidator))
         setValidator (new MeasurementValidator());
      ((NumericValidator) getValidator()).setRange (range);
   }

   @Override
   public boolean setRange (final String minimum, final String maximum)
   {
      if (!(getValidator() instanceof MeasurementValidator))
         setValidator (new MeasurementValidator());
      return ((NumericValidator) getValidator()).setRange (minimum, maximum);
   }

   @Override
   public boolean isValid()
   {
      return ((getValidator() == null) ? true :
              getValidator().isValid (getStandardValue()));
   }

   class MeasurementValidator extends NumericValidator
   {
      private static final long
         serialVersionUID = MeasurementItem.serialVersionUID;

      @Override
      public boolean isValid (final Object value)
      {
         if (value == null || !value.equals (getValue()))
            return super.isValid (value);
         return super.isValid (getStandardValue());
      }
   }

   /**
    * Sending 2 will cause the value to be rounded to 2 decimal places
    * whenever it is set.  For example, x.123 would become x.12. */

   public void setRound (final int decimalPlaces)
   {
      this.round = Math.pow (10, decimalPlaces);
   }

   public void setAutoConvert (final boolean state)
   {
      this.autoConvert = state;
   }

   public void useAbbreviations (final boolean on)
   {
      this.useAbbrev = on;
   }

   public void setUnitOfMeasure (final UnitOfMeasure uom)
   {
      this.unitOfMeasure = uom;
      if (this.standard == null)
      {
         this.standard = uom.getStandard(); // default value
         this.selectedUnits = standard;
      }

      Set<String> units = useAbbrev ? uom.getUnits() : uom.getUnitNames();
      unitComboBox = new TipComboBox (units);
      unitComboBox.addActionListener (new UnitChanger());
      setSelected (selectedUnits);

      miPanel = new JPanel (new BorderLayout());
      miPanel.add (super.getComponent(), BorderLayout.CENTER);
      miPanel.add (unitComboBox, BorderLayout.EAST);
   }

   class UnitChanger implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         String newUnits = getUnits();

         // check for null so we don't insert 0
         if (isValid())
         {
            double value = getValueAsDouble();
            if (autoConvert)
            {
               value = unitOfMeasure.convert (value, selectedUnits, newUnits);
               selectedUnits = newUnits;
               // converting the value does not change it
               if (hasChanged())
                  setValue (value);
               else
                  setInitialValue (value);
            }
            else if (!newUnits.equals (selectedUnits))
            {
               selectedUnits = newUnits;
               setValue (value);
            }
         }
         else
            selectedUnits = newUnits;
      }
   }

   @Override
   public String convertToHTML()
   {
      StringBuffer buf = new StringBuffer();

      buf.append ("<tr>\n");
      String lbl = (getLabel() != null) ? getLabel() : "&nbsp;";
      buf.append (" <td>" + lbl + "</td>\n");
      buf.append (" <td align=right><b>" + getValue() + "</b> " +
                  selectedUnits + "</td>\n");
      buf.append ("</tr>\n");

      return buf.toString();
   }
}
