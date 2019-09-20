/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2015, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.calendar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.LocaleUtility;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.IslamicCalendar;
import android.icu.util.IslamicCalendar.CalculationType;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;

/**
 * Tests for the <code>IslamicCalendar</code> class.
 */
public class IslamicTest extends CalendarTestFmwk {
    /** Constants to save typing. */
    public static final int MUHARRAM = IslamicCalendar.MUHARRAM;
    public static final int SAFAR =  IslamicCalendar.SAFAR;
    public static final int RABI_1 =  IslamicCalendar.RABI_1;
    public static final int RABI_2 =  IslamicCalendar.RABI_2;
    public static final int JUMADA_1 =  IslamicCalendar.JUMADA_1;
    public static final int JUMADA_2 =  IslamicCalendar.JUMADA_2;
    public static final int RAJAB =  IslamicCalendar.RAJAB;
    public static final int SHABAN =  IslamicCalendar.SHABAN;
    public static final int RAMADAN =  IslamicCalendar.RAMADAN;
    public static final int SHAWWAL =  IslamicCalendar.SHAWWAL;
    public static final int QIDAH =  IslamicCalendar.DHU_AL_QIDAH;
    public static final int HIJJAH =  IslamicCalendar.DHU_AL_HIJJAH;

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
       
        IslamicCalendar cal = newCivil();
        doRollAdd(ROLL, cal, tests);
        
