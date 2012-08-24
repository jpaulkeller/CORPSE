package plugins;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import words.Dashboard;
import words.Dashboard.States;

public class OverlapPlugin extends Plugin
{
   private List<String> possible = new ArrayList<String>();
   
   public OverlapPlugin(final Dashboard app)
   {
      super(app);
   }
   
   public JButton getButton()
   {
      return addButton ("Overlap", "Find two words that overlap (Puzzazz)", 
                        States.WORD.name());
   }
   
   @Override
   protected void findMatches()
   {
      List<Character> remaining = new ArrayList<Character>();
      String target = app.getLetters();
      List<Character> letters = new ArrayList<Character>();
      for (char ch : target.toCharArray())
         letters.add (ch);
      
      filter ("[" + target + "]+", 0);
      
      for (String word1 : app.candidates)
      {
         int targetLen = target.length() + 1 - word1.length();
         
         for (String word2 : app.candidates)
            if (word2.length() == targetLen)
            {
               remaining.clear();
               remaining.addAll (letters);
               for (Character ch : word1.toCharArray())
                  remaining.remove (ch);
               
               // must be able to remove all letters except the 1 overlap               
               int missing = 0;
               for (Character ch : word2.toCharArray())
                  if (!remaining.remove (ch))
                     missing++;
               if (remaining.isEmpty() && missing == 1)
                  possible.add (word1 + " " + word2);
            }
      }
   }
}
