{$I Compiler.inc}

(* File: DM_View.Pas *)

UNIT DM_View;

INTERFACE

USES TpWindow;

(* ----------------------------------------------------------------------- *)

  procedure View_File    (TblOr0 : integer; Operation : char);
  procedure View_Matches;
  procedure Create_Table (Choice : integer);

  procedure Execute_File (Path : string);

(* ----------------------------------------------------------------------- *)

IMPLEMENTATION

USES
  DOS,
  TpDOS,    (* complements Turbo DOS   *)
  TPCRT,    (* replaces Turbo CRT unit *)
  TpMenu,

  Env, Sounds, Strs, Rndm,

  DM_Win,   (* for Attr *)
  DM_FMS;

CONST
  TpHome   = #199;  (* Home key         *)
  TpUp     = #200;  (* Up Arrow         *)
  TpPgUp   = #201;  (* Page Up          *)
  TpEnd    = #207;  (* End key          *)
  TpDn     = #208;  (* Down Arrow       *)
  TpPgDn   = #209;  (* Page Down        *)
  TpC_Lt   = #243;  (* Ctrl-Left Arrow  *)
  TpC_Rt   = #244;  (* Ctrl-Right Arrow *)
  TpC_End  = #245;  (* Ctrl-End         *)
  TpC_PgDn = #246;  (* Ctrl-Page Down   *)
  TpC_Home = #247;  (* Ctrl-Home        *)
  TpC_PgUp = #132;  (* Ctrl-Page Up     *)

  LtSd =  1;  RtSd =  7;  (* Left/Right Side     *)
  Lt_1 =  2;  Rt_1 =  8;  (* Left/Right 1 column *)
  PgUp =  4;  PgDn =  5;  (* Page Up/Down        *)
  LnUp =  3;  LnDn =  6;  (* Line Up/Down        *)

  HiLt =  9;
  Seek = 10;
  Fltr = 11;
  ReDo = 12;
  Find = 13;
  Next = 14;
  View = 15;
  Rslv = 16;

  Quit = 20;

  ColsPerPage = 78;  (* 80 - 2 [frame] *)

