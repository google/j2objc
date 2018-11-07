/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2000-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.timezone;
import java.util.Date;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.dev.test.TestUtil.JavaVendor;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.icu.util.SimpleTimeZone;
import android.icu.util.TimeZone;

/**
 * A test which discovers the boundaries of DST programmatically and verifies
 * that they are correct.
 */
public class TimeZoneBoundaryTest extends TestFmwk
{
    static final int ONE_SECOND = 1000;
    static final int ONE_MINUTE = 60*ONE_SECOND;
    static final int ONE_HOUR = 60*ONE_MINUTE;
    static final long ONE_DAY = 24*ONE_HOUR;
    static final long ONE_YEAR = (long)(365.25 * ONE_DAY);
    static final long SIX_MONTHS = ONE_YEAR / 2;

    static final int MONTH_LENGTH[] = {31,29,31,30,31,30,31,31,30,31,30,31};

    // These values are empirically determined to be correct
    static final long PST_1997_BEG  = 860320800000L;
    static final long PST_1997_END  = 877856400000L;

    // Minimum interval for binary searches in ms; should be no larger
    // than 1000.
    static final long INTERVAL = 10; // Milliseconds

    // [3Jan01 Liu] Updated for 2000f data
    static final String AUSTRALIA = "Australia/Adelaide";
    static final long AUSTRALIA_1997_BEG = 877797000000L;
    static final long AUSTRALIA_1997_END = 859653000000L;

    /**
     * Date.toString().substring() Boundary Test
     * Look for a DST changeover to occur within 6 months of the given Date.
     * The initial Date.toString() should yield a string containing the
     * startMode as a SUBSTRING.  The boundary will be tested to be
     * at the expectedBoundary value.
     */
    void findDaylightBoundaryUsingDate(Date d, String startMode, long expectedBoundary)
    {
        // Given a date with a year start, find the Daylight onset
        // and end.  The given date should be 1/1/xx in some year.

        if (d.toString().indexOf(startMode) == -1)
        {
            logln("Error: " + startMode + " not present in " + d);
        }

        // Use a binary search, assuming that we have a Standard
        // time at the midpoint.
        long min = d.getTime();
        long max = min + SIX_MONTHS;

        while ((max - min) >  INTERVAL)
        {
            long mid = (min + max) >> 1;
            String s = new Date(mid).toString();
            // logln(s);
            if (s.indexOf(startMode) != -1)
            {
                min = mid;
            }
            else
            {
                max = mid;
            }
        }

        logln("Date Before: " + showDate(min));
        logln("Date After:  " + showDate(max));
        long mindelta = expectedBoundary - min;
        // not used long maxdelta = max - expectedBoundary;
        if (mindelta >= 0 && mindelta <= INTERVAL &&
            mindelta >= 0 && mindelta <= INTERVAL)
            logln("PASS: Expected boundary at " + expectedBoundary);
        else
            errln("FAIL: Expected boundary at " + expectedBoundary);
    }

    // This test cannot be compiled until the inDaylightTime() method of GregorianCalendar
    // becomes public.
    //    static void findDaylightBoundaryUsingCalendar(Date d, boolean startsInDST)
    //    {
    //  // Given a date with a year start, find the Daylight onset
    //  // and end.  The given date should be 1/1/xx in some year.
    //
    //  GregorianCalendar cal = new GregorianCalendar();
    //  cal.setTime(d);
    //  if (cal.inDaylightTime() != startsInDST)
    //  {
    //      logln("Error: inDaylightTime(" + d + ") != " + startsInDST);
    //  }
    //
    //  // Use a binary search, assuming that we have a Standard
    //  // time at the midpoint.
    //  long min = d.getTime();
    //  long max = min + (long)(365.25 / 2 * 24*60*60*1000);
    //
    //  while ((max - min) >  INTERVAL)
    //  {
    //      long mid = (min + max) >> 1;
    //      cal.setTime(new Date(mid));
    //      if (cal.inDaylightTime() == startsInDST)
    //      {
    //      min = mid;
    //      }
    //      else
    //      {
    //      max = mid;
    //      }
    //  }
    //
    //  logln("Calendar Before: " + showDate(min));
    //  logln("Calendar After:  " + showDate(max));
    //    }

