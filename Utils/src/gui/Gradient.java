package gui;

import java.awt.Color;

/**
 * There are a number of defined gradient types (look at the static fields), but
 * you can create any gradient you like by using either of the create
 * functions.
 * <ul>
 * <li>public static Color[] createMultiGradient(Color[] colors, int numSteps)</li>
 * <li>public static Color[] createGradient(Color one, Color two, int numSteps)</li>
 * </ul>
 */

public final class Gradient
{
   public static final Color[] BLUE_TO_RED = 
      createGradient (Color.BLUE, Color.RED, 25);
   public static final Color[] BLACK_TO_WHITE = 
      createGradient (Color.BLACK, Color.WHITE, 25);
   public static final Color[] RED_TO_GREEN = 
      createGradient (Color.RED, Color.GREEN, 25);
   public static final Color[] GREEN_YELLOW_ORANGE_RED =
      createMultiGradient (new Color[] {
               Color.green, Color.yellow, Color.orange, Color.red }, 25);
   public static final Color[] RAINBOW = 
      createMultiGradient (new Color[] {
               new Color (181, 32, 255), Color.blue, Color.green, Color.yellow,
               Color.orange, Color.red }, 25);
   public static final Color[] HOT =
      createMultiGradient (new Color[] {
               Color.black, new Color (87, 0, 0), Color.red, Color.orange, 
               Color.yellow, Color.white }, 25);
   public static final Color[] HEAT = 
      createMultiGradient (new Color[] {
               Color.black, new Color (105, 0, 0), new Color (192, 23, 0),
               new Color (255, 150, 38), Color.white }, 25);

   private Gradient()
   {
      // utility class; prevent instantiation
   }
   
   /**
    * Creates an array of Color objects for use as a gradient, using a linear
    * interpolation between the two specified colors.
    * 
    * @param one Color used for the bottom of the gradient
    * @param two Color used for the top of the gradient
    * @param numSteps The number of steps in the gradient. 250 is a good number.
    */
   public static Color[] createGradient (final Color one, final Color two,
                                         final int numSteps)
   {
      int r = one.getRed();
      int g = one.getGreen();
      int b = one.getBlue();
      int a = one.getAlpha();

      int deltaR = two.getRed()   - r;
      int deltaG = two.getGreen() - g;
      int deltaB = two.getBlue()  - b;
      int deltaA = two.getAlpha() - a;

      int newR, newG, newB, newA;

      Color[] gradient = new Color[numSteps];
      for (int i = 0; i < numSteps; i++)
      {
         double iNorm = i / (double) numSteps; // a normalized [0:1] variable
         newR = (int) (r + iNorm * deltaR);
         newG = (int) (g + iNorm * deltaG);
         newB = (int) (b + iNorm * deltaB);
         newA = (int) (a + iNorm * deltaA);
         gradient[i] = new Color (newR, newG, newB, newA);
      }

      return gradient;
   }

   /**
    * Creates an array of Color objects for use as a gradient, using an array of
    * Color objects. It uses a linear interpolation between each pair of points.
    * We assume a linear gradient, with equal spacing between colors.
    * The final gradient will be made up of n 'sections', where n = colors.length - 1
    * 
    * @param colors
    *           An array of Color objects used for the gradient. The Color at
    *           index 0 will be the lowest color.
    * @param numSteps
    *           The number of steps in the gradient. 250 is a good number.
    */
   public static Color[] createMultiGradient (final Color[] colors, 
                                              final int numSteps)
   {
      Color[] gradient = new Color[numSteps];

      Color[] colors100 = new Color[100];
      int index = 0;
      int colorsPerSection = (int) Math.ceil (100.0 / (colors.length - 1));
      for (int section = 0; section < colors.length - 1; section++)
      {
         // create a regular gradient for each section
         Color[] temp = createGradient (colors[section], colors[section + 1], colorsPerSection);
         for (int i = 0; i < temp.length && index < 100; i++) // copy sub-gradient into gradient
            colors100[index++] = temp[i];
      }

      for (int i = 0; i < numSteps; i++)
         gradient[i] = colors100[Math.min (99, i * 100 / (numSteps - 1))];

      return gradient;
   }
}
