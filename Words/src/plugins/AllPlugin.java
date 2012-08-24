package plugins;

import java.util.Iterator;
import java.util.regex.Pattern;

import javax.swing.JButton;

import words.Dashboard;
import words.Dashboard.States;

public class AllPlugin extends Plugin
{
   public AllPlugin(final Dashboard app)
   {
      super(app);
   }
   
   public JButton getButton()
   {
      return addButton("All", "Show all possible words using the given letters", 
                       States.WORD.name());
   }
   
   @Override
   protected void findMatches()
   {
      String letters = app.getLetters().toLowerCase();
      String regex = "[" + letters + "]+";
      app.regexItem.setValue(regex);
      
      // get a list of possible matches
      filter(regex, Pattern.CASE_INSENSITIVE);
      app.getProgress().setString("Anagraming: " + regex);

      // remove any that use letters more than once
      Iterator<String> iter = app.candidates.iterator();
      while (iter.hasNext())
      {
         String word = new String(iter.next()).toLowerCase();
         for (Character ch : letters.toCharArray())
            word = word.replaceFirst (ch.toString(), "");
         if (!word.isEmpty())
            iter.remove();
      }
   }
}
