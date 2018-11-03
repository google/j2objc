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
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import android.icu.impl.TextTrieMap.ResultHandler;
import android.icu.text.TimeZoneNames;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

/**
 * The standard ICU implementation of TimeZoneNames
 * @hide Only a subset of ICU is exposed in Android
 */
public class TimeZoneNamesImpl extends TimeZoneNames {

    private static final long serialVersionUID = -2179814848495897472L;

    private static final String ZONE_STRINGS_BUNDLE = "zoneStrings";
    private static final String MZ_PREFIX = "meta:";

    private static volatile Set<String> METAZONE_IDS;
    private static final TZ2MZsCache TZ_TO_MZS_CACHE = new TZ2MZsCache();
    private static final MZ2TZsCache MZ_TO_TZS_CACHE = new MZ2TZsCache();

    private transient ICUResourceBundle _zoneStrings;


    // These are hard cache. We create only one TimeZoneNamesImpl per locale
    // and it's stored in SoftCache, so we do not need to worry about the
    // footprint much.
    private transient ConcurrentHashMap<String, ZNames> _mzNamesMap;
    private transient ConcurrentHashMap<String, ZNames> _tzNamesMap;
    private transient boolean _namesFullyLoaded;

    private transient TextTrieMap<NameInfo> _namesTrie;
    private transient boolean _namesTrieFullyLoaded;

