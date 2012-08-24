package lotro.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import lotro.web.Dropbox;

import file.FileUtils;

public class CharacterWithDeeds extends Character
{
   public static final SortedMap<String, CharacterWithDeeds> CHARACTERS =
      new TreeMap<String, CharacterWithDeeds>();

   private Set<Deed> needed = new HashSet<Deed>();
   private Map<Deed, Assignment> assignments = new HashMap<Deed, Assignment>();

   protected CharacterWithDeeds()
   {
   }

   public CharacterWithDeeds (final Character ch)
   {
      super (ch.getPlayer().getName(), ch.getName(), 
             ch.getRace(), ch.getKlass(), ch.getLevel());
   }
   
   public CharacterWithDeeds (final String player, 
                              final String name, 
                              final Race race, 
                              final Klass klass, 
                              final int level)
   {
      super (player, name, race, klass, level);
   }
   
   public static void load (final String path)
   {
      SortedSet<CharacterWithDeeds> localChars = CharacterWithDeeds.read2 (path);
      for (CharacterWithDeeds ch : localChars)
         CHARACTERS.put (ch.getName(), ch);
   }
   
   public static SortedSet<CharacterWithDeeds> read2 (final String path)
   {
      SortedSet<CharacterWithDeeds> characters = new TreeSet<CharacterWithDeeds>();
      
      List<String> lines = FileUtils.getList (path, FileUtils.UTF8, true);
      for (String line : lines)
      {
         CharacterWithDeeds ch = new CharacterWithDeeds();
         if (ch.parseCharacter (line))
            characters.add (ch);
      }
      
      return characters;
   }

   public int getNeededCount()
   {
      return needed.size();
   }
   
   public void assign (final Deed deed, final boolean needs)
   {
      boolean changed = needs ? needed.add (deed) : needed.remove (deed);
      if (changed)
      {
         Assignment assignment = getAssignment (deed);
         assignment.setOrganized (false);
         assignment.setNeeded (needs);
      }
   }
   
   public void clear (final Deed deed)
   {
      needed.remove (deed);
      Assignment assignment = getAssignment (deed);
      assignment.setOrganized (false);
      assignment.setNeeded (false);
   }
   
   public Assignment getAssignment (final Deed deed)
   {
      Assignment assignment = assignments.get (deed);
      if (assignment == null)
      {
         assignment = new Assignment();
         assignments.put (deed, assignment);
      }
      return assignment;
   }
   
   public boolean needs (final Deed deed)
   {
      return needed.contains (deed);
   }
   
   public static void main (final String[] args)
   {
      CharacterWithDeeds.load (Dropbox.CHAR_PATH);
      for (CharacterWithDeeds ch : CHARACTERS.values())
         System.out.println (ch);
   }
}

