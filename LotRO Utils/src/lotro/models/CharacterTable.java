package lotro.models;

import gui.ComponentTools;
import gui.db.TableView;

import java.util.ArrayList;
import java.util.List;

import lotro.my.reports.FilterFactory;
import lotro.my.xml.CharacterXML;
import lotro.my.xml.KinshipXML;
import db.Model;

public class CharacterTable extends Model
{
   public CharacterTable()
   {
      super ("Characters");
      addColumns();
   }
   
   private void addColumns()
   {
      addColumn ("Player");
      addColumn ("Character");
      addColumn ("Race");
      addColumn ("Class");
      addColumn ("Rank");
      addColumn ("Level");
      addColumn ("Vocation");
      
      for (Stats stat : Stats.values())
         addColumn (stat);
      
      // for (Slot slot : Slot.values()) // equipment
      //    addColumn (slot);
   }
   
   public synchronized void addCharacter (final Character ch)
   {
      List<Object> rowData = new ArrayList<Object>();
      rowData.add (ch.getPlayer());
      rowData.add (ch.getName());
      rowData.add (ch.getRace());
      rowData.add (ch.getKlass());
      rowData.add (ch.getRank());
      rowData.add (ch.getLevel());
      rowData.add (ch.getCraft()); // vocation

      for (Stats stat : Stats.values())
         rowData.add (ch.getStat (stat));
      
      // addEquipment (ch, rowData);
      
      for (String prop : ch.getProperties())
         rowData.add (ch.getProp (prop));

      addRow (rowData.toArray());
   }

   private void addEquipment (final Character ch, final List<Object> rowData)
   {
      for (Slot slot : Slot.values()) // equipment
      {
         Equipment equip = ch.getEquipment();
         if (equip != null)
         {
            Item item = equip.getItem (slot);
            if (item != null)
            {
               rowData.add (item.getName());
               continue;
            }
         }
         rowData.add (null);
      }
   }

   public synchronized void removeCharacter (final Character ch)
   {
      int col = findColumn ("Character");
      for (int row = 0; row < getRowCount(); row++)
         if (ch.getName().equals (getValueAt (row, col)))
         {
            removeRow (row);
            break;
         }
   }
   
   public synchronized void updateCharacter (final Character ch)
   {
      int col = findColumn ("Character");
      for (int row = 0; row < getRowCount(); row++)
      {
         if (ch.getName().equals (getValueAt (row, col)))
         {
            setValueAt (ch.getPlayer(), row, findColumn ("Player"));
            setValueAt (ch.getRace(), row, findColumn ("Race"));
            setValueAt (ch.getKlass(), row, findColumn ("Class"));
            setValueAt (ch.getRank(), row, findColumn ("Rank"));
            setValueAt (ch.getLevel(), row, findColumn ("Level"));
            setValueAt (ch.getCraft(), row, findColumn ("Vocation"));

            for (Stats stat : Stats.values())
               setValueAt (ch.getStat (stat), row, findColumn (stat.toString()));
            
            for (String prop : ch.getProperties())
            {
               int propCol = findColumn (prop);
               if (propCol < 0)
               {
                  addColumn (prop);
                  propCol = getColumnCount() - 1;
               }
               setValueAt (ch.getProp (prop), row, propCol);
            }
            break;
         }
      }
   }

   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      
      CharacterTable model = new CharacterTable();
      TableView.show (model);

      KinshipXML xml = new KinshipXML();
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      kinship.setFilter (FilterFactory.getLevelFilter (60));
      for (Character ch : kinship.getCharacters().values())
      {
         CharacterXML.loadCharacter (ch);
         model.addCharacter (ch);
      }
   }
}
