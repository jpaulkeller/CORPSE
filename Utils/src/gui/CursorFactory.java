package gui;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import utils.ImageTools;

public final class CursorFactory
{
   private static final Map<String, Cursor> CURSORS = new HashMap<String, Cursor>();

   private CursorFactory()
   {
      // utility class; prevent instantiation
   }
   
   public static Cursor getCursor (final String name, final Image image,
                                   final Point hotSpot)
   {
      Cursor cursor = CURSORS.get (name);
      if (cursor == null)
      {
         cursor = Toolkit.getDefaultToolkit().createCustomCursor (image, hotSpot, name);
         CURSORS.put (name, cursor);
      }
      return cursor;
   }

   public static Cursor getCursor (final String cursorPath, final Point hotSpot)
   {
      File file = new File (cursorPath);
      Cursor cursor = CURSORS.get (file.getName());
      if (cursor == null)
      {
         ImageIcon icon = ImageTools.getIcon (cursorPath);
         cursor = getCursor (file.getName(), icon.getImage(), hotSpot);
      }
      return cursor;
   }
}
