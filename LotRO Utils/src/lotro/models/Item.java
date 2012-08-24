package lotro.models;

import file.FileUtils;
import gui.ComponentTools;
import gui.comp.FileChooser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

public class Item
{
   private static final Map<Long, Integer> RADIANCE_GEAR = new HashMap<Long, Integer> ();
   static
   {
      RADIANCE_GEAR.put (1879174213L, 10); // Armour:Armour_of_the_Nameless
      RADIANCE_GEAR.put (1879151823L, 15); // Armour:Arrow-fletcher's_Gauntlets
      RADIANCE_GEAR.put (1879151824L, 15); // Armour:Arrow-fletcher's_Jacket
      RADIANCE_GEAR.put (1879151837L, 15); // Armour:Arrow-fletcher's_Leggings
      RADIANCE_GEAR.put (1879148769L, 10); // Armour:Arthranc
      RADIANCE_GEAR.put (1879148766L, 10); // Armour:Baladhranc
      RADIANCE_GEAR.put (1879158538L, 30); // Armour:Battle-leader's_Boots
      RADIANCE_GEAR.put (1879158546L, 30); // Armour:Battle-leader's_Breastplate
      RADIANCE_GEAR.put (1879158588L, 30); // Armour:Battle-leader's_Gauntlets
      RADIANCE_GEAR.put (1879158562L, 30); // Armour:Battle-leader's_Helm
      RADIANCE_GEAR.put (1879158563L, 30); // Armour:Battle-leader's_Leggings
      RADIANCE_GEAR.put (1879158600L, 30); // Armour:Battle-leader's_Shoulders
      RADIANCE_GEAR.put (1879148757L, 10); // Armour:Beleghar
      RADIANCE_GEAR.put (1879151861L, 25); // Armour:Berserker's_Boots
      RADIANCE_GEAR.put (1879151869L, 25); // Armour:Berserker's_Helm
      RADIANCE_GEAR.put (1879151832L, 25); // Armour:Berserker's_Shoulders
      RADIANCE_GEAR.put (1879112390L, 10); // Armour:Blademaster's_Boots
      RADIANCE_GEAR.put (1879112195L, 10); // Armour:Blademaster's_Breastplate
      RADIANCE_GEAR.put (1879112249L, 10); // Armour:Blademaster's_Gauntlets
      RADIANCE_GEAR.put (1879112285L, 20); // Armour:Blademaster's_Helm
      RADIANCE_GEAR.put (1879112320L, 10); // Armour:Blademaster's_Leggings
      RADIANCE_GEAR.put (1879112356L, 20); // Armour:Blademaster's_Shoulders
      RADIANCE_GEAR.put (1879151863L, 25); // Armour:Boots_of_the_Swift_Arrow
      RADIANCE_GEAR.put (1879112391L, 10); // Armour:Boots_of_Durin's_Guard
      RADIANCE_GEAR.put (1879112402L, 10); // Armour:Boots_of_the_Great_Bow
      RADIANCE_GEAR.put (1879151835L, 25); // Armour:Boots_of_the_High_Captain
      RADIANCE_GEAR.put (1879112392L, 20); // Armour:Boots_of_the_Lady's_Courage
      RADIANCE_GEAR.put (1879112394L, 20); // Armour:Boots_of_the_Lady's_Defense
      RADIANCE_GEAR.put (1879112405L, 20); // Armour:Boots_of_the_Lady's_Discernment
      RADIANCE_GEAR.put (1879112406L, 20); // Armour:Boots_of_the_Lady's_Favour
      RADIANCE_GEAR.put (1879112393L, 20); // Armour:Boots_of_the_Lady's_Power
      RADIANCE_GEAR.put (1879112404L, 20); // Armour:Boots_of_the_Lady's_Secrecy
      RADIANCE_GEAR.put (1879158569L, 30); // Armour:Boots_of_the_Shadow-walker
      RADIANCE_GEAR.put (1879112401L, 10); // Armour:Boots_of_the_Silent_Knife
      RADIANCE_GEAR.put (1879151831L, 25); // Armour:Boots_of_the_Strong_Defender
      RADIANCE_GEAR.put (1879158603L, 30); // Armour:Boots_of_the_Wall-warden
      RADIANCE_GEAR.put (1879112196L, 10); // Armour:Breastplate_of_Durin's_Guard
      RADIANCE_GEAR.put (1879158549L, 30); // Armour:Breastplae_of_the_Deathstorm
      RADIANCE_GEAR.put (1879112197L, 20); // Armour:Breastplae_of_the_Lady's_Courage
      RADIANCE_GEAR.put (1879112199L, 20); // Armour:Breastplae_of_the_Lady's_Defence
      RADIANCE_GEAR.put (1879112198L, 20); // Armour:Breastplae_of_the_Lady's_Power
      RADIANCE_GEAR.put (1879143197L, 10); // Armour:Brenin-crus
      RADIANCE_GEAR.put (1879148762L, 10); // Armour:Curuchar
      RADIANCE_GEAR.put (1879148761L, 10); // Armour:Curuthol
      RADIANCE_GEAR.put (1879148764L, 10); // Armour:Dagoranc
      RADIANCE_GEAR.put (1879151854L, 15); // Armour:Deft-blade's_Gauntlets
      RADIANCE_GEAR.put (1879151843L, 15); // Armour:Deft-blade's_Jacket
      RADIANCE_GEAR.put (1879151851L, 15); // Armour:Deft-blade's_Leggings
      
      RADIANCE_GEAR.put (1879151857L, 25); // Armour:Dungeon-crawler's_Hood
      RADIANCE_GEAR.put (1879151834L, 25); // Armour:Dungeon-crawler's_Shoulders
      RADIANCE_GEAR.put (1879158553L, 30); // Armour:Far-arrow's_Boots
      RADIANCE_GEAR.put (1879158593L, 30); // Armour:Far-arrow's_Gauntlets
      RADIANCE_GEAR.put (1879158556L, 30); // Armour:Far-arrow's_Helm
      RADIANCE_GEAR.put (1879158595L, 30); // Armour:Far-arrow's_Jacket
      RADIANCE_GEAR.put (1879158558L, 30); // Armour:Far-arrow's_Leggings
      RADIANCE_GEAR.put (1879158552L, 30); // Armour:Far-arrow's_Shoulders
      RADIANCE_GEAR.put (1879112250L, 10); // Armour:Gauntlets_of_Durin's_Guard
      RADIANCE_GEAR.put (1879158585L, 30); // Armour:Gauntlets_of_the_Deathstorm
      RADIANCE_GEAR.put (1879112273L, 10); // Armour:Gauntlets_of_the_Great_Bow
      RADIANCE_GEAR.put (1879151862L, 15); // Armour:Gauntlets_of_the_Javelin
      RADIANCE_GEAR.put (1879112251L, 20); // Armour:Gauntlets_of_the_Lady's_Courage
      RADIANCE_GEAR.put (1879112253L, 20); // Armour:Gauntlets_of_the_Lady's_Defence
      RADIANCE_GEAR.put (1879112276L, 20); // Armour:Gauntlets_of_the_Lady's_Discernment
      RADIANCE_GEAR.put (1879112277L, 20); // Armour:Gauntlets_of_the_Lady's_Favour
      RADIANCE_GEAR.put (1879112252L, 20); // Armour:Gauntlets_of_the_Lady's_Power
      RADIANCE_GEAR.put (1879112275L, 20); // Armour:Gauntlets_of_the_Lady's_Secrecy
      RADIANCE_GEAR.put (1879158540L, 30); // Armour:Gauntlets_of_the_Shadow-walker
      RADIANCE_GEAR.put (1879112272L, 10); // Armour:Gauntlets_of_the_Silent_Knife
      RADIANCE_GEAR.put (1879158587L, 30); // Armour:Gauntlets_of_the_Wall-warden
      RADIANCE_GEAR.put (1879112265L, 20); // Armour:Gloves_of_the_Lady's_Foresight
      RADIANCE_GEAR.put (1879112264L, 20); // Armour:Gloves_of_the_Lady's_Grace
      RADIANCE_GEAR.put (1879112263L, 20); // Armour:Gloves_of_the_Lady's_Wisdom
      RADIANCE_GEAR.put (1879112261L, 10); // Armour:Gloves_of_the_Mighty_Verse
      RADIANCE_GEAR.put (1879158551L, 30); // Armour:Gloves_of_the_Silver_Voice
      RADIANCE_GEAR.put (1879151867L, 15); // Armour:Gloves_of_the_Stone-reader's_Apprentice
      RADIANCE_GEAR.put (1879151844L, 15); // Armour:Hall-captain's_Breastplate
      RADIANCE_GEAR.put (1879151853L, 15); // Armour:Hall-captain's_Gauntlets
      RADIANCE_GEAR.put (1879151815L, 15); // Armour:Hall-captain's_Leggings
      RADIANCE_GEAR.put (1879112389L, 10); // Armour:Hall-general's_Boots
      RADIANCE_GEAR.put (1879112194L, 10); // Armour:Hall-general's_Breastplate
      RADIANCE_GEAR.put (1879112248L, 10); // Armour:Hall-general's_Gauntlets
      RADIANCE_GEAR.put (1879112284L, 20); // Armour:Hall-general's_Helm
      RADIANCE_GEAR.put (1879112319L, 10); // Armour:Hall-general's_Leggings
      RADIANCE_GEAR.put (1879112355L, 20); // Armour:Hall-general's_Shoulders
      RADIANCE_GEAR.put (1879148770L, 10); // Armour:Hallanc
      RADIANCE_GEAR.put (1879174652L, 25); // Armour:Harma-Barad
      RADIANCE_GEAR.put (1879151846L, 25); // Armour:Hat_of_the_Ancient_Tongue
      RADIANCE_GEAR.put (1879112301L, 20); // Armour:Hat_of_the_Lady's_Foresight
      RADIANCE_GEAR.put (1879112300L, 20); // Armour:Hat_of_the_Lady's_Grace
      RADIANCE_GEAR.put (1879112299L, 20); // Armour:Hat_of_the_Lady's_Wisdom
      RADIANCE_GEAR.put (1879151818L, 25); // Armour:Hat_of_the_Learned_Master
      RADIANCE_GEAR.put (1879112297L, 20); // Armour:Hat_of_the_Mighty_Verse
      RADIANCE_GEAR.put (1879158571L, 30); // Armour:Hat_of_the_Silver_Voice
      RADIANCE_GEAR.put (1879112286L, 20); // Armour:Helm_of_Durin's_Guard
      RADIANCE_GEAR.put (1879158582L, 30); // Armour:Helm_of_the_Deathstorm
      RADIANCE_GEAR.put (1879112309L, 20); // Armour:Helm_of_the_Great_Bow
      RADIANCE_GEAR.put (1879151816L, 25); // Armour:Helm_of_the_High_Captain
      RADIANCE_GEAR.put (1879112287L, 20); // Armour:Helm_of_the_Lady's_Courage
      
      RADIANCE_GEAR.put (1879112289L, 20); // Armour:Helm_of_the_Lady's_Defence
      RADIANCE_GEAR.put (1879112311L, 20); // Armour:Helm_of_the_Lady's_Discernment
      RADIANCE_GEAR.put (1879112312L, 20); // Armour:Helm_of_the_Lady's_Favour
      RADIANCE_GEAR.put (1879112288L, 20); // Armour:Helm_of_the_Lady's_Power
      RADIANCE_GEAR.put (1879112310L, 20); // Armour:Helm_of_the_Lady's_Secrecy
      RADIANCE_GEAR.put (1879158599L, 30); // Armour:Helm_of_the_Shadow-walker
      RADIANCE_GEAR.put (1879151858L, 25); // Armour:Helm_of_the_Strong_Defender
      RADIANCE_GEAR.put (1879151864L, 25); // Armour:Helm_of_the_Swift_Arrow
      RADIANCE_GEAR.put (1879158568L, 30); // Armour:Helm_of_the_Wall-warden
      RADIANCE_GEAR.put (1879143200L, 10); // Armour:Heremaib
      RADIANCE_GEAR.put (1879143201L, 10); // Armour:Herengaim
      RADIANCE_GEAR.put (1879148755L, 10); // Armour:Heronhar
      RADIANCE_GEAR.put (1879151821L, 25); // Armour:High-warden's_Boots
      RADIANCE_GEAR.put (1879151827L, 25); // Armour:High-warden's_Helm
      RADIANCE_GEAR.put (1879151849L, 25); // Armour:High-warden's_Shoulders
      RADIANCE_GEAR.put (1879112308L, 20); // Armour:Hood_of_the_Silent_Knife
      RADIANCE_GEAR.put (1879148767L, 10); // Armour:Isduranc
      RADIANCE_GEAR.put (1879148768L, 10); // Armour:Ithrodhranc
      RADIANCE_GEAR.put (1879112219L, 10); // Armour:Jacket_of_the_Great_Bow
      RADIANCE_GEAR.put (1879151811L, 15); // Armour:Jacket_of_the_Javelin
      RADIANCE_GEAR.put (1879112222L, 20); // Armour:Jacket_of_the_Lady's_Discernment
      RADIANCE_GEAR.put (1879112223L, 20); // Armour:Jacket_of_the_Lady's_Favour
      RADIANCE_GEAR.put (1879112221L, 20); // Armour:Jacket_of_the_Lady's_Secrecy
      RADIANCE_GEAR.put (1879158583L, 30); // Armour:Jacket_of_the_Shadow-walker
      RADIANCE_GEAR.put (1879112218L, 10); // Armour:Jacket_of_the_Silent_Knife
      RADIANCE_GEAR.put (1879158548L, 30); // Armour:Jacket_of_the_Wall-warden
      RADIANCE_GEAR.put (1879143198L, 10); // Armour:Jofur-klath
      RADIANCE_GEAR.put (1879112321L, 10); // Armour:Leggings_of_Durin's_Guard
      RADIANCE_GEAR.put (1879158537L, 30); // Armour:Leggings_of_the_Deathstorm
      RADIANCE_GEAR.put (1879112344L, 10); // Armour:Leggings_of_the_Great_Bow
      RADIANCE_GEAR.put (1879151833L, 15); // Armour:Leggings_of_the_Javelin
      RADIANCE_GEAR.put (1879112322L, 20); // Armour:Leggings_of_the_Lady's_Courage
      RADIANCE_GEAR.put (1879112324L, 20); // Armour:Leggings_of_the_Lady's_Defence
      RADIANCE_GEAR.put (1879112347L, 20); // Armour:Leggings_of_the_Lady's_Discernment
      RADIANCE_GEAR.put (1879112348L, 20); // Armour:Leggings_of_the_Lady's_Favour
      RADIANCE_GEAR.put (1879112323L, 20); // Armour:Leggings_of_the_Lady's_Power
      RADIANCE_GEAR.put (1879112346L, 20); // Armour:Leggings_of_the_Lady's_Secrecy
      RADIANCE_GEAR.put (1879158579L, 30); // Armour:Leggings_of_the_Shadow-walker
      RADIANCE_GEAR.put (1879112343L, 10); // Armour:Leggings_of_the_Silent_Knife
      RADIANCE_GEAR.put (1879158591L, 30); // Armour:Leggings_of_the_Wall-warden
      RADIANCE_GEAR.put (1879148771L, 10); // Armour:Longranc
      RADIANCE_GEAR.put (1879158542L, 30); // Armour:Lore-keeper's_Gloves
      RADIANCE_GEAR.put (1879158584L, 30); // Armour:Lore-keeper's_Hat
      RADIANCE_GEAR.put (1879158564L, 30); // Armour:Lore-keeper's_Robe
      RADIANCE_GEAR.put (1879158580L, 30); // Armour:Lore-keeper's_Shoes
      RADIANCE_GEAR.put (1879158535L, 30); // Armour:Lore-keeper's_Shoulders
      RADIANCE_GEAR.put (1879158570L, 30); // Armour:Lore-keeper's_Trousers
      RADIANCE_GEAR.put (1879148765L, 10); // Armour:Maethranc
      RADIANCE_GEAR.put (1879143199L, 10); // Armour:Malak-zudur
      RADIANCE_GEAR.put (1879148759L, 10); // Armour:Manathar

      RADIANCE_GEAR.put (1879174212L, 10); // Armour:Prized_Guards_of_Skumfil
      RADIANCE_GEAR.put (1879151859L, 15); // Armour:Rhymer's_Gloves
      RADIANCE_GEAR.put (1879151825L, 15); // Armour:Rhymer's_Robe
      RADIANCE_GEAR.put (1879151808L, 15); // Armour:Rhymer's_Trousers
      RADIANCE_GEAR.put (1879148756L, 10); // Armour:R?anhar
      RADIANCE_GEAR.put (1879143202L, 10); // Armour:R?engaim
      RADIANCE_GEAR.put (1879112211L, 20); // Armour:Robe_of_the_Lady's_Foresight
      RADIANCE_GEAR.put (1879112210L, 20); // Armour:Robe_of_the_Lady's_Grace
      RADIANCE_GEAR.put (1879112209L, 20); // Armour:Robe_of_the_Lady's_Wisdom
      RADIANCE_GEAR.put (1879158554L, 30); // Armour:Robe_of_the_Silver_Voice
      RADIANCE_GEAR.put (1879151868L, 15); // Armour:Robe_of_the_Stone-reader's_Apprentice
      RADIANCE_GEAR.put (1879174642L, 25); // Armour:Rochben
      RADIANCE_GEAR.put (1879148754L, 10); // Armour:Rodonhar
      RADIANCE_GEAR.put (1879151866L, 15); // Armour:Rune-learner's_Gloves
      RADIANCE_GEAR.put (1879151817L, 15); // Armour:Rune-learner's_Robe
      RADIANCE_GEAR.put (1879151819L, 15); // Armour:Rune-learner's_Trousers
      RADIANCE_GEAR.put (1879158602L, 30); // Armour:Runemaker's_Gloves
      RADIANCE_GEAR.put (1879158592L, 30); // Armour:Runemaker's_Hat
      RADIANCE_GEAR.put (1879158555L, 30); // Armour:Runemaker's_Robe
      RADIANCE_GEAR.put (1879158545L, 30); // Armour:Runemaker's_Shoes
      RADIANCE_GEAR.put (1879158590L, 30); // Armour:Runemaker's_Shoulders
      RADIANCE_GEAR.put (1879158574L, 30); // Armour:Runemaker's_Trousers
      RADIANCE_GEAR.put (1879148758L, 10); // Armour:Saelhar
      RADIANCE_GEAR.put (1879158577L, 30); // Armour:Shield-master's_Boots
      RADIANCE_GEAR.put (1879158596L, 30); // Armour:Shield-master's_Breastplate
      RADIANCE_GEAR.put (1879158572L, 30); // Armour:Shield-master's_Gauntlets
      RADIANCE_GEAR.put (1879158534L, 30); // Armour:Shield-master's_Helm
      RADIANCE_GEAR.put (1879158559L, 30); // Armour:Shield-master's_Leggings
      RADIANCE_GEAR.put (1879158578L, 30); // Armour:Shield-master's_Shoulders
      RADIANCE_GEAR.put (1879112207L, 10); // Armour:Shirt_of_the_Mighty_Verse
      RADIANCE_GEAR.put (1879151814L, 25); // Armour:Shoes_of_the_Ancient_Tongue
      RADIANCE_GEAR.put (1879112400L, 20); // Armour:Shoes_of_the_Lady's_Foresight
      RADIANCE_GEAR.put (1879112399L, 20); // Armour:Shoes_of_the_Lady's_Grace
      RADIANCE_GEAR.put (1879112398L, 20); // Armour:Shoes_of_the_Lady's_Wisdom
      RADIANCE_GEAR.put (1879151841L, 25); // Armour:Shoes_of_the_Learned_Master
      RADIANCE_GEAR.put (1879112396L, 10); // Armour:Shoes_of_the_Mighty_Verse
      RADIANCE_GEAR.put (1879158536L, 30); // Armour:Shoes_of_the_Silver_Voice
      RADIANCE_GEAR.put (1879112357L, 20); // Armour:Shoulders_of_Durin's_Guard
      RADIANCE_GEAR.put (1879151850L, 25); // Armour:Shoulders_of_the_Ancient_Tongue
      RADIANCE_GEAR.put (1879158544L, 30); // Armour:Shoulders_of_the_Deathstorm
      RADIANCE_GEAR.put (1879112380L, 20); // Armour:Shoulders_of_the_Great_Bow
      RADIANCE_GEAR.put (1879151860L, 25); // Armour:Shoulders_of_the_High_Captain
      RADIANCE_GEAR.put (1879112358L, 20); // Armour:Shoulders_of_the_Lady's_Courage
      RADIANCE_GEAR.put (1879112360L, 20); // Armour:Shoulders_of_the_Lady's_Defence
      RADIANCE_GEAR.put (1879112383L, 20); // Armour:Shoulders_of_the_Lady's_Discernment
      RADIANCE_GEAR.put (1879112384L, 20); // Armour:Shoulders_of_the_Lady's_Favour
      RADIANCE_GEAR.put (1879112372L, 20); // Armour:Shoulders_of_the_Lady's_Foresight
      RADIANCE_GEAR.put (1879112371L, 20); // Armour:Shoulders_of_the_Lady's_Grace
      RADIANCE_GEAR.put (1879112359L, 20); // Armour:Shoulders_of_the_Lady's_Power
      RADIANCE_GEAR.put (1879112382L, 20); // Armour:Shoulders_of_the_Lady's_Secrecy
     
      RADIANCE_GEAR.put (1879112370L, 20); // Armour:Shoulders_of_the_Lady's_Wisdom
      RADIANCE_GEAR.put (1879151807L, 25); // Armour:Shoulders_of_the_Learned_Master
      RADIANCE_GEAR.put (1879112368L, 20); // Armour:Shoulders_of_the_Mighty_Verse
      RADIANCE_GEAR.put (1879158604L, 30); // Armour:Shoulders_of_the_Shadow-walker
      RADIANCE_GEAR.put (1879112379L, 20); // Armour:Shoulders_of_the_Silent_Knife
      RADIANCE_GEAR.put (1879158560L, 30); // Armour:Shoulders_of_the_Silver_Voice
      RADIANCE_GEAR.put (1879151842L, 25); // Armour:Shoulders_of_the_Strong_Defender
      RADIANCE_GEAR.put (1879151845L, 25); // Armour:Shoulders_of_the_Swift_Arrow
      RADIANCE_GEAR.put (1879158565L, 30); // Armour:Shoulders_of_the_Wall-warden
      RADIANCE_GEAR.put (1879174199L, 10); // Armour:Sigurd's_Lost_Gauntlets
      RADIANCE_GEAR.put (1879174207L, 10); // Armour:Skumfil's_Secret
      RADIANCE_GEAR.put (1879151826L, 25); // Armour:Songmaster's_Hat
      RADIANCE_GEAR.put (1879151830L, 25); // Armour:Songmaster's_Shoes
      RADIANCE_GEAR.put (1879151810L, 25); // Armour:Songmaster's_Shoulders
      RADIANCE_GEAR.put (1879112403L, 10); // Armour:Spear-hurler's_Boots
      RADIANCE_GEAR.put (1879112274L, 10); // Armour:Spear-hurler's_Gauntlets
      RADIANCE_GEAR.put (1879112407L, 20); // Armour:Spear-hurler's_Helm
      RADIANCE_GEAR.put (1879112220L, 10); // Armour:Spear-hurler's_Jacket
      RADIANCE_GEAR.put (1879112345L, 10); // Armour:Spear-hurler's_Leggings
      RADIANCE_GEAR.put (1879112381L, 20); // Armour:Spear-hurler's_Shoulders
      RADIANCE_GEAR.put (1879151812L, 15); // Armour:Stone-guard's_Breastplate
      RADIANCE_GEAR.put (1879151822L, 15); // Armour:Stone-guard's_Gauntlets
      RADIANCE_GEAR.put (1879151836L, 15); // Armour:Stone-guard's_Leggings
      RADIANCE_GEAR.put (1879112260L, 10); // Armour:Stone-reader's_Gloves
      RADIANCE_GEAR.put (1879112296L, 20); // Armour:Stone-reader's_Hat
      RADIANCE_GEAR.put (1879112206L, 10); // Armour:Stone-reader's_Robe
      RADIANCE_GEAR.put (1879112395L, 10); // Armour:Stone-reader's_Shoes
      RADIANCE_GEAR.put (1879112367L, 20); // Armour:Stone-reader's_Shoulders
      RADIANCE_GEAR.put (1879112331L, 10); // Armour:Stone-reader's_Trousers
      RADIANCE_GEAR.put (1879151838L, 15); // Armour:Swiftblade's_Breastplate
      RADIANCE_GEAR.put (1879151840L, 15); // Armour:Swiftblade's_Gauntlets
      RADIANCE_GEAR.put (1879151848L, 15); // Armour:Swiftblade's_Leggings
      RADIANCE_GEAR.put (1879174643L, 25); // Armour:Tathar
      RADIANCE_GEAR.put (1879174321L, 10); // Armour:Thalion-Ruin
      RADIANCE_GEAR.put (1879174648L, 25); // Armour:Th?l-Ernil
      RADIANCE_GEAR.put (1879112336L, 20); // Armour:Trousers_of_the_Lady's_Foresight
      RADIANCE_GEAR.put (1879112335L, 20); // Armour:Trousers_of_the_Lady's_Grace
      RADIANCE_GEAR.put (1879112334L, 20); // Armour:Trousers_of_the_Lady's_Wisdom
      RADIANCE_GEAR.put (1879112332L, 10); // Armour:Trousers_of_the_Mighty_Verse
      RADIANCE_GEAR.put (1879158576L, 30); // Armour:Trousers_of_the_Silver_Voice
      RADIANCE_GEAR.put (1879151856L, 15); // Armour:Trousers_of_the_Stone-reader's_Apprentice
      RADIANCE_GEAR.put (1879148763L, 10); // Armour:T?ranc
      RADIANCE_GEAR.put (1879174695L, 25); // Armour:T?rcam
      RADIANCE_GEAR.put (1879148760L, 10); // Armour:T?rthol
      RADIANCE_GEAR.put (1879112262L, 10); // Armour:Word-smith's_Gloves
      RADIANCE_GEAR.put (1879112298L, 20); // Armour:Word-smith's_Hat
      RADIANCE_GEAR.put (1879112208L, 10); // Armour:Word-smith's_Robe
      RADIANCE_GEAR.put (1879112397L, 10); // Armour:Word-smith's_Shoes
      RADIANCE_GEAR.put (1879112369L, 20); // Armour:Word-smith's_Shoulders
      RADIANCE_GEAR.put (1879112333L, 10); // Armour:Word-smith's_Trousers
   }

