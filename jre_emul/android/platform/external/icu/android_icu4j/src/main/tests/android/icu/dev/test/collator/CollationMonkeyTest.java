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
 * Port From:   ICU4C v2.1 : collate/CollationMonkeyTest
 * Source File: $ICU4CRoot/source/test/intltest/mnkytst.cpp
 **/

package android.icu.dev.test.collator;

import java.util.Locale;
import java.util.Random;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;

/**
 * CollationMonkeyTest is a third level test class.  This tests the random 
 * substrings of the default test strings to verify if the compare and 
 * sort key algorithm works correctly.  For example, any string is always
 * less than the string itself appended with any character.
 */

public class CollationMonkeyTest extends TestFmwk {
    
    private String source = "-abcdefghijklmnopqrstuvwxyz#&^$@";
    
    @Test
    public void TestCollationKey() {
        if(source.length() == 0) {
            errln("CollationMonkeyTest.TestCollationKey(): source is empty - ICU_DATA not set or data missing?");
            return;
        }
        Collator myCollator;
        try {
             myCollator = Collator.getInstance(new Locale("en", "US"));
        } catch (Exception e) {
            warnln("ERROR: in creation of collator of ENGLISH locale");
            return;
        }
        
        Random rand = createRandom(); // use test framework's random seed
        int s = rand.nextInt(0x7fff) % source.length();
        int t = rand.nextInt(0x7fff) % source.length();
        int slen = Math.abs(rand.nextInt(0x7fff) % source.length() - source.length()) % source.length();
        int tlen = Math.abs(rand.nextInt(0x7fff) % source.length() - source.length()) % source.length();
        String subs = source.substring(Math.min(s, slen), Math.min(s + slen, source.length()));
        String subt = source.substring(Math.min(t, tlen), Math.min(t + tlen, source.length()));
    
        CollationKey collationKey1, collationKey2;
    
        myCollator.setStrength(Collator.TERTIARY);
        collationKey1 = myCollator.getCollationKey(subs);
        collationKey2 = myCollator.getCollationKey(subt);
        int result = collationKey1.compareTo(collationKey2);  // Tertiary
        int revResult = collationKey2.compareTo(collationKey1);  // Tertiary
        report( subs, subt, result, revResult);
    
        myCollator.setStrength(Collator.SECONDARY);
        collationKey1 = myCollator.getCollationKey(subs);
        collationKey2 = myCollator.getCollationKey(subt);
        result = collationKey1.compareTo(collationKey2);  // Secondary
        revResult = collationKey2.compareTo(collationKey1);   // Secondary
        report( subs, subt, result, revResult);
    
        myCollator.setStrength(Collator.PRIMARY);
        collationKey1 = myCollator.getCollationKey(subs);
        collationKey2 = myCollator.getCollationKey(subt);
        result = collationKey1.compareTo(collationKey2);  // Primary
        revResult = collationKey2.compareTo(collationKey1);   // Primary
        report(subs, subt, result, revResult);
    
        String msg = "";
        String addOne = subs + String.valueOf(0xE000);
    
        collationKey1 = myCollator.getCollationKey(subs);
        collationKey2 = myCollator.getCollationKey(addOne);
        result = collationKey1.compareTo(collationKey2);
        if (result != -1) {
            msg += "CollationKey(";
            msg += subs;
            msg += ") .LT. CollationKey(";
            msg += addOne;
            msg += ") Failed.";
            errln(msg);
        }
    
        msg = "";
        result = collationKey2.compareTo(collationKey1);
        if (result != 1) {
            msg += "CollationKey(";
            msg += addOne;
            msg += ") .GT. CollationKey(";
            msg += subs;
            msg += ") Failed.";
            errln(msg);
        }
    }
    
    // perform monkey tests using Collator.compare
    @Test
    public void TestCompare() {
        if(source.length() == 0) {
            errln("CollationMonkeyTest.TestCompare(): source is empty - ICU_DATA not set or data missing?");
            return;
        }
        
        Collator myCollator;
        try {
             myCollator = Collator.getInstance(new Locale("en", "US"));
        } catch (Exception e) {
            warnln("ERROR: in creation of collator of ENGLISH locale");
            return;
        }
        
        /* Seed the random-number generator with current time so that
         * the numbers will be different every time we run.
         */
        
        Random rand = createRandom(); // use test framework's random seed
        int s = rand.nextInt(0x7fff) % source.length();
        int t = rand.nextInt(0x7fff) % source.length();
        int slen = Math.abs(rand.nextInt(0x7fff) % source.length() - source.length()) % source.length();
        int tlen = Math.abs(rand.nextInt(0x7fff) % source.length() - source.length()) % source.length();
        String subs = source.substring(Math.min(s, slen), Math.min(s + slen, source.length()));
        String subt = source.substring(Math.min(t, tlen), Math.min(t + tlen, source.length()));
    
        myCollator.setStrength(Collator.TERTIARY);
        int result = myCollator.compare(subs, subt);  // Tertiary
        int revResult = myCollator.compare(subt, subs);  // Tertiary
        report(subs, subt, result, revResult);
    
        myCollator.setStrength(Collator.SECONDARY);
        result = myCollator.compare(subs, subt);  // Secondary
        revResult = myCollator.compare(subt, subs);  // Secondary
        report(subs, subt, result, revResult);
    
        myCollator.setStrength(Collator.PRIMARY);
        result = myCollator.compare(subs, subt);  // Primary
        revResult = myCollator.compare(subt, subs);  // Primary
        report(subs, subt, result, revResult);
    
        String msg = "";
        String addOne = subs + String.valueOf(0xE000);
    
        result = myCollator.compare(subs, addOne);
        if (result != -1) {
            msg += "Test : ";
            msg += subs;
            msg += " .LT. ";
            msg += addOne;
            msg += " Failed.";
            errln(msg);
        }
    
        msg = "";
        result = myCollator.compare(addOne, subs);
        if (result != 1) {
            msg += "Test : ";
            msg += addOne;
            msg += " .GT. ";
            msg += subs;
            msg += " Failed.";
            errln(msg);
        }
    }
    
