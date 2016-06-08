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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

public class DecimalFormatSymbolsTest extends junit.framework.TestCase {

    // http://code.google.com/p/android/issues/detail?id=14495
    public void testSerialization() throws Exception {
        DecimalFormatSymbols originalDfs = DecimalFormatSymbols.getInstance(Locale.GERMANY);

        // Serialize...
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(originalDfs);
        byte[] bytes = out.toByteArray();

        // Deserialize...
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        DecimalFormatSymbols deserializedDfs = (DecimalFormatSymbols) in.readObject();
        assertEquals(-1, in.read());

        // The two objects should claim to be equal.
        assertEquals(originalDfs, deserializedDfs);
    }

//    // https://code.google.com/p/android/issues/detail?id=79925
//    public void testSetSameCurrency() throws Exception {
//        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
//        dfs.setCurrency(Currency.getInstance("USD"));
//        assertEquals("$", dfs.getCurrencySymbol());
//        dfs.setCurrencySymbol("poop");
//        assertEquals("poop", dfs.getCurrencySymbol());
//        dfs.setCurrency(Currency.getInstance("USD"));
//        assertEquals("$", dfs.getCurrencySymbol());
//    }

    public void testSetNulInternationalCurrencySymbol() throws Exception {
        Currency usd = Currency.getInstance("USD");

        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        dfs.setCurrency(usd);
        assertEquals(usd, dfs.getCurrency());
        assertEquals("$", dfs.getCurrencySymbol());
        assertEquals("USD", dfs.getInternationalCurrencySymbol());

        // Setting the international currency symbol to null sets the currency to null too,
        // but not the currency symbol.
        dfs.setInternationalCurrencySymbol(null);
        assertEquals(null, dfs.getCurrency());
        assertEquals("$", dfs.getCurrencySymbol());
        assertEquals(null, dfs.getInternationalCurrencySymbol());
    }

    // https://code.google.com/p/android/issues/detail?id=170718
    public void testSerializationOfMultiCharNegativeAndPercentage() throws Exception {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.forLanguageTag("ar-AR"));
        // TODO(user): Investigate.
        // assertTrue(dfs.getMinusSignString().length() > 1);
        // assertTrue(dfs.getPercentString().length() > 1);

        // Serialize...
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(dfs);
        byte[] bytes = out.toByteArray();

        // Deserialize...
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        DecimalFormatSymbols deserializedDfs = (DecimalFormatSymbols) in.readObject();
        assertEquals(-1, in.read());

        assertEquals(dfs.getMinusSign(), deserializedDfs.getMinusSign());
        assertEquals(dfs.getPercent(), deserializedDfs.getPercent());
    }

//    // http://b/18785260
//    public void testMultiCharMinusSignAndPercentage() {
//        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.forLanguageTag("ar-AR"));
//
//        assertEquals('٪', dfs.getPercent());
//        assertEquals('-', dfs.getMinusSign());
//    }


//    /**
//     * This class exists to allow the test to access the protected methods
//     * getIcuDecimalFormatSymbols and fromIcuInstance on the real DecimalFormatSymbols class.
//     */
//    private static class DFSForTests extends DecimalFormatSymbols {
//        public DFSForTests(Locale locale) {
//            super(locale);
//        }
//
//        @Override
//        public android.icu.text.DecimalFormatSymbols getIcuDecimalFormatSymbols() {
//            return super.getIcuDecimalFormatSymbols();
//        }
//
//        protected static DecimalFormatSymbols fromIcuInstance(
//                android.icu.text.DecimalFormatSymbols dfs) {
//            return DecimalFormatSymbols.fromIcuInstance(dfs);
//        }
//    }
//
//    public void compareDfs(DecimalFormatSymbols dfs,
//                           android.icu.text.DecimalFormatSymbols icuSymb) {
//        // Check currency code is the same because ICU returns its own currency class.
//        assertEquals(dfs.getCurrency().getCurrencyCode(), icuSymb.getCurrency().getCurrencyCode());
//        assertEquals(dfs.getCurrencySymbol(), icuSymb.getCurrencySymbol());
//        assertEquals(dfs.getDecimalSeparator(), icuSymb.getDecimalSeparator());
//        assertEquals(dfs.getDigit(), icuSymb.getDigit());
//        assertEquals(dfs.getExponentSeparator(), icuSymb.getExponentSeparator());
//        assertEquals(dfs.getGroupingSeparator(), icuSymb.getGroupingSeparator());
//        assertEquals(dfs.getInfinity(), icuSymb.getInfinity());
//        assertEquals(dfs.getInternationalCurrencySymbol(),
//                icuSymb.getInternationalCurrencySymbol());
//        assertEquals(dfs.getMinusSign(), icuSymb.getMinusSign());
//        assertEquals(dfs.getMonetaryDecimalSeparator(), icuSymb.getMonetaryDecimalSeparator());
//        assertEquals(dfs.getPatternSeparator(), icuSymb.getPatternSeparator());
//        assertEquals(dfs.getPercent(), icuSymb.getPercent());
//        assertEquals(dfs.getPerMill(), icuSymb.getPerMill());
//        assertEquals(dfs.getZeroDigit(), icuSymb.getZeroDigit());
//    }
//
//    // Test the methods to convert to and from the ICU DecimalFormatSymbols
//    public void testToIcuDecimalFormatSymbols() {
//        DFSForTests dfs = new DFSForTests(Locale.US);
//        android.icu.text.DecimalFormatSymbols icuSymb = dfs.getIcuDecimalFormatSymbols();
//        compareDfs(dfs, icuSymb);
//    }
//
//    public void testFromIcuDecimalFormatSymbols() {
//        android.icu.text.DecimalFormatSymbols icuSymb = new android.icu.text.DecimalFormatSymbols();
//        DecimalFormatSymbols dfs = DFSForTests.fromIcuInstance(icuSymb);
//        compareDfs(dfs, icuSymb);
//    }

}
