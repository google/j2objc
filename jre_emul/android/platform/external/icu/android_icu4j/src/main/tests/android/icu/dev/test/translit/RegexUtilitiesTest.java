/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.translit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.UnicodeRegex;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.lang.UProperty.NameChoice;
import android.icu.text.Transliterator;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;

/**
 * @author markdavis
 */
public class RegexUtilitiesTest extends TestFmwk {
    /**
     * Check basic construction.
     */
    @Test
    public void TestConstruction() {
        String[][] tests = {
                {"a"},
                {"a[a-z]b"},
                {"[ba-z]", "[a-z]"},
                {"q[ba-z]", "q[a-z]"},
                {"[ba-z]q", "[a-z]q"},
                {"a\\p{joincontrol}b", "a[\u200C\u200D]b"},
                {"a\\P{joincontrol}b", "a[^\u200C\u200D]b"},
                {"a[[:whitespace:]&[:Zl:]]b", "a[\\\u2028]b"},
                {"a [[:bc=cs:]&[:wspace:]] b", "a [\u00A0\u202F] b"},
        };
        for (int i = 0; i < tests.length; ++i) {
            final String source = tests[i][0];
            String expected = tests[i].length == 1 ? source : tests[i][1];
            String actual = UnicodeRegex.fix(source);
            assertEquals(source, expected, actual);
        } 
    }

    Transliterator hex = Transliterator.getInstance("hex");

    /**
     * Perform an exhaustive test on all Unicode characters to make sure that the UnicodeSet with each
     * character works.
     */
    @Test
    public void TestCharacters() {
        UnicodeSet requiresQuote = new UnicodeSet("[\\$\\&\\-\\:\\[\\\\\\]\\^\\{\\}[:pattern_whitespace:]]");
        boolean skip = TestFmwk.getExhaustiveness() < 10;
        for (int cp = 0; cp < 0x110000; ++cp) {
            if (cp > 0xFF && skip && (cp % 37 != 0)) {
                continue;
            }
            String cpString = UTF16.valueOf(cp);
            String s = requiresQuote.contains(cp) ? "\\" + cpString : cpString;
            String pattern = null;
            final String rawPattern = "[" + s + s + "]";
            try {
                pattern = UnicodeRegex.fix(rawPattern);
            } catch (Exception e) {
                errln(e.getMessage());
                continue;
            }
            final String expected = "[" + s + "]";
            assertEquals("Doubled character works" + hex.transform(s), expected, pattern);

            // verify that we can create a regex pattern and use as expected
            String shouldNotMatch = UTF16.valueOf((cp + 1) % 0x110000);
            checkCharPattern(Pattern.compile(pattern), pattern, cpString, shouldNotMatch);

            // verify that the Pattern.compile works
            checkCharPattern(UnicodeRegex.compile(rawPattern), pattern, cpString, shouldNotMatch);
        }
    }

    /**
     * Check all integer Unicode properties to make sure they work.
     */
    @Test
    public void TestUnicodeProperties() {
        final boolean skip = TestFmwk.getExhaustiveness() < 10;
        UnicodeSet temp = new UnicodeSet();
        for (int propNum = UProperty.INT_START; propNum < UProperty.INT_LIMIT; ++propNum) {
            if (skip && (propNum % 5 != 0)) {
                continue;
            }
            String propName = UCharacter.getPropertyName(propNum, NameChoice.LONG);
            final int intPropertyMinValue = UCharacter.getIntPropertyMinValue(propNum);
            int intPropertyMaxValue = UCharacter.getIntPropertyMaxValue(propNum);
            if (skip) { // only test first if not exhaustive
                intPropertyMaxValue = intPropertyMinValue;
            }
            for (int valueNum = intPropertyMinValue; valueNum <= intPropertyMaxValue; ++valueNum) {
                // hack for getting property value name
                String valueName = UCharacter.getPropertyValueName(propNum, valueNum, NameChoice.LONG);
                if (valueName == null) {
                    valueName = UCharacter.getPropertyValueName(propNum, valueNum, NameChoice.SHORT);
                    if (valueName == null) {
                        valueName = Integer.toString(valueNum);
                    }
                }
                temp.applyIntPropertyValue(propNum, valueNum);
                if (temp.size() == 0) {
                    continue;
                }
                final String prefix = "a";
                final String suffix = "b";
                String shouldMatch = prefix + UTF16.valueOf(temp.charAt(0)) + suffix;
                temp.complement();
                String shouldNotMatch = prefix + UTF16.valueOf(temp.charAt(0)) + suffix;

                // posix style pattern
                String rawPattern = prefix + "[:" + propName + "=" + valueName + ":]" + suffix;
                String rawNegativePattern = prefix + "[:^" + propName + "=" + valueName + ":]" + suffix;
                checkCharPattern(UnicodeRegex.compile(rawPattern), rawPattern, shouldMatch, shouldNotMatch);
                checkCharPattern(UnicodeRegex.compile(rawNegativePattern), rawNegativePattern, shouldNotMatch, shouldMatch);

                // perl style pattern
                rawPattern = prefix + "\\p{" + propName + "=" + valueName + "}" + suffix;
                rawNegativePattern = prefix + "\\P{" + propName + "=" + valueName + "}" + suffix;
                checkCharPattern(UnicodeRegex.compile(rawPattern), rawPattern, shouldMatch, shouldNotMatch);
                checkCharPattern(UnicodeRegex.compile(rawNegativePattern), rawNegativePattern, shouldNotMatch, shouldMatch);
            }
        }
    }

