import "Turbine";
pcall(import, 'Turbine.Utils');
import "Turbine.UI";
import "Turbine.UI.Lotro";
import "Turbine.UI.Extensions";

EmoManualWindow = class(Turbine.UI.Lotro.Window);

function EmoManualWindow:Constructor()
	Turbine.UI.Lotro.Window.Constructor(self);
	
	self:SetPosition(100, 100);
	self:SetSize(400, 400);
	self:SetText("Emo v".._emoVersion.." Help");
	
	local margin = 20;
	local y = 35;
	
	local txt = function(txt)
		self.lbl:AppendText(txt);
	end

	self.lbl = FrelledPlugins.Emo.UI.Label();
	self.lbl:SetMultiline(true);
	self.lbl:SetParent(self);
	self.lbl:SetPosition(margin, y);
	self.lbl:SetSize(400 - (margin * 2) - 10, 400 - y - margin);
	self.lbl:SetTextAlignment(Turbine.UI.ContentAlignment.MiddleLeft);
	self.lbl:SetFont(Turbine.UI.Lotro.Font.Verdana14);
	self.lbl:SetText("--NAME--\n\n");
	txt("emo - Emote Grouping Utility\nVersion ".._emoVersion.."\n\n");

	txt("--SYNOPSIS--\n\n");
	txt("emo [OPTION]\n\n");

	txt("--DESCRIPTION--\n\n");
	txt("Emo is an easy way to take advantage of the massive number of emotes available in LotRO. It does this by letting you create logical 'groupings' of emotes. These groups appear on a toolbar as buttons.\n\n");
	txt("/emo show\n     Shows the Emo toolbar\n");
	txt("/emo hide\n     Hides the Emo toolbar\n");
	txt("/emo toggle\n     Toggles the visiblity of the Emo toolbar\n");
	txt("/emo options\n     Shows the Emo options window\n");
	txt("/emo help\n     Displays this page\n\n");
	txt("Additionally, Emo has right-button menus which are context-sensitive and also features the wheel scroller to cycle through your emotes in a group without firing the emote.\n\n");

	txt("--SAVED INFORMATION SCOPE--\n\n");
	txt("Custom emotes are saved in the 'account' segment. This means your custom emotes will be accessible on all servers on your account. Unfortunately, there is no easy way to share them across accounts other than copying the data file.\n\n");
	txt("Emo Toolbar groups are also saved in the 'account' segment.\n\n");
	txt("The position of and the options for the Emo Toolbar are saved on a per-character basis.\n\n");
	
	txt("--CUSTOM EMOTES--\n\n");
	txt("  Adding/Editing a New Emote\n");
	txt("To add or edit an emote, right click on the Emo toolbar and select 'Custom Emotes'. This brings up the Customize Emote window.\n\n");
	txt("First, you either select 'NEW EMOTE' or you choose an existing custom emote you want to edit. Now click the 'Next' button.\n\n");
	txt("On this page, there are several settings to fill out. First, give your Custom Emote a Name by typing in (or changing) it in the text box at the top of the window. This must begin with a forward slash and can only contain letters, numbers, or an underscore.\n\n");
	txt("Next, you choose the 'Animation Emote' (this is the emote you wish to borrow the animation from... e.g. /wave would be good for a custom greeting).\n\n");
	txt("Finally, you type in the actual text you want everyone to see in the 'No Target:' section in the 'All:' text box. Note if you use a ;, it will be interpretted as an alias by the system. You can use the special keyword ';target' which will be replaced with your target (however, if you have no target ';target' will show up). Hopefully, we'll get better API access in the future to allow emotes to be more detailed.\n\n");
	txt("Want to see what your emote looks like before you save it? Click the test button and your custom emote will be performed.\n\n");
	txt("Clicking the Save button will save your emote.\n\n");
	
	txt("--TURBINE EMOTES--\n\n");
	txt("This functionality is in place to allow you to add an emote which is provided by Turbine, but has not yet been updated with Emo. Please, if you do find a missing one, notify me!\n\n");
	txt("  usage: /emo add_turbine_emote /new_turbine_emote  (Only use for new in-game emotes that Turbine adds)\n\n");
	
	txt("--GROUPS--\n\n");
	txt("  Adding a New Group\n");
	txt("To add a new group, you right click the Emo toolbar and select Add New Group. This adds a new group to the toolbar, to edit it see below.\n\n");
	txt("  Editing a Group\n");
	txt("To edit an existing group, right click the group button on the Emo toolbar and select Edit Group. This opens up the Edit Group Window for that group.\n\n");
	txt("  Deleting a Group\n");
	txt("To delete a group, right click the group button on the Emo toolbar and select Delete Group. There is no confirmation, so be certain you wish to delete the group!\n\n");
	txt("  Edit Group Window\n");
	txt("The Edit Group Window is composed of 3 overall sections. ");
	txt("The 'title' or 'name' of the group is at the top in a textfield. You can change the name here. It must be unique to other group names. The available emotes are in a list box on the left. The group emotes are in a list box on the right.\n\n");
	txt("To add an emote to the group, simply select one from the available emotes on the left and press the 'move right' button (top button in the middle).\n\n");
	txt("To remove an emote from a group, select the emote from the group emotes on the right and press the 'move left' button (bottom button in the middle).\n\n");
	txt("Once you are done editing, you either Save or Cancel. If you close the window or edit a group from the toolbar menu, you will lose any changes you have made.\n\n");
	txt("  Adding a Default Group\n");
	txt("On the right button menu, there are now default groups available. These are the same groups you saw the first time Emo started up. You can add them back in anytime. If there is already a group named the same, the default group name will have a 1 appended to the end. This is a handy way to add something like Deedmotes until you no longer need it, then delete the group. Bring it back at any time.\n\n");
	
	txt("--OPTIONS--\n\n");
	txt("  Click Action\n");
	txt("The click action determines what happens when you click on the toolbar button.\n");
	txt("     Random - The next emote is chosen randomly from your group.\n");
	txt("     Cycle - The next emote is literally the next emote in the group's list.\n\n");
	txt("  Orientation\n");
	txt("At this time only horizontal is available.\n\n");
	txt("  Toolbar Visibility\n");
	txt("This checkbox determines if the toolbar will automatically be shown when the plugin is loaded.\n\n");
	
	txt("--ADDING A NEW GAME EMOTE FROM TURBINE--\n\n");
	txt("This command is only for use when a new emote has been added to the game by Turbine which is not included in the version of Emo you have installed! It is meant to be a stop-gap measure until you update your version of Emo.\n\n");
	txt("Type in: /emo add_turbine_emote /emote\n");
	txt("Replace '/emote' with the new emote (e.g. '/boo')\n\n");
	
	txt("--HOW TO DEFAULT THE GAME EMOTES--\n\n");
	txt("This command will reset the *game* emote list (the emotes provided by Turbine) to the ones this version of Emo knows about. This is how you correct for entering an emote that does not exist.\n\n");
	txt("Type in: /emo default_game_emotes\n\n");
	
	txt("--ODDITIES--\n\n");
	txt("Currently if you want to retrieve any of the default groups the only way to do so is to delete all your groups and reload. Yes, this will destroy any changes you have made and I know this is pretty stupid. I suck. What else can I say? :)\n\n");

	txt("--AUTHOR--\n\n");
	txt("Written by Scott Powers (Frellco/Frello/Frella of Elendilmir)\n");
	txt("Thanks to Dhor (D.H1cks) for answering some questions and helping me find some of the 'hidden' gems! Some UI Classes originally from Orendar's Plugins -- Many thanks for a great set of UI widgets!\n\n");
	
	txt("--VERSION HISTORY--\n\n");
	txt("2.0\n");
	txt("     10/19/2010\n");
	txt("     Added custom emotes, wheel scroll cycling, fixed some bugs, better data scoping, and failsafe for toolbars and windows which somehow ended up offscreen.\n\n");
	txt("1.0\n");
	txt("     09/12/2010\n");
	txt("     Initial release\n\n");
	
	txt("--COPYRIGHT AND LICENSE--\n\n");
	txt("Copyright (c) Scott Powers\n\n");
	txt("This is free software. There is NO warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n\n");
	txt("This plugin is released into the Public Domain. Period. Use it however you would like. Attribution would be nice, but is not necessary.\n\n");

	txt("--FUTURE PLANS--\n\n");
	txt("- provide the click action on a per group basis rather than overall\n");
	txt("- provide a mechanism for choosing which default groups to add/show\n");
	txt("- vertical orientation\n");
	txt("- provide a mechanism to sort the toolbar buttons as well as sorting the emotes (emotes currently are alphabetized)\n");
	txt("- provide a more traditional Ctrl-\\ movement system\n");
	txt("\n");
	txt("--BUGS, FEEDBACK, and SUGGESTIONS--\n\n");
	txt("Please feel free to drop me an email: frell(at)spowers(dot)net or via the mechanisms at www.lotrointerface.com.\n");

	self.verticalScrollBar = Turbine.UI.Lotro.ScrollBar();
	self.verticalScrollBar:SetOrientation(Turbine.UI.Orientation.Vertical);
	self.verticalScrollBar:SetParent(self);
	self.lbl:SetVerticalScrollBar(self.verticalScrollBar);
	self.verticalScrollBar:SetSize(10, self.lbl:GetHeight());
	self.verticalScrollBar:SetPosition(self.lbl:GetLeft() + self.lbl:GetWidth(), self.lbl:GetTop());
end
