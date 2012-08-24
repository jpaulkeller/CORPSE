package lotro.raid;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JProgressBar;

import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Klass;
import lotro.models.Player;
import lotro.my.reports.FilterFactory;
import lotro.my.xml.KinshipXML;
import lotro.web.Dropbox;

public class Group
{
   private JProgressBar progress;
   private Map<Player, Signup> signups;
   private Set<Character> alternates = new TreeSet<Character>();

   private Composition comp;
   private List<Character> bestGroup;
   private int bestScore;
   
   public Group (final JProgressBar progress)
   {
      this.progress = progress;
   }
   
   public void setSignups (final Map<Player, Signup> signups)
   {
      this.signups = signups;
      alternates.clear();
      bestGroup = null;
   }
   
   public Collection<Signup> getSignups()
   {
      return signups.values();
   }

   public void setComposition (final Composition c)
   {
      this.comp = c;
   }
   
   public Composition getComposition()
   {
      return comp;
   }

   public void optimize()
   {
      GroupOptimizer graph = new GroupOptimizer (progress, getSignups(), comp.getMax());
      bestGroup = graph.getBestGroup();
      bestScore = graph.getBestScore();
      graph.score (bestGroup, true); // trace TBD
   }

   public boolean isOptimized()
   {
      return bestGroup != null;
   }
   
   public List<Character> getBestGroup()
   {
      return bestGroup;
   }
   
   public void saveAsHTML (final String path, final boolean showScore)
   {
      PrintStream out = null;
      try
      {
         FileOutputStream fos = new FileOutputStream (path);
         out = new PrintStream (fos, true, "UTF8");
         generateHTML (out, showScore);
         out.flush();
      }
      catch (IOException x)
      {
         System.out.println ("Unable to create: " + path);
         System.err.println (x);
         x.printStackTrace (System.err);
      }
      finally
      {
         if (out != null)
            out.close();
      }
   }

