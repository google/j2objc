/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2005-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
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
import android.icu.util.EthiopicCalendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;

/**
 * Tests for the <code>EthiopicCalendar</code> class.
 */
public class EthiopicTest extends CalendarTestFmwk 
{
    /** Constants to save typing. */
    public static final int MESKEREM = EthiopicCalendar.MESKEREM;
    public static final int TEKEMT   = EthiopicCalendar.TEKEMT;
    public static final int HEDAR    = EthiopicCalendar.HEDAR;
    public static final int TAHSAS   = EthiopicCalendar.TAHSAS;
    public static final int TER      = EthiopicCalendar.TER;
    public static final int YEKATIT  = EthiopicCalendar.YEKATIT;
    public static final int MEGABIT  = EthiopicCalendar.MEGABIT;
    public static final int MIAZIA   = EthiopicCalendar.MIAZIA;
    public static final int GENBOT   = EthiopicCalendar.GENBOT;
    public static final int SENE     = EthiopicCalendar.SENE;
    public static final int HAMLE    = EthiopicCalendar.HAMLE;
    public static final int NEHASSE  = EthiopicCalendar.NEHASSE;
    public static final int PAGUMEN  = EthiopicCalendar.PAGUMEN;

    /* DY[20050507]  I don't know what this is for yet: 
    @Test
    public void TestRoll() {
       int[][] tests = new int[][] {
       //       input                roll by          output
       //  year  month     day     field amount    year  month     day
    
       {   0001, QIDAH,     2,     MONTH,   1,     0001, HIJJAH,    2 },   // non-leap years
       {   0001, QIDAH,     2,     MONTH,   2,     0001, MUHARRAM,  2 },
       {   0001, QIDAH,     2,     MONTH,  -1,     0001, SHAWWAL,   2 },
       {   0001, MUHARRAM,  2,     MONTH,  12,     0001, MUHARRAM,  2 },
       {   0001, MUHARRAM,  2,     MONTH,  13,     0001, SAFAR,     2 },

       {   0001, HIJJAH,    1,     DATE,   30,     0001, HIJJAH,    2 },   // 29-day month
       {   0002, HIJJAH,    1,     DATE,   31,     0002, HIJJAH,    2 },   // 30-day month

       // Try some rolls that require other fields to be adjusted
       {   0001, MUHARRAM, 30,     MONTH,   1,     0001, SAFAR,    29 },
       {   0002, HIJJAH,   30,     YEAR,   -1,     0001, HIJJAH,   29 },
       };
       
       EthiopicCalendar cal = newCivil();

       doRollAdd(ROLL, cal, tests);
       }
    */

    /* Test dates from:
     * "The Amharic Letters of Emperor Theodore of Ethiopia to Queen Victoria and
     * Her Special Envoy", David Appleyard, Girma Selasse Asfaw, Oxford University Press, 
     * June 1 1979, ISBN: 0856726605, Longwood Pr Ltd
     *  
     * Ethiopic       Gregorian    JD
     * 20/02/1855     29/10/1862  2401443
     * 29/10/1857     05/07/1865  2402423
     * 22/05/1858     29/01/1866  2402631
     * 10/08/1858     17/04/1866  2402709
     * 28/04/1859     05/01/1867  2402972
     * 05/05/1860     13/01/1868  2403345
     * 
     * --------------------------------------------------
     * 
     * From the Calendrica applet:  http://emr.cs.iit.edu/home/reingold/calendar-book/Calendrica.html
     * 
     * Ethiopic       Gregorian    JD
     * 07/05/-8       01/01/0000  1721060
     * 08/05/-7       01/01/0001  1721426
     * 06/13/-1       27/08/0007  1723855
     * 
     * 01/01/0000     28/08/0007  1723856
     * 01/01/0001     27/08/0008  1724221
     * 01/01/0002     27/08/0009  1724586
     * 01/01/0003     27/08/0010  1724951
     * 01/01/0004     28/08/0011  1724537
     * 05/13/0000     26/08/0008  1724220
     * 05/13/0001     26/08/0009  1724585
     * 05/13/0002     26/08/0010  1724950
     * 05/13/0003     26/08/0011  1725315
     * 06/13/0003     27/08/0011  1725316  first ethiopian leap year
     * 05/13/0004     26/08/0012  1725561
     * 
     * 06/02/1575     13/10/1582  2299159
     * 07/02/1575     14/10/1582  2299160  Julian 04/10/1582
     * 08/02/1575     15/10/1582  2299161
     * 09/02/1575     16/10/1582  2299162
     * 
     * 23/04/1892     01/01/1900  2415021
     * 23/04/1997     01/01/2005  2453372
     * 05/13/2000     10/09/2008  2454720
     */
    
