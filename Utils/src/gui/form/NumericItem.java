package gui.form;

import gui.form.valid.NumericValidator;

import java.text.DecimalFormat;

import javax.swing.SwingConstants;

/**
 * NumericItem objects are simple one-line form text items for
 * entering numeric data.
 */
public class NumericItem extends TextItem
{
   private static final long serialVersionUID = 17;

   // NumericItem objects require separate models for the data model and the
   // view, so the current value is not lost as changes are made.
   private Double currentValue; // this is the model

   private DecimalFormat decimalFormat = new DecimalFormat ("#.###"); // default

   public NumericItem()
   {
      super (10);
      setValidator (new NumericValidator());
   }
   
   public NumericItem (final String label)
   {
      super (label, 10);
      setValidator (new NumericValidator());
   }
   
   public NumericItem (final int size)
   {
      super (size);
      setValidator (new NumericValidator());
   }
   
   public NumericItem (final String label, final String value)
   {
      super (label, 10, value);
      setValidator (new NumericValidator());
   }
   
   public NumericItem (final String label, final String value, final int size)
   {
      super (label, size, value);
      setValidator (new NumericValidator());
   }
   
   public NumericItem (final String label, final double value)
   {
      super (label, 10, Double.valueOf (value));
      setValidator (new NumericValidator());
   }
   
   public NumericItem (final String label, final double value, final int size)
   {
      super (label, size, Double.valueOf (value));
      setValidator (new NumericValidator());
   }
   
   @Override
   void initialize()            // called by super's constructors
   {
      super.initialize();
      alignRight();                // default right alignment
      setFilter ("-.0123456789E"); // allow E for exponent notation
   }

   public void alignRight()
   {
      getTextField().setHorizontalAlignment (SwingConstants.RIGHT);
   }
   
   public void alignLeft()
   {
      getTextField().setHorizontalAlignment (SwingConstants.LEFT);
   }
   
   @Override
   public void setFilter (final String charactersAllowed)
   {
      boolean allowReals = allowReals();
      super.setFilter (charactersAllowed);
      if (allowReals != allowReals()) // if "type" was changed
      {
         double value = getValueAsDouble();
         if (hasChanged() || (allowReals && (Math.round (value) != value)))
            setValue (value);   // round value, and indicate change
         else
            setInitialValue (value);
      }
   }

   public void setFormat (final String format)
   {
      decimalFormat = format != null ? new DecimalFormat (format) : null;
   }

   boolean allowReals()
   {
      if (getFilter() == null)
         return true;
      if (getFilter() instanceof TextFilter)
      {
         String charactersAllowed = ((TextFilter) getFilter()).getCharacters();
         if (charactersAllowed.indexOf ('.') >= 0)
            return true;
      }
      else if (getFilter().process ('.') > 0)
         return true;
      return false;
   }
   
   public void setInitialValue (final double value)
   {
      super.setInitialValue (Double.valueOf (value));
   }

   public void setInitialValue (final Double value)
   {
      super.setInitialValue (value);
   }
   
   @Override
   public void setInitialValue (final Object value)
   {
      if (value != null && !value.toString().trim().equals (""))
         super.setInitialValue (Double.parseDouble (value.toString()));
      else
         super.setInitialValue (null);
   }
   
   private boolean changing; // true while setValue() is executing
   
   @Override
   public void setValue (final Object value)
   {
      this.changing = true;

      updateModel (value);

      if (currentValue == null)
         super.setValue (null);
      else if (!allowReals())
         super.setValue ("" + currentValue.longValue());
      else if (decimalFormat == null)
         super.setValue (currentValue);
      else
         super.setValue (decimalFormat.format (currentValue.doubleValue()));

      this.changing = false;

      fireValueChanged();
   }

   public void setValue (final double value)
   {
      setValue (Double.valueOf (value));
   }
   
   protected void updateModel (final Object value)
   {
      if (value == null || value.equals (""))
         this.currentValue = null;
      else if (value instanceof Double)
         this.currentValue = (Double) value;
      else
      {
         try
         {
            this.currentValue = Double.parseDouble (value.toString());
         }
         catch (NumberFormatException x)
         {
            this.currentValue = null;
         }
      }
   }
      
   @Override
   protected void valueChanged()
   {
      if (!changing)
      {
         updateModel (super.getValue());
         super.valueChanged();
      }
   }

   /** Overrides TextItem getValue() to return our local model. */

   @Override
   public Object getValue()
   {
      return currentValue;
   }
         
   // convenience methods for accessing the value

   public double getValueAsDouble()
   {
      if (currentValue == null)
         return 0;
      return currentValue.doubleValue();
   }
   
   public float getValueAsFloat()
   {
      return (float) getValueAsDouble();
   }

   public long getValueAsLong()
   {
      return Math.round (getValueAsDouble());
   }
   
   public int getValueAsInt()
   {
      return (int) Math.round (getValueAsDouble());
   }
   
   @Override
   public boolean hasChanged()
   {
      if (getInitialValue() == null)
         return currentValue != null;
      return !getInitialValue().equals (currentValue);
   }

   // validation methods

   public void setRange (final Range range)
   {
      ((NumericValidator) getValidator()).setRange (range);
   }
   
   public boolean setRange (final String minimum, final String maximum)
   {
      return ((NumericValidator) getValidator()).setRange (minimum, maximum);
   }
   
   public Range getRange()
   {
      return ((NumericValidator) getValidator()).getRange();
   }
   
   @Override
   public String convertToHTML()
   {
      StringBuffer buf = new StringBuffer();

      buf.append ("<tr>\n");
      String lbl = (getLabel() != null) ? getLabel() : "&nbsp;";
      buf.append (" <td>" + lbl + "</td>\n");
      // TBD: may need to format based on filter type (or user-specification)
      buf.append (" <td align=right><b>" + super.getValue() + "</b></td>\n");
      buf.append ("</tr>\n");
      
      return buf.toString();
   }

   public static void main (final String[] args) // for testing
   {
      NumericItem real = new NumericItem ("NumericItem (real)");
      real.setFormat ("#.##");
      real.setInitialValue (Math.PI);
      real.setNullValidity (true);
      real.test();

      NumericItem hour = new NumericItem ("NumericItem (0-23)", 1);
      hour.setFilter ("0123456789");
      hour.setRange (new Range (0, 23));
      hour.setToolTipText ("Enter a number from 0 to 23");
      hour.test();
   }
}
