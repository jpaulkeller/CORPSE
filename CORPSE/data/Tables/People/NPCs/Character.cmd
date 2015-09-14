/ http://random-generator.com/index.php?title=Griffin_Mountain_Personality_Generator

<html>
<body>

<!--
/ NPC.Cmd: creates a minimally fleshed-out non-player character,
/          based on the generated or DM-entered name.

/ No longer true:
/ This file is designed to be used when multiple NPCs are generated.
/ It provides only the most basic of information.
/ For more detailed NPCs, use the NPC_Full (or NPC_Flex) scripts.
-->

/ Generate a random name.
/ Ask the user for a name, using the random one as a default.
/ Assign the entered name to "NPC" for re-use later.
/ Randomize using the name as the seed, for consistent results.
# {NPC={NPC Name?{Name}}}
<!--
{W={10}} / wealth
{G:=Gender}
{C:=color hex}
-->

<!-- Show the name, generate and show a race and previous profession. -->
<p><b>{NPC}</b>

<ul>
  <li>Stats:
  <ul>
    <li>Gender: {G.gender}
    <li>Race: {race}
    <li>Age: {age}
    <li>Str: {3d6} Dex: {3d6} Agi: {3d6} Con: {3d6} Int: {3d6} Wis: {3d6} Dis: {3d6} Cha: {3d6}
  </ul>
 
  <li>Appearance:
  <ul>
    <li>Overall Impression: {appearance}
    <li>Height: {short|average|tall}
    <li>Build: {build}
    <li>Hair: {hair}{{G.gender}=male?{50%He {hair:facial}}}
    <li>Complexion: {skin} 
    <li>Eyes: {eyes}
!loop {2d2-2}
    <li>Marking: {markings}
!loop end
  </ul>
 
  <li>Dress:
  <ul>
    <li>Dressed: {dressed}
    <li>Wearing: {dressed:wearing}
    <li>Wearing: <font color="{C.font}" bgcolor="{C.hex}">{C.name}-colored</font> {75%{condition:clothing} }{~{clothing}}
    <li>Accessories: {{{W}*8}%{jewelry}}
  </ul>
 
  <li>Background:
  <ul>
    <li>Profession: {Profession}
    <li>Family -- Father: {Profession}  Mother: {Profession}
    {25%<li>Birth: {birth}}
    {10%<li>Bloodline: {bloodline}}
    {25%<li>Legacy: {legacy}}
    {50%<li>Motivation: {motivation}}
    {50%<li>Wealth: {wealth}}
  </ul>
 
  <li>Personality:
  <ul>
    <li>Personality: {personality}
    <li>Trait: {50%{degree} }{trait}
    <li>Trait Spectrum:
      <ul>
+ Traits
      </ul>
    </li>
    <li>Disposition: {50%{frequency} }{disposition}
    <li>Feeling (generally): {50%{degree} }{feeling}
    <li>Emotion: {emotion}
    <li>Mood (currently feeling): {50%{degree} }{mood}
    <li>Attitude (inclination): {50%{degree} }{attitude}
    <li>Reaction (to PCs): {50%{degree} }{reaction}
    {50%<li>Vice: {vice}}
  </ul>
  
  <li>Quirks:
  <ul>
    {50%<li>Advantage: {advantage}}
    {50%<li>Belief: {belief}}
    {50%<li>Dark Secret: {dark secret}}
    {50%<li>Disadvantage: {disadvantage}}
    {50%<li>Habit: {habit}}
    {50%<li>Handicap: {handicap}}
    {50%<li>Hobby: {hobby}}
    {50%<li>Injury: {injury}}
    {50%<li>Mannerism: {mannerism}}
    {50%<li>Neurosis: {neurosis}}
    {20%<li>Philia: {philia!}}
    {20%<li>Phobia: {phobia!}}
    {50%<li>Quirk: {quirk}}
    {33%<li>Flaw: {character flaw!}}
  </ul>
 
  <li>Equipment and Possessions:
  <ul>
    <li>Currency: {{5*{W}}%?{10} gp; }{20*{W}} sp; {50} cp{{W}%?; {gem}}
    <li>Carrying: {~{tiny item}}; {~{tiny item{!different}}}; {~{tiny item{!different}}}
    <li>Equipped with: {~{equipment}}; {~{equipment}}; {~{equipment}}
  </ul>
