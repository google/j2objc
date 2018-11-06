/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2004-2008, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.dev.test.lang;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.text.UTF16;

/**
 * Test JDK 1.5 cover APIs.
 */
public final class UCharacterSurrogateTest extends TestFmwk {
    @org.junit.Test
    public void TestUnicodeBlockForName() {
      String[] names = {"Latin-1 Supplement", 
                        "Optical Character Recognition",
                        "CJK Unified Ideographs Extension A", 
                        "Supplemental Arrows-B",
                        "Supplemental arrows b", 
                        "supp-lement-al arrowsb",
                        "Supplementary Private Use Area-B",
                        "supplementary_Private_Use_Area-b",
                        "supplementary_PRIVATE_Use_Area_b"};
        for (int i = 0; i < names.length; ++i) {
            try {
                UCharacter.UnicodeBlock b = UCharacter.UnicodeBlock
                        .forName(names[i]);
                logln("found: " + b + " for name: " + names[i]);
            } catch (Exception e) {
                errln("could not find block for name: " + names[i]);
                break;
            }
        }
    }

    @org.junit.Test
    public void TestIsValidCodePoint() {
        if (UCharacter.isValidCodePoint(-1))
            errln("-1");
        if (!UCharacter.isValidCodePoint(0))
            errln("0");
        if (!UCharacter.isValidCodePoint(UCharacter.MAX_CODE_POINT))
            errln("0x10ffff");
        if (UCharacter.isValidCodePoint(UCharacter.MAX_CODE_POINT + 1))
            errln("0x110000");
    }

    @org.junit.Test
    public void TestIsSupplementaryCodePoint() {
        if (UCharacter.isSupplementaryCodePoint(-1))
            errln("-1");
        if (UCharacter.isSupplementaryCodePoint(0))
            errln("0");
        if (UCharacter
                .isSupplementaryCodePoint(UCharacter.MIN_SUPPLEMENTARY_CODE_POINT - 1))
            errln("0xffff");
        if (!UCharacter
                .isSupplementaryCodePoint(UCharacter.MIN_SUPPLEMENTARY_CODE_POINT))
            errln("0x10000");
        if (!UCharacter.isSupplementaryCodePoint(UCharacter.MAX_CODE_POINT))
            errln("0x10ffff");
        if (UCharacter.isSupplementaryCodePoint(UCharacter.MAX_CODE_POINT + 1))
            errln("0x110000");
    }

    @org.junit.Test
    public void TestIsHighSurrogate() {
        if (UCharacter
                .isHighSurrogate((char) (UCharacter.MIN_HIGH_SURROGATE - 1)))
            errln("0xd7ff");
        if (!UCharacter.isHighSurrogate(UCharacter.MIN_HIGH_SURROGATE))
            errln("0xd800");
        if (!UCharacter.isHighSurrogate(UCharacter.MAX_HIGH_SURROGATE))
            errln("0xdbff");
        if (UCharacter
                .isHighSurrogate((char) (UCharacter.MAX_HIGH_SURROGATE + 1)))
            errln("0xdc00");
    }

    @org.junit.Test
    public void TestIsLowSurrogate() {
        if (UCharacter
                .isLowSurrogate((char) (UCharacter.MIN_LOW_SURROGATE - 1)))
            errln("0xdbff");
        if (!UCharacter.isLowSurrogate(UCharacter.MIN_LOW_SURROGATE))
            errln("0xdc00");
        if (!UCharacter.isLowSurrogate(UCharacter.MAX_LOW_SURROGATE))
            errln("0xdfff");
        if (UCharacter
                .isLowSurrogate((char) (UCharacter.MAX_LOW_SURROGATE + 1)))
            errln("0xe000");
    }

    @org.junit.Test
    public void TestIsSurrogatePair() {
        if (UCharacter.isSurrogatePair(
                (char) (UCharacter.MIN_HIGH_SURROGATE - 1),
                UCharacter.MIN_LOW_SURROGATE))
            errln("0xd7ff,0xdc00");
        if (UCharacter.isSurrogatePair(
                (char) (UCharacter.MAX_HIGH_SURROGATE + 1),
                UCharacter.MIN_LOW_SURROGATE))
            errln("0xd800,0xdc00");
        if (UCharacter.isSurrogatePair(UCharacter.MIN_HIGH_SURROGATE,
                (char) (UCharacter.MIN_LOW_SURROGATE - 1)))
            errln("0xd800,0xdbff");
        if (UCharacter.isSurrogatePair(UCharacter.MIN_HIGH_SURROGATE,
                (char) (UCharacter.MAX_LOW_SURROGATE + 1)))
            errln("0xd800,0xe000");
        if (!UCharacter.isSurrogatePair(UCharacter.MIN_HIGH_SURROGATE,
                UCharacter.MIN_LOW_SURROGATE))
            errln("0xd800,0xdc00");
    }

