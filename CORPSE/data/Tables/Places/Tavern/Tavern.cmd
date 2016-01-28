<html>
<body>

<!-- 
/ Ask for the inn name (with a suitable default), and randomize based on that
/ name so that the following data (until the next #) is consistent.
-->
# {INN={Tavern Name?{Inn Name:Name}}}


<!--
{C1:=color hex}
{C2:=color hex}
{C3:=color hex}
-->

<h1>Welcome to <i>The {INN}</i></h1>

<h3>Location</h3>
<ul>
 <li>{TAVERN FLAVOR:LOCATION}
 <li>in {neighborhood}, near the {LANDMARK}
 <li>{direction} the {city feature:site}
 <li>the street outside is {STREET FLAVOR}
</ul>

<h3>Description</h3> 
<ul>
 <li>The inn is a {small|large|{single-|two-|three-}storey}, {TAVERN FLAVOR:LAYOUT} {building material} building, 
     with {building flavor}{20% and {building flavor{!different}}}.
 <li>Accommodations consist of {quantity:some} {accommodations}. 
 <li>The inn is {locally |widely |well-}known for the {tavern flavor}.
 <li>Other services include:
     <ul>
        {50%<li>{tavern flavor:services}}
        {50%<li>{tavern flavor:services}}
        {50%<li>{tavern flavor:services}}
     </ul>
 {25%<li>There is a {smell:synonym pleasant} of {smell:cooking}} 
 <li>Color scheme:
     <table cellpadding="3" cellspacing="3" style="border: 1px solid #000000;">
     <tr>
       <td align=center bgcolor="{C1.hex}" color="{C1.font}"> {C1.name} </td>
       <td align=center bgcolor="{C2.hex}" color="{C2.font}"> {C2.name} </td>
       <td align=center bgcolor="{C3.hex}" color="{C3.font}"> {C3.name} </td>
     </tr>
     </table>
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
!loop end
</ul>  

<!-- Re-randomize (not based on INN name) -->
#

<h3>Rumors</h3>   
<ul>  
!loop {2d3}
 <li>{RUMOR}
!loop end
</ul>  

<h3>Patrons</h3>
<blockquote>
The following people are currently in the common room:
!loop {0-4}
+ NPC
!loop end
!loop {0-4}
{tavern flavor:patron}
!loop end

!loop {Roll for more NPCs?{0 - 4}}
+ NPC
!loop end
</blockquote>

<h3>Chests contain:</h3>
<ul>  
 <li>Gold  : {99} pieces
 <li>Silver: {500} pieces
 <li>Copper: {100-1000} pieces
</ul>  

</body>
</html>
