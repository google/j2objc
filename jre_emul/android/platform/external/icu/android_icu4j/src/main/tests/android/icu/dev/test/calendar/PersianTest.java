/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2012-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.calendar;
import java.util.Date;

import org.junit.Test;

import android.icu.util.Calendar;
import android.icu.util.PersianCalendar;
import android.icu.util.ULocale;

public class PersianTest extends CalendarTestFmwk {
    /**
     * Test basic mapping to and from Gregorian.
     */
    @Test
    public void TestMapping() {
        final int[] DATA = {
            // (Note: months are 1-based)
            2011, 1, 11, 1389, 10, 21,
            1986, 2, 25, 1364, 12, 6, 
            1934, 3, 14, 1312, 12, 23,

            2090, 3, 19, 1468, 12, 29,
            2007, 2, 22, 1385, 12, 3,
            1969, 12, 31, 1348, 10, 10,
            1945, 11, 12, 1324, 8, 21,
            1925, 3, 31, 1304, 1, 11,

            1996, 3, 19, 1374, 12, 29,
            1996, 3, 20, 1375, 1, 1,
            1997, 3, 20, 1375, 12, 30,
            1997, 3, 21, 1376, 1, 1,

            2008, 3, 19, 1386, 12, 29,
            2008, 3, 20, 1387, 1, 1,
            2004, 3, 19, 1382, 12, 29,
            2004, 3, 20, 1383, 1, 1,

            2006, 3, 20, 1384, 12, 29,
            2006, 3, 21, 1385, 1, 1,

            2005, 4, 20, 1384, 1, 31,
            2005, 4, 21, 1384, 2, 1,
            2005, 5, 21, 1384, 2, 31,
            2005, 5, 22, 1384, 3, 1,
            2005, 6, 21, 1384, 3, 31,
            2005, 6, 22, 1384, 4, 1,
            2005, 7, 22, 1384, 4, 31,
            2005, 7, 23, 1384, 5, 1,
            2005, 8, 22, 1384, 5, 31,
            2005, 8, 23, 1384, 6, 1,
            2005, 9, 22, 1384, 6, 31,
            2005, 9, 23, 1384, 7, 1,
            2005, 10, 22, 1384, 7, 30,
            2005, 10, 23, 1384, 8, 1,
            2005, 11, 21, 1384, 8, 30,
            2005, 11, 22, 1384, 9, 1,
            2005, 12, 21, 1384, 9, 30,
            2005, 12, 22, 1384, 10, 1,
            2006, 1, 20, 1384, 10, 30,
            2006, 1, 21, 1384, 11, 1,
            2006, 2, 19, 1384, 11, 30,
            2006, 2, 20, 1384, 12, 1,
            2006, 3, 20, 1384, 12, 29,
            2006, 3, 21, 1385, 1, 1,

            // The 2820-year cycle arithmetical algorithm would fail this one.
            2025, 3, 21, 1404, 1, 1,
        };

        Calendar cal = Calendar.getInstance(new ULocale("fa_IR@calendar=persian"));
        StringBuilder buf = new StringBuilder();

        logln("Gregorian -> Persian");

        Calendar grego = Calendar.getInstance();
        grego.clear();
        for (int i = 0; i < DATA.length;) {
            grego.set(DATA[i++], DATA[i++] - 1, DATA[i++]);
            Date date = grego.getTime();
            cal.setTime(date);
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH) + 1; // 0-based -> 1-based
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int yE = DATA[i++]; // Expected y, m, d
            int mE = DATA[i++]; // 1-based
            int dE = DATA[i++];
            buf.setLength(0);
            buf.append(date + " -> ");
            buf.append(y + "/" + m + "/" + d);
            if (y == yE && m == mE && d == dE) {
                logln("OK: " + buf.toString());
            } else {
                errln("Fail: " + buf.toString() + ", expected " + yE + "/" + mE + "/" + dE);
            }
        }

        logln("Persian -> Gregorian");
        for (int i = 0; i < DATA.length;) {
            grego.set(DATA[i++], DATA[i++] - 1, DATA[i++]);
            Date dexp = grego.getTime();
            int cyear = DATA[i++];
            int cmonth = DATA[i++];
            int cdayofmonth = DATA[i++];
            cal.clear();
            cal.set(Calendar.YEAR, cyear);
            cal.set(Calendar.MONTH, cmonth - 1);
            cal.set(Calendar.DAY_OF_MONTH, cdayofmonth);
            Date date = cal.getTime();
            buf.setLength(0);
            buf.append(cyear + "/" + cmonth + "/" + cdayofmonth);
            buf.append(" -> " + date);
            if (date.equals(dexp)) {
                logln("OK: " + buf.toString());
            } else {
                errln("Fail: " + buf.toString() + ", expected " + dexp);
            }
        }
    }

    public void TestCoverage12424() {
        class StubCalendar extends PersianCalendar {   
            private static final long serialVersionUID = 1L;
            public StubCalendar() {
                assertEquals("Persian month 0 length", 31, handleGetMonthLength(1000, 0));
                assertEquals("Persian month 7 length", 30, handleGetMonthLength(1000, 7));
                
                int leastWeeks = handleGetLimit(Calendar.WEEK_OF_YEAR, Calendar.LEAST_MAXIMUM);
                assertEquals("Persian Week of Year least maximum", 52, leastWeeks);             
             }
        }
        
        new StubCalendar();
    }
}
