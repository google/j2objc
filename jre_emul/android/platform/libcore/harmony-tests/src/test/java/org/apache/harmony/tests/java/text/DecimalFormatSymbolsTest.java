/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.ServiceConfigurationError;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

public class DecimalFormatSymbolsTest extends TestCase {

    DecimalFormatSymbols dfs;

    DecimalFormatSymbols dfsUS;

    /**
     * @tests java.text.DecimalFormatSymbols#DecimalFormatSymbols()
     */
    public void test_Constructor() {
        // Test for method java.text.DecimalFormatSymbols()
        // Used in tests
    }

    /**
     * @tests java.text.DecimalFormatSymbols#DecimalFormatSymbols(java.util.Locale)
     */
    public void test_ConstructorLjava_util_Locale() {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en",
                "us"));
        assertEquals("Returned incorrect symbols", '%', dfs.getPercent());
    }


    /**
     * @tests java.text.DecimalFormatSymbols#getAvailableLocales()
     */
    public void test_getAvailableLocales_no_provider() throws Exception {
        Locale[] locales = DecimalFormatSymbols.getAvailableLocales();
        assertNotNull(locales);
        // must contain Locale.US
        boolean flag = false;
        for (Locale locale : locales) {
            if (locale.equals(Locale.US)) {
                flag = true;
                break;
            }
        }
        assertTrue(flag);
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getInstance()
     */
    public void test_getInstance() {
        assertEquals(new DecimalFormatSymbols(), DecimalFormatSymbols.getInstance());
        assertEquals(new DecimalFormatSymbols(Locale.getDefault()),
                DecimalFormatSymbols.getInstance());

        assertNotSame(DecimalFormatSymbols.getInstance(), DecimalFormatSymbols.getInstance());
    }

    public void test_getInstanceLjava_util_Locale() {
        try {
            DecimalFormatSymbols.getInstance(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(new DecimalFormatSymbols(Locale.GERMANY), DecimalFormatSymbols.getInstance(Locale.GERMANY));

        Locale locale = new Locale("not exist language", "not exist country");
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
        assertNotNull(symbols);
    }

    /**
     * @tests java.text.DecimalFormatSymbols#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        assertTrue("Equal objects returned false", dfs.equals(dfs.clone()));
        dfs.setDigit('B');
        assertTrue("Un-Equal objects returned true", !dfs
                .equals(new DecimalFormatSymbols()));

    }

    /**
     * @tests java.text.DecimalFormatSymbols#getCurrency()
     */
    public void test_getCurrency() {
        Currency currency = Currency.getInstance("USD");
        assertEquals("Returned incorrect currency",
                dfsUS.getCurrency(), currency);

        Currency currK = Currency.getInstance("KRW");
        Currency currX = Currency.getInstance("XXX");
        Currency currE = Currency.getInstance("EUR");
        // Currency currF = Currency.getInstance("FRF");

        DecimalFormatSymbols dfs1 = new DecimalFormatSymbols(new Locale("ko",
                "KR"));
        assertTrue("Test1: Returned incorrect currency",
                dfs1.getCurrency() == currK);
        assertEquals("Test1: Returned incorrect currencySymbol", "\u20a9", dfs1
                .getCurrencySymbol());
        assertEquals("Test1: Returned incorrect intlCurrencySymbol", "KRW",
                dfs1.getInternationalCurrencySymbol());

        dfs1 = new DecimalFormatSymbols(new Locale("", "KR"));
        assertTrue("Test2: Returned incorrect currency",
                dfs1.getCurrency() == currK);
        assertEquals("Test2: Returned incorrect currencySymbol", "\u20a9", dfs1
                .getCurrencySymbol());
        assertEquals("Test2: Returned incorrect intlCurrencySymbol", "KRW",
                dfs1.getInternationalCurrencySymbol());

        dfs1 = new DecimalFormatSymbols(new Locale("ko", ""));
        assertTrue("Test3: Returned incorrect currency",
                dfs1.getCurrency() == currX);
        assertEquals("Test3: Returned incorrect currencySymbol", "\u00a4", dfs1
                .getCurrencySymbol());
        assertEquals("Test3: Returned incorrect intlCurrencySymbol", "XXX",
                dfs1.getInternationalCurrencySymbol());

        dfs1 = new DecimalFormatSymbols(new Locale("fr", "FR"));
        assertTrue("Test4: Returned incorrect currency",
                dfs1.getCurrency() == currE);
        assertEquals("Test4: Returned incorrect currencySymbol", "\u20ac", dfs1
                .getCurrencySymbol());
        assertEquals("Test4: Returned incorrect intlCurrencySymbol", "EUR",
                dfs1.getInternationalCurrencySymbol());

        // RI fails these tests since it doesn't have the PREEURO variant
        // dfs1 = new DecimalFormatSymbols(new Locale("fr", "FR","PREEURO"));
        // assertTrue("Test5: Returned incorrect currency", dfs1.getCurrency()
        // == currF);
        // assertTrue("Test5: Returned incorrect currencySymbol",
        // dfs1.getCurrencySymbol().equals("F"));
        // assertTrue("Test5: Returned incorrect intlCurrencySymbol",
        // dfs1.getInternationalCurrencySymbol().equals("FRF"));
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getCurrencySymbol()
     */
    public void test_getCurrencySymbol() {
        assertEquals("Returned incorrect currencySymbol", "$", dfsUS
                .getCurrencySymbol());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getDecimalSeparator()
     */
    public void test_getDecimalSeparator() {
        dfs.setDecimalSeparator('*');
        assertEquals("Returned incorrect DecimalSeparator symbol", '*', dfs
                .getDecimalSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getDigit()
     */
    public void test_getDigit() {
        dfs.setDigit('*');
        assertEquals("Returned incorrect Digit symbol", '*', dfs.getDigit());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getExponentSeparator()
     */
    public void test_getExponentSeparator() {
        dfs.setExponentSeparator("EE");
        assertEquals("Returned incorrect Exponent Separator symbol", "EE", dfs
                .getExponentSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getGroupingSeparator()
     */
    public void test_getGroupingSeparator() {
        dfs.setGroupingSeparator('*');
        assertEquals("Returned incorrect GroupingSeparator symbol", '*', dfs
                .getGroupingSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getInfinity()
     */
    public void test_getInfinity() {
        dfs.setInfinity("&");
        assertTrue("Returned incorrect Infinity symbol",
                dfs.getInfinity() == "&");
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getInternationalCurrencySymbol()
     */
    public void test_getInternationalCurrencySymbol() {
        assertEquals("Returned incorrect InternationalCurrencySymbol", "USD",
                dfsUS.getInternationalCurrencySymbol());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getMinusSign()
     */
    public void test_getMinusSign() {
        dfs.setMinusSign('&');
        assertEquals("Returned incorrect MinusSign symbol", '&', dfs
                .getMinusSign());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getNaN()
     */
    public void test_getNaN() {
        dfs.setNaN("NAN!!");
        assertEquals("Returned incorrect nan symbol", "NAN!!", dfs.getNaN());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getPatternSeparator()
     */
    public void test_getPatternSeparator() {
        dfs.setPatternSeparator('X');
        assertEquals("Returned incorrect PatternSeparator symbol", 'X', dfs
                .getPatternSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getPercent()
     */
    public void test_getPercent() {
        dfs.setPercent('*');
        assertEquals("Returned incorrect Percent symbol", '*', dfs.getPercent());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getPerMill()
     */
    public void test_getPerMill() {
        dfs.setPerMill('#');
        assertEquals("Returned incorrect PerMill symbol", '#', dfs.getPerMill());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getZeroDigit()
     */
    public void test_getZeroDigit() {
        dfs.setZeroDigit('*');
        assertEquals("Returned incorrect ZeroDigit symbol", '*', dfs
                .getZeroDigit());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setCurrency(java.util.Currency)
     */
    public void test_setCurrencyLjava_util_Currency() {
        Locale locale = Locale.CANADA;
        DecimalFormatSymbols dfs = ((DecimalFormat) NumberFormat
                .getCurrencyInstance(locale)).getDecimalFormatSymbols();

        try {
            dfs.setCurrency(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }

        Currency currency = Currency.getInstance("JPY");
        dfs.setCurrency(currency);

        assertTrue("Returned incorrect currency", currency == dfs.getCurrency());
        assertEquals("Returned incorrect currency symbol", currency.getSymbol(
                locale), dfs.getCurrencySymbol());
        assertTrue("Returned incorrect international currency symbol", currency
                .getCurrencyCode().equals(dfs.getInternationalCurrencySymbol()));
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setDecimalSeparator(char)
     */
    public void test_setDecimalSeparatorC() {
        dfs.setDecimalSeparator('*');
        assertEquals("Returned incorrect DecimalSeparator symbol", '*', dfs
                .getDecimalSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setDigit(char)
     */
    public void test_setDigitC() {
        dfs.setDigit('*');
        assertEquals("Returned incorrect Digit symbol", '*', dfs.getDigit());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setExponentSeparator(String)
     */
    public void test_setExponentSeparator() {
        try {
            dfs.setExponentSeparator(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        dfs.setExponentSeparator("");
        assertEquals("Returned incorrect Exponent Separator symbol", "", dfs
                .getExponentSeparator());

        dfs.setExponentSeparator("what ever you want");
        assertEquals("Returned incorrect Exponent Separator symbol",
                "what ever you want", dfs.getExponentSeparator());

        dfs.setExponentSeparator(" E ");
        assertEquals("Returned incorrect Exponent Separator symbol", " E ", dfs
                .getExponentSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setGroupingSeparator(char)
     */
    public void test_setGroupingSeparatorC() {
        dfs.setGroupingSeparator('*');
        assertEquals("Returned incorrect GroupingSeparator symbol", '*', dfs
                .getGroupingSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setInfinity(java.lang.String)
     */
    public void test_setInfinityLjava_lang_String() {
        dfs.setInfinity("&");
        assertTrue("Returned incorrect Infinity symbol",
                dfs.getInfinity() == "&");
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setInternationalCurrencySymbol(java.lang.String)
     */
    public void test_setInternationalCurrencySymbolLjava_lang_String() {
        Locale locale = Locale.CANADA;
        DecimalFormatSymbols dfs = ((DecimalFormat) NumberFormat
                .getCurrencyInstance(locale)).getDecimalFormatSymbols();
        Currency currency = Currency.getInstance("JPY");
        dfs.setInternationalCurrencySymbol(currency.getCurrencyCode());

        assertTrue("Test1: Returned incorrect currency", currency == dfs
                .getCurrency());
        assertEquals("Test1: Returned incorrect currency symbol", currency
                .getSymbol(locale), dfs.getCurrencySymbol());
        assertTrue("Test1: Returned incorrect international currency symbol",
                currency.getCurrencyCode().equals(
                        dfs.getInternationalCurrencySymbol()));

        dfs.setInternationalCurrencySymbol("bogus");
        // RI support this legacy country code
        // assertNotNull("Test2: Returned incorrect currency", dfs.getCurrency());
        assertEquals("Test2: Returned incorrect international currency symbol",
                "bogus", dfs.getInternationalCurrencySymbol());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setMinusSign(char)
     */
    public void test_setMinusSignC() {
        dfs.setMinusSign('&');
        assertEquals("Returned incorrect MinusSign symbol", '&', dfs
                .getMinusSign());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setNaN(java.lang.String)
     */
    public void test_setNaNLjava_lang_String() {
        dfs.setNaN("NAN!!");
        assertEquals("Returned incorrect nan symbol", "NAN!!", dfs.getNaN());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setPatternSeparator(char)
     */
    public void test_setPatternSeparatorC() {
        dfs.setPatternSeparator('X');
        assertEquals("Returned incorrect PatternSeparator symbol", 'X', dfs
                .getPatternSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setPercent(char)
     */
    public void test_setPercentC() {
        dfs.setPercent('*');
        assertEquals("Returned incorrect Percent symbol", '*', dfs.getPercent());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setPerMill(char)
     */
    public void test_setPerMillC() {
        dfs.setPerMill('#');
        assertEquals("Returned incorrect PerMill symbol", '#', dfs.getPerMill());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setZeroDigit(char)
     */
    public void test_setZeroDigitC() {
        dfs.setZeroDigit('*');
        assertEquals("Set incorrect ZeroDigit symbol", '*', dfs.getZeroDigit());
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        dfs = new DecimalFormatSymbols();
        dfsUS = new DecimalFormatSymbols(new Locale("en", "us"));
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }

    // Test serialization mechanism of DecimalFormatSymbols
    public void test_serialization() throws Exception {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        Currency currency = symbols.getCurrency();
        assertNotNull(currency);

        // serialize
        ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOStream = new ObjectOutputStream(byteOStream);
        objectOStream.writeObject(symbols);

        // and deserialize
        ObjectInputStream objectIStream = new ObjectInputStream(
                new ByteArrayInputStream(byteOStream.toByteArray()));
        DecimalFormatSymbols symbolsD = (DecimalFormatSymbols) objectIStream
                .readObject();

        // The associated currency will not persist
        currency = symbolsD.getCurrency();
        assertNotNull(currency);
    }

    /**
     * Assert that Harmony can correct read an instance that was created by
     * the Java 1.5 RI. The actual values may differ on Harmony and other JREs,
     * so we only assert the values that are known to be in the serialized data.
     */
    public void test_RIHarmony_compatible() throws Exception {
        DecimalFormatSymbols dfs;
        ObjectInputStream i = null;
        try {
            i = new ObjectInputStream(getClass().getClassLoader().getResourceAsStream(
                    "serialization/org/apache/harmony/tests/java/text/DecimalFormatSymbols.ser"));
            dfs = (DecimalFormatSymbols) i.readObject();
        } finally {
            try {
                if (i != null) {
                    i.close();
                }
            } catch (Exception e) {
            }
        }
        assertDecimalFormatSymbolsRIFrance(dfs);
    }

    static void assertDecimalFormatSymbolsRIFrance(DecimalFormatSymbols dfs) {
        // Values based on Java 1.5 RI DecimalFormatSymbols for Locale.FRANCE
        /*
         * currency = [EUR]
         * currencySymbol = [€][U+20ac]
         * decimalSeparator = [,][U+002c]
         * digit = [#][U+0023]
         * groupingSeparator = [ ][U+00a0]
         * infinity = [∞][U+221e]
         * internationalCurrencySymbol = [EUR]
         * minusSign = [-][U+002d]
         * monetaryDecimalSeparator = [,][U+002c]
         * naN = [�][U+fffd]
         * patternSeparator = [;][U+003b]
         * perMill = [‰][U+2030]
         * percent = [%][U+0025]
         * zeroDigit = [0][U+0030]
         */
        assertEquals("EUR", dfs.getCurrency().getCurrencyCode());
        assertEquals("\u20AC", dfs.getCurrencySymbol());
        assertEquals(',', dfs.getDecimalSeparator());
        assertEquals('#', dfs.getDigit());
        assertEquals('\u00a0', dfs.getGroupingSeparator());
        assertEquals("\u221e", dfs.getInfinity());
        assertEquals("EUR", dfs.getInternationalCurrencySymbol());
        assertEquals('-', dfs.getMinusSign());
        assertEquals(',', dfs.getMonetaryDecimalSeparator());
        // RI's default NaN is U+FFFD, Harmony's is based on ICU
        assertEquals("\uFFFD", dfs.getNaN());
        assertEquals('\u003b', dfs.getPatternSeparator());
        assertEquals('\u2030', dfs.getPerMill());
        assertEquals('%', dfs.getPercent());
        assertEquals('0', dfs.getZeroDigit());
    }

    /**
     * @tests serialization/deserialization compatibility with RI6.
     */
    public void testSerializationCompatibility() throws Exception {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setExponentSeparator("EE");
        symbols.setNaN("NaN");
        SerializationTest.verifyGolden(this, symbols);
    }

    /**
     * @tests serialization/deserialization compatibility.
     */
    public void testSerializationSelf() throws Exception {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ITALIAN);
        SerializationTest.verifySelf(symbols);
    }
}