</ul>

</body>
</html>

#
=================

/Aben Frarde: Female Human Peasant, Neutral. Aben is overweight, with gray hair and green eyes. She wears travel-stained clothing and riding boots. Aben is shrewd and violent.
/Aenwurh: Male Human Merchant, Evil. Aenwurh has a round face, with white hair and blue eyes. He wears modest garments and a yellow cloak. Aenwurh suffers an acute fear of clerics.
/Affed Tonell: Male Halfling Peasant, Good. Affed has a long face, with brown hair and narrow green eyes. He wears sturdy clothing and a wide-brimmed hat. Affed seeks a party to recover the Relic of Andar from the cultists of Kadatha.
/Belle: Male Elf Priest, Good. Belle is short and slender, with black hair and soft brown eyes. He wears tailored clothing and a silver amulet. Belle seeks to find his true love.
/Beorhtio: Male Human Artist, Neutral. Beorhtio is pleasant in appearance, with braided brown hair and narrow gray eyes. He wears expensive clothing and carries a fine stiletto. Beorhtio seeks a party to clear his name against charges of forgery.
/Cuilla: Female Elf Craftsman, Good. Cuilla has golden hair and blue eyes, and small ears. She wears plain clothing and a wide-brimmed hat. Cuilla is haunted by the memories of a past life.
/Dina: Female Elf Priest, Good. Dina has a round face, with straight white hair and gray eyes. She wears well-made clothing and a dragonscale cloak. Dina seeks a party to clear her name against charges of forgery.
/Eadwith: Male Human Merchant, Good. Eadwith is rugged in appearance, with golden hair and sharp amber eyes. He is principled and mischievous. Eadwith seeks a party to find and explore the ancient ruins of Lefield Castle.
/Eanswild: Female Human Peasant, Good. Eanswild is tall, with blonde hair and bright blue eyes. She wears worn clothing and several small tools hang from her belt. Eanswild is hunting the warlord who stole her ancestral sword.
/Ethbeow: Male Human Scofflaw, Evil. Ethbeow has a long face, with braided gray hair and bright hazel eyes. He wears sturdy clothing and carries a long knife.
/Gala: Male Elf Entertainer, Evil. Gala has messy copper hair and blue eyes, and sharpened teeth. He wears modest garments and a sling of vials and potions. Gala is hunting the sorcerer who murdered his family.
/Giles: Male Human Alchemist, Good. Giles has a round face, with long copper hair and green eyes. He wears expensive clothing and carries a fine stiletto.
/Narder: Male Halfling Peasant, Good. Narder has silver hair and large gray eyes, and a thick beard. He wears worn clothing and riding boots.
/Nelebrie: Female Elf Professional, Evil. Nelebrie is overweight, with blonde hair and brown eyes. She wears expensive clothing and numerous rings. Nelebrie suffers a severe allergy to milk.
/Nimrie: Female Elf Scholar, Good. Nimrie is overweight, with long black hair and gray eyes. She wears tailored clothing and several pouches hang from her belt. Nimrie compulsively flirts with others.
/Orverdr Tolfidotr: Female Dwarf Craftsman, Evil. Orverdr is repulsive in appearance, with blonde hair and sharp gray eyes. She wears simple clothing and carries a long knife. Orverdr seeks a party to find and explore the ancient ruins of the Tower of Gothmog of Udun.
/Phily Bowe: Female Halfling Professional, Evil. Phily has black hair and large brown eyes. She wears expensive clothing and a copper amulet.
/Piersym: Male Halfling Merchant, Evil. Piersym is short and slender, with gray hair and sharp amber eyes. He wears worn clothing and a wide-brimmed hat.
/Rida: Female Halfling Mercenary, Evil. Rida has cropped golden hair and dark green eyes. She wears studded leather and wields a bardiche. Rida is fascinated by mythology and the gods.
/Shimi: Male Elf Professional, Neutral. Shimi has red hair and blue eyes, and a messy moustache. He is clever and truthful. Shimi seeks a party to slay the Behemoth of Eriger and retrieve a vial of its blood.
/Sigi: Male Dwarf Artist, Neutral. Sigi is slender, with thin auburn hair and large blue eyes. He wears modest garments and several pouches hang from his belt. Sigi seeks a party to clear his name against charges of treason.
/Thaaki: Male Dwarf Aristocrat, Good. Thaaki has long brown hair and bright green eyes, and a messy beard. He wears fine raiment and a mink fur cape. Thaaki has an animal companion, a tawny rat named Skolrie.
/Thomond Horne: Male Halfling Priest, Neutral. Thomond has a square face, with matted gray hair and blue eyes. He wears well-made clothing and an amulet of luminous crystal.
/Wene Eldyn: Female Human Slave, Evil. Wene has brown hair and large blue eyes, and a flat nose. She wears sturdy clothing and an iron amulet. Wene is insensitive and mercenary.

