package gui.db;

public class ColumnPanel { }

/*
import gui.wizard.WizardPanel;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import db.SQL;
import db.SQLU;

public class ColumnPanel extends WizardPanel implements Observer
{
   private static final long serialVersionUID = 1;
   // maps data model index to selected column name in target table
   private Map<Integer, String> mappedColumns;
   private ColumnChanger cc = null;

   private String connName;
   private String tableName;

   public ColumnPanel (final TableModel data)
   {
      mappedColumns = new LinkedHashMap<Integer, String>();
      
      cc = new ColumnChanger (data);
      add (cc.getPanel(), BorderLayout.CENTER);
      cc.addObserver (ColumnPanel.this);
   }

   public void update (final String destConn, final String destTable)
   {
      if (!destConn.equals (connName) || !destTable.endsWith (tableName))
      {
         connName = destConn;
         tableName = destTable;
         cc.setDestinationTable (connName, tableName);
      }
   }
   
   public void update (final Observable observable, final Object arg)
   {
      enableFinish();
   }
   
   @Override
   public void onEntry()
   {
      wiz.enablePrev (true);
      enableFinish();
   }
   
   private void enableFinish()
   {
      mappedColumns.clear();

      JTable tbl = cc.getView();
      TableColumnModel columnModel = tbl.getColumnModel ();
      int count = columnModel.getColumnCount();
      
      // make sure at least one column has been mapped to the DBMS table
      for (int col = 0; col < count; col++)
      {
         TableColumn column = columnModel.getColumn (col);
         if (!column.getIdentifier().equals (ColumnChanger.UNMAPPED))
            mappedColumns.put (col, column.getIdentifier().toString());
      }
      
      wiz.enableFinish (!mappedColumns.isEmpty());
   }

   @Override
   public void onFinish()
   {
      // TBD: update preferences for this file
      
      // TBD: use a progress bar here
      TableModel model = cc.getModel();
      SQL sql = null;
      try
      {
         sql = new SQL (connName);
         insertIntoDatabase (sql, model);
      }
      catch (SQLException x)
      {
         JOptionPane.showMessageDialog
         (wiz.getWindow(), x.getMessage(),
          "Import Failed", JOptionPane.ERROR_MESSAGE);
      }
      finally
      {
         if (sql != null) sql.close();
      }
      
      if (file != null)
      {
         String message = "Import from " + file.getName() +
            " to " + connName + ":" + tableName + " complete.\n" +
            model.getRowCount() + " records were added.";
         JOptionPane.showMessageDialog
         (wiz.getWindow(), message,
               "Import Complete", JOptionPane.INFORMATION_MESSAGE);
      }
   }

   private void insertIntoDatabase (final SQL sql, final TableModel model)
   {
      DatabaseSubset ds = new DatabaseSubset (connName, tableName, true); 

      // create SQL
      StringBuilder prefix = 
         new StringBuilder ("insert into " + tableName + " (");
      Iterator<String> columns = mappedColumns.values().iterator();
      while (columns.hasNext())
      {
         prefix.append (columns.next());
         if (columns.hasNext())
            prefix.append (", ");
      }
      prefix.append (") values (");
      
      Collection<String> batch = new ArrayList<String>();
      StringBuilder insert = new StringBuilder();
      for (int row = 0, count = model.getRowCount(); row < count; row++)
      {
         insert.setLength (0);
         insert.append (prefix);

         Iterator<Entry<Integer, String>> 
            entries = mappedColumns.entrySet().iterator();
         while (entries.hasNext())
         {
            Entry<Integer, String> entry = entries.next();
            int col = entry.getKey();
            Object value = model.getValueAt (row, col);
            insert.append (SQLU.toSQL (ds, entry.getValue(), value));
            if (entries.hasNext())
               insert.append (", ");
         }
         insert.append (");");

         batch.add (insert.toString());
         if (batch.size() == 50 || row == count - 1)
         {
            sql.execute (batch);
            batch.clear();
         }
      }
   }
}
*/