/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2012-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.UResource;
import android.icu.text.DecimalFormat.Unit;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

/**
 * A cache containing data by locale for {@link CompactDecimalFormat}
 *
 * @author Travis Keep
 */
class CompactDecimalDataCache {

    private static final String SHORT_STYLE = "short";
    private static final String LONG_STYLE = "long";
    private static final String SHORT_CURRENCY_STYLE = "shortCurrency";
    private static final String NUMBER_ELEMENTS = "NumberElements";
    private static final String PATTERNS_LONG = "patternsLong";
    private static final String PATTERNS_SHORT = "patternsShort";
    private static final String DECIMAL_FORMAT = "decimalFormat";
    private static final String CURRENCY_FORMAT = "currencyFormat";
    private static final String LATIN_NUMBERING_SYSTEM = "latn";

    private static enum PatternsTableKey { PATTERNS_LONG, PATTERNS_SHORT };
    private static enum FormatsTableKey { DECIMAL_FORMAT, CURRENCY_FORMAT };

    public static final String OTHER = "other";

    /**
     * We can specify prefixes or suffixes for values with up to 15 digits,
     * less than 10^15.
     */
    static final int MAX_DIGITS = 15;

    private final ICUCache<ULocale, DataBundle> cache =
            new SimpleCache<ULocale, DataBundle>();

    /**
     * Data contains the compact decimal data for a particular locale. Data consists
     * of one array and two hashmaps. The index of the divisors array as well
     * as the arrays stored in the values of the two hashmaps correspond
     * to log10 of the number being formatted, so when formatting 12,345, the 4th
     * index of the arrays should be used. Divisors contain the number to divide
     * by before doing formatting. In the case of english, <code>divisors[4]</code>
     * is 1000.  So to format 12,345, divide by 1000 to get 12. Then use
     * PluralRules with the current locale to figure out which of the 6 plural variants
     * 12 matches: "zero", "one", "two", "few", "many", or "other." Prefixes and
     * suffixes are maps whose key is the plural variant and whose values are
     * arrays of strings with indexes corresponding to log10 of the original number.
     * these arrays contain the prefix or suffix to use.
     *
     * Each array in data is 15 in length, and every index is filled.
     *
     * @author Travis Keep
     *
     */
    static class Data {
        long[] divisors;
        Map<String, DecimalFormat.Unit[]> units;
        boolean fromFallback;

        Data(long[] divisors, Map<String, DecimalFormat.Unit[]> units)
        {
            this.divisors = divisors;
            this.units = units;
        }

        public boolean isEmpty() {
            return units == null || units.isEmpty();
        }
    }

    /**
     * DataBundle contains compact decimal data for all the styles in a particular
     * locale. Currently available styles are short and long for decimals, and
     * short only for currencies.
     *
     * @author Travis Keep
     */
    static class DataBundle {
        Data shortData;
        Data longData;
        Data shortCurrencyData;

        private DataBundle(Data shortData, Data longData, Data shortCurrencyData) {
            this.shortData = shortData;
            this.longData = longData;
            this.shortCurrencyData = shortCurrencyData;
        }

        private static DataBundle createEmpty() {
            return new DataBundle(
                new Data(new long[MAX_DIGITS], new HashMap<String, DecimalFormat.Unit[]>()),
                new Data(new long[MAX_DIGITS], new HashMap<String, DecimalFormat.Unit[]>()),
                new Data(new long[MAX_DIGITS], new HashMap<String, DecimalFormat.Unit[]>())
            );
        }
    }

    /**
     * Sink for enumerating all of the compact decimal format patterns.
     *
     * More specific bundles (en_GB) are enumerated before their parents (en_001, en, root):
     * Only store a value if it is still missing, that is, it has not been overridden.
     */
    private static final class CompactDecimalDataSink extends UResource.Sink {

        private DataBundle dataBundle; // Where to save values when they are read
        private ULocale locale; // The locale we are traversing (for exception messages)
        private boolean isLatin; // Whether or not we are traversing the Latin table
        private boolean isFallback; // Whether or not we are traversing the Latin table as fallback

        /*
         * NumberElements{              <-- top (numbering system table)
         *  latn{                       <-- patternsTable (one per numbering system)
         *    patternsLong{             <-- formatsTable (one per pattern)
         *      decimalFormat{          <-- powersOfTenTable (one per format)
         *        1000{                 <-- pluralVariantsTable (one per power of ten)
         *          one{"0 thousand"}   <-- plural variant and template
         */

