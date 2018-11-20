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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;

import android.icu.impl.CurrencyData.CurrencyDisplayInfo;
import android.icu.impl.locale.AsciiUtil;
import android.icu.lang.UCharacter;
import android.icu.lang.UScript;
import android.icu.text.BreakIterator;
import android.icu.text.DisplayContext;
import android.icu.text.DisplayContext.Type;
import android.icu.text.LocaleDisplayNames;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class LocaleDisplayNamesImpl extends LocaleDisplayNames {
    private final ULocale locale;
    private final DialectHandling dialectHandling;
    private final DisplayContext capitalization;
    private final DisplayContext nameLength;
    private final DisplayContext substituteHandling;
    private final DataTable langData;
    private final DataTable regionData;
    // Compiled SimpleFormatter patterns.
    private final String separatorFormat;
    private final String format;
    private final String keyTypeFormat;
    private final char formatOpenParen;
    private final char formatReplaceOpenParen;
    private final char formatCloseParen;
    private final char formatReplaceCloseParen;
    private final CurrencyDisplayInfo currencyDisplayInfo;

    private static final Cache cache = new Cache();

    /**
     * Capitalization context usage types for locale display names
     */
    private enum CapitalizationContextUsage {
        LANGUAGE,
        SCRIPT,
        TERRITORY,
        VARIANT,
        KEY,
        KEYVALUE
    }
    /**
     * Capitalization transforms. For each usage type, indicates whether to titlecase for
     * the context specified in capitalization (which we know at construction time).
     */
    private boolean[] capitalizationUsage = null;
    /**
     * Map from resource key to CapitalizationContextUsage value
     */
    private static final Map<String, CapitalizationContextUsage> contextUsageTypeMap;
    static {
        contextUsageTypeMap=new HashMap<String, CapitalizationContextUsage>();
        contextUsageTypeMap.put("languages", CapitalizationContextUsage.LANGUAGE);
        contextUsageTypeMap.put("script",    CapitalizationContextUsage.SCRIPT);
        contextUsageTypeMap.put("territory", CapitalizationContextUsage.TERRITORY);
        contextUsageTypeMap.put("variant",   CapitalizationContextUsage.VARIANT);
        contextUsageTypeMap.put("key",       CapitalizationContextUsage.KEY);
        contextUsageTypeMap.put("keyValue",  CapitalizationContextUsage.KEYVALUE);
    }
    /**
     * BreakIterator to use for capitalization
     */
    private transient BreakIterator capitalizationBrkIter = null;


    public static LocaleDisplayNames getInstance(ULocale locale, DialectHandling dialectHandling) {
        synchronized (cache) {
            return cache.get(locale, dialectHandling);
        }
    }

    public static LocaleDisplayNames getInstance(ULocale locale, DisplayContext... contexts) {
        synchronized (cache) {
            return cache.get(locale, contexts);
        }
    }

    private final class CapitalizationContextSink extends UResource.Sink {
        boolean hasCapitalizationUsage = false;

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table contextsTable = value.getTable();
            for (int i = 0; contextsTable.getKeyAndValue(i, key, value); ++i) {

                CapitalizationContextUsage usage = contextUsageTypeMap.get(key.toString());
                if (usage == null) { continue; };

                int[] intVector = value.getIntVector();
                if (intVector.length < 2) { continue; }

                int titlecaseInt = (capitalization == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU)
                        ? intVector[0] : intVector[1];
                if (titlecaseInt == 0) { continue; }

                capitalizationUsage[usage.ordinal()] = true;
                hasCapitalizationUsage = true;
            }
        }
    }

    public LocaleDisplayNamesImpl(ULocale locale, DialectHandling dialectHandling) {
        this(locale, (dialectHandling==DialectHandling.STANDARD_NAMES)? DisplayContext.STANDARD_NAMES: DisplayContext.DIALECT_NAMES,
                DisplayContext.CAPITALIZATION_NONE);
    }

    public LocaleDisplayNamesImpl(ULocale locale, DisplayContext... contexts) {
        DialectHandling dialectHandling = DialectHandling.STANDARD_NAMES;
        DisplayContext capitalization = DisplayContext.CAPITALIZATION_NONE;
        DisplayContext nameLength = DisplayContext.LENGTH_FULL;
        DisplayContext substituteHandling = DisplayContext.SUBSTITUTE;
        for (DisplayContext contextItem : contexts) {
            switch (contextItem.type()) {
            case DIALECT_HANDLING:
                dialectHandling = (contextItem.value()==DisplayContext.STANDARD_NAMES.value())?
                        DialectHandling.STANDARD_NAMES: DialectHandling.DIALECT_NAMES;
                break;
            case CAPITALIZATION:
                capitalization = contextItem;
                break;
            case DISPLAY_LENGTH:
                nameLength = contextItem;
                break;
            case SUBSTITUTE_HANDLING:
                substituteHandling = contextItem;
                break;
            default:
                break;
            }
        }

        this.dialectHandling = dialectHandling;
        this.capitalization = capitalization;
        this.nameLength = nameLength;
        this.substituteHandling = substituteHandling;
        this.langData = LangDataTables.impl.get(locale, substituteHandling == DisplayContext.NO_SUBSTITUTE);
        this.regionData = RegionDataTables.impl.get(locale, substituteHandling == DisplayContext.NO_SUBSTITUTE);
        this.locale = ULocale.ROOT.equals(langData.getLocale()) ? regionData.getLocale() :
            langData.getLocale();

        // Note, by going through DataTable, this uses table lookup rather than straight lookup.
        // That should get us the same data, I think.  This way we don't have to explicitly
        // load the bundle again.  Using direct lookup didn't seem to make an appreciable
        // difference in performance.
        String sep = langData.get("localeDisplayPattern", "separator");
        if (sep == null || "separator".equals(sep)) {
            sep = "{0}, {1}";
        }
        StringBuilder sb = new StringBuilder();
        this.separatorFormat = SimpleFormatterImpl.compileToStringMinMaxArguments(sep, sb, 2, 2);

        String pattern = langData.get("localeDisplayPattern", "pattern");
        if (pattern == null || "pattern".equals(pattern)) {
            pattern = "{0} ({1})";
        }
        this.format = SimpleFormatterImpl.compileToStringMinMaxArguments(pattern, sb, 2, 2);
        if (pattern.contains("（")) {
            formatOpenParen = '（';
            formatCloseParen = '）';
            formatReplaceOpenParen = '［';
            formatReplaceCloseParen = '］';
        } else  {
            formatOpenParen = '(';
            formatCloseParen = ')';
            formatReplaceOpenParen = '[';
            formatReplaceCloseParen = ']';
        }

        String keyTypePattern = langData.get("localeDisplayPattern", "keyTypePattern");
        if (keyTypePattern == null || "keyTypePattern".equals(keyTypePattern)) {
            keyTypePattern = "{0}={1}";
        }
        this.keyTypeFormat = SimpleFormatterImpl.compileToStringMinMaxArguments(
                keyTypePattern, sb, 2, 2);

        // Get values from the contextTransforms data if we need them
        // Also check whether we will need a break iterator (depends on the data)
        boolean needBrkIter = false;
        if (capitalization == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU ||
                capitalization == DisplayContext.CAPITALIZATION_FOR_STANDALONE) {
            capitalizationUsage = new boolean[CapitalizationContextUsage.values().length]; // initialized to all false
            ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, locale);
            CapitalizationContextSink sink = new CapitalizationContextSink();
            try {
                rb.getAllItemsWithFallback("contextTransforms", sink);
            }
            catch (MissingResourceException e) {
                // Silently ignore.  Not every locale has contextTransforms.
            }
            needBrkIter = sink.hasCapitalizationUsage;
        }
        // Get a sentence break iterator if we will need it
        if (needBrkIter || capitalization == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE) {
            capitalizationBrkIter = BreakIterator.getSentenceInstance(locale);
        }

        this.currencyDisplayInfo = CurrencyData.provider.getInstance(locale, false);
    }

    @Override
    public ULocale getLocale() {
        return locale;
    }

    @Override
    public DialectHandling getDialectHandling() {
        return dialectHandling;
    }

    @Override
    public DisplayContext getContext(DisplayContext.Type type) {
        DisplayContext result;
        switch (type) {
        case DIALECT_HANDLING:
            result = (dialectHandling==DialectHandling.STANDARD_NAMES)? DisplayContext.STANDARD_NAMES: DisplayContext.DIALECT_NAMES;
            break;
        case CAPITALIZATION:
            result = capitalization;
            break;
        case DISPLAY_LENGTH:
            result = nameLength;
            break;
        case SUBSTITUTE_HANDLING:
            result = substituteHandling;
            break;
        default:
            result = DisplayContext.STANDARD_NAMES; // hmm, we should do something else here
            break;
        }
        return result;
    }

    private String adjustForUsageAndContext(CapitalizationContextUsage usage, String name) {
        if (name != null && name.length() > 0 && UCharacter.isLowerCase(name.codePointAt(0)) &&
                (capitalization==DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE ||
                (capitalizationUsage != null && capitalizationUsage[usage.ordinal()]) )) {
            // Note, won't have capitalizationUsage != null && capitalizationUsage[usage.ordinal()]
            // unless capitalization is CAPITALIZATION_FOR_UI_LIST_OR_MENU or CAPITALIZATION_FOR_STANDALONE
            synchronized (this) {
                if (capitalizationBrkIter == null) {
                    // should only happen when deserializing, etc.
                    capitalizationBrkIter = BreakIterator.getSentenceInstance(locale);
                }
                return UCharacter.toTitleCase(locale, name, capitalizationBrkIter,
                        UCharacter.TITLECASE_NO_LOWERCASE | UCharacter.TITLECASE_NO_BREAK_ADJUSTMENT);
            }
        }
        return name;
    }

    @Override
    public String localeDisplayName(ULocale locale) {
        return localeDisplayNameInternal(locale);
    }

    @Override
    public String localeDisplayName(Locale locale) {
        return localeDisplayNameInternal(ULocale.forLocale(locale));
    }

    @Override
    public String localeDisplayName(String localeId) {
        return localeDisplayNameInternal(new ULocale(localeId));
    }

    // TODO: implement use of capitalization
    private String localeDisplayNameInternal(ULocale locale) {
        // lang
        // lang (script, country, variant, keyword=value, ...)
        // script, country, variant, keyword=value, ...

        String resultName = null;

        String lang = locale.getLanguage();

        // Empty basename indicates root locale (keywords are ignored for this).
        // Our data uses 'root' to access display names for the root locale in the
        // "Languages" table.
        if (locale.getBaseName().length() == 0) {
            lang = "root";
        }
        String script = locale.getScript();
        String country = locale.getCountry();
        String variant = locale.getVariant();

        boolean hasScript = script.length() > 0;
        boolean hasCountry = country.length() > 0;
        boolean hasVariant = variant.length() > 0;

        // always have a value for lang
        if (dialectHandling == DialectHandling.DIALECT_NAMES) {
            do { // loop construct is so we can break early out of search
                if (hasScript && hasCountry) {
                    String langScriptCountry = lang + '_' + script + '_' + country;
                    String result = localeIdName(langScriptCountry);
                    if (result != null && !result.equals(langScriptCountry)) {
                        resultName = result;
                        hasScript = false;
                        hasCountry = false;
                        break;
                    }
                }
                if (hasScript) {
                    String langScript = lang + '_' + script;
                    String result = localeIdName(langScript);
                    if (result != null && !result.equals(langScript)) {
                        resultName = result;
                        hasScript = false;
                        break;
                    }
                }
                if (hasCountry) {
                    String langCountry = lang + '_' + country;
                    String result = localeIdName(langCountry);
                    if (result != null && !result.equals(langCountry)) {
                        resultName = result;
                        hasCountry = false;
                        break;
                    }
                }
            } while (false);
        }

        if (resultName == null) {
            String result = localeIdName(lang);
            if (result == null) { return null; }
            resultName = result
                    .replace(formatOpenParen, formatReplaceOpenParen)
                    .replace(formatCloseParen, formatReplaceCloseParen);
        }

        StringBuilder buf = new StringBuilder();
        if (hasScript) {
            // first element, don't need appendWithSep
            String result = scriptDisplayNameInContext(script, true);
            if (result == null) { return null; }
            buf.append(result
                    .replace(formatOpenParen, formatReplaceOpenParen)
                    .replace(formatCloseParen, formatReplaceCloseParen));
        }
        if (hasCountry) {
            String result = regionDisplayName(country, true);
            if (result == null) { return null; }
            appendWithSep(result
                    .replace(formatOpenParen, formatReplaceOpenParen)
                    .replace(formatCloseParen, formatReplaceCloseParen), buf);
        }
        if (hasVariant) {
            String result = variantDisplayName(variant, true);
            if (result == null) { return null; }
            appendWithSep(result
                    .replace(formatOpenParen, formatReplaceOpenParen)
                    .replace(formatCloseParen, formatReplaceCloseParen), buf);
        }

        Iterator<String> keys = locale.getKeywords();
        if (keys != null) {
            while (keys.hasNext()) {
                String key = keys.next();
                String value = locale.getKeywordValue(key);
                String keyDisplayName = keyDisplayName(key, true);
                if (keyDisplayName == null) { return null; }
                keyDisplayName = keyDisplayName
                        .replace(formatOpenParen, formatReplaceOpenParen)
                        .replace(formatCloseParen, formatReplaceCloseParen);
                String valueDisplayName = keyValueDisplayName(key, value, true);
                if (valueDisplayName == null) { return null; }
                valueDisplayName = valueDisplayName
                        .replace(formatOpenParen, formatReplaceOpenParen)
                        .replace(formatCloseParen, formatReplaceCloseParen);
                if (!valueDisplayName.equals(value)) {
                    appendWithSep(valueDisplayName, buf);
                } else if (!key.equals(keyDisplayName)) {
                    String keyValue = SimpleFormatterImpl.formatCompiledPattern(
                            keyTypeFormat, keyDisplayName, valueDisplayName);
                    appendWithSep(keyValue, buf);
                } else {
                    appendWithSep(keyDisplayName, buf)
                    .append("=")
                    .append(valueDisplayName);
                }
            }
        }

        String resultRemainder = null;
        if (buf.length() > 0) {
            resultRemainder = buf.toString();
        }

        if (resultRemainder != null) {
            resultName = SimpleFormatterImpl.formatCompiledPattern(
                    format, resultName, resultRemainder);
        }

        return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, resultName);
    }

    private String localeIdName(String localeId) {
        if (nameLength == DisplayContext.LENGTH_SHORT) {
            String locIdName = langData.get("Languages%short", localeId);
            if (locIdName != null && !locIdName.equals(localeId)) {
                return locIdName;
            }
        }
        return langData.get("Languages", localeId);
    }

    @Override
    public String languageDisplayName(String lang) {
        // Special case to eliminate non-languages, which pollute our data.
        if (lang.equals("root") || lang.indexOf('_') != -1) {
            return substituteHandling == DisplayContext.SUBSTITUTE ? lang : null;
        }
        if (nameLength == DisplayContext.LENGTH_SHORT) {
            String langName = langData.get("Languages%short", lang);
            if (langName != null && !langName.equals(lang)) {
                return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, langName);
            }
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, langData.get("Languages", lang));
    }

    @Override
    public String scriptDisplayName(String script) {
        String str = langData.get("Scripts%stand-alone", script);
        if (str == null || str.equals(script)) {
            if (nameLength == DisplayContext.LENGTH_SHORT) {
                str = langData.get("Scripts%short", script);
                if (str != null && !str.equals(script)) {
                    return adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, str);
                }
            }
            str = langData.get("Scripts", script);
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, str);
    }

    private String scriptDisplayNameInContext(String script, boolean skipAdjust) {
        if (nameLength == DisplayContext.LENGTH_SHORT) {
            String scriptName = langData.get("Scripts%short", script);
            if (scriptName != null && !scriptName.equals(script)) {
                return skipAdjust? scriptName: adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, scriptName);
            }
        }
        String scriptName = langData.get("Scripts", script);
        return skipAdjust? scriptName: adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, scriptName);
    }

    @Override
    public String scriptDisplayNameInContext(String script) {
        return scriptDisplayNameInContext(script, false);
    }

    @Override
    public String scriptDisplayName(int scriptCode) {
        return scriptDisplayName(UScript.getShortName(scriptCode));
    }

    private String regionDisplayName(String region, boolean skipAdjust) {
        if (nameLength == DisplayContext.LENGTH_SHORT) {
            String regionName = regionData.get("Countries%short", region);
            if (regionName != null && !regionName.equals(region)) {
                return skipAdjust? regionName: adjustForUsageAndContext(CapitalizationContextUsage.TERRITORY, regionName);
            }
        }
        String regionName = regionData.get("Countries", region);
        return skipAdjust? regionName: adjustForUsageAndContext(CapitalizationContextUsage.TERRITORY, regionName);
    }

    @Override
    public String regionDisplayName(String region) {
        return regionDisplayName(region, false);
    }

    private String variantDisplayName(String variant, boolean skipAdjust) {
        // don't have a resource for short variant names
        String variantName = langData.get("Variants", variant);
        return skipAdjust? variantName: adjustForUsageAndContext(CapitalizationContextUsage.VARIANT, variantName);
    }

    @Override
    public String variantDisplayName(String variant) {
        return variantDisplayName(variant, false);
    }

    private String keyDisplayName(String key, boolean skipAdjust) {
        // don't have a resource for short key names
        String keyName = langData.get("Keys", key);
        return skipAdjust? keyName: adjustForUsageAndContext(CapitalizationContextUsage.KEY, keyName);
    }

    @Override
    public String keyDisplayName(String key) {
        return keyDisplayName(key, false);
    }

    private String keyValueDisplayName(String key, String value, boolean skipAdjust) {
        String keyValueName = null;

        if (key.equals("currency")) {
            keyValueName = currencyDisplayInfo.getName(AsciiUtil.toUpperString(value));
            if (keyValueName == null) {
                keyValueName = value;
            }
        } else {
            if (nameLength == DisplayContext.LENGTH_SHORT) {
                String tmp = langData.get("Types%short", key, value);
                if (tmp != null && !tmp.equals(value)) {
                    keyValueName = tmp;
                }
            }
            if (keyValueName == null) {
                keyValueName = langData.get("Types", key, value);
            }
        }

        return skipAdjust? keyValueName: adjustForUsageAndContext(CapitalizationContextUsage.KEYVALUE, keyValueName);
    }

    @Override
    public String keyValueDisplayName(String key, String value) {
        return keyValueDisplayName(key, value, false);
    }

    @Override
    public List<UiListItem> getUiListCompareWholeItems(Set<ULocale> localeSet, Comparator<UiListItem> comparator) {
        DisplayContext capContext = getContext(Type.CAPITALIZATION);

        List<UiListItem> result = new ArrayList<UiListItem>();
        Map<ULocale,Set<ULocale>> baseToLocales = new HashMap<ULocale,Set<ULocale>>();
        ULocale.Builder builder = new ULocale.Builder();
        for (ULocale locOriginal : localeSet) {
            builder.setLocale(locOriginal); // verify well-formed. We do this here so that we consistently throw exception
            ULocale loc = ULocale.addLikelySubtags(locOriginal);
            ULocale base = new ULocale(loc.getLanguage());
            Set<ULocale> locales = baseToLocales.get(base);
            if (locales == null) {
                baseToLocales.put(base, locales = new HashSet<ULocale>());
            }
            locales.add(loc);
        }
        for (Entry<ULocale, Set<ULocale>> entry : baseToLocales.entrySet()) {
            ULocale base = entry.getKey();
            Set<ULocale> values = entry.getValue();
            if (values.size() == 1) {
                ULocale locale = values.iterator().next();
                result.add(newRow(ULocale.minimizeSubtags(locale, ULocale.Minimize.FAVOR_SCRIPT), capContext));
            } else {
                Set<String> scripts = new HashSet<String>();
                Set<String> regions = new HashSet<String>();
                // need the follow two steps to make sure that unusual scripts or regions are displayed
                ULocale maxBase = ULocale.addLikelySubtags(base);
                scripts.add(maxBase.getScript());
                regions.add(maxBase.getCountry());
                for (ULocale locale : values) {
                    scripts.add(locale.getScript());
                    regions.add(locale.getCountry());
                }
                boolean hasScripts = scripts.size() > 1;
                boolean hasRegions = regions.size() > 1;
                for (ULocale locale : values) {
                    ULocale.Builder modified = builder.setLocale(locale);
                    if (!hasScripts) {
                        modified.setScript("");
                    }
                    if (!hasRegions) {
                        modified.setRegion("");
                    }
                    result.add(newRow(modified.build(), capContext));
                }
            }
        }
        Collections.sort(result, comparator);
        return result;
    }

    private UiListItem newRow(ULocale modified, DisplayContext capContext) {
        ULocale minimized = ULocale.minimizeSubtags(modified, ULocale.Minimize.FAVOR_SCRIPT);
        String tempName = modified.getDisplayName(locale);
        boolean titlecase = capContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU;
        String nameInDisplayLocale =  titlecase ? UCharacter.toTitleFirst(locale, tempName) : tempName;
        tempName = modified.getDisplayName(modified);
        String nameInSelf = capContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU ? UCharacter.toTitleFirst(modified, tempName) : tempName;
        return new UiListItem(minimized, modified, nameInDisplayLocale, nameInSelf);
    }

    public static class DataTable {
        final boolean nullIfNotFound;

        DataTable(boolean nullIfNotFound) {
            this.nullIfNotFound = nullIfNotFound;
        }

        ULocale getLocale() {
            return ULocale.ROOT;
        }

        String get(String tableName, String code) {
            return get(tableName, null, code);
        }

        String get(String tableName, String subTableName, String code) {
            return nullIfNotFound ? null : code;
        }
    }

    static class ICUDataTable extends DataTable {
        private final ICUResourceBundle bundle;

        public ICUDataTable(String path, ULocale locale, boolean nullIfNotFound) {
            super(nullIfNotFound);
            this.bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(
                    path, locale.getBaseName());
        }

        @Override
        public ULocale getLocale() {
            return bundle.getULocale();
        }

        @Override
        public String get(String tableName, String subTableName, String code) {
            return ICUResourceTableAccess.getTableString(bundle, tableName, subTableName,
                    code, nullIfNotFound ? null : code);
        }
    }

    static abstract class DataTables {
        public abstract DataTable get(ULocale locale, boolean nullIfNotFound);
        public static DataTables load(String className) {
            try {
                return (DataTables) Class.forName(className).newInstance();
            } catch (Throwable t) {
                return new DataTables() {
                    @Override
                    public DataTable get(ULocale locale, boolean nullIfNotFound) {
                        return new DataTable(nullIfNotFound);
                    }
                };
            }
        }
    }

    static abstract class ICUDataTables extends DataTables {
        private final String path;

        protected ICUDataTables(String path) {
            this.path = path;
        }

        @Override
        public DataTable get(ULocale locale, boolean nullIfNotFound) {
            return new ICUDataTable(path, locale, nullIfNotFound);
        }
    }

    static class LangDataTables {
        static final DataTables impl = DataTables.load("android.icu.impl.ICULangDataTables");
    }

    static class RegionDataTables {
        static final DataTables impl = DataTables.load("android.icu.impl.ICURegionDataTables");
    }

    public static enum DataTableType {
        LANG, REGION;
    }

    public static boolean haveData(DataTableType type) {
        switch (type) {
        case LANG: return LangDataTables.impl instanceof ICUDataTables;
        case REGION: return RegionDataTables.impl instanceof ICUDataTables;
        default:
            throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    private StringBuilder appendWithSep(String s, StringBuilder b) {
        if (b.length() == 0) {
            b.append(s);
        } else {
            SimpleFormatterImpl.formatAndReplace(separatorFormat, b, null, b, s);
        }
        return b;
    }

    private static class Cache {
        private ULocale locale;
        private DialectHandling dialectHandling;
        private DisplayContext capitalization;
        private DisplayContext nameLength;
        private DisplayContext substituteHandling;
        private LocaleDisplayNames cache;
        public LocaleDisplayNames get(ULocale locale, DialectHandling dialectHandling) {
            if (!(dialectHandling == this.dialectHandling && DisplayContext.CAPITALIZATION_NONE == this.capitalization &&
                    DisplayContext.LENGTH_FULL == this.nameLength && DisplayContext.SUBSTITUTE == this.substituteHandling &&
                    locale.equals(this.locale))) {
                this.locale = locale;
                this.dialectHandling = dialectHandling;
                this.capitalization = DisplayContext.CAPITALIZATION_NONE;
                this.nameLength = DisplayContext.LENGTH_FULL;
                this.substituteHandling = DisplayContext.SUBSTITUTE;
                this.cache = new LocaleDisplayNamesImpl(locale, dialectHandling);
            }
            return cache;
        }
        public LocaleDisplayNames get(ULocale locale, DisplayContext... contexts) {
            DialectHandling dialectHandlingIn = DialectHandling.STANDARD_NAMES;
            DisplayContext capitalizationIn = DisplayContext.CAPITALIZATION_NONE;
            DisplayContext nameLengthIn = DisplayContext.LENGTH_FULL;
            DisplayContext substituteHandling = DisplayContext.SUBSTITUTE;
            for (DisplayContext contextItem : contexts) {
                switch (contextItem.type()) {
                case DIALECT_HANDLING:
                    dialectHandlingIn = (contextItem.value()==DisplayContext.STANDARD_NAMES.value())?
                            DialectHandling.STANDARD_NAMES: DialectHandling.DIALECT_NAMES;
                    break;
                case CAPITALIZATION:
                    capitalizationIn = contextItem;
                    break;
                case DISPLAY_LENGTH:
                    nameLengthIn = contextItem;
                    break;
                case SUBSTITUTE_HANDLING:
                    substituteHandling = contextItem;
                    break;
                default:
                    break;
                }
            }
            if (!(dialectHandlingIn == this.dialectHandling && capitalizationIn == this.capitalization &&
                    nameLengthIn == this.nameLength && substituteHandling == this.substituteHandling &&
                    locale.equals(this.locale))) {
                this.locale = locale;
                this.dialectHandling = dialectHandlingIn;
                this.capitalization = capitalizationIn;
                this.nameLength = nameLengthIn;
                this.substituteHandling = substituteHandling;
                this.cache = new LocaleDisplayNamesImpl(locale, contexts);
            }
            return cache;
        }
    }
}
