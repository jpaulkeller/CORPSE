
{$I Compiler.inc}

(* --------------------------------------------------------------------- *)
(* File: Strs.Pas *)
(* --------------------------------------------------------------------- *)

UNIT Strs;

INTERFACE

(* --------------------------------------------------------------------- *)

TYPE
  Symbols    = ^SymbolRecs;
  SymbolRecs = record
                 Key : string;
                 Val : string;
                 Nxt : Symbols;
               end;

(* ---------------------------------------------------------------------- *)

function  IsUpper  (    c : char                            ): boolean;
function  IsLower  (    c : char                            ): boolean;
function  IsAlpha  (    c : char                            ): boolean;
function  LowerCase(VAR s : string                          ): string;
function  UpperCase(VAR s : string                          ): string;
function  Compress (    s : string; Max : integer           ): string;
function  PopWord  (VAR s : string; WordSepr : char         ): string;
function  SubStr   (    s, FromStr, ToStr : string          ): string;
function  NxtTok   (    s : string; Opn, Cls : char         ): string;
function  Trim     (    s : string                          ): string;
function  Truncate (    s : string; MaxWidth : integer      ): string;
function  Upper    (    s : string                          ): string;

function  XchText1 (    s, Old, New : string                ): string;
function  XchText  (    s, Old, New : string                ): string;

function  Or_      (    s1, s2 : string                     ): string;
function  Pad      (    s : string; Fill : char; Len : integer;
                        Truncate : boolean; Overflow : char ): string;
function  PadNum   (    s : string; Fill : char; Len : integer;
                        Truncate : boolean; Overflow : char ): string;
function  StrF     (    i : longint                         ): string;
function  ValF     (    s, alternate : string               ): longint;

procedure SymPut   (VAR SymTbl : Symbols; Key, Val : string );
function  SymGet   (VAR SymTbl : Symbols;
                        Key    : string;
                    VAR Val    : string                     ): boolean;
procedure SymFree  (VAR SymTbl : Symbols                    );

  {--------------- DOS pathname parsing -----------------}

function JustFilename (PathName : string) : string; {d:\path\FILE.EXT}
function JustName     (PathName : string) : string; {d:\path\FILE.ext}
function JustExtension(Name     : string) : string; {d:\path\file.EXT}
function JustPathname (PathName : string) : string; {D:\PATH\file.ext}

function ExistFile(FName : string) : Boolean;
  {-Return true if file is found}

function DefaultExtension(Name, Ext : string) : string;
  {-Return a file name with a default extension attached}

function ForceExtension(Name, Ext : string) : string;
  {-Force the specified extension onto the file name}

function AddBackSlash(DirName : string) : string;
  {-Add a default backslash to a directory name}

function CleanPathName(PathName : string) : string;
  {-Return a pathname cleaned up as DOS will do it}

function FullPathName(FName : string) : string;
  {-Given FName (known to exist), return a full pathname}

(* --------------------------------------------------------------------- *)

IMPLEMENTATION

USES Dos;