   // URL = http://lorebook.lotro.com/wiki/Special:LotroResource?id=
   // <item name="Hat of the Elder Days" slot="Head"
   // lorebookEntry="URL=1879094671" />

   private static final Pattern LOREBOOK = Pattern.compile ("[^=]+=([0-9]+)");

   private String name;
   private long id;
   private Slot slot;

   public Item (final Slot slot, final String name, final long id)
   {
      this.slot = slot;
      this.name = name;
      this.id = id;
   }

   public Item (final Map<String, String> xmlMap)
   {
      this.name = xmlMap.get ("name");
      this.slot = Slot.find (xmlMap.get ("slot"));
      Matcher m = LOREBOOK.matcher (xmlMap.get ("lorebookEntry"));
      if (m.find ())
         this.id = Long.parseLong (m.group (1));
   }

   public String getName ()
   {
      return name;
   }

   public long getID ()
   {
      return id;
   }

   public Slot getSlot ()
   {
      return slot;
   }

   public int getRadiance ()
   {
      Integer radiance = RADIANCE_GEAR.get (id);
      return radiance != null ? radiance.intValue () : 0;
   }

   public String getLorebookLink ()
   {
      return "http://lorebook.lotro.com/wiki/Special:LotroResource?id=" + id;
   }

   @Override
   public String toString ()
   {
      return name + " (" + id + ") Slot: " + slot;
   }

