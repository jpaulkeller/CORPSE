*
* NPC.Cmd: creates a minimally fleshed-out non-player character,
*          based on the generated or DM-entered character name.

* This file is designed to be used when multiple NPCs are generated.
* It provides only the most basic of information.
* For more detailed NPCs, use the NPC_Full (or NPC_Flex) scripts.

* Generate a random name.
* Ask the user for a name, using the random one as a default.
* Assign the entered name to "NPC" for re-use later.
* Randomize using the name as the seed, for consistent results.
# {NPC={NPC Name?{!Name}}}

* Show the name, generate and show a race and previous profession.
{NPC} {race} {job}

* Generate and show a gender and two personality traits.
  {Gender@Pronoun} is {degree} {trait} and {degree} {trait}.

* Generate and show some pocket stuff (with 2% chance of gem)
  Currency: {0-100} sp; {0-30} cp; {{100}<3?{gem}}
  Carrying: {TinyItem}; {TinyItem}; {TinyItem}

* Generate and show some equipment.
  Equipped with: {equipment}; {equipment}; {equipment}

* Include the script to generate and show the NPC's attributes.
{Attribute.Cmd}
