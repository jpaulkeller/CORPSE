package lotro.models;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import lotro.my.reports.CharacterFilter;

public class Alliance extends Kinship
{
   private List<Kinship> kinships;
   
   public Alliance (final String name)
   {
      super (name);
      this.kinships = new ArrayList<Kinship>();
   }
   
   public void addKinship (final Kinship kinship)
   {
      if (!kinships.contains (kinship))
         kinships.add (kinship);
   }

   public List<Kinship> getKinships()
   {
      return kinships;
   }
   
   public Kinship getKinship (final String kinName)
   {
      for (Kinship kinship : kinships)
         if (kinship.getName().equals (kinName))
            return kinship;
      return null;
   }

   @Override
   public void addFilter (final CharacterFilter filter)
   {
      for (Kinship kinship : kinships)
         kinship.addFilter (filter);
   }

   @Override
   public void removeFilter (final CharacterFilter filter)
   {
      for (Kinship kinship : kinships)
         kinship.removeFilter (filter);
   }

   @Override
   public void setFilter (final CharacterFilter filter)
   {
      for (Kinship kinship : kinships)
         kinship.setFilter (filter);
   }

   @Override
   public SortedMap<String, Character> getCharacters()
   {
      SortedMap<String, Character> characters = new TreeMap<String, Character>();
      for (Kinship kinship : kinships)
         characters.putAll (kinship.getCharacters());
      return characters;
   }

   @Override
   public List<Character> getCharacters (Rank characterRank)
   {
      List<Character> characters = new ArrayList<Character>();
      for (Kinship kinship : kinships)
         characters.addAll (kinship.getCharacters (characterRank));
      return characters;
   }

   @Override
   public List<Character> getMembers()
   {
      List<Character> characters = new ArrayList<Character>();
      for (Kinship kinship : kinships)
         characters.addAll (kinship.getMembers());
      return characters;
   }

   @Override
   public List<Character> getOfficers()
   {
      List<Character> characters = new ArrayList<Character>();
      for (Kinship kinship : kinships)
         characters.addAll (kinship.getOfficers());
      return characters;
   }

   @Override
   public List<Character> getRecruits()
   {
      List<Character> characters = new ArrayList<Character>();
      for (Kinship kinship : kinships)
         characters.addAll (kinship.getRecruits());
      return characters;
   }

   @Override
   public List<Player> getPlayers()
   {
      List<Player> players = new ArrayList<Player>();
      for (Kinship kinship : kinships)
         players.addAll (kinship.getPlayers());
      return players;
   }

   @Override
   public String getWorld()
   {
      return kinships.get (0).getWorld();
   }
   
   public int size()
   {
      return kinships.size();
   }
   
   @Override
   public int size (boolean filtered, boolean scraped)
   {
      int size = 0;
      for (Kinship kinship : kinships)
         size += kinship.size (filtered, scraped);
      return size;
   }
}
