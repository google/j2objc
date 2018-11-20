/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ********************************************************************************
 * Copyright (C) 2006-2016, Google, International Business Machines Corporation
 * and others. All Rights Reserved.
 ********************************************************************************
 */
package android.icu.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.UResource;
import android.icu.util.Calendar;
import android.icu.util.Freezable;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;

/**
 * This class provides flexible generation of date format patterns, like
 * "yy-MM-dd". The user can build up the generator by adding successive
 * patterns. Once that is done, a query can be made using a "skeleton", which is
 * a pattern which just includes the desired fields and lengths. The generator
 * will return the "best fit" pattern corresponding to that skeleton.
 * <p>
 * The main method people will use is getBestPattern(String skeleton), since
 * normally this class is pre-built with data from a particular locale. However,
 * generators can be built directly from other data as well.
 */
public class DateTimePatternGenerator implements Freezable<DateTimePatternGenerator>, Cloneable {
    private static final boolean DEBUG = false;

    // debugging flags
    //static boolean SHOW_DISTANCE = false;
    // TODO add hack to fix months for CJK, as per bug ticket 1099

    /**
     * Create empty generator, to be constructed with addPattern(...) etc.
     */
    public static DateTimePatternGenerator getEmptyInstance() {
        DateTimePatternGenerator instance = new DateTimePatternGenerator();
        instance.addCanonicalItems();
        instance.fillInMissing();
        return instance;
    }

    /**
     * Only for use by subclasses
     */
    protected DateTimePatternGenerator() {
    }

    /**
     * Construct a flexible generator according to data for the default <code>FORMAT</code> locale.
     * @see Category#FORMAT
     */
    public static DateTimePatternGenerator getInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Construct a flexible generator according to data for a given locale.
     * @param uLocale The locale to pass.
     */
    public static DateTimePatternGenerator getInstance(ULocale uLocale) {
        return getFrozenInstance(uLocale).cloneAsThawed();
    }

