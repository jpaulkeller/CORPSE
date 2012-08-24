package lotro.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseForum
{
   private static final String INPUT_PATH  = "C:/pkgs/workspace/LOTRO/LOTRO-coords.txt";
   private static final String OUTPUT_PATH = "C:/pkgs/workspace/LOTRO/sql/LOTRO2.";

   private static final String LOC_REGEX = "([0-9]+[.][0-9][NS],? *[0-9]+[.][0-9][EW]) *";
   private static final Pattern ENTRY_PATTERN =
      Pattern.compile ("(?:[-]: )?([-A-Za-z '&()]+):? [(]?" + LOC_REGEX + "[)]?");
   private static final Pattern REGION_PATTERN = Pattern.compile ("[A-Z][-A-Za-z ']+");

   private List<Entry> entries = new ArrayList<Entry>();
   
   private PrintStream sql = null;
   private PrintStream wiki = null;
   
   public ParseForum()
   {
      try
      {
         sql = new PrintStream (new FileOutputStream (OUTPUT_PATH + "sql"));
         wiki = new PrintStream (new FileOutputStream (OUTPUT_PATH + "wiki"));

         read (INPUT_PATH);
         Collections.sort (entries);
         
         writeSQL();
         writeWiki();
      }
      catch (Exception x)
      {
         System.out.println (x);
         x.printStackTrace();
      }
      finally
      {
         if (sql != null)
         {
            sql.println ("\ndisconnect;\n");
            sql.flush();
            sql.close();
         }
         if (wiki != null)
         {
            wiki.flush();
            wiki.close();
         }
      }
   }

   private void writeSQL()
   {
      sql.println ("connect Demo;\n");
      
      sql.println ("drop table LOTRO;");
      sql.println ("create table LOTRO (\n" +
                   "   Region VARCHAR,\n" +
                   "   Locale VARCHAR,\n" +
                   "   Symbol VARCHAR,\n" +
                   "   Landmark VARCHAR,\n" +
                   "   Location VARCHAR,\n" +
                   "   Notes VARCHAR);\n");
      
      StringBuffer sb = new StringBuffer();
      for (Entry entry : entries)
      {
         sb.setLength (0);
         sb.append ("insert into LOTRO values (");
         sb.append (quoteValue (entry.region) + ", ");
         sb.append (quoteValue (entry.locale) + ", ");
         sb.append (quoteValue (entry.symbol) + ", ");
         sb.append (quoteValue (entry.name) + ", ");
         sb.append (quoteValue (entry.location) + ", ");
         sb.append ("'');");
         sql.println (sb.toString());
      }
   }

   private void writeWiki()
   {
      String prevRegion = null;
      String prevLocale = null;
      
      for (Entry entry : entries)
      {
         if (!entry.locale.equals (prevLocale))
         {
            if (!entry.region.equals (prevRegion))
            {
               wiki.println ("<br>\n");
               wiki.println ("== [[" + entry.region + "]] ==");
               prevRegion = entry.region;
            }
            wiki.println ("=== [[" + entry.locale + "]] ===");
            prevLocale = entry.locale;
         }
         wiki.println ("* [[" + entry.name + "]] - " + entry.location);
      }
   }

   public void read (final String address)
   {
      System.out.println ("\nReading: " + address);
      
      try
      {
         InputStreamReader isr = null;
         if (new File (address).exists())
            isr = new FileReader (address);
         if (isr == null)
         {
            System.out.println ("Unable to open: " + address);
            return;
         }
         
         BufferedReader br = new BufferedReader (isr);
         String line;
         String region = null;
         String locale = null;
         
         while ((line = br.readLine()) != null)
         {
            line = line.trim();
            if (line.length() == 0)
            {
               if (region == null)
                  region = locale;
               continue;
            }
            
            Matcher m = ENTRY_PATTERN.matcher (line);
            if (m.matches())
            {
               String place = m.group (1);
               String coord = m.group (2);
               addEntry (region, locale, place, coord);
            }
            else if (line.startsWith ("-: "))
               // System.out.println ("? [" + line + "]");
               continue;
            else if (line.startsWith ("---"))
            {
               // System.out.println ("Clearing region");
               region = null;
               locale = null;
            }
            else if (REGION_PATTERN.matcher (line).matches())
            {
               locale = line;
               // System.out.println ("Locale: [" + locale + "]");
            }
            else
               System.out.println ("? [" + line + "]");
         }
         
         br.close();
      }
      catch (Exception x)
      {
         System.out.println (x);
         x.printStackTrace();
      }
   }
   
   private static String quoteValue (final String value)
   {
      if (value == null)
         return "''";
      return "'" + value.replaceAll ("'", "''") + "'";
   }
   
   void addEntry (final String region, final String locale, 
                  final String name, final String location)
   {
      Entry entry = new Entry();
      entry.region = region;
      entry.locale = locale;
      entry.symbol = getSymbol (name.toLowerCase());
      entry.name = name;
      entry.location = location;
      entries.add (entry);
   }
   
   private String getSymbol (final String name)
   {
      if (name.contains ("bank"))        return "Bank";
      if (name.contains ("vault"))       return "Bank";
      if (name.contains ("bard"))        return "Bard";
      if (name.contains ("fields"))      return "Crops";
      if (name.contains ("farm"))        return "Farm";
      if (name.contains ("flower"))      return "Flower";
      if (name.contains ("forge"))       return "Forge";
      if (name.contains ("mailbox"))     return "Mailbox";
      if (name.contains ("oven"))        return "Oven";
      if (name.contains ("provisioner")) return "Provisioner";
      if (name.contains ("stable"))      return "Stable";
      if (name.contains ("trainer"))     return "Trainer";
      if (name.contains ("workbench"))   return "Workbench";
      return "LOTRO";
   }
   
   class Entry implements Comparable<Entry>
   {
      private String region;
      private String locale;
      private String symbol;
      private String name;
      private String location;
      
      @Override
      public String toString()
      {
         return region + ":" + locale + ":" + name + ":" + location;
      }
      
      public int compareTo (final Entry other)
      {
         return toString().compareTo (other.toString());
      }
   }

   public static void main (final String[] args) throws Exception
   {
      new ParseForum();
   }
}
