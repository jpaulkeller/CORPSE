package lotro.ww;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;

import org.jdesktop.swingx.JXTable;

import utils.Application;

public class WardenWizard extends Application implements ActionListener
{
   private SortedMap<String, Gambit> gambits = new TreeMap<String, Gambit>();
   
   private JPanel buttons;

   public Model model;

   public WardenWizard()
   {
      super("Warden Wizard", null, "31 Dec 2012", null);

      this.model = new Model();
      model.addColumn("Level");
      model.addColumn("Gambit");
      model.addColumn("Range");
      model.addColumn("Targets");
      model.addColumn("Builders");
      model.addColumn("Basic");
      model.addColumn("Masteries");
      model.addColumn("Offense");
      model.addColumn("Defense");
      model.addColumn("Healing");
      model.addColumn("Threat");
      model.addColumn("Other");

      loadGambits();

      buttons = new JPanel();

      JButton button = new JButton("Rotations");
      button.setToolTipText("Clear the pattern and matches");
      button.addActionListener(this);
      buttons.add(button);

      JPanel controls = new JPanel(new BorderLayout());
      controls.add(buttons, BorderLayout.NORTH);

      JList<JLabel> list = new JList<JLabel>();
      for (Gambit gambit : gambits.values())
         list.add(new JLabel(gambit.getName() + " - " + gambit.getBuilders()));
      
      JPanel output = new JPanel(new BorderLayout());
      output.setPreferredSize(new Dimension(500, 400));
      output.add(new JScrollPane(getView()), BorderLayout.CENTER);
      
      add(controls, BorderLayout.NORTH);
      add(ComponentTools.getTitledPanel(output, "Gambits"), BorderLayout.CENTER);
   }

   public void actionPerformed(final ActionEvent e) // rotation builder
   {
      RotationBuilder rb = new RotationBuilder(gambits);
      rb.setVisible(true);
   }

   private JXTable getView()
   {
      JXTable tbl = new JXTable(model);
      tbl.setEditable(false);
      tbl.setColumnControlVisible(true);
      tbl.setHorizontalScrollEnabled(true);
      tbl.packAll();
      return tbl;
   }

   private void addGambit(final String name, final String builders, final int level,
         final float range, final int maxTargets,
         final String offense, final String defense, final String heal,
         final String threat, final String other)
   {
      Gambit gambit = new Gambit(name, builders, level, range, maxTargets, offense, defense, heal, threat, other);
      gambits.put(name, gambit);
      
      Vector<Object> row = new Vector<Object>();
      row.add(level);
      row.add(gambit.getName());
      row.add(range);
      row.add(maxTargets);
      row.add(gambit.getBuilders());
      row.add(gambit.getBasic());
      String keys = "";
      for (String key : gambit.getKeys())
         keys += key + ", ";
      row.add(keys);
      
      row.add(offense);
      row.add(defense);
      row.add(heal);
      row.add(threat);
      row.add(other);
      
      model.addRow(row);
   }
   
