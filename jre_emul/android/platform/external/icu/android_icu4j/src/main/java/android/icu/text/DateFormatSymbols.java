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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import android.icu.impl.CacheBase;
import android.icu.impl.CalendarUtil;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SoftCache;
import android.icu.impl.UResource;
import android.icu.impl.Utility;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.Calendar;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ICUException;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;

/**
 * <strong>[icu enhancement]</strong> ICU's replacement for {@link java.text.DateFormatSymbols}.&nbsp;Methods, fields, and other functionality specific to ICU are labeled '<strong>[icu]</strong>'.
 *
 * <p><code>DateFormatSymbols</code> is a public class for encapsulating
 * localizable date-time formatting data, such as the names of the
 * months, the names of the days of the week, and the time zone data.
 * <code>DateFormat</code> and <code>SimpleDateFormat</code> both use
 * <code>DateFormatSymbols</code> to encapsulate this information.
 *
 * <p>Typically you shouldn't use <code>DateFormatSymbols</code> directly.
 * Rather, you are encouraged to create a date-time formatter with the
 * <code>DateFormat</code> class's factory methods: <code>getTimeInstance</code>,
 * <code>getDateInstance</code>, or <code>getDateTimeInstance</code>.
 * These methods automatically create a <code>DateFormatSymbols</code> for
 * the formatter so that you don't have to. After the
 * formatter is created, you may modify its format pattern using the
 * <code>setPattern</code> method. For more information about
 * creating formatters using <code>DateFormat</code>'s factory methods,
 * see {@link DateFormat}.
 *
 * <p>If you decide to create a date-time formatter with a specific
 * format pattern for a specific locale, you can do so with:
 * <blockquote>
 * <pre>
 * new SimpleDateFormat(aPattern, new DateFormatSymbols(aLocale)).
 * </pre>
 * </blockquote>
 *
 * <p><code>DateFormatSymbols</code> objects are clonable. When you obtain
 * a <code>DateFormatSymbols</code> object, feel free to modify the
 * date-time formatting data. For instance, you can replace the localized
 * date-time format pattern characters with the ones that you feel easy
 * to remember. Or you can change the representative cities
 * to your favorite ones.
 *
 * <p>New <code>DateFormatSymbols</code> subclasses may be added to support
 * <code>SimpleDateFormat</code> for date-time formatting for additional locales.
 *
 * @see          DateFormat
 * @see          SimpleDateFormat
 * @see          android.icu.util.SimpleTimeZone
 * @author       Chen-Lieh Huang
 */
public class DateFormatSymbols implements Serializable, Cloneable {

    // TODO make sure local pattern char string is 18 characters long,
    // that is, that it encompasses the new 'u' char for
    // EXTENDED_YEAR.  Two options: 1. Make sure resource data is
    // correct; 2. Make code add in 'u' at end if len == 17.

    // Constants for context
    /**
     * <strong>[icu]</strong> Constant for context.
     */
    public static final int FORMAT = 0;

    /**
     * <strong>[icu]</strong> Constant for context.
     */
    public static final int STANDALONE = 1;

    /**
     * <strong>[icu]</strong> Constant for context. NUMERIC context
     * is only supported for leapMonthPatterns.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final int NUMERIC = 2;

    /**
     * <strong>[icu]</strong> Constant for context.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final int DT_CONTEXT_COUNT = 3;

    // Constants for width

    /**
     * <strong>[icu]</strong> Constant for width.
     */
    public static final int ABBREVIATED = 0;

    /**
     * <strong>[icu]</strong> Constant for width.
     */
    public static final int WIDE = 1;

    /**
     * <strong>[icu]</strong> Constant for width.
     */
    public static final int NARROW = 2;

    /**
     * <strong>[icu]</strong> Constant for width; only supported for weekdays.
     */
    public static final int SHORT = 3;

    /**
     * <strong>[icu]</strong> Constant for width.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final int DT_WIDTH_COUNT = 4;

    /**
     * <strong>[icu]</strong> Somewhat temporary constant for leap month pattern type, adequate for Chinese calendar.
     * @hide draft / provisional / internal are hidden on Android
     */
    static final int DT_LEAP_MONTH_PATTERN_FORMAT_WIDE = 0;

    /**
     * <strong>[icu]</strong> Somewhat temporary constant for leap month pattern type, adequate for Chinese calendar.
     * @hide draft / provisional / internal are hidden on Android
     */
    static final int DT_LEAP_MONTH_PATTERN_FORMAT_ABBREV = 1;

    /**
     * <strong>[icu]</strong> Somewhat temporary constant for leap month pattern type, adequate for Chinese calendar.
     * @hide draft / provisional / internal are hidden on Android
     */
    static final int DT_LEAP_MONTH_PATTERN_FORMAT_NARROW = 2;

    /**
     * <strong>[icu]</strong> Somewhat temporary constant for leap month pattern type, adequate for Chinese calendar.
     * @hide draft / provisional / internal are hidden on Android
     */
    static final int DT_LEAP_MONTH_PATTERN_STANDALONE_WIDE = 3;

    /**
     * <strong>[icu]</strong> Somewhat temporary constant for leap month pattern type, adequate for Chinese calendar.
     * @hide draft / provisional / internal are hidden on Android
     */
    static final int DT_LEAP_MONTH_PATTERN_STANDALONE_ABBREV = 4;

    /**
     * <strong>[icu]</strong> Somewhat temporary constant for leap month pattern type, adequate for Chinese calendar.
     * @hide draft / provisional / internal are hidden on Android
     */
    static final int DT_LEAP_MONTH_PATTERN_STANDALONE_NARROW = 5;

    /**
     * <strong>[icu]</strong> Somewhat temporary constant for leap month pattern type, adequate for Chinese calendar.
     * @hide draft / provisional / internal are hidden on Android
     */
    static final int DT_LEAP_MONTH_PATTERN_NUMERIC = 6;

    /**
     * <strong>[icu]</strong> Somewhat temporary constant for month pattern count, adequate for Chinese calendar.
     * @hide draft / provisional / internal are hidden on Android
     */
    static final int DT_MONTH_PATTERN_COUNT = 7;

    /**
     * <strong>[icu]</strong> This default time separator is used for formatting when the locale
     * doesn't specify any time separator, and always recognized when parsing.
     * @hide draft / provisional / internal are hidden on Android
     */
    static final String DEFAULT_TIME_SEPARATOR = ":";

    /**
     * <strong>[icu]</strong> This alternate time separator is always recognized when parsing.
     * @hide draft / provisional / internal are hidden on Android
     */
    static final String ALTERNATE_TIME_SEPARATOR = ".";

