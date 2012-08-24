package words;
import javax.swing.table.DefaultTableModel;

public class Model extends DefaultTableModel
{
   private static final long serialVersionUID = 1L;

   @Override
   public Class<?> getColumnClass (int c)
   {
      if (getRowCount() > 0)
      {
         Object value = getValueAt (0, c); 
         if (value != null) 
            return value.getClass();
      }
      return null;
   }
}
