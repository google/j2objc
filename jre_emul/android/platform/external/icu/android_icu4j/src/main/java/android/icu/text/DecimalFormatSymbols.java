/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;

import android.icu.impl.CacheBase;
import android.icu.impl.CurrencyData;
import android.icu.impl.CurrencyData.CurrencyDisplayInfo;
import android.icu.impl.CurrencyData.CurrencyFormatInfo;
import android.icu.impl.CurrencyData.CurrencySpacingInfo;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SoftCache;
import android.icu.impl.UResource;
import android.icu.util.Currency;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;

/**
 * <strong>[icu enhancement]</strong> ICU's replacement for {@link java.text.DecimalFormatSymbols}.&nbsp;Methods, fields, and other functionality specific to ICU are labeled '<strong>[icu]</strong>'.
 *
 * This class represents the set of symbols (such as the decimal separator, the grouping
 * separator, and so on) needed by <code>DecimalFormat</code> to format
 * numbers. <code>DecimalFormat</code> creates for itself an instance of
 * <code>DecimalFormatSymbols</code> from its locale data.  If you need to change any of
 * these symbols, you can get the <code>DecimalFormatSymbols</code> object from your
 * <code>DecimalFormat</code> and modify it.
 *
 * @see          java.util.Locale
 * @see          DecimalFormat
 * @author       Mark Davis
 * @author       Alan Liu
 */
public class DecimalFormatSymbols implements Cloneable, Serializable {
    /**
     * Creates a DecimalFormatSymbols object for the default <code>FORMAT</code> locale.
     * @see Category#FORMAT
     */
    public DecimalFormatSymbols() {
        initialize(ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Creates a DecimalFormatSymbols object for the given locale.
     * @param locale the locale
     */
    public DecimalFormatSymbols(Locale locale) {
        initialize(ULocale.forLocale(locale));
    }

    /**
     * <strong>[icu]</strong> Creates a DecimalFormatSymbols object for the given locale.
     * @param locale the locale
     */
    public DecimalFormatSymbols(ULocale locale) {
        initialize(locale);
    }

    /**
     * Returns a DecimalFormatSymbols instance for the default locale.
     *
     * <p><strong>Note:</strong> Unlike
     * <code>java.text.DecimalFormatSymbols#getInstance</code>, this method simply returns
     * <code>new android.icu.text.DecimalFormatSymbols()</code>.  ICU currently does not
     * support <code>DecimalFormatSymbolsProvider</code>, which was introduced in Java 6.
     *
     * @return A DecimalFormatSymbols instance.
     */
    public static DecimalFormatSymbols getInstance() {
        return new DecimalFormatSymbols();
    }

    /**
     * Returns a DecimalFormatSymbols instance for the given locale.
     *
     * <p><strong>Note:</strong> Unlike
     * <code>java.text.DecimalFormatSymbols#getInstance</code>, this method simply returns
     * <code>new android.icu.text.DecimalFormatSymbols(locale)</code>.  ICU currently does
     * not support <code>DecimalFormatSymbolsProvider</code>, which was introduced in Java
     * 6.
     *
     * @param locale the locale.
     * @return A DecimalFormatSymbols instance.
     */
    public static DecimalFormatSymbols getInstance(Locale locale) {
        return new DecimalFormatSymbols(locale);
    }

    /**
     * Returns a DecimalFormatSymbols instance for the given locale.
     *
     * <p><strong>Note:</strong> Unlike
     * <code>java.text.DecimalFormatSymbols#getInstance</code>, this method simply returns
     * <code>new android.icu.text.DecimalFormatSymbols(locale)</code>.  ICU currently does
     * not support <code>DecimalFormatSymbolsProvider</code>, which was introduced in Java
     * 6.
     *
     * @param locale the locale.
     * @return A DecimalFormatSymbols instance.
     */
    public static DecimalFormatSymbols getInstance(ULocale locale) {
        return new DecimalFormatSymbols(locale);
    }

    /**
     * Returns an array of all locales for which the <code>getInstance</code> methods of
     * this class can return localized instances.
     *
     * <p><strong>Note:</strong> Unlike
     * <code>java.text.DecimalFormatSymbols#getAvailableLocales</code>, this method simply
     * returns the array of <code>Locale</code>s available for this class.  ICU currently
     * does not support <code>DecimalFormatSymbolsProvider</code>, which was introduced in
     * Java 6.
     *
     * @return An array of <code>Locale</code>s for which localized
     * <code>DecimalFormatSymbols</code> instances are available.
     */
    public static Locale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableLocales();
    }

    /**
     * <strong>[icu]</strong> Returns an array of all locales for which the <code>getInstance</code>
     * methods of this class can return localized instances.
     *
     * <p><strong>Note:</strong> Unlike
     * <code>java.text.DecimalFormatSymbols#getAvailableLocales</code>, this method simply
     * returns the array of <code>ULocale</code>s available in this class.  ICU currently
     * does not support <code>DecimalFormatSymbolsProvider</code>, which was introduced in
     * Java 6.
     *
     * @return An array of <code>ULocale</code>s for which localized
     * <code>DecimalFormatSymbols</code> instances are available.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static ULocale[] getAvailableULocales() {
        return ICUResourceBundle.getAvailableULocales();
    }


    /**
     * Returns the character used for zero. Different for Arabic, etc.
     * @return the character
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public char getZeroDigit() {
        return zeroDigit;
    }

    /**
     * Returns the array of characters used as digits, in order from 0 through 9
     * @return The array
     * @see #getDigitStrings()
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public char[] getDigits() {
        return digits.clone();
    }

    /**
     * Sets the character used for zero.
     * <p>
     * <b>Note:</b> When the specified zeroDigit is a Unicode decimal digit character
     * (category:Nd) and the number value is 0, then this method propagate digit 1 to
     * digit 9 by incrementing code point one by one.
     *
     * @param zeroDigit the zero character.
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public void setZeroDigit(char zeroDigit) {
        this.zeroDigit = zeroDigit;

        // digitStrings or digits might be referencing a cached copy for
        // optimization purpose, so creating a copy before making a modification
        digitStrings = digitStrings.clone();
        digits = digits.clone();

        // Make digitStrings field and digits field in sync
        digitStrings[0] = String.valueOf(zeroDigit);
        digits[0] = zeroDigit;

        // Android patch (ticket #11903) begin.
            for (int i = 1; i < 10; i++) {
                char d = (char)(zeroDigit + i);
                digitStrings[i] = String.valueOf(d);
                digits[i] = d;
            }
        // Android patch (ticket #11903) end.
    }

    /**
    * <strong>[icu]</strong> Returns the array of strings used as digits, in order from 0 through 9
    * @return The array of ten digit strings
    * @see #setDigitStrings(String[])
    * @hide draft / provisional / internal are hidden on Android
    */
    public String[] getDigitStrings() {
        return digitStrings.clone();
    }

    /**
     * Returns the array of strings used as digits, in order from 0 through 9
     * Package private method - doesn't create a defensively copy.
     * @return the array of digit strings
     */
    String[] getDigitStringsLocal() {
        return digitStrings;
    }

    /**
    * <strong>[icu]</strong> Sets the array of strings used as digits, in order from 0 through 9
    * <p>
    * <b>Note:</b>
    * <p>
    * When the input array of digit strings contains any strings
    * represented by multiple Java chars, then {@link #getDigits()} will return
    * the default digits ('0' - '9') and {@link #getZeroDigit()} will return the
    * default zero digit ('0').
    *
    * @param digitStrings The array of digit strings. The length of the array must be exactly 10.
    * @throws NullPointerException if the <code>digitStrings</code> is null.
    * @throws IllegalArgumentException if the length of the array is not 10.
    * @see #getDigitStrings()
    * @hide draft / provisional / internal are hidden on Android
    */
    public void setDigitStrings(String[] digitStrings) {
        if (digitStrings == null) {
            throw new NullPointerException("The input digit string array is null");
        }
        if (digitStrings.length != 10) {
            throw new IllegalArgumentException("Number of digit strings is not 10");
        }

        // Scan input array and create char[] representation if possible
        String[] tmpDigitStrings = new String[10];
        char[] tmpDigits = new char[10];
        for (int i = 0; i < 10; i++) {
            if (digitStrings[i] == null) {
                throw new IllegalArgumentException("The input digit string array contains a null element");
            }
            tmpDigitStrings[i] = digitStrings[i];
            if (tmpDigits != null && digitStrings[i].length() == 1) {
                tmpDigits[i] = digitStrings[i].charAt(0);
            } else {
                // contains digit string with multiple UTF-16 code units
                tmpDigits = null;
            }
        }

        this.digitStrings = tmpDigitStrings;

        if (tmpDigits == null) {
            // fallback to the default digit chars
            this.zeroDigit = DEF_DIGIT_CHARS_ARRAY[0];
            this.digits = DEF_DIGIT_CHARS_ARRAY;
        } else {
            this.zeroDigit = tmpDigits[0];
            this.digits = tmpDigits;
        }
    }

    /**
     * Returns the character used to represent a significant digit in a pattern.
     * @return the significant digit pattern character
     */
    public char getSignificantDigit() {
        return sigDigit;
    }

    /**
     * Sets the character used to represent a significant digit in a pattern.
     * @param sigDigit the significant digit pattern character
     */
    public void setSignificantDigit(char sigDigit) {
        this.sigDigit = sigDigit;
    }

    /**
     * Returns the character used for grouping separator. Different for French, etc.
     * @return the thousands character
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public char getGroupingSeparator() {
        return groupingSeparator;
    }

    /**
     * Sets the character used for grouping separator. Different for French, etc.
     * @param groupingSeparator the thousands character
     * @see #setGroupingSeparatorString(String)
     */
    public void setGroupingSeparator(char groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
        this.groupingSeparatorString = String.valueOf(groupingSeparator);
    }

    /**
     * <strong>[icu]</strong> Returns the string used for grouping separator. Different for French, etc.
     * @return the grouping separator string
     * @see #setGroupingSeparatorString(String)
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getGroupingSeparatorString() {
        return groupingSeparatorString;
    }

    /**
     * <strong>[icu]</strong> Sets the string used for grouping separator.
     * <p>
     * <b>Note:</b> When the input grouping separator String is represented
     * by multiple Java chars, then {@link #getGroupingSeparator()} will
     * return the default grouping separator character (',').
     *
     * @param groupingSeparatorString the grouping separator string
     * @throws NullPointerException if <code>groupingSeparatorString</code> is null.
     * @see #getGroupingSeparatorString()
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setGroupingSeparatorString(String groupingSeparatorString) {
        if (groupingSeparatorString == null) {
            throw new NullPointerException("The input grouping separator is null");
        }
        this.groupingSeparatorString = groupingSeparatorString;
        if (groupingSeparatorString.length() == 1) {
            this.groupingSeparator = groupingSeparatorString.charAt(0);
        } else {
            // Use the default grouping separator character as fallback
            this.groupingSeparator = DEF_GROUPING_SEPARATOR;
        }
    }

    /**
     * Returns the character used for decimal sign. Different for French, etc.
     * @return the decimal character
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    /**
     * Sets the character used for decimal sign. Different for French, etc.
     * @param decimalSeparator the decimal character
     */
    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
        this.decimalSeparatorString = String.valueOf(decimalSeparator);
    }

