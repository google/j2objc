/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2001-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

/**
 * Port From:   ICU4C v1.8.1 : rbbi : RBBIAPITest
 * Source File: $ICU4CRoot/source/test/intltest/rbbiapts.cpp
 **/

package android.icu.dev.test.rbbi;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.BreakIterator;
import android.icu.text.RuleBasedBreakIterator;
import android.icu.util.ULocale;

/**
 * API Test the RuleBasedBreakIterator class
 */
public class RBBIAPITest extends android.icu.dev.test.TestFmwk {
    /**
     * Tests clone() and equals() methods of RuleBasedBreakIterator
     **/
    @Test
    public void TestCloneEquals() {
        RuleBasedBreakIterator bi1 = (RuleBasedBreakIterator) BreakIterator.getCharacterInstance(Locale.getDefault());
        RuleBasedBreakIterator biequal = (RuleBasedBreakIterator) BreakIterator.getCharacterInstance(Locale.getDefault());
        RuleBasedBreakIterator bi3 = (RuleBasedBreakIterator) BreakIterator.getCharacterInstance(Locale.getDefault());
        RuleBasedBreakIterator bi2 = (RuleBasedBreakIterator) BreakIterator.getWordInstance(Locale.getDefault());

        String testString = "Testing word break iterators's clone() and equals()";
        bi1.setText(testString);
        bi2.setText(testString);
        biequal.setText(testString);

        bi3.setText("hello");
        logln("Testing equals()");
        logln("Testing == and !=");
        if (!bi1.equals(biequal) || bi1.equals(bi2) || bi1.equals(bi3))
            errln("ERROR:1 RBBI's == and !- operator failed.");
        if (bi2.equals(biequal) || bi2.equals(bi1) || biequal.equals(bi3))
            errln("ERROR:2 RBBI's == and != operator  failed.");
        logln("Testing clone()");
        RuleBasedBreakIterator bi1clone = (RuleBasedBreakIterator) bi1.clone();
        RuleBasedBreakIterator bi2clone = (RuleBasedBreakIterator) bi2.clone();
        if (!bi1clone.equals(bi1)
            || !bi1clone.equals(biequal)
            || bi1clone.equals(bi3)
            || bi1clone.equals(bi2))
            errln("ERROR:1 RBBI's clone() method failed");

        if (bi2clone.equals(bi1)
            || bi2clone.equals(biequal)
            || bi2clone.equals(bi3)
            || !bi2clone.equals(bi2))
            errln("ERROR:2 RBBI's clone() method failed");

        if (!bi1.getText().equals(bi1clone.getText())
            || !bi2clone.getText().equals(bi2.getText())
            || bi2clone.equals(bi1clone))
            errln("ERROR: RBBI's clone() method failed");
    }

    /**
     * Tests toString() method of RuleBasedBreakIterator
     **/
    @Test
    public void TestToString() {
        RuleBasedBreakIterator bi1 = (RuleBasedBreakIterator) BreakIterator.getCharacterInstance(Locale.getDefault());
        RuleBasedBreakIterator bi2 = (RuleBasedBreakIterator) BreakIterator.getWordInstance(Locale.getDefault());
        logln("Testing toString()");
        bi1.setText("Hello there");
        RuleBasedBreakIterator bi3 = (RuleBasedBreakIterator) bi1.clone();
        String temp = bi1.toString();
        String temp2 = bi2.toString();
        String temp3 = bi3.toString();
        if (temp2.equals(temp3) || temp.equals(temp2) || !temp.equals(temp3))
            errln("ERROR: error in toString() method");
    }

    /**
     * Tests the method hashCode() of RuleBasedBreakIterator
     **/
    @Test
    public void TestHashCode() {
        RuleBasedBreakIterator bi1 = (RuleBasedBreakIterator) BreakIterator.getCharacterInstance(Locale.getDefault());
        RuleBasedBreakIterator bi3 = (RuleBasedBreakIterator) BreakIterator.getCharacterInstance(Locale.getDefault());
        RuleBasedBreakIterator bi2 = (RuleBasedBreakIterator) BreakIterator.getWordInstance(Locale.getDefault());
        logln("Testing hashCode()");
        bi1.setText("Hash code");
        bi2.setText("Hash code");
        bi3.setText("Hash code");
        RuleBasedBreakIterator bi1clone = (RuleBasedBreakIterator) bi1.clone();
        RuleBasedBreakIterator bi2clone = (RuleBasedBreakIterator) bi2.clone();
        if (bi1.hashCode() != bi1clone.hashCode()
            || bi1.hashCode() != bi3.hashCode()
            || bi1clone.hashCode() != bi3.hashCode()
            || bi2.hashCode() != bi2clone.hashCode())
            errln("ERROR: identical objects have different hashcodes");

        if (bi1.hashCode() == bi2.hashCode()
            || bi2.hashCode() == bi3.hashCode()
            || bi1clone.hashCode() == bi2clone.hashCode()
            || bi1clone.hashCode() == bi2.hashCode())
            errln("ERROR: different objects have same hashcodes");
    }

