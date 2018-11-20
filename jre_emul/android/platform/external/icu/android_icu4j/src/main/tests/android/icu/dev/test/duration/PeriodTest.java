/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007-2010, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

// Copyright 2006 Google Inc.  All Rights Reserved.

package android.icu.dev.test.duration;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.duration.Period;
import android.icu.impl.duration.TimeUnit;

public class PeriodTest extends TestFmwk {
    @Test
    public void testIsSet() {
        Period p = Period.at(0, TimeUnit.YEAR);
        assertTrue(null, p.isSet());
        assertTrue(null, p.isSet(TimeUnit.YEAR));
        assertFalse(null, p.isSet(TimeUnit.MONTH));
        assertEquals(null, 0f, p.getCount(TimeUnit.YEAR), .1f);
        p = p.omit(TimeUnit.YEAR);
        assertFalse(null, p.isSet(TimeUnit.YEAR));
    }

    @Test
    public void testMoreLessThan() {
        Period p = Period.moreThan(1, TimeUnit.YEAR);
        assertTrue(null, p.isMoreThan());
        assertFalse(null, p.isLessThan());
        p = p.at();
        assertFalse(null, p.isMoreThan());
        assertFalse(null, p.isLessThan());
        p = p.lessThan();
        assertFalse(null, p.isMoreThan());
        assertTrue(null, p.isLessThan());
        p = p.moreThan();
        assertTrue(null, p.isMoreThan());
        assertFalse(null, p.isLessThan());

        p = Period.lessThan(1, TimeUnit.YEAR);
        assertFalse(null, p.isMoreThan());
        assertTrue(null, p.isLessThan());

        p = Period.at(1, TimeUnit.YEAR);
        assertFalse(null, p.isMoreThan());
        assertFalse(null, p.isLessThan());

        assertEquals(null, 1f, p.getCount(TimeUnit.YEAR), .1f);
    }

    @Test
    public void testFuturePast() {
        Period p = Period.at(1, TimeUnit.YEAR).inFuture();
        assertTrue(null, p.isInFuture());
        p = p.inPast();
        assertFalse(null, p.isInFuture());
        p = p.inFuture(true);
        assertTrue(null, p.isInFuture());
        p = p.inFuture(false);
        assertFalse(null, p.isInFuture());
    }

    @Test
    public void testAnd() {
        Period p = Period.at(1, TimeUnit.YEAR).and(3, TimeUnit.MONTH)
                .inFuture();
        assertTrue(null, p.isSet(TimeUnit.YEAR));
        assertTrue(null, p.isSet(TimeUnit.MONTH));
        assertEquals(null, 3f, p.getCount(TimeUnit.MONTH), .1f);
        p = p.and(2, TimeUnit.MONTH);
        assertEquals(null, 2f, p.getCount(TimeUnit.MONTH), .1f);
    }

    @Test
    public void testInvalidCount() {
        try {
            Period.at(-1, TimeUnit.YEAR);
            fail("at -1");
        } catch (IllegalArgumentException e) {
            // passed
        }
        try {
            Period.moreThan(-1, TimeUnit.YEAR);
            fail("moreThan -1");
        } catch (IllegalArgumentException e) {
            // passed
        }
        try {
            Period.lessThan(-1, TimeUnit.YEAR);
            fail("lessThan -1");
        } catch (IllegalArgumentException e) {
            // passed
        }
        Period p = Period.at(1, TimeUnit.YEAR);
        try {
            p = p.and(-1, TimeUnit.MONTH);
            fail("and -1");
        } catch (IllegalArgumentException e) {
            // passed
        }
    }
}
