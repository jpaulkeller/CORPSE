<html>
<body>

<!--
/ NPC.Cmd: creates a minimally fleshed-out non-player character,
/          based on the generated or DM-entered name.

/ This file is designed to be used when multiple NPCs are generated.
/ It provides only the most basic of information.
/ For more detailed NPCs, use the NPC_Full (or NPC_Flex) scripts.
-->

/ Generate a random name.
/ Ask the user for a name, using the random one as a default.
/ Assign the entered name to "NPC" for re-use later.
/ Randomize using the name as the seed, for consistent results.
# {NPC={NPC Name?{Name}}}

<!-- Show the name, generate and show a race and previous profession. -->
<p><b>{NPC}</b>, {race} {profession}

<ul>
  <!-- Generate and show a gender and two personality traits. -->
  <li>{Gender.Pronoun} is {degree} {trait} and {frequency} {disposition}.  Currently feeling {degree} {mood}.

  <li>Appearance: {Appearance}
 
  <!-- Generate and show some pocket stuff (with 2% chance of gem) -->
  <li>Currency: {20%?{10} gp; }{0-100} sp; {0-30} cp{2%?; {gem}}
  <li>Carrying: {tiny item}; {tiny item{!different}}; {tiny item{!different}}
 
  <!-- Generate and show some equipment. -->
  <li>Equipped with: {equipment}; {equipment}; {equipment}
 
  <li>Str: {3d6} Dex: {3d6} Agi: {3d6} Con: {3d6} Int: {3d6} Wis: {3d6} Dis: {3d6} Cha: {3d6}
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
