package lotro.models;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import file.FileUtils;

public class AssignmentModel implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private static final Pattern ASSIGNMENT_PATTERN = // Frodo:+++--+---++
      Pattern.compile ("([^:]+):([-+]+)");
   private static final Pattern RELATIONSHIP_PATTERN = // Frodo+Sam
      Pattern.compile ("(?i:([a-z]+)([-+])([a-z]+))");
   
   private static final int DEED_COLUMNS = 4; // Region, Name, Trait, Level
   private static final int REGION_COL = 0;
   private static final int NAME_COL   = 1;
   
   private DefaultTableModel table;
   private DeedModel deedModel;
   private CharacterModel charModel;
   private RelationshipModel relaModel;

   public AssignmentModel (final String groupFile)
   {
      table = new MyTableModel();
      deedModel = new DeedModel();
      charModel = new CharacterModel();
      relaModel = new RelationshipModel();
      read (groupFile);
   }
   
   public void addCharacter (final CharacterWithDeeds character)
   {
      charModel.addCharacter (character);
      relaModel.addCharacter (character);
   }
   
   public void addDeed (final Deed deed)
   {
      deedModel.addDeed (deed);
   }
   
   public TableModel getTable()
   {
      return table;
   }
   
   public TableModel getDeedTable()
   {
      return deedModel.getTable();
   }
   
   public TableModel getCharacterTable()
   {
      return charModel.getTable();
   }
   
   public TableModel getRelationshipTable()
   {
      return relaModel.getTable();
   }
   
   public SortedSet<Deed> getDeeds()
   {
      return deedModel.getDeeds();
   }

   public int getDeedCount()
   {
      return deedModel.size();
   }
   
   public int getCharacterCount()
   {
      return charModel.size();
   }
   
   public int getRelationshipCount()
   {
      return relaModel.getRelationshipCount();
   }
   
   public SortedSet<CharacterWithDeeds> getCharacters()
   {
      SortedSet<CharacterWithDeeds> set = new TreeSet<CharacterWithDeeds>();
      for (Character ch : charModel.getCharacters())
         if (ch instanceof CharacterWithDeeds)
            set.add ((CharacterWithDeeds) ch);
      return set;
   }
   
   public SortedSet<CharacterWithDeeds> getCharactersWithDeeds()
   {
      SortedSet<CharacterWithDeeds> set = new TreeSet<CharacterWithDeeds>();
      for (Character ch : charModel.getCharacters())
         if (ch instanceof CharacterWithDeeds)
            if (((CharacterWithDeeds) ch).getNeededCount() > 0)
               set.add ((CharacterWithDeeds) ch);
      return set;
   }
   
   public void removeCharacter (final String name)
   {
      CharacterWithDeeds ch = (CharacterWithDeeds) charModel.get (name);
      if (ch != null)
      {
         charModel.removeCharacter (ch);
         relaModel.removeCharacter (ch);
      }
   }
   
   public void removeDeed (final Deed deed)
   {
      deedModel.removeDeed (deed);
   }
   
   public void clear()
   {
      for (CharacterWithDeeds ch : getCharacters())
         for (Deed deed : getDeeds())
            ch.clear (deed);
   }

   public void clearDeeds()
   {
      deedModel.clear();
   }

   public void clearRelationships()
   {
      relaModel.clear();
   }

   public void populate()
   {
      // clear the model
      while (table.getRowCount() > 0)
         table.removeRow (0);
      table.setColumnCount (0);
      
      table.addColumn ("Region");
      table.addColumn ("Deed");
      table.addColumn ("Trait");
      table.addColumn ("Level");
      for (String charName : charModel.getCharacterNames())
         table.addColumn (charName);
      
      for (Deed deed : getDeeds())
         addRow (deed);

      deedModel.populate();
      charModel.populate();
      populateRelationships();
   }

   public void populateRelationships()
   {
      relaModel.populate();
   }
   
   private void addRow (final Deed deed)
   {
      Vector<Object> row = new Vector<Object>();
      row.add (deed.getRegion());
      row.add (deed.getName());
      row.add (deed.getTrait());
      row.add (deed.getLevel());
      for (CharacterWithDeeds ch : getCharacters())
         row.add (ch.getAssignment (deed));
      table.addRow (row);
   }
   
   public boolean isValid (final Group group)
   {
      return relaModel.isValid (group.getMembers());
   }
   
   // iterate over the table, and update each Charater's needed list
   
   public void updateFromTable()
   {
      for (int row = 0, rows = table.getRowCount(); row < rows; row++)
      {
         String region = (String) table.getValueAt (row, REGION_COL);
         String name   = (String) table.getValueAt (row, NAME_COL);
         String key = Deed.getKey (region, name);
         Deed deed = deedModel.get (key);

         // for each character
         for (int c = 0, cols = charModel.size(); c < cols; c++)
         {
            int col = c + DEED_COLUMNS;
            boolean needed = ((Assignment) table.getValueAt (row, col)).isNeeded();
            CharacterWithDeeds ch = (CharacterWithDeeds) charModel.get (table.getColumnName (col));
            ch.assign (deed, needed);
         }
      }

      deedModel.updateFromTable();
      charModel.updateFromTable();
      relaModel.updateFromTable();
   }
   
   public void write (final String file)
   {
      PrintWriter out = null;
      try
      {
         out = new PrintWriter (file);
         for (Deed deed : getDeeds())
            out.println (deed.getKey());
         for (CharacterWithDeeds ch : getCharacters())
         {
            StringBuilder sb = new StringBuilder (ch.encode());
            sb.append (":");
            for (Deed deed : getDeeds())
               sb.append (ch.needs (deed) ? "+" : "-");
            out.println (sb.toString());
         }
         relaModel.write (out);
         out.flush();
      }
      catch (IOException x)
      {
         System.err.println (x);
      }
      finally
      {
         if (out != null)
            out.close();
      }
   }

   // Reads the given .grp file, and appends the entries into the model.
   
   public void read (final String file)
   {
      Map<String, Deed> allDeeds = Deed.read (Deed.FILE);
      List<Deed> groupDeeds = new ArrayList<Deed>();
      
      List<String> lines = FileUtils.getList (file, FileUtils.UTF8, true);
      for (String line : lines)
         parseLine (allDeeds, groupDeeds, line);
      
      if (relaModel.getCharacterCount() == 0)
         relaModel.setCharacters (getCharacters());
   }

   private void parseLine (final Map<String, Deed> allDeeds, 
                           final List<Deed> groupDeeds,
                           final String line)
   {
      Matcher m = ASSIGNMENT_PATTERN.matcher (line);
      if (m.matches())
         readAssignments (groupDeeds, m);
      else if ((m = RELATIONSHIP_PATTERN.matcher (line)).matches())
         readRelationship (m);
      else
         readDeed (allDeeds, groupDeeds, line);
   }

   private void readDeed (final Map<String, Deed> allDeeds,
                          final List<Deed> groupDeeds,
                          final String line)
   {
      Deed deed = allDeeds.get (line);
      if (deed == null)
      {
         System.err.println ("Missing Deed: " + line);
         deed = new Deed ("", line, "", "", 0);
      }
      groupDeeds.add (deed);
      deedModel.addDeed (deed);
   }

   private void readAssignments (final List<Deed> deedList, final Matcher m)
   {
      String encodedChar = m.group (1);
      CharacterWithDeeds ch = new CharacterWithDeeds();
      if (ch.parseCharacter (encodedChar))
      {
         charModel.addCharacter (ch);
         String assignments = m.group (2);
         for (int i = 0, len = assignments.length(); i < len; i++)
            ch.assign (deedList.get (i), assignments.charAt (i) == '+');
      }
   }

   private void readRelationship (final Matcher m)
   {
      if (relaModel.getCharacterCount() == 0)
         relaModel.setCharacters (getCharacters());
      
      Character ch1 = charModel.get (m.group (1));
      Character ch2 = charModel.get (m.group (3));
      if (ch1 != null && ch2 != null && !ch1.equals (ch2))
      {
         boolean friend = m.group (2).equals ("+");
         if (friend)
            relaModel.addFriends (ch1, ch2);
         else
            relaModel.addEnemies (ch1, ch2);
      }
      else
         System.out.println ("Invalid relationship: " + m.group (0));
   }

   static class MyTableModel extends DefaultTableModel
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getColumnClass (final int c)
      {
         return getValueAt (0, c).getClass();
      }
   }
}
