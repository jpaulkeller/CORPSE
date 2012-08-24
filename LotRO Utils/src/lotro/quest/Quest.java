package lotro.quest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

public class Quest implements Comparable<Quest>
{
   private String id; // name used by URL
   private String link;
   private String name;
   
   private String desc;
   private String category;
   private String bestower;
   private String text;
   private int level;
   
   private boolean oneOf; // if multiple pre-reqs, but only 1 needed
   private SortedSet<Quest> prereqs = new TreeSet<Quest>();
   private SortedSet<Quest> follows = new TreeSet<Quest>();
   
   private SortedSet<Reward> rewards = new TreeSet<Reward>();
   
   Quest (final String id)
   {
      this.id = decode (id);
      this.link = "http://lorebook.lotro.com/index.php/Quest:" + id;
      this.name = this.id; // default, should be replaced after construction
   }
   
   public void setName (final String name)
   {
      this.name = decode (name);
   }
   
   public String getName()
   {
      return name;
   }
   
   public String getCategory()
   {
      return category;
   }
   
   public void setDesc (final String desc)
   {
      this.desc = desc;
   }

   public void setCategory (final String category)
   {
      this.category = category;
   }

   public void setBestower (final String bestower)
   {
      this.bestower = bestower;
   }

   public void setLevel (final int level)
   {
      this.level = level;
   }

   public void setText (final String text)
   {
      String cleanText = text.replace ("<br />", " "); // strip embedded HTML
      cleanText = cleanText.replaceAll ("'([A-Z])", "$1"); // strip spurious quotes
      cleanText = cleanText.replaceAll ("([^a-zI])'", "$1"); // allow can't and I'm
      cleanText = cleanText.replace ("#FFFF00", "#FF0000"); // change yellow to red
      this.text = cleanText;
   }
   
   public static String decode (final String urlArg)
   {
      try
      {
         return URLDecoder.decode (urlArg, "UTF-8");
      }
      catch (UnsupportedEncodingException x)
      {
         System.out.println ("Failed to decode URL argument: " + urlArg);
         x.printStackTrace();
      }

      return null;
   }
   
   public void addPrereq (final Quest quest, final boolean or)
   {
      if (follows.contains (quest))
      {
         System.err.println ("Quest loop: " + this + " and " + quest);
         if (quest.level < this.level)
            follows.remove (quest);
         else
            return;
      }

      oneOf = or;
      prereqs.add (quest);
      quest.follows.add (this);
   }
   
   public void addSequel (final Quest quest)
   {
      if (prereqs.contains (quest))
      {
         System.err.println ("Quest loop: " + this + " and " + quest);
         if (quest.level > this.level)
            prereqs.remove (quest);
         else
            return;
      }

      follows.add (quest);
      quest.prereqs.add (this);
   }
   
   public void addReward (final Reward reward)
   {
      rewards.add (reward);
   }
   
   public Stack<Quest> getChain()
   {
      Stack<Quest> chain = new Stack<Quest>();
      if (follows.isEmpty()) // only for "final" quests
         addToChain (chain, this);
      return chain;
   }
         
   private void addToChain (final Stack<Quest> chain, final Quest quest)
   {
      if (!chain.contains (quest))
      {
         chain.push (quest);
         for (Quest prev : quest.prereqs)
            addToChain (chain, prev);
      }
   }
   
   public boolean isEpic()
   {
      return name.contains ("Intro:") ||
             name.contains ("Chapter") ||
             name.contains ("Epic");
   }
         
   @Override
   public boolean equals (final Object o)
   {
      if (o instanceof Quest)
         return id.equals (((Quest) o).id);
      return false;
   }
   
   public int compareTo (final Quest q)
   {
      return id.compareTo (q.id);
   }
   
   @Override
   public int hashCode()
   {
      return id.hashCode();
   }
   
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder (name);
      if (category != null)
      {
         sb.append (" [");
         sb.append (category);
         if (level > 0)
            sb.append (" " + level);
         sb.append ("]");
      }
      return sb.toString();
   }

   public String getLink()
   {
      StringBuilder sb = new StringBuilder();
      if (level > 0)
         sb.append ("[" + level + "] ");
      sb.append ("<a href=\"" + link + "\">" + name + "</a>");
      return sb.toString();
   }
   
   public String getLinkLocal()
   {
      if (text != null)
      {
         StringBuilder sb = new StringBuilder();
         if (level > 0)
            sb.append ("[" + level + "] ");
         sb.append ("<a href=\"#" + name + "\">" + name + "</a>");
         return sb.toString();
      }
      return getLink(); 
   }
   
   public String export()
   {
      StringBuilder sb = new StringBuilder();
      if (category != null)
      {
         sb.append ("<a name=\"" + name + "\"></a>\n");
         sb.append ("<p><b><a href=\"" + link + "\">" + name + "</a></b> ");
         sb.append (" (" + category + ", min level " + level + ")<br>\n");
         
         sb.append ("  <ul>\n");
         
         if (bestower != null)
            sb.append ("   <li><b>Bestower: </b>" + bestower + "<br>\n");
         if (desc != null)
            sb.append ("   <li><b>Description: </b>" + desc + "<br>\n");
         if (text != null)
            sb.append ("   <li><b>Text: </b><small><i>" + text + "</i></small><br>\n");

         if (!rewards.isEmpty())
         {
            sb.append ("   <li><b>Rewards:</b><br>\n");
            sb.append ("   <ul>\n");
            for (Reward reward : rewards)
            {
               sb.append ("     <li><a href=\"" + reward.getLink() + "\">" + 
                          reward.getName() + "</a>");
               if (reward.isOption())
                  sb.append (" (selectable option)");
               sb.append ("\n");
            }
            sb.append ("   </ul>\n");
         }
         
         if (!prereqs.isEmpty())
         {
            sb.append ("   <li><b>Prerequisites:</b>");
            if (oneOf && prereqs.size() > 1)
               sb.append (" (at least one of the following)");
            sb.append ("<br>\n");
            sb.append ("   <ul>\n");
            for (Quest prereq : prereqs)
               sb.append ("     <li><a href=\"" + prereq.link + "\">" + prereq.name + "</a>");
            sb.append ("   </ul>\n");
         }
         
         if (!follows.isEmpty())
         {
            sb.append ("   <li><b>Next Quest:</b>");
            sb.append ("<br>\n");
            sb.append ("   <ul>\n");
            for (Quest next : follows)
               sb.append ("     <li><a href=\"" + next.link + "\">" + next.name + "</a>");
            sb.append ("   </ul>\n");
         }
         
         sb.append ("  </ul>\n");
      }
      
      return sb.toString();
   }

   public static void main (final String[] args)
   {
      Quest first  = new Quest ("First");
      Quest middle = new Quest ("Middle");
      Quest last   = new Quest ("Last");
      last.addPrereq (middle, false);
      middle.addPrereq (first, false);
      
      System.out.println (last);
   }
}
