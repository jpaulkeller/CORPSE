/ http://random-generator.com/index.php?title=Fantasy_Adventure_Generator
/ http://random-generator.com/index.php?title=Fantasy_Situation_Generator 

<html>
<body>

/ Query for seed (using random default); assign and randomize
# {ADV={Adventure?{1000-9999}}}

<h1>Adventure: {ADV}</h1>

<ul>
  <li><b>What:</b> 
      <ul>
        <li>theme: A {adventure:theme} tale
        <li>goal: The party attempts to {adventure:goal}
        <li>plot: The basic plot is {adventure:plot}
        <li>climax: {adventure:climax}
        <li>Situation: [problem]
        <li>[whatelse]
        <li>Theme: {adventure:theme}
        <li>Inspiration: {Inspiration}
        <li>Goal: {adventure:goal}
        <li>Quest: {QUEST:TASK}{25% {quest:without}}{25% {quest:before}}{25%, {quest:or}}
        <li>Plot: {adventure:plot}
        <li>Climax: {adventure:climax}
        <li>Fight: {adventure:monster encounter}
        <li>Deal With: {adventure:character encounter}
        <li>Trap: {adventure:trap}
      </ul>
      
  <li><b>Why</b>
      <ul>
        <li>Motivation: {Motivation}
        <li>Hook: {Hook} [involved]
        <li>Kickoff: {Kickoff}
      </ul>

  <li><b>Where:</b>
      <ul>
        <li>primary: It mostly takes place {adventure:general setting} 
        <li>with key events also at {adventure:specific setting} and {adventure:specific setting}
        <li>Venue: {adventure:venue}
        <li>Specific Setting: {adventure:specific setting}
        <li>Specific Setting: {adventure:specific setting}
      </ul>
      
  <li><b>Who:</b>
      <ul>
        <li>aided by {adventure:allies neutral}
        <li>opponent: {ADVENTURE:MASTER VILLAIN}
        <li>minions: the {adventure:minor villain} and the {adventure:minor villain}
        <li>weakness: {adventure:villain weakness}
        <li>Protagonist: {Protagonist}
        <li>Antagonist: {Antagonist}
        <li>Minions: {adventure:minor villain}
        <li>Affected: [affecting]
        <li>Allies: {adventure:allies}
        <li>Others: [whoelse]
      </ul>
      
  <li>Obstacles: Along the way, they will have to:
      <ul>
        <li>fight a {adventure:monster encounter}
        <li>deal with a {adventure:character encounter} 
        <li>avoid getting caught in a {adventure:trap}
        <li>avoid falling for the: {adventure:red herring}
      </ul>
      
  <li>Misc:
      <ul>
        <li>special condition: {adventure:special condition}
        <li>omen: {adventure:omen}
        <li>quandary: {adventure:quandary}
        <li>complication: {adventure:complication}
        <li>Twist: {plot twist}
        <li>Disaster Cataclysm
      </ul>
</ul>

</body>
</html>
