package oberon;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import web.ReadURL;

public final class SourceWeb
{
   private SourceWeb() { }
   
   public static void expandUsingWebScraper (final AcronymExtractor app,
                                             final Acronyms acronyms)
   {
      app.getProgress().setString ("Searching for missing acronyms on the web...");
      app.getProgress().setIndeterminate (true);
      app.updateState (Menus.States.extracting.toString(), true);
      
      Thread thread = new Thread (new Runnable()
      {
         public void run()
         {
            Collection<Acronym> targets = acronyms.getSelected();
            boolean selected = !targets.isEmpty();
            if (!selected)
               targets = acronyms.getUndefined();
            int count = 0, total = targets.size();

            for (Acronym acronym : targets)
            {
               app.getProgress().setValue ((++count) * 100 / total); // percent
               app.getProgress().setString ("Searching web for: " + acronym);

               expandUsingIIUSA (acronym);
               expandUsingAbbrevationsDotCom (acronym, "USGOV");
               expandUsingAbbrevationsDotCom (acronym, "MILITARY");
               if (selected || !acronym.isDefined())
                  expandUsingAbbrevationsDotCom (acronym, null);
               if (acronym.isDefined())
                  acronyms.updateModel (acronym);
            }
            
            app.finishedExtracting ("Web search complete");
         }
      });
      thread.start();
   }

   private static final Pattern ABBREV_COM =
      Pattern.compile ("<td class=\"?dsc\"? align=\"?left\"?>([^<]+)</td>");
   
   private static void expandUsingAbbrevationsDotCom (final Acronym acronym, 
                                                      final String category)
   {
      String url = "http://www.abbreviations.com/bs.aspx?st=" +
      acronym.getAbbrev() + "&SE=1&o=p&p=1";
      if (category != null)
         url = url + "&filter=" + category;

      StringBuilder page = ReadURL.capture (url);
      if (page != null)
      {
         final Matcher m = ABBREV_COM.matcher (page);
         Source source = category != null ? Source.WebGovt : Source.WebOther;
         while (m.find() && acronym.getDefinitions().size() < 10) 
            acronym.addValue (m.group (1), source);
      }
   }

   /*
   <span style='font-size:8.0pt;font-family:
   Arial'><b>DAAS</b></span><span style='font-size:8.0pt;font-family:Arial'> -
   defense automatic addressing system</span></p>
   */
   private static void expandUsingIIUSA (final Acronym acronym)
   {
      String first = (acronym.getAbbrev().charAt (0) + "").toUpperCase();
      String url = "http://iiusatech.com/Glossary/Glossary" + first + ".html";

      StringBuilder page = ReadURL.capture (url);
      if (page != null)
      {
         Pattern p = Pattern.compile ("<b>" + acronym.getAbbrev() + "</b></span>" +
                                      "<span[^>]+>\\s-\\s([^<]+)</span>",
                                      Pattern.MULTILINE | Pattern.DOTALL);
         
         final Matcher m = p.matcher (page);
         while (m.find() && acronym.getDefinitions().size() < 10)
         {
            // truncate and standardize the definition
            String s = m.group (1);
            if (s.length() > 50)
               s = s.substring (0, 50) + "...";
            s = s.replaceAll ("\\s+", " ").trim();
            
            // capitalize the first letter in each word
            StringBuilder sb = new StringBuilder (s);
            for (int i = 0; i < sb.length(); i++)
               if (i == 0 || (sb.charAt (i - 1) + "").equals (" "))
                  sb.setCharAt (i, (sb.charAt (i) + "").toUpperCase().charAt (0));
            
            acronym.addValue (sb.toString(), Source.WebGovt);
         }
      }
   }
   
   public static void main (final String[] args)
   {
      Acronym acronym = new Acronym ("DC");
      
      SourceWeb.expandUsingIIUSA (acronym);
      
      SourceWeb.expandUsingAbbrevationsDotCom (acronym, "USGOV");
      SourceWeb.expandUsingAbbrevationsDotCom (acronym, "MILITARY");
      SourceWeb.expandUsingAbbrevationsDotCom (acronym, null);
      
      for (Definition def : acronym.getDefinitions().getDefinitions())
         System.out.println (acronym + " = [" + def + "] from " + def.getSource());
   }
}
