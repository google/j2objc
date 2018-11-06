/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2002-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.calendar;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import android.icu.impl.LocaleUtility;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.JapaneseCalendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;

/**
 * Tests for the <code>JapaneseCalendar</code> class.
 */
public class JapaneseTest extends CalendarTestFmwk {
    
    @Test
    public void TestCoverage() {
        {
            // new JapaneseCalendar(TimeZone)
            JapaneseCalendar cal = new JapaneseCalendar(TimeZone.getDefault());
            if(cal == null){
                errln("could not create JapaneseCalendar with TimeZone");
            }
        }

        {
            // new JapaneseCalendar(ULocale)
            JapaneseCalendar cal = new JapaneseCalendar(ULocale.getDefault());
            if(cal == null){
                errln("could not create JapaneseCalendar with ULocale");
            }
        }

        {
            // new JapaneseCalendar(TimeZone, ULocale)
            JapaneseCalendar cal = new JapaneseCalendar(TimeZone.getDefault(), ULocale.getDefault());
            if(cal == null){
                errln("could not create JapaneseCalendar with TimeZone ULocale");
            }
        }

        {
            // new JapaneseCalendar(Locale)
            JapaneseCalendar cal = new JapaneseCalendar(Locale.getDefault());
            if(cal == null){
                errln("could not create JapaneseCalendar with Locale");
            }
        }

        {
            // new JapaneseCalendar(TimeZone, Locale)
            JapaneseCalendar cal = new JapaneseCalendar(TimeZone.getDefault(), Locale.getDefault());
            if(cal == null){
                errln("could not create JapaneseCalendar with TimeZone Locale");
            }
        }

        {
            // new JapaneseCalendar(Date)
            JapaneseCalendar cal = new JapaneseCalendar(new Date());
            if(cal == null){
                errln("could not create JapaneseCalendar with Date");
            }
        }

        {
            // new JapaneseCalendar(int year, int month, int date)
            JapaneseCalendar cal = new JapaneseCalendar(1868, Calendar.JANUARY, 1);
            if(cal == null){
                errln("could not create JapaneseCalendar with year,month,date");
            }
        }

        {
            // new JapaneseCalendar(int era, int year, int month, int date)
            JapaneseCalendar cal = new JapaneseCalendar(JapaneseCalendar.MEIJI, 43, Calendar.JANUARY, 1);
            if(cal == null){
                errln("could not create JapaneseCalendar with era,year,month,date");
            }
        }

        {
            // new JapaneseCalendar(int year, int month, int date, int hour, int minute, int second)
            JapaneseCalendar cal = new JapaneseCalendar(1868, Calendar.JANUARY, 1, 1, 1, 1);
            if(cal == null){
                errln("could not create JapaneseCalendar with year,month,date,hour,min,second");
            }
        }

        {
            // limits
            JapaneseCalendar cal = new JapaneseCalendar();
            DateFormat fmt = cal.getDateTimeFormat(DateFormat.FULL, DateFormat.FULL, Locale.ENGLISH);

            cal.set(Calendar.ERA, JapaneseCalendar.MEIJI);
            logln("date: " + cal.getTime());
            logln("min era: " + cal.getMinimum(Calendar.ERA));
            logln("min year: " + cal.getMinimum(Calendar.YEAR));
            cal.set(Calendar.YEAR, cal.getActualMaximum(Calendar.YEAR));
            logln("date: " + fmt.format(cal.getTime()));
            cal.add(Calendar.YEAR, 1);
            logln("date: " + fmt.format(cal.getTime()));
        }

        {
            // data
            JapaneseCalendar cal = new JapaneseCalendar(1868, Calendar.JANUARY, 1);
            Date time = cal.getTime();

            String[] calendarLocales = {
                    "en", "ja_JP"
            };

            String[] formatLocales = {
                    "en", "ja"
            };
            for (int i = 0; i < calendarLocales.length; ++i) {
                String calLocName = calendarLocales[i];
                Locale calLocale = LocaleUtility.getLocaleFromName(calLocName);
                cal = new JapaneseCalendar(calLocale);

                for (int j = 0; j < formatLocales.length; ++j) {
                    String locName = formatLocales[j];
                    Locale formatLocale = LocaleUtility.getLocaleFromName(locName);
                    DateFormat format = DateFormat.getDateTimeInstance(cal, DateFormat.FULL, DateFormat.FULL, formatLocale);
                    logln(calLocName + "/" + locName + " --> " + format.format(time));
                }
            }
        }
    }
    
