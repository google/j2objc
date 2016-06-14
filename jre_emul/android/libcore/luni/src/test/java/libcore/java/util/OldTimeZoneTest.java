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

package libcore.java.util;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import junit.framework.TestCase;
import tests.support.Support_Locale;

/*-[
#include "java/lang/System.h"
#include <sys/utsname.h>
]-*/

public class OldTimeZoneTest extends TestCase {

    class Mock_TimeZone extends TimeZone {
        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
            return 0;
        }

        @Override
        public int getRawOffset() {
            return 0;
        }

        @Override
        public boolean inDaylightTime(Date date) {
            return false;
        }

        @Override
        public void setRawOffset(int offsetMillis) {

        }

        @Override
        public boolean useDaylightTime() {
            return false;
        }
    }

    public void test_constructor() {
        assertNotNull(new Mock_TimeZone());
    }

    public void test_clone() {
        TimeZone tz1 = TimeZone.getDefault();
        TimeZone tz2 = (TimeZone)tz1.clone();

        assertTrue(tz1.equals(tz2));
    }

    public void test_getAvailableIDs() {
        String[] str = TimeZone.getAvailableIDs();
        assertNotNull(str);
        assertTrue(str.length != 0);
        for(int i = 0; i < str.length; i++) {
            assertNotNull(TimeZone.getTimeZone(str[i]));
        }
    }

    public void test_getAvailableIDsI() {
        String[] str = TimeZone.getAvailableIDs(0);
        assertNotNull(str);
        assertTrue(str.length != 0);
        for(int i = 0; i < str.length; i++) {
            assertNotNull(TimeZone.getTimeZone(str[i]));
        }
    }

    public void test_getDisplayName() {
        Locale.setDefault(Locale.US);
        TimeZone tz = TimeZone.getTimeZone("GMT-6");
        assertEquals("GMT-06:00", tz.getDisplayName());
        tz = TimeZone.getTimeZone("America/Los_Angeles");
        assertEquals("Pacific Standard Time", tz.getDisplayName());
    }

    public void test_getDisplayNameLjava_util_Locale() {
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        assertEquals("Pacific Standard Time", tz.getDisplayName(new Locale("US")));
        if (Support_Locale.isLocaleAvailable(Locale.FRANCE)) {
            assertTrue(tz.getDisplayName(Locale.FRANCE).startsWith("heure normale du Pacifique"));
        }
    }

    public void test_getDisplayNameZI() {
        Locale.setDefault(Locale.US);
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        assertEquals("PST",                   tz.getDisplayName(false, 0));
        assertEquals("Pacific Daylight Time", tz.getDisplayName(true, 1));
        assertEquals("Pacific Standard Time", tz.getDisplayName(false, 1));
    }

    public void test_getDisplayNameZILjava_util_Locale() {
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        assertEquals("PST",                   tz.getDisplayName(false, 0, Locale.US));
        assertEquals("Pacific Daylight Time", tz.getDisplayName(true,  1, Locale.US));
        if (Support_Locale.isLocaleAvailable(Locale.UK)) {
            assertEquals("Pacific Standard Time", tz.getDisplayName(false, 1, Locale.UK));
        }
        if (Support_Locale.isLocaleAvailable(Locale.FRANCE)) {
            assertEquals("UTC−8", tz.getDisplayName(false, 0, Locale.FRANCE));
            String expected = preElCapitan()
                ? "heure avancée du Pacifique" : "heure d’été du Pacifique";
            assertEquals(expected, tz.getDisplayName(true,  1, Locale.FRANCE));
            assertTrue(tz.getDisplayName(Locale.FRANCE).startsWith("heure normale du Pacifique"));
        }
    }

    public void test_getID() {
        TimeZone tz = TimeZone.getTimeZone("GMT-6");
        assertEquals("GMT-06:00", tz.getID());
        tz = TimeZone.getTimeZone("America/Denver");
        assertEquals("America/Denver", tz.getID());
    }

    public void test_hasSameRulesLjava_util_TimeZone() {
        TimeZone tz1 = TimeZone.getTimeZone("America/Denver");
        TimeZone tz2 = TimeZone.getTimeZone("America/Phoenix");
        assertEquals(tz1.getDisplayName(false, 0), tz2.getDisplayName(false, 0));
        // Arizona doesn't observe DST. See http://phoenix.about.com/cs/weather/qt/timezone.htm
        assertFalse(tz1.hasSameRules(tz2));
        assertFalse(tz1.hasSameRules(null));
        tz1 = TimeZone.getTimeZone("America/New_York");
        tz2 = TimeZone.getTimeZone("US/Eastern");
        assertEquals(tz1.getDisplayName(), tz2.getDisplayName());
        assertFalse(tz1.getID().equals(tz2.getID()));
        assertTrue(tz2.hasSameRules(tz1));
        assertTrue(tz1.hasSameRules(tz1));
    }

    public void test_setIDLjava_lang_String() {
        TimeZone tz = TimeZone.getTimeZone("GMT-6");
        assertEquals("GMT-06:00", tz.getID());
        tz.setID("New ID for GMT-6");
        assertEquals("New ID for GMT-6", tz.getID());
    }

    // Running on an OS X version less than 10.11?
    private static native boolean preElCapitan() /*-[
      if (![JavaLangSystem_getPropertyWithNSString_(@"os.name") isEqual:@"Mac OS X"]) {
        return NO;
      }
      struct utsname uts;
      if (uname(&uts) == 0) {
        return atoi(uts.release) < 15;
      }
      return NO;
    ]-*/;
}
