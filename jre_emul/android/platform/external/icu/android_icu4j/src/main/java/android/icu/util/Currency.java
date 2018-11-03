/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.util;

import java.io.ObjectStreamException;
import java.lang.ref.SoftReference;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import android.icu.impl.CacheBase;
import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUDebug;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SoftCache;
import android.icu.impl.TextTrieMap;
import android.icu.text.CurrencyDisplayNames;
import android.icu.text.CurrencyMetaInfo;
import android.icu.text.CurrencyMetaInfo.CurrencyDigits;
import android.icu.text.CurrencyMetaInfo.CurrencyFilter;
import android.icu.util.ULocale.Category;

/**
 * A class encapsulating a currency, as defined by ISO 4217.  A
 * <tt>Currency</tt> object can be created given a <tt>Locale</tt> or
 * given an ISO 4217 code.  Once created, the <tt>Currency</tt> object
 * can return various data necessary to its proper display:
 *
 * <ul><li>A display symbol, for a specific locale
 * <li>The number of fraction digits to display
 * <li>A rounding increment
 * </ul>
 *
 * The <tt>DecimalFormat</tt> class uses these data to display
 * currencies.
 *
 * <p>Note: This class deliberately resembles
 * <tt>java.util.Currency</tt> but it has a completely independent
 * implementation, and adds features not present in the JDK.
 * @author Alan Liu
 */
public class Currency extends MeasureUnit {
    private static final long serialVersionUID = -5839973855554750484L;
    private static final boolean DEBUG = ICUDebug.enabled("currency");

    // Cache to save currency name trie
    private static ICUCache<ULocale, List<TextTrieMap<CurrencyStringInfo>>> CURRENCY_NAME_CACHE =
        new SimpleCache<ULocale, List<TextTrieMap<CurrencyStringInfo>>>();

    /**
     * Selector for getName() indicating a symbolic name for a
     * currency, such as "$" for USD.
     */
    public static final int SYMBOL_NAME = 0;

    /**
     * Selector for getName() indicating the long name for a
     * currency, such as "US Dollar" for USD.
     */
    public static final int LONG_NAME = 1;

    /**
     * Selector for getName() indicating the plural long name for a
     * currency, such as "US dollar" for USD in "1 US dollar",
     * and "US dollars" for USD in "2 US dollars".
     */
    public static final int PLURAL_LONG_NAME = 2;

    private static final EquivalenceRelation<String> EQUIVALENT_CURRENCY_SYMBOLS =
            new EquivalenceRelation<String>()
            .add("\u00a5", "\uffe5")
            .add("$", "\ufe69", "\uff04")
            .add("\u20a8", "\u20b9")
            .add("\u00a3", "\u20a4");

    /**
     * Currency Usage used for Decimal Format
     */
    public enum CurrencyUsage{
        /**
         * a setting to specify currency usage which determines currency digit and rounding
         * for standard usage, for example: "50.00 NT$"
         */
        STANDARD,

        /**
         * a setting to specify currency usage which determines currency digit and rounding
         * for cash usage, for example: "50 NT$"
         */
        CASH
    }

    // begin registry stuff

    // shim for service code
    /* package */ static abstract class ServiceShim {
        abstract ULocale[] getAvailableULocales();
        abstract Locale[] getAvailableLocales();
        abstract Currency createInstance(ULocale l);
        abstract Object registerInstance(Currency c, ULocale l);
        abstract boolean unregister(Object f);
    }

    private static ServiceShim shim;
    private static ServiceShim getShim() {
        // Note: this instantiation is safe on loose-memory-model configurations
        // despite lack of synchronization, since the shim instance has no state--
        // it's all in the class init.  The worst problem is we might instantiate
        // two shim instances, but they'll share the same state so that's ok.
        if (shim == null) {
            try {
                Class<?> cls = Class.forName("android.icu.util.CurrencyServiceShim");
                shim = (ServiceShim)cls.newInstance();
            }
            catch (Exception e) {
                if(DEBUG){
                    e.printStackTrace();
                }
                throw new RuntimeException(e.getMessage());
            }
        }
        return shim;
    }

