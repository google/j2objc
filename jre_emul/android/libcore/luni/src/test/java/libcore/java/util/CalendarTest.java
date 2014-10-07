/*
 * Copyright (C) 2010 The Android Open Source Project
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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarTest extends junit.framework.TestCase {

    private static final TimeZone AMERICA_SAO_PAULO = TimeZone.getTimeZone("America/Sao_Paulo");

    /** This zone's DST offset is only 30 minutes. */
    private static final TimeZone AUSTRALIA_LORD_HOWE = TimeZone.getTimeZone("Australia/Lord_Howe");

    /**
     * This zone had once used DST but doesn't currently. Any code that uses
     * TimeZone.useDaylightTime() as an optimization will probably be broken
     * for this zone.
     */
    private static final TimeZone ASIA_KUALA_LUMPUR = TimeZone.getTimeZone("Asia/Kuala_Lumpur");

    private static final TimeZone ASIA_SEOUL = TimeZone.getTimeZone("Asia/Seoul");

    // http://code.google.com/p/android/issues/detail?id=6184
    public void test_setTimeZone() {
        // The specific time zones don't matter; they just have to be different so we can see that
        // get(Calendar.ZONE_OFFSET) returns the zone offset of the time zone passed to setTimeZone.
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(0, cal.get(Calendar.ZONE_OFFSET));
        TimeZone tz = java.util.TimeZone.getTimeZone("GMT+7");
        cal.setTimeZone(tz);
        assertEquals(25200000, cal.get(Calendar.ZONE_OFFSET));
    }

    // TODO(tball): enable when b/10533006 is fixed.
