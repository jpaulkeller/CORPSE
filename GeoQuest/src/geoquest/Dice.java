package geoquest;

import java.text.DecimalFormat;

public class Dice
{
   private static final String BLACK = "black";
   private static final String WHITE = "white";
   private static final DecimalFormat DF = new DecimalFormat ("#.#");
   
   enum Sides 
   {
      FIND (0, ""),
      DNF (0, ""),
      
      B0 (0, BLACK),
      B1 (1, BLACK),
      B2 (2, BLACK),
      B3 (3, BLACK),
      B4 (4, BLACK),
      
      W0 (0, WHITE),
      W1 (1, WHITE),
      W2 (2, WHITE),
      W3 (3, WHITE),
      W4 (4, WHITE);
   
      private final int pips;
      private final String color;
      
      private Sides (final int pips, final String color)
      {
         this.pips = pips;
         this.color = color;
      }
      
      public int pips()     { return pips; }
      public String color() { return color; }
   };
      
   public static void main (final String[] args)
   {
      int total = 0;
      int stream = 0; // count of rolling 3 or less (for stream crossing)
      int[] move = new int[8]; // chance or rolling exactly N
      int[] find = new int[5]; // count of rolling N or more, but not DNF
      int newCache = 0; // count of rolling a new cache event (requires double DNFs)
      int doubles = 0; // count of rolling doubles
      int dbl1 = 0, dbl2 = 0; // count of rolling particular doubles
      int maternal = 0; // count of non-matching doubles
      int paternal = 0; // count of matching doubles
      int whiteOne = 0;  // count of rolls with either die white
      int blackOne = 0;    // count of rolls with either die black
      int whiteBoth = 0; // count of rolls with both dice white
      int blackBoth = 0;   // count of rolls with both dice black
      int sameColor = 0;   // count of rolls with both dice the same color
      int unique = 0; // count of rolls where neither colors nor pips match

      /*
      Sides[] d1 = new Sides[] { Sides.FIND, Sides.B1, Sides.B2, Sides.W3, Sides.W2, Sides.W0 };
      Sides[] d2 = new Sides[] { Sides.DNF,  Sides.B1, Sides.B2, Sides.W1, Sides.W2, Sides.B4 };
      TARGET Avg: 3, Stream: 60%, Find%: 90,75,60,45,25, New Cache: 3%, Doubles: 20%
      ACTUAL Avg: 3, Stream: 58%, Find%: 83,78,67,50,31, New Cache: 3%, Doubles: 19% 43/57 same/diff
      Chance to roll: 1/1: 6%, 2/2: 11%, blk/*: 67%, wht/*: 67%, blk/blk: 17%, wht/wht: 17%, same color: 36%, Unique: 53%
      Chance to roll (total): 0=6%, 1=14%, 2=22%, 3=19%, 4=22%, 5=8%, 6=6%, 7=3%, 
      Chance to roll (# or less): 0=6%, 1=19%, 2=42%, 3=61%, 4=83%, 5=92%, 6=97%, 7=100%, 
      Chance to roll (# or more): 7=3%, 6=8%, 5=17%, 4=39%, 3=58%, 2=81%, 1=94%, 0=100%,
      
      swap DNF/FIND:
      ACTUAL Avg: 3, Stream: 58%, Find%: 94,81,58,39,17, New Cache: 0%, Doubles: 17% 50/50 same/diff
      */
      
      Sides[] d1 = new Sides[] { Sides.FIND,  Sides.B1, Sides.B2, Sides.W0, Sides.W2, Sides.W3 }; // green die
      Sides[] d2 = new Sides[] { Sides.DNF,  Sides.B1, Sides.B2, Sides.B4, Sides.W1, Sides.W2 }; // red die
      
      for (Sides s1 : d1)
      {
         for (Sides s2 : d2)
         {
            int roll = s1.pips + s2.pips;
            total += roll;
            move[roll]++;
            if (roll >= 3) stream++; // crossing streams costs 3 move points

            if (s1.color.equals (WHITE) || s2.color.equals (WHITE)) whiteOne++; 
            else if (s1.color.equals (BLACK) && s2.color.equals (BLACK)) blackBoth++; 
            if (s1.color.equals (BLACK) || s2.color.equals (BLACK)) blackOne++; 
            else if (s1.color.equals (WHITE) && s2.color.equals (WHITE)) whiteBoth++;
            if (s1.color.equals (s2.color)) sameColor++; 

            if (!s1.color.equals (s2.color) && s1.pips != s2.pips)
               unique++;
            
            for (int toFind = 1; toFind <= 5; toFind++)
               if ((s1 == Sides.FIND || roll >= toFind) && s2 != Sides.DNF)
                  find[toFind - 1]++;

            if (s1 == Sides.FIND && s2 == Sides.DNF) // new cache event
               newCache++;
            else if (s1.pips == s2.pips && s1.pips > 0)
            {
               doubles++;
               if      (s1.pips == 1) dbl1++;
               else if (s1.pips == 2) dbl2++;
               if (s1.color.equals (s2.color)) paternal++; else maternal++;
            }
         }
      }

      System.out.println ("TARGET Avg: 3.0, Stream: 60%, Find%: 90,75,60,45,25, New Cache: 3%, Doubles: 20%");

      System.out.print ("ACTUAL Avg: " + DF.format (total / 36f)); 
      System.out.print (", Stream: " + Math.round (stream * 100f / 36) + "%"); // streams 
      System.out.print (", Find%: ");
      for (int toFind = 1; toFind <= 5; toFind++)
         System.out.print (Math.round (find[toFind - 1] * 100f / 36) + ","); 
      System.out.print (" New Cache: " + Math.round (newCache * 100f / 36) + "%"); 
      System.out.print (", Doubles: " + Math.round (doubles * 100f / 36) + "% "); 
      System.out.print (Math.round (paternal * 100f / doubles) + "/"); 
      System.out.print (Math.round (maternal * 100f / doubles) + " same/diff"); 
      System.out.println();
      
      System.out.print ("Chance to roll: ");
      System.out.print ("1/1: " + Math.round (dbl1 * 100f / 36) + "%"); 
      System.out.print (", 2/2: " + Math.round (dbl2 * 100f / 36) + "%"); 
      System.out.print (", blk/*: " + Math.round (blackOne * 100f / 36) + "%"); 
      System.out.print (", wht/*: " + Math.round (whiteOne * 100f / 36) + "%");
      System.out.print (", blk/blk: " + Math.round (blackBoth * 100f / 36) + "%"); 
      System.out.print (", wht/wht: " + Math.round (whiteBoth * 100f / 36) + "%");
      System.out.print (", same color: " + Math.round (sameColor * 100f / 36) + "%");
      System.out.print (", unique: " + Math.round (unique * 100f / 36) + "%");
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