   public void generateHTML (final PrintStream out, final boolean showScore)
   {
      out.println ("<meta http-equiv=\"content-type\" content=\"text-html; charset=utf-8\">");

      out.println ("<table border=\"0\" cellpadding=\"10\">");
      out.println ("<tbody>");
      out.println ("<tr valign=top>");

      out.println ("<td>");
      out.println ("<table border=\"1\" cellpadding=\"1\">");
      out.println ("<tbody>");
      out.println ("  <tr bgcolor=yellow align=center>");
      out.println ("    <td colspan=3><b>" + comp.getName() + "</b></td>");
      out.println ("  </tr>");
      addHeader (out);

      int count = 0;
      for (Character ch : bestGroup)
      {
         if (ch != null) // TBD
         {
            Klass klass = ch.getKlass();
            out.println ("  <tr bgcolor=white>");
            out.println ("    <td bgcolor=\"" + klass.getColorBG ("#") + "\">" + klass +
                         " (" + ch.getLevel() + ")</td>");
            addCharacter (out, ch, showScore);
            out.println ("  </tr>");
            count++; // TBD
         }
      }

      String statColor = comp.getStatusColor (count);
      out.println ("  <tr>");
      out.println ("    <td colspan=\"3\" align=\"center\" bgcolor=\"" +
                   statColor + "\"><b>STATUS: " + bestScore + "</b></td>");
      out.println ("  </tr>");

      out.println ("</tbody>");
      out.println ("</table>");
      out.println ("</td>");

      exportBackups (out, showScore);

      out.println ("</tr>");
      out.println ("</table>");
      
      /*
      // out.println ("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
      out.println ("<meta http-equiv=\"content-type\" content=\"text-html; charset=utf-8\">");

      out.println ("<table border=\"0\" cellpadding=\"10\">");
      out.println ("<tbody>");
      out.println ("<tr valign=top>");

      out.println ("<td>");
      out.println ("<table border=\"1\" cellpadding=\"1\">");
      out.println ("<tbody>");
      out.println ("  <tr bgcolor=yellow align=center>");
      out.println ("    <td colspan=3><b>" + comp.getName() + "</b></td>");
      out.println ("  </tr>");
      addHeader (out);

      int lowestScore = 10;
      if (bestGroup.isEmpty())
         lowestScore = 0;
      else
      {
         List<Character> groupCopy = new ArrayList<Character> (bestGroup);
         for (Composition.CompSlot slot : comp.getSlots())
         {
            Character ch = pop (groupCopy, slot.getKlass());
            if (ch != null && slot.getScore() < lowestScore)
               lowestScore = slot.getScore();
         }
      }
      
      int count = 0;
      List<Character> groupCopy = new ArrayList<Character> (bestGroup);
      for (Composition.CompSlot slot : comp.getSlots())
      {
         Character ch = pop (groupCopy, slot.getKlass());
         if (ch != null || slot.getScore() > lowestScore)
         {
            String color = slot.getColor();
            out.println ("  <tr bgcolor=white>");
            Klass klass = ch != null ? ch.getKlass() : slot.getKlass();
            out.println ("    <td bgcolor=\"" + color + "\">" + klass +
                         " (" + slot.getScore() + ")</td>");
            addCharacter (out, ch, showScore);
            out.println ("  </tr>");
            if (ch != null)
               count++;
         }
      }

      if (!groupCopy.isEmpty())
      {
         out.println ("  <tr bgcolor=\"#52F3FF\">");
         out.println ("    <td colspan=3 align=center><b>UNKNOWN CLASSES</b></td>");
         out.println ("  </tr>");
         for (Character ch : groupCopy)
         {
            out.println ("  <tr>");
            out.println ("    <td>" + ch.getKlass() + "</td>");
            addCharacter (out, ch, showScore);
            out.println ("  </tr>");
            count++;
         }
      }

      String statColor = comp.getStatusColor (count);
      out.println ("  <tr>");
      out.println ("    <td colspan=\"3\" align=\"center\" bgcolor=\"" +
            statColor + "\"><b>STATUS: " + bestScore + "</b></td>");
      out.println ("  </tr>");

      out.println ("</tbody>");
      out.println ("</table>");
      out.println ("</td>");

      exportBackups (out, showScore);

      out.println ("</tr>");
      out.println ("</table>");
      */
   }

   private Character pop (final List<Character> group, final Klass klass)
   {
      Iterator<Character> iter = group.iterator();
      while (iter.hasNext())
      {
         Character c = iter.next();
         if (klass == Klass.Unknown || klass == c.getKlass())
         {
            iter.remove();
            return c;
         }
      }
      return null;
   }

   private void exportBackups (final PrintStream out, final boolean showScore)
   {
      // get a list of all players in the group
      List<Player> players = new ArrayList<Player>();
      for (Character ch : bestGroup)
         players.add (ch.getPlayer());

      List<Character> standby = new ArrayList<Character>();
      for (Signup signup : signups.values())
         if (!signup.isBackup())
         {
            for (Character ch : signup.getCharacters())
               if (!players.contains (ch.getPlayer())) // stand-by
               {
                  standby.addAll (signup.getCharacters());
                  break;
               }
               else if (!bestGroup.contains (ch))
                  alternates.add (ch);
         }

      int backupCount = 0;
      for (Signup signup : signups.values())
         if (signup.isBackup())
            backupCount++;
      
      if (backupCount > 0 || !standby.isEmpty() || !alternates.isEmpty())
      {
         out.println ("<td>");
         out.println ("<table border=\"1\" cellpadding=\"1\">");
         out.println ("<tbody>");

         showStandbys (out, showScore, standby);
         showBackups (out, backupCount);
         showAlternates (out, showScore);

         out.println ("</tbody>");
         out.println ("</table>");
         out.println ("</td>");
      }
   }

   private void showStandbys (final PrintStream out, 
                              final boolean showScore, 
                              final List<Character> standby)
   {
      if (!standby.isEmpty())
      {
         out.println ("  <tr bgcolor=\"#52F3FF\">");
         out.println ("    <td colspan=3 align=center><b>STAND-BY</b></td>");
         out.println ("  </tr>");
         addHeader (out);
         for (Character ch : standby)
         {
            out.println ("  <tr bgcolor=white>");
            out.println ("    <td>" + ch.getKlass() + "</td>");
            addCharacter (out, ch, showScore);
            out.println ("  </tr>");
         }
      }
   }

