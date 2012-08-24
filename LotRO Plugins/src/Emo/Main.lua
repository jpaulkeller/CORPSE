import "FrelledPlugins.Emo";
import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";

-- seed the random number generator
math.randomseed(Turbine.Engine.GetGameTime());
-- avoid first random number bug in some lua systems
math.random();

_dw, _dh = Turbine.UI.Display.GetSize();

_emoVersion = "2.2.2";

local function updateScreenCoords(widget)
	local ww, wh, wx, wy;
	local changed = false;

	wx, wy = widget:GetPosition();
	ww, wh = widget:GetSize();

	if ((wx + ww) > _dw) then
		wx = _dw - ww;
		changed = true;
	end
	if ((wy + wh) > _dh) then
		wy = _dh - wh;
		changed = true;
	end
	
	if (wx < 0) then
		wx = 0;
		changed = true;
	end
	if (wy < 0) then
		wy = 0;
		changed = true;
	end
	
	if (changed) then
		widget:SetPosition(wx, wy);
	end
end

-- _emotes is the table created by EmoEmotes construction
-- Call _emoEmotes:Save() to save the data off
-- Call _emoEmotes:Reload() to reread (and destroy) the current _emotes
-- Changes to _emotes will be reflected in EmoEmotes as that is the data used... so be careful!
_emoEmotes = EmoEmotes();

_emoOptions = EmoOptionsWindow();
_emoBar = EmoBar(_emoOptions:GetBarOrientation());
_emoManual = EmoManualWindow();
_emoGroupEdit = EmoGroupEditWindow();
_emoEmoteEdit = EmoEmoteEditWindow();
_emoEmoteEdit.Save = function(sender, args)
	local msg = nil;
	if (args.name == nil or string.find(args.name, "^/[%a%d_]+$") == nil) then
		return false, "Error: Emote name must be of the form: /ALPHA_OR_NUMERIC_OR_UNDERSCORE (e.g. /hi_there1)";
	end
	if (_emotes[args.name] ~= nil and _emotes[args.name].custom == false) then
		return false, "Error: The emote name '"..args.name.."' is a game emote, please choose another name.";
	end
	if (args.emote == nil or args.emote == "") then
		return false, "Error: Invalid animation emote, please choose one from the list.";
	end
	if (args.notarget_all == nil or args.notarget_all == "") then
		msg = "Warning: You have not specified any custom text for 'No Target' and 'All'. This will result in the emote '"..args.emote.."' being executed like normal under the name '"..args.name.."'";
	end
	_emoEmotes:SetCustomEmote(args.name, args.emote, args.notarget_all);
	_emoEmotes:Save();
	_emoBar:AddCustomEmote(args.name);
	return true, msg;
end

_emoOptions:SetVisible(false);
_emoManual:SetVisible(false);
_emoGroupEdit:SetVisible(false);
_emoEmoteEdit:SetVisible(false);
_emoBar:SetVisible(_emoOptions:GetAutoShowBar());

_emoCommand = Turbine.ShellCommand();

-- Make sure everything is visible on the screen
updateScreenCoords(_emoBar);
updateScreenCoords(_emoOptions);
updateScreenCoords(_emoManual);
updateScreenCoords(_emoGroupEdit);
updateScreenCoords(_emoEmoteEdit);

function _emoCommand:Execute(cmd, args)
	-- TODO: need to figure out how multiple args come in or if it's just a string.
	-- then call _emoEmotes:AddNewGameEmote(emote).
	local emote = string.match(args, "^add_turbine_emote [^/]*(/[%a%d_]+)$");
	
	if (args == "show") then
		_emoBar:SetVisible(true);
	elseif (args == "hide") then
		_emoBar:SetVisible(false);
	elseif (args == "toggle") then
		_emoBar:SetVisible(not _emoBar:IsVisible());
	elseif (args == "options") then
		_emoOptions:SetVisible(true);
	elseif (args == "help") then
		_emoManual:SetVisible(true);
	elseif (emote ~= nil) then
		_emoEmotes:AddNewGameEmote(emote);
		_emoBar:UpdateGameEmotes(emote);
		Turbine.Shell.WriteLine("Saved new emote '"..emote.."'.");
	elseif (args == "default_game_emotes") then
		_emoEmotes:DefaultGameEmotes();
		_emoBar:UpdateGameEmotes();
		Turbine.Shell.WriteLine("Saved default emotes.");
	else
		_emoCommand:GetHelp();
	end
end

function _emoCommand:GetHelp()
	Turbine.Shell.WriteLine("Emo v".._emoVersion.." - A Frelled Plugin");
	Turbine.Shell.WriteLine("  usage: /emo [show|hide|toggle|options|help]");
	Turbine.Shell.WriteLine("  usage: /emo add_turbine_emote /new_turbine_emote  (Only use for new in-game emotes that Turbine adds)");
	Turbine.Shell.WriteLine("  usage: /emo default_game_emotes  (Use to get the default emotes provided by Turbine)");
	Turbine.Shell.WriteLine("  For more help: /emo help");
end

Turbine.Shell.AddCommand("emo", _emoCommand);

Turbine.Shell.WriteLine("Emo v".._emoVersion.." by Frellco of Elendilmir (/emo for more info)");
