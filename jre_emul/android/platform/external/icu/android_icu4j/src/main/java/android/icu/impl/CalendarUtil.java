/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009,2016 International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.TreeMap;

import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

/**
 * Calendar utilities.
 * 
 * Date/time format service classes in android.icu.text packages
 * sometimes need to access calendar internal APIs.  But calendar
 * classes are in android.icu.util package, so the package local
 * cannot be used.  This class is added in android.icu.impl
 * package for sharing some calendar internal code for calendar
 * and date format.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CalendarUtil {
    private static final String CALKEY = "calendar";
    private static final String DEFCAL = "gregorian";

    /**
     * Returns a calendar type for the given locale.
     * When the given locale has calendar keyword, the
     * value of calendar keyword is returned.  Otherwise,
     * the default calendar type for the locale is returned.
     * @param loc The locale
     * @return Calendar type string, such as "gregorian"
     */
    public static String getCalendarType(ULocale loc) {
        String calType = loc.getKeywordValue(CALKEY);
        if (calType != null) {
            return calType;
        }

        // Canonicalize, so grandfathered variant will be transformed to keywords
        ULocale canonical = ULocale.createCanonical(loc.toString());
        calType = canonical.getKeywordValue(CALKEY);
        if (calType != null) {
            return calType;
        }

        // When calendar keyword is not available, use the locale's
        // region to get the default calendar type
        String region = ULocale.getRegionForSupplementalData(canonical, true);
        return CalendarPreferences.INSTANCE.getCalendarTypeForRegion(region);
    }

    private static final class CalendarPreferences extends UResource.Sink {
        private static final CalendarPreferences INSTANCE = new CalendarPreferences();
        // A TreeMap should be good because we expect very few entries.
        Map<String, String> prefs = new TreeMap<String, String>();

        CalendarPreferences() {
            try {
                ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.getBundleInstance(
                        ICUData.ICU_BASE_NAME, "supplementalData");
                rb.getAllItemsWithFallback("calendarPreferenceData", this);
            } catch (MissingResourceException mre) {
                // Always use "gregorian".
            }
        }

        String getCalendarTypeForRegion(String region) {
            String type = prefs.get(region);
            return type == null ? DEFCAL : type;
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table calendarPreferenceData = value.getTable();
            for (int i = 0; calendarPreferenceData.getKeyAndValue(i, key, value); ++i) {
                UResource.Array types = value.getArray();
                // The first calendar type is the default for the region.
                if (types.getValue(0, value)) {
                    String type = value.getString();
                    if (!type.equals(DEFCAL)) {
                        prefs.put(key.toString(), type);
                    }
                }
            }
        }
    }
}
