/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2002-2010, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

/** 
 * Port From:   ICU4C v2.1 : Collate/CollationTurkishTest
 * Source File: $ICU4CRoot/source/test/intltest/trcoll.cpp
 **/
 
package android.icu.dev.test.collator;
 
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;
 
public class CollationChineseTest extends TestFmwk{
    public CollationChineseTest() 
    {
    }
    
    @Test
    public void TestPinYin() 
    {
        String seq[] 
            = {"\u963f", "\u554a", "\u54ce", "\u6371", "\u7231", "\u9f98",
               "\u4e5c", "\u8baa", "\u4e42", "\u53c8"};
        RuleBasedCollator collator = null;
        try {
            collator = (RuleBasedCollator)Collator.getInstance(
                                            new Locale("zh", "", "PINYIN"));
        } catch (Exception e) {
            warnln("ERROR: in creation of collator of zh__PINYIN locale");
            return;
        }
        for (int i = 0; i < seq.length - 1; i ++) {
            CollationTest.doTest(this, collator, seq[i], seq[i + 1], -1);
        }
    }
}