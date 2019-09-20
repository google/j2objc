/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2001-2010, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.bidi;

import java.util.Arrays;

import org.junit.Test;

import android.icu.text.Bidi;

/**
 * Regression test for variants to the UBA.
 *
 * @author Lina Kemmel, Matitiahu Allouche
 */

public class TestReorderingMode extends BidiFmwk {

    static final String[] textIn = {
    /* (0) 123 */
        "123",
    /* (1) .123->4.5 */
        ".123->4.5",
    /* (2) 678 */
        "678",
    /* (3) .678->8.9 */
        ".678->8.9",
    /* (4) JIH1.2,3MLK */
        "JIH1.2,3MLK",
    /* (5) FE.>12-> */
        "FE.>12->",
    /* (6) JIH.>12->a */
        "JIH.>12->a",
    /* (7) CBA.>67->89=a */
        "CBA.>67->89=a",
    /* (8) CBA.123->xyz */
        "CBA.123->xyz",
    /* (9) .>12->xyz */
        ".>12->xyz",
    /* (10) a.>67->xyz */
        "a.>67->xyz",
    /* (11) 123JIH */
        "123JIH",
    /* (12) 123 JIH */
        "123 JIH"
    };

    static final String[] textOut = {
    /* TC 0: 123 */
        "123",                                                              /* (0) */
    /* TC 1: .123->4.5 */
        ".123->4.5",                                                        /* (1) */
        "4.5<-123.",                                                        /* (2) */
    /* TC 2: 678 */
        "678",                                                              /* (3) */
    /* TC 3: .678->8.9 */
        ".8.9<-678",                                                        /* (4) */
        "8.9<-678.",                                                        /* (5) */
        ".678->8.9",                                                        /* (6) */
    /* TC 4: MLK1.2,3JIH */
        "KLM1.2,3HIJ",                                                      /* (7) */
    /* TC 5: FE.>12-> */
        "12<.EF->",                                                         /* (8) */
        "<-12<.EF",                                                         /* (9) */
        "EF.>@12->",                                                        /* (10) */
    /* TC 6: JIH.>12->a */
        "12<.HIJ->a",                                                       /* (11) */
        "a<-12<.HIJ",                                                       /* (12) */
        "HIJ.>@12->a",                                                      /* (13) */
        "a&<-12<.HIJ",                                                      /* (14) */
    /* TC 7: CBA.>67->89=a */
        "ABC.>@67->89=a",                                                   /* (15) */
        "a=89<-67<.ABC",                                                    /* (16) */
        "a&=89<-67<.ABC",                                                   /* (17) */
        "89<-67<.ABC=a",                                                    /* (18) */
    /* TC 8: CBA.123->xyz */
        "123.ABC->xyz",                                                     /* (19) */
        "xyz<-123.ABC",                                                     /* (20) */
        "ABC.@123->xyz",                                                    /* (21) */
        "xyz&<-123.ABC",                                                    /* (22) */
    /* TC 9: .>12->xyz */
        ".>12->xyz",                                                        /* (23) */
        "xyz<-12<.",                                                        /* (24) */
        "xyz&<-12<.",                                                       /* (25) */
    /* TC 10: a.>67->xyz */
        "a.>67->xyz",                                                       /* (26) */
        "a.>@67@->xyz",                                                     /* (27) */
        "xyz<-67<.a",                                                       /* (28) */
    /* TC 11: 123JIH */
        "123HIJ",                                                           /* (29) */
        "HIJ123",                                                           /* (30) */
    /* TC 12: 123 JIH */
        "123 HIJ",                                                          /* (31) */
        "HIJ 123",                                                          /* (32) */
    };

