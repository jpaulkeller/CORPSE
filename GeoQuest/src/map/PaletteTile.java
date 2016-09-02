package map;

import java.awt.Color;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.ToolTipManager;

import map.model.Tile;
import map.model.Tiles;

public class PaletteTile extends JButton
{
   private static final long serialVersionUID = 1L;
   private static final Tiles tiles = Tiles.getInstance();

   private DynamicPalette palette; // used to get the dynamic highlight pattern
   private Tile tile;
   private Map<String, Object> properties; // used for the tool-tip table and refining

   public PaletteTile(final DynamicPalette palette, final String relativePath)
   {
      properties = new LinkedHashMap<>();
      putFile(relativePath);

      this.tile = tiles.get(relativePath);
      ImageIcon icon = new ImageIcon(tile.getImage(DynamicPalette.getIconSize()));
      setIcon(icon);

      setBackground(Color.white);
      setMargin(new Insets(0, 0, 0, 0));
      if (palette != null)
         addActionListener(palette.getButtonListener());

      // enable lazy-loaded tool-tips for this component
      this.palette = palette;
      ToolTipManager.sharedInstance().registerComponent(this);
   }

   public Tile getTile()
   {
      return tile;
   }

   public void putFile(final String file)
   {
      put("FILE", file);
   }

   public String getFile()
   {
      return (String) get("FILE");
   }

   public void put(final String key, final Object value)
   {
      properties.put(key, value);
   }

   public Object get(final String key)
   {
      return properties.get(key);
   }

   public Map<String, Object> getProperties()
   {
      return properties;
   }

   public boolean matches(final Pattern pattern)
   {
      for (Object value : properties.values())
         if (value instanceof String && pattern.matcher((String) value).find())
            return true;
      return false;
   }

   @Override
   public String getToolTipText()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("<html>\n");
      sb.append("<table border=1 cellspacing=1 width=400>\n");

      for (Entry<String, Object> entry : properties.entrySet())
         if (entry.getValue() instanceof String && !entry.getValue().equals(""))
         {
            sb.append("<tr>");
            sb.append("<td>" + entry.getKey());
            sb.append("<td>" + hilightPattern((String) entry.getValue()));
            sb.append("</tr>\n");
         }
      sb.append("</table>\n");
      sb.append("</html>");

      return sb.toString();
   }

   // highlight the matching pattern(s)
   private String hilightPattern(final String target)
   {
      Pattern pattern = palette.getHighlightPattern();
      if (pattern != null)
      {
         Matcher m = pattern.matcher(target);
         String replacement = "<b style=\"background-color:yellow\">$0</b>";
         return m.replaceAll(replacement);
      }
      return target;
   }

   @Override
   public String toString()
   {
      return getFile();
   }
}
