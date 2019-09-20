/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2011-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SoftCache;
import android.icu.impl.TZDBTimeZoneNames;
import android.icu.impl.TextTrieMap;
import android.icu.impl.TimeZoneGenericNames;
import android.icu.impl.TimeZoneGenericNames.GenericMatchInfo;
import android.icu.impl.TimeZoneGenericNames.GenericNameType;
import android.icu.impl.TimeZoneNamesImpl;
import android.icu.impl.ZoneMeta;
import android.icu.lang.UCharacter;
import android.icu.text.TimeZoneNames.MatchInfo;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.Calendar;
import android.icu.util.Freezable;
import android.icu.util.Output;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.ULocale;

/**
 * <code>TimeZoneFormat</code> supports time zone display name formatting and parsing.
 * An instance of TimeZoneFormat works as a subformatter of {@link SimpleDateFormat},
 * but you can also directly get a new instance of <code>TimeZoneFormat</code> and
 * formatting/parsing time zone display names.
 * <p>
 * ICU implements the time zone display names defined by <a href="http://www.unicode.org/reports/tr35/">UTS#35
 * Unicode Locale Data Markup Language (LDML)</a>. {@link TimeZoneNames} represents the
 * time zone display name data model and this class implements the algorithm for actual
 * formatting and parsing.
 *
 * @see SimpleDateFormat
 * @see TimeZoneNames
 */
public class TimeZoneFormat extends UFormat implements Freezable<TimeZoneFormat>, Serializable {

    private static final long serialVersionUID = 2281246852693575022L;

    private static final int ISO_Z_STYLE_FLAG = 0x0080;
    private static final int ISO_LOCAL_STYLE_FLAG = 0x0100;

    /**
     * Time zone display format style enum used by format/parse APIs in <code>TimeZoneFormat</code>.
     *
     * @see TimeZoneFormat#format(Style, TimeZone, long)
     * @see TimeZoneFormat#format(Style, TimeZone, long, Output)
     * @see TimeZoneFormat#parse(Style, String, ParsePosition, Output)
     */
    public enum Style {
        /**
         * Generic location format, such as "United States Time (New York)" and "Italy Time".
         * This style is equivalent to the LDML date format pattern "VVVV".
         */
        GENERIC_LOCATION (0x0001),
        /**
         * Generic long non-location format, such as "Eastern Time".
         * This style is equivalent to the LDML date format pattern "vvvv".
         */
        GENERIC_LONG (0x0002),
        /**
         * Generic short non-location format, such as "ET".
         * This style is equivalent to the LDML date format pattern "v".
         */
        GENERIC_SHORT (0x0004),
        /**
         * Specific long format, such as "Eastern Standard Time".
         * This style is equivalent to the LDML date format pattern "zzzz".
         */
        SPECIFIC_LONG (0x0008),
        /**
         * Specific short format, such as "EST", "PDT".
         * This style is equivalent to the LDML date format pattern "z".
         */
        SPECIFIC_SHORT (0x0010),
        /**
         * Localized GMT offset format, such as "GMT-05:00", "UTC+0100"
         * This style is equivalent to the LDML date format pattern "OOOO" and "ZZZZ"
         */
        LOCALIZED_GMT (0x0020),
        /**
         * Short localized GMT offset format, such as "GMT-5", "UTC+1:30"
         * This style is equivalent to the LDML date format pattern "O".
         */
        LOCALIZED_GMT_SHORT (0x0040),
        /**
         * Short ISO 8601 local time difference (basic format) or the UTC indicator.
         * For example, "-05", "+0530", and "Z"(UTC).
         * This style is equivalent to the LDML date format pattern "X".
         */
        ISO_BASIC_SHORT (ISO_Z_STYLE_FLAG),
        /**
         * Short ISO 8601 locale time difference (basic format).
         * For example, "-05" and "+0530".
         * This style is equivalent to the LDML date format pattern "x".
         */
        ISO_BASIC_LOCAL_SHORT (ISO_LOCAL_STYLE_FLAG),
        /**
         * Fixed width ISO 8601 local time difference (basic format) or the UTC indicator.
         * For example, "-0500", "+0530", and "Z"(UTC).
         * This style is equivalent to the LDML date format pattern "XX".
         */
        ISO_BASIC_FIXED (ISO_Z_STYLE_FLAG),
        /**
         * Fixed width ISO 8601 local time difference (basic format).
         * For example, "-0500" and "+0530".
         * This style is equivalent to the LDML date format pattern "xx".
         */
        ISO_BASIC_LOCAL_FIXED (ISO_LOCAL_STYLE_FLAG),
        /**
         * ISO 8601 local time difference (basic format) with optional seconds field, or the UTC indicator.
         * For example, "-0500", "+052538", and "Z"(UTC).
         * This style is equivalent to the LDML date format pattern "XXXX".
         */
        ISO_BASIC_FULL (ISO_Z_STYLE_FLAG),
        /**
         * ISO 8601 local time difference (basic format) with optional seconds field.
         * For example, "-0500" and "+052538".
         * This style is equivalent to the LDML date format pattern "xxxx".
         */
        ISO_BASIC_LOCAL_FULL (ISO_LOCAL_STYLE_FLAG),
        /**
         * Fixed width ISO 8601 local time difference (extended format) or the UTC indicator.
         * For example, "-05:00", "+05:30", and "Z"(UTC).
         * This style is equivalent to the LDML date format pattern "XXX".
         */
        ISO_EXTENDED_FIXED (ISO_Z_STYLE_FLAG),
        /**
         * Fixed width ISO 8601 local time difference (extended format).
         * For example, "-05:00" and "+05:30".
         * This style is equivalent to the LDML date format pattern "xxx" and "ZZZZZ".
         */
        ISO_EXTENDED_LOCAL_FIXED (ISO_LOCAL_STYLE_FLAG),
        /**
         * ISO 8601 local time difference (extended format) with optional seconds field, or the UTC indicator.
         * For example, "-05:00", "+05:25:38", and "Z"(UTC).
         * This style is equivalent to the LDML date format pattern "XXXXX".
         */
        ISO_EXTENDED_FULL (ISO_Z_STYLE_FLAG),
        /**
         * ISO 8601 local time difference (extended format) with optional seconds field.
         * For example, "-05:00" and "+05:25:38".
         * This style is equivalent to the LDML date format pattern "xxxxx".
         */
        ISO_EXTENDED_LOCAL_FULL (ISO_LOCAL_STYLE_FLAG),
        /**
         * Time Zone ID, such as "America/Los_Angeles".
         */
        ZONE_ID (0x0200),
        /**
         * Short Time Zone ID (BCP 47 Unicode location extension, time zone type value), such as "uslax".
         */
        ZONE_ID_SHORT (0x0400),
        /**
         * Exemplar location, such as "Los Angeles" and "Paris".
         */
        EXEMPLAR_LOCATION (0x0800);

        final int flag;

        private Style(int flag) {
            this.flag = flag;
        }
    }

    /**
     * Offset pattern type enum.
     *
     * @see TimeZoneFormat#getGMTOffsetPattern(GMTOffsetPatternType)
     * @see TimeZoneFormat#setGMTOffsetPattern(GMTOffsetPatternType, String)
     */
    public enum GMTOffsetPatternType {
        /**
         * Positive offset with hours and minutes fields
         */
        POSITIVE_HM ("+H:mm", "Hm", true),
        /**
         * Positive offset with hours, minutes and seconds fields
         */
        POSITIVE_HMS ("+H:mm:ss", "Hms", true),
        /**
         * Negative offset with hours and minutes fields
         */
        NEGATIVE_HM ("-H:mm", "Hm", false),
        /**
         * Negative offset with hours, minutes and seconds fields
         */
        NEGATIVE_HMS ("-H:mm:ss", "Hms", false),
        /**
         * Positive offset with hours field
         */
        POSITIVE_H ("+H", "H", true),
        /**
         * Negative offset with hours field
         */
        NEGATIVE_H ("-H", "H", false);

        private String _defaultPattern;
        private String _required;
        private boolean _isPositive;

        private GMTOffsetPatternType(String defaultPattern, String required, boolean isPositive) {
            _defaultPattern = defaultPattern;
            _required = required;
            _isPositive = isPositive;
        }

        private String defaultPattern() {
            return _defaultPattern;
        }

        private String required() {
            return _required;
        }

        private boolean isPositive() {
            return _isPositive;
        }
    }

    /**
     * Time type enum used for receiving time type (standard time, daylight time or unknown)
     * in <code>TimeZoneFormat</code> APIs.
     */
    public enum TimeType {
        /**
         * Unknown
         */
        UNKNOWN,
        /**
         * Standard time
         */
        STANDARD,
        /**
         * Daylight saving time
         */
        DAYLIGHT;
    }

    /**
     * Parse option enum, used for specifying optional parse behavior.
     */
    public enum ParseOption {
        /**
         * When a time zone display name is not found within a set of display names
         * used for the specified style, look for the name from display names used
         * by other styles.
         */
        ALL_STYLES,
        /**
         * When parsing a time zone display name in {@link Style#SPECIFIC_SHORT},
         * look for the IANA tz database compatible zone abbreviations in addition
         * to the localized names coming from the {@link TimeZoneNames} currently
         * used by the {@link TimeZoneFormat}.
         */
        TZ_DATABASE_ABBREVIATIONS;
    }

    /*
     * fields to be serialized
     */
    private ULocale _locale;
    private TimeZoneNames _tznames;
    private String _gmtPattern;
    private String[] _gmtOffsetPatterns;
    private String[] _gmtOffsetDigits;
    private String _gmtZeroFormat;
    private boolean _parseAllStyles;
    private boolean _parseTZDBNames;

    /*
     * Transient fields
     */
    private transient volatile TimeZoneGenericNames _gnames;

    private transient String _gmtPatternPrefix;
    private transient String _gmtPatternSuffix;
    private transient Object[][] _gmtOffsetPatternItems;
    // cache if offset hours and minutes are abutting
    private transient boolean _abuttingOffsetHoursAndMinutes;

    private transient String _region;

    private volatile transient boolean _frozen;

    private transient volatile TimeZoneNames _tzdbNames;

    /*
     * Static final fields
     */
    private static final String TZID_GMT = "Etc/GMT"; // canonical tzid for GMT

    private static final String[] ALT_GMT_STRINGS = {"GMT", "UTC", "UT"};

    private static final String DEFAULT_GMT_PATTERN = "GMT{0}";
    private static final String DEFAULT_GMT_ZERO = "GMT";
    private static final String[] DEFAULT_GMT_DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private static final char DEFAULT_GMT_OFFSET_SEP = ':';
    private static final String ASCII_DIGITS = "0123456789";
    private static final String ISO8601_UTC = "Z";

    private static final String UNKNOWN_ZONE_ID = "Etc/Unknown";
    private static final String UNKNOWN_SHORT_ZONE_ID = "unk";
    private static final String UNKNOWN_LOCATION = "Unknown";

    // Order of GMT offset pattern parsing, *_HMS must be evaluated first
    // because *_HM is most likely a substring of *_HMS
    private static final GMTOffsetPatternType[] PARSE_GMT_OFFSET_TYPES = {
        GMTOffsetPatternType.POSITIVE_HMS, GMTOffsetPatternType.NEGATIVE_HMS,
        GMTOffsetPatternType.POSITIVE_HM, GMTOffsetPatternType.NEGATIVE_HM,
        GMTOffsetPatternType.POSITIVE_H, GMTOffsetPatternType.NEGATIVE_H,
    };

    private static final int MILLIS_PER_HOUR = 60 * 60 * 1000;
    private static final int MILLIS_PER_MINUTE = 60 * 1000;
    private static final int MILLIS_PER_SECOND = 1000;

    // Maximum offset (exclusive) in millisecond supported by offset formats
    private static final int MAX_OFFSET = 24 * MILLIS_PER_HOUR;

    // Maximum values for GMT offset fields
    private static final int MAX_OFFSET_HOUR = 23;
    private static final int MAX_OFFSET_MINUTE = 59;
    private static final int MAX_OFFSET_SECOND = 59;

    private static final int UNKNOWN_OFFSET = Integer.MAX_VALUE;

    private static TimeZoneFormatCache _tzfCache = new TimeZoneFormatCache();

    // The filter used for searching all specific names and exemplar location names
    private static final EnumSet<NameType> ALL_SIMPLE_NAME_TYPES = EnumSet.of(
        NameType.LONG_STANDARD, NameType.LONG_DAYLIGHT,
        NameType.SHORT_STANDARD, NameType.SHORT_DAYLIGHT,
        NameType.EXEMPLAR_LOCATION
    );

    // The filter used for searching all generic names
    private static final EnumSet<GenericNameType> ALL_GENERIC_NAME_TYPES = EnumSet.of(
        GenericNameType.LOCATION, GenericNameType.LONG, GenericNameType.SHORT
    );

    private static volatile TextTrieMap<String> ZONE_ID_TRIE;
    private static volatile TextTrieMap<String> SHORT_ZONE_ID_TRIE;

