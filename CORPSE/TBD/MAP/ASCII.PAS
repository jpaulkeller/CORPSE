
program chars (input, output);
var F : text;
    i : integer;
begin
  assign (F, 'ASCII.DOC'); rewrite (F);
  for i := 1 to 255 do
    writeln (F, i:3, ' ', chr(i));
  close (F);
end.
