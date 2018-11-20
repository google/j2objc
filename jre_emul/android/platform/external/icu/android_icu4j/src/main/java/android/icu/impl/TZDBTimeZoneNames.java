/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.icu.impl.TextTrieMap.ResultHandler;
import android.icu.text.TimeZoneNames;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

/**
 * Yet another TimeZoneNames implementation based on the tz database.
 * This implementation contains only tz abbreviations (short standard
 * and daylight names) for each metazone.
 *
 * The data file $ICU4C_ROOT/source/data/zone/tzdbNames.txt contains
 * the metazone - abbreviations mapping data (manually edited).
 *
 * Note: The abbreviations in the tz database are not necessarily
 * unique. For example, parsing abbreviation "IST" is ambiguous
 * (can be parsed as India Standard Time or Israel Standard Time).
 * The data file (tzdbNames.txt) contains regional mapping, and
 * the locale in the constructor is used as a hint for resolving
 * these ambiguous names.
 * @hide Only a subset of ICU is exposed in Android
 */
public class TZDBTimeZoneNames extends TimeZoneNames {
    private static final long serialVersionUID = 1L;

    private static final ConcurrentHashMap<String, TZDBNames> TZDB_NAMES_MAP =
            new ConcurrentHashMap<String, TZDBNames>();

    private static volatile TextTrieMap<TZDBNameInfo> TZDB_NAMES_TRIE = null;

    private static final ICUResourceBundle ZONESTRINGS;
    static {
        UResourceBundle bundle = ICUResourceBundle
                .getBundleInstance(ICUData.ICU_ZONE_BASE_NAME, "tzdbNames");
        ZONESTRINGS = (ICUResourceBundle)bundle.get("zoneStrings");
    }

    private ULocale _locale;
    private transient volatile String _region;

