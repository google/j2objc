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

import android.icu.text.Bidi;


/**
 * Regression test for the UBA implementation.
 *
 * ported from C by Lina Kemmel, Matitiahu Allouche
 */

public class TestReorder extends BidiFmwk {

    private static final String[] logicalOrder = {
        "del(KC)add(K.C.&)",
        "del(QDVT) add(BVDL)",
        "del(PQ)add(R.S.)T)U.&",
        "del(LV)add(L.V.) L.V.&",
        "day  0  R  DPDHRVR dayabbr",
        "day  1  H  DPHPDHDA dayabbr",
        "day  2   L  DPBLENDA dayabbr",
        "day  3  J  DPJQVM  dayabbr",
        "day  4   I  DPIQNF    dayabbr",
        "day  5  M  DPMEG  dayabbr",
        "helloDPMEG",
        "hello WXYZ"
    };

    private static final String[] visualOrder = {
        "del(CK)add(&.C.K)",
        "del(TVDQ) add(LDVB)",
        "del(QP)add(S.R.)&.U(T",        /* updated for Unicode 6.3 matching brackets */
        "del(VL)add(V.L.) &.V.L",       /* updated for Unicode 6.3 matching brackets */
        "day  0  RVRHDPD  R dayabbr",
        "day  1  ADHDPHPD  H dayabbr",
        "day  2   ADNELBPD  L dayabbr",
        "day  3  MVQJPD  J  dayabbr",
        "day  4   FNQIPD  I    dayabbr",
        "day  5  GEMPD  M  dayabbr",
        "helloGEMPD",
        "hello ZYXW"
    };

    private static final String[] visualOrder1 = {
        ")K.C.&(dda)KC(led",
        ")BVDL(dda )QDVT(led",
        "T(U.&).R.S(dda)PQ(led",        /* updated for Unicode 6.3 matching brackets */
        "L.V.& ).L.V(dda)LV(led",       /* updated for Unicode 6.3 matching brackets */
        "rbbayad R  DPDHRVR  0  yad",
        "rbbayad H  DPHPDHDA  1  yad",
        "rbbayad L  DPBLENDA   2  yad",
        "rbbayad  J  DPJQVM  3  yad",
        "rbbayad    I  DPIQNF   4  yad",
        "rbbayad  M  DPMEG  5  yad",
        "DPMEGolleh",
        "WXYZ olleh"
    };

    private static final String[] visualOrder2 = {
        "@)@K.C.&@(dda)@KC@(led",
        "@)@BVDL@(dda )@QDVT@(led",
        "R.S.)T)U.&@(dda)@PQ@(led",
        "L.V.) L.V.&@(dda)@LV@(led",
        "rbbayad @R  DPDHRVR@  0  yad",
        "rbbayad @H  DPHPDHDA@  1  yad",
        "rbbayad @L  DPBLENDA@   2  yad",
        "rbbayad  @J  DPJQVM@  3  yad",
        "rbbayad    @I  DPIQNF@   4  yad",
        "rbbayad  @M  DPMEG@  5  yad",
        "DPMEGolleh",
        "WXYZ@ olleh"
    };

    private static final String[] visualOrder3 = {
        ")K.C.&(KC)dda(led",
        ")BVDL(ddaQDVT) (led",
        "R.S.)T)U.&(PQ)dda(led",
        "L.V.) L.V.&(LV)dda(led",
        "rbbayad DPDHRVR   R  0 yad",
        "rbbayad DPHPDHDA   H  1 yad",
        "rbbayad DPBLENDA     L 2 yad",
        "rbbayad  DPJQVM   J  3 yad",
        "rbbayad    DPIQNF     I 4 yad",
        "rbbayad  DPMEG   M  5 yad",
        "DPMEGolleh",
        "WXYZ olleh"
    };

    private static final String[] visualOrder4 = {
        "del(add(CK(.C.K)",
        "del( (TVDQadd(LDVB)",
        "del(add(QP(.U(T(.S.R",
        "del(add(VL(.V.L (.V.L",
        "day 0  R   RVRHDPD dayabbr",
        "day 1  H   ADHDPHPD dayabbr",
        "day 2 L     ADNELBPD dayabbr",
        "day 3  J   MVQJPD  dayabbr",
        "day 4 I     FNQIPD    dayabbr",
        "day 5  M   GEMPD  dayabbr",
        "helloGEMPD",
        "hello ZYXW"
    };

