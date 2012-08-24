
UNIT Util;

{$I-,O+,F+,V-,X+,S-}

INTERFACE

{$I Color.Inc}

function Min  (X, Y: Integer): Integer;
function Max  (X, Y: Integer): Integer;
function Attr (Background, Foreground : byte) : byte;

IMPLEMENTATION

function Min(X, Y: Integer): Integer; assembler;
asm
        MOV     AX,X
        CMP     AX,Y
        JLE     @@1
        MOV     AX,Y
@@1:
end;

function Max(X, Y: Integer): Integer; assembler;
asm
        MOV     AX,X
        CMP     AX,Y
        JGE     @@1
        MOV     AX,Y
@@1:
end;

function Attr (Background, Foreground : byte) : byte;
begin
  Attr := (Background * 16) + Foreground;
end;

end.  (* unit Util *)
