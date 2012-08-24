package lotro.my.reports;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import lotro.models.Alliance;
import lotro.models.Character;
import lotro.models.CharacterListModel;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.models.Stats;
import lotro.web.Dropbox;
import file.FileUtils;

public abstract class Report
{
   private static final String LOREBOOK_TIP =
      "http://content.level3.turbine.com/sites/lorebook.lotro.com/js/onering.js";
   private static final String SORT_SCRIPT =
      "http://www.axiomfiles.com/Files/152273/script-sorttable.js";
   
   // http://www.javascriptkit.com/script/script2/htmltooltip.shtml
   private static final String HTML_TIP1 =
      "http://www.axiomfiles.com/Files/152273/script-jquery-122pack.js";
   private static final String HTML_TIP2 =
      "http://www.axiomfiles.com/Files/152273/script-htmltooltip.js";
      
   private static final SimpleDateFormat DMY = new SimpleDateFormat ("dd MMM yyyy");

   private CharacterListModel model;
   private Kinship kinship;
   private String name;
   
   protected Report (final String name, final CharacterListModel model) 
   {
      this.name = name;
      this.model = model;
   }

   protected Report (final Kinship kinship, final String name)
   {
      this.name = name;
      this.kinship = kinship;
   }

   protected Collection<Character> getCharacters()
   {
      if (model != null)
         return model;
      if (kinship != null)
         return kinship.getCharacters().values();
      return null;
   }
   
   // protected Kinship getKinship(final int index)
   
   protected void setName (final String name)
   {
      this.name = name;
   }
   
   protected String getName()
   {
      return name;
   }
   
   protected Kinship getKinship()
   {
      return kinship;
   }
   
   protected boolean isAlliance()
   {
      return kinship instanceof Alliance && ((Alliance) kinship).size() > 1;
   }

   protected boolean hasPlayers()
   {
      if (kinship == null)
         return false;
      Character leader = kinship.getLeader();
      if (leader == null)
         return false;
      Player player = leader.getPlayer();
      if (player == null)
         return false;
      return player.getName().contains (leader.getName());
   }
   
   protected abstract void exportAsHTML (final List<String> lines);
   
   public static String getLink (final Character ch)
   {
      StringBuilder sb = new StringBuilder();
      sb.append ("<a href=\"http://my.lotro.com/character/");
      sb.append (ch.getWorld().toLowerCase() + "/");
      sb.append (ch.getName().toLowerCase() + "/");
      sb.append ("\" target=_blank");
      sb.append (" title=\"" + ch.getKlass() + " (" + ch.getLevel() + ")\""); // tip
      sb.append (">");
      sb.append (ch.getName());
      sb.append ("</a>");
      return sb.toString();
   }
   
   public static String getLine (final Character ch, final Stats attr, final String style)
   {
      return getLine (ch.getStat (attr) + "", style, attr.getAlign(), attr.getColor()); 
   }

   public static String getLine (final String value, final String style, 
                                 final String align, final String bgColor)
   {
      StringBuilder sb = new StringBuilder();
      sb.append ("    <td");
      if (style != null)
         sb.append (" class=" + style);
      if (align != null)
         sb.append (" align=" + align);
      if (bgColor != null)
         sb.append (" bgcolor=" + bgColor);
      sb.append (">");
      sb.append (value);
      sb.append ("</td>");
      return sb.toString();
   }
   
   protected String getTableTag()
   {
      return "<table class=\"sortable\" border=1 cellspacing=1 cellpadding=1>";
   }
   
   protected void addHead (final List<String> lines)
   {
      lines.add ("<html>\n");
      lines.add ("<head>\n");
      addScripts (lines);
      addStyle (lines);
      lines.add ("</head>\n");
   }

   private void addScripts (final List<String> lines)
   {
      lines.add ("<script type=\"text/javascript\" src=\"" + SORT_SCRIPT + "\"></script>");
      lines.add ("<script type=\"text/javascript\" src=\"" + LOREBOOK_TIP + "\"></script>");
      lines.add ("<script type=\"text/javascript\" src=\"" + HTML_TIP1 + "\"></script>");
      lines.add ("<script type=\"text/javascript\" src=\"" + HTML_TIP2 + "\"></script>");
   }

   private void addStyle (final List<String> lines)
   {
      lines.add ("\n<style type=\"text/css\">");
      lines.add (".small {font-size:x-small;}");
      addTooltip (lines);
      lines.add ("</style>\n");
   }

