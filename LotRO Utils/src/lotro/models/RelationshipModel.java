package lotro.models;

import gui.ComponentTools;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import lotro.gui.RelationshipEditor;
import lotro.gui.RelationshipRenderer;

public class RelationshipModel implements Serializable
{
   private static final long serialVersionUID = 1L;

   private SortedSet<Character> chars = new TreeSet<Character>();
   private DefaultTableModel table;
   private Collection<Pair> friends = new ArrayList<Pair>();
   private Collection<Pair> enemies = new ArrayList<Pair>();

   public RelationshipModel()
   {
      table = new MyTableModel();
   }
      
   void setCharacters (final Collection<? extends Character> characters)
   {
      this.chars.addAll (characters);
   }
   
   void addCharacter (final Character ch)
   {
      chars.add (ch);
   }
   
   void removeCharacter (final Character ch)
   {
      chars.remove (ch);
      removeRelationship (friends, ch);
      removeRelationship (enemies, ch);
   }
   
   private void removeRelationship (final Collection<Pair> pairs, 
                                    final Character ch)
   {
      Iterator<Pair> iter = pairs.iterator();
      while (iter.hasNext())
         if (iter.next().contains (ch))
            iter.remove();
   }
   
   TableModel getTable()
   {
      return table;
   }
      
   int getCharacterCount()
   {
      return chars.size();
   }
   
   int getRelationshipCount()
   {
      return friends.size() + enemies.size();
   }
   
   void clear()
   {
      friends.clear();
      enemies.clear();
   }

   void populate()
   {
      // clear the model
      while (table.getRowCount() > 0)
         table.removeRow (0);
      table.setColumnCount (0);

      if (chars.size() > 1)
      {
         table.addColumn ("Character");
         for (Character ch : chars.headSet (chars.last())) // skip last
            table.addColumn (ch.getName());
         for (Character ch : chars)
            if (ch != chars.first()) // skip first
               addRow (ch);
      }
   }
   
   private void addRow (final Character header)
   {
      Vector<Object> row = new Vector<Object>();
      row.add (header.getName());
      for (Character ch : chars.headSet (chars.last()))
         if (header.compareTo (ch) > 0)
         {
            if (related (friends, ch, header))
               row.add (Relationship.JOIN);
            else if (related (enemies, ch, header))
               row.add (Relationship.AVOID);
            else
               row.add (Relationship.NORMAL);
         }
         else
            row.add (Relationship.NONE);
      table.addRow (row);
   }
      
   // iterate over the table, and update the Relationship lists
   
   void updateFromTable()
   {
      friends.clear();
      enemies.clear();
      
      for (int row = 0, rows = table.getRowCount(); row < rows; row++)
      {
         String name = (String) table.getValueAt (row, 0);
         Character c1 = findCharacter (name);

         // start at 1 to skip the Character column
         for (int col = 1, cols = chars.size(); col < cols; col++)
         {
            Relationship r = (Relationship) table.getValueAt (row, col);
            if (r == Relationship.NONE || r == Relationship.NORMAL)
               continue;
            
            Character c2 = findCharacter (table.getColumnName (col));
            if (r == Relationship.JOIN)
               addFriends (c1, c2);
            else if (r == Relationship.AVOID)
               addEnemies (c1, c2);
         }
      }
   }
   
   private boolean related (final Collection<Pair> pairs, 
                            final Character c1, 
                            final Character c2)
   {
      for (Pair pair : pairs)
         if (pair.contains (c1, c2))
            return true;
      return false;
   }
   
   void addFriends (final Character c1, final Character c2)
   {
      friends.add (new Pair (c1, c2));
   }
   
   void addEnemies (final Character c1, final Character c2)
   {
      enemies.add (new Pair (c1, c2));
   }
   
   // The given group is considered valid if doesn't contain any set of 
   // enemies, and it doesn't contain any subset (other than the full set)
   // of friends.
   boolean isValid (final SortedSet<? extends Character> group)
   {
      for (Pair pair : enemies)
         if (group.contains (pair.p1) && group.contains (pair.p2))
            return false;
      
      for (Pair pair : friends)
      {
         if (group.contains (pair.p1) && !group.contains (pair.p2))
            return false;
         if (group.contains (pair.p2) && !group.contains (pair.p1))
            return false;
      }
      
      return true;
   }
   
   private Character findCharacter (final String name)
   {
      for (Character ch : chars)
         if (ch.getName().equals (name))
            return ch;
      return null;
   }
   
   void write (final PrintWriter out)
   {
      for (Pair pair : friends)
         out.println (pair.p1 + "+" + pair.p2);
      for (Pair pair : enemies)
         out.println (pair.p1 + "-" + pair.p2);
   }
   
   private static class MyTableModel extends DefaultTableModel
   {
      private static final long serialVersionUID = 1L;
      
      @Override
      public Class<?> getColumnClass (final int c)
      {
         return c == 0 ? String.class : Relationship.class;
      }
   }
   
   private static class Pair
   {
      private Character p1, p2;
      
      Pair (final Character c1, final Character c2)
      {
         p1 = c1;
         p2 = c2;
      }
      
      boolean contains (final Character ch)
      {
         return p1.equals (ch) || p2.equals (ch);
      }
      
      boolean contains (final Character c1, final Character c2)
      {
         return contains (c1) && contains (c2); 
      }
   }
   
   public static void main (final String[] args)
   {
      RelationshipModel model = new RelationshipModel();
      model.setCharacters (CharacterWithDeeds.read2 ("saves/sample.chr"));
      model.populate();
      
      JTable tbl = new JTable (model.getTable());
      tbl.getTableHeader().setReorderingAllowed (true);
      tbl.setPreferredScrollableViewportSize (new Dimension (600, 300));
      tbl.setDefaultRenderer (Relationship.class, new RelationshipRenderer());
      tbl.setDefaultEditor (Relationship.class, new RelationshipEditor());
         
      JScrollPane scroll = new JScrollPane (tbl);
      ComponentTools.open (scroll, model.getClass().getName());
   }
}
