/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.duration;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeConstants.Field;
import javax.xml.datatype.Duration;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.DurationFormat;
import android.icu.util.Calendar;
import android.icu.util.ULocale;

/**
 * @author srl
 *
 */
public class ICUDurationTest extends TestFmwk {
    /**
     * Allows us to not depend on javax.xml.datatype.DatatypeFactory.
     * We need just a tiny subset of the Duration API:
     * The ICU DurationFormat just extracts the field values,
     * to convert the Duration into an internal Period type.
     */
    private static final class ICUTestDuration extends javax.xml.datatype.Duration {
        private final int sign;
        // Duration docs say BigInteger/BigDecimal but
        // ICU only cares about intValue() and floatValue().
        private final Map<Field, Number> fields;

        ICUTestDuration(long millis) {
            fields = new HashMap<Field, Number>();
            if (millis > 0) {
                sign = 1;
            } else if (millis == 0) {
                sign = 0;
                return;
            } else {
                sign = -1;
                millis = -millis;
            }
            long d = millis / 86400000L;
            millis %= 86400000L;
            if (d > 0) {
                fields.put(DatatypeConstants.DAYS, d);
            }
            long h = millis / 3600000L;
            millis %= 3600000L;
            if (h > 0) {
                fields.put(DatatypeConstants.HOURS, h);
            }
            long m = millis / 60000L;
            millis %= 60000L;
            if (m > 0) {
                fields.put(DatatypeConstants.MINUTES, m);
            }
            fields.put(DatatypeConstants.SECONDS, (float)millis / 1000);
        }

        /**
         * Pass in negative values for fields not to be set.
         */
        ICUTestDuration(int sgn, int y, int months, int d, int h, int m, float s) {
            sign = sgn;
            fields = new HashMap<Field, Number>();
            if (y >= 0) { fields.put(DatatypeConstants.YEARS, y); }
            if (months >= 0) { fields.put(DatatypeConstants.MONTHS, months); }
            if (d >= 0) { fields.put(DatatypeConstants.DAYS, d); }
            if (h >= 0) { fields.put(DatatypeConstants.HOURS, h); }
            if (m >= 0) { fields.put(DatatypeConstants.MINUTES, m); }
            if (s >= 0) { fields.put(DatatypeConstants.SECONDS, s); }
        }

        private ICUTestDuration(int sgn, Map<Field, Number> f) {
            sign = sgn;
            fields = f;
        }

        @Override
        public Duration add(Duration rhs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addTo(java.util.Calendar calendar) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int compare(Duration duration) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Number getField(Field field) {
            return fields.get(field);
        }

        @Override
        public int getSign() {
            return sign;
        }

        @Override
        public int hashCode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSet(Field field) {
            return fields.containsKey(field);
        }

        @Override
        public Duration multiply(BigDecimal factor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Duration negate() {
            return new ICUTestDuration(-sign, fields);
        }

        @Override
        public Duration normalizeWith(java.util.Calendar startTimeInstant) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            String signString = sign > 0 ? "positive" : sign == 0 ? "zero" : "negative";
            return signString + " fields=" + fields;
        }
    }
    private static final ICUTestDuration newDuration(long millis) {
        return new ICUTestDuration(millis);
    }
    private static final ICUTestDuration newDuration(int sgn, int d, int h, int m, float s) {
        return new ICUTestDuration(sgn, -1, -1, d, h, m, s);
    }
    private static final ICUTestDuration newDuration(int sgn, int h, int m, float s) {
        return new ICUTestDuration(sgn, -1, -1, -1, h, m, s);
    }
    private static final ICUTestDuration newDuration(int sgn, float s) {
        return new ICUTestDuration(sgn, -1, -1, -1, -1, -1, s);
    }

    /**
     * 
     */
    public ICUDurationTest() {
    }

    /**
     * Basic test
     */
    @Test
    public void TestBasics() {
        DurationFormat df;
        String expect;
        String formatted;
        
        df = DurationFormat.getInstance(new ULocale("it"));
        formatted = df.formatDurationFromNow(4096);
        expect = "fra quattro secondi";
        if(!expect.equals(formatted)) {
            errln("Expected " + expect + " but got " + formatted);
        } else {
            logln("format duration -> " + formatted);
        }
        
        formatted = df.formatDurationFromNowTo(new Date(0));
        Calendar cal = Calendar.getInstance();
        int years = cal.get(Calendar.YEAR) - 1970; // year of Date(0)
        expect = years + " anni fa";
        if(!expect.equals(formatted)) {
            errln("Expected " + expect + " but got " + formatted);
        } else {
            logln("format date  -> " + formatted);
        }
        
        formatted = df.formatDurationFrom(1000*3600*24, new Date(0).getTime());
        expect = "fra un giorno";
        if(!expect.equals(formatted)) {
            errln("Expected " + expect + " but got " + formatted);
        } else {
            logln("format date from -> " + formatted);
        }

        formatted = df.format(new Long(1000*3600*24*2));
        expect = "fra due giorni";
        if(!expect.equals(formatted)) {
            errln("Expected " + expect + " but got " + formatted);
        } else {
            logln("format long obj -> " + formatted);
        }
    }

