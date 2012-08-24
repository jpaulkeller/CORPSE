package gui.db;

import file.FileUtils;
import gui.ComponentTools;
import gui.comp.DragDropList;
import gui.form.ComboBoxItem;
import gui.form.FileItem;
import gui.form.TextAreaItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;
import gui.form.valid.RegexValidator;
import gui.form.valid.StatusEvent;
import gui.form.valid.StatusListener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.ListModel;
import utils.Application;
import utils.ImageTools;
import db.Model;
import db.SQL;
import db.SQLFile;
import db.SQLU;

// swingx-0.9.5.jar

public final class DatabaseBrowser extends Application
{
   private static final Icon CONFIG_ICON = ImageTools.getIcon ("icons/20/hardware/DataConnection.gif");
   private static final Icon SQL_ICON    = ImageTools.getIcon ("icons/20/objects/Gearwheel.gif");
   private static final Icon DATA_ICON   = ImageTools.getIcon ("icons/20/gui/Row.gif");
   
   private String schema;
   private ListModel<String> listModel; // list of table names

   private ConfigPanel configPanel;
   private ComboBoxItem schemaItem;
   private JPanel mainPanel;
   private JList listView;
   private TableView tableView;
   private FileItem fileItem;
   private TextAreaItem sqlItem;
   private JButton exButton;
   
   private DatabaseBrowser (final String version, final String[] args)
   {
      super ("Oracle Browser", "OracleBrowser", version, args);
      
      configPanel = new ConfigPanel (this); 

      JTabbedPane tabs = new JTabbedPane();
      tabs.addTab ("Config", CONFIG_ICON, configPanel, "Database connection configuration");
      tabs.addTab ("Data", DATA_ICON, makeDataPanel(), "View the data");
      tabs.addTab ("SQL", SQL_ICON, makeSqlPanel(), "Execute SQL");

      add (tabs, BorderLayout.CENTER);
   }

   private Component makeSqlPanel()
   {
      fileItem = FileItem.make ("SQL File", null, new FileListener(), "sql"); 
      
      sqlItem = new TextAreaItem ("SQL", "", 2, 80);
      sqlItem.setValidator (new RegexValidator (SQLFile.SQL_PATTERN));
      sqlItem.addStatusListener (new TextListener());
      
      exButton = ComponentTools.makeButtonNarrow
         ("Run", "icons/20/objects/GearGreen.gif", false, new SQLListener(),
          "Execute the SQL statement");
   
      JPanel panel = new JPanel (new BorderLayout());
      panel.add (fileItem.getTitledPanel(), BorderLayout.NORTH);
      panel.add (sqlItem.getTitledPanel(), BorderLayout.CENTER);
      panel.add (exButton, BorderLayout.SOUTH);
      
      return panel;
   }
   
   private Component makeDataPanel()
   {
      schemaItem = new ComboBoxItem ("Schema");
      schemaItem.setEditable (true);
      schemaItem.addValueChangeListener (new SchemaListener());
      
      JPanel controls = new JPanel (new BorderLayout());
      controls.add (schemaItem.getTitledPanel(), BorderLayout.NORTH);

      List<String> tables = new ArrayList<String>();
      listModel = new ListModel<String> (tables);
      listView = new DragDropList (listModel, DnDConstants.ACTION_COPY);
      listView.addListSelectionListener (new TableListener());
      JScrollPane scroll = new JScrollPane (listView);
      controls.add (ComponentTools.getTitledPanel (scroll, "Tables"), BorderLayout.CENTER);

      tableView = new TableView (null);
      tableView.setFormat (oracle.sql.TIMESTAMP.class, new OracleDateFormat ("yyyy-MM-dd"));

      mainPanel = new JPanel (new BorderLayout());
      mainPanel.setPreferredSize (new Dimension (800, 600));
      mainPanel.add (tableView.getPanel (true, 800, 600), BorderLayout.CENTER);

      JPanel panel = new JPanel (new BorderLayout());
      panel.add (controls, BorderLayout.WEST);
      panel.add (mainPanel, BorderLayout.CENTER);
      
      return panel;
   }
   
   private boolean isConnected()
   {
      return getConnection() != null;
   }
   
   @Override
   protected void process() throws IOException 
   {
   }

   private void loadTables (final String newSchema)
   {
      if (newSchema != null && !newSchema.equals (schema))
      {
         schema = newSchema;
         listModel.clear();
         
         List<String> tables = getConnection().getTables (null, schema, null);
         if (tables == null)
         {
            configPanel.connect(); // attempt to re-connect
            tables = getConnection().getTables (null, schema, null);
         }
         if (tables != null)
            for (String table : tables)
               if (!table.contains ("$"))
                  listModel.add (table);
      }
   }

