/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/********************************************************************
 * Copyright (c) 2002-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 ********************************************************************/

/**
 * UCAConformanceTest performs conformance tests defined in the data
 * files. ICU ships with stub data files, as the whole test are too
 * long. To do the whole test, download the test files.
 */

package android.icu.dev.test.collator;

import java.io.BufferedReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.lang.UCharacter;
import android.icu.text.Collator;
import android.icu.text.RawCollationKey;
import android.icu.text.RuleBasedCollator;
import android.icu.text.UTF16;
import android.icu.util.ULocale;
import android.icu.util.VersionInfo;

public class UCAConformanceTest extends TestFmwk {

    public UCAConformanceTest() {
    }

    @Before
    public void init() throws Exception {
        UCA = (RuleBasedCollator) Collator.getInstance(ULocale.ROOT);
        comparer = new UTF16.StringComparator(true, false, UTF16.StringComparator.FOLD_CASE_DEFAULT);
    }

    private RuleBasedCollator UCA;
    private RuleBasedCollator rbUCA;
    private UTF16.StringComparator comparer;
    private boolean isAtLeastUCA62 = UCharacter.getUnicodeVersion().compareTo(VersionInfo.UNICODE_6_2) >= 0;

    @Test
    public void TestTableNonIgnorable() {
        setCollNonIgnorable(UCA);
        openTestFile("NON_IGNORABLE");
        conformanceTest(UCA);
    }

    @Test
    public void TestTableShifted() {
        setCollShifted(UCA);
        openTestFile("SHIFTED");
        conformanceTest(UCA);
    }

    @Test
    public void TestRulesNonIgnorable() {
        if (logKnownIssue("cldrbug:6745", "UCARules.txt has problems")) {
            return;
        }
        initRbUCA();
        if (rbUCA == null) {
            return;
        }

        setCollNonIgnorable(rbUCA);
        openTestFile("NON_IGNORABLE");
        conformanceTest(rbUCA);
    }

    @Test
    public void TestRulesShifted() {
        logln("This test is currently disabled, as it is impossible to "
                + "wholly represent fractional UCA using tailoring rules.");
        return;
        /*
         * initRbUCA(); if(rbUCA == null) { return; }
         *
         * setCollShifted(rbUCA); openTestFile("SHIFTED"); testConformance(rbUCA);
         */
    }

    BufferedReader in;

    private void openTestFile(String type) {
        String collationTest = "CollationTest_";
        String ext = ".txt";
        try {
            in = TestUtil.getDataReader(collationTest + type + "_SHORT" + ext);
        } catch (Exception e) {
            try {
                in = TestUtil.getDataReader(collationTest + type + ext);
            } catch (Exception e1) {
                try {
                    in = TestUtil.getDataReader(collationTest + type + "_STUB" + ext);
                    logln("INFO: Working with the stub file.\n" + "If you need the full conformance test, please\n"
                            + "download the appropriate data files from:\n"
                            + "http://unicode.org/cldr/trac/browser/trunk/common/uca");
                } catch (Exception e11) {
                    errln("ERROR: Could not find any of the test files");
                }
            }
        }
    }

    private void setCollNonIgnorable(RuleBasedCollator coll) {
        if (coll != null) {
            coll.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            coll.setLowerCaseFirst(false);
            coll.setCaseLevel(false);
            coll.setStrength(isAtLeastUCA62 ? Collator.IDENTICAL : Collator.TERTIARY);
            coll.setAlternateHandlingShifted(false);
        }
    }

    private void setCollShifted(RuleBasedCollator coll) {
        if (coll != null) {
            coll.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            coll.setLowerCaseFirst(false);
            coll.setCaseLevel(false);
            coll.setStrength(isAtLeastUCA62 ? Collator.IDENTICAL : Collator.QUATERNARY);
            coll.setAlternateHandlingShifted(true);
        }
    }

    private void initRbUCA() {
        if (rbUCA == null) {
            String ucarules = UCA.getRules(true);
            try {
                rbUCA = new RuleBasedCollator(ucarules);
            } catch (Exception e) {
                errln("Failure creating UCA rule-based collator: " + e);
            }
        }
    }

