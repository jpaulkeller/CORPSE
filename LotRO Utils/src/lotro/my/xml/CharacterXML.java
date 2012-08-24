package lotro.my.xml;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lotro.models.Character;
import lotro.models.Craft;
import lotro.models.Equipment;
import lotro.models.Klass;
import lotro.models.Profession;
import lotro.models.Race;
import lotro.models.Rank;
import lotro.models.Stats;
import lotro.models.Vocation;

public final class CharacterXML
{
   /*
  <apiresponse>

  <character name="Valdidar" world="Landroval" monster="0" race="Race of Man" class="Lore-master" level="53" origin="Gondor">
    <guild name="Knights of the White Lady" rank="Member" type="Mixed Kinship Theme" />

    <vocation name="Armsman">
      <professions>
        <profession name="Weaponsmith" proficiency="5" mastery="5" />
        <profession name="Woodworker" proficiency="4" mastery="3" />
        <profession name="Prospector" proficiency="6" mastery="5" />
      </professions>
    </vocation>

    <pvmp ratingPoints="1111.44" gloryPoints="4789" gloryRank="3" />

    <stats>
      <stat name="morale" value="2996" />
      <stat name="power" value="2206" />
      ...
    </stats>
 
    <equipment>
      <item name="Hat of the Elder Days" slot="Head" lorebookEntry="http://lorebook.lotro.com/wiki/Special:LotroResource?id=1879094671" />
      ...
    </equipment>

  </character>
  </apiresponse> 
   */
   
   /*
<apiresponse>

<character name="Lognu" world="Landroval" monster="1" race="Spider" class="Weaver" level="60">
<guild name="Creepshow" rank="Officer" type="Mixed Tribe Theme"/>
<pvmp ratingPoints="1184.6" gloryPoints="6840" gloryRank="4"/>

<stats>
<stat name="morale" value="4701"/>
<stat name="power" value="1898"/>
<stat name="armour" value="4487"/>
<stat name="block" value="1288"/>
<stat name="evade" value="2172"/>
<stat name="parry" value="1267"/>
<stat name="meleeCrit" value="1256"/>
<stat name="rangedCrit" value="2061"/>
<stat name="tacticalCrit" value="729"/>
<stat name="commonDef" value="3805"/>
<stat name="fireDef" value="3300"/>
<stat name="frostDef" value="2986"/>
<stat name="lightningDef" value="2850"/>
</stats>
</character>
</apiresponse>     
    */
   
   private static Map<String, Character> characters = new TreeMap<String, Character>();
   
   private CharacterXML() { }
   
   public static Character getCharacter (final String world, final String name)
   {
      String uniqueKey = world + "-" + name;
      Character ch = characters.get (uniqueKey);
      if (ch == null)
      {
         ch = scrapeURL (world, name);
         ch.setWorld (world);
         characters.put (uniqueKey, ch);
      }
      return ch; 
   }

   public static void loadCharacter (final Character ch)
   {
      String uniqueKey = ch.getWorld() + "-" + ch.getName();
      if (characters.get (uniqueKey) == null)
      {
         scrapeURL (ch.getWorld(), ch);
         characters.put (uniqueKey, ch);
      }
   }

   private static Character scrapeURL (final String world, final String charName)
   {
      Character ch = new Character (charName);
      scrapeURL (world, ch);
      return ch;
   }
   
   private static void scrapeURL (final String world, final Character ch)
   {
      ch.setWorld (world);

      int tries = 3;
      while ((tries--) > 0)
      {
         try
         {
            String xml = Scraper.scrapeURL ("charactersheet", "w", world, "c", ch.getName());
            if (xml != null)
            {
               parse (xml, ch);
               ch.setScraped (new Date());
               break;
            }
         }
         catch (IOException x)
         {
            if (x.getMessage().contains ("Connection timed out"))
               System.err.println (x.getMessage() + "; tries left: " + tries);
         }
      }
   }
   
