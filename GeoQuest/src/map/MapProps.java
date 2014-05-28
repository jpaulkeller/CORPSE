package map;

import gui.ComponentTools;
import gui.form.NumericSpinner;
import gui.form.Range;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MapProps extends LinkedHashMap<String, String>
{
   private static final long serialVersionUID = 1L;
   
   private JPanel panel;
   private NumericSpinner rows, cols;

   private Component getComponent()
   {
      if (panel == null)
      {
         rows = new NumericSpinner ("Rows");
         rows.setRange (new Range (1, 100));
         rows.setStepSize (5);

         cols = new NumericSpinner ("Columns");
         cols.setRange (new Range (1, 100));
         cols.setStepSize (5);

         JPanel grid = new JPanel (new GridLayout (0, 1));
         grid.add (rows.getTitledPanel());
         grid.add (cols.getTitledPanel());
         
         panel = new JPanel (new BorderLayout());
         panel.add (grid, BorderLayout.CENTER);
      }

      return panel;
   }
   
   public int open (final Component owner)
   {
      getComponent();
      rows.setInitialValue (Integer.parseInt (get ("Rows")));
      cols.setInitialValue (Integer.parseInt (get ("Columns")));

      int result = JOptionPane.showOptionDialog (owner, panel, "Map Properties", 
                                                 JOptionPane.OK_CANCEL_OPTION, 
                                                 JOptionPane.INFORMATION_MESSAGE, null,
                                                 null, null);
      if (result == JOptionPane.OK_OPTION)
      {
         put ("Rows", rows.getValueAsInt() + "");
         put ("Columns", cols.getValueAsInt() + "");
      }
      return result;
   }
   
   public static void main (final String[] args)
   {
      MapProps mapProps = new MapProps();
      ComponentTools.open (mapProps.getComponent(), "MapProps");
   }
}
