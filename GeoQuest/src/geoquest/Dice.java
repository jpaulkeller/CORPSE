package geoquest;

import java.text.DecimalFormat;

public enum Dice
{
   FIND (0, ""),
   DNF (0, ""),
   
   B0 (0, "blue"),
   B1 (1, "blue"),
   B2 (2, "blue"),
   B3 (3, "blue"),
   B4 (4, "blue"),
   
   Y0 (0, "yellow"),
   Y1 (1, "yellow"),
   Y2 (2, "yellow"),
   Y3 (3, "yellow"),
   Y4 (4, "yellow");
   
   public static final DecimalFormat DF = new DecimalFormat ("#.#");
   
   private final int pips;
   private final String color; // play or keep
   
   private Dice (final int pips, final String color)
   {
      this.pips = pips;
      this.color = color;
   }
   
   public int pips()     { return pips; }
   public String color() { return color; }
   
   public static void main (final String[] args)
   {
      int total = 0;
      int stream = 0; // count of rolling 3 or less (for stream crossing)
      int[] move = new int[8]; // chance or rolling exactly N
      int[] find = new int[5]; // count of rolling N or more, but not DNF
      int cache = 0; // count of rolling a new cache event (requires double DNFs)
      int doubles = 0; // count of rolling doubles
      int dbl1 = 0, dbl2 = 0, dbl3 = 0; // count of rolling particular doubles
      int play = 0; // count of non-matching doubles (a "play-now" event trigger)
      int keep = 0; // count of matching doubles (a "keep" event trigger)
      int yellowOne = 0;  // count of rolls with either die yellow
      int blueOne = 0;    // count of rolls with either die blue
      int yellowBoth = 0; // count of rolls with both dice yellow
      int blueBoth = 0;   // count of rolls with both dice blue
      int unique = 0; // count of rolls where neither colors nor pips match

      // CURRENT
      // Avg: 3.2, Stream: 61%, Find%: 83,83,72,50,36, Cache: 3%, Doubles: 19% 57/43 play/keep
      
      Dice[] d1 = new Dice[] { FIND, B1, B2, Y3, Y2, Y0 };
      Dice[] d2 = new Dice[] { DNF,  B1, B2, Y1, Y2, B4 };
      // Avg: 3, Stream: 61%, Find%: 83,78,67,50,31, Cache: 3%, Doubles: 19% 57/43 play/keep
      
      for (Dice s1 : d1)
      {
         for (Dice s2 : d2)
         {
            int roll = s1.pips + s2.pips;
            total += roll;
            move[roll]++;
            if (roll <= 3) stream++;

            if (s1.color.equals ("yellow") || s2.color.equals ("yellow")) yellowOne++; 
            else if (s1.color.equals ("blue") && s2.color.equals ("blue")) blueBoth++; 
            if (s1.color.equals ("blue") || s2.color.equals ("blue"))  blueOne++; 
            else if (s1.color.equals ("yellow") && s2.color.equals ("yellow")) yellowBoth++; 

            if (!s1.color.equals (s2.color) && s1.pips != s2.pips)
               unique++;
            
            for (int toFind = 1; toFind <= 5; toFind++)
               if ((s1 == FIND || roll >= toFind) && s2 != DNF)
                  find[toFind - 1]++;

            if (s1 == FIND && s2 == DNF) // new cache event
               cache++;
            else if (s1.pips == s2.pips)
            {
               doubles++;
               if      (s1.pips == 1) dbl1++;
               else if (s1.pips == 2) dbl2++;
               else if (s1.pips == 3) dbl3++;
               if (s1.color.equals (s2.color)) keep++; else play++;
            }
         }
      }

      System.out.println ("TARGET Avg: 3.0, Stream: 60%, Find%: 90,75,60,45,25, Cache: 3%, Doubles: 20% 60/40 play/keep");

      System.out.print ("ACTUAL Avg: " + DF.format (total / 36f)); 
      System.out.print (", Stream: " + Math.round (stream * 100f / 36) + "%"); // streams 
      System.out.print (", Find%: ");
      for (int toFind = 1; toFind <= 5; toFind++)
         System.out.print (Math.round (find[toFind - 1] * 100f / 36) + ","); 
      System.out.print (" Cache: " + Math.round (cache * 100f / 36) + "%"); 
      System.out.print (", Doubles: " + Math.round (doubles * 100f / 36) + "% "); 
      System.out.print (Math.round (play * 100f / doubles) + "/"); 
      System.out.print (Math.round (keep * 100f / doubles) + " play/keep"); 
      System.out.println();
      
      System.out.print ("Chance to roll: ");
      System.out.print ("1/1: " + Math.round (dbl1 * 100f / 36) + "%"); 
      System.out.print (", 2/2: " + Math.round (dbl2 * 100f / 36) + "%"); 
      System.out.print (", 3/3: " + Math.round (dbl3 * 100f / 36) + "%"); 
      System.out.print (", Blue/*: " + Math.round (blueOne * 100f / 36) + "%"); 
      System.out.print (", Yellow/*: " + Math.round (yellowOne * 100f / 36) + "%");
      System.out.print (", Blue/Blue: " + Math.round (blueBoth * 100f / 36) + "%"); 
      System.out.print (", Yellow/Yellow: " + Math.round (yellowBoth * 100f / 36) + "%");
      System.out.print (", Unique: " + Math.round (unique * 100f / 36) + "%");
      System.out.println();
      
      System.out.print ("Chance to roll (total): ");
      for (int i = 0; i < 8; i++)
         System.out.print (i + "=" + Math.round (move[i] * 100f / 36) + "%, ");
      System.out.println();
      
      System.out.print ("Chance to roll (# or less): ");
      double cumulative = 0;
      for (int i = 0; i < 8; i++)
      {
         cumulative += move[i] * 100f / 36;
         System.out.print (i + "=" + Math.round (cumulative) + "%, ");
      }
      System.out.println();
      
      System.out.print ("Chance to roll (# or more): ");
      cumulative = 0;
      for (int i = 7; i >= 0; i--)
      {
         double percent = move[i] * 100f / 36;
         cumulative += percent;
         System.out.print (i + "=" + Math.round (cumulative) + "%, ");
      }
      System.out.println();
   }
}
