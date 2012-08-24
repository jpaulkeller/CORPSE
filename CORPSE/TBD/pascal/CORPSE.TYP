
(* CORPSE.TYP -- game structures *)

const
  NameLength = 30;
  MaxWeapons = 10;

type
  Names      = string [ NameLength ];

  AttrNames  = ( Str, Dex, Agi, Con, Int, Wis, Dis, Cha, AgM );

  Attributes = array [ Str..AgM ] of integer;  (* -3..20 *)

  ClassConNames = ( (* -------- TRAINING TYPES -------- *)
                    Arms,    Unarmed,  Defense, Healing,
                    Stealth, Thievery, Faith,   Language,

                    (* -------- SPELL TYPES -------- *)
                    Abjuration, Alteration, Conjuration,
                    Divination, Empathic,   Enchantment,
                    Evocation,  Hypnotic,   Illusion,
                    Invocation, Metamagic,  Necromantic,
                    Spiritual,  UnknownSpellType );

  ClassCons    = array [ Arms..UnknownSpellType ] of integer;  (* 0..5 *)

  Classes      = record
                   Name : Names;
                   Exp  : ClassCons;  (* training experience *)
                 end;

  SaveNames    = ( Chemical, Natural, SpellMagic, Devices );

  Saves        = array [ Chemical..Devices ] of integer;  (* 1..5 *)

  Races        = record
                   Name       : Names;
                   Save       : Saves;
                   Perception : integer;
                   AMod       : Attributes;
                   SizeMod    : integer;
                   ArmorClass : integer;
                   AvgWeight  : integer;
                 end;

  Elements     = ( NoElement, Air, Earth, Fire, Water, Void, Life, Death );
  ElementSet   = set of Elements;

  Genders      = ( UnknownGender, Male, Female );

  Armors       = record
                   Name    : Names;
                   Defense : integer;
                   Class   : integer;
                   AgiMod  : integer;
                 end;

  Weapons      = record
                   Name     : string [20];
                   Time     : integer;
                   Damage   : integer;
                   MinDex   : integer;
                   MinStr   : integer;
                   Weight   : integer;
                   Use      : string [3];
                   Category : string [3];
                   Range    : integer;
                   Cost     : integer;
                 end;

  Characters   = record
                   Name     : Names;
                   Player   : Names;
                   Level    : integer;     (* 1..10 *)
                   Attr     : Attributes;
                   Class    : Classes;
                   Race     : Races;
                   Element  : Elements;    (* could be ElementSet *)
                   Gender   : Genders;
                   Age      : string [10];
                   Height   : string [10];
                   Weight   : integer;     (* in pounds *)
                   PrevProf : string [60];
                   Armor    : Armors;
                 end;

  WeaponStats  = record
                   Std   : Weapons;
                   Plus  : integer;     (* character's proficiency 0..5 *)
                   ACon  : integer;     (* character's ACon with weapon *)
                   AConM : integer;     (* character's modified ACon "  *)
                   Time  : integer;     (* seconds per attack           *)
                 end;

  CharStats    = record
                   SaveVs : array [ SaveNames ] of integer;
                   ACon, AConM, MCon, UCon, UConM, DCon, DConM,
                   Speed, SpeedM, AtkTime, AtkTimeM,
                   Perception, Avoid, AvoidM, HitPts : integer;
                   Weapon : array [ 1..MaxWeapons ] of WeaponStats;
                 end;

  CharRecs     = record   (* for file Dump/Load ability *)
                   C : Characters;
                   S : CharStats;
                 end;

  Spells       = record
                   Name     : string [24];
                   Level    : integer;     (* 0..10 *)
                   MainType : Abjuration..UnknownSpellType;
                   AltType  : Abjuration..UnknownSpellType;
                   Class    : string [3];
                   Source   : string [6];
                   Elements : ElementSet;
                 end;

(*
  CORPSE_Types = ( Armor, Character, Class, Race, Spell, Weapon );

  CORPSE_Recs  = record
                   case CORPSE_Type : CORPSE_Types of
                   Armor     : ( Armor     : Armors     );
                   Character : ( Character : Characters );
                   Class     : ( Class     : Classes    );
                   Race      : ( Race      : Races      );
                   Spell     : ( Spell     : Spells     );
                   Weapon    : ( Weapon    : Weapons    );
                 end;

  CORPSE_Ptrs  = ^CORPSE_Recs;

*)