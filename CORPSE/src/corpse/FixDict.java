package corpse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

public class FixDict
{
   private static String path = "D:/pkgs/workspace/Personal/CORPSE/data/Tables/Names/Racial Names/";
   
   private static final List<Pattern> STRIP = new ArrayList<>();
   static
   {
      STRIP.add(Pattern.compile(" [◇◈] .*"));
      STRIP.add(Pattern.compile("[A-Za-z]+/[0-9IVXAE]+([-:][0-9IVX]+)?,? ?")); // Ety/349, VT/45:6
      STRIP.add(Pattern.compile("\\[.+?\\] ?")); // [ɑdlˈɑnnɔ]
      STRIP.add(Pattern.compile(" ← [^ ]+")); // ← Abonnen
      STRIP.add(Pattern.compile("[A-Z][a-z]+[.] "));
   }
   
   private static final Map<String, String> TYPES = new HashMap<>(); // part of speech
   static
   {
      TYPES.put("adj[.]", "adjective");
      TYPES.put("adv[.]", "adverb");
      TYPES.put("conj[.]", "conjunction");
      TYPES.put("der[.] pl[.] of", "noun (p)");
      TYPES.put("ger[.]", "gerund");
      TYPES.put("inf[.]", "infinitive");
      TYPES.put("interj[.]", "interjection");
      TYPES.put("n[.]", "noun");
      TYPES.put("pa[.] t[.]", "verb (pt)");
      TYPES.put("part[.](?: of)?", "participle");
      TYPES.put("perf[.] of", "verb (p)");
      TYPES.put("pp[.] of", "verb (pp)");
      TYPES.put("pref[.]", "prefix");
      TYPES.put("prep[.]", "preposition");
      TYPES.put("pron[.]", "pronoun");
      TYPES.put("(?:soft |nasal )?mut[.](?: (?:coll|pl)[.])? of", "variant");
      TYPES.put("v[.]", "verb");
   }
   
   private static String TypeRegex = "";
   static
   {
      for (String type : TYPES.keySet())
         TypeRegex += type + "|"; // TODO
      TypeRegex = TypeRegex.substring(0, TypeRegex.length() - 1);
   }
   
   private static final String WORD_REGEX = "[†*∗]?([^- →]+)-?";
   private static Pattern STANDARD = Pattern.compile(WORD_REGEX + " (.+?) (" + TypeRegex + ") ([^→]+)");
   private static Pattern ALT = Pattern.compile(WORD_REGEX + " [INS* .,Arch]+?(?: ([a-z .]+))? → " + WORD_REGEX);
   
   static class Definition // dictionary entry
   {
      String type;
      String meaning;
      
      public Definition(final String type, final String meaning)
      {
         this.type = type; // TYPES.get(type);
         this.meaning = meaning;
      }
   }

   private static final SortedMap<String, Definition> WORDS = new TreeMap<>();

   public static void main(String[] args)
   {
      read();
      write();
   }

   private static void read()
   {
      InputStream is = null;
      BufferedReader br = null;

      try
      {
         is = new FileInputStream(path + "Sindarin Dictionary.txt");
         if (is != null)
         {
            InputStreamReader isr = new InputStreamReader(is, FileUtils.UTF8);
            br = new BufferedReader(isr);

            String line = null;
            while ((line = br.readLine()) != null)
            {
               line = strip(line);
               Matcher m = STANDARD.matcher(line);
               if (m.matches())
                  addWord(m.group(1), m.group(3), m.group(4));
               else if ((m = ALT.matcher(line)).matches())
               {
                  String type = "alternate";
                  if ("pl.".equals(m.group(2)))
                     type = "plural";
                  addWord(m.group(1), type, m.group(3));
               }
               else if (line.length() > 1)
                  System.out.println(line); //TODO
            }
         }
      }
      catch (Exception x)
      {
         x.printStackTrace(System.err);
      }
      finally
      {
         FileUtils.close(br);
         FileUtils.close(is);
      }
   }

   private static String strip(String line)
   {
      for (Pattern p : STRIP)
      {
         Matcher m = p.matcher(line);
         line = m.replaceAll("");
      }
      return line;
   }
   
   private static void addWord(final String sindarin, final String type, final String meaning)
   {
      WORDS.put(sindarin, new Definition(type, meaning));
   }

   private static void write()
   {
      PrintStream out = null;

      try
      {
         FileOutputStream fos = new FileOutputStream(path + "Sindarin Dictionary.tbl", false);
         out = new PrintStream(fos, true, FileUtils.UTF8);

         out.println("/ http://www.jrrvf.com/hisweloke/sindar/online/sindar/dict-sd-en.html\n");
         out.println(".Sindarin, Type, Definition\n");

         System.out.println("\nWords: " + WORDS.size() + "\n");
         
         for (Entry<String, Definition> entry : WORDS.entrySet())
         {
            String sindarin = entry.getKey();
            Definition def = entry.getValue();
            if (def.type.equals("alternate") || def.type.equals("plural"))
            {
               Definition alt = WORDS.get(def.meaning);
               if (alt != null)
               {
                  if (def.type.equals("plural"))
                     def.meaning = "plural of " + alt.meaning;
                  else
                     def.meaning = alt.meaning;
                  def.type = alt.type + " (pl)";
               }
            }
            out.println(sindarin + "; " + def.type + "; " + def.meaning);
         }

         out.flush();
      }
      catch (Exception x)
      {
         x.printStackTrace(System.err);
      }
      finally
      {
         FileUtils.close(out);
      }
   }
}
