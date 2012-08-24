{$IFDEF Windows}                                                      {!!.20}
  !! ERROR: This unit is not compatible with Windows applications !!  {!!.20}
{$ENDIF}                                                              {!!.20}

{$S-,R-,V-,I-,B-,F+}

{$IFNDEF Ver40}
  {$R-,O+,A-}
{$ENDIF}

{$I TPDEFINE.INC} {!!.11}

{*********************************************************}
{*                    TPDOS.PAS 5.20                     *}
{*     Copyright (c) TurboPower Software 1987, 1992.     *}
{* Portions Copyright (c) Sunny Hill Software 1985, 1986 *}
{*     and used under license to TurboPower Software     *}
{*                 All rights reserved.                  *}
{*********************************************************}

unit TpDos;
  {-Miscellaneous DOS/BIOS call routines}

interface

uses
  Dpmi,        {!!.20}
  Dos,
  TpString;

type
  ActionCodeType = (
    ExecSaveScreen, ExecShowMemory, ExecPauseAfterRun, ExecRestoreScreen);
  DiskClass = (
    Floppy360, Floppy720, Floppy12, Floppy144, OtherFloppy, Bernoulli,
    HardDisk, RamDisk, SubstDrive, UnknownDisk, InvalidDrive,
    NovellDrive, CDRomDisk); {!!.12}{!!.20}

var
  IoResultPtr : Pointer;     {if not nil, must point to a routine like
                              Int24Result that returns an I/O error code}
const
  StackSafetyMargin : Word = 1000;
  MinSpaceForDos : Word = 20000; {Minimum bytes for DOS shell to run}

  StdInHandle = 0;           {handle numbers for OpenStdDev}
  StdOutHandle = 1;
  StdErrHandle = 2;
  StdPrnHandle = 4;

function DOSVersion : Word;
  {-Returns the DOS version number. High byte has major version number,
    low byte has minor version number. Eg., DOS 3.1 => $0301.}

function NumberOfDrives : Byte;
  {-Returns the number of logical drives}

procedure SelectDrive(Drive : Char);
  {-Selects the specified drive as default if possible}

function DefaultDrive : Char;
  {-Returns the default drive as an uppercase letter}

function GetDiskInfo(Drive : Byte; var ClustersAvailable, TotalClusters,
                     BytesPerSector, SectorsPerCluster : Word) : Boolean;
  {-Return technical info about the specified drive}

function GetDiskClass(Drive : Char; var SubstDriveChar : Char) : DiskClass;
  {-Return the disk class for the drive with the specified letter}

{$IFNDEF Dpmi}
function ReadDiskSectors(Drive : Word; FirstSect : Longint;
                         NumSects : Word; var Buf) : Boolean;
  {-Read absolute disk sectors.}

function WriteDiskSectors(Drive : Word; FirstSect : Longint;
                          NumSects : Word; var Buf) : Boolean;
  {-Write absolute disk sectors.}
{$ENDIF}

function GetFileMode(FName : string; var Attr : Word) : Byte;
  {-Returns a file's attribute in Attr and the DOS error code as the function
    result.}

function FileHandlesLeft : Byte;
  {-Return the number of available file handles}

function FileHandlesOpen(CountDevices : Boolean) : Byte;
  {-Return the number of open files owned by a program}

procedure SetDta(DTAptr : Pointer);
  {-Set the DOS DTA to point to DTAptr}

procedure GetDta(var DTAptr : Pointer);
  {-Return the DOS DTA pointer}

function VerifyOn : Boolean;
  {-Returns True if disk write verification is on}

procedure SetVerify(On : Boolean);
  {-Turn disk write verification on/off}

function ParsePath(var InputPath, SearchPath, LeadInPath : string) : Boolean;
  {-Takes a user entered path, trims blanks, and returns a valid global
    search path and a valid lead-in path.}

function PrintInstalled : Boolean;
  {-Returns True if PRINT.COM is installed}

function SubmitPrintFile(FileName : string) : Byte;
  {-This procedure submits a file to the PC DOS 3.0 or greater concurrent
   print utility.}

procedure CancelPrintFile(FileMask : string);
  {-Cancels the files matched by the file mask passed in FileMask.}

procedure CancelAllPrintFiles;
  {-Cancels all files in the print queue}

function GetPrintStatus(var QPtr : Pointer) : Byte;
 {-Halts printing, returns current error status, puts pointer to the filename
   queue in the QPtr variable. Filenames in the queue are 64-byte ASCIIZ
   strings. The end of the queue is marked by a name starting with a null.}

procedure EndPrintStatus;
  {-Releases the spooler from the GetPrintStatus procedure.}

function GetEnvironmentString(SearchString : string) : string;
  {-Return a string from the environment}

function SetBlock(var Paragraphs : Word) : Boolean;
  {-Change size of DOS memory block allocated to this program}

function ExecDos(Command : string; UseSecond : Boolean; UserRoutine : Pointer) : Integer;
 {-Execute any DOS command. Call with Command = '' for a new shell.
   If UseSecond is false, Command must be the full pathname of a program to
   be executed. UserRoutine is the address of a routine to display status,
   save/restore the screen, etc., or a nil pointer. It must be of the form:

   procedure UserRoutine(ActionCode : ActionCodeType; Param : Word);

   and it must have the FAR attribute. ExecDos return codes are as follows:
         0 : Success
        -1 : Insufficient memory to store free list
        -2 : DOS setblock error before EXEC call
        -3 : DOS setblock error after EXEC call  -- critical error!
        -4 : Insufficient memory to run DOS command
      else   a DOS error code
  }

function TextSeek(var F : Text; Target : LongInt) : Boolean;
 {-Do a Seek for a text file opened for input. Returns False in case of I/O
   error.}

function TextFileSize(var F : Text) : LongInt;
  {-Return the size of text file F. Returns -1 in case of I/O error.}

function TextPos(var F : Text) : LongInt;
 {-Return the current position of the logical file pointer (that is,
   the position of the physical file pointer, adjusted to account for
   buffering). Returns -1 in case of I/O error.}

function TextFlush(var F : Text) : Boolean;
  {-Flush the buffer(s) for a text file. Returns False in case of I/O error.}

function OpenStdDev(var F : Text; StdHandle : Word) : Boolean;
  {-Assign the text file to a standard DOS device: 0, 1, 2, or 4}

function HandleIsConsole(Handle : Word) : Boolean;
  {-Return true if handle is the console device}

procedure SetRawMode(var F : Text; On : Boolean);
  {-Set "raw" mode on or off for the specified text file (must be a device)}

function ExistFile(FName : string) : Boolean;
  {-Return true if file is found}

function ExistOnPath(FName : string; var FullName : string) : Boolean;
 {-Return true if fname is found in
   a) current directory
   b) program's directory (DOS 3.X only)
   c) any DOS path directory
  and return full path name to file}

function TimeMs : LongInt;
  {-Return time of day in milliseconds since midnight}

  {============================================================================}

implementation

type
  SegOfs = record
             O, S : Word;
           end;
  LongRec = record
              LowWord, HighWord : Word; {structure of a LongInt}
            end;

  {text buffer}
  TextBuffer = array[0..65520] of Byte;

  {structure of a Turbo File Interface Block}
  FIB =
    record
      Handle : Word;
      Mode : Word;
      BufSize : Word;
      Private : Word;
      BufPos : Word;
      BufEnd : Word;
      BufPtr : ^TextBuffer;
      OpenProc : Pointer;
      InOutProc : Pointer;
      FlushProc : Pointer;
      CloseProc : Pointer;
      UserData : array[1..16] of Byte;
      Name : array[0..79] of Char;
      Buffer : array[0..127] of Char;
    end;
const
  FMClosed = $D7B0;
  FMInput = $D7B1;
  FMOutput = $D7B2;
  FMInOut = $D7B3;
