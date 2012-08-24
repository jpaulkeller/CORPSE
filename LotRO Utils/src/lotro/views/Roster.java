package lotro.views;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import lotro.models.Character;
import lotro.models.CharacterListModel;
import lotro.my.reports.Report;
import lotro.raid.SignupListener;

public class Roster implements SignupListener
{
   private CharacterListModel model;
   private Map<Character, String> charHTML = new HashMap<Character, String>();
   private StringBuilder html = new StringBuilder();
   private JTextPane htmlPane;
   private JTextPane rawPane;
   private boolean updateNeeded;
   
   public Roster (final CharacterListModel model)
   {
      htmlPane = new JTextPane();
      htmlPane.setBackground (null);
      htmlPane.setEditable (false);
      htmlPane.setEditorKit (new HTMLEditorKit());
      
      rawPane = new JTextPane();
      rawPane.setBackground (null);
      rawPane.setEditable (false);
      
      this.model = model;
      for (Character ch : model)
         charHTML.put (ch, getHTML (ch));
      updateNeeded = true;
      update();
         
      model.addListener (this);
   }
   
   public JTextPane getHtmlPane()
   {
      return htmlPane;
   }
   
   public JTextPane getRawPane()
   {
      return rawPane;
   }
   
   public void characterAdded (final Character ch)
   {
      charHTML.put (ch, getHTML (ch));
      updateNeeded = true;
   }

   public void characterRemoved (final Character ch)
   {
      charHTML.remove (ch);
      updateNeeded = true;
   }

   public void characterUpdated (final Character ch)
   {
      charHTML.put (ch, getHTML (ch));
      updateNeeded = true;
   }
   
   private String getHTML (final Character ch)
   {
      StringBuilder sb = new StringBuilder();
      sb.append ("  <tr>\n");
      /*
      if (hasPlayers())
      {
         Player player = ch.getPlayer();
         String playerName = player != null ? player.getName() : "&nbsp;";
         sb.append ("    <td align=left bgcolor=white>" + playerName + "</td>\n");
      }
      */
      sb.append ("    <td align=left bgcolor=white>" + Report.getLink (ch) + "</td>\n");

      sb.append (Report.getLine (ch.getRace().toString(), null, null, "white") + "\n");
      String bg = ch.getKlass().getColorBG ("#");
      String fg = ch.getKlass().getColorFG();
      sb.append ("    <td align=left bgcolor=" + bg + ">" +
                 "<font color=" + fg + ">" + ch.getKlass() + "</font></td>\n");
      if (ch.isFreep())
         sb.append (Report.getLine (ch.getLevel() + "", null, "center", "white") + "\n");
      sb.append (Report.getLine (ch.getRank() + "", null, "center", "white") + "\n");
      
      sb.append ("  </tr>\n");
      
      return sb.toString();
   }
   
   public void update()
   {
      if (!updateNeeded)
         return;
      
      html.setLength (0);
      // TBD 
      // sorted.addAll (characters);
      // Collections.sort (sorted); // by name
      
      html.append ("<meta http-equiv=\"content-type\" content=\"text-html; charset=utf-8\">\n");
      // TBD: sorttable.js
      html.append ("<table border=\"1\" cellpadding=\"3\" cellspacing=\"1\">\n");
      html.append ("<tbody>\n");

      addHeader();
      
      for (Character ch : model)
         html.append (charHTML.get (ch));

      html.append ("</tbody>\n");
      html.append ("</table>\n");
      
      html.append ("</body>\n");
      html.append ("</html>\n");
      // addFooter (lines, "The data for this chart was provided by mylotro.com.");
      
      try
      {
         htmlPane.setText (html.toString());
         rawPane.setText (html.toString());
      }
      catch (Exception x) 
      {
         System.err.println (x.getMessage());
      }
      
      updateNeeded = false;
   }
   
   private void addHeader()
   {
      // addTitle (lines);
      
      // html.append (getTableTag());
      html.append ("  <thead>");
      html.append ("  <tr bgcolor=yellow>");
      // if (hasPlayers())
      //    html.append ("    <th>Player</th>");
      html.append ("    <th>Name</th>");
      html.append ("    <th>Race</th>");
      html.append ("    <th>Class</th>");
      // if (isFreep)
         html.append ("    <th>Level</th>");
      html.append ("    <th>Kin Rank</th>");
      html.append ("  </tr>");
      html.append ("  </thead>");
   }   
}
