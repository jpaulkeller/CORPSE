package lotro.cal;

import gui.ComponentTools;
import gui.comp.ImagePanel;
import gui.form.FileItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;
import gui.form.valid.FileValidator;
import gui.form.valid.StatusEvent;
import gui.form.valid.StatusListener;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import utils.ImageTools;

public final class CalendarOverlay
{
   private FileItem imageFile;
   private JFrame frame;
   private ImagePanel imagePanel;
   private JPanel panel;
   private JButton exportButton;
   
   private CalendarOverlay()
   {
      String dir = System.getProperty ("user.home") + "/Desktop";
      FileValidator validator = new FileValidator (true);
      validator.setSuffixes ("gif", "jpg");

      imageFile = new FileItem ("Select Image File", null, 50, true);
      imageFile.setMode (JFileChooser.FILES_ONLY);
      imageFile.setDefaultDir (dir);
      imageFile.setValidator (validator);
      imageFile.setValue (new File (dir));
      imageFile.addStatusListener (new FileListener());

      exportButton = ComponentTools.makeButton
      ("Save", "icons/Convert.gif", false, new ButtonListener(), "tip goes here");
      
      imagePanel = new ImagePanel();
      
      panel = new JPanel (new BorderLayout());
      panel.add (imageFile.getTitledPanel(), BorderLayout.NORTH);
      panel.add (imagePanel, BorderLayout.CENTER);
   }
   
   private void open()
   {
      frame = new JFrame ("Palantiri Calendar Creator");
      frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
      
      frame.add (panel, BorderLayout.NORTH);
      frame.add (exportButton, BorderLayout.EAST);
      frame.pack();
      ComponentTools.centerComponent (frame);
      frame.setVisible (true);
   }

   class FileListener implements StatusListener, ValueChangeListener
   {
      public void valueChanged (final ValueChangeEvent e)
      {
         if (imageFile.isValid())
         {
            System.out.println (imageFile.getFile()); // TBD
            ImageIcon icon = ImageTools.getIcon (imageFile.getPath());
            imagePanel.setOverlay (icon.getImage());
            imagePanel.repaint(); // TBD
         }
      }
      
      public void stateChanged (final StatusEvent e)
      {
         exportButton.setEnabled (e.getStatus());
      }
   }

   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         exportButton.setEnabled (false);
      }
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      CalendarOverlay app = new CalendarOverlay();
      app.open();
   }
}
