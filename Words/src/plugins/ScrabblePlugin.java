package plugins;
import java.util.Iterator;

import javax.swing.JButton;

import words.Dashboard;
import words.Dashboard.States;

public class ScrabblePlugin extends Plugin
{
   public ScrabblePlugin(final Dashboard app)
   {
      super(app);
   }
   
   public JButton getButton()
   {
      return addButton ("Scrabble", "Show possible words using only the given letters plus 1",
                        States.WORD.name());
   }
   
   @Override
   protected void findMatches()
   {
      String letters = app.getLetters().toLowerCase();
      String regex = "[" + letters + "]*[a-z][" + letters + "]*";
      app.regexItem.setValue (regex);
      filter (regex, 0);
      
      // remove any that use tiles more than once
      Iterator<String> iter = app.candidates.iterator();
      while (iter.hasNext())
      {
         String word = iter.next();
         if (word.length() == 1)
            iter.remove();
         else
         {
            String copy = new String(word);
            for (Character ch : letters.toCharArray())
               copy = copy.replaceFirst (ch.toString(), "");
            if (copy.length() > 1)
               iter.remove();
            else
               app.extras.put(word, copy); // keep the extra letter needed
         }
      }
   }
}
