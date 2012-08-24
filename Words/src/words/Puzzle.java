package words;
import java.text.DateFormat;
import java.util.Date;

public class Puzzle
{
   public static void main (String[] args)
   {
      DateFormat df = DateFormat.getDateInstance();
      try
      {
         Date date = df.parse ("Feb 16, 16431"); // 21616431 
         // December 20, 5848                       12205848
         long seconds = date.getTime();
         System.out.println ("Date: " + date);
         System.out.println ("Seconds: " + seconds);
         long minutes = seconds / 60;
         System.out.println ("Minutes: " + minutes);
         long hours = minutes / 60;
         System.out.println ("Hours: " + hours);
         long days = hours / 24;
         System.out.println ("Days: " + days);
      }
      catch (Exception x)
      {
         System.out.println (x);
      }
   }
}

/*
      for (int f = 0; f < 100; f++)
         for (int i = 0; i < 12; i++)
         {
            int total1 = (f * 12) + i; // ordered
            int total2 = (i * 12) + f; // received
            if (total1 * 0.3 == total2)
            {
               System.out.println ("Ordered: " + total1 + ", Recieved: " + total2);
               System.out.println (f + " feet " + i + " inches");
               System.out.println (i + " feet " + f + " inches");
            }               
         }
         
BONUS 
CONTEST ENTRY - choose BONUS and copy and paste the following code, 
replacing the livehuntboxcode/treasurecode value:  
*/
