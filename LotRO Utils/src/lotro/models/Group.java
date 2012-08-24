package lotro.models;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Group
{
   private Collection<Deed> deeds;
   private SortedSet<CharacterWithDeeds> members = new TreeSet<CharacterWithDeeds>();
   private SortedSet<Deed> sharedDeeds = new TreeSet<Deed>();
   private Set<String> regions = new HashSet<String>();

   private int score;

   public Group (final Collection<Deed> deeds)
   {
      this.deeds = deeds;
   }
   
   public void addCharacter (final CharacterWithDeeds c)
   {
      members.add (c);
   }
   
   public int size()
   {
      return members.size();
   }
   
   public SortedSet<? extends Character> getMembers()
   {
      return members;
   }
   
   public Collection<Deed> getDeeds()
   {
      return deeds;
   }
   
   public boolean isShared (final Deed deed)
   {
      return sharedDeeds.contains (deed);
   }
   
   @Override
   public String toString()
   {
      StringBuilder memBuf = new StringBuilder();
      for (CharacterWithDeeds member : members)
      {
         if (memBuf.length() > 0)
            memBuf.append (", ");
         memBuf.append (member);
      }
      
      StringBuilder deedBuf = new StringBuilder();
      StringBuilder rgnBuf = new StringBuilder();
      String region = null;
      for (Deed deed : sharedDeeds) // one line per region
      {
         if (!deed.getRegion().equals (region))
         {
            if (rgnBuf.length() > 0)
            {
               deedBuf.append (rgnBuf + "\n");
               rgnBuf.setLength (0);
            }
            region = deed.getRegion();
            deedBuf.append ("   > " + region + ": ");
         }
         if (rgnBuf.length() > 0)
            rgnBuf.append (", ");
         rgnBuf.append (deed.getShortName());
         rgnBuf.append (" (" + needCount (deed) + ")");
      }
      deedBuf.append (rgnBuf + "\n");
      
      return memBuf.toString() + "\n" + deedBuf.toString(); 
   }
   
   private int needCount (final Deed deed)
   {
      int count = 0;
      for (CharacterWithDeeds member : members)
         if (member.needs (deed))
            count++;
      return count;
   }
   
   public int score()
   {
      score = 0;
      sharedDeeds.clear();
      
      scoreSharedDeeds();
      if (!sharedDeeds.isEmpty())
      {
         scoreRacialDeeds();
         scoreClasses();
         scoreUnsharedDeeds();
         scoreDifficulty();
         scoreRegions();
      }
      
      return score;
   }

   // add points for each shared deed 
   private void scoreSharedDeeds()
   {
      Set<Deed> allDeeds = getAllDeeds();
      int maxLevel = getMaxLevel();
      
      int count;
      for (Deed deed : allDeeds)
      {
         count = 0;
         if (maxLevel >= deed.getLevel())
         {
            for (CharacterWithDeeds member : members)
               if (member.needs (deed))
                  count++;
            if (count >= 2)
            {
               sharedDeeds.add (deed);
               score += Math.pow (count, 2);
            }
         }
      }
   }
   
   // add points for racial deeds
   private void scoreRacialDeeds()
   {
      for (Deed deed : sharedDeeds)
         for (CharacterWithDeeds member : members)
            if (member.getRace().needs (deed))
               score++;
   }

   // add/subtract points based on which classes are in the group
   private void scoreClasses()
   {
      // consider healing
      int bonus = getClassBonus (Klass.Minstrel, 8);
      bonus += getClassBonus (Klass.RuneKeeper, 6);
      bonus += getClassBonus (Klass.Captain, 4);
      bonus += getClassBonus (Klass.Burglar, 2);
      bonus += getClassBonus (Klass.LoreMaster, 2);
      score += Math.max (10, bonus);
      
      // consider DPS
      bonus = getClassBonus (Klass.Hunter, 6);
      bonus += getClassBonus (Klass.Champion, 4);
      score += Math.max (10, bonus);      
      
      // consider tanking
      bonus = getClassBonus (Klass.Guardian, 8);
      bonus += getClassBonus (Klass.Captain, 4);
      bonus += getClassBonus (Klass.Champion, 4);
      bonus += getClassBonus (Klass.Warden, 2);
      score += Math.max (8, bonus);
      
      // consider crowd-control 
      bonus = getClassBonus (Klass.LoreMaster, 6);
      bonus += getClassBonus (Klass.Burglar, 3);
      bonus += getClassBonus (Klass.Hunter, 3);
      score += Math.max (6, bonus);
   }
   
   private int getClassBonus (final Klass klass, final int bonus)
   {
      int classScore = 0;
      for (CharacterWithDeeds member : members)
         if (member.getKlass() == klass)
            classScore += bonus;
      return classScore;
   }

   // subtract points for each unshared deed
   private void scoreUnsharedDeeds()
   {
      for (Deed deed : sharedDeeds)
         for (CharacterWithDeeds member : members)
            if (!member.needs (deed))
               score--;
   }

   // subtract points for deeds that are too easy or too hard
   private void scoreDifficulty()
   {
      for (Deed deed : sharedDeeds)
         for (CharacterWithDeeds member : members)
         {
            if (deed.getLevel() > member.getLevel() + 5) // too hard
               score -= 2;
            else if (member.getLevel() > deed.getLevel() + 8) // too easy
               score--;
         }
   }

   // subtract points for too many regions
   private void scoreRegions()
   {
      regions.clear();
      for (Deed deed : sharedDeeds)
         regions.add (deed.getRegion());
      score -= regions.size();
   }

   // get a list of all deeds (any member has)
   private Set<Deed> getAllDeeds()
   {
      Set<Deed> allDeeds = new HashSet<Deed>();
      for (CharacterWithDeeds member : members)
         for (Deed deed : deeds)
            if (member.needs (deed))
               allDeeds.add (deed);
      return allDeeds;
   }
   
   // determine the maximum deed level for this group
   private int getMaxLevel()
   {
      float totalLevel = 0;
      for (CharacterWithDeeds member : members)
         totalLevel += member.getLevel();
      float avgLevel = totalLevel / members.size();
      return Math.round (avgLevel) + members.size() + 3;
   }
}
