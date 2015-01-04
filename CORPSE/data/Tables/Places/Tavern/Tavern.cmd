<html>
<body>

<!-- 
/ Ask for the inn name (with a suitable default), and randomize based on that
/ name so that the following data (until the next #) is consistent.
-->
# {INN={Tavern Name?{Inn Name}}}

<h1>Welcome to <i>The {INN}</i></h1>

<h3>Location</h3>
<ul>
 <li>on {Street}
 <li>in {neighborhood}, near the {LANDMARK}
 <li>{direction} the {city site}
 <li>The street outside is {street flavor}.
</ul>

<h3>Description</h3> 
<ul>
 <li>The inn is a {small|large|{single-|two-|three-}storey} {building material} building, 
     with {building flavor}{20% and {building flavor{!different}}}.
 <li>Accommodations consist of {quantity:some} {accommodations}. 
 <li>The inn is {locally |widely |well-}known for the {tavern flavor}.
</ul>

<h3>Staff</h3>
<blockquote>
<dl>
 <dt>Innkeeper: <b>{Name}</b>
 <dd>{gender} {race}, who is {degree} {trait} and {frequency} {trait}, and who seems {degree} {mood}
</dl>
</blockquote>

<h3>Menu</h3>
<ul>  
!loop {2d3}
  <li>{menu}
!end
</ul>  

<!-- Re-randomize (not based on INN name) -->
#

<h3>Rumors</h3>   
<ul>  
!loop {2d3}
 <li>{RUMOR}
!end
</ul>  

<h3>Patrons</h3>
<blockquote>
The following people are currently in the common room:
!loop {0-4}
+ NPC
!end

!loop {Roll for more NPCs?{0 - 4}}
+ NPC
!end
</blockquote>

<h3>Chests contain:</h3>
<ul>  
 <li>Gold  : {99} pieces
 <li>Silver: {500} pieces
 <li>Copper: {100-1000} pieces
</ul>  

</body>
</html>
