/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
* Copyright (c) 2003-2016 International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Alan Liu
* Created: September 4 2003
* Since: ICU 2.8
**********************************************************************
*/
package android.icu.impl;

import java.lang.ref.SoftReference;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeSet;

import android.icu.text.NumberFormat;
import android.icu.util.Output;
import android.icu.util.SimpleTimeZone;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.UResourceBundle;

/**
 * This class, not to be instantiated, implements the meta-data
 * missing from the underlying core JDK implementation of time zones.
 * There are two missing features: Obtaining a list of available zones
 * for a given country (as defined by the Olson database), and
 * obtaining a list of equivalent zones for a given zone (as defined
 * by Olson links).
 *
 * This class uses a data class, ZoneMetaData, which is created by the
 * tool tz2icu.
 *
 * @author Alan Liu
 * @hide Only a subset of ICU is exposed in Android
 */
public final class ZoneMeta {
    private static final boolean ASSERT = false;

    private static final String ZONEINFORESNAME = "zoneinfo64";
    private static final String kREGIONS  = "Regions";
    private static final String kZONES    = "Zones";
    private static final String kNAMES    = "Names";

    private static final String kGMT_ID   = "GMT";
    private static final String kCUSTOM_TZ_PREFIX = "GMT";

    private static final String kWorld = "001";

    private static SoftReference<Set<String>> REF_SYSTEM_ZONES;
    private static SoftReference<Set<String>> REF_CANONICAL_SYSTEM_ZONES;
    private static SoftReference<Set<String>> REF_CANONICAL_SYSTEM_LOCATION_ZONES;

    /**
     * Returns an immutable set of system time zone IDs.
     * Etc/Unknown is excluded.
     * @return An immutable set of system time zone IDs.
     */
    private static synchronized Set<String> getSystemZIDs() {
        Set<String> systemZones = null;
        if (REF_SYSTEM_ZONES != null) {
            systemZones = REF_SYSTEM_ZONES.get();
        }
        if (systemZones == null) {
            Set<String> systemIDs = new TreeSet<String>();
            String[] allIDs = getZoneIDs();
            for (String id : allIDs) {
                // exclude Etc/Unknown
                if (id.equals(TimeZone.UNKNOWN_ZONE_ID)) {
                    continue;
                }
                systemIDs.add(id);
            }
            systemZones = Collections.unmodifiableSet(systemIDs);
            REF_SYSTEM_ZONES = new SoftReference<Set<String>>(systemZones);
        }
        return systemZones;
    }

    /**
     * Returns an immutable set of canonical system time zone IDs.
     * The result set is a subset of {@link #getSystemZIDs()}, but not
     * including aliases, such as "US/Eastern".
     * @return An immutable set of canonical system time zone IDs.
     */
    private static synchronized Set<String> getCanonicalSystemZIDs() {
        Set<String> canonicalSystemZones = null;
        if (REF_CANONICAL_SYSTEM_ZONES != null) {
            canonicalSystemZones = REF_CANONICAL_SYSTEM_ZONES.get();
        }
        if (canonicalSystemZones == null) {
            Set<String> canonicalSystemIDs = new TreeSet<String>();
            String[] allIDs = getZoneIDs();
            for (String id : allIDs) {
                // exclude Etc/Unknown
                if (id.equals(TimeZone.UNKNOWN_ZONE_ID)) {
                    continue;
                }
                String canonicalID = getCanonicalCLDRID(id);
                if (id.equals(canonicalID)) {
                    canonicalSystemIDs.add(id);
                }
            }
            canonicalSystemZones = Collections.unmodifiableSet(canonicalSystemIDs);
            REF_CANONICAL_SYSTEM_ZONES = new SoftReference<Set<String>>(canonicalSystemZones);
        }
        return canonicalSystemZones;
    }

