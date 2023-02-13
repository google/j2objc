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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import junit.framework.TestCase;

public class DatatypeFactoryTest extends TestCase {

    private DatatypeFactory factory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        factory = new ExtendsDatatypeFactory();
    }

    public void testConstructor() {
        DatatypeFactory f = new ExtendsDatatypeFactory();
    }

    public void testNewDuration_biiiiii() {
        Duration duration = factory.newDuration(true, 1, 2, 3, 4, 5, 6);
        assertEquals(1, duration.getYears());
        assertEquals(2, duration.getMonths());
        assertEquals(3, duration.getDays());
        assertEquals(4, duration.getHours());
        assertEquals(5, duration.getMinutes());
        assertEquals(6, duration.getSeconds());
    }

    public void testNewDurationDayTime_biiii() {
        Duration duration = factory.newDurationDayTime(true, 3, 4, 5, 6);
        assertEquals(0, duration.getYears());
        assertEquals(0, duration.getMonths());
        assertEquals(3, duration.getDays());
        assertEquals(4, duration.getHours());
        assertEquals(5, duration.getMinutes());
        assertEquals(6, duration.getSeconds());
    }

    public void testNewDurationDayTime_bBigInteger() {
        Duration duration = factory.newDuration(true, BigInteger.valueOf(1), BigInteger.valueOf(2),
                BigInteger.valueOf(3), BigInteger.valueOf(4), BigInteger.valueOf(5),
                BigDecimal.valueOf(6));
        assertEquals(1, duration.getYears());
        assertEquals(2, duration.getMonths());
        assertEquals(3, duration.getDays());
        assertEquals(4, duration.getHours());
        assertEquals(5, duration.getMinutes());
        assertEquals(6, duration.getSeconds());

        duration = factory.newDurationDayTime(true,
            BigInteger.valueOf(3), BigInteger.valueOf(4), BigInteger.valueOf(5),
            BigInteger.valueOf(6));
        assertEquals(0, duration.getYears());
        assertEquals(0, duration.getMonths());
        assertEquals(3, duration.getDays());
        assertEquals(4, duration.getHours());
        assertEquals(5, duration.getMinutes());
        assertEquals(6, duration.getSeconds());
    }

    public void testNewDurationDayTime_String() {
        Duration duration = factory.newDuration("");
        assertNull(duration);

        duration = factory.newDurationDayTime("");
        assertNull(duration);
    }

    public void testNewDurationDayTime_long() {
        Duration duration = factory.newDuration(1000L);
        assertEquals(0, duration.getYears());
        assertEquals(0, duration.getMonths());
        assertEquals(0, duration.getDays());
        assertEquals(0, duration.getHours());
        assertEquals(0, duration.getMinutes());
        assertEquals(1, duration.getSeconds());

        duration = factory.newDurationDayTime(1000L);
        assertEquals(0, duration.getYears());
        assertEquals(0, duration.getMonths());
        assertEquals(0, duration.getDays());
        assertEquals(0, duration.getHours());
        assertEquals(0, duration.getMinutes());
        assertEquals(1, duration.getSeconds());
    }

    public void testNewDurationYearMonth_bii() {
        Duration duration = factory.newDurationYearMonth(true, 1, 2);
        assertEquals(1, duration.getYears());
        assertEquals(2, duration.getMonths());
        assertEquals(0, duration.getDays());
        assertEquals(0, duration.getHours());
        assertEquals(0, duration.getMinutes());
        assertEquals(0, duration.getSeconds());
    }

    public void testNewDurationYearMonth_bBigInteger() {
        Duration duration = factory.newDurationYearMonth(true, BigInteger.valueOf(1), BigInteger.valueOf(2));
        assertEquals(1, duration.getYears());
        assertEquals(2, duration.getMonths());
        assertEquals(0, duration.getDays());
        assertEquals(0, duration.getHours());
        assertEquals(0, duration.getMinutes());
        assertEquals(0, duration.getSeconds());
    }

    public void testNewDurationYearMonth_String() {
        Duration duration = factory.newDurationYearMonth("");
        assertNull(duration);
    }

    public void testNewDurationYearMonth_long() {
        Duration duration = factory.newDurationYearMonth(1000);
        assertEquals(0, duration.getYears());
        assertEquals(0, duration.getMonths());
        assertEquals(0, duration.getDays());
        assertEquals(0, duration.getHours());
        assertEquals(0, duration.getMinutes());
        assertEquals(1, duration.getSeconds());
    }

    public void testNewInstance() {
        try {
            DatatypeFactory.newInstance();
            fail("Unexpectedly created new instance");
        } catch (DatatypeConfigurationException expected) {
            // no default implementation in Android
        }
    }

    public void testNewInstance_customClass() {
        try {
            DatatypeFactory.newInstance(null, null);
            fail("Unexpectedly created new instance");
        } catch (DatatypeConfigurationException expected) {
            // class loading disabled
        }
    }

    public void testNewXMLGregorianCalendar_iiiiiii() {
        XMLGregorianCalendar calendar = factory.newXMLGregorianCalendar(
                1, 2, 3, 4, 5, 6, 7, 0);
        assertNull(calendar);
    }

    public void testNewXMLGregorianCalendarDate_iiii() {
        XMLGregorianCalendar calendar = factory.newXMLGregorianCalendarDate(1, 2, 3, 0);
        assertNull(calendar);
    }

    public void testNewXMLGregorianCalendarTime_iiii() {
        XMLGregorianCalendar calendar = factory.newXMLGregorianCalendarTime(4, 5, 6, 0);
        assertNull(calendar);
    }

    public void testNewXMLGregorianCalendarTime_iiiii() {
        XMLGregorianCalendar calendar = factory.newXMLGregorianCalendarTime(4, 5, 6, 7, 0);
        assertNull(calendar);
    }

    public void testNewXMLGregorianCalendarTime_BigDecimal() {
        XMLGregorianCalendar calendar = factory.newXMLGregorianCalendarTime(4, 5, 6, BigDecimal.valueOf(7), 0);
        assertNull(calendar);
    }

    private static class ExtendsDatatypeFactory extends DatatypeFactory {

        protected ExtendsDatatypeFactory() {
            super();
        }

        @Override
        public Duration newDuration(String lexicalRepresentation) {
            return null;
        }

        @Override
        public Duration newDuration(long durationInMilliSeconds) {
            return new DurationImpl(durationInMilliSeconds);
        }

        @Override
        public Duration newDuration(boolean isPositive, BigInteger years,
                BigInteger months, BigInteger days, BigInteger hours,
                BigInteger minutes, BigDecimal seconds) {
            int y = years == null ? DatatypeConstants.FIELD_UNDEFINED : years.intValue();
            int m = months == null ? DatatypeConstants.FIELD_UNDEFINED : months.intValue();
            int d = days == null ? DatatypeConstants.FIELD_UNDEFINED : days.intValue();
            int h = hours == null ? DatatypeConstants.FIELD_UNDEFINED : hours.intValue();
            int mn = minutes == null ? DatatypeConstants.FIELD_UNDEFINED : minutes.intValue();
            int s = seconds == null ? DatatypeConstants.FIELD_UNDEFINED : seconds.intValue();
            return new DurationImpl(isPositive ? 1 : -1, y, m, d, h, mn, s);
        }

        @Override
        public XMLGregorianCalendar newXMLGregorianCalendar() {
            return null;
        }

        @Override
        public XMLGregorianCalendar newXMLGregorianCalendar(String lexicalRepresentation) {
            return null;
        }

        @Override
        public XMLGregorianCalendar newXMLGregorianCalendar(GregorianCalendar cal) {
            return null;
        }

        @Override
        public XMLGregorianCalendar newXMLGregorianCalendar(BigInteger year, int month,
                int day, int hour, int minute, int second,
                BigDecimal fractionalSecond, int timezone) {
            return null;
        }

    }
}
