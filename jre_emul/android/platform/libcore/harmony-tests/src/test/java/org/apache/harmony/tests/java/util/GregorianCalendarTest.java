/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.util;

import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Vector;

public class GregorianCalendarTest extends junit.framework.TestCase {

    private static final TimeZone AMERICA_CHICAGO = TimeZone.getTimeZone("America/Chicago");
    private static final TimeZone AMERICA_NEW_YORK = TimeZone.getTimeZone("America/New_York");

    private Locale defaultLocale;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        defaultLocale = Locale.getDefault();
        // Most tests are locale independent, but locale does affect start-of-week.
        Locale.setDefault(Locale.US);
    }

    @Override
    public void tearDown() throws Exception {
        Locale.setDefault(defaultLocale);
        super.tearDown();
    }

    /**
     * java.util.GregorianCalendar#GregorianCalendar()
     */
    public void test_Constructor() {
        // Test for method java.util.GregorianCalendar()
        assertTrue("Constructed incorrect calendar", (new GregorianCalendar()
                .isLenient()));
    }

    /**
     * java.util.GregorianCalendar#GregorianCalendar(int, int, int)
     */
    public void test_ConstructorIII() {
        // Test for method java.util.GregorianCalendar(int, int, int)
        GregorianCalendar gc = new GregorianCalendar(1972, Calendar.OCTOBER, 13);
        assertEquals("Incorrect calendar constructed 1",
                1972, gc.get(Calendar.YEAR));
        assertTrue("Incorrect calendar constructed 2",
                gc.get(Calendar.MONTH) == Calendar.OCTOBER);
        assertEquals("Incorrect calendar constructed 3", 13, gc
                .get(Calendar.DAY_OF_MONTH));
        assertTrue("Incorrect calendar constructed 4", gc.getTimeZone().equals(
                TimeZone.getDefault()));
    }

    /**
     * java.util.GregorianCalendar#GregorianCalendar(int, int, int, int,
     *int)
     */
    public void test_ConstructorIIIII() {
        // Test for method java.util.GregorianCalendar(int, int, int, int, int)
        GregorianCalendar gc = new GregorianCalendar(1972, Calendar.OCTOBER,
                13, 19, 9);
        assertEquals("Incorrect calendar constructed",
                1972, gc.get(Calendar.YEAR));
        assertTrue("Incorrect calendar constructed",
                gc.get(Calendar.MONTH) == Calendar.OCTOBER);
        assertEquals("Incorrect calendar constructed", 13, gc
                .get(Calendar.DAY_OF_MONTH));
        assertEquals("Incorrect calendar constructed", 7, gc.get(Calendar.HOUR));
        assertEquals("Incorrect calendar constructed",
                1, gc.get(Calendar.AM_PM));
        assertEquals("Incorrect calendar constructed",
                9, gc.get(Calendar.MINUTE));
        assertTrue("Incorrect calendar constructed", gc.getTimeZone().equals(
                TimeZone.getDefault()));

        //Regression for HARMONY-998
        gc = new GregorianCalendar(1900, 0, 0, 0, Integer.MAX_VALUE);
        assertEquals("Incorrect calendar constructed",
                5983, gc.get(Calendar.YEAR));
    }

    /**
     * java.util.GregorianCalendar#GregorianCalendar(int, int, int, int,
     *int, int)
     */
    public void test_ConstructorIIIIII() {
        // Test for method java.util.GregorianCalendar(int, int, int, int, int,
        // int)
        GregorianCalendar gc = new GregorianCalendar(1972, Calendar.OCTOBER,
                13, 19, 9, 59);
        assertEquals("Incorrect calendar constructed",
                1972, gc.get(Calendar.YEAR));
        assertTrue("Incorrect calendar constructed",
                gc.get(Calendar.MONTH) == Calendar.OCTOBER);
        assertEquals("Incorrect calendar constructed", 13, gc
                .get(Calendar.DAY_OF_MONTH));
        assertEquals("Incorrect calendar constructed", 7, gc.get(Calendar.HOUR));
        assertEquals("Incorrect calendar constructed",
                1, gc.get(Calendar.AM_PM));
        assertEquals("Incorrect calendar constructed",
                9, gc.get(Calendar.MINUTE));
        assertEquals("Incorrect calendar constructed",
                59, gc.get(Calendar.SECOND));
        assertTrue("Incorrect calendar constructed", gc.getTimeZone().equals(
                TimeZone.getDefault()));
    }

    /**
     * java.util.GregorianCalendar#GregorianCalendar(java.util.Locale)
     */
    public void test_ConstructorLjava_util_Locale() {
        // Test for method java.util.GregorianCalendar(java.util.Locale)
        Date date = new Date();
        GregorianCalendar gcJapan = new GregorianCalendar(Locale.JAPAN);
        gcJapan.setTime(date);
        GregorianCalendar gcJapan2 = new GregorianCalendar(Locale.JAPAN);
        gcJapan2.setTime(date);
        GregorianCalendar gcItaly = new GregorianCalendar(Locale.ITALY);
        gcItaly.setTime(date);
        assertTrue("Locales not created correctly", gcJapan.equals(gcJapan2)
                && !gcJapan.equals(gcItaly));
    }

    /**
     * java.util.GregorianCalendar#GregorianCalendar(java.util.TimeZone)
     */
    public void test_ConstructorLjava_util_TimeZone() {
        // Test for method java.util.GregorianCalendar(java.util.TimeZone)
        Date date = new Date(2008, 1, 1);
        TimeZone.getDefault();
        GregorianCalendar gc1 = new GregorianCalendar(AMERICA_NEW_YORK);
        gc1.setTime(date);
        GregorianCalendar gc2 = new GregorianCalendar(AMERICA_CHICAGO);
        gc2.setTime(date);
        // Chicago is 1 hour before New York, add 1 to the Chicago time and convert to 0-12 value
        assertEquals("Incorrect calendar returned",
                gc1.get(Calendar.HOUR), ((gc2.get(Calendar.HOUR) + 1) % 12));

        // Regression test for HARMONY-2961
        SimpleTimeZone timezone = new SimpleTimeZone(-3600 * 24 * 1000 * 2,
                "GMT");
        GregorianCalendar gc = new GregorianCalendar(timezone);

        // Regression test for HARMONY-5195
        Calendar c1 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        c1.set(Calendar.YEAR, 1999);
        c1.set(Calendar.MONTH, Calendar.JUNE);
        c1.set(Calendar.DAY_OF_MONTH, 2);
        c1.set(Calendar.HOUR, 15);
        c1.set(Calendar.MINUTE, 34);
        c1.set(Calendar.SECOND, 16);
        assertEquals(34, c1.get(Calendar.MINUTE));
        c1.setTimeZone(new SimpleTimeZone(60000, "ONE MINUTE"));
        assertEquals(35, c1.get(Calendar.MINUTE));
    }

    /**
     * java.util.GregorianCalendar#GregorianCalendar(java.util.TimeZone,
     *java.util.Locale)
     */
    public void test_ConstructorLjava_util_TimeZoneLjava_util_Locale() {
        // Test for method java.util.GregorianCalendar(java.util.TimeZone,
        // java.util.Locale)
        Date date = new Date(2008, 1, 1);
        TimeZone.getDefault();
        GregorianCalendar gc1 = new GregorianCalendar(AMERICA_NEW_YORK, Locale.JAPAN);
        gc1.setTime(date);
        GregorianCalendar gc2 = new GregorianCalendar(AMERICA_NEW_YORK, Locale.JAPAN);
        gc2.setTime(date);
        GregorianCalendar gc3 = new GregorianCalendar(AMERICA_CHICAGO, Locale.ITALY);
        gc3.setTime(date);
        // Chicago is 1 hour before New York, add 1 to the Chicago time and convert to 0-12 value
        assertEquals("Incorrect calendar returned",
                gc1.get(Calendar.HOUR), ((gc3.get(Calendar.HOUR) + 1) % 12));
        assertTrue("Locales not created correctly", gc1.equals(gc2)
                && !gc1.equals(gc3));
    }

    /**
     * java.util.GregorianCalendar#add(int, int)
     */
    public void test_addII() {
        // Test for method void java.util.GregorianCalendar.add(int, int)
        GregorianCalendar gc1 = new GregorianCalendar(1998, 11, 6);
        gc1.add(GregorianCalendar.YEAR, 1);
        assertEquals("Add failed to Increment",
                1999, gc1.get(GregorianCalendar.YEAR));

        gc1 = new GregorianCalendar(1999, Calendar.JULY, 31);
        gc1.add(Calendar.MONTH, 7);
        assertEquals("Wrong result year 1", 2000, gc1.get(Calendar.YEAR));
        assertTrue("Wrong result month 1",
                gc1.get(Calendar.MONTH) == Calendar.FEBRUARY);
        assertEquals("Wrong result date 1", 29, gc1.get(Calendar.DATE));

        gc1.add(Calendar.YEAR, -1);
        assertEquals("Wrong result year 2", 1999, gc1.get(Calendar.YEAR));
        assertTrue("Wrong result month 2",
                gc1.get(Calendar.MONTH) == Calendar.FEBRUARY);
        assertEquals("Wrong result date 2", 28, gc1.get(Calendar.DATE));

        gc1 = new GregorianCalendar(AMERICA_NEW_YORK);
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.MILLISECOND, 24 * 60 * 60 * 1000);
        assertEquals("Wrong time after MILLISECOND change", 17, gc1
                .get(Calendar.HOUR_OF_DAY));
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.SECOND, 24 * 60 * 60);
        assertEquals("Wrong time after SECOND change", 17, gc1
                .get(Calendar.HOUR_OF_DAY));
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.MINUTE, 24 * 60);
        assertEquals("Wrong time after MINUTE change", 17, gc1
                .get(Calendar.HOUR_OF_DAY));
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.HOUR, 24);
        assertEquals("Wrong time after HOUR change", 17, gc1
                .get(Calendar.HOUR_OF_DAY));
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.HOUR_OF_DAY, 24);
        assertEquals("Wrong time after HOUR_OF_DAY change", 17, gc1
                .get(Calendar.HOUR_OF_DAY));

        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.AM_PM, 2);
        assertEquals("Wrong time after AM_PM change", 16, gc1
                .get(Calendar.HOUR_OF_DAY));
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.DATE, 1);
        assertEquals("Wrong time after DATE change", 16, gc1
                .get(Calendar.HOUR_OF_DAY));
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.DAY_OF_YEAR, 1);
        assertEquals("Wrong time after DAY_OF_YEAR change", 16, gc1
                .get(Calendar.HOUR_OF_DAY));
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.DAY_OF_WEEK, 1);
        assertEquals("Wrong time after DAY_OF_WEEK change", 16, gc1
                .get(Calendar.HOUR_OF_DAY));
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.WEEK_OF_YEAR, 1);
        assertEquals("Wrong time after WEEK_OF_YEAR change", 16, gc1
                .get(Calendar.HOUR_OF_DAY));
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.WEEK_OF_MONTH, 1);
        assertEquals("Wrong time after WEEK_OF_MONTH change", 16, gc1
                .get(Calendar.HOUR_OF_DAY));
        gc1.set(1999, Calendar.APRIL, 3, 16, 0); // day before DST change
        gc1.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        assertEquals("Wrong time after DAY_OF_WEEK_IN_MONTH change", 16, gc1
                .get(Calendar.HOUR_OF_DAY));

        gc1.clear();
        gc1.set(2000, Calendar.APRIL, 1, 23, 0);
        gc1.add(Calendar.DATE, 1);
        assertTrue("Wrong time after DATE change near DST boundary", gc1
                .get(Calendar.MONTH) == Calendar.APRIL
                && gc1.get(Calendar.DATE) == 2
                && gc1.get(Calendar.HOUR_OF_DAY) == 23);
    }

    /**
     * java.util.GregorianCalendar#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean
        // java.util.GregorianCalendar.equals(java.lang.Object)
        GregorianCalendar gc1 = new GregorianCalendar(1998, 11, 6);
        GregorianCalendar gc2 = new GregorianCalendar(2000, 11, 6);
        GregorianCalendar gc3 = new GregorianCalendar(1998, 11, 6);
        assertTrue("Equality check failed", gc1.equals(gc3));
        assertTrue("Equality check failed", !gc1.equals(gc2));
        gc3.setGregorianChange(new Date());
        assertTrue("Different gregorian change", !gc1.equals(gc3));
    }

    /**
     * java.util.GregorianCalendar#getActualMaximum(int)
     */
    public void test_getActualMaximumI() {
        // Test for method int java.util.GregorianCalendar.getActualMaximum(int)
        GregorianCalendar gc1 = new GregorianCalendar(1900, 1, 1);
        GregorianCalendar gc2 = new GregorianCalendar(1996, 1, 1);
        GregorianCalendar gc3 = new GregorianCalendar(1997, 1, 1);
        GregorianCalendar gc4 = new GregorianCalendar(2000, 1, 1);
        GregorianCalendar gc5 = new GregorianCalendar(2000, 9, 9);
        GregorianCalendar gc6 = new GregorianCalendar(2000, 3, 3);
        assertEquals("Wrong actual maximum value for DAY_OF_MONTH for Feb 1900",
                28, gc1.getActualMaximum(Calendar.DAY_OF_MONTH));
        assertEquals("Wrong actual maximum value for DAY_OF_MONTH for Feb 1996",
                29, gc2.getActualMaximum(Calendar.DAY_OF_MONTH));
        assertEquals("Wrong actual maximum value for DAY_OF_MONTH for Feb 1998",
                28, gc3.getActualMaximum(Calendar.DAY_OF_MONTH));
        assertEquals("Wrong actual maximum value for DAY_OF_MONTH for Feb 2000",
                29, gc4.getActualMaximum(Calendar.DAY_OF_MONTH));
        assertEquals("Wrong actual maximum value for DAY_OF_MONTH for Oct 2000",
                31, gc5.getActualMaximum(Calendar.DAY_OF_MONTH));
        assertEquals("Wrong actual maximum value for DAY_OF_MONTH for Apr 2000",
                30, gc6.getActualMaximum(Calendar.DAY_OF_MONTH));
        assertTrue("Wrong actual maximum value for MONTH", gc1
                .getActualMaximum(Calendar.MONTH) == Calendar.DECEMBER);
        assertEquals("Wrong actual maximum value for HOUR_OF_DAY", 23, gc1
                .getActualMaximum(Calendar.HOUR_OF_DAY));
        assertEquals("Wrong actual maximum value for HOUR", 11, gc1
                .getActualMaximum(Calendar.HOUR));
        assertEquals("Wrong actual maximum value for DAY_OF_WEEK_IN_MONTH", 4, gc6
                .getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH));


        // Regression test for harmony 2954
        Date date = new Date(Date.parse("Jan 15 00:00:01 GMT 2000"));
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(Date.parse("Dec 15 00:00:01 GMT 1582"));
        assertEquals(355, gc.getActualMaximum(Calendar.DAY_OF_YEAR));
        gc.setGregorianChange(date);
        gc.setTimeInMillis(Date.parse("Jan 16 00:00:01 GMT 2000"));
        assertEquals(353, gc.getActualMaximum(Calendar.DAY_OF_YEAR));

        //Regression test for HARMONY-3004
        gc = new GregorianCalendar(1900, 7, 1);
        String[] ids = TimeZone.getAvailableIDs();
        for (int i = 0; i < ids.length; i++) {
            TimeZone tz = TimeZone.getTimeZone(ids[i]);
            gc.setTimeZone(tz);
            for (int j = 1900; j < 2000; j++) {
                gc.set(Calendar.YEAR, j);
                assertEquals(7200000, gc.getActualMaximum(Calendar.DST_OFFSET));
            }
        }
    }

    /**
     * java.util.GregorianCalendar#getActualMinimum(int)
     */
    public void test_getActualMinimumI() {
        // Test for method int java.util.GregorianCalendar.getActualMinimum(int)
        GregorianCalendar gc1 = new GregorianCalendar(1900, 1, 1);
        new GregorianCalendar(1996, 1, 1);
        new GregorianCalendar(1997, 1, 1);
        new GregorianCalendar(2000, 1, 1);
        new GregorianCalendar(2000, 9, 9);
        GregorianCalendar gc6 = new GregorianCalendar(2000, 3, 3);
        assertEquals("Wrong actual minimum value for DAY_OF_MONTH for Feb 1900",
                1, gc1.getActualMinimum(Calendar.DAY_OF_MONTH));
        assertTrue("Wrong actual minimum value for MONTH", gc1
                .getActualMinimum(Calendar.MONTH) == Calendar.JANUARY);
        assertEquals("Wrong actual minimum value for HOUR_OF_DAY", 0, gc1
                .getActualMinimum(Calendar.HOUR_OF_DAY));
        assertEquals("Wrong actual minimum value for HOUR", 0, gc1
                .getActualMinimum(Calendar.HOUR));
        assertEquals("Wrong actual minimum value for DAY_OF_WEEK_IN_MONTH", 1, gc6
                .getActualMinimum(Calendar.DAY_OF_WEEK_IN_MONTH));
    }

    /**
     * java.util.GregorianCalendar#getGreatestMinimum(int)
     */
    public void test_getGreatestMinimumI() {
        // Test for method int
        // java.util.GregorianCalendar.getGreatestMinimum(int)
        GregorianCalendar gc = new GregorianCalendar();
        assertEquals("Wrong greatest minimum value for DAY_OF_MONTH", 1, gc
                .getGreatestMinimum(Calendar.DAY_OF_MONTH));
        assertTrue("Wrong greatest minimum value for MONTH", gc
                .getGreatestMinimum(Calendar.MONTH) == Calendar.JANUARY);
        assertEquals("Wrong greatest minimum value for HOUR_OF_DAY", 0, gc
                .getGreatestMinimum(Calendar.HOUR_OF_DAY));
        assertEquals("Wrong greatest minimum value for HOUR", 0, gc
                .getGreatestMinimum(Calendar.HOUR));

        BitSet result = new BitSet();
        int[] min = { 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, -46800000,
                0 };
        for (int i = 0; i < min.length; i++) {
            if (gc.getGreatestMinimum(i) != min[i])
                result.set(i);
        }
        assertTrue("Wrong greatest min for " + result, result.length() == 0);
    }

    /**
     * java.util.GregorianCalendar#getGregorianChange()
     */
    public void test_getGregorianChange() {
        // Test for method java.util.Date
        // java.util.GregorianCalendar.getGregorianChange()
        GregorianCalendar gc = new GregorianCalendar();
        GregorianCalendar returnedChange = new GregorianCalendar(AMERICA_NEW_YORK);
        returnedChange.setTime(gc.getGregorianChange());
        assertEquals("Returned incorrect year",
                1582, returnedChange.get(Calendar.YEAR));
        assertTrue("Returned incorrect month", returnedChange
                .get(Calendar.MONTH) == Calendar.OCTOBER);
        assertEquals("Returned incorrect day of month", 4, returnedChange
                .get(Calendar.DAY_OF_MONTH));
    }

    /**
     * java.util.GregorianCalendar#getLeastMaximum(int)
     */
    public void test_getLeastMaximumI() {
        // Test for method int java.util.GregorianCalendar.getLeastMaximum(int)
        GregorianCalendar gc = new GregorianCalendar();
        assertEquals("Wrong least maximum value for DAY_OF_MONTH", 28, gc
                .getLeastMaximum(Calendar.DAY_OF_MONTH));
        assertTrue("Wrong least maximum value for MONTH", gc
                .getLeastMaximum(Calendar.MONTH) == Calendar.DECEMBER);
        assertEquals("Wrong least maximum value for HOUR_OF_DAY", 23, gc
                .getLeastMaximum(Calendar.HOUR_OF_DAY));
        assertEquals("Wrong least maximum value for HOUR", 11, gc
                .getLeastMaximum(Calendar.HOUR));

        BitSet result = new BitSet();
        Vector values = new Vector();
        int[] max = { 1, 292269054, 11, 50, 3, 28, 355, 7, 3, 1, 11, 23, 59,
                59, 999, 50400000, 1200000 };
        for (int i = 0; i < max.length; i++) {
            if (gc.getLeastMaximum(i) != max[i]) {
                result.set(i);
                values.add(new Integer(gc.getLeastMaximum(i)));
            }
        }
        assertTrue("Wrong least max for " + result + " = " + values, result
                .length() == 0);
    }

    /**
     * java.util.GregorianCalendar#getMaximum(int)
     */
    public void test_getMaximumI() {
        // Test for method int java.util.GregorianCalendar.getMaximum(int)
        GregorianCalendar gc = new GregorianCalendar();
        assertEquals("Wrong maximum value for DAY_OF_MONTH", 31, gc
                .getMaximum(Calendar.DAY_OF_MONTH));
        assertTrue("Wrong maximum value for MONTH", gc
                .getMaximum(Calendar.MONTH) == Calendar.DECEMBER);
        assertEquals("Wrong maximum value for HOUR_OF_DAY", 23, gc
                .getMaximum(Calendar.HOUR_OF_DAY));
        assertEquals("Wrong maximum value for HOUR",
                11, gc.getMaximum(Calendar.HOUR));

        BitSet result = new BitSet();
        Vector values = new Vector();
        int[] max = { 1, 292278994, 11, 53, 6, 31, 366, 7, 6, 1, 11, 23, 59,
                59, 999, 50400000, 7200000 };
        for (int i = 0; i < max.length; i++) {
            if (gc.getMaximum(i) != max[i]) {
                result.set(i);
                values.add(new Integer(gc.getMaximum(i)));
            }
        }
        assertTrue("Wrong max for " + result + " = " + values,
                result.length() == 0);
    }

    /**
     * java.util.GregorianCalendar#getMinimum(int)
     */
    public void test_getMinimumI() {
        // Test for method int java.util.GregorianCalendar.getMinimum(int)
        GregorianCalendar gc = new GregorianCalendar();
        assertEquals("Wrong minimum value for DAY_OF_MONTH", 1, gc
                .getMinimum(Calendar.DAY_OF_MONTH));
        assertTrue("Wrong minimum value for MONTH", gc
                .getMinimum(Calendar.MONTH) == Calendar.JANUARY);
        assertEquals("Wrong minimum value for HOUR_OF_DAY", 0, gc
                .getMinimum(Calendar.HOUR_OF_DAY));
        assertEquals("Wrong minimum value for HOUR",
                0, gc.getMinimum(Calendar.HOUR));

        BitSet result = new BitSet();
        int[] min = { 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, -46800000,
                0 };
        for (int i = 0; i < min.length; i++) {
            if (gc.getMinimum(i) != min[i])
                result.set(i);
        }
        assertTrue("Wrong min for " + result, result.length() == 0);
    }

    /**
     * java.util.GregorianCalendar#isLeapYear(int)
     */
    public void test_isLeapYearI() {
        // Test for method boolean java.util.GregorianCalendar.isLeapYear(int)
        GregorianCalendar gc = new GregorianCalendar(1998, 11, 6);
        assertTrue("Returned incorrect value for leap year", !gc
                .isLeapYear(1998));
        assertTrue("Returned incorrect value for leap year", gc
                .isLeapYear(2000));

    }

    /**
     * java.util.GregorianCalendar#roll(int, int)
     */
    public void test_rollII() {
        // Test for method void java.util.GregorianCalendar.roll(int, int)
        GregorianCalendar gc = new GregorianCalendar(1972, Calendar.OCTOBER, 8,
                2, 5, 0);
        gc.roll(Calendar.DAY_OF_MONTH, -1);
        assertTrue("Failed to roll DAY_OF_MONTH down by 1", gc
                .equals(new GregorianCalendar(1972, Calendar.OCTOBER, 7, 2, 5,
                        0)));
        gc = new GregorianCalendar(1972, Calendar.OCTOBER, 8, 2, 5, 0);
        gc.roll(Calendar.DAY_OF_MONTH, 25);
        assertTrue("Failed to roll DAY_OF_MONTH up by 25", gc
                .equals(new GregorianCalendar(1972, Calendar.OCTOBER, 2, 2, 5,
                        0)));
        gc = new GregorianCalendar(1972, Calendar.OCTOBER, 8, 2, 5, 0);
        gc.roll(Calendar.DAY_OF_MONTH, -10);
        assertTrue("Failed to roll DAY_OF_MONTH down by 10", gc
                .equals(new GregorianCalendar(1972, Calendar.OCTOBER, 29, 2, 5,
                        0)));
    }

    /**
     * java.util.GregorianCalendar#roll(int, boolean)
     */
    public void test_rollIZ() {
        // Test for method void java.util.GregorianCalendar.roll(int, boolean)
        GregorianCalendar gc = new GregorianCalendar(1972, Calendar.OCTOBER,
                13, 19, 9, 59);
        gc.roll(Calendar.DAY_OF_MONTH, false);
        assertTrue("Failed to roll day_of_month down", gc
                .equals(new GregorianCalendar(1972, Calendar.OCTOBER, 12, 19,
                        9, 59)));
        gc = new GregorianCalendar(1972, Calendar.OCTOBER, 13, 19, 9, 59);
        gc.roll(Calendar.DAY_OF_MONTH, true);
        assertTrue("Failed to roll day_of_month up", gc
                .equals(new GregorianCalendar(1972, Calendar.OCTOBER, 14, 19,
                        9, 59)));
        gc = new GregorianCalendar(1972, Calendar.OCTOBER, 31, 19, 9, 59);
        gc.roll(Calendar.DAY_OF_MONTH, true);
        assertTrue("Failed to roll day_of_month up", gc
                .equals(new GregorianCalendar(1972, Calendar.OCTOBER, 1, 19, 9,
                        59)));

        GregorianCalendar cal = new GregorianCalendar();
        int result;
        try {
            cal.roll(Calendar.ZONE_OFFSET, true);
            result = 0;
        } catch (IllegalArgumentException e) {
            result = 1;
        }
        assertEquals("ZONE_OFFSET roll", 1, result);
        try {
            cal.roll(Calendar.DST_OFFSET, true);
            result = 0;
        } catch (IllegalArgumentException e) {
            result = 1;
        }
        assertEquals("ZONE_OFFSET roll", 1, result);

        cal.set(2004, Calendar.DECEMBER, 31, 5, 0, 0);
        cal.roll(Calendar.WEEK_OF_YEAR, true);
        assertEquals("Wrong year: " + cal.getTime(), 2004, cal
                .get(Calendar.YEAR));
        assertEquals("Wrong month: " + cal.getTime(), Calendar.JANUARY, cal
                .get(Calendar.MONTH));
        assertEquals("Wrong date: " + cal.getTime(), 9, cal.get(Calendar.DATE));

        // Regression for HARMONY-4372
        cal.set(1994, 11, 30, 5, 0, 0);
        cal.setMinimalDaysInFirstWeek(4);
        cal.roll(Calendar.WEEK_OF_YEAR, true);
        assertEquals("Wrong year: " + cal.getTime(), 1994, cal
                .get(Calendar.YEAR));
        assertEquals("Wrong month: " + cal.getTime(), Calendar.JANUARY, cal
                .get(Calendar.MONTH));
        assertEquals("Wrong date: " + cal.getTime(), 7, cal.get(Calendar.DATE));

        cal.roll(Calendar.WEEK_OF_YEAR, true);
        assertEquals("Wrong year: " + cal.getTime(), 1994, cal
                .get(Calendar.YEAR));
        assertEquals("Wrong month: " + cal.getTime(), Calendar.JANUARY, cal
                .get(Calendar.MONTH));
        assertEquals("Wrong date: " + cal.getTime(), 14, cal.get(Calendar.DATE));

        cal.roll(Calendar.WEEK_OF_YEAR, false);
        assertEquals("Wrong year: " + cal.getTime(), 1994, cal
                .get(Calendar.YEAR));
        assertEquals("Wrong month: " + cal.getTime(), Calendar.JANUARY, cal
                .get(Calendar.MONTH));
        assertEquals("Wrong date: " + cal.getTime(), 7, cal.get(Calendar.DATE));

        cal.roll(Calendar.WEEK_OF_YEAR, false);
        assertEquals("Wrong year: " + cal.getTime(), 1994, cal
                .get(Calendar.YEAR));
        assertEquals("Wrong month: " + cal.getTime(), Calendar.DECEMBER, cal
                .get(Calendar.MONTH));
        assertEquals("Wrong date: " + cal.getTime(), 30, cal.get(Calendar.DATE));

        cal.roll(Calendar.WEEK_OF_YEAR, false);
        assertEquals("Wrong year: " + cal.getTime(), 1994, cal
                .get(Calendar.YEAR));
        assertEquals("Wrong month: " + cal.getTime(), Calendar.DECEMBER, cal
                .get(Calendar.MONTH));
        assertEquals("Wrong date: " + cal.getTime(), 23, cal.get(Calendar.DATE));

        // Regression for HARMONY-4510
        cal.set(1999, Calendar.DECEMBER, 31, 23, 59, 59);
        cal.roll(GregorianCalendar.WEEK_OF_YEAR, true);
        assertEquals("Wrong year: " + cal.getTime(), 1999, cal
                .get(Calendar.YEAR));
        assertEquals("Wrong month: " + cal.getTime(), Calendar.JANUARY, cal
                .get(Calendar.MONTH));
        assertEquals("Wrong date: " + cal.getTime(), 8, cal.get(Calendar.DATE));
        cal.roll(GregorianCalendar.WEEK_OF_YEAR, false);
        assertEquals("Wrong year: " + cal.getTime(), 1999, cal
                .get(Calendar.YEAR));
        assertEquals("Wrong month: " + cal.getTime(), Calendar.DECEMBER, cal
                .get(Calendar.MONTH));
        assertEquals("Wrong date: " + cal.getTime(), 31, cal.get(Calendar.DATE));
    }

    /**
     * java.util.GregorianCalendar#setGregorianChange(java.util.Date)
     */
    public void test_setGregorianChangeLjava_util_Date() {
        // Test for method void
        // java.util.GregorianCalendar.setGregorianChange(java.util.Date)
        GregorianCalendar gc1 = new GregorianCalendar(1582, Calendar.OCTOBER,
                4, 0, 0);
        GregorianCalendar gc2 = new GregorianCalendar(1972, Calendar.OCTOBER,
                13, 0, 0);
        gc1.setGregorianChange(gc2.getTime());
        assertTrue("Returned incorrect value", gc2.getTime().equals(
                gc1.getGregorianChange()));
    }

    /**
     * java.util.GregorianCalendar#clone()
     */
    public void test_clone() {

        // Regression for HARMONY-498
        GregorianCalendar gCalend = new GregorianCalendar();

        gCalend.set(Calendar.MILLISECOND, 0);
        int dayOfMonth = gCalend.get(Calendar.DAY_OF_MONTH);

        // create clone object and change date
        GregorianCalendar gCalendClone = (GregorianCalendar) gCalend.clone();
        gCalendClone.add(Calendar.DATE, 1);

        assertEquals("Before", dayOfMonth, gCalend.get(Calendar.DAY_OF_MONTH));
        gCalend.set(Calendar.MILLISECOND, 0);//changes nothing
        assertEquals("After", dayOfMonth, gCalend.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * java.util.GregorianCalendar#getMinimalDaysInFirstWeek()
     */
    public void test_getMinimalDaysInFirstWeek() {
        // Regression for Harmony-1037
        // Some non-bug differences below because of different CLDR data of Harmony
        GregorianCalendar g = new GregorianCalendar(TimeZone
                .getTimeZone("Europe/London"), new Locale("en", "GB"));
        int minimalDaysInFirstWeek = g.getMinimalDaysInFirstWeek();
        assertEquals(4, minimalDaysInFirstWeek);

        g = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"),
                new Locale("fr"));
        minimalDaysInFirstWeek = g.getMinimalDaysInFirstWeek();
        assertEquals(4, minimalDaysInFirstWeek);

        g = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"),
                new Locale("fr", "CA"));
        minimalDaysInFirstWeek = g.getMinimalDaysInFirstWeek();
        assertEquals(1, minimalDaysInFirstWeek);

    }

    /**
     * java.util.GregorianCalendar#computeTime()
     */
    public void test_computeTime() {
        // Regression for Harmony-493
        GregorianCalendar g = new GregorianCalendar(
                TimeZone.getTimeZone("Europe/London"),
                new Locale("en", "GB")
        );
        g.clear();
        g.set(2006, Calendar.MARCH, 26, 01, 50, 00);
        assertEquals(1143337800000L, g.getTimeInMillis());

        GregorianCalendar g1 = new GregorianCalendar(
                TimeZone.getTimeZone("Europe/Moscow"));
        g1.clear();
        g1.set(2006, Calendar.MARCH, 26, 02, 20, 00);
        assertEquals(1143328800000L, g1.getTimeInMillis());
        assertEquals(3, g1.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, g1.get(Calendar.MINUTE));

        g1.clear();
        g1.set(2006, Calendar.OCTOBER, 29, 02, 50, 00);
        assertEquals(1162079400000L, g1.getTimeInMillis());
        assertEquals(2, g1.get(Calendar.HOUR_OF_DAY));
        assertEquals(50, g1.get(Calendar.MINUTE));
        // End of regression test
    }

    /**
     * java.util.GregorianCalendar#get(int)
     */
    @SuppressWarnings("deprecation")
    public void test_getI() {
        // Regression test for HARMONY-2959
        Date date = new Date(Date.parse("Jan 15 00:00:01 GMT 2000"));
        GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        gc.setGregorianChange(date);
        gc.setTimeInMillis(Date.parse("Dec 24 00:00:01 GMT 2000"));
        assertEquals(346, gc.get(Calendar.DAY_OF_YEAR));

        // Regression test for HARMONY-3003
        date = new Date(Date.parse("Feb 28 00:00:01 GMT 2000"));
        gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        gc.setGregorianChange(date);
        gc.setTimeInMillis(Date.parse("Dec 1 00:00:01 GMT 2000"));
        assertEquals(1, gc.get(Calendar.DAY_OF_MONTH));
        assertEquals(11, gc.get(Calendar.MONTH));

        // Regression test for HARMONY-4513
        gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        gc.set(1582, Calendar.OCTOBER, 15, 0, 0, 0);
        // reset millisecond to zero in order to be the same time as cutover
        gc.set(Calendar.MILLISECOND, 0);
        assertEquals(0, gc.get(Calendar.MILLISECOND));
        assertEquals(1582, gc.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, gc.get(Calendar.MONTH));
        assertEquals(15, gc.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, gc.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, gc.get(Calendar.MINUTE));
        assertEquals(0, gc.get(Calendar.SECOND));
        gc.set(1582, Calendar.OCTOBER, 14, 0, 0, 0);
        assertEquals(24, gc.get(Calendar.DAY_OF_MONTH));
    }
}
