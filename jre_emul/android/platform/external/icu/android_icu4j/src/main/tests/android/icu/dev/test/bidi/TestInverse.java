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

import java.util.Arrays;

import org.junit.Test;

import android.icu.impl.Utility;
import android.icu.text.Bidi;

/**
 * Regression test for the basic "inverse" Bidi mode.
 *
 * ported from C by Lina Kemmel, Matitiahu Allouche
 */

public class TestInverse extends BidiFmwk {

    private int countRoundtrips = 0;
    private int countNonRoundtrips = 0;

    static final String[] testCases = {
        "\u006c\u0061\u0028\u0074\u0069\u006e\u0020\u05d0\u05d1\u0029\u05d2\u05d3",
        "\u006c\u0061\u0074\u0020\u05d0\u05d1\u05d2\u0020\u0031\u0032\u0033",
        "\u006c\u0061\u0074\u0020\u05d0\u0028\u05d1\u05d2\u0020\u0031\u0029\u0032\u0033",
        "\u0031\u0032\u0033\u0020\u05d0\u05d1\u05d2\u0020\u0034\u0035\u0036",
        "\u0061\u0062\u0020\u0061\u0062\u0020\u0661\u0662"
    };

    @Test
    public void testInverse() {
        Bidi bidi;
        int i;

        logln("\nEntering TestInverse\n");
        bidi = new Bidi();
        log("inverse Bidi: testInverse(L) with " + testCases.length +
            " test cases ---\n");
        for(i = 0; i < testCases.length; ++i) {
            logln("Testing case " + i);
            _testInverseBidi(bidi, testCases[i], Bidi.DIRECTION_LEFT_TO_RIGHT);
        }

        log("inverse Bidi: testInverse(R) with " + testCases.length +
            " test cases ---\n");
        for (i = 0; i < testCases.length; ++i) {
            logln("Testing case " + i);
            _testInverseBidi(bidi, testCases[i], Bidi.DIRECTION_RIGHT_TO_LEFT);
        }

        _testManyInverseBidi(bidi, Bidi.DIRECTION_LEFT_TO_RIGHT);
        _testManyInverseBidi(bidi, Bidi.DIRECTION_RIGHT_TO_LEFT);

        logln("inverse Bidi: rountrips: " + countRoundtrips +
              "   non-roundtrips: " + countNonRoundtrips);

        _testWriteReverse();

        _testManyAddedPoints();

        _testMisc();

        logln("\nExiting TestInverse\n");
    }

    private static final char[][] repeatSegments = {
        { 0x61, 0x62 },     /* L */
        { 0x5d0, 0x5d1 },   /* R */
        { 0x627, 0x628 },   /* AL */
        { 0x31, 0x32 },     /* EN */
        { 0x661, 0x662 },   /* AN */
        { 0x20, 0x20 }      /* WS (N) */
    };
    private static final int COUNT_REPEAT_SEGMENTS = 6;

    private void _testManyInverseBidi(Bidi bidi, int direction) {
        char[] text = { 0, 0, 0x20, 0, 0, 0x20, 0, 0 };
        int i, j, k;

        log("inverse Bidi: testManyInverseBiDi(" +
            (direction == Bidi.DIRECTION_LEFT_TO_RIGHT ? 'L' : 'R') +
            ") - test permutations of text snippets ---\n");
        for (i = 0; i < COUNT_REPEAT_SEGMENTS; ++i) {
            text[0] = repeatSegments[i][0];
            text[1] = repeatSegments[i][1];
            for (j = 0; j < COUNT_REPEAT_SEGMENTS; ++j) {
                text[3] = repeatSegments[j][0];
                text[4] = repeatSegments[j][1];
                for (k = 0; k < COUNT_REPEAT_SEGMENTS; ++k) {
                    text[6] = repeatSegments[k][0];
                    text[7] = repeatSegments[k][1];

                    log("inverse Bidi: testManyInverseBiDi()[" +
                        i + " " + j + " " + k + "]\n");
                    _testInverseBidi(bidi, new String(text), direction);
                }
            }
        }
    }

