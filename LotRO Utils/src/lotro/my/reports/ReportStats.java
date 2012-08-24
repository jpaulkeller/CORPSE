package lotro.my.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lotro.models.Alliance;
import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.models.Stats;
import lotro.my.xml.KinshipXML;

public final class ReportStats extends Report
{
   public ReportStats (final Kinship kinship, final String name)
   {
      super (kinship, name);
   }
   
   @Override
   public void exportAsHTML (final List<String> lines)
   {
      List<Character> sorted = new ArrayList<Character> (getCharacters());
      Collections.sort (sorted, new Character.ByClass());
      boolean isFreep = !sorted.isEmpty() && sorted.get (0).isFreep();

      // add special tool tips
      for (Character ch : sorted)
         lines.add (ch.getCraft().getTooltipText (ch.getName()));
      lines.add ("");
         
      addTitle (lines);

      lines.add (getTableTag());

      String style = "small";
      lines.add ("  <thead>");
      lines.add ("  <tr bgcolor=yellow>");
      if (isAlliance())
         lines.add ("    <th class=" + style + ">Kinship</th>");
      if (hasPlayers())
         lines.add ("    <th class=" + style + ">Player</th>");
      lines.add ("    <th class=" + style + ">Name</th>");
      lines.add ("    <th class=" + style + ">Race</th>");
      lines.add ("    <th class=" + style + ">Class</th>");
      if (isFreep)
      {
         lines.add ("    <th class=" + style + ">Level</th>");
         lines.add ("    <th class=" + style + ">Craft</th>");
      }
      else
      {
         lines.add ("    <th class=" + style + ">Rank</th>");
         lines.add ("    <th class=" + style + ">Infamy</th>");
         lines.add ("    <th class=" + style + ">Rating</th>");
      }
      
      for (Stats attr : Stats.values())
         if (isFreep || !attr.isFreepOnly())
            lines.add ("    <th class=" + style + " bgcolor=yellow>" + attr + "</th>");
      lines.add ("  </tr>");
      lines.add ("  </thead>");

      for (Character ch : sorted)
         exportCharacterStats (ch, lines, style, isFreep);

      lines.add ("</table>\n");
      
      addFooter (lines, "The data for this chart was provided by mylotro.com, " +
                 "which includes any buffs (e.g., residual Hope from tokens) you had " +
                 "when you last logged out.");
   }

   private void exportCharacterStats (final Character ch, final List<String> lines,
                                      final String style, final boolean isFreep)
   {
      lines.add ("  <tr>");
      
      if (isAlliance())
         lines.add ("    <td class=" + style + " align=left bgcolor=white>" +
                    ch.getKinship() + "</td>");
      if (hasPlayers())
      {
         Player player = ch.getPlayer();
         String playerName = player != null ? player.getName() : "&nbsp;";
         lines.add ("    <td class=" + style + " align=left bgcolor=white>" +
                    playerName + "</td>");
      }
      lines.add ("    <td class=" + style + " align=left bgcolor=white>" +
                 Report.getLink (ch) + "</td>");
      lines.add (getLine (ch.getRace().toString(), style, null, "white"));
      String bg = ch.getKlass().getColorBG ("#");
      String fg = ch.getKlass().getColorFG();
      lines.add ("    <td class=" + style + " align=left bgcolor=" + bg + ">" +
                 "<font color=" + fg + ">" + ch.getKlass() + "</font></td>");
      if (isFreep)
      {
         lines.add (getLine (ch.getLevel() + "", style, "center", "white"));
         lines.add (getLine (ch.getCraft().getTooltipLink(), style, "center", "white"));
      }
      else
      {
         lines.add (getLine (ch.getProp ("PvMP Rank") , style, "center", "pink"));
         lines.add (getLine (ch.getProp ("Glory") , style, "center", "pink"));
         lines.add (getLine (ch.getProp ("Rating") , style, "center", "pink"));
      }
      
      for (Stats attr : Stats.values())
         if (isFreep || !attr.isFreepOnly())
            lines.add (getLine (ch, attr, style));
      lines.add ("  </tr>");
   }

   public static void main (final String[] args) throws Exception
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (true);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "Ranya Palantiri");
      
      kinship.setFilter (FilterFactory.getLevelFilter (50));
      Report app = new ReportStats (kinship, "Stats 50+");
      app.saveFile();
      
      kinship.setFilter (FilterFactory.getLevelFilter (75));
      app.setName ("Stats 75");
      app.saveFile();
   }
}
