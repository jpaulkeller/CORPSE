package lotro.models;

import gui.ComponentTools;
import gui.editors.EnumEditor;
import gui.editors.RangeEditor;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;


public class CharacterModel implements Serializable
{
   private static final long serialVersionUID = 1L;

   private SortedMap<String, Character> chars;
   private DefaultTableModel table;

   public CharacterModel()
   {
      chars = new TreeMap<String, Character>();
      table = new MyTableModel();
   }
      
   public void addCharacter (final Character ch)
   {
      chars.put (ch.getName(), ch);
   }
   
   public void removeCharacter (final Character ch)
   {
      chars.remove (ch.getName());
   }
   
   public TableModel getTable()
   {
      return table;
   }
      
   public int size() // getCharacterCount
   {
      return chars.size();
   }
   
   public Character get (final String name)
   {
      return chars.get (name);
   }
   
   public SortedSet<String> getCharacterNames()
   {
      return new TreeSet<String> (chars.keySet());
   }
   
   public SortedSet<Character> getCharacters()
   {
      return new TreeSet<Character> (chars.values());
   }
   
   public void populate()
   {
      // clear the model
      while (table.getRowCount() > 0)
         table.removeRow (0);
      table.setColumnCount (0);

      table.addColumn ("Player");
      table.addColumn ("Name");
      table.addColumn ("Race");
      table.addColumn ("Class");
      table.addColumn ("Level");
         
      for (Character ch : chars.values())
         addRow (ch);
   }
   
   private void addRow (final Character ch)
   {
      Vector<Object> row = new Vector<Object>();
      row.add (ch.getPlayer());
      row.add (ch.getName());
      row.add (ch.getRace());
      row.add (ch.getKlass());
      row.add (ch.getLevel());
      table.addRow (row);
   }
      
   // iterate over the table, and update the model
   
   public void updateFromTable()
   {
      for (int row = 0, rows = table.getRowCount(); row < rows; row++)
      {
         String name = (String) table.getValueAt (row, 1);
         Character ch = chars.get (name);
         if (ch != null)
         {
            ch.setPlayer ((Player) table.getValueAt (row, 0));
            // TBD: change all for this player?
            ch.setRace   ((Race)    table.getValueAt (row, 2));
            ch.setKlass  ((Klass)   table.getValueAt (row, 3));
            ch.setLevel  ((Integer) table.getValueAt (row, 4));
         }
      }
   }
   
   public void write (final String file)
   {
      CharacterWithDeeds.write (chars.values(), file);
   }
   
   private static class MyTableModel extends DefaultTableModel
   {
      private static final long serialVersionUID = 1L;
      
      @Override
      public Class<?> getColumnClass (final int c)
      {
         return getValueAt (0, c).getClass();
      }
   }
   
   public static void main (final String[] args)
   {
      CharacterModel model = new CharacterModel();
      for (Character ch : CharacterWithDeeds.read2 ("saves/sample.chr"))
         model.addCharacter (ch);
      model.populate();
      
      JXTable tbl = new JXTable (model.getTable());
      tbl.getTableHeader().setReorderingAllowed (false);
      tbl.setPreferredScrollableViewportSize (new Dimension (600, 300));
      tbl.setDefaultEditor (Race.class, new EnumEditor<Race> (Race.FREEPS));
      tbl.setDefaultEditor (Klass.class, new EnumEditor<Klass> (Klass.FREEPS));
      tbl.setDefaultEditor (Integer.class, new RangeEditor (1, Character.MAX_LEVEL, 1));
      tbl.packAll();
         
      JScrollPane scroll = new JScrollPane (tbl);
      ComponentTools.open (scroll, model.getClass().getName());
   }
}
