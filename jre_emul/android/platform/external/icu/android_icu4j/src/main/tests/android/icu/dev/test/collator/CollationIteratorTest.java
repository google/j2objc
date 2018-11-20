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
 * Port From:   ICU4C v2.1 : collate/CollationIteratorTest
 * Source File: $ICU4CRoot/source/test/intltest/itercoll.cpp
 **/

package android.icu.dev.test.collator;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.lang.UCharacter;
import android.icu.text.CollationElementIterator;
import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;
import android.icu.text.UCharacterIterator;
import android.icu.util.ULocale;

public class CollationIteratorTest extends TestFmwk {
    
    String test1 = "What subset of all possible test cases?";
    String test2 = "has the highest probability of detecting";
   
    /*
     * @bug 4157299
     */
    @Test
    public void TestClearBuffers(/* char* par */) {
        RuleBasedCollator c = null;
        try {
            c = new RuleBasedCollator("&a < b < c & ab = d");
        } catch (Exception e) {
            warnln("Couldn't create a RuleBasedCollator.");
            return;
        }
    
        String source = "abcd";
        CollationElementIterator i = c.getCollationElementIterator(source);
        int e0 = 0;
        try {
            e0 = i.next();    // save the first collation element
        } catch (Exception e) {
            errln("call to i.next() failed.");
            return;
        }
            
        try {
            i.setOffset(3);        // go to the expanding character
        } catch (Exception e) {
            errln("call to i.setOffset(3) failed.");
            return;
        }
        
        try {
            i.next();                // but only use up half of it
        } catch (Exception e) {
            errln("call to i.next() failed.");
            return;
        }
            
        try {
            i.setOffset(0);        // go back to the beginning
        } catch (Exception e) {
            errln("call to i.setOffset(0) failed. ");
        }
        
        int e = 0;
        try {
            e = i.next();    // and get this one again
        } catch (Exception ee) {
            errln("call to i.next() failed. ");
            return;
        }
        
        if (e != e0) {
            errln("got 0x" + Integer.toHexString(e) + ", expected 0x" + Integer.toHexString(e0));
        }
    }
    
    /** @bug 4108762
     * Test for getMaxExpansion()
     */
    @Test
    public void TestMaxExpansion(/* char* par */) {
        int unassigned = 0xEFFFD;
        String rule = "&a < ab < c/aba < d < z < ch";
        RuleBasedCollator coll = null;
        try {
            coll = new RuleBasedCollator(rule);
        } catch (Exception e) {
            warnln("Fail to create RuleBasedCollator");
            return;
        }
        char ch = 0;
        String str = String.valueOf(ch);
    
        CollationElementIterator iter = coll.getCollationElementIterator(str);
    
        while (ch < 0xFFFF) {
            int count = 1;
            ch ++;
            str = String.valueOf(ch);
            iter.setText(str);
            int order = iter.previous();
    
            // thai management 
            if (order == 0) {
                order = iter.previous();
            }
    
            while (iter.previous() != CollationElementIterator.NULLORDER) {
                count ++; 
            }
    
            if (iter.getMaxExpansion(order) < count) {
                errln("Failure at codepoint " + ch + ", maximum expansion count < " + count);
            }
        }
        
        // testing for exact max expansion 
        ch = 0;
        while (ch < 0x61) {
            str = String.valueOf(ch);
            iter.setText(str);
            int order = iter.previous();
            
            if (iter.getMaxExpansion(order) != 1) {
                errln("Failure at codepoint 0x" + Integer.toHexString(ch)
                      + " maximum expansion count == 1");
            }
            ch ++;
        }

        ch = 0x63;
        str = String.valueOf(ch);
        iter.setText(str);
        int temporder = iter.previous();
            
        if (iter.getMaxExpansion(temporder) != 3) {
            errln("Failure at codepoint 0x" + Integer.toHexString(ch)
                                  + " maximum expansion count == 3");
        }

        ch = 0x64;
        str = String.valueOf(ch);
        iter.setText(str);
        temporder = iter.previous();
            
        if (iter.getMaxExpansion(temporder) != 1) {
            errln("Failure at codepoint 0x" + Integer.toHexString(ch)
                                  + " maximum expansion count == 1");
        }

        str = UCharacter.toString(unassigned);
        iter.setText(str);
        temporder = iter.previous();
            
        if (iter.getMaxExpansion(temporder) != 2) {
            errln("Failure at codepoint 0x" + Integer.toHexString(ch)
                                  + " maximum expansion count == 2");
        }


        // testing jamo
        ch = 0x1165;
        str = String.valueOf(ch);
        iter.setText(str);
        temporder = iter.previous();
            
        if (iter.getMaxExpansion(temporder) > 3) {
            errln("Failure at codepoint 0x" + Integer.toHexString(ch)
                                          + " maximum expansion count < 3");
        }

        // testing special jamo &a<\u1165
        rule = "\u0026\u0071\u003c\u1165\u002f\u0071\u0071\u0071\u0071";

        try {
            coll = new RuleBasedCollator(rule);
        } catch (Exception e) {
            errln("Fail to create RuleBasedCollator");
            return;
        }
        iter = coll.getCollationElementIterator(str);
        
        temporder = iter.previous();
            
        if (iter.getMaxExpansion(temporder) != 6) {
            errln("Failure at codepoint 0x" + Integer.toHexString(ch)
                                         + " maximum expansion count == 6");
        }
    }
    
