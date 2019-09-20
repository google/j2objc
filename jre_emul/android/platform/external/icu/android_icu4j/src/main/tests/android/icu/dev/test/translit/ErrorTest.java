/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.translit;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.ReplaceableString;
import android.icu.text.Transliterator;
import android.icu.text.UnicodeSet;

/**
 * @test
 * @summary Error condition test of Transliterator
 */
public class ErrorTest extends TestFmwk {

    @Test
    public void TestTransliteratorErrors() {
        String trans = "Latin-Greek";
        String bogusID = "LATINGREEK-GREEKLATIN";
        String newID = "Bogus-Latin";
        String newIDRules = "zzz > Z; f <> ph";
        String bogusRules = "a } [b-g m-p ";
        ReplaceableString testString =
            new ReplaceableString("A quick fox jumped over the lazy dog.");
        String insertString = "cats and dogs";
        int stoppedAt = 0, len;
        Transliterator.Position pos = new Transliterator.Position();

        Transliterator t =
            Transliterator.getInstance(trans, Transliterator.FORWARD);
        if (t == null) {
            errln("FAIL: construction of Latin-Greek");
            return;
        }
        len = testString.length();
        stoppedAt = t.transliterate(testString, 0, 100);
        if (stoppedAt != -1) {
            errln("FAIL: Out of bounds check failed (1).");
        } else if (testString.length() != len) {
            testString =
                new ReplaceableString("A quick fox jumped over the lazy dog.");
            errln("FAIL: Transliterate fails and the target string was modified.");
        }
        stoppedAt = t.transliterate(testString, 100, testString.length() - 1);
        if (stoppedAt != -1) {
            errln("FAIL: Out of bounds check failed (2).");
        } else if (testString.length() != len) {
            testString =
                new ReplaceableString("A quick fox jumped over the lazy dog.");
            errln("FAIL: Transliterate fails and the target string was modified.");
        }
        pos.start = 100;
        pos.limit = testString.length();
        try {
            t.transliterate(testString, pos);
            errln("FAIL: Start offset is out of bounds, error not reported.");
        } catch (IllegalArgumentException e) {
            logln("Start offset is out of bounds and detected.");
        }
        pos.limit = 100;
        pos.start = 0;

        try {
            t.transliterate(testString, pos);
            errln("FAIL: Limit offset is out of bounds, error not reported.\n");
        } catch (IllegalArgumentException e) {
            logln("Start offset is out of bounds and detected.");
        }
        len = pos.contextLimit = testString.length();
        pos.contextStart = 0;
        pos.limit = len - 1;
        pos.start = 5;
        try {
            t.transliterate(testString, pos, insertString);
            if (len == pos.limit) {
                errln("FAIL: Test insertion with string: the transliteration position limit didn't change as expected.");
            }
        } catch (IllegalArgumentException e) {
            errln("Insertion test with string failed for some reason.");
        }
        pos.contextStart = 0;
        pos.contextLimit = testString.length();
        pos.limit = testString.length() - 1;
        pos.start = 5;
        try {
            t.transliterate(testString, pos, 0x0061);
            if (len == pos.limit) {
                errln("FAIL: Test insertion with character: the transliteration position limit didn't change as expected.");
            }
        } catch (IllegalArgumentException e) {
            errln("FAIL: Insertion test with UTF-16 code point failed for some reason.");
        }
        len = pos.limit = testString.length();
        pos.contextStart = 0;
        pos.contextLimit = testString.length() - 1;
        pos.start = 5;
        try {
            t.transliterate(testString, pos, insertString);
            errln("FAIL: Out of bounds check failed (3).");
            if (testString.length() != len) {
                errln("FAIL: The input string was modified though the offsets were out of bounds.");
            }
        } catch (IllegalArgumentException e) {
            logln("Insertion test with out of bounds indexes.");
        }
        Transliterator t1 = null;
        try {
            t1 = Transliterator.getInstance(bogusID, Transliterator.FORWARD);
            if (t1 != null) {
                errln("FAIL: construction of bogus ID \"LATINGREEK-GREEKLATIN\"");
            }
        } catch (IllegalArgumentException e) {
        }

        //try { // unneeded - Exception cannot be thrown
        Transliterator t2 =
            Transliterator.createFromRules(
                newID,
                newIDRules,
                Transliterator.FORWARD);
        try {
            Transliterator t3 = t2.getInverse();
            errln("FAIL: The newID transliterator was not registered so createInverse should fail.");
            if (t3 != null) {
                errln("FAIL: The newID transliterator was not registered so createInverse should fail.");
            }
        } catch (Exception e) {
        }
        //} catch (Exception e) { }
        try {
            Transliterator t4 =
                Transliterator.createFromRules(
                    newID,
                    bogusRules,
                    Transliterator.FORWARD);
            if (t4 != null) {
                errln("FAIL: The rules is malformed but error was not reported.");
            }
        } catch (Exception e) {
        }
    }

