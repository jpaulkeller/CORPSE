package lotro.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotro.web.Dropbox;
import file.FileUtils;

public class Character implements Comparable<Character>
{
   public static final int MAX_LEVEL = 85; 
   
   // {Player}, Name, Race, Class, Level
   private static final Pattern CHAR_PATTERN = Pattern.compile
      (" *([^,]*), *([A-Za-z]+), *([A-Za-z]+)?, *([-A-Za-z]+)?(?:, *([0-9]+)?)? *");
   
   private static final SortedMap<String, Character> CHARACTERS =
      new TreeMap<String, Character>();
   
   private Player player;
   private String world;
   private String kinship;
   
   private String name;
   private Race race = Race.Unknown;
   private Klass klass = Klass.Unknown;
   private Rank rank = Rank.Unknown; // within a kinship
   private int level;
   private Craft craft;
   
   private Map<Stats, Integer> stats = new LinkedHashMap<Stats, Integer>();
   
   private Equipment equipment;
   
   private Date scraped;
   private int score;
   private int hash;
   
   private Map<String, String> props = new LinkedHashMap<String, String>();

   protected Character()
   {
      this.race = Race.Unknown;
      this.klass = Klass.Unknown;
      this.craft = new Craft (Vocation.Unknown);
   }
   
   public Character (final String name)
   {
      this();
      setName (name);
   }
   
   public Character (final String player, 
                     final String name, 
                     final Race race, 
                     final Klass klass, 
                     final int level)
   {
      this (name);
      this.player = Player.get (player);
      this.race = race;
      this.klass = klass;
      this.level = level;
   }
   
   public static Character get (final String name)
   {
      return CHARACTERS.get (name);
   }
   
   public static Collection<Character> getCharacters()
   {
      return CHARACTERS.values();
   }
   
   public boolean isFreep()
   {
      return !Race.CREEPS.contains (race);
   }
   
   public void setName (final String name)
   {
      this.name = name.substring (0, 1).toUpperCase() + name.substring (1).toLowerCase();
      this.hash = name.hashCode();
   }
   
   public String getName()
   {
      return name;
   }
   
   public String getWorld()
   {
      return world;
   }

   public void setWorld (final String world)
   {
      this.world = world;
   }

   public String getKinship()
   {
      return kinship;
   }

   public void setKinship (final String kinship)
   {
      this.kinship = kinship;
   }

   public void setPlayer (final Player player)
   {
      this.player = player;
   }

   public Player getPlayer()
   {
      return player;
   }
   
   public void setRace (final Race race)
   {
      this.race = race;
   }

   public Race getRace()
   {
      return race;
   }

   public void setRank (final Rank rank)
   {
      this.rank = rank;
   }

   public Rank getRank()
   {
      return rank;
   }

   public void setKlass (final Klass klass)
   {
      this.klass = klass;
   }

   public Klass getKlass()
   {
      return klass;
   }
   
   public void setLevel (final int level)
   {
      this.level = level;
   }

   public int getLevel()
   {
      return level;
   }

   public Craft getCraft()
   {
      return craft;
   }

   public void setCraft (final Craft craft)
   {
      this.craft = craft;
   }

   public int getStat (final Stats stat)
   {
      Number value = stats.get (stat);
      return value != null ? value.intValue() : 0;
   }
   
   public void putStat (final Stats stat, final int value)
   {
      stats.put (stat, value);
   }
   
   public Equipment getEquipment()
   {
      return equipment;
   }

   public void setEquipment (final Equipment equipment)
   {
      this.equipment = equipment;
   }
   
   public Date getScraped()
   {
      return scraped;
   }

   public void setScraped (final Date date)
   {
      scraped = date;
   }
   
   public void setScore (final int score)
   {
      this.score = score;
   }
   
   public int getScore()
   {
      return score;
   }
   
   public int getScore (final Skill skill, final int targetLevel, final int raidSize)
   {
      int levelDelta = getLevel() - targetLevel;
      float weightedScore = Skill.getPercent (getKlass(), skill) * 100;
      weightedScore += weightedScore * (levelDelta * 0.2); // consider level
      weightedScore /= (raidSize / 6f); // consider raid size
      return Math.round (Math.max (0, weightedScore));
   }
   
   public Set<String> getProperties()
   {
      return props.keySet();
   }
   
   public void putProp (final String key, final String value)
   {
      props.put (key, value);
   }
   
   public String getProp (final String key)
   {
      return props.get (key);
   }
   
   @Override
   public String toString()
   {
      return name + " (" + klass.abbrev() + level + ")";
   }
   
   public String encode()
   {
      StringBuilder sb = new StringBuilder();
      sb.append (player);
      sb.append (", ");
      sb.append (name);
      sb.append (", ");
      sb.append (race);
      sb.append (", ");
      sb.append (klass);
      sb.append (", ");
      sb.append (level);
      return sb.toString();
   }

   public int compareTo (final Character o)
   {
      return name.compareTo (o.name);
   }
   
   @Override
   public boolean equals (final Object obj)
   {
      if (obj instanceof Character)
         return name.equals (((Character) obj).name);
      return false;
   }

   @Override
   public int hashCode()
   {
      return hash;
   }
   
   protected boolean parseCharacter (final String line)
   {
      Matcher m = CHAR_PATTERN.matcher (line);
      if (m.matches())
      {
         setPlayer (Player.get (m.group (1)));
         setName (m.group (2));
         setRace (Race.parse (m.group (3)));
         setKlass (Klass.parse (m.group (4)));
         if (m.group (5) != null)
            setLevel (Integer.parseInt (m.group (5)));
         return true;
      }

      System.err.println ("Ignoring Character: " + line);
      return false;
   }
   
   public static void load (final String path)
   {
      SortedSet<Character> localChars = Character.read (path);
      for (Character ch : localChars)
         CHARACTERS.put (ch.getName(), ch);
   }
   
   public static SortedSet<Character> read (final String path)
   {
      SortedSet<Character> characters = new TreeSet<Character>();
      Character ch = new Character();
      
      List<String> lines = FileUtils.getList (path, FileUtils.UTF8, true);
      for (String line : lines)
      {
         if (ch.parseCharacter (line))
         {
            characters.add (ch);
            ch = new Character();
         }
      }
      
      return characters;
   }

   public static void write (final Collection<? extends Character> characters, 
                             final String path)
   {
      Collection<String> lines = new ArrayList<String>();
      for (Character ch : characters)
         lines.add (ch.encode());
      FileUtils.writeList (lines, path, false);
   }

   public static class ByClass implements Comparator<Character>
   {
      public int compare (final Character c1, final Character c2)
      {
         int sort = c1.getKlass().compareTo (c2.getKlass());
         if (sort == 0)
         {
            sort = c2.getLevel() - c1.getLevel();
            if (sort == 0)
               sort = c1.compareTo (c2); // by name
         }
         return sort;
      }
   }
   
   public static class ByVocation implements Comparator<Character>
   {
      public int compare (final Character c1, final Character c2)
      {
         int sort = c1.getCraft().getVocation().compareTo (c2.getCraft().getVocation());
         if (sort == 0)
         {
            sort = c2.getLevel() - c1.getLevel();
            if (sort == 0)
               sort = c1.compareTo (c2); // by name
         }
         return sort;
      }
   }
   
   public static void main (final String[] args)
   {
      Character.load (Dropbox.CHAR_PATH);
      for (Character ch : CHARACTERS.values())
         System.out.println (ch);
   }
}

