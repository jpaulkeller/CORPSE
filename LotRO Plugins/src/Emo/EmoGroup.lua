import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";

local function out(msg)
	Turbine.Shell.WriteLine(msg);
end

EmoGroup = class();

function EmoGroup:Constructor(groupName)
	self.groupName = groupName;
	self._noUpdate = false;
	self.index = 0;
	
	self.groupEmoteList = {};

	self.availableEmoteList = {};
	
	for k, v in pairs(_emotes) do
		self.availableEmoteList[k] = true;
	end
end

function EmoGroup:Destructor()
	for k in pairs(self.groupEmoteList) do
		self.groupEmoteList[k] = nil;
	end
	self.groupEmoteList = nil;
	for k in pairs(self.availableEmoteList) do
		self.availableEmoteList[k] = nil;
	end
	self.availableEmoteList = nil;
	self.indexedEmoteList = nil;
	self.groupName = nil;
end

function EmoGroup:SetGroupName(name)
	local oname = self.groupName;
	self.groupName = name;
	if (type(self.GroupNameChanged) == "function") then
		self.GroupNameChanged(self, { oldName = oname, newName = name});
	end
end

function EmoGroup:GetGroupName()
	return self.groupName;
end

function EmoGroup:UpdateGameEmotes(emote)
	if (emote == nil) then
		for k, v in pairs(_emotes) do
			if (self.groupEmoteList[k] == nil and self.availableEmoteList[k] == nil) then
				self.availableEmoteList[k] = true;
			end
		end
		for k, v in pairs(self.groupEmoteList) do
			if (_emotes[k] == nil) then
				self.groupEmoteList[k] = nil;
			end
		end
		for k, v in pairs(self.availableEmoteList) do
			if (_emotes[k] == nil) then
				self.availableEmoteList[k] = nil;
			end
		end
	else
		if (self.groupEmoteList[emote] == nil) then
			self.availableEmoteList[emote] = true;
		end
	end
	self:UpdateList();
end

function EmoGroup:NewEmote(emoteName)
	if (self.groupEmoteList[emoteName] == nil) then
		self.availableEmoteList[emoteName] = true;
	end
end

function EmoGroup:AddEmote(emoteName)
	local ret = false;

	if (self.availableEmoteList[emoteName] ~= nil) then
		self.groupEmoteList[emoteName] = true;
		self.availableEmoteList[emoteName] = nil;
		ret = true;
	end
	
	if (ret == true and self._noUpdate == false) then
		self:UpdateList();
	end
	
	return ret;
end

function EmoGroup:AddEmoteList(emoteList)
	local ret = false;
	self.lastError = nil;
	
	if (emoteList == nil) then
		return ret;
	end

	self._noUpdate = true;
	for k, v in pairs(emoteList) do
		ret = self:AddEmote(v);
		if (ret == false) then
			-- error is already set
			break;
		end
	end
	self._noUpdate = false;

	self:UpdateList();

	return ret;
end

function EmoGroup:SetEmoteList(emoteList)
	for k in pairs(self.groupEmoteList) do
		self.groupEmoteList[k] = nil;
		self.availableEmoteList[k] = true;
	end
	self:UpdateList();
	self:AddEmoteList(emoteList);
end

function EmoGroup:RemoveEmote(emoteName)
	local ret = false;
	if (self.groupEmoteList[emoteName] ~= nil) then
		self.groupEmoteList[emoteName] = nil;
		self.availableEmoteList[emoteName] = true;
		self:UpdateList();
		ret = true;
	end

	return ret;
end

function EmoGroup:GetEmoteList()
	local list = {};
	for k in pairs(self.groupEmoteList) do
		table.insert(list, k);
	end
	table.sort(list);
	return list;
end

function EmoGroup:GetAvailableEmoteList()
	local list = {};
	for k in pairs(self.availableEmoteList) do
		table.insert(list, k);
	end
	table.sort(list);
	return list;
end

function EmoGroup:UpdateList()
	self.indexedEmoteList = nil;
	self.indexedEmoteList = self:GetEmoteList();
end

function EmoGroup:GetNextEmote(clickAction, direction)
	local index = 1;
	
	if (#self.indexedEmoteList == 0) then
		return "";
	end

	if (direction ~= nil) then
		if (direction < 0) then
			self.index = self.index - 1;
			if (self.index < 1) then
				self.index = #self.indexedEmoteList;
			end
		else
			self.index = self.index + 1;
			if (self.index > #self.indexedEmoteList) then
				self.index = 1;
			end
		end
	elseif (clickAction == EmoOptionsWindow.ClickActionRandom) then
		self.index = math.random(1, #self.indexedEmoteList);
	elseif (clickAction == EmoOptionsWindow.ClickActionCycle) then
		if (self.index >= #self.indexedEmoteList) then
			self.index = 1;
		else
			self.index = self.index + 1;
		end
	else
		out("Error: GetNextEmote doesn't know what clickAction is");
	end

	local ret = "";
	if (self.indexedEmoteList[self.index] ~= nil) then
		ret = self.indexedEmoteList[self.index];
	end

	return ret;
end
