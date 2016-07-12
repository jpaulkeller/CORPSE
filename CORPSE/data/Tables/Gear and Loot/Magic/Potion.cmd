<html>
<body>

/ Generates a potion, with lots of details, based on the potion type.

/ Generate a random potion.
/ Ask the user for a potion, using the random one as a default.
/ Assign the entered potion to "POT" for re-use later.

{POT={Potion Type?{Potion:Name}}}

<h1>{POT}</h1>
<hr>
<h2>Container</h2>
<ul>
  <li>{container:potions}, holding {2d4} doses
</ul>
 
/ Randomize using the potion as the seed, for consistent results for color, smell, etc:
# {POT}

<h2>Description</h2>
<ul>
 <li>Color: {color}
 <li>Viscosity: {viscosity}{33%, {liquid feature}}{20%, {liquid feature:extra}}
 <li>Smell: {smell}
 <li>Taste: {taste:description}
 <li>Temperature: {20%?{temperature}:ambient}
 {33%<li>Property: {Liquid Feature:Property}}
</ul>
 
<h2>Effects</h2>
<ul>
 <li>Duration (if applicable): {2d10} {duration:brief}s
 <li>Side Effects: {33%?{mild|severe} {symptom} which lasts for {2d3} {duration:moderate}s:none}
</ul>

<h2>Recipe</h2>
<ul>
 <li>{potion recipe:step first}
!loop {2}
 <li>{potion recipe:step add}
!loop end
 <li>{potion recipe:step cook}
!loop {2}
 <li>{potion recipe:step add}
!loop end
 <li>{potion recipe:step last}
</ul>

</body>
</html>