    /**
     * Test for getOffset() and setOffset()
     */
    @Test
    public void TestOffset(/* char* par */) {
        RuleBasedCollator en_us;
        try {
            en_us = (RuleBasedCollator)Collator.getInstance(Locale.US);    
        } catch (Exception e) {
            warnln("ERROR: in creation of collator of ENGLISH locale");
            return;
        }

        CollationElementIterator iter = en_us.getCollationElementIterator(test1);
        // testing boundaries
        iter.setOffset(0);
        if (iter.previous() != CollationElementIterator.NULLORDER) {
            errln("Error: After setting offset to 0, we should be at the end "
                  + "of the backwards iteration");
        }
        iter.setOffset(test1.length());
        if (iter.next() != CollationElementIterator.NULLORDER) {
            errln("Error: After setting offset to the end of the string, we " 
                  + "should be at the end of the forwards iteration");
        }
    
        // Run all the way through the iterator, then get the offset
        int[] orders = CollationTest.getOrders(iter);
        logln("orders.length = " + orders.length);
        
        int offset = iter.getOffset();
    
        if (offset != test1.length()) {
            String msg1 = "offset at end != length: ";
            String msg2 = " vs ";
            errln(msg1 + offset + msg2 + test1.length());
        }
    
        // Now set the offset back to the beginning and see if it works
        CollationElementIterator pristine = en_us.getCollationElementIterator(test1);
        
        try {
            iter.setOffset(0);
        } catch(Exception e) {
            errln("setOffset failed.");
        }
        assertEqual(iter, pristine);
    
        // setting offset in the middle of a contraction
        String contraction = "change";
        RuleBasedCollator tailored = null;
        try {
            tailored = new RuleBasedCollator("& a < ch");
        } catch (Exception e) {
            errln("Error: in creation of Spanish collator");
            return;
        }
        iter = tailored.getCollationElementIterator(contraction);
        int order[] = CollationTest.getOrders(iter);
        iter.setOffset(1); // sets offset in the middle of ch
        int order2[] = CollationTest.getOrders(iter);
        if (!Arrays.equals(order, order2)) {
            errln("Error: setting offset in the middle of a contraction should be the same as setting it to the start of the contraction");
        }
        contraction = "peache";
        iter = tailored.getCollationElementIterator(contraction);
        iter.setOffset(3);
        order = CollationTest.getOrders(iter);
        iter.setOffset(4); // sets offset in the middle of ch
        order2 = CollationTest.getOrders(iter);
        if (!Arrays.equals(order, order2)) {
            errln("Error: setting offset in the middle of a contraction should be the same as setting it to the start of the contraction");
        }
        // setting offset in the middle of a surrogate pair
        String surrogate = "\ud800\udc00str";
        iter = tailored.getCollationElementIterator(surrogate);
        order = CollationTest.getOrders(iter);
        iter.setOffset(1); // sets offset in the middle of surrogate
        order2 = CollationTest.getOrders(iter);
        if (!Arrays.equals(order, order2)) {
            errln("Error: setting offset in the middle of a surrogate pair should be the same as setting it to the start of the surrogate pair");
        }
        surrogate = "simple\ud800\udc00str";
        iter = tailored.getCollationElementIterator(surrogate);
        iter.setOffset(6);
        order = CollationTest.getOrders(iter);
        iter.setOffset(7); // sets offset in the middle of surrogate
        order2 = CollationTest.getOrders(iter);
        if (!Arrays.equals(order, order2)) {
            errln("Error: setting offset in the middle of a surrogate pair should be the same as setting it to the start of the surrogate pair");
        }
        // TODO: try iterating halfway through a messy string.
    }
    
    