    /**
     * <strong>[icu]</strong> Returns the string used for decimal sign.
     * @return the decimal sign string
     * @see #setDecimalSeparatorString(String)
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getDecimalSeparatorString() {
        return decimalSeparatorString;
    }

    /**
     * <strong>[icu]</strong> Sets the string used for decimal sign.
     * <p>
     * <b>Note:</b> When the input decimal separator String is represented
     * by multiple Java chars, then {@link #getDecimalSeparator()} will
     * return the default decimal separator character ('.').
     *
     * @param decimalSeparatorString the decimal sign string
     * @throws NullPointerException if <code>decimalSeparatorString</code> is null.
     * @see #getDecimalSeparatorString()
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setDecimalSeparatorString(String decimalSeparatorString) {
        if (decimalSeparatorString == null) {
            throw new NullPointerException("The input decimal separator is null");
        }
        this.decimalSeparatorString = decimalSeparatorString;
        if (decimalSeparatorString.length() == 1) {
            this.decimalSeparator = decimalSeparatorString.charAt(0);
        } else {
            // Use the default decimal separator character as fallback
            this.decimalSeparator = DEF_DECIMAL_SEPARATOR;
        }
    }

    /**
     * Returns the character used for mille percent sign. Different for Arabic, etc.
     * @return the mille percent character
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public char getPerMill() {
        return perMill;
    }

    /**
     * Sets the character used for mille percent sign. Different for Arabic, etc.
     * @param perMill the mille percent character
     */
    public void setPerMill(char perMill) {
        this.perMill = perMill;
        this.perMillString = String.valueOf(perMill);
    }

    /**
     * <strong>[icu]</strong> Returns the string used for permille sign.
     * @return the permille string
     * @see #setPerMillString(String)
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getPerMillString() {
        return perMillString;
    }

    /**
    * <strong>[icu]</strong> Sets the string used for permille sign.
     * <p>
     * <b>Note:</b> When the input permille String is represented
     * by multiple Java chars, then {@link #getPerMill()} will
     * return the default permille character ('&#x2030;').
     *
     * @param perMillString the permille string
     * @throws NullPointerException if <code>perMillString</code> is null.
     * @see #getPerMillString()
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setPerMillString(String perMillString) {
        if (perMillString == null) {
            throw new NullPointerException("The input permille string is null");
        }
        this.perMillString = perMillString;
        if (perMillString.length() == 1) {
            this.perMill = perMillString.charAt(0);
        } else {
            // Use the default permille character as fallback
            this.perMill = DEF_PERMILL;
        }
    }

    /**
     * Returns the character used for percent sign. Different for Arabic, etc.
     * @return the percent character
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public char getPercent() {
        return percent;
    }

    /**
     * Sets the character used for percent sign. Different for Arabic, etc.
     * @param percent the percent character
     */
    public void setPercent(char percent) {
        this.percent = percent;
        this.percentString = String.valueOf(percent);
    }

