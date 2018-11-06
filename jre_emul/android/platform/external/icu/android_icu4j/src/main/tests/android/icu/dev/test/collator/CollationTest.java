/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 * CollationTest.java, ported from collationtest.cpp
 * C++ version created on: 2012apr27
 * created by: Markus W. Scherer
 */
package android.icu.dev.test.collator;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.impl.Norm2AllModes;
import android.icu.impl.Utility;
import android.icu.impl.coll.Collation;
import android.icu.impl.coll.CollationData;
import android.icu.impl.coll.CollationFCD;
import android.icu.impl.coll.CollationIterator;
import android.icu.impl.coll.CollationRoot;
import android.icu.impl.coll.CollationRootElements;
import android.icu.impl.coll.CollationRuleParser;
import android.icu.impl.coll.CollationWeights;
import android.icu.impl.coll.FCDIterCollationIterator;
import android.icu.impl.coll.FCDUTF16CollationIterator;
import android.icu.impl.coll.UTF16CollationIterator;
import android.icu.impl.coll.UVector32;
import android.icu.text.CollationElementIterator;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
import android.icu.text.Collator.ReorderCodes;
import android.icu.text.Normalizer2;
import android.icu.text.RawCollationKey;
import android.icu.text.RuleBasedCollator;
import android.icu.text.UCharacterIterator;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;
import android.icu.util.IllformedLocaleException;
import android.icu.util.Output;
import android.icu.util.ULocale;

public class CollationTest extends TestFmwk {
    public CollationTest() {
    }

    // Fields
    Normalizer2 fcd, nfd;
    Collator coll;
    String fileLine;
    int fileLineNumber;
    String fileTestName;

    // package private methods ----------------------------------------------
    
    static void doTest(TestFmwk test, RuleBasedCollator col, String source, 
                       String target, int result)
    {
        doTestVariant(test, col, source, target, result);
        if (result == -1) {
            doTestVariant(test, col, target, source, 1);
        } 
        else if (result == 1) {
            doTestVariant(test, col, target, source, -1);
        }
        else {
            doTestVariant(test, col, target, source, 0);
        }

        CollationElementIterator iter = col.getCollationElementIterator(source);
        backAndForth(test, iter);
        iter.setText(target);
        backAndForth(test, iter);
    }
    
    /**
     * Return an integer array containing all of the collation orders
     * returned by calls to next on the specified iterator
     */
    static int[] getOrders(CollationElementIterator iter) 
    {
        int maxSize = 100;
        int size = 0;
        int[] orders = new int[maxSize];
        
        int order;
        while ((order = iter.next()) != CollationElementIterator.NULLORDER) {
            if (size == maxSize) {
                maxSize *= 2;
                int[] temp = new int[maxSize];
                System.arraycopy(orders, 0, temp,  0, size);
                orders = temp;
            }
            orders[size++] = order;
        }
        
        if (maxSize > size) {
            int[] temp = new int[size];
            System.arraycopy(orders, 0, temp,  0, size);
            orders = temp;
        }
        return orders;
    }
    
    static void backAndForth(TestFmwk test, CollationElementIterator iter) 
    {
        // Run through the iterator forwards and stick it into an array
        iter.reset();
        int[] orders = getOrders(iter);
    
        // Now go through it backwards and make sure we get the same values
        int index = orders.length;
        int o;
    
        // reset the iterator
        iter.reset();
    
        while ((o = iter.previous()) != CollationElementIterator.NULLORDER) {
            if (o != orders[--index]) {
                if (o == 0) {
                    index ++;
                } else {
                    while (index > 0 && orders[index] == 0) {
                        index --;
                    } 
                    if (o != orders[index]) {
                        TestFmwk.errln("Mismatch at index " + index + ": 0x" 
                            + Utility.hex(orders[index]) + " vs 0x" + Utility.hex(o));
                        break;
                    }
                }
            }
        }
    
        while (index != 0 && orders[index - 1] == 0) {
          index --;
        }
    
        if (index != 0) {
            String msg = "Didn't get back to beginning - index is ";
            TestFmwk.errln(msg + index);
    
            iter.reset();
            TestFmwk.err("next: ");
            while ((o = iter.next()) != CollationElementIterator.NULLORDER) {
                String hexString = "0x" + Utility.hex(o) + " ";
                TestFmwk.err(hexString);
            }
            TestFmwk.errln("");
            TestFmwk.err("prev: ");
            while ((o = iter.previous()) != CollationElementIterator.NULLORDER) {
                String hexString = "0x" + Utility.hex(o) + " ";
                 TestFmwk.err(hexString);
            }
            TestFmwk.errln("");
        }
    }
    
    static final String appendCompareResult(int result, String target){
        if (result == -1) {
            target += "LESS";
        } else if (result == 0) {
            target += "EQUAL";
        } else if (result == 1) {
            target += "GREATER";
        } else {
            String huh = "?";
            target += huh + result;
        }
        return target;
    }

    static final String prettify(CollationKey key) {
        byte[] bytes = key.toByteArray();
        return prettify(bytes, bytes.length);
    }

    static final String prettify(RawCollationKey key) {
        return prettify(key.bytes, key.size);
    }

    static final String prettify(byte[] skBytes, int length) {
        StringBuilder target = new StringBuilder(length * 3 + 2).append('[');
    
        for (int i = 0; i < length; i++) {
            String numStr = Integer.toHexString(skBytes[i] & 0xff);
            if (numStr.length() < 2) {
                target.append('0');
            }
            target.append(numStr).append(' ');
        }
        target.append(']');
        return target.toString();
    }

    private static void doTestVariant(TestFmwk test, 
                                      RuleBasedCollator myCollation,
                                      String source, String target, int result)
    {
        int compareResult  = myCollation.compare(source, target);
        if (compareResult != result) {
            
            // !!! if not mod build, error, else nothing.
            // warnln if not build, error, else always print warning.
            // do we need a 'quiet warning?' (err or log).  Hmmm,
            // would it work to have the 'verbose' flag let you 
            // suppress warnings?  Are there ever some warnings you
            // want to suppress, and others you don't?
            TestFmwk.errln("Comparing \"" + Utility.hex(source) + "\" with \""
                    + Utility.hex(target) + "\" expected " + result
                    + " but got " + compareResult);
        }
        CollationKey ssk = myCollation.getCollationKey(source);
        CollationKey tsk = myCollation.getCollationKey(target);
        compareResult = ssk.compareTo(tsk);
        if (compareResult != result) {
            TestFmwk.errln("Comparing CollationKeys of \"" + Utility.hex(source) 
            + "\" with \"" + Utility.hex(target) 
            + "\" expected " + result + " but got " 
            + compareResult);
        }
        RawCollationKey srsk = new RawCollationKey();
        myCollation.getRawCollationKey(source, srsk);
        RawCollationKey trsk = new RawCollationKey();
        myCollation.getRawCollationKey(target, trsk);
        compareResult = ssk.compareTo(tsk);
        if (compareResult != result) {
            TestFmwk.errln("Comparing RawCollationKeys of \"" 
                    + Utility.hex(source) 
                    + "\" with \"" + Utility.hex(target) 
                    + "\" expected " + result + " but got " 
                    + compareResult);
        }
    }

