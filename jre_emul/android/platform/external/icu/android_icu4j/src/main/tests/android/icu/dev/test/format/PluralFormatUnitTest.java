/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.dev.test.format;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.MessageFormat;
import android.icu.text.NumberFormat;
import android.icu.text.PluralFormat;
import android.icu.text.PluralRules;
import android.icu.text.PluralRules.PluralType;
import android.icu.text.PluralRules.SampleType;
import android.icu.util.ULocale;

/**
 * @author tschumann (Tim Schumann)
 *
 */
public class PluralFormatUnitTest extends TestFmwk {
    @Test
    public void TestConstructor() {
        // Test correct formatting of numbers.
        PluralFormat plFmts[] = new PluralFormat[10];
        plFmts[0] = new PluralFormat();
        plFmts[0].applyPattern("other{#}");
        plFmts[1] = new PluralFormat(PluralRules.DEFAULT);
        plFmts[1].applyPattern("other{#}");
        plFmts[2] = new PluralFormat(PluralRules.DEFAULT, "other{#}");
        plFmts[3] = new PluralFormat("other{#}");
        plFmts[4] = new PluralFormat(ULocale.getDefault());
        plFmts[4].applyPattern("other{#}");
        plFmts[5] = new PluralFormat(ULocale.getDefault(), PluralRules.DEFAULT);
        plFmts[5].applyPattern("other{#}");
        plFmts[6] = new PluralFormat(ULocale.getDefault(),
                PluralRules.DEFAULT,
                "other{#}");
        plFmts[7] = new PluralFormat(ULocale.getDefault(), "other{#}");

        // Constructors with Java Locale
        plFmts[8] = new PluralFormat(Locale.getDefault());
        plFmts[8].applyPattern("other{#}");
        plFmts[9] = new PluralFormat(Locale.getDefault(), PluralRules.DEFAULT);
        plFmts[9].applyPattern("other{#}");

        // These plural formats should produce the same output as a
        // NumberFormat for the default locale.
        NumberFormat numberFmt = NumberFormat.getInstance(ULocale.getDefault());
        for (int n = 1; n < 13; n++) {
            String result = numberFmt.format(n);
            for (int k = 0; k < plFmts.length; ++k) {
                TestFmwk.assertEquals("PluralFormat's output is not as expected",
                        result, plFmts[k].format(n));
            }
        }
        // Test some bigger numbers.
        // Coverage: Use the format(Object, ...) version.
        StringBuffer sb = new StringBuffer();
        FieldPosition ignore = new FieldPosition(-1);
        for (int n = 100; n < 113; n++) {
            String result = numberFmt.format(n*n);
            for (int k = 0; k < plFmts.length; ++k) {
                sb.delete(0, sb.length());
                String pfResult = plFmts[k].format(Long.valueOf(n*n), sb, ignore).toString();
                TestFmwk.assertEquals("PluralFormat's output is not as expected", result, pfResult);
            }
        }
    }

    public void TestEquals() {
        // There is neither clone() nor a copy constructor.
        PluralFormat de_fee_1 = new PluralFormat(ULocale.GERMAN, PluralType.CARDINAL, "other{fee}");
        PluralFormat de_fee_2 = new PluralFormat(ULocale.GERMAN, PluralType.CARDINAL, "other{fee}");
        PluralFormat de_fi = new PluralFormat(ULocale.GERMAN, PluralType.CARDINAL, "other{fi}");
        PluralFormat fr_fee = new PluralFormat(ULocale.FRENCH, PluralType.CARDINAL, "other{fee}");
        assertTrue("different de_fee objects", de_fee_1 != de_fee_2);
        assertTrue("equal de_fee objects", de_fee_1.equals(de_fee_2));
        assertFalse("different pattern strings", de_fee_1.equals(de_fi));
        assertFalse("different locales", de_fee_1.equals(fr_fee));
    }

