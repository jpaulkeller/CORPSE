{ TVSCRSAV.PAS }

{
Description:  This unit is designed to show how to interrupt the normal
              chain of Turbo Vision events. It clears the screen if an
              event has not occurred in a period of time (the default
              is 5 minutes and the maximum is 60 minutes.)

Author:       Jeff Schafer
Date:         04 February, 1991
Last Revised: 04 February, 1991
Application:  IBM PC and compatibles; Turbo Pascal 6.0
Published in: TUG Lines - Turbo User Group, PO Box 1510, Poulsbo, WA 98370

}

UNIT TVScrSav;

{--------------------}
     INTERFACE
{--------------------}

USES
 App,
 Drivers;

CONST
 MaxMinutes  = 60;
 DefaultTime =  5;

TYPE
 { A time record }
 TTime =
  RECORD
   Hours,
   Minutes,
   Seconds,
   Hundreths : WORD;
  END;  { TTime record }

 { A screen saver application object }
 TScrnApp =
  OBJECT (TApplication)
   CONSTRUCTOR Init;
   PROCEDURE HandleEvent (VAR Event : TEvent); VIRTUAL;
   PROCEDURE Idle; VIRTUAL;
   PROCEDURE SnoozeTimerOn (WaitTime : WORD);
   PROCEDURE SnoozeTimerOff;
   PROCEDURE GoToSleep;
   PROCEDURE DreamRoutine; VIRTUAL;
  PRIVATE
   SnoozeIsSet    : BOOLEAN;
   SnoozeInterval : WORD;
   SnoozeTime     : TTime;
   PROCEDURE ResetTimer;
  END;

{--------------------}
   IMPLEMENTATION
{--------------------}

USES
 Memory,
 Dos,
 Crt;

{--------------------}

CONSTRUCTOR TScrnApp.Init;
BEGIN
 TApplication.Init;
 SnoozeTimerOn (DefaultTime)
END; { TScrnApp.Init }

{+++++}

PROCEDURE TScrnApp.HandleEvent(VAR Event : TEvent);
BEGIN
 IF Event.What <> evNothing
 THEN IF SnoozeIsSet
      THEN ResetTimer;
 TApplication.HandleEvent (Event)
END; { TScrnApp.HandleEvent }

{+++++}

PROCEDURE TScrnApp.Idle;

VAR
 CurrentTime : TTime;

BEGIN
 TApplication.Idle;
 WITH CurrentTime DO GetTime(hours,minutes,seconds,hundreths);

 { Check if it's time to snooze }
 IF   (CurrentTime.Hours = SnoozeTime.Hours)
  AND (CurrentTime.Minutes = SnoozeTime.Minutes)
  AND (CurrentTime.Seconds >= SnoozeTime.Seconds)
 THEN GoToSleep
END; { TScrnApp.Idle }

{+++++}

PROCEDURE TScrnApp.SnoozeTimerOn (WaitTime : WORD);
BEGIN
 SnoozeIsSet := TRUE;
 IF WaitTime = 0
 THEN SnoozeInterval := DefaultTime
 ELSE IF WaitTime <= MaxMinutes
      THEN SnoozeInterval := WaitTime
      ELSE SnoozeInterval := MaxMinutes;
 ResetTimer
END; { TScrnApp.SnoozeTimerOn }

{+++++}

PROCEDURE TScrnApp.SnoozeTimerOff;
BEGIN
 { Set snooze flag off }
 SnoozeIsSet := FALSE
END; { TScrnApp.SnoozeTimerOff }

{+++++}

PROCEDURE TScrnApp.GoToSleep;
BEGIN

 { Turn off Turbo Vision handlers, clear the screen, and call
   sleep procedure }
 DoneSysError;
 DoneEvents;
 DoneVideo;
 DoneMemory;
 DreamRoutine;
 InitMemory;
 InitVideo;
 InitEvents;
 InitSysError;
 Redraw;
 ResetTimer
END; { TScrnApp.GoToSleep }

{+++++}

PROCEDURE TscrnApp.DreamRoutine;

CONST
 KeyMask : BYTE = 15;

VAR
 Regs    :  Registers;
 Wakeup  :  BOOLEAN;
 Dummy   :  CHAR;

BEGIN
 ClrScr;
 Wakeup := False;

 { Loop until either a key is pressed or the mouse is moved }
 WHILE NOT Wakeup DO
  BEGIN
  { Clear the DOS mouse movement since TV doesn't use this call }
   Regs.AX := $0B;
   Regs.CX := $00;
   Regs.DX := $00;
   Intr($33,Regs);

   { Check the state of the shift, alt, and ctrl keys }
   Regs.AH := $02;
   Intr($16,Regs);

   { On a key pressed, restore to TV }
   IF ((Regs.AL AND KeyMask) <> 0) OR KeyPressed
   THEN
    WakeUp := True
   ELSE
    BEGIN
    { Check for mouse movement since the start of sleeping }
     Regs.AX := $0B;
     Regs.CX := $00;
     Regs.DX := $00;
     Intr($33,Regs);

     { If the mouse moved, restore to TV }
     IF (Regs.CX <> 0) OR (Regs.DX <> 0) THEN WakeUp := TRUE
    END
  END { WHILE NOT Wakeup }
END; { TScrnApp.DreamRoutine }

{+++++}

PROCEDURE TScrnApp.ResetTimer;

VAR
 CurrentTime : TTime;

BEGIN
 { Get current time }
 WITH CurrentTime DO GetTime(hours,minutes,seconds,hundreths);

 { Add Wait time to current time to determine time to snooze }
 WITH SnoozeTime DO
  BEGIN
   Seconds := CurrentTime.Seconds;
   Minutes := CurrentTime.Minutes + SnoozeInterval;
   Hours   := (CurrentTime.Hours + (Minutes DIV 60)) MOD 24;
   Minutes := Minutes MOD 60;
  END; { WITH }
END; { TScrnApp.ResetTimer }

END. { UNIT TVScrSav }
