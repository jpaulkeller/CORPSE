package oberon;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import utils.Application;

public final class AcronymExtractor extends Application
{
   private Acronyms acronyms;
   private AcronymDatabase db;
   private SourceDocument doc;
   
   private Menus menus;
   private ActionListener actionListener;
   
   private AcronymExtractor (final String version)
   {
      super ("Oberon Acronym Extractor", "Oberon/Acronyms", version);

      acronyms = new Acronyms (this);
      db = new AcronymDatabase (this);
      
      actionListener = new MenuListener();
      menus = new Menus (this, actionListener);
      setMenus (menus.getMenus());
      
      doc = new SourceDocument (this);
      
      JPanel top = new JPanel (new BorderLayout());
      top.add (doc.getPanel(), BorderLayout.CENTER);
      JPanel buttons = ComponentTools.getTitledPanel (menus.getButtonPanel(), "Tools");
      top.add (buttons, BorderLayout.EAST);

      add (top, BorderLayout.NORTH);
      add (acronyms.getView().getPanel (false, 700, 500), BorderLayout.CENTER);
   }
   
   @Override
   protected void process() throws IOException 
   {
      updateState (Menus.States.extracting.toString(), true);
      getProgress().setString ("Extracting acronyms...");
      if (doc.getText() != null)
      {
         doc.extractAcronyms (acronyms);
         getProgress().reset (acronyms.size() + " acronyms found; extracting definitions...");
         // the following are threaded processes
         expandAcronyms();
         setContext(); // add tool-tips showing context from the document
      }
      else
         getProgress().reset ("Sorry, unable to extract the text of your document.");
   }

   @Override
   protected void close()
   {
      db.close();
   }
   
   private void expandAcronyms()
   {
      Thread thread = new Thread (new Runnable()
      {
         public void run()
         {
            db.expandUsingDatabase (acronyms);
            
            int count = 0;
            for (int row = 0, rows = acronyms.size(); row < rows; row++)
            {
               Acronym acronym = acronyms.getAcronym (row);
               getProgress().setString ("Searching document for explicit: " + acronym);
               getProgress().setValue ((++count) * 100 / rows); // percent
               // find acronyms explicitly defined in the text
               if (doc.findExplicit (acronym))
                  acronyms.approve (row, true); // auto-approve explicit
               
               acronyms.repaint();
            }

            finishedExtracting ("Acronyms extracted");
         }
      });
      thread.start();
   }

   void finishedExtracting (final String message)
   {
      SwingUtilities.invokeLater (new Runnable()
      {
         public void run()
         {
            updateState (Menus.States.extracting.toString(), false);
            
            int defined = 0;
            for (Acronym acronym : acronyms.values())
               if (!acronym.getDefinitions().isEmpty())
                  defined++;
            getProgress().reset (message + " (" + defined + " of " + 
                                 acronyms.size() + " defined)");
         }
      });
   }
   
   private void setContext()
   {
      Thread thread = new Thread (new Runnable()
      {
         public void run()
         {
            for (Acronym acronym : new ArrayList<Acronym> (acronyms.values()))
               if (acronym.getContext() == null)
                  acronym.setContext (doc.findContext (acronym));
         }
      });
      thread.start();
   }

   private void addNew()
   {
      String abbrev = (String) JOptionPane.showInputDialog
         (getFrame(), "New Acronym:\n", "Add New Acronym",
          JOptionPane.PLAIN_MESSAGE, null, null, "");
      if (abbrev != null && !abbrev.trim().equals ("")) 
      {
         acronyms.add (new Acronym (abbrev.trim()));
         acronyms.scrollTo (acronyms.size() - 1);
         updateState (Menus.States.hasData.toString(), true);
      }
   }

   private void defineUsingContext() 
   {
      List<Acronym> selected = acronyms.getSelected(); // should only be 1
      if (selected.size() == 1)
      {
         Acronym acronym = selected.get (0);
         String definition = (String) JOptionPane.showInputDialog
         (getFrame(), "Define " + acronym.getAbbrev() + ":\n", "Define Acronym",
          JOptionPane.PLAIN_MESSAGE, null, null, acronym.getContext());
         if (definition != null && !definition.trim().equals ("")) 
         {
            acronym.addValue (definition, Source.User);
            int row = acronyms.findModelRow (acronym);
            acronyms.approve (row, true); // auto-approve user-entered
            acronyms.scrollTo (row);
         }
      }
   }

   private void removeSelected()
   {
      int removed = 0;
      List<Acronym> selected = acronyms.getSelected(); // should only be 1
      for (Acronym acronym : selected)
         removed += acronyms.remove (acronym);
      getProgress().setString (removed + " acronym(s) removed");
   }
   
   private void publish()
   {
      if (db.isConnected())
         for (Acronym acronym : acronyms.getApproved())
            db.persist (acronym);
      acronyms.exportToRTF (doc.getFile());
   }

   private class MenuListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         String cmd = e.getActionCommand();
         
         if (cmd.equals (Menus.ACRONYMS))
            new Thread (AcronymExtractor.this).start(); // calls process()
         else if (cmd.equals(Menus.ADD_NEW))
            addNew();
         else if (cmd.equals(Menus.DEFINE))
            defineUsingContext();         
         else if (cmd.equals(Menus.REMOVE))
            removeSelected();
         else if (cmd.equals (Menus.CLEAR))
            acronyms.clear();

         // find definitions
         else if (cmd.equals (Menus.DOCUMENT))
            doc.expandUsingDocument (acronyms);
         else if (cmd.equals (Menus.WEB))
            SourceWeb.expandUsingWebScraper (AcronymExtractor.this, acronyms);
         
         else if (!export (cmd))
            getProgress().setString ("Unsupported command: " + cmd);
      }
   }
   
   private boolean export (final String cmd)
   {
      if (cmd.equals (Menus.PUBLISH)) 
         publish();
      else if (cmd.equals (Menus.CONNECT_DB)) 
         db.connect();
      else if (cmd.equals (Menus.ADD_DB)) 
         db.persist (acronyms);
      else if (cmd.equals (Menus.EDIT_DB)) 
         db.load (acronyms);
      else if (cmd.equals (Menus.RTF)) 
         acronyms.exportToRTF (doc.getFile());
      else if (cmd.equals (Menus.EXCEL))
         acronyms.exportToExcel();
      else if (cmd.equals (Menus.HTML))
         acronyms.exportToHTML();
      else if (cmd.equals (Menus.PRINT))
         acronyms.print();
      else
         return false;
      return true;
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      AcronymExtractor app = new AcronymExtractor ("22 Mar 2010"); // TBD
      app.open();
   }
}
