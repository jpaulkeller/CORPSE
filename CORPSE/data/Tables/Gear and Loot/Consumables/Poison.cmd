<html>
<body>

/ Generates a poison, with lots of details (based on the name).

/ Generate a random poison.
/ Ask the user for a poison, using the random one as a default.
/ Assign the entered poison to "PoisonName" for re-use later.

<h1>POISON: {PoisonName={Poison?{Poison}}}</h1>
<hr>
<p>Container: {container:potions}, holding {2d4} doses
 
/ Randomize using the poison as the seed, for consistent results for color, smell, etc:
# {PoisonName}

<h2>Description</h2>
<ul>
 <li>Form: {Powder|Liquid, {viscosity}{33%?, {liquid feature}}{20%?, with bits of {reagent} floating in it}}
 <li>Color: {color}
 <li>Smell: {smell}
 <li>Taste: {taste}
 <li>Miscibility: {poor|moderate|good}
</ul>
 
<h2>Effects</h2>
<ul>
 <li>Application: {ingested|inhaled|contact|injected}
 <li>Base DC: {20}
 <li>Immediate: {symptom} which lasts for {2d6} {duration:short}s
 <li>Ability: {Attribute} -{8} for {10} {duration:moderate}s
 <li>Long Term: {symptom} which lasts for {10} {duration:moderate}s
</ul>

<h2>Antidote</h2>
<ul>
!loop {2,5}
 <li>{reagent}
!end
</ul>

</body>
</html>
