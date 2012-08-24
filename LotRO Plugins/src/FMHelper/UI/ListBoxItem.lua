import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";

ListBoxItem = class(Turbine.UI.Control);

local colorNormal   = Turbine.UI.Color(1, 0, 0, 0); -- black
local colorHover    = Turbine.UI.Color(1, 0, 0.5, 0.1); -- green
local colorSelected = Turbine.UI.Color(1, 0.1, 0.2, 0.8); -- blue
	
function ListBoxItem:Constructor(text)
	Turbine.UI.Control.Constructor(self);

    self.selected = false;
    	
	self.nameLabel = Turbine.UI.Label();
	self.nameLabel:SetParent(self);
	self.nameLabel:SetText(text);
	self.nameLabel:SetPosition(5, 5);
	self.nameLabel:SetMouseVisible(false);
    self.nameLabel:SetFont(Turbine.UI.Lotro.Font.Verdana16);

	self:SetBlendMode(Turbine.UI.BlendMode.Overlay);
	self:Layout();
	
	self.MouseEnter = function(sender, args)
       self:SetBackColor(colorHover)
	end
	
	self.MouseLeave = function(sender, args)
       self:SetColor();
	end
	
	self.MouseClick = function(sender, args)
       self:Toggle();
	end
end

function ListBoxItem:SetText(txt)
	self.nameLabel:SetText(txt);
end

function ListBoxItem:GetText()
	return self.nameLabel:GetText();
end

function ListBoxItem:SetSelected(value)
   self.selected = value;
   self:SetColor();
   self:FireEvent();
end

function ListBoxItem:IsSelected()
   return self.selected;
end

function ListBoxItem:Toggle()
   self.selected = not self.selected;
   self:SetColor();
   self:FireEvent();
end

function ListBoxItem:FireEvent()
   if (type(self.StateChanged) == "function") then
      self:StateChanged();
   end
end

function ListBoxItem:SetColor()
   local color = colorNormal;
   if (self.selected) then color = colorSelected; end
   self:SetBackColor(color);
end

function ListBoxItem:Layout()
	local width = self:GetWidth();
	self.nameLabel:SetSize(width, 18);
end

function ListBoxItem:SizeChanged()
	self:Layout();
end
