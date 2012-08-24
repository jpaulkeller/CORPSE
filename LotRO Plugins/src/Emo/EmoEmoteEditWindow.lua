import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";

local function out(msg)
	Turbine.Shell.WriteLine(msg);
end

EmoEmoteEditWindow = class(Turbine.UI.Lotro.Window);

function EmoEmoteEditWindow:Constructor()
	Turbine.UI.Lotro.Window.Constructor(self);

	self.loading = false;
	self:p_LoadSettings();

	local w = 600;
	local h = 400;
	
	local btnWidth = 60;
	local btnHeight = 30;
	
	self:SetSize(w, h);
	self:SetPosition(self.settings.positionX, self.settings.positionY);
	self:SetText("Customize Emote");
	self:SetAllowDrop(false);
	
	local horizMargin = 20;
	local vertMargin = 35;
	local y = vertMargin;

	self.page = {};
	
	-- ------------------------------------------------------------------
	-- PAGE 1
	local p = {};
	
	-- label of directions
	p.dirLbl = FrelledPlugins.Emo.UI.Label();
	p.dirLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleCenter);
	p.dirLbl:SetText("Select an Emote to Edit or NEW EMOTE");
	p.dirLbl:SetSize(w - (horizMargin * 2), 20);
	p.dirLbl:SetPosition(horizMargin, y);
	p.dirLbl:SetParent(self);
	p.dirLbl:SetMultiline(false);
	
	y = y + 30;
	
	-- List Box row
	p.availableLB = FrelledPlugins.Emo.UI.ListBox();
	p.availableLB:SetParent(self);
	p.availableLB:SetBackColor(Turbine.UI.Color(0.2, 0.2, 0.2));
	p.availableLB:SetPosition(horizMargin, y);
	p.availableLB:SetSize(w - (horizMargin * 2), h - y - (vertMargin * 2) - 20);
	p.availableLB.SelectionChanged = function(sender, args)
		if (args >= 1) then
			self.customizeEmote = args;
			self.page[1].nextBtn:SetEnabled(true);
		else
			self.customizeEmote = args;
			self.page[1].nextBtn:SetEnabled(false);
		end
	end
	
	y = y + p.availableLB:GetHeight() + 10;

	-- Buttons at the bottom
	p.nextBtn = Turbine.UI.Lotro.Button();
	p.nextBtn:SetFont(Turbine.UI.Lotro.Font.Verdana12);
	p.nextBtn:SetFontStyle(Turbine.UI.FontStyle.Outline);
	p.nextBtn:SetText("Next");
	p.nextBtn:SetSize(btnWidth, btnHeight);
	p.nextBtn:SetPosition(w - horizMargin - (btnWidth * 2) - 10, y);
	p.nextBtn:SetParent(self);
	p.nextBtn.Click = function (sender, args)
		self:p_ShowPage(2);
		self.page[2].emoteTB:SetText(self.availableList[self.customizeEmote]);
		if (self.customizeEmote ~= 1) then
			-- we have an existing custom emote to edit
			local anim = _emotes[self.availableList[self.customizeEmote]].emote;
			local aIndex = self.page[2].animationLB:FindText(anim);
			if (aIndex ~= -1) then
				-- found the animation index, select it
				self.page[2].animationLB:SetSelection(aIndex);
			end
			self.page[2].allTB:SetText(_emotes[self.availableList[self.customizeEmote]].no_target);
		end
	end
	
	p.cancelBtn = Turbine.UI.Lotro.Button();
	p.cancelBtn:SetFont(Turbine.UI.Lotro.Font.Verdana12);
	p.cancelBtn:SetFontStyle(Turbine.UI.FontStyle.Outline);
	p.cancelBtn:SetText("Cancel");
	p.cancelBtn:SetSize(btnWidth, btnHeight);
	p.cancelBtn:SetPosition(w - horizMargin - btnWidth, y);
	p.cancelBtn:SetParent(self);
	p.cancelBtn.Click = function (sender, args)
		self:SetVisible(false);
	end

	self.page[1] = p;

	-- ------------------------------------------------------------------
	-- PAGE 2
	p = {};
	y = vertMargin;
	
	--[[
	                Custom Emote Name: [emote_name_tb]
	Emote Animation:		No Target:
	+-----------------+		  Text to All: [no_target_tb]
	|emote1           |
	|emote2           |		Test Emote:	+====+
	|emoteN           |					{TEST}
	+-----------------+					+====+
											|Save| |Cancel|
	--]]

	-- Emote Name row
	p.emoteLbl = FrelledPlugins.Emo.UI.Label();
	p.emoteLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	p.emoteLbl:SetText("Custom Emote Name:");
	p.emoteLbl:SetSize(200, 20);
	p.emoteLbl:SetPosition((w / 2) - 200 - 5, y);
	p.emoteLbl:SetParent(self);
	p.emoteLbl:SetMultiline(false);
	
	p.emoteTB = Turbine.UI.TextBox();
	p.emoteTB:SetBackColor(Turbine.UI.Color(0.2, 0.2, 0.2));
	p.emoteTB:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	p.emoteTB:SetFont(Turbine.UI.Lotro.Font.Verdana14);
	p.emoteTB:SetText("");
	p.emoteTB:SetSize(200, 20);
	p.emoteTB:SetPosition((w / 2) + 5, y);
	p.emoteTB:SetParent(self);
	
	y = y + 30;
	
	-- more labels
	p.animLbl = FrelledPlugins.Emo.UI.Label();
	p.animLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	p.animLbl:SetText("Animation Emote:");
	p.animLbl:SetSize(200, 20);
	p.animLbl:SetPosition(horizMargin, y);
	p.animLbl:SetParent(self);
	p.animLbl:SetMultiline(false);
	
	p.noTargLbl = FrelledPlugins.Emo.UI.Label();
	p.noTargLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	p.noTargLbl:SetText("No Target:");
	p.noTargLbl:SetSize(200, 20);
	p.noTargLbl:SetPosition((w / 2) + 5, y);
	p.noTargLbl:SetParent(self);
	p.noTargLbl:SetMultiline(false);
	
	y = y + 30;
	
	-- list box and label
	local ry = y;
	
	p.animationLB = FrelledPlugins.Emo.UI.ListBox();
	p.animationLB:SetParent(self);
	p.animationLB:SetBackColor(Turbine.UI.Color(0.2, 0.2, 0.2));
	p.animationLB:SetPosition(horizMargin, y);
	p.animationLB:SetSize((w / 2) - horizMargin - 5, h - y - (vertMargin * 2) - 20);
	p.animationLB.SelectionChanged = function(sender, args)
		if (args >= 1) then
			self.animationEmote = args;
			p.saveBtn:SetEnabled(true);
		else
			self.animationEmote = args;
			p.saveBtn:SetEnabled(false);
		end
		self:p_UpdateQuickslot();
	end
	
	y = y + p.animationLB:GetHeight() + 10;

	-- All text row
	p.allLbl = FrelledPlugins.Emo.UI.Label();
	p.allLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	p.allLbl:SetText("All:");
	p.allLbl:SetSize(40, 20);
	p.allLbl:SetPosition((w / 2) + 15, ry);
	p.allLbl:SetParent(self);
	p.allLbl:SetMultiline(false);

	p.allTB = Turbine.UI.TextBox();
	p.allTB:SetWantsUpdates(true);
	p.allTB.Update = function(sender, args)
		if (self:IsVisible()) then
			local tmp = self.page[2].allTB:GetText();
			if (tmp ~= self.lastAllText) then
				self.lastAllText = tmp;
				self:p_UpdateQuickslot();
			end
		end
    end
	p.allTB:SetBackColor(Turbine.UI.Color(0.2, 0.2, 0.2));
	p.allTB:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	p.allTB:SetFont(Turbine.UI.Lotro.Font.Verdana14);
	p.allTB:SetText("");
	p.allTB:SetSize((w / 2) - 15 - 40 - vertMargin, 20);
	p.allTB:SetPosition((w / 2) + 15 + 40, ry);
	p.allTB:SetParent(self);
	p.allTB:SetMultiline(false);

	ry = ry + 30;
	
	-- Test row
	p.testLbl = FrelledPlugins.Emo.UI.Label();
	p.testLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	p.testLbl:SetText("Click to Test:");
	p.testLbl:SetSize(100, 20);
	p.testLbl:SetPosition((w / 2) + 5, ry);
	p.testLbl:SetParent(self);
	p.testLbl:SetMultiline(false);

	p.testQS = Turbine.UI.Lotro.Quickslot();
	p.testQS:SetSize(38, 38);
	p.testQS:SetPosition((w / 2) + 5 + 100, ry);
	p.testQS:SetParent(self);
	p.testQS:SetBackColor(Turbine.UI.Color(0.4, 0.4, 0.4));
	
	-- Button row at bottom
	p.saveBtn = Turbine.UI.Lotro.Button();
	p.saveBtn:SetFont(Turbine.UI.Lotro.Font.Verdana12);
	p.saveBtn:SetFontStyle(Turbine.UI.FontStyle.Outline);
	p.saveBtn:SetText("Save");
	p.saveBtn:SetSize(btnWidth, btnHeight);
	p.saveBtn:SetPosition(w - horizMargin - (btnWidth * 2) - 10, y);
	p.saveBtn:SetParent(self);
	p.saveBtn.Click = function (sender, args)
		if (type(self.Save) == "function") then
			local ret, msg = self.Save(self,
				{
					name = self.page[2].emoteTB:GetText(),
					emote = self.animationList[self.animationEmote],
					notarget_all = self.page[2].allTB:GetText(),
				}
			);
			if (msg ~= nil) then
				out(msg);
			end
			if (ret == true) then
				self:SetVisible(false);
			end
		end
	end
	
	p.cancelBtn = Turbine.UI.Lotro.Button();
	p.cancelBtn:SetFont(Turbine.UI.Lotro.Font.Verdana12);
	p.cancelBtn:SetFontStyle(Turbine.UI.FontStyle.Outline);
	p.cancelBtn:SetText("Cancel");
	p.cancelBtn:SetSize(btnWidth, btnHeight);
	p.cancelBtn:SetPosition(w - horizMargin - btnWidth, y);
	p.cancelBtn:SetParent(self);
	p.cancelBtn.Click = function (sender, args)
		self:SetVisible(false);
	end
	
	self.page[2] = p;

	-- ------------------------------------------------------------------
	-- Initialize
	self:p_PopulateLists();

	-- Show the first page
	self:p_ShowPage(1);
	
	self.Closing = function(sender, args)
		local curX, curY = self:GetPosition();
		self.settings.positionX = curX;
		self.settings.positionY = curY;
		self:p_SaveSettings();
	end
