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

package test.j2objc;

import com.google.j2objc.EnvironmentUtil;
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

    // Test the currency symbol is correctly taken from ICU. Verifies that the fractional digits
    // are not updated because DecimalFormat.setCurrency agrees not to change it.
    public void test_setCurrency() throws Exception {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);

        // The Armenian Dram is a special case where the fractional digits are 0.
        Currency amd = Currency.getInstance("AMD");
        assertEquals(0, amd.getDefaultFractionDigits());

        // Armenian Dram ISO 4217 code.
        nf.setCurrency(amd);
        assertEquals(2, nf.getMinimumFractionDigits());  // Check DecimalFormat has not taken the
        assertEquals(2, nf.getMaximumFractionDigits());  // currency specific fractional digits.
        assertEquals("AMD50.00", nf.format(50.00));

        // Try and explicitly request fractional digits for the specified currency.
        nf.setMaximumFractionDigits(amd.getDefaultFractionDigits());
        assertEquals("AMD50", nf.format(50.00));

        nf = NumberFormat.getCurrencyInstance(Locale.US);

        // Euro sign.
        nf.setCurrency(Currency.getInstance("EUR"));
        assertEquals("€50.00", nf.format(50.00));

        // Japanese Yen symbol.
        nf.setCurrency(Currency.getInstance("JPY"));
        assertEquals("¥50.00", nf.format(50.00));

        // Swiss Franc ISO 4217 code.
        nf.setCurrency(Currency.getInstance("CHF"));
        assertEquals("CHF50.00", nf.format(50.00));
    }

    // Test the setting of locale specific patterns which have different fractional digits.
    public void test_currencyWithPatternDigits() throws Exception {
      // Locale strings updated in macOS 10.12 to match iOS.
      if (!EnvironmentUtil.onMacOSX() || EnvironmentUtil.onMinimumOSVersion("10.12")) {
        // Japanese Yen 0 fractional digits.
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.JAPAN);
        String result = nf.format(50.00);
        // Allow either full-width (0xFFE5) or regular width yen sign (0xA5).
        assertTrue(result.equals("￥50") || result.equals("¥50"));

        // Armenian Dram 0 fractional digits.
        nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("hy-AM"));
        result = nf.format(50.00);
        // Allow different versions of the ICU CLDR.
        assertTrue(result.equals("֏\u00a050") || result.equals("50\u00a0֏"));

        // Swiss Francs 2 fractional digits.
        nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("de-CH"));
        assertEquals("CHF\u00a050.00", nf.format(50.00));
      }
    }

}
