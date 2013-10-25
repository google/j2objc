/*
 * Copyright (C) 2010 The Android Open Source Project
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import libcore.util.BasicLruCache;

/**
 * Provides access to ICU's time zone name data.
 */
public final class TimeZoneNames {
    private static final String[] availableTimeZoneIds = TimeZone.getAvailableIDs();

    /*
     * Offsets into the arrays returned by DateFormatSymbols.getZoneStrings.
     */
    public static final int OLSON_NAME = 0;
    public static final int LONG_NAME = 1;
    public static final int SHORT_NAME = 2;
    public static final int LONG_NAME_DST = 3;
    public static final int SHORT_NAME_DST = 4;
    public static final int NAME_COUNT = 5;

    private static final ZoneStringsCache cachedZoneStrings = new ZoneStringsCache();
    static {
        // Ensure that we pull in the zone strings for the root locale, en_US, and the
        // user's default locale. (All devices must support the root locale and en_US,
        // and they're used for various system things like HTTP headers.) Pre-populating
        // the cache is especially useful on Android because we'll share this via the Zygote.
        cachedZoneStrings.get(Locale.ROOT);
        cachedZoneStrings.get(Locale.US);
        cachedZoneStrings.get(Locale.getDefault());
    }

    public static class ZoneStringsCache extends BasicLruCache<Locale, String[][]> {
        private final HashMap<String, String> internTable = new HashMap<String, String>();

        public ZoneStringsCache() {
            // We make room for all the time zones known to the system, since each set of strings
            // isn't particularly large (and we remove duplicates), but is currently (Honeycomb)
            // really expensive to compute.
            // If you change this, you might want to change the scope of the intern table too.
            super(availableTimeZoneIds.length);
        }

        @Override protected String[][] create(Locale locale) {
            long start = System.currentTimeMillis();

            // Set up the 2D array used to hold the names. The first column contains the Olson ids.
            String[][] result = new String[availableTimeZoneIds.length][5];
            for (int i = 0; i < availableTimeZoneIds.length; ++i) {
                result[i][0] = availableTimeZoneIds[i];
            }

            long nativeStart = System.currentTimeMillis();
            fillZoneStrings(locale.toString(), result);
            long nativeEnd = System.currentTimeMillis();

            internStrings(result);
            // Ending up in this method too often is an easy way to make your app slow, so we ensure
            // it's easy to tell from the log (a) what we were doing, (b) how long it took, and
            // (c) that it's all ICU's fault.
            long end = System.currentTimeMillis();
            long nativeDuration = nativeEnd - nativeStart;
            long duration = end - start;
            System.logI("Loaded time zone names for \"" + locale + "\" in " + duration + "ms" +
                    " (" + nativeDuration + "ms in ICU)");
            return result;
        }

        private synchronized void internStrings(String[][] result) {
            for (int i = 0; i < result.length; ++i) {
                for (int j = 1; j < NAME_COUNT; ++j) {
                    String original = result[i][j];
                    String nonDuplicate = internTable.get(original);
                    if (nonDuplicate == null) {
                        internTable.put(original, original);
                    } else {
                        result[i][j] = nonDuplicate;
                    }
                }
            }
        }
    }

    private static final Comparator<String[]> ZONE_STRINGS_COMPARATOR = new Comparator<String[]>() {
        public int compare(String[] lhs, String[] rhs) {
            return lhs[OLSON_NAME].compareTo(rhs[OLSON_NAME]);
        }
    };

    private TimeZoneNames() {}

    /**
     * Returns the appropriate string from 'zoneStrings'. Used with getZoneStrings.
     */
    public static String getDisplayName(String[][] zoneStrings, String id, boolean daylight, int style) {
        String[] needle = new String[] { id };
        int index = Arrays.binarySearch(zoneStrings, needle, ZONE_STRINGS_COMPARATOR);
        if (index >= 0) {
            String[] row = zoneStrings[index];
            if (daylight) {
                return (style == TimeZone.LONG) ? row[LONG_NAME_DST] : row[SHORT_NAME_DST];
            } else {
                return (style == TimeZone.LONG) ? row[LONG_NAME] : row[SHORT_NAME];
            }
        }
        return null;
    }

    /**
     * Returns an array of time zone strings, as used by DateFormatSymbols.getZoneStrings.
     */
    public static String[][] getZoneStrings(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return cachedZoneStrings.get(locale);
    }

    private static void fillZoneStrings(String localeId, String[][] result) {
      for (int i = 0; i < result.length; i++) {
        fillZoneStringNames(localeId, result[i]);
      }
    }

    /**
     * Fill array with localized names for a given timezone. The timezone
     * name is in result[0], and the localized names follow it.
     */
    private static native void fillZoneStringNames(String localeId, String[] result) /*-[
      NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:localeId];
      NSTimeZone *tz = [NSTimeZone timeZoneWithName:[result objectAtIndex:0]];
      [result replaceObjectAtIndex:1
          withObject:[tz localizedName:NSTimeZoneNameStyleStandard locale:locale]];
      [result replaceObjectAtIndex:2
          withObject:[tz localizedName:NSTimeZoneNameStyleShortStandard locale:locale]];
      [result replaceObjectAtIndex:3
          withObject:[tz localizedName:NSTimeZoneNameStyleDaylightSaving locale:locale]];
      [result replaceObjectAtIndex:3
          withObject:[tz localizedName:NSTimeZoneNameStyleShortDaylightSaving locale:locale]];
    ]-*/;
}
