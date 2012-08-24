package lotro.deed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.swing.SwingUtilities;

import lotro.models.Assignment;
import lotro.models.AssignmentModel;
import lotro.models.Character;
import lotro.models.CharacterWithDeeds;
import lotro.models.Deed;
import lotro.models.Group;

public class Score
{
   private Collection<Group> grouping = new ArrayList<Group> (24);
   
   private DeedOrganizer app;
   
   private int score;
   private int bestScore = Integer.MIN_VALUE;
   private double bestAvgPerChar = Integer.MIN_VALUE;
   private long trials;
   private long possible;
   private int percent;
   private int badGroupSize; // group size is either too small or too large 
   private int badRelations; // group contains enemies, or splits friends
   private int noShared;     // group has no shared deeds
   private int lowPotential; // group score is lower than threshold
   
   public Score (final DeedOrganizer app)
   {
      this.app = app;
   }

   public int getBestScore()
   {
      return bestScore;
   }

   public long getTrials()
   {
      return trials;
   }

   public void incTrials()
   {
      trials++;
   }
   
   public int getPercent()
   {
      return percent;
   }

   public void setPercent (final int percent)
   {
      this.percent = percent;
   }

   public long getPossible()
   {
      return possible;
   }

   public void setPossible (final long possible)
   {
      this.possible = possible;
   }

   void clear()
   {
      bestScore = Integer.MIN_VALUE;
      bestAvgPerChar = Integer.MIN_VALUE;
      trials = 0;
      percent = 0;

      badGroupSize = 0;
      badRelations = 0;
      noShared = 0;
      lowPotential = 0;
   }
   
   void scorePartition (final Partition<CharacterWithDeeds> partition,
                        final AssignmentModel model,
                        final int minGroup, final int maxGroup)
   {
      if (partition.sizeOfSmallest() < minGroup || 
          partition.sizeOfLargest() > maxGroup)
         badGroupSize++;
      else
         scorePartition (partition, model);
   }

   private void scorePartition (final Partition<CharacterWithDeeds> partition, 
                                final AssignmentModel model)
   {
      populateGrouping (partition, model);

      score = 0;

      float charCount = 0;
      double avgThreshold = bestAvgPerChar * 0.5; // TBD make Option
      double avgPerChar = 0;
      
      for (Group group : grouping) // score each grouping
      {
         if (!model.isValid (group))
         {
            badRelations++;
            return; // abort
         }

         int groupScore = group.score();
         if (groupScore <= 0) // abort if group has no shared quests         
         {
            noShared++;
            return; // abort
         }
         
         score += groupScore;
         charCount += group.size();
         avgPerChar = score / charCount;
         if (avgPerChar < avgThreshold) // abort if potential is too low
         {
            lowPotential++;
            return; // abort
         }
      }
      
      updateBest (grouping, score, avgPerChar);
   }

   private void populateGrouping (final Partition<CharacterWithDeeds> partition, 
                                  final AssignmentModel model)
   {
      grouping.clear();
      for (Set<CharacterWithDeeds> charSet : partition)
      {
         Group group = new Group (model.getDeeds());
         for (CharacterWithDeeds ch : charSet)
            group.addCharacter (ch);
         grouping.add (group);
      }
   }
   
   void updateBest (final Collection<Group> groups, 
                    final int newScore, final double avgPerChar)
   {
      if (newScore > bestScore)
      {
         bestScore = newScore;
         bestAvgPerChar = avgPerChar;
         Collection<Group> copy = new ArrayList<Group> (groups);
         showGrouping (copy);
         updateAssignments (copy);
         
         SwingUtilities.invokeLater (new Runnable() {
            public void run()
            {
               app.getTable().repaint();
            }
         });
      }
   }

   private void updateAssignments (final Collection<Group> groups)
   {
      for (Group group : groups)
         for (Deed deed : group.getDeeds()) // all deeds
            for (Character member : group.getMembers())
            {
               Assignment assignment = ((CharacterWithDeeds) member).getAssignment (deed);
               assignment.setOrganized (true);
               assignment.setAssigned (group.isShared (deed));
            }
   }
   
   void showGrouping (final Collection<Group> groups)
   {
      SwingUtilities.invokeLater (new Runnable() {
         public void run()
         {
            app.getResults().setText ("");
            int i = 1;
            for (Group group : groups)
            {
               if (i > 1)
                  app.getResults().append ("\n");
               app.getResults().append ("Group " + (i++) + ") " + group);
            }
            app.getResults().setCaretPosition (0);
         }
      });

      System.out.println (this);
   }
   
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append ("Score: " + bestScore);
      sb.append (" Trials: " + trials);
      sb.append (" " + percent + "%");
      sb.append (" BadGroupSize:" + badGroupSize);
      sb.append (" BadRelations:" + badRelations);
      sb.append (" NoSharedDeeds:" + noShared);
      sb.append (" LowPotential:" + lowPotential);
      sb.append (" Ignored:" + (badGroupSize + badRelations + 
                                noShared + lowPotential));
      return sb.toString();
   }
}
