package lotro.ww;

import gui.ComponentTools;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;

import state.ComponentEnabler;
import state.StateModel;

public class RotationBuilder extends JFrame implements ActionListener
{
   private JTextField rotationKeys;
   private JTextArea rotationGambits;
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
         grid.add(row); 
      }
      
      JButton reset = new JButton("Reset");
      reset.addActionListener(this);
      
      JPanel buttons = new JPanel();
      buttons.add(reset);

      rotationKeys = new JTextField(50);
      rotationGambits = new JTextArea(6, 50);
      JPanel rotation = new JPanel(new BorderLayout());
      rotation.add(rotationKeys, BorderLayout.NORTH);
      rotation.add(rotationGambits, BorderLayout.CENTER);
      
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

      enabler.enableWhen (btn, StateModel.DEFAULT_STATE);
      
      String builder = Gambit.keyMapping.getKey(keys);  
      if (builder != null && builder.length() == 2 && builder.charAt(0) == builder.charAt(1)) // handle RR,GG,YY
         enabler.disableWhen (btn, keys);
      else
         for (char c : keys.toCharArray()) // add a state for each mastery key
         {
            builder = Gambit.keyMapping.getKey(c + "");
            if (builder.length() > 1) // don't disable for R, G, Y
               enabler.disableWhen (btn, c + "");
         }

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
         rotationGambits.append(command + ": " + button.getGambit() + "\n");
         
         String builder = Gambit.keyMapping.getKey(command);  
         if (builder != null && builder.length() == 2 && builder.charAt(0) == builder.charAt(1)) // handle RR,GG,YY
            stateModel.addState (command);
         else
            for (char c : command.toCharArray())
            {
               builder = Gambit.keyMapping.getKey(c + "");
               System.out.println(c + " > " + builder);
               if (builder.length() > 1) // no state for for R, G, Y
                  stateModel.addState (c + "");
            }
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
   
   public static void main(String[] args)
   {
      Map<String, Gambit> gambits = GambitData.load();
      
      ComponentTools.setLookAndFeel();
      ToolTipManager.sharedInstance().setDismissDelay(30000);
      RotationBuilder rb = new RotationBuilder(gambits);
      ComponentTools.centerComponent(rb);
      rb.setVisible(true);
   }
}
