package oberon;

import file.FileUtils;
import file.RTFFile;
import gui.comp.FileChooser;
import gui.db.TableView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;

import utils.Application;
import db.Model;

/** Maps the acronym (abbreviation) to the Acronym object. */
  
public class Acronyms extends TreeMap<String, Acronym>
{
   private Application app;
   private Model model;
   private transient TableView view;

   public Acronyms (final Application app)
   {
      this.app = app;
      makeModel();
      makeView();
   }
   
   public synchronized Acronym add (final Acronym acronym)
   {
      if (!containsKey (acronym.getAbbrev()))
      {
         super.put (acronym.getAbbrev(), acronym);
         model.addRowValues (false, acronym, acronym.getDefinitions());
      }
      return acronym;
   }

   public synchronized int remove (final Acronym acronym)
   {
      int row = findModelRow (acronym);
      if (row >= 0)
      {
         model.removeRow (row);
         super.remove (acronym.getAbbrev());
         return 1;
      }
      return 0;
   }

   public Model getModel()
   {
      return model;
   }
   
   public TableView getView()
   {
      return view;
   }
   
   @Override
   public synchronized void clear()
   {
      super.clear();
      while (model.getRowCount() > 0)
         model.removeRow (0);
      app.updateState (Menus.States.hasData.toString(), false);
   }
   
   public Acronym getAcronym (final int row)
   {
      return (Acronym) model.getValueAt (row, 1);
   }
   
   public void approve (final int row, final boolean approved)
   {
      model.setValueAt (approved, row, 0);
   }
   
   public boolean isApproved (final Acronym acronym)
   {
      int row = findModelRow (acronym);
      return row >= 0 && (Boolean) model.getValueAt (row, 0);
   }
   
   public boolean isApproved (final int row)
   {
      return (Boolean) model.getValueAt (row, 0);
   }
   
   public void updateModel (final Acronym acronym)
   {
      SwingUtilities.invokeLater (new Runnable()
      {
         public void run()
         {
            int row = findModelRow (acronym);
            if (row >= 0)
            {
               boolean approved = isApproved (row);
               model.setValueAt (acronym.getDefinitions(), row, 2);
               if (!approved)
                  approve (row, false); // hack to reset
            }
         }
      });
   }

   public synchronized int findModelRow (final Acronym acronym)
   {
      for (int modelRow = 0; modelRow < model.getRowCount(); modelRow++)
         if (acronym == (Acronym) model.getValueAt (modelRow, 1))
            return modelRow;
      return -1;
   }

   public void scrollTo (final int modelRow)
   {
      int viewRow = view.getView().convertRowIndexToView (modelRow);
      TableView.scrollTo (view.getView(), viewRow);
   }

   public void repaint()
   {
      view.getView().repaint();
   }
   
   public List<Acronym> getSelected()
   {
      List<Acronym> selected = new ArrayList<Acronym>();
      
      JXTable v = view.getView(); 
      if (v.getSelectedRowCount() > 0) // just consider selected rows
         for (int viewRow : v.getSelectedRows())
         {
            int row = v.convertRowIndexToModel (viewRow);
            selected.add ((Acronym) model.getValueAt (row, 1));
         }
      
      return selected;
   }
   
   public List<Acronym> getUndefined()
   {
      List<Acronym> undefined = new ArrayList<Acronym>();
      for (Acronym acronym : values())
         if (!acronym.isDefined())
            undefined.add (acronym);
      return undefined;
   }
   
   public List<Acronym> getApproved()
   {
      List<Acronym> approved = new ArrayList<Acronym>();
      for (int row = 0, rows = model.getRowCount(); row < rows; row++)
         if ((Boolean) model.getValueAt (row, 0)) // approved
            approved.add ((Acronym) model.getValueAt (row, 1));
      return approved;
   }
   
   public void exportToExcel()
   {
      TableView.exportToExcel (view.getView(), view.getName());
   }
   
   public void exportToHTML()
   {
      TableView.exportToHTML (view.getView(), view.getName());
   }
   
   public void exportToRTF (final File inFile)
   {
      int exported = 0;
      
      FileChooser fc = new FileChooser ("Export RTF File", inFile.getPath());
      fc.setDialogType (JFileChooser.SAVE_DIALOG);
      fc.setRegexFilter (".+[.]rtf", "Rich Text Format (*.rtf) files");
      String name = FileUtils.getNameWithoutSuffix (inFile) + " Acronyms.rtf";
      fc.setSelectedFile (new File (name));
      
      if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      {
         File file = fc.getSelectedFile(); 
         RTFFile rtf = null; 
         
         try
         {
            rtf = new RTFFile (file.getPath());
            rtf.createTable (2);
            rtf.addRow (new String[] { "Acronym", "Term/Definition" });
            for (Acronym ac : getApproved())
            {
               rtf.addRow (new String[] { ac.getAbbrev(), ac.getSelected().getText() });
               exported++;
            }
            app.getProgress().reset (exported + " acronyms publised to: " + file);
         }
         catch (IOException x)
         {
            x.printStackTrace();
         }
         finally
         {
            if (rtf != null)
               rtf.close();
         }
      }
   }
   
   public void print()
   {
      TableView.print (view.getView());
   }
   
   private Model makeModel()
   {
      model = new Model ("Acronyms");
      model.addColumn ("Approved", Boolean.class);
      model.addColumn ("Acronym", Acronym.class);
      model.addColumn ("Definitions", Definitions.class);
      model.setColumnEditable (1, false);
      return model;
   }
   
   private void makeView()
   {
      view = new TableView (model, new TableRenderer());
      view.getView().getTableHeader().setReorderingAllowed (false);
      view.getView().setDefaultEditor (Definitions.class, new ComboEditor());
      view.getView().setEditable (true);
      ListSelectionListener listener = new TableSelectionListener();
      view.getView().getSelectionModel().addListSelectionListener (listener);
   }
   
   private class TableSelectionListener implements ListSelectionListener
   {
      public void valueChanged (final ListSelectionEvent event)
      {
         int count = view.getView().getSelectedRowCount();
         app.updateState (Menus.States.acronymSelected.toString(), count == 1);
         app.updateState (Menus.States.acronymsSelected.toString(), count > 0);
      }
   }
}
