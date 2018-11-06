/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2012, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.bidi;

import org.junit.Test;

import android.icu.text.Bidi;


/**
 * Regression test for the Arabic Mathematical Alphabetic Symbols.
 *
 * Ported from C by Ramy Said
 */

public class TestReorderArabicMathSymbols extends BidiFmwk {

    private static final String[] logicalOrder = {
        /* Arabic mathematical Symbols "\u1EE00 - "\u1EE1B */
        "\uD83B\uDE00\uD83B\uDE01\uD83B\uDE02\uD83B\uDE03\u0020" + 
        "\uD83B\uDE24\uD83B\uDE05\uD83B\uDE06\u0020" + 
        "\uD83B\uDE07\uD83B\uDE08\uD83B\uDE09\u0020" + 
        "\uD83B\uDE0A\uD83B\uDE0B\uD83B\uDE0C\uD83B\uDE0D\u0020" + 
        "\uD83B\uDE0E\uD83B\uDE0F\uD83B\uDE10\uD83B\uDE11\u0020" + 
        "\uD83B\uDE12\uD83B\uDE13\uD83B\uDE14\uD83B\uDE15\u0020" + 
        "\uD83B\uDE16\uD83B\uDE17\uD83B\uDE18\u0020" + 
        "\uD83B\uDE19\uD83B\uDE1A\uD83B\uDE1B",
        /* Arabic mathematical Symbols - Looped Symbols\u1EE80 - "\u1EE9B */
        "\uD83B\uDE80\uD83B\uDE81\uD83B\uDE82\uD83B\uDE83\u0020" + 
        "\uD83B\uDE84\uD83B\uDE85\uD83B\uDE86\u0020" + 
        "\uD83B\uDE87\uD83B\uDE88\uD83B\uDE89\u0020" + 
        "\uD83B\uDE8B\uD83B\uDE8C\uD83B\uDE8D\u0020" + 
        "\uD83B\uDE8E\uD83B\uDE8F\uD83B\uDE90\uD83B\uDE91\u0020" + 
        "\uD83B\uDE92\uD83B\uDE93\uD83B\uDE94\uD83B\uDE95\u0020" + 
        "\uD83B\uDE96\uD83B\uDE97\uD83B\uDE98\u0020" + 
        "\uD83B\uDE99\uD83B\uDE9A\uD83B\uDE9B",
        /* Arabic mathematical Symbols - Double-struck Symbols\u1EEA1 - "\u1EEBB */
        "\uD83B\uDEA1\uD83B\uDEA2\uD83B\uDEA3\u0020" + 
        "\uD83B\uDEA5\uD83B\uDEA6\u0020" + 
        "\uD83B\uDEA7\uD83B\uDEA8\uD83B\uDEA9\u0020" + 
        "\uD83B\uDEAB\uD83B\uDEAC\uD83B\uDEAD\u0020" + 
        "\uD83B\uDEAE\uD83B\uDEAF\uD83B\uDEB0\uD83B\uDEB1\u0020" + 
        "\uD83B\uDEB2\uD83B\uDEB3\uD83B\uDEB4\uD83B\uDEB5\u0020" + 
        "\uD83B\uDEB6\uD83B\uDEB7\uD83B\uDEB8\u0020" + 
        "\uD83B\uDEB9\uD83B\uDEBA\uD83B\uDEBB",
        /* Arabic mathematical Symbols - Initial Symbols\u1EE21 - "\u1EE3B */
        "\uD83B\uDE21\uD83B\uDE22\u0020" + 
        "\uD83B\uDE27\uD83B\uDE29\u0020" + 
        "\uD83B\uDE2A\uD83B\uDE2B\uD83B\uDE2C\uD83B\uDE2D\u0020" + 
        "\uD83B\uDE2E\uD83B\uDE2F\uD83B\uDE30\uD83B\uDE31\u0020" + 
        "\uD83B\uDE32\uD83B\uDE34\uD83B\uDE35\u0020" + 
        "\uD83B\uDE36\uD83B\uDE37\u0020" + 
        "\uD83B\uDE39\uD83B\uDE3B",
        /* Arabic mathematical Symbols - Tailed Symbols */
        "\uD83B\uDE42\uD83B\uDE47\uD83B\uDE49\uD83B\uDE4B\u0020" + 
        "\uD83B\uDE4D\uD83B\uDE4E\uD83B\uDE4F\u0020" + 
        "\uD83B\uDE51\uD83B\uDE52\uD83B\uDE54\uD83B\uDE57\u0020" + 
        "\uD83B\uDE59\uD83B\uDE5B\uD83B\uDE5D\uD83B\uDE5F"
    };

