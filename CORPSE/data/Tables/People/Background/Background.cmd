/ http://random-generator.com/index.php?title=Fantasy_Character_History

<html>
<body>

/ Query for name (using random default); assign and randomize
# {PC={Name?{Name}}}

<h1>{PC}</h1>

<ul>
  <li><b>Character Story</b>
  <ul>
    <li>Legitimacy: [LEGITIMACY]
    <li>Family Head: [FAMILY_HEAD]
    <li>FH's Occupation: [OCCUPATION]
  </ul>
  
  <li><b>Birth Circumstances</b>
  <ul>
    <li>Siblings: [NUMBER_SIBLINGS]<br/>
    <li>Place of Birth: [Fantasy Birth.PLACE_OF_BIRTH]<br/>
    <li>Unusual Birth: [Fantasy Birth.UNUSUAL_BIRTH]<br/>
  </ul>

  <li><b>Life Path</b>
  <ul>
    <li>Childhood Events: {life events:youth}
    <li>Adolescent Events: {life events:youth}
    <li>Adult Events: {life events:adult}
  </ul>
</ul>

</body>
</html>
