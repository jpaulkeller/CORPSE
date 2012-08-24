import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";

local function out(msg)
	Turbine.Shell.WriteLine(msg);
end

EmoBar = class(Turbine.UI.Extensions.SimpleWindow);

function EmoBar:Constructor(orientation)
	Turbine.UI.Extensions.SimpleWindow.Constructor(self);

	self.orientation = orientation;
	self.leftMargin = 8;
	self.rightMargin = 8;
	self.height = 48;
	self.groupSlotSize = 32;
	self.group = {};
	self.buttonLbl = {};
	self.groupLbl = {};
	self.buttonFrame = {};
	
	-- load the last settings saved
	self.loading = false;
	self:LoadSettings();

	self.horizLeft = Turbine.UI.Label();
	self.horizLeft:SetParent(self);
	self.horizLeft:SetPosition(0, 0);
	self.horizLeft:SetSize(12, 48);
	self.horizLeft:SetBackground("FrelledPlugins/Emo/Resources/horizontal-left-end.tga");
	self.horizLeft:SetZOrder(5);
	self.horizLeft:SetMouseVisible(false);
	self.horizLeft:SetVisible(true);

	self.horizRight = Turbine.UI.Label();
	self.horizRight:SetParent(self);
	self.horizRight:SetPosition(0, 0);
	self.horizRight:SetSize(12, 48);
	self.horizRight:SetBackground("FrelledPlugins/Emo/Resources/horizontal-right-end.tga");
	self.horizRight:SetZOrder(5);
	self.horizRight:SetMouseVisible(false);
	self.horizRight:SetVisible(true);

	self.contextMenu = Turbine.UI.ContextMenu();
	local menuItems = self.contextMenu:GetItems();
	menuItems:Add(Turbine.UI.MenuItem("Add New Group"));
	menuItems:Add(Turbine.UI.MenuItem("Custom Emotes"));
	menuItems:Add(Turbine.UI.MenuItem("Options"));
	menuItems:Add(Turbine.UI.MenuItem("Help"));
	menuItems:Add(Turbine.UI.MenuItem("----"));
	menuItems:Add(Turbine.UI.MenuItem("Add Default: Bored"));
	menuItems:Add(Turbine.UI.MenuItem("Add Default: Bother"));
	menuItems:Add(Turbine.UI.MenuItem("Add Default: Dance"));
	menuItems:Add(Turbine.UI.MenuItem("Add Default: Deedmotes"));
	menuItems:Add(Turbine.UI.MenuItem("Add Default: Frustrated"));
	menuItems:Add(Turbine.UI.MenuItem("Add Default: Greet"));
	menuItems:Add(Turbine.UI.MenuItem("Add Default: Moods"));
	-- Add New Group
	local mCnt = 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		self:AddNewGroup();
		self:CreateGroups();
		self:SaveSettings();
	end
	-- Custom Emotes
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoEmoteEdit:Show();
	end
	-- Options
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoOptions:SetVisible(true);
	end
	-- Help
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoManual:SetVisible(true);
	end
	-- ----
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		out("----");
	end
	-- Bored
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		self:AddDefaultGroups("Bored", true);
	end
	-- Bother
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		self:AddDefaultGroups("Bother", true);
	end
	-- Dance
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		self:AddDefaultGroups("Dance", true);
	end
	-- Deedmotes
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		self:AddDefaultGroups("Deedmotes", true);
	end
	-- Frustrated
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		self:AddDefaultGroups("Frustrated", true);
	end
	-- Greet
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		self:AddDefaultGroups("Greet", true);
	end
	-- Moods
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		self:AddDefaultGroups("Moods", true);
	end

	self:CreateGroups();

	self:SetBackColor(Turbine.UI.Color(0, 0, 0, 0));
	self:SetPosition(self.settings.positionX, self.settings.positionY);
	self:SetOpacity(1);
	self:SetZOrder(0);
	self:SetAllowDrop(false);

	-- Event Handlers
	local isMoving = false;
	local x = 0;
	local y = 0;

	self.MouseDown = function(sender, args )
		if (args.Button == Turbine.UI.MouseButton.Left) then
			isMoving = true;
			x = args.X;
			y = args.Y;
		end
	end

	self.MouseUp = function(sender, args )
		if (args.Button == Turbine.UI.MouseButton.Left) then
			isMoving = false;
			local curX, curY = self:GetPosition();
			self.settings.positionX = curX;
			self.settings.positionY = curY;
			self:SaveSettings();
		elseif (args.Button == Turbine.UI.MouseButton.Right) then
			self.contextMenu:ShowMenu();
		end
	end

	self.MouseMove = function(sender, args )
		if (isMoving) then
			local oldX,oldY = self:GetPosition();
			self:SetPosition(oldX + args.X - x, oldY + args.Y - y);	
		end
	end