    /**
     * Returns a currency object for the default currency in the given
     * locale.
     * @param locale the locale
     * @return the currency object for this locale
     */
    public static Currency getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    /**
     * Returns a currency object for the default currency in the given
     * locale.
     */
    public static Currency getInstance(ULocale locale) {
        String currency = locale.getKeywordValue("currency");
        if (currency != null) {
            return getInstance(currency);
        }

        if (shim == null) {
            return createCurrency(locale);
        }

        return shim.createInstance(locale);
    }

    /**
     * Returns an array of Strings which contain the currency
     * identifiers that are valid for the given locale on the
     * given date.  If there are no such identifiers, returns null.
     * Returned identifiers are in preference order.
     * @param loc the locale for which to retrieve currency codes.
     * @param d the date for which to retrieve currency codes for the given locale.
     * @return The array of ISO currency codes.
     */
    public static String[] getAvailableCurrencyCodes(ULocale loc, Date d) {
        String region = ULocale.getRegionForSupplementalData(loc, false);
        CurrencyFilter filter = CurrencyFilter.onDate(d).withRegion(region);
        List<String> list = getTenderCurrencies(filter);
        // Note: Prior to 4.4 the spec didn't say that we return null if there are no results, but
        // the test assumed it did.  Kept the behavior and amended the spec.
        if (list.isEmpty()) {
            return null;
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns an array of Strings which contain the currency
     * identifiers that are valid for the given {@link java.util.Locale} on the
     * given date.  If there are no such identifiers, returns null.
     * Returned identifiers are in preference order.
     * @param loc the {@link java.util.Locale} for which to retrieve currency codes.
     * @param d the date for which to retrieve currency codes for the given locale.
     * @return The array of ISO currency codes.
     */
    public static String[] getAvailableCurrencyCodes(Locale loc, Date d) {
        return getAvailableCurrencyCodes(ULocale.forLocale(loc), d);
    }

    /**
     * Returns the set of available currencies. The returned set of currencies contains all of the
     * available currencies, including obsolete ones. The result set can be modified without
     * affecting the available currencies in the runtime.
     *
     * @return The set of available currencies. The returned set could be empty if there is no
     * currency data available.
     */
    public static Set<Currency> getAvailableCurrencies() {
        CurrencyMetaInfo info = CurrencyMetaInfo.getInstance();
        List<String> list = info.currencies(CurrencyFilter.all());
        HashSet<Currency> resultSet = new HashSet<Currency>(list.size());
        for (String code : list) {
            resultSet.add(getInstance(code));
        }
        return resultSet;
    }

    private static final String EUR_STR = "EUR";
    private static final CacheBase<String, Currency, Void> regionCurrencyCache =
            new SoftCache<String, Currency, Void>() {
        @Override
        protected Currency createInstance(String key, Void unused) {
            return loadCurrency(key);
        }
    };

    /**
     * Instantiate a currency from resource data.
     */
    /* package */ static Currency createCurrency(ULocale loc) {
        String variant = loc.getVariant();
        if ("EURO".equals(variant)) {
            return getInstance(EUR_STR);
        }

        // Cache the currency by region, and whether variant=PREEURO.
        // Minimizes the size of the cache compared with caching by ULocale.
        String key = ULocale.getRegionForSupplementalData(loc, false);
        if ("PREEURO".equals(variant)) {
            key = key + '-';
        }
        return regionCurrencyCache.getInstance(key, null);
    }

    private static Currency loadCurrency(String key) {
        String region;
        boolean isPreEuro;
        if (key.endsWith("-")) {
            region = key.substring(0, key.length() - 1);
            isPreEuro = true;
        } else {
            region = key;
            isPreEuro = false;
        }
        CurrencyMetaInfo info = CurrencyMetaInfo.getInstance();
        List<String> list = info.currencies(CurrencyFilter.onRegion(region));
        if (!list.isEmpty()) {
            String code = list.get(0);
            if (isPreEuro && EUR_STR.equals(code)) {
                if (list.size() < 2) {
                    return null;
                }
                code = list.get(1);
            }
            return getInstance(code);
        }
        return null;
    }

    /**
     * Returns a currency object given an ISO 4217 3-letter code.
     * @param theISOCode the iso code
     * @return the currency for this iso code
     * @throws NullPointerException if <code>theISOCode</code> is null.
     * @throws IllegalArgumentException if <code>theISOCode</code> is not a
     *         3-letter alpha code.
     */
    public static Currency getInstance(String theISOCode) {
        if (theISOCode == null) {
            throw new NullPointerException("The input currency code is null.");
        }
        if (!isAlpha3Code(theISOCode)) {
            throw new IllegalArgumentException(
                    "The input currency code is not 3-letter alphabetic code.");
        }
        return (Currency) MeasureUnit.internalGetInstance("currency", theISOCode.toUpperCase(Locale.ENGLISH));
    }


    private static boolean isAlpha3Code(String code) {
        if (code.length() != 3) {
            return false;
        } else {
            for (int i = 0; i < 3; i++) {
                char ch = code.charAt(i);
                if (ch < 'A' || (ch > 'Z' && ch < 'a') || ch > 'z') {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Registers a new currency for the provided locale.  The returned object
     * is a key that can be used to unregister this currency object.
     *
     * <p>Because ICU may choose to cache Currency objects internally, this must
     * be called at application startup, prior to any calls to
     * Currency.getInstance to avoid undefined behavior.
     *
     * @param currency the currency to register
     * @param locale the ulocale under which to register the currency
     * @return a registry key that can be used to unregister this currency
     * @see #unregister
     * @hide unsupported on Android
     */
    public static Object registerInstance(Currency currency, ULocale locale) {
        return getShim().registerInstance(currency, locale);
    }

    /**
     * Unregister the currency associated with this key (obtained from
     * registerInstance).
     * @param registryKey the registry key returned from registerInstance
     * @see #registerInstance
     * @hide unsupported on Android
     */
    public static boolean unregister(Object registryKey) {
        if (registryKey == null) {
            throw new IllegalArgumentException("registryKey must not be null");
        }
        if (shim == null) {
            return false;
        }
        return shim.unregister(registryKey);
    }

    /**
     * Return an array of the locales for which a currency
     * is defined.
     * @return an array of the available locales
     */
    public static Locale[] getAvailableLocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableLocales();
        } else {
            return shim.getAvailableLocales();
        }
    }

    /**
     * Return an array of the ulocales for which a currency
     * is defined.
     * @return an array of the available ulocales
     */
    public static ULocale[] getAvailableULocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableULocales();
        } else {
            return shim.getAvailableULocales();
        }
    }

    // end registry stuff

    /**
     * Given a key and a locale, returns an array of values for the key for which data
     * exists.  If commonlyUsed is true, these are the values that typically are used
     * with this locale, otherwise these are all values for which data exists.
     * This is a common service API.
     * <p>
     * The only supported key is "currency", other values return an empty array.
     * <p>
     * Currency information is based on the region of the locale.  If the locale does not
     * indicate a region, {@link ULocale#addLikelySubtags(ULocale)} is used to infer a region,
     * except for the 'und' locale.
     * <p>
     * If commonlyUsed is true, only the currencies known to be in use as of the current date
     * are returned.  When there are more than one, these are returned in preference order
     * (typically, this occurs when a country is transitioning to a new currency, and the
     * newer currency is preferred), see
     * <a href="http://unicode.org/reports/tr35/#Supplemental_Currency_Data">Unicode TR#35 Sec. C1</a>.
     * If commonlyUsed is false, all currencies ever used in any locale are returned, in no
     * particular order.
     *
     * @param key           key whose values to look up.  the only recognized key is "currency"
     * @param locale        the locale
     * @param commonlyUsed  if true, return only values that are currently used in the locale.
     *                      Otherwise returns all values.
     * @return an array of values for the given key and the locale.  If there is no data, the
     *   array will be empty.
     */
    public static final String[] getKeywordValuesForLocale(String key, ULocale locale,
            boolean commonlyUsed) {

        // The only keyword we recognize is 'currency'
        if (!"currency".equals(key)) {
            return EMPTY_STRING_ARRAY;
        }

        if (!commonlyUsed) {
            // Behavior change from 4.3.3, no longer sort the currencies
            return getAllTenderCurrencies().toArray(new String[0]);
        }

        // Don't resolve region if the requested locale is 'und', it will resolve to US
        // which we don't want.
        if (UND.equals(locale)) {
            return EMPTY_STRING_ARRAY;
        }
        String prefRegion = ULocale.getRegionForSupplementalData(locale, true);

        CurrencyFilter filter = CurrencyFilter.now().withRegion(prefRegion);

        // currencies are in region's preferred order when we're filtering on region, which
        // matches our spec
        List<String> result = getTenderCurrencies(filter);

        // No fallback anymore (change from 4.3.3)
        if (result.size() == 0) {
            return EMPTY_STRING_ARRAY;
        }

        return result.toArray(new String[result.size()]);
    }

    private static final ULocale UND = new ULocale("und");
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Returns the ISO 4217 3-letter code for this currency object.
     */
    public String getCurrencyCode() {
        return subType;
    }

    /**
     * Returns the ISO 4217 numeric code for this currency object.
     * <p>Note: If the ISO 4217 numeric code is not assigned for the currency or
     * the currency is unknown, this method returns 0.</p>
     * @return The ISO 4217 numeric code of this currency.
     */
    public int getNumericCode() {
        int result = 0;
        try {
            UResourceBundle bundle = UResourceBundle.getBundleInstance(
                    ICUData.ICU_BASE_NAME,
                    "currencyNumericCodes",
                    ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            UResourceBundle codeMap = bundle.get("codeMap");
            UResourceBundle numCode = codeMap.get(subType);
            result = numCode.getInt();
        } catch (MissingResourceException e) {
            // fall through
        }
        return result;
    }

    /**
     * Convenience and compatibility override of getName that
     * requests the symbol name for the default <code>DISPLAY</code> locale.
     * @see #getName
     * @see Category#DISPLAY
     */
    public String getSymbol() {
        return getSymbol(ULocale.getDefault(Category.DISPLAY));
    }

    /**
     * Convenience and compatibility override of getName that
     * requests the symbol name.
     * @param loc the Locale for the symbol
     * @see #getName
     */
    public String getSymbol(Locale loc) {
        return getSymbol(ULocale.forLocale(loc));
    }

    /**
     * Convenience and compatibility override of getName that
     * requests the symbol name.
     * @param uloc the ULocale for the symbol
     * @see #getName
     */
    public String getSymbol(ULocale uloc) {
        return getName(uloc, SYMBOL_NAME, new boolean[1]);
    }

    /**
     * Returns the display name for the given currency in the
     * given locale.
     * This is a convenient method for
     * getName(ULocale, int, boolean[]);
     */
    public String getName(Locale locale,
                          int nameStyle,
                          boolean[] isChoiceFormat) {
        return getName(ULocale.forLocale(locale), nameStyle, isChoiceFormat);
    }

    /**
     * Returns the display name for the given currency in the
     * given locale.  For example, the display name for the USD
     * currency object in the en_US locale is "$".
     * @param locale locale in which to display currency
     * @param nameStyle selector for which kind of name to return.
     *                  The nameStyle should be either SYMBOL_NAME or
     *                  LONG_NAME. Otherwise, throw IllegalArgumentException.
     * @param isChoiceFormat fill-in; isChoiceFormat[0] is set to true
     * if the returned value is a ChoiceFormat pattern; otherwise it
     * is set to false
     * @return display string for this currency.  If the resource data
     * contains no entry for this currency, then the ISO 4217 code is
     * returned.  If isChoiceFormat[0] is true, then the result is a
     * ChoiceFormat pattern.  Otherwise it is a static string. <b>Note:</b>
     * as of ICU 4.4, choice formats are not used, and the value returned
     * in isChoiceFormat is always false.
     * <p>
     * @throws  IllegalArgumentException  if the nameStyle is not SYMBOL_NAME
     *                                    or LONG_NAME.
     * @see #getName(ULocale, int, String, boolean[])
     */
    public String getName(ULocale locale, int nameStyle, boolean[] isChoiceFormat) {
        if (!(nameStyle == SYMBOL_NAME || nameStyle == LONG_NAME)) {
            throw new IllegalArgumentException("bad name style: " + nameStyle);
        }

        // We no longer support choice format data in names.  Data should not contain
        // choice patterns.
        if (isChoiceFormat != null) {
            isChoiceFormat[0] = false;
        }

        CurrencyDisplayNames names = CurrencyDisplayNames.getInstance(locale);
        return nameStyle == SYMBOL_NAME ? names.getSymbol(subType) : names.getName(subType);
    }

    /**
     * Returns the display name for the given currency in the given locale.
     * This is a convenience overload of getName(ULocale, int, String, boolean[]);
     */
    public String getName(Locale locale, int nameStyle, String pluralCount,
            boolean[] isChoiceFormat) {
        return getName(ULocale.forLocale(locale), nameStyle, pluralCount, isChoiceFormat);
    }

    /**
     * Returns the display name for the given currency in the
     * given locale.  For example, the SYMBOL_NAME for the USD
     * currency object in the en_US locale is "$".
     * The PLURAL_LONG_NAME for the USD currency object when the currency
     * amount is plural is "US dollars", such as in "3.00 US dollars";
     * while the PLURAL_LONG_NAME for the USD currency object when the currency
     * amount is singular is "US dollar", such as in "1.00 US dollar".
     * @param locale locale in which to display currency
     * @param nameStyle selector for which kind of name to return
     * @param pluralCount plural count string for this locale
     * @param isChoiceFormat fill-in; isChoiceFormat[0] is set to true
     * if the returned value is a ChoiceFormat pattern; otherwise it
     * is set to false
     * @return display string for this currency.  If the resource data
     * contains no entry for this currency, then the ISO 4217 code is
     * returned.  If isChoiceFormat[0] is true, then the result is a
     * ChoiceFormat pattern.  Otherwise it is a static string. <b>Note:</b>
     * as of ICU 4.4, choice formats are not used, and the value returned
     * in isChoiceFormat is always false.
     * @throws  IllegalArgumentException  if the nameStyle is not SYMBOL_NAME,
     *                                    LONG_NAME, or PLURAL_LONG_NAME.
     */
    public String getName(ULocale locale, int nameStyle, String pluralCount,
            boolean[] isChoiceFormat) {
        if (nameStyle != PLURAL_LONG_NAME) {
            return getName(locale, nameStyle, isChoiceFormat);
        }

        // We no longer support choice format
        if (isChoiceFormat != null) {
            isChoiceFormat[0] = false;
        }

        CurrencyDisplayNames names = CurrencyDisplayNames.getInstance(locale);
        return names.getPluralName(subType, pluralCount);
    }

    /**
     * Returns the display name for this currency in the default locale.
     * If the resource data for the default locale contains no entry for this currency,
     * then the ISO 4217 code is returned.
     * <p>
     * Note: This method is a convenience equivalent for
     * {@link java.util.Currency#getDisplayName()} and is equivalent to
     * <code>getName(Locale.getDefault(), LONG_NAME, null)</code>.
     *
     * @return The display name of this currency
     * @see #getDisplayName(Locale)
     * @see #getName(Locale, int, boolean[])
     */
    @SuppressWarnings("javadoc")    // java.util.Currency#getDisplayName() is introduced in Java 7
    public String getDisplayName() {
        return getName(Locale.getDefault(), LONG_NAME, null);
    }

    /**
     * Returns the display name for this currency in the given locale.
     * If the resource data for the given locale contains no entry for this currency,
     * then the ISO 4217 code is returned.
     * <p>
     * Note: This method is a convenience equivalent for
     * {@link java.util.Currency#getDisplayName(java.util.Locale)} and is equivalent
     * to <code>getName(locale, LONG_NAME, null)</code>.
     *
     * @param locale locale in which to display currency
     * @return The display name of this currency for the specified locale
     * @see #getDisplayName(Locale)
     * @see #getName(Locale, int, boolean[])
     */
    @SuppressWarnings("javadoc")    // java.util.Currency#getDisplayName() is introduced in Java 7
    public String getDisplayName(Locale locale) {
        return getName(locale, LONG_NAME, null);
    }

    /**
     * Attempt to parse the given string as a currency, either as a
     * display name in the given locale, or as a 3-letter ISO 4217
     * code.  If multiple display names match, then the longest one is
     * selected.  If both a display name and a 3-letter ISO code
     * match, then the display name is preferred, unless it's length
     * is less than 3.
     *
     * @param locale the locale of the display names to match
     * @param text the text to parse
     * @param type parse against currency type: LONG_NAME only or not
     * @param pos input-output position; on input, the position within
     * text to match; must have 0 &lt;= pos.getIndex() &lt; text.length();
     * on output, the position after the last matched character. If
     * the parse fails, the position in unchanged upon output.
     * @return the ISO 4217 code, as a string, of the best match, or
     * null if there is no match
     *
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static String parse(ULocale locale, String text, int type, ParsePosition pos) {
        List<TextTrieMap<CurrencyStringInfo>> currencyTrieVec = CURRENCY_NAME_CACHE.get(locale);
        if (currencyTrieVec == null) {
            TextTrieMap<CurrencyStringInfo> currencyNameTrie =
                new TextTrieMap<CurrencyStringInfo>(true);
            TextTrieMap<CurrencyStringInfo> currencySymbolTrie =
                new TextTrieMap<CurrencyStringInfo>(false);
            currencyTrieVec = new ArrayList<TextTrieMap<CurrencyStringInfo>>();
            currencyTrieVec.add(currencySymbolTrie);
            currencyTrieVec.add(currencyNameTrie);
            setupCurrencyTrieVec(locale, currencyTrieVec);
            CURRENCY_NAME_CACHE.put(locale, currencyTrieVec);
        }

        int maxLength = 0;
        String isoResult = null;

          // look for the names
        TextTrieMap<CurrencyStringInfo> currencyNameTrie = currencyTrieVec.get(1);
        CurrencyNameResultHandler handler = new CurrencyNameResultHandler();
        currencyNameTrie.find(text, pos.getIndex(), handler);
        isoResult = handler.getBestCurrencyISOCode();
        maxLength = handler.getBestMatchLength();

        if (type != Currency.LONG_NAME) {  // not long name only
            TextTrieMap<CurrencyStringInfo> currencySymbolTrie = currencyTrieVec.get(0);
            handler = new CurrencyNameResultHandler();
            currencySymbolTrie.find(text, pos.getIndex(), handler);
            if (handler.getBestMatchLength() > maxLength) {
                isoResult = handler.getBestCurrencyISOCode();
                maxLength = handler.getBestMatchLength();
            }
        }
        int start = pos.getIndex();
        pos.setIndex(start + maxLength);
        return isoResult;
    }

    private static void setupCurrencyTrieVec(ULocale locale,
            List<TextTrieMap<CurrencyStringInfo>> trieVec) {

        TextTrieMap<CurrencyStringInfo> symTrie = trieVec.get(0);
        TextTrieMap<CurrencyStringInfo> trie = trieVec.get(1);

        CurrencyDisplayNames names = CurrencyDisplayNames.getInstance(locale);
        for (Map.Entry<String, String> e : names.symbolMap().entrySet()) {
            String symbol = e.getKey();
            String isoCode = e.getValue();
            // Register under not just symbol, but under every equivalent symbol as well
            // e.g short width yen and long width yen.
            for (String equivalentSymbol : EQUIVALENT_CURRENCY_SYMBOLS.get(symbol)) {
                symTrie.put(equivalentSymbol, new CurrencyStringInfo(isoCode, symbol));
            }
        }
        for (Map.Entry<String, String> e : names.nameMap().entrySet()) {
            String name = e.getKey();
            String isoCode = e.getValue();
            trie.put(name, new CurrencyStringInfo(isoCode, name));
        }
    }

    private static final class CurrencyStringInfo {
        private String isoCode;
        private String currencyString;

        public CurrencyStringInfo(String isoCode, String currencyString) {
            this.isoCode = isoCode;
            this.currencyString = currencyString;
        }

        public String getISOCode() {
            return isoCode;
        }

        @SuppressWarnings("unused")
        public String getCurrencyString() {
            return currencyString;
        }
    }

    private static class CurrencyNameResultHandler
            implements TextTrieMap.ResultHandler<CurrencyStringInfo> {
        // The length of longest matching key
        private int bestMatchLength;
        // The currency ISO code of longest matching key
        private String bestCurrencyISOCode;

        // As the trie is traversed, handlePrefixMatch is called at each node. matchLength is the
        // length length of the key at the current node; values is the list of all the values mapped to
        // that key. matchLength increases with each call as trie is traversed.
        @Override
        public boolean handlePrefixMatch(int matchLength, Iterator<CurrencyStringInfo> values) {
            if (values.hasNext()) {
                // Since the best match criteria is only based on length of key in trie and since all the
                // values are mapped to the same key, we only need to examine the first value.
                bestCurrencyISOCode = values.next().getISOCode();
                bestMatchLength = matchLength;
            }
            return true;
        }

        public String getBestCurrencyISOCode() {
            return bestCurrencyISOCode;
        }

        public int getBestMatchLength() {
            return bestMatchLength;
        }
    }

    /**
     * Returns the number of the number of fraction digits that should
     * be displayed for this currency.
     * This is equivalent to getDefaultFractionDigits(CurrencyUsage.STANDARD);
     * @return a non-negative number of fraction digits to be
     * displayed
     */
    public int getDefaultFractionDigits() {
        return getDefaultFractionDigits(CurrencyUsage.STANDARD);
    }

    /**
     * Returns the number of the number of fraction digits that should
     * be displayed for this currency with Usage.
     * @param Usage the usage of currency(Standard or Cash)
     * @return a non-negative number of fraction digits to be
     * displayed
     */
    public int getDefaultFractionDigits(CurrencyUsage Usage) {
        CurrencyMetaInfo info = CurrencyMetaInfo.getInstance();
        CurrencyDigits digits = info.currencyDigits(subType, Usage);
        return digits.fractionDigits;
    }

    /**
     * Returns the rounding increment for this currency, or 0.0 if no
     * rounding is done by this currency.
     * This is equivalent to getRoundingIncrement(CurrencyUsage.STANDARD);
     * @return the non-negative rounding increment, or 0.0 if none
     */
    public double getRoundingIncrement() {
        return getRoundingIncrement(CurrencyUsage.STANDARD);
    }

    /**
     * Returns the rounding increment for this currency, or 0.0 if no
     * rounding is done by this currency with the Usage.
     * @param Usage the usage of currency(Standard or Cash)
     * @return the non-negative rounding increment, or 0.0 if none
     */
    public double getRoundingIncrement(CurrencyUsage Usage) {
        CurrencyMetaInfo info = CurrencyMetaInfo.getInstance();
        CurrencyDigits digits = info.currencyDigits(subType, Usage);

        int data1 = digits.roundingIncrement;

        // If there is no rounding return 0.0 to indicate no rounding.
        // This is the high-runner case, by far.
        if (data1 == 0) {
            return 0.0;
        }

        int data0 = digits.fractionDigits;

        // If the meta data is invalid, return 0.0 to indicate no rounding.
        if (data0 < 0 || data0 >= POW10.length) {
            return 0.0;
        }

        // Return data[1] / 10^(data[0]). The only actual rounding data,
        // as of this writing, is CHF { 2, 25 }.
        return (double) data1 / POW10[data0];
    }

    /**
     * Returns the ISO 4217 code for this currency.
     */
    @Override
    public String toString() {
        return subType;
    }

    /**
     * Constructs a currency object for the given ISO 4217 3-letter
     * code.  This constructor assumes that the code is valid.
     *
     * @param theISOCode The iso code used to construct the currency.
     */
    protected Currency(String theISOCode) {
        super("currency", theISOCode);

        // isoCode is kept for readResolve() and Currency class no longer
        // use it. So this statement actually does not have any effect.
        isoCode = theISOCode;
    }

    // POW10[i] = 10^i
    private static final int[] POW10 = {
        1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000
    };


    private static SoftReference<List<String>> ALL_TENDER_CODES;
    private static SoftReference<Set<String>> ALL_CODES_AS_SET;
    /*
     * Returns an unmodifiable String list including all known tender currency codes.
     */
    private static synchronized List<String> getAllTenderCurrencies() {
        List<String> all = (ALL_TENDER_CODES == null) ? null : ALL_TENDER_CODES.get();
        if (all == null) {
            // Filter out non-tender currencies which have "from" date set to 9999-12-31
            // CurrencyFilter has "to" value set to 9998-12-31 in order to exclude them
            //CurrencyFilter filter = CurrencyFilter.onDateRange(null, new Date(253373299200000L));
            CurrencyFilter filter = CurrencyFilter.all();
            all = Collections.unmodifiableList(getTenderCurrencies(filter));
            ALL_TENDER_CODES = new SoftReference<List<String>>(all);
        }
        return all;
    }

    private static synchronized Set<String> getAllCurrenciesAsSet() {
        Set<String> all = (ALL_CODES_AS_SET == null) ? null : ALL_CODES_AS_SET.get();
        if (all == null) {
            CurrencyMetaInfo info = CurrencyMetaInfo.getInstance();
            all = Collections.unmodifiableSet(
                    new HashSet<String>(info.currencies(CurrencyFilter.all())));
            ALL_CODES_AS_SET = new SoftReference<Set<String>>(all);
        }
        return all;
    }

    /**
     * Queries if the given ISO 4217 3-letter code is available on the specified date range.
     * <p>
     * Note: For checking availability of a currency on a specific date, specify the date on both <code>from</code> and
     * <code>to</code>. When both <code>from</code> and <code>to</code> are null, this method checks if the specified
     * currency is available all time.
     *
     * @param code
     *            The ISO 4217 3-letter code.
     * @param from
     *            The lower bound of the date range, inclusive. When <code>from</code> is null, check the availability
     *            of the currency any date before <code>to</code>
     * @param to
     *            The upper bound of the date range, inclusive. When <code>to</code> is null, check the availability of
     *            the currency any date after <code>from</code>
     * @return true if the given ISO 4217 3-letter code is supported on the specified date range.
     * @throws IllegalArgumentException when <code>to</code> is before <code>from</code>.
     */
    public static boolean isAvailable(String code, Date from, Date to) {
        if (!isAlpha3Code(code)) {
            return false;
        }

        if (from != null && to != null && from.after(to)) {
            throw new IllegalArgumentException("To is before from");
        }

        code = code.toUpperCase(Locale.ENGLISH);
        boolean isKnown = getAllCurrenciesAsSet().contains(code);
        if (isKnown == false) {
            return false;
        } else if (from == null && to == null) {
            return true;
        }

        // If caller passed a date range, we cannot rely solely on the cache
        CurrencyMetaInfo info = CurrencyMetaInfo.getInstance();
        List<String> allActive = info.currencies(
                CurrencyFilter.onDateRange(from, to).withCurrency(code));
        return allActive.contains(code);
    }

    /**
     * Returns the list of remaining tender currencies after a filter is applied.
     * @param filter the filter to apply to the tender currencies
     * @return a list of tender currencies
     */
    private static List<String> getTenderCurrencies(CurrencyFilter filter) {
        CurrencyMetaInfo info = CurrencyMetaInfo.getInstance();
        return info.currencies(filter.withTender());
    }

    private static final class EquivalenceRelation<T> {

        private Map<T, Set<T>> data = new HashMap<T, Set<T>>();

        @SuppressWarnings("unchecked")  // See ticket #11395, this is safe.
        public EquivalenceRelation<T> add(T... items) {
            Set<T> group = new HashSet<T>();
            for (T item : items) {
                if (data.containsKey(item)) {
                    throw new IllegalArgumentException("All groups passed to add must be disjoint.");
                }
                group.add(item);
            }
            for (T item : items) {
                data.put(item, group);
            }
            return this;
        }

        public Set<T> get(T item) {
            Set<T> result = data.get(item);
            if (result == null) {
                return Collections.singleton(item);
            }
            return Collections.unmodifiableSet(result);
        }
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MeasureUnitProxy(type, subType);
    }

    // For backward compatibility only
    /**
     * ISO 4217 3-letter code.
     */
    private final String isoCode;

    private Object readResolve() throws ObjectStreamException {
        // The old isoCode field used to determine the currency.
        return Currency.getInstance(isoCode);
    }
}
//eof
