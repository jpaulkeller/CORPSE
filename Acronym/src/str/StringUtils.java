package str;

import java.util.Comparator;

public final class StringUtils
{
   private StringUtils()
   {
      // utility class; prevent instantiation
   }
   
   public static String pad (final String s, final int width)
   {
      return pad (s, width, ' '); 
   }
   
   public static String pad (final String s, final int width, final char ch)
   {
      StringBuilder padded = new StringBuilder();
      if (width > 0)
      {
         padded.append (s);
         for (int i = s.length(); i < width; i++)
            padded.append (ch);
      }
      else
      {
         for (int i = s.length(); i < Math.abs (width); i++)
            padded.append (ch);
         padded.append (s);
      }
      return padded.toString();
   }
   
   public static String getCommonPrefix (final String s1, final String s2)
   {
      String prefix = "";
      int len = Math.min (s1.length(), s2.length());
      int pos = 0;
      while (pos < len && s1.charAt (pos) == s2.charAt (pos))
         pos++;
      if (pos > 0)
         prefix = s1.substring (0, pos);

      return prefix;
   }
   
   public static Comparator<String> getCaseInsensitiveComparator()
   {
      return new Comparator<String>()
      {
         public int compare (final String s1, final String s2)
         {
            return s1.toUpperCase().compareTo (s2.toUpperCase());
         }
      };
   }
   
   public static String capitalizeWords (final String s)
   {
      StringBuilder cap = new StringBuilder(); 
      for (int i = 0; i < s.length(); i++)
      {
         char ch = s.charAt (i);
         if (i == 0)
            cap.append (Character.toUpperCase (ch));
         else if (Character.isLetter (ch) &&
                  !Character.isLetter (s.charAt (i - 1)))
            cap.append (Character.toUpperCase (ch));
         else
            cap.append (ch);
      }
      return cap.toString();
   }
}
