package gui.comp;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class DoubleBufferPanel extends JPanel
{
   private static final long serialVersionUID = 1;

   private Image offscreen;

   @Override
   public void invalidate()
   {
      super.invalidate();
      offscreen = null;
   }

   // override so as not to erase the background before painting
   @Override
   public void update (final Graphics g)
   {
      paint (g);
   }

   // paint children into an off-screen buffer, then blast entire image at once
   @Override
   public void paint (final Graphics g)
   {
      if (offscreen == null)
         offscreen = createImage (getSize().width, getSize().height);

      Graphics og = offscreen.getGraphics();
      og.setClip (g.getClipBounds());
      og.setColor (getBackground());
      og.fillRect (0, 0, getSize().width, getSize().height);
      super.paint (og);
      g.drawImage (offscreen, 0, 0, null);
      og.dispose();
   }
}
