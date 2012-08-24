
program DM_Menus;

uses
  App,
  Dialogs,
  Drivers,
  Editors,
  Menus,
  Objects,
  Validate,
  Views,

  DM_Const,
  DM_Hint;

var
  R: TRect;
  ResFile: TResourceFile;
  MainMenu: PMenuBar;
  StatLine: PHintStatusLine;
  OrderWindow, AboutBox: PDialog;
  Field: PInputLine;
  Cluster: PCluster;
  Memo: PMemo;

(* ---------------------------------------------------------------------- *)

begin
  RegisterViews;
  RegisterDialogs;
  RegisterMenus;
  RegisterValidate;
  RegisterEditors;
  Register_HintStatusLine;

  ResFile.Init(New(PBufStream, Init('CORPSE.TVR', stCreate, 2048)));

  R.Assign(0, 0, 80, 1);
  New(MainMenu, Init(R, NewMenu(
    NewSubMenu('~F~ile', hcNoContext, NewMenu(
      StdFileMenuItems(nil)),
    NewSubMenu('~E~dit', hcNoContext, NewMenu(
      StdEditMenuItems(
      NewLine(
      NewItem('~S~how clipboard', '', kbNoKey, cmClipShow, hcNoContext,
      nil)))),
    NewSubMenu('~T~ABLES', hcNoContext, NewMenu(
      NewItem('~V~iew Table',   '', kbNoKey, cmViewTable,    hcNoContext,
      NewItem('~F~ind Entry',   '', kbNoKey, cmFindEntry,    cmFindEntry,
      NewItem('~R~andom Entry', '', kbNoKey, cmRandomEntry,  cmRandomEntry,
      NewItem('~S~can Tables',  '', kbNoKey, cmScanTables,   hcNoContext,
      NewItem('~E~xpand Macros','', kbNoKey, cmExpandMacros, cmExpandMacros,
      NewItem('~I~ndex Tables', '', kbNoKey, cmIndexTables,  hcNoContext,
      nil))))))),
    NewSubMenu('~S~CRIPTS', hcNoContext, NewMenu(
      NewItem('~V~iew',         '', kbNoKey, cmViewScript,   hcNoContext,
      NewItem('~E~xecute',      '', kbNoKey, cmExecuteScript,hcNoContext,
      nil))),
    NewSubMenu('~O~rders', hcNoContext, NewMenu(
      NewItem('~N~ew', 'F9', kbF9,    cmOrderNew,  hcNoContext,
      NewItem('~S~ave', '',  kbNoKey, cmOrderSave, hcNoContext,
      NewLine(
      NewItem('Next', 'PgDn', kbPgDn, cmOrderNext, hcNoContext,
      NewItem('Prev', 'PgUp', kbPgUp, cmOrderPrev, hcNoContext,
      nil)))))),
    NewSubMenu('Op~t~ions', hcNoContext, NewMenu(
      NewItem('~T~oggle video mode', '', kbNoKey, cmOptionsVideo, hcNoContext,
      NewItem('~S~ave desktop...', '',   kbNoKey, cmOptionsSave,  hcNoContext,
      NewItem('~L~oad desktop...', '',   kbNoKey, cmOptionsLoad,  hcNoContext,
      nil)))),
    NewSubMenu('~W~indow', hcNoContext, NewMenu(
      NewItem('~O~rders', '',      kbNoKey, cmOrderWin,    hcNoContext,
      NewItem('S~u~ppliers', '',   kbNoKey, cmSupplierWin, hcNoContext,
      NewItem('Stoc~k~ items', '', kbNoKey, cmStockWin,    hcNoContext,
      NewLine(
      StdWindowMenuItems(nil)))))),
    NewSubMenu('~H~elp', hcNoContext, NewMenu(
      NewItem('~A~bout', '', kbNoKey, cmAbout, hcNoContext,
      nil)),
    nil)))))))))));
  ResFile.Put(MainMenu, 'MAINMENU');

(* ---------------------------------------------------------------------- *)

  R.Assign(0, 24, 80, 25);
  New(StatLine, Init(R,
    NewStatusDef(0, 0,
      NewStatusKey('~F3~ Open', kbF3, cmOpen,
      NewStatusKey('~F4~ New',  kbF4, cmNew,
      NewStatusKey('~Alt+F3~ Close', kbAltF3, cmClose,
      StdStatusKeys(nil)))),
    NewStatusDef(cmFindEntry, cmFindEntry,
      NewStatusKey('~F2~ Another Match', kbF2, cmAnotherFind,
      NewStatusKey('~F3~ Overwrite',     kbF3, cmReplaceFind,
      StdStatusKeys(nil))),
    NewStatusDef(cmRandomEntry, cmRandomEntry,
      NewStatusKey('~F2~ Another Random', kbF2, cmAnotherRandom,
      NewStatusKey('~F3~ Overwrite',      kbF3, cmReplaceRandom,
      StdStatusKeys(nil))),
    NewStatusDef(cmExpandMacros, cmExpandMacros,
      NewStatusKey('~F2~ Another Expanded', kbF2, cmAnotherExpanded,
      NewStatusKey('~F3~ Overwrite',        kbF3, cmReplaceExpanded,
      StdStatusKeys(nil))),
    NewStatusDef($FFF9, $FFF9,
      NewStatusKey('~PgUp~ Prev', kbPgUp, cmSupplierPrev,
      NewStatusKey('~PgDn~ Next', KbPgDn, cmSupplierNext,
      nil)),
    NewStatusDef($FFFA, $FFFB,
      NewStatusKey('~PgUp~ Prev', kbPgUp, cmStockPrev,
      NewStatusKey('~PgDn~ Next', KbPgDn, cmStockNext,
      nil)),
    NewStatusDef($FFFC, $FFFD,
      NewStatusKey('~PgUp~ Prev', kbPgUp, cmOrderPrev,
      NewStatusKey('~PgDn~ Next', kbPgDn, cmOrderNext,
      StdStatusKeys(nil))),
    NewStatusDef($FFFE, $FFFF,
      StdStatusKeys(nil), nil))))))))));
  ResFile.Put(StatLine, 'STATUS');

