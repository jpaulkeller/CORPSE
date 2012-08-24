package utils;

import gui.ComponentTools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public final class ImageTools
{
   private ImageTools()
   {
      // utility class; prevent instantiation
   }
   
   /**
    * Create an ImageIcon from the given file name.  This method supports
    * reading the icon file out of a jar.
    */
   public static ImageIcon getIcon (final String iconName)
   {
      ImageIcon icon = null;

      if (iconName != null && !iconName.equals (""))
      {
         try
         {
            URL url = ComponentTools.class.getResource ("/" + iconName);
            if (url != null)
               icon = new ImageIcon (url);
            if (icon == null && new File (iconName).exists())
               icon = new ImageIcon (iconName);
         }
         catch (Throwable x)
         {
            System.err.println (x);
         }
      }

      return icon;
   }
   
   /**
    * Create an ImageIcon from the given image files.  This method supports
    * reading the icon files out of a jar.  This intent of this method is to 
    * allow the creation of common icons composed of multiple parts: the main symbol
    * (such as a Folder); and a second "action" indicator (such as a small plus-
    * sign to indicate an Add operation).  All symbols should be the same size 
    * (they are overlaid on each other, the first one is on the bottom).  
    * Subsequent symbols typically are partially or mostly transparent.
    */
   public static ImageIcon getIcon (final String... iconNames)
   {
      ImageIcon icon = getIcon (iconNames[0]);
      BufferedImage bi = createImage (icon.getIconWidth(), icon.getIconHeight());
      Graphics2D g = bi.createGraphics();
      g.drawImage (icon.getImage(), 0, 0, null);

      // layer the remaining images over the earlier images 
      g.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER));
      for (int i = 1; i < iconNames.length; i++)
      {
         icon = getIcon (iconNames[i]);
         if (icon != null)
            g.drawImage (icon.getImage(), 0, 0, null);
      }

      return new ImageIcon (bi);
   }
   
   public static BufferedImage createImage (final int width, final int height)
   {
      BufferedImage image = null;
      if (width > 0 && height > 0)
         image = new BufferedImage (width, height, BufferedImage.TYPE_INT_ARGB);
      return image;
   }

   public static BufferedImage imageToBufferedImage (final Image image)
   {
      return imageToBufferedImage (image, BufferedImage.TYPE_INT_ARGB);
   }

   public static BufferedImage imageToBufferedImage (final Image image,
                                                     final int imageType)
   {
      return imageToBufferedImage (image, imageType, null);
   }

   public static BufferedImage imageToBufferedImage (final Image image,
                                                     final ImageObserver o)
   {
      return imageToBufferedImage (image, BufferedImage.TYPE_INT_ARGB, o);
   }

   public static BufferedImage imageToBufferedImage (final Image image,
                                                     final int imageType,
                                                     final ImageObserver observer)
   {
      BufferedImage bi = null;
      if (image != null)
      {
         int w = image.getWidth (observer);
         int h = image.getHeight (observer);

         bi = new BufferedImage (w, h, imageType);

         Graphics g = bi.getGraphics();
         g.drawImage (image, 0, 0, observer);
         g.dispose();
         bi.flush();
      }
      return bi;
   }

   public static boolean imageToJpeg (final Image image, final OutputStream os)
   {
      boolean status = false;
      try
      {
         status = imageToJpeg (image, os, null);
      }
      catch (IOException e)
      {
         System.err.println ("ImageTools.imageToJpeg(): " + e);
      }
      return status;
   }

   public static boolean imageToJpeg (final Image image,
                                      final OutputStream os,
                                      final ImageObserver obs) throws IOException
   {
      return imageToJpeg (image, os, obs, 0.75f);
   }

   public static boolean imageToJpeg (final Image image,
                                      final OutputStream os,
                                      final ImageObserver obs,
                                      final float quality) throws IOException
   {
      boolean status = false;

      // Note: this code used to check for "image instanceof BufferedImage",
      // but that resulted in the following error:
      //    ImageFormatException: 4 band JFIF files imply CMYK encoding.
      //    Param block indicates alternate encoding.

      BufferedImage bi = imageToBufferedImage (image, BufferedImage.TYPE_INT_RGB, obs);
      if (bi != null)
      {
         try
         {
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder (os);
            if (quality < 1)    // set the quality
            {
               JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam (bi);
               param.setQuality (quality, false);
               encoder.setJPEGEncodeParam (param);
            }

            encoder.encode (bi);
            status = true;
         }
         catch (IOException ioe)
         {
            System.err.println ("ImageTools.imageToJpeg(): " + ioe);
         }
         catch (ImageFormatException ife)
         {
            System.err.println ("ImageTools.imageToJpeg(): " + ife);
         }
         bi.flush();
      }
      os.close();

      return status;
   }

   public static boolean saveImageAsJpeg (final String path, final Image image)
   {
      return saveImageAsJpeg (path, image, null);
   }

   public static boolean saveImageAsJpeg (final String path, final Image image,
                                          final ImageObserver obs)
   {
      return saveImageAsJpeg (path, image, null, 0.75f);
   }

   public static boolean saveImageAsJpeg (final String path, final Image image,
                                          final ImageObserver obs,
                                          final float quality)
   {
      boolean status = false;
      try
      {
         File dir = new File (path).getParentFile();
         if ((dir != null) && !dir.exists())
            if (!dir.mkdirs()) // create any missing directories
               System.err.println ("ImageTools.saveImageAsJpeg() mkdirs failed!");

         FileOutputStream out = new FileOutputStream (path);
         status = ImageTools.imageToJpeg (image, out, obs, quality);
         out.close();
      }
      catch (IOException e)
      {
         System.err.println ("ImageTools.saveImageAsJpeg(): " + e);
      }
      return status;
   }

   public static boolean isImageMonochrome (final Image image)
   {
      return isImageMonochrome 
      (imageToBufferedImage (image, BufferedImage.TYPE_INT_ARGB));
   }

   public static boolean isImageMonochrome (final BufferedImage buf)
   {
      if (buf == null)
         return false;

      for (int x = 0, w = buf.getWidth(), h = buf.getHeight(); x < w; x++)
      {
         for (int y = 0; y < h; y++)
         {
            int rgb = buf.getRGB (x, y);
            if ((rgb != 0) && (rgb != Color.BLACK.getRGB()))
               return false;
         }
      }
      return true;
   }
   
   /** Scale the image without changing the aspect ratio. */
   
   public static ImageIcon scaleImage (final ImageIcon icon, final double width,
                                       final double height, final int scalingHints,
                                       final Component watcher)
   {
      double imageW = icon.getIconWidth();
      double imageH = icon.getIconHeight();
      double srcAspect = imageW / imageH;
      double dstAspect = width / height;
      double scale = srcAspect >= dstAspect ? width / imageW : height / imageH;
      int w = (int) Math.round (scale * imageW);
      int h = (int) Math.round (scale * imageH);

      Image image = ImageTools.scaleImage (icon.getImage(), w, h, scalingHints, watcher);
      return new ImageIcon (image, icon.getDescription());
   }
   
   public static double getScaleFactor (final ImageIcon icon, final double width,
                                        final double height, final int scalingHints,
                                        final Component watcher)
   {
      double imageW = icon.getIconWidth();
      double imageH = icon.getIconHeight();
      double srcAspect = imageW / imageH;
      double dstAspect = width / height;
      double scale = srcAspect >= dstAspect ? width / imageW : height / imageH;
      return scale;
   }
   
   /**
    * Scale the given image, but use a MediaTracker to wait for it to
    * finish (on the given watcher component). */

   public static Image scaleImage (final Image image, 
                                   final int width, final int height,
                                   final int scalingHints, 
                                   final Component watcher)
   {
      Image scaledImage = null;
      
      if (width > 0 || height > 0)
      {
         int w = width;
         int h = height;
         // if either dimension is unspecified, scale it proportionally
         if (w <= 0)
         {
            double curWidth = image.getWidth (null);
            double curHeight = image.getHeight (null);
            w = (int) Math.round (curWidth * h / curHeight);
         }
         else if (h <= 0)
         {
            double curWidth = image.getWidth (null);
            double curHeight = image.getHeight (null);
            h = (int) Math.round (curHeight * w / curWidth);
         }
         
         scaledImage = image.getScaledInstance (w, h, scalingHints);
         ImageIcon icon = new ImageIcon (scaledImage); // Media Tracker
         icon.setImageObserver (watcher);
         scaledImage = icon.getImage();
         // if (watcher != null)
         //    scaledImage.flush(); // TBD
      }
      else
      {
         System.err.println ("ImageTools invalid w/h: " + width + "/" + height);
         Thread.dumpStack();
      }

      return scaledImage;
   }

   public static BufferedImage copyImage (final Image image)
   {
      BufferedImage bi = createImage (image.getWidth (null),
                                      image.getHeight (null));
      Graphics2D g2d = bi.createGraphics();
      g2d.drawImage (image, 0, 0, null);
      g2d.dispose();
      return bi;
   }

   public static ImageIcon swapColor (final ImageIcon icon, 
                                      final int fromRGB, final int toRGB)
   {
      Image image = icon.getImage();
      BufferedImage buf = new BufferedImage (image.getWidth (null),
                                             image.getHeight (null),
                                             BufferedImage.TYPE_INT_ARGB);
      Graphics g = buf.getGraphics();
      g.drawImage (image, 0, 0, null);

      for (int x = 0, w = buf.getWidth(), h = buf.getHeight(); x < w; x++)
         for (int y = 0; y < h; y++)
            if (buf.getRGB (x, y) == fromRGB)
               buf.setRGB (x, y, toRGB);

      return new ImageIcon (buf);
   }
   
   public static BufferedImage getImage (final Component c)
   {
      return getImage (c, c.getWidth(), c.getHeight());
   }
   
   public static BufferedImage getImage (final Component c, final int w, final int h)
   {
      // support transparency
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      GraphicsConfiguration gc = gd.getDefaultConfiguration();
       
      // create an image that supports transparent pixels
      BufferedImage bi = gc.createCompatibleImage (w, h, Transparency.BITMASK);
       
      Graphics2D g = bi.createGraphics();
      c.paint (g);
      return bi;
   }   
   
   public static void main (final String[] args)
   {
      ImageIcon icon;
      String iconName = "icons/20/documents/FolderOpen.gif";
      // icon = ImageTools.getIcon (iconName);
      // ComponentTools.open (new JLabel (icon), iconName);
      
      String action = "icons/20/actions/DeleteTR.gif";
      icon = ImageTools.getIcon (iconName, action);
      ComponentTools.open (new JLabel (icon), iconName);
   }
}
