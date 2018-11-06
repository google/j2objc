/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2008-2010, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.bidi;

import java.util.Arrays;

import org.junit.Test;

import android.icu.impl.Utility;
import android.icu.text.Bidi;
import android.icu.text.BidiRun;

/**
 * Regression test for Bidi multiple paragraphs
 *
 * @author Lina Kemmel, Matitiahu Allouche
 */

public class TestMultipleParagraphs extends BidiFmwk {

    private static final String text =
        "__ABC\u001c"                  /* Para #0 offset 0 */
        + "__\u05d0DE\u001c"           /*       1        6 */
        + "__123\u001c"                /*       2       12 */
        + "\r\n"                       /*       3       18 */
        + "FG\r"                       /*       4       20 */
        + "\r"                         /*       5       23 */
        + "HI\r\n"                     /*       6       24 */
        + "\r\n"                       /*       7       28 */
        + "\n"                         /*       8       30 */
        + "\n"                         /*       9       31 */
        + "JK\u001c";                  /*      10       32 */
    private static final int paraCount = 11;
    private static final int[] paraBounds = {
        0, 6, 12, 18, 20, 23, 24, 28, 30, 31, 32, 35
    };
    private static final byte[] paraLevels = {
        Bidi.LTR, Bidi.RTL, Bidi.LEVEL_DEFAULT_LTR, Bidi.LEVEL_DEFAULT_RTL, 22, 23
    };
    private static final byte[][] multiLevels = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0},
        {22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22},
        {23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23}
    };
    private static final String text2 = "\u05d0 1-2\u001c\u0630 1-2\u001c1-2";
    private static final byte[] levels2 = {
        1, 1, 2, 2, 2, 0, 1, 1, 2, 1, 2, 0, 2, 2, 2
    };
    private static final char[] multiparaTestString = {
        0x5de, 0x5e0, 0x5e1, 0x5d4, 0x20,  0x5e1, 0x5e4, 0x5da,
        0x20,  0xa,   0xa,   0x41,  0x72,  0x74,  0x69,  0x73,
        0x74,  0x3a,  0x20,  0x5de, 0x5e0, 0x5e1, 0x5d4, 0x20,
        0x5e1, 0x5e4, 0x5da, 0x20,  0xa,   0xa,   0x41,  0x6c,
        0x62,  0x75,  0x6d,  0x3a,  0x20,  0x5de, 0x5e0, 0x5e1,
        0x5d4, 0x20,  0x5e1, 0x5e4, 0x5da, 0x20,  0xa,   0xa,
        0x54,  0x69,  0x6d,  0x65,  0x3a,  0x20,  0x32,  0x3a,
        0x32,  0x37,  0xa,  0xa
    };
    private static final byte[] multiparaTestLevels = {
        1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 1, 1, 1, 1, 1,
        1, 1, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 1, 1, 1,
        1, 1, 1, 1, 1, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0
    };

    @Test
    public void testMultipleParagraphs()
    {
        byte gotLevel;
        byte[] gotLevels;
        boolean orderParagraphsLTR;
        String src;
        Bidi bidi = new Bidi();
        Bidi bidiLine;
        int count, paraStart, paraLimit, paraIndex, length;
        int i, j, k;

        logln("\nEntering TestMultipleParagraphs\n");
        try {
            bidi.setPara(text, Bidi.LTR, null);
        } catch (IllegalArgumentException e) {
            errln("1st Bidi.setPara failed, paraLevel = " + Bidi.LTR);
        }

        /* check paragraph count and boundaries */
        if (paraCount != (count = bidi.countParagraphs())) {
            errln("1st Bidi.countParagraphs returned " + count + ", should be " +
                  paraCount);
        }
        BidiRun run;
        for (i = 0; i < paraCount; i++) {
            run = bidi.getParagraphByIndex(i);
            paraStart = run.getStart();
            paraLimit = run.getLimit();
            if ((paraStart != paraBounds[i]) ||
                (paraLimit != paraBounds[i + 1])) {
                errln("Found boundaries of paragraph " + i + ": " +
                      paraStart + "-" + paraLimit + "; expected: " +
                      paraBounds[i] + "-" + paraBounds[i + 1]);
            }
        }

        /* check with last paragraph not terminated by B */
        char[] chars = text.toCharArray();
        chars[chars.length - 1] = 'L';
        src = new String(chars);
        try {
            bidi.setPara(src, Bidi.LTR, null);
        } catch (IllegalArgumentException e) {
            errln("2nd Bidi.setPara failed, paraLevel = " + Bidi.LTR);
        }
        if (paraCount != (count = bidi.countParagraphs())) {
            errln("2nd Bidi.countParagraphs returned " + count +
                  ", should be " + paraCount);
        }
        i = paraCount - 1;
        run = bidi.getParagraphByIndex(i);
        paraStart = run.getStart();
        paraLimit = run.getLimit();
        if ((paraStart != paraBounds[i]) ||
            (paraLimit != paraBounds[i + 1])) {
            errln("2nd Found boundaries of paragraph " + i + ": " +
                  paraStart + "-" + paraLimit + "; expected: " +
                  paraBounds[i] + "-" + paraBounds[i + 1]);
        }

        /* check paraLevel for all paragraphs under various paraLevel specs */
        for (k = 0; k < 6; k++) {
            try {
                bidi.setPara(src, paraLevels[k], null);
            } catch (IllegalArgumentException e) {
                errln("3nd Bidi.setPara failed, paraLevel = " + paraLevels[k]);
            }
            for (i = 0; i < paraCount; i++) {
                paraIndex = bidi.getParagraphIndex(paraBounds[i]);
                run = bidi.getParagraph(paraBounds[i]);
                if (paraIndex != i) {
                    errln("#1 For paraLevel = " + paraLevels[k] +
                          " paragraph = " + i + ", found paragraph" +
                          " index = " + paraIndex + " expected = " + i);
                }
                gotLevel = run.getEmbeddingLevel();
                if (gotLevel != multiLevels[k][i]) {
                    errln("#2 For paraLevel = " + paraLevels[k] +
                          " paragraph = " + i + ", found level = " + gotLevel +
                          ", expected = " + multiLevels[k][i]);
                }
            }
            gotLevel = bidi.getParaLevel();
            if (gotLevel != multiLevels[k][0]) {
                errln("#3 For paraLevel = " + paraLevels[k] +
                      " getParaLevel = " + gotLevel + ", expected " +
                      multiLevels[k][0]);
            }
        }

        /* check that the result of Bidi.getParaLevel changes if the first
         * paragraph has a different level
         */
        chars[0] = '\u05d2';            /* Hebrew letter Gimel */
        src = new String(chars);
        try {
            bidi.setPara(src, Bidi.LEVEL_DEFAULT_LTR, null);
        } catch (IllegalArgumentException e) {
            errln("Bidi.setPara failed, paraLevel = " + Bidi.LEVEL_DEFAULT_LTR);
        }
        gotLevel = bidi.getParaLevel();
        if (gotLevel != Bidi.RTL) {
            errln("#4 For paraLevel = Bidi.LEVEL_DEFAULT_LTR getParaLevel = " +
                  gotLevel + ", expected = " + Bidi.RTL);
        }

        /* check that line cannot overlap paragraph boundaries */
        bidiLine = new Bidi();
        i = paraBounds[1];
        k = paraBounds[2] + 1;
        try {
            bidiLine = bidi.setLine(i, k);
            errln("For line limits " + i + "-" + k
                    + " got success, while expected failure");
        } catch (Exception e) {}

        i = paraBounds[1];
        k = paraBounds[2];
        try {
            bidiLine = bidi.setLine(i, k);
        } catch (Exception e) {
            errln("For line limits " + i + "-" + k + " got failure");
        }

        /* check level of block separator at end of paragraph when orderParagraphsLTR==FALSE */
        try {
            bidi.setPara(src, Bidi.RTL, null);
        } catch (IllegalArgumentException e) {
            errln("Bidi.setPara failed, paraLevel = " + Bidi.RTL);
        }
        /* get levels through para Bidi block */
        try {
            gotLevels = bidi.getLevels();
        } catch (Exception e) {
            errln("Error on Bidi.getLevels");
            gotLevels = new byte[bidi.getLength()];
            Arrays.fill(gotLevels, (byte)-1);
        }
        for (i = 26; i < 32; i++) {
            if (gotLevels[i] != Bidi.RTL) {
                errln("For char " + i + "(0x" + Utility.hex(chars[i]) +
                      "), level = " + gotLevels[i] + ", expected = " + Bidi.RTL);
            }
        }
        /* get levels through para Line block */
        i = paraBounds[1];
        k = paraBounds[2];
        try {
            bidiLine = bidi.setLine(i, k);
        } catch (Exception e) {
            errln("For line limits " + i + "-" + k + " got failure");
            return;
        }
        paraIndex = bidiLine.getParagraphIndex(i);
        run = bidiLine.getParagraph(i);
        try {
            gotLevels = bidiLine.getLevels();
        } catch (Exception e) {
            errln("Error on bidiLine.getLevels");
            gotLevels = new byte[bidiLine.getLength()];
            Arrays.fill(gotLevels, (byte)-1);
        }
        length = bidiLine.getLength();
        gotLevel = run.getEmbeddingLevel();
        if ((gotLevel != Bidi.RTL) || (gotLevels[length - 1] != Bidi.RTL)) {
            errln("For paragraph " + paraIndex + " with limits " +
                  run.getStart() + "-" + run.getLimit() +
                  ", paraLevel = " + gotLevel +
                  "expected = " + Bidi.RTL +
                  ", level of separator = " + gotLevels[length - 1] +
                  " expected = " + Bidi.RTL);
        }
        orderParagraphsLTR = bidi.isOrderParagraphsLTR();
        assertFalse("orderParagraphsLTR is true", orderParagraphsLTR);
        bidi.orderParagraphsLTR(true);
        orderParagraphsLTR = bidi.isOrderParagraphsLTR();
        assertTrue("orderParagraphsLTR is false", orderParagraphsLTR);

        /* check level of block separator at end of paragraph when orderParagraphsLTR==TRUE */
        try {
            bidi.setPara(src, Bidi.RTL, null);
        } catch (IllegalArgumentException e) {
            errln("Bidi.setPara failed, paraLevel = " + Bidi.RTL);
        }
        /* get levels through para Bidi block */
        try {
            gotLevels = bidi.getLevels();
        } catch (Exception e) {
            errln("Error on Bidi.getLevels");
            gotLevels = new byte[bidi.getLength()];
            Arrays.fill(gotLevels, (byte)-1);
        }
        for (i = 26; i < 32; i++) {
            if (gotLevels[i] != 0) {
                errln("For char " + i + "(0x" + Utility.hex(chars[i]) +
                      "), level = "+ gotLevels[i] + ", expected = 0");
            }
        }
        /* get levels through para Line block */
        i = paraBounds[1];
        k = paraBounds[2];
        paraStart = run.getStart();
        paraLimit = run.getLimit();
        try {
            bidiLine = bidi.setLine(paraStart, paraLimit);
        } catch (Exception e) {
            errln("For line limits " + paraStart + "-" + paraLimit +
                  " got failure");
        }
        paraIndex = bidiLine.getParagraphIndex(i);
        run = bidiLine.getParagraph(i);
        try {
            gotLevels = bidiLine.getLevels();
        } catch (Exception e) {
            errln("Error on bidiLine.getLevels");
            gotLevels = new byte[bidiLine.getLength()];
            Arrays.fill(gotLevels, (byte)-1);
        }
        length = bidiLine.getLength();
        gotLevel = run.getEmbeddingLevel();
        if ((gotLevel != Bidi.RTL) || (gotLevels[length - 1] != 0)) {
            err("\nFor paragraph " + paraIndex + " with limits " +
                run.getStart() + "-" + run.getLimit() +
                ", paraLevel = " + gotLevel + "expected = " + Bidi.RTL +
                ", level of separator = " + gotLevels[length - 1] +
                " expected = 0\nlevels = ");
            for (count = 0; count < length; count++) {
                errcont(gotLevels[count] + "  ");
            }
            errcont("\n");
        }

        /* test that the concatenation of separate invocations of the bidi code
         * on each individual paragraph in order matches the levels array that
         * results from invoking bidi once over the entire multiparagraph tests
         * (with orderParagraphsLTR false, of course)
         */
        src = text;                     /* restore original content */
        bidi.orderParagraphsLTR(false);
        try {
            bidi.setPara(src, Bidi.LEVEL_DEFAULT_RTL, null);
        } catch (IllegalArgumentException e) {
            errln("Bidi.setPara failed, paraLevel = " + Bidi.LEVEL_DEFAULT_RTL);
        }
        try {
            gotLevels = bidi.getLevels();
        } catch (Exception e) {
            errln("Error on bidiLine.getLevels");
            gotLevels = new byte[bidi.getLength()];
            Arrays.fill(gotLevels, (byte)-1);
        }
        for (i = 0; i < paraCount; i++) {
            /* use pLine for individual paragraphs */
            paraStart = paraBounds[i];
            length = paraBounds[i + 1] - paraStart;
            try {
                bidiLine.setPara(src.substring(paraStart, paraStart + length),
                                 Bidi.LEVEL_DEFAULT_RTL, null);
            } catch (IllegalArgumentException e) {
                errln("Bidi.setPara failed, paraLevel = " + Bidi.LEVEL_DEFAULT_RTL);
            }
            for (j = 0; j < length; j++) {
                if ((k = bidiLine.getLevelAt(j)) !=
                        (gotLevel = gotLevels[paraStart + j])) {
                    errln("Checking paragraph concatenation: for paragraph[" +
                          i + "], char[" + j + "] = 0x" +
                          Utility.hex(src.charAt(paraStart + j)) +
                          ", level = " + k + ", expected = " + gotLevel);
                }
            }
        }

        /* ensure that leading numerics in a paragraph are not treated as arabic
           numerals because of arabic text in a preceding paragraph
         */
        src = text2;
        bidi.orderParagraphsLTR(true);
        try {
            bidi.setPara(src, Bidi.RTL, null);
        } catch (IllegalArgumentException e) {
            errln("Bidi.setPara failed, paraLevel = " + Bidi.RTL);
        }
        try {
            gotLevels = bidi.getLevels();
        } catch (Exception e) {
            errln("Error on Bidi.getLevels");
            gotLevels = new byte[bidi.getLength()];
            Arrays.fill(gotLevels, (byte)-1);
        }
        for (i = 0, length = src.length(); i < length; i++) {
            if (gotLevels[i] != levels2[i]) {
                errln("Checking leading numerics: for char " + i + "(0x" +
                      Utility.hex(src.charAt(i)) + "), level = " +
                      gotLevels[i] + ", expected = " + levels2[i]);
            }
        }

        /* check handling of whitespace before end of paragraph separator when
         * orderParagraphsLTR==TRUE, when last paragraph has, and lacks, a terminating B
         */
        chars = src.toCharArray();
        Arrays.fill(chars, '\u0020');
        bidi.orderParagraphsLTR(true);
        for (i = 0x001c; i <= 0x0020; i += (0x0020-0x001c)) {
            chars[4] = (char)i;         /* with and without terminating B */
            for (j = 0x0041; j <= 0x05d0; j += (0x05d0-0x0041)) {
                chars[0] = (char)j;     /* leading 'A' or Alef */
                src = new String(chars);
                for (gotLevel = 4; gotLevel <= 5; gotLevel++) {
                    /* test even and odd paraLevel */
                    try {
                        bidi.setPara(src, gotLevel, null);
                    } catch (IllegalArgumentException e) {
                        errln("Bidi.setPara failed, paraLevel = " + gotLevel);
                    }
                    try {
                        gotLevels = bidi.getLevels();
                    } catch (Exception e) {
                        errln("Error on Bidi.getLevels");
                        gotLevels = new byte[bidi.getLength()];
                        Arrays.fill(gotLevels, (byte)-1);
                    }
                    for (k = 1; k <= 3; k++) {
                        if (gotLevels[k] != gotLevel) {
                            errln("Checking trailing spaces for leading char 0x" +
                                  Utility.hex(chars[0]) + ", last_char = " +
                                  Utility.hex(chars[4]) + ", index = " + k +
                                  "level = " + gotLevels[k] +
                                  ", expected = " + gotLevel);
                        }
                    }
                }
            }
        }

        /* check default orientation when inverse bidi and paragraph starts
         * with LTR strong char and ends with RTL strong char, with and without
         * a terminating B
         */
        bidi.setReorderingMode(Bidi.REORDER_INVERSE_LIKE_DIRECT);
        bidi.setPara("abc \u05d2\u05d1\n", Bidi.LEVEL_DEFAULT_LTR, null);
        String out = bidi.writeReordered(0);
        assertEquals("\nInvalid output", "\u05d1\u05d2 abc\n", out);
        bidi.setPara("abc \u05d2\u05d1", Bidi.LEVEL_DEFAULT_LTR, null);
        out = bidi.writeReordered(0);
        assertEquals("\nInvalid output #1", "\u05d1\u05d2 abc", out);

        /* check multiple paragraphs together with explicit levels
         */
        bidi.setReorderingMode(Bidi.REORDER_DEFAULT);
        gotLevels = new byte[] {0,0,0,0,0,0,0,0,0,0};
        bidi.setPara("ab\u05d1\u05d2\n\u05d3\u05d4123", Bidi.LTR, gotLevels);
        out = bidi.writeReordered(0);
        assertEquals("\nInvalid output #2", "ab\u05d2\u05d1\n123\u05d4\u05d3", out);
        assertEquals("\nInvalid number of paras", 2, bidi.countParagraphs());

        logln("\nExiting TestMultipleParagraphs\n");

        /* check levels in multiple paragraphs with default para level
         */
        bidi = new Bidi();
        bidi.setPara(multiparaTestString, Bidi.LEVEL_DEFAULT_LTR, null);
        try {
            gotLevels = bidi.getLevels();
        } catch (Exception e) {
            errln("Error on Bidi.getLevels for multiparaTestString");
            return;
        }
        for (i = 0; i < multiparaTestString.length; i++) {
            if (gotLevels[i] != multiparaTestLevels[i]) {
                errln("Error on level for multiparaTestString at index " + i +
                      ", expected=" + multiparaTestLevels[i] +
                      ", actual=" + gotLevels[i]);
            }
        }
    }
}
