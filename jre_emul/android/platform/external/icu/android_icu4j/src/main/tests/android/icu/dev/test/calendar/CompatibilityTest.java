/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2000-2009,2011 International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.calendar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.SimpleTimeZone;
import android.icu.util.TimeZone;

public class CompatibilityTest extends android.icu.dev.test.TestFmwk {
    static final String[] FIELD_NAME = {
        "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH",
        "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK",
        "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY",
        "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET",
        "DST_OFFSET", "YEAR_WOY", "DOW_LOCAL", "EXTENDED_YEAR",
        "JULIAN_DAY", "MILLISECONDS_IN_DAY",
    };

    /**
     * Test the behavior of the GregorianCalendar around the changeover.
     */
    @Test
    public void TestGregorianChangeover() {
    
        java.util.TimeZone jdkGMT = java.util.TimeZone.getTimeZone("GMT");
        java.util.Calendar jdkCal = java.util.Calendar.getInstance(jdkGMT);
        jdkCal.clear();
        jdkCal.set(1582, Calendar.OCTOBER, 15);
        
//      if(jdkCal instanceof java.util.GregorianCalendar) {
//          logln("jdk IS grego");
//          java.util.GregorianCalendar jdkgc = (java.util.GregorianCalendar)
//          jdkCal;
//          logln("jdk change at: " + jdkgc.getGregorianChange() + "(" + jdkgc.getGregorianChange().getTime() +")" );
//      } else {
//          logln("jdk NOT grego");
//      }

        long a = jdkCal.getTime().getTime();
        Date c = jdkCal.getTime();
        c.toString();
        long b = c.getTime();
        if(a!=b) {
            logln(" " + a + " != " + b);
            logln("JDK has Gregorian cutover anomaly (1.5?) - skipping this test.");
            return;
        }

        Date co = jdkCal.getTime();
        logln("Change over (Oct 15 1582) = " + co + " (" + co.getTime() + ")");
        final int ONE_DAY = 24*60*60*1000;
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        GregorianCalendar cal = new GregorianCalendar(gmt);
        /*
          Changeover -7 days: 1582/9/28 dow=6
          Changeover -6 days: 1582/9/29 dow=7
          Changeover -5 days: 1582/9/30 dow=1
          Changeover -4 days: 1582/10/1 dow=2
          Changeover -3 days: 1582/10/2 dow=3
          Changeover -2 days: 1582/10/3 dow=4
          Changeover -1 days: 1582/10/4 dow=5
          Changeover +0 days: 1582/10/15 dow=6
          Changeover +1 days: 1582/10/16 dow=7
          Changeover +2 days: 1582/10/17 dow=1
          Changeover +3 days: 1582/10/18 dow=2
          Changeover +4 days: 1582/10/19 dow=3
          Changeover +5 days: 1582/10/20 dow=4
          Changeover +6 days: 1582/10/21 dow=5
          Changeover +7 days: 1582/10/22 dow=6
          */
        int MON[] = {  9,  9,  9,10,10,10,10, 10, 10, 10, 10, 10, 10, 10, 10 };
        int DOM[] = { 28, 29, 30, 1, 2, 3, 4, 15, 16, 17, 18, 19, 20, 21, 22 };
        int DOW[] = {  6,  7,  1, 2, 3, 4, 5,  6,  7,  1,  2,  3,  4,  5,  6 };
        //                                     ^ <-Changeover Fri Oct 15 1582
        int j=0;
        for (int i=-7; i<=7; ++i, ++j) {
            Date d = new Date(co.getTime() + i*ONE_DAY);
            cal.setTime(d);
            int y = cal.get(Calendar.YEAR), mon = cal.get(Calendar.MONTH)+1-Calendar.JANUARY,
                dom = cal.get(Calendar.DATE), dow = cal.get(Calendar.DAY_OF_WEEK);
            logln("Changeover " + (i>=0?"+":"") +
                  i + " days: " + y + "/" + mon + "/" + dom + " dow=" + dow);
            if (y != 1582 || mon != MON[j] || dom != DOM[j] || dow != DOW[j])
                errln(" Fail: Above line is wrong");
        }
    }

