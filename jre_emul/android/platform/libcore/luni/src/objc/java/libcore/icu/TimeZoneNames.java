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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import libcore.util.BasicLruCache;
//import libcore.util.ZoneInfoDB;

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
        public ZoneStringsCache() {
            super(5); // Room for a handful of locales.
        }

        @Override protected String[][] create(Locale locale) {
            long start = System.nanoTime();

            // Set up the 2D array used to hold the names. The first column contains the Olson ids.
            String[][] result = new String[availableTimeZoneIds.length][5];
            for (int i = 0; i < availableTimeZoneIds.length; ++i) {
                result[i][0] = availableTimeZoneIds[i];
            }

            long nativeStart = System.nanoTime();
            fillZoneStrings(locale.toString(), result);
            long nativeEnd = System.nanoTime();

            internStrings(result);
            // Ending up in this method too often is an easy way to make your app slow, so we ensure
            // it's easy to tell from the log (a) what we were doing, (b) how long it took, and
            // (c) that it's all ICU's fault.
            long end = System.nanoTime();
            long nativeDuration = TimeUnit.NANOSECONDS.toMillis(nativeEnd - nativeStart);
            long duration = TimeUnit.NANOSECONDS.toMillis(end - start);
            System.logI("Loaded time zone names for \"" + locale + "\" in " + duration + "ms" +
                        " (" + nativeDuration + "ms in ICU)");
            return result;
        }

        // De-duplicate the strings (http://b/2672057).
        private synchronized void internStrings(String[][] result) {
            HashMap<String, String> internTable = new HashMap<String, String>();
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

    /**
     * Returns an array containing the time zone ids in use in the country corresponding to
     * the given locale. This is not necessary for Java API, but is used by telephony as a
     * fallback. We retrieve these strings from zone.tab rather than icu4c because the latter
     * supplies them in alphabetical order where zone.tab has them in a kind of "importance"
     * order (as defined in the zone.tab header).
     * J2ObjC: unused.
    public static String[] forLocale(Locale locale) {
        String countryCode = locale.getCountry();
        ArrayList<String> ids = new ArrayList<String>();
        for (String line : ZoneInfoDB.getInstance().getZoneTab().split("\n")) {
            if (line.startsWith(countryCode)) {
                int olsonIdStart = line.indexOf('\t', 4) + 1;
                int olsonIdEnd = line.indexOf('\t', olsonIdStart);
                if (olsonIdEnd == -1) {
                    olsonIdEnd = line.length(); // Not all zone.tab lines have a comment.
                }
                ids.add(line.substring(olsonIdStart, olsonIdEnd));
            }
        }
        return ids.toArray(new String[ids.size()]);
    }*/

    /* J2ObjC: unused.
    public static native String getExemplarLocation(String locale, String tz);*/

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
      NSTimeZone *tz = [NSTimeZone timeZoneWithName:IOSObjectArray_Get(result, 0)];
      IOSObjectArray_Set(result, 1, [tz localizedName:NSTimeZoneNameStyleStandard locale:locale]);
      IOSObjectArray_Set(result, 2,
          [tz localizedName:NSTimeZoneNameStyleShortStandard locale:locale]);
      IOSObjectArray_Set(result, 3,
          [tz localizedName:NSTimeZoneNameStyleDaylightSaving locale:locale]);
      IOSObjectArray_Set(result, 4,
          [tz localizedName:NSTimeZoneNameStyleShortDaylightSaving locale:locale]);
      [locale release];
    ]-*/;
}
