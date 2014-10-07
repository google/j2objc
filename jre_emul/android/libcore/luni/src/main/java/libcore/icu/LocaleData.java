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

/*-[
#import "IOSLocaleData.h"
]-*/

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

    // Used by android.text.format.DateFormat.getTimeFormat.
    // public String timeFormat12; // "hh:mm a"
    // public String timeFormat24; // "HH:mm"

    // Used by DecimalFormatSymbols.
    public char zeroDigit;
    public char decimalSeparator;
    public char groupingSeparator;
    public char patternSeparator = ';';  // There is no iOS API to fetch a locale-specific version.
    public char percent;
    public char perMill;
    public char monetarySeparator;
    public char minusSign;
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

    /**
     * Returns a shared LocaleData for the given locale.
     */
    public static LocaleData get(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        String localeName = locale.toString();
        synchronized (localeDataCache) {
            LocaleData localeData = localeDataCache.get(localeName);
            if (localeData != null) {
                return localeData;
            }
        }
        LocaleData newLocaleData = initLocaleData(locale);
        synchronized (localeDataCache) {
            LocaleData localeData = localeDataCache.get(localeName);
            if (localeData != null) {
                return localeData;
            }
            localeDataCache.put(localeName, newLocaleData);
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
            return shortTimeFormat;
        case DateFormat.MEDIUM:
            return mediumTimeFormat;
        case DateFormat.LONG:
            return longTimeFormat;
        case DateFormat.FULL:
            return fullTimeFormat;
        }
        throw new AssertionError();
    }

    private static LocaleData initLocaleData(Locale locale) {
        LocaleData localeData = new LocaleData();
        if (!initLocaleDataImpl(locale.toString(), localeData)) {
            throw new AssertionError("couldn't initialize LocaleData for locale " + locale);
        }
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

    private static native boolean initLocaleDataImpl(String localeId, LocaleData result) /*-[
      [IOSLocaleData initLocaleDataImplWithNSString:localeId withLibcoreIcuLocaleData:result];
      return YES;
    ]-*/;
}
