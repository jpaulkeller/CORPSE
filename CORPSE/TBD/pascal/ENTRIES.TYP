
  (* File: Entries.Typ *)

  (* QUANTITY --------------------------------------- *)
  (*   (1) nil                                        *)
  (*   (2) a single number:       #                   *)
  (*   (3) a range of numbers:    # - #               *)
  (*   (4) a roll specification:  # #                 *)
  (*   (5) a query-user command:  ?{"prompt:default"} *)
  (*                                                  *)
  (*   A user response can be any of (2) through (4)  *)
  (* ------------------------------------------------ *)

  (* FIELD-SPECIFICATION ------------------------- *)
  (*   (1) a field name:   string                  *)
  (*   (2) a field region: start# width#           *)
  (* --------------------------------------------- *)

  (* CRITERIA ------------------------------------------------- *)
  (*   (1) null:                  0      = 1 to N (whole file)  *)
  (*   (2) a range of numbers:    # - #  = Roll between # and # *)
  (*   (3) a roll specification:  # #    = Roll # d # for line  *)
  (*   (4) a pattern and field:   pattern FIELD-SPECIFICATION   *)
  (* ---------------------------------------------------------- *)

  (* STRUCTURE ----------------- *)
  (*   (1) null                  *)
  (*   (2) a Field-Specification *)
  (* --------------------------- *)

const

  (* -------------------------------------------------------------------- *)
  (* In TABLE files, a line may be one of three types: blank, a normal    *)
  (* entry, or a special line.  Special lines are identified by one of    *)
  (* the following characters, which must appear in the first column:     *)
  (*                                                                      *)
  (* : (colon): indicates the rest of the line is a FIELD definition      *)
  (* | (pipe) : indicates the rest of the line is a MACRO definition      *)
  (*                                                                      *)
  (* Field and macro definitions are parsed during start-up, but these    *)
  (* lines are otherwise ignored.                                         *)
  (*                                                                      *)
  (* ! (exclamation point): indicates a header line displayed at the top  *)
  (*                                                                      *)
  (* * (asterisk): indicates this is a comment line, never displayed      *)
  (* ? (question mark): a "source" line,   displayed in a different color *)
  (* . (period):        a "sub-header",    displayed in a different color *)
  (* ] (right bracket): a "footer",        displayed in a different color *)
  (* # (pound sign):    a "position" line, displayed in a different color *)
  (* -------------------------------------------------------------------- *)

  FieldMark     = ':';  MacroMark     = '|';
  HeaderMark    = '!';  SubHeaderMark = '.';  FooterMark    = ']';
  SourceMark    = '?';  PositionMark  = '#';  CommentMark   = '*';

  (* TOKENS ------------------------------------------------------------- *)
  (*                                                                      *)
  (* Tokens are essentially embedded macros, delineated by braces.        *)
  (* There are four types of tokens: table-lookups, numeric, conditional, *)
  (* and special commands.                                                *)
  (* Tokens are evaluated, and then this value is used in place of the    *)
  (* token.  By default, this replacement is variable width, but there    *)
  (* ways to do fixed-width tokens also.  Tokens are much like #DEFINES.  *)
  (*                                                                      ()
  (* TABLE-LOOKUP tokens are the most common, the most powerful, and the  *)
  (* easiest to use.  A table-lookup token is just the name of the table, *)
  (* and it is replaced with an entry from the table.  The entry may be   *)
  (* "structured" (i.e., a specific part of the entry may be used instead *)
  (* of the entire entry line).                                           *)
  (*                                                                      *)
  (*   (1) a table name:      {TABLE}                                     *)
  (*   (2) a table and field: {TABLE:FIELD}                               *)
  (*   (3) a table and macro: {TABLE|MACRO}                               *)
  (*                                                                      *)
  (* NUMERIC tokens are "roll-specifications" which means they evaluate   *)
  (* to a random number.  The following specification formats are used:   *)
  (*   (1) a single number:       {#}      = Roll between 1 and #         *)
  (*   (2) a range of numbers:    {# - #}  = Roll between # and #         *)
  (*   (3) a roll specification:  {# #}    = Roll # #-sided dice (# d #)  *)
  (*                                                                      *)
  (* CONDITIONAL tokens are similar to numeric, in that a random number   *)
  (* is generated, but they evaluate to (possibly blank) text.            *)
  (*   (1) all-or-nothing format: {?2=2:ALL}                              *)
  (*   (2) either/or format:      {?2=2:YES:NO}                           *)
  (* Note that a comparison operator (< or >) can be used in place of the *)
  (* the = sign.  For example:    {?10>7:RARE:COMMON}                     *)
  (*                                                                      *)
  (* SPECIAL COMMAND tokens are implemented in code, and evaluate to text.*)
  (* An exclamation point indicates this type of token, and the first     *)
  (* character following that determines the type of special command:     *)
  (*   (1) random name: {!N} generates a random name-like text            *)
  (*                                                                      *)
  (* -------------------------------------------------------------------- *)

  (* -------------------------------------------------------------------- *)
  (* Special control-characters used for tokens:                          *)
  (*                                                                      *)
  (* { and } are used to delineate tokens, anywhere in an entry           *)
  (* = indicates a fixed width token (padded or truncated as needed)      *)
  (* -------------------------------------------------------------------- *)

  TokenL        = '{';  TokenR        = '}';
  DefaultL      = '(';  DefaultR      = ')';

  EqualsMark    = '=';
  Assignment    = '=';
  Conditional   = '?';  CondSepr      = ':';
  CommandMark   = '!';
  NameMark      = 'N';

  (* -------------------------------------------------------------------- *)
  (* Special characters used for displays:                                *)
  (*                                                                      *)
  (* ¯ indicates the resolved entry has been truncated (i.e., more data)  *)
  (*  and  are used to delineate warning and error text (bad tokens)    *)
  (* -------------------------------------------------------------------- *)

  Truncation    = '¯';
  ErrorL        = '';  ErrorR        = '';

  TempMark      = '~';    (* used as part of the name of a temporary file *)

  (* -------------------------------------------------------------------- *)
  (* In SCRIPT files, a line may be either a normal text line (with or    *)
  (* without embedded tokens), or a special command line.  Command lines  *)
  (* are identified by one of the following characters, which must appear *)
  (* in the first column:                                                 *)
  (*                                                                      *)
  (* * (asterisk)     : indicates this is a comment line, never displayed *)
  (* # (pound sign)   : # [seed] (randomize using the optional seed)      *)
  (* = (equals sign)  : = VARIABLE=VALUE (store value for reuse)          *)
  (* ? (question mark):                                                   *)
  (* -------------------------------------------------------------------- *)

  RandomSeed    = '#';
  IncludeMark   = '+';
  Question      = '?';

type

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

  CriteriaTypes  = ( crNull, crDice, crIndex, crRange, crSearch );

  DiceRolls      = record
                     Qty   : integer; (* # of dice to roll  *)
                     Sides : integer; (* # of sides per die *)
                   end;

  Ranges         = record
                     Lo, Hi : integer;
                   end;

  Structures     = record
                     Start : integer;
                     Width : integer;
                   end;

  SearchInfo     = record
                     Pattern : string;
                     Field   : Structures;
                   end;

  Criteria       = record
                     case CriteriaType : CriteriaTypes of
                       crDice   : ( Dice   : DiceRolls  );  (*  4 *)
                       crIndex  : ( Index  : integer    );  (*  2 *)
                       crRange  : ( Range  : Ranges     );  (*  4 *)
                       crSearch : ( Search : SearchInfo );  (* 30 *)
                   end;

  Masks          = string [20];

  Fields         = ^FieldRecs;
  FieldRecs      = record
                     Name  : string [16];
                     Value : Structures;    (* start# and width# *)
                     Next  : Fields;
                   end;

  Macros         = ^MacroRecs;
  MacroRecs      = record
                     Name  : string [16];
                     Value : string [30];   (* Criterion {Structure} *)
                     Next  : Macros;
                   end;

  Tables         = record
                     Name    : string[80];  (* file path and name     *)
                     Entries : integer;     (* # of entries in file   *)
                     Lines   : integer;     (* # of lines in file     *)
                     Width   : integer;     (* widest unresolved line *)
                     Field   : Fields;      (* head of field list     *)
                     Macro   : Macros;      (* head of macro list     *)
                   end;

  (* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *)

