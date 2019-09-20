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

import android.icu.dev.test.UTF16Util;

/**
 * Implements Unicode Normalization Forms C, D, KC, KD.<br>
 * See UTR#15 for details.<br>
 * @author Mark Davis
 * Updates for supplementary code points:
 * Vladimir Weinstein & Markus Scherer
 */
public class UnicodeNormalizer {
//    static final String copyright = "Copyright (C) 1998-2003 International Business Machines Corporation and Unicode, Inc.";

    /**
     * Create a normalizer for a given form.
     */
    public UnicodeNormalizer(byte form, boolean fullData) {
        this.form = form;
        if (data == null) data = NormalizerBuilder.build(fullData); // load 1st time
    }

    /**
    * Masks for the form selector
    */
    static final byte
        COMPATIBILITY_MASK = 1,
        COMPOSITION_MASK = 2;

    /**
    * Normalization Form Selector
    */
    public static final byte
        D = 0 ,
        C = COMPOSITION_MASK,
        KD = COMPATIBILITY_MASK,
        KC = (byte)(COMPATIBILITY_MASK + COMPOSITION_MASK);

    /**
    * Normalizes text according to the chosen form,
    * replacing contents of the target buffer.
    * @param   source      the original text, unnormalized
    * @param   target      the resulting normalized text
    */
    public StringBuffer normalize(String source, StringBuffer target) {

        // First decompose the source into target,
        // then compose if the form requires.

        if (source.length() != 0) {
            internalDecompose(source, target);
            if ((form & COMPOSITION_MASK) != 0) {
                internalCompose(target);
            }
        }
        return target;
    }

    /**
    * Normalizes text according to the chosen form
    * @param   source      the original text, unnormalized
    * @return  target      the resulting normalized text
    */
    public String normalize(String source) {
        return normalize(source, new StringBuffer()).toString();
    }

    // ======================================
    //                  PRIVATES
    // ======================================

    /**
     * The current form.
     */
    private byte form;

    /**
    * Decomposes text, either canonical or compatibility,
    * replacing contents of the target buffer.
    * @param   form        the normalization form. If COMPATIBILITY_MASK
    *                      bit is on in this byte, then selects the recursive
    *                      compatibility decomposition, otherwise selects
    *                      the recursive canonical decomposition.
    * @param   source      the original text, unnormalized
    * @param   target      the resulting normalized text
    */
    private void internalDecompose(String source, StringBuffer target) {
        StringBuffer buffer = new StringBuffer();
        boolean canonical = (form & COMPATIBILITY_MASK) == 0;
        int ch;
        for (int i = 0; i < source.length();) {
            buffer.setLength(0);
            ch = UTF16Util.nextCodePoint(source, i);
            i+=UTF16Util.codePointLength(ch);
            data.getRecursiveDecomposition(canonical, ch, buffer);

            // add all of the characters in the decomposition.
            // (may be just the original character, if there was
            // no decomposition mapping)

            for (int j = 0; j < buffer.length();) {
                ch = UTF16Util.nextCodePoint(buffer, j);
                j+=UTF16Util.codePointLength(ch);
                int chClass = data.getCanonicalClass(ch);
                int k = target.length(); // insertion point
                if (chClass != 0) {

                    // bubble-sort combining marks as necessary

                    int ch2;
                    for (; k > 0; k -= UTF16Util.codePointLength(ch2)) {
                        ch2 = UTF16Util.prevCodePoint(target, k);
                        if (data.getCanonicalClass(ch2) <= chClass) break;
                    }
                }
                UTF16Util.insertCodePoint(target, k, ch);
            }
        }
    }

    /**
    * Composes text in place. Target must already
    * have been decomposed.
    * @param   target      input: decomposed text.
    *                      output: the resulting normalized text.
    */
    private void internalCompose(StringBuffer target) {

        int starterPos = 0;
        int starterCh = UTF16Util.nextCodePoint(target,0);
        int compPos = UTF16Util.codePointLength(starterCh);
        int lastClass = data.getCanonicalClass(starterCh);
        if (lastClass != 0) lastClass = 256; // fix for irregular combining sequence

        // Loop on the decomposed characters, combining where possible

        for (int decompPos = UTF16Util.codePointLength(starterCh); decompPos < target.length(); ) {
            int ch = UTF16Util.nextCodePoint(target, decompPos);
            decompPos += UTF16Util.codePointLength(ch);
            int chClass = data.getCanonicalClass(ch);
            int composite = data.getPairwiseComposition(starterCh, ch);
            if (composite != NormalizerData.NOT_COMPOSITE
            && (lastClass < chClass || lastClass == 0)) {
                UTF16Util.setCodePointAt(target, starterPos, composite);
                starterCh = composite;
            } else {
                if (chClass == 0) {
                    starterPos = compPos;
                    starterCh  = ch;
                }
                lastClass = chClass;
                decompPos += UTF16Util.setCodePointAt(target, compPos, ch);
                compPos += UTF16Util.codePointLength(ch);
            }
        }
        target.setLength(compPos);
    }

    /**
    * Contains normalization data from the Unicode Character Database.
    * use false for the minimal set, true for the real set.
    */
    private static NormalizerData data = null;

    /**
    * Just accessible for testing.
    */
    boolean getExcluded (char ch) {
        return data.getExcluded(ch);
    }

    /**
    * Just accessible for testing.
    */
    String getRawDecompositionMapping (char ch) {
        return data.getRawDecompositionMapping(ch);
    }
}