var
  Regs : Registers;
const                         {!!.10}
  DSReadDrive  : Byte = $FF;  {!!.10}
  DSWriteDrive : Byte = $FF;  {!!.10}
  DSReadBig    : Byte = $00;  {!!.10}
  DSWriteBig   : Byte = $00;  {!!.10}

  function DOSVersion : Word;
  {-Returns the DOS version number. High byte has major version number,
    low byte has minor version number. Eg., DOS 3.1 => $030A ($A = 10).}
  begin
    with Regs do begin
      AH := $30;             {Get MS-DOS version number}
      MsDos(Regs);
      DOSVersion := Swap(AX); {put major version in high byte, minor in low}
    end;
  end;

  {$L TPDISK.OBJ}

  function NumberOfDrives : Byte; external;
  procedure SelectDrive(Drive : Char); external;
  function DefaultDrive : Char; external;
  function GetDiskInfo(Drive : Byte; var ClustersAvailable, TotalClusters,
                       BytesPerSector, SectorsPerCluster : Word) : Boolean;
    external;
{$IFNDEF Dpmi}
  function ReadDiskSectors(Drive : Word; FirstSect : Longint;
                           NumSects : Word; var Buf) : Boolean;
    external;
  function WriteDiskSectors(Drive : Word; FirstSect : Longint;
                            NumSects : Word; var Buf) : Boolean;
    external;
{$ENDIF}

{$IFDEF Dpmi}  {!!.20 - Additions for protected mode}

  function GetDiskClass(Drive : Char; var SubstDriveChar : Char) : DiskClass;
    {-Return the disk class for the drive with the specified letter}
    {-This routine uses two undocumented DOS function ($32 and $52).
      Information about these functions was obtained from Terry Dettmann's DOS
      Programmer's Reference (Que, 1988) and Undocumented DOS (Addison-Wesley,
      1990).}
  type
    NovellTable = array[1..32] of Byte;
    DriveParameterBlock =
      record
        DriveNum, DeviceDriverUnit : Byte;
        BytesPerSector : Word;
        SectorsPerCluster, ShiftFactor : Byte;
        BootSectors : Word;
        FatCopies : Byte;
        RootDirEntries, FirstDataSector, HighestCluster : Word;
        case Byte of
          0 : (SectorsPerFat23 : Byte;
               FirstDirSector23 : Word;
               DeviceDriver23 : Pointer;
               MediaDescriptor23 : Byte;
               AccessFlag23 : Byte;
               Next23 : Pointer;
               Reserved23 : LongInt);
          1 : (SectorsPerFat45 : Word;
               FirstDirSector45 : Word;
               DeviceDriver45 : Pointer;
               MediaDescriptor45 : Byte;
               AccessFlag45 : Byte;
               Next45 : Pointer;
               Reserved45 : LongInt);
      end;
  var
    Ver, DN : Byte;
    MediaDescriptor : Byte;
    SectorsPerFat : Word;
    Found : WordBool;
    pDPBP, DPBP : ^DriveParameterBlock;
    NTP : ^NovellTable;
    Sele : Word;
    Continue : WordBool;
    Regs : DPMIRegisters;
    CDRI : Boolean;
    CDRN : Word;
  Label
    WalkSFT, Evaluate, ExitPoint;

    function CDRomInstalled : Boolean;
    var
      R : DpmiRegisters;
    begin
      FillChar(R, SizeOf(R), 0);
      with R do begin
        AX := $1500;
        if SimulateRealModeInt($2F, R) <> 0 then ;
        CDRomInstalled := (DoubleWord(BX).LoWord <> 0);
      end;
    end;

    function IsDriveCDROM(Drive : Char) : Boolean;
    var
      R : DpmiRegisters;
    begin
      FillChar(R, SizeOf(R), 0);
      with R do begin
        AX := $150B;
        CX := Ord(Upcase(Drive))-Ord('A');
        if SimulateRealModeInt($2F, R) <> 0 then ;
        IsDriveCDRom := (DoubleWord(AX).LoWord <> 0) and
                        (DoubleWord(BX).LoWord = $ADAD);
      end;
    end;

  begin
    {assume failure}
    GetDiskClass := InvalidDrive;
    {assume that this is not a SUBSTituted drive}
    SubstDriveChar := Drive;

    {convert drive letter to drive number}
    Drive := Upcase(Drive);
    case Drive of
      'A'..'Z' : DN := Ord(Drive)-Ord('A');
      else Exit;
    end;

    {check for CDRom installed}
    CDRI := CDRomInstalled;

    {we need a selector for the following}
    if GetSelectorForRealMem(nil, $FFFF, Sele) <> 0 then
      Exit;

    {get DOS version number}
    Ver := Hi(DosVersion);

    {get pointer to drive parameter block}
    asm
      mov     ah,32h
      mov     dl,DN
      inc     dl
      push    ds
      int     21h
      mov     dx,ds
      pop     ds
      cmp     al,0FFh
      je      WalkSFT
      mov     word ptr pDPBP,bx
      mov     word ptr pDPBP+2,dx
      jmp     Evaluate
    end;
