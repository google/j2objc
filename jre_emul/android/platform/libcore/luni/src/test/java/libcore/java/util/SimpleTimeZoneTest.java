/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import junit.framework.TestCase;

/**
 * Tests for {@link SimpleTimeZone}.
 *
 * <p>The methods starting {@code testDstParis2014_...} and {@code testDstNewYork2014} check
 * various different ways to specify the same instants when DST starts and ends in the associated
 * real world time zone in 2014, i.e. Europe/Paris and America/New_York respectively.
 */
public class SimpleTimeZoneTest extends TestCase {

    private static final int NEW_YORK_RAW_OFFSET = -18000000;

    private static final int PARIS_RAW_OFFSET = 3600000;

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    /**
     * Sanity check to ensure that the standard TimeZone for Europe/Paris has the correct DST
     * transition times.
     */
    public void testStandardParis2014() {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Paris");

        checkDstParis2014(timeZone);
    }

    public void testDstParis2014_LastSundayMarch_LastSundayOctober_UtcTime() {
        TimeZone timeZone = new SimpleTimeZone(PARIS_RAW_OFFSET, "Europe/Paris",
                Calendar.MARCH, -1, Calendar.SUNDAY, 3600000, SimpleTimeZone.UTC_TIME,
                Calendar.OCTOBER, -1, Calendar.SUNDAY, 3600000, SimpleTimeZone.UTC_TIME, 3600000);

        checkDstParis2014(timeZone);
    }

    public void testDstParis2014_SundayAfter25thMarch_SundayAfter25thOctober_UtcTime() {
        TimeZone timeZone = new SimpleTimeZone(PARIS_RAW_OFFSET, "Europe/Paris",
                Calendar.MARCH, 25, -Calendar.SUNDAY, 3600000, SimpleTimeZone.UTC_TIME,
                Calendar.OCTOBER, 25, -Calendar.SUNDAY, 3600000, SimpleTimeZone.UTC_TIME, 3600000);

        checkDstParis2014(timeZone);
    }

    public void testDstParis2014_30thMarch_26thOctober_UtcTime() {
        TimeZone timeZone = new SimpleTimeZone(PARIS_RAW_OFFSET, "Europe/Paris",
                Calendar.MARCH, 30, 0, 3600000, SimpleTimeZone.UTC_TIME,
                Calendar.OCTOBER, 26, 0, 3600000, SimpleTimeZone.UTC_TIME, 3600000);

        checkDstParis2014(timeZone);
    }

    /**
     * Check that the DST transitions in the supplied {@link TimeZone} are as expected for
     * Europe/Paris in 2014.
     */
    private void checkDstParis2014(TimeZone timeZone) {
        checkDstTransitionTimes(timeZone, 2014,
                "2014-03-30T01:00:00.000+0000",
                "2014-10-26T01:00:00.000+0000");
    }

    public void testDst_1stSundayApril_1stSundayOctober_DefaultTime() {
        TimeZone timeZone = new SimpleTimeZone(-18000000, "EST",
                Calendar.APRIL, 1, -Calendar.SUNDAY, 7200000,
                Calendar.OCTOBER, -1, Calendar.SUNDAY, 7200000,
                3600000);

        checkDstTransitionTimes(timeZone, 1998,
                "1998-04-05T07:00:00.000+0000",
                "1998-10-25T06:00:00.000+0000");

        checkDstTransitionTimes(timeZone, 2014,
                "2014-04-06T07:00:00.000+0000",
                "2014-10-26T06:00:00.000+0000");
    }

    /**
     * Sanity check to ensure that the standard TimeZone for America/New_York has the correct DST
     * transition times.
     */
    public void testStandardNewYork2014() {
        TimeZone timeZone = TimeZone.getTimeZone("America/New_York");

        checkDstNewYork2014(timeZone);
    }

