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
 * Port From:   ICU4C v2.1 : Collate/G7CollationTest
 * Source File: $ICU4CRoot/source/test/intltest/g7coll.cpp
 **/
 
package android.icu.dev.test.collator;
 
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;
 
public class G7CollationTest extends TestFmwk{
    private static String[] testCases = {
        "blackbirds", "Pat", "p\u00E9ch\u00E9", "p\u00EAche", "p\u00E9cher",            
        "p\u00EAcher", "Tod", "T\u00F6ne", "Tofu", "blackbird", "Ton", 
        "PAT", "black-bird", "black-birds", "pat", // 14
        // Additional tests
        "czar", "churo", "cat", "darn", "?",                                                                                /* 19 */
        "quick", "#", "&", "a-rdvark", "aardvark",                                                        /* 23 */
        "abbot", "co-p", "cop", "coop", "zebra"
    };

    private static int[][] results = {
        { 12, 13, 9, 0, 14, 1, 11, 2, 3, 4, 5, 6, 8, 10, 7, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31 }, /* en_US */
        { 12, 13, 9, 0, 14, 1, 11, 2, 3, 4, 5, 6, 8, 10, 7, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31 }, /* en_GB */
        { 12, 13, 9, 0, 14, 1, 11, 2, 3, 4, 5, 6, 8, 10, 7, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31 }, /* en_CA */
        { 12, 13, 9, 0, 14, 1, 11, 2, 3, 4, 5, 6, 8, 10, 7, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31 }, /* fr_FR */
        { 12, 13, 9, 0, 14, 1, 11, 3, 2, 4, 5, 6, 8, 10, 7, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31 }, /* fr_CA */
        { 12, 13, 9, 0, 14, 1, 11, 2, 3, 4, 5, 6, 8, 10, 7, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31 }, /* de_DE */
        { 12, 13, 9, 0, 14, 1, 11, 2, 3, 4, 5, 6, 8, 10, 7, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31 }, /* it_IT */
        { 12, 13, 9, 0, 14, 1, 11, 2, 3, 4, 5, 6, 8, 10, 7, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31 }, /* ja_JP */
        /* new table collation with rules "& Z < p, P"  loop to FIXEDTESTSET */
        { 12, 13, 9, 0, 6, 8, 10, 7, 14, 1, 11, 2, 3, 4, 5, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31 }, 
        /* new table collation with rules "& C < ch , cH, Ch, CH " loop to TOTALTESTSET */
        { 19, 22, 21, 23, 24, 25, 12, 13, 9, 0, 17, 26, 28, 27, 15, 16, 18, 14, 1, 11, 2, 3, 4, 5, 20, 6, 8, 10, 7, 29 },
        /* new table collation with rules "& Question-mark ; ? & Hash-mark ; # & Ampersand ; '&'  " loop to TOTALTESTSET */
        { 23, 24, 25, 22, 12, 13, 9, 0, 17, 16, 26, 28, 27, 15, 18, 21, 14, 1, 11, 2, 3, 4, 5, 19, 20, 6, 8, 10, 7, 29 },
        /* analogous to Japanese rules " & aa ; a- & ee ; e- & ii ; i- & oo ; o- & uu ; u- " */  /* loop to TOTALTESTSET */
        { 19, 22, 21, 24, 23, 25, 12, 13, 9, 0, 17, 16, 28, 26, 27, 15, 18, 14, 1, 11, 2, 3, 4, 5, 20, 6, 8, 10, 7, 29 }
    };
    
    //private static final int MAX_TOKEN_LEN = 16;
    //private static final int TESTLOCALES = 12;
    private static final int FIXEDTESTSET = 15;
    private static final int TOTALTESTSET = 30;
    
    // perform test with added rules " & Z < p, P"
    @Test
    public void TestDemo1() {
        logln("Demo Test 1 : Create a new table collation with rules \"& Z < p, P\"");
        
        Collator col = Collator.getInstance(Locale.ENGLISH);    

        
        String baseRules = ((RuleBasedCollator)col).getRules();
        String newRules = " & Z < p, P";
        newRules = baseRules + newRules; 
        RuleBasedCollator myCollation = null; 
        try {
            myCollation = new RuleBasedCollator(newRules);
        } catch(Exception e) {
            errln("Fail to create RuleBasedCollator with rules:" + newRules);
            return;
        }
        
        int j, n;
        for (j = 0; j < FIXEDTESTSET; j++) {
            for (n = j+1; n < FIXEDTESTSET; n++) {
                doTest(myCollation, testCases[results[8][j]], testCases[results[8][n]], -1);
            }
        }
    }
    

    // perorm test with added rules "& C < ch , cH, Ch, CH"
    @Test
    public void TestDemo2() {
        logln("Demo Test 2 : Create a new table collation with rules \"& C < ch , cH, Ch, CH\"");
        Collator col = Collator.getInstance(Locale.ENGLISH);    


        String baseRules = ((RuleBasedCollator)col).getRules();
        String newRules = "& C < ch , cH, Ch, CH";
        newRules = baseRules + newRules; 
        RuleBasedCollator myCollation = null; 
        try {
            myCollation = new RuleBasedCollator(newRules);
        }catch(Exception e){
            errln("Fail to create RuleBasedCollator with rules:" + newRules);
            return;
        }  

        int j, n;
        for (j = 0; j < TOTALTESTSET; j++) {
            for (n = j+1; n < TOTALTESTSET; n++) {
                doTest(myCollation, testCases[results[9][j]], testCases[results[9][n]], -1);
            }
        }
    }
    

