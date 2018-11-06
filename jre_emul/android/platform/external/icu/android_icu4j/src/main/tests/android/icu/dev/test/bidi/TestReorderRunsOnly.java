/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2001-2007, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.bidi;

import org.junit.Test;

import android.icu.text.Bidi;

/**
 * Regression test for variants to the UBA.
 *
 * @author Lina Kemmel, Matitiahu Allouche
 */

public class TestReorderRunsOnly extends BidiFmwk {

    static class TestCase {
        String textIn;
        String textOut[][];
        int noroundtrip[];

        TestCase(String in, String[][] out, int[] nrd) {
            this.textIn = in;
            this.textOut = out;
            this.noroundtrip = nrd;
        }
    }

    static final TestCase testCases[] = {
        new TestCase("ab 234 896 de",   // 0
                     new String[][] {{"de 896 ab 234", "de 896 ab 234"},
                                     {"ab 234 @896@ de", "de 896 ab 234"}},
                     new int[] {0, 0}),
        new TestCase("abcGHI",          // 1
                     new String[][] {{"GHIabc", "GHIabc"}, {"GHIabc", "GHIabc"}},
                     new int[] {0, 0}),
        new TestCase("a.>67->",         // 2
                     new String[][] {{"<-67<.a", "<-67<.a"}, {"<-67<.a", "<-67<.a"}},
                     new int[] {0, 0}),
        new TestCase("-=%$123/ *",      // 3
                     new String[][] {{"* /%$123=-", "* /%$123=-"},
                                     {"* /%$123=-", "* /%$123=-"}},
                     new int[] {0, 0}),
        new TestCase("abc->12..>JKL",   // 4
                     new String[][] {{"JKL<..12<-abc", "JKL<..abc->12"},
                                     {"JKL<..12<-abc", "JKL<..abc->12"}},
                     new int[] {0, 0}),
        new TestCase("JKL->12..>abc",   // 5
                     new String[][] {{"abc<..JKL->12", "abc<..12<-JKL"},
                                     {"abc<..JKL->12", "abc<..12<-JKL"}},
                     new int[] {0, 0}),
        new TestCase("123->abc",        // 6
                     new String[][] {{"abc<-123", "abc<-123"},
                                     {"abc&<-123", "abc<-123"}},
                     new int[] {1, 0}),
        new TestCase("123->JKL",        // 7
                     new String[][] {{"JKL<-123", "123->JKL"},
                                     {"JKL<-123", "JKL<-@123"}},
                     new int[] {0, 1}),
        new TestCase("*>12.>34->JKL",   // 8
                     new String[][] {{"JKL<-34<.12<*", "12.>34->JKL<*"},
                                     {"JKL<-34<.12<*", "JKL<-@34<.12<*"}},
                     new int[] {0, 1}),
        new TestCase("*>67.>89->JKL",   // 9
                     new String[][] {{"67.>89->JKL<*", "67.>89->JKL<*"},
                                     {"67.>89->JKL<*", "67.>89->JKL<*"}},
                     new int[] {0, 0}),
        new TestCase("* /abc-=$%123",   // 10
                     new String[][] {{"$%123=-abc/ *", "abc-=$%123/ *"},
                                     {"$%123=-abc/ *", "abc-=$%123/ *"}},
                     new int[] {0, 0}),
        new TestCase("* /$%def-=123",   // 11
                     new String[][] {{"123=-def%$/ *", "def-=123%$/ *"},
                                     {"123=-def%$/ *", "def-=123%$/ *"}},
                     new int[] {0, 0}),
        new TestCase("-=GHI* /123%$",   // 12
                     new String[][] {{"GHI* /123%$=-", "123%$/ *GHI=-"},
                                     {"GHI* /123%$=-", "123%$/ *GHI=-"}},
                     new int[] {0, 0}),
        new TestCase("-=%$JKL* /123",   // 13
                     new String[][] {{"JKL* /%$123=-", "123/ *JKL$%=-"},
                                     {"JKL* /%$123=-", "123/ *JKL$%=-"}},
                     new int[] {0, 0}),
        new TestCase("ab =#CD *?450",   // 14
                     new String[][] {{"CD *?450#= ab", "450?* CD#= ab"},
                                     {"CD *?450#= ab", "450?* CD#= ab"}},
                     new int[] {0, 0}),
        new TestCase("ab 234 896 de",   // 15
                     new String[][] {{"de 896 ab 234", "de 896 ab 234"},
                                     {"ab 234 @896@ de", "de 896 ab 234"}},
                     new int[] {0, 0}),
        new TestCase("abc-=%$LMN* /123",// 16
                     new String[][] {{"LMN* /%$123=-abc", "123/ *LMN$%=-abc"},
                                     {"LMN* /%$123=-abc", "123/ *LMN$%=-abc"}},
                     new int[] {0, 0}),
        new TestCase("123->JKL&MN&P",   // 17
                     new String[][] {{"JKLMNP<-123", "123->JKLMNP"},
                                     {"JKLMNP<-123", "JKLMNP<-@123"}},
                     new int[] {0, 1}),
        new TestCase("123",             // 18   just one run
                     new String[][] {{"123", "123"},
                                     {"123", "123"}},
                     new int[] {0, 0})
    };


