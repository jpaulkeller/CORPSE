<html>
<body>

/ Ask the user for a name, using the random one as a default.
/ Assign the entered name to "N" for re-use later.
/ Randomize using the name as the seed, for consistent results.
# {N={Name?{Name}}}

<!--
{G:=gender}
{C:=color hex}
-->

<p><b>{N}</b>

<ul>
  <li>Gender: {G.gender}
  <li>Overall Impression: {appearance}
  <li>Age: {age}
  <li>Height: {short|average|tall}
  <li>Build: {build}
  <li>Complexion: {skin} 
  <li>Eyes: {eyes}
  <li>Hair: {hair}
!loop {2d2-2}
  <li>Marking: {markings}
!loop end
  <li>Wearing: <font color="{C.font}" bgcolor="{C.hex}">{C.name}-colored</font> {75%{condition:clothing} }{~{clothing}}
/ sample: 
</ul>

</body>
</html>

===

/ http://random-generator.com/index.php?title=Female_Appearance

===Subtables===
{{external|<li>[[Utility]] (LeftRight)<li>[[Tattoos]] (Malemain, Femalemain)<li>[[PiercingsMale]] (Start)<li>[[Piercings]] (Femalemain)}}

{{internal|Build; Deformities; DescriptionText; DistinguishingFeaturesChance; DistinguishingFeaturesFemale; DistinguishingFeaturesMale; ExpressionAdjective; ExpressionNoun; EyeColor; EyeColorUnusual; Eyes; FacialFeaturesFemale; FacialFeaturesMale; FacialHairMale; FacialScars; FemaleFacialExpressionSentence; FemaleHairStyleColor; Finger; HairColor; HairColorUnusual; HairCurve; Height; LimbScars; main; MaleFacialExpressionSentence; MaleHair; MaleHairStyleColor; NoseShape; PiercingChanceFemale; PiercingChanceMale; ScarChanceFemale; Scars; ScarSize; StartFemale; StartMale; TattooChance; TattooChanceFemale; TattooChanceMale; TorsoFeaturesFemale; TorsoScars}}

<sgtable>

;main
1,[StartFemale]

;DescriptionText
1,She has [FemaleHairStyleColor] with [Eyes]. [FemaleFacialExpressionSentence].

;StartFemale
1,[DescriptionText] [DistinguishingFeaturesChance] [TattooChanceFemale] [PiercingChanceFemale] [ScarChanceFemale]

;StartMale
1,[Start]

;HairColor
3,black
3,blonde
3,dark brown
3,light brown
1,[HairColorUnusual]
1,ash blonde
1,ash brown
1,cinnamon
1,copper
1,dark ash brown
1,dark golden blonde
1,deep warm brown
1,fiery red
1,light blonde
1,near-white
1,orange
1,platinum
1,plum brown
1,raven black
1,strawberry blonde
1,tawny blonde
1,warm auburn
1,warm golden blonde

;HairColorUnusual
1,bright red
1,dark blue
1,forest green
1,light blue
1,olive green
1,pink
1,violet

;Eyes
150,[EyeColor] eyes
1,<b>[EyeColorUnusual]</b>

;EyeColor
3,blue
3,brown
3,dark brown
2,aquamarine
2,emerald
2,forest green
2,golden brown
2,gray
2,gray green
2,green
2,hazel
2,olive green
2,sapphire blue
2,sky blue
2,warm brown
1,black
1,copper
1,different colored (right: [EyeColor], left: [EyeColor])
1,indigo
1,jade
1,violet
1,yellow green

;EyeColorUnusual
1,eyes that are all black with white pupils
1,eyes that change color with her mood
1,glowing red eyes
1,glowing yellow eyes
1,eyes like mercury
1,pink eyes
1,eyes that are all white with black pupils
1,yellow eyes with vertical irises (cat-eyes)
1,eyes with irises that look like burning flames

