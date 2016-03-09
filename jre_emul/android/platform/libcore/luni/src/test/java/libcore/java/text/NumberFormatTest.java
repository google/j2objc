/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.text;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.Locale;

public class NumberFormatTest extends junit.framework.TestCase {
    // NumberFormat.format(Object, StringBuffer, FieldPosition) guarantees it calls doubleValue for
    // custom Number subclasses.
    public void test_custom_Number_gets_longValue() throws Exception {
        class MyNumber extends Number {
            public byte byteValue() { throw new UnsupportedOperationException(); }
            public double doubleValue() { return 123; }
            public float floatValue() { throw new UnsupportedOperationException(); }
            public int intValue() { throw new UnsupportedOperationException(); }
            public long longValue() { throw new UnsupportedOperationException(); }
            public short shortValue() { throw new UnsupportedOperationException(); }
            public String toString() { throw new UnsupportedOperationException(); }
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        assertEquals("123", nf.format(new MyNumber()));
    }

    // NumberFormat.format(Object, StringBuffer, FieldPosition) guarantees it calls longValue for
    // any BigInteger with a bitLength strictly less than 64.
    public void test_small_BigInteger_gets_longValue() throws Exception {
        class MyNumberFormat extends NumberFormat {
            public StringBuffer format(double value, StringBuffer b, FieldPosition f) {
                b.append("double");
                return b;
            }
            public StringBuffer format(long value, StringBuffer b, FieldPosition f) {
                b.append("long");
                return b;
            }
            public Number parse(String string, ParsePosition p) {
                throw new UnsupportedOperationException();
            }
        }
        NumberFormat nf = new MyNumberFormat();
        assertEquals("long", nf.format(BigInteger.valueOf(Long.MAX_VALUE)));
        assertEquals("double", nf.format(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)));
        assertEquals("long", nf.format(BigInteger.valueOf(Long.MIN_VALUE)));
        assertEquals("double", nf.format(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE)));
    }

    // Formatting percentages is confusing but deliberate.
    // Ensure we don't accidentally "fix" this.
    // https://code.google.com/p/android/issues/detail?id=10333
    public void test_10333() throws Exception {
        NumberFormat nf = NumberFormat.getPercentInstance(Locale.US);
        assertEquals("15%", nf.format(0.15));
        assertEquals("1,500%", nf.format(15));
        try {
            nf.format("15");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testPercentageRounding() throws Exception {
        NumberFormat nf = NumberFormat.getPercentInstance(Locale.US);
        assertEquals("15%", nf.format(0.149));
        assertEquals("14%", nf.format(0.142));

        nf.setRoundingMode(RoundingMode.UP);
        assertEquals("15%", nf.format(0.142));

        nf.setRoundingMode(RoundingMode.DOWN);
        assertEquals("14%", nf.format(0.149));

        nf.setMaximumFractionDigits(1);
        assertEquals("14.9%", nf.format(0.149));
    }

    // https://code.google.com/p/android/issues/detail?id=62269
    public void test_62269() throws Exception {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        try {
            nf.parse(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_nullLocales() {
        try {
            NumberFormat.getInstance(null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            NumberFormat.getIntegerInstance(null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            NumberFormat.getCurrencyInstance(null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            NumberFormat.getPercentInstance(null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            NumberFormat.getNumberInstance(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    // https://code.google.com/p/android/issues/detail?id=79925\
    // When switching currency after having initialised a DecimalFormat instance to a currency,
    // the symbols are missing.
    public void test_issue79925() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        nf.setCurrency(Currency.getInstance("EUR"));
        assertEquals("€50.00", nf.format(50.0));

        DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) nf).getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("");
        ((DecimalFormat) nf).setDecimalFormatSymbols(decimalFormatSymbols);
        assertEquals("50.00", nf.format(50.0));

        nf.setCurrency(Currency.getInstance("SGD"));
        assertEquals("SGD50.00", nf.format(50.0));

        nf.setCurrency(Currency.getInstance("SGD"));
        assertEquals("SGD50.00", nf.format(50.00));

        nf.setCurrency(Currency.getInstance("USD"));
        assertEquals("$50.00", nf.format(50.0));

        nf.setCurrency(Currency.getInstance("SGD"));
        assertEquals("SGD50.00", nf.format(50.0));
    }

    // Test to ensure explicitly setting a currency symbol will overwrite the defaults.
    public void test_customCurrencySymbol() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        DecimalFormatSymbols dfs = ((DecimalFormat) nf).getDecimalFormatSymbols();
        dfs.setCurrencySymbol("SPECIAL");
        ((DecimalFormat) nf).setDecimalFormatSymbols(dfs);
        assertEquals("SPECIAL3.14", nf.format(3.14));

        // Setting the currency again should reset the symbols.
        nf.setCurrency(Currency.getInstance("USD"));
        assertEquals("$3.14", nf.format(3.14));

        // Setting it back again should work.
        dfs.setCurrencySymbol("NEW");
        ((DecimalFormat) nf).setDecimalFormatSymbols(dfs);
        assertEquals("NEW3.14", nf.format(3.14));
    }

    // Test to ensure currency formatting from specified locale works.
    public void test_currencyFromLocale() {
        // French locale formats with "," as separator and Euro symbol after a non-breaking space.
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        assertEquals("50.00 €", nf.format(50));

        // British locale uses pound sign with no spacing.
        nf = NumberFormat.getCurrencyInstance(Locale.UK);
        assertEquals("£50.00", nf.format(50));
    }

    // Test the currency symbol is correctly taken from ICU. Verifies that the fractional digits
    // are not updated because DecimalFormat.setCurrency agrees not to change it.
    public void test_setCurrency() throws Exception {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);

        // The Armenian Dram is a special case where the fractional digits are 0.
        Currency amd = Currency.getInstance("AMD");
        assertEquals(0, amd.getDefaultFractionDigits());

        // Armenian Dram ISO 4217 code.
        nf.setCurrency(amd);

        // Try and explicitly request fractional digits for the specified currency.
        nf.setMaximumFractionDigits(amd.getDefaultFractionDigits());
        assertEquals("AMD50", nf.format(50.00));

        nf = NumberFormat.getCurrencyInstance(Locale.US);

        // Euro sign.
        nf.setCurrency(Currency.getInstance("EUR"));
        assertEquals("€50.00", nf.format(50.00));

        // Japanese Yen symbol.
        nf.setCurrency(Currency.getInstance("JPY"));
        assertEquals("¥50", nf.format(50.00));

        // Swiss Franc ISO 4217 code.
        nf.setCurrency(Currency.getInstance("CHF"));
        assertEquals("CHF50.00", nf.format(50.00));
    }

    // Test the setting of locale specific patterns which have different fractional digits.
    public void test_currencyWithPatternDigits() throws Exception {
        // Japanese Yen 0 fractional digits.
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.JAPAN);
        assertEquals("¥50", nf.format(50.00));

        // Swiss Francs 2 fractional digits.
        nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("de-CH"));
        assertEquals("CHF 50.00", nf.format(50.00));
    }
}
