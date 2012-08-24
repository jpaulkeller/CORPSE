(* File: DM_ASK.pas *)

UNIT DM_ASK;

INTERFACE

{
function  Ask_For_Num      (    Title        : string;
                                Prompt       : string;
                                Lo, Hi       : integer;
                            VAR Response     : integer   ): boolean;
procedure Ask_For_Text     (    Title        : string;
                                Prompt       : string;
                            VAR Response     : string    );
function  Ask_User         (VAR Line         : string    ): string;
function  Chosen_Entry     (    Prompt       : string;
                                Tbl          : integer;
                                StrucSpec    : string;
                                Sort         : boolean;
                            VAR Criterion    : Criteria  ): string;
function  Chosen_Entries   (    Prompt       : string;
                                Tbl          : integer;
                                StrucSpec    : string;
                                Sort         : boolean;
                            VAR Criterion    : Criteria  ): string;
procedure Entry_Menu       (    Prompt       : string;
                                Tbl          : integer;
                                StrucSpec    : string;
                                Sort         : boolean;
                            VAR Criterion    : Criteria;
                            VAR M            : MenuPtrs  );
function  Get_Entry        (    Prompt       : string;
                                Tbl          : integer;
                                Field        : string;
                                StrucSpec    : string    ): string;

function  Multiple_Entries (var M            : MenuPtrs;
                                Default      : word;
                            VAR Criterion    : Criteria)  : string;
procedure PickField        (VAR FldVar       : string;
                                MaxLen       : integer;
                                Prompt       : string;
                                TableName    : string;
                                Field        : string    );
}
{function  Select_Script                                   : string;}
function  Pick_File        (    DialogTitle,
                                Prompt,
                                Path,
                                Specification,
                                TitleSuffix  : string    ): string;
function  Pick_Table                                      : integer;
{
procedure Set_Pattern      (    Name         : string;
                                Tbl          : integer;
                                Default      : string    );
}

(* ---------------------------------------------------------------------- *)

IMPLEMENTATION

USES
{
  App,
  Dialogs,
  Objects,
  StdDlg,
  WinDos;
}
  App,
  Count,
  Dialogs,
  Drivers,
  Editors,
  Memory,
  Menus,
  Objects,
  Orders,
  StdDlg,
  Stocks,
  Strings,
  Supplier,
  Validate,
  Views,
  WinDos,
  Strs,
  DM_Const,
  DM_Types,
  DM_View;

(* ---------------------------------------------------------------------- *)

function Pick_File (DialogTitle,
                    Prompt,
                    Path,
                    Specification,
                    TitleSuffix : string) : string;
const
  FDOptions: Word = fdOKButton or fdOpenButton;
var
  FileDialog: PFileDialog;
  FilePath: FNameStr;
  FileName: array[0..fsFileName] of Char;
begin
  FilePath := Path + Specification;
  FileDialog := New(PFileDialog,
                    Init(FilePath, DialogTitle, Prompt, FDOptions, 1));
  if ExecuteDialog(FileDialog, @FilePath) <> cmCancel then
    Pick_File := StrPas(FilePath)
  else
    Pick_File := '';
end;  (* Pick_File *)

(* ---------------------------------------------------------------------- *)

