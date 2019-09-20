/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2004-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
*/
package android.icu.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import android.icu.impl.Utility;
import android.icu.text.BreakIterator;
import android.icu.text.Collator;
import android.icu.text.DateFormat;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;

/**
 * This convenience class provides a mechanism for bundling together different
 * globalization preferences. It includes:
 * <ul>
 * <li>A list of locales/languages in preference order</li>
 * <li>A territory</li>
 * <li>A currency</li>
 * <li>A timezone</li>
 * <li>A calendar</li>
 * <li>A collator (for language-sensitive sorting, searching, and matching).</li>
 * <li>Explicit overrides for date/time formats, etc.</li>
 * </ul>
 * The class will heuristically compute implicit, heuristic values for the above
 * based on available data if explicit values are not supplied. These implicit
 * values can be presented to users for confirmation, or replacement if the
 * values are incorrect.
 * <p>
 * To reset any explicit field so that it will get heuristic values, pass in
 * null. For example, myPreferences.setLocale(null);
 * <p>
 * All of the heuristics can be customized by subclasses, by overriding
 * getTerritory(), guessCollator(), etc.
 * <p>
 * The class also supplies display names for languages, scripts, territories,
 * currencies, timezones, etc. These are computed according to the
 * locale/language preference list. Thus, if the preference is Breton; French;
 * English, then the display name for a language will be returned in Breton if
 * available, otherwise in French if available, otherwise in English.
 * <p>
 * The codes used to reference territory, currency, etc. are as defined elsewhere
 * in ICU, and are taken from CLDR (which reflects RFC 3066bis usage, ISO 4217,
 * and the TZ Timezone database identifiers).
 * <p>
 * <b>This is at a prototype stage, and has not incorporated all the design
 * changes that we would like yet; further feedback is welcome.</b></p>
 * Note:
 * <ul>
 * <li>to get the display name for the first day of the week, use the calendar +
 * display names.</li>
 * <li>to get the work days, ask the calendar (when that is available).</li>
 * <li>to get papersize / measurement system/bidi-orientation, ask the locale
 * (when that is available there)</li>
 * <li>to get the field order in a date, and whether a time is 24hour or not,
 * ask the DateFormat (when that is available there)</li>
 * <li>it will support HOST locale when it becomes available (it is a special
 * locale that will ask the services to use the host platform's values).</li>
 * </ul>
 *
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */

//TODO:
// - Add Holidays
// - Add convenience to get/take Locale as well as ULocale.
// - Add Lenient datetime formatting when that is available.
// - Should this be serializable?
// - Other utilities?

public class GlobalizationPreferences implements Freezable<GlobalizationPreferences> {

    /**
     * Default constructor
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences(){}
    /**
     * Number Format type
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final int
        NF_NUMBER = 0,      // NumberFormat.NUMBERSTYLE
        NF_CURRENCY = 1,    // NumberFormat.CURRENCYSTYLE
        NF_PERCENT = 2,     // NumberFormat.PERCENTSTYLE
        NF_SCIENTIFIC = 3,  // NumberFormat.SCIENTIFICSTYLE
        NF_INTEGER = 4;     // NumberFormat.INTEGERSTYLE

    private static final int NF_LIMIT = NF_INTEGER + 1;

    /**
     * Date Format type
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final int
        DF_FULL = DateFormat.FULL,      // 0
        DF_LONG = DateFormat.LONG,      // 1
        DF_MEDIUM = DateFormat.MEDIUM,  // 2
        DF_SHORT = DateFormat.SHORT,    // 3
        DF_NONE = 4;

    private static final int DF_LIMIT = DF_NONE + 1;

    /**
     * For selecting a choice of display names
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final int
        ID_LOCALE = 0,
        ID_LANGUAGE = 1,
        ID_SCRIPT = 2,
        ID_TERRITORY = 3,
        ID_VARIANT = 4,
        ID_KEYWORD = 5,
        ID_KEYWORD_VALUE = 6,
        ID_CURRENCY = 7,
        ID_CURRENCY_SYMBOL = 8,
        ID_TIMEZONE = 9;

    //private static final int ID_LIMIT = ID_TIMEZONE + 1;

    /**
     * Break iterator type
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final int
        BI_CHARACTER = BreakIterator.KIND_CHARACTER,    // 0
        BI_WORD = BreakIterator.KIND_WORD,              // 1
        BI_LINE = BreakIterator.KIND_LINE,              // 2
        BI_SENTENCE = BreakIterator.KIND_SENTENCE,      // 3
        BI_TITLE = BreakIterator.KIND_TITLE;            // 4

    private static final int BI_LIMIT = BI_TITLE + 1;

    /**
     * Sets the language/locale priority list. If other information is
     * not (yet) available, this is used to to produce a default value
     * for the appropriate territory, currency, timezone, etc.  The
     * user should be given the opportunity to correct those defaults
     * in case they are incorrect.
     *
     * @param inputLocales list of locales in priority order, eg {"be", "fr"}
     *     for Breton first, then French if that fails.
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setLocales(List<ULocale> inputLocales) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        locales = processLocales(inputLocales);
        return this;
    }

    /**
     * Get a copy of the language/locale priority list
     *
     * @return a copy of the language/locale priority list.
     * @hide draft / provisional / internal are hidden on Android
     */
    public List<ULocale> getLocales() {
        List<ULocale> result;
        if (locales == null) {
            result = guessLocales();
        } else {
            result = new ArrayList<ULocale>();
            result.addAll(locales);
        }
        return result;
    }

