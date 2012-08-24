import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";

import "Palantiri.GambitGuide.WardenData";

ListBoxItem = class(Turbine.UI.Control);

function ListBoxItem:Constructor(text)
	Turbine.UI.Control.Constructor(self);

    self.selected = false;
    	
	self.label = Turbine.UI.Label();
	self.label:SetParent(self);
	self.label:SetText(text);
	self.label:SetPosition(5, 5);
	self.label:SetMouseVisible(false);
    -- self.label:SetFont(Turbine.UI.Lotro.Font.Verdana16);
    self.label:SetFont(Turbine.UI.Lotro.Font.TrajanPro14);
    self.label:SetForeColor(gold);
    self.label:SetOutlineColor(outlineColor);

	self:SetBlendMode(Turbine.UI.BlendMode.Overlay);
	self:Layout();
	
	self.MouseEnter = function(sender, args)
       self.label:SetForeColor(white);
       self.label:SetFontStyle(Turbine.UI.FontStyle.Outline);
	end
	
	self.MouseLeave = function(sender, args)
       self.label:SetForeColor(gold);
       self.label:SetFontStyle(Turbine.UI.FontStyle.None);
       self:SetColor();
	end
	
	self.MouseClick = function(sender, args)
       self:Toggle();
	end
end

function ListBoxItem:SetText(txt)
	self.label:SetText(txt);
end

function ListBoxItem:GetText()
	return self.label:GetText();
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
      -- Turbine.Shell.WriteLine("LBI calling StateChanged");
      -- self:StateChanged({state=self.selected});
      self:StateChanged();
   end
end

function ListBoxItem:SetColor()
   local color = black;
   if (self.selected) then color = selectedItem; end
   self:SetBackColor(color);
end

function ListBoxItem:Layout()
	local width = self:GetWidth();
	self.label:SetSize(width, 18);
end

function ListBoxItem:SizeChanged()
	self:Layout();
end