    /**
     * Test the mapping between millis and fields.  For the purposes
     * of this test, we don't care about timezones and week data
     * (first day of week, minimal days in first week).
     */
    @Test
    public void TestMapping() {
        if (false) {
            Date PURE_GREGORIAN = new Date(Long.MIN_VALUE);
            Date PURE_JULIAN = new Date(Long.MAX_VALUE);
            GregorianCalendar cal =
                new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            final int EPOCH_JULIAN = 2440588;
            final long ONE_DAY = 24*60*60*1000L;
            android.icu.text.SimpleDateFormat fmt =
                new android.icu.text.SimpleDateFormat("EEE MMM dd yyyy G");
                /*HH:mm:ss.SSS z*/

            for (int type=0; type<2; ++type) {
                System.out.println(type==0 ? "Gregorian" : "Julian");
                cal.setGregorianChange(type==0 ? PURE_GREGORIAN : PURE_JULIAN);
                fmt.setCalendar(cal);
                int[] J = {
                    0x7FFFFFFF,
                    0x7FFFFFF0,
                    0x7F000000,
                    0x78000000,
                    0x70000000,
                    0x60000000,
                    0x50000000,
                    0x40000000,
                    0x30000000,
                    0x20000000,
                    0x10000000,
                };
                for (int i=0; i<J.length; ++i) {
                    String[] lim = new String[2];
                    long[] ms = new long[2];
                    int jd = J[i];
                    for (int sign=0; sign<2; ++sign) {
                        int julian = jd;
                        if (sign==0) julian = -julian;
                        long millis = ((long)julian - EPOCH_JULIAN) * ONE_DAY;
                        ms[sign] = millis;
                        cal.setTime(new Date(millis));
                        lim[sign] = fmt.format(cal.getTime());
                    }
                    System.out.println("JD +/-" +
                                       Long.toString(jd, 16) +
                                       ": " + ms[0] + ".." + ms[1] +
                                       ": " + lim[0] + ".." + lim[1]);
                }
            }
        }

        TimeZone saveZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            //NEWCAL
            Date PURE_GREGORIAN = new Date(Long.MIN_VALUE);
            Date PURE_JULIAN = new Date(Long.MAX_VALUE);
            GregorianCalendar cal = new GregorianCalendar();
            final int EPOCH_JULIAN = 2440588;
            final long ONE_DAY = 24*60*60*1000L;
            int[] DATA = {
                // Julian#   Year  Month               DOM   JULIAN:Year, Month,       DOM
                2440588,     1970, Calendar.JANUARY,   1,    1969, Calendar.DECEMBER,  19, 
                2415080,     1900, Calendar.MARCH,     1,    1900, Calendar.FEBRUARY,  17,
                2451604,     2000, Calendar.FEBRUARY,  29,   2000, Calendar.FEBRUARY,  16,
                2452269,     2001, Calendar.DECEMBER,  25,   2001, Calendar.DECEMBER,  12,
                2416526,     1904, Calendar.FEBRUARY,  15,   1904, Calendar.FEBRUARY,  2,
                2416656,     1904, Calendar.JUNE,      24,   1904, Calendar.JUNE,      11,
                1721426,        1, Calendar.JANUARY,   1,       1, Calendar.JANUARY,   3,
                2000000,      763, Calendar.SEPTEMBER, 18,    763, Calendar.SEPTEMBER, 14,
                4000000,     6239, Calendar.JULY,      12,   6239, Calendar.MAY,       28,
                8000000,    17191, Calendar.FEBRUARY,  26,  17190, Calendar.OCTOBER,   22,
                10000000,   22666, Calendar.DECEMBER,  20,  22666, Calendar.JULY,      5,
            };
            for (int i=0; i<DATA.length; i+=7) {
                int julian = DATA[i];
                int year = DATA[i+1];
                int month = DATA[i+2];
                int dom = DATA[i+3];
                int year2, month2, dom2;
                long millis = (julian - EPOCH_JULIAN) * ONE_DAY;
                String s;

                // Test Gregorian computation
                cal.setGregorianChange(PURE_GREGORIAN);
                cal.clear();
                cal.set(year, month, dom);
                long calMillis = cal.getTime().getTime();
                long delta = calMillis - millis;
                cal.setTime(new Date(millis));
                year2 = cal.get(Calendar.YEAR);
                month2 = cal.get(Calendar.MONTH);
                dom2 = cal.get(Calendar.DAY_OF_MONTH);
                s = "G " + year + "-" + (month+1-Calendar.JANUARY) + "-" + dom +
                    " => " + calMillis +
                    " (" + ((float)delta/ONE_DAY) + " day delta) => " +
                    year2 + "-" + (month2+1-Calendar.JANUARY) + "-" + dom2;
                if (delta != 0 || year != year2 || month != month2 ||
                    dom != dom2) errln(s + " FAIL");
                else logln(s);
                
                // Test Julian computation
                year = DATA[i+4];
                month = DATA[i+5];
                dom = DATA[i+6];
                cal.setGregorianChange(PURE_JULIAN);
                cal.clear();
                cal.set(year, month, dom);
                calMillis = cal.getTime().getTime();
                delta = calMillis - millis;
                cal.setTime(new Date(millis));
                year2 = cal.get(Calendar.YEAR);
                month2 = cal.get(Calendar.MONTH);
                dom2 = cal.get(Calendar.DAY_OF_MONTH);
                s = "J " + year + "-" + (month+1-Calendar.JANUARY) + "-" + dom +
                    " => " + calMillis +
                    " (" + ((float)delta/ONE_DAY) + " day delta) => " +
                    year2 + "-" + (month2+1-Calendar.JANUARY) + "-" + dom2;
                if (delta != 0 || year != year2 || month != month2 ||
                    dom != dom2) errln(s + " FAIL");
                else logln(s);
            }

            java.util.Calendar tempcal = java.util.Calendar.getInstance();
            tempcal.clear();
            tempcal.set(1582, Calendar.OCTOBER, 15);
            cal.setGregorianChange(tempcal.getTime());
            auxMapping(cal, 1582, Calendar.OCTOBER, 4);
            auxMapping(cal, 1582, Calendar.OCTOBER, 15);
            auxMapping(cal, 1582, Calendar.OCTOBER, 16);
            for (int y=800; y<3000; y+=1+(int)(100*Math.random())) {
                for (int m=Calendar.JANUARY; m<=Calendar.DECEMBER; ++m) {
                    auxMapping(cal, y, m, 15);
                }
            }
        }
        finally {
            TimeZone.setDefault(saveZone);
        }
    }
    private void auxMapping(Calendar cal, int y, int m, int d) {
        cal.clear();
        cal.set(y, m, d);
        long millis = cal.getTime().getTime();
        cal.setTime(new Date(millis));
        int year2 = cal.get(Calendar.YEAR);
        int month2 = cal.get(Calendar.MONTH);
        int dom2 = cal.get(Calendar.DAY_OF_MONTH);
        if (y != year2 || m != month2 || dom2 != d)
            errln("Round-trip failure: " + y + "-" + (m+1) + "-"+d+" =>ms=> " +
                  year2 + "-" + (month2+1) + "-" + dom2);
    }

    @Test
    public void TestGenericAPI() {
        // not used String str;

        java.util.Calendar tempcal = java.util.Calendar.getInstance();
        tempcal.clear();
        tempcal.set(1990, Calendar.APRIL, 15);
        Date when = tempcal.getTime();

        String tzid = "TestZone";
        int tzoffset = 123400;

        SimpleTimeZone zone = new SimpleTimeZone(tzoffset, tzid);
        Calendar cal = (Calendar)Calendar.getInstance((SimpleTimeZone)zone.clone());

        if (!zone.equals(cal.getTimeZone())) errln("FAIL: Calendar.getTimeZone failed");

        Calendar cal2 = Calendar.getInstance(cal.getTimeZone());

        cal.setTime(when);
        cal2.setTime(when);

        if (!(cal.equals(cal2))) errln("FAIL: Calendar.operator== failed");
        // if ((*cal != *cal2))  errln("FAIL: Calendar.operator!= failed");
        if (!cal.equals(cal2) ||
            cal.before(cal2) ||
            cal.after(cal2)) errln("FAIL: equals/before/after failed");

        cal2.setTime(new Date(when.getTime() + 1000));
        if (cal.equals(cal2) ||
            cal2.before(cal) ||
            cal.after(cal2)) errln("FAIL: equals/before/after failed");

        cal.roll(Calendar.SECOND, true);
        if (!cal.equals(cal2) ||
            cal.before(cal2) ||
            cal.after(cal2)) errln("FAIL: equals/before/after failed");

        // Roll back to January
        cal.roll(Calendar.MONTH, (int)(1 + Calendar.DECEMBER - cal.get(Calendar.MONTH)));
        if (cal.equals(cal2) ||
            cal2.before(cal) ||
            cal.after(cal2)) errln("FAIL: equals/before/after failed");

        // C++ only
        /* TimeZone z = cal.orphanTimeZone();
           if (z.getID(str) != tzid ||
           z.getRawOffset() != tzoffset)
           errln("FAIL: orphanTimeZone failed");
           */

        for (int i=0; i<2; ++i) {
            boolean lenient = ( i > 0 );
            cal.setLenient(lenient);
            if (lenient != cal.isLenient()) errln("FAIL: setLenient/isLenient failed");
            // Later: Check for lenient behavior
        }

        int i;
        for (i=Calendar.SUNDAY; i<=Calendar.SATURDAY; ++i) {
            cal.setFirstDayOfWeek(i);
            if (cal.getFirstDayOfWeek() != i) errln("FAIL: set/getFirstDayOfWeek failed");
        }

        for (i=1; i<=7; ++i) {
            cal.setMinimalDaysInFirstWeek(i);
            if (cal.getMinimalDaysInFirstWeek() != i) errln("FAIL: set/getFirstDayOfWeek failed");
        }

        for (i=0; i<cal.getFieldCount(); ++i) {
            if (cal.getMinimum(i) > cal.getGreatestMinimum(i))
                errln("FAIL: getMinimum larger than getGreatestMinimum for field " + i);
            if (cal.getLeastMaximum(i) > cal.getMaximum(i))
                errln("FAIL: getLeastMaximum larger than getMaximum for field " + i);
            if (cal.getMinimum(i) >= cal.getMaximum(i))
                errln("FAIL: getMinimum not less than getMaximum for field " + i);
        }

        cal.setTimeZone(TimeZone.getDefault());
        cal.clear();
        cal.set(1984, 5, 24);
        tempcal.clear();
        tempcal.set(1984, 5, 24);
        if (cal.getTime().getTime() != tempcal.getTime().getTime()) {
            errln("FAIL: Calendar.set(3 args) failed");
            logln(" Got: " + cal.getTime() + "  Expected: " + tempcal.getTime());
        }

        cal.clear();
        cal.set(1985, 2, 2, 11, 49);
        tempcal.clear();
        tempcal.set(1985, 2, 2, 11, 49);
        if (cal.getTime().getTime() != tempcal.getTime().getTime()) {
            errln("FAIL: Calendar.set(5 args) failed");
            logln(" Got: " + cal.getTime() + "  Expected: " + tempcal.getTime());
        }

        cal.clear();
        cal.set(1995, 9, 12, 1, 39, 55);
        tempcal.clear();
        tempcal.set(1995, 9, 12, 1, 39, 55);
        if (cal.getTime().getTime() != tempcal.getTime().getTime()) {
            errln("FAIL: Calendar.set(6 args) failed");
            logln(" Got: " + cal.getTime() + "  Expected: " + tempcal.getTime());
        }

        cal.getTime();
        // This test is strange -- why do we expect certain fields to be set, and
        // others not to be?  Revisit the appropriateness of this.  - Alan NEWCAL
        for (i=0; i<cal.getFieldCount(); ++i) {
            switch(i) {
            case Calendar.YEAR: case Calendar.MONTH: case Calendar.DATE:
            case Calendar.HOUR_OF_DAY: case Calendar.MINUTE: case Calendar.SECOND:
            case Calendar.EXTENDED_YEAR:
                if (!cal.isSet(i)) errln("FAIL: " + FIELD_NAME[i] + " is not set");
                break;
            default:
                if (cal.isSet(i)) errln("FAIL: " + FIELD_NAME[i] + " is set");
            }
            cal.clear(i);
            if (cal.isSet(i)) errln("FAIL: Calendar.clear/isSet failed");
        }

        // delete cal;
        // delete cal2;

        Locale[] loc = Calendar.getAvailableLocales();
        long count = loc.length;
        if (count < 1 || loc == null) {
            errln("FAIL: getAvailableLocales failed");
        }
        else {
            for (i=0; i<count; ++i) {
                cal = Calendar.getInstance(loc[i]);
                // delete cal;
            }
        }

        cal = Calendar.getInstance(TimeZone.getDefault(), Locale.ENGLISH);
        // delete cal;

        cal = Calendar.getInstance(zone, Locale.ENGLISH);
        // delete cal;

        GregorianCalendar gc = new GregorianCalendar(zone);
        // delete gc;

        gc = new GregorianCalendar(Locale.ENGLISH);
        // delete gc;

        gc = new GregorianCalendar(Locale.ENGLISH);
        // delete gc;

        gc = new GregorianCalendar(zone, Locale.ENGLISH);
        // delete gc;

        gc = new GregorianCalendar(zone);
        // delete gc;

        gc = new GregorianCalendar(1998, 10, 14, 21, 43);
        tempcal.clear();
        tempcal.set(1998, 10, 14, 21, 43);
        if (gc.getTime().getTime() != tempcal.getTime().getTime())
            errln("FAIL: new GregorianCalendar(ymdhm) failed");
        // delete gc;

        gc = new GregorianCalendar(1998, 10, 14, 21, 43, 55);
        tempcal.clear();
        tempcal.set(1998, 10, 14, 21, 43, 55);
        if (gc.getTime().getTime() != tempcal.getTime().getTime())
            errln("FAIL: new GregorianCalendar(ymdhms) failed");

        // C++ only:
        // GregorianCalendar gc2 = new GregorianCalendar(Locale.ENGLISH);
        // gc2 = gc;
        // if (gc2 != gc || !(gc2 == gc)) errln("FAIL: GregorianCalendar assignment/operator==/operator!= failed");
        // delete gc;
        // delete z;
    }

    // Verify Roger Webster's bug
    @Test
    public void TestRog() {
        GregorianCalendar gc = new GregorianCalendar();

        int year = 1997, month = Calendar.APRIL, date = 1;
        gc.set(year, month, date); // April 1, 1997

        gc.set(Calendar.HOUR_OF_DAY, 23);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < 9; i++, gc.add(Calendar.DATE, 1)) {
            if (gc.get(Calendar.YEAR) != year ||
                gc.get(Calendar.MONTH) != month ||
                gc.get(Calendar.DATE) != (date + i))
                errln("FAIL: Date " + gc.getTime() + " wrong");
        }
    }

    // Verify DAY_OF_WEEK
    @Test
    public void TestDOW943() {
        dowTest(false);
        dowTest(true);
    }

    void dowTest(boolean lenient) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(1997, Calendar.AUGUST, 12); // Wednesday
        cal.getTime(); // Force update
        cal.setLenient(lenient);
        cal.set(1996, Calendar.DECEMBER, 1); // Set the date to be December 1, 1996
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int min = cal.getMinimum(Calendar.DAY_OF_WEEK);
        int max = cal.getMaximum(Calendar.DAY_OF_WEEK);
        if (dow < min || dow > max) errln("FAIL: Day of week " + dow + " out of range");
        if (dow != Calendar.SUNDAY) {
            errln("FAIL2: Day of week should be SUNDAY; is " + dow + ": " + cal.getTime());
        }
        if (min != Calendar.SUNDAY || max != Calendar.SATURDAY) errln("FAIL: Min/max bad");
    }

    // Verify that the clone method produces distinct objects with no
    // unintentionally shared fields.
    @Test
    public void TestClonesUnique908() {
        Calendar c = Calendar.getInstance();
        Calendar d = (Calendar)c.clone();
        c.set(Calendar.MILLISECOND, 123);
        d.set(Calendar.MILLISECOND, 456);
        if (c.get(Calendar.MILLISECOND) != 123 ||
            d.get(Calendar.MILLISECOND) != 456) {
            errln("FAIL: Clones share fields");
        }
    }

    // Verify effect of Gregorian cutoff value
    @Test
    public void TestGregorianChange768() {
        boolean b;
        GregorianCalendar c = new GregorianCalendar();
        logln("With cutoff " + c.getGregorianChange());
        logln(" isLeapYear(1800) = " + (b=c.isLeapYear(1800)));
        logln(" (should be FALSE)");
        if (b != false) errln("FAIL");
        java.util.Calendar tempcal = java.util.Calendar.getInstance();
        tempcal.clear();
        tempcal.set(1900, 0, 1);
        c.setGregorianChange(tempcal.getTime()); // Jan 1 1900
        logln("With cutoff " + c.getGregorianChange());
        logln(" isLeapYear(1800) = " + (b=c.isLeapYear(1800)));
        logln(" (should be TRUE)");
        if (b != true) errln("FAIL");
    }

    // Test the correct behavior of the disambiguation algorithm.
    @Test
    public void TestDisambiguation765() throws Exception {
        Calendar c = Calendar.getInstance();
        c.setLenient(false);

        c.clear();
        c.set(Calendar.YEAR, 1997);
        c.set(Calendar.MONTH, Calendar.JUNE);
        c.set(Calendar.DATE, 3);

        verify765("1997 third day of June = ", c, 1997, Calendar.JUNE, 3);

        c.clear();
        c.set(Calendar.YEAR, 1997);
        c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        c.set(Calendar.MONTH, Calendar.JUNE);
        c.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        verify765("1997 first Tuesday in June = ", c, 1997, Calendar.JUNE, 3);

        c.clear();
        c.set(Calendar.YEAR, 1997);
        c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        c.set(Calendar.MONTH, Calendar.JUNE);
        c.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1);
        verify765("1997 last Tuesday in June = ", c, 1997, Calendar.JUNE, 24);

        IllegalArgumentException e = null;
        try {
            c.clear();
            c.set(Calendar.YEAR, 1997);
            c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            c.set(Calendar.MONTH, Calendar.JUNE);
            c.set(Calendar.DAY_OF_WEEK_IN_MONTH, 0);
            c.getTime();
        }
        catch (IllegalArgumentException ex) {
            e = ex;
        }
        verify765("1997 zero-th Tuesday in June = ", e, c);

        c.clear();
        c.set(Calendar.YEAR, 1997);
        c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        c.set(Calendar.MONTH, Calendar.JUNE);
        c.set(Calendar.WEEK_OF_MONTH, 1);
        verify765("1997 Tuesday in week 1 of June = ", c, 1997, Calendar.JUNE, 3);

        c.clear();
        c.set(Calendar.YEAR, 1997);
        c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        c.set(Calendar.MONTH, Calendar.JUNE);
        c.set(Calendar.WEEK_OF_MONTH, 5);
        verify765("1997 Tuesday in week 5 of June = ", c, 1997, Calendar.JULY, 1);

        try {
            c.clear();
            c.set(Calendar.YEAR, 1997);
            c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            c.set(Calendar.MONTH, Calendar.JUNE);
            c.set(Calendar.WEEK_OF_MONTH, 0);
            c.getTime();
        }
        catch (IllegalArgumentException ex) {
            e = ex;
        }
        verify765("1997 Tuesday in week 0 of June = ", e, c);

        c.clear();
        c.set(Calendar.YEAR, 1997);
        c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        c.set(Calendar.WEEK_OF_YEAR, 1);
        verify765("1997 Tuesday in week 1 of year = ", c, 1996, Calendar.DECEMBER, 31);

        c.clear();
        c.set(Calendar.YEAR, 1997);
        c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        c.set(Calendar.WEEK_OF_YEAR, 10);
        verify765("1997 Tuesday in week 10 of year = ", c, 1997, Calendar.MARCH, 4);

        e = null;
        try {
            c.clear();
            c.set(Calendar.YEAR, 1997);
            c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            c.set(Calendar.WEEK_OF_YEAR, 0);
            c.getTime();
        }
        catch (IllegalArgumentException ex) {
            e = ex;
        }
        verify765("1997 Tuesday in week 0 of year = ", e, c);
    }
    void verify765(String msg, Calendar c, int year, int month, int day) {
        int cy = c.get(Calendar.YEAR); // NEWCAL
        int cm = c.get(Calendar.MONTH);
        int cd = c.get(Calendar.DATE);
        if (cy == year &&
            cm == month &&
            cd == day) {
            logln("PASS: " + msg + c.getTime());
        }
        else {
            errln("FAIL: " + msg + cy + "/" + (cm+1) + "/" + cd +
                  "=" + c.getTime() +
                  "; expected " +
                  year + "/" + (month+1) + "/" + day);
        }
    }
    // Called when e expected to be non-null
    void verify765(String msg, IllegalArgumentException e, Calendar c) {
        if (e == null) errln("FAIL: No IllegalArgumentException for " + msg +
                             c.getTime());
        else logln("PASS: " + msg + "IllegalArgument as expected");
    }

    // Test the behavior of GMT vs. local time
    @Test
    public void TestGMTvsLocal4064654() {
        // Sample output 1:
        // % /usr/local/java/jdk1.1.3/solaris/bin/java test 1997 1 1 12 0 0
        // date = Wed Jan 01 04:00:00 PST 1997
        // offset for Wed Jan 01 04:00:00 PST 1997= -8hr
        aux4064654(1997, 1, 1, 12, 0, 0);

        // Sample output 2:
        // % /usr/local/java/jdk1.1.3/solaris/bin/java test 1997 4 16 18 30 0
        // date = Wed Apr 16 10:30:00 PDT 1997
        // offset for Wed Apr 16 10:30:00 PDT 1997= -7hr

        // Note that in sample output 2 according to the offset, the gmt time
        // of the result would be 1997 4 16 17 30 0 which is different from the
        // input of 1997 4 16 18 30 0.
        aux4064654(1997, 4, 16, 18, 30, 0);
    }
    void aux4064654(int yr, int mo, int dt, int hr, int mn, int sc) {
        Date date;
        Calendar gmtcal = Calendar.getInstance();
        gmtcal.setTimeZone(TimeZone.getTimeZone("Africa/Casablanca"));
        gmtcal.set(yr, mo-1, dt, hr, mn, sc);
        gmtcal.set(Calendar.MILLISECOND, 0);

        date = gmtcal.getTime();
        logln("date = "+date);

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angels"));
        cal.setTime(date);

        int offset = cal.getTimeZone().getOffset(cal.get(Calendar.ERA),
                                                 cal.get(Calendar.YEAR),
                                                 cal.get(Calendar.MONTH),
                                                 cal.get(Calendar.DATE),
                                                 cal.get(Calendar.DAY_OF_WEEK),
                                                 cal.get(Calendar.MILLISECOND));

        logln("offset for "+date+"= "+(offset/1000/60/60.0) + "hr");

        int utc = ((cal.get(Calendar.HOUR_OF_DAY) * 60 +
                    cal.get(Calendar.MINUTE)) * 60 +
                   cal.get(Calendar.SECOND)) * 1000 +
            cal.get(Calendar.MILLISECOND) - offset;

        int expected = ((hr * 60 + mn) * 60 + sc) * 1000;

        if (utc != expected)
            errln("FAIL: Discrepancy of " +
                  (utc - expected) + " millis = " +
                  ((utc-expected)/1000/60/60.0) + " hr");
    }

    // Verify that add and set work regardless of the order in which
    // they are called.
    @Test
    public void TestAddSetOrder621() {
        java.util.Calendar tempcal = java.util.Calendar.getInstance();
        tempcal.clear();
        tempcal.set(1997, 4, 14, 13, 23, 45);
        Date d = tempcal.getTime();

        Calendar cal = Calendar.getInstance ();
        cal.setTime (d);
        cal.add (Calendar.DATE, -5);
        cal.set (Calendar.HOUR_OF_DAY, 0);
        cal.set (Calendar.MINUTE, 0);
        cal.set (Calendar.SECOND, 0);
        // ma feb 03 00:00:00 GMT+00:00 1997
        String s = cal.getTime ().toString ();

        cal = Calendar.getInstance ();
        cal.setTime (d);
        cal.set (Calendar.HOUR_OF_DAY, 0);
        cal.set (Calendar.MINUTE, 0);
        cal.set (Calendar.SECOND, 0);
        cal.add (Calendar.DATE, -5);
        // ma feb 03 13:11:06 GMT+00:00 1997
        String s2 = cal.getTime ().toString ();

        if (s.equals(s2))
            logln("Pass: " + s + " == " + s2);
        else
            errln("FAIL: " + s + " != " + s2);
    }

    // Verify that add works.
    @Test
    public void TestAdd520() {
        int y = 1997, m = Calendar.FEBRUARY, d = 1;
        GregorianCalendar temp = new GregorianCalendar( y, m, d );
        check520(temp, y, m, d);

        temp.add( Calendar.YEAR, 1 );
        y++;
        check520(temp, y, m, d);

        temp.add( Calendar.MONTH, 1 );
        m++;
        check520(temp, y, m, d);

        temp.add( Calendar.DATE, 1 );
        d++;
        check520(temp, y, m, d);

        temp.add( Calendar.DATE, 2 );
        d += 2;
        check520(temp, y, m, d);

        temp.add( Calendar.DATE, 28 );
        d = 1; ++m;
        check520(temp, y, m, d);
    }
    void check520(Calendar c, int y, int m, int d) {
        if (c.get(Calendar.YEAR) != y ||
            c.get(Calendar.MONTH) != m ||
            c.get(Calendar.DATE) != d) {
            errln("FAILURE: Expected YEAR/MONTH/DATE of " +
                  y + "/" + (m+1) + "/" + d +
                  "; got " +
                  c.get(Calendar.YEAR) + "/" +
                  (c.get(Calendar.MONTH)+1) + "/" +
                  c.get(Calendar.DATE));
        }
        else logln("Confirmed: " +
                   y + "/" + (m+1) + "/" + d);
    }

    // Verify that add works across ZONE_OFFSET and DST_OFFSET transitions
    @Test
    public void TestAddAcrossOffsetTransitions() {
        class TransitionItem {
            private String zoneName;
            private int year;
            private int month;
            private int day;
            private int hour;
            TransitionItem(String zn, int y, int m, int d, int h) {
                zoneName = zn;
                year = y;
                month = m;
                day = d;
                hour = h;
            }
            public String getZoneName() { return zoneName; }
            public int getYear() { return year; }
            public int getMonth() { return month; }
            public int getDay() { return day; }
            public int getHour() { return hour; }
        }
        final TransitionItem[] transitionItems = { 
            new TransitionItem( "America/Caracas", 2007, Calendar.DECEMBER,  8, 10 ), // day before change in ZONE_OFFSET
            new TransitionItem( "US/Pacific",      2011,    Calendar.MARCH, 12, 10 ), // day before change in DST_OFFSET
        };
        for (TransitionItem transitionItem: transitionItems) {
            String zoneName = transitionItem.getZoneName();
            Calendar cal = null;
            try {
                cal = Calendar.getInstance(TimeZone.getTimeZone(zoneName), Locale.ENGLISH);
            } catch (Exception e) {
                errln("Error: Calendar.getInstance fails for zone " + zoneName);
                continue;
            }
            int itemHour = transitionItem.getHour();
            cal.set( transitionItem.getYear(), transitionItem.getMonth(), transitionItem.getDay(), itemHour, 0 );
            cal.add( Calendar.DATE, 1 );
            int hr = cal.get( Calendar.HOUR_OF_DAY );
            if ( hr != itemHour ) {
                errln("Error: Calendar.add produced wrong hour " + hr + " when adding day across transition for zone " + zoneName);
            } else {
                cal.add( Calendar.DATE, -1 );
                hr = cal.get( Calendar.HOUR_OF_DAY );
                if ( hr != itemHour ) {
                    errln("Error: Calendar.add produced wrong hour " + hr + " when subtracting day across transition for zone " + zoneName);
                }
            }
        }
    }

    // Verify that setting fields works.  This test fails when an exception is thrown.
    @Test
    public void TestFieldSet4781() {
        try {
            GregorianCalendar g = new GregorianCalendar();
            GregorianCalendar g2 = new GregorianCalendar();
            // At this point UTC value is set, various fields are not.
            // Now set to noon.
            g2.set(Calendar.HOUR, 12);
            g2.set(Calendar.MINUTE, 0);
            g2.set(Calendar.SECOND, 0);
            // At this point the object thinks UTC is NOT set, but fields are set.
            // The following line will result in IllegalArgumentException because
            // it thinks the YEAR is set and it is NOT.
            if (g2.equals(g))
                logln("Same");
            else
                logln("Different");
        }
        catch (IllegalArgumentException e) {
            errln("Unexpected exception seen: " + e);
        }
    }

    // Test serialization of a Calendar object
    @Test
    public void TestSerialize337() {
        Calendar cal = Calendar.getInstance();

        boolean ok = false;

        try {
            ByteArrayOutputStream f = new ByteArrayOutputStream();
            ObjectOutput s = new ObjectOutputStream(f);
            s.writeObject(PREFIX);
            s.writeObject(cal);
            s.writeObject(POSTFIX);
            f.close();

            ByteArrayInputStream in = new ByteArrayInputStream(f.toByteArray());
            ObjectInputStream t = new ObjectInputStream(in);
            String pre = (String)t.readObject();
            Calendar c = (Calendar)t.readObject();
            String post = (String)t.readObject();
            in.close();

            ok = pre.equals(PREFIX) &&
                post.equals(POSTFIX) &&
                cal.equals(c);
        }
        catch (IOException e) {
            errln("FAIL: Exception received:");
            // e.printStackTrace(log);
        }
        catch (ClassNotFoundException e) {
            errln("FAIL: Exception received:");
            // e.printStackTrace(log);
        }

        if (!ok) errln("Serialization of Calendar object failed.");
    }
    static final String PREFIX = "abc";
    static final String POSTFIX = "def";
    static final String FILENAME = "tmp337.bin";

    // Try to zero out the seconds field
    @Test
    public void TestSecondsZero121() {
        Calendar        cal = new GregorianCalendar();
        // Initialize with current date/time
        cal.setTime(new Date());
        // Round down to minute
        cal.set(Calendar.SECOND, 0);
        Date    d = cal.getTime();
        String s = d.toString();
        if (s.indexOf(":00 ") < 0) errln("Expected to see :00 in " + s);
    }

    // Try various sequences of add, set, and get method calls.
    @Test
    public void TestAddSetGet0610() {
        //
        // Error case 1:
        // - Upon initialization calendar fields, millis = System.currentTime
        // - After set is called fields are initialized, time is not
        // - Addition uses millis which are still *now*
        //
        {
            Calendar calendar = new GregorianCalendar( ) ;
            calendar.set( 1993, Calendar.JANUARY, 4 ) ;
            logln( "1A) " + value( calendar ) ) ;
            calendar.add( Calendar.DATE, 1 ) ;
            String v = value(calendar);
            logln( "1B) " + v );
            logln( "--) 1993/0/5" ) ;
            if (!v.equals(EXPECTED_0610)) errln("Expected " + EXPECTED_0610 +
                                                "; saw " + v);
        }

        //
        // Error case 2:
        // - Upon initialization calendar fields set, millis = 0
        // - Addition uses millis which are still 1970, 0, 1
        //

        {
            Calendar calendar = new GregorianCalendar( 1993, Calendar.JANUARY, 4 ) ;
            logln( "2A) " + value( calendar ) ) ;
            calendar.add( Calendar.DATE, 1 ) ;
            String v = value(calendar);
            logln( "2B) " + v );
            logln( "--) 1993/0/5" ) ;
            if (!v.equals(EXPECTED_0610)) errln("Expected " + EXPECTED_0610 +
                                                "; saw " + v);
        }

        //
        // Error case 3:
        // - Upon initialization calendar fields, millis = 0
        // - getTime( ) is called which forces the millis to be set
        // - Addition uses millis which are correct
        //

        {
            Calendar calendar = new GregorianCalendar( 1993, Calendar.JANUARY, 4 ) ;
            logln( "3A) " + value( calendar ) ) ;
            calendar.getTime( ) ;
            calendar.add( Calendar.DATE, 1 ) ;
            String v = value(calendar);
            logln( "3B) " + v ) ;
            logln( "--) 1993/0/5" ) ;
            if (!v.equals(EXPECTED_0610)) errln("Expected " + EXPECTED_0610 +
                                                "; saw " + v);
        }
    }
    static String value( Calendar calendar ) {
        return( calendar.get( Calendar.YEAR )  + "/" +
                calendar.get( Calendar.MONTH ) + "/" +
                calendar.get( Calendar.DATE ) ) ;
    }
    static String EXPECTED_0610 = "1993/0/5";

    // Test that certain fields on a certain date are as expected.
    @Test
    public void TestFields060() {
        int year = 1997;
        int month = java.util.Calendar.OCTOBER;  //october
        int dDate = 22;   //DAYOFWEEK should return 3 for Wednesday
        GregorianCalendar calendar = null;

        calendar = new GregorianCalendar( year, month, dDate);
        for (int i=0; i<EXPECTED_FIELDS.length; ) {
            int field = EXPECTED_FIELDS[i++];
            int expected = EXPECTED_FIELDS[i++];
            if (calendar.get(field) != expected) {
                errln("Expected field " + field + " to have value " + expected +
                      "; received " + calendar.get(field) + " instead");
            }
        }
    }
    static int EXPECTED_FIELDS[] = {
        Calendar.YEAR, 1997,
        Calendar.MONTH, Calendar.OCTOBER,
        Calendar.DAY_OF_MONTH, 22,
        Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY,
        Calendar.DAY_OF_WEEK_IN_MONTH, 4,
        Calendar.DAY_OF_YEAR, 295
    };

    // Verify that the fields are as expected (mostly zero) at the epoch start.
    // Note that we adjust for the default timezone to get most things to zero.
    @Test
    public void TestEpochStartFields() {
        TimeZone z = TimeZone.getDefault();
        Calendar c = Calendar.getInstance();
        Date d = new Date(-z.getRawOffset());
        if (z.inDaylightTime(d)) {
            logln("Warning: Skipping test because " + d +
                  " is in DST.");
        }
        else {
            c.setTime(d);
            c.setMinimalDaysInFirstWeek(1);
            for (int i=0; i<Calendar.ZONE_OFFSET; ++i) {
                if (c.get(i) != EPOCH_FIELDS[i])
                    errln("Expected field " + i + " to have value " + EPOCH_FIELDS[i] +
                          "; saw " + c.get(i) + " instead");
            }
            if (c.get(Calendar.ZONE_OFFSET) != z.getRawOffset())
                errln("Expected field ZONE_OFFSET to have value " + z.getRawOffset() +
                      "; saw " + c.get(Calendar.ZONE_OFFSET) + " instead");
            if (c.get(Calendar.DST_OFFSET) != 0)
                errln("Expected field DST_OFFSET to have value 0" +
                      "; saw " + c.get(Calendar.DST_OFFSET) + " instead");
        }
    }
    // These are the fields at the epoch start
    static int EPOCH_FIELDS[] = { 1, 1970, 0, 1, 1, 1, 1, 5, 1, 0, 0, 0, 0, 0, 0, -28800000, 0 };

    // Verify that as you add days to the calendar (e.g., 24 day periods),
    // the day of the week shifts in the expected pattern.
    @Test
    public void TestDOWProgression() {
        Calendar cal =
            new GregorianCalendar(1972, Calendar.OCTOBER, 26);
        marchByDelta(cal, 24); // Last parameter must be != 0 modulo 7
    }

    // Supply a delta which is not a multiple of 7.
    void marchByDelta(Calendar cal, int delta) {
        Calendar cur = (Calendar)cal.clone();
        int initialDOW = cur.get(Calendar.DAY_OF_WEEK);
        int DOW, newDOW = initialDOW;
        do {
            DOW = newDOW;
            logln("DOW = " + DOW + "  " + cur.getTime());

            cur.add(Calendar.DAY_OF_WEEK, delta);
            newDOW = cur.get(Calendar.DAY_OF_WEEK);
            int expectedDOW = 1 + (DOW + delta - 1) % 7;
            if (newDOW != expectedDOW) {
                errln("Day of week should be " + expectedDOW +
                      " instead of " + newDOW + " on " + cur.getTime());
                return;
            }
        }
        while (newDOW != initialDOW);
    }

    @Test
    public void TestActualMinMax() {
        Calendar cal = new GregorianCalendar(1967, Calendar.MARCH, 10);
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.setMinimalDaysInFirstWeek(3);

        if (cal.getActualMinimum(Calendar.DAY_OF_MONTH) != 1)
            errln("Actual minimum date for 3/10/1967 should have been 1; got " +
                  cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        if (cal.getActualMaximum(Calendar.DAY_OF_MONTH) != 31)
            errln("Actual maximum date for 3/10/1967 should have been 31; got " +
                  cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        if (cal.getActualMaximum(Calendar.DAY_OF_MONTH) != 28)
            errln("Actual maximum date for 2/10/1967 should have been 28; got " +
                  cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (cal.getActualMaximum(Calendar.DAY_OF_YEAR) != 365)
            errln("Number of days in 1967 should have been 365; got " +
                  cal.getActualMaximum(Calendar.DAY_OF_YEAR));

        cal.set(Calendar.YEAR, 1968);
        if (cal.getActualMaximum(Calendar.DAY_OF_MONTH) != 29)
            errln("Actual maximum date for 2/10/1968 should have been 29; got " +
                  cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (cal.getActualMaximum(Calendar.DAY_OF_YEAR) != 366)
            errln("Number of days in 1968 should have been 366; got " +
                  cal.getActualMaximum(Calendar.DAY_OF_YEAR));
        // Using week settings of SUNDAY/3 (see above)
        if (cal.getActualMaximum(Calendar.WEEK_OF_YEAR) != 52)
            errln("Number of weeks in 1968 should have been 52; got " +
                  cal.getActualMaximum(Calendar.WEEK_OF_YEAR));

        cal.set(Calendar.YEAR, 1976);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()); // Added - Liu 11/6/00
        // Using week settings of SUNDAY/3 (see above)
        if (cal.getActualMaximum(Calendar.WEEK_OF_YEAR) != 53)
            errln("Number of weeks in 1976 should have been 53; got " +
                  cal.getActualMaximum(Calendar.WEEK_OF_YEAR));
    }

    @Test
    public void TestRoll() {
        Calendar cal = new GregorianCalendar(1997, Calendar.JANUARY, 31);

        int[] dayValues = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 31 };
        for (int i = 0; i < dayValues.length; i++) {
            Calendar cal2 = (Calendar)cal.clone();
            cal2.roll(Calendar.MONTH, i);
            if (cal2.get(Calendar.DAY_OF_MONTH) != dayValues[i])
                errln("Rolling the month in 1/31/1997 up by " + i + " should have yielded "
                      + ((i + 1) % 12) + "/" + dayValues[i] + "/1997, but actually yielded "
                      + ((i + 1) % 12) + "/" + cal2.get(Calendar.DAY_OF_MONTH) + "/1997.");
        }

        cal.set(1996, Calendar.FEBRUARY, 29);

        //int[] monthValues = { 1, 2, 2, 2, 1, 2, 2, 2, 1, 2 };
        //int[] dayValues2 = { 29, 1, 1, 1, 29, 1, 1, 1, 29, 1 };

        // I've revised the expected values to make more sense -- rolling
        // the year should pin the DAY_OF_MONTH. - Liu 11/6/00
        int[] monthValues = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        int[] dayValues2 = { 29, 28, 28, 28, 29, 28, 28, 28, 29, 28 };

        for (int i = 0; i < dayValues2.length; i++) {
            Calendar cal2 = (Calendar)cal.clone();
            cal2.roll(Calendar.YEAR, i);
            if (cal2.get(Calendar.DAY_OF_MONTH) != dayValues2[i] || cal2.get(Calendar.MONTH)
                != monthValues[i])
                errln("Roll 2/29/1996 by " + i + " year: expected "
                      + (monthValues[i] + 1) + "/" + dayValues2[i] + "/"
                      + (1996 + i) + ", got "
                      + (cal2.get(Calendar.MONTH) + 1) + "/" +
                      cal2.get(Calendar.DAY_OF_MONTH) + "/" + cal2.get(Calendar.YEAR));
        }

        // Test rolling hour of day
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.roll(Calendar.HOUR_OF_DAY, -2);
        int f = cal.get(Calendar.HOUR_OF_DAY);
        if (f != 22) errln("Rolling HOUR_OF_DAY=0 delta=-2 gave " + f + " Wanted 22");
        cal.roll(Calendar.HOUR_OF_DAY, 5);
        f = cal.get(Calendar.HOUR_OF_DAY);
        if (f != 3) errln("Rolling HOUR_OF_DAY=22 delta=5 gave " + f + " Wanted 3");
        cal.roll(Calendar.HOUR_OF_DAY, 21);
        f = cal.get(Calendar.HOUR_OF_DAY);
        if (f != 0) errln("Rolling HOUR_OF_DAY=3 delta=21 gave " + f + " Wanted 0");

        // Test rolling hour
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.roll(Calendar.HOUR, -2);
        f = cal.get(Calendar.HOUR);
        if (f != 10) errln("Rolling HOUR=0 delta=-2 gave " + f + " Wanted 10");
        cal.roll(Calendar.HOUR, 5);
        f = cal.get(Calendar.HOUR);
        if (f != 3) errln("Rolling HOUR=10 delta=5 gave " + f + " Wanted 3");
        cal.roll(Calendar.HOUR, 9);
        f = cal.get(Calendar.HOUR);
        if (f != 0) errln("Rolling HOUR=3 delta=9 gave " + f + " Wanted 0");
    }

    @Test
    public void TestComputeJulianDay4406() {
        // jb4406 is probably not a bug, this is to document the behavior
        GregorianCalendar cal = new GregorianCalendar();
        final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
        
        logln("julian day value jumps at changeover");
        for (int day = 12; day < 18; ++day) {
            cal.set(1582, 9, day);
            logln("[" + day + "] " + (cal.getTimeInMillis()/MILLIS_IN_DAY));
        }

        logln("\njulian days not accurate before 1 March 0004");
        for (int day = 1; day < 3; ++day) {
            cal.set(1, 0, day);
            logln("[" + day + "] " + (cal.getTimeInMillis()/MILLIS_IN_DAY));
        }

        DateFormat fmt = cal.getDateTimeFormat(DateFormat.LONG, 0, Locale.getDefault());

        logln("\nswitchover in 1582");
        cal.set(1582, 9, 4);
        logln(fmt.format(cal));
        cal.add(Calendar.DATE, 1);
        logln(fmt.format(cal));
        cal.set(Calendar.JULIAN_DAY, 1721426);
        logln(fmt.format(cal));

        logln("\nlate switchover - proleptic Julian");
        cal.set(1582, 9, 4);
        cal.setGregorianChange(new Date(Long.MAX_VALUE));
        logln(fmt.format(cal));
        cal.add(Calendar.DATE, 1);
        logln(fmt.format(cal));
        cal.set(Calendar.JULIAN_DAY, 1721426);
        logln(fmt.format(cal));

        logln("\nearly switchover - proleptic Gregorian");
        cal.set(1582, 9, 4);
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        logln(fmt.format(cal));
        cal.add(Calendar.DATE, 1);
        logln(fmt.format(cal));
        cal.set(Calendar.JULIAN_DAY, 1721426);
        logln(fmt.format(cal));
    }
}

//eof