    @Test
    public void TestMinMax() {
        setRootCollator();
        RuleBasedCollator rbc = (RuleBasedCollator)coll;

        final String s = "\uFFFE\uFFFF";
        long[] ces;
 
        ces = rbc.internalGetCEs(s);
        if (ces.length != 2) {
            errln("expected 2 CEs for <FFFE, FFFF>, got " + ces.length);
            return;
        }

        long ce = ces[0];
        long expected = Collation.makeCE(Collation.MERGE_SEPARATOR_PRIMARY);
        if (ce != expected) {
            errln("CE(U+fffe)=0x" + Utility.hex(ce) + " != 02..");
        }

        ce = ces[1];
        expected = Collation.makeCE(Collation.MAX_PRIMARY);
        if (ce != expected) {
            errln("CE(U+ffff)=0x" + Utility.hex(ce) + " != max..");
        }
    }

    @Test
    public void TestImplicits() {
        CollationData cd = CollationRoot.getData();

        // Implicit primary weights should be assigned for the following sets,
        // and sort in ascending order by set and then code point.
        // See http://www.unicode.org/reports/tr10/#Implicit_Weights
        // core Han Unified Ideographs
        UnicodeSet coreHan = new UnicodeSet("[\\p{unified_ideograph}&"
                                 + "[\\p{Block=CJK_Unified_Ideographs}"
                                 + "\\p{Block=CJK_Compatibility_Ideographs}]]");
        // all other Unified Han ideographs
        UnicodeSet otherHan = new UnicodeSet("[\\p{unified ideograph}-"
                                 + "[\\p{Block=CJK_Unified_Ideographs}"
                                 + "\\p{Block=CJK_Compatibility_Ideographs}]]");

        UnicodeSet unassigned = new UnicodeSet("[[:Cn:][:Cs:][:Co:]]");
        unassigned.remove(0xfffe, 0xffff);  // These have special CLDR root mappings.

        // Starting with CLDR 26/ICU 54, the root Han order may instead be
        // the Unihan radical-stroke order.
        // The tests should pass either way, so we only test the order of a small set of Han characters
        // whose radical-stroke order is the same as their code point order.
        UnicodeSet someHanInCPOrder = new UnicodeSet(
                "[\\u4E00-\\u4E16\\u4E18-\\u4E2B\\u4E2D-\\u4E3C\\u4E3E-\\u4E48" +
                "\\u4E4A-\\u4E60\\u4E63-\\u4E8F\\u4E91-\\u4F63\\u4F65-\\u50F1\\u50F3-\\u50F6]");
        UnicodeSet inOrder = new UnicodeSet(someHanInCPOrder);
        inOrder.addAll(unassigned).freeze();

        UnicodeSet[] sets = { coreHan, otherHan, unassigned };
        int prev = 0;
        long prevPrimary = 0;
        UTF16CollationIterator ci = new UTF16CollationIterator(cd, false, "", 0);
        for (int i = 0; i < sets.length; ++i) {
            UnicodeSetIterator iter = new UnicodeSetIterator(sets[i]);
            while (iter.next()) {
                String s = iter.getString();
                int c = s.codePointAt(0);
                ci.setText(false, s, 0);
                long ce = ci.nextCE();
                long ce2 = ci.nextCE();
                if (ce == Collation.NO_CE || ce2 != Collation.NO_CE) {
                    errln("CollationIterator.nextCE(0x" + Utility.hex(c)
                            + ") did not yield exactly one CE");
                    continue;

                }
                if ((ce & 0xffffffffL) != Collation.COMMON_SEC_AND_TER_CE) {
                    errln("CollationIterator.nextCE(U+" + Utility.hex(c, 4)
                            + ") has non-common sec/ter weights: 0x" + Utility.hex(ce & 0xffffffffL, 8));
                    continue;
                }
                long primary = ce >>> 32;
                if (!(primary > prevPrimary) && inOrder.contains(c) && inOrder.contains(prev)) {
                    errln("CE(U+" + Utility.hex(c) + ")=0x" + Utility.hex(primary)
                            + ".. not greater than CE(U+" + Utility.hex(prev)
                            + ")=0x" + Utility.hex(prevPrimary) + "..");

                }
                prev = c;
                prevPrimary = primary;
            }
        }
    }

    // ICU4C: TestNulTerminated / renamed for ICU4J
    @Test
    public void TestSubSequence() {
        CollationData data = CollationRoot.getData();
        final String s = "abab"; // { 0x61, 0x62, 0x61, 0x62 }

        UTF16CollationIterator ci1 = new UTF16CollationIterator(data, false, s, 0);
        UTF16CollationIterator ci2 = new UTF16CollationIterator(data, false, s, 2);

        for (int i = 0; i < 2; ++i) {
            long ce1 = ci1.nextCE();
            long ce2 = ci2.nextCE();

            if (ce1 != ce2) {
                errln("CollationIterator.nextCE(with start position at 0) != "
                      + "nextCE(with start position at 2) at CE " + i);
            }
        }
    }

    
    // ICU4C: TestIllegalUTF8 / not applicable to ICU4J


    private static void addLeadSurrogatesForSupplementary(UnicodeSet src, UnicodeSet dest) {
        for(int c = 0x10000; c < 0x110000;) {
            int next = c + 0x400;
            if(src.containsSome(c, next - 1)) {
                dest.add(UTF16.getLeadSurrogate(c));
            }
            c = next;
        }
    }

    @Test
    public void TestShortFCDData() {
        UnicodeSet expectedLccc = new UnicodeSet("[:^lccc=0:]");
        expectedLccc.add(0xdc00, 0xdfff);   // add all trail surrogates
        addLeadSurrogatesForSupplementary(expectedLccc, expectedLccc);

        UnicodeSet lccc = new UnicodeSet(); // actual
        for (int c = 0; c <= 0xffff; ++c) {
            if (CollationFCD.hasLccc(c)) {
                lccc.add(c);
            }
        }

        UnicodeSet diff = new UnicodeSet(expectedLccc);
        diff.removeAll(lccc);
        diff.remove(0x10000, 0x10ffff);  // hasLccc() only works for the BMP

        String empty = "[]";
        String diffString;

        diffString = diff.toPattern(true);
        assertEquals("CollationFCD::hasLccc() expected-actual", empty, diffString);

        diff = lccc;
        diff.removeAll(expectedLccc);
        diffString = diff.toPattern(true);
        assertEquals("CollationFCD::hasLccc() actual-expected", empty, diffString);

        UnicodeSet expectedTccc = new UnicodeSet("[:^tccc=0:]");
        addLeadSurrogatesForSupplementary(expectedLccc, expectedTccc);
        addLeadSurrogatesForSupplementary(expectedTccc, expectedTccc);

        UnicodeSet tccc = new UnicodeSet(); // actual
        for(int c = 0; c <= 0xffff; ++c) {
            if (CollationFCD.hasTccc(c)) {
                tccc.add(c);
            }
        }

        diff = new UnicodeSet(expectedTccc);
        diff.removeAll(tccc);
        diff.remove(0x10000, 0x10ffff); // hasTccc() only works for the BMP
        assertEquals("CollationFCD::hasTccc() expected-actual", empty, diffString);

        diff = tccc;
        diff.removeAll(expectedTccc);
        diffString = diff.toPattern(true);
        assertEquals("CollationFCD::hasTccc() actual-expected", empty, diffString);
    }

    private static class CodePointIterator {
        int[] cp;
        int length;
        int pos;

        CodePointIterator(int[] cp) {
            this.cp = cp;
            this.length = cp.length;
            this.pos = 0;
        }

        void resetToStart() {
            pos = 0;
        }

        int next() {
            return (pos < length) ? cp[pos++] : Collation.SENTINEL_CP;
        }

        int previous() {
            return (pos > 0) ? cp[--pos] : Collation.SENTINEL_CP;
        }

        int getLength() {
            return length;
        }

        int getIndex() {
            return pos;
        }
    }