    @org.junit.Test
    public void TestCharCount() {
        UCharacter.charCount(-1);
        UCharacter.charCount(UCharacter.MAX_CODE_POINT + 1);
        if (UCharacter.charCount(UCharacter.MIN_SUPPLEMENTARY_CODE_POINT - 1) != 1)
            errln("0xffff");
        if (UCharacter.charCount(UCharacter.MIN_SUPPLEMENTARY_CODE_POINT) != 2)
            errln("0x010000");
    }

    @org.junit.Test
    public void TestToCodePoint() {
        final char[] pairs = {(char) (UCharacter.MIN_HIGH_SURROGATE + 0),
                (char) (UCharacter.MIN_LOW_SURROGATE + 0),
                (char) (UCharacter.MIN_HIGH_SURROGATE + 1),
                (char) (UCharacter.MIN_LOW_SURROGATE + 1),
                (char) (UCharacter.MIN_HIGH_SURROGATE + 2),
                (char) (UCharacter.MIN_LOW_SURROGATE + 2),
                (char) (UCharacter.MAX_HIGH_SURROGATE - 2),
                (char) (UCharacter.MAX_LOW_SURROGATE - 2),
                (char) (UCharacter.MAX_HIGH_SURROGATE - 1),
                (char) (UCharacter.MAX_LOW_SURROGATE - 1),
                (char) (UCharacter.MAX_HIGH_SURROGATE - 0),
                (char) (UCharacter.MAX_LOW_SURROGATE - 0),};
        for (int i = 0; i < pairs.length; i += 2) {
            int cp = UCharacter.toCodePoint(pairs[i], pairs[i + 1]);
            if (pairs[i] != UTF16.getLeadSurrogate(cp)
                    || pairs[i + 1] != UTF16.getTrailSurrogate(cp)) {

                errln(Integer.toHexString(pairs[i]) + ", " + pairs[i + 1]);
                break;
            }
        }
    }

    @org.junit.Test
    public void TestCodePointAtBefore() {
        String s = "" + UCharacter.MIN_HIGH_SURROGATE + // isolated high
                UCharacter.MIN_HIGH_SURROGATE + // pair
                UCharacter.MIN_LOW_SURROGATE + UCharacter.MIN_LOW_SURROGATE; // isolated
                                                                             // low
        char[] c = s.toCharArray();
        int[] avalues = {
                UCharacter.MIN_HIGH_SURROGATE,
                UCharacter.toCodePoint(UCharacter.MIN_HIGH_SURROGATE,
                        UCharacter.MIN_LOW_SURROGATE),
                UCharacter.MIN_LOW_SURROGATE, UCharacter.MIN_LOW_SURROGATE};
        int[] bvalues = {
                UCharacter.MIN_HIGH_SURROGATE,
                UCharacter.MIN_HIGH_SURROGATE,
                UCharacter.toCodePoint(UCharacter.MIN_HIGH_SURROGATE,
                        UCharacter.MIN_LOW_SURROGATE),
                UCharacter.MIN_LOW_SURROGATE,};
        StringBuffer b = new StringBuffer(s);
        for (int i = 0; i < avalues.length; ++i) {
            if (UCharacter.codePointAt(s, i) != avalues[i])
                errln("string at: " + i);
            if (UCharacter.codePointAt(c, i) != avalues[i])
                errln("chars at: " + i);
            if (UCharacter.codePointAt(b, i) != avalues[i])
                errln("stringbuffer at: " + i);

            if (UCharacter.codePointBefore(s, i + 1) != bvalues[i])
                errln("string before: " + i);
            if (UCharacter.codePointBefore(c, i + 1) != bvalues[i])
                errln("chars before: " + i);
            if (UCharacter.codePointBefore(b, i + 1) != bvalues[i])
                errln("stringbuffer before: " + i);
        }

        //cover codePointAtBefore with limit
        logln("Testing codePointAtBefore with limit ...");
        for (int i = 0; i < avalues.length; ++i) {
            if (UCharacter.codePointAt(c, i, 4) != avalues[i])
                errln("chars at: " + i);
            if (UCharacter.codePointBefore(c, i + 1, 0) != bvalues[i])
                errln("chars before: " + i);
        }

    }

