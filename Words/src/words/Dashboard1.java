package words;
import gui.ComponentTools;
import gui.form.TextItem;
import gui.form.valid.RegexValidator;
import gui.form.valid.StatusEvent;
import gui.form.valid.StatusListener;
import gui.form.valid.ValidationAdapter;
import gui.form.valid.Validator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import utils.Application;
import web.ReadURL;

public class Dashboard1 extends Application
{
   static Words words = new Words(); // all words in the dictionary
   
   private TextItem lettersItem;
   private TextItem includeItem;
   private TextItem excludeItem;
   private JPanel buttons;
   private MyTable model;

   List<String> candidates = new ArrayList<String>();

   public Dashboard1()
   {
      super ("Dictionary Dashboard", null, "25 May 2010", null);
      
      model = new MyTable();
      model.addColumn ("Word");
      model.addColumn ("Length");
      
      lettersItem = new TextItem ("Letters / Word", 25);
      lettersItem.getComponent().setFont (new Font ("Courier", Font.PLAIN, 16));
      lettersItem.setValidator (new RegexValidator (".+"));
      lettersItem.addStatusListener (new TextListener ("LettersValid"));

      includeItem = new TextItem ("Include", 25);
      includeItem.getComponent().setFont (new Font ("Courier", Font.PLAIN, 16));
      Validator validator = new PatternValidator();
      includeItem.setValidator (validator);
      includeItem.addStatusListener (new TextListener ("IncludeValid"));

      excludeItem = new TextItem ("Exclude", 25);
      excludeItem.getComponent().setFont (new Font ("Courier", Font.PLAIN, 16));
      excludeItem.setValidator (validator);

      ButtonListener listener = new ButtonListener();
      buttons = new JPanel();
      addButton ("Matches", listener, "Show all words matching the regular expression", "IncludeValid");
      addButton ("All", listener, "Show all words using only the given letters", "LettersValid");
      addButton ("Anagram", listener, "Show all anagrams of the given pattern", "LettersValid");
      addButton ("Scrabble", listener, "Show possible words using only the given letters plus 1", "LettersValid");
      addButton ("Overlap", listener, "Find two words that overlap (Puzzazz)", "LettersValid");
      addButton ("Synonyms", listener, "Look up synonyms on-line.", "LettersValid");
      addButton ("Reset", listener, "Clear the pattern and matches");

      JPanel output = new JPanel (new BorderLayout());
      output.setPreferredSize (new Dimension (500, 400));
      output.add (new JScrollPane (getView (model)), BorderLayout.CENTER);

      JPanel grid = new JPanel (new GridLayout (0, 1));
      grid.add (lettersItem.getTitledPanel());
      grid.add (includeItem.getTitledPanel());
      grid.add (excludeItem.getTitledPanel());
      
      JPanel controls = new JPanel (new BorderLayout());
      controls.add (grid, BorderLayout.NORTH);
      controls.add (buttons, BorderLayout.CENTER);
        
      add (controls, BorderLayout.NORTH);
      add (ComponentTools.getTitledPanel (output, "Matches"), BorderLayout.CENTER);
   }

   private JButton addButton (String label, ActionListener listener, String tip,
                              String... states)
   {
      JButton button = new JButton (label);
      button.setToolTipText (tip);
      if (states != null && states.length > 0)
      {
         button.setEnabled (false);
         enableWhen (button, states);
      }
      disableWhen (button, "Running");
      button.addActionListener (listener);
      buttons.add (button);
      return button;
   }
   
   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         updateState ("Running", true);
         getProgress().setIndeterminate (true);
         
         // candidates.clear();
         if (candidates.isEmpty())
            candidates.addAll(words);

         while (model.getRowCount() > 0)
            model.removeRow (0);
         
