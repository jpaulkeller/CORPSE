package lotro.my.reports;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import lotro.models.Character;
import lotro.models.CharacterListModel;
import lotro.raid.CharacterSelector;

public final class Test extends JApplet
{
   private CharacterListModel characters;
   private CharacterSelector charChooser;
   private JProgressBar progress;
   
   public Test()
   {
      charChooser = new CharacterSelector (null, null, false);
      charChooser.setLevelRange (50, Character.MAX_LEVEL);
      CharListener charListener = new CharListener();
      charChooser.addMouseListener (charListener);
      charChooser.addObserver (charListener);
      Component comp = charChooser.getComponent(); 
      comp.setPreferredSize (comp.getMinimumSize());
   }
   
   @Override
   public String getAppletInfo()
   {
      return "Name: Test";
   }

   // Returns an array of strings for the parameters understood by this applet.
   @Override
   public String[][] getParameterInfo()
   {
      String[][] info =
      { 
         { "date", "String", "Date/time to which applet will countdown" },
      };
      return info;
   }

   @Override
   public void init()
   {
      try
      {
         SwingUtilities.invokeAndWait(new Runnable()
         {
            public void run() 
            {
               createGUI();
            }
         });
      } 
      catch (Exception x) 
      {
         x.printStackTrace();
      }
   }
   
   private void createGUI() 
   {
      JLabel label = new JLabel ("You are successfully running a Swing applet!");
      label.setHorizontalAlignment (JLabel.CENTER);
      label.setBorder (BorderFactory.createMatteBorder (1, 1, 1, 1, Color.black));
      getContentPane().add (label, BorderLayout.CENTER);
   }
   
   @Override
   public void start()
   {
      System.out.println ("Dashboard.start()"); // TBD
   }

   @Override
   public void stop()
   {
      System.out.println ("Dashboard.stop()"); // TBD
   }
   
   class CharListener extends MouseAdapter implements Observer
   {
      @Override
      public void mouseClicked (final MouseEvent e)
      {
         if (e.getClickCount() > 1) // support double-click
         {
            Character ch = charChooser.getSelected();
            if (ch != null)
               characters.add (ch);
         }
      }

      public void update (final Observable o, final Object arg)
      {
         progress.setString ((String) arg);
         progress.setIndeterminate (arg.toString().contains ("Loading"));
      }
   }
}
