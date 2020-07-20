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

import com.google.j2objc.LibraryNotLinkedError;
import com.google.j2objc.util.CurrencyNumericCodes;
import java.io.Serializable;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

/**
 * Represents a currency. Currencies are identified by their ISO 4217 currency
 * codes. Visit the <a href="http://www.iso.org/iso/home/standards/currency_codes.htm">
 * ISO web site</a> for more information.
 * <p>
 * The class is designed so that there's never more than one
 * <code>Currency</code> instance for any given currency. Therefore, there's
 * no public constructor. You obtain a <code>Currency</code> instance using
 * the <code>getInstance</code> methods.
 *
 * @since 1.4
 */
public final class Currency implements Serializable {
    private static final long serialVersionUID = -158308464356906721L;

    private static final HashMap<String, Currency> codesToCurrencies = new HashMap<String, Currency>();
    private static final HashMap<Locale, Currency> localesToCurrencies = new HashMap<Locale, Currency>();

    private static final LinkedHashSet<String> availableCurrencyCodes =
        constructAvailableCurrencyCodes();

    /**
     * ISO 4217 currency code for this currency.
     *
     * @serial
     */
    private final String currencyCode;

    private Currency(String currencyCode) {
        this.currencyCode = currencyCode;
        if (!availableCurrencyCodes.contains(currencyCode)) {
            throw new IllegalArgumentException("Unsupported ISO 4217 currency code: " +
                    currencyCode);
        }
    }

    /**
     * Returns the <code>Currency</code> instance for the given currency code.
     *
     * @param currencyCode the ISO 4217 code of the currency
     * @return the <code>Currency</code> instance for the given currency code
     * @exception NullPointerException if <code>currencyCode</code> is null
     * @exception IllegalArgumentException if <code>currencyCode</code> is not
     * a supported ISO 4217 code.
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
     * Returns the <code>Currency</code> instance for the country of the
     * given locale. The language and variant components of the locale
     * are ignored. The result may vary over time, as countries change their
     * currencies. For example, for the original member countries of the
     * European Monetary Union, the method returns the old national currencies
     * until December 31, 2001, and the Euro from January 1, 2002, local time
     * of the respective countries.
     * <p>
     * The method returns <code>null</code> for territories that don't
     * have a currency, such as Antarctica.
     *
     * @param locale the locale for whose country a <code>Currency</code>
     * instance is needed
     * @return the <code>Currency</code> instance for the country of the given
     * locale, or {@code null}
     * @exception NullPointerException if <code>locale</code> or its country
     * code is {@code null}
     * @exception IllegalArgumentException if the country of the given {@code locale}
     * is not a supported ISO 3166 country code.
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

            String currencyCode = ICU.getCurrencyCode(locale);
            if (currencyCode == null) {
                // Don't cache -- this is rarely necessary since most countries have currencies.
                if (Arrays.asList(Locale.getISOCountries()).contains(locale.getCountry())) {
                    return null;
                }
                throw new IllegalArgumentException("Unsupported ISO 3166 country: " + locale);
            } else if (currencyCode.equals("XXX")) {
                return null;
            }
            Currency result = getInstance(currencyCode);
            localesToCurrencies.put(locale, result);
            return result;
        }
    }

    private static LinkedHashSet<String> constructAvailableCurrencyCodes() {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        for (String code : ICU.getAvailableCurrencyCodes()) {
          result.add(code);
        }
        return result;
    }

    /**
     * Gets the set of available currencies.  The returned set of currencies
     * contains all of the available currencies, which may include currencies
     * that represent obsolete ISO 4217 codes.  The set can be modified
     * without affecting the available currencies in the runtime.
     *
     * @return the set of available currencies.  If there is no currency
     *    available in the runtime, the returned set is empty.
     * @since 1.7
     */
    public static Set<Currency> getAvailableCurrencies() {
        Set<Currency> result = new LinkedHashSet<Currency>();
        for (String currencyCode : availableCurrencyCodes) {
            result.add(Currency.getInstance(currencyCode));
        }
        return result;
    }

