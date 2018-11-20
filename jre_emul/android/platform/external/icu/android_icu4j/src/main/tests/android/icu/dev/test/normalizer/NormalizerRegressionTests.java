/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2005, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.dev.test.normalizer;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.Normalizer;

public class NormalizerRegressionTests extends TestFmwk {
    @Test
    public void TestJB4472() {
        // submitter's test case
        String tamil = "\u0b87\u0ba8\u0bcd\u0ba4\u0bbf\u0baf\u0bbe";
        logln("Normalized: " + Normalizer.isNormalized(tamil, Normalizer.NFC, 0));

        // markus's test case
        // the combining cedilla can't be applied to 'b', so this is in normalized form.
        // but the isNormalized test identifies the cedilla as a 'maybe' and so tries
        // to normalize the relevant substring ("b\u0327")and compare the result to the
        // original.  the original code was passing in the start and length of the
        // substring (3, 5-3 = 2) but the called code was expecting start and limit.
        // it subtracted the start again to get what it thought was the length, but
        // ended up with -1.  the loop was incrementing an index from 0 and testing
        // against length, but 0 was never == -1 before it walked off the array end.

        // a workaround in lieu of this patch is to catch the exception and always
        // normalize.

        // this should return true, since the string is normalized (and it should
        // not throw an exception!)
        String sample = "aaab\u0327";
        logln("Normalized: " + Normalizer.isNormalized(sample, Normalizer.NFC, 0));

        // this should return false, since the string is _not_ normalized (and it should
        // not throw an exception!)
        String sample2 = "aaac\u0327";
        logln("Normalized: " + Normalizer.isNormalized(sample2, Normalizer.NFC, 0));
    }
}