    void assertEqual(CollationElementIterator i1, CollationElementIterator i2) {
        int c1, c2, count = 0;
        do {
            c1 = i1.next();
            c2 = i2.next();
            if (c1 != c2) {
                errln("    " + count + ": strength(0x" + 
                    Integer.toHexString(c1) + ") != strength(0x" + Integer.toHexString(c2) + ")");
                break;
            }
            count += 1;
        } while (c1 != CollationElementIterator.NULLORDER);
        CollationTest.backAndForth(this, i1);
        CollationTest.backAndForth(this, i2);
    }
    
    /**
     * Test for CollationElementIterator.previous()
     *
     * @bug 4108758 - Make sure it works with contracting characters
     * 
     */
    @Test
    public void TestPrevious(/* char* par */) {
        RuleBasedCollator en_us = (RuleBasedCollator)Collator.getInstance(Locale.US);
        CollationElementIterator iter = en_us.getCollationElementIterator(test1);
    
        // A basic test to see if it's working at all
        CollationTest.backAndForth(this, iter);
    
        // Test with a contracting character sequence
        String source;
        RuleBasedCollator c1 = null;
        try {
            c1 = new RuleBasedCollator("&a,A < b,B < c,C, d,D < z,Z < ch,cH,Ch,CH");
        } catch (Exception e) {
            errln("Couldn't create a RuleBasedCollator with a contracting sequence.");
            return;
        }
    
        source = "abchdcba";
        iter = c1.getCollationElementIterator(source);
        CollationTest.backAndForth(this, iter);
    
        // Test with an expanding character sequence
        RuleBasedCollator c2 = null;
        try {
            c2 = new RuleBasedCollator("&a < b < c/abd < d");
        } catch (Exception e ) {
            errln("Couldn't create a RuleBasedCollator with an expanding sequence.");
            return;
        }
    
        source = "abcd";
        iter = c2.getCollationElementIterator(source);
        CollationTest.backAndForth(this, iter);
    
        // Now try both
        RuleBasedCollator c3 = null;
        try {
            c3 = new RuleBasedCollator("&a < b < c/aba < d < z < ch");
        } catch (Exception e) {
            errln("Couldn't create a RuleBasedCollator with both an expanding and a contracting sequence.");
            return;
        }
        
        source = "abcdbchdc";
        iter = c3.getCollationElementIterator(source);
        CollationTest.backAndForth(this, iter);
    
        source= "\u0e41\u0e02\u0e41\u0e02\u0e27abc";
        Collator c4 = null;
        try {
            c4 = Collator.getInstance(new Locale("th", "TH", ""));
        } catch (Exception e) {
            errln("Couldn't create a collator");
            return;
        }
        
        iter = ((RuleBasedCollator)c4).getCollationElementIterator(source);
        CollationTest.backAndForth(this, iter);
       
        source= "\u0061\u30CF\u3099\u30FC";
        Collator c5 = null;
        try {
            c5 = Collator.getInstance(new Locale("ja", "JP", ""));
        } catch (Exception e) {
            errln("Couldn't create Japanese collator\n");
            return;
        }
        iter = ((RuleBasedCollator)c5).getCollationElementIterator(source);
        
        CollationTest.backAndForth(this, iter);
    }
    
    
    
    /**
     * Test for setText()
     */
    @Test
    public void TestSetText(/* char* par */) {
        RuleBasedCollator en_us = (RuleBasedCollator)Collator.getInstance(Locale.US);
        CollationElementIterator iter1 = en_us.getCollationElementIterator(test1);
        CollationElementIterator iter2 = en_us.getCollationElementIterator(test2);
    
        // Run through the second iterator just to exercise it
        int c = iter2.next();
        int i = 0;
    
        while ( ++i < 10 && c != CollationElementIterator.NULLORDER) {
            try {
                c = iter2.next();
            } catch (Exception e) {
                errln("iter2.next() returned an error.");
                break;
            }
        }
    
        // Now set it to point to the same string as the first iterator
        try {
            iter2.setText(test1);
        } catch (Exception e) {
            errln("call to iter2->setText(test1) failed.");
            return;
        }
        assertEqual(iter1, iter2);
        
        iter1.reset();
        //now use the overloaded setText(ChracterIterator&, UErrorCode) function to set the text
        CharacterIterator chariter = new StringCharacterIterator(test1);
        try {
            iter2.setText(chariter);
        } catch (Exception e ) {
            errln("call to iter2->setText(chariter(test1)) failed.");
            return;
        }
        assertEqual(iter1, iter2);
        
        iter1.reset();
        //now use the overloaded setText(ChracterIterator&, UErrorCode) function to set the text
        UCharacterIterator uchariter = UCharacterIterator.getInstance(test1);
        try {
            iter2.setText(uchariter);
        } catch (Exception e ) {
            errln("call to iter2->setText(uchariter(test1)) failed.");
            return;
        }
        assertEqual(iter1, iter2);
    }