    private void checkFCD(String name, CollationIterator ci, CodePointIterator cpi) {
        // Iterate forward to the limit.
        for (;;) {
            int c1 = ci.nextCodePoint();
            int c2 = cpi.next();
            if (c1 != c2) {
                errln(name + ".nextCodePoint(to limit, 1st pass) = U+" + Utility.hex(c1)
                        + " != U+" + Utility.hex(c1) + " at " + cpi.getIndex());
                return;
            }
            if (c1 < 0) {
                break;
            }
        }

        // Iterate backward most of the way.
        for (int n = (cpi.getLength() * 2) / 3; n > 0; --n) {
            int c1 = ci.previousCodePoint();
            int c2 = cpi.previous();
            if (c1 != c2) {
                errln(name + ".previousCodePoint() = U+" + Utility.hex(c1) +
                        " != U+" + Utility.hex(c2) + " at " + cpi.getIndex());
                return;
            }
        }

        // Forward again.
        for (;;) {
            int c1 = ci.nextCodePoint();
            int c2 = cpi.next();
            if (c1 != c2) {
                errln(name + ".nextCodePoint(to limit again) = U+" + Utility.hex(c1)
                        + " != U+" + Utility.hex(c2) + " at " + cpi.getIndex());
                return;
            }
            if (c1 < 0) {
                break;
            }
        }

        // Iterate backward to the start.
        for (;;) {
            int c1 = ci.previousCodePoint();
            int c2 = cpi.previous();
            if (c1 != c2) {
                errln(name + ".nextCodePoint(to start) = U+" + Utility.hex(c1)
                        + " != U+" + Utility.hex(c2) + " at " + cpi.getIndex());
                return;
            }
            if (c1 < 0) {
                break;
            }
        }
    }

    @Test
    public void TestFCD() {
        CollationData data = CollationRoot.getData();

        // Input string, not FCD.
        StringBuilder buf = new StringBuilder();
        buf.append("\u0308\u00e1\u0062\u0301\u0327\u0430\u0062")
            .appendCodePoint(0x1D15F)   // MUSICAL SYMBOL QUARTER NOTE=1D158 1D165, ccc=0, 216
            .append("\u0327\u0308")     // ccc=202, 230
            .appendCodePoint(0x1D16D)   // MUSICAL SYMBOL COMBINING AUGMENTATION DOT, ccc=226
            .appendCodePoint(0x1D15F)
            .appendCodePoint(0x1D16D)
            .append("\uac01")
            .append("\u00e7")           // Character with tccc!=0 decomposed together with mis-ordered sequence.
            .appendCodePoint(0x1D16D).appendCodePoint(0x1D165)
            .append("\u00e1")           // Character with tccc!=0 decomposed together with decomposed sequence.
            .append("\u0f73\u0f75")     // Tibetan composite vowels must be decomposed.
            .append("\u4e00\u0f81");
        String s = buf.toString();

        // Expected code points.
        int[] cp = {
            0x308, 0xe1, 0x62, 0x327, 0x301, 0x430, 0x62,
            0x1D158, 0x327, 0x1D165, 0x1D16D, 0x308,
            0x1D15F, 0x1D16D,
            0xac01,
            0x63, 0x327, 0x1D165, 0x1D16D,
            0x61,
            0xf71, 0xf71, 0xf72, 0xf74, 0x301,
            0x4e00, 0xf71, 0xf80
        };

        FCDUTF16CollationIterator u16ci = new FCDUTF16CollationIterator(data, false, s, 0);
        CodePointIterator cpi = new CodePointIterator(cp);
        checkFCD("FCDUTF16CollationIterator", u16ci, cpi);

        cpi.resetToStart();
        UCharacterIterator iter = UCharacterIterator.getInstance(s);
        FCDIterCollationIterator uici = new FCDIterCollationIterator(data, false, iter, 0);
        checkFCD("FCDIterCollationIterator", uici, cpi);
    }

    private void checkAllocWeights(CollationWeights cw, long lowerLimit, long upperLimit,
            int n, int someLength, int minCount) {

        if (!cw.allocWeights(lowerLimit, upperLimit, n)) {
            errln("CollationWeights::allocWeights(0x"
                    + Utility.hex(lowerLimit) + ",0x"
                    + Utility.hex(upperLimit) + ","
                    + n + ") = false");
            return;
        }
        long previous = lowerLimit;
        int count = 0; // number of weights that have someLength
        for (int i = 0; i < n; ++i) {
            long w = cw.nextWeight();
            if (w == 0xffffffffL) {
                errln("CollationWeights::allocWeights(0x"
                        + Utility.hex(lowerLimit) + ",0x"
                        + Utility.hex(upperLimit) + ",0x"
                        + n + ").nextWeight() returns only "
                        + i + " weights");
                return;
            }
            if (!(previous < w && w < upperLimit)) {
                errln("CollationWeights::allocWeights(0x"
                        + Utility.hex(lowerLimit) + ",0x"
                        + Utility.hex(upperLimit) + ","
                        + n + ").nextWeight() number "
                        + (i + 1) + " -> 0x" + Utility.hex(w)
                        + " not between "
                        + Utility.hex(previous) + " and "
                        + Utility.hex(upperLimit));
                return;
            }
            if (CollationWeights.lengthOfWeight(w) == someLength) {
                ++count;
            }
        }
        if (count < minCount) {
            errln("CollationWeights::allocWeights(0x"
                    + Utility.hex(lowerLimit) + ",0x"
                    + Utility.hex(upperLimit) + ","
                    + n + ").nextWeight() returns only "
                    + count + " < " + minCount + " weights of length "
                    + someLength);

        }
    }

    @Test
    public void TestCollationWeights() {
        CollationWeights cw = new CollationWeights();

        // Non-compressible primaries use 254 second bytes 02..FF.
        logln("CollationWeights.initForPrimary(non-compressible)");
        cw.initForPrimary(false);
        // Expect 1 weight 11 and 254 weights 12xx.
        checkAllocWeights(cw, 0x10000000L, 0x13000000L, 255, 1, 1);
        checkAllocWeights(cw, 0x10000000L, 0x13000000L, 255, 2, 254);
        // Expect 255 two-byte weights from the ranges 10ff, 11xx, 1202.
        checkAllocWeights(cw, 0x10fefe40L, 0x12030300L, 260, 2, 255);
        // Expect 254 two-byte weights from the ranges 10ff and 11xx.
        checkAllocWeights(cw, 0x10fefe40L, 0x12030300L, 600, 2, 254);
        // Expect 254^2=64516 three-byte weights.
        // During computation, there should be 3 three-byte ranges
        // 10ffff, 11xxxx, 120202.
        // The middle one should be split 64515:1,
        // and the newly-split-off range and the last ranged lengthened.
        checkAllocWeights(cw, 0x10fffe00L, 0x12020300L, 1 + 64516 + 254 + 1, 3, 64516);
        // Expect weights 1102 & 1103.
        checkAllocWeights(cw, 0x10ff0000L, 0x11040000L, 2, 2, 2);
        // Expect weights 102102 & 102103.
        checkAllocWeights(cw, 0x1020ff00L, 0x10210400L, 2, 3, 2);

        // Compressible primaries use 251 second bytes 04..FE.
        logln("CollationWeights.initForPrimary(compressible)");
        cw.initForPrimary(true);
        // Expect 1 weight 11 and 251 weights 12xx.
        checkAllocWeights(cw, 0x10000000L, 0x13000000L, 252, 1, 1);
        checkAllocWeights(cw, 0x10000000L, 0x13000000L, 252, 2, 251);
        // Expect 252 two-byte weights from the ranges 10fe, 11xx, 1204.
        checkAllocWeights(cw, 0x10fdfe40L, 0x12050300L, 260, 2, 252);
        // Expect weights 1104 & 1105.
        checkAllocWeights(cw, 0x10fe0000L, 0x11060000L, 2, 2, 2);
        // Expect weights 102102 & 102103.
        checkAllocWeights(cw, 0x1020ff00L, 0x10210400L, 2, 3, 2);

        // Secondary and tertiary weights use only bytes 3 & 4.
        logln("CollationWeights.initForSecondary()");
        cw.initForSecondary();
        // Expect weights fbxx and all four fc..ff.
        checkAllocWeights(cw, 0xfb20L, 0x10000L, 20, 3, 4);

        logln("CollationWeights.initForTertiary()");
        cw.initForTertiary();
        // Expect weights 3dxx and both 3e & 3f.
        checkAllocWeights(cw, 0x3d02L, 0x4000L, 10, 3, 2);
    }