    public void testDstNewYork2014_2ndSundayMarch_1stSundayNovember_StandardTime() {
        TimeZone timeZone = new SimpleTimeZone(NEW_YORK_RAW_OFFSET, "EST",
                Calendar.MARCH, 2, Calendar.SUNDAY, 7200000, SimpleTimeZone.STANDARD_TIME,
                Calendar.NOVEMBER, 1, Calendar.SUNDAY, 3600000, SimpleTimeZone.STANDARD_TIME,
                3600000);

        checkDstNewYork2014(timeZone);
    }

    public void testDstNewYork2014_2ndSundayMarch_1stSundayNovember_UtcTime() {
        TimeZone timeZone = new SimpleTimeZone(NEW_YORK_RAW_OFFSET, "EST",
                Calendar.MARCH, 2, Calendar.SUNDAY, 25200000, SimpleTimeZone.UTC_TIME,
                Calendar.NOVEMBER, 1, Calendar.SUNDAY, 21600000, SimpleTimeZone.UTC_TIME,
                3600000);

        checkDstNewYork2014(timeZone);
    }

    public void testDstNewYork2014_2ndSundayMarch_1stSundayNovember_WallTime() {
        TimeZone timeZone = new SimpleTimeZone(NEW_YORK_RAW_OFFSET, "EST",
                Calendar.MARCH, 2, Calendar.SUNDAY, 7200000, SimpleTimeZone.WALL_TIME,
                Calendar.NOVEMBER, 1, Calendar.SUNDAY, 7200000, SimpleTimeZone.WALL_TIME,
                3600000);

        checkDstNewYork2014(timeZone);
    }

    public void testDstNewYork2014_2ndSundayMarch_1stSundayNovember_DefaultTime() {
        TimeZone timeZone = new SimpleTimeZone(NEW_YORK_RAW_OFFSET, "EST",
                Calendar.MARCH, 2, Calendar.SUNDAY, 7200000,
                Calendar.NOVEMBER, 1, Calendar.SUNDAY, 7200000,
                3600000);

        checkDstNewYork2014(timeZone);
    }

    public void testDstNewYork2014_9thMarch_2ndNovember_StandardTime() {
        TimeZone timeZone = new SimpleTimeZone(NEW_YORK_RAW_OFFSET, "EST",
                Calendar.MARCH, 9, 0, 7200000, SimpleTimeZone.STANDARD_TIME,
                Calendar.NOVEMBER, 2, 0, 3600000, SimpleTimeZone.STANDARD_TIME,
                3600000);

        checkDstNewYork2014(timeZone);
    }

    public void testDstNewYork2014_9thMarch_2ndNovember_UtcTime() {
        TimeZone timeZone = new SimpleTimeZone(NEW_YORK_RAW_OFFSET, "EST",
                Calendar.MARCH, 9, 0, 25200000, SimpleTimeZone.UTC_TIME,
                Calendar.NOVEMBER, 2, 0, 21600000, SimpleTimeZone.UTC_TIME,
                3600000);

        checkDstNewYork2014(timeZone);
    }

    public void testDstNewYork2014_9thMarch_2ndNovember_WallTime() {
        TimeZone timeZone = new SimpleTimeZone(NEW_YORK_RAW_OFFSET, "EST",
                Calendar.MARCH, 9, 0, 7200000, SimpleTimeZone.WALL_TIME,
                Calendar.NOVEMBER, 2, 0, 7200000, SimpleTimeZone.WALL_TIME,
                3600000);

        checkDstNewYork2014(timeZone);
    }

    public void testDstNewYork2014_9thMarch_2ndNovember_DefaultTime() {
        TimeZone timeZone = new SimpleTimeZone(NEW_YORK_RAW_OFFSET, "EST",
                Calendar.MARCH, 9, 0, 7200000,
                Calendar.NOVEMBER, 2, 0, 7200000,
                3600000);

        checkDstNewYork2014(timeZone);
    }