    /**
     * Convenience function for getting the locales in priority order
     * @param index The index (0..n) of the desired item.
     * @return desired item. null if index is out of range
     * @hide draft / provisional / internal are hidden on Android
     */
    public ULocale getLocale(int index) {
        List<ULocale> lcls = locales;
        if (lcls == null) {
            lcls = guessLocales();
        }
        if (index >= 0 && index < lcls.size()) {
            return lcls.get(index);
        }
        return null;
    }

    /**
     * Convenience routine for setting the language/locale priority
     * list from an array.
     *
     * @see #setLocales(List locales)
     * @param uLocales list of locales in an array
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setLocales(ULocale[] uLocales) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        return setLocales(Arrays.asList(uLocales));
    }

    /**
     * Convenience routine for setting the language/locale priority
     * list from a single locale/language.
     *
     * @see #setLocales(List locales)
     * @param uLocale single locale
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setLocale(ULocale uLocale) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        return setLocales(new ULocale[]{uLocale});
    }

    /**
     * Convenience routine for setting the locale priority list from
     * an Accept-Language string.
     * @see #setLocales(List locales)
     * @param acceptLanguageString Accept-Language list, as defined by
     *     Section 14.4 of the RFC 2616 (HTTP 1.1)
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setLocales(String acceptLanguageString) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        ULocale[] acceptLocales = null;
        try {
            acceptLocales = ULocale.parseAcceptLanguage(acceptLanguageString, true);
        } catch (ParseException pe) {
            //TODO: revisit after 3.8
            throw new IllegalArgumentException("Invalid Accept-Language string");
        }
        return setLocales(acceptLocales);
    }

    /**
     * Convenience function to get a ResourceBundle instance using
     * the specified base name based on the language/locale priority list
     * stored in this object.
     *
     * @param baseName the base name of the resource bundle, a fully qualified
     * class name
     * @return a resource bundle for the given base name and locale based on the
     * language/locale priority list stored in this object
     * @hide draft / provisional / internal are hidden on Android
     */
    public ResourceBundle getResourceBundle(String baseName) {
        return getResourceBundle(baseName, null);
    }

    /**
     * Convenience function to get a ResourceBundle instance using
     * the specified base name and class loader based on the language/locale
     * priority list stored in this object.
     *
     * @param baseName the base name of the resource bundle, a fully qualified
     * class name
     * @param loader the class object from which to load the resource bundle
     * @return a resource bundle for the given base name and locale based on the
     * language/locale priority list stored in this object
     * @hide draft / provisional / internal are hidden on Android
     */
    public ResourceBundle getResourceBundle(String baseName, ClassLoader loader) {
        UResourceBundle urb = null;
        UResourceBundle candidate = null;
        String actualLocaleName = null;
        List<ULocale> fallbacks = getLocales();
        for (int i = 0; i < fallbacks.size(); i++) {
            String localeName = (fallbacks.get(i)).toString();
            if (actualLocaleName != null && localeName.equals(actualLocaleName)) {
                // Actual locale name in the previous round may exactly matches
                // with the next fallback locale
                urb = candidate;
                break;
            }
            try {
                if (loader == null) {
                    candidate = UResourceBundle.getBundleInstance(baseName, localeName);
                }
                else {
                    candidate = UResourceBundle.getBundleInstance(baseName, localeName, loader);
                }
                if (candidate != null) {
                    actualLocaleName = candidate.getULocale().getName();
                    if (actualLocaleName.equals(localeName)) {
                        urb = candidate;
                        break;
                    }
                    if (urb == null) {
                        // Preserve the available bundle as the last resort
                        urb = candidate;
                    }
                }
            } catch (MissingResourceException mre) {
                actualLocaleName = null;
                continue;
            }
        }
        if (urb == null) {
            throw new MissingResourceException("Can't find bundle for base name "
                    + baseName, baseName, "");
        }
        return urb;
    }

    /**
     * Sets the territory, which is a valid territory according to for
     * RFC 3066 (or successor).  If not otherwise set, default
     * currency and timezone values will be set from this.  The user
     * should be given the opportunity to correct those defaults in
     * case they are incorrect.
     *
     * @param territory code
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setTerritory(String territory) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        this.territory = territory; // immutable, so don't need to clone
        return this;
    }

    /**
     * Gets the territory setting. If it wasn't explicitly set, it is
     * computed from the general locale setting.
     *
     * @return territory code, explicit or implicit.
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getTerritory() {
        if (territory == null) {
            return guessTerritory();
        }
        return territory; // immutable, so don't need to clone
    }

    /**
     * Sets the currency code. If this has not been set, uses default for territory.
     *
     * @param currency Valid ISO 4217 currency code.
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setCurrency(Currency currency) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        this.currency = currency; // immutable, so don't need to clone
        return this;
    }

    /**
     * Get a copy of the currency computed according to the settings.
     *
     * @return currency code, explicit or implicit.
     * @hide draft / provisional / internal are hidden on Android
     */
    public Currency getCurrency() {
        if (currency == null) {
            return guessCurrency();
        }
        return currency; // immutable, so don't have to clone
    }

    /**
     * Sets the calendar. If this has not been set, uses default for territory.
     *
     * @param calendar arbitrary calendar
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setCalendar(Calendar calendar) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        this.calendar = (Calendar) calendar.clone(); // clone for safety
        return this;
    }

    /**
     * Get a copy of the calendar according to the settings.
     *
     * @return calendar explicit or implicit.
     * @hide draft / provisional / internal are hidden on Android
     */
    public Calendar getCalendar() {
        if (calendar == null) {
            return guessCalendar();
        }
        Calendar temp = (Calendar) calendar.clone(); // clone for safety
        temp.setTimeZone(getTimeZone());
        temp.setTimeInMillis(System.currentTimeMillis());
        return temp;
    }

