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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import tests.support.Support_Locale;

public class DecimalFormatTest extends junit.framework.TestCase {
    public void test_exponentSeparator() throws Exception {
        DecimalFormat df = new DecimalFormat("0E0");
        assertEquals("1E4", df.format(12345.));

        DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
        dfs.setExponentSeparator("-useless-api-");
        df.setDecimalFormatSymbols(dfs);
        assertEquals("1-useless-api-4", df.format(12345.));
    }

    public void test_setMaximumFractionDigitsAffectsRoundingMode() throws Exception {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        df.setMaximumFractionDigits(0);
        df.setRoundingMode(RoundingMode.HALF_UP);
        assertEquals("-0", df.format(-0.2));
        df.setMaximumFractionDigits(1);
        assertEquals("-0.2", df.format(-0.2));
    }

    // Android fails this test, truncating to 127 digits, as does iOS.
//    public void test_setMaximumIntegerDigits() throws Exception {
//        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
//        numberFormat.setGroupingUsed(false);
//        numberFormat.setMinimumIntegerDigits(400);
//        // The RI's documentation suggests that the int should be formatted to 309 characters --
//        // a magic number they don't explain -- but the BigInteger should be formatted to the 400
//        // characters we asked for. In practice, the RI uses 309 in both cases.
//        assertEquals(309, numberFormat.format(123).length());
//        assertEquals(309, numberFormat.format(BigInteger.valueOf(123)).length());
//    }

    // TODO(tball): enable when field position iterating on iOS is supported.
//    public void testBigDecimalBug1897917() {
//        // For example. the BigDecimal 0.17 formatted in PercentInstance is 0% instead of 17%:
//        NumberFormat pf = NumberFormat.getPercentInstance();
//        assertEquals("17%", pf.format(BigDecimal.valueOf(0.17)));
//
//        // Test long decimal formatted in PercentInstance with various fractions.
//        String longDec = "11.2345678901234567890123456789012345678901234567890";
//        BigDecimal bd = new BigDecimal(longDec);
//        assertBigDecimalWithFraction(bd, "1,123.46%", 2);
//        assertBigDecimalWithFraction(bd, "1,123.45678901%", 8);
//        assertBigDecimalWithFraction(bd, "1,123.4567890123%", 10);
//        assertBigDecimalWithFraction(bd, "1,123.45678901234567890123%", 20);
//        assertBigDecimalWithFraction(bd, "1,123.456789012345678901234567890123%", 30);
//
//        // Test trailing zeros.
//        assertDecFmtWithMultiplierAndFraction("3333.33333333", 3, 4, "10,000");
//        assertDecFmtWithMultiplierAndFraction("3333.33333333", -3, 4, "-10,000");
//        assertDecFmtWithMultiplierAndFraction("0.00333333", 3, 4, "0.01");
//
//        assertDecFmtWithMultiplierAndFractionByLocale("3330000000000000000000000000000000", 3, 4,
//                    Locale.US, "9,990,000,000,000,000,000,000,000,000,000,000");
//
//        Locale inLocale = new Locale("en", "IN");
//        if (Support_Locale.isLocaleAvailable(inLocale)) {
//            assertDecFmtWithMultiplierAndFractionByLocale("3330000000000000000000000000000000", 3, 4,
//                    inLocale, "9,99,00,00,00,00,00,00,00,00,00,00,00,00,00,00,000");
//        }
//    }

