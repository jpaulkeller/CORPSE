
{$M 65520,16384,655360}  (* Stack size, minimum and maximum heap sizes    *)
{.$M 49152,16384,655360}  (* Stack size, minimum and maximum heap sizes    *)
{.$M 32768,16384,655360}  (* Stack size, minimum and maximum heap sizes    *)

program DM;

uses
  App,
  Count,
  Dialogs,
  Drivers,
  Editors,
  Memory,
  Menus,
  MsgBox,
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
  Util,

  DM_Const,
  DM_Hint,
  DM_FMS,
  DM_Msg,
  DM_Types,
  DM_View;

var
  ResFile: TResourceFile;

type
  DM_App = object(TApplication)
    ClipboardWindow: PEditWindow;
    OrderWindow: POrderDialog;

    constructor Init;
    destructor Done; virtual;
    procedure CancelOrder;
    procedure DoAboutBox;
    procedure EnterNewOrder;
    procedure HandleEvent(var Event: TEvent); virtual;
    procedure InitMenuBar; virtual;
    procedure InitStatusLine; virtual;
    procedure LoadDesktop;
    procedure NewWindow(W, H: integer);
    procedure OpenOrderWindow;
    procedure OpenWindow;
    procedure SaveDesktop;
    procedure SaveOrderData;
    procedure ShowOrder(AOrderNum: Integer);
    procedure ViewTable (Name: string;
                         BoxP: PRect;
                         Help: integer;
                         Opts: word);

    procedure RandomEntry(Name: string; BoxP: PRect);
    procedure ReplaceRandom;
    procedure AnotherRandom;

    procedure FindEntry(Name: string; Pattern: string; BoxP: PRect);
    procedure ReplaceFind;
    procedure AnotherFind;

    procedure ExpandMacros(Name: string; BoxP: PRect);
    procedure ReplaceExpanded;
    procedure AnotherExpanded;

    procedure ScanTables(Pattern : string);
    procedure IndexTables;

    procedure ViewScript;
    procedure ExecuteScript;

  private
    function  PickFile (DialogTitle,
                        Prompt,
                        Path,
                        Specification : string) : string;
    function  PickTable (Relative : boolean) : string;
    function  PickScript : string;

  end;

(* ----------------------------------------------------------------------- *)

procedure DM_StreamError(var S: TStream); far;
var
  ErrorMessage: String;
begin
  case S.Status of
    stError:      ErrorMessage := 'Stream access error';
    stInitError:  ErrorMessage := 'Cannot initialize stream (CORPSE.TVR)';
    stReadError:  ErrorMessage := 'Read beyond end of stream';
    stWriteError: ErrorMessage := 'Cannot expand stream';
    stGetError:   ErrorMessage := 'Unregistered type read from stream';
    stPutError:   ErrorMessage := 'Unregistered type written to stream';
    end;
  DoneVideo;
  PrintStr('Error: ' + ErrorMessage);
  Halt(Abs(S.Status));
end; (* StreamError *)

(* ----------------------------------------------------------------------- *)

constructor DM_App.Init;
var
  R: TRect;
begin
  MaxHeapSize  := 8192;
  EditorDialog := StdEditorDialog;
  StreamError  := @DM_StreamError;
  RegisterObjects;
  RegisterViews;
  RegisterMenus;
  RegisterEditors;
  RegisterApp;
  RegisterDialogs;
  RegisterValidate;
  RegisterGlobals;
  RegisterOrders;
  RegisterCount;
  RegisterStocks;

  Register_HintStatusLine;
  Register_DM_View;
  Register_EntryDialog;

  ResFile.Init(New(PBufStream, Init('CORPSE.TVR', stOpen, 1024)));
  inherited Init;
  Desktop^.GetExtent(R);
  Desktop^.TileColumnsFirst := true;
  ClipBoardWindow := New(PEditWindow, Init(R, '', wnNoNumber));
  if Application^.ValidView(ClipboardWindow) <> nil then
  begin
    ClipboardWindow^.Hide;
    InsertWindow(ClipboardWindow);
    Clipboard := ClipboardWindow^.Editor;
    Clipboard^.CanUndo := False;
  end;

  DM_FMS.Index_Files(false);
  LoadOrders;
  CurrentOrder := 0;
  OrderInfo := POrderObj(OrderColl^.At(CurrentOrder))^.TransferRecord;
  LoadStock;
  LoadSuppliers;
  DisableCommands([cmOrderNext, cmOrderPrev, cmOrderCancel, cmOrderSave]);