(* ---------------------------------------------------------------------- *)

  R.Assign(0, 0, 34, 11);
  AboutBox := New(PDialog, Init(R, 'About CORPSE'));
  with AboutBox^ do
  begin
    Options := Options or ofCentered;
    R.Assign(2, 2, 32, 7);
    Insert(New(PStaticText,
      Init(R, #3'Computer Oriented Role Playing'#13 +
              #3'System and Environment'#13 + #13 +
              #3'Copyright 1993'#13 +
              #3'Istari Corporation')));
    R.Assign(12, 8, 21, 10);
    Insert(New(PButton, Init(R, '~C~lose', cmOk, bfDefault)));
  end;
  ResFile.Put(AboutBox, 'ABOUTBOX');

(* ---------------------------------------------------------------------- *)

  R.Assign(0, 0, 60, 17);
  OrderWindow := New(PDialog, Init(R, 'Orders'));
  with OrderWindow^ do
  begin
    Options := Options or ofCentered;
    HelpCtx := $F000;

    R.Assign(13, 2, 23, 3);
    Field := New(PInputLine, Init(R, 8));
    Field^.SetValidator(New(PRangeValidator, Init(1, 32767)));
    Insert(Field);
    R.Assign(2, 2, 12, 3);
    Insert(New(PLabel, Init(R, '~O~rder #:', Field)));

    R.Assign(13, 4, 23, 5);
    Field := New(PInputLine, Init(R, 8));
    Field^.SetValidator(New(PPXPictureValidator, Init('&&&-####', True)));
    Insert(Field);
    R.Assign(2, 4, 12, 5);
    Insert(New(PLabel, Init(R, '~S~tock #:', Field)));

    R.Assign(43, 2, 53, 3);
    Field := New(PInputLine, Init(R, 8));
    Field^.SetValidator(New(PPXPictureValidator, Init('{#[#]}/{#[#]}/{##[##]}', True)));
    Insert(Field);
    R.Assign(26, 2, 41, 3);
    Insert(New(PLabel, Init(R, '~D~ate of order:', Field)));

    R.Assign(46, 4, 53, 5);
    Field := New(PInputLine, Init(R, 5));
    Field^.SetValidator(New(PRangeValidator, Init(1, 32767)));
    Insert(Field);
    R.Assign(26, 4, 44, 5);
    Insert(New(PLabel, Init(R, '~Q~uantity ordered:', Field)));

    R.Assign(3, 7, 57, 8);
    Cluster := New(PRadioButtons, Init(R,
      NewSItem('Cash   ',
      NewSItem('Check  ',
      NewSItem('P.O.   ',
      NewSItem('Account', nil))))));
    Insert(Cluster);
    R.Assign(2, 6, 21, 7);
    Insert(New(PLabel, Init(R, '~P~ayment method:', Cluster)));

    R.Assign(22, 8, 37, 9);
    Cluster := New(PCheckBoxes, Init(R, NewSItem('~R~eceived', nil)));
    Insert(Cluster);

    R.Assign(3, 10, 57, 13);
    Memo := New(PMemo, Init(R, nil, nil, nil, 255));
    Insert(Memo);
    R.Assign(2, 9, 9, 10);
    Insert(New(PLabel, Init(R, 'Notes:', Memo)));

    R.Assign(2, 14, 12, 16);
    Insert(New(PButton, Init(R, '~N~ew', cmOrderNew, bfNormal)));
    R.Assign(17, 14, 27, 16);
    Insert(New(PButton, Init(R, '~S~ave', cmOrderSave, bfDefault)));
    R.Assign(32, 14, 42, 16);
    Insert(New(PButton, Init(R, '~C~ancel', cmOrderCancel, bfNormal)));
    R.Assign(45, 14, 55, 16);
    Insert(New(PButton, Init(R, 'Next', cmOrderNext, bfNormal)));
    SelectNext(False);
  end;
  ResFile.Put(OrderWindow, 'ORDERS');

  ResFile.Done;
end.
