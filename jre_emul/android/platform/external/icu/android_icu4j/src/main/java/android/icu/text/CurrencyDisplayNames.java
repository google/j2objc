/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

import java.util.Locale;
import java.util.Map;

import android.icu.impl.CurrencyData;
import android.icu.util.ULocale;

/**
 * Returns currency names localized for a locale.
 * 
 * This class is not intended for public subclassing.
 * 
 * @hide Only a subset of ICU is exposed in Android
 */
public abstract class CurrencyDisplayNames {
    /**
     * Return an instance of CurrencyDisplayNames that provides information
     * localized for display in the provided locale.  If there is no data for the
     * provided locale, this falls back to the current default locale; if there
     * is no data for that either, it falls back to the root locale.  Substitute
     * values are returned from APIs when there is no data for the requested ISO 
     * code.
     * 
     * @param locale the locale into which to localize the names
     * @return a CurrencyDisplayNames
     */
    public static CurrencyDisplayNames getInstance(ULocale locale) {
        return CurrencyData.provider.getInstance(locale, true);
    }

    /**
     * Return an instance of CurrencyDisplayNames that provides information
     * localized for display in the provided locale.  If there is no data for the
     * provided locale, this falls back to the current default locale; if there
     * is no data for that either, it falls back to the root locale.  Substitute
     * values are returned from APIs when there is no data for the requested ISO 
     * code.
     * 
     * @param locale the locale into which to localize the names
     * @return a CurrencyDisplayNames
     */
    public static CurrencyDisplayNames getInstance(Locale locale) {
        return getInstance(locale, true);
    }

    /**
     * Return an instance of CurrencyDisplayNames that provides information
     * localized for display in the provided locale.  If noSubstitute is false,
     * this behaves like {@link #getInstance(ULocale)}.  Otherwise, 1) if there
     * is no supporting data for the locale at all, there is no fallback through
     * the default locale or root, and null is returned, and 2) if there is data
     * for the locale, but not data for the requested ISO code, null is returned
     * from those APIs instead of a substitute value.
     * 
     * @param locale the locale into which to localize the names
     * @param noSubstitute if true, do not return substitute values.
     * @return a CurrencyDisplayNames
     */
    public static CurrencyDisplayNames getInstance(ULocale locale, boolean noSubstitute) {
        return CurrencyData.provider.getInstance(locale, !noSubstitute);
    }

    /**
     * Return an instance of CurrencyDisplayNames that provides information
     * localized for display in the provided locale.  If noSubstitute is false,
     * this behaves like {@link #getInstance(Locale)}.  Otherwise, 1) if there
     * is no supporting data for the locale at all, there is no fallback through
     * the default locale or root, and null is returned, and 2) if there is data
     * for the locale, but not data for the requested ISO code, null is returned
     * from those APIs instead of a substitute value.
     * 
     * @param locale the {@link java.util.Locale} into which to localize the names
     * @param noSubstitute if true, do not return substitute values.
     * @return a CurrencyDisplayNames
     */
    public static CurrencyDisplayNames getInstance(Locale locale, boolean noSubstitute) {
        return getInstance(ULocale.forLocale(locale), noSubstitute);
    }

    /**
     * Returns true if currency display name data is available.
     * @return true if currency display name data is available
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static boolean hasData() {
        return CurrencyData.provider.hasData();
    }

    /**
     * Returns the locale used to determine how to translate the currency names.
     * This is not necessarily the same locale passed to {@link #getInstance(ULocale)}.
     * @return the display locale
     */
    public abstract ULocale getULocale();

    /**
     * Returns the symbol for the currency with the provided ISO code.  If
     * there is no data for the ISO code, substitutes isoCode or returns null.
     * 
     * @param isoCode the three-letter ISO code.
     * @return the display name.
     */
    public abstract String getSymbol(String isoCode);

    /**
     * Returns the 'long name' for the currency with the provided ISO code.
     * If there is no data for the ISO code, substitutes isoCode or returns null.
     * 
     * @param isoCode the three-letter ISO code
     * @return the display name
     */
    public abstract String getName(String isoCode);

    /**
     * Returns a 'plural name' for the currency with the provided ISO code corresponding to
     * the pluralKey.  If there is no data for the ISO code, substitutes isoCode or
     * returns null.  If there is data for the ISO code but no data for the plural key, 
     * substitutes the 'other' value (and failing that the isoCode) or returns null.
     * 
     * @param isoCode the three-letter ISO code
     * @param pluralKey the plural key, for example "one", "other"
     * @return the display name
     * @see android.icu.text.PluralRules
     */
    public abstract String getPluralName(String isoCode, String pluralKey);

    /**
     * Returns a mapping from localized symbols and currency codes to currency codes.
     * The returned map is unmodifiable.
     * @return the map
     */
    public abstract Map<String, String> symbolMap();

    /**
     * Returns a mapping from localized names (standard and plural) to currency codes.
     * The returned map is unmodifiable.
     * @return the map
     */
    public abstract Map<String, String> nameMap();

    /**
     * Sole constructor.  (For invocation by subclass constructors,
     * typically implicit.)
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected CurrencyDisplayNames() {
    }
}
