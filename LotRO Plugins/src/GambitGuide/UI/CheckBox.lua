import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";

import "Palantiri.GambitGuide.WardenData";

CheckBox = class(Turbine.UI.Control);

CheckBox.LocationLeft = 1;
CheckBox.LocationRight = 2;

function CheckBox:Constructor()
    Turbine.UI.Control.Constructor(self);

    -- state
    self.checked = false;

	self.checkboxLocation = CheckBox.LocationLeft;
	
    -- text label
    self.label = Turbine.UI.Label();
    self.label:SetParent(self);
    self.label:SetFont(Turbine.UI.Lotro.Font.TrajanPro14);
    self.label:SetForeColor(standardText);
    self.label:SetOutlineColor(outlineColor);
    self.label:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleRight);
    self.label:SetMouseVisible(false);
    
    -- check
    self.check = Turbine.UI.Control();
    self.check:SetParent(self);
    self.check:SetSize(16, 16);
    self.check:SetBlendMode(Turbine.UI.BlendMode.AlphaBlend);
    self.check:SetBackground("Palantiri/GambitGuide/Resources/checkbox_02_empty.tga");
    self.check:SetMouseVisible(false);

	self:Layout();
	
    -- listeners
	self.MouseEnter = function(sender, args)
       self.label:SetFontStyle(Turbine.UI.FontStyle.Outline);
       self.label:SetForeColor(white);
	end
	
	self.MouseLeave = function(sender, args)
       self.label:SetFontStyle(Turbine.UI.FontStyle.None);
       self:UpdateState();
	end
	
	self.MouseClick = function(sender, args)
       self:Toggle();
	end
end

function CheckBox:Toggle()
   -- if (not self:IsEnabled()) then return; end
   -- if (args.Button == Turbine.UI.MouseButton.Left) then
   self:SetChecked(not self.checked);
   -- self:FireEvent();
   if (type(self.Click) == "function") then
      self:Click({});
   end
end

function CheckBox:IsChecked()
    return self.checked;
end

function CheckBox:SetChecked(checked)
    self.checked = checked;
    self:UpdateState();
end

-- Turbine.UI.Control.SetEnabled(self, enabled);

function CheckBox:SetText(text)
    self.label:SetText(text);
end

function CheckBox:GetText()
	return self.label:GetText();
end

function CheckBox:SetSize(width, height)
    Turbine.UI.Control.SetSize(self, width, height);
    self:Layout();
end

function CheckBox:SetCheckBoxLocation(location)
	self.checkboxLocation = location;
end

function CheckBox:Layout()
    local width, height = self:GetSize();
	if (self.checkboxLocation == CheckBox.LocationRight) then
		self.label:SetPosition(0, 0);
		self.label:SetSize(width - 22, height);
		self.check:SetPosition(width - 16, ((height - 16) / 2));
	else
		self.label:SetPosition(22, 0);
		self.label:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
		self.label:SetSize(width - 22, height);
		self.check:SetPosition(0, ((height - 16) / 2));
	end
end

function CheckBox:UpdateState()
   -- local enabled = self:IsEnabled();
   if (self.checked) then
       self.label:SetForeColor(disabledText);
       self.check:SetBackground("Palantiri/GambitGuide/Resources/checkbox_02"..(enabled and "" or "_ghosted")..".tga");
   else
       self.label:SetForeColor(standardText);
       self.check:SetBackground("Palantiri/GambitGuide/Resources/checkbox_02_empty"..(enabled and "" or "_ghosted")..".tga");
   end
end
