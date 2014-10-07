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

package java.text;

import java.io.IOException;
import java.io.Serializable;
import java.util.Currency;
import java.util.Locale;

import libcore.icu.ICU;
import libcore.icu.LocaleData;

/**
 * Encapsulates the set of symbols (such as the decimal separator, the grouping
 * separator, and so on) needed by {@code DecimalFormat} to format numbers.
 * {@code DecimalFormat} internally creates an instance of
 * {@code DecimalFormatSymbols} from its locale data. If you need to change any
 * of these symbols, you can get the {@code DecimalFormatSymbols} object from
 * your {@code DecimalFormat} and modify it.
 *
 * @see java.util.Locale
 * @see DecimalFormat
 */
public class DecimalFormatSymbols implements Cloneable, Serializable {

    private static final long serialVersionUID = 5772796243397350300L;

    private char zeroDigit;
    private char digit;
    private char decimalSeparator;
    private char groupingSeparator;
    private char patternSeparator;
    private char percent;
    private char perMill;
    private char monetarySeparator;
    private char minusSign;
    private String infinity, NaN, currencySymbol, intlCurrencySymbol;

    private transient Currency currency;
    private transient Locale locale;
    private transient String exponentSeparator;

    /**
     * Constructs a new {@code DecimalFormatSymbols} containing the symbols for
     * the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     * Best practice is to create a {@code DecimalFormat}
     * and then to get the {@code DecimalFormatSymbols} from that object by
     * calling {@link DecimalFormat#getDecimalFormatSymbols()}.
     */
    public DecimalFormatSymbols() {
        this(Locale.getDefault());
    }

    /**
     * Constructs a new DecimalFormatSymbols containing the symbols for the
     * specified Locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     * Best practice is to create a {@code DecimalFormat}
     * and then to get the {@code DecimalFormatSymbols} from that object by
     * calling {@link DecimalFormat#getDecimalFormatSymbols()}.
     *
     * @param locale
     *            the locale.
     */
    public DecimalFormatSymbols(Locale locale) {
        LocaleData localeData = LocaleData.get(locale);
        this.zeroDigit = localeData.zeroDigit;
        this.digit = '#';
        this.decimalSeparator = localeData.decimalSeparator;
        this.groupingSeparator = localeData.groupingSeparator;
        this.patternSeparator = localeData.patternSeparator;
        this.percent = localeData.percent;
        this.perMill = localeData.perMill;
        this.monetarySeparator = localeData.monetarySeparator;
        this.minusSign = localeData.minusSign;
        this.infinity = localeData.infinity;
        this.NaN = localeData.NaN;
        this.exponentSeparator = localeData.exponentSeparator;
        this.locale = locale;
        try {
            currency = Currency.getInstance(locale);
            currencySymbol = currency.getSymbol(locale);
            intlCurrencySymbol = currency.getCurrencyCode();
        } catch (IllegalArgumentException e) {
            currency = Currency.getInstance("XXX");
            currencySymbol = localeData.currencySymbol;
            intlCurrencySymbol = localeData.internationalCurrencySymbol;
        }
    }

    /**
     * Returns a new {@code DecimalFormatSymbols} instance for the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @return an instance of {@code DecimalFormatSymbols}
     * @since 1.6
     */
    public static DecimalFormatSymbols getInstance() {
        return getInstance(Locale.getDefault());
    }

