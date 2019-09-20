/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.dev.test.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.PatternProps;
import android.icu.impl.UCharacterName;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterCategory;
import android.icu.lang.UCharacterDirection;
import android.icu.lang.UCharacterEnums;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.text.Normalizer2;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;
import android.icu.util.RangeValueIterator;
import android.icu.util.ULocale;
import android.icu.util.ValueIterator;
import android.icu.util.VersionInfo;

/**
* Testing class for UCharacter
* Mostly following the test cases for ICU
* @author Syn Wee Quek
* @since nov 04 2000
*/
public final class UCharacterTest extends TestFmwk
{
    // private variables =============================================

    /**
     * Expected Unicode version.
     */
    private final VersionInfo VERSION_ = VersionInfo.getInstance(9);

    // constructor ===================================================

    /**
    * Constructor
    */
    public UCharacterTest()
    {
    }

    // public methods ================================================

    /**
    * Testing the letter and number determination in UCharacter
    */
    @Test
    public void TestLetterNumber()
    {
        for (int i = 0x0041; i < 0x005B; i ++)
        if (!UCharacter.isLetter(i))
            errln("FAIL \\u" + hex(i) + " expected to be a letter");

        for (int i = 0x0660; i < 0x066A; i ++)
        if (UCharacter.isLetter(i))
            errln("FAIL \\u" + hex(i) + " expected not to be a letter");

        for (int i = 0x0660; i < 0x066A; i ++)
        if (!UCharacter.isDigit(i))
            errln("FAIL \\u" + hex(i) + " expected to be a digit");

        for (int i = 0x0041; i < 0x005B; i ++)
            if (!UCharacter.isLetterOrDigit(i))
                errln("FAIL \\u" + hex(i) + " expected not to be a digit");

        for (int i = 0x0660; i < 0x066A; i ++)
            if (!UCharacter.isLetterOrDigit(i))
                errln("FAIL \\u" + hex(i) +
                    "expected to be either a letter or a digit");

        /*
         * The following checks work only starting from Unicode 4.0.
         * Check the version number here.
         */
        VersionInfo version =    UCharacter.getUnicodeVersion();
        if(version.getMajor()<4 || version.equals(VersionInfo.getInstance(4, 0, 1))) {
            return;
        }



        /*
         * Sanity check:
         * Verify that exactly the digit characters have decimal digit values.
         * This assumption is used in the implementation of u_digit()
         * (which checks nt=de)
         * compared with the parallel java.lang.Character.digit()
         * (which checks Nd).
         *
         * This was not true in Unicode 3.2 and earlier.
         * Unicode 4.0 fixed discrepancies.
         * Unicode 4.0.1 re-introduced problems in this area due to an
         * unintentionally incomplete last-minute change.
         */
        String digitsPattern = "[:Nd:]";
        String decimalValuesPattern = "[:Numeric_Type=Decimal:]";

        UnicodeSet digits, decimalValues;

        digits= new UnicodeSet(digitsPattern);
        decimalValues=new UnicodeSet(decimalValuesPattern);


        compareUSets(digits, decimalValues, "[:Nd:]", "[:Numeric_Type=Decimal:]", true);


    }

    /**
    * Tests for space determination in UCharacter
    */
    @Test
    public void TestSpaces()
    {
        int spaces[] = {0x0020, 0x00a0, 0x2000, 0x2001, 0x2005};
        int nonspaces[] = {0x0061, 0x0062, 0x0063, 0x0064, 0x0074};
        int whitespaces[] = {0x2008, 0x2009, 0x200a, 0x001c, 0x000c /* ,0x200b */}; // 0x200b was "Zs" in Unicode 4.0, but it is "Cf" in Unicode 4.1
        int nonwhitespaces[] = {0x0061, 0x0062, 0x003c, 0x0028, 0x003f, 0x00a0, 0x2007, 0x202f, 0xfefe, 0x200b};

        int size = spaces.length;
        for (int i = 0; i < size; i ++)
        {
            if (!UCharacter.isSpaceChar(spaces[i]))
            {
                errln("FAIL \\u" + hex(spaces[i]) +
                    " expected to be a space character");
                break;
            }

            if (UCharacter.isSpaceChar(nonspaces[i]))
            {
                errln("FAIL \\u" + hex(nonspaces[i]) +
                " expected not to be space character");
                break;
            }

            if (!UCharacter.isWhitespace(whitespaces[i]))
            {
                errln("FAIL \\u" + hex(whitespaces[i]) +
                        " expected to be a white space character");
                break;
            }
            if (UCharacter.isWhitespace(nonwhitespaces[i]))
            {
                errln("FAIL \\u" + hex(nonwhitespaces[i]) +
                            " expected not to be a space character");
                break;
            }
            logln("Ok    \\u" + hex(spaces[i]) + " and \\u" +
                  hex(nonspaces[i]) + " and \\u" + hex(whitespaces[i]) +
                  " and \\u" + hex(nonwhitespaces[i]));
        }

        int patternWhiteSpace[] = {0x9, 0xd, 0x20, 0x85,
                                0x200e, 0x200f, 0x2028, 0x2029};
        int nonPatternWhiteSpace[] = {0x8, 0xe, 0x21, 0x86, 0xa0, 0xa1,
                                   0x1680, 0x1681, 0x180e, 0x180f,
                                   0x1FFF, 0x2000, 0x200a, 0x200b,
                                   0x2010, 0x202f, 0x2030, 0x205f,
                                   0x2060, 0x3000, 0x3001};
        for (int i = 0; i < patternWhiteSpace.length; i ++) {
            if (!PatternProps.isWhiteSpace(patternWhiteSpace[i])) {
                errln("\\u" + Utility.hex(patternWhiteSpace[i], 4)
                      + " expected to be a Pattern_White_Space");
            }
        }
        for (int i = 0; i < nonPatternWhiteSpace.length; i ++) {
            if (PatternProps.isWhiteSpace(nonPatternWhiteSpace[i])) {
                errln("\\u" + Utility.hex(nonPatternWhiteSpace[i], 4)
                      + " expected to be a non-Pattern_White_Space");
            }
        }

        // TODO: propose public API for constants like uchar.h's U_GC_*_MASK
        // (http://bugs.icu-project.org/trac/ticket/7461)
        int GC_Z_MASK =
            (1 << UCharacter.SPACE_SEPARATOR) |
            (1 << UCharacter.LINE_SEPARATOR) |
            (1 << UCharacter.PARAGRAPH_SEPARATOR);

        // UCharacter.isWhitespace(c) should be the same as Character.isWhitespace().
        // This uses logln() because Character.isWhitespace() differs between Java versions, thus
        // it is not necessarily an error if there is a difference between
        // particular Java and ICU versions.
        // However, you need to run tests with -v to see the output.
        // Also note that, at least as of Unicode 5.2,
        // there are no supplementary white space characters.
        for (int c = 0; c <= 0xffff; ++c) {
            boolean j = Character.isWhitespace(c);
            boolean i = UCharacter.isWhitespace(c);
            boolean u = UCharacter.isUWhiteSpace(c);
            boolean z = (UCharacter.getIntPropertyValue(c, UProperty.GENERAL_CATEGORY_MASK) &
                         GC_Z_MASK) != 0;
            if (j != i) {
                logln(String.format(
                    "isWhitespace(U+%04x) difference: JDK %5b ICU %5b Unicode WS %5b Z Separator %5b",
                    c, j, i, u, z));
            } else if (j || i || u || z) {
                logln(String.format(
                    "isWhitespace(U+%04x) FYI:        JDK %5b ICU %5b Unicode WS %5b Z Separator %5b",
                    c, j, i, u, z));
            }
        }
        for (char c = 0; c <= 0xff; ++c) {
            boolean j = Character.isSpace(c);
            boolean i = UCharacter.isSpace(c);
            boolean z = (UCharacter.getIntPropertyValue(c, UProperty.GENERAL_CATEGORY_MASK) &
                         GC_Z_MASK) != 0;
            if (j != i) {
                logln(String.format(
                    "isSpace(U+%04x) difference: JDK %5b ICU %5b Z Separator %5b",
                    (int)c, j, i, z));
            } else if (j || i || z) {
                logln(String.format(
                    "isSpace(U+%04x) FYI:        JDK %5b ICU %5b Z Separator %5b",
                    (int)c, j, i, z));
            }
        }
    }

    /**
     * Test various implementations of Pattern_Syntax & Pattern_White_Space.
     */
    @Test
    public void TestPatternProperties() {
        UnicodeSet syn_pp = new UnicodeSet();
        UnicodeSet syn_prop = new UnicodeSet("[:Pattern_Syntax:]");
        UnicodeSet syn_list = new UnicodeSet(
            "[!-/\\:-@\\[-\\^`\\{-~"+
            "\u00A1-\u00A7\u00A9\u00AB\u00AC\u00AE\u00B0\u00B1\u00B6\u00BB\u00BF\u00D7\u00F7"+
            "\u2010-\u2027\u2030-\u203E\u2041-\u2053\u2055-\u205E\u2190-\u245F\u2500-\u2775"+
            "\u2794-\u2BFF\u2E00-\u2E7F\u3001-\u3003\u3008-\u3020\u3030\uFD3E\uFD3F\uFE45\uFE46]");
        UnicodeSet ws_pp = new UnicodeSet();
        UnicodeSet ws_prop = new UnicodeSet("[:Pattern_White_Space:]");
        UnicodeSet ws_list = new UnicodeSet("[\\u0009-\\u000D\\ \\u0085\\u200E\\u200F\\u2028\\u2029]");
        UnicodeSet syn_ws_pp = new UnicodeSet();
        UnicodeSet syn_ws_prop = new UnicodeSet(syn_prop).addAll(ws_prop);
        for(int c=0; c<=0xffff; ++c) {
            if(PatternProps.isSyntax(c)) {
                syn_pp.add(c);
            }
            if(PatternProps.isWhiteSpace(c)) {
                ws_pp.add(c);
            }
            if(PatternProps.isSyntaxOrWhiteSpace(c)) {
                syn_ws_pp.add(c);
            }
        }
        compareUSets(syn_pp, syn_prop,
                     "PatternProps.isSyntax()", "[:Pattern_Syntax:]", true);
        compareUSets(syn_pp, syn_list,
                     "PatternProps.isSyntax()", "[Pattern_Syntax ranges]", true);
        compareUSets(ws_pp, ws_prop,
                     "PatternProps.isWhiteSpace()", "[:Pattern_White_Space:]", true);
        compareUSets(ws_pp, ws_list,
                     "PatternProps.isWhiteSpace()", "[Pattern_White_Space ranges]", true);
        compareUSets(syn_ws_pp, syn_ws_prop,
                     "PatternProps.isSyntaxOrWhiteSpace()",
                     "[[:Pattern_Syntax:][:Pattern_White_Space:]]", true);
    }

    /**
    * Tests for defined and undefined characters
    */
    @Test
    public void TestDefined()
    {
        int undefined[] = {0xfff1, 0xfff7, 0xfa6e};
        int defined[] = {0x523E, 0x004f88, 0x00fffd};

        int size = undefined.length;
        for (int i = 0; i < size; i ++)
        {
            if (UCharacter.isDefined(undefined[i]))
            {
                errln("FAIL \\u" + hex(undefined[i]) +
                            " expected not to be defined");
                break;
            }
            if (!UCharacter.isDefined(defined[i]))
            {
                errln("FAIL \\u" + hex(defined[i]) + " expected defined");
                break;
            }
        }
    }

    /**
    * Tests for base characters and their cellwidth
    */
    @Test
    public void TestBase()
    {
        int base[] = {0x0061, 0x000031, 0x0003d2};
        int nonbase[] = {0x002B, 0x000020, 0x00203B};
        int size = base.length;
        for (int i = 0; i < size; i ++)
        {
            if (UCharacter.isBaseForm(nonbase[i]))
            {
                errln("FAIL \\u" + hex(nonbase[i]) +
                            " expected not to be a base character");
                break;
            }
            if (!UCharacter.isBaseForm(base[i]))
            {
                errln("FAIL \\u" + hex(base[i]) +
                      " expected to be a base character");
                break;
            }
        }
    }