    /**
     * Gets the ISO 4217 currency code of this currency.
     *
     * @return the ISO 4217 currency code of this currency.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Gets the symbol of this currency for the default
     * {@link Locale.Category#DISPLAY DISPLAY} locale.
     * For example, for the US Dollar, the symbol is "$" if the default
     * locale is the US, while for other locales it may be "US$". If no
     * symbol can be determined, the ISO 4217 currency code is returned.
     * <p>
     * This is equivalent to calling
     * {@link #getSymbol(Locale)
     *     getSymbol(Locale.getDefault(Locale.Category.DISPLAY))}.
     *
     * @return the symbol of this currency for the default
     *     {@link Locale.Category#DISPLAY DISPLAY} locale
     */
    public String getSymbol() {
        return getSymbol(Locale.getDefault(Locale.Category.DISPLAY));
    }

    /**
     * Gets the symbol of this currency for the specified locale.
     * For example, for the US Dollar, the symbol is "$" if the specified
     * locale is the US, while for other locales it may be "US$". If no
     * symbol can be determined, the ISO 4217 currency code is returned.
     *
     * @param locale the locale for which a display name for this currency is
     * needed
     * @return the symbol of this currency for the specified locale
     * @exception NullPointerException if <code>locale</code> is null
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
     * Gets the default number of fraction digits used with this currency.
     * For example, the default number of fraction digits for the Euro is 2,
     * while for the Japanese Yen it's 0.
     * In the case of pseudo-currencies, such as IMF Special Drawing Rights,
     * -1 is returned.
     *
     * @return the default number of fraction digits used with this currency
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
     * Returns the ISO 4217 numeric code of this currency.
     *
     * @return the ISO 4217 numeric code of this currency
     * @since 1.7
     */
    public int getNumericCode() {
        try {
            String name = "com.google.j2objc.util.CurrencyNumericCodesImpl";
            CurrencyNumericCodes cnc = (CurrencyNumericCodes) Class.forName(name).newInstance();
            return cnc.getNumericCode(currencyCode);
        } catch (Exception e) {
            throw new LibraryNotLinkedError("java.util support", "jre_util",
                "ComGoogleJ2objcUtilCurrencyNumericCodesImpl");
        }
    }

    /**
     * Gets the name that is suitable for displaying this currency for
     * the default {@link Locale.Category#DISPLAY DISPLAY} locale.
     * If there is no suitable display name found
     * for the default locale, the ISO 4217 currency code is returned.
     * <p>
     * This is equivalent to calling
     * {@link #getDisplayName(Locale)
     *     getDisplayName(Locale.getDefault(Locale.Category.DISPLAY))}.
     *
     * @return the display name of this currency for the default
     *     {@link Locale.Category#DISPLAY DISPLAY} locale
     * @since 1.7
     */
    public String getDisplayName() {
        return getDisplayName(Locale.getDefault(Locale.Category.DISPLAY));
    }

    /**
     * Gets the name that is suitable for displaying this currency for
     * the specified locale.  If there is no suitable display name found
     * for the specified locale, the ISO 4217 currency code is returned.
     *
     * @param locale the locale for which a display name for this currency is
     * needed
     * @return the display name of this currency for the specified locale
     * @exception NullPointerException if <code>locale</code> is null
     * @since 1.7
     */
    public String getDisplayName(Locale locale) {
        return ICU.getCurrencyDisplayName(locale, currencyCode);
    }

    /**
     * Returns the ISO 4217 currency code of this currency.
     *
     * @return the ISO 4217 currency code of this currency
     */
    @Override
    public String toString() {
        return currencyCode;
    }

    /**
     * Resolves instances being deserialized to a single instance per currency.
     */
    private Object readResolve() {
        return getInstance(currencyCode);
    }
}