    /**
     * Test for CollationElementIterator previous and next for the whole set of
     * unicode characters.
     */
    @Test
    public void TestUnicodeChar() {
        RuleBasedCollator en_us = (RuleBasedCollator)Collator.getInstance(Locale.US);
        CollationElementIterator iter;
        char codepoint;
        StringBuffer source = new StringBuffer();
        source.append("\u0e4d\u0e4e\u0e4f");
        // source.append("\u04e8\u04e9");
        iter = en_us.getCollationElementIterator(source.toString());
        // A basic test to see if it's working at all 
        CollationTest.backAndForth(this, iter);
        for (codepoint = 1; codepoint < 0xFFFE;) {
            source.delete(0, source.length());
            while (codepoint % 0xFF != 0) {
                if (UCharacter.isDefined(codepoint)) {
                    source.append(codepoint);
                }
                codepoint ++;
            }
            
            if (UCharacter.isDefined(codepoint)) {
                source.append(codepoint);
            }
            
            if (codepoint != 0xFFFF) {
                codepoint ++;
            }
            /*if (codepoint >= 0x04fc) {
                System.out.println("codepoint " + Integer.toHexString(codepoint));
                String str = source.substring(230, 232);
                System.out.println(android.icu.impl.Utility.escape(str));
                System.out.println("codepoint " + Integer.toHexString(codepoint) 
                                   + "length " + str.length());
                iter = en_us.getCollationElementIterator(str);
                CollationTest.backAndForth(this, iter);
            }
            */
            iter = en_us.getCollationElementIterator(source.toString());
            // A basic test to see if it's working at all 
            CollationTest.backAndForth(this, iter);
        }
    }
    
    /**
     * Test for CollationElementIterator previous and next for the whole set of
     * unicode characters with normalization on.
     */
    @Test
    public void TestNormalizedUnicodeChar()
    {
        // thai should have normalization on
        RuleBasedCollator th_th = null;
        try {
            th_th = (RuleBasedCollator)Collator.getInstance(
                                                       new Locale("th", "TH"));
        } catch (Exception e) {
            warnln("Error creating Thai collator");
            return;
        }
        StringBuffer source = new StringBuffer();
        source.append('\uFDFA');
        CollationElementIterator iter 
                        = th_th.getCollationElementIterator(source.toString());
        CollationTest.backAndForth(this, iter);
        for (char codepoint = 0x1; codepoint < 0xfffe;) {
            source.delete(0, source.length());
            while (codepoint % 0xFF != 0) {
                if (UCharacter.isDefined(codepoint)) {
                    source.append(codepoint);
                }
                codepoint ++;
            }
            
            if (UCharacter.isDefined(codepoint)) {
                source.append(codepoint);
            }
            
            if (codepoint != 0xFFFF) {
                codepoint ++;
            }
            
            /*if (((int)codepoint) >= 0xfe00) {
                String str = source.substring(185, 190);
                System.out.println(android.icu.impl.Utility.escape(str));
                System.out.println("codepoint " 
                                   + Integer.toHexString(codepoint) 
                                   + "length " + str.length());
                iter = th_th.getCollationElementIterator(str);
                CollationTest.backAndForth(this, iter);
            */
            iter = th_th.getCollationElementIterator(source.toString());
            // A basic test to see if it's working at all 
            CollationTest.backAndForth(this, iter);
        }
    }
    