Name: Frederich
 milkeheued

Half-Elf; Male; NG; HP: 20; AC: 1; Attr (S/I/W/D/C/Ch): 13/13/17/6/14/6
Hit: 0; Dam: 0; Weight: 100; OpenD: 2(d6); BBLG: 4(%d)
AddLang: 0; MagicAttack: 3
ReactAdj/Miss: 0; DefAdj: 1; SSS: 88(%d); ResSurv: 92(%d); HenchMax: 2; Loyal: -15; React: -10
Infra: 60; Det.S Door: 2(d6); Det.C Door: 3(d6)
30% resist sleep/charm
Langs: Elvish, gnome, halfling, goblin, hobgoblin, orcish, gnoll, common
(R) Ranger; Lvl: 4 (Courser)
(R) XP: 10001; Bonus vs. giant types: 4; Surprise Chance: 3(d6) to surp.; 1(d6) against 
(R) Att/Rnd: 1/1; Tracking Base (Indoors): 65; Tracking Base (Outdoors): 90
(R) Perks: 
(R) Armor: Any; Shield: Any; Weap: Any (no longbow or +12' if under 5' height; no heav. crossb. or pole arms +200 id -100 weight); Oil: yes; Poison: no 
(R) Eq.: +1 Large Shield; +1 Chain Mail; +1 Dagger (1d4 + 1); Long Sword (1d8); Long Bow (1d6); Potion of Climbing; 
(R) Attack (-10/10): 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11 10 9 8
(R) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 13/14/15/17/16
*************************************
Age: 40 (Young); Height: 66 (Average: 66); Weight: 130 (Average: 130)
Additional Langs: 
Possessions: Exceptional; Gen. Appearance: Foppish; Sanity: Normal
Gen. Tendencies: Altruist; Personality: Egoist/Arrogant; Disposition: Proud/Haughty
Intellect: Active; Nature: Soft-hearted; Materialism: Intellectualist
Honesty: Scrupulous; Bravery: Fearless; Morals: Lustful; Piety: Average
Energy: Normal; Thrift: Average; Interests: Collector: Swords
--------------------------------------------------------------------------------------------------------------------------------------------------
Name: 'Amr
 Rensell

