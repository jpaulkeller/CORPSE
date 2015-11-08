/ duplicate spells will be different versions (components, range, area of effect, etc) 

<html>

<head>
<style>
body {
    color: white;
    background-image:  url(https://41.media.tumblr.com/4d00e9e292e761b296d77b05fd4b8369/tumblr_nvmbgxj8tg1t5z3yho1_1280.jpg);
    background-size:   contain;
    background-repeat: no-repeat;
}
</style>
</head>

<body>

/ Query for name (using random default); assign and randomize
# {BK={Spellbook?{Name}'s {Book:Spellbook}}}

<h1>{BK}</h1>

<ul>
  <li>Format: {book flavor:format}
  <li>Cover: {book flavor:cover}
  <li>Condition: {condition:book}
  {75%<li>Pages: {book flavor:page count}{50%, made of {paper:material}}}
  {50%<li>Binding: {book flavor:binding}}
  {75%<li>Size: {5d4} inches tall and {5d3} inches wide}
  {25%<li>Hardware: {book flavor:hardware}}
  {25%<li>Special: {book flavor:special}}
  {25%<li>Found in the book: {found in book}}
  {90%<li>Protection: {book flavor:protection}}
  {50%<li>Trap: {book flavor:trap}}
  
  <p>
  <li>Standard Spells {SC:=Spell Category}(<b>{SC}</b>)
  <ul>
!loop {2d10}
    <li>{Spell:{SC}}
!loop end
  </ul>
  
  <p>
  <li>Unique Spells
  <ul>
!loop {d6-1}
    <li>{Spell Name}
!loop end
  </ul>
</ul>

</body>
</html>
