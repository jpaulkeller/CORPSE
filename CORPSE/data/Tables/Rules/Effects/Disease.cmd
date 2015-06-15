<html>
<body>

/ Query for name (using random default); assign and randomize
# {NAME={Name?{Disease Name}}}

<h1>{NAME}</h1>

<ul>
  <li>Infection: {disease name:infection}
  <li>DC: {2d10}
  <li>Symptom: {symptom!}
  <li>Incubation: {2d{6}} {duration:moderate}s
  <li>Damage: -{{4|6|8}} {attribute}
</ul>

</body>
</html>