//    public void testAddOneDayOverDstForwardAdds23HoursAt0100() {
//        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
//        calendar.set(2011, 9, 15, 1, 0); // 01:00 GMT-3
//        double hoursSinceEpoch = hoursSinceEpoch(calendar);
//        calendar.add(Calendar.DATE, 1);
//        assertEquals(23.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
//        assertCalendarEquals(calendar, 2011, 9, 16, 1, 0); // 01:00 GMT-2; +23 hours
//    }
//
//    /**
//     * At their daylight savings time switch, Sao Paulo changes from
//     * "00:00 GMT-3" to "01:00 GMT-2". When adding time across this boundary,
//     * drop an hour to keep the hour+minute constant unless that prevents the
//     * date field from being incremented.
//     * http://code.google.com/p/android/issues/detail?id=17502
//     */
//    public void testAddOneDayOverDstForwardAdds24HoursAt0000() {
//        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
//        calendar.set(2011, 9, 15, 0, 0); // 00:00 GMT-3
//        double hoursSinceEpoch = hoursSinceEpoch(calendar);
//        calendar.add(Calendar.DATE, 1);
//        assertEquals(24.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
//        assertCalendarEquals(calendar, 2011, 9, 16, 1, 0); // 01:00 GMT-2; +24 hours
//    }
//
//    public void testAddOneDayOverDstBackAdds25HoursAt0000() {
//        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
//        calendar.set(2011, 1, 19, 0, 0); // 00:00 GMT-2
//        double hoursSinceEpoch = hoursSinceEpoch(calendar);
//        calendar.add(Calendar.DATE, 1);
//        assertEquals(25.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
//        assertCalendarEquals(calendar, 2011, 1, 20, 0, 0); // 00:00 GMT-3; +25 hours
//    }
//
//    public void testAddOneDayOverDstBackAdds25HoursAt0100() {
//        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
//        calendar.set(2011, 1, 19, 1, 0); // 00:00 GMT-2
//        double hoursSinceEpoch = hoursSinceEpoch(calendar);
//        calendar.add(Calendar.DATE, 1);
//        assertEquals(25.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
//        assertCalendarEquals(calendar, 2011, 1, 20, 1, 0); // 00:00 GMT-3; +25 hours
//    }
//
//    public void testAddTwoHalfDaysOverDstForwardAdds23HoursAt0100() {
//        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
//        calendar.set(2011, 9, 15, 1, 0); // 01:00 GMT-3
//        double hoursSinceEpoch = hoursSinceEpoch(calendar);
//        calendar.add(Calendar.AM_PM, 2);
//        assertEquals(23.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
//        assertCalendarEquals(calendar, 2011, 9, 16, 1, 0); // 01:00 GMT-2; +23 hours
//    }
//
//    public void testAdd24HoursOverDstForwardAdds24Hours() {
//        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
//        calendar.set(2011, 9, 15, 1, 0); // 01:00 GMT-3
//        double hoursSinceEpoch = hoursSinceEpoch(calendar);
//        calendar.add(Calendar.HOUR, 24);
//        assertEquals(24.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
//        assertCalendarEquals(calendar, 2011, 9, 16, 2, 0); // 02:00 GMT-2; +24 hours
//    }
//
//    public void testAddOneDayAndOneDayOver30MinuteDstForwardAdds48Hours() {
//        Calendar calendar = new GregorianCalendar(AUSTRALIA_LORD_HOWE);
//        calendar.set(2011, 9, 1, 2, 10); // 02:10 GMT+10:30
//        double hoursSinceEpoch = hoursSinceEpoch(calendar);
//        calendar.add(Calendar.DATE, 1);
//        calendar.add(Calendar.DATE, 1);
//        // The RI fails this test by returning 47.0. It adjusts for DST on both of the add() calls!
//        assertEquals(48.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
//        assertCalendarEquals(calendar, 2011, 9, 3, 2, 40); // 02:40 GMT+11:00; +48.0 hours
//    }
//
//    public void testAddTwoDaysOver30MinuteDstForwardAdds47AndAHalfHours() {
//        Calendar calendar = new GregorianCalendar(AUSTRALIA_LORD_HOWE);
//        calendar.set(2011, 9, 1, 2, 10); // 02:10 GMT+10:30
//        double hoursSinceEpoch = hoursSinceEpoch(calendar);
//        calendar.add(Calendar.DATE, 2);
//        assertEquals(47.5, hoursSinceEpoch(calendar) - hoursSinceEpoch);
//        assertCalendarEquals(calendar, 2011, 9, 3, 2, 10); // 02:10 GMT+11:00; +47.5 hours
//    }

    // http://code.google.com/p/android/issues/detail?id=17741
    public void testNewCalendarKoreaIsSelfConsistent() {
        testSetSelfConsistent(ASIA_SEOUL, 1921, 0, 1);
        testSetSelfConsistent(ASIA_SEOUL, 1955, 0, 1);
        testSetSelfConsistent(ASIA_SEOUL, 1962, 0, 1);
        testSetSelfConsistent(ASIA_SEOUL, 2065, 0, 1);
    }

    // http://code.google.com/p/android/issues/detail?id=15629
    public void testSetTimeInZoneWhereDstIsNoLongerUsed() throws Exception {
        testSetSelfConsistent(ASIA_KUALA_LUMPUR, 1970, 0, 1);
    }

    private void testSetSelfConsistent(TimeZone timeZone, int year, int month, int day) {
        int hour = 0;
        int minute = 0;
        Calendar calendar = new GregorianCalendar(timeZone);
        calendar.clear();
        calendar.set(year, month, day, hour, minute);
        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, calendar.get(Calendar.MINUTE));
    }

    private void assertCalendarEquals(Calendar calendar,
            int year, int month, int day, int hour, int minute) {
        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DATE));
        assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, calendar.get(Calendar.MINUTE));
    }

    private static double hoursSinceEpoch(Calendar c) {
        double ONE_HOUR = 3600d * 1000d;
        return c.getTimeInMillis() / ONE_HOUR;
    }

    // https://code.google.com/p/android/issues/detail?id=45877
    public void test_clear_45877() {
      GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("America/Los_Angeles"));
      cal.set(Calendar.YEAR, 1970);
      cal.set(Calendar.MONTH, Calendar.JANUARY);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      cal.clear(Calendar.HOUR_OF_DAY);
      cal.clear(Calendar.HOUR);
      cal.clear(Calendar.MINUTE);
      cal.clear(Calendar.SECOND);
      cal.clear(Calendar.MILLISECOND);

      // Now we have a mix of set and unset fields.
      assertTrue(cal.isSet(Calendar.DAY_OF_MONTH));
      assertFalse(cal.isSet(Calendar.HOUR_OF_DAY));

      // When we call get, unset fields are computed.
      assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
      // And set fields stay the same.
      assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));

      // ...so now everything is set.
      assertTrue(cal.isSet(Calendar.DAY_OF_MONTH));
      assertTrue(cal.isSet(Calendar.HOUR_OF_DAY));

      assertEquals(28800000, cal.getTimeInMillis());

      cal.set(Calendar.HOUR_OF_DAY, 1);
      assertEquals(32400000, cal.getTimeInMillis());
    }
}
