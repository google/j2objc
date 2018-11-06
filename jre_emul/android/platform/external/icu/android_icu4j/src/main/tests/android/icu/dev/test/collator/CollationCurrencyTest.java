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
 * Port From:   ICU4C v2.1 : Collate/CollationCurrencyTest
 * Source File: $ICU4CRoot/source/test/intltest/currcoll.cpp
 **/
 
package android.icu.dev.test.collator;
 
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;
 
public class CollationCurrencyTest extends TestFmwk {
    @Test
    public void TestCurrency() {
        // All the currency symbols, in collation order
        char[][] currency = {
            { 0x00A4 }, /*00A4; L; [14 36, 03, 03]    # [082B.0020.0002] # CURRENCY SIGN*/
            { 0x00A2 }, /*00A2; L; [14 38, 03, 03]    # [082C.0020.0002] # CENT SIGN*/
            { 0xFFE0 }, /*FFE0; L; [14 38, 03, 05]    # [082C.0020.0003] # FULLWIDTH CENT SIGN*/
            { 0x0024 }, /*0024; L; [14 3A, 03, 03]    # [082D.0020.0002] # DOLLAR SIGN*/
            { 0xFF04 }, /*FF04; L; [14 3A, 03, 05]    # [082D.0020.0003] # FULLWIDTH DOLLAR SIGN*/
            { 0xFE69 }, /*FE69; L; [14 3A, 03, 1D]    # [082D.0020.000F] # SMALL DOLLAR SIGN*/
            { 0x00A3 }, /*00A3; L; [14 3C, 03, 03]    # [082E.0020.0002] # POUND SIGN*/
            { 0xFFE1 }, /*FFE1; L; [14 3C, 03, 05]    # [082E.0020.0003] # FULLWIDTH POUND SIGN*/
            { 0x00A5 }, /*00A5; L; [14 3E, 03, 03]    # [082F.0020.0002] # YEN SIGN*/
            { 0xFFE5 }, /*FFE5; L; [14 3E, 03, 05]    # [082F.0020.0003] # FULLWIDTH YEN SIGN*/
            { 0x09F2 }, /*09F2; L; [14 40, 03, 03]    # [0830.0020.0002] # BENGALI RUPEE MARK*/
            { 0x09F3 }, /*09F3; L; [14 42, 03, 03]    # [0831.0020.0002] # BENGALI RUPEE SIGN*/
            { 0x0E3F }, /*0E3F; L; [14 44, 03, 03]    # [0832.0020.0002] # THAI CURRENCY SYMBOL BAHT*/
            { 0x17DB }, /*17DB; L; [14 46, 03, 03]    # [0833.0020.0002] # KHMER CURRENCY SYMBOL RIEL*/
            { 0x20A0 }, /*20A0; L; [14 48, 03, 03]    # [0834.0020.0002] # EURO-CURRENCY SIGN*/
            { 0x20A1 }, /*20A1; L; [14 4A, 03, 03]    # [0835.0020.0002] # COLON SIGN*/
            { 0x20A2 }, /*20A2; L; [14 4C, 03, 03]    # [0836.0020.0002] # CRUZEIRO SIGN*/
            { 0x20A3 }, /*20A3; L; [14 4E, 03, 03]    # [0837.0020.0002] # FRENCH FRANC SIGN*/
            { 0x20A4 }, /*20A4; L; [14 50, 03, 03]    # [0838.0020.0002] # LIRA SIGN*/
            { 0x20A5 }, /*20A5; L; [14 52, 03, 03]    # [0839.0020.0002] # MILL SIGN*/
            { 0x20A6 }, /*20A6; L; [14 54, 03, 03]    # [083A.0020.0002] # NAIRA SIGN*/
            { 0x20A7 }, /*20A7; L; [14 56, 03, 03]    # [083B.0020.0002] # PESETA SIGN*/
            { 0x20A9 }, /*20A9; L; [14 58, 03, 03]    # [083C.0020.0002] # WON SIGN*/
            { 0xFFE6 }, /*FFE6; L; [14 58, 03, 05]    # [083C.0020.0003] # FULLWIDTH WON SIGN*/
            { 0x20AA }, /*20AA; L; [14 5A, 03, 03]    # [083D.0020.0002] # NEW SHEQEL SIGN*/
            { 0x20AB }, /*20AB; L; [14 5C, 03, 03]    # [083E.0020.0002] # DONG SIGN*/
            { 0x20AC }, /*20AC; L; [14 5E, 03, 03]    # [083F.0020.0002] # EURO SIGN*/
            { 0x20AD }, /*20AD; L; [14 60, 03, 03]    # [0840.0020.0002] # KIP SIGN*/
            { 0x20AE }, /*20AE; L; [14 62, 03, 03]    # [0841.0020.0002] # TUGRIK SIGN*/
            { 0x20AF } /*20AF; L; [14 64, 03, 03]    # [0842.0020.0002] # DRACHMA SIGN*/
        };
    
        int i, j;
        int expectedResult = 0;
        RuleBasedCollator c = (RuleBasedCollator)Collator.getInstance(Locale.ENGLISH);
        
        // Compare each currency symbol against all the
        // currency symbols, including itself
        String source;
        String target;
        
        for (i = 0; i < currency.length; i += 1) {
            for (j = 0; j < currency.length; j += 1) {
                source = new String(currency[i]);
                target = new String(currency[j]);

                if (i < j) {
                    expectedResult = -1;
                } else if ( i == j) {
                    expectedResult = 0;
                } else {
                    expectedResult = 1;
                }

                int compareResult = c.compare(source, target);
                CollationKey sourceKey = null;
            
                sourceKey = c.getCollationKey(source);

                if (sourceKey == null) {
                    errln("Couldn't get collationKey for source");
                    continue;
                }
            
                CollationKey targetKey = null;
                targetKey = c.getCollationKey(target);
                if (targetKey == null) {
                    errln("Couldn't get collationKey for source");
                    continue;
                }

                int keyResult = sourceKey.compareTo(targetKey);

                reportCResult( source, target, sourceKey, targetKey, compareResult, keyResult, compareResult, expectedResult );
            }
        }
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
