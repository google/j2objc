/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2005-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.calendar;

import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import android.icu.impl.LocaleUtility;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.IndianCalendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;

/**
 * Tests for the <code>IndianCalendar</code> class.
 */
public class IndianTest extends CalendarTestFmwk 
{
    // Months in indian calendar are 0-based. Here taking 1-based names:
    public static final int CHAITRA = IndianCalendar.CHAITRA + 1; 
    public static final int VAISAKHA = IndianCalendar.VAISAKHA + 1;
    public static final int JYAISTHA = IndianCalendar.JYAISTHA + 1;
    public static final int ASADHA = IndianCalendar.ASADHA + 1;
    public static final int SRAVANA = IndianCalendar.SRAVANA + 1 ;
    public static final int BHADRA = IndianCalendar.BHADRA + 1 ;
    public static final int  ASVINA = IndianCalendar.ASVINA  + 1 ;
    public static final int KARTIKA = IndianCalendar.KARTIKA + 1 ;
    public static final int AGRAHAYANA = IndianCalendar.AGRAHAYANA + 1 ;
    public static final int PAUSA = IndianCalendar.PAUSA + 1 ;
    public static final int MAGHA = IndianCalendar.MAGHA + 1 ;
    public static final int PHALGUNA = IndianCalendar.PHALGUNA + 1 ;

    /** Constants to save typing. */
    /* Test dates generated from:
     * http://www.fourmilab.ch/documents/calendar/ 
    
    /** A huge list of test cases to make sure that computeTime and computeFields
     * work properly for a wide range of data in the Indian civil calendar.
     */
    @Test
    public void TestCases()
    {
        final TestCase[] tests = {
            //
            // The months in this table are 1-based rather than 0-based,
            // because it's easier to edit that way.
            //                      Indian
            //          Julian Day  Era  Year   Month Day    WkDay Hour Min Sec
           new TestCase(1770641.5,  0,    57,    ASVINA,       10,  SUN,   0,  0,  0),
           new TestCase(1892731.5,  0,   391,    PAUSA,        18,  WED,   0,  0,  0),
           new TestCase(1931579.5,  0,   498,    VAISAKHA,     30,  MON,   0,  0,  0),
           new TestCase(1974851.5,  0,   616,    KARTIKA,      19,  SAT,   0,  0,  0),
           new TestCase(2091164.5,  0,   935,    VAISAKHA,      5,  SUN,   0,  0,  0),
           new TestCase(2121509.5,  0,  1018,    JYAISTHA,      3,  SUN,   0,  0,  0),
           new TestCase(2155779.5,  0,  1112,    CHAITRA,       2,  FRI,   0,  0,  0),
           new TestCase(2174029.5,  0,  1161,    PHALGUNA,     20,  SAT,   0,  0,  0),
           new TestCase(2191584.5,  0,  1210,    CHAITRA,      13,  FRI,   0,  0,  0),
           new TestCase(2195261.5,  0,  1220,    VAISAKHA,      7,  SUN,   0,  0,  0),
           new TestCase(2229274.5,  0,  1313,    JYAISTHA,     22,  SUN,   0,  0,  0),
           new TestCase(2245580.5,  0,  1357,    MAGHA,        14,  WED,   0,  0,  0),
           new TestCase(2266100.5,  0,  1414,    CHAITRA,      20,  SAT,   0,  0,  0),
           new TestCase(2288542.5,  0,  1475,    BHADRA,       28,  SAT,   0,  0,  0),
           new TestCase(2290901.5,  0,  1481,    PHALGUNA,     15,  SAT,   0,  0,  0),
           new TestCase(2323140.5,  0,  1570,    JYAISTHA,     20,  WED,   0,  0,  0),
           new TestCase(2334551.5,  0,  1601,    BHADRA,       16,  THU,   0,  0,  0),
           new TestCase(2334581.5,  0,  1601,    ASVINA,       15,  SAT,   0,  0,  0),
           new TestCase(2334610.5,  0,  1601,    KARTIKA,      14,  SUN,   0,  0,  0),
           new TestCase(2334639.5,  0,  1601,    AGRAHAYANA,   13,  MON,   0,  0,  0),
           new TestCase(2334668.5,  0,  1601,    PAUSA,        12,  TUE,   0,  0,  0),
           new TestCase(2334698.5,  0,  1601,    MAGHA,        12,  THU,   0,  0,  0),
           new TestCase(2334728.5,  0,  1601,    PHALGUNA,     12,  SAT,   0,  0,  0),
           new TestCase(2334757.5,  0,  1602,    CHAITRA,      11,  SUN,   0,  0,  0),
           new TestCase(2334787.5,  0,  1602,    VAISAKHA,     10,  TUE,   0,  0,  0),
           new TestCase(2334816.5,  0,  1602,    JYAISTHA,      8,  WED,   0,  0,  0),
           new TestCase(2334846.5,  0,  1602,    ASADHA,        7,  FRI,   0,  0,  0),
           new TestCase(2334848.5,  0,  1602,    ASADHA,        9,  SUN,   0,  0,  0),
           new TestCase(2348020.5,  0,  1638,    SRAVANA,       2,  FRI,   0,  0,  0),
           new TestCase(2334934.5,  0,  1602,    ASVINA,        2,  TUE,   0,  0,  0),
           new TestCase(2366978.5,  0,  1690,    JYAISTHA,     29,  SUN,   0,  0,  0),
           new TestCase(2385648.5,  0,  1741,    SRAVANA,      11,  MON,   0,  0,  0),
           new TestCase(2392825.5,  0,  1761,    CHAITRA,       6,  WED,   0,  0,  0),
           new TestCase(2416223.5,  0,  1825,    CHAITRA,      29,  SUN,   0,  0,  0),
           new TestCase(2425848.5,  0,  1851,    BHADRA,        3,  SUN,   0,  0,  0),
           new TestCase(2430266.5,  0,  1863,    ASVINA,        7,  MON,   0,  0,  0),
           new TestCase(2430833.5,  0,  1865,    CHAITRA,      29,  MON,   0,  0,  0),
           new TestCase(2431004.5,  0,  1865,    ASVINA,       15,  THU,   0,  0,  0),
           new TestCase(2448698.5,  0,  1913,    PHALGUNA,     27,  TUE,   0,  0,  0),
           new TestCase(2450138.5,  0,  1917,    PHALGUNA,      6,  SUN,   0,  0,  0),
           new TestCase(2465737.5,  0,  1960,    KARTIKA,      19,  WED,   0,  0,  0),
           new TestCase(2486076.5,  0,  2016,    ASADHA,       27,  SUN,   0,  0,  0),
        };
        
        IndianCalendar testCalendar = new IndianCalendar();
        testCalendar.setLenient(true);
        doTestCases(tests, testCalendar);
    }