    void findDaylightBoundaryUsingTimeZone(Date d, boolean startsInDST, long expectedBoundary)
    {
        findDaylightBoundaryUsingTimeZone(d, startsInDST, expectedBoundary,
                                          TimeZone.getDefault());
    }

    void findDaylightBoundaryUsingTimeZone(Date d, boolean startsInDST,
                                           long expectedBoundary, TimeZone tz)
    {
        // Given a date with a year start, find the Daylight onset
        // and end.  The given date should be 1/1/xx in some year.

        // Use a binary search, assuming that we have a Standard
        // time at the midpoint.
        long min = d.getTime();
        long max = min + SIX_MONTHS;

        if (tz.inDaylightTime(d) != startsInDST)
        {
            errln("FAIL: " + tz.getID() + " inDaylightTime(" +
                  d + ") != " + startsInDST);
            startsInDST = !startsInDST; // Flip over; find the apparent value
        }

        if (tz.inDaylightTime(new Date(max)) == startsInDST)
        {
            errln("FAIL: " + tz.getID() + " inDaylightTime(" +
                  (new Date(max)) + ") != " + (!startsInDST));
            return;
        }

        while ((max - min) >  INTERVAL)
        {
            long mid = (min + max) >> 1;
            boolean isIn = tz.inDaylightTime(new Date(mid));
            if (isIn == startsInDST)
            {
                min = mid;
            }
            else
            {
                max = mid;
            }
        }

        logln(tz.getID() + " Before: " + showDate(min, tz));
        logln(tz.getID() + " After:  " + showDate(max, tz));

        long mindelta = expectedBoundary - min;
        // not used long maxdelta = max - expectedBoundary; 
        DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        fmt.setTimeZone(tz);
        if (mindelta >= 0 && mindelta <= INTERVAL &&
            mindelta >= 0 && mindelta <= INTERVAL)
            logln("PASS: Expected boundary at " + expectedBoundary + " = " + fmt.format(new Date(expectedBoundary)));
        else
            errln("FAIL: Expected boundary at " + expectedBoundary + " = " + fmt.format(new Date(expectedBoundary)));
    }

    private static String showDate(long l)
    {
        return showDate(new Date(l));
    }

    private static String showDate(Date d)
    {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(d);
        return "" + (cal.get(Calendar.YEAR) - 1900) + "/" + 
               showNN(cal.get(Calendar.MONTH) + 1) + "/" + 
               showNN(cal.get(Calendar.DAY_OF_MONTH)) + " " + 
               showNN(cal.get(Calendar.HOUR_OF_DAY)) + ":" 
               + showNN(cal.get(Calendar.MINUTE)) + " \"" + d + "\" = " +
               d.getTime();
    }

    private static String showDate(long l, TimeZone z)
    {
        return showDate(new Date(l), z);
    }

    private static String showDate(Date d, TimeZone zone)
    {
        DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        fmt.setTimeZone(zone);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(d);
        return "" + (cal.get(Calendar.YEAR) - 1900) + "/" + 
               showNN(cal.get(Calendar.MONTH) + 1) + "/" + 
               showNN(cal.get(Calendar.DAY_OF_MONTH)) + " " + 
               showNN(cal.get(Calendar.HOUR_OF_DAY)) + ":" + 
               showNN(cal.get(Calendar.MINUTE)) + " \"" + d + "\" = " +
               fmt.format(d) + " = " + d.getTime();
    }

    private static String showNN(int n)
    {
        return ((n < 10) ? "0" : "") + n;
    }

