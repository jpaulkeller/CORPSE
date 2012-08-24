package gui;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Since the Color class does not have a setAlpha() method, this class
 * provides a convenient way to construct a translucent color.
 */
public class TranslucentColor extends Color
{
   private static final long serialVersionUID = 0;

   public static final TranslucentColor CLEAR =
      new TranslucentColor (0, 0, 0, 0f);

   // translucent versions of the java.awt.Color constants
   public static final Color TR_BLACK      = new TranslucentColor (Color.black);
   public static final Color TR_BLUE       = new TranslucentColor (Color.blue);
   public static final Color TR_CYAN       = new TranslucentColor (Color.cyan);
   public static final Color TR_DARK_GRAY  = new TranslucentColor (Color.darkGray);
   public static final Color TR_GRAY       = new TranslucentColor (Color.gray);
   public static final Color TR_GREEN      = new TranslucentColor (Color.green);
   public static final Color TR_LIGHT_GRAY = new TranslucentColor (Color.lightGray);
   public static final Color TR_MAGENTA    = new TranslucentColor (Color.magenta);
   public static final Color TR_ORANGE     = new TranslucentColor (Color.orange);
   public static final Color TR_PINK       = new TranslucentColor (Color.pink);
   public static final Color TR_RED        = new TranslucentColor (Color.red);
   public static final Color TR_WHITE      = new TranslucentColor (Color.white);
   public static final Color TR_YELLOW     = new TranslucentColor (Color.yellow);

   /**
    * Regular expression pattern for three hexadecimal values ranging
    * from 0 to 255, as used by browsers to store (RGB) colors.  For
    * example, #FFFF00 would be yellow (red = FF/255, green = FF/255,
    * blue = 0)
    */
   public static final Pattern BROWSER_COLOR =
      Pattern.compile ("[#]?([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})",
                       Pattern.CASE_INSENSITIVE);

   /**
    * Regular expression pattern for RGB(A) integer values ranging from 0 to 255.
    * An optional 4th integer supports the alpha value for translucency.
    *
    * "255 255 0" would be solid yellow.
    * "255 0 0 100" would be translucent red.
    */
   private static final String RGBA = "(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])";
   public static final Pattern RGBA_COLOR =
      Pattern.compile (RGBA + " " + RGBA + " " + RGBA + "(?: " + RGBA + ")?");
   
   public static final Pattern JAVA_COLOR = Pattern.compile
      ("[a-zA-Z.]*\\[r=" + RGBA + ",g=" + RGBA + ",b=" + RGBA + "\\]");
                  
   /**
    * Given a color, creates a new color object that is
    * translucent. */

   public TranslucentColor (final Color color)
   {
      super (color.getRed(), color.getGreen(), color.getBlue(), 100);
   }

   /**
    * Given a color, creates a new color object that can be
    * translucent.  The alpha value should be between 0 (for totally
    * translucent) to 255 (for totally opaque).
    */
   public TranslucentColor (final Color color, final int alpha)
   {
      super (color.getRed(), color.getGreen(), color.getBlue(), alpha);
   }

   /**
    * Given a color, creates a new color object that can be
    * translucent.  The alpha value should be between 0.0 (for totally
    * translucent) to 1.0 (for totally opaque).
    */
   public TranslucentColor (final Color color, final float alpha)
   {
      super (color.getRed(), color.getGreen(), color.getBlue(),
             Math.round (alpha * 255));
   }

   /**
    * Given the Red/Green/Blue components, creates a new color object
    * that may be translucent.  The alpha value should be between 0
    * (for totally translucent) and 255 (for totally opaque).
    */
   public TranslucentColor (final int red, final int green, final int blue,
                            final int alpha)
   {
      super (red, green, blue, alpha);
   }

   /**
    * Given the Red/Green/Blue components, creates a new color object
    * that may be translucent.  The alpha value should be between 0.0
    * (for totally translucent) to 1.0 (for totally opaque).
    */
   public TranslucentColor (final int red, final int green, final int blue, 
                            final float alpha)
   {
      super (red, green, blue, Math.round (alpha * 255));
   }

   /**
    * Given an RGB (or RGBA) value, creates a new color object that
    * may be translucent.
    */
   public TranslucentColor (final int rgba)
   {
      super (rgba, true);
   }

   /**
    * Static convenience method to determine a color given a "code".
    * This method will parse the string using regular expressions for
    * some common formats.
    */
   public static TranslucentColor parse (final String code, 
                                         final float alpha)
   {
      // check if the given code is a RGBA string
      Matcher matcher = RGBA_COLOR.matcher (code);
      if (!matcher.matches())
         matcher = JAVA_COLOR.matcher (code);
      if (matcher.matches())
      {
         int r = Integer.parseInt (matcher.group (1));
         int g = Integer.parseInt (matcher.group (2));
         int b = Integer.parseInt (matcher.group (3));
         float codeAlpha = alpha;
         if (matcher.groupCount() == 4)
         {
            String a = matcher.group (4);
            if (a != null)
               codeAlpha = Integer.parseInt (a) / 255f;
         }
         return new TranslucentColor (r, g, b, codeAlpha);
      }
      
      // check if the given name is a browser RGB color
      matcher = BROWSER_COLOR.matcher (code);
      if (matcher.matches())
      {
         int r = Integer.parseInt (matcher.group (1), 16);
         int g = Integer.parseInt (matcher.group (2), 16);
         int b = Integer.parseInt (matcher.group (3), 16);
         return new TranslucentColor (r, g, b, alpha);
      }
      
      // check for simple RGBA value (if so, ignore alpha argument)
      try
      {
         int rgba = Integer.parseInt (code);
         return new TranslucentColor (rgba);
      }
      catch (NumberFormatException x) { }
      
      return null;
   }

