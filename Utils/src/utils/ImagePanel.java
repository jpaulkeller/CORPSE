package utils;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel
{
   private static final long serialVersionUID = 1L;
   
   private Image image = null;
   
   public ImagePanel()
   {    
   }

   public ImagePanel (final Image image)
   {
      setImage (image);
   }
  
   public ImagePanel (final String path)
   {
      setImage (new ImageIcon (path).getImage());
   }
   
   public void setImage (final Image image)
   {
      this.image = image;
   }
   
   public Image getImage()
   {
      return image;
   }
   
   @Override
   public void paintComponent (final Graphics g)
   {
      super.paintComponent (g); // paint background
      if (image != null)
      {
         int height = getSize().height;
         int width = getSize().width;         
         g.drawImage (image, 0, 0, width, height, this);
      }
   }
}
