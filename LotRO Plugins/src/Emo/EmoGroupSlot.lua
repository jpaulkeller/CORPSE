import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";

local function out(msg)
	Turbine.Shell.WriteLine(msg);
end

EmoGroupSlot = class();

function EmoGroupSlot:Constructor(group)
	self.group = group;
	self.curWidth = 36;
	self.curHeight = 36;
	self.curX = 0;
	self.curY = 0;
	self.curParent = nil;
	self.curZ = 1;
	self.quickslot = {};
	self.TextChanged = nil;
	
	self:CreateQuickslots();

	self.contextMenu = Turbine.UI.ContextMenu();
	local menuItems = self.contextMenu:GetItems();
	menuItems:Add(Turbine.UI.MenuItem("Edit Group"));
	menuItems:Add(Turbine.UI.MenuItem("Add New Group"));
	menuItems:Add(Turbine.UI.MenuItem("Delete Group"));
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
	local mCnt = 1;
	local menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoGroupEdit:SetGroup(self.group);
		_emoGroupEdit:Show();
		_emoGroupEdit.Save = function(sender, args)
			local ret = true;
			local msg = nil;
			
			-- first to check is if the name changed
			if (args.id ~= args.name) then
				if (_emoBar:HasGroup(args.name)) then
					ret = false;
					msg = "'"..args.name.."' already exists.";
				else
					self.group:SetGroupName(args.name);
				end
			end

			-- save any changes to the actual group
			self.group:SetEmoteList(args.groupList);
			self:CreateQuickslots();
			
			if (ret == true) then
				if (type(self.Save) == "function") then
					self.Save(self, nil);
				end
				_emoGroupEdit:SetGroup(nil);
			end
			
			return ret, msg;
		end
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoBar:AddNewGroup();
		_emoBar:CreateGroups();
		_emoBar:SaveSettings();
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoBar:RemoveGroup(self.group:GetGroupName());
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoEmoteEdit:Show();
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoOptions:SetVisible(true);
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoManual:SetVisible(true);
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		out("----");
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoBar:AddDefaultGroups("Bored", true);
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoBar:AddDefaultGroups("Bother", true);
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoBar:AddDefaultGroups("Dance", true);
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoBar:AddDefaultGroups("Deedmotes", true);
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoBar:AddDefaultGroups("Frustrated", true);
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoBar:AddDefaultGroups("Greet", true);
	end
	mCnt = mCnt + 1;
	menuItem = menuItems:Get(mCnt);
	menuItem.Click = function(sender, args)
		_emoBar:AddDefaultGroups("Moods", true);
	end
end

function EmoGroupSlot:Deconstructor()
	for k, v in pairs(self.quickslot) do
		v:SetVisible(false);
		v:SetParent(nil);
		self.quickslot[k] = nil;
	end
	self.quickslot = nil;
	self.curParent = nil;
	self.group = nil;
	self.TextChanged = nil;
	self.contextMenu = nil;
	self.currentSlot = nil;
end

function EmoGroupSlot:CreateQuickslots()
	for k, v in pairs(self.quickslot) do
		v:SetVisible(false);
		v:SetParent(nil);
		self.quickslot[k] = nil;
	end
	self.quickslot = nil;
	self.quickslot = {};
	for k, v in pairs(self.group:GetEmoteList()) do
		local qs = Turbine.UI.Lotro.Quickslot();
		qs:SetVisible(false);
		qs:SetSize(self.curWidth, self.curHeight);
		qs:SetPosition(self.curX, self.curY);
		qs:SetZOrder(self.curZ);
		qs:SetUseOnRightClick(false);
		local alias;
		if (_emotes[v] ~= nil) then
			if (_emotes[v].custom == false) then
				alias = v;
			else
				alias = _emotes[v].emote;
				if (_emotes[v].no_target ~= nil) then
					alias = alias .. " " .. _emotes[v].no_target;
				end
			end
		else
			alias = "!Error!";
		end
		qs:SetShortcut(
			Turbine.UI.Lotro.Shortcut(
				Turbine.UI.Lotro.ShortcutType.Alias,
				alias
			)
		);
		qs.MouseClick = function(sender, args)
			if (args.Button == Turbine.UI.MouseButton.Left) then
				self:UpdateShortcut();
			elseif (args.Button == Turbine.UI.MouseButton.Right) then
				self.contextMenu:ShowMenu();
			end
		end
		qs.MouseWheel = function(sender, args)
			self:UpdateShortcut(args.Direction);
		end
		qs:SetParent(self.curParent);
		self.quickslot[v] = qs;
	end
	self:UpdateShortcut();
end

function EmoGroupSlot:SetSize(width, height)
	for k, v in pairs(self.quickslot) do
		v:SetSize(width, height);
	end
	self.curWidth = width;
	self.curHeight = height;
end

function EmoGroupSlot:SetZOrder(z)
	for k, v in pairs(self.quickslot) do
		v:SetSize(z);
	end
	self.curZ = z;
end

function EmoGroupSlot:SetParent(parent)
	for k, v in pairs(self.quickslot) do
		v:SetParent(parent);
	end
	self.curParent = parent;
end

function EmoGroupSlot:SetPosition(x, y)
	for k, v in pairs(self.quickslot) do
		v:SetPosition(x, y);
	end
	self.curX = x;
	self.curY = y;
end

function EmoGroupSlot:SetVisible(visible)
	for k, v in pairs(self.quickslot) do
		v:SetVisible(false);
	end
	if (visible == true and self.currentSlot ~= nil) then
		self.quickslot[self.currentSlot]:SetVisible(true);
	end
end

function EmoGroupSlot:SetGroup(group)
	self.group = nil;
	self.group = group;
	self:CreateQuickslots();
end

function EmoGroupSlot:GetGroup()
	return self.group;
end

function EmoGroupSlot:GetCurrentAction()
	return self.currentSlot;
end

function EmoGroupSlot:UpdateShortcut(direction)
	if (self.group ~= nil) then
		local alias = self.group:GetNextEmote(_emoOptions:GetClickAction(), direction);
		if (self.quickslot[alias] ~= nil) then
			self.currentSlot = alias;
		else
			self.currentSlot = nil;
		end
		self:SetVisible(true);
	else
		self.currentSlot = nil;
		self:SetVisible(false);
	end

	if (self.TextChanged ~= nil and type(self.TextChanged) == "function") then
		self.TextChanged(self, {name = self.group:GetGroupName(), action = self.currentSlot});
	end
end
