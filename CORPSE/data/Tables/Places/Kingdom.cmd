<html>
<body>

/ http://www222.pair.com/sjohn/blueroom/demog.htm

# {Kingdom={Kingdom?{Place Name}}}

<!--
{Square Miles={Square Miles?{50000, 200000}}}
{Population Density={Population Density (per square mile)?{50, 120}}}
/ alternate {Population Density={Population Density (per square mile)?{6d4x{5}}}}
{Population={={Population Density}*{Square Miles}}}
-->

<h1>Kingdom: {Kingdom}</h1>
<p>Square Miles: {Square Miles}
<p>Population Density: {Population Density} per square mile
<p>Population: {Population}
<p>A hex is typically 30 miles across, which would be ~780 square miles.

<!--
{P={={Population}^0.5}} / square root of the total population
{M={2d4+10}}
{P={={P}*{M}}}
-->

<p>Largest City: {P}

!loop 4
<!-- {P={={P}*0.{2d4}}} next largest city is 20-80% of the previous one -->
<p>Next City: {P}
!end

</body>
</html>
