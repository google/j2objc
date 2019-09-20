/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.impl;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.CacheValue;
import android.icu.impl.CacheValue.Strength;

public class CacheTest extends TestFmwk {
    public CacheTest() {}

    /** Code coverage for CacheValue. */
    @Test
    public void testNullCacheValue() {
        CacheValue<Object> nv = CacheValue.getInstance(null);
        assertTrue("null CacheValue isNull()", nv.isNull());
        assertTrue("null CacheValue get()==null", nv.get() == null);
        assertTrue("null CacheValue reset==null", nv.resetIfCleared(null) == null);
        try {
            Object v = nv.resetIfCleared(this);
            fail("null CacheValue reset(not null) should throw an Exception, returned " +
                    v + " instead");
        } catch(Exception expected) {
        }
    }

    /** Code coverage for CacheValue. */
    @Test
    public void testStrongCacheValue() {
        boolean wasStrong = CacheValue.futureInstancesWillBeStrong();
        CacheValue.setStrength(Strength.STRONG);
        assertTrue("setStrength(STRONG).futureInstancesWillBeStrong()",
                CacheValue.futureInstancesWillBeStrong());
        CacheValue<Object> sv = CacheValue.<Object>getInstance(this);
        assertFalse("strong CacheValue not isNull()", sv.isNull());
        assertTrue("strong CacheValue get()==same", sv.get() == this);
        // A strong CacheValue never changes value.
        // The implementation does not check that the new value is equal to the old one,
        // or even of equal type, so it does not matter which new value we pass in.
        assertTrue("strong CacheValue reset==same", sv.resetIfCleared("") == this);
        if (!wasStrong) {
            CacheValue.setStrength(Strength.SOFT);
        }
    }

    /** Code coverage for CacheValue. */
    @Test
    public void testSoftCacheValue() {
        boolean wasStrong = CacheValue.futureInstancesWillBeStrong();
        CacheValue.setStrength(Strength.SOFT);
        assertFalse("setStrength(SOFT).futureInstancesWillBeStrong()",
                CacheValue.futureInstancesWillBeStrong());
        CacheValue<Object> sv = CacheValue.<Object>getInstance(this);
        assertFalse("soft CacheValue not isNull()", sv.isNull());
        Object v = sv.get();
        assertTrue("soft CacheValue get()==same or null", v == this || v == null);
        assertTrue("soft CacheValue reset==same", sv.resetIfCleared(this) == this);
        if (wasStrong) {
            CacheValue.setStrength(Strength.STRONG);
        }
    }
}
