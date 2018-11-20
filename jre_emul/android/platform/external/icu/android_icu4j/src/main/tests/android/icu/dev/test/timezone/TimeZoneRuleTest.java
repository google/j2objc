/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2011, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.timezone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.util.AnnualTimeZoneRule;
import android.icu.util.BasicTimeZone;
import android.icu.util.Calendar;
import android.icu.util.DateTimeRule;
import android.icu.util.GregorianCalendar;
import android.icu.util.InitialTimeZoneRule;
import android.icu.util.RuleBasedTimeZone;
import android.icu.util.SimpleTimeZone;
import android.icu.util.TimeArrayTimeZoneRule;
import android.icu.util.TimeZone;
import android.icu.util.TimeZoneRule;
import android.icu.util.TimeZoneTransition;
import android.icu.util.ULocale;
import android.icu.util.VTimeZone;

/**
 * Test cases for TimeZoneRule and RuleBasedTimeZone
 */
public class TimeZoneRuleTest extends TestFmwk {

    private static final int HOUR = 60 * 60 * 1000;

    /*
     * RuleBasedTimeZone test cases
     */
    @Test
    public void TestSimpleRuleBasedTimeZone() {
        SimpleTimeZone stz = new SimpleTimeZone(-1*HOUR, "TestSTZ",
                Calendar.SEPTEMBER, -30, -Calendar.SATURDAY, 1*HOUR, SimpleTimeZone.WALL_TIME,
                Calendar.FEBRUARY, 2, Calendar.SUNDAY, 1*HOUR, SimpleTimeZone.WALL_TIME,
                1*HOUR);


        DateTimeRule dtr;
        AnnualTimeZoneRule atzr;
        final int STARTYEAR = 2000;

        InitialTimeZoneRule ir = new InitialTimeZoneRule(
                "RBTZ_Initial", // Initial time Name
                -1*HOUR,        // Raw offset
                1*HOUR);        // DST saving amount
        
        // RBTZ
        RuleBasedTimeZone rbtz1 = new RuleBasedTimeZone("RBTZ1", ir);
        dtr = new DateTimeRule(Calendar.SEPTEMBER, 30, Calendar.SATURDAY, false,
                1*HOUR, DateTimeRule.WALL_TIME); // SUN<=30 in September, at 1AM wall time
        atzr = new AnnualTimeZoneRule("RBTZ_DST1",
                -1*HOUR /* rawOffset */, 1*HOUR /* dstSavings */, dtr,
                STARTYEAR, AnnualTimeZoneRule.MAX_YEAR);
        rbtz1.addTransitionRule(atzr);
        dtr = new DateTimeRule(Calendar.FEBRUARY, 2, Calendar.SUNDAY,
                1*HOUR, DateTimeRule.WALL_TIME); // 2nd Sunday in February, at 1AM wall time
        atzr = new AnnualTimeZoneRule("RBTZ_STD1",
                -1*HOUR /* rawOffset */, 0 /* dstSavings */, dtr,
                STARTYEAR, AnnualTimeZoneRule.MAX_YEAR);
        rbtz1.addTransitionRule(atzr);

        // Equivalent, but different date rule type
        RuleBasedTimeZone rbtz2 = new RuleBasedTimeZone("RBTZ2", ir);
        dtr = new DateTimeRule(Calendar.SEPTEMBER, -1, Calendar.SATURDAY,
                1*HOUR, DateTimeRule.WALL_TIME); // Last Sunday in September at 1AM wall time
        atzr = new AnnualTimeZoneRule("RBTZ_DST2", -1*HOUR, 1*HOUR, dtr, STARTYEAR, AnnualTimeZoneRule.MAX_YEAR);
        rbtz2.addTransitionRule(atzr);
        dtr = new DateTimeRule(Calendar.FEBRUARY, 8, Calendar.SUNDAY, true,
                1*HOUR, DateTimeRule.WALL_TIME); // SUN>=8 in February, at 1AM wall time
        atzr = new AnnualTimeZoneRule("RBTZ_STD2", -1*HOUR, 0, dtr, STARTYEAR, AnnualTimeZoneRule.MAX_YEAR);
        rbtz2.addTransitionRule(atzr);

        // Equivalent, but different time rule type
        RuleBasedTimeZone rbtz3 = new RuleBasedTimeZone("RBTZ3", ir);
        dtr = new DateTimeRule(Calendar.SEPTEMBER, 30, Calendar.SATURDAY, false,
                2*HOUR, DateTimeRule.UTC_TIME);
        atzr = new AnnualTimeZoneRule("RBTZ_DST3", -1*HOUR, 1*HOUR, dtr, STARTYEAR, AnnualTimeZoneRule.MAX_YEAR);
        rbtz3.addTransitionRule(atzr);
        dtr = new DateTimeRule(Calendar.FEBRUARY, 2, Calendar.SUNDAY,
                0*HOUR, DateTimeRule.STANDARD_TIME);
        atzr = new AnnualTimeZoneRule("RBTZ_STD3", -1*HOUR, 0, dtr, STARTYEAR, AnnualTimeZoneRule.MAX_YEAR);
        rbtz3.addTransitionRule(atzr);

        // Check equivalency for 10 years
        long start = getUTCMillis(STARTYEAR, Calendar.JANUARY, 1);
        long until = getUTCMillis(STARTYEAR + 10, Calendar.JANUARY, 1);

        if (!(stz.hasEquivalentTransitions(rbtz1, start, until))) {
            errln("FAIL: rbtz1 must be equivalent to the SimpleTimeZone in the time range.");
        }
        if (!(stz.hasEquivalentTransitions(rbtz2, start, until))) {
            errln("FAIL: rbtz2 must be equivalent to the SimpleTimeZone in the time range.");
        }
        if (!(stz.hasEquivalentTransitions(rbtz3, start, until))) {
            errln("FAIL: rbtz3 must be equivalent to the SimpleTimeZone in the time range.");
        }

        // hasSameRules
        if (rbtz1.hasSameRules(rbtz2)) {
            errln("FAIL: rbtz1 and rbtz2 have different rules, but returned true.");
        }
        if (rbtz1.hasSameRules(rbtz3)) {
            errln("FAIL: rbtz1 and rbtz3 have different rules, but returned true.");
        }
        RuleBasedTimeZone rbtz1c = (RuleBasedTimeZone)rbtz1.clone();
        if (!rbtz1.hasSameRules(rbtz1c)) {
            errln("FAIL: Cloned RuleBasedTimeZone must have the same rules with the original.");
        }

        // getOffset
        GregorianCalendar cal = new GregorianCalendar();
        int[] offsets = new int[2];
        int offset;
        boolean dst;

        cal.setTimeZone(rbtz1);
        cal.clear();

        // Jan 1, 1000 BC
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(1000, Calendar.JANUARY, 1);

        offset = rbtz1.getOffset(cal.get(Calendar.ERA), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.MILLISECONDS_IN_DAY));
        if (offset != 0) {
            errln("FAIL: Invalid time zone offset: " + offset + " /expected: 0");
        }
        dst = rbtz1.inDaylightTime(cal.getTime());
        if (!dst) {
            errln("FAIL: Invalid daylight saving time");
        }
        rbtz1.getOffset(cal.getTimeInMillis(), true, offsets);
        if (offsets[0] != -3600000) {
            errln("FAIL: Invalid time zone raw offset: " + offsets[0] + " /expected: -3600000");
        }
        if (offsets[1] != 3600000) {            
            errln("FAIL: Invalid DST amount: " + offsets[1] + " /expected: 3600000");
        }

        // July 1, 2000, AD
        cal.set(Calendar.ERA, GregorianCalendar.AD);
        cal.set(2000, Calendar.JULY, 1);

