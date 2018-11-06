/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.rbbi;

import java.text.CharacterIterator;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.BreakIterator;

public class BreakIteratorRegTest extends TestFmwk
{
    @Test
    public void TestRegUnreg() {
    Locale thailand_locale = new Locale("th", "TH", "");
    Locale foo_locale = new Locale("fu", "FU", "FOO");
    BreakIterator jwbi = BreakIterator.getWordInstance(Locale.JAPAN);
    BreakIterator uwbi = BreakIterator.getWordInstance(Locale.US);
    BreakIterator usbi = BreakIterator.getSentenceInstance(Locale.US);
    BreakIterator twbi = BreakIterator.getWordInstance(thailand_locale);
    BreakIterator rwbi = BreakIterator.getWordInstance(new Locale("", "", ""));

    BreakIterator sbi = (BreakIterator)usbi.clone();
    // todo: this will cause the test to fail, no way to set a breakiterator to null text so can't fix yet.
    // String text = "This is some test, by golly. Boy, they don't make tests like they used to, do they?  This here test ain't worth $2.50.  Nope.";
    // sbi.setText(text);

    assertTrue(!BreakIterator.unregister(""), "unregister before register"); // coverage

    Object key0 = BreakIterator.registerInstance((BreakIterator)twbi.clone(), foo_locale, BreakIterator.KIND_WORD);
    Object key1 = BreakIterator.registerInstance(sbi, Locale.US, BreakIterator.KIND_WORD);
    Object key2 = BreakIterator.registerInstance((BreakIterator)twbi.clone(), Locale.US, BreakIterator.KIND_WORD);
    
    {
        BreakIterator test0 = BreakIterator.getWordInstance(Locale.JAPAN);
        BreakIterator test1 = BreakIterator.getWordInstance(Locale.US);
        BreakIterator test2 = BreakIterator.getSentenceInstance(Locale.US);
        BreakIterator test3 = BreakIterator.getWordInstance(thailand_locale);
        BreakIterator test4 = BreakIterator.getWordInstance(foo_locale);

        assertEqual(test0, jwbi, "japan word == japan word");
        assertEqual(test1, twbi, "us word == thai word");
        assertEqual(test2, usbi, "us sentence == us sentence");
        assertEqual(test3, twbi, "thai word == thai word");
        assertEqual(test4, twbi, "foo word == thai word");
    }

    //Locale[] locales = BreakIterator.getAvailableLocales();
    
    assertTrue(BreakIterator.unregister(key2), "unregister us word (thai word)");
    assertTrue(!BreakIterator.unregister(key2), "unregister second time");
    boolean error = false;
    try {
        BreakIterator.unregister(null);
    }
    catch (IllegalArgumentException e) {
        error = true;
    }

    assertTrue(error, "unregister null");

    {
        CharacterIterator sci = BreakIterator.getWordInstance(Locale.US).getText();
        int len = sci.getEndIndex() - sci.getBeginIndex();
        assertEqual(len, 0, "us word text: " + getString(sci));
    }

    assertTrue(Arrays.asList(BreakIterator.getAvailableLocales()).contains(foo_locale), "foo_locale");
    assertTrue(BreakIterator.unregister(key0), "unregister foo word (thai word)");
    assertTrue(!Arrays.asList(BreakIterator.getAvailableLocales()).contains(foo_locale), "no foo_locale");
    assertEqual(BreakIterator.getWordInstance(Locale.US), usbi, "us word == us sentence");
    
    assertTrue(BreakIterator.unregister(key1), "unregister us word (us sentence)");
    {
        BreakIterator test0 = BreakIterator.getWordInstance(Locale.JAPAN);
        BreakIterator test1 = BreakIterator.getWordInstance(Locale.US);
        BreakIterator test2 = BreakIterator.getSentenceInstance(Locale.US);
        BreakIterator test3 = BreakIterator.getWordInstance(thailand_locale);
        BreakIterator test4 = BreakIterator.getWordInstance(foo_locale);

        assertEqual(test0, jwbi, "japanese word break");
        assertEqual(test1, uwbi, "us sentence-word break");
        assertEqual(test2, usbi, "us sentence break");
        assertEqual(test3, twbi, "thai word break");
        assertEqual(test4, rwbi, "root word break");

        CharacterIterator sci = test1.getText();
        int len = sci.getEndIndex() - sci.getBeginIndex();
        assertEqual(len, 0, "us sentence-word break text: " + getString(sci));
    }
    }

    private void assertEqual(Object lhs, Object rhs, String msg) {
    msg(msg, lhs.equals(rhs) ? LOG : ERR, true, true);
    }

    private void assertEqual(int lhs, int rhs, String msg) {
    msg(msg, lhs == rhs ? LOG : ERR, true, true);
    }

    private void assertTrue(boolean arg, String msg) {
    msg(msg, arg ? LOG : ERR, true, true);
    }

    private static String getString(CharacterIterator ci) {
    StringBuffer buf = new StringBuffer(ci.getEndIndex() - ci.getBeginIndex() + 2);
    buf.append("'");
    for (char c = ci.first(); c != CharacterIterator.DONE; c = ci.next()) {
        buf.append(c);
    }
    buf.append("'");
    return buf.toString();
    }
}
