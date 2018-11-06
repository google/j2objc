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
 * Port From:   ICU4C v2.1 : Collate/CollationEnglishTest
 * Source File: $ICU4CRoot/source/test/intltest/encoll.cpp
 **/
 
package android.icu.dev.test.collator;
 
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
 
public class CollationEnglishTest extends TestFmwk{
    private static char[][] testSourceCases = {
        {0x0061 /* 'a' */, 0x0062 /* 'b' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x002D /* '-' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x0020 /* ' ' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x002D /* '-' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */},
        {0x0048 /* 'H' */, 0x0065 /* 'e' */, 0x006C /* 'l' */, 0x006C /* 'l' */, 0x006F /* 'o' */},
        {0x0041 /* 'A' */, 0x0042 /* 'B' */, 0x0043 /* 'C' */}, 
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x002D /* '-' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x002D /* '-' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */},
        {0x0070 /* 'p' */, 0x00EA, 0x0063 /* 'c' */, 0x0068 /* 'h' */, 0x0065 /* 'e' */},                                            
        {0x0070 /* 'p' */, 0x00E9, 0x0063 /* 'c' */, 0x0068 /* 'h' */, 0x00E9},
        {0x00C4, 0x0042 /* 'B' */, 0x0308, 0x0043 /* 'C' */, 0x0308},
        {0x0061 /* 'a' */, 0x0308, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0070 /* 'p' */, 0x00E9, 0x0063 /* 'c' */, 0x0068 /* 'h' */, 0x0065 /* 'e' */, 0x0072 /* 'r' */},
        {0x0072 /* 'r' */, 0x006F /* 'o' */, 0x006C /* 'l' */, 0x0065 /* 'e' */, 0x0073 /* 's' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0041 /* 'A' */},
        {0x0041 /* 'A' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */},                                                                
        {0x0074 /* 't' */, 0x0063 /* 'c' */, 0x006F /* 'o' */, 0x006D /* 'm' */, 0x0070 /* 'p' */, 0x0061 /* 'a' */, 0x0072 /* 'r' */, 0x0065 /* 'e' */, 0x0070 /* 'p' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0069 /* 'i' */, 0x006E /* 'n' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */}, 
        {0x0061 /* 'a' */, 0x0023 /* '#' */, 0x0062 /* 'b' */},
        {0x0061 /* 'a' */, 0x0023 /* '#' */, 0x0062 /* 'b' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0041 /* 'A' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */, 0x0064 /* 'd' */, 0x0061 /* 'a' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */, 0x0064 /* 'd' */, 0x0061 /* 'a' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */, 0x0064 /* 'd' */, 0x0061 /* 'a' */},
        {0x00E6, 0x0062 /* 'b' */, 0x0063 /* 'c' */, 0x0064 /* 'd' */, 0x0061 /* 'a' */},
        {0x00E4, 0x0062 /* 'b' */, 0x0063 /* 'c' */, 0x0064 /* 'd' */, 0x0061 /* 'a' */},                                            
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x0048 /* 'H' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0308, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0074 /* 't' */, 0x0068 /* 'h' */, 0x0069 /* 'i' */, 0x0302, 0x0073 /* 's' */},
        {0x0070 /* 'p' */, 0x00EA, 0x0063 /* 'c' */, 0x0068 /* 'h' */, 0x0065 /* 'e' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},                                                         
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x00E6, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x00E6, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},               
        {0x0070 /* 'p' */, 0x00E9, 0x0063 /* 'c' */, 0x0068 /* 'h' */, 0x00E9}                                            // 49
    };
    
    private static char[][] testTargetCases = {
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x002D /* '-' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */},
        {0x0068 /* 'h' */, 0x0065 /* 'e' */, 0x006C /* 'l' */, 0x006C /* 'l' */, 0x006F /* 'o' */},
        {0x0041 /* 'A' */, 0x0042 /* 'B' */, 0x0043 /* 'C' */},
        {0x0041 /* 'A' */, 0x0042 /* 'B' */, 0x0043 /* 'C' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */, 0x0073 /* 's' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */, 0x0073 /* 's' */},
        {0x0062 /* 'b' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0063 /* 'c' */, 0x006B /* 'k' */, 0x0062 /* 'b' */, 0x0069 /* 'i' */, 0x0072 /* 'r' */, 0x0064 /* 'd' */},                             
        {0x0070 /* 'p' */, 0x00E9, 0x0063 /* 'c' */, 0x0068 /* 'h' */, 0x00E9},
        {0x0070 /* 'p' */, 0x00E9, 0x0063 /* 'c' */, 0x0068 /* 'h' */, 0x0065 /* 'e' */, 0x0072 /* 'r' */},
        {0x00C4, 0x0042 /* 'B' */, 0x0308, 0x0043 /* 'C' */, 0x0308},
        {0x0041 /* 'A' */, 0x0308, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0070 /* 'p' */, 0x00E9, 0x0063 /* 'c' */, 0x0068 /* 'h' */, 0x0065 /* 'e' */},
        {0x0072 /* 'r' */, 0x006F /* 'o' */, 0x0302, 0x006C /* 'l' */, 0x0065 /* 'e' */},
        {0x0041 /* 'A' */, 0x00E1, 0x0063 /* 'c' */, 0x0064 /* 'd' */},
        {0x0041 /* 'A' */, 0x00E1, 0x0063 /* 'c' */, 0x0064 /* 'd' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},                                                             
        {0x0054 /* 'T' */, 0x0043 /* 'C' */, 0x006F /* 'o' */, 0x006D /* 'm' */, 0x0070 /* 'p' */, 0x0061 /* 'a' */, 0x0072 /* 'r' */, 0x0065 /* 'e' */, 0x0050 /* 'P' */, 0x006C /* 'l' */, 0x0061 /* 'a' */, 0x0069 /* 'i' */, 0x006E /* 'n' */},
        {0x0061 /* 'a' */, 0x0042 /* 'B' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0023 /* '#' */, 0x0042 /* 'B' */},
        {0x0061 /* 'a' */, 0x0026 /* '&' */, 0x0062 /* 'b' */},
        {0x0061 /* 'a' */, 0x0023 /* '#' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */, 0x0064 /* 'd' */, 0x0061 /* 'a' */},
        {0x00C4, 0x0062 /* 'b' */, 0x0063 /* 'c' */, 0x0064 /* 'd' */, 0x0061 /* 'a' */},
        {0x00E4, 0x0062 /* 'b' */, 0x0063 /* 'c' */, 0x0064 /* 'd' */, 0x0061 /* 'a' */},
        {0x00C4, 0x0062 /* 'b' */, 0x0063 /* 'c' */, 0x0064 /* 'd' */, 0x0061 /* 'a' */},
        {0x00C4, 0x0062 /* 'b' */, 0x0063 /* 'c' */, 0x0064 /* 'd' */, 0x0061 /* 'a' */},                                             
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0023 /* '#' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x003D /* '=' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0064 /* 'd' */},
        {0x00E4, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0043 /* 'C' */, 0x0048 /* 'H' */, 0x0063 /* 'c' */},
        {0x00E4, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0074 /* 't' */, 0x0068 /* 'h' */, 0x00EE, 0x0073 /* 's' */},
        {0x0070 /* 'p' */, 0x00E9, 0x0063 /* 'c' */, 0x0068 /* 'h' */, 0x00E9},
        {0x0061 /* 'a' */, 0x0042 /* 'B' */, 0x0043 /* 'C' */},                                                          
        {0x0061 /* 'a' */, 0x0062 /* 'b' */, 0x0064 /* 'd' */},
        {0x00E4, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x00C6, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0042 /* 'B' */, 0x0064 /* 'd' */},
        {0x00E4, 0x0062 /* 'b' */, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x00C6, 0x0063 /* 'c' */},
        {0x0061 /* 'a' */, 0x0042 /* 'B' */, 0x0064 /* 'd' */},
        {0x00E4, 0x0062 /* 'b' */, 0x0063 /* 'c' */},          
        {0x0070 /* 'p' */, 0x00EA, 0x0063 /* 'c' */, 0x0068 /* 'h' */, 0x0065 /* 'e' */}
    };                                           // 49

    private static int[] results = {
    //-1:LESS; 0:EQUAL; 1:GREATER
        -1, 
        -1, /*Collator::GREATER,*/
        -1,
        1,
        1,
        0,
        -1,
        -1,
        -1,
        -1, /*Collator::GREATER,*/                                                          /* 10 */
        1,
        -1,
        0,
        -1,
        1,
        1,
        1,
        -1,
        -1,
        -1,                                                             /* 20 */
        -1,
        -1,
        -1,
        1,
        1,
        1,
        /* Test Tertiary  > 26 */
        -1,
        -1,
        1,
        -1,                                                             /* 30 */
        1,
        0,
        1,
        -1,
        -1,
        -1,
        /* test identical > 36 */
        0,
        0,
        /* test primary > 38 */
        0,
        0,                                                            /* 40 */
        -1,
        0,
        0,
        /* test secondary > 43 */
        -1,
        -1,
        0,
        -1,
        -1, 
        -1                                                                  // 49
    };

    private static char [][] testBugs = {
        {0x61},
        {0x41},
        {0x65},
        {0x45},
        {0x00e9},
        {0x00e8},
        {0x00ea},
        {0x00eb},
        {0x65, 0x61},
        {0x78}
    };

    // 0x0300 is grave, 0x0301 is acute
    // the order of elements in this array must be different than the order in CollationFrenchTest
    private static char[][] testAcute = {
        {0x65, 0x65},
        {0x65, 0x65, 0x0301},
        {0x65, 0x65, 0x0301, 0x0300},
        {0x65, 0x65, 0x0300},
        {0x65, 0x65, 0x0300, 0x0301},
        {0x65, 0x0301, 0x65},
        {0x65, 0x0301, 0x65, 0x0301},
        {0x65, 0x0301, 0x65, 0x0301, 0x0300},
        {0x65, 0x0301, 0x65, 0x0300},
        {0x65, 0x0301, 0x65, 0x0300, 0x0301},
        {0x65, 0x0301, 0x0300, 0x65},
        {0x65, 0x0301, 0x0300, 0x65, 0x0301},
        {0x65, 0x0301, 0x0300, 0x65, 0x0301, 0x0300},
        {0x65, 0x0301, 0x0300, 0x65, 0x0300},
        {0x65, 0x0301, 0x0300, 0x65, 0x0300, 0x0301},
        {0x65, 0x0300, 0x65},
        {0x65, 0x0300, 0x65, 0x0301},
        {0x65, 0x0300, 0x65, 0x0301, 0x0300},
        {0x65, 0x0300, 0x65, 0x0300},
        {0x65, 0x0300, 0x65, 0x0300, 0x0301},
        {0x65, 0x0300, 0x0301, 0x65},
        {0x65, 0x0300, 0x0301, 0x65, 0x0301},
        {0x65, 0x0300, 0x0301, 0x65, 0x0301, 0x0300},
        {0x65, 0x0300, 0x0301, 0x65, 0x0300},
        {0x65, 0x0300, 0x0301, 0x65, 0x0300, 0x0301}
    };

    private static char[][] testMore = {
        {0x0061 /* 'a' */, 0x0065 /* 'e' */},
        { 0x00E6},
        { 0x00C6},
        {0x0061 /* 'a' */, 0x0066 /* 'f' */},
        {0x006F /* 'o' */, 0x0065 /* 'e' */},
        { 0x0153},
        { 0x0152},
        {0x006F /* 'o' */, 0x0066 /* 'f' */},
    };
    
    private Collator myCollation = null;
    
    public CollationEnglishTest() {
    }
    
    @Before
    public void init()throws Exception {
        myCollation = Collator.getInstance(Locale.ENGLISH);
    }
    
    //performs test with strength PRIMARY
    @Test
    public void TestPrimary() {
        int i;
        myCollation.setStrength(Collator.PRIMARY);
        for (i = 38; i < 43 ; i++) {
            doTest(testSourceCases[i], testTargetCases[i], results[i]);
        }  
    }
    
    //perform test with strength SECONDARY
    @Test
    public void TestSecondary() {
        int i;
        myCollation.setStrength(Collator.SECONDARY);
        for (i = 43; i < 49 ; i++) {
            doTest(testSourceCases[i], testTargetCases[i], results[i]);
        }

        //test acute and grave ordering (compare to french collation)
        int j;
        int expected;
        for (i = 0; i < testAcute.length; i++) {
            for (j = 0; j < testAcute.length; j++) {
                logln("i = " + i + "; j = " + j);
                if (i <  j)
                    expected = -1;
                else if (i == j)
                    expected = 0;
                else // (i >  j)
                    expected = 1;
                doTest(testAcute[i], testAcute[j], expected );
            }
        }
    }
    
    //perform test with strength TERTIARY
    @Test
    public void TestTertiary() {
        int i = 0;
        myCollation.setStrength(Collator.TERTIARY);
        //for (i = 0; i < 38 ; i++)  //attention: there is something wrong with 36, 37.
        for (i = 0; i < 38 ; i++)
        {
            doTest(testSourceCases[i], testTargetCases[i], results[i]);
        } 

        int j = 0;
        for (i = 0; i < 10; i++)
        {
            for (j = i+1; j < 10; j++)
            {
                doTest(testBugs[i], testBugs[j], -1);
            }
        }

        //test more interesting cases
        int expected;
        for (i = 0; i < testMore.length; i++)
        {
            for (j = 0; j < testMore.length; j++)
            {
                if (i <  j)
                    expected = -1;
                else if (i == j)
                    expected = 0;
                else // (i >  j)
                    expected = 1;
                doTest(testMore[i], testMore[j], expected );
            }
        }
    }
    
   // main test routine, tests rules defined by the "en" locale
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
