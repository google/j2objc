/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007-2011, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

// Copyright 2007 Google Inc.  All Rights Reserved.

package android.icu.dev.test.duration;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.duration.BasicPeriodFormatterService;
import android.icu.impl.duration.Period;
import android.icu.impl.duration.PeriodBuilder;
import android.icu.impl.duration.PeriodBuilderFactory;
import android.icu.impl.duration.TimeUnit;
import android.icu.impl.duration.TimeUnitConstants;

public class PeriodBuilderFactoryTest extends TestFmwk implements TimeUnitConstants {
    private PeriodBuilderFactory pbf;

    private static final long[] approxDurations = {
      36525L*24*60*60*10, 3045*24*60*60*10L, 7*24*60*60*1000L, 24*60*60*1000L, 
      60*60*1000L, 60*1000L, 1000L, 1L
    };
    
    @Test
    public void testSetAvailableUnitRange() {
        // sanity check, make sure by default all units are set
        pbf = BasicPeriodFormatterService.getInstance().newPeriodBuilderFactory();
        pbf.setLocale("en"); // in en locale, all units always available
        PeriodBuilder b = pbf.getSingleUnitBuilder();
        for (TimeUnit unit = YEAR; unit != null; unit = unit.smaller()) {
            Period p = b.create((long)(approxDurations[unit.ordinal()]*2.5));
            assertTrue(null, p.isSet(unit));
        }

        pbf.setAvailableUnitRange(MINUTE, MONTH);
        // units that are not available are never set
        b = pbf.getSingleUnitBuilder();
        for (TimeUnit unit = YEAR; unit != null; unit = unit.smaller()) {
            Period p = b.create((long)(approxDurations[unit.ordinal()]*2.5));
            assertEquals(null, p.isSet(unit), unit.ordinal() >= MONTH.ordinal() && unit.ordinal() <= MINUTE.ordinal());
        }

        // fixed unit builder returns null when unit is not available
        for (TimeUnit unit = YEAR; unit != null; unit = unit.smaller()) {
            b = pbf.getFixedUnitBuilder(unit);
            if (unit.ordinal() >= MONTH.ordinal() && unit.ordinal() <= MINUTE.ordinal()) {
                assertNotNull(null, b);
            } else {
                assertNull(null, b);
            }
        }

        // can't set empty range
        try {
            pbf.setAvailableUnitRange(MONTH, MINUTE);
            fail("set empty range");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void testSetUnitIsAvailable() {
        pbf = BasicPeriodFormatterService.getInstance().newPeriodBuilderFactory();
        pbf.setAvailableUnitRange(MONTH, MONTH);
        assertNotNull(null, pbf.getSingleUnitBuilder());
        assertNotNull(null, pbf.getOneOrTwoUnitBuilder());
        assertNotNull(null, pbf.getMultiUnitBuilder(2));

        // now no units are available, make sure we can't generate a builder
        pbf.setUnitIsAvailable(MONTH, false);
        assertNull(null, pbf.getSingleUnitBuilder());
        assertNull(null, pbf.getOneOrTwoUnitBuilder());
        assertNull(null, pbf.getMultiUnitBuilder(2));

        pbf.setUnitIsAvailable(DAY, true);
        assertNotNull(null, pbf.getSingleUnitBuilder());
        assertNotNull(null, pbf.getOneOrTwoUnitBuilder());
        assertNotNull(null, pbf.getMultiUnitBuilder(2));
    }
    
    @Test
    public void testBuilderFactoryPeriodConstruction() {
        // see ticket #8307
        pbf = BasicPeriodFormatterService.getInstance().newPeriodBuilderFactory();
        pbf.setAvailableUnitRange(SECOND, DAY);
        PeriodBuilder pb = pbf.getOneOrTwoUnitBuilder();
        long H1M35S30M100 = 100 + 1000 * (30 + 35 * 60 + 1 * 60 * 60);
        Period p = pb.create(H1M35S30M100);
        assertEquals("hours", 1.0f, p.getCount(HOUR));
        assertEquals("minutes", 35.501f, p.getCount(MINUTE));
        assertFalse("seconds", p.isSet(SECOND));
        
        pb = pbf.getMultiUnitBuilder(3);
        p = pb.create(H1M35S30M100);
        assertEquals("hours", 1.0f, p.getCount(HOUR));
        assertEquals("minutes", 35f, p.getCount(MINUTE));
        assertEquals("seconds", 30.1f, p.getCount(SECOND));
        assertFalse("millis", p.isSet(MILLISECOND));
    }
}
