import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";
import "Palantiri.FMHelper.Data";
import "Palantiri.FMHelper.UI.ComboBox";
import "Palantiri.FMHelper.UI.Label";
import "Palantiri.FMHelper.UI.ListBox";
import "Palantiri.FMHelper.UI.ListBoxItem"

-- TODO
-- background image for main panel
-- icon (quarted FM symbols)
-- options
-- font

GUI = class(Turbine.UI.Lotro.Window);

function GUI:Constructor()
	Turbine.UI.Lotro.Window.Constructor(self);
	
	-- load the last settings saved
	self.loading = false;
	self:LoadSettings();

	-- set window properties
	local w = 600;
	local h = 300;
	self:SetText("FM Helper");
	self:SetSize(w, h);
	self:SetBackColor(Turbine.UI.Color(0, 0, 0, 0));
	self:SetPosition(self.settings.positionX, self.settings.positionY);
	self:SetOpacity(1);
	self:SetAllowDrop(false);
	self.focusBgColor = Turbine.UI.Color(0.3, 0.5, 0.5, 0.5);

	local margin = 20;
	local y = 35;
	
	-- Build the UI
	
	self.channelLabel = Palantiri.FMHelper.UI.Label();
	self.channelLabel:SetParent(self);
	self.channelLabel:SetSize(120, 20);
	self.channelLabel:SetPosition(margin, y);
	self.channelLabel:SetText("Channel:");
	self.channelLabel:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	
	self.channelCombo = Palantiri.FMHelper.UI.ComboBox();
	self.channelCombo:SetParent(self);
	self.channelCombo:SetSize(100, 20);
	self.channelCombo:SetPosition(80, y);
	
    -- add the elements to a set, and sort them
    local set = {}
	for _, channel in ipairs(channelTable) do
      item = self.channelCombo:AddItem(channel.label, channel);
	end
	
	self.channelCombo:SetSelection(self.settings.channel);
	self.channelCombo.SelectedIndexChanged = function(sender, args)
       -- self.settings.channel = args.selection;
	   self.settings.channel = self.channelCombo:GetSelection(); -- TODO
       self:SaveSettings();
	end
	y = y + 30;
	
	local fmWidth = 110;
	self.fmList = Palantiri.FMHelper.UI.ListBox();
	self.fmList:SetParent(self);
	self.fmList:SetSize(fmWidth, h - y - 20);
	self.fmList:SetPosition(margin, y);

    -- add the elements to a set, and sort them
    local set = {}
	for _, rec in ipairs(fmTable) do set[rec.name] = true; end
	local fms = {}
	for pair in pairs (set) do table.insert(fms, pair); end
	table.sort(fms);
    
	for i, fm in pairs (fms) do
        self.fmList:AddText(fm);
	end
	
	self.fmList.ItemSelected = function(sender, args)
	   self.fm = args.selection;
       Turbine.Shell.WriteLine(self.settings.channel.to .. " " .. self.fm);
	end
	
	self.Closing = function(sender, args)
		local curX, curY = self:GetPosition();
		self.settings.positionX = curX;
		self.settings.positionY = curY;
		self:SaveSettings();
	end
end

function GUI:LoadSettings()
	self.loading = true;

	self.settings = Turbine.PluginData.Load(Turbine.DataScope.Character, "PalantiriFMHelperSettings");
	if (type(self.settings) ~= "table") then
		self.settings = {};
	end

	if (not self.settings.positionX) then
		self.settings.positionX = 100; 
	end
	if (not self.settings.positionY) then
		self.settings.positionY = 100;
	end
	
	if (not self.settings.channel) then
		self.settings.channel = {label="Default", to=""};
	end

	self.loading = false;
end

function GUI:SaveSettings()
	if (self.loading) then
		return;
	end
	
	Turbine.PluginData.Save(Turbine.DataScope.Character, "PalantiriFMHelperSettings", self.settings);
end
