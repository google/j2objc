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

import java.util.Arrays;

import org.junit.Test;

import android.icu.text.Bidi;
import android.icu.text.BidiRun;

/**
 * Regression test for Bidi class override.
 *
 * @author Lina Kemmel, Matitiahu Allouche
 */

public class TestBidi extends BidiFmwk {

    private static final int MAXLEN = 256;
    private static final String levelString = "............................";

    @Test
    public void testBidi() {
        Bidi bidi;
        Bidi bidiLine;

        logln("\nEntering TestBidi");
        bidi = new Bidi(MAXLEN, 0);
        bidiLine = new Bidi();

        doTests(bidi, bidiLine, false);
        doTests(bidi, bidiLine, true);
        doMisc();
        logln("\nExiting TestBidi");
    }

    private void doTests(Bidi bidi, Bidi bidiLine, boolean countRunsFirst) {
        int testNumber;
        String string;
        int lineStart;
        byte paraLevel;
        int bidiTestCount = TestData.testCount();

        for (testNumber = 0; testNumber < bidiTestCount; ++testNumber) {
            TestData test = TestData.getTestData(testNumber);
            string = getStringFromDirProps(test.dirProps);
            paraLevel = test.paraLevel;
            try {
                bidi.setPara(string, paraLevel, null);
                logln("Bidi.setPara(tests[" + testNumber + "] OK, direction "
                        + bidi.getDirection() + " paraLevel "
                        + paraLevel);
            } catch (Exception e) {
                errln("Bidi.setPara(tests[" + testNumber + "] failed, direction "
                        + bidi.getDirection() + " paraLevel "
                        + paraLevel);
            }
            lineStart = test.lineStart;
            if (lineStart == -1) {
                doTest(bidi, testNumber, test, 0, countRunsFirst);
            } else {
                try {
                    bidiLine = bidi.setLine(lineStart, test.lineLimit);
                    logln("Bidi.setLine(" + lineStart + ", " + test.lineLimit
                            + "), in tests[" + testNumber + "] OK, direction "
                            + bidiLine.getDirection() + " paraLevel "
                            + bidiLine.getBaseLevel());
                    doTest(bidiLine, testNumber, test, lineStart, countRunsFirst);
                } catch (Exception e)  {
                    errln("Bidi.setLine(" + lineStart + ", " + test.lineLimit
                            + "), in runAll test[" + testNumber + "] failed");
                }
                /* do it again using createLineBidi instead of setLine */
                try {
                    bidiLine = bidi.createLineBidi(lineStart, test.lineLimit);
                    logln("Bidi.createLineBidi(" + lineStart + ", " + test.lineLimit
                            + "), in tests[" + testNumber + "] OK, direction "
                            + bidiLine.getDirection() + " paraLevel "
                            + bidiLine.getBaseLevel());
                    doTest(bidiLine, testNumber, test, lineStart, countRunsFirst);
                } catch (Exception e)  {
                    errln("Bidi.createLineBidi(" + lineStart + ", " + test.lineLimit
                            + "), in runAll test[" + testNumber + "] failed");
                }
            }
        }
    }

