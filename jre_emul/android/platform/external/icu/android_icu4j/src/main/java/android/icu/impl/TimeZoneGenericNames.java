/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2011-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.icu.impl.TextTrieMap.ResultHandler;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.TimeZoneFormat.TimeType;
import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.MatchInfo;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.BasicTimeZone;
import android.icu.util.Freezable;
import android.icu.util.Output;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.TimeZoneTransition;
import android.icu.util.ULocale;

/**
 * This class interact with TimeZoneNames and LocaleDisplayNames
 * to format and parse time zone's generic display names.
 * It is not recommended to use this class directly, instead
 * use android.icu.text.TimeZoneFormat.
 * @hide Only a subset of ICU is exposed in Android
 */
public class TimeZoneGenericNames implements Serializable, Freezable<TimeZoneGenericNames> {

    // Note: This class implements Serializable, but we no longer serialize instance of
    // TimeZoneGenericNames in ICU 49. ICU 4.8 android.icu.text.TimeZoneFormat used to
    // serialize TimeZoneGenericNames field. TimeZoneFormat no longer read TimeZoneGenericNames
    // field, we have to keep TimeZoneGenericNames Serializable. Otherwise it fails to read
    // (unused) TimeZoneGenericNames serialized data.

    private static final long serialVersionUID = 2729910342063468417L;

    /**
     * Generic name type enum
     */
    public enum GenericNameType {
        LOCATION ("LONG", "SHORT"),
        LONG (),
        SHORT ();

        String[] _fallbackTypeOf;
        GenericNameType(String... fallbackTypeOf) {
            _fallbackTypeOf = fallbackTypeOf;
        }

