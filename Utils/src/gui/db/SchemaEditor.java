package gui.db;

public class SchemaEditor
{
}

/*
import gui.ComponentTools;
import gui.form.ComboBoxItem;
import gui.form.TextItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;
import gui.form.valid.RegexValidator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import model.ObservableDelegate;
import model.ObservableDelegator;
import utils.ImageTools;
import db.SQL;
import db.SQLU;

public class SchemaEditor implements ObservableDelegator, ValueChangeListener
{
   private static final Icon addIcon =
      ImageTools.getIcon ("icons/20/gui/RowNew.gif");
   private static final Icon delIcon =
      ImageTools.getIcon ("icons/20/gui/RowDelete.gif");
   private static final Icon upIcon =
      ImageTools.getIcon ("icons/20/flow/ArrowUp.gif");
   private static final Icon downIcon =
      ImageTools.getIcon ("icons/20/flow/ArrowDown.gif");

   private static final Dimension DIM = new Dimension (30, 30);

   private JPanel panel;
   private JTable view;
   private JButton delButton, upButton, downButton;
   private DefaultTableModel model;
   private ComboBoxItem connBox;
   private TextItem tableItem;
   private JButton loadButton, createButton;
   private ObservableDelegate observable;

   public SchemaEditor()
   {
      panel = new JPanel (new BorderLayout());
      panel.setBorder (BorderFactory.createTitledBorder 
                       ("Select or create the table to hold the imported data"));

      view = new JTable();
      view.getTableHeader().setReorderingAllowed (false);

      Collection<String> connections = SQLU.getConnectionNames();
      connBox = new ComboBoxItem ("Database", connections);
      connBox.setToolTipText ("Select a DBMS Connection");
      connBox.addValueChangeListener (this);

      // TBD: use an editable combo box with the tables from the DBMS
      // TBD: default to file name
      tableItem = new TextItem ("Table Name");
      tableItem.setValidator (new RegexValidator ("[-A-Za-z0-9_]+"));
      tableItem.addValueChangeListener (this);

      JScrollPane scroll = new JScrollPane (view);

      JPanel columnPanel = new JPanel (new BorderLayout());
      columnPanel.add (getButtonPanel(), BorderLayout.WEST);
      columnPanel.add (scroll, BorderLayout.CENTER);
      columnPanel.setBorder (FormattedDataImporter.TABLE_BORDER);

      JPanel upper = new JPanel (new BorderLayout());
      upper.add (connBox.getTitledPanel(), BorderLayout.WEST);
      upper.add (tableItem.getTitledPanel(), BorderLayout.CENTER);

      ActionListener listener = new ButtonListener();
      
      loadButton = new JButton ("Load Schema");
      loadButton.setToolTipText ("Load the table schema into the editor");
      loadButton.addActionListener (listener);
      loadButton.setEnabled (false);

      createButton = new JButton ("Create New Table");
      createButton.setToolTipText ("Create a new table using the current schema");
      createButton.addActionListener (listener);
      createButton.setEnabled (false);

      JPanel controls = new JPanel (new FlowLayout (FlowLayout.LEADING));
      controls.add (loadButton);
      controls.add (createButton);

      panel.add (upper, BorderLayout.NORTH);
      panel.add (columnPanel, BorderLayout.CENTER);
      panel.add (controls, BorderLayout.SOUTH);

      observable = new ObservableDelegate();
   }

   // called when the connection or table name changes
   
   public void valueChanged (final ValueChangeEvent event)
   {
      enableButtons();
      observable.registerChange();
   }

   private void enableButtons()
   {
      String conn = (String) connBox.getValue ();
      String table = tableItem.getValue ().toString().toUpperCase();
      SQL sql = new SQL (conn);
      boolean ok = tableItem.isValid () && tableItem.hasChanged ();
      loadButton.setEnabled (ok && SQL.exists (conn, table));
      if (model != null)
         createButton.setEnabled (ok && model.getRowCount () > 0);
   }
   
   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         String connName = (String) connBox.getValue();
         String tableName = tableItem.getValue().toString().toUpperCase();
         String cmd = e.getActionCommand();
         if (cmd.equals ("Load Schema"))
            loadSchema (connName, tableName);
         else if (cmd.equals ("Create New Table"))
            createTable (connName, tableName);
      }
   }

   public void setTable (final TableModel inputTable, final int skip)
   {
      model = new DefaultTableModel();
      model.addColumn ("Column Name");
      model.addColumn ("Column Type");

      // start at 1 to skip the "Line Number" column
      if (inputTable != null)
         for (int col = skip; col < inputTable.getColumnCount(); col++)
         {
            String columnName = inputTable.getColumnName (col);
            columnName = SQLU.standardizeColumn (columnName);
            String columnType = "VARCHAR";
            if (inputTable instanceof MutableTableModel)
               columnType = ((MutableTableModel) inputTable).getColumnTypeName (col);
            // TBD: combo-box of valid types
            model.addRow (new String[] { columnName, columnType });
         }

      view.setModel (model);
   }

   JPanel getButtonPanel()
   {
      ActionListener bl = new EditorButtonListener();

      JButton addButton =
         makeButton (addIcon, "Add", "Add a new column entry", bl);
      addButton.setEnabled (true);

      delButton  = makeButton (delIcon, "Delete", "Remove selected column entries", bl);
      upButton   = makeButton (upIcon, "Up", "Move column entry up", bl);
      downButton = makeButton (downIcon, "Down", "Move column entry down", bl);

      JPanel buttons = new JPanel (new GridLayout (0, 1));
      buttons.add (addButton);
      buttons.add (delButton);
      buttons.add (upButton);
      buttons.add (downButton);

      SelectionListener l = new SelectionListener(); // calls valueChanged
      view.getSelectionModel().addListSelectionListener (l);

      return buttons;
   }
   
   private JButton makeButton (final Icon icon, final String command, 
                               final String tip, final ActionListener listener)
   {
      JButton button = new JButton (icon);
      button.setPreferredSize (DIM);
      button.setActionCommand (command);
      button.setToolTipText (tip);
      button.addActionListener (listener);
      button.setEnabled (false);
      return button;
   }

   class EditorButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         tableItem.setInitialValue (null); // to indicate schema change
         
         String command = e.getActionCommand();
         if (model == null)
            setTable(null, 0);

         if (command.equals ("Add"))
         {
            String columnName = "COLUMN_" + (model.getRowCount() + 1);
            model.addRow (new String[] { columnName, "VARCHAR" });
            enableButtons(); // in case we just added the first row
         }
         else if (command.equals ("Delete"))
         {
            int[] selected = view.getSelectedRows();
            for (int i = selected.length - 1; i >= 0; i--)
               model.removeRow (selected[i]);
            view.clearSelection();
            enableButtons(); // in case we just deleted the last row
         }
         else if (command.equals ("Up"))
         {
            int selected = view.getSelectedRows()[0];
            int row = selected - 1;
            model.moveRow (selected, selected, row);
            view.getSelectionModel().setSelectionInterval (row, row);
         }
         else if (command.equals ("Down"))
         {
            int selected = view.getSelectedRows()[0];
            int row = selected + 1;
            model.moveRow (selected, selected, row);
            view.getSelectionModel().setSelectionInterval (row, row);
         }
      }
   }

   class SelectionListener implements ListSelectionListener
   {
      public void valueChanged (final ListSelectionEvent e)
      {
         int[] selected = view.getSelectedRows();
         delButton.setEnabled (selected.length >= 1);
         upButton.setEnabled (selected.length == 1 && selected[0] > 0);
         downButton.setEnabled (selected.length == 1 &&
                                selected[0] < view.getRowCount() - 1);
      }
   }

   public void loadSchema (final String connName, final String tableName)
   {
      connBox.setInitialValue (connName);
      tableItem.setInitialValue (tableName);
      setTable (new DatabaseSubset (connName, tableName, false), 0);
   }

   private boolean createTable (final String connName, final String tableName)
   {
      if (SQL.exists (connName, tableName))
      {
         int count = (int)
            SQL.getDouble (connName, "select count(*) from " + tableName);
         if (Confirm.confirm
             (panel, "TABLE EXISTS",
              "The " + tableName + " table already exists.\n" +
              "It currently contains " + count + " record(s).\n" +
              "Are you sure you want to overwrite it?"))
         {
            SQL.execute (connName, "drop table " + tableName);
            tableItem.apply();
         }
         else
            return false;    // abort
      }

      StringBuilder sql = new StringBuilder ("create table ");
      sql.append (tableName);
      sql.append (" (\n");
      for (int row = 0, count = model.getRowCount(); row < count; row++)
      {
         sql.append ("   ");
         sql.append (model.getValueAt (row, 0)); // column name
         sql.append (" ");
         sql.append (model.getValueAt (row, 1)); // column type
         if (row < count - 1)
            sql.append (",");
         sql.append ("\n");
      }
      sql.append (")");

      boolean ok = SQL.execute (connName, sql.toString());

      if (ok)
      {
         observable.registerChange();
         tableItem.apply ();
      }
      else
         JOptionPane.showMessageDialog
            (panel, "Database error ocurred during table creation:\n" + sql,
             "ERROR", JOptionPane.ERROR_MESSAGE);

      return ok;
   }
   
   public JPanel getPanel()
   {
      return panel;
   }

   public String getConnection()
   {
      return (String) connBox.getValue();
   }
   
   public String getTableName()
   {
      return tableItem.isValid() ? (String) tableItem.getValue() : null;
   }

   public List<String> getColumnNames()
   {
      if (model != null)
      {
         List<String> columnNames = new ArrayList<String>();
         for (int row = 0; row < model.getRowCount(); row++)
            columnNames.add ((String) model.getValueAt (row, 0));
         return columnNames;
      }
      
      return null;
   }

   public Observable getObservable()
   {
      return observable;
   }

   public void addObserver (final Observer observer)
   {
      observable.addObserver (observer);
   }

   public void deleteObserver (final Observer observer)
   {
      observable.deleteObserver (observer);
   }
   
   public static void main (final String[] args)
   {
      SchemaEditor se = new SchemaEditor();
      ComponentTools.open (se.getPanel(), SchemaEditor.class.getName());
   }
}
*/