    /**
     * Sets the timezone ID.  If this has not been set, uses default for territory.
     *
     * @param timezone a valid TZID (see UTS#35).
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setTimeZone(TimeZone timezone) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        this.timezone = (TimeZone) timezone.clone(); // clone for safety;
        return this;
    }

    /**
     * Get the timezone. It was either explicitly set, or is
     * heuristically computed from other settings.
     *
     * @return timezone, either implicitly or explicitly set
     * @hide draft / provisional / internal are hidden on Android
     */
    public TimeZone getTimeZone() {
        if (timezone == null) {
            return guessTimeZone();
        }
        return timezone.cloneAsThawed(); // clone for safety
    }

    /**
     * Get a copy of the collator according to the settings.
     *
     * @return collator explicit or implicit.
     * @hide draft / provisional / internal are hidden on Android
     */
    public Collator getCollator() {
        if (collator == null) {
            return guessCollator();
        }
        try {
            return (Collator) collator.clone();  // clone for safety
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("Error in cloning collator", e);
        }
    }

    /**
     * Explicitly set the collator for this object.
     * @param collator The collator object to be passed.
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setCollator(Collator collator) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        try {
            this.collator = (Collator) collator.clone(); // clone for safety
        } catch (CloneNotSupportedException e) {
                throw new ICUCloneNotSupportedException("Error in cloning collator", e);
        }
        return this;
    }

    /**
     * Get a copy of the break iterator for the specified type according to the
     * settings.
     *
     * @param type break type - BI_CHARACTER or BI_WORD, BI_LINE, BI_SENTENCE, BI_TITLE
     * @return break iterator explicit or implicit
     * @hide draft / provisional / internal are hidden on Android
     */
    public BreakIterator getBreakIterator(int type) {
        if (type < BI_CHARACTER || type >= BI_LIMIT) {
            throw new IllegalArgumentException("Illegal break iterator type");
        }
        if (breakIterators == null || breakIterators[type] == null) {
            return guessBreakIterator(type);
        }
        return (BreakIterator) breakIterators[type].clone(); // clone for safety
    }