end

function EmoBar:DeleteGroupsUI(name)
	for k, v in pairs(self.group) do
		if (name == nil or name == k) then
			v:Deconstructor();
			self.group[k] = nil;
		end
	end
	if (name == nil) then
		self.group = nil;
	end
	for k, v in pairs(self.buttonLbl) do
		if (name == nil or name == k) then
			v:SetVisible(false);
			v:SetParent(nil);
			self.buttonLbl[k] = nil;
		end
	end
	if (name == nil) then
		self.buttonLbl = nil;
	end
	for k, v in pairs(self.groupLbl) do
		if (name == nil or name == k) then
			v:SetVisible(false);
			v:SetParent(nil);
			self.groupLbl[k] = nil;
		end
	end
	if (name == nil) then
		self.groupLbl = nil;
	end
	for k, v in pairs(self.buttonFrame) do
		if (name == nil or name == k) then
			v:SetVisible(false);
			v:SetParent(nil);
			self.buttonFrame[k] = nil;
		end
	end
	if (name == nil) then
		self.buttonFrame = nil;
	end
end

function EmoBar:AddCustomEmote(customEmote)
	for k, v in pairs(self.group) do
		v:GetGroup():NewEmote(customEmote);
	end
end

function EmoBar:UpdateGameEmotes(emote)
	for k, v in pairs(self.group) do
		v:GetGroup():UpdateGameEmotes(emote);
	end
	self:SaveSettings();
end

