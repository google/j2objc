/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2008-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

import android.icu.text.PluralRanges;
import android.icu.text.PluralRules;
import android.icu.text.PluralRules.PluralType;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

/**
 * Loader for plural rules data.
 * @hide Only a subset of ICU is exposed in Android
 */
public class PluralRulesLoader extends PluralRules.Factory {
    private final Map<String, PluralRules> rulesIdToRules;
    // lazy init, use getLocaleIdToRulesIdMap to access
    private Map<String, String> localeIdToCardinalRulesId;
    private Map<String, String> localeIdToOrdinalRulesId;
    private Map<String, ULocale> rulesIdToEquivalentULocale;
    private static Map<String, PluralRanges> localeIdToPluralRanges;


    /**
     * Access through singleton.
     */
    private PluralRulesLoader() {
        rulesIdToRules = new HashMap<String, PluralRules>();
    }

    /**
     * Returns the locales for which we have plurals data. Utility for testing.
     */
    public ULocale[] getAvailableULocales() {
        Set<String> keys = getLocaleIdToRulesIdMap(PluralType.CARDINAL).keySet();
        ULocale[] locales = new ULocale[keys.size()];
        int n = 0;
        for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
            locales[n++] = ULocale.createCanonical(iter.next());
        }
        return locales;
    }

    /**
     * Returns the functionally equivalent locale.
     */
    public ULocale getFunctionalEquivalent(ULocale locale, boolean[] isAvailable) {
        if (isAvailable != null && isAvailable.length > 0) {
            String localeId = ULocale.canonicalize(locale.getBaseName());
            Map<String, String> idMap = getLocaleIdToRulesIdMap(PluralType.CARDINAL);
            isAvailable[0] = idMap.containsKey(localeId);
        }

        String rulesId = getRulesIdForLocale(locale, PluralType.CARDINAL);
        if (rulesId == null || rulesId.trim().length() == 0) {
            return ULocale.ROOT; // ultimate fallback
        }

        ULocale result = getRulesIdToEquivalentULocaleMap().get(
                rulesId);
        if (result == null) {
            return ULocale.ROOT; // ultimate fallback
        }

        return result;
    }

    /**
     * Returns the lazily-constructed map.
     */
    private Map<String, String> getLocaleIdToRulesIdMap(PluralType type) {
        checkBuildRulesIdMaps();
        return (type == PluralType.CARDINAL) ? localeIdToCardinalRulesId : localeIdToOrdinalRulesId;
    }

    /**
     * Returns the lazily-constructed map.
     */
    private Map<String, ULocale> getRulesIdToEquivalentULocaleMap() {
        checkBuildRulesIdMaps();
        return rulesIdToEquivalentULocale;
    }

    /**
     * Lazily constructs the localeIdToRulesId and rulesIdToEquivalentULocale
     * maps if necessary. These exactly reflect the contents of the locales
     * resource in plurals.res.
     */
    private void checkBuildRulesIdMaps() {
        boolean haveMap;
        synchronized (this) {
            haveMap = localeIdToCardinalRulesId != null;
        }
        if (!haveMap) {
            Map<String, String> tempLocaleIdToCardinalRulesId;
            Map<String, String> tempLocaleIdToOrdinalRulesId;
            Map<String, ULocale> tempRulesIdToEquivalentULocale;
            try {
                UResourceBundle pluralb = getPluralBundle();
                // Read cardinal-number rules.
                UResourceBundle localeb = pluralb.get("locales");

                // sort for convenience of getAvailableULocales
                tempLocaleIdToCardinalRulesId = new TreeMap<String, String>();
                // not visible
                tempRulesIdToEquivalentULocale = new HashMap<String, ULocale>();

                for (int i = 0; i < localeb.getSize(); ++i) {
                    UResourceBundle b = localeb.get(i);
                    String id = b.getKey();
                    String value = b.getString().intern();
                    tempLocaleIdToCardinalRulesId.put(id, value);

                    if (!tempRulesIdToEquivalentULocale.containsKey(value)) {
                        tempRulesIdToEquivalentULocale.put(value, new ULocale(id));
                    }
                }

                // Read ordinal-number rules.
                localeb = pluralb.get("locales_ordinals");
                tempLocaleIdToOrdinalRulesId = new TreeMap<String, String>();
                for (int i = 0; i < localeb.getSize(); ++i) {
                    UResourceBundle b = localeb.get(i);
                    String id = b.getKey();
                    String value = b.getString().intern();
                    tempLocaleIdToOrdinalRulesId.put(id, value);
                }
            } catch (MissingResourceException e) {
                // dummy so we don't try again
                tempLocaleIdToCardinalRulesId = Collections.emptyMap();
                tempLocaleIdToOrdinalRulesId = Collections.emptyMap();
                tempRulesIdToEquivalentULocale = Collections.emptyMap();
            }

            synchronized(this) {
                if (localeIdToCardinalRulesId == null) {
                    localeIdToCardinalRulesId = tempLocaleIdToCardinalRulesId;
                    localeIdToOrdinalRulesId = tempLocaleIdToOrdinalRulesId;
                    rulesIdToEquivalentULocale = tempRulesIdToEquivalentULocale;
                }
            }
        }
    }

    /**
     * Gets the rulesId from the locale,with locale fallback. If there is no
     * rulesId, return null. The rulesId might be the empty string if the rule
     * is the default rule.
     */
    public String getRulesIdForLocale(ULocale locale, PluralType type) {
        Map<String, String> idMap = getLocaleIdToRulesIdMap(type);
        String localeId = ULocale.canonicalize(locale.getBaseName());
        String rulesId = null;
        while (null == (rulesId = idMap.get(localeId))) {
            int ix = localeId.lastIndexOf("_");
            if (ix == -1) {
                break;
            }
            localeId = localeId.substring(0, ix);
        }
        return rulesId;
    }

    /**
     * Gets the rule from the rulesId. If there is no rule for this rulesId,
     * return null.
     */
    public PluralRules getRulesForRulesId(String rulesId) {
        // synchronize on the map.  release the lock temporarily while we build the rules.
        PluralRules rules = null;
        boolean hasRules;  // Separate boolean because stored rules can be null.
        synchronized (rulesIdToRules) {
            hasRules = rulesIdToRules.containsKey(rulesId);
            if (hasRules) {
                rules = rulesIdToRules.get(rulesId);  // can be null
            }
        }
        if (!hasRules) {
            try {
                UResourceBundle pluralb = getPluralBundle();
                UResourceBundle rulesb = pluralb.get("rules");
                UResourceBundle setb = rulesb.get(rulesId);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < setb.getSize(); ++i) {
                    UResourceBundle b = setb.get(i);
                    if (i > 0) {
                        sb.append("; ");
                    }
                    sb.append(b.getKey());
                    sb.append(": ");
                    sb.append(b.getString());
                }
                rules = PluralRules.parseDescription(sb.toString());
            } catch (ParseException e) {
            } catch (MissingResourceException e) {
            }
            synchronized (rulesIdToRules) {
                if (rulesIdToRules.containsKey(rulesId)) {
                    rules = rulesIdToRules.get(rulesId);
                } else {
                    rulesIdToRules.put(rulesId, rules);  // can be null
                }
            }
        }
        return rules;
    }

    /**
     * Return the plurals resource. Note MissingResourceException is unchecked,
     * listed here for clarity. Callers should handle this exception.
     */
    public UResourceBundle getPluralBundle() throws MissingResourceException {
        return ICUResourceBundle.getBundleInstance(
                ICUData.ICU_BASE_NAME, "plurals",
                ICUResourceBundle.ICU_DATA_CLASS_LOADER, true);
    }

    /**
     * Returns the plural rules for the the locale. If we don't have data,
     * android.icu.text.PluralRules.DEFAULT is returned.
     */
    public PluralRules forLocale(ULocale locale, PluralRules.PluralType type) {
        String rulesId = getRulesIdForLocale(locale, type);
        if (rulesId == null || rulesId.trim().length() == 0) {
            return PluralRules.DEFAULT;
        }
        PluralRules rules = getRulesForRulesId(rulesId);
        if (rules == null) {
            rules = PluralRules.DEFAULT;
        }
        return rules;
    }

    /**
     * The only instance of the loader.
     */
    public static final PluralRulesLoader loader = new PluralRulesLoader();

    /* (non-Javadoc)
     * @see android.icu.text.PluralRules.Factory#hasOverride(android.icu.util.ULocale)
     */
    @Override
    public boolean hasOverride(ULocale locale) {
        return false;
    }
    
    private static final PluralRanges UNKNOWN_RANGE = new PluralRanges().freeze();

    public PluralRanges getPluralRanges(ULocale locale) {
        // TODO markdavis Fix the bad fallback, here and elsewhere in this file.
        String localeId = ULocale.canonicalize(locale.getBaseName());
        PluralRanges result;
        while (null == (result = localeIdToPluralRanges.get(localeId))) {
            int ix = localeId.lastIndexOf("_");
            if (ix == -1) {
                result = UNKNOWN_RANGE;
                break;
            }
            localeId = localeId.substring(0, ix);
        }
        return result;
    }

    public boolean isPluralRangesAvailable(ULocale locale) {
        return getPluralRanges(locale) == UNKNOWN_RANGE;
    }

    // TODO markdavis FIX HARD-CODED HACK once we have data from CLDR in the bundles
    static {
        String[][] pluralRangeData = {
                {"locales", "id ja km ko lo ms my th vi zh"},
                {"other", "other", "other"},

                {"locales", "am bn fr gu hi hy kn mr pa zu"},
                {"one", "one", "one"},
                {"one", "other", "other"},
                {"other", "other", "other"},

                {"locales", "fa"},
                {"one", "one", "other"},
                {"one", "other", "other"},
                {"other", "other", "other"},

                {"locales", "ka"},
                {"one", "other", "one"},
                {"other", "one", "other"},
                {"other", "other", "other"},

                {"locales", "az de el gl hu it kk ky ml mn ne nl pt sq sw ta te tr ug uz"},
                {"one", "other", "other"},
                {"other", "one", "one"},
                {"other", "other", "other"},

                {"locales", "af bg ca en es et eu fi nb sv ur"},
                {"one", "other", "other"},
                {"other", "one", "other"},
                {"other", "other", "other"},

                {"locales", "da fil is"},
                {"one", "one", "one"},
                {"one", "other", "other"},
                {"other", "one", "one"},
                {"other", "other", "other"},

                {"locales", "si"},
                {"one", "one", "one"},
                {"one", "other", "other"},
                {"other", "one", "other"},
                {"other", "other", "other"},

                {"locales", "mk"},
                {"one", "one", "other"},
                {"one", "other", "other"},
                {"other", "one", "other"},
                {"other", "other", "other"},

                {"locales", "lv"},
                {"zero", "zero", "other"},
                {"zero", "one", "one"},
                {"zero", "other", "other"},
                {"one", "zero", "other"},
                {"one", "one", "one"},
                {"one", "other", "other"},
                {"other", "zero", "other"},
                {"other", "one", "one"},
                {"other", "other", "other"},

                {"locales", "ro"},
                {"one", "few", "few"},
                {"one", "other", "other"},
                {"few", "one", "few"},
                {"few", "few", "few"},
                {"few", "other", "other"},
                {"other", "few", "few"},
                {"other", "other", "other"},

                {"locales", "hr sr bs"},
                {"one", "one", "one"},
                {"one", "few", "few"},
                {"one", "other", "other"},
                {"few", "one", "one"},
                {"few", "few", "few"},
                {"few", "other", "other"},
                {"other", "one", "one"},
                {"other", "few", "few"},
                {"other", "other", "other"},

                {"locales", "sl"},
                {"one", "one", "few"},
                {"one", "two", "two"},
                {"one", "few", "few"},
                {"one", "other", "other"},
                {"two", "one", "few"},
                {"two", "two", "two"},
                {"two", "few", "few"},
                {"two", "other", "other"},
                {"few", "one", "few"},
                {"few", "two", "two"},
                {"few", "few", "few"},
                {"few", "other", "other"},
                {"other", "one", "few"},
                {"other", "two", "two"},
                {"other", "few", "few"},
                {"other", "other", "other"},

                {"locales", "he"},
                {"one", "two", "other"},
                {"one", "many", "many"},
                {"one", "other", "other"},
                {"two", "many", "other"},
                {"two", "other", "other"},
                {"many", "many", "many"},
                {"many", "other", "many"},
                {"other", "one", "other"},
                {"other", "two", "other"},
                {"other", "many", "many"},
                {"other", "other", "other"},

                {"locales", "cs pl sk"},
                {"one", "few", "few"},
                {"one", "many", "many"},
                {"one", "other", "other"},
                {"few", "few", "few"},
                {"few", "many", "many"},
                {"few", "other", "other"},
                {"many", "one", "one"},
                {"many", "few", "few"},
                {"many", "many", "many"},
                {"many", "other", "other"},
                {"other", "one", "one"},
                {"other", "few", "few"},
                {"other", "many", "many"},
                {"other", "other", "other"},

                {"locales", "lt ru uk"},
                {"one", "one", "one"},
                {"one", "few", "few"},
                {"one", "many", "many"},
                {"one", "other", "other"},
                {"few", "one", "one"},
                {"few", "few", "few"},
                {"few", "many", "many"},
                {"few", "other", "other"},
                {"many", "one", "one"},
                {"many", "few", "few"},
                {"many", "many", "many"},
                {"many", "other", "other"},
                {"other", "one", "one"},
                {"other", "few", "few"},
                {"other", "many", "many"},
                {"other", "other", "other"},

                {"locales", "cy"},
                {"zero", "one", "one"},
                {"zero", "two", "two"},
                {"zero", "few", "few"},
                {"zero", "many", "many"},
                {"zero", "other", "other"},
                {"one", "two", "two"},
                {"one", "few", "few"},
                {"one", "many", "many"},
                {"one", "other", "other"},
                {"two", "few", "few"},
                {"two", "many", "many"},
                {"two", "other", "other"},
                {"few", "many", "many"},
                {"few", "other", "other"},
                {"many", "other", "other"},
                {"other", "one", "one"},
                {"other", "two", "two"},
                {"other", "few", "few"},
                {"other", "many", "many"},
                {"other", "other", "other"},

                {"locales", "ar"},
                {"zero", "one", "zero"},
                {"zero", "two", "zero"},
                {"zero", "few", "few"},
                {"zero", "many", "many"},
                {"zero", "other", "other"},
                {"one", "two", "other"},
                {"one", "few", "few"},
                {"one", "many", "many"},
                {"one", "other", "other"},
                {"two", "few", "few"},
                {"two", "many", "many"},
                {"two", "other", "other"},
                {"few", "few", "few"},
                {"few", "many", "many"},
                {"few", "other", "other"},
                {"many", "few", "few"},
                {"many", "many", "many"},
                {"many", "other", "other"},
                {"other", "one", "other"},
                {"other", "two", "other"},
                {"other", "few", "few"},
                {"other", "many", "many"},
                {"other", "other", "other"},     
        };
        PluralRanges pr = null;
        String[] locales = null;
        HashMap<String, PluralRanges> tempLocaleIdToPluralRanges = new HashMap<String, PluralRanges>();
        for (String[] row : pluralRangeData) {
            if (row[0].equals("locales")) {
                if (pr != null) {
                    pr.freeze();
                    for (String locale : locales) {
                        tempLocaleIdToPluralRanges.put(locale, pr);
                    }
                }
                locales = row[1].split(" ");
                pr = new PluralRanges();
            } else {
                pr.add(
                        StandardPlural.fromString(row[0]),
                        StandardPlural.fromString(row[1]),
                        StandardPlural.fromString(row[2]));
            }
        }
        // do last one
        for (String locale : locales) {
            tempLocaleIdToPluralRanges.put(locale, pr);
        }
        // now make whole thing immutable
        localeIdToPluralRanges = Collections.unmodifiableMap(tempLocaleIdToPluralRanges);
    }
}