    @Test
    public void testReorder() {
        Bidi bidi = new Bidi();
        int testNumber;
        int nTests = logicalOrder.length;
        String src, srcU16, dest = "";

        logln("\nEntering TestReorder\n");

        for (testNumber = 0; testNumber < nTests; testNumber++) {
            logln("Testing L2V #1 for case " + testNumber);
            src = logicalOrder[testNumber];
            srcU16 = pseudoToU16(src);
            try {
                bidi.setPara(srcU16, Bidi.LEVEL_DEFAULT_LTR, null);
            } catch (Exception e) {
                errln("Bidi.setPara(tests[" + testNumber + "], paraLevel " +
                      Bidi.LEVEL_DEFAULT_LTR + " failed.");
            }
            try {
                dest = u16ToPseudo(bidi.writeReordered(Bidi.DO_MIRRORING));
            } catch (Exception e) {
                errln("Bidi.writeReordered(tests[" + testNumber + "], paraLevel " +
                      Bidi.LEVEL_DEFAULT_LTR + " failed.");
            }
            if (!visualOrder[testNumber].equals(dest)) {
                assertEquals("Failure #1 in Bidi.writeReordered(), test number " +
                             testNumber, visualOrder[testNumber], dest, src, null,
                             "Bidi.DO_MIRRORING", "Bidi.LEVEL_DEFAULT_LTR");
            }
            checkWhatYouCan(bidi, src, dest);
        }

        for (testNumber = 0; testNumber < nTests; testNumber++) {
            logln("Testing L2V #2 for case " + testNumber);
            src = logicalOrder[testNumber];
            srcU16 = pseudoToU16(src);
            try {
                bidi.setPara(srcU16, Bidi.LEVEL_DEFAULT_LTR, null);
            } catch (Exception e) {
                errln("Bidi.setPara(tests[" + testNumber + "], paraLevel " +
                      Bidi.LEVEL_DEFAULT_LTR + " failed.");
            }
            try {
                dest = u16ToPseudo(bidi.writeReordered(Bidi.DO_MIRRORING +
                                                       Bidi.OUTPUT_REVERSE));
            } catch (Exception e) {
                errln("Bidi.writeReordered(test[" + testNumber + "], paraLevel "
                        + Bidi.LEVEL_DEFAULT_LTR + " failed.");
            }
            assertEquals("Failure #2 in Bidi.writeReordered() at index " +
                         testNumber, visualOrder1[testNumber], dest,
                         logicalOrder[testNumber], null,
                         "DO_MIRRORING + OUTPUT_REVERSE",
                         "Bidi.LEVEL_DEFAULT_LTR");
        }

        for (testNumber = 0; testNumber < nTests; testNumber++) {
            logln("Testing V2L #3 for case " + testNumber);
            src = logicalOrder[testNumber];
            srcU16 = pseudoToU16(src);
            bidi.setInverse(true);
            try {
                bidi.setPara(srcU16, Bidi.LEVEL_DEFAULT_LTR, null);
            } catch (Exception e) {
                errln("Bidi.setPara(tests[" + testNumber + "], paraLevel " +
                      Bidi.LEVEL_DEFAULT_LTR + " failed.");
            }
            try {
                dest = u16ToPseudo(bidi.writeReordered(Bidi.OUTPUT_REVERSE |
                                                       Bidi.INSERT_LRM_FOR_NUMERIC));
            } catch (Exception e) {
                errln("Bidi.writeReordered(test[" + testNumber + "], paraLevel " +
                      Bidi.LEVEL_DEFAULT_LTR + " failed.");
            }
            assertEquals("Failure #3 in Bidi.writeReordered(test[" + testNumber +
                         "])", visualOrder2[testNumber], dest,
                         logicalOrder[testNumber], null,
                         "INSERT_LRM_FOR_NUMERIC + OUTPUT_REVERSE",
                         "Bidi.LEVEL_DEFAULT_LTR");
        }

        /* Max Explicit level */
        for (testNumber = 0; testNumber < nTests; testNumber++) {
            logln("Testing V2L #4 for case " + testNumber);
            src = logicalOrder[testNumber];
            srcU16 = pseudoToU16(src);
            byte[] levels = new byte[Bidi.MAX_EXPLICIT_LEVEL];
            for (int i = 0; i < 10; i++) {
                levels[i] = (byte)(i + 1);
            }
            try {
                bidi.setPara(srcU16, Bidi.LEVEL_DEFAULT_LTR, levels);
            } catch (Exception e) {
                errln("Bidi.setPara(tests[" + testNumber +
                      "], paraLevel = MAX_EXPLICIT_LEVEL = " +
                      Bidi.MAX_EXPLICIT_LEVEL + " failed.");
            }
            try {
                dest = u16ToPseudo(bidi.writeReordered(Bidi.OUTPUT_REVERSE));
            } catch (Exception e) {
                errln("Bidi.writeReordered(test[" + testNumber + "], paraLevel " +
                      Bidi.LEVEL_DEFAULT_LTR + " failed.");
            }
            assertEquals("Failure #4 in Bidi.writeReordered(test[" + testNumber +
                         "])", visualOrder3[testNumber], dest,
                         logicalOrder[testNumber], null,
                         "OUTPUT_REVERSE", "Bidi.LEVEL_DEFAULT_LTR");
        }

        for (testNumber = 0; testNumber < nTests; testNumber++) {
            logln("Testing V2L #5 for case " + testNumber);
            src = logicalOrder[testNumber];
            srcU16 = pseudoToU16(src);
            byte[] levels = new byte[Bidi.MAX_EXPLICIT_LEVEL];
            for (int i = 0; i < 10; i++) {
                levels[i] = (byte)(i + 1);
            }
            try {
                bidi.setPara(srcU16, Bidi.LEVEL_DEFAULT_LTR, levels);
            } catch (Exception e) {
                errln("Bidi.setPara(tests[" + testNumber + "], paraLevel " +
                      Bidi.MAX_EXPLICIT_LEVEL + " failed.");
            }
            try {
                dest = u16ToPseudo(bidi.writeReordered(Bidi.DO_MIRRORING |
                                                       Bidi.REMOVE_BIDI_CONTROLS));
            } catch (Exception e) {
                errln("Bidi.writeReordered(test[" + testNumber + "], paraLevel "
                        + Bidi.LEVEL_DEFAULT_LTR + " failed.");
            }
            assertEquals("Failure #5 in Bidi.writeReordered(test[" + testNumber +
                         "])", visualOrder4[testNumber], dest,
                         logicalOrder[testNumber], null,
                         "DO_MIRRORING + REMOVE_BIDI_CONTROLS",
                         "Bidi.LEVEL_DEFAULT_LTR");
        }

        logln("\nExiting TestReorder\n");
    }
}
