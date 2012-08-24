import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";

local function out(msg)
	Turbine.Shell.WriteLine(msg);
end

EmoGroupEditWindow = class(Turbine.UI.Lotro.Window);

function EmoGroupEditWindow:Constructor()
	Turbine.UI.Lotro.Window.Constructor(self);

	self.loading = false;
	self:LoadSettings();

	local w = 600;
	local h = 400;
	
	self:SetSize(w, h);
	self:SetPosition(self.settings.positionX, self.settings.positionY);
	self:SetText("Edit Emo Group: UNTITLED");
	self:SetAllowDrop(false);
	
	local horizMargin = 20;
	local vertMargin = 35;
	local y = vertMargin;
	local lbl;
	
	-- Group name row
	lbl = FrelledPlugins.Emo.UI.Label();
	lbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	lbl:SetText("Group Title:");
	lbl:SetSize(100, 20);
	lbl:SetPosition(horizMargin + (w / 4), y);
	lbl:SetParent(self);
	lbl:SetMultiline(false);
	
	self.titleTB = Turbine.UI.TextBox();
	self.titleTB:SetBackColor(Turbine.UI.Color(0.2, 0.2, 0.2));
	self.titleTB:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	self.titleTB:SetFont(Turbine.UI.Lotro.Font.Verdana14);
	self.titleTB:SetText("");
	self.titleTB:SetSize((w / 2) - 25 - lbl:GetWidth() - horizMargin, 20);
	self.titleTB:SetPosition(lbl:GetWidth() + horizMargin + (w / 4), y);
	self.titleTB:SetParent(self);

	y = y + 30;
	
	-- LB Title row
	lbl = FrelledPlugins.Emo.UI.Label();
	lbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	lbl:SetText("Available Emotes");
	lbl:SetSize(200, 20);
	lbl:SetPosition(horizMargin, y);
	lbl:SetForeColor(Turbine.UI.Color(1, 1, 0));
	lbl:SetParent(self);
	lbl:SetMultiline(false);
	
	lbl = FrelledPlugins.Emo.UI.Label();
	lbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	lbl:SetText("Group Emotes");
	lbl:SetSize(200, 20);
	lbl:SetPosition((w / 2) + 25, y);
	lbl:SetForeColor(Turbine.UI.Color(1, 1, 0));
	lbl:SetParent(self);
	lbl:SetMultiline(false);

	y = y + 30;

	-- List Box row
	self.availableLB = FrelledPlugins.Emo.UI.ListBox();
	self.availableLB:SetParent(self);
	self.availableLB:SetBackColor(Turbine.UI.Color(0.2, 0.2, 0.2));
	self.availableLB:SetPosition(horizMargin, y);
	self.availableLB:SetSize((w / 2) - horizMargin - 25, h - y - vertMargin - 20);
	self.availableLB.SelectionChanged = function(sender, args)
		if (args >= 1) then
			self.groupLB:SetSelection(0);
		end
	end

	self.groupLB = FrelledPlugins.Emo.UI.ListBox();
	self.groupLB:SetParent(self);
	self.groupLB:SetBackColor(Turbine.UI.Color(0.2, 0.2, 0.2));
	self.groupLB:SetPosition((w / 2) + 25, y);
	self.groupLB:SetSize((w / 2) - horizMargin - 25, h - y - vertMargin - 20);
	self.groupLB.SelectionChanged = function(sender, args)
		if (args >= 1) then
			self.availableLB:SetSelection(0);
		end
	end
	
	y = y + self.groupLB:GetHeight() + 10;
	
	-- Buttons in the middle
	self.rightButton = Turbine.UI.Lotro.Button();
	self.rightButton:SetFont(Turbine.UI.Lotro.Font.Verdana12);
	self.rightButton:SetFontStyle(Turbine.UI.FontStyle.Outline);
	self.rightButton:SetText(">");
	self.rightButton:SetSize(30, 30);
	self.rightButton:SetPosition((w / 2) - (self.rightButton:GetWidth() / 2) + 1, (h / 2) - 25);
	self.rightButton:SetParent(self);
	self.rightButton.Click = function (sender, args)
		local index = self.availableLB:GetSelection();
		if (index >= 1) then
			local txt = self.availableLB:GetItem(index):GetText();
			self.availableLB:RemoveItemAt(index);
			self.groupLB:AddText(txt);

			self.availableLB:SortItems();
			self.groupLB:SortItems();
			
			self.availableLB:SetSelection(0);
			self.groupLB:SetSelection(self.groupLB:FindText(txt));
		end
	end

	self.leftButton = Turbine.UI.Lotro.Button();
	self.leftButton:SetFont(Turbine.UI.Lotro.Font.Verdana12);
	self.leftButton:SetFontStyle(Turbine.UI.FontStyle.Outline);
	self.leftButton:SetText("<");
	self.leftButton:SetSize(30, 30);
	self.leftButton:SetPosition((w / 2) - (self.leftButton:GetWidth() / 2) + 1, (h / 2) + 25);
	self.leftButton:SetParent(self);
	self.leftButton.Click = function (sender, args)
		local index = self.groupLB:GetSelection();
		if (index >= 1) then
			local txt = self.groupLB:GetItem(index):GetText();
			self.groupLB:RemoveItemAt(index);
			self.availableLB:AddText(txt);

			self.groupLB:SortItems();
			self.availableLB:SortItems();
			
			self.groupLB:SetSelection(0);
			self.availableLB:SetSelection(self.availableLB:FindText(txt));
		end
	end

	-- Buttons at the bottom
	self.saveButton = Turbine.UI.Lotro.Button();
	self.saveButton:SetFont(Turbine.UI.Lotro.Font.Verdana12);
	self.saveButton:SetFontStyle(Turbine.UI.FontStyle.Outline);
	self.saveButton:SetText("Save");
	self.saveButton:SetSize(60, 30);
	self.saveButton:SetPosition(w - horizMargin - (self.saveButton:GetWidth() * 2) - 10, y);
	self.saveButton:SetParent(self);
	self.saveButton.Click = function (sender, args)
		if (type(self.Save) == "function") then
			local ret, msg = self.Save(self,
				{
					id = self.group:GetGroupName(),
					name = self.titleTB:GetText(),
					availableList = self.availableLB:GetList(),
					groupList = self.groupLB:GetList()
				}
			);
			if (ret == true) then
				self:SetVisible(false);
			else
				if (msg ~= nil) then
					out("Error: "..msg);
				end
			end
		end
	end
	
	self.cancelButton = Turbine.UI.Lotro.Button();
	self.cancelButton:SetFont(Turbine.UI.Lotro.Font.Verdana12);
	self.cancelButton:SetFontStyle(Turbine.UI.FontStyle.Outline);
	self.cancelButton:SetText("Cancel");
	self.cancelButton:SetSize(60, 30);
	self.cancelButton:SetPosition(w - horizMargin - self.saveButton:GetWidth(), y);
	self.cancelButton:SetParent(self);
	self.cancelButton.Click = function (sender, args)
		self:SetVisible(false);
	end
	
	-- Initialize to an empty group
	self:SetGroup(EmoGroup("UNTITLED"));

	self.Closing = function(sender, args)
		local curX, curY = self:GetPosition();
		self.settings.positionX = curX;
		self.settings.positionY = curY;
		self:SaveSettings();
		self:SetGroup(nil);
	end
