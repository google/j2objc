/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.util.locale.provider;

import android.icu.text.DateFormatSymbols;
import android.icu.util.ULocale;

import static java.util.Calendar.*;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@code CalendarDataUtility} is a utility class for getting calendar field name values.
 *
 * @author Masayoshi Okutsu
 * @author Naoto Sato
 */
public class CalendarDataUtility {

    private static final String ISLAMIC_CALENDAR = "islamic";
    private static final String GREGORIAN_CALENDAR = "gregorian";
    private static final String BUDDHIST_CALENDAR = "buddhist";
    private static final String JAPANESE_CALENDAR = "japanese";

    // No instantiation
    private CalendarDataUtility() {
    }

    // Android-changed: Removed retrieveFirstDayOfWeek and retrieveMinimalDaysInFirstWeek.
    // use libcore.icu.LocaleData or android.icu.util.Calendar.WeekData instead

    public static String retrieveFieldValueName(String id, int field, int value, int style,
            Locale locale) {
        // Android-changed: delegate to ICU.
        if (field == Calendar.ERA) {
            // For era the field value does not always equal the index into the names array.
            switch (normalizeCalendarType(id)) {
                // These calendars have only one era, but represented it by the value 1.
                case BUDDHIST_CALENDAR:
                case ISLAMIC_CALENDAR:
                    value -= 1;
                    break;
                case JAPANESE_CALENDAR:
                    // CLDR contains full data for historical eras, java.time only supports the 4
                    // modern eras and numbers the modern eras starting with 1 (MEIJI). There are
                    // 232 historical eras in CLDR/ICU so to get the real offset, we add 231.
                    value += 231;
                    break;
                default:
                    // Other eras use 0-based values (e.g. 0=BCE, 1=CE for gregorian).
                    break;
            }
        }
        if (value < 0) {
            return null;
        }
        String[] names = getNames(id, field, style, locale);
        if (value >= names.length) {
            return null;
        }
        return names[value];
    }

    public static String retrieveJavaTimeFieldValueName(String id, int field, int value, int style,
            Locale locale) {
        // Android-changed: don't distinguish between retrieve* and retrieveJavaTime* methods.
        return retrieveFieldValueName(id, field, value, style, locale);
    }

    // ALL_STYLES implies SHORT_FORMAT and all of these values.
    private static int[] REST_OF_STYLES = {
            SHORT_STANDALONE, LONG_FORMAT, LONG_STANDALONE,
            NARROW_FORMAT, NARROW_STANDALONE
    };

    public static Map<String, Integer> retrieveFieldValueNames(String id, int field, int style,
            Locale locale) {
        // Android-changed: delegate to ICU.
        Map<String, Integer> names;
        if (style == ALL_STYLES) {
            names = retrieveFieldValueNamesImpl(id, field, SHORT_FORMAT, locale);
            for (int st : REST_OF_STYLES) {
                names.putAll(retrieveFieldValueNamesImpl(id, field, st, locale));
            }
        } else {
            // specific style
            names = retrieveFieldValueNamesImpl(id, field, style, locale);
        }
        return names.isEmpty() ? null : names;
    }

    private static Map<String, Integer> retrieveFieldValueNamesImpl(String id, int field, int style,
            Locale locale) {
        String[] names = getNames(id, field, style, locale);
        int skipped = 0;
        int offset = 0;
        if (field == Calendar.ERA) {
            // See retrieveFieldValueName() for explanation of this code and the values used.
            switch (normalizeCalendarType(id)) {
                case BUDDHIST_CALENDAR:
                case ISLAMIC_CALENDAR:
                    offset = 1;
                    break;
                case JAPANESE_CALENDAR:
                    skipped = 232;
                    offset = -231;
                    break;
                default:
                    break;
            }
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int i = skipped; i < names.length; i++) {
            if (names[i].isEmpty()) {
                continue;
            }

            if (result.put(names[i], i + offset) != null) {
                // Duplicate names indicate that the names would be ambiguous. Skip this style for
                // ALL_STYLES. In other cases this results in null being returned in
                // retrieveValueNames(), which is required by Calendar.getDisplayNames().
                return new LinkedHashMap<>();
            }
        }
        return result;
    }

    public static Map<String, Integer> retrieveJavaTimeFieldValueNames(String id, int field,
            int style, Locale locale) {
        // Android-changed: don't distinguish between retrieve* and retrieveJavaTime* methods.
        return retrieveFieldValueNames(id, field, style, locale);
    }

    private static String[] getNames(String id, int field, int style, Locale locale) {
        int context = toContext(style);
        int width = toWidth(style);
        DateFormatSymbols symbols = getDateFormatSymbols(id, locale);
        switch (field) {
            case Calendar.MONTH:
                return symbols.getMonths(context, width);
            case Calendar.ERA:
                switch (width) {
                    case DateFormatSymbols.NARROW:
                        return symbols.getNarrowEras();
                    case DateFormatSymbols.ABBREVIATED:
                        return symbols.getEras();
                    case DateFormatSymbols.WIDE:
                        return symbols.getEraNames();
                    default:
                        throw new UnsupportedOperationException("Unknown width: " + width);
                }
            case Calendar.DAY_OF_WEEK:
                return symbols.getWeekdays(context, width);
            case Calendar.AM_PM:
                return symbols.getAmPmStrings();
            default:
                throw new UnsupportedOperationException("Unknown field: " + field);
        }
    }

    private static DateFormatSymbols getDateFormatSymbols(String id, Locale locale) {
        String calendarType = normalizeCalendarType(id);
        return new DateFormatSymbols(ULocale.forLocale(locale), calendarType);
    }

    /**
     * Transform a {@link Calendar} style constant into an ICU width value.
     */
    private static int toWidth(int style) {
        switch (style) {
            case Calendar.SHORT_FORMAT:
            case Calendar.SHORT_STANDALONE:
                return DateFormatSymbols.ABBREVIATED;
            case Calendar.NARROW_FORMAT:
            case Calendar.NARROW_STANDALONE:
                return DateFormatSymbols.NARROW;
            case Calendar.LONG_FORMAT:
            case Calendar.LONG_STANDALONE:
                return DateFormatSymbols.WIDE;
            default:
                throw new IllegalArgumentException("Invalid style: " + style);
        }
    }

    /**
     * Transform a {@link Calendar} style constant into an ICU context value.
     */
    private static int toContext(int style) {
        switch (style) {
            case Calendar.SHORT_FORMAT:
            case Calendar.NARROW_FORMAT:
            case Calendar.LONG_FORMAT:
                return DateFormatSymbols.FORMAT;
            case Calendar.SHORT_STANDALONE:
            case Calendar.NARROW_STANDALONE:
            case Calendar.LONG_STANDALONE:
                return DateFormatSymbols.STANDALONE;
            default:
                throw new IllegalArgumentException("Invalid style: " + style);
        }
    }

    private static String normalizeCalendarType(String requestID) {
        String type;
        // Android-changed: normalize "gregory" to "gregorian", not the other way around.
        // See android.icu.text.DateFormatSymbols.CALENDAR_CLASSES for reference.
        if (requestID.equals("gregory") || requestID.equals("iso8601")) {
            type = GREGORIAN_CALENDAR;
        } else if (requestID.startsWith(ISLAMIC_CALENDAR)) {
            type = ISLAMIC_CALENDAR;
        } else {
            type = requestID;
        }
        return type;
    }

    // Android-changed: Removed CalendarFieldValueNameGetter, CalendarFieldValueNamesMapGetter
    // and CalendarWeekParameterGetter
}