    private static boolean isValidCE(CollationRootElements re, CollationData data, long p, long s, long ctq) {
        long p1 = p >>> 24;
        long p2 = (p >>> 16) & 0xff;
        long p3 = (p >>> 8) & 0xff;
        long p4 = p & 0xff;
        long s1 = s >>> 8;
        long s2 = s & 0xff;
        // ctq = Case, Tertiary, Quaternary
        long c = (ctq & Collation.CASE_MASK) >>> 14;
        long t = ctq & Collation.ONLY_TERTIARY_MASK;
        long t1 = t >>> 8;
        long t2 = t & 0xff;
        long q = ctq & Collation.QUATERNARY_MASK;
        // No leading zero bytes.
        if ((p != 0 && p1 == 0) || (s != 0 && s1 == 0) || (t != 0 && t1 == 0)) {
            return false;
        }
        // No intermediate zero bytes.
        if (p1 != 0 && p2 == 0 && (p & 0xffff) != 0) {
            return false;
        }
        if (p2 != 0 && p3 == 0 && p4 != 0) {
            return false;
        }
        // Minimum & maximum lead bytes.
        if ((p1 != 0 && p1 <= Collation.MERGE_SEPARATOR_BYTE)
                || s1 == Collation.LEVEL_SEPARATOR_BYTE
                || t1 == Collation.LEVEL_SEPARATOR_BYTE || t1 > 0x3f) {
            return false;
        }
        if (c > 2) {
            return false;
        }
        // The valid byte range for the second primary byte depends on compressibility.
        if (p2 != 0) {
            if (data.isCompressibleLeadByte((int)p1)) {
                if (p2 <= Collation.PRIMARY_COMPRESSION_LOW_BYTE
                        || Collation.PRIMARY_COMPRESSION_HIGH_BYTE <= p2) {
                    return false;
                }
            } else {
                if (p2 <= Collation.LEVEL_SEPARATOR_BYTE) {
                    return false;
                }
            }
        }
        // Other bytes just need to avoid the level separator.
        // Trailing zeros are ok.
        // assert (Collation.LEVEL_SEPARATOR_BYTE == 1);
        if (p3 == Collation.LEVEL_SEPARATOR_BYTE || p4 == Collation.LEVEL_SEPARATOR_BYTE
                || s2 == Collation.LEVEL_SEPARATOR_BYTE || t2 == Collation.LEVEL_SEPARATOR_BYTE) {
            return false;
        }
        // Well-formed CEs.
        if (p == 0) {
            if (s == 0) {
                if (t == 0) {
                    // Completely ignorable CE.
                    // Quaternary CEs are not supported.
                    if (c != 0 || q != 0) {
                        return false;
                    }
                } else {
                    // Tertiary CE.
                    if (t < re.getTertiaryBoundary() || c != 2) {
                        return false;
                    }
                }
            } else {
                // Secondary CE.
                if (s < re.getSecondaryBoundary() || t == 0 || t >= re.getTertiaryBoundary()) {
                    return false;
                }
            }
        } else {
            // Primary CE.
            if (s == 0 || (Collation.COMMON_WEIGHT16 < s && s <= re.getLastCommonSecondary())
                    || s >= re.getSecondaryBoundary()) {
                return false;
            }
            if (t == 0 || t >= re.getTertiaryBoundary()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidCE(CollationRootElements re, CollationData data, long ce) {
        long p = ce >>> 32;
        long secTer = ce & 0xffffffffL;
        return isValidCE(re, data, p, secTer >>> 16, secTer & 0xffff);
    }

    private static class RootElementsIterator {
        CollationData data;
        long[] elements;
        int length;

        long pri;
        long secTer;
        int index;

        RootElementsIterator(CollationData root) {
            data = root;
            elements = root.rootElements;
            length = elements.length;
            pri = 0;
            secTer = 0;
            index = (int)elements[CollationRootElements.IX_FIRST_TERTIARY_INDEX];
        }

        boolean next() {
            if (index >= length) {
                return false;
            }
            long p = elements[index];
            if (p == CollationRootElements.PRIMARY_SENTINEL) {
                return false;
            }
            if ((p & CollationRootElements.SEC_TER_DELTA_FLAG) != 0) {
                ++index;
                secTer = p & ~CollationRootElements.SEC_TER_DELTA_FLAG;
                return true;
            }
            if ((p & CollationRootElements.PRIMARY_STEP_MASK) != 0) {
                // End of a range, enumerate the primaries in the range.
                int step = (int)p & CollationRootElements.PRIMARY_STEP_MASK;
                p &= 0xffffff00;
                if (pri == p) {
                    // Finished the range, return the next CE after it.
                    ++index;
                    return next();
                }
                assert (pri < p);
                // Return the next primary in this range.
                boolean isCompressible = data.isCompressiblePrimary(pri);
                if ((pri & 0xffff) == 0) {
                    pri = Collation.incTwoBytePrimaryByOffset(pri, isCompressible, step);
                } else {
                    pri = Collation.incThreeBytePrimaryByOffset(pri, isCompressible, step);
                }
                return true;
            }
            // Simple primary CE.
            ++index;
            pri = p;
            // Does this have an explicit below-common sec/ter unit,
            // or does it imply a common one?
            if(index == length) {
                secTer = Collation.COMMON_SEC_AND_TER_CE;
            } else {
                secTer = elements[index];
                if((secTer & CollationRootElements.SEC_TER_DELTA_FLAG) == 0) {
                    // No sec/ter delta.
                    secTer = Collation.COMMON_SEC_AND_TER_CE;
                } else {
                    secTer &= ~CollationRootElements.SEC_TER_DELTA_FLAG;
                    if(secTer > Collation.COMMON_SEC_AND_TER_CE) {
                        // Implied sec/ter.
                        secTer = Collation.COMMON_SEC_AND_TER_CE;
                    } else {
                        // Explicit sec/ter below common/common.
                        ++index;
                    }
                }
            }
            return true;
        }

        long getPrimary() {
            return pri;
        }

        long getSecTer() {
            return secTer;
        }
    }

    @Test
    public void TestRootElements() {
        CollationData root = CollationRoot.getData();

        CollationRootElements rootElements = new CollationRootElements(root.rootElements);
        RootElementsIterator iter = new RootElementsIterator(root);

        // We check each root CE for validity,
        // and we also verify that there is a tailoring gap between each two CEs.
        CollationWeights cw1c = new CollationWeights(); // compressible primary weights
        CollationWeights cw1u = new CollationWeights(); // uncompressible primary weights
        CollationWeights cw2 = new CollationWeights();
        CollationWeights cw3 = new CollationWeights();

        cw1c.initForPrimary(true);
        cw1u.initForPrimary(false);
        cw2.initForSecondary();
        cw3.initForTertiary();

        // Note: The root elements do not include Han-implicit or unassigned-implicit CEs,
        // nor the special merge-separator CE for U+FFFE.
        long prevPri = 0;
        long prevSec = 0;
        long prevTer = 0;

        while (iter.next()) {
            long pri = iter.getPrimary();
            long secTer = iter.getSecTer();
            // CollationRootElements CEs must have 0 case and quaternary bits.
            if ((secTer & Collation.CASE_AND_QUATERNARY_MASK) != 0) {
                errln("CollationRootElements CE has non-zero case and/or quaternary bits: "
                        + "0x" + Utility.hex(pri, 8) + " 0x" + Utility.hex(secTer, 8));
            }
            long sec = secTer >>> 16;
            long ter = secTer & Collation.ONLY_TERTIARY_MASK;
            long ctq = ter;
            if (pri == 0 && sec == 0 && ter != 0) {
                // Tertiary CEs must have uppercase bits,
                // but they are not stored in the CollationRootElements.
                ctq |= 0x8000;
            }
            if (!isValidCE(rootElements, root, pri, sec, ctq)) {
                errln("invalid root CE 0x"
                        + Utility.hex(pri, 8) + " 0x" + Utility.hex(secTer, 8));
            } else {
                if (pri != prevPri) {
                    long newWeight = 0;
                    if (prevPri == 0 || prevPri >= Collation.FFFD_PRIMARY) {
                        // There is currently no tailoring gap after primary ignorables,
                        // and we forbid tailoring after U+FFFD and U+FFFF.
                    } else if (root.isCompressiblePrimary(prevPri)) {
                        if (!cw1c.allocWeights(prevPri, pri, 1)) {
                            errln("no primary/compressible tailoring gap between "
                                    + "0x" + Utility.hex(prevPri, 8)
                                    + " and 0x" + Utility.hex(pri, 8));
                        } else {
                            newWeight = cw1c.nextWeight();
                        }
                    } else {
                        if (!cw1u.allocWeights(prevPri, pri, 1)) {
                            errln("no primary/uncompressible tailoring gap between "
                                    + "0x" + Utility.hex(prevPri, 8)
                                    + " and 0x" + Utility.hex(pri, 8));
                        } else {
                            newWeight = cw1u.nextWeight();
                        }
                    }
                    if (newWeight != 0 && !(prevPri < newWeight && newWeight < pri)) {
                        errln("mis-allocated primary weight, should get "
                                + "0x" + Utility.hex(prevPri, 8)
                                + " < 0x" + Utility.hex(newWeight, 8)
                                + " < 0x" + Utility.hex(pri, 8));
                    }
                } else if (sec != prevSec) {
                    long lowerLimit = prevSec == 0 ?
                            rootElements.getSecondaryBoundary() - 0x100 : prevSec;
                    if (!cw2.allocWeights(lowerLimit, sec, 1)) {
                        errln("no secondary tailoring gap between "
                                + "0x" + Utility.hex(lowerLimit)
                                + " and 0x" + Utility.hex(sec));
                    } else {
                        long newWeight = cw2.nextWeight();
                        if (!(prevSec < newWeight && newWeight < sec)) {
                            errln("mis-allocated secondary weight, should get "
                                    + "0x" + Utility.hex(lowerLimit)
                                    + " < 0x" + Utility.hex(newWeight)
                                    + " < 0x" + Utility.hex(sec));
                        }
                    }
                } else if (ter != prevTer) {
                    long lowerLimit = prevTer == 0 ?
                            rootElements.getTertiaryBoundary() - 0x100 : prevTer;
                    if (!cw3.allocWeights(lowerLimit, ter, 1)) {
                        errln("no tertiary tailoring gap between "
                                + "0x" + Utility.hex(lowerLimit)
                                + " and 0x" + Utility.hex(ter));
                    } else {
                        long newWeight = cw3.nextWeight();
                        if (!(prevTer < newWeight && newWeight < ter)) {
                            errln("mis-allocated tertiary weight, should get "
                                    + "0x" + Utility.hex(lowerLimit)
                                    + " < 0x" + Utility.hex(newWeight)
                                    + " < 0x" + Utility.hex(ter));
                        }
                    }
                } else {
                    errln("duplicate root CE 0x"
                            + Utility.hex(pri, 8) + " 0x" + Utility.hex(secTer, 8));
                }
            }
            prevPri = pri;
            prevSec = sec;
            prevTer = ter;
        }
    }

    @Test
    public void TestTailoredElements() {
        CollationData root = CollationRoot.getData();
        CollationRootElements rootElements = new CollationRootElements(root.rootElements);

        Set<String> prevLocales = new HashSet<String>();
        prevLocales.add("");
        prevLocales.add("root");
        prevLocales.add("root@collation=standard");

        long[] ces;
        ULocale[] locales = Collator.getAvailableULocales();
        String localeID = "root";
        int locIdx = 0;

        for (; locIdx < locales.length; localeID = locales[locIdx++].getName()) {
            ULocale locale = new ULocale(localeID);
            String[] types = Collator.getKeywordValuesForLocale("collation", locale, false);
            for (int typeIdx = 0; typeIdx < types.length; ++typeIdx) {
                String type = types[typeIdx];  // first: default type
                if (type.startsWith("private-")) {
                    errln("Collator.getKeywordValuesForLocale(" + localeID +
                            ") returns private collation keyword: " + type);
                }
                ULocale localeWithType = locale.setKeywordValue("collation", type);
                Collator coll = Collator.getInstance(localeWithType);
                ULocale actual = coll.getLocale(ULocale.ACTUAL_LOCALE);
                if (prevLocales.contains(actual.getName())) {
                    continue;
                }
                prevLocales.add(actual.getName());
                logln("TestTailoredElements(): requested " + localeWithType.getName()
                        + " -> actual " + actual.getName());
                if (!(coll instanceof RuleBasedCollator)) {
                    continue;
                }
                RuleBasedCollator rbc = (RuleBasedCollator) coll;

                // Note: It would be better to get tailored strings such that we can
                // identify the prefix, and only get the CEs for the prefix+string,
                // not also for the prefix.
                // There is currently no API for that.
                // It would help in an unusual case where a contraction starting in the prefix
                // extends past its end, and we do not see the intended mapping.
                // For example, for a mapping p|st, if there is also a contraction ps,
                // then we get CEs(ps)+CEs(t), rather than CEs(p|st).
                UnicodeSet tailored = coll.getTailoredSet();
                UnicodeSetIterator iter = new UnicodeSetIterator(tailored);
                while (iter.next()) {
                    String s = iter.getString();
                    ces = rbc.internalGetCEs(s);
                    for (int i = 0; i < ces.length; ++i) {
                        long ce = ces[i];
                        if (!isValidCE(rootElements, root, ce)) {
                            logln(prettify(s));
                            errln("invalid tailored CE 0x" + Utility.hex(ce, 16)
                                    + " at CE index " + i + " from string:");
                        }
                    }
                }
            }
        }
    }

    private static boolean isSpace(char c) {
        return (c == 0x09 || c == 0x20 || c == 0x3000);
    }

    private static boolean isSectionStarter(char c) {
        return (c == '%' || c == '*' || c == '@');
    }

    private int skipSpaces(int i) {
        while (isSpace(fileLine.charAt(i))) {
            ++i;
        }
        return i;
    }

    private String printSortKey(byte[] p) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < p.length; ++i) {
            if (i > 0) {
                s.append(' ');
            }
            byte b = p[i];
            if (b == 0) {
                s.append('.');
            } else if (b == 1) {
                s.append('|');
            } else {
                s.append(String.format("%02x", b & 0xff));
            }
        }
        return s.toString();
    }

    private String printCollationKey(CollationKey key) {
        byte[] p = key.toByteArray();
        return printSortKey(p);
    }

    private boolean readNonEmptyLine(BufferedReader in) throws IOException {
        for (;;) {
            String line = in.readLine();
            if (line == null) {
                fileLine = null;
                return false;
            }
            if (fileLineNumber == 0 && line.length() != 0 && line.charAt(0) == '\uFEFF') {
                line = line.substring(1);  // Remove the BOM.
            }
            ++fileLineNumber;
            // Strip trailing comments and spaces
            int idx = line.indexOf('#');
            if (idx < 0) {
                idx = line.length();
            }
            while (idx > 0 && isSpace(line.charAt(idx - 1))) {
                --idx;
            }
            if (idx != 0) {
                fileLine = idx < line.length() ? line.substring(0, idx) : line;
                return true;
            }
            // Empty line, continue.
        }
    }

    private int parseString(int start, Output<String> prefix, Output<String> s) throws ParseException {
        int length = fileLine.length();
        int i;
        for (i = start; i < length && !isSpace(fileLine.charAt(i)); ++i) {
        }
        int pipeIndex = fileLine.indexOf('|', start);
        if (pipeIndex >= 0 && pipeIndex < i) {
            String tmpPrefix  = Utility.unescape(fileLine.substring(start, pipeIndex));
            if (tmpPrefix.length() == 0) {
                prefix.value = null;
                logln(fileLine);
                throw new ParseException("empty prefix on line " + fileLineNumber, fileLineNumber);
            }
            prefix.value = tmpPrefix;
            start = pipeIndex + 1;
        } else {
            prefix.value = null;
        }

        String tmp = Utility.unescape(fileLine.substring(start, i));
        if (tmp.length() == 0) {
            s.value = null;
            logln(fileLine);
            throw new ParseException("empty string on line " + fileLineNumber, fileLineNumber);
        }
        s.value = tmp;
        return i;
    }

    private int parseRelationAndString(Output<String> s) throws ParseException {
        int relation = Collation.NO_LEVEL;
        int start;
        if (fileLine.charAt(0) == '<') {
            char second = fileLine.charAt(1);
            start = 2;
            switch(second) {
            case 0x31:  // <1
                relation = Collation.PRIMARY_LEVEL;
                break;
            case 0x32:  // <2
                relation = Collation.SECONDARY_LEVEL;
                break;
            case 0x33:  // <3
                relation = Collation.TERTIARY_LEVEL;
                break;
            case 0x34:  // <4
                relation = Collation.QUATERNARY_LEVEL;
                break;
            case 0x63:  // <c
                relation = Collation.CASE_LEVEL;
                break;
            case 0x69:  // <i
                relation = Collation.IDENTICAL_LEVEL;
                break;
            default:  // just <
                relation = Collation.NO_LEVEL;
                start = 1;
                break;
            }
        } else if (fileLine.charAt(0) == '=') {
            relation = Collation.ZERO_LEVEL;
            start = 1;
        } else {
            start = 0;
        }

        if (start == 0 || !isSpace(fileLine.charAt(start))) {
            logln(fileLine);
            throw new ParseException("no relation (= < <1 <2 <c <3 <4 <i) at beginning of line "
                                        + fileLineNumber, fileLineNumber);
        }

        start = skipSpaces(start);
        Output<String> prefixOut = new Output<String>();
        start = parseString(start, prefixOut, s);
        if (prefixOut.value != null) {
            logln(fileLine);
            throw new ParseException("prefix string not allowed for test string: on line "
                                        + fileLineNumber, fileLineNumber);
        }
        if (start < fileLine.length()) {
            logln(fileLine);
            throw new ParseException("unexpected line contents after test string on line "
                                        + fileLineNumber, fileLineNumber);
        }

        return relation;
    }

    private void parseAndSetAttribute() throws ParseException {
        // Parse attributes even if the Collator could not be created,
        // in order to report syntax errors.
        int start = skipSpaces(1);
        int equalPos = fileLine.indexOf('=');
        if (equalPos < 0) {
            if (fileLine.regionMatches(start, "reorder", 0, 7)) {
                parseAndSetReorderCodes(start + 7);
                return;
            }
            logln(fileLine);
            throw new ParseException("missing '=' on line " + fileLineNumber, fileLineNumber);
        }

        String attrString = fileLine.substring(start,  equalPos);
        String valueString = fileLine.substring(equalPos + 1);
        if (attrString.equals("maxVariable")) {
            int max;
            if (valueString.equals("space")) {
                max = ReorderCodes.SPACE;
            } else if(valueString.equals("punct")) {
                max = ReorderCodes.PUNCTUATION;
            } else if(valueString.equals("symbol")) {
                max = ReorderCodes.SYMBOL;
            } else if(valueString.equals("currency")) {
                max = ReorderCodes.CURRENCY;
            } else {
                logln(fileLine);
                throw new ParseException("invalid attribute value name on line "
                                            + fileLineNumber, fileLineNumber);
            }
            if (coll != null) {
                coll.setMaxVariable(max);
            }
            fileLine = null;
            return;
        }

        boolean parsed = true;
        RuleBasedCollator rbc = (RuleBasedCollator)coll;
        if (attrString.equals("backwards")) {
            if (valueString.equals("on")) {
                if (rbc != null) rbc.setFrenchCollation(true);
            } else if (valueString.equals("off")) {
                if (rbc != null) rbc.setFrenchCollation(false);
            } else if (valueString.equals("default")) {
                if (rbc != null) rbc.setFrenchCollationDefault();
            } else {
                parsed = false;
            }
        } else if (attrString.equals("alternate")) {
            if (valueString.equals("non-ignorable")) {
                if (rbc != null) rbc.setAlternateHandlingShifted(false);
            } else if (valueString.equals("shifted")) {
                if (rbc != null) rbc.setAlternateHandlingShifted(true);
            } else if (valueString.equals("default")) {
                if (rbc != null) rbc.setAlternateHandlingDefault();
            } else {
                parsed = false;
            }
        } else if (attrString.equals("caseFirst")) {
            if (valueString.equals("upper")) {
                if (rbc != null) rbc.setUpperCaseFirst(true);
            } else if (valueString.equals("lower")) {
                if (rbc != null) rbc.setLowerCaseFirst(true);
            } else if (valueString.equals("default")) {
                if (rbc != null) rbc.setCaseFirstDefault();
            } else {
                parsed = false;
            }
        } else if (attrString.equals("caseLevel")) {
            if (valueString.equals("on")) {
                if (rbc != null) rbc.setCaseLevel(true);
            } else if (valueString.equals("off")) {
                if (rbc != null) rbc.setCaseLevel(false);
            } else if (valueString.equals("default")) {
                if (rbc != null) rbc.setCaseLevelDefault();
            } else {
                parsed = false;
            }
        } else if (attrString.equals("strength")) {
            if (valueString.equals("primary")) {
                if (rbc != null) rbc.setStrength(Collator.PRIMARY);
            } else if (valueString.equals("secondary")) {
                if (rbc != null) rbc.setStrength(Collator.SECONDARY);
            } else if (valueString.equals("tertiary")) {
                if (rbc != null) rbc.setStrength(Collator.TERTIARY);
            } else if (valueString.equals("quaternary")) {
                if (rbc != null) rbc.setStrength(Collator.QUATERNARY);
            } else if (valueString.equals("identical")) {
                if (rbc != null) rbc.setStrength(Collator.IDENTICAL);
            } else if (valueString.equals("default")) {
                if (rbc != null) rbc.setStrengthDefault();
            } else {
                parsed = false;
            }
        } else if (attrString.equals("numeric")) {
            if (valueString.equals("on")) {
                if (rbc != null) rbc.setNumericCollation(true);
            } else if (valueString.equals("off")) {
                if (rbc != null) rbc.setNumericCollation(false);
            } else if (valueString.equals("default")) {
                if (rbc != null) rbc.setNumericCollationDefault();
            } else {
                parsed = false;
            }
        } else {
            logln(fileLine);
            throw new ParseException("invalid attribute name on line "
                                        + fileLineNumber, fileLineNumber);
        }
        if (!parsed) {
            logln(fileLine);
            throw new ParseException(
                    "invalid attribute value name or attribute=value combination on line "
                    + fileLineNumber, fileLineNumber);
        }

        fileLine = null;
    }

    private void parseAndSetReorderCodes(int start) throws ParseException {
        UVector32 reorderCodes = new UVector32();
        while (start < fileLine.length()) {
            start = skipSpaces(start);
            int limit = start;
            while (limit < fileLine.length() && !isSpace(fileLine.charAt(limit))) {
                ++limit;
            }
            String name = fileLine.substring(start, limit);
            int code = CollationRuleParser.getReorderCode(name);
            if (code < -1) {
                if (name.equalsIgnoreCase("default")) {
                    code = ReorderCodes.DEFAULT;  // -1
                } else {
                    logln(fileLine);
                    throw new ParseException("invalid reorder code '" + name + "' on line "
                                                + fileLineNumber, fileLineNumber);
                }
            }
            reorderCodes.addElement(code);
            start = limit;
        }
        if (coll != null) {
            int[] reorderCodesArray = new int[reorderCodes.size()];
            System.arraycopy(reorderCodes.getBuffer(), 0,
                    reorderCodesArray, 0, reorderCodes.size());
            coll.setReorderCodes(reorderCodesArray);
        }

        fileLine = null;
    }

    private void buildTailoring(BufferedReader in) throws IOException {
        StringBuilder rules = new StringBuilder();
        while (readNonEmptyLine(in) && !isSectionStarter(fileLine.charAt(0))) {
            rules.append(Utility.unescape(fileLine));
        }

        try {
            coll = new RuleBasedCollator(rules.toString());
        } catch (Exception e) {
            logln(rules.toString());
            // Android patch: Add --omitCollationRules to genrb.
            logln("RuleBasedCollator(rules) failed - " + e.getMessage());
            // Android patch end.
            coll = null;
        }
    }

    private void setRootCollator() {
        coll = Collator.getInstance(ULocale.ROOT);
    }

    private void setLocaleCollator() {
        coll = null;
        ULocale locale = null;
        if (fileLine.length() > 9) {
            String localeID = fileLine.substring(9); // "@ locale <langTag>"
            try {
                locale = new ULocale(localeID);  // either locale ID or language tag
            } catch (IllformedLocaleException e) {
                locale = null;
            }
        }
        if (locale == null) {
            logln(fileLine);
            errln("invalid language tag on line " + fileLineNumber);
            return;
        }

        logln("creating a collator for locale ID " + locale.getName());
        try {
            coll = Collator.getInstance(locale);
        } catch (Exception e) {
            errln("unable to create a collator for locale " + locale +
                    " on line " + fileLineNumber + " - " + e);
        }
    }

    private boolean needsNormalization(String s) {
        if (!fcd.isNormalized(s)) {
            return true;
        }
        // In some sequences with Tibetan composite vowel signs,
        // even if the string passes the FCD check,
        // those composites must be decomposed.
        // Check if s contains 0F71 immediately followed by 0F73 or 0F75 or 0F81.
        int index = 0;
        while((index = s.indexOf(0xf71, index)) >= 0) {
            if (++index < s.length()) {
                char c = s.charAt(index);
                if (c == 0xf73 || c == 0xf75 || c == 0xf81) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean getCollationKey(String norm, String line, String s, Output<CollationKey> keyOut) {
        CollationKey key = coll.getCollationKey(s);
        keyOut.value = key;

        byte[] keyBytes = key.toByteArray();
        if (keyBytes.length == 0 || keyBytes[keyBytes.length - 1] != 0) {
            logln(fileTestName);
            logln(line);
            logln(printCollationKey(key));
            errln("Collator(" + norm + ").getCollationKey() wrote an empty or unterminated key");
            return false;
        }

        int numLevels = coll.getStrength();
        if (numLevels < Collator.IDENTICAL) {
            ++numLevels;
        } else {
            numLevels = 5;
        }
        if (((RuleBasedCollator)coll).isCaseLevel()) {
            ++numLevels;
        }
        int numLevelSeparators = 0;
        for (int i = 0; i < (keyBytes.length - 1); ++i) {
            byte b = keyBytes[i];
            if (b == 0) {
                logln(fileTestName);
                logln(line);
                logln(printCollationKey(key));
                errln("Collator(" + norm + ").getCollationKey() contains a 00 byte");
                return false;
            }
            if (b == 1) {
                ++numLevelSeparators;
            }
        }
        if (numLevelSeparators != (numLevels - 1)) {
            logln(fileTestName);
            logln(line);
            logln(printCollationKey(key));
            errln("Collator(" + norm + ").getCollationKey() has "
                    + numLevelSeparators + " level separators for "
                    + numLevels + " levels");
            return false;
        }

        // No nextSortKeyPart support in ICU4J

        return true;
    }

    /**
     * Changes the key to the merged segments of the U+FFFE-separated substrings of s.
     * Leaves key unchanged if s does not contain U+FFFE.
     * @return true if the key was successfully changed
     */
    private boolean getMergedCollationKey(String s, Output<CollationKey> key) {
        CollationKey mergedKey = null;
        int sLength = s.length();
        int segmentStart = 0;
        for (int i = 0;;) {
            if (i == sLength) {
                if (segmentStart == 0) {
                    // s does not contain any U+FFFE.
                    return false;
                }
            } else if (s.charAt(i) != '\uFFFE') {
                ++i;
                continue;
            }
            // Get the sort key for another segment and merge it into mergedKey.
            CollationKey tmpKey = coll.getCollationKey(s.substring(segmentStart, i));
            if (mergedKey == null) {
                mergedKey = tmpKey;
            } else {
                mergedKey = mergedKey.merge(tmpKey);
            }
            if (i == sLength) {
                break;
            }
            segmentStart = ++i;
        }
        key.value = mergedKey;
        return true;
    }

    private static int getDifferenceLevel(CollationKey prevKey, CollationKey key,
            int order, boolean collHasCaseLevel) {
        if (order == Collation.EQUAL) {
            return Collation.NO_LEVEL;
        }
        byte[] prevBytes = prevKey.toByteArray();
        byte[] bytes = key.toByteArray();
        int level = Collation.PRIMARY_LEVEL;
        for (int i = 0;; ++i) {
            byte b = prevBytes[i];
            if (b != bytes[i]) {
                break;
            }
            if ((int)b == Collation.LEVEL_SEPARATOR_BYTE) {
                ++level;
                if (level == Collation.CASE_LEVEL && !collHasCaseLevel) {
                    ++level;
                }
            }
        }
        return level;
    }

    private boolean checkCompareTwo(String norm, String prevFileLine, String prevString, String s,
                                    int expectedOrder, int expectedLevel) {
        // Get the sort keys first, for error debug output.
        Output<CollationKey> prevKeyOut = new Output<CollationKey>();
        CollationKey prevKey;
        if (!getCollationKey(norm, fileLine, prevString, prevKeyOut)) {
            return false;
        }
        prevKey = prevKeyOut.value;

        Output<CollationKey> keyOut = new Output<CollationKey>();
        CollationKey key;
        if (!getCollationKey(norm, fileLine, s, keyOut)) {
            return false;
        }
        key = keyOut.value;

        int order = coll.compare(prevString, s);
        if (order != expectedOrder) {
            logln(fileTestName);
            logln(prevFileLine);
            logln(fileLine);
            logln(printCollationKey(prevKey));
            logln(printCollationKey(key));
            errln("line " + fileLineNumber
                    + " Collator(" + norm + ").compare(previous, current) wrong order: "
                    + order + " != " + expectedOrder);
            return false;
        }
        order = coll.compare(s, prevString);
        if (order != -expectedOrder) {
            logln(fileTestName);
            logln(prevFileLine);
            logln(fileLine);
            logln(printCollationKey(prevKey));
            logln(printCollationKey(key));
            errln("line " + fileLineNumber
                    + " Collator(" + norm + ").compare(current, previous) wrong order: "
                    + order + " != " + -expectedOrder);
            return false;
        }

        order = prevKey.compareTo(key);
        if (order != expectedOrder) {
            logln(fileTestName);
            logln(prevFileLine);
            logln(fileLine);
            logln(printCollationKey(prevKey));
            logln(printCollationKey(key));
            errln("line " + fileLineNumber
                    + " Collator(" + norm + ").getCollationKey(previous, current).compareTo() wrong order: "
                    + order + " != " + expectedOrder);
            return false;
        }
        boolean collHasCaseLevel = ((RuleBasedCollator)coll).isCaseLevel();
        int level = getDifferenceLevel(prevKey, key, order, collHasCaseLevel);
        if (order != Collation.EQUAL && expectedLevel != Collation.NO_LEVEL) {
            if (level != expectedLevel) {
                logln(fileTestName);
                logln(prevFileLine);
                logln(fileLine);
                logln(printCollationKey(prevKey));
                logln(printCollationKey(key));
                errln("line " + fileLineNumber
                        + " Collator(" + norm + ").getCollationKey(previous, current).compareTo()="
                        + order + " wrong level: " + level + " != " + expectedLevel);
                return false;
            }
        }

        // If either string contains U+FFFE, then their sort keys must compare the same as
        // the merged sort keys of each string's between-FFFE segments.
        //
        // It is not required that
        //   sortkey(str1 + "\uFFFE" + str2) == mergeSortkeys(sortkey(str1), sortkey(str2))
        // only that those two methods yield the same order.
        //
        // Use bit-wise OR so that getMergedCollationKey() is always called for both strings.
        Output<CollationKey> outPrevKey = new Output<CollationKey>(prevKey);
        Output<CollationKey> outKey = new Output<CollationKey>(key);
        if (getMergedCollationKey(prevString, outPrevKey) | getMergedCollationKey(s, outKey)) {
            prevKey = outPrevKey.value;
            key = outKey.value;
            order = prevKey.compareTo(key);
            if (order != expectedOrder) {
                logln(fileTestName);
                errln("line " + fileLineNumber
                        + " Collator(" + norm + ").getCollationKey"
                        + "(previous, current segments between U+FFFE)).merge().compareTo() wrong order: "
                        + order + " != " + expectedOrder);
                logln(prevFileLine);
                logln(fileLine);
                logln(printCollationKey(prevKey));
                logln(printCollationKey(key));
                return false;
            }
            int mergedLevel = getDifferenceLevel(prevKey, key, order, collHasCaseLevel);
            if (order != Collation.EQUAL && expectedLevel != Collation.NO_LEVEL) {
                if(mergedLevel != level) {
                    logln(fileTestName);
                    errln("line " + fileLineNumber
                        + " Collator(" + norm + ").getCollationKey"
                        + "(previous, current segments between U+FFFE)).merge().compareTo()="
                        + order + " wrong level: " + mergedLevel + " != " + level);
                    logln(prevFileLine);
                    logln(fileLine);
                    logln(printCollationKey(prevKey));
                    logln(printCollationKey(key));
                    return false;
                }
            }
        }
        return true;
    }

    private void checkCompareStrings(BufferedReader in) throws IOException {
        String prevFileLine = "(none)";
        String prevString = "";
        Output<String> sOut = new Output<String>();
        while (readNonEmptyLine(in) && !isSectionStarter(fileLine.charAt(0))) {
            // Parse the line even if it will be ignored (when we do not have a Collator)
            // in order to report syntax issues.
            int relation;
            try {
                relation = parseRelationAndString(sOut);
            } catch (ParseException pe) {
                errln(pe.toString());
                break;
            }
            if(coll == null) {
                // We were unable to create the Collator but continue with tests.
                // Ignore test data for this Collator.
                // The next Collator creation might work.
                continue;
            }
            String s = sOut.value;
            int expectedOrder = (relation == Collation.ZERO_LEVEL) ? Collation.EQUAL : Collation.LESS;
            int expectedLevel = relation;
            boolean isOk = true;
            if (!needsNormalization(prevString) && !needsNormalization(s)) {
                coll.setDecomposition(Collator.NO_DECOMPOSITION);
                isOk = checkCompareTwo("normalization=off", prevFileLine, prevString, s,
                                        expectedOrder, expectedLevel);
            }
            if (isOk) {
                coll.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
                isOk = checkCompareTwo("normalization=on", prevFileLine, prevString, s,
                                        expectedOrder, expectedLevel);
            }
            if (isOk && (!nfd.isNormalized(prevString) || !nfd.isNormalized(s))) {
                String pn = nfd.normalize(prevString);
                String n = nfd.normalize(s);
                isOk = checkCompareTwo("NFD input", prevFileLine, pn, n,
                                        expectedOrder, expectedLevel);
            }
            prevFileLine = fileLine;
            prevString = s;
        }
    }

    @Test
    public void TestDataDriven() {
        nfd = Normalizer2.getNFDInstance();
        fcd = Norm2AllModes.getFCDNormalizer2();

        BufferedReader in = null;

        try {
            in = TestUtil.getDataReader("collationtest.txt", "UTF-8");

            // Read a new line if necessary.
            // Sub-parsers leave the first line set that they do not handle.
            while (fileLine != null || readNonEmptyLine(in)) {
                if (!isSectionStarter(fileLine.charAt(0))) {
                    logln(fileLine);
                    errln("syntax error on line " + fileLineNumber);
                    return;
                }
                if (fileLine.startsWith("** test: ")) {
                    fileTestName = fileLine;
                    logln(fileLine);
                    fileLine = null;
                } else if (fileLine.equals("@ root")) {
                    setRootCollator();
                    fileLine = null;
                } else if (fileLine.startsWith("@ locale ")) {
                    setLocaleCollator();
                    fileLine = null;
                } else if (fileLine.equals("@ rules")) {
                    buildTailoring(in);
                } else if (fileLine.charAt(0) == '%'
                        && fileLine.length() > 1 && isSpace(fileLine.charAt(1))) {
                    parseAndSetAttribute();
                } else if (fileLine.equals("* compare")) {
                    checkCompareStrings(in);
                } else {
                    logln(fileLine);
                    errln("syntax error on line " + fileLineNumber);
                    return;
                }
            }
        } catch (ParseException pe) {
            errln(pe.toString());
        } catch (IOException e) {
            errln(e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
