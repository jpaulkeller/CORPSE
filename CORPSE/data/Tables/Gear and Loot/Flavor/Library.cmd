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
    <li>Title: <b><i>{Book}</i></b>
    {50%<li>Author: {book:author}} 
    {50%<li>Condition: {condition}}
    {50%<li>Quality: {book:quality}}
    {50%<li>Extra: {book:extra}}
  </ul>
!end
</ul>
!end

</body>
</html>