    public void TestApplyPatternAndFormat() {
        // Create rules for testing.
        PluralRules oddAndEven =  PluralRules.createRules("odd: n mod 2 is 1");
        {
            // Test full specified case for testing RuleSet
            PluralFormat plfOddAndEven = new PluralFormat(oddAndEven);
            plfOddAndEven.applyPattern("odd{# is odd.} other{# is even.}");

            // Test fall back to other.
            PluralFormat plfOddOrEven = new PluralFormat(oddAndEven);
            plfOddOrEven.applyPattern("other{# is odd or even.}");

            NumberFormat numberFormat =
                    NumberFormat.getInstance(ULocale.getDefault());
            for (int i = 0; i < 22; ++i) {
                assertEquals("Fallback to other gave wrong results",
                        numberFormat.format(i) + " is odd or even.",
                        plfOddOrEven.format(i));
                assertEquals("Fully specified PluralFormat gave wrong results",
                        numberFormat.format(i) + ((i%2 == 1) ?  " is odd."
                                :  " is even."),
                                plfOddAndEven.format(i));
            }

            // ICU 4.8 does not check for duplicate keywords any more.
            PluralFormat pf = new PluralFormat(ULocale.ENGLISH, oddAndEven,
                    "odd{foo} odd{bar} other{foobar}");
            assertEquals("should use first occurrence of the 'odd' keyword", "foo", pf.format(1));
            pf.applyPattern("odd{foo} other{bar} other{foobar}");
            assertEquals("should use first occurrence of the 'other' keyword", "bar", pf.format(2));
            // This sees the first "other" before calling the PluralSelector which then selects "other".
            pf.applyPattern("other{foo} odd{bar} other{foobar}");
            assertEquals("should use first occurrence of the 'other' keyword", "foo", pf.format(2));
        }
        // omit other keyword.
        try {
            PluralFormat plFmt = new PluralFormat(oddAndEven);
            plFmt.applyPattern("odd{foo}");
            errln("Not defining plural case other should result in an " +
                    "exception but did not.");
        }catch (IllegalArgumentException e){}

        // ICU 4.8 does not check for unknown keywords any more.
        {
            PluralFormat pf = new PluralFormat(ULocale.ENGLISH, oddAndEven, "otto{foo} other{bar}");
            assertEquals("should ignore unknown keywords", "bar", pf.format(1));
        }

        // Test invalid keyword.
        try {
            PluralFormat plFmt = new PluralFormat(oddAndEven);
            plFmt.applyPattern("*odd{foo} other{bar}");
            errln("Defining a message for an invalid keyword should result in " +
                    "an exception but did not.");
        }catch (IllegalArgumentException e){}

        // Test invalid syntax
        //   -- comma between keyword{message} clauses
        //   -- space in keywords
        //   -- keyword{message1}{message2}
        try {
            PluralFormat plFmt = new PluralFormat(oddAndEven);
            plFmt.applyPattern("odd{foo},other{bar}");
            errln("Separating keyword{message} items with other characters " +
                    "than space should provoke an exception but did not.");
        }catch (IllegalArgumentException e){}
        try {
            PluralFormat plFmt = new PluralFormat(oddAndEven);
            plFmt.applyPattern("od d{foo} other{bar}");
            errln("Spaces inside keywords should provoke an exception but " +
                    "did not.");
        }catch (IllegalArgumentException e){}
        try {
            PluralFormat plFmt = new PluralFormat(oddAndEven);
            plFmt.applyPattern("odd{foo}{foobar}other{foo}");
            errln("Defining multiple messages after a keyword should provoke " +
                    "an exception but did not.");
        }catch (IllegalArgumentException e){}

        // Check that nested format is preserved.
        {
            PluralFormat plFmt = new PluralFormat(oddAndEven);
            plFmt.applyPattern("odd{The number {0, number, #.#0} is odd.}" +
                    "other{The number {0, number, #.#0} is even.}");
            for (int i = 1; i < 3; ++i) {
                assertEquals("format did not preserve a nested format string.",
                        ((i % 2 == 1) ?
                                "The number {0, number, #.#0} is odd."
                                : "The number {0, number, #.#0} is even."),
                                plFmt.format(i));
            }

        }
        // Check that a pound sign in curly braces is preserved.
        {
            PluralFormat plFmt = new PluralFormat(oddAndEven);
            plFmt.applyPattern("odd{The number {1,number,#} is odd.}" +
                    "other{The number {2,number,#} is even.}");
            for (int i = 1; i < 3; ++i) {
                assertEquals("format did not preserve # inside curly braces.",
                        ((i % 2 == 1) ? "The number {1,number,#} is odd."
                                : "The number {2,number,#} is even."),
                                plFmt.format(i));
            }

        }
    }


