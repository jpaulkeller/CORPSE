{$I Compiler.inc}
{$O-}  (* prevent unit from being overlayed    *)

{$DEFINE EnablePickOrientations}  (* allow alternate orientations *)

(* ---------------------------------------------------------------------- *)
(* File: JMenu.Pas *)
(* ---------------------------------------------------------------------- *)

(* WARNING: TpMouse.InitializeMouse should be called prior to Choice()    *)

UNIT JMenu;

INTERFACE

USES TpPick;

type
  Values   = word;

  Items    = ^ItemRecs;
  ItemRecs = record
               Lbl : string[25];  (* item label *)
               Val : Values;      (* item value *)
               Prv : Items;       (* prev item  *)
               Nxt : Items;       (* next item  *)
             end;

  MenuPtrs = ^MenuRecs;
  MenuRecs = record
               Prompt    : string[80];   (* prompt / menu window title    *)
               Item      : Items;        (* pointer to the current item   *)
               FirstItem : Items;        (* pointer to head of item list  *)
               ItemQty   : integer;      (* current # of items in menu    *)
               Sort      : boolean;      (* true to auto-sort added items *)
               ItemWidth : integer;      (* screen-specific maximum width *)
               Colors    : TpPick.PickColorArray;
               HelpFn    : Pointer;      (* Pointer to FAR help function  *)
             end;

(* ---------------------------------------------------------------------- *)
(* The following functions should be used as examples of proper usage:    *)
(* ---------------------------------------------------------------------- *)

 function  FileChoice     (    FileSpecs   : string  ): string;
 function  Quick          (    Prompt      : string;
                           VAR ItemString  : string  ): integer;

(* ---------------------------------------------------------------------- *)

 function  Create         (    Prompt      : string;
                               HelpFn      : pointer;
                               SortFlag    : boolean ): MenuPtrs;
 procedure AddItem        (VAR Menu        : MenuPtrs;
                               ItemLabel   : string;
                               Value       : Values  );
 procedure Finalize       (VAR Menu        : MenuPtrs);
 function  Choice         (var Menu        : MenuPtrs;
                               DefaultItem : word    ): Values;
 function  Choices        (var Menu        : MenuPtrs;
                               DefaultItem : word    ): Values;

(* ---------------------------------------------------------------------- *)

 function  ItemFromPick   (    WhichItem   : word    ): Items;
 function  LabelFromValue (    Value       : Values  ): string;
 function  LabelFromPick  (    WhichItem   : word    ): string;

(* ====================================================================== *)

IMPLEMENTATION

USES DOS,                                        (* Turbo Pascal provided *)
     TpCRT,                                      (* Turbo Power units     *)
     TpWindow,
     Env, Strs;

const

 (*  Color            Background    Foreground *)
 (* -------          ------------   ---------- *)
    Frame   : byte = (16 * Black) + LightBlue;
    Title   : byte = (16 * Black) + White;
    ItemOn  : byte = (16 * Cyan ) + Black;
    ItemOff : byte = (16 * Black) + Green;

var
  NullItem    : Items;
  CurrentMenu : MenuPtrs; (* global variable of the currently active menu *)

(* ---------------------------------------------------------------------- *)
(* function ItemFromPos (Menu : MenuPtrs; WhichItem : integer) : Items;   *)
(* function ItemFromVal (Menu : MenuPtrs; Value     : Values ) : Items;   *)
(* ---------------------------------------------------------------------- *)

  (* function HeapFunc (Size : word) : integer;
  (* begin
  (*   HeapFunc := 1;  (* causes NEW to return NIL instead of breaking *)
  (* end;  (* HeapFunc from TP Ref p200 *)

(* ---------------------------------------------------------------------- *)

procedure SetColors (VAR Colors : TpPick.PickColorArray;
                         Frame, Title, ItemOn, ItemOff : byte);
  begin
    Colors [ WindowAttr ] := ItemOff;   (* unselected item *)
    Colors [ FrameAttr  ] := Frame;     (* window frame    *)
    Colors [ HeaderAttr ] := Title;     (* window title    *)
    Colors [ SelectAttr ] := ItemOn;    (* selected item   *)
    Colors [ AltNormal  ] := ItemOff;   (* alt. unselected *)
    Colors [ AltHigh    ] := ItemOn;    (* alt. selected   *)
  end;  (* SetColors *)

(* ---------------------------------------------------------------------- *)

function Create (Prompt   : string;
                 HelpFn   : Pointer;
                 SortFlag : boolean) : MenuPtrs;

  var Menu    : MenuPtrs;
      Colors  : TpPick.PickColorArray;

  begin
    new (Menu);  (* should be Disposed/Released after use *)
    Menu^.Prompt    := Prompt;
    Menu^.FirstItem := nil;
    Menu^.Item      := nil;   (* default *)
    Menu^.ItemQty   := 0;
    Menu^.Sort      := SortFlag;
    Menu^.HelpFn    := HelpFn;
    SetColors (Menu^.Colors, Frame, Title, ItemOn, ItemOff); (* default *)
    Create := Menu;
  end;  (* function Create *)

(* ---------------------------------------------------------------------- *)

procedure AddItem (VAR Menu      : MenuPtrs;
                       ItemLabel : string;
                       Value     : Values);
  var LastItem,
      NewItem   : Items;
      NewHeader : boolean;

  begin
    if (Value = 0) then
      ENV.Warning ('Invalid value(0) for menu item: ' + ItemLabel);

    new (NewItem);
    if  (NewItem = NIL) then
      begin
        writeln ('Unable to allocate space for item: ' + ItemLabel);
        Exit;
      end;

    NewItem^.Lbl := ItemLabel;
    NewItem^.Val := Value;

    if (Menu^.ItemQty = 0) then  (* start item list *)
      begin
        Menu^.FirstItem := NewItem;
        NewItem^.Nxt    := NewItem;
        NewItem^.Prv    := NewItem;
        Menu^.ItemQty   := 1;
      end
    else
      begin
        Menu^.ItemQty := Menu^.ItemQty + 1;  (* increment quantity *)
        LastItem := Menu^.FirstItem^.Prv;    (* start at last item *)

        if Menu^.Sort then (* insert new item sorted by label *)
          begin
            NewHeader := (ItemLabel < LastItem^.Nxt^.Lbl);
            if not NewHeader then                      (* search for pos *)
              while (ItemLabel < LastItem^.Lbl) and    (* not sorted yet *)
                    (LastItem <> Menu^.FirstItem) do   (* not tried all  *)
                LastItem := LastItem^.Prv;             (* try prev item  *)
          end;  (* if InSorted *)

        NewItem^.Prv      := LastItem;       (* from new to last *)
        NewItem^.Nxt      := LastItem^.Nxt;  (* from new to next *)
        LastItem^.Nxt     := NewItem;        (* from last to new *)
        NewItem^.Nxt^.Prv := NewItem;        (* from next to new *)

        if Menu^.Sort and NewHeader then
          Menu^.FirstItem := Menu^.FirstItem^.Prv;

      end;  (* else add to list of current items*)

  end;  (* procedure AddItem *)

(* ---------------------------------------------------------------------- *)

procedure Finalize (VAR Menu : MenuPtrs);

  var Item : Items;
      i    : integer;

  begin
    Item := Menu^.FirstItem;
    Menu^.ItemWidth := length (Item^.Lbl);
    for i := 2 to Menu^.ItemQty do  (* find widest item *)
      begin
        Item := Item^.Nxt;
        if (length (Item^.Lbl) > Menu^.ItemWidth) then
          Menu^.ItemWidth := length (Item^.Lbl);
      end;  (* for each item *)
  end;  (* Finalize *)

(* ---------------------------------------------------------------------- *)

function ItemFromPos (Menu : MenuPtrs; WhichItem : integer) : Items; (* F+ *)
  var Item : Items;
      i    : integer;
  begin
    if (WhichItem > 0) and (WhichItem <= Menu^.ItemQty) then
      begin
        Item := Menu^.FirstItem;
        for i := 2 to WhichItem do
          Item := Item^.Nxt;
        ItemFromPos := Item;
      end
    else ItemFromPos := NullItem;
  end;  (* function ItemFromPos *)

(* ---------------------------------------------------------------------- *)

function ItemFromVal (Menu : MenuPtrs; Value : Values) : Items;
  var Item : Items;
  begin
    Item := Menu^.FirstItem;
    while (Item^.Val <> Value) and            (* not found yet *)
          (Item <> Menu^.FirstItem^.Prv) do   (* not tried all *)
      Item := Item^.Nxt;
    if (Item^.Val = Value) then ItemFromVal := Item
    else ItemFromVal := NullItem;
  end;  (* function ItemFromVal *)

(* ---------------------------------------------------------------------- *)

function LabelFromMenuValue (Menu : MenuPtrs; Value : Values) : string;
  begin
    LabelFromMenuValue := ItemFromVal (Menu, Value)^.Lbl;
  end;  (* function LabelFromMenuValue *)

(* ---------------------------------------------------------------------- *)

function LabelFromValue (Value : Values) : string;
  begin
    LabelFromValue := LabelFromMenuValue (CurrentMenu, Value);
  end;  (* function LabelFromValue *)

(* ---------------------------------------------------------------------- *)

function ItemFromPick (WhichItem : word) : Items; (* must be F+ *)
  begin
    ItemFromPick := ItemFromPos (CurrentMenu, WhichItem);
  end;  (* ItemFromPick *)

(* ---------------------------------------------------------------------- *)

function ValueFromPick (WhichItem : word) : Values;
  begin
    ValueFromPick := ItemFromPick (WhichItem)^.Val;
(* Env.Log ('1>> ' + StrF (ItemFromPick (WhichItem)^.Val) + ' (' + StrF (WhichItem) + ')'); Env.LogOut; *)
  end;  (* ValueFromPick *)

(* ---------------------------------------------------------------------- *)

function LabelFromPick (WhichItem : word) : string; (* must be F+ *)
  (* routine required by TpPick unit *)
  begin
    LabelFromPick := ItemFromPick (WhichItem)^.Lbl;
  end;  (* LabelFromPick *)

(* ---------------------------------------------------------------------- *)

function Choice (var Menu : MenuPtrs; DefaultItem : word) : Values;

  var TopX, TopY,
      BtmX, BtmY      : integer;
      MinColumns      : integer;  (* minimum # of columns reqd for 1 page *)
      MaxItemsPerPage : integer;  (* max # of items which fit on 1 page   *)

  begin
    TopX := 1;
    TopY := 2;
    BtmX := ENV.WindowWidth;
    BtmY := ENV.WindowHeight - 2;

    MinColumns      := (Menu^.ItemQty DIV (BtmY - TopY - 1)) + 1;
    MaxItemsPerPage := (BtmX - TopX - 1) DIV (Menu^.ItemWidth + 1);

    (* set up TpPick environment *)
    TpPick.PickSrch := StringAltSrch;         (* incremental w/ auto-next *)
    TpPick.SetSnakingPick;
    if (MinColumns <= MaxItemsPerPage) then           (* set # of columns *)
         TpPick.PickMatrix := MinColumns
    else TpPick.PickMatrix := MaxItemsPerPage;

    TpPick.PickHelpPtr := Menu^.HelpFn;  (* may need to save old ptr ? *)

    if TpMouse.MouseInstalled then  (* enable Mouse *)
      begin
        TpPick.PickMouseScroll := true;
        EnablePickMouse;
      end;

    (* shrink to fit: squeeze columns; TpPick handles squeezing rows *)
    MinColumns := PickMatrix * (Menu^.ItemWidth + 3);
    while ((BtmX - TopX - 1) > MinColumns) do
      begin
        TopX := TopX + 1;
        BtmX := BtmX - 1;
      end;

    CurrentMenu := Menu;  (* for global use of LabelFromPick routine *)

    if not PickWindow (@LabelFromPick,    (* routine to return label *)
                       Menu^.ItemQty,     (* number of items in menu *)
                       TopX, TopY,        (* top left corner of menu *)
                       BtmX, BtmY,        (* bottom right corner     *)
                       true,              (* draw frame around menu  *)
                       Menu^.Colors,      (* TpPick color array      *)
                       Menu^.Prompt,      (* title of the menu       *)
                       DefaultItem) then  (* VAR chosen item         *)
      begin
        ENV.Warning ('Insufficient Memory for menu: ' + Menu^.Prompt);
        Choice := 0;
      end
    else if (TpPick.PickCmdNum = PKSSelect) then
      Choice := ValueFromPick (DefaultItem)
    else Choice := 0;  (* user ESCaped the menu *)

  end;  (* Choice *)

(* ---------------------------------------------------------------------- *)

function Choices (var Menu : MenuPtrs; DefaultItem : word) : Values;

  var W               : WindowPtr;
      TopX, TopY,
      BtmX, BtmY      : integer;
      MinColumns      : integer;  (* minimum # of columns reqd for 1 page *)
      MaxItemsPerPage : integer;  (* max # of items which fit on 1 page   *)
      OneChoice       : word;
      Row             : word;

  begin
    TopX := 1;
    TopY := 2;
    BtmX := ENV.WindowWidth;
    BtmY := ENV.WindowHeight - 2;

    MinColumns      := (Menu^.ItemQty DIV (BtmY - TopY - 1)) + 1;
    MaxItemsPerPage := (BtmX - TopX - 1) DIV (Menu^.ItemWidth + 1);

    (* set up TpPick environment *)
    TpPick.PickSrch := StringAltSrch;         (* incremental w/ auto-next *)
    TpPick.SetSnakingPick;
    if (MinColumns <= MaxItemsPerPage) then           (* set # of columns *)
         TpPick.PickMatrix := MinColumns
    else TpPick.PickMatrix := MaxItemsPerPage;

    TpPick.PickHelpPtr := Menu^.HelpFn;  (* may need to save old ptr ? *)

    if TpMouse.MouseInstalled then  (* enable Mouse *)
      begin
        TpPick.PickMouseScroll := true;
        EnablePickMouse;
      end;

    (* shrink to fit: squeeze columns; TpPick handles squeezing rows *)
    MinColumns := PickMatrix * (Menu^.ItemWidth + 3);
    while ((BtmX - TopX - 1) > MinColumns) do
      begin
        TopX := TopX + 1;
        BtmX := BtmX - 1;
      end;

    CurrentMenu := Menu;  (* for global use of LabelFromPick routine *)

    if not MakeWindow (W,
                       TopX, TopY,     (* top left corner of menu *)
                       BtmX, BtmY,     (* bottom right corner     *)
                       true,           (* draw frame around menu  *)
                       false,          (* do not clear the window *)
                       false,          (* do not save the window  *)
                       Menu^.Colors [WindowAttr],
                       Menu^.Colors [FrameAttr],
                       Menu^.Colors [HeaderAttr],
                       Menu^.Prompt) then (* title of menu/window *)
      begin
      Env.Warning ('Menu.Choices unable to MakeWindow');
      exit;
      end;

    Row := 1;
    FillPickWindow (W, 
                    @LabelFromPick,    (* routine to return label *)
                    Menu^.ItemQty,     (* number of items in menu *)
                    Menu^.Colors,      (* TpPick color array      *)
                    DefaultItem,       (* VAR chosen item         *)
                    Row);              (* internal for PickBar    *)

    (* get user choices until <Esc> is pressed *)
    repeat
      TpPick.PickBar (W, @LabelFromPick, Menu^.ItemQty,
                      Menu^.Colors, false, DefaultItem, Row);
      if (PickCmdNum = PKSSelect) then  (* a selection was made *)
        begin
        ENV.Warning ('>>>> selection: ' + LabelFromPick (OneChoice));
        OneChoice := ValueFromPick (DefaultItem);
        end
      else OneChoice := 0;  (* user ESCaped the menu *)
    until (PickCmdNum <> PKSSelect);

    Choices := OneChoice;

  end;  (* Choices *)

(* ---------------------------------------------------------------------- *)

function FileChoice (FileSpecs : string) : string;

  (* Prompts the user with a menu of files matching any of the given  *)
  (* FileSpecifications.  Returns the selected filename or '' if none *)
  (* matched or if user ESCaped                                       *)

  const NoMoreFiles = 18;

  var Menu     : MenuPtrs;
      SrchRec  : SearchRec;  (* DOS type *)
      Value    : Values;
      HeapPtr  : pointer;
      Name     : string;
      FileSpec : string;

  begin
    Value := 0;
    Mark (HeapPtr);

    Menu := Create ('Select File: ', nil, true);

    while (FileSpecs <> '') do
      begin
        FileSpec := Strs.PopWord (FileSpecs, ' ');
        DOS.FindFirst (FileSpec, 0, SrchRec);
        while (DosError <> NoMoreFiles) do
          begin
            Inc (Value);
            AddItem (Menu, SrchRec.Name, Value);
            DOS.FindNext (SrchRec);
          end;  (* while more matching files *)
      end;  (* while more file specifications *)

    if (Menu^.ItemQty > 0) then  (* some file(s) matched spec *)
      begin
        Finalize (Menu);
        Value := Choice (Menu, 1);
      end;

    if (Value <= 0) then Name := '' (* 0 = no files or <Abort> *)
    else Name := LabelFromMenuValue (Menu, Value);

    Release (HeapPtr);
    FileChoice := Name;

  end;  (* function FileChoice *)

(* ---------------------------------------------------------------------- *)

function Quick (    Prompt     : string;
                VAR ItemString : string) : integer;

  (* Parses the given ItemString, and prompts the user to select one   *)
  (* of the words in the string.  Returns an integer which corresponds *)
  (* to the Nth word in the ItemString (as defined by PopWord)         *)
  (* The word selected is also returned in the ItemString.             *)
  (* The vert. bar "|" is used as the word separator to allow phrases. *)

  var Menu    : MenuPtrs;
      HeapPtr : pointer;
      Value   : Values;

  begin
    Value := 0;
    Mark (HeapPtr);

    Menu := Create (Prompt, nil, false);
    while (ItemString <> '') do
      begin
        Prompt := Strs.PopWord (ItemString, '|');
        Value  := Value + 1;
        AddItem (Menu, Prompt, Value);
      end;  (* while more items *)

    if (Menu^.ItemQty > 0) then
      begin
        Finalize (Menu);
        Value := Choice (Menu, 1);
      end;

    if (Value <= 0) then ItemString := ''
    else ItemString := LabelFromMenuValue (Menu, Value);
    Release (HeapPtr);
    Quick := Value;
  end;  (* function Quick *)

(* ---------------------------------------------------------------------- *)

BEGIN

  (* HeapError := @HeapFunc;  (* see Turbo Pascal Reference page 200 *)

  NullItem^.Lbl := '';
  NullItem^.Val := 0;
  NullItem^.Prv := nil;
  NullItem^.Nxt := nil;

END.  (* unit JMenu *)