        cal = newIslamicUmalqura();
        doRollAdd(ROLL, cal, tests);
    }

    /**
     * A huge list of test cases to make sure that computeTime and computeFields
     * work properly for a wide range of data in the civil calendar.
     */
    @Test
    public void TestCivilCases()
    {
        final TestCase[] tests = {
            //
            // Most of these test cases were taken from the back of
            // "Calendrical Calculations", with some extras added to help
            // debug a few of the problems that cropped up in development.
            //
            // The months in this table are 1-based rather than 0-based,
            // because it's easier to edit that way.
            //                       Islamic
            //          Julian Day  Era  Year  Month Day  WkDay Hour Min Sec
            new TestCase(1507231.5,  0, -1245,   12,   9,  SUN,   0,  0,  0),
            new TestCase(1660037.5,  0,  -813,    2,  23,  WED,   0,  0,  0),
            new TestCase(1746893.5,  0,  -568,    4,   1,  WED,   0,  0,  0),
            new TestCase(1770641.5,  0,  -501,    4,   6,  SUN,   0,  0,  0),
            new TestCase(1892731.5,  0,  -157,   10,  17,  WED,   0,  0,  0),
            new TestCase(1931579.5,  0,   -47,    6,   3,  MON,   0,  0,  0),
            new TestCase(1974851.5,  0,    75,    7,  13,  SAT,   0,  0,  0),
            new TestCase(2091164.5,  0,   403,   10,   5,  SUN,   0,  0,  0),
            new TestCase(2121509.5,  0,   489,    5,  22,  SUN,   0,  0,  0),
            new TestCase(2155779.5,  0,   586,    2,   7,  FRI,   0,  0,  0),
            new TestCase(2174029.5,  0,   637,    8,   7,  SAT,   0,  0,  0),
            new TestCase(2191584.5,  0,   687,    2,  20,  FRI,   0,  0,  0),
            new TestCase(2195261.5,  0,   697,    7,   7,  SUN,   0,  0,  0),
            new TestCase(2229274.5,  0,   793,    7,   1,  SUN,   0,  0,  0),
            new TestCase(2245580.5,  0,   839,    7,   6,  WED,   0,  0,  0),
            new TestCase(2266100.5,  0,   897,    6,   1,  SAT,   0,  0,  0),
            new TestCase(2288542.5,  0,   960,    9,  30,  SAT,   0,  0,  0),
            new TestCase(2290901.5,  0,   967,    5,  27,  SAT,   0,  0,  0),
            new TestCase(2323140.5,  0,  1058,    5,  18,  WED,   0,  0,  0),
            new TestCase(2334848.5,  0,  1091,    6,   2,  SUN,   0,  0,  0),
            new TestCase(2348020.5,  0,  1128,    8,   4,  FRI,   0,  0,  0),
            new TestCase(2366978.5,  0,  1182,    2,   3,  SUN,   0,  0,  0),
            new TestCase(2385648.5,  0,  1234,   10,  10,  MON,   0,  0,  0),
            new TestCase(2392825.5,  0,  1255,    1,  11,  WED,   0,  0,  0),
            new TestCase(2416223.5,  0,  1321,    1,  21,  SUN,   0,  0,  0),
            new TestCase(2425848.5,  0,  1348,    3,  19,  SUN,   0,  0,  0),
            new TestCase(2430266.5,  0,  1360,    9,   8,  MON,   0,  0,  0),
            new TestCase(2430833.5,  0,  1362,    4,  13,  MON,   0,  0,  0),
            new TestCase(2431004.5,  0,  1362,   10,   7,  THU,   0,  0,  0),
            new TestCase(2448698.5,  0,  1412,    9,  13,  TUE,   0,  0,  0),
            new TestCase(2450138.5,  0,  1416,   10,   5,  SUN,   0,  0,  0),
            new TestCase(2465737.5,  0,  1460,   10,  12,  WED,   0,  0,  0),
            new TestCase(2486076.5,  0,  1518,    3,   5,  SUN,   0,  0,  0),
        };
        
        IslamicCalendar civilCalendar = newCivil();
        civilCalendar.setLenient(true);
        doTestCases(tests, civilCalendar);
    }

    @Test
    public void TestBasic() {
        IslamicCalendar cal = newCivil();
        cal.clear();
        cal.set(1000, 0, 30);
        logln("1000/0/30 -> " +
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

    /**
     * Test limits of the Islamic calendar
     */
    @Test
    public void TestLimits() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 1);
        IslamicCalendar islamic = newCivil();
        doLimitsTest(islamic, null, cal.getTime());
        doTheoreticalLimitsTest(islamic, true);


        // number of days to test - Islamic calendar starts to exhibit 
        // rounding errors after year AH3954 - about 2500 years out.

        IslamicCalendar islamic2 = new IslamicCalendar();
        islamic2.setCalculationType(CalculationType.ISLAMIC);
        int testTime = TestFmwk.getExhaustiveness() <= 5 ? 20000 : 800000;
        doLimitsTest(islamic2, null, cal.getTime(), testTime);
        doTheoreticalLimitsTest(islamic2, true);
    }

    @Test
    public void Test7427() {
        // Test the add month in a leap year problem as reported in ticket #7427
        IslamicCalendar cal = new IslamicCalendar();
        cal.clear();
        cal.set(IslamicCalendar.YEAR,1431);
        cal.set(IslamicCalendar.MONTH, IslamicCalendar.DHU_AL_HIJJAH);
        cal.add(IslamicCalendar.MONTH,1);
        if ( cal.get(IslamicCalendar.MONTH) != IslamicCalendar.MUHARRAM  ||
           ( cal.get(IslamicCalendar.YEAR) != 1432 )) {
               errln("Error incrementing month at the end of a leap year.  Expected Month:0 Year:1432 - Got Month:" + 
                       cal.get(IslamicCalendar.MONTH) + " Year:" + cal.get(IslamicCalendar.YEAR));
           }
    }
    
    @Test
    public void TestCoverage() {
    {
        // new IslamicCalendar(TimeZone)
        IslamicCalendar cal = new IslamicCalendar(TimeZone.getDefault());
        if(cal == null){
            errln("could not create IslamicCalendar with TimeZone");
        }
    }

    {
        // new IslamicCalendar(ULocale)
        IslamicCalendar cal = new IslamicCalendar(ULocale.getDefault());
        if(cal == null){
            errln("could not create IslamicCalendar with ULocale");
        }
    }
        
    {
        // new IslamicCalendar(Locale)
        IslamicCalendar cal = new IslamicCalendar(Locale.getDefault());
        if(cal == null){
            errln("could not create IslamicCalendar with Locale");
        }
    }

    {
        // new IslamicCalendar(Date)
        IslamicCalendar cal = new IslamicCalendar(new Date());
        if(cal == null){
            errln("could not create IslamicCalendar with Date");
        }
    }

    {
        // new IslamicCalendar(int year, int month, int date)
        IslamicCalendar cal = new IslamicCalendar(800, IslamicCalendar.RAMADAN, 1);
        if(cal == null){
            errln("could not create IslamicCalendar with year,month,date");
        }
    }

    {
        // new IslamicCalendar(int year, int month, int date, int hour, int minute, int second)
        IslamicCalendar cal = new IslamicCalendar(800, IslamicCalendar.RAMADAN, 1, 1, 1, 1);
        if(cal == null){
            errln("could not create IslamicCalendar with year,month,date,hour,minute,second");
        }
    }

    {
        // setCivil/isCivil
        // operations on non-civil calendar
        IslamicCalendar cal = new IslamicCalendar(800, IslamicCalendar.RAMADAN, 1, 1, 1, 1);
        cal.setCivil(false);
        if (cal.isCivil()) {
        errln("islamic calendar is civil");
        }

        // since setCivil/isCivil are now deprecated, make sure same test works for setType
        // operations on non-civil calendar
        cal = new IslamicCalendar(800, IslamicCalendar.RAMADAN, 1, 1, 1, 1);
        cal.setCalculationType(CalculationType.ISLAMIC);
        if (cal.isCivil()) {
        errln("islamic calendar is civil");
        }
        
        // ensure calculation type getter returns correct object and value
        cal.setCalculationType(CalculationType.ISLAMIC_UMALQURA);
        Object ctObj = cal.getCalculationType();
        if(ctObj instanceof CalculationType) {
            CalculationType ct = (CalculationType)ctObj;
            if(ct != CalculationType.ISLAMIC_UMALQURA) {
                errln("wrong calculation type returned from getCalculationType");
            }
        } else {
            errln("wrong object type returned from getCalculationType");
        }
        

        Date now = new Date();
        cal.setTime(now);

        Date then = cal.getTime();
        if (!now.equals(then)) {
        errln("get/set time failed with non-civil islamic calendar");
        }

        logln(then.toString());

        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.YEAR, 1);

        logln(cal.getTime().toString());
    }
    
    {
        // data
        IslamicCalendar cal = new IslamicCalendar(800, IslamicCalendar.RAMADAN, 1);
        Date time = cal.getTime();

        String[] calendarLocales = {
        "ar_AE", "ar_BH", "ar_DZ", "ar_EG", "ar_JO", "ar_KW", "ar_OM", 
        "ar_QA", "ar_SA", "ar_SY", "ar_YE", "ms_MY"
        };

        String[] formatLocales = {
        "en", "ar", "fi", "fr", "hu", "iw", "nl"
        };
        for (int i = 0; i < calendarLocales.length; ++i) {
        String calLocName = calendarLocales[i];
        Locale calLocale = LocaleUtility.getLocaleFromName(calLocName);
        cal = new IslamicCalendar(calLocale);

        for (int j = 0; j < formatLocales.length; ++j) {
            String locName = formatLocales[j];
            Locale formatLocale = LocaleUtility.getLocaleFromName(locName);
            DateFormat format = DateFormat.getDateTimeInstance(cal, DateFormat.FULL, DateFormat.FULL, formatLocale);
            logln(calLocName + "/" + locName + " --> " + format.format(time));
        }
        }
    }
    }

    private static IslamicCalendar newCivil() {
        IslamicCalendar civilCalendar = new IslamicCalendar();
        civilCalendar.setCalculationType(CalculationType.ISLAMIC_CIVIL);
        return civilCalendar;
    }
    private static IslamicCalendar newIslamic() {
        IslamicCalendar civilCalendar = new IslamicCalendar();
        civilCalendar.setCalculationType(CalculationType.ISLAMIC);
        return civilCalendar;
    }
    
    private static IslamicCalendar newIslamicUmalqura() {
        IslamicCalendar civilCalendar = new IslamicCalendar();
        civilCalendar.setCalculationType(CalculationType.ISLAMIC_UMALQURA);
        return civilCalendar;
    }

    private void verifyType(Calendar c, String expectType) {
        String theType = c.getType();
        if(!theType.equals(expectType)) {
            errln("Expected calendar to be type " + expectType + " but instead it is " + theType);
        }
    }

    @Test
    public void Test8822() {
        verifyType(newIslamic(),"islamic");
        verifyType(newCivil(),"islamic-civil");
        verifyType(newIslamicUmalqura(), "islamic-umalqura");
    } 
    
    private void setAndTestCalendar(IslamicCalendar cal, int initMonth, int initDay, int initYear) {
        cal.clear();
        cal.setLenient(false);
        cal.set(initYear, initMonth, initDay);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        if(initDay != day || initMonth != month || initYear != year)
        {
            errln("year init values:\tmonth "+initMonth+"\tday "+initDay+"\tyear "+initYear);
            errln("values post set():\tmonth "+month+"\tday "+day+"\tyear "+year);
        }
    }

    private void setAndTestWholeYear(IslamicCalendar cal, int startYear) {
        for(int startMonth = 0; startMonth < 12; startMonth++) {
            for(int startDay = 1; startDay < 31; startDay++ ) {                
                try {
                    setAndTestCalendar(cal, startMonth, startDay, startYear);
                } catch(IllegalArgumentException iae) {
                    if(startDay != 30) {
                        errln("unexpected exception that wasn't for trying to set a date to '30'. errmsg - " + iae.getLocalizedMessage());
                    }                    
                }                
            }
        }
    }
    
    @Test
    public void TestIslamicUmAlQura() {

        class GregoUmmAlQuraMap {
            public int gYear;
            public int gMon; // 1-based
            public int gDay;
            public int uYear;
            public int uMon; // 1-based
            public int uDay;
             // Simple constructor
            public GregoUmmAlQuraMap(int gY, int gM, int gD, int uY, int uM, int uD) {
                gYear = gY;
                gMon  = gM;
                gDay  = gD;
                uYear = uY;
                uMon  = uM;
                uDay  = uD;
            }
        };
        // data from
        // Official Umm-al-Qura calendar of SA:
        // home, http://www.ummulqura.org.sa/default.aspx
        // converter, http://www.ummulqura.org.sa/Index.aspx
        final GregoUmmAlQuraMap[] guMappings = {
                //                     gregorian,    ummAlQura
                //                     year mo da,   year mo da
                //                     (using 1-based months here)
                new GregoUmmAlQuraMap( 1882,11,12,   1300, 1, 1 ),
                new GregoUmmAlQuraMap( 1892, 7,25,   1310, 1, 1 ),
                new GregoUmmAlQuraMap( 1896, 6,12,   1314, 1, 1 ),
                new GregoUmmAlQuraMap( 1898, 5,22,   1316, 1, 1 ),
                new GregoUmmAlQuraMap( 1900, 4,30,   1318, 1, 1 ),
                new GregoUmmAlQuraMap( 1901, 4,20,   1319, 1, 1 ),
                new GregoUmmAlQuraMap( 1902, 4,10,   1320, 1, 1 ),
                new GregoUmmAlQuraMap( 1903, 3,30,   1321, 1, 1 ),
                new GregoUmmAlQuraMap( 1904, 3,19,   1322, 1, 1 ),
                new GregoUmmAlQuraMap( 1905, 3, 8,   1323, 1, 1 ),
                new GregoUmmAlQuraMap( 1906, 2,25,   1324, 1, 1 ),
                new GregoUmmAlQuraMap( 1907, 2,14,   1325, 1, 1 ),
                new GregoUmmAlQuraMap( 1908, 2, 4,   1326, 1, 1 ),
                new GregoUmmAlQuraMap( 1909, 1,23,   1327, 1, 1 ),
                new GregoUmmAlQuraMap( 1910, 1,13,   1328, 1, 1 ),
                new GregoUmmAlQuraMap( 1911, 1, 2,   1329, 1, 1 ),
                new GregoUmmAlQuraMap( 1911,12,22,   1330, 1, 1 ),
                new GregoUmmAlQuraMap( 1912,12,10,   1331, 1, 1 ),
                new GregoUmmAlQuraMap( 1913,11,30,   1332, 1, 1 ),
                new GregoUmmAlQuraMap( 1914,11,19,   1333, 1, 1 ),
                new GregoUmmAlQuraMap( 1915,11, 9,   1334, 1, 1 ),
                new GregoUmmAlQuraMap( 1916,10,28,   1335, 1, 1 ),
                new GregoUmmAlQuraMap( 1917,10,18,   1336, 1, 1 ),
                new GregoUmmAlQuraMap( 1918,10, 7,   1337, 1, 1 ),
                new GregoUmmAlQuraMap( 1919, 9,26,   1338, 1, 1 ),
                new GregoUmmAlQuraMap( 1920, 9,14,   1339, 1, 1 ),
                new GregoUmmAlQuraMap( 1921, 9, 4,   1340, 1, 1 ),
                new GregoUmmAlQuraMap( 1922, 8,24,   1341, 1, 1 ),
                new GregoUmmAlQuraMap( 1923, 8,14,   1342, 1, 1 ),
                new GregoUmmAlQuraMap( 1924, 8, 2,   1343, 1, 1 ),
                new GregoUmmAlQuraMap( 1925, 7,22,   1344, 1, 1 ),
                new GregoUmmAlQuraMap( 1926, 7,11,   1345, 1, 1 ),
                new GregoUmmAlQuraMap( 1927, 6,30,   1346, 1, 1 ),
                new GregoUmmAlQuraMap( 1928, 6,19,   1347, 1, 1 ),
                new GregoUmmAlQuraMap( 1929, 6, 9,   1348, 1, 1 ),
                new GregoUmmAlQuraMap( 1930, 5,29,   1349, 1, 1 ),
                new GregoUmmAlQuraMap( 1931, 5,19,   1350, 1, 1 ),
                new GregoUmmAlQuraMap( 1932, 5, 7,   1351, 1, 1 ),
                new GregoUmmAlQuraMap( 1933, 4,26,   1352, 1, 1 ),
                new GregoUmmAlQuraMap( 1934, 4,15,   1353, 1, 1 ),
                new GregoUmmAlQuraMap( 1935, 4, 5,   1354, 1, 1 ),
                new GregoUmmAlQuraMap( 1936, 3,24,   1355, 1, 1 ),
                new GregoUmmAlQuraMap( 1937, 3,14,   1356, 1, 1 ),
                new GregoUmmAlQuraMap( 1938, 3, 4,   1357, 1, 1 ),
                new GregoUmmAlQuraMap( 1939, 2,21,   1358, 1, 1 ),
                new GregoUmmAlQuraMap( 1940, 2,10,   1359, 1, 1 ),
                new GregoUmmAlQuraMap( 1941, 1,29,   1360, 1, 1 ),
                new GregoUmmAlQuraMap( 1942, 1,18,   1361, 1, 1 ),
                new GregoUmmAlQuraMap( 1943, 1, 8,   1362, 1, 1 ),
                new GregoUmmAlQuraMap( 1943,12,28,   1363, 1, 1 ),
                new GregoUmmAlQuraMap( 1944,12,17,   1364, 1, 1 ),
                new GregoUmmAlQuraMap( 1945,12, 6,   1365, 1, 1 ),
                new GregoUmmAlQuraMap( 1946,11,25,   1366, 1, 1 ),
                new GregoUmmAlQuraMap( 1947,11,14,   1367, 1, 1 ),
                new GregoUmmAlQuraMap( 1948,11, 3,   1368, 1, 1 ),
                new GregoUmmAlQuraMap( 1949,10,23,   1369, 1, 1 ),
                new GregoUmmAlQuraMap( 1950,10,13,   1370, 1, 1 ),
                new GregoUmmAlQuraMap( 1951,10, 3,   1371, 1, 1 ),
                new GregoUmmAlQuraMap( 1952, 9,21,   1372, 1, 1 ),
                new GregoUmmAlQuraMap( 1953, 9,10,   1373, 1, 1 ),
                new GregoUmmAlQuraMap( 1954, 8,30,   1374, 1, 1 ),
                new GregoUmmAlQuraMap( 1955, 8,19,   1375, 1, 1 ),
                new GregoUmmAlQuraMap( 1956, 8, 8,   1376, 1, 1 ),
                new GregoUmmAlQuraMap( 1957, 7,29,   1377, 1, 1 ),
                new GregoUmmAlQuraMap( 1958, 7,18,   1378, 1, 1 ),
                new GregoUmmAlQuraMap( 1959, 7, 8,   1379, 1, 1 ),
                new GregoUmmAlQuraMap( 1960, 6,26,   1380, 1, 1 ),
                new GregoUmmAlQuraMap( 1961, 6,15,   1381, 1, 1 ),
                new GregoUmmAlQuraMap( 1962, 6, 4,   1382, 1, 1 ),
                new GregoUmmAlQuraMap( 1963, 5,24,   1383, 1, 1 ),
                new GregoUmmAlQuraMap( 1964, 5,13,   1384, 1, 1 ),
                new GregoUmmAlQuraMap( 1965, 5, 3,   1385, 1, 1 ),
                new GregoUmmAlQuraMap( 1966, 4,22,   1386, 1, 1 ),
                new GregoUmmAlQuraMap( 1967, 4,11,   1387, 1, 1 ),
                new GregoUmmAlQuraMap( 1968, 3,30,   1388, 1, 1 ),
                new GregoUmmAlQuraMap( 1969, 3,19,   1389, 1, 1 ),
                new GregoUmmAlQuraMap( 1970, 3, 9,   1390, 1, 1 ),
                new GregoUmmAlQuraMap( 1971, 2,27,   1391, 1, 1 ),
                new GregoUmmAlQuraMap( 1972, 2,16,   1392, 1, 1 ),
                new GregoUmmAlQuraMap( 1973, 2, 5,   1393, 1, 1 ),
                new GregoUmmAlQuraMap( 1974, 1,25,   1394, 1, 1 ),
                new GregoUmmAlQuraMap( 1975, 1,14,   1395, 1, 1 ),
                new GregoUmmAlQuraMap( 1976, 1, 3,   1396, 1, 1 ),
                new GregoUmmAlQuraMap( 1976,12,22,   1397, 1, 1 ),
                new GregoUmmAlQuraMap( 1977,12,12,   1398, 1, 1 ),
                new GregoUmmAlQuraMap( 1978,12, 1,   1399, 1, 1 ),
                new GregoUmmAlQuraMap( 1979,11,21,   1400, 1, 1 ),
                new GregoUmmAlQuraMap( 1980,11, 9,   1401, 1, 1 ),
                new GregoUmmAlQuraMap( 1981,10,29,   1402, 1, 1 ),
                new GregoUmmAlQuraMap( 1982,10,18,   1403, 1, 1 ),
                new GregoUmmAlQuraMap( 1983,10, 8,   1404, 1, 1 ),
                new GregoUmmAlQuraMap( 1984, 9,26,   1405, 1, 1 ),
                new GregoUmmAlQuraMap( 1985, 9,16,   1406, 1, 1 ),
                new GregoUmmAlQuraMap( 1986, 9, 6,   1407, 1, 1 ),
                new GregoUmmAlQuraMap( 1987, 8,26,   1408, 1, 1 ),
                new GregoUmmAlQuraMap( 1988, 8,14,   1409, 1, 1 ),
                new GregoUmmAlQuraMap( 1989, 8, 3,   1410, 1, 1 ),
                new GregoUmmAlQuraMap( 1990, 7,23,   1411, 1, 1 ),
                new GregoUmmAlQuraMap( 1991, 7,13,   1412, 1, 1 ),
                new GregoUmmAlQuraMap( 1992, 7, 2,   1413, 1, 1 ),
                new GregoUmmAlQuraMap( 1993, 6,21,   1414, 1, 1 ),
                new GregoUmmAlQuraMap( 1994, 6,11,   1415, 1, 1 ),
                new GregoUmmAlQuraMap( 1995, 5,31,   1416, 1, 1 ),
                new GregoUmmAlQuraMap( 1996, 5,19,   1417, 1, 1 ),
                new GregoUmmAlQuraMap( 1997, 5, 8,   1418, 1, 1 ),
                new GregoUmmAlQuraMap( 1998, 4,28,   1419, 1, 1 ),
                new GregoUmmAlQuraMap( 1999, 4,17,   1420, 1, 1 ),
                new GregoUmmAlQuraMap( 1999, 5,16,   1420, 2, 1 ),
                new GregoUmmAlQuraMap( 1999, 6,15,   1420, 3, 1 ),
                new GregoUmmAlQuraMap( 1999, 7,14,   1420, 4, 1 ),
                new GregoUmmAlQuraMap( 1999, 8,12,   1420, 5, 1 ),
                new GregoUmmAlQuraMap( 1999, 9,11,   1420, 6, 1 ),
                new GregoUmmAlQuraMap( 1999,10,10,   1420, 7, 1 ),
                new GregoUmmAlQuraMap( 1999,11, 9,   1420, 8, 1 ),
                new GregoUmmAlQuraMap( 1999,12, 9,   1420, 9, 1 ),
                new GregoUmmAlQuraMap( 2000, 1, 8,   1420,10, 1 ),
                new GregoUmmAlQuraMap( 2000, 2, 7,   1420,11, 1 ),
                new GregoUmmAlQuraMap( 2000, 3, 7,   1420,12, 1 ),
                new GregoUmmAlQuraMap( 2000, 4, 6,   1421, 1, 1 ),
                new GregoUmmAlQuraMap( 2000, 5, 5,   1421, 2, 1 ),
                new GregoUmmAlQuraMap( 2000, 6, 3,   1421, 3, 1 ),
                new GregoUmmAlQuraMap( 2000, 7, 3,   1421, 4, 1 ),
                new GregoUmmAlQuraMap( 2000, 8, 1,   1421, 5, 1 ),
                new GregoUmmAlQuraMap( 2000, 8,30,   1421, 6, 1 ),
                new GregoUmmAlQuraMap( 2000, 9,28,   1421, 7, 1 ),
                new GregoUmmAlQuraMap( 2000,10,28,   1421, 8, 1 ),
                new GregoUmmAlQuraMap( 2000,11,27,   1421, 9, 1 ),
                new GregoUmmAlQuraMap( 2000,12,27,   1421,10, 1 ),
                new GregoUmmAlQuraMap( 2001, 1,26,   1421,11, 1 ),
                new GregoUmmAlQuraMap( 2001, 2,24,   1421,12, 1 ),
                new GregoUmmAlQuraMap( 2001, 3,26,   1422, 1, 1 ),
                new GregoUmmAlQuraMap( 2001, 4,25,   1422, 2, 1 ),
                new GregoUmmAlQuraMap( 2001, 5,24,   1422, 3, 1 ),
                new GregoUmmAlQuraMap( 2001, 6,22,   1422, 4, 1 ),
                new GregoUmmAlQuraMap( 2001, 7,22,   1422, 5, 1 ),
                new GregoUmmAlQuraMap( 2001, 8,20,   1422, 6, 1 ),
                new GregoUmmAlQuraMap( 2001, 9,18,   1422, 7, 1 ),
                new GregoUmmAlQuraMap( 2001,10,17,   1422, 8, 1 ),
                new GregoUmmAlQuraMap( 2001,11,16,   1422, 9, 1 ),
                new GregoUmmAlQuraMap( 2001,12,16,   1422,10, 1 ),
                new GregoUmmAlQuraMap( 2002, 1,15,   1422,11, 1 ),
                new GregoUmmAlQuraMap( 2002, 2,13,   1422,12, 1 ),
                new GregoUmmAlQuraMap( 2002, 3,15,   1423, 1, 1 ),
                new GregoUmmAlQuraMap( 2002, 4,14,   1423, 2, 1 ),
                new GregoUmmAlQuraMap( 2002, 5,13,   1423, 3, 1 ),
                new GregoUmmAlQuraMap( 2002, 6,12,   1423, 4, 1 ),
                new GregoUmmAlQuraMap( 2002, 7,11,   1423, 5, 1 ),
                new GregoUmmAlQuraMap( 2002, 8,10,   1423, 6, 1 ),
                new GregoUmmAlQuraMap( 2002, 9, 8,   1423, 7, 1 ),
                new GregoUmmAlQuraMap( 2002,10, 7,   1423, 8, 1 ),
                new GregoUmmAlQuraMap( 2002,11, 6,   1423, 9, 1 ),
                new GregoUmmAlQuraMap( 2002,12, 5,   1423,10, 1 ),
                new GregoUmmAlQuraMap( 2003, 1, 4,   1423,11, 1 ),
                new GregoUmmAlQuraMap( 2003, 2, 2,   1423,12, 1 ),
                new GregoUmmAlQuraMap( 2003, 3, 4,   1424, 1, 1 ),
                new GregoUmmAlQuraMap( 2003, 4, 3,   1424, 2, 1 ),
                new GregoUmmAlQuraMap( 2003, 5, 2,   1424, 3, 1 ),
                new GregoUmmAlQuraMap( 2003, 6, 1,   1424, 4, 1 ),
                new GregoUmmAlQuraMap( 2003, 7, 1,   1424, 5, 1 ),
                new GregoUmmAlQuraMap( 2003, 7,30,   1424, 6, 1 ),
                new GregoUmmAlQuraMap( 2003, 8,29,   1424, 7, 1 ),
                new GregoUmmAlQuraMap( 2003, 9,27,   1424, 8, 1 ),
                new GregoUmmAlQuraMap( 2003,10,26,   1424, 9, 1 ),
                new GregoUmmAlQuraMap( 2003,11,25,   1424,10, 1 ),
                new GregoUmmAlQuraMap( 2003,12,24,   1424,11, 1 ),
                new GregoUmmAlQuraMap( 2004, 1,23,   1424,12, 1 ),
                new GregoUmmAlQuraMap( 2004, 2,21,   1425, 1, 1 ),
                new GregoUmmAlQuraMap( 2004, 3,22,   1425, 2, 1 ),
                new GregoUmmAlQuraMap( 2004, 4,20,   1425, 3, 1 ),
                new GregoUmmAlQuraMap( 2004, 5,20,   1425, 4, 1 ),
                new GregoUmmAlQuraMap( 2004, 6,19,   1425, 5, 1 ),
                new GregoUmmAlQuraMap( 2004, 7,18,   1425, 6, 1 ),
                new GregoUmmAlQuraMap( 2004, 8,17,   1425, 7, 1 ),
                new GregoUmmAlQuraMap( 2004, 9,15,   1425, 8, 1 ),
                new GregoUmmAlQuraMap( 2004,10,15,   1425, 9, 1 ),
                new GregoUmmAlQuraMap( 2004,11,14,   1425,10, 1 ),
                new GregoUmmAlQuraMap( 2004,12,13,   1425,11, 1 ),
                new GregoUmmAlQuraMap( 2005, 1,12,   1425,12, 1 ),
                new GregoUmmAlQuraMap( 2005, 2,10,   1426, 1, 1 ),
                new GregoUmmAlQuraMap( 2005, 3,11,   1426, 2, 1 ),
                new GregoUmmAlQuraMap( 2005, 4,10,   1426, 3, 1 ),
                new GregoUmmAlQuraMap( 2005, 5, 9,   1426, 4, 1 ),
                new GregoUmmAlQuraMap( 2005, 6, 8,   1426, 5, 1 ),
                new GregoUmmAlQuraMap( 2005, 7, 7,   1426, 6, 1 ),
                new GregoUmmAlQuraMap( 2005, 8, 6,   1426, 7, 1 ),
                new GregoUmmAlQuraMap( 2005, 9, 5,   1426, 8, 1 ),
                new GregoUmmAlQuraMap( 2005,10, 4,   1426, 9, 1 ),
                new GregoUmmAlQuraMap( 2005,11, 3,   1426,10, 1 ),
                new GregoUmmAlQuraMap( 2005,12, 3,   1426,11, 1 ),
                new GregoUmmAlQuraMap( 2006, 1, 1,   1426,12, 1 ),
                new GregoUmmAlQuraMap( 2006, 1,31,   1427, 1, 1 ),
                new GregoUmmAlQuraMap( 2006, 3, 1,   1427, 2, 1 ),
                new GregoUmmAlQuraMap( 2006, 3,30,   1427, 3, 1 ),
                new GregoUmmAlQuraMap( 2006, 4,29,   1427, 4, 1 ),
                new GregoUmmAlQuraMap( 2006, 5,28,   1427, 5, 1 ),
                new GregoUmmAlQuraMap( 2006, 6,27,   1427, 6, 1 ),
                new GregoUmmAlQuraMap( 2006, 7,26,   1427, 7, 1 ),
                new GregoUmmAlQuraMap( 2006, 8,25,   1427, 8, 1 ),
                new GregoUmmAlQuraMap( 2006, 9,24,   1427, 9, 1 ),
                new GregoUmmAlQuraMap( 2006,10,23,   1427,10, 1 ),
                new GregoUmmAlQuraMap( 2006,11,22,   1427,11, 1 ),
                new GregoUmmAlQuraMap( 2006,12,22,   1427,12, 1 ),
                new GregoUmmAlQuraMap( 2007, 1,20,   1428, 1, 1 ),
                new GregoUmmAlQuraMap( 2007, 2,19,   1428, 2, 1 ),
                new GregoUmmAlQuraMap( 2007, 3,20,   1428, 3, 1 ),
                new GregoUmmAlQuraMap( 2007, 4,18,   1428, 4, 1 ),
                new GregoUmmAlQuraMap( 2007, 5,18,   1428, 5, 1 ),
                new GregoUmmAlQuraMap( 2007, 6,16,   1428, 6, 1 ),
                new GregoUmmAlQuraMap( 2007, 7,15,   1428, 7, 1 ),
                new GregoUmmAlQuraMap( 2007, 8,14,   1428, 8, 1 ),
                new GregoUmmAlQuraMap( 2007, 9,13,   1428, 9, 1 ),
                new GregoUmmAlQuraMap( 2007,10,13,   1428,10, 1 ),
                new GregoUmmAlQuraMap( 2007,11,11,   1428,11, 1 ),
                new GregoUmmAlQuraMap( 2007,12,11,   1428,12, 1 ),
                new GregoUmmAlQuraMap( 2008, 1,10,   1429, 1, 1 ),
                new GregoUmmAlQuraMap( 2008, 2, 8,   1429, 2, 1 ),
                new GregoUmmAlQuraMap( 2008, 3, 9,   1429, 3, 1 ),
                new GregoUmmAlQuraMap( 2008, 4, 7,   1429, 4, 1 ),
                new GregoUmmAlQuraMap( 2008, 5, 6,   1429, 5, 1 ),
                new GregoUmmAlQuraMap( 2008, 6, 5,   1429, 6, 1 ),
                new GregoUmmAlQuraMap( 2008, 7, 4,   1429, 7, 1 ),
                new GregoUmmAlQuraMap( 2008, 8, 2,   1429, 8, 1 ),
                new GregoUmmAlQuraMap( 2008, 9, 1,   1429, 9, 1 ),
                new GregoUmmAlQuraMap( 2008,10, 1,   1429,10, 1 ),
                new GregoUmmAlQuraMap( 2008,10,30,   1429,11, 1 ),
                new GregoUmmAlQuraMap( 2008,11,29,   1429,12, 1 ),
                new GregoUmmAlQuraMap( 2008,12,29,   1430, 1, 1 ),
                new GregoUmmAlQuraMap( 2009, 1,27,   1430, 2, 1 ),
                new GregoUmmAlQuraMap( 2009, 2,26,   1430, 3, 1 ),
                new GregoUmmAlQuraMap( 2009, 3,28,   1430, 4, 1 ),
                new GregoUmmAlQuraMap( 2009, 4,26,   1430, 5, 1 ),
                new GregoUmmAlQuraMap( 2009, 5,25,   1430, 6, 1 ),
                new GregoUmmAlQuraMap( 2009, 6,24,   1430, 7, 1 ),
                new GregoUmmAlQuraMap( 2009, 7,23,   1430, 8, 1 ),
                new GregoUmmAlQuraMap( 2009, 8,22,   1430, 9, 1 ),
                new GregoUmmAlQuraMap( 2009, 9,20,   1430,10, 1 ),
                new GregoUmmAlQuraMap( 2009,10,20,   1430,11, 1 ),
                new GregoUmmAlQuraMap( 2009,11,18,   1430,12, 1 ),
                new GregoUmmAlQuraMap( 2009,12,18,   1431, 1, 1 ),
                new GregoUmmAlQuraMap( 2010, 1,16,   1431, 2, 1 ),
                new GregoUmmAlQuraMap( 2010, 2,15,   1431, 3, 1 ),
                new GregoUmmAlQuraMap( 2010, 3,17,   1431, 4, 1 ),
                new GregoUmmAlQuraMap( 2010, 4,15,   1431, 5, 1 ),
                new GregoUmmAlQuraMap( 2010, 5,15,   1431, 6, 1 ),
                new GregoUmmAlQuraMap( 2010, 6,13,   1431, 7, 1 ),
                new GregoUmmAlQuraMap( 2010, 7,13,   1431, 8, 1 ),
                new GregoUmmAlQuraMap( 2010, 8,11,   1431, 9, 1 ),
                new GregoUmmAlQuraMap( 2010, 9,10,   1431,10, 1 ),
                new GregoUmmAlQuraMap( 2010,10, 9,   1431,11, 1 ),
                new GregoUmmAlQuraMap( 2010,11, 7,   1431,12, 1 ),
                new GregoUmmAlQuraMap( 2010,12, 7,   1432, 1, 1 ),
                new GregoUmmAlQuraMap( 2011, 1, 5,   1432, 2, 1 ),
                new GregoUmmAlQuraMap( 2011, 2, 4,   1432, 3, 1 ),
                new GregoUmmAlQuraMap( 2011, 3, 6,   1432, 4, 1 ),
                new GregoUmmAlQuraMap( 2011, 4, 5,   1432, 5, 1 ),
                new GregoUmmAlQuraMap( 2011, 5, 4,   1432, 6, 1 ),
                new GregoUmmAlQuraMap( 2011, 6, 3,   1432, 7, 1 ),
                new GregoUmmAlQuraMap( 2011, 7, 2,   1432, 8, 1 ),
                new GregoUmmAlQuraMap( 2011, 8, 1,   1432, 9, 1 ),
                new GregoUmmAlQuraMap( 2011, 8,30,   1432,10, 1 ),
                new GregoUmmAlQuraMap( 2011, 9,29,   1432,11, 1 ),
                new GregoUmmAlQuraMap( 2011,10,28,   1432,12, 1 ),
                new GregoUmmAlQuraMap( 2011,11,26,   1433, 1, 1 ),
                new GregoUmmAlQuraMap( 2011,12,26,   1433, 2, 1 ),
                new GregoUmmAlQuraMap( 2012, 1,24,   1433, 3, 1 ),
                new GregoUmmAlQuraMap( 2012, 2,23,   1433, 4, 1 ),
                new GregoUmmAlQuraMap( 2012, 3,24,   1433, 5, 1 ),
                new GregoUmmAlQuraMap( 2012, 4,22,   1433, 6, 1 ),
                new GregoUmmAlQuraMap( 2012, 5,22,   1433, 7, 1 ),
                new GregoUmmAlQuraMap( 2012, 6,21,   1433, 8, 1 ),
                new GregoUmmAlQuraMap( 2012, 7,20,   1433, 9, 1 ),
                new GregoUmmAlQuraMap( 2012, 8,19,   1433,10, 1 ),
                new GregoUmmAlQuraMap( 2012, 9,17,   1433,11, 1 ),
                new GregoUmmAlQuraMap( 2012,10,17,   1433,12, 1 ),
                new GregoUmmAlQuraMap( 2012,11,15,   1434, 1, 1 ),
                new GregoUmmAlQuraMap( 2012,12,14,   1434, 2, 1 ),
                new GregoUmmAlQuraMap( 2013, 1,13,   1434, 3, 1 ),
                new GregoUmmAlQuraMap( 2013, 2,11,   1434, 4, 1 ),
                new GregoUmmAlQuraMap( 2013, 3,13,   1434, 5, 1 ),
                new GregoUmmAlQuraMap( 2013, 4,11,   1434, 6, 1 ),
                new GregoUmmAlQuraMap( 2013, 5,11,   1434, 7, 1 ),
                new GregoUmmAlQuraMap( 2013, 6,10,   1434, 8, 1 ),
                new GregoUmmAlQuraMap( 2013, 7, 9,   1434, 9, 1 ),
                new GregoUmmAlQuraMap( 2013, 8, 8,   1434,10, 1 ),
                new GregoUmmAlQuraMap( 2013, 9, 7,   1434,11, 1 ),
                new GregoUmmAlQuraMap( 2013,10, 6,   1434,12, 1 ),
                new GregoUmmAlQuraMap( 2013,11, 4,   1435, 1, 1 ),
                new GregoUmmAlQuraMap( 2013,12, 4,   1435, 2, 1 ),
                new GregoUmmAlQuraMap( 2014, 1, 2,   1435, 3, 1 ),
                new GregoUmmAlQuraMap( 2014, 2, 1,   1435, 4, 1 ),
                new GregoUmmAlQuraMap( 2014, 3, 2,   1435, 5, 1 ),
                new GregoUmmAlQuraMap( 2014, 4, 1,   1435, 6, 1 ),
                new GregoUmmAlQuraMap( 2014, 4,30,   1435, 7, 1 ),
                new GregoUmmAlQuraMap( 2014, 5,30,   1435, 8, 1 ),
                new GregoUmmAlQuraMap( 2014, 6,28,   1435, 9, 1 ),
                new GregoUmmAlQuraMap( 2014, 7,28,   1435,10, 1 ),
                new GregoUmmAlQuraMap( 2014, 8,27,   1435,11, 1 ),
                new GregoUmmAlQuraMap( 2014, 9,25,   1435,12, 1 ),
                new GregoUmmAlQuraMap( 2014,10,25,   1436, 1, 1 ),
                new GregoUmmAlQuraMap( 2014,11,23,   1436, 2, 1 ),
                new GregoUmmAlQuraMap( 2014,12,23,   1436, 3, 1 ),
                new GregoUmmAlQuraMap( 2015, 1,21,   1436, 4, 1 ),
                new GregoUmmAlQuraMap( 2015, 2,20,   1436, 5, 1 ),
                new GregoUmmAlQuraMap( 2015, 3,21,   1436, 6, 1 ),
                new GregoUmmAlQuraMap( 2015, 4,20,   1436, 7, 1 ),
                new GregoUmmAlQuraMap( 2015, 5,19,   1436, 8, 1 ),
                new GregoUmmAlQuraMap( 2015, 6,18,   1436, 9, 1 ),
                new GregoUmmAlQuraMap( 2015, 7,17,   1436,10, 1 ),
                new GregoUmmAlQuraMap( 2015, 8,16,   1436,11, 1 ),
                new GregoUmmAlQuraMap( 2015, 9,14,   1436,12, 1 ),
                new GregoUmmAlQuraMap( 2015,10,14,   1437, 1, 1 ),
                new GregoUmmAlQuraMap( 2015,11,13,   1437, 2, 1 ),
                new GregoUmmAlQuraMap( 2015,12,12,   1437, 3, 1 ),
                new GregoUmmAlQuraMap( 2016, 1,11,   1437, 4, 1 ),
                new GregoUmmAlQuraMap( 2016, 2,10,   1437, 5, 1 ),
                new GregoUmmAlQuraMap( 2016, 3,10,   1437, 6, 1 ),
                new GregoUmmAlQuraMap( 2016, 4, 8,   1437, 7, 1 ),
                new GregoUmmAlQuraMap( 2016, 5, 8,   1437, 8, 1 ),
                new GregoUmmAlQuraMap( 2016, 6, 6,   1437, 9, 1 ),
                new GregoUmmAlQuraMap( 2016, 7, 6,   1437,10, 1 ),
                new GregoUmmAlQuraMap( 2016, 8, 4,   1437,11, 1 ),
                new GregoUmmAlQuraMap( 2016, 9, 2,   1437,12, 1 ),
                new GregoUmmAlQuraMap( 2016,10, 2,   1438, 1, 1 ),
                new GregoUmmAlQuraMap( 2016,11, 1,   1438, 2, 1 ),
                new GregoUmmAlQuraMap( 2016,11,30,   1438, 3, 1 ),
                new GregoUmmAlQuraMap( 2016,12,30,   1438, 4, 1 ),
                new GregoUmmAlQuraMap( 2017, 1,29,   1438, 5, 1 ),
                new GregoUmmAlQuraMap( 2017, 2,28,   1438, 6, 1 ),
                new GregoUmmAlQuraMap( 2017, 3,29,   1438, 7, 1 ),
                new GregoUmmAlQuraMap( 2017, 4,27,   1438, 8, 1 ),
                new GregoUmmAlQuraMap( 2017, 5,27,   1438, 9, 1 ),
                new GregoUmmAlQuraMap( 2017, 6,25,   1438,10, 1 ),
                new GregoUmmAlQuraMap( 2017, 7,24,   1438,11, 1 ),
                new GregoUmmAlQuraMap( 2017, 8,23,   1438,12, 1 ),
                new GregoUmmAlQuraMap( 2017, 9,21,   1439, 1, 1 ),
                new GregoUmmAlQuraMap( 2017,10,21,   1439, 2, 1 ),
                new GregoUmmAlQuraMap( 2017,11,19,   1439, 3, 1 ),
                new GregoUmmAlQuraMap( 2017,12,19,   1439, 4, 1 ),
                new GregoUmmAlQuraMap( 2018, 1,18,   1439, 5, 1 ),
                new GregoUmmAlQuraMap( 2018, 2,17,   1439, 6, 1 ),
                new GregoUmmAlQuraMap( 2018, 3,18,   1439, 7, 1 ),
                new GregoUmmAlQuraMap( 2018, 4,17,   1439, 8, 1 ),
                new GregoUmmAlQuraMap( 2018, 5,16,   1439, 9, 1 ),
                new GregoUmmAlQuraMap( 2018, 6,15,   1439,10, 1 ),
                new GregoUmmAlQuraMap( 2018, 7,14,   1439,11, 1 ),
                new GregoUmmAlQuraMap( 2018, 8,12,   1439,12, 1 ),
                new GregoUmmAlQuraMap( 2018, 9,11,   1440, 1, 1 ),
                new GregoUmmAlQuraMap( 2019, 8,31,   1441, 1, 1 ),
                new GregoUmmAlQuraMap( 2020, 8,20,   1442, 1, 1 ),
                new GregoUmmAlQuraMap( 2021, 8, 9,   1443, 1, 1 ),
                new GregoUmmAlQuraMap( 2022, 7,30,   1444, 1, 1 ),
                new GregoUmmAlQuraMap( 2023, 7,19,   1445, 1, 1 ),
                new GregoUmmAlQuraMap( 2024, 7, 7,   1446, 1, 1 ),
                new GregoUmmAlQuraMap( 2025, 6,26,   1447, 1, 1 ),
                new GregoUmmAlQuraMap( 2026, 6,16,   1448, 1, 1 ),
                new GregoUmmAlQuraMap( 2027, 6, 6,   1449, 1, 1 ),
                new GregoUmmAlQuraMap( 2028, 5,25,   1450, 1, 1 ),
                new GregoUmmAlQuraMap( 2029, 5,14,   1451, 1, 1 ),
                new GregoUmmAlQuraMap( 2030, 5, 4,   1452, 1, 1 ),
                new GregoUmmAlQuraMap( 2031, 4,23,   1453, 1, 1 ),
                new GregoUmmAlQuraMap( 2032, 4,11,   1454, 1, 1 ),
                new GregoUmmAlQuraMap( 2033, 4, 1,   1455, 1, 1 ),
                new GregoUmmAlQuraMap( 2034, 3,22,   1456, 1, 1 ),
                new GregoUmmAlQuraMap( 2035, 3,11,   1457, 1, 1 ),
                new GregoUmmAlQuraMap( 2036, 2,29,   1458, 1, 1 ),
                new GregoUmmAlQuraMap( 2037, 2,17,   1459, 1, 1 ),
                new GregoUmmAlQuraMap( 2038, 2, 6,   1460, 1, 1 ),
                new GregoUmmAlQuraMap( 2039, 1,26,   1461, 1, 1 ),
                new GregoUmmAlQuraMap( 2040, 1,15,   1462, 1, 1 ),
                new GregoUmmAlQuraMap( 2041, 1, 4,   1463, 1, 1 ),
                new GregoUmmAlQuraMap( 2041,12,25,   1464, 1, 1 ),
                new GregoUmmAlQuraMap( 2042,12,14,   1465, 1, 1 ),
                new GregoUmmAlQuraMap( 2043,12, 3,   1466, 1, 1 ),
                new GregoUmmAlQuraMap( 2044,11,21,   1467, 1, 1 ),
                new GregoUmmAlQuraMap( 2045,11,11,   1468, 1, 1 ),
                new GregoUmmAlQuraMap( 2046,10,31,   1469, 1, 1 ),
                new GregoUmmAlQuraMap( 2047,10,21,   1470, 1, 1 ),
                new GregoUmmAlQuraMap( 2048,10, 9,   1471, 1, 1 ),
                new GregoUmmAlQuraMap( 2049, 9,29,   1472, 1, 1 ),
                new GregoUmmAlQuraMap( 2050, 9,18,   1473, 1, 1 ),
                new GregoUmmAlQuraMap( 2051, 9, 7,   1474, 1, 1 ),
                new GregoUmmAlQuraMap( 2052, 8,26,   1475, 1, 1 ),
                new GregoUmmAlQuraMap( 2053, 8,15,   1476, 1, 1 ),
                new GregoUmmAlQuraMap( 2054, 8, 5,   1477, 1, 1 ),
                new GregoUmmAlQuraMap( 2055, 7,26,   1478, 1, 1 ),
                new GregoUmmAlQuraMap( 2056, 7,14,   1479, 1, 1 ),
                new GregoUmmAlQuraMap( 2057, 7, 3,   1480, 1, 1 ),
                new GregoUmmAlQuraMap( 2058, 6,22,   1481, 1, 1 ),
                new GregoUmmAlQuraMap( 2059, 6,11,   1482, 1, 1 ),
                new GregoUmmAlQuraMap( 2061, 5,21,   1484, 1, 1 ),
                new GregoUmmAlQuraMap( 2063, 4,30,   1486, 1, 1 ),
                new GregoUmmAlQuraMap( 2065, 4, 7,   1488, 1, 1 ),
                new GregoUmmAlQuraMap( 2067, 3,17,   1490, 1, 1 ),
                new GregoUmmAlQuraMap( 2069, 2,23,   1492, 1, 1 ),
                new GregoUmmAlQuraMap( 2071, 2, 2,   1494, 1, 1 ),
                new GregoUmmAlQuraMap( 2073, 1,10,   1496, 1, 1 ),
                new GregoUmmAlQuraMap( 2074,12,20,   1498, 1, 1 ),
                new GregoUmmAlQuraMap( 2076,11,28,   1500, 1, 1 ),
        };


        int firstYear = 1318;
        //*  use either 1 or 2 leading slashes to toggle
        int lastYear = 1368;    // just enough to be pretty sure
        /*/
        int lastYear = 1480;    // the whole shootin' match
        //*/
        
        ULocale umalquraLoc = new ULocale("ar_SA@calendar=islamic-umalqura"); 
        ULocale gregoLoc = new ULocale("ar_SA@calendar=gregorian"); 
        TimeZone tzSA = TimeZone.getTimeZone("Asia/Riyadh");
        IslamicCalendar tstCal = new IslamicCalendar(tzSA, umalquraLoc);
        GregorianCalendar gregCal = new GregorianCalendar(tzSA, gregoLoc);
        tstCal.clear();
        tstCal.setLenient(false);
        
        int day=0, month=0, year=0, initDay = 27, initMonth = IslamicCalendar.RAJAB, initYear = 1434;

        try {
            for( int startYear = firstYear; startYear <= lastYear; startYear++) {
                setAndTestWholeYear(tstCal, startYear);
            }
        } catch(Throwable t) {
            errln("unexpected exception thrown - message=" +t.getLocalizedMessage());
        }

        try {
            initMonth = IslamicCalendar.RABI_2;
            initDay = 5;
            int loopCnt = 25;
            tstCal.clear();
            setAndTestCalendar( tstCal, initMonth, initDay, initYear);
            for(int x=1; x<=loopCnt; x++) {
                day = tstCal.get(Calendar.DAY_OF_MONTH);
                month = tstCal.get(Calendar.MONTH);
                year = tstCal.get(Calendar.YEAR);
                tstCal.roll(Calendar.DAY_OF_MONTH, true);
            }
            if(day != (initDay + loopCnt - 1) || month != IslamicCalendar.RABI_2 || year != 1434)
                errln("invalid values for RABI_2 date after roll of " + loopCnt);
        } catch(IllegalArgumentException iae) {
            errln("unexpected exception received!!!");
        }
        
        try {
            tstCal.clear();
            initMonth = 2;
            initDay = 30;
            setAndTestCalendar( tstCal, initMonth, initDay, initYear);
            errln("expected exception NOT thrown");
        } catch(IllegalArgumentException iae) {
            // expected this
        }
        
        try {
            tstCal.clear();
            initMonth = 3;
            initDay = 30;
            setAndTestCalendar( tstCal, initMonth, initDay, initYear);
        } catch(IllegalArgumentException iae) {
            errln("unexpected exception received!!!");
        }
        
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");            
            Date date = formatter.parse("1975-05-06");
            IslamicCalendar is_cal = new IslamicCalendar();
            is_cal.setCalculationType(CalculationType.ISLAMIC_UMALQURA);
            is_cal.setTime(date);
            SimpleDateFormat formatterIslamic = (SimpleDateFormat) is_cal.getDateTimeFormat(0,0,umalquraLoc);
            formatterIslamic.applyPattern("yyyy-MMMM-dd");
            String str = formatterIslamic.format(is_cal.getTime());

            // 1395 - Rabi - 24
            int is_day = is_cal.get(Calendar.DAY_OF_MONTH);
            int is_month = is_cal.get(Calendar.MONTH);
            int is_year = is_cal.get(Calendar.YEAR);
            if(is_day != 24 || is_month != IslamicCalendar.RABI_2 || is_year != 1395)
                errln("unexpected conversion date: "+is_day+" "+is_month+" "+is_year);

            String expectedFormatResult = "\u0661\u0663\u0669\u0665-\u0631\u0628\u064A\u0639 \u0627\u0644\u0622\u062E\u0631-\u0662\u0664";
            if(!str.equals(expectedFormatResult))
                errln("unexpected formatted result: "+str);
            
        }catch(Exception e){
            errln(e.getLocalizedMessage());
        }
        
        // check against data
        gregCal.clear();
        tstCal.clear();
        for (GregoUmmAlQuraMap guMap: guMappings) {
            gregCal.set(guMap.gYear, guMap.gMon - 1, guMap.gDay, 12, 0);
            long mapDate = gregCal.getTimeInMillis();
            tstCal.setTimeInMillis(mapDate);
            int uYear = tstCal.get(Calendar.YEAR);
            int uMon  = tstCal.get(Calendar.MONTH) + 1;
            int uDay  = tstCal.get(Calendar.DATE);
            if ( uYear != guMap.uYear || uMon != guMap.uMon || uDay != guMap.uDay ) {
                errln("For gregorian "     + guMap.gYear+"-"+guMap.gMon+"-"+guMap.gDay+
                      ", expect umalqura " + guMap.uYear+"-"+guMap.uMon+"-"+guMap.uDay+
                      ", get "             +       uYear+"-"+      uMon+"-"+      uDay);
            }
        }
    }
    
    @Test
    public void TestSerialization8449() {
        try {
            ByteArrayOutputStream icuStream = new ByteArrayOutputStream();
    
            IslamicCalendar tstCalendar = new IslamicCalendar();
            tstCalendar.setCivil(false);
            
            long expectMillis = 1187912520931L; // with seconds (not ms) cleared.
            tstCalendar.setTimeInMillis(expectMillis);
            
            logln("instantiated: "+tstCalendar);
            logln("getMillis: "+tstCalendar.getTimeInMillis());
            tstCalendar.set(IslamicCalendar.SECOND, 0);
            logln("setSecond=0: "+tstCalendar);
            {
                long gotMillis = tstCalendar.getTimeInMillis();
                if(gotMillis != expectMillis) {
                    errln("expect millis "+expectMillis+" but got "+gotMillis);
                } else {
                    logln("getMillis: "+gotMillis);
                }
            }
            ObjectOutputStream icuOut = new ObjectOutputStream(icuStream);
            icuOut.writeObject(tstCalendar);
            icuOut.flush();
            icuOut.close();
            
            ObjectInputStream icuIn = new ObjectInputStream(new ByteArrayInputStream(icuStream.toByteArray()));
            tstCalendar = null;
            tstCalendar = (IslamicCalendar)icuIn.readObject();
            
            logln("serialized back in: "+tstCalendar);
            {
                long gotMillis = tstCalendar.getTimeInMillis();
                if(gotMillis != expectMillis) {
                    errln("expect millis "+expectMillis+" but got "+gotMillis);
                } else {
                    logln("getMillis: "+gotMillis);
                }
            }
            
            tstCalendar.set(IslamicCalendar.SECOND, 0);
                    
            logln("setSecond=0: "+tstCalendar);
            {
                long gotMillis = tstCalendar.getTimeInMillis();
                if(gotMillis != expectMillis) {
                    errln("expect millis "+expectMillis+" after stream and setSecond but got "+gotMillis);
                } else {
                    logln("getMillis after stream and setSecond: "+gotMillis);
                }
            }
        } catch(IOException e) {
            errln(e.toString());
            e.printStackTrace();
        } catch(ClassNotFoundException cnf) {
            errln(cnf.toString());
            cnf.printStackTrace();
        }
    }
    
    @Test
    public void TestIslamicTabularDates() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = formatter.parse("1975-05-06");
        }catch(Exception e){
            errln("unable to parse test date string - errMsg:" +e.getLocalizedMessage());
        }

        IslamicCalendar is_cal = new IslamicCalendar();
        is_cal.setCalculationType(CalculationType.ISLAMIC_CIVIL);
        is_cal.setTime(date);
        IslamicCalendar is_cal2 = new IslamicCalendar();
        is_cal2.setCalculationType(CalculationType.ISLAMIC_TBLA);
        is_cal2.setTime(date);

        int is_month = is_cal.get(Calendar.MONTH);
        int is_month2 = is_cal2.get(Calendar.MONTH);
        int is_year = is_cal.get(Calendar.YEAR);
        int is_year2 = is_cal2.get(Calendar.YEAR);
        if( (is_month != is_month2) || (is_year != is_year2))
            errln("unexpected difference between islamic and tbla month "+is_month+" : "+is_month2+" and/or year "+is_year+" : "+is_year2);
        
        int is_day = is_cal.get(Calendar.DAY_OF_MONTH);
        int is_day2 = is_cal2.get(Calendar.DAY_OF_MONTH);
        if(is_day2 - is_day != 1)
            errln("unexpected difference between civil and tbla: "+is_day2+" : "+is_day);

    }

    @Test
    public void TestCreationByLocale() {
        ULocale islamicLoc = new ULocale("ar_SA@calendar=islamic-umalqura"); 
        IslamicCalendar is_cal = new IslamicCalendar(islamicLoc);
        String thisCalcType = is_cal.getType(); 
        if(!"islamic-umalqura".equalsIgnoreCase(thisCalcType)) {
            errln("non umalqura calc type generated - " + thisCalcType);
        }

        islamicLoc = new ULocale("ar_SA@calendar=islamic-civil"); 
        is_cal = new IslamicCalendar(islamicLoc);
        thisCalcType = is_cal.getType(); 
        if(!"islamic-civil".equalsIgnoreCase(thisCalcType)) {
            errln("non civil calc type generated - " + thisCalcType);
        }

        islamicLoc = new ULocale("ar_SA@calendar=islamic-tbla"); 
        is_cal = new IslamicCalendar(islamicLoc);
        thisCalcType = is_cal.getType(); 
        if(!"islamic-tbla".equalsIgnoreCase(thisCalcType)) {
            errln("non tbla calc type generated - " + thisCalcType);
        }

        islamicLoc = new ULocale("ar_SA@calendar=islamic-xyzzy"); 
        is_cal = new IslamicCalendar(islamicLoc);
        thisCalcType = is_cal.getType(); 
        if(!"islamic".equalsIgnoreCase(thisCalcType)) {
            errln("incorrect default calc type generated - " + thisCalcType);
        }

    }

}