   // <a href="/wiki/Armour:Boots_of_the_Silent_Knife"><img
   // src="http://content.level3.turbine.com/sites/lorebook.lotro.com/images/icons/item/feet/eq_feet_med_tier1_set_1_burglar.png"
   // class="icon"
   // rel="/index.php?action=ajax&rs=efLotroResourceAjaxWrapper&rsargs[]=armor&rsargs[]=1879112401"
   // /></a>
   
   // <a href="/wiki/Armour:Far-arrow's_Boots"><img 
   // src="http://content.turbine.com/sites/lorebook.lotro.com/images/icons/item/feet/eq_feet_med_tier3_set_1_hunter.png"
   // class="icon" 
   // rel="/index.php?action=ajax&amp;rs=efLotroResourceAjaxWrapper&amp;
   // rsargs[]=armor&amp;rsargs[]=1879158553" title="">
   
   // <a href="/wiki/Armour:Gauntlets_of_the_Lady's_Secrecy"><img 
   // src="http://content.turbine.com/sites/lorebook.lotro.com/images/icons/item/hands/eq_hands_med_tier2_set_1_burglar.png" 
   // class="icon" 
   // rel="/index.php?action=ajax&amp;rs=efLotroResourceAjaxWrapper&amp;
   // rsargs[]=armor&amp;rsargs[]=1879112275">
   
