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
import "Palantiri.VirtueView.UI.TextBox"

-- API docs: http://www.lotrointerface.com/wiki
-- Turbine.Shell.WriteLine("debug");

-- To upload: 
-- http://www.lotrointerface.com/downloads/upload-update.php?
-- http://www.lotrointerface.com/downloads/editfile.php?id=720

-- TODO
-- background image for main panel
-- icon
-- option

GUI = class(Turbine.UI.Lotro.Window);

function GUI:Constructor()
	Turbine.UI.Lotro.Window.Constructor(self);
	
	-- load the last settings saved
	self.loading = false;
	self:LoadSettings();

	-- set window properties
	local w = 1000;
	local h = 500;
	self:SetText("Virtue Deeds");
	self:SetSize(w, h);
	self:SetBackColor(clear);
	self:SetPosition(self.settings.positionX, self.settings.positionY);
	self:SetOpacity(1);
	self:SetAllowDrop(false);
	self.focusBgColor = Turbine.UI.Color(1, 0.5, 0.5, 0.5);

	local margin = 5;
	local x = margin + 5;
	local y = 35;
	local width;
	
	-- Build the UI
	
	-- Zones
	
	self.zoneLbl = Palantiri.VirtueView.UI.Label();
	self.zoneLbl:SetParent(self);
	width = 70;
	self.zoneLbl:SetSize(width, 20);
	self.zoneLbl:SetPosition(x, y);
	x = x + width + 5;
	self.zoneLbl:SetText("Zone:");
	self.zoneLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	
	self.zoneCombo = Palantiri.VirtueView.UI.ComboBox();
	self.zoneCombo:SetParent(self);
	width = 130;
	self.zoneCombo:SetSize(width, 20);
	self.zoneCombo:SetPosition(x, y);
	x = x + width + 5;
	
    -- add the elements to a set, and sort them
    local set = {}
	for _, rec in ipairs(virtueTable) do set[rec.zone] = true; end
	zones = {}
	for pair in pairs (set) do table.insert(zones, pair); end
	table.sort(zones);
	table.insert(zones, 1, "Any");
	
	for i, zone in ipairs(zones) do
      item = self.zoneCombo:AddItem(zone, i);
      item:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	end
	
	self.zoneCombo:SetSelection(self.settings.zone);
	self.zoneCombo.SelectedIndexChanged = function(sender, args)
       self:SaveSettings();
       self:LoadMatchingDeeds();
	end

	-- Regions
	
	self.regionLbl = Palantiri.VirtueView.UI.Label();
	self.regionLbl:SetParent(self);
	width = 70;
	self.regionLbl:SetSize(width, 20);
	self.regionLbl:SetPosition(x, y);
	x = x + width + 5;
	self.regionLbl:SetText("Region:");
	self.regionLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	
	self.regionCombo = Palantiri.VirtueView.UI.ComboBox();
	self.regionCombo:SetParent(self);
	width = 370;
	self.regionCombo:SetSize(width, 20);
	self.regionCombo:SetPosition(x, y);
	x = x + width + 5;
	
  -- add the elements to a set, and sort them
  local set = {}
	for _, rec in ipairs(virtueTable) do set[rec.region] = true; end
	regions = {}
	for pair in pairs (set) do table.insert(regions, pair); end
  table.insert(regions, 1, "Mines of Moria");
  table.insert(regions, 1, "Shadows of Angmar");
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

    -- Types
    
	self.typeLbl = Palantiri.VirtueView.UI.Label();
	self.typeLbl:SetParent(self);
	width = 50;
	self.typeLbl:SetSize(width, 20);
	self.typeLbl:SetPosition(x, y);
	x = x + width + 5;
	self.typeLbl:SetText("Type:");
	self.typeLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	
	self.typeCombo = Palantiri.VirtueView.UI.ComboBox();
	self.typeCombo:SetParent(self);
	width = 90;
	self.typeCombo:SetSize(width, 20);
	self.typeCombo:SetPosition(x, y);
	x = x + width + 5;
	
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

  -- Max Level
    	
	self.maxLevelLbl = Palantiri.VirtueView.UI.Label();
	self.maxLevelLbl:SetParent(self);
	width = 90;
	self.maxLevelLbl:SetSize(width, 20);
	self.maxLevelLbl:SetPosition(x, y);
	x = x + width + 5;
	self.maxLevelLbl:SetText("Max Level:");
	self.maxLevelLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	
	self.maxLevelText = Palantiri.VirtueView.UI.TextBox();
	self.maxLevelText:SetParent(self);
	width = 40;
	self.maxLevelText:SetSize(width, 20);
	self.maxLevelText:SetPosition(x, y);
	self.maxLevelText:SetText(self.settings.maxLevel);
	x = x + width + 5;
	
	self.maxLevelText.TextChanged = function(sender, args)
       self:SaveSettings();
       self:LoadMatchingDeeds();
	end
	
	-- end controls
	
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
   local zone = zones[self.settings.zone];
   local includeZone = (zone == "Any");
   
   local region = regions[self.settings.region];
   local includeRegion = (region == "Any");
   
   local type = types[self.settings.type];
   local includeType = (type == "Any");
   
   local maxLevel = self.settings.maxLevel;
    
   self.deedList:RemoveAll();
   
   for _, rec in ipairs(virtueTable) do
      if (self:InZone(rec, zone)) then
         if (self:InRegion(rec, region)) then
            if (self:OfType(rec, type)) then
               if (self:underLevel(rec, maxLevel)) then
                  if (self.settings.selectedVirtues[rec.virtue]) then
                     self:AddDeed(rec, includeZone, includeRegion, includeType);
                  end 
               end 
            end 
         end 
      end 
   end