    @Test
    public void TestBasic() {
        IndianCalendar cal = new IndianCalendar();
        cal.clear();
        cal.set(1000, 0, 30);
        logln("1000/0/30-> " +
              cal.get(YEAR) + "/" +
              cal.get(MONTH) + "/" + 
              cal.get(DATE));
        cal.clear();
        cal.set(1, 0, 30);
        logln("1/0/30 -> " +
              cal.get(YEAR) + "/" +
              cal.get(MONTH) + "/" + 
              cal.get(DATE));
    }

    @Test
    public void TestCoverage() {
        {
            // new IndianCalendar(TimeZone)
            IndianCalendar cal = new IndianCalendar(TimeZone.getDefault()); 
            if(cal == null){
                errln("could not create IndianCalendar with TimeZone");
            }
        }

        {
            // new IndianCalendar(ULocale)
            IndianCalendar cal = new IndianCalendar(ULocale.getDefault());
            if(cal == null){
                errln("could not create IndianCalendar with ULocale");
            }
        }
        
        {
            // new IndianCalendar(Locale)
            IndianCalendar cal = new IndianCalendar(Locale.getDefault());
            if(cal == null){
                errln("could not create IndianCalendar with Locale");
            }
        }
        
        {                                                                                       
            // new IndianCalendar(TimeZone, Locale)                                             
            IndianCalendar cal = new IndianCalendar(TimeZone.getDefault(),Locale.getDefault()); 
            if(cal == null){                                                                    
                errln("could not create IndianCalendar with TimeZone, Locale");                 
            }                                                                                   
        }                                                                                       
                                                                                                
        {                                                                                       
            // new IndianCalendar(TimeZone, ULocale)                                            
            IndianCalendar cal = new IndianCalendar(TimeZone.getDefault(),ULocale.getDefault());
            if(cal == null){                                                                    
                errln("could not create IndianCalendar with TimeZone, ULocale");                
            }                                                                                   
        }                                                                                       
        
        {
            // new IndianCalendar(Date)
            IndianCalendar cal = new IndianCalendar(new Date());
            if(cal == null){
                errln("could not create IndianCalendar with Date");
            }
        }
        {
            // new IndianCalendar(int year, int month, int date)
            IndianCalendar cal = new IndianCalendar(1917, IndianCalendar.CHAITRA, 1);
            if(cal == null){
                errln("could not create IndianCalendar with year,month,date");
            }
        }
        {
            // new IndianCalendar(int year, int month, int date, int hour, int minute, int second)
            IndianCalendar cal = new IndianCalendar(1917, IndianCalendar.CHAITRA, 1, 1, 1, 1);
            if(cal == null){
                errln("could not create IndianCalendar with year,month,date,hour,minute,second");
            }
        }
    
        {
            // data
            String[] calendarLocales = {
                "bn_IN", "gu_IN", "hi_IN", "kn_IN", "ml_IN", "or_IN", "pa_IN", "ta_IN", "te_IN"
            };

            String[] formatLocales = {
                "en", "fr", "bn", "gu", "hi", "kn", "ml", "or", "pa", "ta", "te"
            };

            for (int i = 0; i < calendarLocales.length; ++i) {
                String calLocName = calendarLocales[i];
                Locale calLocale = LocaleUtility.getLocaleFromName(calLocName);
                IndianCalendar cal = new IndianCalendar(calLocale);
                cal.set(-1039, 9, 21);

                for (int j = 0; j < formatLocales.length; j++  ) {
                    String locName = formatLocales[j];
                    Locale formatLocale = LocaleUtility.getLocaleFromName(locName);
                    DateFormat format = DateFormat.getDateTimeInstance(cal, DateFormat.FULL, DateFormat.FULL, formatLocale);
                    logln(calLocName + "/" + locName + " --> " + format.format(cal));
                }
            }
        }
    }

