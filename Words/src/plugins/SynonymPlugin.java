package plugins;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;

import web.ReadURL;
import words.Dashboard;
import words.Dashboard.States;

public class SynonymPlugin extends Plugin
{
   private final static Pattern SYNONYM = 
      Pattern.compile("http://thesaurus.com/browse/[a-z]+\">(.+)\\n");
   private final static Pattern WORD = Pattern.compile("([-a-z]+)");
   
   public SynonymPlugin(final Dashboard app)
   {
      super(app);
   }

   public JButton getButton()
   {
      return addButton("Synonyms", "Look up synonyms on-line.", States.WORD.name());
   }

   @Override
   protected void findMatches()
   {
      app.candidates.clear(); // this plug-in doesn't use candidate words
      
      String word = app.getLetters();
      String url = "http://thesaurus.reference.com/browse/" + word;
      StringBuilder html = ReadURL.capture(url);

      Pattern p = Pattern.compile
        (">Main Entry:<.*?>" + word + "</.*?>Synonyms:<.*?<span>(.*?)</span>",
         Pattern.DOTALL | Pattern.MULTILINE);
      Matcher span = p.matcher(html);
      if (span.find())
      {
         Matcher variations = SYNONYM.matcher(span.group(1));
         while (variations.find())
         {
            Matcher synonym = WORD.matcher(variations.group(1));
            while (synonym.find())
               if (!"a".equals(synonym.group(1))) // ignore </a>
                  app.candidates.add(synonym.group(1));
         }
      }
   }
}
