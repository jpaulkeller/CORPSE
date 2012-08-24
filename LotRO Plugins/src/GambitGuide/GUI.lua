import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";
import "Palantiri.GambitGuide.WardenData";
import "Palantiri.GambitGuide.UI.CheckBox";
import "Palantiri.GambitGuide.UI.ComboBox";
import "Palantiri.GambitGuide.UI.Label";
import "Palantiri.GambitGuide.UI.ListBox";
import "Palantiri.GambitGuide.UI.ListBoxItem"

-- TODO
-- rename Gambit Guide?
-- icons for gambit build
-- max level
-- Any Stance
-- background image for main panel
-- icon
-- options

GUI = class(Turbine.UI.Lotro.Window);

function GUI:Constructor()
	Turbine.UI.Lotro.Window.Constructor(self);
	
	-- load the last settings saved
	self.loading = false;
	self:LoadSettings();

	-- set window properties
	local w = 900;
	local h = 500;
	self:SetText("Gambit Guide");
	self:SetSize(w, h);
	self:SetBackColor(clear);
	self:SetPosition(self.settings.positionX, self.settings.positionY);
	self:SetOpacity(1);
	self:SetAllowDrop(false);
	self.focusBgColor = Turbine.UI.Color(1, 0.5, 0.5, 0.5);

	local margin = 20;
	local y = 35;
	
	-- Build the UI
	
	self.stanceLbl = Palantiri.GambitGuide.UI.Label();
	self.stanceLbl:SetParent(self);
	self.stanceLbl:SetSize(120, 20);
	self.stanceLbl:SetPosition(margin + 80, y);
	self.stanceLbl:SetText("Stance:");
	self.stanceLbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
	
	self.stanceCombo = Palantiri.GambitGuide.UI.ComboBox();
	self.stanceCombo:SetParent(self);
	self.stanceCombo:SetSize(400, 20);
	self.stanceCombo:SetPosition(margin + 80 + 120 + 10, y);
	
    -- STANCES
    stances = {}
    for _, rec in ipairs(stanceTable) do 
      -- Turbine.Shell.WriteLine(rec.key .. " = " .. rec.name);
      stances[rec.key] = rec.name;
      item = self.stanceCombo:AddItem(rec.name, rec.key);
      item:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	end
	
	self.stanceCombo:SetSelection(self.settings.stance);
	self.stanceCombo.SelectedIndexChanged = function(sender, args)
       self:SaveSettings();
       self:LoadMatching();
	end
	y = y + 30;

    -- FILTERS	
	local filterWidth = 110;
	self.filterList = Palantiri.GambitGuide.UI.ListBox();
	self.filterList:SetParent(self);
	self.filterList:SetSize(filterWidth, h - y - 20);
	self.filterList:SetPosition(margin, y);

	for _, filterRec in pairs (filterTable) do
        self:AddFilter(self.filterList, filterRec);
	end
	
    for _, filter in pairs(self.settings.selectedFilters) do
       self.filterList:FindItemByLabel(filter):Toggle();
    end
    
   -- GAMBITS TODO
   self.gambits = {}
   for _, gambitRec in pairs(gambitTable) do
      local info = "bld=" .. gambitRec.bld;
      if (gambitRec.fx) then info = info .. "::" .. gambitRec.fx end
      for _, stanceRec in pairs(stanceTable) do -- add stance-specific effects
         if (gambitRec[stanceRec.key]) then info = info .. "::" .. gambitRec[stanceRec.key]; end
      end
      Turbine.Shell.WriteLine(gambitRec.name .. " = " .. info);
	  self.gambits[gambitRec.name] = info; 
   end
   -- table.sort(self.gambits);
    
	self.gambitList = Palantiri.GambitGuide.UI.ListBox();
	self.gambitList:SetParent(self);
	self.gambitList:SetSize(w - filterWidth - 35, h - y - 20);
	self.gambitList:SetPosition(filterWidth + 15, y);
	
    self:LoadMatching();
    
	self.filterList.SelectionChanged = function(sender, args)
       self:SaveSettings();
       self:LoadMatching();
	end
	
	self.Closing = function(sender, args)
		local curX, curY = self:GetPosition();
		self.settings.positionX = curX;
		self.settings.positionY = curY;
		self:SaveSettings();
	end
end

function GUI:AddFilter(listBox, filterRec)
   local item = Palantiri.GambitGuide.UI.ListBoxItem(filterRec.name); -- TODO just use label
   item:SetParent(listBox);
   item:SetSize(listBox:GetWidth() - 10, 20);
   item.filterRec = filterRec;
   listBox:AddItem2(item);
   item.StateChanged = function(sender, args) listBox:FireEvent(); end -- listen
end
	
function GUI:LoadMatching()
   self.gambitList:RemoveAll();
   -- TODO
   for _, rec in ipairs(gambitTable) do
      local match = false;
      local info = self.gambits[rec.name];
      for _, filterName in pairs(self.settings.selectedFilters) do
         -- TODO null check
         filterRec = self.filterList:FindItemByLabel(filterName).filterRec;
         if (filterRec) then
            -- Turbine.Shell.WriteLine("    >> " .. filterRec.pattern);
            for pattern in filterRec.pattern:gmatch("[^|]+") do
               if (info:find(pattern)) then match = true; end -- TODO break
            end
         end
      end
      if (match) then self:AddGambit(rec); end
   end
end

function GUI:AddGambit(rec)
   local item = Palantiri.GambitGuide.UI.CheckBox();
   
   local info = rec.name;
   if (rec.fx) then info = info .. " -- " .. rec.fx; end
   local stanceFx = rec[self.settings.stance]; 
   if (stanceFx) then info = info .. " -- " .. stanceFx; end
   item:SetText(info);
   
   item:SetParent(self.gambitList);
   item:SetSize(self.gambitList:GetWidth() - 10, 20);
   self.gambitList:AddItem2(item);
end
	
function GUI:GetStance()
	return self.stanceCombo:GetSelection();
end

function GUI:LoadSettings()
   self.loading = true;

   self.settings = Turbine.PluginData.Load(Turbine.DataScope.Character, "PalantiriGambitGuideSettings");
   if (type(self.settings) ~= "table") then
      self.settings = {};
   end

   if (not self.settings.positionX) then
      self.settings.positionX = 100; 
   end
   if (not self.settings.positionY) then
      self.settings.positionY = 100;
   end
	
   if (not self.settings.stance) then
      self.settings.stance = "d";
   end
   
   self.settings.selectedFilters = {} -- TODO
   if (not self.settings.selectedFilters) then
      self.settings.selectedFilters = {}
   end
    
	self.loading = false;
end

function GUI:SaveSettings()
	if (self.loading) then
		return;
	end
	
	self.settings.stance = self.stanceCombo:GetSelection();
	self.settings.selectedFilters = self.filterList:GetSelected(); 
	
	Turbine.PluginData.Save(Turbine.DataScope.Character, "PalantiriGambitGuideSettings", self.settings);
end
