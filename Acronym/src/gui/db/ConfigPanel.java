package gui.db;

import gui.ComponentTools;
import gui.form.FormItems;
import gui.form.NumericItem;
import gui.form.PasswordItem;
import gui.form.TextItem;
import gui.form.ToggleItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;
import gui.form.valid.RegexValidator;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import utils.Application;
import db.JtdsDriver;
import db.OracleDriver;
import db.SQL;

/**
 * The GUI for configuration settings (database connection).
 * Default values are stored in a simple property file.
 */
public class ConfigPanel extends JPanel
{
   private static final long serialVersionUID = 1L;

   private static final String USER = System.getProperty ("user.name");
   private static final String DBMSHOST = "DBMS Host";
   private static final String DBMSPORT = "DBMS Port";
   private static final String DATABASE = "Database";
   
   // A number from 0 to 255 (without leading zeros)
   private static final String OCTET = "(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])";
   // A 4-octet Internet Protocol (IP) address
   private static final String IP4 = "(?:" + OCTET + "[.]){3}" + OCTET;
   // An Internet Protocol (IP) address
   private static final String IP = "(?:" + IP4 + ")";
   // A machine name
   private static final String HOST = "(?:[A-Za-z]+[-_A-Za-z0-9.]*)";
   // An IP address or host name
   private static final String IP_OR_HOST = "(?:" + IP + "|" + HOST + ")";
   
   private static final int ROWS = 3;
   
   private static final String CONNECT = "Connect";
   
   private Application app;
   private JProgressBar progress;
   
   private FormItems dbItems;
   private TextItem dbmsHostField;
   private NumericItem portField;
   private TextItem dbField;
   
   private FormItems userItems;
   private ToggleItem ssoToggle;
   private TextItem userField;
   private PasswordItem pswdField;
   
   private Map<String, JButton> buttons = new HashMap<String, JButton>();
   private transient ActionListener buttonListener;
   private transient ValueChangeListener changeListener;
   
   public ConfigPanel (final Application app)
   {
      this.app = app;
      this.progress = app.getProgress();

      String dbmsHost = app.getOptions().get (DBMSHOST, "localhost");
      int port = app.getOptions().getInt (DBMSPORT, 1521);
      String db = app.getOptions().get (DATABASE, "XE");
      String pswd = null;
      
      buttonListener = new ButtonListener();
      changeListener = new ChangeListener();
      dbItems = new FormItems (changeListener);
      userItems = new FormItems (changeListener);

      JPanel hostPanel = makeDbmsPanel (dbmsHost, port, db);
      JPanel userPanel = makeUserPanel (USER, pswd);

      JPanel configPanel = new JPanel (new GridLayout (2, 0));
      configPanel.add (hostPanel);
      configPanel.add (userPanel);

      JButton button = ComponentTools.makeButton (CONNECT, "icons/20/hardware/DataConnection.gif", 
               dbItems.isValid(), buttonListener, "Connect to the specified DBMS");
      buttons.put (CONNECT, button); // cache the button
      final JPanel buttonPanel = new JPanel();
      buttonPanel.add (button);

      setLayout (new BorderLayout());
      add (buttonPanel, BorderLayout.NORTH);
      add (configPanel, BorderLayout.CENTER);
   }

   private JPanel makeDbmsPanel (final String host, final int port, final String db)
   {
      RegexValidator hostValidator = new RegexValidator (IP_OR_HOST);
      dbmsHostField = new TextItem ("Host (IP Address or Name)", host);
      dbmsHostField.setValidator (hostValidator);
      dbItems.add (dbmsHostField);

      portField = new NumericItem ("Port", port, 6);
      portField.setFilter ("0123456789");
      dbItems.add (portField);
      
      dbField = new TextItem ("Database", db);
      dbField.setValidator (new RegexValidator (".+"));
      dbItems.add (dbField);
      
      JPanel hostPanel = new JPanel (new GridLayout (ROWS, 1));
      hostPanel.setBorder (new TitledBorder ("DBMS Server Identification"));
      hostPanel.add (dbmsHostField.getTitledPanel());
      hostPanel.add (portField.getTitledPanel());
      hostPanel.add (dbField.getTitledPanel());
      return hostPanel;
   }
   