Dwarf; Male; N; HP: 6; AC: 4; Attr (S/I/W/D/C/Ch): 17/6/6/9/19/15
Hit: 1; Dam: 1; Weight: 500; OpenD: 3(d6); BBLG: 13(%d)
AddLang: 0; MagicAttack: -1
ReactAdj/Miss: 0; DefAdj: 0; SSS: 99(%d); ResSurv: 100(%d); HenchMax: 7; Loyal: 15; React: 15
Spell/Poison Bonus: 5; Infra: 60; Det. Slope/Constr.: 3(d4); Det. Shift/Slide: 4(d4); Det. Trap/Depth: 2(d4)
+1 vs. half-orcs, goblins, hobgoblins, or orcs; -4 from ogres, trolls, ogre magi, giants, or titans
Langs: Dwarven, gnome, goblin, kobold, orcish, common
(F) Fighter; Lvl: 1 (Veteran)
(F) XP: 0; Att/Rnd: 1/1
(F) Armor: Any; Shield: Any; Weap: Any (no longbow or +12' if under 5' height; no heav. crossb. or pole arms +200 id -100 weight); Oil: yes; Poison: yes 
(F) Eq.: Large Shield; Chain Mail; Long Sword (1d8); Long Bow (1d6); 
(F) Attack (-10/10): 25 24 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11 10
(F) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 14/15/16/18/17
(T) Thief; Lvl: 1 (Rogue (Apprentice))
(T) XP: 0; Backstab Dam.: 2; Read Lang.: -5; Misread Scroll: 
(T) Perks: Knows thieves' cant; +4 to hit when backstabbing
(T) PickP: 30; Locks: 35; Traps: 35
(T) Silent: 15; Hide: 10; Hear: 10; Climb: 75
(T) Armor: Leather; Shield: No; Weap: club, dagger, dart, sling, sword (not bastard/2-hand); Oil: yes; Poison: yes 
(T) Eq.: Chain Mail; Long Sword (1d8); Long Bow (1d6); 
(T) Attack (-10/10): 26 25 24 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11
(T) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 13/12/14/16/15
Starting Gold: 60 g.p.
*************************************
Age: 125 (Mature); Height: 48 (Average: 48); Weight: 162 (Average: 150)
Additional Langs: 
Possessions: Scant; Gen. Appearance: Ragged; Sanity: Normal
Gen. Tendencies: Pessimist; Personality: Aloof; Disposition: Proud/Haughty
Intellect: Active; Nature: Hard-hearted; Materialism: Average
Honesty: Average; Bravery: Foolhardy; Morals: Immoral; Piety: Average
Energy: Normal; Thrift: Average; Interests: Exotic animals
--------------------------------------------------------------------------------------------------------------------------------------------------
Name: Amis
 Madeley

Gnome; Male; N; HP: 9; AC: 5; Attr (S/I/W/D/C/Ch): 15/15/6/16/10/9
Hit: 0; Dam: 0; Weight: 200; OpenD: 2(d6); BBLG: 7(%d)
AddLang: 0; MagicAttack: -1
ReactAdj/Miss: 1; DefAdj: -2; SSS: 70(%d); ResSurv: 75(%d); HenchMax: 4; Loyal: 0; React: 0
Spell/Poison Bonus: 2; Infra: 60; Det. Slope: 8(d10); Det. Unsafe: 7(d10); Det. Depth: 6(d10); Det. Direc: 5(d10)
+1 to hit kobolds and goblins; -4 from gnolls, bugbears, ogres, ogre magi, giants, and titans
Langs: Dwarvish, gnome, halfling, goblin, kobold, burrowers, common
(I) Illusionist; Lvl: 5 (Cabalist)
(I) XP: 18001; Spells - 1st: Hypnotism; Detect Illusion; Light; Phantasmal Force
(I) 2nd: Fog Cloud; Improved Phantasmal Force; 3rd: Paralyzation
(I) Perks: 
(I) Armor: No; Shield: No; Weap: dagger, dart, staff; Oil: yes; Poison: yes 
(I) Eq.: +1 Ring of Prot.; Dagger (1d4); 
(I) Attack (-10/10): 26 25 24 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11
(I) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 14/13/11/15/12
*************************************
Age: 260 (Mature); Height: 42 (Average: 42); Weight: 72 (Average: 80)
Additional Langs: 
Possessions: Exceptional; Gen. Appearance: Unkempt; Sanity: Very stable
Gen. Tendencies: Sober; Personality: Blustering; Disposition: Humble
Intellect: Scheming; Nature: Jealous; Materialism: Intellectualist
Honesty: Average; Bravery: Fearless; Morals: Lusty; Piety: Pious
Energy: Slothful; Thrift: Thrifty; Interests: Hunting
--------------------------------------------------------------------------------------------------------------------------------------------------
Name: Thrystan
 Markett