VAR
  TotalColumns   : integer;  (* total # of columns in the file  *)
  RowsInTable    : integer;  (* total # of rows in the file     *)
  RowsInHeader   : integer;  (* # of rows in header (usually 1) *)
  RowsPerPage    : integer;  (* varies depending on header size *)
  RowsPerBlock   : integer;  (* # of rows in virtual screen     *)
  RowsPerVScreen : integer;  (* max # of rows in virtual screen *)
  MaxCol, MaxRow : integer;  (* maximum values for scrolling    *)
  CurCol, CurRow : integer;  (* current column and row position *)

  NmbrWidth      : integer;

  TxtAttr        : word;
  FrmAttr        : word;
  TtlAttr        : word;
  WHdr, WTxt     : TpWindow.WindowPtr;
  VHdr, VTxt     : TpWindow.VScreen;
  Sepr           : string[100];   (* window separation line *)

  Chr       : char;
  Done      : boolean;
  M         : TpMenu.Menu;
  Choice    : TpMenu.MenuKey;

(* ----------------------------------------------------------------------- *)
(* Scroll current window to new Col/Row by redisplaying the VirtualScreen. *)
(* Click if no scrolling was possible; otherwise update current Col & Row. *)

procedure ScrollTo (var VirtualScreen  : TpWindow.VScreen;
                        NewCol, NewRow : integer);
  begin
    Env.Log ('ScrollTo: ' + StrF (NewCol) + ', ' + StrF (NewRow));
    if (NewCol = CurCol) and (NewRow = CurRow) then  (* nothing changed *)
      Sounds.Click
    else
      begin
      CurCol := NewCol;
      CurRow := NewRow;
      ClrScr;
      TpWindow.MoveVScreenToWindow (VirtualScreen, CurRow, CurCol);
      end;
    Env.Logout;
  end;  (* ScrollTo *)

(* ----------------------------------------------------------------------- *)
(* Scroll current window (carefully) offseting Col/Row by the given deltas *)

procedure ScrollBy (var VirtualScreen  : TpWindow.VScreen;
                        DeltaX, DeltaY : integer;
                        MaxCol, MaxRow : integer);
  var NewCol, NewRow : integer;
  begin
    Env.Log ('ScrollBy: ' + StrF (CurCol) + '/' + StrF (CurRow)
                   + ', ' + StrF (DeltaX) + '/' + StrF (DeltaY)
                   + ', ' + StrF (MaxCol) + '/' + StrF (MaxRow));
    NewCol := CurCol + DeltaX;
    if (DeltaX <> 0) then NewCol := Min (Max (NewCol, 1), MaxCol);
    NewRow := CurRow + DeltaY;
    if (DeltaY <> 0) then NewRow := Min (Max (NewRow, 1), MaxRow);
    ScrollTo (VirtualScreen, NewCol, NewRow);
    Env.Logout;
  end;  (* ScrollBy *)

(* ----------------------------------------------------------------------- *)

procedure ScrollHeader (NewCol : integer);
  begin
    Env.Log ('ScrollHeader: ' + StrF (NewCol));
    if not TpWindow.SetTopTiledWindow (WHdr) then ;
    ClrScr;
    TpWindow.MoveVScreenToWindow (VHdr, 1, NewCol);
    if not TpWindow.SetTopTiledWindow (WTxt) then ;
    TpCRT.FastWrite (Sepr, 4 + RowsInHeader, 1, FrmAttr);
    Env.Logout;
  end;  (* ScrollHeader *)

(* ----------------------------------------------------------------------- *)

procedure ScrollBothTo (NewCol, NewRow : integer);
  begin
    Env.Log ('ScrollBothTo: ' + StrF (NewCol) + ', ' + StrF (NewRow));
    if (RowsInHeader > 0) and (NewCol <> CurCol) then ScrollHeader (NewCol);
    ScrollTo (VTxt, NewCol, NewRow);
    Env.Logout;
  end;  (* ScrollBothTo *)

(* ----------------------------------------------------------------------- *)

procedure ScrollBothBy (DeltaX, DeltaY : integer);
  var OldCol : integer;
  begin
    Env.Log ('ScrollBothBy: ' + StrF (DeltaX) + ', ' + StrF (DeltaY));
    OldCol := CurCol;
    ScrollBy (VTxt, DeltaX, DeltaY, MaxCol, MaxRow);
    if (RowsInHeader > 0) and (OldCol <> CurCol) then ScrollHeader (CurCol);
    Env.Logout;
  end;  (* ScrollBothBy *)

(* ----------------------------------------------------------------------- *)
(* creates the main menu for table view operations *)

procedure ViewMenu (VAR M : TpMenu.Menu; Operation : char; Title : string);

const                        {Frm  Hdr  Body Chsn HtKy Help Unav Shdw}
  Color1  : MenuColorArray = ($0A, $0A, $03, $1F, $0B, $71, $08, $78);
  Color2  : MenuColorArray = ($0E, $0E, $03, $1F, $0B, $71, $08, $78);
  Frame1  : FrameArray     = 'ÉÈ»¼Íº';
  Frame2  : FrameArray     = 'ÑÔÑ¾Í³';

  HotKeys : MenuCharSet    = [TpHome, TpC_Home, TpEnd,  TpC_End,
                              TpPgUp, TpC_PgUp, TpPgDn, TpC_PgDn,
                              TpUp,   TpC_Lt,   TpDn,   TpC_Rt];

var HelpRow : integer;

begin
  Env.Log ('ViewMenu');
  HelpRow := ScreenHeight;

  M := NewMenu (HotKeys, nil);
  SubMenu(1,1,HelpRow,Horizontal,Frame1,Color1, Title);

    MenuItem(' ® ' , 1,2,LtSd,' Scroll to show the left edge (HOME).');
    MenuItem(' < ' , 4,2,Lt_1,' Scroll to show one more left column (^).');
    MenuItem('  ' , 7,2,LnUp,' Scroll to show the previous line.');
    MenuItem('  ' ,10,2,PgUp,' Scroll to show the previous page (PAGE UP).');
    MenuItem('  ' ,13,2,PgDn,' Scroll to show the next page (PAGE DOWN).');
    MenuItem('  ' ,16,2,LnDn,' Scroll to show the next line.');
    MenuItem(' > ' ,19,2,Rt_1,' Scroll to show one more right column.');
    MenuItem(' ¯ ' ,22,2,RtSd,' Scroll to show the right edge (END).');

    case {mode-specific} Operation of
      'V' : begin
            MenuItem('Highlight',27,1,HiLt,' Highlight text matching an entered pattern.');
            MenuItem('Seek'     ,38,1,Seek,' Jump to the next occurrence of an entered pattern.');
            MenuItem('Filter'   ,44,1,Fltr,' Hide any lines which do NOT contain an entered pattern.');
            MenuItem('Re-Load'  ,52,1,ReDo,' Re-display the complete (not filtered) table.');
            MenuItem('Resolve'  ,61,1,Rslv,' Resolve all variables in this table.');
            end;
      'R' : begin;
            MenuItem('Highlight',27,1,HiLt,' Highlight text matching an entered pattern.');
            MenuItem('Seek'     ,38,1,Seek,' Jump to the next occurrence of an entered pattern.');
            MenuItem('Filter'   ,44,1,Fltr,' Hide any lines which do NOT contain an entered pattern.');
            MenuItem('Re-Load'  ,52,1,ReDo,' Completely reload this table (with new resolved variables).');
            MenuItem('View'     ,61,1,View,' View the source table from which this resolved text came.');
            end;
      'G' : begin
            MenuItem('Another'  ,27,1,ReDo,' Randomly generate another entry from the same table.');
            end;
      'F' : begin
            MenuItem('Another'  ,27,1,ReDo,' Re-generate this entry (with new resolved variables).');
            MenuItem('Next'     ,36,1,Next,' Show the next entry matching the same pattern.');
            MenuItem('Find-New' ,42,1,Find,' Search table for a different pattern.');
            end;
    end;  (* case *)

    MenuItem(' Exit ',73,3,Quit,' Return to the CORPSE main menu.');
    PopSublevel;
  ResetMenu(M);
  Env.Logout;
end;  (* ViewMenu *)

(* ----------------------------------------------------------------------- *)
(* Get the header; make a virtual screen to hold the table header *)
(* Sets the global variable: RowsInHeader *)

procedure MakeHeaderWindow (var F         : text;
                                Tbl       : integer;
                                Title     : string;
                                NmbrWidth : integer);
  var
    HdrAttr : word;
    Entry   : string;
    Row     : integer;

  begin
    Env.Log ('MakeHeaderWindow [' + Title + ']: ' + Info[Tbl].Name +
             + ' (' + StrF (RowsInTable) + ')');
    HdrAttr := Attr (Blue, Yellow);

    RowsInHeader := 0;
    reset (F);
    while not EOF (F) and (RowsInHeader < (ScreenHeight - 7)) do
      begin
      readln (F, Entry);
      if (Entry <> '') and (Entry[1] = HeaderMark) then Inc (RowsInHeader);
      end;  (* while *)
    reset (F);

    if (RowsInHeader > 0) then
      begin
      if not MakeVScreen (VHdr, RowsInHeader,
                          Info [Tbl].Width + NmbrWidth) then
        Env.Error ('Unable to create header virtual screen');
      TpWindow.ClearVScreen (VHdr, HdrAttr, ' ');

      (* build a (non-scrolling) window containing the table's header *)
      Row := 0;
      repeat
        readln (F, Entry);
        if (Entry <> '') and (Entry[1] = HeaderMark) then  (* header *)
          begin
          Inc (Row);
          Entry [1] := ' ';
          TpWindow.WriteVScreen (VHdr, Entry, Row, NmbrWidth+1, HdrAttr);
          end;  (* if header *)
      until (Row = RowsInHeader);
      reset (F);

      if (NmbrWidth > 0) then
        TpWindow.WriteVScreen (VHdr, StrF(RowsInTable), RowsInHeader,
                               1, Attr (Blue, LightCyan));

      Inc (RowsInHeader);  (* include top frame *)
      if not MakeWindow (WHdr, 1, 4, ColsPerPage + 2, 3 + RowsInHeader + 2,
                         true, true, false, TxtAttr, FrmAttr, TtlAttr,
                         Title + Info [Tbl].Name + ' ') then
        Env.Error ('Unable to create window');

      if TpMouse.MouseInstalled then TpMouse.HideMouse;
      if not TpWindow.DisplayWindow (WHdr) then
        Env.Error ('Window Display');
      if TpMouse.MouseInstalled then TpMouse.ShowMouse;

      TpWindow.MoveVScreenToWindow (VHdr, 1, 1);
      end;  (* if header *)

    Env.Logout;
  end;  (* MakeHeaderWindow *)
  
(* ----------------------------------------------------------------------- *)
(* Make virtual screen to hold table text *)
(* Sets the global variables: RowsPerPage RowsPerVScreen RowsPerBlock *)

procedure MakeTextWindow;

  const MaxBytes = 32768;
  var RowsUsed : integer;

  begin
    Env.Log ('MakeTextWindow: ' +
             StrF(TotalColumns) + ' x ' + StrF(RowsInTable));
    if (TotalColumns = 0) then
      TotalColumns := Min (255, (MaxBytes DIV RowsInTable));
    RowsUsed       := 3 {menu} + RowsInHeader + 2 {frame} + 1 {help};
    RowsPerPage    := ScreenHeight - RowsUsed;
    RowsPerVScreen := MaxBytes DIV TotalColumns;  (* max addressable *)
    RowsPerBlock   := Min (RowsInTable, RowsPerVSCreen);
    Env.Log ('> Cols x Rows = ' + StrF(TotalColumns) + ' x ' +
             StrF(RowsPerBlock) + '(' + StrF(RowsPerVScreen) + ')');
    Env.LogOut;

    while not MakeVScreen (VTxt, RowsPerBlock, TotalColumns) do
      begin
      Env.Log ('!! Create VTxt: ' + StrF(RowsPerBlock)); Env.Logout;
      RowsPerBlock := trunc (RowsPerBlock * 0.9);
      if (RowsPerBlock < 1) then Env.Error ('VTxt: Out of memory');
      end;
    MaxRow := Max (1, (RowsPerBlock - RowsPerPage + 1));
    RowsPerVScreen := RowsPerBlock;
    TpWindow.ClearVScreen (VTxt, TxtAttr, ' ');

    while not MakeWindow (WTxt, 1, 4 + RowsInHeader,
                          ColsPerPage + 2,
                          3 + RowsInHeader + RowsPerPage + 2,
                          true, true, false,
                          TxtAttr, FrmAttr, FrmAttr, '') do
      begin
      Env.Log ('!! Create WTxt: ' + StrF(RowsPerPage)); Env.Logout;
      Dec (RowsPerPage);
      if (RowsPerPage < 1) then Env.Error ('WTxt: Out of memory');
      end;

    if TpMouse.MouseInstalled then TpMouse.HideMouse;
    if not TpWindow.DisplayWindow (WTxt) then Env.Error ('Window Display');
    (* overwrite the text window frame header *)
    if (RowsInHeader > 0) then   (* header window exists *)
      begin
      Sepr := 'Ã' + Strs.Pad ('', 'Ä', ColsPerPage, false, ' ') + '´';
      TpCRT.FastWrite (Sepr, 4 + RowsInHeader, 1, FrmAttr);
      end;
    if TpMouse.MouseInstalled then TpMouse.ShowMouse;

    Env.Logout;
  end;  (* MakeTextWindow *)
  
(* ----------------------------------------------------------------------- *)
(* Routines which are used to load a table into a block                    *)
(* ----------------------------------------------------------------------- *)

(* ----------------------------------------------------------------------- *)
(* Alters text display attributes based on case-sensitive patterns.        *)
(* <Opn> and <Cls> are the pattern delimiters; they may be identical.      *)
(* <Opn> may be an un-delimited pattern, in which case <Cls> is optional.  *)
(* Note that unlike the Highlight function, ALL matches are highlighted.   *)

  procedure Overwrite (Entry     : string;
                       Row       : integer;
                       Opn, Cls  : string;   (* Cls may be '' *)
                       Attribute : word);

    var Lt, Rt : integer;  (* left and right positions of pattern *)
        OpnQty : integer;  (* # of Opn patterns passed *)
        ClsQty : integer;  (* # of Cls patterns passed *)


    function Pattern : boolean;
      begin
        Lt := pos (Opn, Entry);
        if (Cls = '') then
          Rt := Lt + length (Opn) - 1
        else if (Lt > 0) then
          begin
          Rt := Lt + length (Opn) - 1;
          if (Opn = Cls) then
            repeat Inc (Rt);
            until ((Rt = length (Entry)) or
                   ((Entry [Rt] = Cls[1]) and
                    (copy (Entry, Rt, length (Cls)) = Cls)))
          else
            begin
            OpnQty := 1;
            ClsQty := 0;
            repeat
              Inc (Rt);
              if ((Entry [Rt] = Opn[1]) and
                  (copy (Entry, Rt, length (Opn)) = Opn)) then
                Inc (OpnQty);
              if ((Entry [Rt] = Cls[1]) and
                  (copy (Entry, Rt, length (Cls)) = Cls)) then
                Inc (ClsQty);
            until ((ClsQty = OpnQty) or
                   (Rt = (length (Entry) - length (Cls) + 1)));
            if (ClsQty <> OpnQty) then Lt := 0;  (* return false *)
            end;
          end;
        Pattern := ((Lt > 0) and (Lt <= Rt));
      end;

    begin
      while Pattern do
        begin
        WriteVScreen (VTxt, copy (Entry, Lt, Rt - Lt + length (Cls)),
                      Row, Lt, Attribute);
        Entry [Lt] := ' ';
        if (Cls <> '') then Entry [Rt] := ' ';
        end;  (* while *)
    end;  (* Overwrite *)

(* ----------------------------------------------------------------------- *)
(* Writes an entry into the virtual screen block, altering some attributes *)

procedure Write_Entry (var Entry : string; Row : integer);

  const Bkgd = Blue;
  var   LineAttr : word;

  begin
    case Entry[1] of
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

    WriteVScreen (VTxt, Entry, Row, 1, LineAttr);

    Overwrite (Entry, Row, '[', ']', Attr (Bkgd, LightCyan));
    Overwrite (Entry, Row, '(', ')', Attr (Bkgd, LightGray));
    Overwrite (Entry, Row, '<', '>', Attr (Yellow, Bkgd));
    Overwrite (Entry, Row, TokenL, TokenR, Attr (White, Green));
  end;  (* Write_Entry *)

(* ----------------------------------------------------------------------- *)
(* Writes an entry into the virtual screen block, altering some attributes *)

procedure Write_Script (Entry : string; Row : integer; ExpIncl : integer);

  const Bkgd = Blue;
        Fore = White;
  var   LineAttr : word;
        Col      : integer;
        NxtPos   : integer;
        ECopy    : string;
        Piece    : string;

  begin
    if (Entry[1] = ColorMark) then
      begin
      ECopy := Entry;
      Entry := '';
      Col := 1;
      repeat  (* write each part *)
        LineAttr := Codes2Attr (ECopy[3], Bkgd, ECopy[2], Fore);
        Delete (ECopy, 1, 1);                           (* ColorMark  *)
        if (ECopy[1] <> ' ') then Delete (ECopy, 1, 1); (* foreground *)
        if (ECopy[1] <> ' ') then Delete (ECopy, 1, 1); (* background *)
        if (ECopy[1] =  ' ') then Delete (ECopy, 1, 1); (* separator  *)

        NxtPos := pos (ColorMark, ECopy) - 1;
        if (NxtPos < 0) then
          NxtPos := length (ECopy);  (* force-write the rest *)
        Piece := copy (ECopy, 1, NxtPos);
        Entry := Entry + Piece;  (* recreate Entry w/o color codes *)
        WriteVScreen (VTxt, Piece, Row, Col, LineAttr);
        Inc (Col, NxtPos);
        Delete (ECopy, 1, NxtPos);
      until (ECopy = '');
      end

    else
      begin
      case Entry[1] of
        CommentMark : LineAttr := Attr (Bkgd,   Yellow);
        RandomSeed  : LineAttr := Attr (Yellow, Black );
        Question    : LineAttr := Attr (Cyan,   Black );
        IncludeMark : begin
                      LineAttr := Attr (Red,    Yellow);
                      if (ExpIncl <> 0) then
                        Entry := Entry + ' ' + ErrorL + 'MISSING' + ErrorR;
                      end;
        else          LineAttr := Attr (Bkgd,   White );
      end;  (* case *)
      WriteVScreen (VTxt, Entry, Row, 1, LineAttr);
      end;  (* else *)

    Overwrite (Entry, Row, TokenL, TokenR, Attr (White, Green));
    Overwrite (Entry, Row, ErrorL, ErrorR, Attr (Red, White));
  end;  (* Write_Script *)

(* ----------------------------------------------------------------------- *)
(* Writes an entry into the virtual screen block, altering some attributes *)

procedure Write_Resolved (Entry     : string;
                          Row       : integer;
                          LineNmbr  : string;
                          NmbrWidth : integer);
  const Bkgd = Blue;
  var   LineAttr : word;
        NmbrAttr : word;

  begin
    LineAttr := Attr (Bkgd, White);
    NmbrAttr := Attr (Bkgd, LightCyan);
    
    if (NmbrWidth > 0) then
      WriteVScreen (VTxt, LineNmbr, Row, 1, NmbrAttr)
    else
      begin
      NmbrWidth := pos (' ', Entry) - 1;
      LineNmbr  := copy (Entry, 1, NmbrWidth);
      WriteVScreen (VTxt, LineNmbr, Row, 1, NmbrAttr);
      Entry := copy (Entry, NmbrWidth + 2, 255);
      end;

    WriteVScreen (VTxt, Entry, Row, NmbrWidth + 2, LineAttr);

    Entry := LineNmbr + ' ' + Entry;
    Overwrite (Entry, Row, '[', ']', Attr (Bkgd, LightCyan));
    Overwrite (Entry, Row, '(', ')', Attr (Bkgd, LightGray));
    Overwrite (Entry, Row, '<', '>', Attr (Yellow, Bkgd));
    Overwrite (Entry, Row, ErrorL, ErrorR, Attr (Red, White));
    Overwrite (Entry, Row, Truncation, '', Attr (Yellow, Red));
  end;  (* Write_Resolved *)

(* ----------------------------------------------------------------------- *)

procedure Finish_Load;
  begin
    MaxCol := Max (1, (TotalColumns - ColsPerPage + 1));
    MaxRow := Max (1, (RowsPerBlock - RowsPerPage + 1));
    CurCol := 1;
    CurRow := 1;
    ClrScr;
    TpWindow.MoveVScreenToWindow (VTxt, 1, 1);
  end;  (* Finish_Load *)

(* ----------------------------------------------------------------------- *)
(* Store the (unresolved) file in the virtual screen (with colors) *)
(* Uses the global variable: RowsPerBlock *)

procedure Load_Straight_Table (var F : text; Skip : string);

  var r     : integer;
      Entry : string;

  begin
    Env.Log ('Load_Straight_Table');
    TpWindow.ClearVScreen (VTxt, TxtAttr, ' ');

    reset (F);
    for r := 1 to RowsPerBlock do
      if not EOF(F) then
        begin
        readln (F, Entry);
        if ((Entry = '') or (pos (Entry[1], Skip) = 0)) then
          Write_Entry (Entry, r)
        else
          Dec (r);
        end;  (* for each line *)

    Finish_Load;
    Env.Logout;
  end;  (* Load_Straight_Table *)

(* ----------------------------------------------------------------------- *)

function Include_File (Line : string; VAR F : text) : boolean;
  begin
    Include_File := false;
    if ((Line <> '') and (Line[1] = IncludeMark)) then
      begin
      Env.Log ('Include_File: ' + Line);
      Delete (Line, 1, 1);  (* IncludeMark *)
      Line := Strs.Upper (Strs.Trim (Line));

      if TpDOS.ExistFile (CommandPath + Line) then  (* assumes .CMD *)
        begin
        assign (F, CommandPath + Line); reset (F);  (* assumes .CMD *)
        Include_File := true;
        end;
      Env.Logout;
      end;
  end;  (* Include_File *)

(* ----------------------------------------------------------------------- *)
(* Store the (unresolved) file in the virtual screen (with colors) *)
(* Uses the global variable: RowsPerBlock *)

(* ExpIncl should be the number of levels to expand included files. *)
(* For example, 1 would cause just the files included by the main   *)
(* file to be expanded (i.e., included).  0 will cause no files to  *)
(* be expanded.  Any negative number will expand all files.         *)

procedure Load_Straight_Script (var F       : text;
                                VAR Row     : integer;
                                    Skip    : string;
                                    ExpIncl : integer);
  var Entry : string;
      IncF  : text;  (* included file *)

  begin
    Env.Log ('Load_Straight_Script: ' + Skip + ', ' + StrF (Row));

    reset (F);
    while not EOF(F) do
      begin
      readln (F, Entry);
      if ((Entry = '') or (pos (Entry[1], Skip) = 0)) then
        begin
        if ((ExpIncl <> 0) and (Include_File (Entry, IncF))) then
          Load_Straight_Script (IncF, Row, Skip, ExpIncl - 1)
        else
          begin
          Write_Script (Entry, Row, ExpIncl);
          Inc (Row);
          end;
        end;  (* if not skip line *)
      end;  (* while more lines *)

    Env.Logout;
  end;  (* Load_Straight_Script *)

(* ----------------------------------------------------------------------- *)

procedure Milestone (Attribute : word; Count : integer);
  var c, r : integer;
  begin
    r := ((Count-1) div ColsPerPage) + 1;
    c := ((Count-1) mod ColsPerPage) + 1;
    TpCRT.FastWriteWindow ('°', r, c, Attribute);
  end;  (* Milestone *)

(* ----------------------------------------------------------------------- *)
(* Resolve a table, and load it into the virtual screen (with colors). *)
(* Uses the global variable: RowsPerBlock *)
(* Sets the global variable: TotalColumns *)

procedure Load_Resolved_Table (VAR F : text; NmbrWidth : integer);

  var
    MileAttr   : word;     (* milestone attribute *)
    Entry      : string;
    Criterion  : Criteria;
    Width      : integer;
    MaxWidth   : integer;
    i          : integer;
    LineNmbr   : string;
    Status     : integer;
    Repetition : integer;
    PrevEntry  : string;
    NullSyms   : Symbols;

  begin
    Env.Log ('Load_Resolved_Table: ' + StrF(RowsPerBlock));

    TpWindow.ClearVScreen (VTxt, TxtAttr, ' ');
    MileAttr := Attr (Blue, White);
    for i := 1 to RowsPerBlock do Milestone (MileAttr, i);

    NullSyms := nil;
    MaxWidth := 0;
    Repetition := 0;
    reset (F);

    for i := 1 to RowsPerBlock do
      begin

      if (Repetition <= 0) then  (* should never be < 0 *)
        begin
        Entry := Next_Entry (F, Repetition);
        if (Repetition > 0) then
          PrevEntry := Entry;
        end
      else
        begin
        Dec (Repetition);
        Entry := PrevEntry;
        end;

      Status := 0;
      if (Entry <> '') then
        begin
        if (pos (TokenL, Entry) > 0)      then Status := 2
        else if (Entry [1] in ['0'..'9']) then Status := 1;
        end;
      case Status of
        2 : MileAttr := Attr (Red,    Blue);  (* variable *)
        1 : MileAttr := Attr (Yellow, Blue);  (* multiple *)
        0 : MileAttr := Attr (Green,  Blue);  (* standard *)
        end;
      Milestone (MileAttr, i);

      if (Status > 0) then
        Resolve_Line (Entry, NullSyms);

      if (Entry <> '') and (Entry [1] = ' ') then
        Entry := Strs.Trim (Entry);

      LineNmbr := PadNum (StrF (i), '0', NmbrWidth, true, '+');
      Write_Resolved (Entry, i, LineNmbr, NmbrWidth);
      Width := length (Entry);
      if (Width > MaxWidth) then MaxWidth := Width;
      end;  (* for each Entry *)

    TotalColumns := Min (TotalColumns, (MaxWidth + NmbrWidth));
    Finish_Load;
    
    Env.Logout;
  end;  (* Load_Resolved_Table *)

(* ----------------------------------------------------------------------- *)
(* Resolve a script, and load it into the virtual screen (with colors). *)
(* Uses the global variable: RowsPerBlock *)
(* Sets the global variable: TotalColumns *)

procedure Load_Resolved_Script (VAR F        : text;
                                VAR Row      : integer;
                                VAR MaxWidth : integer);

  var
    Line     : string;
    UserSyms : Symbols;
    IncF     : text;     (* included file *)

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  procedure Execute_Command (var Line : string);
    var Command : char;
    begin
      Env.Log ('Execute_Command: ' + Line);
      Command := Line [1];
      Delete (Line, 1, 1);
      Line := Strs.Trim (Line);

      case (Command) of
{ ? }   Question   : (* set CmdQueryMode *);
{ # }   RandomSeed : RNDM.Seed (Strs.Trim (Line));
      end;  (* case *)
      Env.Logout;
    end;  (* Execute_Command *)

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  function Next_Line : string;
    begin
      readln (F, Line);

      if (Line = '') then
        begin
        Milestone (Attr (White, Blue), Row);
        Line := ' ';  (* so it will get written out *)
        end

      else if (Line[1] = CommentMark) then  (* white space *)
        begin
        Milestone (Attr (White, Blue), Row);
        Line := '';  (* so it won't get written out *)
        end

      else
        begin
        if (pos (TokenR, Line) > 0) then (* variable *)
          begin  
          Milestone (Attr (Red, Blue), Row);
          Resolve_Line (Line, UserSyms);
          end;

        if (Line[1] in [RandomSeed, Question]) then (* command *)
          begin
          Milestone (Attr (Yellow, Blue), Row);
          Execute_Command (Line);
          Line := '';  (* so it won't get written out *)
          end
        else if (Line[1] = IncludeMark) then  (* include file *)
          Milestone (Attr (Black, Blue), Row)
        else  (* plain text *)
          Milestone (Attr (Green, Blue), Row);
        end;

      Next_Line := Line;
    end;

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  begin
    Env.Log ('Load_Resolved_Script: ' + StrF(Row));

    UserSyms := nil;

    reset (F);
    while not EOF(F) do
      begin
      Line := Next_Line;
      if (Line <> '') then
        begin
        if (Include_File (Line, IncF)) then  (* open included file *)
          Load_Resolved_Script (IncF, Row, MaxWidth)
        else
          begin
          if (length (Line) > MaxWidth) then MaxWidth := length (Line);
          Write_Script (Line, Row, -1);
          Inc (Row);
          end;
        end;
      end;  (* while not EOF *)

    Strs.SymFree (UserSyms);

    Env.Logout;
  end;  (* Load_Resolved_Script *)

(* ----------------------------------------------------------------------- *)
(* Routines which can be performed against a loaded block                  *)
(* ----------------------------------------------------------------------- *)

(* ----------------------------------------------------------------------- *)
(* Using the global Target, highlights the match within the given <Entry>. *)
(* This routine is case-INsensitive                                        *)
(* Highlight changes only the FIRST match, assuming Match has been called. *)
(* HighlightAll makes no assumptions, and highlights all matches.          *)
(* Both routines use the global variable Target, which must be initialized.*)
(* See also: Overwrite                                                     *)

  procedure Highlight (var Line : string; Row : integer; NewAttr : word);
    begin
      Env.Log ('Highlight: ' + StrF (Target.Field.Start));
      with Target.Field do
        WriteVScreen (VTxt, copy (Line, Start, Width), Row, Start, NewAttr);
      Env.LogOut;
    end;  (* Highlight *)

  function HighlightAll (Line    : string;
                         Row     : integer;
                         NewAttr : word) : integer;
    var Qty, i : integer;
    begin
      Qty := 0;
      Target.Field.Start := 1;
      Target.Field.Width := 255;
      if Match (Target, Line) then
        repeat
          Inc (Qty);
          with Target.Field do
            begin
            WriteVScreen (VTxt, copy (Line, Start, Width), Row, Start, NewAttr);
            for i := Start to Start + Width do Line [i] := ' ';
            Start := 1;
            Width := 255;
            end;
        until not Match (Target, Line);
      HighlightAll := Qty;
    end;  (* Highlight *)

(* ----------------------------------------------------------------------- *)
(* Highlight any text in the current block matching an entered pattern. *)
(* Uses the global variable: TotalColumns   *)
(* Sets the global variable: Target.Pattern *)

procedure Scan_Block (Name : string);

  var Entry    : string;
      r        : integer;
      FindAttr : word;
      Qty      : integer;

  begin
    Env.Log ('Scan_Block');
    FindAttr := Attr (LightMagenta, White);
    Ask_For_Text ('Scan Window ' + Name, 'Search Pattern: ', Target.Pattern);
    if (Target.Pattern <> '') then
      begin
      Qty := 0;
      for r := 1 to RowsPerBlock do    
        begin
        ReadVScreen (VTxt, TotalColumns, r, 1, Entry);
        Qty := Qty + HighlightAll (Entry, r, FindAttr);
        end;
      TpWindow.MoveVScreenToWindow (VTxt, CurRow, CurCol);
      Message (StrF (Qty) + ' occurrences of <' + Target.Pattern + '> found.',
               '', FindAttr);
      end;
    Env.Logout;
  end;  (* Scan_Block *)

(* ----------------------------------------------------------------------- *)
(* Seek text in the current block matching an entered pattern; jump to it. *)
(* A called routine resets the global variables CurCol and CurRow.         *)
(* Uses the global variables: Target RowsPerPage RowsPerBlock TotalColumns *)

procedure Seek_In_Block (Name : string; Tbl : integer);

  var Line  : string;
      r     : integer;
      Found : boolean;

  begin
    Env.Log ('Seek_In_Block');
    Set_Pattern (Name, Tbl, '');
    if (Target.Pattern <> '') then
      begin
      (* r := Min ((CurRow + (RowsPerPage DIV 2)), RowsPerBlock); *)
      r := CurRow;
      Found := false;
      while (r < RowsPerBlock) and not Found do
        begin
        Inc (r);
        ReadVScreen (VTxt, TotalColumns, r, 1, Line);
        Found := Match (Target, Line);
        end;  (* while *)

      if Found then
        begin
        Highlight (Line, r, Attr (Yellow, Black));
        (* ScrollTo (VTxt, CurCol, CurRow, CurCol, Max (1, (r - (RowsPerPage DIV 2)))); *)
        ScrollTo (VTxt, CurCol, Max (1, r)); (* Pattern on top row *)
        end  (* if Found *)
      else
        DM_Win.Message ('No more entries match <' + Target.Pattern + '>',
                        'Pattern Not Found', Attr (Yellow, Black));
      end;
    Env.Logout;
  end;  (* Seek_In_Block *)

(* ----------------------------------------------------------------------- *)
(* Remove entries from the current block not matching an entered pattern.  *)
(* Uses the global variable: Target RowsPerBlock TotalColumns RowsPerPage  *)

procedure Filter_Block (Operation : char; Name : string);

  var Entry     : string;
      Ptrn      : string;   (* upper-case version of Target.Pattern *)
      r         : integer;  (* current row *)
      NewRow    : integer;  (* row to move matching entry into *)
      NewTtl    : integer;  (* RowsPerBlock may decrease, so keep this *)
      Empty     : string;
      Resolved  : boolean;

  begin
    Env.Log ('Filter_Block');
    Ask_For_Text ('Filter Window ' + Name,
                  'Search Pattern: ', Target.Pattern);
    if (Target.Pattern <> '') then
      begin
      Ptrn     := Strs.Upper (Target.Pattern);
      NewTtl   := RowsPerBlock;
      NewRow   := 0;
      Resolved := Operation = 'R';

      for r := 1 to RowsPerBlock do    
        begin
        ReadVScreen (VTxt, TotalColumns, r, 1, Entry);
        if (pos (Ptrn, Strs.Upper (Entry)) > 0) then  (* found *)
          begin
          Inc (NewRow);
          if (r <> NewRow) then   (* have to move it *)
            if (Resolved) then
              Write_Resolved (Entry, NewRow, '', 0)
            else
              Write_Entry (Entry, NewRow);
          end
        else
          dec (NewTtl);  (* filter out the line *)
        end;  (* for each line *)

      (* clear to the end of the NEXT screen if necessary *)
      (* this ensures the buffer is empty for routines like Seek *)
      if (RowsPerBlock <> NewTtl) then
        begin
        Empty := Strs.Pad ('', ' ', TotalColumns, false, ' ');
        for r := NewTtl + 1 to Min (RowsPerBlock, RowsPerPage * 2) do
          if (Resolved) then
            Write_Resolved (Empty, r, '', 0)
          else
            Write_Entry (Empty, r);
        RowsPerBlock := NewTtl;
        end;

      Finish_Load;

      if (Operation = 'V') then
        begin
        EnableMenuItem  (M, ReDo);
        DisableMenuItem (M, Rslv);
        RedrawMenu (M);
        end;

      end;  (* if Pattern *)

    Env.Logout;
  end;  (* Filter_Block *)

(* ----------------------------------------------------------------------- *)
(* Free the overhead associated with the current operation. *)

procedure Switch_Operations (VAR Operation : char; NewOp : char);
  begin
    Operation := NewOp;
    TpMenu.EraseMenu (M, false);
    TpMenu.DisposeMenu (M);
    TpWindow.DisposeWindow  (TpWindow.EraseTopWindow);  (* WTxt *)
    TpWindow.DisposeVScreen (VTxt);
    if (RowsInHeader > 0) then
      begin
      TpWindow.DisposeWindow  (TpWindow.EraseTopWindow);  (* WHdr *)
      TpWindow.DisposeVScreen (VHdr);
      end;
  end;  (* Switch_Operations *)

(* ----------------------------------------------------------------------- *)

procedure View_File (TblOr0: integer; Operation : char);

(* ----------------------------------------------------------------------- *)
(* interactively display the given file *)
(* ----------------------------------------------------------------------- *)

VAR
  Tbl : integer;
  F   : text;

(* ----------------------------------------------------------------------- *)
(* Generate and display a random (resolved) entry. *)
(* Uses the global variable: ColsPerPage  *)
(* Sets the global variable: TotalColumns *)

procedure Load_Random_Entry (Tbl : integer);

  var
    Entry  : string;
    Status : integer;

  begin
    Env.Log ('Load_Random_Entry');
    TpWindow.ClearVScreen (VTxt, TxtAttr, ' ');
    Entry := File_Entry (Info [Tbl], true, NullCrit, NullStruc, Status);
    Write_Resolved (Entry, 1, '', 0);
    TotalColumns := length (Entry);
    Finish_Load;
    Env.Logout;
  end;  (* Load_Random_Entry *)

(* ----------------------------------------------------------------------- *)
(* Load the 1st entry from the selected tbl matching the entered pattern.  *)
(* Uses the global variable: ColsPerPage  *)
(* Sets the global variable: TotalColumns *)

procedure Load_Matching_Entry (Tbl : integer; Ask : boolean);

  var
    Entry  : string;
    Crit   : Criteria;
    Status : integer;

  begin
    Env.Log ('Load_Matching_Entry');
    TpWindow.ClearVScreen (VTxt, TxtAttr, ' ');
    if Ask then Set_Pattern (Info [Tbl].Name, Tbl, '');
    Crit.CriteriaType := Search;
    Crit.Search       := Target;
    Entry := File_Entry (Info [Tbl], true, Crit, NullStruc, Status);
    Write_Resolved (Entry, 1, '', 0);
    TotalColumns := length (Entry);
    Finish_Load;
    Env.Logout;
  end;  (* Load_Matching_Entry *)

(* ----------------------------------------------------------------------- *)
(* Perform the mode-specific "re-load" menu operation for views. *)
(* Sets the global variables: TotalColumns RowsInTable RowsPerBlock *)

procedure ReLoad_View (    Mode      : char;
                           Tbl       : integer;
                       var F         : text);
  begin
    Env.Log ('ReLoad_View: ' + Mode);
    case Mode of
      'V' : begin
            RowsInTable  := Info [Tbl].Lines - RowsInHeader;
            RowsPerBlock := Min (RowsInTable, RowsPerVSCreen);
            Load_Straight_Table (F, HeaderMark);
            DisableMenuItem (M, ReDo);
            EnableMenuItem  (M, Rslv);
            RedrawMenu (M);
            end;
      'R' : begin
            RowsInTable  := Info [Tbl].Entries;
            RowsPerBlock := Min (RowsInTable, RowsPerVSCreen);
            Load_Resolved_Table (F, NmbrWidth);
            end;
      'G' : Load_Random_Entry   (Tbl);
      'F' : Load_Matching_Entry (Tbl, false);
    end;  (* case *)
    Env.Logout;
  end;  (* ReLoad_View *)

(* ----------------------------------------------------------------------- *)

procedure View_Table (VAR Operation : char; Tbl : integer);
  begin
    Env.Log ('View_Table' + ' (' + Operation + ')');
    if (Operation <> 'V') then Switch_Operations (Operation, 'V');
    TotalColumns := Min (Info [Tbl].Width, 255);
    RowsInTable  := Info [Tbl].Lines;
    ViewMenu (M, Operation, 'µ C O R P S E : VIEW TABLE Æ');
    DisableMenuItem (M, ReDo);
    MakeHeaderWindow (F, Tbl, ' View ', 0);
    RowsInTable := RowsInTable - RowsInHeader;
    MakeTextWindow;
    Load_Straight_Table (F, HeaderMark);
    Env.Logout;
  end;  (* View_Table *)

(* ----------------------------------------------------------------------- *)

procedure Resolve_Table (VAR Operation : char; Tbl : integer);
  begin
    if (Operation <> 'R') then Switch_Operations (Operation, 'R');
    TotalColumns := 0;
    RowsInTable  := Info [Tbl].Entries;
    NmbrWidth := length (StrF (RowsInTable));
    ViewMenu (M, Operation, 'µ C O R P S E : RESOLVE TABLE Æ');
    MakeHeaderWindow (F, Tbl, ' Resolve ', NmbrWidth);
    MakeTextWindow;
    Load_Resolved_Table (F, NmbrWidth);
  end;  (* Resolve_Table *)

(* ----------------------------------------------------------------------- *)

  begin  (* View_File *)
    Env.Log ('>> View_File: [' + Operation + ':' + StrF (TblOr0) + '] <<');

    if (TblOr0 = 0) then Tbl := Select_Table else Tbl := TblOr0;
    if (Tbl = 0) then exit;  (* user chose none *)
    if not TpDOS.ExistFile (TablePath + Info [Tbl].Name + '.TBL') then
      begin
      Env.Error ('Unable to find: ' + Info [Tbl].Name);
      exit;
      end;
    Env.Log ('Selected Table: ' + Info [Tbl].Name); Env.Logout;
    assign (F, TablePath + Info [Tbl].Name + '.TBL');  reset (F);

    (* HiddenCursor; *)
    SetBlink (false);
    (* ClrScr; *)

    TtlAttr := Attr (LightGray, Blue);
    FrmAttr := Attr (Blue, LightGray);
    TxtAttr := Attr (Blue, White);

    case Operation of

      'V' : View_Table (Operation, Tbl);
      'R' : Resolve_Table (Operation, Tbl);

      'G' : begin  (* GENERATE RANDOM ENTRY *)
            TotalColumns := 255;
            RowsInTable  := 1;
            ViewMenu (M, Operation, 'µ C O R P S E : GENERATE RANDOM ENTRY Æ');
            MakeHeaderWindow (F, Tbl, ' Random Entry from ', 0);
            MakeTextWindow;
            Load_Random_Entry (Tbl);
            end;

      'F' : begin  (* FIND MATCHING ENTRY *)
            TotalColumns := Min (Info [Tbl].Width, 255);
            RowsInTable  := 1;
            ViewMenu (M, Operation, 'µ C O R P S E : FIND MATCHING ENTRY Æ');
            MakeHeaderWindow (F, Tbl, ' Find from ', 0);
            MakeTextWindow;
            Load_Matching_Entry (Tbl, true);
            end;

      'S' : View_Matches;

      end;  (* case *)

    MaxCol := Max (1, (TotalColumns - ColsPerPage + 1));
    MaxRow := Max (1, (RowsPerBlock - RowsPerPage + 1));
    CurCol := 1;
    CurRow := 1;
    Done   := false;

    (* - - - - - - - - - - MAIN LOOP - - - - - - - - - - *)

    repeat
      if TpMouse.MouseInstalled then
        begin
        TpMenu.EnableMenuMouse;
        TpMouse.MouseWindow (2, 2, 79, 2);
        end;

      Choice := TpMenu.MenuChoice (M, Chr);

      case TpMenu.MenuCmdNum of

        MKSExit   : Done := true;  (* ESCape from top level *)

        MKSSelect : case Choice of
          LnUp : ScrollBy (VTxt, 0,           -1, 0, MaxRow);
          LnDn : ScrollBy (VTxt, 0,            1, 0, MaxRow);
          PgDn : ScrollBy (VTxt, 0,  RowsPerPage, 0, MaxRow);
          PgUp : ScrollBy (VTxt, 0, -RowsPerPage, 0, MaxRow);
          Lt_1 : ScrollBothBy (-1, 0);
          Rt_1 : ScrollBothBy ( 1, 0);
          LtSd : ScrollBothTo (     1, CurRow);
          RtSd : ScrollBothTo (MaxCol, CurRow);

          HiLt : Scan_Block          (Info [Tbl].Name);
          Fltr : Filter_Block        (Operation, Info [Tbl].Name);
          Seek : Seek_In_Block       (Info [Tbl].Name, Tbl);
          ReDo : ReLoad_View         (Operation, Tbl, F);
          Next : Load_Matching_Entry (Tbl, false);
          Find : Load_Matching_Entry (Tbl, true);
          View : View_Table          (Operation, Tbl);
          Rslv : Resolve_Table       (Operation, Tbl);

          Quit : Done := true;  (* selected Exit *)
          else ;
          end;  (* case *)

        MKSAlpha  : case Chr of
          TpUp    : ScrollBy (VTxt, 0,           -1, 0, MaxRow);
          TpDn    : ScrollBy (VTxt, 0,            1, 0, MaxRow);
          TpPgDn  : ScrollBy (VTxt, 0,  RowsPerPage, 0, MaxRow);
          TpPgUp  : ScrollBy (VTxt, 0, -RowsPerPage, 0, MaxRow);
          TpC_PgUp: ScrollTo (VTxt, CurCol, 1);
          TpC_PgDn: ScrollTo (VTxt, CurCol, MaxRow);
          TpC_Lt  : ScrollBothBy (-1, 0);
          TpC_Rt  : ScrollBothBy ( 1, 0);
          TpHome  : ScrollBothTo (     1, CurRow);
          TpEnd   : ScrollBothTo (MaxCol, CurRow);
          TpC_Home: ScrollBothTo (1, 1);
          TpC_End : ScrollBothTo (1, MaxRow);
          else Sounds.Click;
          end;  (* case *)

        else Env.Warning ('Chr = ' + Chr + 'Code = ' + StrF (MenuCmdNum));
        end;

    until Done;

    (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

    Switch_Operations (Operation, 'X');
    close (F);

    (* NormalCursor; *)
    (* SetBlink (true); *)

    Env.Logout;
  end;  (* View_File *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)

procedure Execute_File (Path : string);

(* ----------------------------------------------------------------------- *)
(* interactively display the given script *)
(* ----------------------------------------------------------------------- *)

var
  Operation : char;
  FileName  : string;
  F         : text;

(* ----------------------------------------------------------------------- *)
(* count lines in data file *)

procedure Size_File (var F        : text;
                         Skip     : string;
                     VAR Lines    : integer;
                     VAR MaxWidth : integer;
                         ExpIncl  : integer); (* expand included files? *)

  var Line  : string;
      Width : integer;  (* current line width *)
      LnQty : integer;  (* 1; or # of lines in included file *)
      IncF  : text;     (* included file *)

  begin
    Env.Log ('Size_File: ' + Skip);

    Lines    := 0;
    MaxWidth := 0;
    reset (F);
    while not EOF (F) do
      begin
      readln (F, Line);
      if ((Line = '') or (pos (Line[1], Skip) = 0)) then
        begin
        if ((ExpIncl <> 0) and Include_File (Line, IncF)) then
          Size_File (IncF, Skip, LnQty, Width, ExpIncl - 1)
        else
          begin
          LnQty := 1;
          Width := Length (Line);
          end;

        Inc (Lines, LnQty);
        if (Width > MaxWidth) then MaxWidth := Width;  (* save max width *)
        end;  (* if not a skip line *)
      end;  (* while more lines *)
    reset (F);

    Env.LogOut;
  end;  (* Size_File *)

(* ----------------------------------------------------------------------- *)
(* Perform the mode-specific "re-load" menu operation for scripts. *)
(* Sets the global variables: TotalColumns RowsInTable RowsPerBlock *)

procedure ReLoad_Script (Mode : char; var F : text);
  var Row, MaxWidth : integer;
      MileAttr : byte;
  begin
    Env.Log ('ReLoad_Script: ' + Mode);
    case Mode of
      'V' : begin
            Size_File (F, CommentMark, RowsInTable, MaxWidth, 0);
            RowsPerBlock := Min (RowsInTable, RowsPerVSCreen);
            TpWindow.ClearVScreen (VTxt, TxtAttr, ' ');
            CurRow := 1;
            Load_Straight_Script (F, CurRow, CommentMark, 0);
            Finish_Load;
            DisableMenuItem (M, ReDo);
            EnableMenuItem  (M, Rslv);
            RedrawMenu (M);
            end;
      'R' : begin
            Size_File (F, CommentMark + RandomSeed,
                       RowsInTable, MaxWidth, -1);
            RowsPerBlock := Min (RowsInTable, RowsPerVSCreen);
            TpWindow.ClearVScreen (VTxt, TxtAttr, ' ');
            MileAttr := Attr (Blue, White);
            for Row := 1 to RowsPerBlock do Milestone (MileAttr, Row);
            Row := 1;
            MaxWidth := 0;
            Load_Resolved_Script (F, Row, MaxWidth);
            TotalColumns := Min (TotalColumns, MaxWidth);
            Finish_Load;
            end;
    end;  (* case *)
    Env.Logout;
  end;  (* ReLoad_Script *)

(* ----------------------------------------------------------------------- *)

procedure View_Script (VAR Operation : char);
  begin
    Env.Log ('View_Script' + ' (' + Operation + ')');
    if (Operation <> 'V') then Switch_Operations (Operation, 'V');
    Size_File (F, CommentMark, RowsInTable, TotalColumns, 0);
    TotalColumns := Min (TotalColumns, 255);
    ViewMenu (M, Operation, 'µ C O R P S E : VIEW SCRIPT Æ');
    DisableMenuItem (M, ReDo);
    RowsInHeader := 0;
    MakeTextWindow;
    TpWindow.ClearVScreen (VTxt, TxtAttr, ' ');
    CurRow := 1;
    Load_Straight_Script (F, CurRow, CommentMark, 0);
    Finish_Load;
    Env.Logout;
  end;  (* View_Script *)

(* ----------------------------------------------------------------------- *)

procedure Resolve_Script (VAR Operation : char);
  var Row, MaxWidth : integer;
      MileAttr : byte;
  begin
    if (Operation <> 'R') then Switch_Operations (Operation, 'R');
    Size_File (F, CommentMark + RandomSeed, RowsInTable, TotalColumns, -1);
    TotalColumns := 0;
    ViewMenu (M, Operation, 'µ C O R P S E : RESOLVE SCRIPT Æ');
    MakeTextWindow;
    TpWindow.ClearVScreen (VTxt, TxtAttr, ' ');
    MileAttr := Attr (Blue, White);
    for Row := 1 to RowsPerBlock do Milestone (MileAttr, Row);
    Row := 1;
    MaxWidth := 0;
    Load_Resolved_Script (F, Row, MaxWidth);
    TotalColumns := Min (TotalColumns, MaxWidth);
    Finish_Load;
  end;  (* Resolve_Script *)

(* ----------------------------------------------------------------------- *)

  begin  (* Execute_File *)
    Env.Log ('>> Execute_File: [' + Path + '] <<');

    FileName := Select_Script;
    if (FileName = '') then exit;  (* user chose none *)
    if not TpDOS.ExistFile (CommandPath + FileName) then
      begin
      Env.Error ('Unable to find: ' + CommandPath + FileName);
      exit;
      end;
    Env.Log ('Selected Script: ' + FileName); Env.Logout;
    assign (F, CommandPath + FileName);  reset (F);

    (* HiddenCursor; *)
    SetBlink (false);
    (* ClrScr; *)

    TtlAttr := Attr (LightGray, Blue);
    FrmAttr := Attr (Blue, LightGray);
    TxtAttr := Attr (Blue, White);

    Operation := 'V';

    case Operation of
      'V' : View_Script (Operation);
      'R' : Resolve_Script (Operation);
      'S' : View_Matches;
    end;  (* case *)

    MaxCol := Max (1, (TotalColumns - ColsPerPage + 1));
    MaxRow := Max (1, (RowsPerBlock - RowsPerPage + 1));
    CurCol := 1;
    CurRow := 1;
    Done   := false;

    (* - - - - - - - - - - MAIN LOOP - - - - - - - - - - *)

    repeat
      if TpMouse.MouseInstalled then
        begin
        TpMenu.EnableMenuMouse;
        TpMouse.MouseWindow (2, 2, 79, 2);
        end;

      Choice := TpMenu.MenuChoice (M, Chr);

      case TpMenu.MenuCmdNum of

        MKSExit   : Done := true;  (* ESCape from top level *)

        MKSSelect : case Choice of
          LnUp : ScrollBy (VTxt, 0,           -1, 0, MaxRow);
          LnDn : ScrollBy (VTxt, 0,            1, 0, MaxRow);
          PgDn : ScrollBy (VTxt, 0,  RowsPerPage, 0, MaxRow);
          PgUp : ScrollBy (VTxt, 0, -RowsPerPage, 0, MaxRow);
          Lt_1 : ScrollBothBy (-1, 0);
          Rt_1 : ScrollBothBy ( 1, 0);
          LtSd : ScrollBothTo (     1, CurRow);
          RtSd : ScrollBothTo (MaxCol, CurRow);

          HiLt : Scan_Block     (FileName);
          Fltr : Filter_Block   (Operation, FileName);
          Seek : Seek_In_Block  (FileName, 0);
          ReDo : ReLoad_Script  (Operation, F);
          View : View_Script    (Operation);
          Rslv : Resolve_Script (Operation);

          Quit : Done := true;  (* selected Exit *)
          else ;
          end;  (* case *)

        MKSAlpha  : case Chr of
          TpUp    : ScrollBy (VTxt, 0,           -1, 0, MaxRow);
          TpDn    : ScrollBy (VTxt, 0,            1, 0, MaxRow);
          TpPgDn  : ScrollBy (VTxt, 0,  RowsPerPage, 0, MaxRow);
          TpPgUp  : ScrollBy (VTxt, 0, -RowsPerPage, 0, MaxRow);
          TpC_PgUp: ScrollTo (VTxt, CurCol, 1);
          TpC_PgDn: ScrollTo (VTxt, CurCol, MaxRow);
          TpC_Lt  : ScrollBothBy (-1, 0);
          TpC_Rt  : ScrollBothBy ( 1, 0);
          TpHome  : ScrollBothTo (     1, CurRow);
          TpEnd   : ScrollBothTo (MaxCol, CurRow);
          TpC_Home: ScrollBothTo (1, 1);
          TpC_End : ScrollBothTo (1, MaxRow);
          else Sounds.Click;
          end;  (* case *)

        else Env.Warning ('Chr = ' + Chr + 'Code = ' + StrF (MenuCmdNum));
        end;

    until Done;

    (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

    Switch_Operations (Operation, 'X');
    close (F);

    (* NormalCursor; *)
    (* SetBlink (true); *)

    Env.Logout;
  end;  (* Execute_File *)

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
            writeln (MatchF, SourceMark + ' ' + TpString.JustPathName (Path));
            end;
          FirstInFile := false;
          writeln (MatchF);
          writeln (MatchF, SubHeaderMark + ' <' +
                           TpString.JustFileName (Path) + '>');
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
    Env.Log ('Search_Directory: ' + Path);
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
      else if (TpString.JustExtension (F.Name) = 'TBL') then
        Search_Table (Path + F.Name, Pattern, MatchF, FirstInDir);
      DOS.FindNext (F);
      end;

    Env.LogOut;
  end;  (* Search_Directory *)

(* ----------------------------------------------------------------------- *)

procedure View_Matches;

  var MatchF   : text;
      FileName : string;

  begin
    Env.Log ('View_Matches');

    Ask_For_Text ('SEARCH ALL TABLES', 'Search Pattern: ', Target.Pattern);
    if (Target.Pattern <> '') then
      begin
      FileName := TablePath + TempMark +
                  Upper (copy (Target.Pattern, 1, 6)) + '.TBL';
      assign (MatchF, FileName);  rewrite (MatchF);
      writeln (MatchF, HeaderMark + ' TABLE ENTRIES MATCHING: ' + Target.Pattern);
      Search_Directory (DM_FMS.TablePath, Upper (Target.Pattern), MatchF);
      writeln (MatchF);
      close (MatchF);
      Index_File (FileName);
      View_File (FileFromName (FileName), 'V');
      erase (MatchF);
      end;

    Env.Logout;
  end;  (* View_Matches *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)
(* interactively create, index, and save a new file *)

procedure Create_Table (Choice : integer);

  var WhichFile : integer;
      Qty       : integer;
      s, Name   : string;
      F, F2     : text;

  procedure AddFile (Name : string; Lines : integer);
    begin
      Env.Log ('AddFile: ' + Name);
      TblQty := TblQty + 1;
      Info [TblQty].Name    := Name;
      Info [TblQty].Entries := Lines;
      Env.Logout;
    end;  (* AddFile *)

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)
  function AddLines : integer;  (* # of lines added *)
    var Line    : string;
        Entries : integer;
    begin
      Env.Log ('AddLines');
      Entries := 0;
      TextColor (Green);
      Line := '';
      repeat
        readln (Line); (* Line := PROMPT.Query (0, '', Green, Line, []); *)
        writeln;
        if (Line = '') then begin end  (* empty line means done *)
        else
          begin
          writeln (F, Line);
          if (Line [1] <> ' ') then Entries := Entries + 1;
          end;
      until (Line = '');
      close (F);
      Env.Logout;
      AddLines := Entries;  (* # of lines added *)
    end;  (* AddLines *)
  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  begin  (* Create_Table *)

    Env.Log ('>> Create_Table: ' + StrF (Choice) + ' <<');

    case Choice of
      1 : begin  (* criteria  *)
          WhichFile := Select_Table;
          if (WhichFile = 0) then EXIT;  (* user selected none *)
          s := UserPath +
               copy(Info [ WhichFile ].Name, length(TablePath)+1, 255);
          Name := 'test.tbl';  (* PROMPT.NewFileName (s); !!!!!! *)
          assign (F, NameFromFile (WhichFile)); reset (F);
          assign (F2, Name);  rewrite (F2);
          Set_Pattern (Name, WhichFile, '');
          Qty := 0;  (* number of lines in new file *)
          while not EOF (F) do
            begin
            readln (F, s);
            if (s [1] <> ' ') then writeln (F2, s)
            else if true (* Match (Target, s) !!!!!!!!!!!!! *) then
              begin writeln (F2, s); Qty := Qty + 1; end;
            end;  (* while *)
          close (F); close (F2);
          AddFile (Name, Qty);
          end;   (* criteria *)

      2 : begin  (* y/n query *)
          Sounds.Click;
          end;   (* y/n query *)

      3 : begin  (* new file  *)
          s := 'test.tbl';  (* PROMPT.NewFileName (UserPath); !!!!! *)
          assign (F, s); rewrite (F);
          ClrScr;
          writeln ('Enter Lines (blank line to end)');
          AddFile (s, AddLines);
          end;   (* new file *)

      4 : begin  (* add lines *)
          WhichFile := Select_Table;
          if (WhichFile = 0) then EXIT;  (* user selected none *)
          assign (F, NameFromFile (WhichFile));
          ClrScr;
          writeln ('Enter Lines (blank line to end)');
          (* ShowHeader (F, 'H');  (* resets F *)
          close (F); append (F);
          Info [ WhichFile ].Entries :=
            Info [ WhichFile ].Entries + AddLines;
          end;   (* add lines *)

      end;  (* case *)

    Env.Logout;
  end;  (* Create_Table *)

(* --------------------------------------------------------------------- *)

BEGIN
  Env.Log ('Unit DM_View');  Env.Logout;
END.  (* unit DM_View *)