    /**
      * Tests the methods getText() and setText() of RuleBasedBreakIterator
      **/
    @Test
    public void TestGetSetText() {
        logln("Testing getText setText ");
        String str1 = "first string.";
        String str2 = "Second string.";
        //RuleBasedBreakIterator charIter1 = (RuleBasedBreakIterator) BreakIterator.getCharacterInstance(Locale.getDefault());
        RuleBasedBreakIterator wordIter1 = (RuleBasedBreakIterator) BreakIterator.getWordInstance(Locale.getDefault());
        CharacterIterator text1 = new StringCharacterIterator(str1);
        //CharacterIterator text1Clone = (CharacterIterator) text1.clone();
        //CharacterIterator text2 = new StringCharacterIterator(str2);
        wordIter1.setText(str1);
        if (!wordIter1.getText().equals(text1))
            errln("ERROR:1 error in setText or getText ");
        if (wordIter1.current() != 0)
            errln("ERROR:1 setText did not set the iteration position to the beginning of the text, it is"
                   + wordIter1.current() + "\n");
        wordIter1.next(2);
        wordIter1.setText(str2);
        if (wordIter1.current() != 0)
            errln("ERROR:2 setText did not reset the iteration position to the beginning of the text, it is"
                    + wordIter1.current() + "\n");
        //ICU4J has remove the method adoptText
        /*
        charIter1.adoptText(text1Clone);
        if (wordIter1.getText() == charIter1.getText()
            || wordIter1.getText() != text2
            || charIter1.getText() != text1)
            errln((UnicodeString) "ERROR:2 error is getText or setText()");

        RuleBasedBreakIterator rb = (RuleBasedBreakIterator) wordIter1.clone();
        rb.adoptText(text1);
        if (rb.getText() != text1)
            errln((UnicodeString) "ERROR:1 error in adoptText ");
        rb.adoptText(text2);
        if (rb.getText() != text2)
            errln((UnicodeString) "ERROR:2 error in adoptText ");
        */
    }