    /** A huge list of test cases to make sure that computeTime and computeFields
     * work properly for a wide range of data in the civil calendar.
     */
    @Test
    public void TestCases()
    {
        final TestCase[] tests = {
            //
            // The months in this table are 1-based rather than 0-based,
            // because it's easier to edit that way.
            //                      Ethiopic
            //          Julian Day  Era  Year  Month Day  WkDay Hour Min Sec
            //
            // Dates from "Emporer Theodore..."

            new TestCase(2401442.5,  1,  1855,    2,  20,  WED,   0,  0,  0), // Gregorian: 29/10/1862
            new TestCase(2402422.5,  1,  1857,   10,  29,  WED,   0,  0,  0), // Gregorian: 05/07/1865
            new TestCase(2402630.5,  1,  1858,    5,  22,  MON,   0,  0,  0), // Gregorian: 29/01/1866
            new TestCase(2402708.5,  1,  1858,    8,  10,  TUE,   0,  0,  0), // Gregorian: 17/04/1866
            new TestCase(2402971.5,  1,  1859,    4,  28,  SAT,   0,  0,  0), // Gregorian: 05/01/1867
            new TestCase(2403344.5,  1,  1860,    5,   5,  MON,   0,  0,  0), // Gregorian: 13/01/1868
                        
            // Miscellaneous:
            /* Skip these tests until JD bug fixed in the Gregorian calendar:
             * http://www.jtcsv.com/cgibin/icu-bugs/incoming?id=4406;page=2;user=guest
             */
            new TestCase(1721059.5,  0,  5492,    5,   7,  SAT,   0,  0,  0), // Gregorian: 01/01/0000
            new TestCase(1721425.5,  0,  5493,    5,   8,  MON,   0,  0,  0), // Gregorian: 01/01/0001
            new TestCase(1723854.5,  0,  5499,   13,   6,  MON,   0,  0,  0), // Gregorian: 27/08/0007

            new TestCase(1723855.5,  0,  5500,    1,   1,  TUE,   0,  0,  0), // Gregorian: 28/08/0007
            new TestCase(1724220.5,  1,     1,    1,   1,  WED,   0,  0,  0), // Gregorian: 27/08/0008
            new TestCase(1724585.5,  1,     2,    1,   1,  THU,   0,  0,  0), // Gregorian: 27/08/0009
            new TestCase(1724950.5,  1,     3,    1,   1,  FRI,   0,  0,  0), // Gregorian: 27/08/0010

            // new TestCase(1724536.5,  1,     4,    1,   1,  SUN,   0,  0,  0), // Gregorian: 28/08/0011
            new TestCase(1725316.5,  1,     4,    1,   1,  SUN,   0,  0,  0), // Gregorian: 28/08/0011 - dlf
            new TestCase(1724219.5,  0,  5500,   13,   5,  TUE,   0,  0,  0), // Gregorian: 26/08/0008
            new TestCase(1724584.5,  1,     1,   13,   5,  WED,   0,  0,  0), // Gregorian: 26/08/0009
            new TestCase(1724949.5,  1,     2,   13,   5,  THU,   0,  0,  0), // Gregorian: 26/08/0010
            new TestCase(1725314.5,  1,     3,   13,   5,  FRI,   0,  0,  0), // Gregorian: 26/08/0011
            new TestCase(1725315.5,  1,     3,   13,   6,  SAT,   0,  0,  0), // Gregorian: 27/08/0011 - first ethiopic leap year
            // new TestCase(1725560.5,  1,     4,   13,   5,  SUN,   0,  0,  0), // Gregorian: 26/08/0012 - dlf
            new TestCase(1725680.5,  1,     4,   13,   5,  SUN,   0,  0,  0), // Gregorian: 26/08/0012
            new TestCase(2299158.5,  1,  1575,    2,   6,  WED,   0,  0,  0), // Gregorian: 13/10/1582  
            new TestCase(2299159.5,  1,  1575,    2,   7,  THU,   0,  0,  0), // Gregorian: 14/10/1582  Julian 04/10/1582

            new TestCase(2299160.5,  1,  1575,    2,   8,  FRI,   0,  0,  0), // Gregorian: 15/10/1582
            new TestCase(2299161.5,  1,  1575,    2,   9,  SAT,   0,  0,  0), // Gregorian: 16/10/1582

            new TestCase(2415020.5,  1,  1892,    4,  23,  MON,   0,  0,  0), // Gregorian: 01/01/1900
            new TestCase(2453371.5,  1,  1997,    4,  23,  SAT,   0,  0,  0), // Gregorian: 01/01/2005
            new TestCase(2454719.5,  1,  2000,   13,   5,  WED,   0,  0,  0), // Gregorian: 10/09/2008
        };

        final TestCase[] testsAmeteAlem = {
                //
                // The months in this table are 1-based rather than 0-based,
                // because it's easier to edit that way.
                //                      Ethiopic
                //          Julian Day  Era  Year  Month Day  WkDay Hour Min Sec
                //
                // Dates from "Emporer Theodore..."

                new TestCase(2401442.5,  0,  7355,    2,  20,  WED,   0,  0,  0), // Gregorian: 29/10/1862
                new TestCase(2402422.5,  0,  7357,   10,  29,  WED,   0,  0,  0), // Gregorian: 05/07/1865
                new TestCase(2402630.5,  0,  7358,    5,  22,  MON,   0,  0,  0), // Gregorian: 29/01/1866
                new TestCase(2402708.5,  0,  7358,    8,  10,  TUE,   0,  0,  0), // Gregorian: 17/04/1866
                new TestCase(2402971.5,  0,  7359,    4,  28,  SAT,   0,  0,  0), // Gregorian: 05/01/1867
                new TestCase(2403344.5,  0,  7360,    5,   5,  MON,   0,  0,  0), // Gregorian: 13/01/1868
                            
                // Miscellaneous:
                /* Skip these tests until JD bug fixed in the Gregorian calendar:
                 * http://www.jtcsv.com/cgibin/icu-bugs/incoming?id=4406;page=2;user=guest
                 */
                new TestCase(1721059.5,  0,  5492,    5,   7,  SAT,   0,  0,  0), // Gregorian: 01/01/0000
                new TestCase(1721425.5,  0,  5493,    5,   8,  MON,   0,  0,  0), // Gregorian: 01/01/0001
                new TestCase(1723854.5,  0,  5499,   13,   6,  MON,   0,  0,  0), // Gregorian: 27/08/0007

                new TestCase(1723855.5,  0,  5500,    1,   1,  TUE,   0,  0,  0), // Gregorian: 28/08/0007
                new TestCase(1724220.5,  0,  5501,    1,   1,  WED,   0,  0,  0), // Gregorian: 27/08/0008
                new TestCase(1724585.5,  0,  5502,    1,   1,  THU,   0,  0,  0), // Gregorian: 27/08/0009
                new TestCase(1724950.5,  0,  5503,    1,   1,  FRI,   0,  0,  0), // Gregorian: 27/08/0010

                // new TestCase(1724536.5,  0,  5504,    1,   1,  SUN,   0,  0,  0), // Gregorian: 28/08/0011
                new TestCase(1725316.5,  0,  5504,    1,   1,  SUN,   0,  0,  0), // Gregorian: 28/08/0011 - dlf
                new TestCase(1724219.5,  0,  5500,   13,   5,  TUE,   0,  0,  0), // Gregorian: 26/08/0008
                new TestCase(1724584.5,  0,  5501,   13,   5,  WED,   0,  0,  0), // Gregorian: 26/08/0009
                new TestCase(1724949.5,  0,  5502,   13,   5,  THU,   0,  0,  0), // Gregorian: 26/08/0010
                new TestCase(1725314.5,  0,  5503,   13,   5,  FRI,   0,  0,  0), // Gregorian: 26/08/0011
                new TestCase(1725315.5,  0,  5503,   13,   6,  SAT,   0,  0,  0), // Gregorian: 27/08/0011 - first ethiopic leap year
                // new TestCase(1725560.5,  0,  5504,   13,   5,  SUN,   0,  0,  0), // Gregorian: 26/08/0012 - dlf
                new TestCase(1725680.5,  0,  5504,   13,   5,  SUN,   0,  0,  0), // Gregorian: 26/08/0012
                new TestCase(2299158.5,  0,  7075,    2,   6,  WED,   0,  0,  0), // Gregorian: 13/10/1582  
                new TestCase(2299159.5,  0,  7075,    2,   7,  THU,   0,  0,  0), // Gregorian: 14/10/1582  Julian 04/10/1582

                new TestCase(2299160.5,  0,  7075,    2,   8,  FRI,   0,  0,  0), // Gregorian: 15/10/1582
                new TestCase(2299161.5,  0,  7075,    2,   9,  SAT,   0,  0,  0), // Gregorian: 16/10/1582

                new TestCase(2415020.5,  0,  7392,    4,  23,  MON,   0,  0,  0), // Gregorian: 01/01/1900
                new TestCase(2453371.5,  0,  7497,    4,  23,  SAT,   0,  0,  0), // Gregorian: 01/01/2005
                new TestCase(2454719.5,  0,  7500,   13,   5,  WED,   0,  0,  0), // Gregorian: 10/09/2008
            };

        EthiopicCalendar testCalendar = new EthiopicCalendar();
        testCalendar.setLenient(true);
        doTestCases(tests, testCalendar);

        // Testing Amete Alem mode

        EthiopicCalendar testCalendarAmeteAlem = new EthiopicCalendar();
        testCalendarAmeteAlem.setAmeteAlemEra(true);
        testCalendarAmeteAlem.setLenient(true);
        doTestCases(testsAmeteAlem, testCalendarAmeteAlem);
    }