WalkSFT:
    {DPMI16BI doesn't support this call, so virtualize it}
    FillChar(Regs, SizeOf(Regs), 0);
    Regs.AX := $5200;
    if SimulateRealModeInt($21, Regs) <> 0 then
      goto ExitPoint;
    if SetSegmentBaseAddr(Sele, Linear(Ptr(Regs.ES, Regs.BX))) <> 0 then
      Goto ExitPoint;
    DPBP := Ptr(MemW[Sele:2], MemW[Sele:0]);
    if SetSegmentBaseAddr(Sele, Linear(DPBP)) <> 0 then
      Goto ExitPoint;
    Continue := Word(DPBP) <> $FFFF;
    pDPBP := Ptr(Sele, 0);

    Found := False;
    while (Word(DPBP) <> $FFFF) and not Found do begin
      if pDPBP^.DriveNum = DN then
        Found := True
      else if Ver < 4 then
        Move(pDPBP^.Next23, DPBP, SizeOf(DPBP))
      else
        Move(pDPBP^.Next45, DPBP, SizeOf(DPBP));

      Continue := Word(DPBP) <> $FFFF;
      if (not Found) and Continue then begin
        if SetSegmentBaseAddr(Sele, Linear(DPBP)) <> 0 then
          Goto ExitPoint;
      end;
    end;
    if not Found then begin
      {386MAX 6.01 doesn't support this call, so virtualize it}
      FillChar(Regs, SizeOf(Regs), 0);
      Regs.SI := 0;
      Regs.ES := 0;
      Regs.AX := $EF01;
      if SimulateRealModeInt($21, Regs) <> 0 then
        goto ExitPoint;
      if (Regs.ES = 0) and (Regs.SI = 0) then
        goto ExitPoint;
      if SetSegmentBaseAddr(Sele, Linear(Ptr(Regs.ES, Regs.SI))) <> 0 then
        goto ExitPoint;
      NTP := Ptr(Sele, 0);
      if NTP^[DN+1] and $03 <> 0 then
        GetDiskClass := NovellDrive
      else if (CDRI) and (IsDriveCDRom(Drive)) then
        GetDiskClass := CDRomDisk;
      Goto ExitPoint;
    end;
  Evaluate:
      with pDPBP^ do begin
        if Ver < 4 then begin
          MediaDescriptor := MediaDescriptor23;
          SectorsPerFat := SectorsPerFat23;
        end
        else begin
          MediaDescriptor := MediaDescriptor45;
          SectorsPerFat := SectorsPerFat45;
        end;

        {check for SUBSTituted drive}
        if (DriveNum <> DN) then begin
          GetDiskClass := SubstDrive;
          SubstDriveChar := Char(Ord('A')+DriveNum);
        end
        else if (FatCopies = 1) then
          {RAM disks have one copy of File Allocation Table}
          GetDiskClass := RamDisk
        else if (MediaDescriptor = $F8) then
          {MediaDescriptor of $F8 indicates hard disk}
          GetDiskClass := HardDisk
        else if (MediaDescriptor >= $FC) and (SectorsPerFat <> 2) then
          {Bernoulli drives have more than 2 sectors per FAT}
          GetDiskClass := Bernoulli
        else if (MediaDescriptor >= $F9) then
          {media descriptors >= $F9 are for floppy disks}
          case HighestCluster of
             355 : GetDiskClass := Floppy360;
             714,
            1423 : GetDiskClass := Floppy720;
            2372 : GetDiskClass := Floppy12;
            else   GetDiskClass := OtherFloppy;
          end
        else if (MediaDescriptor = $F0) and (HighestCluster = 2848) then
          {it's a 1.44 meg floppy}
          GetDiskClass := Floppy144
        else if (CDRI) and (IsDriveCDRom(Drive)) then
          {it's a CDRom}
          GetDiskClass := CDRomDisk
        else
          {unable to classify disk/drive}
          GetDiskClass := UnknownDisk;
      end;
    ExitPoint:
      if FreeLDTDescriptor(Sele) = 0 then ;
  end;

{$ELSE}

  function GetDiskClass(Drive : Char; var SubstDriveChar : Char) : DiskClass;
    {-Return the disk class for the drive with the specified letter}
    {-This routine uses two undocumented DOS function ($32 and $52).
      Information about these functions was obtained from Terry Dettmann's DOS
      Programmer's Reference (Que, 1988) and Undocumented DOS (Addison-Wesley,
      1990).}
  type                                {!!.13} {rewritten}
    NovellTable = array[1..32] of Byte;
    DriveParameterBlock =
      record
        DriveNum, DeviceDriverUnit : Byte;
        BytesPerSector : Word;
        SectorsPerCluster, ShiftFactor : Byte;
        BootSectors : Word;
        FatCopies : Byte;
        RootDirEntries, FirstDataSector, HighestCluster : Word;
        case Byte of
          0 : (SectorsPerFat23 : Byte;
               FirstDirSector23 : Word;
               DeviceDriver23 : Pointer;
               MediaDescriptor23 : Byte;
               AccessFlag23 : Byte;
               Next23 : Pointer;
               Reserved23 : LongInt);
          1 : (SectorsPerFat45 : Word;
               FirstDirSector45 : Word;
               DeviceDriver45 : Pointer;
               MediaDescriptor45 : Byte;
               AccessFlag45 : Byte;
               Next45 : Pointer;
               Reserved45 : LongInt);
      end;
  var
    Ver, DN : Byte;
    MediaDescriptor : Byte;
    SectorsPerFat : Word;
    DPBP : ^DriveParameterBlock;
    NTP : ^NovellTable;
    CDRI : Boolean;                       {!!.20 - begin}
    CDRN : Word;

    function CDRomInstalled : Boolean;
    var
      R : Registers;
    begin
      FillChar(R, SizeOf(R), 0);
      with R do begin
        AX := $1500;
        Intr($2F, R);
        CDRomInstalled := (BX <> 0);
      end;
    end;

    function IsDriveCDROM(Drive : Char) : Boolean;
    var
      R : Registers;
    begin
      FillChar(R, SizeOf(R), 0);
      with R do begin
        AX := $150B;
        CX := Ord(Upcase(Drive))-Ord('A');
        Intr($2F, R);
        IsDriveCDRom := (AX <> 0) and (BX = $ADAD);
      end;
    end;                                     {!!.20 - end}

  begin
    {assume failure}
    GetDiskClass := InvalidDrive;

    {assume that this is not a SUBSTituted drive}
    SubstDriveChar := Drive;

    {convert drive letter to drive number}
    Drive := Upcase(Drive);
    case Drive of
      'A'..'Z' : DN := Ord(Drive)-Ord('A');
      else Exit;
    end;

    {check for CDRom driver installed}              {!!.20}
    CDRI := CDRomInstalled;                         {!!.20}

    {get DOS version number}
    Ver := Hi(DosVersion);

    with Regs do begin
      {get pointer to drive parameter block}
      AH := $32;
      DL := DN+1;
      MsDos(Regs);

      if (AL <> $FF) then
        DPBP := Ptr(DS,BX)
      else begin
        {see if it's a Novell drive}
        AX := $EF01;
        ES := 0;
        SI := 0;
        MsDos(Regs);
        NTP := Ptr(ES,SI);
        if NTP <> nil then begin          {!!.20 - begin}
          if NTP^[DN+1] and $03 <> 0 then
            GetDiskClass := NovellDrive
          else if (CDRI) and (IsDriveCDRom(Drive)) then
            GetDiskClass := CDRomDisk;
          Exit;
        end;                              {!!.20 - end}
      end;

      with DPBP^ do begin
        if Ver < 4 then begin
          MediaDescriptor := MediaDescriptor23;
          SectorsPerFat := SectorsPerFat23;
        end
        else begin
          MediaDescriptor := MediaDescriptor45;
          SectorsPerFat := SectorsPerFat45;
        end;

        {check for SUBSTituted drive}
        if (DriveNum <> DN) then begin
          GetDiskClass := SubstDrive;
          SubstDriveChar := Char(Ord('A')+DriveNum);
        end
        else if (FatCopies = 1) then
          {RAM disks have one copy of File Allocation Table}
          GetDiskClass := RamDisk
        else if (MediaDescriptor = $F8) then
          {MediaDescriptor of $F8 indicates hard disk}
          GetDiskClass := HardDisk
        else if (MediaDescriptor >= $FC) and (SectorsPerFat <> 2) then
          {Bernoulli drives have more than 2 sectors per FAT}
          GetDiskClass := Bernoulli
        else if (MediaDescriptor >= $F9) then
          {media descriptors >= $F9 are for floppy disks}
          case HighestCluster of
             355 : GetDiskClass := Floppy360;
             714,
            1423 : GetDiskClass := Floppy720;
            2372 : GetDiskClass := Floppy12;
            else   GetDiskClass := OtherFloppy;
          end
        else if (MediaDescriptor = $F0) and (HighestCluster = 2848) then
          {it's a 1.44 meg floppy}
          GetDiskClass := Floppy144
        else if (CDRI) and (IsDriveCDRom(Drive)) then              {!!.20}
          {it's a CDRom}                                           {!!.20}
          GetDiskClass := CDRomDisk                                {!!.20}
        else
          {unable to classify disk/drive}
          GetDiskClass := UnknownDisk;
      end;
    end;
  end;

{$ENDIF}

  function GetFileMode(FName : string; var Attr : Word) : Byte;
    {-Returns a file's attribute in Attr and the DOS error code as the function
      result.}
  var
    F : file;
  begin
    Assign(F, FName);
    {call routine in Turbo's DOS unit to get the attribute}
    GetFAttr(F, Attr);
    GetFileMode := DosError;
  end;

  procedure SetDta(DTAptr : Pointer);
    {-Set the DOS DTA to point to DTA}
  type
    OS =
      record
        O, S : Word;
      end;
  begin
    with Regs do begin
      AH := $1A;
      DS := OS(DTAptr).S;    {!!.20}
      DX := OS(DTAptr).O;    {!!.20}
      MsDos(Regs);
    end;
  end;

  procedure GetDta(var DTAptr : Pointer);
    {-Return the DOS DTA pointer}
  var
    Regs : Registers;
  begin
    with Regs do begin
      AH := $2F;
      MsDos(Regs);
      DTAptr := Ptr(ES, BX);
    end;
  end;

  function VerifyOn : Boolean;
    {-Returns True if disk write verification is on}
  begin
    Regs.AH := $54;          {Get verify state function}
    MsDos(Regs);
    VerifyOn := Boolean(Regs.AL);
  end;

  procedure SetVerify(On : Boolean);
    {-Turn disk write verification on/off}
  begin
    Regs.DL := 0;            {only MicroSoft knows for sure}
    Regs.AL := Byte(On);     {0 = off, 1 = on}
    Regs.AH := $2E;          {Set verify state function}
    MsDos(Regs);
  end;

  function ParsePath(var InputPath, SearchPath, LeadInPath : string) : Boolean;
    {-Takes a user entered path, trims blanks, and returns a valid global
      search path and a valid lead-in path.}
  var                                        {!!.12} {replaced}
    S : string;
    SLen : Byte absolute S;
    Attr : Word;

    function IsPath(S : string) : Boolean;
      {-Return True if S is empty or ends with ':' or '\'}
    var
      SLen : Byte absolute S;
    begin
      {check last character in S}
      case S[SLen] of
        ':' : IsPath := (SLen = 2);
        '\' : IsPath := True;
        '.' : IsPath := (SLen = 1) or (S = '..') or
                        ((SLen > 2) and
                         ((Pos('\..', S) = (SLen - 2)) or
                          (Pos(':.', S)  = (SLen - 1)) or
                          (Pos(':..', S) = (SLen - 2)) )
                        );
        else IsPath := (SLen = 0); {True if string is empty}
      end;
    end;

    function NameIsValid(S : string) : Boolean;
    var
      I : Word;
    begin
      NameIsValid := False;
      for I := 1 to Length(S) do
        if S[I] in [#0..#31, '"', '[', ']', '|', ';'..'>', '+', ','] then
          Exit;
      NameIsValid := True;
    end;

  begin
    {Assume success}
    ParsePath := True;

    {Get working copy of InputPath; convert to uppercase and trim blanks}
    S := StUpCase(Trim(InputPath));

    if (Pos(':', S) > 2) or (Pos('/', S) <> 0) or
       (Pos('::', S) <> 0) or (Pos('\\', S) <> 0) then
      ParsePath := False
    {if S is just a path name, add "*.*" to search path}
    else if IsPath(S) then begin
      if S[SLen] = '.' then
        S := S+'\';
      LeadInPath := S;
      SearchPath := S+'*.*';
    end
    else
      if SLen >= 77 then
        ParsePath := False
      else
        {test validity of pathname by calling routine to get file attribute}
        case GetFileMode(S, Attr) of

          0 : if (Attr and Directory {= $10} ) <> 0 then begin
                {Input path is valid directory name}
                SearchPath := S+'\*.*';
                LeadInPath := S+'\';
              end
              else begin
                {Input path is the name of a file}
                SearchPath := S;

                {trim end of string until only a path is left}
                while not IsPath(S) do
                  Dec(SLen);
                LeadInPath := S
              end;

          3 : begin
                {path not found}

                {reject clearly invalid names}
                ParsePath := NameIsValid(S);

                SearchPath := S;

                {trim end of string until only a path is left}
                while not IsPath(S) do
                  Dec(SLen);

                if (S[SLen] <> ':') or (SLen = 2) then
                  LeadInPath := S
                else
                  ParsePath := False;
              end;
        else
          ParsePath := False;
        end;
  end;

  function PrintInstalled : Boolean;
    {-Returns True if PRINT.COM is installed}
  begin
    {INT $2F functions available only in DOS 3}
    if DOSVersion >= $300 then
      with Regs do begin
        AX := $0100;         {get PRINT installed status}
        Intr($2F, Regs);     {print spool control interrupt}
        PrintInstalled := (AL = $FF); {DOS returns $FF in AL if PRINT installed}
      end
    else
      PrintInstalled := False;
  end;

  function SubmitPrintFile(FileName : string) : Byte;
    {-This procedure submits a file to the PC DOS 3.0 or greater concurrent
      print utility.}
  type
    AsciiZ = array[1..65] of Char;
    SubmitPacket = record
                     Level : Byte;
                     FilenamePtr : ^AsciiZ;
                   end;
  var
    SubPack : SubmitPacket;
    S : string;
    SLen : Byte absolute S;
  begin
    S := Trim(FileName);
    S := FullPathName(S); {!!.12}
    if SLen <> 0 then
      with SubPack, Regs do begin
        Level := 0;          {set level code}
        if SLen > 64 then
          SLen := 64;        {truncate filenames longer than 64 characters}
        S[Succ(SLen)] := #0; {add null to end of string}
        FilenamePtr := @S[1]; {point to first character in S}
        DS := Seg(SubPack);  {DS:DX points to the packet}
        DX := Ofs(SubPack);
        AX := $0101;         {submit file to be printed}
        Intr($2F, Regs);     {print spool control interrupt}
        if Odd(Flags) then   {check carry flag}
          SubmitPrintFile := AL {carry set, return code in AL}
        else
          SubmitPrintFile := 0;
      end
    else
      SubmitPrintFile := 2;  {return the code for a file not found error}
  end;

  procedure CancelPrintFile(FileMask : string);
    {-Cancels the files matched by the file mask passed in FileMask.}
  var
    Len : Byte absolute FileMask;
  begin
    if Len > 64 then
      Len := 64;             {truncate filenames longer than 64 characters}
    with Regs do begin
      FileMask[Succ(Len)] := #0; {make FileMask an ASCIIZ string}
      DS := Seg(FileMask);   {DS:DX points to the ASCIIZ string}
      DX := Ofs(FileMask[1]);
      AX := $0102;           {cancel print file}
      Intr($2F, Regs);       {print spool control interrupt}
    end;
  end;

  procedure CancelAllPrintFiles;
    {-Cancels all files in the print queue}
  begin
    Regs.AX := $0103;        {cancel all files function}
    Intr($2F, Regs);         {print spool control interrupt}
  end;

  function GetPrintStatus(var QPtr : Pointer) : Byte;
    {-Halts printing, returns current error status, puts pointer to the filename
      queue in the QPtr variable. Filenames in the queue are 64-byte ASCIIZ
      strings. The end of the queue is marked by a name starting with a null.}
  begin
    with Regs do begin
      AX := $0104;           {access print queue function}
      Intr($2F, Regs);       {print spool control interrupt}
      {check carry flag}
      if Odd(Flags) then begin
        {carry set, return code in AL}
        QPtr := nil;
        GetPrintStatus := AL;
      end
      else begin
        {DS:SI points to the queue}
        QPtr := Ptr(DS, SI);
        GetPrintStatus := 0;
      end;
    end;
  end;

  procedure EndPrintStatus;
    {-Releases the spooler from the GetPrintStatus procedure.}
  begin
    Regs.AX := $0105;        {unfreeze queue function}
    Intr($2F, Regs);         {print spool control interrupt}
  end;

  function GetEnvironmentString(SearchString : string) : string;
    {-Return a string from the environment}
  type
    Env = array[0..32767] of Char;
  var
    EPtr : ^Env;
    EStr : string;
    EStrLen : Byte absolute EStr;
    Done : Boolean;
    SearchLen : Byte absolute SearchString;
    I : Word;
  begin
    GetEnvironmentString := '';
    if SearchString = '' then
      Exit;

    {force upper case}
    for I := 1 to SearchLen do
      SearchString[I] := Upcase(SearchString[I]);

    EPtr := Ptr(MemW[PrefixSeg:$2C], 0);
    I := 0;
    if SearchString[SearchLen] <> '=' then
      SearchString := SearchString+'=';
    Done := False;
    EStrLen := 0;
    repeat
      if EPtr^[I] = #0 then begin
        if EPtr^[Succ(I)] = #0 then begin
          Done := True;
          if SearchString = '==' then begin
            EStrLen := 0;
            Inc(I, 4);
            while EPtr^[I] <> #0 do begin
              Inc(EStrLen);
              EStr[EStrLen] := EPtr^[I];
              Inc(I);
            end;
            GetEnvironmentString := EStr;
          end;
        end;
        if Copy(EStr, 1, SearchLen) = SearchString then begin
          GetEnvironmentString := Copy(EStr, Succ(SearchLen), 255);
          Done := True;
        end;
        EStrLen := 0;
      end
      else begin
        Inc(EStrLen);
        EStr[EStrLen] := EPtr^[I];
      end;
      Inc(I);
    until Done;
  end;

  function PtrDiff(H, L : Pointer) : LongInt;
    {-Return the number of bytes between H^ and L^. H is the higher address}
  var
    High : SegOfs absolute H;
    Low : SegOfs absolute L;
  begin
    PtrDiff := (LongInt(High.S) shl 4+High.O)-(LongInt(Low.S) shl 4+Low.O);
  end;

  function SetBlock(var Paragraphs : Word) : Boolean;
    {-Change size of DOS memory block allocated to this program}
  begin
    with Regs do begin
      AH := $4A;
      ES := PrefixSeg;
      BX := Paragraphs;
      MsDos(Regs);
      Paragraphs := BX;
      SetBlock := not Odd(Flags);
    end;
  end;

{$IFNDEF Ver40}
  function UsingEmulator : Boolean;
    {-Return True if floating point emulator in use}
  type
    Array3 = array[1..3] of Char;
  const
    EmuSignature : Array3 = 'emu';
  var
    A3P : ^Array3;
  begin
    A3P := Ptr(SSeg, $E0);
    {using emulator if Test8087 is 0 and emulator's signature is found in SS}
    UsingEmulator := (Test8087 = 0) and (A3P^ = EmuSignature);
  end;
{$ENDIF}

{$IFDEF Dpmi}  {!!.20} {New version of ExecDos for PMode follows}

  function ExecDos(Command : string; UseSecond : Boolean; UserRoutine : Pointer) : Integer;
    {-Execute any DOS command. Call with Command = '' for a new shell. If
      UseSecond is false, Command must be the full pathname of a program to be
      executed}

    procedure CallUserRoutine(ActionCode : ActionCodeType; Param : Word);
      {-Call UserRoutine with an action code}
    inline(
      $FF/$5E/<UserRoutine); {call far dword ptr [bp+<UserRoutine]}

  var
    {Variables for parsing the command line}
    BlankPos : Word;
    PathName : string[127];
    CommandTail : string[127];

    SaveDta : Pointer;
  begin
    {get parameters for Execute}
    if Command = '' then
      UseSecond := True;
    CommandTail := '';
    if not UseSecond {command processor} then begin
      {Command is assumed to be a full pathname for a program}
      BlankPos := Pos(' ', Command);
      if BlankPos = 0 then
        PathName := Command
      else begin
        CommandTail := Copy(Command, BlankPos, Length(Command));
        PathName := Copy(Command, 1, Pred(BlankPos));
      end;
    end
    else begin
      {Pathname is the full pathname for COMMAND.COM}
      PathName := GetEnvironmentString('COMSPEC');

      {if Command is empty, we're doing a shell}
      if Command <> '' then
        {we're asking COMMAND.COM to execute the command}
        CommandTail := '/C '+Command;
    end;

    {Let user routine store and clear the physical screen}
    if UserRoutine <> nil then
      CallUserRoutine(ExecSaveScreen, 0);

    {get current DTA}
    GetDta(SaveDta);

    {swap interrupt vectors}
    SwapVectors;

    {Call Turbo's EXEC function}
    Exec(PathName, CommandTail);

    {swap interrupt vectors}
    SwapVectors;

    {restore DTA}
    SetDta(SaveDta);

    {if not in shell, let user routine allow time to see result}
    if ((Command <> '') or (DosError <> 0)) and (UserRoutine <> nil) then
      CallUserRoutine(ExecPauseAfterRun, 0);

    {give user routine a chance to restore the screen}
    if UserRoutine <> nil then
      CallUserRoutine(ExecRestoreScreen, 0);

    {If we get to here, our function result is in DosError}
    if DosError = $4B00 then  {!!.20}
      DosError := 0;          {!!.20}
    ExecDos := DosError;
  end;

{$ELSE}
{$IFDEF Heap6} {!!.11} {New version of ExecDos follows}

  function ExecDos(Command : string; UseSecond : Boolean; UserRoutine : Pointer) : Integer;
    {-Execute any DOS command. Call with Command = '' for a new shell. If
      UseSecond is false, Command must be the full pathname of a program to be
      executed}

    procedure CallUserRoutine(ActionCode : ActionCodeType; Param : Word);
      {-Call UserRoutine with an action code}
    inline(
      $FF/$5E/<UserRoutine); {call far dword ptr [bp+<UserRoutine]}

  var
    {Variables for saving and restoring state of system}
    OurInt23 : Pointer;
    OurInt24 : Pointer;
    SaveDta : Pointer;

    {Variables for managing the heap compression}
    ParasWeHave : Word;
    ParasForDos : Word;
    ParasToKeep : Word;
    OldHeapEnd : Pointer;

    {Variables for parsing the command line}
    BlankPos : Word;
    PathName : string[127];
    CommandTail : string[127];
  begin
    {Current DOS memory allocation read from memory control block}
    ParasWeHave := MemW[Pred(PrefixSeg):3];

    {Calculate amount of memory to give up}
    ParasForDos := Pred(PtrDiff(HeapEnd, HeapPtr) shr 4);

    {Calculate amount of memory to keep while in shell}
    ParasToKeep := ParasWeHave-ParasForDos;

    {See if enough memory to run DOS}
    if (ParasForDos > 0) and (ParasForDos < (MinSpaceForDos shr 4)) then begin
      ExecDos := -4;
      Exit;
    end;

    {Deallocate memory for DOS}
    if not SetBlock(ParasToKeep) then begin
      ExecDos := -2;
      Exit;
    end;

    {Save old end of heap and set new one}
    OldHeapEnd := HeapEnd;
    HeapEnd := HeapPtr;

    {get parameters for Execute}
    if Command = '' then
      UseSecond := True;
    CommandTail := '';
    if not UseSecond {command processor} then begin
      {Command is assumed to be a full pathname for a program}
      BlankPos := Pos(' ', Command);
      if BlankPos = 0 then
        PathName := Command
      else begin
        CommandTail := Copy(Command, BlankPos, Length(Command));
        PathName := Copy(Command, 1, Pred(BlankPos));
      end;
    end
    else begin
      {Pathname is the full pathname for COMMAND.COM}
      PathName := GetEnvironmentString('COMSPEC');

      {if Command is empty, we're doing a shell}
      if Command <> '' then
        {we're asking COMMAND.COM to execute the command}
        CommandTail := '/C '+Command;
    end;

    {Let user routine store and clear the physical screen}
    if UserRoutine <> nil then
      CallUserRoutine(ExecSaveScreen, 0);

    {let user routine show status info if entering DOS shell}
    if (Command = '') and (UserRoutine <> nil) then
      {Pass user routine the approximate memory available in KB}
      CallUserRoutine(ExecShowMemory, (ParasForDos-240) shr 6);

    {get current DTA}
    GetDta(SaveDta);

    {swap interrupt vectors}
    SwapVectors;

    {Call Turbo's EXEC function}
    Exec(PathName, CommandTail);

    {swap interrupt vectors}
    SwapVectors;

    {restore DTA}
    SetDta(SaveDta);

    {Reallocate memory from DOS}
    if not SetBlock(ParasWeHave) then begin
      ExecDos := -3;
      Exit;
    end;

    {Put heap end back where it was}
    HeapEnd := OldHeapEnd;

    {if not in shell, let user routine allow time to see result}
    if ((Command <> '') or (DosError <> 0)) and (UserRoutine <> nil) then
      CallUserRoutine(ExecPauseAfterRun, 0);

    {give user routine a chance to restore the screen}
    if UserRoutine <> nil then
      CallUserRoutine(ExecRestoreScreen, 0);

    {If we get to here, our function result is in DosError}
    ExecDos := DosError;
  end;

{$ELSE}

  function EndOfHeap : Pointer;
    {-Returns a pointer to the end of the free list}
  var
    FreeSegOfs : SegOfs absolute FreePtr;
  begin
    if FreeSegOfs.O = 0 then
      {the free list is empty, add $1000 to the segment}
      EndOfHeap := Ptr(FreeSegOfs.S+$1000, 0)
    else
      EndOfHeap := Ptr(FreeSegOfs.S+(FreeSegOfs.O shr 4), 0);
  end;

  function ExecDos(Command : string; UseSecond : Boolean; UserRoutine : Pointer) : Integer;
    {-Execute any DOS command. Call with Command = '' for a new shell. If
      UseSecond is false, Command must be the full pathname of a program to be
      executed. UserRoutine is the address of a routine to display status,
      save/restore the screen, etc., or a nil pointer.}

    procedure CallUserRoutine(ActionCode : ActionCodeType; Param : Word);
      {-Call UserRoutine with an action code}
    inline(
      $FF/$5E/<UserRoutine); {call far dword ptr [bp+<UserRoutine]}

  label
    ExitPoint;
  var
    PathName,
    CommandTail : string[127];
    OurInt23,
    OurInt24,
    OldEndOfHeap,
    NewEndOfHeap,
    TopOfHeap : Pointer;
    BlankPos,
    Allocated,
    SizeOfFreeList,
    ParasToKeep,
    ParasWeHave,
    ParasForDos : Word;
    SaveDta : Pointer;  {!!.10}
    {$IFDEF Ver40}
    UsingEmulator : Boolean;
    {$ENDIF}
  begin
    {Calculate number of bytes to save}
    TopOfHeap := Ptr(SegOfs(FreePtr).S+$1000, 0);
    SizeOfFreeList := PtrDiff(TopOfHeap, EndOfHeap);

    {get current DTA}    {!!.10}
    GetDta(SaveDta);     {!!.10}

    {If enough space available, use stack to store the free list}
    {$IFDEF Ver40}
    UsingEmulator := False;
    {$ENDIF}
    if (not UsingEmulator) and
       (LongInt(SizeOfFreeList)+StackSafetyMargin < LongInt(SPtr)) then begin
      NewEndOfHeap := Ptr(SSeg, 0);
      Allocated := 0;
    end
    else begin
      {Check for sufficient memory}
      if MaxAvail < LongInt(SizeOfFreeList) then begin
        {Insufficient memory to store free list}
        ExecDos := -1;
        Exit;
      end;

      {Allocate memory for a copy of free list}
      Allocated := SizeOfFreeList;
      if Allocated > 0 then
        GetMem(NewEndOfHeap, Allocated);

      {Recalculate the size of the free list}
      SizeOfFreeList := Word(PtrDiff(TopOfHeap, EndOfHeap));
    end;

    {Save the current pointer to the end of the free list}
    OldEndOfHeap := EndOfHeap;

    {Current DOS memory allocation read from memory control block}
    ParasWeHave := MemW[Pred(PrefixSeg):3];

    {Calculate amount of memory to give up}
    ParasForDos := Pred(PtrDiff(TopOfHeap, HeapPtr) shr 4);

    {Calculate amount of memory to keep while in shell}
    ParasToKeep := ParasWeHave-ParasForDos;

    {See if enough memory to run DOS}
    if (ParasForDos > 0) and (ParasForDos < (MinSpaceForDos shr 4)) then begin
      ExecDos := -4;
      goto ExitPoint;
    end;

    {Deallocate memory for DOS}
    if not SetBlock(ParasToKeep) then begin
      ExecDos := -2;
      goto ExitPoint;
    end;

    {get parameters for Execute}
    if Command = '' then
      UseSecond := True;
    if not UseSecond {command processor} then begin
      {Command is assumed to be a full pathname for a program}
      BlankPos := Pos(' ', Command);
      if BlankPos = 0 then begin
        PathName := Command;
        CommandTail := '';
      end
      else begin
        CommandTail := Copy(Command, BlankPos, Length(Command));
        PathName := Copy(Command, 1, Pred(BlankPos));
      end;
    end
    else begin
      {Pathname is the full pathname for COMMAND.COM}
      PathName := GetEnvironmentString('COMSPEC');

      {if Command is empty, we're doing a shell}
      if Command = '' then
        CommandTail := ''
      else
        {we're asking COMMAND.COM to execute the command}
        CommandTail := '/C '+Command;
    end;

    {Let user routine store and clear the physical screen}
    if UserRoutine <> nil then
      CallUserRoutine(ExecSaveScreen, 0);

    {let user routine show status info if entering DOS shell}
    if (Command = '') and (UserRoutine <> nil) then
      {Pass user routine the approximate memory available in KB}
      CallUserRoutine(ExecShowMemory, (ParasForDos-240) shr 6);

    {Copy the free list to a safe location}
    Move(OldEndOfHeap^, NewEndOfHeap^, SizeOfFreeList);

    {$IFDEF Ver40}
      {save our INT 23 and 24 vectors and put old ones back}
      GetIntVec($23, OurInt23);
      GetIntVec($24, OurInt24);
      SetIntVec($23, SaveInt23);
      SetIntVec($24, SaveInt24);
      {$ELSE}
      SwapVectors;
    {$ENDIF}

    {Call Turbo's EXEC function}
    Exec(PathName, CommandTail);

    {$IFDEF Ver40}
      {restore our INT 23 and 24 vectors}
      SetIntVec($23, OurInt23);
      SetIntVec($24, OurInt24);
    {$ELSE}
      SwapVectors;
    {$ENDIF}

    {restore DTA}        {!!.10}
    SetDta(SaveDta);     {!!.10}

    {Reallocate memory from DOS}
    if not SetBlock(ParasWeHave) then begin
      ExecDos := -3;
      goto ExitPoint;
    end;

    {Put free list back where it was}
    Move(NewEndOfHeap^, OldEndOfHeap^, SizeOfFreeList);

    {if not in shell , let user routine allow time to see result}
    if ((Command <> '') or (DosError <> 0)) and (UserRoutine <> nil) then
      CallUserRoutine(ExecPauseAfterRun, 0);

    {give user routine a chance to restore the screen}
    if UserRoutine <> nil then
      CallUserRoutine(ExecRestoreScreen, 0);

    {If we get to here, our function result is in DosError}
    ExecDos := DosError;

ExitPoint:
    {Deallocate any dynamic memory used}
    if Allocated <> 0 then
      FreeMem(NewEndOfHeap, Allocated);
  end;

{$ENDIF}
{$ENDIF}

  function UserDefinedIoResult : Word;
    {-Calls user-defined I/O checking routine}
  inline(
    $FF/$1E/>IoResultPtr);   {CALL DWORD PTR [IoResultPtr]}

  function IoResult : Word;
    {-Returns I/O result if IoResultPtr is nil, else the code returned by
      the user-specified I/O error checking routine.}
  begin
    if IoResultPtr = nil then
      IoResult := System.IoResult
    else
      IoResult := UserDefinedIoResult;
  end;

  function DosBlockWrite(H : Word; var Src; N : Word) : Word;
    {-Calls DOS's BlockWrite routine. Returns 0 if successful, else the DOS
      error code.}
  begin
    with Regs do begin
      AH := $40;             {write to file}
      BX := H;               {file handle}
      CX := N;               {Number of bytes to write}
      DS := Seg(Src);        {DS:DX points to buffer}
      DX := Ofs(Src);
      MsDos(Regs);           {returns bytes written in AX}

      {check carry flag, also the number of bytes written}
      if Odd(Flags) or (AX <> N) then
        DosBlockWrite := AX
      else
        DosBlockWrite := 0;
    end;
  end;

  function TextSeek(var F : Text; Target : LongInt) : Boolean;
    {-Do a Seek for a text file opened for input. Returns False in case of I/O
      error.}
  var
    T : LongRec absolute Target;
    Pos : LongInt;
  begin
    with Regs, FIB(F) do begin
      {assume failure}
      TextSeek := False;

      {check for file opened for input}
      if Mode <> FMInput then
        Exit;

      {get current position of the file pointer}
      AX := $4201;           {move file pointer function}
      BX := Handle;          {file handle}
      CX := 0;               {if CX and DX are both 0, call returns the..}
      DX := 0;               {current file pointer in DX:AX}
      MsDos(Regs);

      {check for I/O error}
      if Odd(Flags) then
        Exit;

      {calculate current position for the start of the buffer}
      LongRec(Pos).HighWord := DX;
      LongRec(Pos).LowWord := AX;
      Dec(Pos, BufEnd);

      {see if the Target is within the buffer}
      Pos := Target-Pos;
      if (Pos >= 0) and (Pos < BufEnd) then
        {it is--just move the buffer pointer}
        BufPos := Pos
      else begin
        {have DOS seek to the Target-ed offset}
        AX := $4200;         {move file pointer function}
        BX := Handle;        {file handle}
        CX := T.HighWord;    {CX has high word of Target offset}
        DX := T.LowWord;     {DX has low word}
        MsDos(Regs);

        {check for I/O error}
        if Odd(Flags) then
          Exit;

        {tell Turbo its buffer is empty}
        BufEnd := 0;
        BufPos := 0;
      end;
    end;

    {if we get to here we succeeded}
    TextSeek := True;
  end;

  function TextFileSize(var F : Text) : LongInt;
    {-Return the size of text file F. Returns -1 in case of I/O error.}
  var
    OldHi, OldLow : Integer;
  begin
    with Regs, FIB(F) do begin
      {check for open file}
      if Mode = FMClosed then begin
        TextFileSize := -1;
        Exit;
      end;

      {get current position of the file pointer}
      AX := $4201;           {move file pointer function}
      BX := Handle;          {file handle}
      CX := 0;               {if CX and DX are both 0, call returns the..}
      DX := 0;               {current file pointer in DX:AX}
      MsDos(Regs);

      {check for I/O error}
      if Odd(Flags) then begin
        TextFileSize := -1;
        Exit;
      end;

      {save current position of the file pointer}
      OldHi := DX;
      OldLow := AX;

      {have DOS move to end-of-file}
      AX := $4202;           {move file pointer function}
      BX := Handle;          {file handle}
      CX := 0;               {if CX and DX are both 0, call returns the...}
      DX := 0;               {current file pointer in DX:AX}
      MsDos(Regs);           {call DOS}

      {check for I/O error}
      if Odd(Flags) then begin
        TextFileSize := -1;
        Exit;
      end;

      {calculate the size}
      TextFileSize := LongInt(DX) shl 16+AX;

      {reset the old position of the file pointer}
      AX := $4200;           {move file pointer function}
      BX := Handle;          {file handle}
      CX := OldHi;           {high word of old position}
      DX := OldLow;          {low word of old position}
      MsDos(Regs);           {call DOS}

      {check for I/O error}
      if Odd(Flags) then
        TextFileSize := -1;
    end;
  end;

  function TextPos(var F : Text) : LongInt;
    {-Return the current position of the logical file pointer (that is,
      the position of the physical file pointer, adjusted to account for
      buffering). Returns -1 in case of I/O error.}
  var
    Position : LongInt;
  begin
    with Regs, FIB(F) do begin
      {check for open file}
      if Mode = FMClosed then begin
        TextPos := -1;
        Exit;
      end;

      {get current position of the physical file pointer}
      AX := $4201;           {move file pointer function}
      BX := Handle;          {file handle}
      CX := 0;               {if CX and DX are both 0, call returns the...}
      DX := 0;               {current file pointer in DX:AX}
      MsDos(Regs);           {call DOS}

      {check for I/O error}
      if Odd(Flags) then begin
        TextPos := -1;
        Exit;
      end;

      {calculate the position of the logical file pointer}
      LongRec(Position).HighWord := DX;
      LongRec(Position).LowWord := AX;
      if Mode = FMOutput then
        {writing}
        Inc(Position, BufPos)
      else
        {reading}
        if BufEnd <> 0 then
          Dec(Position, BufEnd-BufPos);

      {return the calculated position}
      TextPos := Position;
    end;
  end;

  function TextFlush(var F : Text) : Boolean;
    {-Flush the buffer(s) for a text file. Returns False in case of I/O error.}
  var
    Position : LongInt;
    P : LongRec absolute Position;
    Code : Word;
  begin
    with Regs, FIB(F) do begin
      {assume failure}
      TextFlush := False;

      {check for open file}
      if Mode = FMClosed then
        Exit;

      {see if file is opened for reading or writing}
      if Mode = FMInput then begin
        {get current position of the logical file pointer}
        Position := TextPos(F);

        {exit in case of I/O error}
        if Position = -1 then
          Exit;

        {set the new position of the physical file pointer}
        AX := $4200;         {move file pointer function}
        BX := Handle;        {file handle}
        CX := P.HighWord;    {CX has high word of offset}
        DX := P.LowWord;     {DX has low word}
        MsDos(Regs);         {call DOS}

        {check for I/O error}
        if Odd(Flags) then
          Exit;
      end
      else begin
        {write the current contents of the buffer, if any}
        if BufPos <> 0 then begin
          Code := DosBlockWrite(Handle, BufPtr^, BufPos);
          if Code <> 0 then
            Exit;
        end;

        {dupe the file handle}
        AH := $45;
        BX := Handle;
        MsDos(Regs);
        if Odd(Flags) then
          Exit;

        {close the duped file}
        BX := AX;
        AH := $3E;
        MsDos(Regs);
        if Odd(Flags) then
          Exit;
      end;

      {tell Turbo its buffer is empty}
      BufEnd := 0;
      BufPos := 0;
    end;

    {if we get to here we succeeded}
    TextFlush := True;
  end;

  function OpenStdDev(var F : Text; StdHandle : Word) : Boolean;
    {-Assign the text file to the specified standard DOS device}
  begin
    OpenStdDev := False;
    case StdHandle of
      StdInHandle,
      StdOutHandle,
      StdErrHandle,
      StdPrnHandle :
        begin
          {Initialize the file variable}
          Assign(F, '');
          if StdHandle = StdInHandle then {!!.12}
            Reset(F)                      {!!.12}
          else                            {!!.12}
            Rewrite(F);                   {!!.12}
          if IoResult = 0 then begin
            FIB(F).Handle := StdHandle;
            if StdHandle = StdErrHandle then
              FIB(F).BufSize := 1;
            OpenStdDev := True;
          end;
        end;
    end;
  end;

  function HandleIsConsole(Handle : Word) : Boolean;
    {-Return true if handle is the console device (input or output)}
  begin
    with Regs do begin
      AX := $4400;
      BX := Handle;
      MsDos(Regs);
      if (DX and $80) = 0 then
        HandleIsConsole := False
      else
        HandleIsConsole := (DX and $02 <> 0) or (DX and $01 <> 0);
    end;
  end;

  procedure SetRawMode(var F : Text; On : Boolean);
    {-Set "raw" mode on or off for the specified text file (must be a device)}
  var
    FH : Word absolute F; {F's file handle}
    FMod : Word;
  begin
    {check for open file}
    FMod := FIB(F).Mode;
    if (FMod < FMInput) or (FMod > fmInOut) then begin
      {Turbo's file not found error code}
      DosError := 103;
      Exit;
    end;

    DosError := 0;
    with Regs do begin
      AX := $4400;           {Get device information}
      BX := FH;
      MsDos(Regs);           {returns device info in DX}

      if not Odd(Flags) then begin
        {check bit 7 for device flag}
        if DL and $80 = 0 then
          Exit;

        {clear unwanted bits}
        DX := DX and $00AF;

        {select raw/cooked mode}
        if On then
          {set bit 5 of DX}
          DL := DL or $20
        else
          {clear bit 5 of DX}
          DL := DL and $DF;

        AX := $4401;           {Set device information}
        BX := FH;              {BX has file handle}
        MsDos(Regs);
      end;

      if Odd(Flags) then
        DosError := AX
      else
        DosError := 0;
    end;
  end;

  function FileHandlesOpen(CountDevices : Boolean) : Byte; {!!.10}
    {-Return the number of open files owned by a program}
  type
    HandleTable = array[0..254] of Byte;
  var
    HandlesPtr : ^HandleTable;
    I, N, Max : Byte;
  begin
    {pointer to file handles table at PrefixSeg:$34}
    HandlesPtr := Pointer(MemL[PrefixSeg:$34]);

    {size of file handles table at PrefixSeg:$32}
    Max := Mem[PrefixSeg:$0032]-1;

    N := 0;
    for I := 0 to Max do
      if HandlesPtr^[I] <> $FF then
        case I of
          0..4 : Inc(N, Ord(CountDevices));
          else   Inc(N);
        end;

    FileHandlesOpen := N;
  end;

  function FileHandlesLeft : Byte;    {!!.10} {revised} {!!.12}
    {-Return the number of available file handles}
  const
    NullName : array[1..4] of Char = 'NUL'#0;
    MaxHandles = 255;
  var
    Handles : array[1..MaxHandles] of Word;
    N : Byte;
  begin
    inline(
      $30/$ED/               {xor ch,ch            ;CX = MaxHandles}
      $B1/<MaxHandles/       {mov cl,<MaxHandles}
      $BA/>NullName/         {mov dx,>NullName     ;DS:DX => NullName}
      $8D/$B6/>Handles/      {lea si,[bp+>Handles] ;SI has offset for Handles[1]}
                             {Next:}
      $B8/$02/$3D/           {mov ax,$3D02         ;DOS open file function}
      $CD/$21/               {int $21              ;call DOS}
      $72/$07/               {jc Close             ;start closing if CF set}
      $36/                   {ss:}
      $89/$04/               {mov [si],ax          ;save the Handle}
      $46/                   {inc si               ;inc pointer into Handles}
      $46/                   {inc si}
      $E2/$F2/               {loop Next            ;repeat if CX > 0}
                             {Close:}
      $F6/$D1/               {not cl               ;flip bits in cl}
      $88/$8E/>N/            {mov [bp+>N],cl       ;save handle count in N}
      $E3/$0C/               {jcxz Done            ;done if count is 0}
                             {CloseOne:}
      $4E/                   {dec si               ;dec pointer into Handles}
      $4E/                   {dec si}
      $36/                   {ss:}
      $8B/$1C/               {mov bx,[si]          ;get the handle into BX}
      $B8/$00/$3E/           {mov ax,$3E00         ;DOS close file function}
      $CD/$21/               {int $21              ;call DOS, ignore error}
      $E2/$F4);              {loop CloseOne        ;do it again}
                             {Done:}

    FileHandlesLeft := N;
  end;

  function ExistFile(FName : string) : Boolean;
    {-Return true if file is found}
  var
    Regs : Registers;
    FLen : Byte absolute FName;
  begin
    {check for empty string}
    if Length(FName) = 0 then
      ExistFile := False
    else with Regs do begin
      if IoResult = 0 then ; {clear IoResult} {!!.11}
      Inc(FLen);
      FName[FLen] := #0;
      AX := $4300;           {get file attribute}
      DS := Seg(FName);
      DX := Ofs(FName[1]);
      MsDos(Regs);
      ExistFile := (not Odd(Flags)) and (IoResult = 0) and  {!!.08}
                   (CX and (VolumeID+Directory) = 0);       {!!.08}
    end;
  end;

  function ExistOnPath(FName : string; var FullName : string) : Boolean;
   {-Return true if fname is found in
      a) current directory (returns just name, no path)
      b) program's directory (DOS 3.X only)
      c) any DOS path directory
    and return path name to file}
  type
    Environment = array[0..32766] of Char;
  const
    Null : Char = #0;
    DoubleNull : string[2] = #0#0;
    PathStr : string[5] = 'PATH=';
  var
    E : ^Environment;
    Elast : Word;
    Epos : Word;
    Fpos : Word;
    Found : Boolean;
  begin
    {string empty?}                 {!!.09}
    if Length(FName) = 0 then begin {!!.09}
      ExistOnPath := False;         {!!.09}
      Exit;                         {!!.09}
    end;                            {!!.09}

    {Assume success}
    ExistOnPath := True;

    {Check current directory -- If you need the complete path name,
     call TPString.FullPathname after calling ExistOnPath}
    FullName := FName;
    if ExistFile(FullName) then
      Exit;

    {Get a pointer to the DOS environment}
    E := Ptr(MemW[PrefixSeg:$2C], 0);

    {Find the end of the environment}
    Elast := Search(E^[0], 32767, DoubleNull[1], 2);
    if Elast = $FFFF then begin
      {Something is wrong}
      ExistOnPath := False;
      Exit;
    end;

    {If DOS 3 or higher, check the directory where the program was found}
    if DOSVersion >= $300 then begin
      {Skip over the doublenull and a word count}
      Epos := Elast+4;
      {Find the next null}
      Fpos := Search(E^[Epos], 100, Null, 1);
      if Fpos <> $FFFF then begin
        {Move from the environment into the return string}
        FullName[0] := Chr(Fpos);
        Move(E^[Epos], FullName[1], Fpos);
        FullName := AddBackSlash(JustPathname(FullName))+FName;
        if ExistFile(FullName) then
          Exit;
      end;
    end;

    {Check the path}
    Found := False;
    Epos := 0;
    repeat
      Fpos := Search(E^[Epos], Elast-Epos, PathStr[1], Length(PathStr));
      if Fpos <> $FFFF then begin
        {PATH= was found}
        Inc(Epos, Fpos);
        Found := (Epos = 0) or (E^[Pred(Epos)] = Null);
        if not(Found) then
          {Something like DPATH= was found}
          Inc(Epos);
      end;
    until (Fpos = $FFFF) or Found;

    if Found then begin
      {True PATH= was found, skip over the PATH= part}
      Inc(Epos, Length(PathStr));

      {Scan each item in the path}
      repeat

        {Find the termination of the current path entry}
        Fpos := Epos;
        while (E^[Fpos] <> ';') and (E^[Fpos] <> Null) do
          Inc(Fpos);

        if Fpos > Epos then begin
          {A path entry found}
          FullName[0] := Char(Fpos-Epos);
          Move(E^[Epos], FullName[1], Fpos-Epos);
          FullName := AddBackSlash(FullName)+FName;
          if ExistFile(FullName) then
            Exit;
        end;

        {Prepare to look at next item}
        Epos := Succ(Fpos);

      until E^[Fpos] = Null;
    end;

    {Not found, even on the path}
    ExistOnPath := False;
    FullName := FName;
  end;

  function TimeMs : LongInt;
    {-Return time of day in milliseconds since midnight}
  begin
    with Regs do begin
      AH := $2C;
      MsDos(Regs);
      TimeMs := 1000*(LongInt(DH)+60*(LongInt(CL)+60*LongInt(CH)))+10*LongInt(DL);
    end;
  end;

begin
  {No user-defined ioresult routine yet}
  IoResultPtr := nil;
end.