    @Test
    public void Test3860()
    {
        ULocale loc = new ULocale("ja_JP@calendar=japanese");
        Calendar cal = new JapaneseCalendar(loc);
        DateFormat enjformat = cal.getDateTimeFormat(0,0,new ULocale("en_JP@calendar=japanese"));
        DateFormat format = cal.getDateTimeFormat(0,0,loc);
        ((SimpleDateFormat)format).applyPattern("y.M.d");  // Note: just 'y' doesn't work here.
        ParsePosition pos = new ParsePosition(0);
        Date aDate = format.parse("1.1.9", pos); // after the start of heisei accession.  Jan 1, 1H wouldn't work  because it is actually showa 64
        String inEn = enjformat.format(aDate);

        cal.clear();
        cal.setTime(aDate);
        int gotYear = cal.get(Calendar.YEAR);
        int gotEra = cal.get(Calendar.ERA);
        
        int expectYear = 1;
        int expectEra = JapaneseCalendar.CURRENT_ERA;
        
        if((gotYear != expectYear) || (gotEra != expectEra)) {
            errln("Expected year " + expectYear + ", era " + expectEra +", but got year " + gotYear + " and era " + gotEra + ", == " + inEn);
        } else {
            logln("Got year " + gotYear + " and era " + gotEra + ", == " + inEn);
        }

        // Test parse with missing era (should default to current era, heisei)
        // Test parse with incomplete information
        logln("Testing parse w/ just year...");
        Calendar cal2 = new JapaneseCalendar(loc);
        SimpleDateFormat fmt = new SimpleDateFormat("y", loc);
        SimpleDateFormat fmt2 = new SimpleDateFormat("HH:mm:ss.S MMMM d, yyyy G", new ULocale("en_US@calendar=gregorian"));
        cal2.clear();
        String samplestr = "1";
        logln("Test Year: " + samplestr);
        try {
            aDate = fmt.parse(samplestr);
        } catch (ParseException pe) {
            errln("Error parsing " + samplestr);
        }
        ParsePosition pp = new ParsePosition(0);
        fmt.parse(samplestr, cal2, pp);
        logln("cal2 after 1 parse:");
        String str = fmt2.format(aDate);
        logln("as Gregorian Calendar: " + str);

        cal2.setTime(aDate);
        gotYear = cal2.get(Calendar.YEAR);
        gotEra = cal2.get(Calendar.ERA);
        expectYear = 1;
        expectEra = JapaneseCalendar.CURRENT_ERA;
        if((gotYear != 1) || (gotEra != expectEra)) {
            errln("parse "+ samplestr + " of 'y' as Japanese Calendar, expected year " + expectYear + 
                " and era " + expectEra + ", but got year " + gotYear + " and era " + gotEra + " (Gregorian:" + str +")");
        } else {            
            logln(" year: " + gotYear + ", era: " + gotEra);
        }
    }

    @Test
    public void Test5345parse() {
        // Test parse with incomplete information
        DateFormat fmt2= DateFormat.getDateInstance(); //DateFormat.LONG, Locale.US);
        JapaneseCalendar c = new JapaneseCalendar(TimeZone.getDefault(), new ULocale("en_US"));
        SimpleDateFormat fmt = (SimpleDateFormat)c.getDateTimeFormat(1,1,new ULocale("en_US@calendar=japanese"));
        fmt.applyPattern("G y");
        logln("fmt's locale = " + fmt.getLocale(ULocale.ACTUAL_LOCALE));
        //SimpleDateFormat fmt = new SimpleDateFormat("G y", new Locale("en_US@calendar=japanese"));
        long aDateLong = -3197117222000L; // 1868-09-08 00:00 Pacific Time (GMT-07:52:58)
        if (TimeZone.getDefaultTimeZoneType() == TimeZone.TIMEZONE_JDK) {
            // Java time zone implementation does not support LMTs
            aDateLong = -3197116800000L; // 1868-09-08 00:00 Pacific Time (GMT-08:00)
        }
        Date aDate = new Date(aDateLong);
        logln("aDate: " + aDate.toString() +", from " + aDateLong);
        String str;
        str = fmt2.format(aDate);
        logln("Test Date: " + str);
        str = fmt.format(aDate);
        logln("as Japanese Calendar: " + str);
        String expected = "Meiji 1";
        if(!str.equals(expected)) {
            errln("FAIL: Expected " + expected + " but got " + str);
        }
        Date otherDate;
        try {
            otherDate = fmt.parse(expected);
            if(!otherDate.equals(aDate)) { 
                String str3;
    //            ParsePosition pp;
                Date dd = fmt.parse(expected);
                str3 = fmt.format(otherDate);
                long oLong = otherDate.getTime();
                long aLong = otherDate.getTime();
                
                errln("FAIL: Parse incorrect of " + expected + ":  wanted " + aDate + " ("+aLong+"), but got " +  " " +
                    otherDate + " ("+oLong+") = " + str3 + " not " + dd.toString() );


            } else {
                logln("Parsed OK: " + expected);
            }
        } catch(java.text.ParseException pe) {
            errln("FAIL: ParseException: " + pe.toString());
            pe.printStackTrace();
        }
    }


