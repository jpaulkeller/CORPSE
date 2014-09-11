package corpse;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import file.FileUtils;

public class ExtractData
{
   private final static Pattern HTML_TABLE_ROW = 
      Pattern.compile("<tr[^>]*>(.*?)</tr>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
   private final static Pattern HTML_TABLE_FIELD = 
      Pattern.compile("<td[^>]*>(.*?)</td>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
   
   public static void processRows (final File file, final String... regexes)
   {
      String text = FileUtils.getText(file);
      Matcher m = HTML_TABLE_ROW.matcher(text);
      while (m.find())
         processFields(m.group(1), regexes);  // contents of <tr>...</tr>
   }
   
   public static void processFields (final String row, final String... regexes)
   {
      int fld = 0;
      Matcher m = HTML_TABLE_FIELD.matcher(row);
      while (m.find()) // more fields
      {
         String tableData = m.group(1); // contents of <td>...</td>
         
         String regex = regexes[fld++];
         if (regex != null)
         {
            Pattern pattern = Pattern.compile (regex, Pattern.UNICODE_CASE);
            Matcher fieldMatcher = pattern.matcher (tableData);
            if (!fieldMatcher.find())
               System.out.print("* " + clean(fieldMatcher.group(1)) + "; "); // indicate match not found
            else // support multiple matches in one <td>
            {
               fieldMatcher.reset();
               while (fieldMatcher.find())
                  System.out.print(clean(fieldMatcher.group(1)) + "; ");
            }
         }
         else
            System.out.print(clean(tableData) + "; ");
      }
      System.out.println();
   }
   
   private static String clean(String s)
   {
      s = s.trim();
      s = s.replace("&#39;", "'");
      s = s.replace("&amp;", "&");
      s = s.replace("&mdash;", "-");
      s = s.replace("<br/>", "");
      return s;
   }
   
   public static void main(String[] args)
   {
      File file = new File("C:/Users/J/Desktop/Document1.txt");
      String href  = "<a href=\"[^>]+>([^<]+)</a>";
      String yesNo  = "<img src=\"[^-]+-([^.]+)[^>]*>";
      String simple = "([^<]+)";
      
      ExtractData.processRows(file, href, null, null, null, href, href);
   }
}