        public boolean isFallbackTypeOf(GenericNameType type) {
            String typeStr = type.toString();
            for (String t : _fallbackTypeOf) {
                if (t.equals(typeStr)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Format pattern enum used for composing location and partial location names
     */
    public enum Pattern {
        // The format pattern such as "{0} Time", where {0} is the country or city.
        REGION_FORMAT("regionFormat", "({0})"),

        // Note: FALLBACK_REGION_FORMAT is no longer used since ICU 50/CLDR 22.1
        // The format pattern such as "{1} Time ({0})", where {1} is the country and {0} is a city.
        //FALLBACK_REGION_FORMAT("fallbackRegionFormat", "{1} ({0})"),

        // The format pattern such as "{1} ({0})", where {1} is the metazone, and {0} is the country or city.
        FALLBACK_FORMAT("fallbackFormat", "{1} ({0})");

        String _key;
        String _defaultVal;

        Pattern(String key, String defaultVal) {
            _key = key;
            _defaultVal = defaultVal;
        }

        String key() {
            return _key;
        }

        String defaultValue() {
            return _defaultVal;
        }
    }

    private final ULocale _locale;
    private TimeZoneNames _tznames;

    private transient volatile boolean _frozen;
    private transient String _region;
    private transient WeakReference<LocaleDisplayNames> _localeDisplayNamesRef;
    private transient MessageFormat[] _patternFormatters;

    private transient ConcurrentHashMap<String, String> _genericLocationNamesMap;
    private transient ConcurrentHashMap<String, String> _genericPartialLocationNamesMap;
    private transient TextTrieMap<NameInfo> _gnamesTrie;
    private transient boolean _gnamesTrieFullyLoaded;

    private static Cache GENERIC_NAMES_CACHE = new Cache();

    // Window size used for DST check for a zone in a metazone (about a half year)
    private static final long DST_CHECK_RANGE = 184L*(24*60*60*1000);

    private static final NameType[] GENERIC_NON_LOCATION_TYPES =
                                {NameType.LONG_GENERIC, NameType.SHORT_GENERIC};


    /**
     * Constructs a <code>TimeZoneGenericNames</code> with the given locale
     * and the <code>TimeZoneNames</code>.
     * @param locale the locale
     * @param tznames the TimeZoneNames
     */
    public TimeZoneGenericNames(ULocale locale, TimeZoneNames tznames) {
        _locale = locale;
        _tznames = tznames;
        init();
    }

    /**
     * Private method initializing the instance of <code>TimeZoneGenericName</code>.
     * This method should be called from a constructor and readObject.
     */
    private void init() {
        if (_tznames == null) {
            _tznames = TimeZoneNames.getInstance(_locale);
        }
        _genericLocationNamesMap = new ConcurrentHashMap<String, String>();
        _genericPartialLocationNamesMap = new ConcurrentHashMap<String, String>();

        _gnamesTrie = new TextTrieMap<NameInfo>(true);
        _gnamesTrieFullyLoaded = false;

        // Preload zone strings for the default time zone
        TimeZone tz = TimeZone.getDefault();
        String tzCanonicalID = ZoneMeta.getCanonicalCLDRID(tz);
        if (tzCanonicalID != null) {
            loadStrings(tzCanonicalID);
        }
    }

    /**
     * Constructs a <code>TimeZoneGenericNames</code> with the given locale.
     * This constructor is private and called from {@link #getInstance(ULocale)}.
     * @param locale the locale
     */
    private TimeZoneGenericNames(ULocale locale) {
        this(locale, null);
    }

    /**
     * The factory method of <code>TimeZoneGenericNames</code>. This static method
     * returns a frozen instance of cached <code>TimeZoneGenericNames</code>.
     * @param locale the locale
     * @return A frozen <code>TimeZoneGenericNames</code>.
     */
    public static TimeZoneGenericNames getInstance(ULocale locale) {
        String key = locale.getBaseName();
        return GENERIC_NAMES_CACHE.getInstance(key, locale);
    }

    /**
     * Returns the display name of the time zone for the given name type
     * at the given date, or null if the display name is not available.
     *
     * @param tz the time zone
     * @param type the generic name type - see {@link GenericNameType}
     * @param date the date
     * @return the display name of the time zone for the given name type
     * at the given date, or null.
     */
    public String getDisplayName(TimeZone tz, GenericNameType type, long date) {
        String name = null;
        String tzCanonicalID = null;
        switch (type) {
        case LOCATION:
            tzCanonicalID = ZoneMeta.getCanonicalCLDRID(tz);
            if (tzCanonicalID != null) {
                name = getGenericLocationName(tzCanonicalID);
            }
            break;
        case LONG:
        case SHORT:
            name = formatGenericNonLocationName(tz, type, date);
            if (name == null) {
                tzCanonicalID = ZoneMeta.getCanonicalCLDRID(tz);
                if (tzCanonicalID != null) {
                    name = getGenericLocationName(tzCanonicalID);
                }
            }
            break;
        }
        return name;
    }

    /**
     * Returns the generic location name for the given canonical time zone ID.
     *
     * @param canonicalTzID the canonical time zone ID
     * @return the generic location name for the given canonical time zone ID.
     */
    public String getGenericLocationName(String canonicalTzID) {
        if (canonicalTzID == null || canonicalTzID.length() == 0) {
            return null;
        }
        String name = _genericLocationNamesMap.get(canonicalTzID);
        if (name != null) {
            if (name.length() == 0) {
                // empty string to indicate the name is not available
                return null;
            }
            return name;
        }

        Output<Boolean> isPrimary = new Output<Boolean>();
        String countryCode = ZoneMeta.getCanonicalCountry(canonicalTzID, isPrimary);
        if (countryCode != null) {
            if (isPrimary.value) {
                // If this is only the single zone in the country, use the country name
                String country = getLocaleDisplayNames().regionDisplayName(countryCode);
                name = formatPattern(Pattern.REGION_FORMAT, country);
            } else {
                // If there are multiple zones including this in the country,
                // use the exemplar city name

                // getExemplarLocationName should return non-empty String
                // if the time zone is associated with a location
                String city = _tznames.getExemplarLocationName(canonicalTzID);
                name = formatPattern(Pattern.REGION_FORMAT, city);
            }
        }

        if (name == null) {
            _genericLocationNamesMap.putIfAbsent(canonicalTzID.intern(), "");
        } else {
            synchronized (this) {   // we have to sync the name map and the trie
                canonicalTzID = canonicalTzID.intern();
                String tmp = _genericLocationNamesMap.putIfAbsent(canonicalTzID, name.intern());
                if (tmp == null) {
                    // Also put the name info the to trie
                    NameInfo info = new NameInfo(canonicalTzID, GenericNameType.LOCATION);
                    _gnamesTrie.put(name, info);
                } else {
                    name = tmp;
                }
            }
        }
        return name;
    }

    /**
     * Sets the pattern string for the pattern type.
     * Note: This method is designed for CLDR ST - not for common use.
     * @param patType the pattern type
     * @param patStr the pattern string
     * @return this object.
     */
    public TimeZoneGenericNames setFormatPattern(Pattern patType, String patStr) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }

        // Changing pattern will invalidates cached names
        if (!_genericLocationNamesMap.isEmpty()) {
            _genericLocationNamesMap = new ConcurrentHashMap<String, String>();
        }
        if (!_genericPartialLocationNamesMap.isEmpty()) {
            _genericPartialLocationNamesMap = new ConcurrentHashMap<String, String>();
        }
        _gnamesTrie = null;
        _gnamesTrieFullyLoaded = false;

        if (_patternFormatters == null) {
            _patternFormatters = new MessageFormat[Pattern.values().length];
        }
        _patternFormatters[patType.ordinal()] = new MessageFormat(patStr);
        return this;
    }

    /**
     * Private method to get a generic string, with fallback logics involved,
     * that is,
     *
     * 1. If a generic non-location string is available for the zone, return it.
     * 2. If a generic non-location string is associated with a meta zone and
     *    the zone never use daylight time around the given date, use the standard
     *    string (if available).
     * 3. If a generic non-location string is associated with a meta zone and
     *    the offset at the given time is different from the preferred zone for the
     *    current locale, then return the generic partial location string (if available)
     * 4. If a generic non-location string is not available, use generic location
     *    string.
     *
     * @param tz the requested time zone
     * @param date the date
     * @param type the generic name type, either LONG or SHORT
     * @return the name used for a generic name type, which could be the
     * generic name, or the standard name (if the zone does not observes DST
     * around the date), or the partial location name.
     */
    private String formatGenericNonLocationName(TimeZone tz, GenericNameType type, long date) {
        assert(type == GenericNameType.LONG || type == GenericNameType.SHORT);
        String tzID = ZoneMeta.getCanonicalCLDRID(tz);

        if (tzID == null) {
            return null;
        }

        // Try to get a name from time zone first
        NameType nameType = (type == GenericNameType.LONG) ? NameType.LONG_GENERIC : NameType.SHORT_GENERIC;
        String name = _tznames.getTimeZoneDisplayName(tzID, nameType);

        if (name != null) {
            return name;
        }

        // Try meta zone
        String mzID = _tznames.getMetaZoneID(tzID, date);
        if (mzID != null) {
            boolean useStandard = false;
            int[] offsets = {0, 0};
            tz.getOffset(date, false, offsets);

            if (offsets[1] == 0) {
                useStandard = true;
                // Check if the zone actually uses daylight saving time around the time
                if (tz instanceof BasicTimeZone) {
                    BasicTimeZone btz = (BasicTimeZone)tz;
                    TimeZoneTransition before = btz.getPreviousTransition(date, true);
                    if (before != null
                            && (date - before.getTime() < DST_CHECK_RANGE)
                            && before.getFrom().getDSTSavings() != 0) {
                        useStandard = false;
                    } else {
                        TimeZoneTransition after = btz.getNextTransition(date, false);
                        if (after != null
                                && (after.getTime() - date < DST_CHECK_RANGE)
                                && after.getTo().getDSTSavings() != 0) {
                            useStandard = false;
                        }
                    }
                } else {
                    // If not BasicTimeZone... only if the instance is not an ICU's implementation.
                    // We may get a wrong answer in edge case, but it should practically work OK.
                    int[] tmpOffsets = new int[2];
                    tz.getOffset(date - DST_CHECK_RANGE, false, tmpOffsets);
                    if (tmpOffsets[1] != 0) {
                        useStandard = false;
                    } else {
                        tz.getOffset(date + DST_CHECK_RANGE, false, tmpOffsets);
                        if (tmpOffsets[1] != 0){
                            useStandard = false;
                        }
                    }
                }
            }
            if (useStandard) {
                NameType stdNameType = (nameType == NameType.LONG_GENERIC) ?
                        NameType.LONG_STANDARD : NameType.SHORT_STANDARD;
                String stdName = _tznames.getDisplayName(tzID, stdNameType, date);
                if (stdName != null) {
                    name = stdName;

                    // TODO: revisit this issue later
                    // In CLDR, a same display name is used for both generic and standard
                    // for some meta zones in some locales.  This looks like a data bugs.
                    // For now, we check if the standard name is different from its generic
                    // name below.
                    String mzGenericName = _tznames.getMetaZoneDisplayName(mzID, nameType);
                    if (stdName.equalsIgnoreCase(mzGenericName)) {
                        name = null;
                    }
                }
            }

            if (name == null) {
                // Get a name from meta zone
                String mzName = _tznames.getMetaZoneDisplayName(mzID, nameType);
                if (mzName != null) {
                    // Check if we need to use a partial location format.
                    // This check is done by comparing offset with the meta zone's
                    // golden zone at the given date.
                    String goldenID = _tznames.getReferenceZoneID(mzID, getTargetRegion());
                    if (goldenID != null && !goldenID.equals(tzID)) {
                        TimeZone goldenZone = TimeZone.getFrozenTimeZone(goldenID);
                        int[] offsets1 = {0, 0};

                        // Check offset in the golden zone with wall time.
                        // With getOffset(date, false, offsets1),
                        // you may get incorrect results because of time overlap at DST->STD
                        // transition.
                        goldenZone.getOffset(date + offsets[0] + offsets[1], true, offsets1);

                        if (offsets[0] != offsets1[0] || offsets[1] != offsets1[1]) {
                            // Now we need to use a partial location format.
                            name = getPartialLocationName(tzID, mzID, (nameType == NameType.LONG_GENERIC), mzName);
                        } else {
                            name = mzName;
                        }
                    } else {
                        name = mzName;
                    }
                }
            }
        }
        return name;
    }

    /**
     * Private simple pattern formatter used for formatting generic location names
     * and partial location names. We intentionally use JDK MessageFormat
     * for performance reason.
     *
     * @param pat the message pattern enum
     * @param args the format argument(s)
     * @return the formatted string
     */
    private synchronized String formatPattern(Pattern pat, String... args) {
        if (_patternFormatters == null) {
            _patternFormatters = new MessageFormat[Pattern.values().length];
        }

        int idx = pat.ordinal();
        if (_patternFormatters[idx] == null) {
            String patText;
            try {
                ICUResourceBundle bundle = (ICUResourceBundle) ICUResourceBundle.getBundleInstance(
                    ICUData.ICU_ZONE_BASE_NAME, _locale);
                patText = bundle.getStringWithFallback("zoneStrings/" + pat.key());
            } catch (MissingResourceException e) {
                patText = pat.defaultValue();
            }

            _patternFormatters[idx] = new MessageFormat(patText);
        }
        return _patternFormatters[idx].format(args);
    }

    /**
     * Private method returning LocaleDisplayNames instance for the locale of this
     * instance. Because LocaleDisplayNames is only used for generic
     * location formant and partial location format, the LocaleDisplayNames
     * is instantiated lazily.
     *
     * @return the instance of LocaleDisplayNames for the locale of this object.
     */
    private synchronized LocaleDisplayNames getLocaleDisplayNames() {
        LocaleDisplayNames locNames = null;
        if (_localeDisplayNamesRef != null) {
            locNames = _localeDisplayNamesRef.get();
        }
        if (locNames == null) {
            locNames = LocaleDisplayNames.getInstance(_locale);
            _localeDisplayNamesRef = new WeakReference<LocaleDisplayNames>(locNames);
        }
        return locNames;
    }

    private synchronized void loadStrings(String tzCanonicalID) {
        if (tzCanonicalID == null || tzCanonicalID.length() == 0) {
            return;
        }
        // getGenericLocationName() formats a name and put it into the trie
        getGenericLocationName(tzCanonicalID);

        // Generic partial location format
        Set<String> mzIDs = _tznames.getAvailableMetaZoneIDs(tzCanonicalID);
        for (String mzID : mzIDs) {
            // if this time zone is not the golden zone of the meta zone,
            // partial location name (such as "PT (Los Angeles)") might be
            // available.
            String goldenID = _tznames.getReferenceZoneID(mzID, getTargetRegion());
            if (!tzCanonicalID.equals(goldenID)) {
                for (NameType genNonLocType : GENERIC_NON_LOCATION_TYPES) {
                    String mzGenName = _tznames.getMetaZoneDisplayName(mzID, genNonLocType);
                    if (mzGenName != null) {
                        // getPartialLocationName() formats a name and put it into the trie
                        getPartialLocationName(tzCanonicalID, mzID, (genNonLocType == NameType.LONG_GENERIC), mzGenName);
                    }
                }
            }
        }
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
     * Private method for formatting partial location names. This format
     * is used when a generic name of a meta zone is available, but the given
     * time zone is not a reference zone (golden zone) of the meta zone.
     *
     * @param tzID the canonical time zone ID
     * @param mzID the meta zone ID
     * @param isLong true when long generic name
     * @param mzDisplayName the meta zone generic display name
     * @return the partial location format string
     */
    private String getPartialLocationName(String tzID, String mzID, boolean isLong, String mzDisplayName) {
        String letter = isLong ? "L" : "S";
        String key = tzID + "&" + mzID + "#" + letter;
        String name = _genericPartialLocationNamesMap.get(key);
        if (name != null) {
            return name;
        }
        String location = null;
        String countryCode = ZoneMeta.getCanonicalCountry(tzID);
        if (countryCode != null) {
            // Is this the golden zone for the region?
            String regionalGolden = _tznames.getReferenceZoneID(mzID, countryCode);
            if (tzID.equals(regionalGolden)) {
                // Use country name
                location = getLocaleDisplayNames().regionDisplayName(countryCode);
            } else {
                // Otherwise, use exemplar city name
                location = _tznames.getExemplarLocationName(tzID);
            }
        } else {
            location = _tznames.getExemplarLocationName(tzID);
            if (location == null) {
                // This could happen when the time zone is not associated with a country,
                // and its ID is not hierarchical, for example, CST6CDT.
                // We use the canonical ID itself as the location for this case.
                location = tzID;
            }
        }
        name = formatPattern(Pattern.FALLBACK_FORMAT, location, mzDisplayName);
        synchronized (this) {   // we have to sync the name map and the trie
            String tmp = _genericPartialLocationNamesMap.putIfAbsent(key.intern(), name.intern());
            if (tmp == null) {
                NameInfo info = new NameInfo(tzID.intern(),
                        isLong ? GenericNameType.LONG : GenericNameType.SHORT);
                _gnamesTrie.put(name, info);
            } else {
                name = tmp;
            }
        }
        return name;
    }

    /**
     * A private class used for storing the name information in the local trie.
     */
    private static class NameInfo {
        final String tzID;
        final GenericNameType type;

        NameInfo(String tzID, GenericNameType type) {
            this.tzID = tzID;
            this.type = type;
        }
    }

    /**
     * A class used for returning the name search result used by
     * {@link TimeZoneGenericNames#find(String, int, EnumSet)}.
     */
    public static class GenericMatchInfo {
        final GenericNameType nameType;
        final String tzID;
        final int matchLength;
        final TimeType timeType;

        private GenericMatchInfo(GenericNameType nameType, String tzID, int matchLength) {
            this(nameType, tzID, matchLength, TimeType.UNKNOWN);
        }

        private GenericMatchInfo(GenericNameType nameType, String tzID, int matchLength, TimeType timeType) {
            this.nameType = nameType;
            this.tzID = tzID;
            this.matchLength = matchLength;
            this.timeType = timeType;
        }

        public GenericNameType nameType() {
            return nameType;
        }

        public String tzID() {
            return tzID;
        }

        public TimeType timeType() {
            return timeType;
        }

        public int matchLength() {
            return matchLength;
        }
    }

    /**
     * A private class implementing the search callback interface in
     * <code>TextTrieMap</code> for collecting match results.
     */
    private static class GenericNameSearchHandler implements ResultHandler<NameInfo> {
        private EnumSet<GenericNameType> _types;
        private Collection<GenericMatchInfo> _matches;
        private int _maxMatchLen;

        GenericNameSearchHandler(EnumSet<GenericNameType> types) {
            _types = types;
        }

        /* (non-Javadoc)
         * @see android.icu.impl.TextTrieMap.ResultHandler#handlePrefixMatch(int, java.util.Iterator)
         */
        @Override
        public boolean handlePrefixMatch(int matchLength, Iterator<NameInfo> values) {
            while (values.hasNext()) {
                NameInfo info = values.next();
                if (_types != null && !_types.contains(info.type)) {
                    continue;
                }
                GenericMatchInfo matchInfo = new GenericMatchInfo(info.type, info.tzID, matchLength);
                if (_matches == null) {
                    _matches = new LinkedList<GenericMatchInfo>();
                }
                _matches.add(matchInfo);
                if (matchLength > _maxMatchLen) {
                    _maxMatchLen = matchLength;
                }
            }
            return true;
        }

        /**
         * Returns the match results
         * @return the match results
         */
        public Collection<GenericMatchInfo> getMatches() {
            return _matches;
        }

        /**
         * Returns the maximum match length, or 0 if no match was found
         * @return the maximum match length
         */
        public int getMaxMatchLen() {
            return _maxMatchLen;
        }

        /**
         * Resets the match results
         */
        public void resetResults() {
            _matches = null;
            _maxMatchLen = 0;
        }
    }

    /**
     * Returns the best match of time zone display name for the specified types in the
     * given text at the given offset.
     * @param text the text
     * @param start the start offset in the text
     * @param genericTypes the set of name types.
     * @return the best matching name info.
     */
    public GenericMatchInfo findBestMatch(String text, int start, EnumSet<GenericNameType> genericTypes) {
        if (text == null || text.length() == 0 || start < 0 || start >= text.length()) {
            throw new IllegalArgumentException("bad input text or range");
        }
        GenericMatchInfo bestMatch = null;

        // Find matches in the TimeZoneNames first
        Collection<MatchInfo> tznamesMatches = findTimeZoneNames(text, start, genericTypes);
        if (tznamesMatches != null) {
            MatchInfo longestMatch = null;
            for (MatchInfo match : tznamesMatches) {
                if (longestMatch == null || match.matchLength() > longestMatch.matchLength()) {
                    longestMatch = match;
                }
            }
            if (longestMatch != null) {
                bestMatch = createGenericMatchInfo(longestMatch);
                if (bestMatch.matchLength() == (text.length() - start)) {
                    // Full match
                    //return bestMatch;

                    // TODO Some time zone uses a same name for the long standard name
                    // and the location name. When the match is a long standard name,
                    // then we need to check if the name is same with the location name.
                    // This is probably a data error or a design bug.
//                    if (bestMatch.nameType != GenericNameType.LONG || bestMatch.timeType != TimeType.STANDARD) {
//                        return bestMatch;
//                    }

                    // TODO The deprecation of commonlyUsed flag introduced the name
                    // conflict not only for long standard names, but short standard names too.
                    // These short names (found in zh_Hant) should be gone once we clean
                    // up CLDR time zone display name data. Once the short name conflict
                    // problem (with location name) is resolved, we should change the condition
                    // below back to the original one above. -Yoshito (2011-09-14)
                    if (bestMatch.timeType != TimeType.STANDARD) {
                        return bestMatch;
                    }
                }
            }
        }

        // Find matches in the local trie
        Collection<GenericMatchInfo> localMatches = findLocal(text, start, genericTypes);
        if (localMatches != null) {
            for (GenericMatchInfo match : localMatches) {
                // TODO See the above TODO. We use match.matchLength() >= bestMatch.matcheLength()
                // for the reason described above.
                //if (bestMatch == null || match.matchLength() > bestMatch.matchLength()) {
                if (bestMatch == null || match.matchLength() >= bestMatch.matchLength()) {
                    bestMatch = match;
                }
            }
        }

        return bestMatch;
    }

    /**
     * Returns a collection of time zone display name matches for the specified types in the
     * given text at the given offset.
     * @param text the text
     * @param start the start offset in the text
     * @param genericTypes the set of name types.
     * @return A collection of match info.
     */
    public Collection<GenericMatchInfo> find(String text, int start, EnumSet<GenericNameType> genericTypes) {
        if (text == null || text.length() == 0 || start < 0 || start >= text.length()) {
            throw new IllegalArgumentException("bad input text or range");
        }
        // Find matches in the local trie
        Collection<GenericMatchInfo> results = findLocal(text, start, genericTypes);

        // Also find matches in the TimeZoneNames
        Collection<MatchInfo> tznamesMatches = findTimeZoneNames(text, start, genericTypes);
        if (tznamesMatches != null) {
            // transform matches and append them to local matches
            for (MatchInfo match : tznamesMatches) {
                if (results == null) {
                    results = new LinkedList<GenericMatchInfo>();
                }
                results.add(createGenericMatchInfo(match));
            }
        }
        return results;
    }

    /**
     * Returns a <code>GenericMatchInfo</code> for the given <code>MatchInfo</code>.
     * @param matchInfo the MatchInfo
     * @return A GenericMatchInfo
     */
    private GenericMatchInfo createGenericMatchInfo(MatchInfo matchInfo) {
        GenericNameType nameType = null;
        TimeType timeType = TimeType.UNKNOWN;
        switch (matchInfo.nameType()) {
        case LONG_STANDARD:
            nameType = GenericNameType.LONG;
            timeType = TimeType.STANDARD;
            break;
        case LONG_GENERIC:
            nameType = GenericNameType.LONG;
            break;
        case SHORT_STANDARD:
            nameType = GenericNameType.SHORT;
            timeType = TimeType.STANDARD;
            break;
        case SHORT_GENERIC:
            nameType = GenericNameType.SHORT;
            break;
        default:
            throw new IllegalArgumentException("Unexpected MatchInfo name type - " + matchInfo.nameType());
        }

        String tzID = matchInfo.tzID();
        if (tzID == null) {
            String mzID = matchInfo.mzID();
            assert(mzID != null);
            tzID = _tznames.getReferenceZoneID(mzID, getTargetRegion());
        }
        assert(tzID != null);

        GenericMatchInfo gmatch = new GenericMatchInfo(nameType, tzID, matchInfo.matchLength(), timeType);

        return gmatch;
    }

    /**
     * Returns a collection of time zone display name matches for the specified types in the
     * given text at the given offset. This method only finds matches from the TimeZoneNames
     * used by this object.
     * @param text the text
     * @param start the start offset in the text
     * @param types the set of name types.
     * @return A collection of match info.
     */
    private Collection<MatchInfo> findTimeZoneNames(String text, int start, EnumSet<GenericNameType> types) {
        Collection<MatchInfo> tznamesMatches = null;

        // Check if the target name type is really in the TimeZoneNames
        EnumSet<NameType> nameTypes = EnumSet.noneOf(NameType.class);
        if (types.contains(GenericNameType.LONG)) {
            nameTypes.add(NameType.LONG_GENERIC);
            nameTypes.add(NameType.LONG_STANDARD);
        }
        if (types.contains(GenericNameType.SHORT)) {
            nameTypes.add(NameType.SHORT_GENERIC);
            nameTypes.add(NameType.SHORT_STANDARD);
        }

        if (!nameTypes.isEmpty()) {
            // Find matches in the TimeZoneNames
            tznamesMatches = _tznames.find(text, start, nameTypes);
        }
        return tznamesMatches;
    }

    /**
     * Returns a collection of time zone display name matches for the specified types in the
     * given text at the given offset. This method only finds matches from the local trie,
     * that contains 1) generic location names and 2) long/short generic partial location names,
     * used by this object.
     * @param text the text
     * @param start the start offset in the text
     * @param types the set of name types.
     * @return A collection of match info.
     */
    private synchronized Collection<GenericMatchInfo> findLocal(String text, int start, EnumSet<GenericNameType> types) {
        GenericNameSearchHandler handler = new GenericNameSearchHandler(types);
        _gnamesTrie.find(text, start, handler);
        if (handler.getMaxMatchLen() == (text.length() - start) || _gnamesTrieFullyLoaded) {
            // perfect match
            return handler.getMatches();
        }

        // All names are not yet loaded into the local trie.
        // Load all available names into the trie. This could be very heavy.

        Set<String> tzIDs = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null);
        for (String tzID : tzIDs) {
            loadStrings(tzID);
        }
        _gnamesTrieFullyLoaded = true;

        // now, try it again
        handler.resetResults();
        _gnamesTrie.find(text, start, handler);
        return handler.getMatches();
    }

    /**
     * <code>TimeZoneGenericNames</code> cache implementation.
     */
    private static class Cache extends SoftCache<String, TimeZoneGenericNames, ULocale> {

        /* (non-Javadoc)
         * @see android.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected TimeZoneGenericNames createInstance(String key, ULocale data) {
            return new TimeZoneGenericNames(data).freeze();
        }

    }

    /*
     * The custom deserialization method.
     * This implementation only read locale used by the object.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

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
    public TimeZoneGenericNames freeze() {
        _frozen = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZoneGenericNames cloneAsThawed() {
        TimeZoneGenericNames copy = null;
        try {
            copy = (TimeZoneGenericNames)super.clone();
            copy._frozen = false;
        } catch (Throwable t) {
            // This should never happen
        }
        return copy;
    }
}