    /**
     * <strong>[icu]</strong> Returns the string used for percent sign.
     * @return the percent string
     * @see #setPercentString(String)
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getPercentString() {
        return percentString;
    }

    /**
     * <strong>[icu]</strong> Sets the string used for percent sign.
     * <p>
     * <b>Note:</b> When the input grouping separator String is represented
     * by multiple Java chars, then {@link #getPercent()} will
     * return the default percent sign character ('%').
     *
     * @param percentString the percent string
     * @throws NullPointerException if <code>percentString</code> is null.
     * @see #getPercentString()
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setPercentString(String percentString) {
        if (percentString == null) {
            throw new NullPointerException("The input percent sign is null");
        }
        this.percentString = percentString;
        if (percentString.length() == 1) {
            this.percent = percentString.charAt(0);
        } else {
            // Use default percent character as fallback
            this.percent = DEF_PERCENT;
        }
    }

    /**
     * Returns the character used for a digit in a pattern.
     * @return the digit pattern character
     */
    public char getDigit() {
        return digit;
    }

    /**
     * Sets the character used for a digit in a pattern.
     * @param digit the digit pattern character
     */
    public void setDigit(char digit) {
        this.digit = digit;
    }

    /**
     * Returns the character used to separate positive and negative subpatterns
     * in a pattern.
     * @return the pattern separator character
     */
    public char getPatternSeparator() {
        return patternSeparator;
    }

    /**
     * Sets the character used to separate positive and negative subpatterns
     * in a pattern.
     * @param patternSeparator the pattern separator character
     */
    public void setPatternSeparator(char patternSeparator) {
        this.patternSeparator = patternSeparator;
    }

    /**
     * Returns the String used to represent infinity. Almost always left
     * unchanged.
     * @return the Infinity string
     */
     //Bug 4194173 [Richard/GCL]

    public String getInfinity() {
        return infinity;
    }

    /**
     * Sets the String used to represent infinity. Almost always left
     * unchanged.
     * @param infinity the Infinity String
     */
    public void setInfinity(String infinity) {
        this.infinity = infinity;
    }

    /**
     * Returns the String used to represent NaN. Almost always left
     * unchanged.
     * @return the NaN String
     */
     //Bug 4194173 [Richard/GCL]
    public String getNaN() {
        return NaN;
    }

    /**
     * Sets the String used to represent NaN. Almost always left
     * unchanged.
     * @param NaN the NaN String
     */
    public void setNaN(String NaN) {
        this.NaN = NaN;
    }

    /**
     * Returns the character used to represent minus sign. If no explicit
     * negative format is specified, one is formed by prefixing
     * minusSign to the positive format.
     * @return the minus sign character
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public char getMinusSign() {
        return minusSign;
    }

    /**
     * Sets the character used to represent minus sign. If no explicit
     * negative format is specified, one is formed by prefixing
     * minusSign to the positive format.
     * @param minusSign the minus sign character
     */
    public void setMinusSign(char minusSign) {
        this.minusSign = minusSign;
        this.minusString = String.valueOf(minusSign);
    }

    /**
     * <strong>[icu]</strong> Returns the string used to represent minus sign.
     * @return the minus sign string
     * @see #setMinusSignString(String)
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getMinusSignString() {
        return minusString;
    }

    /**
     * <strong>[icu]</strong> Sets the string used to represent minus sign.
     * <p>
     * <b>Note:</b> When the input minus sign String is represented
     * by multiple Java chars, then {@link #getMinusSign()} will
     * return the default minus sign character ('-').
     *
     * @param minusSignString the minus sign string
     * @throws NullPointerException if <code>minusSignString</code> is null.
     * @see #getGroupingSeparatorString()
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setMinusSignString(String minusSignString) {
        if (minusSignString == null) {
            throw new NullPointerException("The input minus sign is null");
        }
        this.minusString = minusSignString;
        if (minusSignString.length() == 1) {
            this.minusSign = minusSignString.charAt(0);
        } else {
            // Use the default minus sign as fallback
            this.minusSign = DEF_MINUS_SIGN;
        }
    }

    /**
     * <strong>[icu]</strong> Returns the localized plus sign.
     * @return the plus sign, used in localized patterns and formatted
     * strings
     * @see #setPlusSign
     * @see #setMinusSign
     * @see #getMinusSign
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public char getPlusSign() {
        return plusSign;
    }

    /**
     * <strong>[icu]</strong> Sets the localized plus sign.
     * @param plus the plus sign, used in localized patterns and formatted
     * strings
     * @see #getPlusSign
     * @see #setMinusSign
     * @see #getMinusSign
     */
    public void setPlusSign(char plus) {
        this.plusSign = plus;
        this.plusString = String.valueOf(plus);
    }

    /**
     * <strong>[icu]</strong> Returns the string used to represent plus sign.
     * @return the plus sign string
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getPlusSignString() {
        return plusString;
    }

    /**
     * <strong>[icu]</strong> Sets the localized plus sign string.
     * <p>
     * <b>Note:</b> When the input plus sign String is represented
     * by multiple Java chars, then {@link #getPlusSign()} will
     * return the default plus sign character ('+').
     *
     * @param plusSignString the plus sign string, used in localized patterns and formatted
     * strings
     * @throws NullPointerException if <code>plusSignString</code> is null.
     * @see #getPlusSignString()
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setPlusSignString(String plusSignString) {
        if (plusSignString == null) {
            throw new NullPointerException("The input plus sign is null");
        }
        this.plusString = plusSignString;
        if (plusSignString.length() == 1) {
            this.plusSign = plusSignString.charAt(0);
        } else {
            // Use the default plus sign as fallback
            this.plusSign = DEF_PLUS_SIGN;
        }
    }

    /**
     * Returns the string denoting the local currency.
     * @return the local currency String.
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    /**
     * Sets the string denoting the local currency.
     * @param currency the local currency String.
     */
    public void setCurrencySymbol(String currency) {
        currencySymbol = currency;
    }

    /**
     * Returns the international string denoting the local currency.
     * @return the international string denoting the local currency
     */
    public String getInternationalCurrencySymbol() {
        return intlCurrencySymbol;
    }

    /**
     * Sets the international string denoting the local currency.
     * @param currency the international string denoting the local currency.
     */
    public void setInternationalCurrencySymbol(String currency) {
        intlCurrencySymbol = currency;
    }