    /**
     * Given a date, a TimeZone, and expected values for inDaylightTime,
     * useDaylightTime, zone and DST offset, verify that this is the case.
     */
    void verifyDST(String tag, Calendar cal, TimeZone time_zone,
                   boolean expUseDaylightTime, boolean expInDaylightTime,
                   int expRawOffset, int expOffset)
    {
        Date d = cal.getTime();

        logln("-- " + tag + ": " + d +
              " in zone " + time_zone.getID() + " (" +
              d.getTime()/3600000.0 + ")");

        if (time_zone.inDaylightTime(d) == expInDaylightTime)
            logln("PASS: inDaylightTime = " + time_zone.inDaylightTime(d));
        else
            errln("FAIL: inDaylightTime = " + time_zone.inDaylightTime(d));

        if (time_zone.useDaylightTime() == expUseDaylightTime)
            logln("PASS: useDaylightTime = " + time_zone.useDaylightTime());
        else
            errln("FAIL: useDaylightTime = " + time_zone.useDaylightTime());

        if (time_zone.getRawOffset() == expRawOffset)
            logln("PASS: getRawOffset() = " + expRawOffset/(double)ONE_HOUR);
        else
            errln("FAIL: getRawOffset() = " + time_zone.getRawOffset()/(double)ONE_HOUR +
                  "; expected " + expRawOffset/(double)ONE_HOUR);

        //GregorianCalendar gc = new GregorianCalendar(time_zone);
        //gc.setTime(d);
        int offset = time_zone.getOffset(cal.get(Calendar.ERA), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                                         cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_WEEK),
                                         ((cal.get(Calendar.HOUR_OF_DAY) * 60 +
                                           cal.get(Calendar.MINUTE)) * 60 +
                                          cal.get(Calendar.SECOND)) * 1000 +
                                         cal.get(Calendar.MILLISECOND));
        if (offset == expOffset)
            logln("PASS: getOffset() = " + offset/(double)ONE_HOUR);
        else {
            logln("era=" + cal.get(Calendar.ERA) +
                  ", year=" + cal.get(Calendar.YEAR) +
                  ", month=" + cal.get(Calendar.MONTH) +
                  ", dom=" + cal.get(Calendar.DAY_OF_MONTH) +
                  ", dow=" + cal.get(Calendar.DAY_OF_WEEK) +
                  ", time-of-day=" + (((cal.get(Calendar.HOUR_OF_DAY) * 60 +
                               cal.get(Calendar.MINUTE)) * 60 +
                              cal.get(Calendar.SECOND)) * 1000 +
                             cal.get(Calendar.MILLISECOND)) / 3600000.0 +
                            " hours");
            errln("FAIL: getOffset() = " + offset/(double)ONE_HOUR +
                  "; expected " + expOffset/(double)ONE_HOUR);
        }
    }

    /**
     * Check that the given year/month/dom/hour maps to and from the
     * given epochHours.  This verifies the functioning of the
     * calendar and time zone in conjunction with one another,
     * including the calendar time->fields and fields->time and
     * the time zone getOffset method.
     *
     * @param epochHours hours after Jan 1 1970 0:00 GMT.
     */
    void verifyMapping(Calendar cal, int year, int month, int dom, int hour,
                       double epochHours) {
        double H = 3600000.0;
        cal.clear();
        cal.set(year, month, dom, hour, 0, 0);
        Date d = cal.getTime();
        double e = d.getTime() / H;
        Date ed = new Date((long)(epochHours * H));
        if (e == epochHours) {
            logln("Ok: " + year + "/" + (month+1) + "/" + dom + " " + hour + ":00 => " +
                  e + " (" + ed + ")");
        } else {
            errln("FAIL: " + year + "/" + (month+1) + "/" + dom + " " + hour + ":00 => " +
                  e + " (" + new Date((long)(e * H)) + ")" +
                  ", expected " + epochHours + " (" + ed + ")");
        }
        cal.setTime(ed);
        if (cal.get(Calendar.YEAR) == year &&
            cal.get(Calendar.MONTH) == month &&
            cal.get(Calendar.DATE) == dom &&
            cal.get(Calendar.MILLISECONDS_IN_DAY) == hour * 3600000) {
            logln("Ok: " + epochHours + " (" + ed + ") => " +
                  cal.get(Calendar.YEAR) + "/" +
                  (cal.get(Calendar.MONTH)+1) + "/" +
                  cal.get(Calendar.DATE) + " " +
                  cal.get(Calendar.MILLISECONDS_IN_DAY)/H);
        } else {
            errln("FAIL: " + epochHours + " (" + ed + ") => " +
                  cal.get(Calendar.YEAR) + "/" +
                  (cal.get(Calendar.MONTH)+1) + "/" +
                  cal.get(Calendar.DATE) + " " +
                  cal.get(Calendar.MILLISECONDS_IN_DAY)/H +
                  ", expected " + year + "/" + (month+1) + "/" + dom +
                  " " + hour);
        }
    }

