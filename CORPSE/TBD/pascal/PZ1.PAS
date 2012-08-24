program p1 (input, output);

const N = 10000;
var max : integer;
    lo, hi, sum : integer;

begin
  writeln ('Puzzle 1');
  lo := 1;
  hi := 1;
  max := N DIV 2;
  for hi := 1 to max do
    begin
    inc (sum, hi);
    while (sum > N) do
      begin
      dec (sum, lo);
      inc (lo);
      end;
    if (sum = N) then
      writeln ('FOUND: ', lo:1, ' ', hi:1);
    end;
end.