    @org.junit.Test
    public void TestToChars() {
        char[] chars = new char[3];
        int cp = UCharacter.toCodePoint(UCharacter.MIN_HIGH_SURROGATE,
                UCharacter.MIN_LOW_SURROGATE);
        UCharacter.toChars(cp, chars, 1);
        if (chars[1] != UCharacter.MIN_HIGH_SURROGATE
                || chars[2] != UCharacter.MIN_LOW_SURROGATE) {

            errln("fail");
        }

        chars = UCharacter.toChars(cp);
        if (chars[0] != UCharacter.MIN_HIGH_SURROGATE
                || chars[1] != UCharacter.MIN_LOW_SURROGATE) {

            errln("fail");
        }
    }

    @org.junit.Test
    public void TestCodePointCount() {
        class Test {
            String str(String s, int start, int limit) {
                if(s==null){
                    s="";
                }
                return "codePointCount('" + Utility.escape(s) + "' " + start
                        + ", " + limit + ")";
            }

            void test(String s, int start, int limit, int expected) {
                int val1 = UCharacter.codePointCount(s.toCharArray(), start,
                        limit);
                int val2 = UCharacter.codePointCount(s, start, limit);
                if (val1 != expected) {
                    errln("char[] " + str(s, start, limit) + "(" + val1
                            + ") != " + expected);
                } else if (val2 != expected) {
                    errln("String " + str(s, start, limit) + "(" + val2
                            + ") != " + expected);
                } else if (isVerbose()) {
                    logln(str(s, start, limit) + " == " + expected);
                }
            }

            void fail(String s, int start, int limit, Class exc) {
                try {
                    UCharacter.codePointCount(s, start, limit);
                    errln("unexpected success " + str(s, start, limit));
                } catch (Throwable e) {
                    if (!exc.isInstance(e)) {
                        warnln("bad exception " + str(s, start, limit)
                                + e.getClass().getName());
                    }
                }
            }
        }

        Test test = new Test();
        test.fail(null, 0, 1, NullPointerException.class);
        test.fail("a", -1, 0, IndexOutOfBoundsException.class);
        test.fail("a", 1, 2, IndexOutOfBoundsException.class);
        test.fail("a", 1, 0, IndexOutOfBoundsException.class);
        test.test("", 0, 0, 0);
        test.test("\ud800", 0, 1, 1);
        test.test("\udc00", 0, 1, 1);
        test.test("\ud800\udc00", 0, 1, 1);
        test.test("\ud800\udc00", 1, 2, 1);
        test.test("\ud800\udc00", 0, 2, 1);
        test.test("\udc00\ud800", 0, 1, 1);
        test.test("\udc00\ud800", 1, 2, 1);
        test.test("\udc00\ud800", 0, 2, 2);
        test.test("\ud800\ud800\udc00", 0, 2, 2);
        test.test("\ud800\ud800\udc00", 1, 3, 1);
        test.test("\ud800\ud800\udc00", 0, 3, 2);
        test.test("\ud800\udc00\udc00", 0, 2, 1);
        test.test("\ud800\udc00\udc00", 1, 3, 2);
        test.test("\ud800\udc00\udc00", 0, 3, 2);
    }

