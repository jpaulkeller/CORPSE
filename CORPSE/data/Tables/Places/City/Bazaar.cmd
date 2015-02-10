/ inspired by http://chaoticshiny.com/bazaargen.php

<html>
<body>

# {MARKET={Market?{Landmark} Market}}

<h2>{MARKET}</h2>

<br>Location: {direction} the {city site}
<br>Traffic: {degree} {sparse|heavy}
<br>{Quantity} guards; {quantity:few} pick-pockets; {quantity} beggars 
<br/>

<hr>

!loop {2d4}
+ merchant
!loop end

</body>
</html>
