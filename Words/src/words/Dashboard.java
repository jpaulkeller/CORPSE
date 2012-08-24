package words;

import gui.ComponentTools;
import gui.form.FileItem;
import gui.form.TextItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;

import plugins.AllPlugin;
import plugins.AnagramPlugin;
import plugins.MatchPlugin;
import plugins.OverlapPlugin;
import plugins.ScrabblePlugin;
import plugins.SynonymPlugin;
import utils.Application;

public class Dashboard extends Application implements ActionListener
{
   public enum States { WORD, REGEX, RUNNING, FILTER };
   
   public static Words words = new Words(); // all words in the dictionary
   
   private FileItem dictItem;
   public TextItem lettersItem;
   public TextItem regexItem;
   private JPanel buttons;
   
   public List<String> candidates = new ArrayList<String>();
   public Map<String, String> extras = new TreeMap<String, String>();
   public Model model;

   public Dashboard(final File dictionary)
   {
      super("Dictionary Dashboard", null, "9 Aug 2011", null);
      
      words.load(dictionary);
      
      this.model = new Model();

      dictItem = new FileItem("Dictionary", null, 25);
      dictItem.setNullValidity(false);
      dictItem.setInitialValue(dictionary);
      dictItem.addValueChangeListener (new DictListener());
      
      lettersItem = new TextItem("Letters / Word", 25);
      lettersItem.getComponent().setFont(new Font("Courier", Font.PLAIN, 16));
      lettersItem.setValidator(new RegexValidator(".+"));
      lettersItem.addStatusListener(new TextListener(States.WORD));

      regexItem = new TextItem("Regex Pattern", 25);
      regexItem.getComponent().setFont(new Font("Courier", Font.PLAIN, 16));
      Validator validator = new PatternValidator();
      regexItem.setValidator(validator);
      regexItem.addStatusListener(new TextListener(States.REGEX));

      buttons = new JPanel();
      buttons.add(new MatchPlugin(this).getButton());
      buttons.add(new AllPlugin(this).getButton());
      buttons.add(new AnagramPlugin(this).getButton());
      buttons.add(new ScrabblePlugin(this).getButton());
      buttons.add(new OverlapPlugin(this).getButton());
      buttons.add(new SynonymPlugin(this).getButton());
      
      JButton button = new JButton("Reset");
      button.setToolTipText("Clear the pattern and matches");
      enableWhen(button, States.FILTER.name());
      disableWhen(button, States.RUNNING.name());
      button.addActionListener(this);
      buttons.add(button);

      JPanel output = new JPanel(new BorderLayout());
      output.setPreferredSize(new Dimension(500, 400));
      output.add(new JScrollPane(getView()), BorderLayout.CENTER);

      JPanel input = new JPanel(new BorderLayout());
      input.add (lettersItem.getTitledPanel(), BorderLayout.WEST);
      input.add (regexItem.getTitledPanel(), BorderLayout.EAST);
      
      JPanel grid = new JPanel(new GridLayout(0, 1));
      grid.add(dictItem.getTitledPanel());
      grid.add(input);
      
      JPanel controls = new JPanel(new BorderLayout());
      controls.add(grid, BorderLayout.NORTH);
      controls.add(buttons, BorderLayout.CENTER);
        
      add(controls, BorderLayout.NORTH);
      add(ComponentTools.getTitledPanel(output, "Matches"), BorderLayout.CENTER);
   }

   public String getLetters() 
   {
      return (String) lettersItem.getValue();
   }
   
   public String getRegex() 
   {
      return regexItem.isValid() ? (String) regexItem.getValue() : null;
   }
   
   private void showResults()
   {
      lettersItem.apply();
      regexItem.apply();
   }
   
   public void actionPerformed(final ActionEvent e) // reset
   {
      updateState (States.FILTER.name(), false);
      
      if (!lettersItem.hasChanged())
         lettersItem.setInitialValue("");
      if (!regexItem.hasChanged())
         regexItem.setInitialValue("");

      candidates.clear();
      while (model.getRowCount() > 0)
         model.removeRow(model.getRowCount() - 1);
   }

   private JXTable getView()
   {
      JXTable tbl = new JXTable(model);
      tbl.setEditable(false);
      tbl.setColumnControlVisible(true);
      tbl.setHorizontalScrollEnabled(true);
      tbl.packAll();
      return tbl;
   }

   class PatternValidator extends ValidationAdapter
   {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isValid(Object value)
      {
         if (value != null && !value.toString().isEmpty())
         {
            try
            {
               Pattern.compile(value.toString());
               return true;
            }
            catch(PatternSyntaxException x) { }
         }
         return false;
      }
   }
   
   class DictListener implements ValueChangeListener
   {
      @Override
      public void valueChanged (ValueChangeEvent e)
      {
         if (e.getValue() instanceof File)
         {
            File f = (File) e.getValue();
            if (f.exists())
            {
               words.clear();
               words.load(f);
               getProgress().setString ("Loaded " + words.size() + " words");
            }
         }
      }
   }
   
   class TextListener implements StatusListener
   {
      private States state;
      
      public TextListener(final States state)
      {
         this.state = state;
      }
      
      @Override
      public void stateChanged(StatusEvent event)
      {
         updateState(state.name(), event.getStatus());
      }
   }

   public static void main(String[] args)
   {
      ComponentTools.setLookAndFeel();
      Dashboard app = new Dashboard(new File("../dictionary.en"));
      app.open();
   }
}