    // basic check to see that we print out eras ok
    // eventually should modify to use locale strings and formatter appropriate to coptic calendar
    @Test
    public void TestEraStart() {
        SimpleDateFormat fmt = new SimpleDateFormat("EEE MMM dd, yyyy GG");
        fmt.setCalendar(new EthiopicCalendar());

        EthiopicCalendar cal = new EthiopicCalendar(1, 0, 1);
        assertEquals("Ethiopic Date", "Wed Jan 01, 0001 AD", fmt.format(cal));

        cal.set(Calendar.ERA, 0);
        cal.set(Calendar.YEAR, 5500);
        assertEquals("Ethiopic Date", "Tue Jan 01, 5500 BC", fmt.format(cal));

        // The gregorian calendar gets off by two days when
        // the date gets low, unless the gregorian changeover is set to 
        // very early.  The funny thing is, it's ok for dates in the year
        // 283, but not in the year 7, and it claims to be ok until the year 4.
        // should track down when the dates start to differ...
        
        GregorianCalendar gc = new GregorianCalendar();
        gc.setGregorianChange(new Date(Long.MIN_VALUE)); // act like proleptic Gregorian
        gc.setTime(cal.getTime());
        fmt.setCalendar(new GregorianCalendar());
        assertEquals("Gregorian Date", "Tue Aug 28, 0007 AD", fmt.format(gc));
    }

