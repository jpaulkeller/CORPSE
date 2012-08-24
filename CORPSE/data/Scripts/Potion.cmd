* Generate a random potion.
* Ask the user for a potion, using the random one as a default.
* Assign the entered potion to "PotionName" for re-use later.
POTION: {PotionName={Potion Name?{Potion}}}

> Container: {Container:Liquid} ({2d4} doses)

* Randomize using the potion as the seed, for consistent results for color, smell, etc:
# {PotionName}

> Description: {Color} {Viscosity} liquid{{5}=5?, with bits of {Reagent} floating in it}
> Smell: {{3}=3?{SmellAdjective} }{Smell}
> Taste: {Taste}
