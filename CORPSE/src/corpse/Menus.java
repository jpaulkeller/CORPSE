package corpse;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import utils.ImageTools;
import utils.Utils;
import file.FileUtils;

public class Menus
{
   private static final String ABOUT     = "About";
   private static final String EXIT      = "Exit";
   private static final String HELP      = "Help";
   private static final String OPTIONS   = "Options";
   static final         String ROLL      = "Roll";
   private static final String SEARCH    = "Search";
   
   private ActionListener buttonListener = new ButtonListener();
   // private About about;
   private Map<String, JComponent> invokers = new HashMap<String, JComponent>();
   
   private CORPSE app;
   private JMenuBar menus;
   
   public Menus (final CORPSE app)
   {
      this.app = app;
      menus = makeMenus();
   }

   JMenuBar getMenus()
   {
      enable();
      return menus;
   }
   
   private JMenuBar makeMenus()
   {
      JMenuBar menubar = new JMenuBar();
      menubar.add (makeMainMenu());
      menubar.add (makeHelpMenu());
      return menubar;
   }

   private JMenu makeMainMenu()
   {
      JMenu menu = new JMenu ("File");
      menu.setMnemonic ('F');
      menu.add (makeMenuItem (SEARCH, 'S', "20/objects/Magnify.gif", "Search all data files for an entered pattern"));
      menu.add (makeMenuItem (OPTIONS, 'O', "20/gui/Form.gif", "Edit configuration options"));
      menu.addSeparator();
      menu.add (makeMenuItem (EXIT, 'E', "XRed.gif", "Exit this application"));
      return menu;
   }
   
   private JMenu makeHelpMenu()
   {
      JMenu menu = new JMenu (HELP);
      menu.setMnemonic ('H');
      menu.add (makeMenuItem (ABOUT, 'A', "20/markers/SignInformation.gif", 
                              "Show infomation about this application"));
      menu.add (makeMenuItem (HELP, 'H', "20/gui/Help.gif", "How do I use this?"));
      return menu;
   }

   private JMenuItem makeMenuItem (final String label, final char mnemonic, 
                                   final String icon, final String tip)
   {
      JMenuItem mi = new JMenuItem (label);
      mi.setMnemonic (mnemonic);
      mi.setToolTipText (tip);
      if (icon != null)
         mi.setIcon (ImageTools.getIcon ("icons/" + icon));
      mi.addActionListener (buttonListener);
      invokers.put (label, mi);
      return mi;
   }
   
   JButton makeButton (final String command, final String iconName, final String tip)
   {
      JButton button = new JButton (ImageTools.getIcon (iconName));
      button.setMargin (new Insets (0, 0, 0, 0));
      button.setActionCommand (command);
      button.setToolTipText (tip);
      button.addActionListener (buttonListener);
      invokers.put (command, button);
      return button;
   }
   
   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         String cmd = e.getActionCommand();         
         if      (cmd.equals (ABOUT))     about();
         else if (cmd.equals (EXIT))      System.exit (0);
         else if (cmd.equals (HELP))      showFile ("data/readme.txt", "CORPSE Quick Help"); 
         else if (cmd.equals (OPTIONS))   setOptions();
         else if (cmd.equals (ROLL))      app.roll();
         else if (cmd.equals (SEARCH))    app.search();
         else
         {
            Toolkit.getDefaultToolkit().beep();
            app.setText ("Unsupported command: " + cmd);
         }
      }
   }
   
   private void enable()
   {
      // invokers.get (CLEAR_A).setEnabled (chars);
   }
   
   private void about()
   {
      JOptionPane.showMessageDialog
         (app.getMainPanel(), "Version 0.7 beta",
          "About CORPSE", JOptionPane.INFORMATION_MESSAGE);

      /*
      if (about == null)
         about = new About ("data", "Splash.jpg");
      
      String title = "About CORPSE";
      JFrame frame = (JFrame) app.mainPanel.getTopLevelAncestor();
      JDialog window = new JDialog (frame, title, true);
      window.add (about);
      window.pack();
      Utils.centerComponent (window);
      window.setVisible (true);
      */
   }
   
   private void setOptions()
   {
      // options.configure ((JFrame) app.mainPanel.getTopLevelAncestor());
      enable();
   }
   
   private void showFile (final String file, final String title)
   {
      String s = FileUtils.getText (file);
      JTextArea text = new JTextArea (s);
      text.setEditable (false);
      
      JPanel panel = new JPanel();
      panel.setBorder (Utils.BORDER);
      panel.add (text);
      
      JOptionPane.showMessageDialog
         (app.getMainPanel(), panel, title, JOptionPane.INFORMATION_MESSAGE);
   }
}
