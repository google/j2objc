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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

import libcore.icu.ICU;
import libcore.icu.LocaleData;
import libcore.icu.TimeZoneNames;

/**
 * Encapsulates localized date-time formatting data, such as the names of the
 * months, the names of the days of the week, and the time zone data.
 * {@code DateFormat} and {@code SimpleDateFormat} both use
 * {@code DateFormatSymbols} to encapsulate this information.
 *
 * <p>Typically you shouldn't use {@code DateFormatSymbols} directly. Rather, you
 * are encouraged to create a date/time formatter with the {@code DateFormat}
 * class's factory methods: {@code getTimeInstance}, {@code getDateInstance},
 * or {@code getDateTimeInstance}. These methods automatically create a
 * {@code DateFormatSymbols} for the formatter so that you don't have to. After
 * the formatter is created, you may modify its format pattern using the
 * {@code setPattern} method. For more information about creating formatters
 * using {@code DateFormat}'s factory methods, see {@link DateFormat}.
 *
 * <p>Direct use of {@code DateFormatSymbols} is likely to be less efficient
 * because the implementation cannot make assumptions about user-supplied/user-modifiable data
 * to the same extent that it can with its own built-in data.
 *
 * @see DateFormat
 * @see SimpleDateFormat
 */
public class DateFormatSymbols implements Serializable, Cloneable {

    private static final long serialVersionUID = -5987973545549424702L;

    private String localPatternChars;

    String[] ampms, eras, months, shortMonths, shortWeekdays, weekdays;

    // This is used to implement parts of Unicode UTS #35 not historically supported.
    transient LocaleData localeData;

    // Localized display names.
    String[][] zoneStrings;
    // Has the user called setZoneStrings?
    transient boolean customZoneStrings;

    /**
     * Locale, necessary to lazily load time zone strings. We force the time
     * zone names to load upon serialization, so this will never be needed
     * post deserialization.
     */
    transient final Locale locale;

    /**
     * Gets zone strings, initializing them if necessary. Does not create
     * a defensive copy, so make sure you do so before exposing the returned
     * arrays to clients.
     */
    synchronized String[][] internalZoneStrings() {
        if (zoneStrings == null) {
            zoneStrings = TimeZoneNames.getZoneStrings(locale);
        }
        return zoneStrings;
    }

    /**
     * Constructs a new {@code DateFormatSymbols} instance containing the
     * symbols for the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     */
    public DateFormatSymbols() {
        this(Locale.getDefault());
    }

    /**
     * Constructs a new {@code DateFormatSymbols} instance containing the
     * symbols for the specified locale.
     *
     * @param locale
     *            the locale.
     */
    public DateFormatSymbols(Locale locale) {
        this.locale = locale;
        this.localPatternChars = SimpleDateFormat.PATTERN_CHARS;

        this.localeData = LocaleData.get(locale);
        this.ampms = localeData.amPm;
        this.eras = localeData.eras;
        this.months = localeData.longMonthNames;
        this.shortMonths = localeData.shortMonthNames;
        this.weekdays = localeData.longWeekdayNames;
        this.shortWeekdays = localeData.shortWeekdayNames;
    }

    /**
     * Returns a new {@code DateFormatSymbols} instance for the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @return an instance of {@code DateFormatSymbols}
     * @since 1.6
     */
    public static final DateFormatSymbols getInstance() {
        return getInstance(Locale.getDefault());
    }

