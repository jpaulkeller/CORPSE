package lotro.raid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.my.reports.FilterFactory;
import lotro.my.xml.CharacterXML;
import lotro.my.xml.KinshipXML;
import lotro.web.Dropbox;
import file.FileUtils;

public class Signup implements Comparable<Signup>
{
   private static final Pattern SIGNUP_PATTERN = 
      Pattern.compile ("([A-Za-z]+) *(-?[0-9]+)?,? *");

   private Player player;
   private Set<Character> characters;
   private int score;
   private int hash;

   public Signup()
   {
      characters = new LinkedHashSet<Character>();
   }

   public Signup (final List<Character> characters)
   {
      this.characters = new LinkedHashSet<Character> (characters);
      if (!this.characters.isEmpty())
         setPlayer (this.characters.iterator().next().getPlayer());
   }

   public void setPlayer (final Player player)
   {
      this.player = player;
      if (player != null)
         this.hash = player.hashCode();
   }

   public Player getPlayer()
   {
      return player;
   }

   public void addCharacter (final Character ch)
   {
      if (characters.isEmpty())
         setPlayer (ch.getPlayer());
      characters.add (ch);
   }

   public Set<Character> getCharacters()
   {
      return characters;
   }

   public boolean isBackup()
   {
      for (Character ch : characters)
         if (ch.getScore() >= 0)
            return false;
      return true; // if all character scores are < 0
   }

   public int getScore()
   {
      return score;
   }

   public void setScore (final int score)
   {
      this.score = score;
   }

   public int compareTo (final Signup o)
   {
      return player.compareTo (o.player);
   }

   @Override
   public boolean equals (final Object obj)
   {
      if (obj instanceof Signup)
         return player.equals (((Signup) obj).player);
      return false;
   }

   @Override
   public int hashCode()
   {
      return hash;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append (getPlayer() + ": ");
      if (!characters.isEmpty())
      {
         Iterator<Character> iter = characters.iterator();
         Character first = iter.next();
         sb.append (getCharacterLabel (first));
         while (iter.hasNext())
            sb.append (", " + getCharacterLabel (iter.next()));
      }
      return sb.toString();
   }

   private String getCharacterLabel (final Character ch)
   {
      StringBuilder sb = new StringBuilder();
      sb.append (ch.getName());
      sb.append (" (");
      sb.append (ch.getKlass().abbrev());
      if (ch.getLevel() > 0)
         sb.append (ch.getLevel());
      sb.append (")");
      return sb.toString();
   }

   public static Map<Player, Signup> loadFromFile (final Kinship kinship,
                                                   final String path)
   {
      Map<Player, Signup> signups = new LinkedHashMap<Player, Signup>();
      List<String> lines = FileUtils.getList (path, FileUtils.UTF8, true);
      for (String line : lines)
      {
         List<Character> characters = parseSignup (kinship, line);
         if (!characters.isEmpty())
         {
            Player player = characters.get (0).getPlayer();
            Signup signup = signups.get (player);
            if (signup != null)
               for (Character ch : characters)
                  signup.addCharacter (ch);
            else
               signups.put (player, new Signup (characters));
         }
      }

      return signups;
   }

   private static List<Character> parseSignup (final Kinship kinship, final String line)
   {
      List<Character> characters = new ArrayList<Character>();

      Matcher m = SIGNUP_PATTERN.matcher (line);
      while (m.find())
      {
         boolean found = false;
         String name = m.group (1);
         String prefScore = m.group (2);
         Character ch = kinship.getCharacters().get (name);
         if (ch != null)
         {
            addCharacter (ch, prefScore, characters);
            found = true;
         }
         else // check for Player name
         {
            for (Character c : kinship.getCharacters().values())
               if (c.getPlayer().getName().equals (name))
               {
                  addCharacter (c, prefScore, characters);
                  found = true;
               }
         }
         
         if (!found)
         {
            System.err.println ("Unknown character: " + name + "; checking mylotro.com");
            ch = CharacterXML.getCharacter (kinship.getWorld(), name);
            // ch = new Character (name);
            // ch.setWorld (kinship.getWorld());
            ch.setPlayer (new Player ("[" + name + "]"));
            addCharacter (ch, null, characters);
         }
      }

      return characters;
   }

   private static void addCharacter (final Character ch, final String prefScore,
                                     final List<Character> characters)
   {
      int score = 0;
      if (prefScore != null)
         score = Integer.parseInt (prefScore);
      if (ch.getLevel() > 0)
         score += (ch.getLevel() - 50);
      ch.setScore (score);
      characters.add (ch);
   }

   public static void write (final Collection<Signup> signups, final String path)
   {
      Collection<String> lines = new ArrayList<String>();
      // TBD: one line per sign-up instead?
      for (Signup signup : signups)
         for (Character ch : signup.getCharacters())
            lines.add (ch.getName() + " " + ch.getScore());
      FileUtils.writeList (lines, path, false);
   }

   public static void main (final String[] args)
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (true);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      kinship.setFilter (FilterFactory.getLevelFilter (48));

      String dropbox = Dropbox.get().getPath ("/raids/SK-09-01.signup");
      Map<Player, Signup> signups = Signup.loadFromFile (kinship, dropbox);
      for (Signup signup : signups.values())
         System.out.println (signup);
   }
}