;FemaleHairStyleColor
10,short [HairCurve] [HairColor] hair
5,pixie-cut [HairColor] hair
10,chin-length [HairCurve] [HairColor] hair
10,shoulder-length [HairCurve] [HairColor] hair
11,long [HairCurve] [HairColor] hair
5,[HairColor] hair in pigtails
5,[HairColor] hair in a ponytail
5,[HairColor] hair in two long braids
5,[HairColor] hair in a single braid
4,unkept [HairCurve] [HairColor] hair
2,[HairColor] hair in dreadlocks
4,[HairCurve] [HairColor] hair parted in the middle
4,[HairColor] hair in a topknot
4,[HairColor] hair in a bun
2,[HairColor] hair cut close to her head
1,[HairColor] hair in a large mohawk
1,[HairColor] hair pulled behind a headband
1,[HairColor] hair held back by a tortoiseshell comb
6,very long [HairCurve] [HairColor] hair
10,long, braided [HairColor] hair
4,greasy, limp [HairColor] hair

;HairCurve
5,straight
5,wavy
5,curly
1,frizzy
2,windswept
1,unkempt

;MaleHair
8,has [MaleHairStyleColor]
1,is bald
1,has thinning, receding [MaleHairStyleColor]

;MaleHairStyleColor
10,short-cropped [HairColor] hair
4,wavy [HairColor] hair
4,long [HairColor] hair
3,[HairColor] hair in a ponytail
1,“monk-cut” [HairColor] hair

;FemaleFacialExpressionSentence
5,She has {{ia|[ExpressionAdjective]}} expression
5,Her face has an expression of [ExpressionNoun]
5,She seems [ExpressionAdjective]
5,She seems [ExpressionAdjective]
5,Her expression shows her inner [ExpressionNoun]

;MaleFacialExpressionSentence
5,He has {{ia|[ExpressionAdjective]}} expression
5,His face has an expression of [ExpressionNoun]
5,He seems [ExpressionAdjective]
5,He looks [ExpressionAdjective]
5,His expression shows his inner [ExpressionNoun]

;ExpressionAdjective
5,calm
3,cheerful
3,content
3,friendly
2,aloof
2,determined
2,emotionless
2,happy
2,proud
2,thoughtful
1,aggressive
1,angry
1,confused
1,dazed
1,easy going
1,enraged
1,excited
1,focused
1,furtive
1,haughty
1,humble
1,kind
1,mysterious
1,provocative
1,resolute
1,sad
1,seductive
1,serene
1,serious
1,shy
1,sleepy
1,studious
1,subdued
1,surprised
1,tired
1,uneasy
1,well-bred

;ExpressionNoun
4,happiness
3,calm
3,contentment
3,determination
3,resolve
3,sadness
2,boredom
2,interest
2,joy
2,peace
2,serenity
1,anger
1,confusion
1,disdain
1,disgust
1,dread
1,embarrassment
1,fear
1,horror
1,rage
1,surprise

;NoseShape
1,beaklike
1,bulbous
1,crooked
1,hooked
1,humped
1,long, thin
1,piggish
1,previously-broken
1,retroussé

;Build
1,athletic
1,burly
1,gaunt
1,grossly obese
1,healthy
1,lanky
1,muscular
1,obese
1,plump
1,portly
1,skeletal
1,slightly chubby
1,stout
1,thin
1,well-proportioned
1,wiry

;Height
1,extremely tall
1,tall
6,of average height
1,short

;Deformities
2,missing [Finger] on [Utility.LeftRight] hand
2,withered [Finger] on [Utility.LeftRight] hand
1,extra finger on [Utility.LeftRight] hand
1,humped back
1,missing [Utility.LeftRight] arm
1,missing [Utility.LeftRight] hand
1,missing [Utility.LeftRight] leg
1,withered [Utility.LeftRight] arm
1,withered [Utility.LeftRight] hand

;Finger
5,thumb
5,index finger
5,middle finger
5,ring finger
5,pinky

;DistinguishingFeaturesChance
5,You immediately notice her [DistinguishingFeaturesFemale].
2,