    @Test
    public void TestSamples() {
        Map<ULocale,Set<ULocale>> same = new LinkedHashMap();
        for (ULocale locale : PluralRules.getAvailableULocales()) {
            ULocale otherLocale = PluralRules.getFunctionalEquivalent(locale, null);
            Set<ULocale> others = same.get(otherLocale);
            if (others == null) same.put(otherLocale, others = new LinkedHashSet());
            others.add(locale);
            continue;
        }
        for (ULocale locale0 : same.keySet()) {
            PluralRules rules = PluralRules.forLocale(locale0);
            String localeName = locale0.toString().length() == 0 ? "root" : locale0.toString();
            logln(localeName + "\t=\t" + same.get(locale0));
            logln(localeName + "\ttoString\t" + rules.toString());
            Set<String> keywords = rules.getKeywords();
            for (String keyword : keywords) {
                Collection<Double> list = rules.getSamples(keyword);
                if (list.size() == 0) {
                    // if there aren't any integer samples, get the decimal ones.
                    list = rules.getSamples(keyword, SampleType.DECIMAL);
                }

                if (list == null || list.size() == 0) {
                    errln("Empty list for " + localeName + " : " + keyword);
                } else {
                    logln("\t" + localeName + " : " + keyword + " ; " + list);
                }
            }
        }
    }

    @Test
    public void TestSetLocale() {
        // Create rules for testing.
        PluralRules oddAndEven = PluralRules.createRules("odd__: n mod 2 is 1");

        PluralFormat plFmt = new PluralFormat(oddAndEven);
        plFmt.applyPattern("odd__{odd} other{even}");
        plFmt.setLocale(ULocale.ENGLISH);

        // Check that pattern gets deleted.
        NumberFormat nrFmt = NumberFormat.getInstance(ULocale.ENGLISH);
        assertEquals("pattern was not resetted by setLocale() call.",
                nrFmt.format(5),
                plFmt.format(5));

        // Check that rules got updated.
        plFmt.applyPattern("odd__{odd} other{even}");
        assertEquals("SetLocale should reset rules but did not.", "even", plFmt.format(1));

        plFmt.applyPattern("one{one} other{not one}");
        for (int i = 0; i < 20; ++i) {
            assertEquals("Wrong ruleset loaded by setLocale()",
                    ((i==1) ? "one" : "not one"),
                    plFmt.format(i));
        }
    }