    /**
     * Returns the currency symbol, for {@link DecimalFormatSymbols#getCurrency()} API
     * compatibility only. ICU clients should use the Currency API directly.
     * @return the currency used, or null
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Sets the currency.
     *
     * <p><strong>Note:</strong> ICU does not use the DecimalFormatSymbols for the currency
     * any more.  This API is present for API compatibility only.
     *
     * <p>This also sets the currency symbol attribute to the currency's symbol
     * in the DecimalFormatSymbols' locale, and the international currency
     * symbol attribute to the currency's ISO 4217 currency code.
     *
     * @param currency the new currency to be used
     * @throws NullPointerException if <code>currency</code> is null
     * @see #setCurrencySymbol
     * @see #setInternationalCurrencySymbol
     */
    public void setCurrency(Currency currency) {
        if (currency == null) {
            throw new NullPointerException();
        }
        this.currency = currency;
        intlCurrencySymbol = currency.getCurrencyCode();
        currencySymbol = currency.getSymbol(requestedLocale);
    }

    /**
     * Returns the monetary decimal separator.
     * @return the monetary decimal separator character
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public char getMonetaryDecimalSeparator() {
        return monetarySeparator;
    }

    /**
     * Sets the monetary decimal separator.
     * @param sep the monetary decimal separator character
     */
    public void setMonetaryDecimalSeparator(char sep) {
        this.monetarySeparator = sep;
        this.monetarySeparatorString = String.valueOf(sep);
    }

    /**
     * <strong>[icu]</strong> Returns the monetary decimal separator string.
     * @return the monetary decimal separator string
     * @see #setMonetaryDecimalSeparatorString(String)
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getMonetaryDecimalSeparatorString() {
        return monetarySeparatorString;
    }

    /**
     * <strong>[icu]</strong> Sets the monetary decimal separator string.
     * <p>
     * <b>Note:</b> When the input monetary decimal separator String is represented
     * by multiple Java chars, then {@link #getMonetaryDecimalSeparatorString()} will
     * return the default monetary decimal separator character ('.').
     *
     * @param sep the monetary decimal separator string
     * @throws NullPointerException if <code>sep</code> is null.
     * @see #getMonetaryDecimalSeparatorString()
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setMonetaryDecimalSeparatorString(String sep) {
        if (sep == null) {
            throw new NullPointerException("The input monetary decimal separator is null");
        }
        this.monetarySeparatorString = sep;
        if (sep.length() == 1) {
            this.monetarySeparator = sep.charAt(0);
        } else {
            // Use default decimap separator character as fallbacl
            this.monetarySeparator = DEF_DECIMAL_SEPARATOR;
        }
    }

    /**
     * <strong>[icu]</strong> Returns the monetary grouping separator.
     * @return the monetary grouping separator character
     * @apiNote <strong>Discouraged:</strong> ICU 58 use 
     */
    public char getMonetaryGroupingSeparator() {
        return monetaryGroupingSeparator;
    }

    /**
     * <strong>[icu]</strong> Sets the monetary grouping separator.
     * @param sep the monetary grouping separator character
     */
    public void setMonetaryGroupingSeparator(char sep) {
        this.monetaryGroupingSeparator = sep;
        this.monetaryGroupingSeparatorString = String.valueOf(sep);
    }

    /**
     * <strong>[icu]</strong> Returns the monetary grouping separator.
     * @return the monetary grouping separator string
     * @see #setMonetaryGroupingSeparatorString(String)
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getMonetaryGroupingSeparatorString() {
        return monetaryGroupingSeparatorString;
    }

    /**
     * <strong>[icu]</strong> Sets the monetary grouping separator string.
     * <p>
     * <b>Note:</b> When the input grouping separator String is represented
     * by multiple Java chars, then {@link #getMonetaryGroupingSeparator()} will
     * return the default monetary grouping separator character (',').
     *
     * @param sep the monetary grouping separator string
     * @throws NullPointerException if <code>sep</code> is null.
     * @see #getMonetaryGroupingSeparatorString()
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setMonetaryGroupingSeparatorString(String sep) {
        if (sep == null) {
            throw new NullPointerException("The input monetary grouping separator is null");
        }
        this.monetaryGroupingSeparatorString = sep;
        if (sep.length() == 1) {
            this.monetaryGroupingSeparator = sep.charAt(0);
        } else {
            // Use default grouping separator character as fallback
            this.monetaryGroupingSeparator = DEF_GROUPING_SEPARATOR;
        }
    }

    /**
    }
     * Internal API for NumberFormat
     * @return String currency pattern string
     */
    String getCurrencyPattern() {
        return currencyPattern;
    }

    /**
    * Returns the multiplication sign
    */
    public String getExponentMultiplicationSign() {
        return exponentMultiplicationSign;
    }

    /**
    * Sets the multiplication sign
    */
    public void setExponentMultiplicationSign(String exponentMultiplicationSign) {
        this.exponentMultiplicationSign = exponentMultiplicationSign;
    }

    /**
     * <strong>[icu]</strong> Returns the string used to separate the mantissa from the exponent.
     * Examples: "x10^" for 1.23x10^4, "E" for 1.23E4.
     * @return the localized exponent symbol, used in localized patterns
     * and formatted strings
     * @see #setExponentSeparator
     */
    public String getExponentSeparator() {
        return exponentSeparator;
    }

    /**
     * <strong>[icu]</strong> Sets the string used to separate the mantissa from the exponent.
     * Examples: "x10^" for 1.23x10^4, "E" for 1.23E4.
     * @param exp the localized exponent symbol, used in localized patterns
     * and formatted strings
     * @see #getExponentSeparator
     */
    public void setExponentSeparator(String exp) {
        exponentSeparator = exp;
    }

    /**
     * <strong>[icu]</strong> Returns the character used to pad numbers out to a specified width.  This is
     * not the pad character itself; rather, it is the special pattern character
     * <em>preceding</em> the pad character.  In the pattern "*_#,##0", '*' is the pad
     * escape, and '_' is the pad character.
     * @return the character
     * @see #setPadEscape
     * @see DecimalFormat#getFormatWidth
     * @see DecimalFormat#getPadPosition
     * @see DecimalFormat#getPadCharacter
     */
    public char getPadEscape() {
        return padEscape;
    }

    /**
     * <strong>[icu]</strong> Sets the character used to pad numbers out to a specified width.  This is not
     * the pad character itself; rather, it is the special pattern character
     * <em>preceding</em> the pad character.  In the pattern "*_#,##0", '*' is the pad
     * escape, and '_' is the pad character.
     * @see #getPadEscape
     * @see DecimalFormat#setFormatWidth
     * @see DecimalFormat#setPadPosition
     * @see DecimalFormat#setPadCharacter
     */
    public void setPadEscape(char c) {
        padEscape = c;
    }

    /**
     * <strong>[icu]</strong> Indicates the currency match pattern used in {@link #getPatternForCurrencySpacing}.
     */
    public static final int CURRENCY_SPC_CURRENCY_MATCH = 0;

    /**
     * <strong>[icu]</strong> Indicates the surrounding match pattern used in {@link
     * #getPatternForCurrencySpacing}.
     */
    public static final int CURRENCY_SPC_SURROUNDING_MATCH = 1;

