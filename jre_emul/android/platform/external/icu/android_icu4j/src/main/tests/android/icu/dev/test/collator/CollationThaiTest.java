/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2002-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

/** 
 * Port From:   ICU4C v2.1 : collate/CollationRegressionTest
 * Source File: $ICU4CRoot/source/test/intltest/regcoll.cpp
 **/
 
package android.icu.dev.test.collator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.text.CollationElementIterator;
import android.icu.text.CollationKey;
import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;

public class CollationThaiTest extends TestFmwk {
    
    final int MAX_FAILURES_TO_SHOW = -1;
    
    /**
     * Odd corner conditions taken from "How to Sort Thai Without Rewriting Sort",
     * by Doug Cooper, http://seasrc.th.net/paper/thaisort.zip
     */
    @Test
    public void TestCornerCases() {
        String TESTS[] = {
            // Shorter words precede longer
            "\u0e01",                               "<",    "\u0e01\u0e01",
    
            // Tone marks are considered after letters (i.e. are primary ignorable)
            "\u0e01\u0e32",                        "<",    "\u0e01\u0e49\u0e32",
    
            // ditto for other over-marks
            "\u0e01\u0e32",                        "<",    "\u0e01\u0e32\u0e4c",
    
            // commonly used mark-in-context order.
            // In effect, marks are sorted after each syllable.
            "\u0e01\u0e32\u0e01\u0e49\u0e32",   "<",    "\u0e01\u0e48\u0e32\u0e01\u0e49\u0e32",
    
            // Hyphens and other punctuation follow whitespace but come before letters
            "\u0e01\u0e32",                        "=",    "\u0e01\u0e32-",
            "\u0e01\u0e32-",                       "<",    "\u0e01\u0e32\u0e01\u0e32",
    
            // Doubler follows an indentical word without the doubler
            "\u0e01\u0e32",                        "=",    "\u0e01\u0e32\u0e46",
            "\u0e01\u0e32\u0e46",                 "<",    "\u0e01\u0e32\u0e01\u0e32",
    
            // \u0e45 after either \u0e24 or \u0e26 is treated as a single
            // combining character, similar to "c < ch" in traditional spanish.
            // TODO: beef up this case
            "\u0e24\u0e29\u0e35",                 "<",    "\u0e24\u0e45\u0e29\u0e35",
            "\u0e26\u0e29\u0e35",                 "<",    "\u0e26\u0e45\u0e29\u0e35",
    
            // Vowels reorder, should compare \u0e2d and \u0e34
            "\u0e40\u0e01\u0e2d",                 "<",    "\u0e40\u0e01\u0e34",
    
            // Tones are compared after the rest of the word (e.g. primary ignorable)
            "\u0e01\u0e32\u0e01\u0e48\u0e32",   "<",    "\u0e01\u0e49\u0e32\u0e01\u0e32",
    
            // Periods are ignored entirely
            "\u0e01.\u0e01.",                      "<",    "\u0e01\u0e32",
        };
        
        RuleBasedCollator coll = null;
        try {
            coll = getThaiCollator();
        } catch (Exception e) {
            warnln("could not construct Thai collator");
            return;
        }
        compareArray(coll, TESTS); 
    }
    
    void compareArray(RuleBasedCollator c, String[] tests) {
        for (int i = 0; i < tests.length; i += 3) {
            int expect = 0;
            if (tests[i+1].equals("<")) {
                expect = -1;
            } else if (tests[i+1].equals(">")) {
                expect = 1;
            } else if (tests[i+1].equals("=")) {
                expect = 0;
            } else {
                // expect = Integer.decode(tests[i+1]).intValue();
                errln("Error: unknown operator " + tests[i+1]);
                return;
            }
            String s1 = tests[i];
            String s2 = tests[i+2];
            CollationTest.doTest(this, c, s1, s2, expect);
        }
    }
    
    int sign(int i ) {
        if (i < 0) return -1;
        if (i > 0) return 1;
        return 0;
    }
    
