package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import str.StringUtils;

public final class DateUtils
{
   // ignoring leap-seconds
   private static final long MILLISECS_PER_MINUTE = 60 * 1000;
   private static final long MILLISECS_PER_HOUR = 60 * MILLISECS_PER_MINUTE;
   private static final long MILLISECS_PER_DAY = 24 * MILLISECS_PER_HOUR;
   
   private static final String MM   = "(?:0[1-9]|1[0-2])"; // month (01-12)
   private static final String M    = "(?:0?[1-9]|1[0-2])"; // month (1-12)
   private static final String MMM  = "(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)";
   private static final String dd   = "(?:0[1-9]|[12][0-9]|3[01])"; // day (01-31)
   private static final String d    = "(?:0?[1-9]|[12][0-9]|3[01])"; // day (1-31)
   private static final String EEE  = "(?:SUN|MON|TUE|WED|THU|FRI|SAT)"; // day of week
   private static final String yy   = "(?:[0-9][0-9])"; // 2-digit year (00-99)
   private static final String yyyy = "(?:(?:19|20)" + yy + ")"; // 4 digit year
   private static final String HH   = "(?:0[0-9]|1[0-9]|2[0-3])"; //hour in day (00-23)
   private static final String mm   = "(?:[0-5][0-9])"; // minute in hour (00-59)
   private static final String ss   = "(?:[0-5][0-9])"; // second in minute (00-59)
   private static final String SSS  = "(?:[0-5][0-9][.][0-9]{1,3})"; // decimal secs
   private static final String zzz  = "(?:[A-Z][A-Z][A-Z])"; // time zone e.g. EST

   // common DBMS patterns
   private static final DT DBMS_DATE_1 =
      new DT ("yyyy-MM-dd HH:mm:ss.SSS",
              yyyy + "-" + MM + "-" + dd + " " + HH + ":" + mm + ":" + SSS);
   private static final DT DBMS_DATE_2 =
      new DT ("yyyy-MM-dd HH:mm:ss",
              yyyy + "-" + MM + "-" + dd + " " + HH + ":" + mm + ":" + ss);
   private static final DT DBMS_DATE_3 =
      new DT ("yyyy/MM/dd HH:mm:ss.SSS",
              yyyy + "/" + MM + "/" + dd + " " + HH + ":" + mm + ":" + SSS);
   private static final DT DBMS_DATE_4 =
      new DT ("yyyy/MM/dd HH:mm:ss",
              yyyy + "/" + MM + "/" + dd + " " + HH + ":" + mm + ":" + ss);

   // other patterns
   private static final DT dd_MMM_yyyy_HH_mm_ss =
      new DT ("dd MMM yyyy HH:mm:ss",
              dd + " " + MMM + " " + yyyy + " " + HH + ":" + mm + ":" + ss);
   private static final DT ddMMMyyyy = new DT ("ddMMMyyyy", dd + MMM + yyyy);
   private static final DT ddHHmm_MMM_yy =
      new DT ("ddHHmm MMM yy", dd + HH + mm + " " + MMM + " " + yy);
   private static final DT MM_dd_yyyy_HH_mm_ss =
      new DT ("MM/dd/yyyy HH:mm:ss",
              MM + "/" + dd + "/" + yyyy + " " + HH + ":" + mm + ":" + ss);
   private static final DT MM_dd_yyyy =
      new DT ("MM/dd/yyyy", M + "/" + d + "/" + yyyy);
   private static final DT MM_dd_yy_HH_mm_ss =
      new DT ("MM/dd/yy HH:mm:ss",
              MM + "/" + dd + "/" + yy + " " + HH + ":" + mm + ":" + ss);
   private static final DT MM_dd_yy =
      new DT ("MM/dd/yy", M + "/" + d + "/" + yy);
   private static final DT dd_MMM_yyyy =
      new DT ("dd MMM yyyy", d + " " + MMM + " " + yyyy);

   // standard Java Date.toString() pattern (Fri Apr 11 16:49:45 EDT 2003)
   private static final DT EEE_MMM_dd_HH_mm_ss_zzz_yyyy =
      new DT ("EEE MMM dd HH:mm:ss zzz yyyy",
              EEE + " " + MMM + " " + dd + " " +
              HH + ":" + mm + ":" + ss + " " + zzz + " " + yyyy);

