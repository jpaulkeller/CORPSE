/ http://www.seventhsanctum.com/generate.php?Genname=dragon
/ http://www.seventhsanctum.com/generate.php?Genname=dragondesc
/ http://mwtools.thyle.net/d_gen.html

<html>
<body>

/ Query for name (using random default); assign and randomize
# {D={Dragon?{Dragon Name}}}
{G:=Gender}

<h1>{D}</h1>

<ul>
!switch {traditional|random}

!switch traditional
  <li><b>Description</b>
  <ul>
    <li>Influence: {dragon:Influence}
    <li>Species: {dragon:tsr} dragon
    <li>Gender: {G.gender}
    <li>Age: {dragon:age}
    <li>Eyes: {dragon:eyes}{20% that are {dragon:eye color}}
   </ul>
    
!switch random
  <li><b>Description</b>
  <ul>
    <li>Influence: {dragon:Influence}
    <li>Species: {dragon:species} dragon
    <li>Type: {dragon:breed}
    <li>Gender: {G.gender}
    <li>Age: {dragon:age}
    <li>Alignment: {frequency:some} {alignment}
   </ul>
    
  <li><b>Appearance</b>
  <ul>
    {50%<li>Head: {dragon:head}}
    <li>Eyes: {dragon:eyes}{20%that are {dragon:eye color}}
    <li>Hide: {dragon:hide color} {dragon:hide}
    {25%<li>Legs: {dragon:legs}}
    <li>Wings: {dragon:wings}
    <li>Tail: {dragon:tail}
    {20%<li>Special Cosmetic: {dragon:special cosmetic}}
   </ul>
    
  <li><b>Special Abilities</b>
  <ul>
    <li>Breath: {dragon:breath}
    {25%<li>Special Quality: {dragon:special quality}}
    {10%<li>Special Aura: {dragon:special aura}}
    {10%<li>Special Offense: {special offense}}
    {10%<li>Special Defense: {special defense:natural}}
    {10%<li>Special Defense: {special defense:magical}}
    {10%<li>Special Movement: {SM:=special movement}{SM.movement}: {SM.description}}
  </ul>
  
!switch end
 
  <li><b>Stats</b>
  <ul>
    <li>Hit Dice:
    <li>Initiative:
    <li>Speed:
    <li>AC:
    <li>Attacks:
    <li>Damage:
    <li>Face/Reach:
    <li>Saves:
    <li>Abilities:
    <li>Skills:
  </ul>
  
  <li><b>Personality</b>
  <ul>
    <li>Personality: {personality}
    <li>Attitude: {attitude}
    <li>Feeling: {50%{degree} }{feeling}
    <li>Reaction (to PCs): {reaction}
    {99%<li>Vice: {vice}}
    {99%<li>Favorite Food: {dragon:favorite food}}
  </ul>
 
  <li><b>Quirks</b>
  <ul>
    {99%<li>Rumored Weakness: {dragon:weakness}}
    {99%<li>Quirk: {quirk}}
    {99%<li>Injury: {injury}}
    {99%<li>Phobia: {P:=Phobia}{P.phobia} - {P.description}}
  </ul>
 
  <li><b>Lair / Hoard</b>
  <ul>
    <li>Lair: {dragon:lair}
    <li>Currency: {5000} gp; {10000} sp; {10000} cp; {gem}
  </ul>
</ul>

</body>
</html>
