/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2002-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

/** 
 * Port From:   ICU4C v2.1 : Collate/CollationFinnishTest
 * Source File: $ICU4CRoot/source/test/intltest/ficoll.cpp
 **/
 
package android.icu.dev.test.collator;
 
import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
import android.icu.util.ULocale;
 
public class CollationFinnishTest extends TestFmwk {
    private static char[][] testSourceCases = {
        {0x77, 0x61, 0x74},
        {0x76, 0x61, 0x74},
        {0x61, 0x00FC, 0x62, 0x65, 0x63, 0x6b},
        {0x4c, 0x00E5, 0x76, 0x69},
        {0x77, 0x61, 0x74}
    };

    private static char[][] testTargetCases = {
        {0x76, 0x61, 0x74},
        {0x77, 0x61, 0x79},
        {0x61, 0x78, 0x62, 0x65, 0x63, 0x6b},
        {0x4c, 0x00E4, 0x77, 0x65},
        {0x76, 0x61, 0x74}
    };

    private static int[] results = {
        1,
        -1,
        1,
        -1,
        // test primary > 4
        1,  // v < w per cldrbug 6615
    };

    private Collator myCollation = null;
    
    public CollationFinnishTest() {
    }
    
    @Before
    public void init()throws Exception{
        myCollation = Collator.getInstance(new ULocale("fi_FI@collation=standard"));
    }
     
    
    // perform tests with strength PRIMARY
    @Test
    public void TestPrimary() {
        int i = 0;
        myCollation.setStrength(Collator.PRIMARY);
        for(i = 4; i < 5; i++) {
            doTest(testSourceCases[i], testTargetCases[i], results[i]);    
        }         
    }
    
    // perform test with strength TERTIARY
    @Test
    public void TestTertiary() {
        int i = 0;
        myCollation.setStrength(Collator.TERTIARY);
        for(i = 0; i < 4; i++ ) {
            doTest(testSourceCases[i], testTargetCases[i], results[i]);
        }    
    }
    
    // main test routine, tests rules specific to the finish locale
    private void doTest(char[] source, char[] target, int result) {
        String s = new String(source);
        String t = new String(target);
        int compareResult = myCollation.compare(s, t);
        CollationKey sortKey1, sortKey2;
        sortKey1 = myCollation.getCollationKey(s);
        sortKey2 = myCollation.getCollationKey(t);
        int keyResult = sortKey1.compareTo(sortKey2);
        reportCResult(s, t, sortKey1, sortKey2, compareResult, keyResult, compareResult, result);
    }
    
    private void reportCResult( String source, String target, CollationKey sourceKey, CollationKey targetKey,
                                int compareResult, int keyResult, int incResult, int expectedResult ) {
        if (expectedResult < -1 || expectedResult > 1) {
            errln("***** invalid call to reportCResult ****");
            return;
        }

        boolean ok1 = (compareResult == expectedResult);
        boolean ok2 = (keyResult == expectedResult);
        boolean ok3 = (incResult == expectedResult);

        if (ok1 && ok2 && ok3 && !isVerbose()) {
            return;    
        } else {
            String msg1 = ok1? "Ok: compare(\"" : "FAIL: compare(\"";
            String msg2 = "\", \"";
            String msg3 = "\") returned ";
            String msg4 = "; expected ";
            
            String sExpect = new String("");
            String sResult = new String("");
            sResult = CollationTest.appendCompareResult(compareResult, sResult);
            sExpect = CollationTest.appendCompareResult(expectedResult, sExpect);
            if (ok1) {
                logln(msg1 + source + msg2 + target + msg3 + sResult);
            } else {
                errln(msg1 + source + msg2 + target + msg3 + sResult + msg4 + sExpect);
            }
            
            msg1 = ok2 ? "Ok: key(\"" : "FAIL: key(\"";
            msg2 = "\").compareTo(key(\"";
            msg3 = "\")) returned ";
            sResult = CollationTest.appendCompareResult(keyResult, sResult);
            if (ok2) {
                logln(msg1 + source + msg2 + target + msg3 + sResult);
            } else {
                errln(msg1 + source + msg2 + target + msg3 + sResult + msg4 + sExpect);
                msg1 = "  ";
                msg2 = " vs. ";
                errln(msg1 + CollationTest.prettify(sourceKey) + msg2 + CollationTest.prettify(targetKey));
            }
            
            msg1 = ok3 ? "Ok: incCompare(\"" : "FAIL: incCompare(\"";
            msg2 = "\", \"";
            msg3 = "\") returned ";

            sResult = CollationTest.appendCompareResult(incResult, sResult);

            if (ok3) {
                logln(msg1 + source + msg2 + target + msg3 + sResult);
            } else {
                errln(msg1 + source + msg2 + target + msg3 + sResult + msg4 + sExpect);
            }               
        }
    }
}
