package plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import words.Dashboard;
import words.Dashboard.States;

public abstract class Plugin implements ActionListener
{
   protected Dashboard app;
   
   public Plugin(final Dashboard app)
   {
      this.app = app;
   }

   public abstract JButton getButton();
   protected abstract void findMatches();

   protected JButton addButton(String label, String tip, String... states)
   {
      JButton button = new JButton(label);
      button.setToolTipText(tip);
      if (states != null && states.length > 0)
      {
         button.setEnabled(false);
         app.enableWhen(button, states);
      }
      app.disableWhen(button, States.RUNNING.name());
      button.addActionListener(this);
      return button;
   }
   
   public void actionPerformed(final ActionEvent e)
   {
      process();
   }
   
   private void process()
   {
      app.updateState(States.RUNNING.name(), true);
      app.getProgress().setIndeterminate(true);
         
      if (app.candidates.isEmpty())
      {
         app.candidates.addAll(Dashboard.words);
         System.out.println ("Loading " + app.candidates.size() + " words");
      }
      
      while(app.model.getRowCount() > 0)
         app.model.removeRow(0);
         
      Thread thread = new Thread(new Runnable()
      {
         public void run()
         {
            findMatches();
            app.updateState(States.RUNNING.name(), false);
            showResults();
         }
      });
      thread.start();
   }
   
   protected void showResults()
   {
      SwingUtilities.invokeLater(new Runnable() 
      {
         public void run()
         {
            app.getProgress().reset("Matches found: " + app.candidates.size());
            if (!app.candidates.isEmpty())
               populateModel();
         }
      });
   }
   
   private void populateModel()
   {
      app.model.setColumnCount(0);
      app.model.addColumn("Word");
      if (!app.extras.isEmpty())
         app.model.addColumn ("Extra");
      app.model.addColumn("Length");

      for (String word : app.candidates)
      {
         Vector<Object> row = new Vector<Object>();
         row.add(word);
         if (!app.extras.isEmpty())
            row.add (app.extras.get(word));
         row.add(word.length());
         app.model.addRow(row);
      }
      
      app.updateState (States.FILTER.name(), !app.candidates.isEmpty());
   }
   
   protected void filter(final String regex, final int flags)
   {
      if (regex != null && !regex.trim().isEmpty())
      {
         app.getProgress().setIndeterminate(false);
         app.getProgress().setString("Finding all words matching: " + regex);
         Pattern pattern = Pattern.compile(regex, flags);
         int count = 0, total = app.candidates.size();
         Iterator<String> iter = app.candidates.iterator();
         while (iter.hasNext())
         {
            app.getProgress().setValue((int) Math.round(100.0 * ++count / total));
            if (!pattern.matcher(iter.next()).matches())
               iter.remove();
         }
         app.getProgress().setString("Found " + app.candidates.size() + " matching: " + regex);
      }
   }
}
