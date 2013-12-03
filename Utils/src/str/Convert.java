package str;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The Convert class provides convenient static methods to convert
 * strings to integers, etc.  */

public class Convert
{
   // regular expressions
   public static final String
      COMPASS_DEGREES = "[-]?([0-9]|[0-9][0-9]|[0-2][0-9][0-9]|3[0-5][0-9])";
   public static final String
      COMPASS_HEADING = "[NSEW]|[NS][EW]|NN[EW]|SS[EW]|E[NS]E|W[NS]W|NORTH|SOUTH|EAST|WEST|NO|SO|EA|WE";

   private static final Pattern IntPattern = Pattern.compile ("(-?\\d+)");

   public static int toInt (String s)
   {
      return toInt (s, -1);
   }

   public static int toInt (String s, int defaultValue)
   {
      int value = defaultValue;

      if ((s != null) && !s.trim().equals (""))
      {
         try
         {
            value = (int) Math.round (toDbl (s, defaultValue));
         }
         catch (NumberFormatException nfe)
         {
            System.out.println ("NumberFormatException: toInt (" + s + ")");
         }
      }

      return value;
   }

   /**
    * Convert a string contain multiple integers into an array of int
    * values (using regular expression matching).
    */
   public static List<Integer> toInts (String integers)
   {
      List<Integer> list = new ArrayList<Integer>();

      if (integers != null)
      {
         Matcher matcher = IntPattern.matcher (integers);
         while (matcher.find())
            list.add (toInt (matcher.group (1)));
      }

      return list;
   }

   /**
    * static method returns a String to represent the int constant
    * passed as a parameter.
    *
    * @return String representation of constant.
    */
   public static String toString (int i)
   {
      String strVal = null;
      try
      {
         String s = Integer.toString (i);
         if (s != null && s.length() > 0)
            strVal = s;
      }
      catch (NumberFormatException nfe)
      {
         System.out.println ("NumberFormatException: toString (" + i + ")");
      }
      return strVal;
   }

   public static double toDbl (String s)
   {
      return toDbl (s, -1.0);
   }

   public static double toDbl (String s, double defaultValue)
   {
      double value = defaultValue;

      if ((s != null) && !s.trim().equals (""))
      {
         try
         {
            value = Double.parseDouble (s.trim());
         }
         catch (NumberFormatException nfe)
         {
            System.out.println ("NumberFormatException: toDbl (" + s + ")");
         }
      }

      return value;
   }

   /**
    * static method returns a String to represent the double constant
    * passed as a parameter.
    *
    * @return String representation of constant.
    */
   public static String toString (double d)
   {
      String strVal = null;
      try
      {
         String s = Double.toString (d);
         if (s != null && s.length() > 0)
            strVal = s;
      }
      catch (NumberFormatException nfe)
      {
         System.out.println ("NumberFormatException: toString (" + d + ")");
      }
      return strVal;
   }

   /**
    * static method returns a String to represent the Double constant
    * passed as a parameter.
    *
    * @return String representation of constant.
    */
   public static String toString (Double d)
   {
      if (d != null)
         return toString (d.doubleValue());
      return null;
   }

   /**
    * static method returns an RGB int from a Color
    *
    * @param  color  Color
    * @return intVal int - RGB int value of Color
    */
   public static int toRGB (Color color)
   {
      int intVal = 0;

      if (color != null)
         intVal = color.getRGB();

      return intVal;
   }

   /**
    * static method returns an RGB int from a Color.  If the
    * color parameter is null, use the default color
    *
    * @return RGB int value of Color
    */
   public static int toRGB (Color color, Color defaultColor)
   {
      int intVal = 0;

      if (color != null)
         intVal = color.getRGB();
      else if (defaultColor != null)
         intVal = defaultColor.getRGB();

      return intVal;
   }

   /**
    * static method returns a Color from an RGB int
    *
    * @param rgb RGB int value of Color
    * @return Color
    */
   public static Color toColor (int rgb)
   {
      Color color = null;

      if (rgb != 0)
         color = new Color (rgb);

      return color;
   }

   /**
    * static method returns a Color from an RGB int
    * if the RGB int value is 0, use the default
    *
    * @param  rgb RGB int value of Color
    * @return Color
    */
   public static Color toColor (int rgb, Color defaultColor)
   {
      Color color = defaultColor;

      if (rgb != 0)
         color = new Color (rgb);

      return color;
   }

   public static int toDegrees (String heading, int defaultDegrees)
   {
      int compassDegrees = defaultDegrees;

      if (heading != null)
      {
         heading = heading.trim().toUpperCase();
         if (RegExp.matches (COMPASS_DEGREES, heading))
         {
            compassDegrees = Integer.parseInt (heading);
            if (compassDegrees < 0)
               compassDegrees += 360;
         }
         else if (RegExp.matches (COMPASS_HEADING, heading))
         {
            if      (heading.equals ("N"))     compassDegrees =   0;
            else if (heading.equals ("NO"))    compassDegrees =   0;
            else if (heading.equals ("NORTH")) compassDegrees =   0;
            else if (heading.equals ("NNE"))   compassDegrees =  22;
            else if (heading.equals ("NE"))    compassDegrees =  45;
            else if (heading.equals ("ENE"))   compassDegrees =  67;
            else if (heading.equals ("E"))     compassDegrees =  90;
            else if (heading.equals ("EA"))    compassDegrees =  90;
            else if (heading.equals ("EAST"))  compassDegrees =  90;
            else if (heading.equals ("ESE"))   compassDegrees = 112;
            else if (heading.equals ("SE"))    compassDegrees = 135;
            else if (heading.equals ("SSE"))   compassDegrees = 157;
            else if (heading.equals ("S"))     compassDegrees = 180;
            else if (heading.equals ("SO"))    compassDegrees = 180;
            else if (heading.equals ("SOUTH")) compassDegrees = 180;
            else if (heading.equals ("SSW"))   compassDegrees = 202;
            else if (heading.equals ("SW"))    compassDegrees = 225;
            else if (heading.equals ("WSW"))   compassDegrees = 247;
            else if (heading.equals ("W"))     compassDegrees = 270;
            else if (heading.equals ("WE"))    compassDegrees = 270;
            else if (heading.equals ("WEST"))  compassDegrees = 270;
            else if (heading.equals ("WNW"))   compassDegrees = 292;
            else if (heading.equals ("NW"))    compassDegrees = 315;
            else if (heading.equals ("NNW"))   compassDegrees = 337;
         }
         else if (heading.length() > 0)
            System.out.println ("Invalid heading: [" + heading + "]");
      }
      return compassDegrees;
   }

   public static void main (String[] args)
   {
      System.out.println ("Convert.toString (12): " + toString (12));
      String s = "012.7";
      System.out.println ("Convert.toDbl (" + s + "): " + toDbl (s));
      System.out.println ("Convert.toInt (" + s + "): " + toInt (s));
      s = "abc";
      System.out.println ("Convert.toInt (" + s + "): " + toInt (s));
      System.out.println ("Convert.toInt (" + s + ", 0): " + toInt (s, 0));

      s = "1 2 x 3y";
      System.out.print ("Convert.toInts (" + s + "): ");
      for (int i : toInts (s))
         System.out.print (i + ", ");
      System.out.println();
   }
}
