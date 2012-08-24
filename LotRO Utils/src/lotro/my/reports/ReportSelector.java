package lotro.my.reports;

import gui.ButtonGroup;
import gui.ComponentTools;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import utils.ImageTools;

public class ReportSelector
{
   private ButtonGroup buttons;
   private ActionListener listener;
   private ActionListener iconUpdater;
   private JPanel panel;
   private Map<AbstractButton, Icon> icons = new HashMap<AbstractButton, Icon>();
   
   public ReportSelector (final ActionListener listener)
   {
      this.listener = listener;
      iconUpdater = new IconUpdater();
      panel = new JPanel();
      buttons = new ButtonGroup();
      buttons.setMaxSelectable (1);
      
      addButton ("Classes", "20/gui/GraphPie.gif", "A pie chart by character class");
      addButton ("Class/Level", "20/gui/GraphBar.gif", "A bar chart by class and level");
      addButton ("Roster", "20/people/Users.gif", "A simple roster table");
      addButton ("Stats", "20/documents/DocumentList.gif", "A table showing character stats");
      addButton ("Crafters", "20/objects/Hammer.gif", "A table showing all Master Crafters");
      addButton ("Equipment", "20/map/COA.gif", "A table showing all Equipped Gear");
   }
   
   private JRadioButton addButton (final String label, final String iconFile,
                                   final String toolTip)
   {
      JRadioButton button = new JRadioButton (label, false);
      button.setMargin (new Insets (0, 0, 0, 0));
      button.setToolTipText (toolTip);
      
      button.addActionListener (iconUpdater);
      if (listener != null)
         button.addActionListener (listener);
      
      buttons.add (button);
      panel.add (button);
      
      ImageIcon icon = ImageTools.getIcon ("icons/" + iconFile);
      icons.put (button, icon);
      
      return button;
   }
   
   // <option value="tpc">Pie Chart (by Class)</option>
   // <option value="tstats">Monster Stats</option>

   public Component getComponent()
   {
      return panel; 
   }
   
   private class IconUpdater implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         for (AbstractButton button : buttons.getButtons())
            button.setIcon (null);
         AbstractButton button = (AbstractButton) e.getSource();
         if (button.isSelected())
            button.setIcon (icons.get (button));
      }
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      ReportSelector rs = new ReportSelector (null);
      ComponentTools.open (rs.getComponent(), "ReportSelector");
   }
}
