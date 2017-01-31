/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.icu;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import libcore.util.Objects;

/**
 * Passes locale-specific from ICU native code to Java.
 * <p>
 * Note that you share these; you must not alter any of the fields, nor their array elements
 * in the case of arrays. If you ever expose any of these things to user code, you must give
 * them a clone rather than the original.
 */
public final class LocaleData {
    // A cache for the locale-specific data.
    private static final HashMap<String, LocaleData> localeDataCache = new HashMap<String, LocaleData>();
    static {
        // Ensure that we pull in the locale data for the root locale, en_US, and the
        // user's default locale. (All devices must support the root locale and en_US,
        // and they're used for various system things like HTTP headers.) Pre-populating
        // the cache is especially useful on Android because we'll share this via the Zygote.
        get(Locale.ROOT);
        get(Locale.US);
        get(Locale.getDefault());
    }

    // Used by Calendar.
    public Integer firstDayOfWeek;
    public Integer minimalDaysInFirstWeek;

    // Used by DateFormatSymbols.
    public String[] amPm; // "AM", "PM".
    public String[] eras; // "BC", "AD".

    public String[] longMonthNames; // "January", ...
    public String[] shortMonthNames; // "Jan", ...
    public String[] tinyMonthNames; // "J", ...
    public String[] longStandAloneMonthNames; // "January", ...
    public String[] shortStandAloneMonthNames; // "Jan", ...
    public String[] tinyStandAloneMonthNames; // "J", ...

    public String[] longWeekdayNames; // "Sunday", ...
    public String[] shortWeekdayNames; // "Sun", ...
    public String[] tinyWeekdayNames; // "S", ...
    public String[] longStandAloneWeekdayNames; // "Sunday", ...
    public String[] shortStandAloneWeekdayNames; // "Sun", ...
    public String[] tinyStandAloneWeekdayNames; // "S", ...

    // Used by frameworks/base DateSorter and DateUtils.
    public String yesterday; // "Yesterday".
    public String today; // "Today".
    public String tomorrow; // "Tomorrow".

    public String fullTimeFormat;
    public String longTimeFormat;
    public String mediumTimeFormat;
    public String shortTimeFormat;

    public String fullDateFormat;
    public String longDateFormat;
    public String mediumDateFormat;
    public String shortDateFormat;

    // Used by TimePicker. Not currently used by UTS#35.
    /* J2ObjC unused.
    public String narrowAm; // "a".
    public String narrowPm; // "p".*/

    // Used by DateFormat to implement 12- and 24-hour SHORT and MEDIUM.
    // The first two are also used directly by frameworks code.
    /* J2ObjC unused.
    public String timeFormat_hm;
    public String timeFormat_Hm;
    public String timeFormat_hms;
    public String timeFormat_Hms;*/

    // Used by DecimalFormatSymbols.
    public char zeroDigit;
    public char decimalSeparator;
    public char groupingSeparator;
    public char patternSeparator;
    public String percent;
    public char perMill;
    public char monetarySeparator;
    public String minusSign;
    public String exponentSeparator;
    public String infinity;
    public String NaN;
    // Also used by Currency.
    public String currencySymbol;
    public String internationalCurrencySymbol;

    // Used by DecimalFormat and NumberFormat.
    public String numberPattern;
    public String integerPattern;
    public String currencyPattern;
    public String percentPattern;

    private LocaleData() {
    }

    public static Locale mapInvalidAndNullLocales(Locale locale) {
        if (locale == null) {
            return Locale.getDefault();
        }

        if ("und".equals(locale.toLanguageTag())) {
            return Locale.ROOT;
        }

        return locale;
    }

    /**
     * Returns a shared LocaleData for the given locale.
     */
    public static LocaleData get(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }

        final String languageTag = locale.toLanguageTag();
        synchronized (localeDataCache) {
            LocaleData localeData = localeDataCache.get(languageTag);
            if (localeData != null) {
                return localeData;
            }
        }
        LocaleData newLocaleData = initLocaleData(locale);
        synchronized (localeDataCache) {
            LocaleData localeData = localeDataCache.get(languageTag);
            if (localeData != null) {
                return localeData;
            }
            localeDataCache.put(languageTag, newLocaleData);
            return newLocaleData;
        }
    }

    @Override public String toString() {
        return Objects.toString(this);
    }

    public String getDateFormat(int style) {
        switch (style) {
        case DateFormat.SHORT:
            return shortDateFormat;
        case DateFormat.MEDIUM:
            return mediumDateFormat;
        case DateFormat.LONG:
            return longDateFormat;
        case DateFormat.FULL:
            return fullDateFormat;
        }
        throw new AssertionError();
    }

    public String getTimeFormat(int style) {
        switch (style) {
        case DateFormat.SHORT:
            /* J2ObjC changed - DateFormat.set24HourTimePref is Android-specific API.
            if (DateFormat.is24Hour == null) {
                return shortTimeFormat;
            } else {
                return DateFormat.is24Hour ? timeFormat_Hm : timeFormat_hm;
            }*/
            return shortTimeFormat;
        case DateFormat.MEDIUM:
            /* J2ObjC changed - DateFormat.set24HourTimePref is Android-specific API.
            if (DateFormat.is24Hour == null) {
                return mediumTimeFormat;
            } else {
                return DateFormat.is24Hour ? timeFormat_Hms : timeFormat_hms;
            }*/
            return mediumTimeFormat;
        case DateFormat.LONG:
            // CLDR doesn't really have anything we can use to obey the 12-/24-hour preference.
            return longTimeFormat;
        case DateFormat.FULL:
            // CLDR doesn't really have anything we can use to obey the 12-/24-hour preference.
            return fullTimeFormat;
        }
        throw new AssertionError();
    }

    private static LocaleData initLocaleData(Locale locale) {
        LocaleData localeData = new LocaleData();
        if (!ICU.initLocaleDataNative(locale.toLanguageTag(), localeData)) {
            throw new AssertionError("couldn't initialize LocaleData for locale " + locale);
        }

        // Get the SHORT and MEDIUM 12- and 24-hour time format strings.
        /* J2ObjC unused.
        localeData.timeFormat_hm = ICU.getBestDateTimePattern("hm", locale);
        localeData.timeFormat_Hm = ICU.getBestDateTimePattern("Hm", locale);
        localeData.timeFormat_hms = ICU.getBestDateTimePattern("hms", locale);
        localeData.timeFormat_Hms = ICU.getBestDateTimePattern("Hms", locale);*/

        // Fix up a couple of patterns.
        if (localeData.fullTimeFormat != null) {
            // There are some full time format patterns in ICU that use the pattern character 'v'.
            // Java doesn't accept this, so we replace it with 'z' which has about the same result
            // as 'v', the timezone name.
            // 'v' -> "PT", 'z' -> "PST", v is the generic timezone and z the standard tz
            // "vvvv" -> "Pacific Time", "zzzz" -> "Pacific Standard Time"
            localeData.fullTimeFormat = localeData.fullTimeFormat.replace('v', 'z');
        }
        if (localeData.numberPattern != null) {
            // The number pattern might contain positive and negative subpatterns. Arabic, for
            // example, might look like "#,##0.###;#,##0.###-" because the minus sign should be
            // written last. Macedonian supposedly looks something like "#,##0.###;(#,##0.###)".
            // (The negative subpattern is optional, though, and not present in most locales.)
            // By only swallowing '#'es and ','s after the '.', we ensure that we don't
            // accidentally eat too much.
            localeData.integerPattern = localeData.numberPattern.replaceAll("\\.[#,]*", "");
        }
        return localeData;
    }
}