end

function GUI:InZone(rec, zone)
   return (zone == "Any") or (zone == rec.zone);
end

function GUI:InRegion(rec, region)
   return (region == "Any") or (rec.region:startsWith(region));
end

function GUI:OfType(rec, type)
   return (type == "Any") or (type == rec.type);
end

function GUI:underLevel(rec, maxLevel)
   return (maxLevel >= rec.level);
end

function GUI:AddItem(listBox, str)
   local item = Palantiri.VirtueView.UI.ListBoxItem(str);
   item:SetParent(listBox);
   item:SetSize(listBox:GetWidth() - 10, 20);
   listBox:AddItem2(item);
   
   -- item.StateChanged = function(sender, args) listBox:FireEvent(); end -- listen
   item.StateChanged = function(sender, args) listBox:FireEvent(); end -- listen
end
	
function GUI:AddDeed(rec, includeZone, includeRegion, includeType)
   local item = Palantiri.VirtueView.UI.CheckBox();
   
   local where = "";
   if (includeZone and includeRegion) then
      where = rec.zone .. "/" .. rec.region;
   elseif (includeZone) then
      where = rec.zone .. " ";
   elseif (includeRegion) then
      where = rec.region .. " ";
   end
      
   local type = " - ";
   if (includeType) then
      type = " (" .. rec.type .. ") ";
   end
   
   local plus = "";
   if (rec.reward ~= "+1") then
     plus = rec.reward;
   end
      
   local reward = "";
   if (self:ElementCount(self.settings.selectedVirtues) > 1) then
      reward = " (" .. plus .. rec.virtue .. ")";
   else
      reward = " (" .. plus .. ")";
   end

   local level = " - Level ";
   if (rec.level == 0) then
      level = level .. "? ";
   else
      level = level .. rec.level .. " ";
   end
   
   item:SetText(where .. type .. rec.deed .. level .. reward);
   
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

function GUI:ElementCount(T)
  local count = 0
  for _ in pairs(T) do count = count + 1 end
  return count
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
	
   if (not self.settings.zone) then
      self.settings.zone = self.zone;
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
    
   if (not self.settings.maxLevel) then
      self.settings.maxLevel = 100;
   end
	
	self.loading = false;
end

function GUI:SaveSettings()
	if (self.loading) then
		return;
	end
	
	self.settings.zone = self.zoneCombo:GetSelection();
	self.settings.region = self.regionCombo:GetSelection();
	self.settings.type = self.typeCombo:GetSelection();
	self.settings.selectedVirtues = self.virtueList:GetSelected(); 
	self.settings.maxLevel = self.maxLevelText:GetNumber();
	
	Turbine.PluginData.Save(Turbine.DataScope.Character, "PalantiriVirtueViewSettings", self.settings);
end

startsWith = function(self, piece)
  return string.sub(self, 1, string.len(piece)) == piece
end
rawset(_G.string, "startsWith", startsWith)
