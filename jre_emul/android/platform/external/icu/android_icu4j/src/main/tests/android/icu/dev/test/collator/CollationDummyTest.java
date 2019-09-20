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
 * Port From:   ICU4C v2.1 : Collate/CollationDummyTest
 * Source File: $ICU4CRoot/source/test/intltest/allcoll.cpp
 *              $ICU4CRoot/source/test/cintltst/callcoll.c
 **/
 
package android.icu.dev.test.collator;
 
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.CollationElementIterator;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
import android.icu.text.Normalizer;
import android.icu.text.RuleBasedCollator;
 
public class CollationDummyTest extends TestFmwk {
    //testSourceCases[][] and testTargetCases[][], testCases[][] are ported from the file callcoll.c in icu4c
    private static char[][] testSourceCases = {
        {0x61, 0x62, 0x27, 0x63},
        {0x63, 0x6f, 0x2d, 0x6f, 0x70},
        {0x61, 0x62},
        {0x61, 0x6d, 0x70, 0x65, 0x72, 0x73, 0x61, 0x64},
        {0x61, 0x6c, 0x6c},
        {0x66, 0x6f, 0x75, 0x72},
        {0x66, 0x69, 0x76, 0x65},
        {0x31},
        {0x31},
        {0x31},                                            //  10 
        {0x32},
        {0x32},
        {0x48, 0x65, 0x6c, 0x6c, 0x6f},
        {0x61, 0x3c, 0x62},
        {0x61, 0x3c, 0x62},
        {0x61, 0x63, 0x63},
        {0x61, 0x63, 0x48, 0x63},  //  simple test 
        {0x70, 0x00EA, 0x63, 0x68, 0x65},
        {0x61, 0x62, 0x63},
        {0x61, 0x62, 0x63},                                  //  20 
        {0x61, 0x62, 0x63},
        {0x61, 0x62, 0x63},
        {0x61, 0x62, 0x63},
        {0x61, 0x00E6, 0x63},
        {0x61, 0x63, 0x48, 0x63},  //  primary test 
        {0x62, 0x6c, 0x61, 0x63, 0x6b},
        {0x66, 0x6f, 0x75, 0x72},
        {0x66, 0x69, 0x76, 0x65},
        {0x31},
        {0x61, 0x62, 0x63},                                        //  30 
        {0x61, 0x62, 0x63},                                  
        {0x61, 0x62, 0x63, 0x48},
        {0x61, 0x62, 0x63},
        {0x61, 0x63, 0x48, 0x63},                              //  34 
        {0x61, 0x63, 0x65, 0x30},
        {0x31, 0x30},
        {0x70, 0x00EA,0x30}                                    // 37     
    };

    private static char[][] testTargetCases = {
        {0x61, 0x62, 0x63, 0x27},
        {0x43, 0x4f, 0x4f, 0x50},
        {0x61, 0x62, 0x63},
        {0x26},
        {0x26},
        {0x34},
        {0x35},
        {0x6f, 0x6e, 0x65},
        {0x6e, 0x6e, 0x65},
        {0x70, 0x6e, 0x65},                                  //  10 
        {0x74, 0x77, 0x6f},
        {0x75, 0x77, 0x6f},
        {0x68, 0x65, 0x6c, 0x6c, 0x4f},
        {0x61, 0x3c, 0x3d, 0x62},
        {0x61, 0x62, 0x63},
        {0x61, 0x43, 0x48, 0x63},
        {0x61, 0x43, 0x48, 0x63},  //  simple test 
        {0x70, 0x00E9, 0x63, 0x68, 0x00E9},
        {0x61, 0x62, 0x63},
        {0x61, 0x42, 0x43},                                  //  20 
        {0x61, 0x62, 0x63, 0x68},
        {0x61, 0x62, 0x64},
        {0x00E4, 0x62, 0x63},
        {0x61, 0x00C6, 0x63},
        {0x61, 0x43, 0x48, 0x63},  //  primary test 
        {0x62, 0x6c, 0x61, 0x63, 0x6b, 0x2d, 0x62, 0x69, 0x72, 0x64},
        {0x34},
        {0x35},
        {0x6f, 0x6e, 0x65},
        {0x61, 0x62, 0x63},
        {0x61, 0x42, 0x63},                                  //  30 
        {0x61, 0x62, 0x63, 0x68},
        {0x61, 0x62, 0x64},
        {0x61, 0x43, 0x48, 0x63},                                //  34 
        {0x61, 0x63, 0x65, 0x30},
        {0x31, 0x30},
        {0x70, 0x00EB,0x30}                                    // 37 
    };
    
