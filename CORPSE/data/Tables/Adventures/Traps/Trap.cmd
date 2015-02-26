<html>
<body>

/ Query for name (using random default); assign and randomize
# {TRAP={Trap Name?{Trap:Name}}}
{TYPE={trap:type}}

<h1>{TRAP}</h1>

<ul>
  <li>Type: {TYPE}
  <li>Trigger: {trap:trigger}
  <li>Effect: {trap:{TYPE} result}
  <li>Reset:
  <li>Bypass:
  <li>Search/Disable:
  <li>Special: {trap:special}
</ul>

</body>
</html>