// NOTE: Enable this code to check the behavior of the underlying JDK,
// using a JDK Calendar object.
//
//    int millisInDay(java.util.Calendar cal) {
//        return ((cal.get(Calendar.HOUR_OF_DAY) * 60 +
//                 cal.get(Calendar.MINUTE)) * 60 +
//                cal.get(Calendar.SECOND)) * 1000 +
//            cal.get(Calendar.MILLISECOND);
//    }
//
//    void verifyMapping(java.util.Calendar cal, int year, int month, int dom, int hour,
//                       double epochHours) {
//        cal.clear();
//        cal.set(year, month, dom, hour, 0, 0);
//        Date d = cal.getTime();
//        double e = d.getTime() / 3600000.0;
//        Date ed = new Date((long)(epochHours * 3600000));
//        if (e == epochHours) {
//            logln("Ok: " + year + "/" + (month+1) + "/" + dom + " " + hour + ":00 => " +
//                  e + " (" + ed + ")");
//        } else {
//            errln("FAIL: " + year + "/" + (month+1) + "/" + dom + " " + hour + ":00 => " +
//                  e + " (" + new Date((long)(e * 3600000)) + ")" +
//                  ", expected " + epochHours + " (" + ed + ")");
//        }
//        cal.setTime(ed);
//        if (cal.get(Calendar.YEAR) == year &&
//            cal.get(Calendar.MONTH) == month &&
//            cal.get(Calendar.DATE) == dom &&
//            millisInDay(cal) == hour * 3600000) {
//            logln("Ok: " + epochHours + " (" + ed + ") => " +
//                  cal.get(Calendar.YEAR) + "/" +
//                  (cal.get(Calendar.MONTH)+1) + "/" +
//                  cal.get(Calendar.DATE) + " " +
//                  millisInDay(cal)/3600000.0);
//        } else {
//            errln("FAIL: " + epochHours + " (" + ed + ") => " +
//                  cal.get(Calendar.YEAR) + "/" +
//                  (cal.get(Calendar.MONTH)+1) + "/" +
//                  cal.get(Calendar.DATE) + " " +
//                  millisInDay(cal)/3600000.0 +
//                  ", expected " + year + "/" + (month+1) + "/" + dom +
//                  " " + hour);
//        }
//    }

    @Test
    public void TestBoundaries()
    {
        TimeZone save = TimeZone.getDefault();

        // Check basic mappings.  We had a problem with this for ICU
        // 2.8 after migrating to using pass-through time zones.  The
        // problem appeared only on JDK 1.3.
        TimeZone pst = safeGetTimeZone("PST");
        Calendar tempcal = Calendar.getInstance(pst);
        verifyMapping(tempcal, 1997, Calendar.APRIL, 3,  0, 238904.0);
        verifyMapping(tempcal, 1997, Calendar.APRIL, 4,  0, 238928.0);
        verifyMapping(tempcal, 1997, Calendar.APRIL, 5,  0, 238952.0);
        verifyMapping(tempcal, 1997, Calendar.APRIL, 5, 23, 238975.0);
        verifyMapping(tempcal, 1997, Calendar.APRIL, 6,  0, 238976.0);
        verifyMapping(tempcal, 1997, Calendar.APRIL, 6,  1, 238977.0);
        verifyMapping(tempcal, 1997, Calendar.APRIL, 6,  3, 238978.0);
        
        TimeZone utc = safeGetTimeZone("UTC");
        Calendar utccal = Calendar.getInstance(utc);
        verifyMapping(utccal, 1997, Calendar.APRIL, 6, 0, 238968.0);

// NOTE: Enable this code to check the behavior of the underlying JDK,
// using a JDK Calendar object.
//
//        java.util.TimeZone jdkpst = java.util.TimeZone.getTimeZone("PST");
//        java.util.Calendar jdkcal = java.util.Calendar.getInstance(jdkpst);
//        verifyMapping(jdkcal, 1997, Calendar.APRIL, 5,  0, 238952.0);
//        verifyMapping(jdkcal, 1997, Calendar.APRIL, 5, 23, 238975.0);
//        verifyMapping(jdkcal, 1997, Calendar.APRIL, 6,  0, 238976.0);
//        verifyMapping(jdkcal, 1997, Calendar.APRIL, 6,  1, 238977.0);
//        verifyMapping(jdkcal, 1997, Calendar.APRIL, 6,  3, 238978.0);

        tempcal.clear();
        tempcal.set(1997, Calendar.APRIL, 6);
        Date d = tempcal.getTime();

        try {
            TimeZone.setDefault(pst);

            // DST changeover for PST is 4/6/1997 at 2 hours past midnight
            // at 238978.0 epoch hours.

            // i is minutes past midnight standard time
            for (int i=-120; i<=180; i+=60)
            {
                boolean inDST = (i >= 120);
                tempcal.setTimeInMillis(d.getTime() + i*60*1000);
                verifyDST("hour=" + i/60,
                          tempcal, pst, true, inDST, -8*ONE_HOUR,
                          inDST ? -7*ONE_HOUR : -8*ONE_HOUR);
            }
        } finally {
            TimeZone.setDefault(save);
        }

        // We no longer use ICU TimeZone implementation for Java
        // default TimeZone.  Java 1.3 or older version do not
        // support historic transitions, therefore, the test below
        // will fail on such environment (with the latest TimeZone
        // patch for US 2007+ rule).
        /* J2ObjC: always execute. */
        if (true) {
            // This only works in PST/PDT
            TimeZone.setDefault(safeGetTimeZone("PST"));
            logln("========================================");
            tempcal.set(1997, 0, 1);
            findDaylightBoundaryUsingDate(tempcal.getTime(), "PST", PST_1997_BEG);
            logln("========================================");
            tempcal.set(1997, 6, 1);
            findDaylightBoundaryUsingDate(tempcal.getTime(), "PDT", PST_1997_END);
        }

        //  if (true)
        //  {
        //      logln("========================================");
        //      findDaylightBoundaryUsingCalendar(new Date(97,0,1), false);
        //      logln("========================================");
        //      findDaylightBoundaryUsingCalendar(new Date(97,6,1), true);
        //  }

        if (true)
        {
            // Southern hemisphere test
            logln("========================================");
            TimeZone z = safeGetTimeZone(AUSTRALIA);
            tempcal.set(1997, 0, 1);
            findDaylightBoundaryUsingTimeZone(tempcal.getTime(), true, AUSTRALIA_1997_END, z);
            logln("========================================");
            tempcal.set(1997, 6, 1);
            findDaylightBoundaryUsingTimeZone(tempcal.getTime(), false, AUSTRALIA_1997_BEG, z);
        }

        if (true)
        {
            logln("========================================");
            tempcal.set(1997, 0, 1);
            findDaylightBoundaryUsingTimeZone(tempcal.getTime(), false, PST_1997_BEG);
            logln("========================================");
            tempcal.set(1997, 6, 1);
            findDaylightBoundaryUsingTimeZone(tempcal.getTime(), true, PST_1997_END);
        }

        // This just shows the offset for April 4-7 in 1997.  This is redundant
        // with a test above, so we disable it.
        if (false)
        {
            TimeZone z = TimeZone.getDefault();
            tempcal.set(1997, 3, 4);
            logln(z.getOffset(1, 97, 3, 4, 6, 0) + " " + tempcal.getTime());
            tempcal.set(1997, 3, 5);
            logln(z.getOffset(1, 97, 3, 5, 7, 0) + " " + tempcal.getTime());
            tempcal.set(1997, 3, 6);
            logln(z.getOffset(1, 97, 3, 6, 1, 0) + " " + tempcal.getTime());
            tempcal.set(1997, 3, 7);
            logln(z.getOffset(1, 97, 3, 7, 2, 0) + " " + tempcal.getTime());
        }
    }


    //----------------------------------------------------------------------
    // Can't do any of these without a public inDaylightTime in GC
    //----------------------------------------------------------------------


    //    static GregorianCalendar cal = new GregorianCalendar();
    //
    //    static void _testUsingBinarySearch(Date d, boolean startsInDST)
    //    {
    //  // Given a date with a year start, find the Daylight onset
    //  // and end.  The given date should be 1/1/xx in some year.
    //
    //  // Use a binary search, assuming that we have a Standard
    //  // time at the midpoint.
    //  long min = d.getTime();
    //  long max = min + (long)(365.25 / 2 * ONE_DAY);
    //
    //  // First check the max
    //  cal.setTime(new Date(max));
    //  if (cal.inDaylightTime() == startsInDST)
    //  {
    //      logln("Error: inDaylightTime(" + (new Date(max)) + ") != " + (!startsInDST));
    //  }
    //
    //  cal.setTime(d);
    //  if (cal.inDaylightTime() != startsInDST)
    //  {
    //      logln("Error: inDaylightTime(" + d + ") != " + startsInDST);
    //  }
    //
    //  while ((max - min) >  INTERVAL)
    //  {
    //      long mid = (min + max) >> 1;
    //      cal.setTime(new Date(mid));
    //      if (cal.inDaylightTime() == startsInDST)
    //      {
    //      min = mid;
    //      }
    //      else
    //      {
    //      max = mid;
    //      }
    //  }
    //
    //  logln("Binary Search Before: " + showDate(min));
    //  logln("Binary Search After:  " + showDate(max));
    //    }
    //
    //    static void _testUsingMillis(Date d, boolean startsInDST)
    //    {
    //  long millis = d.getTime();
    //  long max = millis + (long)(370 * ONE_DAY); // A year plus extra
    //
    //  boolean lastDST = startsInDST;
    //  while (millis < max)
    //  {
    //      cal.setTime(new Date(millis));
    //      boolean inDaylight = cal.inDaylightTime();
    //
    //      if (inDaylight != lastDST)
    //      {
    //      logln("Switch " + (inDaylight ? "into" : "out of")
    //                 + " DST at " + (new Date(millis)));
    //      lastDST = inDaylight;
    //      }
    //
    //      millis += 15*ONE_MINUTE;
    //  }
    //    }
    //
    //    static void _testUsingFields(int y, boolean startsInDST)
    //    {
    //  boolean lastDST = startsInDST;
    //  for (int m = 0; m < 12; ++m)
    //  {
    //      for (int d = 1; d <= MONTH_LENGTH[m]; ++d)
    //      {
    //      for (int h = 0; h < 24; ++h)
    //      {
    //          for (int min = 0; min < 60; min += 15)
    //          {
    //          cal.clear();
    //          cal.set(y, m, d, h, min);
    //          boolean inDaylight = cal.inDaylightTime();
    //          if (inDaylight != lastDST)
    //          {
    //              lastDST = inDaylight;
    //              log("Switch " + (lastDST ? "into" : "out of")
    //                       + " DST at " + y + "/" + (m+1) + "/" + d
    //                       + " " + showNN(h) + ":" + showNN(min));
    //              logln(" " + cal.getTime());
    //
    //              cal.set(y, m, d, h-1, 45);
    //              log("Before = "
    //+ y + "/" + (m+1) + "/" + d
    //+ " " + showNN(h-1) + ":" + showNN(45));
    //              logln(" " + cal.getTime());
    //          }
    //          }
    //      }
    //      }
    //  }
    //    }
    //
    //    public void Test1()
    //    {
    //  logln(Locale.getDefault().getDisplayName());
    //  logln(TimeZone.getDefault().getID());
    //  logln(new Date(0));
    //
    //  if (true)
    //  {
    //      logln("========================================");
    //      _testUsingBinarySearch(new Date(97,0,1), false);
    //      logln("========================================");
    //      _testUsingBinarySearch(new Date(97,6,1), true);
    //  }
    //
    //  if (true)
    //  {
    //      logln("========================================");
    //      logln("Stepping using millis");
    //      _testUsingMillis(new Date(97,0,1), false);
    //  }
    //
    //  if (true)
    //  {
    //      logln("========================================");
    //      logln("Stepping using fields");
    //      _testUsingFields(1997, false);
    //  }
    //
    //  if (false)
    //  {
    //      cal.clear();
    //      cal.set(1997, 3, 5, 10, 0);
    //      //  cal.inDaylightTime();
    //      logln("Date = " + cal.getTime());
    //      logln("Millis = " + cal.getTime().getTime()/3600000);
    //  }
    //    }

    //----------------------------------------------------------------------
    //----------------------------------------------------------------------
    //----------------------------------------------------------------------

    void _testUsingBinarySearch(SimpleTimeZone tz, Date d, long expectedBoundary)
    {
        // Given a date with a year start, find the Daylight onset
        // and end.  The given date should be 1/1/xx in some year.

        // Use a binary search, assuming that we have a Standard
        // time at the midpoint.
        long min = d.getTime();
        long max = min + (long)(365.25 / 2 * ONE_DAY);

        // First check the boundaries
        boolean startsInDST = tz.inDaylightTime(d);

        if (tz.inDaylightTime(new Date(max)) == startsInDST)
        {
            errln("Error: inDaylightTime(" + (new Date(max)) + ") != " + (!startsInDST));
        }

        while ((max - min) >  INTERVAL)
        {
            long mid = (min + max) >> 1;
            if (tz.inDaylightTime(new Date(mid)) == startsInDST)
            {
                min = mid;
            }
            else
            {
                max = mid;
            }
        }

        logln("Binary Search Before: " + showDate(min));
        logln("Binary Search After:  " + showDate(max));

        long mindelta = expectedBoundary - min;
        // not used long maxdelta = max - expectedBoundary;
        if (mindelta >= 0 && mindelta <= INTERVAL &&
            mindelta >= 0 && mindelta <= INTERVAL)
            logln("PASS: Expected boundary at " + expectedBoundary);
        else
            errln("FAIL: Expected boundary at " + expectedBoundary);
    }

    /*
      static void _testUsingMillis(Date d, boolean startsInDST)
      {
      long millis = d.getTime();
      long max = millis + (long)(370 * ONE_DAY); // A year plus extra

      boolean lastDST = startsInDST;
      while (millis < max)
      {
      cal.setTime(new Date(millis));
      boolean inDaylight = cal.inDaylightTime();

      if (inDaylight != lastDST)
      {
      logln("Switch " + (inDaylight ? "into" : "out of")
      + " DST at " + (new Date(millis)));
      lastDST = inDaylight;
      }

      millis += 15*ONE_MINUTE;
      }
      }
      */

    /**
     * Test new rule formats.
     */
    @Test
    public void TestNewRules()
    {
        //logln(Locale.getDefault().getDisplayName());
        //logln(TimeZone.getDefault().getID());
        //logln(new Date(0));

        if (true)
        {
            // Doesn't matter what the default TimeZone is here, since we
            // are creating our own TimeZone objects.

            SimpleTimeZone tz;
            java.util.Calendar tempcal = java.util.Calendar.getInstance();
            tempcal.clear();

            logln("-----------------------------------------------------------------");
            logln("Aug 2ndTues .. Mar 15");
            tz = new SimpleTimeZone(-8*ONE_HOUR, "Test_1",
                                    Calendar.AUGUST, 2, Calendar.TUESDAY, 2*ONE_HOUR,
                                    Calendar.MARCH, 15, 0, 2*ONE_HOUR);
            //logln(tz.toString());
            logln("========================================");
            tempcal.set(1997, 0, 1);
            _testUsingBinarySearch(tz, tempcal.getTime(), 858416400000L);
            logln("========================================");
            tempcal.set(1997, 6, 1);
            _testUsingBinarySearch(tz, tempcal.getTime(), 871380000000L);

            logln("-----------------------------------------------------------------");
            logln("Apr Wed>=14 .. Sep Sun<=20");
            tz = new SimpleTimeZone(-8*ONE_HOUR, "Test_2",
                                    Calendar.APRIL, 14, -Calendar.WEDNESDAY, 2*ONE_HOUR,
                                    Calendar.SEPTEMBER, -20, -Calendar.SUNDAY, 2*ONE_HOUR);
            //logln(tz.toString());
            logln("========================================");
            tempcal.set(1997, 0, 1);
            _testUsingBinarySearch(tz, tempcal.getTime(), 861184800000L);
            logln("========================================");
            tempcal.set(1997, 6, 1);
            _testUsingBinarySearch(tz, tempcal.getTime(), 874227600000L);
        }

        /*
          if (true)
          {
          logln("========================================");
          logln("Stepping using millis");
          _testUsingMillis(new Date(97,0,1), false);
          }

          if (true)
          {
          logln("========================================");
          logln("Stepping using fields");
          _testUsingFields(1997, false);
          }

          if (false)
          {
          cal.clear();
          cal.set(1997, 3, 5, 10, 0);
          //    cal.inDaylightTime();
          logln("Date = " + cal.getTime());
          logln("Millis = " + cal.getTime().getTime()/3600000);
          }
          */
    }

    //----------------------------------------------------------------------
    //----------------------------------------------------------------------
    //----------------------------------------------------------------------
    // Long Bug
    //----------------------------------------------------------------------
    //----------------------------------------------------------------------
    //----------------------------------------------------------------------

    //public void Test3()
    //{
    //    findDaylightBoundaryUsingTimeZone(new Date(97,6,1), true);
    //}

    /**
     * Find boundaries by stepping.
     */
    void findBoundariesStepwise(int year, long interval, TimeZone z, int expectedChanges)
    {
        java.util.Calendar tempcal = java.util.Calendar.getInstance();
        tempcal.clear();
        tempcal.set(year, Calendar.JANUARY, 1);
        Date d = tempcal.getTime();
        long time = d.getTime(); // ms
        long limit = time + ONE_YEAR + ONE_DAY;
        boolean lastState = z.inDaylightTime(d);
        int changes = 0;
        logln("-- Zone " + z.getID() + " starts in " + year + " with DST = " + lastState);
        logln("useDaylightTime = " + z.useDaylightTime());
        while (time < limit)
        {
            d.setTime(time);
            boolean state = z.inDaylightTime(d);
            if (state != lastState)
            {
                logln((state ? "Entry " : "Exit ") +
                      "at " + d);
                lastState = state;
                ++changes;
            }
            time += interval;
        }
        if (changes == 0)
        {
            if (!lastState && !z.useDaylightTime()) logln("No DST");
            else errln("FAIL: DST all year, or no DST with true useDaylightTime");
        }
        else if (changes != 2)
        {
            errln("FAIL: " + changes + " changes seen; should see 0 or 2");
        }
        else if (!z.useDaylightTime())
        {
            errln("FAIL: useDaylightTime false but 2 changes seen");
        }
        if (changes != expectedChanges)
        {
            errln("FAIL: " + changes + " changes seen; expected " + expectedChanges);
        }
    }

    @Test
    public void TestStepwise()
    {
        findBoundariesStepwise(1997, ONE_DAY, safeGetTimeZone("America/New_York"), 2);
        // disabled Oct 2003 aliu; ACT could mean anything, depending on the underlying JDK, as of 2.8
        // findBoundariesStepwise(1997, ONE_DAY, safeGetTimeZone("ACT"), 2);
        findBoundariesStepwise(1997, ONE_DAY, safeGetTimeZone("America/Phoenix"), 0); // Added 3Jan01
        findBoundariesStepwise(1997, ONE_DAY, safeGetTimeZone(AUSTRALIA), 2);
    }
}