   /**
    * Determines if the given color is equivalent, ignoring the
    * translucency value.
    */
   public boolean equalsRGB (final Color color)
   {
      return ((color.getRed()   == getRed()) &&
              (color.getGreen() == getGreen()) &&
              (color.getBlue()  == getBlue()));
   }

   /**
    * Return a browser-compatible string version of the color.  This
    * is in the format #xxxxxx, where x is a hexadecimal character.
    * For example, #FFFF00 would be yellow (red = FF/255, green =
    * FF/255, blue = 0).  Note that the translucency is lost.
    */
   public String toHex()
   {
      return TranslucentColor.toHex (this);
   }

   public static String toHex (final Color c)
   {
      return toHex (c, "#");
   }
   
   public static String toHex (final Color c, final String prefix)
   {
      String s = toHex (c.getRed()) + toHex (c.getGreen()) + toHex (c.getBlue());
      return prefix + s.toUpperCase();
   }

   private static String toHex (final int i)
   {
      if (i < 16)
         return "0" + Integer.toHexString (i);
      return Integer.toHexString (i);
   }

   /** Mimic the Color toString() method, but also show the alpha value. */
   @Override
   public String toString()
   {
      String s = super.toString();
      if (getAlpha() != 255)
         s = s.replace ("]", ",a=" + getAlpha() + "]");
      return s;
   }

   /**
    * Override, since Color's method loses translucency.
    */
   @Override
   public Color brighter()
   {
      return new TranslucentColor (super.brighter(), getAlpha());
   }

   /**
    * Override, since Color's method loses translucency.
    */
   @Override
   public Color darker()
   {
      return new TranslucentColor (super.darker(), getAlpha());
   }

   /**
    * Like brighter(), except you can use a factor other than 0.7.
    */
   public TranslucentColor brighter (final double factor)
   {
      // code copied from java.awt.Color
      int r = getRed();
      int g = getGreen();
      int b = getBlue();

      int i = (int) (1.0 / (1.0 - factor));
      if (r == 0 && g == 0 && b == 0) // black
         return new TranslucentColor (i, i, i, getAlpha());

      if (r > 0 && r < i) r = i;
      if (g > 0 && g < i) g = i;
      if (b > 0 && b < i) b = i;

      r = Math.min ((int) (r / factor), 255);
      g = Math.min ((int) (g / factor), 255);
      b = Math.min ((int) (b / factor), 255);

      return new TranslucentColor (r, g, b, getAlpha());
   }

   /**
    * Like darker(), except you can use a factor other than 0.7.
    */
   public TranslucentColor darker (final double factor)
   {
      // code copied from java.awt.Color
      int r = Math.max ((int) (getRed()   * factor), 0);
      int g = Math.max ((int) (getGreen() * factor), 0);
      int b = Math.max ((int) (getBlue()  * factor), 0);

      return new TranslucentColor (r, g, b, getAlpha());
   }

   private static final int DELTA = 255 / 4;

   /**
    * Java's brighter() method doesn't work for pure colors (where one
    * or more of the RGB values is 0).  This may be used as an
    * alternative. This will work with Color and TranslucentColor objects.
    */
   public static Color brighter (final Color c)
   {
      Color brighter = new Color (Math.min (255, c.getRed()   + DELTA),
                                  Math.min (255, c.getGreen() + DELTA),
                                  Math.min (255, c.getBlue()  + DELTA));
      if (c instanceof TranslucentColor)
         return new TranslucentColor (brighter, c.getAlpha());
      return brighter;
   }

   public static void main (final String[] args)
   {
      Color c = Color.red;
      TranslucentColor t = new TranslucentColor (c);

      System.out.println ("Red (opaque)      = " + c + " RGB = " + c.getRGB());
      System.out.println ("Red (translucent) = " + t + " RGB = " + t.getRGB());
      System.out.println ("> equals()    = " + t.equals (c));
      System.out.println ("> equalsRGB() = " + t.equalsRGB (c));
      System.out.println();

      t = TranslucentColor.parse (Color.RED.toString(), 1f);
      System.out.println (Color.RED.toString() + " = " + t);
      System.out.println ("toHex => " + t.toHex());
      System.out.println();

      t = TranslucentColor.parse ("#FFA000", 1f);
      System.out.println ("#FFA000 = " + t); // 255, 160, 0
      System.out.println ("toHex => " + t.toHex());
      System.out.println();

      t = TranslucentColor.parse ("-65536", .75f); // opaque red
      System.out.println ("-65536 = " + t); // alpha should be 191 (75% of 255)
      System.out.println ("toHex => " + t.toHex());
      System.out.println();

      t = TranslucentColor.parse ("255 0 0", 1f); // opaque red
      System.out.println ("255 0 0 = " + t);
      System.out.println ("toHex => " + t.toHex());
      System.out.println();

      t = TranslucentColor.parse ("255 0 0 100", .75f); // translucent red
      System.out.println ("255 0 0 100 = " + t); // alpha should remain 100
      System.out.println ("toHex => " + t.toHex());
      System.out.println();

      t = TranslucentColor.parse ("1694433280", .75f); // translucent red
      System.out.println ("1694433280 = " + t); // alpha should remain 100
      System.out.println ("toHex => " + t.toHex());
      System.out.println();
   }
}
