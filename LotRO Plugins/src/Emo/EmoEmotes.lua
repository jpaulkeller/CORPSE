import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";

local private = {};
private = {
	NewGameEmote = function(self, gameemote)
		return {
			emote = gameemote,
			custom = false,
			no_target = nil,
			target_to_all = nil,
			target_to_target = nil,
			target_to_self = nil,
		};
	end,

	SetGameEmotes = function(self)
		_emotes["/agree"] = private.NewGameEmote(self, "/agree");
		_emotes["/ahem"] = private.NewGameEmote(self, "/ahem");
		_emotes["/airlute"] = private.NewGameEmote(self, "/airlute");
		_emotes["/angry"] = private.NewGameEmote(self, "/angry");
		_emotes["/assist"] = private.NewGameEmote(self, "/assist");
		_emotes["/attack"] = private.NewGameEmote(self, "/attack");
		_emotes["/away"] = private.NewGameEmote(self, "/away");
		
		_emotes["/beckon"] = private.NewGameEmote(self, "/beckon");
		_emotes["/beg"] = private.NewGameEmote(self, "/beg");
		_emotes["/bio"] = private.NewGameEmote(self, "/bio");
		_emotes["/blush"] = private.NewGameEmote(self, "/blush");
		_emotes["/boo"] = private.NewGameEmote(self, "/boo");
		_emotes["/bored"] = private.NewGameEmote(self, "/bored");
		_emotes["/bother"] = private.NewGameEmote(self, "/bother");
		_emotes["/bow"] = private.NewGameEmote(self, "/bow");
		_emotes["/brb"] = private.NewGameEmote(self, "/brb");
		_emotes["/burp"] = private.NewGameEmote(self, "/burp");
		_emotes["/bye"] = private.NewGameEmote(self, "/bye");
		
		_emotes["/calm"] = private.NewGameEmote(self, "/calm");
		_emotes["/challenge"] = private.NewGameEmote(self, "/challenge");
		_emotes["/charge"] = private.NewGameEmote(self, "/charge");
		_emotes["/cheer"] = private.NewGameEmote(self, "/cheer");
		_emotes["/chip"] = private.NewGameEmote(self, "/chip");
		_emotes["/chuckle"] = private.NewGameEmote(self, "/chuckle");
		_emotes["/clap"] = private.NewGameEmote(self, "/clap");
		_emotes["/coinflip"] = private.NewGameEmote(self, "/coinflip");
		_emotes["/confused"] = private.NewGameEmote(self, "/confused");
		_emotes["/congratulate"] = private.NewGameEmote(self, "/congratulate");
		_emotes["/cough"] = private.NewGameEmote(self, "/cough");
		_emotes["/cower"] = private.NewGameEmote(self, "/cower");
		_emotes["/crazy"] = private.NewGameEmote(self, "/crazy");
		_emotes["/cry"] = private.NewGameEmote(self, "/cry");
		_emotes["/curtsey"] = private.NewGameEmote(self, "/curtsey");
		
		_emotes["/dance"] = private.NewGameEmote(self, "/dance");
		_emotes["/dance1"] = private.NewGameEmote(self, "/dance1");
		_emotes["/dance2"] = private.NewGameEmote(self, "/dance2");
		_emotes["/dance3"] = private.NewGameEmote(self, "/dance3");
		_emotes["/dance_dwarf"] = private.NewGameEmote(self, "/dance_dwarf");
		_emotes["/dance_dwarf2"] = private.NewGameEmote(self, "/dance_dwarf2");
		_emotes["/dance_dwarf3"] = private.NewGameEmote(self, "/dance_dwarf3");
		_emotes["/dance_elf"] = private.NewGameEmote(self, "/dance_elf");
		_emotes["/dance_elf2"] = private.NewGameEmote(self, "/dance_elf2");
		_emotes["/dance_elf3"] = private.NewGameEmote(self, "/dance_elf3");
		_emotes["/dance_hobbit"] = private.NewGameEmote(self, "/dance_hobbit");
		_emotes["/dance_hobbit2"] = private.NewGameEmote(self, "/dance_hobbit2");
		_emotes["/dance_hobbit3"] = private.NewGameEmote(self, "/dance_hobbit3");
		_emotes["/dance_jig"] = private.NewGameEmote(self, "/dance_jig");
		_emotes["/dance_man"] = private.NewGameEmote(self, "/dance_man");
		_emotes["/dance_man2"] = private.NewGameEmote(self, "/dance_man2");
		_emotes["/dance_man3"] = private.NewGameEmote(self, "/dance_man3");
		_emotes["/dream"] = private.NewGameEmote(self, "/dream");
		_emotes["/drink"] = private.NewGameEmote(self, "/drink");
		_emotes["/drive"] = private.NewGameEmote(self, "/drive");
		_emotes["/drool"] = private.NewGameEmote(self, "/drool");
		_emotes["/drunk"] = private.NewGameEmote(self, "/drunk");
		_emotes["/dustoff"] = private.NewGameEmote(self, "/dustoff");
		
		_emotes["/eat"] = private.NewGameEmote(self, "/eat");
		
		_emotes["/faint"] = private.NewGameEmote(self, "/faint");
		_emotes["/feather"] = private.NewGameEmote(self, "/feather");
		_emotes["/fidget"] = private.NewGameEmote(self, "/fidget");
		_emotes["/fight"] = private.NewGameEmote(self, "/fight");
		_emotes["/firebreath"] = private.NewGameEmote(self, "/firebreath");
		_emotes["/fishslap"] = private.NewGameEmote(self, "/fishslap");
		_emotes["/flex"] = private.NewGameEmote(self, "/flex");
		_emotes["/flip"] = private.NewGameEmote(self, "/flip");
		_emotes["/flirt"] = private.NewGameEmote(self, "/flirt");
		_emotes["/followme"] = private.NewGameEmote(self, "/followme");
		
		_emotes["/giggle"] = private.NewGameEmote(self, "/giggle");
		_emotes["/golf"] = private.NewGameEmote(self, "/golf");
		_emotes["/groundroll"] = private.NewGameEmote(self, "/groundroll");
		_emotes["/grumble"] = private.NewGameEmote(self, "/grumble");
		
		_emotes["/hail"] = private.NewGameEmote(self, "/hail");
		_emotes["/handstand"] = private.NewGameEmote(self, "/handstand");
		_emotes["/heropose"] = private.NewGameEmote(self, "/heropose");
		_emotes["/howl"] = private.NewGameEmote(self, "/howl");
		_emotes["/hug"] = private.NewGameEmote(self, "/hug");
		
		_emotes["/impatient"] = private.NewGameEmote(self, "/impatient");
		_emotes["/innocent"] = private.NewGameEmote(self, "/innocent");
		_emotes["/inspectgem"] = private.NewGameEmote(self, "/inspectgem");
		
		_emotes["/jazzhands"] = private.NewGameEmote(self, "/jazzhands");
		_emotes["/jjacks"] = private.NewGameEmote(self, "/jjacks");
		_emotes["/juggle"] = private.NewGameEmote(self, "/juggle");
		_emotes["/jump"] = private.NewGameEmote(self, "/jump");
		
		_emotes["/kiss"] = private.NewGameEmote(self, "/kiss");
		_emotes["/kneel"] = private.NewGameEmote(self, "/kneel");
		
		_emotes["/laugh"] = private.NewGameEmote(self, "/laugh");
		_emotes["/liedown"] = private.NewGameEmote(self, "/liedown");
		_emotes["/lol"] = private.NewGameEmote(self, "/lol");
		_emotes["/look"] = private.NewGameEmote(self, "/look");
		_emotes["/lookaround"] = private.NewGameEmote(self, "/lookaround");
		
		_emotes["/meditate"] = private.NewGameEmote(self, "/meditate");
		_emotes["/mock"] = private.NewGameEmote(self, "/mock");
		_emotes["/mood_angry"] = private.NewGameEmote(self, "/mood_angry");
		_emotes["/mood_apprehensive"] = private.NewGameEmote(self, "/mood_apprehensive");
		_emotes["/mood_calm "] = private.NewGameEmote(self, "/mood_calm");
		_emotes["/mood_confused"] = private.NewGameEmote(self, "/mood_confused");
		_emotes["/mood_fearful"] = private.NewGameEmote(self, "/mood_fearful");
		_emotes["/mood_happy"] = private.NewGameEmote(self, "/mood_happy");
		_emotes["/mood_mischievous"] = private.NewGameEmote(self, "/mood_mischievous");
		_emotes["/mood_sad"] = private.NewGameEmote(self, "/mood_sad");
		_emotes["/mood_sleepy"] = private.NewGameEmote(self, "/mood_sleepy");
		_emotes["/mood_solemn"] = private.NewGameEmote(self, "/mood_solemn");
		_emotes["/mood_surprised"] = private.NewGameEmote(self, "/mood_surprised");
		_emotes["/mountbow"] = private.NewGameEmote(self, "/mountbos");
		_emotes["/mountkick"] = private.NewGameEmote(self, "/mountkick");
		_emotes["/mountrearup"] = private.NewGameEmote(self, "/mountrearup");
		_emotes["/mourn"] = private.NewGameEmote(self, "/mourn");
		_emotes["/mumble"] = private.NewGameEmote(self, "/mumble");
		_emotes["/munch"] = private.NewGameEmote(self, "/munch");
		
		_emotes["/no"] = private.NewGameEmote(self, "/no");
		_emotes["/nothing"] = private.NewGameEmote(self, "/nothing");
		
		_emotes["/oop"] = private.NewGameEmote(self, "/oop");
		
		_emotes["/pan"] = private.NewGameEmote(self, "/pan");
		_emotes["/paper"] = private.NewGameEmote(self, "/paper");
		_emotes["/pat"] = private.NewGameEmote(self, "/pat");
		_emotes["/pick"] = private.NewGameEmote(self, "/pick");
		_emotes["/point"] = private.NewGameEmote(self, "/point");
		_emotes["/poke"] = private.NewGameEmote(self, "/poke");
		_emotes["/pose"] = private.NewGameEmote(self, "/pose");
		_emotes["/pushups"] = private.NewGameEmote(self, "/pushups");
		_emotes["/putt"] = private.NewGameEmote(self, "/putt");
		
		_emotes["/ready"] = private.NewGameEmote(self, "/ready");
		_emotes["/resist"] = private.NewGameEmote(self, "/resist");
		_emotes["/rest"] = private.NewGameEmote(self, "/rest");
		_emotes["/rich"] = private.NewGameEmote(self, "/rich");
		_emotes["/roar"] = private.NewGameEmote(self, "/roar");
		_emotes["/rude"] = private.NewGameEmote(self, "/rude");
		
		_emotes["/sad"] = private.NewGameEmote(self, "/sad");
		_emotes["/salute"] = private.NewGameEmote(self, "/salute");
		_emotes["/scissor"] = private.NewGameEmote(self, "/scissor");
		_emotes["/scold"] = private.NewGameEmote(self, "/scold");
		_emotes["/scratch"] = private.NewGameEmote(self, "/scratch");
		_emotes["/shakefist"] = private.NewGameEmote(self, "/shakefist");
		_emotes["/shiver"] = private.NewGameEmote(self, "/shiver");
		_emotes["/shrug"] = private.NewGameEmote(self, "/shrug");
		_emotes["/sigh"] = private.NewGameEmote(self, "/sigh");
		_emotes["/sing"] = private.NewGameEmote(self, "/sing");
		_emotes["/sit"] = private.NewGameEmote(self, "/sit");
		_emotes["/slap"] = private.NewGameEmote(self, "/slap");
		_emotes["/smackhead"] = private.NewGameEmote(self, "/smackhead");
		_emotes["/smoke"] = private.NewGameEmote(self, "/smoke");
		_emotes["/smoke1"] = private.NewGameEmote(self, "/smoke1");
		_emotes["/sneeze"] = private.NewGameEmote(self, "/sneeze");
		_emotes["/snowwizard"] = private.NewGameEmote(self, "/snowwizard");
		_emotes["/sorry"] = private.NewGameEmote(self, "/sorry");
		_emotes["/spin"] = private.NewGameEmote(self, "/spin");
		_emotes["/stare"] = private.NewGameEmote(self, "/stare");
		_emotes["/stone"] = private.NewGameEmote(self, "/stone");
		_emotes["/story"] = private.NewGameEmote(self, "/story");
		_emotes["/stretch"] = private.NewGameEmote(self, "/stretch");
		_emotes["/succumb"] = private.NewGameEmote(self, "/succumb");
		_emotes["/surrender"] = private.NewGameEmote(self, "/surrender");
		_emotes["/sweat"] = private.NewGameEmote(self, "/sweat");
		_emotes["/swordsalute"] = private.NewGameEmote(self, "/swordsalute");
		
		_emotes["/talk "] = private.NewGameEmote(self, "/talk");
		_emotes["/tantrum"] = private.NewGameEmote(self, "/tantrum");
		_emotes["/tear"] = private.NewGameEmote(self, "/tear");
		_emotes["/thank"] = private.NewGameEmote(self, "/thank");
		_emotes["/think"] = private.NewGameEmote(self, "/think");
		_emotes["/tickle"] = private.NewGameEmote(self, "/tickle");
		
		_emotes["/wait"] = private.NewGameEmote(self, "/wait");
		_emotes["/warmhands"] = private.NewGameEmote(self, "/warmhands");
		_emotes["/wave"] = private.NewGameEmote(self, "/wave");
		_emotes["/whippitydo"] = private.NewGameEmote(self, "/whippitydo");
		_emotes["/whistle"] = private.NewGameEmote(self, "/whistle");
		_emotes["/wince"] = private.NewGameEmote(self, "/wince");
		_emotes["/wink"] = private.NewGameEmote(self, "/wink");
		_emotes["/wipesweat"] = private.NewGameEmote(self, "/wipesweat");
		
		_emotes["/yawn"] = private.NewGameEmote(self, "/yawn");
		_emotes["/yes"] = private.NewGameEmote(self, "/yes");
	end,

	ReadGameEmotes = function(self)
		self.loading = true;

		local gameEmotes = Turbine.PluginData.Load(Turbine.DataScope.Account, "EmoGameEmotes");

		if (type(gameEmotes) ~= "table") then
			gameEmotes = {};
		end

		for k, v in ipairs(gameEmotes) do
			if (_emotes[v] == nil) then
				_emotes[v] = private.NewGameEmote(self, v);
			end
			gameEmotes[k] = nil;
		end
		gameEmotes = nil;

		self.loading = false;
	end,

	SaveGameEmotes = function(self)
		if (self.loading == true) then
			return;
		end
		
		local gameEmotes = {};

		for k, v in pairs(_emotes) do
			-- only add game emotes
			if (v.custom == false) then
				table.insert(gameEmotes, k);
			end
		end
		table.sort(gameEmotes);

		Turbine.PluginData.Save(Turbine.DataScope.Account, "EmoGameEmotes", gameEmotes);
		
		for k, v in ipairs(gameEmotes) do
			gameEmotes[k] = nil;
		end
		gameEmotes = nil;
	end,

	ReadCustomEmotes = function(self)
		-- grab customs from options
		self.loading = true;

		local customEmotes = Turbine.PluginData.Load(Turbine.DataScope.Account, "EmoCustomEmotes");

		if (type(customEmotes) ~= "table") then
			customEmotes = {};
		end

		--[[
		TODO: 
		
		if there is a conflict with an existing Game Emote, pop a dialog asking
		for a new name. If we have any of these, immediately after all the changes,
		write the customs back out.
		
		this needs to loop through until we have no conflicts. 
		--]]
		for k, v in pairs(customEmotes) do
			if (_emotes[k] ~= nil) then
				out("Error: Custom emote '"..k.."' is an actual game emote. This may be a recent change. Please rename your custom emote.");
			else
				-- Define it this way to avoid later upgrade issues
				if (v ~= nil and v.emote ~= nil) then
					_emotes[k] = private.NewGameEmote(self, k);
					_emotes[k].emote = v.emote;
					_emotes[k].custom = true;
					if (v.no_target ~= nil) then
						_emotes[k].no_target = v.no_target;
					end
					if (v.target_to_all ~= nil) then
						_emotes[k].target_to_all = v.target_to_all;
					end
					if (v.target_to_target ~= nil) then
						_emotes[k].target_to_target = v.target_to_target;
					end
					if (v.target_to_self ~= nil) then
						_emotes[k].target_to_self = v.target_to_self;
					end
				end
			end
			customEmotes[k] = nil;
		end
		customEmotes = nil;

		self.loading = false;
	end,

	SaveCustomEmotes = function(self)
		if (self.loading == true) then
			return;
		end
		
		local customEmotes = {};

		-- loop through _emotes and save off the ones which are customs
		for k, v in pairs(_emotes) do
			if (v.custom == true) then
				customEmotes[k] = v;
			end
		end

		-- write out customEmotes to the options
		Turbine.PluginData.Save(Turbine.DataScope.Account, "EmoCustomEmotes", customEmotes);
		
		for k, v in pairs(customEmotes) do
			customEmotes[k] = nil;
		end
		customEmotes = nil;
	end,
};