    /**
     * Returns a new {@code DateFormatSymbols} for the given locale.
     *
     * @param locale the locale
     * @return an instance of {@code DateFormatSymbols}
     * @throws NullPointerException if {@code locale == null}
     * @since 1.6
     */
    public static final DateFormatSymbols getInstance(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }
        return new DateFormatSymbols(locale);
    }

    /**
     * Returns an array of locales for which custom {@code DateFormatSymbols} instances
     * are available.
     * <p>Note that Android does not support user-supplied locale service providers.
     * @since 1.6
     */
    public static Locale[] getAvailableLocales() {
        return ICU.getAvailableDateFormatSymbolsLocales();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.localeData = LocaleData.get(locale);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        internalZoneStrings();
        oos.defaultWriteObject();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Compares this object with the specified object and indicates if they are
     * equal.
     *
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if {@code object} is an instance of
     *         {@code DateFormatSymbols} and has the same symbols as this
     *         object, {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DateFormatSymbols)) {
            return false;
        }
        DateFormatSymbols rhs = (DateFormatSymbols) object;
        return localPatternChars.equals(rhs.localPatternChars) &&
                Arrays.equals(ampms, rhs.ampms) &&
                Arrays.equals(eras, rhs.eras) &&
                Arrays.equals(months, rhs.months) &&
                Arrays.equals(shortMonths, rhs.shortMonths) &&
                Arrays.equals(shortWeekdays, rhs.shortWeekdays) &&
                Arrays.equals(weekdays, rhs.weekdays) &&
                timeZoneStringsEqual(this, rhs);
    }

    private static boolean timeZoneStringsEqual(DateFormatSymbols lhs, DateFormatSymbols rhs) {
        // Quick check that may keep us from having to load the zone strings.
        // Note that different locales may have the same strings, so the opposite check isn't valid.
        if (lhs.zoneStrings == null && rhs.zoneStrings == null && lhs.locale.equals(rhs.locale)) {
            return true;
        }
        // Make sure zone strings are loaded, then check.
        return Arrays.deepEquals(lhs.internalZoneStrings(), rhs.internalZoneStrings());
    }

    @Override
    public String toString() {
        // 'locale' isn't part of the externally-visible state.
        // 'zoneStrings' is so large, we just print a representative value.
        return getClass().getName() +
                "[amPmStrings=" + Arrays.toString(ampms) +
                ",customZoneStrings=" + customZoneStrings +
                ",eras=" + Arrays.toString(eras) +
                ",localPatternChars=" + localPatternChars +
                ",months=" + Arrays.toString(months) +
                ",shortMonths=" + Arrays.toString(shortMonths) +
                ",shortWeekdays=" + Arrays.toString(shortWeekdays) +
                ",weekdays=" + Arrays.toString(weekdays) +
                ",zoneStrings=[" + Arrays.toString(internalZoneStrings()[0]) + "...]" +
                "]";
    }

    /**
     * Returns the array of strings which represent AM and PM. Use the
     * {@link java.util.Calendar} constants {@code Calendar.AM} and
     * {@code Calendar.PM} as indices for the array.
     *
     * @return an array of strings.
     */
    public String[] getAmPmStrings() {
        return ampms.clone();
    }

    /**
     * Returns the array of strings which represent BC and AD. Use the
     * {@link java.util.Calendar} constants {@code GregorianCalendar.BC} and
     * {@code GregorianCalendar.AD} as indices for the array.
     *
     * @return an array of strings.
     */
    public String[] getEras() {
        return eras.clone();
    }

    /**
     * Returns the pattern characters used by {@link SimpleDateFormat} to
     * specify date and time fields.
     *
     * @return a string containing the pattern characters.
     */
    public String getLocalPatternChars() {
        return localPatternChars;
    }

    /**
     * Returns the array of strings containing the full names of the months. Use
     * the {@link java.util.Calendar} constants {@code Calendar.JANUARY} etc. as
     * indices for the array.
     *
     * @return an array of strings.
     */
    public String[] getMonths() {
        return months.clone();
    }

    /**
     * Returns the array of strings containing the abbreviated names of the
     * months. Use the {@link java.util.Calendar} constants
     * {@code Calendar.JANUARY} etc. as indices for the array.
     *
     * @return an array of strings.
     */
    public String[] getShortMonths() {
        return shortMonths.clone();
    }

    /**
     * Returns the array of strings containing the abbreviated names of the days
     * of the week. Use the {@link java.util.Calendar} constants
     * {@code Calendar.SUNDAY} etc. as indices for the array.
     *
     * @return an array of strings.
     */
    public String[] getShortWeekdays() {
        return shortWeekdays.clone();
    }

    /**
     * Returns the array of strings containing the full names of the days of the
     * week. Use the {@link java.util.Calendar} constants
     * {@code Calendar.SUNDAY} etc. as indices for the array.
     *
     * @return an array of strings.
     */
    public String[] getWeekdays() {
        return weekdays.clone();
    }

    /**
     * Returns the two-dimensional array of strings containing localized names for time zones.
     * Each row is an array of five strings:
     * <ul>
     * <li>The time zone ID, for example "America/Los_Angeles".
     *     This is not localized, and is used as a key into the table.
     * <li>The long display name, for example "Pacific Standard Time".
     * <li>The short display name, for example "PST".
     * <li>The long display name for DST, for example "Pacific Daylight Time".
     *     This is the non-DST long name for zones that have never had DST, for
     *     example "Central Standard Time" for "Canada/Saskatchewan".
     * <li>The short display name for DST, for example "PDT". This is the
     *     non-DST short name for zones that have never had DST, for example
     *     "CST" for "Canada/Saskatchewan".
     * </ul>
     */
    public String[][] getZoneStrings() {
        String[][] result = clone2dStringArray(internalZoneStrings());
        // If icu4c doesn't have a name, our array contains a null. TimeZone.getDisplayName
        for (String[] zone : result) {
            String id = zone[0];
            if (zone[1] == null) {
                zone[1] = TimeZone.getTimeZone(id).getDisplayName(false, TimeZone.LONG, locale);
            }
            if (zone[2] == null) {
                zone[2] = TimeZone.getTimeZone(id).getDisplayName(false, TimeZone.SHORT, locale);
            }
            if (zone[3] == null) {
                zone[3] = TimeZone.getTimeZone(id).getDisplayName(true, TimeZone.LONG, locale);
            }
            if (zone[4] == null) {
                zone[4] = TimeZone.getTimeZone(id).getDisplayName(true, TimeZone.SHORT, locale);
            }
        }
        return result;
    }

    private static String[][] clone2dStringArray(String[][] array) {
        String[][] result = new String[array.length][];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i].clone();
        }
        return result;
    }

    @Override
    public int hashCode() {
        String[][] zoneStrings = internalZoneStrings();
        int hashCode;
        hashCode = localPatternChars.hashCode();
        for (String element : ampms) {
            hashCode += element.hashCode();
        }
        for (String element : eras) {
            hashCode += element.hashCode();
        }
        for (String element : months) {
            hashCode += element.hashCode();
        }
        for (String element : shortMonths) {
            hashCode += element.hashCode();
        }
        for (String element : shortWeekdays) {
            hashCode += element.hashCode();
        }
        for (String element : weekdays) {
            hashCode += element.hashCode();
        }
        for (String[] element : zoneStrings) {
            for (int j = 0; j < element.length; j++) {
                if (element[j] != null) {
                    hashCode += element[j].hashCode();
                }
            }
        }
        return hashCode;
    }

    /**
     * Sets the array of strings which represent AM and PM. Use the
     * {@link java.util.Calendar} constants {@code Calendar.AM} and
     * {@code Calendar.PM} as indices for the array.
     *
     * @param data
     *            the array of strings for AM and PM.
     */
    public void setAmPmStrings(String[] data) {
        ampms = data.clone();
    }

    /**
     * Sets the array of Strings which represent BC and AD. Use the
     * {@link java.util.Calendar} constants {@code GregorianCalendar.BC} and
     * {@code GregorianCalendar.AD} as indices for the array.
     *
     * @param data
     *            the array of strings for BC and AD.
     */
    public void setEras(String[] data) {
        eras = data.clone();
    }

    /**
     * Sets the pattern characters used by {@link SimpleDateFormat} to specify
     * date and time fields.
     *
     * @param data
     *            the string containing the pattern characters.
     * @throws NullPointerException
     *            if {@code data} is null
     */
    public void setLocalPatternChars(String data) {
        if (data == null) {
            throw new NullPointerException("data == null");
        }
        localPatternChars = data;
    }

    /**
     * Sets the array of strings containing the full names of the months. Use
     * the {@link java.util.Calendar} constants {@code Calendar.JANUARY} etc. as
     * indices for the array.
     *
     * @param data
     *            the array of strings.
     */
    public void setMonths(String[] data) {
        months = data.clone();
    }

    /**
     * Sets the array of strings containing the abbreviated names of the months.
     * Use the {@link java.util.Calendar} constants {@code Calendar.JANUARY}
     * etc. as indices for the array.
     *
     * @param data
     *            the array of strings.
     */
    public void setShortMonths(String[] data) {
        shortMonths = data.clone();
    }

    /**
     * Sets the array of strings containing the abbreviated names of the days of
     * the week. Use the {@link java.util.Calendar} constants
     * {@code Calendar.SUNDAY} etc. as indices for the array.
     *
     * @param data
     *            the array of strings.
     */
    public void setShortWeekdays(String[] data) {
        shortWeekdays = data.clone();
    }

    /**
     * Sets the array of strings containing the full names of the days of the
     * week. Use the {@link java.util.Calendar} constants
     * {@code Calendar.SUNDAY} etc. as indices for the array.
     *
     * @param data
     *            the array of strings.
     */
    public void setWeekdays(String[] data) {
        weekdays = data.clone();
    }

    /**
     * Sets the two-dimensional array of strings containing localized names for time zones.
     * See {@link #getZoneStrings} for details.
     * @throws IllegalArgumentException if any row has fewer than 5 elements.
     * @throws NullPointerException if {@code zoneStrings == null}.
     */
    public void setZoneStrings(String[][] zoneStrings) {
        if (zoneStrings == null) {
            throw new NullPointerException("zoneStrings == null");
        }
        for (String[] row : zoneStrings) {
            if (row.length < 5) {
                throw new IllegalArgumentException(Arrays.toString(row) + ".length < 5");
            }
        }
        this.zoneStrings = clone2dStringArray(zoneStrings);
        this.customZoneStrings = true;
    }

    /**
     * Returns the display name of the timezone specified. Returns null if no name was found in the
     * zone strings.
     *
     * @param daylight whether to return the daylight savings or the standard name
     * @param style one of the {@link TimeZone} styles such as {@link TimeZone#SHORT}
     *
     * @hide used internally
     */
    String getTimeZoneDisplayName(TimeZone tz, boolean daylight, int style) {
        if (style != TimeZone.SHORT && style != TimeZone.LONG) {
            throw new IllegalArgumentException("Bad style: " + style);
        }

        // If custom zone strings have been set with setZoneStrings() we use those. Otherwise we
        // use the ones from LocaleData.
        String[][] zoneStrings = internalZoneStrings();
        return TimeZoneNames.getDisplayName(zoneStrings, tz.getID(), daylight, style);
    }
}