    private static char[][] testCases = {
        {0x61},
        {0x41},
        {0x00e4},
        {0x00c4},
        {0x61, 0x65},
        {0x61, 0x45},
        {0x41, 0x65},
        {0x41, 0x45},
        {0x00e6},
        {0x00c6},
        {0x62},
        {0x63},
        {0x7a}
    };
    
    int[] results = {
        -1,
        -1, //Collator::GREATER,
        -1,
        -1,
        -1,
        -1,
        -1,
        1,
        1,
        -1,                                     //  10 
        1,
        -1,
        1,
        1,
        -1,
        -1,
        -1,
    //  test primary > 17 
        0,
        0,
        0,                                    //  20 
        -1,
        -1,
        0,
        0,
        0,
        -1,
    //  test secondary > 26 
        0,
        0,
        0,
        0,
        0,                                    //  30 
        0,
        -1,
        0,                                     //  34 
        0,
        0,
        -1 
    };
    
    final int MAX_TOKEN_LEN = 16;
    
    private RuleBasedCollator myCollation;
    
    public CollationDummyTest() {
    }
    
    @Before
    public void init() throws Exception {
        String ruleset = "& C < ch, cH, Ch, CH & Five, 5 & Four, 4 & one, 1 & Ampersand; '&' & Two, 2 ";
        // String ruleset = "& Four, 4";
        myCollation = new RuleBasedCollator(ruleset);
    }
    
    // perform test with strength tertiary
    @Test
    public void TestTertiary() {
        int i = 0;
        myCollation.setStrength(Collator.TERTIARY);
        for (i = 0; i < 17 ; i++) {
            doTest(myCollation, testSourceCases[i], testTargetCases[i], results[i]);
        }
    }

    // perform test with strength PRIMARY
    @Test
    public void TestPrimary() {
       // problem in strcollinc for unfinshed contractions 
       myCollation.setStrength(Collator.PRIMARY);
        for (int i = 17; i < 26 ; i++) {
            doTest(myCollation, testSourceCases[i], testTargetCases[i], results[i]);
        }
    }

    //perform test with strength SECONDARY
    @Test
    public void TestSecondary() {
        int i;
        myCollation.setStrength(Collator.SECONDARY);
        for (i = 26; i < 34; i++) {
            doTest(myCollation, testSourceCases[i], testTargetCases[i], results[i]);
        }
    }

    // perform extra tests
    @Test
    public void TestExtra() {
        int i, j;
        myCollation.setStrength(Collator.TERTIARY);
        for (i = 0; i < testCases.length - 1; i++) {
            for (j = i + 1; j < testCases.length; j += 1) {
                doTest(myCollation, testCases[i], testCases[j], -1);
            }
        }
    }

    @Test
    public void TestIdentical() {
        int i;
        myCollation.setStrength(Collator.IDENTICAL);
        for (i= 34; i<37; i++) {
            doTest(myCollation, testSourceCases[i], testTargetCases[i], results[i]);
        }
    }

    @Test
    public void TestJB581() {
        String source = "THISISATEST.";
        String target = "Thisisatest.";
        Collator coll = null;
        try {
            coll = Collator.getInstance(Locale.ENGLISH);
        } catch (Exception e) {
            errln("ERROR: Failed to create the collator for : en_US\n");
            return;
        }

        int result = coll.compare(source, target);
        // result is 1, secondary differences only for ignorable space characters
        if (result != 1) {
            errln("Comparing two strings with only secondary differences in C failed.\n");
            return;
        }
        
        // To compare them with just primary differences 
        coll.setStrength(Collator.PRIMARY);
        result = coll.compare(source, target);
        // result is 0 
        if (result != 0) {
            errln("Comparing two strings with no differences in C failed.\n");
            return;
        } 
          
        // Now, do the same comparison with keys 
        CollationKey sourceKeyOut, targetKeyOut;
        sourceKeyOut = coll.getCollationKey(source);
        targetKeyOut = coll.getCollationKey(target);
        result = sourceKeyOut.compareTo(targetKeyOut);
        if (result != 0) {
            errln("Comparing two strings with sort keys in C failed.\n");
            return;
        }
    }
    