   /**
     * Constructs a DateFormatSymbols object by loading format data from
     * resources for the default <code>FORMAT</code> locale.
     *
     * @throws java.util.MissingResourceException if the resources for the default locale
     *          cannot be found or cannot be loaded.
     * @see Category#FORMAT
     */
    public DateFormatSymbols()
    {
        this(ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs a DateFormatSymbols object by loading format data from
     * resources for the given locale.
     *
     * @throws java.util.MissingResourceException if the resources for the specified
     *          locale cannot be found or cannot be loaded.
     */
    public DateFormatSymbols(Locale locale)
    {
        this(ULocale.forLocale(locale));
    }

    /**
     * <strong>[icu]</strong> Constructs a DateFormatSymbols object by loading format data from
     * resources for the given ulocale.
     *
     * @throws java.util.MissingResourceException if the resources for the specified
     *          locale cannot be found or cannot be loaded.
     */
    public DateFormatSymbols(ULocale locale)
    {
        initializeData(locale, CalendarUtil.getCalendarType(locale));
    }

    /**
     * Returns a DateFormatSymbols instance for the default locale.
     *
     * <strong>[icu] Note:</strong> Unlike <code>java.text.DateFormatSymbols#getInstance</code>,
     * this method simply returns <code>new android.icu.text.DateFormatSymbols()</code>.
     * ICU does not support <code>DateFormatSymbolsProvider</code> introduced in Java 6
     * or its equivalent implementation for now.
     *
     * @return A DateFormatSymbols instance.
     */
    public static DateFormatSymbols getInstance() {
        return new DateFormatSymbols();
    }

    /**
     * Returns a DateFormatSymbols instance for the given locale.
     *
     * <strong>[icu] Note:</strong> Unlike <code>java.text.DateFormatSymbols#getInstance</code>,
     * this method simply returns <code>new android.icu.text.DateFormatSymbols(locale)</code>.
     * ICU does not support <code>DateFormatSymbolsProvider</code> introduced in Java 6
     * or its equivalent implementation for now.
     *
     * @param locale the locale.
     * @return A DateFormatSymbols instance.
     */
    public static DateFormatSymbols getInstance(Locale locale) {
        return new DateFormatSymbols(locale);
    }

    /**
     * <strong>[icu]</strong> Returns a DateFormatSymbols instance for the given locale.
     *
     * <strong>[icu] Note:</strong> Unlike <code>java.text.DateFormatSymbols#getInstance</code>,
     * this method simply returns <code>new android.icu.text.DateFormatSymbols(locale)</code>.
     * ICU does not support <code>DateFormatSymbolsProvider</code> introduced in Java 6
     * or its equivalent implementation for now.
     *
     * @param locale the locale.
     * @return A DateFormatSymbols instance.
     */
    public static DateFormatSymbols getInstance(ULocale locale) {
        return new DateFormatSymbols(locale);
    }

    /**
     * Returns an array of all locales for which the <code>getInstance</code> methods of
     * this class can return localized instances.
     *
     * <strong>[icu] Note:</strong> Unlike <code>java.text.DateFormatSymbols#getAvailableLocales</code>,
     * this method simply returns the array of <code>Locale</code>s available in this
     * class.  ICU does not support <code>DateFormatSymbolsProvider</code> introduced in
     * Java 6 or its equivalent implementation for now.
     *
     * @return An array of <code>Locale</code>s for which localized
     * <code>DateFormatSymbols</code> instances are available.
     */
    public static Locale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableLocales();
    }

    /**
     * <strong>[icu]</strong> Returns an array of all locales for which the <code>getInstance</code>
     * methods of this class can return localized instances.
     *
     * <strong>[icu] Note:</strong> Unlike <code>java.text.DateFormatSymbols#getAvailableLocales</code>,
     * this method simply returns the array of <code>ULocale</code>s available in this
     * class.  ICU does not support <code>DateFormatSymbolsProvider</code> introduced in
     * Java 6 or its equivalent implementation for now.
     *
     * @return An array of <code>ULocale</code>s for which localized
     * <code>DateFormatSymbols</code> instances are available.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static ULocale[] getAvailableULocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    /**
     * Era strings. For example: "AD" and "BC".  An array of 2 strings,
     * indexed by <code>Calendar.BC</code> and <code>Calendar.AD</code>.
     * @serial
     */
    String eras[] = null;

    /**
     * Era name strings. For example: "Anno Domini" and "Before Christ".  An array of 2 strings,
     * indexed by <code>Calendar.BC</code> and <code>Calendar.AD</code>.
     * @serial
     */
    String eraNames[] = null;

    /**
     * Narrow era names. For example: "A" and "B". An array of 2 strings,
     * indexed by <code>Calendar.BC</code> and <code>Calendar.AD</code>.
     * @serial
     */
    String narrowEras[] = null;

    /**
     * Month strings. For example: "January", "February", etc.  An array
     * of 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.
     * @serial
     */
    String months[] = null;

    /**
     * Short month strings. For example: "Jan", "Feb", etc.  An array of
     * 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.

     * @serial
     */
    String shortMonths[] = null;

    /**
     * Narrow month strings. For example: "J", "F", etc.  An array of
     * 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.

     * @serial
     */
    String narrowMonths[] = null;

    /**
     * Standalone month strings. For example: "January", "February", etc.  An array
     * of 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.
     * @serial
     */
    String standaloneMonths[] = null;

    /**
     * Standalone short month strings. For example: "Jan", "Feb", etc.  An array of
     * 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.

     * @serial
     */
    String standaloneShortMonths[] = null;

    /**
     * Standalone narrow month strings. For example: "J", "F", etc.  An array of
     * 13 strings (some calendars have 13 months), indexed by
     * <code>Calendar.JANUARY</code>, <code>Calendar.FEBRUARY</code>, etc.

     * @serial
     */
    String standaloneNarrowMonths[] = null;

    /**
     * Format wide weekday strings, for example: "Sunday", "Monday", etc.
     * An array of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>weekdays[0]</code> is ignored.
     * @serial
     */
    String weekdays[] = null;

    /**
     * CLDR-style format abbreviated (not short) weekday strings,
     * for example: "Sun", "Mon", etc.
     * An array of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>shortWeekdays[0]</code> is ignored.
     * @serial
     */
    String shortWeekdays[] = null;

    /**
     * CLDR-style format short weekday strings, for example: "Su", "Mo", etc.
     * An array of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>shorterWeekdays[0]</code> is ignored.
     * @serial
     */
   // Note, serialization restore from pre-ICU-51 will leave this null.
    String shorterWeekdays[] = null;

    /**
     * CLDR-style format narrow weekday strings, for example: "S", "M", etc.
     * An array of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>narrowWeekdays[0]</code> is ignored.
     * @serial
     */
    String narrowWeekdays[] = null;

    /**
     * Standalone wide weekday strings. For example: "Sunday", "Monday", etc.
     * An array of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>standaloneWeekdays[0]</code> is ignored.
     * @serial
     */
    String standaloneWeekdays[] = null;

    /**
     * CLDR-style standalone abbreviated (not short) weekday strings,
     * for example: "Sun", "Mon", etc.
     * An array of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>standaloneShortWeekdays[0]</code> is ignored.
     * @serial
     */
    String standaloneShortWeekdays[] = null;

    /**
     * CLDR-style standalone short weekday strings, for example: "Sun", "Mon", etc.
     * An array of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>standaloneShorterWeekdays[0]</code> is ignored.
     * @serial
     */
    // Note, serialization restore from pre-ICU-51 will leave this null.
    String standaloneShorterWeekdays[] = null;

    /**
     * Standalone narrow weekday strings. For example: "S", "M", etc.  An array
     * of 8 strings, indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     * The element <code>standaloneNarrowWeekdays[0]</code> is ignored.
     * @serial
     */
    String standaloneNarrowWeekdays[] = null;

    /**
     * AM and PM strings. For example: "AM" and "PM".  An array of
     * 2 strings, indexed by <code>Calendar.AM</code> and
     * <code>Calendar.PM</code>.
     * @serial
     */
    String ampms[] = null;

    /**
     * narrow AM and PM strings. For example: "a" and "p".  An array of
     * 2 strings, indexed by <code>Calendar.AM</code> and
     * <code>Calendar.PM</code>.
     * @serial
     */
    String ampmsNarrow[] = null;

    /**
     * Time separator string. For example: ":".
     * @serial
     */
    private String timeSeparator = null;

    /**
     * Abbreviated quarter names. For example: "Q1", "Q2", "Q3", "Q4". An array
     * of 4 strings indexed by the month divided by 3.
     * @serial
     */
    String shortQuarters[] = null;

    /**
     * Full quarter names. For example: "1st Quarter", "2nd Quarter", "3rd Quarter",
     * "4th Quarter". An array of 4 strings, indexed by the month divided by 3.
     * @serial
     */
    String quarters[] = null;

    /**
     * Standalone abbreviated quarter names. For example: "Q1", "Q2", "Q3", "Q4". An array
     * of 4 strings indexed by the month divided by 3.
     * @serial
     */
    String standaloneShortQuarters[] = null;

    /**
     * Standalone full quarter names. For example: "1st Quarter", "2nd Quarter", "3rd Quarter",
     * "4th Quarter". An array of 4 strings, indexed by the month divided by 3.
     * @serial
     */
    String standaloneQuarters[] = null;

    /**
     * All leap month patterns, for example "{0}bis".
     * An array of DT_MONTH_PATTERN_COUNT strings, indexed by the DT_LEAP_MONTH_PATTERN_XXX value.
     * @serial
     */
    String leapMonthPatterns[] = null;

     /**
     * Cyclic year names, for example: "jia-zi", "yi-chou", ... "gui-hai".
     * An array of (normally) 60 strings, corresponding to cyclic years 1-60 (in Calendar YEAR field).
     * Currently we only have data for format/abbreviated.
     * For the others, just get from format/abbreviated, ignore set.
     * @serial
     */
    String shortYearNames[] = null;

     /**
     * Cyclic zodiac names, for example: "Rat", "Ox", "Tiger", etc.
     * An array of (normally) 12 strings.
     * Currently we only have data for format/abbreviated.
     * For the others, just get from format/abbreviated, ignore set.
     * @serial
     */
    String shortZodiacNames[] = null;

   /**
     * Localized names of time zones in this locale.  This is a
     * two-dimensional array of strings of size <em>n</em> by <em>m</em>,
     * where <em>m</em> is at least 5 and up to 7.  Each of the <em>n</em> rows is an
     * entry containing the localized names for a single <code>TimeZone</code>.
     * Each such row contains (with <code>i</code> ranging from
     * 0..<em>n</em>-1):
     * <ul>
     * <li><code>zoneStrings[i][0]</code> - time zone ID</li>
     * <li><code>zoneStrings[i][1]</code> - long name of zone in standard
     * time</li>
     * <li><code>zoneStrings[i][2]</code> - short name of zone in
     * standard time</li>
     * <li><code>zoneStrings[i][3]</code> - long name of zone in daylight
     * savings time</li>
     * <li><code>zoneStrings[i][4]</code> - short name of zone in daylight
     * savings time</li>
     * <li><code>zoneStrings[i][5]</code> - location name of zone</li>
     * <li><code>zoneStrings[i][6]</code> - long generic name of zone</li>
     * <li><code>zoneStrings[i][7]</code> - short generic of zone</li>
    *  </ul>
     * The zone ID is <em>not</em> localized; it corresponds to the ID
     * value associated with a system time zone object.  All other entries
     * are localized names.  If a zone does not implement daylight savings
     * time, the daylight savings time names are ignored.
     * <em>Note:</em>CLDR 1.5 introduced metazone and its historical mappings.
     * This simple two-dimensional array is no longer sufficient to represent
     * localized names and its historic changes.  Since ICU 3.8.1, localized
     * zone names extracted from ICU locale data is stored in a ZoneStringFormat
     * instance.  But we still need to support the old way of customizing
     * localized zone names, so we keep this field for the purpose.
     * @see android.icu.util.TimeZone
     * @serial
     */
    private String zoneStrings[][] = null;

     /**
     * Unlocalized date-time pattern characters. For example: 'y', 'd', etc.
     * All locales use the same unlocalized pattern characters.
     */
    static final String patternChars = "GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxrbB";

    /**
     * Localized date-time pattern characters. For example, a locale may
     * wish to use 'u' rather than 'y' to represent years in its date format
     * pattern strings.
     * This string must be exactly 18 characters long, with the index of
     * the characters described by <code>DateFormat.ERA_FIELD</code>,
     * <code>DateFormat.YEAR_FIELD</code>, etc.  Thus, if the string were
     * "Xz...", then localized patterns would use 'X' for era and 'z' for year.
     * @serial
     */
    String localPatternChars = null;

    /**
     * Localized names for abbreviated (== short) day periods.
     * An array of strings, in the order of DayPeriod constants.
     */
    String abbreviatedDayPeriods[] = null;

    /**
     * Localized names for wide day periods.
     * An array of strings, in the order of DayPeriod constants.
     */
    String wideDayPeriods[] = null;

    /**
     * Localized names for narrow day periods.
     * An array of strings, in the order of DayPeriod constants.
     */
    String narrowDayPeriods[] = null;

    /**
     * Localized names for standalone abbreviated (== short) day periods.
     * An array of strings, in the order of DayPeriod constants.
     */
    String standaloneAbbreviatedDayPeriods[] = null;

    /**
     * Localized names for standalone wide day periods.
     * An array of strings, in the order of DayPeriod constants.
     */
    String standaloneWideDayPeriods[] = null;

    /**
     * Localized names for standalone narrow day periods.
     * An array of strings, in the order of DayPeriod constants.
     */
    String standaloneNarrowDayPeriods[] = null;

    /* use serialVersionUID from JDK 1.1.4 for interoperability */
    private static final long serialVersionUID = -5987973545549424702L;

    private static final String[][] CALENDAR_CLASSES = {
        {"GregorianCalendar", "gregorian"},
        {"JapaneseCalendar", "japanese"},
        {"BuddhistCalendar", "buddhist"},
        {"TaiwanCalendar", "roc"},
        {"PersianCalendar", "persian"},
        {"IslamicCalendar", "islamic"},
        {"HebrewCalendar", "hebrew"},
        {"ChineseCalendar", "chinese"},
        {"IndianCalendar", "indian"},
        {"CopticCalendar", "coptic"},
        {"EthiopicCalendar", "ethiopic"},
    };

    /**
     * <strong>[icu]</strong> Constants for capitalization context usage types
     * related to date formatting.
     * @hide draft / provisional / internal are hidden on Android
     */
    enum CapitalizationContextUsage {
        OTHER,
        MONTH_FORMAT,     /* except narrow */
        MONTH_STANDALONE, /* except narrow */
        MONTH_NARROW,
        DAY_FORMAT,     /* except narrow */
        DAY_STANDALONE, /* except narrow */
        DAY_NARROW,
        ERA_WIDE,
        ERA_ABBREV,
        ERA_NARROW,
        ZONE_LONG,
        ZONE_SHORT,
        METAZONE_LONG,
        METAZONE_SHORT
    }

    /** Map from resource key to CapitalizationContextUsage value
     */
    private static final Map<String, CapitalizationContextUsage> contextUsageTypeMap;
    static {
        contextUsageTypeMap=new HashMap<String, CapitalizationContextUsage>();
        contextUsageTypeMap.put("month-format-except-narrow", CapitalizationContextUsage.MONTH_FORMAT);
        contextUsageTypeMap.put("month-standalone-except-narrow", CapitalizationContextUsage.MONTH_STANDALONE);
        contextUsageTypeMap.put("month-narrow",   CapitalizationContextUsage.MONTH_NARROW);
        contextUsageTypeMap.put("day-format-except-narrow", CapitalizationContextUsage.DAY_FORMAT);
        contextUsageTypeMap.put("day-standalone-except-narrow", CapitalizationContextUsage.DAY_STANDALONE);
        contextUsageTypeMap.put("day-narrow",     CapitalizationContextUsage.DAY_NARROW);
        contextUsageTypeMap.put("era-name",       CapitalizationContextUsage.ERA_WIDE);
        contextUsageTypeMap.put("era-abbr",       CapitalizationContextUsage.ERA_ABBREV);
        contextUsageTypeMap.put("era-narrow",     CapitalizationContextUsage.ERA_NARROW);
        contextUsageTypeMap.put("zone-long",      CapitalizationContextUsage.ZONE_LONG);
        contextUsageTypeMap.put("zone-short",     CapitalizationContextUsage.ZONE_SHORT);
        contextUsageTypeMap.put("metazone-long",  CapitalizationContextUsage.METAZONE_LONG);
        contextUsageTypeMap.put("metazone-short", CapitalizationContextUsage.METAZONE_SHORT);
    }

     /**
     * Capitalization transforms. For each usage type, the first array element indicates
     * whether to titlecase for uiListOrMenu context, the second indicates whether to
     * titlecase for stand-alone context.
     * @serial
     */
    Map<CapitalizationContextUsage,boolean[]> capitalization = null;

    /**
     * Returns era strings. For example: "AD" and "BC".
     * @return the era strings.
     */
    public String[] getEras() {
        return duplicate(eras);
    }

    /**
     * Sets era strings. For example: "AD" and "BC".
     * @param newEras the new era strings.
     */
    public void setEras(String[] newEras) {
        eras = duplicate(newEras);
    }

    /**
     * <strong>[icu]</strong> Returns era name strings. For example: "Anno Domini" and "Before Christ".
     * @return the era strings.
     */
    public String[] getEraNames() {
        return duplicate(eraNames);
    }

    /**
     * <strong>[icu]</strong> Sets era name strings. For example: "Anno Domini" and "Before Christ".
     * @param newEraNames the new era strings.
     */
    public void setEraNames(String[] newEraNames) {
        eraNames = duplicate(newEraNames);
    }

    // Android patch (http://b/30464240) start: Add getter for narrow eras.
    /**
     * <strong>[icu]</strong> Returns narrow era name strings. For example: "A" and "B".
     * @return the era strings.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public String[] getNarrowEras() {
        return duplicate(narrowEras);
    }
    // Android patch end.

    /**
     * Returns month strings. For example: "January", "February", etc.
     * @return the month strings.
     */
    public String[] getMonths() {
        return duplicate(months);
    }

    /**
     * Returns month strings. For example: "January", "February", etc.
     * @param context    The month context, FORMAT or STANDALONE.
     * @param width      The width or the returned month string,
     *                   either WIDE, ABBREVIATED, or NARROW.
     * @return the month strings.
     */
    public String[] getMonths(int context, int width) {
        String [] returnValue = null;
        switch (context) {
           case FORMAT :
              switch(width) {
                 case WIDE :
                    returnValue = months;
                    break;
                 case ABBREVIATED :
                 case SHORT : // no month data for this, defaults to ABBREVIATED
                    returnValue = shortMonths;
                    break;
                 case NARROW :
                    returnValue = narrowMonths;
                    break;
              }
              break;
           case STANDALONE :
              switch(width) {
                 case WIDE :
                    returnValue = standaloneMonths;
                    break;
                 case ABBREVIATED :
                 case SHORT : // no month data for this, defaults to ABBREVIATED
                    returnValue = standaloneShortMonths;
                    break;
                 case NARROW :
                    returnValue = standaloneNarrowMonths;
                    break;
              }
              break;
        }
        if (returnValue == null) {
            throw new IllegalArgumentException("Bad context or width argument");
        }
        return duplicate(returnValue);
    }

    /**
     * Sets month strings. For example: "January", "February", etc.
     * @param newMonths the new month strings.
     */
    public void setMonths(String[] newMonths) {
        months = duplicate(newMonths);
    }

    /**
     * Sets month strings. For example: "January", "February", etc.
     * @param newMonths the new month strings.
     * @param context    The formatting context, FORMAT or STANDALONE.
     * @param width      The width of the month string,
     *                   either WIDE, ABBREVIATED, or NARROW.
     */
    public void setMonths(String[] newMonths, int context, int width) {
        switch (context) {
           case FORMAT :
              switch(width) {
                 case WIDE :
                    months = duplicate(newMonths);
                    break;
                 case ABBREVIATED :
                    shortMonths = duplicate(newMonths);
                    break;
                 case NARROW :
                    narrowMonths = duplicate(newMonths);
                    break;
                 default : // HANDLE SHORT, etc.
                    break;
              }
              break;
           case STANDALONE :
              switch(width) {
                 case WIDE :
                    standaloneMonths = duplicate(newMonths);
                    break;
                 case ABBREVIATED :
                    standaloneShortMonths = duplicate(newMonths);
                    break;
                 case NARROW :
                    standaloneNarrowMonths = duplicate(newMonths);
                    break;
                 default : // HANDLE SHORT, etc.
                    break;
              }
              break;
        }
    }

    /**
     * Returns short month strings. For example: "Jan", "Feb", etc.
     * @return the short month strings.
     */
    public String[] getShortMonths() {
        return duplicate(shortMonths);
    }

    /**
     * Sets short month strings. For example: "Jan", "Feb", etc.
     * @param newShortMonths the new short month strings.
     */
    public void setShortMonths(String[] newShortMonths) {
        shortMonths = duplicate(newShortMonths);
    }

    /**
     * Returns wide weekday strings. For example: "Sunday", "Monday", etc.
     * @return the weekday strings. Use <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc. to index the result array.
     */
    public String[] getWeekdays() {
        return duplicate(weekdays);
    }

    /**
     * Returns weekday strings. For example: "Sunday", "Monday", etc.
     * @return the weekday strings. Use <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc. to index the result array.
     * @param context    Formatting context, either FORMAT or STANDALONE.
     * @param width      Width of strings to be returned, either
     *                   WIDE, ABBREVIATED, SHORT, or NARROW
     */
    public String[] getWeekdays(int context, int width) {
        String [] returnValue = null;
        switch (context) {
           case FORMAT :
              switch(width) {
                 case WIDE :
                    returnValue = weekdays;
                    break;
                 case ABBREVIATED :
                    returnValue = shortWeekdays;
                    break;
                 case SHORT :
                    returnValue = (shorterWeekdays != null)? shorterWeekdays: shortWeekdays;
                    break;
                 case NARROW :
                    returnValue = narrowWeekdays;
                    break;
              }
              break;
           case STANDALONE :
              switch(width) {
                 case WIDE :
                    returnValue = standaloneWeekdays;
                    break;
                 case ABBREVIATED :
                    returnValue = standaloneShortWeekdays;
                    break;
                 case SHORT :
                    returnValue = (standaloneShorterWeekdays != null)? standaloneShorterWeekdays: standaloneShortWeekdays;
                    break;
                 case NARROW :
                    returnValue = standaloneNarrowWeekdays;
                    break;
              }
              break;
        }
        if (returnValue == null) {
            throw new IllegalArgumentException("Bad context or width argument");
        }
        return duplicate(returnValue);
    }

    /**
     * Sets weekday strings. For example: "Sunday", "Monday", etc.
     * @param newWeekdays The new weekday strings.
     * @param context     The formatting context, FORMAT or STANDALONE.
     * @param width       The width of the strings,
     *                    either WIDE, ABBREVIATED, SHORT, or NARROW.
     */
    public void setWeekdays(String[] newWeekdays, int context, int width) {
        switch (context) {
           case FORMAT :
              switch(width) {
                 case WIDE :
                    weekdays = duplicate(newWeekdays);
                    break;
                 case ABBREVIATED :
                    shortWeekdays = duplicate(newWeekdays);
                    break;
                 case SHORT :
                    shorterWeekdays = duplicate(newWeekdays);
                    break;
                 case NARROW :
                    narrowWeekdays = duplicate(newWeekdays);
                    break;
              }
              break;
           case STANDALONE :
              switch(width) {
                 case WIDE :
                    standaloneWeekdays = duplicate(newWeekdays);
                    break;
                 case ABBREVIATED :
                    standaloneShortWeekdays = duplicate(newWeekdays);
                    break;
                 case SHORT :
                    standaloneShorterWeekdays = duplicate(newWeekdays);
                    break;
                 case NARROW :
                    standaloneNarrowWeekdays = duplicate(newWeekdays);
                    break;
              }
              break;
        }
    }

    /**
     * Sets wide weekday strings. For example: "Sunday", "Monday", etc.
     * @param newWeekdays the new weekday strings. The array should
     * be indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     */
    public void setWeekdays(String[] newWeekdays) {
        weekdays = duplicate(newWeekdays);
    }

    /**
     * Returns abbreviated weekday strings; for example: "Sun", "Mon", etc.
     * (Note: the method name is misleading; it does not get the CLDR-style
     * "short" weekday strings, e.g. "Su", "Mo", etc.)
     * @return the abbreviated weekday strings. Use <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc. to index the result array.
     */
    public String[] getShortWeekdays() {
        return duplicate(shortWeekdays);
    }

    /**
     * Sets abbreviated weekday strings; for example: "Sun", "Mon", etc.
     * (Note: the method name is misleading; it does not set the CLDR-style
     * "short" weekday strings, e.g. "Su", "Mo", etc.)
     * @param newAbbrevWeekdays the new abbreviated weekday strings. The array should
     * be indexed by <code>Calendar.SUNDAY</code>,
     * <code>Calendar.MONDAY</code>, etc.
     */
    public void setShortWeekdays(String[] newAbbrevWeekdays) {
        shortWeekdays = duplicate(newAbbrevWeekdays);
    }
    /**
     * <strong>[icu]</strong> Returns quarter strings. For example: "1st Quarter", "2nd Quarter", etc.
     * @param context    The quarter context, FORMAT or STANDALONE.
     * @param width      The width or the returned quarter string,
     *                   either WIDE or ABBREVIATED. There are no NARROW quarters.
     * @return the quarter strings.
     */
    public String[] getQuarters(int context, int width) {
        String [] returnValue = null;
        switch (context) {
           case FORMAT :
              switch(width) {
                 case WIDE :
                    returnValue = quarters;
                    break;
                 case ABBREVIATED :
                 case SHORT : // no quarter data for this, defaults to ABBREVIATED
                    returnValue = shortQuarters;
                    break;
                 case NARROW :
                     returnValue = null;
                     break;
              }
              break;

           case STANDALONE :
              switch(width) {
                 case WIDE :
                    returnValue = standaloneQuarters;
                    break;
                 case ABBREVIATED :
                 case SHORT : // no quarter data for this, defaults to ABBREVIATED
                    returnValue = standaloneShortQuarters;
                    break;
                 case NARROW:
                     returnValue = null;
                     break;
              }
              break;
        }
        if (returnValue == null) {
            throw new IllegalArgumentException("Bad context or width argument");
        }
        return duplicate(returnValue);
    }

    /**
     * <strong>[icu]</strong> Sets quarter strings. For example: "1st Quarter", "2nd Quarter", etc.
     * @param newQuarters the new quarter strings.
     * @param context    The formatting context, FORMAT or STANDALONE.
     * @param width      The width of the quarter string,
     *                   either WIDE or ABBREVIATED. There are no NARROW quarters.
     */
    public void setQuarters(String[] newQuarters, int context, int width) {
        switch (context) {
           case FORMAT :
              switch(width) {
                 case WIDE :
                    quarters = duplicate(newQuarters);
                    break;
                 case ABBREVIATED :
                    shortQuarters = duplicate(newQuarters);
                    break;
                 case NARROW :
                    //narrowQuarters = duplicate(newQuarters);
                    break;
                 default : // HANDLE SHORT, etc.
                    break;
              }
              break;
           case STANDALONE :
              switch(width) {
                 case WIDE :
                    standaloneQuarters = duplicate(newQuarters);
                    break;
                 case ABBREVIATED :
                    standaloneShortQuarters = duplicate(newQuarters);
                    break;
                 case NARROW :
                    //standaloneNarrowQuarters = duplicate(newQuarters);
                    break;
                 default : // HANDLE SHORT, etc.
                    break;
              }
              break;
        }
    }

    /**
     * Returns cyclic year name strings if the calendar has them,
     * for example: "jia-zi", "yi-chou", etc.
     * @param context   The usage context: FORMAT, STANDALONE.
     * @param width     The requested name width: WIDE, ABBREVIATED, SHORT, NARROW.
     * @return          The year name strings, or null if they are not
     *                  available for this calendar.
     */
    public String[] getYearNames(int context, int width) {
        // context & width ignored for now, one set of names for all uses
        if (shortYearNames != null) {
            return duplicate(shortYearNames);
        }
        return null;
    }

    /**
     * Sets cyclic year name strings, for example: "jia-zi", "yi-chou", etc.
     * @param yearNames The new cyclic year name strings.
     * @param context   The usage context: FORMAT, STANDALONE (currently only FORMAT is supported).
     * @param width     The name width: WIDE, ABBREVIATED, NARROW (currently only ABBREVIATED is supported).
     */
    public void setYearNames(String[] yearNames, int context, int width) {
        if (context == FORMAT && width == ABBREVIATED) {
            shortYearNames = duplicate(yearNames);
        }
    }

    /**
     * Returns calendar zodiac name strings if the calendar has them,
     * for example: "Rat", "Ox", "Tiger", etc.
     * @param context   The usage context: FORMAT, STANDALONE.
     * @param width     The requested name width: WIDE, ABBREVIATED, SHORT, NARROW.
     * @return          The zodiac name strings, or null if they are not
     *                  available for this calendar.
     */
    public String[] getZodiacNames(int context, int width) {
        // context & width ignored for now, one set of names for all uses
        if (shortZodiacNames != null) {
            return duplicate(shortZodiacNames);
        }
        return null;
    }

    /**
     * Sets calendar zodiac name strings, for example: "Rat", "Ox", "Tiger", etc.
     * @param zodiacNames   The new zodiac name strings.
     * @param context   The usage context: FORMAT, STANDALONE (currently only FORMAT is supported).
     * @param width     The name width: WIDE, ABBREVIATED, NARROW (currently only ABBREVIATED is supported).
     */
    public void setZodiacNames(String[] zodiacNames, int context, int width) {
        if (context == FORMAT && width == ABBREVIATED) {
            shortZodiacNames = duplicate(zodiacNames);
        }
    }

    /**
     * Returns the appropriate leapMonthPattern if the calendar has them,
     * for example: "{0}bis"
     * @param context   The usage context: FORMAT, STANDALONE, NUMERIC.
     * @param width     The requested pattern width: WIDE, ABBREVIATED, SHORT, NARROW.
     * @return          The leapMonthPattern, or null if not available for
     *                  this calendar.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public String getLeapMonthPattern(int context, int width) {
        if (leapMonthPatterns != null) {
            int leapMonthPatternIndex = -1;
            switch (context) {
               case FORMAT :
                  switch(width) {
                     case WIDE :
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_FORMAT_WIDE;
                        break;
                     case ABBREVIATED :
                     case SHORT : // no month data for this, defaults to ABBREVIATED
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_FORMAT_ABBREV;
                        break;
                     case NARROW :
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_FORMAT_NARROW;
                        break;
                  }
                  break;
               case STANDALONE :
                  switch(width) {
                     case WIDE :
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_STANDALONE_WIDE;
                        break;
                     case ABBREVIATED :
                     case SHORT : // no month data for this, defaults to ABBREVIATED
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_FORMAT_ABBREV;
                        break;
                     case NARROW :
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_STANDALONE_NARROW;
                        break;
                  }
                  break;
               case NUMERIC :
                  leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_NUMERIC;
                  break;
            }
            if (leapMonthPatternIndex < 0) {
                throw new IllegalArgumentException("Bad context or width argument");
            }
            return leapMonthPatterns[leapMonthPatternIndex];
        }
        return null;
    }

    /**
     * Sets a leapMonthPattern, for example: "{0}bis"
     * @param leapMonthPattern  The new leapMonthPattern.
     * @param context   The usage context: FORMAT, STANDALONE, NUMERIC.
     * @param width     The name width: WIDE, ABBREVIATED, NARROW.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public void setLeapMonthPattern(String leapMonthPattern, int context, int width) {
        if (leapMonthPatterns != null) {
            int leapMonthPatternIndex = -1;
            switch (context) {
               case FORMAT :
                  switch(width) {
                     case WIDE :
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_FORMAT_WIDE;
                        break;
                     case ABBREVIATED :
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_FORMAT_ABBREV;
                        break;
                     case NARROW :
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_FORMAT_NARROW;
                        break;
                     default : // HANDLE SHORT, etc.
                        break;
                  }
                  break;
               case STANDALONE :
                  switch(width) {
                     case WIDE :
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_STANDALONE_WIDE;
                        break;
                     case ABBREVIATED :
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_FORMAT_ABBREV;
                        break;
                     case NARROW :
                        leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_STANDALONE_NARROW;
                        break;
                     default : // HANDLE SHORT, etc.
                        break;
                  }
                  break;
               case NUMERIC :
                  leapMonthPatternIndex = DT_LEAP_MONTH_PATTERN_NUMERIC;
                  break;
               default :
                  break;
            }
            if (leapMonthPatternIndex >= 0) {
                leapMonthPatterns[leapMonthPatternIndex] = leapMonthPattern;
            }
        }
    }

    /**
     * Returns am/pm strings. For example: "AM" and "PM".
     * @return the weekday strings.
     */
    public String[] getAmPmStrings() {
        return duplicate(ampms);
    }

    /**
     * Sets am/pm strings. For example: "AM" and "PM".
     * @param newAmpms the new ampm strings.
     */
    public void setAmPmStrings(String[] newAmpms) {
        ampms = duplicate(newAmpms);
    }

    /**
     * Returns the time separator string. For example: ":".
     * @return the time separator string.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public String getTimeSeparatorString() {
        return timeSeparator;
    }

    /**
     * Sets the time separator string. For example: ":".
     * @param newTimeSeparator the new time separator string.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public void setTimeSeparatorString(String newTimeSeparator) {
        timeSeparator = newTimeSeparator;
    }

    /**
     * Returns time zone strings.
     * <p>
     * The array returned by this API is a two dimensional String array and
     * each row contains at least following strings:
     * <ul>
     * <li>ZoneStrings[n][0] - System time zone ID
     * <li>ZoneStrings[n][1] - Long standard time display name
     * <li>ZoneStrings[n][2] - Short standard time display name
     * <li>ZoneStrings[n][3] - Long daylight saving time display name
     * <li>ZoneStrings[n][4] - Short daylight saving time display name
     * </ul>
     * When a localized display name is not available, the corresponding
     * array element will be <code>null</code>.
     * <p>
     * <b>Note</b>: ICU implements the time zone display name formatting algorithm
     * specified by <a href="http://www.unicode.org/reports/tr35/">UTS#35 Unicode
     * Locale Data Markup Language(LDML)</a>. The algorithm supports historic
     * display name changes and various different types of names not available in
     * {@link java.text.DateFormatSymbols#getZoneStrings()}. For accessing the full
     * set of time zone string data used by ICU implementation, you should use
     * {@link TimeZoneNames} APIs instead.
     *
     * @return the time zone strings.
     */
    public String[][] getZoneStrings() {
        if (zoneStrings != null) {
            return duplicate(zoneStrings);
        }

        String[] tzIDs = TimeZone.getAvailableIDs();
        TimeZoneNames tznames = TimeZoneNames.getInstance(validLocale);
        tznames.loadAllDisplayNames();
        NameType types[] = {
            NameType.LONG_STANDARD, NameType.SHORT_STANDARD,
            NameType.LONG_DAYLIGHT, NameType.SHORT_DAYLIGHT
        };
        long now = System.currentTimeMillis();
        String[][] array = new String[tzIDs.length][5];
        for (int i = 0; i < tzIDs.length; i++) {
            String canonicalID = TimeZone.getCanonicalID(tzIDs[i]);
            if (canonicalID == null) {
                canonicalID = tzIDs[i];
            }

            array[i][0] = tzIDs[i];
            tznames.getDisplayNames(canonicalID, types, now, array[i], 1);
        }

        zoneStrings = array;
        return zoneStrings;
    }

    /**
     * Sets time zone strings.
     * <p>
     * <b>Note</b>: {@link SimpleDateFormat} no longer uses the
     * zone strings stored in a <code>DateFormatSymbols</code>.
     * Therefore, the time zone strings set by this method have
     * no effects in an instance of <code>SimpleDateFormat</code>
     * for formatting time zones. If you want to customize time
     * zone display names formatted by <code>SimpleDateFormat</code>,
     * you should customize {@link TimeZoneFormat} and set the
     * instance by {@link SimpleDateFormat#setTimeZoneFormat(TimeZoneFormat)}
     * instead.
     *
     * @param newZoneStrings the new time zone strings.
     */
    public void setZoneStrings(String[][] newZoneStrings) {
        zoneStrings = duplicate(newZoneStrings);
    }

    /**
     * Returns localized date-time pattern characters. For example: 'u', 't', etc.
     *
     * <p>Note: ICU no longer provides localized date-time pattern characters for a locale
     * starting ICU 3.8.  This method returns the non-localized date-time pattern
     * characters unless user defined localized data is set by setLocalPatternChars.
     * @return the localized date-time pattern characters.
     */
    public String getLocalPatternChars() {
        return localPatternChars;
    }

    /**
     * Sets localized date-time pattern characters. For example: 'u', 't', etc.
     * @param newLocalPatternChars the new localized date-time
     * pattern characters.
     */
    public void setLocalPatternChars(String newLocalPatternChars) {
        localPatternChars = newLocalPatternChars;
    }

    /**
     * Overrides clone.
     */
    @Override
    public Object clone()
    {
        try {
            DateFormatSymbols other = (DateFormatSymbols)super.clone();
            return other;
        } catch (CloneNotSupportedException e) {
            ///CLOVER:OFF
            throw new ICUCloneNotSupportedException(e);
            ///CLOVER:ON
        }
    }

    /**
     * Override hashCode.
     * Generates a hash code for the DateFormatSymbols object.
     */
    @Override
    public int hashCode() {
        // Is this sufficient?
        return requestedLocale.toString().hashCode();
    }

    /**
     * Overrides equals.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DateFormatSymbols that = (DateFormatSymbols) obj;
        return (Utility.arrayEquals(eras, that.eras)
                && Utility.arrayEquals(eraNames, that.eraNames)
                && Utility.arrayEquals(months, that.months)
                && Utility.arrayEquals(shortMonths, that.shortMonths)
                && Utility.arrayEquals(narrowMonths, that.narrowMonths)
                && Utility.arrayEquals(standaloneMonths, that.standaloneMonths)
                && Utility.arrayEquals(standaloneShortMonths, that.standaloneShortMonths)
                && Utility.arrayEquals(standaloneNarrowMonths, that.standaloneNarrowMonths)
                && Utility.arrayEquals(weekdays, that.weekdays)
                && Utility.arrayEquals(shortWeekdays, that.shortWeekdays)
                && Utility.arrayEquals(shorterWeekdays, that.shorterWeekdays)
                && Utility.arrayEquals(narrowWeekdays, that.narrowWeekdays)
                && Utility.arrayEquals(standaloneWeekdays, that.standaloneWeekdays)
                && Utility.arrayEquals(standaloneShortWeekdays, that.standaloneShortWeekdays)
                && Utility.arrayEquals(standaloneShorterWeekdays, that.standaloneShorterWeekdays)
                && Utility.arrayEquals(standaloneNarrowWeekdays, that.standaloneNarrowWeekdays)
                && Utility.arrayEquals(ampms, that.ampms)
                && Utility.arrayEquals(ampmsNarrow, that.ampmsNarrow)
                && Utility.arrayEquals(abbreviatedDayPeriods, that.abbreviatedDayPeriods)
                && Utility.arrayEquals(wideDayPeriods, that.wideDayPeriods)
                && Utility.arrayEquals(narrowDayPeriods, that.narrowDayPeriods)
                && Utility.arrayEquals(standaloneAbbreviatedDayPeriods, that.standaloneAbbreviatedDayPeriods)
                && Utility.arrayEquals(standaloneWideDayPeriods, that.standaloneWideDayPeriods)
                && Utility.arrayEquals(standaloneNarrowDayPeriods, that.standaloneNarrowDayPeriods)
                && Utility.arrayEquals(timeSeparator, that.timeSeparator)
                && arrayOfArrayEquals(zoneStrings, that.zoneStrings)
                // getDiplayName maps deprecated country and language codes to the current ones
                // too bad there is no way to get the current codes!
                // I thought canolicalize() would map the codes but .. alas! it doesn't.
                && requestedLocale.getDisplayName().equals(that.requestedLocale.getDisplayName())
                && Utility.arrayEquals(localPatternChars,
                                       that.localPatternChars));
    }

    // =======================privates===============================

    /**
     * Useful constant for defining timezone offsets.
     */
    static final int millisPerHour = 60*60*1000;

    // DateFormatSymbols cache
    private static CacheBase<String, DateFormatSymbols, ULocale> DFSCACHE =
        new SoftCache<String, DateFormatSymbols, ULocale>() {
            @Override
            protected DateFormatSymbols createInstance(String key, ULocale locale) {
                // Extract the type string from the key.
                // Otherwise we would have to create a pair object that
                // carries both the locale and the type.
                int typeStart = key.indexOf('+') + 1;
                int typeLimit = key.indexOf('+', typeStart);
                if (typeLimit < 0) {
                    // no numbers keyword value
                    typeLimit = key.length();
                }
                String type = key.substring(typeStart, typeLimit);
                return new DateFormatSymbols(locale, null, type);
            }
        };

    /**
     * Initializes format symbols for the locale and calendar type
     * @param desiredLocale The locale whose symbols are desired.
     * @param type          The calendar type whose date format symbols are desired.
     */
    //TODO: This protected seems to be marked as @stable accidentally.
    // We may need to deescalate this API to @internal.
    protected void initializeData(ULocale desiredLocale, String type)
    {
        String key = desiredLocale.getBaseName() + '+' + type;
        String ns = desiredLocale.getKeywordValue("numbers");
        if (ns != null && ns.length() > 0) {
            key += '+' + ns;
        }
        DateFormatSymbols dfs = DFSCACHE.getInstance(key, desiredLocale);
        initializeData(dfs);
    }

    /**
     * Initializes format symbols using another instance.
     *
     * TODO Clean up initialization methods for subclasses
     */
    void initializeData(DateFormatSymbols dfs) {
        this.eras = dfs.eras;
        this.eraNames = dfs.eraNames;
        this.narrowEras = dfs.narrowEras;
        this.months = dfs.months;
        this.shortMonths = dfs.shortMonths;
        this.narrowMonths = dfs.narrowMonths;
        this.standaloneMonths = dfs.standaloneMonths;
        this.standaloneShortMonths = dfs.standaloneShortMonths;
        this.standaloneNarrowMonths = dfs.standaloneNarrowMonths;
        this.weekdays = dfs.weekdays;
        this.shortWeekdays = dfs.shortWeekdays;
        this.shorterWeekdays = dfs.shorterWeekdays;
        this.narrowWeekdays = dfs.narrowWeekdays;
        this.standaloneWeekdays = dfs.standaloneWeekdays;
        this.standaloneShortWeekdays = dfs.standaloneShortWeekdays;
        this.standaloneShorterWeekdays = dfs.standaloneShorterWeekdays;
        this.standaloneNarrowWeekdays = dfs.standaloneNarrowWeekdays;
        this.ampms = dfs.ampms;
        this.ampmsNarrow = dfs.ampmsNarrow;
        this.timeSeparator = dfs.timeSeparator;
        this.shortQuarters = dfs.shortQuarters;
        this.quarters = dfs.quarters;
        this.standaloneShortQuarters = dfs.standaloneShortQuarters;
        this.standaloneQuarters = dfs.standaloneQuarters;
        this.leapMonthPatterns = dfs.leapMonthPatterns;
        this.shortYearNames = dfs.shortYearNames;
        this.shortZodiacNames = dfs.shortZodiacNames;
        this.abbreviatedDayPeriods = dfs.abbreviatedDayPeriods;
        this.wideDayPeriods = dfs.wideDayPeriods;
        this.narrowDayPeriods = dfs.narrowDayPeriods;
        this.standaloneAbbreviatedDayPeriods = dfs.standaloneAbbreviatedDayPeriods;
        this.standaloneWideDayPeriods = dfs.standaloneWideDayPeriods;
        this.standaloneNarrowDayPeriods = dfs.standaloneNarrowDayPeriods;

        this.zoneStrings = dfs.zoneStrings; // always null at initialization time for now
        this.localPatternChars = dfs.localPatternChars;

        this.capitalization = dfs.capitalization;

        this.actualLocale = dfs.actualLocale;
        this.validLocale = dfs.validLocale;
        this.requestedLocale = dfs.requestedLocale;
    }


    /**
     * Sink to enumerate the calendar data
     */
    private static final class CalendarDataSink extends UResource.Sink {

        // Data structures to store resources from the resource bundle
        Map<String, String[]> arrays = new TreeMap<String, String[]>();
        Map<String, Map<String, String>> maps = new TreeMap<String, Map<String, String>>();
        List<String> aliasPathPairs = new ArrayList<String>();

        // Current and next calendar resource table which should be loaded
        String currentCalendarType = null;
        String nextCalendarType = null;

        // Resources to visit when enumerating fallback calendars
        private Set<String> resourcesToVisit;

        // Alias' relative path populated when an alias is read
        private String aliasRelativePath;

        /**
         * Initializes CalendarDataSink with default values
         */
        CalendarDataSink() { }

        /**
         * Configure the CalendarSink to visit all the resources
         */
        void visitAllResources() {
            resourcesToVisit = null;
        }

        /**
         * Actions to be done before enumerating
         */
        void preEnumerate(String calendarType) {
            currentCalendarType = calendarType;
            nextCalendarType = null;
            aliasPathPairs.clear();
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            assert currentCalendarType != null && !currentCalendarType.isEmpty();

            // Stores the resources to visit on the next calendar.
            Set<String> resourcesToVisitNext = null;
            UResource.Table calendarData = value.getTable();

            // Enumerate all resources for this calendar
            for (int i = 0; calendarData.getKeyAndValue(i, key, value); i++) {
                String keyString = key.toString();

                // == Handle aliases ==
                AliasType aliasType = processAliasFromValue(keyString, value);
                if (aliasType == AliasType.GREGORIAN) {
                    // Ignore aliases to the gregorian calendar, all of its resources will be loaded anyways.
                    continue;

                } else if (aliasType == AliasType.DIFFERENT_CALENDAR) {
                    // Whenever an alias to the next calendar (except gregorian) is encountered, register the
                    // calendar type it's pointing to
                    if (resourcesToVisitNext == null) {
                        resourcesToVisitNext = new HashSet<String>();
                    }
                    resourcesToVisitNext.add(aliasRelativePath);
                    continue;

                } else if (aliasType == AliasType.SAME_CALENDAR) {
                    // Register same-calendar alias
                    if (!arrays.containsKey(keyString) && !maps.containsKey(keyString)) {
                        aliasPathPairs.add(aliasRelativePath);
                        aliasPathPairs.add(keyString);
                    }
                    continue;
                }

                // Only visit the resources that were referenced by an alias on the previous calendar
                // (AmPmMarkersAbbr is an exception).
                if (resourcesToVisit != null && !resourcesToVisit.isEmpty() && !resourcesToVisit.contains(keyString)
                        && !keyString.equals("AmPmMarkersAbbr")) { continue; }

                // == Handle data ==
                if (keyString.startsWith("AmPmMarkers")) {
                    if (!keyString.endsWith("%variant") && !arrays.containsKey(keyString)) {
                        String[] dataArray = value.getStringArray();
                        arrays.put(keyString, dataArray);
                    }
                } else if (keyString.equals("eras")
                        || keyString.equals("dayNames")
                        || keyString.equals("monthNames")
                        || keyString.equals("quarters")
                        || keyString.equals("dayPeriod")
                        || keyString.equals("monthPatterns")
                        || keyString.equals("cyclicNameSets")) {
                    processResource(keyString, key, value);
                }
            }

            // Apply same-calendar aliases
            boolean modified;
            do {
                modified = false;
                for (int i = 0; i < aliasPathPairs.size();) {
                    boolean mod = false;
                    String alias = aliasPathPairs.get(i);
                    if (arrays.containsKey(alias)) {
                        arrays.put(aliasPathPairs.get(i + 1), arrays.get(alias));
                        mod = true;
                    } else if (maps.containsKey(alias)) {
                        maps.put(aliasPathPairs.get(i + 1), maps.get(alias));
                        mod = true;
                    }
                    if (mod) {
                        aliasPathPairs.remove(i + 1);
                        aliasPathPairs.remove(i);
                        modified = true;
                    } else {
                        i += 2;
                    }
                }
            } while (modified && !aliasPathPairs.isEmpty());

            // Set the resources to visit on the next calendar
            if (resourcesToVisitNext != null) {
                resourcesToVisit = resourcesToVisitNext;
            }
        }

        /**
         * Process the nested resource bundle tables
         * @param path Table's relative path to the calendar
         * @param key Resource bundle key
         * @param value Resource bundle value (has to have the table to read)
         */
        protected void processResource(String path, UResource.Key key, UResource.Value value) {

            UResource.Table table = value.getTable();
            Map<String, String> stringMap = null;

            // Iterate over all the elements of the table and add them to the map
            for(int i = 0; table.getKeyAndValue(i, key, value); i++) {
                // Ignore '%variant' keys
                if (key.endsWith("%variant")) { continue; }

                String keyString = key.toString();

                // == Handle String elements ==
                if (value.getType() == ICUResourceBundle.STRING) {
                    // We are on a leaf, store the map elements into the stringMap
                    if (i == 0) {
                        stringMap = new HashMap<String, String>();
                        maps.put(path, stringMap);
                    }
                    assert stringMap != null;
                    stringMap.put(keyString, value.getString());
                    continue;
                }
                assert stringMap == null;

                String currentPath = path + "/" + keyString;
                // In cyclicNameSets ignore everything but years/format/abbreviated
                // and zodiacs/format/abbreviated
                if (currentPath.startsWith("cyclicNameSets")) {
                    if (!"cyclicNameSets/years/format/abbreviated".startsWith(currentPath)
                            && !"cyclicNameSets/zodiacs/format/abbreviated".startsWith(currentPath)
                            && !"cyclicNameSets/dayParts/format/abbreviated".startsWith(currentPath))
                    { continue; }
                }

                // == Handle aliases ==
                if (arrays.containsKey(currentPath)
                        || maps.containsKey(currentPath)) { continue; }

                AliasType aliasType = processAliasFromValue(currentPath, value);
                if (aliasType == AliasType.SAME_CALENDAR) {
                    aliasPathPairs.add(aliasRelativePath);
                    aliasPathPairs.add(currentPath);
                    continue;
                }
                assert aliasType == AliasType.NONE;

                // == Handle data ==
                if (value.getType() == ICUResourceBundle.ARRAY) {
                    // We are on a leaf, store the array
                    String[] dataArray = value.getStringArray();
                    arrays.put(currentPath, dataArray);
                } else if (value.getType() == ICUResourceBundle.TABLE) {
                    // We are not on a leaf, recursively process the subtable.
                    processResource(currentPath, key, value);
                }
            }
        }

        // Alias' path prefix
        private static final String CALENDAR_ALIAS_PREFIX = "/LOCALE/calendar/";

        /**
         * Populates an AliasIdentifier with the alias information contained on the UResource.Value.
         * @param currentRelativePath Relative path of this alias' resource
         * @param value Value which contains the alias
         * @return The AliasType of the alias found on Value
         */
        private AliasType processAliasFromValue(String currentRelativePath, UResource.Value value) {
            if (value.getType() == ICUResourceBundle.ALIAS) {
                String aliasPath = value.getAliasString();
                if (aliasPath.startsWith(CALENDAR_ALIAS_PREFIX) &&
                        aliasPath.length() > CALENDAR_ALIAS_PREFIX.length()) {
                    int typeLimit = aliasPath.indexOf('/', CALENDAR_ALIAS_PREFIX.length());
                    if (typeLimit > CALENDAR_ALIAS_PREFIX.length()) {
                        String aliasCalendarType = aliasPath.substring(CALENDAR_ALIAS_PREFIX.length(), typeLimit);
                        aliasRelativePath = aliasPath.substring(typeLimit + 1);

                        if (currentCalendarType.equals(aliasCalendarType)
                                && !currentRelativePath.equals(aliasRelativePath)) {
                            // If we have an alias to the same calendar, the path to the resource must be different
                            return AliasType.SAME_CALENDAR;

                        } else if (!currentCalendarType.equals(aliasCalendarType)
                                && currentRelativePath.equals(aliasRelativePath)) {
                            // If we have an alias to a different calendar, the path to the resource must be the same
                            if (aliasCalendarType.equals("gregorian")) {
                                return AliasType.GREGORIAN;
                            } else if (nextCalendarType == null || nextCalendarType.equals(aliasCalendarType)) {
                                nextCalendarType = aliasCalendarType;
                                return AliasType.DIFFERENT_CALENDAR;
                            }
                        }
                    }
                }
                throw new ICUException("Malformed 'calendar' alias. Path: " + aliasPath);
            }
            return AliasType.NONE;
        }

        /**
         * Enum which specifies the type of alias received, or no alias
         */
        private enum AliasType {
            SAME_CALENDAR,
            DIFFERENT_CALENDAR,
            GREGORIAN,
            NONE
        }
    }

    /** Private, for cache.getInstance(). */
    private DateFormatSymbols(ULocale desiredLocale, ICUResourceBundle b, String calendarType) {
        initializeData(desiredLocale, b, calendarType);
    }

    /**
     * Initializes format symbols for the locale and calendar type
     * @param desiredLocale The locale whose symbols are desired.
     * @param b Resource bundle provided externally
     * @param calendarType  The calendar type being used
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    // This API was accidentally marked as @stable ICU 3.0 formerly.
    protected void initializeData(ULocale desiredLocale, ICUResourceBundle b, String calendarType)
    {
        // Create a CalendarSink to load this data and a resource bundle
        CalendarDataSink calendarSink = new CalendarDataSink();
        if (b == null) {
            b = (ICUResourceBundle) UResourceBundle
                    .getBundleInstance(ICUData.ICU_BASE_NAME, desiredLocale);
        }

        // Iterate over the resource bundle data following the fallbacks through different calendar types
        while (calendarType != null) {

            // Enumerate this calendar type. If the calendar is not found fallback to gregorian.
            ICUResourceBundle dataForType = b.findWithFallback("calendar/" + calendarType);
            if (dataForType == null) {
                if (!"gregorian".equals(calendarType)) {
                    calendarType = "gregorian";
                    calendarSink.visitAllResources();
                    continue;
                }
                throw new MissingResourceException("The 'gregorian' calendar type wasn't found for the locale: "
                        + desiredLocale.getBaseName(), getClass().getName(), "gregorian");
            }
            calendarSink.preEnumerate(calendarType);
            dataForType.getAllItemsWithFallback("", calendarSink);

            // Stop loading when gregorian was loaded
            if (calendarType.equals("gregorian")) {
                break;
            }

            // Get the next calendar type to process from the sink
            calendarType = calendarSink.nextCalendarType;

            // Gregorian is always the last fallback
            if (calendarType == null) {
                calendarType = "gregorian";
                calendarSink.visitAllResources();
            }
        }

        Map<String, String[]> arrays = calendarSink.arrays;
        Map<String, Map<String, String>> maps = calendarSink.maps;

        eras = arrays.get("eras/abbreviated");
        eraNames = arrays.get("eras/wide");
        narrowEras = arrays.get("eras/narrow");

        months = arrays.get("monthNames/format/wide");
        shortMonths = arrays.get("monthNames/format/abbreviated");
        narrowMonths = arrays.get("monthNames/format/narrow");

        standaloneMonths = arrays.get("monthNames/stand-alone/wide");
        standaloneShortMonths = arrays.get("monthNames/stand-alone/abbreviated");
        standaloneNarrowMonths = arrays.get("monthNames/stand-alone/narrow");

        String[] lWeekdays = arrays.get("dayNames/format/wide");
        weekdays = new String[8];
        weekdays[0] = "";  // 1-based
        System.arraycopy(lWeekdays, 0, weekdays, 1, lWeekdays.length);

        String[] aWeekdays = arrays.get("dayNames/format/abbreviated");
        shortWeekdays = new String[8];
        shortWeekdays[0] = "";  // 1-based
        System.arraycopy(aWeekdays, 0, shortWeekdays, 1, aWeekdays.length);

        String[] sWeekdays = arrays.get("dayNames/format/short");
        shorterWeekdays = new String[8];
        shorterWeekdays[0] = "";  // 1-based
        System.arraycopy(sWeekdays, 0, shorterWeekdays, 1, sWeekdays.length);

        String [] nWeekdays = arrays.get("dayNames/format/narrow");
        if (nWeekdays == null) {
            nWeekdays = arrays.get("dayNames/stand-alone/narrow");

            if (nWeekdays == null) {
                nWeekdays = arrays.get("dayNames/format/abbreviated");

                if (nWeekdays == null) {
                    throw new MissingResourceException("Resource not found",
                            getClass().getName(), "dayNames/format/abbreviated");
                }
            }
        }
        narrowWeekdays = new String[8];
        narrowWeekdays[0] = "";  // 1-based
        System.arraycopy(nWeekdays, 0, narrowWeekdays, 1, nWeekdays.length);

        String [] swWeekdays = null;
        swWeekdays = arrays.get("dayNames/stand-alone/wide");
        standaloneWeekdays = new String[8];
        standaloneWeekdays[0] = "";  // 1-based
        System.arraycopy(swWeekdays, 0, standaloneWeekdays, 1, swWeekdays.length);

        String [] saWeekdays = null;
        saWeekdays = arrays.get("dayNames/stand-alone/abbreviated");
        standaloneShortWeekdays = new String[8];
        standaloneShortWeekdays[0] = "";  // 1-based
        System.arraycopy(saWeekdays, 0, standaloneShortWeekdays, 1, saWeekdays.length);

        String [] ssWeekdays = null;
        ssWeekdays = arrays.get("dayNames/stand-alone/short");
        standaloneShorterWeekdays = new String[8];
        standaloneShorterWeekdays[0] = "";  // 1-based
        System.arraycopy(ssWeekdays, 0, standaloneShorterWeekdays, 1, ssWeekdays.length);

        String [] snWeekdays = null;
        snWeekdays = arrays.get("dayNames/stand-alone/narrow");
        standaloneNarrowWeekdays = new String[8];
        standaloneNarrowWeekdays[0] = "";  // 1-based
        System.arraycopy(snWeekdays, 0, standaloneNarrowWeekdays, 1, snWeekdays.length);

        ampms = arrays.get("AmPmMarkers");
        ampmsNarrow = arrays.get("AmPmMarkersNarrow");

        quarters = arrays.get("quarters/format/wide");
        shortQuarters = arrays.get("quarters/format/abbreviated");

        standaloneQuarters = arrays.get("quarters/stand-alone/wide");
        standaloneShortQuarters = arrays.get("quarters/stand-alone/abbreviated");

        abbreviatedDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/format/abbreviated"));
        wideDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/format/wide"));
        narrowDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/format/narrow"));
        standaloneAbbreviatedDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/stand-alone/abbreviated"));
        standaloneWideDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/stand-alone/wide"));
        standaloneNarrowDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/stand-alone/narrow"));

        for (int i = 0; i < DT_MONTH_PATTERN_COUNT; i++) {
            String monthPatternPath = LEAP_MONTH_PATTERNS_PATHS[i];
            if (monthPatternPath != null) {
                Map<String, String> monthPatternMap = maps.get(monthPatternPath);
                if (monthPatternMap != null) {
                    String leapMonthPattern = monthPatternMap.get("leap");
                    if (leapMonthPattern != null) {
                        if (leapMonthPatterns == null) {
                            leapMonthPatterns = new String[DT_MONTH_PATTERN_COUNT];
                        }
                        leapMonthPatterns[i] = leapMonthPattern;
                    }
                }
            }
        }

        shortYearNames = arrays.get("cyclicNameSets/years/format/abbreviated");
        shortZodiacNames = arrays.get("cyclicNameSets/zodiacs/format/abbreviated");

        requestedLocale = desiredLocale;

        ICUResourceBundle rb =
            (ICUResourceBundle)UResourceBundle.getBundleInstance(
                ICUData.ICU_BASE_NAME, desiredLocale);

        localPatternChars = patternChars;

        // TODO: obtain correct actual/valid locale later
        ULocale uloc = rb.getULocale();
        setLocale(uloc, uloc);

        capitalization = new HashMap<CapitalizationContextUsage,boolean[]>();
        boolean[] noTransforms = new boolean[2];
        noTransforms[0] = false;
        noTransforms[1] = false;
        CapitalizationContextUsage allUsages[] = CapitalizationContextUsage.values();
        for (CapitalizationContextUsage usage: allUsages) {
            capitalization.put(usage, noTransforms);
        }
        UResourceBundle contextTransformsBundle = null;
        try {
           contextTransformsBundle = rb.getWithFallback("contextTransforms");
        }
        catch (MissingResourceException e) {
            contextTransformsBundle = null; // probably redundant
        }
        if (contextTransformsBundle != null) {
            UResourceBundleIterator ctIterator = contextTransformsBundle.getIterator();
            while ( ctIterator.hasNext() ) {
                UResourceBundle contextTransformUsage = ctIterator.next();
                int[] intVector = contextTransformUsage.getIntVector();
                if (intVector.length >= 2) {
                    String usageKey = contextTransformUsage.getKey();
                    CapitalizationContextUsage usage = contextUsageTypeMap.get(usageKey);
                    if (usage != null) {
                        boolean[] transforms = new boolean[2];
                        transforms[0] = (intVector[0] != 0);
                        transforms[1] = (intVector[1] != 0);
                        capitalization.put(usage, transforms);
                    }
                }
            }
        }

        NumberingSystem ns = NumberingSystem.getInstance(desiredLocale);
        String nsName = ns == null ? "latn" : ns.getName();  // Latin is default.
        String tsPath = "NumberElements/" + nsName + "/symbols/timeSeparator";
        try {
            setTimeSeparatorString(rb.getStringWithFallback(tsPath));
        } catch (MissingResourceException e) {
            setTimeSeparatorString(DEFAULT_TIME_SEPARATOR);
        }
    }

    /**
     * Resource bundle paths for each leap month pattern
     */
    private static final String[] LEAP_MONTH_PATTERNS_PATHS = new String[DT_MONTH_PATTERN_COUNT];
    static {
        LEAP_MONTH_PATTERNS_PATHS[DT_LEAP_MONTH_PATTERN_FORMAT_WIDE] = "monthPatterns/format/wide";
        LEAP_MONTH_PATTERNS_PATHS[DT_LEAP_MONTH_PATTERN_FORMAT_ABBREV] = "monthPatterns/format/abbreviated";
        LEAP_MONTH_PATTERNS_PATHS[DT_LEAP_MONTH_PATTERN_FORMAT_NARROW] = "monthPatterns/format/narrow";
        LEAP_MONTH_PATTERNS_PATHS[DT_LEAP_MONTH_PATTERN_STANDALONE_WIDE] = "monthPatterns/stand-alone/wide";
        LEAP_MONTH_PATTERNS_PATHS[DT_LEAP_MONTH_PATTERN_STANDALONE_ABBREV] = "monthPatterns/stand-alone/abbreviated";
        LEAP_MONTH_PATTERNS_PATHS[DT_LEAP_MONTH_PATTERN_STANDALONE_NARROW] = "monthPatterns/stand-alone/narrow";
        LEAP_MONTH_PATTERNS_PATHS[DT_LEAP_MONTH_PATTERN_NUMERIC] = "monthPatterns/numeric/all";
    }

    private static final boolean arrayOfArrayEquals(Object[][] aa1, Object[][]aa2) {
        if (aa1 == aa2) { // both are null
            return true;
        }
        if (aa1 == null || aa2 == null) { // one is null and the other is not
            return false;
        }
        if (aa1.length != aa2.length) {
            return false;
        }
        boolean equal = true;
        for (int i = 0; i < aa1.length; i++) {
            equal = Utility.arrayEquals(aa1[i], aa2[i]);
            if (!equal) {
                break;
            }
        }
        return equal;
    }

    /**
     * Keys for dayPeriods
     */
    private static final String[] DAY_PERIOD_KEYS = {"midnight", "noon",
            "morning1", "afternoon1", "evening1", "night1",
            "morning2", "afternoon2", "evening2", "night2"};

    /**
     * Loads localized names for day periods in the requested format.
     * @param resourceMap Contains the dayPeriod resource to load
     */
    private String[] loadDayPeriodStrings(Map<String, String> resourceMap) {
        String strings[] = new String[DAY_PERIOD_KEYS.length];
        if (resourceMap != null) {
            for (int i = 0; i < DAY_PERIOD_KEYS.length; ++i) {
                strings[i] = resourceMap.get(DAY_PERIOD_KEYS[i]);  // Null if string doesn't exist.
            }
        }
        return strings;
    }

    /*
     * save the input locale
     */
    private ULocale requestedLocale;

    /*
     * Clones an array of Strings.
     * @param srcArray the source array to be cloned.
     * @return a cloned array.
     */
    private final String[] duplicate(String[] srcArray)
    {
        return srcArray.clone();
    }

    private final String[][] duplicate(String[][] srcArray)
    {
        String[][] aCopy = new String[srcArray.length][];
        for (int i = 0; i < srcArray.length; ++i)
            aCopy[i] = duplicate(srcArray[i]);
        return aCopy;
    }

    /*
     * Compares the equality of the two arrays of String.
     * @param current this String array.
     * @param other that String array.
    private final boolean equals(String[] current, String[] other)
    {
        int count = current.length;

        for (int i = 0; i < count; ++i)
            if (!current[i].equals(other[i]))
                return false;
        return true;
    }
     */

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Returns the {@link DateFormatSymbols} object that should be used to format a
     * calendar system's dates in the given locale.
     * <p>
     * <b>Subclassing:</b><br>
     * When creating a new Calendar subclass, you must create the
     * {@link ResourceBundle ResourceBundle}
     * containing its {@link DateFormatSymbols DateFormatSymbols} in a specific place.
     * The resource bundle name is based on the calendar's fully-specified
     * class name, with ".resources" inserted at the end of the package name
     * (just before the class name) and "Symbols" appended to the end.
     * For example, the bundle corresponding to "android.icu.util.HebrewCalendar"
     * is "android.icu.impl.data.HebrewCalendarSymbols".
     * <p>
     * Within the ResourceBundle, this method searches for five keys:
     * <ul>
     * <li><b>DayNames</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>DAY_OF_WEEK</code> field.  Even though
     *      <code>DAY_OF_WEEK</code> starts with <code>SUNDAY</code> = 1,
     *      This array is 0-based; the name for Sunday goes in the
     *      first position, at index 0.  If this key is not found
     *      in the bundle, the day names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     *
     * <li><b>DayAbbreviations</b> -
     *      An array of abbreviated day names corresponding
     *      to the values in the "DayNames" array.  If this key
     *      is not found in the resource bundle, the "DayNames"
     *      values are used instead.  If neither key is found,
     *      the day abbreviations are inherited from the default
     *      <code>DateFormatSymbols</code> for the locale.
     *
     * <li><b>MonthNames</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>MONTH</code> field.  If this key is not found
     *      in the bundle, the month names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     *
     * <li><b>MonthAbbreviations</b> -
     *      An array of abbreviated day names corresponding
     *      to the values in the "MonthNames" array.  If this key
     *      is not found in the resource bundle, the "MonthNames"
     *      values are used instead.  If neither key is found,
     *      the day abbreviations are inherited from the default
     *      <code>DateFormatSymbols</code> for the locale.
     *
     * <li><b>Eras</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>ERA</code> field.  If this key is not found
     *      in the bundle, the era names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     * </ul>
     * <p>
     * @param cal       The calendar system whose date format symbols are desired.
     * @param locale    The locale whose symbols are desired.
     *
     * @see DateFormatSymbols#DateFormatSymbols(java.util.Locale)
     */
    public DateFormatSymbols(Calendar cal, Locale locale) {
        initializeData(ULocale.forLocale(locale), cal.getType());
    }

    /**
     * Returns the {@link DateFormatSymbols} object that should be used to format a
     * calendar system's dates in the given locale.
     * <p>
     * <b>Subclassing:</b><br>
     * When creating a new Calendar subclass, you must create the
     * {@link ResourceBundle ResourceBundle}
     * containing its {@link DateFormatSymbols DateFormatSymbols} in a specific place.
     * The resource bundle name is based on the calendar's fully-specified
     * class name, with ".resources" inserted at the end of the package name
     * (just before the class name) and "Symbols" appended to the end.
     * For example, the bundle corresponding to "android.icu.util.HebrewCalendar"
     * is "android.icu.impl.data.HebrewCalendarSymbols".
     * <p>
     * Within the ResourceBundle, this method searches for five keys:
     * <ul>
     * <li><b>DayNames</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>DAY_OF_WEEK</code> field.  Even though
     *      <code>DAY_OF_WEEK</code> starts with <code>SUNDAY</code> = 1,
     *      This array is 0-based; the name for Sunday goes in the
     *      first position, at index 0.  If this key is not found
     *      in the bundle, the day names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     *
     * <li><b>DayAbbreviations</b> -
     *      An array of abbreviated day names corresponding
     *      to the values in the "DayNames" array.  If this key
     *      is not found in the resource bundle, the "DayNames"
     *      values are used instead.  If neither key is found,
     *      the day abbreviations are inherited from the default
     *      <code>DateFormatSymbols</code> for the locale.
     *
     * <li><b>MonthNames</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>MONTH</code> field.  If this key is not found
     *      in the bundle, the month names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     *
     * <li><b>MonthAbbreviations</b> -
     *      An array of abbreviated day names corresponding
     *      to the values in the "MonthNames" array.  If this key
     *      is not found in the resource bundle, the "MonthNames"
     *      values are used instead.  If neither key is found,
     *      the day abbreviations are inherited from the default
     *      <code>DateFormatSymbols</code> for the locale.
     *
     * <li><b>Eras</b> -
     *      An array of strings corresponding to each possible
     *      value of the <code>ERA</code> field.  If this key is not found
     *      in the bundle, the era names are inherited from the
     *      default <code>DateFormatSymbols</code> for the requested locale.
     * </ul>
     * <p>
     * @param cal       The calendar system whose date format symbols are desired.
     * @param locale    The ulocale whose symbols are desired.
     *
     * @see DateFormatSymbols#DateFormatSymbols(java.util.Locale)
     */
    public DateFormatSymbols(Calendar cal, ULocale locale) {
        initializeData(locale, cal.getType());
    }

    /**
     * Variant of DateFormatSymbols(Calendar, Locale) that takes the Calendar class
     * instead of a Calendar instance.
     * @see #DateFormatSymbols(Calendar, Locale)
     */
    public DateFormatSymbols(Class<? extends Calendar> calendarClass, Locale locale) {
        this(calendarClass, ULocale.forLocale(locale));
    }

    /**
     * Variant of DateFormatSymbols(Calendar, ULocale) that takes the Calendar class
     * instead of a Calendar instance.
     * @see #DateFormatSymbols(Calendar, Locale)
     */
    public DateFormatSymbols(Class<? extends Calendar> calendarClass, ULocale locale) {
        String fullName = calendarClass.getName();
        int lastDot = fullName.lastIndexOf('.');
        String className = fullName.substring(lastDot+1);
        String calType = null;
        for (String[] calClassInfo : CALENDAR_CLASSES) {
            if (calClassInfo[0].equals(className)) {
                calType = calClassInfo[1];
                break;
            }
        }
        if (calType == null) {
            calType = className.replaceAll("Calendar", "").toLowerCase(Locale.ENGLISH);
        }

        initializeData(locale, calType);
    }

    // Android patch (http://b/30464240) start: Add constructor taking a calendar type.
    /**
     * Variant of DateFormatSymbols(Calendar, ULocale) that takes the calendar type
     * instead of a Calendar instance.
     * @see #DateFormatSymbols(Calendar, Locale)
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public DateFormatSymbols(ULocale locale, String calType) {
        initializeData(locale, calType);
    }
    // Android patch end.

    /**
     * Fetches a custom calendar's DateFormatSymbols out of the given resource
     * bundle.  Symbols that are not overridden are inherited from the
     * default DateFormatSymbols for the locale.
     * @see DateFormatSymbols#DateFormatSymbols(java.util.Locale)
     */
    public DateFormatSymbols(ResourceBundle bundle, Locale locale) {
        this(bundle, ULocale.forLocale(locale));
    }

    /**
     * Fetches a custom calendar's DateFormatSymbols out of the given resource
     * bundle.  Symbols that are not overridden are inherited from the
     * default DateFormatSymbols for the locale.
     * @see DateFormatSymbols#DateFormatSymbols(java.util.Locale)
     */
    public DateFormatSymbols(ResourceBundle bundle, ULocale locale) {
        initializeData(locale, (ICUResourceBundle) bundle, CalendarUtil.getCalendarType(locale));
    }

    /**
     * Finds the ResourceBundle containing the date format information for
     * a specified calendar subclass in a given locale.
     * <p>
     * The resource bundle name is based on the calendar's fully-specified
     * class name, with ".resources" inserted at the end of the package name
     * (just before the class name) and "Symbols" appended to the end.
     * For example, the bundle corresponding to "android.icu.util.HebrewCalendar"
     * is "android.icu.impl.data.HebrewCalendarSymbols".
     * <p>
     * <b>Note:</b>Because of the structural changes in the ICU locale bundle,
     * this API no longer works as described.  This method always returns null.
     * @deprecated ICU 4.0
     * @hide original deprecated declaration
     */
    @Deprecated
    // This API was formerly @stable ICU 2.0
    static public ResourceBundle getDateFormatBundle(Class<? extends Calendar> calendarClass,
                                                     Locale locale)
        throws MissingResourceException {
        return null;
    }

    /**
     * Finds the ResourceBundle containing the date format information for
     * a specified calendar subclass in a given locale.
     * <p>
     * The resource bundle name is based on the calendar's fully-specified
     * class name, with ".resources" inserted at the end of the package name
     * (just before the class name) and "Symbols" appended to the end.
     * For example, the bundle corresponding to "android.icu.util.HebrewCalendar"
     * is "android.icu.impl.data.HebrewCalendarSymbols".
     * <p>
     * <b>Note:</b>Because of the structural changes in the ICU locale bundle,
     * this API no longer works as described.  This method always returns null.
     * @deprecated ICU 4.0
     * @hide original deprecated declaration
     */
    @Deprecated
    // This API was formerly @stable ICU 3.2
    static public ResourceBundle getDateFormatBundle(Class<? extends Calendar> calendarClass,
                                                     ULocale locale)
        throws MissingResourceException {
        return null;
    }

    /**
     * Variant of getDateFormatBundle(java.lang.Class, java.util.Locale) that takes
     * a Calendar instance instead of a Calendar class.
     * <p>
     * <b>Note:</b>Because of the structural changes in the ICU locale bundle,
     * this API no longer works as described.  This method always returns null.
     * @see #getDateFormatBundle(java.lang.Class, java.util.Locale)
     * @deprecated ICU 4.0
     * @hide original deprecated declaration
     */
    @Deprecated
    // This API was formerly @stable ICU 2.2
    public static ResourceBundle getDateFormatBundle(Calendar cal, Locale locale)
        throws MissingResourceException {
        return null;
    }

    /**
     * Variant of getDateFormatBundle(java.lang.Class, java.util.Locale) that takes
     * a Calendar instance instead of a Calendar class.
     * <p>
     * <b>Note:</b>Because of the structural changes in the ICU locale bundle,
     * this API no longer works as described.  This method always returns null.
     * @see #getDateFormatBundle(java.lang.Class, java.util.Locale)
     * @deprecated ICU 4.0
     * @hide original deprecated declaration
     */
    @Deprecated
    // This API was formerly @stable ICU 3.2
    public static ResourceBundle getDateFormatBundle(Calendar cal, ULocale locale)
        throws MissingResourceException {
        return null;
    }

    // -------- BEGIN ULocale boilerplate --------

    /**
     * Returns the locale that was used to create this object, or null.
     * This may may differ from the locale requested at the time of
     * this object's creation.  For example, if an object is created
     * for locale <tt>en_US_CALIFORNIA</tt>, the actual data may be
     * drawn from <tt>en</tt> (the <i>actual</i> locale), and
     * <tt>en_US</tt> may be the most specific locale that exists (the
     * <i>valid</i> locale).
     *
     * <p>Note: This method will be implemented in ICU 3.0; ICU 2.8
     * contains a partial preview implementation.  The * <i>actual</i>
     * locale is returned correctly, but the <i>valid</i> locale is
     * not, in most cases.
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
     * Sets information about the locales that were used to create this
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

    // -------- END ULocale boilerplate --------

    /**
     * 3.8 or older version did not have localized GMT format
     * patterns.
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
}