   private static void parse (final String charSheet, final Character ch)
   {
      Map<String, String> attr = Scraper.parseWrapper (charSheet, "character");
      if (attr != null)
      {
         ch.setRace (Race.parse (attr.get ("race")));
         ch.setKlass (Klass.parse (attr.get ("class")));
         ch.setLevel (parseInt (ch, "level", attr.get ("level")));
         
         boolean isFreep = !attr.get ("monster").equals ("1");
         if (isFreep)
            ch.putProp ("Origin", attr.get ("origin"));
         
         String xml = attr.get ("contents"); 
         if (xml != null)
         {
            try
            {
               parseKin (xml, ch);
               parsePvMP (xml, ch);
               parseStats (xml, ch);
               if (isFreep)
               {
                  parseGear (xml, ch);
                  parseVocation (xml, ch);
               }
            }
            catch (Exception x)
            {
               x.printStackTrace (System.err);
               System.err.println (xml);
            }
         }
      }
   }

   private static void parseKin (final String xml, final Character ch)
   {
      // <guild name="Knights of the White Lady" rank="Member" type="Mixed Kinship Theme" />
      Map<String, String> kinInfo = Scraper.parseWrapper (xml, "guild");
      if (kinInfo != null)
      {
    	  ch.setKinship (kinInfo.get ("name"));
    	  ch.setRank (Rank.parse (kinInfo.get ("rank")));
      }
      else
    	  System.err.println("Missing <guild> tag for: " + ch);
   }

   private static void parsePvMP (final String xml, final Character ch)
   {
      // <pvmp ratingPoints="1111.44" gloryPoints="4789" gloryRank="3" />
      Map<String, String> pvmp = Scraper.parseWrapper (xml, "pvmp");
      ch.putProp ("PvMP Rank", pvmp.get ("gloryRank"));
      if (!ch.isFreep())
      {
         ch.putProp ("Rating", pvmp.get ("ratingPoints"));
         ch.putProp ("Glory", pvmp.get ("gloryPoints"));
      }
   }

   private static void parseStats (final String xml, final Character ch)
   {
      // <stat name="morale" value="2996" />
      List<Map<String, String>> stats = Scraper.parseTags (xml, "stat");
      for (Map<String, String> stat : stats)
      {
         int value = parseInt (ch, stat.get ("name"), stat.get ("value"));   
         ch.putStat (Stats.parseTag (stat.get ("name")), value);
      }
   }

   private static void parseGear (final String xml, final Character ch)
   {
      Map<String, String> allEquipment = Scraper.parseWrapper (xml, "equipment");
      if (allEquipment != null)
      {
         String eqXML = allEquipment.get ("contents");
         if (eqXML != null)
            ch.setEquipment (new Equipment (eqXML));
      }
   }
   
   private static void parseVocation (final String xml, final Character ch)
   {
      Map<String, String> vocation = Scraper.parseWrapper (xml, "vocation");
      if (vocation != null)
      {
         Vocation voc = Vocation.parse (vocation.get ("name"));
         Craft craft = new Craft (voc);
         
         // <profession name="Woodworker" proficiency="4" mastery="3" />
         List<Map<String, String>> profs = Scraper.parseTags (xml, "profession");
         for (Map<String, String> prof : profs)
         {
            Profession p = Profession.parse (prof.get ("name"));
            craft.setProficiency (p, parseInt (ch, voc + " proficiency", 
                                               prof.get ("proficiency")));
            craft.setMastery (p, parseInt (ch, voc + " mastery", prof.get ("mastery")));
         }
         
         ch.setCraft (craft);
      }
   }
   
   private static int parseInt (final Character ch, final String field, final String s)
   {
      int i = 0;
      if (s != null && !s.isEmpty())
      {
         try
         {
            i = Integer.parseInt (s);
         }
         catch (NumberFormatException x)
         {
            if (!"N/A".equalsIgnoreCase (s) && !"??".equalsIgnoreCase (s) && !field.equals("radiance"))
               System.out.println ("Non-numeric for " + ch + " " + field + ": " + s);
         }
      }
      return i;
   }
   
   public static void main (final String[] args) throws Exception
   {
      Character ch = CharacterXML.getCharacter ("Landroval", "Lognu");
      System.out.println (ch.getName() + " of " + ch.getWorld() + 
                          " (" + ch.getLevel() + " " + ch.getKlass() + ")");
   }
}