   private void loadGambits()
   {
      // name, builders, range, max targets, offense, defense, heal, threat, other
      addGambit("Adroit Maneuver", "RYGR", 58, 2.5f, 1, "5", null, null, null, "+Attack Speed");
      addGambit("Aggression", "YGRY", 62, 2.5f, 1, "4", null, null, "Fellowship Leech, Moderate", null);
      addGambit("Boar's Rush", "RYRY", 44, 2.5f, 1, "6 (+%Crit)", null, null, null, "25% Fear; ?% Daze");
      addGambit("Brink of Victory", "YGY", 22,2.5f, 1, "2 (DoT)", "+Evade", null, "Moderate", null);
      addGambit("Celebration of Skill", "GRGR", 40, 2.5f, 1, "4", "+Block", "HoT", "Moderate?", null);
      addGambit("Combination Strike", "RYR", 28, 2.5f, 1, "9", null, null, null, null);
      addGambit("Conviction", "GYGYG", 54, 30f, 0, null, null, "Fellowship HoT", "Fellowship Leech, Moderate", null);
      addGambit("Dance of War", "GYGY", 42, 0, 0, "4", "+Evade/CritDef/Mit", null, "Fellowship Leech, Moderate", null);
      addGambit("Defensive Strike", "GG", 2, 2.5f, 1, "4", "+Block", null, null, "Potency");
      addGambit("Deflection", "GYR", 50, 2.5f, 1, "1", null, null, "-Great", null);
      addGambit("Deft Strike", "RR", 1, 2.5f, 1, "5", null, null, null, "Potency");
      addGambit("Desolation", "YGYGY", 56, 6.2f, 3, "AoE DoT", null, null, null, "25% Fear");
      addGambit("Exultation of Battle", "YRGYG", 60, 6.2f, 10, "AoE DoT", null, "AoE Leech", "AoE ToT", null);
      addGambit("Fierce Resolve", "YRG", 26, 6.2f, 10, "AoE DoT", null, "AoE Leech", null, null);
      addGambit("Goad", "YY", 4, 6, 3, "2 (DoT)", null, null, "AoE", "Potency");
      addGambit("Impressive Flourish", "GY", 9, 2.5f, 0, "1 (DoT)", "+CritDef/Mit", "HoT", null, null);
      addGambit("Maddening Strike", "GYG", 16, 2.5f, 1, "4", "+CritDef/Mit", null, "Fellowship Leech, Slight", null);
      addGambit("Mighty Blow", "RGYR", 38, 2.5f, 1, "6 (-P/E)", null, null, null, null);
      addGambit("Offensive Strike", "RY", 10, 2.5f, 1, "8", null, null, "Moderate?", null);
      addGambit("Onslaught", "RGR", 32, 2.5f, 1, "X", null, null, null, "Interrupt");
      addGambit("Persevere", "GR", 6, 2.5f, 1, "4", "+Block", "HoT", "Moderate?", null);
      addGambit("Piercing Strike", "YRY", 30, 2.5f, 1, "5", null, null, "Moderate", null);
      addGambit("Power Attack", "RGY", 18, 2.5f, 1, "6 (-block?)", null, null, null, null);
      addGambit("Precise Blow", "YR", 12, 2.5f, 1, "4", null, null, "Moderate+", null);
      addGambit("Resolution", "YRGY", 72, -1, -1, "AoE DoT", null, "Aoe Leech", null, null);
      addGambit("Restoration", "GRGRG", 74, -1, -1, "4", null, "HoT", null, null);
      addGambit("Reversal", "RYG", 52, 2.5f, 1, "5", null, null, null, "Remove Corruption");
      addGambit("Safeguard", "GRG", 24, 2.5f, 1, "1", "+Block", "HoT", "Moderate?", null);
      addGambit("Shield Mastery", "GRYG", 34, 0, 0, null, "+Block/Evade", null, null, null);
      addGambit("Shield Tactics", "GYRG", 68, -1, -1, null, "+Mit", null, null, "-Stun");
      addGambit("Shield Up", "GRY", 20, 0, 0, null, "+Block/Evade", null, null, null);
      addGambit("Spear of Virtue", "YRYR", 46, 2.5f, 1, "7", null, null, "Great", null);
      addGambit("Surety of Death", "YGYG", 48, 2.5f, 1, "7 DoT", "+Evade", null, "Great", null);
      addGambit("The Boot", "RG", 3, 2.5f, 1, "2", null, null, null, "Interrupt; 25% Daze");
      addGambit("The Dark Before Dawn", "RGRYR", 64, 2.5f, 1, "X", null, null, null, "+Power");
      addGambit("Unerring Strike", "RGYRG", 50, 2.5f,1, "6 DoT (-avoid)", null, null, null, null);
      addGambit("Wall of Steel", "RGRG", 36, 2.5f, 1, "X", "+Parry", null, null, "Interrupt");
      addGambit("War-Cry", "YG", 13, 10.2f, 10, "AoE DoT", "+Evade", "HoT", "AoE", null);
      addGambit("Warden's Triumph", "RYGRY", 70, -1, -1, "?", "?", "?", "?", "+Stun Immunity");
   }

   public static void main(String[] args)
   {
      ComponentTools.setLookAndFeel();
      ToolTipManager.sharedInstance().setDismissDelay(30000);
      WardenWizard app = new WardenWizard();
      app.open();
   }
}