    /**
     * Explicitly set the break iterator for this object.
     *
     * @param type break type - BI_CHARACTER or BI_WORD, BI_LINE, BI_SENTENCE, BI_TITLE
     * @param iterator a break iterator
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setBreakIterator(int type, BreakIterator iterator) {
        if (type < BI_CHARACTER || type >= BI_LIMIT) {
            throw new IllegalArgumentException("Illegal break iterator type");
        }
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        if (breakIterators == null)
            breakIterators = new BreakIterator[BI_LIMIT];
        breakIterators[type] = (BreakIterator) iterator.clone(); // clone for safety
        return this;
    }

    /**
     * Get the display name for an ID: language, script, territory, currency, timezone...
     * Uses the language priority list to do so.
     *
     * @param id language code, script code, ...
     * @param type specifies the type of the ID: ID_LANGUAGE, etc.
     * @return the display name
     * @hide draft / provisional / internal are hidden on Android
     */
    public String getDisplayName(String id, int type) {
        String result = id;
        for (ULocale locale : getLocales()) {
            if (!isAvailableLocale(locale, TYPE_GENERIC)) {
                continue;
            }
            switch (type) {
            case ID_LOCALE:
                result = ULocale.getDisplayName(id, locale);
                break;
            case ID_LANGUAGE:
                result = ULocale.getDisplayLanguage(id, locale);
                break;
            case ID_SCRIPT:
                result = ULocale.getDisplayScript("und-" + id, locale);
                break;
            case ID_TERRITORY:
                result = ULocale.getDisplayCountry("und-" + id, locale);
                break;
            case ID_VARIANT:
                // TODO fix variant parsing
                result = ULocale.getDisplayVariant("und-QQ-" + id, locale);
                break;
            case ID_KEYWORD:
                result = ULocale.getDisplayKeyword(id, locale);
                break;
            case ID_KEYWORD_VALUE:
                String[] parts = new String[2];
                Utility.split(id,'=',parts);
                result = ULocale.getDisplayKeywordValue("und@"+id, parts[0], locale);
                // TODO fix to tell when successful
                if (result.equals(parts[1])) {
                    continue;
                }
                break;
            case ID_CURRENCY_SYMBOL:
            case ID_CURRENCY:
                Currency temp = new Currency(id);
                result =temp.getName(locale, type==ID_CURRENCY
                                     ? Currency.LONG_NAME
                                     : Currency.SYMBOL_NAME, new boolean[1]);
                // TODO: have method that doesn't take parameter. Add
                // function to determine whether string is choice
                // format.
                // TODO: have method that doesn't require us
                // to create a currency
                break;
            case ID_TIMEZONE:
                SimpleDateFormat dtf = new SimpleDateFormat("vvvv",locale);
                dtf.setTimeZone(TimeZone.getFrozenTimeZone(id));
                result = dtf.format(new Date());
                // TODO, have method that doesn't require us to create a timezone
                // fix other hacks
                // hack for couldn't match

                boolean isBadStr = false;
                // Matcher badTimeZone = Pattern.compile("[A-Z]{2}|.*\\s\\([A-Z]{2}\\)").matcher("");
                // badtzstr = badTimeZone.reset(result).matches();
                String teststr = result;
                int sidx = result.indexOf('(');
                int eidx = result.indexOf(')');
                if (sidx != -1 && eidx != -1 && (eidx - sidx) == 3) {
                    teststr = result.substring(sidx+1, eidx);
                }
                if (teststr.length() == 2) {
                    isBadStr = true;
                    for (int i = 0; i < 2; i++) {
                        char c = teststr.charAt(i);
                        if (c < 'A' || 'Z' < c) {
                            isBadStr = false;
                            break;
                        }
                    }
                }
                if (isBadStr) {
                    continue;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
            }

            // TODO need better way of seeing if we fell back to root!!
            // This will not work at all for lots of stuff
            if (!id.equals(result)) {
                return result;
            }
        }
        return result;
    }

    /**
     * Set an explicit date format. Overrides the locale priority list for
     * a particular combination of dateStyle and timeStyle. DF_NONE should
     * be used if for the style, where only the date or time format individually
     * is being set.
     *
     * @param dateStyle DF_FULL, DF_LONG, DF_MEDIUM, DF_SHORT or DF_NONE
     * @param timeStyle DF_FULL, DF_LONG, DF_MEDIUM, DF_SHORT or DF_NONE
     * @param format The date format
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setDateFormat(int dateStyle, int timeStyle, DateFormat format) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        if (dateFormats == null) {
            dateFormats = new DateFormat[DF_LIMIT][DF_LIMIT];
        }
        dateFormats[dateStyle][timeStyle] = (DateFormat) format.clone(); // for safety
        return this;
    }

    /**
     * Gets a date format according to the current settings. If there
     * is an explicit (non-null) date/time format set, a copy of that
     * is returned. Otherwise, the language priority list is used.
     * DF_NONE should be used for the style, where only the date or
     * time format individually is being gotten.
     *
     * @param dateStyle DF_FULL, DF_LONG, DF_MEDIUM, DF_SHORT or DF_NONE
     * @param timeStyle DF_FULL, DF_LONG, DF_MEDIUM, DF_SHORT or DF_NONE
     * @return a DateFormat, according to the above description
     * @hide draft / provisional / internal are hidden on Android
     */
    public DateFormat getDateFormat(int dateStyle, int timeStyle) {
        if (dateStyle == DF_NONE && timeStyle == DF_NONE
                || dateStyle < 0 || dateStyle >= DF_LIMIT
                || timeStyle < 0 || timeStyle >= DF_LIMIT) {
            throw new IllegalArgumentException("Illegal date format style arguments");
        }
        DateFormat result = null;
        if (dateFormats != null) {
            result = dateFormats[dateStyle][timeStyle];
        }
        if (result != null) {
            result = (DateFormat) result.clone(); // clone for safety
            // Not sure overriding configuration is what we really want...
            result.setTimeZone(getTimeZone());
        } else {
            result = guessDateFormat(dateStyle, timeStyle);
        }
        return result;
    }

    /**
     * Gets a number format according to the current settings.  If
     * there is an explicit (non-null) number format set, a copy of
     * that is returned.  Otherwise, the language priority list is
     * used.
     *
     * @param style NF_NUMBER, NF_CURRENCY, NF_PERCENT, NF_SCIENTIFIC, NF_INTEGER
     * @hide draft / provisional / internal are hidden on Android
     */
    public NumberFormat getNumberFormat(int style) {
        if (style < 0 || style >= NF_LIMIT) {
            throw new IllegalArgumentException("Illegal number format type");
        }
        NumberFormat result = null;
        if (numberFormats != null) {
            result = numberFormats[style];
        }
        if (result != null) {
            result = (NumberFormat) result.clone(); // clone for safety (later optimize)
        } else {
            result = guessNumberFormat(style);
        }
        return result;
    }

    /**
     * Sets a number format explicitly. Overrides the general locale settings.
     *
     * @param style NF_NUMBER, NF_CURRENCY, NF_PERCENT, NF_SCIENTIFIC, NF_INTEGER
     * @param format The number format
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences setNumberFormat(int style, NumberFormat format) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        if (numberFormats == null) {
            numberFormats = new NumberFormat[NF_LIMIT];
        }
        numberFormats[style] = (NumberFormat) format.clone(); // for safety
        return this;
    }

    /**
     * Restore the object to the initial state.
     *
     * @return this, for chaining
     * @hide draft / provisional / internal are hidden on Android
     */
    public GlobalizationPreferences reset() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        locales = null;
        territory = null;
        calendar = null;
        collator = null;
        breakIterators = null;
        timezone = null;
        currency = null;
        dateFormats = null;
        numberFormats = null;
        implicitLocales = null;
        return this;
    }

