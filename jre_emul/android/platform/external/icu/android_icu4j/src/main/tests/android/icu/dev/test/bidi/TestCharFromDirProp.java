/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2007-2010, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.bidi;

import org.junit.Test;

import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterDirection;

/**
 * Regression test for Bidi charFromDirProp
 *
 * @author Lina Kemmel, Matitiahu Allouche
 */

public class TestCharFromDirProp extends BidiFmwk {

    /* verify that the exemplar characters have the expected bidi classes */
    @Test
    public void testCharFromDirProp() {

        logln("\nEntering TestCharFromDirProp");
        int i = UCharacterDirection.CHAR_DIRECTION_COUNT;
        while (i-- > 0) {
            char c = charFromDirProp[i];
            int dir = UCharacter.getDirection(c);
            assertEquals("UCharacter.getDirection(TestData.charFromDirProp[" + i
                    + "] == U+" + Utility.hex(c) + ") failed", i, dir);
        }
        logln("\nExiting TestCharFromDirProp");
    }
}
