<html>
<body>

/ Query for name (using random default); assign and randomize
# {NAME={Name?{Disease Name}}}

<h1>{NAME}</h1>

<ul>
  <li>Victims: {20%{young|old} }{20%{male|female} }{humanoid|{{humanoid}+}}
  <li>Cause: {{disease:cause|disease:vector}}
  <li>Contagious: {disease:contagious}
  <li>DC: {2d10}
  <li>Incubation: {2d{6}} {duration:moderate}s
  <li>Symptom: {50%{degree:severity} }{symptom!}
  <li>Effects: {d{4|6|8} damage|-{6} {attribute}}
  {25%<li>Special: {DISEASE:SPECIAL}}
  <li>Duration: {2d{4}} {duration:broad}s
  <li>Course: {disease:course}
  <li>Treatment: {33%{relatively|moderately|very} }{simple|difficult}
</ul>

<p>When a character is injured by a contaminated attack (such as a mummy's slam attack, which can transmit mummy rot), 
touches an item smeared with diseased matter, or consumes disease-tainted food or drink, he must make an immediate 
Fortitude saving throw. If he succeeds, the disease has no effect; his immune system fought off the infection. 
If he fails, he takes damage after an incubation period. Once per day afterward, he must make a successful Fortitude
saving throw to avoid repeated damage. Two successful saving throws in a row indicate that he has fought off the disease
and recovers, taking no more damage.

<p>You can roll these Fortitude saving throws for the player so that he doesn't know whether the disease has taken hold.
With the wide range of methods disease can use to ravage creatures and people, plagues can take many forms: 
fast-moving viruses, flesh-eating bacteria, slow-developing strains that focus on the young or only men or only one race.

<p>The effects of the disease can be loaded with symbolic meaning, branding the sick physically, robbing them 
of the ability to create children, or deranging them mentally.

<p>Wide-spread death would bring economic ruin, refugees, and other lamentations.

<p>Since the concept of viruses and bacteria don't fit your typical fantasy realm, all manner of scapegoats 
would be found: bad sacrifices, bad behaviors, evil people, or unclean races.

<p>The disease could become an epidemic or even a pandemic.

</body>
</html>
