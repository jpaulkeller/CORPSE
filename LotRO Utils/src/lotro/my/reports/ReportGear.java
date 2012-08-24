package lotro.my.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lotro.models.Alliance;
import lotro.models.Character;
import lotro.models.Equipment;
import lotro.models.Item;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.models.Slot;
import lotro.models.Stats;
import lotro.my.xml.KinshipXML;

public final class ReportGear extends Report
{
   public ReportGear (final Kinship kinship, final String name)
   {
      super (kinship, name);
   }
   
   @Override
   public void exportAsHTML (final List<String> lines)
   {
      List<Character> sorted = new ArrayList<Character> (getCharacters());
      Collections.sort (sorted, new Character.ByClass());

      addTitle (lines);

      List<String> headers = new ArrayList<String>();
      if (isAlliance())
         headers.add ("Kinship");
      if (hasPlayers())
         headers.add ("Player");
      headers.add ("Character");
      headers.add ("Class");
      headers.add ("Level");
      headers.add ("Morale");
      headers.add ("Power");
      headers.add ("Armor");
      for (Slot slot : Slot.values())
         headers.add (slot.getAbbrev());
      
      lines.add (getTableTag());
      lines.add ("  <thead>");
      lines.add ("  <tr bgcolor=yellow>");
      for (String header : headers)
         lines.add ("    <th>" + header + "</th>");
      lines.add ("  </tr>");
      lines.add ("  </thead>");
      
      for (Character ch : sorted)
         exportCharacter (ch, lines);

      lines.add ("</table>\n");
      
      addFooter (lines, "The data for this chart was provided by mylotro.com, " +
      "which only shows the gear that you had equipped when you last logged out.");
   }

   private void exportCharacter (final Character ch, final List<String> lines)
   {
      lines.add ("  <tr>");
      
      if (isAlliance())
         lines.add ("    <td align=left bgcolor=white>" + ch.getKinship() + "</td>");
      if (hasPlayers())
      {
         Player player = ch.getPlayer();
         String playerName = player != null ? player.getName() : "&nbsp;";
         lines.add ("    <td align=left bgcolor=white>" + playerName + "</td>");
      }
      lines.add ("    <td align=left bgcolor=white>" + Report.getLink (ch) + "</td>");
      String bg = ch.getKlass().getColorBG ("#");
      String fg = ch.getKlass().getColorFG();
      lines.add ("    <td align=left bgcolor=" + bg + "><font color=" + fg + ">" +
                 ch.getKlass() + "</font></td>");
      lines.add (getLine (ch.getLevel() + "", null, "center", "white"));
      
      lines.add (getLine (ch, Stats.Morale, null));
      lines.add (getLine (ch, Stats.Power, null));
      lines.add (getLine (ch, Stats.Armour, null));
      
      Equipment eq = ch.getEquipment();
      for (Slot slot : Slot.values())
         if (eq != null)
            lines.add (getLine (eq.getItem (slot)));
         else
            lines.add ("<td>&nbsp;</td>");
      
      lines.add ("  </tr>");
   }

   private String getLine (final Item item)
   {
      StringBuilder sb = new StringBuilder();
      if (item != null && item.getSlot() != null)
      {
         sb.append ("    <td align=center bgcolor=" + item.getSlot().getColor() + ">");
         sb.append ("<a href=\"" + item.getLorebookLink() + "\"");
         sb.append (" title=\"" + item.getName() + "\""); // tooltip
         sb.append (" target=_blank>");
         sb.append (item.getSlot().getAbbrev());
         sb.append ("</a");
         sb.append ("</td>");
      }
      else
         sb.append ("<td>&nbsp;</td>");
         
      return sb.toString();
   }
   
   public static void main (final String[] args) throws Exception
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (true);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      kinship.setFilter (FilterFactory.getLevelFilter (50));
      Report app = new ReportGear (kinship, "Gear");
      app.saveFile();
   }
}