    /**
     * Process a language/locale priority list specified via <code>setLocales</code>.
     * The input locale list may be expanded or re-ordered to represent the prioritized
     * language/locale order actually used by this object by the algorithm explained
     * below.
     * <br>
     * <br>
     * <b>Step 1</b>: Move later occurrence of more specific locale before earlier
     * occurrence of less specific locale.
     * <br>
     * Before: en, fr_FR, en_US, en_GB
     * <br>
     * After: en_US, en_GB, en, fr_FR
     * <br>
     * <br>
     * <b>Step 2</b>: Append a fallback locale to each locale.
     * <br>
     * Before: en_US, en_GB, en, fr_FR
     * <br>
     * After: en_US, en, en_GB, en, en, fr_FR, fr
     * <br>
     * <br>
     * <b>Step 3</b>: Remove earlier occurrence of duplicated locale entries.
     * <br>
     * Before: en_US, en, en_GB, en, en, fr_FR, fr
     * <br>
     * After: en_US, en_GB, en, fr_FR, fr
     * <br>
     * <br>
     * The final locale list is used to produce a default value for the appropriate territory,
     * currency, timezone, etc.  The list also represents the lookup order used in
     * <code>getResourceBundle</code> for this object.  A subclass may override this method
     * to customize the algorithm used for populating the locale list.
     *
     * @param inputLocales The list of input locales
     * @hide draft / provisional / internal are hidden on Android
     */
    protected List<ULocale> processLocales(List<ULocale> inputLocales) {
        List<ULocale> result = new ArrayList<ULocale>();
        /*
         * Step 1: Relocate later occurrence of more specific locale
         * before earlier occurrence of less specific locale.
         *
         * Example:
         *   Before - en_US, fr_FR, zh, en_US_Boston, zh_TW, zh_Hant, fr_CA
         *   After  - en_US_Boston, en_US, fr_FR, zh_TW, zh_Hant, zh, fr_CA
         */
        for (int i = 0; i < inputLocales.size(); i++) {
            ULocale uloc = inputLocales.get(i);

            String language = uloc.getLanguage();
            String script = uloc.getScript();
            String country = uloc.getCountry();
            String variant = uloc.getVariant();

            boolean bInserted = false;
            for (int j = 0; j < result.size(); j++) {
                // Check if this locale is more specific
                // than existing locale entries already inserted
                // in the destination list
                ULocale u = result.get(j);
                if (!u.getLanguage().equals(language)) {
                    continue;
                }
                String s = u.getScript();
                String c = u.getCountry();
                String v = u.getVariant();
                if (!s.equals(script)) {
                    if (s.length() == 0 && c.length() == 0 && v.length() == 0) {
                        result.add(j, uloc);
                        bInserted = true;
                        break;
                    } else if (s.length() == 0 && c.equals(country)) {
                        // We want to see zh_Hant_HK before zh_HK
                        result.add(j, uloc);
                        bInserted = true;
                        break;
                    } else if (script.length() == 0 && country.length() > 0 && c.length() == 0) {
                        // We want to see zh_HK before zh_Hant
                        result.add(j, uloc);
                        bInserted = true;
                        break;
                    }
                    continue;
                }
                if (!c.equals(country)) {
                    if (c.length() == 0 && v.length() == 0) {
                        result.add(j, uloc);
                        bInserted = true;
                        break;
                    }
                }
                if (!v.equals(variant) && v.length() == 0) {
                    result.add(j, uloc);
                    bInserted = true;
                    break;
                }
            }
            if (!bInserted) {
                // Add this locale at the end of the list
                result.add(uloc);
            }
        }

        // TODO: Locale aliases might be resolved here
        // For example, zh_Hant_TW = zh_TW

        /*
         * Step 2: Append fallback locales for each entry
         *
         * Example:
         *   Before - en_US_Boston, en_US, fr_FR, zh_TW, zh_Hant, zh, fr_CA
         *   After  - en_US_Boston, en_US, en, en_US, en, fr_FR, fr,
         *            zh_TW, zn, zh_Hant, zh, zh, fr_CA, fr
         */
        int index = 0;
        while (index < result.size()) {
            ULocale uloc = result.get(index);
            while ((uloc = uloc.getFallback()) != null) {
                if (uloc.getLanguage().length() == 0) {
                    break;
                }
                index++;
                result.add(index, uloc);
            }
            index++;
        }

        /*
         * Step 3: Remove earlier occurrence of duplicated locales
         *
         * Example:
         *   Before - en_US_Boston, en_US, en, en_US, en, fr_FR, fr,
         *            zh_TW, zn, zh_Hant, zh, zh, fr_CA, fr
         *   After  - en_US_Boston, en_US, en, fr_FR, zh_TW, zh_Hant,
         *            zh, fr_CA, fr
         */
        index = 0;
        while (index < result.size() - 1) {
            ULocale uloc = result.get(index);
            boolean bRemoved = false;
            for (int i = index + 1; i < result.size(); i++) {
                if (uloc.equals(result.get(i))) {
                    // Remove earlier one
                    result.remove(index);
                    bRemoved = true;
                    break;
                }
            }
            if (!bRemoved) {
                index++;
            }
        }
        return result;
    }


    /**
     * This function can be overridden by subclasses to use different heuristics.
     * <b>It MUST return a 'safe' value,
     * one whose modification will not affect this object.</b>
     *
     * @param dateStyle
     * @param timeStyle
     * @hide draft / provisional / internal are hidden on Android
     */
    protected DateFormat guessDateFormat(int dateStyle, int timeStyle) {
        DateFormat result;
        ULocale dfLocale = getAvailableLocale(TYPE_DATEFORMAT);
        if (dfLocale == null) {
            dfLocale = ULocale.ROOT;
        }
        if (timeStyle == DF_NONE) {
            result = DateFormat.getDateInstance(getCalendar(), dateStyle, dfLocale);
        } else if (dateStyle == DF_NONE) {
            result = DateFormat.getTimeInstance(getCalendar(), timeStyle, dfLocale);
        } else {
            result = DateFormat.getDateTimeInstance(getCalendar(), dateStyle, timeStyle, dfLocale);
        }
        return result;
    }