    /**
      * Testing the methods first(), next(), next(int) and following() of RuleBasedBreakIterator
      *   TODO:  Most of this test should be retired, rule behavior is much better covered by
      *          TestExtended, which is also easier to understand and maintain.
      **/
    @Test
    public void TestFirstNextFollowing() {
        int p, q;
        String testString = "This is a word break. Isn't it? 2.25";
        logln("Testing first() and next(), following() with custom rules");
        logln("testing word iterator - string :- \"" + testString + "\"\n");
        RuleBasedBreakIterator wordIter1 = (RuleBasedBreakIterator) BreakIterator.getWordInstance(Locale.getDefault());
        wordIter1.setText(testString);
        p = wordIter1.first();
        if (p != 0)
            errln("ERROR: first() returned" + p + "instead of 0");
        q = wordIter1.next(9);
        doTest(testString, p, q, 20, "This is a word break");
        p = q;
        q = wordIter1.next();
        doTest(testString, p, q, 21, ".");
        p = q;
        q = wordIter1.next(3);
        doTest(testString, p, q, 28, " Isn't ");
        p = q;
        q = wordIter1.next(2);
        doTest(testString, p, q, 31, "it?");
        q = wordIter1.following(2);
        doTest(testString, 2, q, 4, "is");
        q = wordIter1.following(22);
        doTest(testString, 22, q, 27, "Isn't");
        wordIter1.last();
        p = wordIter1.next();
        q = wordIter1.following(wordIter1.last());
        if (p != BreakIterator.DONE || q != BreakIterator.DONE)
            errln("ERROR: next()/following() at last position returned #"
                    + p + " and " + q + " instead of" + testString.length() + "\n");
        RuleBasedBreakIterator charIter1 = (RuleBasedBreakIterator) BreakIterator.getCharacterInstance(Locale.getDefault());
        testString = "Write hindi here. ";
        logln("testing char iter - string:- \"" + testString + "\"");
        charIter1.setText(testString);
        p = charIter1.first();
        if (p != 0)
            errln("ERROR: first() returned" + p + "instead of 0");
        q = charIter1.next();
        doTest(testString, p, q, 1, "W");
        p = q;
        q = charIter1.next(4);
        doTest(testString, p, q, 5, "rite");
        p = q;
        q = charIter1.next(12);
        doTest(testString, p, q, 17, " hindi here.");
        p = q;
        q = charIter1.next(-6);
        doTest(testString, p, q, 11, " here.");
        p = q;
        q = charIter1.next(6);
        doTest(testString, p, q, 17, " here.");
        p = charIter1.following(charIter1.last());
        q = charIter1.next(charIter1.last());
        if (p != BreakIterator.DONE || q != BreakIterator.DONE)
            errln("ERROR: following()/next() at last position returned #"
                    + p + " and " + q + " instead of" + testString.length());
        testString = "Hello! how are you? I'am fine. Thankyou. How are you doing? This  costs $20,00,000.";
        RuleBasedBreakIterator sentIter1 = (RuleBasedBreakIterator) BreakIterator.getSentenceInstance(Locale.getDefault());
        logln("testing sentence iter - String:- \"" + testString + "\"");
        sentIter1.setText(testString);
        p = sentIter1.first();
        if (p != 0)
            errln("ERROR: first() returned" + p + "instead of 0");
        q = sentIter1.next();
        doTest(testString, p, q, 7, "Hello! ");
        p = q;
        q = sentIter1.next(2);
        doTest(testString, p, q, 31, "how are you? I'am fine. ");
        p = q;
        q = sentIter1.next(-2);
        doTest(testString, p, q, 7, "how are you? I'am fine. ");
        p = q;
        q = sentIter1.next(4);
        doTest(testString, p, q, 60, "how are you? I'am fine. Thankyou. How are you doing? ");
        p = q;
        q = sentIter1.next();
        doTest(testString, p, q, 83, "This  costs $20,00,000.");
        q = sentIter1.following(1);
        doTest(testString, 1, q, 7, "ello! ");
        q = sentIter1.following(10);
        doTest(testString, 10, q, 20, " are you? ");
        q = sentIter1.following(20);
        doTest(testString, 20, q, 31, "I'am fine. ");
        p = sentIter1.following(sentIter1.last());
        q = sentIter1.next(sentIter1.last());
        if (p != BreakIterator.DONE || q != BreakIterator.DONE)
            errln("ERROR: following()/next() at last position returned #"
                    + p + " and " + q + " instead of" + testString.length());
        testString = "Hello! how\r\n (are)\r you? I'am fine- Thankyou. foo\u00a0bar How, are, you? This, costs $20,00,000.";
        logln("(UnicodeString)testing line iter - String:- \"" + testString + "\"");
        RuleBasedBreakIterator lineIter1 = (RuleBasedBreakIterator) BreakIterator.getLineInstance(Locale.getDefault());
        lineIter1.setText(testString);
        p = lineIter1.first();
        if (p != 0)
            errln("ERROR: first() returned" + p + "instead of 0");
        q = lineIter1.next();
        doTest(testString, p, q, 7, "Hello! ");
        p = q;
        p = q;
        q = lineIter1.next(4);
        doTest(testString, p, q, 20, "how\r\n (are)\r ");
        p = q;
        q = lineIter1.next(-4);
        doTest(testString, p, q, 7, "how\r\n (are)\r ");
        p = q;
        q = lineIter1.next(6);
        doTest(testString, p, q, 30, "how\r\n (are)\r you? I'am ");
        p = q;
        q = lineIter1.next();
        doTest(testString, p, q, 36, "fine- ");
        p = q;
        q = lineIter1.next(2);
        doTest(testString, p, q, 54, "Thankyou. foo\u00a0bar ");
        q = lineIter1.following(60);
        doTest(testString, 60, q, 64, "re, ");
        q = lineIter1.following(1);
        doTest(testString, 1, q, 7, "ello! ");
        q = lineIter1.following(10);
        doTest(testString, 10, q, 12, "\r\n");
        q = lineIter1.following(20);
        doTest(testString, 20, q, 25, "you? ");
        p = lineIter1.following(lineIter1.last());
        q = lineIter1.next(lineIter1.last());
        if (p != BreakIterator.DONE || q != BreakIterator.DONE)
            errln("ERROR: following()/next() at last position returned #"
                    + p + " and " + q + " instead of" + testString.length());
    }

