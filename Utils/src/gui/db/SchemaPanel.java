package gui.db;

import gui.wizard.Wizard;

import java.lang.reflect.Method;

public class SchemaPanel
{
   // private Wizard wiz;
   // private Object callback;
   // private Method onNextMethod;
   
   public SchemaPanel (final Wizard wiz, 
                       final Object callback,
                       final Method onNextMethod)
   {
      // this.wiz = wiz;
      // this.callback = callback;
      // this.onNextMethod = onNextMethod;
   }
}

/*
import gui.wizard.WizardPanel;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Map.Entry;

import javax.mail.Message;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import db.SQL;
import db.SQLU;

public class SchemaPanel extends WizardPanel implements Observer
{
   private SchemaEditor se;
   private TableModel data;

   public SchemaPanel()
   {
      se = new SchemaEditor();
      se.addObserver (SchemaPanel.this);
      add (se.getPanel (), BorderLayout.CENTER);
   }

   public void update (final TableModel inputTable)
   {
      data = inputTable;
      String destTable = se.getTableName();
      if (destTable == null || destTable.equals (""))
         se.setTable (inputTable, 1); // skip the Line Number column
   }

   public void update (final Observable observable, final Object arg)
   {
      onEntry();             // reset button state
   }

   @Override
   public void onEntry()
   {
      wiz.enablePrev (true);
      
      String connName = se.getConnection();
      String tableName = se.getTableName();
      if (se.getColumnNames() != null)
      {
         wiz.enableNext (tableName != null &&
                      !se.getColumnNames().isEmpty() && // check schema
                      SQL.exists (connName, tableName.toUpperCase()));
         
         if (tableName != null && 
             SQL.exists (connName, tableName.toUpperCase()) &&
             data == null)
         {
            wiz.dispose();
            new Message("Database Table Creation Wizard", "Table " + 
                  connName + ":" + tableName + " was created");
         }
      }
   }

   @Override
   public void onNext()
   {
      String connName = se.getConnection();
      String tableName = se.getTableName().toUpperCase();
      se.loadSchema (connName, tableName);
      
      List<String> columnNames = se.getColumnNames();
      loadMapPanel (data, connName, tableName, columnNames);         
   }
}
*/
