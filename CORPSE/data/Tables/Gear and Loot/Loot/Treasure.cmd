/ http://random-generator.com/index.php?title=Interesting_Treasure_Hoard_Generator
/ http://www.mithrilandmages.com/utilities/1ETreasure.php
/ http://angband.oook.cz/steamband/Treasure.pdf
/ http://www.giantitp.com/forums/showthread.php?453619-Art-Objects-Pimp-your-BBEG-s-crib!
/ https://rpgathenaeum.wordpress.com/tag/pimp-my-treasure-parcel/
/ https://www.goodreads.com/author_blog_posts/871940-precious-things-japanese-art-objects-for-your-d-d-game

<html>
<body>

/ Query for seed (using random default); assign and randomize
# {T={Treasure?{1000-9999}}}
/ {S:=Loot}
/ {L:=Loot}
{S={Size (1-5)?{5}}}
{L={Level (1-9)?{9}}}
{R={2*{{S}*{L}}}}

<h1>Treasure #{T} (size: {S}, level: {L}, rank: {R}) </h1>

<ul>
!loop {S}
   <li>{container:small loot} holding {N={{S}d20}}{C:=currency}{N} {C.nickname}s ({C.coin}s, worth about {{N}*{C.cp}} cp)
   {{R}% <li>salvage: {salvage}}
   {{R}% <li>gathered: {GATHERED}}
   {{R}% <li>{container:small loot} holding {{S}d20} coins: {coin}}
   {{R}% <li>{container:small loot} holding {{S}d{L}} {{~{gem cost.gem}}+}{50% and {{S}d{L}} {{~{gem cost.gem{!different}}}+}}}
   {{R}% <li>{{S}d100} {coin:simple}s, {coin:storage}}
   {{R}% <li>{container:medium loot} holding {{S}d4} {metal} {ingots|bars}}
   {{R}% <li><font color=green>Luxury: {LUXURY:ITEM} (worth {{L}d25}0 gp)</font>}
   {{R}% <li><font color=brown>Art: {ART}{50% (by {ART:ARTIST})}</font>}
   {{R}% <li><font color=gold>Jewelry: {luxury:jewelry} (worth {{L}d25}0 gp)</font>}
   {{R}% <li><font color=blue>Magic Item: {ENCHANTED ITEM}</font>}
!loop end
   {{{L}*3}% <li><font color=purple>Artifact: {ARTIFACT NAME}</font>}
   {{{L}*3}% <li><font color=red>Relic: {RELIC}</font>}
</ul>

/   <li>Hidden: {treasure:hidden}
/   <li>{treasure}{5% (the {inscription|design|decoration} provides a clue to its use)}
/   <li>Condition: {condition:any}
/   <li>Quality: {quality:any}
/ {:misc}

</body>
</html>
