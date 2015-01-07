/ http://random-generator.com/index.php?title=Cave_generator
/ http://goblinpunch.blogspot.ca
/ http://lizardmandiaries.blogspot.com.au/2013/12/flatter-crikemass-and-his-oily-black.html

<html>
<body>

/ Generate a random name.
/ Ask the user for a name, using the random one as a default.
/ Assign the entered name to "NAME" for re-use later.
/ Randomize using the name as the seed, for consistent results.
# {NAME={Name?{Name}'s {Cave:Name}}}

<p><b>{NAME}</b>

<ul>
  <li>General Description: {cave:general}
  <li>Entrance: {cave:entrance}
!loop {6}
  <li>Linking Tunnel: {cave:tunnel}
  {50%<li>Tunnel Feature: {cave:tunnel feature}}
  <li>Shape: {cave:shape}
  <li>Size: {cave:size}
  <li>Light Source: {cave:light}
  {25%<li>Ceiling: {cave:ceiling}}
  {25%<li>Feature: {cave:feature}}
  {25%<li>Content: {cave:content}}
  <li>Exit(s): {cave:exit}{25%; {cave:exit}}{25%; {cave:exit}}
</ul>
<ul>
!end
</ul>

</body>
</html>
