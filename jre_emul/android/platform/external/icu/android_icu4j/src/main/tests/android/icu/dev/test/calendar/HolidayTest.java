/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.calendar;

import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.LocaleUtility;
import android.icu.util.Calendar;
import android.icu.util.EasterHoliday;
import android.icu.util.GregorianCalendar;
import android.icu.util.Holiday;
import android.icu.util.RangeDateRule;
import android.icu.util.SimpleDateRule;
import android.icu.util.SimpleHoliday;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;

/**
 * Tests for the <code>Holiday</code> class.
 */
public class HolidayTest extends TestFmwk {
    @Before
    public void init() throws Exception {
        if(cal==null){
            cal = new GregorianCalendar(1, 0, 1);
            longTimeAgo = cal.getTime();
            now = new Date();
        }
    }
    
    private Calendar cal;
    private Date longTimeAgo;
    private Date now;
    private static long awhile = 3600L * 24 * 28; // 28 days

    @Test
    public void TestAPI() {
        {
            // getHolidays
            Holiday[] holidays = Holiday.getHolidays();
            exerciseHolidays(holidays, Locale.getDefault());
        }

        {
            // getHolidays(Locale)
            String[] localeNames =
            {
                "en_US",
                "da",
                "da_DK",
                "de",
                "de_AT",
                "de_DE",
                "el",
                "el_GR",
                "en",
                "en_CA",
                "en_GB",
                "es",
                "es_MX",
                "fr",
                "fr_CA",
                "fr_FR",
                "it",
                "it_IT",
                "iw",
                "iw_IL",
                "ja",
                "ja_JP",
            };

            for (int i = 0; i < localeNames.length; ++i) {
                Locale locale = LocaleUtility.getLocaleFromName(localeNames[i]);
                Holiday[] holidays = Holiday.getHolidays(locale);
                exerciseHolidays(holidays, locale);
            }
        }
    }

    void exerciseHolidays(Holiday[] holidays, Locale locale) {
        for (int i = 0; i < holidays.length; ++i) {
            exerciseHoliday(holidays[i], locale);
        }
    }

    void exerciseHoliday(Holiday h, Locale locale) {
        logln("holiday: " + h.getDisplayName());
        logln("holiday in " + locale + ": " + h.getDisplayName(locale));

        Date first = h.firstAfter(longTimeAgo);
        logln("firstAfter: " + longTimeAgo + " is " + first);
        if (first == null) {
            first = longTimeAgo;
        }
        first.setTime(first.getTime() + awhile);

        Date second = h.firstBetween(first, now);
        logln("firstBetween: " + first + " and " + now + " is " + second);
        if (second == null) {
            second = now;
        } else {
            if(second.after(now)) {
                errln("Error: "+h.getDisplayName()+".firstBetween("+first+", "+now+")="+second);
            }
        }

        logln("is on " + first + ": " + h.isOn(first));
        logln("is on " + now + ": " + h.isOn(now));
        logln(
              "is between "
              + first
              + " and "
              + now
              + ": "
              + h.isBetween(first, now));
        logln(
              "is between "
              + first
              + " and "
              + second
              + ": "
              + h.isBetween(first, second));

        //        logln("rule: " + h.getRule().toString());

        //        h.setRule(h.getRule());
        
        
        logln("HolidayCalendarDemo test");
        {
            final Calendar c = Calendar.getInstance(TimeZone.GMT_ZONE); // Temporary copy
            c.set(2014, 10, 8); // when this test was hit
            final Date fStartOfMonth = startOfMonth((Calendar)c.clone());

            // Stash away a few useful constants for this calendar and display
            //minDay = c.getMinimum(Calendar.DAY_OF_WEEK);
            //daysInWeek = c.getMaximum(Calendar.DAY_OF_WEEK) - minDay + 1;

            //firstDayOfWeek = Calendar.getInstance(fDisplayLocale).getFirstDayOfWeek();

            // Stash away a Date for the start of this month

            // Find the day of week of the first day in this month
            c.setTime(fStartOfMonth);
            //firstDayInMonth = c.get(Calendar.DAY_OF_WEEK);

            // Now find the # of days in the month
            c.roll(Calendar.DATE, false);
            // final int daysInMonth = c.get(Calendar.DATE);

            // Finally, find the end of the month, i.e. the start of the next one
            c.roll(Calendar.DATE, true);
            c.add(Calendar.MONTH, 1);
            c.getTime();        // JDK 1.1.2 bug workaround
            c.add(Calendar.SECOND, -1);
            Date endOfMonth = c.getTime();

            //
            // Calculate the number of full or partial weeks in this month.
            // To do this I can just reuse the code that calculates which
            // calendar cell contains a given date.
            //
            //numWeeks = dateToCell(daysInMonth).y - dateToCell(1).y + 1;

            // Remember which holidays fall on which days in this month,
            // to save the trouble of having to do it later
            //fHolidays.setSize(0);
            int patience=100;

            //for (int h = 0; h < fAllHolidays.length; h++)
            {
                Date d = fStartOfMonth;
                while ( (d = h.firstBetween(d, endOfMonth) ) != null)
                {
                    if(--patience <= 0) {
                        errln("Patience exceeded for " + h.getDisplayName() +" at " + d);
                        break;
                    }
                    if(d.after(endOfMonth)) {
                        errln("Error: for " + h.getDisplayName()+": " + d +" is after end of month " + endOfMonth);
                        break;
                    }
                    c.setTime(d);
                    logln("New date: " + d);
//                    fHolidays.addElement( new HolidayInfo(c.get(Calendar.DATE),
//                                            fAllHolidays[h],
//                                            fAllHolidays[h].getDisplayName(fDisplayLocale) ));

                    d.setTime( d.getTime() + 1000 );    // "d++"
                }
            }
//            dirty = false;
        }
        // end HolidayDemoApplet code

    }

