import "Turbine";
pcall(import, 'Turbine.Utils');
import "Palantiri.VirtueView.UI.ListBoxItem"

ListBox = class(Turbine.UI.ListBox);

function ListBox:Constructor()
	Turbine.UI.ListBox.Constructor(self);
	self:SetBackColor(Turbine.UI.Color(1, 0, 0, 0));

	self.verticalScrollBar = Turbine.UI.Lotro.ScrollBar();
	self.verticalScrollBar:SetOrientation(Turbine.UI.Orientation.Vertical);
	self.verticalScrollBar:SetParent(self);
	self:SetVerticalScrollBar(self.verticalScrollBar);
	
	-- TODO
	self.horizontalScrollBar = Turbine.UI.Lotro.ScrollBar();
	self.horizontalScrollBar:SetOrientation(Turbine.UI.Orientation.Horizontal);
	self.horizontalScrollBar:SetParent(self);
	self:SetHorizontalScrollBar(self.horizontalScrollBar);
end

function ListBox:AddItem2(item)
   self:AddItem(item);
   
   self.verticalScrollBar:SetPosition(self:GetWidth() - 10, 0);
   self.verticalScrollBar:SetSize(10, self:GetHeight());
   self.horizontalScrollBar:SetPosition(0, self:GetHeight() - 10);
   self.horizontalScrollBar:SetSize(self:GetWidth(), 10);
end

function ListBox:RemoveText(str)
	local index = self:FindText(str);
	if (index >= 1) then
		self:RemoveItemAt(index);
	end
end

function ListBox:RemoveAll()
	for i = self:GetItemCount(), 1, -1 do
		self:RemoveItemAt(i);
	end
end

function ListBox:FireEvent()
   if (type(self.SelectionChanged) == "function") then
      self:SelectionChanged();
   end
end

function ListBox:SortItems()
	local list = {};
	for i = 1, self:GetItemCount(), 1 do
		table.insert(list, self:GetItem(i):GetText());
	end
	table.sort(list);
	for i = 1, self:GetItemCount(), 1 do
		self:GetItem(i):SetText(list[i]);
	end
end

function ListBox:FindItemByLabel(txt)
	local item = nil;
	for i = 1, self:GetItemCount(), 1 do
		if (self:GetItem(i):GetText() == txt) then
			item = self:GetItem(i);
			break;
		end
	end
	return item;
end

function ListBox:FindIIndexByLabel(txt)
	local ret = 0;
	for i = 1, self:GetItemCount(), 1 do
		if (self:GetItem(i):GetText() == txt) then
			ret = i;
			break;
		end
	end
	return ret;
end

function ListBox:GetList()
	local list = {};
	for i = 1, self:GetItemCount(), 1 do
		table.insert(list, self:GetItem(i):GetText());
	end
	return list;
end

function ListBox:GetSelected()
   local list = {};
   for i = 1, self:GetItemCount(), 1 do
      local item = self:GetItem(i);
      if (item:IsSelected()) then
         list[item:GetText()] = item:GetText(); 
      end
   end
   return list;
end
