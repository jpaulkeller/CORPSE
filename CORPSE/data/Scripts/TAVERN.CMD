* Ask for the inn name (with a suitable default), and randomize based on that
* name so that the following data (until the next #) is consistent.
# {INN={Tavern Name?{Inn-Name}}}

<<<<<<<<<< Welcome to The {INN} >>>>>>>>>>

Your innkeeper, {!Name}, is a {gender} {race},
who is {degree} {trait} and {degree} {trait}.
 
The following individuals are currently in the common room:
{{0 - 2} NPC.Cmd}

* Re-randomize (not based on INN name)
#
{{0 - 2} NPC.Cmd}
{{Roll for more NPCs?{0 - 4}} NPC.Cmd}

Chests contain:
  Gold  : {200} pieces
  Silver: {500} pieces
  Copper: {100-1000} pieces