    @Test
    public void TestSimpleXMLDuration() {
        Duration d;
        DurationFormat df;
        String out;
        String expected;
        String expected2;
        
        // test 1
        d = newDuration(1, 2, 46, 40);  // "PT2H46M40S"
        df = DurationFormat.getInstance(new ULocale("en"));
        expected = "2 hours, 46 minutes, and 40 seconds";
        out = df.format(d);
        if(out.equals(expected)) {
            logln("out=expected: " + expected + " from " + d);
        } else {
            errln("FAIL: got " + out + " wanted " + expected + " from " + d);
        }
        
        // test 2
        d = newDuration(10000);
        df = DurationFormat.getInstance(new ULocale("en"));
        expected = "10 seconds";
        out = df.format(d);
        if(out.equals(expected)) {
            logln("out=expected: " + expected + " from " + d);
        } else {
            errln("FAIL: got " + out + " wanted " + expected + " from " + d);
        }
        // test 3
        d = newDuration(1, 0, 0, 0, 10);  // "P0DT0H0M10.0S"
        df = DurationFormat.getInstance(new ULocale("en"));
        expected = "10 seconds";
        out = df.format(d);
        if(out.equals(expected)) {
            logln("out=expected: " + expected + " from " + d);
        } else {
            errln("FAIL: got " + out + " wanted " + expected + " from " + d);
        }
        // test 4
        d = newDuration(86400000);
        df = DurationFormat.getInstance(new ULocale("en"));
        expected = "1 day, 0 hours, 0 minutes, and 0 seconds";
        expected2 = "1 day and 0 seconds"; // This is the expected result for Windows with IBM JRE6
        out = df.format(d);
        if(out.equals(expected)) {
            logln("out=expected: " + expected + " from " + d);
        } else {
            if(out.equals(expected2)){
                logln("WARNING: got " + out + " wanted " + expected + " from " + d);
            } else{
                errln("FAIL: got " + out + " wanted " + expected + " from " + d);
            }
        }
    }

    @Test
    public void TestXMLDuration() {
        final class TestCase {
            final String localeString;
            final ULocale locale;
            final String durationString;
            final Duration duration;
            final String expected;

            TestCase(String loc, String ds, Duration d, String exp) {
                localeString = loc;
                locale = new ULocale(loc);
                durationString = ds;
                duration = d;
                expected = exp;
            }
        }

        TestCase cases[] = {
            new TestCase("en", "PT10.00099S",  newDuration(1, 10.00099F),  "10 seconds"),
            new TestCase("en", "#10000",       newDuration(10000),         "10 seconds"),
            new TestCase("en", "-PT10.00099S", newDuration(-1, 10.00099F), "10 seconds"),
            new TestCase("en", "#-10000",      newDuration(-10000),        "10 seconds"),

            // from BD req's
            new TestCase("en", "PT2H46M40S",   newDuration(1, 2, 46, 40),  "2 hours, 46 minutes, and 40 seconds"),
            new TestCase("it", "PT2H46M40S",   newDuration(1, 2, 46, 40),  "due ore, 46 minuti e 40 secondi"),

            // more cases
            new TestCase("en", "PT10S",        newDuration(1, 10),         "10 seconds"),
            new TestCase("en", "PT88M70S",     newDuration(1, -1, 88, 70), "88 minutes and 70 seconds"),
            new TestCase("en", "PT10.100S",    newDuration(1, 10.100F),    "10 seconds and 100 milliseconds"),
            new TestCase("en", "-PT10S",       newDuration(-1, 10),        "10 seconds"),
            new TestCase("en", "PT0H5M0S",     newDuration(1, 0, 5, 0),    "5 minutes and 0 seconds")
        };

        for (TestCase tc : cases) {
            String loc = tc.localeString;
            String from = tc.durationString;
            String to = tc.expected;

            ULocale locale = tc.locale;
            Duration d = tc.duration;

            DurationFormat df = DurationFormat.getInstance(locale);
            String output = df.format(d);
            
            if(output.equals(to)) {
                logln("SUCCESS: locale: " + loc + ", from " + from + " ["+d.toString()+"] " +" to " + to + "= " + output);
            } else {
                logln("FAIL: locale: " + loc + ", from " + from + " ["+d.toString()+"] " +": expected " + to + " got " + output);
            }
        }
    }


