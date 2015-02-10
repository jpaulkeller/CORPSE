<html>
<body>

<ul>
! switch {leather|heavy|special|mixed}
! leather
  <li>a matched suit of {armor flavor:leather armor}
  {10%<li>Head: {helm}}
  {10%<li>Shield: {shield}}
! heavy
  <li>a matched suit of {armor flavor:heavy armor}
  {50%<li>Head: {helm}}
  {33%<li>Shield: {shield}}
! special
  <li>a matched suit of {armor flavor:special armor}
! mixed
  <li>Head: {helm} 
  <li>Upper: {armor flavor:random armor} 
  <li>Lower: {armor flavor:random armor} 
  <li>Hands: {armor flavor:random armor} 
! end switch
  <li>Size: {3d4}
  <li>Condition: {condition:clothing}
  <li>Quality: {quality:weapon}
</ul>

</body>
</html>