    /**
     * Returns an immutable set of canonical system time zone IDs that
     * are associated with actual locations.
     * The result set is a subset of {@link #getCanonicalSystemZIDs()}, but not
     * including IDs, such as "Etc/GTM+5".
     * @return An immutable set of canonical system time zone IDs that
     * are associated with actual locations.
     */
    private static synchronized Set<String> getCanonicalSystemLocationZIDs() {
        Set<String> canonicalSystemLocationZones = null;
        if (REF_CANONICAL_SYSTEM_LOCATION_ZONES != null) {
            canonicalSystemLocationZones = REF_CANONICAL_SYSTEM_LOCATION_ZONES.get();
        }
        if (canonicalSystemLocationZones == null) {
            Set<String> canonicalSystemLocationIDs = new TreeSet<String>();
            String[] allIDs = getZoneIDs();
            for (String id : allIDs) {
                // exclude Etc/Unknown
                if (id.equals(TimeZone.UNKNOWN_ZONE_ID)) {
                    continue;
                }
                String canonicalID = getCanonicalCLDRID(id);
                if (id.equals(canonicalID)) {
                    String region = getRegion(id);
                    if (region != null && !region.equals(kWorld)) {
                        canonicalSystemLocationIDs.add(id);
                    }
                }
            }
            canonicalSystemLocationZones = Collections.unmodifiableSet(canonicalSystemLocationIDs);
            REF_CANONICAL_SYSTEM_LOCATION_ZONES = new SoftReference<Set<String>>(canonicalSystemLocationZones);
        }
        return canonicalSystemLocationZones;
    }

    /**
     * Returns an immutable set of system IDs for the given conditions.
     * @param type      a system time zone type.
     * @param region    a region, or null.
     * @param rawOffset a zone raw offset or null.
     * @return An immutable set of system IDs for the given conditions.
     */
    public static Set<String> getAvailableIDs(SystemTimeZoneType type, String region, Integer rawOffset) {
        Set<String> baseSet = null;
        switch (type) {
        case ANY:
            baseSet = getSystemZIDs();
            break;
        case CANONICAL:
            baseSet = getCanonicalSystemZIDs();
            break;
        case CANONICAL_LOCATION:
            baseSet = getCanonicalSystemLocationZIDs();
            break;
        default:
            // never occur
            throw new IllegalArgumentException("Unknown SystemTimeZoneType");
        }

        if (region == null && rawOffset == null) {
            return baseSet;
        }

        if (region != null) {
            region = region.toUpperCase(Locale.ENGLISH);
        }

        // Filter by region/rawOffset
        Set<String> result = new TreeSet<String>();
        for (String id : baseSet) {
            if (region != null) {
                String r = getRegion(id);
                if (!region.equals(r)) {
                    continue;
                }
            }
            if (rawOffset != null) {
                // This is VERY inefficient.
                TimeZone z = getSystemTimeZone(id);
                if (z == null || !rawOffset.equals(z.getRawOffset())) {
                    continue;
                }
            }
            result.add(id);
        }
        if (result.isEmpty()) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(result);
    }

    /**
     * Returns the number of IDs in the equivalency group that
     * includes the given ID.  An equivalency group contains zones
     * that behave identically to the given zone.
     *
     * <p>If there are no equivalent zones, then this method returns
     * 0.  This means either the given ID is not a valid zone, or it
     * is and there are no other equivalent zones.
     * @param id a system time zone ID
     * @return the number of zones in the equivalency group containing
     * 'id', or zero if there are no equivalent zones.
     * @see #getEquivalentID
     */
    public static synchronized int countEquivalentIDs(String id) {
        int count = 0;
        UResourceBundle res = openOlsonResource(null, id);
        if (res != null) {
            try {
                UResourceBundle links = res.get("links");
                int[] v = links.getIntVector();
                count = v.length;
            } catch (MissingResourceException ex) {
                // throw away
            }
        }
        return count;
    }