    /**
     * Read the external dictionary file, which is already in proper
     * sorted order, and confirm that the collator compares each line as
     * preceding the following line.
     */
    @Test
    public void TestDictionary() {
        RuleBasedCollator coll = null;
        try {
            coll = getThaiCollator();
        } catch (Exception e) {
            warnln("could not construct Thai collator");
            return;
        }

        // Read in a dictionary of Thai words
        int line = 0;
        int failed = 0;
        int wordCount = 0;
        BufferedReader in = null;
        try {
            String fileName = "riwords.txt";
            in = TestUtil.getDataReader(fileName, "UTF-8");

            //
            // Loop through each word in the dictionary and compare it to the previous
            // word. They should be in sorted order.
            //
            String lastWord = "";
            String word = in.readLine();
            while (word != null) {
                line++;

                // Skip comments and blank lines
                if (word.length() == 0 || word.charAt(0) == 0x23) {
                    word = in.readLine();
                    continue;
                }

                // Show the first 8 words being compared, so we can see what's happening
                ++wordCount;
                if (wordCount <= 8) {
                    logln("Word " + wordCount + ": " + word);
                }

                if (lastWord.length() > 0) {
                    // CollationTest.doTest isn't really set up to handle situations where
                    // the result can be equal or greater than the previous, so have to skip for now.
                    // Not a big deal, since we're still testing to make sure everything sorts out
                    // right, just not looking at the colation keys in detail...
                    // CollationTest.doTest(this, coll, lastWord, word, -1);
                    int result = coll.compare(lastWord, word);

                    if (result > 0) {
                        failed++;
                        if (MAX_FAILURES_TO_SHOW < 0 || failed <= MAX_FAILURES_TO_SHOW) {
                            String msg = "--------------------------------------------\n" + line + " compare("
                                    + lastWord + ", " + word + ") returned " + result + ", expected -1\n";
                            CollationKey k1, k2;
                            k1 = coll.getCollationKey(lastWord);
                            k2 = coll.getCollationKey(word);
                            msg += "key1: " + CollationTest.prettify(k1) + "\n" + "key2: " + CollationTest.prettify(k2);
                            errln(msg);
                        }
                    }
                }
                lastWord = word;
                word = in.readLine();
            }
        } catch (IOException e) {
            errln("IOException " + e.getMessage());
        } finally {
            if (in == null) {
                errln("Error: could not open test file. Aborting test.");
                return;
            } else {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (failed != 0) {
            if (failed > MAX_FAILURES_TO_SHOW) {
                errln("Too many failures; only the first " +
                      MAX_FAILURES_TO_SHOW + " failures were shown");
            }
            errln("Summary: " + failed + " of " + (line - 1) +
                  " comparisons failed");
        }
    
        logln("Words checked: " + wordCount);
    }
    
    @Test
    public void TestInvalidThai() 
    {
        String tests[] = { "\u0E44\u0E01\u0E44\u0E01",
                           "\u0E44\u0E01\u0E01\u0E44",
                           "\u0E01\u0E44\u0E01\u0E44",
                           "\u0E01\u0E01\u0E44\u0E44",
                           "\u0E44\u0E44\u0E01\u0E01",
                           "\u0E01\u0E44\u0E44\u0E01",
                         };
     
        RuleBasedCollator collator;
        StrCmp comparator;
        try {
            collator = getThaiCollator();
            comparator = new StrCmp();
        } catch (Exception e) {
            warnln("could not construct Thai collator");
            return;
        }
        
        Arrays.sort(tests, comparator);
     
        for (int i = 0; i < tests.length; i ++)
        {
            for (int j = i + 1; j < tests.length; j ++) {
                if (collator.compare(tests[i], tests[j]) > 0) {
                    // inconsistency ordering found!
                    errln("Inconsistent ordering between strings " + i 
                          + " and " + j);
                }
            }
            CollationElementIterator iterator 
                = collator.getCollationElementIterator(tests[i]);
            CollationTest.backAndForth(this, iterator);
        }
    }
    
    @Test
    public void TestReordering() 
    {
        String tests[] = {
            "\u0E41c\u0301",      "=", "\u0E41\u0107", // composition
            "\u0E41\uD835\uDFCE", "<", "\u0E41\uD835\uDFCF", // supplementaries
            "\u0E41\uD834\uDD5F", "=", "\u0E41\uD834\uDD58\uD834\uDD65", // supplementary composition decomps to supplementary
            "\u0E41\uD87E\uDC02", "=", "\u0E41\u4E41", // supplementary composition decomps to BMP
            "\u0E41\u0301",       "=", "\u0E41\u0301", // unsafe (just checking backwards iteration)
            "\u0E41\u0301\u0316", "=", "\u0E41\u0316\u0301",

            "abc\u0E41c\u0301",      "=", "abc\u0E41\u0107", // composition
            "abc\u0E41\uD834\uDC00", "<", "abc\u0E41\uD834\uDC01", // supplementaries
            "abc\u0E41\uD834\uDD5F", "=", "abc\u0E41\uD834\uDD58\uD834\uDD65", // supplementary composition decomps to supplementary
            "abc\u0E41\uD87E\uDC02", "=", "abc\u0E41\u4E41", // supplementary composition decomps to BMP
            "abc\u0E41\u0301",       "=", "abc\u0E41\u0301", // unsafe (just checking backwards iteration)
            "abc\u0E41\u0301\u0316", "=", "abc\u0E41\u0316\u0301",

            "\u0E41c\u0301abc",      "=", "\u0E41\u0107abc", // composition
            "\u0E41\uD834\uDC00abc", "<", "\u0E41\uD834\uDC01abc", // supplementaries
            "\u0E41\uD834\uDD5Fabc", "=", "\u0E41\uD834\uDD58\uD834\uDD65abc", // supplementary composition decomps to supplementary
            "\u0E41\uD87E\uDC02abc", "=", "\u0E41\u4E41abc", // supplementary composition decomps to BMP
            "\u0E41\u0301abc",       "=", "\u0E41\u0301abc", // unsafe (just checking backwards iteration)
            "\u0E41\u0301\u0316abc", "=", "\u0E41\u0316\u0301abc",

            "abc\u0E41c\u0301abc",      "=", "abc\u0E41\u0107abc", // composition
            "abc\u0E41\uD834\uDC00abc", "<", "abc\u0E41\uD834\uDC01abc", // supplementaries
            "abc\u0E41\uD834\uDD5Fabc", "=", "abc\u0E41\uD834\uDD58\uD834\uDD65abc", // supplementary composition decomps to supplementary
            "abc\u0E41\uD87E\uDC02abc", "=", "abc\u0E41\u4E41abc", // supplementary composition decomps to BMP
            "abc\u0E41\u0301abc",       "=", "abc\u0E41\u0301abc", // unsafe (just checking backwards iteration)
            "abc\u0E41\u0301\u0316abc", "=", "abc\u0E41\u0316\u0301abc",
        };

        RuleBasedCollator collator;
        try {
            collator = (RuleBasedCollator)getThaiCollator();
        } catch (Exception e) {
            warnln("could not construct Thai collator");
            return;
        }
        compareArray(collator, tests);
    
        String rule = "& c < ab";
        String testcontraction[] = { "\u0E41ab", ">", "\u0E41c"};
        try {
            collator = new RuleBasedCollator(rule);
        } catch (Exception e) {
            errln("Error: could not construct collator with rule " + rule);
            return;
        }
        compareArray(collator, testcontraction);
    }

    // private inner class -------------------------------------------------
    
    private static final class StrCmp implements Comparator<String> 
    {
        public int compare(String string1, String string2) 
        {
            return collator.compare(string1, string2);
        }
        
        StrCmp() throws Exception
        {
            collator = getThaiCollator();
        }
        
        Collator collator;
    }
    
    // private data members ------------------------------------------------
    
    private static RuleBasedCollator m_collator_;
    
    // private methods -----------------------------------------------------
    
    private static RuleBasedCollator getThaiCollator() throws Exception
    {
        if (m_collator_ == null) {
            m_collator_ = (RuleBasedCollator)Collator.getInstance(
                                                new Locale("th", "TH", ""));
        }
        return m_collator_;
    }
}
