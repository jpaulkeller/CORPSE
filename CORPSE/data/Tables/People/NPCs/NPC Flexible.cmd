* NPC_Flex.Cmd: creates a non-player character, by querying for all
* significant variables.
*
* This file is designed to be used when the DM wishes to generate
* an NPC of some specific known type.
* For simple random NPCs, use the NPC script.

* Generate a random name.
* Ask the user for a name, using the random one as a default.
* Assign the entered name to "NPC" for re-use later.
* Randomize using the name as the seed, for consistent results.
# {NPC={NPC Name?{Name}}}

* Show the name, generate a show a race and previous profession.
{NPC} {Race??} {Profession??}

* Generate and show a gender and two personality traits.
  {Gender?{Gender.Pronoun}} is {Trait??} and {Trait??}

* Generate and show some pocket stuff (with 2% chance of gem)
  Currency: {Silver?{0-100}} sp{2%? and a {Gem}}
  Carrying: {TinyItem??}; {TinyItem??}; {TinyItem??}

* Generate and show some equipment.
  Equipped with: {Equipment??}, {Equipment??}, {Equipment??}

* Include the script to generate and show the NPC's attributes.
{Attribute.Cmd}
