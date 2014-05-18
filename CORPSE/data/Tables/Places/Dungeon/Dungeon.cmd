
// Ask for the inn name (with a suitable default), and randomize based on that
// name so that the following data (until the next #) is consistent.
# {DUNGEON={Dungeon Name?*}}

Dungeon: {DUNGEON}
------------------------------------------------
.
Flavor:
------------------------------------------------
Rumor: {Dungeon Flavor:Rumor}
Overlord: {Dungeon Flavor:Overlord}
Entrance: {Dungeon Flavor:Entrance}
.
Level 1
{Dungeon Encounter:Level1}
{Dungeon Item:Level1}
{Dungeon Ambience:Sound}
.
Rooms:
------------------------------------------------
Floor: {Dungeon Room:Floor}
Ceiling: {Dungeon Room:Ceiling}
Door: {Dungeon Room:Unusual Door}
Empty: {Dungeon Room:Empty}
Larder: {Dungeon Room:Larder}
Torture Chamber: {Dungeon Room:Torture Chamber}
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
