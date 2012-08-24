package lotro.models;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lotro.web.Dropbox;
import file.FileUtils;

public class Player implements Comparable<Player>
{
   // private static final String HOME = System.getProperty ("user.home");
   // SK_LIST = HOME + "/My Documents/My Dropbox/Public/Palantiri/sk";
   
   private static final Map<String, Player> PLAYERS =
      new TreeMap<String, Player>();
   
   private String name;
   private int score;
   private int hash;

   public Player (final String name)
   {
      this.name = name;
      this.hash = name.hashCode();
      PLAYERS.put (name, this);
   }
   
   static
   {
      Character.load (Dropbox.CHAR_PATH); // get player names from DropBox
   }
   
   public static Player getPlayer (final String characterName)
   {
      Player player = null;
      Character dropBoxChar = Character.get (characterName);
      if (dropBoxChar != null)
         player = dropBoxChar.getPlayer();
      return player;
   }

   public static Player get (final String name)
   {
      Player player = PLAYERS.get (name);
      if (player == null && name != null)
         player = new Player (name);
      return player;
   }
   
   public String getName()
   {
      return name;
   }
   
   public void setScore (final int score)
   {
      this.score = score;
   }
   
   public void addScore (final int plusScore)
   {
      this.score += plusScore;
   }
   
   public int getScore()
   {
      return score;
   }
   
   @Override
   public String toString()
   {
      return name;
   }
   
   public int compareTo (final Player o)
   {
      return name.compareTo (o.name);
   }
   
   @Override
   public boolean equals (final Object obj)
   {
      if (obj instanceof Player)
         return name.equals (((Player) obj).name);
      return false;
   }

   @Override
   public int hashCode()
   {
      return hash;
   }

   public static void load (final String fileName)
   {
      List<String> lines = FileUtils.getList (fileName, FileUtils.UTF8, true);
      for (String line : lines)
      {
         Player player = new Player (line);
         PLAYERS.put (player.getName(), player);
      }
   }
}
