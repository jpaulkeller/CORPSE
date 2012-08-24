package lotro.my.xml;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Klass;
import lotro.models.Player;
import lotro.models.Race;
import lotro.models.Rank;

public final class KinshipXML
{
   /*
   <apiresponse>
     <guild name="The Palantiri" world="Landroval" theme="Mixed Kinship Theme" memberCount="148">
       <characters>
         <character name="Xyz" level="17" class="Rune-keeper" race="Elf" rank="Recruit"/>
         ...
       </characters>
     </guild>
   </apiresponse>
   */

   private boolean includeDetails;
   private boolean lookupPlayer;
   
   // TBD cache like CharacterXML
   
   public void setLookupPlayer (final boolean lookupPlayer)
   {
      this.lookupPlayer = lookupPlayer;
   }
   
   public void setIncludeDetails (final boolean includeDetails)
   {
      this.includeDetails = includeDetails;
   }
   
   public Kinship scrapeURL (final String world, final String kinName)
   {
      Kinship kinship = new Kinship (kinName);
      kinship.setWorld (world);
      
      int tries = 3;
      while ((tries--) > 0)
      {
         try
         {
            String xml = Scraper.scrapeURL ("guildroster", "w", world, "g", kinName);
            if (xml != null)
            {
               parse (xml, kinship);
               break;
            }
         }
         catch (IOException x)
         {
            if (x.getMessage().contains ("Connection timed out"))
               System.err.println (x.getMessage() + "; tries left: " + tries);
         }
      }
      
      return kinship;
   }

   private void parse (final String xml, final Kinship kinship)
   {
      if (xml != null)
      {
         Map<String, String> guild = Scraper.parseWrapper (xml, "guild");
         if (guild != null)
         {
            kinship.setWorld (guild.get ("world"));
            parseCharacters (guild.get ("contents"), kinship);
         }
      }
   }

   private void parseCharacters (final String xml, final Kinship kinship)
   {
      List<Map<String, String>> chars = Scraper.parseTags (xml, "character");
      for (Map<String, String> props : chars)
      {
         // <character name="Xyz" level="60" class="Captain" race="Race of Man" rank="Leader"/>
         String charName = props.get ("name");
         // System.out.println ("KinshipXML.parseCharacters(): " + charName); // TBD
         if (charName != null)
         {
            Character ch = null;
            if (includeDetails)
               ch = CharacterXML.getCharacter (kinship.getWorld(), charName);
            else
            {
               ch = new Character (charName);
               ch.setWorld (kinship.getWorld());
               try
               {
            	   ch.setLevel (Integer.parseInt (props.get ("level")));
               }
               catch (NumberFormatException x)
               {
            	   System.err.println("Unable to parse level for: " + charName);
               }
               
               ch.setKlass (Klass.parse (props.get ("class")));
               ch.setRace (Race.parse (props.get ("race")));
            }
            
            if (ch != null)
            {
               if (lookupPlayer)
               {
                  Player player = Player.getPlayer (ch.getName());
                  ch.setPlayer (player != null ? player : new Player ("?" + ch.getName()));
               }
               ch.setKinship (kinship.getName());
               ch.setRank (Rank.parse (props.get ("rank")));
               kinship.addCharacter (ch);
            }
         }
      }
   }
   
   public static void main (final String[] args) throws Exception
   {
      KinshipXML xml = new KinshipXML();
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      // Kinship kinship = xml.scrapeURL ("Landroval", "Blades of Anarion");
      // Kinship kinship = xml.scrapeURL ("Landroval", "Knights of the White Lady");
      
      for (Character ch : kinship.getCharacters().values())
         System.out.println (ch.getName() + " (" + ch.getLevel() + " " + ch.getKlass() + ")");
      System.out.println();
   }
}
