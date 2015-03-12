<html>
<body>

/ Generates a potion, with lots of details, based on the potion type.

/ Generate a random potion.
/ Ask the user for a potion, using the random one as a default.
/ Assign the entered potion to "PotionName" for re-use later.

<h1>POTION: {PotionName={Potion Type?{Potion}}}</h1>
<hr>
<p>Container: {container:potions}, holding {2d4} doses
 
/ Randomize using the potion as the seed, for consistent results for color, smell, etc:
# {PotionName}

<h2>Description</h2>
<ul>
 <li>Color: {color}
 <li>Viscosity: {viscosity}{33%, {liquid feature}}{20%, {liquid feature:extra}}
 <li>Smell: {smell}
 <li>Taste: {taste:description}
 <li>Temperature: {20%?{temperature}:ambient}
</ul>
 
<h2>Effects</h2>
<ul>
 <li>Duration (if applicable): {2d10} {duration:brief}s
 <li>Side Effects: {33%?{mild|severe} {symptom} which lasts for {2d3} {duration:moderate}s:none}
</ul>

<h2>Ingredients</h2>
<ul>
!loop {3,7}
 <li>{reagent}
!loop end
</ul>

</body>
</html>

===

/ http://random-generator.com/index.php?title=Alchemical_Recipes

1,Dissolve [Powder] in water; boil the solution. Add [Liquid] until [Color] fumes cease to come off, and enough [Liquid] to render the solution clear. To this add a solution of [Powder] and [Liquid], as long as any precipitate is produced. Wash this precipitate thoroughly with water acidulated with [Acid], and dry in a warm place for a [TimePeriod].
1,Dissolve in a small quantity of hot [Liquid], [Dice.1d8] parts of [Ingredients]; in another part, boil [Dice.1d10] parts of [Ingredients] with [Dice.1d10] parts of [Ingredients], until it throws out no more [Acid]; mix by degrees this hot solution with the first, agitating continually until the effervescence has entirely ceased; these then form a precipitate of a dirty [Color], very abundant; add to it about [Dice.1d6] parts of [Acid], or such a quantity that there may be a slight excess perceptible to the smell after the mixture; by degrees the precipitate diminishes the bulk, and in a few hours there deposes spontaneously at the bottom of the liquor entirely discolored, a powder of a contexture slightly crystalline, and of a very beautiful [Color]; a [TimePeriod] afterwards the floating liquor is separated.
1,Dissolve the [Powder], cold, in [Acid], and produce a precipitation of it by means of [Powder], employed in such doses that it will be absorbed by the acid, in order that the precipitate may be pure, that is, without any mixture. When the liquor has been decanted, wash the precipitate and spread it out on a piece of linen cloth to drain. If a portion of this precipitate, which is [Color], be placed on a grinding-stone, and if a little [Powder] be added, the color will be immediately changed into a beautiful [Color]. When the whole matter acquires the consistency of paste, desiccation will take place in about a [TimePeriod].
1,Mix [Dice.1d6] parts of [Acid] and 1 part of [Ingredients], heat to about redness. Gas and water are given off. The resulting salt when thrown into [Liquid] is decomposed. The precipitate is collected and washed. This is a remarkably fine color of [Color], solid and brilliant even by artificial light. It must be cured by leaving it in sunlight for at least one [TimePeriod] before it can be used.
1,Put into a crucible surrounded by burning coals, fragments of [Ingredients], with an equal amount of [MonsterParts.DryGoods], and cover it closely. When no more smoke is seen to pass through the joining of the cover, leave the crucible over the fire for half a [TimePeriod] or longer, or until it has completely cooled. There will then be found in it a hard carbonaceous matter, which, when pounded and ground on porphyry with [Liquid], is washed on a filter with warm [Liquid] and then dried. Before it is used it must be dissolved again into [Liquid].
1,Separate the [Color] parts of [Powder], and reduce them, on a piece of porphyry, to an impalpable powder, which besprinkle with [Liquid], then make a paste with equal parts of [Ingredients], [Ingredients], and [Ingredients], say, [Dice.1d12] oz. of each; and add to this paste 1/2 oz. of [Liquid], [Dice.1d4] oz. of [Liquid], and as much more [Ingredients]. Then take [Dice.1d10] parts of this mixture, and [Dice.1d4] of [HardIngredients], ground with oil on a piece of porphyry, mix the whole warm, and suffer it to digest for a [TimePeriod], at the end of which knead the mixture thoroughly in warm [Liquid], till the [Color] part separates from it, and at the end of some [TimePeriod]s decant the liquor.
1,Start with [Dice.1d12] oz. of [Ingredients], [Dice.1d12] oz. of [Ingredients], [Dice.1d12] oz. of [Ingredients], and [Dice.1d12] oz. of [Ingredients]. Pulverize these ingredients, and having mixed them thoroughly, put them into a capsule or crucible of earth, and place over it a covering of the same substance. Expose it at first to a gentle heat, which must be gradually increased till the capsule is moderately red. The oxidation arising from this process requires, at least, [Dice.1d6] [TimePeriod]s’ exposure to heat before it is completed. The result of this calcination is then ground in [Liquid] on a porphyry slab with an ivory spatula, as iron alters the material. The paste is then dried and preserved for use. There is no necessity of adhering so strictly to the doses as to prevent their being varied.

