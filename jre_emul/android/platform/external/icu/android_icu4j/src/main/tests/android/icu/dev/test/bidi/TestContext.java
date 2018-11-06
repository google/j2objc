/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2007-2011, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.bidi;

import org.junit.Test;

import android.icu.text.Bidi;

/**
 * Regression test for doing transformations in context
 *
 * @author Matitiahu Allouche
 */

public class TestContext extends BidiFmwk {

    private class ContextCase {
        String prologue;
        String source;
        String epilogue;
        String expected;
        byte paraLevel;

        ContextCase(String pro, String src, String epi, String exp, byte lev) {
            prologue = pro;
            source = src;
            epilogue = epi;
            expected = exp;
            paraLevel = lev;
        }
    };

    private final ContextCase[] contextData = {
        /*00*/  new ContextCase("", "", "", "", Bidi.LTR),
        /*01*/  new ContextCase("", ".-=JKL-+*", "", ".-=LKJ-+*", Bidi.LTR),
        /*02*/  new ContextCase(" ", ".-=JKL-+*", " ", ".-=LKJ-+*", Bidi.LTR),
        /*03*/  new ContextCase("a", ".-=JKL-+*", "b", ".-=LKJ-+*", Bidi.LTR),
        /*04*/  new ContextCase("D", ".-=JKL-+*", "", "LKJ=-.-+*", Bidi.LTR),
        /*05*/  new ContextCase("", ".-=JKL-+*", " D", ".-=*+-LKJ", Bidi.LTR),
        /*06*/  new ContextCase("", ".-=JKL-+*", " 2", ".-=*+-LKJ", Bidi.LTR),
        /*07*/  new ContextCase("", ".-=JKL-+*", " 7", ".-=*+-LKJ", Bidi.LTR),
        /*08*/  new ContextCase(" G 1", ".-=JKL-+*", " H", "*+-LKJ=-.", Bidi.LTR),
        /*09*/  new ContextCase("7", ".-=JKL-+*", " H", ".-=*+-LKJ", Bidi.LTR),
        /*10*/  new ContextCase("", ".-=abc-+*", "", "*+-abc=-.", Bidi.RTL),
        /*11*/  new ContextCase(" ", ".-=abc-+*", " ", "*+-abc=-.", Bidi.RTL),
        /*12*/  new ContextCase("D", ".-=abc-+*", "G", "*+-abc=-.", Bidi.RTL),
        /*13*/  new ContextCase("x", ".-=abc-+*", "", "*+-.-=abc", Bidi.RTL),
        /*14*/  new ContextCase("", ".-=abc-+*", " y", "abc-+*=-.", Bidi.RTL),
        /*15*/  new ContextCase("", ".-=abc-+*", " 2", "abc-+*=-.", Bidi.RTL),
        /*16*/  new ContextCase(" x 1", ".-=abc-+*", " 2", ".-=abc-+*", Bidi.RTL),
        /*17*/  new ContextCase(" x 7", ".-=abc-+*", " 8", "*+-.-=abc", Bidi.RTL),
        /*18*/  new ContextCase("x|", ".-=abc-+*", " 8", "*+-abc=-.", Bidi.RTL),
        /*19*/  new ContextCase("G|y", ".-=abc-+*", " 8", "*+-.-=abc", Bidi.RTL),
        /*20*/  new ContextCase("", ".-=", "", ".-=", Bidi.LEVEL_DEFAULT_LTR),
        /*21*/  new ContextCase("D", ".-=", "", "=-.", Bidi.LEVEL_DEFAULT_LTR),
        /*22*/  new ContextCase("G", ".-=", "", "=-.", Bidi.LEVEL_DEFAULT_LTR),
        /*23*/  new ContextCase("xG", ".-=", "", ".-=", Bidi.LEVEL_DEFAULT_LTR),
        /*24*/  new ContextCase("x|G", ".-=", "", "=-.", Bidi.LEVEL_DEFAULT_LTR),
        /*25*/  new ContextCase("x|G", ".-=|-+*", "", "=-.|-+*", Bidi.LEVEL_DEFAULT_LTR),
    };
    private final int CONTEXT_COUNT = contextData.length;

    @Test
    public void testContext()
    {
        String prologue, epilogue, src, dest;
        Bidi bidi = new Bidi();
        int tc;
        ContextCase cc;

        logln("\nEntering TestContext\n");

        bidi.orderParagraphsLTR(true);

        for (tc = 0; tc < CONTEXT_COUNT; tc++) {
            cc = contextData[tc];
            prologue = pseudoToU16(cc.prologue);
            epilogue = pseudoToU16(cc.epilogue);
            /* in the call below, prologue and epilogue are swapped to show
               that the next call will override this call */
            bidi.setContext(epilogue, prologue);
            bidi.setContext(prologue, epilogue);
            src = pseudoToU16(cc.source);
            bidi.setPara(src, cc.paraLevel, null);
            dest = bidi.writeReordered(Bidi.DO_MIRRORING);
            dest = u16ToPseudo(dest);
            assertEquals("\nActual and expected output mismatch on case "+tc+"." +
                         "\nPrologue:           " + cc.prologue +
                         "\nInput:              " + cc.source +
                         "\nEpilogue:           " + cc.epilogue +
                         "\nParagraph level:    " + Byte.toString(bidi.getParaLevel()) + "\n",
                         cc.expected, dest);
        }

        logln("\nExiting TestContext\n");
    }
}
