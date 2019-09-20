/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ****************************************************************************************
 * Copyright (C) 2009-2016, Google, Inc.; International Business Machines Corporation
 * and others. All Rights Reserved.
 ****************************************************************************************
 */
package android.icu.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.Relation;
import android.icu.impl.Row;
import android.icu.impl.Row.R3;
import android.icu.impl.Utility;

/**
 * Provides a way to match the languages (locales) supported by a product to the
 * languages (locales) acceptable to a user, and get the best match. For
 * example:
 * 
 * <pre>
 * LocaleMatcher matcher = new LocaleMatcher("fr, en-GB, en");
 * 
 * // afterwards:
 * matcher.getBestMatch("en-US").toLanguageTag() =&gt; "en"
 * </pre>
 * 
 * It takes into account when languages are close to one another, such as fil
 * and tl, and when language regional variants are close, like en-GB and en-AU.
 * It also handles scripts, like zh-Hant vs zh-TW. For examples, see the test
 * file.
 * <p>All classes implementing this interface should be immutable. Often a
 * product will just need one static instance, built with the languages
 * that it supports. However, it may want multiple instances with different
 * default languages based on additional information, such as the domain.
 * 
 * @author markdavis@google.com
 * @hide Only a subset of ICU is exposed in Android
 */
public class LocaleMatcher {

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static final boolean DEBUG = false;

    private static final ULocale UNKNOWN_LOCALE = new ULocale("und");

    /**
     * Threshold for falling back to the default (first) language. May make this
     * a parameter in the future.
     */
    private static final double DEFAULT_THRESHOLD = 0.5;

    /**
     * The default language, in case the threshold is not met.
     */
    private final ULocale defaultLanguage;

    /**
     * The default language, in case the threshold is not met.
     */
    private final double threshold;

    /**
     * Create a new language matcher. The highest-weighted language is the
     * default. That means that if no other language is matches closer than a given
     * threshold, that default language is chosen. Typically the default is English,
     * but it could be different based on additional information, such as the domain
     * of the page.
     * 
     * @param languagePriorityList weighted list
     */
    public LocaleMatcher(LocalePriorityList languagePriorityList) {
        this(languagePriorityList, defaultWritten);
    }

    /**
     * Create a new language matcher from a String form. The highest-weighted
     * language is the default.
     * 
     * @param languagePriorityListString String form of LanguagePriorityList
     */
    public LocaleMatcher(String languagePriorityListString) {
        this(LocalePriorityList.add(languagePriorityListString).build());
    }

    /**
     * Internal testing function; may expose API later.
     * @param languagePriorityList LocalePriorityList to match
     * @param matcherData Internal matching data
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public LocaleMatcher(LocalePriorityList languagePriorityList, LanguageMatcherData matcherData) {
        this(languagePriorityList, matcherData, DEFAULT_THRESHOLD);
    }

    /**
     * Internal testing function; may expose API later.
     * @param languagePriorityList LocalePriorityList to match
     * @param matcherData Internal matching data
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public LocaleMatcher(LocalePriorityList languagePriorityList, LanguageMatcherData matcherData, double threshold) {
        this.matcherData = matcherData == null ? defaultWritten : matcherData.freeze();
        for (final ULocale language : languagePriorityList) {
            add(language, languagePriorityList.getWeight(language));
        }
        processMapping();
        Iterator<ULocale> it = languagePriorityList.iterator();
        defaultLanguage = it.hasNext() ? it.next() : null;
        this.threshold = threshold;
    }


    /**
     * Returns a fraction between 0 and 1, where 1 means that the languages are a
     * perfect match, and 0 means that they are completely different. Note that
     * the precise values may change over time; no code should be made dependent
     * on the values remaining constant.
     * @param desired Desired locale
     * @param desiredMax Maximized locale (using likely subtags)
     * @param supported Supported locale
     * @param supportedMax Maximized locale (using likely subtags)
     * @return value between 0 and 1, inclusive.
     */
    public double match(ULocale desired, ULocale desiredMax, ULocale supported, ULocale supportedMax) {
        return matcherData.match(desired, desiredMax, supported, supportedMax);
    }


