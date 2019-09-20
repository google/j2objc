/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

import android.icu.impl.CurrencyData.CurrencyDisplayInfo;
import android.icu.impl.CurrencyData.CurrencyDisplayInfoProvider;
import android.icu.impl.CurrencyData.CurrencyFormatInfo;
import android.icu.impl.CurrencyData.CurrencySpacingInfo;
import android.icu.impl.ICUResourceBundle.OpenType;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class ICUCurrencyDisplayInfoProvider implements CurrencyDisplayInfoProvider {
    public ICUCurrencyDisplayInfoProvider() {
    }

    @Override
    public CurrencyDisplayInfo getInstance(ULocale locale, boolean withFallback) {
        ICUResourceBundle rb;
        if (withFallback) {
            rb = ICUResourceBundle.getBundleInstance(
                    ICUData.ICU_CURR_BASE_NAME, locale, OpenType.LOCALE_DEFAULT_ROOT);
        } else {
            try {
                rb = ICUResourceBundle.getBundleInstance(
                        ICUData.ICU_CURR_BASE_NAME, locale, OpenType.LOCALE_ONLY);
            } catch (MissingResourceException e) {
                return null;
            }
        }
        return new ICUCurrencyDisplayInfo(rb, withFallback);
    }

    @Override
    public boolean hasData() {
        return true;
    }

    static class ICUCurrencyDisplayInfo extends CurrencyDisplayInfo {
        private final boolean fallback;
        private final ICUResourceBundle rb;
        private final ICUResourceBundle currencies;
        private final ICUResourceBundle plurals;
        private SoftReference<Map<String, String>> _symbolMapRef;
        private SoftReference<Map<String, String>> _nameMapRef;

        public ICUCurrencyDisplayInfo(ICUResourceBundle rb, boolean fallback) {
            this.fallback = fallback;
            this.rb = rb;
            this.currencies = rb.findTopLevel("Currencies");
            this.plurals = rb.findTopLevel("CurrencyPlurals");
       }

        @Override
        public ULocale getULocale() {
            return rb.getULocale();
        }

        @Override
        public String getName(String isoCode) {
            return getName(isoCode, false);
        }

        @Override
        public String getSymbol(String isoCode) {
            return getName(isoCode, true);
        }

        private String getName(String isoCode, boolean symbolName) {
            if (currencies != null) {
                ICUResourceBundle result = currencies.findWithFallback(isoCode);
                if (result != null) {
                    if (!fallback && !rb.isRoot() && result.isRoot()) {
                        return null;
                    }
                    return result.getString(symbolName ? 0 : 1);
                }
            }

            return fallback ? isoCode : null;
        }

        @Override
        public String getPluralName(String isoCode, String pluralKey ) {
            // See http://unicode.org/reports/tr35/#Currencies, especially the fallback rule.
            if (plurals != null) {
                ICUResourceBundle pluralsBundle = plurals.findWithFallback(isoCode);
                if (pluralsBundle != null) {
                    String pluralName = pluralsBundle.findStringWithFallback(pluralKey);
                    if (pluralName == null) {
                        if (!fallback) {
                            return null;
                        }
                        pluralName = pluralsBundle.findStringWithFallback("other");
                        if (pluralName == null) {
                            return getName(isoCode);
                        }
                    }
                    return pluralName;
                }
            }

            return fallback ? getName(isoCode) : null;
        }

        @Override
        public Map<String, String> symbolMap() {
            Map<String, String> map = _symbolMapRef == null ? null : _symbolMapRef.get();
            if (map == null) {
                map = _createSymbolMap();
                // atomic and idempotent
                _symbolMapRef = new SoftReference<Map<String, String>>(map);
            }
            return map;
        }

        @Override
        public Map<String, String> nameMap() {
            Map<String, String> map = _nameMapRef == null ? null : _nameMapRef.get();
            if (map == null) {
                map = _createNameMap();
                // atomic and idempotent
                _nameMapRef = new SoftReference<Map<String, String>>(map);
            }
            return map;
        }

        @Override
        public Map<String, String> getUnitPatterns() {
            Map<String, String> result = new HashMap<String, String>();

            ULocale locale = rb.getULocale();
            for (;locale != null; locale = locale.getFallback()) {
                ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance(
                        ICUData.ICU_CURR_BASE_NAME, locale);
                if (r == null) {
                    continue;
                }
                ICUResourceBundle cr = r.findWithFallback("CurrencyUnitPatterns");
                if (cr == null) {
                    continue;
                }
                for (int index = 0, size = cr.getSize(); index < size; ++index) {
                    ICUResourceBundle b = (ICUResourceBundle) cr.get(index);
                    String key = b.getKey();
                    if (result.containsKey(key)) {
                        continue;
                    }
                    result.put(key, b.getString());
                }
            }

            // Default result is the empty map. Callers who require a pattern will have to
            // supply a default.
            return Collections.unmodifiableMap(result);
        }

        @Override
        public CurrencyFormatInfo getFormatInfo(String isoCode) {
            ICUResourceBundle crb = currencies.findWithFallback(isoCode);
            if (crb != null && crb.getSize() > 2) {
                crb = crb.at(2);
                if (crb != null) {
                  String pattern = crb.getString(0);
                  String separator = crb.getString(1);
                  String groupingSeparator = crb.getString(2);
                  return new CurrencyFormatInfo(pattern, separator, groupingSeparator);
                }
            }
            return null;
        }

        @Override
        public CurrencySpacingInfo getSpacingInfo() {
            SpacingInfoSink sink = new SpacingInfoSink();
            rb.getAllItemsWithFallback("currencySpacing", sink);
            return sink.getSpacingInfo(fallback);
        }

        private final class SpacingInfoSink extends UResource.Sink {
            CurrencySpacingInfo spacingInfo = new CurrencySpacingInfo();
            boolean hasBeforeCurrency = false;
            boolean hasAfterCurrency = false;

            /*
             *  currencySpacing{
             *      afterCurrency{
             *          currencyMatch{"[:^S:]"}
             *          insertBetween{" "}
             *          surroundingMatch{"[:digit:]"}
             *      }
             *      beforeCurrency{
             *          currencyMatch{"[:^S:]"}
             *          insertBetween{" "}
             *          surroundingMatch{"[:digit:]"}
             *      }
             *  }
             */
            @Override
            public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
                UResource.Table spacingTypesTable = value.getTable();
                for (int i = 0; spacingTypesTable.getKeyAndValue(i, key, value); ++i) {
                    CurrencySpacingInfo.SpacingType type;
                    if (key.contentEquals("beforeCurrency")) {
                        type = CurrencySpacingInfo.SpacingType.BEFORE;
                        hasBeforeCurrency = true;
                    } else if (key.contentEquals("afterCurrency")) {
                        type = CurrencySpacingInfo.SpacingType.AFTER;
                        hasAfterCurrency = true;
                    } else {
                        continue;
                    }

                    UResource.Table patternsTable = value.getTable();
                    for (int j = 0; patternsTable.getKeyAndValue(j, key, value); ++j) {
                        CurrencySpacingInfo.SpacingPattern pattern;
                        if (key.contentEquals("currencyMatch")) {
                            pattern = CurrencySpacingInfo.SpacingPattern.CURRENCY_MATCH;
                        } else if (key.contentEquals("surroundingMatch")) {
                            pattern = CurrencySpacingInfo.SpacingPattern.SURROUNDING_MATCH;
                        } else if (key.contentEquals("insertBetween")) {
                            pattern = CurrencySpacingInfo.SpacingPattern.INSERT_BETWEEN;
                        } else {
                            continue;
                        }

                        spacingInfo.setSymbolIfNull(type, pattern, value.getString());
                    }
                }
            }

            CurrencySpacingInfo getSpacingInfo(boolean fallback) {
                if (hasBeforeCurrency && hasAfterCurrency) {
                    return spacingInfo;
                } else if (fallback) {
                    return CurrencySpacingInfo.DEFAULT;
                } else {
                    return null;
                }
            }
        }

        private Map<String, String> _createSymbolMap() {
            Map<String, String> result = new HashMap<String, String>();

            for (ULocale locale = rb.getULocale(); locale != null; locale = locale.getFallback()) {
                ICUResourceBundle bundle = (ICUResourceBundle)
                    UResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, locale);
                ICUResourceBundle curr = bundle.findTopLevel("Currencies");
                if (curr == null) {
                    continue;
                }
                for (int i = 0; i < curr.getSize(); ++i) {
                    ICUResourceBundle item = curr.at(i);
                    String isoCode = item.getKey();
                    if (!result.containsKey(isoCode)) {
                        // put the code itself
                        result.put(isoCode, isoCode);
                        // 0 == symbol element
                        String symbol = item.getString(0);
                        result.put(symbol, isoCode);
                    }
                }
            }

            return Collections.unmodifiableMap(result);
        }

        private Map<String, String> _createNameMap() {
            // ignore case variants
            Map<String, String> result = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

            Set<String> visited = new HashSet<String>();
            Map<String, Set<String>> visitedPlurals = new HashMap<String, Set<String>>();
            for (ULocale locale = rb.getULocale(); locale != null; locale = locale.getFallback()) {
                ICUResourceBundle bundle = (ICUResourceBundle)
                    UResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, locale);
                ICUResourceBundle curr = bundle.findTopLevel("Currencies");
                if (curr != null) {
                    for (int i = 0; i < curr.getSize(); ++i) {
                        ICUResourceBundle item = curr.at(i);
                        String isoCode = item.getKey();
                        if (!visited.contains(isoCode)) {
                            visited.add(isoCode);
                            // 1 == name element
                            String name = item.getString(1);
                            result.put(name, isoCode);
                        }
                    }
                }

                ICUResourceBundle plurals = bundle.findTopLevel("CurrencyPlurals");
                if (plurals != null) {
                    for (int i = 0; i < plurals.getSize(); ++i) {
                        ICUResourceBundle item = plurals.at(i);
                        String isoCode = item.getKey();
                        Set<String> pluralSet = visitedPlurals.get(isoCode);
                        if (pluralSet == null) {
                            pluralSet = new HashSet<String>();
                            visitedPlurals.put(isoCode, pluralSet);
                        }
                        for (int j = 0; j < item.getSize(); ++j) {
                            ICUResourceBundle plural = item.at(j);
                            String pluralType = plural.getKey();
                            if (!pluralSet.contains(pluralType)) {
                                String pluralName = plural.getString();
                                result.put(pluralName, isoCode);
                                pluralSet.add(pluralType);
                            }
                        }
                    }
                }
            }

            return Collections.unmodifiableMap(result);
        }
    }
}
