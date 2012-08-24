package lotro.models;

import gui.ComponentTools;
import gui.editors.RangeEditor;
import gui.editors.SubsetEditor;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;


public class DeedModel implements Serializable
{
   private static final long serialVersionUID = 1L;

   private SortedMap<String, Deed> deeds;
   private DefaultTableModel table;

   public DeedModel()
   {
      deeds = new TreeMap<String, Deed>();
      table = new MyTableModel();
   }
      
   void setDeeds (final Collection<Deed> deedsToAdd)
   {
      for (Deed deed : deedsToAdd)
         addDeed (deed);
   }
   
   void addDeed (final Deed deed)
   {
      deeds.put (deed.getKey(), deed);
   }
   
   void removeDeed (final Deed deed)
   {
      deeds.remove (deed.getKey());
   }
   
   public void clear()
   {
      deeds.clear();
   }
   
   TableModel getTable()
   {
      return table;
   }
      
   int size()
   {
      return deeds.size();
   }
   
   Deed get (final String key)
   {
      return deeds.get (key);
   }
   
   SortedSet<String> getDeedNames()
   {
      return new TreeSet<String> (deeds.keySet());
   }
   
   SortedSet<Deed> getDeeds()
   {
      return new TreeSet<Deed> (deeds.values());
   }
   
   void populate()
   {
      // clear the model
      while (table.getRowCount() > 0)
         table.removeRow (0);
      table.setColumnCount (0);

      table.addColumn ("Region");
      table.addColumn ("Name");
      table.addColumn ("Type");
      table.addColumn ("Trait");
      table.addColumn ("Level");
         
      for (Deed deed : deeds.values())
         addRow (deed);
   }
   
   private void addRow (final Deed deed)
   {
      Vector<Object> row = new Vector<Object>();
      row.add (deed.getRegion());
      row.add (deed.getName());
      row.add (deed.getType());
      row.add (deed.getTrait());
      row.add (deed.getLevel());
      table.addRow (row);
   }
      
   // iterate over the table, and update model
   
   void updateFromTable()
   {
      for (int row = 0, rows = table.getRowCount(); row < rows; row++)
      {
         String region = (String) table.getValueAt (row, 0);
         String name = (String) table.getValueAt (row, 1);
         String key = Deed.getKey (region, name);
         Deed deed = deeds.get (key);
         deed.setType ((String)  table.getValueAt (row, 2));
         deed.setTrait ((String)  table.getValueAt (row, 3));
         deed.setLevel ((Integer) table.getValueAt (row, 4));
      }
   }
   
   void write (final String file)
   {
      Deed.write (deeds.values(), file);
   }
   
   private static class MyTableModel extends DefaultTableModel
   {
      private static final long serialVersionUID = 1L;
      
      @Override
      public Class<?> getColumnClass (final int c)
      {
         return c == 4 ? Integer.class : String.class;
      }
   }
   
   public static void main (final String[] args)
   {
      DeedModel model = new DeedModel();
      model.setDeeds (Deed.read (Deed.FILE).values());
      model.populate();
      
      JXTable tbl = new JXTable (model.getTable());
      tbl.getTableHeader().setReorderingAllowed (false);
      tbl.setPreferredScrollableViewportSize (new Dimension (600, 300));
      tbl.setDefaultEditor (String.class, new SubsetEditor (true));
      tbl.setDefaultEditor (Integer.class, new RangeEditor (1, Character.MAX_LEVEL, 1));
         
      JScrollPane scroll = new JScrollPane (tbl);
      ComponentTools.open (scroll, model.getClass().getName());
   }
}
