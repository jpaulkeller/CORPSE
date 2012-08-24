package gui.form;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * SpinnerItem objects are JSpinner objects for use in forms designed to
 * hold infinite (generated) data (numbers, dates, lists).
 */
public class SpinnerItem extends FormItemAdapter implements ChangeListener
{
   private static final long serialVersionUID = 6;

   public static final int ARROW_WIDTH = 28;  //account for arrow size

   private JSpinner spinner;
   private FontMetrics fontMetrics;

   /**
    * SpinnerItem Constructor - constructs a JSpinner with a default
    * SpinnerNumberModel.
    */
   public SpinnerItem()
   {
      this (new SpinnerNumberModel());
   }

   /**
    * SpinnerItem Constructor given a label - constructs a JSpinner with a
    * default SpinnerNumberModel.
    */
   public SpinnerItem (final String label)
   {
      this (label, new SpinnerNumberModel());
   }

   public SpinnerItem (final Object[] objects)
   {
      this (null, objects);
   }

   public SpinnerItem (final String label, final Object[] objects)
   {
      this (label, new SpinnerListModel (objects));
   }

   public SpinnerItem (final SpinnerModel model)
   {
      this (null, model);
   }

   public SpinnerItem (final String label, final SpinnerModel model)
   {
      spinner = new JSpinner();
      fontMetrics = spinner.getFontMetrics (spinner.getFont());

      setModel (model);
      setLabel (label);
   }

   public SpinnerModel getModel()
   {
      return spinner.getModel();
   }

   public void setModel (final SpinnerModel model)
   {
      if (model != null)
      {
         spinner.setModel (model);
         setInitialValue (getValue());
         resize();
         model.addChangeListener (this);
      }
   }

   public void setModel (final Object[] objects)
   {
      spinner.setModel (new SpinnerListModel (objects));
   }

   protected void resize()
   {
      int height = fontMetrics.getHeight() + 14;
      if (spinner.getModel() instanceof SpinnerListModel)
         spinner.setPreferredSize (new Dimension (getWidth(), height));
   }

   public int getWidth()
   {
      int width = 0;
      SpinnerListModel model = (SpinnerListModel) spinner.getModel();
      if (model != null)
      {
         String largestString = "";
         for (Object val : model.getList())
            if (val != null &&
                val.toString().length() > largestString.length())
               largestString = val.toString();

         width = fontMetrics.stringWidth (largestString) + ARROW_WIDTH;
      }
      return width;
   }

   @Override
   public JComponent getComponent()
   {
      return (spinner);
   }

   @Override
   public void setValue (final Object value)
   {
      SpinnerModel model = spinner.getModel();
      if (model != null)
         model.setValue (value);
   }

   @Override
   public Object getValue()
   {
      Object value = null;
      SpinnerModel model = spinner.getModel();
      if (model != null)
         value = model.getValue();

      return value;
   }

   /** Notifies any ValueChangeListeners when the value changes. */

   public void stateChanged (final ChangeEvent e)
   {
      // set the foreground color based on whether or not the value changed
      JComponent comp = spinner.getEditor();
      if (comp instanceof JSpinner.DefaultEditor)
         comp = ((JSpinner.DefaultEditor) comp).getTextField();
      comp.setForeground (hasChanged() ? Color.blue : Color.black);
      fireValueChanged();
   }

   // TBD: support filters and validators

   public static void main (final String[] args)
   {
      // SpinnerItem containing a List Model
      SpinnerItem listSpinner = new SpinnerItem
         ("List", new Object[] { "January", "February", "March",
                                 "April", "May", "June",
                                 "July", "August", "September",
                                 "October", "November", "December" });

      listSpinner.setToolTipText ("List Spinner");
      listSpinner.addValueChangeListener (new ValueChangeListener() {
         public void valueChanged (final ValueChangeEvent e)
         {
            FormItem item = (FormItem) e.getSource();
            System.out.println (item.getLabel() + ": " + item.getValue());
         } 
      });

      // SpinnerItem containing an infinite Number Model
      SpinnerItem numSpinner = new SpinnerItem ("Number");
      numSpinner.setToolTipText ("Number Spinner");
      numSpinner.addValueChangeListener (new ValueChangeListener() {
         public void valueChanged (final ValueChangeEvent e)
         {
            FormItem item = (FormItem) e.getSource();
            System.out.println (item.getLabel() + ": " + item.getValue());
         } 
      });

      // SpinnerItem containing a Date Model
      SpinnerItem dateSpinner =
         new SpinnerItem ("Date", new SpinnerDateModel());
      dateSpinner.setToolTipText ("Date Spinner");
      dateSpinner.addValueChangeListener (new ValueChangeListener() {
         public void valueChanged (final ValueChangeEvent e)
         {
            FormItem item = (FormItem) e.getSource();
            System.out.println (item.getLabel() + ": " + item.getValue());
         }
      });

      JPanel panel = new JPanel (new BorderLayout());
      panel.add (listSpinner.getComponent(), BorderLayout.NORTH);
      panel.add (numSpinner.getComponent(),  BorderLayout.CENTER);
      panel.add (dateSpinner.getComponent(), BorderLayout.SOUTH);

      ComponentTools.open (panel, "SpinnerItem");
   }
}