    /**
     * Returns a new {@code DecimalFormatSymbols} for the given locale.
     *
     * @param locale the locale
     * @return an instance of {@code DecimalFormatSymbols}
     * @throws NullPointerException if {@code locale == null}
     * @since 1.6
     */
    public static DecimalFormatSymbols getInstance(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }
        return new DecimalFormatSymbols(locale);
    }

    /**
     * Returns an array of locales for which custom {@code DecimalFormatSymbols} instances
     * are available.
     * <p>Note that Android does not support user-supplied locale service providers.
     * @since 1.6
     */
    public static Locale[] getAvailableLocales() {
        return ICU.getAvailableDecimalFormatSymbolsLocales();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Compares the specified object to this {@code DecimalFormatSymbols} and
     * indicates if they are equal. In order to be equal, {@code object} must be
     * an instance of {@code DecimalFormatSymbols} and contain the same symbols.
     *
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this
     *         {@code DecimalFormatSymbols}; {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DecimalFormatSymbols)) {
            return false;
        }
        DecimalFormatSymbols obj = (DecimalFormatSymbols) object;
        return currency.equals(obj.currency) &&
                currencySymbol.equals(obj.currencySymbol) &&
                decimalSeparator == obj.decimalSeparator &&
                digit == obj.digit &&
                exponentSeparator.equals(obj.exponentSeparator) &&
                groupingSeparator == obj.groupingSeparator &&
                infinity.equals(obj.infinity) &&
                intlCurrencySymbol.equals(obj.intlCurrencySymbol) &&
                minusSign == obj.minusSign &&
                monetarySeparator == obj.monetarySeparator &&
                NaN.equals(obj.NaN) &&
                patternSeparator == obj.patternSeparator &&
                perMill == obj.perMill &&
                percent == obj.percent &&
                zeroDigit == obj.zeroDigit;
    }

    @Override
    public String toString() {
        return getClass().getName() +
                "[currency=" + currency +
                ",currencySymbol=" + currencySymbol +
                ",decimalSeparator=" + decimalSeparator +
                ",digit=" + digit +
                ",exponentSeparator=" + exponentSeparator +
                ",groupingSeparator=" + groupingSeparator +
                ",infinity=" + infinity +
                ",intlCurrencySymbol=" + intlCurrencySymbol +
                ",minusSign=" + minusSign +
                ",monetarySeparator=" + monetarySeparator +
                ",NaN=" + NaN +
                ",patternSeparator=" + patternSeparator +
                ",perMill=" + perMill +
                ",percent=" + percent +
                ",zeroDigit=" + zeroDigit +
                "]";
    }

    /**
     * Returns the currency.
     * <p>
     * {@code null} is returned if {@code setInternationalCurrencySymbol()} has
     * been previously called with a value that is not a valid ISO 4217 currency
     * code.
     * <p>
     *
     * @return the currency that was set in the constructor or by calling
     *         {@code setCurrency()} or {@code setInternationalCurrencySymbol()},
     *         or {@code null} if an invalid currency was set.
     * @see #setCurrency(Currency)
     * @see #setInternationalCurrencySymbol(String)
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Returns the international currency symbol.
     *
     * @return the international currency symbol as string.
     */
    public String getInternationalCurrencySymbol() {
        return intlCurrencySymbol;
    }

    /**
     * Returns the currency symbol.
     *
     * @return the currency symbol as string.
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    /**
     * Returns the character which represents the decimal point in a number.
     *
     * @return the decimal separator character.
     */
    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    /**
     * Returns the character which represents a single digit in a format
     * pattern.
     *
     * @return the digit pattern character.
     */
    public char getDigit() {
        return digit;
    }

    /**
     * Returns the character used as the thousands separator in a number.
     *
     * @return the thousands separator character.
     */
    public char getGroupingSeparator() {
        return groupingSeparator;
    }

    /**
     * Returns the string which represents infinity.
     *
     * @return the infinity symbol as a string.
     */
    public String getInfinity() {
        return infinity;
    }

    /**
     * Returns the minus sign character.
     *
     * @return the minus sign as a character.
     */
    public char getMinusSign() {
        return minusSign;
    }

    /**
     * Returns the character which represents the decimal point in a monetary
     * value.
     *
     * @return the monetary decimal point as a character.
     */
    public char getMonetaryDecimalSeparator() {
        return monetarySeparator;
    }

    /**
     * Returns the string which represents NaN.
     *
     * @return the symbol NaN as a string.
     */
    public String getNaN() {
        return NaN;
    }

    /**
     * Returns the character which separates the positive and negative patterns
     * in a format pattern.
     *
     * @return the pattern separator character.
     */
    public char getPatternSeparator() {
        return patternSeparator;
    }

    /**
     * Returns the percent character.
     *
     * @return the percent character.
     */
    public char getPercent() {
        return percent;
    }

    /**
     * Returns the per mill sign character.
     *
     * @return the per mill sign character.
     */
    public char getPerMill() {
        return perMill;
    }

    /**
     * Returns the character which represents zero.
     *
     * @return the zero character.
     */
    public char getZeroDigit() {
        return zeroDigit;
    }

    /*
     * Returns the string used to separate mantissa and exponent. Typically "E", as in "1.2E3".
     * @since 1.6
     */
    public String getExponentSeparator() {
        return exponentSeparator;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31*result + zeroDigit;
        result = 31*result + digit;
        result = 31*result + decimalSeparator;
        result = 31*result + groupingSeparator;
        result = 31*result + patternSeparator;
        result = 31*result + percent;
        result = 31*result + perMill;
        result = 31*result + monetarySeparator;
        result = 31*result + minusSign;
        result = 31*result + exponentSeparator.hashCode();
        result = 31*result + infinity.hashCode();
        result = 31*result + NaN.hashCode();
        result = 31*result + currencySymbol.hashCode();
        result = 31*result + intlCurrencySymbol.hashCode();
        return result;
    }

    /**
     * Sets the currency.
     * <p>
     * The international currency symbol and the currency symbol are updated,
     * but the min and max number of fraction digits stays the same.
     * <p>
     *
     * @param currency
     *            the new currency.
     * @throws NullPointerException
     *             if {@code currency} is {@code null}.
     */
    public void setCurrency(Currency currency) {
        if (currency == null) {
            throw new NullPointerException("currency == null");
        }
        if (currency == this.currency) {
            return;
        }
        this.currency = currency;
        intlCurrencySymbol = currency.getCurrencyCode();
        currencySymbol = currency.getSymbol(locale);
    }

    /**
     * Sets the international currency symbol.
     * <p>
     * The currency and currency symbol are also updated if {@code value} is a
     * valid ISO4217 currency code.
     * <p>
     * The min and max number of fraction digits stay the same.
     *
     * @param value
     *            the currency code.
     */
    public void setInternationalCurrencySymbol(String value) {
        if (value == null) {
            currency = null;
            intlCurrencySymbol = null;
            return;
        }

        if (value.equals(intlCurrencySymbol)) {
            return;
        }

        try {
            currency = Currency.getInstance(value);
            currencySymbol = currency.getSymbol(locale);
        } catch (IllegalArgumentException e) {
            currency = null;
        }
        intlCurrencySymbol = value;
    }

    /**
     * Sets the currency symbol.
     *
     * @param value
     *            the currency symbol.
     */
    public void setCurrencySymbol(String value) {
        this.currencySymbol = value;
    }

    /**
     * Sets the character which represents the decimal point in a number.
     *
     * @param value
     *            the decimal separator character.
     */
    public void setDecimalSeparator(char value) {
        this.decimalSeparator = value;
    }

    /**
     * Sets the character which represents a single digit in a format pattern.
     *
     * @param value
     *            the digit character.
     */
    public void setDigit(char value) {
        this.digit = value;
    }

    /**
     * Sets the character used as the thousands separator in a number.
     *
     * @param value
     *            the grouping separator character.
     */
    public void setGroupingSeparator(char value) {
        this.groupingSeparator = value;
    }

    /**
     * Sets the string which represents infinity.
     *
     * @param value
     *            the string representing infinity.
     */
    public void setInfinity(String value) {
        this.infinity = value;
    }

    /**
     * Sets the minus sign character.
     *
     * @param value
     *            the minus sign character.
     */
    public void setMinusSign(char value) {
        this.minusSign = value;
    }

    /**
     * Sets the character which represents the decimal point in a monetary
     * value.
     *
     * @param value
     *            the monetary decimal separator character.
     */
    public void setMonetaryDecimalSeparator(char value) {
        this.monetarySeparator = value;
    }

    /**
     * Sets the string which represents NaN.
     *
     * @param value
     *            the string representing NaN.
     */
    public void setNaN(String value) {
        this.NaN = value;
    }

    /**
     * Sets the character which separates the positive and negative patterns in
     * a format pattern.
     *
     * @param value
     *            the pattern separator character.
     */
    public void setPatternSeparator(char value) {
        this.patternSeparator = value;
    }

    /**
     * Sets the percent character.
     *
     * @param value
     *            the percent character.
     */
    public void setPercent(char value) {
        this.percent = value;
    }

    /**
     * Sets the per mill sign character.
     *
     * @param value
     *            the per mill character.
     */
    public void setPerMill(char value) {
        this.perMill = value;
    }

    /**
     * Sets the character which represents zero.
     *
     * @param value
     *            the zero digit character.
     */
    public void setZeroDigit(char value) {
        this.zeroDigit = value;
    }

    /**
     * Sets the string used to separate mantissa and exponent. Typically "E", as in "1.2E3".
     * @since 1.6
     */
    public void setExponentSeparator(String value) {
        if (value == null) {
            throw new NullPointerException("value == null");
        }
        this.exponentSeparator = value;
    }
}
