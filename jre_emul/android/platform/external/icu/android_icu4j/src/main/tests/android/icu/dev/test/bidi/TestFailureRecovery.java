/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2007-2013, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.bidi;

import org.junit.Test;

import android.icu.text.Bidi;

/**
 * Regression test for Bidi failure recovery
 *
 * @author Lina Kemmel, Matitiahu Allouche
 */

public class TestFailureRecovery extends BidiFmwk {

    @Test
    public void testFailureRecovery()
    {
        logln("\nEntering TestFailureRecovery\n");
        Bidi bidi = new Bidi();
        // Skip the following test since there are no invalid values
        // between MAX_EXPLICIT_LEVEL+1 and LEVEL_DEFAULT_LTR
        //try {
        //    bidi.setPara("abc", (byte)(Bidi.LEVEL_DEFAULT_LTR - 1), null);
        //    errln("Bidi.setPara did not fail when passed too big para level");
        //} catch (IllegalArgumentException e) {
        //    logln("OK: Got exception for bidi.setPara(..., Bidi.LEVEL_DEFAULT_LTR - 1, ...)"
        //            + " as expected: " + e.getMessage());
        //}
        try {
            bidi.setPara("abc", (byte)(-1), null);
            errln("Bidi.setPara did not fail when passed negative para level");
        } catch (IllegalArgumentException e) {
            logln("OK: Got exception for bidi.setPara(..., -1, ...)"
                    + " as expected: " + e.getMessage());
        }
        try {
            Bidi.writeReverse(null, 0);
            errln("Bidi.writeReverse did not fail when passed a null string");
        } catch (IllegalArgumentException e) {
            logln("OK: Got exception for Bidi.writeReverse(null) as expected: "
                  + e.getMessage());
        }
        bidi = new Bidi();
        try {
            bidi.setLine(0, 1);
            errln("bidi.setLine did not fail when called before valid setPara()");
        } catch (IllegalStateException e) {
            logln("OK: Got exception for Bidi.setLine(0, 1) as expected: "
                  + e.getMessage());
        }
        try {
            bidi.getDirection();
            errln("bidi.getDirection did not fail when called before valid setPara()");
        } catch (IllegalStateException e) {
            logln("OK: Got exception for Bidi.getDirection() as expected: "
                  + e.getMessage());
        }
        bidi.setPara("abc", Bidi.LTR, null);
        try {
            bidi.getLevelAt(3);
            errln("bidi.getLevelAt did not fail when called with bad argument");
        } catch (IllegalArgumentException e) {
            logln("OK: Got exception for Bidi.getLevelAt(3) as expected: "
                  + e.getMessage());
        }
        try {
            bidi = new Bidi(-1, 0);
            errln("Bidi constructor did not fail when called with bad argument");
        } catch (IllegalArgumentException e) {
            logln("OK: Got exception for Bidi(-1,0) as expected: "
                  + e.getMessage());
        }
        bidi = new Bidi(2, 1);
        try {
            bidi.setPara("abc", Bidi.LTR, null);
            errln("setPara did not fail when called with text too long");
        } catch (OutOfMemoryError e) {
            logln("OK: Got exception for setPara(\"abc\") as expected: "
                  + e.getMessage());
        }
        try {
            bidi.setPara("=2", Bidi.RTL, null);
            bidi.countRuns();
            errln("countRuns did not fail when called for too many runs");
        } catch (OutOfMemoryError e) {
            logln("OK: Got exception for countRuns as expected: "
                  + e.getMessage());
        }
        int rm = bidi.getReorderingMode();
        bidi.setReorderingMode(Bidi.REORDER_DEFAULT - 1);
        if (rm != bidi.getReorderingMode()) {
            errln("setReorderingMode with bad argument #1 should have no effect");
        }
        bidi.setReorderingMode(9999);
        if (rm != bidi.getReorderingMode()) {
            errln("setReorderingMode with bad argument #2 should have no effect");
        }
        /* Try a surrogate char */
        bidi = new Bidi();
        bidi.setPara("\uD800\uDC00", Bidi.RTL, null);
        if (bidi.getDirection() != Bidi.MIXED) {
            errln("getDirection for 1st surrogate char should be MIXED");
        }
        byte[] levels = new byte[] {6,5,4};
        try {
            bidi.setPara("abc", (byte)5, levels);
            errln("setPara did not fail when called with bad levels");
        } catch (IllegalArgumentException e) {
            logln("OK: Got exception for setPara(..., levels) as expected: "
                  + e.getMessage());
        }

        logln("\nExiting TestFailureRecovery\n");
    }
}
