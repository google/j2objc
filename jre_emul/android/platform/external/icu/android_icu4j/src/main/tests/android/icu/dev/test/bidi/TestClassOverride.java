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

import org.junit.Test;

import android.icu.text.Bidi;
import android.icu.text.BidiClassifier;

/**
 * Regression test for Bidi class override.
 *
 * @author Lina Kemmel, Matitiahu Allouche
 */

public class TestClassOverride extends BidiFmwk {

    private static final int DEF = TestData.DEF;
    private static final int L   = TestData.L;
    private static final int R   = TestData.R;
    private static final int AL  = TestData.AL;
    private static final int AN  = TestData.AN;
    private static final int EN  = TestData.EN;
    private static final int LRE = TestData.LRE;
    private static final int RLE = TestData.RLE;
    private static final int LRO = TestData.LRO;
    private static final int RLO = TestData.RLO;
    private static final int PDF = TestData.PDF;
    private static final int NSM = TestData.NSM;
    private static final int B   = TestData.B;
    private static final int S   = TestData.S;
    private static final int BN  = TestData.BN;

    private static final int[] customClasses = {
    /*  0/8    1/9    2/A    3/B    4/C    5/D    6/E    7/F  */
        DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF, //00-07
        DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF, //08-0F
        DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF, //10-17
        DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF, //18-1F
        DEF,   DEF,   DEF,   DEF,   DEF,   DEF,     R,   DEF, //20-27
        DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF, //28-2F
         EN,    EN,    EN,    EN,    EN,    EN,    AN,    AN, //30-37
         AN,    AN,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF, //38-3F
          L,    AL,    AL,    AL,    AL,    AL,    AL,     R, //40-47
          R,     R,     R,     R,     R,     R,     R,     R, //48-4F
          R,     R,     R,     R,     R,     R,     R,     R, //50-57
          R,     R,     R,   LRE,   DEF,   RLE,   PDF,     S, //58-5F
        NSM,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF, //60-67
        DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF, //68-6F
        DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF,   DEF, //70-77
        DEF,   DEF,   DEF,   LRO,     B,   RLO,    BN,   DEF  //78-7F
    };
    static final int nEntries = customClasses.length;

    static final String textIn  = "JIH.>12->a \u05d0\u05d1 6 ABC78";
    static final String textOut = "12<.HIJ->a 78CBA 6 \u05d1\u05d0";

    protected static class CustomClassifier extends BidiClassifier {

        public CustomClassifier(Object context) {
          super(context);
        }
        public int classify(int c) {
            // some (meaningless) action - just for testing purposes
            return (this.context != null ? ((Integer)context).intValue()
                            : c >= nEntries ? super.classify(c)
                            : customClasses[c]);
        }
    }

    private void verifyClassifier(Bidi bidi) {
        BidiClassifier actualClassifier = bidi.getCustomClassifier();

        if (this.classifier == null) {
            if (actualClassifier != null) {
                errln("Bidi classifier is not yet set, but reported as not null");
            }
        } else {
            Class<?> expectedClass = this.classifier.getClass();
            assertTrue("null Bidi classifier", actualClassifier != null);
            if (actualClassifier == null) {
                return;
            }
            if (expectedClass.isInstance(actualClassifier)) {
                Object context = classifier.getContext();
                if (context == null) {
                    if (actualClassifier.getContext() != null) {
                        errln("Unexpected context, should be null");
                    }
                } else {
                    assertEquals("Unexpected classifier context", context,
                                 actualClassifier.getContext());
                    assertEquals("Unexpected context's content",
                                 ((Integer)context).intValue(),
                                 bidi.getCustomizedClass('a'));
                }
            } else {
                errln("Bidi object reports classifier is an instance of " +
                      actualClassifier.getClass().getName() +
                      ",\nwhile the expected classifier should be an " +
                      "instance of " + expectedClass);
            }
        }
    }

    CustomClassifier classifier = null;

    @Test
    public void testClassOverride()
    {
        Bidi bidi;

        logln("\nEntering TestClassOverride\n");

        bidi = new Bidi();
        verifyClassifier(bidi);

        classifier = new CustomClassifier(new Integer(TestData.R));
        bidi.setCustomClassifier(classifier);
        verifyClassifier(bidi);

        classifier.setContext(null);
        verifyClassifier(bidi);

        bidi.setPara(textIn, Bidi.LTR, null);

        String out = bidi.writeReordered(Bidi.DO_MIRRORING);
        assertEquals("Actual and expected output mismatch", textOut, out);

        logln("\nExiting TestClassOverride\n");
    }
}
