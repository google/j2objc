/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2008-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.format;

import java.util.Arrays;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.StandardPlural;
import android.icu.text.MeasureFormat;
import android.icu.text.MeasureFormat.FormatWidth;
import android.icu.text.PluralRanges;
import android.icu.text.PluralRules.Factory;
import android.icu.util.Currency;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.icu.util.ULocale;

/**
 * @author markdavis
 * 
 */
public class PluralRangesTest extends TestFmwk {
    @Test
    public void TestLocaleData() {
        String[][] tests = {
                {"de", "other", "one", "one"},
                {"xxx", "few", "few", "few" },
                {"de", "one", "other", "other"},
                {"de", "other", "one", "one"},
                {"de", "other", "other", "other"},
                {"ro", "one", "few", "few"},
                {"ro", "one", "other", "other"},
                {"ro", "few", "one", "few"},
        };
        for (String[] test : tests) {
            final ULocale locale = new ULocale(test[0]);
            final StandardPlural start = StandardPlural.fromString(test[1]);
            final StandardPlural end = StandardPlural.fromString(test[2]);
            final StandardPlural expected = StandardPlural.fromString(test[3]);
            final PluralRanges pluralRanges = Factory.getDefaultFactory().getPluralRanges(locale);

            StandardPlural actual = pluralRanges.get(start, end);
            assertEquals("Deriving range category", expected, actual);
        }
    }

    @Test
    public void TestRangePattern() {
        String[][] tests = {
                {"de", "SHORT", "{0}–{1}"},
                {"ja", "NARROW", "{0}～{1}"},
        };
        for (String[] test : tests) {
            ULocale ulocale = new ULocale(test[0]);
            FormatWidth width = FormatWidth.valueOf(test[1]);
            String expected = test[2];
            String formatter = MeasureFormat.getRangeFormat(ulocale, width);
            String actual = SimpleFormatterImpl.formatCompiledPattern(formatter, "{0}", "{1}");
            assertEquals("range pattern " + Arrays.asList(test), expected, actual);
        }
    }

    @Test
    public void TestFormatting() {
        Object[][] tests = {
                {0.0, 1.0, ULocale.FRANCE, FormatWidth.WIDE, MeasureUnit.FAHRENHEIT, "0–1 degré Fahrenheit"},
                {1.0, 2.0, ULocale.FRANCE, FormatWidth.WIDE, MeasureUnit.FAHRENHEIT, "1–2 degrés Fahrenheit"},
                {3.1, 4.25, ULocale.FRANCE, FormatWidth.SHORT, MeasureUnit.FAHRENHEIT, "3,1–4,25 °F"},
                {3.1, 4.25, ULocale.ENGLISH, FormatWidth.SHORT, MeasureUnit.FAHRENHEIT, "3.1–4.25°F"},
                {3.1, 4.25, ULocale.CHINESE, FormatWidth.WIDE, MeasureUnit.INCH, "3.1-4.25英寸"},
                {0.0, 1.0, ULocale.ENGLISH, FormatWidth.WIDE, MeasureUnit.INCH, "0–1 inches"},

                {0.0, 1.0, ULocale.ENGLISH, FormatWidth.NARROW, Currency.getInstance("EUR"), "€0.00–1.00"},
                {0.0, 1.0, ULocale.FRENCH, FormatWidth.NARROW, Currency.getInstance("EUR"), "0,00–1,00 €"},
                {0.0, 100.0, ULocale.FRENCH, FormatWidth.NARROW, Currency.getInstance("JPY"), "0–100\u00a0JPY"},

                {0.0, 1.0, ULocale.ENGLISH, FormatWidth.SHORT, Currency.getInstance("EUR"), "EUR0.00–1.00"},
                {0.0, 1.0, ULocale.FRENCH, FormatWidth.SHORT, Currency.getInstance("EUR"), "0,00–1,00\u00a0EUR"},
                {0.0, 100.0, ULocale.FRENCH, FormatWidth.SHORT, Currency.getInstance("JPY"), "0–100\u00a0JPY"},

                {0.0, 1.0, ULocale.ENGLISH, FormatWidth.WIDE, Currency.getInstance("EUR"), "0.00–1.00 euros"},
                {0.0, 1.0, ULocale.FRENCH, FormatWidth.WIDE, Currency.getInstance("EUR"), "0,00–1,00 euro"},
                {0.0, 2.0, ULocale.FRENCH, FormatWidth.WIDE, Currency.getInstance("EUR"), "0,00–2,00 euros"},
                {0.0, 100.0, ULocale.FRENCH, FormatWidth.WIDE, Currency.getInstance("JPY"), "0–100 yens japonais"},
        };
        int i = 0;
        for (Object[] test : tests) {
            ++i;
            double low = (Double) test[0];
            double high = (Double) test[1];
            final ULocale locale = (ULocale) test[2];
            final FormatWidth width = (FormatWidth) test[3];
            final MeasureUnit unit = (MeasureUnit) test[4];
            final Object expected = test[5];

            MeasureFormat mf = MeasureFormat.getInstance(locale, width);
            Object actual;
            try {
                actual = mf.formatMeasureRange(new Measure(low, unit), new Measure(high, unit));
            } catch (Exception e) {
                actual = e.getClass();
            }
            assertEquals(i + " Formatting unit", expected, actual);
        }
    }

    @Test
    public void TestBasic() {
        PluralRanges a = new PluralRanges();
        a.add(StandardPlural.ONE, StandardPlural.OTHER, StandardPlural.ONE);
        StandardPlural actual = a.get(StandardPlural.ONE, StandardPlural.OTHER);
        assertEquals("range", StandardPlural.ONE, actual);
        a.freeze();
        try {
            a.add(StandardPlural.ONE, StandardPlural.ONE, StandardPlural.ONE);
            errln("Failed to cause exception on frozen instance");
        } catch (UnsupportedOperationException e) {
        }
    }
}