    private String parseString(String line) {
        int i = 0, value;
        StringBuilder result = new StringBuilder(), buffer = new StringBuilder();

        for (;;) {
            while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
                i++;
            }
            while (i < line.length() && Character.isLetterOrDigit(line.charAt(i))) {
                buffer.append(line.charAt(i));
                i++;
            }
            if (buffer.length() == 0) {
                // We hit something that was not whitespace/letter/digit.
                // Should be ';' or end of string.
                return result.toString();
            }
            /* read one code point */
            value = Integer.parseInt(buffer.toString(), 16);
            buffer.setLength(0);
            result.appendCodePoint(value);
        }

    }

    private static final int IS_SHIFTED = 1;
    private static final int FROM_RULES = 2;

    private static boolean skipLineBecauseOfBug(String s, int flags) {
        // Add temporary exceptions here if there are ICU bugs, until we can fix them.
        // For examples see the ICU 52 version of this file.
        return false;
    }

    private static int normalizeResult(int result) {
        return result < 0 ? -1 : result == 0 ? 0 : 1;
    }

    private void conformanceTest(RuleBasedCollator coll) {
        if (in == null || coll == null) {
            return;
        }
        int skipFlags = 0;
        if (coll.isAlternateHandlingShifted()) {
            skipFlags |= IS_SHIFTED;
        }
        if (coll == rbUCA) {
            skipFlags |= FROM_RULES;
        }

        logln("-prop:ucaconfnosortkeys=1 turns off getSortKey() in UCAConformanceTest");
        boolean withSortKeys = getProperty("ucaconfnosortkeys") == null;

        int lineNo = 0;

        String line = null, oldLine = null, buffer = null, oldB = null;
        RawCollationKey sk1 = new RawCollationKey(), sk2 = new RawCollationKey();
        RawCollationKey oldSk = null, newSk = sk1;

        try {
            while ((line = in.readLine()) != null) {
                lineNo++;
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                buffer = parseString(line);

                if (skipLineBecauseOfBug(buffer, skipFlags)) {
                    logln("Skipping line " + lineNo + " because of a known bug");
                    continue;
                }

                if (withSortKeys) {
                    coll.getRawCollationKey(buffer, newSk);
                }
                if (oldSk != null) {
                    boolean ok = true;
                    int skres = withSortKeys ? oldSk.compareTo(newSk) : 0;
                    int cmpres = coll.compare(oldB, buffer);
                    int cmpres2 = coll.compare(buffer, oldB);

                    if (cmpres != -cmpres2) {
                        errln(String.format(
                                "Compare result not symmetrical on line %d: "
                                        + "previous vs. current (%d) / current vs. previous (%d)",
                                lineNo, cmpres, cmpres2));
                        ok = false;
                    }

                    // TODO: Compare with normalization turned off if the input passes the FCD test.

                    if (withSortKeys && cmpres != normalizeResult(skres)) {
                        errln("Difference between coll.compare (" + cmpres + ") and sortkey compare (" + skres
                                + ") on line " + lineNo);
                        ok = false;
                    }

                    int res = cmpres;
                    if (res == 0 && !isAtLeastUCA62) {
                        // Up to UCA 6.1, the collation test files use a custom tie-breaker,
                        // comparing the raw input strings.
                        res = comparer.compare(oldB, buffer);
                        // Starting with UCA 6.2, the collation test files use the standard UCA tie-breaker,
                        // comparing the NFD versions of the input strings,
                        // which we do via setting strength=identical.
                    }
                    if (res > 0) {
                        errln("Line " + lineNo + " is not greater or equal than previous line");
                        ok = false;
                    }

                    if (!ok) {
                        errln("  Previous data line " + oldLine);
                        errln("  Current data line  " + line);
                        if (withSortKeys) {
                            errln("  Previous key: " + CollationTest.prettify(oldSk));
                            errln("  Current key:  " + CollationTest.prettify(newSk));
                        }
                    }
                }

                oldSk = newSk;
                oldB = buffer;
                oldLine = line;
                if (oldSk == sk1) {
                    newSk = sk2;
                } else {
                    newSk = sk1;
                }
            }
        } catch (Exception e) {
            errln("Unexpected exception " + e);
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
            in = null;
        }
    }
}
