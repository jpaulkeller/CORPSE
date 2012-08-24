package gui.db;

import java.awt.Color;

import javax.swing.JTable;

public interface CellDecorator
{
   Color getBackgroundColor (final JTable table, final int row, final int col,
                             final Object value);
   
   Color getBorderColor  (final JTable table, final int row, final int col,
                          final Object value);
   
   String getToolTipText (final JTable table, final int row, final int col,
                          final Object value);
}