    @Test
    public void TestBadObjectError() {
        Runtime r = Runtime.getRuntime();
        DurationFormat df = DurationFormat.getInstance(new ULocale("en"));
        String output = null;
        try {
            output = df.format(r);
            errln("FAIL: did NOT get IllegalArgumentException! Should have. Formatted Runtime as " + output + " ???");
        } catch (IllegalArgumentException iae) {
            logln("PASS: expected: Caught iae: " + iae.toString() );
        }
        // try a second time, because it is a different code path for java < 1.5
        try {
            output = df.format(r);
            errln("FAIL: [#2] did NOT get IllegalArgumentException! Should have. Formatted Runtime as " + output + " ???");
        } catch (IllegalArgumentException iae) {
            logln("PASS: [#2] expected: Caught iae: " + iae.toString() );
        }
    }

    @Test
    public void TestBadLocaleError() {
        try {
            DurationFormat df = DurationFormat.getInstance(new ULocale("und"));
            df.format(new Date());
            logln("Should have thrown err.");
            errln("failed, should have thrown err.");
        } catch(MissingResourceException mre) {
            logln("PASS: caught missing resource exception on locale 'und'");
            logln(mre.toString());
        }
    }

    @Test
    public void TestResourceWithCalendar() {
        DurationFormat df = DurationFormat.getInstance(new ULocale("th@calendar=buddhist"));
        // should pass, but return a default formatter for th.
        if (df == null) {
            errln("FAIL: null DurationFormat returned.");
        }
    }
    
    /* Tests the class
     *      DurationFormat
     */
    @Test
    public void TestDurationFormat(){
        @SuppressWarnings("serial")
        class TestDurationFormat extends DurationFormat {
            public StringBuffer format(Object object, StringBuffer toAppend, FieldPosition pos) {return null;}
            public String formatDurationFrom(long duration, long referenceDate) {return null;}
            public String formatDurationFromNow(long duration) {return null;}
            public String formatDurationFromNowTo(Date targetDate) {return null;}
            public TestDurationFormat() {super();}
            
        }
        
        // Tests the constructor and the following method
        //      public Object parseObject(String source, ParsePosition pos)
        try{
            TestDurationFormat tdf = new TestDurationFormat();
            tdf.parseObject("",null);
            errln("DurationFormat.parseObjet(String,ParsePosition) was " +
                    "to return an exception for an unsupported operation.");
        } catch(Exception e){}
    }

    @Test
    public void TestFromNowTo() {
        class TestCase {
            ULocale locale;
            int diffInSeconds;
            String expected;
            TestCase(ULocale locale, int diffInSeconds, String expected) {
                this.locale = locale;
                this.diffInSeconds = diffInSeconds;
                this.expected = expected;
            }
        }
        TestCase[] testCases = {
            new TestCase(ULocale.US, 10, "10 seconds from now"),
            new TestCase(ULocale.US, -10, "10 seconds ago"),
            new TestCase(ULocale.US, -1800, "30 minutes ago"),
            new TestCase(ULocale.US, 3600, "1 hour from now"),
            new TestCase(ULocale.US, 10000, "2 hours from now"),
            new TestCase(ULocale.US, -20000, "5 hours ago"),
            new TestCase(ULocale.FRANCE, -1800, "il y a 30 minutes"),
            new TestCase(ULocale.ITALY, 10000, "fra due ore"),
        };

        final long delayMS = 10;    // Safe margin - 10 milliseconds
                                    // See the comments below
        for (TestCase test : testCases) {
            DurationFormat df = DurationFormat.getInstance(test.locale);
            long target = System.currentTimeMillis() + test.diffInSeconds * 1000;
            // Need some adjustment because time difference is recalculated in
            // formatDurationFromNowTo method.
            target = test.diffInSeconds > 0 ? target + delayMS : target - delayMS;
            Date d = new Date(target);
            String result = df.formatDurationFromNowTo(d);
            assertEquals("TestFromNowTo (" + test.locale + ", " + test.diffInSeconds + "sec)",
                    test.expected, result);
        }
    }
}