    //TestSurrogates() is ported from cintltst/callcoll.c
    
    /**
    * Tests surrogate support.
    */
    @Test
    public void TestSurrogates() 
    {
        String rules = "&z<'\ud800\udc00'<'\ud800\udc0a\u0308'<A";
        String source[] = {"z",
                           "\uD800\uDC00",
                           "\ud800\udc0a\u0308",
                           "\ud800\udc02"    
        };
        
        String target[] = {"\uD800\uDC00",
                           "\ud800\udc0a\u0308",
                           "A",
                           "\ud800\udc03"    
        };
    
        // this test is to verify the supplementary sort key order in the english 
        // collator
        Collator enCollation;
        try {
            enCollation = Collator.getInstance(Locale.ENGLISH);
        } catch (Exception e) {
            errln("ERROR: Failed to create the collator for ENGLISH");
            return;       
        }
        
        myCollation.setStrength(Collator.TERTIARY);
        int count = 0;
        // logln("start of english collation supplementary characters test\n");
        while (count < 2) {
            doTest(enCollation, source[count], target[count], -1);
            count ++;
        }
        doTest(enCollation, source[count], target[count], 1);
            
        // logln("start of tailored collation supplementary characters test\n");
        count = 0;
        Collator newCollation;
        try {
            newCollation = new RuleBasedCollator(rules);
        } catch (Exception e) {
            errln("ERROR: Failed to create the collator for rules");
            return;       
        }
        
        // tests getting collation elements for surrogates for tailored rules 
        while (count < 4) {
            doTest(newCollation, source[count], target[count], -1);
            count ++;
        }
    
        // tests that \uD801\uDC01 still has the same value, not changed 
        CollationKey enKey = enCollation.getCollationKey(source[3]);
        CollationKey newKey = newCollation.getCollationKey(source[3]);
        int keyResult = enKey.compareTo(newKey);
        if(keyResult != 0) {
            errln("Failed : non-tailored supplementary characters should have the same value\n");
        }
    }

    private static final boolean SUPPORT_VARIABLE_TOP_RELATION = false;
    //TestVariableTop() is ported from cintltst/callcoll.c
    /**
    * Tests the [variable top] tag in rule syntax. Since the default [alternate]
    * tag has the value shifted, any codepoints before [variable top] should give
    * a primary ce of 0.
    */
    @Test
    public void TestVariableTop() {
        /*
         * Starting with ICU 53, setting the variable top via a pseudo relation string
         * is not supported any more.
         * It was replaced by the [maxVariable symbol] setting.
         * See ICU tickets #9958 and #8032.
         */
        if(!SUPPORT_VARIABLE_TOP_RELATION) { return; }
        String rule = "&z = [variable top]";
        Collator  myColl;
        Collator  enColl;
        char[] source = new char[1];
        char ch;
        int expected[] = {0};
    
        try {
            enColl = Collator.getInstance(Locale.ENGLISH);
        } catch (Exception e) {
            errln("ERROR: Failed to create the collator for ENGLISH");
            return;
        }
        
        try{
            myColl = new RuleBasedCollator(rule);
        } catch(Exception e){
            errln("Fail to create RuleBasedCollator with rules:" + rule);
            return;
        }  
        enColl.setStrength(Collator.PRIMARY);
        myColl.setStrength(Collator.PRIMARY);
        
        ((RuleBasedCollator)enColl).setAlternateHandlingShifted(true);
        ((RuleBasedCollator)myColl).setAlternateHandlingShifted(true);
        
        if(((RuleBasedCollator)enColl).isAlternateHandlingShifted() != true) {
            errln("ERROR: ALTERNATE_HANDLING value can not be set to SHIFTED\n");
        }
        
        // space is supposed to be a variable 
        CollationKey key = enColl.getCollationKey(" ");   
        byte[] result = key.toByteArray(); 
        
        for(int i = 0; i < result.length; i++) {
            if(result[i]!= expected[i]) {
                errln("ERROR: SHIFTED alternate does not return 0 for primary of space\n");  
                break;  
            }
        }
        
        ch = 'a';
        while (ch < 'z') {
            source[0] = ch;
            key = myColl.getCollationKey(new String(source));
            result = key.toByteArray();
            
            for(int i = 0; i < result.length; i++) {
                if(result[i]!= expected[i]) {
                    errln("ERROR: SHIFTED alternate does not return 0 for primary of space\n");  
                    break;  
                }
            }
            ch ++;
        }
    }
    
