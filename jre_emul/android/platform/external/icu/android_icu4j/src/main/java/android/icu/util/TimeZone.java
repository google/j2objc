/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 * @(#)TimeZone.java    1.51 00/01/19
 *
 * Copyright (C) 1996-2016, International Business Machines
 * Corporation and others.  All Rights Reserved.
 */

package android.icu.util;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Logger;

import android.icu.impl.Grego;
import android.icu.impl.ICUConfig;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.JavaTimeZone;
import android.icu.impl.TimeZoneAdapter;
import android.icu.impl.ZoneMeta;
import android.icu.text.TimeZoneFormat;
import android.icu.text.TimeZoneFormat.Style;
import android.icu.text.TimeZoneFormat.TimeType;
import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.ULocale.Category;

/**
 * <strong>[icu enhancement]</strong> ICU's replacement for {@link java.util.TimeZone}.&nbsp;Methods, fields, and other functionality specific to ICU are labeled '<strong>[icu]</strong>'.
 *
 * <p><code>TimeZone</code> represents a time zone offset, and also computes daylight
 * savings.
 *
 * <p>Typically, you get a <code>TimeZone</code> using {@link #getDefault()}
 * which creates a <code>TimeZone</code> based on the time zone where the program
 * is running. For example, for a program running in Japan, <code>getDefault</code>
 * creates a <code>TimeZone</code> object based on Japanese Standard Time.
 *
 * <p>You can also get a <code>TimeZone</code> using {@link #getTimeZone(String)}
 * along with a time zone ID. For instance, the time zone ID for the
 * U.S. Pacific Time zone is "America/Los_Angeles". So, you can get a
 * U.S. Pacific Time <code>TimeZone</code> object with:
 *
 * <blockquote>
 * <pre>
 * TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
 * </pre>
 * </blockquote>
 * You can use the {@link #getAvailableIDs()} method to iterate through
 * all the supported time zone IDs, or getCanonicalID method to check
 * if a time zone ID is supported or not. You can then choose a
 * supported ID to get a <code>TimeZone</code>.
 * If the time zone you want is not represented by one of the
 * supported IDs, then you can create a custom time zone ID with
 * the following syntax:
 *
 * <blockquote>
 * <pre>
 * GMT[+|-]hh[[:]mm]
 * </pre>
 * </blockquote>
 *
 * For example, you might specify GMT+14:00 as a custom
 * time zone ID.  The <code>TimeZone</code> that is returned
 * when you specify a custom time zone ID uses the specified
 * offset from GMT(=UTC) and does not observe daylight saving
 * time. For example, you might specify GMT+14:00 as a custom
 * time zone ID to create a TimeZone representing 14 hours ahead
 * of GMT (with no daylight saving time). In addition,
 * <code>getCanonicalID</code> can also be used to
 * normalize a custom time zone ID.
 *
 * <p>For compatibility with JDK 1.1.x, some other three-letter time zone IDs
 * (such as "PST", "CTT", "AST") are also supported. However, <strong>their
 * use is deprecated</strong> because the same abbreviation is often used
 * for multiple time zones (for example, "CST" could be U.S. "Central Standard
 * Time" and "China Standard Time"), and the Java platform can then only
 * recognize one of them.
 *
 * @see          Calendar
 * @see          GregorianCalendar
 * @see          SimpleTimeZone
 * @author       Mark Davis, Deborah Goldsmith, Chen-Lieh Huang, Alan Liu
 */
abstract public class TimeZone implements Serializable, Cloneable, Freezable<TimeZone> {
    /**
     * Logger instance for this class
     */
    private static final Logger LOGGER = Logger.getLogger("android.icu.util.TimeZone");

    // using serialver from jdk1.4.2_05
    private static final long serialVersionUID = -744942128318337471L;

    /**
     * Default constructor.  (For invocation by subclass constructors,
     * typically implicit.)
     */
    public TimeZone() {
    }

    /**
     * Constructing a TimeZone with the given time zone ID.
     * @param ID the time zone ID.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected TimeZone(String ID) {
        if (ID == null) {
            throw new NullPointerException();
        }
        this.ID = ID;
    }

    /**
     * <strong>[icu]</strong> A time zone implementation type indicating ICU's own TimeZone used by
     * <code>getTimeZone</code>.
     */
    public static final int TIMEZONE_ICU = 0;
    /**
     * <strong>[icu]</strong> A time zone implementation type indicating the {@link java.util.TimeZone}
     * used by <code>getTimeZone</code>.
     */
    public static final int TIMEZONE_JDK = 1;

    /**
     * A style specifier for <code>getDisplayName()</code> indicating
     * a short name, such as "PST."
     * @see #LONG
     */
    public static final int SHORT = 0;

    /**
     * A style specifier for <code>getDisplayName()</code> indicating
     * a long name, such as "Pacific Standard Time."
     * @see #SHORT
     */
    public static final int LONG  = 1;

    /**
     * <strong>[icu]</strong> A style specifier for <code>getDisplayName()</code> indicating
     * a short generic name, such as "PT."
     * @see #LONG_GENERIC
     */
    public static final int SHORT_GENERIC = 2;

    /**
     * <strong>[icu]</strong> A style specifier for <code>getDisplayName()</code> indicating
     * a long generic name, such as "Pacific Time."
     * @see #SHORT_GENERIC
     */
    public static final int LONG_GENERIC = 3;

    /**
     * <strong>[icu]</strong> A style specifier for <code>getDisplayName()</code> indicating
     * a short name derived from the timezone's offset, such as "-0800."
     * @see #LONG_GMT
     */
    public static final int SHORT_GMT = 4;

    /**
     * <strong>[icu]</strong> A style specifier for <code>getDisplayName()</code> indicating
     * a long name derived from the timezone's offset, such as "GMT-08:00."
     * @see #SHORT_GMT
     */
    public static final int LONG_GMT = 5;

    /**
     * <strong>[icu]</strong> A style specifier for <code>getDisplayName()</code> indicating
     * a short name derived from the timezone's short standard or daylight
     * timezone name ignoring commonlyUsed, such as "PDT."
     */

    public static final int SHORT_COMMONLY_USED = 6;

    /**
     * <strong>[icu]</strong> A style specifier for <code>getDisplayName()</code> indicating
     * a long name derived from the timezone's fallback name, such as
     * "United States (Los Angeles)."
     */
    public static final int GENERIC_LOCATION = 7;

    /**
     * <strong>[icu]</strong> The time zone ID reserved for unknown time zone.
     * @see #getTimeZone(String)
     */
    public static final String UNKNOWN_ZONE_ID = "Etc/Unknown";

    /**
     * The canonical ID for GMT(UTC) time zone.
     */
    static final String GMT_ZONE_ID = "Etc/GMT";

    /**
     * <strong>[icu]</strong> The immutable (frozen) "unknown" time zone.
     * It behaves like the GMT/UTC time zone but has the UNKNOWN_ZONE_ID = "Etc/Unknown".
     * {@link TimeZone#getTimeZone(String)} returns a mutable clone of this
     * time zone if the input ID is not recognized.
     *
     * @see #UNKNOWN_ZONE_ID
     * @see #getTimeZone(String)
     */
    public static final TimeZone UNKNOWN_ZONE = new ConstantZone(0, UNKNOWN_ZONE_ID).freeze();

    /**
     * <strong>[icu]</strong> The immutable GMT (=UTC) time zone. Its ID is "Etc/GMT".
     */
    public static final TimeZone GMT_ZONE = new ConstantZone(0, GMT_ZONE_ID).freeze();

    /**
     * <strong>[icu]</strong> System time zone type constants used by filtering zones in
     * {@link TimeZone#getAvailableIDs(SystemTimeZoneType, String, Integer)}
     */
    public enum SystemTimeZoneType {
        /**
         * Any system zones.
         * @hide draft / provisional / internal are hidden on Android
         */
        ANY,

        /**
         * Canonical system zones.
         * @hide draft / provisional / internal are hidden on Android
         */
        CANONICAL,

        /**
         * Canonical system zones associated with actual locations.
         * @hide draft / provisional / internal are hidden on Android
         */
        CANONICAL_LOCATION,
    }

    /**
     * Gets the time zone offset, for current date, modified in case of
     * daylight savings. This is the offset to add *to* UTC to get local time.
     * @param era the era of the given date.
     * @param year the year in the given date.
     * @param month the month in the given date.
     * Month is 0-based. e.g., 0 for January.
     * @param day the day-in-month of the given date.
     * @param dayOfWeek the day-of-week of the given date.
     * @param milliseconds the millis in day in <em>standard</em> local time.
     * @return the offset to add *to* GMT to get local time.
     */
    abstract public int getOffset(int era, int year, int month, int day,
                                  int dayOfWeek, int milliseconds);


    /**
     * Returns the offset of this time zone from UTC at the specified
     * date. If Daylight Saving Time is in effect at the specified
     * date, the offset value is adjusted with the amount of daylight
     * saving.
     *
     * @param date the date represented in milliseconds since January 1, 1970 00:00:00 GMT
     * @return the amount of time in milliseconds to add to UTC to get local time.
     *
     * @see Calendar#ZONE_OFFSET
     * @see Calendar#DST_OFFSET
     * @see #getOffset(long, boolean, int[])
     */
    public int getOffset(long date) {
        int[] result = new int[2];
        getOffset(date, false, result);
        return result[0]+result[1];
    }

    /**
     * Returns the time zone raw and GMT offset for the given moment
     * in time.  Upon return, local-millis = GMT-millis + rawOffset +
     * dstOffset.  All computations are performed in the proleptic
     * Gregorian calendar.  The default implementation in the TimeZone
     * class delegates to the 8-argument getOffset().
     *
     * @param date moment in time for which to return offsets, in
     * units of milliseconds from January 1, 1970 0:00 GMT, either GMT
     * time or local wall time, depending on `local'.
     * @param local if true, `date' is local wall time; otherwise it
     * is in GMT time.
     * @param offsets output parameter to receive the raw offset, that
     * is, the offset not including DST adjustments, in offsets[0],
     * and the DST offset, that is, the offset to be added to
     * `rawOffset' to obtain the total offset between local and GMT
     * time, in offsets[1]. If DST is not in effect, the DST offset is
     * zero; otherwise it is a positive value, typically one hour.
     */
    public void getOffset(long date, boolean local, int[] offsets) {
        offsets[0] = getRawOffset();
        if (!local) {
            date += offsets[0]; // now in local standard millis
        }

        // When local == true, date might not be in local standard
        // millis.  getOffset taking 6 parameters used here assume
        // the given time in day is local standard time.
        // At STD->DST transition, there is a range of time which
        // does not exist.  When 'date' is in this time range
        // (and local == true), this method interprets the specified
        // local time as DST.  At DST->STD transition, there is a
        // range of time which occurs twice.  In this case, this
        // method interprets the specified local time as STD.
        // To support the behavior above, we need to call getOffset
        // (with 6 args) twice when local == true and DST is
        // detected in the initial call.
        int fields[] = new int[6];
        for (int pass = 0; ; pass++) {
            Grego.timeToFields(date, fields);
            offsets[1] = getOffset(GregorianCalendar.AD,
                                    fields[0], fields[1], fields[2],
                                    fields[3], fields[5]) - offsets[0];

            if (pass != 0 || !local || offsets[1] == 0) {
                break;
            }
            // adjust to local standard millis
            date -= offsets[1];
        }
    }

    /**
     * Sets the base time zone offset to GMT.
     * This is the offset to add *to* UTC to get local time.
     * @param offsetMillis the given base time zone offset to GMT.
     */
    abstract public void setRawOffset(int offsetMillis);

    /**
     * Gets unmodified offset, NOT modified in case of daylight savings.
     * This is the offset to add *to* UTC to get local time.
     * @return the unmodified offset to add *to* UTC to get local time.
     */
    abstract public int getRawOffset();

    /**
     * Gets the ID of this time zone.
     * @return the ID of this time zone.
     */
    public String getID() {
        return ID;
    }

    /**
     * Sets the time zone ID. This does not change any other data in
     * the time zone object.
     * @param ID the new time zone ID.
     */
    public void setID(String ID) {
        if (ID == null) {
            throw new NullPointerException();
        }
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen TimeZone instance.");
        }
        this.ID = ID;
    }

    /**
     * Returns a name of this time zone suitable for presentation to the user
     * in the default <code>DISPLAY</code> locale.
     * This method returns the long generic name.
     * If the display name is not available for the locale,
     * a fallback based on the country, city, or time zone id will be used.
     * @return the human-readable name of this time zone in the default locale.
     * @see Category#DISPLAY
     */
    public final String getDisplayName() {
        return _getDisplayName(LONG_GENERIC, false, ULocale.getDefault(Category.DISPLAY));
    }

    /**
     * Returns a name of this time zone suitable for presentation to the user
     * in the specified locale.
     * This method returns the long generic name.
     * If the display name is not available for the locale,
     * a fallback based on the country, city, or time zone id will be used.
     * @param locale the locale in which to supply the display name.
     * @return the human-readable name of this time zone in the given locale
     * or in the default locale if the given locale is not recognized.
     */
    public final String getDisplayName(Locale locale) {
        return _getDisplayName(LONG_GENERIC, false, ULocale.forLocale(locale));
    }

    /**
     * Returns a name of this time zone suitable for presentation to the user
     * in the specified locale.
     * This method returns the long name, not including daylight savings.
     * If the display name is not available for the locale,
     * a fallback based on the country, city, or time zone id will be used.
     * @param locale the ulocale in which to supply the display name.
     * @return the human-readable name of this time zone in the given locale
     * or in the default ulocale if the given ulocale is not recognized.
     */
    public final String getDisplayName(ULocale locale) {
        return _getDisplayName(LONG_GENERIC, false, locale);
    }

    /**
     * Returns a name of this time zone suitable for presentation to the user
     * in the default <code>DISPLAY</code> locale.
     * If the display name is not available for the locale,
     * then this method returns a string in the localized GMT offset format
     * such as <code>GMT[+-]HH:mm</code>.
     * @param daylight if true, return the daylight savings name.
     * @param style the output style of the display name.  Valid styles are
     * <code>SHORT</code>, <code>LONG</code>, <code>SHORT_GENERIC</code>,
     * <code>LONG_GENERIC</code>, <code>SHORT_GMT</code>, <code>LONG_GMT</code>,
     * <code>SHORT_COMMONLY_USED</code> or <code>GENERIC_LOCATION</code>.
     * @return the human-readable name of this time zone in the default locale.
     * @see Category#DISPLAY
     */
    public final String getDisplayName(boolean daylight, int style) {
        return getDisplayName(daylight, style, ULocale.getDefault(Category.DISPLAY));
    }

    /**
     * Returns a name of this time zone suitable for presentation to the user
     * in the specified locale.
     * If the display name is not available for the locale,
     * then this method returns a string in the localized GMT offset format
     * such as <code>GMT[+-]HH:mm</code>.
     * @param daylight if true, return the daylight savings name.
     * @param style the output style of the display name.  Valid styles are
     * <code>SHORT</code>, <code>LONG</code>, <code>SHORT_GENERIC</code>,
     * <code>LONG_GENERIC</code>, <code>SHORT_GMT</code>, <code>LONG_GMT</code>,
     * <code>SHORT_COMMONLY_USED</code> or <code>GENERIC_LOCATION</code>.
     * @param locale the locale in which to supply the display name.
     * @return the human-readable name of this time zone in the given locale
     * or in the default locale if the given locale is not recognized.
     * @exception IllegalArgumentException style is invalid.
     */
    public String getDisplayName(boolean daylight, int style, Locale locale) {
        return getDisplayName(daylight, style, ULocale.forLocale(locale));
    }

    /**
     * Returns a name of this time zone suitable for presentation to the user
     * in the specified locale.
     * If the display name is not available for the locale,
     * then this method returns a string in the localized GMT offset format
     * such as <code>GMT[+-]HH:mm</code>.
     * @param daylight if true, return the daylight savings name.
     * @param style the output style of the display name.  Valid styles are
     * <code>SHORT</code>, <code>LONG</code>, <code>SHORT_GENERIC</code>,
     * <code>LONG_GENERIC</code>, <code>SHORT_GMT</code>, <code>LONG_GMT</code>,
     * <code>SHORT_COMMONLY_USED</code> or <code>GENERIC_LOCATION</code>.
     * @param locale the locale in which to supply the display name.
     * @return the human-readable name of this time zone in the given locale
     * or in the default locale if the given locale is not recognized.
     * @exception IllegalArgumentException style is invalid.
     */
    public String getDisplayName(boolean daylight, int style, ULocale locale) {
        if (style < SHORT || style > GENERIC_LOCATION) {
            throw new IllegalArgumentException("Illegal style: " + style);
        }

        return _getDisplayName(style, daylight, locale);
    }

    /**
     * internal version (which is called by public APIs) accepts
     * SHORT, LONG, SHORT_GENERIC, LONG_GENERIC, SHORT_GMT, LONG_GMT,
     * SHORT_COMMONLY_USED and GENERIC_LOCATION.
     */
    private String _getDisplayName(int style, boolean daylight, ULocale locale) {
        if (locale == null) {
            throw new NullPointerException("locale is null");
        }

        String result = null;

        if (style == GENERIC_LOCATION || style == LONG_GENERIC || style == SHORT_GENERIC) {
            // Generic format
            TimeZoneFormat tzfmt = TimeZoneFormat.getInstance(locale);
            long date = System.currentTimeMillis();
            Output<TimeType> timeType = new Output<TimeType>(TimeType.UNKNOWN);

            switch (style) {
            case GENERIC_LOCATION:
                result = tzfmt.format(Style.GENERIC_LOCATION, this, date, timeType);
                break;
            case LONG_GENERIC:
                result = tzfmt.format(Style.GENERIC_LONG, this, date, timeType);
                break;
            case SHORT_GENERIC:
                result = tzfmt.format(Style.GENERIC_SHORT, this, date, timeType);
                break;
            }

            // Generic format many use Localized GMT as the final fallback.
            // When Localized GMT format is used, the result might not be
            // appropriate for the requested daylight value.
            if (daylight && timeType.value == TimeType.STANDARD ||
                    !daylight && timeType.value == TimeType.DAYLIGHT) {
                int offset = daylight ? getRawOffset() + getDSTSavings() : getRawOffset();
                result = (style == SHORT_GENERIC) ?
                        tzfmt.formatOffsetShortLocalizedGMT(offset) : tzfmt.formatOffsetLocalizedGMT(offset);
            }

        } else if (style == LONG_GMT || style == SHORT_GMT) {
            // Offset format
            TimeZoneFormat tzfmt = TimeZoneFormat.getInstance(locale);
            int offset = daylight && useDaylightTime() ? getRawOffset() + getDSTSavings() : getRawOffset();
            switch (style) {
            case LONG_GMT:
                result = tzfmt.formatOffsetLocalizedGMT(offset);
                break;
            case SHORT_GMT:
                result = tzfmt.formatOffsetISO8601Basic(offset, false, false, false);
                break;
            }
        } else {
            // Specific format
            assert(style == LONG || style == SHORT || style == SHORT_COMMONLY_USED);

            // Gets the name directly from TimeZoneNames
            long date = System.currentTimeMillis();
            TimeZoneNames tznames = TimeZoneNames.getInstance(locale);
            NameType nameType = null;
            switch (style) {
            case LONG:
                nameType = daylight ? NameType.LONG_DAYLIGHT : NameType.LONG_STANDARD;
                break;
            case SHORT:
            case SHORT_COMMONLY_USED:
                nameType = daylight ? NameType.SHORT_DAYLIGHT : NameType.SHORT_STANDARD;
                break;
            }
            result = tznames.getDisplayName(ZoneMeta.getCanonicalCLDRID(this), nameType, date);
            if (result == null) {
                // Fallback to localized GMT
                TimeZoneFormat tzfmt = TimeZoneFormat.getInstance(locale);
                int offset = daylight && useDaylightTime() ? getRawOffset() + getDSTSavings() : getRawOffset();
                result = (style == LONG) ?
                        tzfmt.formatOffsetLocalizedGMT(offset) : tzfmt.formatOffsetShortLocalizedGMT(offset);
            }
        }
        assert(result != null);

        return result;
    }

    /**
     * Returns the amount of time to be added to local standard time
     * to get local wall clock time.
     * <p>
     * The default implementation always returns 3600000 milliseconds
     * (i.e., one hour) if this time zone observes Daylight Saving
     * Time. Otherwise, 0 (zero) is returned.
     * <p>
     * If an underlying TimeZone implementation subclass supports
     * historical Daylight Saving Time changes, this method returns
     * the known latest daylight saving value.
     *
     * @return the amount of saving time in milliseconds
     */
    public int getDSTSavings() {
        if (useDaylightTime()) {
            return 3600000;
        }
        return 0;
    }

    /**
     * Queries if this time zone uses daylight savings time.
     * @return true if this time zone uses daylight savings time,
     * false, otherwise.
     * <p><strong>Note:</strong>The default implementation of
     * ICU TimeZone uses the tz database, which supports historic
     * rule changes, for system time zones. With the implementation,
     * there are time zones that used daylight savings time in the
     * past, but no longer used currently. For example, Asia/Tokyo has
     * never used daylight savings time since 1951. Most clients would
     * expect that this method to return <code>false</code> for such case.
     * The default implementation of this method returns <code>true</code>
     * when the time zone uses daylight savings time in the current
     * (Gregorian) calendar year.
     */
    abstract public boolean useDaylightTime();

    /**
     * Queries if this time zone is in daylight saving time or will observe
     * daylight saving time at any future time.
     * <p>The default implementation in this class returns <code>true</code> if {@link #useDaylightTime()}
     * or {@link #inDaylightTime(Date) inDaylightTime(new Date())} returns <code>true</code>.
     * <p>
     * <strong>Note:</strong> This method was added for {@link java.util.TimeZone} compatibility
     * support. The {@link java.util.TimeZone#useDaylightTime()} method only checks the last known
     * rule(s), therefore it may return false even the zone observes daylight saving time currently.
     * {@link java.util.TimeZone} added <code>observesDaylightTime()</code> to resolve the issue.
     * In ICU, {@link #useDaylightTime()} works differently. The ICU implementation checks if the
     * zone uses daylight saving time in the current calendar year. Therefore, it will never return
     * <code>false</code> if daylight saving time is currently used.
     * <p>
     * ICU's TimeZone subclass implementations override this method to support the same behavior
     * with {@link java.util.TimeZone#observesDaylightTime()}. Unlike {@link #useDaylightTime()},
     * the implementation does not take past daylight saving time into account, so
     * that this method may return <code>false</code> even when {@link #useDaylightTime()} returns
     * <code>true</code>.
     *
     * @return <code>true</code> if this time zone is in daylight saving time or will observe
     * daylight saving time at any future time.
     * @see #useDaylightTime
     */
    @SuppressWarnings("javadoc")    // java.util.TimeZone#observesDaylightTime() is introduced in Java 7
    public boolean observesDaylightTime() {
        return useDaylightTime() || inDaylightTime(new Date());
    }

    /**
     * Queries if the given date is in daylight savings time in
     * this time zone.
     * @param date the given Date.
     * @return true if the given date is in daylight savings time,
     * false, otherwise.
     */
    abstract public boolean inDaylightTime(Date date);

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     *
     * @param ID the ID for a <code>TimeZone</code>, such as "America/Los_Angeles",
     * or a custom ID such as "GMT-8:00". Note that the support of abbreviations,
     * such as "PST", is for JDK 1.1.x compatibility only and full names should be used.
     *
     * @return the specified <code>TimeZone</code>, or a mutable clone of the UNKNOWN_ZONE
     * if the given ID cannot be understood or if the given ID is "Etc/Unknown".
     * @see #UNKNOWN_ZONE
     */
    public static TimeZone getTimeZone(String ID) {
        return getTimeZone(ID, TZ_IMPL, false);
    }

    /**
     * Gets the <code>TimeZone</code> for the given ID. The instance of <code>TimeZone</code>
     * returned by this method is immutable. Any methods mutate the instance({@link #setID(String)},
     * {@link #setRawOffset(int)}) will throw <code>UnsupportedOperationException</code> upon its
     * invocation.
     *
     * @param ID the ID for a <code>TimeZone</code>, such as "America/Los_Angeles",
     * or a custom ID such as "GMT-8:00". Note that the support of abbreviations,
     * such as "PST", is for JDK 1.1.x compatibility only and full names should be used.
     *
     * @return the specified <code>TimeZone</code>, or the UNKNOWN_ZONE
     * if the given ID cannot be understood.
     * @see #UNKNOWN_ZONE
     */
    public static TimeZone getFrozenTimeZone(String ID) {
        return getTimeZone(ID, TZ_IMPL, true);
    }

    /**
     * Gets the <code>TimeZone</code> for the given ID and the timezone type.
     * @param ID the ID for a <code>TimeZone</code>, such as "America/Los_Angeles", or a
     * custom ID such as "GMT-8:00". Note that the support of abbreviations, such as
     * "PST", is for JDK 1.1.x compatibility only and full names should be used.
     * @param type Time zone type, either <code>TIMEZONE_ICU</code> or
     * <code>TIMEZONE_JDK</code>.
     * @return the specified <code>TimeZone</code>, or a mutable clone of the UNKNOWN_ZONE if the given ID
     * cannot be understood or if the given ID is "Etc/Unknown".
     * @see #UNKNOWN_ZONE
     */
    public static TimeZone getTimeZone(String ID, int type) {
        return getTimeZone(ID, type, false);
    }

    /**
     * Gets the <code>TimeZone</code> for the given ID and the timezone type.
     * @param id time zone ID
     * @param type time zone implementation type, TIMEZONE_JDK or TIMEZONE_ICU
     * @param frozen specify if the returned object can be frozen
     * @return the specified <code>TimeZone</code> or UNKNOWN_ZONE if the given ID
     * cannot be understood.
     */
    private static TimeZone getTimeZone(String id, int type, boolean frozen) {
        TimeZone result;
        if (type == TIMEZONE_JDK) {
            result = JavaTimeZone.createTimeZone(id);
            if (result != null) {
                return frozen ? result.freeze() : result;
            }
            result = getFrozenICUTimeZone(id, false);
        } else {
            result = getFrozenICUTimeZone(id, true);
        }
        if (result == null) {
            LOGGER.fine("\"" +id + "\" is a bogus id so timezone is falling back to Etc/Unknown(GMT).");
            result = UNKNOWN_ZONE;
        }
        return frozen ? result : result.cloneAsThawed();
    }

    /**
     * Returns a frozen ICU type TimeZone object given a time zone ID.
     * @param id the time zone ID
     * @param trySystem if true tries the system time zones first otherwise skip to the
     *   custom time zones.
     * @return the frozen ICU TimeZone or null if one could not be created.
     */
    static BasicTimeZone getFrozenICUTimeZone(String id, boolean trySystem) {
        BasicTimeZone result = null;
        if (trySystem) {
            result = ZoneMeta.getSystemTimeZone(id);
        }
        if (result == null) {
            result = ZoneMeta.getCustomTimeZone(id);
        }
        return result;
    }

    /**
     * Sets the default time zone type used by <code>getTimeZone</code>.
     * @param type time zone type, either <code>TIMEZONE_ICU</code> or
     * <code>TIMEZONE_JDK</code>.
     * @hide unsupported on Android
     */
    public static synchronized void setDefaultTimeZoneType(int type) {
        if (type != TIMEZONE_ICU && type != TIMEZONE_JDK) {
            throw new IllegalArgumentException("Invalid timezone type");
        }
        TZ_IMPL = type;
    }

    /**
     * <strong>[icu]</strong> Returns the default time zone type currently used.
     * @return The default time zone type, either <code>TIMEZONE_ICU</code> or
     * <code>TIMEZONE_JDK</code>.
     * @hide unsupported on Android
     */
    public static int getDefaultTimeZoneType() {
        return TZ_IMPL;
    }

    /**
     * <strong>[icu]</strong> Returns a set of time zone ID strings with the given filter conditions.
     * <p><b>Note:</b>A <code>Set</code> returned by this method is
     * immutable.
     * @param zoneType      The system time zone type.
     * @param region        The ISO 3166 two-letter country code or UN M.49 three-digit area code.
     *                      When null, no filtering done by region.
     * @param rawOffset     An offset from GMT in milliseconds, ignoring the effect of daylight savings
     *                      time, if any. When null, no filtering done by zone offset.
     * @return an immutable set of system time zone IDs.
     * @see SystemTimeZoneType
     */
    public static Set<String> getAvailableIDs(SystemTimeZoneType zoneType,
            String region, Integer rawOffset) {
        return ZoneMeta.getAvailableIDs(zoneType, region, rawOffset);
    }

    /**
     * Return a new String array containing all system TimeZone IDs
     * with the given raw offset from GMT.  These IDs may be passed to
     * <code>get()</code> to construct the corresponding TimeZone
     * object.
     * @param rawOffset the offset in milliseconds from GMT
     * @return an array of IDs for system TimeZones with the given
     * raw offset.  If there are none, return a zero-length array.
     * @see #getAvailableIDs(SystemTimeZoneType, String, Integer)
     */
    public static String[] getAvailableIDs(int rawOffset) {
        Set<String> ids = getAvailableIDs(SystemTimeZoneType.ANY, null, Integer.valueOf(rawOffset));
        return ids.toArray(new String[0]);
    }

    /**
     * Return a new String array containing all system TimeZone IDs
     * associated with the given country.  These IDs may be passed to
     * <code>get()</code> to construct the corresponding TimeZone
     * object.
     * @param country a two-letter ISO 3166 country code, or <code>null</code>
     * to return zones not associated with any country
     * @return an array of IDs for system TimeZones in the given
     * country.  If there are none, return a zero-length array.
     * @see #getAvailableIDs(SystemTimeZoneType, String, Integer)
     */
    public static String[] getAvailableIDs(String country) {
        Set<String> ids = getAvailableIDs(SystemTimeZoneType.ANY, country, null);
        return ids.toArray(new String[0]);
    }

    /**
     * Return a new String array containing all system TimeZone IDs.
     * These IDs (and only these IDs) may be passed to
     * <code>get()</code> to construct the corresponding TimeZone
     * object.
     * @return an array of all system TimeZone IDs
     * @see #getAvailableIDs(SystemTimeZoneType, String, Integer)
     */
    public static String[] getAvailableIDs() {
        Set<String> ids = getAvailableIDs(SystemTimeZoneType.ANY, null, null);
        return ids.toArray(new String[0]);
    }

    /**
     * <strong>[icu]</strong> Returns the number of IDs in the equivalency group that
     * includes the given ID.  An equivalency group contains zones
     * that have the same GMT offset and rules.
     *
     * <p>The returned count includes the given ID; it is always &gt;= 1
     * for valid IDs.  The given ID must be a system time zone.  If it
     * is not, returns zero.
     * @param id a system time zone ID
     * @return the number of zones in the equivalency group containing
     * 'id', or zero if 'id' is not a valid system ID
     * @see #getEquivalentID
     */
    public static int countEquivalentIDs(String id) {
        return ZoneMeta.countEquivalentIDs(id);
    }

    /**
     * Returns an ID in the equivalency group that
     * includes the given ID.  An equivalency group contains zones
     * that have the same GMT offset and rules.
     *
     * <p>The given index must be in the range 0..n-1, where n is the
     * value returned by <code>countEquivalentIDs(id)</code>.  For
     * some value of 'index', the returned value will be equal to the
     * given id.  If the given id is not a valid system time zone, or
     * if 'index' is out of range, then returns an empty string.
     * @param id a system time zone ID
     * @param index a value from 0 to n-1, where n is the value
     * returned by <code>countEquivalentIDs(id)</code>
     * @return the ID of the index-th zone in the equivalency group
     * containing 'id', or an empty string if 'id' is not a valid
     * system ID or 'index' is out of range
     * @see #countEquivalentIDs
     */
    public static String getEquivalentID(String id, int index) {
        return ZoneMeta.getEquivalentID(id, index);
    }

    /**
     * Gets the default <code>TimeZone</code> for this host.
     * The source of the default <code>TimeZone</code>
     * may vary with implementation.
     * @return a default <code>TimeZone</code>.
     */
    public static TimeZone getDefault() {
        // Android patch (http://b/30979219) start.
        // Avoid race condition by copying defaultZone to a local variable.
        TimeZone result = defaultZone;
        if (result == null) {
            // Android patch (http://b/30937209) start.
            // Avoid a deadlock by always acquiring monitors in order (1) java.util.TimeZone.class
            // then (2) icu.util.TimeZone.class and not (2) then (1).
            // Without the synchronized here there is a possible deadlock between threads calling
            // this method and other threads calling methods on java.util.TimeZone. e.g.
            // java.util.TimeZone.setDefault() calls back into
            // icu.util.TimeZone.clearCachedDefault() so always acquires them in order (1) then (2).
            synchronized (java.util.TimeZone.class) {
                synchronized (TimeZone.class) {
                    result = defaultZone;
                    if (result == null) {
                        if (TZ_IMPL == TIMEZONE_JDK) {
                            result = new JavaTimeZone();
                        } else {
                            java.util.TimeZone temp = java.util.TimeZone.getDefault();
                            result = getFrozenTimeZone(temp.getID());
                        }
                        defaultZone = result;
                    }
                }
            }
            // Android patch (http://b/30937209) end.
        }
        return result.cloneAsThawed();
        // Android patch (http://b/30979219) end.
    }

    // Android patch (http://b/28949992) start.
    // ICU TimeZone.setDefault() not supported on Android.
    /**
     * Clears the cached default time zone.
     * This causes {@link #getDefault()} to re-request the default time zone
     * from {@link java.util.TimeZone}.
     * @hide unsupported on Android
     */
    public static synchronized void clearCachedDefault() {
        defaultZone = null;
    }
    // Android patch (http://b/28949992) end.

    /**
     * Sets the <code>TimeZone</code> that is
     * returned by the <code>getDefault</code> method.  If <code>zone</code>
     * is null, reset the default to the value it had originally when the
     * VM first started.
     * @param tz the new default time zone
     * @hide unsupported on Android
     */
    public static synchronized void setDefault(TimeZone tz) {
        defaultZone = tz;
        java.util.TimeZone jdkZone = null;
        if (defaultZone instanceof JavaTimeZone) {
            jdkZone = ((JavaTimeZone)defaultZone).unwrap();
        } else {
            // Keep java.util.TimeZone default in sync so java.util.Date
            // can interoperate with android.icu.util classes.

            if (tz != null) {
                if (tz instanceof android.icu.impl.OlsonTimeZone) {
                    // Because of the lack of APIs supporting historic
                    // zone offset/dst saving in JDK TimeZone,
                    // wrapping ICU TimeZone with JDK TimeZone will
                    // cause historic offset calculation in Calendar/Date.
                    // JDK calendar implementation calls getRawOffset() and
                    // getDSTSavings() when the instance of JDK TimeZone
                    // is not an instance of JDK internal TimeZone subclass
                    // (sun.util.calendar.ZoneInfo).  Ticket#6459
                    String icuID = tz.getID();
                    jdkZone = java.util.TimeZone.getTimeZone(icuID);
                    if (!icuID.equals(jdkZone.getID())) {
                        // If the ID was unknown, retry with the canonicalized
                        // ID instead. This will ensure that JDK 1.1.x
                        // compatibility IDs supported by ICU (but not
                        // necessarily supported by the platform) work.
                        // Ticket#11483
                        icuID = getCanonicalID(icuID);
                        jdkZone = java.util.TimeZone.getTimeZone(icuID);
                        if (!icuID.equals(jdkZone.getID())) {
                            // JDK does not know the ID..
                            jdkZone = null;
                        }
                    }
                }
                if (jdkZone == null) {
                    jdkZone = TimeZoneAdapter.wrap(tz);
                }
            }
        }
        java.util.TimeZone.setDefault(jdkZone);
    }

    /**
     * Returns true if this zone has the same rule and offset as another zone.
     * That is, if this zone differs only in ID, if at all.  Returns false
     * if the other zone is null.
     * @param other the <code>TimeZone</code> object to be compared with
     * @return true if the other zone is not null and is the same as this one,
     * with the possible exception of the ID
     */
    public boolean hasSameRules(TimeZone other) {
        return other != null &&
            getRawOffset() == other.getRawOffset() &&
            useDaylightTime() == other.useDaylightTime();
    }

    /**
     * Overrides clone.
     */
    @Override
    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    /**
     * Overrides equals.
     */
    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return (ID.equals(((TimeZone)obj).ID));
    }

    /**
     * Overrides hashCode.
     */
    @Override
    public int hashCode(){
        return ID.hashCode();
    }

    /**
     * <strong>[icu]</strong> Returns the time zone data version currently used by ICU.
     *
     * @return the version string, such as "2007f"
     * @throws MissingResourceException if ICU time zone resource bundle
     * is missing or the version information is not available.
     */
    public static String getTZDataVersion() {
        // The implementation had been moved to VersionInfo.
        return VersionInfo.getTZDataVersion();
    }

    /**
     * <strong>[icu]</strong> Returns the canonical system time zone ID or the normalized
     * custom time zone ID for the given time zone ID.
     * @param id The input time zone ID to be canonicalized.
     * @return The canonical system time zone ID or the custom time zone ID
     * in normalized format for the given time zone ID.  When the given time zone ID
     * is neither a known system time zone ID nor a valid custom time zone ID,
     * null is returned.
     */
    public static String getCanonicalID(String id) {
        return getCanonicalID(id, null);
    }

    /**
     * <strong>[icu]</strong> Returns the canonical system time zone ID or the normalized
     * custom time zone ID for the given time zone ID.
     * @param id The input time zone ID to be canonicalized.
     * @param isSystemID When non-null boolean array is specified and
     * the given ID is a known system time zone ID, true is set to <code>isSystemID[0]</code>
     * @return The canonical system time zone ID or the custom time zone ID
     * in normalized format for the given time zone ID.  When the given time zone ID
     * is neither a known system time zone ID nor a valid custom time zone ID,
     * null is returned.
     */
    public static String getCanonicalID(String id, boolean[] isSystemID) {
        String canonicalID = null;
        boolean systemTzid = false;
        if (id != null && id.length() != 0) {
            if (id.equals(TimeZone.UNKNOWN_ZONE_ID)) {
                // special case - Etc/Unknown is a canonical ID, but not system ID
                canonicalID = TimeZone.UNKNOWN_ZONE_ID;
                systemTzid = false;
            } else {
                canonicalID = ZoneMeta.getCanonicalCLDRID(id);
                if (canonicalID != null) {
                    systemTzid = true;
                } else {
                    canonicalID = ZoneMeta.getCustomID(id);
                }
            }
        }
        if (isSystemID != null) {
            isSystemID[0] = systemTzid;
        }
        return canonicalID;
    }

    /**
     * <strong>[icu]</strong> Returns the region code associated with the given
     * system time zone ID. The region code is either ISO 3166
     * 2-letter country code or UN M.49 3-digit area code.
     * When the time zone is not associated with a specific location,
     * for example - "Etc/UTC", "EST5EDT", then this method returns
     * "001" (UN M.49 area code for World).
     * @param id the system time zone ID.
     * @return the region code associated with the given
     * system time zone ID.
     * @throws IllegalArgumentException if <code>id</code> is not a known system ID.
     * @see #getAvailableIDs(String)
     */
    public static String getRegion(String id) {
        String region = null;
        // "Etc/Unknown" is not a system time zone ID,
        // but in the zone data.
        if (!id.equals(UNKNOWN_ZONE_ID)) {
            region = ZoneMeta.getRegion(id);
        }
        if (region == null) {
            // unknown id
            throw new IllegalArgumentException("Unknown system zone id: " + id);
        }
        return region;
    }

    /**
     * <strong>[icu]</strong> Converts a system time zone ID to an equivalent Windows time zone ID. For example,
     * Windows time zone ID "Pacific Standard Time" is returned for input "America/Los_Angeles".
     *
     * <p>There are system time zones that cannot be mapped to Windows zones. When the input
     * system time zone ID is unknown or unmappable to a Windows time zone, then this
     * method returns <code>null</code>.
     *
     * <p>This implementation utilizes <a href="http://unicode.org/cldr/charts/supplemental/zone_tzid.html">
     * Zone-Tzid mapping data</a>. The mapping data is updated time to time. To get the latest changes,
     * please read the ICU user guide section <a href="http://userguide.icu-project.org/datetime/timezone#TOC-Updating-the-Time-Zone-Data">
     * Updating the Time Zone Data</a>.
     *
     * @param id A system time zone ID
     * @return A Windows time zone ID mapped from the input system time zone ID,
     * or <code>null</code> when the input ID is unknown or unmappable.
     * @see #getIDForWindowsID(String, String)
     */
    public static String getWindowsID(String id) {
        // canonicalize the input ID
        boolean[] isSystemID = {false};
        id = getCanonicalID(id, isSystemID);
        if (!isSystemID[0]) {
            // mapping data is only applicable to tz database IDs
            return null;
        }

        UResourceBundle top = UResourceBundle.getBundleInstance(
                ICUData.ICU_BASE_NAME, "windowsZones", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        UResourceBundle mapTimezones = top.get("mapTimezones");

        UResourceBundleIterator resitr = mapTimezones.getIterator();
        while (resitr.hasNext()) {
            UResourceBundle winzone = resitr.next();
            if (winzone.getType() != UResourceBundle.TABLE) {
                continue;
            }
            UResourceBundleIterator rgitr = winzone.getIterator();
            while (rgitr.hasNext()) {
                UResourceBundle regionalData = rgitr.next();
                if (regionalData.getType() != UResourceBundle.STRING) {
                    continue;
                }
                String[] tzids = regionalData.getString().split(" ");
                for (String tzid : tzids) {
                    if (tzid.equals(id)) {
                        return winzone.getKey();
                    }
                }
            }
        }

        return null;
    }

    /**
     * <strong>[icu]</strong> Converts a Windows time zone ID to an equivalent system time zone ID
     * for a region. For example, system time zone ID "America/Los_Angeles" is returned
     * for input Windows ID "Pacific Standard Time" and region "US" (or <code>null</code>),
     * "America/Vancouver" is returned for the same Windows ID "Pacific Standard Time" and
     * region "CA".
     *
     * <p>Not all Windows time zones can be mapped to system time zones. When the input
     * Windows time zone ID is unknown or unmappable to a system time zone, then this
     * method returns <code>null</code>.
     *
     * <p>This implementation utilizes <a href="http://unicode.org/cldr/charts/supplemental/zone_tzid.html">
     * Zone-Tzid mapping data</a>. The mapping data is updated time to time. To get the latest changes,
     * please read the ICU user guide section <a href="http://userguide.icu-project.org/datetime/timezone#TOC-Updating-the-Time-Zone-Data">
     * Updating the Time Zone Data</a>.
     *
     * @param winid A Windows time zone ID
     * @param region A region code, or <code>null</code> if no regional preference.
     * @return A system time zone ID mapped from the input Windows time zone ID,
     * or <code>null</code> when the input ID is unknown or unmappable.
     * @see #getWindowsID(String)
     */
    public static String getIDForWindowsID(String winid, String region) {
        String id = null;

        UResourceBundle top = UResourceBundle.getBundleInstance(
                ICUData.ICU_BASE_NAME, "windowsZones", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        UResourceBundle mapTimezones = top.get("mapTimezones");

        try {
            UResourceBundle zones = mapTimezones.get(winid);
            if (region != null) {
                try {
                    id = zones.getString(region);
                    if (id != null) {
                        // first ID delimited by space is the default one
                        int endIdx = id.indexOf(' ');
                        if (endIdx > 0) {
                            id = id.substring(0, endIdx);
                        }
                    }
                } catch (MissingResourceException e) {
                    // no explicit region mapping found
                }
            }
            if (id == null) {
                id = zones.getString("001");
            }
        } catch (MissingResourceException e) {
            // no mapping data found
        }

        return id;
    }

    // Freezable stuffs

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFrozen() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZone freeze() {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZone cloneAsThawed() {
        try {
            TimeZone other = (TimeZone) super.clone();
            return other;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    // =======================privates===============================

    /**
     * The string identifier of this <code>TimeZone</code>.  This is a
     * programmatic identifier used internally to look up <code>TimeZone</code>
     * objects from the system table and also to map them to their localized
     * display names.  <code>ID</code> values are unique in the system
     * table but may not be for dynamically created zones.
     * @serial
     */
    private String           ID;

    /**
     * The default time zone, or null if not set.
     */
    private static volatile TimeZone  defaultZone = null;

    /**
     * TimeZone implementation type
     */
    private static int TZ_IMPL = TIMEZONE_ICU;

    /**
     * TimeZone implementation type initialization
     */
    private static final String TZIMPL_CONFIG_KEY = "android.icu.util.TimeZone.DefaultTimeZoneType";
    private static final String TZIMPL_CONFIG_ICU = "ICU";
    private static final String TZIMPL_CONFIG_JDK = "JDK";

    static {
        String type = ICUConfig.get(TZIMPL_CONFIG_KEY, TZIMPL_CONFIG_ICU);
        if (type.equalsIgnoreCase(TZIMPL_CONFIG_JDK)) {
            TZ_IMPL = TIMEZONE_JDK;
        }
    }

    /*
     * ConstantZone is a private TimeZone subclass dedicated for the two TimeZone class
     * constants - TimeZone.GMT_ZONE and TimeZone.UNKNOWN_ZONE. Previously, these zones
     * are instances of SimpleTimeZone. However, when the SimpleTimeZone constructor and
     * TimeZone's static methods (such as TimeZone.getDefault()) are called from multiple
     * threads at the same time, it causes a deadlock by TimeZone's static initializer
     * and SimpleTimeZone's static initializer. To avoid this issue, these TimeZone
     * constants (GMT/UNKNOWN) must be implemented by a class not visible from users.
     * See ticket#11343.
     */
    private static final class ConstantZone extends TimeZone {
        private static final long serialVersionUID = 1L;

        private int rawOffset;

        private ConstantZone(int rawOffset, String ID) {
            super(ID);
            this.rawOffset = rawOffset;
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
            return rawOffset;
        }

        @Override
        public void setRawOffset(int offsetMillis) {
            if (isFrozen()) {
                throw new UnsupportedOperationException("Attempt to modify a frozen TimeZone instance.");
            }
            rawOffset = offsetMillis;
        }

        @Override
        public int getRawOffset() {
            return rawOffset;
        }

        @Override
        public boolean useDaylightTime() {
            return false;
        }

        @Override
        public boolean inDaylightTime(Date date) {
            return false;
        }

        private volatile transient boolean isFrozen = false;

        @Override
        public boolean isFrozen() {
            return isFrozen;
        }

        @Override
        public TimeZone freeze() {
            isFrozen = true;
            return this;
        }

        @Override
        public TimeZone cloneAsThawed() {
            ConstantZone tz = (ConstantZone)super.cloneAsThawed();
            tz.isFrozen = false;
            return tz;
        }
    }
}

//eof