    @Test
    public void testReorderRunsOnly() {

        Bidi bidi = new Bidi();
        Bidi bidiL2V = new Bidi();
        String src, dest, visual1, visual2;
        String srcU16, destU16, visual1U16, visual2U16;
        int option, i, j, nCases;
        byte level;

        logln("\nEntering TestReorderRunsOnly\n");
        bidi.setReorderingMode(Bidi.REORDER_RUNS_ONLY);
        bidiL2V.setReorderingOptions(Bidi.OPTION_REMOVE_CONTROLS);

        for (option = 0; option < 2; option++) {
            bidi.setReorderingOptions(option == 0 ? Bidi.OPTION_REMOVE_CONTROLS
                                                  : Bidi.OPTION_INSERT_MARKS);
            for (i = 0, nCases = testCases.length; i < nCases; i++) {
                src = testCases[i].textIn;
                srcU16 = pseudoToU16(src);
                for (j = 0; j < 2; j++) {
                    logln("Now doing test for option " + option +
                          ", case " + i + ", level " + j);
                    level = (byte)j;
                    bidi.setPara(srcU16, level, null);
                    destU16 = bidi.writeReordered(Bidi.DO_MIRRORING);
                    dest = u16ToPseudo(destU16);
                    checkWhatYouCan(bidi, src, dest);
                    assertEquals("Reorder runs only failed for case " + i,
                                 testCases[i].textOut[option][level],
                                 dest, src, null, null, Byte.toString(level));

                    if ((option == 0) && (testCases[i].noroundtrip[level] > 0)) {
                        continue;
                    }
                    bidiL2V.setPara(srcU16, level, null);
                    visual1U16 = bidiL2V.writeReordered(Bidi.DO_MIRRORING);
                    visual1 = u16ToPseudo(visual1U16);
                    checkWhatYouCan(bidiL2V, src, visual1);
                    bidiL2V.setPara(destU16, (byte)(level^1), null);
                    visual2U16 = bidiL2V.writeReordered(Bidi.DO_MIRRORING);
                    visual2 = u16ToPseudo(visual2U16);
                    checkWhatYouCan(bidiL2V, dest, visual2);
                    assertEquals("Round trip failed for case " + i,
                                 visual1, visual2, src,
                                 "REORDER_RUNS_ONLY (2)",
                                 option == 0 ? "0" : "OPTION_INSERT_MARKS",
                                 Byte.toString(level));
                }
            }
        }

        /* test with null or empty text */
        int paras;
        bidi.setPara((String)null, Bidi.LTR, null);
        paras = bidi.countParagraphs();
        assertEquals("\nInvalid number of paras #1 (should be 0): ", 0, paras);
        bidi.setPara((char[])null, Bidi.LTR, null);
        paras = bidi.countParagraphs();
        assertEquals("\nInvalid number of paras #2 (should be 0): ", 0, paras);
        bidi.setPara("", Bidi.LTR, null);
        paras = bidi.countParagraphs();
        assertEquals("\nInvalid number of paras #3 (should be 0): ", 0, paras);
        bidi.setPara(new char[0], Bidi.LTR, null);
        paras = bidi.countParagraphs();
        assertEquals("\nInvalid number of paras #4 (should be 0): ", 0, paras);

        logln("\nExiting TestReorderRunsOnly\n");
    }
}