    @org.junit.Test
    public void TestOffsetByCodePoints() {
        class Test {
            String str(String s, int start, int count, int index, int offset) {
                return "offsetByCodePoints('" + Utility.escape(s) + "' "
                        + start + ", " + count + ", " + index + ", " + offset
                        + ")";
            }

            void test(String s, int start, int count, int index, int offset,
                    int expected, boolean flip) {
                char[] chars = s.toCharArray();
                String string = s.substring(start, start + count);
                int val1 = UCharacter.offsetByCodePoints(chars, start, count,
                        index, offset);
                int val2 = UCharacter.offsetByCodePoints(string, index - start,
                        offset)
                        + start;

                if (val1 != expected) {
                    errln("char[] " + str(s, start, count, index, offset) + "("
                            + val1 + ") != " + expected);
                } else if (val2 != expected) {
                    errln("String " + str(s, start, count, index, offset) + "("
                            + val2 + ") != " + expected);
                } else if (isVerbose()) {
                    logln(str(s, start, count, index, offset) + " == "
                            + expected);
                }

                if (flip) {
                    val1 = UCharacter.offsetByCodePoints(chars, start, count,
                            expected, -offset);
                    val2 = UCharacter.offsetByCodePoints(string, expected
                            - start, -offset)
                            + start;
                    if (val1 != index) {
                        errln("char[] "
                                + str(s, start, count, expected, -offset) + "("
                                + val1 + ") != " + index);
                    } else if (val2 != index) {
                        errln("String "
                                + str(s, start, count, expected, -offset) + "("
                                + val2 + ") != " + index);
                    } else if (isVerbose()) {
                        logln(str(s, start, count, expected, -offset) + " == "
                                + index);
                    }
                }
            }

            void fail(char[] text, int start, int count, int index, int offset,
                    Class exc) {
                try {
                    UCharacter.offsetByCodePoints(text, start, count, index,
                            offset);
                    errln("unexpected success "
                            + str(new String(text), start, count, index, offset));
                } catch (Throwable e) {
                    if (!exc.isInstance(e)) {
                        errln("bad exception "
                                + str(new String(text), start, count, index,
                                        offset) + e.getClass().getName());
                    }
                }
            }

            void fail(String text, int index, int offset, Class exc) {
                try {
                    UCharacter.offsetByCodePoints(text, index, offset);
                    errln("unexpected success "
                            + str(text, index, offset, 0, text.length()));
                } catch (Throwable e) {
                    if (!exc.isInstance(e)) {
                        errln("bad exception "
                                + str(text, 0, text.length(), index, offset)
                                + e.getClass().getName());
                    }
                }
            }
        }

        Test test = new Test();

        test.test("\ud800\ud800\udc00", 0, 2, 0, 1, 1, true);

        test.fail((char[]) null, 0, 1, 0, 1, NullPointerException.class);
        test.fail((String) null, 0, 1, NullPointerException.class);
        test.fail("abc", -1, 0, IndexOutOfBoundsException.class);
        test.fail("abc", 4, 0, IndexOutOfBoundsException.class);
        test.fail("abc", 1, -2, IndexOutOfBoundsException.class);
        test.fail("abc", 2, 2, IndexOutOfBoundsException.class);
        char[] abc = "abc".toCharArray();
        test.fail(abc, -1, 2, 0, 0, IndexOutOfBoundsException.class);
        test.fail(abc, 2, 2, 3, 0, IndexOutOfBoundsException.class);
        test.fail(abc, 1, -1, 0, 0, IndexOutOfBoundsException.class);
        test.fail(abc, 1, 1, 2, -2, IndexOutOfBoundsException.class);
        test.fail(abc, 1, 1, 1, 2, IndexOutOfBoundsException.class);
        test.fail(abc, 1, 2, 1, 3, IndexOutOfBoundsException.class);
        test.fail(abc, 0, 2, 2, -3, IndexOutOfBoundsException.class);
        test.test("", 0, 0, 0, 0, 0, false);
        test.test("\ud800", 0, 1, 0, 1, 1, true);
        test.test("\udc00", 0, 1, 0, 1, 1, true);

        String s = "\ud800\udc00";
        test.test(s, 0, 1, 0, 1, 1, true);
        test.test(s, 0, 2, 0, 1, 2, true);
        test.test(s, 0, 2, 1, 1, 2, false);
        test.test(s, 1, 1, 1, 1, 2, true);

        s = "\udc00\ud800";
        test.test(s, 0, 1, 0, 1, 1, true);
        test.test(s, 0, 2, 0, 1, 1, true);
        test.test(s, 0, 2, 0, 2, 2, true);
        test.test(s, 0, 2, 1, 1, 2, true);
        test.test(s, 1, 1, 1, 1, 2, true);

        s = "\ud800\ud800\udc00";
        test.test(s, 0, 1, 0, 1, 1, true);
        test.test(s, 0, 2, 0, 1, 1, true);
        test.test(s, 0, 2, 0, 2, 2, true);
        test.test(s, 0, 2, 1, 1, 2, true);
        test.test(s, 0, 3, 0, 1, 1, true);
        test.test(s, 0, 3, 0, 2, 3, true);
        test.test(s, 0, 3, 1, 1, 3, true);
        test.test(s, 0, 3, 2, 1, 3, false);
        test.test(s, 1, 1, 1, 1, 2, true);
        test.test(s, 1, 2, 1, 1, 3, true);
        test.test(s, 1, 2, 2, 1, 3, false);
        test.test(s, 2, 1, 2, 1, 3, true);

        s = "\ud800\udc00\udc00";
        test.test(s, 0, 1, 0, 1, 1, true);
        test.test(s, 0, 2, 0, 1, 2, true);
        test.test(s, 0, 2, 1, 1, 2, false);
        test.test(s, 0, 3, 0, 1, 2, true);
        test.test(s, 0, 3, 0, 2, 3, true);
        test.test(s, 0, 3, 1, 1, 2, false);
        test.test(s, 0, 3, 1, 2, 3, false);
        test.test(s, 0, 3, 2, 1, 3, true);
        test.test(s, 1, 1, 1, 1, 2, true);
        test.test(s, 1, 2, 1, 1, 2, true);
        test.test(s, 1, 2, 1, 2, 3, true);
        test.test(s, 1, 2, 2, 1, 3, true);
        test.test(s, 2, 1, 2, 1, 3, true);
    }
}