   private static final DT yyyyMMddHHmm =
      new DT ("yyyyMMddHHmm", yyyy + MM + dd + HH + mm);
   private static final DT yyyyMMdd = new DT ("yyyyMMdd", yyyy + MM + dd);
   private static final DT yyMMdd = new DT ("yyMMdd", yy + MM + dd);
   private static final DT MMM_dd_yyyy_HH_mm_ss =
      new DT ("MMM d, yyyy HH:mm:ss",
              MMM + " " + d + ", " + yyyy + " " + HH + ":" + mm + ":" + ss);

   private static final Collection<DT> PATTERNS = loadPatterns(); // ordered DT objects

   private DateUtils() { /* prevent instantiation */ }
   
   private static Collection<DT> loadPatterns()
   {
      // The formats are stored in a List to ensure that they are
      // iterated over in order.  This is critical in identifying the
      // right format.
      Collection<DT> patternList = new ArrayList<DT>();

      // add the typical DBMS formats
      patternList.add (DBMS_DATE_1);
      patternList.add (DBMS_DATE_2);
      patternList.add (DBMS_DATE_3);
      patternList.add (DBMS_DATE_4);

      // add other formats
      patternList.add (dd_MMM_yyyy_HH_mm_ss);
      patternList.add (ddMMMyyyy);
      patternList.add (ddHHmm_MMM_yy);
      patternList.add (MM_dd_yyyy_HH_mm_ss);
      patternList.add (MM_dd_yy_HH_mm_ss);
      patternList.add (EEE_MMM_dd_HH_mm_ss_zzz_yyyy);
      patternList.add (MM_dd_yyyy);
      patternList.add (yyyyMMddHHmm);
      patternList.add (yyyyMMdd);
      patternList.add (yyMMdd);
      patternList.add (MM_dd_yy);
      patternList.add (dd_MMM_yyyy);
      patternList.add (MMM_dd_yyyy_HH_mm_ss);
      
      return patternList;
   }

   // parses the given date string using the first matching pattern
   public static Date parse (final String fromDate)
   {
      return parse (fromDate, "Etc/GMT0"); // assume GMT (zulu)
   }
   
   // parses the given date string using the first matching pattern
   public static Date parse (final String fromDate, final String offsetGMT)
   {
      if (fromDate != null && !fromDate.equals ("null"))
      {
         for (DT dt : PATTERNS)
         {
            Matcher matcher = dt.pattern.matcher (fromDate.toUpperCase());
            if (matcher.matches())
               return parse (matcher.group (1), dt, offsetGMT);
         }
      }
      return null;
   }

   private static Date parse (final String dateString, final DT dt, final String offsetGMT)
   {
      Date date = null;
      
      try
      {
         SimpleDateFormat sdf = new SimpleDateFormat (dt.format);
         sdf.setLenient (false);
         if (offsetGMT != null)
            sdf.setTimeZone (TimeZone.getTimeZone (offsetGMT));
         date = sdf.parse (dateString.toUpperCase());
      }
      catch (ParseException x)
      {
         x.printStackTrace();
      }

      return date;
   }

   /**
    * Value to add to the day number returned by this calendar to find the
    * Julian Day number. This is the Julian Day number for 1/1/1970. Note: Since
    * the Unix Day number is the same from local midnight to local midnight
    * adding JULIAN_DAY_OFFSET to that value results in the chronologist's
    * Julian Day number.
    * 
    * @see http://www.hermetic.ch/cal_stud/jdn.htm
    */
   public static final long EPOCH_UNIX_ERA_DAY = 2440588L;

   /**
    * @return Day number where day 0 is 1/1/1970, as per the Unix/Java date/time
    *         epoch.
    */
   public static long getUnixDay (final Calendar cal)
   {
      long offset = cal.get (Calendar.ZONE_OFFSET) + cal.get (Calendar.DST_OFFSET);
      long day = (long) Math.floor ((double) (cal.getTime().getTime() + offset) /
                                    (double) MILLISECS_PER_DAY);
      return day;
   }

