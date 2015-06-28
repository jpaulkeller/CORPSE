/ http://clevergirlhelps.tumblr.com/post/89608530869/im-trying-to-develop-the-city-where-my-fictional-story

<html>
<body>

/ Query for name (using random default); assign and randomize
# {NAME={Name?{Town Name}}}

<h1>{NAME}</h1>

<ul>
  <li>Feature
    <ul>
      <li>District: {city flavor:district}
      <li>Resource: {city flavor:resource}
      <li>Terrain: {city flavor:terrain}
!loop {6}
      {50%<li>Feature: {city feature}}
!loop end
    </ul>
  <li>Society
    <ul>
      <li>Ruler: {city flavor:ruler}
      <li>Faith: {city flavor:faith}
      <li>Guild: {city flavor:guild}
    </ul>
  <li>Population
    <ul>
      <li>Population: {d5000+5000}
      <li>Caste: {city flavor:caste}
      <li>Race: {city flavor:race}
      <li>Attitude: {city flavor:attitude}
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
