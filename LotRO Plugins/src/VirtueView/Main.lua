import "Palantiri.VirtueView.GUI";
import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";

_dw, _dh = Turbine.UI.Display.GetSize();

local function updateScreenCoords(widget)
	local ww, wh, wx, wy;
	local changed = false;

	wx, wy = widget:GetPosition();
	ww, wh = widget:GetSize();

	if ((wx + ww) > _dw) then
		wx = _dw - ww;
		changed = true;
	end
	if ((wy + wh) > _dh) then
		wy = _dh - wh;
		changed = true;
	end
	
	if (wx < 0) then
		wx = 0;
		changed = true;
	end
	if (wy < 0) then
		wy = 0;
		changed = true;
	end
	
	if (changed) then
		widget:SetPosition(wx, wy);
	end
end

gui = GUI();
-- gui:SetVisible(true);

-- Make sure everything is visible on the screen
updateScreenCoords(gui);

command = Turbine.ShellCommand();

function command:Execute(cmd, args)
	if (args == "show") then
		gui:SetVisible(true);
	elseif (args == "hide") then
		gui:SetVisible(false);
	elseif (args == "toggle") then
		gui:SetVisible(not gui:IsVisible());
	else
		command:GetHelp();
	end
end

function command:GetHelp()
   Turbine.Shell.WriteLine("<rgb=#00FFFF>VirtueView (version " .. Plugins.VirtueView:GetVersion() .. ")</rgb> by Mosby of Landroval");
   Turbine.Shell.WriteLine("Usage: /vv [show|hide|toggle]");
end

Turbine.Shell.AddCommand("vv", command);
command:GetHelp();