    /**
     * This function can be overridden by subclasses to use different heuristics.
     * <b>It MUST return a 'safe' value,
     * one whose modification will not affect this object.</b>
     *
     * @param style
     * @hide draft / provisional / internal are hidden on Android
     */
    protected NumberFormat guessNumberFormat(int style) {
        NumberFormat result;
        ULocale nfLocale = getAvailableLocale(TYPE_NUMBERFORMAT);
        if (nfLocale == null) {
            nfLocale = ULocale.ROOT;
        }
        switch (style) {
        case NF_NUMBER:
            result = NumberFormat.getInstance(nfLocale);
            break;
        case NF_SCIENTIFIC:
            result = NumberFormat.getScientificInstance(nfLocale);
            break;
        case NF_INTEGER:
            result = NumberFormat.getIntegerInstance(nfLocale);
            break;
        case NF_PERCENT:
            result = NumberFormat.getPercentInstance(nfLocale);
            break;
        case NF_CURRENCY:
            result = NumberFormat.getCurrencyInstance(nfLocale);
            result.setCurrency(getCurrency());
            break;
        default:
            throw new IllegalArgumentException("Unknown number format style");
        }
        return result;
    }

    /**
     * This function can be overridden by subclasses to use different heuristics.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    protected String guessTerritory() {
        String result;
        // pass through locales to see if there is a territory.
        for (ULocale locale : getLocales()) {
            result = locale.getCountry();
            if (result.length() != 0) {
                return result;
            }
        }
        // if not, guess from the first language tag, or maybe from
        // intersection of languages, eg nl + fr => BE
        // TODO: fix using real data
        // for now, just use fixed values
        ULocale firstLocale = getLocale(0);
        String language = firstLocale.getLanguage();
        String script = firstLocale.getScript();
        result = null;
        if (script.length() != 0) {
            result = language_territory_hack_map.get(language + "_" + script);
        }
        if (result == null) {
            result = language_territory_hack_map.get(language);
        }
        if (result == null) {
            result = "US"; // need *some* default
        }
        return result;
    }

    /**
     * This function can be overridden by subclasses to use different heuristics
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    protected Currency guessCurrency() {
        return Currency.getInstance(new ULocale("und-" + getTerritory()));
    }

    /**
     * This function can be overridden by subclasses to use different heuristics
     * <b>It MUST return a 'safe' value,
     * one whose modification will not affect this object.</b>
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    protected List<ULocale> guessLocales() {
        if (implicitLocales == null) {
            List<ULocale> result = new ArrayList<ULocale>(1);
            result.add(ULocale.getDefault());
            implicitLocales = processLocales(result);
        }
        return implicitLocales;
    }

    /**
     * This function can be overridden by subclasses to use different heuristics.
     * <b>It MUST return a 'safe' value,
     * one whose modification will not affect this object.</b>
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    protected Collator guessCollator() {
        ULocale collLocale = getAvailableLocale(TYPE_COLLATOR);
        if (collLocale == null) {
            collLocale = ULocale.ROOT;
        }
        return Collator.getInstance(collLocale);
    }

    /**
     * This function can be overridden by subclasses to use different heuristics.
     * <b>It MUST return a 'safe' value,
     * one whose modification will not affect this object.</b>
     *
     * @param type
     * @hide draft / provisional / internal are hidden on Android
     */
    protected BreakIterator guessBreakIterator(int type) {
        BreakIterator bitr = null;
        ULocale brkLocale = getAvailableLocale(TYPE_BREAKITERATOR);
        if (brkLocale == null) {
            brkLocale = ULocale.ROOT;
        }
        switch (type) {
        case BI_CHARACTER:
            bitr = BreakIterator.getCharacterInstance(brkLocale);
            break;
        case BI_TITLE:
            bitr = BreakIterator.getTitleInstance(brkLocale);
            break;
        case BI_WORD:
            bitr = BreakIterator.getWordInstance(brkLocale);
            break;
        case BI_LINE:
            bitr = BreakIterator.getLineInstance(brkLocale);
            break;
        case BI_SENTENCE:
            bitr = BreakIterator.getSentenceInstance(brkLocale);
            break;
        default:
            throw new IllegalArgumentException("Unknown break iterator type");
        }
        return bitr;
    }

