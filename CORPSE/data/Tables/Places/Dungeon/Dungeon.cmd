<html>
<body>

/ Query for name (using random default); assign and randomize
# {DUNGEON={Dungeon Name?*}}

<h1>{DUNGEON}</h1>

<ul>
  <li><b>Flavor</b>
  <ul>
    <li>Rumor: {Dungeon Rumor}
    <li>Overlord: {Dungeon Flavor:Overlord}
    <li>Entrance: {Dungeon Flavor:Entrance}
  </ul>
  
  <li><b>Level 1</b>
  <ul>
    <li>Ambiance: {Dungeon Ambience:Sound}
    <li>Encounter: {Dungeon Encounter:Level1}
    <li>Item: {Dungeon Item:Level1}
    <li>Floor: {Dungeon Room:Floor}
    <li>Ceiling: {Dungeon Room:Ceiling}
    <li>Door: {Dungeon Room:Unusual Door}
    <li>Empty: {Dungeon Room:Empty}
    <li>Larder: {Dungeon Room:Larder}
    <li>Torture Chamber: {Dungeon Room:Torture Chamber}
  </ul>

!loop {2}
<ul>
  <li>Linking Tunnel: {cave:tunnel}
  {50%<li>Tunnel Feature: {cave:tunnel feature}}
  <li>Shape: {cave:shape}
  <li>Size: {cave:size}
  <li>Light Source: {cave:light}
  {25%<li>Ceiling: {cave:ceiling}}
  {25%<li>Feature: {cave:feature}}
  {25%<li>Content: {cave:content}}
  <li>Exit(s): {cave:exit}{25%; {cave:exit}}{25%; {cave:exit}}
</ul>
!loop end

</ul>

</body>
</html>


.
Level 2
{Dungeon Encounter:Level2}
{Dungeon Item:Level2}
{Dungeon Ambience:Sound}
.
Level 3
{Dungeon Encounter:Level3}
{Dungeon Item:Level3}
{Dungeon Ambience:Sound}
.
Level 4
{Dungeon Encounter:Level4}
{Dungeon Item:Level4}
{Dungeon Ambience:Sound}
.
// Re-randomize (not based on DUNGEON name)
#
.
Details:
------------------------------------------------
Body: {Dungeon Detail:Corpse}
Clue: {Dungeon Detail:Clue}
Fluid: {Dungeon Detail:Fluid}
Fog: {Dungeon Detail:Fog}
Pool: {Dungeon Detail:Pool}
Statue: {Dungeon Detail:Statue}
.
Search Results:
------------------------------------------------
Sarcophagus: {Dungeon Search: Sarcophagus}
Unexpected: {Dungeon Search: Unexpected}
YDiscarded Treasure: {Dungeon Search: Discarded Treasure}
Gross: {Dungeon Search: Gross}
Secret Door: {Dungeon Search: Secret Door}
Pit: {Dungeon Search: Pit}
.
Encounters:
------------------------------------------------
Wandering: {Dungeon Encounter: Wandering}
Friendly: {Dungeon Encounter: Friendly}
Mystery: {Dungeon Encounter: Mystery}
Pet: {Dungeon Encounter: Pet}
Infestation: {Dungeon Encounter: Infestation}
No Longer Empty: {Dungeon Encounter: No Longer Empty}
Warned: {Dungeon Encounter: Warned}
Distracted: {Dungeon Encounter: Distracted}
Geniuses: {Dungeon Encounter: Geniuses}
Recruit: {Dungeon Encounter: Recruit}
Security: {Dungeon Encounter: Security} 
Missing: {Dungeon Encounter: Missing}
Panhandler: {Dungeon Encounter: Panhandler}
Relocation: {Dungeon Encounter: Relocation}
Tiny: {Dungeon Encounter: Tiny}
Fleeing: {Dungeon Encounter: Fleeing}
Symbiosis: {Dungeon Encounter: Symbiosis}
Guard: {Dungeon Encounter: Guard}
.
Plague:
------------------------------------------------
Pathogen: {Dungeon Plague: Pathogen}
Vector: {Dungeon Plague: Vector}
Cure: {Dungeon Plague: Cure}
------------------------------------------------
Trap: {Trap: Effect}
