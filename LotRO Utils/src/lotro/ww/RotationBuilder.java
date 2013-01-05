package lotro.ww;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import state.ComponentEnabler;
import state.StateModel;

public class RotationBuilder extends JFrame implements ActionListener
{
   private JTextField rotationKeys;
   private JTextField rotationGambits;
   private ComponentEnabler enabler;
   private StateModel stateModel;

   public RotationBuilder(final Map<String, Gambit> gambits)
   {
      stateModel = new StateModel();
      enabler = new ComponentEnabler (stateModel);
      stateModel.setState(StateModel.DEFAULT_STATE);

      JPanel grid = new JPanel(new GridLayout(0, 3));

      for (Gambit gambit : gambits.values())
      {
         JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
         row.setToolTipText(gambit.getToolTip());
         
         JLabel name = new JLabel(gambit.getName());
         name.setToolTipText(gambit.getToolTip());
         row.add(name);
         
         for (String keys : gambit.getKeys())
            row.add(makeButton(gambit, keys));
         // JPanel withName = ComponentTools.getTitledPanel(row, gambit.getName());
         grid.add(row); 
      }
      
      JButton reset = new JButton("Reset");
      reset.addActionListener(this);
      
      JPanel buttons = new JPanel();
      buttons.add(reset);

      rotationKeys = new JTextField(50);
      rotationGambits = new JTextField(50);
      JPanel rotation = new JPanel(new GridLayout(2, 1));
      rotation.add(rotationKeys);
      rotation.add(rotationGambits);
      
      Container c = getContentPane();
      c.setLayout (new BorderLayout());

      c.add (buttons, BorderLayout.NORTH);
      c.add (grid, BorderLayout.CENTER);
      c.add (rotation, BorderLayout.SOUTH);

      setTitle ("Warden Rotation Builder");
      pack();
   }

   public JButton makeButton (final Gambit gambit, final String keys)
   {
      JButton btn = new GambitButton (gambit, keys);
      btn.setActionCommand(keys);
      btn.addActionListener (this);
      btn.setToolTipText(gambit.getToolTip());

      // add a state for each key
      String[] used = new String[keys.length()];
      for (int i = 0; i < keys.length(); i++)
         used[i] = keys.charAt (i) + "";
      
      enabler.enableWhen (btn, StateModel.DEFAULT_STATE);
      enabler.disableWhen (btn, used);

      return btn;
   }

   public void actionPerformed (final ActionEvent e)
   {
      String command = e.getActionCommand();
      
      if (command.equals("Reset"))
      {
         rotationKeys.setText("");
         rotationGambits.setText("");
         stateModel.setState(StateModel.DEFAULT_STATE);
      }
      else
      {
         GambitButton button = (GambitButton) e.getSource();
         rotationKeys.setText(rotationKeys.getText() + command + ", ");
         rotationGambits.setText(rotationGambits.getText() + button.getGambit().getName() + ", ");
         for (int i = 0; i < command.length(); i++)
            stateModel.addState (command.charAt (i) + "");
      }
   }
   
   class GambitButton extends JButton
   {
      private Gambit gambit;
      
      public GambitButton(final Gambit gambit, final String keys)
      {
         super(keys);
         this.gambit = gambit;
      }
      
      public Gambit getGambit()
      {
         return gambit;
      }
   }
}