    /**
    * Testing the discontiguous contractions
    */
    @Test
    public void TestDiscontiguous() 
    {
        String rulestr ="&z < AB < X\u0300 < ABC < X\u0300\u0315";
        String src[] = {"ADB", "ADBC", "A\u0315B", "A\u0315BC",
                        // base character blocked
                        "XD\u0300", "XD\u0300\u0315",
                        // non blocking combining character
                        "X\u0319\u0300", "X\u0319\u0300\u0315",
                        // blocking combining character
                        "X\u0314\u0300", "X\u0314\u0300\u0315",
                        // contraction prefix
                        "ABDC", "AB\u0315C","X\u0300D\u0315", 
                        "X\u0300\u0319\u0315", "X\u0300\u031A\u0315",
                        // ends not with a contraction character
                        "X\u0319\u0300D", "X\u0319\u0300\u0315D", 
                        "X\u0300D\u0315D", "X\u0300\u0319\u0315D", 
                        "X\u0300\u031A\u0315D"
        };
        String tgt[] = {// non blocking combining character
                        "A D B", "A D BC", "A \u0315 B", "A \u0315 BC",
                        // base character blocked
                        "X D \u0300", "X D \u0300\u0315",
                        // non blocking combining character
                        "X\u0300 \u0319", "X\u0300\u0315 \u0319",
                        // blocking combining character
                        "X \u0314 \u0300", "X \u0314 \u0300\u0315",
                        // contraction prefix
                        "AB DC", "AB \u0315 C","X\u0300 D \u0315", 
                        "X\u0300\u0315 \u0319", "X\u0300 \u031A \u0315",
                        // ends not with a contraction character
                        "X\u0300 \u0319D", "X\u0300\u0315 \u0319D", 
                        "X\u0300 D\u0315D", "X\u0300\u0315 \u0319D", 
                        "X\u0300 \u031A\u0315D"
        };
        int count = 0;
        try {
            RuleBasedCollator coll = new RuleBasedCollator(rulestr);
            CollationElementIterator iter 
                                        = coll.getCollationElementIterator("");
            CollationElementIterator resultiter 
                                        = coll.getCollationElementIterator("");    
            while (count < src.length) {
                iter.setText(src[count]);
                int s = 0;
                while (s < tgt[count].length()) {
                    int e = tgt[count].indexOf(' ', s);
                    if (e < 0) {
                        e = tgt[count].length();
                    }
                    String resultstr = tgt[count].substring(s, e);
                    resultiter.setText(resultstr);
                    int ce = resultiter.next();
                    while (ce != CollationElementIterator.NULLORDER) {
                        if (ce != iter.next()) {
                            errln("Discontiguos contraction test mismatch at" 
                                  + count);
                            return;
                        }
                        ce = resultiter.next();
                    }
                    s = e + 1;
                }
                iter.reset();
                CollationTest.backAndForth(this, iter);
                count ++;
            }
        }
        catch (Exception e) {
            warnln("Error running discontiguous tests " + e.toString());
        }
    }

    /**
    * Test the incremental normalization
    */
    @Test
    public void TestNormalization()
    {
        String rules = "&a < \u0300\u0315 < A\u0300\u0315 < \u0316\u0315B < \u0316\u0300\u0315";
        String testdata[] = {"\u1ED9", "o\u0323\u0302",
                            "\u0300\u0315", "\u0315\u0300",
                            "A\u0300\u0315B", "A\u0315\u0300B",
                            "A\u0316\u0315B", "A\u0315\u0316B",
                            "\u0316\u0300\u0315", "\u0315\u0300\u0316",
                            "A\u0316\u0300\u0315B", "A\u0315\u0300\u0316B",
                            "\u0316\u0315\u0300", "A\u0316\u0315\u0300B"};
        RuleBasedCollator coll = null;
        try {
            coll = new RuleBasedCollator(rules);
            coll.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        } catch (Exception e) {
            warnln("ERROR: in creation of collator using rules " + rules);
            return;
        }
        
        CollationElementIterator iter = coll.getCollationElementIterator("testing");
        for (int count = 0; count < testdata.length; count ++) {
            iter.setText(testdata[count]);
            CollationTest.backAndForth(this, iter);
        }
    }

