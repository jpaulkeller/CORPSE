package oberon;

import gui.ComponentTools;
import gui.db.ConfigPanel;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import utils.Application;
import db.DatabaseAPI;
import db.Model;
import db.SQL;
import db.SQLU;

public class AcronymDatabase
{
   private static final SimpleDateFormat YMD = 
      new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss.0");

   private Application app;
   private SQL sql;

   public AcronymDatabase (final Application app)
   {
      this.app = app;
      sql = DatabaseAPI.connect ("Web");
      app.updateState (Menus.States.noDatabase.toString(), !isConnected());
   }
   
   public void connect()
   {
      ConfigPanel panel = new ConfigPanel (app);
      ComponentTools.open (panel, "Database Connection");
      // sql = DatabaseAPI.connect (host, port, database, user, password);
      // app.updateState (Menus.States.noDatabase.toString(), !isConnected());
      // TODO: call makeTable if necessary
   }
   
   public boolean isConnected()
   {
      return sql != null;
   }
   
   public void expandUsingDatabase (final Acronyms acronyms)
   {
      if (isConnected()) // expand by looking up definitions in the database
         for (int row = 0, rows = acronyms.size(); row < rows; row++)
         {
            Acronym acronym = acronyms.getAcronym (row);
            lookupValues (acronym);
            if (acronym.getDefinitions().size (Source.Database) == 1)
               acronyms.approve (row, true); // auto-approve
            acronyms.repaint();
         }
      
      SwingUtilities.invokeLater (new Runnable()
      {
         public void run()
         {
            acronyms.getView().getView().packAll();
         }
      });
   }
   
   public void load (final Acronyms acronyms)
   {
      String select = "select Acronym, Definition from Acronyms " +
                      " order by Acronym, Updated desc"; // sort by date
      try
      {
         Acronym acronym = null;
         String prev = null;
         
         Model model = sql.getModel (select);
         for (int row = 0; row < model.getRowCount(); row++)
         {
            String abbrev = (String) model.getValueAt (row, "Acronym");
            String text = (String) model.getValueAt (row, "Definition");
            if (!abbrev.equals (prev))
            {
               prev = abbrev;
               acronym = new Acronym (abbrev);
               Definition def = acronym.addValue (text, Source.Database);
               acronym.setSelected (def); // select the newest one
               acronyms.add (acronym);
            }
            else if (acronym != null)
               acronym.addValue (text, Source.Database);
         }
         
         acronyms.getView().getView().packAll();
         app.updateState (Menus.States.hasData.toString(), !acronyms.isEmpty());
      }
      catch (SQLException x)
      {
         System.err.println (x + "\n" + select);
         x.printStackTrace (System.err);
      }
   }

   public void lookupValues (final Acronym acronym)
   {
      String select = "select Definition from Acronyms where " +
      "Acronym = " + SQLU.quote (acronym.getAbbrev()) + 
      " order by Updated desc"; // sort by date
   try
   {
      List<String> values = sql.getList (select);
      if (!values.isEmpty())
      {
         Definition mostRecent = null;
         for (String value : values)
         {
            Definition def = acronym.addValue (value, Source.Database);
            if (mostRecent == null)
               mostRecent = def;
         }
         acronym.setSelected (mostRecent);
      }
   }
   catch (SQLException x)
   {
      System.err.println (x + "\n" + select);
      x.printStackTrace (System.err);
   }
   }
   
   public void persist (final Acronyms acronyms)
   {
      app.getProgress().setIndeterminate (true);
      
      if (isConnected())
      {
         Thread thread = new Thread (new Runnable()
         {
            public void run()
            {
               Collection<Acronym> targets = acronyms.getSelected();
               if (!targets.isEmpty())
                  app.getProgress().setString ("Saving selected acronyms to the database...");
               else
               {
                  targets = acronyms.getApproved();
                  app.getProgress().setString ("Saving approved acronyms to the database...");
               }

               int count = 0;
               for (Acronym acronym : targets)
                  count += persist (acronym);
               app.getProgress().reset (count + " database entries updated");
            }
         });
         thread.start();
      }
   }

   public int persist (final Acronym acronym)
   {
      int updated = 0;

      if (acronym.getSource() != Source.Empty)
      {
         Definition definition = acronym.getSelected();
         
         StringBuilder stmt = new StringBuilder(); 
         try
         {
            String date = YMD.format (new Date());
            stmt.append ("update Acronyms ");
            stmt.append ("set Updated = '" + date + "' ");
            stmt.append ("where ");
            stmt.append ("Acronym = " + SQLU.quote (acronym.getAbbrev()) + " and ");
            stmt.append ("Definition = " + SQLU.quote (definition.getText()));
            
            updated = sql.execute (stmt);
            if (updated == 0)
            {
               stmt.setLength (0);
               stmt.append ("insert into Acronyms values (");
               stmt.append (SQLU.quote (acronym.getAbbrev()) + ", ");
               stmt.append (SQLU.quote (definition.getText()) + ", ");
               stmt.append ("'" + date + "')");
               updated = sql.execute (stmt);
            }
         }
         catch (SQLException x)
         {
            System.err.println (x + "\n" + stmt);
            x.printStackTrace (System.err);
         }
      }
         
      return updated;
   }
   
   public void close()
   {
      if (sql != null)
         sql.close();
   }
   
   private static void makeTable()
   {
      try
      {
         // TODO: support Oracle
         SQL sql = DatabaseAPI.connect ("Web");
         sql.execute ("drop table Acronyms");
         sql.execute ("create table Acronyms (" +
                      "Acronym    VARCHAR(16), " +
                      "Definition VARCHAR(255), " +
                      "Updated    SMALLDATETIME)");
      }
      catch (SQLException x)
      {
         x.printStackTrace (System.err);
         JOptionPane.showMessageDialog (null, x.getMessage(), "Database Error", 
                                        JOptionPane.ERROR_MESSAGE, null);
      }
   }
   
   public static void main (final String[] args)
   {
      AcronymDatabase.makeTable();
   }
}