    /**
     * Testing the methods lastt(), previous(), and preceding() of RuleBasedBreakIterator
     **/
    @Test
    public void TestLastPreviousPreceding() {
        int p, q;
        String testString = "This is a word break. Isn't it? 2.25 dollars";
        logln("Testing last(),previous(), preceding() with custom rules");
        logln("testing word iteration for string \"" + testString + "\"");
        RuleBasedBreakIterator wordIter1 = (RuleBasedBreakIterator) BreakIterator.getWordInstance(Locale.getDefault());
        wordIter1.setText(testString);
        p = wordIter1.last();
        if (p != testString.length()) {
            errln("ERROR: first() returned" + p + "instead of" + testString.length());
        }
        q = wordIter1.previous();
        doTest(testString, p, q, 37, "dollars");
        p = q;
        q = wordIter1.previous();
        doTest(testString, p, q, 36, " ");
        q = wordIter1.preceding(25);
        doTest(testString, 25, q, 22, "Isn");
        p = q;
        q = wordIter1.previous();
        doTest(testString, p, q, 21, " ");
        q = wordIter1.preceding(20);
        doTest(testString, 20, q, 15, "break");
        p = wordIter1.preceding(wordIter1.first());
        if (p != BreakIterator.DONE)
            errln("ERROR: preceding()  at starting position returned #" + p + " instead of 0");
        testString = "Hello! how are you? I'am fine. Thankyou. How are you doing? This  costs $20,00,000.";
        logln("testing sentence iter - String:- \"" + testString + "\"");
        RuleBasedBreakIterator sentIter1 = (RuleBasedBreakIterator) BreakIterator.getSentenceInstance(Locale.getDefault());
        sentIter1.setText(testString);
        p = sentIter1.last();
        if (p != testString.length())
            errln("ERROR: last() returned" + p + "instead of " + testString.length());
        q = sentIter1.previous();
        doTest(testString, p, q, 60, "This  costs $20,00,000.");
        p = q;
        q = sentIter1.previous();
        doTest(testString, p, q, 41, "How are you doing? ");
        q = sentIter1.preceding(40);
        doTest(testString, 40, q, 31, "Thankyou.");
        q = sentIter1.preceding(25);
        doTest(testString, 25, q, 20, "I'am ");
        sentIter1.first();
        p = sentIter1.previous();
        q = sentIter1.preceding(sentIter1.first());
        if (p != BreakIterator.DONE || q != BreakIterator.DONE)
            errln("ERROR: previous()/preceding() at starting position returned #"
                    + p + " and " + q + " instead of 0\n");
        testString = "Hello! how are you? I'am fine. Thankyou. How are you doing? This\n costs $20,00,000.";
        logln("testing line iter - String:- \"" + testString + "\"");
        RuleBasedBreakIterator lineIter1 = (RuleBasedBreakIterator) BreakIterator.getLineInstance(Locale.getDefault());
        lineIter1.setText(testString);
        p = lineIter1.last();
        if (p != testString.length())
            errln("ERROR: last() returned" + p + "instead of " + testString.length());
        q = lineIter1.previous();
        doTest(testString, p, q, 72, "$20,00,000.");
        p = q;
        q = lineIter1.previous();
        doTest(testString, p, q, 66, "costs ");
        q = lineIter1.preceding(40);
        doTest(testString, 40, q, 31, "Thankyou.");
        q = lineIter1.preceding(25);
        doTest(testString, 25, q, 20, "I'am ");
        lineIter1.first();
        p = lineIter1.previous();
        q = lineIter1.preceding(sentIter1.first());
        if (p != BreakIterator.DONE || q != BreakIterator.DONE)
            errln("ERROR: previous()/preceding() at starting position returned #"
                    + p + " and " + q + " instead of 0\n");
    }

    /**
     * Tests the method IsBoundary() of RuleBasedBreakIterator
     **/
    @Test
    public void TestIsBoundary() {
        String testString1 = "Write here. \u092d\u0301\u0930\u0924 \u0938\u0941\u0902\u0926\u0930 a\u0301u";
        RuleBasedBreakIterator charIter1 = (RuleBasedBreakIterator) BreakIterator.getCharacterInstance(Locale.getDefault());
        charIter1.setText(testString1);
        int bounds1[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 20, 21, 22, 23, 25, 26};
        doBoundaryTest(charIter1, testString1, bounds1);
        RuleBasedBreakIterator wordIter2 = (RuleBasedBreakIterator) BreakIterator.getWordInstance(Locale.getDefault());
        wordIter2.setText(testString1);
        int bounds2[] = {0, 5, 6, 10, 11, 12, 16, 17, 22, 23, 26};
        doBoundaryTest(wordIter2, testString1, bounds2);
    }

