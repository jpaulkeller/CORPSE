{$I Compiler.inc}

(* File: DM_FMS.Pas *)

UNIT DM_FMS;  (* File Management System *)

INTERFACE

USES Objects, Strs;

(* --------------------------------------------------------------------- *)

{$I Entries.Typ}  (* types for various file entry records *)

(* --------------------------------------------------------------------- *)

CONST

  MaxTblQty     =  200;                              (* arbitrary *)
  CommandPath   = 'COMMANDS';            (* path of Command files *)
  TablePath     = 'TABLES';              (* path of Table files   *)
  UserPath      = 'USER';                (* path of User files    *)

  (* ------------------------------------------------------------ *)

VAR
  Info          : array [1..MaxTblQty + 1] of Tables; (* +1 for search end *)
  TableList     : PCollection;

  NullCrit      : Criteria;                           (* for NIL criteria  *)
  NullStruc     : Structures;                         (* for NIL structure *)
  Target        : SearchInfo;                       (* global pattern info *)
  TblQty        : integer;                          (* current # of tables *)
  HeapPtr       : pointer;                     (* must be internal to work *)

(* ----------------------------------------------------------------------- *)

TYPE
  PTable = ^TTable;
  TTable = object(TObject)
    Name    : PString;     (* file path and name     *)
    Entries : integer;     (* # of entries in file   *)
    Lines   : integer;     (* # of lines in file     *)
    Width   : integer;     (* widest unresolved line *)
    Field   : Fields;      (* head of field list     *)
    Macro   : Macros;      (* head of macro list     *)

    constructor Init (RelativePath    : string;
                      NumberOfEntries : integer;
                      NumberOfLines   : integer;
                      WidestLine      : integer;
                      NewFields       : Fields;
                      NewMacros       : Macros);
    destructor  Done; virtual;
  end;  (* TTable *)

  function TableOfName (FindName : string) : PTable;

(* ----------------------------------------------------------------------- *)

function  Ask_User         (var   Line         : string    ): string;
function  FieldValue       (VAR   Entry        : string;
                                  Tbl          : integer;
                                  FldName      : string;
                                  Erase        : boolean   ): string;
function  FileFromName     (      Name         : string    ): integer;
function  File_Entry       (      FRec         : Tables;
                                  Resolve      : boolean;
                            VAR   Criterion    : Criteria;
                                  Structure    : Structures;
                            VAR   Status       : integer   ): string;
function  FindEntry        (      TableName    : string;
                                  Pattern      : string    ): string;
procedure Index_File       (      FileName     : string    );
procedure Index_Files      (      FreeFirst    : boolean   );
function  Match            (const Pattern      : string;
                            VAR   Start        : integer;
                            VAR   Width        : integer;
                                  Line         : string    ): boolean;
function  NameFromFile     (      FileIndex    : integer   ): string;
function  Next_Entry       (var   F            : text;
                            VAR   Repetition   : integer   ): string;
procedure Pop_Criterion    (VAR   Line         : string;
                                  WhichFile    : integer;
                            VAR   Criterion    : Criteria  );
procedure Pop_Mask         (VAR   Line         : string;
                            VAR   Mask         : Masks     );
function  Pop_Qty          (VAR   Line         : string    ): integer;
procedure Pop_Structure    (VAR   Line         : string;
                                  WhichFile    : integer;
                            VAR   Structure    : Structures);
function  Read_Entry       (      Tbl          : integer;
                                  FldName      : string;
                                  FldValue     : string    ): string;
procedure Resolve_Line     (VAR   Line         : string;
                            VAR   UserSyms     : Symbols   );
function  RandomEntry      (      TableName    : string;
                                  StrucSpec    : string    ): string;
function  Rndm_Entry       (      Tbl          : integer;
                                  TableName    : string;
                                  Structure    : Structures): string;
function  RndmName                                          : string;

(* ----------------------------------------------------------------------- *)

IMPLEMENTATION

USES
  {Turbo Pascal}
  DOS,

  {Turbo Vision}
  Dialogs,
  MsgBox,
  Views,

  {JPK Utilities}
  Rndm,
  Util;

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)

constructor TTable.Init (RelativePath    : string;
                         NumberOfEntries : integer;
                         NumberOfLines   : integer;
                         WidestLine      : integer;
                         NewFields       : Fields;
                         NewMacros       : Macros);
begin
  Name    := NewStr (RelativePath);
  Entries := NumberOfEntries;
  Lines   := NumberOfLines;
  Width   := WidestLine;
  Field   := NewFields;
  Macro   := NewMacros;
end;  (* TTable.Init *)

(* ----------------------------------------------------------------------- *)

destructor TTable.Done;
begin
  Dispose (Name);
end;  (* TTable.Done *)

(* ----------------------------------------------------------------------- *)

function TableOfName (FindName : string) : PTable;

  function NameEqual (Table : PTable) : boolean;
  begin
    NameEqual := (FindName = Strs.JustName (Table^.Name^));
  end;
begin
  FindName := Strs.JustName (FindName);
  TableOfName := TableList^.FirstThat (@NameEqual);
end;  (* TableOfName *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)
(* Queries the user with a menu or prompt for string. *)
(* A prompt should be in the form: {? Prompt(Default)} *)
(*                             or: {? Table} *)

function Ask_User (var Line : string) : string;
  var s         : string;
      Prompt    : string;
      {Criterion : Criteria;
      Field     : string;
      Tbl       : integer;
      Sepr      : char;}

  begin
    Delete (Line, 1, 1);
    Line := Strs.Trim (Line);

    if (Pos (DefaultL, Line) > 0) then  (* TBD: this could be better *)
      begin
      s := Strs.SubStr (Line, DefaultL, DefaultR);  (* default *)
      Prompt := Strs.SubStr (Line, '', DefaultL) + ' ';
      if (InputBox ('title', Prompt, s, 80) = cmCancel) then
        s := '';
      end
;{
    else   (* menu query *)
      begin
      if (pos (FieldMark, Line) > 0) then Sepr := FieldMark
      else Sepr := ' ';
      s := Strs.Upper (Strs.PopWord (Line, Sepr));
      Tbl := FileFromName (s);

      if (Tbl > 0) then          (* file exists *)
        begin
        Prompt := ' Which ' + s + '? ';
        Field := PopWord (Line, ' ');
        if ((Field = '') and (Sepr = FieldMark)) then Field := s;
        if (Line = '') then
          Criterion := NullCrit
        else
          begin
          Criterion.CriteriaType := Index;
          Criterion.Index := Pop_Qty (Line);
          end;
        s := Get_Entry (Prompt, Tbl, Field, Field);
        end
      else
        s := ErrorL + s + ErrorR;
      end;  (* menu query *)
}
    Ask_User := s;
  end;  (* Ask_User *)

(* ----------------------------------------------------------------------- *)
(* Converts a table entry numeric prefix into a quantity. *)
(* For example, 5 = 5; 1-3 = 3 *)

function Entries_Per_Line (Line : string) : integer;

 var Range    : string;
     Lo, Hi   : integer;
     Quantity : integer;

 begin
   (* Env.Log ('Entries_Per_Line'); *)
   Range := Strs.PopWord (Line, ' ');
   if (pos ('-', Range) > 0) then
     begin
     Lo := Strs.ValF (Strs.PopWord (Range, '-'), '1');
     Hi := Strs.ValF (Range, '1');
     Quantity := Hi - Lo + 1;
     end
   else
     Quantity := Strs.ValF (Range, '1');

   Entries_Per_Line := Quantity;
   (* Env.LogOut; *)
 end;  (* Entries_Per_Line *)
     
(* ----------------------------------------------------------------------- *)

procedure UpdateIndexFile (var IndexF : text; FldQty, MacQty : integer);

  var Fld : Fields;
      Mac : Macros;

  begin
    with Info [TblQty] do
      begin
      writeln (IndexF, Lines:4,  ' ', Entries:4, ' ', Width:3, ' ',
                       FldQty:2, ' ', MacQty:2,  ' ', Name);

      Fld := Field;
      while (Fld <> nil) do
        with Fld^ do
          begin
          writeln (IndexF, ' ', Value.Start:2, ' ', Value.Width:2, ' ', Name);
          Fld := Fld^.Next;
          end;

      Mac := Macro;
      while (Mac <> nil) do
        with Mac^ do
          begin
          writeln (IndexF, ' ', Name, ' ', Value);
          Mac := Mac^.Next;
          end;
      end;  (* with *)
  end;  (* UpdateIndexFile *)

(* ----------------------------------------------------------------------- *)
(* Save index info for given <FileName> to OPEN index file. *)

procedure IndexTable (var IndexF : text; FileName : string);

  var Line   : string;
      F      : text;
      FldQty : integer;  (* number of fields *)
      MacQty : integer;  (* number of macros *)
      Len    : integer;  (* current line len *)

  procedure InsertField (VAR Field : Fields);
    var NewField : Fields;
    begin
      (* Env.Log ('InsertField: ' + Line); *)
      Delete (Line, 1, 1); (* FieldMark *)
      FldQty := FldQty + 1;
      new (NewField);
      NewField^.Next        := Field;
      NewField^.Name        := Strs.Upper(PopWord (Line, ' '));
      NewField^.Value.Start := ValF (Strs.PopWord (Line, ' '), '');
      NewField^.Value.Width := ValF (Strs.PopWord (Line, ' '), '');
      Field := NewField;  (* push field *)
      (* Env.LogOut; *)
    end;  (* InsertField *)

  procedure InsertMacro (VAR Macro : Macros);
    var NewMacro : Macros;
    begin
      (* Env.Log ('InsertMacro: ' + Line); *)
      Delete (Line, 1, 1); (* MacroMark *)
      MacQty := MacQty + 1;
      new (NewMacro);
      NewMacro^.Next  := Macro;
      NewMacro^.Name  := Strs.Upper (Strs.PopWord (Line, ' '));
      NewMacro^.Value := Line;
      Macro := NewMacro;  (* push Macro *)
      (* Env.LogOut; *)
    end;  (* InsertMacro *)

  begin  (* IndexTable *)

    (* count lines in data file *)
    Inc (TblQty);
    with Info [ TblQty ] do
    begin
      Name := Strs.SubStr (FileName, TablePath + '\', '.TBL');
      writeln (' Indexing (', TblQty:3, ') ', Name, '...');
      Field   := nil;
      Macro   := nil;
      Entries := 0;
      Lines   := 0;
      Width   := 0;
      FldQty  := 0;
      MacQty  := 0;

      assign (F, FileName);  reset (F);
      while not EOF (F) do
        begin
        readln (F, Line);
        Inc (Lines);
        Len := Length (Line);
        if (Len > 0) then    (* not blank line *)
          begin
          if (Len > Width) then Width := Len;  (* save max width *)
          case Line [1] of
            FieldMark     : InsertField (Field);
            MacroMark     : InsertMacro (Macro);
            CommentMark,
            FooterMark,
            HeaderMark,
            PositionMark,
            SourceMark,
            SubHeaderMark : ;
            '0'..'9'      : Entries := Entries + Entries_Per_Line (Line);
            else Inc (Entries);
            end;  (* case *)
          end;  (* if *)
        end;  (* while more entries *)
      close (F);

      TableList^.Insert (New (PTable,
        Init (Name, Entries, Lines, Width, Field, Macro)));

      if (Name[1] <> TempMark) then
        UpdateIndexFile (IndexF, FldQty, MacQty);

      {if (Entries = 0) then
        Env.Warning ('File with 0 entries: ' + FileName);}

      end;  (* with *)
  end;  (* IndexTable *)

(* ----------------------------------------------------------------------- *)

procedure Index_Directory (var IndexF : text; Path : string);

  var F : DOS.SearchRec;  (* DOS type *)

  begin
    {Env.Log ('Index_Directory: ' + Path);}
    DOS.FindFirst (Path + '*.*', DOS.Directory, F);

    while (DOS.DosError = 0) do
      begin
      if (F.Attr = DOS.Directory) then
        begin
        if (F.Name[1] <> '.') then
          Index_Directory (IndexF, Path + F.Name + '\');
        end
      else if (Strs.JustExtension (F.Name) = 'TBL') then
        IndexTable (IndexF, Path + F.Name); (* plain table file *)
      DOS.FindNext (F);
      end;

    {Env.LogOut;}
  end;  (* Index_Directory *)

(* ----------------------------------------------------------------------- *)
(* Read the index from the index file. *)

procedure Load_Index_File (var IndexF : text);

  var
    FldQty : integer;
    MacQty : integer;
    i      : integer;
    ch     : char;     (* field separator *)

  procedure LoadField (VAR Field : Fields);
    var NewField : Fields;
    begin
      new (NewField);
      with NewField^ do
        readln (IndexF, Value.Start, Value.Width, ch, Name);
      (* Env.Log ('LoadField: ' + NewField^.Name); Env.Logout; *)
      NewField^.Next := Field;
      Field := NewField;  (* push field *)
    end;  (* LoadField *)

  procedure LoadMacro (VAR Macro : Macros);
    var NewMacro : Macros;
    begin
      new (NewMacro);
      with NewMacro^ do
        begin
        readln (IndexF, Value);
        Name := Strs.PopWord (Value, ' ');
        (* Env.Log ('LoadMacro: ' + Name); Env.Logout; *)
        Next := Macro;
        end;
      Macro := NewMacro;  (* push macro *)
    end;  (* LoadMacro *)

  begin  (* Load_Index_File *)

    TableList := New(PCollection, Init(150, 10));
    reset (IndexF);

    while not EOF (IndexF) do
      begin
      Inc (TblQty);
      with Info [ TblQty ] do
        begin
        readln (IndexF, Lines, Entries, Width, FldQty, MacQty, ch, Name);
        {writeln (' Loading index for ', Name, '...');}
        Field := nil;
        Macro := nil;
        for i := 1 to FldQty do LoadField (Field);
        for i := 1 to MacQty do LoadMacro (Macro);
        TableList^.Insert (New (PTable,
          Init (Name, Entries, Lines, Width, Field, Macro)));
        end;  (* with *)
      end;  (* while *)

  end;  (* Load_Index_File *)

(* ----------------------------------------------------------------------- *)

procedure Free_Index;

  var i   : integer;
      Fld : Fields;
      Mac : Macros;

  begin
    {Env.Log ('Free_Index: ' + StrF (TblQty));}

    for i := 1 to TblQty do
      with Info[i] do
        begin
        while (Field <> nil) do
          begin
          Fld := Field^.Next;
          dispose (Field);
          Field := Fld;
          end;
        while (Macro <> nil) do
          begin
          Mac := Macro^.Next;
          dispose (Macro);
          Macro := Mac;
          end;
        end;  (* for/with each indexed table *)

    {Env.LogOut;}
  end;  (* Free_Index *)

(* ----------------------------------------------------------------------- *)
(* perform program initialization *)

procedure Index_Files (FreeFirst : boolean);

  const IndexFile = 'TABLES.NDX';

  var IndexF : text;
      i      : integer;

  begin
    assign (IndexF, IndexFile);

    if FreeFirst and (TblQty > 0) then Free_Index;
    TblQty := 0;

    if (not FreeFirst and Strs.ExistFile (IndexFile)) then
      Load_Index_File (IndexF)
    else
      begin
      TableList := New(PCollection, Init(150, 10));
      rewrite (IndexF);
      Index_Directory (IndexF, TablePath + '\');
      end;

    close (IndexF);
    writeln (' TABLE INDEX COMPLETE: ', TblQty, ' tables indexed.');
  end;  (* Index_Files *)

(* ----------------------------------------------------------------------- *)
(* Open the index file; index the given file; close the index file. *)

procedure Index_File (FileName : string);

  const IndexFile = 'TABLES.NDX';
  var   IndexF    : text;

  begin  (* Index_File *)
    {Env.Log ('Index_File: ' + FileName);}
    assign (IndexF, IndexFile);
    if (Strs.ExistFile (IndexFile)) then append (IndexF)
    else rewrite (IndexF);
    IndexTable (IndexF, FileName);
    close (IndexF);
    {Env.LogOut;}
  end;  (* Index_File *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)
(* Given a file name, return the associated index number. *)
(* Returns the file number if found, else Warning and returns 0. *)

function FileFromName (Name : string) : integer;

  var Found : integer;
  begin
    Found := 0;
    Name := Strs.Upper (Strs.JustName (Name));
    {writeln ('FileFromName: ' + Name);}

    (* perform sentinel search *)
    Info [ TblQty + 1 ].Name := Name;  (* guarantee success *)
    repeat Inc (Found);
    until (Strs.JustName (Info [ Found ].Name) = Name);

    if (Found <= TblQty) then
      FileFromName := Found
    else
      begin
      {Env.Warning ('File Not Found: <' + Name + '>');}
      FileFromName := 0;
      end;
    {Env.LogOut;}
  end;  (* FileFromName *)

(* ----------------------------------------------------------------------- *)
(* Given a file index, return the associated full path name. *)

function NameFromFile (FileIndex : integer) : string;
  begin
    {Env.Log ('NameFromFile: ' + StrF (FileIndex));}
    NameFromFile := TablePath + '\' + Info [ FileIndex ].Name + '.TBL';
    {Env.LogOut;}
  end;  (* NameFromFile *)

(* ----------------------------------------------------------------------- *)
(* Returns true if given target and line match. *)
(* If match found, resets the global Target structure to reflect position. *)

function Match (const Pattern : string;
                VAR   Start   : integer;
                VAR   Width   : integer;
                      Line    : string) : boolean;

  var Position : integer;
  begin
    Match := false;  (* default *)
    if (Line <> '') then
      begin
      if (Start > 1) and (Width > 0) then
         Line := copy (Line, Start, Width);
      Position := Pos (Pattern, Line);
      if (Position > 0) then
        begin
        Start := Position + Start - 1;
        Width := length (Pattern);
        Match := true;
        end;
      end;
  end;  (* Match *)

(* ----------------------------------------------------------------------- *)

function Pop_Qty (VAR Line : string) : integer;

  (* ------------------------------------------------ *)
  (* Interprets a quantity which can be:              *)
  (*                                                  *)
  (*   (1) nil                                        *)
  (*   (2) a single number:       #      (1 d #)      *)
  (*   (3) a range of numbers:    # - #  (or #-#)     *)
  (*   (4) a roll specification:  # #    (# d #)      *)
  (*   (5) a query user command:  ?{"prompt:default"} *)
  (*                                                  *)
  (*   A user response can be any of (2) through (4)  *)
  (*                                                  *)
  (* ------------------------------------------------ *)

  var s          : string;
      Qty        : integer;
      ReadTooFar : boolean;
      Ask, Dflt  : string;

  begin
    (* Env.Log ('Pop_Qty: ' + Line); *)

    if (pos ('-', Line) > 0) and (pos (' - ', Line) = 0) then
      Line := Strs.XchText1 (Line, '-', ' - ');
    s := Strs.PopWord (Line, ' ');

{
    if (s[1] = '?') then
      begin
      Delete (s, 1, 1);
      Ask  := Or_ (SubStr (s, '', ':'), 'Enter Quantity (# <#>)') + ': ';
      Dflt := Or_ (SubStr (s, ':', ''), ' 1');

      (* !!!!!!!!!!!!!!!!!!!!!!! fixing !!!!!!!!!!!!!!!!! *)
      (* s := Trim (Query (Yellow, Ask, Green, Dflt, ['0'..'9',' ','-'])); *)
      if (Ask_For_Num (Ask, Ask, 0, 0, Qty)) then ;

      if (s <> '') then  (* valid Qty *)
        begin
        Line := s + ' ' + Line;  (* push user's Qty specification *)
        s := Strs.PopWord (Line, ' ');
        end
      else
        s := '1';  (* invalid Qty; default to 1 *)
      end;  (* if *)
}
    ReadTooFar := not (s[1] in ['0'..'9', '?']);
    if ReadTooFar then Qty := 1  (* no quantity given, default to 1 *)
    else
      begin
      Qty := Strs.ValF (s, '');
      s   := Strs.PopWord (Line, ' ');
      ReadTooFar := not (s[1] in ['1'..'9', '-']);

      if ReadTooFar or (s = '') then
        Qty := RNDM.Number (1, Qty)
      else if (s = '-') then  (* roll spec is range *)
        Qty := RNDM.Number (Qty, ValF (PopWord (Line, ' '), ''))
      else  (* roll spec is bell *)
        Qty := RNDM.BellCurve (Qty, Strs.ValF (s, ''), false);
      end;  (* else *)

    if ReadTooFar then Line := s + ' ' + Line;  (* push non-qty back *)

    Pop_Qty := Qty;
    {Env.Log ('Pop_Qty = ' + StrF (Qty)); Env.LogOut;}
  end;  (* Pop_Qty *)

(* ----------------------------------------------------------------------- *)

procedure Pop_Structure (VAR Line      : string;
                             WhichFile : integer;
                         VAR Structure : Structures);

  (* ------------------------------------ *)
  (* Interprets a structure which can be: *)
  (*   (1) optional                       *)
  (*   (2) a field name:  field           *)
  (*   (3) a region:      # #             *)
  (* ------------------------------------ *)

  var s   : string;
      Fld : Fields;
  begin
    {Env.Log ('Pop_Structure: ' + Line);}

    Structure := NullStruc;
    if (Line = '') or (WhichFile = 0) then Exit;

    s := Strs.PopWord (Line, ' ');

    if (s[1] in ['1'..'9']) then  (* region: start# width# *)
      begin
      Structure.Start := ValF (s, '1');
      Structure.Width := ValF (PopWord (Line, ' '), '80');
      end

    else                               (* field name *)
      with Info [ WhichFile ] do
        begin
        Fld := Field;
        while (Fld <> nil) and (Fld^.Name <> Strs.Upper (s)) do
          Fld := Fld^.Next;
        if (Fld <> nil) then
          Structure := Fld^.Value
        else
          begin
          {Env.Warning ('Missing field <' + s + '> in ' +
                       Info [ WhichFile ].Name);}
          Structure := NullStruc;
          end;
        end;  (* with *)
    {Env.LogOut;}
  end;  (* Pop_Structure *)

(* ----------------------------------------------------------------------- *)

procedure Pop_Criterion (VAR Line      : string;
                             WhichFile : integer;
                         VAR Criterion : Criteria);

  (* ------------------------------------------------ *)
  (* Interprets a critrion which can be:              *)
  (*   (1) optional (at the end)                      *)
  (*   (2) the text ANY:          ANY                 *)
  (*   (3) zero (whole file):     0                   *)
  (*   (4) a range of numbers:    # - #               *)
  (*   (5) a roll specification:  # #                 *)
  (*   (6) a pattern and field:   pattern structure   *)
  (* ------------------------------------------------ *)

  var i : integer;
      s : string;

  begin
    {Env.Log ('Pop_Criterion: ' + Line);}
    s := Strs.PopWord (Line, ' ');

    if (s = '') or (s = 'ANY') then
      Criterion := NullCrit

    else if (s[1] in ['0'..'9']) then
      begin
      i := ValF (s, '0');
      if (i = 0) then Criterion := NullCrit
      else begin
        s := Strs.PopWord (Line, ' ');
        if (s = '-') then  (* range # - # *)
          begin
          Criterion.CriteriaType := crRange;
          Criterion.Range.Lo := i;
          Criterion.Range.Hi := ValF (PopWord (Line, ' '), '');
          end
        else    (* dice # d # *)
          begin
          Criterion.CriteriaType := crDice;
          Criterion.Dice.Qty     := i;
          Criterion.Dice.Sides   := ValF (s, '');
          end;
        end;  (* else *)
      end

    else  (* pattern *)
      begin
      Criterion.CriteriaType   := crSearch;
      Criterion.Search.Pattern := s;
      Pop_Structure (Line, WhichFile, Criterion.Search.Field);
      end;  (* else pattern *)

    {Env.LogOut;}
  end;  (* Pop_Criterion *)

(* ----------------------------------------------------------------------- *)

procedure Pop_Mask (VAR Line : string; VAR Mask : Masks);
  begin
    {Env.Log ('Pop_Mask: ' + Line);}
    Mask := Strs.PopWord (Line, ' ');
    {Env.LogOut;}
  end;  (* Pop_Mask *)

(* ----------------------------------------------------------------------- *)

function Macro_Entry (Tbl : integer; MacroName : string) : string;

  var Criterion : Criteria;
      Structure : Structures;
      Status    : integer;
      s         : string;
      Mac       : Macros;

  begin
    {Env.Log ('Macro_Entry: ' + Info [Tbl].Name);}
    with Info [Tbl] do
      if (Macro <> nil) then
        begin
        Mac := Macro;
        while (Mac <> nil) and
              (Mac^.Name <> Strs.Upper (MacroName)) do
          Mac := Mac^.Next;

        if (Mac = nil) then
          begin
          {Env.Warning ('Missing macro <' + MacroName + '> in ' + Name);}
          s := ErrorL + MacroName + ErrorR;
          end

        else      (* found *)
          begin
          s := copy (Mac^.Value, 1, 255);
          Pop_Criterion (s, Tbl, Criterion);
          (* check for BAD CRITERION here !!!!!!!!!!!!!!!!!!!! *)
          Pop_Structure (s, Tbl, Structure);
          s := File_Entry (Info [Tbl], true, Criterion, Structure, Status);
          end;
        end  (* if/with *)

        else
          begin
          {Env.Warning ('No macros in ' + Name + ' (' + MacroName + ')');}
          s := ErrorL + MacroName + ErrorR;
          end;

    {Env.Logout;}
    Macro_Entry := s;
  end;  (* Macro_Entry *)

(* ----------------------------------------------------------------------- *)
(* Alters the case of the given <s> to match the given <Token> *)

procedure Fix_Case (VAR s : string; Token : string);
var
  Upper, Lower : boolean;
  i : integer;
begin
  if ((length (Token) > 1) and (length (s) > 0)) then
    begin
    Upper := Strs.IsUpper (Token[1]) or Strs.IsUpper (Token[2]);
    Lower := Strs.IsLower (Token[1]) or Strs.IsLower (Token[2]);
    LowerCase (s);
    if (Upper and Lower) then
      begin
      s[1] := UpCase (s[1]);
      for i := 2 to length (s) do
        if not (IsAlpha (s[i-1]) or (s[i-1] in ['-', ''''])) then
          s[i] := UpCase (s[i]);
      end
    else if (Upper) then
      UpperCase (s);
    end;
end;  (* Fix_Case *)

(* ----------------------------------------------------------------------- *)

  (* -------------------------------------------------- *)
  (*   TOKEN                   DESCRIPTION              *)
  (* -------------    --------------------------------- *)
  (* {TABLE}        = roll default line from TABLE.TBL  *)
  (* {TABLE:FIELD}  = roll default line; extract FIELD  *)
  (* {TABLE:# #}    = roll default line; extract region *)
  (* {TABLE|MACRO}  = execute the MACRO from TABLE.TBL  *)
  (* {!NAME}        = generate random name              *)
  (*                                                    *)
  (* {quantity}     = get quantity (see Pop_Qty)        *)
  (* {?q=#:A:B}     = conditional; if q=# then A else B *)
  (* {?q>#:A:B}     = other operators (<,>) may be used *)
  (*                                                    *)
  (* {=token}       = pad/truncate result to fit token  *)
  (* {=# token}     = pad/truncate result to be # long  *)
  (* -------------------------------------------------- *)

  (* Note: If a FIELD or MACRO separator is included,    *)
  (*       but the field or macro name is not provided,  *)
  (*       the TABLE name will be used as a default.     *)
  (*       E.g., {TABLE:} is the same as {TABLE:TABLE}   *)

  (* Note: Tokens (text delimited by curly braces) may   *)
  (*       not contain colons (:) or pipes (|), and may  *)
  (*       not begin with !, ?, or =; except to indicate *)
  (*       the above described functions.                *)


function Evaluate_Token (Token : string; VAR UserSyms : Symbols) : string;

  var ForceLen  : integer;
      s         : string;
      FileName  : string;
      Macro     : string;
      Tbl       : integer;
      Structure : Structures;
      Sepr      : char;  (* type of table look-up *)

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  function ParseToken (Separator : char) : string;
    var s      : string;
        Status : integer;
    begin
      {Env.Log ('ParseToken: ' + Token);}
      FileName := Strs.PopWord (Token, Separator);
      Tbl := FileFromName (FileName);
      Token := Strs.Trim (Token);
      if (Token = '') then
        Token := Strs.JustFileName (FileName);

      if (Tbl = 0) then  (* error *)
        s := ErrorL + Token + ErrorR

      else case Separator of
        FieldMark :
          begin
          s := Token;  (* save Token for use after Pop *)
          Pop_Structure (s, Tbl, Structure);
          s := File_Entry (Info [Tbl], true, NullCrit, Structure, Status);
          end;
        MacroMark : s := Macro_Entry (Tbl, Token);
        else s := File_Entry (Info [Tbl], true, NullCrit, NullStruc, Status);
        end;  (* case *)

      Fix_Case (s, Token);
      ParseToken := s;
      {Env.Logout;}
    end;  (* ParseToken *)

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  function Pop_Condition : string;       (* {?RndQty=FxdQty:1:2} *)

    var Operator       : char;
        RndQty, FxdQty : integer;
        s              : string;

    begin
      Delete (Token, 1, 1);

      if      (Pos ('=', Token) > 0) then Operator := '='
      else if (Pos ('<', Token) > 0) then Operator := '<'
      else if (Pos ('>', Token) > 0) then Operator := '>'
      else
        begin
        Pop_Condition :=
          ErrorL + 'Invalid CONDITION (missing operator): ' + Token + ErrorR;
        exit;
        end;

      s := Strs.PopWord (Token, Operator);
      RndQty := Pop_Qty (s);
      FxdQty := Strs.ValF (PopWord (Token, CondSepr), '0');
      s := Strs.PopWord (Token, CondSepr);

      case Operator of
        '=' : if (RndQty <> FxdQty) then s := Token;
        '>' : if (RndQty <= FxdQty) then s := Token;
        '<' : if (RndQty >= FxdQty) then s := Token;
      end;
      Pop_Condition := s;
    end  (* Pop_Condition *);

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  begin
    {Env.Log ('Evaluate_Token [' + Token + ']');}

    if (Token = '') then
      begin
      Evaluate_Token := ErrorL + 'Empty Token' + ErrorR;
      exit;
      end;

    (* handle the EqualsMark for forcing a certain length *)
    ForceLen := 0;
    if (Token[1] = EqualsMark) then
      begin
      Delete (Token, 1, 1);
      if (Token[1] in ['0'..'9']) then
        ForceLen := Strs.ValF (Strs.PopWord (Token, ' '), '0');
      if (ForceLen = 0) then
        ForceLen := length (Token) + 3;  (* {=} *)
      end;

    case Token[1] of

      '0'..'9'    : s := Strs.StrF (Pop_Qty (Token));

{ ? } Conditional : if (Token[2] in ['0'..'9']) then s := Pop_Condition
                    else s := Ask_User (Token);

{ ! } CommandMark : begin
                    Delete (Token, 1, 1);
                    case Token[1] of
                      NameMark : s := RndmName;
                      else {Env.Warning ('Invalid Token Command: ' + Token)};
                      end;  (* case *)
                    end;

      else if (pos (Assignment, Token) > 0) then (* assignment *)
        begin
        s := PopWord (Token, Assignment);
        Strs.SymPut (UserSyms, s, Token);
        s := Token;
        end

      else if (Strs.SymGet (UserSyms, Token, s)) then (* user variable *)
        begin {do nothing} end

      else  (* table name *)
        begin
        if      (pos (FieldMark, Token) > 0) then Sepr := FieldMark
        else if (pos (MacroMark, Token) > 0) then Sepr := MacroMark
        else                                      Sepr := ' ';
        s := ParseToken (Sepr);
        end;

      end;  (* case *)

    s := Strs.Compress (s, 0);
    if (ForceLen > 0) then
      s := Strs.Pad (s, ' ', ForceLen, true, Truncation);

    {Env.Logout;}
    Evaluate_Token := s;
  end;  (* Evaluate_Token *)

(* ----------------------------------------------------------------------- *)
(* Handle token replacement within an entry.                               *)
(* For example, in the line "{Color} Dragon", this routine would replace   *)
(* the bracketed substring "{Color}" with a line (or field) that was       *)
(* randomly selected from the file COLOR.TBL                               *)

procedure Resolve_Line (VAR Line : string; VAR UserSyms : Symbols);

  var Token, Value : string;
      i : integer;

  begin
    {Env.Log ('Resolve_Line: ' + Line);}
    Token := Strs.NxtTok (Line, TokenL, TokenR);
    while (Token <> '') do
      begin
      Value := Evaluate_Token (Token, UserSyms);
      Line  := Strs.XchText1 (Line, TokenL + Token + TokenR, Value);
      Token := Strs.NxtTok (Line, TokenL, TokenR);
      end;

    (* clear any quantity indicators *)
    i := 1;
    while (i <= length(Line)) and (Line[i] in ['0'..'9', '-']) do
      begin
      Line[i] := ' ';
      Inc (i);
      end;
    {Env.Logout;}
  end;  (* Resolve_Line *)

(* ----------------------------------------------------------------------- *)
(* Returns the next entry from an open file (unresolved).  *)
(* Entries must begin with a <space>, letter, or digit.    *)

function Next_Entry (var F : text; VAR Repetition : integer) : string;

  var Entry    : string;
      SkipLine : boolean;

  begin
    (* Env.Log ('Next_Entry'); *)
    Repetition := 0;
    SkipLine := true;
    while (SkipLine and not EOF (F)) do
      begin
      readln (F, Entry);
      if (Length (Entry) > 0) then
        case Entry [1] of
          CommentMark,
          FieldMark,
          FooterMark,
          HeaderMark,
          MacroMark,
          PositionMark,
          SourceMark,
          SubHeaderMark : ;
          '0'..'9'      : begin
                          SkipLine   := false;  (* weighted entry *)
                          Repetition := Entries_Per_Line (Entry) - 1;
                          end;
          else SkipLine := false; (* assume entry *)
        end;  (* if not empty and case *)
      end;  (* while *)

    if SkipLine then Next_Entry := ''
    else             Next_Entry := Entry;
    (* {Env.Logout;} *)
  end;  (* Next_Entry *)

(* ----------------------------------------------------------------------- *)
(* Returns a 'resolved' file entry given a file name. *)

function File_Entry; (* from forward declaration *)

  (* Args =     FRec       : Tables     *)
  (*            Resolve    : boolean    *)
  (*        VAR Criterion  : Criteria   *)
  (*            Structure  : Structures *)
  (*        VAR Status     : integer    *)
  (* Returns  : string                  *)

  (* Sets Status:  0 - standard line *)
  (*               1 - weighted line *)
  (*               2 - variable line *)

  var F          : text;
      i          : integer;
      Entry      : string;
      WhichEntry : integer;
      Repetition : integer;
      NullSyms   : Symbols;
      Pattern    : string;

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  begin (* File_Entry *)

    {
    Env.Log ('File_Entry: ' + FRec.Name);
    if Env.DeeperThan (18) then
      begin
      Env.Log ('!! RECURSION OVERFLOW (>18) !!');
      File_Entry := ErrorL + ' RECURSION OVERFLOW ' + ErrorR;
      Env.Logout;
      Env.Logout;
      exit;
      end;
    }

    assign (F, TablePath + '\' + FRec.Name + '.TBL'); reset (F);

    case Criterion.CriteriaType of
      crNull   : WhichEntry := RNDM.Number (1, FRec.Entries);
      crDice   : with Criterion.Dice do
                   if ((Qty * Sides) <= FRec.Entries) then
                     WhichEntry := RNDM.BellCurve (Qty, Sides, false)
                   {else
                      Env.Error ('Invalid Dice Criterion: ' +
                                  StrF (Qty) + ' ' + StrF (Sides))};

      crIndex  : with Criterion do
                   if (Index <= FRec.Entries) then
                     WhichEntry := Index
                   {else
                     Env.Error ('Invalid Entry Criterion: ' + StrF (Index))};
      crRange  : with Criterion.Range do
                   if (Hi <= FRec.Entries) then
                     WhichEntry := RNDM.Number (Lo, Hi)
                   {else
                     Env.Error ('Invalid Range Criterion: ' + StrF (Hi))};
      crSearch : begin
                 if not EOF (F) then
                   begin
                   Pattern := Strs.Upper (Criterion.Search.Pattern);
                   repeat Entry := Next_Entry (F, Repetition);
                   until Match (Pattern,
                                Criterion.Search.Field.Start,
                                Criterion.Search.Field.Width,
                                Strs.Upper (Entry))
                      or EOF (F);
                   end;
                 if EOF (F) and not Match (Pattern,
                                           Criterion.Search.Field.Start,
                                           Criterion.Search.Field.Width,
                                           Strs.Upper (Entry)) then
                   with Criterion.Search do
                     Entry := ErrorL + ' No entry matches: ' +
                              Pattern + ErrorR;
                 WhichEntry := 0;  (* already here *)
                 end;
      end;  (* case *)

    Repetition := 0;
    for i := 1 to WhichEntry do    (* skip to entry # WhichEntry *)
      if (Repetition <= 0) then    (* should never be < 0 *)
        Entry := Next_Entry (F, Repetition)
      else
        Dec (Repetition);  (* no need to reset Entry *)

    Status := 0;
    if Resolve and (Entry <> '') then
      begin
      if (pos (TokenR, Entry) > 0)      then Status := 2
      else if (Entry [1] in ['0'..'9']) then Status := 1;
      if (Status > 0) then
        begin
        NullSyms := nil;
        Resolve_Line (Entry, NullSyms);
        end;
      end;

    close (F);

    (* apply structure if any *)
    if (Structure.Start < 0) then Entry := ErrorL + Entry + ErrorR
    else if (Structure.Start > 0) then
      Entry := copy (Entry, Structure.Start, Structure.Width);
    (* else Entry[1] := ' '; *)

    File_Entry := Entry;
    {Env.Logout;}
  end;  (* File_Entry *)

(* ----------------------------------------------------------------------- *)
(* Returns the value of a given field from an entry. *)

function FieldValue (VAR Entry   : string;
                         Tbl     : integer;
                         FldName : string;
                         Erase   : boolean) : string;

  var Structure : Structures;
      FldVal    : string;
      First, i  : integer;

  begin
    {Env.Log ('FieldValue: ' + FldName);}
    Pop_Structure (FldName, Tbl, Structure);

    if (Structure.Start = 0) then    (* NullStruc *)
      begin
      FldVal := Entry;
      if Erase then
        for i := 1 to length (Entry) do
          Entry [i] := ' ';
      end

    else with Structure do
      begin
      (* TBD: ??? First  := Start - 1; *)  First := Start;
      FldVal := Strs.Trim (copy (Entry, First, Width));
      if Erase then  (* clear accessed field from entry *)
        for i := First to (First + Width - 1) do
          Entry [i] := ' ';
      end;

    {Env.Logout;}
    FieldValue := FldVal;
  end;  (* FieldValue *)

(* --------------------------------------------------------------------- *)
(* Search <Tbl> for an entry whose <FldName> which matches <FldValue>. *)

function Read_Entry (Tbl : integer; FldName, FldValue : string) : string;

  var s         : string;
      Criterion : Criteria;
      Status    : integer;

  begin
    {Env.Log ('Read_Entry' + FldName + ': ' + FldValue);}
    if (Pos (' ', FldValue) > 0) then  (* quote multi-word values *)
      FldValue := '"' + FldValue + '"';
    s   := FldValue + ' ' + FldName;
    Pop_Criterion (s, Tbl, Criterion);
    s := File_Entry (Info [Tbl], true, Criterion, NullStruc, Status);
    {Env.Logout;}
    if (Pos (' No entry matching', s) > 0) then Read_Entry := ''
    else Read_Entry := s;
  end;  (* Read_Entry *)

(* --------------------------------------------------------------------- *)
(* Randomly select an entry from the <Tbl>. *)

function Rndm_Entry (Tbl       : integer;  (* optional *)
                     TableName : string;
                     Structure : Structures) : string;
  var i : integer;
  begin
    {Env.Log ('Rndm_Entry: ' + TableName);}
    if (Tbl = 0) then
      Tbl := FileFromName (TableName);
    if (Tbl = 0) then
      Rndm_Entry := ''
    else
      Rndm_Entry := File_Entry (Info [Tbl], true, NullCrit, Structure, i);
    {Env.Logout;}
  end;  (* Rndm_Entry *)

(* ----------------------------------------------------------------------- *)

  function RandomEntry (TableName, StrucSpec : string) : string;
  var Structure : Structures;
      Tbl       : integer;
  begin
    {Env.Log ('RndmEntry: ' + TableName);}
    Tbl := FileFromName (TableName);
    Pop_Structure (StrucSpec, Tbl, Structure);
    RandomEntry := Rndm_Entry (Tbl, '', Structure);
    {Env.Logout;}
  end;  (* RndmEntry *)

(* ----------------------------------------------------------------------- *)
(* Generate a reasonably pronouncable name. *)

function RndmName : string; (* -- from forward declaration *)

  var F : Tables;
      i : integer;
      s, Mask : string [20];
      Vowel, Consonant, Dipthong, Digram, Ending, Repeater : string [80];
      Status : integer;

  function Letter (LetterType : char) : string;
    begin
      {Env.Log ('Letter: ' + LetterType);}
      {Env.Logout;}
      case LetterType of
        'V' : Letter := Vowel     [ RNDM.Number (1, 10) ];
        'C' : Letter := Consonant [ RNDM.Number (1, 20) ];
        'W' : Letter := copy (Dipthong, RNDM.Number (1, 11) * 3 - 2, 2);
        'D' : Letter := copy (Digram,   RNDM.Number (1, 22) * 3 - 2, 2);
        'N' : Letter := copy (Ending,   RNDM.Number (1, 25) * 3 - 2, 2);
        'R' : Letter := copy (Repeater, RNDM.Number (1, 11) * 3 - 2, 2);
        else  Letter := LetterType;  (* punctuation or hard-letter  ? *)
        end;  (* case *)
    end;  (* Letter *)

  begin  (* Name *)
    {Env.Log ('RndmName');}
    Vowel     := 'AAEEIIOOUY';  (* weighted *)
    Consonant := 'BCDFGHJKLMNPRSTVWXYZ';
    Dipthong  := 'AI-AU-AY-EA-EE-EY-IA-IE-IO-OA-OO';
    Digram    := 'BL-BR-CH-CL-CR-DR-FL-FR-GH-GL-GR-PH-PL-PR-QU-SH-SL-ST-SW-TH-TR-WR';
    Ending    := 'CK-CT-LD-LF-LK-LM-LP-LT-MP-ND-NG-NK-NT-RD-RG-RK-RM-RN-RP-RT-SH-SK-ST-TH-WN';
    Repeater  := 'BB-DD-FF-GG-LL-MM-NN-PP-RR-SS-TT';

    F := Info [ FileFromName ('NAME') ];
    Mask := Strs.Trim (File_Entry (F, false, NullCrit, NullStruc, Status));
    s := '';
    for i := 1 to Length (Mask) do
      s := s + Letter (Mask [i]);

    if (Length (s) < 5) and  (* small enough to consider expanding *)
       (RNDM.Number (1, 10) > 7) then   (* 30% chance of expanding *)
      begin
      Mask := Strs.Trim (File_Entry (F, false, NullCrit, NullStruc, Status));
      for i := 1 to Length (Mask) do s := s + Letter (Mask [i]);
      end;  (* expand name *)

    RndmName := s;
    {Env.Logout;}
  end;  (* RndmName *)

(* --------------------------------------------------------------------- *)

function FindEntry (TableName, Pattern : string) : string;

  var Tbl : integer;
      Criterion : Criteria;
      Entry : string;
      Status : integer;
  begin
    Entry := '';
    Tbl := FileFromName (TableName);
    (* Tbl := TableOfName (TableName); *)
    if (Tbl <> 0) then
      begin
      Criterion.CriteriaType   := crSearch;
      Criterion.Search.Pattern := Pattern;
      Criterion.Search.Field   := NullStruc;
      Entry := File_Entry (Info [Tbl], true, Criterion, NullStruc, Status);
      end;
    FindEntry := Entry;
  end;  (* FindEntry *)

(* --------------------------------------------------------------------- *)

BEGIN  (* unit initialization *)

  {Env.Log ('DM_FMS unit initialization');}

  NullCrit.CriteriaType := crNull;       (* Null = whole file *)
  NullStruc.Start := 0;
  NullStruc.Width := 0;

  Target.Pattern     := '';  (* initial pattern (none) to match  *)
  Target.Field.Start := 1;   (* default value -- start at col. 1 *)
  Target.Field.Width := 0;   (* default value -- pattern length  *)

  TblQty := 0;

  {Env.Logout;}

END.  (* unit DM_FMS *)