Human; Male; CE; HP: 58; AC: -2; Attr (S/I/W/D/C/Ch): 6/13/16/12/14/18
Hit: -1; Dam: 0; Weight: -150; OpenD: 1(d6); BBLG: 0(%d)
AddLang: 3; MagicAttack: 2
ReactAdj/Miss: 0; DefAdj: 0; SSS: 88(%d); ResSurv: 92(%d); HenchMax: 15; Loyal: 40; React: 35
(C) Cleric; Lvl: 19 (High Priest)
(C) XP: 2475001; Spells - 1st: Command; Light; Command; Resist Cold; Bless; Light; Bless; Light; Detect Magic; Bless; Bless; 2nd: Find Traps; Know Alignment; Hold Person; Hold Person; Resist Fire; Speak with Animals; Find Traps; Speak with Animals; Find Traps; Silence 15' Radius; Slow Poison
(C) 3rd: Continual Light; Prayer; Glyph of Warding; Speak with Dead; Speak with Dead; Prayer; Create Food & Water; Dispel Magic; Prayer; 4th: Divination; Lower Water; Sticks to Snakes; Speak with Plants; Divination; Protection from Evil 10' Radius; Protection from Evil 10' Radius
(C) 5th: Raise Dead; Commune; True Seeing; Plane Shift; Cure Critical Wounds; Insect Plague
(C) Perks: Est. Stronghold
(C) Armor: Any; Shield: Any; Weap: club, flail, hammer, mace, staff; Oil: yes; Poison: no (unless evil) 
(C) Eq.: +5 Small Shield; +1 Banded Mail; +1 Ring of Prot.; +5 Mace (1d6 + 6); Potion of Polymorph Self; Scroll w. 1 Spell; 
(C) Attack (-10/10): 19 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 2 1 0 -1
(C) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 2/5/6/8/7
(C) Turn: (d12) Skeleton: D(d6+6); Zombie: D(d6+d); Ghoul: D(d6+6); Shadow: D(d6+6); Wight: D; Ghast: D; Wraith: D; Mummy (P1/P2): T; Spectre (P3/P4): T; Vampire (P5/P6): 4; Ghost (P7/P8): 7; Lich (P9/P10): 10; Special (P11+): 13
*************************************
Age: 35 (Mature); Height: 72 (Average: 72); Weight: 175 (Average: 175)
Additional Langs: Lizard Man; Hobgoblin; Goblin; 
Possessions: Average; Gen. Appearance: Unkempt; Sanity: Very stable
Gen. Tendencies: Optimist; Personality: Friendly; Disposition: Cheerful
Intellect: Dull; Nature: Jealous; Materialism: Aesthetic
Honesty: Average; Bravery: Fearless; Morals: Immoral; Piety: Martyr/Zealot
Energy: Energetic; Thrift: Thrifty; Interests: Community service
--------------------------------------------------------------------------------------------------------------------------------------------------
Name: Edelina
 Ormond

Dwarf; Female; N; HP: 4; AC: 1; Attr (S/I/W/D/C/Ch): 17/18/7/9/13/6
Hit: 1; Dam: 1; Weight: 500; OpenD: 3(d6); BBLG: 13(%d)
AddLang: 0; MagicAttack: -1
ReactAdj/Miss: 0; DefAdj: 0; SSS: 85(%d); ResSurv: 90(%d); HenchMax: 2; Loyal: -15; React: -10
Spell/Poison Bonus: 3; Infra: 60; Det. Slope/Constr.: 3(d4); Det. Shift/Slide: 4(d4); Det. Trap/Depth: 2(d4)
+1 vs. half-orcs, goblins, hobgoblins, or orcs; -4 from ogres, trolls, ogre magi, giants, or titans
Langs: Dwarven, gnome, goblin, kobold, orcish, common
(F) Fighter; Lvl: 1 (Veteran)
(F) XP: 0; Att/Rnd: 1/1
(F) Armor: Any; Shield: Any; Weap: Any (no longbow or +12' if under 5' height; no heav. crossb. or pole arms +200 id -100 weight); Oil: yes; Poison: yes 
(F) Eq.: Large Shield; Plate Mail; Spear (1d6); Long Bow (1d6); Scroll of prot.; 
(F) Attack (-10/10): 25 24 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11 10
(F) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 14/15/16/18/17
(T) Thief; Lvl: 1 (Rogue (Apprentice))
(T) XP: 0; Backstab Dam.: 2; Read Lang.: -10; Misread Scroll: 
(T) Perks: Knows thieves' cant; +4 to hit when backstabbing
(T) PickP: 30; Locks: 35; Traps: 35
(T) Silent: 15; Hide: 10; Hear: 10; Climb: 75
(T) Armor: Leather; Shield: No; Weap: club, dagger, dart, sling, sword (not bastard/2-hand); Oil: yes; Poison: yes 
(T) Eq.: Plate Mail; Spear (1d6); Long Bow (1d6); Scroll of prot.; 
(T) Attack (-10/10): 26 25 24 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11
(T) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 13/12/14/16/15
Starting Gold: 80 g.p.
*************************************
Age: 405 (Middle-aged); Height: 46 (Average: 46); Weight: 112 (Average: 120)
Additional Langs: 
Possessions: Average; Gen. Appearance: Non-descript; Sanity: Normal
Gen. Tendencies: Foul/Barbaric; Personality: Friendly; Disposition: Brilliant
Intellect: Dull; Nature: Vengeful; Materialism: Intellectualist
Honesty: Average; Bravery: Fearless; Morals: Virtuous; Piety: Average
Energy: Normal; Thrift: Thrifty; Interests: Wines & Spirits
--------------------------------------------------------------------------------------------------------------------------------------------------
Name: Sechnassach
 seruiens