    public TZDBTimeZoneNames(ULocale loc) {
        _locale = loc;
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getAvailableMetaZoneIDs()
     */
    @Override
    public Set<String> getAvailableMetaZoneIDs() {
        return TimeZoneNamesImpl._getAvailableMetaZoneIDs();
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getAvailableMetaZoneIDs(java.lang.String)
     */
    @Override
    public Set<String> getAvailableMetaZoneIDs(String tzID) {
        return TimeZoneNamesImpl._getAvailableMetaZoneIDs(tzID);
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getMetaZoneID(java.lang.String, long)
     */
    @Override
    public String getMetaZoneID(String tzID, long date) {
        return TimeZoneNamesImpl._getMetaZoneID(tzID, date);
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getReferenceZoneID(java.lang.String, java.lang.String)
     */
    @Override
    public String getReferenceZoneID(String mzID, String region) {
        return TimeZoneNamesImpl._getReferenceZoneID(mzID, region);
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getMetaZoneDisplayName(java.lang.String,
     *      android.icu.text.TimeZoneNames.NameType)
     */
    @Override
    public String getMetaZoneDisplayName(String mzID, NameType type) {
        if (mzID == null || mzID.length() == 0 ||
                (type != NameType.SHORT_STANDARD && type != NameType.SHORT_DAYLIGHT)) {
            return null;
        }
        return getMetaZoneNames(mzID).getName(type);
    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#getTimeZoneDisplayName(java.lang.String,
     *      android.icu.text.TimeZoneNames.NameType)
     */
    @Override
    public String getTimeZoneDisplayName(String tzID, NameType type) {
        // No abbreviations associated a zone directly for now.
        return null;
    }

//    /* (non-Javadoc)
//     * @see android.icu.text.TimeZoneNames#getExemplarLocationName(java.lang.String)
//     */
//    public String getExemplarLocationName(String tzID) {
//        return super.getExemplarLocationName(tzID);
//    }

    /* (non-Javadoc)
     * @see android.icu.text.TimeZoneNames#find(java.lang.CharSequence, int, java.util.EnumSet)
     */
    @Override
    public Collection<MatchInfo> find(CharSequence text, int start, EnumSet<NameType> nameTypes) {
        if (text == null || text.length() == 0 || start < 0 || start >= text.length()) {
            throw new IllegalArgumentException("bad input text or range");
        }

        prepareFind();
        TZDBNameSearchHandler handler = new TZDBNameSearchHandler(nameTypes, getTargetRegion());
        TZDB_NAMES_TRIE.find(text, start, handler);
        return handler.getMatches();
    }

    private static class TZDBNames {
        public static final TZDBNames EMPTY_TZDBNAMES = new TZDBNames(null, null);

        private String[] _names;
        private String[] _parseRegions;
        private static final String[] KEYS = {"ss", "sd"};

        private TZDBNames(String[] names, String[] parseRegions) {
            _names = names;
            _parseRegions = parseRegions;
        }

        static TZDBNames getInstance(ICUResourceBundle zoneStrings, String key) {
            if (zoneStrings == null || key == null || key.length() == 0) {
                return EMPTY_TZDBNAMES;
            }

            ICUResourceBundle table = null;
            try {
                table = (ICUResourceBundle)zoneStrings.get(key);
            } catch (MissingResourceException e) {
                return EMPTY_TZDBNAMES;
            }

            boolean isEmpty = true;
            String[] names = new String[KEYS.length];
            for (int i = 0; i < names.length; i++) {
                try {
                    names[i] = table.getString(KEYS[i]);
                    isEmpty = false;
                } catch (MissingResourceException e) {
                    names[i] = null;
                }
            }

            if (isEmpty) {
                return EMPTY_TZDBNAMES;
            }

            String[] parseRegions = null;
            try {
                ICUResourceBundle regionsRes = (ICUResourceBundle)table.get("parseRegions");
                if (regionsRes.getType() == UResourceBundle.STRING) {
                    parseRegions = new String[1];
                    parseRegions[0] = regionsRes.getString();
                } else if (regionsRes.getType() == UResourceBundle.ARRAY) {
                    parseRegions = regionsRes.getStringArray();
                }
            } catch (MissingResourceException e) {
                // fall through
            }

            return new TZDBNames(names, parseRegions);
        }

        String getName(NameType type) {
            if (_names == null) {
                return null;
            }
            String name = null;
            switch (type) {
            case SHORT_STANDARD:
                name = _names[0];
                break;
            case SHORT_DAYLIGHT:
                name = _names[1];
                break;
            }

            return name;
        }

        String[] getParseRegions() {
            return _parseRegions;
        }
    }

    private static class TZDBNameInfo {
        final String mzID;
        final NameType type;
        final boolean ambiguousType;
        final String[] parseRegions;

        TZDBNameInfo(String mzID, NameType type, boolean ambiguousType, String[] parseRegions) {
            this.mzID = mzID;
            this.type = type;
            this.ambiguousType = ambiguousType;
            this.parseRegions = parseRegions;
        }
    }

    private static class TZDBNameSearchHandler implements ResultHandler<TZDBNameInfo> {
        private EnumSet<NameType> _nameTypes;
        private Collection<MatchInfo> _matches;
        private String _region;

        TZDBNameSearchHandler(EnumSet<NameType> nameTypes, String region) {
            _nameTypes = nameTypes;
            assert region != null;
            _region = region;
        }

        /* (non-Javadoc)
         * @see android.icu.impl.TextTrieMap.ResultHandler#handlePrefixMatch(int,
         *      java.util.Iterator)
         */
        @Override
        public boolean handlePrefixMatch(int matchLength, Iterator<TZDBNameInfo> values) {
            TZDBNameInfo match = null;
            TZDBNameInfo defaultRegionMatch = null;

            while (values.hasNext()) {
                TZDBNameInfo ninfo = values.next();

                if (_nameTypes != null && !_nameTypes.contains(ninfo.type)) {
                    continue;
                }

                // Some tz database abbreviations are ambiguous. For example,
                // CST means either Central Standard Time or China Standard Time.
                // Unlike CLDR time zone display names, this implementation
                // does not use unique names. And TimeZoneFormat does not expect
                // multiple results returned for the same time zone type.
                // For this reason, this implementation resolve one among same
                // zone type with a same name at this level.
                if (ninfo.parseRegions == null) {
                    // parseRegions == null means this is the default metazone
                    // mapping for the abbreviation.
                    if (defaultRegionMatch == null) {
                        match = defaultRegionMatch = ninfo;
                    }
                } else {
                    boolean matchRegion = false;
                    // non-default metazone mapping for an abbreviation
                    // comes with applicable regions. For example, the default
                    // metazone mapping for "CST" is America_Central,
                    // but if region is one of CN/MO/TW, "CST" is parsed
                    // as metazone China (China Standard Time).
                    for (String region : ninfo.parseRegions) {
                        if (_region.equals(region)) {
                            match = ninfo;
                            matchRegion = true;
                            break;
                        }
                    }
                    if (matchRegion) {
                        break;
                    }
                    if (match == null) {
                        match = ninfo;
                    }
                }
            }

            if (match != null) {
                NameType ntype = match.type;
                // Note: Workaround for duplicated standard/daylight names
                // The tz database contains a few zones sharing a
                // same name for both standard time and daylight saving
                // time. For example, Australia/Sydney observes DST,
                // but "EST" is used for both standard and daylight.
                // When both SHORT_STANDARD and SHORT_DAYLIGHT are included
                // in the find operation, we cannot tell which one was
                // actually matched.
                // TimeZoneFormat#parse returns a matched name type (standard
                // or daylight) and DateFormat implementation uses the info to
                // to adjust actual time. To avoid false type information,
                // this implementation replaces the name type with SHORT_GENERIC.
                if (match.ambiguousType
                        && (ntype == NameType.SHORT_STANDARD || ntype == NameType.SHORT_DAYLIGHT)
                        && _nameTypes.contains(NameType.SHORT_STANDARD)
                        && _nameTypes.contains(NameType.SHORT_DAYLIGHT)) {
                    ntype = NameType.SHORT_GENERIC;
                }
                MatchInfo minfo = new MatchInfo(ntype, null, match.mzID, matchLength);
                if (_matches == null) {
                    _matches = new LinkedList<MatchInfo>();
                }
                _matches.add(minfo);
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
    }

    private static TZDBNames getMetaZoneNames(String mzID) {
        TZDBNames names = TZDB_NAMES_MAP.get(mzID);
        if (names == null) {
            names = TZDBNames.getInstance(ZONESTRINGS, "meta:" + mzID);
            mzID = mzID.intern();
            TZDBNames tmpNames = TZDB_NAMES_MAP.putIfAbsent(mzID, names);
            names = (tmpNames == null) ? names : tmpNames;
        }
        return names;
    }

    private static void prepareFind() {
        if (TZDB_NAMES_TRIE == null) {
            synchronized(TZDBTimeZoneNames.class) {
                if (TZDB_NAMES_TRIE == null) {
                    // loading all names into trie
                    TextTrieMap<TZDBNameInfo> trie = new TextTrieMap<TZDBNameInfo>(true);
                    Set<String> mzIDs = TimeZoneNamesImpl._getAvailableMetaZoneIDs();
                    for (String mzID : mzIDs) {
                        TZDBNames names = getMetaZoneNames(mzID);
                        String std = names.getName(NameType.SHORT_STANDARD);
                        String dst = names.getName(NameType.SHORT_DAYLIGHT);
                        if (std == null && dst == null) {
                            continue;
                        }
                        String[] parseRegions = names.getParseRegions();
                        mzID = mzID.intern();

                        // The tz database contains a few zones sharing a
                        // same name for both standard time and daylight saving
                        // time. For example, Australia/Sydney observes DST,
                        // but "EST" is used for both standard and daylight.
                        // we need to store the information for later processing.
                        boolean ambiguousType = (std != null && dst != null && std.equals(dst));

                        if (std != null) {
                            TZDBNameInfo stdInf = new TZDBNameInfo(mzID,
                                    NameType.SHORT_STANDARD,
                                    ambiguousType,
                                    parseRegions);
                            trie.put(std, stdInf);
                        }
                        if (dst != null) {
                            TZDBNameInfo dstInf = new TZDBNameInfo(mzID,
                                    NameType.SHORT_DAYLIGHT,
                                    ambiguousType,
                                    parseRegions);
                            trie.put(dst, dstInf);
                        }
                    }
                    TZDB_NAMES_TRIE = trie;
                }
            }
        }
    }

    private String getTargetRegion() {
        if (_region == null) {
            String region = _locale.getCountry();
            if (region.length() == 0) {
                ULocale tmp = ULocale.addLikelySubtags(_locale);
                region = tmp.getCountry();
                if (region.length() == 0) {
                    region = "001";
                }
            }
            _region = region;
        }
        return _region;
    }
}