    void report(String s, String t, int result, int revResult) {
        if (revResult != -result) {
            String msg = "";
            msg += s; 
            msg += " and ";
            msg += t;
            msg += " round trip comparison failed";
            msg += " (result " + result + ", reverse Result " + revResult + ")"; 
            errln(msg);
        }
    }
    
    @Test
    public void TestRules() {
        String testSourceCases[] = {
            "\u0061\u0062\u007a", 
            "\u0061\u0062\u007a", 
        };
    
        String testTargetCases[] = {
            "\u0061\u0062\u00e4",
            "\u0061\u0062\u0061\u0308",
        };
        
        int i=0;
        logln("Demo Test 1 : Create a new table collation with rules \"& z < 0x00e4\"");
        Collator col = Collator.getInstance(new Locale("en", "US"));
        String baseRules = ((RuleBasedCollator)col).getRules();
        String newRules = " & z < ";
        newRules = baseRules + newRules + String.valueOf(0x00e4);
        RuleBasedCollator myCollation = null;
        try {
            myCollation = new RuleBasedCollator(newRules);
        } catch (Exception e) {
            warnln( "Demo Test 1 Table Collation object creation failed.");
            return;
        }
        
        for(i=0; i<2; i++){
            doTest(myCollation, testSourceCases[i], testTargetCases[i], -1);
        }
        logln("Demo Test 2 : Create a new table collation with rules \"& z < a 0x0308\"");
        newRules = "";
        newRules = baseRules + " & z < a" + String.valueOf(0x0308);
        try {
            myCollation = new RuleBasedCollator(newRules);
        } catch (Exception e) {
            errln( "Demo Test 1 Table Collation object creation failed.");
            return;
        }
        for(i=0; i<2; i++){
            doTest(myCollation, testSourceCases[i], testTargetCases[i], -1);
        }
    }
    
    void doTest(RuleBasedCollator myCollation, String mysource, String target, int result) {
        int compareResult = myCollation.compare(source, target);
        CollationKey sortKey1, sortKey2;
        
        try {
            sortKey1 = myCollation.getCollationKey(source);
            sortKey2 = myCollation.getCollationKey(target);
        } catch (Exception e) {
            errln("SortKey generation Failed.\n");
            return;
        }
        int keyResult = sortKey1.compareTo(sortKey2);
        reportCResult( mysource, target, sortKey1, sortKey2, compareResult, keyResult, compareResult, result );
    }
    
    public void reportCResult(String src, String target, CollationKey sourceKey, CollationKey targetKey,
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
                logln(msg1 + src + msg2 + target + msg3 + sResult);
            } else {
                errln(msg1 + src + msg2 + target + msg3 + sResult + msg4 + sExpect);
            }
            msg1 = ok2 ? "Ok: key(\"" : "FAIL: key(\"";
            msg2 = "\").compareTo(key(\"";
            msg3 = "\")) returned ";
            sResult = CollationTest.appendCompareResult(keyResult, sResult);
            if (ok2) {
                logln(msg1 + src + msg2 + target + msg3 + sResult);
            } else {
                errln(msg1 + src + msg2 + target + msg3 + sResult + msg4 + sExpect);
                msg1 = "  ";
                msg2 = " vs. ";
                errln(msg1 + CollationTest.prettify(sourceKey) + msg2 + CollationTest.prettify(targetKey));
            }
            msg1 = ok3 ? "Ok: incCompare(\"" : "FAIL: incCompare(\"";
            msg2 = "\", \"";
            msg3 = "\") returned ";
            sResult = CollationTest.appendCompareResult(incResult, sResult);
            if (ok3) {
                logln(msg1 + src + msg2 + target + msg3 + sResult);
            } else {
                errln(msg1 + src + msg2 + target + msg3 + sResult + msg4 + sExpect);
            }                
        }
    }
}
