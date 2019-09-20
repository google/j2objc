/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2001-2013, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.bidi;

import android.icu.lang.UCharacterDirection;
import android.icu.text.Bidi;


/**
 * Data and helper methods for Bidi regression tests
 *
 * Ported from C by Lina Kemmel, Matitiahu Allouche
 *
 */
public class TestData {
    protected static final int L   = UCharacterDirection.LEFT_TO_RIGHT;
    protected static final int R   = UCharacterDirection.RIGHT_TO_LEFT;
    protected static final int EN  = UCharacterDirection.EUROPEAN_NUMBER;
    protected static final int ES  = UCharacterDirection.EUROPEAN_NUMBER_SEPARATOR;
    protected static final int ET  = UCharacterDirection.EUROPEAN_NUMBER_TERMINATOR;
    protected static final int AN  = UCharacterDirection.ARABIC_NUMBER;
    protected static final int CS  = UCharacterDirection.COMMON_NUMBER_SEPARATOR;
    protected static final int B   = UCharacterDirection.BLOCK_SEPARATOR;
    protected static final int S   = UCharacterDirection.SEGMENT_SEPARATOR;
    protected static final int WS  = UCharacterDirection.WHITE_SPACE_NEUTRAL;
    protected static final int ON  = UCharacterDirection.OTHER_NEUTRAL;
    protected static final int LRE = UCharacterDirection.LEFT_TO_RIGHT_EMBEDDING;
    protected static final int LRO = UCharacterDirection.LEFT_TO_RIGHT_OVERRIDE;
    protected static final int AL  = UCharacterDirection.RIGHT_TO_LEFT_ARABIC;
    protected static final int RLE = UCharacterDirection.RIGHT_TO_LEFT_EMBEDDING;
    protected static final int RLO = UCharacterDirection.RIGHT_TO_LEFT_OVERRIDE;
    protected static final int PDF = UCharacterDirection.POP_DIRECTIONAL_FORMAT;
    protected static final int NSM = UCharacterDirection.DIR_NON_SPACING_MARK;
    protected static final int BN  = UCharacterDirection.BOUNDARY_NEUTRAL;
    protected static final int FSI = UCharacterDirection.FIRST_STRONG_ISOLATE;
    protected static final int LRI = UCharacterDirection.LEFT_TO_RIGHT_ISOLATE;
    protected static final int RLI = UCharacterDirection.RIGHT_TO_LEFT_ISOLATE;
    protected static final int PDI = UCharacterDirection.POP_DIRECTIONAL_ISOLATE;
    protected static final int DEF = Bidi.CLASS_DEFAULT;

