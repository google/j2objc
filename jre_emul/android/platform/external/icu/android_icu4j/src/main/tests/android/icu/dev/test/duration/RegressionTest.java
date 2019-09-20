/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.duration;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.duration.BasicPeriodFormatterService;
import android.icu.impl.duration.DurationFormatter;
import android.icu.impl.duration.PeriodBuilder;
import android.icu.impl.duration.PeriodFormatterService;
import android.icu.text.DurationFormat;
import android.icu.util.ULocale;

public class RegressionTest extends TestFmwk {
    // bug6397
    @Test
    public void TestDisallowedMillis() {
        // original test case
        // if we don't support milliseconds, format times less than 1 second as 
        // 'less than 1 second'
        {
            ULocale ul = new ULocale("th");
            DurationFormat df = DurationFormat.getInstance(ul);
            String result = df.formatDurationFromNow(500);
            assertEquals(
                "original test case", 
                "\u0E44\u0E21\u0E48\u0E16\u0E36\u0E07\u0E2D\u0E35\u0E01 1 \u0E27\u0E34\u0E19\u0E32\u0E17\u0E35", 
                result);
        }
        
        // same issue, but using English and the internal APIs
        {
            PeriodFormatterService pfs = BasicPeriodFormatterService.getInstance();
            PeriodBuilder pb = pfs.newPeriodBuilderFactory()
                .setAllowMilliseconds(false)
                .getSingleUnitBuilder();
            DurationFormatter df = pfs.newDurationFormatterFactory()
                .setPeriodBuilder(pb)
                .getFormatter();
            String result = df.formatDurationFromNow(500);
            assertEquals(
                "english test case",
                "less than 1 second from now",
                result);
           
        }
        
        // if the limit is set on milliseconds, and they are not supported, 
        // use an effective limit based on seconds
        {
            PeriodFormatterService pfs = BasicPeriodFormatterService.getInstance();
            PeriodBuilder pb = pfs.newPeriodBuilderFactory()
                .setMinLimit(2500)
                .setAllowMilliseconds(false)
                .getSingleUnitBuilder();
            DurationFormatter df = pfs.newDurationFormatterFactory()
                .setPeriodBuilder(pb)
                .getFormatter();
            String result = df.formatDurationFromNow(500);
            assertEquals(
                "limit test case",
                "less than 2 seconds from now",
                result);
        }
    }
}
