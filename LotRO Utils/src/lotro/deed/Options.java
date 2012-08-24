package lotro.deed;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import file.FileUtils;

public class Options
{
   private static final String OPTIONS = "data/options.txt";
   private static final Pattern OPT_PATTERN = Pattern.compile ("([^=]+)=([0-9]+)");

   private static final String MIN_GROUP = "Minimum Group Size";
   private static final String MAX_GROUP = "Maximum Group Size";
   private static final String GP_ROSTER = "GuildPortal Roster URL";
   
   static final String FORUM_URL =
      "http://palantiri.guildportal.com/Guild.aspx?GuildID=152273&TabID=1291953&ForumID=872303";
   
   private int minGroupSize = 2;
   private int maxGroupSize = 6;
   private String rosterURL;
   
   private OptionEditor optionEditor;
   
   public Options()
   {
      read();
   }
   
   int getMinGroupSize()
   {
      return minGroupSize;
   }
   
   void setMinGroupSize (final int minGroupSize)
   {
      this.minGroupSize = minGroupSize;
   }

   int getMaxGroupSize()
   {
      return maxGroupSize;
   }
   
   void setMaxGroupSize (final int maxGroupSize)
   {
      this.maxGroupSize = maxGroupSize;
   }

   String getRosterURL()
   {
      return rosterURL;
   }
   
   void setRosterURL (final String rosterURL)
   {
      this.rosterURL = rosterURL;
   }

   void configure (final JFrame owner)
   {
      if (optionEditor == null)
         optionEditor = new OptionEditor (owner);
      optionEditor.open (this);
   }
   
   public void read()
   {
      List<String> lines = FileUtils.getList (OPTIONS, FileUtils.UTF8, true);
      for (String line : lines)
         parseOption (line);
   }

   private void parseOption (final String line)
   {
      Matcher m = OPT_PATTERN.matcher (line);
      if (m.matches())
      {
         String name = m.group (1);
         if (name.equals (MIN_GROUP))
            minGroupSize = Integer.parseInt (m.group (2)); 
         else if (name.equals (MAX_GROUP))
            maxGroupSize = Integer.parseInt (m.group (2)); 
         else if (name.equals (GP_ROSTER))
            rosterURL = m.group (2); 
      }
   }
   
   public void write()
   {
      PrintWriter out = null;
      try
      {
         out = new PrintWriter (OPTIONS);
         out.println (MIN_GROUP + "=" + minGroupSize);
         out.println (MAX_GROUP + "=" + maxGroupSize);
         if (rosterURL != null)
            out.println (GP_ROSTER + "=" + rosterURL);
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
   
   public static void main (final String[] args)
   {
      Options options = new Options();
      System.out.println ("Min Group: " + options.minGroupSize);
      System.out.println ("Max Group: " + options.maxGroupSize);
   }
}
