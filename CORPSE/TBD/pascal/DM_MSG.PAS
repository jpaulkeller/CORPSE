
UNIT DM_Msg;

{$O+,F+,X+,I-,S-}

INTERFACE

USES Drivers, Dialogs, Objects;

(* ----------------------------------------------------------------------- *)

type
  PRect = ^TRect;

type
  PMilestone = ^TMilestone;
  TMilestone = object(TDialog)
    Position : integer;
    ColsPerRow : integer;
    constructor Init (const BoxTitle  : string;
                            Width     : integer;
                            Quantity  : integer);
    procedure Update (      Attribute : word);
    procedure HandleEvent(var Event: TEvent); virtual;

  private

  end;

type
  PEntryDialog = ^TEntryDialog;
  TEntryDialog = object(TDialog)
    TableName : string;
    constructor Init     (const Name : string;
                          const Ttl  : string;
                          const Msg  : string;
                                Help : integer);
    constructor InitAt   (const Name : string;
                          const Ttl  : string;
                          const Msg  : string;
                                Help : integer;
                                Old  : TRect);
    constructor Load     (var   S    : TStream);
    procedure Store      (var   S    : TStream);

    procedure HandleEvent(var   Event: TEvent); virtual;
  end;

const
  REntryDialog: TStreamRec = (
    ObjType: 1002; (* stEntryDialog *)
    VmtLink: Ofs(TypeOf(TEntryDialog)^);
    Load   : @TEntryDialog.Load;
    Store  : @TEntryDialog.Store
  );

(* ----------------------------------------------------------------------- *)

procedure Register_EntryDialog;

(* ----------------------------------------------------------------------- *)

procedure ShowEntry  (const Name    : string;
                      const Ttl     : string;
                      const Msg     : string;
                            Help    : integer;
                            BoxP    : PRect);

procedure ShowMessage(const Title   : string;
                      const Msg     : string;
                            Help    : word);

(* ----------------------------------------------------------------------- *)

IMPLEMENTATION

uses App, Views, Util, DM_Const;

(* ----------------------------------------------------------------------- *)

constructor TMilestone.Init (const BoxTitle : string;
                                   Width    : integer;
                                   Quantity : integer);
var
  Buf: string;
  Height: integer;
  Box: TRect;
  TBx: TRect;
  Txt: PView;
  Win: PDialog;
  i: integer;

begin
  Position := 0;
  ColsPerRow := Width;
  Height := (Quantity div Width) + 1;

  Box.Assign(0, 0, Width + 1, Height + 2);
  Win := New(PDialog, Init(Box, BoxTitle));
  Win^.Options := Win^.Options or ofCentered;
  with Win^ do
  begin
    Buf := '';
    for i := 1 to Quantity do Buf := Buf + '°'; (* LIMITED TO 255 !!! *)
    TBx.Assign(1, 1, Width, Height + 1);
    Txt := New(PStaticText, Init(TBx, Buf));
    Insert(Txt);
    SelectNext(False);
  end;
  Application^.InsertWindow (Win);
end;  (* TMilestone.Init *)

(* ----------------------------------------------------------------------- *)

procedure TMilestone.Update (Attribute : word);
var c, r : integer;
begin
  Inc(Position);
  c := ((Position-1) mod ColsPerRow) + 1;
  r := ((Position-1) div ColsPerRow) + 1;
  {TpCRT.FastWriteWindow ('°', r, c, Attribute);}
end;  (* TMilestone.Update *)

(* ----------------------------------------------------------------------- *)

procedure TMilestone.HandleEvent(var Event: TEvent);
begin
  inherited HandleEvent(Event);
  if (Event.What = evBroadcast) and (Event.Command = cmFindMilestone) then
    ClearEvent(Event);
end;

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)

procedure ShowMessage(const Title : string;
                      const Msg   : string;
                            Help  : word);
var
  Box: TRect;
  TBx: TRect;
  Txt: PView;
  Win: PDialog;

begin
  Box.Assign(0, 0,
             Min(Max(length(Msg) + 2, length(Title) + MinWinSize.X), 80),
             MinWinSize.Y);
  Win := New(PDialog, Init(Box, Title));
  with Win^ do
  begin
    Options := Options or ofCentered or ofFirstClick (* or ofTileable *);
    HelpCtx := Help;
    TBx.Assign(1, 1, Size.X - 1, 2);
    Txt := New(PStaticText, Init(TBx, Msg));
    Insert(Txt);
  end;
  Application^.InsertWindow (Win);
end; (* ShowMessage *)

(* ----------------------------------------------------------------------- *)
(* ----------------------------------------------------------------------- *)

constructor TEntryDialog.Init (const Name : string;
                               const Ttl  : string;
                               const Msg  : string;
                                     Help : integer);
var
  Box: TRect;
  TBx: TRect;
  Txt: PView;
begin
  Box.Assign(0, 0,
             Min(Max(length(Msg) + 2, length(Ttl) + MinWinSize.X), 80),
             MinWinSize.Y);
  inherited Init(Box, Ttl);
  TableName := Name;
  Options := Options or ofCentered or ofFirstClick;
  HelpCtx := Help;
  (* insert the text message *)
  TBx.Assign(1, 1, Size.X - 1, 2);
  Txt := New(PStaticText, Init(TBx, Msg));
  Insert(Txt);
end; (* Init *)

(* ----------------------------------------------------------------------- *)

constructor TEntryDialog.InitAt (const Name : string;
                                 const Ttl  : string;
                                 const Msg  : string;
                                       Help : integer;
                                       Old  : TRect);
var
  Box: TRect;
  TBx: TRect;
  Txt: PView;
begin
  Box.Assign(0, 0,
             Min(Max(length(Msg) + 2, length(Ttl) + MinWinSize.X), 80),
             MinWinSize.Y);
  Box.Move (Old.A.X, Old.A.Y);
  inherited Init(Box, Ttl);
  TableName := Name;
  Options := Options or ofFirstClick;
  HelpCtx := Help;
  (* insert the text message *)
  TBx.Assign(1, 1, Size.X - 1, 2);
  Txt := New(PStaticText, Init(TBx, Msg));
  Insert(Txt);
end; (* InitAt *)

(* ----------------------------------------------------------------------- *)

constructor TEntryDialog.Load (var S: TStream);
begin
  inherited Load(S);
  S.Read(TableName[0], SizeOf(Char));
  S.Read(TableName[1], Length(TableName));
end; (* Load *)

procedure TEntryDialog.Store(var S: TStream);
begin
  inherited Store(S);
  S.Write(TableName, Length(TableName) + 1);
end; (* Store *)

(* ----------------------------------------------------------------------- *)

procedure Register_EntryDialog;
begin
  RegisterType (REntryDialog);
end; (* Register_EntryDialog *)

(* ----------------------------------------------------------------------- *)

procedure TEntryDialog.HandleEvent(var Event: TEvent);
begin
  inherited HandleEvent(Event);
  if (Event.What = evBroadcast) and (Event.Command = cmFindEntryWindow) then
    ClearEvent(Event);
end; (* HandleEvent *)

(* ----------------------------------------------------------------------- *)

procedure ShowEntry (const Name : string;
                     const Ttl  : string;
                     const Msg  : string;
                           Help : integer;
                           BoxP : PRect);
var
  Win: PEntryDialog;
begin
  if (BoxP = nil) then
    Win := New(PEntryDialog, Init(Name, Ttl, Msg, Help))
  else
    Win := New(PEntryDialog, InitAt(Name, Ttl, Msg, Help, BoxP^));
  Application^.InsertWindow (Win);
end; (* ShowEntry *)

end.
