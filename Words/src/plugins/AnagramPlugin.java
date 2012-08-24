package plugins;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.swing.JButton;

import words.Dashboard;
import words.Dashboard.States;

public class AnagramPlugin extends Plugin
{
   public AnagramPlugin(final Dashboard app)
   {
      super(app);
   }
   
   public JButton getButton()
   {
      return addButton("Anagram", "Show all anagrams of the given pattern", 
                       States.WORD.name());
   }
   
   @Override
   protected void findMatches()
   {
      String letters = app.getLetters().toLowerCase();
      String regex = "(?=^.{" + letters.length() + "}$)[" + letters + "]+";
      app.regexItem.setValue(regex);
      
      // get a list of possible matches
      filter(regex, Pattern.CASE_INSENSITIVE);
      app.getProgress().setString("Anagraming: " + regex);

      // remove any that aren't true anagrams of the original letters
      int count = 0, total = app.candidates.size();
      Iterator<String> iter = app.candidates.iterator();
      while (iter.hasNext())
      {
         app.getProgress().setValue((int) Math.round(100.0 * ++count / total));
         StringBuilder copy = new StringBuilder(iter.next());
         for (int i = 0; i < regex.length(); i++)
         {
            int pos = copy.indexOf(regex.substring(i, i + 1));
            if (pos >= 0)
               copy.replace(pos, pos + 1, "");
         }
         if (copy.length() > 0)
            iter.remove();
      }
   }
}