    /**
     * The protected constructor for subclassing.
     * @param locale the locale
     */
    protected TimeZoneFormat(ULocale locale) {
        _locale = locale;
        _tznames = TimeZoneNames.getInstance(locale);
        // TimeZoneGenericNames _gnames will be instantiated lazily

        String gmtPattern = null;
        String hourFormats = null;
        _gmtZeroFormat = DEFAULT_GMT_ZERO;

        try {
            ICUResourceBundle bundle = (ICUResourceBundle) ICUResourceBundle.getBundleInstance(
                    ICUData.ICU_ZONE_BASE_NAME, locale);
            try {
                gmtPattern = bundle.getStringWithFallback("zoneStrings/gmtFormat");
            } catch (MissingResourceException e) {
                // fall through
            }
            try {
                hourFormats = bundle.getStringWithFallback("zoneStrings/hourFormat");
            } catch (MissingResourceException e) {
                // fall through
            }
            try {
                _gmtZeroFormat = bundle.getStringWithFallback("zoneStrings/gmtZeroFormat");
            } catch (MissingResourceException e) {
                // fall through
            }
        } catch (MissingResourceException e) {
            // fall through
        }

        if (gmtPattern == null) {
            gmtPattern = DEFAULT_GMT_PATTERN;
        }
        initGMTPattern(gmtPattern);

        String[] gmtOffsetPatterns = new String[GMTOffsetPatternType.values().length];
        if (hourFormats != null) {
            String[] hourPatterns = hourFormats.split(";", 2);
            gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_H.ordinal()] = truncateOffsetPattern(hourPatterns[0]);
            gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HM.ordinal()] = hourPatterns[0];
            gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HMS.ordinal()] = expandOffsetPattern(hourPatterns[0]);
            gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_H.ordinal()] = truncateOffsetPattern(hourPatterns[1]);
            gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HM.ordinal()] = hourPatterns[1];
            gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HMS.ordinal()] = expandOffsetPattern(hourPatterns[1]);
        } else {
            for (GMTOffsetPatternType patType : GMTOffsetPatternType.values()) {
                gmtOffsetPatterns[patType.ordinal()] = patType.defaultPattern();
            }
        }
        initGMTOffsetPatterns(gmtOffsetPatterns);

        _gmtOffsetDigits = DEFAULT_GMT_DIGITS;
        NumberingSystem ns = NumberingSystem.getInstance(locale);
        if (!ns.isAlgorithmic()) {
            // we do not support algorithmic numbering system for GMT offset for now
            _gmtOffsetDigits = toCodePoints(ns.getDescription());
        }
    }

    /**
     * Returns a frozen instance of <code>TimeZoneFormat</code> for the given locale.
     * <p><b>Note</b>: The instance returned by this method is frozen. If you want to
     * customize a TimeZoneFormat, you must use {@link #cloneAsThawed()} to get a
     * thawed copy first.
     *
     * @param locale the locale.
     * @return a frozen instance of <code>TimeZoneFormat</code> for the given locale.
     */
    public static TimeZoneFormat getInstance(ULocale locale) {
        if (locale == null) {
            throw new NullPointerException("locale is null");
        }
        return _tzfCache.getInstance(locale, locale);
    }

    /**
     * Returns a frozen instance of <code>TimeZoneFormat</code> for the given
     * {@link java.util.Locale}.
     * <p><b>Note</b>: The instance returned by this method is frozen. If you want to
     * customize a TimeZoneFormat, you must use {@link #cloneAsThawed()} to get a
     * thawed copy first.
     *
     * @param locale the {@link Locale}.
     * @return a frozen instance of <code>TimeZoneFormat</code> for the given locale.
     */
    public static TimeZoneFormat getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    /**
     * Returns the time zone display name data used by this instance.
     *
     * @return the time zone display name data.
     * @see #setTimeZoneNames(TimeZoneNames)
     */
    public TimeZoneNames getTimeZoneNames() {
        return _tznames;
    }

    /**
     * Private method returning the instance of TimeZoneGenericNames
     * used by this object. The instance of TimeZoneGenericNames might
     * not be available until the first use (lazy instantiation) because
     * it is only required for handling generic names (that are not used
     * by DateFormat's default patterns) and it requires relatively heavy
     * one time initialization.
     * @return the instance of TimeZoneGenericNames used by this object.
     */
    private TimeZoneGenericNames getTimeZoneGenericNames() {
        if (_gnames == null) { // _gnames is volatile
            synchronized(this) {
                if (_gnames == null) {
                    _gnames = TimeZoneGenericNames.getInstance(_locale);
                }
            }
        }
        return _gnames;
    }

    /**
     * Private method returning the instance of TZDBTimeZoneNames.
     * The instance if used only for parsing when {@link ParseOption#TZ_DATABASE_ABBREVIATIONS}
     * is enabled.
     * @return an instance of TZDBTimeZoneNames.
     */
    private TimeZoneNames getTZDBTimeZoneNames() {
        if (_tzdbNames == null) {
            synchronized(this) {
                if (_tzdbNames == null) {
                    _tzdbNames = new TZDBTimeZoneNames(_locale);
                }
            }
        }
        return _tzdbNames;
    }

    /**
     * Sets the time zone display name data to this instance.
     *
     * @param tznames the time zone display name data.
     * @return this object.
     * @throws UnsupportedOperationException when this object is frozen.
     * @see #getTimeZoneNames()
     */
    public TimeZoneFormat setTimeZoneNames(TimeZoneNames tznames) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
       _tznames = tznames;
       // TimeZoneGenericNames must be changed to utilize the new TimeZoneNames instance.
       _gnames = new TimeZoneGenericNames(_locale, _tznames);
       return this;
    }

    /**
     * Returns the localized GMT format pattern.
     *
     * @return the localized GMT format pattern.
     * @see #setGMTPattern(String)
     */
    public String getGMTPattern() {
        return _gmtPattern;
    }

    /**
     * Sets the localized GMT format pattern. The pattern must contain
     * a single argument {0}, for example "GMT {0}".
     *
     * @param pattern the localized GMT format pattern string
     * @return this object.
     * @throws IllegalArgumentException when the pattern string does not contain "{0}"
     * @throws UnsupportedOperationException when this object is frozen.
     * @see #getGMTPattern()
     */
    public TimeZoneFormat setGMTPattern(String pattern) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        initGMTPattern(pattern);
        return this;
    }

    /**
     * Returns the offset pattern used for localized GMT format.
     *
     * @param type the offset pattern enum
     * @see #setGMTOffsetPattern(GMTOffsetPatternType, String)
     */
    public String getGMTOffsetPattern(GMTOffsetPatternType type) {
        return _gmtOffsetPatterns[type.ordinal()];
    }

    /**
     * Sets the offset pattern for the given offset type.
     *
     * @param type the offset pattern.
     * @param pattern the pattern string.
     * @return this object.
     * @throws IllegalArgumentException when the pattern string does not have required time field letters.
     * @throws UnsupportedOperationException when this object is frozen.
     * @see #getGMTOffsetPattern(GMTOffsetPatternType)
     */
    public TimeZoneFormat setGMTOffsetPattern(GMTOffsetPatternType type, String pattern) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        if (pattern == null) {
            throw new NullPointerException("Null GMT offset pattern");
        }

        Object[] parsedItems = parseOffsetPattern(pattern, type.required());

        _gmtOffsetPatterns[type.ordinal()] = pattern;
        _gmtOffsetPatternItems[type.ordinal()] = parsedItems;
        checkAbuttingHoursAndMinutes();

        return this;
    }

    /**
     * Returns the decimal digit characters used for localized GMT format in a single string
     * containing from 0 to 9 in the ascending order.
     *
     * @return the decimal digits for localized GMT format.
     * @see #setGMTOffsetDigits(String)
     */
    public String getGMTOffsetDigits() {
        StringBuilder buf = new StringBuilder(_gmtOffsetDigits.length);
        for (String digit : _gmtOffsetDigits) {
            buf.append(digit);
        }
        return buf.toString();
    }

    /**
     * Sets the decimal digit characters used for localized GMT format.
     *
     * @param digits a string contains the decimal digit characters from 0 to 9 n the ascending order.
     * @return this object.
     * @throws IllegalArgumentException when the string did not contain ten characters.
     * @throws UnsupportedOperationException when this object is frozen.
     * @see #getGMTOffsetDigits()
     */
    public TimeZoneFormat setGMTOffsetDigits(String digits) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        if (digits == null) {
            throw new NullPointerException("Null GMT offset digits");
        }
        String[] digitArray = toCodePoints(digits);
        if (digitArray.length != 10) {
            throw new IllegalArgumentException("Length of digits must be 10");
        }
        _gmtOffsetDigits = digitArray;
        return this;
    }

    /**
     * Returns the localized GMT format string for GMT(UTC) itself (GMT offset is 0).
     *
     * @return the localized GMT string string for GMT(UTC) itself.
     * @see #setGMTZeroFormat(String)
     */
    public String getGMTZeroFormat() {
        return _gmtZeroFormat;
    }

    /**
     * Sets the localized GMT format string for GMT(UTC) itself (GMT offset is 0).
     *
     * @param gmtZeroFormat the localized GMT format string for GMT(UTC).
     * @return this object.
     * @throws UnsupportedOperationException when this object is frozen.
     * @see #getGMTZeroFormat()
     */
    public TimeZoneFormat setGMTZeroFormat(String gmtZeroFormat) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        if (gmtZeroFormat == null) {
            throw new NullPointerException("Null GMT zero format");
        }
        if (gmtZeroFormat.length() == 0) {
            throw new IllegalArgumentException("Empty GMT zero format");
        }
        _gmtZeroFormat = gmtZeroFormat;
        return this;
    }

    /**
     * Sets the default parse options.
     * <p>
     * <b>Note:</b> By default, an instance of <code>TimeZoneFormat</code>
     * created by {@link #getInstance(ULocale)} has no parse options set.
     *
     * @param options the default parse options.
     * @return this object.
     * @see ParseOption
     */
    public TimeZoneFormat setDefaultParseOptions(EnumSet<ParseOption> options) {
        _parseAllStyles = options.contains(ParseOption.ALL_STYLES);
        _parseTZDBNames = options.contains(ParseOption.TZ_DATABASE_ABBREVIATIONS);
        return this;
    }

    /**
     * Returns the default parse options used by this <code>TimeZoneFormat</code> instance.
     * @return the default parse options.
     * @see ParseOption
     */
    public EnumSet<ParseOption> getDefaultParseOptions() {
        if (_parseAllStyles && _parseTZDBNames) {
            return EnumSet.of(ParseOption.ALL_STYLES, ParseOption.TZ_DATABASE_ABBREVIATIONS);
        } else if (_parseAllStyles) {
            return EnumSet.of(ParseOption.ALL_STYLES);
        } else if (_parseTZDBNames) {
            return EnumSet.of(ParseOption.TZ_DATABASE_ABBREVIATIONS);
        }
        return EnumSet.noneOf(ParseOption.class);
    }

    /**
     * Returns the ISO 8601 basic time zone string for the given offset.
     * For example, "-08", "-0830" and "Z"
     *
     * @param offset the offset from GMT(UTC) in milliseconds.
     * @param useUtcIndicator true if ISO 8601 UTC indicator "Z" is used when the offset is 0.
     * @param isShort true if shortest form is used.
     * @param ignoreSeconds true if non-zero offset seconds is appended.
     * @return the ISO 8601 basic format.
     * @throws IllegalArgumentException if the specified offset is out of supported range
     * (-24 hours &lt; offset &lt; +24 hours).
     * @see #formatOffsetISO8601Extended(int, boolean, boolean, boolean)
     * @see #parseOffsetISO8601(String, ParsePosition)
     */
    public final String formatOffsetISO8601Basic(int offset, boolean useUtcIndicator, boolean isShort, boolean ignoreSeconds) {
        return formatOffsetISO8601(offset, true, useUtcIndicator, isShort, ignoreSeconds);
    }

    /**
     * Returns the ISO 8601 extended time zone string for the given offset.
     * For example, "-08:00", "-08:30" and "Z"
     *
     * @param offset the offset from GMT(UTC) in milliseconds.
     * @param useUtcIndicator true if ISO 8601 UTC indicator "Z" is used when the offset is 0.
     * @param isShort true if shortest form is used.
     * @param ignoreSeconds true if non-zero offset seconds is appended.
     * @return the ISO 8601 extended format.
     * @throws IllegalArgumentException if the specified offset is out of supported range
     * (-24 hours &lt; offset &lt; +24 hours).
     * @see #formatOffsetISO8601Basic(int, boolean, boolean, boolean)
     * @see #parseOffsetISO8601(String, ParsePosition)
     */
    public final String formatOffsetISO8601Extended(int offset, boolean useUtcIndicator, boolean isShort, boolean ignoreSeconds) {
        return formatOffsetISO8601(offset, false, useUtcIndicator, isShort, ignoreSeconds);
    }

    /**
     * Returns the localized GMT(UTC) offset format for the given offset.
     * The localized GMT offset is defined by;
     * <ul>
     * <li>GMT format pattern (e.g. "GMT {0}" - see {@link #getGMTPattern()})
     * <li>Offset time pattern (e.g. "+HH:mm" - see {@link #getGMTOffsetPattern(GMTOffsetPatternType)})
     * <li>Offset digits (e.g. "0123456789" - see {@link #getGMTOffsetDigits()})
     * <li>GMT zero format (e.g. "GMT" - see {@link #getGMTZeroFormat()})
     * </ul>
     * This format always uses 2 digit hours and minutes. When the given offset has non-zero
     * seconds, 2 digit seconds field will be appended. For example,
     * GMT+05:00 and GMT+05:28:06.
     * @param offset the offset from GMT(UTC) in milliseconds.
     * @return the localized GMT format string
     * @see #parseOffsetLocalizedGMT(String, ParsePosition)
     * @throws IllegalArgumentException if the specified offset is out of supported range
     * (-24 hours &lt; offset &lt; +24 hours).
     */
    public String formatOffsetLocalizedGMT(int offset) {
        return formatOffsetLocalizedGMT(offset, false);
    }

    /**
     * Returns the short localized GMT(UTC) offset format for the given offset.
     * The short localized GMT offset is defined by;
     * <ul>
     * <li>GMT format pattern (e.g. "GMT {0}" - see {@link #getGMTPattern()})
     * <li>Offset time pattern (e.g. "+HH:mm" - see {@link #getGMTOffsetPattern(GMTOffsetPatternType)})
     * <li>Offset digits (e.g. "0123456789" - see {@link #getGMTOffsetDigits()})
     * <li>GMT zero format (e.g. "GMT" - see {@link #getGMTZeroFormat()})
     * </ul>
     * This format uses the shortest representation of offset. The hours field does not
     * have leading zero and lower fields with zero will be truncated. For example,
     * GMT+5 and GMT+530.
     * @param offset the offset from GMT(UTC) in milliseconds.
     * @return the short localized GMT format string
     * @see #parseOffsetLocalizedGMT(String, ParsePosition)
     * @throws IllegalArgumentException if the specified offset is out of supported range
     * (-24 hours &lt; offset &lt; +24 hours).
     */
    public String formatOffsetShortLocalizedGMT(int offset) {
        return formatOffsetLocalizedGMT(offset, true);
    }

    /**
     * Returns the display name of the time zone at the given date for
     * the style.
     *
     * <p><b>Note</b>: A style may have fallback styles defined. For example,
     * when <code>GENERIC_LONG</code> is requested, but there is no display name
     * data available for <code>GENERIC_LONG</code> style, the implementation
     * may use <code>GENERIC_LOCATION</code> or <code>LOCALIZED_GMT</code>.
     * See UTS#35 UNICODE LOCALE DATA MARKUP LANGUAGE (LDML)
     * <a href="http://www.unicode.org/reports/tr35/#Time_Zone_Fallback">Appendix J: Time Zone Display Name</a>
     * for the details.
     *
     * @param style the style enum (e.g. <code>GENERIC_LONG</code>, <code>LOCALIZED_GMT</code>...)
     * @param tz the time zone.
     * @param date the date.
     * @return the display name of the time zone.
     * @see Style
     * @see #format(Style, TimeZone, long, Output)
     */
    public final String format(Style style, TimeZone tz, long date) {
        return format(style, tz, date, null);
    }

    /**
     * Returns the display name of the time zone at the given date for
     * the style. This method takes an extra argument <code>Output&lt;TimeType&gt; timeType</code>
     * in addition to the argument list of {@link #format(Style, TimeZone, long)}.
     * The argument is used for receiving the time type (standard time
     * or daylight saving time, or unknown) actually used for the display name.
     *
     * @param style the style enum (e.g. <code>GENERIC_LONG</code>, <code>LOCALIZED_GMT</code>...)
     * @param tz the time zone.
     * @param date the date.
     * @param timeType the output argument for receiving the time type (standard/daylight/unknown)
     * used for the display name, or specify null if the information is not necessary.
     * @return the display name of the time zone.
     * @see Style
     * @see #format(Style, TimeZone, long)
     */
    public String format(Style style, TimeZone tz, long date, Output<TimeType> timeType) {
        String result = null;

        if (timeType != null) {
            timeType.value = TimeType.UNKNOWN;
        }

        boolean noOffsetFormatFallback = false;

        switch (style) {
        case GENERIC_LOCATION:
            result = getTimeZoneGenericNames().getGenericLocationName(ZoneMeta.getCanonicalCLDRID(tz));
            break;
        case GENERIC_LONG:
            result = getTimeZoneGenericNames().getDisplayName(tz, GenericNameType.LONG, date);
            break;
        case GENERIC_SHORT:
            result = getTimeZoneGenericNames().getDisplayName(tz, GenericNameType.SHORT, date);
            break;
        case SPECIFIC_LONG:
            result = formatSpecific(tz, NameType.LONG_STANDARD, NameType.LONG_DAYLIGHT, date, timeType);
            break;
        case SPECIFIC_SHORT:
            result = formatSpecific(tz, NameType.SHORT_STANDARD, NameType.SHORT_DAYLIGHT, date, timeType);
            break;

        case ZONE_ID:
            result = tz.getID();
            noOffsetFormatFallback = true;
            break;
        case ZONE_ID_SHORT:
            result = ZoneMeta.getShortID(tz);
            if (result == null) {
                result = UNKNOWN_SHORT_ZONE_ID;
            }
            noOffsetFormatFallback = true;
            break;
        case EXEMPLAR_LOCATION:
            result = formatExemplarLocation(tz);
            noOffsetFormatFallback = true;
            break;

        default:
            // will be handled below
            break;
        }

        if (result == null && !noOffsetFormatFallback) {
            int[] offsets = {0, 0};
            tz.getOffset(date, false, offsets);
            int offset = offsets[0] + offsets[1];

            switch (style) {
            case GENERIC_LOCATION:
            case GENERIC_LONG:
            case SPECIFIC_LONG:
            case LOCALIZED_GMT:
                result = formatOffsetLocalizedGMT(offset);
                break;

            case GENERIC_SHORT:
            case SPECIFIC_SHORT:
            case LOCALIZED_GMT_SHORT:
                result = formatOffsetShortLocalizedGMT(offset);
                break;

            case ISO_BASIC_SHORT:
                result = formatOffsetISO8601Basic(offset, true, true, true);
                break;

            case ISO_BASIC_LOCAL_SHORT:
                result = formatOffsetISO8601Basic(offset, false, true, true);
                break;

            case ISO_BASIC_FIXED:
                result = formatOffsetISO8601Basic(offset, true, false, true);
                break;

            case ISO_BASIC_LOCAL_FIXED:
                result = formatOffsetISO8601Basic(offset, false, false, true);
                break;

            case ISO_BASIC_FULL:
                result = formatOffsetISO8601Basic(offset, true, false, false);
                break;

            case ISO_BASIC_LOCAL_FULL:
                result = formatOffsetISO8601Basic(offset, false, false, false);
                break;

            case ISO_EXTENDED_FIXED:
                result = formatOffsetISO8601Extended(offset, true, false, true);
                break;

            case ISO_EXTENDED_LOCAL_FIXED:
                result = formatOffsetISO8601Extended(offset, false, false, true);
                break;

            case ISO_EXTENDED_FULL:
                result = formatOffsetISO8601Extended(offset, true, false, false);
                break;

            case ISO_EXTENDED_LOCAL_FULL:
                result = formatOffsetISO8601Extended(offset, false, false, false);
                break;

            default:
                // Other cases are handled earlier and never comes into this
                // switch statement.
                assert false;
                break;
            }
            // time type
            if (timeType != null) {
                timeType.value = (offsets[1] != 0) ? TimeType.DAYLIGHT : TimeType.STANDARD;
            }
        }

        assert(result != null);

        return result;
    }

    /**
     * Returns offset from GMT(UTC) in milliseconds for the given ISO 8601
     * basic or extended time zone string. When the given string is not an ISO 8601 time
     * zone string, this method sets the current position as the error index
     * to <code>ParsePosition pos</code> and returns 0.
     *
     * @param text the text contains ISO 8601 style time zone string (e.g. "-08", "-0800", "-08:00", and "Z")
     * at the position.
     * @param pos the position.
     * @return the offset from GMT(UTC) in milliseconds for the given ISO 8601 style
     * time zone string.
     * @see #formatOffsetISO8601Basic(int, boolean, boolean, boolean)
     * @see #formatOffsetISO8601Extended(int, boolean, boolean, boolean)
     */
    public final int parseOffsetISO8601(String text, ParsePosition pos) {
        return parseOffsetISO8601(text, pos, false, null);
    }

    /**
     * Returns offset from GMT(UTC) in milliseconds for the given localized GMT
     * offset format string. When the given string cannot be parsed, this method
     * sets the current position as the error index to <code>ParsePosition pos</code>
     * and returns 0.
     *
     * @param text the text contains a localized GMT offset string at the position.
     * @param pos the position.
     * @return the offset from GMT(UTC) in milliseconds for the given localized GMT
     * offset format string.
     * @see #formatOffsetLocalizedGMT(int)
     */
    public int parseOffsetLocalizedGMT(String text, ParsePosition pos) {
        return parseOffsetLocalizedGMT(text, pos, false, null);
    }

    /**
     * Returns offset from GMT(UTC) in milliseconds for the given short localized GMT
     * offset format string. When the given string cannot be parsed, this method
     * sets the current position as the error index to <code>ParsePosition pos</code>
     * and returns 0.
     *
     * @param text the text contains a short localized GMT offset string at the position.
     * @param pos the position.
     * @return the offset from GMT(UTC) in milliseconds for the given short localized GMT
     * offset format string.
     * @see #formatOffsetShortLocalizedGMT(int)
     */
    public int parseOffsetShortLocalizedGMT(String text, ParsePosition pos) {
        return parseOffsetLocalizedGMT(text, pos, true, null);
    }

    /**
     * Returns a <code>TimeZone</code> by parsing the time zone string according to
     * the parse position, the style and the parse options.
     *
     * @param text the text contains a time zone string at the position.
     * @param style the format style.
     * @param pos the position.
     * @param options the parse options.
     * @param timeType The output argument for receiving the time type (standard/daylight/unknown),
     * or specify null if the information is not necessary.
     * @return A <code>TimeZone</code>, or null if the input could not be parsed.
     * @see Style
     * @see #format(Style, TimeZone, long, Output)
     */
    public TimeZone parse(Style style, String text, ParsePosition pos, EnumSet<ParseOption> options, Output<TimeType> timeType) {
        if (timeType == null) {
            timeType = new Output<TimeType>(TimeType.UNKNOWN);
        } else {
            timeType.value = TimeType.UNKNOWN;
        }

        int startIdx = pos.getIndex();
        int maxPos = text.length();
        int offset;

        // Styles using localized GMT format as fallback
        boolean fallbackLocalizedGMT =
                (style == Style.SPECIFIC_LONG || style == Style.GENERIC_LONG || style == Style.GENERIC_LOCATION);
        boolean fallbackShortLocalizedGMT =
                (style == Style.SPECIFIC_SHORT || style == Style.GENERIC_SHORT);

        int evaluated = 0;  // bit flags representing already evaluated styles
        ParsePosition tmpPos = new ParsePosition(startIdx);

        int parsedOffset = UNKNOWN_OFFSET;  // stores successfully parsed offset for later use
        int parsedPos = -1;                 // stores successfully parsed offset position for later use

        // Try localized GMT format first if necessary
        if (fallbackLocalizedGMT || fallbackShortLocalizedGMT) {
            Output<Boolean> hasDigitOffset = new Output<Boolean>(false);
            offset = parseOffsetLocalizedGMT(text, tmpPos, fallbackShortLocalizedGMT, hasDigitOffset);
            if (tmpPos.getErrorIndex() == -1) {
                // Even when the input text was successfully parsed as a localized GMT format text,
                // we may still need to evaluate the specified style if -
                //   1) GMT zero format was used, and
                //   2) The input text was not completely processed
                if (tmpPos.getIndex() == maxPos || hasDigitOffset.value) {
                    pos.setIndex(tmpPos.getIndex());
                    return getTimeZoneForOffset(offset);
                }
                parsedOffset = offset;
                parsedPos = tmpPos.getIndex();
            }
            // Note: For now, no distinction between long/short localized GMT format in the parser.
            // This might be changed in future.
//            evaluated |= (fallbackLocalizedGMT ? Style.LOCALIZED_GMT.flag : Style.LOCALIZED_GMT_SHORT.flag);
            evaluated |= (Style.LOCALIZED_GMT.flag | Style.LOCALIZED_GMT_SHORT.flag);
        }

        boolean parseTZDBAbbrev = (options == null) ?
                getDefaultParseOptions().contains(ParseOption.TZ_DATABASE_ABBREVIATIONS)
                : options.contains(ParseOption.TZ_DATABASE_ABBREVIATIONS);

        // Try the specified style
        switch (style) {
            case LOCALIZED_GMT:
            {
                tmpPos.setIndex(startIdx);
                tmpPos.setErrorIndex(-1);

                offset = parseOffsetLocalizedGMT(text, tmpPos);
                if (tmpPos.getErrorIndex() == -1) {
                    pos.setIndex(tmpPos.getIndex());
                    return getTimeZoneForOffset(offset);
                }
                // Note: For now, no distinction between long/short localized GMT format in the parser.
                // This might be changed in future.
                evaluated |= Style.LOCALIZED_GMT_SHORT.flag;
                break;
            }
            case LOCALIZED_GMT_SHORT:
            {
                tmpPos.setIndex(startIdx);
                tmpPos.setErrorIndex(-1);

                offset = parseOffsetShortLocalizedGMT(text, tmpPos);
                if (tmpPos.getErrorIndex() == -1) {
                    pos.setIndex(tmpPos.getIndex());
                    return getTimeZoneForOffset(offset);
                }
                // Note: For now, no distinction between long/short localized GMT format in the parser.
                // This might be changed in future.
                evaluated |= Style.LOCALIZED_GMT.flag;
                break;
            }

            case ISO_BASIC_SHORT:
            case ISO_BASIC_FIXED:
            case ISO_BASIC_FULL:
            case ISO_EXTENDED_FIXED:
            case ISO_EXTENDED_FULL:
            {
                tmpPos.setIndex(startIdx);
                tmpPos.setErrorIndex(-1);

                offset = parseOffsetISO8601(text, tmpPos);
                if (tmpPos.getErrorIndex() == -1) {
                    pos.setIndex(tmpPos.getIndex());
                    return getTimeZoneForOffset(offset);
                }
                break;
            }

            case ISO_BASIC_LOCAL_SHORT:
            case ISO_BASIC_LOCAL_FIXED:
            case ISO_BASIC_LOCAL_FULL:
            case ISO_EXTENDED_LOCAL_FIXED:
            case ISO_EXTENDED_LOCAL_FULL:
            {
                tmpPos.setIndex(startIdx);
                tmpPos.setErrorIndex(-1);

                // Exclude the case of UTC Indicator "Z" here
                Output<Boolean> hasDigitOffset = new Output<Boolean>(false);
                offset = parseOffsetISO8601(text, tmpPos, false, hasDigitOffset);
                if (tmpPos.getErrorIndex() == -1 && hasDigitOffset.value) {
                    pos.setIndex(tmpPos.getIndex());
                    return getTimeZoneForOffset(offset);
                }
                break;
            }

            case SPECIFIC_LONG:
            case SPECIFIC_SHORT:
            {
                // Specific styles
                EnumSet<NameType> nameTypes = null;
                if (style == Style.SPECIFIC_LONG) {
                    nameTypes = EnumSet.of(NameType.LONG_STANDARD, NameType.LONG_DAYLIGHT);
                } else {
                    assert style == Style.SPECIFIC_SHORT;
                    nameTypes = EnumSet.of(NameType.SHORT_STANDARD, NameType.SHORT_DAYLIGHT);
                }
                Collection<MatchInfo> specificMatches = _tznames.find(text, startIdx, nameTypes);
                if (specificMatches != null) {
                    MatchInfo specificMatch = null;
                    for (MatchInfo match : specificMatches) {
                        if (startIdx + match.matchLength() > parsedPos) {
                            specificMatch = match;
                            parsedPos = startIdx + match.matchLength();
                        }
                    }
                    if (specificMatch != null) {
                        timeType.value = getTimeType(specificMatch.nameType());
                        pos.setIndex(parsedPos);
                        return TimeZone.getTimeZone(getTimeZoneID(specificMatch.tzID(), specificMatch.mzID()));
                    }
                }

                if (parseTZDBAbbrev && style == Style.SPECIFIC_SHORT) {
                    assert nameTypes.contains(NameType.SHORT_STANDARD);
                    assert nameTypes.contains(NameType.SHORT_DAYLIGHT);

                    Collection<MatchInfo> tzdbNameMatches =
                            getTZDBTimeZoneNames().find(text, startIdx, nameTypes);
                    if (tzdbNameMatches != null) {
                        MatchInfo tzdbNameMatch = null;
                        for (MatchInfo match : tzdbNameMatches) {
                            if (startIdx + match.matchLength() > parsedPos) {
                                tzdbNameMatch = match;
                                parsedPos = startIdx + match.matchLength();
                            }
                        }
                        if (tzdbNameMatch != null) {
                            timeType.value = getTimeType(tzdbNameMatch.nameType());
                            pos.setIndex(parsedPos);
                            return TimeZone.getTimeZone(getTimeZoneID(tzdbNameMatch.tzID(), tzdbNameMatch.mzID()));
                        }
                    }
                }
                break;
            }
            case GENERIC_LONG:
            case GENERIC_SHORT:
            case GENERIC_LOCATION:
            {
                EnumSet<GenericNameType> genericNameTypes = null;
                switch (style) {
                case GENERIC_LOCATION:
                    genericNameTypes = EnumSet.of(GenericNameType.LOCATION);
                    break;
                case GENERIC_LONG:
                    genericNameTypes = EnumSet.of(GenericNameType.LONG, GenericNameType.LOCATION);
                    break;
                case GENERIC_SHORT:
                    genericNameTypes = EnumSet.of(GenericNameType.SHORT, GenericNameType.LOCATION);
                    break;
                default:
                    // style cannot be other than above cases
                    assert false;
                    break;
                }
                GenericMatchInfo bestGeneric = getTimeZoneGenericNames().findBestMatch(text, startIdx, genericNameTypes);
                if (bestGeneric != null && (startIdx + bestGeneric.matchLength() > parsedPos)) {
                    timeType.value = bestGeneric.timeType();
                    pos.setIndex(startIdx + bestGeneric.matchLength());
                    return TimeZone.getTimeZone(bestGeneric.tzID());
                }
                break;
            }
            case ZONE_ID:
            {
                tmpPos.setIndex(startIdx);
                tmpPos.setErrorIndex(-1);

                String id = parseZoneID(text, tmpPos);
                if (tmpPos.getErrorIndex() == -1) {
                    pos.setIndex(tmpPos.getIndex());
                    return TimeZone.getTimeZone(id);
                }
                break;
            }
            case ZONE_ID_SHORT:
            {
                tmpPos.setIndex(startIdx);
                tmpPos.setErrorIndex(-1);

                String id = parseShortZoneID(text, tmpPos);
                if (tmpPos.getErrorIndex() == -1) {
                    pos.setIndex(tmpPos.getIndex());
                    return TimeZone.getTimeZone(id);
                }
                break;
            }
            case EXEMPLAR_LOCATION:
            {
                tmpPos.setIndex(startIdx);
                tmpPos.setErrorIndex(-1);

                String id = parseExemplarLocation(text, tmpPos);
                if (tmpPos.getErrorIndex() == -1) {
                    pos.setIndex(tmpPos.getIndex());
                    return TimeZone.getTimeZone(id);
                }
                break;
            }
        }
        evaluated |= style.flag;

        if (parsedPos > startIdx) {
            // When the specified style is one of SPECIFIC_XXX or GENERIC_XXX, we tried to parse the input
            // as localized GMT format earlier. If parsedOffset is positive, it means it was successfully
            // parsed as localized GMT format, but offset digits were not detected (more specifically, GMT
            // zero format). Then, it tried to find a match within the set of display names, but could not
            // find a match. At this point, we can safely assume the input text contains the localized
            // GMT format.
            assert parsedOffset != UNKNOWN_OFFSET;
            pos.setIndex(parsedPos);
            return getTimeZoneForOffset(parsedOffset);
        }


        // Failed to parse the input text as the time zone format in the specified style.
        // Check the longest match among other styles below.
        String parsedID = null;                     // stores successfully parsed zone ID for later use
        TimeType parsedTimeType = TimeType.UNKNOWN; // stores successfully parsed time type for later use
        assert parsedPos < 0;
        assert parsedOffset == UNKNOWN_OFFSET;

        // ISO 8601
        if (parsedPos < maxPos &&
                ((evaluated & ISO_Z_STYLE_FLAG) == 0 || (evaluated & ISO_LOCAL_STYLE_FLAG) == 0)) {
            tmpPos.setIndex(startIdx);
            tmpPos.setErrorIndex(-1);

            Output<Boolean> hasDigitOffset = new Output<Boolean>(false);
            offset = parseOffsetISO8601(text, tmpPos, false, hasDigitOffset);
            if (tmpPos.getErrorIndex() == -1) {
                if (tmpPos.getIndex() == maxPos || hasDigitOffset.value) {
                    pos.setIndex(tmpPos.getIndex());
                    return getTimeZoneForOffset(offset);
                }
                // Note: When ISO 8601 format contains offset digits, it should not
                // collide with other formats. However, ISO 8601 UTC format "Z" (single letter)
                // may collide with other names. In this case, we need to evaluate other names.
                if (parsedPos < tmpPos.getIndex()) {
                    parsedOffset = offset;
                    parsedID = null;
                    parsedTimeType = TimeType.UNKNOWN;
                    parsedPos = tmpPos.getIndex();
                    assert parsedPos == startIdx + 1;   // only when "Z" is used
                }
            }
        }


        // Localized GMT format
        if (parsedPos < maxPos &&
                (evaluated & Style.LOCALIZED_GMT.flag) == 0) {
            tmpPos.setIndex(startIdx);
            tmpPos.setErrorIndex(-1);

            Output<Boolean> hasDigitOffset = new Output<Boolean>(false);
            offset = parseOffsetLocalizedGMT(text, tmpPos, false, hasDigitOffset);
            if (tmpPos.getErrorIndex() == -1) {
                if (tmpPos.getIndex() == maxPos || hasDigitOffset.value) {
                    pos.setIndex(tmpPos.getIndex());
                    return getTimeZoneForOffset(offset);
                }
                // Evaluate other names - see the comment earlier in this method.
                if (parsedPos < tmpPos.getIndex()) {
                    parsedOffset = offset;
                    parsedID = null;
                    parsedTimeType = TimeType.UNKNOWN;
                    parsedPos = tmpPos.getIndex();
                }
            }
        }

        if (parsedPos < maxPos &&
                (evaluated & Style.LOCALIZED_GMT_SHORT.flag) == 0) {
            tmpPos.setIndex(startIdx);
            tmpPos.setErrorIndex(-1);

            Output<Boolean> hasDigitOffset = new Output<Boolean>(false);
            offset = parseOffsetLocalizedGMT(text, tmpPos, true, hasDigitOffset);
            if (tmpPos.getErrorIndex() == -1) {
                if (tmpPos.getIndex() == maxPos || hasDigitOffset.value) {
                    pos.setIndex(tmpPos.getIndex());
                    return getTimeZoneForOffset(offset);
                }
                // Evaluate other names - see the comment earlier in this method.
                if (parsedPos < tmpPos.getIndex()) {
                    parsedOffset = offset;
                    parsedID = null;
                    parsedTimeType = TimeType.UNKNOWN;
                    parsedPos = tmpPos.getIndex();
                }
            }
        }

        // When ParseOption.ALL_STYLES is available, we also try to look all possible display names and IDs.
        // For example, when style is GENERIC_LONG, "EST" (SPECIFIC_SHORT) is never
        // used for America/New_York. With parseAllStyles true, this code parses "EST"
        // as America/New_York.

        // Note: Adding all possible names into the trie used by the implementation is quite heavy operation,
        // which we want to avoid normally (note that we cache the trie, so this is applicable to the
        // first time only as long as the cache does not expire).

        boolean parseAllStyles = (options == null) ?
                getDefaultParseOptions().contains(ParseOption.ALL_STYLES)
                : options.contains(ParseOption.ALL_STYLES);

        if (parseAllStyles) {
            // Try all specific names and exemplar location names
            if (parsedPos < maxPos) {
                Collection<MatchInfo> specificMatches = _tznames.find(text, startIdx, ALL_SIMPLE_NAME_TYPES);
                MatchInfo specificMatch = null;
                int matchPos = -1;
                if (specificMatches != null) {
                    for (MatchInfo match : specificMatches) {
                        if (startIdx + match.matchLength() > matchPos) {
                            specificMatch = match;
                            matchPos = startIdx + match.matchLength();
                        }
                    }
                }
                if (parsedPos < matchPos) {
                    parsedPos = matchPos;
                    parsedID = getTimeZoneID(specificMatch.tzID(), specificMatch.mzID());
                    parsedTimeType = getTimeType(specificMatch.nameType());
                    parsedOffset = UNKNOWN_OFFSET;
                }
            }
            if (parseTZDBAbbrev && parsedPos < maxPos && (evaluated & Style.SPECIFIC_SHORT.flag) == 0) {
                Collection<MatchInfo> tzdbNameMatches =
                        getTZDBTimeZoneNames().find(text, startIdx, ALL_SIMPLE_NAME_TYPES);
                MatchInfo tzdbNameMatch = null;
                int matchPos = -1;
                if (tzdbNameMatches != null) {
                    for (MatchInfo match : tzdbNameMatches) {
                        if (startIdx + match.matchLength() > matchPos) {
                            tzdbNameMatch = match;
                            matchPos = startIdx + match.matchLength();
                        }
                    }
                    if (parsedPos < matchPos) {
                        parsedPos = matchPos;
                        parsedID = getTimeZoneID(tzdbNameMatch.tzID(), tzdbNameMatch.mzID());
                        parsedTimeType = getTimeType(tzdbNameMatch.nameType());
                        parsedOffset = UNKNOWN_OFFSET;
                    }
                }

            }
            // Try generic names
            if (parsedPos < maxPos) {
                GenericMatchInfo genericMatch = getTimeZoneGenericNames().findBestMatch(text, startIdx, ALL_GENERIC_NAME_TYPES);
                if (genericMatch != null && parsedPos < startIdx + genericMatch.matchLength()) {
                    parsedPos = startIdx + genericMatch.matchLength();
                    parsedID = genericMatch.tzID();
                    parsedTimeType = genericMatch.timeType();
                    parsedOffset = UNKNOWN_OFFSET;
                }
            }

            // Try time zone ID
            if (parsedPos < maxPos && (evaluated & Style.ZONE_ID.flag) == 0) {
                tmpPos.setIndex(startIdx);
                tmpPos.setErrorIndex(-1);

                String id = parseZoneID(text, tmpPos);
                if (tmpPos.getErrorIndex() == -1 && parsedPos < tmpPos.getIndex()) {
                    parsedPos = tmpPos.getIndex();
                    parsedID = id;
                    parsedTimeType = TimeType.UNKNOWN;
                    parsedOffset = UNKNOWN_OFFSET;
                }
            }
            // Try short time zone ID
            if (parsedPos < maxPos && (evaluated & Style.ZONE_ID_SHORT.flag) == 0) {
                tmpPos.setIndex(startIdx);
                tmpPos.setErrorIndex(-1);

                String id = parseShortZoneID(text, tmpPos);
                if (tmpPos.getErrorIndex() == -1 && parsedPos < tmpPos.getIndex()) {
                    parsedPos = tmpPos.getIndex();
                    parsedID = id;
                    parsedTimeType = TimeType.UNKNOWN;
                    parsedOffset = UNKNOWN_OFFSET;
                }
            }
        }

        if (parsedPos > startIdx) {
            // Parsed successfully
            TimeZone parsedTZ = null;
            if (parsedID != null) {
                parsedTZ = TimeZone.getTimeZone(parsedID);
            } else {
                assert parsedOffset != UNKNOWN_OFFSET;
                parsedTZ = getTimeZoneForOffset(parsedOffset);
            }
            timeType.value = parsedTimeType;
            pos.setIndex(parsedPos);
            return parsedTZ;
        }

        pos.setErrorIndex(startIdx);
        return null;
    }

    /**
     * Returns a <code>TimeZone</code> by parsing the time zone string according to
     * the parse position, the style and the default parse options.
     * <p>
     * <b>Note</b>: This method is equivalent to {@link #parse(Style, String, ParsePosition, EnumSet, Output)
     * parse(style, text, pos, null, timeType)}.
     *
     * @param text the text contains a time zone string at the position.
     * @param style the format style
     * @param pos the position.
     * @param timeType The output argument for receiving the time type (standard/daylight/unknown),
     * or specify null if the information is not necessary.
     * @return A <code>TimeZone</code>, or null if the input could not be parsed.
     * @see Style
     * @see #parse(Style, String, ParsePosition, EnumSet, Output)
     * @see #format(Style, TimeZone, long, Output)
     * @see #setDefaultParseOptions(EnumSet)
     */
    public TimeZone parse(Style style, String text, ParsePosition pos, Output<TimeType> timeType) {
        return parse(style, text, pos, null, timeType);
    }

    /**
     * Returns a <code>TimeZone</code> by parsing the time zone string according to
     * the given parse position.
     * <p>
     * <b>Note</b>: This method is equivalent to {@link #parse(Style, String, ParsePosition, EnumSet, Output)
     * parse(Style.GENERIC_LOCATION, text, pos, EnumSet.of(ParseOption.ALL_STYLES), timeType)}.
     *
     * @param text the text contains a time zone string at the position.
     * @param pos the position.
     * @return A <code>TimeZone</code>, or null if the input could not be parsed.
     * @see #parse(Style, String, ParsePosition, EnumSet, Output)
     */
    public final TimeZone parse(String text, ParsePosition pos) {
        return parse(Style.GENERIC_LOCATION, text, pos, EnumSet.of(ParseOption.ALL_STYLES), null);
    }

    /**
     * Returns a <code>TimeZone</code> for the given text.
     * <p>
     * <b>Note</b>: The behavior of this method is equivalent to {@link #parse(String, ParsePosition)}.
     * @param text the time zone string
     * @return A <code>TimeZone</code>.
     * @throws ParseException when the input could not be parsed as a time zone string.
     * @see #parse(String, ParsePosition)
     */
    public final TimeZone parse(String text) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        TimeZone tz = parse(text, pos);
        if (pos.getErrorIndex() >= 0) {
            throw new ParseException("Unparseable time zone: \"" + text + "\"" , 0);
        }
        assert(tz != null);
        return tz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        TimeZone tz = null;
        long date = System.currentTimeMillis();

        if (obj instanceof TimeZone) {
            tz = (TimeZone)obj;
        } else if (obj instanceof Calendar) {
            tz = ((Calendar)obj).getTimeZone();
            date = ((Calendar)obj).getTimeInMillis();
        } else {
            throw new IllegalArgumentException("Cannot format given Object (" +
                    obj.getClass().getName() + ") as a time zone");
        }
        assert(tz != null);
        String result = formatOffsetLocalizedGMT(tz.getOffset(date));
        toAppendTo.append(result);

        if (pos.getFieldAttribute() == DateFormat.Field.TIME_ZONE
                || pos.getField() == DateFormat.TIMEZONE_FIELD) {
            pos.setBeginIndex(0);
            pos.setEndIndex(result.length());
        }
        return toAppendTo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        StringBuffer toAppendTo = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        toAppendTo = format(obj, toAppendTo, pos);

        // supporting only DateFormat.Field.TIME_ZONE
        AttributedString as = new AttributedString(toAppendTo.toString());
        as.addAttribute(DateFormat.Field.TIME_ZONE, DateFormat.Field.TIME_ZONE);

        return as.getIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    /**
     * Private method used for localized GMT formatting.
     * @param offset the zone's UTC offset
     * @param isShort true if the short localized GMT format is desired
     * @return the localized GMT string
     */
    private String formatOffsetLocalizedGMT(int offset, boolean isShort) {
        if (offset == 0) {
            return _gmtZeroFormat;
        }

        StringBuilder buf = new StringBuilder();
        boolean positive = true;
        if (offset < 0) {
            offset = -offset;
            positive = false;
        }

        int offsetH = offset / MILLIS_PER_HOUR;
        offset = offset % MILLIS_PER_HOUR;
        int offsetM = offset / MILLIS_PER_MINUTE;
        offset = offset % MILLIS_PER_MINUTE;
        int offsetS = offset / MILLIS_PER_SECOND;

        if (offsetH > MAX_OFFSET_HOUR || offsetM > MAX_OFFSET_MINUTE || offsetS > MAX_OFFSET_SECOND) {
            throw new IllegalArgumentException("Offset out of range :" + offset);
        }

        Object[] offsetPatternItems;
        if (positive) {
            if (offsetS != 0) {
                offsetPatternItems = _gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_HMS.ordinal()];
            } else if (offsetM != 0 || !isShort) {
                offsetPatternItems = _gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_HM.ordinal()];
            } else {
                offsetPatternItems = _gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_H.ordinal()];
            }
        } else {
            if (offsetS != 0) {
                offsetPatternItems = _gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_HMS.ordinal()];
            } else if (offsetM != 0 || !isShort) {
                offsetPatternItems = _gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_HM.ordinal()];
            } else {
                offsetPatternItems = _gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_H.ordinal()];
            }
        }

        // Building the GMT format string
        buf.append(_gmtPatternPrefix);

        for (Object item : offsetPatternItems) {
            if (item instanceof String) {
                // pattern literal
                buf.append((String)item);
            } else if (item instanceof GMTOffsetField) {
                // Hour/minute/second field
                GMTOffsetField field = (GMTOffsetField)item;
                switch (field.getType()) {
                case 'H':
                    appendOffsetDigits(buf, offsetH, (isShort ? 1 : 2));
                    break;
                case 'm':
                    appendOffsetDigits(buf, offsetM, 2);
                    break;
                case 's':
                    appendOffsetDigits(buf, offsetS, 2);
                    break;
                }
            }
        }
        buf.append(_gmtPatternSuffix);
        return buf.toString();
    }

    /**
     * Numeric offset field combinations
     */
    private enum OffsetFields {
        H, HM, HMS
    }

    private String formatOffsetISO8601(int offset, boolean isBasic, boolean useUtcIndicator, boolean isShort, boolean ignoreSeconds) {
        int absOffset = offset < 0 ? -offset : offset;
        if (useUtcIndicator && (absOffset < MILLIS_PER_SECOND || (ignoreSeconds && absOffset < MILLIS_PER_MINUTE))) {
            return ISO8601_UTC;
        }
        OffsetFields minFields = isShort ? OffsetFields.H : OffsetFields.HM;
        OffsetFields maxFields = ignoreSeconds ? OffsetFields.HM : OffsetFields.HMS;
        Character sep = isBasic ? null : ':';

        // Note: OffsetFields.HMS as maxFields is an ICU extension. ISO 8601 specification does
        // not support seconds field.

        if (absOffset >= MAX_OFFSET) {
            throw new IllegalArgumentException("Offset out of range :" + offset);
        }

        int[] fields = new int[3];
        fields[0] = absOffset / MILLIS_PER_HOUR;
        absOffset = absOffset % MILLIS_PER_HOUR;
        fields[1] = absOffset / MILLIS_PER_MINUTE;
        absOffset = absOffset % MILLIS_PER_MINUTE;
        fields[2] = absOffset / MILLIS_PER_SECOND;

        assert(fields[0] >= 0 && fields[0] <= MAX_OFFSET_HOUR);
        assert(fields[1] >= 0 && fields[1] <= MAX_OFFSET_MINUTE);
        assert(fields[2] >= 0 && fields[2] <= MAX_OFFSET_SECOND);

        int lastIdx = maxFields.ordinal();
        while (lastIdx > minFields.ordinal()) {
            if (fields[lastIdx] != 0) {
                break;
            }
            lastIdx--;
        }

        StringBuilder buf = new StringBuilder();
        char sign = '+';
        if (offset < 0) {
            // if all output fields are 0s, do not use negative sign
            for (int idx = 0; idx <= lastIdx; idx++) {
                if (fields[idx] != 0) {
                    sign = '-';
                    break;
                }
            }
        }
        buf.append(sign);

        for (int idx = 0; idx <= lastIdx; idx++) {
            if (sep != null && idx != 0) {
                buf.append(sep);
            }
            if (fields[idx] < 10) {
                buf.append('0');
            }
            buf.append(fields[idx]);
        }
        return buf.toString();
    }

    /**
     * Private method returning the time zone's specific format string.
     *
     * @param tz the time zone
     * @param stdType the name type used for standard time
     * @param dstType the name type used for daylight time
     * @param date the date
     * @param timeType when null, actual time type is set
     * @return the time zone's specific format name string
     */
    private String formatSpecific(TimeZone tz, NameType stdType, NameType dstType, long date, Output<TimeType> timeType) {
        assert(stdType == NameType.LONG_STANDARD || stdType == NameType.SHORT_STANDARD);
        assert(dstType == NameType.LONG_DAYLIGHT || dstType == NameType.SHORT_DAYLIGHT);

        boolean isDaylight = tz.inDaylightTime(new Date(date));
        String name = isDaylight?
                getTimeZoneNames().getDisplayName(ZoneMeta.getCanonicalCLDRID(tz), dstType, date) :
                getTimeZoneNames().getDisplayName(ZoneMeta.getCanonicalCLDRID(tz), stdType, date);

        if (name != null && timeType != null) {
            timeType.value = isDaylight ? TimeType.DAYLIGHT : TimeType.STANDARD;
        }
        return name;
    }

    /**
     * Private method returning the time zone's exemplar location string.
     * This method will never return null.
     *
     * @param tz the time zone
     * @return the time zone's exemplar location name.
     */
    private String formatExemplarLocation(TimeZone tz) {
        String location = getTimeZoneNames().getExemplarLocationName(ZoneMeta.getCanonicalCLDRID(tz));
        if (location == null) {
            // Use "unknown" location
            location = getTimeZoneNames().getExemplarLocationName(UNKNOWN_ZONE_ID);
            if (location == null) {
                // last resort
                location = UNKNOWN_LOCATION;
            }
        }
        return location;
    }

    /**
     * Private method returns a time zone ID. If tzID is not null, the value of tzID is returned.
     * If tzID is null, then this method look up a time zone ID for the current region. This is a
     * small helper method used by the parse implementation method
     *
     * @param tzID
     *            the time zone ID or null
     * @param mzID
     *            the meta zone ID or null
     * @return A time zone ID
     * @throws IllegalArgumentException
     *             when both tzID and mzID are null
     */
    private String getTimeZoneID(String tzID, String mzID) {
        String id = tzID;
        if (id == null) {
            assert (mzID != null);
            id = _tznames.getReferenceZoneID(mzID, getTargetRegion());
            if (id == null) {
                throw new IllegalArgumentException("Invalid mzID: " + mzID);
            }
        }
        return id;
    }

    /**
     * Private method returning the target region. The target regions is determined by
     * the locale of this instance. When a generic name is coming from
     * a meta zone, this region is used for checking if the time zone
     * is a reference zone of the meta zone.
     *
     * @return the target region
     */
    private synchronized String getTargetRegion() {
        if (_region == null) {
            _region = _locale.getCountry();
            if (_region.length() == 0) {
                ULocale tmp = ULocale.addLikelySubtags(_locale);
                _region = tmp.getCountry();
                if (_region.length() == 0) {
                    _region = "001";
                }
            }
        }
        return _region;
    }

    /**
     * Returns the time type for the given name type
     * @param nameType the name type
     * @return the time type (unknown/standard/daylight)
     */
    private TimeType getTimeType(NameType nameType) {
        switch (nameType) {
        case LONG_STANDARD:
        case SHORT_STANDARD:
            return TimeType.STANDARD;

        case LONG_DAYLIGHT:
        case SHORT_DAYLIGHT:
            return TimeType.DAYLIGHT;

        default:
            return TimeType.UNKNOWN;
        }
    }

    /**
     * Parses the localized GMT pattern string and initialize
     * localized gmt pattern fields including {{@link #_gmtPatternTokens}.
     * This method must be also called at deserialization time.
     *
     * @param gmtPattern the localized GMT pattern string such as "GMT {0}"
     * @throws IllegalArgumentException when the pattern string does not contain "{0}"
     */
    private void initGMTPattern(String gmtPattern) {
        // This implementation not perfect, but sufficient practically.
        int idx = gmtPattern.indexOf("{0}");
        if (idx < 0) {
            throw new IllegalArgumentException("Bad localized GMT pattern: " + gmtPattern);
        }
        _gmtPattern = gmtPattern;
        _gmtPatternPrefix = unquote(gmtPattern.substring(0, idx));
        _gmtPatternSuffix = unquote(gmtPattern.substring(idx + 3));
    }

    /**
     * Unquotes the message format style pattern.
     *
     * @param s the pattern
     * @return the unquoted pattern string
     */
    private static String unquote(String s) {
        if (s.indexOf('\'') < 0) {
            return s;
        }
        boolean isPrevQuote = false;
        boolean inQuote = false;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                if (isPrevQuote) {
                    buf.append(c);
                    isPrevQuote = false;
                } else {
                    isPrevQuote = true;
                }
                inQuote = !inQuote;
            } else {
                isPrevQuote = false;
                buf.append(c);
            }
        }
        return buf.toString();
    }

    /**
     * Initialize localized GMT format offset hour/min/sec patterns.
     * This method parses patterns into optimized run-time format.
     * This method must be called at deserialization time.
     *
     * @param gmtOffsetPatterns patterns, String[4]
     * @throws IllegalArgumentException when patterns are not valid
     */
    private void initGMTOffsetPatterns(String[] gmtOffsetPatterns) {
        int size = GMTOffsetPatternType.values().length;
        if (gmtOffsetPatterns.length < size) {
            throw new IllegalArgumentException("Insufficient number of elements in gmtOffsetPatterns");
        }
        Object[][] gmtOffsetPatternItems = new Object[size][];
        for (GMTOffsetPatternType t : GMTOffsetPatternType.values()) {
            int idx = t.ordinal();
            // Note: parseOffsetPattern will validate the given pattern and throws
            // IllegalArgumentException when pattern is not valid
            Object[] parsedItems = parseOffsetPattern(gmtOffsetPatterns[idx], t.required());
            gmtOffsetPatternItems[idx] = parsedItems;
        }

        _gmtOffsetPatterns = new String[size];
        System.arraycopy(gmtOffsetPatterns, 0, _gmtOffsetPatterns, 0, size);
        _gmtOffsetPatternItems = gmtOffsetPatternItems;
        checkAbuttingHoursAndMinutes();
    }

    private void checkAbuttingHoursAndMinutes() {
        _abuttingOffsetHoursAndMinutes = false;
        for (Object[] items : _gmtOffsetPatternItems) {
            boolean afterH = false;
            for (Object item : items) {
                if (item instanceof GMTOffsetField) {
                    GMTOffsetField fld = (GMTOffsetField)item;
                    if (afterH) {
                        _abuttingOffsetHoursAndMinutes = true;
                    } else if (fld.getType() == 'H') {
                        afterH = true;
                    }
                } else if (afterH) {
                    break;
                }
            }
        }
    }

    /**
     * Used for representing localized GMT time fields in the parsed pattern object.
     * @see TimeZoneFormat#parseOffsetPattern(String, String)
     */
    private static class GMTOffsetField {
        final char _type;
        final int _width;

        GMTOffsetField(char type, int width) {
            _type = type;
            _width = width;
        }

        char getType() {
            return _type;
        }

        @SuppressWarnings("unused")
        int getWidth() {
            return _width;
        }

        static boolean isValid(char type, int width) {
            return (width == 1 ||  width == 2);
        }
    }

    /**
     * Parse the GMT offset pattern into runtime optimized format
     *
     * @param pattern the offset pattern string
     * @param letters the required pattern letters such as "Hm"
     * @return An array of Object. Each array entry is either String (representing
     * pattern literal) or GMTOffsetField (hour/min/sec field)
     */
    private static Object[] parseOffsetPattern(String pattern, String letters) {
        boolean isPrevQuote = false;
        boolean inQuote = false;
        StringBuilder text = new StringBuilder();
        char itemType = 0;  // 0 for string literal, otherwise time pattern character
        int itemLength = 1;
        boolean invalidPattern = false;

        List<Object> items = new ArrayList<Object>();
        BitSet checkBits = new BitSet(letters.length());

        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            if (ch == '\'') {
                if (isPrevQuote) {
                    text.append('\'');
                    isPrevQuote = false;
                } else {
                    isPrevQuote = true;
                    if (itemType != 0) {
                        if (GMTOffsetField.isValid(itemType, itemLength)) {
                            items.add(new GMTOffsetField(itemType, itemLength));
                        } else {
                            invalidPattern = true;
                            break;
                        }
                        itemType = 0;
                    }
                }
                inQuote = !inQuote;
            } else {
                isPrevQuote = false;
                if (inQuote) {
                    text.append(ch);
                } else {
                    int patFieldIdx = letters.indexOf(ch);
                    if (patFieldIdx >= 0) {
                        // an offset time pattern character
                        if (ch == itemType) {
                            itemLength++;
                        } else {
                            if (itemType == 0) {
                                if (text.length() > 0) {
                                    items.add(text.toString());
                                    text.setLength(0);
                                }
                            } else {
                                if (GMTOffsetField.isValid(itemType, itemLength)) {
                                    items.add(new GMTOffsetField(itemType, itemLength));
                                } else {
                                    invalidPattern = true;
                                    break;
                                }
                            }
                            itemType = ch;
                            itemLength = 1;
                            checkBits.set(patFieldIdx);
                        }
                    } else {
                        // a string literal
                        if (itemType != 0) {
                            if (GMTOffsetField.isValid(itemType, itemLength)) {
                                items.add(new GMTOffsetField(itemType, itemLength));
                            } else {
                                invalidPattern = true;
                                break;
                            }
                            itemType = 0;
                        }
                        text.append(ch);
                    }
                }
            }
        }
        // handle last item
        if (!invalidPattern) {
            if (itemType == 0) {
                if (text.length() > 0) {
                    items.add(text.toString());
                    text.setLength(0);
                }
            } else {
                if (GMTOffsetField.isValid(itemType, itemLength)) {
                    items.add(new GMTOffsetField(itemType, itemLength));
                } else {
                    invalidPattern = true;
                }
            }
        }

        if (invalidPattern || checkBits.cardinality() != letters.length()) {
            throw new IllegalStateException("Bad localized GMT offset pattern: " + pattern);
        }

        return items.toArray(new Object[items.size()]);
    }

    /**
     * Appends seconds field to the offset pattern with hour/minute
     *
     * @param offsetHM the offset pattern including hours and minutes fields
     * @return the offset pattern including hours, minutes and seconds fields
     */
    //TODO This code will be obsoleted once we add hour-minute-second pattern data in CLDR
    private static String expandOffsetPattern(String offsetHM) {
        int idx_mm = offsetHM.indexOf("mm");
        if (idx_mm < 0) {
            throw new RuntimeException("Bad time zone hour pattern data");
        }
        String sep = ":";
        int idx_H = offsetHM.substring(0, idx_mm).lastIndexOf("H");
        if (idx_H >= 0) {
            sep = offsetHM.substring(idx_H + 1, idx_mm);
        }
        return offsetHM.substring(0, idx_mm + 2) + sep + "ss" + offsetHM.substring(idx_mm + 2);
    }

    /**
     * Truncates minutes field from the offset pattern with hour/minute
     *
     * @param offsetHM the offset pattern including hours and minutes fields
     * @return the offset pattern including only hours field
     */
    //TODO This code will be obsoleted once we add hour pattern data in CLDR
    private static String truncateOffsetPattern(String offsetHM) {
        int idx_mm = offsetHM.indexOf("mm");
        if (idx_mm < 0) {
            throw new RuntimeException("Bad time zone hour pattern data");
        }
        int idx_HH = offsetHM.substring(0, idx_mm).lastIndexOf("HH");
        if (idx_HH >= 0) {
            return offsetHM.substring(0, idx_HH + 2);
        }
        int idx_H = offsetHM.substring(0, idx_mm).lastIndexOf("H");
        if (idx_H >= 0) {
            return offsetHM.substring(0, idx_H + 1);
        }
        throw new RuntimeException("Bad time zone hour pattern data");
    }

    /**
     * Appends localized digits to the buffer.
     * <p>
     * Note: This code assumes that the input number is 0 - 59
     *
     * @param buf the target buffer
     * @param n the integer number
     * @param minDigits the minimum digits width
     */
    private void appendOffsetDigits(StringBuilder buf, int n, int minDigits) {
        assert(n >= 0 && n < 60);
        int numDigits = n >= 10 ? 2 : 1;
        for (int i = 0; i < minDigits - numDigits; i++) {
            buf.append(_gmtOffsetDigits[0]);
        }
        if (numDigits == 2) {
            buf.append(_gmtOffsetDigits[n / 10]);
        }
        buf.append(_gmtOffsetDigits[n % 10]);
    }

    /**
     * Creates an instance of TimeZone for the given offset
     * @param offset the offset
     * @return A TimeZone with the given offset
     */
    private TimeZone getTimeZoneForOffset(int offset) {
        if (offset == 0) {
            // when offset is 0, we should use "Etc/GMT"
            return TimeZone.getTimeZone(TZID_GMT);
        }
        return ZoneMeta.getCustomTimeZone(offset);
    }

    /**
     * Returns offset from GMT(UTC) in milliseconds for the given localized GMT
     * offset format string. When the given string cannot be parsed, this method
     * sets the current position as the error index to <code>ParsePosition pos</code>
     * and returns 0.
     *
     * @param text the text contains a localized GMT offset string at the position.
     * @param pos the position.
     * @param isShort true if this parser to try the short format first
     * @param hasDigitOffset receiving if the parsed zone string contains offset digits.
     * @return the offset from GMT(UTC) in milliseconds for the given localized GMT
     * offset format string.
     */
    private int parseOffsetLocalizedGMT(String text, ParsePosition pos, boolean isShort, Output<Boolean> hasDigitOffset) {
        int start = pos.getIndex();
        int offset = 0;
        int[] parsedLength = {0};

        if (hasDigitOffset != null) {
            hasDigitOffset.value = false;
        }

        offset = parseOffsetLocalizedGMTPattern(text, start, isShort, parsedLength);

        // For now, parseOffsetLocalizedGMTPattern handles both long and short
        // formats, no matter isShort is true or false. This might be changed in future
        // when strict parsing is necessary, or different set of patterns are used for
        // short/long formats.
//        if (parsedLength[0] == 0) {
//            offset = parseOffsetLocalizedGMTPattern(text, start, !isShort, parsedLength);
//        }

        if (parsedLength[0] > 0) {
            if (hasDigitOffset != null) {
                hasDigitOffset.value = true;
            }
            pos.setIndex(start + parsedLength[0]);
            return offset;
        }

        // Try the default patterns
        offset = parseOffsetDefaultLocalizedGMT(text, start, parsedLength);
        if (parsedLength[0] > 0) {
            if (hasDigitOffset != null) {
                hasDigitOffset.value = true;
            }
            pos.setIndex(start + parsedLength[0]);
            return offset;
        }

        // Check if this is a GMT zero format
        if (text.regionMatches(true, start, _gmtZeroFormat, 0, _gmtZeroFormat.length())) {
            pos.setIndex(start + _gmtZeroFormat.length());
            return 0;
        }

        // Check if this is a default GMT zero format
        for (String defGMTZero : ALT_GMT_STRINGS) {
            if (text.regionMatches(true, start, defGMTZero, 0, defGMTZero.length())) {
                pos.setIndex(start + defGMTZero.length());
                return 0;
            }
        }

        // Nothing matched
        pos.setErrorIndex(start);
        return 0;
    }

    /**
     * Parse localized GMT format generated by the pattern used by this formatter, except
     * GMT Zero format.
     * @param text the input text
     * @param start the start index
     * @param isShort true if the short localized GMT format is parsed.
     * @param parsedLen the parsed length, or 0 on failure.
     * @return the parsed offset in milliseconds.
     */
    private int parseOffsetLocalizedGMTPattern(String text, int start, boolean isShort, int[] parsedLen) {
        int idx = start;
        int offset = 0;
        boolean parsed = false;

        do {
            // Prefix part
            int len = _gmtPatternPrefix.length();
            if (len > 0 && !text.regionMatches(true, idx, _gmtPatternPrefix, 0, len)) {
                // prefix match failed
                break;
            }
            idx += len;

            // Offset part
            int[] offsetLen = new int[1];
            offset = parseOffsetFields(text, idx, false, offsetLen);
            if (offsetLen[0] == 0) {
                // offset field match failed
                break;
            }
            idx += offsetLen[0];

            // Suffix part
            len = _gmtPatternSuffix.length();
            if (len > 0 && !text.regionMatches(true, idx, _gmtPatternSuffix, 0, len)) {
                // no suffix match
                break;
            }
            idx += len;
            parsed = true;
        } while (false);

        parsedLen[0] = parsed ? idx - start : 0;
        return offset;
    }

    /**
     * Parses localized GMT offset fields into offset.
     *
     * @param text the input text
     * @param start the start index
     * @param isShort true if this is a short format - currently not used
     * @param parsedLen the parsed length, or 0 on failure.
     * @return the parsed offset in milliseconds.
     */
    private int parseOffsetFields(String text, int start, boolean isShort, int[] parsedLen) {
        int outLen = 0;
        int offset = 0;
        int sign = 1;

        if (parsedLen != null && parsedLen.length >= 1) {
            parsedLen[0] = 0;
        }

        int offsetH, offsetM, offsetS;
        offsetH = offsetM = offsetS = 0;

        int[] fields = {0, 0, 0};
        for (GMTOffsetPatternType gmtPatType : PARSE_GMT_OFFSET_TYPES) {
            Object[] items = _gmtOffsetPatternItems[gmtPatType.ordinal()];
            assert items != null;

            outLen = parseOffsetFieldsWithPattern(text, start, items, false, fields);
            if (outLen > 0) {
                sign = gmtPatType.isPositive() ? 1 : -1;
                offsetH = fields[0];
                offsetM = fields[1];
                offsetS = fields[2];
                break;
            }
        }
        if (outLen > 0 && _abuttingOffsetHoursAndMinutes) {
            // When hours field is abutting minutes field,
            // the parse result above may not be appropriate.
            // For example, "01020" is parsed as 01:02 above,
            // but it should be parsed as 00:10:20.
            int tmpLen = 0;
            int tmpSign = 1;
            for (GMTOffsetPatternType gmtPatType : PARSE_GMT_OFFSET_TYPES) {
                Object[] items = _gmtOffsetPatternItems[gmtPatType.ordinal()];
                assert items != null;

                // forcing parse to use single hour digit
                tmpLen = parseOffsetFieldsWithPattern(text, start, items, true, fields);
                if (tmpLen > 0) {
                    tmpSign = gmtPatType.isPositive() ? 1 : -1;
                    break;
                }
            }
            if (tmpLen > outLen) {
                // Better parse result with single hour digit
                outLen = tmpLen;
                sign = tmpSign;
                offsetH = fields[0];
                offsetM = fields[1];
                offsetS = fields[2];
            }
        }

        if (parsedLen != null && parsedLen.length >= 1) {
            parsedLen[0] = outLen;
        }

        if (outLen > 0) {
            offset = ((((offsetH * 60) + offsetM) * 60) + offsetS) * 1000 * sign;
        }

        return offset;
    }

    /**
     * Parses localized GMT offset fields with the given pattern
     *
     * @param text the input text
     * @param start the start index
     * @param patternItems the pattern (already itemized)
     * @param forceSingleHourDigit true if hours field is parsed as a single digit
     * @param fields receives the parsed hours/minutes/seconds
     * @return parsed length
     */
    private int parseOffsetFieldsWithPattern(String text, int start, Object[] patternItems, boolean forceSingleHourDigit, int fields[]) {
        assert (fields != null && fields.length >= 3);
        fields[0] = fields[1] = fields[2] = 0;

        boolean failed = false;
        int offsetH, offsetM, offsetS;
        offsetH = offsetM = offsetS = 0;
        int idx = start;
        int[] tmpParsedLen = {0};
        for (int i = 0; i < patternItems.length; i++) {
            if (patternItems[i] instanceof String) {
                String patStr = (String)patternItems[i];
                int len = patStr.length();
                if (!text.regionMatches(true, idx, patStr, 0, len)) {
                    failed = true;
                    break;
                }
                idx += len;
            } else {
                assert(patternItems[i] instanceof GMTOffsetField);
                GMTOffsetField field = (GMTOffsetField)patternItems[i];
                char fieldType = field.getType();
                if (fieldType == 'H') {
                    int maxDigits = forceSingleHourDigit ? 1 : 2;
                    offsetH = parseOffsetFieldWithLocalizedDigits(text, idx, 1, maxDigits, 0, MAX_OFFSET_HOUR, tmpParsedLen);
                } else if (fieldType == 'm') {
                    offsetM = parseOffsetFieldWithLocalizedDigits(text, idx, 2, 2, 0, MAX_OFFSET_MINUTE, tmpParsedLen);
                } else if (fieldType == 's') {
                    offsetS = parseOffsetFieldWithLocalizedDigits(text, idx, 2, 2, 0, MAX_OFFSET_SECOND, tmpParsedLen);
                }

                if (tmpParsedLen[0] == 0) {
                    failed = true;
                    break;
                }
                idx += tmpParsedLen[0];
            }
        }

        if (failed) {
            return 0;
        }

        fields[0] = offsetH;
        fields[1] = offsetM;
        fields[2] = offsetS;

        return idx - start;
    }

    /**
     * Parses the input text using the default format patterns (e.g. "UTC{0}").
     * @param text the input text
     * @param start the start index
     * @param parsedLen the parsed length, or 0 on failure
     * @return the parsed offset in milliseconds.
     */
    private int parseOffsetDefaultLocalizedGMT(String text, int start, int[] parsedLen) {
        int idx = start;
        int offset = 0;
        int parsed = 0;
        do {
            // check global default GMT alternatives
            int gmtLen = 0;
            for (String gmt : ALT_GMT_STRINGS) {
                int len = gmt.length();
                if (text.regionMatches(true, idx, gmt, 0, len)) {
                    gmtLen = len;
                    break;
                }
            }
            if (gmtLen == 0) {
                break;
            }
            idx += gmtLen;

            // offset needs a sign char and a digit at minimum
            if (idx + 1 >= text.length()) {
                break;
            }

            // parse sign
            int sign = 1;
            char c = text.charAt(idx);
            if (c == '+') {
                sign = 1;
            } else if (c == '-') {
                sign = -1;
            } else {
                break;
            }
            idx++;

            // offset part
            // try the default pattern with the separator first
            int[] lenWithSep = {0};
            int offsetWithSep = parseDefaultOffsetFields(text, idx, DEFAULT_GMT_OFFSET_SEP, lenWithSep);
            if (lenWithSep[0] == text.length() - idx) {
                // maximum match
                offset = offsetWithSep * sign;
                idx += lenWithSep[0];
            } else {
                // try abutting field pattern
                int[] lenAbut = {0};
                int offsetAbut = parseAbuttingOffsetFields(text, idx, lenAbut);

                if (lenWithSep[0] > lenAbut[0]) {
                    offset = offsetWithSep * sign;
                    idx += lenWithSep[0];
                } else {
                    offset = offsetAbut * sign;
                    idx += lenAbut[0];
                }
            }
            parsed = idx - start;
        } while (false);

        parsedLen[0] = parsed;
        return offset;
    }

    /**
     * Parses the input GMT offset fields with the default offset pattern.
     * @param text the input text
     * @param start the start index
     * @param separator the separator character, e.g. ':'
     * @param parsedLen the parsed length, or 0 on failure.
     * @return the parsed offset in milliseconds.
     */
    private int parseDefaultOffsetFields(String text, int start, char separator, int[] parsedLen) {
        int max = text.length();
        int idx = start;
        int[] len = {0};
        int hour = 0, min = 0, sec = 0;

        do {
            hour = parseOffsetFieldWithLocalizedDigits(text, idx, 1, 2, 0, MAX_OFFSET_HOUR, len);
            if (len[0] == 0) {
                break;
            }
            idx += len[0];

            if (idx + 1 < max && text.charAt(idx) == separator) {
                min = parseOffsetFieldWithLocalizedDigits(text, idx + 1, 2, 2, 0, MAX_OFFSET_MINUTE, len);
                if (len[0] == 0) {
                    break;
                }
                idx += (1 + len[0]);

                if (idx + 1 < max && text.charAt(idx) == separator) {
                    sec = parseOffsetFieldWithLocalizedDigits(text, idx + 1, 2, 2, 0, MAX_OFFSET_SECOND, len);
                    if (len[0] == 0) {
                        break;
                    }
                    idx += (1 + len[0]);
                }
            }
        } while (false);

        if (idx == start) {
            parsedLen[0] = 0;
            return 0;
        }

        parsedLen[0] = idx - start;
        return hour * MILLIS_PER_HOUR + min * MILLIS_PER_MINUTE + sec * MILLIS_PER_SECOND;
    }

    /**
     * Parses abutting localized GMT offset fields (such as 0800) into offset.
     * @param text the input text
     * @param start the start index
     * @param parsedLen the parsed length, or 0 on failure
     * @return the parsed offset in milliseconds.
     */
    private int parseAbuttingOffsetFields(String text, int start, int[] parsedLen) {
        final int MAXDIGITS = 6;
        int[] digits = new int[MAXDIGITS];
        int[] parsed = new int[MAXDIGITS];  // accumulative offsets

        // Parse digits into int[]
        int idx = start;
        int[] len = {0};
        int numDigits = 0;
        for (int i = 0; i < MAXDIGITS; i++) {
            digits[i] = parseSingleLocalizedDigit(text, idx, len);
            if (digits[i] < 0) {
                break;
            }
            idx += len[0];
            parsed[i] = idx - start;
            numDigits++;
        }

        if (numDigits == 0) {
            parsedLen[0] = 0;
            return 0;
        }

        int offset = 0;
        while (numDigits > 0) {
            int hour = 0;
            int min = 0;
            int sec = 0;

            assert(numDigits > 0 && numDigits <= 6);
            switch (numDigits) {
            case 1: // H
                hour = digits[0];
                break;
            case 2: // HH
                hour = digits[0] * 10 + digits[1];
                break;
            case 3: // Hmm
                hour = digits[0];
                min = digits[1] * 10 + digits[2];
                break;
            case 4: // HHmm
                hour = digits[0] * 10 + digits[1];
                min = digits[2] * 10 + digits[3];
                break;
            case 5: // Hmmss
                hour = digits[0];
                min = digits[1] * 10 + digits[2];
                sec = digits[3] * 10 + digits[4];
                break;
            case 6: // HHmmss
                hour = digits[0] * 10 + digits[1];
                min = digits[2] * 10 + digits[3];
                sec = digits[4] * 10 + digits[5];
                break;
            }
            if (hour <= MAX_OFFSET_HOUR && min <= MAX_OFFSET_MINUTE && sec <= MAX_OFFSET_SECOND) {
                // found a valid combination
                offset = hour * MILLIS_PER_HOUR + min * MILLIS_PER_MINUTE + sec * MILLIS_PER_SECOND;
                parsedLen[0] = parsed[numDigits - 1];
                break;
            }
            numDigits--;
        }
        return offset;
    }

    /**
     * Reads an offset field value. This method will stop parsing when
     * 1) number of digits reaches <code>maxDigits</code>
     * 2) just before already parsed number exceeds <code>maxVal</code>
     *
     * @param text the text
     * @param start the start offset
     * @param minDigits the minimum number of required digits
     * @param maxDigits the maximum number of digits
     * @param minVal the minimum value
     * @param maxVal the maximum value
     * @param parsedLen the actual parsed length is set to parsedLen[0], must not be null.
     * @return the integer value parsed
     */
    private int parseOffsetFieldWithLocalizedDigits(String text, int start, int minDigits, int maxDigits,
            int minVal, int maxVal, int[] parsedLen) {

        parsedLen[0] = 0;

        int decVal = 0;
        int numDigits = 0;
        int idx = start;
        int[] digitLen = {0};
        while (idx < text.length() && numDigits < maxDigits) {
            int digit = parseSingleLocalizedDigit(text, idx, digitLen);
            if (digit < 0) {
                break;
            }
            int tmpVal = decVal * 10 + digit;
            if (tmpVal > maxVal) {
                break;
            }
            decVal = tmpVal;
            numDigits++;
            idx += digitLen[0];
        }

        // Note: maxVal is checked in the while loop
        if (numDigits < minDigits || decVal < minVal) {
            decVal = -1;
            numDigits = 0;
        } else {
            parsedLen[0] = idx - start;
        }


        return decVal;
    }

    /**
     * Reads a single decimal digit, either localized digits used by this object
     * or any Unicode numeric character.
     * @param text the text
     * @param start the start index
     * @param len the actual length read from the text
     * the start index is not a decimal number.
     * @return the integer value of the parsed digit, or -1 on failure.
     */
    private int parseSingleLocalizedDigit(String text, int start, int[] len) {
        int digit = -1;
        len[0] = 0;
        if (start < text.length()) {
            int cp = Character.codePointAt(text, start);

            // First, try digits configured for this instance
            for (int i = 0; i < _gmtOffsetDigits.length; i++) {
                if (cp == _gmtOffsetDigits[i].codePointAt(0)) {
                    digit = i;
                    break;
                }
            }
            // If failed, check if this is a Unicode digit
            if (digit < 0) {
                digit = UCharacter.digit(cp);
            }

            if (digit >= 0) {
                len[0] = Character.charCount(cp);
            }
        }
        return digit;
    }

    /**
     * Break input String into String[]. Each array element represents
     * a code point. This method is used for parsing localized digit
     * characters and support characters in Unicode supplemental planes.
     *
     * @param str the string
     * @return the array of code points in String[]
     */
    private static String[] toCodePoints(String str) {
        int len = str.codePointCount(0, str.length());
        String[] codePoints = new String[len];

        for (int i = 0, offset = 0; i < len; i++) {
            int code = str.codePointAt(offset);
            int codeLen = Character.charCount(code);
            codePoints[i] = str.substring(offset, offset + codeLen);
            offset += codeLen;
        }
        return codePoints;
    }


    /**
     * Returns offset from GMT(UTC) in milliseconds for the given ISO 8601 time zone string
     * (basic format, extended format, or UTC indicator). When the given string is not an ISO 8601 time
     * zone string, this method sets the current position as the error index
     * to <code>ParsePosition pos</code> and returns 0.
     *
     * @param text the text contains ISO 8601 style time zone string (e.g. "-08", "-08:00", "Z")
     * at the position.
     * @param pos the position.
     * @param extendedOnly <code>true</code> if parsing the text as ISO 8601 extended offset format (e.g. "-08:00"),
     *                     or <code>false</code> to evaluate the text as basic format.
     * @param hasDigitOffset receiving if the parsed zone string contains offset digits.
     * @return the offset from GMT(UTC) in milliseconds for the given ISO 8601 style
     * time zone string.
     */
    private static int parseOffsetISO8601(String text, ParsePosition pos, boolean extendedOnly, Output<Boolean> hasDigitOffset) {
        if (hasDigitOffset != null) {
            hasDigitOffset.value = false;
        }
        int start = pos.getIndex();
        if (start >= text.length()) {
            pos.setErrorIndex(start);
            return 0;
        }

        char firstChar = text.charAt(start);
        if (Character.toUpperCase(firstChar) == ISO8601_UTC.charAt(0)) {
            // "Z" - indicates UTC
            pos.setIndex(start + 1);
            return 0;
        }

        int sign;
        if (firstChar == '+') {
            sign = 1;
        } else if (firstChar == '-') {
            sign = -1;
        } else {
            // Not an ISO 8601 offset string
            pos.setErrorIndex(start);
            return 0;
        }
        ParsePosition posOffset = new ParsePosition(start + 1);
        int offset = parseAsciiOffsetFields(text, posOffset, ':', OffsetFields.H, OffsetFields.HMS);
        if (posOffset.getErrorIndex() == -1 && !extendedOnly && (posOffset.getIndex() - start <= 3)) {
            // If the text is successfully parsed as extended format with the options above, it can be also parsed
            // as basic format. For example, "0230" can be parsed as offset 2:00 (only first digits are valid for
            // extended format), but it can be parsed as offset 2:30 with basic format. We use longer result.
            ParsePosition posBasic = new ParsePosition(start + 1);
            int tmpOffset = parseAbuttingAsciiOffsetFields(text, posBasic, OffsetFields.H, OffsetFields.HMS, false);
            if (posBasic.getErrorIndex() == -1 && posBasic.getIndex() > posOffset.getIndex()) {
                offset = tmpOffset;
                posOffset.setIndex(posBasic.getIndex());
            }
        }

        if (posOffset.getErrorIndex() != -1) {
            pos.setErrorIndex(start);
            return 0;
        }

        pos.setIndex(posOffset.getIndex());
        if (hasDigitOffset != null) {
            hasDigitOffset.value = true;
        }
        return sign * offset;
    }

    /**
     * Parses offset represented by contiguous ASCII digits
     * <p>
     * Note: This method expects the input position is already at the start of
     * ASCII digits and does not parse sign (+/-).
     *
     * @param text The text contains a sequence of ASCII digits
     * @param pos The parse position
     * @param minFields The minimum Fields to be parsed
     * @param maxFields The maximum Fields to be parsed
     * @param fixedHourWidth true if hours field must be width of 2
     * @return Parsed offset, 0 or positive number.
     */
    private static int parseAbuttingAsciiOffsetFields(String text, ParsePosition pos,
            OffsetFields minFields, OffsetFields maxFields, boolean fixedHourWidth) {
        int start = pos.getIndex();

        int minDigits = 2 * (minFields.ordinal() + 1) - (fixedHourWidth ? 0 : 1);
        int maxDigits = 2 * (maxFields.ordinal() + 1);

        int[] digits = new int[maxDigits];
        int numDigits = 0;
        int idx = start;
        while (numDigits < digits.length && idx < text.length()) {
            int digit = ASCII_DIGITS.indexOf(text.charAt(idx));
            if (digit < 0) {
                break;
            }
            digits[numDigits] = digit;
            numDigits++;
            idx++;
        }

        if (fixedHourWidth && ((numDigits & 1) != 0)) {
            // Fixed digits, so the number of digits must be even number. Truncating.
            numDigits--;
        }

        if (numDigits < minDigits) {
            pos.setErrorIndex(start);
            return 0;
        }

        int hour = 0, min = 0, sec = 0;
        boolean bParsed = false;
        while (numDigits >= minDigits) {
            switch (numDigits) {
            case 1: //H
                hour = digits[0];
                break;
            case 2: //HH
                hour = digits[0] * 10 + digits[1];
                break;
            case 3: //Hmm
                hour = digits[0];
                min = digits[1] * 10 + digits[2];
                break;
            case 4: //HHmm
                hour = digits[0] * 10 + digits[1];
                min = digits[2] * 10 + digits[3];
                break;
            case 5: //Hmmss
                hour = digits[0];
                min = digits[1] * 10 + digits[2];
                sec = digits[3] * 10 + digits[4];
                break;
            case 6: //HHmmss
                hour = digits[0] * 10 + digits[1];
                min = digits[2] * 10 + digits[3];
                sec = digits[4] * 10 + digits[5];
                break;
            }

            if (hour <= MAX_OFFSET_HOUR && min <= MAX_OFFSET_MINUTE && sec <= MAX_OFFSET_SECOND) {
                // Successfully parsed
                bParsed = true;
                break;
            }

            // Truncating
            numDigits -= (fixedHourWidth ? 2 : 1);
            hour = min = sec = 0;
        }

        if (!bParsed) {
            pos.setErrorIndex(start);
            return 0;
        }
        pos.setIndex(start + numDigits);
        return ((((hour * 60) + min) * 60) + sec) * 1000;
    }

    /**
     * Parses offset represented by ASCII digits and separators.
     * <p>
     * Note: This method expects the input position is already at the start of
     * ASCII digits and does not parse sign (+/-).
     *
     * @param text The text
     * @param pos The parse position
     * @param sep The separator character
     * @param minFields The minimum Fields to be parsed
     * @param maxFields The maximum Fields to be parsed
     * @return Parsed offset, 0 or positive number.
     */
    private static int parseAsciiOffsetFields(String text, ParsePosition pos, char sep,
            OffsetFields minFields, OffsetFields maxFields) {
        int start = pos.getIndex();
        int[] fieldVal = {0, 0, 0};
        int[] fieldLen = {0, -1, -1};
        for (int idx = start, fieldIdx = 0; idx < text.length() && fieldIdx <= maxFields.ordinal(); idx++) {
            char c = text.charAt(idx);
            if (c == sep) {
                if (fieldIdx == 0) {
                    if (fieldLen[0] == 0) {
                        // no hours field
                        break;
                    }
                    // 1 digit hour, move to next field
                    fieldIdx++;
                } else {
                    if (fieldLen[fieldIdx] != -1) {
                        // premature minutes or seconds field
                        break;
                    }
                    fieldLen[fieldIdx] = 0;
                }
                continue;
            } else if (fieldLen[fieldIdx] == -1) {
                // no separator after 2 digit field
                break;
            }
            int digit = ASCII_DIGITS.indexOf(c);
            if (digit < 0) {
                // not a digit
                break;
            }
            fieldVal[fieldIdx] = fieldVal[fieldIdx] * 10 + digit;
            fieldLen[fieldIdx]++;
            if (fieldLen[fieldIdx] >= 2) {
                // parsed 2 digits, move to next field
                fieldIdx++;
            }
        }

        int offset = 0;
        int parsedLen = 0;
        OffsetFields parsedFields = null;
        do {
            // hour
            if (fieldLen[0] == 0) {
                break;
            }
            if (fieldVal[0] > MAX_OFFSET_HOUR) {
                offset = (fieldVal[0] / 10) * MILLIS_PER_HOUR;
                parsedFields = OffsetFields.H;
                parsedLen = 1;
                break;
            }
            offset = fieldVal[0] * MILLIS_PER_HOUR;
            parsedLen = fieldLen[0];
            parsedFields = OffsetFields.H;

            // minute
            if (fieldLen[1] != 2 || fieldVal[1] > MAX_OFFSET_MINUTE) {
                break;
            }
            offset += fieldVal[1] * MILLIS_PER_MINUTE;
            parsedLen += (1 + fieldLen[1]);
            parsedFields = OffsetFields.HM;

            // second
            if (fieldLen[2] != 2 || fieldVal[2] > MAX_OFFSET_SECOND) {
                break;
            }
            offset += fieldVal[2] * MILLIS_PER_SECOND;
            parsedLen += (1 + fieldLen[2]);
            parsedFields = OffsetFields.HMS;
        } while (false);

        if (parsedFields == null || parsedFields.ordinal() < minFields.ordinal()) {
            pos.setErrorIndex(start);
            return 0;
        }

        pos.setIndex(start + parsedLen);
        return offset;
    }

    /**
     * Parse a zone ID.
     * @param text the text contains a time zone ID string at the position.
     * @param pos the position.
     * @return The zone ID parsed.
     */
    private static String parseZoneID(String text, ParsePosition pos) {
        String resolvedID = null;
        if (ZONE_ID_TRIE == null) {
            synchronized (TimeZoneFormat.class) {
                if (ZONE_ID_TRIE == null) {
                    // Build zone ID trie
                    TextTrieMap<String> trie = new TextTrieMap<String>(true);
                    String[] ids = TimeZone.getAvailableIDs();
                    for (String id : ids) {
                        trie.put(id, id);
                    }
                    ZONE_ID_TRIE = trie;
                }
            }
        }

        int[] matchLen = new int[] {0};
        Iterator<String> itr = ZONE_ID_TRIE.get(text, pos.getIndex(), matchLen);
        if (itr != null) {
            resolvedID = itr.next();
            pos.setIndex(pos.getIndex() + matchLen[0]);
        } else {
            // TODO
            // We many need to handle rule based custom zone ID (See ZoneMeta.parseCustomID),
            // such as GM+05:00. However, the public parse method in this class also calls
            // parseOffsetLocalizedGMT and custom zone IDs are likely supported by the parser,
            // so we might not need to handle them here.
            pos.setErrorIndex(pos.getIndex());
        }
        return resolvedID;
    }

    /**
     * Parse a short zone ID.
     * @param text the text contains a time zone ID string at the position.
     * @param pos the position.
     * @return The zone ID for the parsed short zone ID.
     */
    private static String parseShortZoneID(String text, ParsePosition pos) {
        String resolvedID = null;
        if (SHORT_ZONE_ID_TRIE == null) {
            synchronized (TimeZoneFormat.class) {
                if (SHORT_ZONE_ID_TRIE == null) {
                    // Build short zone ID trie
                    TextTrieMap<String> trie = new TextTrieMap<String>(true);
                    Set<String> canonicalIDs = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null);
                    for (String id : canonicalIDs) {
                        String shortID = ZoneMeta.getShortID(id);
                        if (shortID != null) {
                            trie.put(shortID, id);
                        }
                    }
                    // Canonical list does not contain Etc/Unknown
                    trie.put(UNKNOWN_SHORT_ZONE_ID, UNKNOWN_ZONE_ID);
                    SHORT_ZONE_ID_TRIE = trie;
                }
            }
        }

        int[] matchLen = new int[] {0};
        Iterator<String> itr = SHORT_ZONE_ID_TRIE.get(text, pos.getIndex(), matchLen);
        if (itr != null) {
            resolvedID = itr.next();
            pos.setIndex(pos.getIndex() + matchLen[0]);
        } else {
            pos.setErrorIndex(pos.getIndex());
        }

        return resolvedID;
    }

    /**
     * Parse an exemplar location string.
     * @param text the text contains an exemplar location string at the position.
     * @param pos the position.
     * @return The zone ID for the parsed exemplar location.
     */
    private String parseExemplarLocation(String text, ParsePosition pos) {
        int startIdx = pos.getIndex();
        int parsedPos = -1;
        String tzID = null;

        EnumSet<NameType> nameTypes = EnumSet.of(NameType.EXEMPLAR_LOCATION);
        Collection<MatchInfo> exemplarMatches = _tznames.find(text, startIdx, nameTypes);
        if (exemplarMatches != null) {
            MatchInfo exemplarMatch = null;
            for (MatchInfo match : exemplarMatches) {
                if (startIdx + match.matchLength() > parsedPos) {
                    exemplarMatch = match;
                    parsedPos = startIdx + match.matchLength();
                }
            }
            if (exemplarMatch != null) {
                tzID = getTimeZoneID(exemplarMatch.tzID(), exemplarMatch.mzID());
                pos.setIndex(parsedPos);
            }
        }
        if (tzID == null) {
            pos.setErrorIndex(startIdx);
        }

        return tzID;
    }

    /**
     * Implements <code>TimeZoneFormat</code> object cache
     */
    private static class TimeZoneFormatCache extends SoftCache<ULocale, TimeZoneFormat, ULocale> {

        /* (non-Javadoc)
         * @see android.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected TimeZoneFormat createInstance(ULocale key, ULocale data) {
            TimeZoneFormat fmt = new TimeZoneFormat(data);
            fmt.freeze();
            return fmt;
        }
    }

    // ----------------------------------
    // Serialization stuff
    //-----------------------------------

    /**
     * @serialField _locale ULocale The locale of this TimeZoneFormat object.
     * @serialField _tznames TimeZoneNames The time zone name data.
     * @serialField _gmtPattern String The pattern string for localized GMT format.
     * @serialField _gmtOffsetPatterns String[] The array of GMT offset patterns used by localized GMT format
     *              (positive hour-min, positive hour-min-sec, negative hour-min, negative hour-min-sec).
     * @serialField _gmtOffsetDigits String[] The array of decimal digits used by localized GMT format
     *              (the size of array is 10).
     * @serialField _gmtZeroFormat String The localized GMT string used for GMT(UTC).
     * @serialField _parseAllStyles boolean <code>true</code> if this TimeZoneFormat object is configure
     *              for parsing all available names.
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("_locale", ULocale.class),
        new ObjectStreamField("_tznames", TimeZoneNames.class),
        new ObjectStreamField("_gmtPattern", String.class),
        new ObjectStreamField("_gmtOffsetPatterns", String[].class),
        new ObjectStreamField("_gmtOffsetDigits", String[].class),
        new ObjectStreamField("_gmtZeroFormat", String.class),
        new ObjectStreamField("_parseAllStyles", boolean.class),
    };

    /**
     *
     * @param oos the object output stream
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        ObjectOutputStream.PutField fields = oos.putFields();

        fields.put("_locale", _locale);
        fields.put("_tznames", _tznames);
        fields.put("_gmtPattern", _gmtPattern);
        fields.put("_gmtOffsetPatterns", _gmtOffsetPatterns);
        fields.put("_gmtOffsetDigits", _gmtOffsetDigits);
        fields.put("_gmtZeroFormat", _gmtZeroFormat);
        fields.put("_parseAllStyles", _parseAllStyles);

        oos.writeFields();
    }

    /**
     *
     * @param ois the object input stream
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField fields = ois.readFields();

        _locale = (ULocale)fields.get("_locale", null);
        if (_locale == null) {
            throw new InvalidObjectException("Missing field: locale");
        }

        _tznames = (TimeZoneNames)fields.get("_tznames", null);
        if (_tznames == null) {
            throw new InvalidObjectException("Missing field: tznames");
        }

        _gmtPattern = (String)fields.get("_gmtPattern", null);
        if (_gmtPattern == null) {
            throw new InvalidObjectException("Missing field: gmtPattern");
        }

        String[] tmpGmtOffsetPatterns = (String[])fields.get("_gmtOffsetPatterns", null);
        if (tmpGmtOffsetPatterns == null) {
            throw new InvalidObjectException("Missing field: gmtOffsetPatterns");
        } else if (tmpGmtOffsetPatterns.length < 4) {
            throw new InvalidObjectException("Incompatible field: gmtOffsetPatterns");
        }
        _gmtOffsetPatterns = new String[6];
        if (tmpGmtOffsetPatterns.length == 4) {
            for (int i = 0; i < 4; i++) {
                _gmtOffsetPatterns[i] = tmpGmtOffsetPatterns[i];
            }
            _gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_H.ordinal()] = truncateOffsetPattern(_gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HM.ordinal()]);
            _gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_H.ordinal()] = truncateOffsetPattern(_gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HM.ordinal()]);
        } else {
            _gmtOffsetPatterns = tmpGmtOffsetPatterns;
        }

        _gmtOffsetDigits = (String[])fields.get("_gmtOffsetDigits", null);
        if (_gmtOffsetDigits == null) {
            throw new InvalidObjectException("Missing field: gmtOffsetDigits");
        } else if (_gmtOffsetDigits.length != 10) {
            throw new InvalidObjectException("Incompatible field: gmtOffsetDigits");
        }

        _gmtZeroFormat = (String)fields.get("_gmtZeroFormat", null);
        if (_gmtZeroFormat == null) {
            throw new InvalidObjectException("Missing field: gmtZeroFormat");
        }

        _parseAllStyles = fields.get("_parseAllStyles", false);
        if (fields.defaulted("_parseAllStyles")) {
            throw new InvalidObjectException("Missing field: parseAllStyles");
        }

        // Optimization for TimeZoneNames
        //
        // Note:
        //
        // android.icu.impl.TimeZoneNamesImpl is a read-only object initialized
        // by locale only. But it loads time zone names from resource bundles and
        // builds trie for parsing. We want to keep TimeZoneNamesImpl as singleton
        // per locale. We cannot do this for custom TimeZoneNames provided by user.
        //
        // android.icu.impl.TimeZoneGenericNames is a runtime generated object
        // initialized by ULocale and TimeZoneNames. Like TimeZoneNamesImpl, it
        // also composes time zone names and trie for parsing. We also want to keep
        // TimeZoneGenericNames as siongleton per locale. If TimeZoneNames is
        // actually a TimeZoneNamesImpl, we can reuse cached TimeZoneGenericNames
        // instance.
        if (_tznames instanceof TimeZoneNamesImpl) {
            _tznames = TimeZoneNames.getInstance(_locale);
            _gnames = null; // will be created by _locale later when necessary
        } else {
            // Custom TimeZoneNames implementation is used. We need to create
            // a new instance of TimeZoneGenericNames here.
            _gnames = new TimeZoneGenericNames(_locale, _tznames);
        }

        // Transient fields requiring initialization
        initGMTPattern(_gmtPattern);
        initGMTOffsetPatterns(_gmtOffsetPatterns);

    }

    // ----------------------------------
    // Freezable stuff
    //-----------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFrozen() {
        return _frozen;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZoneFormat freeze() {
        _frozen = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZoneFormat cloneAsThawed() {
        TimeZoneFormat copy = (TimeZoneFormat)super.clone();
        copy._frozen = false;
        return copy;
    }
}