    private void doTest(Bidi bidi, int testNumber, TestData test,
                        int lineStart, boolean countRunsFirst) {
        short[] dirProps = test.dirProps;
        byte[] levels = test.levels;
        int[] visualMap = test.visualMap;
        int i, len = bidi.getLength(), logicalIndex = -1, runCount = 0;
        byte level, level2;

        if (countRunsFirst) {
            logln("Calling Bidi.countRuns() first.");
            try {
                runCount = bidi.countRuns();
            } catch (IllegalStateException e) {
                errln("Bidi.countRuns(test[" + testNumber + "]) failed");
            }
        } else {
            logln("Calling Bidi.getLogicalMap() first.");
        }

        _testReordering(bidi, testNumber);

        for (i = 0; i < len; ++i) {
            logln(i + "  " + bidi.getLevelAt(i) + "  " + levelString
                    + TestData.dirPropNames[dirProps[lineStart + i]] + "  "
                    + bidi.getVisualIndex(i));
        }

        log("\n-----levels:");
        for (i = 0; i < len; ++i) {
            if (i > 0) {
                log(",");
            }
            log(" " + bidi.getLevelAt(i));
        }

        log("\n--reordered:");
        for (i = 0; i < len; ++i) {
            if (i > 0) {
                log(",");
            }
            log(" " + bidi.getVisualIndex(i));
        }
        log("\n");

        assertEquals("\nFailure in Bidi.getDirection(test[" + testNumber + "])",
                     test.direction, bidi.getDirection());
        assertEquals("\nFailure in Bidi.getParaLevel(test[" + testNumber + "])",
                     test.resultLevel, bidi.getParaLevel());

        for (i = 0; i < len; ++i) {
            assertEquals("\nFailure in Bidi.getLevelAt(" + i +
                         ") in test[" + testNumber + "]",
                         levels[i], bidi.getLevelAt(i));
        }

        for (i = 0; i < len; ++i) {
            try {
                logicalIndex = bidi.getVisualIndex(i);
            } catch (Throwable th) {
                errln("Bidi.getVisualIndex(" + i + ") in test[" + testNumber
                        + "] failed");
            }
            if(visualMap[i] != logicalIndex) {
                assertEquals("\nFailure in Bidi.getVisualIndex(" + i +
                             ") in test[" + testNumber + "])",
                             visualMap[i], logicalIndex);
            }
        }

        if (!countRunsFirst) {
            try {
                runCount = bidi.countRuns();
            } catch (IllegalStateException e) {
                errln("Bidi.countRuns(test[" + testNumber + "]) failed");
            }
        }

        BidiRun run;

        for (logicalIndex = 0; logicalIndex < len; ) {
            level = bidi.getLevelAt(logicalIndex);
            run = bidi.getLogicalRun(logicalIndex);
            logicalIndex = run.getLimit();
            level2 = run.getEmbeddingLevel();
            assertEquals("Logical " + run.toString() +
                         " in test[" + testNumber + "]: wrong level",
                         level, level2);
            if (--runCount < 0) {
                errln("Bidi.getLogicalRun(test[" + testNumber
                      + "]): wrong number of runs compared to Bidi.countRuns() = "
                      + bidi.countRuns());
            }
        }
        if (runCount != 0) {
            errln("Bidi.getLogicalRun(test[" + testNumber
                    + "]): wrong number of runs compared to Bidi.countRuns() = "
                    + bidi.countRuns());
        }

        log("\n\n");
    }

