package corpse;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import utils.ImagePanel;

public class About extends ImagePanel implements ActionListener
{
   private static final long serialVersionUID = 1L;

   private static final FilenameFilter GIF_FILTER = new FilenameFilter()
   {
      public boolean accept (final File dir, final String name)
      {
         return name.toLowerCase().startsWith ("about") &&
                name.toLowerCase().endsWith ("gif");
      }
   };

   private int numImages;
   private ImageIcon[] icon;
   private double[] x, y;
   private int[] xh, yh;

   private Timer animator;
   private Random rand = new Random();

   public About (final String dir, final String splash)
   {
      super (dir + File.separator + splash);
      
      Dimension dim =
         new Dimension (getImage().getWidth (null), getImage().getHeight (null));
      setPreferredSize (dim);

      JPanel p1 = new JPanel();
      p1.setLayout (new BoxLayout (p1, BoxLayout.Y_AXIS));
      p1.setOpaque (false);

      Font font = new Font ("Comic Sans MS", Font.BOLD, 24);
      addComponent (p1, new JLabel ("DEED ORGANIZER"), font);
      font = new Font ("Comic Sans MS", Font.PLAIN, 18);
      addComponent (p1, new JLabel ("by Mosby of Landroval"), font);
      font = new Font ("Comic Sans MS", Font.PLAIN, 16);
      addComponent (p1, new JLabel ("palantiri.guildportal.com"), font);
      addComponent (p1, new JLabel ("version 1.0"), font);
      addComponent (p1, new JLabel ("released June 2007"), font);

      JPanel p2 = new JPanel();
      p2.setLayout (new BoxLayout (p2, BoxLayout.Y_AXIS));
      p2.setOpaque (false);
      
      font = new Font ("Comic Sans MS", Font.PLAIN, 14);
      JTextArea footnote = new JTextArea 
         ("This software is free.  In-game donations (mailed to Mosby on Landroval) " +
          "are appreciated, and will be used as prizes for Palantiri-hosted events.  " +
          "Thank you!");
      footnote.setOpaque (false);
      footnote.setLineWrap (true);
      footnote.setWrapStyleWord (true);
      footnote.setForeground (Color.yellow);
      addComponent (p2, footnote, font);

      setLayout (new BorderLayout());
      add (p1, BorderLayout.CENTER);
      add (p2, BorderLayout.SOUTH);

      makeImages (dir);
      go();
   }

   private void addComponent (final Container p, final JComponent c, final Font font)
   {
      c.setFont (font);
      c.setAlignmentX (Component.CENTER_ALIGNMENT);
      p.add (c);
   }

   private void makeImages (final String path)
   {
      File file = new File (path);
      numImages = file.listFiles (GIF_FILTER).length;
      
      icon = new ImageIcon[numImages];
      x = new double[numImages];
      y = new double[numImages];
      xh = new int[numImages];
      yh = new int[numImages];
      
      int i = 0;
      for (File f : file.listFiles (GIF_FILTER))
         icon[i++] = new ImageIcon (f.getPath());
   }

   public void go()
   {
      animator = new Timer (66, this);
      animator.setInitialDelay (3000);
      animator.start();
   }

   @Override
   public void paint (final Graphics g)
   {
      super.paint (g);
      for (int i = 0; i < numImages; i++)
      {
         if (x[i] > 3 * i)
         {
            nudge (i);
            if (isVisible())
               g.drawImage (icon[i].getImage(), xh[i], yh[i], this);
         }
         else
         {
            x[i] += .05;
            y[i] += .05;
         }
      }
   }

   public void nudge (final int i)
   {
      x[i] += (double) rand.nextInt (1000) / 8756;
      y[i] += (double) rand.nextInt (1000) / 5432;
      int nudgeX = (int) (((double) getWidth()  / 2) * .8);
      int nudgeY = (int) (((double) getHeight() / 2) * .8);
      xh[i] = (int) (Math.sin(x[i]) * nudgeX) + nudgeX;
      yh[i] = (int) (Math.sin(y[i]) * nudgeY) + nudgeY;
   }

   public void actionPerformed (final ActionEvent e)
   {
      if (isVisible())
         repaint();
      else
         animator.stop();
   }
   
   public static void main (final String[] args)
   {
      About about = new About ("data", "Splash.jpg");
      ComponentTools.open (about, "About");
   }
}

