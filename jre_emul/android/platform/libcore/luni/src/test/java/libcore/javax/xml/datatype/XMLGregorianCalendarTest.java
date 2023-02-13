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
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

public class XMLGregorianCalendarTest extends TestCase {

    private XMLGregorianCalendar calendar;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        calendar = new XMLGregorianCalendarImpl();
    }

    public void testGetMillisecond() {
        assertEquals(DatatypeConstants.FIELD_UNDEFINED, calendar.getMillisecond());
    }

    public void testSetTime_iii() {
        calendar.setTime(1, 2, 3);
        assertEquals(1, calendar.getHour());
        assertEquals(2, calendar.getMinute());
        assertEquals(3, calendar.getSecond());
    }

    public void testSetTime_iiii() {
        calendar.setTime(1, 2, 3, 4);
        assertEquals(1, calendar.getHour());
        assertEquals(2, calendar.getMinute());
        assertEquals(3, calendar.getSecond());
        assertEquals(DatatypeConstants.FIELD_UNDEFINED, calendar.getMillisecond());
    }

    public void testSetTime_iiiBigDecimal() {
        calendar.setTime(1, 2, 3, BigDecimal.valueOf(0.1));
        assertEquals(1, calendar.getHour());
        assertEquals(2, calendar.getMinute());
        assertEquals(3, calendar.getSecond());
        assertEquals(100, calendar.getMillisecond());
        assertEquals(BigDecimal.valueOf(0.1), calendar.getFractionalSecond());
    }

    /**
     * Stub implementation intended for test coverage.
     */
    private static final class XMLGregorianCalendarImpl extends XMLGregorianCalendar {

        private int year;
        private int month;
        private int day;
        private int hour;
        private int minute;
        private int second;
        private int millisecond;
        private BigDecimal fractional;
        private int timezoneOffset;

        @Override
        public void clear() {
            year = month = day = hour = minute = second = millisecond = timezoneOffset = 0;
            fractional = BigDecimal.valueOf(0);
        }

        @Override
        public void reset() {
            year = month = day = hour = minute = second = millisecond = timezoneOffset = 0;
            fractional = BigDecimal.valueOf(0);
        }

        @Override
        public void setYear(BigInteger year) {
            this.year = year.intValue();
        }

        @Override
        public void setYear(int year) {
            this.year = year;
        }

        @Override
        public void setMonth(int month) {
            this.month = month;
        }

        @Override
        public void setDay(int day) {
            this.day = day;
        }

        @Override
        public void setTimezone(int offset) {
            this.timezoneOffset = offset;
        }

        @Override
        public void setHour(int hour) {
            this.hour = hour;
        }

        @Override
        public void setMinute(int minute) {
            this.minute = minute;
        }

        @Override
        public void setSecond(int second) {
            this.second = second;
        }

        @Override
        public void setMillisecond(int millisecond) {
            this.millisecond = millisecond;
        }

        @Override
        public void setFractionalSecond(BigDecimal fractional) {
            this.fractional = fractional;
        }

        @Override
        public BigInteger getEon() {
            return null;
        }

        @Override
        public int getYear() {
            return year;
        }

        @Override
        public BigInteger getEonAndYear() {
            return null;
        }

        @Override
        public int getMonth() {
            return month;
        }

        @Override
        public int getDay() {
            return day;
        }

        @Override
        public int getTimezone() {
            return timezoneOffset;
        }

        @Override
        public int getHour() {
            return hour;
        }

        @Override
        public int getMinute() {
            return minute;
        }

        @Override
        public int getSecond() {
            return second;
        }

        @Override
        public BigDecimal getFractionalSecond() {
            return fractional;
        }

        @Override
        public int compare(XMLGregorianCalendar rhs) {
            if (year != rhs.getYear()) return year - rhs.getYear();
            if (month != rhs.getMonth()) return month - rhs.getMonth();
            if (day != rhs.getDay()) return day - rhs.getDay();
            if (hour != rhs.getHour()) return hour - rhs.getHour();
            if (minute != rhs.getMinute()) return minute - rhs.getMinute();
            if (second != rhs.getSecond()) return second - rhs.getSecond();
            if (millisecond != rhs.getMillisecond()) return millisecond - getMillisecond();
            return fractional.subtract(rhs.getFractionalSecond()).intValue();
        }

        @Override
        public XMLGregorianCalendar normalize() {
            return null;
        }

        @Override
        public String toXMLFormat() {
            return null;
        }

        @Override
        public QName getXMLSchemaType() {
            return null;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public void add(Duration duration) {}

        @Override
        public GregorianCalendar toGregorianCalendar() {
            return null;
        }

        @Override
        public GregorianCalendar toGregorianCalendar(TimeZone timezone, Locale aLocale,
                XMLGregorianCalendar defaults) {
            return null;
        }

        @Override
        public TimeZone getTimeZone(int defaultZoneoffset) {
            return null;
        }

        @Override
        public Object clone() {
            return null;
        }
    }
}
