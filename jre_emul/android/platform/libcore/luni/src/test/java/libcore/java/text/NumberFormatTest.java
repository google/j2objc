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

    public void test_getIntegerInstance_ar() throws Exception {
        // Previous versions of android use just the positive format string (ICU4C) although now we
        // use '<positive_format>;<negative_format>' because of ICU4J denormalization.
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("ar"));
        String patternNI = ((DecimalFormat) numberFormat).toPattern();
        assertTrue("#,##0.###;-#,##0.###".equals(patternNI) || "#,##0.###".equals(patternNI));
        NumberFormat integerFormat = NumberFormat.getIntegerInstance(new Locale("ar"));
        String patternII = ((DecimalFormat) integerFormat).toPattern();
        assertTrue("#,##0;-#,##0".equals(patternII) || "#,##0".equals(patternII));
    }

    /* J2ObjC: The Arabic symbols don't appear to be available on macOS.
    public void test_numberLocalization() throws Exception {
        Locale arabic = new Locale("ar");
        NumberFormat nf = NumberFormat.getNumberInstance(arabic);
        assertEquals('\u0660', new DecimalFormatSymbols(arabic).getZeroDigit());
        assertEquals("١٬٢٣٤٬٥٦٧٬٨٩٠", nf.format(1234567890));
    }*/

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
        // J2ObjC: Was a bad test using 0.149 as input because floating point representation might
        // be less than 0.149 and round down to 14.8%.
        assertEquals("14.9%", nf.format(0.1491));
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
        assertEquals("50,00\u00a0€", nf.format(50));

        // British locale uses pound sign with no spacing.
        nf = NumberFormat.getCurrencyInstance(Locale.UK);
        assertEquals("£50.00", nf.format(50));
    }

    // Test the currency symbol is correctly taken from ICU. Verifies that the fractional digits
    // are not updated because DecimalFormat.setCurrency agrees not to change it.
    public void test_setCurrency() throws Exception {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);

        // The Japanese Yen is a special case where the fractional digits are 0.
        Currency jpy = Currency.getInstance("JPY");
        assertEquals(0, jpy.getDefaultFractionDigits());

        nf.setCurrency(jpy);
        assertEquals(2, nf.getMinimumFractionDigits());  // Check DecimalFormat has not taken the
        assertEquals(2, nf.getMaximumFractionDigits());  // currency specific fractional digits.
        assertEquals("¥50.00", nf.format(50.00));

        // Try and explicitly request fractional digits for the specified currency.
        nf.setMaximumFractionDigits(jpy.getDefaultFractionDigits());
        assertEquals("¥50", nf.format(50.00));

        nf = NumberFormat.getCurrencyInstance(Locale.US);

        // Euro sign.
        nf.setCurrency(Currency.getInstance("EUR"));
        assertEquals("€50.00", nf.format(50.00));

        // Armenian Dram symbol.
        nf.setCurrency(Currency.getInstance("AMD"));
        assertEquals("AMD50.00", nf.format(50.00));

        // Swiss Franc ISO 4217 code.
        nf.setCurrency(Currency.getInstance("CHF"));
        assertEquals("CHF50.00", nf.format(50.00));
    }

    // Test the setting of locale specific patterns which have different fractional digits.
    public void test_currencyWithPatternDigits() throws Exception {
      // Japanese Yen 0 fractional digits.
      NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.JAPAN);
      String result = nf.format(50.00);
      // Allow either full-width (0xFFE5) or regular width yen sign (0xA5).
      assertTrue(result.equals("￥50") || result.equals("¥50"));

      // Armenian Dram 0 fractional digits.
      nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("hy-AM"));
      result = nf.format(50.00);
      // Allow different versions of the ICU CLDR.
      assertTrue(result.equals("֏\u00a050")
          || result.equals("50\u00a0֏")  // iOS 12
          || result.equals("50,00 ֏"));  // macOS 10.15

      // Swiss Francs 2 fractional digits.
      nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("de-CH"));
      assertEquals("CHF\u00a050.00", nf.format(50.00));
    }

    // http://b/28893763
    public void test_setCurrency_leavesFractionDigitsUntouched() {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        format.setMinimumFractionDigits(0);
        format.setCurrency(Currency.getInstance("USD"));
        assertEquals("$10", format.format(10d));
    }
}
