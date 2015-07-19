package geoquest;

import file.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Utils;

public final class CardUtils
{
   public static final String BLANK = "&nbsp";
   
   private static final SortedSet<String> REFERENCES = new TreeSet<String>();
   
   private static String[] dirs = { "Good", "Free", "Cards" };
   private static String[] suffixes = { "png", "jpg", "gif" };
   
   private CardUtils()
   {
      // prevent instantiation
   }
   
   static String findImage (final String preferredDir, final String image)
   {
      dirs[1] = preferredDir;
      for (String dir : dirs)
         for (String suffix : suffixes)
         {
            String imagePath = dir + "/" + image + "." + suffix;
            if (new File ("docs/" + imagePath).exists())
               return imagePath;
         }
      System.err.println("Missing " + preferredDir + " image: " + image);
      return null;
   }
   
   public static void printStyle (final PrintWriter pw)
   {
      pw.println ("<style type=\"text/css\">");
      pw.println ("em.cacher    { font-style: normal; font-weight: bold; " +
                  "outline-style: solid; outline-width: thin; margin: 2px; " + 
                  "background-color: " + Cacher.COLOR + "; }");
      pw.println ("em.combo     { font-style: normal; font-weight: bold; " +
                  "outline-style: solid; outline-width: thin; margin: 2px; " +
                  "background-color: " + Combo.COLOR + "; }");
      pw.println ("em.equipment { font-style: normal; font-weight: bold; " +
                  "outline-style: solid; outline-width: thin; margin: 2px; " +
                  "background-color: " + Equipment.COLOR + "; }");
      pw.println ("em.event     { font-style: normal; font-weight: bold; " +
                  "outline-style: solid; outline-width: thin; margin: 2px; " +
                  "background-color: " + Event.COLOR + "; }");
      pw.println ("em.geocache  { font-style: normal; font-weight: bold; " +
                  "outline-style: solid; outline-width: thin; margin: 2px; " +
                  "background-color: " + Geocache.COLOR + "; }");
      pw.println ("em.tb        { font-style: normal; font-weight: bold; " +
                  "outline-style: solid; outline-width: thin; margin: 2px; " +
                  "background-color: " + TravelBug.COLOR + "; }");
      
      pw.println ("em.dnf       { font-style: normal; font-weight: bold; background-color: salmon }");
      pw.println ("em.find      { font-style: normal; font-weight: bold; background-color: lightgreen }");
      pw.println ("em.roll      { font-style: normal; font-weight: bold; background-color: yellow }");
      pw.println ("em.ftf       { font-style: normal; font-weight: bold; color: blue; background-color: yellow }");
      pw.println ("em.tbd       { font-style: normal; background-color: pink }");
      pw.println (".bug         { font-size: small; }");
      pw.println ("</style>\n");
   }

   private static void countCards()
   {
      System.out.println ("Cachers  : " + Cacher.CACHERS.size());
      System.out.println ("Equipment: " + Equipment.EQUIPMENT.size());
      System.out.println ("Events   : " + Event.EVENTS.size());
      System.out.println ("Combos   : " + Combo.COMBOS.size());
      System.out.println ("Caches   : " + Geocache.CACHES.size());
      System.out.println ("Trvl Bugs: " + TravelBug.BUGS.size());
      System.out.println();
   }