    /**
     * This function can be overridden by subclasses to use different heuristics.
     * <b>It MUST return a 'safe' value,
     * one whose modification will not affect this object.</b>
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    protected TimeZone guessTimeZone() {
        // TODO fix using real data
        // for single-zone countries, pick that zone
        // for others, pick the most populous zone
        // for now, just use fixed value
        // NOTE: in a few cases can do better by looking at language.
        // Eg haw+US should go to Pacific/Honolulu
        // fr+CA should go to America/Montreal
        String timezoneString = territory_tzid_hack_map.get(getTerritory());
        if (timezoneString == null) {
            String[] attempt = TimeZone.getAvailableIDs(getTerritory());
            if (attempt.length == 0) {
                timezoneString = "Etc/GMT"; // gotta do something
            } else {
                int i;
                // this all needs to be fixed to use real data. But for now, do slightly better by skipping cruft
                for (i = 0; i < attempt.length; ++i) {
                    if (attempt[i].indexOf("/") >= 0) break;
                }
                if (i > attempt.length) i = 0;
                timezoneString = attempt[i];
            }
        }
        return TimeZone.getTimeZone(timezoneString);
    }

    /**
     * This function can be overridden by subclasses to use different heuristics.
     * <b>It MUST return a 'safe' value,
     * one whose modification will not affect this object.</b>
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    protected Calendar guessCalendar() {
        ULocale calLocale = getAvailableLocale(TYPE_CALENDAR);
        if (calLocale == null) {
            calLocale = ULocale.US;
        }
        return Calendar.getInstance(getTimeZone(), calLocale);
    }

    // PRIVATES

    private List<ULocale> locales;
    private String territory;
    private Currency currency;
    private TimeZone timezone;
    private Calendar calendar;
    private Collator collator;
    private BreakIterator[] breakIterators;
    private DateFormat[][] dateFormats;
    private NumberFormat[] numberFormats;
    private List<ULocale> implicitLocales;

    {
        reset();
    }


    private ULocale getAvailableLocale(int type) {
        List<ULocale> locs = getLocales();
        ULocale result = null;
        for (int i = 0; i < locs.size(); i++) {
            ULocale l = locs.get(i);
            if (isAvailableLocale(l, type)) {
                result = l;
                break;
            }
        }
        return result;
    }

    private boolean isAvailableLocale(ULocale loc, int type) {
        BitSet bits = available_locales.get(loc);
        if (bits != null && bits.get(type)) {
            return true;
        }
        return false;
    }

    /*
     * Available locales for service types
     */
    private static final HashMap<ULocale, BitSet> available_locales = new HashMap<ULocale, BitSet>();
    private static final int
        TYPE_GENERIC = 0,
        TYPE_CALENDAR = 1,
        TYPE_DATEFORMAT= 2,
        TYPE_NUMBERFORMAT = 3,
        TYPE_COLLATOR = 4,
        TYPE_BREAKITERATOR = 5,
        TYPE_LIMIT = TYPE_BREAKITERATOR + 1;

    static {
        BitSet bits;
        ULocale[] allLocales = ULocale.getAvailableLocales();
        for (int i = 0; i < allLocales.length; i++) {
            bits = new BitSet(TYPE_LIMIT);
            available_locales.put(allLocales[i], bits);
            bits.set(TYPE_GENERIC);
        }

        ULocale[] calLocales = Calendar.getAvailableULocales();
        for (int i = 0; i < calLocales.length; i++) {
            bits = available_locales.get(calLocales[i]);
            if (bits == null) {
                bits = new BitSet(TYPE_LIMIT);
                available_locales.put(allLocales[i], bits);
            }
            bits.set(TYPE_CALENDAR);
        }

        ULocale[] dateLocales = DateFormat.getAvailableULocales();
        for (int i = 0; i < dateLocales.length; i++) {
            bits = available_locales.get(dateLocales[i]);
            if (bits == null) {
                bits = new BitSet(TYPE_LIMIT);
                available_locales.put(allLocales[i], bits);
            }
            bits.set(TYPE_DATEFORMAT);
        }

        ULocale[] numLocales = NumberFormat.getAvailableULocales();
        for (int i = 0; i < numLocales.length; i++) {
            bits = available_locales.get(numLocales[i]);
            if (bits == null) {
                bits = new BitSet(TYPE_LIMIT);
                available_locales.put(allLocales[i], bits);
            }
            bits.set(TYPE_NUMBERFORMAT);
        }

        ULocale[] collLocales = Collator.getAvailableULocales();
        for (int i = 0; i < collLocales.length; i++) {
            bits = available_locales.get(collLocales[i]);
            if (bits == null) {
                bits = new BitSet(TYPE_LIMIT);
                available_locales.put(allLocales[i], bits);
            }
            bits.set(TYPE_COLLATOR);
        }

        ULocale[] brkLocales = BreakIterator.getAvailableULocales();
        for (int i = 0; i < brkLocales.length; i++) {
            bits = available_locales.get(brkLocales[i]);
            bits.set(TYPE_BREAKITERATOR);
        }
    }

