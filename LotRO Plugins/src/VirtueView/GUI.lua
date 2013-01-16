import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";
import "Palantiri.VirtueView.Data";
import "Palantiri.VirtueView.UI.CheckBox";
import "Palantiri.VirtueView.UI.ComboBox";
import "Palantiri.VirtueView.UI.Label";
import "Palantiri.VirtueView.UI.ListBox";
import "Palantiri.VirtueView.UI.ListBoxItem"

-- TODO
-- background image for main panel
-- icon
-- option
-- slot for Max Level
-- Region: All, Eriador, Rhovanion
-- Zone: Bree, etc (current region)

GUI = class(Turbine.UI.Lotro.Window);

function GUI:Constructor()
	Turbine.UI.Lotro.Window.Constructor(self);
	
	-- load the last settings saved
	self.loading = false;
	self:LoadSettings();

	-- set window properties
	local w = 900;
	local h = 500;
	self:SetText("Virtue Deeds");
	self:SetSize(w, h);
	self:SetBackColor(clear);
	self:SetPosition(self.settings.positionX, self.settings.positionY);
	self:SetOpacity(1);
	self:SetAllowDrop(false);
	self.focusBgColor = Turbine.UI.Color(1, 0.5, 0.5, 0.5);

	local margin = 20;
	local x = margin + 20;
	local y = 35;
	local width;
	
	-- Build the UI
	
	self.regionLbl = Palantiri.VirtueView.UI.Label();
	self.regionLbl:SetParent(self);
	width = 80;
	self.regionLbl:SetSize(width, 20);
	self.regionLbl:SetPosition(x, y);
	x = x + width + 20;
	self.regionLbl:SetText("Region:");
	self.regionLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	
	self.regionCombo = Palantiri.VirtueView.UI.ComboBox();
	self.regionCombo:SetParent(self);
	width = 250;
	self.regionCombo:SetSize(width, 20);
	self.regionCombo:SetPosition(x, y);
	x = x + width + 20;
	
    -- add the elements to a set, and sort them
    local set = {}
	for _, rec in ipairs(virtueTable) do set[rec.region] = true; end
	regions = {}
	for pair in pairs (set) do table.insert(regions, pair); end
	table.sort(regions);
	table.insert(regions, 1, "Any");
	
	for i, region in ipairs(regions) do
      item = self.regionCombo:AddItem(region, i);
      item:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	end
	
	self.regionCombo:SetSelection(self.settings.region);
	self.regionCombo.SelectedIndexChanged = function(sender, args)
       self:SaveSettings();
       self:LoadMatchingDeeds();
	end
	
	self.typeLbl = Palantiri.VirtueView.UI.Label();
	self.typeLbl:SetParent(self);
	width = 60;
	self.typeLbl:SetSize(width, 20);
	self.typeLbl:SetPosition(x, y);
	x = x + width + 20;
	self.typeLbl:SetText("Type:");
	self.typeLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	
	self.typeCombo = Palantiri.VirtueView.UI.ComboBox();
	self.typeCombo:SetParent(self);
	width = 100;
	self.typeCombo:SetSize(width, 20);
	self.typeCombo:SetPosition(x, y);
	
    -- add the elements to a set, and sort them
    local set = {}
	for _, rec in ipairs(virtueTable) do set[rec.type] = true; end
	types = {}
	for pair in pairs (set) do table.insert(types, pair); end
	table.sort(types);
	table.insert(types, 1, "Any");
	
	for i, type in ipairs(types) do
      item = self.typeCombo:AddItem(type, i);
      item:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	end
	
	self.typeCombo:SetSelection(self.settings.type);
	self.typeCombo.SelectedIndexChanged = function(sender, args)
       self:SaveSettings();
       self:LoadMatchingDeeds();
	end
	
	y = y + 30;
	
	local virtWidth = 110;
	self.virtueList = Palantiri.VirtueView.UI.ListBox();
	self.virtueList:SetParent(self);
	self.virtueList:SetSize(virtWidth, h - y - 20);
	self.virtueList:SetPosition(margin, y);

    -- add the elements to a set, and sort them
    local set = {}
	for _, rec in ipairs(virtueTable) do set[rec.virtue] = true; end
	local virtues = {}
	for pair in pairs (set) do table.insert(virtues, pair); end
	table.sort(virtues);
    
	for i, virtue in pairs (virtues) do
        self:AddItem(self.virtueList, virtue);
	end
	
    for _, virtue in pairs(self.settings.selectedVirtues) do
       self.virtueList:FindItemByLabel(virtue):Toggle();
    end
    
	self.deedList = Palantiri.VirtueView.UI.ListBox();
	self.deedList:SetParent(self);
	self.deedList:SetSize(w - virtWidth - 35, h - y - 20);
	self.deedList:SetPosition(virtWidth + 15, y);
	
    self:LoadMatchingDeeds();
    
	self.virtueList.SelectionChanged = function(sender, args)
       self:SaveSettings();
       -- Turbine.Shell.WriteLine("GUI loading matching deeds");
       self:LoadMatchingDeeds();
	end
	
	self.Closing = function(sender, args)
		local curX, curY = self:GetPosition();
		self.settings.positionX = curX;
		self.settings.positionY = curY;
		self:SaveSettings();
	end