    @Test
    public void TestBnf() {
        UnicodeRegex regex = new UnicodeRegex();
        final String[][] tests = {
                {
                    "c = a wq;\n" +
                    "a = xyz;\n" +
                    "b = a a c;\n"
                },
                {
                    "c = a b;\n" +
                    "a = xyz;\n" +
                    "b = a a c;\n",
                    "Exception"
                },
                {
                    "uri = (?: (scheme) \\:)? (host) (?: \\? (query))? (?: \\u0023 (fragment))?;\n" +
                    "scheme = reserved+;\n" +
                    "host = // reserved+;\n" +
                    "query = [\\=reserved]+;\n" +
                    "fragment = reserved+;\n" +
                    "reserved = [[:ascii:][:sc=grek:]&[:alphabetic:]];\n",
                "http://\u03B1\u03B2\u03B3?huh=hi#there"},
                {
                    "langtagRegex.txt"
                }
        };
        for (int i = 0; i < tests.length; ++i) {
            String test = tests[i][0];
            final boolean expectException = tests[i].length < 2 ? false : tests[i][1].equals("Exception");
            try {
                String result;
                if (test.endsWith(".txt")) {
                    java.io.InputStream is = RegexUtilitiesTest.class.getResourceAsStream(test);
                    List lines;
                    try {
                        lines = UnicodeRegex.appendLines(new ArrayList(), is, "UTF-8");
                    } finally {
                        is.close();
                    }
                    result = regex.compileBnf(lines);
                } else {
                    result = regex.compileBnf(test);
                }
                if (expectException) {
                    errln("Expected exception for " + test);
                    continue;
                }
                result = result.replaceAll("[0-9]+%", ""); // just so we can use the language subtag stuff
                String resolved = regex.transform(result);
                logln(resolved);
                Matcher m = Pattern.compile(resolved, Pattern.COMMENTS).matcher("");
                String checks = "";
                for (int j = 1; j < tests[i].length; ++j) {
                    String check = tests[i][j];
                    if (!m.reset(check).matches()) {
                        checks = checks + "Fails " + check + "\n";
                    } else {
                        for (int k = 1; k <= m.groupCount(); ++k) {
                            checks += "(" + m.group(k) + ")";
                        }
                        checks += "\n";
                    }
                }
                logln("Result: " + result + "\n" + checks + "\n" + test);
            } catch (Exception e) {
                if (!expectException) {
                    errln(e.getClass().getName() + ": " + e.getMessage());
                }
                continue;
            }
        }
    }

    /**
     * Utility for checking patterns
     */
    private void checkCharPattern(Pattern pat, String matchTitle, String shouldMatch, String shouldNotMatch) {
        Matcher matcher = pat.matcher(shouldMatch);
        assertTrue(matchTitle + " and " + shouldMatch, matcher.matches());
        matcher.reset(shouldNotMatch);
        assertFalse(matchTitle + " and " + shouldNotMatch, matcher.matches());
    }
}