    // perform test with added rules 
    // "& Question'-'mark ; '?' & Hash'-'mark ; '#' & Ampersand ; '&'"
    @Test
    public void TestDemo3() {
        // logln("Demo Test 3 : Create a new table collation with rules \"& Question'-'mark ; '?' & Hash'-'mark ; '#' & Ampersand ; '&'\"");
        Collator col = Collator.getInstance(Locale.ENGLISH);    

        
        String baseRules = ((RuleBasedCollator)col).getRules();
        String newRules = "& Question'-'mark ; '?' & Hash'-'mark ; '#' & Ampersand ; '&'";
        newRules = baseRules + newRules;
        RuleBasedCollator myCollation = null; 
        try {
            myCollation = new RuleBasedCollator(newRules);
        }catch(Exception e){
            errln("Fail to create RuleBasedCollator with rules:" + newRules);
            return;
        }  

        int j, n;
        for (j = 0; j < TOTALTESTSET; j++) {
            for (n = j+1; n < TOTALTESTSET; n++) {
                doTest(myCollation, testCases[results[10][j]], testCases[results[10][n]], -1);
            }
        }
    }
    

    // perform test with added rules 
    // " & aa ; a'-' & ee ; e'-' & ii ; i'-' & oo ; o'-' & uu ; u'-' "
    @Test
    public void TestDemo4() {
        logln("Demo Test 4 : Create a new table collation with rules \" & aa ; a'-' & ee ; e'-' & ii ; i'-' & oo ; o'-' & uu ; u'-' \"");
        Collator col = Collator.getInstance(Locale.ENGLISH);    

        String baseRules = ((RuleBasedCollator)col).getRules();
        String newRules = " & aa ; a'-' & ee ; e'-' & ii ; i'-' & oo ; o'-' & uu ; u'-' ";
        newRules = baseRules + newRules;
        RuleBasedCollator myCollation = null; 
        try {
            myCollation = new RuleBasedCollator(newRules);
        }catch(Exception e){
            errln("Fail to create RuleBasedCollator with rules:" + newRules);
            return;
        }  

        int j, n;
        for (j = 0; j < TOTALTESTSET; j++) {
            for (n = j+1; n < TOTALTESTSET; n++) {
                doTest(myCollation, testCases[results[11][j]], testCases[results[11][n]], -1);
            }
        }
    }
    
    @Test
    public void TestG7Data() {
        Locale locales[] = {
                Locale.US,
                Locale.UK,
                Locale.CANADA,
                Locale.FRANCE,
                Locale.CANADA_FRENCH,
                Locale.GERMANY,
                Locale.JAPAN,
                Locale.ITALY
            };
        int i = 0, j = 0;
        for (i = 0; i < locales.length; i++) {
            Collator myCollation= null;
            RuleBasedCollator tblColl1 = null;
            try {
                myCollation = Collator.getInstance(locales[i]);
                tblColl1 = new RuleBasedCollator(((RuleBasedCollator)myCollation).getRules());
            } catch (Exception foo) {
                warnln("Exception: " + foo.getMessage() +
                      "; Locale : " + locales[i].getDisplayName() + " getRules failed");
                continue;
            }
            for (j = 0; j < FIXEDTESTSET; j++) {
                for (int n = j+1; n < FIXEDTESTSET; n++) {
                    doTest(tblColl1, testCases[results[i][j]], testCases[results[i][n]], -1);
                }
            }
            myCollation = null;
        }
    }
    
    
    // main test routine, tests comparisons for a set of strings against sets of expected results
    private void doTest(Collator myCollation, String source, String target, 
                        int result){
        
        int compareResult = myCollation.compare(source, target);
        CollationKey sortKey1, sortKey2;
        sortKey1 = myCollation.getCollationKey(source);
        sortKey2 = myCollation.getCollationKey(target);
        int keyResult = sortKey1.compareTo(sortKey2);
        reportCResult(source, target, sortKey1, sortKey2, compareResult, 
                      keyResult, compareResult, result);
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
                // Android patch: Add --omitCollationRules to genrb.
                logln(msg1 + source + msg2 + target + msg3 + sResult + msg4 + sExpect);
                // Android patch end.
            }
            
            msg1 = ok2 ? "Ok: key(\"" : "FAIL: key(\"";
            msg2 = "\").compareTo(key(\"";
            msg3 = "\")) returned ";
            sResult = CollationTest.appendCompareResult(keyResult, sResult);
            if (ok2) {
                logln(msg1 + source + msg2 + target + msg3 + sResult);
            } else {
                // Android patch: Add --omitCollationRules to genrb.
                logln(msg1 + source + msg2 + target + msg3 + sResult + msg4 + sExpect);
                // Android patch end.
                msg1 = "  ";
                msg2 = " vs. ";
                // Android patch: Add --omitCollationRules to genrb.
                logln(msg1 + CollationTest.prettify(sourceKey) + msg2 + CollationTest.prettify(targetKey));
                // Android patch end.
            }
            
            msg1 = ok3 ? "Ok: incCompare(\"" : "FAIL: incCompare(\"";
            msg2 = "\", \"";
            msg3 = "\") returned ";

            sResult = CollationTest.appendCompareResult(incResult, sResult);

            if (ok3) {
                logln(msg1 + source + msg2 + target + msg3 + sResult);
            } else {
                // Android patch: Add --omitCollationRules to genrb.
                logln(msg1 + source + msg2 + target + msg3 + sResult + msg4 + sExpect);
                // Android patch end.
            }                
        }
    }
}
