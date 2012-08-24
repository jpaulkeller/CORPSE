import "Turbine";
pcall(import, 'Turbine.Utils');
import "Palantiri.FMHelper.UI.ListBoxItem"

ListBox = class(Turbine.UI.ListBox);

function ListBox:Constructor()
	Turbine.UI.ListBox.Constructor(self);

	self.verticalScrollBar = Turbine.UI.Lotro.ScrollBar();
	self.verticalScrollBar:SetOrientation(Turbine.UI.Orientation.Vertical);
	self.verticalScrollBar:SetParent(self);
	self:SetVerticalScrollBar(self.verticalScrollBar);
end

function ListBox:AddText(str)
	local li = ListBoxItem(str);
	li:SetParent(self);
	li:SetSize(self:GetWidth() - 10, 20);
	self:AddItem(li);
    li.StateChanged = function(sender, args) 
       -- Turbine.Shell.WriteLine("LB StateChanged " .. li:GetText());
       self:FireEvent(li:GetText()); 
    end
	
	self.verticalScrollBar:SetPosition(self:GetWidth() - 10, 0);
	self.verticalScrollBar:SetSize(10, self:GetHeight());
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

function ListBox:FireEvent(itemLabel)
   if (type(self.ItemSelected) == "function") then
      Turbine.Shell.WriteLine("here I am " .. itemLabel);
      self:ItemSelected({selection=itemLabel});
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