    static final int[][][][] outIndices = {
        { /* TC 0: 123 */
            {{ 0,  0}, { 0,  0}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 0,  0}, { 0,  0}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 0,  0}, { 0,  0}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 0,  0}, { 0,  0}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 1: .123->4.5 */
            {{ 1,  2}, { 1,  2}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  2}, { 1,  2}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 1,  2}, { 1,  2}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  2}, { 1,  2}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 2: 678 */
            {{ 3,  3}, { 3,  3}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 3,  3}, { 3,  3}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 3,  3}, { 3,  3}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 3,  3}, { 3,  3}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 3: .678->8.9 */
            {{ 6,  5}, { 6,  5}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 4,  5}, { 4,  5}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 6,  5}, { 6,  5}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 6,  5}, { 6,  5}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 4: MLK1.2,3JIH */
            {{ 7,  7}, { 7,  7}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 7,  7}, { 7,  7}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 7,  7}, { 7,  7}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 7,  7}, { 7,  7}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 5: FE.>12-> */
            {{ 8,  9}, { 8,  9}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{10,  9}, { 8,  9}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 8,  9}, { 8,  9}}, /* REORDER_NUMBERS_SPECIAL */
            {{10,  9}, { 8,  9}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 6: JIH.>12->a */
            {{11, 12}, {11, 12}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{13, 14}, {11, 12}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{11, 12}, {11, 12}}, /* REORDER_NUMBERS_SPECIAL */
            {{13, 14}, {11, 12}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 7: CBA.>67->89=a */
            {{18, 16}, {18, 16}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{18, 17}, {18, 16}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{18, 16}, {18, 16}}, /* REORDER_NUMBERS_SPECIAL */
            {{15, 17}, {18, 16}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 8: CBA.>124->xyz */
            {{19, 20}, {19, 20}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{21, 22}, {19, 20}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{19, 20}, {19, 20}}, /* REORDER_NUMBERS_SPECIAL */
            {{21, 22}, {19, 20}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 9: .>12->xyz */
            {{23, 24}, {23, 24}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{23, 25}, {23, 24}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{23, 24}, {23, 24}}, /* REORDER_NUMBERS_SPECIAL */
            {{23, 25}, {23, 24}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 10: a.>67->xyz */
            {{26, 26}, {26, 26}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{26, 27}, {26, 28}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{26, 28}, {26, 28}}, /* REORDER_NUMBERS_SPECIAL */
            {{26, 27}, {26, 28}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 11: 124JIH */
            {{30, 30}, {30, 30}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{29, 30}, {29, 30}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{30, 30}, {30, 30}}, /* REORDER_NUMBERS_SPECIAL */
            {{30, 30}, {30, 30}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        },
        { /* TC 12: 124 JIH */
            {{32, 32}, {32, 32}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{31, 32}, {31, 32}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{31, 32}, {31, 32}}, /* REORDER_NUMBERS_SPECIAL */
            {{31, 32}, {31, 32}}  /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
        }
    };

    static final short[] modes = {
        Bidi.REORDER_GROUP_NUMBERS_WITH_R,
        Bidi.REORDER_INVERSE_LIKE_DIRECT,
        Bidi.REORDER_NUMBERS_SPECIAL,
        Bidi.REORDER_INVERSE_FOR_NUMBERS_SPECIAL,
        Bidi.REORDER_INVERSE_NUMBERS_AS_L
    };

    static final int[] options = { Bidi.OPTION_INSERT_MARKS, 0 };

    static final byte[] paraLevels = { Bidi.LTR, Bidi.RTL };

    static final int TC_COUNT = textIn.length;
    static final int MODES_COUNT = modes.length;
    static final int OPTIONS_COUNT = options.length;
    static final int LEVELS_COUNT = paraLevels.length;

    @Test
    public void testReorderingMode() {

        String src, dest;
        Bidi bidi = new Bidi();
        Bidi bidi2 = new Bidi();
        Bidi bidi3 = new Bidi();
        int tc, mode, option, level;
        int modeValue, modeBack;
        int optionValue, optionBack;
        int index;
        String expected;
        boolean testOK = true;

        logln("\nEntering TestReorderingMode\n");

        bidi2.setInverse(true);

        for (tc = 0; tc < TC_COUNT; tc++) {
            src = textIn[tc];

            for (mode = 0; mode < MODES_COUNT; mode++) {
                modeValue = modes[mode];
                bidi.setReorderingMode(modeValue);
                modeBack = bidi.getReorderingMode();
                if (modeValue != modeBack) {
                    errln("Error while setting reordering mode to " +
                    modeValue + ", returned " + modeBack);
                }

                for (option = 0; option < OPTIONS_COUNT; option++) {
                    optionValue = options[option];
                    bidi.setReorderingOptions(optionValue);
                    optionBack = bidi.getReorderingOptions();
                    if (optionValue != optionBack) {
                        errln("Error while setting reordering options to " +
                        modeValue + ", returned " + modeBack);
                    }

                    for (level = 0; level < LEVELS_COUNT; level++) {
                        logln("starting test " + tc + " mode=" + modeValue +
                            " option=" + optionValue + " level=" + level);
                        bidi.setPara(pseudoToU16(src), paraLevels[level], null);

                        dest = bidi.writeReordered(Bidi.DO_MIRRORING);
                        dest = u16ToPseudo(dest);
                        if (!((modeValue == Bidi.REORDER_INVERSE_NUMBERS_AS_L) &&
                              (optionValue == Bidi.OPTION_INSERT_MARKS))) {
                            checkWhatYouCan(bidi, src, dest);
                        }
                        String modeDesc = modeToString(modeValue);
                        String optDesc = spOptionsToString(optionValue);

                        if (modeValue == Bidi.REORDER_INVERSE_NUMBERS_AS_L) {
                            index = -1;
                            expected = inverseBasic(bidi2, src, optionValue,
                                                    paraLevels[level]);
                        }
                        else {
                            index = outIndices[tc][mode][option][level];
                            expected = textOut[index];
                        }
                        if (!assertEquals("Actual and expected output mismatch",
                                          expected, dest, src, modeDesc, optDesc,
                                          String.valueOf(level))) {
                            testOK = false;
                            continue;
                        }
                        if ((optionValue == Bidi.OPTION_INSERT_MARKS) &&
                            !assertRoundTrip(bidi3, tc, index, src, dest,
                                             mode, option,
                                             paraLevels[level])) {
                            testOK = false;
                            continue;
                        }
                        if (!checkResultLength(bidi, src, dest, modeDesc, optDesc,
                                               paraLevels[level])) {
                            testOK = false;
                            continue;
                        }
                        if ((index > -1) &&
                            !checkMaps(bidi, index, src, dest, modeDesc, optDesc,
                                       paraLevels[level], true)) {
                            testOK = false;
                        }
                    }
                }
            }
        }
        if (testOK) {
            logln("Reordering mode test OK");
        }

        logln("\nExiting TestReorderingMode\n");
    }

    String inverseBasic(Bidi bidi, String src, int option, byte level) {
        String dest2;

        if (bidi == null || src == null) {
            return null;
        }
        bidi.setReorderingOptions(option);
        bidi.setPara(pseudoToU16(src), level, null);
        dest2 = u16ToPseudo(bidi.writeReordered(Bidi.DO_MIRRORING));
        if (!(option == Bidi.OPTION_INSERT_MARKS)) {
            checkWhatYouCan(bidi, src, dest2);
        }
        return dest2;
    }

    static final byte roundtrip[][][][] =
    {
        { /* TC 0: 123 */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 1,  1}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 1: .123->4.5 */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 1,  1}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 2: 678 */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 1,  1}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 3: .678->8.9 */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 1,  1}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 0,  0}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 4: MLK1.2,3JIH */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 1,  1}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 5: FE.>12-> */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 0,  1}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 6: JIH.>12->a */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 0,  0}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 7: CBA.>67->89=a */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 0,  1}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 0,  0}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 8: CBA.>123->xyz */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 0,  0}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 9: .>12->xyz */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 1,  0}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 10: a.>67->xyz */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 1,  1}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  0}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 11: 123JIH */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 1,  1}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        },
        { /* TC 12: 123 JIH */
            {{ 1,  1}, { 1,  1}}, /* REORDER_GROUP_NUMBERS_WITH_R */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_LIKE_DIRECT */
            {{ 1,  1}, { 1,  1}}, /* REORDER_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}, /* REORDER_INVERSE_FOR_NUMBERS_SPECIAL */
            {{ 1,  1}, { 1,  1}}  /* REORDER_INVERSE_NUMBERS_AS_L */
        }
    };

    private boolean assertRoundTrip(Bidi bidi, int tc, int outIndex,
                                    String src, String dest,
                                    int mode, int option, byte level) {
        String descMode, descOption;
        String dest2;

        switch (modes[mode]) {
            case Bidi.REORDER_NUMBERS_SPECIAL:
                bidi.setReorderingMode(Bidi.REORDER_INVERSE_FOR_NUMBERS_SPECIAL);
                break;
            case Bidi.REORDER_GROUP_NUMBERS_WITH_R:
                bidi.setReorderingMode(Bidi.REORDER_GROUP_NUMBERS_WITH_R);
                break;
            case Bidi.REORDER_RUNS_ONLY:
                bidi.setReorderingMode(Bidi.REORDER_RUNS_ONLY);
                break;
            case Bidi.REORDER_INVERSE_NUMBERS_AS_L:
                bidi.setReorderingMode(Bidi.REORDER_DEFAULT);
                break;
            case Bidi.REORDER_INVERSE_LIKE_DIRECT:
                bidi.setReorderingMode(Bidi.REORDER_DEFAULT);
                break;
            case Bidi.REORDER_INVERSE_FOR_NUMBERS_SPECIAL:
                bidi.setReorderingMode(Bidi.REORDER_NUMBERS_SPECIAL);
                break;
            default:
                bidi.setReorderingMode(Bidi.REORDER_INVERSE_LIKE_DIRECT);
                break;
        }
        bidi.setReorderingOptions(Bidi.OPTION_REMOVE_CONTROLS);

        bidi.setPara(pseudoToU16(dest), level, null);
        dest2 = bidi.writeReordered(Bidi.DO_MIRRORING);

        dest2 = u16ToPseudo(dest2);
        checkWhatYouCan(bidi, dest, dest2);
        descMode = modeToString(modes[mode]);
        descOption = spOptionsToString(options[option]);
        if (!src.equals(dest2)) {
            if (roundtrip[tc][mode][option][level] == 1) {
                errln("\nRound trip failed for case=" + tc +
                      " mode=" + mode + " option=" + option +
                      "\nOriginal text:      " + src +
                      "\nRound-tripped text: " + dest2 +
                      "\nIntermediate text:  " + dest +
                      "\nReordering mode:    " + descMode +
                      "\nReordering option:  " + descOption +
                      "\nParagraph level:    " + level);
            } else {
                logln("\nExpected round trip failure for case=" + tc +
                      " mode=" + mode + " option=" + option +
                      "\nOriginal text:      " + src +
                      "\nRound-tripped text: " + dest2 +
                      "\nIntermediate text:  " + dest +
                      "\nReordering mode:    " + descMode +
                      "\nReordering option:  " + descOption +
                      "\nParagraph level:    " + level);
            }
            return false;
        }
        if (!checkResultLength(bidi, dest, dest2, descMode,
                               "OPTION_REMOVE_CONTROLS", level)) {
            return false;
        }
        if ((outIndex > -1) &&
            !checkMaps(bidi, outIndex, src, dest, descMode,
                       "OPTION_REMOVE_CONTROLS", level, false)) {
            return false;
        }
        return true;
    }

    private boolean checkResultLength(Bidi bidi, String src, String dest,
                                   String mode, String option, byte level) {
        int actualLen;
        if (mode.equals("REORDER_INVERSE_NUMBERS_AS_L"))
            actualLen = dest.length();
        else
            actualLen = bidi.getResultLength();
        if (actualLen != dest.length()) {
            errln("\nBidi.getResultLength failed." +
                  "\nExpected:           " + dest.length() +
                  "\nActual:             " + actualLen +
                  "\nInput:              " + src +
                  "\nOutput:             " + dest +
                  "\nReordering mode:    " + mode +
                  "\nReordering option:  " + option +
                  "\nParagraph level:    " + level);
            return false;
        }
        return true;
    }

    static String formatMap(int[] map)
    {
        char[] buffer = new char[map.length];
        int i, k;
        char c;
        for (i = 0; i < map.length; i++) {
            k = map[i];
            if (k < 0)
                c = '-';
            else if (k >= columns.length)
                c = '+';
            else
                c = columns[k];
            buffer[i] = c;
        }
        return new String(buffer);
    }

    static final int NO = Bidi.MAP_NOWHERE;

    static final int forwardMap[][] = {
    /* TC 0: 123 */
        { 0, 1, 2 },                                                    /* (0) */
    /* TC 1: .123->4.5 */
        { 0, 1, 2, 3, 4, 5, 6, 7, 8 },                                  /* (1) */
        { 8, 5, 6, 7, 4, 3, 0, 1, 2 },                                  /* (2) */
    /* TC 2: 678 */
        { 0, 1, 2 },                                                    /* (3) */
    /* TC 3: .678->8.9 */
        { 0, 6, 7, 8, 5, 4, 1, 2, 3 },                                  /* (4) */
        { 8, 5, 6, 7, 4, 3, 0, 1, 2 },                                  /* (5) */
        { 0, 1, 2, 3, 4, 5, 6, 7, 8 },                                  /* (6) */
    /* TC 4: MLK1.2,3JIH */
        { 10, 9, 8, 3, 4, 5, 6, 7, 2, 1, 0 },                           /* (7) */
    /* TC 5: FE.>12-> */
        { 5, 4, 3, 2, 0, 1, 6, 7 },                                     /* (8) */
        { 7, 6, 5, 4, 2, 3, 1, 0 },                                     /* (9) */
        { 1, 0, 2, 3, 5, 6, 7, 8 },                                     /* (10) */
    /* TC 6: JIH.>12->a */
        { 6, 5, 4, 3, 2, 0, 1, 7, 8, 9 },                               /* (11) */
        { 9, 8, 7, 6, 5, 3, 4, 2, 1, 0 },                               /* (12) */
        { 2, 1, 0, 3, 4, 6, 7, 8, 9, 10 },                              /* (13) */
        { 10, 9, 8, 7, 6, 4, 5, 3, 2, 0 },                              /* (14) */
    /* TC 7: CBA.>67->89=a */
        { 2, 1, 0, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13 },                  /* (15) */
        { 12, 11, 10, 9, 8, 6, 7, 5, 4, 2, 3, 1, 0 },                   /* (16) */
        { 13, 12, 11, 10, 9, 7, 8, 6, 5, 3, 4, 2, 0 },                  /* (17) */
        { 10, 9, 8, 7, 6, 4, 5, 3, 2, 0, 1, 11, 12 },                   /* (18) */
    /* TC 8: CBA.123->xyz */
        { 6, 5, 4, 3, 0, 1, 2, 7, 8, 9, 10, 11 },                       /* (19) */
        { 11, 10, 9, 8, 5, 6, 7, 4, 3, 0, 1, 2 },                       /* (20) */
        { 2, 1, 0, 3, 5, 6, 7, 8, 9, 10, 11, 12 },                      /* (21) */
        { 12, 11, 10, 9, 6, 7, 8, 5, 4, 0, 1, 2 },                      /* (22) */
    /* TC 9: .>12->xyz */
        { 0, 1, 2, 3, 4, 5, 6, 7, 8 },                                  /* (23) */
        { 8, 7, 5, 6, 4, 3, 0, 1, 2 },                                  /* (24) */
        { 9, 8, 6, 7, 5, 4, 0, 1, 2 },                                  /* (25) */
    /* TC 10: a.>67->xyz */
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },                               /* (26) */
        { 0, 1, 2, 4, 5, 7, 8, 9, 10, 11 },                             /* (27) */
        { 9, 8, 7, 5, 6, 4, 3, 0, 1, 2 },                               /* (28) */
    /* TC 11: 123JIH */
        { 0, 1, 2, 5, 4, 3 },                                           /* (29) */
        { 3, 4, 5, 2, 1, 0 },                                           /* (30) */
    /* TC 12: 123 JIH */
        { 0, 1, 2, 3, 6, 5, 4 },                                        /* (31) */
        { 4, 5, 6, 3, 2, 1, 0 },                                        /* (32) */
    };
    static final int inverseMap[][] = {
    /* TC 0: 123 */
        { 0, 1, 2 },                                                    /* (0) */
    /* TC 1: .123->4.5 */
        { 0, 1, 2, 3, 4, 5, 6, 7, 8 },                                  /* (1) */
        { 6, 7, 8, 5, 4, 1, 2, 3, 0 },                                  /* (2) */
    /* TC 2: 678 */
        { 0, 1, 2 },                                                    /* (3) */
    /* TC 3: .678->8.9 */
        { 0, 6, 7, 8, 5, 4, 1, 2, 3 },                                  /* (4) */
        { 6, 7, 8, 5, 4, 1, 2, 3, 0 },                                  /* (5) */
        { 0, 1, 2, 3, 4, 5, 6, 7, 8 },                                  /* (6) */
    /* TC 4: MLK1.2,3JIH */
        { 10, 9, 8, 3, 4, 5, 6, 7, 2, 1, 0 },                           /* (7) */
    /* TC 5: FE.>12-> */
        { 4, 5, 3, 2, 1, 0, 6, 7 },                                     /* (8) */
        { 7, 6, 4, 5, 3, 2, 1, 0 },                                     /* (9) */
        { 1, 0, 2, 3, NO, 4, 5, 6, 7 },                                 /* (10) */
    /* TC 6: JIH.>12->a */
        { 5, 6, 4, 3, 2, 1, 0, 7, 8, 9 },                               /* (11) */
        { 9, 8, 7, 5, 6, 4, 3, 2, 1, 0 },                               /* (12) */
        { 2, 1, 0, 3, 4, NO, 5, 6, 7, 8, 9 },                           /* (13) */
        { 9, NO, 8, 7, 5, 6, 4, 3, 2, 1, 0 },                           /* (14) */
    /* TC 7: CBA.>67->89=a */
        { 2, 1, 0, 3, 4, NO, 5, 6, 7, 8, 9, 10, 11, 12 },               /* (15) */
        { 12, 11, 9, 10, 8, 7, 5, 6, 4, 3, 2, 1, 0 },                   /* (16) */
        { 12, NO, 11, 9, 10, 8, 7, 5, 6, 4, 3, 2, 1, 0 },               /* (17) */
        { 9, 10, 8, 7, 5, 6, 4, 3, 2, 1, 0, 11, 12 },                   /* (18) */
    /* TC 8: CBA.123->xyz */
        { 4, 5, 6, 3, 2, 1, 0, 7, 8, 9, 10, 11 },                       /* (19) */
        { 9, 10, 11, 8, 7, 4, 5, 6, 3, 2, 1, 0 },                       /* (20) */
        { 2, 1, 0, 3, NO, 4, 5, 6, 7, 8, 9, 10, 11 },                   /* (21) */
        { 9, 10, 11, NO, 8, 7, 4, 5, 6, 3, 2, 1, 0 },                   /* (22) */
    /* TC 9: .>12->xyz */
        { 0, 1, 2, 3, 4, 5, 6, 7, 8 },                                  /* (23) */
        { 6, 7, 8, 5, 4, 2, 3, 1, 0 },                                  /* (24) */
        { 6, 7, 8, NO, 5, 4, 2, 3, 1, 0 },                              /* (25) */
    /* TC 10: a.>67->xyz */
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },                               /* (26) */
        { 0, 1, 2, NO, 3, 4, NO, 5, 6, 7, 8, 9 },                       /* (27) */
        { 7, 8, 9, 6, 5, 3, 4, 2, 1, 0 },                               /* (28) */
    /* TC 11: 123JIH */
        { 0, 1, 2, 5, 4, 3 },                                           /* (29) */
        { 5, 4, 3, 0, 1, 2 },                                           /* (30) */
    /* TC 12: 123 JIH */
        { 0, 1, 2, 3, 6, 5, 4 },                                        /* (31) */
        { 6, 5, 4, 3, 0, 1, 2 },                                        /* (32) */
    };

    private boolean checkMaps(Bidi bidi, int stringIndex, String src, String dest,
            String mode, String option, byte level, boolean forward) {

        int[] actualLogicalMap;
        int[] actualVisualMap;
        int[] getIndexMap;
        int i, srcLen, resLen, index;
        int[] expectedLogicalMap, expectedVisualMap;
        boolean testOK = true;

        if (forward) {
            expectedLogicalMap = forwardMap[stringIndex];
            expectedVisualMap  = inverseMap[stringIndex];
        } else {
            expectedLogicalMap = inverseMap[stringIndex];
            expectedVisualMap  = forwardMap[stringIndex];
        }
        actualLogicalMap = bidi.getLogicalMap();
        srcLen = bidi.getProcessedLength();
        if (!Arrays.equals(expectedLogicalMap, actualLogicalMap)) {
            err("Bidi.getLogicalMap returned unexpected map for output " +
                "string index " + stringIndex + "\n" +
                "source: " + src + "\n" +
                "dest  : " + dest + "\n" +
                "Scale : " + columnString + "\n" +
                "ExpMap: " + formatMap(expectedLogicalMap) + "\n" +
                "Actual: " + formatMap(actualLogicalMap) + "\n" +
                "Paragraph level  : " + level + " == " + bidi.getParaLevel() + "\n" +
                "Reordering mode  : " + mode + " == " + bidi.getReorderingMode() + "\n" +
                "Reordering option: " + option + " == " + bidi.getReorderingOptions() + "\n" +
                "Forward flag     : " + forward + "\n");
            testOK = false;
        }
        resLen = bidi.getResultLength();
        actualVisualMap = bidi.getVisualMap();
        if (!Arrays.equals(expectedVisualMap, actualVisualMap)) {
            err("Bidi.getVisualMap returned unexpected map for output " +
                "string index " + stringIndex + "\n" +
                "source: " + src + "\n" +
                "dest  : " + dest + "\n" +
                "Scale : " + columnString + "\n" +
                "ExpMap: " + formatMap(expectedVisualMap) + "\n" +
                "Actual: " + formatMap(actualVisualMap) + "\n" +
                "Paragraph level  : " + level + " == " + bidi.getParaLevel() + "\n" +
                "Reordering mode  : " + mode + " == " + bidi.getReorderingMode() + "\n" +
                "Reordering option: " + option + " == " + bidi.getReorderingOptions() + "\n" +
                "Forward flag     : " + forward + "\n");
            testOK = false;
        }
        getIndexMap = new int[srcLen];
        for (i = 0; i < srcLen; i++) {
            index = bidi.getVisualIndex(i);
            getIndexMap[i] = index;
        }
        if (!Arrays.equals(actualLogicalMap, getIndexMap)) {
            err("Mismatch between getLogicalMap and getVisualIndex for output " +
                "string index " + stringIndex + "\n" +
                "source: " + src + "\n" +
                "dest  : " + dest + "\n" +
                "Scale : " + columnString + "\n" +
                "ActMap: " + formatMap(actualLogicalMap) + "\n" +
                "IdxMap: " + formatMap(getIndexMap) + "\n" +
                "Paragraph level  : " + level + " == " + bidi.getParaLevel() + "\n" +
                "Reordering mode  : " + mode + " == " + bidi.getReorderingMode() + "\n" +
                "Reordering option: " + option + " == " + bidi.getReorderingOptions() + "\n" +
                "Forward flag     : " + forward + "\n");
            testOK = false;
        }
        getIndexMap = new int[resLen];
        for (i = 0; i < resLen; i++) {
            index = bidi.getLogicalIndex(i);
            getIndexMap[i] = index;
        }
        if (!Arrays.equals(actualVisualMap, getIndexMap)) {
            err("Mismatch between getVisualMap and getLogicalIndex for output " +
                "string index " + stringIndex + "\n" +
                "source: " + src + "\n" +
                "dest  : " + dest + "\n" +
                "Scale : " + columnString + "\n" +
                "ActMap: " + formatMap(actualVisualMap) + "\n" +
                "IdxMap: " + formatMap(getIndexMap) + "\n" +
                "Paragraph level  : " + level + " == " + bidi.getParaLevel() + "\n" +
                "Reordering mode  : " + mode + " == " + bidi.getReorderingMode() + "\n" +
                "Reordering option: " + option + " == " + bidi.getReorderingOptions() + "\n" +
                "Forward flag     : " + forward + "\n");
            testOK = false;
        }
        return testOK;
    }
}