end; (* Init *)

(* ----------------------------------------------------------------------- *)

destructor DM_App.Done;
begin
  if Assigned(OrderColl) then Dispose(OrderColl, Done);
  inherited Done;
  ResFile.Done;
end; (* Done *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.CancelOrder;
begin
  if CurrentOrder < OrderColl^.Count then
    ShowOrder(CurrentOrder)
  else
  begin
    Dispose(TempOrder, Done);
    ShowOrder(CurrentOrder - 1);
  end;
  EnableCommands([cmOrderNew]);
end; (* CancelOrder *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.DoAboutBox;
begin
  ExecuteDialog(PDialog(ResFile.Get('ABOUTBOX')), nil);
end; (* DoAboutBox *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.EnterNewOrder;
begin
  OpenOrderWindow;
  CurrentOrder := OrderColl^.Count;
  TempOrder := New(POrderObj, Init);
  OrderInfo := TempOrder^.TransferRecord;
  with OrderWindow^ do
  begin
    SetData(OrderInfo);
    Counter^.SetCurrent(CurrentOrder + 1);
  end;
  DisableCommands([cmOrderNext, cmOrderPrev, cmOrderNew]);
  EnableCommands([cmOrderCancel, cmOrderSave]);
end; (* EnterNewOrder *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.HandleEvent(var Event: TEvent);
var
  ClearFlag : boolean;
begin
  inherited HandleEvent(Event);
  if Event.What = evCommand then
  begin
    ClearFlag := true;
    case Event.Command of
      cmOpen:          OpenWindow;
      cmNew:           NewWindow(75, 20);
      cmClipShow:      with ClipBoardWindow^ do begin Select; Show; end;
      cmAbout:         DoAboutBox;
      cmOrderWin:      OpenOrderWindow;
      cmStockWin:      ExecuteDialog(New(PStockDialog, Init), StockInfo);
      cmSupplierWin:   ExecuteDialog(New(PSupplierDialog, Init), SupplierInfo);

      cmOrderNew:      EnterNewOrder;
      cmOrderSave:     SaveOrderData;
      cmOrderCancel:   CancelOrder;
      cmOrderNext:     ShowOrder(CurrentOrder + 1);
      cmOrderPrev:     ShowOrder(CurrentOrder - 1);

      cmOptionsVideo:  SetScreenMode(ScreenMode xor smFont8x8);
      cmOptionsSave:   SaveDesktop;
      cmOptionsLoad:   LoadDesktop;

      cmViewTable:     ViewTable('', nil, 0, 0);

      cmFindEntry:     FindEntry('', '', nil);
      cmReplaceFind:   ReplaceFind;
      cmAnotherFind:   AnotherFind;

      cmRandomEntry:   RandomEntry('', nil);
      cmReplaceRandom: ReplaceRandom;
      cmAnotherRandom: AnotherRandom;

      cmExpandMacros:    ExpandMacros('', nil);
      cmReplaceExpanded: ReplaceExpanded;
      cmAnotherExpanded: AnotherExpanded;

      cmScanTables:    ScanTables('');
      cmIndexTables:   IndexTables;

      cmViewScript:    ViewScript;
      cmExecuteScript: ExecuteScript;

      else
        ClearFlag := false;
    end;
    if (ClearFlag) then ClearEvent (Event);
  end;
end; (* HandleEvent *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.InitMenuBar;
begin
  MenuBar := PMenuBar(ResFile.Get('MAINMENU'));
end; (* InitMenuBar *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.InitStatusLine;
var
  Box: TRect;
begin
  StatusLine := PStatusLine(ResFile.Get('STATUS'));
  GetExtent(Box);
  StatusLine^.MoveTo(0, Box.B.Y - 1);
end; (* InitStatusLine *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.LoadDesktop;
var
  DesktopFile: TBufStream;
  TempDesktop: PDesktop;
  Box: TRect;
  Name: string;
begin
  Name := PickFile ('Select ADVENTURE', '~O~pen a file', 'GAME', '*.*');
  if (Name <> '') then
    begin
    DesktopFile.Init(Name, stOpenRead, 1024);
    TempDesktop := PDesktop(DesktopFile.Get);
    DesktopFile.Done;
    if ValidView(TempDesktop) <> nil then
    begin
      Desktop^.Delete(ClipboardWindow);
      Delete(Desktop);
      Dispose(Desktop, Done);
      Desktop := TempDesktop;
      Insert(Desktop);
      GetExtent(Box);
      Box.Grow(0, -1);
      Desktop^.Locate(Box);
      InsertWindow(ClipboardWindow);
    end;
  end;
end; (* LoadDesktop *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.NewWindow(W, H: integer);
var
  Box: TRect;
begin
  Box.Assign(0, 0, W, H);
  InsertWindow(New(PEditWindow, Init(Box, '', wnNoNumber)));
end; (* NewWindow *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.OpenOrderWindow;
begin
  if Message(Desktop, evBroadcast, cmFindOrderWindow, nil) = nil then
  begin
    OrderWindow := New(POrderDialog, Init);
    InsertWindow(OrderWindow);
  end
  else
    if PView(OrderWindow) <> Desktop^.TopView then OrderWindow^.Select;
  ShowOrder(CurrentOrder);
end; (* OpenOrderWindow *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.SaveDesktop;
var
  DesktopFile: TBufStream;
  Name : string;
begin
  Name := PickFile ('Enter ADVENTURE', '~O~pen a file', 'GAME', '*.*');
  if (Name <> '') then
    begin
    Desktop^.Delete(ClipboardWindow);
    DesktopFile.Init(Name, stCreate, 1024);
    DesktopFile.Put(Desktop);
    DesktopFile.Done;
    InsertWindow(ClipboardWindow);
    end;
end; (* SaveDesktop *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.SaveOrderData;
begin
  if OrderWindow^.Valid(cmClose) then
  begin
    if CurrentOrder = OrderColl^.Count then
    begin
      OrderColl^.Insert(TempOrder);
      OrderWindow^.Counter^.IncCount;
    end;
    OrderWindow^.GetData(OrderInfo);
    SaveOrders;
    EnableCommands([cmOrderPrev, cmOrderNew]);
  end;
end; (* SaveOrderData *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.ShowOrder(AOrderNum: Integer);
begin
  CurrentOrder := AOrderNum;
  OrderInfo := POrderObj(OrderColl^.At(CurrentOrder))^.TransferRecord;
  with OrderWindow^ do
  begin
    SetData(OrderInfo);
    Counter^.SetCurrent(CurrentOrder + 1);
  end;
  if CurrentOrder > 0 then EnableCommands([cmOrderPrev])
  else DisableCommands([cmOrderPrev]);
  if OrderColl^.Count > 0 then EnableCommands([cmOrderNext]);
  if CurrentOrder >= OrderColl^.Count - 1 then DisableCommands([cmOrderNext]);
  EnableCommands([cmOrderNew, cmOrderSave]);
end; (* ShowOrder *)

(* ---------------------------------------------------------------------- *)
(* Dialog-prompts for a file matching <specification>. *)
(* Returns the full path to the selected file. *)

function DM_App.PickFile (DialogTitle,
                          Prompt,
                          Path,
                          Specification : string) : string;
const
  FDOptions: Word = fdOKButton or fdOpenButton;
var
  FileDialog: PFileDialog;
  FilePath: FNameStr;
  FileName: array[0..fsFileName] of Char;
  CurrentPath : string;
begin
  GetDir (0, CurrentPath);
  ChDir (Path);
  FilePath := Specification;
  FileDialog := New(PFileDialog,
                    Init(FilePath, DialogTitle, Prompt, FDOptions, 1));
  if ExecuteDialog(FileDialog, @FilePath) <> cmCancel then
    PickFile := FilePath
  else
    PickFile := '';
  ChDir (CurrentPath);
end;  (* PickFile *)

(* ---------------------------------------------------------------------- *)
(* Dialog-prompts for a table. *)
(* Returns the full (or relative) path to the selected table. *)

function DM_App.PickTable (Relative : boolean): string;
var Path : string;
begin
  Path := PickFile ('Select TABLE', '~T~able Path', TablePath, '*.TBL');
  if (Relative) then
    Path := Strs.SubStr (Path, TablePath + '\', '.');
  PickTable := Path;
end;  (* PickTable *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)

procedure DM_App.OpenWindow;
var
  Name : string;
  W : PViewWindow;
begin
  Name := PickFile ('Select FILE', '~O~pen a file', '', '*.*');
  if (Name <> '') then
    begin
    W := New (PViewWindow, Make (nil, Name, '', '', 0, 0));
    InsertWindow (W);
    end;
end; (* OpenWindow *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.ViewTable (Name : string;
                            BoxP : PRect;
                            Help : integer;
                            Opts : word);
var
  W : PViewWindow;
begin
  if (Name = '') then Name := PickTable (false);
  if (Name <> '') then
    begin
    W := New (PViewWindow, Make (BoxP, Name, TablePath + '\',
                                 ' TABLE', Help, Opts));
    InsertWindow (W);
    end;
end; (* ViewTable *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)

procedure DM_App.RandomEntry (Name : string; BoxP : PRect);
var
  Entry: string;
begin
  if (Name = '') then Name := PickTable (true);
  if (Name <> '') then
    begin
    Randomize;
    Entry := DM_FMS.RandomEntry (Name, '');
    Entry := Strs.Compress (Entry, 0);
    ShowEntry (Name, 'RANDOM ' + Name, Entry, cmRandomEntry, BoxP);
    end;
end; (* RandomEntry *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.ReplaceRandom;
var
  Window : PEntryDialog;
  BoxP : PRect;
begin
  Window := Message (DeskTop, evBroadcast, cmFindEntryWindow, nil);
  if (Window <> nil) then
    begin
    Window^.GetBounds (BoxP^);
    RandomEntry (Window^.TableName, BoxP);
    Window^.Done;
    end;
end; (* ReplaceRandom *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.AnotherRandom;
var
  Window : PEntryDialog;
begin
  Window := Message (DeskTop, evBroadcast, cmFindEntryWindow, nil);
  if (Window <> nil) then
    RandomEntry (Window^.TableName, nil);
end; (* AnotherRandom *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)

procedure DM_App.FindEntry (Name : string; Pattern : string; BoxP : PRect);
var
  Entry: string;
begin
  if (Name = '') then Name := PickTable (true);
  if (Name <> '') then
    begin
    if (Pattern = '') then
      begin
      Pattern := Target.Pattern;  (* use global default *)
      if (InputBox ('FIND ENTRY', 'Search Pattern: ', Pattern, 80) = cmCancel) then
        Pattern := ''
      else
        Target.Pattern := Pattern;  (* save global *)
      end;

    if (Pattern <> '') then
      begin
      Entry := DM_FMS.FindEntry (Name, Pattern);
      Entry := Strs.Compress (Entry, 0);
      ShowEntry (Name, 'FIND ' + Name + ': ' + ErrorL + Pattern + ErrorR,
                 Entry, cmFindEntry, BoxP);
      end;
    end;
end; (* FindEntry *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.ReplaceFind;
var
  Window : PEntryDialog;
  BoxP : PRect;
  Pattern : string;
begin
  Window := Message (DeskTop, evBroadcast, cmFindEntryWindow, nil);
  if (Window <> nil) then
    begin
    Window^.GetBounds (BoxP^);
    Pattern := Strs.SubStr (Window^.GetTitle (80), ErrorL, ErrorR);
    FindEntry (Window^.TableName, Pattern, BoxP);
    Window^.Done;
    end;
end; (* ReplaceFind *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.AnotherFind;
var
  Window : PEntryDialog;
  Pattern : string;
begin
  Window := Message (DeskTop, evBroadcast, cmFindEntryWindow, nil);
  if (Window <> nil) then
    begin
    Pattern := Strs.SubStr (Window^.GetTitle (80), ErrorL, ErrorR);
    FindEntry (Window^.TableName, Pattern, nil);
    end;
end; (* AnotherFind *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.ScanTables (Pattern : string);

var MatchF   : text;
    FileName : string;

begin
  if (Pattern = '') then
    begin
    Pattern := Target.Pattern;  (* use global default *)
    if (InputBox ('SEARCH ALL TABLES', 'Pattern: ', Pattern, 80) = cmCancel) then
      Pattern := '';
    end;

  if (Pattern <> '') then
    begin
    Target.Pattern := Pattern;  (* save global *)
    FileName := TablePath + '\' + TempMark +
                Upper (copy (Pattern, 1, 6)) + '.TBL';
    assign (MatchF, FileName); rewrite (MatchF);
    writeln (MatchF, HeaderMark + ' TABLE ENTRIES MATCHING: ' + Pattern);
    Search_Directory (DM_FMS.TablePath + '\', Upper (Pattern), MatchF);
    writeln (MatchF);
    close (MatchF);
    ViewTable (FileName, nil, 0, vwDeleteFile);
    end;
end;  (* ScanTables *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.IndexTables;
begin
  DM_FMS.Index_Files (true);
end; (* IndexTables *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.ExpandMacros (Name : string; BoxP : PRect);
var
  RealPath   : string;
  TempPath   : string;
  Tbl        : PTable;
  TableName  : string;
  DstF       : text;
begin
  if (Name = '') then Name := PickTable (true);
  if (Name <> '') then
    begin
    {Tbl := FileFromName (Name);}
    Tbl := TableOfName (Name);
    if (Tbl <> nil) then
      begin
      Randomize;
      TableName := Name;
      if (pos (TablePath + '\', TableName) > 0) then
        TableName := Strs.SubStr (TableName, TablePath + '\', '.');
      RealPath := TablePath + '\' + TableName + '.TBL';
      TempPath := TablePath + '\' + TableName + '.T00';
      assign (DstF, TempPath); rewrite (DstF);
      Resolve_Table (DstF, TableName, RealPath, Tbl^.Entries);
      close (DstF);
      ViewTable (TempPath, BoxP, cmExpandMacros, vwDeleteFile);
    end;  (* if Tbl *)
  end;
end;  (* ExpandMacros *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.ReplaceExpanded;
var
  Window : PViewWindow;
  BoxP : PRect;
begin
  Window := Message (DeskTop, evBroadcast, cmFindViewWindow, nil);
  if (Window <> nil) then
    begin
    Window^.GetBounds (BoxP^);
    ExpandMacros (Window^.FileName, BoxP);
    Window^.Done;
    end;
end; (* ReplaceExpanded *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.AnotherExpanded;
var
  Window : PViewWindow;
begin
  Window := Message (DeskTop, evBroadcast, cmFindViewWindow, nil);
  if (Window <> nil) then
    ExpandMacros (Window^.FileName, nil);
end; (* AnotherExpanded *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)

function DM_App.PickScript : string;
begin
  PickScript :=
    PickFile ('Select SCRIPT', '~S~cript Path', CommandPath, '*.CMD');
end;  (* PickScript *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.ViewScript;
var
  Name : string;
  W : PViewWindow;
begin
  Name := PickScript;
  if (Name <> '') then
    begin
    W := New(PViewWindow, Make(nil, Name, CommandPath, ' SCRIPT', 0, 0));
    InsertWindow(W);
    end;
end; (* ViewScript *)

(* ----------------------------------------------------------------------- *)

procedure DM_App.ExecuteScript;
var
  Name       : string;
  TempPath   : string;
  DstF       : text;
begin
  Name := PickScript;
  if (Name <> '') then
    begin
    TempPath := Strs.XchText1 (Name, '.CMD', '.T00');
    assign (DstF, TempPath); rewrite (DstF);
    Resolve_Script (DstF, Name);
    close (DstF);
    ViewTable (TempPath, nil, cmExpandMacros, vwDeleteFile);
  end;
end; (* ExecuteScript *)

(* ----------------------------------------------------------------------- *)

var DMApp: DM_App;

begin
  DMApp.Init;
  DMApp.Run;
  DMApp.Done;
end.

{ BUGS:

- Temporary tables are deleted when closed, but can be saved to desktop.
- Find bug in Turbo Vision edit buffers

}
{ NOTES FOR FUTURE EXPANSION:

- Don't abort desk load when file not found
- store relative file path (not full) in desktop
- Allow "expand macros" from View Window
- provide context keys for scan/filter/etc
- override editor to show colors (TFileEditor.LoadFile)
- override the double-click to support word-selection
- prevent user from going "up" from the given path
- force certain suffix use
- make entry window non-scrollable
- suppot multiple desktops (1 per game?)

}