    private void checkExpected(Calendar c, int expected[] ) {
        final String[] FIELD_NAME = {
            "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH",
            "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK",
            "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY",
            "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET",
            "DST_OFFSET", "YEAR_WOY", "DOW_LOCAL", "EXTENDED_YEAR",
            "JULIAN_DAY", "MILLISECONDS_IN_DAY",
        };

        for(int i= 0;i<expected.length;i += 2) {
            int fieldNum = expected[i+0];
            int expectedVal = expected[i+1];
            int actualVal = c.get(fieldNum);
            
            if(expectedVal == actualVal) {
                logln(FIELD_NAME[fieldNum]+": "+ actualVal);
            } else {
                errln("FAIL: "+FIELD_NAME[fieldNum]+": expected "+ expectedVal + " got " +  actualVal);
            }
        }
    }
    
    @Test
    public void Test5345calendar() {
        logln("** testIncompleteCalendar()");
        // Test calendar with incomplete information
        JapaneseCalendar c = new JapaneseCalendar(TimeZone.getDefault());
        logln("test clear");
        c.clear();
 
        // Showa 45 = Gregorian 1970
        int expected0[] = {   Calendar.ERA, 234,
                              Calendar.YEAR, 45 };
        checkExpected(c, expected0);

        logln("test setting era");
        c.clear();
        c.set(Calendar.ERA, JapaneseCalendar.MEIJI);
        
        
        int expectedA[] = {   Calendar.ERA, JapaneseCalendar.MEIJI };
        checkExpected(c, expectedA);


        logln("test setting era and year and month and date");
        c.clear();
        c.set(Calendar.ERA, JapaneseCalendar.MEIJI);
        c.set(Calendar.YEAR, 1);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DATE, 1);


        int expectedC[] = {   Calendar.ERA, JapaneseCalendar.MEIJI -1};
        checkExpected(c, expectedC);


        logln("test setting  year and month and date THEN era");
        c.clear();
        c.set(Calendar.YEAR, 1);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DATE, 1);
        c.set(Calendar.ERA, JapaneseCalendar.MEIJI);
        
        
        checkExpected(c, expectedC);
        
        
        logln("test setting era and year");
        c.clear();
        c.set(Calendar.YEAR, 1);
        c.set(Calendar.ERA, JapaneseCalendar.MEIJI);


        int expectedB[] = { Calendar.ERA, JapaneseCalendar.MEIJI,
                            Calendar.YEAR, 1 };
        checkExpected(c, expectedB);

    }

    @Test
    public void TestJapaneseYear3282() {
        Calendar c = Calendar.getInstance(ULocale.ENGLISH);
        c.set(2003,Calendar.SEPTEMBER,25);
        JapaneseCalendar jcal = new JapaneseCalendar();
        //jcal.setTime(new Date(1187906308151L));  alternate value
        jcal.setTime(c.getTime());
        logln("Now is: " + jcal.getTime());
        c.setTime(jcal.getTime());
        int nowYear = c.get(Calendar.YEAR);
        logln("Now year: "+nowYear);
        SimpleDateFormat jdf = (SimpleDateFormat) SimpleDateFormat.getDateInstance(jcal,
                SimpleDateFormat.DEFAULT, Locale.getDefault());
        jdf.applyPattern("G yy/MM/dd");
        String text = jdf.format(jcal.getTime());
        logln("Now is: " + text + " (in Japan)");
        try {
            Date date = jdf.parse(text);
            logln("But is this not the date?: " + date);
            c.setTime(date);
            int thenYear = c.get(Calendar.YEAR);
            logln("Then year: "+thenYear);
            if(thenYear != nowYear) {
                errln("Nowyear "+nowYear +" is not thenyear "+thenYear);
            } else {
                logln("Nowyear "+nowYear +" == thenyear "+thenYear);
            }
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Test limits of the Japanese calendar
     */
    @Test
    public void TestLimits() {
        Calendar cal = Calendar.getInstance();
        cal.set(1988, Calendar.DECEMBER, 1);
        JapaneseCalendar jcal = new JapaneseCalendar();
        doLimitsTest(jcal, null, cal.getTime());
        doTheoreticalLimitsTest(jcal, true);
    }
}

