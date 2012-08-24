package lotro.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import lotro.my.reports.CharacterFilter;

public class Kinship
{
   private String name;
   private String world; // server
   private int rank;
   
   private Character leader;
   private SortedMap<String, Character> characters = new TreeMap<String, Character>();

   private List<CharacterFilter> filters = new ArrayList<CharacterFilter>();

   public Kinship (final String name)
   {
      setName (name);
   }
   
   public String getName()
   {
      return name;
   }
   
   public void setName (final String name)
   {
      this.name = name;
   }

   public String getWorld()
   {
      return world;
   }

   public void setWorld (final String world)
   {
      this.world = world;
   }

   public int getRank()
   {
      return rank;
   }

   public void setRank (final int rank)
   {
      this.rank = rank;
   }

   public void addCharacter (final Character character)
   {
      characters.put (character.getName(), character);
      if (character.getRank() == Rank.Leader)
         leader = character;
   }
   
   public void setFilter (final CharacterFilter filter)
   {
      filters.clear();
      if (filter != null)
         addFilter (filter);
   }
   
   public void addFilter (final CharacterFilter filter)
   {
      filters.add (filter);
   }

   public void removeFilter (final CharacterFilter filter)
   {
      filters.remove (filter);
   }

   public List<Player> getPlayers()
   {
      List<Player> players = new ArrayList<Player>();
      for (Character ch : getCharacters().values())
         if (!players.contains (ch.getPlayer()))
            players.add (ch.getPlayer());
      Collections.sort (players);
      return players;
   }
   
   public SortedMap<String, Character> getCharacters()
   {
      SortedMap<String, Character> filtered = new TreeMap<String, Character>();
      filtered.putAll (characters);
      
      if (!filters.isEmpty())
         for (Character ch : characters.values())
            for (CharacterFilter filter : filters)
               if (!filter.include (ch))
                  filtered.remove (ch.getName());
      
      return filtered;
   }

   public int size (final boolean filtered, final boolean scraped)
   {
      int count = 0;
      
      if (scraped)
      {
         Collection<Character> chars = filtered ? getCharacters().values() : characters.values();  
         for (Character ch : chars)
            if (ch.getScraped() != null)
               count++;
      }
      else if (filtered)
         count = getCharacters().size();
      else
         count = characters.size();
      
      return count;
   }
   
   public Character getLeader()
   {
      return leader;
   }
   
   public List<Character> getOfficers()
   {
      return getCharacters (Rank.Officer);
   }
   
   public List<Character> getMembers()
   {
      return getCharacters (Rank.Member);
   }
   
   public List<Character> getRecruits()
   {
      return getCharacters (Rank.Recruit);
   }
   
   public List<Character> getCharacters (final Rank characterRank)
   {
      List<Character> list = new ArrayList<Character>();
      for (Character ch : characters.values())
         if (ch.getRank() == characterRank)
            list.add (ch);
      return list;
   }
}