        public CompactDecimalDataSink(DataBundle dataBundle, ULocale locale) {
            this.dataBundle = dataBundle;
            this.locale = locale;
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean isRoot) {
            // SPECIAL CASE: Don't consume root in the non-Latin numbering system
            if (isRoot && !isLatin) { return; }

            UResource.Table patternsTable = value.getTable();
            for (int i1 = 0; patternsTable.getKeyAndValue(i1, key, value); ++i1) {

                // patterns table: check for patternsShort or patternsLong
                PatternsTableKey patternsTableKey;
                if (key.contentEquals(PATTERNS_SHORT)) {
                    patternsTableKey = PatternsTableKey.PATTERNS_SHORT;
                } else if (key.contentEquals(PATTERNS_LONG)) {
                    patternsTableKey = PatternsTableKey.PATTERNS_LONG;
                } else {
                    continue;
                }

                // traverse into the table of formats
                UResource.Table formatsTable = value.getTable();
                for (int i2 = 0; formatsTable.getKeyAndValue(i2, key, value); ++i2) {

                    // formats table: check for decimalFormat or currencyFormat
                    FormatsTableKey formatsTableKey;
                    if (key.contentEquals(DECIMAL_FORMAT)) {
                        formatsTableKey = FormatsTableKey.DECIMAL_FORMAT;
                    } else if (key.contentEquals(CURRENCY_FORMAT)) {
                        formatsTableKey = FormatsTableKey.CURRENCY_FORMAT;
                    } else {
                        continue;
                    }

                    // Set the current style and destination based on the lvl1 and lvl2 keys
                    String style = null;
                    Data destination = null;
                    if (patternsTableKey == PatternsTableKey.PATTERNS_LONG
                            && formatsTableKey == FormatsTableKey.DECIMAL_FORMAT) {
                        style = LONG_STYLE;
                        destination = dataBundle.longData;
                    } else if (patternsTableKey == PatternsTableKey.PATTERNS_SHORT
                            && formatsTableKey == FormatsTableKey.DECIMAL_FORMAT) {
                        style = SHORT_STYLE;
                        destination = dataBundle.shortData;
                    } else if (patternsTableKey == PatternsTableKey.PATTERNS_SHORT
                            && formatsTableKey == FormatsTableKey.CURRENCY_FORMAT) {
                        style = SHORT_CURRENCY_STYLE;
                        destination = dataBundle.shortCurrencyData;
                    } else {
                        // Silently ignore this case
                        continue;
                    }

                    // SPECIAL CASE: RULES FOR WHETHER OR NOT TO CONSUME THIS TABLE:
                    //   1) Don't consume longData if shortData was consumed from the non-Latin
                    //      locale numbering system
                    //   2) Don't consume longData for the first time if this is the root bundle and
                    //      shortData is already populated from a more specific locale. Note that if
                    //      both longData and shortData are both only in root, longData will be
                    //      consumed since it is alphabetically before shortData in the bundle.
                    if (isFallback
                            && style == LONG_STYLE
                            && !dataBundle.shortData.isEmpty()
                            && !dataBundle.shortData.fromFallback) {
                        continue;
                    }
                    if (isRoot
                            && style == LONG_STYLE
                            && dataBundle.longData.isEmpty()
                            && !dataBundle.shortData.isEmpty()) {
                        continue;
                    }

                    // Set the "fromFallback" flag on the data object
                    destination.fromFallback = isFallback;

                    // traverse into the table of powers of ten
                    UResource.Table powersOfTenTable = value.getTable();
                    for (int i3 = 0; powersOfTenTable.getKeyAndValue(i3, key, value); ++i3) {

                        // This value will always be some even power of 10. e.g 10000.
                        long power10 = Long.parseLong(key.toString());
                        int log10Value = (int) Math.log10(power10);

                        // Silently ignore divisors that are too big.
                        if (log10Value >= MAX_DIGITS) continue;

                        // Iterate over the plural variants ("one", "other", etc)
                        UResource.Table pluralVariantsTable = value.getTable();
                        for (int i4 = 0; pluralVariantsTable.getKeyAndValue(i4, key, value); ++i4) {
                            // TODO: Use StandardPlural rather than String.
                            String pluralVariant = key.toString();
                            String template = value.toString();

                            // Copy the data into the in-memory data bundle (do not overwrite
                            // existing values)
                            int numZeros = populatePrefixSuffix(
                                    pluralVariant, log10Value, template, locale, style, destination, false);

                            // If populatePrefixSuffix returns -1, it means that this key has been
                            // encountered already.
                            if (numZeros < 0) {
                                continue;
                            }

                            // Set the divisor, which is based on the number of zeros in the template
                            // string.  If the divisor from here is different from the one previously
                            // stored, it means that the number of zeros in different plural variants
                            // differs; throw an exception.
                            long divisor = calculateDivisor(power10, numZeros);
                            if (destination.divisors[log10Value] != 0L
                                    && destination.divisors[log10Value] != divisor) {
                                throw new IllegalArgumentException("Plural variant '" + pluralVariant
                                        + "' template '" + template
                                        + "' for 10^" + log10Value
                                        + " has wrong number of zeros in " + localeAndStyle(locale, style));
                            }
                            destination.divisors[log10Value] = divisor;
                        }
                    }
                }
            }
        }
    }