    /** WARNING: All of this data is temporary, until we start importing from CLDR!!!
     *
     */
    private static final Map<String, String> language_territory_hack_map = new HashMap<String, String>();
    private static final String[][] language_territory_hack = {
        {"af", "ZA"},
        {"am", "ET"},
        {"ar", "SA"},
        {"as", "IN"},
        {"ay", "PE"},
        {"az", "AZ"},
        {"bal", "PK"},
        {"be", "BY"},
        {"bg", "BG"},
        {"bn", "IN"},
        {"bs", "BA"},
        {"ca", "ES"},
        {"ch", "MP"},
        {"cpe", "SL"},
        {"cs", "CZ"},
        {"cy", "GB"},
        {"da", "DK"},
        {"de", "DE"},
        {"dv", "MV"},
        {"dz", "BT"},
        {"el", "GR"},
        {"en", "US"},
        {"es", "ES"},
        {"et", "EE"},
        {"eu", "ES"},
        {"fa", "IR"},
        {"fi", "FI"},
        {"fil", "PH"},
        {"fj", "FJ"},
        {"fo", "FO"},
        {"fr", "FR"},
        {"ga", "IE"},
        {"gd", "GB"},
        {"gl", "ES"},
        {"gn", "PY"},
        {"gu", "IN"},
        {"gv", "GB"},
        {"ha", "NG"},
        {"he", "IL"},
        {"hi", "IN"},
        {"ho", "PG"},
        {"hr", "HR"},
        {"ht", "HT"},
        {"hu", "HU"},
        {"hy", "AM"},
        {"id", "ID"},
        {"is", "IS"},
        {"it", "IT"},
        {"ja", "JP"},
        {"ka", "GE"},
        {"kk", "KZ"},
        {"kl", "GL"},
        {"km", "KH"},
        {"kn", "IN"},
        {"ko", "KR"},
        {"kok", "IN"},
        {"ks", "IN"},
        {"ku", "TR"},
        {"ky", "KG"},
        {"la", "VA"},
        {"lb", "LU"},
        {"ln", "CG"},
        {"lo", "LA"},
        {"lt", "LT"},
        {"lv", "LV"},
        {"mai", "IN"},
        {"men", "GN"},
        {"mg", "MG"},
        {"mh", "MH"},
        {"mk", "MK"},
        {"ml", "IN"},
        {"mn", "MN"},
        {"mni", "IN"},
        {"mo", "MD"},
        {"mr", "IN"},
        {"ms", "MY"},
        {"mt", "MT"},
        {"my", "MM"},
        {"na", "NR"},
        {"nb", "NO"},
        {"nd", "ZA"},
        {"ne", "NP"},
        {"niu", "NU"},
        {"nl", "NL"},
        {"nn", "NO"},
        {"no", "NO"},
        {"nr", "ZA"},
        {"nso", "ZA"},
        {"ny", "MW"},
        {"om", "KE"},
        {"or", "IN"},
        {"pa", "IN"},
        {"pau", "PW"},
        {"pl", "PL"},
        {"ps", "PK"},
        {"pt", "BR"},
        {"qu", "PE"},
        {"rn", "BI"},
        {"ro", "RO"},
        {"ru", "RU"},
        {"rw", "RW"},
        {"sd", "IN"},
        {"sg", "CF"},
        {"si", "LK"},
        {"sk", "SK"},
        {"sl", "SI"},
        {"sm", "WS"},
        {"so", "DJ"},
        {"sq", "CS"},
        {"sr", "CS"},
        {"ss", "ZA"},
        {"st", "ZA"},
        {"sv", "SE"},
        {"sw", "KE"},
        {"ta", "IN"},
        {"te", "IN"},
        {"tem", "SL"},
        {"tet", "TL"},
        {"th", "TH"},
        {"ti", "ET"},
        {"tg", "TJ"},
        {"tk", "TM"},
        {"tkl", "TK"},
        {"tvl", "TV"},
        {"tl", "PH"},
        {"tn", "ZA"},
        {"to", "TO"},
        {"tpi", "PG"},
        {"tr", "TR"},
        {"ts", "ZA"},
        {"uk", "UA"},
        {"ur", "IN"},
        {"uz", "UZ"},
        {"ve", "ZA"},
        {"vi", "VN"},
        {"wo", "SN"},
        {"xh", "ZA"},
        {"zh", "CN"},
        {"zh_Hant", "TW"},
        {"zu", "ZA"},
        {"aa", "ET"},
        {"byn", "ER"},
        {"eo", "DE"},
        {"gez", "ET"},
        {"haw", "US"},
        {"iu", "CA"},
        {"kw", "GB"},
        {"sa", "IN"},
        {"sh", "HR"},
        {"sid", "ET"},
        {"syr", "SY"},
        {"tig", "ER"},
        {"tt", "RU"},
        {"wal", "ET"},  };
    static {
        for (int i = 0; i < language_territory_hack.length; ++i) {
            language_territory_hack_map.put(language_territory_hack[i][0],language_territory_hack[i][1]);
        }
    }

    static final Map<String, String> territory_tzid_hack_map = new HashMap<String, String>();
    static final String[][] territory_tzid_hack = {
        {"AQ", "Antarctica/McMurdo"},
        {"AR", "America/Buenos_Aires"},
        {"AU", "Australia/Sydney"},
        {"BR", "America/Sao_Paulo"},
        {"CA", "America/Toronto"},
        {"CD", "Africa/Kinshasa"},
        {"CL", "America/Santiago"},
        {"CN", "Asia/Shanghai"},
        {"EC", "America/Guayaquil"},
        {"ES", "Europe/Madrid"},
        {"GB", "Europe/London"},
        {"GL", "America/Godthab"},
        {"ID", "Asia/Jakarta"},
        {"ML", "Africa/Bamako"},
        {"MX", "America/Mexico_City"},
        {"MY", "Asia/Kuala_Lumpur"},
        {"NZ", "Pacific/Auckland"},
        {"PT", "Europe/Lisbon"},
        {"RU", "Europe/Moscow"},
        {"UA", "Europe/Kiev"},
        {"US", "America/New_York"},
        {"UZ", "Asia/Tashkent"},
        {"PF", "Pacific/Tahiti"},
        {"FM", "Pacific/Kosrae"},
        {"KI", "Pacific/Tarawa"},
        {"KZ", "Asia/Almaty"},
        {"MH", "Pacific/Majuro"},
        {"MN", "Asia/Ulaanbaatar"},
        {"SJ", "Arctic/Longyearbyen"},
        {"UM", "Pacific/Midway"},
    };
    static {
        for (int i = 0; i < territory_tzid_hack.length; ++i) {
            territory_tzid_hack_map.put(territory_tzid_hack[i][0],territory_tzid_hack[i][1]);
        }
    }

    // Freezable implementation

    private volatile boolean frozen;

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    public GlobalizationPreferences freeze() {
        frozen = true;
        return this;
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    public GlobalizationPreferences cloneAsThawed() {
        try {
            GlobalizationPreferences result = (GlobalizationPreferences) clone();
            result.frozen = false;
            return result;
        } catch (CloneNotSupportedException e) {
            // will always work
            return null;
        }
    }
}

