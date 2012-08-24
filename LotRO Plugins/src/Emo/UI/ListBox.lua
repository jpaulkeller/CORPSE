import "Turbine";
pcall(import, 'Turbine.Utils');
import "FrelledPlugins.Emo.UI.ListBoxItem"

ListBox = class(Turbine.UI.ListBox);

function ListBox:Constructor()
	Turbine.UI.ListBox.Constructor(self);

	self.verticalScrollBar = Turbine.UI.Lotro.ScrollBar();
	self.verticalScrollBar:SetOrientation(Turbine.UI.Orientation.Vertical);
	self.verticalScrollBar:SetParent(self);
	self:SetVerticalScrollBar(self.verticalScrollBar);

	self.selectedIndex = -1;
	self.SelectionChanged = nil;
	
	self.SelectedIndexChanged = function(sender, args)
		if (self.selectedIndex >= 1) then
			self:GetItem(self.selectedIndex):SetSelected(false);
		end

		self.selectedIndex = self:GetSelectedIndex();

		if (self.selectedIndex >= 1) then
			self:GetItem(self.selectedIndex):SetSelected(true);
		end
		
		if (type(self.SelectionChanged) == "function") then
			self.SelectionChanged(self, self.selectedIndex);
		end
	end
end

function ListBox:AddText(str)
	if (type(str) == "table") then
		for k, v in pairs(str) do
			local li = ListBoxItem(v);
			li:SetParent(self);
			li:SetSize(self:GetWidth() - 10, 20);
			self:AddItem(li);
		end
	else
		local li = ListBoxItem(str);
		li:SetParent(self);
		li:SetSize(self:GetWidth() - 10, 20);
		self:AddItem(li);
	end
	self.verticalScrollBar:SetPosition(self:GetWidth() - 10, 0);
	self.verticalScrollBar:SetSize(10, self:GetHeight());
end

function ListBox:RemoveText(str)
	local index = self:FindText(str);
	if (index >= 1) then
		self:RemoveItemAt(index);
	end
	self.selectedIndex = self:GetSelectedIndex();
end

function ListBox:RemoveAll()
	for i = self:GetItemCount(), 1, -1 do
		self:RemoveItemAt(i);
	end
	self.selectedIndex = -1;
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

function ListBox:FindText(txt)
	local ret = 0;
	for i = 1, self:GetItemCount(), 1 do
		if (self:GetItem(i):GetText() == txt) then
			ret = i;
			break;
		end
	end
	return ret;
end

function ListBox:SetSelection(index)
	self:SetSelectedIndex(index - 1);
end

function ListBox:GetSelection()
	return self:GetSelectedIndex();
end

function ListBox:GetList()
	local list = {};
	for i = 1, self:GetItemCount(), 1 do
		table.insert(list, self:GetItem(i):GetText());
	end
	return list;
end
