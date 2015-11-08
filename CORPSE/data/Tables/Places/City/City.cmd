/ http://clevergirlhelps.tumblr.com/post/89608530869/im-trying-to-develop-the-city-where-my-fictional-story
/ http://rangen.co.uk/world/citygen.php

<html>
<body>

/ Query for name (using random default); assign and randomize
# {NAME={Name?{Town Name}}}

<h1>{NAME}</h1>

<ul>
  <li>Overview
    <ul>
      <li>City reputation: {Terrible|Poor|Fair|Good|Excellent}
      <li>Financial status:	{In debt|Comfortable|Well-off}
      <li>Employment prospects: {Terrible|Poor|Fair|Good|Excellent}
      <li>Public services: {50%{Few|Many} and }{Terrible|Poor|Fair|Efficient|Excellent}
      <li>Crime rate: {Almost none|Low|Moderate|High|Extreme}
      <li>Cost of living: {Cheap|Affordable|Expensive}
    </ul>
    
  <li>Society
    <ul>
      <li>Ruler: {city flavor:ruler}
      <li>Faith: {city flavor:faith}
      <li>Guild: {city flavor:guild}
    </ul>
    
  <li>Population
    <ul>
      <li>Population: {d5000+5000}{50% {growing|shrinking}{33% {slowly|rapidly}}}
      <li>Caste: {city flavor:caste}
      <li>Race: {city flavor:race}
      <li>Attitude: {city flavor:attitude}
      <li>Level of contentment: {Displeased|Dissatisfied|Disillusioned|Apathetic|Content|Proud}
      <li>Diversity: {Indigenous only|Mixed|Multi-cultural|Diverse}
      <li>Attitudes towards visitors: {Prejudiced|Intolerant|Indifferent|Tolerant|Welcoming|Friendly}
      <li>Regional accent: {Neutral|Mild|Unintelligible|Distinctive|Famous}
    </ul>
    
  <li>Appearance
    <ul>
      <li>Overall: {Cramped|Drab|Flat|Gloomy|Spacious|Towering}
      <li>General construction level: {Quality}
      <li>General level of upkeep: {Condition}
      <li>General street condition: {Condition}
    </ul>
    
  <li>Features
    <ul>
      <li>District: {city flavor:district}
      <li>Resource: {city flavor:resource}
      <li>Terrain: {city flavor:terrain}
!loop {6}
      {50%<li>Feature: {city feature}}
!loop end
    </ul>
    
  <li>Trouble
    <ul>
      <li>Rumour: {city flavor:rumour}
      <li>Threat: {city flavor:threat}
      <li>Unrest: {city flavor:unrest}
    </ul>
</ul>

</body>
</html>
