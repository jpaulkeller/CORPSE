clear    = Turbine.UI.Color(0, 0, 0, 0);
black    = Turbine.UI.Color(0, 0, 0);
white    = Turbine.UI.Color(1, 1, 1);
red      = Turbine.UI.Color(1, 0, 0);
yellow   = Turbine.UI.Color(1, 1, 0);
green    = Turbine.UI.Color(0, 1, 0);
gold     = Turbine.UI.Color(245/255, 222/255, 147/255);

disabledText = Turbine.UI.Color(162/255, 162/255, 162/255);
standardText = Turbine.UI.Color(245/255, 222/255, 147/255);
outlineColor = Turbine.UI.Color(232/255, 175/255, 72/255);
selectedText = Turbine.UI.Color(203/255, 195/255, 52/255);
selectedItem = Turbine.UI.Color(0, 55/255, 45/255);

stanceTable = {
  {name="Determination", key="d"}, 
  {name="Recklessness",  key="r"}, 
  {name="Assailment",    key="a"}
};

filterTable = {
  {name="Spear (1)",  pattern="bld=1"},
  {name="Shield (2)", pattern="bld=2"},
  {name="Fist (3)",   pattern="bld=3"},
  {name="AoE",        pattern="AoE"},
  {name="CC",         pattern="daze|stun|root|fear"},
  {name="Defense",    pattern="block|parry|evade|defense"},
  {name="DoT",        pattern="bleed|DoT"},
  {name="Heal",       pattern="heal|morale|HoT|lifetip"},
  {name="Mitigation", pattern="mitigation"},
  {name="Threat",     pattern="threat|ToT"}
};

gambitTable = {
  {name="Aggression",           lvl=62, bld="3213",  fx="", d="+1 (per fellow) threat leech"},
  {name="Boar's Rush",          lvl=44, bld="1313",  fx=""},
  {name="Brink of Victory",     lvl=22, bld="323",   fx="", d="+1 threat; bleed"},
  {name="Celebration of Skill", lvl=40, bld="2121",  fx="", d="+morale; +block (10s)"},
  {name="Combination Strike",   lvl=28, bld="131",   fx="", d="damage"},
  {name="Conviction",           lvl=54, bld="23232", fx="", d="+1 (per fellow) threat leech; +morale (for fellowship)"},
  {name="Dance of War",         lvl=42, bld="2323",  fx="", d="+1 (per fellow) threat leech; +evade (20s); +phys/tact mitigation and crit defense (10s)"},
  {name="Defensive Strike",     lvl= 2, bld="22",    fx="", d="+block (20s)"},
  {name="Deflection",           lvl=50, bld="231",   fx=""},
  {name="Deft Strike",          lvl= 1, bld="11",    fx="potency", damage=479},
  {name="Desolation",           lvl=56, bld="32323", fx="", d="bleed"},
  {name="Exultation of Battle", lvl=60, bld="31232", fx="", d="+8 ToT; AoE damage"},
  {name="Fierce Resolve",       lvl=26, bld="312",   fx="", d="damage; AoE damage"},
  {name="Goad",                 lvl= 4, bld="33",    fx="potency; low bleed (16s)", damage=226, d="low threat increase", a="low threat decrease"},
  {name="Impressive Flourish",  lvl= 9, bld="23",    fx="", d="+phys/tact mitigation and crit defense (30s)"},
  {name="Maddening Strike",     lvl=16, bld="232",   fx="", d="+0.5 (per fellow) threat leech; +phys/tact mitigation and crit defense (20s)"},
  {name="Mighty Blow",          lvl=38, bld="1231",  note="hard to parry/evade", damage=618, r="moderate bleed (16s), bonus damage if over low bleed"},
  {name="Offensive Strike",     lvl=10, bld="13",    damage=812, d="+2 threat", r="bleed (16s)", a="bleed (16s)"},
  {name="Onslaught",            lvl=32, bld="121",   fx="interrupt", damage=1074, d="damage"},
  {name="Persevere",            lvl= 6, bld="21",    fx="", d="+morale"},
  {name="Piercing Strike",      lvl=30, bld="313",   fx="", d="+1 threat; +block (30s)"},
  {name="Power Attack",         lvl=18, bld="123",   note="hard to block", damage=604, r="low bleed (16s)"},
  {name="Precise Blow",         lvl=12, bld="31",    fx="", d="+2 threat, +2 ToT"},
  {name="Resolution",           lvl=42, bld="3123",  fx="", d="AoE damage"},
  {name="Restoration",          lvl=74, bld="21212", fx="", d="+morale"},
  {name="Reversal",             lvl=52, bld="132",   fx=""},
  {name="Safeguard",            lvl=24, bld="212",   fx="", d="+morale; +block (20s)"},
  {name="Shield Mastery",       lvl=34, bld="2132",  fx="", d="+block/evade (60s)"},
  {name="Shield Tactics",       lvl=74, bld="2312",  fx="", d="+tact mitigation (60s)"},
  {name="Shield Up",            lvl=20, bld="213",   fx="", d="+block/evade (30s)"},
  {name="Spear of Virtue",      lvl=46, bld="3131",  fx="", d="+3 threat"},
  {name="Surety of Death",      lvl=48, bld="3232",  fx="", d="+3 threat; bleed; +evade (10s)"},
  {name="The Boot",             lvl= 3, bld="12",    fx="interrupt", damage=219,  d="25% chance to apply 5s daze", r="bleed (16s)", a="25% chance to apply 5s root"},
  {name="The Dark Before Dawn", lvl=64, bld="12131", damage=1192, d="restores 178 power + 1% max power per 4s (stacks once)", r="restores power; restores 1% power per 4s (stacks once)"},
  {name="Unerring Strike",      lvl=50, bld="12312", note="hard to block/parry/evade", damage=648, fx="bleed (16s)", r="bleed (16s), bonus damage if over moderate bleed"},
  {name="Wall of Steel",        lvl=36, bld="1212",  fx="interrupt", damage=1239, d="+1500 parry (30s)", r="on common/fire/shadow: reflect damage, 10% restore 2% power (30s)", a="on common/fire/shadow: reflect damage (30s)"},
  {name="War-cry",              lvl=13, bld="32",    fx="", d="+2 threat; bleed; +morale; +evade (30s)"},
  {name="Warden's Triumph",     lvl=70, bld="13213", fx="", d="damage"}
};