local function out(msg)
	Turbine.Shell.WriteLine(msg);
end

EmoEmotes = class();

_emotes = {};

function EmoEmotes:Constructor()
	self.loading = false;
	self:Reload();
end

function EmoEmotes:Save()
	private.SaveCustomEmotes(self);
	private.SaveGameEmotes(self);
end

function EmoEmotes:Reload()
	for k in pairs(_emotes) do
		_emotes[k] = nil;
	end
	_emotes = nil;
	_emotes = {};
	private.SetGameEmotes(self);
	private.ReadGameEmotes(self);
	private.ReadCustomEmotes(self);
end

function EmoEmotes:SetCustomEmote(pname, pemote, pnotarget_all)
	if (_emotes[pname] ~= nil) then
		_emotes[pname] = nil;
	end
	
	_emotes[pname] = private.NewGameEmote(self, pname);
	_emotes[pname].emote = pemote;
	_emotes[pname].custom = true;
	_emotes[pname].no_target = pnotarget_all;
end

function EmoEmotes:AddNewGameEmote(pemote)
	if (_emotes[pemote] == nil) then
		_emotes[pemote] = private.NewGameEmote(self, pemote);
		private.SaveGameEmotes(self);
	end
end

function EmoEmotes:DefaultGameEmotes()
	for k, v in pairs(_emotes) do
		_emotes[k] = nil;
	end
	_emotes = nil;
	_emotes = {};
	private.SetGameEmotes(self);
	private.SaveGameEmotes(self);
end