   private JPanel makeUserPanel (final String user, final String pswd)
   {
      ssoToggle = new ToggleItem ("Single Sign On", "Use Windows Authentication", true);
      ssoToggle.addValueChangeListener (changeListener);
      
      userField = new TextItem ("User Name");
      userField.setEnabled (false);
      userField.setValue (user);
      userField.setValidator (new RegexValidator (".+"));
      userItems.add (userField);
      
      pswdField = new PasswordItem ("Password");
      pswdField.setEnabled (false);
      pswdField.setValue (pswd);
      pswdField.setValidator (new RegexValidator (".+"));
      pswdField.setNullValidity (true);
      userItems.add (pswdField);

      JPanel userPanel = new JPanel (new GridLayout (ROWS, 1));
      userPanel.setBorder (new TitledBorder ("DBMS User Identification"));
      userPanel.add (ssoToggle.getTitledPanel());
      userPanel.add (userField.getTitledPanel());
      userPanel.add (pswdField.getTitledPanel());
      return userPanel;
   }

   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         String cmd = e.getActionCommand();
         if (cmd.equals (CONNECT))
            connect();
         else if (progress != null)
            progress.setString ("Unsupported command: " + cmd);
      }
   }
   
   private void connect()
   {
      final boolean sso = ssoToggle.isSelected();
      final String host = (String) dbmsHostField.getValue();
      final int port = portField.getValueAsInt();
      final String db = (String) dbField.getValue();
      final String user = (String) userField.getValue();
      final String pswd = (String) pswdField.getValue();
      
      if (progress != null)
      {
         progress.setIndeterminate (true);
         progress.setString ("Connecting to: " + host + ":" + port + " " + db +
                             " as " + user + "...");
      }

      Thread thread = new Thread (new Runnable()
      {
         public void run()
         {
            connectUsingOracle();
         }
         
         public void connectUsingOracle()
         {
            String url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + db;
            final SQL sql = OracleDriver.connect (url, user, pswd);
            
            SwingUtilities.invokeLater (new Runnable()
            {
               public void run()
               {
                  if (progress != null)
                     progress.setIndeterminate (false);

                  if (sql != null)
                  {
                     app.getOptions().put (DBMSHOST, host);
                     app.getOptions().put (DBMSPORT, port + "");
                     app.getOptions().put (DATABASE, db);
                     app.getOptions().write();
                     
                     // app.connect (sql);
                     connected (host, port, user, db);
                  }
                  else
                  {
                     Toolkit.getDefaultToolkit().beep();
                     if (progress != null)
                        progress.setString ("Unable to connect to: " + host + ":" + port +
                                            " " + db + " as " + user);
                     else
                        System.out.println ("Unable to connect to: " + host + ":" + port +
                                            " " + db + " as " + user);
                  }
               }
            });
         }
         
         public void connectUsingJTDS()
         {
            // SQLServerDriver dd = new SQLServerDriver();
            JtdsDriver dd = new JtdsDriver(); // supports Windows Authentication SSO
            
            final Connection conn;
            if (sso) // use Windows Authentication (single sign on)
               conn = dd.connect (host, port + "", db);
            else
               conn = dd.connect (host, port + "", db, user, pswd);
            
            SwingUtilities.invokeLater (new Runnable()
            {
               public void run()
               {
                  if (progress != null)
                     progress.setIndeterminate (false);

                  if (conn != null)
                  {
                     app.getOptions().put (DBMSHOST, host);
                     app.getOptions().put (DBMSPORT, port + "");
                     app.getOptions().put (DATABASE, db);
                     app.getOptions().write();
                     
                     SQL sql = new SQL (db, conn);
                     // app.connect (sql);
                     connected (host, port, user, db);
                  }
                  else
                  {
                     Toolkit.getDefaultToolkit().beep();
                     if (progress != null)
                        progress.setString ("Unable to connect to: " + host + ":" + port +
                                            " " + db + " as " + user);
                     else
                        System.out.println ("Unable to connect to: " + host + ":" + port +
                                            " " + db + " as " + user);
                  }
               }
            });
         }
      });
      thread.start();
   }
   
   private void connected (final String host, final int port,
                           final String user, final String db)
   {
      if (progress != null)
         progress.setString ("Connected to: " + host + ":" + port +
                             " " + db + " as " + user);
      dbItems.apply();
      userItems.apply();
   }
   
   class ChangeListener implements ValueChangeListener
   {
      public void valueChanged (final ValueChangeEvent e)
      {
         boolean sso = ssoToggle.isSelected(); 
         if (e.getSource() == ssoToggle)
         {
            if (sso)
            {
               userItems.setEnabled (false);
               userField.setValue (USER);
               pswdField.setNullValidity (true);
            }
            else
            {
               userItems.setEnabled (true);
               pswdField.setNullValidity (false);
            }
         }
         
         boolean readyToConnect = dbItems.isValid() && (sso || userItems.isValid());
         buttons.get (CONNECT).setEnabled (readyToConnect);
      }
   }
   
   public static void main (final String[] args)
   {
      Application app = new Application ("ConfigPanel Test", "ConfigPanel", "12 May 2010")
      {
         public void connect (SQL sql)
         {
            System.out.println ("ConfigPanel connect(): " + sql);
         }
      };
      
      ConfigPanel panel = new ConfigPanel (app);
      ComponentTools.open (panel, ConfigPanel.class.getName());
   }
}