    public TimeZoneNamesImpl(ULocale locale) {
        initialize(locale);
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getAvailableMetaZoneIDs()
     */
    @Override
    public Set<String> getAvailableMetaZoneIDs() {
        return _getAvailableMetaZoneIDs();
    }

    static Set<String> _getAvailableMetaZoneIDs() {
        if (METAZONE_IDS == null) {
            synchronized (TimeZoneNamesImpl.class) {
                if (METAZONE_IDS == null) {
                    UResourceBundle bundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metaZones");
                    UResourceBundle mapTimezones = bundle.get("mapTimezones");
                    Set<String> keys = mapTimezones.keySet();
                    METAZONE_IDS = Collections.unmodifiableSet(keys);
                }
            }
        }
        return METAZONE_IDS;
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getAvailableMetaZoneIDs(java.lang.String)
     */
    @Override
    public Set<String> getAvailableMetaZoneIDs(String tzID) {
        return _getAvailableMetaZoneIDs(tzID);
    }

    static Set<String> _getAvailableMetaZoneIDs(String tzID) {
        if (tzID == null || tzID.length() == 0) {
            return Collections.emptySet();
        }
        List<MZMapEntry> maps = TZ_TO_MZS_CACHE.getInstance(tzID, tzID);
        if (maps.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> mzIDs = new HashSet<String>(maps.size());
        for (MZMapEntry map : maps) {
            mzIDs.add(map.mzID());
        }
        // make it unmodifiable because of the API contract. We may cache the results in futre.
        return Collections.unmodifiableSet(mzIDs);
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getMetaZoneID(java.lang.String, long)
     */
    @Override
    public String getMetaZoneID(String tzID, long date) {
        return _getMetaZoneID(tzID, date);
    }

    static String _getMetaZoneID(String tzID, long date) {
        if (tzID == null || tzID.length() == 0) {
            return null;
        }
        String mzID = null;
        List<MZMapEntry> maps = TZ_TO_MZS_CACHE.getInstance(tzID, tzID);
        for (MZMapEntry map : maps) {
            if (date >= map.from() && date < map.to()) {
                mzID = map.mzID();
                break;
            }
        }
        return mzID;
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getReferenceZoneID(java.lang.String, java.lang.String)
     */
    @Override
    public String getReferenceZoneID(String mzID, String region) {
        return _getReferenceZoneID(mzID, region);
    }

    static String _getReferenceZoneID(String mzID, String region) {
        if (mzID == null || mzID.length() == 0) {
            return null;
        }
        String refID = null;
        Map<String, String> regionTzMap = MZ_TO_TZS_CACHE.getInstance(mzID, mzID);
        if (!regionTzMap.isEmpty()) {
            refID = regionTzMap.get(region);
            if (refID == null) {
                refID = regionTzMap.get("001");
            }
        }
        return refID;
    }

    /*
     * (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getMetaZoneDisplayName(java.lang.String, android.icu.text.TimeZoneNames.NameType)
     */
    @Override
    public String getMetaZoneDisplayName(String mzID, NameType type) {
        if (mzID == null || mzID.length() == 0) {
            return null;
        }
        return loadMetaZoneNames(mzID).getName(type);
    }

    /*
     * (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getTimeZoneDisplayName(java.lang.String, android.icu.text.TimeZoneNames.NameType)
     */
    @Override
    public String getTimeZoneDisplayName(String tzID, NameType type) {
        if (tzID == null || tzID.length() == 0) {
            return null;
        }
        return loadTimeZoneNames(tzID).getName(type);
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getExemplarLocationName(java.lang.String)
     */
    @Override
    public String getExemplarLocationName(String tzID) {
        if (tzID == null || tzID.length() == 0) {
            return null;
        }
        String locName = loadTimeZoneNames(tzID).getName(NameType.EXEMPLAR_LOCATION);
        return locName;
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#find(java.lang.CharSequence, int, java.util.Set)
     */
    @Override
    public synchronized Collection<MatchInfo> find(CharSequence text, int start, EnumSet<NameType> nameTypes) {
        if (text == null || text.length() == 0 || start < 0 || start >= text.length()) {
            throw new IllegalArgumentException("bad input text or range");
        }
        NameSearchHandler handler = new NameSearchHandler(nameTypes);
        Collection<MatchInfo> matches;

        // First try of lookup.
        matches = doFind(handler, text, start);
        if (matches != null) {
            return matches;
        }

        // All names are not yet loaded into the trie.
        // We may have loaded names for formatting several time zones,
        // and might be parsing one of those.
        // Populate the parsing trie from all of the already-loaded names.
        addAllNamesIntoTrie();

        // Second try of lookup.
        matches = doFind(handler, text, start);
        if (matches != null) {
            return matches;
        }

        // There are still some names we haven't loaded into the trie yet.
        // Load everything now.
        internalLoadAllDisplayNames();

        // Set default time zone location names
        // for time zones without explicit display names.
        // TODO: Should this logic be moved into internalLoadAllDisplayNames?
        Set<String> tzIDs = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null);
        for (String tzID : tzIDs) {
            if (!_tzNamesMap.containsKey(tzID)) {
                ZNames.createTimeZoneAndPutInCache(_tzNamesMap, null, tzID);
            }
        }
        addAllNamesIntoTrie();
        _namesTrieFullyLoaded = true;

        // Third try: we must return this one.
        return doFind(handler, text, start);
    }

    private Collection<MatchInfo> doFind(NameSearchHandler handler, CharSequence text, int start) {
        handler.resetResults();
        _namesTrie.find(text, start, handler);
        if (handler.getMaxMatchLen() == (text.length() - start) || _namesTrieFullyLoaded) {
            return handler.getMatches();
        }
        return null;
    }

    @Override
    public synchronized void loadAllDisplayNames() {
        internalLoadAllDisplayNames();
    }

    @Override
    public void getDisplayNames(String tzID, NameType[] types, long date,
            String[] dest, int destOffset) {
        if (tzID == null || tzID.length() == 0) {
            return;
        }
        ZNames tzNames = loadTimeZoneNames(tzID);
        ZNames mzNames = null;
        for (int i = 0; i < types.length; ++i) {
            NameType type = types[i];
            String name = tzNames.getName(type);
            if (name == null) {
                if (mzNames == null) {
                    String mzID = getMetaZoneID(tzID, date);
                    if (mzID == null || mzID.length() == 0) {
                        mzNames = ZNames.EMPTY_ZNAMES;
                    } else {
                        mzNames = loadMetaZoneNames(mzID);
                    }
                }
                name = mzNames.getName(type);
            }
            dest[destOffset + i] = name;
        }
    }

    /** Caller must synchronize. */
    private void internalLoadAllDisplayNames() {
        if (!_namesFullyLoaded) {
            _namesFullyLoaded = true;
            new ZoneStringsLoader().load();
        }
    }

    /** Caller must synchronize. */
    private void addAllNamesIntoTrie() {
        for (Map.Entry<String, ZNames> entry : _tzNamesMap.entrySet()) {
            entry.getValue().addAsTimeZoneIntoTrie(entry.getKey(), _namesTrie);
        }
        for (Map.Entry<String, ZNames> entry : _mzNamesMap.entrySet()) {
            entry.getValue().addAsMetaZoneIntoTrie(entry.getKey(), _namesTrie);
        }
    }

    /**
     * Loads all meta zone and time zone names for this TimeZoneNames' locale.
     */
    private final class ZoneStringsLoader extends UResource.Sink {
        /**
         * Prepare for several hundred time zones and meta zones.
         * _zoneStrings.getSize() is ineffective in a sparsely populated locale like en-GB.
         */
        private static final int INITIAL_NUM_ZONES = 300;
        private HashMap<UResource.Key, ZNamesLoader> keyToLoader =
                new HashMap<UResource.Key, ZNamesLoader>(INITIAL_NUM_ZONES);
        private StringBuilder sb = new StringBuilder(32);

        /** Caller must synchronize. */
        void load() {
            _zoneStrings.getAllItemsWithFallback("", this);
            for (Map.Entry<UResource.Key, ZNamesLoader> entry : keyToLoader.entrySet()) {
                ZNamesLoader loader = entry.getValue();
                if (loader == ZNamesLoader.DUMMY_LOADER) { continue; }
                UResource.Key key = entry.getKey();

                if (isMetaZone(key)) {
                    String mzID = mzIDFromKey(key);
                    ZNames.createMetaZoneAndPutInCache(_mzNamesMap, loader.getNames(), mzID);
                } else {
                    String tzID = tzIDFromKey(key);
                    ZNames.createTimeZoneAndPutInCache(_tzNamesMap, loader.getNames(), tzID);
                }
            }
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table timeZonesTable = value.getTable();
            for (int j = 0; timeZonesTable.getKeyAndValue(j, key, value); ++j) {
                assert !value.isNoInheritanceMarker();
                if (value.getType() == UResourceBundle.TABLE) {
                    consumeNamesTable(key, value, noFallback);
                } else {
                    // Ignore fields that aren't tables (e.g., fallbackFormat and regionFormatStandard).
                    // All time zone fields are tables.
                }
            }
        }

        private void consumeNamesTable(UResource.Key key, UResource.Value value, boolean noFallback) {
            ZNamesLoader loader = keyToLoader.get(key);
            if (loader == null) {
                if (isMetaZone(key)) {
                    String mzID = mzIDFromKey(key);
                    if (_mzNamesMap.containsKey(mzID)) {
                        // We have already loaded the names for this meta zone.
                        loader = ZNamesLoader.DUMMY_LOADER;
                    } else {
                        loader = new ZNamesLoader();
                    }
                } else {
                    String tzID = tzIDFromKey(key);
                    if (_tzNamesMap.containsKey(tzID)) {
                        // We have already loaded the names for this time zone.
                        loader = ZNamesLoader.DUMMY_LOADER;
                    } else {
                        loader = new ZNamesLoader();
                    }
                }

                UResource.Key newKey = createKey(key);
                keyToLoader.put(newKey, loader);
            }

            if (loader != ZNamesLoader.DUMMY_LOADER) {
                // Let the ZNamesLoader consume the names table.
                loader.put(key, value, noFallback);
            }
        }

        UResource.Key createKey(UResource.Key key) {
            return key.clone();
        }

        boolean isMetaZone(UResource.Key key) {
            return key.startsWith(MZ_PREFIX);
        }

        /**
         * Equivalent to key.substring(MZ_PREFIX.length())
         * except reuses our StringBuilder.
         */
        private String mzIDFromKey(UResource.Key key) {
            sb.setLength(0);
            for (int i = MZ_PREFIX.length(); i < key.length(); ++i) {
                sb.append(key.charAt(i));
            }
            return sb.toString();
        }

        private String tzIDFromKey(UResource.Key key) {
            sb.setLength(0);
            for (int i = 0; i < key.length(); ++i) {
                char c = key.charAt(i);
                if (c == ':') {
                    c = '/';
                }
                sb.append(c);
            }
            return sb.toString();
        }
    }

    /**
     * Initialize the transient fields, called from the constructor and
     * readObject.
     *
     * @param locale The locale
     */
    private void initialize(ULocale locale) {
        ICUResourceBundle bundle = (ICUResourceBundle)ICUResourceBundle.getBundleInstance(
                ICUData.ICU_ZONE_BASE_NAME, locale);
        _zoneStrings = (ICUResourceBundle)bundle.get(ZONE_STRINGS_BUNDLE);

        // TODO: Access is synchronized, can we use a non-concurrent map?
        _tzNamesMap = new ConcurrentHashMap<String, ZNames>();
        _mzNamesMap = new ConcurrentHashMap<String, ZNames>();
        _namesFullyLoaded = false;

        _namesTrie = new TextTrieMap<NameInfo>(true);
        _namesTrieFullyLoaded = false;

        // Preload zone strings for the default time zone
        TimeZone tz = TimeZone.getDefault();
        String tzCanonicalID = ZoneMeta.getCanonicalCLDRID(tz);
        if (tzCanonicalID != null) {
            loadStrings(tzCanonicalID);
        }
    }

    /**
     * Load all strings used by the specified time zone.
     * This is called from the initializer to load default zone's
     * strings.
     * @param tzCanonicalID the canonical time zone ID
     */
    private synchronized void loadStrings(String tzCanonicalID) {
        if (tzCanonicalID == null || tzCanonicalID.length() == 0) {
            return;
        }
        loadTimeZoneNames(tzCanonicalID);

        Set<String> mzIDs = getAvailableMetaZoneIDs(tzCanonicalID);
        for (String mzID : mzIDs) {
            loadMetaZoneNames(mzID);
        }
    }

    /*
     * The custom serialization method.
     * This implementation only preserve locale object used for the names.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        ULocale locale = _zoneStrings.getULocale();
        out.writeObject(locale);
    }

    /*
     * The custom deserialization method.
     * This implementation only read locale object used by the object.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ULocale locale = (ULocale)in.readObject();
        initialize(locale);
    }

    /**
     * Returns a set of names for the given meta zone ID. This method loads
     * the set of names into the internal map and trie for future references.
     * @param mzID the meta zone ID
     * @return An instance of ZNames that includes a set of meta zone display names.
     */
    private synchronized ZNames loadMetaZoneNames(String mzID) {
        ZNames mznames = _mzNamesMap.get(mzID);
        if (mznames == null) {
            ZNamesLoader loader = new ZNamesLoader();
            loader.loadMetaZone(_zoneStrings, mzID);
            mznames = ZNames.createMetaZoneAndPutInCache(_mzNamesMap, loader.getNames(), mzID);
        }
        return mznames;
    }

    /**
     * Returns a set of names for the given time zone ID. This method loads
     * the set of names into the internal map and trie for future references.
     * @param tzID the canonical time zone ID
     * @return An instance of ZNames that includes a set of time zone display names.
     */
    private synchronized ZNames loadTimeZoneNames(String tzID) {
        ZNames tznames = _tzNamesMap.get(tzID);
        if (tznames == null) {
            ZNamesLoader loader = new ZNamesLoader();
            loader.loadTimeZone(_zoneStrings, tzID);
            tznames = ZNames.createTimeZoneAndPutInCache(_tzNamesMap, loader.getNames(), tzID);
        }
        return tznames;
    }

    /**
     * An instance of NameInfo is stored in the zone names trie.
     */
    private static class NameInfo {
        String tzID;
        String mzID;
        NameType type;
    }

    /**
     * NameSearchHandler is used for collecting name matches.
     */
    private static class NameSearchHandler implements ResultHandler<NameInfo> {
        private EnumSet<NameType> _nameTypes;
        private Collection<MatchInfo> _matches;
        private int _maxMatchLen;

        NameSearchHandler(EnumSet<NameType> nameTypes) {
            _nameTypes = nameTypes;
        }

        /* (non-Javadoc)
         * @see android.icu.impl.TextTrieMap.ResultHandler#handlePrefixMatch(int, java.util.Iterator)
         */
        @Override
        public boolean handlePrefixMatch(int matchLength, Iterator<NameInfo> values) {
            while (values.hasNext()) {
                NameInfo ninfo = values.next();
                if (_nameTypes != null && !_nameTypes.contains(ninfo.type)) {
                    continue;
                }
                MatchInfo minfo;
                if (ninfo.tzID != null) {
                    minfo = new MatchInfo(ninfo.type, ninfo.tzID, null, matchLength);
                } else {
                    assert(ninfo.mzID != null);
                    minfo = new MatchInfo(ninfo.type, null, ninfo.mzID, matchLength);
                }
                if (_matches == null) {
                    _matches = new LinkedList<MatchInfo>();
                }
                _matches.add(minfo);
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
        public Collection<MatchInfo> getMatches() {
            if (_matches == null) {
                return Collections.emptyList();
            }
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

    private static final class ZNamesLoader extends UResource.Sink {
        private String[] names;

        /**
         * Does not load any names, for no-fallback handling.
         */
        private static ZNamesLoader DUMMY_LOADER = new ZNamesLoader();

        void loadMetaZone(ICUResourceBundle zoneStrings, String mzID) {
            String key = MZ_PREFIX + mzID;
            loadNames(zoneStrings, key);
        }

        void loadTimeZone(ICUResourceBundle zoneStrings, String tzID) {
            String key = tzID.replace('/', ':');
            loadNames(zoneStrings, key);
        }

        void loadNames(ICUResourceBundle zoneStrings, String key) {
            assert zoneStrings != null;
            assert key != null;
            assert key.length() > 0;

            // Reset names so that this instance can be used to load data multiple times.
            names = null;
            try {
                zoneStrings.getAllItemsWithFallback(key, this);
            } catch (MissingResourceException e) {
            }
        }

        private static ZNames.NameTypeIndex nameTypeIndexFromKey(UResource.Key key) {
            // Avoid key.toString() object creation.
            if (key.length() != 2) {
                return null;
            }
            char c0 = key.charAt(0);
            char c1 = key.charAt(1);
            if (c0 == 'l') {
                return c1 == 'g' ? ZNames.NameTypeIndex.LONG_GENERIC :
                        c1 == 's' ? ZNames.NameTypeIndex.LONG_STANDARD :
                            c1 == 'd' ? ZNames.NameTypeIndex.LONG_DAYLIGHT : null;
            } else if (c0 == 's') {
                return c1 == 'g' ? ZNames.NameTypeIndex.SHORT_GENERIC :
                        c1 == 's' ? ZNames.NameTypeIndex.SHORT_STANDARD :
                            c1 == 'd' ? ZNames.NameTypeIndex.SHORT_DAYLIGHT : null;
            } else if (c0 == 'e' && c1 == 'c') {
                return ZNames.NameTypeIndex.EXEMPLAR_LOCATION;
            }
            return null;
        }

        private void setNameIfEmpty(UResource.Key key, UResource.Value value) {
            if (names == null) {
                names = new String[ZNames.NUM_NAME_TYPES];
            }
            ZNames.NameTypeIndex index = nameTypeIndexFromKey(key);
            if (index == null) { return; }
            assert index.ordinal() < ZNames.NUM_NAME_TYPES;
            if (names[index.ordinal()] == null) {
                names[index.ordinal()] = value.getString();
            }
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table namesTable = value.getTable();
            for (int i = 0; namesTable.getKeyAndValue(i, key, value); ++i) {
                assert value.getType() == UResourceBundle.STRING;
                setNameIfEmpty(key, value);  // could be value.isNoInheritanceMarker()
            }
        }

        private String[] getNames() {
            if (Utility.sameObjects(names, null)) {
                return null;
            }
            int length = 0;
            for (int i = 0; i < ZNames.NUM_NAME_TYPES; ++i) {
                String name = names[i];
                if (name != null) {
                    if (name.equals(ICUResourceBundle.NO_INHERITANCE_MARKER)) {
                        names[i] = null;
                    } else {
                        length = i + 1;
                    }
                }
            }

            String[] result;
            if (length == ZNames.NUM_NAME_TYPES) {
                // Return the full array if the last name is set.
                result = names;
            } else if (length == 0) {
                // Return null instead of a zero-length array.
                result = null;
            } else {
                // Return a shorter array for permanent storage.
                // Copy all names into the minimal array.
                result = Arrays.copyOfRange(names, 0, length);
            }
            return result;
        }
    }

    /**
     * This class stores name data for a meta zone or time zone.
     */
    private static class ZNames {
        /**
         * Private enum corresponding to the public TimeZoneNames::NameType for the order in
         * which fields are stored in a ZNames instance.  EXEMPLAR_LOCATION is stored first
         * for efficiency.
         */
        private static enum NameTypeIndex {
            EXEMPLAR_LOCATION, LONG_GENERIC, LONG_STANDARD, LONG_DAYLIGHT, SHORT_GENERIC, SHORT_STANDARD, SHORT_DAYLIGHT;
            /* J2ObjC: renamed to avoid collision with the generated array of values. */
            static final NameTypeIndex values_temp[] = values();
        };

        public static final int NUM_NAME_TYPES = 7;

        private static int getNameTypeIndex(NameType type) {
            switch (type) {
            case EXEMPLAR_LOCATION:
                return NameTypeIndex.EXEMPLAR_LOCATION.ordinal();
            case LONG_GENERIC:
                return NameTypeIndex.LONG_GENERIC.ordinal();
            case LONG_STANDARD:
                return NameTypeIndex.LONG_STANDARD.ordinal();
            case LONG_DAYLIGHT:
                return NameTypeIndex.LONG_DAYLIGHT.ordinal();
            case SHORT_GENERIC:
                return NameTypeIndex.SHORT_GENERIC.ordinal();
            case SHORT_STANDARD:
                return NameTypeIndex.SHORT_STANDARD.ordinal();
            case SHORT_DAYLIGHT:
                return NameTypeIndex.SHORT_DAYLIGHT.ordinal();
            default:
                throw new AssertionError("No NameTypeIndex match for " + type);
            }
        }

        private static NameType getNameType(int index) {
            switch (NameTypeIndex.values_temp[index]) {
            case EXEMPLAR_LOCATION:
                return NameType.EXEMPLAR_LOCATION;
            case LONG_GENERIC:
                return NameType.LONG_GENERIC;
            case LONG_STANDARD:
                return NameType.LONG_STANDARD;
            case LONG_DAYLIGHT:
                return NameType.LONG_DAYLIGHT;
            case SHORT_GENERIC:
                return NameType.SHORT_GENERIC;
            case SHORT_STANDARD:
                return NameType.SHORT_STANDARD;
            case SHORT_DAYLIGHT:
                return NameType.SHORT_DAYLIGHT;
            default:
                throw new AssertionError("No NameType match for " + index);
            }
        }

        static final ZNames EMPTY_ZNAMES = new ZNames(null);
        // A meta zone names instance never has an exemplar location string.
        private static final int EX_LOC_INDEX = NameTypeIndex.EXEMPLAR_LOCATION.ordinal();

        private String[] _names;
        private boolean didAddIntoTrie;

        protected ZNames(String[] names) {
            _names = names;
            didAddIntoTrie = names == null;
        }

        public static ZNames createMetaZoneAndPutInCache(Map<String, ZNames> cache,
                String[] names, String mzID) {
            String key = mzID.intern();
            ZNames value;
            if (names == null) {
                value = EMPTY_ZNAMES;
            } else {
                value = new ZNames(names);
            }
            cache.put(key, value);
            return value;
        }

        public static ZNames createTimeZoneAndPutInCache(Map<String, ZNames> cache,
                String[] names, String tzID) {
            // For time zones, check that the exemplar city name is populated.  If necessary, use
            // "getDefaultExemplarLocationName" to extract it from the time zone name.
            names = (names == null) ? new String[EX_LOC_INDEX + 1] : names;
            if (names[EX_LOC_INDEX] == null) {
                names[EX_LOC_INDEX] = getDefaultExemplarLocationName(tzID);
            }

            String key = tzID.intern();
            ZNames value = new ZNames(names);
            cache.put(key, value);
            return value;
        }

        public String getName(NameType type) {
            int index = getNameTypeIndex(type);
            if (_names != null && index < _names.length) {
                return _names[index];
            } else {
                return null;
            }
        }

        public void addAsMetaZoneIntoTrie(String mzID, TextTrieMap<NameInfo> trie) {
            addNamesIntoTrie(mzID, null, trie);
        }

        public void addAsTimeZoneIntoTrie(String tzID, TextTrieMap<NameInfo> trie) {
            addNamesIntoTrie(null, tzID, trie);
        }

        private void addNamesIntoTrie(String mzID, String tzID, TextTrieMap<NameInfo> trie) {
            if (_names == null || didAddIntoTrie) {
                return;
            }
            didAddIntoTrie = true;

            for (int i = 0; i < _names.length; ++i) {
                String name = _names[i];
                if (name != null) {
                    NameInfo info = new NameInfo();
                    info.mzID = mzID;
                    info.tzID = tzID;
                    info.type = getNameType(i);
                    trie.put(name, info);
                }
            }
        }
    }

    //
    // Canonical time zone ID -> meta zone ID
    //

    private static class MZMapEntry {
        private String _mzID;
        private long _from;
        private long _to;

        MZMapEntry(String mzID, long from, long to) {
            _mzID = mzID;
            _from = from;
            _to = to;
        }

        String mzID() {
            return _mzID;
        }

        long from() {
            return _from;
        }

        long to() {
            return _to;
        }
    }

    private static class TZ2MZsCache extends SoftCache<String, List<MZMapEntry>, String> {
        /* (non-Javadoc)
         * @see android.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected List<MZMapEntry> createInstance(String key, String data) {
            List<MZMapEntry> mzMaps = null;

            UResourceBundle bundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metaZones");
            UResourceBundle metazoneInfoBundle = bundle.get("metazoneInfo");

            String tzkey = data.replace('/', ':');
            try {
                UResourceBundle zoneBundle = metazoneInfoBundle.get(tzkey);

                mzMaps = new ArrayList<MZMapEntry>(zoneBundle.getSize());
                for (int idx = 0; idx < zoneBundle.getSize(); idx++) {
                    UResourceBundle mz = zoneBundle.get(idx);
                    String mzid = mz.getString(0);
                    String fromStr = "1970-01-01 00:00";
                    String toStr = "9999-12-31 23:59";
                    if (mz.getSize() == 3) {
                        fromStr = mz.getString(1);
                        toStr = mz.getString(2);
                    }
                    long from, to;
                    from = parseDate(fromStr);
                    to = parseDate(toStr);
                    mzMaps.add(new MZMapEntry(mzid, from, to));
                }

            } catch (MissingResourceException mre) {
                mzMaps = Collections.emptyList();
            }
            return mzMaps;
        }

        /**
         * Private static method parsing the date text used by meta zone to
         * time zone mapping data in locale resource.
         *
         * @param text the UTC date text in the format of "yyyy-MM-dd HH:mm",
         * for example - "1970-01-01 00:00"
         * @return the date
         */
        private static long parseDate (String text) {
            int year = 0, month = 0, day = 0, hour = 0, min = 0;
            int idx;
            int n;

            // "yyyy" (0 - 3)
            for (idx = 0; idx <= 3; idx++) {
                n = text.charAt(idx) - '0';
                if (n >= 0 && n < 10) {
                    year = 10*year + n;
                } else {
                    throw new IllegalArgumentException("Bad year");
                }
            }
            // "MM" (5 - 6)
            for (idx = 5; idx <= 6; idx++) {
                n = text.charAt(idx) - '0';
                if (n >= 0 && n < 10) {
                    month = 10*month + n;
                } else {
                    throw new IllegalArgumentException("Bad month");
                }
            }
            // "dd" (8 - 9)
            for (idx = 8; idx <= 9; idx++) {
                n = text.charAt(idx) - '0';
                if (n >= 0 && n < 10) {
                    day = 10*day + n;
                } else {
                    throw new IllegalArgumentException("Bad day");
                }
            }
            // "HH" (11 - 12)
            for (idx = 11; idx <= 12; idx++) {
                n = text.charAt(idx) - '0';
                if (n >= 0 && n < 10) {
                    hour = 10*hour + n;
                } else {
                    throw new IllegalArgumentException("Bad hour");
                }
            }
            // "mm" (14 - 15)
            for (idx = 14; idx <= 15; idx++) {
                n = text.charAt(idx) - '0';
                if (n >= 0 && n < 10) {
                    min = 10*min + n;
                } else {
                    throw new IllegalArgumentException("Bad minute");
                }
            }

            long date = Grego.fieldsToDay(year, month - 1, day) * Grego.MILLIS_PER_DAY
                        + (long)hour * Grego.MILLIS_PER_HOUR + (long)min * Grego.MILLIS_PER_MINUTE;
            return date;
         }
    }

    //
    // Meta zone ID -> time zone ID
    //

    private static class MZ2TZsCache extends SoftCache<String, Map<String, String>, String> {

        /* (non-Javadoc)
         * @see android.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected Map<String, String> createInstance(String key, String data) {
            Map<String, String> map = null;

            UResourceBundle bundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metaZones");
            UResourceBundle mapTimezones = bundle.get("mapTimezones");

            try {
                UResourceBundle regionMap = mapTimezones.get(key);

                Set<String> regions = regionMap.keySet();
                map = new HashMap<String, String>(regions.size());

                for (String region : regions) {
                    String tzID = regionMap.getString(region).intern();
                    map.put(region.intern(), tzID);
                }
            } catch (MissingResourceException e) {
                map = Collections.emptyMap();
            }
            return map;
        }
    }

    private static final Pattern LOC_EXCLUSION_PATTERN = Pattern.compile("Etc/.*|SystemV/.*|.*/Riyadh8[7-9]");

    /**
     * Default exemplar location name based on time zone ID.
     * For example, "America/New_York" -> "New York"
     * @param tzID the time zone ID
     * @return the exemplar location name or null if location is not available.
     */
    public static String getDefaultExemplarLocationName(String tzID) {
        if (tzID == null || tzID.length() == 0 || LOC_EXCLUSION_PATTERN.matcher(tzID).matches()) {
            return null;
        }

        String location = null;
        int sep = tzID.lastIndexOf('/');
        if (sep > 0 && sep + 1 < tzID.length()) {
            location = tzID.substring(sep + 1).replace('_', ' ');
        }

        return location;
    }
}