         Thread thread = new Thread (new Runnable()
         {
            public void run()
            {
               String command = e.getActionCommand();
               if (command.equals ("Matches"))
                  ;
               else if (command.equals ("All"))
                  findAll();
               else if (command.equals ("Anagram"))
                  anagram();
               else if (command.equals ("Scrabble"))
                  findScrabble();
               else if (command.equals ("Overlap"))
                  findOverlap();
               else if (command.equals ("Synonyms"))
                  findSynonyms();
               else if (command.equals ("Reset"))
                  reset();
               
               if (!candidates.isEmpty())
               {
                  System.out.println (candidates.size() + " matches found");
                  include ((String) includeItem.getValue(), Pattern.CASE_INSENSITIVE);
                  int count = candidates.size();
                  System.out.println (count + " words included");
                  exclude();
                  System.out.println ((count - candidates.size()) + " words excluded");
               }
               
               showResults();
               updateState ("Running", false);
            }
         });
         thread.start();
      }
      
      private void showResults()
      {
         SwingUtilities.invokeLater (new Runnable() {
            public void run()
            {
               getProgress().reset ("");
               if (!candidates.isEmpty())
                  showCandidates();

               lettersItem.apply();
               includeItem.apply();
               excludeItem.apply();
            }
         });
      }
   }
   
   private void findAll()
   {
      String letters = ((String) lettersItem.getValue()).toLowerCase();
      include ("[" + letters + "]+", Pattern.CASE_INSENSITIVE);
   }

   private void findScrabble()
   {
      String letters = ((String) lettersItem.getValue()).toLowerCase();
      include ("[" + letters + "]*[a-z][" + letters + "]*", 0);
      
      // remove any that use tiles more than once
      Iterator<String> iter = candidates.iterator();
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
         }
      }
   }
   
   private void anagram()
   {
      String letters = ((String) lettersItem.getValue()).toLowerCase();
      anagram ("(?=^.{" + letters.length() + "}$)[" + letters + "]+");
   }

   private void anagram (final String regex)
   {
      // get a list of possible matches
      include (regex, Pattern.CASE_INSENSITIVE);
      getProgress().setString ("Anagraming: " + regex);

      // remove any that aren't true anagrams of the original letters
      int count = 0, total = candidates.size();
      Iterator<String> iter = candidates.iterator();
      while (iter.hasNext())
      {
         getProgress().setValue ((int) Math.round (100.0 * ++count / total));
         StringBuilder copy = new StringBuilder (iter.next());
         for (int i = 0; i < regex.length(); i++)
         {
            int pos = copy.indexOf (regex.substring (i, i + 1));
            if (pos >= 0)
               copy.replace (pos, pos + 1, "");
         }
         if (copy.length() > 0)
            iter.remove();
      }
   }

   private void findOverlap()
   {
      List<Character> remaining = new ArrayList<Character>();
      List<String> possible = new ArrayList<String>();
      String target = (String) lettersItem.getValue();
      List<Character> letters = new ArrayList<Character>();
      for (char ch : target.toCharArray())
         letters.add (ch);
      
      include ("[" + lettersItem.getValue() + "]+", 0);
      // filter before appending words only if there's no space in the pattern       
      String includePattern = (String) includeItem.getValue(); 
      if (!includePattern.contains (" "))
         include (includePattern, 0);
      
      for (String word1 : candidates)
      {
         int targetLen = target.length() + 1 - word1.length();
         
         for (String word2 : candidates)
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
      
      candidates.clear();
      candidates.addAll (possible);
   }

   private final static Pattern SYNONYM = 
      Pattern.compile ("http://thesaurus.reference.com/browse/[a-z]+\">([a-z]+)\\*?</a>",
                       Pattern.MULTILINE);
    
   private void findSynonyms()
   {
      String word = (String) lettersItem.getValue();
      String url = "http://thesaurus.reference.com/browse/" + word;
      StringBuilder html = ReadURL.capture (url);
      
      Pattern p = Pattern.compile
         ("<b>Main Entry:</b></td>\\s+<td>" + word + "</td>" +
          ".*?<b>Synonyms:</b>.*?<span>(.*?)</span>", Pattern.DOTALL | Pattern.MULTILINE);
      Matcher m = p.matcher (html);
      if (m.find())
      {
         Matcher m2 = SYNONYM.matcher (m.group (1));
         while (m2.find())
            candidates.add (m2.group (1));
      }
   }

   private void include (final String regex, final int flags)
   {
      if (regex != null && !regex.trim().isEmpty())
      {
         getProgress().setIndeterminate (false);
         getProgress().setString ("Finding all words matching: " + regex);
         System.out.println ("Finding all words matching: " + regex);
         Pattern pattern = Pattern.compile (regex, flags);
         int count = 0, total = candidates.size();
         System.out.println ("Starting with " + total + " words");
         Iterator<String> iter = candidates.iterator();
         while (iter.hasNext())
         {
            getProgress().setValue ((int) Math.round (100.0 * ++count / total));
            if (!pattern.matcher (iter.next()).matches())
               iter.remove();
         }
      }
   }

   private void exclude()
   {
      String regex = (String) excludeItem.getValue(); 
      if (regex != null && !regex.trim().isEmpty())
      {
         getProgress().setIndeterminate (false);
         getProgress().setString ("Removing all words matching: " + regex);
         Pattern pattern = Pattern.compile (regex, Pattern.CASE_INSENSITIVE);
         int count = 0, total = candidates.size();
         Iterator<String> iter = candidates.iterator();
         while (iter.hasNext())
         {
            getProgress().setValue ((int) Math.round (100.0 * ++count / total));
            if (pattern.matcher (iter.next()).matches())
               iter.remove();
         }
      }
   }

   private void showCandidates()
   {
      getProgress().reset ("Matches found: " + candidates.size());
      
      for (String word : candidates)
      {
         Vector<Object> row = new Vector<Object>();
         row.add (word);
         row.add (word.length());
         model.addRow (row);
      }
   }

   private void reset()
   {
      if (!lettersItem.hasChanged())
         lettersItem.setInitialValue ("");
      if (!includeItem.hasChanged())
         includeItem.setInitialValue ("");
      if (!excludeItem.hasChanged())
         excludeItem.setInitialValue ("");
      candidates.clear();
      while (model.getRowCount() > 0)
         model.removeRow (0);
   }

   private JXTable getView (DefaultTableModel model)
   {
      JXTable tbl = new JXTable (model);
      tbl.setEditable (false);
      tbl.setColumnControlVisible (true);
      tbl.setHorizontalScrollEnabled (true);
      tbl.packAll();
      return tbl;
   }

   static class MyTable extends DefaultTableModel
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getColumnClass (int c)
      {
         if (getRowCount() > 0)
            return getValueAt (0, c).getClass();
         return null;
      }
   }
   
   class PatternValidator extends ValidationAdapter
   {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isValid (Object value)
      {
         if (value != null && !value.toString().isEmpty())
         {
            try
            {
               Pattern.compile (value.toString());
               return true;
            }
            catch (PatternSyntaxException x) { }
         }
         return false;
      }
   }
   
   class TextListener implements StatusListener
   {
      private String state;
      
      public TextListener (final String state)
      {
         this.state = state;
      }
      
      @Override
      public void stateChanged (StatusEvent event)
      {
         updateState (state, event.getStatus());
      }
   }

   public static void main (String[] args)
   {
      Dashboard1 app = new Dashboard1();
      Dashboard1.words.load (new File ("../dictionary.en"));
      app.open();
   }
}