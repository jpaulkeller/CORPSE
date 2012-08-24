package lotro.raid;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotro.models.Skill;
import lotro.web.Dropbox;
import file.FileUtils;

public class Composition
{
   private static final Pattern COMPOSITION_PATTERN = // skill minPoints
      Pattern.compile (" *([A-Za-z]+) +([0-9]+) *");

   private String name;
   private int max;
   private Map<Skill, Integer> skills = new LinkedHashMap<Skill, Integer>();
   
   public void setName (final String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }
   
   public void setMax (final int max)
   {
      this.max = max;
   }

   public int getMax()
   {
      return max;
   }
   
   public void add (final Skill skill, final int score)
   {
      skills.put (skill, score);
   }
   
   public Map<Skill, Integer> getSkills()
   {
      return skills;
   }

   public String getStatusColor (final int count)
   {
      String color = "#ffdddd"; // pink
      if (count > max)
         color = "#aaaaff"; // blue
      else if (count == max)
         color = "#aaffaa"; // green
      else if (count >= max * 0.75)
         color = "#ffffbb"; // yellow
      else if (count >= max * 0.50)
         color = "#eecc99"; // orange
      return color;
   }
   
   public static Composition loadFromFile (final String path)
   {
      Composition comp = new Composition();
      String name = FileUtils.getNameWithoutSuffix (new File (path));
      comp.setName (name.replace ("%20", " "));

      List<String> lines = FileUtils.getList (path, FileUtils.UTF8, true);
      if (lines.size() > 1)
      {
         comp.setMax (Integer.parseInt (lines.remove (0)));
         for (String line : lines)
            parseLine (comp, line);
      }

      return comp;
   }

   private static void parseLine (final Composition comp, final String line)
   {
      Matcher m = COMPOSITION_PATTERN.matcher (line);
      if (m.matches())
      {
         Skill skill = Skill.parse (m.group (1));
         int score = Integer.parseInt (m.group (2));
         comp.add (skill, score);
      }
   }
   
   public static void main (final String[] args)
   {
      String raid = Dropbox.get().getPath ("/raids/Rift.raid");
      Composition comp = Composition.loadFromFile (raid);
      System.out.println (comp.getName());
      for (Skill skill : comp.getSkills().keySet())
         System.out.println (" > " + skill + " " + comp.getSkills().get (skill));
   }
   
   /*
   private static final Pattern COMPOSITION_PATTERN = // class score
      Pattern.compile (" *([-A-Za-z]+) *([0-9]+) *");

   private String name;
   private int max;
   
   private List<CompSlot> slots = new ArrayList<CompSlot>();
   
   private Composition()
   {
   }
   
   public Composition (final String name, final int total)
   {
      this.name = name;
      this.max = total;
   }
   
   public void add (final Klass klass, final int score)
   {
      slots.add (new CompSlot (klass, score));
   }

   public void setName (final String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }
   
   public void setMax (final int max)
   {
      this.max = max;
   }

   public int getMax()
   {
      return max;
   }
   
   public List<CompSlot> getSlots()
   {
      return slots;
   }
   
   public String getStatusColor (final int count)
   {
      String color = "#ffdddd"; // pink
      if (count > max)
         color = "#aaaaff"; // blue
      else if (count == max)
         color = "#aaffaa"; // green
      else if (count >= max * 0.75)
         color = "#ffffbb"; // yellow
      else if (count >= max * 0.50)
         color = "#eecc99"; // orange
      return color;
   }
   
   public static Composition loadFromFile (final String path)
   {
      Composition comp = new Composition();
      String name = FileUtils.getNameWithoutSuffix (new File (path));
      comp.setName (name.replace ("%20", " "));

      List<String> lines = FileUtils.getList (path, FileUtils.UTF8, true);
      if (lines.size() > 1)
      {
         comp.setMax (Integer.parseInt (lines.remove (0)));
         for (String line : lines)
            parseLine (comp, line);
      }

      return comp;
   }

   private static void parseLine (final Composition comp, final String line)
   {
      Matcher m = COMPOSITION_PATTERN.matcher (line);
      if (m.matches())
      {
         Klass klass = Klass.parse (m.group (1));
         int score = Integer.parseInt (m.group (2));
         comp.add (klass, score);
      }
   }
   
   class CompSlot
   {
      private Klass klass;
      private int score;
      
      CompSlot (final Klass klass, final int score)
      {
         this.klass = klass;
         this.score = score;
      }

      public Klass getKlass()
      {
         return klass;
      }
      
      public int getScore()
      {
         return score;
      }

      public String getColor()
      {
         return Score.getColor (score);
      }
      
      @Override
      public String toString()
      {
         return klass + " " + score; 
      }
   }
   
   public static void main (final String[] args)
   {
      String raid = Dropbox.get().getPath ("/raids/Rift Day 3 Balrog.raid");
      Composition comp = Composition.loadFromFile (raid);
      System.out.println (comp.getName());
      for (CompSlot slot : comp.getSlots())
         System.out.println (" > " + slot);
   }
   */
}
