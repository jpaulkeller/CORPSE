
UNIT DM_View;

INTERFACE

uses
  Dialogs,
  Drivers,
  Editors,
  Objects,
  StdDlg,
  Views,
  WinDos,  (* for fsPathName *)
  DM_Msg;

{$I Entries.Typ}

const
  vwScanWindow = $01; (* display using special attributes *)
  vwDeleteFile = $02; (* delete associated file when Done *)

type
  PViewWindow = ^TViewWindow;
  TViewWindow = object(TEditWindow)

    ViewTitle: TTitleStr;
    FileName: string;
    vwOptions: word;

    constructor Init     (    BoxP        : PRect;
                              FilePath    : FNameStr  );
    constructor Load     (var S           : TStream   );
    constructor Make     (    BoxP        : PRect;
                              FilePath    : FNameStr;
                              Path        : string;
                              TitleSuffix : string;
                              Help        : integer;
                              ViewOptions : word      );
    destructor  Done;                                               virtual;

    procedure Draw;                                                 virtual;
    function  GetPath                                  : TTitleStr;
    function  GetTitle   (    MaxSize     : Integer   ): TTitleStr; virtual;
    procedure HandleEvent(var Event       : TEvent    );            virtual;
    procedure SetTitle   (    NewViewTitle: TTitleStr );
    procedure Store      (var S           : TStream   );

  private
    function  LineString (    FromPtr,
                              ToPtr       : word      ): string;
    procedure Overwrite  (var Line        : string;
                              Row         : integer;
                              Opn, Cls    : string;   (* Cls may be '' *)
                              Attribute   : word      );
    procedure ScanWindow;

  end;

const
  RViewWindow: TStreamRec = (
    ObjType: 1001; (*  stViewWindow *)
    VmtLink: Ofs(TypeOf(TViewWindow)^);
    Load   : @TViewWindow.Load;
    Store  : @TViewWindow.Store
  );

procedure Register_DM_View;

(* ----------------------------------------------------------------------- *)

procedure Search_Directory (    Path      : string;
                                Pattern   : string;
                            var MatchF    : text);

procedure Resolve_Table    (VAR DstF      : text;
                                TableName : string;
                                FilePath  : string;
                                EntryQty  : integer);

procedure Resolve_Script   (VAR DstF      : text;
                                FilePath  : string);
(* ----------------------------------------------------------------------- *)

IMPLEMENTATION

USES
  Dos, App, MsgBox, Rndm, Strs, Util, DM_Const, DM_FMS;

(* ----------------------------------------------------------------------- *)

constructor TViewWindow.Init (BoxP     : PRect;
                              FilePath : FNameStr);
begin
  if (BoxP = nil) then
    BoxP^.Assign (0, 0, 75, 20);
  inherited Init (BoxP^, FilePath, wnNoNumber);
  ViewTitle := inherited GetTitle (fsPathName);
  FileName  := '';
  vwOptions := 0;
  HelpCtx   := 0;
end; (* Init *)

constructor TViewWindow.Make (BoxP        : PRect;
                              FilePath    : FNameStr;
                              Path        : string;
                              TitleSuffix : string;
                              Help        : integer;
                              ViewOptions : word);
begin
  if (BoxP = nil) then
    BoxP^.Assign (0, 0, 75, 20);
  inherited Init (BoxP^, FilePath, wnNoNumber);
  ViewTitle := inherited GetTitle (fsPathName);
  FileName  := Strs.SubStr (ViewTitle, Path, '.');
  ViewTitle := FileName + TitleSuffix;
  vwOptions := vwScanWindow or ViewOptions;
  HelpCtx := Help;
end; (* Make *)

(* ----------------------------------------------------------------------- *)

constructor TViewWindow.Load (var S: TStream);
begin
  inherited Load(S);
  S.Read(ViewTitle[0], SizeOf(Char));
  S.Read(ViewTitle[1], Length(ViewTitle));
  S.Read(FileName[0], SizeOf(Char));
  S.Read(FileName[1], Length(FileName));
  S.Read(vwOptions, SizeOf(Word));
end; (* Load *)

procedure TViewWindow.Store(var S: TStream);
begin
  inherited Store(S);
  S.Write(ViewTitle, Length(ViewTitle) + 1);
  S.Write(FileName,  Length(FileName) + 1);
  S.Write(vwOptions, SizeOf(Word));
end; (* Store *)

procedure Register_DM_View;
begin
  RegisterType (RViewWindow);
end; (* Registre_DM_View *)

(* ----------------------------------------------------------------------- *)

destructor TViewWindow.Done;
var F : text;
    FilePath : TTitleStr;