   private static void checkAllCardReferences()
   {
      Pattern pattern =
         Pattern.compile ("<em class=(cacher|combo|equipment|event|tb)>([^<]*)</em>",
                          Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
      
      for (Card card : Cacher.CACHERS.values())
         checkCard (pattern, card.getText(), "Cacher: " + card.getName());
      for (Card card : Equipment.EQUIPMENT.values())
         checkCard (pattern, card.getText(), "Equipment: " + card.getName());
      for (Card card : Combo.COMBOS.values())
         checkCard (pattern, card.getText(), "Combo: " + card.getName());
      for (Card card : Event.EVENTS.values())
         checkCard (pattern, card.getText(), "Event: " + card.getName());
      for (Card card : Geocache.CACHES.values())
         checkCard (pattern, card.getText(), "Geocache: " + card.getName());
      for (Card card : TravelBug.BUGS.values())
         checkCard (pattern, card.getText(), "Travel Bug: " + card.getName());
      
      String rules = FileUtils.getText ("docs/Rules.html");
      checkCard (pattern, rules, "Rules.html");
      
      for (String ref : REFERENCES)
      {
         if (ref.startsWith ("Found"))
            System.out.println (ref);
         else // missing
            System.err.println (ref);
         System.out.flush();
         System.err.flush();
         Utils.sleep (5); // to avoid stdout/stderr interleaving
      }

      System.out.println();
   }

   private static void checkCard (final Pattern pattern, final String textToCheck, final String source)
   {
      if (textToCheck != null)
      {
         Matcher m = pattern.matcher (textToCheck);
         while (m.find())
         {
            String type = m.group (1);
            String cardName = m.group (2).replaceAll ("\n", " ").trim();
            String status = findCard (cardName, type);
            REFERENCES.add (status + type + " [" + cardName + "]" + " referenced by " + source);
         }
      }
   }
   
   private static String findCard (final String cardName, final String type)
   {
      Card card = null;
      if (type.equals ("cacher"))
         card = Cacher.CACHERS.get (cardName);
      else if (type.equals ("equipment"))
         card = Equipment.EQUIPMENT.get (cardName);
      else if (type.equals ("combo"))
         card = Combo.COMBOS.get (cardName);
      else if (type.equals ("event"))
         card = Event.EVENTS.get (cardName);
      else if (type.equals ("geocache"))
         card = Geocache.CACHES.get (cardName);
      else if (type.equals ("tb"))
         card = TravelBug.BUGS.get (cardName);
      return card != null ? "Found: " : "MISSING: ";
   }
   
   private static void findUnannotatedCardReferences()
   {
      findUnannotatedCardReferences ("docs/Rules.html");
      findUnannotatedCardReferences ("src/geoquest/Cacher.java");
      findUnannotatedCardReferences ("src/geoquest/Combo.java");
      findUnannotatedCardReferences ("src/geoquest/Equipment.java");
      findUnannotatedCardReferences ("src/geoquest/Event.java");
      findUnannotatedCardReferences ("src/geoquest/Geocache.java");
      findUnannotatedCardReferences ("src/geoquest/TravelBug.java");
   }
   
   private static void findUnannotatedCardReferences (final String fileName)
   {
      System.out.println ("Checking " + fileName + "...");
      String textToCheck = FileUtils.getText (fileName);
      
      for (Card card : Cacher.CACHERS.values())
         checkCard (card.getName(), "cacher", textToCheck);
      for (Card card : Equipment.EQUIPMENT.values())
         checkCard (card.getName(), "equipment", textToCheck);
      for (Card card : Combo.COMBOS.values())
         checkCard (card.getName(), "combo", textToCheck);
      for (Card card : Event.EVENTS.values())
         checkCard (card.getName(), "event", textToCheck);
      for (Card card : Geocache.CACHES.values())
         checkCard (card.getName(), "geocache", textToCheck);
      for (Card card : TravelBug.BUGS.values())
         checkCard (card.getName(), "tb", textToCheck);
      
      System.out.println();
   }

   private static void checkCard (final String cardName, final String cardType, final String textToCheck)
   {
      String regex = cardName.replace (" ", "\\s");
      // regex = Pattern.quote (regex);
      regex = regex.replace ("?", Pattern.quote ("?"));
      regex = regex.replace ("[", Pattern.quote ("["));
      regex = regex.replace ("]", Pattern.quote ("]"));
      regex = ".{25}\\b" + regex + "\\b.{5}"; // capture some context
      Pattern pattern =
         Pattern.compile (regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
      regex = "<em class=(" + cardType + ")>([^<]*)</em>";
      Pattern annotated = Pattern.compile (regex);
      regex = "\"\\[?" + cardName + "\\]?\""; 
      Pattern quoted = Pattern.compile (regex);

      Matcher m = pattern.matcher (textToCheck);
      while (m.find()) // for each reference
      {
         String line = "..." + m.group().replaceAll ("\n", " ") + "...";
         if (annotated.matcher (line).find()) // if annotated
            continue;
         if (quoted.matcher (line).find()) // if quoted
            continue;
         System.out.println ("  " + cardType + " " + cardName + ": " + line);
      }
   }
   
   public static void main (final String[] args)
   {
      countCards();
      checkAllCardReferences();
      findUnannotatedCardReferences();
      System.out.println ("Complete");
   }
}