    /**
    * Tests for digit characters
    */
    @Test
    public void TestDigits()
    {
        int digits[] = {0x0030, 0x000662, 0x000F23, 0x000ED5, 0x002160};

        //special characters not in the properties table
        int digits2[] = {0x3007, 0x004e00, 0x004e8c, 0x004e09, 0x0056d8,
                         0x004e94, 0x00516d, 0x4e03, 0x00516b, 0x004e5d};
        int nondigits[] = {0x0010, 0x000041, 0x000122, 0x0068FE};

        int digitvalues[] = {0, 2, 3, 5, 1};
        int digitvalues2[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        int size  = digits.length;
        for (int i = 0; i < size; i ++) {
            if (UCharacter.isDigit(digits[i]) &&
                UCharacter.digit(digits[i]) != digitvalues[i])
            {
                errln("FAIL \\u" + hex(digits[i]) +
                        " expected digit with value " + digitvalues[i]);
                break;
            }
        }
        size = nondigits.length;
        for (int i = 0; i < size; i ++)
            if (UCharacter.isDigit(nondigits[i]))
            {
                errln("FAIL \\u" + hex(nondigits[i]) + " expected nondigit");
                break;
            }

        size = digits2.length;
        for (int i = 0; i < 10; i ++) {
            if (UCharacter.isDigit(digits2[i]) &&
                UCharacter.digit(digits2[i]) != digitvalues2[i])
            {
                errln("FAIL \\u" + hex(digits2[i]) +
                    " expected digit with value " + digitvalues2[i]);
                break;
            }
        }
    }

    /**
    *  Tests for numeric characters
    */
    @Test
    public void TestNumeric()
    {
        if (UCharacter.getNumericValue(0x00BC) != -2) {
            errln("Numeric value of 0x00BC expected to be -2");
        }

        for (int i = '0'; i < '9'; i ++) {
            int n1 = UCharacter.getNumericValue(i);
            double n2 = UCharacter.getUnicodeNumericValue(i);
            if (n1 != n2 ||  n1 != (i - '0')) {
                errln("Numeric value of " + (char)i + " expected to be " +
                      (i - '0'));
            }
        }
        for (int i = 'A'; i < 'F'; i ++) {
            int n1 = UCharacter.getNumericValue(i);
            double n2 = UCharacter.getUnicodeNumericValue(i);
            if (n2 != UCharacter.NO_NUMERIC_VALUE ||  n1 != (i - 'A' + 10)) {
                errln("Numeric value of " + (char)i + " expected to be " +
                      (i - 'A' + 10));
            }
        }
        for (int i = 0xFF21; i < 0xFF26; i ++) {
            // testing full wideth latin characters A-F
            int n1 = UCharacter.getNumericValue(i);
            double n2 = UCharacter.getUnicodeNumericValue(i);
            if (n2 != UCharacter.NO_NUMERIC_VALUE ||  n1 != (i - 0xFF21 + 10)) {
                errln("Numeric value of " + (char)i + " expected to be " +
                      (i - 0xFF21 + 10));
            }
        }
        // testing han numbers
        int han[] = {0x96f6, 0, 0x58f9, 1, 0x8cb3, 2, 0x53c3, 3,
                     0x8086, 4, 0x4f0d, 5, 0x9678, 6, 0x67d2, 7,
                     0x634c, 8, 0x7396, 9, 0x5341, 10, 0x62fe, 10,
                     0x767e, 100, 0x4f70, 100, 0x5343, 1000, 0x4edf, 1000,
                     0x824c, 10000, 0x5104, 100000000};
        for (int i = 0; i < han.length; i += 2) {
            if (UCharacter.getHanNumericValue(han[i]) != han[i + 1]) {
                errln("Numeric value of \\u" +
                      Integer.toHexString(han[i]) + " expected to be " +
                      han[i + 1]);
            }
        }
    }

    /**
    * Tests for version
    */
    @Test
    public void TestVersion()
    {
        if (!UCharacter.getUnicodeVersion().equals(VERSION_))
            errln("FAIL expected: " + VERSION_ + " got: " + UCharacter.getUnicodeVersion());
    }

    /**
    * Tests for control characters
    */
    @Test
    public void TestISOControl()
    {
        int control[] = {0x001b, 0x000097, 0x000082};
        int noncontrol[] = {0x61, 0x000031, 0x0000e2};

        int size = control.length;
        for (int i = 0; i < size; i ++)
        {
            if (!UCharacter.isISOControl(control[i]))
            {
                errln("FAIL 0x" + Integer.toHexString(control[i]) +
                        " expected to be a control character");
                break;
            }
            if (UCharacter.isISOControl(noncontrol[i]))
            {
                errln("FAIL 0x" + Integer.toHexString(noncontrol[i]) +
                        " expected to be not a control character");
                break;
            }

            logln("Ok    0x" + Integer.toHexString(control[i]) + " and 0x" +
                    Integer.toHexString(noncontrol[i]));
        }
    }

    /**
     * Test Supplementary
     */
    @Test
    public void TestSupplementary()
    {
        for (int i = 0; i < 0x10000; i ++) {
            if (UCharacter.isSupplementary(i)) {
                errln("Codepoint \\u" + Integer.toHexString(i) +
                      " is not supplementary");
            }
        }
        for (int i = 0x10000; i < 0x10FFFF; i ++) {
            if (!UCharacter.isSupplementary(i)) {
                errln("Codepoint \\u" + Integer.toHexString(i) +
                      " is supplementary");
            }
        }
    }

    /**
     * Test mirroring
     */
    @Test
    public void TestMirror()
    {
        if (!(UCharacter.isMirrored(0x28) && UCharacter.isMirrored(0xbb) &&
              UCharacter.isMirrored(0x2045) && UCharacter.isMirrored(0x232a)
              && !UCharacter.isMirrored(0x27) &&
              !UCharacter.isMirrored(0x61) && !UCharacter.isMirrored(0x284)
              && !UCharacter.isMirrored(0x3400))) {
            errln("isMirrored() does not work correctly");
        }

        if (!(UCharacter.getMirror(0x3c) == 0x3e &&
              UCharacter.getMirror(0x5d) == 0x5b &&
              UCharacter.getMirror(0x208d) == 0x208e &&
              UCharacter.getMirror(0x3017) == 0x3016 &&

              UCharacter.getMirror(0xbb) == 0xab &&
              UCharacter.getMirror(0x2215) == 0x29F5 &&
              UCharacter.getMirror(0x29F5) == 0x2215 && /* large delta between the code points */

              UCharacter.getMirror(0x2e) == 0x2e &&
              UCharacter.getMirror(0x6f3) == 0x6f3 &&
              UCharacter.getMirror(0x301c) == 0x301c &&
              UCharacter.getMirror(0xa4ab) == 0xa4ab &&

              /* see Unicode Corrigendum #6 at http://www.unicode.org/versions/corrigendum6.html */
              UCharacter.getMirror(0x2018) == 0x2018 &&
              UCharacter.getMirror(0x201b) == 0x201b &&
              UCharacter.getMirror(0x301d) == 0x301d)) {
            errln("getMirror() does not work correctly");
        }

        /* verify that Bidi_Mirroring_Glyph roundtrips */
        UnicodeSet set=new UnicodeSet("[:Bidi_Mirrored:]");
        UnicodeSetIterator iter=new UnicodeSetIterator(set);
        int start, end, c2, c3;
        while(iter.nextRange() && (start=iter.codepoint)>=0) {
            end=iter.codepointEnd;
            do {
                c2=UCharacter.getMirror(start);
                c3=UCharacter.getMirror(c2);
                if(c3!=start) {
                    errln("getMirror() does not roundtrip: U+"+hex(start)+"->U+"+hex(c2)+"->U+"+hex(c3));
                }
                c3=UCharacter.getBidiPairedBracket(start);
                if(UCharacter.getIntPropertyValue(start, UProperty.BIDI_PAIRED_BRACKET_TYPE)==UCharacter.BidiPairedBracketType.NONE) {
                    if(c3!=start) {
                        errln("u_getBidiPairedBracket(U+"+hex(start)+") != self for bpt(c)==None");
                    }
                } else {
                    if(c3!=c2) {
                        errln("u_getBidiPairedBracket(U+"+hex(start)+") != U+"+hex(c2)+" = bmg(c)'");
                    }
                }
            } while(++start<=end);
        }

        // verify that Unicode Corrigendum #6 reverts mirrored status of the following
        if (UCharacter.isMirrored(0x2018) ||
            UCharacter.isMirrored(0x201d) ||
            UCharacter.isMirrored(0x201f) ||
            UCharacter.isMirrored(0x301e)) {
            errln("Unicode Corrigendum #6 conflict, one or more of 2018/201d/201f/301e has mirrored property");
        }
    }

    /**
    * Tests for printable characters
    */
    @Test
    public void TestPrint()
    {
        int printable[] = {0x0042, 0x00005f, 0x002014};
        int nonprintable[] = {0x200c, 0x00009f, 0x00001b};

        int size = printable.length;
        for (int i = 0; i < size; i ++)
        {
            if (!UCharacter.isPrintable(printable[i]))
            {
                errln("FAIL \\u" + hex(printable[i]) +
                    " expected to be a printable character");
                break;
            }
            if (UCharacter.isPrintable(nonprintable[i]))
            {
                errln("FAIL \\u" + hex(nonprintable[i]) +
                        " expected not to be a printable character");
                break;
            }
            logln("Ok    \\u" + hex(printable[i]) + " and \\u" +
                    hex(nonprintable[i]));
        }

        // test all ISO 8 controls
        for (int ch = 0; ch <= 0x9f; ++ ch) {
            if (ch == 0x20) {
                // skip ASCII graphic characters and continue with DEL
                ch = 0x7f;
            }
            if (UCharacter.isPrintable(ch)) {
                errln("Fail \\u" + hex(ch) +
                    " is a ISO 8 control character hence not printable\n");
            }
        }

        /* test all Latin-1 graphic characters */
        for (int ch = 0x20; ch <= 0xff; ++ ch) {
            if (ch == 0x7f) {
                ch = 0xa0;
            }
            if (!UCharacter.isPrintable(ch)
                && ch != 0x00AD/* Unicode 4.0 changed the defintion of soft hyphen to be a Cf*/) {
                errln("Fail \\u" + hex(ch) +
                      " is a Latin-1 graphic character\n");
            }
        }
    }

    /**
    * Testing for identifier characters
    */
    @Test
    public void TestIdentifier()
    {
        int unicodeidstart[] = {0x0250, 0x0000e2, 0x000061};
        int nonunicodeidstart[] = {0x2000, 0x00000a, 0x002019};
        int unicodeidpart[] = {0x005f, 0x000032, 0x000045};
        int nonunicodeidpart[] = {0x2030, 0x0000a3, 0x000020};
        int idignore[] = {0x0006, 0x0010, 0x206b};
        int nonidignore[] = {0x0075, 0x0000a3, 0x000061};

        int size = unicodeidstart.length;
        for (int i = 0; i < size; i ++)
        {
            if (!UCharacter.isUnicodeIdentifierStart(unicodeidstart[i]))
            {
                errln("FAIL \\u" + hex(unicodeidstart[i]) +
                    " expected to be a unicode identifier start character");
                break;
            }
            if (UCharacter.isUnicodeIdentifierStart(nonunicodeidstart[i]))
            {
                errln("FAIL \\u" + hex(nonunicodeidstart[i]) +
                        " expected not to be a unicode identifier start " +
                        "character");
                break;
            }
            if (!UCharacter.isUnicodeIdentifierPart(unicodeidpart[i]))
            {
                errln("FAIL \\u" + hex(unicodeidpart[i]) +
                    " expected to be a unicode identifier part character");
                break;
            }
            if (UCharacter.isUnicodeIdentifierPart(nonunicodeidpart[i]))
            {
                errln("FAIL \\u" + hex(nonunicodeidpart[i]) +
                        " expected not to be a unicode identifier part " +
                        "character");
                break;
            }
            if (!UCharacter.isIdentifierIgnorable(idignore[i]))
            {
                errln("FAIL \\u" + hex(idignore[i]) +
                        " expected to be a ignorable unicode character");
                break;
            }
            if (UCharacter.isIdentifierIgnorable(nonidignore[i]))
            {
                errln("FAIL \\u" + hex(nonidignore[i]) +
                    " expected not to be a ignorable unicode character");
                break;
            }
            logln("Ok    \\u" + hex(unicodeidstart[i]) + " and \\u" +
                    hex(nonunicodeidstart[i]) + " and \\u" +
                    hex(unicodeidpart[i]) + " and \\u" +
                    hex(nonunicodeidpart[i]) + " and \\u" +
                    hex(idignore[i]) + " and \\u" + hex(nonidignore[i]));
        }
    }

    /**
    * Tests for the character types, direction.<br>
    * This method reads in UnicodeData.txt file for testing purposes. A
    * default path is provided relative to the src path, however the user
    * could set a system property to change the directory path.<br>
    * e.g. java -DUnicodeData="data_directory_path"
    * android.icu.dev.test.lang.UCharacterTest
    */
    @Test
    public void TestUnicodeData()
    {
        // this is the 2 char category types used in the UnicodeData file
        final String TYPE =
            "LuLlLtLmLoMnMeMcNdNlNoZsZlZpCcCfCoCsPdPsPePcPoSmScSkSoPiPf";

        // directorionality types used in the UnicodeData file
        // padded by spaces to make each type size 4
        final String DIR =
            "L   R   EN  ES  ET  AN  CS  B   S   WS  ON  LRE LRO AL  RLE RLO PDF NSM BN  FSI LRI RLI PDI ";

        Normalizer2 nfc = Normalizer2.getNFCInstance();
        Normalizer2 nfkc = Normalizer2.getNFKCInstance();

        BufferedReader input = null;
        try {
            input = TestUtil.getDataReader("unicode/UnicodeData.txt");
            int numErrors = 0;

            for (;;) {
                String s = input.readLine();
                if(s == null) {
                    break;
                }
                if(s.length()<4 || s.startsWith("#")) {
                    continue;
                }
                String[] fields = s.split(";", -1);
                assert (fields.length == 15 ) : "Number of fields is " + fields.length + ": " + s;

                int ch = Integer.parseInt(fields[0], 16);

                // testing the general category
                int type = TYPE.indexOf(fields[2]);
                if (type < 0)
                    type = 0;
                else
                    type = (type >> 1) + 1;
                if (UCharacter.getType(ch) != type)
                {
                    errln("FAIL \\u" + hex(ch) + " expected type " + type);
                    break;
                }

                if (UCharacter.getIntPropertyValue(ch,
                           UProperty.GENERAL_CATEGORY_MASK) != (1 << type)) {
                    errln("error: getIntPropertyValue(\\u" +
                          Integer.toHexString(ch) +
                          ", UProperty.GENERAL_CATEGORY_MASK) != " +
                          "getMask(getType(ch))");
                }

                // testing combining class
                int cc = Integer.parseInt(fields[3]);
                if (UCharacter.getCombiningClass(ch) != cc)
                {
                    errln("FAIL \\u" + hex(ch) + " expected combining " +
                            "class " + cc);
                    break;
                }
                if (nfkc.getCombiningClass(ch) != cc)
                {
                    errln("FAIL \\u" + hex(ch) + " expected NFKC combining " +
                            "class " + cc);
                    break;
                }

                // testing the direction
                String d = fields[4];
                if (d.length() == 1)
                    d = d + "   ";

                int dir = DIR.indexOf(d) >> 2;
                if (UCharacter.getDirection(ch) != dir)
                {
                    errln("FAIL \\u" + hex(ch) +
                        " expected direction " + dir + " but got " + UCharacter.getDirection(ch));
                    break;
                }

                byte bdir = (byte)dir;
                if (UCharacter.getDirectionality(ch) != bdir)
                {
                    errln("FAIL \\u" + hex(ch) +
                        " expected directionality " + bdir + " but got " +
                        UCharacter.getDirectionality(ch));
                    break;
                }

                /* get Decomposition_Type & Decomposition_Mapping, field 5 */
                int dt;
                if(fields[5].length()==0) {
                    /* no decomposition, except UnicodeData.txt omits Hangul syllable decompositions */
                    if(ch==0xac00 || ch==0xd7a3) {
                        dt=UCharacter.DecompositionType.CANONICAL;
                    } else {
                        dt=UCharacter.DecompositionType.NONE;
                    }
                } else {
                    d=fields[5];
                    dt=-1;
                    if(d.charAt(0)=='<') {
                        int end=d.indexOf('>', 1);
                        if(end>=0) {
                            dt=UCharacter.getPropertyValueEnum(UProperty.DECOMPOSITION_TYPE, d.substring(1, end));
                            while(d.charAt(++end)==' ') {}  // skip spaces
                            d=d.substring(end);
                        }
                    } else {
                        dt=UCharacter.DecompositionType.CANONICAL;
                    }
                }
                String dm;
                if(dt>UCharacter.DecompositionType.NONE) {
                    if(ch==0xac00) {
                        dm="\u1100\u1161";
                    } else if(ch==0xd7a3) {
                        dm="\ud788\u11c2";
                    } else {
                        String[] dmChars=d.split(" +");
                        StringBuilder dmb=new StringBuilder(dmChars.length);
                        for(String dmc : dmChars) {
                            dmb.appendCodePoint(Integer.parseInt(dmc, 16));
                        }
                        dm=dmb.toString();
                    }
                } else {
                    dm=null;
                }
                if(dt<0) {
                    errln(String.format("error in UnicodeData.txt: syntax error in U+%04x decomposition field", ch));
                    return;
                }
                int i=UCharacter.getIntPropertyValue(ch, UProperty.DECOMPOSITION_TYPE);
                assertEquals(
                        String.format("error: UCharacter.getIntPropertyValue(U+%04x, UProperty.DECOMPOSITION_TYPE) is wrong", ch),
                        dt, i);
                /* Expect Decomposition_Mapping=nfkc.getRawDecomposition(c). */
                String mapping=nfkc.getRawDecomposition(ch);
                assertEquals(
                        String.format("error: nfkc.getRawDecomposition(U+%04x) is wrong", ch),
                        dm, mapping);
                /* For canonical decompositions only, expect Decomposition_Mapping=nfc.getRawDecomposition(c). */
                if(dt!=UCharacter.DecompositionType.CANONICAL) {
                    dm=null;
                }
                mapping=nfc.getRawDecomposition(ch);
                assertEquals(
                        String.format("error: nfc.getRawDecomposition(U+%04x) is wrong", ch),
                        dm, mapping);
                /* recompose */
                if(dt==UCharacter.DecompositionType.CANONICAL
                        && !UCharacter.hasBinaryProperty(ch, UProperty.FULL_COMPOSITION_EXCLUSION)) {
                    int a=dm.codePointAt(0);
                    int b=dm.codePointBefore(dm.length());
                    int composite=nfc.composePair(a, b);
                    assertEquals(
                            String.format(
                                    "error: nfc U+%04X decomposes to U+%04X+U+%04X "+
                                    "but does not compose back (instead U+%04X)",
                                    ch, a, b, composite),
                            ch, composite);
                    /*
                     * Note: NFKC has fewer round-trip mappings than NFC,
                     * so we can't just test nfkc.composePair(a, b) here without further data.
                     */
                }

                // testing iso comment
                try{
                    String isocomment = fields[11];
                    String comment = UCharacter.getISOComment(ch);
                    if (comment == null) {
                        comment = "";
                    }
                    if (!comment.equals(isocomment)) {
                        errln("FAIL \\u" + hex(ch) +
                            " expected iso comment " + isocomment);
                        break;
                    }
                }catch(Exception e){
                    if(e.getMessage().indexOf("unames.icu") >= 0){
                        numErrors++;
                    }else{
                        throw e;
                    }
                }

                String upper = fields[12];
                int tempchar = ch;
                if (upper.length() > 0) {
                    tempchar = Integer.parseInt(upper, 16);
                }
                int resultCp = UCharacter.toUpperCase(ch);
                if (resultCp != tempchar) {
                    errln("FAIL \\u" + Utility.hex(ch, 4)
                            + " expected uppercase \\u"
                            + Utility.hex(tempchar, 4)
                            + " but got \\u"
                            + Utility.hex(resultCp, 4));
                    break;
                }

                String lower = fields[13];
                tempchar = ch;
                if (lower.length() > 0) {
                    tempchar = Integer.parseInt(lower, 16);
                }
                if (UCharacter.toLowerCase(ch) != tempchar) {
                    errln("FAIL \\u" + Utility.hex(ch, 4)
                            + " expected lowercase \\u"
                            + Utility.hex(tempchar, 4));
                    break;
                }



                String title = fields[14];
                tempchar = ch;
                if (title.length() > 0) {
                    tempchar = Integer.parseInt(title, 16);
                }
                if (UCharacter.toTitleCase(ch) != tempchar) {
                    errln("FAIL \\u" + Utility.hex(ch, 4)
                            + " expected titlecase \\u"
                            + Utility.hex(tempchar, 4));
                    break;
                }
            }
            if(numErrors > 0){
                warnln("Could not find unames.icu");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (UCharacter.UnicodeBlock.of(0x0041)
                                        != UCharacter.UnicodeBlock.BASIC_LATIN
            || UCharacter.getIntPropertyValue(0x41, UProperty.BLOCK)
                              != UCharacter.UnicodeBlock.BASIC_LATIN.getID()) {
            errln("UCharacter.UnicodeBlock.of(\\u0041) property failed! "
                    + "Expected : "
                    + UCharacter.UnicodeBlock.BASIC_LATIN.getID() + " got "
                    + UCharacter.UnicodeBlock.of(0x0041));
        }

        // sanity check on repeated properties
        for (int ch = 0xfffe; ch <= 0x10ffff;) {
            int type = UCharacter.getType(ch);
            if (UCharacter.getIntPropertyValue(ch,
                                               UProperty.GENERAL_CATEGORY_MASK)
                != (1 << type)) {
                errln("error: UCharacter.getIntPropertyValue(\\u"
                      + Integer.toHexString(ch)
                      + ", UProperty.GENERAL_CATEGORY_MASK) != "
                      + "getMask(getType())");
            }
            if (type != UCharacterCategory.UNASSIGNED) {
                errln("error: UCharacter.getType(\\u" + Utility.hex(ch, 4)
                        + " != UCharacterCategory.UNASSIGNED (returns "
                        + UCharacterCategory.toString(UCharacter.getType(ch))
                        + ")");
            }
            if ((ch & 0xffff) == 0xfffe) {
                ++ ch;
            }
            else {
                ch += 0xffff;
            }
        }

        // test that PUA is not "unassigned"
        for(int ch = 0xe000; ch <= 0x10fffd;) {
            int type = UCharacter.getType(ch);
            if (UCharacter.getIntPropertyValue(ch,
                                               UProperty.GENERAL_CATEGORY_MASK)
                != (1 << type)) {
                errln("error: UCharacter.getIntPropertyValue(\\u"
                      + Integer.toHexString(ch)
                      + ", UProperty.GENERAL_CATEGORY_MASK) != "
                      + "getMask(getType())");
            }

            if (type == UCharacterCategory.UNASSIGNED) {
                errln("error: UCharacter.getType(\\u"
                        + Utility.hex(ch, 4)
                        + ") == UCharacterCategory.UNASSIGNED");
            }
            else if (type != UCharacterCategory.PRIVATE_USE) {
                logln("PUA override: UCharacter.getType(\\u"
                      + Utility.hex(ch, 4) + ")=" + type);
            }
            if (ch == 0xf8ff) {
                ch = 0xf0000;
            }
            else if (ch == 0xffffd) {
                ch = 0x100000;
            }
            else {
                ++ ch;
            }
        }
    }


    /**
    * Test for the character names
    */
    @Test
    public void TestNames()
    {
        try{
            int length = UCharacterName.INSTANCE.getMaxCharNameLength();
            if (length < 83) { // Unicode 3.2 max char name length
               errln("getMaxCharNameLength()=" + length + " is too short");
            }

            int c[] = {0x0061,                //LATIN SMALL LETTER A
                       0x000284,              //LATIN SMALL LETTER DOTLESS J WITH STROKE AND HOOK
                       0x003401,              //CJK UNIFIED IDEOGRAPH-3401
                       0x007fed,              //CJK UNIFIED IDEOGRAPH-7FED
                       0x00ac00,              //HANGUL SYLLABLE GA
                       0x00d7a3,              //HANGUL SYLLABLE HIH
                       0x00d800, 0x00dc00,    //LINEAR B SYLLABLE B008 A
                       0xff08,                //FULLWIDTH LEFT PARENTHESIS
                       0x00ffe5,              //FULLWIDTH YEN SIGN
                       0x00ffff,              //null
                       0x0023456              //CJK UNIFIED IDEOGRAPH-23456
                       };
            String name[] = {
                             "LATIN SMALL LETTER A",
                             "LATIN SMALL LETTER DOTLESS J WITH STROKE AND HOOK",
                             "CJK UNIFIED IDEOGRAPH-3401",
                             "CJK UNIFIED IDEOGRAPH-7FED",
                             "HANGUL SYLLABLE GA",
                             "HANGUL SYLLABLE HIH",
                             "",
                             "",
                             "FULLWIDTH LEFT PARENTHESIS",
                             "FULLWIDTH YEN SIGN",
                             "",
                             "CJK UNIFIED IDEOGRAPH-23456"
                             };
            String oldname[] = {"", "", "",
                            "",
                            "", "", "", "", "", "",
                            "", ""};
            String extendedname[] = {"LATIN SMALL LETTER A",
                                 "LATIN SMALL LETTER DOTLESS J WITH STROKE AND HOOK",
                                 "CJK UNIFIED IDEOGRAPH-3401",
                                 "CJK UNIFIED IDEOGRAPH-7FED",
                                 "HANGUL SYLLABLE GA",
                                 "HANGUL SYLLABLE HIH",
                                 "<lead surrogate-D800>",
                                 "<trail surrogate-DC00>",
                                 "FULLWIDTH LEFT PARENTHESIS",
                                 "FULLWIDTH YEN SIGN",
                                 "<noncharacter-FFFF>",
                                 "CJK UNIFIED IDEOGRAPH-23456"};

            int size = c.length;
            String str;
            int uc;

            for (int i = 0; i < size; i ++)
            {
                // modern Unicode character name
                str = UCharacter.getName(c[i]);
                if ((str == null && name[i].length() > 0) ||
                    (str != null && !str.equals(name[i])))
                {
                    errln("FAIL \\u" + hex(c[i]) + " expected name " +
                            name[i]);
                    break;
                }

                // 1.0 Unicode character name
                str = UCharacter.getName1_0(c[i]);
                if ((str == null && oldname[i].length() > 0) ||
                    (str != null && !str.equals(oldname[i])))
                {
                    errln("FAIL \\u" + hex(c[i]) + " expected 1.0 name " +
                            oldname[i]);
                    break;
                }

                // extended character name
                str = UCharacter.getExtendedName(c[i]);
                if (str == null || !str.equals(extendedname[i]))
                {
                    errln("FAIL \\u" + hex(c[i]) + " expected extended name " +
                            extendedname[i]);
                    break;
                }

                // retrieving unicode character from modern name
                uc = UCharacter.getCharFromName(name[i]);
                if (uc != c[i] && name[i].length() != 0)
                {
                    errln("FAIL " + name[i] + " expected character \\u" +
                          hex(c[i]));
                    break;
                }

                //retrieving unicode character from 1.0 name
                uc = UCharacter.getCharFromName1_0(oldname[i]);
                if (uc != c[i] && oldname[i].length() != 0)
                {
                    errln("FAIL " + oldname[i] + " expected 1.0 character \\u" +
                          hex(c[i]));
                    break;
                }

                //retrieving unicode character from 1.0 name
                uc = UCharacter.getCharFromExtendedName(extendedname[i]);
                if (uc != c[i] && i != 0 && (i == 1 || i == 6))
                {
                    errln("FAIL " + extendedname[i] +
                          " expected extended character \\u" + hex(c[i]));
                    break;
                }
            }

            // test getName works with mixed-case names (new in 2.0)
            if (0x61 != UCharacter.getCharFromName("LATin smALl letTER A")) {
                errln("FAIL: 'LATin smALl letTER A' should result in character "
                      + "U+0061");
            }

            if (TestFmwk.getExhaustiveness() >= 5) {
                // extra testing different from icu
                for (int i = UCharacter.MIN_VALUE; i < UCharacter.MAX_VALUE; i ++)
                {
                    str = UCharacter.getName(i);
                    if (str != null && UCharacter.getCharFromName(str) != i)
                    {
                        errln("FAIL \\u" + hex(i) + " " + str  +
                                            " retrieval of name and vice versa" );
                        break;
                    }
                }
            }

            // Test getCharNameCharacters
            if (TestFmwk.getExhaustiveness() >= 10) {
                boolean map[] = new boolean[256];

                UnicodeSet set = new UnicodeSet(1, 0); // empty set
                UnicodeSet dumb = new UnicodeSet(1, 0); // empty set

                // uprv_getCharNameCharacters() will likely return more lowercase
                // letters than actual character names contain because
                // it includes all the characters in lowercased names of
                // general categories, for the full possible set of extended names.
                UCharacterName.INSTANCE.getCharNameCharacters(set);

                // build set the dumb (but sure-fire) way
                Arrays.fill(map, false);

                int maxLength = 0;
                for (int cp = 0; cp < 0x110000; ++ cp) {
                    String n = UCharacter.getExtendedName(cp);
                    int len = n.length();
                    if (len > maxLength) {
                        maxLength = len;
                    }

                    for (int i = 0; i < len; ++ i) {
                        char ch = n.charAt(i);
                        if (!map[ch & 0xff]) {
                            dumb.add(ch);
                            map[ch & 0xff] = true;
                        }
                    }
                }

                length = UCharacterName.INSTANCE.getMaxCharNameLength();
                if (length != maxLength) {
                    errln("getMaxCharNameLength()=" + length
                          + " differs from the maximum length " + maxLength
                          + " of all extended names");
                }

                // compare the sets.  Where is my uset_equals?!!
                boolean ok = true;
                for (int i = 0; i < 256; ++ i) {
                    if (set.contains(i) != dumb.contains(i)) {
                        if (0x61 <= i && i <= 0x7a // a-z
                            && set.contains(i) && !dumb.contains(i)) {
                            // ignore lowercase a-z that are in set but not in dumb
                            ok = true;
                        }
                        else {
                            ok = false;
                            break;
                        }
                    }
                }

                String pattern1 = set.toPattern(true);
                String pattern2 = dumb.toPattern(true);

                if (!ok) {
                    errln("FAIL: getCharNameCharacters() returned " + pattern1
                          + " expected " + pattern2
                          + " (too many lowercase a-z are ok)");
                } else {
                    logln("Ok: getCharNameCharacters() returned " + pattern1);
                }
            }
            // improve code coverage
           String expected = "LATIN SMALL LETTER A|LATIN SMALL LETTER DOTLESS J WITH STROKE AND HOOK|"+
                             "CJK UNIFIED IDEOGRAPH-3401|CJK UNIFIED IDEOGRAPH-7FED|HANGUL SYLLABLE GA|"+
                             "HANGUL SYLLABLE HIH|LINEAR B SYLLABLE B008 A|FULLWIDTH LEFT PARENTHESIS|"+
                             "FULLWIDTH YEN SIGN|"+
                             "null|"+ // getName returns null because 0xFFFF does not have a name, but has an extended name!
                             "CJK UNIFIED IDEOGRAPH-23456";
           String separator= "|";
           String source = Utility.valueOf(c);
           String result = UCharacter.getName(source, separator);
           if(!result.equals(expected)){
               errln("UCharacter.getName did not return the expected result.\n\t Expected: "+ expected+"\n\t Got: "+ result);
           }

        }catch(IllegalArgumentException e){
            if(e.getMessage().indexOf("unames.icu") >= 0){
                warnln("Could not find unames.icu");
            }else{
                throw e;
            }
        }

    }

    @Test
    public void TestUCharFromNameUnderflow() {
        // Ticket #10889: Underflow crash when there is no dash.
        int c = UCharacter.getCharFromExtendedName("<NO BREAK SPACE>");
        if(c >= 0) {
            errln("UCharacter.getCharFromExtendedName(<NO BREAK SPACE>) = U+" + hex(c) +
                    " but should fail (-1)");
        }

        // Test related edge cases.
        c = UCharacter.getCharFromExtendedName("<-00a0>");
        if(c >= 0) {
            errln("UCharacter.getCharFromExtendedName(<-00a0>) = U+" + hex(c) +
                    " but should fail (-1)");
        }

        c = UCharacter.getCharFromExtendedName("<control->");
        if(c >= 0) {
            errln("UCharacter.getCharFromExtendedName(<control->) = U+" + hex(c) +
                    " but should fail (-1)");
        }

        c = UCharacter.getCharFromExtendedName("<control-111111>");
        if(c >= 0) {
            errln("UCharacter.getCharFromExtendedName(<control-111111>) = U+" + hex(c) +
                    " but should fail (-1)");
        }
    }

    /**
    * Testing name iteration
    */
    @Test
    public void TestNameIteration()throws Exception
    {
        try {
            ValueIterator iterator = UCharacter.getExtendedNameIterator();
            ValueIterator.Element element = new ValueIterator.Element();
            ValueIterator.Element old     = new ValueIterator.Element();
            // testing subrange
            iterator.setRange(-10, -5);
            if (iterator.next(element)) {
                errln("Fail, expected iterator to return false when range is set outside the meaningful range");
            }
            iterator.setRange(0x110000, 0x111111);
            if (iterator.next(element)) {
                errln("Fail, expected iterator to return false when range is set outside the meaningful range");
            }
            try {
                iterator.setRange(50, 10);
                errln("Fail, expected exception when encountered invalid range");
            } catch (Exception e) {
            }

            iterator.setRange(-10, 10);
            if (!iterator.next(element) || element.integer != 0) {
                errln("Fail, expected iterator to return 0 when range start limit is set outside the meaningful range");
            }

            iterator.setRange(0x10FFFE, 0x200000);
            int last = 0;
            while (iterator.next(element)) {
                last = element.integer;
            }
            if (last != 0x10FFFF) {
                errln("Fail, expected iterator to return 0x10FFFF when range end limit is set outside the meaningful range");
            }

            iterator = UCharacter.getNameIterator();
            iterator.setRange(0xF, 0x45);
            while (iterator.next(element)) {
                if (element.integer <= old.integer) {
                    errln("FAIL next returned a less codepoint \\u" +
                        Integer.toHexString(element.integer) + " than \\u" +
                        Integer.toHexString(old.integer));
                    break;
                }
                if (!UCharacter.getName(element.integer).equals(element.value))
                {
                    errln("FAIL next codepoint \\u" +
                        Integer.toHexString(element.integer) +
                        " does not have the expected name " +
                        UCharacter.getName(element.integer) +
                        " instead have the name " + (String)element.value);
                    break;
                }
                old.integer = element.integer;
            }

            iterator.reset();
            iterator.next(element);
            if (element.integer != 0x20) {
                errln("FAIL reset in iterator");
            }

            iterator.setRange(0, 0x110000);
            old.integer = 0;
            while (iterator.next(element)) {
                if (element.integer != 0 && element.integer <= old.integer) {
                    errln("FAIL next returned a less codepoint \\u" +
                        Integer.toHexString(element.integer) + " than \\u" +
                        Integer.toHexString(old.integer));
                    break;
                }
                if (!UCharacter.getName(element.integer).equals(element.value))
                {
                    errln("FAIL next codepoint \\u" +
                            Integer.toHexString(element.integer) +
                            " does not have the expected name " +
                            UCharacter.getName(element.integer) +
                            " instead have the name " + (String)element.value);
                    break;
                }
                for (int i = old.integer + 1; i < element.integer; i ++) {
                    if (UCharacter.getName(i) != null) {
                        errln("FAIL between codepoints are not null \\u" +
                                Integer.toHexString(old.integer) + " and " +
                                Integer.toHexString(element.integer) + " has " +
                                Integer.toHexString(i) + " with a name " +
                                UCharacter.getName(i));
                        break;
                    }
                }
                old.integer = element.integer;
            }

            iterator = UCharacter.getExtendedNameIterator();
            old.integer = 0;
            while (iterator.next(element)) {
                if (element.integer != 0 && element.integer != old.integer) {
                    errln("FAIL next returned a codepoint \\u" +
                            Integer.toHexString(element.integer) +
                            " different from \\u" +
                            Integer.toHexString(old.integer));
                    break;
                }
                if (!UCharacter.getExtendedName(element.integer).equals(
                                                              element.value)) {
                    errln("FAIL next codepoint \\u" +
                        Integer.toHexString(element.integer) +
                        " name should be "
                        + UCharacter.getExtendedName(element.integer) +
                        " instead of " + (String)element.value);
                    break;
                }
                old.integer++;
            }
            iterator = UCharacter.getName1_0Iterator();
            old.integer = 0;
            while (iterator.next(element)) {
                logln(Integer.toHexString(element.integer) + " " +
                                                        (String)element.value);
                if (element.integer != 0 && element.integer <= old.integer) {
                    errln("FAIL next returned a less codepoint \\u" +
                        Integer.toHexString(element.integer) + " than \\u" +
                        Integer.toHexString(old.integer));
                    break;
                }
                if (!element.value.equals(UCharacter.getName1_0(
                                                            element.integer))) {
                    errln("FAIL next codepoint \\u" +
                            Integer.toHexString(element.integer) +
                            " name cannot be null");
                    break;
                }
                for (int i = old.integer + 1; i < element.integer; i ++) {
                    if (UCharacter.getName1_0(i) != null) {
                        errln("FAIL between codepoints are not null \\u" +
                            Integer.toHexString(old.integer) + " and " +
                            Integer.toHexString(element.integer) + " has " +
                            Integer.toHexString(i) + " with a name " +
                            UCharacter.getName1_0(i));
                        break;
                    }
                }
                old.integer = element.integer;
            }
        } catch(Exception e){
            // !!! wouldn't preflighting be simpler?  This looks like
            // it is effectively be doing that.  It seems that for every
            // true error the code will call errln, which will throw the error, which
            // this will catch, which this will then rethrow the error.  Just seems
            // cumbersome.
            if(e.getMessage().indexOf("unames.icu") >= 0){
                warnln("Could not find unames.icu");
            } else {
                errln(e.getMessage());
            }
        }
    }

    /**
    * Testing the for illegal characters
    */
    @Test
    public void TestIsLegal()
    {
        int illegal[] = {0xFFFE, 0x00FFFF, 0x005FFFE, 0x005FFFF, 0x0010FFFE,
                         0x0010FFFF, 0x110000, 0x00FDD0, 0x00FDDF, 0x00FDE0,
                         0x00FDEF, 0xD800, 0xDC00, -1};
        int legal[] = {0x61, 0x00FFFD, 0x0010000, 0x005FFFD, 0x0060000,
                       0x0010FFFD, 0xFDCF, 0x00FDF0};
        for (int count = 0; count < illegal.length; count ++) {
            if (UCharacter.isLegal(illegal[count])) {
                errln("FAIL \\u" + hex(illegal[count]) +
                        " is not a legal character");
            }
        }

        for (int count = 0; count < legal.length; count ++) {
            if (!UCharacter.isLegal(legal[count])) {
                errln("FAIL \\u" + hex(legal[count]) +
                                                   " is a legal character");
            }
        }

        String illegalStr = "This is an illegal string ";
        String legalStr = "This is a legal string ";

        for (int count = 0; count < illegal.length; count ++) {
            StringBuffer str = new StringBuffer(illegalStr);
            if (illegal[count] < 0x10000) {
                str.append((char)illegal[count]);
            }
            else {
                char lead = UTF16.getLeadSurrogate(illegal[count]);
                char trail = UTF16.getTrailSurrogate(illegal[count]);
                str.append(lead);
                str.append(trail);
            }
            if (UCharacter.isLegal(str.toString())) {
                errln("FAIL " + hex(str.toString()) +
                      " is not a legal string");
            }
        }

        for (int count = 0; count < legal.length; count ++) {
            StringBuffer str = new StringBuffer(legalStr);
            if (legal[count] < 0x10000) {
                str.append((char)legal[count]);
            }
            else {
                char lead = UTF16.getLeadSurrogate(legal[count]);
                char trail = UTF16.getTrailSurrogate(legal[count]);
                str.append(lead);
                str.append(trail);
            }
            if (!UCharacter.isLegal(str.toString())) {
                errln("FAIL " + hex(str.toString()) + " is a legal string");
            }
        }
    }

    /**
     * Test getCodePoint
     */
    @Test
    public void TestCodePoint()
    {
        int ch = 0x10000;
        for (char i = 0xD800; i < 0xDC00; i ++) {
            for (char j = 0xDC00; j <= 0xDFFF; j ++) {
                if (UCharacter.getCodePoint(i, j) != ch) {
                    errln("Error getting codepoint for surrogate " +
                          "characters \\u"
                          + Integer.toHexString(i) + " \\u" +
                          Integer.toHexString(j));
                }
                ch ++;
            }
        }
        try
        {
            UCharacter.getCodePoint((char)0xD7ff, (char)0xDC00);
            errln("Invalid surrogate characters should not form a " +
                  "supplementary");
        } catch(Exception e) {
        }
        for (char i = 0; i < 0xFFFF; i++) {
            if (i == 0xFFFE ||
                (i >= 0xD800 && i <= 0xDFFF) ||
                (i >= 0xFDD0 && i <= 0xFDEF)) {
                // not a character
                try {
                    UCharacter.getCodePoint(i);
                    errln("Not a character is not a valid codepoint");
                } catch (Exception e) {
                }
            }
            else {
                if (UCharacter.getCodePoint(i) != i) {
                    errln("A valid codepoint should return itself");
                }
            }
        }
    }

    /**
    * This method is a little different from the type test in icu4c.
    * But combined with testUnicodeData, they basically do the same thing.
    */
    @Test
    public void TestIteration()
    {
        int limit     = 0;
        int prevtype  = -1;
        int shouldBeDir;
        int test[][]={{0x41, UCharacterCategory.UPPERCASE_LETTER},
                        {0x308, UCharacterCategory.NON_SPACING_MARK},
                        {0xfffe, UCharacterCategory.GENERAL_OTHER_TYPES},
                        {0xe0041, UCharacterCategory.FORMAT},
                        {0xeffff, UCharacterCategory.UNASSIGNED}};

        // default Bidi classes for unassigned code points, from the DerivedBidiClass.txt header
        int defaultBidi[][]={
            { 0x0590, UCharacterDirection.LEFT_TO_RIGHT },
            { 0x0600, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x07C0, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x08A0, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x0900, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },  /* Unicode 6.1 changes U+08A0..U+08FF from R to AL */
            { 0x20A0, UCharacterDirection.LEFT_TO_RIGHT },
            { 0x20D0, UCharacterDirection.EUROPEAN_NUMBER_TERMINATOR },  /* Unicode 6.3 changes the currency symbols block U+20A0..U+20CF to default to ET not L */
            { 0xFB1D, UCharacterDirection.LEFT_TO_RIGHT },
            { 0xFB50, UCharacterDirection.RIGHT_TO_LEFT },
            { 0xFE00, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0xFE70, UCharacterDirection.LEFT_TO_RIGHT },
            { 0xFF00, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x10800, UCharacterDirection.LEFT_TO_RIGHT },
            { 0x11000, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x1E800, UCharacterDirection.LEFT_TO_RIGHT },  /* new default-R range in Unicode 5.2: U+1E800 - U+1EFFF */
            { 0x1EE00, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x1EF00, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },  /* Unicode 6.1 changes U+1EE00..U+1EEFF from R to AL */
            { 0x1F000, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x110000, UCharacterDirection.LEFT_TO_RIGHT }
        };

        RangeValueIterator iterator = UCharacter.getTypeIterator();
        RangeValueIterator.Element result = new RangeValueIterator.Element();
        while (iterator.next(result)) {
            if (result.start != limit) {
                errln("UCharacterIteration failed: Ranges not continuous " +
                        "0x" + Integer.toHexString(result.start));
            }

            limit = result.limit;
            if (result.value == prevtype) {
                errln("Type of the next set of enumeration should be different");
            }
            prevtype = result.value;

            for (int i = result.start; i < limit; i ++) {
                int temptype = UCharacter.getType(i);
                if (temptype != result.value) {
                    errln("UCharacterIteration failed: Codepoint \\u" +
                            Integer.toHexString(i) + " should be of type " +
                            temptype + " not " + result.value);
                }
            }

            for (int i = 0; i < test.length; ++ i) {
                if (result.start <= test[i][0] && test[i][0] < result.limit) {
                    if (result.value != test[i][1]) {
                        errln("error: getTypes() has range ["
                              + Integer.toHexString(result.start) + ", "
                              + Integer.toHexString(result.limit)
                              + "] with type " + result.value
                              + " instead of ["
                              + Integer.toHexString(test[i][0]) + ", "
                              + Integer.toHexString(test[i][1]));
                    }
                }
            }

            // LineBreak.txt specifies:
            //   #  - Assigned characters that are not listed explicitly are given the value
            //   #    "AL".
            //   #  - Unassigned characters are given the value "XX".
            //
            // PUA characters are listed explicitly with "XX".
            // Verify that no assigned character has "XX".
            if (result.value != UCharacterCategory.UNASSIGNED
                && result.value != UCharacterCategory.PRIVATE_USE) {
                int c = result.start;
                while (c < result.limit) {
                    if (0 == UCharacter.getIntPropertyValue(c,
                                                UProperty.LINE_BREAK)) {
                        logln("error UProperty.LINE_BREAK(assigned \\u"
                              + Utility.hex(c, 4) + ")=XX");
                    }
                    ++ c;
                }
            }

            /*
             * Verify default Bidi classes.
             * For recent Unicode versions, see UCD.html.
             *
             * For older Unicode versions:
             * See table 3-7 "Bidirectional Character Types" in UAX #9.
             * http://www.unicode.org/reports/tr9/
             *
             * See also DerivedBidiClass.txt for Cn code points!
             *
             * Unicode 4.0.1/Public Review Issue #28 (http://www.unicode.org/review/resolved-pri.html)
             * changed some default values.
             * In particular, non-characters and unassigned Default Ignorable Code Points
             * change from L to BN.
             *
             * UCD.html version 4.0.1 does not yet reflect these changes.
             */
            if (result.value == UCharacterCategory.UNASSIGNED
                || result.value == UCharacterCategory.PRIVATE_USE) {
                int c = result.start;
                for (int i = 0; i < defaultBidi.length && c < result.limit;
                     ++ i) {
                    if (c < defaultBidi[i][0]) {
                        while (c < result.limit && c < defaultBidi[i][0]) {
                            // TODO change to public UCharacter.isNonCharacter(c) once it's available
                            if(android.icu.impl.UCharacterUtility.isNonCharacter(c) || UCharacter.hasBinaryProperty(c, UProperty.DEFAULT_IGNORABLE_CODE_POINT)) {
                                shouldBeDir=UCharacter.BOUNDARY_NEUTRAL;
                            } else {
                                shouldBeDir=defaultBidi[i][1];
                            }

                            if (UCharacter.getDirection(c) != shouldBeDir
                                || UCharacter.getIntPropertyValue(c,
                                                          UProperty.BIDI_CLASS)
                                   != shouldBeDir) {
                                errln("error: getDirection(unassigned/PUA "
                                      + Integer.toHexString(c)
                                      + ") should be "
                                      + shouldBeDir);
                            }
                            ++ c;
                        }
                    }
                }
            }
        }

        iterator.reset();
        if (iterator.next(result) == false || result.start != 0) {
            System.out.println("result " + result.start);
            errln("UCharacterIteration reset() failed");
        }
    }

    /**
     * Testing getAge
     */
    @Test
    public void TestGetAge()
    {
        int ages[] = {0x41,    1, 1, 0, 0,
                      0xffff,  1, 1, 0, 0,
                      0x20ab,  2, 0, 0, 0,
                      0x2fffe, 2, 0, 0, 0,
                      0x20ac,  2, 1, 0, 0,
                      0xfb1d,  3, 0, 0, 0,
                      0x3f4,   3, 1, 0, 0,
                      0x10300, 3, 1, 0, 0,
                      0x220,   3, 2, 0, 0,
                      0xff60,  3, 2, 0, 0};
        for (int i = 0; i < ages.length; i += 5) {
            VersionInfo age = UCharacter.getAge(ages[i]);
            if (age != VersionInfo.getInstance(ages[i + 1], ages[i + 2],
                                               ages[i + 3], ages[i + 4])) {
                errln("error: getAge(\\u" + Integer.toHexString(ages[i]) +
                      ") == " + age.toString() + " instead of " +
                      ages[i + 1] + "." + ages[i + 2] + "." + ages[i + 3] +
                      "." + ages[i + 4]);
            }
        }

        int[] valid_tests = {
                UCharacter.MIN_VALUE, UCharacter.MIN_VALUE+1,
                UCharacter.MAX_VALUE-1, UCharacter.MAX_VALUE};
        int[] invalid_tests = {
                UCharacter.MIN_VALUE-1, UCharacter.MIN_VALUE-2,
                UCharacter.MAX_VALUE+1, UCharacter.MAX_VALUE+2};

        for(int i=0; i< valid_tests.length; i++){
            try{
                UCharacter.getAge(valid_tests[i]);
            } catch(Exception e){
                errln("UCharacter.getAge(int) was not suppose to have " +
                        "an exception. Value passed: " + valid_tests[i]);
            }
        }

        for(int i=0; i< invalid_tests.length; i++){
            try{
                UCharacter.getAge(invalid_tests[i]);
                errln("UCharacter.getAge(int) was suppose to have " +
                        "an exception. Value passed: " + invalid_tests[i]);
            } catch(Exception e){
            }
        }
    }

    /**
     * Test binary non core properties
     */
    @Test
    public void TestAdditionalProperties()
    {
        // test data for hasBinaryProperty()
        int props[][] = { // code point, property
            { 0x0627, UProperty.ALPHABETIC, 1 },
            { 0x1034a, UProperty.ALPHABETIC, 1 },
            { 0x2028, UProperty.ALPHABETIC, 0 },

            { 0x0066, UProperty.ASCII_HEX_DIGIT, 1 },
            { 0x0067, UProperty.ASCII_HEX_DIGIT, 0 },

            { 0x202c, UProperty.BIDI_CONTROL, 1 },
            { 0x202f, UProperty.BIDI_CONTROL, 0 },

            { 0x003c, UProperty.BIDI_MIRRORED, 1 },
            { 0x003d, UProperty.BIDI_MIRRORED, 0 },

            /* see Unicode Corrigendum #6 at http://www.unicode.org/versions/corrigendum6.html */
            { 0x2018, UProperty.BIDI_MIRRORED, 0 },
            { 0x201d, UProperty.BIDI_MIRRORED, 0 },
            { 0x201f, UProperty.BIDI_MIRRORED, 0 },
            { 0x301e, UProperty.BIDI_MIRRORED, 0 },

            { 0x058a, UProperty.DASH, 1 },
            { 0x007e, UProperty.DASH, 0 },

            { 0x0c4d, UProperty.DIACRITIC, 1 },
            { 0x3000, UProperty.DIACRITIC, 0 },

            { 0x0e46, UProperty.EXTENDER, 1 },
            { 0x0020, UProperty.EXTENDER, 0 },

            { 0xfb1d, UProperty.FULL_COMPOSITION_EXCLUSION, 1 },
            { 0x1d15f, UProperty.FULL_COMPOSITION_EXCLUSION, 1 },
            { 0xfb1e, UProperty.FULL_COMPOSITION_EXCLUSION, 0 },

            { 0x110a, UProperty.NFD_INERT, 1 },      /* Jamo L */
            { 0x0308, UProperty.NFD_INERT, 0 },

            { 0x1164, UProperty.NFKD_INERT, 1 },     /* Jamo V */
            { 0x1d79d, UProperty.NFKD_INERT, 0 },   /* math compat version of xi */

            { 0x0021, UProperty.NFC_INERT, 1 },      /* ! */
            { 0x0061, UProperty.NFC_INERT, 0 },     /* a */
            { 0x00e4, UProperty.NFC_INERT, 0 },     /* a-umlaut */
            { 0x0102, UProperty.NFC_INERT, 0 },     /* a-breve */
            { 0xac1c, UProperty.NFC_INERT, 0 },     /* Hangul LV */
            { 0xac1d, UProperty.NFC_INERT, 1 },      /* Hangul LVT */

            { 0x1d79d, UProperty.NFKC_INERT, 0 },   /* math compat version of xi */
            { 0x2a6d6, UProperty.NFKC_INERT, 1 },    /* Han, last of CJK ext. B */

            { 0x00e4, UProperty.SEGMENT_STARTER, 1 },
            { 0x0308, UProperty.SEGMENT_STARTER, 0 },
            { 0x110a, UProperty.SEGMENT_STARTER, 1 }, /* Jamo L */
            { 0x1164, UProperty.SEGMENT_STARTER, 0 },/* Jamo V */
            { 0xac1c, UProperty.SEGMENT_STARTER, 1 }, /* Hangul LV */
            { 0xac1d, UProperty.SEGMENT_STARTER, 1 }, /* Hangul LVT */

            { 0x0044, UProperty.HEX_DIGIT, 1 },
            { 0xff46, UProperty.HEX_DIGIT, 1 },
            { 0x0047, UProperty.HEX_DIGIT, 0 },

            { 0x30fb, UProperty.HYPHEN, 1 },
            { 0xfe58, UProperty.HYPHEN, 0 },

            { 0x2172, UProperty.ID_CONTINUE, 1 },
            { 0x0307, UProperty.ID_CONTINUE, 1 },
            { 0x005c, UProperty.ID_CONTINUE, 0 },

            { 0x2172, UProperty.ID_START, 1 },
            { 0x007a, UProperty.ID_START, 1 },
            { 0x0039, UProperty.ID_START, 0 },

            { 0x4db5, UProperty.IDEOGRAPHIC, 1 },
            { 0x2f999, UProperty.IDEOGRAPHIC, 1 },
            { 0x2f99, UProperty.IDEOGRAPHIC, 0 },

            { 0x200c, UProperty.JOIN_CONTROL, 1 },
            { 0x2029, UProperty.JOIN_CONTROL, 0 },

            { 0x1d7bc, UProperty.LOWERCASE, 1 },
            { 0x0345, UProperty.LOWERCASE, 1 },
            { 0x0030, UProperty.LOWERCASE, 0 },

            { 0x1d7a9, UProperty.MATH, 1 },
            { 0x2135, UProperty.MATH, 1 },
            { 0x0062, UProperty.MATH, 0 },

            { 0xfde1, UProperty.NONCHARACTER_CODE_POINT, 1 },
            { 0x10ffff, UProperty.NONCHARACTER_CODE_POINT, 1 },
            { 0x10fffd, UProperty.NONCHARACTER_CODE_POINT, 0 },

            { 0x0022, UProperty.QUOTATION_MARK, 1 },
            { 0xff62, UProperty.QUOTATION_MARK, 1 },
            { 0xd840, UProperty.QUOTATION_MARK, 0 },

            { 0x061f, UProperty.TERMINAL_PUNCTUATION, 1 },
            { 0xe003f, UProperty.TERMINAL_PUNCTUATION, 0 },

            { 0x1d44a, UProperty.UPPERCASE, 1 },
            { 0x2162, UProperty.UPPERCASE, 1 },
            { 0x0345, UProperty.UPPERCASE, 0 },

            { 0x0020, UProperty.WHITE_SPACE, 1 },
            { 0x202f, UProperty.WHITE_SPACE, 1 },
            { 0x3001, UProperty.WHITE_SPACE, 0 },

            { 0x0711, UProperty.XID_CONTINUE, 1 },
            { 0x1d1aa, UProperty.XID_CONTINUE, 1 },
            { 0x007c, UProperty.XID_CONTINUE, 0 },

            { 0x16ee, UProperty.XID_START, 1 },
            { 0x23456, UProperty.XID_START, 1 },
            { 0x1d1aa, UProperty.XID_START, 0 },

            /*
             * Version break:
             * The following properties are only supported starting with the
             * Unicode version indicated in the second field.
             */
            { -1, 0x320, 0 },

            { 0x180c, UProperty.DEFAULT_IGNORABLE_CODE_POINT, 1 },
            { 0xfe02, UProperty.DEFAULT_IGNORABLE_CODE_POINT, 1 },
            { 0x1801, UProperty.DEFAULT_IGNORABLE_CODE_POINT, 0 },

            { 0x0149, UProperty.DEPRECATED, 1 },         /* changed in Unicode 5.2 */
            { 0x0341, UProperty.DEPRECATED, 0 },        /* changed in Unicode 5.2 */
            { 0xe0001, UProperty.DEPRECATED, 1 },       /* Changed from Unicode 5 to 5.1 */
            { 0xe0100, UProperty.DEPRECATED, 0 },

            { 0x00a0, UProperty.GRAPHEME_BASE, 1 },
            { 0x0a4d, UProperty.GRAPHEME_BASE, 0 },
            { 0xff9d, UProperty.GRAPHEME_BASE, 1 },
            { 0xff9f, UProperty.GRAPHEME_BASE, 0 },      /* changed from Unicode 3.2 to 4  and again 5 to 5.1 */

            { 0x0300, UProperty.GRAPHEME_EXTEND, 1 },
            { 0xff9d, UProperty.GRAPHEME_EXTEND, 0 },
            { 0xff9f, UProperty.GRAPHEME_EXTEND, 1 },   /* changed from Unicode 3.2 to 4 and again 5 to 5.1 */
            { 0x0603, UProperty.GRAPHEME_EXTEND, 0 },

            { 0x0a4d, UProperty.GRAPHEME_LINK, 1 },
            { 0xff9f, UProperty.GRAPHEME_LINK, 0 },

            { 0x2ff7, UProperty.IDS_BINARY_OPERATOR, 1 },
            { 0x2ff3, UProperty.IDS_BINARY_OPERATOR, 0 },

            { 0x2ff3, UProperty.IDS_TRINARY_OPERATOR, 1 },
            { 0x2f03, UProperty.IDS_TRINARY_OPERATOR, 0 },

            { 0x0ec1, UProperty.LOGICAL_ORDER_EXCEPTION, 1 },
            { 0xdcba, UProperty.LOGICAL_ORDER_EXCEPTION, 0 },

            { 0x2e9b, UProperty.RADICAL, 1 },
            { 0x4e00, UProperty.RADICAL, 0 },

            { 0x012f, UProperty.SOFT_DOTTED, 1 },
            { 0x0049, UProperty.SOFT_DOTTED, 0 },

            { 0xfa11, UProperty.UNIFIED_IDEOGRAPH, 1 },
            { 0xfa12, UProperty.UNIFIED_IDEOGRAPH, 0 },

            { -1, 0x401, 0 }, /* version break for Unicode 4.0.1 */

            { 0x002e, UProperty.S_TERM, 1 },
            { 0x0061, UProperty.S_TERM, 0 },

            { 0x180c, UProperty.VARIATION_SELECTOR, 1 },
            { 0xfe03, UProperty.VARIATION_SELECTOR, 1 },
            { 0xe01ef, UProperty.VARIATION_SELECTOR, 1 },
            { 0xe0200, UProperty.VARIATION_SELECTOR, 0 },

            /* enum/integer type properties */
            /* test default Bidi classes for unassigned code points */
            { 0x0590, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x05cf, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x05ed, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x07f2, UProperty.BIDI_CLASS, UCharacterDirection.DIR_NON_SPACING_MARK }, /* Nko, new in Unicode 5.0 */
            { 0x07fe, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT }, /* unassigned R */
            { 0x089f, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0xfb37, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0xfb42, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x10806, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x10909, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x10fe4, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },

            { 0x061d, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x063f, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x070e, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x0775, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0xfbc2, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0xfd90, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0xfefe, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },

            { 0x02AF, UProperty.BLOCK, UCharacter.UnicodeBlock.IPA_EXTENSIONS.getID() },
            { 0x0C4E, UProperty.BLOCK, UCharacter.UnicodeBlock.TELUGU.getID()},
            { 0x155A, UProperty.BLOCK, UCharacter.UnicodeBlock.UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS.getID() },
            { 0x1717, UProperty.BLOCK, UCharacter.UnicodeBlock.TAGALOG.getID() },
            { 0x1900, UProperty.BLOCK, UCharacter.UnicodeBlock.LIMBU.getID() },
            { 0x1CBF, UProperty.BLOCK, UCharacter.UnicodeBlock.NO_BLOCK.getID()},
            { 0x3040, UProperty.BLOCK, UCharacter.UnicodeBlock.HIRAGANA.getID()},
            { 0x1D0FF, UProperty.BLOCK, UCharacter.UnicodeBlock.BYZANTINE_MUSICAL_SYMBOLS.getID()},
            { 0x50000, UProperty.BLOCK, UCharacter.UnicodeBlock.NO_BLOCK.getID() },
            { 0xEFFFF, UProperty.BLOCK, UCharacter.UnicodeBlock.NO_BLOCK.getID() },
            { 0x10D0FF, UProperty.BLOCK, UCharacter.UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_B.getID() },

            /* UProperty.CANONICAL_COMBINING_CLASS tested for assigned characters in TestUnicodeData() */
            { 0xd7d7, UProperty.CANONICAL_COMBINING_CLASS, 0 },

            { 0x00A0, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.NOBREAK },
            { 0x00A8, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.COMPAT },
            { 0x00bf, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.NONE },
            { 0x00c0, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.CANONICAL },
            { 0x1E9B, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.CANONICAL },
            { 0xBCDE, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.CANONICAL },
            { 0xFB5D, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.MEDIAL },
            { 0x1D736, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.FONT },
            { 0xe0033, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.NONE },

            { 0x0009, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.NEUTRAL },
            { 0x0020, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.NARROW },
            { 0x00B1, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.AMBIGUOUS },
            { 0x20A9, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.HALFWIDTH },
            { 0x2FFB, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
            { 0x3000, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.FULLWIDTH },
            { 0x35bb, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
            { 0x58bd, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
            { 0xD7A3, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
            { 0xEEEE, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.AMBIGUOUS },
            { 0x1D198, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.NEUTRAL },
            { 0x20000, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
            { 0x2F8C7, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
            { 0x3a5bd, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
            { 0x5a5bd, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.NEUTRAL },
            { 0xFEEEE, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.AMBIGUOUS },
            { 0x10EEEE, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.AMBIGUOUS },

            /* UProperty.GENERAL_CATEGORY tested for assigned characters in TestUnicodeData() */
            { 0xd7c7, UProperty.GENERAL_CATEGORY, 0 },
            { 0xd7d7, UProperty.GENERAL_CATEGORY, UCharacterEnums.ECharacterCategory.OTHER_LETTER },     /* changed in Unicode 5.2 */

            { 0x0444, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.NO_JOINING_GROUP },
            { 0x0639, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.AIN },
            { 0x072A, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.DALATH_RISH },
            { 0x0647, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.HEH },
            { 0x06C1, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.HEH_GOAL },

            { 0x200C, UProperty.JOINING_TYPE, UCharacter.JoiningType.NON_JOINING },
            { 0x200D, UProperty.JOINING_TYPE, UCharacter.JoiningType.JOIN_CAUSING },
            { 0x0639, UProperty.JOINING_TYPE, UCharacter.JoiningType.DUAL_JOINING },
            { 0x0640, UProperty.JOINING_TYPE, UCharacter.JoiningType.JOIN_CAUSING },
            { 0x06C3, UProperty.JOINING_TYPE, UCharacter.JoiningType.RIGHT_JOINING },
            { 0x0300, UProperty.JOINING_TYPE, UCharacter.JoiningType.TRANSPARENT },
            { 0x070F, UProperty.JOINING_TYPE, UCharacter.JoiningType.TRANSPARENT },
            { 0xe0033, UProperty.JOINING_TYPE, UCharacter.JoiningType.TRANSPARENT },

            /* TestUnicodeData() verifies that no assigned character has "XX" (unknown) */
            { 0xe7e7, UProperty.LINE_BREAK, UCharacter.LineBreak.UNKNOWN },
            { 0x10fffd, UProperty.LINE_BREAK, UCharacter.LineBreak.UNKNOWN },
            { 0x0028, UProperty.LINE_BREAK, UCharacter.LineBreak.OPEN_PUNCTUATION },
            { 0x232A, UProperty.LINE_BREAK, UCharacter.LineBreak.CLOSE_PUNCTUATION },
            { 0x3401, UProperty.LINE_BREAK, UCharacter.LineBreak.IDEOGRAPHIC },
            { 0x4e02, UProperty.LINE_BREAK, UCharacter.LineBreak.IDEOGRAPHIC },
            { 0x20004, UProperty.LINE_BREAK, UCharacter.LineBreak.IDEOGRAPHIC },
            { 0xf905, UProperty.LINE_BREAK, UCharacter.LineBreak.IDEOGRAPHIC },
            { 0xdb7e, UProperty.LINE_BREAK, UCharacter.LineBreak.SURROGATE },
            { 0xdbfd, UProperty.LINE_BREAK, UCharacter.LineBreak.SURROGATE },
            { 0xdffc, UProperty.LINE_BREAK, UCharacter.LineBreak.SURROGATE },
            { 0x2762, UProperty.LINE_BREAK, UCharacter.LineBreak.EXCLAMATION },
            { 0x002F, UProperty.LINE_BREAK, UCharacter.LineBreak.BREAK_SYMBOLS },
            { 0x1D49C, UProperty.LINE_BREAK, UCharacter.LineBreak.ALPHABETIC },
            { 0x1731, UProperty.LINE_BREAK, UCharacter.LineBreak.ALPHABETIC },

            /* UProperty.NUMERIC_TYPE tested in TestNumericProperties() */

            /* UProperty.SCRIPT tested in TestUScriptCodeAPI() */

            { 0x10ff, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
            { 0x1100, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },
            { 0x1111, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },
            { 0x1159, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },
            { 0x115a, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },     /* changed in Unicode 5.2 */
            { 0x115e, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },     /* changed in Unicode 5.2 */
            { 0x115f, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },

            { 0xa95f, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
            { 0xa960, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },     /* changed in Unicode 5.2 */
            { 0xa97c, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },     /* changed in Unicode 5.2 */
            { 0xa97d, UProperty.HANGUL_SYLLABLE_TYPE, 0 },

            { 0x1160, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },
            { 0x1161, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },
            { 0x1172, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },
            { 0x11a2, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },
            { 0x11a3, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },       /* changed in Unicode 5.2 */
            { 0x11a7, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },       /* changed in Unicode 5.2 */

            { 0xd7af, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
            { 0xd7b0, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },       /* changed in Unicode 5.2 */
            { 0xd7c6, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },       /* changed in Unicode 5.2 */
            { 0xd7c7, UProperty.HANGUL_SYLLABLE_TYPE, 0 },

            { 0x11a8, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },
            { 0x11b8, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },
            { 0x11c8, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },
            { 0x11f9, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },
            { 0x11fa, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },    /* changed in Unicode 5.2 */
            { 0x11ff, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },    /* changed in Unicode 5.2 */
            { 0x1200, UProperty.HANGUL_SYLLABLE_TYPE, 0 },

            { 0xd7ca, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
            { 0xd7cb, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },    /* changed in Unicode 5.2 */
            { 0xd7fb, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },    /* changed in Unicode 5.2 */
            { 0xd7fc, UProperty.HANGUL_SYLLABLE_TYPE, 0 },

            { 0xac00, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LV_SYLLABLE },
            { 0xac1c, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LV_SYLLABLE },
            { 0xc5ec, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LV_SYLLABLE },
            { 0xd788, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LV_SYLLABLE },

            { 0xac01, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LVT_SYLLABLE },
            { 0xac1b, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LVT_SYLLABLE },
            { 0xac1d, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LVT_SYLLABLE },
            { 0xc5ee, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LVT_SYLLABLE },
            { 0xd7a3, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LVT_SYLLABLE },

            { 0xd7a4, UProperty.HANGUL_SYLLABLE_TYPE, 0 },

            { -1, 0x410, 0 }, /* version break for Unicode 4.1 */

            { 0x00d7, UProperty.PATTERN_SYNTAX, 1 },
            { 0xfe45, UProperty.PATTERN_SYNTAX, 1 },
            { 0x0061, UProperty.PATTERN_SYNTAX, 0 },

            { 0x0020, UProperty.PATTERN_WHITE_SPACE, 1 },
            { 0x0085, UProperty.PATTERN_WHITE_SPACE, 1 },
            { 0x200f, UProperty.PATTERN_WHITE_SPACE, 1 },
            { 0x00a0, UProperty.PATTERN_WHITE_SPACE, 0 },
            { 0x3000, UProperty.PATTERN_WHITE_SPACE, 0 },

            { 0x1d200, UProperty.BLOCK, UCharacter.UnicodeBlock.ANCIENT_GREEK_MUSICAL_NOTATION_ID },
            { 0x2c8e,  UProperty.BLOCK, UCharacter.UnicodeBlock.COPTIC_ID },
            { 0xfe17,  UProperty.BLOCK, UCharacter.UnicodeBlock.VERTICAL_FORMS_ID },

            { 0x1a00,  UProperty.SCRIPT, UScript.BUGINESE },
            { 0x2cea,  UProperty.SCRIPT, UScript.COPTIC },
            { 0xa82b,  UProperty.SCRIPT, UScript.SYLOTI_NAGRI },
            { 0x103d0, UProperty.SCRIPT, UScript.OLD_PERSIAN },

            { 0xcc28, UProperty.LINE_BREAK, UCharacter.LineBreak.H2 },
            { 0xcc29, UProperty.LINE_BREAK, UCharacter.LineBreak.H3 },
            { 0xac03, UProperty.LINE_BREAK, UCharacter.LineBreak.H3 },
            { 0x115f, UProperty.LINE_BREAK, UCharacter.LineBreak.JL },
            { 0x11aa, UProperty.LINE_BREAK, UCharacter.LineBreak.JT },
            { 0x11a1, UProperty.LINE_BREAK, UCharacter.LineBreak.JV },

            { 0xb2c9, UProperty.GRAPHEME_CLUSTER_BREAK, UCharacter.GraphemeClusterBreak.LVT },
            { 0x036f, UProperty.GRAPHEME_CLUSTER_BREAK, UCharacter.GraphemeClusterBreak.EXTEND },
            { 0x0000, UProperty.GRAPHEME_CLUSTER_BREAK, UCharacter.GraphemeClusterBreak.CONTROL },
            { 0x1160, UProperty.GRAPHEME_CLUSTER_BREAK, UCharacter.GraphemeClusterBreak.V },

            { 0x05f4, UProperty.WORD_BREAK, UCharacter.WordBreak.MIDLETTER },
            { 0x4ef0, UProperty.WORD_BREAK, UCharacter.WordBreak.OTHER },
            { 0x19d9, UProperty.WORD_BREAK, UCharacter.WordBreak.NUMERIC },
            { 0x2044, UProperty.WORD_BREAK, UCharacter.WordBreak.MIDNUM },

            { 0xfffd, UProperty.SENTENCE_BREAK, UCharacter.SentenceBreak.OTHER },
            { 0x1ffc, UProperty.SENTENCE_BREAK, UCharacter.SentenceBreak.UPPER },
            { 0xff63, UProperty.SENTENCE_BREAK, UCharacter.SentenceBreak.CLOSE },
            { 0x2028, UProperty.SENTENCE_BREAK, UCharacter.SentenceBreak.SEP },

            { -1, 0x520, 0 }, /* version break for Unicode 5.2 */

            /* unassigned code points in new default Bidi R blocks */
            { 0x1ede4, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x1efe4, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },

            /* test some script codes >127 */
            { 0xa6e6,  UProperty.SCRIPT, UScript.BAMUM },
            { 0xa4d0,  UProperty.SCRIPT, UScript.LISU },
            { 0x10a7f,  UProperty.SCRIPT, UScript.OLD_SOUTH_ARABIAN },

            { -1, 0x600, 0 }, /* version break for Unicode 6.0 */

            /* value changed in Unicode 6.0 */
            { 0x06C3, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.TEH_MARBUTA_GOAL },

            { -1, 0x610, 0 }, /* version break for Unicode 6.1 */

            /* unassigned code points in new/changed default Bidi AL blocks */
            { 0x08ba, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x1eee4, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },

            { -1, 0x630, 0 }, /* version break for Unicode 6.3 */

            /* unassigned code points in the currency symbols block now default to ET */
            { 0x20C0, UProperty.BIDI_CLASS, UCharacterDirection.EUROPEAN_NUMBER_TERMINATOR },
            { 0x20CF, UProperty.BIDI_CLASS, UCharacterDirection.EUROPEAN_NUMBER_TERMINATOR },

            /* new property in Unicode 6.3 */
            { 0x0027, UProperty.BIDI_PAIRED_BRACKET_TYPE, UCharacter.BidiPairedBracketType.NONE },
            { 0x0028, UProperty.BIDI_PAIRED_BRACKET_TYPE, UCharacter.BidiPairedBracketType.OPEN },
            { 0x0029, UProperty.BIDI_PAIRED_BRACKET_TYPE, UCharacter.BidiPairedBracketType.CLOSE },
            { 0xFF5C, UProperty.BIDI_PAIRED_BRACKET_TYPE, UCharacter.BidiPairedBracketType.NONE },
            { 0xFF5B, UProperty.BIDI_PAIRED_BRACKET_TYPE, UCharacter.BidiPairedBracketType.OPEN },
            { 0xFF5D, UProperty.BIDI_PAIRED_BRACKET_TYPE, UCharacter.BidiPairedBracketType.CLOSE },

            { -1, 0x700, 0 }, /* version break for Unicode 7.0 */

            /* new character range with Joining_Group values */
            { 0x10ABF, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.NO_JOINING_GROUP },
            { 0x10AC0, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.MANICHAEAN_ALEPH },
            { 0x10AC1, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.MANICHAEAN_BETH },
            { 0x10AEF, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.MANICHAEAN_HUNDRED },
            { 0x10AF0, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.NO_JOINING_GROUP },

            /* undefined UProperty values */
            { 0x61, 0x4a7, 0 },
            { 0x234bc, 0x15ed, 0 }
        };


        if (UCharacter.getIntPropertyMinValue(UProperty.DASH) != 0
            || UCharacter.getIntPropertyMinValue(UProperty.BIDI_CLASS) != 0
            || UCharacter.getIntPropertyMinValue(UProperty.BLOCK)!= 0  /* j2478 */
            || UCharacter.getIntPropertyMinValue(UProperty.SCRIPT)!= 0 /* JB#2410 */
            || UCharacter.getIntPropertyMinValue(0x2345) != 0) {
            errln("error: UCharacter.getIntPropertyMinValue() wrong");
        }

        if( UCharacter.getIntPropertyMaxValue(UProperty.DASH)!=1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.DASH) wrong\n");
        }
        if( UCharacter.getIntPropertyMaxValue(UProperty.ID_CONTINUE)!=1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.ID_CONTINUE) wrong\n");
        }
        if( UCharacter.getIntPropertyMaxValue(UProperty.BINARY_LIMIT-1)!=1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.BINARY_LIMIT-1) wrong\n");
        }

        if( UCharacter.getIntPropertyMaxValue(UProperty.BIDI_CLASS)!=UCharacterDirection.CHAR_DIRECTION_COUNT-1 ) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.BIDI_CLASS) wrong\n");
        }
        if( UCharacter.getIntPropertyMaxValue(UProperty.BLOCK)!=UCharacter.UnicodeBlock.COUNT-1 ) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.BLOCK) wrong\n");
        }
        if(UCharacter.getIntPropertyMaxValue(UProperty.LINE_BREAK)!=UCharacter.LineBreak.COUNT-1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.LINE_BREAK) wrong\n");
        }
        if(UCharacter.getIntPropertyMaxValue(UProperty.SCRIPT)!=UScript.CODE_LIMIT-1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.SCRIPT) wrong\n");
        }
        if(UCharacter.getIntPropertyMaxValue(UProperty.NUMERIC_TYPE)!=UCharacter.NumericType.COUNT-1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.NUMERIC_TYPE) wrong\n");
        }
        if(UCharacter.getIntPropertyMaxValue(UProperty.GENERAL_CATEGORY)!=UCharacterCategory.CHAR_CATEGORY_COUNT-1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.GENERAL_CATEGORY) wrong\n");
        }
        if(UCharacter.getIntPropertyMaxValue(UProperty.HANGUL_SYLLABLE_TYPE)!=UCharacter.HangulSyllableType.COUNT-1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.HANGUL_SYLLABLE_TYPE) wrong\n");
        }
        if(UCharacter.getIntPropertyMaxValue(UProperty.GRAPHEME_CLUSTER_BREAK)!=UCharacter.GraphemeClusterBreak.COUNT-1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.GRAPHEME_CLUSTER_BREAK) wrong\n");
        }
        if(UCharacter.getIntPropertyMaxValue(UProperty.SENTENCE_BREAK)!=UCharacter.SentenceBreak.COUNT-1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.SENTENCE_BREAK) wrong\n");
        }
        if(UCharacter.getIntPropertyMaxValue(UProperty.WORD_BREAK)!=UCharacter.WordBreak.COUNT-1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.WORD_BREAK) wrong\n");
        }
        if(UCharacter.getIntPropertyMaxValue(UProperty.BIDI_PAIRED_BRACKET_TYPE)!=UCharacter.BidiPairedBracketType.COUNT-1) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.BIDI_PAIRED_BRACKET_TYPE) wrong\n");
        }
        /*JB#2410*/
        if( UCharacter.getIntPropertyMaxValue(0x2345)!=-1) {
            errln("error: UCharacter.getIntPropertyMaxValue(0x2345) wrong\n");
        }
        if( UCharacter.getIntPropertyMaxValue(UProperty.DECOMPOSITION_TYPE) !=  (UCharacter.DecompositionType.COUNT - 1)) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.DECOMPOSITION_TYPE) wrong\n");
        }
        if( UCharacter.getIntPropertyMaxValue(UProperty.JOINING_GROUP) !=   (UCharacter.JoiningGroup.COUNT -1)) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.JOINING_GROUP) wrong\n");
        }
        if( UCharacter.getIntPropertyMaxValue(UProperty.JOINING_TYPE) !=  (UCharacter.JoiningType.COUNT -1)) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.JOINING_TYPE) wrong\n");
        }
        if( UCharacter.getIntPropertyMaxValue(UProperty.EAST_ASIAN_WIDTH) !=  (UCharacter.EastAsianWidth.COUNT -1)) {
            errln("error: UCharacter.getIntPropertyMaxValue(UProperty.EAST_ASIAN_WIDTH) wrong\n");
        }

        VersionInfo version = UCharacter.getUnicodeVersion();

        // test hasBinaryProperty()
        for (int i = 0; i < props.length; ++ i) {
            int which = props[i][1];
            if (props[i][0] < 0) {
                if (version.compareTo(VersionInfo.getInstance(which >> 8,
                                                          (which >> 4) & 0xF,
                                                          which & 0xF,
                                                          0)) < 0) {
                    break;
                }
                continue;
            }
            String whichName;
            try {
                whichName = UCharacter.getPropertyName(which, UProperty.NameChoice.LONG);
            } catch(IllegalArgumentException e) {
                // There are intentionally invalid property integer values ("which").
                // Catch and ignore the exception from getPropertyName().
                whichName = "undefined UProperty value";
            }
            boolean expect = true;
            if (props[i][2] == 0) {
                expect = false;
            }
            if (which < UProperty.INT_START) {
                if (UCharacter.hasBinaryProperty(props[i][0], which)
                    != expect) {
                    errln("error: UCharacter.hasBinaryProperty(U+" +
                            Utility.hex(props[i][0], 4) + ", " +
                          whichName + ") has an error, expected=" + expect);
                }
            }

            int retVal = UCharacter.getIntPropertyValue(props[i][0], which);
            if (retVal != props[i][2]) {
                errln("error: UCharacter.getIntPropertyValue(U+" +
                      Utility.hex(props[i][0], 4) +
                      ", " + whichName + ") is wrong, expected="
                      + props[i][2] + " actual=" + retVal);
            }

            // test separate functions, too
            switch (which) {
            case UProperty.ALPHABETIC:
                if (UCharacter.isUAlphabetic(props[i][0]) != expect) {
                    errln("error: UCharacter.isUAlphabetic(\\u" +
                          Integer.toHexString(props[i][0]) +
                          ") is wrong expected " + props[i][2]);
                }
                break;
            case UProperty.LOWERCASE:
                if (UCharacter.isULowercase(props[i][0]) != expect) {
                    errln("error: UCharacter.isULowercase(\\u" +
                          Integer.toHexString(props[i][0]) +
                          ") is wrong expected " +props[i][2]);
                }
                break;
            case UProperty.UPPERCASE:
                if (UCharacter.isUUppercase(props[i][0]) != expect) {
                    errln("error: UCharacter.isUUppercase(\\u" +
                          Integer.toHexString(props[i][0]) +
                          ") is wrong expected " + props[i][2]);
                }
                break;
            case UProperty.WHITE_SPACE:
                if (UCharacter.isUWhiteSpace(props[i][0]) != expect) {
                    errln("error: UCharacter.isUWhiteSpace(\\u" +
                          Integer.toHexString(props[i][0]) +
                          ") is wrong expected " + props[i][2]);
                }
                break;
            default:
                break;
            }
        }
    }

    @Test
    public void TestNumericProperties()
    {
        // see UnicodeData.txt, DerivedNumericValues.txt
        double values[][] = {
            // Code point, numeric type, numeric value.
            // If a fourth value is specified, it is the getNumericValue().
            // Otherwise it is expected to be the same as the getUnicodeNumericValue(),
            // where UCharacter.NO_NUMERIC_VALUE is turned into -1.
            // getNumericValue() returns -2 if the code point has a value
            // which is not a non-negative integer. (This is mostly auto-converted to -2.)
            { 0x0F33, UCharacter.NumericType.NUMERIC, -1./2. },
            { 0x0C66, UCharacter.NumericType.DECIMAL, 0 },
            { 0x96f6, UCharacter.NumericType.NUMERIC, 0 },
            { 0xa833, UCharacter.NumericType.NUMERIC, 1./16. },
            { 0x2152, UCharacter.NumericType.NUMERIC, 1./10. },
            { 0x2151, UCharacter.NumericType.NUMERIC, 1./9. },
            { 0x1245f, UCharacter.NumericType.NUMERIC, 1./8. },
            { 0x2150, UCharacter.NumericType.NUMERIC, 1./7. },
            { 0x2159, UCharacter.NumericType.NUMERIC, 1./6. },
            { 0x09f6, UCharacter.NumericType.NUMERIC, 3./16. },
            { 0x2155, UCharacter.NumericType.NUMERIC, 1./5. },
            { 0x00BD, UCharacter.NumericType.NUMERIC, 1./2. },
            { 0x0031, UCharacter.NumericType.DECIMAL, 1. },
            { 0x4e00, UCharacter.NumericType.NUMERIC, 1. },
            { 0x58f1, UCharacter.NumericType.NUMERIC, 1. },
            { 0x10320, UCharacter.NumericType.NUMERIC, 1. },
            { 0x0F2B, UCharacter.NumericType.NUMERIC, 3./2. },
            { 0x00B2, UCharacter.NumericType.DIGIT, 2. }, /* Unicode 4.0 change */
            { 0x5f10, UCharacter.NumericType.NUMERIC, 2. },
            { 0x1813, UCharacter.NumericType.DECIMAL, 3. },
            { 0x5f0e, UCharacter.NumericType.NUMERIC, 3. },
            { 0x2173, UCharacter.NumericType.NUMERIC, 4. },
            { 0x8086, UCharacter.NumericType.NUMERIC, 4. },
            { 0x278E, UCharacter.NumericType.DIGIT, 5. },
            { 0x1D7F2, UCharacter.NumericType.DECIMAL, 6. },
            { 0x247A, UCharacter.NumericType.DIGIT, 7. },
            { 0x7396, UCharacter.NumericType.NUMERIC, 9. },
            { 0x1372, UCharacter.NumericType.NUMERIC, 10. },
            { 0x216B, UCharacter.NumericType.NUMERIC, 12. },
            { 0x16EE, UCharacter.NumericType.NUMERIC, 17. },
            { 0x249A, UCharacter.NumericType.NUMERIC, 19. },
            { 0x303A, UCharacter.NumericType.NUMERIC, 30. },
            { 0x5345, UCharacter.NumericType.NUMERIC, 30. },
            { 0x32B2, UCharacter.NumericType.NUMERIC, 37. },
            { 0x1375, UCharacter.NumericType.NUMERIC, 40. },
            { 0x10323, UCharacter.NumericType.NUMERIC, 50. },
            { 0x0BF1, UCharacter.NumericType.NUMERIC, 100. },
            { 0x964c, UCharacter.NumericType.NUMERIC, 100. },
            { 0x217E, UCharacter.NumericType.NUMERIC, 500. },
            { 0x2180, UCharacter.NumericType.NUMERIC, 1000. },
            { 0x4edf, UCharacter.NumericType.NUMERIC, 1000. },
            { 0x2181, UCharacter.NumericType.NUMERIC, 5000. },
            { 0x137C, UCharacter.NumericType.NUMERIC, 10000. },
            { 0x4e07, UCharacter.NumericType.NUMERIC, 10000. },
            { 0x12432, UCharacter.NumericType.NUMERIC, 216000. },
            { 0x12433, UCharacter.NumericType.NUMERIC, 432000. },
            { 0x4ebf, UCharacter.NumericType.NUMERIC, 100000000. },
            { 0x5146, UCharacter.NumericType.NUMERIC, 1000000000000. },
            { -1, UCharacter.NumericType.NONE, UCharacter.NO_NUMERIC_VALUE },
            { 0x61, UCharacter.NumericType.NONE, UCharacter.NO_NUMERIC_VALUE, 10. },
            { 0x3000, UCharacter.NumericType.NONE, UCharacter.NO_NUMERIC_VALUE },
            { 0xfffe, UCharacter.NumericType.NONE, UCharacter.NO_NUMERIC_VALUE },
            { 0x10301, UCharacter.NumericType.NONE, UCharacter.NO_NUMERIC_VALUE },
            { 0xe0033, UCharacter.NumericType.NONE, UCharacter.NO_NUMERIC_VALUE },
            { 0x10ffff, UCharacter.NumericType.NONE, UCharacter.NO_NUMERIC_VALUE },
            { 0x110000, UCharacter.NumericType.NONE, UCharacter.NO_NUMERIC_VALUE }
        };

        for (int i = 0; i < values.length; ++ i) {
            int c = (int)values[i][0];
            int type = UCharacter.getIntPropertyValue(c,
                                                      UProperty.NUMERIC_TYPE);
            double nv = UCharacter.getUnicodeNumericValue(c);

            if (type != values[i][1]) {
                errln("UProperty.NUMERIC_TYPE(\\u" + Utility.hex(c, 4)
                       + ") = " + type + " should be " + (int)values[i][1]);
            }
            if (0.000001 <= Math.abs(nv - values[i][2])) {
                errln("UCharacter.getUnicodeNumericValue(\\u" + Utility.hex(c, 4)
                        + ") = " + nv + " should be " + values[i][2]);
            }

            // Test getNumericValue() as well.
            // It can only return the subset of numeric values that are
            // non-negative and fit into an int.
            int expectedInt;
            if (values[i].length == 3) {
                if (values[i][2] == UCharacter.NO_NUMERIC_VALUE) {
                    expectedInt = -1;
                } else {
                    expectedInt = (int)values[i][2];
                    if (expectedInt < 0 || expectedInt != values[i][2]) {
                        // The numeric value is not a non-negative integer.
                        expectedInt = -2;
                    }
                }
            } else {
                expectedInt = (int)values[i][3];
            }
            int nvInt = UCharacter.getNumericValue(c);
            if (nvInt != expectedInt) {
                errln("UCharacter.getNumericValue(\\u" + Utility.hex(c, 4)
                        + ") = " + nvInt + " should be " + expectedInt);
            }
        }
    }

    /**
     * Test the property values API.  See JB#2410.
     */
    @Test
    public void TestPropertyValues() {
        int i, p, min, max;

        /* Min should be 0 for everything. */
        /* Until JB#2478 is fixed, the one exception is UProperty.BLOCK. */
        for (p=UProperty.INT_START; p<UProperty.INT_LIMIT; ++p) {
            min = UCharacter.getIntPropertyMinValue(p);
            if (min != 0) {
                if (p == UProperty.BLOCK) {
                    /* This is okay...for now.  See JB#2487.
                       TODO Update this for JB#2487. */
                } else {
                    String name;
                    name = UCharacter.getPropertyName(p, UProperty.NameChoice.LONG);
                    errln("FAIL: UCharacter.getIntPropertyMinValue(" + name + ") = " +
                          min + ", exp. 0");
                }
            }
        }

        if (UCharacter.getIntPropertyMinValue(UProperty.GENERAL_CATEGORY_MASK)
            != 0
            || UCharacter.getIntPropertyMaxValue(
                                               UProperty.GENERAL_CATEGORY_MASK)
               != -1) {
            errln("error: UCharacter.getIntPropertyMin/MaxValue("
                  + "UProperty.GENERAL_CATEGORY_MASK) is wrong");
        }

        /* Max should be -1 for invalid properties. */
        max = UCharacter.getIntPropertyMaxValue(-1);
        if (max != -1) {
            errln("FAIL: UCharacter.getIntPropertyMaxValue(-1) = " +
                  max + ", exp. -1");
        }

        /* Script should return 0 for an invalid code point. If the API
           throws an exception then that's fine too. */
        for (i=0; i<2; ++i) {
            try {
                int script = 0;
                String desc = null;
                switch (i) {
                case 0:
                    script = UScript.getScript(-1);
                    desc = "UScript.getScript(-1)";
                    break;
                case 1:
                    script = UCharacter.getIntPropertyValue(-1, UProperty.SCRIPT);
                    desc = "UCharacter.getIntPropertyValue(-1, UProperty.SCRIPT)";
                    break;
                }
                if (script != 0) {
                    errln("FAIL: " + desc + " = " + script + ", exp. 0");
                }
            } catch (IllegalArgumentException e) {}
        }
    }

    @Test
    public void TestBidiPairedBracketType() {
        // BidiBrackets-6.3.0.txt says:
        //
        // The set of code points listed in this file was originally derived
        // using the character properties General_Category (gc), Bidi_Class (bc),
        // Bidi_Mirrored (Bidi_M), and Bidi_Mirroring_Glyph (bmg), as follows:
        // two characters, A and B, form a pair if A has gc=Ps and B has gc=Pe,
        // both have bc=ON and Bidi_M=Y, and bmg of A is B. Bidi_Paired_Bracket
        // maps A to B and vice versa, and their Bidi_Paired_Bracket_Type
        // property values are Open and Close, respectively.
        UnicodeSet bpt = new UnicodeSet("[:^bpt=n:]");
        assertTrue("bpt!=None is not empty", !bpt.isEmpty());
        // The following should always be true.
        UnicodeSet mirrored = new UnicodeSet("[:Bidi_M:]");
        UnicodeSet other_neutral = new UnicodeSet("[:bc=ON:]");
        assertTrue("bpt!=None is a subset of Bidi_M", mirrored.containsAll(bpt));
        assertTrue("bpt!=None is a subset of bc=ON", other_neutral.containsAll(bpt));
        // The following are true at least initially in Unicode 6.3.
        UnicodeSet bpt_open = new UnicodeSet("[:bpt=o:]");
        UnicodeSet bpt_close = new UnicodeSet("[:bpt=c:]");
        UnicodeSet ps = new UnicodeSet("[:Ps:]");
        UnicodeSet pe = new UnicodeSet("[:Pe:]");
        assertTrue("bpt=Open is a subset of Ps", ps.containsAll(bpt_open));
        assertTrue("bpt=Close is a subset of Pe", pe.containsAll(bpt_close));
    }

    @Test
    public void TestEmojiProperties() {
        assertFalse("space is not Emoji", UCharacter.hasBinaryProperty(0x20, UProperty.EMOJI));
        assertTrue("shooting star is Emoji", UCharacter.hasBinaryProperty(0x1F320, UProperty.EMOJI));
        UnicodeSet emoji = new UnicodeSet("[:Emoji:]");
        assertTrue("lots of Emoji", emoji.size() > 700);

        assertTrue("shooting star is Emoji_Presentation",
                UCharacter.hasBinaryProperty(0x1F320, UProperty.EMOJI_PRESENTATION));
        assertTrue("Fitzpatrick 6 is Emoji_Modifier",
                UCharacter.hasBinaryProperty(0x1F3FF, UProperty.EMOJI_MODIFIER));
        assertTrue("happy person is Emoji_Modifier_Base",
                UCharacter.hasBinaryProperty(0x1F64B, UProperty.EMOJI_MODIFIER_BASE));
    }

    @Test
    public void TestIsBMP()
    {
        int ch[] = {0x0, -1, 0xffff, 0x10ffff, 0xff, 0x1ffff};
        boolean flag[] = {true, false, true, false, true, false};
        for (int i = 0; i < ch.length; i ++) {
            if (UCharacter.isBMP(ch[i]) != flag[i]) {
                errln("Fail: \\u" + Utility.hex(ch[i], 8)
                      + " failed at UCharacter.isBMP");
            }
        }
    }

    private boolean showADiffB(UnicodeSet a, UnicodeSet b,
                                        String a_name, String b_name,
                                        boolean expect,
                                        boolean diffIsError){
        int i, start, end;
        boolean equal=true;
        for(i=0; i < a.getRangeCount(); ++i) {
            start  = a.getRangeStart(i);
            end    = a.getRangeEnd(i);
            if(expect!=b.contains(start, end)) {
                equal=false;
                while(start<=end) {
                    if(expect!=b.contains(start)) {
                        if(diffIsError) {
                            if(expect) {
                                errln("error: "+ a_name +" contains "+ hex(start)+" but "+ b_name +" does not");
                            } else {
                                errln("error: "+a_name +" and "+ b_name+" both contain "+hex(start) +" but should not intersect");
                            }
                        } else {
                            if(expect) {
                                logln("info: "+a_name +" contains "+hex(start)+ "but " + b_name +" does not");
                            } else {
                                logln("info: "+a_name +" and "+b_name+" both contain "+hex(start)+" but should not intersect");
                            }
                        }
                    }
                    ++start;
                }
            }
        }
        return equal;
    }
    private boolean showAMinusB(UnicodeSet a, UnicodeSet b,
                                        String a_name, String b_name,
                                        boolean diffIsError) {

        return showADiffB(a, b, a_name, b_name, true, diffIsError);
    }

    private boolean showAIntersectB(UnicodeSet a, UnicodeSet b,
                                            String a_name, String b_name,
                                            boolean diffIsError) {
        return showADiffB(a, b, a_name, b_name, false, diffIsError);
    }

    private boolean compareUSets(UnicodeSet a, UnicodeSet b,
                                         String a_name, String b_name,
                                         boolean diffIsError) {
        return
            showAMinusB(a, b, a_name, b_name, diffIsError) &&
            showAMinusB(b, a, b_name, a_name, diffIsError);
    }

   /* various tests for consistency of UCD data and API behavior */
    @Test
    public void TestConsistency() {
       UnicodeSet set1, set2, set3, set4;

       int start, end;
       int i, length;

       String hyphenPattern = "[:Hyphen:]";
       String dashPattern = "[:Dash:]";
       String lowerPattern = "[:Lowercase:]";
       String formatPattern = "[:Cf:]";
       String alphaPattern  =  "[:Alphabetic:]";

       /*
        * It used to be that UCD.html and its precursors said
        * "Those dashes used to mark connections between pieces of words,
        *  plus the Katakana middle dot."
        *
        * Unicode 4 changed 00AD Soft Hyphen to Cf and removed it from Dash
        * but not from Hyphen.
        * UTC 94 (2003mar) decided to leave it that way and to change UCD.html.
        * Therefore, do not show errors when testing the Hyphen property.
        */
       logln("Starting with Unicode 4, inconsistencies with [:Hyphen:] are\n"
                   + "known to the UTC and not considered errors.\n");

       set1=new UnicodeSet(hyphenPattern);
       set2=new UnicodeSet(dashPattern);

           /* remove the Katakana middle dot(s) from set1 */
           set1.remove(0x30fb);
           set2.remove (0xff65); /* halfwidth variant */
           showAMinusB(set1, set2, "[:Hyphen:]", "[:Dash:]", false);


       /* check that Cf is neither Hyphen nor Dash nor Alphabetic */
       set3=new UnicodeSet(formatPattern);
       set4=new UnicodeSet(alphaPattern);

       showAIntersectB(set3, set1, "[:Cf:]", "[:Hyphen:]", false);
       showAIntersectB(set3, set2, "[:Cf:]", "[:Dash:]", true);
       showAIntersectB(set3, set4, "[:Cf:]", "[:Alphabetic:]", true);
       /*
        * Check that each lowercase character has "small" in its name
        * and not "capital".
        * There are some such characters, some of which seem odd.
        * Use the verbose flag to see these notices.
        */
       set1=new UnicodeSet(lowerPattern);

       for(i=0;; ++i) {
//               try{
//                   length=set1.getItem(set1, i, &start, &end, NULL, 0, &errorCode);
//               }catch(Exception e){
//                   break;
//               }
            start = set1.getRangeStart(i);
            end = set1.getRangeEnd(i);
            length = i<set1.getRangeCount() ? set1.getRangeCount() : 0;
           if(length!=0) {
               break; /* done with code points, got a string or -1 */
           }

           while(start<=end) {
               String name=UCharacter.getName(start);

               if( (name.indexOf("SMALL")< 0 || name.indexOf("CAPITAL")<-1) &&
                   name.indexOf("SMALL CAPITAL")==-1
               ) {
                   logln("info: [:Lowercase:] contains U+"+hex(start) + " whose name does not suggest lowercase: " + name);
               }
               ++start;
           }
       }


       /*
        * Test for an example that unorm_getCanonStartSet() delivers
        * all characters that compose from the input one,
        * even in multiple steps.
        * For example, the set for "I" (0049) should contain both
        * I-diaeresis (00CF) and I-diaeresis-acute (1E2E).
        * In general, the set for the middle such character should be a subset
        * of the set for the first.
        */
       Normalizer2 norm2=Normalizer2.getNFDInstance();
       set1=new UnicodeSet();
       Norm2AllModes.getNFCInstance().impl.
           ensureCanonIterData().getCanonStartSet(0x49, set1);
       set2=new UnicodeSet();

       /* enumerate all characters that are plausible to be latin letters */
       for(start=0xa0; start<0x2000; ++start) {
           String decomp=norm2.normalize(UTF16.valueOf(start));
           if(decomp.length() > 1 && decomp.charAt(0)==0x49) {
               set2.add(start);
           }
       }

       compareUSets(set1, set2,
                    "[canon start set of 0049]", "[all c with canon decomp with 0049]",
                    false);

   }

    @Test
    public void TestCoverage() {
        //cover forDigit
        char ch1 = UCharacter.forDigit(7, 11);
        assertEquals("UCharacter.forDigit ", "7", String.valueOf(ch1));
        char ch2 = UCharacter.forDigit(17, 20);
        assertEquals("UCharacter.forDigit ", "h", String.valueOf(ch2));

        //Jitterbug 4451, for coverage
        for (int i = 0x0041; i < 0x005B; i++) {
            if (!UCharacter.isJavaLetter(i))
                errln("FAIL \\u" + hex(i) + " expected to be a letter");
            if (!UCharacter.isJavaIdentifierStart(i))
                errln("FAIL \\u" + hex(i) + " expected to be a Java identifier start character");
            if (!UCharacter.isJavaLetterOrDigit(i))
                errln("FAIL \\u" + hex(i) + " expected not to be a Java letter");
            if (!UCharacter.isJavaIdentifierPart(i))
                errln("FAIL \\u" + hex(i) + " expected to be a Java identifier part character");
        }
        char[] spaces = {'\t','\n','\f','\r',' '};
        for (int i = 0; i < spaces.length; i++){
            if (!UCharacter.isSpace(spaces[i]))
                errln("FAIL \\u" + hex(spaces[i]) + " expected to be a Java space");
        }
    }

    @Test
    public void TestBlockData()
    {
        Class ubc = UCharacter.UnicodeBlock.class;

        for (int b = 1; b < UCharacter.UnicodeBlock.COUNT; b += 1) {
            UCharacter.UnicodeBlock blk = UCharacter.UnicodeBlock.getInstance(b);
            int id = blk.getID();
            String name = blk.toString();

            if (id != b) {
                errln("UCharacter.UnicodeBlock.getInstance(" + b + ") returned a block with id = " + id);
            }

            try {
                if (ubc.getField(name + "_ID").getInt(blk) != b) {
                    errln("UCharacter.UnicodeBlock.getInstance(" + b + ") returned a block with a name of " + name +
                          " which does not match the block id.");
                }
            } catch (Exception e) {
                errln("Couldn't get the id name for id " + b);
            }
        }
    }

    /*
     * The following method tests
     *      public static UnicodeBlock getInstance(int id)
     */
    @Test
    public void TestGetInstance(){
        // Testing values for invalid and valid ID
        int[] invalid_test = {-1,-10,-100};
        for(int i=0; i< invalid_test.length; i++){
            if(UCharacter.UnicodeBlock.INVALID_CODE != UCharacter.UnicodeBlock.getInstance(invalid_test[i])){
                errln("UCharacter.UnicodeBlock.getInstance(invalid_test[i]) was " +
                        "suppose to return UCharacter.UnicodeBlock.INVALID_CODE. Got " +
                        UCharacter.UnicodeBlock.getInstance(invalid_test[i]) + ". Expected " +
                        UCharacter.UnicodeBlock.INVALID_CODE);
            }
        }
    }

    /*
     * The following method tests
     *      public static UnicodeBlock of(int ch)
     */
    @Test
    public void TestOf(){
        if(UCharacter.UnicodeBlock.INVALID_CODE != UCharacter.UnicodeBlock.of(UTF16.CODEPOINT_MAX_VALUE+1)){
            errln("UCharacter.UnicodeBlock.of(UTF16.CODEPOINT_MAX_VALUE+1) was " +
                    "suppose to return UCharacter.UnicodeBlock.INVALID_CODE. Got " +
                    UCharacter.UnicodeBlock.of(UTF16.CODEPOINT_MAX_VALUE+1) + ". Expected " +
                    UCharacter.UnicodeBlock.INVALID_CODE);
        }
    }

    /*
     * The following method tests
     *      public static final UnicodeBlock forName(String blockName)
     */
    @Test
    public void TestForName(){
        //UCharacter.UnicodeBlock.forName("");
        //Tests when "if (b == null)" is true
    }

    /*
     * The following method tests
     *      public static int getNumericValue(int ch)
     */
    @Test
    public void TestGetNumericValue(){
        // The following tests the else statement when
        //      if(numericType<NumericType.COUNT) is false
        // The following values were obtained by testing all values from
        //      UTF16.CODEPOINT_MIN_VALUE to UTF16.CODEPOINT_MAX_VALUE inclusively
        //      to obtain the value to go through the else statement.
        int[] valid_values =
            {3058,3442,4988,8558,8559,8574,8575,8576,8577,8578,8583,8584,19975,
             20159,20191,20740,20806,21315,33836,38433,65819,65820,65821,65822,
             65823,65824,65825,65826,65827,65828,65829,65830,65831,65832,65833,
             65834,65835,65836,65837,65838,65839,65840,65841,65842,65843,65861,
             65862,65863,65868,65869,65870,65875,65876,65877,65878,65899,65900,
             65901,65902,65903,65904,65905,65906,66378,68167};

        int[] results =
            {1000,1000,10000,500,1000,500,1000,1000,5000,10000,50000,100000,
             10000,100000000,1000,100000000,-2,1000,10000,1000,300,400,500,
             600,700,800,900,1000,2000,3000,4000,5000,6000,7000,8000,9000,
             10000,20000,30000,40000,50000,60000,70000,80000,90000,500,5000,
             50000,500,1000,5000,500,1000,10000,50000,300,500,500,500,500,500,
             1000,5000,900,1000};

        if(valid_values.length != results.length){
            errln("The valid_values array and the results array need to be "+
                    "the same length.");
        } else {
            for(int i = 0; i < valid_values.length; i++){
                try{
                    if(UCharacter.getNumericValue(valid_values[i]) != results[i]){
                        errln("UCharacter.getNumericValue(i) returned a " +
                                "different value from the expected result. " +
                                "Got " + UCharacter.getNumericValue(valid_values[i]) +
                                "Expected" + results[i]);
                    }
                } catch(Exception e){
                    errln("UCharacter.getNumericValue(int) returned an exception " +
                            "with the parameter value");
                }
            }
        }
    }

    /*
     * The following method tests
     *      public static double getUnicodeNumericValue(int ch)
     */
    // The following tests covers if(mant==0), else if(mant > 9), and default
    @Test
    public void TestGetUnicodeNumericValue(){
        /*  The code coverage for if(mant==0), else if(mant > 9), and default
         *  could not be covered even with input values from UTF16.CODEPOINT_MIN_VALUE
         *  to UTF16.CODEPOINT_MAX_VALUE. I also tested from UTF16.CODEPOINT_MAX_VALUE to
         *  Integer.MAX_VALUE and didn't recieve any code coverage there too.
         *  Therefore, the code could either be dead code or meaningless.
         */
    }

    /*
     * The following method tests
     *      public static String toString(int ch)
     */
    @Test
    public void TestToString(){
        int[] valid_tests = {
                UCharacter.MIN_VALUE, UCharacter.MIN_VALUE+1,
                UCharacter.MAX_VALUE-1, UCharacter.MAX_VALUE};
        int[] invalid_tests = {
                UCharacter.MIN_VALUE-1, UCharacter.MIN_VALUE-2,
                UCharacter.MAX_VALUE+1, UCharacter.MAX_VALUE+2};

        for(int i=0; i< valid_tests.length; i++){
            if(UCharacter.toString(valid_tests[i]) == null){
                errln("UCharacter.toString(int) was not suppose to return " +
                "null because it was given a valid parameter. Value passed: " +
                valid_tests[i] + ". Got null.");
            }
        }

        for(int i=0; i< invalid_tests.length; i++){
            if(UCharacter.toString(invalid_tests[i]) != null){
                errln("UCharacter.toString(int) was suppose to return " +
                "null because it was given an invalid parameter. Value passed: " +
                invalid_tests[i] + ". Got: " + UCharacter.toString(invalid_tests[i]));
            }
        }
    }

    /*
     * The following method tests
     *      public static int getCombiningClass(int ch)
     */
    @Test
    public void TestGetCombiningClass(){
        int[] valid_tests = {
                UCharacter.MIN_VALUE, UCharacter.MIN_VALUE+1,
                UCharacter.MAX_VALUE-1, UCharacter.MAX_VALUE};
        int[] invalid_tests = {
                UCharacter.MIN_VALUE-1, UCharacter.MIN_VALUE-2,
                UCharacter.MAX_VALUE+1, UCharacter.MAX_VALUE+2};

        for(int i=0; i< valid_tests.length; i++){
            try{
                UCharacter.getCombiningClass(valid_tests[i]);
            } catch(Exception e){
                errln("UCharacter.getCombiningClass(int) was not supposed to have " +
                        "an exception. Value passed: " + valid_tests[i]);
            }
        }

        for(int i=0; i< invalid_tests.length; i++){
            try{
                assertEquals("getCombiningClass(out of range)",
                             0, UCharacter.getCombiningClass(invalid_tests[i]));
            } catch(Exception e){
                errln("UCharacter.getCombiningClass(int) was not supposed to have " +
                        "an exception. Value passed: " + invalid_tests[i]);
            }
        }
    }

    /*
     * The following method tests
     *      public static String getName(int ch)
     */
    @Test
    public void TestGetName(){
        // Need to test on other "one characters" for the getName() method
        String[] data = {"a","z"};
        String[] results = {"LATIN SMALL LETTER A","LATIN SMALL LETTER Z"};
        if(data.length != results.length){
            errln("The data array and the results array need to be "+
                    "the same length.");
        } else {
            for(int i=0; i < data.length; i++){
                if(UCharacter.getName(data[i], "").compareTo(results[i]) != 0){
                    errln("UCharacter.getName(String, String) was suppose " +
                            "to have the same result for the data in the parameter. " +
                            "Value passed: " + data[i] + ". Got: " +
                            UCharacter.getName(data[i], "") + ". Expected: " +
                            results[i]);
                }
            }
        }
    }

    /*
     * The following method tests
     *      public static String getISOComment(int ch)
     */
    @Test
    public void TestGetISOComment(){
        int[] invalid_tests = {
                UCharacter.MIN_VALUE-1, UCharacter.MIN_VALUE-2,
                UCharacter.MAX_VALUE+1, UCharacter.MAX_VALUE+2};

        for(int i=0; i< invalid_tests.length; i++){
            if(UCharacter.getISOComment(invalid_tests[i]) != null){
                errln("UCharacter.getISOComment(int) was suppose to return " +
                "null because it was given an invalid parameter. Value passed: " +
                invalid_tests[i] + ". Got: " + UCharacter.getISOComment(invalid_tests[i]));
            }
        }
    }

    /*
     * The following method tests
     *      public void setLimit(int lim)
     */
    @Test
    public void TestSetLimit(){
        // TODO: Tests when "if(0<=lim && lim<=s.length())" is false
    }

    /*
     * The following method tests
     *      public int nextCaseMapCP()
     */
    @Test
    public void TestNextCaseMapCP(){
        // TODO: Tests when "if(UTF16.LEAD_SURROGATE_MIN_VALUE<=c || c<=UTF16.TRAIL_SURROGATE_MAX_VALUE)" is false
        /* TODO: Tests when "if( c<=UTF16.LEAD_SURROGATE_MAX_VALUE && cpLimit<limit &&
         * UTF16.TRAIL_SURROGATE_MIN_VALUE<=(c2=s.charAt(cpLimit)) && c2<=UTF16.TRAIL_SURROGATE_MAX_VALUE)" is false
         */
    }

    /*
     * The following method tests
     *      public void reset(int direction)
     */
    @Test
    public void TestReset(){
        // The method reset() is never called by another function
        // TODO: Tests when "else if(direction<0)" is false
    }

    /*
     * The following methods test
     *      public static String toTitleCase(Locale locale, String str, BreakIterator breakiter)
     */
    @Test
    public void TestToTitleCaseCoverage(){
        //Calls the function "toTitleCase(Locale locale, String str, BreakIterator breakiter)"
        String[] locale={"en","fr","zh","ko","ja","it","de",""};
        for(int i=0; i<locale.length; i++){
            UCharacter.toTitleCase(new Locale(locale[i]), "", null);
        }

        // Calls the function "String toTitleCase(ULocale locale, String str, BreakIterator titleIter, int options)"
        // Tests when "if (locale == null)" is true
        UCharacter.toTitleCase((ULocale)null, "", null, 0);

        // TODO: Tests when "if(index==BreakIterator.DONE || index>srcLength)" is true
        // TODO: Tests when "while((c=iter.nextCaseMapCP())>=0 && UCaseProps.NONE==gCsp.getType(c))" is false
        // TODO: Tests when "if(prev<titleStart)" is false
        // TODO: Tests when "if(c<=0xffff)" is false
        // TODO: Tests when "if(c<=0xffff)" is false
        // TODO: Tests when "if(titleLimit<index)" is false
        // TODO: Tests when "else if((nc=iter.nextCaseMapCP())>=0)" is false
    }

    @Test
    public void testToTitleCase_Locale_String_BreakIterator_I() {
        String titleCase = UCharacter.toTitleCase(new Locale("nl"), "ijsland", null,
                UCharacter.FOLD_CASE_DEFAULT);
        assertEquals("Wrong title casing", "IJsland", titleCase);
    }

    @Test
    public void testToTitleCase_String_BreakIterator_en() {
        String titleCase = UCharacter.toTitleCase(new Locale("en"), "ijsland", null);
        assertEquals("Wrong title casing", "Ijsland", titleCase);
    }
    /*
     * The following method tests
     *      public static String toUpperCase(ULocale locale, String str)
     */
    @Test
    public void TestToUpperCase(){
        // TODO: Tests when "while((c=iter.nextCaseMapCP())>=0)" is false
    }

    /*
     * The following method tests
     *      public static String toLowerCase(ULocale locale, String str)
     */
    @Test
    public void TestToLowerCase(){
        // Test when locale is null
        String[] cases = {"","a","A","z","Z","Dummy","DUMMY","dummy","a z","A Z",
                "'","\"","0","9","0a","a0","*","~!@#$%^&*()_+"};
        for(int i=0; i<cases.length; i++){
            try{
                UCharacter.toLowerCase((ULocale) null, cases[i]);
            } catch(Exception e){
                errln("UCharacter.toLowerCase was not suppose to return an " +
                        "exception for input of null and string: " + cases[i]);
            }
        }
        // TODO: Tests when "while((c=iter.nextCaseMapCP())>=0)" is false
    }

    /*
     * The following method tests
     *      public static int getHanNumericValue(int ch)
     */
    @Test
    public void TestGetHanNumericValue(){
        int[] valid = {
                0x3007, //IDEOGRAPHIC_NUMBER_ZERO_
                0x96f6, //CJK_IDEOGRAPH_COMPLEX_ZERO_
                0x4e00, //CJK_IDEOGRAPH_FIRST_
                0x58f9, //CJK_IDEOGRAPH_COMPLEX_ONE_
                0x4e8c, //CJK_IDEOGRAPH_SECOND_
                0x8cb3, //CJK_IDEOGRAPH_COMPLEX_TWO_
                0x4e09, //CJK_IDEOGRAPH_THIRD_
                0x53c3, //CJK_IDEOGRAPH_COMPLEX_THREE_
                0x56db, //CJK_IDEOGRAPH_FOURTH_
                0x8086, //CJK_IDEOGRAPH_COMPLEX_FOUR_
                0x4e94, //CJK_IDEOGRAPH_FIFTH_
                0x4f0d, //CJK_IDEOGRAPH_COMPLEX_FIVE_
                0x516d, //CJK_IDEOGRAPH_SIXTH_
                0x9678, //CJK_IDEOGRAPH_COMPLEX_SIX_
                0x4e03, //CJK_IDEOGRAPH_SEVENTH_
                0x67d2, //CJK_IDEOGRAPH_COMPLEX_SEVEN_
                0x516b, //CJK_IDEOGRAPH_EIGHTH_
                0x634c, //CJK_IDEOGRAPH_COMPLEX_EIGHT_
                0x4e5d, //CJK_IDEOGRAPH_NINETH_
                0x7396, //CJK_IDEOGRAPH_COMPLEX_NINE_
                0x5341, //CJK_IDEOGRAPH_TEN_
                0x62fe, //CJK_IDEOGRAPH_COMPLEX_TEN_
                0x767e, //CJK_IDEOGRAPH_HUNDRED_
                0x4f70, //CJK_IDEOGRAPH_COMPLEX_HUNDRED_
                0x5343, //CJK_IDEOGRAPH_THOUSAND_
                0x4edf, //CJK_IDEOGRAPH_COMPLEX_THOUSAND_
                0x824c, //CJK_IDEOGRAPH_TEN_THOUSAND_
                0x5104, //CJK_IDEOGRAPH_HUNDRED_MILLION_
        };

        int[] invalid = {-5,-2,-1,0};

        int[] results = {0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,100,100,
                1000,1000,10000,100000000};

        if(valid.length != results.length){
            errln("The arrays valid and results are suppose to be the same length " +
                    "to test getHanNumericValue(int ch).");
        } else{
            for(int i=0; i<valid.length; i++){
                if(UCharacter.getHanNumericValue(valid[i]) != results[i]){
                    errln("UCharacter.getHanNumericValue does not return the " +
                            "same result as expected. Passed value: " + valid[i] +
                            ". Got: " + UCharacter.getHanNumericValue(valid[i]) +
                            ". Expected: " + results[i]);
                }
            }
        }

        for(int i=0; i<invalid.length; i++){
            if(UCharacter.getHanNumericValue(invalid[i]) != -1){
                errln("UCharacter.getHanNumericValue does not return the " +
                        "same result as expected. Passed value: " + invalid[i] +
                        ". Got: " + UCharacter.getHanNumericValue(invalid[i]) +
                        ". Expected: -1");
            }
        }
    }

    /*
     * The following method tests
     *      public static boolean hasBinaryProperty(int ch, int property)
     */
    @Test
    public void TestHasBinaryProperty(){
        // Testing when "if (ch < MIN_VALUE || ch > MAX_VALUE)" is true
        int[] invalid = {
                UCharacter.MIN_VALUE-1, UCharacter.MIN_VALUE-2,
                UCharacter.MAX_VALUE+1, UCharacter.MAX_VALUE+2};
        int[] valid = {
                UCharacter.MIN_VALUE, UCharacter.MIN_VALUE+1,
                UCharacter.MAX_VALUE, UCharacter.MAX_VALUE-1};

        for(int i=0; i<invalid.length; i++){
            try{
                if (UCharacter.hasBinaryProperty(invalid[i], 1)) {
                    errln("UCharacter.hasBinaryProperty(ch, property) should return " +
                            "false for out-of-range code points but " +
                            "returns true for " + invalid[i]);
                }
            } catch(Exception e) {
                errln("UCharacter.hasBinaryProperty(ch, property) should not " +
                        "throw an exception for any input. Value passed: " +
                        invalid[i]);
            }
        }

        for(int i=0; i<valid.length; i++){
            try{
                UCharacter.hasBinaryProperty(valid[i], 1);
            } catch(Exception e) {
                errln("UCharacter.hasBinaryProperty(ch, property) should not " +
                        "throw an exception for any input. Value passed: " +
                        valid[i]);
            }
        }
    }

    /*
     * The following method tests
     *      public static int getIntPropertyValue(int ch, int type)
     */
    @Test
    public void TestGetIntPropertyValue(){
        /* Testing UCharacter.getIntPropertyValue(ch, type) */
        // Testing when "if (type < UProperty.BINARY_START)" is true
        int[] negative_cases = {-100,-50,-10,-5,-2,-1};
        for(int i=0; i<negative_cases.length; i++){
            if(UCharacter.getIntPropertyValue(0, negative_cases[i]) != 0){
                errln("UCharacter.getIntPropertyValue(ch, type) was suppose to return 0 " +
                        "when passing a negative value of " + negative_cases[i]);

            }
        }

        // Testing when "if(ch<NormalizerImpl.JAMO_L_BASE)" is true
        for(int i=Normalizer2Impl.Hangul.JAMO_L_BASE-5; i<Normalizer2Impl.Hangul.JAMO_L_BASE; i++){
            if(UCharacter.getIntPropertyValue(i, UProperty.HANGUL_SYLLABLE_TYPE) != 0){
                errln("UCharacter.getIntPropertyValue(ch, type) was suppose to return 0 " +
                        "when passing ch: " + i + "and type of Property.HANGUL_SYLLABLE_TYPE");

            }
        }

        // Testing when "else if((ch-=NormalizerImpl.HANGUL_BASE)<0)" is true
        for(int i=Normalizer2Impl.Hangul.HANGUL_BASE-5; i<Normalizer2Impl.Hangul.HANGUL_BASE; i++){
            if(UCharacter.getIntPropertyValue(i, UProperty.HANGUL_SYLLABLE_TYPE) != 0){
                errln("UCharacter.getIntPropertyValue(ch, type) was suppose to return 0 " +
                        "when passing ch: " + i + "and type of Property.HANGUL_SYLLABLE_TYPE");

            }
        }
    }

    /*
     * The following method tests
     *      public static int getIntPropertyMaxValue(int type)
     */
    @Test
    public void TestGetIntPropertyMaxValue(){
        /* Testing UCharacter.getIntPropertyMaxValue(type) */
        // Testing when "else if (type < UProperty.INT_START)" is true
        int[] cases = {UProperty.BINARY_LIMIT, UProperty.BINARY_LIMIT+1,
                UProperty.INT_START-2, UProperty.INT_START-1};
        for(int i=0; i<cases.length; i++){
            if(UCharacter.getIntPropertyMaxValue(cases[i]) != -1){
                errln("UCharacter.getIntPropertyMaxValue was suppose to return -1 " +
                        "but got " + UCharacter.getIntPropertyMaxValue(cases[i]));
            }
        }

        // TODO: Testing when the case statment reaches "default"
        // After testing between values of UProperty.INT_START and
        // UProperty.INT_LIMIT are covered, none of the values reaches default.
    }

    /*
     * The following method tests
     *      public static final int codePointAt(CharSequence seq, int index)
     *      public static final int codePointAt(char[] text, int index, int limit)
     */
    @Test
    public void TestCodePointAt(){

        // {LEAD_SURROGATE_MIN_VALUE,
        //  LEAD_SURROGATE_MAX_VALUE, LEAD_SURROGATE_MAX_VALUE-1
        String[] cases = {"\uD800","\uDBFF","\uDBFE"};
        int[] result = {55296,56319,56318};
        for(int i=0; i < cases.length; i++){
            /* Testing UCharacter.codePointAt(seq, index) */
            // Testing when "if (index < seq.length())" is false
            if(UCharacter.codePointAt(cases[i], 0) != result[i])
                errln("UCharacter.codePointAt(CharSequence ...) did not return as expected. " +
                        "Passed value: " + cases[i] + ". Expected: " +
                        result[i] + ". Got: " +
                        UCharacter.codePointAt(cases[i], 0));

            /* Testing UCharacter.codePointAt(text, index) */
            // Testing when "if (index < text.length)" is false
            if(UCharacter.codePointAt(cases[i].toCharArray(), 0) != result[i])
                errln("UCharacter.codePointAt(char[] ...) did not return as expected. " +
                        "Passed value: " + cases[i] + ". Expected: " +
                        result[i] + ". Got: " +
                        UCharacter.codePointAt(cases[i].toCharArray(), 0));

            /* Testing UCharacter.codePointAt(text, index, limit) */
            // Testing when "if (index < limit)" is false
            if(UCharacter.codePointAt(cases[i].toCharArray(), 0, 1) != result[i])
                errln("UCharacter.codePointAt(char[], int, int) did not return as expected. " +
                        "Passed value: " + cases[i] + ". Expected: " +
                        result[i] + ". Got: " +
                        UCharacter.codePointAt(cases[i].toCharArray(), 0, 1));
        }

        /* Testing UCharacter.codePointAt(text, index, limit) */
        // Testing when "if (index >= limit || limit > text.length)" is true
        char[] empty_text = {};
        char[] one_char_text = {'a'};
        char[] reg_text = {'d','u','m','m','y'};
        int[] limitCases = {2,3,5,10,25};

        // When index >= limit
        for(int i=0; i < limitCases.length; i++){
            try{
                UCharacter.codePointAt(reg_text, 100, limitCases[i]);
                errln("UCharacter.codePointAt was suppose to return an exception " +
                        "but got " + UCharacter.codePointAt(reg_text, 100, limitCases[i]) +
                        ". The following passed parameters were Text: " + String.valueOf(reg_text) + ", Start: " +
                        100 + ", Limit: " + limitCases[i] + ".");
            } catch(Exception e){
            }
        }

        // When limit > text.length
        for(int i=0; i < limitCases.length; i++){
            try{
                UCharacter.codePointAt(empty_text, 0, limitCases[i]);
                errln("UCharacter.codePointAt was suppose to return an exception " +
                        "but got " + UCharacter.codePointAt(empty_text, 0, limitCases[i]) +
                        ". The following passed parameters were Text: " + String.valueOf(empty_text) + ", Start: " +
                        0 + ", Limit: " + limitCases[i] + ".");
            } catch(Exception e){
            }

            try{
                UCharacter.codePointCount(one_char_text, 0, limitCases[i]);
                errln("UCharacter.codePointCount was suppose to return an exception " +
                        "but got " + UCharacter.codePointCount(one_char_text, 0, limitCases[i]) +
                        ". The following passed parameters were Text: " + String.valueOf(one_char_text) + ", Start: " +
                        0 + ", Limit: " + limitCases[i] + ".");
            } catch(Exception e){
            }
        }
    }

    /*
     * The following method tests
     *      public static final int codePointBefore(CharSequence seq, int index)
     *      public static final int codePointBefore(char[] text, int index)
     *      public static final int codePointBefore(char[] text, int index, int limit)
     */
    @Test
    public void TestCodePointBefore(){
        // {TRAIL_SURROGATE_MIN_VALUE,
        //  TRAIL_SURROGATE_MAX_VALUE, TRAIL_SURROGATE_MAX_VALUE -1
        String[] cases = {"\uDC00","\uDFFF","\uDDFE"};
        int[] result = {56320,57343,56830};
        for(int i=0; i < cases.length; i++){
            /* Testing UCharacter.codePointBefore(seq, index) */
            // Testing when "if (index > 0)" is false
            if(UCharacter.codePointBefore(cases[i], 1) != result[i])
                errln("UCharacter.codePointBefore(CharSequence ...) did not return as expected. " +
                        "Passed value: " + cases[i] + ". Expected: " +
                        result[i] + ". Got: " +
                        UCharacter.codePointBefore(cases[i], 1));

            /* Testing UCharacter.codePointBefore(text, index) */
            // Testing when "if (index > 0)" is false
            if(UCharacter.codePointBefore(cases[i].toCharArray(), 1) != result[i])
                errln("UCharacter.codePointBefore(char[] ...) did not return as expected. " +
                        "Passed value: " + cases[i] + ". Expected: " +
                        result[i] + ". Got: " +
                        UCharacter.codePointBefore(cases[i].toCharArray(), 1));

            /* Testing UCharacter.codePointBefore(text, index, limit) */
            // Testing when "if (index > limit)" is false
            if(UCharacter.codePointBefore(cases[i].toCharArray(), 1, 0) != result[i])
                errln("UCharacter.codePointBefore(char[], int, int) did not return as expected. " +
                        "Passed value: " + cases[i] + ". Expected: " +
                        result[i] + ". Got: " +
                        UCharacter.codePointBefore(cases[i].toCharArray(), 1, 0));
        }

        /* Testing UCharacter.codePointBefore(text, index, limit) */
        char[] dummy = {'d','u','m','m','y'};
        // Testing when "if (index <= limit || limit < 0)" is true
        int[] negative_cases = {-100,-10,-5,-2,-1};
        int[] index_cases = {0,1,2,5,10,100};

        for(int i=0; i < negative_cases.length; i++){
            try{
                UCharacter.codePointBefore(dummy, 10000, negative_cases[i]);
                errln("UCharacter.codePointBefore(text, index, limit) was suppose to return an exception " +
                        "when the parameter limit of " + negative_cases[i] + " is a negative number.");
            } catch(Exception e) {}
        }

        for(int i=0; i < index_cases.length; i++){
            try{
                UCharacter.codePointBefore(dummy, index_cases[i], 101);
                errln("UCharacter.codePointBefore(text, index, limit) was suppose to return an exception " +
                        "when the parameter index of " + index_cases[i] + " is a negative number.");
            } catch(Exception e) {}
        }
    }

    /*
     * The following method tests
     *      public static final int toChars(int cp, char[] dst, int dstIndex)
     *      public static final char[] toChars(int cp)
     */
    @Test
    public void TestToChars(){
        int[] positive_cases = {1,2,5,10,100};
        char[] dst = {'a'};

        /* Testing UCharacter.toChars(cp, dst, dstIndex) */
        for(int i=0; i < positive_cases.length; i++){
            // Testing negative values when cp < 0 for if (cp >= 0)
            try{
                UCharacter.toChars(-1*positive_cases[i],dst,0);
                errln("UCharacter.toChars(int,char[],int) was suppose to return an exception " +
                        "when the parameter " + (-1*positive_cases[i]) + " is a negative number.");
            } catch(Exception e){
            }

            // Testing when "if (cp < MIN_SUPPLEMENTARY_CODE_POINT)" is true
            if(UCharacter.toChars(UCharacter.MIN_SUPPLEMENTARY_CODE_POINT-positive_cases[i], dst, 0) != 1){
                errln("UCharacter.toChars(int,char[],int) was suppose to return a value of 1. Got: " +
                        UCharacter.toChars(UCharacter.MIN_SUPPLEMENTARY_CODE_POINT-positive_cases[i], dst, 0));
            }

            // Testing when "if (cp < MIN_SUPPLEMENTARY_CODE_POINT)" is false and
            //     when "if (cp <= MAX_CODE_POINT)" is false
            try{
                UCharacter.toChars(UCharacter.MAX_CODE_POINT+positive_cases[i],dst,0);
                errln("UCharacter.toChars(int,char[],int) was suppose to return an exception " +
                        "when the parameter " + (UCharacter.MAX_CODE_POINT+positive_cases[i]) +
                        " is a large number.");
            } catch(Exception e){
            }
        }


        /* Testing UCharacter.toChars(cp)*/
        for(int i=0; i<positive_cases.length; i++){
            // Testing negative values when cp < 0 for if (cp >= 0)
            try{
                UCharacter.toChars(-1*positive_cases[i]);
                errln("UCharacter.toChars(cint) was suppose to return an exception " +
                        "when the parameter " + positive_cases[i] + " is a negative number.");
            } catch(Exception e){
            }

            // Testing when "if (cp < MIN_SUPPLEMENTARY_CODE_POINT)" is true
            if(UCharacter.toChars(UCharacter.MIN_SUPPLEMENTARY_CODE_POINT-positive_cases[i]).length <= 0){
                errln("UCharacter.toChars(int) was suppose to return some result result when the parameter " +
                        (UCharacter.MIN_SUPPLEMENTARY_CODE_POINT-positive_cases[i]) + "is passed.");
            }

            // Testing when "if (cp < MIN_SUPPLEMENTARY_CODE_POINT)" is false and
            //     when "if (cp <= MAX_CODE_POINT)" is false
            try{
                UCharacter.toChars(UCharacter.MAX_CODE_POINT+positive_cases[i]);
                errln("UCharacter.toChars(int) was suppose to return an exception " +
                        "when the parameter " + positive_cases[i] + " is a large number.");
            } catch(Exception e){
            }
        }
    }

    /*
     * The following method tests
     *      public static int codePointCount(CharSequence text, int start, int limit)
     *      public static int codePointCount(char[] text, int start, int limit)
     */
    @Test
    public void TestCodePointCount(){
        // The following tests the first if statement to make it true:
        //  if (start < 0 || limit < start || limit > text.length)
        //  which will throw an exception.
        char[] empty_text = {};
        char[] one_char_text = {'a'};
        char[] reg_text = {'d','u','m','m','y'};
        int[] invalid_startCases = {-1,-2,-5,-10,-100};
        int[] limitCases = {2,3,5,10,25};

        // When start < 0
        for(int i=0; i < invalid_startCases.length; i++){
            try{
                UCharacter.codePointCount(reg_text, invalid_startCases[i], 1);
                errln("UCharacter.codePointCount was suppose to return an exception " +
                        "but got " + UCharacter.codePointCount(reg_text, invalid_startCases[i], 1) +
                        ". The following passed parameters were Text: " + String.valueOf(reg_text) + ", Start: " +
                        invalid_startCases[i] + ", Limit: " + 1 + ".");
            } catch(Exception e){
            }
        }

        // When limit < start
        for(int i=0; i < limitCases.length; i++){
            try{
                UCharacter.codePointCount(reg_text, 100, limitCases[i]);
                errln("UCharacter.codePointCount was suppose to return an exception " +
                        "but got " + UCharacter.codePointCount(reg_text, 100, limitCases[i]) +
                        ". The following passed parameters were Text: " + String.valueOf(reg_text) + ", Start: " +
                        100 + ", Limit: " + limitCases[i] + ".");
            } catch(Exception e){
            }
        }

        // When limit > text.length
        for(int i=0; i < limitCases.length; i++){
            try{
                UCharacter.codePointCount(empty_text, 0, limitCases[i]);
                errln("UCharacter.codePointCount was suppose to return an exception " +
                        "but got " + UCharacter.codePointCount(empty_text, 0, limitCases[i]) +
                        ". The following passed parameters were Text: " + String.valueOf(empty_text) + ", Start: " +
                        0 + ", Limit: " + limitCases[i] + ".");
            } catch(Exception e){
            }

            try{
                UCharacter.codePointCount(one_char_text, 0, limitCases[i]);
                errln("UCharacter.codePointCount was suppose to return an exception " +
                        "but got " + UCharacter.codePointCount(one_char_text, 0, limitCases[i]) +
                        ". The following passed parameters were Text: " + String.valueOf(one_char_text) + ", Start: " +
                        0 + ", Limit: " + limitCases[i] + ".");
            } catch(Exception e){
            }
        }
    }

    /*
     * The following method tests
     *      private static int getEuropeanDigit(int ch)
     * The method needs to use the method "digit" in order to access the
     * getEuropeanDigit method.
     */
    @Test
    public void TestGetEuropeanDigit(){
        //The number retrieved from 0xFF41 to 0xFF5A is due to
        //  exhaustive testing from UTF16.CODEPOINT_MIN_VALUE to
        //  UTF16.CODEPOINT_MAX_VALUE return a value of -1.

        int[] radixResult = {
                10,11,12,13,14,15,16,17,18,19,20,21,22,
                23,24,25,26,27,28,29,30,31,32,33,34,35};
        // Invalid and too-small-for-these-digits radix values.
        int[] radixCase1 = {0,1,5,10,100};
        // Radix values that work for at least some of the "digits".
        int[] radixCase2 = {12,16,20,36};

        for(int i=0xFF41; i<=0xFF5A; i++){
            for(int j=0; j < radixCase1.length; j++){
                if(UCharacter.digit(i, radixCase1[j]) != -1){
                    errln("UCharacter.digit(int,int) was supposed to return -1 for radix " + radixCase1[j]
                            + ". Value passed: U+" + Integer.toHexString(i) + ". Got: " + UCharacter.digit(i, radixCase1[j]));
                }
            }
            for(int j=0; j < radixCase2.length; j++){
                int radix = radixCase2[j];
                int expected = (radixResult[i-0xFF41] < radix) ? radixResult[i-0xFF41] : -1;
                int actual = UCharacter.digit(i, radix);
                if(actual != expected){
                    errln("UCharacter.digit(int,int) was supposed to return " +
                            expected + " for radix " + radix +
                            ". Value passed: U+" + Integer.toHexString(i) + ". Got: " + actual);
                    break;
                }
            }
        }
    }

    /* Tests the method
     *      private static final int getProperty(int ch)
     * from public static int getType(int ch)
     */
    @Test
    public void TestGetProperty(){
        int[] cases = {UTF16.CODEPOINT_MAX_VALUE+1, UTF16.CODEPOINT_MAX_VALUE+2};
        for(int i=0; i < cases.length; i++)
            if(UCharacter.getType(cases[i]) != 0)
                errln("UCharacter.getType for testing UCharacter.getProperty "
                        + "did not return 0 for passed value of " + cases[i] +
                        " but got " + UCharacter.getType(cases[i]));
    }

    /* Tests the class
     *      abstract public static class XSymbolTable implements SymbolTable
     */
    @Test
    public void TestXSymbolTable(){
        class MyXSymbolTable extends UnicodeSet.XSymbolTable {}
        MyXSymbolTable st = new MyXSymbolTable();

        // Tests "public UnicodeMatcher lookupMatcher(int i)"
        if(st.lookupMatcher(0) != null)
            errln("XSymbolTable.lookupMatcher(int i) was suppose to return null.");

        // Tests "public boolean applyPropertyAlias(String propertyName, String propertyValue, UnicodeSet result)"
        if(st.applyPropertyAlias("", "", new UnicodeSet()) != false)
            errln("XSymbolTable.applyPropertyAlias(String propertyName, String propertyValue, UnicodeSet result) was suppose to return false.");

        // Tests "public char[] lookup(String s)"
        if(st.lookup("") != null)
            errln("XSymbolTable.lookup(String s) was suppose to return null.");

        // Tests "public String parseReference(String text, ParsePosition pos, int limit)"
        if(st.parseReference("", null, 0) != null)
            errln("XSymbolTable.parseReference(String text, ParsePosition pos, int limit) was suppose to return null.");
    }

    /* Tests the method
     *      public boolean isFrozen()
     */
    @Test
    public void TestIsFrozen(){
        UnicodeSet us = new UnicodeSet();
        if(us.isFrozen() != false)
            errln("Unicode.isFrozen() was suppose to return false.");

        us.freeze();
        if(us.isFrozen() != true)
            errln("Unicode.isFrozen() was suppose to return true.");
    }

    /* Tests the methods
     *      public static String getNameAlias() and
     *      public static String getCharFromNameAlias()
     */
    @Test
    public void testNameAliasing() {
        int input = '\u01a2';
        String alias = UCharacter.getNameAlias(input);
        assertEquals("Wrong name alias", "LATIN CAPITAL LETTER GHA", alias);
        int output = UCharacter.getCharFromNameAlias(alias);
        assertEquals("alias for '" + input + "'", input, output);
    }
}
