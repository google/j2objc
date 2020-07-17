/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package test.j2objc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class CurrencyTest extends junit.framework.TestCase {

    private Locale originalLocale;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        originalLocale = Locale.getDefault();
    }

    @Override
    protected void tearDown() {
        Locale.setDefault(originalLocale);
    }


    /**
     * java.util.Currency#getInstance(java.util.Locale)
     */
    public void test_getInstanceLjava_util_Locale() {
        /*
         * the behaviour in all these three cases should be the same since this
         * method ignores language and variant component of the locale.
         */
        Currency c0 = Currency.getInstance("CAD");
        Currency c1 = Currency.getInstance(new Locale("en", "CA"));
        assertTrue(
                "Currency.getInstance(new Locale(\"en\",\"CA\")) isn't equal to Currency.getInstance(\"CAD\")",
                c1 == c0);
        Currency c2 = Currency.getInstance(new Locale("fr", "CA"));
        assertTrue(
                "Currency.getInstance(new Locale(\"fr\",\"CA\")) isn't equal to Currency.getInstance(\"CAD\")",
                c2 == c0);
        Currency c3 = Currency.getInstance(new Locale("", "CA"));
        assertTrue(
                "Currency.getInstance(new Locale(\"\",\"CA\")) isn't equal to Currency.getInstance(\"CAD\")",
                c3 == c0);

        c0 = Currency.getInstance("JPY");
        c1 = Currency.getInstance(new Locale("ja", "JP"));
        assertTrue(
                "Currency.getInstance(new Locale(\"ja\",\"JP\")) isn't equal to Currency.getInstance(\"JPY\")",
                c1 == c0);
        c2 = Currency.getInstance(new Locale("", "JP"));
        assertTrue(
                "Currency.getInstance(new Locale(\"\",\"JP\")) isn't equal to Currency.getInstance(\"JPY\")",
                c2 == c0);
        c3 = Currency.getInstance(new Locale("bogus", "JP"));
        assertTrue(
                "Currency.getInstance(new Locale(\"bogus\",\"JP\")) isn't equal to Currency.getInstance(\"JPY\")",
                c3 == c0);

        Locale localeGu = new Locale("gu", "IN");
        Currency cGu = Currency.getInstance(localeGu);
        Locale localeKn = new Locale("kn", "IN");
        Currency cKn = Currency.getInstance(localeKn);
        assertTrue("Currency.getInstance(Locale_" + localeGu.toString() + "))"
                + "isn't equal to " + "Currency.getInstance(Locale_"
                + localeKn.toString() + "))", cGu == cKn);

        // some teritories do not have currencies, like Antarctica
        Locale loc = new Locale("", "AQ");
        try {
            Currency curr = Currency.getInstance(loc);
            assertNull(
                    "Currency.getInstance(new Locale(\"\", \"AQ\")) did not return null",
                    curr);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException " + e);
        }

        /* These return valid currencies in iOS.
        // unsupported/legacy iso3 countries
        loc = new Locale("", "ZR");
        try {
            Currency curr = Currency.getInstance(loc);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        loc = new Locale("", "ZAR");
        try {
            Currency curr = Currency.getInstance(loc);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        loc = new Locale("", "FX");
        try {
            Currency curr = Currency.getInstance(loc);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        loc = new Locale("", "FXX");
        try {
            Currency curr = Currency.getInstance(loc);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }*/
    }

}
