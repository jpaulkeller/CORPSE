package plugins;

import javax.swing.JButton;

import words.Dashboard;
import words.Dashboard.States;

public class MatchPlugin extends Plugin
{
   public MatchPlugin(final Dashboard app)
   {
      super(app);
   }

   public JButton getButton()
   {
      return addButton("Match", "Show all words matching the pattern", 
                       States.REGEX.name());
   }

   protected void findMatches()
   {
      filter (app.getRegex(), 0);
   }
}