   private void addTooltip (final List<String> lines)
   {
      lines.add ("div.htmltooltip {");
      lines.add ("   position: absolute; z-index: 1000; left: -1000px; top: -1000px;");
      lines.add ("   background: #272727;");
      lines.add ("   border: 10px solid black;");
      lines.add ("   color: white;");
      lines.add ("   padding: 3px;");
      lines.add ("   width: 250px;"); // pop-up panel width
      lines.add ("}");
   }

   protected void addTitle (final List<String> lines)
   {
      if (kinship == null)
         return;
      String world = kinship.getWorld().toLowerCase();

      String link;
      if (isAlliance())
         link = "<a target=\"_blank\" href=\"http://allies2.guildlaunch.com\">Alliance</a> ";
      else
      {
         String kin = kinship.getName().toLowerCase().replace (' ', '_'); 
         String url = "http://my.lotro.com/kinship-" + world + "-" + kin + "/";
         link = "<a target=\"_blank\" href=\"" + url + "\">" + kinship.getName() + "</a> ";
      }
      
      int size = kinship.size (true, false);
            
      // Use a header tag since SortTable.js doesn't support multi-row headers
      StringBuilder sb = new StringBuilder();
      sb.append ("<h3 align=center>");
      sb.append (link);
      sb.append (name);
      sb.append ("&nbsp;-&nbsp;" + size + " characters");
      sb.append ("&nbsp;&nbsp;(updated " + DMY.format (new Date()) + ")");
      sb.append ("</h3>");
      lines.add (sb.toString());
   }
   
   protected void addFooter (final List<String> lines, final String footer)
   {
      lines.add ("<p><em>" + footer + "</em></p>");
      lines.add ("<hr>");
      lines.add
         ("Comments or questions? Please " +
          "<a href=\"mailto:mosby.palantiri@gmail.com?subject=" + name + "\">contact Mosby</a> of " +
          "<a href=\"http://my.lotro.com/kinship-landroval-the_palantiri/\" target=\"_blank\">The Palantiri</a>" +
         " (Landroval)\n");
   }
   
   public List<String> getLines()
   {
      List<String> lines = new ArrayList<String>();
      addHead (lines);
      
      lines.add ("<body>\n");
      lines.add ("<center>\n");
      exportAsHTML (lines);
      
      lines.add ("</center>\n");
      lines.add ("</body>");
      lines.add ("</html>");

      return lines;
   }
   
   // Total should normally be kinship.sizeFiltered() unless the filter is
   // based on detailed data, in which case all characters need to be scraped.
   // If so, the total needs to be the total size of the kinship.
   
   public List<String> getLinesAJAX (final String url, final int delaySeconds)
   {
      List<String> lines = new ArrayList<String>();

      lines.add ("<head>\n");
      addScripts (lines);

      // add refresh script
      lines.add ("<script type=\"text/javascript\">");
      lines.add ("<!--");
      lines.add ("function refresher() {");
      // String unique = url + "&key=" + UUID.randomUUID(); // TBD
      lines.add ("   document.location = \"" + url + "\"");
      lines.add ("}");
      lines.add ("//-->");
      lines.add ("</script>");
      
      addStyle (lines);
      lines.add ("</head>\n");

      int ms = delaySeconds * 1000;
      lines.add ("<body onLoad=\"setTimeout ('refresher()', " + ms + ")\">\n");

      lines.add ("<font color=red>");
      lines.add ("<b>" + kinship.size (false, true) + " of " + 
                 kinship.size (false, false) + "</b> characters loaded...<br/>");
      lines.add ("Note: this page will automatically refresh as more data " +
                 "becomes available. This may take several minutes for " +
                 "large kinships.");
      lines.add ("</font>");
      lines.add ("<hr>\n");
      
      lines.add ("<center>\n");
      exportAsHTML (lines);
      lines.add ("</center>\n");
      
      lines.add ("</body>");
      lines.add ("</html>");

      return lines;
   }
   
   protected void saveFile()
   {
      List<String> lines = getLines();
      String path = Dropbox.get().getPath ("/charts/" + kinship.getName() + 
                                           " " + getName() + ".html");
      FileUtils.writeList (lines, path, false);
      System.out.println ("Output: " + path);
   }
}