function Pick_Table : string;
begin
  Pick_Table :=
    Pick_File ('Select TABLE', '~T~able Path', 'TABLES\', '*.TBL', ' TABLE');
end;  (* Pick_Table *)

{
(* ----------------------------------------------------------------------- *)
(* Menu-prompt the user to select a (possibly structured) entry (index). *)

procedure Entry_Menu (    Prompt    : string;
                          Tbl       : integer;
                          StrucSpec : string;
                          Sort      : boolean;
                      VAR Criterion : Criteria;
                      VAR M         : MenuPtrs);

  var i          : integer;
      Structure  : Structures;
      F          : text;
      Entry      : string;
      Repetition : integer;

  begin
    Env.Log ('Entry_Menu: ' + Prompt);
    Pop_Structure (StrucSpec, Tbl, Structure);
    assign (F, NameFromFile (Tbl)); reset (F);

    Mark (HeapPtr);
    M := JMenu.Create (Or_(Prompt, 'Select Entry: '), nil, Sort);

    if (Criterion.CriteriaType <> Index) then
      begin
      Criterion.CriteriaType := Index;
      Criterion.Index := Info [Tbl].Entries;
      end
    else if (Criterion.Index = 0) or
            (Criterion.Index > Info [Tbl].Entries) then
      Criterion.Index := Info [Tbl].Entries;

    for i := 1 to Criterion.Index do
      begin
      Entry := Next_Entry (F, Repetition);
      if (Structure.Start > 0) then
        Entry := copy (Entry, Structure.Start, Structure.Width);
      Entry := Strs.Trim (Entry);
      JMenu.AddItem (M, Entry, i);
      end;  (* for each entry *)

    close (F);

    JMenu.Finalize (M);
    Env.Logout;
  end;  (* Entry_Menu *)

(* ----------------------------------------------------------------------- *)
(* Menu-prompt the user to select a (possibly structured) entry. *)

function Chosen_Entry (    Prompt    : string;
                           Tbl       : integer;
                           StrucSpec : string;
                           Sort      : boolean;
                       VAR Criterion : Criteria) : string;

  (* Prompt the user for an entry by showing all field values;       *)
  (* return the field value selected, and set the criterion, so that *)
  (* a subsequent call to File_Entry will return the selected entry. *)
  (* Criterion.Index may be passed in as a limit on the # of items.  *)

  var M     : MenuPtrs;
      Entry : string;

  begin
    Env.Log ('Chosen_Entry: ' + Prompt);
    Entry_Menu (Prompt, Tbl, StrucSpec, Sort, Criterion, M);
    TpMouse.InitializeMouse;
    Criterion.Index := JMenu.Choice (M, 1);
    if (Criterion.Index = 0) then Entry := ''
    else Entry := JMenu.LabelFromValue (Criterion.Index);
Env.Log ('>> ' + StrF (Criterion.Index) + ': ' + Entry); Env.Logout;
    Release (HeapPtr);
    Chosen_Entry := Entry;
    Env.Logout;
  end;  (* Chosen_Entry *)

(* ----------------------------------------------------------------------- *)

function Multiple_Entries (var M         : MenuPtrs;
                               Default   : word;
                           VAR Criterion : Criteria) : string;

  (* Quick & dirty method of providing a multiple-choice menu. *)
  (* Calling routine MUST call Release (HeapPtr) itself.       *)

  var Entry : string;
      Item  : Items;

  begin
    Env.Log ('Multiple_Entries');
    Criterion.Index := JMenu.Choice (M, Default);
    if (Criterion.Index = 0) then Entry := ''
    else
      begin
      Item            := JMenu.ItemFromPick (Criterion.Index);
      Entry           := Item^.Lbl;
      Criterion.Index := Item^.Val;
      end;

    Env.Logout;
    Multiple_Entries := Entry;
  end;  (* Multiple_Entries *)

(* ----------------------------------------------------------------------- *)

function Chosen_Entries (    Prompt    : string;
                             Tbl       : integer;
                             StrucSpec : string;
                             Sort      : boolean;
                         VAR Criterion : Criteria) : string;

  var M         : MenuPtrs;
      Item      : Items;
      i         : integer;
      FldVal    : string;
      Structure : Structures;
      Status    : integer;

  begin
    Env.Log ('Chosen_Entries: ' + Prompt);
    Criterion.CriteriaType := Index;
    Pop_Structure (StrucSpec, Tbl, Structure);

    Mark (HeapPtr);
    M := JMenu.Create (Or_(Prompt, 'Select Entry: '), nil, Sort);
    JMenu.AddItem (M, '<CANCEL>', 0);
    for i := 1 to Info [Tbl].Entries do
      begin
      Criterion.Index := i;
      FldVal := File_Entry (Info [Tbl], true, Criterion, Structure, Status);
      JMenu.AddItem (M, FldVal, Criterion.Index);
      end;  (* for each entry *)
    JMenu.AddItem (M, '< ALL >', Criterion.Index + 1);

    JMenu.Finalize (M);
    Criterion.Index := JMenu.Choice (M, 1);

    if (Criterion.Index = 0) then FldVal := ''
    else
      begin
      Item            := JMenu.ItemFromPick (Criterion.Index);
      FldVal          := Item^.Lbl;
      Criterion.Index := Item^.Val;
      end;

    Release (HeapPtr);

    Env.Logout;
    Chosen_Entries := FldVal;
  end;  (* Chosen_Entries *)

(* ----------------------------------------------------------------------- *)

procedure Ask_For_Text (    Title    : string;
                            Prompt   : string;
                        VAR Response : string);

  var Escaped : boolean;
      Width   : integer;
      W       : TpWindow.WindowPtr;
      Status  : MouseStatus;

  begin
    Env.Log ('Ask_For_Text: ' + Prompt);
    (* TpEdit.ClearFirstChar  := false; *)
    (* TpEdit.CursorToEnd     := false; *)
    (* TpEdit.EditMapCtrls    := false; *)
    (* TpEdit.ForceUpper      := true;  *)
    (* TpEdit.InsertByDefault := false; *)
    (* TpEdit.TrimBlanks      := false; *)

    TpEdit.WindowRelative  := true;

    W := DM_Win.PopUp (60, Title, Attr (Magenta, White));
    Width := 59 - length (Prompt);

    if TpMouse.MouseInstalled then
      begin
      TpEdit.EnableEditMouse;
      DM_Win.PushMouse (Status, Attr (Black, Yellow));
      end;

    TpEdit.ReadString (Prompt, 1, 1, Width,
                       Attr (Magenta, White),
                       Attr (Magenta, Yellow),
                       Attr (Red, White),
                       Escaped, Response);

    if TpMouse.MouseInstalled then
      DM_Win.PopMouse (Status);
    DisposeWindow (EraseTopWindow);
    if Escaped then Response := '';
    Env.Logout;
  end;  (* Ask_For_Text *)


(* ----------------------------------------------------------------------- *)

function Ask_For_Num (    Title    : string;
                          Prompt   : string;
                          Lo, Hi   : integer;
                      VAR Response : integer) : boolean;

  var Escaped : boolean;
      W       : TpWindow.WindowPtr;

  begin
    Env.Log ('Ask_For_Num: ' + Prompt);
    (* TpEdit.ClearFirstChar  := false; *)
    (* TpEdit.CursorToEnd     := false; *)
    (* TpEdit.EditMapCtrls    := false; *)
    (* TpEdit.ForceUpper      := true;  *)
    (* TpEdit.InsertByDefault := false; *)
    (* TpEdit.TrimBlanks      := false; *)

    TpEdit.WindowRelative  := true;
    EnableEditMouse;

    W := DM_Win.PopUp (60, Title, Attr (Magenta, White));
    TpEdit.ReadInteger (Prompt, 1, 1, 59 - length (Prompt),
                        Attr (Magenta, White),
                        Attr (Magenta, Yellow),
                        Lo, Hi,
                        Escaped, Response);
    DisposeWindow (EraseTopWindow);
    Ask_For_Num := not Escaped;
    Env.Logout;
  end;  (* Ask_For_Num *)

(* ----------------------------------------------------------------------- *)
(* Queries the user with a menu or prompt for string. *)

function Ask_User (var Line : string) : string;
}
  (* A prompt should be in the form: {? Prompt(Default)} *)
  (*                             or: {? Table} *)
{
  var s         : string;
      Criterion : Criteria;
      Prompt    : string;
      Field     : string;
      Tbl       : integer;
      Sepr      : char;

  begin
    Env.Log ('Ask_User: ' + Line);

    Delete (Line, 1, 1);
    Line := Strs.Trim (Line);

    if (Pos (DefaultL, Line) > 0) then  (* TBD: this could be better *)
      begin
      s := Strs.SubStr (Line, DefaultL, DefaultR);  (* default *)
      Prompt := Strs.SubStr (Line, '', DefaultL) + ' ';
      Ask_For_Text ((* title *) '', Prompt, s);
      end

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

    Ask_User := s;

    Env.Logout;
  end;  (* Ask_User *)

(* ----------------------------------------------------------------------- *)
(* Interactively set the global pattern variables. *)

procedure Set_Pattern (Name : string; Tbl : integer; Default : string);

  var Fld, i : integer;
      Field  : Fields;
      M      : MenuPtrs;

  begin
    Env.Log ('Set_Pattern: ' + Name + ', ' + Default);

    if (Default <> '') then Target.Pattern := Default;
    Ask_For_Text ('Search Pattern' + Name, 'Enter Pattern: ', Target.Pattern);
    if (Target.Pattern = '') then
      begin
      Target.Field := NullStruc;
      Exit;
      end;

    Target.Field.Start := 1;
    Target.Field.Width := 255;

    if (Tbl > 0) then
      begin
      Fld   := 0;
      Field := Info [Tbl].Field;
      Mark (HeapPtr);

      if (Field <> NIL) then
        begin
        M := JMenu.Create ('Select Field:', nil, false);
        JMenu.AddItem (M, '<Entire Line>', 1);
        JMenu.AddItem (M, '<Ad Hoc>'     , 2);
        Fld := 3;
        while (Field <> NIL) do
          begin
          JMenu.AddItem (M, Field^.Name, Fld);
          Fld   := Fld + 1;
          Field := Field^.Next;
          end;  (* for each field *)
        JMenu.Finalize (M);
        Fld := JMenu.Choice (M, 1);
        end;  (* if fields *)

      if (Fld = 2) then   (* prompt for start# and width# *)
        with Target.Field do
          begin
          (* if Header then ShowHeader (F, HeaderMark + PositionMark); *)
          if not Ask_For_Num ('SEARCH COLUMNS', 'Starting Column: ',
                              1, 255, Start) then Start := 1;
          Width := Length (Target.Pattern);
          if not Ask_For_Num ('SEARCH COLUMNS', 'Column Width: ',
                              1, 255, Width) then Width := 1;
          end
      else   (* look up start# and width# for field *)
        begin
        Field := Info [Tbl].Field;
        for i := 4 to Fld do Field := Field^.Next;
        Target.Field := Field^.Value;
        end;

      Release (HeapPtr);
      end;

    Env.Logout;
  end;  (* Set_Pattern *)

(* ----------------------------------------------------------------------- *)

  procedure PickField (VAR FldVar    : string;
                           MaxLen    : integer;
                           Prompt    : string;
                           TableName : string;
                           Field     : string);
  var Save : string;
      Tbl  : integer;
  begin
    Env.Log ('PickField: ' + TableName + ':' + Field);
    Save := FldVar;
    Tbl  := FileFromName (TableName);
    FldVar := Get_Entry ('Which ' + Prompt + '?', Tbl, Field, Field);
    if (PickCmdNum <> PKSSelect) then FldVar := Save  (* user ESCaped *)
    else FldVar := Strs.Truncate (Strs.Compress (FldVar, 0), MaxLen);
    Env.Logout;
  end;  (* PickField *)

(* --------------------------------------------------------------------- *)
(* Ask the user to select a (possibly structured) entry from <Tbl>, *)
(* using a menu whose items are values from the <Field> column.     *)

(* WARNING: this may not work for weighted files ??? 1/20/93 *)

function Get_Entry (Prompt    : string;
                    Tbl       : integer;
                    Field     : string;
                    StrucSpec : string) : string;

  var Criterion : Criteria;
      s         : string;
      Status    : integer;
      Structure : Structures;

  begin
    Env.Log ('Get_Entry: ' + Prompt);
    Criterion.CriteriaType := Index;  Criterion.Index := 0;
    s := Chosen_Entry (Prompt, Tbl, Field, true, Criterion);
Env.Log ('* Get_Entry (choose) ==> ' + s);  Env.Logout;
    if (s <> '') then
      begin
      Pop_Structure (StrucSpec, Tbl, Structure);
      s := File_Entry (Info [Tbl], true, Criterion, Structure, Status);
Env.Log ('* Get_Entry (fentry) ==> ' + s);  Env.Logout;
      end;

    Get_Entry := s;
    Env.Logout;
  end;  (* Get_Entry *)
}
