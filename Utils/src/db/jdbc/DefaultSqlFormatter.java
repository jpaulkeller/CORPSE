package db.jdbc;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class DefaultSqlFormatter extends SqlFormatter
{
   private static final String YMD24 = "'YYYY-MM-DD HH24:MI:SS.#'";

   private String format(final Calendar cal)
   {
      return "TO_DATE('" + new java.sql.Timestamp(cal.getTime().getTime()) + "'," + YMD24 + ")";
   }

   private String format(final java.sql.Date date)
   {
      return "TO_DATE('" + new java.sql.Timestamp(date.getTime()) + "'," + YMD24 + ")";
   }

   private String format(final java.sql.Time time)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new java.util.Date(time.getTime()));
      return "TO_DATE('" + cal.get(Calendar.HOUR_OF_DAY) + ":" + 
      cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + "." + 
      cal.get(Calendar.MILLISECOND) + "','HH24:MI:SS.#')";
   }

   private String format(final java.sql.Timestamp timestamp)
   {
      return "TO_DATE('" + timestamp.toString() + "','YYYY-MM-DD HH24:MI:SS.#')";
   }

   @Override
   public String format(final Object o) throws SQLException
   {
      if (o == null)
         return "NULL";
      if (o instanceof Calendar)
         return format((Calendar) o);
      if (o instanceof Date)
         return format((Date) o);
      if (o instanceof Time)
         return format((Time) o);
      if (o instanceof Timestamp)
         return format((Timestamp) o);
      // if object not in one of our overridden methods, send to super class
      return super.format(o);
   }
}