        offset = rbtz1.getOffset(cal.get(Calendar.ERA), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.MILLISECONDS_IN_DAY));
        if (offset != -3600000) {
            errln("FAIL: Invalid time zone offset: " + offset + " /expected: -3600000");
        }
        dst = rbtz1.inDaylightTime(cal.getTime());
        if (dst) {
            errln("FAIL: Invalid daylight saving time");
        }
        rbtz1.getOffset(cal.getTimeInMillis(), true, offsets);
        if (offsets[0] != -3600000) {
            errln("FAIL: Invalid time zone raw offset: " + offsets[0] + " /expected: -3600000");
        }
        if (offsets[1] != 0) {            
            errln("FAIL: Invalid DST amount: " + offsets[1] + " /expected: 0");
        }

        // July 1, 2000, AD

        // Try to add 3rd final rule
        dtr = new DateTimeRule(Calendar.OCTOBER, 15, 1*HOUR, DateTimeRule.WALL_TIME);
        atzr = new AnnualTimeZoneRule("3RD_ATZ", -1*HOUR, 2*HOUR, dtr, STARTYEAR, AnnualTimeZoneRule.MAX_YEAR);
        boolean bException = false;
        try {
            rbtz1.addTransitionRule(atzr);
        } catch (IllegalStateException ise) {
            bException = true;
        }
        if (!bException) {
            errln("FAIL: 3rd final rule must be rejected");
        }

        // Try to add an initial rule
        bException = false;
        try {
            rbtz1.addTransitionRule(new InitialTimeZoneRule("Test Initial", 2*HOUR, 0));
        } catch (IllegalArgumentException iae) {
            bException = true;
        }
        if (!bException) {
            errln("FAIL: InitialTimeZoneRule must be rejected");
        }
    }

    /*
     * Test equivalency between OlsonTimeZone and custom RBTZ representing the
     * equivalent rules in a certain time range
     */
    @Test
    public void TestHistoricalRuleBasedTimeZone() {
        // Compare to America/New_York with equivalent RBTZ
        TimeZone ny = TimeZone.getTimeZone("America/New_York", TimeZone.TIMEZONE_ICU);

        //RBTZ
        InitialTimeZoneRule ir = new InitialTimeZoneRule("EST", -5*HOUR, 0);
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone("EST5EDT", ir);

        DateTimeRule dtr;
        AnnualTimeZoneRule tzr;

        // Standard time
        dtr = new DateTimeRule(Calendar.OCTOBER, -1, Calendar.SUNDAY,
                2*HOUR, DateTimeRule.WALL_TIME);    // Last Sunday in October, at 2AM wall time
        tzr = new AnnualTimeZoneRule("EST", -5*HOUR /* rawOffset */, 0 /* dstSavings */, dtr, 1967, 2006);
        rbtz.addTransitionRule(tzr);

        dtr = new DateTimeRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY,
                true, 2*HOUR, DateTimeRule.WALL_TIME);  // SUN>=1 in November, at 2AM wall time
        tzr = new AnnualTimeZoneRule("EST", -5*HOUR, 0, dtr, 2007, AnnualTimeZoneRule.MAX_YEAR);
        rbtz.addTransitionRule(tzr);

        // Daylight saving time
        dtr = new DateTimeRule(Calendar.APRIL, -1, Calendar.SUNDAY,
                2*HOUR, DateTimeRule.WALL_TIME);    // Last Sunday in April, at 2AM wall time
        tzr = new AnnualTimeZoneRule("EDT", -5*HOUR, 1*HOUR, dtr, 1967, 1973);
        rbtz.addTransitionRule(tzr);

        dtr = new DateTimeRule(Calendar.JANUARY, 6,
                2*HOUR, DateTimeRule.WALL_TIME);    // January 6, at 2AM wall time
        tzr = new AnnualTimeZoneRule("EDT", -5*HOUR, 1*HOUR, dtr, 1974, 1974);
        rbtz.addTransitionRule(tzr);
        
        dtr = new DateTimeRule(Calendar.FEBRUARY, 23,
                2*HOUR, DateTimeRule.WALL_TIME);    // February 23, at 2AM wall time
        tzr = new AnnualTimeZoneRule("EDT", -5*HOUR, 1*HOUR, dtr, 1975, 1975);
        rbtz.addTransitionRule(tzr);

        dtr = new DateTimeRule(Calendar.APRIL, -1, Calendar.SUNDAY,
                2*HOUR, DateTimeRule.WALL_TIME);    // Last Sunday in April, at 2AM wall time
        tzr = new AnnualTimeZoneRule("EDT", -5*HOUR, 1*HOUR, dtr, 1976, 1986);
        rbtz.addTransitionRule(tzr);

        dtr = new DateTimeRule(Calendar.APRIL, 1, Calendar.SUNDAY,
                true, 2*HOUR, DateTimeRule.WALL_TIME);  // SUN>=1 in April, at 2AM wall time
        tzr = new AnnualTimeZoneRule("EDT", -5*HOUR, 1*HOUR, dtr, 1987, 2006);
        rbtz.addTransitionRule(tzr);

        dtr = new DateTimeRule(Calendar.MARCH, 8, Calendar.SUNDAY,
                true, 2*HOUR, DateTimeRule.WALL_TIME);  // SUN>=8 in March, at 2AM wall time
        tzr = new AnnualTimeZoneRule("EDT", -5*HOUR, 1*HOUR, dtr, 2007, AnnualTimeZoneRule.MAX_YEAR);
        rbtz.addTransitionRule(tzr);

        // hasEquivalentTransitions
        long jan1_1950 = getUTCMillis(1950, Calendar.JANUARY, 1);
        long jan1_1967 = getUTCMillis(1971, Calendar.JANUARY, 1);
        long jan1_2010 = getUTCMillis(2010, Calendar.JANUARY, 1);        

        if (!(((BasicTimeZone)ny).hasEquivalentTransitions(rbtz, jan1_1967, jan1_2010))) {
            errln("FAIL: The RBTZ must be equivalent to America/New_York between 1967 and 2010");
        }
        if (((BasicTimeZone)ny).hasEquivalentTransitions(rbtz, jan1_1950, jan1_2010)) {
            errln("FAIL: The RBTZ must not be equivalent to America/New_York between 1950 and 2010");
        }

        // Same with above, but calling RBTZ#hasEquivalentTransitions against OlsonTimeZone
        if (!rbtz.hasEquivalentTransitions(ny, jan1_1967, jan1_2010)) {
            errln("FAIL: The RBTZ must be equivalent to America/New_York between 1967 and 2010");
        }
        if (rbtz.hasEquivalentTransitions(ny, jan1_1950, jan1_2010)) {
            errln("FAIL: The RBTZ must not be equivalent to America/New_York between 1950 and 2010");
        }

        // TimeZone APIs
        if (ny.hasSameRules(rbtz) || rbtz.hasSameRules(ny)) {
            errln("FAIL: hasSameRules must return false");
        }
        RuleBasedTimeZone rbtzc = (RuleBasedTimeZone)rbtz.clone();
        if (!rbtz.hasSameRules(rbtzc) || !rbtz.hasEquivalentTransitions(rbtzc, jan1_1950, jan1_2010)) {
            errln("FAIL: hasSameRules/hasEquivalentTransitions must return true for cloned RBTZs");
        }

        long times[] = {
           getUTCMillis(2006, Calendar.MARCH, 15),
           getUTCMillis(2006, Calendar.NOVEMBER, 1),
           getUTCMillis(2007, Calendar.MARCH, 15),
           getUTCMillis(2007, Calendar.NOVEMBER, 1),
           getUTCMillis(2008, Calendar.MARCH, 15),
           getUTCMillis(2008, Calendar.NOVEMBER, 1)
        };
        int[] offsets1 = new int[2];
        int[] offsets2 = new int[2];

        for (int i = 0; i < times.length; i++) {
            // Check getOffset - must return the same results for these time data
            rbtz.getOffset(times[i], false, offsets1);
            ny.getOffset(times[i], false, offsets2);
            if (offsets1[0] != offsets2[0] || offsets1[1] != offsets2[1]) {
                errln("FAIL: Incompatible time zone offsets for ny and rbtz");
            }
            // Check inDaylightTime
            Date d = new Date(times[i]);
            if (rbtz.inDaylightTime(d) != ny.inDaylightTime(d)) {
                errln("FAIL: Incompatible daylight saving time for ny and rbtz");
            }
        }
    }

    /*
     * Check if transitions returned by getNextTransition/getPreviousTransition
     * are actual time transitions.
     */
    @Test
    public void TestOlsonTransition() {
        String[] zids = getTestZIDs();
        for (int i = 0; i < zids.length; i++) {
            TimeZone tz = TimeZone.getTimeZone(zids[i], TimeZone.TIMEZONE_ICU);
            if (tz == null) {
                break;
            }
            int j = 0;
            while (true) {
                long[] timerange = getTestTimeRange(j++);
                if (timerange == null) {
                    break;
                }
                verifyTransitions(tz, timerange[0], timerange[1]);
            }
        }
    }

    /*
     * Check if an OlsonTimeZone and its equivalent RBTZ have the exact same
     * transitions.
     */
    @Test
    public void TestRBTZTransition() {
        int[] STARTYEARS = {
            1950,
            1975,
            2000,
            2010
        };

        String[] zids = getTestZIDs();
        for (int i = 0; i < zids.length; i++) {
            TimeZone tz = TimeZone.getTimeZone(zids[i], TimeZone.TIMEZONE_ICU);
            if (tz == null) {
                break;
            }
            for (int j = 0; j < STARTYEARS.length; j++) {
                long startTime = getUTCMillis(STARTYEARS[j], Calendar.JANUARY, 1);
                TimeZoneRule[] rules = ((BasicTimeZone)tz).getTimeZoneRules(startTime);
                RuleBasedTimeZone rbtz = new RuleBasedTimeZone(tz.getID() + "(RBTZ)",
                        (InitialTimeZoneRule)rules[0]);
                for (int k = 1; k < rules.length; k++) {
                    rbtz.addTransitionRule(rules[k]);
                }

                // Compare the original OlsonTimeZone with the RBTZ starting the startTime for 20 years
                long until = getUTCMillis(STARTYEARS[j] + 20, Calendar.JANUARY, 1);

                // Ascending
                compareTransitionsAscending(tz, rbtz, startTime, until, false);
                // Ascending/inclusive
                compareTransitionsAscending(tz, rbtz, startTime + 1, until, true);
                // Descending
                compareTransitionsDescending(tz, rbtz, startTime, until, false);
                // Descending/inclusive
                compareTransitionsDescending(tz, rbtz, startTime + 1, until, true);
            }
            
        }
    }

    /*
     * Test cases for HasTimeZoneRules#hasEquivalentTransitions
     */
    @Test
    public void TestHasEquivalentTransitions() {
        // America/New_York and America/Indiana/Indianapolis are equivalent
        // since 2006
        TimeZone newyork = TimeZone.getTimeZone("America/New_York", TimeZone.TIMEZONE_ICU);
        TimeZone indianapolis = TimeZone.getTimeZone("America/Indiana/Indianapolis", TimeZone.TIMEZONE_ICU);
        TimeZone gmt_5 = TimeZone.getTimeZone("Etc/GMT+5", TimeZone.TIMEZONE_ICU);

        long jan1_1971 = getUTCMillis(1971, Calendar.JANUARY, 1);
        long jan1_2005 = getUTCMillis(2005, Calendar.JANUARY, 1);
        long jan1_2006 = getUTCMillis(2006, Calendar.JANUARY, 1);
        long jan1_2007 = getUTCMillis(2007, Calendar.JANUARY, 1);
        long jan1_2011 = getUTCMillis(2010, Calendar.JANUARY, 1);
        
        if (((BasicTimeZone)newyork).hasEquivalentTransitions(indianapolis, jan1_2005, jan1_2011)) {
            errln("FAIL: New_York is not equivalent to Indianapolis between 2005 and 2010, but returned true");
        }
        if (!((BasicTimeZone)newyork).hasEquivalentTransitions(indianapolis, jan1_2006, jan1_2011)) {
            errln("FAIL: New_York is equivalent to Indianapolis between 2006 and 2010, but returned false");
        }

        if (!((BasicTimeZone)indianapolis).hasEquivalentTransitions(gmt_5, jan1_1971, jan1_2006)) {
            errln("FAIL: Indianapolis is equivalent to GMT+5 between 1971 and 2005, but returned false");
        }
        if (((BasicTimeZone)indianapolis).hasEquivalentTransitions(gmt_5, jan1_1971, jan1_2007)) {
            errln("FAIL: Indianapolis is not equivalent to GMT+5 between 1971 and 2006, but returned true");
        }

        // Cloned TimeZone
        TimeZone newyork2 = (TimeZone)newyork.clone();
        if (!((BasicTimeZone)newyork).hasEquivalentTransitions(newyork2, jan1_1971, jan1_2011)) {
            errln("FAIL: Cloned TimeZone must have the same transitions");
        }
        if (!((BasicTimeZone)newyork).hasEquivalentTransitions(newyork2, jan1_1971, jan1_2011, true /*ignoreDstAmount*/)) {
            errln("FAIL: Cloned TimeZone must have the same transitions");
        }

        // America/New_York and America/Los_Angeles has same DST start rules, but
        // raw offsets are different
        TimeZone losangeles = TimeZone.getTimeZone("America/Los_Angeles", TimeZone.TIMEZONE_ICU);
        if (((BasicTimeZone)newyork).hasEquivalentTransitions(losangeles, jan1_2006, jan1_2011)) {
            errln("FAIL: New_York is not equivalent to Los Angeles, but returned true");
        }
    }

    /*
     * Write out time zone rules of OlsonTimeZone into VTIMEZONE format, create a new
     * VTimeZone from the VTIMEZONE data, then compare transitions
     */
    @Test
    public void TestVTimeZoneRoundTrip() {
        long startTime = getUTCMillis(1850, Calendar.JANUARY, 1);
        long endTime = getUTCMillis(2050, Calendar.JANUARY, 1);

        String[] tzids = getTestZIDs();
        for (int i = 0; i < tzids.length; i++) {
            BasicTimeZone olsontz = (BasicTimeZone)TimeZone.getTimeZone(tzids[i], TimeZone.TIMEZONE_ICU);
            VTimeZone vtz_org = VTimeZone.create(tzids[i]);
            vtz_org.setTZURL("http://source.icu-project.org/timezone");
            vtz_org.setLastModified(new Date());
            VTimeZone vtz_new = null;
            try {
                // Write out VTIMEZONE
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(baos);
                vtz_org.write(writer);
                writer.close();
                byte[] vtzdata = baos.toByteArray();
                // Read VTIMEZONE
                ByteArrayInputStream bais = new ByteArrayInputStream(vtzdata);
                InputStreamReader reader = new InputStreamReader(bais);
                vtz_new = VTimeZone.create(reader);
                reader.close();

                // Write out VTIMEZONE one more time
                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                OutputStreamWriter writer1 = new OutputStreamWriter(baos1);
                vtz_new.write(writer1);
                writer1.close();
                byte[] vtzdata1 = baos1.toByteArray();

                // Make sure VTIMEZONE data is exactly same with the first one
                if (vtzdata.length != vtzdata1.length) {
                    errln("FAIL: different VTIMEZONE data length");
                }
                for (int j = 0; j < vtzdata.length; j++) {
                    if (vtzdata[j] != vtzdata1[j]) {
                        errln("FAIL: different VTIMEZONE data");
                        break;
                    }
                }
            } catch (IOException ioe) {
                errln("FAIL: IO error while writing/reading VTIMEZONE data");
            }
            // Check equivalency after the first transition.
            // The DST information before the first transition might be lost
            // because there is no good way to represent the initial time with
            // VTIMEZONE.
            if (vtz_new.getOffset(startTime) != olsontz.getOffset(startTime)) {
                errln("FAIL: VTimeZone for " + tzids[i]
                         + " is not equivalent to its OlsonTimeZone corresponding at " + startTime);
            }
            TimeZoneTransition tzt = olsontz.getNextTransition(startTime, false);
            if (tzt != null) {
                if (!vtz_new.hasEquivalentTransitions(olsontz, tzt.getTime(), endTime, true)) {
                    int maxDelta = 1000;
                    if (!hasEquivalentTransitions(vtz_new, olsontz, tzt.getTime() + maxDelta, endTime, true, maxDelta)) {
                        errln("FAIL: VTimeZone for " + tzids[i] + " is not equivalent to its OlsonTimeZone corresponding.");
                    } else {
                        logln("VTimeZone for " + tzids[i] + " differs from its OlsonTimeZone corresponding with maximum transition time delta - " + maxDelta);
                    }
                }
                if (!vtz_new.hasEquivalentTransitions(olsontz, tzt.getTime(), endTime, false)) {
                    logln("VTimeZone for " + tzids[i] + " is not equivalent to its OlsonTimeZone corresponding in strict comparison mode.");
                }
            }
        }
    }

    /*
     * Write out time zone rules of OlsonTimeZone after a cutoff date into VTIMEZONE format,
     * create a new VTimeZone from the VTIMEZONE data, then compare transitions
     */
    @Test
    public void TestVTimeZoneRoundTripPartial() {
        long[] startTimes = new long[] {
            getUTCMillis(1900, Calendar.JANUARY, 1),
            getUTCMillis(1950, Calendar.JANUARY, 1),
            getUTCMillis(2020, Calendar.JANUARY, 1)
        };
        long endTime = getUTCMillis(2050, Calendar.JANUARY, 1);

        String[] tzids = getTestZIDs();
        for (int n = 0; n < startTimes.length; n++) {
            long startTime = startTimes[n];
            for (int i = 0; i < tzids.length; i++) {
                BasicTimeZone olsontz = (BasicTimeZone)TimeZone.getTimeZone(tzids[i], TimeZone.TIMEZONE_ICU);
                VTimeZone vtz_org = VTimeZone.create(tzids[i]);
                VTimeZone vtz_new = null;
                try {
                    // Write out VTIMEZONE
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(baos);
                    vtz_org.write(writer, startTime);
                    writer.close();
                    byte[] vtzdata = baos.toByteArray();
                    // Read VTIMEZONE
                    ByteArrayInputStream bais = new ByteArrayInputStream(vtzdata);
                    InputStreamReader reader = new InputStreamReader(bais);
                    vtz_new = VTimeZone.create(reader);
                    reader.close();

                } catch (IOException ioe) {
                    errln("FAIL: IO error while writing/reading VTIMEZONE data");
                }
                // Check equivalency after the first transition.
                // The DST information before the first transition might be lost
                // because there is no good way to represent the initial time with
                // VTIMEZONE.
                if (vtz_new.getOffset(startTime) != olsontz.getOffset(startTime)) {
                    errln("FAIL: VTimeZone for " + tzids[i]
                             + " is not equivalent to its OlsonTimeZone corresponding at " + startTime);
                }
                TimeZoneTransition tzt = olsontz.getNextTransition(startTime, false);
                if (tzt != null) {
                    if (!vtz_new.hasEquivalentTransitions(olsontz, tzt.getTime(), endTime, true)) {
                        int maxDelta = 1000;
                        if (!hasEquivalentTransitions(vtz_new, olsontz, tzt.getTime() + maxDelta, endTime, true, maxDelta)) {
                            errln("FAIL: VTimeZone for " + tzids[i] + "(>=" + startTime + ") is not equivalent to its OlsonTimeZone corresponding.");
                        } else {
                            logln("VTimeZone for " + tzids[i] + "(>=" + startTime + ")  differs from its OlsonTimeZone corresponding with maximum transition time delta - " + maxDelta);
                        }
                    }
                }
            }
        }
    }

    /*
     * Write out simple time zone rules from an OlsonTimeZone at various time into VTIMEZONE
     * format and create a new VTimeZone from the VTIMEZONE data, then make sure the raw offset
     * and DST savings are same in these two time zones.
     */
    @Test
    public void TestVTimeZoneSimpleWrite() {
        long[] testTimes = new long[] {
                getUTCMillis(2006, Calendar.JANUARY, 1),
                getUTCMillis(2006, Calendar.MARCH, 15),
                getUTCMillis(2006, Calendar.MARCH, 31),
                getUTCMillis(2006, Calendar.APRIL, 5),
                getUTCMillis(2006, Calendar.OCTOBER, 25),
                getUTCMillis(2006, Calendar.NOVEMBER, 1),
                getUTCMillis(2006, Calendar.NOVEMBER, 5),
                getUTCMillis(2007, Calendar.JANUARY, 1)
        };

        String[] tzids = getTestZIDs();
        for (int n = 0; n < testTimes.length; n++) {
            long time = testTimes[n];

            int[] offsets1 = new int[2];
            int[] offsets2 = new int[2];

            for (int i = 0; i < tzids.length; i++) {
                VTimeZone vtz_org = VTimeZone.create(tzids[i]);
                VTimeZone vtz_new = null;
                try {
                    // Write out VTIMEZONE
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(baos);
                    vtz_org.writeSimple(writer, time);
                    writer.close();
                    byte[] vtzdata = baos.toByteArray();
                    // Read VTIMEZONE
                    ByteArrayInputStream bais = new ByteArrayInputStream(vtzdata);
                    InputStreamReader reader = new InputStreamReader(bais);
                    vtz_new = VTimeZone.create(reader);
                    reader.close();
                } catch (IOException ioe) {
                    errln("FAIL: IO error while writing/reading VTIMEZONE data");
                }

                // Check equivalency
                vtz_org.getOffset(time, false, offsets1);
                vtz_new.getOffset(time, false, offsets2);
                if (offsets1[0] != offsets2[0] || offsets1[1] != offsets2[1]) {
                    errln("FAIL: VTimeZone writeSimple for " + tzids[i] + " at time " + time + " failed to the round trip.");
                }
            }
        }
    }

    /*
     * Write out time zone rules of OlsonTimeZone into VTIMEZONE format with RFC2445 header TZURL and
     * LAST-MODIFIED, create a new VTimeZone from the VTIMEZONE data to see if the headers are preserved.
     */
    @Test
    public void TestVTimeZoneHeaderProps() {
        String tzid = "America/Chicago";
        String tzurl = "http://source.icu-project.org";
        Date lastmod = new Date(getUTCMillis(2007, Calendar.JUNE, 1));

        VTimeZone vtz = VTimeZone.create(tzid);
        vtz.setTZURL(tzurl);
        vtz.setLastModified(lastmod);

        // Roundtrip conversion
        VTimeZone newvtz1 = null;
        try {
            // Write out VTIMEZONE
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos);
            vtz.write(writer);
            writer.close();
            byte[] vtzdata = baos.toByteArray();
            // Read VTIMEZONE
            ByteArrayInputStream bais = new ByteArrayInputStream(vtzdata);
            InputStreamReader reader = new InputStreamReader(bais);
            newvtz1 = VTimeZone.create(reader);
            reader.close();

            // Check if TZURL and LAST-MODIFIED headers are preserved
            if (!(tzurl.equals(newvtz1.getTZURL()))) {
                errln("FAIL: TZURL property is not preserved during the roundtrip conversion.  Before:"
                        + tzurl + "/After:" + newvtz1.getTZURL());
            }
            if (!(lastmod.equals(newvtz1.getLastModified()))) {
                errln("FAIL: LAST-MODIFIED property is not preserved during the roundtrip conversion.  Before:"
                        + lastmod.getTime() + "/After:" + newvtz1.getLastModified().getTime());
            }
        } catch (IOException ioe) {
            errln("FAIL: IO error while writing/reading VTIMEZONE data");
        }

        // Second roundtrip, with a cutoff
        VTimeZone newvtz2 = null;
        try {
            // Set different tzurl
            String newtzurl = "http://www.ibm.com";
            newvtz1.setTZURL(newtzurl);
            // Write out VTIMEZONE
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos);
            newvtz1.write(writer, getUTCMillis(2000, Calendar.JANUARY, 1));
            writer.close();
            byte[] vtzdata = baos.toByteArray();
            // Read VTIMEZONE
            ByteArrayInputStream bais = new ByteArrayInputStream(vtzdata);
            InputStreamReader reader = new InputStreamReader(bais);
            newvtz2 = VTimeZone.create(reader);
            reader.close();

            // Check if TZURL and LAST-MODIFIED headers are preserved
            if (!(newtzurl.equals(newvtz2.getTZURL()))) {
                errln("FAIL: TZURL property is not preserved during the second roundtrip conversion.  Before:"
                        + newtzurl + "/After:" + newvtz2.getTZURL());
            }
            if (!(lastmod.equals(newvtz2.getLastModified()))) {
                errln("FAIL: LAST-MODIFIED property is not preserved during the second roundtrip conversion.  Before:"
                        + lastmod.getTime() + "/After:" + newvtz2.getLastModified().getTime());
            }
        } catch (IOException ioe) {
            errln("FAIL: IO error while writing/reading VTIMEZONE data");
        }
        
    }

    /*
     * Extract simple rules from an OlsonTimeZone and make sure the rule format matches
     * the expected format.
     */
    @Test
    public void TestGetSimpleRules() {
        long[] testTimes = new long[] {
                getUTCMillis(1970, Calendar.JANUARY, 1),
                getUTCMillis(2000, Calendar.MARCH, 31),
                getUTCMillis(2005, Calendar.JULY, 1),
                getUTCMillis(2010, Calendar.NOVEMBER, 1),
            };

        String[] tzids = getTestZIDs();
        for (int n = 0; n < testTimes.length; n++) {
            long time = testTimes[n];
            for (int i = 0; i < tzids.length; i++) {
                BasicTimeZone tz = (BasicTimeZone)TimeZone.getTimeZone(tzids[i], TimeZone.TIMEZONE_ICU);
                TimeZoneRule[] rules = tz.getSimpleTimeZoneRulesNear(time);
                if (rules == null) {
                    errln("FAIL: Failed to extract simple rules for " + tzids[i] + " at " + time);
                } else {
                    if (rules.length == 1) {
                        if (!(rules[0] instanceof InitialTimeZoneRule)) {
                            errln("FAIL: Unexpected rule object type is returned for " + tzids[i] + " at " + time);
                        }
                    } else if (rules.length == 3) {
                        if (!(rules[0] instanceof InitialTimeZoneRule)
                                || !(rules[1] instanceof AnnualTimeZoneRule)
                                || !(rules[2] instanceof AnnualTimeZoneRule)) {
                            errln("FAIL: Unexpected rule object type is returned for " + tzids[i] + " at " + time);
                        }
                        for (int idx = 1; idx <= 2; idx++) {
                            DateTimeRule dtr = ((AnnualTimeZoneRule)rules[idx]).getRule();
                            if (dtr.getTimeRuleType() != DateTimeRule.WALL_TIME) {
                                errln("FAIL: WALL_TIME is not used as the time rule in the time zone rule(" + idx + ") for " + tzids[i] + " at " + time);
                            }
                            if (dtr.getDateRuleType() != DateTimeRule.DOW) {
                                errln("FAIL: DOW is not used as the date rule in the time zone rule(" + idx + ") for " + tzids[i] + " at " + time);
                            }
                        }
                    } else {
                        errln("FAIL: Unexpected number of rules returned for " + tzids[i] + " at " + time);
                    }
                }
            }
        }
    }

    /*
     * API coverage tests for TimeZoneRule 
     */
    @Test
    public void TestTimeZoneRuleCoverage() {
        long time1 = getUTCMillis(2005, Calendar.JULY, 4);
        long time2 = getUTCMillis(2015, Calendar.JULY, 4);
        long time3 = getUTCMillis(1950, Calendar.JULY, 4);

        DateTimeRule dtr1 = new DateTimeRule(Calendar.FEBRUARY, 29, Calendar.SUNDAY, false,
                3*HOUR, DateTimeRule.WALL_TIME); // Last Sunday on or before Feb 29, at 3 AM, wall time
        DateTimeRule dtr2 = new DateTimeRule(Calendar.MARCH, 11, 2*HOUR,
                DateTimeRule.STANDARD_TIME); // Mar 11, at 2 AM, standard time
        DateTimeRule dtr3 = new DateTimeRule(Calendar.OCTOBER, -1, Calendar.SATURDAY,
                6*HOUR, DateTimeRule.UTC_TIME); //Last Saturday in Oct, at 6 AM, UTC
        DateTimeRule dtr4 = new DateTimeRule(Calendar.MARCH, 8, Calendar.SUNDAY, true,
                2*HOUR, DateTimeRule.WALL_TIME); // First Sunday on or after Mar 8, at 2 AM, wall time

        AnnualTimeZoneRule a1 = new AnnualTimeZoneRule("a1", -3*HOUR, 1*HOUR, dtr1,
                2000, AnnualTimeZoneRule.MAX_YEAR);
        AnnualTimeZoneRule a2 = new AnnualTimeZoneRule("a2", -3*HOUR, 1*HOUR, dtr1,
                2000, AnnualTimeZoneRule.MAX_YEAR);
        AnnualTimeZoneRule a3 = new AnnualTimeZoneRule("a3", -3*HOUR, 1*HOUR, dtr1,
                2000, 2010);
        
        InitialTimeZoneRule i1 = new InitialTimeZoneRule("i1", -3*HOUR, 0);
        InitialTimeZoneRule i2 = new InitialTimeZoneRule("i2", -3*HOUR, 0);
        InitialTimeZoneRule i3 = new InitialTimeZoneRule("i3", -3*HOUR, 1*HOUR);
        
        long[] emptytimes = {};
        long[] trtimes1 = {0};
        long[] trtimes2 = {0, 10000000};

        TimeArrayTimeZoneRule t0 = null;
        try {
            // Try to construct TimeArrayTimeZoneRule with null transition times
            t0 = new TimeArrayTimeZoneRule("nulltimes", -3*HOUR, 0,
                    null, DateTimeRule.UTC_TIME);
        } catch (IllegalArgumentException iae) {
            logln("TimeArrayTimeZoneRule constructor throws IllegalArgumentException as expected.");
            t0 = null;
        }
        if (t0 != null) {
            errln("FAIL: TimeArrayTimeZoneRule constructor did not throw IllegalArgumentException for null times");
        }
        
        try {
            // Try to construct TimeArrayTimeZoneRule with empty transition times
            t0 = new TimeArrayTimeZoneRule("nulltimes", -3*HOUR, 0,
                    emptytimes, DateTimeRule.UTC_TIME);
        } catch (IllegalArgumentException iae) {
            logln("TimeArrayTimeZoneRule constructor throws IllegalArgumentException as expected.");
            t0 = null;
        }
        if (t0 != null) {
            errln("FAIL: TimeArrayTimeZoneRule constructor did not throw IllegalArgumentException for empty times");
        }

        TimeArrayTimeZoneRule t1 = new TimeArrayTimeZoneRule("t1", -3*HOUR, 0, trtimes1, DateTimeRule.UTC_TIME);
        TimeArrayTimeZoneRule t2 = new TimeArrayTimeZoneRule("t2", -3*HOUR, 0, trtimes1, DateTimeRule.UTC_TIME);
        TimeArrayTimeZoneRule t3 = new TimeArrayTimeZoneRule("t3", -3*HOUR, 0, trtimes2, DateTimeRule.UTC_TIME);
        TimeArrayTimeZoneRule t4 = new TimeArrayTimeZoneRule("t4", -3*HOUR, 0, trtimes1, DateTimeRule.STANDARD_TIME);
        TimeArrayTimeZoneRule t5 = new TimeArrayTimeZoneRule("t5", -4*HOUR, 1*HOUR, trtimes1, DateTimeRule.WALL_TIME);

        // AnnualTimeZoneRule#getRule
        if (!a1.getRule().equals(a2.getRule())) {
            errln("FAIL: The same DateTimeRule must be returned from AnnualTimeZoneRule a1 and a2");
        }
    
        // AnnualTimeZoneRule#getStartYear
        int startYear = a1.getStartYear();
        if (startYear != 2000) {
            errln("FAIL: The start year of AnnualTimeZoneRule a1 must be 2000 - returned: " + startYear);
        }

        // AnnualTimeZoneRule#getEndYear
        int endYear = a1.getEndYear();
        if (endYear != AnnualTimeZoneRule.MAX_YEAR) {
            errln("FAIL: The start year of AnnualTimeZoneRule a1 must be MAX_YEAR - returned: " + endYear);
        }
        endYear = a3.getEndYear();
        if (endYear != 2010) {
            errln("FAIL: The start year of AnnualTimeZoneRule a3 must be 2010 - returned: " + endYear);
        }
        
        // AnnualTimeZone#getStartInYear
        Date d1 = a1.getStartInYear(2005, -3*HOUR, 0);
        Date d2 = a3.getStartInYear(2005, -3*HOUR, 0);
        if (d1 == null || d2 == null || !d1.equals(d2)) {
            errln("FAIL: AnnualTimeZoneRule#getStartInYear did not work as expected");
        }
        d2 = a3.getStartInYear(2015, -3*HOUR, 0);
        if (d2 != null) {
            errln("FAIL: AnnualTimeZoneRule#getSTartInYear returned non-null date for 2015 which is out of rule range");
        }

        // AnnualTimeZone#getFirstStart
        d1 = a1.getFirstStart(-3*HOUR, 0);
        d2 = a1.getFirstStart(-4*HOUR, 1*HOUR);
        if (d1 == null || d2 == null || !d1.equals(d2)) {
            errln("FAIL: The same start time should be returned by getFirstStart");
        }

        // AnnualTimeZone#getFinalStart
        d1 = a1.getFinalStart(-3*HOUR, 0);
        if (d1 != null) {
            errln("FAIL: Non-null Date is returned by getFinalStart for a1");
        }
        d1 = a1.getStartInYear(2010, -3*HOUR, 0);
        d2 = a3.getFinalStart(-3*HOUR, 0);
        if (d1 == null || d2 == null || !d1.equals(d2)) {
            errln("FAIL: Bad date is returned by getFinalStart");
        }

        // AnnualTimeZone#getNextStart / getPreviousStart
        d1 = a1.getNextStart(time1, -3*HOUR, 0, false);
        if (d1 == null) {
            errln("FAIL: Null Date is returned by getNextStart");
        } else {
            d2 = a1.getPreviousStart(d1.getTime(), -3*HOUR, 0, true);
            if (d2 == null || !d1.equals(d2)) {
                errln("FAIL: Bad Date is returned by getPreviousStart");
            }
        }
        d1 = a3.getNextStart(time2, -3*HOUR, 0, false);
        if (d1 != null) {
            errln("FAIL: getNextStart must return null when no start time is available after the base time");
        }
        d1 = a3.getFinalStart(-3*HOUR, 0);
        d2 = a3.getPreviousStart(time2, -3*HOUR, 0, false);
        if (d1 == null || d2 == null || !d1.equals(d2)) {
            errln("FAIL: getPreviousStart does not match with getFinalStart after the end year");
        }

        // AnnualTimeZone#isEquavalentTo
        if (!a1.isEquivalentTo(a2)) {
            errln("FAIL: AnnualTimeZoneRule a1 is equivalent to a2, but returned false");
        }
        if (a1.isEquivalentTo(a3)) {
            errln("FAIL: AnnualTimeZoneRule a1 is not equivalent to a3, but returned true");
        }
        if (!a1.isEquivalentTo(a1)) {
            errln("FAIL: AnnualTimeZoneRule a1 is equivalent to itself, but returned false");
        }
        if (a1.isEquivalentTo(t1)) {
            errln("FAIL: AnnualTimeZoneRule is not equivalent to TimeArrayTimeZoneRule, but returned true");
        }

        // AnnualTimeZone#isTransitionRule
        if (!a1.isTransitionRule()) {
            errln("FAIL: An AnnualTimeZoneRule is a transition rule, but returned false");
        }

        // AnnualTimeZone#toString
        String str = a1.toString();
        if (str == null || str.length() == 0) {
            errln("FAIL: AnnualTimeZoneRule#toString for a1 returns null or empty string");
        } else {
            logln("AnnualTimeZoneRule a1 : " + str);
        }
        str = a3.toString();
        if (str == null || str.length() == 0) {
            errln("FAIL: AnnualTimeZoneRule#toString for a3 returns null or empty string");
        } else {
            logln("AnnualTimeZoneRule a3 : " + str);
        }

        // InitialTimeZoneRule#isEquivalentRule
        if (!i1.isEquivalentTo(i2)) {
            errln("FAIL: InitialTimeZoneRule i1 is equivalent to i2, but returned false");
        }
        if (i1.isEquivalentTo(i3)) {
            errln("FAIL: InitialTimeZoneRule i1 is not equivalent to i3, but returned true");
        }
        if (i1.isEquivalentTo(a1)) {
            errln("FAIL: An InitialTimeZoneRule is not equivalent to an AnnualTimeZoneRule, but returned true");
        }

        // InitialTimeZoneRule#getFirstStart/getFinalStart/getNextStart/getPreviousStart
        d1 = i1.getFirstStart(0, 0);
        if (d1 != null) {
            errln("FAIL: Non-null Date is returned by InitialTimeZone#getFirstStart");
        }
        d1 = i1.getFinalStart(0, 0);
        if (d1 != null) {
            errln("FAIL: Non-null Date is returned by InitialTimeZone#getFinalStart");
        }
        d1 = i1.getNextStart(time1, 0, 0, false);
        if (d1 != null) {
            errln("FAIL: Non-null Date is returned by InitialTimeZone#getNextStart");
        }
        d1 = i1.getPreviousStart(time1, 0, 0, false);
        if (d1 != null) {
            errln("FAIL: Non-null Date is returned by InitialTimeZone#getPreviousStart");
        }

        // InitialTimeZoneRule#isTransitionRule
        if (i1.isTransitionRule()) {
            errln("FAIL: An InitialTimeZoneRule is not a transition rule, but returned true");
        }

        // InitialTimeZoneRule#toString
        str = i1.toString();
        if (str == null || str.length() == 0) {
            errln("FAIL: InitialTimeZoneRule#toString returns null or empty string");
        } else {
            logln("InitialTimeZoneRule i1 : " + str);
        }
        
        
        // TimeArrayTimeZoneRule#getStartTimes
        long[] times = t1.getStartTimes();
        if (times == null || times.length == 0 || times[0] != 0) {
            errln("FAIL: Bad start times are returned by TimeArrayTimeZoneRule#getStartTimes");
        }

        // TimeArrayTimeZoneRule#getTimeType
        if (t1.getTimeType() != DateTimeRule.UTC_TIME) {
            errln("FAIL: TimeArrayTimeZoneRule t1 uses UTC_TIME, but different type is returned");
        }
        if (t4.getTimeType() != DateTimeRule.STANDARD_TIME) {
            errln("FAIL: TimeArrayTimeZoneRule t4 uses STANDARD_TIME, but different type is returned");
        }
        if (t5.getTimeType() != DateTimeRule.WALL_TIME) {
            errln("FAIL: TimeArrayTimeZoneRule t5 uses WALL_TIME, but different type is returned");
        }

        // TimeArrayTimeZoneRule#getFirstStart/getFinalStart
        d1 = t1.getFirstStart(0, 0);
        if (d1 == null || d1.getTime() != trtimes1[0]) {
            errln("FAIL: Bad first start time returned from TimeArrayTimeZoneRule t1");
        }
        d1 = t1.getFinalStart(0, 0);
        if (d1 == null || d1.getTime() != trtimes1[0]) {
            errln("FAIL: Bad final start time returned from TimeArrayTimeZoneRule t1");
        }
        d1 = t4.getFirstStart(-4*HOUR, 1*HOUR);
        if (d1 == null || (d1.getTime() != trtimes1[0] + 4*HOUR)) {
            errln("FAIL: Bad first start time returned from TimeArrayTimeZoneRule t4");
        }
        d1 = t5.getFirstStart(-4*HOUR, 1*HOUR);
        if (d1 == null || (d1.getTime() != trtimes1[0] + 3*HOUR)) {
            errln("FAIL: Bad first start time returned from TimeArrayTimeZoneRule t5");
        }

        // TimeArrayTimeZoneRule#getNextStart/getPreviousStart
        d1 = t3.getNextStart(time1, -3*HOUR, 1*HOUR, false);
        if (d1 != null) {
            errln("FAIL: Non-null Date is returned by getNextStart after the final transition for t3");
        }
        d1 = t3.getPreviousStart(time1, -3*HOUR, 1*HOUR, false);
        if (d1 == null || d1.getTime() != trtimes2[1]) {
            errln("FAIL: Bad start time returned by getPreviousStart for t3");
        } else {
            d2 = t3.getPreviousStart(d1.getTime(), -3*HOUR, 1*HOUR, false);
            if (d2 == null || d2.getTime() != trtimes2[0]) {
                errln("FAIL: Bad start time returned by getPreviousStart for t3");
            }
        }
        d1 = t3.getPreviousStart(time3, -3*HOUR, 1*HOUR, false); //time3 - year 1950, no result expected
        if (d1 != null) {
            errln("FAIL: Non-null Date is returned by getPrevoousStart for t3");
        }

        // TimeArrayTimeZoneRule#isEquivalentTo
        if (!t1.isEquivalentTo(t2)) {
            errln("FAIL: TimeArrayTimeZoneRule t1 is equivalent to t2, but returned false");
        }
        if (t1.isEquivalentTo(t3)) {
            errln("FAIL: TimeArrayTimeZoneRule t1 is not equivalent to t3, but returned true");
        }
        if (t1.isEquivalentTo(t4)) {
            errln("FAIL: TimeArrayTimeZoneRule t1 is not equivalent to t4, but returned true");
        }
        if (t1.isEquivalentTo(a1)) {
            errln("FAIL: TimeArrayTimeZoneRule is not equivalent to AnnualTimeZoneRule, but returned true");
        }

        // TimeArrayTimeZoneRule#isTransitionRule
        if (!t1.isTransitionRule()) {
            errln("FAIL: A TimeArrayTimeZoneRule is a transition rule, but returned false");
        }

        // TimeArrayTimeZoneRule#toString
        str = t3.toString();
        if (str == null || str.length() == 0) {
            errln("FAIL: TimeArrayTimeZoneRule#toString returns null or empty string");
        } else {
            logln("TimeArrayTimeZoneRule t3 : " + str);
        }

        // DateTimeRule#toString
        str = dtr1.toString();
        if (str == null || str.length() == 0) {
            errln("FAIL: DateTimeRule#toString for dtr1 returns null or empty string");
        } else {
            logln("DateTimeRule dtr1 : " + str);
        }
        str = dtr2.toString();
        if (str == null || str.length() == 0) {
            errln("FAIL: DateTimeRule#toString for dtr2 returns null or empty string");
        } else {
            logln("DateTimeRule dtr1 : " + str);
        }
        str = dtr3.toString();
        if (str == null || str.length() == 0) {
            errln("FAIL: DateTimeRule#toString for dtr3 returns null or empty string");
        } else {
            logln("DateTimeRule dtr1 : " + str);
        }
        str = dtr4.toString();
        if (str == null || str.length() == 0) {
            errln("FAIL: DateTimeRule#toString for dtr4 returns null or empty string");
        } else {
            logln("DateTimeRule dtr1 : " + str);
        }
    }

    /*
     * API coverage test for BasicTimeZone APIs in SimpleTimeZone
     */
    @Test
    public void TestSimpleTimeZoneCoverage() {

        long time1 = getUTCMillis(1990, Calendar.JUNE, 1);
        long time2 = getUTCMillis(2000, Calendar.JUNE, 1);

        TimeZoneTransition tzt1, tzt2;

        // BasicTimeZone API implementation in SimpleTimeZone
        SimpleTimeZone stz1 = new SimpleTimeZone(-5*HOUR, "GMT-5");

        tzt1 = stz1.getNextTransition(time1, false);
        if (tzt1 != null) {
            errln("FAIL: No transition must be returned by getNextTranstion for SimpleTimeZone with no DST rule");
        }
        tzt1 = stz1.getPreviousTransition(time1, false);
        if (tzt1 != null) {
            errln("FAIL: No transition must be returned by getPreviousTransition  for SimpleTimeZone with no DST rule");
        }
        TimeZoneRule[] tzrules = stz1.getTimeZoneRules();
        if (tzrules.length != 1 || !(tzrules[0] instanceof InitialTimeZoneRule)) {
            errln("FAIL: Invalid results returned by SimpleTimeZone#getTimeZoneRules");
        }

        // Set DST rule
        stz1.setStartRule(Calendar.MARCH, 11, 2*HOUR); // March 11
        stz1.setEndRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 2*HOUR); // First Sunday in November
        tzt1 = stz1.getNextTransition(time1, false);
        if (tzt1 == null) {
            errln("FAIL: Non-null transition must be returned by getNextTranstion for SimpleTimeZone with a DST rule");
        } else {
            String str = tzt1.toString();
            if (str == null || str.length() == 0) {
                errln("FAIL: TimeZoneTransition#toString returns null or empty string");
            } else {
                logln(str);
            }
        }
        tzt1 = stz1.getPreviousTransition(time1, false);
        if (tzt1 == null) {
            errln("FAIL: Non-null transition must be returned by getPreviousTransition  for SimpleTimeZone with a DST rule");
        }
        tzrules = stz1.getTimeZoneRules();
        if (tzrules.length != 3 || !(tzrules[0] instanceof InitialTimeZoneRule)
                || !(tzrules[1] instanceof AnnualTimeZoneRule)
                || !(tzrules[2] instanceof AnnualTimeZoneRule)) {
            errln("FAIL: Invalid results returned by SimpleTimeZone#getTimeZoneRules for a SimpleTimeZone with DST");
        }
        // Set DST start year
        stz1.setStartYear(2007);
        tzt1 = stz1.getPreviousTransition(time1, false);
        if (tzt1 != null) {
            errln("FAIL: No transition must be returned before 1990");
        }
        tzt1 = stz1.getNextTransition(time1, false); // transition after 1990-06-01
        tzt2 = stz1.getNextTransition(time2, false); // transition after 2000-06-01
        if (tzt1 == null || tzt2 == null || !tzt1.equals(tzt2)) {
            errln("FAIL: Bad transition returned by SimpleTimeZone#getNextTransition");
        }
    }
    
    /*
     * API coverage test for VTimeZone
     */
    @Test
    public void TestVTimeZoneCoverage() {
        final String TZID = "Europe/Moscow";
        BasicTimeZone otz = (BasicTimeZone)TimeZone.getTimeZone(TZID, TimeZone.TIMEZONE_ICU);
        VTimeZone vtz = VTimeZone.create(TZID);

        // getOffset(era, year, month, day, dayOfWeek, milliseconds)
        int offset1 = otz.getOffset(GregorianCalendar.AD, 2007, Calendar.JULY, 1, Calendar.SUNDAY, 0);
        int offset2 = vtz.getOffset(GregorianCalendar.AD, 2007, Calendar.JULY, 1, Calendar.SUNDAY, 0);
        if (offset1 != offset2) {
            errln("FAIL: getOffset(int,int,int,int,int,int) returned different results in VTimeZone and OlsonTimeZone");
        }

        // getOffset(date, local, offsets)
        int[] offsets1 = new int[2];
        int[] offsets2 = new int[2];
        long t = System.currentTimeMillis();
        otz.getOffset(t, false, offsets1);
        vtz.getOffset(t, false, offsets2);
        if (offsets1[0] != offsets2[0] || offsets1[1] != offsets2[1]) {
            errln("FAIL: getOffset(long,boolean,int[]) returned different results in VTimeZone and OlsonTimeZone");
        }

        // getRawOffset
        if (otz.getRawOffset() != vtz.getRawOffset()) {
            errln("FAIL: getRawOffset returned different results in VTimeZone and OlsonTimeZone");
        }

        // inDaylightTime
        Date d = new Date();
        if (otz.inDaylightTime(d) != vtz.inDaylightTime(d)) {
            errln("FAIL: inDaylightTime returned different results in VTimeZone and OlsonTimeZone");
        }

        // useDaylightTime
        if (otz.useDaylightTime() != vtz.useDaylightTime()) {
            errln("FAIL: useDaylightTime returned different results in VTimeZone and OlsonTimeZone");
        }

        // setRawOffset
        final int RAW = -10*HOUR;
        VTimeZone tmpvtz = (VTimeZone)vtz.clone();
        tmpvtz.setRawOffset(RAW);
        if (tmpvtz.getRawOffset() != RAW) {
            logln("setRawOffset is implemented");
        }

        // hasSameRules
        boolean bSame = otz.hasSameRules(vtz);
        logln("OlsonTimeZone#hasSameRules(VTimeZone) should return false always for now - actual: " + bSame);

        // getTZURL/setTZURL
        final String TZURL = "http://icu-project.org/timezone";
        String tzurl = vtz.getTZURL();
        if (tzurl != null) {
            errln("FAIL: getTZURL returned non-null value");
        }
        vtz.setTZURL(TZURL);
        tzurl = vtz.getTZURL();
        if (!TZURL.equals(tzurl)) {
            errln("FAIL: URL returned by getTZURL does not match the one set by setTZURL");
        }

        // getLastModified/setLastModified
        Date lastmod = vtz.getLastModified();
        if (lastmod != null) {
            errln("FAIL: getLastModified returned non-null value");
        }
        Date newdate = new Date();
        vtz.setLastModified(newdate);
        lastmod = vtz.getLastModified();
        if (!newdate.equals(lastmod)) {
            errln("FAIL: Date returned by getLastModified does not match the one set by setLastModified");
        }

        // getNextTransition/getPreviousTransition
        long base = getUTCMillis(2007, Calendar.JULY, 1);
        TimeZoneTransition tzt1 = otz.getNextTransition(base, true);
        TimeZoneTransition tzt2 = vtz.getNextTransition(base, true);
        if (tzt1.equals(tzt2)) {
            errln("FAIL: getNextTransition returned different results in VTimeZone and OlsonTimeZone");
        }
        tzt1 = otz.getPreviousTransition(base, false);
        tzt2 = vtz.getPreviousTransition(base, false);
        if (tzt1.equals(tzt2)) {
            errln("FAIL: getPreviousTransition returned different results in VTimeZone and OlsonTimeZone");
        }

        // hasEquivalentTransitions
        long time1 = getUTCMillis(1950, Calendar.JANUARY, 1);
        long time2 = getUTCMillis(2020, Calendar.JANUARY, 1);
        if (!vtz.hasEquivalentTransitions(otz, time1, time2)) {
            errln("FAIL: hasEquivalentTransitons returned false for the same time zone");
        }

        // getTimeZoneRules
        TimeZoneRule[] rulesetAll = vtz.getTimeZoneRules();
        TimeZoneRule[] ruleset1 = vtz.getTimeZoneRules(time1);
        TimeZoneRule[] ruleset2 = vtz.getTimeZoneRules(time2);
        if (rulesetAll.length < ruleset1.length || ruleset1.length < ruleset2.length) {
            errln("FAIL: Number of rules returned by getRules is invalid");
        }
        
        int[] offsets_vtzc = new int[2];
        VTimeZone vtzc = VTimeZone.create("PST");
        vtzc.getOffsetFromLocal(Calendar.getInstance(vtzc).getTimeInMillis(), VTimeZone.LOCAL_STD, VTimeZone.LOCAL_STD, offsets_vtzc);
        if (offsets_vtzc[0] > offsets_vtzc[1]) {
            errln("Error getOffsetFromLocal()");
        }
    }

    @Test
    public void TestVTimeZoneParse() {
        // Trying to create VTimeZone from empty data
        StringReader r = new StringReader("");
        VTimeZone empty = VTimeZone.create(r);
        if (empty != null) {
            errln("FAIL: Non-null VTimeZone is returned for empty VTIMEZONE data");
        }

        // Create VTimeZone for Asia/Tokyo
        String asiaTokyo =
                "BEGIN:VTIMEZONE\r\n" +
                "TZID:Asia\r\n" +
                "\t/Tokyo\r\n" +
                "BEGIN:STANDARD\r\n" +
                "TZOFFSETFROM:+0900\r\n" +
                "TZOFFSETTO:+0900\r\n" +
                "TZNAME:JST\r\n" +
                "DTSTART:19700101\r\n" +
                " T000000\r\n" +
                "END:STANDARD\r\n" +
                "END:VTIMEZONE";
        r = new StringReader(asiaTokyo);
        VTimeZone tokyo = VTimeZone.create(r);
        if (tokyo == null) {
            errln("FAIL: Failed to create a VTimeZone tokyo");
        } else {
            // Make sure offsets are correct
            int[] offsets = new int[2];
            tokyo.getOffset(System.currentTimeMillis(), false, offsets);
            if (offsets[0] != 9*HOUR || offsets[1] != 0) {
                errln("FAIL: Bad offsets returned by a VTimeZone created for Tokyo");
            }
        }        

        // Create VTimeZone from VTIMEZONE data
        String fooData = 
            "BEGIN:VCALENDAR\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:FOO\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:-0700\r\n" +
            "TZOFFSETTO:-0800\r\n" +
            "TZNAME:FST\r\n" +
            "DTSTART:20071010T010000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=WE;BYMONTHDAY=10,11,12,13,14,15,16;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:-0800\r\n" +
            "TZOFFSETTO:-0700\r\n" +
            "TZNAME:FDT\r\n" +
            "DTSTART:20070415T010000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTHDAY=15;BYMONTH=4\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "END:VCALENDAR";

        r = new StringReader(fooData);
        VTimeZone foo = VTimeZone.create(r);
        if (foo == null) {
            errln("FAIL: Failed to create a VTimeZone foo");
        } else {
            // Write VTIMEZONE data
            StringWriter w = new StringWriter();
            try {
                foo.write(w, getUTCMillis(2005, Calendar.JANUARY, 1));
            } catch (IOException ioe) {
                errln("FAIL: IOException is thrown while writing VTIMEZONE data for foo");
            }
            logln(w.toString());
        }
    }

    @Test
    public void TestT6216() {
        // Test case in #6216
        String tokyoTZ =
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//PYVOBJECT//NONSGML Version 1//EN\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Asia/Tokyo\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:20000101T000000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=1\r\n" +
            "TZNAME:Asia/Tokyo\r\n" +
            "TZOFFSETFROM:+0900\r\n" +
            "TZOFFSETTO:+0900\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "END:VCALENDAR";

        // Single final rule, overlapping with another
        String finalOverlap =
            "BEGIN:VCALENDAR\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:FinalOverlap\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:-0200\r\n" +
            "TZOFFSETTO:-0300\r\n" +
            "TZNAME:STD\r\n" +
            "DTSTART:20001029T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:-0300\r\n" +
            "TZOFFSETTO:-0200\r\n" +
            "TZNAME:DST\r\n" +
            "DTSTART:19990404T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=4;UNTIL=20050403T040000Z\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "END:VCALENDAR";

        // Single final rule, no overlapping with another
        String finalNonOverlap = 
            "BEGIN:VCALENDAR\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:FinalNonOverlap\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:-0200\r\n" +
            "TZOFFSETTO:-0300\r\n" +
            "TZNAME:STD\r\n" +
            "DTSTART:20001029T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10;UNTIL=20041031T040000Z\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:-0300\r\n" +
            "TZOFFSETTO:-0200\r\n" +
            "TZNAME:DST\r\n" +
            "DTSTART:19990404T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=4;UNTIL=20050403T040000Z\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:-0200\r\n" +
            "TZOFFSETTO:-0300\r\n" +
            "TZNAME:STDFINAL\r\n" +
            "DTSTART:20071028T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "END:VCALENDAR";

        int[][] TestDates = {
                {1995, Calendar.JANUARY, 1},
                {1995, Calendar.JULY, 1},
                {2000, Calendar.JANUARY, 1},
                {2000, Calendar.JULY, 1},
                {2005, Calendar.JANUARY, 1},
                {2005, Calendar.JULY, 1},
                {2010, Calendar.JANUARY, 1},
                {2010, Calendar.JULY, 1},
        };

        String[] TestZones = {
            tokyoTZ,
            finalOverlap,
            finalNonOverlap,
        };

        int[][] Expected = {
          //  JAN90      JUL90      JAN00      JUL00      JAN05      JUL05      JAN10      JUL10
            { 32400000,  32400000,  32400000,  32400000,  32400000,  32400000,  32400000,  32400000},
            {-10800000, -10800000,  -7200000,  -7200000, -10800000,  -7200000, -10800000, -10800000},
            {-10800000, -10800000,  -7200000,  -7200000, -10800000,  -7200000, -10800000, -10800000},
        };

        // Get test times
        long[] times = new long[TestDates.length];
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("Etc/GMT"));
        for (int i = 0; i < TestDates.length; i++) {
            cal.clear();
            cal.set(TestDates[i][0], TestDates[i][1], TestDates[i][2]);
            times[i] = cal.getTimeInMillis();
        }

        for (int i = 0; i < TestZones.length; i++) {
            try {
                VTimeZone vtz = VTimeZone.create(new StringReader(TestZones[i]));
                for (int j = 0; j < times.length; j++) {
                    int offset = vtz.getOffset(times[j]);
                    if (offset != Expected[i][j]) {
                        errln("FAIL: Invalid offset at time(" + times[j] + "):" + offset + " Expected:" + Expected[i][j]);
                    }
                }
            } catch (Exception e) {
                errln("FAIL: Failed to calculate the offset for VTIMEZONE data " + i);
            }
        }
    }

    @Test
    public void TestT6669() {
        // getNext/PreviousTransition implementation in SimpleTimeZone
        // used to use a bad condition for detecting if DST is enabled or not.

        SimpleTimeZone stz = new SimpleTimeZone(0, "CustomID",
                Calendar.JANUARY, 1, Calendar.SUNDAY, 0,
                Calendar.JULY, 1, Calendar.SUNDAY, 0);

        long t = 1230681600000L; //2008-12-31T00:00:00
        long expectedNext = 1231027200000L; //2009-01-04T00:00:00
        long expectedPrev = 1215298800000L; //2008-07-06T00:00:00

        TimeZoneTransition tzt = stz.getNextTransition(t, false);
        if (tzt == null) {
            errln("FAIL: No transition returned by getNextTransition.");
        } else if (tzt.getTime() != expectedNext){
            errln("FAIL: Wrong transition time returned by getNextTransition - "
                    + tzt.getTime() + " Expected: " + expectedNext);
        }

        tzt = stz.getPreviousTransition(t, true);
        if (tzt == null) {
            errln("FAIL: No transition returned by getPreviousTransition.");
        } else if (tzt.getTime() != expectedPrev){
            errln("FAIL: Wrong transition time returned by getPreviousTransition - "
                    + tzt.getTime() + " Expected: " + expectedPrev);
        }
    }
    
    @Test
    public void TestBasicTimeZoneCoverage() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        if (tz instanceof BasicTimeZone) {
            BasicTimeZone btz = (BasicTimeZone)tz;
            int []offsets = new int[2];

            btz.getOffsetFromLocal(Calendar.getInstance().getTimeInMillis(), BasicTimeZone.LOCAL_STD, BasicTimeZone.LOCAL_STD, offsets);
            if (offsets[0] > offsets[1]) {
                errln("Error calling getOffsetFromLocal().");
            }
        } else {
            logln("Skipping TestBasicTimeZoneCoverage: ICU4J is configured to use JDK TimeZone");
        }
    }

    // Internal utility methods -----------------------------------------

    /*
     * Check if a time shift really happens on each transition returned by getNextTransition or
     * getPreviousTransition in the specified time range
     */
    private void verifyTransitions(TimeZone tz, long start, long end) {
        BasicTimeZone icutz = (BasicTimeZone)tz;
        long time;
        int[] before = new int[2];
        int[] after = new int[2];
        TimeZoneTransition tzt0;

        // Ascending
        tzt0 = null;
        time = start;
        while(true) {
            TimeZoneTransition tzt = icutz.getNextTransition(time, false);

            if (tzt == null) {
                break;
            }
            time = tzt.getTime();
            if (time >= end) {
                break;
            }
            icutz.getOffset(time, false, after);
            icutz.getOffset(time - 1, false, before);

            if (after[0] == before[0] && after[1] == before[1]) {
                errln("FAIL: False transition returned by getNextTransition for " + icutz.getID() + " at " + time);
            }
            if (tzt0 != null &&
                    (tzt0.getTo().getRawOffset() != tzt.getFrom().getRawOffset()
                    || tzt0.getTo().getDSTSavings() != tzt.getFrom().getDSTSavings())) {
                errln("FAIL: TO rule of the previous transition does not match FROM rule of this transtion at "
                        + time + " for " + icutz.getID());                
            }
            tzt0 = tzt;
        }

        // Descending
        tzt0 = null;
        time = end;
        while(true) {
            TimeZoneTransition tzt = icutz.getPreviousTransition(time, false);
            if (tzt == null) {
                break;
            }
            time = tzt.getTime();
            if (time <= start) {
                break;
            }
            icutz.getOffset(time, false, after);
            icutz.getOffset(time - 1, false, before);

            if (after[0] == before[0] && after[1] == before[1]) {
                errln("FAIL: False transition returned by getPreviousTransition for " + icutz.getID() + " at " + time);
            }

            if (tzt0 != null &&
                    (tzt0.getFrom().getRawOffset() != tzt.getTo().getRawOffset()
                    || tzt0.getFrom().getDSTSavings() != tzt.getTo().getDSTSavings())) {
                errln("FAIL: TO rule of the next transition does not match FROM rule in this transtion at "
                        + time + " for " + icutz.getID());                
            }
            tzt0 = tzt;
        }
    }

    /*
     * Compare all time transitions in 2 time zones in the specified time range in ascending order
     */
    private void compareTransitionsAscending(TimeZone tz1, TimeZone tz2, long start, long end, boolean inclusive) {
        BasicTimeZone z1 = (BasicTimeZone)tz1;
        BasicTimeZone z2 = (BasicTimeZone)tz2;
        String zid1 = tz1.getID();
        String zid2 = tz2.getID();

        long time = start;
        while(true) {
            TimeZoneTransition tzt1 = z1.getNextTransition(time, inclusive);
            TimeZoneTransition tzt2 = z2.getNextTransition(time, inclusive);
            boolean inRange1 = false;
            boolean inRange2 = false;
            if (tzt1 != null) {
                if (tzt1.getTime() < end || (inclusive && tzt1.getTime() == end)) {
                    inRange1 = true;
                }
            }
            if (tzt2 != null) {
                if (tzt2.getTime() < end || (inclusive && tzt2.getTime() == end)) {
                    inRange2 = true;
                }
            }
            if (!inRange1 && !inRange2) {
                // No more transition in the range
                break;
            }
            if (!inRange1) {
                errln("FAIL: " + zid1 + " does not have any transitions after " + time + " before " + end);
                break;
            }
            if (!inRange2) {
                errln("FAIL: " + zid2 + " does not have any transitions after " + time + " before " + end);
                break;
            }
            if (tzt1.getTime() != tzt2.getTime()) {
                errln("FAIL: First transition after " + time + " "
                        + zid1 + "[" + tzt1.getTime() + "] "
                        + zid2 + "[" + tzt2.getTime() + "]");
                break;
            }
            time = tzt1.getTime();
            if (inclusive) {
                time++;
            }
        }
    }

    /*
     * Compare all time transitions in 2 time zones in the specified time range in descending order
     */
    private void compareTransitionsDescending(TimeZone tz1, TimeZone tz2, long start, long end, boolean inclusive) {
        BasicTimeZone z1 = (BasicTimeZone)tz1;
        BasicTimeZone z2 = (BasicTimeZone)tz2;
        String zid1 = tz1.getID();
        String zid2 = tz2.getID();
        long time = end;
        while(true) {
            TimeZoneTransition tzt1 = z1.getPreviousTransition(time, inclusive);
            TimeZoneTransition tzt2 = z2.getPreviousTransition(time, inclusive);
            boolean inRange1 = false;
            boolean inRange2 = false;
            if (tzt1 != null) {
                if (tzt1.getTime() > start || (inclusive && tzt1.getTime() == start)) {
                    inRange1 = true;
                }
            }
            if (tzt2 != null) {
                if (tzt2.getTime() > start || (inclusive && tzt2.getTime() == start)) {
                    inRange2 = true;
                }
            }
            if (!inRange1 && !inRange2) {
                // No more transition in the range
                break;
            }
            if (!inRange1) {
                errln("FAIL: " + zid1 + " does not have any transitions before " + time + " after " + start);
                break;
            }
            if (!inRange2) {
                errln("FAIL: " + zid2 + " does not have any transitions before " + time + " after " + start);
                break;
            }
            if (tzt1.getTime() != tzt2.getTime()) {
                errln("FAIL: Last transition before " + time + " "
                        + zid1 + "[" + tzt1.getTime() + "] "
                        + zid2 + "[" + tzt2.getTime() + "]");
                break;
            }
            time = tzt1.getTime();
            if (inclusive) {
                time--;
            }
        }
    }

    private static final String[] TESTZIDS = {
        "AGT",
        "America/New_York",
        "America/Los_Angeles",
        "America/Indiana/Indianapolis",
        "America/Havana",
        "Europe/Lisbon",
        "Europe/Paris",
        "Asia/Tokyo",
        "Asia/Sakhalin",
        "Africa/Cairo",
        "Africa/Windhoek",
        "Australia/Sydney",
        "Etc/GMT+8",
        "Asia/Amman",
    };

    private String[] getTestZIDs() {
        if (TestFmwk.getExhaustiveness() > 5) {
            return TimeZone.getAvailableIDs();
        }
        return TESTZIDS;
    }

    private static final int[][] TESTYEARS = {
        {1895, 1905}, // including int32 minimum second
        {1965, 1975}, // including the epoch
        {1995, 2015}  // practical year range
    };

    private long[] getTestTimeRange(int idx) {
        int loyear, hiyear;
        if (idx < TESTYEARS.length) {
            loyear = TESTYEARS[idx][0];
            hiyear = TESTYEARS[idx][1];
        } else if (idx == TESTYEARS.length && TestFmwk.getExhaustiveness() > 5) {
            loyear = 1850;
            hiyear = 2050;
        } else {
            return null;
        }

        long[] times = new long[2];
        times[0] = getUTCMillis(loyear, Calendar.JANUARY, 1);
        times[1] = getUTCMillis(hiyear + 1, Calendar.JANUARY, 1);

        return times;
    }

    private GregorianCalendar utcCal;

    private long getUTCMillis(int year, int month, int dayOfMonth) {
        if (utcCal == null) {
            utcCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), ULocale.ROOT);
        }
        utcCal.clear();
        utcCal.set(year, month, dayOfMonth);
        return utcCal.getTimeInMillis();
    }

    /*
     * Slightly modified version of BasicTimeZone#hasEquivalentTransitions.
     * This version returns true if transition time delta is within the given
     * delta range.
     */
    private static boolean hasEquivalentTransitions(BasicTimeZone tz1, BasicTimeZone tz2,
                                            long start, long end, 
                                            boolean ignoreDstAmount, int maxTransitionTimeDelta) {
        if (tz1.hasSameRules(tz2)) {
            return true;
        }

        // Check the offsets at the start time
        int[] offsets1 = new int[2];
        int[] offsets2 = new int[2];

        tz1.getOffset(start, false, offsets1);
        tz2.getOffset(start, false, offsets2);

        if (ignoreDstAmount) {
            if ((offsets1[0] + offsets1[1] != offsets2[0] + offsets2[1])
                || (offsets1[1] != 0 && offsets2[1] == 0)
                || (offsets1[1] == 0 && offsets2[1] != 0)) {
                return false;
            }
        } else {
            if (offsets1[0] != offsets2[0] || offsets1[1] != offsets2[1]) {
                return false;
            }
        }

        // Check transitions in the range
        long time = start;
        while (true) {
            TimeZoneTransition tr1 = tz1.getNextTransition(time, false);
            TimeZoneTransition tr2 = tz2.getNextTransition(time, false);

            if (ignoreDstAmount) {
                // Skip a transition which only differ the amount of DST savings
                while (true) {
                    if (tr1 != null
                            && tr1.getTime() <= end
                            && (tr1.getFrom().getRawOffset() + tr1.getFrom().getDSTSavings()
                                    == tr1.getTo().getRawOffset() + tr1.getTo().getDSTSavings())
                            && (tr1.getFrom().getDSTSavings() != 0 && tr1.getTo().getDSTSavings() != 0)) {
                        tr1 = tz1.getNextTransition(tr1.getTime(), false);
                    } else {
                        break;
                    }
                }
                while (true) {
                    if (tr2 != null
                            && tr2.getTime() <= end
                            && (tr2.getFrom().getRawOffset() + tr2.getFrom().getDSTSavings()
                                    == tr2.getTo().getRawOffset() + tr2.getTo().getDSTSavings())
                            && (tr2.getFrom().getDSTSavings() != 0 && tr2.getTo().getDSTSavings() != 0)) {
                        tr2 = tz2.getNextTransition(tr2.getTime(), false);
                    } else {
                        break;
                    }
                }            }

            boolean inRange1 = false;
            boolean inRange2 = false;
            if (tr1 != null) {
                if (tr1.getTime() <= end) {
                    inRange1 = true;
                }
            }
            if (tr2 != null) {
                if (tr2.getTime() <= end) {
                    inRange2 = true;
                }
            }
            if (!inRange1 && !inRange2) {
                // No more transition in the range
                break;
            }
            if (!inRange1 || !inRange2) {
                return false;
            }
            if (Math.abs(tr1.getTime() - tr2.getTime()) > maxTransitionTimeDelta) {
                return false;
            }
            if (ignoreDstAmount) {
                if (tr1.getTo().getRawOffset() + tr1.getTo().getDSTSavings()
                            != tr2.getTo().getRawOffset() + tr2.getTo().getDSTSavings()
                        || tr1.getTo().getDSTSavings() != 0 &&  tr2.getTo().getDSTSavings() == 0
                        || tr1.getTo().getDSTSavings() == 0 &&  tr2.getTo().getDSTSavings() != 0) {
                    return false;
                }
            } else {
                if (tr1.getTo().getRawOffset() != tr2.getTo().getRawOffset() ||
                    tr1.getTo().getDSTSavings() != tr2.getTo().getDSTSavings()) {
                    return false;
                }
            }
            time = tr1.getTime() > tr2.getTime() ? tr1.getTime() : tr2.getTime();
        }
        return true;
    }

    // Test case for ticket#8943
    // RuleBasedTimeZone#getOffsets throws NPE
    @Test
    public void TestT8943() {
        String id = "Ekaterinburg Time";
        String stdName = "Ekaterinburg Standard Time";
        String dstName = "Ekaterinburg Daylight Time";

        InitialTimeZoneRule initialRule = new InitialTimeZoneRule(stdName, 18000000, 0);
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone(id, initialRule);

        DateTimeRule dtRule = new DateTimeRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 10800000, DateTimeRule.WALL_TIME);
        AnnualTimeZoneRule atzRule = new AnnualTimeZoneRule(stdName, 18000000, 0, dtRule, 2000, 2010);
        rbtz.addTransitionRule(atzRule);

        dtRule = new DateTimeRule(Calendar.MARCH, -1, Calendar.SUNDAY, 7200000, DateTimeRule.WALL_TIME);
        atzRule = new AnnualTimeZoneRule(dstName, 18000000, 3600000, dtRule, 2000, 2010);
        rbtz.addTransitionRule(atzRule);

        dtRule = new DateTimeRule(Calendar.JANUARY, 1, 0, DateTimeRule.WALL_TIME);
        atzRule = new AnnualTimeZoneRule(stdName, 21600000, 0, dtRule, 2011, AnnualTimeZoneRule.MAX_YEAR);
        rbtz.addTransitionRule(atzRule);

        dtRule = new DateTimeRule(Calendar.JANUARY, 1, 1, DateTimeRule.WALL_TIME);
        atzRule = new AnnualTimeZoneRule(dstName, 21600000, 0, dtRule, 2011, AnnualTimeZoneRule.MAX_YEAR);
        rbtz.addTransitionRule(atzRule);

        int[] expected = {21600000, 0};
        int[] offsets = new int[2];
        try {
            rbtz.getOffset(1293822000000L /* 2010-12-31 19:00:00 UTC */, false, offsets);
            if (offsets[0] != expected[0] || offsets[1] != expected[1]) {
                errln("Fail: Wrong offsets: " + offsets[0] + "/" + offsets[1] + " Expected: " + expected[0] + "/" + expected[1]);
            }
        } catch (Exception e) {
            errln("Fail: Exception thrown - " + e.getMessage());
        }
    }
}