    @Test
    public void TestJB1401() {
        Collator     myCollator = null;
        char[] NFD_UnsafeStartChars = {
            0x0f73,          // Tibetan Vowel Sign II 
            0x0f75,          // Tibetan Vowel Sign UU 
            0x0f81,          // Tibetan Vowel Sign Reversed II 
            0
        };
        int i;
    
        try{
            myCollator = Collator.getInstance(Locale.ENGLISH);
        } catch(Exception e) {
            errln("ERROR: Failed to create the collator for ENGLISH");
            return;
        }
        myCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        for (i=0; ; i++) {
            // Get the next funny character to be tested, and set up the
            // three test strings X, Y, Z, consisting of an A-grave + test char,
            // in original form, NFD, and then NFC form.
            char c = NFD_UnsafeStartChars[i];
            if (c==0) {break;}
            
            String x = "\u00C0" + c;       // \u00C0 is A Grave
            String y;
            String z;
    
            try{
                y = Normalizer.decompose(x, false);
                z = Normalizer.decompose(y, true);
            } catch (Exception e) {
                errln("ERROR: Failed to normalize test of character" + c);
                return;
            }
       
            // Collation test.  All three strings should be equal.
            // doTest does both strcoll and sort keys, with params in both orders.
            doTest(myCollator, x, y, 0);
            doTest(myCollator, x, z, 0);
            doTest(myCollator, y, z, 0); 
    
            // Run collation element iterators over the three strings.  Results should be same for each.
             
            {
                CollationElementIterator ceiX, ceiY, ceiZ;
                int ceX, ceY, ceZ;
                int j;
                try {
                    ceiX = ((RuleBasedCollator)myCollator).getCollationElementIterator(x);
                    ceiY = ((RuleBasedCollator)myCollator).getCollationElementIterator(y);
                    ceiZ = ((RuleBasedCollator)myCollator).getCollationElementIterator(z);
                } catch(Exception e) {
                    errln("ERROR: getCollationElementIterator failed");
                    return;
                }
    
                for (j=0;; j++) {
                    try{
                        ceX = ceiX.next();
                        ceY = ceiY.next();
                        ceZ = ceiZ.next();
                    } catch (Exception e) {
                        errln("ERROR: CollationElementIterator.next failed for iteration " + j);
                        break;
                    }
                  
                    if (ceX != ceY || ceY != ceZ) {
                        errln("ERROR: ucol_next failed for iteration " + j);
                        break;
                    }
                    if (ceX == CollationElementIterator.NULLORDER) {
                        break;
                    }
                }
            }
        }
    }
    
    // main test method called with different strengths,
    // tests comparison of custum collation with different strengths
    
    private void doTest(Collator collation, char[] source, char[] target, int result) {
        String s = new String(source);
        String t = new String(target);
        doTestVariant(collation, s, t, result);
        if(result == -1) {
            doTestVariant(collation, t, s, 1);
        } else if(result == 1) {
            doTestVariant(collation, t, s, -1);
        } else {
            doTestVariant(collation, t, s, 0);
        }
    }
    
    // main test method called with different strengths,
    // tests comparison of custum collation with different strengths
    
    private void doTest(Collator collation,String s, String t, int result) {
        doTestVariant(collation, s, t, result);
        if(result == -1) {
            doTestVariant(collation, t, s, 1);
        } else if(result == 1) {
            doTestVariant(collation, t, s, -1);
        } else {
            doTestVariant(collation, t, s, 0);
        }
    }
    
    private void doTestVariant(Collator collation, String source, String target, int result) {
        int compareResult = collation.compare(source, target);
        CollationKey srckey , tgtkey;
        srckey = collation.getCollationKey(source);
        tgtkey = collation.getCollationKey(target);
        int keyResult = srckey.compareTo(tgtkey);
        if (compareResult != result) {
            errln("String comparison failed in variant test\n");
        }
        if (keyResult != result) {
            errln("Collation key comparison failed in variant test\n");
        }
    }
}