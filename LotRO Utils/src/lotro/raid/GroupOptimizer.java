package lotro.raid;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.models.Skill;
import lotro.my.reports.FilterFactory;
import lotro.my.xml.KinshipXML;
import lotro.web.Dropbox;

public class GroupOptimizer
{
   public static final DecimalFormat DF = new DecimalFormat();
   static { DF.setGroupingSize (3); }

   private Node root;
   private List<Node> current;
   private int length;
   
   private JProgressBar progress; 
   private int maxGroupSize;
   private Collection<Signup> signups;
   private int bestScore = -1;
   private List<Node> bestPath;
   private long count;
   private long total; // number of many possible groups
   
   public GroupOptimizer (final JProgressBar progress,
                          final Collection<Signup> signups,
                          final int maxGroupSize)
   {
      this.progress = progress;
      this.signups = signups;
      this.maxGroupSize = maxGroupSize;
      
      root = new Node (null);
      current = new ArrayList<Node>();
      current.add (root);
      
      total = 1;
      for (Signup signup : signups)
         if (!signup.isBackup())
            total *= (signup.getCharacters().size() + 1); // +1 for groups w/o this player
      
      int score = maxGroupSize + 1;
      for (Signup signup : signups)
         if (!signup.isBackup())
            add (signup, Math.max (score--, 1));
   }
   
   public int length()
   {
      return length; 
   }
   
   public long getPossibleGroupCount()
   {
      return total;
   }
   
   public int getBestScore()
   {
      return bestScore;
   }

   private void add (final Signup signup, final int score)
   {
      length++;
      List<Node> newNodes = new ArrayList<Node>();
      for (Character ch : signup.getCharacters())
      {
         signup.setScore (score);
         newNodes.add (new Node (ch));
      }
      newNodes.add (new Node (null)); // to skip the Character
      
      for (Node node : current)
         for (Node newNode : newNodes)
            node.next.add (newNode);
      
      current.clear();
      current.addAll (newNodes);
   }

   public List<Character> getBestGroup()
   {
      List<Character> group = new ArrayList<Character>();
      int signupSize = 0; // don't count backups
      for (Signup signup : signups)
         if (!signup.isBackup())
            signupSize++;
      findPath (root, new ArrayList<Node>(), Math.min (maxGroupSize, signupSize));
      if (bestPath != null)
         for (Node node : bestPath)
            if (node.ch != null)
               group.add (node.ch);
      return group;
   }
   
   public void findPath (final Node fromNode, final List<Node> path, final int targetSize)
   {
      path.add (fromNode);
      
      if (fromNode.next.isEmpty()) // end of graph
      {
         count++;
         if (getGroupSize (path) == targetSize)
         {
            int score = score (path);
            if (score > bestScore)
            {
               bestScore = score;
               bestPath = path;
            }
         }
         
         if (count % 1000 == 0)
            SwingUtilities.invokeLater (new Runnable()
            {
               public void run()
               {
                  int percent = (int) (count * 100L / total);
                  progress.setValue (percent);
                  progress.setString ("Scoring " + DF.format (count) + " of " +
                                      DF.format (total) + " raid groups...");
               }
            });

         return;
      }

      for (Node next : fromNode.next)
         findPath (next, new ArrayList<Node> (path), targetSize);
   }
   
   // Warning: this can be a very large list!
   
   public List<List<Character>> getAllGroups()
   {
      List<List<Character>> groups = new ArrayList<List<Character>>();
      List<List<Node>> paths = new ArrayList<List<Node>>();
      int targetSize = Math.min (maxGroupSize, signups.size());
      findPaths (root, new ArrayList<Node>(), paths, targetSize);
      for (List<Node> path : paths)
      {
         List<Character> group = new ArrayList<Character>();
         for (Node node : path)
            if (node.ch != null)
               group.add (node.ch);
         groups.add (group);
      }
      return groups;
   }
   
   public void findPaths (final Node fromNode, final List<Node> path,
                          final List<List<Node>> paths,
                          final int targetSize)
   {
      path.add (fromNode);
      
      if (fromNode.next.isEmpty()) // end of graph
      {
         count++;
         if (getGroupSize (path) == targetSize)
            paths.add (path);
         return;
      }

      for (Node next : fromNode.next)
         findPaths (next, new ArrayList<Node> (path), paths, targetSize);
   }
   
   private int score (final List<Node> nodes)
   {
      List<Character> group = new ArrayList<Character>();
      for (Node node : nodes)
         group.add (node.ch);
      return score (group, false);
   }
   
   public int score (final List<Character> group, final boolean trace)
   {
      int score = 0;

      // add points based on sign-up order
      for (Character ch : group)
         if (ch != null)
            score += ch.getPlayer().getScore();

      // add points based on preferences
      for (Character ch : group)
         if (ch != null)
            score += ch.getScore();

      // add points based on class skills
      for (Character ch : group)
         if (ch != null)
            for (Skill skill : Skill.values())
               score += Skill.getPercent (ch.getKlass(), skill); // * ch.getLevel();
      
      /*
      // add points based on preferred group composition
      List<Character> tempChars = new ArrayList<Character> (group);
      for (CompSlot slot : comp.getSlots())
      {
         Iterator<Character> charIter = tempChars.iterator();
         while (charIter.hasNext())
         {
            Character ch = charIter.next();
            if (ch != null && ch.getKlass() == slot.getKlass())
            {
               charIter.remove();
               score += slot.getScore();
               break;
            }
         }
      }
      */
      
      return score;
   }
   
   private int getGroupSize (final List<Node> path) // count all Characters
   {
      int size = 0;
      for (Node node : path)
         if (node.ch != null)
            size++;
      return size;
   }
   
   private static final class Node
   {
      private Character ch;
      private List<Node> next;
      
      private Node (final Character ch)
      {
         this.ch = ch;
         this.next = new ArrayList<Node>();
      }
   }
   
   public static void main (final String[] args)
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (false);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      kinship.setFilter (FilterFactory.getLevelFilter (48));
      
      String dropbox = Dropbox.get().getPath ("/raids/Test 1.signup");
      Map<Player, Signup> signups = Signup.loadFromFile (kinship, dropbox);
      for (Player player : signups.keySet())
         player.setScore (signups.get (player).getScore());
      
      String raid = Dropbox.get().getPath ("/raids/Rift Day 3 Balrog.raid");
      Composition comp = Composition.loadFromFile (raid);
      GroupOptimizer optimizer = new GroupOptimizer (null, signups.values(), comp.getMax());
      
      System.out.println ("\nTotal: " + optimizer.getPossibleGroupCount());
         
      List<Character> group = optimizer.getBestGroup();
      System.out.print ("Count: " + optimizer.count + "\nScore: ");
      System.out.print (optimizer.bestScore + ": ");
      for (Character ch : group)
         System.out.print (ch + ", ");
      System.out.println();
   }
}