    private void _testReordering(Bidi bidi, int testNumber) {
        int[] logicalMap1;
        int[] logicalMap2;
        int[] logicalMap3;
        int[] visualMap1;
        int[] visualMap2;
        int[] visualMap3;
        int[] visualMap4 = new int[MAXLEN];
        byte[] levels;
        int i, length = bidi.getLength(),
               destLength = bidi.getResultLength();
        int runCount, visualIndex, logicalIndex = -1, logicalStart, runLength;
        boolean odd;

        if(length <= 0) {
            return;
        }
        /* get the logical and visual maps from the object */
        logicalMap1 = bidi.getLogicalMap();
        if (logicalMap1 == null) {
            errln("getLogicalMap in test " + testNumber + " is null");
            logicalMap1 = new int[0];
        }

        visualMap1 = bidi.getVisualMap();

        if (visualMap1 == null) {
            errln("getVisualMap() in test " + testNumber + " is null");
            visualMap1 = new int[0];
        }

        /* invert them both */
        visualMap2 = Bidi.invertMap(logicalMap1);
        logicalMap2 = Bidi.invertMap(visualMap1);

        /* get them from the levels array, too */
        levels = bidi.getLevels();

        if (levels == null || levels.length != length) {
            errln("getLevels() in test " + testNumber + " failed");
        }

        logicalMap3 = Bidi.reorderLogical(levels);
        visualMap3 = Bidi.reorderVisual(levels);

        /* get the visual map from the runs, too */
        try {
            runCount = bidi.countRuns();
        } catch (IllegalStateException e) {
            errln("countRuns() in test " + testNumber + " failed");
            runCount = 0;
        }

        logln("\n---- " + runCount + " runs");
        visualIndex = 0;
        BidiRun run;
        for (i = 0; i < runCount; ++i) {
            run = bidi.getVisualRun(i);
            if (run == null) {
                errln("null visual run encountered at index " + i +
                      ", in test " + testNumber);
                continue;
            }
            odd = run.isOddRun();
            logicalStart = run.getStart();
            runLength = run.getLength();
            log("(" + (run.isOddRun() ? "R" : "L"));
            log(" @" + run.getStart() + '[' + run.getLength() + "])\n");
            if (!odd) {
                do {    /* LTR */
                    visualMap4[visualIndex++] = logicalStart++;
                } while (--runLength > 0);
            } else {
                logicalStart += runLength;  /* logicalLimit */
                do {    /* RTL */
                    visualMap4[visualIndex++] = --logicalStart;
                } while (--runLength > 0);
            }
        }
        log("\n");

        /* print all the maps */
        logln("logical maps:");
        for (i = 0; i < length; ++i) {
            log(logicalMap1[i] + " ");
        }
        log("\n");
        for (i = 0; i < length; ++i) {
            log(logicalMap2[i] + " ");
        }
        log("\n");
        for (i = 0; i < length; ++i) {
            log(logicalMap3[i] + " ");
        }

        log("\nvisual maps:\n");
        for (i = 0; i < destLength; ++i) {
            log(visualMap1[i] + " ");
        }
        log("\n");
        for (i = 0; i < destLength; ++i) {
            log(visualMap2[i] + " ");
        }
        log("\n");
        for (i = 0; i < length; ++i) {
            log(visualMap3[i] + " ");
        }
        log("\n");
        for (i = 0; i < length; ++i) {
            log(visualMap4[i] + " ");
        }
        log("\n");

        /* check that the indexes are the same between these and Bidi.getLogical/VisualIndex() */
        for (i = 0; i < length; ++i) {
            if (logicalMap1[i] != logicalMap2[i]) {
                errln("Error in tests[" + testNumber + "]: (logicalMap1[" + i +
                      "] == " + logicalMap1[i] + ") != (logicalMap2[" + i +
                      "] == " + logicalMap2[i] + ")");
            }
            if (logicalMap1[i] != logicalMap3[i]) {
                errln("Error in tests[" + testNumber + "]: (logicalMap1[" + i +
                      "] == " + logicalMap1[i] + ") != (logicalMap3[" + i +
                      "] == " + logicalMap3[i] + ")");
            }
            if (visualMap1[i] != visualMap2[i]) {
                errln("Error in tests[" + testNumber + "]: (visualMap1[" + i +
                      "] == " + visualMap1[i] + ") != (visualMap2[" + i +
                      "] == " + visualMap2[i] + ")");
            }
            if (visualMap1[i] != visualMap3[i]) {
                errln("Error in tests[" + testNumber + "]: (visualMap1[" + i +
                      "] == " + visualMap1[i] + ") != (visualMap3[" + i +
                      "] == " + visualMap3[i] + ")");
            }
            if (visualMap1[i] != visualMap4[i]) {
                errln("Error in tests[" + testNumber + "]: (visualMap1[" + i +
                      "] == " + visualMap1[i] + ") != (visualMap4[" + i +
                      "] == " + visualMap4[i] + ")");
            }
            try {
                visualIndex = bidi.getVisualIndex(i);
            } catch (Exception e) {
                errln("Bidi.getVisualIndex(" + i + ") failed in tests[" +
                      testNumber + "]");
            }
            if (logicalMap1[i] != visualIndex) {
                errln("Error in tests[" + testNumber + "]: (logicalMap1[" + i +
                      "] == " + logicalMap1[i] + ") != (Bidi.getVisualIndex(" + i +
                      ") == " + visualIndex + ")");
            }
            try {
                logicalIndex = bidi.getLogicalIndex(i);
            } catch (Exception e) {
                errln("Bidi.getLogicalIndex(" + i + ") failed in tests[" +
                      testNumber + "]");
            }
            if (visualMap1[i] != logicalIndex) {
                errln("Error in tests[" + testNumber + "]: (visualMap1[" + i +
                      "] == " + visualMap1[i] + ") != (Bidi.getLogicalIndex(" + i +
                      ") == " + logicalIndex + ")");
            }
        }
    }