Half-Elf; Male; NG; HP: 16; AC: 3; Attr (S/I/W/D/C/Ch): 9/17/6/10/17/8
Hit: 0; Dam: 0; Weight: 0; OpenD: 2(d6); BBLG: 1(%d)
AddLang: 1; MagicAttack: -1
ReactAdj/Miss: 0; DefAdj: 0; SSS: 97(%d); ResSurv: 98(%d); HenchMax: 3; Loyal: -5; React: 0
Infra: 60; Det.S Door: 2(d6); Det.C Door: 3(d6)
30% resist sleep/charm
Langs: Elvish, gnome, halfling, goblin, hobgoblin, orcish, gnoll, common
(F) Fighter; Lvl: 2 (Warrior)
(F) XP: 2001; Att/Rnd: 1/1
(F) Armor: Any; Shield: Any; Weap: Any (no longbow or +12' if under 5' height; no heav. crossb. or pole arms +200 id -100 weight); Oil: yes; Poison: yes 
(F) Eq.: Large Shield; Chain Mail; Sword (1d8); Long Bow (1d6); 
(F) Attack (-10/10): 25 24 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11 10
(F) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 14/15/16/18/17
*************************************
Age: 315 (Middle-aged); Height: 66 (Average: 66); Weight: 126 (Average: 130)
Additional Langs: Human foreign; 
Possessions: Exceptional; Gen. Appearance: Non-descript; Sanity: Unstable
Gen. Tendencies: Moody; Personality: Egoist/Arrogant; Disposition: Unfeeling/Insensitive
Intellect: Active; Nature: Jealous; Materialism: Aesthetic
Honesty: Average; Bravery: Normal; Morals: Lusty; Piety: Impious
Energy: Normal; Thrift: Mean; Interests: Community service
--------------------------------------------------------------------------------------------------------------------------------------------------
Name: Reynard
 du Bec

Gnome; Male; LE; HP: 6; AC: 6; Attr (S/I/W/D/C/Ch): 12/11/6/12/8/3
Hit: 0; Dam: 0; Weight: 100; OpenD: 2(d6); BBLG: 4(%d)
AddLang: 0; MagicAttack: -1
ReactAdj/Miss: 0; DefAdj: 0; SSS: 60(%d); ResSurv: 65(%d); HenchMax: 1; Loyal: -30; React: -25
Spell/Poison Bonus: 2; Infra: 60; Det. Slope: 8(d10); Det. Unsafe: 7(d10); Det. Depth: 6(d10); Det. Direc: 5(d10)
+1 to hit kobolds and goblins; -4 from gnolls, bugbears, ogres, ogre magi, giants, and titans
Langs: Dwarvish, gnome, halfling, goblin, kobold, burrowers, common
(A) Assassin; Lvl: 2 (Rutterkin)
(A) XP: 1501; PickP: 35; Locks: 34; Traps: 35
(A) Silent: 26; Hide: 20; Hear: 20; Climb: 71
(A) Poison Spot: 10%/round (20% attack/50% call watch/30% both)
(A) Disguise Spot: 2%/day (+2% diff. class/race/sex; 30+ int/wis + 1%/point; 24- int/wis -1%/point)
(A) Armor: Leather; Shield: Any; Weap: Any (no longbow or +12' if under 5' height; no heav. crossb. or pole arms +200 id -100 weight); Oil: yes; Poison: yes 
(A) Eq.: Leather Armor; +1 Ring of Prot.; Short Sword (1d6); 
(A) Attack (-10/10): 26 25 24 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11
(A) Assassinate: 0/1: 55%; 2/3: 50%; 4/5: 40%; 6/7: 30%; 8/9: 15%; 10/11: 2%
(A) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 13/12/14/16/15
*************************************
Age: 610 (Middle-aged); Height: 45 (Average: 42); Weight: 82 (Average: 80)
Additional Langs: 
Possessions: Above average; Gen. Appearance: Ragged; Sanity: Normal
Gen. Tendencies: Fanatical/Obsessive; Personality: Forceful; Disposition: Even tempered
Intellect: Dreaming; Nature: Soft-hearted; Materialism: Avaricious
Honesty: Scrupulous; Bravery: Fearless; Morals: Virtuous; Piety: Impious
Energy: Slothful; Thrift: Average; Interests: Legends
--------------------------------------------------------------------------------------------------------------------------------------------------
Name: Alaric
 Kalmeyer