    // from HolidayCalendarDemo
    private Date startOfMonth(/*Date dateInMonth,*/ Calendar fCalendar)
    {
        //synchronized(fCalendar) {
        //    fCalendar.setTime(dateInMonth);             // TODO: synchronization

            int era = fCalendar.get(Calendar.ERA);
            int year = fCalendar.get(Calendar.YEAR);
            int month = fCalendar.get(Calendar.MONTH);

            fCalendar.clear();
            fCalendar.set(Calendar.ERA, era);
            fCalendar.set(Calendar.YEAR, year);
            fCalendar.set(Calendar.MONTH, month);
            fCalendar.set(Calendar.DATE, 1);

            return fCalendar.getTime();
       // }
    }

    @Test
    public void TestCoverage(){
        Holiday[] h = { new EasterHoliday("Ram's Easter"),
                        new SimpleHoliday(2, 29, 0, "Leap year", 1900, 2100)};
        exerciseHolidays(h, Locale.getDefault());

        RangeDateRule rdr = new RangeDateRule();
        rdr.add(new SimpleDateRule(7, 10));
        Date mbd = getDate(1953, Calendar.JULY, 10);
        Date dbd = getDate(1958, Calendar.AUGUST, 15);
        Date nbd = getDate(1990, Calendar.DECEMBER, 17);
        Date abd = getDate(1992, Calendar.SEPTEMBER, 16);
        Date xbd = getDate(1976, Calendar.JULY, 4);
        Date ybd = getDate(2003, Calendar.DECEMBER, 8);
        rdr.add(new SimpleDateRule(Calendar.JULY, 10, Calendar.MONDAY, false));
        rdr.add(dbd, new SimpleDateRule(Calendar.AUGUST, 15, Calendar.WEDNESDAY, true));
        rdr.add(xbd, null);
        rdr.add(nbd, new SimpleDateRule(Calendar.DECEMBER, 17, Calendar.MONDAY, false));
        rdr.add(ybd, null);

        logln("first after " + mbd + " is " + rdr.firstAfter(mbd));
        logln("first between " + mbd + " and " + dbd + " is " + rdr.firstBetween(mbd, dbd));
        logln("first between " + dbd + " and " + nbd + " is " + rdr.firstBetween(dbd, nbd));
        logln("first between " + nbd + " and " + abd + " is " + rdr.firstBetween(nbd, abd));
        logln("first between " + abd + " and " + xbd + " is " + rdr.firstBetween(abd, xbd));
        logln("first between " + abd + " and " + null + " is " + rdr.firstBetween(abd, null));
        logln("first between " + xbd + " and " + null + " is " + rdr.firstBetween(xbd, null));
        
        //getRule, setRule
        logln("The rule in the holiday: " + h[1].getRule());
        exerciseHoliday(h[1], Locale.getDefault());
        h[1].setRule(rdr);
        logln("Set the new rule to the SimpleHoliday ...");
        if (!rdr.equals(h[1].getRule())) {
            errln("FAIL: getRule and setRule not matched.");
        }
        exerciseHoliday(h[1], Locale.getDefault());
    }
    
