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

package libcore.java.text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class SimpleDateFormatTest extends junit.framework.TestCase {

    private static final TimeZone AMERICA_LOS_ANGELES = TimeZone.getTimeZone("America/Los_Angeles");
    private static final TimeZone AUSTRALIA_LORD_HOWE = TimeZone.getTimeZone("Australia/Lord_Howe");

    public void test2038() {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        assertEquals("Sun Nov 24 17:31:44 1833",
                format.format(new Date(((long) Integer.MIN_VALUE + Integer.MIN_VALUE) * 1000L)));
        assertEquals("Fri Dec 13 20:45:52 1901",
                format.format(new Date(Integer.MIN_VALUE * 1000L)));
        assertEquals("Thu Jan 01 00:00:00 1970",
                format.format(new Date(0L)));
        assertEquals("Tue Jan 19 03:14:07 2038",
                format.format(new Date(Integer.MAX_VALUE * 1000L)));
        assertEquals("Sun Feb 07 06:28:16 2106",
                format.format(new Date((2L + Integer.MAX_VALUE + Integer.MAX_VALUE) * 1000L)));
    }

    // In Honeycomb, only one Olson id was associated with CET (or any other "uncommon"
    // abbreviation). This was changed after KitKat to avoid Java hacks on top of ICU data.
    // ICU data only provides abbreviations for timezones in the locales where they would
    // not be ambiguous to most people of that locale.
    public void testFormattingUncommonTimeZoneAbbreviations() {
        String fmt = "yyyy-MM-dd HH:mm:ss.SSS z";
        String unambiguousDate = "1970-01-01 01:00:00.000 CET";
        String ambiguousDate = "1970-01-01 01:00:00.000 GMT+1";

        // The locale to use when formatting. Not every Locale renders "Europe/Berlin" as "CET". The
        // UK is one that does, the US is one that does not.
        Locale cetUnambiguousLocale = Locale.UK;
        Locale cetAmbiguousLocale = Locale.US;

        SimpleDateFormat sdf = new SimpleDateFormat(fmt, cetUnambiguousLocale);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        assertEquals(unambiguousDate, sdf.format(new Date(0)));
        sdf = new SimpleDateFormat(fmt, cetUnambiguousLocale);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Zurich"));
        assertEquals(unambiguousDate, sdf.format(new Date(0)));

        sdf = new SimpleDateFormat(fmt, cetAmbiguousLocale);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        assertEquals(ambiguousDate, sdf.format(new Date(0)));
        sdf = new SimpleDateFormat(fmt, cetAmbiguousLocale);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Zurich"));
        assertEquals(ambiguousDate, sdf.format(new Date(0)));
    }

    // http://code.google.com/p/android/issues/detail?id=8258
    public void testTimeZoneFormatting() throws Exception {
        Date epoch = new Date(0);

        // Create a SimpleDateFormat that defaults to America/Chicago...
        TimeZone.setDefault(TimeZone.getTimeZone("America/Chicago"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        // We should see something appropriate to America/Chicago...
        assertEquals("1969-12-31 18:00:00 -0600", sdf.format(epoch));
        // We can set any TimeZone we want:
        sdf.setTimeZone(AMERICA_LOS_ANGELES);
        assertEquals("1969-12-31 16:00:00 -0800", sdf.format(epoch));
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("1970-01-01 00:00:00 +0000", sdf.format(epoch));

        // A new SimpleDateFormat will default to America/Chicago...
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        // ...and parsing an America/Los_Angeles time will *not* change that...
        sdf.parse("2010-12-03 00:00:00 -0800");
        // ...so our time zone here is "America/Chicago":
        assertEquals("1969-12-31 18:00:00 -0600", sdf.format(epoch));
        // We can set any TimeZone we want:
        sdf.setTimeZone(AMERICA_LOS_ANGELES);
        assertEquals("1969-12-31 16:00:00 -0800", sdf.format(epoch));
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("1970-01-01 00:00:00 +0000", sdf.format(epoch));

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = sdf.parse("2010-07-08 02:44:48");
        assertEquals(1278557088000L, date.getTime());
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(AMERICA_LOS_ANGELES);
        assertEquals("2010-07-07T19:44:48-0700", sdf.format(date));
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2010-07-08T02:44:48+0000", sdf.format(date));
    }

    public void testDstZoneNameWithNonDstTimestamp() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm zzzz", Locale.US);
        Calendar calendar = new GregorianCalendar(AMERICA_LOS_ANGELES);
        calendar.setTime(format.parse("2011-06-21T10:00 Pacific Standard Time")); // 18:00 GMT-8
        assertEquals(11, calendar.get(Calendar.HOUR_OF_DAY)); // 18:00 GMT-7
        assertEquals(0, calendar.get(Calendar.MINUTE));
    }

    public void testNonDstZoneWithDstTimestampForNonHourDstZone() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm zzzz", Locale.US);
        Calendar calendar = new GregorianCalendar(AUSTRALIA_LORD_HOWE);
        calendar.setTime(format.parse("2010-12-21T19:30 Lord Howe Standard Time")); //9:00 GMT+10:30
        assertEquals(20, calendar.get(Calendar.HOUR_OF_DAY)); // 9:00 GMT+11:00
        assertEquals(0, calendar.get(Calendar.MINUTE));
    }

    public void testLocales() throws Exception {
        // Just run through them all. Handy as a poor man's benchmark, and a sanity check.
        for (Locale l : Locale.getAvailableLocales()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzzz", l);
            sdf.format(new Date(0));
        }
    }

    // http://code.google.com/p/android/issues/detail?id=36689
    public void testParseArabic() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ar", "EG"));
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        // Can we parse an ASCII-formatted date in an Arabic locale?
        Date d = sdf.parse("2012-08-29 10:02:45");
        assertEquals(1346259765000L, d.getTime());
    }

    public void test_59383() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("d. MMM yyyy H:mm", Locale.GERMAN);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        assertEquals(1376927400000L, sdf.parse("19. Aug 2013 8:50").getTime());
    }

    public void test_nullLocales() {
        try {
            SimpleDateFormat.getDateInstance(DateFormat.SHORT, null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            SimpleDateFormat.getTimeInstance(DateFormat.SHORT, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    public void test_sl_dates() throws Exception {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("sl"));
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("1. 01. 70", df.format(0L));
    }
}
