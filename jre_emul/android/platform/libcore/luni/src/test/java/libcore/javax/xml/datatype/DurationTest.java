/*
 * Copyright (C) 2021 The Android Open Source Project
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

package libcore.javax.xml.datatype;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import junit.framework.TestCase;

/**
 * Android has no {@link Duration} implementation, so use stubbed one that throws
 * on abstract methods. This unit test is primarily for code coverage, it barely tests
 * the actual implementation.
 */
public class DurationTest extends TestCase {

    private static final Duration oneSecond = new DurationImpl(1, 0, 0, 0, 0, 0, 1);
    private static final Duration tenMinutes = new DurationImpl(1, 0, 0, 0, 0, 10, 0);
    private static final Duration oneDay = new DurationImpl(1, 0, 0, 1, 0 , 0, 0);
    private static final Duration fiveDays = new DurationImpl(1, 0, 0, 5, 0, 0, 0);
    private static final Duration twoMonths = new DurationImpl(1, 0, 2, 0, 0, 0, 0);
    private static final Duration threeYears = new DurationImpl(1, 3, 0, 0, 0, 0, 0);
    private static final Duration y6m5d4h3m2s1 = new DurationImpl(1, 6, 5, 4, 3, 2, 1);

    public void testAddTo_Date() {
        Date date = null;
        try {
            oneSecond.addTo(date);
        } catch (NullPointerException expected) {}

        date = new Date(1000);
        try {
            oneSecond.addTo(date);
        } catch (UnsupportedOperationException expected) {}
    }

    public void testGetDays() {
        assertEquals(0, oneSecond.getDays());
        assertEquals(0, tenMinutes.getDays());
        assertEquals(1, oneDay.getDays());
        assertEquals(5, fiveDays.getDays());
        assertEquals(0, twoMonths.getDays());
        assertEquals(4, y6m5d4h3m2s1.getDays());
    }

    public void testGetHours() {
        assertEquals(0, oneSecond.getHours());
        assertEquals(0, tenMinutes.getHours());
        assertEquals(0, oneDay.getHours());
        assertEquals(0, fiveDays.getHours());
        assertEquals(0, twoMonths.getHours());
        assertEquals(3, y6m5d4h3m2s1.getHours());
    }

    public void testGetMinutes() {
        assertEquals(0, oneSecond.getMinutes());
        assertEquals(10, tenMinutes.getMinutes());
        assertEquals(0, oneDay.getMinutes());
        assertEquals(0, fiveDays.getMinutes());
        assertEquals(0, twoMonths.getMinutes());
        assertEquals(2, y6m5d4h3m2s1.getMinutes());
    }

    public void testGetMonths() {
        assertEquals(0, oneSecond.getMonths());
        assertEquals(0, tenMinutes.getMonths());
        assertEquals(0, oneDay.getMonths());
        assertEquals(0, fiveDays.getMonths());
        assertEquals(2, twoMonths.getMonths());
        assertEquals(0, threeYears.getMonths());
        assertEquals(5, y6m5d4h3m2s1.getMonths());
    }

    public void testGetSeconds() {
        assertEquals(1, oneSecond.getSeconds());
        assertEquals(0, tenMinutes.getSeconds());
        assertEquals(0, oneDay.getSeconds());
        assertEquals(0, fiveDays.getSeconds());
        assertEquals(0, twoMonths.getSeconds());
        assertEquals(0, threeYears.getSeconds());
        assertEquals(1, y6m5d4h3m2s1.getSeconds());
    }

    public void testGetTimeInMillis_calendar() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(1000);
        try {
            oneSecond.getTimeInMillis(calendar);
        } catch (UnsupportedOperationException expected) {}
    }

    public void testGetTimeInMillis_date() {
        Date date = new Date(1000);
        try {
            oneSecond.getTimeInMillis(date);
        } catch (UnsupportedOperationException expected) {}
    }

    public void testGetXmlSchemaType() {
        Duration allSet = new DurationImpl(1, 1, 1, 1, 1, 1, 1);
        assertEquals(DatatypeConstants.DURATION, allSet.getXMLSchemaType());

        Duration dayHourMinuteSecondSet = new DurationImpl(1, -1, -1, 1, 1, 1, 1);
        assertEquals(DatatypeConstants.DURATION_DAYTIME, dayHourMinuteSecondSet.getXMLSchemaType());

        Duration yearMonthSet = new DurationImpl(1, 1, 1, -1, -1, -1, -1);
        assertEquals(DatatypeConstants.DURATION_YEARMONTH, yearMonthSet.getXMLSchemaType());

        Duration noneSet = new DurationImpl(0, -1, -1, -1, -1, -1, -1);
        try {
            noneSet.getXMLSchemaType();
            fail("Unexpectedly didn't throw");
        } catch (IllegalStateException expected) {}
    }

    public void testGetYears() {
        assertEquals(0, oneSecond.getYears());
        assertEquals(0, tenMinutes.getYears());
        assertEquals(0, oneDay.getYears());
        assertEquals(0, fiveDays.getYears());
        assertEquals(0, twoMonths.getYears());
        assertEquals(3, threeYears.getYears());
        assertEquals(6, y6m5d4h3m2s1.getYears());
    }

    public void testIsLongerThan() {
        final Duration[] durations = {
                oneSecond, tenMinutes, oneDay, fiveDays, twoMonths, threeYears };
        for (int i = 0; i < durations.length - 1; i++) {
            for (int j = i + 1; j < durations.length; j++) {
                try {
                    assertTrue(durations[j].isLongerThan(durations[i]));
                } catch (UnsupportedOperationException expected) {}
            }
        }
    }

    public void testIsShorterThan() {
        final Duration[] durations = {
                oneSecond, tenMinutes, oneDay, fiveDays, twoMonths, threeYears };
        for (int i = 0; i < durations.length - 1; i++) {
            for (int j = i + 1; j < durations.length; j++) {
                try {
                    assertTrue(durations[i].isShorterThan(durations[j]));
                } catch (UnsupportedOperationException expected) {}
            }
        }
    }

    public void testMultiply() {
        final Duration[] durations = {
                oneSecond, tenMinutes, oneDay, fiveDays, twoMonths, threeYears };
        for (Duration d : durations) {
            try {
                d.multiply(2);
            } catch (UnsupportedOperationException expected) {}
        }
    }

    public void testSubtract() {
        final Duration[] durations = {
                oneSecond, tenMinutes, oneDay, fiveDays, twoMonths, threeYears };
        for (Duration i : durations) {
            for (Duration j : durations) {
                try {
                    i.subtract(j);
                } catch (UnsupportedOperationException expected) {}
            }
        }
    }
}
