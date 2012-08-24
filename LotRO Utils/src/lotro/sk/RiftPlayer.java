package lotro.sk;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import lotro.models.Character;
import file.FileUtils;

public class RiftPlayer implements Comparable<RiftPlayer>
{
   public static final Map<String, RiftPlayer> PLAYERS = new HashMap<String, RiftPlayer>();
   private static final String HOME = System.getProperty ("user.home");
   public static final String SK_LIST =
      HOME + "/My Documents/My Dropbox/Public/Palantiri/sk";
   
   private static DecimalFormat df = new DecimalFormat ("000");
   
   private String name;
   private int order;
   
   public RiftPlayer (final String name)
   {
      setName (name);
      setOrder (PLAYERS.size() + 1);
   }
   
   public String getName()
   {
      return name;
   }

   public void setName (final String name)
   {
      this.name = name;
   }

   public int getOrder()
   {
      return order;
   }

   public void setOrder (final int order)
   {
      this.order = order;
   }

   @Override public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append (df.format (order));
      sb.append (" - ");
      sb.append (name);
      
      Queue<Character> characters = new LinkedList<Character>();
      for (Character ch : Character.getCharacters())
         if (ch.getPlayer().getName().equals (name))
            characters.add (ch);
      if (!characters.isEmpty())
      {
         sb.append (" (");
         sb.append (characters.poll());
         while (!characters.isEmpty())
            sb.append (", " + characters.poll());
         sb.append (")");
      }
      
      return sb.toString();
   }
   
   public int compareTo (final RiftPlayer o)
   {
      int compare = getOrder() - o.getOrder();
      if (compare == 0)
         compare = getName().compareTo (o.getName());
      return compare;
   }

   public static void load()
   {
      List<String> lines = FileUtils.getList (SK_LIST, FileUtils.UTF8, true);
      for (String line : lines)
      {
         RiftPlayer player = new RiftPlayer (line);
         PLAYERS.put (player.getName(), player); 
      }
   }
   
   public static void publish (final Collection<RiftPlayer> players)
   {
      List<String> lines = new ArrayList<String>();
      for (RiftPlayer player : players)
         lines.add (player.getName());
      FileUtils.writeList (lines, SK_LIST, false);
   }
}
