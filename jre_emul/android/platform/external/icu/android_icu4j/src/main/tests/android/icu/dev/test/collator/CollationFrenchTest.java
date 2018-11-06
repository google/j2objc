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
 * Port From:   ICU4C v2.1 : Collate/CollationFrenchTest
 * Source File: $ICU4CRoot/source/test/intltest/frcoll.cpp
 **/
 
package android.icu.dev.test.collator;
 
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;
 
public class CollationFrenchTest extends TestFmwk{
    private static char[][] testSourceCases = {
        {0x0061/*'a'*/, 0x0062/*'b'*/, 0x0063/*'c'*/},
        {0x0043/*'C'*/, 0x004f/*'O'*/, 0x0054/*'T'*/, 0x0045/*'E'*/},
        {0x0063/*'c'*/, 0x006f/*'o'*/, 0x002d/*'-'*/, 0x006f/*'o'*/, 0x0070/*'p'*/},
        {0x0070/*'p'*/, 0x00EA, 0x0063/*'c'*/, 0x0068/*'h'*/, 0x0065/*'e'*/},
        {0x0070/*'p'*/, 0x00EA, 0x0063/*'c'*/, 0x0068/*'h'*/, 0x0065/*'e'*/, 0x0072/*'r'*/},
        {0x0070/*'p'*/, 0x00E9, 0x0063/*'c'*/, 0x0068/*'h'*/, 0x0065/*'e'*/, 0x0072/*'r'*/},
        {0x0070/*'p'*/, 0x00E9, 0x0063/*'c'*/, 0x0068/*'h'*/, 0x0065/*'e'*/, 0x0072/*'r'*/},
        {0x0048/*'H'*/, 0x0065/*'e'*/, 0x006c/*'l'*/, 0x006c/*'l'*/, 0x006f/*'o'*/},
        {0x01f1},
        {0xfb00},
        {0x01fa},
        {0x0101}
    };

    private static char[][] testTargetCases = {
        {0x0041/*'A'*/, 0x0042/*'B'*/, 0x0043/*'C'*/},
        {0x0063/*'c'*/, 0x00f4, 0x0074/*'t'*/, 0x0065/*'e'*/},
        {0x0043/*'C'*/, 0x004f/*'O'*/, 0x004f/*'O'*/, 0x0050/*'P'*/},
        {0x0070/*'p'*/, 0x00E9, 0x0063/*'c'*/, 0x0068/*'h'*/, 0x00E9},
        {0x0070/*'p'*/,  0x00E9, 0x0063/*'c'*/, 0x0068/*'h'*/, 0x00E9},
        {0x0070/*'p'*/, 0x00EA, 0x0063/*'c'*/, 0x0068/*'h'*/, 0x0065/*'e'*/},
        {0x0070/*'p'*/, 0x00EA, 0x0063/*'c'*/, 0x0068/*'h'*/, 0x0065/*'e'*/, 0x0072/*'r'*/},
        {0x0068/*'h'*/, 0x0065/*'e'*/, 0x006c/*'l'*/, 0x006c/*'l'*/, 0x004f/*'O'*/},
        {0x01ee},
        {0x25ca},
        {0x00e0},
        {0x01df}
    };

    private static int[] results = {
        -1,
        -1,
        -1, /*Collator::GREATER,*/
        -1,
        1,
        1,
        -1,
        1,
       -1, /*Collator::GREATER,*/
        1,
        -1,
        -1
    };