    protected static final String[] dirPropNames = {
        "L", "R", "EN", "ES", "ET", "AN", "CS", "B", "S", "WS", "ON",
        "LRE", "LRO", "AL", "RLE", "RLO", "PDF", "NSM", "BN",
        "FSI", "LRI", "RLI", "PDI"  /* new in Unicode 6.3/ICU 52 */
    };
    protected static final short[][] testDirProps = {
        { L, L, WS, L, WS, EN, L, B },                                          // 0
        { R, AL, WS, R, AL, WS, R },                                            // 1
        { L, L, WS, EN, CS, WS, EN, CS, EN, WS, L, L },                         // 2
        { L, AL, AL, AL, L, AL, AL, L, WS, EN, CS, WS, EN, CS, EN, WS, L, L },  // 3
        { AL, R, AL, WS, EN, CS, WS, EN, CS, EN, WS, R, R, WS, L, L },          // 4
        { R, EN, NSM, ET },                                                     // 5
        { RLE, WS, R, R, R, WS, PDF, WS, B },                                   // 6
        {
    LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE,      /* 15 entries */
    LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE,      /* 15 entries */
    AN, RLO, NSM, LRE, PDF, RLE, ES, EN, ON                                         /*  9 entries */
        },                                                                      //7
        {
    LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE,      /* 15 entries */
    LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE, LRE,      /* 15 entries */
    LRE, BN, CS, RLO, S, PDF, EN, LRO, AN, ES                                       /* 10 entries */
        },                                                                      // 8
        { S, WS, NSM, RLE, WS, L, L, L, WS, LRO, WS, R, R, R, WS, RLO, WS, L, L,
            L, WS, LRE, WS, R, R, R, WS, PDF, WS, L, L, L, WS, PDF, WS, AL, AL,
            AL, WS, PDF, WS, L, L, L, WS, PDF, WS, L, L, L, WS, PDF, ON, PDF,
            BN, BN, ON, PDF },                                                  // 9
        { NSM, WS, L, L, L, L, L, L, L, WS, L, L, L, L, WS, R, R, R, R, R, WS,
            L, L, L, L, L, L, L, WS, WS, AL, AL, AL, AL, WS, EN, EN, ES, EN,
            EN, CS, S, EN, EN, CS, WS, EN, EN, WS, AL, AL, AL, AL, AL, B, L, L,
            L, L, L, L, L, L, WS, AN, AN, CS, AN, AN, WS },                     // 10
        { NSM, WS, L, L, L, L, L, L, L, WS, L, L, L, L, WS, R, R, R, R, R, WS,
            L, L, L, L, L, L, L, WS, WS, AL, AL, AL, AL, WS, EN, EN, ES, EN,
            EN, CS, S, EN, EN, CS, WS, EN, EN, WS, AL, AL, AL, AL, AL, B, L, L,
            L, L, L, L, L, L, WS, AN, AN, CS, AN, AN, WS },                     // 11
        { NSM, WS, L, L, L, L, L, L, L, WS, L, L, L, L, WS, R, R, R, R, R, WS,
            L, L, L, L, L, L, L, WS, WS, AL, AL, AL, AL, WS, EN, EN, ES, EN,
            EN, CS, S, EN, EN, CS, WS, EN, EN, WS, AL, AL, AL, AL, AL, B, L, L,
            L, L, L, L, L, L, WS, AN, AN, CS, AN, AN, WS },                     // 12
        { NSM, WS, L, L, L, L, L, L, L, WS, L, L, L, L, WS, R, R, R, R, R, WS,
            L, L, L, L, L, L, L, WS, WS, AL, AL, AL, AL, WS, EN, EN, ES, EN,
            EN, CS, S, EN, EN, CS, WS, EN, EN, WS, AL, AL, AL, AL, AL, B, L, L,
            L, L, L, L, L, L, WS, AN, AN, CS, AN, AN, WS },                     // 13
        { NSM, WS, L, L, L, L, L, L, L, WS, L, L, L, L, WS, R, R, R, R, R, WS,
            L, L, L, L, L, L, L, WS, WS, AL, AL, AL, AL, WS, EN, EN, ES, EN,
            EN, CS, S, EN, EN, CS, WS, EN, EN, WS, AL, AL, AL, AL, AL, B, L, L,
            L, L, L, L, L, L, WS, AN, AN, CS, AN, AN, WS },                     // 14
        { ON, L, RLO, CS, R, WS, AN, AN, PDF, LRE, R, L, LRO, WS, BN, ON, S,
            LRE, LRO, B },                                                      // 15
        { ON, L, RLO, CS, R, WS, AN, AN, PDF, LRE, R, L, LRO, WS, BN, ON, S,
            LRE, LRO, B },                                                      // 16
        { RLO, RLO, AL, AL, WS, EN, ES, ON, WS, S, S, PDF, LRO, WS, AL, ET, RLE,
            ON, EN, B },                                                        // 17
        { R, L, CS, L },                                                        // 18
        { L, L, L, WS, L, L, L, WS, L, L, L },                                  // 19
        { R, R, R, WS, R, R, R, WS, R, R, R },                                  // 20
        { L },                                                                  // 21
        null                                                                    // 22
    };