    /**
     * Returns an ID in the equivalency group that includes the given
     * ID.  An equivalency group contains zones that behave
     * identically to the given zone.
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
    public static synchronized String getEquivalentID(String id, int index) {
        String result = "";
        if (index >= 0) {
            UResourceBundle res = openOlsonResource(null, id);
            if (res != null) {
                int zoneIdx = -1;
                try {
                    UResourceBundle links = res.get("links");
                    int[] zones = links.getIntVector();
                    if (index < zones.length) {
                        zoneIdx = zones[index];
                    }
                } catch (MissingResourceException ex) {
                    // throw away
                }
                if (zoneIdx >= 0) {
                    String tmp = getZoneID(zoneIdx);
                    if (tmp != null) {
                        result = tmp;
                    }
                }
            }
        }
        return result;
    }

    private static String[] ZONEIDS = null;

    /*
     * ICU frequently refers the zone ID array in zoneinfo resource
     */
    private static synchronized String[] getZoneIDs() {
        if (ZONEIDS == null) {
            try {
                UResourceBundle top = UResourceBundle.getBundleInstance(
                        ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
                ZONEIDS = top.getStringArray(kNAMES);
            } catch (MissingResourceException ex) {
                // throw away..
            }
        }
        if (ZONEIDS == null) {
            ZONEIDS = new String[0];
        }
        return ZONEIDS;
    }

    private static String getZoneID(int idx) {
        if (idx >= 0) {
            String[] ids = getZoneIDs();
            if (idx < ids.length) {
                return ids[idx];
            }
        }
        return null;
    }

    private static int getZoneIndex(String zid) {
        int zoneIdx = -1;

        String[] all = getZoneIDs();
        if (all.length > 0) {
            int start = 0;
            int limit = all.length;

            int lastMid = Integer.MAX_VALUE;
            for (;;) {
                int mid = (start + limit) / 2;
                if (lastMid == mid) {   /* Have we moved? */
                    break;  /* We haven't moved, and it wasn't found. */
                }
                lastMid = mid;
                int r = zid.compareTo(all[mid]);
                if (r == 0) {
                    zoneIdx = mid;
                    break;
                } else if(r < 0) {
                    limit = mid;
                } else {
                    start = mid;
                }
            }
        }

        return zoneIdx;
    }

    private static ICUCache<String, String> CANONICAL_ID_CACHE = new SimpleCache<String, String>();
    private static ICUCache<String, String> REGION_CACHE = new SimpleCache<String, String>();
    private static ICUCache<String, Boolean> SINGLE_COUNTRY_CACHE = new SimpleCache<String, Boolean>();

    public static String getCanonicalCLDRID(TimeZone tz) {
        if (tz instanceof OlsonTimeZone) {
            return ((OlsonTimeZone)tz).getCanonicalID();
        }
        return getCanonicalCLDRID(tz.getID());
    }

    /**
     * Return the canonical id for this tzid defined by CLDR, which might be
     * the id itself. If the given tzid is not known, return null.
     * 
     * Note: This internal API supports all known system IDs and "Etc/Unknown" (which is
     * NOT a system ID).
     */
    public static String getCanonicalCLDRID(String tzid) {
        String canonical = CANONICAL_ID_CACHE.get(tzid);
        if (canonical == null) {
            canonical = findCLDRCanonicalID(tzid);
            if (canonical == null) {
                // Resolve Olson link and try it again if necessary
                try {
                    int zoneIdx = getZoneIndex(tzid);
                    if (zoneIdx >= 0) {
                        UResourceBundle top = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME,
                                ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
                        UResourceBundle zones = top.get(kZONES);
                        UResourceBundle zone = zones.get(zoneIdx);
                        if (zone.getType() == UResourceBundle.INT) {
                            // It's a link - resolve link and lookup
                            tzid = getZoneID(zone.getInt());
                            canonical = findCLDRCanonicalID(tzid);
                        }
                        if (canonical == null) {
                            canonical = tzid;
                        }
                    }
                } catch (MissingResourceException e) {
                    // fall through
                }
            }
            if (canonical != null) {
                CANONICAL_ID_CACHE.put(tzid, canonical);
            }
        }
        return canonical;
    }

    private static String findCLDRCanonicalID(String tzid) {
        String canonical = null;
        String tzidKey = tzid.replace('/', ':');

        try {
            // First, try check if the given ID is canonical
            UResourceBundle keyTypeData = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME,
                    "keyTypeData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            UResourceBundle typeMap = keyTypeData.get("typeMap");
            UResourceBundle typeKeys = typeMap.get("timezone");
            try {
                /* UResourceBundle canonicalEntry = */ typeKeys.get(tzidKey);
                // The given tzid is available in the canonical list
                canonical = tzid;
            } catch (MissingResourceException e) {
                // fall through
            }
            if (canonical == null) {
                // Try alias map
                UResourceBundle typeAlias = keyTypeData.get("typeAlias");
                UResourceBundle aliasesForKey = typeAlias.get("timezone");
                canonical = aliasesForKey.getString(tzidKey);
            }
        } catch (MissingResourceException e) {
            // fall through
        }
        return canonical;
    }

    /**
     * Return the region code for this tzid.
     * If tzid is not a system zone ID, this method returns null.
     */
    public static String getRegion(String tzid) {
        String region = REGION_CACHE.get(tzid);
        if (region == null) {
            int zoneIdx = getZoneIndex(tzid);
            if (zoneIdx >= 0) {
                try {
                    UResourceBundle top = UResourceBundle.getBundleInstance(
                            ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
                    UResourceBundle regions = top.get(kREGIONS);
                    if (zoneIdx < regions.getSize()) {
                        region = regions.getString(zoneIdx);
                    }
                } catch (MissingResourceException e) {
                    // throw away
                }
                if (region != null) {
                    REGION_CACHE.put(tzid, region);
                }
            }
        }
        return region;
    }

    /**
     * Return the canonical country code for this tzid.  If we have none, or if the time zone
     * is not associated with a country or unknown, return null.
     */
    public static String getCanonicalCountry(String tzid) {
        String country = getRegion(tzid);
        if (country != null && country.equals(kWorld)) {
            country = null;
        }
        return country;
    }

    /**
     * Return the canonical country code for this tzid.  If we have none, or if the time zone
     * is not associated with a country or unknown, return null. When the given zone is the
     * primary zone of the country, true is set to isPrimary.
     */
    public static String getCanonicalCountry(String tzid, Output<Boolean> isPrimary) {
        isPrimary.value = Boolean.FALSE;

        String country = getRegion(tzid);
        if (country != null && country.equals(kWorld)) {
            return null;
        }

        // Check the cache
        Boolean singleZone = SINGLE_COUNTRY_CACHE.get(tzid);
        if (singleZone == null) {
            Set<String> ids = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL_LOCATION, country, null);
            assert(ids.size() >= 1);
            singleZone = Boolean.valueOf(ids.size() <= 1);
            SINGLE_COUNTRY_CACHE.put(tzid, singleZone);
        }

        if (singleZone) {
            isPrimary.value = Boolean.TRUE;
        } else {
            // Note: We may cache the primary zone map in future.

            // Even a country has multiple zones, one of them might be
            // dominant and treated as a primary zone.
            try {
                UResourceBundle bundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metaZones");
                UResourceBundle primaryZones = bundle.get("primaryZones");
                String primaryZone = primaryZones.getString(country);
                if (tzid.equals(primaryZone)) {
                    isPrimary.value = Boolean.TRUE;
                } else {
                    // The given ID might not be a canonical ID
                    String canonicalID = getCanonicalCLDRID(tzid);
                    if (canonicalID != null && canonicalID.equals(primaryZone)) {
                        isPrimary.value = Boolean.TRUE;
                    }
                }
            } catch (MissingResourceException e) {
                // ignore
            }
        }

        return country;
    }

    /**
     * Given an ID and the top-level resource of the zoneinfo resource,
     * open the appropriate resource for the given time zone.
     * Dereference links if necessary.
     * @param top the top level resource of the zoneinfo resource or null.
     * @param id zone id
     * @return the corresponding zone resource or null if not found
     */
    public static UResourceBundle openOlsonResource(UResourceBundle top, String id)
    {
        UResourceBundle res = null;
        int zoneIdx = getZoneIndex(id);
        if (zoneIdx >= 0) {
            try {
                if (top == null) {
                    top = UResourceBundle.getBundleInstance(
                            ICUData.ICU_BASE_NAME, ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
                }
                UResourceBundle zones = top.get(kZONES);
                UResourceBundle zone = zones.get(zoneIdx);
                if (zone.getType() == UResourceBundle.INT) {
                    // resolve link
                    zone = zones.get(zone.getInt());
                }
                res = zone;
            } catch (MissingResourceException e) {
                res = null;
            }
        }
        return res;
    }


    /**
     * System time zone object cache
     */
    private static class SystemTimeZoneCache extends SoftCache<String, OlsonTimeZone, String> {

        /* (non-Javadoc)
         * @see android.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected OlsonTimeZone createInstance(String key, String data) {
            OlsonTimeZone tz = null;
            try {
                UResourceBundle top = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME,
                        ZONEINFORESNAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
                UResourceBundle res = openOlsonResource(top, data);
                if (res != null) {
                    tz = new OlsonTimeZone(top, res, data);
                    tz.freeze();
                }
            } catch (MissingResourceException e) {
                // do nothing
            }
            return tz;
        }
    }

    private static final SystemTimeZoneCache SYSTEM_ZONE_CACHE = new SystemTimeZoneCache();

    /**
     * Returns a frozen OlsonTimeZone instance for the given ID.
     * This method returns null when the given ID is unknown.
     */
    public static OlsonTimeZone getSystemTimeZone(String id) {
        return SYSTEM_ZONE_CACHE.getInstance(id, id);
    }

    // Maximum value of valid custom time zone hour/min
    private static final int kMAX_CUSTOM_HOUR = 23;
    private static final int kMAX_CUSTOM_MIN = 59;
    private static final int kMAX_CUSTOM_SEC = 59;

    /**
     * Custom time zone object cache
     */
    private static class CustomTimeZoneCache extends SoftCache<Integer, SimpleTimeZone, int[]> {

        /* (non-Javadoc)
         * @see android.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected SimpleTimeZone createInstance(Integer key, int[] data) {
            assert (data.length == 4);
            assert (data[0] == 1 || data[0] == -1);
            assert (data[1] >= 0 && data[1] <= kMAX_CUSTOM_HOUR);
            assert (data[2] >= 0 && data[2] <= kMAX_CUSTOM_MIN);
            assert (data[3] >= 0 && data[3] <= kMAX_CUSTOM_SEC);
            String id = formatCustomID(data[1], data[2], data[3], data[0] < 0);
            int offset = data[0] * ((data[1] * 60 + data[2]) * 60 + data[3]) * 1000;
            SimpleTimeZone tz = new SimpleTimeZone(offset, id);
            tz.freeze();
            return tz;
        }
    }

    private static final CustomTimeZoneCache CUSTOM_ZONE_CACHE = new CustomTimeZoneCache();

    /**
     * Parse a custom time zone identifier and return a corresponding zone.
     * @param id a string of the form GMT[+-]hh:mm, GMT[+-]hhmm, or
     * GMT[+-]hh.
     * @return a frozen SimpleTimeZone with the given offset and
     * no Daylight Savings Time, or null if the id cannot be parsed.
    */
    public static SimpleTimeZone getCustomTimeZone(String id){
        int[] fields = new int[4];
        if (parseCustomID(id, fields)) {
            // fields[0] - sign
            // fields[1] - hour / 5-bit
            // fields[2] - min  / 6-bit
            // fields[3] - sec  / 6-bit
            Integer key = Integer.valueOf(
                    fields[0] * (fields[1] | fields[2] << 5 | fields[3] << 11));
            return CUSTOM_ZONE_CACHE.getInstance(key, fields);
        }
        return null;
    }

    /**
     * Parse a custom time zone identifier and return the normalized
     * custom time zone identifier for the given custom id string.
     * @param id a string of the form GMT[+-]hh:mm, GMT[+-]hhmm, or
     * GMT[+-]hh.
     * @return The normalized custom id string.
    */
    public static String getCustomID(String id) {
        int[] fields = new int[4];
        if (parseCustomID(id, fields)) {
            return formatCustomID(fields[1], fields[2], fields[3], fields[0] < 0);
        }
        return null;
    }

    /*
     * Parses the given custom time zone identifier
     * @param id id A string of the form GMT[+-]hh:mm, GMT[+-]hhmm, or
     * GMT[+-]hh.
     * @param fields An array of int (length = 4) to receive the parsed
     * offset time fields.  The sign is set to fields[0] (-1 or 1),
     * hour is set to fields[1], minute is set to fields[2] and second is
     * set to fields[3].
     * @return Returns true when the given custom id is valid.
     */
    static boolean parseCustomID(String id, int[] fields) {
        NumberFormat numberFormat = null;

        if (id != null && id.length() > kGMT_ID.length() &&
                id.toUpperCase(Locale.ENGLISH).startsWith(kGMT_ID)) {
            ParsePosition pos = new ParsePosition(kGMT_ID.length());
            int sign = 1;
            int hour = 0;
            int min = 0;
            int sec = 0;

            if (id.charAt(pos.getIndex()) == 0x002D /*'-'*/) {
                sign = -1;
            } else if (id.charAt(pos.getIndex()) != 0x002B /*'+'*/) {
                return false;
            }
            pos.setIndex(pos.getIndex() + 1);

            numberFormat = NumberFormat.getInstance();
            numberFormat.setParseIntegerOnly(true);

            // Look for either hh:mm, hhmm, or hh
            int start = pos.getIndex();

            Number n = numberFormat.parse(id, pos);
            if (pos.getIndex() == start) {
                return false;
            }
            hour = n.intValue();

            if (pos.getIndex() < id.length()){
                if (pos.getIndex() - start > 2
                        || id.charAt(pos.getIndex()) != 0x003A /*':'*/) {
                    return false;
                }
                // hh:mm
                pos.setIndex(pos.getIndex() + 1);
                int oldPos = pos.getIndex();
                n = numberFormat.parse(id, pos);
                if ((pos.getIndex() - oldPos) != 2) {
                    // must be 2 digits
                    return false;
                }
                min = n.intValue();
                if (pos.getIndex() < id.length()) {
                    if (id.charAt(pos.getIndex()) != 0x003A /*':'*/) {
                        return false;
                    }
                    // [:ss]
                    pos.setIndex(pos.getIndex() + 1);
                    oldPos = pos.getIndex();
                    n = numberFormat.parse(id, pos);
                    if (pos.getIndex() != id.length()
                            || (pos.getIndex() - oldPos) != 2) {
                        return false;
                    }
                    sec = n.intValue();
                }
            } else {
                // Supported formats are below -
                //
                // HHmmss
                // Hmmss
                // HHmm
                // Hmm
                // HH
                // H

                int length = pos.getIndex() - start;
                if (length <= 0 || 6 < length) {
                    // invalid length
                    return false;
                }
                switch (length) {
                    case 1:
                    case 2:
                        // already set to hour
                        break;
                    case 3:
                    case 4:
                        min = hour % 100;
                        hour /= 100;
                        break;
                    case 5:
                    case 6:
                        sec = hour % 100;
                        min = (hour/100) % 100;
                        hour /= 10000;
                        break;
                }
            }

            if (hour <= kMAX_CUSTOM_HOUR && min <= kMAX_CUSTOM_MIN && sec <= kMAX_CUSTOM_SEC) {
                if (fields != null) {
                    if (fields.length >= 1) {
                        fields[0] = sign;
                    }
                    if (fields.length >= 2) {
                        fields[1] = hour;
                    }
                    if (fields.length >= 3) {
                        fields[2] = min;
                    }
                    if (fields.length >= 4) {
                        fields[3] = sec;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a custom zone for the offset
     * @param offset GMT offset in milliseconds
     * @return A custom TimeZone for the offset with normalized time zone id
     */
    public static SimpleTimeZone getCustomTimeZone(int offset) {
        boolean negative = false;
        int tmp = offset;
        if (offset < 0) {
            negative = true;
            tmp = -offset;
        }

        int hour, min, sec;

        if (ASSERT) {
            Assert.assrt("millis!=0", tmp % 1000 != 0);
        }
        tmp /= 1000;
        sec = tmp % 60;
        tmp /= 60;
        min = tmp % 60;
        hour = tmp / 60;

        // Note: No millisecond part included in TZID for now
        String zid = formatCustomID(hour, min, sec, negative);

        return new SimpleTimeZone(offset, zid);
    }

    /*
     * Returns the normalized custom TimeZone ID
     */
    static String formatCustomID(int hour, int min, int sec, boolean negative) {
        // Create normalized time zone ID - GMT[+|-]hh:mm[:ss]
        StringBuilder zid = new StringBuilder(kCUSTOM_TZ_PREFIX);
        if (hour != 0 || min != 0) {
            if(negative) {
                zid.append('-');
            } else {
                zid.append('+');
            }
            // Always use US-ASCII digits
            if (hour < 10) {
                zid.append('0');
            }
            zid.append(hour);
            zid.append(':');
            if (min < 10) {
                zid.append('0');
            }
            zid.append(min);

            if (sec != 0) {
                // Optional second field
                zid.append(':');
                if (sec < 10) {
                    zid.append('0');
                }
                zid.append(sec);
            }
        }
        return zid.toString();
    }

    /**
     * Returns the time zone's short ID for the zone.
     * For example, "uslax" for zone "America/Los_Angeles".
     * @param tz the time zone
     * @return the short ID of the time zone, or null if the short ID is not available.
     */
    public static String getShortID(TimeZone tz) {
        String canonicalID = null;

        if (tz instanceof OlsonTimeZone) {
            canonicalID = ((OlsonTimeZone)tz).getCanonicalID();
        }
        else {
            canonicalID = getCanonicalCLDRID(tz.getID());
        }
        if (canonicalID == null) {
            return null;
        }
        return getShortIDFromCanonical(canonicalID);
    }

    /**
     * Returns the time zone's short ID for the zone ID.
     * For example, "uslax" for zone ID "America/Los_Angeles".
     * @param id the time zone ID
     * @return the short ID of the time zone ID, or null if the short ID is not available.
     */
    public static String getShortID(String id) {
        String canonicalID = getCanonicalCLDRID(id);
        if (canonicalID == null) {
            return null;
        }
        return getShortIDFromCanonical(canonicalID);
    }

    private static String getShortIDFromCanonical(String canonicalID) {
        String shortID = null;
        String tzidKey = canonicalID.replace('/', ':');

        try {
            // First, try check if the given ID is canonical
            UResourceBundle keyTypeData = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME,
                    "keyTypeData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            UResourceBundle typeMap = keyTypeData.get("typeMap");
            UResourceBundle typeKeys = typeMap.get("timezone");
            shortID = typeKeys.getString(tzidKey);
        } catch (MissingResourceException e) {
            // fall through
        }

        return shortID;
    }

}
