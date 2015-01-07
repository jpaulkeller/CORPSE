<!--
/ Strange creatures with special features.  These are typically unique creatures, created or transformed
/ (intentionally or not) by some magic or sorcery.
/ http://random-generator.com/index.php?title=Beasts
-->
 
<html>
<body>

/ Generate a random name.
/ Ask the user for a name, using the random one as a default.
/ Assign the entered name to "NAME" for re-use later.
/ Randomize using the name as the seed, for consistent results.
# {NAME={Name?{Monster Name}}}

<p><b>{NAME}</b>

<ul>
  <li>Description: {monstrosity}
  {25%<li>Form: {monstrosity:form}}
  {33%<li>Hide: {monstrosity:hide}}
!loop {2d2-2}
  <li>Special Offense: {special offense}
!end
!loop {2d2-2}
  <li>Special Defense: {special defense}
!end
  <li>Special Movement: {special movement}
  {25%<li>Other: {monstrosity:special other}}
  <li>Disposition: {monstrosity:disposition}
 </ul>

</body>
</html>
 