   @Override
   public void setConnection (final SQL sql)
   {
      super.setConnection (sql);
      if (sql != null)
      {
         getFrame().setTitle ("Oracle Browser: " + sql.getDatabase());
         List<String> schemas = getConnection().getSchemas();
         schemaItem.setModel (new DefaultComboBoxModel (schemas.toArray()));
      }
   }
   
   /*
   private boolean connect (final Config config)
   {
      DatabaseDriver dd = new OracleDriver();
      String className = dd.getDriverString();
      
      try
      {
         // load the class (without requiring Oracle jar at compile time)
         System.out.print ("Loading " + className + "... ");
         Class<Driver> c = (Class<Driver>) Class.forName (className);
         System.out.println ("loaded.");

         System.out.print ("Instantiating Oracle driver... ");
         Driver driver = c.newInstance();
         System.out.println (driver);
         
         System.out.println ("Registering Oracle driver...");
         DriverManager.registerDriver (driver);

         String url = config.get ("DB_URL");         
         System.out.println ("URL = " + url);
         String user = config.get ("DB_USER");         
         String pswd = config.get ("DB_PSWD");         
         
         System.out.print ("Establishing connection... ");
         Connection conn = DriverManager.getConnection (url, user, pswd);
         System.out.println (conn);
         
         String dbName = config.getDir();
         sql = new SQL (dbName, conn);
      }
      catch (ClassNotFoundException x)
      {
         System.err.println ("Oracle Driver not found: " + className);
         System.err.println (x.getMessage());
      }
      catch (Exception x)
      {
         System.err.println (x.getMessage());
         x.printStackTrace (System.err);
      }
      
      return sql != null;
   }
   */
   
   private void showCurrentTable()
   {
      if (listView.getSelectedIndices().length == 1)
      {
         String table = (String) listView.getSelectedValue();
         showTable ("select * from " + schema + "." + table + "");
      }
   }
   
   private void showTable (final String select)
   {
      getProgress().setString (select);
      getProgress().setIndeterminate (true);
      
      Thread thread = new Thread (new Runnable()
      {
         public void run()
         {
            try
            {
               Model model = getConnection().getModel (select);
               tableView.setModel (model);
               tableView.getView().repaint(); // TBD
               getProgress().reset (select + ": " + model.getRowCount() + " entries loaded.");
            }
            catch (SQLException x)
            {
               System.out.println (select);
               x.printStackTrace();
               getProgress().reset (x.getMessage() + "; attempting to re-connect");
               configPanel.connect(); // attempt to re-connect
            }
         }
      });
      thread.start();
   }
   
   class FileListener implements StatusListener, ValueChangeListener
   {
      public void valueChanged (final ValueChangeEvent e)
      {
         if (fileItem.isValid())
         {
            String dir = fileItem.getFile().getParent();
            fileItem.setDefaultDir (dir);
            getConfig().put ("SQL File", fileItem.getPath());
            getConfig().write();
         }
      }
      
      public void stateChanged (final StatusEvent e)
      {
         if (e.getStatus())
         {
            String sqlStatements = FileUtils.getText (fileItem.getFile());
            sqlItem.setValue (sqlStatements);
         }
      }
   }
         
   private class TextListener implements StatusListener
   {
      public void stateChanged (final StatusEvent e)
      {
         exButton.setEnabled (e.getStatus());
      }
   }
   
   private class SQLListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         try
         {
            String stmt = sqlItem.getValue().toString();
            int count = getConnection().execute (stmt);
            if (stmt.toLowerCase().startsWith ("select"))
               showTable (stmt);
            else
            {
               getProgress().setString (count + " record(s) affected");
               String table = SQLU.getTableName (stmt);
               showTable ("select * from " + schema + "." + table + "");
            }
         }
         catch (SQLException x)
         {
            getProgress().setString (x.getMessage());
            x.printStackTrace();
         }
      }
   }
   
   private class SchemaListener implements ValueChangeListener
   {
      public void valueChanged (final ValueChangeEvent e)
      {
         loadTables ((String) schemaItem.getValue());
         // TBD: clear the table view
      }
   }
   
   private class TableListener implements ListSelectionListener
   {
      public void valueChanged (final ListSelectionEvent e)
      {
         if (!e.getValueIsAdjusting())
            showCurrentTable();
      }
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      
      DatabaseBrowser app = new DatabaseBrowser ("10 May 2010", args);
      app.open();
   }
}