   private static final Pattern RAD_ITEM =
      Pattern.compile ("<a href=\"/wiki/([^\"]+)\"><[^>]+=([0-9]+)\"");

   public static void main (final String[] args)
   {
      // parse the Lorebook Radiance (Advanced Search results page)
      // http://lorebook.lotro.com/wiki/Special:Advancedsearch?type=item&action=search&item_equip_mods[]=268444563&item_type[]=all&sort=name
      // Use Chrome (or Firefox/firebug); select Edit HTML; save as file

      ComponentTools.setDefaults ();

      String dir = System.getProperty ("user.home") + "/Desktop";
      FileChooser fc = new FileChooser ("Select Lorebook HTML File", dir);
      fc.setRegexFilter (".+[.]html", "HTML files");

      if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      {
         File file = fc.getSelectedFile ();
         if (file != null)
         {
            List<String> lines = FileUtils.getList (file.getPath (), FileUtils.UTF8, true);
            for (String line : lines)
            {
               Matcher m = RAD_ITEM.matcher (line);
               while (m.find())
               {
                  // RADIANCE_GEAR.put (1879112390L, 10); // boots
                  System.out.println ("RADIANCE_GEAR.put (" + m.group (2) + "L, 0); // "
                           + m.group (1));
               }
            }
         }
      }
   }
}