    /**
     * Fetch data for a particular locale. Clients must not modify any part of the returned data. Portions of returned
     * data may be shared so modifying it will have unpredictable results.
     */
    DataBundle get(ULocale locale) {
        DataBundle result = cache.get(locale);
        if (result == null) {
            result = load(locale);
            cache.put(locale, result);
        }
        return result;
    }

    private static DataBundle load(ULocale ulocale) throws MissingResourceException {
        DataBundle dataBundle = DataBundle.createEmpty();
        String nsName = NumberingSystem.getInstance(ulocale).getName();
        ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME,
                ulocale);
        CompactDecimalDataSink sink = new CompactDecimalDataSink(dataBundle, ulocale);
        sink.isFallback = false;

        // First load the number elements data from nsName if nsName is not Latin.
        if (!nsName.equals(LATIN_NUMBERING_SYSTEM)) {
            sink.isLatin = false;

            try {
                r.getAllItemsWithFallback(NUMBER_ELEMENTS + "/" + nsName, sink);
            } catch (MissingResourceException e) {
                // Silently ignore and use Latin
            }

            // Set the "isFallback" flag for when we read Latin
            sink.isFallback = true;
        }

        // Now load Latin, which will fill in things that were left out from above.
        sink.isLatin = true;
        r.getAllItemsWithFallback(NUMBER_ELEMENTS + "/" + LATIN_NUMBERING_SYSTEM, sink);

        // If longData is empty, default it to be equal to shortData
        if (dataBundle.longData.isEmpty()) {
            dataBundle.longData = dataBundle.shortData;
        }

        // Check for "other" variants in each of the three data classes
        checkForOtherVariants(dataBundle.longData, ulocale, LONG_STYLE);
        checkForOtherVariants(dataBundle.shortData, ulocale, SHORT_STYLE);
        checkForOtherVariants(dataBundle.shortCurrencyData, ulocale, SHORT_CURRENCY_STYLE);

        // Resolve missing elements
        fillInMissing(dataBundle.longData);
        fillInMissing(dataBundle.shortData);
        fillInMissing(dataBundle.shortCurrencyData);

