<html>
<body>

/ Ask the user for a name, using the random one as a default.
/ Assign the entered name to "N" for re-use later.
/ Randomize using the name as the seed, for consistent results.
# {N={Name?{Name}}}

<!--
{G:=gender}
{C:=color hex}
-->

<p><b>{N}</b>

<ul>
  <li>Gender: {G.gender}
  <li>Overall Impression: {appearance}
  <li>Age: {age}
  <li>Height: {short|average|tall}
  <li>Build: {build}
  <li>Complexion: {skin} 
  <li>Eyes: {eyes}
  <li>Hair: {hair}
!loop {2d2-2}
  <li>Marking: {markings}
!loop end
  <li>Wearing: <font color="{C.font}" bgcolor="{C.hex}">{C.name}-colored</font> {75%{condition:clothing} }{~{clothing}}
/ sample: 
</ul>

</body>
</html>
