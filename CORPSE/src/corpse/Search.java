package corpse;

import java.awt.Component;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import utils.ImageTools;
import corpse.ui.TabPane;

public final class Search
{
   private static final Icon SEARCH_ICON = ImageTools.getIcon("icons/20/objects/Magnify.gif");
   
   private Search()
   {
      // prevent instantiation
   }

   public static void search(final Component owner, final TabPane tabs)
   {
      String message = "Enter Search Pattern";
      String title = "Search Data Files";
      int type = JOptionPane.QUESTION_MESSAGE;
      Icon icon = ImageTools.getIcon("20/objects/Magnify.gif");
      String defaultValue = null;

      String pattern = (String) JOptionPane.showInputDialog(owner, message, title, type, icon, null, defaultValue);
      if (pattern != null && !pattern.isEmpty())
      {
         StringBuilder html = search(pattern);
         showSearchResults(tabs, html, pattern);
         // FileUtils.writeFile(new File("C:/Users/J/Desktop/test.html"), html.toString(), false);
      }
   }

   private static StringBuilder search(final String pattern)
   {
      StringBuilder html = new StringBuilder();
      html.append("<meta http-equiv=\"content-type\" content=\"text-html; charset=utf-8\">\n");
      html.append("<html>\n");
      html.append("<body>\n\n");

      // not supported by java's HTML
      /*
       * html.append ("<style type=\"text/css\">\n"); html.append ("em.match { font-style: normal; background-color: yellow }\n");
       * html.append ("</style>\n\b");
       */

      html.append("<h2>Search: " + pattern + "</h2>\n");

      Pattern p = Pattern.compile("(" + Pattern.quote(pattern) + ")", Pattern.CASE_INSENSITIVE);
      String upper = pattern.toUpperCase();

      searchTables(html, p, upper);
      searchScripts(html, p, upper);

      html.append("</body>\n");
      html.append("</html>\n");

      return html;
   }

   private static void searchTables(final StringBuilder html, final Pattern p, final String upper)
   {
      for (Table table : Table.getTables())
      {
         if ("DEPENDS/ERRORS/SAMPLES".contains(table.getName().toUpperCase()))
            continue;

         if (table.getName().toUpperCase().contains(upper))
            html.append("<h3>" + hilight(p, table.getName()) + " (" + table.getFile() + ")</h3>\n");

         List<String> matches = table.search(upper);
         if (!matches.isEmpty())
         {
            if (!table.getName().toUpperCase().contains(upper))
               html.append("<h3>" + table.getName() + " (" + table.getFile() + ")</h3>\n");
            html.append("<ul>\n");
            for (String match : matches)
               html.append(" <li>" + hilight(p, match) + "</li>\n");
            html.append("</ul>\n\n");
         }
      }
   }

   private static void searchScripts(final StringBuilder html, final Pattern p, final String upper)
   {
      for (Script script : Script.SCRIPTS.values())
      {
         if (script.getName().toUpperCase().contains(upper))
            html.append("<h3>" + hilight(p, script.getName()) + " (" + script.getFile() + ")</h3>\n");

         List<String> matches = script.search(upper);
         if (!matches.isEmpty())
         {
            if (!script.getName().toUpperCase().contains(upper))
               html.append("<h3>" + script.getName() + " (" + script.getFile() + ")</h3>\n");
            html.append("<ul>\n");
            for (String match : matches)
               html.append(" <li>" + hilight(p, match) + "</li>\n");
            html.append("</ul>\n\n");
         }
      }
   }

   private static String hilight(final Pattern pattern, final String text)
   {
      Matcher m = pattern.matcher(text);
      // return m.replaceAll("<em class=match>$1</em>"); // not supported by java's HTML
      return m.replaceAll("<font bgcolor=yellow>$1</font>");
   }

   private static void showSearchResults(final TabPane tabs, final StringBuilder html, final String pattern)
   {
      try
      {
         JTextPane htmlPane = new JTextPane();
         htmlPane.setBackground(null);
         htmlPane.setEditable(false);
         htmlPane.setEditorKit(new HTMLEditorKit());
         htmlPane.setText(html.toString());
         htmlPane.setCaretPosition(0);

         Component searchResults = new JScrollPane(htmlPane);
         tabs.addToggleTab(pattern, SEARCH_ICON, searchResults, "Search results for: " + pattern);

         int index = tabs.indexOfTab(pattern);
         tabs.setSelectedIndex(index);

      }
      catch (Exception x)
      {
         System.err.println(x.getMessage());
      }
   }
}