    @Test
    public void TestParse() {
        PluralFormat plFmt = new PluralFormat("other{test}");
        try {
            plFmt.parse("test", new ParsePosition(0));
            errln("parse() should throw an UnsupportedOperationException but " +
                    "did not");
        } catch (UnsupportedOperationException e) {
        }

        plFmt = new PluralFormat("other{test}");
        try {
            plFmt.parseObject("test", new ParsePosition(0));
            errln("parse() should throw an UnsupportedOperationException but " +
                    "did not");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void TestPattern() {
        Object[] args = { "acme", null };

        {
            // ICU 4.8 PluralFormat does not trim() its pattern any more.
            // None of the other *Format classes do.
            String pat = "  one {one ''widget} other {# widgets}  ";
            PluralFormat pf = new PluralFormat(pat);
            assertEquals("should not trim() the pattern", pat, pf.toPattern());
        }

        MessageFormat pfmt = new MessageFormat("The disk ''{0}'' contains {1, plural,  one {one ''''{1, number, #.0}'''' widget} other {# widgets}}.");
        logln("");
        for (int i = 0; i < 3; ++i) {
            args[1] = new Integer(i);
            logln(pfmt.format(args));
        }
        /* ICU 4.8 returns null instead of a choice/plural/select Format object
         * (because it does not create an object for any "complex" argument).
        PluralFormat pf = (PluralFormat)pfmt.getFormatsByArgumentIndex()[1];
        logln(pf.toPattern());
         */
        logln(pfmt.toPattern());
        MessageFormat pfmt2 = new MessageFormat(pfmt.toPattern());
        assertEquals("message formats are equal", pfmt, pfmt2);
    }

    @Test
    public void TestExtendedPluralFormat() {
        String[] targets = {
                "There are no widgets.",
                "There is one widget.",
                "There is a bling widget and one other widget.",
                "There is a bling widget and 2 other widgets.",
                "There is a bling widget and 3 other widgets.",
                "Widgets, five (5-1=4) there be.",
                "There is a bling widget and 5 other widgets.",
                "There is a bling widget and 6 other widgets.",
        };
        String pluralStyle =
                "offset:1.0 "
                        + "=0 {There are no widgets.} "
                        + "=1.0 {There is one widget.} "
                        + "=5 {Widgets, five (5-1=#) there be.} "
                        + "one {There is a bling widget and one other widget.} "
                        + "other {There is a bling widget and # other widgets.}";
        PluralFormat pf = new PluralFormat(ULocale.ENGLISH, pluralStyle);
        MessageFormat mf = new MessageFormat("{0,plural," + pluralStyle + "}", ULocale.ENGLISH);
        Integer args[] = new Integer[1];
        for (int i = 0; i <= 7; ++i) {
            String result = pf.format(i);
            assertEquals("PluralFormat.format(value " + i + ")", targets[i], result);
            args[0] = i;
            result = mf.format(args);
            assertEquals("MessageFormat.format(value " + i + ")", targets[i], result);
        }

        // Try explicit values after keywords.
        pf.applyPattern("other{zz}other{yy}one{xx}one{ww}=1{vv}=1{uu}");
        assertEquals("should find first matching *explicit* value", "vv", pf.format(1));
    }

    @Test
    public void TestExtendedPluralFormatParsing() {
        String[] failures = {
                "offset:1..0 =0 {Foo}",
                "offset:1.0 {Foo}",
                "=0= {Foo}",
                "=0 {Foo} =0.0 {Bar}",
                " = {Foo}",
        };
        for (String fmt : failures) {
            try {
                new PluralFormat(fmt);
                fail("expected exception when parsing '" + fmt + "'");
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }

    @Test
    public void TestOrdinalFormat() {
        String pattern = "one{#st file}two{#nd file}few{#rd file}other{#th file}";
        PluralFormat pf = new PluralFormat(ULocale.ENGLISH, PluralType.ORDINAL, pattern);
        assertEquals("PluralFormat.format(321)", "321st file", pf.format(321));
        assertEquals("PluralFormat.format(22)", "22nd file", pf.format(22));
        assertEquals("PluralFormat.format(3)", "3rd file", pf.format(3));

        // Code coverage: Use the other new-for-PluralType constructor as well.
        pf = new PluralFormat(ULocale.ENGLISH, PluralType.ORDINAL);
        pf.applyPattern(pattern);
        assertEquals("PluralFormat.format(456)", "456th file", pf.format(456));
        assertEquals("PluralFormat.format(111)", "111th file", pf.format(111));

        // Code coverage: Use Locale not ULocale.
        pf = new PluralFormat(Locale.ENGLISH, PluralType.ORDINAL);
        pf.applyPattern(pattern);
        assertEquals("PluralFormat.format(456)", "456th file", pf.format(456));
        assertEquals("PluralFormat.format(111)", "111th file", pf.format(111));
    }

    @Test
    public void TestDecimals() {
        // Simple number replacement.
        PluralFormat pf = new PluralFormat(ULocale.ENGLISH, "one{one meter}other{# meters}");
        assertEquals("simple format(1)", "one meter", pf.format(1));
        assertEquals("simple format(1.5)", "1.5 meters", pf.format(1.5));
        PluralFormat pf2 = new PluralFormat(ULocale.ENGLISH,
                "offset:1 one{another meter}other{another # meters}");
        pf2.setNumberFormat(new DecimalFormat("0.0", new DecimalFormatSymbols(ULocale.ENGLISH)));
        assertEquals("offset-decimals format(1)", "another 0.0 meters", pf2.format(1));
        assertEquals("offset-decimals format(2)", "another 1.0 meters", pf2.format(2));
        assertEquals("offset-decimals format(2.5)", "another 1.5 meters", pf2.format(2.5));
    }
    
    @Test
    public void TestNegative() {
        PluralFormat pluralFormat = new PluralFormat(ULocale.ENGLISH, "one{# foot}other{# feet}");
        String actual = pluralFormat.format(-3);
        assertEquals(pluralFormat.toString(), "-3 feet", actual);
    }
}