    protected static final byte[][] testLevels = {
        { 0, 0, 0, 0, 0, 0, 0, 0 },                                             // 0
        { 1, 1, 1, 1, 1, 1, 1 },                                                // 1
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },                                 // 2
        { 0, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },               // 3
        { 1, 1, 1, 1, 2, 1, 1, 2, 2, 2, 1, 1, 1, 1, 2, 2 },                     // 4
        { 1, 2, 2, 2 },                                                         // 5
        { 1, 1, 1, 1, 1, 1, 1, 1, 1 },                                          // 6
        {
    126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126,      /* 15 entries */
    126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126, 126,      /* 15 entries */
    126, 125, 125, 125, 125, 125, 125, 125, 125                                     /*  9 entries */
        },                                                                      // 7
        {
    124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124,      /* 15 entries */
    124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124, 124,      /* 15 entries */
    124, 124, 124, 64, 64, 124, 124, 126, 126, 124                                  /* 10 entries */
        },                                                                      // 8
        { 0, 0, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4,
            5, 5, 5, 4, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },                               // 9
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 1, 2, 2, 1, 0, 2, 2, 1, 1,
            2, 2, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2,
            0 },                                                                // 10
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 1, 2, 2, 1, 0, 2, 2, 1, 1,
            2, 2, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2,
            0 },                                                                // 11
        { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 3, 4, 4, 3, 2, 4, 4, 3, 3,
            4, 4, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4,
            2 },                                                                // 12
        { 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5, 5, 6, 6,
            6, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5, 5, 6, 6, 5, 6, 6, 5, 5, 6, 6, 5, 5,
            6, 6, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 5, 6, 6, 6, 6, 6,
            5 },                                                                // 13
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 1, 2, 2, 1, 0, 2, 2, 1, 1,
            2, 2, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2,
            0 },                                                                // 14
        { 0, 0, 1, 1, 1, 1, 1, 1, 3, 3, 3, 2, 4, 4, 4, 4, 0, 0, 0, 0 },         // 15
        { 0, 0, 1, 1, 1, 0 },                                                   // 16
        { 1 },                                                                  // 17
        { 2 },                                                                  // 18
        { 2, 2, 2, 2, 2, 2, 2, 1 },                                             // 19
        { 1, 1, 1, 1, 1, 1, 1, 0 },                                             // 20
        { 2 },                                                                  // 21
        null                                                                    // 22
    };

    protected static final int[][] testVisualMaps = {
        { 0, 1, 2, 3, 4, 5, 6, 7 },                                             // 0
        { 6, 5, 4, 3, 2, 1, 0 },                                                // 1
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 },                               // 2
        { 0, 3, 2, 1, 4, 6, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 },       // 3
        { 15, 14, 13, 12, 11, 10, 9, 6, 7, 8, 5, 4, 3, 2, 0, 1 },               // 4
        { 3, 0, 1, 2 },                                                         // 5
        { 8, 7, 6, 5, 4, 3, 2, 1, 0 },                                          // 6
        {
    8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,                       /* 15 entries */
    23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,                     /* 15 entries */
    38, 7, 6, 5, 4, 3, 2, 1, 0                                                      /*  9 entries */
        },                                                                      // 7
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
            20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
            37, 38, 39 },                                                       // 8
        { 0, 1, 2, 44, 43, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 31, 30, 29, 28, 27,
            26, 20, 21, 24, 23, 22, 25, 19, 18, 17, 16, 15, 14, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 3, 45, 46, 47, 48, 49, 50, 51, 52, 53,
            54, 55, 56, 57 },                                                   // 9
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 19, 18, 17, 16, 15,
            20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 40, 39, 38, 37, 36, 34, 35,
            33, 31, 32, 30, 41, 52, 53, 51, 50, 48, 49, 47, 46, 45, 44, 43, 42,
            54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69 },   // 10
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 19, 18, 17, 16, 15,
            20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 40, 39, 38, 37, 36, 34, 35,
            33, 31, 32, 30, 41, 52, 53, 51, 50, 48, 49, 47, 46, 45, 44, 43, 42,
            54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69 },   // 11
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 19, 18, 17, 16, 15,
            20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 40, 39, 38, 37, 36, 34, 35,
            33, 31, 32, 30, 41, 52, 53, 51, 50, 48, 49, 47, 46, 45, 44, 43, 42,
            54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69 },   // 12
        { 69, 68, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 55, 54, 53,
            52, 51, 50, 49, 42, 43, 44, 45, 46, 47, 48, 41, 40, 39, 38, 37, 36,
            35, 33, 34, 32, 30, 31, 29, 28, 26, 27, 25, 24, 22, 23, 21, 20, 19,
            18, 17, 16, 15, 7, 8, 9, 10, 11, 12, 13, 14, 6, 1, 2, 3, 4, 5, 0 }, // 13
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 19, 18, 17, 16, 15,
            20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 40, 39, 38, 37, 36, 34, 35,
            33, 31, 32, 30, 41, 52, 53, 51, 50, 48, 49, 47, 46, 45, 44, 43, 42,
            54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69 },   // 14
        { 0, 1, 15, 14, 13, 12, 11, 10, 4, 3, 2, 5, 6, 7, 8, 9, 16, 17, 18, 19 }, // 15
        { 0, 1, 4, 3, 2, 5 },                                                   // 16
        { 0 },                                                                  // 17
        { 0 },                                                                  // 18
        { 1, 2, 3, 4, 5, 6, 7, 0 },                                             // 19
        { 6, 5, 4, 3, 2, 1, 0, 7 },                                             // 20
        { 0 },                                                                  // 21
        null                                                                    // 22
    };

    protected static final byte[] testParaLevels = {
        Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_LTR,
        Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_LTR,
        Bidi.LEVEL_DEFAULT_LTR, 64,                     64,
        Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_RTL,
        2, 5, Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_LTR,
        Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_LTR, Bidi.RTL, Bidi.LTR, Bidi.RTL,
        Bidi.LEVEL_DEFAULT_LTR
    };

    protected static final byte[] testDirections = {
        Bidi.LTR, Bidi.RTL, Bidi.LTR, Bidi.MIXED, Bidi.MIXED, Bidi.MIXED,
        Bidi.RTL, Bidi.MIXED, Bidi.MIXED, Bidi.MIXED, Bidi.MIXED, Bidi.MIXED,
        Bidi.MIXED, Bidi.MIXED, Bidi.MIXED, Bidi.MIXED, Bidi.MIXED, Bidi.RTL,
        Bidi.LTR, Bidi.MIXED, Bidi.MIXED, Bidi.MIXED, Bidi.LTR
    };

    protected static final byte[] testResultLevels = new byte[] {
        Bidi.LTR, Bidi.RTL, Bidi.LTR, Bidi.LTR, Bidi.RTL, Bidi.RTL, Bidi.RTL,
        64,       64      , Bidi.LTR, Bidi.LTR, Bidi.LTR, 2, 5, Bidi.LTR,
        Bidi.LTR, Bidi.LTR, Bidi.RTL, 2, Bidi.RTL, Bidi.LTR, Bidi.RTL, Bidi.LTR
    };

    protected static final byte[] testLineStarts = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 13,
        2, 0, 0, -1, -1
    };

    protected static final byte[] testLineLimits = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 14,
        3, 8, 8, -1, -1
    };

    protected short[] dirProps;
    protected int lineStart;
    protected int lineLimit;
    protected byte direction;
    protected byte paraLevel;
    protected byte resultLevel;
    protected byte[] levels;
    protected int[] visualMap;

    private TestData(short[] dirProps, int lineStart, int lineLimit,
            byte direction, byte paraLevel, byte resultLevel, byte[] levels,
            int[] visualMap) {
        this.dirProps = dirProps;
        this.lineStart = lineStart;
        this.lineLimit = lineLimit;
        this.direction = direction;
        this.paraLevel = paraLevel;
        this.resultLevel = resultLevel;
        this.levels = levels;
        this.visualMap = visualMap;
    }

    protected static TestData getTestData(int testNumber) {
        return new TestData(testDirProps[testNumber],
                testLineStarts[testNumber], testLineLimits[testNumber],
                testDirections[testNumber], testParaLevels[testNumber],
                testResultLevels[testNumber], testLevels[testNumber],
                testVisualMaps[testNumber]);
    }

    protected static int testCount() {
        return testDirProps.length;
    }
}