    @Test
    public void TestBasic() {
        EthiopicCalendar cal = new EthiopicCalendar();
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
    public void TestJD(){
        int jd = EthiopicCalendar.EthiopicToJD(1567,8,9);
        EthiopicCalendar cal = new EthiopicCalendar();
        cal.clear();
        cal.set(Calendar.JULIAN_DAY, jd);
        if (cal.get(Calendar.EXTENDED_YEAR) == 1567 &&
            cal.get(Calendar.MONTH) == 8 &&
            cal.get(Calendar.DAY_OF_MONTH) == 9){
            logln("EthiopicCalendar.getDateFromJD tested");
        } else {
            errln("EthiopicCalendar.getDateFromJD failed");
        }
    }

    /**
     * Test limits of the Coptic calendar
     */
    @Test
    public void TestLimits() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 1);
        EthiopicCalendar ethiopic = new EthiopicCalendar();
        doLimitsTest(ethiopic, null, cal.getTime());
        doTheoreticalLimitsTest(ethiopic, true);
    }

    
    @Test
    public void TestCoverage() {

        {
            // new EthiopicCalendar(TimeZone)
            EthiopicCalendar cal = new EthiopicCalendar(TimeZone.getDefault()); 
            if(cal == null){
                errln("could not create EthiopicCalendar with TimeZone");
            }
        }

        {
            // new EthiopicCalendar(ULocale)
            EthiopicCalendar cal = new EthiopicCalendar(ULocale.getDefault());
            if(cal == null){
                errln("could not create EthiopicCalendar with ULocale");
            }
        }
        
        {
            // new EthiopicCalendar(Locale)
            EthiopicCalendar cal = new EthiopicCalendar(Locale.getDefault());
            if(cal == null){
                errln("could not create EthiopicCalendar with Locale");
            }
        }

        {
            // new EthiopicCalendar(TimeZone, Locale)
            EthiopicCalendar cal = new EthiopicCalendar(TimeZone.getDefault(), Locale.getDefault());
            if(cal == null){
                errln("could not create EthiopicCalendar with TimeZone,Locale");
            }
        }

        {
            // new EthiopicCalendar(TimeZone, ULocale)
            EthiopicCalendar cal = new EthiopicCalendar(TimeZone.getDefault(), ULocale.getDefault());
            if(cal == null){
                errln("could not create EthiopicCalendar with TimeZone,ULocale");
            }
        }
        
        {
            // new EthiopicCalendar(Date)
            EthiopicCalendar cal = new EthiopicCalendar(new Date());
            if(cal == null){
                errln("could not create EthiopicCalendar with Date");
            }
        }

        {
            // new EthiopicCalendar(int year, int month, int date)
            EthiopicCalendar cal = new EthiopicCalendar(1997, EthiopicCalendar.MESKEREM, 1);
            if(cal == null){
                errln("could not create EthiopicCalendar with year,month,date");
            }
        }

        {
            // new EthiopicCalendar(int year, int month, int date, int hour, int minute, int second)
            EthiopicCalendar cal = new EthiopicCalendar(1997, EthiopicCalendar.MESKEREM, 1, 1, 1, 1);
            if(cal == null){
                errln("could not create EthiopicCalendar with year,month,date,hour,minute,second");
            }
        }

        {
            // setCivil/isCivil
            // operations on non-civil calendar
            EthiopicCalendar cal = newAmeteAlemEraCalendar();
            cal.setAmeteAlemEra(false);
            if (cal.isAmeteAlemEra()) {
                errln("EthiopicCalendar calendar is old system");
            }

            Date now = new Date();
            cal.setTime(now);

            Date then = cal.getTime();
            if (!now.equals(then)) {
                errln("get/set time failed with non-civil EthiopicCalendar calendar");
            }

            logln(then.toString());

            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.YEAR, 1);

            logln(cal.getTime().toString());
        }
    
        {
            // data
            EthiopicCalendar cal = new EthiopicCalendar(1997, EthiopicCalendar.MESKEREM, 1);
            Date time = cal.getTime();

            String[] calendarLocales = {
                "am_ET", "gez_ET", "ti_ET"
            };

            String[] formatLocales = {
                "en", "am", "gez", "ti"
            };
            for (int i = 0; i < calendarLocales.length; ++i) {
                String calLocName = calendarLocales[i];
                Locale calLocale = LocaleUtility.getLocaleFromName(calLocName);
                cal = new EthiopicCalendar(calLocale);

                for (int j = 0; j < formatLocales.length; ++j) {
                    String locName = formatLocales[j];
                    Locale formatLocale = LocaleUtility.getLocaleFromName(locName);
                    DateFormat format = DateFormat.getDateTimeInstance(cal, DateFormat.FULL, DateFormat.FULL, formatLocale);
                    logln(calLocName + "/" + locName + " --> " + format.format(time));
                }
            }
        }
    }
    
    private static EthiopicCalendar newAmeteAlemEraCalendar() {
        EthiopicCalendar alemawiCalendar = new EthiopicCalendar();
        alemawiCalendar.setAmeteAlemEra(true);
        return alemawiCalendar;
    }

    @Test
    public void TestAddSet() {
        class TestAddSetItem {
            private int startYear;
            private int startMonth; // 0-based
            private int startDay; // 1-based
            private int fieldToChange;
            private int fieldDelta;
            private int endYear;
            private int endMonth;
            private int endDay;
            TestAddSetItem(int sYr, int sMo, int sDa, int field, int delta, int eYr, int eMo, int eDa) {
                startYear = sYr;
                startMonth = sMo;
                startDay = sDa;
                fieldToChange = field;
                fieldDelta = delta;
                endYear = eYr;
                endMonth = eMo;
                endDay = eDa;
            }
            public int getStartYear()  { return startYear; }
            public int getStartMonth() { return startMonth; }
            public int getStartDay()   { return startDay; }
            public int getField()      { return fieldToChange; }
            public int getDelta()      { return fieldDelta; }
            public int getEndYear()    { return endYear; }
            public int getEndMonth()   { return endMonth; }
            public int getEndDay()     { return endDay; }
        }
        final TestAddSetItem[] tests = {
            new TestAddSetItem( 2000, 12, 1, Calendar.MONTH, +1, 2001,  0, 1 ),
            new TestAddSetItem( 2000, 12, 1, Calendar.MONTH, +9, 2001,  8, 1 ),
            new TestAddSetItem( 1999, 12, 2, Calendar.MONTH, +1, 2000,  0, 2 ), // 1999 is a leap year
            new TestAddSetItem( 1999, 12, 2, Calendar.MONTH, +9, 2000,  8, 2 ),
            new TestAddSetItem( 2001,  0, 1, Calendar.MONTH, -1, 2000, 12, 1 ),
            new TestAddSetItem( 2001,  0, 1, Calendar.MONTH, -6, 2000,  7, 1 ),
            new TestAddSetItem( 2000, 12, 1, Calendar.DATE,  +8, 2001,  0, 4 ),
            new TestAddSetItem( 1999, 12, 1, Calendar.DATE,  +8, 2000,  0, 3 ), // 1999 is a leap year
            new TestAddSetItem( 2000,  0, 1, Calendar.DATE,  -1, 1999, 12, 6 ),
        };
        EthiopicCalendar testCalendar = new EthiopicCalendar();
        for ( int i = 0; i < tests.length; i++ ) {
            TestAddSetItem item = tests[i];
            testCalendar.set( item.getStartYear(), item.getStartMonth(), item.getStartDay(), 9, 0 );
            testCalendar.add( item.getField(), item.getDelta() );
            int endYear = testCalendar.get(Calendar.YEAR);
            int endMonth = testCalendar.get(Calendar.MONTH);
            int endDay = testCalendar.get(Calendar.DATE);
            if ( endYear != item.getEndYear() || endMonth != item.getEndMonth() || endDay != item.getEndDay() ) {
                errln("EToJD FAILS: field " + item.getField() + " delta " + item.getDelta() + 
                            " expected yr " + item.getEndYear() + " mo " + item.getEndMonth() +  " da " + item.getEndDay() +
                            " got yr " + endYear + " mo " + endMonth +  " da " + endDay);
            }
        }
    }
}