    /**
     * Canonicalize a locale (language). Note that for now, it is canonicalizing
     * according to CLDR conventions (he vs iw, etc), since that is what is needed
     * for likelySubtags.
     * @param ulocale language/locale code
     * @return ULocale with remapped subtags.
     */
    public ULocale canonicalize(ULocale ulocale) {
        // TODO Get the data from CLDR, use Java conventions.
        String lang = ulocale.getLanguage();
        String lang2 = canonicalMap.get(lang);
        String script = ulocale.getScript();
        String script2 = canonicalMap.get(script);
        String region = ulocale.getCountry();
        String region2 = canonicalMap.get(region);
        if (lang2 != null || script2 != null || region2 != null) {
            return new ULocale(
                lang2 == null ? lang : lang2,
                    script2 == null ? script : script2,
                        region2 == null ? region : region2
                );
        }
        return ulocale;
    }

    /**
     * Get the best match for a LanguagePriorityList
     * 
     * @param languageList list to match
     * @return best matching language code
     */
    public ULocale getBestMatch(LocalePriorityList languageList) {
        double bestWeight = 0;
        ULocale bestTableMatch = null;
        double penalty = 0;
        OutputDouble matchWeight = new OutputDouble();
        for (final ULocale language : languageList) {
            final ULocale matchLocale = getBestMatchInternal(language, matchWeight);
            final double weight = matchWeight.value * languageList.getWeight(language) - penalty;
            if (weight > bestWeight) {
                bestWeight = weight;
                bestTableMatch = matchLocale;
            }
            penalty += 0.07000001;
        }
        if (bestWeight < threshold) {
            bestTableMatch = defaultLanguage;
        }
        return bestTableMatch;
    }

    /**
     * Convenience method: Get the best match for a LanguagePriorityList
     * 
     * @param languageList String form of language priority list
     * @return best matching language code
     */
    public ULocale getBestMatch(String languageList) {
        return getBestMatch(LocalePriorityList.add(languageList).build());
    }

