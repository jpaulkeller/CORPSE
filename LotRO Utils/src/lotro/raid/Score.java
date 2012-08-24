package lotro.raid;

public final class Score
{
   private Score() { }
   
   public static String getColor (final int score)
   {
      if (score >= 10)
         return "#dbeffe"; // blue
      else if (score == 9)
         return "#ddffee"; // blue-green
      else if (score == 8)
         return "#ddffdd"; // green
      else if (score == 7)
         return "#eeffdd"; // yellow-green
      else if (score == 6)
         return "#ffffdd"; // yellow
      else if (score == 5)
         return "#fff2dd"; // yellow-orange
      else if (score == 4)
         return "#ffeedd"; // orange
      else if (score >= 2)
         return "#ffdddd"; // pink

      return "";
   }
}
