/ http://random-generator.com/index.php?title=Female_Appearance

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
  <li>Complexion: {skin} 
  <li>Eyes: {eyes}
  {10%<li>Feature: {physical feature}}
!loop {2d2-2}
  <li>Marking: {markings}
!loop end

!switch {G.gender}

!switch male
  <li>Hair: {hair:male}
  <li>Facial Hair: {hair:facial}
  <li>Build: {build:male}
    
!switch female
  <li>Hair: {hair:female}
  <li>Build: {build:female}
  <li>Wearing: <font color="{C.font}" bgcolor="{C.hex}">{C.name}-colored</font> {75%{condition:clothing} }{female fine}}
  
!switch end

</ul>

</body>
</html>
