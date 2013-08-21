import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";

TextBox = class(Turbine.UI.Lotro.TextBox);

function TextBox:Constructor()
   Turbine.UI.Lotro.TextBox.Constructor(self);
   
   self:SetFont(Turbine.UI.Lotro.Font.TrajanProBold16);
   self.text = nil;
   self.max = 99;
   
   self.Update = function(sender, args)
      local val = self:GetText();
      if (self.text ~= val) then
         local number = self.max;
	     if (val ~= "") then
            number = tonumber(val);
         end
         if (number == nil) or (number > self.max) then -- invalid
            self:SetText(self.text); -- restore previous valid value
         else
            self.text = val;
            self.number = number;
            self.TextChanged();
         end
      end
   end
   
   self.FocusGained = function(sender, args)
      self:SetWantsUpdates(true);
   end
   
   self.FocusLost = function(sender, args)
      self:SetWantsUpdates(false);
   end
end

function TextBox:GetNumber()
   return self.number;
end

function TextBox:SetText(text)
   Turbine.UI.Lotro.TextBox.SetText(self, text); -- forward call to parent class
   self.text = text;
   self.number = tonumber(text);
end
