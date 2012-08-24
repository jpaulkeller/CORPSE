package gui.db;

import java.sql.SQLException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class OracleDateFormat extends Format 
{
   private SimpleDateFormat df;
   
   public OracleDateFormat (final String pattern)
   {
      df = new SimpleDateFormat (pattern);
      df.setLenient (false);
      df.setTimeZone (TimeZone.getTimeZone ("Etc/GMT0")); // Zulu
   }
   
   @Override
   public StringBuffer format (final Object obj,
                               final StringBuffer toAppendTo,
                               final FieldPosition pos)   
   {
      try
      {
         oracle.sql.TIMESTAMP time = (oracle.sql.TIMESTAMP) obj;
         java.util.Date date = time.dateValue();
         toAppendTo.append (df.format (date));
      }
      catch (SQLException x)
      {
         x.printStackTrace();
      }
      
      return toAppendTo;
   }

   @Override
   public Object parseObject (String source, ParsePosition pos)
   {
      java.util.Date date = (java.util.Date) df.parseObject (source, pos);
      java.sql.Date sqlDate = new java.sql.Date (date.getTime());
      return new oracle.sql.TIMESTAMP (sqlDate);
   }
}