Elf; Male; CE; HP: 29; AC: 5; Attr (S/I/W/D/C/Ch): 9/15/13/18/7/9
Hit: 0; Dam: 0; Weight: 0; OpenD: 2(d6); BBLG: 1(%d)
AddLang: 0; MagicAttack: 0
ReactAdj/Miss: 3; DefAdj: -4; SSS: 55(%d); ResSurv: 60(%d); HenchMax: 4; Loyal: 0; React: 0
Infra: 60; Det.S Door: 2(d6); Det.C Door: 3(d6)
+1 with bow, short sword, and long sword; 90% resist sleep/charm
1-4 (d6) chance to surprise if 90' ahead; 1-2 (d6) if opened door
Langs: Elvish, gnome, halfling, goblin, hobgoblin, orcish, gnoll, common
(F) Fighter; Lvl: 3 (Swordsman)
(F) XP: 4001; Att/Rnd: 1/1
(F) Armor: Any; Shield: Any; Weap: Any (no longbow or +12' if under 5' height; no heav. crossb. or pole arms +200 id -100 weight); Oil: yes; Poison: yes 
(F) Eq.: Large Shield; Plate Mail; Long Sword (1d8); Long Bow (1d6); 
(F) Attack (-10/10): 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11 10 9 8
(F) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 13/14/15/17/16
(T) Thief; Lvl: 3 (Cutpurse)
(T) XP: 2501; Backstab Dam.: 2; Read Lang.: -10; Misread Scroll: 
(T) Perks: Knows thieves' cant; +4 to hit when backstabbing
(T) PickP: 45; Locks: 28; Traps: 30
(T) Silent: 32; Hide: 30; Hear: 20; Climb: 87
(T) Armor: Leather; Shield: No; Weap: club, dagger, dart, sling, sword (not bastard/2-hand); Oil: yes; Poison: yes 
(T) Eq.: Plate Mail; Long Sword (1d8); Long Bow (1d6); 
(T) Attack (-10/10): 26 25 24 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11
(T) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 13/12/14/16/15
*************************************
Age: 470 (Mature); Height: 60 (Average: 60); Weight: 96 (Average: 100)
Additional Langs: 
Possessions: Average; Gen. Appearance: Ragged; Sanity: Normal
Gen. Tendencies: Violent/Warlike; Personality: Blustering; Disposition: Even tempered
Intellect: Ponderous; Nature: Hard-hearted; Materialism: Greedy
Honesty: Scrupulous; Bravery: Fearless; Morals: Average; Piety: Average
Energy: Normal; Thrift: Thrifty; Interests: Legends
--------------------------------------------------------------------------------------------------------------------------------------------------
Name: Virmundus
 Leafgrene