    private String getStringFromDirProps(short[] dirProps) {
        int i;

        if (dirProps == null) {
            return null;
        }
        int length = dirProps.length;
        char[] buffer = new char[length];

        /* this part would have to be modified for UTF-x */
        for (i = 0; i < length; ++i) {
            buffer[i] = charFromDirProp[dirProps[i]];
        }
        return new String(buffer);
    }

    private void doMisc() {
    /* Miscellaneous tests to exercize less popular code paths */
        Bidi bidi = new Bidi(120, 66), bidiLine;

        assertEquals("\nwriteReverse should return an empty string",
                     "", Bidi.writeReverse("", 0));

        bidi.setPara("", Bidi.LTR, null);
        assertEquals("\nwriteReordered should return an empty string",
                     "", bidi.writeReordered(0));

        bidi.setPara("abc", Bidi.LTR, null);
        assertEquals("\ngetRunStart should return 0",
                     0, bidi.getRunStart(0));
        assertEquals("\ngetRunLimit should return 3",
                     3, bidi.getRunLimit(0));

        bidi.setPara("abc          ", Bidi.RTL, null);
        bidiLine = bidi.setLine(0, 6);
        for (int i = 3; i < 6; i++) {
            assertEquals("\nTrailing space at " + i + " should get paragraph level",
                         Bidi.RTL, bidiLine.getLevelAt(i));
        }

        bidi.setPara("abc       def", Bidi.RTL, null);
        bidiLine = bidi.setLine(0, 6);
        for (int i = 3; i < 6; i++) {
            assertEquals("\nTrailing space at " + i + " should get paragraph level",
                         Bidi.RTL, bidiLine.getLevelAt(i));
        }

        bidi.setPara("abcdefghi    ", Bidi.RTL, null);
        bidiLine = bidi.setLine(0, 6);
        for (int i = 3; i < 6; i++) {
            assertEquals("\nTrailing char at " + i + " should get level 2",
                         2, bidiLine.getLevelAt(i));
        }

        bidi.setReorderingOptions(Bidi.OPTION_REMOVE_CONTROLS);
        bidi.setPara("\u200eabc       def", Bidi.RTL, null);
        bidiLine = bidi.setLine(0, 6);
        assertEquals("\nWrong result length", 5, bidiLine.getResultLength());

        bidi.setPara("abcdefghi", Bidi.LTR, null);
        bidiLine = bidi.setLine(0, 6);
        assertEquals("\nWrong direction #1", Bidi.LTR, bidiLine.getDirection());

        bidi.setPara("", Bidi.LTR, null);
        byte[] levels = bidi.getLevels();
        assertEquals("\nWrong number of level elements", 0, levels.length);
        assertEquals("\nWrong number of runs #1", 0, bidi.countRuns());

        bidi.setPara("          ", Bidi.RTL, null);
        bidiLine = bidi.setLine(0, 6);
        assertEquals("\nWrong number of runs #2", 1, bidiLine.countRuns());

        bidi.setPara("a\u05d0        bc", Bidi.RTL, null);
        bidiLine = bidi.setLine(0, 6);
        assertEquals("\nWrong direction #2", Bidi.MIXED, bidi.getDirection());
        assertEquals("\nWrong direction #3", Bidi.MIXED, bidiLine.getDirection());
        assertEquals("\nWrong number of runs #3", 2, bidiLine.countRuns());

        int[] map = Bidi.reorderLogical(null);
        assertTrue("\nWe should have got a null map #1", map == null);
        map = Bidi.reorderLogical(new byte[] {0,126, 127});
        assertTrue("\nWe should have got a null map #2", map == null);
        map = Bidi.reorderVisual(null);
        assertTrue("\nWe should have got a null map #3", map == null);
        map = Bidi.reorderVisual(new byte[] {0, -1, 4});
        assertTrue("\nWe should have got a null map #4", map == null);

        map = Bidi.invertMap(null);
        assertTrue("\nWe should have got a null map #5", map == null);
        map = Bidi.invertMap(new int[] {0,1,-1,5,4});
        assertTrue("\nUnexpected inverted Map",
                   Arrays.equals(map, new int[] {0,1,-1,-1,4,3}));

        bidi.setPara("", Bidi.LTR, null);
        map = bidi.getLogicalMap();
        assertTrue("\nMap should have length==0 #1", map.length == 0);
        map = bidi.getVisualMap();
        assertTrue("\nMap should have length==0 #2", map.length == 0);

        /* test BidiRun.toString and allocation of run memory > 1 */
        bidi.setPara("abc", Bidi.LTR, null);
        assertEquals("\nWrong run display", "BidiRun 0 - 3 @ 0",
                     bidi.getLogicalRun(0).toString());

        /* test REMOVE_BIDI_CONTROLS together with DO_MIRRORING */
        bidi.setPara("abc\u200e", Bidi.LTR, null);
        String out = bidi.writeReordered(Bidi.REMOVE_BIDI_CONTROLS | Bidi.DO_MIRRORING);
        assertEquals("\nWrong result #1", "abc", out);

        /* test inverse Bidi with marks and contextual orientation */
        bidi.setReorderingMode(Bidi.REORDER_INVERSE_LIKE_DIRECT);
        bidi.setReorderingOptions(Bidi.OPTION_INSERT_MARKS);
        bidi.setPara("", Bidi.LEVEL_DEFAULT_RTL, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #2", "", out);
        bidi.setPara("   ", Bidi.LEVEL_DEFAULT_RTL, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #3", "   ", out);
        bidi.setPara("abc", Bidi.LEVEL_DEFAULT_RTL, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #4", "abc", out);
        bidi.setPara("\u05d0\u05d1", Bidi.LEVEL_DEFAULT_RTL, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #5", "\u05d1\u05d0", out);
        bidi.setPara("abc \u05d0\u05d1", Bidi.LEVEL_DEFAULT_RTL, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #6", "\u05d1\u05d0 abc", out);
        bidi.setPara("\u05d0\u05d1 abc", Bidi.LEVEL_DEFAULT_RTL, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #7", "\u200fabc \u05d1\u05d0", out);
        bidi.setPara("\u05d0\u05d1 abc .-=", Bidi.LEVEL_DEFAULT_RTL, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #8", "\u200f=-. abc \u05d1\u05d0", out);
        bidi.orderParagraphsLTR(true);
        bidi.setPara("\n\r   \n\rabc\n\u05d0\u05d1\rabc \u05d2\u05d3\n\r" +
                     "\u05d4\u05d5 abc\n\u05d6\u05d7 abc .-=\r\n" +
                     "-* \u05d8\u05d9 abc .-=", Bidi.LEVEL_DEFAULT_RTL, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #9",
                     "\n\r   \n\rabc\n\u05d1\u05d0\r\u05d3\u05d2 abc\n\r" +
                     "\u200fabc \u05d5\u05d4\n\u200f=-. abc \u05d7\u05d6\r\n" +
                     "\u200f=-. abc \u05d9\u05d8 *-", out);

        bidi.setPara("\u05d0 \t", Bidi.LTR, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #10", "\u05D0\u200e \t", out);
        bidi.setPara("\u05d0 123 \t\u05d1 123 \u05d2", Bidi.LTR, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #11", "\u05d0 \u200e123\u200e \t\u05d2 123 \u05d1", out);
        bidi.setPara("\u05d0 123 \u0660\u0661 ab", Bidi.LTR, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #12", "\u05d0 \u200e123 \u200e\u0660\u0661 ab", out);
        bidi.setPara("ab \t", Bidi.RTL, null);
        out = bidi.writeReordered(0);
        assertEquals("\nWrong result #13", "\u200f\t ab", out);

        /* check exceeding para level */
        bidi = new Bidi();
        bidi.setPara("A\u202a\u05d0\u202aC\u202c\u05d1\u202cE", (byte)(Bidi.MAX_EXPLICIT_LEVEL - 1), null);
        assertEquals("\nWrong level at index 2", Bidi.MAX_EXPLICIT_LEVEL, bidi.getLevelAt(2));

        /* check 1-char runs with RUNS_ONLY */
        bidi.setReorderingMode(Bidi.REORDER_RUNS_ONLY);
        bidi.setPara("a \u05d0 b \u05d1 c \u05d2 d ", Bidi.LTR, null);
        assertEquals("\nWrong number of runs #4", 14, bidi.countRuns());
        
        /* test testGetBaseDirection to verify fast string direction detection function */
        /* mixed start with L */
        String mixedEnglishFirst = "\u0061\u0627\u0032\u06f3\u0061\u0034";
        assertEquals("\nWrong direction through fast detection #1", Bidi.LTR, Bidi.getBaseDirection(mixedEnglishFirst));
        /* mixed start with AL */
        String mixedArabicFirst = "\u0661\u0627\u0662\u06f3\u0061\u0664";
        assertEquals("\nWrong direction through fast detection #2", Bidi.RTL, Bidi.getBaseDirection(mixedArabicFirst));
        /* mixed Start with R */
        String mixedHebrewFirst = "\u05EA\u0627\u0662\u06f3\u0061\u0664";
        assertEquals("\nWrong direction through fast detection #3", Bidi.RTL, Bidi.getBaseDirection(mixedHebrewFirst));
        /* all AL (Arabic. Persian) */
        String persian = "\u0698\u067E\u0686\u06AF";
        assertEquals("\nWrong direction through fast detection #4", Bidi.RTL, Bidi.getBaseDirection(persian));
        /* all R (Hebrew etc.) */
        String hebrew = "\u0590\u05D5\u05EA\u05F1";
        assertEquals("\nWrong direction through fast detection #5", Bidi.RTL, Bidi.getBaseDirection(hebrew));
        /* all L (English) */
        String english = "\u0071\u0061\u0066";
        assertEquals("\nWrong direction through fast detection #6", Bidi.LTR, Bidi.getBaseDirection(english));
        /* mixed start with weak AL an then L */
        String startWeakAL = "\u0663\u0071\u0061\u0066";
        assertEquals("\nWrong direction through fast detection #7", Bidi.LTR, Bidi.getBaseDirection(startWeakAL));
        /* mixed start with weak L and then AL */
        String startWeakL = "\u0031\u0698\u067E\u0686\u06AF";
        assertEquals("\nWrong direction through fast detection #8", Bidi.RTL, Bidi.getBaseDirection(startWeakL));
        /* empty */
        String empty = "";
        assertEquals("\nWrong direction through fast detection #9", Bidi.NEUTRAL, Bidi.getBaseDirection(empty));
        /* surrogate character */
        String surrogateChar = "\uD800\uDC00";
        assertEquals("\nWrong direction through fast detection #10", Bidi.LTR, Bidi.getBaseDirection(surrogateChar));
        /* all weak L (English digits) */
        String allEnglishDigits = "\u0031\u0032\u0033";
        assertEquals("\nWrong direction through fast detection #11", Bidi.NEUTRAL, Bidi.getBaseDirection(allEnglishDigits));
        /* all weak AL (Arabic digits) */
        String allArabicDigits = "\u0663\u0664\u0665";
        assertEquals("\nWrong direction through fast detection #12", Bidi.NEUTRAL, Bidi.getBaseDirection(allArabicDigits));
        /* null string */
        String nullString = null;
        assertEquals("\nWrong direction through fast detection #13", Bidi.NEUTRAL, Bidi.getBaseDirection(nullString));   
        /* first L (English) others are R (Hebrew etc.) */
        String startEnglishOthersHebrew = "\u0071\u0590\u05D5\u05EA\u05F1";
        assertEquals("\nWrong direction through fast detection #14", Bidi.LTR, Bidi.getBaseDirection(startEnglishOthersHebrew));
        /* last R (Hebrew etc.) others are weak L (English Digits) */
        String lastHebrewOthersEnglishDigit = "\u0031\u0032\u0033\u05F1";
        assertEquals("\nWrong direction through fast detection #15", Bidi.RTL, Bidi.getBaseDirection(lastHebrewOthersEnglishDigit));
    }
}