    @Test
    public void TestYear() {
        // Gregorian Calendar
        Calendar gCal= new GregorianCalendar();
        Date gToday=gCal.getTime();
        gCal.add(GregorianCalendar.MONTH,2);
        Date gFuture=gCal.getTime();
        DateFormat gDF = DateFormat.getDateInstance(gCal,DateFormat.FULL);
        logln("gregorian calendar: " + gDF.format(gToday) +
              " + 2 months = " + gDF.format(gFuture));

        // Indian Calendar
        IndianCalendar iCal= new IndianCalendar();
        Date iToday=iCal.getTime();
        iCal.add(IndianCalendar.MONTH,2);
        Date iFuture=iCal.getTime();
        DateFormat iDF = DateFormat.getDateInstance(iCal,DateFormat.FULL);
        logln("Indian calendar: " + iDF.format(iToday) +
              " + 2 months = " + iDF.format(iFuture));

    }

    /**
     * Test limits of the Indian calendar
     */
    @Test
    public void TestLimits() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 1);
        IndianCalendar indian = new IndianCalendar();
        doLimitsTest(indian, null, cal.getTime());
        doTheoreticalLimitsTest(indian, true);
    }
    
    /**
     * Problem reported by Bruno Haible <bruno.haible@de.ibm.com>
     *  -- see ticket 8419 -- http://bugs.icu-project.org/trac/ticket/8419
     * Problem with months out of range 0-11
     */
    @Test
    public void TestYearEdge() {
        // Display dates in ISO 8601 format.
        DateFormat fmt = new SimpleDateFormat("YYYY-MM-dd", ULocale.US);

        // Instantiate an Indian calendar.
        ULocale locale = ULocale.US.setKeywordValue("calendar", "indian");
        Calendar cal = Calendar.getInstance(locale);

        // Try add() repeatedly.
        cal.setTimeInMillis(1295568000000L);
        if (!fmt.format(cal.getTime()).equals("2011-01-20")){
            errln("Incorrect calendar value for year edge test");
        }
        cal.add(Calendar.MONTH, 1);
        if (!fmt.format(cal.getTime()).equals("2011-02-19")){
            errln("Incorrect calendar value for year edge test");
        }
        cal.add(Calendar.MONTH, 1);
        if (!fmt.format(cal.getTime()).equals("2011-03-21")){
            errln("Incorrect calendar value for year edge test");
        }
        cal.add(Calendar.MONTH, 1);
        if (!fmt.format(cal.getTime()).equals("2011-04-20")){
            errln("Incorrect calendar value for year edge test");
        }
    }

    public void TestCoverage12424() {
        class StubCalendar extends IndianCalendar {   
            private static final long serialVersionUID = 1L;
            public StubCalendar() {
                assertEquals("Indian month 0 length", 30, handleGetMonthLength(1000, 0));
                assertEquals("Indian month 2 length", 31, handleGetMonthLength(1000, 2));
             }
        }
        
        new StubCalendar();
    }
}
