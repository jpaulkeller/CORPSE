package lotro.deed;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class OptionEditor
{
   private SpinnerNumberModel minModel;
   private SpinnerNumberModel maxModel;
   private JSpinner minGroupChooser;
   private JSpinner maxGroupChooser;
   private JTextField rosterField;
   
   private JPanel panel;
   private JDialog dialog;
   private JFrame owner;
   
   public OptionEditor (final JFrame owner)
   {
      this.owner = owner;
      
      ChangeListener listener = new SpinnerListener();
      minModel = new SpinnerNumberModel (2, 1, 6, 1); 
      maxModel = new SpinnerNumberModel (6, 2, 6, 1); 
      minGroupChooser = new JSpinner (minModel); 
      maxGroupChooser = new JSpinner (maxModel);
      minGroupChooser.addChangeListener (listener);
      maxGroupChooser.addChangeListener (listener);
      
      rosterField = new JTextField (30);
      
      JPanel sizePanel = new JPanel (new GridLayout (2, 2));
      sizePanel.setBorder (BorderFactory.createTitledBorder ("Fellowship Size"));
      sizePanel.add (new JLabel ("Minimum"));
      sizePanel.add (minGroupChooser);
      sizePanel.add (new JLabel ("Maximum"));
      sizePanel.add (maxGroupChooser);
      
      JPanel urlPanel = new JPanel();
      urlPanel.setBorder (BorderFactory.createTitledBorder ("GuildPortal Roster URL"));
      urlPanel.add (rosterField);
      
      ActionListener buttonListener = new ButtonListener();
      JButton ok = new JButton ("OK");
      JButton cancel = new JButton ("Cancel");
      ok.addActionListener (buttonListener);
      cancel.addActionListener (buttonListener);
      
      JPanel mainPanel = new JPanel (new BorderLayout());
      mainPanel.add (sizePanel, BorderLayout.NORTH);
      mainPanel.add (urlPanel, BorderLayout.SOUTH);
      
      JPanel buttons = new JPanel();
      buttons.add (ok);
      buttons.add (cancel);

      panel = new JPanel (new BorderLayout());
      panel.add (mainPanel, BorderLayout.CENTER);
      panel.add (buttons, BorderLayout.SOUTH);
   }

   public void open (final Options options)
   {
      if (dialog == null)
      {
         dialog = new JDialog (owner, "Options");
         dialog.setModal (true);
         dialog.add (panel);
         dialog.pack();
         ComponentTools.centerComponent (dialog);
      }
      
      minGroupChooser.setValue (options.getMinGroupSize());
      maxGroupChooser.setValue (options.getMaxGroupSize());
      rosterField.setText (options.getRosterURL());
      
      dialog.setVisible (true);
      
      options.setMinGroupSize ((Integer) minGroupChooser.getValue());
      options.setMaxGroupSize ((Integer) maxGroupChooser.getValue());
      options.setRosterURL (rosterField.getText());
      options.write();
   }
   
   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         // String cmd = e.getActionCommand();
         // if (cmd.equals ("OK"))
         dialog.setVisible (false);
      }
   }
   
   class SpinnerListener implements ChangeListener
   {
      public void stateChanged (final ChangeEvent e)
      {
         // min must be less than max, so update the constraints as needed
         int value = (Integer) ((JSpinner) e.getSource()).getValue();
         if (e.getSource() == minGroupChooser)
            maxModel.setMinimum (Math.max (2, value));
         else
            minModel.setMaximum (Math.min (6, value));
      }
   }
   
   public static void main (final String[] args)
   {
      Options options = new Options();
      options.configure (null); // uses OptionEditor
      
      System.out.println ("Min Group: " + options.getMinGroupSize());
      System.out.println ("Max Group: " + options.getMaxGroupSize());
      System.out.println ("Roster URL: " + options.getRosterURL());
   }
}
