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
import java.util.Locale;
import java.util.TimeZone;
import junit.framework.TestCase;

public class DateTest extends TestCase {
    // http://code.google.com/p/android/issues/detail?id=6013
    public void test_toString_us() throws Exception {
        // Ensure that no matter where this is run, we know what time zone to expect.
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("America/Chicago"));
        assertEquals("Wed Dec 31 18:00:00 CST 1969", new Date(0).toString());
    }

    // https://code.google.com/p/android/issues/detail?id=81924
    public void test_toString_nonUs() {
        // The string for the timezone depends on what the default locale is. Not every locale
        // has a short-name for America/Chicago -> CST.
        Locale.setDefault(Locale.CHINA);
        TimeZone.setDefault(TimeZone.getTimeZone("America/Chicago"));
        assertEquals("Wed Dec 31 18:00:00 CST 1969", new Date(0).toString());
    }

    public void test_toGMTString_us() throws Exception {
        // Based on https://issues.apache.org/jira/browse/HARMONY-501
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
        Locale.setDefault(Locale.US);

        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Calendar.YEAR, 21);
        assertEquals("Wed Jan 01 00:00:00 PST 21", c.getTime().toString());
        assertEquals("1 Jan 21 08:00:00 GMT", c.getTime().toGMTString());
        c.set(Calendar.YEAR, 321);
        assertEquals("Sun Jan 01 00:00:00 PST 321", c.getTime().toString());
        assertEquals("1 Jan 321 08:00:00 GMT", c.getTime().toGMTString());
    }

    public void test_toGMTString_nonUs() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
        Locale.setDefault(Locale.UK);

        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Calendar.YEAR, 21);
        assertEquals("Wed Jan 01 00:00:00 PST 21", c.getTime().toString());
        assertEquals("1 Jan 21 08:00:00 GMT", c.getTime().toGMTString());
        c.set(Calendar.YEAR, 321);
        assertEquals("Sun Jan 01 00:00:00 PST 321", c.getTime().toString());
        assertEquals("1 Jan 321 08:00:00 GMT", c.getTime().toGMTString());
    }

    public void test_parse_timezones() {
       assertEquals(
               Date.parse("Wed, 06 Jan 2016 11:55:59 GMT+05:00"),
               Date.parse("Wed, 06 Jan 2016 11:55:59 GMT+0500"));

        assertEquals(
                Date.parse("Wed, 06 Jan 2016 11:55:59 GMT+05:00"),
                Date.parse("Wed, 06 Jan 2016 11:55:59 GMT+05"));

    }
}