const
  DosDelimSet : set of Char = ['\', ':', #0];

(* --------------------------------------------------------------------- *)

function IsUpper (c : char) : boolean;
begin
  IsUpper := (c >= 'A') and (c <= 'Z');
end; (* IsUpper *)

(* --------------------------------------------------------------------- *)

function IsLower (c : char) : boolean;
begin
  IsLower := (c >= 'a') and (c <= 'z');
end; (* IsLower *)

(* --------------------------------------------------------------------- *)

function IsAlpha (c : char) : boolean;
begin
  IsAlpha := IsUpper (c) or IsLower (c);
end; (* IsAlpha *)

(* --------------------------------------------------------------------- *)
(* Destructively lower-cases the given <s>. *)

function LowerCase (VAR s : string) : string;
var i : integer;
begin
  for i := 1 to length (s) do
    if IsUpper (s[i]) then
      s[i] := chr (ord(s[i]) + (ord('a') - ord('A')));
  LowerCase := s;
end;  (* LowerCase *)

(* --------------------------------------------------------------------- *)
(* Destructively upper-cases the given <s>. *)

function UpperCase (VAR s : string) : string;
var i : integer;
begin
  for i := 1 to length (s) do
    if IsLower (s[i]) then
      s[i] := chr (ord(s[i]) - (ord('a') - ord('A')));
  UpperCase := s;
end;  (* UpperCase *)

(* ---------------------------------------------------------------------- *)
(* Non-destructively upper cases the given <s>. *)

function Upper (s : string) : string;
var i : integer;
begin
  for i := 1 to Length (s) do
    s[i] := UpCase (s[i]);
  Upper := s;
end;  (* Upper *)

(* --------------------------------------------------------------------- *)

function Or_ (s1, s2 : string) : string;
  begin
    if (s1 = '') then Or_ := s2
    else              Or_ := s1;
  end;  (* Or_ *)

(* --------------------------------------------------------------------- *)
(* Append <Fill> to right side of <s> until <Len> is reached.            *)
(* <Overflow> should be ' ' or something like '+' to indicate overflow.  *)

function Pad (s        : string;
              Fill     : char;
              Len      : integer;
              Truncate : boolean;
              Overflow : char) : string;

  var i      : integer;
      CurLen : integer;

  begin
    CurLen := length (s);
    if (CurLen > Len) and Truncate then
      begin
      s := copy (s, 1, Len);  (* <s> too long; truncate *)
      if (Overflow <> ' ') then s[Len] := Overflow;
      end
    else if (CurLen < Len) then
      for i := CurLen + 1 to Len do s := s + Fill;  (* pad right *)
    Pad := s;
  end;  (* Pad *)

(* --------------------------------------------------------------------- *)
(* Same as Pad, but right-justifies for numeric data *)

function PadNum (s        : string;
                 Fill     : char;
                 Len      : integer;
                 Truncate : boolean;
                 Overflow : char) : string;

  var i      : integer;
      CurLen : integer;

  begin
    CurLen := length (s);
    if (CurLen > Len) and Truncate then
      begin
      s := copy (s, 1, Len);  (* <s> too long; truncate *)
      if (Overflow <> ' ') then s[Len] := Overflow;   
      end
    else if (CurLen < Len) then
      for i := CurLen + 1 to Len do s := Fill + s;  (* pad left *)
    PadNum := s;
  end;  (* PadNum *)

(* --------------------------------------------------------------------- *)

function SubStr (s, FromStr, ToStr : string) : string;

  (* Returns the substring in <s> between <FromStr> and <ToStr>, non-   *)
  (* inclusive.  If <FromStr> is not found, '' is returned.  If <ToStr> *)
  (* is empty or not found, then the rest of the string is returned. If *)
  (* <FromStr> is empty, then the start position defaults to 1.         *)

  var FromPos, ToPos : integer;
  begin
    if (Length (s) = 0) then SubStr := ''
    else
      begin
        if (Length (FromStr) = 0) then FromPos := 1
        else FromPos := Pos (FromStr, s);
        if (FromPos = 0) then SubStr := ''
        else
          begin
            Delete (s, 1, FromPos + Length (FromStr) - 1);
            if (ToStr = '') then ToPos := 0
            else ToPos := Pos (ToStr, s);
            if (ToPos > 0) then       (* delete to end of string  *)
              Delete (s, ToPos, 255); (* Length (s) - ToPos + 1 *)
            SubStr := s;
          end;
      end;
  end;  (* SubStr *)

(* --------------------------------------------------------------------- *)
(* Extracts a delimited (possibly nested) token from the given string.   *)

function NxtTok (s : string; Opn, Cls : char) : string;
  var Lt, Rt : integer;
  begin
    Rt := pos (Cls, s);
    Lt := Rt - 1;
    while ((Lt > 1) and (s[Lt] <> Opn)) do Dec (Lt);
    if (Lt < 1) or (s[Lt] <> Opn) then NxtTok := ''
    else NxtTok := copy (s, Lt + 1, Rt - (Lt + 1));
  end;  (* NxtTok *)

(* --------------------------------------------------------------------- *)

function Trim (s : string) : string;

  (* Removes all leading and trailing blanks from <s> *)

  begin
    while (Length (s) > 0) and (s[1] = ' ') do Delete (s, 1, 1);
    if (Length (s) > 0) then
      while (s [ Length (s) ] = ' ') do Delete (s, Length (s), 1);
    Trim := s;
  end;  (* Trim *)

(* --------------------------------------------------------------------- *)

(* Removes all leading, trailing and redundant embedded blanks from <s> *)
(* If Max > 0, then the returned string is truncated if necessary. *)

function Compress (s : string; Max : integer) : string;

  var i : integer;
  begin
    if (s <> '') then
      while (s[1] = ' ') do Delete (s, 1, 1);
    if (s <> '') then
      while (s [ Length (s) ] = ' ') do Delete (s, Length (s), 1);
    if (Length (s) >= 4) and    (* extra spaces are possible *)
       (Pos ('  ', s) > 0) then (* extra spaces are found    *)
      for i := Length (s) - 2 downto 2 do
        if (s[i] = ' ') and (s[i+1] = ' ') then Delete (s, i, 1);
    if (Max > 0) and (Length (s) > Max) then
      s := copy (s, 1, Max);  (* <s> too long; truncate *)
    Compress := s;
  end;  (* Compress *)

(* --------------------------------------------------------------------- *)

function ExchangeText (s, New : string; From, Len : integer) : string;

  (* Exchanges <Len> bytes of <s> at <From> with <New> *)

  begin
    ExchangeText := copy (s, 1, From - 1) + New + copy (s, From + Len, 255);
  end;  (* ExchangeText *)

function XchText1 (s, Old, New : string) : string;

  (* Exhanges the first occurrence of <Old> in <s> with <New> *)

  begin
    XchText1 := ExchangeText (s, New, pos (Old, s), length (Old));
  end;  (* XchText1 *)

function XchText (s, Old, New : string) : string;

  (* Exchanges all occurrences of <Old> in <s> with <New> *)

  var From, Len : integer;
  begin
    if (s <> '') then
      begin
        Len := length (Old);
        repeat
          From := pos (Old, s);
          if (From > 0) then
            s := ExchangeText (s, New, From, Len);
        until (From = 0);
      end;  (* if valid input *)
    XchText := s;
  end;  (* XchText *)

(* --------------------------------------------------------------------- *)

function PopWord (VAR s : string; WordSepr : char) : string;

  (* Destructively removes (and returns) the first word from <s>.       *)
  (* The returned word is trimmed (i.e, all leading and trailing Seprs  *)
  (* are removed); and leading WordSeprs are removed from <s> also.     *)
  (* A word is the string of chars from the 1st non-WordSepr upto but   *)
  (* not including the next WordSepr.  WordSepr is usually a space.     *)
  (* For example, the code:                                             *)
  (*    S := '  This  is a phrase.';                                    *)
  (*    W := Str_PopWord (S, ' ');                                      *)
  (* would set W to 'This' and S to 'is a phrase.'                      *)

  const Quote = '"';

  var SeprPos : integer;
      Word    : string;

  procedure StripLeadingSeprs;
    begin
      if (s <> '') then  (* remove leading separators *)
        while (s[1] = WordSepr) and (Length (s) > 0) do
          Delete (s, 1, 1);
    end;  (* StripLeadingSeprs *)

  begin  (* PopWord *)

    StripLeadingSeprs;
    if (Length (s) > 0) then
      begin
        SeprPos := Pos (WordSepr, s);
        if (SeprPos = 0) then  (* last word *)
          begin
            Word := s;
            s    := '';
          end

        else if (s[1] = Quote) and (* quoted phrase with embedded seprs *)
                (SeprPos < Pos (Quote, copy (s, 2, 255)) + 1) then
          begin
            Delete (s, 1, 1);
            Word := '';
            while (s [1] <> Quote) do
              begin Word := Word + s[1]; Delete (s, 1, 1); end;
            Delete (s, 1, 1);  (* end Quote *)
            while (s <> '') and (s[1] <> WordSepr) do
              begin Word := Word + s[1]; Delete (s, 1, 1); end;
            StripLeadingSeprs;
          end

        else  (* pop word *)
          begin
            Word := copy (s, 1, SeprPos - 1);
            Delete (s, 1, SeprPos);
            StripLeadingSeprs;
          end;  (* else *)
      end  (* if s *)
    else
      Word := '';
    PopWord := Word;
  end;  (* PopWord *)

(* --------------------------------------------------------------------- *)

function Truncate (s : string; MaxWidth : integer) : string;

  (* Truncates <s> so that it is at most MaxWidth chars long, *)
  (* and so that the truncation is done at a smart location.  *)

  var NewStr : string;
      Len    : integer;
  begin
    if (Length (s) > MaxWidth) then
      begin
        NewStr := copy (s, 1, MaxWidth);  (* drop excess *)
        Len := MaxWidth;
        while (Len > (MaxWidth DIV 2)) and
              (NewStr [Len] in ['A'..'Z', 'a'..'z', '0'..'9']) do
          begin
          Delete (NewStr, Len, 1);  (* delete last char *)
          Dec (Len);
          end;
        if (Len <= (MaxWidth DIV 2)) then  (* no good break point  *)
          s := copy (s, 1, MaxWidth)       (* so break at MaxWidth *)
        else s := NewStr;
      end;  (* if too long *)
    Truncate := s;
  end;  (* Truncate *)

(* --------------------------------------------------------------------- *)

function StrF (i : longint) : string;
  (* Functional inteface to Turbo STR procedure. *)
  var s : string;
  begin
    str (i, s);
    StrF := s;
  end;  (* StrF *)

(* --------------------------------------------------------------------- *)

function ValF (s, Alternate : string) : longint;

  (* Functional interface to Turbo VAL procedure.  If <s> is not valid, *)
  (* <Alterante> is used.  If <Alternate> is not valid, this fn HALTS.  *)

  var Long : longint;
      Code : integer;  (* error code *)
  begin
    val (s, Long, Code);
    if (Code <> 0) then
      begin
        val (Alternate, Long, Code);
        if (Code <> 0) then (* Invalid ValF call *)
          ValF := -1
        else if (Long > 65535) then (* ValF result out of range *)
          ValF := -1;
      end;  (* try alternate *)
    ValF := Long;
  end;  (* ValF *)

(* --------------------------------------------------------------------- *)

procedure SymPut (VAR SymTbl : Symbols; Key, Val : string);
  var Symbol : Symbols;
  begin
    new (Symbol);
    Symbol^.Key := Key;
    Symbol^.Val := Val;
    Symbol^.Nxt := SymTbl; SymTbl := Symbol; (* push *)
  end;  (* SymPut *)

(* --------------------------------------------------------------------- *)

function SymGet (VAR SymTbl : Symbols;
                     Key    : string;
                 VAR Val    : string) : boolean;
  var Symbol : Symbols;
  begin
    Symbol := SymTbl;
    while ((Symbol <> nil) and (Symbol^.Key <> Key)) do
      Symbol := Symbol^.Nxt;
    if (Symbol <> nil) then
      Val := Symbol^.Val
    else
      Val := '';
    SymGet := (Symbol <> nil);
  end;  (* SymGet *)

(* --------------------------------------------------------------------- *)
(* dispose of all symbol table memory *)

procedure SymFree (VAR SymTbl : Symbols);
  var Symbol : Symbols;
  begin
    Symbol := SymTbl;
    while (Symbol <> nil) do
      begin
      Symbol := Symbol^.Nxt;
      dispose (SymTbl);
      SymTbl := Symbol;
      end;
  end;  (* SymFree *)

(* --------------------------------------------------------------------- *)
(* --------------------------------------------------------------------- *)

  function ExistFile(FName : string) : Boolean;
    {-Return true if file is found}
  var
    Regs : Registers;
    FLen : Byte absolute FName;
  begin
    {check for empty string}
    if Length(FName) = 0 then
      ExistFile := False
    else with Regs do begin
      if IoResult = 0 then ; {clear IoResult}
      Inc(FLen);
      FName[FLen] := #0;
      AX := $4300;           {get file attribute}
      DS := Seg(FName);
      DX := Ofs(FName[1]);
      MsDos(Regs);
      ExistFile := (not Odd(Flags)) and (IoResult = 0) and
                   (CX and (VolumeID+Directory) = 0);
    end;
  end;

(* --------------------------------------------------------------------- *)

  function HasExtension(Name : string; var DotPos : Word) : Boolean;
    {-Return whether and position of extension separator dot in a pathname}
  var
    I : Word;
  begin
    DotPos := 0;
    for I := Length(Name) downto 1 do
      if (Name[I] = '.') and (DotPos = 0) then
        DotPos := I;
    HasExtension := (DotPos > 0) and (Pos('\', Copy(Name, Succ(DotPos), 64)) = 0);
  end;

(* --------------------------------------------------------------------- *)

  function DefaultExtension(Name, Ext : string) : string;
    {-Return a pathname with the specified extension attached}
  var
    DotPos : Word;
  begin
    if HasExtension(Name, DotPos) then
      DefaultExtension := Name
    else
      DefaultExtension := Name+'.'+Ext;
  end;

(* --------------------------------------------------------------------- *)

  function ForceExtension(Name, Ext : string) : string;
    {-Return a pathname with the specified extension attached}
  var
    DotPos : Word;
  begin
    if HasExtension(Name, DotPos) then
      ForceExtension := Copy(Name, 1, DotPos)+Ext
    else
      ForceExtension := Name+'.'+Ext;
  end;

(* --------------------------------------------------------------------- *)

  function JustExtension(Name : string) : string;
    {-Return d:\path\file.EXT}
  var
    DotPos : Word;
  begin
    if HasExtension(Name, DotPos) then
      JustExtension := Copy(Name, Succ(DotPos), 3)
    else
      JustExtension[0] := #0;
  end;

(* --------------------------------------------------------------------- *)

  function JustFilename(PathName : string) : string;
    {-Return d:\path\FILE.EXT}
  var
    I : Word;
  begin
    I := Succ(Word(Length(PathName)));
    repeat
      Dec(I);
    until (PathName[I] in DosDelimSet) or (I = 0);
    JustFilename := Copy(PathName, Succ(I), 64);
  end;

(* --------------------------------------------------------------------- *)

  function JustName(PathName : string) : string;
    {-Return d:\path\FILE.ext}
  var
    I, DotPos : Word;
  begin
    I := Succ(Word(Length(PathName)));
    repeat
      Dec(I);
    until (PathName[I] in DosDelimSet) or (I = 0);
    if (HasExtension (PathName, DotPos)) then
      JustName := Copy(PathName, Succ(I), DotPos - Succ(I))
    else
      JustName := Copy(PathName, Succ(I), 64);
  end;

(* --------------------------------------------------------------------- *)

  function JustPathname(PathName : string) : string;
    {-Return D:\PATH\file.ext}
  var
    I : Word;
  begin
    I := Succ(Word(Length(PathName)));
    repeat
      Dec(I);
    until (PathName[I] in DosDelimSet) or (I = 0);

    if I = 0 then
      {Had no drive or directory name}
      JustPathname[0] := #0
    else if I = 1 then
      {Either the root directory of default drive or invalid pathname}
      JustPathname := PathName[1]
    else if (PathName[I] = '\') then begin
      if PathName[Pred(I)] = ':' then
        {Root directory of a drive, leave trailing backslash}
        JustPathname := Copy(PathName, 1, I)
      else
        {Subdirectory, remove the trailing backslash}
        JustPathname := Copy(PathName, 1, Pred(I));
    end else
      {Either the default directory of a drive or invalid pathname}
      JustPathname := Copy(PathName, 1, I);
  end;

(* --------------------------------------------------------------------- *)

  function AddBackSlash(DirName : string) : string;
    {-Add a default backslash to a directory name}
  begin
    if DirName[Length(DirName)] in DosDelimSet then
      AddBackSlash := DirName
    else
      AddBackSlash := DirName+'\';
  end;

(* --------------------------------------------------------------------- *)

  function CleanFileName(FileName : string) : string;
    {-Return filename with at most 8 chars of name and 3 of extension}
  var
    DotPos : Word;
    namelen : Word;
  begin
    if HasExtension(FileName, DotPos) then begin
      {Take the first 8 chars of name and first 3 chars of extension}
      namelen := Pred(DotPos);
      if namelen > 8 then
        namelen := 8;
      CleanFileName := Copy(FileName, 1, namelen)+Copy(FileName, DotPos, 4);
    end else
      {Take the first 8 chars of name}
      CleanFileName := Copy(FileName, 1, 8);
  end;

(* --------------------------------------------------------------------- *)

  function CleanPathName(PathName : string) : string;
    {-Return a pathname cleaned up as DOS will do it}
  var
    I : Word;
    oname : string;
  begin
    oname[0] := #0;

    {!!.10} {Remove repeated backslashes}
    I := Succ(Word(Length(PathName)));
    repeat
      dec(I);
      if I > 2 then
        if (PathName[I] = '\') and (PathName[I-1] = '\') then
          if (PathName[I-2] <> ':') then
            Delete(PathName, I, 1);
    until I <= 0;

    I := Succ(Word(Length(PathName)));
    repeat
      {Get the next directory or drive portion of pathname}
      repeat
        Dec(I);
      until (PathName[I] in DosDelimSet) or (I = 0);

      {Clean it up and prepend it to output string}
      oname := CleanFileName(Copy(PathName, Succ(I), 64))+oname;
      if I > 0 then begin
        oname := PathName[I]+oname;
        Delete(PathName, I, 255);
      end;
    until I <= 0;

    CleanPathName := oname;
  end;

(* --------------------------------------------------------------------- *)

  function FullPathName(FName : string) : string;
    {-Given FName (known to exist), return a full pathname}
  var
    CurDir : string[64];
    Cpos : Byte;
  begin
    Cpos := Pos(':', FName);
    if Cpos <> 0 then begin
      {Drive letter specified}
      if FName[Succ(Cpos)] = '\' then
        {Complete path already specified}
        FullPathName := FName
      else begin
        {Drive specified, but incomplete path}
        GetDir(Pos(Upcase(FName[1]), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), CurDir);
        FullPathName := AddBackSlash(CurDir)+Copy(FName, Succ(Cpos), 100);
      end;
    end else begin
      {No drive specified}
      GetDir(0, CurDir);
      if FName[1] = '\' then
        {Complete path but no drive}
        FullPathName := Copy(CurDir, 1, 2)+FName
      else
        {No drive, incomplete path}
        FullPathName := AddBackSlash(CurDir)+FName;
    end;
  end;

(* --------------------------------------------------------------------- *)

END.  (* unit Strs *)