end

function EmoGroupEditWindow:Show()
	_emoEmoteEdit:SetVisible(false);
	_emoGroupEdit:SetVisible(true);
end

function EmoGroupEditWindow:SetGroup(group)
	-- remove all refs to the current group
	self:SetText("Emo Edit Group: UNTITLED");
	self.titleTB:SetText("UNTITLED");
	self.availableLB:RemoveAll();
	self.groupLB:RemoveAll();
	self.group = nil;
	
	if (group ~= nil) then
		-- populate the UI based on the group
		self.group = group;
		self:SetText("Emo Edit Group: "..group:GetGroupName());
		self.titleTB:SetText(group:GetGroupName());
		self.availableLB:AddText(self.group:GetAvailableEmoteList());
		self.groupLB:AddText(self.group:GetEmoteList());
		self.availableLB:SetSelection(1);
		self.groupLB:SetSelection(0);
	end
end

function EmoGroupEditWindow:LoadSettings()
	-- load the settings.  If a value is not available, set a default value
	self.loading = true;

	self.settings = Turbine.PluginData.Load(Turbine.DataScope.Character, "EmoGroupEditWindowSettings");

	if (type(self.settings) ~= "table") then
		self.settings = {};
	end

	if (not self.settings.positionX) then
		self.settings.positionX = 100; 
	end
	if (not self.settings.positionY) then
		self.settings.positionY = 100;
	end
	
	self.loading = false;
end

function EmoGroupEditWindow:SaveSettings()
	if (self.loading) then
		return;
	end

	Turbine.PluginData.Save(Turbine.DataScope.Character, "EmoGroupEditWindowSettings", self.settings);
end
