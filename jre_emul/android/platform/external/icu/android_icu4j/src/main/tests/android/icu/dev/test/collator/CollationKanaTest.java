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
 * Port From:   ICU4C v2.1 : Collate/CollationKanaTest
 * Source File: $ICU4CRoot/source/test/intltest/jacoll.cpp
 **/
 
package android.icu.dev.test.collator;
 
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;
import android.icu.util.ULocale;
 
public class CollationKanaTest extends TestFmwk{
    private static char[][] testSourceCases = {
        {0xff9E},
        {0x3042},
        {0x30A2},
        {0x3042, 0x3042},
        {0x30A2, 0x30FC},
        {0x30A2, 0x30FC, 0x30C8}                               /*  6 */
    };

    private static char[][] testTargetCases = {
        {0xFF9F},
        {0x30A2},
        {0x3042, 0x3042},
        {0x30A2, 0x30FC},
        {0x30A2, 0x30FC, 0x30C8},
        {0x3042, 0x3042, 0x3068}                              /*  6 */
    };

    private static int[] results = {
        -1,
        0,   //Collator::LESS, /* Katakanas and Hiraganas are equal on tertiary level(ICU 2.0)*/
        -1,
        1, // Collator::LESS, /* Prolonged sound mark sorts BEFORE equivalent vowel (ICU 2.0)*/
        -1,
        -1,    //Collator::GREATER /* Prolonged sound mark sorts BEFORE equivalent vowel (ICU 2.0)*//*  6 */
    };

    private static char[][] testBaseCases = {
        {0x30AB},
        {0x30AB, 0x30AD},
        {0x30AD},
        {0x30AD, 0x30AD}
    };

    private static char[][] testPlainDakutenHandakutenCases = {
        {0x30CF, 0x30AB},
        {0x30D0, 0x30AB},
        {0x30CF, 0x30AD},
        {0x30D0, 0x30AD}
    };

    private static char[][] testSmallLargeCases = {
        {0x30C3, 0x30CF},
        {0x30C4, 0x30CF},
        {0x30C3, 0x30D0},
        {0x30C4, 0x30D0}
    };

    private static char[][] testKatakanaHiraganaCases = {
        {0x3042, 0x30C3},
        {0x30A2, 0x30C3},
        {0x3042, 0x30C4},
        {0x30A2, 0x30C4}
    };

    private static char[][] testChooonKigooCases = {
        /*0*/ {0x30AB, 0x30FC, 0x3042},
        /*1*/ {0x30AB, 0x30FC, 0x30A2},
        /*2*/ {0x30AB, 0x30A4, 0x3042},
        /*3*/ {0x30AB, 0x30A4, 0x30A2},
        /*6*/ {0x30AD, 0x30FC, 0x3042}, /* Prolonged sound mark sorts BEFORE equivalent vowel (ICU 2.0)*/
        /*7*/ {0x30AD, 0x30FC, 0x30A2}, /* Prolonged sound mark sorts BEFORE equivalent vowel (ICU 2.0)*/
        /*4*/ {0x30AD, 0x30A4, 0x3042},
        /*5*/ {0x30AD, 0x30A4, 0x30A2}
    };
        
    private Collator myCollation = null;
    
    public CollationKanaTest() {
    }
    
    @Before
    public void init()throws Exception { 
        if(myCollation==null){
            myCollation = Collator.getInstance(Locale.JAPANESE); 
        }
    }

    // performs test with strength TERIARY
    @Test
    public void TestTertiary() {
        int i = 0;
        myCollation.setStrength(Collator.TERTIARY);
        
        for (i = 0; i < 6; i++) {
            doTest(testSourceCases[i], testTargetCases[i], results[i]);
        }
    }

    /* Testing base letters */
    @Test
    public void TestBase() {
        int i;
        myCollation.setStrength(Collator.PRIMARY);
        for (i = 0; i < 3 ; i++) {
            doTest(testBaseCases[i], testBaseCases[i + 1], -1);
        }
    }

    /* Testing plain, Daku-ten, Handaku-ten letters */
    @Test
    public void TestPlainDakutenHandakuten() {
        int i;
        myCollation.setStrength(Collator.SECONDARY);
        for (i = 0; i < 3 ; i++) {
            doTest(testPlainDakutenHandakutenCases[i], testPlainDakutenHandakutenCases[i + 1], -1);
        }
    }

    /* 
    * Test Small, Large letters
    */
    @Test
    public void TestSmallLarge() {
        int i;
        myCollation.setStrength(Collator.TERTIARY);

        for (i = 0; i < 3 ; i++) {
            doTest(testSmallLargeCases[i], testSmallLargeCases[i + 1], -1);
        }
    }

    /*
    * Test Katakana, Hiragana letters
    */
    @Test
    public void TestKatakanaHiragana() {
        int i;
        myCollation.setStrength(Collator.QUATERNARY);
        for (i = 0; i < 3 ; i++) {
            doTest(testKatakanaHiraganaCases[i], testKatakanaHiraganaCases[i + 1], -1);
        }
    }

    /*
    * Test Choo-on kigoo
    */
    @Test
    public void TestChooonKigoo() {
        int i;
        myCollation.setStrength(Collator.QUATERNARY);
        for (i = 0; i < 7 ; i++) {
            doTest(testChooonKigooCases[i], testChooonKigooCases[i + 1], -1);
        }
    }
    
    /*
     * Test common Hiragana and Katakana characters (e.g. 0x3099) (ticket:6140)
     */
    @Test
    public void TestCommonCharacters() {
        char[] tmp1 = { 0x3058, 0x30B8 };
        char[] tmp2 = { 0x3057, 0x3099, 0x30B7, 0x3099 };
        CollationKey key1, key2;
        int result;
        String string1 = new String(tmp1);
        String string2 = new String(tmp2);
        RuleBasedCollator rb = (RuleBasedCollator)Collator.getInstance(ULocale.JAPANESE);
        rb.setStrength(Collator.QUATERNARY);
        rb.setAlternateHandlingShifted(false);
        
        result = rb.compare(string1, string2);
        
        key1 = rb.getCollationKey(string1);
        key2 = rb.getCollationKey(string2);
        
        if ( result != 0 || !key1.equals(key2)) {
            errln("Failed Hiragana and Katakana common characters test. Expected results to be equal.");
        }
        
    }
    // main test routine, tests rules specific to "Kana" locale
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
        if (expectedResult < -1 || expectedResult > 1) {
            errln("***** invalid call to reportCResult ****");
            return;
        }

        boolean ok1 = (compareResult == expectedResult);
        boolean ok2 = (keyResult == expectedResult);
        boolean ok3 = (incResult == expectedResult);

        if (ok1 && ok2 && ok3 && !isVerbose()){
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
