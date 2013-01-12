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

package org.apache.harmony.luni.tests.java.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarTest extends junit.framework.TestCase {

        Locale defaultLocale;

        /**
         * @tests java.util.Calendar#set(int, int)
         *
        TODO(user): enable when calendar ICU integration is complete.
        public void test_setII() {
                // Test for correct result defined by the last set field
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                assertTrue("Incorrect result 0: " + cal.getTime().getTime(), cal
                                .getTime().getTime() == 1009861200000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                assertTrue("Incorrect result 0a: " + cal.getTime(), cal.getTime()
                                .getTime() == 1014958800000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DATE, 24);
                assertTrue("Incorrect result 0b: " + cal.getTime(), cal.getTime()
                                .getTime() == 1011848400000L);

                cal.set(Calendar.MONTH, Calendar.OCTOBER);
                cal.set(Calendar.DATE, 31);
                cal.set(Calendar.MONTH, Calendar.NOVEMBER);
                cal.set(Calendar.DATE, 26);
                assertTrue("Incorrect month: " + cal.get(Calendar.MONTH), cal
                                .get(Calendar.MONTH) == Calendar.NOVEMBER);

                int dow = cal.get(Calendar.DAY_OF_WEEK);
                cal.set(Calendar.DATE, 27);
                assertTrue("Incorrect DAY_OF_WEEK: " + cal.get(Calendar.DAY_OF_WEEK)
                                + " expected: " + dow, cal.get(Calendar.DAY_OF_WEEK) != dow);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                assertTrue("Incorrect result 0c1: " + cal.getTime().getTime(), cal
                                .getTime().getTime() == 1010379600000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                assertTrue("Incorrect result 0c2: " + cal.getTime().getTime(), cal
                                .getTime().getTime() == 1009861200000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                assertTrue("Incorrect result 0c3: " + cal.getTime(), cal.getTime()
                                .getTime() == 1010034000000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_MONTH, 2);
                assertTrue("Incorrect result 0d: " + cal.getTime(), cal.getTime()
                                .getTime() == 1010293200000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2);
                assertTrue("Incorrect result 0e: " + cal.getTime(), cal.getTime()
                                .getTime() == 1010898000000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 11);
                assertTrue("Incorrect result 0f: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015736400000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DATE, 24);
                cal.set(Calendar.WEEK_OF_YEAR, 11);
                assertTrue("Incorrect result 0g: " + cal.getTime(), cal.getTime()
                                .getTime() == 1011848400000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.get(Calendar.WEEK_OF_YEAR); // Force fields to compute
                cal.set(Calendar.WEEK_OF_YEAR, 11);
                assertTrue("Incorrect result 0h: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015909200000L);

                // WEEK_OF_YEAR has priority over MONTH/DATE
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_YEAR, 170);
                cal.set(Calendar.WEEK_OF_YEAR, 11);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DATE, 5);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                assertTrue("Incorrect result 1: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // WEEK_OF_YEAR has priority over MONTH/DATE
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 11);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DATE, 5);
                cal.set(Calendar.DAY_OF_YEAR, 170);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                assertTrue("Incorrect result 1a: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // DAY_OF_WEEK has no effect when other fields not set
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                cal.set(Calendar.DATE, 11);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                assertTrue("Incorrect result 1b: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);
        // Regression for HARMONY-4384
        // Set DAY_OF_WEEK without DATE
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertEquals("Incorrect result 1b: " + cal.getTime(), 1015304400000L, cal.getTime()
                .getTime());


                // WEEK_OF_MONTH has priority
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 12);
                cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
                cal.set(Calendar.WEEK_OF_MONTH, 3);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                cal.set(Calendar.DATE, 5);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                assertTrue("Incorrect result 2: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // DAY_OF_WEEK_IN_MONTH has priority over WEEK_OF_YEAR
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 12);
                cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                cal.set(Calendar.DATE, 5);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                assertTrue("Incorrect result 3: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // WEEK_OF_MONTH has priority, MONTH not set
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 12);
                cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
                cal.set(Calendar.WEEK_OF_MONTH, 3);
                cal.set(Calendar.DATE, 25);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                assertTrue("Incorrect result 4: " + cal.getTime(), cal.getTime()
                                .getTime() == 1010984400000L);

                // WEEK_OF_YEAR has priority when MONTH set last and DAY_OF_WEEK set
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 11);
                cal.set(Calendar.DATE, 25);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                assertTrue("Incorrect result 5: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // Use MONTH/DATE when WEEK_OF_YEAR set but not DAY_OF_WEEK
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 12);
                cal.set(Calendar.DATE, 11);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                assertTrue("Incorrect result 5a: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // Use MONTH/DATE when DAY_OF_WEEK is not set
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 12);
                cal.set(Calendar.DATE, 11);
                cal.set(Calendar.WEEK_OF_MONTH, 1);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                assertTrue("Incorrect result 5b: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // WEEK_OF_MONTH has priority
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 12);
                cal.set(Calendar.DATE, 5);
                cal.set(Calendar.WEEK_OF_MONTH, 3);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                assertTrue("Incorrect result 5c: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // DATE has priority when set last
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 12);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                cal.set(Calendar.DATE, 11);
                assertTrue("Incorrect result 6: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // DATE has priority when set last, MONTH not set
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 12);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.DATE, 14);
                assertTrue("Incorrect result 7: " + cal.getTime(), cal.getTime()
                                .getTime() == 1010984400000L);

                // DAY_OF_YEAR has priority when MONTH set last and DATE not set
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_YEAR, 70);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                assertTrue("Incorrect result 8: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // DAY/MONTH has priority when DATE set after DAY_OF_YEAR
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_YEAR, 170);
                cal.set(Calendar.DATE, 11);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                assertTrue("Incorrect result 8a: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // DAY_OF_YEAR has priority when set after DATE
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DATE, 15);
                cal.set(Calendar.DAY_OF_YEAR, 70);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                assertTrue("Incorrect result 8b: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // DATE has priority when set last
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_YEAR, 70);
                cal.set(Calendar.DATE, 14);
                assertTrue("Incorrect result 9: " + cal.getTime(), cal.getTime()
                                .getTime() == 1010984400000L);

                // DATE has priority when set last
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_YEAR, 15);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                cal.set(Calendar.DATE, 14);
                assertTrue("Incorrect result 9a: " + cal.getTime(), cal.getTime()
                                .getTime() == 1010984400000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.DATE, 14);
                cal.set(Calendar.WEEK_OF_YEAR, 11);
                assertTrue("Incorrect result 9b: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DATE, 14);
                cal.set(Calendar.WEEK_OF_YEAR, 11);
                assertTrue("Incorrect result 9c: " + cal.getTime(), cal.getTime()
                                .getTime() == 1010984400000L);

                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.WEEK_OF_MONTH, 1);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                cal.set(Calendar.DATE, 11);
                assertTrue("Incorrect result 9d: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // DAY_OF_YEAR has priority when DAY_OF_MONTH set last and other fields
                // not set
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_YEAR, 70);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                assertTrue("Incorrect result 10: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // MONTH/DATE has priority when DAY_OF_WEEK_IN_MONTH set last but
                // DAY_OF_WEEK not set
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DATE, 11);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
                assertTrue("Incorrect result 11: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // MONTH/DATE has priority when WEEK_OF_YEAR set last but DAY_OF_WEEK
                // not set
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DATE, 11);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                cal.set(Calendar.WEEK_OF_YEAR, 15);
                assertTrue("Incorrect result 12: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // MONTH/DATE has priority when WEEK_OF_MONTH set last but DAY_OF_WEEK
                // not set
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DATE, 11);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                cal.set(Calendar.WEEK_OF_MONTH, 1);
                assertTrue("Incorrect result 13: " + cal.getTime(), cal.getTime()
                                .getTime() == 1015822800000L);

                // Ensure last date field set is reset after computing
                cal.clear();
                cal.set(Calendar.YEAR, 2002);
                cal.set(Calendar.DAY_OF_YEAR, 111);
                cal.get(Calendar.YEAR);
                cal.set(Calendar.MONTH, Calendar.MARCH);
                cal.set(Calendar.AM_PM, Calendar.AM);
                assertTrue("Incorrect result 14: " + cal.getTime(), cal.getTime()
                                .getTime() == 1016686800000L);

                int hour = cal.get(Calendar.HOUR);
                cal.set(Calendar.HOUR, hour);
                cal.set(Calendar.AM_PM, Calendar.PM);
                assertEquals("AM_PM not changed", Calendar.PM, cal.get(Calendar.AM_PM));
                // setting AM_PM without HOUR should not have any affect
                cal.set(Calendar.AM_PM, Calendar.AM);
                assertEquals("AM_PM was changed 1",
                                Calendar.AM, cal.get(Calendar.AM_PM));
                int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                hour = cal.get(Calendar.HOUR);
                cal.set(Calendar.AM_PM, Calendar.PM);
                assertEquals("AM_PM was changed 2",
                                Calendar.PM, cal.get(Calendar.AM_PM));
                assertEquals(hour, cal.get(Calendar.HOUR));
                assertEquals(hourOfDay + 12, cal.get(Calendar.HOUR_OF_DAY));

        // regression test for Harmony-2122
        cal = Calendar.getInstance();
        int oldValue = cal.get(Calendar.AM_PM);
        int newValue = (oldValue == Calendar.AM) ? Calendar.PM : Calendar.AM;
        cal.set(Calendar.AM_PM, newValue);
        newValue = cal.get(Calendar.AM_PM);
        assertTrue(newValue != oldValue);
        }

        /**
         * @tests java.util.Calendar#setTime(java.util.Date)
         */
        public void test_setTimeLjava_util_Date() {
                Calendar cal = Calendar.getInstance();
                // Use millisecond time for testing in Core
                cal.setTime(new Date(884581200000L)); // (98, Calendar.JANUARY, 12)
                assertEquals("incorrect millis", 884581200000L, cal.getTime().getTime());
                cal.setTimeZone(TimeZone.getTimeZone("EST"));
                cal.setTime(new Date(943506000000L)); // (99, Calendar.NOVEMBER, 25)
                assertTrue("incorrect fields", cal.get(Calendar.YEAR) == 1999
                                && cal.get(Calendar.MONTH) == Calendar.NOVEMBER
                                && cal.get(Calendar.DATE) == 25);
        }

        /**
         * @tests java.util.Calendar#compareTo(Calendar)
         */
        public void test_compareToLjava_util_Calendar_null() {
                Calendar cal = Calendar.getInstance();
                try {
                        cal.compareTo(null);
                        fail("should throw NullPointerException");
                } catch (NullPointerException e) {
                        // expected
                }
        }

        /**
         * @tests java.util.Calendar#compareTo(Calendar)
         */
        public void test_compareToLjava_util_Calendar() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1997, 12, 13, 23, 57);

        Calendar anotherCal = Calendar.getInstance();
        anotherCal.clear();
        anotherCal.set(1997, 12, 13, 23, 57);
        assertEquals(0, cal.compareTo(anotherCal));

        anotherCal = Calendar.getInstance();
        anotherCal.clear();
        anotherCal.set(1997, 11, 13, 24, 57);
        assertEquals(1, cal.compareTo(anotherCal));

        anotherCal = Calendar.getInstance();
        anotherCal.clear();
        anotherCal.set(1997, 12, 13, 23, 58);
        assertEquals(-1, cal.compareTo(anotherCal));
    }

    /**
     * @tests java.util.Calendar#clone()
     */
    public void test_clone() {
        // Regression for HARMONY-475
        Calendar cal = Calendar.getInstance();
        cal.set(2006, 5, 6, 11, 35);
        Calendar anotherCal = (Calendar) cal.clone();
        // should be deep clone
        assertNotSame("getTimeZone", cal.getTimeZone(), anotherCal
                .getTimeZone());
    }

    /**
     * @tests java.util.Calendar#getTimeInMillis()
     */
    public void test_getTimeInMillis() {
        Calendar cal = Calendar.getInstance();

        int year = Integer.MIN_VALUE + 71;
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));;
        cal.set(Calendar.YEAR, year + 1900);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        assertEquals(6017546357372606464L, cal.getTimeInMillis());
    }

    /**
     * @tests java.util.Calendar#getActualMaximum(int)
     */
    public void test_getActualMaximum_I() {
        Calendar c = new MockCalendar();
        assertEquals("should be equal to 0", 0, c.getActualMaximum(0));
    }

    /**
     * @tests java.util.Calendar#getActualMinimum(int)
     */
    public void test_getActualMinimum_I() {
        Calendar c = new MockCalendar();
        assertEquals("should be equal to 0", 0, c.getActualMinimum(0));
    }

    /**
     * @tests java.util.Calendar#before(Object)
     * @tests java.util.Calendar#after(Object)
     */
    public void test_before_after() {
        Calendar early = Calendar.getInstance();
        Calendar late = Calendar.getInstance();
        // test by second
        early.set(2008, 3, 20, 17, 28, 12);
        late.set(2008, 3, 20, 17, 28, 22);
        // test before()
        assertTrue(early.before(late));
        assertFalse(early.before(early));
        assertFalse(late.before(early));
        // test after();
        assertTrue(late.after(early));
        assertFalse(late.after(late));
        assertFalse(early.after(late));

        // test by minute
        early.set(2008, 3, 20, 17, 18, 12);
        late.set(2008, 3, 20, 17, 28, 12);
        // test before()
        assertTrue(early.before(late));
        assertFalse(early.before(early));
        assertFalse(late.before(early));
        // test after();
        assertTrue(late.after(early));
        assertFalse(late.after(late));
        assertFalse(early.after(late));

        // test by hour
        early.set(2008, 3, 20, 17, 28, 12);
        late.set(2008, 3, 20, 27, 28, 12);
        // test before()
        assertTrue(early.before(late));
        assertFalse(early.before(early));
        assertFalse(late.before(early));
        // test after();
        assertTrue(late.after(early));
        assertFalse(late.after(late));
        assertFalse(early.after(late));

        // test by day
        early.set(2008, 3, 10, 17, 28, 12);
        late.set(2008, 3, 20, 17, 28, 12);
        // test before()
        assertTrue(early.before(late));
        assertFalse(early.before(early));
        assertFalse(late.before(early));
        // test after();
        assertTrue(late.after(early));
        assertFalse(late.after(late));
        assertFalse(early.after(late));

        // test by month
        early.set(2008, 2, 20, 17, 28, 12);
        late.set(2008, 3, 20, 17, 28, 12);
        // test before()
        assertTrue(early.before(late));
        assertFalse(early.before(early));
        assertFalse(late.before(early));
        // test after();
        assertTrue(late.after(early));
        assertFalse(late.after(late));
        assertFalse(early.after(late));

        // test by year
        early.set(2007, 3, 20, 17, 28, 12);
        late.set(2008, 3, 20, 17, 28, 12);
        // test before()
        assertTrue(early.before(late));
        assertFalse(early.before(early));
        assertFalse(late.before(early));
        // test after();
        assertTrue(late.after(early));
        assertFalse(late.after(late));
        assertFalse(early.after(late));
    }

    /**
     * @tests java.util.Calendar#clear()
     * @tests java.util.Calendar#clear(int)
     */
    public void test_clear() {
        Calendar calendar = Calendar.getInstance();

        int count = 6;
        int[] fields = new int[count];
        int[] defaults = new int[count];

        fields[0] = Calendar.YEAR;
        fields[1] = Calendar.MONTH;
        fields[2] = Calendar.DATE;
        fields[3] = Calendar.HOUR_OF_DAY;
        fields[4] = Calendar.MINUTE;
        fields[5] = Calendar.SECOND;

        defaults[0] = 1970;
        defaults[1] = 0;
        defaults[2] = 1;
        defaults[3] = 0;
        defaults[4] = 0;
        defaults[5] = 0;

        calendar.set(2008, 3, 20, 17, 28, 12);

        // test clear(int)
        for (int i = 0; i < fields.length; i++) {
            int index = fields[i];
            calendar.clear(index);
            if (5 == index) {
                // RI also doesn't change the value of DATE
                assertEquals("Field " + index + " Should equal to 20.", 20,
                        calendar.get(index));
            } else if (11 == index) {
                // RI also doesn't change the value of HOUR
                assertEquals("Field " + index + " Should equal to 17.", 17,
                        calendar.get(index));
            } else {
                // Other have been set to default values
                assertEquals("Field " + index + " Should equal to "
                        + defaults[i] + ".", defaults[i], calendar.get(index));
            }
        }

        // test clear()
        calendar.set(2008, 3, 20, 17, 28, 12);

        calendar.clear();

        for (int i = 0; i < fields.length; i++) {
            int index = fields[i];
            assertEquals("Field " + index + " Should equal to "
                    + defaults[i] + ".", defaults[i], calendar.get(index));
        }
    }

    /**
     * @tests java.util.Calendar#isSet(int)
     */
    public void test_isSet() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        for (int i = 0; i < Calendar.FIELD_COUNT; i++) {
            assertFalse(calendar.isSet(i));
        }
    }

    /**
     * @tests java.util.Calendar#getAvailableLocales()
     */
    public void test_getAvailableLocales() {
        Locale[] locales = Calendar.getAvailableLocales();
        boolean exist = false;
        for (int i = 0; i < locales.length; i++) {
            Locale l = locales[i];
            if (Locale.US.equals(l)) {
                exist = true;
                break;
            }
        }
        assertTrue(exist);
    }

    /**
     * @tests java.util.Calendar#getInstance(Locale)
     * @tests java.util.Calendar#getInstance(TimeZone, Locale)
     *
    TODO(user): enable when locale properties like first day of
                 week are implemented.
    public void test_getInstance() {
        // test getInstance(Locale)
        Calendar us_calendar = Calendar.getInstance(Locale.US);
        Calendar ch_calendar = Calendar.getInstance(Locale.CHINESE);
        assertEquals(Calendar.SUNDAY, us_calendar
                .getFirstDayOfWeek());
        assertEquals(Calendar.MONDAY, ch_calendar
                .getFirstDayOfWeek());

        // test getInstance(Locale, TimeZone)
        Calendar gmt_calendar = Calendar.getInstance(TimeZone
                .getTimeZone("GMT"), Locale.US);
        assertEquals(TimeZone.getTimeZone("GMT"),
                gmt_calendar.getTimeZone());
        Calendar est_calendar = Calendar.getInstance(TimeZone
                .getTimeZone("EST"), Locale.US);
        assertEquals(TimeZone.getTimeZone("EST")
                .getID(), est_calendar.getTimeZone().getID());
    }

    /**
     * @tests java.util.Calendar#internalGet(int)
     */
    public void test_internalGet() {
        MockGregorianCalendar c = new MockGregorianCalendar();
        c.clear(Calendar.YEAR);
        assertEquals(0, c.internal_get(Calendar.YEAR));
    }

    /**
     * @tests java.util.Calendar#hashCode()
     */
    public void test_hashcode() {
        Calendar calendar = Calendar.getInstance(Locale.JAPAN);
        assertTrue(calendar.hashCode() == calendar.hashCode());
    }

    /**
     * @tests java.util.Calendar#roll(int, int)
     */
    public void test_roll() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2008, 3, 20, 17, 28, 12);

        // roll up
        calendar.roll(Calendar.DATE, 5);
        assertEquals(25, calendar.get(Calendar.DATE));

        // roll down
        calendar.roll(Calendar.DATE, -5);
        assertEquals(20, calendar.get(Calendar.DATE));

        // roll 0
        calendar.roll(Calendar.DATE, 0);
        assertEquals(20, calendar.get(Calendar.DATE));

        // roll overweight
        calendar.set(2008, 1, 31, 17, 28, 12);
        calendar.roll(Calendar.MONTH, 1);
        assertEquals(2, calendar.get(Calendar.DATE));

    }

    /**
     * @tests java.util.Calendar#toString()
     */
    public void test_toString() {
        Calendar calendar = Calendar.getInstance();
        //Should be the current time with no interrogation in the string.
        assertTrue(calendar.toString() instanceof String);
        assertEquals(-1, calendar.toString().indexOf("?"));
        calendar.clear();
        assertTrue(calendar.toString() instanceof String);
        assertTrue(0 <= calendar.toString().indexOf("?"));
    }

    private class MockGregorianCalendar extends GregorianCalendar {
        public int internal_get(int field) {
            return super.internalGet(field);
        }
    }

    private class MockCalendar extends Calendar {

                public MockCalendar() {
                        super();
                }

                @Override
                public void add(int field, int value) {
                }

                @Override
                protected void computeFields() {
                }

                @Override
                protected void computeTime() {
                }

                @Override
                public int getGreatestMinimum(int field) {
                        return 0;
                }

                @Override
                public int getLeastMaximum(int field) {
                        return 0;
                }

                @Override
                public int getMaximum(int field) {
                        return 0;
                }

                @Override
                public int getMinimum(int field) {
                        return 0;
                }

                @Override
                public void roll(int field, boolean increment) {
                }
    }


        protected void setUp() {
                defaultLocale = Locale.getDefault();
                Locale.setDefault(Locale.US);
        }

        protected void tearDown() {
                Locale.setDefault(defaultLocale);
        }
}
