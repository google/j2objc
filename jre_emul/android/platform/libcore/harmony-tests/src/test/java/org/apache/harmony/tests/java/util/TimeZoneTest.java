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

import tests.support.Support_TimeZone;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class TimeZoneTest extends junit.framework.TestCase {

    private static final int ONE_HOUR = 3600000;

    private TimeZone processDefault;

    /**
     * java.util.TimeZone#getDefault()
     */
    public void test_getDefault() {
        assertNotSame("returns identical",
                TimeZone.getDefault(), TimeZone.getDefault());
    }

    /**
     * java.util.TimeZone#getDSTSavings()
     */
    public void test_getDSTSavings() {
        // Test for method int java.util.TimeZone.getDSTSavings()

        // test on subclass SimpleTimeZone
        TimeZone st1 = TimeZone.getTimeZone("America/New_York");
        assertEquals("T1A. Incorrect daylight savings returned",
                ONE_HOUR, st1.getDSTSavings());

        // a SimpleTimeZone with daylight savings different then 1 hour
        st1 = TimeZone.getTimeZone("Australia/Lord_Howe");
        assertEquals("T1B. Incorrect daylight savings returned",
                1800000, st1.getDSTSavings());

        // test on subclass Support_TimeZone, an instance with daylight savings
        TimeZone tz1 = new Support_TimeZone(-5 * ONE_HOUR, true);
        assertEquals("T2. Incorrect daylight savings returned",
                ONE_HOUR, tz1.getDSTSavings());

        // an instance without daylight savings
        tz1 = new Support_TimeZone(3 * ONE_HOUR, false);
        assertEquals("T3. Incorrect daylight savings returned, ",
                0, tz1.getDSTSavings());
    }

    /**
     * java.util.TimeZone#getOffset(long)
     */
    public void test_getOffset_long() {
        // Test for method int java.util.TimeZone.getOffset(long time)

        // test on subclass SimpleTimeZone
        TimeZone st1 = TimeZone.getTimeZone("EST");
        long time1 = new GregorianCalendar(1998, Calendar.NOVEMBER, 11)
                .getTimeInMillis();
        assertEquals("T1. Incorrect offset returned",
                -(5 * ONE_HOUR), st1.getOffset(time1));

        long time2 = new GregorianCalendar(1998, Calendar.JUNE, 11)
                .getTimeInMillis();
        st1 = TimeZone.getTimeZone("EST");
        assertEquals("T2. Incorrect offset returned",
                -(5 * ONE_HOUR), st1.getOffset(time2));

        // test on subclass Support_TimeZone, an instance with daylight savings
        TimeZone tz1 = new Support_TimeZone(-5 * ONE_HOUR, true);
        assertEquals("T3. Incorrect offset returned, ",
                -(5 * ONE_HOUR), tz1.getOffset(time1));
        assertEquals("T4. Incorrect offset returned, ",
                -(4 * ONE_HOUR), tz1.getOffset(time2));

        // an instance without daylight savings
        tz1 = new Support_TimeZone(3 * ONE_HOUR, false);
        assertEquals("T5. Incorrect offset returned, ",
                (3 * ONE_HOUR), tz1.getOffset(time1));
        assertEquals("T6. Incorrect offset returned, ",
                (3 * ONE_HOUR), tz1.getOffset(time2));
    }

    /**
     * java.util.TimeZone#getTimeZone(java.lang.String)
     */
    public void test_getTimeZoneLjava_lang_String() {
        assertEquals("Must return GMT when given an invalid TimeZone id SMT-8.",
                "GMT", TimeZone.getTimeZone("SMT-8").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+28:70.",
                "GMT", TimeZone.getTimeZone("GMT+28:70").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+28:30.",
                "GMT", TimeZone.getTimeZone("GMT+28:30").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+8:70.",
                "GMT", TimeZone.getTimeZone("GMT+8:70").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+3:.",
                "GMT", TimeZone.getTimeZone("GMT+3:").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+3:0.",
                "GMT", TimeZone.getTimeZone("GMT+3:0").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+2360.",
                "GMT", TimeZone.getTimeZone("GMT+2360").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+892.",
                "GMT", TimeZone.getTimeZone("GMT+892").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+082.",
                "GMT", TimeZone.getTimeZone("GMT+082").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+28.",
                "GMT", TimeZone.getTimeZone("GMT+28").getID());
        assertEquals("Must return GMT when given an invalid TimeZone time GMT+30.",
                "GMT", TimeZone.getTimeZone("GMT+30").getID());
        assertEquals("Must return GMT when given TimeZone GMT.",
                "GMT", TimeZone.getTimeZone("GMT").getID());
        assertEquals("Must return GMT when given TimeZone GMT+.",
                "GMT", TimeZone.getTimeZone("GMT+").getID());
        assertEquals("Must return GMT when given TimeZone GMT-.",
                "GMT", TimeZone.getTimeZone("GMT-").getID());
        /* j2objc: NSTimeZone can actually parse this format.
        assertEquals("Must return GMT when given an invalid TimeZone time GMT-8.45.",
                "GMT", TimeZone.getTimeZone("GMT-8.45").getID());*/
        assertEquals("Must return GMT when given an invalid TimeZone time GMT-123:23.",
                "GMT", TimeZone.getTimeZone("GMT-123:23").getID());
        /* j2objc: NSTimeZone can actually parse this format.
        assertEquals("Must return proper GMT formatted string for GMT+8:30 (eg. GMT+08:20).",
                "GMT+08:30", TimeZone.getTimeZone("GMT+8:30").getID());*/
        assertEquals("Must return proper GMT formatted string for GMT+3 (eg. GMT+08:20).",
                "GMT+03:00", TimeZone.getTimeZone("GMT+3").getID());
        /* j2objc: NSTimeZone can actually parse this format.
        assertEquals("Must return proper GMT formatted string for GMT+3:02 (eg. GMT+08:20).",
                "GMT+03:02", TimeZone.getTimeZone("GMT+3:02").getID());*/
        assertEquals("Must return proper GMT formatted string for GMT+2359 (eg. GMT+08:20).",
                "GMT+23:59", TimeZone.getTimeZone("GMT+2359").getID());
        assertEquals("Must return proper GMT formatted string for GMT+520 (eg. GMT+08:20).",
                "GMT+05:20", TimeZone.getTimeZone("GMT+520").getID());
        assertEquals("Must return proper GMT formatted string for GMT+052 (eg. GMT+08:20).",
                "GMT+00:52", TimeZone.getTimeZone("GMT+052").getID());
        /* j2objc: NSTimeZone treats GMT-00 as GMT.
        // GMT-0 is an available ID in ICU, so replace it with GMT-00
        assertEquals("Must return proper GMT formatted string for GMT-00 (eg. GMT+08:20).",
                "GMT-00:00", TimeZone.getTimeZone("GMT-00").getID());*/
    }

    /**
     * java.util.TimeZone#setDefault(java.util.TimeZone)
     */
    public void test_setDefaultLjava_util_TimeZone() {
        // NOTE: Required to get tests passing under vogar. Vogar sets
        // a hardcoded timezone before running every test, so we have to
        // set it back to the "real" default before we run the test.
        TimeZone.setDefault(null);

        TimeZone oldDefault = TimeZone.getDefault();
        TimeZone zone = new SimpleTimeZone(45, "TEST");
        TimeZone.setDefault(zone);
        assertEquals("timezone not set", zone, TimeZone.getDefault());
        TimeZone.setDefault(null);
        assertEquals("default not restored",
                oldDefault, TimeZone.getDefault());
    }

    /**
     * java.util.TimeZone#getDisplayName(java.util.Locale)
     */
    public void test_getDisplayNameLjava_util_Locale() {
        TimeZone timezone = TimeZone.getTimeZone("Asia/Shanghai");
        assertEquals("\u4e2d\u56fd\u6807\u51c6\u65f6\u95f4", timezone
                .getDisplayName(Locale.CHINA));
    }

    /**
     * java.util.TimeZone#getDisplayName(boolean, int, java.util.Locale)
     */
    public void test_getDisplayNameZILjava_util_Locale() {
        TimeZone timezone = TimeZone.getTimeZone("Asia/Shanghai");
        // j2objc: disabled; use an older assertion that also works on OS X/iOS.
        /* Time zone data was changed in ICU49.2.  Many common short names were removed. */
        /*assertEquals("中国标准时间",
                timezone.getDisplayName(false, TimeZone.LONG, Locale.CHINA));
        assertEquals("GMT+08:00",
                timezone.getDisplayName(false, TimeZone.SHORT, Locale.CHINA));*/
        assertTrue("\u4e2d\u56fd\u6807\u51c6\u65f6\u95f4".equals(
                timezone.getDisplayName(false, TimeZone.LONG, Locale.CHINA)));
        try {
            timezone.getDisplayName(false, 100, Locale.CHINA);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /*
     * Regression for HARMONY-5860
     */
    public void test_GetTimezoneOffset() {
        // America/Toronto is lazy initialized 
        TimeZone.setDefault(TimeZone.getTimeZone("America/Toronto"));
        Date date = new Date(07, 2, 24);
        assertEquals(300, date.getTimezoneOffset());
        date = new Date(99, 8, 1);
        assertEquals(240, date.getTimezoneOffset());
    }

    protected void setUp() {
        processDefault = TimeZone.getDefault();
    }

    protected void tearDown() {
        TimeZone.setDefault(processDefault);
    }

    public void test_getAvailableIDs_I_16947622() {
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        int rawOffset = tz.getRawOffset();
        assertEquals(-8 * 60 * 60 * 1000, rawOffset);
        List<String> ids = Arrays.asList(TimeZone.getAvailableIDs(rawOffset));

        // Obviously, for all time zones, the time zone whose raw offset we started with
        // should be one of the available ids for that offset.
        assertTrue(ids.toString(), ids.contains("America/Los_Angeles"));

        // Any one of these might legitimately change its raw offset, though that's
        // fairly unlikely, and the chances of more than one changing are very slim.
        assertTrue(ids.toString(), ids.contains("America/Dawson"));
        assertTrue(ids.toString(), ids.contains("America/Tijuana"));
        assertTrue(ids.toString(), ids.contains("America/Vancouver"));
        // j2objc: NSTimeZone does not list Canada/* as known time zone names.
        // assertTrue(ids.toString(), ids.contains("Canada/Pacific"));
        // assertTrue(ids.toString(), ids.contains("Canada/Yukon"));
        assertTrue(ids.toString(), ids.contains("Pacific/Pitcairn"));
    }

    public void test_getAvailableIDs_I() {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        int rawoffset = tz.getRawOffset();
        String[] ids = TimeZone.getAvailableIDs(rawoffset);
        List<String> idList = Arrays.asList(ids);
        assertTrue(idList.toString(), idList.contains("Asia/Hong_Kong"));
    }

    /**
     * @add test {@link java.util.TimeZone#getDisplayName()}
     */
    public void test_getDisplayName() {
        TimeZone defaultZone = TimeZone.getDefault();
        Locale defaulLocal = Locale.getDefault();
        String defaultName = defaultZone.getDisplayName();
        String expectedName = defaultZone.getDisplayName(defaulLocal);
        assertEquals(
                "getDispalyName() did not return the default Locale suitable name",
                expectedName, defaultName);
    }

    /**
     * @add test {@link java.util.TimeZone#getDisplayName(boolean, int)}
     */
    public void test_getDisplayName_ZI() {
        TimeZone defaultZone = TimeZone.getDefault();
        Locale defaultLocale = Locale.getDefault();
        String actualName = defaultZone.getDisplayName(false, TimeZone.LONG);
        String expectedName = defaultZone.getDisplayName(false, TimeZone.LONG,
                defaultLocale);
        assertEquals(
                "getDisplayName(daylight,style) did not return the default locale suitable name",
                expectedName, actualName);
    }

    /**
     * @add test {@link java.util.TimeZone#hasSameRules(TimeZone)}
     */
    public void test_hasSameRules_Ljava_util_TimeZone() {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        // j2objc: disabled. This test can be problematic. The first id in the ids array that does
        // not have the same id as tz may actually not have the same rules. We use another test in
        // NativeTimeZoneTest to cover the method hasSameRules.
        /*int offset = tz.getRawOffset();

        String[] ids = TimeZone.getAvailableIDs(offset);
        int i = 0;
        if (ids.length != 0) {
            while (true) {
                if (!(ids[i].equalsIgnoreCase(tz.getID()))) {
                    TimeZone sameZone = TimeZone.getTimeZone(ids[i]);
                    assertTrue(tz.hasSameRules(sameZone));
                    break;
                } else {
                    i++;
                }
            }
        }*/
        assertFalse("should return false when parameter is null", tz
                .hasSameRules(null));
    }
}