Gnome; Male; NG; HP: 26; AC: 2; Attr (S/I/W/D/C/Ch): 9/7/12/9/8/18
Hit: 0; Dam: 0; Weight: 0; OpenD: 2(d6); BBLG: 1(%d)
AddLang: 0; MagicAttack: 0
ReactAdj/Miss: 0; DefAdj: 0; SSS: 60(%d); ResSurv: 65(%d); HenchMax: 15; Loyal: 40; React: 35
Spell/Poison Bonus: 2; Infra: 60; Det. Slope: 8(d10); Det. Unsafe: 7(d10); Det. Depth: 6(d10); Det. Direc: 5(d10)
+1 to hit kobolds and goblins; -4 from gnolls, bugbears, ogres, ogre magi, giants, and titans
Langs: Dwarvish, gnome, halfling, goblin, kobold, burrowers, common
(F) Fighter; Lvl: 5 (Swashbuckler)
(F) XP: 18001; Att/Rnd: 1/1
(F) Armor: Any; Shield: Any; Weap: Any (no longbow or +12' if under 5' height; no heav. crossb. or pole arms +200 id -100 weight); Oil: yes; Poison: yes 
(F) Eq.: Large Shield; Banded Mail; Battle Axe (1d8); Long Bow (1d6); Potion of Climbing; 
(F) Attack (-10/10): 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11 10 9 8 7 6
(F) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 11/12/13/15/14
*************************************
Age: 190 (Youthful); Height: 42 (Average: 42); Weight: 80 (Average: 80)
Additional Langs: 
Possessions: Scant; Gen. Appearance: Unkempt; Sanity: Normal
Gen. Tendencies: Pessimist; Personality: Blustering; Disposition: Harsh
Intellect: Anti-intellectual; Nature: Soft-hearted; Materialism: Intellectualist
Honesty: Average; Bravery: Normal; Morals: Average; Piety: Average
Energy: Normal; Thrift: Thrifty; Interests: Hunting
--------------------------------------------------------------------------------------------------------------------------------------------------
Name: Pantaleone
 Trushernays

Gnome; Male; LE; HP: 13; AC: 9; Attr (S/I/W/D/C/Ch): 12/15/6/18/12/3
Hit: 0; Dam: 0; Weight: 100; OpenD: 2(d6); BBLG: 4(%d)
AddLang: 0; MagicAttack: -1
ReactAdj/Miss: 3; DefAdj: -4; SSS: 80(%d); ResSurv: 85(%d); HenchMax: 1; Loyal: -30; React: -25
Spell/Poison Bonus: 3; Infra: 60; Det. Slope: 8(d10); Det. Unsafe: 7(d10); Det. Depth: 6(d10); Det. Direc: 5(d10)
+1 to hit kobolds and goblins; -4 from gnolls, bugbears, ogres, ogre magi, giants, and titans
Langs: Dwarvish, gnome, halfling, goblin, kobold, burrowers, common
(A) Assassin; Lvl: 3 (Waghalter)
(A) XP: 3001; PickP: 40; Locks: 38; Traps: 40
(A) Silent: 32; Hide: 25; Hear: 25; Climb: 72
(A) Poison Spot: 10%/round (20% attack/50% call watch/30% both)
(A) Disguise Spot: 2%/day (+2% diff. class/race/sex; 30+ int/wis + 1%/point; 24- int/wis -1%/point)
(A) Armor: Leather; Shield: Any; Weap: Any (no longbow or +12' if under 5' height; no heav. crossb. or pole arms +200 id -100 weight); Oil: yes; Poison: yes 
(A) Eq.: Leather Armor; +1 Ring of Prot.; Mace (1d6 + 1); 
(A) Attack (-10/10): 26 25 24 23 22 21 20 20 20 20 20 20 19 18 17 16 15 14 13 12 11
(A) Assassinate: 0/1: 60%; 2/3: 55%; 4/5: 45%; 6/7: 35%; 8/9: 20%; 10/11: 5%
(A) Save (Para|Poi|DM/Petri|Poly/Rod|St|Wnd/Breath/Spell): 13/12/14/16/15
*************************************
Age: 120 (Youthful); Height: 39 (Average: 42); Weight: 80 (Average: 80)
Additional Langs: 
Possessions: Superabundant; Gen. Appearance: Dirty; Sanity: Normal
Gen. Tendencies: Careless; Personality: Overbearing; Disposition: Harsh
Intellect: Dreaming; Nature: Unforgiving; Materialism: Avaricious
Honesty: Very honorable; Bravery: Fearless; Morals: Amoral; Piety: Average
Energy: Slothful; Thrift: Miserly; Interests: Wines & Spirits
--------------------------------------------------------------------------------------------------------------------------------------------------