    /**
     * Construct a flexible generator according to data for a given locale.
     * @param locale The {@link java.util.Locale} to pass.
     */
    public static DateTimePatternGenerator getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    /**
     * Construct a frozen instance of DateTimePatternGenerator for a
     * given locale.  This method returns a cached frozen instance of
     * DateTimePatternGenerator, so less expensive than the regular
     * factory method.
     * @param uLocale The locale to pass.
     * @return A frozen DateTimePatternGenerator.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static DateTimePatternGenerator getFrozenInstance(ULocale uLocale) {
        String localeKey = uLocale.toString();
        DateTimePatternGenerator result = DTPNG_CACHE.get(localeKey);
        if (result != null) {
            return result;
        }

        result = new DateTimePatternGenerator();
        result.initData(uLocale);

        // freeze and cache
        result.freeze();
        DTPNG_CACHE.put(localeKey, result);
        return result;
    }

    private void initData(ULocale uLocale) {
        // This instance of PatternInfo is required for calling some functions.  It is used for
        // passing additional information to the caller.  We won't use this extra information, but
        // we still need to make a temporary instance.
        PatternInfo returnInfo = new PatternInfo();

        addCanonicalItems();
        addICUPatterns(returnInfo, uLocale);
        addCLDRData(returnInfo, uLocale);
        setDateTimeFromCalendar(uLocale);
        setDecimalSymbols(uLocale);
        getAllowedHourFormats(uLocale);
        fillInMissing();
    }

    private void addICUPatterns(PatternInfo returnInfo, ULocale uLocale) {
        // first load with the ICU patterns
        for (int i = DateFormat.FULL; i <= DateFormat.SHORT; ++i) {
            SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance(i, uLocale);
            addPattern(df.toPattern(), false, returnInfo);
            df = (SimpleDateFormat) DateFormat.getTimeInstance(i, uLocale);
            addPattern(df.toPattern(), false, returnInfo);

            if (i == DateFormat.SHORT) {
                consumeShortTimePattern(df.toPattern(), returnInfo);
            }
        }
    }

    private String getCalendarTypeToUse(ULocale uLocale) {
        // Get the correct calendar type
        // TODO: C++ and Java are inconsistent (see #9952).
        String calendarTypeToUse = uLocale.getKeywordValue("calendar");
        if ( calendarTypeToUse == null ) {
            String[] preferredCalendarTypes = Calendar.getKeywordValuesForLocale("calendar", uLocale, true);
            calendarTypeToUse = preferredCalendarTypes[0]; // the most preferred calendar
        }
        if ( calendarTypeToUse == null ) {
            calendarTypeToUse = "gregorian"; // fallback
        }
        return calendarTypeToUse;
    }

    private void consumeShortTimePattern(String shortTimePattern, PatternInfo returnInfo) {
        // keep this pattern to populate other time field
        // combination patterns by hackTimes later in this method.
        // use hour style in SHORT time pattern as the default
        // hour style for the locale
        FormatParser fp = new FormatParser();
        fp.set(shortTimePattern);
        List<Object> items = fp.getItems();
        for (int idx = 0; idx < items.size(); idx++) {
            Object item = items.get(idx);
            if (item instanceof VariableField) {
                VariableField fld = (VariableField)item;
                if (fld.getType() == HOUR) {
                    defaultHourFormatChar = fld.toString().charAt(0);
                    break;
                }
            }
        }

        // some languages didn't add mm:ss or HH:mm, so put in a hack to compute that from the short time.
        hackTimes(returnInfo, shortTimePattern);
    }

    private class AppendItemFormatsSink extends UResource.Sink {
        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table itemsTable = value.getTable();
            for (int i = 0; itemsTable.getKeyAndValue(i, key, value); ++i) {
                int field = getAppendFormatNumber(key);
                assert field != -1;
                if (getAppendItemFormat(field) == null) {
                    setAppendItemFormat(field, value.toString());
                }
            }
        }
    }

    private class AppendItemNamesSink extends UResource.Sink {
        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table itemsTable = value.getTable();
            for (int i = 0; itemsTable.getKeyAndValue(i, key, value); ++i) {
                int field = getCLDRFieldNumber(key);
                if (field == -1) { continue; }
                UResource.Table detailsTable = value.getTable();
                for (int j = 0; detailsTable.getKeyAndValue(j, key, value); ++j) {
                    if (!key.contentEquals("dn")) continue;
                    if (getAppendItemName(field) == null) {
                        setAppendItemName(field, value.toString());
                    }
                    break;
                }
            }
        }
    }

    private void fillInMissing() {
        for (int i = 0; i < TYPE_LIMIT; ++i) {
            if (getAppendItemFormat(i) == null) {
                setAppendItemFormat(i, "{0} \u251C{2}: {1}\u2524");
            }
            if (getAppendItemName(i) == null) {
                setAppendItemName(i, "F" + i);
            }
        }
    }

    private class AvailableFormatsSink extends UResource.Sink {
        PatternInfo returnInfo;
        public AvailableFormatsSink(PatternInfo returnInfo) {
            this.returnInfo = returnInfo;
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean isRoot) {
            UResource.Table formatsTable = value.getTable();
            for (int i = 0; formatsTable.getKeyAndValue(i, key, value); ++i) {
                String formatKey = key.toString();
                if (!isAvailableFormatSet(formatKey)) {
                    setAvailableFormat(formatKey);
                    // Add pattern with its associated skeleton. Override any duplicate derived from std patterns,
                    // but not a previous availableFormats entry:
                    String formatValue = value.toString();
                    addPatternWithSkeleton(formatValue, formatKey, !isRoot, returnInfo);
                }
            }
        }
    }

    private void addCLDRData(PatternInfo returnInfo, ULocale uLocale) {
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
        String calendarTypeToUse = getCalendarTypeToUse(uLocale);

        //      ICU4J getWithFallback does not work well when
        //      1) A nested table is an alias to /LOCALE/...
        //      2) getWithFallback is called multiple times for going down hierarchical resource path
        //      #9987 resolved the issue of alias table when full path is specified in getWithFallback,
        //      but there is no easy solution when the equivalent operation is done by multiple operations.
        //      This issue is addressed in #9964.

        // Load append item formats.
        AppendItemFormatsSink appendItemFormatsSink = new AppendItemFormatsSink();
        try {
            rb.getAllItemsWithFallback(
                    "calendar/" + calendarTypeToUse + "/appendItems",
                    appendItemFormatsSink);
        }catch(MissingResourceException e) {
        }

        // Load CLDR item names.
        AppendItemNamesSink appendItemNamesSink = new AppendItemNamesSink();
        try {
            rb.getAllItemsWithFallback(
                    "fields",
                    appendItemNamesSink);
        }catch(MissingResourceException e) {
        }

        // Load the available formats from CLDR.
        AvailableFormatsSink availableFormatsSink = new AvailableFormatsSink(returnInfo);
        try {
            rb.getAllItemsWithFallback(
                    "calendar/" + calendarTypeToUse + "/availableFormats",
                    availableFormatsSink);
        } catch (MissingResourceException e) {
        }
    }

    private void setDateTimeFromCalendar(ULocale uLocale) {
        String dateTimeFormat = Calendar.getDateTimePattern(Calendar.getInstance(uLocale), uLocale, DateFormat.MEDIUM);
        setDateTimeFormat(dateTimeFormat);
    }

    private void setDecimalSymbols(ULocale uLocale) {
        // decimal point for seconds
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(uLocale);
        setDecimal(String.valueOf(dfs.getDecimalSeparator()));
    }

    private static final String[] LAST_RESORT_ALLOWED_HOUR_FORMAT = {"H"};

    private void getAllowedHourFormats(ULocale uLocale) {
        // key can be either region or locale (lang_region)
        //        ZW{
        //            allowed{
        //                "h",
        //                "H",
        //            }
        //            preferred{"h"}
        //        }
        //        af_ZA{
        //            allowed{
        //                "h",
        //                "H",
        //                "hB",
        //                "hb",
        //            }
        //            preferred{"h"}
        //        }

        ULocale max = ULocale.addLikelySubtags(uLocale);
        String country = max.getCountry();
        if (country.isEmpty()) {
            country = "001";
        }
        String langCountry = max.getLanguage() + "_" + country;
        String[] list = LOCALE_TO_ALLOWED_HOUR.get(langCountry);
        if (list == null) {
            list = LOCALE_TO_ALLOWED_HOUR.get(country);
            if (list == null) {
                list = LAST_RESORT_ALLOWED_HOUR_FORMAT;
            }
        }
        allowedHourFormats = list;
    }

    private static class DayPeriodAllowedHoursSink extends UResource.Sink {
        HashMap<String, String[]> tempMap;

        private DayPeriodAllowedHoursSink(HashMap<String, String[]> tempMap) {
            this.tempMap = tempMap;
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table timeData = value.getTable();
            for (int i = 0; timeData.getKeyAndValue(i, key, value); ++i) {
                String regionOrLocale = key.toString();
                UResource.Table formatList = value.getTable();
                for (int j = 0; formatList.getKeyAndValue(j, key, value); ++j) {
                    if (key.contentEquals("allowed")) {  // Ignore "preferred" list.
                        tempMap.put(regionOrLocale, value.getStringArrayOrStringAsArray());
                    }
                }
            }
        }
    }

    // Get the data for dayperiod C.
    static final Map<String, String[]> LOCALE_TO_ALLOWED_HOUR;
    static {
        HashMap<String, String[]> temp = new HashMap<String, String[]>();
        ICUResourceBundle suppData = (ICUResourceBundle)ICUResourceBundle.getBundleInstance(
                ICUData.ICU_BASE_NAME,
                "supplementalData",
                ICUResourceBundle.ICU_DATA_CLASS_LOADER);

        DayPeriodAllowedHoursSink allowedHoursSink = new DayPeriodAllowedHoursSink(temp);
        suppData.getAllItemsWithFallback("timeData", allowedHoursSink);

        LOCALE_TO_ALLOWED_HOUR = Collections.unmodifiableMap(temp);
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public char getDefaultHourFormatChar() {
        return defaultHourFormatChar;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public void setDefaultHourFormatChar(char defaultHourFormatChar) {
        this.defaultHourFormatChar = defaultHourFormatChar;
    }

    private void hackTimes(PatternInfo returnInfo, String shortTimePattern) {
        fp.set(shortTimePattern);
        StringBuilder mmss = new StringBuilder();
        // to get mm:ss, we strip all but mm literal ss
        boolean gotMm = false;
        for (int i = 0; i < fp.items.size(); ++i) {
            Object item = fp.items.get(i);
            if (item instanceof String) {
                if (gotMm) {
                    mmss.append(fp.quoteLiteral(item.toString()));
                }
            } else {
                char ch = item.toString().charAt(0);
                if (ch == 'm') {
                    gotMm = true;
                    mmss.append(item);
                } else if (ch == 's') {
                    if (!gotMm) {
                        break; // failed
                    }
                    mmss.append(item);
                    addPattern(mmss.toString(), false, returnInfo);
                    break;
                } else if (gotMm || ch == 'z' || ch == 'Z' || ch == 'v' || ch == 'V') {
                    break; // failed
                }
            }
        }
        // to get hh:mm, we strip (literal ss) and (literal S)
        // the easiest way to do this is to mark the stuff we want to nuke, then remove it in a second pass.
        BitSet variables = new BitSet();
        BitSet nuke = new BitSet();
        for (int i = 0; i < fp.items.size(); ++i) {
            Object item = fp.items.get(i);
            if (item instanceof VariableField) {
                variables.set(i);
                char ch = item.toString().charAt(0);
                if (ch == 's' || ch == 'S') {
                    nuke.set(i);
                    for (int j = i-1; j >= 0; ++j) {
                        if (variables.get(j)) break;
                        nuke.set(i);
                    }
                }
            }
        }
        String hhmm = getFilteredPattern(fp, nuke);
        addPattern(hhmm, false, returnInfo);
    }

    private static String getFilteredPattern(FormatParser fp, BitSet nuke) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fp.items.size(); ++i) {
            if (nuke.get(i)) continue;
            Object item = fp.items.get(i);
            if (item instanceof String) {
                result.append(fp.quoteLiteral(item.toString()));
            } else {
                result.append(item.toString());
            }
        }
        return result.toString();
    }

    /*private static int getAppendNameNumber(String string) {
        for (int i = 0; i < CLDR_FIELD_NAME.length; ++i) {
            if (CLDR_FIELD_NAME[i].equals(string)) return i;
        }
        return -1;
    }*/

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static int getAppendFormatNumber(UResource.Key key) {
        for (int i = 0; i < CLDR_FIELD_APPEND.length; ++i) {
            if (key.contentEquals(CLDR_FIELD_APPEND[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static int getAppendFormatNumber(String string) {
        for (int i = 0; i < CLDR_FIELD_APPEND.length; ++i) {
            if (CLDR_FIELD_APPEND[i].equals(string)) {
                return i;
            }
        }
        return -1;
    }

    private static int getCLDRFieldNumber(UResource.Key key) {
        for (int i = 0; i < CLDR_FIELD_NAME.length; ++i) {
            if (key.contentEquals(CLDR_FIELD_NAME[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return the best pattern matching the input skeleton. It is guaranteed to
     * have all of the fields in the skeleton.
     * <p>Example code:{@sample external/icu/android_icu4j/src/samples/java/android/icu/samples/text/datetimepatterngenerator/DateTimePatternGeneratorSample.java getBestPatternExample}
     * @param skeleton The skeleton is a pattern containing only the variable fields.
     *            For example, "MMMdd" and "mmhh" are skeletons.
     * @return Best pattern matching the input skeleton.
     */
    public String getBestPattern(String skeleton) {
        return getBestPattern(skeleton, null, MATCH_NO_OPTIONS);
    }

    /**
     * Return the best pattern matching the input skeleton. It is guaranteed to
     * have all of the fields in the skeleton.
     *
     * @param skeleton The skeleton is a pattern containing only the variable fields.
     *            For example, "MMMdd" and "mmhh" are skeletons.
     * @param options MATCH_xxx options for forcing the length of specified fields in
     *            the returned pattern to match those in the skeleton (when this would
     *            not happen otherwise). For default behavior, use MATCH_NO_OPTIONS.
     * @return Best pattern matching the input skeleton (and options).
     */
    public String getBestPattern(String skeleton, int options) {
        return getBestPattern(skeleton, null, options);
    }

    /*
     * getBestPattern which takes optional skip matcher
     */
    private String getBestPattern(String skeleton, DateTimeMatcher skipMatcher, int options) {
        EnumSet<DTPGflags> flags = EnumSet.noneOf(DTPGflags.class);
        // Replace hour metacharacters 'j', 'C', and 'J', set flags as necessary
        StringBuilder skeletonCopy = new StringBuilder(skeleton);
        boolean inQuoted = false;
        for (int patPos = 0; patPos < skeleton.length(); patPos++) {
            char patChr = skeleton.charAt(patPos);
            if (patChr == '\'') {
                inQuoted = !inQuoted;
            } else if (!inQuoted) {
                if (patChr == 'j') {
                    skeletonCopy.setCharAt(patPos, defaultHourFormatChar);
                } else if (patChr == 'C') {
                    String preferred = allowedHourFormats[0];
                    skeletonCopy.setCharAt(patPos, preferred.charAt(0));
                    final DTPGflags alt = DTPGflags.getFlag(preferred);
                    if (alt != null) {
                        flags.add(alt);
                    }
                } else if (patChr == 'J') {
                    // Get pattern for skeleton with H, then (in adjustFieldTypes)
                    // replace H or k with defaultHourFormatChar
                    skeletonCopy.setCharAt(patPos, 'H');
                    flags.add(DTPGflags.SKELETON_USES_CAP_J);
                }
            }
        }

        String datePattern, timePattern;
        synchronized(this) {
            current.set(skeletonCopy.toString(), fp, false);
            PatternWithMatcher bestWithMatcher = getBestRaw(current, -1, _distanceInfo, skipMatcher);
            if (_distanceInfo.missingFieldMask == 0 && _distanceInfo.extraFieldMask == 0) {
                // we have a good item. Adjust the field types
                return adjustFieldTypes(bestWithMatcher, current, flags, options);
            }
            int neededFields = current.getFieldMask();

            // otherwise break up by date and time.
            datePattern = getBestAppending(current, neededFields & DATE_MASK, _distanceInfo, skipMatcher, flags, options);
            timePattern = getBestAppending(current, neededFields & TIME_MASK, _distanceInfo, skipMatcher, flags, options);
        }

        if (datePattern == null) return timePattern == null ? "" : timePattern;
        if (timePattern == null) return datePattern;
        return SimpleFormatterImpl.formatRawPattern(
                getDateTimeFormat(), 2, 2, timePattern, datePattern);
    }

    /**
     * PatternInfo supplies output parameters for addPattern(...). It is used because
     * Java doesn't have real output parameters. It is treated like a struct (eg
     * Point), so all fields are public.
     */
    public static final class PatternInfo { // struct for return information
        /**
         */
        public static final int OK = 0;

        /**
         */
        public static final int BASE_CONFLICT = 1;

        /**
         */
        public static final int CONFLICT = 2;

        /**
         */
        public int status;

        /**
         */
        public String conflictingPattern;

        /**
         * Simple constructor, since this is treated like a struct.
         */
        public PatternInfo() {
        }
    }

    /**
     * Adds a pattern to the generator. If the pattern has the same skeleton as
     * an existing pattern, and the override parameter is set, then the previous
     * value is overridden. Otherwise, the previous value is retained. In either
     * case, the conflicting information is returned in PatternInfo.
     * <p>
     * Note that single-field patterns (like "MMM") are automatically added, and
     * don't need to be added explicitly!
     * * <p>Example code:{@sample external/icu/android_icu4j/src/samples/java/android/icu/samples/text/datetimepatterngenerator/DateTimePatternGeneratorSample.java addPatternExample}
     * @param pattern Pattern to add.
     * @param override When existing values are to be overridden use true, otherwise
     *            use false.
     * @param returnInfo Returned information.
     */
    public DateTimePatternGenerator addPattern(String pattern, boolean override, PatternInfo returnInfo) {
        return addPatternWithSkeleton(pattern, null, override, returnInfo);
    }

    /**
     * addPatternWithSkeleton:
     * If skeletonToUse is specified, then an availableFormats entry is being added. In this case:
     * 1. We pass that skeleton to DateTimeMatcher().set instead of having it derive a skeleton from the pattern.
     * 2. If the new entry's skeleton or basePattern does match an existing entry but that entry also had a skeleton specified
     * (i.e. it was also from availableFormats), then the new entry does not override it regardless of the value of the override
     * parameter. This prevents later availableFormats entries from a parent locale overriding earlier ones from the actual
     * specified locale. However, availableFormats entries *should* override entries with matching skeleton whose skeleton was
     * derived (i.e. entries derived from the standard date/time patters for the specified locale).
     * 3. When adding the pattern (skeleton2pattern.put, basePattern_pattern.put), we set a field to indicate that the added
     * entry had a specified skeleton.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public DateTimePatternGenerator addPatternWithSkeleton(String pattern, String skeletonToUse, boolean override, PatternInfo returnInfo) {
        checkFrozen();
        DateTimeMatcher matcher;
        if (skeletonToUse == null) {
            matcher = new DateTimeMatcher().set(pattern, fp, false);
        } else {
            matcher = new DateTimeMatcher().set(skeletonToUse, fp, false);
        }
        String basePattern = matcher.getBasePattern();
        // We only care about base conflicts - and replacing the pattern associated with a base - if:
        // 1. the conflicting previous base pattern did *not* have an explicit skeleton; in that case the previous
        // base + pattern combination was derived from either (a) a canonical item, (b) a standard format, or
        // (c) a pattern specified programmatically with a previous call to addPattern (which would only happen
        // if we are getting here from a subsequent call to addPattern).
        // 2. a skeleton is specified for the current pattern, but override=false; in that case we are checking
        // availableFormats items from root, which should not override any previous entry with the same base.
        PatternWithSkeletonFlag previousPatternWithSameBase = basePattern_pattern.get(basePattern);
        if (previousPatternWithSameBase != null && (!previousPatternWithSameBase.skeletonWasSpecified || (skeletonToUse != null && !override))) {
            returnInfo.status = PatternInfo.BASE_CONFLICT;
            returnInfo.conflictingPattern = previousPatternWithSameBase.pattern;
            if (!override) {
                return this;
            }
        }
        // The only time we get here with override=true and skeletonToUse!=null is when adding availableFormats
        // items from CLDR data. In that case, we don't want an item from a parent locale to replace an item with
        // same skeleton from the specified locale, so skip the current item if skeletonWasSpecified is true for
        // the previously-specified conflicting item.
        PatternWithSkeletonFlag previousValue = skeleton2pattern.get(matcher);
        if (previousValue != null) {
            returnInfo.status = PatternInfo.CONFLICT;
            returnInfo.conflictingPattern = previousValue.pattern;
            if (!override || (skeletonToUse != null && previousValue.skeletonWasSpecified)) return this;
        }
        returnInfo.status = PatternInfo.OK;
        returnInfo.conflictingPattern = "";
        PatternWithSkeletonFlag patWithSkelFlag = new PatternWithSkeletonFlag(pattern,skeletonToUse != null);
        if (DEBUG) {
            System.out.println(matcher + " => " + patWithSkelFlag);
        }
        skeleton2pattern.put(matcher, patWithSkelFlag);
        basePattern_pattern.put(basePattern, patWithSkelFlag);
        return this;
    }

    /**
     * Utility to return a unique skeleton from a given pattern. For example,
     * both "MMM-dd" and "dd/MMM" produce the skeleton "MMMdd".
     *
     * @param pattern Input pattern, such as "dd/MMM"
     * @return skeleton, such as "MMMdd"
     */
    public String getSkeleton(String pattern) {
        synchronized (this) { // synchronized since a getter must be thread-safe
            current.set(pattern, fp, false);
            return current.toString();
        }
    }

    /**
     * Same as getSkeleton, but allows duplicates
     *
     * @param pattern Input pattern, such as "dd/MMM"
     * @return skeleton, such as "MMMdd"
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public String getSkeletonAllowingDuplicates(String pattern) {
        synchronized (this) { // synchronized since a getter must be thread-safe
            current.set(pattern, fp, true);
            return current.toString();
        }
    }

    /**
     * Same as getSkeleton, but allows duplicates
     * and returns a string using canonical pattern chars
     *
     * @param pattern Input pattern, such as "ccc, d LLL"
     * @return skeleton, such as "MMMEd"
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public String getCanonicalSkeletonAllowingDuplicates(String pattern) {
        synchronized (this) { // synchronized since a getter must be thread-safe
            current.set(pattern, fp, true);
            return current.toCanonicalString();
        }
    }

    /**
     * Utility to return a unique base skeleton from a given pattern. This is
     * the same as the skeleton, except that differences in length are minimized
     * so as to only preserve the difference between string and numeric form. So
     * for example, both "MMM-dd" and "d/MMM" produce the skeleton "MMMd"
     * (notice the single d).
     *
     * @param pattern Input pattern, such as "dd/MMM"
     * @return skeleton, such as "MMMdd"
     */
    public String getBaseSkeleton(String pattern) {
        synchronized (this) { // synchronized since a getter must be thread-safe
            current.set(pattern, fp, false);
            return current.getBasePattern();
        }
    }

    /**
     * Return a list of all the skeletons (in canonical form) from this class,
     * and the patterns that they map to.
     *
     * @param result an output Map in which to place the mapping from skeleton to
     *            pattern. If you want to see the internal order being used,
     *            supply a LinkedHashMap. If the input value is null, then a
     *            LinkedHashMap is allocated.
     *            <p>
     *            <i>Issue: an alternate API would be to just return a list of
     *            the skeletons, and then have a separate routine to get from
     *            skeleton to pattern.</i>
     * @return the input Map containing the values.
     */
    public Map<String, String> getSkeletons(Map<String, String> result) {
        if (result == null) {
            result = new LinkedHashMap<String, String>();
        }
        for (DateTimeMatcher item : skeleton2pattern.keySet()) {
            PatternWithSkeletonFlag patternWithSkelFlag = skeleton2pattern.get(item);
            String pattern = patternWithSkelFlag.pattern;
            if (CANONICAL_SET.contains(pattern)) {
                continue;
            }
            result.put(item.toString(), pattern);
        }
        return result;
    }

    /**
     * Return a list of all the base skeletons (in canonical form) from this class
     */
    public Set<String> getBaseSkeletons(Set<String> result) {
        if (result == null) {
            result = new HashSet<String>();
        }
        result.addAll(basePattern_pattern.keySet());
        return result;
    }

    /**
     * Adjusts the field types (width and subtype) of a pattern to match what is
     * in a skeleton. That is, if you supply a pattern like "d-M H:m", and a
     * skeleton of "MMMMddhhmm", then the input pattern is adjusted to be
     * "dd-MMMM hh:mm". This is used internally to get the best match for the
     * input skeleton, but can also be used externally.
     * <p>Example code:{@sample external/icu/android_icu4j/src/samples/java/android/icu/samples/text/datetimepatterngenerator/DateTimePatternGeneratorSample.java replaceFieldTypesExample}
     * @param pattern input pattern
     * @param skeleton For the pattern to match to.
     * @return pattern adjusted to match the skeleton fields widths and subtypes.
     */
    public String replaceFieldTypes(String pattern, String skeleton) {
        return replaceFieldTypes(pattern, skeleton, MATCH_NO_OPTIONS);
    }

    /**
     * Adjusts the field types (width and subtype) of a pattern to match what is
     * in a skeleton. That is, if you supply a pattern like "d-M H:m", and a
     * skeleton of "MMMMddhhmm", then the input pattern is adjusted to be
     * "dd-MMMM hh:mm". This is used internally to get the best match for the
     * input skeleton, but can also be used externally.
     *
     * @param pattern input pattern
     * @param skeleton For the pattern to match to.
     * @param options MATCH_xxx options for forcing the length of specified fields in
     *            the returned pattern to match those in the skeleton (when this would
     *            not happen otherwise). For default behavior, use MATCH_NO_OPTIONS.
     * @return pattern adjusted to match the skeleton fields widths and subtypes.
     */
    public String replaceFieldTypes(String pattern, String skeleton, int options) {
        synchronized (this) { // synchronized since a getter must be thread-safe
            PatternWithMatcher patternNoMatcher = new PatternWithMatcher(pattern, null);
            return adjustFieldTypes(patternNoMatcher, current.set(skeleton, fp, false), EnumSet.noneOf(DTPGflags.class), options);
        }
    }

    /**
     * The date time format is a message format pattern used to compose date and
     * time patterns. The default value is "{1} {0}", where {1} will be replaced
     * by the date pattern and {0} will be replaced by the time pattern.
     * <p>
     * This is used when the input skeleton contains both date and time fields,
     * but there is not a close match among the added patterns. For example,
     * suppose that this object was created by adding "dd-MMM" and "hh:mm", and
     * its datetimeFormat is the default "{1} {0}". Then if the input skeleton
     * is "MMMdhmm", there is not an exact match, so the input skeleton is
     * broken up into two components "MMMd" and "hmm". There are close matches
     * for those two skeletons, so the result is put together with this pattern,
     * resulting in "d-MMM h:mm".
     *
     * @param dateTimeFormat message format pattern, where {1} will be replaced by the date
     *            pattern and {0} will be replaced by the time pattern.
     */
    public void setDateTimeFormat(String dateTimeFormat) {
        checkFrozen();
        this.dateTimeFormat = dateTimeFormat;
    }

    /**
     * Getter corresponding to setDateTimeFormat.
     *
     * @return pattern
     */
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    /**
     * The decimal value is used in formatting fractions of seconds. If the
     * skeleton contains fractional seconds, then this is used with the
     * fractional seconds. For example, suppose that the input pattern is
     * "hhmmssSSSS", and the best matching pattern internally is "H:mm:ss", and
     * the decimal string is ",". Then the resulting pattern is modified to be
     * "H:mm:ss,SSSS"
     *
     * @param decimal The decimal to set to.
     */
    public void setDecimal(String decimal) {
        checkFrozen();
        this.decimal = decimal;
    }

    /**
     * Getter corresponding to setDecimal.
     * @return string corresponding to the decimal point
     */
    public String getDecimal() {
        return decimal;
    }

    /**
     * Redundant patterns are those which if removed, make no difference in the
     * resulting getBestPattern values. This method returns a list of them, to
     * help check the consistency of the patterns used to build this generator.
     *
     * @param output stores the redundant patterns that are removed. To get these
     *            in internal order, supply a LinkedHashSet. If null, a
     *            collection is allocated.
     * @return the collection with added elements.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public Collection<String> getRedundants(Collection<String> output) {
        synchronized (this) { // synchronized since a getter must be thread-safe
            if (output == null) {
                output = new LinkedHashSet<String>();
            }
            for (DateTimeMatcher cur : skeleton2pattern.keySet()) {
                PatternWithSkeletonFlag patternWithSkelFlag = skeleton2pattern.get(cur);
                String pattern = patternWithSkelFlag.pattern;
                if (CANONICAL_SET.contains(pattern)) {
                    continue;
                }
                String trial = getBestPattern(cur.toString(), cur, MATCH_NO_OPTIONS);
                if (trial.equals(pattern)) {
                    output.add(pattern);
                }
            }
            ///CLOVER:OFF
            //The following would never be called since the parameter is false
            //Eclipse stated the following is "dead code"
            /*if (false) { // ordered
                DateTimePatternGenerator results = new DateTimePatternGenerator();
                PatternInfo pinfo = new PatternInfo();
                for (DateTimeMatcher cur : skeleton2pattern.keySet()) {
                    String pattern = skeleton2pattern.get(cur);
                    if (CANONICAL_SET.contains(pattern)) {
                        continue;
                    }
                    //skipMatcher = current;
                    String trial = results.getBestPattern(cur.toString());
                    if (trial.equals(pattern)) {
                        output.add(pattern);
                    } else {
                        results.addPattern(pattern, false, pinfo);
                    }
                }
            }*/
            ///CLOVER:ON
            return output;
        }
    }

    // Field numbers, used for AppendItem functions

    /**
     */
    public static final int ERA = 0;

    /**
     */
    public static final int YEAR = 1;

    /**
     */
    public static final int QUARTER = 2;

    /**
     */
    public static final int MONTH = 3;

    /**
     */
    public static final int WEEK_OF_YEAR = 4;

    /**
     */
    public static final int WEEK_OF_MONTH = 5;

    /**
     */
    public static final int WEEKDAY = 6;

    /**
     */
    public static final int DAY = 7;

    /**
     */
    public static final int DAY_OF_YEAR = 8;

    /**
     */
    public static final int DAY_OF_WEEK_IN_MONTH = 9;

    /**
     */
    public static final int DAYPERIOD = 10;

    /**
     */
    public static final int HOUR = 11;

    /**
     */
    public static final int MINUTE = 12;

    /**
     */
    public static final int SECOND = 13;

    /**
     */
    public static final int FRACTIONAL_SECOND = 14;

    /**
     */
    public static final int ZONE = 15;

    /**
     * One more than the highest normal field number.
     * @deprecated ICU 58 The numeric value may change over time, see ICU ticket #12420.
     * @hide unsupported on Android
     */
    @Deprecated
    public static final int TYPE_LIMIT = 16;

    // Option masks for getBestPattern, replaceFieldTypes (individual masks may be ORed together)

    /**
     * Default option mask used for {@link #getBestPattern(String, int)}
     * and {@link #replaceFieldTypes(String, String, int)}.
     * @see #getBestPattern(String, int)
     * @see #replaceFieldTypes(String, String, int)
     */
    public static final int MATCH_NO_OPTIONS = 0;

    /**
     * Option mask for forcing the width of hour field.
     * @see #getBestPattern(String, int)
     * @see #replaceFieldTypes(String, String, int)
     */
    public static final int MATCH_HOUR_FIELD_LENGTH = 1 << HOUR;

    /**
     * Option mask for forcing  the width of minute field.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final int MATCH_MINUTE_FIELD_LENGTH = 1 << MINUTE;

    /**
     * Option mask for forcing  the width of second field.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final int MATCH_SECOND_FIELD_LENGTH = 1 << SECOND;

    /**
     * Option mask for forcing the width of all date and time fields.
     * @see #getBestPattern(String, int)
     * @see #replaceFieldTypes(String, String, int)
     */
    public static final int MATCH_ALL_FIELDS_LENGTH = (1 << TYPE_LIMIT) - 1;

    /**
     * An AppendItem format is a pattern used to append a field if there is no
     * good match. For example, suppose that the input skeleton is "GyyyyMMMd",
     * and there is no matching pattern internally, but there is a pattern
     * matching "yyyyMMMd", say "d-MM-yyyy". Then that pattern is used, plus the
     * G. The way these two are conjoined is by using the AppendItemFormat for G
     * (era). So if that value is, say "{0}, {1}" then the final resulting
     * pattern is "d-MM-yyyy, G".
     * <p>
     * There are actually three available variables: {0} is the pattern so far,
     * {1} is the element we are adding, and {2} is the name of the element.
     * <p>
     * This reflects the way that the CLDR data is organized.
     *
     * @param field such as ERA
     * @param value pattern, such as "{0}, {1}"
     */
    public void setAppendItemFormat(int field, String value) {
        checkFrozen();
        appendItemFormats[field] = value;
    }

    /**
     * Getter corresponding to setAppendItemFormats. Values below 0 or at or
     * above TYPE_LIMIT are illegal arguments.
     *
     * @param field The index to retrieve the append item formats.
     * @return append pattern for field
     */
    public String getAppendItemFormat(int field) {
        return appendItemFormats[field];
    }

    /**
     * Sets the names of fields, eg "era" in English for ERA. These are only
     * used if the corresponding AppendItemFormat is used, and if it contains a
     * {2} variable.
     * <p>
     * This reflects the way that the CLDR data is organized.
     *
     * @param field Index of the append item names.
     * @param value The value to set the item to.
     */
    public void setAppendItemName(int field, String value) {
        checkFrozen();
        appendItemNames[field] = value;
    }

    /**
     * Getter corresponding to setAppendItemNames. Values below 0 or at or above
     * TYPE_LIMIT are illegal arguments.
     *
     * @param field The index to get the append item name.
     * @return name for field
     */
    public String getAppendItemName(int field) {
        return appendItemNames[field];
    }

    /**
     * Determines whether a skeleton contains a single field
     *
     * @param skeleton The skeleton to determine if it contains a single field.
     * @return true or not
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static boolean isSingleField(String skeleton) {
        char first = skeleton.charAt(0);
        for (int i = 1; i < skeleton.length(); ++i) {
            if (skeleton.charAt(i) != first) return false;
        }
        return true;
    }

    /**
     * Add key to HashSet cldrAvailableFormatKeys.
     *
     * @param key of the availableFormats in CLDR
     */
    private void setAvailableFormat(String key) {
        checkFrozen();
        cldrAvailableFormatKeys.add(key);
    }

    /**
     * This function checks the corresponding slot of CLDR_AVAIL_FORMAT_KEY[]
     * has been added to DateTimePatternGenerator.
     * The function is to avoid the duplicate availableFomats added to
     * the pattern map from parent locales.
     *
     * @param key of the availableFormatMask in CLDR
     * @return TRUE if the corresponding slot of CLDR_AVAIL_FORMAT_KEY[]
     * has been added to DateTimePatternGenerator.
     */
    private boolean isAvailableFormatSet(String key) {
        return cldrAvailableFormatKeys.contains(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateTimePatternGenerator freeze() {
        frozen = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateTimePatternGenerator cloneAsThawed() {
        DateTimePatternGenerator result = (DateTimePatternGenerator) (this.clone());
        frozen = false;
        return result;
    }

    /**
     * Returns a copy of this <code>DateTimePatternGenerator</code> object.
     * @return A copy of this <code>DateTimePatternGenerator</code> object.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            DateTimePatternGenerator result = (DateTimePatternGenerator) (super.clone());
            result.skeleton2pattern = (TreeMap<DateTimeMatcher, PatternWithSkeletonFlag>) skeleton2pattern.clone();
            result.basePattern_pattern = (TreeMap<String, PatternWithSkeletonFlag>) basePattern_pattern.clone();
            result.appendItemFormats = appendItemFormats.clone();
            result.appendItemNames = appendItemNames.clone();
            result.current = new DateTimeMatcher();
            result.fp = new FormatParser();
            result._distanceInfo = new DistanceInfo();

            result.frozen = false;
            return result;
        } catch (CloneNotSupportedException e) {
            ///CLOVER:OFF
            throw new ICUCloneNotSupportedException("Internal Error", e);
            ///CLOVER:ON
        }
    }

    /**
     * Utility class for FormatParser. Immutable class that is only used to mark
     * the difference between a variable field and a literal string. Each
     * variable field must consist of 1 to n variable characters, representing
     * date format fields. For example, "VVVV" is valid while "V4" is not, nor
     * is "44".
     *
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static class VariableField {
        private final String string;
        private final int canonicalIndex;

        /**
         * Create a variable field: equivalent to VariableField(string,false);
         * @param string The string for the variable field.
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public VariableField(String string) {
            this(string, false);
        }
        /**
         * Create a variable field
         * @param string The string for the variable field
         * @param strict If true, then only allows exactly those lengths specified by CLDR for variables. For example, "hh:mm aa" would throw an exception.
         * @throws IllegalArgumentException if the variable field is not valid.
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public VariableField(String string, boolean strict) {
            canonicalIndex = DateTimePatternGenerator.getCanonicalIndex(string, strict);
            if (canonicalIndex < 0) {
                throw new IllegalArgumentException("Illegal datetime field:\t"
                        + string);
            }
            this.string = string;
        }

        /**
         * Get the main type of this variable. These types are ERA, QUARTER,
         * MONTH, DAY, WEEK_OF_YEAR, WEEK_OF_MONTH, WEEKDAY, DAY, DAYPERIOD
         * (am/pm), HOUR, MINUTE, SECOND,FRACTIONAL_SECOND, ZONE.
         * @return main type.
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public int getType() {
            return types[canonicalIndex][1];
        }

        /**
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public static String getCanonicalCode(int type) {
            try {
                return CANONICAL_ITEMS[type];
            } catch (Exception e) {
                return String.valueOf(type);
            }
        }
        /**
         * Check if the type of this variable field is numeric.
         * @return true if the type of this variable field is numeric.
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public boolean isNumeric() {
            return types[canonicalIndex][2] > 0;
        }

        /**
         * Private method.
         */
        private int getCanonicalIndex() {
            return canonicalIndex;
        }

        /**
         * Get the string represented by this variable.
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Override
        @Deprecated
        public String toString() {
            return string;
        }
    }

    /**
     * This class provides mechanisms for parsing a SimpleDateFormat pattern
     * or generating a new pattern, while handling the quoting. It represents
     * the result of the parse as a list of items, where each item is either a
     * literal string or a variable field. When parsing It can be used to find
     * out which variable fields are in a date format, and in what order, such
     * as for presentation in a UI as separate text entry fields. It can also be
     * used to construct new SimpleDateFormats.
     * <p>Example:
     * <pre>
    public boolean containsZone(String pattern) {
        for (Iterator it = formatParser.set(pattern).getItems().iterator(); it.hasNext();) {
            Object item = it.next();
            if (item instanceof VariableField) {
                VariableField variableField = (VariableField) item;
                if (variableField.getType() == DateTimePatternGenerator.ZONE) {
                    return true;
                }
            }
        }
        return false;
    }
     *  </pre>
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    static public class FormatParser {
        private static final UnicodeSet SYNTAX_CHARS = new UnicodeSet("[a-zA-Z]").freeze();
        private static final UnicodeSet QUOTING_CHARS = new UnicodeSet("[[[:script=Latn:][:script=Cyrl:]]&[[:L:][:M:]]]").freeze();
        private transient PatternTokenizer tokenizer = new PatternTokenizer()
        .setSyntaxCharacters(SYNTAX_CHARS)
        .setExtraQuotingCharacters(QUOTING_CHARS)
        .setUsingQuote(true);
        private List<Object> items = new ArrayList<Object>();

        /**
         * Construct an empty date format parser, to which strings and variables can be added with set(...).
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public FormatParser() {
        }

        /**
         * Parses the string into a list of items.
         * @param string The string to parse.
         * @return this, for chaining
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        final public FormatParser set(String string) {
            return set(string, false);
        }

        /**
         * Parses the string into a list of items, taking into account all of the quoting that may be going on.
         * @param string  The string to parse.
         * @param strict If true, then only allows exactly those lengths specified by CLDR for variables. For example, "hh:mm aa" would throw an exception.
         * @return this, for chaining
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public FormatParser set(String string, boolean strict) {
            items.clear();
            if (string.length() == 0) return this;
            tokenizer.setPattern(string);
            StringBuffer buffer = new StringBuffer();
            StringBuffer variable = new StringBuffer();
            while (true) {
                buffer.setLength(0);
                int status = tokenizer.next(buffer);
                if (status == PatternTokenizer.DONE) break;
                if (status == PatternTokenizer.SYNTAX) {
                    if (variable.length() != 0 && buffer.charAt(0) != variable.charAt(0)) {
                        addVariable(variable, false);
                    }
                    variable.append(buffer);
                } else {
                    addVariable(variable, false);
                    items.add(buffer.toString());
                }
            }
            addVariable(variable, false);
            return this;
        }

        private void addVariable(StringBuffer variable, boolean strict) {
            if (variable.length() != 0) {
                items.add(new VariableField(variable.toString(), strict));
                variable.setLength(0);
            }
        }

        //        /** Private method. Return a collection of fields. These will be a mixture of literal Strings and VariableFields. Any "a" variable field is removed.
        //         * @param output List to append the items to. If null, is allocated as an ArrayList.
        //         * @return list
        //         */
        //        private List getVariableFields(List output) {
        //            if (output == null) output = new ArrayList();
        //            main:
        //                for (Iterator it = items.iterator(); it.hasNext();) {
        //                    Object item = it.next();
        //                    if (item instanceof VariableField) {
        //                        String s = item.toString();
        //                        switch(s.charAt(0)) {
        //                        //case 'Q': continue main; // HACK
        //                        case 'a': continue main; // remove
        //                        }
        //                        output.add(item);
        //                    }
        //                }
        //            //System.out.println(output);
        //            return output;
        //        }

        //        /**
        //         * Produce a string which concatenates all the variables. That is, it is the logically the same as the input with all literals removed.
        //         * @return a string which is a concatenation of all the variable fields
        //         */
        //        public String getVariableFieldString() {
        //            List list = getVariableFields(null);
        //            StringBuffer result = new StringBuffer();
        //            for (Iterator it = list.iterator(); it.hasNext();) {
        //                String item = it.next().toString();
        //                result.append(item);
        //            }
        //            return result.toString();
        //        }

        /**
         * Returns modifiable list which is a mixture of Strings and VariableFields, in the order found during parsing. The strings represent literals, and have all quoting removed. Thus the string "dd 'de' MM" will parse into three items:
         * <pre>
         * VariableField: dd
         * String: " de "
         * VariableField: MM
         * </pre>
         * The list is modifiable, so you can add any strings or variables to it, or remove any items.
         * @return modifiable list of items.
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public List<Object> getItems() {
            return items;
        }

        /** Provide display form of formatted input. Each literal string is quoted if necessary.. That is, if the input was "hh':'mm", the result would be "hh:mm", since the ":" doesn't need quoting. See quoteLiteral().
         * @return printable output string
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Override
        @Deprecated
        public String toString() {
            return toString(0, items.size());
        }

        /**
         * Provide display form of a segment of the parsed input. Each literal string is minimally quoted. That is, if the input was "hh':'mm", the result would be "hh:mm", since the ":" doesn't need quoting. See quoteLiteral().
         * @param start item to start from
         * @param limit last item +1
         * @return printable output string
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public String toString(int start, int limit) {
            StringBuilder result = new StringBuilder();
            for (int i = start; i < limit; ++i) {
                Object item = items.get(i);
                if (item instanceof String) {
                    String itemString = (String) item;
                    result.append(tokenizer.quoteLiteral(itemString));
                } else {
                    result.append(items.get(i).toString());
                }
            }
            return result.toString();
        }

        /**
         * Returns true if it has a mixture of date and time variable fields: that is, at least one date variable and at least one time variable.
         * @return true or false
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public boolean hasDateAndTimeFields() {
            int foundMask = 0;
            for (Object item : items) {
                if (item instanceof VariableField) {
                    int type = ((VariableField)item).getType();
                    foundMask |= 1 << type;
                }
            }
            boolean isDate = (foundMask & DATE_MASK) != 0;
            boolean isTime = (foundMask & TIME_MASK) != 0;
            return isDate && isTime;
        }

        //        /**
        //         * Internal routine
        //         * @param value
        //         * @param result
        //         * @return list
        //         */
        //        public List getAutoPatterns(String value, List result) {
        //            if (result == null) result = new ArrayList();
        //            int fieldCount = 0;
        //            int minField = Integer.MAX_VALUE;
        //            int maxField = Integer.MIN_VALUE;
        //            for (Iterator it = items.iterator(); it.hasNext();) {
        //                Object item = it.next();
        //                if (item instanceof VariableField) {
        //                    try {
        //                        int type = ((VariableField)item).getType();
        //                        if (minField > type) minField = type;
        //                        if (maxField < type) maxField = type;
        //                        if (type == ZONE || type == DAYPERIOD || type == WEEKDAY) return result; // skip anything with zones
        //                        fieldCount++;
        //                    } catch (Exception e) {
        //                        return result; // if there are any funny fields, return
        //                    }
        //                }
        //            }
        //            if (fieldCount < 3) return result; // skip
        //            // trim from start
        //            // trim first field IF there are no letters around it
        //            // and it is either the min or the max field
        //            // first field is either 0 or 1
        //            for (int i = 0; i < items.size(); ++i) {
        //                Object item = items.get(i);
        //                if (item instanceof VariableField) {
        //                    int type = ((VariableField)item).getType();
        //                    if (type != minField && type != maxField) break;
        //
        //                    if (i > 0) {
        //                        Object previousItem = items.get(0);
        //                        if (alpha.containsSome(previousItem.toString())) break;
        //                    }
        //                    int start = i+1;
        //                    if (start < items.size()) {
        //                        Object nextItem = items.get(start);
        //                        if (nextItem instanceof String) {
        //                            if (alpha.containsSome(nextItem.toString())) break;
        //                            start++; // otherwise skip over string
        //                        }
        //                    }
        //                    result.add(toString(start, items.size()));
        //                    break;
        //                }
        //            }
        //            // now trim from end
        //            for (int i = items.size()-1; i >= 0; --i) {
        //                Object item = items.get(i);
        //                if (item instanceof VariableField) {
        //                    int type = ((VariableField)item).getType();
        //                    if (type != minField && type != maxField) break;
        //                    if (i < items.size() - 1) {
        //                        Object previousItem = items.get(items.size() - 1);
        //                        if (alpha.containsSome(previousItem.toString())) break;
        //                    }
        //                    int end = i-1;
        //                    if (end > 0) {
        //                        Object nextItem = items.get(end);
        //                        if (nextItem instanceof String) {
        //                            if (alpha.containsSome(nextItem.toString())) break;
        //                            end--; // otherwise skip over string
        //                        }
        //                    }
        //                    result.add(toString(0, end+1));
        //                    break;
        //                }
        //            }
        //
        //            return result;
        //        }

        //        private static UnicodeSet alpha = new UnicodeSet("[:alphabetic:]");

        //        private int getType(Object item) {
        //            String s = item.toString();
        //            int canonicalIndex = getCanonicalIndex(s);
        //            if (canonicalIndex < 0) {
        //                throw new IllegalArgumentException("Illegal field:\t"
        //                        + s);
        //            }
        //            int type = types[canonicalIndex][1];
        //            return type;
        //        }

        /**
         *  Each literal string is quoted as needed. That is, the ' quote marks will only be added if needed. The exact pattern of quoting is not guaranteed, thus " de la " could be quoted as " 'de la' " or as " 'de' 'la' ".
         * @param string The string to check.
         * @return string with quoted literals
         * @deprecated This API is ICU internal only.
         * @hide original deprecated declaration
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public Object quoteLiteral(String string) {
            return tokenizer.quoteLiteral(string);
        }

    }

    /**
     * Used by CLDR tooling; not in ICU4C.
     * Note, this will not work correctly with normal skeletons, since fields
     * that should be related in the two skeletons being compared - like EEE and
     * ccc, or y and U - will not be sorted in the same relative place as each
     * other when iterating over both TreeSets being compare, using TreeSet's
     * "natural" code point ordering (this could be addressed by initializing
     * the TreeSet with a comparator that compares fields first by their index
     * from getCanonicalIndex()). However if comparing canonical skeletons from
     * getCanonicalSkeletonAllowingDuplicates it will be OK regardless, since
     * in these skeletons all fields are normalized to the canonical pattern
     * char for those fields - M or L to M, E or c to E, y or U to y, etc. -
     * so corresponding fields will sort in the same way for both TreeMaps.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public boolean skeletonsAreSimilar(String id, String skeleton) {
        if (id.equals(skeleton)) {
            return true; // fast path
        }
        // must clone array, make sure items are in same order.
        TreeSet<String> parser1 = getSet(id);
        TreeSet<String> parser2 = getSet(skeleton);
        if (parser1.size() != parser2.size()) {
            return false;
        }
        Iterator<String> it2 = parser2.iterator();
        for (String item : parser1) {
            int index1 = getCanonicalIndex(item, false);
            String item2 = it2.next(); // same length so safe
            int index2 = getCanonicalIndex(item2, false);
            if (types[index1][1] != types[index2][1]) {
                return false;
            }
        }
        return true;
    }

    private TreeSet<String> getSet(String id) {
        final List<Object> items = fp.set(id).getItems();
        TreeSet<String> result = new TreeSet<String>();
        for (Object obj : items) {
            final String item = obj.toString();
            if (item.startsWith("G") || item.startsWith("a")) {
                continue;
            }
            result.add(item);
        }
        return result;
    }

    // ========= PRIVATES ============

    private static class PatternWithMatcher {
        public String pattern;
        public DateTimeMatcher matcherWithSkeleton;
        // Simple constructor
        public PatternWithMatcher(String pat, DateTimeMatcher matcher) {
            pattern = pat;
            matcherWithSkeleton = matcher;
        }
    }
    private static class PatternWithSkeletonFlag {
        public String pattern;
        public boolean skeletonWasSpecified;
        // Simple constructor
        public PatternWithSkeletonFlag(String pat, boolean skelSpecified) {
            pattern = pat;
            skeletonWasSpecified = skelSpecified;
        }
        @Override
        public String toString() {
            return pattern + "," + skeletonWasSpecified;
        }
    }
    private TreeMap<DateTimeMatcher, PatternWithSkeletonFlag> skeleton2pattern = new TreeMap<DateTimeMatcher, PatternWithSkeletonFlag>(); // items are in priority order
    private TreeMap<String, PatternWithSkeletonFlag> basePattern_pattern = new TreeMap<String, PatternWithSkeletonFlag>(); // items are in priority order
    private String decimal = "?";
    private String dateTimeFormat = "{1} {0}";
    private String[] appendItemFormats = new String[TYPE_LIMIT];
    private String[] appendItemNames = new String[TYPE_LIMIT];
    private char defaultHourFormatChar = 'H';
    //private boolean chineseMonthHack = false;
    //private boolean isComplete = false;
    private volatile boolean frozen = false;

    private transient DateTimeMatcher current = new DateTimeMatcher();
    private transient FormatParser fp = new FormatParser();
    private transient DistanceInfo _distanceInfo = new DistanceInfo();

    private String[] allowedHourFormats;

    private static final int FRACTIONAL_MASK = 1<<FRACTIONAL_SECOND;
    private static final int SECOND_AND_FRACTIONAL_MASK = (1<<SECOND) | (1<<FRACTIONAL_SECOND);

    // Cache for DateTimePatternGenerator
    private static ICUCache<String, DateTimePatternGenerator> DTPNG_CACHE = new SimpleCache<String, DateTimePatternGenerator>();

    private void checkFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
    }

    /**
     * We only get called here if we failed to find an exact skeleton. We have broken it into date + time, and look for the pieces.
     * If we fail to find a complete skeleton, we compose in a loop until we have all the fields.
     */
    private String getBestAppending(DateTimeMatcher source, int missingFields, DistanceInfo distInfo, DateTimeMatcher skipMatcher, EnumSet<DTPGflags> flags, int options) {
        String resultPattern = null;
        if (missingFields != 0) {
            PatternWithMatcher resultPatternWithMatcher = getBestRaw(source, missingFields, distInfo, skipMatcher);
            resultPattern = adjustFieldTypes(resultPatternWithMatcher, source, flags, options);

            while (distInfo.missingFieldMask != 0) { // precondition: EVERY single field must work!

                // special hack for SSS. If we are missing SSS, and we had ss but found it, replace the s field according to the
                // number separator
                if ((distInfo.missingFieldMask & SECOND_AND_FRACTIONAL_MASK) == FRACTIONAL_MASK
                        && (missingFields & SECOND_AND_FRACTIONAL_MASK) == SECOND_AND_FRACTIONAL_MASK) {
                    resultPatternWithMatcher.pattern = resultPattern;
                    flags = EnumSet.copyOf(flags);
                    flags.add(DTPGflags.FIX_FRACTIONAL_SECONDS);
                    resultPattern = adjustFieldTypes(resultPatternWithMatcher, source, flags, options);
                    distInfo.missingFieldMask &= ~FRACTIONAL_MASK; // remove bit
                    continue;
                }

                int startingMask = distInfo.missingFieldMask;
                PatternWithMatcher tempWithMatcher = getBestRaw(source, distInfo.missingFieldMask, distInfo, skipMatcher);
                String temp = adjustFieldTypes(tempWithMatcher, source, flags, options);
                int foundMask = startingMask & ~distInfo.missingFieldMask;
                int topField = getTopBitNumber(foundMask);
                resultPattern = SimpleFormatterImpl.formatRawPattern(
                        getAppendFormat(topField), 2, 3, resultPattern, temp, getAppendName(topField));
            }
        }
        return resultPattern;
    }

    private String getAppendName(int foundMask) {
        return "'" + appendItemNames[foundMask] + "'";
    }
    private String getAppendFormat(int foundMask) {
        return appendItemFormats[foundMask];
    }

    //    /**
    //     * @param current2
    //     * @return
    //     */
    //    private String adjustSeconds(DateTimeMatcher current2) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }

    /**
     * @param foundMask
     */
    private int getTopBitNumber(int foundMask) {
        int i = 0;
        while (foundMask != 0) {
            foundMask >>>= 1;
    ++i;
        }
        return i-1;
    }

    private void addCanonicalItems() {
        PatternInfo patternInfo = new PatternInfo();
        // make sure that every valid field occurs once, with a "default" length
        for (int i = 0; i < CANONICAL_ITEMS.length; ++i) {
            addPattern(String.valueOf(CANONICAL_ITEMS[i]), false, patternInfo);
        }
    }

    private PatternWithMatcher getBestRaw(DateTimeMatcher source, int includeMask, DistanceInfo missingFields, DateTimeMatcher skipMatcher) {
        //      if (SHOW_DISTANCE) System.out.println("Searching for: " + source.pattern
        //      + ", mask: " + showMask(includeMask));
        int bestDistance = Integer.MAX_VALUE;
        PatternWithMatcher bestPatternWithMatcher = new PatternWithMatcher("", null);
        DistanceInfo tempInfo = new DistanceInfo();
        for (DateTimeMatcher trial : skeleton2pattern.keySet()) {
            if (trial.equals(skipMatcher)) {
                continue;
            }
            int distance = source.getDistance(trial, includeMask, tempInfo);
            //          if (SHOW_DISTANCE) System.out.println("\tDistance: " + trial.pattern + ":\t"
            //          + distance + ",\tmissing fields: " + tempInfo);
            if (distance < bestDistance) {
                bestDistance = distance;
                PatternWithSkeletonFlag patternWithSkelFlag = skeleton2pattern.get(trial);
                bestPatternWithMatcher.pattern = patternWithSkelFlag.pattern;
                // If the best raw match had a specified skeleton then return it too.
                // This can be passed through to adjustFieldTypes to help it do a better job.
                if (patternWithSkelFlag.skeletonWasSpecified) {
                    bestPatternWithMatcher.matcherWithSkeleton = trial;
                } else {
                    bestPatternWithMatcher.matcherWithSkeleton = null;
                }
                missingFields.setTo(tempInfo);
                if (distance == 0) {
                    break;
                }
            }
        }
        return bestPatternWithMatcher;
    }

    /*
     * @param fixFractionalSeconds TODO
     */
    // flags values
    private enum DTPGflags {
        FIX_FRACTIONAL_SECONDS,
        SKELETON_USES_CAP_J,
        SKELETON_USES_b,
        SKELETON_USES_B,
        ;

        public static DTPGflags getFlag(String preferred) {
            char last = preferred.charAt(preferred.length()-1);
            switch (last) {
            case 'b' : return SKELETON_USES_b;
            case 'B' : return SKELETON_USES_B;
            default: return null;
            }
        }
    };

    private String adjustFieldTypes(PatternWithMatcher patternWithMatcher, DateTimeMatcher inputRequest, EnumSet<DTPGflags> flags, int options) {
        fp.set(patternWithMatcher.pattern);
        StringBuilder newPattern = new StringBuilder();
        for (Object item : fp.getItems()) {
            if (item instanceof String) {
                newPattern.append(fp.quoteLiteral((String)item));
            } else {
                final VariableField variableField = (VariableField) item;

                StringBuilder fieldBuilder = new StringBuilder(variableField.toString());
                //                int canonicalIndex = getCanonicalIndex(field, true);
                //                if (canonicalIndex < 0) {
                //                    continue; // don't adjust
                //                }
                //                int type = types[canonicalIndex][1];
                int type = variableField.getType();

                // handle special day periods
                if (type == DAYPERIOD
                        && !flags.isEmpty()) {
                    char c = flags.contains(DTPGflags.SKELETON_USES_b) ? 'b' : flags.contains(DTPGflags.SKELETON_USES_B) ? 'B' : '0';
                    if (c != '0') {
                        int len = fieldBuilder.length();
                        fieldBuilder.setLength(0);
                        for (int i = len; i > 0; --i) {
                            fieldBuilder.append(c);
                        }
                    }
                }

                if (flags.contains(DTPGflags.FIX_FRACTIONAL_SECONDS) && type == SECOND) {
                    fieldBuilder.append(decimal);
                    inputRequest.original.appendFieldTo(FRACTIONAL_SECOND, fieldBuilder);
                } else if (inputRequest.type[type] != 0) {
                    // Here:
                    // - "reqField" is the field from the originally requested skeleton, with length
                    // "reqFieldLen".
                    // - "field" is the field from the found pattern.
                    //
                    // The adjusted field should consist of characters from the originally requested
                    // skeleton, except in the case of HOUR or MONTH or WEEKDAY or YEAR, in which case it
                    // should consist of characters from the found pattern.
                    //
                    // The length of the adjusted field (adjFieldLen) should match that in the originally
                    // requested skeleton, except that in the following cases the length of the adjusted field
                    // should match that in the found pattern (i.e. the length of this pattern field should
                    // not be adjusted):
                    // 1. type is HOUR and the corresponding bit in options is not set (ticket #7180).
                    //    Note, we may want to implement a similar change for other numeric fields (MM, dd,
                    //    etc.) so the default behavior is to get locale preference for field length, but
                    //    options bits can be used to override this.
                    // 2. There is a specified skeleton for the found pattern and one of the following is true:
                    //    a) The length of the field in the skeleton (skelFieldLen) is equal to reqFieldLen.
                    //    b) The pattern field is numeric and the skeleton field is not, or vice versa.
                    //
                    // Old behavior was:
                    // normally we just replace the field. However HOUR is special; we only change the length

                    char reqFieldChar = inputRequest.original.getFieldChar(type);
                    int reqFieldLen = inputRequest.original.getFieldLength(type);
                    if ( reqFieldChar == 'E' && reqFieldLen < 3 ) {
                        reqFieldLen = 3; // 1-3 for E are equivalent to 3 for c,e
                    }
                    int adjFieldLen = reqFieldLen;
                    DateTimeMatcher matcherWithSkeleton = patternWithMatcher.matcherWithSkeleton;
                    if ( (type == HOUR && (options & MATCH_HOUR_FIELD_LENGTH)==0) ||
                            (type == MINUTE && (options & MATCH_MINUTE_FIELD_LENGTH)==0) ||
                            (type == SECOND && (options & MATCH_SECOND_FIELD_LENGTH)==0) ) {
                        adjFieldLen = fieldBuilder.length();
                    } else if (matcherWithSkeleton != null) {
                        int skelFieldLen = matcherWithSkeleton.original.getFieldLength(type);
                        boolean patFieldIsNumeric = variableField.isNumeric();
                        boolean skelFieldIsNumeric = matcherWithSkeleton.fieldIsNumeric(type);
                        if (skelFieldLen == reqFieldLen
                                || (patFieldIsNumeric && !skelFieldIsNumeric)
                                || (skelFieldIsNumeric && !patFieldIsNumeric)) {
                            // don't adjust the field length in the found pattern
                            adjFieldLen = fieldBuilder.length();
                        }
                    }
                    char c = (type != HOUR
                            && type != MONTH
                            && type != WEEKDAY
                            && (type != YEAR || reqFieldChar=='Y'))
                            ? reqFieldChar
                            : fieldBuilder.charAt(0);
                    if (type == HOUR && flags.contains(DTPGflags.SKELETON_USES_CAP_J)) {
                        c = defaultHourFormatChar;
                    }
                    fieldBuilder = new StringBuilder();
                    for (int i = adjFieldLen; i > 0; --i) fieldBuilder.append(c);
                }
                newPattern.append(fieldBuilder);
            }
        }
        //if (SHOW_DISTANCE) System.out.println("\tRaw: " + pattern);
        return newPattern.toString();
    }

    //  public static String repeat(String s, int count) {
    //  StringBuffer result = new StringBuffer();
    //  for (int i = 0; i < count; ++i) {
    //  result.append(s);
    //  }
    //  return result.toString();
    //  }

    /**
     * internal routine
     * @param pattern The pattern that is passed.
     * @return field value
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public String getFields(String pattern) {
        fp.set(pattern);
        StringBuilder newPattern = new StringBuilder();
        for (Object item : fp.getItems()) {
            if (item instanceof String) {
                newPattern.append(fp.quoteLiteral((String)item));
            } else {
                newPattern.append("{" + getName(item.toString()) + "}");
            }
        }
        return newPattern.toString();
    }

    private static String showMask(int mask) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < TYPE_LIMIT; ++i) {
            if ((mask & (1<<i)) == 0)
                continue;
            if (result.length() != 0)
                result.append(" | ");
            result.append(FIELD_NAME[i]);
            result.append(" ");
        }
        return result.toString();
    }

    private static final String[] CLDR_FIELD_APPEND = {
        "Era", "Year", "Quarter", "Month", "Week", "*", "Day-Of-Week",
        "Day", "*", "*", "*",
        "Hour", "Minute", "Second", "*", "Timezone"
    };

    private static final String[] CLDR_FIELD_NAME = {
        "era", "year", "*", "month", "week", "*", "weekday",
        "day", "*", "*", "dayperiod",
        "hour", "minute", "second", "*", "zone"
    };

    private static final String[] FIELD_NAME = {
        "Era", "Year", "Quarter", "Month", "Week_in_Year", "Week_in_Month", "Weekday",
        "Day", "Day_Of_Year", "Day_of_Week_in_Month", "Dayperiod",
        "Hour", "Minute", "Second", "Fractional_Second", "Zone"
    };


    private static final String[] CANONICAL_ITEMS = {
        "G", "y", "Q", "M", "w", "W", "E",
        "d", "D", "F",
        "H", "m", "s", "S", "v"
    };

    private static final Set<String> CANONICAL_SET = new HashSet<String>(Arrays.asList(CANONICAL_ITEMS));
    private Set<String> cldrAvailableFormatKeys = new HashSet<String>(20);

    private static final int
    DATE_MASK = (1<<DAYPERIOD) - 1,
    TIME_MASK = (1<<TYPE_LIMIT) - 1 - DATE_MASK;

    private static final int // numbers are chosen to express 'distance'
    DELTA = 0x10,
    NUMERIC = 0x100,
    NONE = 0,
    NARROW = -0x101,
    SHORT = -0x102,
    LONG = -0x103,
    EXTRA_FIELD =   0x10000,
    MISSING_FIELD = 0x1000;


    private static String getName(String s) {
        int i = getCanonicalIndex(s, true);
        String name = FIELD_NAME[types[i][1]];
        if (types[i][2] < 0) {
            name += ":S"; // string
        }
        else {
            name += ":N";
        }
        return name;
    }

    /**
     * Get the canonical index, or return -1 if illegal.
     * @param s
     * @param strict TODO
     */
    private static int getCanonicalIndex(String s, boolean strict) {
        int len = s.length();
        if (len == 0) {
            return -1;
        }
        int ch = s.charAt(0);
        //      verify that all are the same character
        for (int i = 1; i < len; ++i) {
            if (s.charAt(i) != ch) {
                return -1;
            }
        }
        int bestRow = -1;
        for (int i = 0; i < types.length; ++i) {
            int[] row = types[i];
            if (row[0] != ch) continue;
            bestRow = i;
            if (row[3] > len) continue;
            if (row[row.length-1] < len) continue;
            return i;
        }
        return strict ? -1 : bestRow;
    }

    /**
     * Gets the canonical character associated with the specified field (ERA, YEAR, etc).
     */
    private static char getCanonicalChar(int field, char reference) {
        // Special case: distinguish between 12-hour and 24-hour
        if (reference == 'h' || reference == 'K') {
            return 'h';
        }

        // Linear search over types (return the top entry for each field)
        for (int i = 0; i < types.length; ++i) {
            int[] row = types[i];
            if (row[1] == field) {
                return (char) row[0];
            }
        }
        throw new IllegalArgumentException("Could not find field " + field);
    }

    private static final int[][] types = {
        // the order here makes a difference only when searching for single field.
        // format is:
        // pattern character, main type, weight, min length, weight
        {'G', ERA, SHORT, 1, 3},
        {'G', ERA, LONG, 4},

        {'y', YEAR, NUMERIC, 1, 20},
        {'Y', YEAR, NUMERIC + DELTA, 1, 20},
        {'u', YEAR, NUMERIC + 2*DELTA, 1, 20},
        {'r', YEAR, NUMERIC + 3*DELTA, 1, 20},
        {'U', YEAR, SHORT, 1, 3},
        {'U', YEAR, LONG, 4},
        {'U', YEAR, NARROW, 5},

        {'Q', QUARTER, NUMERIC, 1, 2},
        {'Q', QUARTER, SHORT, 3},
        {'Q', QUARTER, LONG, 4},

        {'q', QUARTER, NUMERIC + DELTA, 1, 2},
        {'q', QUARTER, SHORT + DELTA, 3},
        {'q', QUARTER, LONG + DELTA, 4},

        {'M', MONTH, NUMERIC, 1, 2},
        {'M', MONTH, SHORT, 3},
        {'M', MONTH, LONG, 4},
        {'M', MONTH, NARROW, 5},
        {'L', MONTH, NUMERIC + DELTA, 1, 2},
        {'L', MONTH, SHORT - DELTA, 3},
        {'L', MONTH, LONG - DELTA, 4},
        {'L', MONTH, NARROW - DELTA, 5},

        {'l', MONTH, NUMERIC + DELTA, 1, 1},

        {'w', WEEK_OF_YEAR, NUMERIC, 1, 2},
        {'W', WEEK_OF_MONTH, NUMERIC + DELTA, 1},

        {'E', WEEKDAY, SHORT, 1, 3},
        {'E', WEEKDAY, LONG, 4},
        {'E', WEEKDAY, NARROW, 5},
        {'c', WEEKDAY, NUMERIC + 2*DELTA, 1, 2},
        {'c', WEEKDAY, SHORT - 2*DELTA, 3},
        {'c', WEEKDAY, LONG - 2*DELTA, 4},
        {'c', WEEKDAY, NARROW - 2*DELTA, 5},
        {'e', WEEKDAY, NUMERIC + DELTA, 1, 2}, // 'e' is currently not used in CLDR data, should not be canonical
        {'e', WEEKDAY, SHORT - DELTA, 3},
        {'e', WEEKDAY, LONG - DELTA, 4},
        {'e', WEEKDAY, NARROW - DELTA, 5},

        {'d', DAY, NUMERIC, 1, 2},
        {'D', DAY_OF_YEAR, NUMERIC + DELTA, 1, 3},
        {'F', DAY_OF_WEEK_IN_MONTH, NUMERIC + 2*DELTA, 1},
        {'g', DAY, NUMERIC + 3*DELTA, 1, 20}, // really internal use, so we don't care

        {'a', DAYPERIOD, SHORT, 1},

        {'H', HOUR, NUMERIC + 10*DELTA, 1, 2}, // 24 hour
        {'k', HOUR, NUMERIC + 11*DELTA, 1, 2},
        {'h', HOUR, NUMERIC, 1, 2}, // 12 hour
        {'K', HOUR, NUMERIC + DELTA, 1, 2},

        {'m', MINUTE, NUMERIC, 1, 2},

        {'s', SECOND, NUMERIC, 1, 2},
        {'S', FRACTIONAL_SECOND, NUMERIC + DELTA, 1, 1000},
        {'A', SECOND, NUMERIC + 2*DELTA, 1, 1000},

        {'v', ZONE, SHORT - 2*DELTA, 1},
        {'v', ZONE, LONG - 2*DELTA, 4},
        {'z', ZONE, SHORT, 1, 3},
        {'z', ZONE, LONG, 4},
        {'Z', ZONE, NARROW - DELTA, 1, 3},
        {'Z', ZONE, LONG - DELTA, 4},
        {'Z', ZONE, SHORT - DELTA, 5},
        {'O', ZONE, SHORT - DELTA, 1},
        {'O', ZONE, LONG - DELTA, 4},
        {'V', ZONE, SHORT - DELTA, 1},
        {'V', ZONE, LONG - DELTA, 2},
        {'X', ZONE, NARROW - DELTA, 1},
        {'X', ZONE, SHORT - DELTA, 2},
        {'X', ZONE, LONG - DELTA, 4},
        {'x', ZONE, NARROW - DELTA, 1},
        {'x', ZONE, SHORT - DELTA, 2},
        {'x', ZONE, LONG - DELTA, 4},
    };


    /**
     * A compact storage mechanism for skeleton field strings.  Several dozen of these will be created
     * for a typical DateTimePatternGenerator instance.
     * @author sffc
     */
    private static class SkeletonFields {
        private byte[] chars = new byte[TYPE_LIMIT];
        private byte[] lengths = new byte[TYPE_LIMIT];
        private static final byte DEFAULT_CHAR = '\0';
        private static final byte DEFAULT_LENGTH = 0;

        public void clear() {
            Arrays.fill(chars, DEFAULT_CHAR);
            Arrays.fill(lengths, DEFAULT_LENGTH);
        }

        void copyFieldFrom(SkeletonFields other, int field) {
            chars[field] = other.chars[field];
            lengths[field] = other.lengths[field];
        }

        void clearField(int field) {
            chars[field] = DEFAULT_CHAR;
            lengths[field] = DEFAULT_LENGTH;
        }

        char getFieldChar(int field) {
            return (char) chars[field];
        }

        int getFieldLength(int field) {
            return lengths[field];
        }

        void populate(int field, String value) {
            // Ensure no loss in character data
            for (char ch : value.toCharArray()) {
                assert ch == value.charAt(0);
            }

            populate(field, value.charAt(0), value.length());
        }

        void populate(int field, char ch, int length) {
            assert ch <= Byte.MAX_VALUE;
            assert length <= Byte.MAX_VALUE;

            chars[field] = (byte) ch;
            lengths[field] = (byte) length;
        }

        public boolean isFieldEmpty(int field) {
            return lengths[field] == DEFAULT_LENGTH;
        }

        @Override
        public String toString() {
            return appendTo(new StringBuilder()).toString();
        }

        public String toCanonicalString() {
            return appendTo(new StringBuilder(), true).toString();
        }

        public StringBuilder appendTo(StringBuilder sb) {
            return appendTo(sb, false);
        }

        private StringBuilder appendTo(StringBuilder sb, boolean canonical) {
            for (int i=0; i<TYPE_LIMIT; ++i) {
                appendFieldTo(i, sb, canonical);
            }
            return sb;
        }

        public StringBuilder appendFieldTo(int field, StringBuilder sb) {
            return appendFieldTo(field, sb, false);
        }

        private StringBuilder appendFieldTo(int field, StringBuilder sb, boolean canonical) {
            char ch = (char) chars[field];
            int length = lengths[field];

            if (canonical) {
                ch = getCanonicalChar(field, ch);
            }

            for (int i=0; i<length; i++) {
                sb.append(ch);
            }
            return sb;
        }

        public int compareTo(SkeletonFields other) {
            for (int i = 0; i < TYPE_LIMIT; ++i) {
                int charDiff = chars[i] - other.chars[i];
                if (charDiff != 0) {
                    return charDiff;
                }
                int lengthDiff = lengths[i] - other.lengths[i];
                if (lengthDiff != 0) {
                    return lengthDiff;
                }
            }
            return 0;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other != null && other instanceof SkeletonFields
                && compareTo((SkeletonFields) other) == 0);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(chars) ^ Arrays.hashCode(lengths);
        }
    }


    private static class DateTimeMatcher implements Comparable<DateTimeMatcher> {
        //private String pattern = null;
        private int[] type = new int[TYPE_LIMIT];
        private SkeletonFields original = new SkeletonFields();
        private SkeletonFields baseOriginal = new SkeletonFields();

        // just for testing; fix to make multi-threaded later
        // private static FormatParser fp = new FormatParser();

        public boolean fieldIsNumeric(int field) {
            return type[field] > 0;
        }

        @Override
        public String toString() {
            return original.toString();
        }

        // returns a string like toString but using the canonical character for most types,
        // e.g. M for M or L, E for E or c, y for y or U, etc. The hour field is canonicalized
        // to 'H' (for 24-hour types) or 'h' (for 12-hour types)
        public String toCanonicalString() {
            return original.toCanonicalString();
        }

        String getBasePattern() {
            return baseOriginal.toString();
        }

        DateTimeMatcher set(String pattern, FormatParser fp, boolean allowDuplicateFields) {
            // Reset any data stored in this instance
            Arrays.fill(type, NONE);
            original.clear();
            baseOriginal.clear();

            fp.set(pattern);
            for (Object obj : fp.getItems()) {
                if (!(obj instanceof VariableField)) {
                    continue;
                }
                VariableField item = (VariableField)obj;
                String value = item.toString();
                if (value.charAt(0) == 'a') continue; // skip day period, special case
                int canonicalIndex = item.getCanonicalIndex();
                //                if (canonicalIndex < 0) {
                //                    throw new IllegalArgumentException("Illegal field:\t"
                //                            + field + "\t in " + pattern);
                //                }
                int[] row = types[canonicalIndex];
                int field = row[1];
                if (!original.isFieldEmpty(field)) {
                    char ch1 = original.getFieldChar(field);
                    char ch2 = value.charAt(0);
                    if ( allowDuplicateFields ||
                            (ch1 == 'r' && ch2 == 'U') ||
                            (ch1 == 'U' && ch2 == 'r') ) {
                        continue;
                    }
                    throw new IllegalArgumentException("Conflicting fields:\t"
                            + ch1 + ", " + value + "\t in " + pattern);
                }
                original.populate(field, value);
                char repeatChar = (char)row[0];
                int repeatCount = row[3];
                // #7930 removes hack to cap repeatCount at 3
                if ("GEzvQ".indexOf(repeatChar) >= 0) repeatCount = 1;
                baseOriginal.populate(field, repeatChar, repeatCount);
                int subField = row[2];
                if (subField > 0) subField += value.length();
                type[field] = subField;
            }
            return this;
        }

        int getFieldMask() {
            int result = 0;
            for (int i = 0; i < type.length; ++i) {
                if (type[i] != 0) result |= (1<<i);
            }
            return result;
        }

        @SuppressWarnings("unused")
        void extractFrom(DateTimeMatcher source, int fieldMask) {
            for (int i = 0; i < type.length; ++i) {
                if ((fieldMask & (1<<i)) != 0) {
                    type[i] = source.type[i];
                    original.copyFieldFrom(source.original, i);
                } else {
                    type[i] = NONE;
                    original.clearField(i);
                }
            }
        }

        int getDistance(DateTimeMatcher other, int includeMask, DistanceInfo distanceInfo) {
            int result = 0;
            distanceInfo.clear();
            for (int i = 0; i < TYPE_LIMIT; ++i) {
                int myType = (includeMask & (1<<i)) == 0 ? 0 : type[i];
                int otherType = other.type[i];
                if (myType == otherType) continue; // identical (maybe both zero) add 0
                if (myType == 0) { // and other is not
                    result += EXTRA_FIELD;
                    distanceInfo.addExtra(i);
                } else if (otherType == 0) { // and mine is not
                    result += MISSING_FIELD;
                    distanceInfo.addMissing(i);
                } else {
                    result += Math.abs(myType - otherType); // square of mismatch
                }
            }
            return result;
        }

        @Override
        public int compareTo(DateTimeMatcher that) {
            int result = original.compareTo(that.original);
            return result > 0 ? -1 : result < 0 ? 1 : 0; // Reverse the order.
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other != null && other instanceof DateTimeMatcher
                && original.equals(((DateTimeMatcher) other).original));
        }

        @Override
        public int hashCode() {
            return original.hashCode();
        }
    }

    private static class DistanceInfo {
        int missingFieldMask;
        int extraFieldMask;
        void clear() {
            missingFieldMask = extraFieldMask = 0;
        }
        void setTo(DistanceInfo other) {
            missingFieldMask = other.missingFieldMask;
            extraFieldMask = other.extraFieldMask;
        }
        void addMissing(int field) {
            missingFieldMask |= (1<<field);
        }
        void addExtra(int field) {
            extraFieldMask |= (1<<field);
        }
        @Override
        public String toString() {
            return "missingFieldMask: " + DateTimePatternGenerator.showMask(missingFieldMask)
                    + ", extraFieldMask: " + DateTimePatternGenerator.showMask(extraFieldMask);
        }
    }
}
//eof