    // 0x0300 is grave, 0x0301 is acute
    // the order of elements in this array must be different than the order in CollationEnglishTest
    private static char[][] testAcute = {
    /*00*/    {0x0065/*'e'*/, 0x0065/*'e'*/},
    /*01*/    {0x0065/*'e'*/, 0x0301, 0x0065/*'e'*/},
    /*02*/    {0x0065/*'e'*/, 0x0300, 0x0301, 0x0065/*'e'*/},
    /*03*/    {0x0065/*'e'*/, 0x0300, 0x0065/*'e'*/},
    /*04*/    {0x0065/*'e'*/, 0x0301, 0x0300, 0x0065/*'e'*/},
    /*05*/    {0x0065/*'e'*/, 0x0065/*'e'*/, 0x0301}, 
    /*06*/    {0x0065/*'e'*/, 0x0301, 0x0065/*'e'*/, 0x0301},
    /*07*/    {0x0065/*'e'*/, 0x0300, 0x0301, 0x0065/*'e'*/, 0x0301},
    /*08*/    {0x0065/*'e'*/, 0x0300, 0x0065/*'e'*/, 0x0301},
    /*09*/    {0x0065/*'e'*/, 0x0301, 0x0300, 0x0065/*'e'*/, 0x0301},
    /*0a*/    {0x0065/*'e'*/, 0x0065/*'e'*/, 0x0300, 0x0301},
    /*0b*/    {0x0065/*'e'*/, 0x0301, 0x0065/*'e'*/, 0x0300, 0x0301},
    /*0c*/    {0x0065/*'e'*/, 0x0300, 0x0301, 0x0065/*'e'*/, 0x0300, 0x0301},
    /*0d*/    {0x0065/*'e'*/, 0x0300, 0x0065/*'e'*/, 0x0300, 0x0301},
    /*0e*/    {0x0065/*'e'*/, 0x0301, 0x0300, 0x0065/*'e'*/, 0x0300, 0x0301},
    /*0f*/    {0x0065/*'e'*/, 0x0065/*'e'*/, 0x0300},
    /*10*/    {0x0065/*'e'*/, 0x0301, 0x0065/*'e'*/, 0x0300},
    /*11*/    {0x0065/*'e'*/, 0x0300, 0x0301, 0x0065/*'e'*/, 0x0300},
    /*12*/    {0x0065/*'e'*/, 0x0300, 0x0065/*'e'*/, 0x0300},
    /*13*/    {0x0065/*'e'*/, 0x0301, 0x0300, 0x0065/*'e'*/, 0x0300},
    /*14*/    {0x0065/*'e'*/, 0x0065/*'e'*/, 0x0301, 0x0300},
    /*15*/    {0x0065/*'e'*/, 0x0301, 0x0065/*'e'*/, 0x0301, 0x0300},
    /*16*/    {0x0065/*'e'*/, 0x0300, 0x0301, 0x0065/*'e'*/, 0x0301, 0x0300},
    /*17*/    {0x0065/*'e'*/, 0x0300, 0x0065/*'e'*/, 0x0301, 0x0300},
    /*18*/    {0x0065/*'e'*/, 0x0301, 0x0300, 0x0065/*'e'*/, 0x0301, 0x0300}
    };

    private static char[][] testBugs = {
        {0x0061/*'a'*/},
        {0x0041/*'A'*/},
        {0x0065/*'e'*/},
        {0x0045/*'E'*/},
        {0x00e9},
        {0x00e8},
        {0x00ea},
        {0x00eb},
        {0x0065/*'e'*/, 0x0061/*'a'*/},
        {0x0078/*'x'*/}
    };
    
    
    private Collator myCollation = null;
    
    public CollationFrenchTest() {
    }
    
    @Before
    public void init()throws Exception {
        myCollation = Collator.getInstance(Locale.CANADA_FRENCH);
    }
     
    // perform tests with strength TERTIARY
    @Test
    public void TestTertiary() {
        int i = 0;
        myCollation.setStrength(Collator.TERTIARY);
        
        for (i = 0; i < 12 ; i++) {
            doTest(testSourceCases[i], testTargetCases[i], results[i]);
        }
    }
    
    // perform tests with strength SECONDARY
    @Test
    public void TestSecondary() {
        //test acute and grave ordering
        int i = 0;
        int j;
        int expected;

        myCollation.setStrength(Collator.SECONDARY);
        
        for (i = 0; i < testAcute.length; i++) {
            for (j = 0; j < testAcute.length; j++) {
                if (i <  j) {
                    expected = -1;
                } else if (i == j) {
                    expected = 0;
                } else {
                    expected = 1;
                }
                doTest(testAcute[i], testAcute[j], expected );
            }
        }
    }

    // perform extra tests
    @Test
    public void TestExtra() {
        int i, j;
        myCollation.setStrength(Collator.TERTIARY);
        for (i = 0; i < 9 ; i++) {
            for (j = i + 1; j < 10; j += 1) {
                doTest(testBugs[i], testBugs[j], -1);
            }
        }
    }
    
    @Test
    public void TestContinuationReordering()
    {
        String rule = "&0x2f00 << 0x2f01";
        try {
            RuleBasedCollator collator = new RuleBasedCollator(rule);
            collator.setFrenchCollation(true);
            CollationKey key1 
                        = collator.getCollationKey("a\u0325\u2f00\u2f01b\u0325");
            CollationKey key2
                        = collator.getCollationKey("a\u0325\u2f01\u2f01b\u0325");
            if (key1.compareTo(key2) >= 0) {
                errln("Error comparing continuation strings");
            }
        } catch (Exception e) {
            errln(e.toString());
        }
    }
     
    // main test routine, test rules specific to the french locale
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
