/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2004-2013, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.format;

import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.RuleBasedNumberFormat;
import android.icu.util.ULocale;

public class RBNFParseTest extends TestFmwk {
    @Test
    public void TestParse() {

        // these rules make no sense but behave rationally
        String[] okrules = {
            "random text",
            "%foo:bar",
            "%foo: bar",
            "0:",
            "0::",
            "%%foo:;",
            "-",
            "-1",
            "-:",
            ".",
            ".1",
            "[",
            "]",
            "[]",
            "[foo]",
            "[[]",
            "[]]",
            "[[]]",
            "[][]",
            "<",
            ">",
            "=",
            "==",
            "===",
            "=foo=",
        };

        String[] exceptrules = {
            "",
            ";",
            ";;",
            ":",
            "::",
            ":1",
            ":;",
            ":;:;",
            "<<",
            "<<<",
            "10:;9:;",
            ">>",
            ">>>",
            "10:", // formatting any value with a one's digit will fail
            "11: << x", // formating a multiple of 10 causes rollback rule to fail
            "%%foo: 0 foo; 10: =%%bar=; %%bar: 0: bar; 10: =%%foo=;",
        };

        String[][] allrules = {
            okrules,
            exceptrules,
        };

        for (int j = 0; j < allrules.length; ++j) {
            String[] tests = allrules[j];
            boolean except = tests == exceptrules;
            for (int i = 0; i < tests.length; ++i) {
                logln("----------");
                logln("rules: '" + tests[i] + "'");
                boolean caughtException = false;
                try {
                    RuleBasedNumberFormat fmt = new RuleBasedNumberFormat(tests[i], Locale.US);
                    logln("1.23: " + fmt.format(20));
                    logln("-123: " + fmt.format(-123));
                    logln(".123: " + fmt.format(.123));
                    logln(" 123: " + fmt.format(123));
                }
                catch (Exception e) {
                    if (!except) {
                        errln("Unexpected exception: " + e.getMessage());
                    } else {
                        caughtException = true;
                    }
                }
                if (except && !caughtException) {
                    errln("expected exception but didn't get one!");
                }
            }
        }
    }

    private void parseFormat(RuleBasedNumberFormat rbnf, String s, String target) {
        try {
            Number n = rbnf.parse(s);
            String t = rbnf.format(n);
            assertEquals(rbnf.getLocale(ULocale.ACTUAL_LOCALE) + ": " + s + " : " + n, target, t);
        } catch (java.text.ParseException e){
            fail("exception:" + e);
        }
    }

    private void parseList(RuleBasedNumberFormat rbnf_en, RuleBasedNumberFormat rbnf_fr, String[][] lists) {
        for (int i = 0; i < lists.length; ++i) {
            String[] list = lists[i];
            String s = list[0];
            String target_en = list[1];
            String target_fr = list[2];

            parseFormat(rbnf_en, s, target_en);
            parseFormat(rbnf_fr, s, target_fr);
        }
    }

    @Test
    public void TestLenientParse() throws Exception {
        RuleBasedNumberFormat rbnf_en, rbnf_fr;

        // TODO: this still passes, but setLenientParseMode should have no effect now.
        // Did it ever test what it was supposed to?
        rbnf_en = new RuleBasedNumberFormat(Locale.ENGLISH, RuleBasedNumberFormat.SPELLOUT);
        rbnf_en.setLenientParseMode(true);
        rbnf_fr = new RuleBasedNumberFormat(Locale.FRENCH, RuleBasedNumberFormat.SPELLOUT);
        rbnf_fr.setLenientParseMode(true);

        Number n = rbnf_en.parse("1,2 million");
        logln(n.toString());

        String[][] lists = {
            { "1,2", "twelve", "un virgule deux" },
            { "1,2 million", "twelve million", "un virgule deux" },
            { "1,2 millions", "twelve million", "un million deux cent mille" },
            { "1.2", "one point two", "douze" },
            { "1.2 million", "one million two hundred thousand", "douze" },
            { "1.2 millions", "one million two hundred thousand", "douze millions" },
        };

        Locale.setDefault(Locale.FRANCE);
        logln("Default locale:" + Locale.getDefault());
        logln("rbnf_en:" + rbnf_en.getDefaultRuleSetName());
        logln("rbnf_fr:" + rbnf_en.getDefaultRuleSetName());
        parseList(rbnf_en, rbnf_fr, lists);

        Locale.setDefault(Locale.US);
        logln("Default locale:" + Locale.getDefault());
        logln("rbnf_en:" + rbnf_en.getDefaultRuleSetName());
        logln("rbnf_fr:" + rbnf_en.getDefaultRuleSetName());
        parseList(rbnf_en, rbnf_fr, lists);
    }
}