    // TODO(tball): enable when field position iterating on iOS is supported.
//    public void testBigDecimalTestBigIntWithMultiplier() {
//        // Big integer tests.
//        assertDecFmtWithMultiplierAndFractionByLocale("123456789012345", 10, 0,
//                Locale.US, "1,234,567,890,123,450");
//        assertDecFmtWithMultiplierAndFractionByLocale("12345678901234567890", 10, 0,
//                Locale.US, "123,456,789,012,345,678,900");
//        assertDecFmtWithMultiplierAndFractionByLocale("98765432109876543210987654321", 10, 0,
//                Locale.US, "987,654,321,098,765,432,109,876,543,210");
//
//        assertDecFmtWithMultiplierAndFractionByLocale("123456789012345", -10, 0,
//                Locale.US, "-1,234,567,890,123,450");
//        assertDecFmtWithMultiplierAndFractionByLocale("12345678901234567890", -10, 0,
//                Locale.US, "-123,456,789,012,345,678,900");
//        assertDecFmtWithMultiplierAndFractionByLocale("98765432109876543210987654321", -10, 0,
//                Locale.US, "-987,654,321,098,765,432,109,876,543,210");
//
//        Locale inLocale = new Locale("en", "IN");
//        if (Support_Locale.isLocaleAvailable(inLocale)) {
//            assertDecFmtWithMultiplierAndFractionByLocale("123456789012345", 10, 0,
//                    inLocale, "1,23,45,67,89,01,23,450");
//            assertDecFmtWithMultiplierAndFractionByLocale("12345678901234567890", 10, 0,
//                    inLocale, "12,34,56,78,90,12,34,56,78,900");
//            assertDecFmtWithMultiplierAndFractionByLocale("98765432109876543210987654321", 10, 0,
//                    inLocale, "9,87,65,43,21,09,87,65,43,21,09,87,65,43,210");
//
//            assertDecFmtWithMultiplierAndFractionByLocale("123456789012345", -10, 0,
//                    inLocale, "-1,23,45,67,89,01,23,450");
//            assertDecFmtWithMultiplierAndFractionByLocale("12345678901234567890", -10, 0,
//                    inLocale, "-12,34,56,78,90,12,34,56,78,900");
//            assertDecFmtWithMultiplierAndFractionByLocale("98765432109876543210987654321", -10, 0,
//                    inLocale, "-9,87,65,43,21,09,87,65,43,21,09,87,65,43,210");
//        }
//    }

    // TODO(tball): enable when field position iterating on iOS is supported.
//    public void testBigDecimalICUConsistency() {
//        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
//        df.setMaximumFractionDigits(2);
//        df.setMultiplier(2);
//        assertEquals(df.format(BigDecimal.valueOf(0.16)),
//        df.format(BigDecimal.valueOf(0.16).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(0.0293)),
//        df.format(BigDecimal.valueOf(0.0293).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(0.006)),
//        df.format(BigDecimal.valueOf(0.006).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(0.00283)),
//        df.format(BigDecimal.valueOf(0.00283).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(1.60)),
//        df.format(BigDecimal.valueOf(1.60).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(15)),
//        df.format(BigDecimal.valueOf(15).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(170)),
//        df.format(BigDecimal.valueOf(170).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(234.56)),
//        df.format(BigDecimal.valueOf(234.56).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(0)),
//        df.format(BigDecimal.valueOf(0).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(-1)),
//        df.format(BigDecimal.valueOf(-1).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(-10000)),
//        df.format(BigDecimal.valueOf(-10000).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(-0.001)),
//        df.format(BigDecimal.valueOf(-0.001).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(1234567890.1234567)),
//        df.format(BigDecimal.valueOf(1234567890.1234567).doubleValue()));
//        assertEquals(df.format(BigDecimal.valueOf(1.234567E100)),
//        df.format(BigDecimal.valueOf(1.234567E100).doubleValue()));
//    }

    private void assertBigDecimalWithFraction(BigDecimal bd, String expectedResult, int fraction) {
        NumberFormat pf = NumberFormat.getPercentInstance();
        pf.setMaximumFractionDigits(fraction);
        assertEquals(expectedResult, pf.format(bd));
    }

    private void assertDecFmtWithMultiplierAndFraction(String value, int multiplier, int fraction, String expectedResult) {
        DecimalFormat df = (DecimalFormat)NumberFormat.getInstance();
        df.setMultiplier(multiplier);
        df.setMaximumFractionDigits(fraction);
        BigDecimal d = new BigDecimal(value);
        assertEquals(expectedResult, df.format(d));
    }

    private void assertDecFmtWithMultiplierAndFractionByLocale(String value, int multiplier, int fraction, Locale locale, String expectedResult) {
        DecimalFormat df = (DecimalFormat)NumberFormat.getIntegerInstance(locale);
        df.setMultiplier(multiplier);
        df.setMaximumFractionDigits(fraction);
        BigDecimal d = new BigDecimal(value);
        assertEquals(expectedResult, df.format(d));
    }
}
