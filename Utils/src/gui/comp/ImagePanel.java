package gui.comp;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * Supports an overlay (a partially transparent image drawn on top of the 
 * primary background image) and double-buffering.
 */
public class ImagePanel extends JPanel
{
   private static final long serialVersionUID = 1L;
   
   private Image overlay;
   private Image offscreen;
   
   public Image getOverlay()
   {
      return overlay;
   }

   public void setOverlay (final Image overlay)
   {
      this.overlay = overlay;
   }

   @Override
   public void invalidate()
   {
      super.invalidate();
      offscreen = null;
   }

   @Override // don't erase the background before painting
   public void update (final Graphics g)
   {
      paint (g);
   }

   // Paint children into an off-screen buffer, then blast entire image at
   // once.
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
      if (overlay != null)
         // og.drawImage (overlay, 0, 0, null);
         // scale them image to fit -- we may not want to do this
         og.drawImage (overlay, 0, 0, getSize().width, getSize().height, 0, 0, 
                       overlay.getWidth (null), overlay.getHeight (null), null);
      g.drawImage (offscreen, 0, 0, null);
      og.dispose();
   }
}