begin
  FilePath := inherited GetTitle (fsPathName);
  if (vwOptions and vwDeleteFile <> 0) then
    if (Strs.ExistFile (FilePath)) then
      begin
      assign (F, FilePath);
      erase (F);
      end;
  inherited Done;
end; (* Done *)

(* ----------------------------------------------------------------------- *)

procedure TViewWindow.Draw;
begin
  inherited Draw;
  if (vwOptions and vwScanWindow <> 0) then ScanWindow;
end; (* Draw *)

(* ----------------------------------------------------------------------- *)
(* Alters text display attributes based on case-sensitive patterns.        *)
(* <Opn> and <Cls> are the pattern delimiters; they may be identical.      *)
(* <Opn> may be an un-delimited pattern, in which case <Cls> is optional.  *)
(* Note that unlike the Highlight function, ALL matches are highlighted.   *)

procedure TViewWindow.Overwrite (var Line      : string;
                                     Row       : integer;
                                     Opn, Cls  : string;  (* Cls may be '' *)
                                     Attribute : word);

var Lt, Rt : integer;  (* left and right positions of pattern *)
    OpnQty : integer;  (* # of Opn patterns passed *)
    ClsQty : integer;  (* # of Cls patterns passed *)
    Len    : integer;
    Token  : string;
    DrawBuf: array[0..MaxLineLength - 1] of Word;

  function Pattern : boolean;
  begin
    Lt := pos (Opn, Line);
    if (Cls = '') then
      Rt := Lt + length (Opn) - 1
    else if (Lt > 0) then
      begin
      Rt := Lt + length (Opn) - 1;
      if (Opn = Cls) then
        repeat Inc (Rt);
        until ((Rt = length (Line)) or
               ((Line [Rt] = Cls[1]) and
                (copy (Line, Rt, length (Cls)) = Cls)))
      else
        begin
        OpnQty := 1;
        ClsQty := 0;
        repeat
          Inc (Rt);
          if ((Line [Rt] = Opn[1]) and
              (copy (Line, Rt, length (Opn)) = Opn)) then
            Inc (OpnQty);
          if ((Line [Rt] = Cls[1]) and
              (copy (Line, Rt, length (Cls)) = Cls)) then
            Inc (ClsQty);
        until ((ClsQty = OpnQty) or
               (Rt = (length (Line) - length (Cls) + 1)));
        if (ClsQty <> OpnQty) then Lt := 0;  (* return false *)
        end;
      end;
    Pattern := ((Lt > 0) and (Lt <= Rt));
  end; (* Pattern *)

begin
  while Pattern do
    begin
    Token := copy (line, Lt, Rt - Lt + length (Cls));
    Len := Min (Size.X - 1 - Lt, length(Token));
    if (Len > 0) then
      begin
      MoveBuf (DrawBuf, Token[1], Attribute, Len);
      WriteBuf(Lt, Row, Len, 1, DrawBuf);
      end;
    Line [Lt] := ' ';
    if (Cls <> '') then Line [Rt] := ' ';
    end;
end;  (* Overwrite *)

(* ----------------------------------------------------------------------- *)
(* Extract a line from the edit buffer as a string. *)

function TViewWindow.LineString (FromPtr, ToPtr : word): string;
var i : integer;
    Line : string;
begin
  for i := FromPtr to ToPtr - 1 do
    Line[i - FromPtr + 1] := Editor^.BufChar(i);
  Line[0] := chr(ToPtr - FromPtr);
  LineString := Line;
end; (* LineString *)

(* ----------------------------------------------------------------------- *)
(* Check visible lines for special patterns to be displayed differently.   *)

procedure TViewWindow.ScanWindow;
var
  Row: integer;
  LinePtr: word;
  NextPtr: word;
  Line: string;
begin
  LinePtr := Editor^.DrawPtr;
  for Row := 1 to Size.Y - 2 do  (* for each visible row in window *)
  begin
    NextPtr := Editor^.NextLine (LinePtr);
    Line := LineString (LinePtr, NextPtr);
    if (Line <> '') then
      Overwrite (Line, Row, TokenL, TokenR, Attr (Blue, White));
{
      begin
      Overwrite (Line, Row, '[', ']', Attr (Bkgd, LightCyan));
      Overwrite (Line, Row, '(', ')', Attr (Bkgd, LightGray));
      Overwrite (Line, Row, '<', '>', Attr (Yellow, Bkgd));
      end;
}
    LinePtr := NextPtr;
  end;
end; (* ScanWindow *)

{
(* ----------------------------------------------------------------------- *)
(* Writes a Line into the virtual screen block, altering some attributes.  *)

procedure Write_Line (var Line : string; Row : integer);

  const Bkgd = Blue;
  var   LineAttr : word;

  begin
    case Line[1] of
      CommentMark   : LineAttr := Attr (Bkgd, Yellow      );
      FieldMark     : LineAttr := Attr (Bkgd, Cyan        );
      FooterMark    : LineAttr := Attr (Bkgd, LightMagenta);
      MacroMark     : LineAttr := Attr (Bkgd, Cyan        );
      PositionMark  : LineAttr := Attr (Bkgd, LightGray   );
      SourceMark    : LineAttr := Attr (LightBlue, Black  );
      SubHeaderMark : LineAttr := Attr (Bkgd, Magenta     );
      HeaderMark    : LineAttr := Attr (Red,  White       );
      else            LineAttr := Attr (Bkgd, White       );
    end;  (* case *)

    WriteVScreen (VTxt, Line, Row, 1, LineAttr);

  end;  (* Write_Line *)
}

(* ----------------------------------------------------------------------- *)

function TViewWindow.GetPath: TTitleStr;
begin
  GetPath := inherited GetTitle (fsPathName);
end; (* GetPath *)

function TViewWindow.GetTitle(MaxSize: Integer): TTitleStr;
begin
  GetTitle := ViewTitle;
end; (* GetTitle *)

procedure TViewWindow.HandleEvent(var Event: TEvent);
begin
  inherited HandleEvent(Event);
  if (Event.What = evBroadcast) and (Event.Command = cmFindViewWindow) then
    ClearEvent(Event);
end; (* HandleEvent *)

procedure TViewWindow.SetTitle(NewViewTitle: TTitleStr);
begin
  ViewTitle := NewViewTitle;
end; (* SetTitle *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)

procedure Search_Table (    Path       : string;
                            Pattern    : string;
                        var MatchF     : text;
                        VAR FirstInDir : boolean);

  var Line        : string;
      FirstInFile : boolean;
      F           : text;

  begin
    (* Env.Log ('Search_Table: ' + Path); *)
    FirstInFile := true;

    assign (F, Path); reset (F);
    while not EOF (F) do
      begin
      repeat readln (F, Line); until ((Line <> '') or EOF(F));
      if (pos (Pattern, Strs.Upper (Line)) > 0) then
        begin
        if FirstInFile then
          begin
          if FirstInDir then
            begin
            FirstInDir := false;
            writeln (MatchF);
            writeln (MatchF, SourceMark + ' ' + Strs.JustPathName (Path));
            end;
          FirstInFile := false;
          writeln (MatchF);
          writeln (MatchF, SubHeaderMark + ' <' +
                           Strs.JustFileName (Path) + '>');
          end;

        if (Line[1] = HeaderMark) then Line[1] := SubHeaderMark;

        writeln (MatchF, Line);
        end;  (* if *)
      end;  (* while *)
    close (F);
    (* Env.LogOut; *)
  end;  (* Search_Table *)

(* ----------------------------------------------------------------------- *)

procedure Search_Directory (    Path    : string;
                                Pattern : string;
                            var MatchF  : text);

  var F          : DOS.SearchRec;
      FirstInDir : boolean;

  begin
    {Env.Log ('Search_Directory: ' + Path);}
    writeln ('Searching ', Path, '...');
    FirstInDir  := true;

    DOS.FindFirst (Path + '*.*', DOS.Directory, F);
    while (DOS.DosError = 0) do
      begin
      if (F.Attr = DOS.Directory) then
        begin
        if (F.Name[1] <> '.') then
          Search_Directory (Path + F.Name + '\', Pattern, MatchF);
        end
      else if (Strs.JustExtension (F.Name) = 'TBL') then
        Search_Table (Path + F.Name, Pattern, MatchF, FirstInDir);
      DOS.FindNext (F);
      end;

    {Env.LogOut;}
  end;  (* Search_Directory *)

(* ----------------------------------------------------------------------- *)

procedure Resolve_Entry (VAR SrcF       : text;
                         VAR DstF       : text;
                             Line       : integer;
                             Feedback   : PMilestone;
                         VAR Repetition : integer;
                         VAR PrevEntry  : string;
                         VAR NullSyms   : Symbols);
var
  {MileAttr   : word;     (* milestone attribute *)}
  Entry      : string;
  {LineNmbr   : string;}
  Status     : integer;

begin
  if (Repetition <= 0) then  (* should never be < 0 *)
    begin
    Entry := Next_Entry (SrcF, Repetition);
    if (Repetition > 0) then PrevEntry := Entry;
    end
  else begin Dec (Repetition); Entry := PrevEntry; end;

  Status := 0;
  if (Entry <> '') then
    begin
    if (pos (TokenL, Entry) > 0)      then Status := 2
    else if (Entry [1] in ['0'..'9']) then Status := 1;
    end;

  {case Status of
    2 : MileAttr := Attr (Red,    Blue);  (* variable *)
    1 : MileAttr := Attr (Yellow, Blue);  (* multiple *)
    0 : MileAttr := Attr (Green,  Blue);  (* standard *)
    end;
  Feedback^.Update (MileAttr);}

  if (Status > 0) then
    Resolve_Line (Entry, NullSyms);
  if (Entry <> '') and (Entry [1] = ' ') then
    Entry := Strs.Trim (Entry);

  {LineNmbr := PadNum (StrF (i), '0', NmbrWidth, true, '+');
  Write_Resolved (Entry, i, LineNmbr, NmbrWidth);}

  writeln (DstF, Line:4, ' ', Entry);
end;  (* Resolve_Entry *)

(* ----------------------------------------------------------------------- *)

procedure Resolve_Table (VAR DstF       : text;
                             TableName  : string;
                             FilePath   : string;
                             EntryQty   : integer);
var
  Feedback   : PMilestone;
  SrcF       : text;
  Line       : integer;
  Repetition : integer;
  PrevEntry  : string;
  Syms       : Symbols;

begin
  {Feedback^.Init ('Expanding ' + TableName + ' Macros...',
                   Min (60, EntryQty), EntryQty);}
  assign (SrcF, FilePath); reset (SrcF);
  Repetition := 0;
  Syms := nil;

  for Line := 1 to EntryQty do
    Resolve_Entry (SrcF, DstF, Line, Feedback, Repetition, PrevEntry, Syms);

  writeln (DstF);
  close (SrcF);
  {Feedback^.Done;}
end; (* Resolve_Table *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)

function Include_File (VAR Line : string; VAR Qty : integer) : boolean;
  begin
    Include_File := false;
    if ((Line <> '') and (Line[1] = IncludeMark)) then
      begin
      Delete (Line, 1, 1);  (* IncludeMark *)
      Line := Strs.Trim (Line);
      if (Line <> '') then
        begin
        Qty := 1;
        if (Line[1] in ['0'..'9']) then
          Qty := ValF (Strs.PopWord (Line, ' '), '0');
        Strs.UpperCase (Line);
        if Strs.ExistFile (CommandPath + '\' + Line) then  (* assumes .CMD *)
          begin
          Line := CommandPath + '\' + Line;  (* assumes .CMD *)
          Include_File := true;
          end;
        end;
      end;
  end;  (* Include_File *)

(* ----------------------------------------------------------------------- *)

procedure Resolve_Script (VAR DstF      : text;
                              FilePath  : string);
var
  SrcF       : text;
  Line       : string;
  UserSyms   : Symbols;
  IncQty, i  : integer;

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  procedure Execute_Command (var Line : string);
    var Command : char;
    begin
      Command := Line [1];
      Delete (Line, 1, 1);
      case (Command) of
{ ? }   Question   : (* set CmdQueryMode *);
{ # }   RandomSeed : RNDM.Seed (Strs.Trim (Line));
      end;  (* case *)
      Line := '';  (* so it won't get written out *)
    end;  (* Execute_Command *)

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  function Next_Line : string;
    begin
      readln (SrcF, Line);

      if (Line = '') then
        Line := ' '  (* so it will get written out *)
      else if (Line[1] = CommentMark) then  (* white space *)
        Line := ''  (* so it won't get written out *)
      else
        begin
        if (pos (TokenR, Line) > 0) then (* variable *)
          Resolve_Line (Line, UserSyms);
        if (Line[1] in [RandomSeed, Question]) then (* command *)
          Execute_Command (Line);
        end;

      Next_Line := Line;
    end;

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

begin (* Resolve_Script *)
  UserSyms := nil;
  assign (SrcF, FilePath); reset (SrcF);

  while not EOF(SrcF) do
    begin
    Line := Next_Line;
    if (Line <> '') then
      begin
      if (Include_File (Line, IncQty)) then  (* open included file *)
        for i := 1 to IncQty do
          Resolve_Script (DstF, Line)
      else
        writeln (DstF, Line);
      end;
    end;  (* while not EOF *)

  writeln (DstF);
  close (SrcF);
  Strs.SymFree (UserSyms);
end; (* Resolve_Script *)

(* ----------------------------------------------------------------------- *)

end.