        // Return the data bundle
        return dataBundle;
    }


    /**
     * Populates prefix and suffix information for a particular plural variant
     * and index (log10 value).
     * @param pluralVariant e.g "one", "other"
     * @param idx the index (log10 value of the number) 0 <= idx < MAX_DIGITS
     * @param template e.g "00K"
     * @param locale the locale
     * @param style the style
     * @param destination Extracted prefix and suffix stored here.
     * @return number of zeros found before any decimal point in template, or -1 if it was not saved.
     */
    private static int populatePrefixSuffix(
            String pluralVariant, int idx, String template, ULocale locale, String style,
            Data destination, boolean overwrite) {
        int firstIdx = template.indexOf("0");
        int lastIdx = template.lastIndexOf("0");
        if (firstIdx == -1) {
            throw new IllegalArgumentException(
                "Expect at least one zero in template '" + template +
                "' for variant '" +pluralVariant + "' for 10^" + idx +
                " in " + localeAndStyle(locale, style));
        }
        String prefix = template.substring(0, firstIdx);
        String suffix = template.substring(lastIdx + 1);

        // Save the unit, and return -1 if it was not saved
        boolean saved = saveUnit(new DecimalFormat.Unit(prefix, suffix), pluralVariant, idx, destination.units, overwrite);
        if (!saved) {
            return -1;
        }

        // If there is effectively no prefix or suffix, ignore the actual
        // number of 0's and act as if the number of 0's matches the size
        // of the number
        if (prefix.trim().length() == 0 && suffix.trim().length() == 0) {
          return idx + 1;
        }

        // Calculate number of zeros before decimal point.
        int i = firstIdx + 1;
        while (i <= lastIdx && template.charAt(i) == '0') {
            i++;
        }
        return i - firstIdx;
    }

    /**
     * Calculate a divisor based on the magnitude and number of zeros in the
     * template string.
     * @param power10
     * @param numZeros
     * @return
     */
    private static long calculateDivisor(long power10, int numZeros) {
        // We craft our divisor such that when we divide by it, we get a
        // number with the same number of digits as zeros found in the
        // plural variant templates. If our magnitude is 10000 and we have
        // two 0's in our plural variants, then we want a divisor of 1000.
        // Note that if we have 43560 which is of same magnitude as 10000.
        // When we divide by 1000 we a quotient which rounds to 44 (2 digits)
        long divisor = power10;
        for (int i = 1; i < numZeros; i++) {
            divisor /= 10;
        }
        return divisor;
    }


    /**
     * Returns locale and style. Used to form useful messages in thrown exceptions.
     *
     * Note: This is not covered by unit tests since no exceptions are thrown on the default CLDR data.  It is too
     * cumbersome to cover via reflection.
     *
     * @param locale the locale
     * @param style the style
     */
    private static String localeAndStyle(ULocale locale, String style) {
        return "locale '" + locale + "' style '" + style + "'";
    }

    /**
     * Checks to make sure that an "other" variant is present in all powers of 10.
     * @param data
     */
    private static void checkForOtherVariants(Data data, ULocale locale, String style) {
        DecimalFormat.Unit[] otherByBase = data.units.get(OTHER);

        if (otherByBase == null) {
            throw new IllegalArgumentException("No 'other' plural variants defined in "
                    + localeAndStyle(locale, style));
        }

        // Check all other plural variants, and make sure that if any of them are populated, then
        // other is also populated
        for (Map.Entry<String, Unit[]> entry : data.units.entrySet()) {
            if (entry.getKey() == OTHER) continue;
            DecimalFormat.Unit[] variantByBase = entry.getValue();
            for (int log10Value = 0; log10Value < MAX_DIGITS; log10Value++) {
                if (variantByBase[log10Value] != null && otherByBase[log10Value] == null) {
                    throw new IllegalArgumentException(
                            "No 'other' plural variant defined for 10^" + log10Value
                            + " but a '" + entry.getKey() + "' variant is defined"
                            + " in " +localeAndStyle(locale, style));
                }
            }
        }
    }

    /**
     * After reading information from resource bundle into a Data object, there
     * is guarantee that it is complete.
     *
     * This method fixes any incomplete data it finds within <code>result</code>.
     * It looks at each log10 value applying the two rules.
     *   <p>
     *   If no prefix is defined for the "other" variant, use the divisor, prefixes and
     *   suffixes for all defined variants from the previous log10. For log10 = 0,
     *   use all empty prefixes and suffixes and a divisor of 1.
     *   </p><p>
     *   Otherwise, examine each plural variant defined for the given log10 value.
     *   If it has no prefix and suffix for a particular variant, use the one from the
     *   "other" variant.
     *   </p>
     *
     * @param result this instance is fixed in-place.
     */
    private static void fillInMissing(Data result) {
        // Initially we assume that previous divisor is 1 with no prefix or suffix.
        long lastDivisor = 1L;
        for (int i = 0; i < result.divisors.length; i++) {
            if (result.units.get(OTHER)[i] == null) {
                result.divisors[i] = lastDivisor;
                copyFromPreviousIndex(i, result.units);
            } else {
                lastDivisor = result.divisors[i];
                propagateOtherToMissing(i, result.units);
            }
        }
    }

    private static void propagateOtherToMissing(
            int idx, Map<String, DecimalFormat.Unit[]> units) {
        DecimalFormat.Unit otherVariantValue = units.get(OTHER)[idx];
        for (DecimalFormat.Unit[] byBase : units.values()) {
            if (byBase[idx] == null) {
                byBase[idx] = otherVariantValue;
            }
        }
    }

    private static void copyFromPreviousIndex(int idx, Map<String, DecimalFormat.Unit[]> units) {
        for (DecimalFormat.Unit[] byBase : units.values()) {
            if (idx == 0) {
                byBase[idx] = DecimalFormat.NULL_UNIT;
            } else {
                byBase[idx] = byBase[idx - 1];
            }
        }
    }

    private static boolean saveUnit(
            DecimalFormat.Unit unit, String pluralVariant, int idx,
            Map<String, DecimalFormat.Unit[]> units,
            boolean overwrite) {
        DecimalFormat.Unit[] byBase = units.get(pluralVariant);
        if (byBase == null) {
            byBase = new DecimalFormat.Unit[MAX_DIGITS];
            units.put(pluralVariant, byBase);
        }

        // Don't overwrite a pre-existing value unless the "overwrite" flag is true.
        if (!overwrite && byBase[idx] != null) {
            return false;
        }

        // Save the value and return
        byBase[idx] = unit;
        return true;
    }

    /**
     * Fetches a prefix or suffix given a plural variant and log10 value. If it
     * can't find the given variant, it falls back to "other".
     * @param prefixOrSuffix the prefix or suffix map
     * @param variant the plural variant
     * @param base log10 value. 0 <= base < MAX_DIGITS.
     * @return the prefix or suffix.
     */
    static DecimalFormat.Unit getUnit(
            Map<String, DecimalFormat.Unit[]> units, String variant, int base) {
        DecimalFormat.Unit[] byBase = units.get(variant);
        if (byBase == null) {
            byBase = units.get(CompactDecimalDataCache.OTHER);
        }
        return byBase[base];
    }
}