   private void showBackups (final PrintStream out, final int backupCount)
   {
      if (backupCount > 0)
      {
         out.println ("  <tr bgcolor=\"#52F3FF\">");
         out.println ("    <td colspan=3 align=center><b>BACKUPS</b></td>");
         out.println ("  </tr>");
         addHeader (out);
         for (Signup signup : signups.values())
            if (signup.isBackup())
               for (Character ch : signup.getCharacters())
               {
                  out.println ("  <tr bgcolor=white>");
                  out.println ("    <td>" + ch.getKlass() + "</td>");
                  addCharacter (out, ch, false);
                  out.println ("  </tr>");
               }
      }
   }

   private void showAlternates (final PrintStream out, final boolean showScore)
   {
      if (!alternates.isEmpty())
      {
         out.println ("  <tr bgcolor=\"#52F3FF\">");
         out.println ("    <td colspan=3 align=center><b>ALTERNATES</b></td>");
         out.println ("  </tr>");
         addHeader (out);
         for (Character alt : alternates)
         {
            out.println ("  <tr bgcolor=white>");
            out.println ("    <td>" + alt.getKlass() + "</td>");
            addCharacter (out, alt, showScore);
            out.println ("  </tr>");
         }
      }
   }

   private void addCharacter (final PrintStream out, 
                              final Character ch, 
                              final boolean showScore)
   {
      if (ch != null)
      {
         int score = ch.getScore();
         String s = showScore && score != 0 ? " (" + score + ")" : "";
         String color = getCharacterColor (score);
         out.println ("    <td" + color + "><b>" + ch.getName() + "</b>" + s + "</td>");
         
         Signup signup = signups.get (ch.getPlayer());
         score = signup.getScore();
         s = showScore && score != 0 ? " (" + score + ")" : "";
         color = getPlayerColor (score);
         out.println ("    <td" + color + ">" + ch.getPlayer() + s + "</td>");
      }
      else
      {
         out.println ("    <td>&nbsp;</td>");
         out.println ("    <td>&nbsp;</td>");
      }
   }
   
   private String getCharacterColor (final int score)
   {
      if (score >= 0)
         return "";
      else if (score >= -5)
         return " bgcolor=\"" + Score.getColor (6) + "\"";
      return " bgcolor=\"" + Score.getColor (4) + "\"";
   }

   private String getPlayerColor (final int score)
   {
      if (score > 1)
         return " bgcolor=\"" + Score.getColor (10) + "\"";
      else if (score == 1)
         return " bgcolor=\"" + Score.getColor (8) + "\"";
      return " bgcolor=\"" + Score.getColor (6) + "\"";
   }

   private void addHeader (final PrintStream out)
   {
      out.println ("  <tr bgcolor=\"#52F3FF\" align=center>"); // cyan
      out.println ("    <td><b>CLASS</b></td>");
      out.println ("    <td><b>CHARACTER</b></td>");
      out.println ("    <td><b>PLAYER</b></td>");
      out.println ("  </tr>");
   }

   public static void main (final String[] args)
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (false);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      kinship.setFilter (FilterFactory.getLevelFilter (48));
      
      String dropbox = Dropbox.get().getPath ("/raids/SK-09-01.signup");
      Map<Player, Signup> signups = Signup.loadFromFile (kinship, dropbox);
      String raid = Dropbox.get().getPath ("/raids/Rift Day 3 Balrog.raid");
      Composition comp = Composition.loadFromFile (raid);
      String desk = System.getProperty ("user.home") + "/Desktop";
      String path = desk + "/" + comp.getName() + ".html";

      Group group = new Group (null);
      group.setSignups (signups);
      group.setComposition (comp);
      group.optimize();
      group.saveAsHTML (path, true);
      System.out.println ("Output: " + path);
      System.exit (0);
   }
}
