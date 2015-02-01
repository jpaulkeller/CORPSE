/ http://random-generator.com/index.php?title=Castle

<html>
<body>

/ Query for name (using random default); assign and randomize
# {N={Name?{1000-9999}}}

<h1>Castle {N}</h1>

<ul>
  <li><b>Structure:</b> {50%{site:adj} }{castle}
  <li><b>Construction:</b> {castle:tower construction}
  <li><b>Condition:</b> {site:condition}{20%, now {{site:abandoned}|ruins, destroyed by {disaster}}}
  <li><b>Special:</b> {CASTLE:SPECIAL}
  <li><b>Location:</b> {CASTLE:TOWER LOCATION}
  <li><b>Defenses:</b>
      <ul>
        <li><b>Garrison:</b> Normally, {CASTLE:GARRISON}
        <li><b>Defense:</b> {CASTLE:DEFENSE}
        <li><b>Outer Defense:</b> {CASTLE:OUTER DEFENSE}
        <li><b>Curtain Walls:</b> {CASTLE:CURTAIN WALLS}
        <li><b>Moat:</b> {CASTLE:MOAT}
        <li><b>Traps:</b> {CASTLE:TRAPS}
      </ul>
</ul>

</body>
</html>
