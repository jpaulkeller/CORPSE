package lotro.my.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lotro.models.Alliance;
import lotro.models.Character;
import lotro.models.Craft;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.models.Profession;
import lotro.my.xml.KinshipXML;

public class ReportCraft extends Report
{
   private CharacterFilter filter;
   
   public ReportCraft (final Kinship kinship, final String name)
   {
      super (kinship, name);
      filter = new CraftFilter (5);
   }

   // Since the CraftFilter requires character details, it is added only for
   // the duration of this method.
   
   @Override
   public void exportAsHTML (final List<String> lines)
   {
      getKinship().addFilter (filter);
      List<Character> sorted = new ArrayList<Character> (getCharacters());
      Collections.sort (sorted, new Character.ByVocation());

      addTitle (lines);

      lines.add (getTableTag());

      lines.add ("  <thead>");
      lines.add ("  <tr bgcolor=yellow>");
      if (isAlliance())
         lines.add ("    <th>Kinship</th>");
      if (hasPlayers())
         lines.add ("    <th>Player</th>");
      lines.add ("    <th>Name</th>");
      lines.add ("    <th>Craft</th>");
      for (Profession p : Profession.getCrafters())
         lines.add ("    <th>" + p + "</th>");
      lines.add ("  </tr>");
      lines.add ("  </thead>");

      for (Character ch : sorted)
         exportCharacter (ch, lines, "small");

      lines.add ("</table>\n");
      
      addFooter (lines, "The data for this chart was provided by mylotro.com.");
      
      getKinship().removeFilter (filter);
   }

   private void exportCharacter (final Character ch, final List<String> lines,
                                 final String style)
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

      Craft craft = ch.getCraft();
      lines.add (getLine (craft.getVocation() + "", null, "left", "white"));
      for (Profession p : Profession.getCrafters())
         if (craft.getVocation().getProfessions().contains (p))
         {
            String title = craft.getTitle (p);
            String color = title.equals ("Supreme Master") ? "cyan" : "white";
            lines.add (getLine (title, style, "left", color));
         }
         else
            lines.add ("    <td>&nbsp;</td>");
      
      lines.add ("  </tr>");
   }

   public static class CraftFilter implements CharacterFilter
   {
      private List<Profession> crafters;
      private int minMastery;
      
      public CraftFilter (final int minMastery)
      {
         crafters = Profession.getCrafters();
         this.minMastery = minMastery;
      }
      
      public boolean include (final Character ch)
      {
         Craft craft = ch.getCraft();
         if (craft != null)
            for (Profession p : craft.getProfessions())
               if (crafters.contains (p) && craft.getMastery (p) >= minMastery)
                  return true;
         return false;
      }
   }
   
   public static void main (final String[] args) throws Exception
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (true);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      Report app = new ReportCraft (kinship, "Master Crafters");
      app.saveFile();
   }
}