end

function EmoEmoteEditWindow:Show()
	_emoGroupEdit:SetVisible(false);
	self:p_PopulateLists();
	self:p_ShowPage(1);
	self:SetVisible(true);
end

function EmoEmoteEditWindow:p_UpdateQuickslot()
	local alias = "???";
	if (self.animationEmote >= 1 and self.animationList[self.animationEmote] ~= nil) then
		alias = self.animationList[self.animationEmote];
		if (self.page[2].allTB:GetText() ~= nil) then
			alias = alias .. " " .. self.page[2].allTB:GetText();
		end
	end
	self.page[2].testQS:SetShortcut(
		Turbine.UI.Lotro.Shortcut(
			Turbine.UI.Lotro.ShortcutType.Alias,
			alias
		)
	);
end

function EmoEmoteEditWindow:p_PopulateLists()
	-- create the available list of custom and animation emotes
	self.availableList = nil;
	self.animationList = nil;
	
	self.availableList = {};
	self.animationList = {};
	
	for k, v in pairs(_emotes) do
		if (v.custom == true) then
			table.insert(self.availableList, k);
		else
			table.insert(self.animationList, k);
		end
	end

	table.sort(self.availableList);
	table.insert(self.availableList, 1, "NEW EMOTE");

	table.sort(self.animationList);
	
	-- update the list box
	self.page[1].availableLB:RemoveAll();
	self.page[1].availableLB:AddText(self.availableList);

	self.page[2].animationLB:RemoveAll();
	self.page[2].animationLB:AddText(self.animationList);

	-- intitialize the selection
	self.page[1].availableLB:SetSelection(1);
	self.customizeEmote = 1;
	self.page[1].nextBtn:SetEnabled(true);

	self.page[2].animationLB:SetSelection(1);
	self.animationEmote = 1;
	self.page[2].saveBtn:SetEnabled(true);
end

function EmoEmoteEditWindow:p_ShowPage(show)
	for k, v in pairs(self.page) do
		local vis = (k == show);
		for kk, vv in pairs(v) do
			vv:SetVisible(vis);
		end
	end
end

function EmoEmoteEditWindow:p_LoadSettings()
	-- load the settings.  If a value is not available, set a default value
	self.loading = true;

	self.settings = Turbine.PluginData.Load(Turbine.DataScope.Character, "EmoEmoteEditWindowSettings");

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

function EmoEmoteEditWindow:p_SaveSettings()
	if (self.loading) then
		return;
	end

	Turbine.PluginData.Save(Turbine.DataScope.Character, "EmoEmoteEditWindowSettings", self.settings);
end