    /**
     * TestSearchCollatorElements tests iterator behavior (forwards and backwards) with
     * normalization on AND jamo tailoring, among other things.
     *
     * Note: This test is sensitive to changes of the root collator,
     * for example whether the ae-ligature maps to three CEs (as in the DUCET)
     * or to two CEs (as in the CLDR 24 FractionalUCA.txt).
     * It is also sensitive to how those CEs map to the iterator's 32-bit CE encoding.
     * For example, the DUCET's artificial secondary CE in the ae-ligature
     * may map to two 32-bit iterator CEs (as it did until ICU 52).
     */
    @Test
    public void TestSearchCollatorElements()
    {
        String tsceText =
            " \uAC00" +              // simple LV Hangul
            " \uAC01" +              // simple LVT Hangul
            " \uAC0F" +              // LVTT, last jamo expands for search
            " \uAFFF" +              // LLVVVTT, every jamo expands for search
            " \u1100\u1161\u11A8" +  // 0xAC01 as conjoining jamo
            " \u3131\u314F\u3131" +  // 0xAC01 as compatibility jamo
            " \u1100\u1161\u11B6" +  // 0xAC0F as conjoining jamo; last expands for search
            " \u1101\u1170\u11B6" +  // 0xAFFF as conjoining jamo; all expand for search
            " \u00E6" +              // small letter ae, expands
            " \u1E4D" +              // small letter o with tilde and acute, decomposes
            " ";
        
        int[] rootStandardOffsets = {
            0,  1,2,
            2,  3,4,4,
            4,  5,6,6,
            6,  7,8,8,
            8,  9,10,11,
            12, 13,14,15,
            16, 17,18,19,
            20, 21,22,23,
            24, 25,26,  /* plus another 1-2 offset=26 if ae-ligature maps to three CEs */
            26, 27,28,28,
            28,
            29
        };
        
        int[] rootSearchOffsets = {
            0,  1,2,
            2,  3,4,4,
            4,  5,6,6,6,
            6,  7,8,8,8,8,8,8,
            8,  9,10,11,
            12, 13,14,15,
            16, 17,18,19,20,
            20, 21,22,22,23,23,23,24,
            24, 25,26,  /* plus another 1-2 offset=26 if ae-ligature maps to three CEs */
            26, 27,28,28,
            28,
            29
        };

        class TSCEItem {
            private String localeString;
            private int[] offsets;
            TSCEItem(String locStr, int[] offs) {
                localeString = locStr;
                offsets = offs;
            }
            public String getLocaleString() { return localeString; }
            public int[] getOffsets() { return offsets; }
        }
        final TSCEItem[] tsceItems = { 
            new TSCEItem( "root",                  rootStandardOffsets ),
            new TSCEItem( "root@collation=search", rootSearchOffsets   ),
        };

        for (TSCEItem tsceItem: tsceItems) {
            String localeString = tsceItem.getLocaleString();
            ULocale uloc = new ULocale(localeString);
            RuleBasedCollator col = null;
            try {
                col = (RuleBasedCollator)Collator.getInstance(uloc);
            } catch (Exception e) {
                errln("Error: in locale " + localeString + ", err in Collator.getInstance");
                continue;
            }
            CollationElementIterator uce = col.getCollationElementIterator(tsceText);
            int[] offsets = tsceItem.getOffsets();
            int ioff, noff = offsets.length;
            int offset, element;

            ioff = 0;
            do {
                offset = uce.getOffset();
                element = uce.next();
                logln(String.format("(%s) offset=%2d  ce=%08x\n", tsceItem.localeString, offset, element));
                if (element == 0) {
                    errln("Error: in locale " + localeString + ", CEIterator next() returned element 0");
                }
                if ( ioff < noff ) {
                    if ( offset != offsets[ioff] ) {
                        errln("Error: in locale " + localeString + ", expected CEIterator next()->getOffset " + offsets[ioff] + ", got " + offset);
                        //ioff = noff;
                        //break;
                    }
                    ioff++;
                } else {
                    errln("Error: in locale " + localeString + ", CEIterator next() returned more elements than expected");
                }
            } while (element != CollationElementIterator.NULLORDER);
            if ( ioff < noff ) {
                errln("Error: in locale " + localeString + ", CEIterator next() returned fewer elements than expected");
            }

            // backwards test
            uce.setOffset(tsceText.length());
            ioff = noff;
            do {
                offset = uce.getOffset();
                element = uce.previous();
                if (element == 0) {
                    errln("Error: in locale " + localeString + ", CEIterator previous() returned element 0");
                }
                if ( ioff > 0 ) {
                    ioff--;
                    if ( offset != offsets[ioff] ) {
                        errln("Error: in locale " + localeString + ", expected CEIterator previous()->getOffset " + offsets[ioff] + ", got " + offset);
                        //ioff = 0;
                        //break;
                    }
                } else {
                    errln("Error: in locale " + localeString + ", CEIterator previous() returned more elements than expected");
                }
            } while (element != CollationElementIterator.NULLORDER);
            if ( ioff > 0 ) {
                errln("Error: in locale " + localeString + ", CEIterator previous() returned fewer elements than expected");
            }
        }
    }
}
