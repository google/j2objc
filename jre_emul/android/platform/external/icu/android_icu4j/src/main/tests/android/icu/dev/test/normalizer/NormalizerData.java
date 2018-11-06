/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 * Copyright (C) 1998-2007 International Business Machines Corporation and
 * Unicode, Inc. All Rights Reserved.<br>
 * The Unicode Consortium makes no expressed or implied warranty of any
 * kind, and assumes no liability for errors or omissions.
 * No liability is assumed for incidental and consequential damages
 * in connection with or arising out of the use of the information here.
 */
package android.icu.dev.test.normalizer;

import java.util.BitSet;

import android.icu.dev.test.UTF16Util;

/**
 * Accesses the Normalization Data used for Forms C and D.<br>
 * @author Mark Davis
 * Updates for supplementary code points:
 * Vladimir Weinstein & Markus Scherer
 */
public class NormalizerData {
//    static final String copyright = "Copyright (C) 1998-2003 International Business Machines Corporation and Unicode, Inc.";

    /**
    * Constant for use in getPairwiseComposition
    */
    public static final int NOT_COMPOSITE = '\uFFFF';

    /**
    * Gets the combining class of a character from the
    * Unicode Character Database.
    * @param   ch      the source character
    * @return          value from 0 to 255
    */
    public int getCanonicalClass(int ch) {
        return canonicalClass.get(ch);
    }

    /**
    * Returns the composite of the two characters. If the two
    * characters don't combine, returns NOT_COMPOSITE.
    * @param   first   first character (e.g. 'c')
    * @param   second  second character (e.g. \u0327 cedilla)
    * @return          composite (e.g. \u00C7 c cedilla)
    */
    public int getPairwiseComposition(int first, int second) {
        return compose.get(((long)first << 32) | second);
    }


    /**
    * Gets recursive decomposition of a character from the
    * Unicode Character Database.
    * @param   canonical    If true
    *                  bit is on in this byte, then selects the recursive
    *                  canonical decomposition, otherwise selects
    *                  the recursive compatibility and canonical decomposition.
    * @param   ch      the source character
    * @param   buffer  buffer to be filled with the decomposition
    */
    public void getRecursiveDecomposition(boolean canonical, int ch, StringBuffer buffer) {
        String decomp = decompose.get(ch);
        if (decomp != null && !(canonical && isCompatibility.get(ch))) {
            for (int i = 0; i < decomp.length(); i+=UTF16Util.codePointLength(ch)) {
                ch = UTF16Util.nextCodePoint(decomp, i);
                getRecursiveDecomposition(canonical, ch, buffer);
            }
        } else {                    // if no decomp, append
            UTF16Util.appendCodePoint(buffer, ch);
        }
    }

    // =================================================
    //                   PRIVATES
    // =================================================

    /**
     * Only accessed by NormalizerBuilder.
     */
    NormalizerData(IntHashtable canonicalClass, IntStringHashtable decompose,
      LongHashtable compose, BitSet isCompatibility, BitSet isExcluded) {
        this.canonicalClass = canonicalClass;
        this.decompose = decompose;
        this.compose = compose;
        this.isCompatibility = isCompatibility;
        this.isExcluded = isExcluded;
    }

    /**
    * Just accessible for testing.
    */
    boolean getExcluded (char ch) {
        return isExcluded.get(ch);
    }

    /**
    * Just accessible for testing.
    */
    String getRawDecompositionMapping (char ch) {
        return decompose.get(ch);
    }

    /**
    * For now, just use IntHashtable
    * Two-stage tables would be used in an optimized implementation.
    */
    private IntHashtable canonicalClass;

    /**
    * The main data table maps chars to a 32-bit int.
    * It holds either a pair: top = first, bottom = second
    * or singleton: top = 0, bottom = single.
    * If there is no decomposition, the value is 0.
    * Two-stage tables would be used in an optimized implementation.
    * An optimization could also map chars to a small index, then use that
    * index in a small array of ints.
    */
    private IntStringHashtable decompose;

    /**
    * Maps from pairs of characters to single.
    * If there is no decomposition, the value is NOT_COMPOSITE.
    */
    private LongHashtable compose;

    /**
    * Tells whether decomposition is canonical or not.
    */
    private BitSet isCompatibility = new BitSet();

    /**
    * Tells whether character is script-excluded or not.
    * Used only while building, and for testing.
    */

    private BitSet isExcluded = new BitSet();
}