    /**
     * <strong>[icu]</strong> Indicates the insertion value used in {@link #getPatternForCurrencySpacing}.
     */
    public static final int CURRENCY_SPC_INSERT = 2;

    private String[] currencySpcBeforeSym;
    private String[] currencySpcAfterSym;

    /**
     * <strong>[icu]</strong> Returns the desired currency spacing value. Original values come from ICU's
     * CLDR data based on the locale provided during construction, and can be null.  These
     * values govern what and when text is inserted between a currency code/name/symbol
     * and the currency amount when formatting money.
     *
     * <p>For more information, see <a href="http://www.unicode.org/reports/tr35/#Currencies"
     * >UTS#35 section 5.10.2</a>.
     *
     * <p><strong>Note:</strong> ICU4J does not currently use this information.
     *
     * @param itemType one of CURRENCY_SPC_CURRENCY_MATCH, CURRENCY_SPC_SURROUNDING_MATCH
     * or CURRENCY_SPC_INSERT
     * @param beforeCurrency true to get the <code>beforeCurrency</code> values, false
     * to get the <code>afterCurrency</code> values.
     * @return the value, or null.
     * @see #setPatternForCurrencySpacing(int, boolean, String)
     */
    public String getPatternForCurrencySpacing(int itemType, boolean beforeCurrency)  {
        if (itemType < CURRENCY_SPC_CURRENCY_MATCH ||
            itemType > CURRENCY_SPC_INSERT ) {
            throw new IllegalArgumentException("unknown currency spacing: " + itemType);
        }
        if (beforeCurrency) {
            return currencySpcBeforeSym[itemType];
        }
        return currencySpcAfterSym[itemType];
    }

    /**
     * <strong>[icu]</strong> Sets the indicated currency spacing pattern or value. See {@link
     * #getPatternForCurrencySpacing} for more information.
     *
     * <p>Values for currency match and surrounding match must be {@link
     * android.icu.text.UnicodeSet} patterns. Values for insert can be any string.
     *
     * <p><strong>Note:</strong> ICU4J does not currently use this information.
     *
     * @param itemType one of CURRENCY_SPC_CURRENCY_MATCH, CURRENCY_SPC_SURROUNDING_MATCH
     * or CURRENCY_SPC_INSERT
     * @param beforeCurrency true if the pattern is for before the currency symbol.
     * false if the pattern is for after it.
     * @param  pattern string to override current setting; can be null.
     * @see #getPatternForCurrencySpacing(int, boolean)
     */
    public void setPatternForCurrencySpacing(int itemType, boolean beforeCurrency, String pattern) {
        if (itemType < CURRENCY_SPC_CURRENCY_MATCH ||
            itemType > CURRENCY_SPC_INSERT ) {
            throw new IllegalArgumentException("unknown currency spacing: " + itemType);
        }
        if (beforeCurrency) {
            currencySpcBeforeSym[itemType] = pattern;
        } else {
            currencySpcAfterSym[itemType] = pattern;
        }
    }

    /**
     * Returns the locale for which this object was constructed.
     * @return the locale for which this object was constructed
     */
    public Locale getLocale() {
        return requestedLocale;
    }

    /**
     * Returns the locale for which this object was constructed.
     * @return the locale for which this object was constructed
     */
    public ULocale getULocale() {
        return ulocale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
            // other fields are bit-copied
        } catch (CloneNotSupportedException e) {
            ///CLOVER:OFF
            throw new ICUCloneNotSupportedException(e);
            ///CLOVER:ON
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DecimalFormatSymbols)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        DecimalFormatSymbols other = (DecimalFormatSymbols) obj;
        for (int i = 0; i <= CURRENCY_SPC_INSERT; i++) {
            if (!currencySpcBeforeSym[i].equals(other.currencySpcBeforeSym[i])) {
                return false;
            }
            if (!currencySpcAfterSym[i].equals(other.currencySpcAfterSym[i])) {
                return false;
            }
        }

        if ( other.digits == null ) {
            for (int i = 0 ; i < 10 ; i++) {
                if (digits[i] != other.zeroDigit + i) {
                    return false;
                }
            }
        } else if (!Arrays.equals(digits,other.digits)) {
                    return false;
        }

