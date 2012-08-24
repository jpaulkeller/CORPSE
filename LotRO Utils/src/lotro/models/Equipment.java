package lotro.models;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lotro.my.xml.Scraper;

public class Equipment
{
   /*
    URL = http://lorebook.lotro.com/wiki/Special:LotroResource?id=
    <equipment>
      <item name="Hat of the Elder Days" slot="Head" lorebookEntry="URL=1879094671" />
    </equipment> 
   */
   private Map<Slot, Item> items = new TreeMap<Slot, Item>();
   
   public Equipment()
   {
   }
   
   public Equipment (final String xml)
   {
      List<Map<String, String>> itemTags = Scraper.parseTags (xml, "item");
      for (Map<String, String> itemMap : itemTags)
         putItem (new Item (itemMap));
   }

   public Item getItem (final Slot slot)
   {
      return items.get (slot);
   }
   
   public void putItem (final Item item)
   {
      if (item.getSlot() != null)
         items.put (item.getSlot(), item);
   }
}
