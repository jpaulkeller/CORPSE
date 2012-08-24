{$I Compiler.inc}

(* File: Depends.Pas *)

(* -------------------------------------------------------------- *)
(* Usage  : Depends                                               *)
(* -------------------------------------------------------------- *)

program Depends (input, output);

uses CRT, DOS, Strs, List;

const
  NoMoreFiles = 18;  (* DOS error code *)
  DstName     = 'TABLES\DEPENDS.TBL';

{$I Entries.Typ}  (* for TokenL, TokenR *)

var
  Unique : List.Lists;
  Dst    : text;
  DQty   : integer;
  FQty   : integer;
  TQty   : integer;
  LQty   : integer;
  UQty   : integer;
  Prev   : string;

(* ---------------------------------------------------------------------- *)

procedure Insert_Token (VAR Unique : Lists;
                            Path, Name, Line, Token : string);

  var Numeric : boolean;
      s : string;

  begin
    inc (TQty);

    (* remove FIXED-WIDTH indicators and values *)
    if (Token[1] = EqualsMark) then
      begin
      delete (Token, 1, 1);
      while (Token[1] in [' ', '0'..'9']) do
        delete (Token, 1, 1);
      end;

    s := Token;  (* save original *)
    Numeric := Token[1] in ['?', '0'..'9'];  (* or conditional *)

    if not Numeric then
      inc (LQty);

    if (Token <> Prev) then
      begin
      inc (UQty);
      s := Strs.Pad (s, ' ', 25, true, '+');
      s := Name + s + TokenL + Token + TokenR;
      List.Insert (Unique, s, false);
      Prev := Token;
      end;
  end;

(* ---------------------------------------------------------------------- *)

procedure Check_File (SrcName : string; Tab : string);

  var
    Line  : string;
    Token : string;
    Src   : text;
    Path  : string;
    Name  : string;

  begin
    inc (FQty);
    write (Tab, '  ', SrcName, '...');
    ClrEOL; GoToXY (1, WhereY);
    assign (Src, SrcName); reset (Src);

    Prev   := '';

    Path := copy (SrcName, 1, 255);
    delete (Path, 1, 7);                    (* remove "TABLES\" *)
    delete (Path, length (Path) - 3, 255);  (* remove ".TBL"    *)
    Name := Strs.JustFileName (Path);
    Path := Strs.JustPathName (Path);
    Name := Strs.Pad (Name, ' ', 10, true, '+');

    while not EOF (Src) do
      begin
      readln (Src, Line);

      if ((Line <> '') and
          (Line[1] <> CommentMark) and
          (pos (TokenR, Line) > 0)) then
        begin
        Line  := Strs.Upper (Line);
        Token := Strs.NxtTok (Line, TokenL, TokenR);
        while (Token <> '') do
          begin
          Insert_Token (Unique, Path, Name, Line, Token);
          Line  := Strs.XchText1 (Line, TokenL + Token + TokenR, Token);
          Token := Strs.NxtTok (Line, TokenL, TokenR);
          end;
        end;
      end;  (* while *)

    close (Src);
  end;  (* Check_File *)

(* ---------------------------------------------------------------------- *)

procedure Check_Directory (Path : string; Tab : string);

  var SRec : SearchRec; (* DOS type *)

  begin
    inc (DQty);
    Path := Path + '\';
    write (Tab, Path, '...'); ClrEOL; writeln;
    FindFirst (Path + '*.*', DOS.Directory, SRec);

    while (DosError <> NoMoreFiles) do
      begin
      if (SRec.Attr = DOS.Directory) then
        begin
        if (SRec.Name[1] <> '.') then
          Check_Directory (Path + SRec.Name, Tab + '  ');
        end
      else if (pos ('.TBL', SRec.Name) > 0) and
              (pos (DstName, Path + SRec.Name) = 0) then
        Check_File (Path + SRec.Name, Tab);
      FindNext (SRec);
      end;
  end;

(* ---------------------------------------------------------------------- *)

begin  (* main *)

  DQty := 0;  (* # of directories *)
  FQty := 0;  (* # of files  *)
  TQty := 0;  (* # of tokens (includes numeric and conditional *)
  LQty := 0;  (* # of look-up tokens *)
  UQty := 0;  (* # of unique file/token pairs *)

  assign  (Dst, DstName); rewrite (Dst);
  writeln (Dst);
  writeln (Dst, '? Sources: Created using DEPENDS.EXE');
  writeln (Dst);
  writeln (Dst, '! TABLE     VARIABLE (Look-Up, Numeric, Condition, etc)');
  writeln (Dst);

  writeln;

  Unique := nil;
  Check_Directory ('TABLES', '');
  ClrEOL;
  
  if (Unique <> nil) then
    List.Dump (Dst, Unique);
  writeln (Dst);
  close (Dst);

  writeln;
  writeln (DQty:6, ' directories');
  writeln (FQty:6, ' tables');
  writeln (TQty:6, ' tokens (includes numeric and conditional)');
  writeln (LQty:6, ' look-ups');
  writeln (UQty:6, ' unique table/tokens');
  writeln;

end.  (* program Depends *)

