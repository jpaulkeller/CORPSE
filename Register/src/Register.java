import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Register 
{
   public static void main(String[] args) 
   {
      JPanel panel = new JPanel(new GridLayout(4, 3));
      
      ButtonListener listener = new ButtonListener();
      
      JButton b = new JButton("soda");
      panel.add(b);
      b.addActionListener(listener);

      b = new JButton("sub");
      panel.add(b);
      b.addActionListener(listener);
      
      b = new JButton("fries");
      panel.add(b);
      b.addActionListener(listener);
      
      b = new JButton("salad");
      panel.add(b);
      b.addActionListener(listener);
      
      b = new JButton("shake");
      panel.add(b);
      b.addActionListener(listener);
      
      JFrame frame = new JFrame("Register");
      frame.add(panel); // , BorderLayout.CENTER);
      frame.setVisible(true);
   }
   
   static class ButtonListener implements ActionListener
   {
      @Override
      public void actionPerformed(ActionEvent e)
      {
         System.out.println(e.getActionCommand());
      }
   }
}
