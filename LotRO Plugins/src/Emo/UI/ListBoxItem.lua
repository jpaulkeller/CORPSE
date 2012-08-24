import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";

ListBoxItem = class(Turbine.UI.Control);

function ListBoxItem:Constructor(text)
	Turbine.UI.Control.Constructor(self);

	self:SetBlendMode(Turbine.UI.BlendMode.Overlay);

	self.nameLabel = Turbine.UI.Label();
	self.nameLabel:SetParent(self);
	self.nameLabel:SetText(text);
	self.nameLabel:SetPosition(5, 5);

--	self.selectedColor = Turbine.UI.Color(1, 1, 0, 1);
--	self.unselectedColor = Turbine.UI.Color(0, 0, 0, 1);
	self.selectedColor = Turbine.UI.Color( 1, 0.1, 0.3, 1 );
--	self.unselectedColor = Turbine.UI.Color( 1, 0, 0, 0 );
	
	self:Layout();
end

function ListBoxItem:SetSelected(value)
	if (value) then
		self:SetBackColor(self.selectedColor)
	else
--		self:SetBackColor(self.unselectedColor)
		self:SetBackColor(self:GetParent():GetBackColor())
	end
end

function ListBoxItem:GetText()
	return self.nameLabel:GetText();
end

function ListBoxItem:SetText(txt)
	self.nameLabel:SetText(txt);
end

function ListBoxItem:Layout()
	local width = self:GetWidth();

	self.nameLabel:SetSize(width, 18);
end

function ListBoxItem:SizeChanged()
	self:Layout();
end