   public static long getUnixDay (final Date date)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime (date);
      cal.set (Calendar.HOUR_OF_DAY, 0);
      cal.set (Calendar.MINUTE, 0);
      cal.set (Calendar.SECOND, 0);
      cal.set (Calendar.MILLISECOND, 0);
      return getUnixDay (cal);
   }
   
   /**
    * @return LOCAL Chronologists Julian day number each day starting from
    *         midnight LOCAL TIME.
    * @see http://tycho.usno.navy.mil/mjd.html for more information about local
    *      C-JDN
    */
   public static long getJulianDay (final Calendar cal)
   {
      return getUnixDay (cal) + EPOCH_UNIX_ERA_DAY;
   }
   
   public static long getJulianDay (final Date date)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime (date);
      cal.set (Calendar.HOUR_OF_DAY, 0);
      cal.set (Calendar.MINUTE, 0);
      cal.set (Calendar.SECOND, 0);
      cal.set (Calendar.MILLISECOND, 0);
      return getJulianDay (cal);
   }

   public static Date getDate (final long julian)
   {
      Calendar cal = Calendar.getInstance();
      long offset = cal.get (Calendar.ZONE_OFFSET) + cal.get (Calendar.DST_OFFSET);
      long unixDay = julian - EPOCH_UNIX_ERA_DAY;
      long unixMS = (unixDay * MILLISECS_PER_DAY) - offset;
      
      cal.setTime (new Date (unixMS));
      cal.set (Calendar.HOUR_OF_DAY, 0);
      cal.set (Calendar.MINUTE, 0);
      cal.set (Calendar.SECOND, 0);
      cal.set (Calendar.MILLISECOND, 0);
      return cal.getTime(); 
   }

   /**
    * Find the number of days between the given dates. Later end dates result in
    * positive values.  Note this is not the same as subtracting day numbers. 
    * Just after midnight subtracted from just before midnight is 0 days for 
    * this method while subtracting day numbers would yields 1 day.
    * 
    * @param end - any Calendar representing the moment of time at the end of the
    *           interval for calculation.
    */
   public static long daysBetween (final Calendar from, final Calendar to)
   {
      long endL = to.getTimeInMillis() + to.getTimeZone().getOffset (to.getTimeInMillis());
      long startL = from.getTimeInMillis() + from.getTimeZone().getOffset (from.getTimeInMillis());
      return (endL - startL) / MILLISECS_PER_DAY;
   }
   
   public static Calendar today()
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime (new Date());
      cal.set (Calendar.HOUR_OF_DAY, 0);
      cal.set (Calendar.MINUTE, 0);
      cal.set (Calendar.SECOND, 0);
      cal.set (Calendar.MILLISECOND, 0);
      return cal;
   }
   
   private static class DT              // Date Time pattern
   {
      private String format;
      private Pattern pattern;

      public DT (final String format, final String regex)
      {
         this.format = format;
         this.pattern = Pattern.compile ("(" + regex + ")"); // group 1
      }

      @Override public String toString()
      {
         return format;
      }
   }

   public static void main (final String[] args)
   {
      Calendar today = Calendar.getInstance();
      today.setTime (new Date());
      System.out.println ("Today: " + today.getTime());
      long julian = DateUtils.getJulianDay (today);
      System.out.println ("  getJulianDay: " + julian);
      System.out.println ("  getDate     : " + DateUtils.getDate (julian));
      System.out.println();
      
      // test parseDate
      ArrayList<String> dates = new ArrayList<String>();
      dates.add ("2006-01-31 11:59:00.123"); // yyyy-MM-dd HH:mm:ss
      dates.add ("2006-01-31 11:59:00");     // yyyy-MM-dd HH:mm:ss
      dates.add ("2006/01/31 11:59:00.001"); // yyyy/MM/dd HH:mm:ss.SSS
      dates.add ("2006/01/31 11:59:00");     // yyyy/MM/dd HH:mm:ss
      dates.add ("31 Jan 2006 11:59:00");    // dd MMM yyyy HH:mm:ss
      dates.add ("31Jan2006");               // ddMMMyyyy
      dates.add ("311159 Jan 06");           // ddHHmm MMM yy
      dates.add ("01/31/2006 11:59:00");     // MM/dd/yyyy HH:mm:ss
      dates.add ("01/31/2006");              // MM/dd/yyyy
      dates.add ("01/31/06 11:59:00");       // MM/dd/yy HH:mm:ss
      dates.add ("Tue Jan 31 11:59:00 EDT 2006");
      dates.add ("200601311159");            // yyyyMMddHHmm
      dates.add ("20060131");                // yyyyMMdd
      dates.add ("060131");                  // yyMMdd
      dates.add ("Jan 31, 2006 11:59:00");   // MMM d, yyyy HH:mm:ss
      dates.add ("1/31/2006");               // MM/dd/yyyy
      dates.add ("31 Jan 2006");             // dd MMM yyyy

      for (String date : dates)
      {
         System.out.print (StringUtils.pad (date, 30) + " => ");
         System.out.println (DateUtils.parse (date, null));
      }
      System.out.println();
   }
}