    /**
     *  Tests the rule status return value constants
     */
    @Test
    public void TestRuleStatus() {
        BreakIterator bi = BreakIterator.getWordInstance(ULocale.ENGLISH);

        bi.setText("# ");
        assertEquals(null, bi.next(), 1);
        assertTrue(null, bi.getRuleStatus() >= RuleBasedBreakIterator.WORD_NONE);
        assertTrue(null, bi.getRuleStatus() < RuleBasedBreakIterator.WORD_NONE_LIMIT);

        bi.setText("3 ");
        assertEquals(null, bi.next(), 1);
        assertTrue(null, bi.getRuleStatus() >= RuleBasedBreakIterator.WORD_NUMBER);
        assertTrue(null, bi.getRuleStatus() < RuleBasedBreakIterator.WORD_NUMBER_LIMIT);

        bi.setText("a ");
        assertEquals(null, bi.next(), 1);
        assertTrue(null, bi.getRuleStatus() >= RuleBasedBreakIterator.WORD_LETTER );
        assertTrue(null, bi.getRuleStatus() < RuleBasedBreakIterator.WORD_LETTER_LIMIT);


        bi.setText("イ  ");
        assertEquals(null, bi.next(), 1);
        assertTrue(null, bi.getRuleStatus() >= RuleBasedBreakIterator.WORD_KANA );
        // TODO: ticket #10261, Kana is not returning the correct status.
        // assertTrue(null, bi.getRuleStatus() < RuleBasedBreakIterator.WORD_KANA_LIMIT);
        // System.out.println("\n" + bi.getRuleStatus());

        bi.setText("退 ");
        assertEquals(null, bi.next(), 1);
        assertTrue(null, bi.getRuleStatus() >= RuleBasedBreakIterator.WORD_IDEO );
        assertTrue(null, bi.getRuleStatus() < RuleBasedBreakIterator.WORD_IDEO_LIMIT);
    }

    /**
     *  Tests the rule dump debug function.
     */
    @Test
    public void TestRuledump() {
        RuleBasedBreakIterator bi = (RuleBasedBreakIterator)BreakIterator.getCharacterInstance();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bos);
        bi.dump(out);
        assertTrue(null, bos.size() > 100);
    }

    //---------------------------------------------
    //Internal subroutines
    //---------------------------------------------

    /* Internal subroutine used by TestIsBoundary() */
    private void doBoundaryTest(BreakIterator bi, String text, int[] boundaries) {
        logln("testIsBoundary():");
        int p = 0;
        boolean isB;
        for (int i = 0; i < text.length(); i++) {
            isB = bi.isBoundary(i);
            logln("bi.isBoundary(" + i + ") -> " + isB);
            if (i == boundaries[p]) {
                if (!isB)
                    errln("Wrong result from isBoundary() for " + i + ": expected true, got false");
                p++;
            } else {
                if (isB)
                    errln("Wrong result from isBoundary() for " + i + ": expected false, got true");
            }
        }
    }

    /*Internal subroutine used for comparision of expected and acquired results */
    private void doTest(String testString, int start, int gotoffset, int expectedOffset, String expectedString) {
        String selected;
        String expected = expectedString;
        if (gotoffset != expectedOffset)
            errln("ERROR:****returned #" + gotoffset + " instead of #" + expectedOffset);
        if (start <= gotoffset) {
            selected = testString.substring(start, gotoffset);
        } else {
            selected = testString.substring(gotoffset, start);
        }
        if (!selected.equals(expected))
            errln("ERROR:****selected \"" + selected + "\" instead of \"" + expected + "\"");
        else
            logln("****selected \"" + selected + "\"");
    }

    @Test
    public void testGetTitleInstance() {
        BreakIterator bi = BreakIterator.getTitleInstance(new Locale("en", "CA"));
        TestFmwk.assertNotEquals("Title instance break iterator not correctly instantiated", bi.first(), null);
        bi.setText("Here is some Text");
        TestFmwk.assertEquals("Title instance break iterator not correctly instantiated", bi.first(), 0);
    }
}
