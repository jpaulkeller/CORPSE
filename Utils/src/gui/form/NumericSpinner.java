package gui.form;

import javax.swing.SpinnerNumberModel;

/**
 * NumericSpinner objects are SpinnerItem objects with additional
 * support for numeric data. */

public class NumericSpinner extends SpinnerItem
{
   private static final long serialVersionUID = 2;

   private SpinnerNumberModel model;

   public NumericSpinner()
   {
      super();
      this.model = (SpinnerNumberModel) getModel();
      setFilter ("0123456789");
   }
   
   public NumericSpinner (final String label)
   {
      this();
      setLabel (label);
   }
   
   public NumericSpinner (final String label, final int value)
   {
      this (label);
      setInitialValue (value);
   }
   
   public void setFilter (final String charactersAllowed)
   {
      setFilter (TextFilter.getFilter (charactersAllowed));
   }

   public void setValue (final int value)
   {
      super.setValue (Integer.valueOf (value));
   }
   
   public void setInitialValue (final int value)
   {
      super.setInitialValue (Integer.valueOf (value));
   }
   
   public int getValueAsInt()
   {
      return ((Integer) getValue()).intValue();
   }
   
   public void setStepSize (final int step)
   {
      model.setStepSize (Integer.valueOf (step));
   }
   
   public void setRange (final Range range)
   {
      model.setMinimum (Integer.valueOf ((int) range.getMin()));
      model.setMaximum (Integer.valueOf ((int) range.getMax()));
   }
   
   @Override
   public String convertToHTML()
   {
      StringBuffer buf = new StringBuffer();

      buf.append ("<tr>\n");
      String lbl = (getLabel() != null) ? getLabel() : "&nbsp;";
      buf.append (" <td>" + lbl + "</td>\n");
      // TBD: may need to format based on user-specification
      buf.append (" <td align=right><b>" + getValue() + "</b></td>\n");
      buf.append ("</tr>\n");
      
      return (buf.toString());
   }

   public static void main (final String[] args) // for testing
   {
      NumericSpinner n = new NumericSpinner ("NumericSpinner (by 5)", 20);
      n.setStepSize (5);
      n.test();

      NumericSpinner hour = new NumericSpinner ("NumericSpinner (0-23)", 1);
      hour.setRange (new Range (0, 23));
      hour.setToolTipText ("Enter a number from 0 to 23");
      hour.setValue (11);
      hour.test();
   }
}
