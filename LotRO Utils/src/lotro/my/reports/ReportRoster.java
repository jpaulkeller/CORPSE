package lotro.my.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lotro.models.Alliance;
import lotro.models.Character;
import lotro.models.CharacterListModel;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.my.xml.KinshipXML;

public class ReportRoster extends Report
{
   public ReportRoster (final String name, final CharacterListModel model)
   {
      super (name, model);
   }
   
   public ReportRoster (final Kinship kinship, final String name)
   {
      super (kinship, name);
   }
   
   @Override
   public void exportAsHTML (final List<String> lines)
   {
      List<Character> sorted = new ArrayList<Character> (getCharacters());
      Collections.sort (sorted);
      boolean isFreep = !sorted.isEmpty() && sorted.get (0).isFreep();
      
      addTitle (lines);
      
      lines.add (getTableTag());
      lines.add ("  <thead>");
      lines.add ("  <tr bgcolor=yellow>");
      if (isAlliance())
         lines.add ("    <th>Kinship</th>");
      if (hasPlayers())
         lines.add ("    <th>Player</th>");
      lines.add ("    <th>Name</th>");
      lines.add ("    <th>Race</th>");
      lines.add ("    <th>Class</th>");
      if (isFreep)
         lines.add ("    <th>Level</th>");
      lines.add ("    <th>Kin Rank</th>");
      lines.add ("  </tr>");
      lines.add ("  </thead>");

      for (Character ch : sorted)
         exportCharacter (ch, lines, isFreep);

      lines.add ("</table>\n");
      
      addFooter (lines, "The data for this chart was provided by mylotro.com.");
   }

   private void exportCharacter (final Character ch, final List<String> lines,
                                 final boolean isFreep)
   {
      lines.add ("  <tr>");
      
      if (isAlliance())
         lines.add ("    <td align=left bgcolor=white>" + ch.getKinship() + "</td>");
      if (hasPlayers())
      {
         Player player = ch.getPlayer();
         String playerName = player != null ? player.getName() : "&nbsp;";
         lines.add ("    <td align=left bgcolor=white>" +
                    playerName + "</td>");
      }
      lines.add ("    <td align=left bgcolor=white>" + Report.getLink (ch) + "</td>");

      lines.add (getLine (ch.getRace().toString(), null, null, "white"));
      String bg = ch.getKlass().getColorBG ("#");
      String fg = ch.getKlass().getColorFG();
      lines.add ("    <td align=left bgcolor=" + bg + ">" +
                 "<font color=" + fg + ">" + ch.getKlass() + "</font></td>");
      if (isFreep)
         lines.add (getLine (ch.getLevel() + "", null, "center", "white"));
      lines.add (getLine (ch.getRank() + "", null, "center", "white"));
      
      lines.add ("  </tr>");
   }

   public static void main (final String[] args) throws Exception
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (false);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      Report app = new ReportRoster (kinship, "Roster");
      app.saveFile();
   }
}
