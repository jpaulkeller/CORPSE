package gui.form.valid;

import gui.ComponentTools;
import gui.form.FormItem;
import gui.form.TextItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * A SequenceValidator represents the logical concatenation of other
 * validators. It starts out with an ordered collection of validators
 * and applies each one, until one fails, or until all validate the
 * data.
 *
 * This class allows one data source to have multiple "daisy-chained"
 * validators.  These validators are tested in order, so that more
 * complex (slower) validation will only be done if easier (faster)
 * validation succeeds.
 */

public class SequenceValidator extends ValidationAdapter
{
   private static final long serialVersionUID = 5;

   private List<Validator> sequence;

   public SequenceValidator() { }

   public SequenceValidator (final List<Validator> sequence)
   {
      this.sequence = sequence;
   }

   public void addValidator (final Validator v)
   {
      if (sequence == null)
         sequence = new ArrayList<Validator>();
      sequence.add (v);
   }

   public void removeValidator (final Validator v)
   {
      if (sequence != null)
         sequence.remove (v);
   }

   @Override
   public boolean isValid (final Object value)
   {
      if (sequence == null)
         return true;

      for (Validator v : sequence)
         if (!v.isValid (value))
            return false;       // return at first failure

      return true;              // all passed
   }

   public static void main (final String[] args) // for testing
   {
      try
      {
         TextItem item = new TextItem ("Sequence Validator Test");

         SequenceValidator v = new SequenceValidator();
         v.addValidator (new NumericValidator());
         v.addValidator (new RegexValidator (".*123.*"));
         item.setValidator (v);

         item.addValueChangeListener (
            new ValueChangeListener()
            {
               public void valueChanged (final ValueChangeEvent e)
               {
                  FormItem comp = (FormItem) e.getSource();
                  System.out.println (comp.getLabel() + ": " + comp.getValue());
               }
            });
         item.getComponent().setPreferredSize (new Dimension (200, 50));

         final JFrame frame = new JFrame (SequenceValidator.class.getName());
         frame.getContentPane().add (item.getComponent());
         
         // do the following on the GUI event-dispatching thread
         SwingUtilities.invokeLater (new Runnable() {
            public void run()
            {
               frame.pack();
               ComponentTools.centerComponent (frame);
               frame.setVisible (true);
            }
         });
      }
      catch (Exception e)
      {
         System.err.println (e + ": " + e.getMessage());
         e.printStackTrace (System.err);
      }
   }
}