function EmoBar:CreateGroups()
	self:DeleteGroupsUI();
	
	self.group = {};
	self.buttonLbl = {};
	self.groupLbl = {};
	self.buttonFrame = {};
	
	local w = (#self.settings.groupData * 40) + self.leftMargin + self.rightMargin;
	if (self.orientation == _emoOptions.BarHorizontal) then
		self:SetSize(w, self.height);
	else
		self:SetSize(self.height, w);
	end

	local x = self.leftMargin;
	local cnt = #self.settings.groupData;
	for i = 1, cnt, 1 do
		if (self.settings.groupData ~= nil and self.settings.groupData[i] ~= nil and #self.settings.groupData[i].list == 0) then
			table.remove(self.settings.groupData, i);
			cnt = cnt - 1;
		end
	end
	
	for i = 1, #self.settings.groupData, 1 do
		local name = self.settings.groupData[i].name;
		
		local lbl = Turbine.UI.Label();
		lbl:SetParent(self);
		lbl:SetPosition(x, 0);
		lbl:SetSize(40, 48);
		lbl:SetBackground("FrelledPlugins/Emo/Resources/horizontal-button-frame.tga");
		lbl:SetZOrder(5);
		lbl:SetMouseVisible(false);
		lbl:SetVisible(true);
		self.buttonFrame[name] = lbl;

		lbl = Turbine.UI.Label();
		lbl:SetFont(Turbine.UI.Lotro.Font.Verdana10);
		lbl:SetParent(self);
		lbl:SetPosition(x + 5, 14); -- 6,6?
		lbl:SetSize(30, 30);
--		lbl:SetBackColor(Turbine.UI.Color(0, 1, 0, 1));
		lbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleCenter);
		lbl:SetZOrder(5);
		lbl:SetMouseVisible(false);
		lbl:SetVisible(true);
		self.buttonLbl[name] = lbl;

		lbl = Turbine.UI.Label();
		lbl:SetFont(Turbine.UI.Lotro.Font.Verdana10);
		lbl:SetParent(self);
		lbl:SetPosition(x + 5, 3);
		lbl:SetSize(30, 9);
--		lbl:SetBackColor(Turbine.UI.Color(0, 1, 0, 1));
		lbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleCenter);
		lbl:SetZOrder(5);
		lbl:SetMouseVisible(false);
		lbl:SetMultiline(false);
		lbl:SetForeColor(Turbine.UI.Color(1, 1, 1, 0.5));
		lbl:SetText(name);
		lbl:SetVisible(true);
		self.groupLbl[name] = lbl;

		local group = EmoGroup(name);
		group:AddEmoteList(self.settings.groupData[i].list);
		group.GroupNameChanged = function(sender, args)
			self.group[args.newName] = self.group[args.oldName];
			self.group[args.oldName] = nil;
			self.groupLbl[args.newName] = self.groupLbl[args.oldName];
			self.groupLbl[args.oldName] = nil;
			self.groupLbl[args.newName]:SetText(args.newName);
			self.buttonLbl[args.newName] = self.buttonLbl[args.oldName];
			self.buttonLbl[args.oldName] = nil;
			self.buttonFrame[args.newName] = self.buttonFrame[args.oldName];
			self.buttonFrame[args.oldName] = nil;
			self:SaveSettings();
		end
		local groupSlot = EmoGroupSlot(group);
		groupSlot:SetParent(self);
		groupSlot:SetZOrder(2);
		groupSlot:SetSize(self.groupSlotSize, self.groupSlotSize);
		if (self.orientation == _emoOptions.BarHorizontal) then
			groupSlot:SetPosition(x + 4, 13);
		else
			groupSlot:SetPosition(9, x + 2);
		end
		groupSlot:SetVisible(true);
		groupSlot.TextChanged = function(sender, args)
			local txt = "?";
			if (self.buttonLbl[args.name] ~= nil) then
				if (args.action ~= nil and args.action ~= "") then
					txt = string.sub(string.gsub(args.action, "_", "\n"), 2);
				end
			end
			self.buttonLbl[args.name]:SetText(txt);
		end
		groupSlot.Save = function(sender, args)
			if (#sender:GetGroup():GetEmoteList() == 0) then
				self:RemoveGroup(sender:GetGroup():GetGroupName());
			end
			self:SaveSettings();
		end
		self.group[name] = groupSlot;

		groupSlot.TextChanged(self, {name = name, action = groupSlot:GetCurrentAction()});
		
		x = x + 40;
	end
	
	self.horizRight:SetPosition(x, 0);
end

function EmoBar:RemoveGroup(name)
	local ret = false;

	for i = 1, #self.settings.groupData, 1 do
		if (self.settings.groupData[i].name == name) then
			table.remove(self.settings.groupData, i);
			self:DeleteGroupsUI(name);
			ret = true;
			self:SaveSettings();
			self:CreateGroups();
			break;
		end
	end

	return ret;
end

function EmoBar:HasGroup(name)
	return (self.group ~= nil and self.group[name] ~= nil);
end

function EmoBar:AddNewGroup()
	local newname;
	local cnt = 0;
	while (1) do
		newname = "NEW"..cnt;
		if (self.group == nil or self.group[newname] == nil) then
			break;
		end
		cnt = cnt + 1;
	end
	table.insert(self.settings.groupData, {
		name = newname,
		list = {
			"/fishslap",
		},
	});
end

function EmoBar:AddDefaultGroups(name, createGroups)
	local appendName;
	appendName = "";
	
	if (name ~= nil) then
		local found;
		local test = name;
		while (true) do
			found = false;
			for key, val in ipairs(self.settings.groupData) do
				if (val.name == test) then
					appendName = appendName .. "1";
					test = test .. appendName;
					found = true;
					break;
				end
			end
			if (found == false) then
				break;
			end
		end
	end
	
	if (name == nil or name == "Greet") then
		table.insert(self.settings.groupData, {
			name = "Greet"..appendName,
			list = {
				"/bow",
				"/burp",
				"/curtsey",
				"/drink",
				"/feather",
				"/hail",
				"/salute",
				"/swordsalute",
				"/wave",
			}
		});
	end
	
	if (name == nil or name == "Bored") then
		table.insert(self.settings.groupData, {
			name = "Bored"..appendName,
			list = {
				"/burp",
				"/charge",
				"/confused",
				"/cry",
				"/drink",
				"/drunk",
				"/dustoff",
				"/eat",
				"/feather",
				"/fidget",
				"/fight",
				"/firebreath",
				"/flex",
				"/flip",
				"/handstand",
				"/heropose",
				"/juggle",
				"/jump",
				"/meditate",
				"/mood_mischievous",
				"/mumble",
				"/munch",
				"/pick",
				"/pose",
				"/pushups",
				"/roar",
				"/scratch",
				"/sing",
				"/smoke1",
				"/spin",
				"/story",
				"/swordsalute",
				"/think",
				"/whistle"
			}
		});
	end
	
	if (name == nil or name == "Moods") then
		table.insert(self.settings.groupData, {
			name = "Moods"..appendName,
			list = {
				"/mood_angry",
				"/mood_apprehensive",
				"/mood_calm",
				"/mood_confused",
				"/mood_fearful",
				"/mood_happy",
				"/mood_mischievous",
				"/mood_sad",
				"/mood_sleepy",
				"/mood_solemn",
				"/mood_surprised",
			}
		});
	end
	
	if (name == nil or name == "Dance") then
		table.insert(self.settings.groupData, {
			name = "Dance"..appendName,
			list = {
				"/dance",
				"/dance_dwarf",
				"/dance_dwarf2",
				"/dance_elf",
				"/dance_elf2",
				"/dance_hobbit",
				"/dance_hobbit2",
				"/dance_jig",
				"/dance_man",
				"/dance_man2",
				"/dance1",
				"/dance2",
				"/dance3",
			}
		});
	end
	
	if (name == nil or name == "Bother") then
		table.insert(self.settings.groupData, {
			name = "Bother"..appendName,
			list = {
				"/ahem",
				"/attack",
				"/beckon",
				"/beg",
				"/bother",
				"/burp",
				"/challenge",
				"/charge",
				"/cough",
				"/crazy",
				"/fight",
				"/firebreath",
				"/fishslap",
				"/impatient",
				"/juggle",
				"/mock",
				"/mood_angry",
				"/pat",
				"/point",
				"/poke",
				"/roar",
				"/rude",
				"/shakefist",
				"/slap",
				"/sneeze",
				"/tantrum",
				"/tickle",
				"/whippitydo",
				"/whistle",
			}
		});
	end
	
	if (name == nil or name == "Frustrated") then
		table.insert(self.settings.groupData, {
			name = "Frustrated"..appendName,
			list = {
				"/angry",
				"/attack",
				"/challenge",
				"/charge",
				"/fight",
				"/fishslap",
				"/grumble",
				"/mood_angry",
				"/no",
				"/roar",
				"/rude",
				"/scold",
				"/shakefist",
				"/sigh",
				"/slap",
				"/tantrum",
			}
		});
	end
	
	if (name == nil or name == "Deedmotes") then
		table.insert(self.settings.groupData, {
			name = "Deedmotes"..appendName,
			list = {
				"/angry",
				"/beg",
				"/bored",
				"/bow",
				"/cheer",
				"/confused",
				"/cower",
				"/flirt",
				"/kiss",
				"/laugh",
				"/mock",
				"/rude",
				"/salute",
				"/scold",
				"/surrender",
				"/thank",
			}
		});
	end
--[[
		table.insert(self.settings.groupData, {
			name = ""..appendName,
			list = {
			}
		});
--]]

	if (createGroups ~= nil and createGroups == true) then
		self:CreateGroups();
		self:SaveSettings();
	end
end

function EmoBar:SetClickAction(action)
	self.action = action;
	-- need to tell all the quickslots
	-- perhaps make this a per group setting instead?
end

function EmoBar:SetOrientation(orientation)
	self.orientation = orientation;
	-- need to re-layout
end

function EmoBar:LoadSettings()
	-- load the settings.  If a value is not available, set a default value
	self.loading = true;

	self.settings = Turbine.PluginData.Load(Turbine.DataScope.Character, "EmoBarSettings");

	if (type(self.settings) ~= "table") then
		self.settings = {};
	end

	if (not self.settings.positionX) then
		self.settings.positionX = Turbine.UI.Display.GetWidth() - self:GetWidth() - 250; 
	end

	if (not self.settings.positionY) then
		self.settings.positionY = Turbine.UI.Display.GetHeight() - self:GetHeight() - 150 * 1.5;
	end

	if (not self.settings.groupData or #self.settings.groupData == 0) then
		self.settings.groupData = Turbine.PluginData.Load(Turbine.DataScope.Account, "EmoGroups");
		if (not self.settings.groupData or #self.settings.groupData == 0) then
			self.settings.groupData = {};
			self:AddDefaultGroups();
		end
	end

	self.loading = false;
end

function EmoBar:SaveSettings()
	if (self.loading) then
		return;
	end
	
	self.settings.groupData = nil;

	-- save the character settings (no group data)
	Turbine.PluginData.Save(Turbine.DataScope.Character, "EmoBarSettings", self.settings);

	self.settings.groupData = {};
	for k, v in pairs(self.group) do
		table.insert(self.settings.groupData, {
			name = v:GetGroup():GetGroupName(),
			list = v:GetGroup():GetEmoteList(),
		});
	end

	-- save the server settings (group data)
	Turbine.PluginData.Save(Turbine.DataScope.Account, "EmoGroups", self.settings.groupData);
end