end

function GUI:LoadMatchingDeeds()
   local region = regions[self.settings.region];
   local includeRegion = (region == "Any");
   
   local type = types[self.settings.type];
   local includeType = (type == "Any");
    
   self.deedList:RemoveAll();
   
   for _, rec in ipairs(virtueTable) do
      if (self:InRegion(rec, region)) then
         if (self:OfType(rec, type)) then
            if (self.settings.selectedVirtues[rec.virtue]) then
               self:AddDeed(rec, includeRegion, includeType);
            end 
         end 
      end 
   end
end

function GUI:InRegion(rec, region)
   return (region == "Any") or (region == rec.region);
end

function GUI:OfType(rec, type)
   return (type == "Any") or (type == rec.type);
end

function GUI:AddItem(listBox, str)
   local item = Palantiri.VirtueView.UI.ListBoxItem(str);
   item:SetParent(listBox);
   item:SetSize(listBox:GetWidth() - 10, 20);
   listBox:AddItem2(item);
   
   -- item.StateChanged = function(sender, args) listBox:FireEvent(); end -- listen
   item.StateChanged = function(sender, args) listBox:FireEvent(); end -- listen
end
	
function GUI:AddDeed(rec, includeRegion, includeType)
   local item = Palantiri.VirtueView.UI.CheckBox();
   if (includeRegion and includeType) then
      item:SetText(rec.reward .. " " .. rec.virtue .. " [" .. rec.type .. "] " .. rec.deed .. " in " .. rec.region);
   elseif (includeRegion) then
      item:SetText(rec.reward .. " " .. rec.virtue .. " " .. rec.deed .. " in " .. rec.region);
   elseif (includeType) then
      item:SetText(rec.reward .. " " .. rec.virtue .. " [" .. rec.type .. "] " .. rec.deed);
   else
      item:SetText(rec.reward .. " " .. rec.virtue .. " " .. rec.deed);
   end
   
   if (self.settings.selectedDeeds[rec.region .. ":" .. rec.deed]) then
      item:SetChecked(true);
   end
      
   item:SetParent(self.deedList);
   item:SetSize(self.deedList:GetWidth() - 10, 20);
   self.deedList:AddItem2(item);
   
   item.Click = function(sender, args)
      -- Turbine.Shell.WriteLine(rec.region .. " " .. rec.deed .. " " .. tostring(item:IsChecked()));
      self.settings.selectedDeeds[rec.region .. ":" .. rec.deed] = item:IsChecked();
      self:SaveSettings();
   end
end
	
function GUI:GetRegion()
	return self.regionCombo:GetSelection();
end

function GUI:LoadSettings()
   self.loading = true;

   self.settings = Turbine.PluginData.Load(Turbine.DataScope.Character, "PalantiriVirtueViewSettings"); -- TODO
   if (type(self.settings) ~= "table") then
      self.settings = {};
   end

   if (not self.settings.positionX) then
      self.settings.positionX = 100; 
   end
   if (not self.settings.positionY) then
      self.settings.positionY = 100;
   end
	
   if (not self.settings.region) then
      self.settings.region = self.region;
   end
   if (not self.settings.type) then
      self.settings.type = self.type;
   end
   if (not self.settings.selectedVirtues) then
      self.settings.selectedVirtues = {}
   end
   if (not self.settings.selectedDeeds) then
      self.settings.selectedDeeds = {}
   end
    
	self.loading = false;
end

function GUI:SaveSettings()
	if (self.loading) then
		return;
	end
	
	self.settings.region = self.regionCombo:GetSelection();
	self.settings.type = self.typeCombo:GetSelection();
	self.settings.selectedVirtues = self.virtueList:GetSelected(); 
	
	Turbine.PluginData.Save(Turbine.DataScope.Character, "PalantiriVirtueViewSettings", self.settings);
end
