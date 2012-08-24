
package lotro.models;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

public class Deed implements Comparable<Deed>
{
   // Region, Name, Type, Trait, Level
   private static final Pattern DEED_PATTERN =
      Pattern.compile (" *([^,]+), *([^,]+), *([^,]+), *([^,]+)?(?:, *([0-9]+)?)?");
   
   public static final String FILE = "data/deeds.txt";
   
   private String region;
   private String name;
   private String type;
   private String trait;
   private int level;
   
   // derived fields
   private String abbrev;
   private String shortName;
   private String key;
   private int hash;
   
   public Deed (final String region, final String name, final String type,
                final String trait, final int level)
   {
      this.region = region;
      this.name = name;
      this.type = type;
      this.trait = trait;
      this.level = level;
      
      this.key = getKey (region, name);
      this.hash = key.hashCode();
      
      shortName = name;
      int brk = shortName.indexOf (" (Advanced)");
      if (brk > 0)
         shortName = shortName.substring (0, brk);
      brk = shortName.indexOf ("-slayer");
      if (brk > 0)
         shortName = shortName.substring (0, brk);
      brk = shortName.indexOf ("-kicker");
      if (brk > 0)
         shortName = shortName.substring (0, brk);
      brk = shortName.indexOf (" Slayer");
      if (brk > 0)
         shortName = shortName.substring (0, brk);
      
      abbrev = region;
      if (abbrev.equals ("Bree-land"))
         abbrev = "Bree";
      else
      {
         int space = abbrev.indexOf (' ');
         int dash = abbrev.indexOf ('-');
         if (space > 0)
            abbrev = abbrev.charAt (0) + abbrev.substring (space + 1, space + 2);
         else if (dash > 0)
            abbrev = abbrev.charAt (0) + abbrev.substring (dash + 1, dash + 2).toUpperCase();
      }
      abbrev = abbrev + " " + shortName;
   }
   
   public String getType()
   {
      return type;
   }
   
   public void setType (final String type)
   {
      this.type = type;
   }

   public String getKey()
   {
      return key;
   }
   
   public String getRegion()
   {
      return region;
   }

   public String getName()
   {
      return name;
   }

   public String getShortName()
   {
      return shortName;
   }

   public String getTrait()
   {
      return trait;
   }

   public void setTrait (final String trait)
   {
      this.trait = trait;
   }

   public int getLevel()
   {
      return level;
   }
   
   public void setLevel (final int level)
   {
      this.level = level;
   }

   public static String getKey (final String region, final String name)
   {
      return region + " " + name;      
   }
   
   @Override
   public String toString()
   {
      return abbrev;
   }
   
   public String encode()
   {
      StringBuilder sb = new StringBuilder();
      sb.append (region);
      sb.append (", ");
      sb.append (name);
      sb.append (", ");
      sb.append (type);
      sb.append (", ");
      sb.append (trait);
      sb.append (", ");
      sb.append (level);
      return sb.toString();
   }

   public int compareTo (final Deed o)
   {
      return key.compareTo (o.key);
   }
   
   @Override
   public boolean equals (final Object obj)
   {
      if (obj instanceof Deed)
         return key.equals (((Deed) obj).key);
      return super.equals (obj);
   }

   @Override
   public int hashCode()
   {
      return hash;
   }
   
   public static SortedMap<String, Deed> read (final String path)
   {
      SortedMap<String, Deed> deeds = new TreeMap<String, Deed>();

      List<String> lines = FileUtils.getList (path, FileUtils.UTF8, true);
      for (String line : lines)
      {
         Deed deed = parseDeed (line);
         if (deed != null)
            deeds.put (deed.getKey(), deed);
      }
      
      return deeds;
   }

   static Deed parseDeed (final String line)
   {
      Deed deed = null;
      
      Matcher m = DEED_PATTERN.matcher (line);
      if (m.matches())
      {
         String region = m.group (1);
         String name   = m.group (2);
         String type   = m.group (3);
         String trait  = m.group (4);
         
         int level = 0;
         if (m.group (5) != null)
            level = Integer.parseInt (m.group (5));
                  
         deed = new Deed (region, name, type, trait, level);
      }
      else
         System.err.println ("Ignoring Deed: " + line);
      
      return deed;
   }
   
   public static void write (final Collection<Deed> deeds, final String file)
   {
      PrintWriter out = null;
      try
      {
         out = new PrintWriter (file);
         for (Deed deed : deeds)
            out.println (deed.encode());
         out.flush();
      }
      catch (IOException x)
      {
         System.err.println (x);
      }
      finally
      {
         if (out != null)
            out.close();
      }
   }
}
