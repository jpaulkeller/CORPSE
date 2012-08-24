package oberon;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import str.StringUtils;
import file.TextExtractor;

public final class SourceDocument
{
   // Allow numbers, some symbols, and acronyms like eTEST
   private static final Pattern ACRONYM = 
      Pattern.compile ("(?:\\b|\\W)(" +
                       "(?:[A-Za-z]+[-/&A-Za-z0-9]{0,6}[A-Z0-9]+)|" +
                       "(?:[A-Z]{2,})" +
                       ")(?:\\b|\\W)");
   
   // valid acronyms must contain at least two consecutive upper-case letters
   private static final Pattern UP2C = Pattern.compile ("[A-Z]{2}"); 
   
   // valid definitions must contain at least two upper-case letters
   private static final Pattern UP2 = Pattern.compile ("[A-Z].*[A-Z]");
   
   private static final String WORD_SEPR = 
      "(?:\\s*?[-,/\\s&]\\s*(?:[a-z]{1,3}\\s+){0,3})?";

   private AcronymExtractor app;
   private TextExtractor ext;
   private String docText;
   
   public SourceDocument (final AcronymExtractor app)
   {
      this.app = app;
      
      ext = new TextExtractor();
      ext.setState (app.getStateModel(), Menus.States.hasDocument.toString());
      ext.setOptions (app.getOptions());
      app.disableWhen (ext.getComponent(), Menus.States.extracting.toString());
   }
   
   public JPanel getPanel()
   {
      return ext.getPanel();
   }

   public File getFile()
   {
      return ext.getFile();
   }
   
   public String getText()
   {
      docText = ext.getText();
      return docText;
   }
   
   public void extractAcronyms (final Acronyms acronyms)
   {
      // find all possible acronyms
      Comparator<String> comp = StringUtils.getCaseInsensitiveComparator();
      SortedSet<String> candidates = new TreeSet<String> (comp);
      Matcher m = ACRONYM.matcher (docText);
      while (m.find())
         if (UP2C.matcher (m.group (1)).find()) // must have 2 upper-case in a row
            candidates.add (m.group (1));

      // create the acronyms and update the model
      for (String abbrev : candidates)
         acronyms.add (new Acronym (abbrev));
      app.updateState (Menus.States.hasData.toString(), !acronyms.isEmpty());
   }

   // Searches the document for an explicit reference to the acronym.  Returns
   // the number of (unique) definitions found. 
   
   public boolean findExplicit (final Acronym acronym)
   {
      // if abbrev = ABC, look for "A... [Bb]... and C... (ABC)" text
      StringBuilder regex = getRegex (acronym, "(?:\\b|\\W)(", false);
      regex.append (")\\s+\\(" + acronym.getAbbrev() + "\\)");
         
      Pattern p = Pattern.compile (regex.toString());
      Matcher m = p.matcher (docText);
      while (m.find())
         if (addDefinition (acronym, m.group (1)))
            return true;
      
      // if abbrev = ABC, look for "ABC - A... [Bb]... and C..."
      regex.setLength (0);
      regex = getRegex (acronym, "(?:\\b|\\W)" + acronym.getAbbrev() + 
                        "[^A-Z]{1,3}(", false);
      regex.append (")");
      
      p = Pattern.compile (regex.toString());
      m = p.matcher (docText);
      while (m.find())
         if (addDefinition (acronym, m.group (1)))
            return true;

      return false;
   }

   private boolean addDefinition (final Acronym acronym, final String defText)
   {
      if (!defText.contains (acronym.getAbbrev()))
      {
         String cleanDef = defText.replaceAll ("\\s+", " ");
         acronym.addValue (cleanDef, Source.Explicit);
         return true;
      }
      return false;
   }

   public void expandUsingDocument (final Acronyms acronyms)
   {
      app.getProgress().setString ("Searching the document for missing acronyms...");
      app.updateState (Menus.States.extracting.toString(), true);
      
      Thread thread = new Thread (new Runnable()
      {
         public void run()
         {
            Collection<Acronym> targets = acronyms.getSelected();
            if (targets.isEmpty())
               targets = acronyms.values();
            int count = 0, total = targets.size();

            for (Acronym acronym : targets)
            {
               app.getProgress().setValue ((++count) * 100 / total); // percent
               app.getProgress().setString ("Searching document for: " + acronym);
               
               // if abbrev = ABC, look for "A... B... and C..." text
               StringBuilder regex = getRegex (acronym, "(?:\\b|\\W)(", true);
               regex.append (")(?:\\b|\\W)");
               Pattern p = Pattern.compile (regex.toString());
               Matcher m = p.matcher (docText);
               while (m.find())
                  if (UP2.matcher (m.group (1)).find()) // must have 2 upper-case
                  {
                     String definition = m.group (1).replaceAll ("\\s+", " ");
                     if (!definition.contains (acronym.getAbbrev()))
                        acronym.addValue (definition, Source.Document);
                  }
               
               // selectBest (acronym);
               
               acronyms.repaint();
            }
            
            app.finishedExtracting ("Document search complete");
         }
      });
      thread.start();
   }
   
   private StringBuilder getRegex (final Acronym acronym, final String open,
                                   final boolean requireSeparator)
   {
      StringBuilder regex = new StringBuilder();
      char[] chars = acronym.getAbbrev().toCharArray();
      
      int i = 0;
      for (Character ch : chars)
      {
         if (Character.isLetter (ch))
         {
            regex.append (i == 0 ? open : WORD_SEPR);
            /*
            if (i == 0)
               regex.append (Character.toUpperCase (ch)); // only allow capitals 
            */
            if (ch== 'X' || ch == 'x')
               regex.append ("(?i)E?" + ch + "(?-i)"); // allow Ex to match X
            else
               regex.append ("(?i)" + ch + "(?-i)"); // allow either case
            regex.append ("[-'A-Za-z0-9]*,?");
         }
         i++;
      }
      return regex;
   }

   public String findContext (final Acronym acronym)
   {
      String abbrev = acronym.getAbbrev();
      // find the first sentence which contains the acronym
      Pattern context = Pattern.compile 
      ("\\.\\s*([^.]{0,50}(?:\\b|\\W)" + abbrev + "(?:\\b|\\W)[^.]{0,50}\\.)\\s*",
       Pattern.DOTALL | Pattern.MULTILINE);
      Matcher m = context.matcher (docText);
      if (m.find())
         return m.group(1).trim();
      
      // find the first line which contains the acronym
      context = Pattern.compile ("^.*?(.{1,50}(?:\\b|\\W)" + abbrev +
                                 "(?:\\b|\\W).{1,50}).*?$", Pattern.MULTILINE);
      m = context.matcher (docText); 
      return m.find() ? m.group (1).trim() : "";
   }
}
