/ inspired by http://donjon.bin.sh/adnd/magic/shop.html
/ inspired by http://chaoticshiny.com/merchgen.php

<html>
<body>

{M:=merchandise}
<h2>{M.Type} Shop</h2>

<ul>
<li>Merchant
  <ul>
    <li>Proprietor: {Name}; {gender} {race} 
    <li>Affiliation: {none|guild|religious}
    <li>Disposition: {degree} {trait} and {frequency} {disposition}
    <li>Feeling: generally {feeling}, currently {degree} {mood}
    <li>Attitude: {attitude}
    <li>Reputation: {decent|sketchy}
    <li>Current Activity: {merchant:activity}
    <li>Behavior: {merchant:behavior}
  </ul>

<li>Shop
  <ul>
    <li>Stall Type: {market stall} 
    <li>Customers: {NPC}{50%, {NPC}}{50%, {NPC}}{50%, {NPC}}
    <li>Security: {none|nothing obvious}
  </ul>

<li>Merchandise
  <ul>
    <li>Goods: {M.type}  
    <li>Specialty: {M.merchandise}  
    <li>On sale: {M.merchandise}  
    <li>Also Provides: {M.merchandise}  
    <li>Quality: {quality}; Condition: {condition}
    <li>Source: {imported|home-made|local artisan|guild|stolen|unknown}
    <li>Variety/Selection: {{degree} poor|average|{degree} good}
    <li>Price: {{degree} low|moderate|{degree} high}; haggling {haggling}
  </ul>
</ul>

</body>
</html>
