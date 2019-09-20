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
 * Port From:   ICU4C v2.1 : Collate/CollationGermanTest
 * Source File: $ICU4CRoot/source/test/intltest/decoll.cpp
 **/
 
 package android.icu.dev.test.collator;
 
 import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
 
 public class CollationGermanTest extends TestFmwk{
    private static char[][] testSourceCases = {
        {0x47, 0x72, 0x00F6, 0x00DF, 0x65},
        {0x61, 0x62, 0x63},
        {0x54, 0x00F6, 0x6e, 0x65},
        {0x54, 0x00F6, 0x6e, 0x65},
        {0x54, 0x00F6, 0x6e, 0x65},
        {0x61, 0x0308, 0x62, 0x63},
        {0x00E4, 0x62, 0x63},
        {0x00E4, 0x62, 0x63},
        {0x53, 0x74, 0x72, 0x61, 0x00DF, 0x65},
        {0x65, 0x66, 0x67},
        {0x00E4, 0x62, 0x63},
        {0x53, 0x74, 0x72, 0x61, 0x00DF, 0x65}
    };

    private static char[][] testTargetCases = {
        {0x47, 0x72, 0x6f, 0x73, 0x73, 0x69, 0x73, 0x74},
        {0x61, 0x0308, 0x62, 0x63},
        {0x54, 0x6f, 0x6e},
        {0x54, 0x6f, 0x64},
        {0x54, 0x6f, 0x66, 0x75},
        {0x41, 0x0308, 0x62, 0x63},
        {0x61, 0x0308, 0x62, 0x63},
        {0x61, 0x65, 0x62, 0x63},
        {0x53, 0x74, 0x72, 0x61, 0x73, 0x73, 0x65},
        {0x65, 0x66, 0x67},
        {0x61, 0x65, 0x62, 0x63},
        {0x53, 0x74, 0x72, 0x61, 0x73, 0x73, 0x65}
    };

    private static int results[][] =
    {
        //  Primary  Tertiary
        { -1,        -1 },
        { 0,         -1 },
        { 1,          1 },
        { 1,          1 },
        { 1,          1 },
        { 0,         -1 },
        { 0,          0 },
        { -1,        -1 },
        { 0,          1 },
        { 0,          0 },
        { -1,        -1 },
        { 0,          1 }
    };
    
    private Collator myCollation = null;
    
    public CollationGermanTest() {
    }
    
    @Before
    public void init() throws Exception {
        myCollation = Collator.getInstance(Locale.GERMAN);
        if(myCollation == null) {
            errln("ERROR: in creation of collator of GERMAN locale");
        }
    }

    // perform test with strength TERTIARY
    @Test
    public void TestTertiary(){
        if(myCollation == null ) {
            errln("decoll: cannot start test, collator is null\n");
            return;
        }

        int i = 0;
        myCollation.setStrength(Collator.TERTIARY);
        myCollation.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        for (i = 0; i < 12 ; i++)
        {
            doTest(testSourceCases[i], testTargetCases[i], results[i][1]);
        }
    }
    
    // perform test with strength SECONDARY
    //This method in icu4c has no implementation.
    @Test
    public void TestSecondary(){
    }
    
    // perform test with strength PRIMARY
    @Test
    public void TestPrimary(){
        if(myCollation == null ) {
            errln("decoll: cannot start test, collator is null\n");
            return;
        }
        int i;
        myCollation.setStrength(Collator.PRIMARY);
        myCollation.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        for (i = 0; i < 12 ; i++)
        {
            doTest(testSourceCases[i], testTargetCases[i], results[i][0]);
        }
    }
    
    
    //main test routine, tests rules specific to germa locale
    private void doTest(char[] source, char[] target, int result){
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
                                int compareResult, int keyResult, int incResult, int expectedResult ){
        if (expectedResult < -1 || expectedResult > 1)
        {
            errln("***** invalid call to reportCResult ****");
            return;
        }

        boolean ok1 = (compareResult == expectedResult);
        boolean ok2 = (keyResult == expectedResult);
        boolean ok3 = (incResult == expectedResult);

        if (ok1 && ok2 && ok3 && !isVerbose()){
            return;    
        }else{
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
