import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";

EmoOptionsWindow = class(Turbine.UI.Lotro.Window);

-- enums
EmoOptionsWindow.ClickActionRandom = 1;
EmoOptionsWindow.ClickActionCycle = 2;

EmoOptionsWindow.BarHorizontal = 1;
EmoOptionsWindow.BarVertical = 2;

function EmoOptionsWindow:Constructor()
	Turbine.UI.Lotro.Window.Constructor(self);
	
	-- load the last settings saved
	self.loading = false;
	self:LoadSettings();

	--[[ set window properties ]]--
	local w = 310;
	local h = 150;
	self:SetSize(w, h);
	self:SetBackColor(Turbine.UI.Color(0, 0, 0, 0));
	self:SetPosition(self.settings.positionX, self.settings.positionY);
	self:SetOpacity(1);
	self:SetText("Emo Options");
	self:SetAllowDrop(false);
--	self:SetZOrder(10);

	self.focusBgColor = Turbine.UI.Color(0.3, 0.5, 0.5, 0.5);

	local margin = 20;
	local y = 35;
	
	-- Build the UI
	self.clickActionLbl = FrelledPlugins.Emo.UI.Label();
	self.clickActionLbl:SetParent(self);
	self.clickActionLbl:SetSize(120, 20);
	self.clickActionLbl:SetPosition(margin, y);
	self.clickActionLbl:SetText("Click Action:");
	self.clickActionLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	self.clickActionCombo = FrelledPlugins.Emo.UI.ComboBox();
	self.clickActionCombo:SetParent(self);
	self.clickActionCombo:SetSize(140, 20);
	self.clickActionCombo:SetPosition(145, y);
	self.clickActionCombo:AddItem("Random", self.ClickActionRandom);
	self.clickActionCombo:AddItem("Cycle", self.ClickActionCycle);
	self.clickActionCombo:SetSelection(self.settings.clickAction);
	self.clickActionCombo.SelectedIndexChanged = function(sender, args)
		self:SaveSettings();
		if (_emoBar ~= nil) then
			_emoBar:SetClickAction(args.selection);
		end
	end
	y = y + 30;
	
	self.barOrientationLbl = FrelledPlugins.Emo.UI.Label();
	self.barOrientationLbl:SetParent(self);
	self.barOrientationLbl:SetSize(120, 20);
	self.barOrientationLbl:SetPosition(margin, y);
	self.barOrientationLbl:SetText("Bar Orientation:");
	self.barOrientationLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	self.barOrientationCombo = FrelledPlugins.Emo.UI.ComboBox();
	self.barOrientationCombo:SetParent(self);
	self.barOrientationCombo:SetSize(140, 20);
	self.barOrientationCombo:SetPosition(145, y);
	self.barOrientationCombo:AddItem("Horizontal", self.BarHorizontal);
--	self.barOrientationCombo:AddItem("Vertical", self.BarVertical);
	self.barOrientationCombo:SetSelection(self.settings.barOrientation);
	self.barOrientationCombo.SelectedIndexChanged = function(sender, args)
		self:SaveSettings();
		if (_emoBar ~= nil) then
			_emoBar:SetOrientation(args.selection);
		end
	end
	y = y + 30;

	self.autoShowBarCB = FrelledPlugins.Emo.UI.CheckBox();
	self.autoShowBarCB:SetCheckBoxLocation(FrelledPlugins.Emo.UI.CheckBox.LocationLeft);
	self.autoShowBarCB:SetParent(self);
	self.autoShowBarCB:SetText("Auto Show Emo Bar on Load");
	self.autoShowBarCB:SetPosition(margin, y);
	if (self.settings.autoShowBar == 1) then
		self.autoShowBarCB:SetChecked(true);
	else
		self.autoShowBarCB:SetChecked(false);
	end
	self.autoShowBarCB:SetSize(w - margin * 2, 20);
	self.autoShowBarCB.Click = function(sender, args)
		self:SaveSettings();
	end
	y = y + 30;

	self.Closing = function(sender, args)
		local curX, curY = self:GetPosition();
		self.settings.positionX = curX;
		self.settings.positionY = curY;
		self:SaveSettings();
	end
end

function EmoOptionsWindow:GetClickAction()
	return self.clickActionCombo:GetSelection();
end

function EmoOptionsWindow:GetBarOrientation()
	return self.barOrientationCombo:GetSelection();
end

function EmoOptionsWindow:GetAutoShowBar()
	return self.autoShowBarCB:IsChecked();
end

function EmoOptionsWindow:LoadSettings()
	-- load the settings.  If a value is not available, set a default value
	self.loading = true;

	self.settings = Turbine.PluginData.Load(Turbine.DataScope.Character, "EmoOptionsWindowSettings");

	if (type(self.settings) ~= "table") then
		self.settings = {};
	end

	if (not self.settings.positionX) then
		self.settings.positionX = 100; 
	end
	if (not self.settings.positionY) then
		self.settings.positionY = 100;
	end
	
	if (not self.settings.autoShowBar) then
		self.settings.autoShowBar = 1;
	end
	if (not self.settings.clickAction) then
		self.settings.clickAction = self.ClickActionRandom;
	end
	
	if (not self.settings.barOrientation) then
		self.settings.barOrientation = self.BarHorizontal;
	end

	self.loading = false;
end

function EmoOptionsWindow:SaveSettings()
	if (self.loading) then
		return;
	end
	
	if (self.autoShowBarCB:IsChecked() == true) then
		self.settings.autoShowBar = 1;
	else
		self.settings.autoShowBar = 2;
	end
	self.settings.clickAction = self.clickActionCombo:GetSelection();
	self.settings.barOrientation = self.barOrientationCombo:GetSelection();
	
	-- save the settings
	Turbine.PluginData.Save(Turbine.DataScope.Character, "EmoOptionsWindowSettings", self.settings);
end
