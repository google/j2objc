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

import java.util.Collections;
import java.util.Map;

import android.icu.text.CurrencyDisplayNames;
import android.icu.text.DecimalFormatSymbols;
import android.icu.util.ULocale;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class CurrencyData {
    public static final CurrencyDisplayInfoProvider provider;

    private CurrencyData() {}

    public static interface CurrencyDisplayInfoProvider {
        CurrencyDisplayInfo getInstance(ULocale locale, boolean withFallback);
        boolean hasData();
    }

    public static abstract class CurrencyDisplayInfo extends CurrencyDisplayNames {
        public abstract Map<String, String> getUnitPatterns();
        public abstract CurrencyFormatInfo getFormatInfo(String isoCode);
        public abstract CurrencySpacingInfo getSpacingInfo();
    }

    public static final class CurrencyFormatInfo {
        public final String currencyPattern;
        public final String monetarySeparator;
        public final String monetaryGroupingSeparator;

        public CurrencyFormatInfo(String currencyPattern, String monetarySeparator,
                String monetaryGroupingSeparator) {
            this.currencyPattern = currencyPattern;
            this.monetarySeparator = monetarySeparator;
            this.monetaryGroupingSeparator = monetaryGroupingSeparator;
        }
    }

    public static final class CurrencySpacingInfo {
        private final String[][] symbols = new String[SpacingType.COUNT.ordinal()][SpacingPattern.COUNT.ordinal()];

        public static enum SpacingType { BEFORE, AFTER, COUNT };
        public static enum SpacingPattern {
            CURRENCY_MATCH(DecimalFormatSymbols.CURRENCY_SPC_CURRENCY_MATCH),
            SURROUNDING_MATCH(DecimalFormatSymbols.CURRENCY_SPC_SURROUNDING_MATCH),
            INSERT_BETWEEN(DecimalFormatSymbols.CURRENCY_SPC_INSERT),
            COUNT;

            SpacingPattern() {}
            SpacingPattern(int value) { assert value == ordinal(); }
        };

        public CurrencySpacingInfo() {}

        public CurrencySpacingInfo(String... strings) {
            assert strings.length == 6;

            int k = 0;
            for (int i=0; i<SpacingType.COUNT.ordinal(); i++) {
                for (int j=0; j<SpacingPattern.COUNT.ordinal(); j++) {
                    symbols[i][j] = strings[k];
                    k++;
                }
            }
        }

        public void setSymbolIfNull(SpacingType type, SpacingPattern pattern, String value) {
            int i = type.ordinal();
            int j = pattern.ordinal();
            if (symbols[i][j] == null) {
                symbols[i][j] = value;
            }
        }

        public String[] getBeforeSymbols() {
            return symbols[SpacingType.BEFORE.ordinal()];
        }

        public String[] getAfterSymbols() {
            return symbols[SpacingType.AFTER.ordinal()];
        }

        private static final String DEFAULT_CUR_MATCH = "[:letter:]";
        private static final String DEFAULT_CTX_MATCH = "[:digit:]";
        private static final String DEFAULT_INSERT = " ";

        public static final CurrencySpacingInfo DEFAULT = new CurrencySpacingInfo(
                DEFAULT_CUR_MATCH, DEFAULT_CTX_MATCH, DEFAULT_INSERT,
                DEFAULT_CUR_MATCH, DEFAULT_CTX_MATCH, DEFAULT_INSERT);
    }

    static {
        CurrencyDisplayInfoProvider temp = null;
        try {
            Class<?> clzz = Class.forName("android.icu.impl.ICUCurrencyDisplayInfoProvider");
            temp = (CurrencyDisplayInfoProvider) clzz.newInstance();
        } catch (Throwable t) {
            temp = new CurrencyDisplayInfoProvider() {
                @Override
                public CurrencyDisplayInfo getInstance(ULocale locale, boolean withFallback) {
                    return DefaultInfo.getWithFallback(withFallback);
                }

                @Override
                public boolean hasData() {
                    return false;
                }
            };
        }
        provider = temp;
    }

    public static class DefaultInfo extends CurrencyDisplayInfo {
        private final boolean fallback;

        private DefaultInfo(boolean fallback) {
            this.fallback = fallback;
        }

        public static final CurrencyDisplayInfo getWithFallback(boolean fallback) {
            return fallback ? FALLBACK_INSTANCE : NO_FALLBACK_INSTANCE;
        }

        @Override
        public String getName(String isoCode) {
            return fallback ? isoCode : null;
        }

        @Override
        public String getPluralName(String isoCode, String pluralType) {
            return fallback ? isoCode : null;
        }

        @Override
        public String getSymbol(String isoCode) {
            return fallback ? isoCode : null;
        }

        @Override
        public Map<String, String> symbolMap() {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, String> nameMap() {
            return Collections.emptyMap();
        }

        @Override
        public ULocale getULocale() {
            return ULocale.ROOT;
        }

        @Override
        public Map<String, String> getUnitPatterns() {
            if (fallback) {
                return Collections.emptyMap();
            }
            return null;
        }

        @Override
        public CurrencyFormatInfo getFormatInfo(String isoCode) {
            return null;
        }

        @Override
        public CurrencySpacingInfo getSpacingInfo() {
            return fallback ? CurrencySpacingInfo.DEFAULT : null;
        }

        private static final CurrencyDisplayInfo FALLBACK_INSTANCE = new DefaultInfo(true);
        private static final CurrencyDisplayInfo NO_FALLBACK_INSTANCE = new DefaultInfo(false);
    }
}