    @Test
    public void TestUnicodeSetErrors() {
        String badPattern = "[[:L:]-[0x0300-0x0400]";
        UnicodeSet set = new UnicodeSet();
        //String result;

        if (!set.isEmpty()) {
            errln("FAIL: The default ctor of UnicodeSet created a non-empty object.");
        }
        try {
            set.applyPattern(badPattern);
            errln("FAIL: Applied a bad pattern to the UnicodeSet object okay.");
        } catch (IllegalArgumentException e) {
            logln("Test applying with the bad pattern.");
        }
        try {
            new UnicodeSet(badPattern);
            errln("FAIL: Created a UnicodeSet based on bad patterns.");
        } catch (IllegalArgumentException e) {
            logln("Test constructing with the bad pattern.");
        }
    }

//    public void TestUniToHexErrors() {
//        Transliterator t = null;
//        try {
//            t = new UnicodeToHexTransliterator("", true, null);
//            if (t != null) {
//                errln("FAIL: Created a UnicodeToHexTransliterator with an empty pattern.");
//            }
//        } catch (IllegalArgumentException e) {
//        }
//        try {
//            t = new UnicodeToHexTransliterator("\\x", true, null);
//            if (t != null) {
//                errln("FAIL: Created a UnicodeToHexTransliterator with a bad pattern.");
//            }
//        } catch (IllegalArgumentException e) {
//        }
//        t = new UnicodeToHexTransliterator();
//        try {
//            ((UnicodeToHexTransliterator) t).applyPattern("\\x");
//            errln("FAIL: UnicodeToHexTransliterator::applyPattern succeeded with a bad pattern.");
//        } catch (Exception e) {
//        }
//    }

    @Test
    public void TestRBTErrors() {

        String rules = "ab>y";
        String id = "MyRandom-YReverse";
        String goodPattern = "[[:L:]&[\\u0000-\\uFFFF]]"; /* all BMP letters */
        UnicodeSet set = null;
        try {
            set = new UnicodeSet(goodPattern);
            try {
                Transliterator t =
                    Transliterator.createFromRules(id, rules, Transliterator.REVERSE);
                t.setFilter(set);
                Transliterator.registerClass(id, t.getClass(), null);
                Transliterator.unregister(id);
                try {
                    Transliterator.getInstance(id, Transliterator.REVERSE);
                    errln("FAIL: construction of unregistered ID should have failed.");
                } catch (IllegalArgumentException e) {
                }
            } catch (IllegalArgumentException e) {
                errln("FAIL: Was not able to create a good RBT to test registration.");
            }
        } catch (IllegalArgumentException e) {
            errln("FAIL: Was not able to create a good UnicodeSet based on valid patterns.");
            return;
        }
    }

//    public void TestHexToUniErrors() {
//        Transliterator t = null;
//        //try { // unneeded - exception cannot be thrown
//        t = new HexToUnicodeTransliterator("", null);
//        //} catch (Exception e) {
//        //    errln("FAIL: Could not create a HexToUnicodeTransliterator with an empty pattern.");
//        //}
//        try {
//            t = new HexToUnicodeTransliterator("\\x", null);
//            errln("FAIL: Created a HexToUnicodeTransliterator with a bad pattern.");
//        } catch (IllegalArgumentException e) {
//        }
//
//        t = new HexToUnicodeTransliterator();
//        try {
//            ((HexToUnicodeTransliterator) t).applyPattern("\\x");
//            errln("FAIL: HexToUnicodeTransliterator::applyPattern succeeded with a bad pattern.");
//        } catch (IllegalArgumentException e) {
//        }
//    }
}