    /**
     * Check that the DST transitions in the supplied {@link TimeZone} are as expected for
     * America/New_York in 2014.
     */
    private void checkDstNewYork2014(TimeZone timeZone) {
        checkDstTransitionTimes(timeZone, 2014,
                "2014-03-09T07:00:00.000+0000",
                "2014-11-02T06:00:00.000+0000");
    }

    /**
     * Scan from the start of the year to the end to find the DST transition points.
     *
     * @param timeZone the {@link TimeZone} whose transition points are being found.
     * @param startOfYearMillis the start of the calendar year in {@code timeZone} to scan, in
     *     milliseconds.
     * @return an array of the entry and exit time in millis.
     */
    private static long[] findDstEntryAndExit(TimeZone timeZone, long startOfYearMillis) {
        if (!timeZone.useDaylightTime()) {
            throw new IllegalStateException("Time zone " + timeZone
                    + " doesn't support daylight savings time");
        }

        long[] transitions = new long[2];

        GregorianCalendar cal = new GregorianCalendar(timeZone, Locale.ENGLISH);
        cal.setTimeInMillis(startOfYearMillis);
        int year = cal.get(Calendar.YEAR);
        while (!timeZone.inDaylightTime(new Date(cal.getTimeInMillis()))) {
            // Make sure that this doesn't loop forever.
            if (cal.get(Calendar.YEAR) != year) {
                throw new IllegalStateException(
                        "Doesn't enter daylight savings time in " + year + " in " + timeZone);
            }
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }

        cal.add(Calendar.MILLISECOND, -1);
        assertFalse(timeZone.inDaylightTime(cal.getTime()));
        cal.add(Calendar.MILLISECOND, 1);
        long entryPoint = cal.getTimeInMillis();

        while (timeZone.inDaylightTime(new Date(cal.getTimeInMillis()))) {
            if (cal.get(Calendar.YEAR) != year) {
                throw new IllegalStateException(
                        "Doesn't exit daylight savings time in " + year + " in " + timeZone);
            }
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }

        cal.add(Calendar.MILLISECOND, -1);
        assertTrue(timeZone.inDaylightTime(cal.getTime()));
        cal.add(Calendar.MILLISECOND, 1);
        long exitPoint = cal.getTimeInMillis();

        transitions[0] = entryPoint;
        transitions[1] = exitPoint;
        return transitions;
    }

    public static String formatCalendar(Calendar cal) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                Locale.ENGLISH);
        format.setTimeZone(cal.getTimeZone());
        return format.format(new Date(cal.getTimeInMillis()));
    }

    private static String formatTime(TimeZone timeZone, long millis) {
        Calendar cal = new GregorianCalendar(timeZone, Locale.ENGLISH);
        cal.setTimeInMillis(millis);
        return formatCalendar(cal);
    }

    private void checkDstTransitionTimes(TimeZone timeZone, int year,
            String expectedUtcEntryTime, String expectedUtcExitTime) {

        // Find the start of the year in the supplied time zone.
        GregorianCalendar calendar = new GregorianCalendar(timeZone, Locale.ENGLISH);
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long start = calendar.getTimeInMillis();

        // Find the DST transitions instants.
        long[] simpleTransitions = findDstEntryAndExit(timeZone, start);

        String actualUtcEntryTime = formatTime(UTC, simpleTransitions[0]);

        String actualUtcExitTime = formatTime(UTC, simpleTransitions[1]);

        assertEquals("Transition point mismatch: ",
                describeTransitions(expectedUtcEntryTime, expectedUtcExitTime),
                describeTransitions(actualUtcEntryTime, actualUtcExitTime));
    }

    /**
     * Create a string representation of the transition information to allow all aspects to be
     * compared in one go providing a better error message.
     */
    private String describeTransitions(String utcEntryTime, String utcExitTime) {
        return "{Entry: " + utcEntryTime + ", Exit: " + utcExitTime + "}";
    }
}
