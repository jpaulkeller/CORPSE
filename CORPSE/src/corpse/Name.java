package corpse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Name
{
   private static final List<String> VOWELS     = new ArrayList<String>();
   private static final List<String> CONSONANTS = new ArrayList<String>();
   private static final List<String> DIPTHONGS  = new ArrayList<String>();
   private static final List<String> DIGRAPHS   = new ArrayList<String>();
   private static final List<String> ENDING     = new ArrayList<String>();
   private static final List<String> REPEATING  = new ArrayList<String>();
   
   static
   {
      VOWELS.add ("A");
      VOWELS.add ("E");
      VOWELS.add ("I");
      VOWELS.add ("O");
      VOWELS.add ("U");
      VOWELS.add ("Y");
   }
   
   static
   {
      CONSONANTS.add ("B");
      CONSONANTS.add ("C");
      CONSONANTS.add ("D");
      CONSONANTS.add ("F");
      CONSONANTS.add ("G");
      CONSONANTS.add ("H");
      CONSONANTS.add ("J");
      CONSONANTS.add ("K");
      CONSONANTS.add ("L");
      CONSONANTS.add ("M");
      CONSONANTS.add ("N");
      CONSONANTS.add ("P");
      CONSONANTS.add ("R");
      CONSONANTS.add ("S");
      CONSONANTS.add ("T");
      CONSONANTS.add ("V");
      CONSONANTS.add ("W");
      CONSONANTS.add ("X");
      CONSONANTS.add ("Y");
      CONSONANTS.add ("Z");
   }
   
   static
   {
      DIPTHONGS.add ("AI");
      DIPTHONGS.add ("AU");
      DIPTHONGS.add ("EA");
      DIPTHONGS.add ("EE");
      DIPTHONGS.add ("EI");
      DIPTHONGS.add ("OA");
      DIPTHONGS.add ("OI");
      DIPTHONGS.add ("OO");
      DIPTHONGS.add ("OU");
   }
   
   static
   {
      DIGRAPHS.add ("CH");
      DIGRAPHS.add ("GH");
      DIGRAPHS.add ("PH");
      DIGRAPHS.add ("QU");
      DIGRAPHS.add ("SC");
      DIGRAPHS.add ("SH");
      DIGRAPHS.add ("TH");
      DIGRAPHS.add ("WH");
      DIGRAPHS.add ("WR");
   }
   
   static
   {
      REPEATING.add ("FF");
      REPEATING.add ("GG");
      REPEATING.add ("LL");
      REPEATING.add ("MM");
      REPEATING.add ("NN");
      REPEATING.add ("PP");
      REPEATING.add ("RR");
      REPEATING.add ("SS");
      REPEATING.add ("TT");
   }
   
   static
   {
      ENDING.add ("CH");
      ENDING.add ("CK");
      ENDING.add ("LD");
      ENDING.add ("LF");
      ENDING.add ("LK");
      ENDING.add ("LM");
      ENDING.add ("LT");
      ENDING.add ("ND");
      ENDING.add ("NG");
      ENDING.add ("NK");
      ENDING.add ("NT");
      ENDING.add ("RD");
      ENDING.add ("RG");
      ENDING.add ("RK");
      ENDING.add ("RM");
      ENDING.add ("RN");
      ENDING.add ("RT");
      ENDING.add ("SH");
      ENDING.add ("SK");
      ENDING.add ("ST");
      ENDING.add ("TH");
   }
   
   private Name() { }
   
   static String getRandomName()
   {
      List<String> list = null;
      
      String entry = RandomEntry.get ("NAME", null, null);
      StringBuilder pattern = new StringBuilder (entry); 
      StringBuilder name = new StringBuilder(); 
      for (int i = 0; i < pattern.length(); i++)
      {
         char key = pattern.charAt (i);
         switch (key)
         {
         case 'C': list = CONSONANTS; break;
         case 'D': list = DIGRAPHS; break;
         case 'N': list = ENDING; break;
         case 'R': list = REPEATING; break;
         case 'V': list = VOWELS; break;
         case 'W': list = DIPTHONGS; break;
         default:
            if ("'-".indexOf (key) < 0)
               System.err.println ("Invalid NAME pattern: " + key);
         }
         if (list != null)
            name.append (list.get (RandomEntry.get (list.size())));
         else // handle literals (e.g., '-')
            name.append (key);
         list = null;
      }
      return name.toString();
   }
   
   public static void main (final String[] args)
   {
      Table.populate (new File ("data/Tables/People"));
      
      for (int i = 0; i < 10; i++)
         System.out.println (Name.getRandomName());
   }
}
