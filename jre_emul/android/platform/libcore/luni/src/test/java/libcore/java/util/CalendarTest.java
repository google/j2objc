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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import libcore.libcore.util.SerializationTester;

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

    public void testAddOneDayOverDstForwardAdds23HoursAt0100() {
        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
        calendar.set(2011, 9, 15, 1, 0); // 01:00 GMT-3
        double hoursSinceEpoch = hoursSinceEpoch(calendar);
        calendar.add(Calendar.DATE, 1);
        assertEquals(23.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
        assertCalendarEquals(calendar, 2011, 9, 16, 1, 0); // 01:00 GMT-2; +23 hours
    }

    /**
     * At their daylight savings time switch, Sao Paulo changes from
     * "00:00 GMT-3" to "01:00 GMT-2". When adding time across this boundary,
     * drop an hour to keep the hour+minute constant unless that prevents the
     * date field from being incremented.
     * http://code.google.com/p/android/issues/detail?id=17502
     */
    public void testAddOneDayOverDstForwardAdds24HoursAt0000() {
        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
        calendar.set(2011, 9, 15, 0, 0); // 00:00 GMT-3
        double hoursSinceEpoch = hoursSinceEpoch(calendar);
        calendar.add(Calendar.DATE, 1);
        assertEquals(24.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
        assertCalendarEquals(calendar, 2011, 9, 16, 1, 0); // 01:00 GMT-2; +24 hours
    }

    public void testAddOneDayOverDstBackAdds25HoursAt0000() {
        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
        calendar.set(2011, 1, 19, 0, 0); // 00:00 GMT-2
        double hoursSinceEpoch = hoursSinceEpoch(calendar);
        calendar.add(Calendar.DATE, 1);
        assertEquals(25.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
        assertCalendarEquals(calendar, 2011, 1, 20, 0, 0); // 00:00 GMT-3; +25 hours
    }

    public void testAddOneDayOverDstBackAdds25HoursAt0100() {
        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
        calendar.set(2011, 1, 19, 1, 0); // 00:00 GMT-2
        double hoursSinceEpoch = hoursSinceEpoch(calendar);
        calendar.add(Calendar.DATE, 1);
        assertEquals(25.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
        assertCalendarEquals(calendar, 2011, 1, 20, 1, 0); // 00:00 GMT-3; +25 hours
    }

    public void testAddTwoHalfDaysOverDstForwardAdds23HoursAt0100() {
        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
        calendar.set(2011, 9, 15, 1, 0); // 01:00 GMT-3
        double hoursSinceEpoch = hoursSinceEpoch(calendar);
        calendar.add(Calendar.AM_PM, 2);
        assertEquals(23.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
        assertCalendarEquals(calendar, 2011, 9, 16, 1, 0); // 01:00 GMT-2; +23 hours
    }

    public void testAdd24HoursOverDstForwardAdds24Hours() {
        Calendar calendar = new GregorianCalendar(AMERICA_SAO_PAULO);
        calendar.set(2011, 9, 15, 1, 0); // 01:00 GMT-3
        double hoursSinceEpoch = hoursSinceEpoch(calendar);
        calendar.add(Calendar.HOUR, 24);
        assertEquals(24.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
        assertCalendarEquals(calendar, 2011, 9, 16, 2, 0); // 02:00 GMT-2; +24 hours
    }

    public void testAddOneDayAndOneDayOver30MinuteDstForwardAdds48Hours() {
        Calendar calendar = new GregorianCalendar(AUSTRALIA_LORD_HOWE);
        calendar.set(2011, 9, 1, 2, 10); // 02:10 GMT+10:30
        double hoursSinceEpoch = hoursSinceEpoch(calendar);
        calendar.add(Calendar.DATE, 1);
        calendar.add(Calendar.DATE, 1);
        // The RI fails this test by returning 47.0. It adjusts for DST on both of the add() calls!
        assertEquals(48.0, hoursSinceEpoch(calendar) - hoursSinceEpoch);
        assertCalendarEquals(calendar, 2011, 9, 3, 2, 40); // 02:40 GMT+11:00; +48.0 hours
    }

    public void testAddTwoDaysOver30MinuteDstForwardAdds47AndAHalfHours() {
        Calendar calendar = new GregorianCalendar(AUSTRALIA_LORD_HOWE);
        calendar.set(2011, 9, 1, 2, 10); // 02:10 GMT+10:30
        double hoursSinceEpoch = hoursSinceEpoch(calendar);
        calendar.add(Calendar.DATE, 2);
        assertEquals(47.5, hoursSinceEpoch(calendar) - hoursSinceEpoch);
        assertCalendarEquals(calendar, 2011, 9, 3, 2, 10); // 02:10 GMT+11:00; +47.5 hours
    }

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

    // http://b/5179775
    /* J2ObjC: Calendar/TimeZone serialization is broken. (b/34764665)
    public void testCalendarSerialization() {
        String s = "aced00057372001b6a6176612e7574696c2e477265676f7269616e43616c656e6461728f3dd7d6e"
                + "5b0d0c103000749000f63757272656e7459656172536b65774a0010677265676f7269616e4375746"
                + "f7665725a000869734361636865644a00126c6173744d69646e696768744d696c6c697349000c6c6"
                + "1737459656172536b65774a00126e6578744d69646e696768744d696c6c69735b000c63616368656"
                + "44669656c64737400025b49787200126a6176612e7574696c2e43616c656e646172e6ea4d1ec8dc5"
                + "b8e03000b5a000c6172654669656c647353657449000e66697273744461794f665765656b5a00096"
                + "97354696d655365745a00076c656e69656e744900166d696e696d616c44617973496e46697273745"
                + "765656b4900096e6578745374616d7049001573657269616c56657273696f6e4f6e53747265616d4"
                + "a000474696d655b00066669656c647371007e00015b000569735365747400025b5a4c00047a6f6e6"
                + "57400144c6a6176612f7574696c2f54696d655a6f6e653b787001000000010101000000040000000"
                + "20000000100000000a15c9800757200025b494dba602676eab2a5020000787000000011000000010"
                + "00007b20000000100000005000000010000000100000020000000010000000100000000000000000"
                + "0000000000000000000000000000000fe488c0000000000757200025b5a578f203914b85de202000"
                + "07870000000110101010101010101010101010101010101737200186a6176612e7574696c2e53696"
                + "d706c6554696d655a6f6e65fa675d60d15ef5a603001049000a647374536176696e6773490006656"
                + "e6444617949000c656e644461794f665765656b490007656e644d6f6465490008656e644d6f6e746"
                + "8490007656e6454696d654900097261774f666673657449001573657269616c56657273696f6e4f6"
                + "e53747265616d490008737461727444617949000e73746172744461794f665765656b49000973746"
                + "172744d6f646549000a73746172744d6f6e7468490009737461727454696d6549000973746172745"
                + "96561725a000b7573654461796c696768745b000b6d6f6e74684c656e6774687400025b427872001"
                + "26a6176612e7574696c2e54696d655a6f6e6531b3e9f57744aca10200014c000249447400124c6a6"
                + "176612f6c616e672f537472696e673b7870740009474d542d30383a30300036ee800000000000000"
                + "001000000000000000000000000fe488c00000000010000000000000001000000000000000000000"
                + "0000000000000757200025b42acf317f8060854e002000078700000000c1f1c1f1e1f1e1f1f1e1f1"
                + "e1f7708000000040001000178780000000afffff4e2f964ac0001000000009fa5240000000000000"
                + "00000a4cb7c187571007e00060000000a000007b2000000010000000100000001fe488c000000000"
                + "10000000500000001000000200000000178";
        Calendar calendar = new GregorianCalendar(1970, 1, 1, 0, 0, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT-08:00"));
        // Calendar fields firstDayOfWeek and minimalDaysInFirstWeek are are sensitive to the Locale
        // and ICU data. Specifying the values here makes the serialized form stable.
        calendar.setMinimalDaysInFirstWeek(4);
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        new SerializationTester<Calendar>(calendar, s).test();
    }*/

    private void assertCalendarEquals(Calendar calendar,
            int year, int month, int day, int hour, int minute) {
        assertEquals("year", year, calendar.get(Calendar.YEAR));
        assertEquals("month", month, calendar.get(Calendar.MONTH));
        assertEquals("day", day, calendar.get(Calendar.DATE));
        assertEquals("hour", hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals("minute", minute, calendar.get(Calendar.MINUTE));
    }

    private static double hoursSinceEpoch(Calendar c) {
        double ONE_HOUR = 3600d * 1000d;
        return c.getTimeInMillis() / ONE_HOUR;
    }

    // https://code.google.com/p/android/issues/detail?id=45877
    public void test_clear_45877_morning() {
      GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("America/Los_Angeles"));
      // 3rd February 2016 05:32:40.000 America/Los_Angeles time.
      cal.setTimeInMillis(1454506360000L);
      checkClear(cal, 0, 28800000);
    }

    // https://code.google.com/p/android/issues/detail?id=45877
    public void test_clear_45877_afternoon() {
      GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("America/Los_Angeles"));
      // 3rd February 2016 12:32:40.000 America/Los_Angeles time.
      cal.setTimeInMillis(1454531560000L);
      checkClear(cal, 12, 72000000);
    }

    private void checkClear(GregorianCalendar cal, int expectedHourOfDay, long expectedMillis) {
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
      assertEquals(expectedHourOfDay, cal.get(Calendar.HOUR_OF_DAY));
      // And set fields stay the same.
      assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));

      // ...so now everything is set.
      assertTrue(cal.isSet(Calendar.DAY_OF_MONTH));
      assertTrue(cal.isSet(Calendar.HOUR_OF_DAY));

      assertEquals(expectedMillis, cal.getTimeInMillis());

      cal.set(Calendar.HOUR_OF_DAY, 1);
      assertEquals(32400000, cal.getTimeInMillis());
    }

    // http://b/16938922.
    //
    // TODO: This is for backwards compatibility only. Seems like a better idea to throw
    // here. We should add a targetSdkVersion based check and throw for this case.
    public void test_nullLocale_getInstance_Locale() {
        assertCalendarConfigEquals(
                Calendar.getInstance(Locale.getDefault()),
                Calendar.getInstance((Locale) null));
    }

    // http://b/16938922.
    //
    // TODO: This is for backwards compatibility only. Seems like a better idea to throw
    // here. We should add a targetSdkVersion based check and throw for this case.
    public void test_nullLocale_getInstance_TimeZone_Locale() {
        assertCalendarConfigEquals(
                Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault()),
                Calendar.getInstance(TimeZone.getDefault(), null));
    }

    // http://b/16938922.
    //
    // TODO: This is for backwards compatibility only. Seems like a better idea to throw
    // here. We should add a targetSdkVersion based check and throw for this case.
    public void test_nullLocale_GregorianCalendar_Locale() {
        assertCalendarConfigEquals(
                new GregorianCalendar(Locale.getDefault()),
                new GregorianCalendar((Locale) null));
    }

    public void assertCalendarConfigEquals(Calendar a, Calendar b) {
        Date d = new Date();
        a.setTime(d);
        b.setTime(d);
        assertEquals(a, b);
    }

    public void testCloneMakesDeepCopyOfCalendarFields() {
        FakeCalendar c = new FakeCalendar();
        FakeCalendar c2 = (FakeCalendar) c.clone();

        assertFalse(c.getTimeZone() == c2.getTimeZone());
        assertEquals(c.getTimeZone(), c2.getTimeZone());

        // The default clone() implementation makes a deep copy of calendar
        // fields...
        assertFalse(c.getCalenderFields() == c2.getCalenderFields());
        // ,,, and a shallow copy of subclass fields.
        assertSame(c.getSubclassFields(), c2.getSubclassFields());
    }

    // http://b/26581303
    public void testSetHourOfDayInEuropeLondon() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        assertEquals(1,calendar.get(Calendar.HOUR_OF_DAY));
    }

    public void testGetWeekYear() {
        try {
            new FakeCalendar().getWeekYear();
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    public void testGetWeeksInWeekYear() {
        try {
            new FakeCalendar().getWeeksInWeekYear();
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    public void testIsWeekDateSupported() {
        assertFalse(new FakeCalendar().isWeekDateSupported());
    }

    public void testSetWeekDate() {
        try {
            new FakeCalendar().setWeekDate(1, 1, 1);
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    public static class FakeCalendar extends Calendar {

        private int[] subclassFields;

        public FakeCalendar() {
            super(TimeZone.getDefault(), Locale.getDefault());
            subclassFields = new int[12];
        }

        public int[] getCalenderFields() {
            return fields;
        }

        public int[] getSubclassFields() {
            return subclassFields;
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
}
