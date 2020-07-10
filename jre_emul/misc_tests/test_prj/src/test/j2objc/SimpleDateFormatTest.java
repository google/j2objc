/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.j2objc;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;


public class SimpleDateFormatTest extends junit.framework.TestCase {

    private TimeZone previousDefaultTimeZone;

    @Override public void setUp() {
        previousDefaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    @Override public void tearDown() {
        TimeZone.setDefault(previousDefaultTimeZone);
    }

    public void test_timeZoneFormatting() {
        // tests specific to formatting of timezones
        Date summerDate = new GregorianCalendar(1999, Calendar.JUNE, 2, 15, 3, 6).getTime();
        Date winterDate = new GregorianCalendar(1999, Calendar.JANUARY, 12).getTime();

        verifyFormatTimezone(
                "America/Los_Angeles", "PDT, Pacific Daylight Time", "-0700, GMT-07:00",
                summerDate);
        verifyFormatTimezone(
                "America/Los_Angeles", "PST, Pacific Standard Time", "-0800, GMT-08:00",
                winterDate);

        verifyFormatTimezone("GMT-7", "GMT-07:00, GMT-07:00", "-0700, GMT-07:00", summerDate);
        verifyFormatTimezone("GMT-7", "GMT-07:00, GMT-07:00", "-0700, GMT-07:00", winterDate);

        verifyFormatTimezone("GMT+14", "GMT+14, GMT+14:00", "+1400, GMT+14:00", summerDate);
        verifyFormatTimezone("GMT+14", "GMT+14, GMT+14:00", "+1400, GMT+14:00", winterDate);

        // this fails on the RI!
        verifyFormatTimezone("America/Detroit", "EDT, Eastern Daylight Time", "-0400, GMT-04:00",
                summerDate);
        verifyFormatTimezone("America/Detroit", "EST, Eastern Standard Time", "-0500, GMT-05:00",
                winterDate);

        // Pacific/Kiritimati is one of the timezones supported only in mJava
        verifyFormatTimezone(
                "Pacific/Kiritimati", "GMT+14, Line Islands Time", "+1400, GMT+14:00",
                summerDate);
        verifyFormatTimezone(
                "Pacific/Kiritimati", "GMT+14, Line Islands Time", "+1400, GMT+14:00",
                winterDate);

        verifyFormatTimezone("EST", "GMT-5, GMT-05:00", "-0500, GMT-05:00", summerDate);
        verifyFormatTimezone("EST", "GMT-5, GMT-05:00", "-0500, GMT-05:00", winterDate);

        verifyFormatTimezone("GMT+14", "GMT+14, GMT+14:00", "+1400, GMT+14:00", summerDate);
        verifyFormatTimezone("GMT+14", "GMT+14, GMT+14:00", "+1400, GMT+14:00", winterDate);
    }

    private void verifyFormatTimezone(String timeZoneId, String expected1, String expected2,
            Date date) {
        SimpleDateFormat format = new SimpleDateFormat("", Locale.ENGLISH);
        format.setTimeZone(SimpleTimeZone.getTimeZone(timeZoneId));
        format.applyPattern("z, zzzz");
        assertEquals("Test z for TimeZone : " + timeZoneId, expected1, format.format(date));

        format.applyPattern("Z, ZZZZ");
        assertEquals("Test Z for TimeZone : " + timeZoneId, expected2, format.format(date));
    }

  }
