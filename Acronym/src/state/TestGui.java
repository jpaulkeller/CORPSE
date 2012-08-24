package state;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import str.Token;

public class TestGui extends JFrame implements ActionListener
{
   private static final long serialVersionUID = 0;

   private ComponentEnabler enabler;
   private StateModel stateModel;
   private String allState = "ALL";

   public TestGui()
   {
      stateModel = new StateModel();
      enabler = new ComponentEnabler (stateModel);

      String phrase = "this is a test";

      JPanel wordPanel = new JPanel();
      JButton btn = new JButton (allState);
      btn.addActionListener (this);
      wordPanel.add (btn);

      // add a button for each word
      String[] words = Token.tokenize (phrase);
      for (int i = 0; i < words.length; i++)
         wordPanel.add (makeButton (words[i]));

      // add a state button for each letter
      JPanel letterPanel = new JPanel();
      List<String> letters = new ArrayList<String>();
      for (int i = 0; i < phrase.length(); i++)
         if (!letters.contains (phrase.charAt (i) + ""))
            letters.add (phrase.charAt (i) + "");
      letters.remove (" ");
      Collections.sort (letters);
      for (String letter : letters)
         letterPanel.add (makeButton (letter));

      Container c = getContentPane();
      c.setLayout (new BorderLayout());

      c.add (wordPanel, BorderLayout.NORTH);
      c.add (letterPanel, BorderLayout.CENTER);

      setTitle ("State Class TestGui");
      pack();
      setVisible (true);
   }

   public JButton makeButton (final String word)
   {
      JButton btn = new JButton (word);

      // add a state for EACH letter
      String[] active = new String[word.length() + 1];
      active[0] = allState;
      for (int i = 0; i < word.length(); i++)
         active[i + 1] = word.charAt (i) + "";
      enabler.enableWhen (btn, active);

      btn.addActionListener (this);

      return btn;
   }

   public JButton makeButton (final String l1, final String l2)
   {
      JButton btn = new JButton (l1 + "&" + l2);

      // add a state for ALL letters
      String[] active = new String[] { allState, l1, l2 };
      enabler.enableWhen (btn, active);

      btn.setActionCommand (l1 + l2);
      btn.addActionListener (this);

      return btn;
   }

   public void actionPerformed (final ActionEvent e)
   {
      String command = e.getActionCommand();

      if ((command.equals (allState)) || (command.length() == 1))
      {
         System.out.println ("setState: " + command);
         stateModel.setState (command); // test setState
      }
      else
      {
         System.out.print ("addState: " + command.charAt (0));
         stateModel.setState (command.charAt (0) + "");
         for (int i = 1; i < command.length(); i++)
         {
            System.out.print (", " + command.charAt (i));
            stateModel.addState (command.charAt (i) + ""); // test addState
         }
         System.out.println();
      }
   }

   public static void main (final String[] args)
   {
      new TestGui();
   }
}