    private static final String[] visualOrder = {
        /* Arabic mathematical Symbols "\u1EE00 - "\u1EE1B */
        "\uD83B\uDE1B\uD83B\uDE1A\uD83B\uDE19\u0020" +    
        "\uD83B\uDE18\uD83B\uDE17\uD83B\uDE16\u0020" +    
        "\uD83B\uDE15\uD83B\uDE14\uD83B\uDE13\uD83B\uDE12\u0020" +
        "\uD83B\uDE11\uD83B\uDE10\uD83B\uDE0F\uD83B\uDE0E\u0020" +
        "\uD83B\uDE0D\uD83B\uDE0C\uD83B\uDE0B\uD83B\uDE0A\u0020" +
        "\uD83B\uDE09\uD83B\uDE08\uD83B\uDE07\u0020" +    
        "\uD83B\uDE06\uD83B\uDE05\uD83B\uDE24\u0020" +    
        "\uD83B\uDE03\uD83B\uDE02\uD83B\uDE01\uD83B\uDE00",
        /* Arabic mathematical Symbols - Looped Symbols\u1EE80 - "\u1EE9B */
        "\uD83B\uDE9B\uD83B\uDE9A\uD83B\uDE99\u0020" +    
        "\uD83B\uDE98\uD83B\uDE97\uD83B\uDE96\u0020" +    
        "\uD83B\uDE95\uD83B\uDE94\uD83B\uDE93\uD83B\uDE92\u0020" +
        "\uD83B\uDE91\uD83B\uDE90\uD83B\uDE8F\uD83B\uDE8E\u0020" +
        "\uD83B\uDE8D\uD83B\uDE8C\uD83B\uDE8B\u0020" +    
        "\uD83B\uDE89\uD83B\uDE88\uD83B\uDE87\u0020" +    
        "\uD83B\uDE86\uD83B\uDE85\uD83B\uDE84\u0020" +    
        "\uD83B\uDE83\uD83B\uDE82\uD83B\uDE81\uD83B\uDE80",
        /* Arabic mathematical Symbols - Double-struck Symbols\u1EEA1 - "\u1EEBB */
        "\uD83B\uDEBB\uD83B\uDEBA\uD83B\uDEB9\u0020" +    
        "\uD83B\uDEB8\uD83B\uDEB7\uD83B\uDEB6\u0020" +    
        "\uD83B\uDEB5\uD83B\uDEB4\uD83B\uDEB3\uD83B\uDEB2\u0020" +
        "\uD83B\uDEB1\uD83B\uDEB0\uD83B\uDEAF\uD83B\uDEAE\u0020" +
        "\uD83B\uDEAD\uD83B\uDEAC\uD83B\uDEAB\u0020" +    
        "\uD83B\uDEA9\uD83B\uDEA8\uD83B\uDEA7\u0020" +    
        "\uD83B\uDEA6\uD83B\uDEA5\u0020" +            
        "\uD83B\uDEA3\uD83B\uDEA2\uD83B\uDEA1",
        /* Arabic mathematical Symbols - Initial Symbols\u1EE21 - "\u1EE3B */
        "\uD83B\uDE3B\uD83B\uDE39\u0020" +            
        "\uD83B\uDE37\uD83B\uDE36\u0020" +            
        "\uD83B\uDE35\uD83B\uDE34\uD83B\uDE32\u0020" +    
        "\uD83B\uDE31\uD83B\uDE30\uD83B\uDE2F\uD83B\uDE2E\u0020" +
        "\uD83B\uDE2D\uD83B\uDE2C\uD83B\uDE2B\uD83B\uDE2A\u0020" +
        "\uD83B\uDE29\uD83B\uDE27\u0020" +            
        "\uD83B\uDE22\uD83B\uDE21",
        /* Arabic mathematical Symbols - Tailed Symbols */
        "\uD83B\uDE5F\uD83B\uDE5D\uD83B\uDE5B\uD83B\uDE59\u0020" +
        "\uD83B\uDE57\uD83B\uDE54\uD83B\uDE52\uD83B\uDE51\u0020" +
        "\uD83B\uDE4F\uD83B\uDE4E\uD83B\uDE4D\u0020" +    
        "\uD83B\uDE4B\uD83B\uDE49\uD83B\uDE47\uD83B\uDE42"
    };

    @Test
    public void testReorderArabicMathSymbols() {
        Bidi bidi = new Bidi();
        int testNumber;
        int nTests = logicalOrder.length;
        String srcU16, dest = "";

        logln("\nEntering TestReorderArabicMathSymbols\n");

        for (testNumber = 0; testNumber < nTests; testNumber++) {
            logln("Testing L2V #1 for case " + testNumber);
            srcU16 = logicalOrder[testNumber];
            try {
                bidi.setPara(srcU16, Bidi.LEVEL_DEFAULT_LTR, null);
            } catch (Exception e) {
                errln("Bidi.setPara(tests[" + testNumber + "], paraLevel " +
                      Bidi.LEVEL_DEFAULT_LTR + " failed.");
            }
            try {
                dest = bidi.writeReordered(Bidi.DO_MIRRORING);
            } catch (Exception e) {
                errln("Bidi.writeReordered(tests[" + testNumber + "], paraLevel " +
                      Bidi.LEVEL_DEFAULT_LTR + " failed.");
            }
            if (!visualOrder[testNumber].equals(dest)) {
                assertEquals("Failure #1 in Bidi.writeReordered(), test number " +
                             testNumber, visualOrder[testNumber], dest, srcU16, null,
                             "Bidi.DO_MIRRORING", "Bidi.LEVEL_DEFAULT_LTR");
            }
        }
        
        logln("\nExiting TestReorderArabicMathSymbols\n");
    }
}
