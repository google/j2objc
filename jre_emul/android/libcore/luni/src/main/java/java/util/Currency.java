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

package java.util;

import java.io.Serializable;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

/**
 * A currency corresponding to an <a href="http://en.wikipedia.org/wiki/ISO_4217">ISO 4217</a>
 * currency code such as "EUR" or "USD".
 */
public final class Currency implements Serializable {
    private static final long serialVersionUID = -158308464356906721L;

    private static final HashMap<String, Currency> codesToCurrencies = new HashMap<String, Currency>();
    private static final HashMap<Locale, Currency> localesToCurrencies = new HashMap<Locale, Currency>();

    private final String currencyCode;

    private Currency(String currencyCode) {
        this.currencyCode = currencyCode;
        String symbol = ICU.getCurrencySymbol(Locale.US, currencyCode);
        if (symbol == null) {
            throw new IllegalArgumentException("Unsupported ISO 4217 currency code: " +
                    currencyCode);
        }
    }

    /**
     * Returns the {@code Currency} instance for the given ISO 4217 currency code.
     * @throws IllegalArgumentException
     *             if the currency code is not a supported ISO 4217 currency code.
     */
    public static Currency getInstance(String currencyCode) {
        synchronized (codesToCurrencies) {
            Currency currency = codesToCurrencies.get(currencyCode);
            if (currency == null) {
                currency = new Currency(currencyCode);
                codesToCurrencies.put(currencyCode, currency);
            }
            return currency;
        }
    }

    /**
     * Returns the {@code Currency} instance for this {@code Locale}'s country.
     * @throws IllegalArgumentException
     *             if the locale's country is not a supported ISO 3166 country.
     */
    public static Currency getInstance(Locale locale) {
        synchronized (localesToCurrencies) {
            if (locale == null) {
                throw new NullPointerException("locale == null");
            }
            Currency currency = localesToCurrencies.get(locale);
            if (currency != null) {
                return currency;
            }
            String country = locale.getCountry();
            String variant = locale.getVariant();
            if (!variant.isEmpty() && (variant.equals("EURO") || variant.equals("HK") ||
                    variant.equals("PREEURO"))) {
                country = country + "_" + variant;
            }

            String currencyCode = ICU.getCurrencyCode(country);
            if (currencyCode == null) {
                throw new IllegalArgumentException("Unsupported ISO 3166 country: " + locale);
            } else if (currencyCode.equals("XXX")) {
                return null;
            }
            Currency result = getInstance(currencyCode);
            localesToCurrencies.put(locale, result);
            return result;
        }
    }

    /**
     * Returns a set of all known currencies.
     * @since 1.7
     */
    public static Set<Currency> getAvailableCurrencies() {
        Set<Currency> result = new LinkedHashSet<Currency>();
        String[] currencyCodes = ICU.getAvailableCurrencyCodes();
        for (String currencyCode : currencyCodes) {
            result.add(Currency.getInstance(currencyCode));
        }
        return result;
    }

    /**
     * Returns this currency's ISO 4217 currency code.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Equivalent to {@code getDisplayName(Locale.getDefault())}.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     * @since 1.7
     */
    public String getDisplayName() {
        return getDisplayName(Locale.getDefault());
    }

    /**
     * Returns the localized name of this currency in the given {@code locale}.
     * Returns the ISO 4217 currency code if no localized name is available.
     * @since 1.7
     */
    public String getDisplayName(Locale locale) {
        return ICU.getCurrencyDisplayName(locale, currencyCode);
    }

    /**
     * Equivalent to {@code getSymbol(Locale.getDefault())}.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     */
    public String getSymbol() {
        return getSymbol(Locale.getDefault());
    }

    /**
     * Returns the localized currency symbol for this currency in {@code locale}.
     * That is, given "USD" and Locale.US, you'd get "$", but given "USD" and a non-US locale,
     * you'd get "US$".
     *
     * <p>If the locale only specifies a language rather than a language and a country (such as
     * {@code Locale.JAPANESE} or {new Locale("en", "")} rather than {@code Locale.JAPAN} or
     * {new Locale("en", "US")}), the ISO 4217 currency code is returned.
     *
     * <p>If there is no locale-specific currency symbol, the ISO 4217 currency code is returned.
     */
    public String getSymbol(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }
        // Check the locale first, in case the locale has the same currency.
        LocaleData localeData = LocaleData.get(locale);
        if (localeData.internationalCurrencySymbol.equals(currencyCode)) {
            return localeData.currencySymbol;
        }

        // Try ICU, and fall back to the currency code if ICU has nothing.
        String symbol = ICU.getCurrencySymbol(locale, currencyCode);
        return symbol != null ? symbol : currencyCode;
    }

    /**
     * Returns the default number of fraction digits for this currency.
     * For instance, the default number of fraction digits for the US dollar is 2 because there are
     * 100 US cents in a US dollar. For the Japanese Yen, the number is 0 because coins smaller
     * than 1 Yen became invalid in 1953. In the case of pseudo-currencies, such as
     * IMF Special Drawing Rights, -1 is returned.
     */
    public int getDefaultFractionDigits() {
        // In some places the code XXX is used as the fall back currency.
        // The RI returns -1, but ICU defaults to 2 for unknown currencies.
        if (currencyCode.equals("XXX")) {
            return -1;
        }
        return ICU.getCurrencyFractionDigits(currencyCode);
    }

    /**
     * Returns this currency's ISO 4217 currency code.
     */
    @Override
    public String toString() {
        return currencyCode;
    }

    private Object readResolve() {
        return getInstance(currencyCode);
    }
}
