{$I Compiler.inc}

(* File: Tables.Pas *)

(* -------------------------------------------------------------- *)
(* Usage  : Tables                                                *)
(* -------------------------------------------------------------- *)

program TableLister (input, output);

uses DOS, TPCRT, TpString, Strs, List;

{$I Entries.Typ}  (* types for various file entry records *)

const
  NoMoreFiles = 18;  (* DOS error code *)
  DstName     = 'TABLES\TABLES.TBL';

var
  Lst  : List.Lists;
  Dst  : text;
  DQty : integer;
  FQty : integer;

(* ---------------------------------------------------------------------- *)

procedure Do_File (SrcName : string; Tab : string);

  var
    Line : string;
    Src  : text;
    Path : string;
    Name : string;
    s    : string;
    Done : boolean;
    MatchingValue : Values;

  begin
    inc (FQty);
    write (Tab, '  ', SrcName, '...');
    ClrEOL; GoToXY (1, WhereY);
    assign (Src, SrcName); reset (Src);

    Path := copy (SrcName, 1, 255);
    delete (Path, 1, 7);                    (* remove "TABLES\" *)
    delete (Path, length (Path) - 3, 255);  (* remove ".TBL"    *)
    Name := TpString.JustFileName (Path);
    Path := TpString.JustPathName (Path);
    Path := Strs.Pad (Path, ' ', 20, true, '+');
    Name := Strs.Pad (Name, ' ', 10, true, '+');
    s    := Name + Path;

    Done := false;
    while not (EOF (Src) or Done) do
      begin
      readln (Src, Line);
      if ((Line <> '') and (Line[1] = HeaderMark)) then
        begin
        Line[1] := ' ';
        Line := Strs.Trim (Line);
        s := Name + Path + Line;
        Done := true;
        end;
      end;  (* while *)

    if (List.Match (Lst, Name, MatchingValue) = 1) then
      begin
      Name := Strs.PopWord (MatchingValue, ' ');
      Path := Strs.PopWord (MatchingValue, ' ');
      TextColor (LightMagenta);
      write ('** Warning: duplicate name: ', Path, '\', Name);
      TextColor (Yellow);
      ClrEOL;
      writeln;
      end;

    List.Insert (Lst, s, true);

    close (Src);
  end;  (* Do_File *)

(* ---------------------------------------------------------------------- *)

procedure Do_Directory (Path : string; Tab : string);

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
          Do_Directory (Path + SRec.Name, Tab + '  ');
        end
      else if (pos ('.TBL', SRec.Name) > 0) and
              (pos (DstName, Path + SRec.Name) = 0) then
        Do_File (Path + SRec.Name, Tab);
      FindNext (SRec);
      end;
  end;

(* ---------------------------------------------------------------------- *)

begin  (* main *)

  DQty := 0;  (* # of directories *)
  FQty := 0;  (* # of files  *)

  assign  (Dst, DstName); rewrite (Dst);
  writeln (Dst);
  writeln (Dst, '? Source: Created using Tables.EXE');
  writeln (Dst);
  writeln (Dst, '! TABLE     PATH                DESCRIPTION');
  writeln (Dst);

  writeln;

  Lst := nil;
  TextColor (Yellow);
  Do_Directory ('TABLES', '');
  ClrEOL;
  
  if (Lst <> nil) then
    List.Dump (Dst, Lst);
  writeln (Dst);
  close (Dst);

  writeln;
  writeln (DQty:6, ' directories');
  writeln (FQty:6, ' tables');
  writeln;

end.  (* program TableLister *)