    private void _testInverseBidi(Bidi bidi, String src, int direction) {
        String visualLTR, logicalDest, visualDest;
        try {
            if (direction == Bidi.DIRECTION_LEFT_TO_RIGHT) {
                log("inverse Bidi: testInverse(L)\n");

                /* convert visual to logical */
                bidi.setInverse(true);
                if (!bidi.isInverse()) {
                    err("Error while doing setInverse(true)\n");
                }
                bidi.setPara(src, Bidi.LTR, null);
                if (!Arrays.equals(src.toCharArray(), bidi.getText())) {
                    err("Wrong value returned by getText\n");
                }
                if (!src.equals(bidi.getTextAsString())) {
                    err("Wrong value returned by getTextAsString\n");
                }
                logicalDest = bidi.writeReordered(Bidi.DO_MIRRORING |
                                                  Bidi.INSERT_LRM_FOR_NUMERIC);
                log("  v ");
                printUnicode(src.toCharArray(), bidi.getLevels());
                log("\n");

                /* convert back to visual LTR */
                bidi.setInverse(false);
                if (bidi.isInverse()) {
                    err("Error while doing setInverse(false)\n");
                }
                bidi.setPara(logicalDest, Bidi.LTR, null);
                visualDest = bidi.writeReordered(Bidi.DO_MIRRORING |
                                                 Bidi.REMOVE_BIDI_CONTROLS);
            } else {
                logln("inverse Bidi: testInverse(R)\n");

                /* reverse visual from RTL to LTR */
                visualLTR = Bidi.writeReverse(src, 0);
                log("  vr");
                printUnicode(src.toCharArray(), null);
                log("\n");

                /* convert visual RTL to logical */
                bidi.setInverse(true);
                bidi.setPara(visualLTR, Bidi.LTR, null);
                logicalDest = bidi.writeReordered(Bidi.DO_MIRRORING |
                                                  Bidi.INSERT_LRM_FOR_NUMERIC);
                log("  vl");
                printUnicode(visualLTR.toCharArray(), bidi.getLevels());
                log("\n");

                /* convert back to visual RTL */
                bidi.setInverse(false);
                bidi.setPara(logicalDest, Bidi.LTR, null);
                visualDest = bidi.writeReordered(Bidi.DO_MIRRORING |
                             Bidi.REMOVE_BIDI_CONTROLS | Bidi.OUTPUT_REVERSE);
            }
            log("  l ");
            printUnicode(logicalDest.toCharArray(), bidi.getLevels());
            log("\n");
            log("  v ");
            printUnicode(visualDest.toCharArray(), null);
            log("\n");
        } catch (Exception e) {
            errln("\ninverse Bidi: *** failed");
            errln("   error message: " + e.getMessage());
            e.printStackTrace();
            visualDest = null;
        }

        /* check and print results */
        if (src.equals(visualDest)) {
            ++countRoundtrips;
            log(" + roundtripped\n");
        } else {
            ++countNonRoundtrips;
            log(" * did not roundtrip\n");
        }
    }

    private void _testWriteReverse() {
        /* U+064e and U+0650 are combining marks (Mn) */
        final String
            forward = "\u200f\u0627\u064e\u0650\u0020\u0028\u0031\u0029",
            reverseKeepCombining =
                "\u0029\u0031\u0028\u0020\u0627\u064e\u0650\u200f",
            reverseRemoveControlsKeepCombiningDoMirror =
                "\u0028\u0031\u0029\u0020\u0627\u064e\u0650";

        String reverse;

        /* test Bidi.writeReverse() with "interesting" options */
        try {
            reverse = Bidi.writeReverse(forward, Bidi.KEEP_BASE_COMBINING);
        } catch (Exception e) {
            errln("Failure in Bidi.writeReverse(KEEP_BASE_COMBINING)");
            reverse = null;
        }
        assertEquals("\nFailure in " + getClass().toString() +
                     " in Bidi.writeReverse", reverseKeepCombining,
                     reverse, forward, null, "KEEP_BASE_COMBINING", null);

        try {
            reverse = Bidi.writeReverse(forward, Bidi.REMOVE_BIDI_CONTROLS |
                                        Bidi.DO_MIRRORING | Bidi.KEEP_BASE_COMBINING);
        } catch (Exception e) {
            errln("Failure in Bidi.writeReverse(KEEP_BASE_COMBINING)");
        }
        assertEquals("\nFailure in " + getClass().toString() +
                     " in Bidi.writeReverse",
                     reverseRemoveControlsKeepCombiningDoMirror,
                     reverse, forward, null,
                     "REMOVE_BIDI_CONTROLS|DO_MIRRORING|KEEP_BASE_COMBINING",
                     null);
    }

    private void printUnicode(char[] chars, byte[] levels) {
        int i;

        log("{ ");
        for (i = 0; i < chars.length; ++i) {
            log("0x" + Utility.hex(chars[i]));
            if (levels != null) {
                log("." + levels[i]);
            }
            log("   ");
        }
        log(" }");
    }

    private void _testManyAddedPoints() {
        Bidi bidi = new Bidi();
        char[] text = new char[90];
        for (int i = 0; i < text.length; i+=3) {
            text[i] = 'a';
            text[i+1] = '\u05d0';
            text[i+2] = '3';
        }
        bidi.setReorderingMode(Bidi.REORDER_INVERSE_LIKE_DIRECT);
        bidi.setReorderingOptions(Bidi.OPTION_INSERT_MARKS);
        bidi.setPara(text, Bidi.LTR, null);
        String out = bidi.writeReordered(0);
        char[] expected = new char[120];
        for (int i = 0; i < expected.length; i+=4) {
            expected[i] = 'a';
            expected[i+1] = '\u05d0';
            expected[i+2] = '\u200e';
            expected[i+3] = '3';
        }
        assertEquals("\nInvalid output with many added points",
                     new String(expected), out);
    }

    private void _testMisc() {
        Bidi bidi = new Bidi();
        bidi.setInverse(true);
        bidi.setPara("   ", Bidi.RTL, null);
        String out = bidi.writeReordered(Bidi.OUTPUT_REVERSE | Bidi.INSERT_LRM_FOR_NUMERIC);
        assertEquals("\nInvalid output with RLM at both sides",
                     "\u200f   \u200f", out);
    }
}
