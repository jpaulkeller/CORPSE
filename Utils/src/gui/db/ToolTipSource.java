package gui.db;

import javax.swing.JTable;

public interface ToolTipSource
{
   String getToolTipText (final JTable table,
                          final int row, final int col,
                          final Object value);
}
