package lotro.ww;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
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
   private Map<String, Gambit> gambits;
   
   private JPanel buttons;

   public Model model;

   public WardenWizard()
   {
      super("Warden Wizard", null, "31 Dec 2012", null);

      populateModel();

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
   
   private void populateModel()
   {
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

      gambits = GambitData.load();
      for (Gambit gambit : gambits.values())
         addGambitToModel(gambit);
   }

   private void addGambitToModel(final Gambit gambit)
   {
      Vector<Object> row = new Vector<Object>();
      row.add(gambit.getLevel());
      row.add(gambit.getName());
      row.add(gambit.getRange());
      row.add(gambit.getMaxTargets());
      row.add(gambit.getBuilders());
      row.add(gambit.getBasic());
      
      String keys = "";
      for (String key : gambit.getKeys())
         keys += key + ", ";
      row.add(keys);
      
      row.add(gambit.getOffense());
      row.add(gambit.getDefense());
      row.add(gambit.getHeal());
      row.add(gambit.getThreat());
      row.add(gambit.getOther());
      
      model.addRow(row);
   }
   
   public static void main(String[] args)
   {
      ComponentTools.setLookAndFeel();
      ToolTipManager.sharedInstance().setDismissDelay(30000);
      WardenWizard app = new WardenWizard();
      app.open();
   }
}
