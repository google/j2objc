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

import org.junit.Test;

import android.icu.impl.Utility;
import android.icu.text.Bidi;

/**
 * Regression test for streaming mode
 *
 * @author Lina Kemmel, Matitiahu Allouche
 */

public class TestStreaming extends BidiFmwk {

    static final int MAXPORTIONS = 10;

    static class TestCase {
        String textIn;
        int chunk;
        int[] nPortions;
        int[][] portionLens;
        String[] message;

        public TestCase(String in, int ch, int[] np, int[][] lens, String[] msg) {
            this.textIn = in;
            this.chunk = ch;
            this.nPortions = np;
            this.portionLens = lens;
            this.message = msg;
        }
    }

    static final TestCase[] testCases = {
        new TestCase("123\n"    +
                     "abc45\r"  +
                     "67890\n"  +
                     "\r"       +
                     "02468\r"  +
                     "ghi",
            6, new int[] { 6, 6 },
            new int[][] {{ 4, 6, 6, 1, 6, 3}, { 4, 6, 6, 1, 6, 3 }},
            new String[] {"4, 6, 6, 1, 6, 3",  "4, 6, 6, 1, 6, 3"}
        ),
        new TestCase("abcd\nfgh\r12345\n456",
            6, new int[] { 4, 4 },
            new int[][] {{ 5, 4, 6, 3 }, { 5, 4, 6, 3 }},
            new String[] {"5, 4, 6, 3",   "5, 4, 6, 3"}
        ),
        new TestCase("abcd\nfgh\r12345\n45\r",
            6, new int[] { 4, 4 },
            new int[][] {{ 5, 4, 6, 3 }, { 5, 4, 6, 3 }},
            new String[] {"5, 4, 6, 3",   "5, 4, 6, 3"}
        ),
        new TestCase("abcde\nfghi",
            10, new int[] { 2, 2 },
            new int[][] {{ 6, 4 }, { 6, 4 }},
            new String[] {"6, 4",   "6, 4"}
        )
    };
    static final int MAXLOOPS = 20;
    static final byte[] paraLevels = { Bidi.LTR, Bidi.RTL };

    @Test
    public void testStreaming()
    {
        String src, subsrc;
        Bidi bidi;
        int srcLen, processedLen, chunk, len, nPortions, offset;
        int i, j, levelIndex;
        byte level;
        int nTests = testCases.length, nLevels = paraLevels.length;
        boolean mismatch, testOK = true;
        StringBuffer processedLenStr = new StringBuffer(MAXLOOPS * 5);

        logln("\nEntering TestStreaming\n");

        bidi = new Bidi();

        bidi.orderParagraphsLTR(true);

        for (levelIndex = 0; levelIndex < nLevels; levelIndex++) {
            for (i = 0; i < nTests; i++) {
                src = testCases[i].textIn;
                srcLen = src.length();
                chunk = testCases[i].chunk;
                nPortions = testCases[i].nPortions[levelIndex];
                level = paraLevels[levelIndex];
                processedLenStr.setLength(0);
                logln("Testing level " + level + ", case " + i);

                mismatch = false;

                bidi.setReorderingOptions(Bidi.OPTION_STREAMING);
                for (j = 0; j < MAXPORTIONS && srcLen > 0; j++) {
                    len = chunk < srcLen ? chunk : srcLen;
                    offset = src.length() - srcLen;
                    subsrc = src.substring(offset, offset + len);
                    bidi.setPara(subsrc, level, null);

                    processedLen = bidi.getProcessedLength();
                    if (processedLen == 0) {
                        bidi.setReorderingOptions(Bidi.OPTION_DEFAULT);
                        j--;
                        continue;
                    }
                    bidi.setReorderingOptions(Bidi.OPTION_STREAMING);

                    mismatch |= j >= nPortions ||
                               processedLen != testCases[i].portionLens[levelIndex][j];

                    processedLenStr.append(Integer.toString(processedLen) + " ");
                    srcLen -= processedLen;
                }

                if (mismatch || j != nPortions) {
                    testOK = false;
                    errln("\nProcessed lengths mismatch for" +
                          "\n\tParagraph level = " + level +
                          "\n\tInput string: " + Utility.escape(src) +
                          "\n\tChunk = " + chunk +
                          "\n\tActually processed portion lengths: { " +
                                processedLenStr + " }" +
                          "\n\tExpected portion lengths          : { " +
                                testCases[i].message[levelIndex] + " }\n");
                }
            }
        }
        if (testOK) {
            logln("\nBidi streaming test OK");
        }
        logln("\nExiting TestStreaming\n");
    }
}