    /**
     * Get the best match for an individual language code.
     * 
     * @param ulocale locale/language code to match
     * @return best matching language code
     */
    public ULocale getBestMatch(ULocale ulocale) {
        return getBestMatchInternal(ulocale, null);
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public ULocale getBestMatch(ULocale... ulocales) {
        return getBestMatch(LocalePriorityList.add(ulocales).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{" + defaultLanguage + ", " 
            + localeToMaxLocaleAndWeight + "}";
    }
    // ================= Privates =====================

    /**
     * Get the best match for an individual language code.
     * 
     * @param languageCode
     * @return best matching language code and weight (as per
     *         {@link #match(ULocale, ULocale)})
     */
    private ULocale getBestMatchInternal(ULocale languageCode, OutputDouble outputWeight) {
        languageCode = canonicalize(languageCode);
        final ULocale maximized = addLikelySubtags(languageCode);
        if (DEBUG) {
            System.out.println("\ngetBestMatchInternal: " + languageCode + ";\t" + maximized);
        }
        double bestWeight = 0;
        ULocale bestTableMatch = null;
        String baseLanguage = maximized.getLanguage();
        Set<R3<ULocale, ULocale, Double>> searchTable = desiredLanguageToPossibleLocalesToMaxLocaleToData.get(baseLanguage);
        if (searchTable != null) { // we preprocessed the table so as to filter by lanugage
            if (DEBUG) System.out.println("\tSearching: " + searchTable);
            for (final R3<ULocale, ULocale, Double> tableKeyValue : searchTable) {
                ULocale tableKey = tableKeyValue.get0();
                ULocale maxLocale = tableKeyValue.get1();
                Double matchedWeight = tableKeyValue.get2();
                final double match = match(languageCode, maximized, tableKey, maxLocale);
                if (DEBUG) {
                    System.out.println("\t" + tableKeyValue + ";\t" + match + "\n");
                }
                final double weight = match * matchedWeight;
                if (weight > bestWeight) {
                    bestWeight = weight;
                    bestTableMatch = tableKey;
                    if (weight > 0.999d) { // bail on good enough match.
                        break;
                    }
                }
            }
        }
        if (bestWeight < threshold) {
            bestTableMatch = defaultLanguage;
        }
        if (outputWeight != null) {
            outputWeight.value = bestWeight; // only return the weight when needed
        }
        return bestTableMatch;
    }
    
    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    private static class OutputDouble { // TODO, move to where OutputInt is
        double value;
    }

    private void add(ULocale language, Double weight) {
        language = canonicalize(language);
        R3<ULocale, ULocale, Double> row = Row.of(language, addLikelySubtags(language), weight);
        row.freeze();
        localeToMaxLocaleAndWeight.add(row);
    }

    /**
     * We preprocess the data to get just the possible matches for each desired base language. 
     */
    private void processMapping() {
        for (Entry<String, Set<String>> desiredToMatchingLanguages : matcherData.matchingLanguages().keyValuesSet()) {
            String desired = desiredToMatchingLanguages.getKey();
            Set<String> supported = desiredToMatchingLanguages.getValue();
            for (R3<ULocale, ULocale, Double> localeToMaxAndWeight : localeToMaxLocaleAndWeight) {
                final ULocale key = localeToMaxAndWeight.get0();
                String lang = key.getLanguage();
                if (supported.contains(lang)) {
                    addFiltered(desired, localeToMaxAndWeight);
                }
            }
        }
        // now put in the values directly, since languages always map to themselves
        for (R3<ULocale, ULocale, Double> localeToMaxAndWeight : localeToMaxLocaleAndWeight) {
            final ULocale key = localeToMaxAndWeight.get0();
            String lang = key.getLanguage();
            addFiltered(lang, localeToMaxAndWeight);
        }
    }

    private void addFiltered(String desired, R3<ULocale, ULocale, Double> localeToMaxAndWeight) {
        Set<R3<ULocale, ULocale, Double>> map = desiredLanguageToPossibleLocalesToMaxLocaleToData.get(desired);
        if (map == null) {
            desiredLanguageToPossibleLocalesToMaxLocaleToData.put(desired, map = new LinkedHashSet<R3<ULocale, ULocale, Double>>());
        }
        map.add(localeToMaxAndWeight);
        if (DEBUG) {
            System.out.println(desired + ", " + localeToMaxAndWeight);
        }
    }

    Set<Row.R3<ULocale, ULocale, Double>> localeToMaxLocaleAndWeight = new LinkedHashSet<Row.R3<ULocale, ULocale, Double>>();
    Map<String,Set<Row.R3<ULocale, ULocale, Double>>> desiredLanguageToPossibleLocalesToMaxLocaleToData 
    = new LinkedHashMap<String,Set<Row.R3<ULocale, ULocale, Double>>>();

    // =============== Special Mapping Information ==============

    /**
     * We need to add another method to addLikelySubtags that doesn't return
     * null, but instead substitutes Zzzz and ZZ if unknown. There are also
     * a few cases where addLikelySubtags needs to have expanded data, to handle
     * all deprecated codes.
     * @param languageCode
     * @return "fixed" addLikelySubtags
     */
    private ULocale addLikelySubtags(ULocale languageCode) {
        // max("und") = "en_Latn_US", and since matching is based on maximized tags, the undefined
        // language would normally match English.  But that would produce the counterintuitive results
        // that getBestMatch("und", LocaleMatcher("it,en")) would be "en", and
        // getBestMatch("en", LocaleMatcher("it,und")) would be "und".
        //
        // To avoid that, we change the matcher's definitions of max (AddLikelySubtagsWithDefaults)
        // so that max("und")="und". That produces the following, more desirable results:
        if (languageCode.equals(UNKNOWN_LOCALE)) {
            return UNKNOWN_LOCALE;
        }
        final ULocale result = ULocale.addLikelySubtags(languageCode);
        // should have method on getLikelySubtags for this
        if (result == null || result.equals(languageCode)) {
            final String language = languageCode.getLanguage();
            final String script = languageCode.getScript();
            final String region = languageCode.getCountry();
            return new ULocale((language.length()==0 ? "und"
                : language)
                + "_"
                + (script.length()==0 ? "Zzzz" : script)
                + "_"
                + (region.length()==0 ? "ZZ" : region));
        }
        return result;
    }

    private static class LocalePatternMatcher {
        // a value of null means a wildcard; matches any.
        private String lang;
        private String script;
        private String region;
        private Level level;
        static Pattern pattern = Pattern.compile(
            "([a-z]{1,8}|\\*)"
                + "(?:[_-]([A-Z][a-z]{3}|\\*))?"
                + "(?:[_-]([A-Z]{2}|[0-9]{3}|\\*))?");

        public LocalePatternMatcher(String toMatch) {
            Matcher matcher = pattern.matcher(toMatch);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Bad pattern: " + toMatch);
            }
            lang = matcher.group(1);
            script = matcher.group(2);
            region = matcher.group(3);
            level = region != null ? Level.region : script != null ? Level.script : Level.language;

            if (lang.equals("*")) {
                lang = null;
            }
            if (script != null && script.equals("*")) {
                script = null;
            }
            if (region != null && region.equals("*")) {
                region = null;
            }
        }

        boolean matches(ULocale ulocale) {
            if (lang != null && !lang.equals(ulocale.getLanguage())) {
                return false;
            }
            if (script != null && !script.equals(ulocale.getScript())) {
                return false;
            }
            if (region != null && !region.equals(ulocale.getCountry())) {
                return false;
            }
            return true;
        }

        public Level getLevel() {
            return level;
        }

        public String getLanguage() {
            return (lang == null ? "*" : lang);
        }

        public String getScript() {
            return (script == null ? "*" : script);
        }

        public String getRegion() {
            return (region == null ? "*" : region);
        }

        public String toString() {
            String result = getLanguage();
            if (level != Level.language) {
                result += "-" + getScript();
                if (level != Level.script) {
                    result += "-" + getRegion();
                }
            }
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !(obj instanceof LocalePatternMatcher)) {
                return false;
            }
            LocalePatternMatcher other = (LocalePatternMatcher) obj;
            return Utility.objectEquals(level, other.level)
                && Utility.objectEquals(lang, other.lang)
                && Utility.objectEquals(script, other.script)
                && Utility.objectEquals(region, other.region);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return level.ordinal()
                ^ (lang == null ? 0 : lang.hashCode())
                ^ (script == null ? 0 : script.hashCode())
                ^ (region == null ? 0 : region.hashCode());
        }
    }

    enum Level {
        language(0.99),
        script(0.2), 
        region(0.04);

        final double worst;

        Level(double d) {
            worst = d;
        }
    }

    private static class ScoreData implements Freezable<ScoreData> {
        @SuppressWarnings("unused")
        private static final double maxUnequal_changeD_sameS = 0.5;

        @SuppressWarnings("unused")
        private static final double maxUnequal_changeEqual = 0.75;

        LinkedHashSet<Row.R3<LocalePatternMatcher,LocalePatternMatcher,Double>> scores = new LinkedHashSet<R3<LocalePatternMatcher, LocalePatternMatcher, Double>>();
        final Level level;

        public ScoreData(Level level) {
            this.level = level;
        }

        void addDataToScores(String desired, String supported, R3<LocalePatternMatcher,LocalePatternMatcher,Double> data) {
            //            Map<String, Set<R3<LocalePatternMatcher,LocalePatternMatcher,Double>>> lang_result = scores.get(desired);
            //            if (lang_result == null) {
            //                scores.put(desired, lang_result = new HashMap());
            //            }
            //            Set<R3<LocalePatternMatcher,LocalePatternMatcher,Double>> result = lang_result.get(supported);
            //            if (result == null) {
            //                lang_result.put(supported, result = new LinkedHashSet());
            //            }
            //            result.add(data);
            boolean added = scores.add(data);
            if (!added) {
                throw new ICUException("trying to add duplicate data: " +  data);
            }
        }

        double getScore(ULocale dMax, String desiredRaw, String desiredMax, 
            ULocale sMax, String supportedRaw, String supportedMax) {
            double distance = 0;
            if (!desiredMax.equals(supportedMax)) {
                distance = getRawScore(dMax, sMax);
            } else if (!desiredRaw.equals(supportedRaw)) { // maxes are equal, changes are equal
                distance += 0.001;
            }
            return distance;
        }

        private double getRawScore(ULocale desiredLocale, ULocale supportedLocale) {
            if (DEBUG) {
                System.out.println("\t\t\t" + level + " Raw Score:\t" + desiredLocale + ";\t" + supportedLocale);
            }
            for (R3<LocalePatternMatcher,LocalePatternMatcher,Double> datum : scores) { // : result
                if (datum.get0().matches(desiredLocale) 
                    && datum.get1().matches(supportedLocale)) {
                    if (DEBUG) {
                        System.out.println("\t\t\t\tFOUND\t" + datum);
                    }
                    return datum.get2();
                }
            }
            if (DEBUG) {
                System.out.println("\t\t\t\tNOTFOUND\t" + level.worst);
            }
            return level.worst;
        }

        public String toString() {
            StringBuilder result = new StringBuilder().append(level);
            for (R3<LocalePatternMatcher, LocalePatternMatcher, Double> score : scores) {
                result.append("\n\t\t").append(score);
            }
            return result.toString();
        }


        @SuppressWarnings("unchecked")
        public ScoreData cloneAsThawed() {
            try {
                ScoreData result = (ScoreData) clone();
                result.scores = (LinkedHashSet<R3<LocalePatternMatcher, LocalePatternMatcher, Double>>) result.scores.clone();
                result.frozen = false;
                return result;
            } catch (CloneNotSupportedException e) {
                throw new ICUCloneNotSupportedException(e); // will never happen
            }

        }

        private volatile boolean frozen = false;

        public ScoreData freeze() {
            return this;
        }

        public boolean isFrozen() {
            return frozen;
        }

        public Relation<String,String> getMatchingLanguages() {
            Relation<String,String> desiredToSupported = Relation.of(new LinkedHashMap<String,Set<String>>(), HashSet.class);
            for (R3<LocalePatternMatcher, LocalePatternMatcher, Double> item : scores) {
                LocalePatternMatcher desired = item.get0();
                LocalePatternMatcher supported = item.get1();
                if (desired.lang != null && supported.lang != null) { // explicitly mentioned languages must have reasonable distance
                    desiredToSupported.put(desired.lang, supported.lang);
                }
            }
            desiredToSupported.freeze();
            return desiredToSupported;
        }
    }

    /**
     * Only for testing and use by tools. Interface may change!!
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static class LanguageMatcherData implements Freezable<LanguageMatcherData> {
        private ScoreData languageScores = new ScoreData(Level.language);
        private ScoreData scriptScores = new ScoreData(Level.script);
        private ScoreData regionScores = new ScoreData(Level.region);
        private Relation<String, String> matchingLanguages;
        private volatile boolean frozen = false;


        /**
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public LanguageMatcherData() {
        }

        /**
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public Relation<String, String> matchingLanguages() {
            return matchingLanguages;
        }

        /**
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public String toString() {
            return languageScores + "\n\t" + scriptScores + "\n\t" + regionScores;
        }

        /**
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public double match(ULocale a, ULocale aMax, ULocale b, ULocale bMax) {
            double diff = 0;
            diff += languageScores.getScore(aMax, a.getLanguage(), aMax.getLanguage(), bMax, b.getLanguage(), bMax.getLanguage());
            if (diff > 0.999d) { // with no language match, we bail
                return 0.0d;
            }
            diff += scriptScores.getScore(aMax, a.getScript(), aMax.getScript(), bMax, b.getScript(), bMax.getScript());
            diff += regionScores.getScore(aMax, a.getCountry(), aMax.getCountry(), bMax, b.getCountry(), bMax.getCountry());

            if (!a.getVariant().equals(b.getVariant())) {
                diff += 0.01;
            }
            if (diff < 0.0d) {
                diff = 0.0d;
            } else if (diff > 1.0d) {
                diff = 1.0d;
            }
            if (DEBUG) {
                System.out.println("\t\t\tTotal Distance\t" + diff);
            }
            return 1.0 - diff;
        }

        /**
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public LanguageMatcherData addDistance(String desired, String supported, int percent, String comment) {
            return addDistance(desired, supported, percent, false, comment);
        }
        /**
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public LanguageMatcherData addDistance(String desired, String supported, int percent, boolean oneway) {
            return addDistance(desired, supported, percent, oneway, null);
        }

        private LanguageMatcherData addDistance(String desired, String supported, int percent, boolean oneway, String comment) {
            if (DEBUG) {
                System.out.println("\t<languageMatch desired=\"" + desired + "\"" +
                    " supported=\"" + supported + "\"" +
                    " percent=\"" + percent + "\""
                    + (oneway ? " oneway=\"true\"" : "")
                    + "/>"
                    + (comment == null ? "" : "\t<!-- " + comment + " -->"));
                //                    //     .addDistance("nn", "nb", 4, true)
                //                        System.out.println(".addDistance(\"" + desired + "\"" +
                //                                ", \"" + supported + "\"" +
                //                                ", " + percent + ""
                //                                + (oneway ? "" : ", true")
                //                                + (comment == null ? "" : ", \"" + comment + "\"")
                //                                + ")"
                //                        );

            }
            double score = 1-percent/100.0; // convert from percentage
            LocalePatternMatcher desiredMatcher = new LocalePatternMatcher(desired);
            Level desiredLen = desiredMatcher.getLevel();
            LocalePatternMatcher supportedMatcher = new LocalePatternMatcher(supported);
            Level supportedLen = supportedMatcher.getLevel();
            if (desiredLen != supportedLen) {
                throw new IllegalArgumentException("Lengths unequal: " + desired + ", " + supported);
            }
            R3<LocalePatternMatcher,LocalePatternMatcher,Double> data = Row.of(desiredMatcher, supportedMatcher, score);
            R3<LocalePatternMatcher,LocalePatternMatcher,Double> data2 = oneway ? null : Row.of(supportedMatcher, desiredMatcher, score);
            boolean desiredEqualsSupported = desiredMatcher.equals(supportedMatcher);
            switch (desiredLen) {
            case language:
                String dlanguage = desiredMatcher.getLanguage();
                String slanguage = supportedMatcher.getLanguage();
                languageScores.addDataToScores(dlanguage, slanguage, data);
                if (!oneway && !desiredEqualsSupported) {
                    languageScores.addDataToScores(slanguage, dlanguage, data2);
                }
                break;
            case script:
                String dscript = desiredMatcher.getScript();
                String sscript = supportedMatcher.getScript();
                scriptScores.addDataToScores(dscript, sscript, data);
                if (!oneway && !desiredEqualsSupported) {
                    scriptScores.addDataToScores(sscript, dscript, data2);
                }
                break;
            case region:
                String dregion = desiredMatcher.getRegion();
                String sregion = supportedMatcher.getRegion();
                regionScores.addDataToScores(dregion, sregion, data);
                if (!oneway && !desiredEqualsSupported) {
                    regionScores.addDataToScores(sregion, dregion, data2);
                }
                break;
            }
            return this;
        }

        /** 
         * {@inheritDoc}
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public LanguageMatcherData cloneAsThawed() {
            LanguageMatcherData result;
            try {
                result = (LanguageMatcherData) clone();
                result.languageScores = languageScores.cloneAsThawed();
                result.scriptScores = scriptScores.cloneAsThawed();
                result.regionScores = regionScores.cloneAsThawed();
                result.frozen = false;
                return result;
            } catch (CloneNotSupportedException e) {
                throw new ICUCloneNotSupportedException(e); // will never happen
            }
        }

        /** 
         * {@inheritDoc}
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public LanguageMatcherData freeze() {
            languageScores.freeze();
            regionScores.freeze();
            scriptScores.freeze();
            matchingLanguages = languageScores.getMatchingLanguages();
            frozen = true;
            return this;
        }

        /** 
         * {@inheritDoc}
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        public boolean isFrozen() {
            return frozen;
        }
    }

    LanguageMatcherData matcherData;

    private static final LanguageMatcherData defaultWritten;

    private static HashMap<String,String> canonicalMap = new HashMap<String, String>();


    static {
        canonicalMap.put("iw", "he");
        canonicalMap.put("mo", "ro");
        canonicalMap.put("tl", "fil");

        ICUResourceBundle suppData = getICUSupplementalData();
        ICUResourceBundle languageMatching = suppData.findTopLevel("languageMatching");
        ICUResourceBundle written = (ICUResourceBundle) languageMatching.get("written");
        defaultWritten = new LanguageMatcherData();

        for(UResourceBundleIterator iter = written.getIterator(); iter.hasNext();) {
            ICUResourceBundle item = (ICUResourceBundle) iter.next();
            /*
            "*_*_*",
            "*_*_*",
            "96",
             */
            // <languageMatch desired="gsw" supported="de" percent="96" oneway="true" />
            boolean oneway = item.getSize() > 3 && "1".equals(item.getString(3));
            defaultWritten.addDistance(item.getString(0), item.getString(1), Integer.parseInt(item.getString(2)), oneway);
        }
        defaultWritten.freeze();
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static ICUResourceBundle getICUSupplementalData() {
        ICUResourceBundle suppData = (ICUResourceBundle) UResourceBundle.getBundleInstance(
            ICUData.ICU_BASE_NAME,
            "supplementalData",
            ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        return suppData;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static double match(ULocale a, ULocale b) {
        final LocaleMatcher matcher = new LocaleMatcher("");
        return matcher.match(a, matcher.addLikelySubtags(a), b, matcher.addLikelySubtags(b));
    }
}
