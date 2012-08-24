package lotro.raid;

import gui.ComponentTools;
import gui.comp.CheckBoxMenu;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import lotro.models.Stats;

public class StatPanel extends JPanel
{
   private CheckBoxMenu<Stats> attrMenu;
   private CheckBoxMenu<Stats> defMenu;
   private CheckBoxMenu<Stats> mitMenu;
   private CheckBoxMenu<Stats> statMenu;
   
   public StatPanel()
   {
      super (new GridLayout (0, 1));
      
      Collection<Stats> stats = new ArrayList<Stats>();
      stats.add (Stats.Might);
      stats.add (Stats.Agility);
      stats.add (Stats.Vitality);
      stats.add (Stats.Will);
      stats.add (Stats.Fate);
      attrMenu = new CheckBoxMenu<Stats> (stats, "Attributes");
      
      stats = new ArrayList<Stats>();
      stats.add (Stats.Block);
      stats.add (Stats.Parry);
      stats.add (Stats.Evade);
      stats.add (Stats.CriticalDefense);
      defMenu = new CheckBoxMenu<Stats> (stats, "Defenses");
      
      stats = new ArrayList<Stats>();
      stats.add (Stats.Resist);
      stats.add (Stats.Common);
      stats.add (Stats.PhysicalMitigation);
      stats.add (Stats.TacticalMitigation);
      mitMenu = new CheckBoxMenu<Stats> (stats, "Mitigations and Resistance");
      
      stats = new ArrayList<Stats>();
      stats.add (Stats.Morale);
      stats.add (Stats.Power);
      stats.add (Stats.Armour);
      stats.add (Stats.Critical);
      stats.add (Stats.Finesse);
      statMenu = new CheckBoxMenu<Stats> (stats, "Other Stats");
      
      stats.clear();
      stats.add (Stats.Morale);
      stats.add (Stats.Armour);
      statMenu.setSelected (stats);
      
      add (attrMenu.getPanel (null));
      add (defMenu.getPanel (null));
      add (mitMenu.getPanel (null));
      add (statMenu.getPanel (null));
   }
   
   public Collection<Stats> getSelected()
   {
      Collection<Stats> stats = new ArrayList<Stats>();
      stats.addAll (attrMenu.getSelected());
      stats.addAll (defMenu.getSelected());
      stats.addAll (mitMenu.getSelected());
      stats.addAll (statMenu.getSelected());
      return stats;
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.open (new StatPanel(), "StatPanel");
   }
}
