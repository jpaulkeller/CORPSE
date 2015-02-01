<html>
<body>

/ Generate a random name.
/ Ask the user for a name, using the random one as a default.
/ Assign the entered name to "NAME" for re-use later.
/ Randomize using the name as the seed, for consistent results.
<!--
{SITE={site}}
-->
# {NAME={Name?{Name}'s {SITE}}}

<p><b>{NAME}</b>

<ul>
  <li>Description:
  {25%<li>Abandoned: {site:abandoned}}
  <li>Defenses:
  <li>Food
  <li>Water: {80%?{campsite:water source}:none apparent
  <li>Features: {proximity} {campsite:feature}
 </ul>

</body>
</html>