        return (
        groupingSeparator == other.groupingSeparator &&
        decimalSeparator == other.decimalSeparator &&
        percent == other.percent &&
        perMill == other.perMill &&
        digit == other.digit &&
        minusSign == other.minusSign &&
        minusString.equals(other.minusString) &&
        patternSeparator == other.patternSeparator &&
        infinity.equals(other.infinity) &&
        NaN.equals(other.NaN) &&
        currencySymbol.equals(other.currencySymbol) &&
        intlCurrencySymbol.equals(other.intlCurrencySymbol) &&
        padEscape == other.padEscape &&
        plusSign == other.plusSign &&
        plusString.equals(other.plusString) &&
        exponentSeparator.equals(other.exponentSeparator) &&
        monetarySeparator == other.monetarySeparator &&
        monetaryGroupingSeparator == other.monetaryGroupingSeparator &&
        exponentMultiplicationSign.equals(other.exponentMultiplicationSign));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
            int result = digits[0];
            result = result * 37 + groupingSeparator;
            result = result * 37 + decimalSeparator;
            return result;
    }

    /**
     * List of field names to be loaded from the data files.
     * The indices of each name into the array correspond to the position of that item in the
     * numberElements array.
     */
    private static final String[] SYMBOL_KEYS = {
            "decimal",
            "group",
            "list",
            "percentSign",
            "minusSign",
            "plusSign",
            "exponential",
            "perMille",
            "infinity",
            "nan",
            "currencyDecimal",
            "currencyGroup",
            "superscriptingExponent"
    };

    /*
     * Default digits
     */
    private static final String[] DEF_DIGIT_STRINGS_ARRAY =
        {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private static final char[] DEF_DIGIT_CHARS_ARRAY =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    /*
     *  Default symbol characters, used for fallbacks.
     */
    private static final char DEF_DECIMAL_SEPARATOR = '.';
    private static final char DEF_GROUPING_SEPARATOR = ',';
    private static final char DEF_PERCENT = '%';
    private static final char DEF_MINUS_SIGN = '-';
    private static final char DEF_PLUS_SIGN = '+';
    private static final char DEF_PERMILL = '\u2030';

    /**
     * List of default values for the symbols.
     */
    private static final String[] SYMBOL_DEFAULTS = new String[] {
            String.valueOf(DEF_DECIMAL_SEPARATOR),  // decimal
            String.valueOf(DEF_GROUPING_SEPARATOR), // group
            ";", // list
            String.valueOf(DEF_PERCENT),    // percentSign
            String.valueOf(DEF_MINUS_SIGN), // minusSign
            String.valueOf(DEF_PLUS_SIGN),  // plusSign
            "E", // exponential
            String.valueOf(DEF_PERMILL),    // perMille
            "\u221e", // infinity
            "NaN", // NaN
            null, // currency decimal
            null, // currency group
            "\u00D7" // superscripting exponent
        };

    /**
     * Constants for path names in the data bundles.
     */
    private static final String LATIN_NUMBERING_SYSTEM = "latn";
    private static final String NUMBER_ELEMENTS = "NumberElements";
    private static final String SYMBOLS = "symbols";

    /**
     * Sink for enumerating all of the decimal format symbols (more specifically, anything
     * under the "NumberElements.symbols" tree).
     *
     * More specific bundles (en_GB) are enumerated before their parents (en_001, en, root):
     * Only store a value if it is still missing, that is, it has not been overridden.
     */
    private static final class DecFmtDataSink extends UResource.Sink {

        private String[] numberElements; // Array where to store the characters (set in constructor)

        public DecFmtDataSink(String[] numberElements) {
            this.numberElements = numberElements;
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table symbolsTable = value.getTable();
            for (int j = 0; symbolsTable.getKeyAndValue(j, key, value); ++j) {
                for (int i = 0; i < SYMBOL_KEYS.length; i++) {
                    if (key.contentEquals(SYMBOL_KEYS[i])) {
                        if (numberElements[i] == null) {
                            numberElements[i] = value.toString();
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Initializes the symbols from the locale data.
     */
    private void initialize( ULocale locale ) {
        this.requestedLocale = locale.toLocale();
        this.ulocale = locale;
        CacheData data = cachedLocaleData.getInstance(locale, null /* unused */);
        setLocale(data.validLocale, data.validLocale);
        setDigitStrings(data.digits);
        String[] numberElements = data.numberElements;

        // Copy data from the numberElements map into instance fields
        setDecimalSeparatorString(numberElements[0]);
        setGroupingSeparatorString(numberElements[1]);

        // See CLDR #9781
        // assert numberElements[2].length() == 1;
        patternSeparator = numberElements[2].charAt(0);

        setPercentString(numberElements[3]);
        setMinusSignString(numberElements[4]);
        setPlusSignString(numberElements[5]);
        setExponentSeparator(numberElements[6]);
        setPerMillString(numberElements[7]);
        setInfinity(numberElements[8]);
        setNaN(numberElements[9]);
        setMonetaryDecimalSeparatorString(numberElements[10]);
        setMonetaryGroupingSeparatorString(numberElements[11]);
        setExponentMultiplicationSign(numberElements[12]);

        digit = DecimalFormat.PATTERN_DIGIT;  // Localized pattern character no longer in CLDR
        padEscape = DecimalFormat.PATTERN_PAD_ESCAPE;
        sigDigit  = DecimalFormat.PATTERN_SIGNIFICANT_DIGIT;


        CurrencyDisplayInfo info = CurrencyData.provider.getInstance(locale, true);

        // Obtain currency data from the currency API.  This is strictly
        // for backward compatibility; we don't use DecimalFormatSymbols
        // for currency data anymore.
        currency = Currency.getInstance(locale);
        if (currency != null) {
            intlCurrencySymbol = currency.getCurrencyCode();
            currencySymbol = currency.getName(locale, Currency.SYMBOL_NAME, null);
            CurrencyFormatInfo fmtInfo = info.getFormatInfo(intlCurrencySymbol);
            if (fmtInfo != null) {
                currencyPattern = fmtInfo.currencyPattern;
                setMonetaryDecimalSeparatorString(fmtInfo.monetarySeparator);
                setMonetaryGroupingSeparatorString(fmtInfo.monetaryGroupingSeparator);
            }
        } else {
            intlCurrencySymbol = "XXX";
            currencySymbol = "\u00A4"; // 'OX' currency symbol
        }


        // Get currency spacing data.
        initSpacingInfo(info.getSpacingInfo());
    }

    private static CacheData loadData(ULocale locale) {
        String nsName;
        // Attempt to set the decimal digits based on the numbering system for the requested locale.
        NumberingSystem ns = NumberingSystem.getInstance(locale);
        String[] digits = new String[10];
        if (ns != null && ns.getRadix() == 10 && !ns.isAlgorithmic() &&
                NumberingSystem.isValidDigitString(ns.getDescription())) {
            String digitString = ns.getDescription();

            for (int i = 0, offset = 0; i < 10; i++) {
                int cp = digitString.codePointAt(offset);
                int nextOffset = offset + Character.charCount(cp);
                digits[i] = digitString.substring(offset, nextOffset);
                offset = nextOffset;
            }
            nsName = ns.getName();
        } else {
            // Default numbering system
            digits = DEF_DIGIT_STRINGS_ARRAY;
            nsName = "latn";
        }

        // Open the resource bundle and get the locale IDs.
        // TODO: Is there a better way to get the locale than making an ICUResourceBundle instance?
        ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.
                getBundleInstance(ICUData.ICU_BASE_NAME, locale);
        // TODO: Determine actual and valid locale correctly.
        ULocale validLocale = rb.getULocale();

        String[] numberElements = new String[SYMBOL_KEYS.length];

        // Load using a data sink
        DecFmtDataSink sink = new DecFmtDataSink(numberElements);
        try {
            rb.getAllItemsWithFallback(NUMBER_ELEMENTS + "/" + nsName + "/" + SYMBOLS, sink);
        } catch (MissingResourceException e) {
            // The symbols don't exist for the given nsName and resource bundle.
            // Silently ignore and fall back to Latin.
        }

        // Load the Latin fallback if necessary
        boolean hasNull = false;
        for (String entry : numberElements) {
            if (entry == null) {
                hasNull = true;
                break;
            }
        }
        if (hasNull && !nsName.equals(LATIN_NUMBERING_SYSTEM)) {
            rb.getAllItemsWithFallback(NUMBER_ELEMENTS + "/" + LATIN_NUMBERING_SYSTEM + "/" + SYMBOLS, sink);
        }

        // Fill in any remaining missing values
        for (int i = 0; i < SYMBOL_KEYS.length; i++) {
            if (numberElements[i] == null) {
                numberElements[i] = SYMBOL_DEFAULTS[i];
            }
        }

        // If monetary decimal or grouping were not explicitly set, then set them to be the same as
        // their non-monetary counterparts.
        if (numberElements[10] == null) {
            numberElements[10] = numberElements[0];
        }
        if (numberElements[11] == null) {
            numberElements[11] = numberElements[1];
        }

        return new CacheData(validLocale, digits, numberElements);
    }

    private void initSpacingInfo(CurrencySpacingInfo spcInfo) {
        currencySpcBeforeSym = spcInfo.getBeforeSymbols();
        currencySpcAfterSym = spcInfo.getAfterSymbols();
    }

    /**
     * Reads the default serializable fields, then if <code>serialVersionOnStream</code>
     * is less than 1, initialize <code>monetarySeparator</code> to be
     * the same as <code>decimalSeparator</code> and <code>exponential</code>
     * to be 'E'.
     * Finally, sets serialVersionOnStream back to the maximum allowed value so that
     * default serialization will work properly if this object is streamed out again.
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {

        // TODO: it looks to me {dlf} that the serialization code was never updated
        // to handle the actual/valid ulocale fields.

        stream.defaultReadObject();
        ///CLOVER:OFF
        // we don't have data for these old serialized forms any more
        if (serialVersionOnStream < 1) {
            // Didn't have monetarySeparator or exponential field;
            // use defaults.
            monetarySeparator = decimalSeparator;
            exponential = 'E';
        }
        if (serialVersionOnStream < 2) {
            padEscape = DecimalFormat.PATTERN_PAD_ESCAPE;
            plusSign = DecimalFormat.PATTERN_PLUS_SIGN;
            exponentSeparator = String.valueOf(exponential);
            // Although we read the exponential field on stream to create the
            // exponentSeparator, we don't do the reverse, since scientific
            // notation isn't supported by the old classes, even though the
            // symbol is there.
        }
        ///CLOVER:ON
        if (serialVersionOnStream < 3) {
            // Resurrected objects from old streams will have no
            // locale.  There is no 100% fix for this.  A
            // 90% fix is to construct a mapping of data back to
            // locale, perhaps a hash of all our members.  This is
            // expensive and doesn't seem worth it.
            requestedLocale = Locale.getDefault();
        }
        if (serialVersionOnStream < 4) {
            // use same default behavior as for versions with no Locale
            ulocale = ULocale.forLocale(requestedLocale);
        }
        if (serialVersionOnStream < 5) {
            // use the same one for groupingSeparator
            monetaryGroupingSeparator = groupingSeparator;
        }
        if (serialVersionOnStream < 6) {
            // Set null to CurrencySpacing related fields.
            if (currencySpcBeforeSym == null) {
                currencySpcBeforeSym = new String[CURRENCY_SPC_INSERT+1];
            }
            if (currencySpcAfterSym == null) {
                currencySpcAfterSym = new String[CURRENCY_SPC_INSERT+1];
            }
            initSpacingInfo(CurrencyData.CurrencySpacingInfo.DEFAULT);
        }
        if (serialVersionOnStream < 7) {
            // Set minusString,plusString from minusSign,plusSign
            if (minusString == null) {
                minusString = String.valueOf(minusSign);
            }
            if (plusString == null) {
                plusString = String.valueOf(plusSign);
            }
        }
        if (serialVersionOnStream < 8) {
            if (exponentMultiplicationSign == null) {
                exponentMultiplicationSign = "\u00D7";
            }
        }
        if (serialVersionOnStream < 9) {
            // String version of digits
            if (digitStrings == null) {
                digitStrings = new String[10];
                if (digits != null && digits.length == 10) {
                    zeroDigit = digits[0];
                    for (int i = 0; i < 10; i++) {
                        digitStrings[i] = String.valueOf(digits[i]);
                    }
                } else {
                    char digit = zeroDigit;
                    if (digits == null) {
                        digits = new char[10];
                    }
                    for (int i = 0; i < 10; i++) {
                        digits[i] = digit;
                        digitStrings[i] = String.valueOf(digit);
                        digit++;
                    }
                }
            }

            // String version of symbols
            if (decimalSeparatorString == null) {
                decimalSeparatorString = String.valueOf(decimalSeparator);
            }
            if (groupingSeparatorString == null) {
                groupingSeparatorString = String.valueOf(groupingSeparator);
            }
            if (percentString == null) {
                percentString = String.valueOf(percentString);
            }
            if (perMillString == null) {
                perMillString = String.valueOf(perMill);
            }
            if (monetarySeparatorString == null) {
                monetarySeparatorString = String.valueOf(monetarySeparator);
            }
            if (monetaryGroupingSeparatorString == null) {
                monetaryGroupingSeparatorString = String.valueOf(monetaryGroupingSeparator);
            }
        }

        serialVersionOnStream = currentSerialVersion;

    // recreate
    currency = Currency.getInstance(intlCurrencySymbol);
    }

    /**
     * Character used for zero.  This remains only for backward compatibility
     * purposes.  The digits array below is now used to actively store the digits.
     *
     * @serial
     * @see #getZeroDigit
     */
    private  char    zeroDigit;

    /**
     * Array of characters used for the digits 0-9 in order.
     */
    private  char    digits[];

    /**
     * Array of Strings used for the digits 0-9 in order.
     * @serial
     */
    private String digitStrings[];

    /**
     * Character used for thousands separator.
     *
     * @serial
     * @see #getGroupingSeparator
     */
    private  char    groupingSeparator;

    /**
     * String used for thousands separator.
     * @serial
     */
    private String groupingSeparatorString;

    /**
     * Character used for decimal sign.
     *
     * @serial
     * @see #getDecimalSeparator
     */
    private  char    decimalSeparator;

    /**
     * String used for decimal sign.
     * @serial
     */
    private String decimalSeparatorString;

    /**
     * Character used for mille percent sign.
     *
     * @serial
     * @see #getPerMill
     */
    private  char    perMill;

    /**
     * String used for mille percent sign.
     * @serial
     */
    private String perMillString;

    /**
     * Character used for percent sign.
     * @serial
     * @see #getPercent
     */
    private  char    percent;

    /**
     * String used for percent sign.
     * @serial
     */
    private String percentString;

    /**
     * Character used for a digit in a pattern.
     *
     * @serial
     * @see #getDigit
     */
    private  char    digit;

    /**
     * Character used for a significant digit in a pattern.
     *
     * @serial
     * @see #getSignificantDigit
     */
    private  char    sigDigit;

    /**
     * Character used to separate positive and negative subpatterns
     * in a pattern.
     *
     * @serial
     * @see #getPatternSeparator
     */
    private  char    patternSeparator;

    /**
     * Character used to represent infinity.
     * @serial
     * @see #getInfinity
     */
    private  String  infinity;

    /**
     * Character used to represent NaN.
     * @serial
     * @see #getNaN
     */
    private  String  NaN;

    /**
     * Character used to represent minus sign.
     * @serial
     * @see #getMinusSign
     */
    private  char    minusSign;

    /**
     * String versions of minus sign.
     * @serial
     */
    private String minusString;

    /**
     * The character used to indicate a plus sign.
     * @serial
     */
    private char plusSign;

    /**
     * String versions of plus sign.
     * @serial
     */
    private String plusString;

    /**
     * String denoting the local currency, e.g. "$".
     * @serial
     * @see #getCurrencySymbol
     */
    private  String  currencySymbol;

    /**
     * International string denoting the local currency, e.g. "USD".
     * @serial
     * @see #getInternationalCurrencySymbol
     */
    private  String  intlCurrencySymbol;

    /**
     * The decimal separator character used when formatting currency values.
     * @serial
     * @see #getMonetaryDecimalSeparator
     */
    private  char    monetarySeparator; // Field new in JDK 1.1.6

    /**
     * The decimal separator string used when formatting currency values.
     * @serial
     */
    private String monetarySeparatorString;

    /**
     * The grouping separator character used when formatting currency values.
     * @serial
     * @see #getMonetaryGroupingSeparator
     */
    private  char    monetaryGroupingSeparator; // Field new in JDK 1.1.6

    /**
     * The grouping separator string used when formatting currency values.
     * @serial
     */
    private String monetaryGroupingSeparatorString;

    /**
     * The character used to distinguish the exponent in a number formatted
     * in exponential notation, e.g. 'E' for a number such as "1.23E45".
     * <p>
     * Note that this field has been superseded by <code>exponentSeparator</code>.
     * It is retained for backward compatibility.
     *
     * @serial
     */
    private  char    exponential;       // Field new in JDK 1.1.6

    /**
     * The string used to separate the mantissa from the exponent.
     * Examples: "x10^" for 1.23x10^4, "E" for 1.23E4.
     * <p>
     * Note that this supersedes the <code>exponential</code> field.
     *
     * @serial
     */
    private String exponentSeparator;

    /**
     * The character used to indicate a padding character in a format,
     * e.g., '*' in a pattern such as "$*_#,##0.00".
     * @serial
     */
    private char padEscape;

    /**
     * The locale for which this object was constructed.  Set to the
     * default locale for objects resurrected from old streams.
     */
    private Locale requestedLocale;

    /**
     * The requested ULocale.  We keep the old locale for serialization compatibility.
     */
    private ULocale ulocale;

    /**
     * Exponent multiplication sign. e.g "x"
     * @serial
     */
    private String exponentMultiplicationSign = null;

    // Proclaim JDK 1.1 FCS compatibility
    private static final long serialVersionUID = 5772796243397350300L;

    // The internal serial version which says which version was written
    // - 0 (default) for version up to JDK 1.1.5
    // - 1 for version from JDK 1.1.6, which includes two new fields:
    //     monetarySeparator and exponential.
    // - 2 for version from AlphaWorks, which includes 3 new fields:
    //     padEscape, exponentSeparator, and plusSign.
    // - 3 for ICU 2.2, which includes the locale field
    // - 4 for ICU 3.2, which includes the ULocale field
    // - 5 for ICU 3.6, which includes the monetaryGroupingSeparator field
    // - 6 for ICU 4.2, which includes the currencySpc* fields
    // - 7 for ICU 52, which includes the minusString and plusString fields
    // - 8 for ICU 54, which includes exponentMultiplicationSign field.
    // - 9 for ICU 58, which includes a series of String symbol fields.
    private static final int currentSerialVersion = 8;

    /**
     * Describes the version of <code>DecimalFormatSymbols</code> present on the stream.
     * Possible values are:
     * <ul>
     * <li><b>0</b> (or uninitialized): versions prior to JDK 1.1.6.
     *
     * <li><b>1</b>: Versions written by JDK 1.1.6 or later, which includes
     *      two new fields: <code>monetarySeparator</code> and <code>exponential</code>.
     * <li><b>2</b>: Version for AlphaWorks.  Adds padEscape, exponentSeparator,
     *      and plusSign.
     * <li><b>3</b>: Version for ICU 2.2, which adds locale.
     * <li><b>4</b>: Version for ICU 3.2, which adds ulocale.
     * <li><b>5</b>: Version for ICU 3.6, which adds monetaryGroupingSeparator.
     * <li><b>6</b>: Version for ICU 4.2, which adds currencySpcBeforeSym and
     *      currencySpcAfterSym.
     * <li><b>7</b>: Version for ICU 52, which adds minusString and plusString.
     * </ul>
     * When streaming out a <code>DecimalFormatSymbols</code>, the most recent format
     * (corresponding to the highest allowable <code>serialVersionOnStream</code>)
     * is always written.
     *
     * @serial
     */
    private int serialVersionOnStream = currentSerialVersion;

    /**
     * cache to hold the NumberElements of a Locale.
     */
    private static final CacheBase<ULocale, CacheData, Void> cachedLocaleData =
        new SoftCache<ULocale, CacheData, Void>() {
            @Override
            protected CacheData createInstance(ULocale locale, Void unused) {
                return DecimalFormatSymbols.loadData(locale);
            }
        };

    /**
     *
     */
    private String  currencyPattern = null;

    // -------- BEGIN ULocale boilerplate --------

    /**
     * <strong>[icu]</strong> Returns the locale that was used to create this object, or null.
     * This may may differ from the locale requested at the time of
     * this object's creation.  For example, if an object is created
     * for locale <tt>en_US_CALIFORNIA</tt>, the actual data may be
     * drawn from <tt>en</tt> (the <i>actual</i> locale), and
     * <tt>en_US</tt> may be the most specific locale that exists (the
     * <i>valid</i> locale).
     *
     * <p>Note: The <i>actual</i> locale is returned correctly, but the <i>valid</i>
     * locale is not, in most cases.
     * @param type type of information requested, either {@link
     * android.icu.util.ULocale#VALID_LOCALE} or {@link
     * android.icu.util.ULocale#ACTUAL_LOCALE}.
     * @return the information specified by <i>type</i>, or null if
     * this object was not constructed from locale data.
     * @see android.icu.util.ULocale
     * @see android.icu.util.ULocale#VALID_LOCALE
     * @see android.icu.util.ULocale#ACTUAL_LOCALE
     * @hide draft / provisional / internal are hidden on Android
     */
    public final ULocale getLocale(ULocale.Type type) {
        return type == ULocale.ACTUAL_LOCALE ?
            this.actualLocale : this.validLocale;
    }

    /**
     * <strong>[icu]</strong> Sets information about the locales that were used to create this
     * object.  If the object was not constructed from locale data,
     * both arguments should be set to null.  Otherwise, neither
     * should be null.  The actual locale must be at the same level or
     * less specific than the valid locale.  This method is intended
     * for use by factories or other entities that create objects of
     * this class.
     * @param valid the most specific locale containing any resource
     * data, or null
     * @param actual the locale containing data used to construct this
     * object, or null
     * @see android.icu.util.ULocale
     * @see android.icu.util.ULocale#VALID_LOCALE
     * @see android.icu.util.ULocale#ACTUAL_LOCALE
     */
    final void setLocale(ULocale valid, ULocale actual) {
        // Change the following to an assertion later
        if ((valid == null) != (actual == null)) {
            ///CLOVER:OFF
            throw new IllegalArgumentException();
            ///CLOVER:ON
        }
        // Another check we could do is that the actual locale is at
        // the same level or less specific than the valid locale.
        this.validLocale = valid;
        this.actualLocale = actual;
    }

    /**
     * The most specific locale containing any resource data, or null.
     * @see android.icu.util.ULocale
     */
    private ULocale validLocale;

    /**
     * The locale containing data used to construct this object, or
     * null.
     * @see android.icu.util.ULocale
     */
    private ULocale actualLocale;

    // not serialized, reconstructed from intlCurrencyCode
    private transient Currency currency;

    // -------- END ULocale boilerplate --------

    private static class CacheData {
        final ULocale validLocale;
        final String[] digits;
        final String[] numberElements;

        public CacheData(ULocale loc, String[] digits, String[] numberElements) {
            validLocale = loc;
            this.digits = digits;
            this.numberElements = numberElements;
        }
    }
}
