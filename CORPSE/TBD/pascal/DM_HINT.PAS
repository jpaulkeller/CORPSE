
UNIT DM_Hint;

INTERFACE

USES Menus, Objects;

type
  PHintStatusLine = ^THintStatusLine;
  THintStatusLine = object(TStatusLine)
    constructor Load  (var S       : TStream);
    procedure   Store (var S       : TStream);
    function    Hint  (    AHelpCtx: Word): String; virtual;
  end;

procedure Register_HintStatusLine;

(* ----------------------------------------------------------------------- *)

IMPLEMENTATION

USES DM_Const;

const
  RHintStatusLine: TStreamRec = (
    ObjType: 1003; (* stHintStatus *)
    VmtLink: Ofs(TypeOf(THintStatusLine)^);
    Load   : @THintStatusLine.Load;
    Store  : @THintStatusLine.Store
  );

(* ----------------------------------------------------------------------- *)

procedure Register_HintStatusLine;
begin
  RegisterType (RHintStatusLine);
end; (* Register_HintStatusLine *)

constructor THintStatusLine.Load (var S: TStream);
begin
  inherited Load(S);
end; (* Load *)

procedure THintStatusLine.Store(var S: TStream);
begin
  inherited Store(S);
end; (* Store *)

(* ----------------------------------------------------------------------- *)
(* Provide a context-sensitive help message. *)
(* NOTE: This routine needs to be converted to a collection: TV p191 *)

function THintStatusLine.Hint (AHelpCtx: Word): String;
begin
  case AHelpCtx of
    cmViewTable:       Hint := 'ViewTable';
    cmCreateTable:     Hint := 'CreateTable';
    cmSubCriteria:     Hint := 'SubCriteria';
    cmSubQuery:        Hint := 'SubQuery';
    cmCreateNew:       Hint := 'CreateNew';
    cmAppendNew:       Hint := 'AppendNew';

    cmFindEntry:       Hint := 'FindEntry';
    cmAnotherFind:     Hint := 'AnotherFind';
    cmReplaceFind:     Hint := 'ReplaceFind';

    cmRandomEntry:     Hint := 'RandomEntry';
    cmAnotherRandom:   Hint := 'AnotherRandom';
    cmReplaceRandom:   Hint := 'ReplaceRandom';

    cmExpandMacros:    Hint := 'ExpandMacros';
    cmAnotherExpanded: Hint := 'AnotherExpanded';
    cmReplaceExpanded: Hint := 'ReplaceExpanded';

    cmScanTables:      Hint := 'ScanTables';
    cmRenameTable:     Hint := 'RenameTable';
    cmDeleteTable:     Hint := 'DeleteTable';
    cmIndexTables:     Hint := 'IndexTables';

    cmExecuteScript:   Hint := 'ExecuteScript';
    cmCreateScript:    Hint := 'CreateScript';
    cmViewScript:      Hint := 'ViewScript';
    cmDeleteScript:    Hint := 'DeleteScript';

    cmChar_Gen:        Hint := 'Char_Gen';
    cmNPC_Gen:         Hint := 'NPC_Gen';
    cmEncounters:      Hint := 'Encounters';
    cmTreasures:       Hint := 'Treasures';
    cmSpells:          Hint := 'Spells';
    cmWeather:         Hint := 'Weather';

    cmChanceTo:        Hint := 'ChanceTo';
    cmAttack:          Hint := 'Attack';
    cmCast:            Hint := 'Cast';
    cmNormal:          Hint := 'Normal';
    cmSpecialCT:       Hint := 'SpecialCT';
    cmRollDice:        Hint := 'RollDice';
    cmd4:              Hint := 'd4';
    cmd6:              Hint := 'd6';
    cmd8:              Hint := 'd8';
    cmd10:             Hint := 'd10';
    cmd12:             Hint := 'd12';
    cmd20:             Hint := 'd20';
    cmd100:            Hint := 'd100';
    cmRollAttr:        Hint := 'RollAttr';
    cmRollChar:        Hint := 'RollChar';
    cmRollSpecial:     Hint := 'RollSpecial';
    cmCombatMatrix:    Hint := 'CombatMatrix';

    cmCreateMap:       Hint := 'CreateMap';
    cmWilderness:      Hint := 'Wilderness';
    cmVillage:         Hint := 'Village';
    cmCity:            Hint := 'City';
    cmFortress:        Hint := 'Fortress';
    cmDungeon:         Hint := 'Dungeon';
    cmSpecial:         Hint := 'Special';
    cmAccessMap:       Hint := 'AccessMap';
    cmDeleteMap:       Hint := 'DeleteMap';

    cmQuit:            Hint := 'Quit';
    cmLooks:           Hint := 'Looks';
    cmZoom:            Hint := 'Zoom';
    cmHelp:            Hint := 'Help';

  else
    Hint := '';
  end;
end; (* Hint *)

(* ----------------------------------------------------------------------- *)

end.
