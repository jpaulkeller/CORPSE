package gui.db;

import java.awt.Color;

import javax.swing.JTable;

public abstract class CellDecoratorAdapter implements CellDecorator
{
   public Color getBackgroundColor (final JTable table, final int row, final int col,
                                    final Object value)
   {
      return null;
   }

   public Color getBorderColor (final JTable table, final int row, final int col,
                                final Object value)
   {
      return null;
   }

   public String getToolTipText (final JTable table, final int row, final int col, 
                                 final Object value)
   {
      return null;
   }
}