;DistinguishingFeaturesMale
10,[NoseShape] nose
10,[FacialFeaturesMale]
1,[Deformities]
10,[FacialHairMale]
10,[Build] build
5,[DistinguishingFeaturesMale] and his [DistinguishingFeaturesMale]

;DistinguishingFeaturesFemale
10,[NoseShape] nose
10,[FacialFeaturesFemale]
3,[TorsoFeaturesFemale]
1,[Deformities]
10,[Build] build
8,[DistinguishingFeaturesFemale] and her [DistinguishingFeaturesFemale]

;FacialHairMale
1,“soul patch” beard
1,bushy beard
1,goatee
1,grizzled beard
1,handlebar mustache
1,long beard
1,long sideburns
1,medium-length beard with no mustache
1,moustache
1,mutton chop sideburns
1,neatly trimmed beard
1,patchy beard

;FacialFeaturesFemale
3,[Utility.LeftRight] eye is covered by an eyepatch
1,bad acne
1,chiseled facial features
1,chubby cheeks
1,cleft chin
1,freckles
1,full lips
1,graceful neck
1,hairy upper lip
1,high cheekbones
1,long face
1,pouty lips
1,pretty face
1,prominent freckles
1,rosy cheeks
1,round face
1,severe underbite
1,sneering lip
1,vacant stare
1,buck teeth
1,missing teeth
1,stained teeth
1,bushy eyebrows
1,lazy eye
1,shaved eyebrows
1,thinly plucked eyebrows
1,alluring eyes
1,beady eyes
1,bulging eyes
1,laugh lines around her eyes
1,seductive eyes
1,smiling eyes

;FacialFeaturesMale
3,[Utility.LeftRight] eye is covered by an eyepatch
1,[Utility.LeftRight] eye has cataracts
1,bad acne
1,buck teeth
1,bulging eyes
1,bushy eyebrows
1,chiseled facial features
1,cleft chin
1,dimples
1,furrowed brow
1,intense eyes
1,lazy eye
1,long face
1,missing teeth
1,missing teeth
1,pockmarked cheeks
1,prominent freckles
1,pudgy cheeks
1,round face
1,shaved eyebrows
1,stained teeth
1,twisted lip
1,weathered features

;TorsoFeaturesFemale
5,large breasts
4,hourglass figure
4,pale skin
4,tanned skin
4,voluptuous figure
2,curvy figure
2,protruding belly
1,freckled skin
1,obvious pregnancy
1,rolls of fat

;TattooChance
1,[TattooChanceFemale]

;TattooChanceMale
8,
1,[Tattoos.Malemain]

;TattooChanceFemale
7,
1,{{ucfirst:[Tattoos.Femalemain]}}.

;PiercingChanceMale
15,
1,[PiercingsMale.Start]

;PiercingChanceFemale
3,
1,[Piercings.Femalemain]


;ScarChanceFemale
10,
1,She has a [ScarSize] scar on her [Scars].

;Scars
1,[FacialScars]
1,[LimbScars]
1,[TorsoScars]

;ScarSize
5,small
3,noticeable
2,large
1,disfiguring

;FacialScars
1,[Utility.LeftRight] cheek
1,[Utility.LeftRight] ear
1,[Utility.LeftRight] forehead
1,[Utility.LeftRight] lip
1,[Utility.LeftRight] side of the neck
1,across the [Utility.LeftRight] eye area

;LimbScars
1,[Utility.LeftRight] calf
1,[Utility.LeftRight] elbow
1,[Utility.LeftRight] foot
1,[Utility.LeftRight] hand
1,[Utility.LeftRight] knee
1,[Utility.LeftRight] thigh
1,lower [Utility.LeftRight] arm
1,upper [Utility.LeftRight] arm

;TorsoScars
1,[Utility.LeftRight] buttock
1,[Utility.LeftRight] shoulder
1,[Utility.LeftRight] stomach area
1,lower [Utility.LeftRight] back
1,lower [Utility.LeftRight] ribcage
1,upper [Utility.LeftRight] back
1,upper [Utility.LeftRight] side of the chest
