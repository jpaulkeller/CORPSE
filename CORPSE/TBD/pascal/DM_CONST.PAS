
unit DM_Const; { Contains global constants for CORPSE DM }

(* Command ranges:     0 ..     99 (reserved by Borland)
                     100 ..    255 (can be disabled)
                     256 ..    999 (reserved by Borland)
                    1000 .. 65,535 (cannot be disabled) *)

(*
Turbo Vision reserves help context values 0 through 999 for its own use.
Help contexts in the range $FF00..$FFFF are reserved by Borland.

 hcNoContext � 0     � No context specified
 hcDragging  � 1     � Object is being dragged

 Constant    � Value � Meaning
 �����������������������������
 hcNew       � $FF01 � File|New
 hcOpen      � $FF02 � File|Open
 hcSave      � $FF03 � File|Save
 hcSaveAs    � $FF04 � File|Save As
 hcSaveAll   � $FF05 � File|Save All
 hcChangeDir � $FF06 � File|Change Dir
 hcDosShell  � $FF07 � File|DOS Shell
 hcExit      � $FF08 � File|Exit


 Constant � Value � Meaning
 ��������������������������
 hcUndo   � $FF10 � Edit|Undo
 hcCut    � $FF11 � Edit|Cut
 hcCopy   � $FF12 � Edit|Copy
 hcPaste  � $FF13 � Edit|Paste
 hcClear  � $FF14 � Edit|Clear


 Constant   � Value � Meaning
 ����������������������������
 hcTile     � $FF20 � Window|Tile
 hcCascade  � $FF21 � Window|Cascade
 hcCloseAll � $FF22 � Window|Close All
 hcResize   � $FF23 � Window|Resize
 hcZoom     � $FF24 � Window|Zoom
 hcNext     � $FF25 � Window|Next
 hcPrev     � $FF26 � Window|Prev
 hcClose    � $FF27 � Window|Close
*)

interface

const
  cmOrderNew        =  251;
  cmOrderWin        =  252;
  cmOrderSave       =  253;
  cmOrderCancel     =  254;
  cmOrderNext       =  255;
  cmOrderPrev       =  250;
  cmClipShow        =  260;
  cmAbout           =  270;
  cmFindOrderWindow = 2000;

  cmOptionsVideo = 1502;
  cmOptionsSave  = 1503;
  cmOptionsLoad  = 1504;

  cmStockNew    = 241;
  cmStockWin    = 242;
  cmStockSave   = 243;
  cmStockCancel = 244;
  cmStockNext   = 245;
  cmStockPrev   = 240;

  cmSupplierNew    = 231;
  cmSupplierWin    = 232;
  cmSupplierSave   = 233;
  cmSupplierCancel = 234;
  cmSupplierNext   = 235;
  cmSupplierPrev   = 230;

  cmFindEntryWindow = 1000;
  cmFindViewWindow  = 1001;
  cmFindMilestone   = 1002;

  {
    Stream Registration Keys
    ------------------------
    stViewWindow  = 1001
    stEntryDialog = 1002
    stHintStatus  = 1003
  }

(* ----------------------------------------------------------------------- *)

cmTABLES          = 1200;
  cmViewTable     = 1210;
  cmCreateTable   = 1220;
    cmSubCriteria = 1221;
    cmSubQuery    = 1222;
    cmCreateNew   = 1223;
    cmAppendNew   = 1224;

  cmFindEntry     = 1230;  (* search table *)
  cmAnotherFind   = 1231;
  cmReplaceFind   = 1232;

  cmRandomEntry   = 1240;
  cmAnotherRandom = 1241;
  cmReplaceRandom = 1242;

  cmExpandMacros    = 1280;  (* resolve / test / validate table *)
  cmAnotherExpanded = 1281;
  cmReplaceExpanded = 1282;

  cmScanTables    = 1250;
  cmRenameTable   = 1260;
  cmDeleteTable   = 1270;
  cmIndexTables   = 1290;

cmSCRIPTS         = 1300;
  cmExecuteScript = 1310;
  cmCreateScript  = 1320;
  cmViewScript    = 1330;
  cmDeleteScript  = 1340;

cmFORMS           = 1400;
  cmChar_Gen      = 1410;
  cmNPC_Gen       = 1420;
  cmEncounters    = 1430;
  cmTreasures     = 1440;
  cmSpells        = 1450;
  cmWeather       = 1460;

cmGAME            = 1500;
  cmChanceTo      = 1510;
    cmAttack      = 1511;
    cmCast        = 1512;
    cmNormal      = 1513;
    cmSpecialCT   = 1514;
  cmRollDice      = 1520;
    cmd4          = 1521;
    cmd6          = 1522;
    cmd8          = 1523;
    cmd10         = 1524;
    cmd12         = 1525;
    cmd20         = 1526;
    cmd100        = 1527;
    cmRollAttr    = 1528;
    cmRollChar    = 1529;
    cmRollSpecial = 1520;
  cmCombatMatrix  = 1530;

cmMAPS            = 1100;
  cmCreateMap     = 1110;
    cmWilderness  = 1111;
    cmVillage     = 1112;
    cmCity        = 1113;
    cmFortress    = 1114;
    cmDungeon     = 1115;
    cmSpecial     = 1116;
  cmAccessMap     = 1120;
  cmDeleteMap     = 1130;

cmENVIRONMENT     = 1600;
  cmQuit          = 1610;
  cmLooks         = 1620;
  cmZoom          = 1630;
  cmHelp          = 1640;

implementation

end.