    @Test
    public void TestEaster(){        
        // Verify that Easter is working. Should be April 20, 2014
        final Holiday h = new EasterHoliday("Easter Sunday");
        final Date beginApril = getDate(2014, Calendar.APRIL, 1);
        final Date endApril   = getDate(2014, Calendar.APRIL, 30);
        final Date expect     = getDate(2014, Calendar.APRIL, 20);
        final Date actual     = h.firstBetween(beginApril, endApril);
        
        if(actual == null) {
            errln("Error: Easter 2014 should be on " + expect + " but got null.");
        } else {
            Calendar c = Calendar.getInstance(TimeZone.GMT_ZONE, Locale.US);
            c.setTime(actual);
            assertEquals("Easter's year:  ", 2014, c.get(Calendar.YEAR));
            assertEquals("Easter's month: ", Calendar.APRIL, c.get(Calendar.MONTH));
            assertEquals("Easter's date:  ", 20, c.get(Calendar.DATE));
        }
    }

    @Test
    public void TestIsOn() {
        // jb 1901
        SimpleHoliday sh = new SimpleHoliday(Calendar.AUGUST, 15, "Doug's Day", 1958, 2058);
        
        Calendar gcal = new GregorianCalendar();
        gcal.clear();
        gcal.set(Calendar.YEAR, 2000);
        gcal.set(Calendar.MONTH, Calendar.AUGUST);
        gcal.set(Calendar.DAY_OF_MONTH, 15);
        
        Date d0 = gcal.getTime();
        gcal.add(Calendar.SECOND, 1);
        Date d1 = gcal.getTime();
        gcal.add(Calendar.SECOND, -2);
        Date d2 = gcal.getTime();
        gcal.add(Calendar.DAY_OF_MONTH, 1);
        Date d3 = gcal.getTime();
        gcal.add(Calendar.SECOND, 1);
        Date d4 = gcal.getTime();
        gcal.add(Calendar.SECOND, -2);
        gcal.set(Calendar.YEAR, 1957);
        Date d5 = gcal.getTime();
        gcal.set(Calendar.YEAR, 1958);
        Date d6 = gcal.getTime();
        gcal.set(Calendar.YEAR, 2058);
        Date d7 = gcal.getTime();
        gcal.set(Calendar.YEAR, 2059);
        Date d8 = gcal.getTime();

        Date[] dates = { d0, d1, d2, d3, d4, d5, d6, d7, d8 };
        boolean[] isOns = { true, true, false, true, false, false, true, true, false };
        for (int i = 0; i < dates.length; ++i) {
            Date d = dates[i];
            logln("\ndate: " + d);
            boolean isOn = sh.isOn(d);
            logln("isOnDate: " + isOn);
            if (isOn != isOns[i]) {
                errln("date: " + d + " should be on Doug's Day!");
            }
            Date h = sh.firstAfter(d);
            logln("firstAfter: " + h);
        }
    }
    
    @Test
    public void TestDisplayName() {
        Holiday[] holidays = Holiday.getHolidays(ULocale.US);
        for (int i = 0; i < holidays.length; ++i) {
            Holiday h = holidays[i];
            // only need to test one
            // if the display names differ, we're using our data.  We know these names
            // should differ for this holiday (not all will).
            if ("Christmas".equals(h.getDisplayName(ULocale.US))) {
                if ("Christmas".equals(h.getDisplayName(ULocale.GERMANY))) {
                    errln("Using default name for holidays");
                }
            }
        }
    }
}
