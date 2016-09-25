package geoquest;

import java.util.ArrayList;
import java.util.List;

public class Puzzles
{
   static final List<String> PUZZLES = new ArrayList<>();
   static
   {
      populate();
   }

   private static void populate()
   {
      PUZZLES.add("5-letter\nWords");
      PUZZLES.add("Anatomy");
      PUZZLES.add("Animals");
      PUZZLES.add("Art and\nArtists");
      PUZZLES.add("Black or\nWhite");
      PUZZLES.add("Books &\nAuthors");
      PUZZLES.add("Couples");
      PUZZLES.add("Dangerous\nThings");
      PUZZLES.add("Events"); // or maybe current events?
      PUZZLES.add("Famous\nand\nLiving");
      PUZZLES.add("Far Away"); // anything far away from where you currently are
      PUZZLES.add("Fictional\nCharacters");
      PUZZLES.add("First\n& Last"); // puzzle letters must be first and last in solution word ("c/h" could be solved by\n"couch")
      PUZZLES.add("Flowers");
      PUZZLES.add("Foreign\nWords");
      PUZZLES.add("Games");
      PUZZLES.add("Herbs &\nSpices");
      PUZZLES.add("Historical\nFigures");
      PUZZLES.add("Hot or\nCold");
      PUZZLES.add("In a\nLaboratory"); 
      PUZZLES.add("In This\nRoom"); // anything in the current room
      PUZZLES.add("Inside Word"); // puzzle letters must be in the word, but not first or last ("c/h" could be solved by\n"shock")
      PUZZLES.add("Movies,\nPlays,\nor TV");
      PUZZLES.add("Noisy\nThings");
      PUZZLES.add("On a\nfarm");
      PUZZLES.add("Opposites"); // each word must have one of the puzzle letters ("c/h" could be solved by "thick/thin" or "cold/hot")
      PUZZLES.add("Outer\nSpace");
      PUZZLES.add("Places");
      PUZZLES.add("Quotations");
      PUZZLES.add("Round\nThings");
      PUZZLES.add("Scary\nThings");
      PUZZLES.add("Science\nFiction");
      PUZZLES.add("Sea\nCreatures");
      PUZZLES.add("Side\nby\nSide"); // puzzle letters must be adjacent ("c/h" could be solved by\n"child")
      PUZZLES.add("Small\nThings");
      PUZZLES.add("Soft\nThings");
      PUZZLES.add("Someone\nYou\nKnow"); 
      PUZZLES.add("Sounds"); 
      PUZZLES.add("Sports &\nAthletes");  
      PUZZLES.add("Things in\na Garden");
      PUZZLES.add("Things in\na Kitchen");
      PUZZLES.add("Things in\nan Office");
      PUZZLES.add("Things\nThat\nFly");
      PUZZLES.add("Things\nYou Eat");
      PUZZLES.add("Things\nYou Wear");
   }

   public static void main(final String[] args)
   {
      // HtmlGenerator htmlGen = new HtmlGenerator(80, 8, 0, 0, 0, 0);
      // htmlGen.printPuzzles(PUZZLES);
      
      ImageGenerator imgGen = new ImageGenerator(ImageStats.getShardStats(36), false);
      int i = 1;
      for (String puzzle : PUZZLES)
         imgGen.publish(puzzle, i++);
      System.out.println();
   }
}
