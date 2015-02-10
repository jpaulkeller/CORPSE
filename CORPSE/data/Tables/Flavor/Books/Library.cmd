/ http://donjon.bin.sh/fantasy/name/#setting
/ http://random-generator.com/index.php?title=Dusty_Bookshelves
/ http://random-generator.com/index.php?title=ArcaneBooks

<html>
<body>

/ Query for name (using random default); assign and randomize
# {LIBRARY={Library?The Library of {Name}}}

<h1>{LIBRARY}</h1>

!loop {6}
<ul>
  <li>{Shelf|Bookcase}: {bookshelf}

!loop {2d4}
  <ul>
    <li>Title: <b><i>{BOOK}</i></b>
    {75%<li>Author: {AUTHOR}}{10% {AUTHOR:EDITS}} 
    {50%<li>Quality: {book:quality}}
    {33%<li>Condition: {condition:book}}
    {10%<li>Extra: {found in book}}
  </ul>
!loop end
</ul>
!loop end

</body>
</html>
